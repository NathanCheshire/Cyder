package cyder.genesis;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.enums.ExitCondition;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.subroutines.RegisterFonts;
import cyder.genesis.subroutines.SingularInstance;
import cyder.genesis.subroutines.SupportedOs;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.handlers.internal.LoginHandler;
import cyder.test.ManualTests;
import cyder.threads.CyderThreadRunner;
import cyder.utils.*;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.io.File;

/**
 * The main Cyder entry point that performs checks on data and
 * environment variables to ensure a successful start can happen.
 */
public final class Cyder {
    /**
     * Instantiation of top level program genesis not permitted.
     */
    private Cyder() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Setup and start the best program ever made :D
     *
     * @param arguments possible command line args passed in. Currently, these serve no purpose,
     *                  but we'll log them anyway (just like Big Brother would want)
     */
    public static void main(String[] arguments) {
        TimeUtil.setAbsoluteStartTime(System.currentTimeMillis());

        PropLoader.loadProps();

        addExitHook();

        Logger.initialize();

        initUiAndSystemProps();

        CyderWatchdog.initializeWatchDog();

        // todo make use reflection to find all classes which implement the interface and call them
        //  if failure and necessary, call the exit
        // Necessary subroutines
        new SingularInstance().ensure();
        new RegisterFonts().ensure();
        new SupportedOs().ensure();

        if (fastTestingCheck()) return;

        CyderSplash.INSTANCE.showSplash();

        if (completeNecessarySubroutines()) {
            spinOffSufficientSubroutines(arguments);

            LoginHandler.determineCyderEntry();
        }
    }

    /**
     * Checks for the prop <b><fast_test/b> being enabled and skips most of Cyder setup and instead invokes
     * the ManualTests method {@link ManualTests#launchTests()}.
     *
     * @return whether fast testing was found to be enabled
     */
    private static boolean fastTestingCheck() {
        if (PropLoader.getBoolean("fast_test")) {
            ManualTests.launchTests();
            ExceptionHandler.exceptionExit("Fast Testing launched; dispose this frame to exit",
                    "Fast Testing", ExitCondition.TestingModeExit);
            return true;
        }

        return false;
    }

    /**
     * Sequentially runs the subroutines whose completion is necessary prior to Cyder starting.
     *
     * @return whether all subroutines completed successfully.
     */
    // todo necessary subroutines too
    private static boolean completeNecessarySubroutines() {
        try {
            CyderSplash.INSTANCE.setLoadingMessage("Creating dynamics");
            OSUtil.ensureDynamicsCreated();

            CyderSplash.INSTANCE.setLoadingMessage("Validating users");
            UserUtil.validateUsers();

            CyderSplash.INSTANCE.setLoadingMessage("Cleaning users");
            UserUtil.cleanUsers();

            CyderSplash.INSTANCE.setLoadingMessage("Validating props");
            ReflectionUtil.validateProps();

            CyderSplash.INSTANCE.setLoadingMessage("Validating Widgets");
            ReflectionUtil.validateWidgets();

            CyderSplash.INSTANCE.setLoadingMessage("Validating Test");
            ReflectionUtil.validateTests();

            CyderSplash.INSTANCE.setLoadingMessage("Validating Vanilla");
            ReflectionUtil.validateVanillaWidgets();

            CyderSplash.INSTANCE.setLoadingMessage("Validating Handles");
            ReflectionUtil.validateHandles();

            return true;
        } catch (Exception e) {
            ExceptionHandler.exceptionExit("Exception thrown from necessary subroutine runner, message = "
                    + e.getMessage(), "Subroutine Exception", ExitCondition.SubroutineException);
        }

        return false;
    }

    /**
     * Starts a thread for subroutines who's successful completion are not necessary for Cyder use.
     *
     * @param arguments the Jvm provided arguments
     */
    private static void spinOffSufficientSubroutines(String[] arguments) {
        CyderThreadRunner.submit(() -> {
            CyderSplash.INSTANCE.setLoadingMessage("Logging JVM args");
            IOUtil.logArgs(arguments);
        }, "Secondary Subroutines Runner");
    }

    /**
     * Initializes UIManager tooltip key-value props.
     */
    private static void initUiManagerTooltipProps() {
        UIManager.put("ToolTip.background", CyderColors.tooltipBackgroundColor);
        UIManager.put("ToolTip.border", new BorderUIResource(BorderFactory.createLineBorder(
                CyderColors.tooltipBorderColor, 2, true)));
        UIManager.put("ToolTip.font", CyderFonts.TOOLTIP_FONT);
        UIManager.put("ToolTip.foreground", CyderColors.tooltipForegroundColor);
    }

    /**
     * Initializes all system and ui-manager toolkit key-value props.
     */
    private static void initUiAndSystemProps() {
        initUiManagerTooltipProps();
        initSystemProps();

        // calls which have no method
        UIManager.put("Slider.onlyLeftMouseButtonDrag", Boolean.TRUE);
    }

    /**
     * Initializes System.getProperty key/value pairs such as the ui scale.
     */
    private static void initSystemProps() {
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("sun.java2d.uiScale", PropLoader.getString("ui_scale"));
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("ide.ui.scale", PropLoader.getString("ui_scale"));
    }

    /**
     * The name to use for the exit hook thread.
     */
    public static final String EXIT_HOOK = "exit-hook";

    /**
     * Adds the exit hook to this Jvm.
     */
    private static void addExitHook() {
        Runtime.getRuntime().addShutdownHook(CyderThreadRunner.createThread(() -> {
            File deleteDirectory = OSUtil.buildFile(Dynamic.PATH, Dynamic.TEMP.getDirectoryName());
            OSUtil.deleteFile(deleteDirectory, false);
        }, EXIT_HOOK));
    }
}