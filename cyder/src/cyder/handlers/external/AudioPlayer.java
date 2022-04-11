package cyder.handlers.external;

import com.google.common.base.Preconditions;
import cyder.annotations.Widget;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderIconButton;
import cyder.ui.CyderProgressBar;
import cyder.utilities.OSUtil;
import cyder.utilities.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

// todo audio player menu options: export as wav,
//  export as mp3, export waveform, download audio (can
//  search for music on youtube and refresh with top 10 queries,
//  click one and confirm to download, this implies there will be levels of pages to the mp3
//  player and I kind of want them to slide in and out like StraightShot)

// todo youtube-dl should be downloaded with Cyder too and always pointed to locally

// todo dreamify checkbox for audio player, will need to generate wav first time in tmp and play from that
//  after conversion finished, should be seamless audio transition

// todo before starting audio player we need to ensure ffmpeg stack is downloaded.

// todo static icons declared class level public final

// todo still need to prevent spamming of skip actions

// todo play next and play last methods

// todo should select dir be a menu option? menu should be vertical shouuldn't it?

/**
 * An audio player widget which can also download YouTube video audio and thumbnails.
 */
public class AudioPlayer {
    public static final Color backgroundColor = new Color(8,23,52);
    public static final JLabel albumArtLabel = new JLabel();

    private static final ImageIcon playIcon = new ImageIcon(
            "static/pictures/music/Play.png");
    private static final ImageIcon playIconHover = new ImageIcon(
            "static/pictures/music/PlayHover.png");

    private static final ImageIcon pauseIcon = new ImageIcon(
            "static/pictures/music/Pause.png");
    private static final ImageIcon pauseIconHover = new ImageIcon(
            "static/pictures/music/PauseHover.png");

    private static final ImageIcon nextIcon = new ImageIcon(
            "static/pictures/music/Skip.png");
    private static final ImageIcon nextIconHover = new ImageIcon(
            "static/pictures/music/SkipHover.png");

    private static final ImageIcon lastIcon = new ImageIcon(
            "static/pictures/music/SkipBack.png");
    private static final ImageIcon lastIconHover = new ImageIcon(
            "static/pictures/music/SkipBackHover.png");

    private static final ImageIcon repeatIcon = new ImageIcon(
            "static/pictures/music/Repeat.png");
    private static final ImageIcon repeatIconHover = new ImageIcon(
            "static/pictures/music/RepeatHover.png");

    private static final ImageIcon shuffleIcon = new ImageIcon(
            "static/pictures/music/Shuffle.png");
    private static final ImageIcon shuffleIconHover = new ImageIcon(
            "static/pictures/music/ShuffleHover.png");

