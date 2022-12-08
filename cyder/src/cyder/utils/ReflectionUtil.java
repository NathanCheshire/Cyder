package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.ClassPath;
import cyder.annotations.Handle;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.input.BaseInputHandler;
import cyder.handlers.input.InputHandler;
import cyder.handlers.internal.ExceptionHandler;
import cyder.strings.StringUtil;
import cyder.ui.frame.CyderFrame;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import static cyder.strings.CyderStrings.*;

/**
 * Utilities for Jvm reflection.
 */
public final class ReflectionUtil {
    /**
     * Suppress default constructor.
     */
    private ReflectionUtil() {
        throw new IllegalMethodException(ATTEMPTED_INSTANTIATION);
    }

    /**
     * The class string.
     */
    private static final String clazz = "class";

    /**
     * The get string.
     */
    private static final String GET = "get";

    /**
     * Returns a String representation of the provided object
     * using all public accessor methods found.
     *
     * @param object the object to build into a String
     * @return the string representation of the object
     */
    public static String buildGetterString(Object object) {
        Preconditions.checkNotNull(object);

        StringBuilder ret = new StringBuilder();

        ret.append(clazz).append(colon).append(space);
        ret.append(getBottomLevelClass(object.getClass()));
        ret.append(comma).append(space);

        for (Method m : object.getClass().getMethods()) {
            if (m.getName().startsWith(GET) && m.getParameterTypes().length == 0) {
                try {
                    ret.append(m.getName());
                    ret.append(colon).append(space);
                    ret.append(m.invoke(object));
                    ret.append(comma).append(space);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }
        }

        String retString = ret.toString();
        retString = retString.trim().substring(0, retString.length() - 1).trim();
        return retString;
    }

    /**
     * Finds all getters associated with the provided class and returns a list
     * containing the method names of all the public accessor methods.
     *
     * @param clazz the class to find all getters of
     * @return a list of getter names
     */
    public static ImmutableList<String> getGetterNames(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        LinkedList<String> ret = new LinkedList<>();

        for (Method m : clazz.getMethods()) {
            if (m.getName().startsWith(GET) && m.getParameterTypes().length == 0) {
                try {
                    ret.add(m.invoke(clazz).toString());
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * The set string used to locate setter/mutator methods of an object.
     */
    private static final String SET = "set";

    /**
     * Finds all setters associated with the provided class and returns a list
     * containing the method names of all the public mutator methods.
     *
     * @param clazz the class to find all mutators of
     * @return a list of setter names
     */
    public static ImmutableList<String> getSetterNames(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        LinkedList<String> ret = new LinkedList<>();

        for (Method m : clazz.getMethods()) {
            if (m.getName().startsWith(SET) && m.getParameterTypes().length == 1) {
                try {
                    ret.add(m.invoke(clazz).toString());
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns the name of the class without all the package info.
     * Example: if {@link CyderFrame} was provided, typically invoking {@link CyderFrame#toString()}
     * would return "cyder.ui.CyderFrame" (with its hashcode appended of course).
     * This method will simply return "CyderFrame".
     *
     * @param clazz the class to find the name of
     * @return the bottom level class name
     */
    public static String getBottomLevelClass(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        String superName = clazz.toString();

        boolean innerClass = superName.contains("$");
        if (innerClass) superName = superName.split("\\$")[0];

        // Remove package info (cyder.ui.CyderFrame)
        if (superName.contains(".")) {
            String[] parts = superName.split("\\.");
            superName = ArrayUtil.getLastElement(parts);
        }

        String ret = superName;
        if (innerClass) {
            ret += space + openingParenthesis + "inner class" + closingParenthesis;
        }

        return ret;
    }

    /**
     * The top level package for Cyder.
     */
    public static final String TOP_LEVEL_PACKAGE_NAME = "cyder";

    /**
     * A set of all classes contained within Cyder starting at {@link ReflectionUtil#TOP_LEVEL_PACKAGE_NAME}.
     */
    private static final ImmutableList<ClassPath.ClassInfo> cyderClasses;

    /**
     * Returns the class info objects of all classes found within the current build of Cyder.
     *
     * @return the class info objects of all classes found within the current build of Cyder
     */
    public static ImmutableList<ClassPath.ClassInfo> getCyderClasses() {
        return cyderClasses;
    }

    static {
        ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
        ClassPath cyderClassPath = null;

        try {
            cyderClassPath = ClassPath.from(currentThreadClassLoader);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        cyderClasses = cyderClassPath == null
                ? ImmutableList.of()
                : ImmutableList.copyOf(cyderClassPath.getTopLevelClassesRecursive(TOP_LEVEL_PACKAGE_NAME));
    }

    /**
     * Returns whether the provided class contains more than one {@link Handle} annotation.
     *
     * @param clazz the class
     * @return whether the class contains more than one handle annotation
     */
    public static boolean clazzContainsMoreThanOneHandle(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return getHandleMethods(clazz).size() > 1;
    }

    /**
     * Returns whether the provided class extends the {@link InputHandler}.
     *
     * @param clazz the class
     * @return whether the provided class extends the input handler
     */
    public static boolean extendsInputHandler(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return InputHandler.class.isAssignableFrom(clazz);
    }

    /**
     * Returns the methods annotated with {@link Handle} in the provided class.
     *
     * @param clazz the class
     * @return the handle methods found
     */
    public static ImmutableList<Method> getHandleMethods(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        ArrayList<Method> ret = new ArrayList<>();
        Arrays.stream(clazz.getMethods()).filter(method -> method.isAnnotationPresent(Handle.class)).forEach(ret::add);

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns whether the provided class is contained in {@link BaseInputHandler}s final handlers list.
     *
     * @param clazz the class
     * @return whether the provided class is contained in the final handlers list
     */
    public static boolean isFinalHandler(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return BaseInputHandler.finalHandlers.contains(clazz);
    }

    /**
     * Returns whether the provided class is contained in {@link BaseInputHandler}s primary handles list.
     *
     * @param clazz the class
     * @return whether the provided class is contained in the primary handles list
     */
    public static boolean isPrimaryHandler(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return BaseInputHandler.primaryHandlers.contains(clazz);
    }

    /**
     * Returns the triggers of the handle annotation on the provided method.
     *
     * @param method the method
     * @return the handle triggers
     */
    public static ImmutableList<String> getHandleTriggers(Method method) {
        Preconditions.checkNotNull(method);

        String[] triggers = method.getAnnotation(Handle.class).value();
        ArrayList<String> ret = new ArrayList<>();
        Arrays.stream(triggers).filter(trigger -> !StringUtil.isNullOrEmpty(trigger)).forEach(ret::add);
        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns whether the provided method is public.
     *
     * @param method the method
     * @return whether the method is public
     */
    public static boolean isPublic(Method method) {
        Preconditions.checkNotNull(method);

        return Modifier.isPublic(method.getModifiers());
    }

    /**
     * Returns whether the provided method is static.
     *
     * @param method the method
     * @return whether the provided method is static
     */
    public static boolean isStatic(Method method) {
        Preconditions.checkNotNull(method);

        return Modifier.isStatic(method.getModifiers());
    }

    /**
     * Returns whether the provided method returns a {@link Boolean} type.
     *
     * @param method the method
     * @return whether the method returns a boolean
     */
    public static boolean returnsBoolean(Method method) {
        Preconditions.checkNotNull(method);

        return method.getReturnType() == boolean.class;
    }

    /**
     * Returns whether the provided method returns a {@link Void} type.
     *
     * @param method the method
     * @return whether the method returns a void type
     */
    public static boolean returnsVoid(Method method) {
        Preconditions.checkNotNull(method);

        return method.getReturnType().equals(Void.TYPE);
    }

    /**
     * Returns whether the provided method is public, static, and returns a boolean type.
     *
     * @param method the method
     * @return whether the method is public, static, and returns a boolean type
     */
    public static boolean isPublicStaticBoolean(Method method) {
        Preconditions.checkNotNull(method);

        return isPublic(method) && isStatic(method) && returnsBoolean(method);
    }
}
