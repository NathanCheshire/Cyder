package cyder.genesis.subroutines;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.ClassPath;
import cyder.annotations.*;
import cyder.constants.CyderStrings;
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
import cyder.user.UserUtil;
import cyder.utils.*;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;

import static cyder.constants.CyderStrings.space;

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
    public static final String STANDARD_WIDGET_SHOW_METHOD_NAME = "showGui";

    /**
     * The vanilla developer names.
     */
    private static final ImmutableList<String> DEVELOPER_NAMES = ImmutableList.of(
            "Nathan Cheshire", "Nate Cheshire", "Natche", "Cypher");

    /**
     * The key word to look for a class to end with it if contains a method annotated with {@link Widget}.
     */
    private static final String WIDGET = "widget";

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
    public static void execute() {
        try {
            CyderSplash.INSTANCE.setLoadingMessage("Registering fonts");
            if (!registerFonts()) {
                throw new FatalException("Registering fonts failed");
            }

            CyderSplash.INSTANCE.setLoadingMessage("Ensuring singular instance");
            if (!InstanceSocketUtil.instanceSocketPortAvailable()) {
                throw new FatalException("Could not bind to instance socket port: "
                        + InstanceSocketUtil.getInstanceSocketPort());
            }
            InstanceSocketUtil.bind();

            CyderSplash.INSTANCE.setLoadingMessage("Ensuring OS is supported");
            if (OsUtil.isOsx()) {
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

            CyderSplash.INSTANCE.setLoadingMessage("Validating Tests");
            validateTests();

            CyderSplash.INSTANCE.setLoadingMessage("Validating Vanilla annotations");
            validateVanillaWidgets();

            CyderSplash.INSTANCE.setLoadingMessage("Validating Handles");
            validateHandles();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            OsUtil.exit(ExitCondition.NecessarySubroutineExit);
        }
    }

    /**
     * Registers the fonts within static/fonts.
     *
     * @return whether the fonts could be loaded
     */
    private static boolean registerFonts() {
        File[] fontFiles = StaticUtil.getStaticDirectory("fonts").listFiles();

        if (fontFiles == null || fontFiles.length == 0) {
            return false;
        }

        for (File fontFile : fontFiles) {
            if (FileUtil.isSupportedFontExtension(fontFile)) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                try {
                    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
                    Logger.log(LogTag.FONT_LOADED, FileUtil.getFilename(fontFile));
                } catch (Exception e) {
                    ExceptionHandler.silentHandle(e);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Finds all gui tests within Cyder by looking for methods annotated with {@link GuiTest}.
     * The annotated method MUST take no parameters, and contain a valid, unique trigger.
     *
     * @throws IllegalMethodException if an invalid {@link GuiTest} annotation is located
     */
    public static void validateTests() {
        LinkedList<String> foundTriggers = new LinkedList<>();

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
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

                        Logger.log(LogTag.GUI_TEST_WARNING, "Method annotated with @GuiTest does not end"
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
     * Finds all widgets within Cyder by looking for methods annotated with {@link Widget}.
     * The annotated method MUST take no parameters, be named {@link #STANDARD_WIDGET_SHOW_METHOD_NAME},
     * contain a valid description, and contain at least one trigger.
     *
     * @throws IllegalMethodException if an invalid {@link Widget} annotation is located
     */
    public static void validateWidgets() {
        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
            Class<?> clazz = classInfo.load();

            for (Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(Widget.class)) {
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
                StringUtil.capsFirst(handleWarning.name().replace("_", space))));
    }

    /**
     * The possible warnings for invalid {@link Handle}s.
     */
    private enum HandleWarning {
        CONTAINS_HANDLE("Found class which does not extend InputHandler with @Handle annotation"),
        MORE_THAN_ONE_HANDLE("Found class which contains more than one method annotated with @Handle"),
        MISSING_TRIGGER("Primary handle class found to be missing triggers"),
        FINAL_HANDLER_HAS_TRIGGERS("Final handle class found to contain triggers"),
        HANDLER_NOT_USED("Handle class not contained in primary or final handlers"),
        PRIMARY_AND_FINAL("Handle class found to be contained in both primary and final lists"),
        NOT_PUBLIC_STATIC_BOOLEAN("Method annotated with @Handle found to not be public static boolean"),
        EMPTY_TRIGGER("Handle annotation found to contain empty triggers"),
        DUPLICATE_TRIGGER("Found duplicate trigger, trigger");

        /**
         * The log prefix for this handle warning.
         */
        private final String logPrefix;

        HandleWarning(String logPrefix) {
            this.logPrefix = logPrefix;
        }

        /**
         * Returns the log prefix for this handle warning.
         *
         * @return the log prefix for this handle warning
         */
        public String getLogPrefix() {
            return this.logPrefix;
        }
    }
}
