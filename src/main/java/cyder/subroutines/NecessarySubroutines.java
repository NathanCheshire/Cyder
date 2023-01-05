package cyder.subroutines;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.ClassPath;
import cyder.annotations.*;
import cyder.enums.CyderInspection;
import cyder.enums.Dynamic;
import cyder.enums.ExitCondition;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.genesis.CyderSplash;
import cyder.handlers.input.InputHandler;
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
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import static cyder.strings.CyderStrings.*;

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
     * The list of triggers for GuiTest methods.
     */
    private static final LinkedList<String> guiTestTriggers = new LinkedList<>();

    /**
     * The font directory name to load the true-type fonts from.
     */
    private static final String fonts = "fonts";

    /**
     * A list of the discovered triggers from handle annotations.
     */
    private static final LinkedList<String> handleTriggers = new LinkedList<>();

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
                throw new FatalException(subroutine.getOnFailureMessage());
            }
        }

        CyderSplash.INSTANCE.setLoadingMessage("Ensuring singular instance");
        if (!InstanceSocketUtil.instanceSocketPortAvailable()) {
            ExceptionHandler.exceptionExit("Multiple instances of Cyder not allowed",
                    ExitCondition.MultipleInstancesExit, "Instances");
            throw new FatalException("Multiple instances of Cyder are not allowed");
        }

        InstanceSocketUtil.startListening();
    }

    /**
     * The list of necessary subroutines which must complete successfully before builds of Cyder are released.
     */
    public static final ImmutableList<Subroutine> subroutines = ImmutableList.of(
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
                    "Validating Gui Tests", "Validation of GuiTests failed"),

            new Subroutine(NecessarySubroutines::validateWidgets,
                    "Validating Widgets", "Validation of Widgets failed"),

            new Subroutine(() -> !OsUtil.OperatingSystem.OSX.isCurrentOperatingSystem(),
                    "Ensuring Supported OS", "Unsupported OS"),

            new Subroutine(NecessarySubroutines::validateVanillaAnnotations,
                    "Validating vanilla classes", "Validation of vanilla classes failed"),

            new Subroutine(NecessarySubroutines::validateHandles,
                    "Validating handles", "Validation of handles failed"),

            new Subroutine(Dynamic::ensureDynamicsCreated,
                    "Creating dynamics", "Creation of dynamics failed"),

            new Subroutine(() -> {
                UserUtil.validateUsers();
                return true;
            }, "Validating users", "Validation of users failed"),

            new Subroutine(() -> {
                UserUtil.cleanUsers();
                return true;
            }, "Cleaning users", "Cleaning users failed")
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
                if (!method.isAnnotationPresent(GuiTest.class)) continue;
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

            Logger.log(LogTag.GUI_TEST_WARNING,
                    "Method annotated with @GuiTest does not end"
                            + space
                            + "with"
                            + space
                            + CyderStrings.quote
                            + "test"
                            + CyderStrings.quote
                            + "; name"
                            + colon
                            + space
                            + method.getName());
        }

        if (StringUtil.in(trigger, true, guiTestTriggers)) {
            Logger.log(LogTag.GUI_TEST_WARNING,
                    "Method annotation with @GuiTest"
                            + space
                            + "has a trigger which has already been used; method"
                            + colon
                            + space
                            + method.getName()
                            + comma
                            + space
                            + "trigger"
                            + colon
                            + space
                            + trigger);
            return false;
        }

        guiTestTriggers.add(trigger);
        return true;
    }

    /**
     * Finds all widgets within Cyder by looking for methods annotated with {@link Widget}.
     * All annotated methods are validated.
     *
     * @return whether all Cyder widget methods are valid
     */
    private static boolean validateWidgets() {
        boolean ret = true;

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
            Class<?> clazz = classInfo.load();

            for (Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(Widget.class)) {
                    if (!validateWidget(method)) {
                        ret = false;
                    }
                }
            }
        }

        return ret;
    }

    /**
     * Validates the provided widget method annotated with {@link Widget}.
     * A widget method is valid if the following conditions are met:
     * <ul>
     *     <li>The method name equals showGui</li>
     *     <li>The description is not empty</li>
     *     <li>The widget is not missing triggers</li>
     *     <li>The widget has non-empty triggers</li>
     *     <li></li>
     * </ul>
     * <p>
     * All of the above checks may be skipped if the the method contains a
     * {@link CyderInspection#WidgetInspection} suppression.
     *
     * @param method the method to validate
     * @return whether the widget is valid
     */
    private static boolean validateWidget(Method method) {
        Preconditions.checkNotNull(method);
        String[] triggers = method.getAnnotation(Widget.class).triggers();
        String description = method.getAnnotation(Widget.class).description();

        if (method.isAnnotationPresent(SuppressCyderInspections.class)
                && ArrayUtil.toList(method.getAnnotation(SuppressCyderInspections.class).value())
                .contains(CyderInspection.WidgetInspection)) {
            return true;
        }

        if (!method.getName().equals(STANDARD_WIDGET_SHOW_METHOD_NAME)) {
            Logger.log(LogTag.WIDGET_WARNING, "Method annotated with @Widget is not named "
                    + STANDARD_WIDGET_SHOW_METHOD_NAME + "(); name: " + method.getName());
            return false;
        }

        if (StringUtil.isNullOrEmpty(description)) {
            Logger.log(LogTag.WIDGET_WARNING, "Method annotated with @Widget has empty description");
            return false;
        }

        if (triggers.length == 0) {
            Logger.log(LogTag.WIDGET_WARNING, "Method annotated with @Widget has no triggers");
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
     *
     * @return whether all vanilla annotations are valid
     */
    private static boolean validateVanillaAnnotations() {
        boolean ret = true;

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
            Class<?> clazz = classInfo.load();

            if (clazz.isAnnotationPresent(Vanilla.class)) {
                if (!validateVanillaAnnotation(clazz)) {
                    ret = false;
                }
            }
        }

        return ret;
    }

    /**
     * Validates the provided vanilla class.
     * A vanilla class is valid if the following are true
     * <ul>
     *     <li>The CyderAuthor annotation is present</li>
     *     <li>The CyderAuthor annotation credits Nate Cheshire as the author</li>
     * </ul>
     *
     * @param clazz the vanilla class to validate
     * @return whether the vanilla class is valid
     */
    private static boolean validateVanillaAnnotation(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkArgument(clazz.isAnnotationPresent(Vanilla.class));

        if (clazz.isAnnotationPresent(SuppressCyderInspections.class)
                && ArrayUtil.toList(clazz.getAnnotation(
                SuppressCyderInspections.class).value()).contains(CyderInspection.VanillaInspection)) {
            return true;
        }

        if (!clazz.isAnnotationPresent(CyderAuthor.class)) {
            Logger.log(LogTag.VANILLA_WARNING, "Method annotated with @Vanilla"
                    + " does not contain a @CyderAuthor annotation");
            return false;
        }

        String author = clazz.getAnnotation(CyderAuthor.class).author();

        if (!StringUtil.in(author, true, DEVELOPER_NAMES)) {
            Logger.log(LogTag.VANILLA_WARNING, "Method annotated with @Vanilla"
                    + " does not contain Nathan Cheshire as an author");
            return false;
        }

        return true;
    }

    /**
     * Validates all {@link Handle}s found throughout Cyder.
     *
     * @return whether all handles found are valid
     */
    public static boolean validateHandles() {
        boolean ret = true;

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
            Class<?> clazz = classInfo.load();
            if (!validateHandle(clazz)) {
                ret = false;
            }
        }

        return ret;
    }

    /**
     * Returns whether the provided class contains valid triggers
     * if any methods are annotated with {@link Handle}.
     * A class is valid if the following conditions are met:
     * <ul>
     *     <li>One method at most exists with the handle annotation</li>
     *     <li>The class contains one handle annotation if and only if the class extends {@link InputHandler}</li>
     *     <li>The handle has at least one trigger if the method is a primary handler</li>
     *     <li>The handle has no triggers if the method is a final handler</li>
     *     <li>The method is either a primary or final handler, not both</li>
     *     <li>The method is modified with public and static and returns a boolean</li>
     *     <li>Each trigger of each method is universally unique and not empty</li>
     * </ul>
     *
     * @param clazz the class to validate
     * @return whether the provided class is valid
     */
    @SuppressWarnings("ConstantConditions") /* Might not always be true */
    private static boolean validateHandle(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        ImmutableList<Method> handleMethods = ReflectionUtil.getHandleMethods(clazz);

        if (!ReflectionUtil.extendsInputHandler(clazz)) {
            if (handleMethods.size() > 0) {
                logHandleWarning(HandleWarning.CONTAINS_HANDLE, ReflectionUtil.getBottomLevelClass(clazz));
                return false;
            }
        }

        if (ReflectionUtil.clazzContainsMoreThanOneHandle(clazz)) {
            logHandleWarning(HandleWarning.MORE_THAN_ONE_HANDLE, ReflectionUtil.getBottomLevelClass(clazz));
            return false;
        }

        if (handleMethods.size() == 0) {
            return true;
        }

        Method handleMethod = handleMethods.get(0);
        if (handleMethod.isAnnotationPresent(SuppressCyderInspections.class)
                && ArrayUtil.toList(handleMethod.getAnnotation(
                SuppressCyderInspections.class).value()).contains(CyderInspection.HandleInspection)) {
            return true;
        }

        boolean isPrimaryHandle = ReflectionUtil.isPrimaryHandler(clazz);
        boolean isFinalHandler = ReflectionUtil.isFinalHandler(clazz);

        ImmutableList<String> triggers = ReflectionUtil.getHandleTriggers(handleMethod);

        if (isPrimaryHandle && triggers.size() < 1) {
            logHandleWarning(HandleWarning.MISSING_TRIGGER, ReflectionUtil.getBottomLevelClass(clazz));
            return false;
        } else if (isFinalHandler && triggers.size() > 0) {
            logHandleWarning(HandleWarning.FINAL_HANDLER_HAS_TRIGGERS, ReflectionUtil.getBottomLevelClass(clazz));
            return false;
        } else if (!isPrimaryHandle && !isFinalHandler) {
            logHandleWarning(HandleWarning.HANDLER_NOT_USED, ReflectionUtil.getBottomLevelClass(clazz));
            return false;
        } else if (isPrimaryHandle && isFinalHandler) {
            logHandleWarning(HandleWarning.PRIMARY_AND_FINAL, ReflectionUtil.getBottomLevelClass(clazz));
            return false;
        } else if (!ReflectionUtil.isPublicStaticBoolean(handleMethod)) {
            logHandleWarning(HandleWarning.NOT_PUBLIC_STATIC_BOOLEAN, ReflectionUtil.getBottomLevelClass(clazz));
            return false;
        }

        AtomicBoolean triggersValid = new AtomicBoolean(true);

        triggers.forEach(trigger -> {
            trigger = trigger.trim();

            if (StringUtil.isNullOrEmpty(trigger)) {
                logHandleWarning(HandleWarning.EMPTY_TRIGGER, ReflectionUtil.getBottomLevelClass(clazz));
                triggersValid.set(false);
            } else {
                if (handleTriggers.contains(trigger)) {
                    logHandleWarning(HandleWarning.DUPLICATE_TRIGGER, trigger);
                    triggersValid.set(false);
                } else {
                    handleTriggers.add(trigger);
                }
            }
        });

        return triggersValid.get();
    }

    /**
     * Logs the provided handle warning and shows a popup to the user.
     *
     * @param handleWarning     the handle warning
     * @param classOrMethodName the method or class name that caused in the handle warning
     */
    @ForReadability
    private static void logHandleWarning(HandleWarning handleWarning, String classOrMethodName) {
        String errorMessagePrefix = handleWarning.getLogPrefix() + CyderStrings.colon + space + classOrMethodName;

        Logger.log(LogTag.HANDLE_WARNING, errorMessagePrefix);
        InformHandler.inform(new InformHandler.Builder(errorMessagePrefix).setTitle(
                StringUtil.capsFirstWord(handleWarning.name().replace(CyderStrings.underscore, space))));
    }
}
