package cyder.handlers.external;

import com.google.common.base.Preconditions;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.enums.DynamicDirectory;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.handlers.internal.objects.InformBuilder;
import cyder.messaging.MessagingUtils;
import cyder.threads.CyderThreadFactory;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

// todo views should slide in and out like StraightShot

// todo dreamify should be seamless audio transition

// todo still need to prevent spamming of skip actions

/**
 * An audio player widget which can also download YouTube video audio and thumbnails.
 */
public class AudioPlayer {
    private static CyderFrame audioPlayerFrame;
    private static final JLabel albumArtLabel = new JLabel();
    private static final int BORDER_WIDTH = 3;
    private static final File DEFAULT_ALBUM_ART = OSUtil.buildFile(
            "static","pictures","music","Default.png");

    public static final String DEFAULT_AUDIO_TITLE = "No Audio Playing";
    private static final JLabel audioTitleLabel = new JLabel();
    private static final JLabel audioTitleLabelContainer = new JLabel();
    private static final int AUDIO_TITLE_LABEL_HEIGHT =
            StringUtil.getMinHeight("YATTA", CyderFonts.defaultFontSmall);

    private static final CyderProgressBar audioProgressBar = new CyderProgressBar(0, 100);
    private static final CyderProgressUI audioProgressBarUi = new CyderProgressUI();
    private static final CyderLabel audioProgressLabel = new CyderLabel();

    private static final JSlider audioVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
    private static final CyderSliderUI audioVolumeSliderUi = new CyderSliderUI(audioVolumeSlider);
    private static final int DEFAULT_AUDIO_SLIDER_VALUE = 50;

    private static File currentUserAlbumArtDir;

    private static final CyderLabel audioPercentLabel = new CyderLabel();

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

    private static final Dimension CONTROL_BUTTON_SIZE = new Dimension(30, 30);

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
            repeatAudioButton.setIcon(repeatAudio ? repeatIcon : repeatIconHover);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            repeatAudioButton.setIcon(repeatAudio ? repeatIconHover : repeatIcon);
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
            shuffleAudioButton.setIcon(shuffleAudio ? shuffleIcon : shuffleIconHover);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            shuffleAudioButton.setIcon(shuffleAudio ? shuffleIconHover : shuffleIcon);
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

    public static final Color BACKGROUND_COLOR = new Color(8,23,52);

    private static final ArrayList<File> audioFileQueue = new ArrayList<>();
    private static File currentAudioFile;
    private static final ArrayList<File> validAudioFiles = new ArrayList<>();

    public static final File DEFAULT_AUDIO_FILE = OSUtil.buildFile(
            "static","audio","Kendrick Lamar - All The Stars.mp3");

    private static final int DEFAULT_FRAME_LEN = 600;

    public static final int ALBUM_ART_LABEL_SIZE = 300;

    private static final ImageIcon alternateView = new ImageIcon("static/pictures/icons/ChangeSize1");
    private static final ImageIcon alternateViewHover = new ImageIcon("static/pictures/icons/ChangeSize2");

