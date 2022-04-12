package cyder.handlers.external;

import com.google.common.base.Preconditions;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.enums.DynamicDirectory;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.messaging.MessagingUtils;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.ui.enums.AnimationDirection;
import cyder.ui.enums.SliderShape;
import cyder.user.UserFile;
import cyder.utilities.*;
import cyder.utilities.objects.GetterBuilder;
import javazoom.jl.player.Player;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Port;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Future;

// todo audio player menu options: export as wav,
//  export as mp3, export waveform, download audio (can
//  search for music on youtube and refresh with top 10 queries,
//  click one and confirm to download, this implies there will be levels of pages to the mp3
//  player and I kind of want them to slide in and out like StraightShot)
//  and choose audio button for file chooser

// todo youtube-dl should be downloaded with Cyder too and always pointed to locally if command doesn't work

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
    // ------------------
    // UI element members
    // ------------------

    private static CyderFrame audioFrame;
    private static final JLabel albumArtLabel = new JLabel();
    private static final JLabel audioTitleLabel = new JLabel();
    private static final JLabel audioTitleLabelContainer = new JLabel();
    private static final CyderProgressBar audioProgressBar = new CyderProgressBar(0, 100);
    private static final CyderProgressUI audioProgressBarUi = new CyderProgressUI();
    private static final JLabel audioProgressLabel = new JLabel();

    private static final JSlider audioVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
    private static final CyderSliderUI audioVolumeSliderUi = new CyderSliderUI(audioVolumeSlider);

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
            playPauseButton.setIcon(isAudioPlaying() ? pauseIconHover : playIconHover);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            playPauseButton.setIcon(isAudioPlaying() ? pauseIcon : playIcon);
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

    private enum FrameView {
        /**
         * All ui elements visible.
         */
        FULL,
        /**
         * Album art hidden.
         */
        HIDDEN_ART,
        /**
         * Mini audio player mode.
         */
        MINI,
        /**
         * Searching YouTube for a video's audio to download.
         */
        SEARCH,
        /**
         * Confirming/downloading a YouTube video's audio.
         */
        DOWNLOAD,
    }

    private static FrameView currentFrameView;

    // ----------------------
    // Non-ui widget members
    // ----------------------

    public static final Color backgroundColor = new Color(8,23,52);

    private static final ArrayList<File> audioFileQueue = new ArrayList<>();
    private static File currentAudioFile;
    private static final ArrayList<File> validAudioFiles = new ArrayList<>();

    public static final File DEFAULT_AUDIO_FILE = OSUtil.buildFile(
            "static","audio","Kendrick Lamar - All The Stars.mp3");

    private static Player player;

    private static final int DEFAULT_FRAME_WIDTH = 600;
    private static final int DEFAULT_FRAME_HEIGHT = 600;

    public static final int ALBUM_ART_LEN = 400;

    private static final ImageIcon alternateView = new ImageIcon("static/pictures/icons/ChangeSize1");
    private static final ImageIcon alternateViewHover = new ImageIcon("static/pictures/icons/ChangeSize2");

    private static final CyderIconButton switchFrameView = new CyderIconButton(
            "Switch View", alternateView, alternateViewHover,
            new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
           switch (currentFrameView) {
               case FULL:
                   setupAndShowFrameView(FrameView.HIDDEN_ART);
                   break;
               case HIDDEN_ART:
                   setupAndShowFrameView(FrameView.MINI);
                   break;
               case MINI:
                   setupAndShowFrameView(FrameView.FULL);
                   break;
               default:
                   throw new IllegalArgumentException(
                           "Illegal requested view to switch to via view switch frame button");
           }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            switchFrameView.setIcon(alternateViewHover);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            switchFrameView.setIcon(alternateView);
        }
    });

    /**
     * Suppress default constructor.
     */
    private AudioPlayer() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Allow widget to be found via reflection.
     */
    @Widget(triggers = {"mp3", "music", "audio"}, description = "An audio playing widget")
    public static void showGUI() {
        showGUI(DEFAULT_AUDIO_FILE);
    }

    // todo eventaully if already open, start playing new
    //  audio but don't close frame or change frame state
    /**
     * Starts playing the provided audio file.
     * The file must be mp3 or wav.
     *
     * @param startPlaying the audio file to start playing.
     *                     If null, {@link AudioPlayer#DEFAULT_AUDIO_FILE} is played.
     */
    public static void showGUI(File startPlaying) {
        Preconditions.checkNotNull(startPlaying);
        Preconditions.checkArgument(startPlaying.exists());

        currentAudioFile = startPlaying;

        audioFrame = new CyderFrame(DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT, backgroundColor);
        refreshFrameTitle();

        audioFrame.getTopDragLabel().addButton(switchFrameView, 0);
        audioFrame.setCurrentMenuType(CyderFrame.MenuType.PANEL);
        audioFrame.setMenuEnabled(true);
        installMenuItems();

        // todo set size of all components ever needed on frame and add to frame
        albumArtLabel.setSize(ALBUM_ART_LEN, ALBUM_ART_LEN);
        audioFrame.getContentPane().add(albumArtLabel);

        audioTitleLabelContainer.setSize(ALBUM_ART_LEN, 40);
        audioFrame.getContentPane().add(audioTitleLabelContainer);

        // todo will be updated and centered in parent
        audioTitleLabel.setSize(ALBUM_ART_LEN, 40);
        audioTitleLabelContainer.add(audioTitleLabel);

        audioProgressBar.setSize(ALBUM_ART_LEN, 40);
        audioFrame.getContentPane().add(audioProgressBar);
        audioProgressBarUi.setAnimationDirection(AnimationDirection.LEFT_TO_RIGHT);
        audioProgressBarUi.setColors(new Color[] {CyderColors.regularPink, CyderColors.notificationForegroundColor});
        audioProgressBarUi.setShape(CyderProgressUI.Shape.SQUARE);
        audioProgressBar.setUI(audioProgressBarUi);
        audioProgressBar.setMinimum(0);
        audioProgressBar.setMaximum(10000);
        audioProgressBar.setBorder(new LineBorder(Color.black, 2));
        audioProgressBar.setOpaque(false);
        audioProgressBar.setFocusable(false);

        audioProgressLabel.setSize(ALBUM_ART_LEN, 40);
        audioFrame.getContentPane().add(audioProgressLabel);
        audioProgressLabel.setFont(CyderFonts.defaultFontSmall);
        audioProgressLabel.setForeground(CyderColors.vanila);
        audioProgressLabel.setFocusable(false);
        audioProgressLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // todo change audio location
            }
        });

        audioVolumeSliderUi.setThumbStroke(new BasicStroke(2.0f));
        audioVolumeSliderUi.setSliderShape(SliderShape.CIRCLE);
        audioVolumeSliderUi.setThumbDiameter(25);
        audioVolumeSliderUi.setFillColor(CyderColors.vanila);
        audioVolumeSliderUi.setOutlineColor(CyderColors.vanila);
        audioVolumeSliderUi.setNewValColor(CyderColors.vanila);
        audioVolumeSliderUi.setOldValColor(CyderColors.regularRed);
        audioVolumeSliderUi.setTrackStroke(new BasicStroke(2.0f));

        audioVolumeSlider.setSize(ALBUM_ART_LEN, 40);
        audioFrame.getContentPane().add(audioVolumeSlider);
        audioVolumeSlider.setUI(audioVolumeSliderUi);
        audioVolumeSlider.setMinimum(0);
        audioVolumeSlider.setMaximum(100);
        audioVolumeSlider.setPaintTicks(false);
        audioVolumeSlider.setPaintLabels(false);
        audioVolumeSlider.setVisible(true);
        audioVolumeSlider.setValue(50);
        audioVolumeSlider.addChangeListener(e -> {
            refreshAudioLine();
        });
        audioVolumeSlider.setOpaque(false);
        audioVolumeSlider.setToolTipText("Volume");
        audioVolumeSlider.setFocusable(false);
        audioVolumeSlider.repaint();
        audioFrame.getContentPane().add(audioVolumeSlider);

        setUiComponentsVisible(false);

        setupAndShowFrameView(FrameView.FULL);

        audioFrame.finalizeAndShow();
    }

    private static void installMenuItems() {
        // just to be safe
        audioFrame.clearMenuItems();
        audioFrame.addMenuItem("Export wav", () -> {
            if (FileUtil.validateExtension(currentAudioFile, ".wav")) {
                audioFrame.notify("This file is already a wav");
                return;
            } else if (FileUtil.validateExtension(currentAudioFile, ".mp3")) {
                CyderThreadRunner.submit(() -> {
                    Future<Optional<File>> wavConvertedFile = AudioUtil.mp3ToWav(currentAudioFile);

                    while (!wavConvertedFile.isDone()) {
                        Thread.onSpinWait();
                    }

                    try {
                        if (wavConvertedFile.get().isPresent()) {
                            File destination = OSUtil.buildFile(
                                    DynamicDirectory.DYNAMIC_PATH,
                                    DynamicDirectory.USERS.getDirectoryName(),
                                    UserFile.MUSIC.getName(),
                                    FileUtil.getFilename(wavConvertedFile.get().get()) + ".wav");

                            Files.copy(wavConvertedFile.get().get().toPath(), destination.toPath());
                        } else {
                            audioFrame.notify("Could not convert \""
                                    + currentAudioFile.getName() + "\" to a wav at this time");
                        }
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }, "Wav exporter");
            } else {
                throw new IllegalArgumentException("Unsupported audio format: " + currentAudioFile.getName());
            }
        });
        audioFrame.addMenuItem("Export mp3", () -> {
            if (FileUtil.validateExtension(currentAudioFile, ".mp3")) {
                audioFrame.notify("This file is already an mp3");
                return;
            } else if (FileUtil.validateExtension(currentAudioFile, ".wav")) {
                CyderThreadRunner.submit(() -> {
                    Future<Optional<File>> mp3ConvertedFile = AudioUtil.wavToMp3(currentAudioFile);

                    while (!mp3ConvertedFile.isDone()) {
                        Thread.onSpinWait();
                    }

                    try {
                        if (mp3ConvertedFile.get().isPresent()) {
                            File destination = OSUtil.buildFile(
                                    DynamicDirectory.DYNAMIC_PATH,
                                    DynamicDirectory.USERS.getDirectoryName(),
                                    UserFile.MUSIC.getName(),
                                    FileUtil.getFilename(mp3ConvertedFile.get().get()) + ".mp3");

                            Files.copy(mp3ConvertedFile.get().get().toPath(), destination.toPath());
                        } else {
                            audioFrame.notify("Could not convert \""
                                    + currentAudioFile.getName() + "\" to an mp3 at this time");
                        }
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }, "Mp3 exporter");
            } else {
                throw new IllegalArgumentException("Unsupported audio format: " + currentAudioFile.getName());
            }
        });
        audioFrame.addMenuItem("Waveform", () -> {
            CyderThreadRunner.submit(() -> {
                GetterBuilder builder = new GetterBuilder("Export name");
                builder.setLabelText("Enter a name to export the waveform as");
                builder.setSubmitButtonText("Save to files");

                String saveName = GetterUtil.getInstance().getString(builder);

                if (!StringUtil.isNull(saveName)) {
                    if (OSUtil.isValidFilename(saveName)) {
                        File saveFile = OSUtil.buildFile(
                                DynamicDirectory.DYNAMIC_PATH,
                                DynamicDirectory.USERS.getDirectoryName(),
                                UserFile.FILES.getName(),
                                saveName + ".png");

                        Future<BufferedImage> waveform = MessagingUtils.generateLargeWaveform(currentAudioFile);

                        while (!waveform.isDone()) {
                            Thread.onSpinWait();
                        }

                        try {
                            ImageIO.write(waveform.get(), "png", saveFile);
                            audioFrame.notify("Saved waveform to your files directory");
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                            audioFrame.notify("Could not save waveform at this time");
                        }
                    } else {
                        audioFrame.notify("Invalid filename for " + OSUtil.OPERATING_SYSTEM_NAME);
                    }
                }
            }, "AudioPlayer Waveform Exporter");
        });
        audioFrame.addMenuItem("Search", () -> {
            // phase 2
        });
        audioFrame.addMenuItem("Choose File", () -> {
            CyderThreadRunner.submit(() -> {
                GetterBuilder builder = new GetterBuilder("Choose an mp3 or wav file");
                builder.setRelativeTo(audioFrame);
                File chosenFile = GetterUtil.getInstance().getFile(builder);

                if (chosenFile != null && FileUtil.isSupportedAudioExtension(chosenFile)) {
                    // todo end stuff, set as current file, find siblings to play, start playing it
                } else {
                    audioFrame.notify("Invalid file chosen");
                }
            }, "AudioPlayer File Chooser");
        });
    }

    private static void setupAndShowFrameView(FrameView view) {
        setUiComponentsVisible(false);

        switch (view) {
            case FULL:
                // set location of all components needed

                setUiComponentsVisible(true);
                currentFrameView = FrameView.FULL;
                break;
            case HIDDEN_ART:
                // set location of all components needed

                // only set elements to show to visible
                currentFrameView = FrameView.HIDDEN_ART;
                break;
            case MINI:
                // set location of all components needed

                // only set elements to show to visible, (buttons)
                currentFrameView = FrameView.MINI;
                break;
            default:
                throw new IllegalArgumentException("Unsupported frame view to switch to: " + view);
        }
    }

    public static final String DEFAULT_FRAME_TITLE = "Audio Player";
    public static final int MAX_TITLE_LENGTH = 40;

    private static void refreshFrameTitle() {
        String title = DEFAULT_FRAME_TITLE;

        if (currentAudioFile != null) {
            title = StringUtil.capsFirst(StringUtil.getTrimmedText(title));

            if (title.length() > MAX_TITLE_LENGTH - 3) {
                String[] parts = title.split("\\s+");

                StringBuilder builder = new StringBuilder();

                for (String part : parts) {
                    if (builder.length() + part.length() <= MAX_TITLE_LENGTH) {
                        builder.append(part).append(" ");
                    } else {
                        builder.append("...");
                        break;
                    }
                }

                title = title.substring(0, MAX_TITLE_LENGTH - 4) + "...";
            }
        }

        audioFrame.setTitle(title);
    }

    private static final void refreshAudioFiles() {
        Preconditions.checkNotNull(currentAudioFile);

        validAudioFiles.clear();

        File parentDirectory = currentAudioFile.getParentFile();

        if (parentDirectory.exists()) {
            File[] siblings = parentDirectory.listFiles();

            if (siblings.length > 0) {
                for (File sibling : siblings) {
                    if (FileUtil.isSupportedAudioExtension(sibling)) {
                        validAudioFiles.add(sibling);
                    }
                }
            }
        }
    }

    public static boolean isAudioPlaying() {
        return player != null && !player.isComplete();
    }

    public static boolean isWidgetOpen() {
        return audioFrame != null;
    }

    public static void closeWidget() {
         if (audioFrame != null) {
             audioFrame.dispose(true);

             // todo other actions

             audioFrame = null;
         }
    }

    public static void refreshAudioLine() {
        try {
            if (AudioSystem.isLineSupported(Port.Info.SPEAKER)) {
                Port outline = (Port) AudioSystem.getLine(Port.Info.SPEAKER);
                outline.open();
                FloatControl volumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);
                volumeControl.setValue((float) (audioVolumeSlider.getValue() * 0.001));
            }

            if (AudioSystem.isLineSupported(Port.Info.HEADPHONE)) {
                Port outline = (Port) AudioSystem.getLine(Port.Info.HEADPHONE);
                outline.open();
                FloatControl volumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);
                volumeControl.setValue((float) (audioVolumeSlider.getValue() * 0.001));
            }
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    public static void handlePlayPauseButtonClick() {
        // if we're playing, pause the audio
        if (isAudioPlaying()) {

        }
        // otherwise start playing, this should always play something
        else {

        }
    }

    public static void stopAudio() {

    }

    public static void handleLastAudioButtonClick() {

    }

    public static void handleNextAudioButtonClick() {

    }

    private static boolean repeatAudio;

    public static void handleRepeatButtonClick() {

    }

    private static boolean shuffleAudio;

    public static void handleShuffleButtonClick() {

    }

    public static void playAudioNext(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());

        if (!isWidgetOpen()) {
            showGUI(audioFile);
        } else {
            audioFileQueue.add(0, audioFile);
        }
    }

    public static void playAudioLast(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());

        if (!isWidgetOpen()) {
            showGUI(audioFile);
        } else {
            audioFileQueue.add(audioFile);
        }
    }

    // used to ensure deleting audio isn't playing currently
    public static File getCurrentAudio() {
        return null;
    }

    public static void setUiComponentsVisible(boolean visible) {
        // todo shouldn't depened on mode
        albumArtLabel.setVisible(visible);
    }

    // -----------------------------------------------------
    // Audio Location Text class (layered over progress bar)
    // -----------------------------------------------------

    /**
     * The class to update the audio location label which is layered over the progress bar.
     */
    private static class AudioLocationLabelUpdater {
        /**
         * The formatter used for the audio location label text.
         */
        private static final DecimalFormat locationLabelFormat = new DecimalFormat("##.#");

        /**
         * The delay between update cycles for the audio lcoation text.
         */
        private static final int audioLocationTextUpdateDelay = 250;

        /**
         * Whether this AudioLocationlabelUpdater has been killed.
         */
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
                            // todo simply setting text
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

        public ScrollingTitleLabel(JLabel effectLabel, String localTitle) {
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

    // --------------------
    // Search YouTube View
    // --------------------

    // todo after audio player completely working and tested start with other views
}