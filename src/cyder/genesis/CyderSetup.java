package cyder.genesis;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.handler.ErrorHandler;
import cyder.handler.SessionLogger;
import cyder.ui.CyderFrame;
import cyder.utilities.IOUtil;
import cyder.utilities.StringUtil;
import cyder.widgets.GenericInformer;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;
import java.io.File;

public class CyderSetup {
    /**
     * This is called from the shutdown hook, things imperitive to do
     * no matter what, before we close, System.exit has already been called here
     * so you shouldn't do any reading or writing to files or anything with locks/semaphores
     */
    public static void commonExitHook() {
        //delete temp dir
        IOUtil.deleteTempDir();
    }

    public static void addShutdownHook(Runnable hook, String name) {
        Runtime.getRuntime().addShutdownHook(new Thread(hook, name));
    }

    public static void addCommonExitHook() {
        addShutdownHook(CyderSetup::commonExitHook, "common-exit-hook");
    }

    public static void initFrameChecker() {
        GenesisShare.suspendFrameChecker();
        GenesisShare.startFinalFrameDisposedChecker();
    }

    /**
     * Initializes System.getProperty key/value pairs
     */
    public static void initSystemProperties() {
        System.setProperty("sun.java2d.uiScale", String.valueOf(IOUtil.getSystemData().getUiscale()));
    }

    /**
     * Initializes UIManager.put key/value pairs. Call this method before
     * loading a frame if bypassing Cyder.java's main method.
     */
    public static void initUIManager() {
        UIManager.put("ToolTip.background", CyderColors.tooltipBackgroundColor);
        UIManager.put("ToolTip.border", new BorderUIResource(BorderFactory.createLineBorder(CyderColors.tooltipBorderColor, 2, true)));
        UIManager.put("ToolTip.font", CyderFonts.tahoma.deriveFont(22f));
        UIManager.put("ToolTip.foreground", CyderColors.tooltipForegroundColor);
        UIManager.put("Slider.onlyLeftMouseButtonDrag", Boolean.TRUE);
    }

    /**
     * Registers the fonts within the fonts/ directory. These fonts are then serialized into objects inside of consts.CyderFonts.
     * These fonts may ONLY be derived throughout the program. No other fonts may be used aside from the user selected font which
     * is guaranteed to work since we pull the list of fonts from the GraphicsEnvironment.
     */
    public static void registerFonts() {
        //loop through fonts dir
        for (File f : new File("fonts").listFiles()) {
            //if it's a valid font file
            if (StringUtil.getExtension(f).equals(".ttf")) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                try {
                    //register the font so we can use it throughout Cyder
                    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT,f));
                } catch (Exception e) {
                    ErrorHandler.silentHandle(e);
                    //todo maybe we should exit if we can't load a font
                }
            }
        }
    }

    public static void commonCyderSetup() {
        CyderSetup.addCommonExitHook();

        CyderSetup.initSystemProperties();
        CyderSetup.initUIManager();

        CyderSetup.initFrameChecker();
    }

    public static void osxExit() {
        SessionLogger.log(SessionLogger.Tag.LOGIN, "IMPROPER OS");
        GenesisShare.cancelFrameCheckerSuspention();

        CyderFrame retFrame = GenericInformer.informRet("System OS not intended for Cyder use. You should" +
                " install a dual boot or a VM or something.","OS Exception");
        retFrame.addPreCloseAction(() -> GenesisShare.exit(178));
        retFrame.setVisible(true);
        retFrame.setLocationRelativeTo(null);
    }
}
