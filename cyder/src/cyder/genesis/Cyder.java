package cyder.genesis;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderNumbers;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.enums.ExitCondition;
import cyder.enums.IgnoreThread;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.handlers.internal.LoginHandler;
import cyder.test.ManualTests;
import cyder.threads.CyderThreadRunner;
import cyder.utils.*;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;
import java.io.File;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;

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

    // todo simplify further, move methods to util class in this package, move strings to constants/utils class

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

        if (!isSingularInstance()) {
            Logger.log(Logger.Tag.DEBUG, "ATTEMPTED MULTIPLE CYDER INSTANCES");
            ExceptionHandler.exceptionExit("Multiple instances of Cyder are not allowed. " +
                            "Terminate other instances before launching a new one.",
                    "Instance Exception", ExitCondition.MultipleInstancesExit);
            return;
        }

        if (!registerFonts()) {
            Logger.log(Logger.Tag.EXCEPTION, "SYSTEM FAILURE");
            ExceptionHandler.exceptionExit("Font required by system could not be loaded",
                    "Font failure", ExitCondition.CorruptedSystemFiles);
            return;
        }

        if (!isSupportedOperatingSystem()) {
            return;
        }

        if (fastTestingCheck()) {
            return;
        }

        CyderSplash.INSTANCE.showSplash();

        if (completeNecessarySubroutines()) {
            spinOffSufficientSubroutines(arguments);

            LoginHandler.determineCyderEntry();
        }
    }

    /**
     * Checks for the proper operating system.
     *
     * @return whether Cyder is currently running on a valid and supported operating system
     */
    private static boolean isSupportedOperatingSystem() {
        if (OSUtil.isOSX()) {
            Logger.log(Logger.Tag.EXCEPTION, "IMPROPER OS");
            ExceptionHandler.exceptionExit("System OS not intended for Cyder use. You should" +
                            " install a dual boot or a VM or something :/", "OS Exception",
                    ExitCondition.CorruptedSystemFiles);
            return true;
        }

        return false;
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

    /**
     * Registers the fonts within the fonts/ directory. These fonts are then
     * serialized into objects inside constants/CyderFonts. These fonts may ONLY
     * be derived throughout the program. No other fonts may be used aside from the user selected font which
     * is guaranteed to work since we pull the list of fonts from the GraphicsEnvironment.
     *
     * @return whether all the fonts were loaded properly
     */
    private static boolean registerFonts() {
        File[] fontFiles = OSUtil.buildFile("static", "fonts").listFiles();

        if (fontFiles == null || fontFiles.length == 0) {
            return false;
        }

        for (File fontFile : fontFiles) {
            if (FileUtil.isSupportedFontExtension(fontFile)) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                try {
                    if (!ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile))) {
                        return false;
                    }

                    Logger.log(Logger.Tag.FONT_LOADED, FileUtil.getFilename(fontFile));
                } catch (Exception e) {
                    ExceptionHandler.silentHandle(e);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Determines if the provided instance of Cyder is the only one.
     *
     * @return whether the provided instance of Cyder is the only one
     */
    private static boolean isSingularInstance() {
        AtomicBoolean ret = new AtomicBoolean(true);

        CyderThreadRunner.submit(() -> {
            try {
                //blocking method which also throws

                new ServerSocket(CyderNumbers.INSTANCE_SOCKET_PORT).accept();
            } catch (Exception e) {
                ret.set(false);
            }
        }, IgnoreThread.SingularInstanceEnsurer.getName());

        try {
            // started blocking method in above thread but need to wait
            // for it to either bind or fail
            Thread.sleep(CyderNumbers.singleInstanceEnsurerTimeout);
        } catch (InterruptedException e) {
            ExceptionHandler.handle(e);
        }

        return ret.get();
    }
}