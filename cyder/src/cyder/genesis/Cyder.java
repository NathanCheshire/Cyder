package cyder.genesis;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderNums;
import cyder.constants.CyderStrings;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.handlers.internal.LoginHandler;
import cyder.utilities.IOUtil;
import cyder.utilities.OSUtil;
import cyder.utilities.StringUtil;
import test.java.Debug;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;
import java.io.File;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;

import static cyder.genesis.CyderSplash.setLoadingMessage;

/**
 * The Cyderbase that performs checks on data and environment variables to ensure
 * a successful start can happen.
 */
public class Cyder {
    /**
     * Instantiation of the Cyder class is not allowed.
     */
    private Cyder() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * Setup and start the best program ever made :D
     * @param CA possible command line args passed in. They serve no purpose yet
     *           but we shall log them regardless (just like Big Brother would want)
     */
    public static void main(String[] CA)  {
        //set start time, this should be the first call always
        CyderCommon.setAbsoluteStartTime(System.currentTimeMillis());

        //set shutdown hooks
        addExitHook();

        //start session logger
        Logger.initialize();
        Logger.log(Logger.Tag.ENTRY, OSUtil.getSystemUsername());

        //subroutines
        initSystemKeys();
        initUIKeys();

        //prevent multiple instances, fatal subroutine if failure
        if (!ensureCyderSingleInstance()) {
            Logger.log(Logger.Tag.EXCEPTION, "ATTEMPTED MULTIPLE CYDER INSTANCES");
            ExceptionHandler.exceptionExit("Multiple instances of Cyder are not allowed. " +
                    "Terminate other instances before launching a new one.", "Instance Exception", -450);
            return;
        }

        //check for fast testing
        if (IOUtil.getSystemData().isFasttestingmode()) {
            Debug.launchTests();
            ExceptionHandler.exceptionExit("Fast Testing Loaded; dispose this frame to exit","Fast Testing", 50);
            return;
        }

        //make sure all fonts are loaded, fatal subroutine if failure
        if (!registerFonts()) {
            Logger.log(Logger.Tag.EXCEPTION, "SYSTEM FAILURE");
            ExceptionHandler.exceptionExit("Font required by system could not be loaded","Font failure", 278);
            return;
        }

        //launch splash screen since we will most likely be launching Cyder
        CyderSplash.showSplash();

        setLoadingMessage("Checkinging for exit collisions");
        if (IOUtil.checkForExitCollisions()) {
            Logger.log(Logger.Tag.EXCEPTION, "DUPLICATE EXIT CODES");
            ExceptionHandler.exceptionExit("You messed up exit codes :/","Exit Codes Exception", 278);
            return;
        }

        if (OSUtil.isOSX()) {
            Logger.log(Logger.Tag.EXCEPTION, "IMPROPER OS");
            ExceptionHandler.exceptionExit("System OS not intended for Cyder use. You should" +
                    " install a dual boot or a VM or something.","OS Exception", 278);
            return;
        }

        //IOUtil necessary subroutines to complete with success before continuing
        setLoadingMessage("Checking system data");
        IOUtil.checkSystemData();
        setLoadingMessage("Fixing users");
        IOUtil.fixUsers();
        setLoadingMessage("Cleaning users");
        IOUtil.cleanUsers();

        //IOUtil secondary subroutines that can be executed when program has started essentially
        new Thread(() -> {
            setLoadingMessage("Logging JVM args");
            IOUtil.logArgs(CA);

            IOUtil.cleanSandbox();
            IOUtil.deleteTempDir();
        },"Cyder Start Secondary Subroutines").start();

        //start GUI exiting failsafe
        CyderCommon.startFinalFrameDisposedChecker();

        //offship how to login to the LoginHandler since all subroutines finished
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
     * Initializes System.getProperty key/value pairs such as the ui scale
     */
    private static void initSystemKeys() {
        System.setProperty("sun.java2d.uiScale", String.valueOf(IOUtil.getSystemData().getUiscale()));
    }

    /**
     * Adds the exiting hook to the JVM.
     */
    private static void addExitHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            //noinspection Convert2MethodRef
            IOUtil.deleteTempDir();
        }, "common-exit-hook"));
    }

    /**
     * Registers the fonts within the fonts/ directory. These fonts are then
     * serialized into objects inside of consts.CyderFonts. These fonts may ONLY
     * be derived throughout the program. No other fonts may be used aside from the user selected font which
     * is guaranteed to work since we pull the list of fonts from the GraphicsEnvironment.
     *
     * @return whether or not all the fonts were loaded properly
     */
    public static boolean registerFonts() {
        boolean ret = true;

        File[] fontsDir = new File("static/fonts").listFiles();

        if (!new File("static/fonts").exists()) {
            ret = false;
        } else {
            //loop through fonts dir
            for (File f : fontsDir) {
                //if it's a valid font file
                if (StringUtil.getExtension(f).equals(".ttf")) {
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    try {
                        //register the font so we can use it throughout Cyder
                        ge.registerFont(Font.createFont(Font.TRUETYPE_FONT,f));
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
     * @return whether or not the provided instance of Cyder is the only one
     */
    public static boolean ensureCyderSingleInstance() {
        AtomicBoolean ret = new AtomicBoolean(true);

        new Thread(() -> {
            try {
                //blocking method which also throws
                new ServerSocket(CyderNums.INSTANCE_SOCKET_PORT).accept();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                ret.set(false);
            }
        }, "Singular Cyder Instance Ensurer Thread").start();

        try {
            Thread.sleep(CyderNums.singleInstanceEnsurerTimeout);
        } catch (InterruptedException e) {
            ExceptionHandler.handle(e);
        }

        return ret.get();
    }
}