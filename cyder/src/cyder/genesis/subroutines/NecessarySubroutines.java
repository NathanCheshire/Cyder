package cyder.genesis.subroutines;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.ClassPath;
import cyder.annotations.*;
import cyder.enums.CyderInspection;
import cyder.enums.ExitCondition;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.genesis.CyderSplash;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.session.InstanceSocketUtil;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.user.UserUtil;
import cyder.utils.ArrayUtil;
import cyder.utils.OsUtil;
import cyder.utils.ReflectionUtil;
import cyder.utils.StaticUtil;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;

import static cyder.strings.CyderStrings.space;

/**
 * Subroutines which must complete in order for Cyder to start.
 */
public final class NecessarySubroutines {
    /**
     * The test key word to validate tests.
     */
    private static final String TEST = "test";

    /**
     * The standard primary method name for widget's {@link Widget} annotated method.
     */
    private static final String STANDARD_WIDGET_SHOW_METHOD_NAME = "showGui";

    /**
     * The vanilla developer names.
     */
    private static final ImmutableList<String> DEVELOPER_NAMES = ImmutableList.of(
            "Nathan Cheshire",
            "Nate Cheshire",
            "Natche",
            "Cypher"
    );

    /**
     * The key word to look for a class to end with it if contains a method annotated with {@link Widget}.
     */
    private static final String WIDGET = "widget";

    /**
     * The list of triggers for GuiTest methods.
     */
    private static final LinkedList<String> guiTestTriggers = new LinkedList<>();

    /**
     * The font directory name to load the true-type fonts from.
     */
    private static final String fonts = "fonts";

    /**
     * Suppress default constructor.
     */
    private NecessarySubroutines() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Executes the necessary subroutines on the main thread.
     * If any fail then the program is exited with the exit condition of {@link ExitCondition#NecessarySubroutineExit}.
     */
    public static void executeSubroutines() {
        for (Subroutine subroutine : subroutines) {
            CyderSplash.INSTANCE.setLoadingMessage(subroutine.getThreadName());
            Boolean ret = subroutine.getRoutine().get();

            if (ret == null || !ret) {
                throw new FatalException("todo on failure error message");
            }
        }

        CyderSplash.INSTANCE.setLoadingMessage("Ensuring singular instance");
        if (!InstanceSocketUtil.instanceSocketPortAvailable()) {
            ExceptionHandler.exceptionExit("Multiple instances of Cyder not allowed",
                    ExitCondition.MultipleInstancesExit, "Instances");
            throw new FatalException("Multiple instances of Cyder are not allowed");
        }

        InstanceSocketUtil.startListening();

        CyderSplash.INSTANCE.setLoadingMessage("Ensuring OS is supported");
        if (OsUtil.OperatingSystem.OSX.isCurrentOperatingSystem()) {
            throw new FatalException("Unsupported OS");
        }

        CyderSplash.INSTANCE.setLoadingMessage("Creating dynamics");
        OsUtil.ensureDynamicsCreated();

        CyderSplash.INSTANCE.setLoadingMessage("Validating users");
        UserUtil.validateUsers();

        CyderSplash.INSTANCE.setLoadingMessage("Cleaning users");
        UserUtil.cleanUsers();

        CyderSplash.INSTANCE.setLoadingMessage("Validating Widgets");
        validateWidgets();

        CyderSplash.INSTANCE.setLoadingMessage("Validating Vanilla annotations");
        validateVanillaWidgets();

        CyderSplash.INSTANCE.setLoadingMessage("Validating Handles");
        validateHandles();
    }

    private static final ImmutableList<Subroutine> subroutines = ImmutableList.of(
            new Subroutine(() -> {
                File[] fontFiles = StaticUtil.getStaticDirectory(fonts).listFiles();

                if (fontFiles == null || fontFiles.length == 0) return false;

                for (File fontFile : fontFiles) {
                    if (FileUtil.isSupportedFontExtension(fontFile)) {
                        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

                        try {
                            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
                            Logger.log(LogTag.FONT_LOADED, FileUtil.getFilename(fontFile));
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                            return false;
                        }
                    }
                }

                return true;
            }, "Registering fonts", "Registering fonts failed"),

            new Subroutine(NecessarySubroutines::validateGuiTests,
                    "Validating Gui Tests", "Validation of GuiTests failed")
    );


    /**
     * Finds all gui tests within Cyder by looking for methods annotated with {@link GuiTest}.
     * A Gui test is valid if the following conditions are met:
     * <ul>
     *     <li>The name ends with test</li>
     *     <li>The trigger is unique</li>
     * </ul>
     *
     * @return whether an invalid gui test was found
     */
    private static boolean validateGuiTests() {
        boolean ret = true;

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
            Class<?> clazz = classInfo.load();

            for (Method method : clazz.getMethods()) {
                if (!validateGuiTestMethod(method)) ret = false;
            }
        }

        return ret;
    }