    private static final CyderIconButton switchFrameAudioView = new CyderIconButton(
            "Switch Mode", alternateView, alternateViewHover,
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
            switchFrameAudioView.setIcon(alternateViewHover);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            switchFrameAudioView.setIcon(alternateView);
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
    @Widget(triggers = {"mp3", "wav", "music", "audio"}, description = "An advanced audio playing widget")
    public static void showGUI() {
        showGUI(DEFAULT_AUDIO_FILE);
    }

    // some how we shoudl bypass construction stuff and directly act as if a file was selected to play from
    /**
     * Starts playing the provided audio file.
     * The file must be mp3 or wav.
     *
     * @param startPlaying the audio file to start playing.
     *                     If null, {@link AudioPlayer#DEFAULT_AUDIO_FILE} is played.
     * @throws IllegalArgumentException if startPlaying is null or or doesn't exist
     */
    public static void showGUI(File startPlaying) {
        Preconditions.checkNotNull(startPlaying);
        Preconditions.checkArgument(startPlaying.exists());

        currentAudioFile = startPlaying;
        refreshAudioFiles();

        // if frame is open, stop whatever audio is playing or
        // paused and begin playing the requested audio
        if (isWidgetOpen()) {
            if (isAudioPlaying()) {
                stopAudio();
                pauseLocation = 0;
            }

            playAudio();

            return;
        }

        currentUserAlbumArtDir = OSUtil.buildFile(DynamicDirectory.DYNAMIC_PATH,
                DynamicDirectory.USERS.getDirectoryName(),
                ConsoleFrame.INSTANCE.getUUID(), UserFile.MUSIC.getName(), "AlbumArt");

        audioPlayerFrame = new CyderFrame(DEFAULT_FRAME_LEN, DEFAULT_FRAME_LEN, BACKGROUND_COLOR);
        refreshFrameTitle();
        audioPlayerFrame.getTopDragLabel().addButton(switchFrameAudioView, 0);
        audioPlayerFrame.setCurrentMenuType(CyderFrame.MenuType.PANEL);
        audioPlayerFrame.setMenuEnabled(true);
        audioPlayerFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // no other pre/post close window Runnables
                // should be added or window listeners
                killWidget();
            }
        });
        installFrameMenuItems();

        /*
         All components which will ever be on the frame for phase 1 are added now and their sizes set.
         The bounds are set in the view switcher.
         The sizes are almost never set outside of the construction below.
         */

        albumArtLabel.setSize(ALBUM_ART_LABEL_SIZE, ALBUM_ART_LABEL_SIZE);
        albumArtLabel.setOpaque(true);
        albumArtLabel.setBackground(BACKGROUND_COLOR);
        albumArtLabel.setBorder(new LineBorder(Color.BLACK, BORDER_WIDTH));
        audioPlayerFrame.getContentPane().add(albumArtLabel);

        int mainRowWidth = (int) (ALBUM_ART_LABEL_SIZE * 1.5);
        int mainRowHeight = 40;

        audioTitleLabelContainer.setSize(mainRowWidth, mainRowHeight);
        audioTitleLabel.setSize(mainRowWidth, mainRowHeight);
        audioTitleLabel.setText(DEFAULT_AUDIO_TITLE);
        audioTitleLabel.setFont(CyderFonts.defaultFontSmall);
        audioTitleLabel.setForeground(CyderColors.vanila);
        audioTitleLabelContainer.add(audioTitleLabel);
        audioPlayerFrame.getContentPane().add(audioTitleLabelContainer);

        shuffleAudioButton.setSize(CONTROL_BUTTON_SIZE);
        audioPlayerFrame.getContentPane().add(shuffleAudioButton);

        lastAudioButton.setSize(CONTROL_BUTTON_SIZE);
        audioPlayerFrame.getContentPane().add(lastAudioButton);

        playPauseButton.setSize(CONTROL_BUTTON_SIZE);
        audioPlayerFrame.getContentPane().add(playPauseButton);

        nextAudioButton.setSize(CONTROL_BUTTON_SIZE);
        audioPlayerFrame.getContentPane().add(nextAudioButton);

        repeatAudioButton.setSize(CONTROL_BUTTON_SIZE);
        audioPlayerFrame.getContentPane().add(repeatAudioButton);

        audioProgressBar.setSize(mainRowWidth, mainRowHeight);
        audioPlayerFrame.getContentPane().add(audioProgressBar);
        audioProgressBarUi.setAnimationDirection(AnimationDirection.LEFT_TO_RIGHT);
        audioProgressBarUi.setColors(new Color[] {CyderColors.regularPink, CyderColors.notificationForegroundColor});
        audioProgressBarUi.setShape(CyderProgressUI.Shape.SQUARE);
        audioProgressBar.setUI(audioProgressBarUi);
        audioProgressBar.setMinimum(0);
        audioProgressBar.setMaximum(10000);
        audioProgressBar.setOpaque(false);
        audioProgressBar.setFocusable(false);

        audioProgressLabel.setSize(mainRowWidth, mainRowHeight);
        audioProgressLabel.setForeground(CyderColors.vanila);
        audioProgressBar.add(audioProgressLabel);
        audioProgressLabel.setFocusable(false);
        audioProgressLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (uiLocked) {
                    return;
                }

                float audioPercent = e.getX() / (float) audioProgressLabel.getWidth();
                long skipLocation = (long) (((double) e.getX() / (double)
                        audioProgressLabel.getWidth()) * totalAudioLength);

                if (isAudioPlaying()) {
                    stopAudio();
                    pauseLocation = skipLocation;
                    playAudio();
                } else {
                    stopAudio();
                    pauseLocation = skipLocation;
                }

                // todo update progress bar and progress label
                // todo use methods for this
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

        audioPercentLabel.setForeground(CyderColors.vanila);
        audioPercentLabel.setSize(100, 40);
        audioPlayerFrame.getContentPane().add(audioPercentLabel);
        startAudioVolumeLabelThread();

        audioVolumeSlider.setSize(mainRowWidth, mainRowHeight);
        audioPlayerFrame.getContentPane().add(audioVolumeSlider);
        audioVolumeSlider.setUI(audioVolumeSliderUi);
        audioVolumeSlider.setMinimum(0);
        audioVolumeSlider.setMaximum(100);
        audioVolumeSlider.setPaintTicks(false);
        audioVolumeSlider.setPaintLabels(false);
        audioVolumeSlider.setVisible(true);
        audioVolumeSlider.setValue(DEFAULT_AUDIO_SLIDER_VALUE);
        audioVolumeSlider.addChangeListener(e -> {
            if (uiLocked) {
                return;
            }

            refreshAudioLine();
            audioPercentLabel.setVisible(true);
            audioPercentLabel.setText(audioVolumeSlider.getValue() + "%");
            audioVolumeLabelTimeout.set(MAX_AUDIO_VOLUME_LABEL_VISIBLE
                    + AUDIO_VOLUME_LABEL_SLEEP_TIME);
        });
        audioVolumeSlider.setOpaque(false);
        audioVolumeSlider.setToolTipText("Volume");
        audioVolumeSlider.setFocusable(false);
        audioVolumeSlider.repaint();
        refreshAudioLine();

        setUiComponentsVisible(false);

        setupAndShowFrameView(FrameView.FULL);

        audioPlayerFrame.finalizeAndShow();

        // now that frame is shown, ensure binaries installed and restrict UI until proven
        if (!AudioUtil.ffmpegInstalled() || !AudioUtil.youtubeDlInstalled()) {
            CyderThreadRunner.submit(() -> {
                try {
                    lockUi();

                    audioPlayerFrame.notify("Attempting to download ffmpeg or youtube-dl");

                    Future<Boolean> passedPreliminaries = handlePreliminaries();

                    while (!passedPreliminaries.isDone()) {
                        Thread.onSpinWait();
                    }

                    // wait to start playing if downloading
                    if (!passedPreliminaries.get()) {
                        audioPlayerFrame.revokeAllNotifications();

                        InformBuilder builder = new InformBuilder("Could not download necessary " +
                                "binaries. Try to install both ffmpeg and youtube-dl and try again");
                        builder.setTitle("Network Error");
                        builder.setRelativeTo(audioPlayerFrame);
                        builder.setPostCloseAction(() -> {
                            killWidget();
                        });

                        InformHandler.inform(builder);
                    } else {
                        audioPlayerFrame.revokeAllNotifications();
                        unlockUi();
                        audioPlayerFrame.notify("Successfully downloaded necessary binaries");
                    }
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "AudioPlayer Preliminary Handler");
        }
    }

    /**
     * The time remaining before setting the visibility of the audio volume label to false.
     */
    private static final AtomicInteger audioVolumeLabelTimeout = new AtomicInteger();

    /**
     * The time in between checks when sleeping before the audio volume label is set to invisible.
     */
    private static final int AUDIO_VOLUME_LABEL_SLEEP_TIME = 50;

    /**
     * The total sleep time before setting the audio volume label to invisible.
     */
    private static final int MAX_AUDIO_VOLUME_LABEL_VISIBLE = 3000;

    // todo is this a proper exit condition? maybe use a class
    private static void startAudioVolumeLabelThread() {
        CyderThreadRunner.submit(() -> {
            try {
                while (isWidgetOpen()) {
                    while (audioVolumeLabelTimeout.get() > 0) {
                        audioPercentLabel.setVisible(true);
                        Thread.sleep(AUDIO_VOLUME_LABEL_SLEEP_TIME);
                        audioVolumeLabelTimeout.getAndAdd(-AUDIO_VOLUME_LABEL_SLEEP_TIME);
                    }

                    audioPercentLabel.setVisible(false);
                }
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }, "Audio Progress Label Animator");
    }

    /**
     * Sets the visibility of all phase 1 components to the value of visible.
     *
     * @param visible whether to set phase 1 components to visible
     */
    public static void setUiComponentsVisible(boolean visible) {
        albumArtLabel.setVisible(visible);

        audioTitleLabel.setVisible(visible);
        audioTitleLabelContainer.setVisible(visible);

        shuffleAudioButton.setVisible(visible);
        lastAudioButton.setVisible(visible);
        playPauseButton.setVisible(visible);
        nextAudioButton.setVisible(visible);
        repeatAudioButton.setVisible(visible);

        audioProgressBar.setVisible(visible);
        if (visible) {
            audioProgressBar.setBorder(new LineBorder(Color.black, BORDER_WIDTH));
        } else {
            audioProgressBar.setBorder(null);
        }

        audioProgressLabel.setVisible(visible);

        audioPercentLabel.setVisible(visible);

        audioVolumeSlider.setVisible(visible);
    }

    private static boolean uiLocked;

    public static void lockUi() {
        uiLocked = true;

        audioPlayerFrame.setMenuEnabled(false);
    }

    public static void unlockUi() {
        uiLocked = false;

        audioPlayerFrame.setMenuEnabled(true);
    }

    private static Future<Boolean> handlePreliminaries() {
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("AudioPlayer Preliminary Handler")).submit(() -> {
            boolean binariesInstalled = true;

            if (!AudioUtil.youtubeDlInstalled()) {
                Future<Boolean> downloadedYoutubeDl = AudioUtil.downloadYoutubeDl();

                while (!downloadedYoutubeDl.isDone()) {
                    Thread.onSpinWait();
                }

                binariesInstalled = downloadedYoutubeDl.get();

                // if failed, immediately return false
                if (!binariesInstalled) {
                    return false;
                }
            }

            if (!AudioUtil.ffmpegInstalled()) {
                Future<Boolean> ffmpegDownloaded = AudioUtil.downloadFfmpegStack();

                while (!ffmpegDownloaded.isDone()) {
                    Thread.onSpinWait();
                }

                binariesInstalled = ffmpegDownloaded.get();
            }

            return binariesInstalled;
        });
    }

    private static void installFrameMenuItems() {
        // just to be safe
        audioPlayerFrame.clearMenuItems();
        audioPlayerFrame.addMenuItem("Export wav", () -> {
            if (FileUtil.validateExtension(currentAudioFile, ".wav")) {
                audioPlayerFrame.notify("This file is already a wav");
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
                            audioPlayerFrame.notify("Could not convert \""
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
        audioPlayerFrame.addMenuItem("Export mp3", () -> {
            if (FileUtil.validateExtension(currentAudioFile, ".mp3")) {
                audioPlayerFrame.notify("This file is already an mp3");
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
                            audioPlayerFrame.notify("Could not convert \""
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
        audioPlayerFrame.addMenuItem("Waveform", () -> {
            CyderThreadRunner.submit(() -> {
                GetterBuilder builder = new GetterBuilder("Export waveform");
                builder.setRelativeTo(audioPlayerFrame);
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
                            audioPlayerFrame.notify("Saved waveform to your files directory");
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                            audioPlayerFrame.notify("Could not save waveform at this time");
                        }
                    } else {
                        audioPlayerFrame.notify("Invalid filename for " + OSUtil.OPERATING_SYSTEM_NAME);
                    }
                }
            }, "AudioPlayer Waveform Exporter");
        });
        audioPlayerFrame.addMenuItem("Search", () -> {
            // phase 2
        });
        audioPlayerFrame.addMenuItem("Choose File", () -> {
            CyderThreadRunner.submit(() -> {
                GetterBuilder builder = new GetterBuilder("Choose an mp3 or wav file");
                builder.setRelativeTo(audioPlayerFrame);
                File chosenFile = GetterUtil.getInstance().getFile(builder);

                if (chosenFile != null && FileUtil.isSupportedAudioExtension(chosenFile)) {
                    // todo end stuff (method which calls smaller methods for this),

                    // set file and find audio fields in same directory
                    currentAudioFile = chosenFile;
                    refreshAudioFiles();

                    // todo start playing
                } else {
                    audioPlayerFrame.notify("Invalid file chosen");
                }
            }, "AudioPlayer File Chooser");
        });
        audioPlayerFrame.addMenuItem("Dreamify", () -> {
            // todo will need to determine if current audio has already been dreamified

            // continue playing audio if an mp3 while converting to wav

            // export as wav to tmp directory

            // reference the wav and play at the last frame the current audio was at
            // should be a seemless transition

            // how to handle pausing and resuming?
        });
    }

    private static void setupAndShowFrameView(FrameView view) {
        setUiComponentsVisible(false);

        switch (view) {
            case FULL:
                // set location of all components needed
                int xOff = DEFAULT_FRAME_LEN / 2 - ALBUM_ART_LABEL_SIZE / 2;
                int yOff = CyderDragLabel.DEFAULT_HEIGHT;
                yOff += 20;

                int yPadding = 20;

                albumArtLabel.setLocation(xOff, yOff);
                yOff += ALBUM_ART_LABEL_SIZE + yPadding;

                refreshAlbumArt();

                // xOff of rest of components is s.t. the total width is 1.5x width of album art label
                xOff = (int) (DEFAULT_FRAME_LEN / 2 - (1.5 * ALBUM_ART_LABEL_SIZE) / 2);

                audioTitleLabel.setSize(StringUtil.getAbsoluteMinWidth(audioTitleLabel.getText(),
                        audioTitleLabel.getFont()), AUDIO_TITLE_LABEL_HEIGHT);
                audioTitleLabel.setLocation(audioTitleLabelContainer.getWidth() / 2
                        - audioTitleLabel.getWidth() / 2, audioTitleLabelContainer.getHeight() / 2
                        - audioTitleLabel.getHeight() / 2);

                audioTitleLabelContainer.setLocation(xOff, yOff);
                yOff += 40 + yPadding;

                int buttonWidth = 30;
                int spacing = (int) ((1.5 * ALBUM_ART_LABEL_SIZE - 5 * 30) / 6);

                shuffleAudioButton.setLocation(xOff + spacing, yOff);
                lastAudioButton.setLocation(xOff + spacing * 2 + buttonWidth, yOff);
                playPauseButton.setLocation(xOff + spacing * 3 + buttonWidth * 2, yOff);
                nextAudioButton.setLocation(xOff + spacing * 4 + buttonWidth * 3, yOff);
                repeatAudioButton.setLocation(xOff + spacing * 5 + buttonWidth * 4, yOff);

                yOff += 30 + yPadding;

                audioProgressBar.setLocation(xOff, yOff);
                audioProgressBar.setValue(audioProgressBar.getMaximum());

                // 0,0 since it is layered perfectly over audioProgressBar
                audioProgressLabel.setLocation(0, 0);

                audioPercentLabel.setLocation(DEFAULT_FRAME_LEN / 2 - audioPercentLabel.getWidth() / 2,
                        yOff + 35);

                yOff += 40 + yPadding;

                audioVolumeSlider.setLocation(xOff, yOff);

                yOff += 40 + yPadding;

                setUiComponentsVisible(true);
                currentFrameView = FrameView.FULL;
                break;
            case HIDDEN_ART:
                setUiComponentsVisible(false);

                // set location of all components needed
                // only set elements to show to visible

                currentFrameView = FrameView.HIDDEN_ART;
                break;
            case MINI:
                setUiComponentsVisible(false);

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
            title = StringUtil.capsFirst(StringUtil.getTrimmedText(FileUtil.getFilename(currentAudioFile)));

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

        audioPlayerFrame.setTitle(title);
    }

    // todo should only call this if necessary
    private static void refreshAlbumArt() {
        File albumArtFile = OSUtil.buildFile(currentUserAlbumArtDir.getAbsolutePath(),
                FileUtil.getFilename(currentAudioFile) + ".png");

        ImageIcon customAlbumArt = null;

        if (albumArtFile.exists()) {
            try {
                customAlbumArt = new ImageIcon(ImageIO.read(albumArtFile));
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        } else {
            try {
                customAlbumArt = new ImageIcon(ImageIO.read(DEFAULT_ALBUM_ART));
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }

        albumArtLabel.setIcon(customAlbumArt);
        albumArtLabel.repaint();
        audioPlayerFrame.setCustomTaskbarIcon(customAlbumArt);
        audioPlayerFrame.setUseCustomTaskbarIcon(customAlbumArt != null);
        ConsoleFrame.INSTANCE.revalidateMenu();
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
        return audioPlayer != null && !audioPlayer.isComplete();
    }

    public static boolean isWidgetOpen() {
        return audioPlayerFrame != null;
    }

    public static void closeWidget() {
         if (audioPlayerFrame != null) {
             audioPlayerFrame.dispose(true);

             // todo other actions to end worker threads

             audioPlayerFrame = null;
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
        // always before handle button methods
        Preconditions.checkNotNull(currentAudioFile);
        Preconditions.checkArgument(!uiLocked);

        // if we're playing, pause the audio
        if (isAudioPlaying()) {
            stopAudio();
            playPauseButton.setIcon(playIcon);
        }
        // otherwise start playing, this should always play something
        else {
            playAudio();
            playPauseButton.setIcon(pauseIcon);
        }
    }

    private static FileInputStream fis;
    private static BufferedInputStream bis;
    private static Player audioPlayer;

    private static long pauseLocation;
    private static long totalAudioLength;

    private static final int PAUSE_AUDIO_REACTION_OFFSET = 10000;

    // todo play pause button can't be a cyder icon button

    public static void playAudio() {
        CyderThreadRunner.submit(() -> {
            try {
                fis = new FileInputStream(currentAudioFile);
                bis = new BufferedInputStream(fis);

                totalAudioLength = fis.available();

                // just to be safe
                fis.skip(Math.max(0, pauseLocation));

                audioPlayer = new Player(bis);
                audioPlayer.play();

                // todo handle next audio method? pass in optional skip direction enum?
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "AudioPlayer [" + FileUtil.getFilename(currentAudioFile) + "]");
    }

    public static void stopAudio() {
        try {
            if (fis != null) {
                pauseLocation = totalAudioLength - fis.available() - PAUSE_AUDIO_REACTION_OFFSET;
                fis = null;
            }

            if (bis != null) {
                bis = null;
            }

            if (audioPlayer != null) {
                audioPlayer.close();
                audioPlayer = null;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public static void handleLastAudioButtonClick() {
        // always before handle button methods
        Preconditions.checkNotNull(currentAudioFile);
        Preconditions.checkArgument(!uiLocked);

    }

    public static void handleNextAudioButtonClick() {
        // always before handle button methods
        Preconditions.checkNotNull(currentAudioFile);
        Preconditions.checkArgument(!uiLocked);

    }

    private static boolean repeatAudio;

    public static void handleRepeatButtonClick() {
        // always before handle button methods
        Preconditions.checkNotNull(currentAudioFile);
        Preconditions.checkArgument(!uiLocked);

        repeatAudio = !repeatAudio;
    }

    private static boolean shuffleAudio;

    public static void handleShuffleButtonClick() {
        // always before handle button methods
        Preconditions.checkNotNull(currentAudioFile);
        Preconditions.checkArgument(!uiLocked);

        shuffleAudio = !shuffleAudio;
    }

    public static void addAudioNext(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());

        if (!isWidgetOpen()) {
            showGUI(audioFile);
        } else {
            audioFileQueue.add(0, audioFile);
        }
    }

    public static void addAudioLast(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());

        if (!isWidgetOpen()) {
            showGUI(audioFile);
        } else {
            audioFileQueue.add(audioFile);
        }
    }

    // used to ensure deleting audio isn't playing currently
    // whenever this is open this is never null, if null the player is killed and not open
    public static File getCurrentAudio() {
        return currentAudioFile;
    }

    private static void killWidget() {
        if (audioPlayerFrame != null) {
            audioPlayerFrame.dispose();
        } else {
            audioPlayerFrame = null;

            // never null so save
            audioProgressBarUi.stopAnimationTimer();

            currentAudioFile = null;

            stopAudio();

            pauseLocation = 0;
            totalAudioLength = 0;

            // todo end all executors and reset to initial state
        }
    }

    /*
    Inner class thread workers
     */

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
        /**
         * Whether this scrolling title label object has been killed.
         */
        private final AtomicBoolean killed = new AtomicBoolean();

        /**
         * The timeout to sleep for before checking for title scroll label being terminated.
         */
        private static final int SLEEP_WITH_CHECKS_TIMEOUT = 50;

        /**
         * The timeout between moving the label from one side to the opposite side.
         */
        private static final int SIDE_TO_SIDE_TIMEOUT = 5000;

        /**
         * The timeout between starting the initial timeout.
         */
        private static final int INITIAL_TIMEOUT = 3000;

        /**
         * The timeout between movement increments of the title label.
         */
        private static final int MOVEMENT_TIMEOUT = 25;

        public ScrollingTitleLabel(JLabel effectLabel, String localTitle) {
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

                    CyderThreadRunner.submit(() -> {
                        try {
                            TimeUtil.sleepWithChecks(INITIAL_TIMEOUT, SLEEP_WITH_CHECKS_TIMEOUT, killed);

                            while (!killed.get()) {
                                int goBack = 0;

                                while (goBack < minWidth - parentWidth) {
                                    if (killed.get()) {
                                        break;
                                    }

                                    effectLabel.setLocation(effectLabel.getX() - 1, effectLabel.getY());
                                    Thread.sleep(MOVEMENT_TIMEOUT);
                                    goBack++;
                                }

                                TimeUtil.sleepWithChecks(SIDE_TO_SIDE_TIMEOUT, SLEEP_WITH_CHECKS_TIMEOUT, killed);

                                while (goBack > 0) {
                                    if (killed.get()) {
                                        break;
                                    }

                                    effectLabel.setLocation(effectLabel.getX() + 1, effectLabel.getY());
                                    Thread.sleep(MOVEMENT_TIMEOUT);
                                    goBack--;
                                }

                                TimeUtil.sleepWithChecks(SIDE_TO_SIDE_TIMEOUT, SLEEP_WITH_CHECKS_TIMEOUT, killed);
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
         * Kills the current scrolling title label.
         */
        public void kill() {
           killed.set(false);
        }
    }

    // --------------------
    // Search YouTube View
    // --------------------

    // todo after audio player completely working and tested start with other views
}