    private static final CyderIconButton playPauseButton =
            new CyderIconButton("Play", playIcon, playIconHover,
            new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            handlePlayPauseButtonClick();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            playPauseButton.setIcon(audioPlaying() ? pauseIconHover : playIconHover);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            playPauseButton.setIcon(audioPlaying() ? pauseIcon : playIcon);
        }
    });

    private static final CyderIconButton lastAudioButton =
            new CyderIconButton("Last", lastIcon, lastIconHover,
            new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            handleLastAudioButtonClick();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            lastAudioButton.setIcon(lastIconHover);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            lastAudioButton.setIcon(lastIcon);
        }
    });

    private static final CyderIconButton nextAudioButton =
            new CyderIconButton("Next", nextIcon, nextIconHover,
            new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            handleNextAudioButtonClick();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            nextAudioButton.setIcon(nextIconHover);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            nextAudioButton.setIcon(nextIcon);
        }
    });

    private static final CyderIconButton repeatAudioButton =
            new CyderIconButton("Repeat", repeatIcon, repeatIconHover,
            new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            handleRepeatButtonClick();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            repeatAudioButton.setIcon(repeatIconHover);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            repeatAudioButton.setIcon(repeatIcon);
        }
    });

    private static final CyderIconButton shuffleAudioButton =
            new CyderIconButton("Shuffle", shuffleIcon, shuffleIconHover,
            new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            handleShuffleButtonClick();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            shuffleAudioButton.setIcon(shuffleIconHover);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            shuffleAudioButton.setIcon(shuffleIcon);
        }
    });

    private static final ArrayList<File> audioFileQueue = new ArrayList<>();
    private static File currentAudioFile;
    private static final File DEFAULT_AUDIO_FILE = OSUtil.buildFile(
            "static","audio","Kendrick Lamar - All The Stars.mp3");

    /**
     * Suppress default constructor.
     */
    private AudioPlayer() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    private static final void refreshAudioFiles() {
        Preconditions.checkNotNull(currentAudioFile);
    }

    @Widget(triggers = {"mp3", "music", "audio"}, description = "An audio playing widget")
    public static void showGUI() {
        showGUI(DEFAULT_AUDIO_FILE);
    }

    public static void showGUI(File startPlaying) {
        Preconditions.checkNotNull(startPlaying);
        Preconditions.checkArgument(startPlaying.exists());

        currentAudioFile = startPlaying;

        // mainly frame setup, then call setup to set bounds and visibilities for the opening type
    }

    public static boolean audioPlaying() {
        //return player != null && !player.isComplete();
        return false;
    }


    public static boolean windowOpen() {
        //return audioFrame != null;
        // impliesa need to set audioframe to null when widget is disposed.
        return false;
    }

    public static void refreshAudioLine() {
//        try {
//            if (AudioSystem.isLineSupported(Port.Info.SPEAKER)) {
//                Port outline = (Port) AudioSystem.getLine(Port.Info.SPEAKER);
//                outline.open();
//                FloatControl volumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);
//                volumeControl.setValue((float) (audioVolumeSlider.getValue() * 0.001));
//            }
//
//            if (AudioSystem.isLineSupported(Port.Info.HEADPHONE)) {
//                Port outline = (Port) AudioSystem.getLine(Port.Info.HEADPHONE);
//                outline.open();
//                FloatControl volumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);
//                volumeControl.setValue((float) (audioVolumeSlider.getValue() * 0.001));
//            }
//        } catch (Exception ex) {
//            ExceptionHandler.handle(ex);
//        }
    }

    public static void handlePlayPauseButtonClick() {

    }

    public static void handleStopAudioButtonClick() {

    }

    public static void stopAudio() {

    }

    public static void handleLastAudioButtonClick() {

    }

    public static void handleNextAudioButtonClick() {

    }

    public static void handleRepeatButtonClick() {

    }

    public static void handleShuffleButtonClick() {

    }

    public static void playAudioNext(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());

        if (!windowOpen()) {
            showGUI(audioFile);
        } else {
            audioFileQueue.add(0, audioFile);
        }
    }

    public static void playAudioLast(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());

        if (!windowOpen()) {
            showGUI(audioFile);
        } else {
            audioFileQueue.add(audioFile);
        }
    }

    /**
     * The formatter used for the audio location label text.
     */
    private static final DecimalFormat locationLabelFormat = new DecimalFormat("##.#");

    /**
     * The delay between update cycles for the audio lcoation text.
     */
    private static final int audioLocationTextUpdateDelay = 250;


    // used to ensure deleting audio isn't playing currently
    public static File getCurrentAudio() {
        return null;
    }

    /**
     * The class to update the audio location label which is layered over the progress bar.
     */
    private static class AudioLocationLabelUpdater {
        private boolean killed;

        /**
         * Constructs a new audio location label to update for the provided progress bar.
         *
         * @param effectBar the CyderProgressBar to place a label on and update
         */
        public AudioLocationLabelUpdater(CyderProgressBar effectBar) {
            try {
                CyderThreadRunner.submit( () -> {
                    while (!killed) {
                        try {
                            // todo
                            Thread.sleep(audioLocationTextUpdateDelay);
                        } catch (Exception ignored) {}
                    }
                },"SONG NAME HERE Progress Label Thread");
            } catch (Exception e) {
                ExceptionHandler.silentHandle(e);
            }
        }

        /**
         * Ends the updation of the label text.
         */
        public void kill() {
            killed = true;
        }
    }

    // ---------------------------
    // Scrolling Title Label class
    // ---------------------------

    /**
     * Private inner class for the scrolling audio label.
     */
    private static class ScrollingTitleLabel {
        boolean scroll;

        ScrollingTitleLabel(JLabel effectLabel, String localTitle) {
            scroll = true;

            try {
                effectLabel.setText(localTitle);

                int parentX = effectLabel.getParent().getX();
                int parentY = effectLabel.getParent().getY();

                int parentWidth = effectLabel.getParent().getWidth();
                int parentHeight = effectLabel.getParent().getHeight();

                int minWidth = StringUtil.getMinWidth(localTitle, effectLabel.getFont());
                effectLabel.setSize(minWidth, parentHeight);

                if (minWidth - 12 > parentWidth) {
                    effectLabel.setLocation(0,0);

                    scroll = true;
                    int miliTimeout = 24;
                    int milipause = 5000;
                    int initialMiliPause = 3000;

                    CyderThreadRunner.submit(() -> {
                        try {
                            sleepWithChecks(initialMiliPause);

                            while (scroll) {
                                int goBack = 0;

                                while (goBack < minWidth - parentWidth) {
                                    if (!scroll) {
                                        break;
                                    }

                                    effectLabel.setLocation(effectLabel.getX() - 1, effectLabel.getY());
                                    Thread.sleep(miliTimeout);
                                    goBack++;
                                }

                                sleepWithChecks(milipause);

                                while (goBack > 0) {
                                    if (!scroll) {
                                        break;
                                    }

                                    effectLabel.setLocation(effectLabel.getX() + 1, effectLabel.getY());
                                    Thread.sleep(miliTimeout);
                                    goBack--;
                                }

                                sleepWithChecks(milipause);
                            }
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }
                    },"AUDIO TITLE HERE");
                } else {
                   // this would set the label to the full title and center it in the parent container
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }

        /**
         * The timeout to sleep for before checking for title scroll label being terminated.
         */
        private static final int SLEEP_WITH_CHECKS_TIMEOUT = 50;

        /**
         * Sleeps for the designated amount of time, breaking every TIMEOUT ms to check for a stop call.
         *
         * @param sleepTime the total length to sleep for
         */
        public void sleepWithChecks(long sleepTime) {
            try {
                long acc = 0;

                while (acc < sleepTime) {
                    Thread.sleep(SLEEP_WITH_CHECKS_TIMEOUT);
                    acc += SLEEP_WITH_CHECKS_TIMEOUT;

                    if (!scroll) {
                        break;
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }

        public void kill() {
            scroll = false;
        }
    }
}