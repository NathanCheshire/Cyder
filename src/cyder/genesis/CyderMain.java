package cyder.genesis;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.handler.ErrorHandler;
import cyder.threads.CyderThreadFactory;
import cyder.utilities.IOUtil;
import cyder.utilities.SecurityUtil;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

public class CyderMain {
    /**
     * start the best program ever made
     * @param CA - the arguments passed in
     */
    public static void main(String[] CA)  {
        Runtime.getRuntime().addShutdownHook(new Thread(CyderMain::shutdown, "exit-hook"));

        initSystemProperties();
        initUIManager();

        IOUtil.cleanUsers();
        IOUtil.deleteTempDir();
        IOUtil.logArgs(CA);
        IOUtil.cleanErrors();
        IOUtil.cleanSandbox();

        startFinalFrameDisposedChecker();

        if (SecurityUtil.nathanLenovo()) {
            Entry.autoCypher();
        } else if (IOUtil.getSystemData("Released").equals("1")) {
            Entry.showEntryGUI();
        } else {
            try {
                GenesisShare.getExitingSem().acquire();
                GenesisShare.getExitingSem().release();
                GenesisShare.exit(-600);
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }
    }

    /**
     * Initializes System.getProperty key/value pairs
     */
    private static void initSystemProperties() {
        System.setProperty("sun.java2d.uiScale", IOUtil.getSystemData("UISCALE"));
    }

    /**
     * Initializes UIManager.put key/value pairs
     */
    private static void initUIManager() {
        UIManager.put("ToolTip.background", CyderColors.tooltipBackgroundColor);
        UIManager.put("ToolTip.border", new BorderUIResource(BorderFactory.createLineBorder(CyderColors.tooltipBorderColor, 2, true)));
        UIManager.put("ToolTip.font", CyderFonts.tahoma.deriveFont(22f));
        UIManager.put("ToolTip.foreground", CyderColors.tooltipForegroundColor);
        UIManager.put("Slider.onlyLeftMouseButtonDrag", Boolean.TRUE);
    }

    private static void startFinalFrameDisposedChecker() {
        Executors.newSingleThreadScheduledExecutor(
                new CyderThreadFactory("Final Frame Disposed Checker")).scheduleAtFixedRate(() -> {
            Frame[] frames = Frame.getFrames();
            int validFrames = 0;

            for (Frame f : frames) {
                if (f.isShowing()) {
                    validFrames++;
                }
            }

            if (validFrames < 1) {
                GenesisShare.exit(120);
            }
        }, 10, 5, SECONDS);
    }

    /**
     * This is called from the shutdown hook, things imperitive to do
     * no matter what, before we close, System.exit has already been called here
     * so you shouldn't do any reading or writing to files or anything with locks/semaphores
     */
    private static void shutdown() {
        //delete temp dir
        IOUtil.deleteTempDir();
    }
}