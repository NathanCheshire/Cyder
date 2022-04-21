package cyder.utilities;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import cyder.annotations.*;
import cyder.constants.CyderStrings;
import cyder.enums.LoggerTag;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadFactory;
import cyder.ui.CyderFrame;
import cyder.utilities.objects.WidgetDescription;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

        if (reflectedFields == null || reflectedFields.isEmpty()) {
            reflectedFields = "No reflection data acquired";
        }

        return getBottomLevelClass(obj.getClass()) + ", hash = " + obj.hashCode()
                + ", reflection data = " + reflectedFields;
    }

    /**
     * A toString() replacement method used by most Cyder ui classes.
     *
     * @param obj the object to invoke toString() on
     * @return a custom toString() representation of the provided object
     */
    public static String commonCyderUIReflection(Component obj) {
        CyderFrame topFrame = (CyderFrame) SwingUtilities.getWindowAncestor(obj);
        String parentFrame;

        if (topFrame != null) {
            parentFrame = topFrame.getTitle();
        } else if (obj instanceof CyderFrame) {
            parentFrame = "Component is top frame";
        } else {
            parentFrame = "No parent frame";
        }

        // special methods to look for as a Component
        String getTitleResult = null;
        String getTextResult = null;
        String getTooltipResult = null;

        try {
            for (Method method : obj.getClass().getMethods()) {
                if (method.getName().startsWith("getText") && method.getParameterCount() == 0) {
                    Object locGetText = method.invoke(obj);

                    if (locGetText instanceof String) {
                        String locGetTextString = (String) locGetText;

                        if (locGetTextString != null && !locGetTextString.isEmpty()
                                && !locGetTextString.equalsIgnoreCase("null")) {
                            getTextResult = locGetTextString;
                        }
                    }
                } else if (method.getName().startsWith("getTooltipText") && method.getParameterCount() == 0) {
                    Object locGetTooltipText = method.invoke(obj);

                    if (locGetTooltipText instanceof String) {
                        String locGetTooltipTextString = (String) locGetTooltipText;

                        if (locGetTooltipTextString != null && !locGetTooltipTextString.isEmpty()
                                && !locGetTooltipTextString.equalsIgnoreCase("null")) {
                            getTooltipResult = locGetTooltipTextString;
                        }
                    }
                } else if (method.getName().startsWith("getTitle") && method.getParameterCount() == 0) {
                    Object locGetTitle = method.invoke(obj);

                    if (locGetTitle instanceof String) {
                        String locGetTitleString = (String) locGetTitle;

                        if (locGetTitleString != null && !locGetTitleString.isEmpty()
                                && !locGetTitleString.equalsIgnoreCase("null")) {
                            getTitleResult = locGetTitleString;
                        }
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        StringBuilder ret = new StringBuilder();
        ret.append("Component = " + getBottomLevelClass(obj.getClass()) + ", hash = " + obj.hashCode()
                + ", bounds = (" + obj.getX() + ", " + obj.getY() + ", " + obj.getWidth() + ", "
                + obj.getHeight() + ")");
        ret.append(", parent frame = " + parentFrame);

        if (!StringUtil.isNull(getTextResult)) {
            ret.append(", getText() = " + getTextResult);
        }

        if (!StringUtil.isNull(getTooltipResult)) {
            ret.append(", getTooltip() = " + getTooltipResult);
        }

        if (!StringUtil.isNull(getTitleResult)) {
            ret.append(", getTitle() = " + getTitleResult);
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

                        Logger.log(LoggerTag.DEBUG, "Method annotated with @Widget is not named " +
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
                    String trigger = m.getAnnotation(ManualTest.class).trigger();

                    String[] values = null;

                    if (m.isAnnotationPresent(SuppressCyderInspections.class))
                        values = m.getAnnotation(SuppressCyderInspections.class).values();

                    if (!m.getName().toLowerCase().endsWith("test")) {
                        if (values != null && StringUtil.in("TestInspection", false, values))
                            continue;

                        Logger.log(LoggerTag.DEBUG, "Method annotated with @ManualTest does not end" +
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
                    Logger.log(LoggerTag.DEBUG,
                            "Class annotated with @Vanilla does not end" +
                            " with Widget; name: " + clazz.getName());
                }

                if (!clazz.isAnnotationPresent(CyderAuthor.class)) {
                    Logger.log(LoggerTag.DEBUG, "Method annotated with @Vanilla does not contain" +
                            " a @CyderAuthor annotation");
                } else {
                    String author = clazz.getAnnotation(CyderAuthor.class).author();

                    if (!StringUtil.in(author, true, "Nathan Cheshire", "Natche", "Cypher")) {
                        Logger.log(LoggerTag.DEBUG, "Method annotated with @Vanilla does not contain" +
                                " Nathan Cheshire as an author");
                    }
                }
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

                                    Logger.log(LoggerTag.WIDGET_OPENED,
                                            shortWidgetName + ", trigger = " + trigger);

                                    return true;
                                } else throw new IllegalStateException("Found widget showGui()" +
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
     * Executor service used to find a similar command utilizing command_finder.py.
     */
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(
            new CyderThreadFactory("Similar Command Finder"));
    // todo will be removed after new ContextEngine and InputHandler implementation

    /**
     * Finds the most similar command to the unrecognized one the user provided.
     *
     * @param command the command to find a similar one to
     * @return the most similar command to the one provided
     */
    public static Future<Optional<String>> getSimilarCommand(String command) {
        return executor.submit(() -> {
            Optional<String> ret = Optional.empty();

            try {
                Runtime rt = Runtime.getRuntime();
                String[] commands = {"python",
                        OSUtil.buildPath("static", "python", "command_finder.py"),
                        command, String.valueOf(OSUtil.JAR_MODE)};
                Process proc = rt.exec(commands);

                BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String output = stdInput.readLine();
                ret = Optional.of(output);
            } catch (Exception e) {
                ExceptionHandler.silentHandle(e);
            }

            return ret;
        });
    }
}
