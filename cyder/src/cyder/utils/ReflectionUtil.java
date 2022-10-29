package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.ClassPath;
import cyder.annotations.*;
import cyder.enums.CyderInspection;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.input.BaseInputHandler;
import cyder.handlers.input.InputHandler;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.ui.frame.CyderFrame;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import static cyder.constants.CyderStrings.*;

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
     * using all public get() methods found.
     *
     * @param object the object to build into a String
     * @return the string representation of the object
     */
    private static String buildGetterString(Object object) {
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
     * A common method utilized by near all top-level Cyder classes
     * as the overridden logic for their {@link Object#toString()} implementation.
     *
     * @param object the object
     * @return the String representation for the provided object
     * detailing the classname, hashcode, and reflected data detailed by the {@link GetterMethodResult}s
     */
    public static String commonCyderToString(Object object) {
        Preconditions.checkNotNull(object);

        String reflectedFields = buildGetterString(object);
        return getBottomLevelClass(object.getClass()) + ", hash: " + object.hashCode()
                + ", reflection data: " + reflectedFields;
    }

    // todo string util

    /**
     * A class used for reflection to find special methods within an object.
     */
    private static class GetterMethodResult {
        /**
         * The prefix for the method name to start with.
         */
        private final String startsWith;

        /**
         * The result of invoking the getter method.
         */
        private String methodResult;

        /**
         * Constructs a new getter method result.
         *
         * @param startsWith what the method should start with
         */
        public GetterMethodResult(String startsWith) {
            this.startsWith = Preconditions.checkNotNull(startsWith);
        }

        /**
         * Returns what the method should start with.
         *
         * @return what the method should start with
         */
        public String getStartsWith() {
            return startsWith;
        }

        /**
         * returns the method result if set.
         *
         * @return the method result if set
         */
        public String getMethodResult() {
            Preconditions.checkState(methodResult != null);

            return methodResult;
        }

        /**
         * Sets the method result.
         *
         * @param methodResult the method result
         */
        public void setMethodResult(String methodResult) {
            Preconditions.checkNotNull(methodResult);

            this.methodResult = methodResult;
        }
    }

    // todo string util
    /**
     * Special methods which should be found and invoked if found when reflecting on a ui component.
     */
    private static final ImmutableList<GetterMethodResult> getterMethods = ImmutableList.of(
            new GetterMethodResult("getText"),
            new GetterMethodResult("getTooltipText"),
            new GetterMethodResult("getTitle")
    );

    // todo string util
    /**
     * Returns a string representation of the provided component's top level frame parent if found.
     *
     * @param component the component
     * @return a string representation of the provided component's top level frame parent
     */
    public static String getComponentFrameRepresentation(Component component) {
        Preconditions.checkNotNull(component);

        CyderFrame topFrame = (CyderFrame) SwingUtilities.getWindowAncestor(component);

        return topFrame != null
                ? topFrame.getTitle()
                : component instanceof CyderFrame
                ? "Component itself is a CyderFrame"
                : "No parent frame found";
    }

    // todo string util
    /**
     * A string representation of {@link Component}s used by most Cyder ui classes for logging.
     *
     * @param component the component
     * @return a string representation of the provided component
     */
    public static String commonCyderUiToString(Component component) {
        Preconditions.checkNotNull(component);

        String parentFrame = getComponentFrameRepresentation(component);

        try {
            for (Method method : component.getClass().getMethods()) {
                for (GetterMethodResult getterMethod : getterMethods) {
                    if (method.getName().startsWith(getterMethod.startsWith)
                            && method.getParameterCount() == 0) {
                        Object localInvokeResult = method.invoke(component);

                        if (localInvokeResult instanceof String localInvokeResultString) {
                            if (!localInvokeResultString.isEmpty()
                                    && !StringUtil.isNullOrEmpty(localInvokeResultString)) {
                                getterMethod.setMethodResult(localInvokeResultString);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        StringBuilder ret = new StringBuilder();

        ret.append("Component: ")
                .append(getBottomLevelClass(component.getClass()))
                .append(", hash: ")
                .append(component.hashCode())
                .append(", bounds: (").append(component.getX()).append(", ").append(component.getY())
                .append(", ").append(component.getWidth()).append(", ").append(component.getHeight())
                .append(closingParenthesis);

        ret.append(", parent frame: ").append(parentFrame);

        getterMethods.forEach(specialMethod -> {
            String result = specialMethod.getMethodResult();
            if (result != null && !result.isEmpty()) {
                ret.append(comma).append(space).append(specialMethod.getStartsWith()).append(colon).append(space);
                ret.append(result);
            }
        });

        return ret.toString();
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
        boolean inner = false;

        // Remove inner class IDs
        if (superName.contains("$")) {
            superName = superName.split("\\$")[0];
            inner = true;
        }

        // Remove package info
        if (superName.contains(".")) {
            String[] parts = superName.split("\\.");
            superName = parts[parts.length - 1];
        }

        String ret = superName;
        if (inner) {
            ret += space + openingParenthesis + "inner" + closingParenthesis;
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


    // todo validation utils?
    /**
     * The standard primary method name for widget's @Widget annotated method.
     */
    public static final String STANDARD_WIDGET_SHOW_METHOD_NAME = "showGui";

    /**
     * Finds all widgets within Cyder by looking for methods annotated with {@link Widget}.
     * The annotated method MUST take no parameters, be named {@link ReflectionUtil#STANDARD_WIDGET_SHOW_METHOD_NAME},
     * contain a valid description, and contain at least one trigger.
     *
     * @throws IllegalMethodException if an invalid {@link Widget} annotation is located
     */
    public static void validateWidgets() {
        for (ClassPath.ClassInfo classInfo : cyderClasses) {
            Class<?> clazz = classInfo.load();

            for (Method m : clazz.getMethods()) {
                if (m.isAnnotationPresent(Widget.class)) {
                    String[] triggers = m.getAnnotation(Widget.class).triggers();
                    String description = m.getAnnotation(Widget.class).description();

                    CyderInspection[] suppressionValues = null;

                    if (m.isAnnotationPresent(SuppressCyderInspections.class)) {
                        suppressionValues = m.getAnnotation(SuppressCyderInspections.class).value();
                    }

                    if (!m.getName().equals(STANDARD_WIDGET_SHOW_METHOD_NAME)) {
                        if (suppressionValues != null) {
                            boolean in = false;

                            for (CyderInspection inspection : suppressionValues) {
                                if (inspection == CyderInspection.WidgetInspection) {
                                    in = true;
                                    break;
                                }
                            }

                            if (in) {
                                continue;
                            }
                        }

                        Logger.log(LogTag.DEBUG, "Method annotated with @Widget is not named "
                                + STANDARD_WIDGET_SHOW_METHOD_NAME + "(); name: " + m.getName());
                    }

                    if (StringUtil.isNullOrEmpty(description)) {
                        if (suppressionValues != null) {
                            boolean in = false;

                            for (CyderInspection inspection : suppressionValues) {
                                if (inspection == CyderInspection.WidgetInspection) {
                                    in = true;
                                    break;
                                }
                            }

                            if (in) {
                                continue;
                            }
                        }

                        throw new IllegalMethodException("Method annotated with @Widget has empty description");
                    }

                    if (triggers.length == 0) {
                        throw new IllegalMethodException("Method annotated with @Widget has empty triggers");
                    }

                    for (String trigger : triggers) {
                        if (StringUtil.isNullOrEmpty(trigger)) {
                            throw new IllegalMethodException("Method annotated with @Widget has an empty trigger");
                        }
                    }
                }
            }
        }
    }

    /**
     * The test key word.
     */
    private static final String TEST = "test";

    /**
     * Finds all gui tests within Cyder by looking for methods annotated with {@link GuiTest}.
     * The annotated method MUST take no parameters, and contain a valid, unique trigger.
     *
     * @throws IllegalMethodException if an invalid {@link GuiTest} annotation is located
     */
    public static void validateTests() {
        LinkedList<String> foundTriggers = new LinkedList<>();

        for (ClassPath.ClassInfo classInfo : cyderClasses) {
            Class<?> clazz = classInfo.load();

            for (Method m : clazz.getMethods()) {
                if (m.isAnnotationPresent(GuiTest.class)) {
                    String trigger = m.getAnnotation(GuiTest.class).value();

                    CyderInspection[] values = null;

                    if (m.isAnnotationPresent(SuppressCyderInspections.class)) {
                        values = m.getAnnotation(SuppressCyderInspections.class).value();
                    }

                    if (!m.getName().toLowerCase().endsWith(TEST)) {
                        if (values != null) {
                            boolean in = false;

                            for (CyderInspection inspection : values) {
                                if (inspection == CyderInspection.TestInspection) {
                                    in = true;
                                    break;
                                }
                            }

                            if (in) {
                                continue;
                            }
                        }

                        Logger.log(LogTag.DEBUG, "Method annotated with @GuiTest does not end"
                                + " with \"test\"; name: " + m.getName());
                    }

                    if (StringUtil.in(trigger, true, foundTriggers)) {
                        throw new IllegalArgumentException("Method annotation with @GuiTest "
                                + "has a trigger which has already been used; method: " + m.getName()
                                + ", trigger: " + trigger);
                    }

                    foundTriggers.add(trigger);
                }
            }
        }
    }

    /**
     * The vanilla developer names.
     */
    private static final ImmutableList<String> DEVELOPER_NAMES
            = ImmutableList.of("Nathan Cheshire", "Natche", "Cypher", "Nate Cheshire");

    /**
     * The key word to look for a class to end with it if contains a method annotated with {@link Widget}.
     */
    private static final String WIDGET = "widget";

    /**
     * Validates all widget classes annotated with with {@link cyder.annotations.Vanilla} annotation.
     */
    public static void validateVanillaWidgets() {
        for (ClassPath.ClassInfo classInfo : cyderClasses) {
            Class<?> clazz = classInfo.load();

            if (clazz.isAnnotationPresent(Vanilla.class)) {
                if (clazz.isAnnotationPresent(SuppressCyderInspections.class)) {
                    CyderInspection[] suppressedInspections = clazz.getAnnotation(
                            SuppressCyderInspections.class).value();

                    if (suppressedInspections != null) {
                        if (Arrays.stream(suppressedInspections).anyMatch(cyderInspection
                                -> cyderInspection == CyderInspection.VanillaInspection)) {
                            continue;
                        }
                    }
                }

                boolean widgetAnnotationFound = Arrays.stream(clazz.getMethods())
                        .anyMatch(method -> method.getName().toLowerCase().endsWith(WIDGET));

                if (!clazz.getName().toLowerCase().endsWith(WIDGET) && !widgetAnnotationFound) {
                    Logger.log(LogTag.DEBUG, "Class annotated with @Vanilla does not end"
                            + " with Widget; name: " + clazz.getName());
                }

                if (!clazz.isAnnotationPresent(CyderAuthor.class)) {
                    Logger.log(LogTag.DEBUG, "Method annotated with @Vanilla"
                            + " does not contain a @CyderAuthor annotation");
                } else {
                    String author = clazz.getAnnotation(CyderAuthor.class).author();

                    if (!StringUtil.in(author, true, DEVELOPER_NAMES)) {
                        Logger.log(LogTag.DEBUG, "Method annotated with @Vanilla"
                                + " does not contain Nathan Cheshire as an author");
                    }
                }
            }
        }
    }

    // todo maybe remove reflection util? Methods could be in specific classes as needed
    // todo have a subroutine when the prefs is open to reload from disk if files are added
    // todo maybe design and implement a directory listener class

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
     * Returns whether the provided method is public, static, and returns a boolean type.
     *
     * @param method the method
     * @return whether the method is public, static, and returns a boolean type
     */
    private static boolean isPublicStaticBoolean(Method method) {
        return isPublic(method) && isStatic(method) && returnsBoolean(method);
    }

    /**
     * The possible warnings for invalid {@link Handle}s.
     */
    private enum HandleWarning {
        CONTAINS_HANDLE,
        MORE_THAN_ONE_HANDLE,
        MISSING_TRIGGER,
        FINAL_HANDLER_HAS_TRIGGERS,
        HANDLER_NOT_USED,
        PRIMARY_AND_FINAL,
        NOT_PUBLIC_STATIC_BOOLEAN,
        EMPTY_TRIGGER,
        DUPLICATE_TRIGGER
    }

    @SuppressWarnings("UnnecessaryDefault")
    private static void logHandleWarning(HandleWarning handleWarning, String classOrMethodName) {
        String errorString = switch (handleWarning) {
            case CONTAINS_HANDLE -> "Found class which does not extend InputHandler"
                    + " with @Handle annotation: " + classOrMethodName;
            case MORE_THAN_ONE_HANDLE -> "Found class which contains more than one method"
                    + " annotated with @Handle: " + classOrMethodName;
            case MISSING_TRIGGER -> "Primary handle class found to be missing triggers: " + classOrMethodName;
            case FINAL_HANDLER_HAS_TRIGGERS -> "Final handle class found to contain triggers: " + classOrMethodName;
            case HANDLER_NOT_USED -> "Handle class not contained in primary or final handlers: " + classOrMethodName;
            case PRIMARY_AND_FINAL -> "Handle class found to be contained in both primary"
                    + " and final lists: " + classOrMethodName;
            case NOT_PUBLIC_STATIC_BOOLEAN -> "Method annotated with @Handle found to not"
                    + " be public static boolean: " + classOrMethodName;
            case EMPTY_TRIGGER -> "Handle annotation found to contain empty triggers: " + classOrMethodName;
            case DUPLICATE_TRIGGER -> "Found duplicate trigger, trigger: " + classOrMethodName;
            default -> throw new IllegalArgumentException("Illegal handle warning: " + handleWarning);
        };

        Logger.log(LogTag.DEBUG, errorString);
        InformHandler.inform(new InformHandler.Builder(errorString).setTitle(
                StringUtil.capsFirst(handleWarning.name().replace("_", space))));
    }

    /**
     * Validates all handles throughout Cyder.
     */
    @SuppressWarnings("ConstantConditions")
    public static void validateHandles() {
        LinkedList<String> allTriggers = new LinkedList<>();

        for (ClassPath.ClassInfo classInfo : cyderClasses) {
            Class<?> clazz = classInfo.load();

            ImmutableList<Method> handleMethods = getHandleMethods(clazz);

            if (!extendsInputHandler(clazz)) {
                if (handleMethods.size() > 0) {
                    logHandleWarning(HandleWarning.CONTAINS_HANDLE, getBottomLevelClass(clazz));
                }
                continue;
            }

            if (clazzContainsMoreThanOneHandle(clazz)) {
                logHandleWarning(HandleWarning.MORE_THAN_ONE_HANDLE, getBottomLevelClass(clazz));
                continue;
            }

            if (handleMethods.size() < 1) continue;
            Method handleMethod = handleMethods.get(0);
            boolean suppressCyderInspectionsPresent = handleMethod.isAnnotationPresent(SuppressCyderInspections.class);
            boolean shouldSuppressHandleInspections = false;
            if (suppressCyderInspectionsPresent) {
                ImmutableList<CyderInspection> suppressionValues = ImmutableList.copyOf(
                        handleMethod.getAnnotation(SuppressCyderInspections.class).value());

                for (CyderInspection suppressionValue : suppressionValues) {
                    if (suppressionValue == CyderInspection.HandleInspection) {
                        shouldSuppressHandleInspections = true;
                        break;
                    }
                }
            }

            if (shouldSuppressHandleInspections) continue;

            ImmutableList<String> triggers = getHandleTriggers(handleMethod);

            boolean isPrimaryHandle = isPrimaryHandler(clazz);
            boolean isFinalHandler = isFinalHandler(clazz);

            if (isPrimaryHandle && triggers.size() < 1) {
                logHandleWarning(HandleWarning.MISSING_TRIGGER, getBottomLevelClass(clazz));
                continue;
            }

            if (isFinalHandler && triggers.size() > 0) {
                logHandleWarning(HandleWarning.FINAL_HANDLER_HAS_TRIGGERS, getBottomLevelClass(clazz));
                continue;
            }

            if (!isPrimaryHandle && !isFinalHandler) {
                logHandleWarning(HandleWarning.HANDLER_NOT_USED, getBottomLevelClass(clazz));
                continue;
            }

            if (isPrimaryHandle && isFinalHandler) {
                logHandleWarning(HandleWarning.PRIMARY_AND_FINAL, getBottomLevelClass(clazz));
                continue;
            }

            if (!isPublicStaticBoolean(handleMethod)) {
                logHandleWarning(HandleWarning.NOT_PUBLIC_STATIC_BOOLEAN, getBottomLevelClass(clazz));
                continue;
            }

            for (String trigger : triggers) {
                trigger = trigger.trim();

                if (StringUtil.isNullOrEmpty(trigger)) {
                    logHandleWarning(HandleWarning.EMPTY_TRIGGER, getBottomLevelClass(clazz));
                    continue;
                }

                if (allTriggers.contains(trigger)) {
                    logHandleWarning(HandleWarning.DUPLICATE_TRIGGER, trigger);
                } else {
                    allTriggers.add(trigger);
                }
            }
        }
    }
}