    /**
     * Validates the provided GuiTest method.
     *
     * @param method the GuiTest method
     * @return whether the GuiTest method is valid
     */
    private static boolean validateGuiTestMethod(Method method) {
        Preconditions.checkNotNull(method);
        Preconditions.checkArgument(method.isAnnotationPresent(GuiTest.class));

        String trigger = method.getAnnotation(GuiTest.class).value();

        CyderInspection[] values = null;

        if (method.isAnnotationPresent(SuppressCyderInspections.class)) {
            values = method.getAnnotation(SuppressCyderInspections.class).value();
        }

        if (!method.getName().toLowerCase().endsWith(TEST)) {
            if (values != null) {
                if (ArrayUtil.toList(values).contains(CyderInspection.TestInspection)) {
                    return true;
                }
            }

            Logger.log(LogTag.GUI_TEST_WARNING, "Method annotated with @GuiTest does not end"
                    + " with \"test\"; name: " + method.getName());
        }

        if (StringUtil.in(trigger, true, guiTestTriggers)) {
            Logger.log(LogTag.GUI_TEST_WARNING, "Method annotation with @GuiTest "
                    + "has a trigger which has already been used; method: " + method.getName()
                    + ", trigger: " + trigger);
            return false;
        }

        guiTestTriggers.add(trigger);
        return true;
    }

