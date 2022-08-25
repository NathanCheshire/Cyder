package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.console.Console;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.threads.BletchyThread;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.utils.IOUtil;
import cyder.utils.StaticUtil;
import cyder.utils.StringUtil;
import cyder.youtube.YoutubeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * A handler for commands that play audio.
 */
public class PlayAudioHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private PlayAudioHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle({"heyya", "windows", "lightsaber", "xbox", "startrek", "toystory",
            "logic", "18002738255", "xxx", "blackpanther", "chadwickboseman", "f17", "play"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().inputIgnoringSpacesMatches("heyya")) {
            IOUtil.playGeneralAudio(StaticUtil.getStaticResource("hey.mp3"));
        } else if (getInputHandler().commandIs("windows")) {
            IOUtil.playGeneralAudio(StaticUtil.getStaticResource("windows.mp3"));
        } else if (getInputHandler().commandIs("lightsaber")) {
            IOUtil.playGeneralAudio(StaticUtil.getStaticResource("lightsaber.mp3"));
        } else if (getInputHandler().commandIs("xbox")) {
            IOUtil.playGeneralAudio(StaticUtil.getStaticResource("xbox.mp3"));
        } else if (getInputHandler().commandIs("startrek")) {
            IOUtil.playGeneralAudio(StaticUtil.getStaticResource("startrek.mp3"));
        } else if (getInputHandler().commandIs("toystory")) {
            IOUtil.playGeneralAudio(StaticUtil.getStaticResource("theclaw.mp3"));
        } else if (getInputHandler().commandIs("logic")) {
            IOUtil.playGeneralAudio(StaticUtil.getStaticResource("commando.mp3"));
        } else if (getInputHandler().getCommand()
                .replace("-", "").equals("18002738255")) {
            IOUtil.playGeneralAudio(StaticUtil.getStaticResource("1800.mp3"));
        } else if (getInputHandler().commandIs("xxx")) {
            CyderIcons.setCurrentCyderIcon(CyderIcons.X_ICON);
            Console.INSTANCE.getConsoleCyderFrame()
                    .setIconImage(new ImageIcon(StaticUtil.getStaticPath("x.png")).getImage());
            IOUtil.playGeneralAudio(StaticUtil.getStaticResource("x.mp3"));
        } else if (getInputHandler().inputIgnoringSpacesMatches("blackpanther")
                || getInputHandler().inputIgnoringSpacesMatches("chadwickboseman")) {
            CyderThreadRunner.submit(() -> {
                getInputHandler().getJTextPane().setText("");

                IOUtil.playGeneralAudio(StaticUtil.getStaticResource("allthestars.mp3"));
                getInputHandler().getJTextPane().setFont(new Font("BEYNO",
                        Font.BOLD, getInputHandler().getJTextPane().getFont().getSize()));
                BletchyThread.bletchy("RIP CHADWICK BOSEMAN",
                        false, 15, false);

                ThreadUtil.sleep(4000);

                getInputHandler().getJTextPane().setFont(Console.INSTANCE.generateUserFont());
            }, "Chadwick Boseman");
        } else if (getInputHandler().commandIs("f17")) {
            if (getInputHandler().getRobot() != null) {
                getInputHandler().getRobot().keyPress(KeyEvent.VK_F17);
            } else {
                getInputHandler().println("Mr. Robot didn't start :(");
            }
        } else if (getInputHandler().commandIs("play")) {
            if (StringUtil.isNullOrEmpty(getInputHandler().argsToString())) {
                getInputHandler().println("Play command usage: Play [video_url/playlist_url/search query]");
            }

            CyderThreadRunner.submit(() -> {
                String url = getInputHandler().argsToString();

                if (YoutubeUtil.isPlaylistUrl(url)) {
                    YoutubeUtil.downloadPlaylist(url, Console.INSTANCE.getInputHandler());
                } else {
                    String extractedUuid = getInputHandler().argsToString()
                            .replace(CyderUrls.YOUTUBE_VIDEO_HEADER, "");

                    if (extractedUuid.replace(" ", "").length() != 11) {
                        getInputHandler().println("Searching youtube for: " + url);
                        String uuid = YoutubeUtil.getFirstUuid(url);
                        url = CyderUrls.YOUTUBE_VIDEO_HEADER + uuid;
                    }

                    YoutubeUtil.downloadVideo(url, Console.INSTANCE.getInputHandler());
                }
            }, "YouTube Download Initializer");
        } else {
            ret = false;
        }

        return ret;
    }
}
