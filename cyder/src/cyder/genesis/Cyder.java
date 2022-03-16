package cyder.genesis;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderNumbers;
import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.enums.LoggerTag;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.handlers.internal.LoginHandler;
import cyder.test.ManualTests;
import cyder.threads.CyderThreadRunner;
import cyder.utilities.*;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;
import java.io.File;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;

import static cyder.genesis.CyderSplash.setLoadingMessage;

/**
 * The Cyder-base that performs checks on data and environment variables to ensure
 * a successful start can happen.
 */
public class Cyder {
    /**
     * Instantiation of the Cyder class is not allowed.
     */
    private Cyder() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Setup and start the best program ever made :D
     * @param ca possible command line args passed in. They serve no purpose yet,
     *           but we shall log them regardless (just like Big Brother would want)
     */
    public static void main(String[] ca) {
        // set start time, this should be the first call always
        CyderShare.setAbsoluteStartTime(System.currentTimeMillis());

        //set shutdown hooks
        addExitHook();

        // start session logger
        Logger.initialize();

        // subroutines
        initSystemKeys();
        initUIKeys();

        // prevent multiple instances, fatal subroutine if failure
        if (!ensureCyderSingleInstance()) {
            Logger.log(LoggerTag.EXCEPTION, "ATTEMPTED MULTIPLE CYDER INSTANCES");
            ExceptionHandler.exceptionExit("Multiple instances of Cyder are not allowed. " +
                    "Terminate other instances before launching a new one.", "Instance Exception",
                    ExitCondition.MultipleInstancesExit);
            return;
        }

        // check for fast testing
        if (CyderShare.isFastTestingMode()) {
            ManualTests.launchTests();
            ExceptionHandler.exceptionExit("Fast Testing Loaded; dispose this frame to exit","Fast Testing",
                    ExitCondition.TestingModeExit);
            return;
        }

        // make sure all fonts are loaded, fatal subroutine if failure
        if (!registerFonts()) {
            Logger.log(LoggerTag.EXCEPTION, "SYSTEM FAILURE");
            ExceptionHandler.exceptionExit("Font required by system could not be loaded","Font failure",
                    ExitCondition.CorruptedSystemFiles);
            return;
        }

        // not permitted on Mac OS X
        if (OSUtil.isOSX()) {
            Logger.log(LoggerTag.EXCEPTION, "IMPROPER OS");
            ExceptionHandler.exceptionExit("System OS not intended for Cyder use. You should" +
                    " install a dual boot or a VM or something.","OS Exception",
                    ExitCondition.CorruptedSystemFiles);
            return;
        }

        // launch splash screen since we will most likely be launching Cyder
        CyderSplash.showSplash();

        // necessary subroutines to complete with success before continuing
        try {
            setLoadingMessage("Validating users");
            UserUtil.validateAllusers();
            setLoadingMessage("Cleaning users");
            UserUtil.cleanUsers();
            setLoadingMessage("Validating widgets");
            ReflectionUtil.validateWidgets();
            setLoadingMessage("Validating manual tests");
            ReflectionUtil.validateTests();
        } catch (Exception e) {
            ExceptionHandler.exceptionExit("Exception thrown from subroutine. "
                    + e.getMessage(), "Subroutine Exception", ExitCondition.SubroutineException);
            return;
        }


        //IOUtil secondary subroutines that can be executed when program has started essentially
        CyderThreadRunner.submit(() -> {
            setLoadingMessage("Logging JVM args");
            IOUtil.logArgs(ca);
            IOUtil.cleanSandbox();
            OSUtil.deleteTempDir();
        },"Cyder Start Secondary Subroutines");

        // Off-ship how to login to the LoginHandler since all subroutines finished
        LoginHandler.determineCyderEntry();
    }

    /**
     * Initializes UIManager.put key/value pairs.
     */
    private static void initUIKeys() {
        UIManager.put("ToolTip.background", CyderColors.tooltipBackgroundColor);
        UIManager.put("ToolTip.border", new BorderUIResource(
                BorderFactory.createLineBorder(CyderColors.tooltipBorderColor, 2, true)));
        UIManager.put("ToolTip.font", CyderFonts.javaTooltipFont);
        UIManager.put("ToolTip.foreground", CyderColors.tooltipForegroundColor);
        UIManager.put("Slider.onlyLeftMouseButtonDrag", Boolean.TRUE);
    }

    /**
     * Initializes System.getProperty key/value pairs such as the ui scale.
     */
    private static void initSystemKeys() {
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("sun.java2d.uiScale", String.valueOf(OSUtil.getUIScale()));
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("ide.ui.scale", String.valueOf(OSUtil.getUIScale()));
    }

    /**
     * Adds the exiting hook to the JVM.
     */
    private static void addExitHook() {
        Runtime.getRuntime().addShutdownHook(CyderThreadRunner.createThread(() -> {
            //noinspection Convert2MethodRef
            OSUtil.deleteTempDir();
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
        boolean ret = true;

        File fontsDir = new File("static/fonts");

        if (!fontsDir.exists())
            throw new IllegalStateException("Fonts directory does not exist");

        File[] fontFiles = new File("static/fonts").listFiles();

        if (fontFiles == null || fontFiles.length == 0)
            throw new IllegalStateException("No fonts were found to load");

        if (!new File("static/fonts").exists()) {
            ret = false;
        } else {
            // loop through fonts dir
            for (File f : fontFiles) {
                // if it's a valid font file
                if (StringUtil.in(FileUtil.getExtension(f), true, FileUtil.validFontExtensions)) {
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    try {
                        // register the font so we can use it throughout Cyder
                        ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, f));
                    } catch (Exception e) {
                        ExceptionHandler.silentHandle(e);
                        ret = false;
                        break;
                    }
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
    private static boolean ensureCyderSingleInstance() {
        AtomicBoolean ret = new AtomicBoolean(true);

        CyderThreadRunner.submit(() -> {
            try {
                //blocking method which also throws

                //noinspection resource,IOResourceOpenedButNotSafelyClosed,SocketOpenedButNotSafelyClosed
                new ServerSocket(CyderNumbers.INSTANCE_SOCKET_PORT).accept();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                ret.set(false);
            }
        }, "Singular Cyder Instance Ensurer");

        try {
            Thread.sleep(CyderNumbers.singleInstanceEnsurerTimeout);
        } catch (InterruptedException e) {
            ExceptionHandler.handle(e);
        }

        return ret.get();
    }

    // todo need precaution for gui thread freezing to restart program

    // todo need ability to shutdown everything and restart without closing program
    // startup another thread to poll gui thread and if no response lambda times
    // then popup to inform GUI thread is frozen and add kill option
}