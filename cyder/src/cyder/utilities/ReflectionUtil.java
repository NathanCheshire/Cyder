package cyder.utilities;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import cyder.annotations.*;
import cyder.common.WidgetDescription;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderFrame;
import cyder.widgets.CardWidget;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Utilities for methods regarding reflection.
 */
public class ReflectionUtil {
    /**
     * Prevent illegal class instantiation.
     */
    private ReflectionUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
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
    @SuppressWarnings("unused")
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
     * A toString() replacement method used by most Cyder ui classes.
     *
     * @param obj the object to invoke toString() on
     * @return a custom toString() representation of the provided object
     */
    public static String commonCyderUIReflection(Component obj) {
        CyderFrame topFrame = (CyderFrame) SwingUtilities.getWindowAncestor(obj);

        String parentFrame = topFrame != null
                ? topFrame.getTitle()
                : obj instanceof CyderFrame
                ? "Component itself is a CyderFrame"
                : "No parent frame found";

        ImmutableList<SpecialMethod> specialMethods = ImmutableList.of(
                new SpecialMethod("getText", ", getText() = "),
                new SpecialMethod("getTooltipText", ", getTooltipText() = "),
                new SpecialMethod("getTitle", ", getTitle() = ")
        );

        try {
            for (Method method : obj.getClass().getMethods()) {
                for (SpecialMethod specialMethod : specialMethods) {
                    if (method.getName().startsWith(specialMethod.startsWith)
                            && method.getParameterCount() == 0) {
                        Object localInvokeResult = method.invoke(obj);

                        if (localInvokeResult instanceof String localInvokeResultString) {
                            if (!localInvokeResultString.isEmpty()
                                    && !StringUtil.isNull(localInvokeResultString)) {
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
                .append(getBottomLevelClass(obj.getClass()))
                .append(", hash = ")
                .append(obj.hashCode())
                .append(", bounds = (").append(obj.getX()).append(", ").append(obj.getY())
                .append(", ").append(obj.getWidth()).append(", ").append(obj.getHeight()).append(")");

        ret.append(", parent frame = ").append(parentFrame);

        for (SpecialMethod specialMethod : specialMethods) {
            if (!StringUtil.isNull(specialMethod.getMethodResult())) {
                ret.append(specialMethod.getMethodResult());
            }
        }

        return ret.toString();
    }

    /**
     * Returns the name of the class without all the package info.
     * Example: if {@link CyderFrame} was provided, typically invoking
     * getClass() on CyderFrame would return "cyder.ui.CyderFrame" but
     * this method will simply return CyderFrame.
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
    public static ImmutableSet<ClassPath.ClassInfo> cyderClasses;

    // load cyder classes at runtime
    static {
        try {
            cyderClasses = ClassPath
                    .from(Thread.currentThread().getContextClassLoader())
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
        for (ClassPath.ClassInfo classInfo : cyderClasses) {
            Class<?> classer = classInfo.load();

            for (Method m : classer.getMethods()) {
                if (m.isAnnotationPresent(Widget.class)) {
                    String[] triggers = m.getAnnotation(Widget.class).triggers();
                    String description = m.getAnnotation(Widget.class).description();

                    String[] suppressionValues = null;

                    if (m.isAnnotationPresent(SuppressCyderInspections.class))
                        suppressionValues = m.getAnnotation(SuppressCyderInspections.class).values();

                    if (!m.getName().equals(STANDARD_WIDGET_SHOW_METHOD_NAME)) {
                        if (suppressionValues != null && StringUtil.in("WidgetInspection",
                                false, suppressionValues))
                            continue;

                        Logger.log(Logger.Tag.DEBUG, "Method annotated with @Widget is not named " +
                                STANDARD_WIDGET_SHOW_METHOD_NAME + "(); name: " + m.getName());
                    }

                    if (StringUtil.isNull(description)) {
                        if (suppressionValues != null && StringUtil.in("WidgetInspection",
                                false, suppressionValues))
                            continue;

                        throw new IllegalMethodException("Method annotated with @Widget has empty description");
                    }

                    if (triggers.length == 0) {
                        throw new IllegalMethodException("Method annotated with @Widget has empty triggers");
                    }

                    for (String trigger : triggers) {
                        if (StringUtil.isNull(trigger)) {
                            throw new IllegalMethodException("Method annotated with @Widget has an empty trigger");
                        } else if (trigger.contains(" ")) {
                            if (suppressionValues != null && StringUtil.in("WidgetInspection",
                                    false, suppressionValues))
                                continue;

                            throw new IllegalMethodException("Method annotated with " +
                                    "@Widget has triggers which contain spaces: \"" + trigger + "\"");
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

        for (ClassPath.ClassInfo classInfo : cyderClasses) {
            Class<?> classer = classInfo.load();

            for (Method m : classer.getMethods()) {
                if (m.isAnnotationPresent(ManualTest.class)) {
                    String trigger = m.getAnnotation(ManualTest.class).value();

                    String[] values = null;

                    if (m.isAnnotationPresent(SuppressCyderInspections.class))
                        values = m.getAnnotation(SuppressCyderInspections.class).values();

                    if (!m.getName().toLowerCase().endsWith("test")) {
                        if (values != null && StringUtil.in("TestInspection", false, values))
                            continue;

                        Logger.log(Logger.Tag.DEBUG, "Method annotated with @ManualTest does not end" +
                                " with \"test\"; name: " + m.getName());
                    }

                    if (StringUtil.isNull(trigger)) {
                        throw new IllegalMethodException("Method annotated with @ManualTest has no trigger");
                    }

                    if (StringUtil.in(trigger, true, foundTriggers)) {
                        throw new IllegalArgumentException("Method annotation with @ManualTest " +
                                "has a trigger which has already been used; method: " + m.getName()
                                + ", trigger: " + trigger);
                    }

                    foundTriggers.add(trigger);
                }
            }
        }
    }

    /**
     * Validates all widget classes annotated with with {@link cyder.annotations.Vanilla} annotation.
     */
    public static void validateVanillaWidgets() {
        for (ClassPath.ClassInfo classInfo : cyderClasses) {
            Class<?> clazz = classInfo.load();

            if (clazz.isAnnotationPresent(Vanilla.class)) {
                if (clazz.isAnnotationPresent(SuppressCyderInspections.class)) {
                    String[] values = clazz.getAnnotation(SuppressCyderInspections.class).values();

                    // if set to ignore, go to next class
                    if (StringUtil.in("VanillaInspection", true, values)) {
                        continue;
                    }
                }

                if (!clazz.getName().toLowerCase().endsWith("widget")) {
                    Logger.log(Logger.Tag.DEBUG,
                            "Class annotated with @Vanilla does not end" +
                                    " with Widget; name: " + clazz.getName());
                }

                if (!clazz.isAnnotationPresent(CyderAuthor.class)) {
                    Logger.log(Logger.Tag.DEBUG, "Method annotated with @Vanilla does not contain" +
                            " a @CyderAuthor annotation");
                } else {
                    String author = clazz.getAnnotation(CyderAuthor.class).author();

                    if (!StringUtil.in(author, true, "Nathan Cheshire", "Natche", "Cypher")) {
                        Logger.log(Logger.Tag.DEBUG, "Method annotated with @Vanilla does not contain" +
                                " Nathan Cheshire as an author");
                    }
                }
            }
        }
    }

    /**
     * Validates all the props within props.ini.
     */
    public static void validateProps() {
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
     * Returns a list of names, descriptions, and triggers of all the widgets found within Cyder.
     *
     * @return a list of descriptions of all the widgets found within Cyder
     */
    public static ArrayList<WidgetDescription> getWidgetDescriptions() {
        ArrayList<WidgetDescription> ret = new ArrayList<>();

        for (ClassPath.ClassInfo classInfo : cyderClasses) {
            Class<?> classer = classInfo.load();

            for (Method m : classer.getMethods()) {
                if (m.isAnnotationPresent(Widget.class)) {
                    String[] triggers = m.getAnnotation(Widget.class).triggers();
                    String description = m.getAnnotation(Widget.class).description();
                    ret.add(new WidgetDescription(classer.getName(), description, triggers));
                }
            }
        }

        return ret;
    }

    /**
     * Opens the widget with the same trigger as the one provided.
     * If for some reason someone was an idiot and put a duplicate trigger
     * for a widget in Cyder, the first occurrence will be invoked.
     *
     * @param trigger the trigger for the widget to open
     * @return whether a widget was opened
     */
    public static boolean openWidget(String trigger) {
        Preconditions.checkNotNull(trigger);
        Preconditions.checkArgument(!trigger.isEmpty());

        for (ClassPath.ClassInfo classInfo : cyderClasses) {
            Class<?> clazz = classInfo.load();

            for (Method m : clazz.getMethods()) {
                if (m.isAnnotationPresent(Widget.class)) {
                    String[] widgetTriggers = m.getAnnotation(Widget.class).triggers();

                    for (String widgetTrigger : widgetTriggers) {
                        if (widgetTrigger.equalsIgnoreCase(trigger)) {
                            String shortWidgetName = getBottomLevelClass(clazz);
                            ConsoleFrame.INSTANCE.getInputHandler().println("Opening widget: " + shortWidgetName);
                            try {
                                if (m.getParameterCount() == 0) {
                                    m.invoke(clazz);

                                    Logger.log(Logger.Tag.WIDGET_OPENED,
                                            shortWidgetName + ", trigger = " + trigger);

                                    return true;
                                } else
                                    throw new IllegalStateException("Found widget showGui()" +
                                            " annotated method with parameters: " + m.getName() + ", class: " + clazz);
                            } catch (Exception e) {
                                ExceptionHandler.handle(e);
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Invokes the method with the name holiday + year from the CardsWidget.
     *
     * @param holiday the holiday name such as Christmas
     * @param year    the year of the holiday such as 2021
     */
    public static void cardInvoker(String holiday, int year) {
        try {
            for (Method m : CardWidget.class.getMethods()) {
                if (m.getName().toLowerCase().contains(holiday.toLowerCase())
                        && m.getName().toLowerCase().contains(String.valueOf(year))) {
                    m.invoke(CardWidget.class);
                }
            }
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
        }
    }

    /**
     * A record representing a found similar command how close the
     * found command is to the original string.
     */
    public static record SimilarCommand(Optional<String> command, float tolerance) {
    }

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
        float tol = 100.0f;

        for (ClassPath.ClassInfo classInfo : cyderClasses) {
            Class<?> clazz = classInfo.load();

            for (Method m : clazz.getMethods()) {
                if (m.isAnnotationPresent(Handle.class)) {
                    String[] triggers = m.getAnnotation(Handle.class).value();

                    for (String trigger : triggers) {
                        int ld = StringUtil.levenshteinDistance(trigger, command);
                        float difference = ld / (float) command.length();

                        if (difference < tol) {
                            tol = difference;
                            mostSimilarTrigger = trigger;
                        }
                    }
                }
            }
        }

        return new SimilarCommand(StringUtil.isNull(mostSimilarTrigger)
                ? Optional.empty() : Optional.of(mostSimilarTrigger), tol);
    }
}
