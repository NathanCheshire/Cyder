package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.audio.GeneralAndSystemAudioPlayer;
import cyder.console.Console;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.BletchyThread;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.utils.StaticUtil;
import cyder.youtube.YoutubeUtil;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * A handler for commands that play audio.
 */
public class PlayAudioHandler extends InputHandler {
    /**
     * The all the stars music file.
     */
    private static final File allTheStars = StaticUtil.getStaticResource("allthestars.mp3");

    /**
     * The Beyno font.
     */
    private static final Font beynoFont = new CyderFonts.FontBuilder("BEYNO")
            .setSize(getInputHandler().getJTextPane().getFont().getSize()).generate();

    /**
     * The chadwick boseman bletchy text.
     */
    private static final String chadwickBosemanBletchyText = "RIP CHADWICK BOSEMAN";

    /**
     * The delay between starting the chadwick boseman easter egg audio/bletchy thread,
     * and resetting to the user's font.
     */
    private static final int chadwickBosemanResetFontDelay = 4000;

    /**
     * Suppress default constructor.
     */
    private PlayAudioHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle({"heyya",
            "windows",
            "lightsaber",
            "xbox",
            "startrek",
            "toystory",
            "logic",
            "18002738255",
            "x",
            "blackpanther",
            "chadwickboseman",
            "f17",
            "play"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().inputIgnoringSpacesMatches("heyya")) {
            GeneralAndSystemAudioPlayer.playGeneralAudio(StaticUtil.getStaticResource("hey.mp3"));
        } else if (getInputHandler().commandIs("windows")) {
            GeneralAndSystemAudioPlayer.playGeneralAudio(StaticUtil.getStaticResource("windows.mp3"));
        } else if (getInputHandler().commandIs("lightsaber")) {
            GeneralAndSystemAudioPlayer.playGeneralAudio(StaticUtil.getStaticResource("lightsaber.mp3"));
        } else if (getInputHandler().commandIs("xbox")) {
            GeneralAndSystemAudioPlayer.playGeneralAudio(StaticUtil.getStaticResource("xbox.mp3"));
        } else if (getInputHandler().commandIs("startrek")) {
            GeneralAndSystemAudioPlayer.playGeneralAudio(StaticUtil.getStaticResource("startrek.mp3"));
        } else if (getInputHandler().commandIs("toystory")) {
            GeneralAndSystemAudioPlayer.playGeneralAudio(StaticUtil.getStaticResource("theclaw.mp3"));
        } else if (getInputHandler().commandIs("logic")) {
            GeneralAndSystemAudioPlayer.playGeneralAudio(StaticUtil.getStaticResource("commando.mp3"));
        } else if (getInputHandler().getCommand().replace(CyderStrings.dash, "").equals("18002738255")) {
            GeneralAndSystemAudioPlayer.playGeneralAudio(StaticUtil.getStaticResource("1800.mp3"));
        } else if (getInputHandler().commandIs(CyderStrings.X)) {
            Console.INSTANCE.getConsoleCyderFrame().setIconImage(CyderIcons.X_ICON.getImage());
            GeneralAndSystemAudioPlayer.playGeneralAudioWithCompletionCallback(
                    StaticUtil.getStaticResource("x.mp3"),
                    () -> Console.INSTANCE.getConsoleCyderFrame().setIconImage(CyderIcons.CYDER_ICON.getImage()));
        } else if (getInputHandler().inputIgnoringSpacesMatches("blackpanther")
                || getInputHandler().inputIgnoringSpacesMatches("chadwickboseman")) {
            chadwickBosemanEasterEgg();
        } else if (getInputHandler().commandIs("f17")) {
            if (getInputHandler().getRobot() != null) {
                getInputHandler().getRobot().keyPress(KeyEvent.VK_F17);
            } else {
                getInputHandler().println("Mr. Robot didn't start :(");
            }
        } else if (getInputHandler().commandIs("play")) {
            onPlayCommand();
        } else {
            ret = false;
        }

        return ret;
    }

    /**
     * The actions to invoke when a play command is handled.
     */
    private static void onPlayCommand() {
        if (StringUtil.isNullOrEmpty(getInputHandler().argsToString())) {
            getInputHandler().println("Play command usage: Play [video_url/playlist_url/search query]");
        }

        CyderThreadRunner.submit(() -> {
            String url = getInputHandler().argsToString();

            if (YoutubeUtil.isPlaylistUrl(url)) {
                // todo
            } else if (YoutubeUtil.isVideoUrl(url)) {
                YoutubeUtil.downloadYouTubeAudio(url, Console.INSTANCE.getInputHandler());
            } else {
                getInputHandler().println("Searching youtube for: " + url);
                String uuid = YoutubeUtil.getFirstUuid(url);
                url = CyderUrls.YOUTUBE_VIDEO_HEADER + uuid;
                YoutubeUtil.downloadYouTubeAudio(url, Console.INSTANCE.getInputHandler());
            }
        }, "YouTube Download Initializer");
    }

    /**
     * Shows the Chadwick Boseman easter egg.
     */
    private static void chadwickBosemanEasterEgg() {
        String threadName = "Chadwick Boseman Easter Egg Thread";
        CyderThreadRunner.submit(() -> {
            getInputHandler().getJTextPane().setText("");

            GeneralAndSystemAudioPlayer.playGeneralAudio(allTheStars);
            getInputHandler().getJTextPane().setFont(beynoFont);
            BletchyThread.bletchy(chadwickBosemanBletchyText, false, 15, false);

            ThreadUtil.sleep(chadwickBosemanResetFontDelay);

            getInputHandler().getJTextPane().setFont(Console.INSTANCE.generateUserFont());
        }, threadName);
    }
}
