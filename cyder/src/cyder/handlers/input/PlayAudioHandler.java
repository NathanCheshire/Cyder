package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.BletchyThread;
import cyder.threads.CyderThreadRunner;
import cyder.utilities.IOUtil;

import javax.swing.*;
import java.awt.*;

/**
 * A handler for commands that play audio.
 */
public class PlayAudioHandler {
    /**
     * Suppress default constructor.
     */
    private PlayAudioHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Handle("")
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("hey")) {
            IOUtil.playAudio("static/audio/hey.mp3");
        } else if (getInputHandler().commandIs("windows")) {
            IOUtil.playAudio("static/audio/windows.mp3");
        } else if (getInputHandler().commandIs("lightsaber")) {
            IOUtil.playAudio("static/audio/Lightsaber.mp3");
        } else if (getInputHandler().commandIs("xbox")) {
            IOUtil.playAudio("static/audio/xbox.mp3");
        } else if (getInputHandler().commandIs("startrek")) {
            IOUtil.playAudio("static/audio/StarTrek.mp3");
        } else if (getInputHandler().commandIs("toystory")) {
            IOUtil.playAudio("static/audio/theclaw.mp3");
        } else if (getInputHandler().commandIs("logic")) {
            IOUtil.playAudio("static/audio/commando.mp3");
        } else if (getInputHandler().getCommand()
                .replace("-", "").equals("18002738255")) {
            IOUtil.playAudio("static/audio/1800.mp3");
        } else if (getInputHandler().commandIs("xxx")) {
            CyderIcons.setCurrentCyderIcon(CyderIcons.xxxIcon);
            ConsoleFrame.INSTANCE.getConsoleCyderFrame()
                    .setIconImage(new ImageIcon("static/pictures/print/x.png").getImage());
            IOUtil.playAudio("static/audio/x.mp3");
        } else if (getInputHandler().commandAndArgsToString()
                .replaceAll("\\s+", "").equalsIgnoreCase("blackpanther")
                || getInputHandler().commandAndArgsToString()
                .replaceAll("\\s+", "").equalsIgnoreCase("chadwickboseman")) {
            CyderThreadRunner.submit(() -> {
                getInputHandler().getOutputArea().setText("");

                IOUtil.playAudio("static/audio/allthestars.mp3");
                Font oldFont = getInputHandler().getOutputArea().getFont();
                getInputHandler().getOutputArea().setFont(new Font("BEYNO", Font.BOLD, oldFont.getSize()));
                BletchyThread.bletchy("RIP CHADWICK BOSEMAN",
                        false, 15, false);

                try {
                    Thread.sleep(4000);
                } catch (Exception e) {
                    ExceptionHandler.silentHandle(e);
                }

                getInputHandler().getOutputArea().setFont(oldFont);
            }, "Chadwick Boseman");
        } else {
            ret = false;
        }

        return ret;
    }

    /**
     * Returns the ConsoleFrame's input handler.
     *
     * @return the ConsoleFrame's input handler
     */
    private static BaseInputHandler getInputHandler() {
        return ConsoleFrame.INSTANCE.getInputHandler();
    }
}
