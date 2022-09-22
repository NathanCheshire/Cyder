package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import cyder.annotations.*;
import cyder.constants.CyderStrings;
import cyder.enums.CyderInspection;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.handlers.input.BaseInputHandler;
import cyder.handlers.input.InputHandler;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.logging.Logger;
import cyder.ui.frame.CyderFrame;
import org.apache.commons.text.similarity.JaroWinklerDistance;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Utilities for Jvm reflection.
 */
public final class ReflectionUtil {
    /**
     * Suppress default constructor.
     */
    private ReflectionUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns whether one or more of the provided objects are null.
     *
     * @param object  the first object
     * @param objects the additional objects (optional)
     * @return whether one or more of the provided objects were found to be null
     */
    public static boolean someAreNull(Object object, Object... objects) {
        if (object == null) return true;

        for (Object obj : objects) {
            if (obj == null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a String representation of the provided object
     * using all public get() methods found.
     *
     * @param obj the object to build into a String
     * @return the string representation of the object
     */
    private static String buildGetterString(Object obj) {
        Preconditions.checkNotNull(obj);

        StringBuilder ret = new StringBuilder();

        ret.append("class = ");
        ret.append(getBottomLevelClass(obj.getClass()));
        ret.append(", ");

        for (Method m : obj.getClass().getMethods()) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0) {
                try {
                    ret.append(m.getName());
                    ret.append(" = ");
                    ret.append(m.invoke(obj));
                    ret.append(", ");
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }
        }

        String retString = ret.toString().trim();
        return retString.substring(0, retString.length() - 3).trim();
    }

    /**
     * Finds all getters associated with the provided class and returns a list
     * containing the toString representation of all values returned by all getters.
     *
     * @param clazz the class to find all getters of
     * @return a list of strings resulting from the get calls on the provided class
     */
    public static LinkedList<String> getGetters(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        LinkedList<String> ret = new LinkedList<>();

        for (Method m : clazz.getMethods()) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0) {
                try {
                    ret.add(m.invoke(clazz).toString());
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }
        }

        return ret;
    }

    /**
     * A common method utilized by near all top-level Cyder classes
     * as the overridden logic for their toString() methods.
     *
     * @param obj the obj to return a String representation for
     * @return the String representation for the provided object
     * detailing the classname, hashcode, and reflected data
     */
    public static String commonCyderToString(Object obj) {
        Preconditions.checkNotNull(obj);

        String reflectedFields = buildGetterString(obj);

        if (reflectedFields.isEmpty()) {
            reflectedFields = "No reflection data acquired";
        }

        return getBottomLevelClass(obj.getClass()) + ", hash = " + obj.hashCode()
                + ", reflection data = " + reflectedFields;
    }

    /**
     * A class used for reflection to find special methods within an object.
     */
    private static class SpecialMethod {
        private final String startsWith;
        private final String logPattern;
        private String methodResult;

        public SpecialMethod(String startsWith, String logPattern) {
            this.startsWith = startsWith;
            this.logPattern = logPattern;
        }

        public String getStartsWith() {
            return startsWith;
        }

        public String getLogPattern() {
            return logPattern;
        }

        public String getMethodResult() {
            return methodResult;
        }

        public void setMethodResult(String methodResult) {
            this.methodResult = methodResult;
        }
    }

    /**
     * Special methods which should be found and invoked if found when reflecting on a ui component.
     */
    private static final ImmutableList<SpecialMethod> specialMethods = ImmutableList.of(
            new SpecialMethod("getText", ", getText() = "),
            new SpecialMethod("getTooltipText", ", getTooltipText() = "),
            new SpecialMethod("getTitle", ", getTitle() = ")
    );

    /**
     * A toString() replacement method used by most Cyder ui classes.
     *
     * @param comp the object to invoke toString() on
     * @return a custom toString() representation of the provided object
     */
    public static String commonCyderUiToString(Component comp) {
        CyderFrame topFrame = (CyderFrame) SwingUtilities.getWindowAncestor(comp);

        String parentFrame = topFrame != null
                ? topFrame.getTitle()
                : comp instanceof CyderFrame
                ? "Component itself is a CyderFrame"
                : "No parent frame found";

        try {
            for (Method method : comp.getClass().getMethods()) {
                for (SpecialMethod specialMethod : specialMethods) {
                    if (method.getName().startsWith(specialMethod.startsWith)
                            && method.getParameterCount() == 0) {
                        Object localInvokeResult = method.invoke(comp);

                        if (localInvokeResult instanceof String localInvokeResultString) {
                            if (!localInvokeResultString.isEmpty()
                                    && !StringUtil.isNullOrEmpty(localInvokeResultString)) {
                                specialMethod.setMethodResult(specialMethod.getLogPattern()
                                        + localInvokeResultString);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        StringBuilder ret = new StringBuilder();

        ret.append("Component = ")
                .append(getBottomLevelClass(comp.getClass()))
                .append(", hash = ")
                .append(comp.hashCode())
                .append(", bounds = (").append(comp.getX()).append(", ").append(comp.getY())
                .append(", ").append(comp.getWidth()).append(", ").append(comp.getHeight()).append(")");

        ret.append(", parent frame = ").append(parentFrame);

        for (SpecialMethod specialMethod : specialMethods) {
            if (!StringUtil.isNullOrEmpty(specialMethod.getMethodResult())) {
                ret.append(specialMethod.getMethodResult());
            }
        }

        return ret.toString();
    }

    /**
     * Returns the name of the class without all the package info.
     * Example: if {@link CyderFrame} was provided, typically invoking
     * {@link CyderFrame#toString()} would return "cyder.ui.CyderFrame"
     * (with its hashcode appended of course). This method will simply return "CyderFrame."
     *
     * @param clazz the class to find the name of
     * @return the bottom level class name
     */
    public static String getBottomLevelClass(Class<?> clazz) {
        String superName = clazz.toString();

        boolean inner = false;

        // remove inner class IDs
        if (superName.contains("$")) {
            superName = superName.split("\\$")[0];
            inner = true;
        }

        // remove package info
        if (superName.contains(".")) {
            String[] parts = superName.split("\\.");
            superName = parts[parts.length - 1];
        }

        return superName + (inner ? " (inner)" : "");
    }

    /**
     * The top level package for Cyder.
     */
    public static final String TOP_LEVEL_PACKAGE = "cyder";

    /**
     * A set of all classes contained within Cyder starting at {@link ReflectionUtil#TOP_LEVEL_PACKAGE}.
     */
    public static ImmutableSet<ClassPath.ClassInfo> CYDER_CLASSES;

    static {
        try {
            CYDER_CLASSES = ClassPath.from(Thread.currentThread().getContextClassLoader())
                    .getTopLevelClassesRecursive(TOP_LEVEL_PACKAGE);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * The standard primary method name for widget's @Widget annotated method.
     */
    public static final String STANDARD_WIDGET_SHOW_METHOD_NAME = "showGui";

    /**
     * Finds all widgets within Cyder by looking for methods annotated with {@link Widget}.
     * The annotated method MUST take no parameters, be named
     * {@link ReflectionUtil#STANDARD_WIDGET_SHOW_METHOD_NAME},
     * contain a valid description, and contain at least one trigger.
     *
     * @throws IllegalMethodException if an invalid {@link Widget} annotation is located
     */
    public static void validateWidgets() {
        for (ClassPath.ClassInfo classInfo : CYDER_CLASSES) {
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

                        Logger.log(Logger.Tag.DEBUG, "Method annotated with @Widget is not named "
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
     * Finds all manual tests within Cyder by looking for methods annotated with {@link ManualTest}.
     * The annotated method MUST take no parameters, and contain a valid, unique trigger.
     *
     * @throws IllegalMethodException if an invalid {@link ManualTest} annotation is located
     */
    public static void validateTests() {
        LinkedList<String> foundTriggers = new LinkedList<>();

        for (ClassPath.ClassInfo classInfo : CYDER_CLASSES) {
            Class<?> clazz = classInfo.load();

            for (Method m : clazz.getMethods()) {
                if (m.isAnnotationPresent(ManualTest.class)) {
                    String trigger = m.getAnnotation(ManualTest.class).value();

                    CyderInspection[] values = null;

                    if (m.isAnnotationPresent(SuppressCyderInspections.class))
                        values = m.getAnnotation(SuppressCyderInspections.class).value();

                    if (!m.getName().toLowerCase().endsWith("test")) {
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

                        Logger.log(Logger.Tag.DEBUG, "Method annotated with @ManualTest does not end"
                                + " with \"test\"; name: " + m.getName());
                    }

                    if (StringUtil.isNullOrEmpty(trigger)) {
                        throw new IllegalMethodException("Method annotated with @ManualTest has no trigger");
                    }

                    if (StringUtil.in(trigger, true, foundTriggers)) {
                        throw new IllegalArgumentException("Method annotation with @ManualTest "
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
            = ImmutableList.of("Nathan Cheshire", "Natche", "Cypher");

    /**
     * The key word to look for a class to end with it if contains a method annotated with {@link Widget}.
     */
    private static final String WIDGET = "widget";

    /**
     * Validates all widget classes annotated with with {@link cyder.annotations.Vanilla} annotation.
     */
    public static void validateVanillaWidgets() {
        for (ClassPath.ClassInfo classInfo : CYDER_CLASSES) {
            Class<?> clazz = classInfo.load();

            if (clazz.isAnnotationPresent(Vanilla.class)) {
                if (clazz.isAnnotationPresent(SuppressCyderInspections.class)) {
                    CyderInspection[] values = clazz.getAnnotation(SuppressCyderInspections.class).value();

                    if (values != null) {
                        boolean in = false;

                        for (CyderInspection inspection : values) {
                            if (inspection == CyderInspection.VanillaInspection) {
                                in = true;
                                break;
                            }
                        }

                        if (in) {
                            continue;
                        }
                    }
                }

                boolean widgetAnnotationFound = false;

                for (Method method : clazz.getMethods()) {
                    if (method.getName().toLowerCase().endsWith(WIDGET)) {
                        widgetAnnotationFound = true;
                        break;
                    }
                }

                if (!clazz.getName().toLowerCase().endsWith(WIDGET) && !widgetAnnotationFound) {
                    Logger.log(Logger.Tag.DEBUG, "Class annotated with @Vanilla does not end"
                            + " with Widget; name: " + clazz.getName());
                }

                if (!clazz.isAnnotationPresent(CyderAuthor.class)) {
                    Logger.log(Logger.Tag.DEBUG, "Method annotated with @Vanilla"
                            + " does not contain a @CyderAuthor annotation");
                } else {
                    String author = clazz.getAnnotation(CyderAuthor.class).author();

                    if (!StringUtil.in(author, true, DEVELOPER_NAMES)) {
                        Logger.log(Logger.Tag.DEBUG, "Method annotated with @Vanilla"
                                + " does not contain Nathan Cheshire as an author");
                    }
                }
            }
        }
    }

    /**
     * Ensures there are no duplicate props within the loaded props.
     */
    public static void ensureNoDuplicateProps() {
        ArrayList<String> discoveredKeys = new ArrayList<>();

        for (PropLoader.Prop prop : PropLoader.getProps()) {
            if (!StringUtil.in(prop.key(), false, discoveredKeys)) {
                discoveredKeys.add(prop.key());
            } else {
                Logger.log(Logger.Tag.DEBUG, "Found duplicate prop key: " + prop.key());
            }
        }
    }

    /**
     * A widget and it's name and triggers.
     */
    public record WidgetDescription(String name, String description, String[] triggers) {}

    /**
     * Returns a list of names, descriptions, and triggers of all the widgets found within Cyder.
     *
     * @return a list of descriptions of all the widgets found within Cyder
     */
    public static ArrayList<WidgetDescription> getWidgetDescriptions() {
        ArrayList<WidgetDescription> ret = new ArrayList<>();

        for (ClassPath.ClassInfo classInfo : CYDER_CLASSES) {
            Class<?> clazz = classInfo.load();

            for (Method m : clazz.getMethods()) {
                if (m.isAnnotationPresent(Widget.class)) {
                    String[] triggers = m.getAnnotation(Widget.class).triggers();
                    String description = m.getAnnotation(Widget.class).description();
                    ret.add(new WidgetDescription(clazz.getName(), description, triggers));
                }
            }
        }

        return ret;
    }

    @ForReadability
    public static boolean moreThanOneHandle(Class<?> clazz) {
        return getHandleMethods(clazz).size() > 1;
    }

    @ForReadability
    private static boolean extendsInputHandler(Class<?> clazz) {
        return InputHandler.class.isAssignableFrom(clazz);
    }

    @ForReadability
    private static ImmutableList<Method> getHandleMethods(Class<?> clazz) {
        LinkedList<Method> ret = new LinkedList<>();

        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(Handle.class)) {
                ret.add(method);
            }
        }

        return ImmutableList.copyOf(ret);
    }

    @ForReadability
    private static boolean isFinalHandler(Class<?> clazz) {
        return BaseInputHandler.finalHandlers.contains(clazz);
    }

    @ForReadability
    private static boolean isPrimaryHandler(Class<?> clazz) {
        return BaseInputHandler.primaryHandlers.contains(clazz);
    }

    @ForReadability
    private static ImmutableList<String> getTriggers(Method method) {
        String[] triggers = method.getAnnotation(Handle.class).value();
        LinkedList<String> ret = new LinkedList<>();
        for (String trigger : triggers) {
            if (!StringUtil.isNullOrEmpty(trigger)) {
                ret.add(trigger);
            }
        }
        return ImmutableList.copyOf(ret);
    }

    @ForReadability
    private static boolean isPublic(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }

    @ForReadability
    private static boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    @ForReadability
    public static boolean returnsBoolean(Method method) {
        return method.getReturnType() == boolean.class;
    }

    @ForReadability
    private static boolean isPublicStaticBoolean(Method method) {
        return isPublic(method) && isStatic(method) && returnsBoolean(method);
    }

    @ForReadability
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

        Logger.log(Logger.Tag.DEBUG, errorString);
        InformHandler.inform(new InformHandler.Builder(errorString).setTitle(
                StringUtil.capsFirst(handleWarning.name().replace("_", ""))));
    }

    /**
     * Validates all handles throughout Cyder.
     */
    @SuppressWarnings("ConstantConditions")
    public static void validateHandles() {
        LinkedList<String> allTriggers = new LinkedList<>();

        for (ClassPath.ClassInfo classInfo : CYDER_CLASSES) {
            Class<?> clazz = classInfo.load();

            ImmutableList<Method> handleMethods = getHandleMethods(clazz);

            if (!extendsInputHandler(clazz)) {
                if (handleMethods.size() > 0) {
                    logHandleWarning(HandleWarning.CONTAINS_HANDLE, getBottomLevelClass(clazz));
                }
                continue;
            }

            if (moreThanOneHandle(clazz)) {
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

            ImmutableList<String> triggers = getTriggers(handleMethod);

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

    /**
     * A record representing a found similar command how close the
     * found command is to the original string.
     */
    public record SimilarCommand(Optional<String> command, double tolerance) {}

    /**
     * Finds the most similar command to the unrecognized one provided.
     *
     * @param command the user entered command to attempt to find a similar command to
     * @return the most similar command to the one provided
     */
    public static SimilarCommand getSimilarCommand(String command) {
        Preconditions.checkNotNull(command);
        Preconditions.checkArgument(!command.isEmpty());

        String mostSimilarTrigger = "";
        float mostSimilarRatio = 0.0f;

        for (ClassPath.ClassInfo classInfo : CYDER_CLASSES) {
            Class<?> clazz = classInfo.load();

            for (Method m : clazz.getMethods()) {
                ImmutableList<String> triggers = ImmutableList.of();

                if (m.isAnnotationPresent(Handle.class)) {
                    triggers = ImmutableList.copyOf(m.getAnnotation(Handle.class).value());
                } else if (m.isAnnotationPresent(Widget.class)) {
                    triggers = ImmutableList.copyOf(m.getAnnotation(Widget.class).triggers());
                }

                for (String trigger : triggers) {
                    double ratio = new JaroWinklerDistance().apply(trigger, command);

                    if (ratio > mostSimilarRatio) {
                        mostSimilarRatio = (float) ratio;
                        mostSimilarTrigger = trigger;
                    }
                }
            }
        }

        return new SimilarCommand(StringUtil.isNullOrEmpty(mostSimilarTrigger)
                ? Optional.empty() : Optional.of(mostSimilarTrigger), mostSimilarRatio);
    }

    /**
     * Returns a list the available manual tests that follow the standard
     * naming convention to the linked JTextPane.
     *
     * @return a list of triggers for manual tests
     */
    public static ImmutableList<String> getManualTests() {
        LinkedList<String> ret = new LinkedList<>();

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.CYDER_CLASSES) {
            Class<?> clazz = classInfo.load();

            for (Method m : clazz.getMethods()) {
                if (m.isAnnotationPresent(ManualTest.class)) {
                    String trigger = m.getAnnotation(ManualTest.class).value();
                    ret.add(trigger);
                }
            }
        }

        return ImmutableList.copyOf(ret);
    }
}
