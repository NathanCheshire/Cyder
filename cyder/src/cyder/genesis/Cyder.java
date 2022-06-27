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
 * The Cyder-base that performs checks on data and environment variables to ensure
 * a successful start can happen.
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
     * @param arguments possible command line args passed in. They serve no purpose yet,
     *                  but we shall log them regardless (just like Big Brother would want)
     */
    public static void main(String[] arguments) {
        TimeUtil.setAbsoluteStartTime(System.currentTimeMillis());

        PropLoader.loadProps();

        addExitHook();
        Logger.initialize();

        initUiAndSystemProps();

        if (PropLoader.getBoolean("activate_watchdog")) {
            CyderWatchdog.initializeWatchDog();
        } else {
            Logger.log(Logger.Tag.DEBUG, "Watchdog skipped");
        }

        if (!isSingularInstance()) {
            Logger.log(Logger.Tag.DEBUG, "ATTEMPTED MULTIPLE CYDER INSTANCES");
            ExceptionHandler.exceptionExit("Multiple instances of Cyder are not allowed. " +
                            "Terminate other instances before launching a new one.", "Instance Exception",
                    ExitCondition.MultipleInstancesExit);
            return;
        }

        if (!registerFonts()) {
            Logger.log(Logger.Tag.EXCEPTION, "SYSTEM FAILURE");
            ExceptionHandler.exceptionExit("Font required by system could not be loaded", "Font failure",
                    ExitCondition.CorruptedSystemFiles);
            return;
        }

        if (OSUtil.isOSX()) {
            Logger.log(Logger.Tag.EXCEPTION, "IMPROPER OS");
            ExceptionHandler.exceptionExit("System OS not intended for Cyder use. You should" +
                            " install a dual boot or a VM or something :/", "OS Exception",
                    ExitCondition.CorruptedSystemFiles);
            return;
        }

        if (PropLoader.getBoolean("fast_test")) {
            ManualTests.launchTests();
            ExceptionHandler.exceptionExit("Fast Testing launched; dispose this frame to exit",
                    "Fast Testing", ExitCondition.TestingModeExit);
            return;
        }

        BackendUtil.setupBackend(PropLoader.getInteger("local_backend_port"));

        CyderSplash.INSTANCE.showSplash();

        // Necessary subroutines
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
        } catch (Exception e) {
            ExceptionHandler.exceptionExit("Exception thrown from subroutine runner, message = "
                    + e.getMessage(), "Subroutine Exception", ExitCondition.SubroutineException);
            return;
        }

        // Sufficient subroutines
        CyderThreadRunner.submit(() -> {
            CyderSplash.INSTANCE.setLoadingMessage("Logging JVM args");
            IOUtil.logArgs(arguments);
        }, "Secondary Subroutines Runner");

        // off-ship how to login to the LoginHandler since all subroutines finished
        LoginHandler.determineCyderEntry();
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
     * Adds the exiting hook to the JVM.
     */
    private static void addExitHook() {
        Runtime.getRuntime().addShutdownHook(CyderThreadRunner.createThread(() -> {
            // Currently, all that iss done here is delete the tmp directory.
            // Occasionally this fails due to file handles still being open
            // on files inside of tmp which is why we attempt to delete it on start.

            File deleteDirectory = OSUtil.buildFile(Dynamic.PATH,
                    Dynamic.TEMP.getDirectoryName());
            OSUtil.deleteFile(deleteDirectory, false);
        }, "common-exit-hook"));
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
        File fontsDir = OSUtil.buildFile("static", "fonts");

        if (!fontsDir.exists()) {
            return false;
        }

        File[] fontFiles = fontsDir.listFiles();

        if (fontFiles == null || fontFiles.length == 0) {
            return false;
        }

        boolean ret = true;

        for (File f : fontFiles) {
            // if it's a valid font file
            if (StringUtil.in(FileUtil.getExtension(f), true, FileUtil.SUPPORTED_FONT_EXTENSIONS)) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                try {
                    // register the font so we can use it throughout Cyder
                    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, f));
                    Logger.log(Logger.Tag.FONT_LOADED, FileUtil.getFilename(f));
                } catch (Exception e) {
                    ExceptionHandler.silentHandle(e);
                    ret = false;
                    break;
                }
            }
        }

        return ret;
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