    /**
     * Finds all widgets within Cyder by looking for methods annotated with {@link Widget}.
     * The annotated method MUST take no parameters, be named {@link #STANDARD_WIDGET_SHOW_METHOD_NAME},
     * contain a valid description, and contain at least one trigger.
     *
     * @throws IllegalMethodException if an invalid {@link Widget} annotation is located
     */
    private static void validateWidgets() {
        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
            Class<?> clazz = classInfo.load();

            for (Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(Widget.class)) {

                }
            }
        }
    }

    /**
     * Validates the provided widget method annotated with {@link Widget}.
     * A widget method is valid if the following conditions are met:
     * <ul>
     *     <li></li>
     * </ul>
     *
     * @param method the method to validate
     * @return whether the widget is valid
     */
    private static boolean validateWidget(Method method) {
        String[] triggers = method.getAnnotation(Widget.class).triggers();
        String description = method.getAnnotation(Widget.class).description();

        CyderInspection[] suppressionValues = new CyderInspection[]{};

        if (method.isAnnotationPresent(SuppressCyderInspections.class)) {
            suppressionValues = method.getAnnotation(SuppressCyderInspections.class).value();
        }

        // Not standard name so check for suppression
        if (!method.getName().equals(STANDARD_WIDGET_SHOW_METHOD_NAME)) {
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

            Logger.log(LogTag.WIDGET_WARNING, "Method annotated with @Widget is not named "
                    + STANDARD_WIDGET_SHOW_METHOD_NAME + "(); name: " + method.getName());
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
            Logger.log(LogTag.WIDGET_WARNING, "Method annotated with @Widget has empty triggers");
            return false;
        }

        boolean triggersValid = true;

        for (String trigger : triggers) {
            if (StringUtil.isNullOrEmpty(trigger)) {
                Logger.log(LogTag.WIDGET_WARNING, "Method annotated with @Widget has an empty trigger");
                triggersValid = false;
            }
        }

        return triggersValid;
    }

    /**
     * Validates all widget classes annotated with with {@link cyder.annotations.Vanilla} annotation.
     */
    public static void validateVanillaWidgets() {
        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
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
                    Logger.log(LogTag.VANILLA_WARNING, "Class annotated with @Vanilla does not end"
                            + " with Widget; name: " + clazz.getName());
                }

                if (!clazz.isAnnotationPresent(CyderAuthor.class)) {
                    Logger.log(LogTag.VANILLA_WARNING, "Method annotated with @Vanilla"
                            + " does not contain a @CyderAuthor annotation");
                } else {
                    String author = clazz.getAnnotation(CyderAuthor.class).author();

                    if (!StringUtil.in(author, true, DEVELOPER_NAMES)) {
                        Logger.log(LogTag.VANILLA_WARNING, "Method annotated with @Vanilla"
                                + " does not contain Nathan Cheshire as an author");
                    }
                }
            }
        }
    }

    /**
     * Validates all {@link Handle}s found throughout Cyder.
     */
    @SuppressWarnings("ConstantConditions") /* Check for final and primary handler */
    public static void validateHandles() {
        LinkedList<String> allTriggers = new LinkedList<>();

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
            Class<?> clazz = classInfo.load();

            ImmutableList<Method> handleMethods = ReflectionUtil.getHandleMethods(clazz);

            if (!ReflectionUtil.extendsInputHandler(clazz)) {
                if (handleMethods.size() > 0) {
                    logHandleWarning(HandleWarning.CONTAINS_HANDLE, ReflectionUtil.getBottomLevelClass(clazz));
                }
                continue;
            }

            if (ReflectionUtil.clazzContainsMoreThanOneHandle(clazz)) {
                logHandleWarning(HandleWarning.MORE_THAN_ONE_HANDLE, ReflectionUtil.getBottomLevelClass(clazz));
                continue;
            }

            if (handleMethods.size() < 1) continue;
            Method handleMethod = handleMethods.get(0);
            boolean suppressCyderInspectionsPresent = handleMethod.isAnnotationPresent(SuppressCyderInspections.class);
            boolean shouldSuppressHandleInspections = false;
            if (suppressCyderInspectionsPresent) {
                shouldSuppressHandleInspections = Arrays.stream(handleMethod.getAnnotation(
                                SuppressCyderInspections.class).value())
                        .anyMatch(suppressionValue -> suppressionValue == CyderInspection.HandleInspection);
            }

            if (shouldSuppressHandleInspections) continue;

            boolean isPrimaryHandle = ReflectionUtil.isPrimaryHandler(clazz);
            boolean isFinalHandler = ReflectionUtil.isFinalHandler(clazz);

            ImmutableList<String> triggers = ReflectionUtil.getHandleTriggers(handleMethod);

            if (isPrimaryHandle && triggers.size() < 1) {
                logHandleWarning(HandleWarning.MISSING_TRIGGER, ReflectionUtil.getBottomLevelClass(clazz));
                continue;
            } else if (isFinalHandler && triggers.size() > 0) {
                logHandleWarning(HandleWarning.FINAL_HANDLER_HAS_TRIGGERS, ReflectionUtil.getBottomLevelClass(clazz));
                continue;
            } else if (!isPrimaryHandle && !isFinalHandler) {
                logHandleWarning(HandleWarning.HANDLER_NOT_USED, ReflectionUtil.getBottomLevelClass(clazz));
                continue;
            } else if (isPrimaryHandle && isFinalHandler) {
                logHandleWarning(HandleWarning.PRIMARY_AND_FINAL, ReflectionUtil.getBottomLevelClass(clazz));
                continue;
            } else if (!ReflectionUtil.isPublicStaticBoolean(handleMethod)) {
                logHandleWarning(HandleWarning.NOT_PUBLIC_STATIC_BOOLEAN, ReflectionUtil.getBottomLevelClass(clazz));
                continue;
            }

            triggers.forEach(trigger -> {
                trigger = trigger.trim();

                if (StringUtil.isNullOrEmpty(trigger)) {
                    logHandleWarning(HandleWarning.EMPTY_TRIGGER, ReflectionUtil.getBottomLevelClass(clazz));
                } else {
                    if (allTriggers.contains(trigger)) {
                        logHandleWarning(HandleWarning.DUPLICATE_TRIGGER, trigger);
                    } else {
                        allTriggers.add(trigger);
                    }
                }
            });
        }
    }

    /**
     * Logs the provided handle warning and shows a popup to the user.
     *
     * @param handleWarning     the handle warning
     * @param classOrMethodName the method or class name that caused in the handle warning
     */
    private static void logHandleWarning(HandleWarning handleWarning, String classOrMethodName) {
        String errorMessagePrefix = handleWarning.getLogPrefix() + CyderStrings.colon + space + classOrMethodName;

        Logger.log(LogTag.HANDLE_WARNING, errorMessagePrefix);
        InformHandler.inform(new InformHandler.Builder(errorMessagePrefix).setTitle(
                StringUtil.capsFirst(handleWarning.name().replace(CyderStrings.underscore, space))));
    }
}
