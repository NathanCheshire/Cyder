package cyder.audio;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AtomicDouble;
import cyder.annotations.CyderAuthor;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderRegexPatterns;
import cyder.enums.CyderInspection;
import cyder.enums.Dynamic;
import cyder.enums.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.files.DirectoryWatcher;
import cyder.files.FileUtil;
import cyder.files.WatchDirectoryEvent;
import cyder.files.WatchDirectorySubscriber;
import cyder.getter.GetFileBuilder;
import cyder.getter.GetInputBuilder;
import cyder.getter.GetterUtil;
import cyder.handlers.external.PhotoViewer;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.math.NumberUtil;
import cyder.messaging.MessagingUtil;
import cyder.parsers.youtube.YoutubeSearchResultPage;
import cyder.parsers.youtube.YoutubeVideo;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadFactory;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.button.CyderButton;
import cyder.ui.button.CyderIconButton;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.drag.button.ChangeSizeButton;
import cyder.ui.field.CyderCaret;
import cyder.ui.field.CyderModernTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.frame.MenuType;
import cyder.ui.frame.NotificationBuilder;
import cyder.ui.label.CyderLabel;
import cyder.ui.pane.CyderOutputPane;
import cyder.ui.pane.CyderScrollPane;
import cyder.ui.slider.CyderSliderUi;
import cyder.ui.slider.ThumbShape;
import cyder.user.UserFile;
import cyder.utils.ImageUtil;
import cyder.utils.OsUtil;
import cyder.utils.SerializationUtil;
import cyder.utils.StaticUtil;
import cyder.youtube.YoutubeAudioDownload;
import cyder.youtube.YoutubeConstants;
import cyder.youtube.YoutubeUtil;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Port;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static cyder.audio.AudioIcons.*;

/**
 * An audio player widget which can also download YouTube video audio and thumbnails.
 */
@Vanilla
@CyderAuthor
@SuppressCyderInspections(CyderInspection.VanillaInspection)
public final class AudioPlayer {
    /**
     * The width and height of the audio frame.
     */
    private static final int DEFAULT_FRAME_LEN = 600;

    /**
     * The width and height of the album art label.
     */
    private static final int ALBUM_ART_LABEL_SIZE = 300;

    /**
     * The number to subtract from the frame height when the frame is in mini mode.
     */
    private static final int MINI_FRAME_HEIGHT_OFFSET = 150;

    /**
     * The number to subtract from the frame height when the frame is in hidden art mode.
     */
    private static final int HIDDEN_ART_HEIGHT_OFFSET = 40;

    /**
     * The width of a primary ui control row.
     */
    private static final int UI_ROW_WIDTH = (int) (ALBUM_ART_LABEL_SIZE * 1.5);

    /**
     * The height of a primary ui control row.
     */
    private static final int UI_ROW_HEIGHT = 40;

    /**
     * The audio player frame.
     */
    private static CyderFrame audioPlayerFrame;

    /**
     * The label used to hold the album art or default album art if no album
     * art exists if the audio player is in the standard audio view.
     */
    private static final JLabel albumArtLabel = new JLabel();

    /**
     * The border width of black borders placed on some ui components.
     */
    private static final int BORDER_WIDTH = 3;

    /**
     * The file representing the default album art to use if the frame is
     * in the standard audio view and the current audio file has no linked album art.
     */
    private static final File DEFAULT_ALBUM_ART = StaticUtil.getStaticResource("Default.png");

    /**
     * The format of the waveform image to export.
     */
    private static final String WAVEFORM_EXPORT_FORMAT = Extension.PNG.getExtensionWithoutPeriod();

    /**
     * The default text to display for the audio title label.
     */
    public static final String DEFAULT_AUDIO_TITLE = "No Audio Playing";

    /**
     * The label to display the current audio title.
     */
    private static final JLabel audioTitleLabel = new JLabel("", SwingConstants.CENTER);

    /**
     * The container to hold the audioTitleLabel used for animations like Spotify if the text overflows.
     */
    private static final JLabel audioTitleLabelContainer = new JLabel();

    /**
     * The label to display the seconds in.
     */
    private static final CyderLabel secondsInLabel = new CyderLabel();

    /**
     * The label to display the seconds remaining or total audio length.
     */
    private static final CyderLabel totalSecondsLabel = new CyderLabel();

    /**
     * The default value for the audio volume slider.
     */
    private static final int DEFAULT_AUDIO_SLIDER_VALUE = 50;

    /**
     * The audio volume slider.
     */
    private static final JSlider audioVolumeSlider = new JSlider(
            JSlider.HORIZONTAL, 0, 100, DEFAULT_AUDIO_SLIDER_VALUE);

    /**
     * The default value for the audio location slider.
     */
    private static final int DEFAULT_LOCATION_SLIDER_VALUE = 0;

    /**
     * The min value for the audio location slider.
     */
    private static final int DEFAULT_LOCATION_SLIDER_MIN_VALUE = 0;

    /**
     * The max value for the audio location slider.
     */
    private static final int DEFAULT_LOCATION_SLIDER_MAX_VALUE = 450;

    /**
     * The audio location slider.
     */
    private static final JSlider audioLocationSlider = new JSlider(
            JSlider.HORIZONTAL, DEFAULT_LOCATION_SLIDER_MIN_VALUE,
            DEFAULT_LOCATION_SLIDER_MAX_VALUE, DEFAULT_LOCATION_SLIDER_VALUE);

    /**
     * The ui for the audio volume slider.
     */
    private static final CyderSliderUi audioVolumeSliderUi = new CyderSliderUi(audioVolumeSlider);

    /**
     * the ui for the audio location slider.
     */
    private static final CyderSliderUi audioLocationSliderUi = new CyderSliderUi(audioLocationSlider);

    /**
     * The album art directory for the current Cyder user.
     */
    private static File currentUserAlbumArtDir;

    /**
     * The audio volume percent label which appears on change of the audio volume.
     */
    private static final CyderLabel audioVolumePercentLabel = new CyderLabel();

    /**
     * The size of the primary audio control buttons.
     */
    private static final Dimension CONTROL_BUTTON_SIZE = new Dimension(30, 30);

    /**
     * The play pause icon button.
     */
    private static JButton playPauseButton;

    /**
     * The play last audio icon button.
     */
    private static final CyderIconButton lastAudioButton =
            new CyderIconButton.Builder("Last", lastIcon, lastIconHover)
                    .setClickAction(AudioPlayer::handleLastAudioButtonClick).build();

    /**
     * The play next audio icon button.
     */
    private static final CyderIconButton nextAudioButton =
            new CyderIconButton.Builder("Next", nextIcon, nextIconHover)
                    .setClickAction(AudioPlayer::handleNextAudioButtonClick).build();

    /**
     * The repeat audio icon button.
     */
    private static final CyderIconButton repeatAudioButton =
            new CyderIconButton.Builder("Repeat", repeatIcon, repeatIconHover)
                    .setClickAction(AudioPlayer::handleRepeatButtonClick)
                    .setToggleButton(true).build();

    /**
     * The shuffle audio icon button.
     */
    private static final CyderIconButton shuffleAudioButton =
            new CyderIconButton.Builder("Shuffle", shuffleIcon, shuffleIconHover)
                    .setClickAction(AudioPlayer::handleShuffleButtonClick)
                    .setToggleButton(true).build();

    /**
     * The current frame view the audio player is in.
     */
    private static final AtomicReference<FrameView> currentFrameView = new AtomicReference<>(FrameView.FULL);

    /**
     * The frame background color.
     */
    public static final Color BACKGROUND_COLOR = new Color(8, 23, 52);

    /**
     * The background color of the track in front of the slider thumb.
     */
    private static final Color trackNewColor = new Color(45, 45, 45);

    /**
     * The list of songs to play next before sequentially proceeding to the next valid audio file.
     */
    private static final ArrayList<File> audioFileQueue = new ArrayList<>();

    /**
     * The current audio file we are at.
     */
    private static final AtomicReference<File> currentAudioFile = new AtomicReference<>();

    /**
     * The animator object for the audio volume percent.
     * This is set upon the frame appearing and is only killed when the widget is killed.
     */
    private static AudioVolumeLabelAnimator audioVolumeLabelAnimator;

    /**
     * The animator object for the audio location label.
     * This is set and the previous object killed whenever a new audio file is initiated.
     */
    private static AudioLocationUpdater audioLocationUpdater;

    /**
     * The scrolling title label to display and scroll the current
     * audio title if it exceeds the parent container's bounds.
     */
    private static ScrollingTitleLabel scrollingTitleLabel;

    /**
     * The last action invoked by the user.
     */
    private static LastAction lastAction = LastAction.Unknown;

    /**
     * Whether the audio location slider is currently pressed.
     */
    private static final AtomicBoolean audioLocationSliderPressed = new AtomicBoolean(false);

    /**
     * The thumb size for the sliders.
     */
    private static final int THUMB_SIZE = 25;

    /**
     * The value to increment the radius of the sliders by on click events.
     */
    private static final int BIG_THUMB_INC = 5;

    /**
     * The thumb size for the sliders on click events.
     */
    private static final int BIG_THUMB_SIZE = THUMB_SIZE + 2 * BIG_THUMB_INC;

    /**
     * The actual object that plays audio.
     */
    private static InnerAudioPlayer innerAudioPlayer;

    /**
     * The audio progress bar animation controller.
     */
    private static final AudioProgressBarAnimator audioProgressBarAnimator
            = new AudioProgressBarAnimator(audioLocationSlider, audioLocationSliderUi);

    /**
     * The stroke for the audio volume and location sliders.
     */
    private static final BasicStroke SLIDER_STROKE = new BasicStroke(2.0f);

    /**
     * The location of a quick single click audio location request.
     */
    private static final AtomicDouble possiblePercentRequest = new AtomicDouble(Long.MAX_VALUE);

    /**
     * The time of a quick single click audio location request mouse initial press event.
     */
    private static final AtomicLong possiblePercentRequestTime = new AtomicLong(Long.MAX_VALUE);

    /**
     * The window of time between the possible percent request initial
     * press and release to perform the percent request.
     */
    private static final int POSSIBLE_PERCENT_REQUEST_WINDOW = 100;

    /**
     * The total percent of a completed youtube audio download.
     */
    private static final float completedProgress = 100.0f;

    /**
     * The default album art image.
     */
    private static BufferedImage defaultAlbumArtImage;

    static {
        try {
            defaultAlbumArtImage = ImageUtil.read(DEFAULT_ALBUM_ART);
        } catch (Exception ignored) {}
    }

    /**
     * Suppress default constructor.
     */
    private AudioPlayer() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Allow widget to be found via reflection.
     */
    @Widget(triggers = {"mp3", "wav", "music", "audio"}, description = "An advanced audio playing widget")
    public static void showGui() {
        File userMusicDir = Dynamic.buildDynamic(
                Dynamic.USERS.getFileName(),
                Console.INSTANCE.getUuid(),
                UserFile.MUSIC.getName());

        File[] userMusicFiles = userMusicDir.listFiles((dir, name)
                -> FileUtil.isSupportedAudioExtension(OsUtil.buildFile(userMusicDir.getAbsolutePath(), name)));

        if (userMusicFiles != null && userMusicFiles.length > 0) {
            showGui(userMusicFiles[NumberUtil.generateRandomInt(userMusicFiles.length - 1)]);
        } else {
            throw new IllegalArgumentException("Could not find any user audio files");
        }
    }

    /**
     * Starts playing the provided audio file.
     * The file must be mp3 or wav.
     *
     * @param startPlaying the audio file to start playing
     * @throws IllegalArgumentException if startPlaying is null or doesn't exist
     */
    public static void showGui(File startPlaying) {
        checkNotNull(startPlaying);
        checkArgument(startPlaying.exists());

        currentAudioFile.set(startPlaying);

        audioDreamified.set(isCurrentAudioDreamy());

        startMusicFileAddedDirectoryWatcher();

        // if frame is open, stop whatever audio is playing or
        // paused and begin playing the requested audio
        if (isWidgetOpen()) {
            if (currentFrameView.get() == FrameView.SEARCH) {
                goBackFromSearchView();
            }

            boolean audioPlaying = isAudioPlaying();

            if (audioPlaying) {
                pauseAudio();
            }

            revalidateFromAudioFileChange();

            innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
            innerAudioPlayer.setLocation(0);
            audioLocationUpdater.setPercentIn(0f);
            audioLocationUpdater.update(false);

            if (audioPlaying) {
                playAudio();
            }

            return;
        }

        currentUserAlbumArtDir = Dynamic.buildDynamic(Dynamic.USERS.getFileName(),
                Console.INSTANCE.getUuid(), UserFile.MUSIC.getName(), UserFile.ALBUM_ART);

        audioPlayerFrame = new CyderFrame(DEFAULT_FRAME_LEN, DEFAULT_FRAME_LEN, BACKGROUND_COLOR);
        refreshFrameTitle();

        ChangeSizeButton changeSizeButton = new ChangeSizeButton();
        changeSizeButton.setToolTipText("Switch Mode");
        changeSizeButton.setClickAction(() -> {
            switch (currentFrameView.get()) {
                case FULL -> setupAndShowFrameView(FrameView.HIDDEN_ALBUM_ART);
                case HIDDEN_ALBUM_ART -> setupAndShowFrameView(FrameView.MINI);
                case MINI -> setupAndShowFrameView(FrameView.FULL);
                case SEARCH -> goBackFromSearchView();
                default -> throw new IllegalArgumentException(
                        "Illegal requested view to switch to via view switch frame button");
            }
        });
        audioPlayerFrame.getTopDragLabel().addRightButton(changeSizeButton, 1);
        audioPlayerFrame.setMenuType(MenuType.PANEL);
        audioPlayerFrame.setMenuEnabled(true);
        audioPlayerFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // no other pre/post close window Runnables
                // should be added or window listeners
                killAndCloseWidget();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                // no other pre/post close window Runnables
                // should be added or window listeners
                killAndCloseWidget();
            }
        });
        installFrameMenuItems();

        /*
         All components which will ever be on the frame for the audio player are added now and their sizes set.
         The bounds are set in the view switcher.
         The sizes are almost never set outside the construction below.
         */

        albumArtLabel.setSize(ALBUM_ART_LABEL_SIZE, ALBUM_ART_LABEL_SIZE);
        albumArtLabel.setOpaque(true);
        albumArtLabel.setBackground(BACKGROUND_COLOR);
        albumArtLabel.setBorder(new LineBorder(Color.BLACK, BORDER_WIDTH));
        audioPlayerFrame.getContentPane().add(albumArtLabel);

        albumArtLabel.add(dreamyLabel);
        dreamyLabel.setSize(albumArtLabel.getSize());
        dreamyLabel.setFont(dreamyLabel.getFont().deriveFont(150f));
        dreamyLabel.setVisible(false);

        audioTitleLabelContainer.setSize(UI_ROW_WIDTH, UI_ROW_HEIGHT);
        audioTitleLabel.setSize(UI_ROW_WIDTH, UI_ROW_HEIGHT);
        audioTitleLabel.setText(DEFAULT_AUDIO_TITLE);
        audioTitleLabel.setFont(CyderFonts.DEFAULT_FONT_SMALL);
        audioTitleLabel.setForeground(CyderColors.vanilla);

        audioTitleLabelContainer.add(audioTitleLabel, SwingConstants.CENTER);
        audioPlayerFrame.getContentPane().add(audioTitleLabelContainer);

        shuffleAudioButton.setSize(CONTROL_BUTTON_SIZE);
        audioPlayerFrame.getContentPane().add(shuffleAudioButton);

        lastAudioButton.setSize(CONTROL_BUTTON_SIZE);
        audioPlayerFrame.getContentPane().add(lastAudioButton);

        playPauseButton = new JButton();
        refreshPlayPauseButtonIcon();
        playPauseButton.setFocusPainted(false);
        playPauseButton.setOpaque(false);
        playPauseButton.setContentAreaFilled(false);
        playPauseButton.setBorderPainted(false);
        playPauseButton.setVisible(true);
        playPauseButton.addMouseListener(new MouseAdapter() {
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
        playPauseButton.setSize(CONTROL_BUTTON_SIZE);
        audioPlayerFrame.getContentPane().add(playPauseButton);

        nextAudioButton.setSize(CONTROL_BUTTON_SIZE);
        audioPlayerFrame.getContentPane().add(nextAudioButton);

        repeatAudioButton.setSize(CONTROL_BUTTON_SIZE);
        audioPlayerFrame.getContentPane().add(repeatAudioButton);

        audioLocationSliderUi.setThumbStroke(SLIDER_STROKE);
        audioLocationSliderUi.setThumbShape(ThumbShape.CIRCLE);
        audioLocationSliderUi.setThumbRadius(25);
        audioLocationSliderUi.setThumbFillColor(CyderColors.vanilla);
        audioLocationSliderUi.setThumbOutlineColor(CyderColors.vanilla);
        audioLocationSliderUi.setRightThumbColor(trackNewColor);
        audioLocationSliderUi.setLeftThumbColor(CyderColors.vanilla);
        audioLocationSliderUi.setTrackStroke(SLIDER_STROKE);
        audioLocationSliderUi.setAnimationEnabled(true);
        audioLocationSliderUi.setAnimationLen(75);

        audioLocationSlider.setSize(UI_ROW_WIDTH, UI_ROW_HEIGHT);
        audioLocationSlider.setMinorTickSpacing(1);
        audioLocationSlider.setValue(DEFAULT_LOCATION_SLIDER_VALUE);
        audioLocationSlider.setMinimum(DEFAULT_LOCATION_SLIDER_MIN_VALUE);
        audioLocationSlider.setMaximum(DEFAULT_LOCATION_SLIDER_MAX_VALUE);
        audioLocationSlider.setUI(audioLocationSliderUi);
        audioLocationSlider.setPaintTicks(false);
        audioLocationSlider.setPaintLabels(false);
        audioLocationSlider.setVisible(true);
        audioLocationSlider.addChangeListener(e -> {
            if (audioTotalLength == UNKNOWN_AUDIO_LENGTH || audioTotalLength == 0) {
                audioTotalLength = AudioUtil.getTotalBytes(currentAudioFile.get());
            }

            if (audioLocationUpdater != null) {
                audioLocationUpdater.setPercentIn((float) audioLocationSlider.getValue()
                        / audioLocationSlider.getMaximum());

                audioLocationUpdater.update(true);
            }
        });
        audioLocationSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                audioLocationSliderPressed.set(true);

                audioLocationSliderUi.setThumbRadius(BIG_THUMB_SIZE);
                audioLocationSlider.repaint();

                possiblePercentRequest.set(e.getX() / (float) audioLocationSlider.getWidth());
                possiblePercentRequestTime.set(System.currentTimeMillis());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (audioLocationSliderPressed.get()) {
                    audioLocationSliderPressed.set(false);

                    boolean wasPlaying = isAudioPlaying();

                    if (wasPlaying) {
                        float newPercentIn = (float) audioLocationSlider.getValue() / audioLocationSlider.getMaximum();

                        if (System.currentTimeMillis() - possiblePercentRequestTime.get()
                                < POSSIBLE_PERCENT_REQUEST_WINDOW) {
                            newPercentIn = (float) possiblePercentRequest.get();
                            possiblePercentRequest.set(Long.MAX_VALUE);
                            possiblePercentRequestTime.set(Long.MAX_VALUE);
                        }

                        long resumeLocation = (long) (newPercentIn * innerAudioPlayer.getTotalAudioLength());

                        audioLocationUpdater.pauseTimer();
                        pauseAudio();

                        innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
                        innerAudioPlayer.setLocation(resumeLocation);
                        audioLocationUpdater.setPercentIn(newPercentIn);

                        playAudio();
                    } else {
                        if (audioTotalLength == UNKNOWN_AUDIO_LENGTH || audioTotalLength == 0) {
                            audioTotalLength = AudioUtil.getTotalBytes(currentAudioFile.get());
                        }

                        float newPercentIn = (float) audioLocationSlider.getValue() / audioLocationSlider.getMaximum();
                        long resumeLocation = (long) (newPercentIn * audioTotalLength);

                        innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
                        innerAudioPlayer.setLocation(resumeLocation);
                        audioLocationUpdater.setPercentIn(newPercentIn);
                        audioLocationUpdater.update(true);
                    }
                }

                audioLocationSliderUi.setThumbRadius(THUMB_SIZE);
                audioLocationSlider.repaint();
            }
        });
        audioLocationSlider.setOpaque(false);
        audioLocationSlider.setFocusable(false);
        audioLocationSlider.repaint();
        audioPlayerFrame.getContentPane().add(audioLocationSlider);

        secondsInLabel.setSize(UI_ROW_WIDTH / 4, UI_ROW_HEIGHT);
        secondsInLabel.setText("");
        secondsInLabel.setHorizontalAlignment(JLabel.LEFT);
        secondsInLabel.setForeground(CyderColors.vanilla);
        audioPlayerFrame.getContentPane().add(secondsInLabel);
        secondsInLabel.setFocusable(false);

        totalSecondsLabel.setSize(UI_ROW_WIDTH / 4, UI_ROW_HEIGHT);
        totalSecondsLabel.setText("");
        totalSecondsLabel.setHorizontalAlignment(JLabel.RIGHT);
        totalSecondsLabel.setForeground(CyderColors.vanilla);
        audioPlayerFrame.getContentPane().add(totalSecondsLabel);
        totalSecondsLabel.setFocusable(false);

        audioLocationUpdater = new AudioLocationUpdater(secondsInLabel, totalSecondsLabel, currentFrameView,
                currentAudioFile, audioLocationSliderPressed, audioLocationSlider);

        audioVolumeSliderUi.setThumbStroke(SLIDER_STROKE);
        audioVolumeSliderUi.setThumbShape(ThumbShape.CIRCLE);
        audioVolumeSliderUi.setThumbRadius(25);
        audioVolumeSliderUi.setThumbFillColor(CyderColors.vanilla);
        audioVolumeSliderUi.setThumbOutlineColor(CyderColors.vanilla);
        audioVolumeSliderUi.setRightThumbColor(trackNewColor);
        audioVolumeSliderUi.setLeftThumbColor(CyderColors.vanilla);
        audioVolumeSliderUi.setTrackStroke(SLIDER_STROKE);

        audioVolumePercentLabel.setForeground(CyderColors.vanilla);
        audioVolumePercentLabel.setSize(100, 40);
        audioPlayerFrame.getContentPane().add(audioVolumePercentLabel);

        if (audioVolumeLabelAnimator != null) {
            audioVolumeLabelAnimator.kill();
        }

        audioVolumeLabelAnimator = new AudioVolumeLabelAnimator(audioVolumePercentLabel);

        audioVolumeSlider.setSize(UI_ROW_WIDTH, UI_ROW_HEIGHT);
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
            audioVolumePercentLabel.setVisible(true);
            audioVolumePercentLabel.setText(audioVolumeSlider.getValue() + "%");
            audioVolumeLabelAnimator.resetTimeout();
        });
        audioVolumeSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                audioVolumeSliderUi.setThumbRadius(BIG_THUMB_SIZE);
                audioVolumeSlider.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                audioVolumeSliderUi.setThumbRadius(THUMB_SIZE);
                audioVolumeSlider.repaint();
            }
        });
        audioVolumeSlider.setOpaque(false);
        audioVolumeSlider.setToolTipText("Volume");
        audioVolumeSlider.setFocusable(false);
        audioVolumeSlider.repaint();
        refreshAudioLine();

        setPhaseOneComponentsVisible(false);

        setupAndShowFrameView(FrameView.FULL);

        audioPlayerFrame.finalizeAndShow();

        Console.INSTANCE.revalidateAudioMenuVisibility();

        // now that frame is shown, ensure binaries installed and restrict UI until proven
        if (!AudioUtil.ffmpegInstalled() || !AudioUtil.youtubeDlInstalled()) {
            downloadBinaries();
        }
    }

    /**
     * Attempts to download ffmpeg and youtube-dl to reference locally for downloading and processing audio files.
     */
    private static void downloadBinaries() {
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

                    InformHandler.inform(new InformHandler.Builder("Could not download necessary "
                            + "binaries. Try to install both ffmpeg and youtube-dl and try again")
                            .setTitle("Network Error")
                            .setRelativeTo(audioPlayerFrame)
                            .setPostCloseAction(AudioPlayer::killAndCloseWidget));
                } else {
                    audioPlayerFrame.revokeAllNotifications();
                    unlockUi();
                    audioPlayerFrame.notify("Successfully downloaded necessary binaries");
                }
            } catch (Exception e) {
                unlockUi();
                ExceptionHandler.handle(e);
            }
        }, "AudioPlayer Preliminary Handler");
    }

    /**
     * Sets the visibility of all phase 1 components to the value of visible.
     *
     * @param visible whether to set phase 1 components to visible
     */
    public static void setPhaseOneComponentsVisible(boolean visible) {
        albumArtLabel.setVisible(visible);

        audioTitleLabel.setVisible(visible);
        audioTitleLabelContainer.setVisible(visible);

        shuffleAudioButton.setVisible(visible);
        lastAudioButton.setVisible(visible);
        playPauseButton.setVisible(visible);
        nextAudioButton.setVisible(visible);
        repeatAudioButton.setVisible(visible);

        secondsInLabel.setVisible(visible);
        totalSecondsLabel.setVisible(visible);

        audioVolumePercentLabel.setVisible(visible);

        audioVolumeSlider.setVisible(visible);
        audioLocationSlider.setVisible(visible);
    }

    /**
     * Whether the UI is locked.
     */
    private static boolean uiLocked;

    /**
     * Locks the UI components of the audio player.
     */
    public static void lockUi() {
        uiLocked = true;

        audioPlayerFrame.setMenuEnabled(false);
    }

    /**
     * Unlocks the UI components of the audio player.
     */
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

    /**
     * Whether the wav exporter menu option is locked.
     */
    private static final AtomicBoolean wavExporterLocked = new AtomicBoolean();

    /**
     * Whether the mp3 exporter menu option is locked.
     */
    private static final AtomicBoolean mp3ExporterLocked = new AtomicBoolean();

    /**
     * Whether the waveform exporter menu option is locked.
     */
    private static final AtomicBoolean waveformExporterLocked = new AtomicBoolean();

    /**
     * Whether the audio file chooser menu option is locked.
     */
    private static final AtomicBoolean chooseFileLocked = new AtomicBoolean();

    /**
     * Whether the dreamify menu option is locked.
     */
    private static final AtomicBoolean dreamifierLocked = new AtomicBoolean();

    /**
     * Whether the current audio is dreamified.
     */
    private static final AtomicBoolean audioDreamified = new AtomicBoolean();

    /**
     * Whether the current frame view is the search view.
     */
    private static final AtomicBoolean onSearchView = new AtomicBoolean();

    /**
     * Installs all the menu options on the AudioPlayer frame.
     */
    private static void installFrameMenuItems() {
        audioPlayerFrame.clearMenuItems();

        audioPlayerFrame.addMenuItem("Export wav", exportWavMenuItem);
        audioPlayerFrame.addMenuItem("Export mp3", exportMp3MenuItem);
        audioPlayerFrame.addMenuItem("Waveform", waveformExporterMenuItem);
        audioPlayerFrame.addMenuItem("Search", searchMenuItem, onSearchView);
        audioPlayerFrame.addMenuItem("Choose File", chooseFileMenuItem);
        audioPlayerFrame.addMenuItem("Dreamify", dreamifyMenuItem, audioDreamified);
    }

    /**
     * The menu item to export the current audio as a wav.
     */
    private static final Runnable exportWavMenuItem = () -> {
        if (wavExporterLocked.get() || uiLocked) {
            return;
        }

        if (FileUtil.validateExtension(currentAudioFile.get(), Extension.WAV.getExtension())) {
            audioPlayerFrame.notify("This file is already a wav");
        } else if (FileUtil.validateExtension(currentAudioFile.get(), Extension.MP3.getExtension())) {
            CyderThreadRunner.submit(() -> {
                Future<Optional<File>> wavConvertedFile = AudioUtil.mp3ToWav(currentAudioFile.get());

                wavExporterLocked.set(true);

                while (!wavConvertedFile.isDone()) {
                    Thread.onSpinWait();
                }

                wavExporterLocked.set(false);

                try {
                    if (wavConvertedFile.get().isPresent()) {
                        File moveTo = Dynamic.buildDynamic(
                                Dynamic.USERS.getFileName(),
                                Console.INSTANCE.getUuid(),
                                UserFile.MUSIC.getName(),
                                FileUtil.getFilename(wavConvertedFile.get().get()) + Extension.WAV.getExtension());

                        Files.copy(Paths.get(wavConvertedFile.get().get().getAbsolutePath()),
                                Paths.get(moveTo.getAbsolutePath()));

                        audioPlayerFrame.notify("Saved \""
                                + moveTo.getName() + "\" to your music directory");
                    } else {
                        audioPlayerFrame.notify("Could not convert \""
                                + currentAudioFile.get().getName() + "\" to a wav at this time");
                    }
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "Wav exporter");
        } else {
            throw new IllegalArgumentException("Unsupported audio format: " + currentAudioFile.get().getName());
        }
    };

    /**
     * The menu item for exporting the current audio as an mp3.
     */
    private static final Runnable exportMp3MenuItem = () -> {
        if (mp3ExporterLocked.get() || uiLocked) {
            return;
        }

        if (FileUtil.validateExtension(currentAudioFile.get(), Extension.MP3.getExtension())) {
            audioPlayerFrame.notify("This file is already an mp3");
        } else if (FileUtil.validateExtension(currentAudioFile.get(), Extension.WAV.getExtension())) {
            CyderThreadRunner.submit(() -> {
                Future<Optional<File>> mp3ConvertedFile = AudioUtil.wavToMp3(currentAudioFile.get());

                mp3ExporterLocked.set(true);

                while (!mp3ConvertedFile.isDone()) {
                    Thread.onSpinWait();
                }

                mp3ExporterLocked.set(false);

                try {
                    if (mp3ConvertedFile.get().isPresent()) {
                        File moveTo = Dynamic.buildDynamic(
                                Dynamic.USERS.getFileName(),
                                Console.INSTANCE.getUuid(),
                                UserFile.MUSIC.getName(),
                                FileUtil.getFilename(mp3ConvertedFile.get().get()) + Extension.MP3.getExtension());

                        Files.copy(Paths.get(mp3ConvertedFile.get().get().getAbsolutePath()),
                                Paths.get(moveTo.getAbsolutePath()));

                        audioPlayerFrame.notify("Saved \""
                                + moveTo.getName() + "\" to your music directory");
                    } else {
                        audioPlayerFrame.notify("Could not convert \""
                                + currentAudioFile.get().getName() + "\" to an mp3 at this time");
                    }
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "Mp3 exporter");
        } else {
            throw new IllegalArgumentException("Unsupported audio format: " + currentAudioFile.get().getName());
        }
    };

    /**
     * The logic for the export waveform menu option.
     */
    private static final Runnable waveformExporterMenuItem = () -> {
        if (waveformExporterLocked.get() || uiLocked) {
            return;
        }

        CyderThreadRunner.submit(() -> {
            Optional<String> optionalSaveName = GetterUtil.getInstance().getInput(
                    new GetInputBuilder("Export Waveform", "Enter a name to export the waveform as")
                            .setRelativeTo(audioPlayerFrame)
                            .setSubmitButtonText("Save to files")
                            .setInitialFieldText(FileUtil.getFilename(getCurrentAudio()) + "_waveform"));
            if (optionalSaveName.isEmpty()) return;
            String saveName = optionalSaveName.get();

            if (OsUtil.isValidFilename(saveName)) {
                File saveFile = Dynamic.buildDynamic(
                        Dynamic.USERS.getFileName(),
                        Console.INSTANCE.getUuid(),
                        UserFile.FILES.getName(),
                        saveName + "." + WAVEFORM_EXPORT_FORMAT);

                Future<BufferedImage> waveform = MessagingUtil.generateLargeWaveform(currentAudioFile.get());

                waveformExporterLocked.set(true);

                while (!waveform.isDone()) {
                    Thread.onSpinWait();
                }

                waveformExporterLocked.set(false);

                try {
                    ImageIO.write(waveform.get(), WAVEFORM_EXPORT_FORMAT, saveFile.getAbsoluteFile());
                    audioPlayerFrame.notify(new NotificationBuilder
                            ("Saved waveform to your files directory")
                            .setOnKillAction(() -> PhotoViewer.getInstance(saveFile).showGui()));
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                    audioPlayerFrame.notify("Could not save waveform at this time");
                }
            } else {
                audioPlayerFrame.notify("Invalid filename for " + OsUtil.OPERATING_SYSTEM_NAME);
            }

        }, "AudioPlayer Waveform Exporter");
    };

    /**
     * The menu item for searching youtube for songs.
     */
    private static final Runnable searchMenuItem = () -> {
        if (onSearchView.get()) {
            goBackFromSearchView();
        } else {
            onSearchView.set(true);
            constructPhaseTwoView();
        }
    };

    /**
     * The menu item for choosing a local audio file.
     */
    private static final Runnable chooseFileMenuItem = () -> {
        if (chooseFileLocked.get() || uiLocked) {
            return;
        }

        CyderThreadRunner.submit(() -> {
            chooseFileLocked.set(true);
            Optional<File> optionalFile = GetterUtil.getInstance().getFile(
                    new GetFileBuilder("Audio file chooser")
                            .setRelativeTo(audioPlayerFrame));
            if (optionalFile.isEmpty()) return;
            File chosenFile = optionalFile.get();

            chooseFileLocked.set(false);

            if (FileUtil.isSupportedAudioExtension(chosenFile)) {
                lastAction = LastAction.FileChosen;

                if (currentFrameView.get() == FrameView.SEARCH) {
                    goBackFromSearchView();
                }

                pauseAudio();

                currentAudioFile.set(chosenFile);

                revalidateFromAudioFileChange();

                playAudio();
            } else {
                audioPlayerFrame.notify("Invalid file chosen");
            }
        }, "AudioPlayer File Chooser");
    };

    /**
     * The runnable used to dreamify an audio file.
     */
    private static final Runnable dreamifyRunnable = () -> {
        audioPlayerFrame.notify(new NotificationBuilder("Dreamifying \""
                + FileUtil.getFilename(currentAudioFile.get()) + CyderStrings.quote).setViewDuration(10000));
        dreamifierLocked.set(true);

        Future<Optional<File>> dreamifiedAudioFuture = AudioUtil.dreamifyAudio(currentAudioFile.get());

        while (!dreamifiedAudioFuture.isDone()) {
            Thread.onSpinWait();
        }

        Optional<File> dreamifiedAudio;

        try {
            dreamifiedAudio = dreamifiedAudioFuture.get();
        } catch (Exception ignored) {
            dreamifyFailed();
            return;
        }

        if (dreamifiedAudio.isEmpty()) {
            dreamifyFailed();
            return;
        }

        File dreamifiedFile = dreamifiedAudio.get();
        File targetFile = Dynamic.buildDynamic(
                Dynamic.USERS.getFileName(),
                Console.INSTANCE.getUuid(),
                UserFile.MUSIC.getName(),
                dreamifiedFile.getName());

        try {
            Path source = dreamifiedFile.toPath();
            Path targetDirectory = targetFile.toPath();

            Files.copy(source, targetDirectory);
        } catch (Exception ignored) {
            dreamifyFailed();
            return;
        }

        dreamyAudioCallback(targetFile);
    };

    /**
     * A callback used when dreamifying an audio file finishes.
     *
     * @param dreamyAudio the dreamified audio file to play
     */
    private static void dreamyAudioCallback(File dreamyAudio) {
        float percentIn = (float) audioLocationSlider.getValue()
                / audioLocationSlider.getMaximum();

        boolean audioPlaying = isAudioPlaying();

        if (audioPlaying) {
            pauseAudio();
        }

        currentAudioFile.set(dreamyAudio);

        revalidateFromAudioFileChange();

        innerAudioPlayer = new InnerAudioPlayer(dreamyAudio);
        innerAudioPlayer.setLocation((long) (percentIn
                * AudioUtil.getTotalBytes(dreamyAudio)));
        audioLocationUpdater.setPercentIn(percentIn);
        audioLocationUpdater.update(false);

        if (audioPlaying) {
            playAudio();
        }

        dreamifierLocked.set(false);
        audioPlayerFrame.notify("Successfully dreamified audio");
    }

    /**
     * The item menu to toggle between dreamify states of an audio file.
     */
    private static final Runnable dreamifyMenuItem = () -> {
        if (dreamifierLocked.get() || uiLocked) {
            return;
        }

        if (currentAudioFile.get() == null) {
            return;
        }

        String currentAudioFilename = FileUtil.getFilename(currentAudioFile.get());

        // Already dreamified so attempt to find non-dreamy version
        if (currentAudioFilename.endsWith(AudioUtil.DREAMY_SUFFIX)) {
            String nonDreamyName = currentAudioFilename.substring(0,
                    currentAudioFilename.length() - AudioUtil.DREAMY_SUFFIX.length());

            if (attemptFindNonDreamyAudio(nonDreamyName)) {
                return;
            }

            audioPlayerFrame.notify("Could not find audio's non-dreamy equivalent");
            return;
        }

        File userMusicDir = Dynamic.buildDynamic(Dynamic.USERS.getFileName(), Console.INSTANCE.getUuid(),
                UserFile.MUSIC.getName());

        // Not dreamified so attempt to find previously dreamified file if exists
        if (userMusicDir.exists()) {
            if (attemptFindDreamyAudio(currentAudioFilename)) {
                return;
            }
        }

        CyderThreadRunner.submit(dreamifyRunnable, "Audio Dreamifier: " + currentAudioFilename);
    };

    /**
     * Attempts to find the non dreamy version of the audio currently
     * playing and if found, set it as the current audio. This will be played if any
     * audio was playing when requested.
     *
     * @param nonDreamyName the name of the non-dreamy audio file to look for
     * @return whether the non dreamy audio file was located
     */
    private static boolean attemptFindNonDreamyAudio(String nonDreamyName) {
        for (File validAudioFile : getValidAudioFiles()) {
            String localFilename = FileUtil.getFilename(validAudioFile);

            if (localFilename.equals(nonDreamyName)) {
                float percentIn = (float) audioLocationSlider.getValue()
                        / audioLocationSlider.getMaximum();

                boolean audioPlaying = isAudioPlaying();

                if (audioPlaying) {
                    pauseAudio();
                }

                currentAudioFile.set(validAudioFile);

                revalidateFromAudioFileChange();

                innerAudioPlayer = new InnerAudioPlayer(validAudioFile);
                innerAudioPlayer.setLocation((long) (percentIn
                        * AudioUtil.getTotalBytes(validAudioFile)));
                audioLocationUpdater.setPercentIn(percentIn);
                audioLocationUpdater.update(false);

                if (audioPlaying) {
                    playAudio();
                }

                audioDreamified.set(false);
                audioPlayerFrame.revalidateMenu();

                return true;
            }
        }

        return false;
    }

    /**
     * Attempts to find the non-dreamy audio file corresponding to the current dreamy audio file
     *
     * @param currentAudioFilename the name of the current non-dreamy audio file
     * @return whether the dreamy audio file was found and handled
     */
    private static boolean attemptFindDreamyAudio(String currentAudioFilename) {
        for (File validAudioFile : getValidAudioFiles()) {
            if ((currentAudioFilename + AudioUtil.DREAMY_SUFFIX)
                    .equalsIgnoreCase(FileUtil.getFilename(validAudioFile))) {
                float percentIn = (float) audioLocationSlider.getValue()
                        / audioLocationSlider.getMaximum();

                boolean audioPlaying = isAudioPlaying();

                if (audioPlaying) {
                    pauseAudio();
                }

                currentAudioFile.set(validAudioFile);

                revalidateFromAudioFileChange();

                innerAudioPlayer = new InnerAudioPlayer(validAudioFile);
                innerAudioPlayer.setLocation((long) (percentIn
                        * AudioUtil.getTotalBytes(validAudioFile)));
                audioLocationUpdater.setPercentIn(percentIn);
                audioLocationUpdater.update(false);

                if (audioPlaying) {
                    playAudio();
                }

                audioDreamified.set(true);
                audioPlayerFrame.revalidateMenu();

                return true;
            }
        }

        return false;
    }

    /**
     * Performs calls necessary when a requested dreamify audio call failed.
     */
    private static void dreamifyFailed() {
        audioDreamified.set(false);
        dreamifierLocked.set(false);
        audioPlayerFrame.revalidateMenu();
        audioPlayerFrame.revokeAllNotifications();
        audioPlayerFrame.notify("Could not dreamify audio at this time");
    }

    /**
     * The padding used between component rows.
     */
    private static final int yComponentPadding = 20;

    /**
     * The width and height used for the primary control buttons.
     */
    private static final int primaryButtonWidth = 30;

    /**
     * The spacing between the primary buttons.
     */
    private static final int primaryButtonSpacing = (int) ((1.5 * ALBUM_ART_LABEL_SIZE - 5 * 30) / 6);

    /**
     * Sets component visibilities and locations based on the provided frame view.
     *
     * @param view the requested frame view
     */
    private static void setupAndShowFrameView(FrameView view) {
        setPhaseOneComponentsVisible(false);
        setPhaseTwoComponentsVisible(false);

        switch (view) {
            case FULL -> {
                setPhaseOneComponentsVisible(true);
                currentFrameView.set(FrameView.FULL);
                audioPlayerFrame.setSize(DEFAULT_FRAME_LEN, DEFAULT_FRAME_LEN);

                // set location of all components needed
                int xOff = DEFAULT_FRAME_LEN / 2 - ALBUM_ART_LABEL_SIZE / 2;
                int yOff = CyderDragLabel.DEFAULT_HEIGHT;
                yOff += 20;
                albumArtLabel.setLocation(xOff, yOff);
                yOff += ALBUM_ART_LABEL_SIZE + yComponentPadding;
                refreshAlbumArt();

                // xOff of rest of components is len s.t. the total
                // width is 1.5x the width of the  album art label
                xOff = (int) (DEFAULT_FRAME_LEN / 2 - (1.5 * ALBUM_ART_LABEL_SIZE) / 2);
                refreshAudioTitleLabel();
                audioTitleLabelContainer.setLocation(xOff, yOff);
                yOff += 40 + yComponentPadding;
                shuffleAudioButton.setLocation(xOff + primaryButtonSpacing, yOff);
                lastAudioButton.setLocation(xOff + primaryButtonSpacing * 2 + primaryButtonWidth, yOff);
                playPauseButton.setLocation(xOff + primaryButtonSpacing * 3 + primaryButtonWidth * 2, yOff);
                nextAudioButton.setLocation(xOff + primaryButtonSpacing * 4 + primaryButtonWidth * 3, yOff);
                repeatAudioButton.setLocation(xOff + primaryButtonSpacing * 5 + primaryButtonWidth * 4, yOff);
                yOff += 30 + yComponentPadding;
                audioLocationSlider.setLocation(xOff, yOff);
                secondsInLabel.setLocation(xOff, yOff + 20);
                totalSecondsLabel.setLocation(xOff + UI_ROW_WIDTH - UI_ROW_WIDTH / 4, yOff + 20);
                audioVolumePercentLabel.setLocation(DEFAULT_FRAME_LEN / 2 - audioVolumePercentLabel.getWidth() / 2,
                        yOff + 35);
                yOff += 40 + yComponentPadding;
                audioVolumeSlider.setLocation(xOff, yOff);
            }
            case HIDDEN_ALBUM_ART -> {
                setPhaseOneComponentsVisible(true);
                audioPlayerFrame.setSize(DEFAULT_FRAME_LEN, DEFAULT_FRAME_LEN
                        - ALBUM_ART_LABEL_SIZE - HIDDEN_ART_HEIGHT_OFFSET);
                albumArtLabel.setVisible(false);
                albumArtLabel.setBorder(null);

                // set location of all components needed
                int xOff;
                int yOff = CyderDragLabel.DEFAULT_HEIGHT + 10;

                // xOff of rest of components is s.t. the total width is 1.5x width of album art label
                xOff = (int) (DEFAULT_FRAME_LEN / 2 - (1.5 * ALBUM_ART_LABEL_SIZE) / 2);
                refreshAudioTitleLabel();
                audioTitleLabelContainer.setLocation(xOff, yOff);
                yOff += 40 + yComponentPadding;
                shuffleAudioButton.setLocation(xOff + primaryButtonSpacing, yOff);
                lastAudioButton.setLocation(xOff + primaryButtonSpacing * 2 + primaryButtonWidth, yOff);
                playPauseButton.setLocation(xOff + primaryButtonSpacing * 3 + primaryButtonWidth * 2, yOff);
                nextAudioButton.setLocation(xOff + primaryButtonSpacing * 4 + primaryButtonWidth * 3, yOff);
                repeatAudioButton.setLocation(xOff + primaryButtonSpacing * 5 + primaryButtonWidth * 4, yOff);
                yOff += 30 + yComponentPadding;
                audioLocationSlider.setLocation(xOff, yOff);
                secondsInLabel.setLocation(xOff, yOff + 20);
                totalSecondsLabel.setLocation(xOff + UI_ROW_WIDTH - UI_ROW_WIDTH / 4, yOff + 20);

                audioVolumePercentLabel.setLocation(
                        DEFAULT_FRAME_LEN / 2 - audioVolumePercentLabel.getWidth() / 2, yOff + 35);
                yOff += 40 + yComponentPadding;
                audioVolumeSlider.setLocation(xOff, yOff);
                currentFrameView.set(FrameView.HIDDEN_ALBUM_ART);
            }
            case MINI -> {
                currentFrameView.set(FrameView.MINI);
                setPhaseOneComponentsVisible(true);
                audioPlayerFrame.setSize(DEFAULT_FRAME_LEN, DEFAULT_FRAME_LEN
                        - ALBUM_ART_LABEL_SIZE - MINI_FRAME_HEIGHT_OFFSET);
                albumArtLabel.setVisible(false);
                albumArtLabel.setBorder(null);

                // set location of all components needed
                int xOff;
                int yOff = CyderDragLabel.DEFAULT_HEIGHT + 10;

                // xOff of rest of components is s.t. the total width is 1.5x width of album art label
                xOff = (int) (DEFAULT_FRAME_LEN / 2 - (1.5 * ALBUM_ART_LABEL_SIZE) / 2);
                refreshAudioTitleLabel();
                yOff += 40 + yComponentPadding;
                shuffleAudioButton.setLocation(xOff + primaryButtonSpacing, yOff);
                lastAudioButton.setLocation(xOff + primaryButtonSpacing * 2 + primaryButtonWidth, yOff);
                playPauseButton.setLocation(xOff + primaryButtonSpacing * 3 + primaryButtonWidth * 2, yOff);
                nextAudioButton.setLocation(xOff + primaryButtonSpacing * 4 + primaryButtonWidth * 3, yOff);
                repeatAudioButton.setLocation(xOff + primaryButtonSpacing * 5 + primaryButtonWidth * 4, yOff);
                secondsInLabel.setVisible(false);
                audioVolumeSlider.setVisible(false);
                totalSecondsLabel.setVisible(false);
                audioLocationSlider.setVisible(false);
            }
            default -> throw new IllegalArgumentException("Unsupported frame view to switch to: " + view);
        }
    }

    /**
     * The default frame title.
     */
    private static final String DEFAULT_FRAME_TITLE = "Audio Player";

    /**
     * Refreshes the audio frame title.
     */
    private static void refreshFrameTitle() {
        String title = DEFAULT_FRAME_TITLE;

        if (currentAudioFile.get() != null) {
            title = StringUtil.capsFirstWord(StringUtil.getTrimmedText(FileUtil.getFilename(currentAudioFile.get())));
        }

        audioPlayerFrame.setTitle(title);
    }

    /**
     * The label to place over the album art if the audio has been dreamified.
     */
    private static final CyderLabel dreamyLabel = new CyderLabel("D");

    /**
     * Attempts to find and set the album art label to the current audio file's album art if it originates
     * from a user's audio files with a linked audio file album art. Otherwise, the label is set to the
     * default album art.
     */
    private static void refreshAlbumArt() {
        if (currentFrameView.get() != FrameView.FULL) {
            albumArtLabel.setVisible(false);
            return;
        }

        String name = FileUtil.getFilename(currentAudioFile.get());
        boolean dreamy = isCurrentAudioDreamy();

        if (name.endsWith(AudioUtil.DREAMY_SUFFIX)) {
            name = name.substring(0, name.length() - AudioUtil.DREAMY_SUFFIX.length());
            dreamy = true;
        }

        File albumArtFilePng = OsUtil.buildFile(currentUserAlbumArtDir.getAbsolutePath(),
                name + Extension.PNG.getExtension());
        File albumArtFileJpg = OsUtil.buildFile(currentUserAlbumArtDir.getAbsolutePath(),
                name + Extension.JPG.getExtension());

        ImageIcon customAlbumArt = null;

        try {
            if (albumArtFilePng.exists()) {
                customAlbumArt = new ImageIcon(ImageUtil.read(albumArtFilePng));
            } else if (albumArtFileJpg.exists()) {
                customAlbumArt = new ImageIcon(ImageUtil.read(albumArtFileJpg));
            } else {
                customAlbumArt = new ImageIcon(ImageUtil.read(DEFAULT_ALBUM_ART));
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        ImageIcon regularIcon = ImageUtil.ensureFitsInBounds(customAlbumArt,
                new Dimension(ALBUM_ART_LABEL_SIZE, ALBUM_ART_LABEL_SIZE));
        if (dreamy) {

            ImageIcon distortedIcon = ImageUtil.toImageIcon(
                    ImageUtil.grayscaleImage(ImageUtil.toBufferedImage(regularIcon)));

            albumArtLabel.setIcon(distortedIcon);

            dreamyLabel.setVisible(true);

            audioPlayerFrame.setCustomTaskbarIcon(distortedIcon);
        } else {
            albumArtLabel.setIcon(regularIcon);
            audioPlayerFrame.setCustomTaskbarIcon(regularIcon);

            dreamyLabel.setVisible(false);
        }

        albumArtLabel.repaint();

        Console.INSTANCE.revalidateMenu();
    }

    /**
     * Returns whether the current audio file is a dreamy audio file.
     *
     * @return whether the current audio file is a dreamy audio file
     */
    private static boolean isCurrentAudioDreamy() {
        return FileUtil.getFilename(currentAudioFile.get()).endsWith(AudioUtil.DREAMY_SUFFIX);
    }

    /**
     * Terminates the current ScrollingTitleLabel object controlling the title label
     * in the title label container and creates a new instance based on the current audio file's title.
     */
    static void refreshAudioTitleLabel() {
        String text = StringUtil.capsFirstWord(FileUtil.getFilename(currentAudioFile.get().getName()));

        // end old object
        if (scrollingTitleLabel != null) {
            // if the same title then do not update
            if (scrollingTitleLabel.localTitle().equals(text)) {
                return;
            }

            scrollingTitleLabel.kill();
            scrollingTitleLabel = null;
        }

        int textWidth = StringUtil.getAbsoluteMinWidth(text, audioTitleLabel.getFont());
        textWidth = Math.max(textWidth, ScrollingTitleLabel.MIN_WIDTH);

        int textHeight = StringUtil.getMinHeight(text, audioTitleLabel.getFont());
        int parentWidth = audioTitleLabel.getParent().getWidth();
        int parentHeight = audioTitleLabel.getParent().getHeight();

        if (textWidth > parentWidth) {
            scrollingTitleLabel = new ScrollingTitleLabel(audioTitleLabel, text);
        } else {
            audioTitleLabel.setBounds(parentWidth / 2 - textWidth / 2,
                    parentHeight / 2 - textHeight / 2, textWidth, textHeight);
            audioTitleLabel.setText(text);
        }
    }

    /**
     * Refreshes the audio progress label and total length.
     */
    private static void refreshAudioProgressLabel() {
        if (currentFrameView.get() == FrameView.MINI) {
            return;
        }

        if (audioLocationUpdater != null) {
            audioLocationUpdater.kill();
        }

        audioLocationUpdater = new AudioLocationUpdater(secondsInLabel, totalSecondsLabel, currentFrameView,
                currentAudioFile, audioLocationSliderPressed, audioLocationSlider);
    }

    /**
     * Returns a list of valid audio files within the current directory.
     */
    private static ImmutableList<File> getValidAudioFiles() {
        checkNotNull(currentAudioFile);

        ArrayList<File> ret = new ArrayList<>();

        File parentDirectory = currentAudioFile.get().getParentFile();

        if (parentDirectory.exists()) {
            File[] siblings = parentDirectory.listFiles();

            if (siblings != null && siblings.length > 0) {
                for (File sibling : siblings) {
                    if (FileUtil.isSupportedAudioExtension(sibling)) {
                        ret.add(sibling);
                    }
                }
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Refreshes the icon of the play/pause button.
     */
    static void refreshPlayPauseButtonIcon() {
        if (isAudioPlaying()) {
            playPauseButton.setIcon(pauseIcon);
            playPauseButton.setToolTipText("Pause");
        } else {
            playPauseButton.setIcon(playIcon);
            playPauseButton.setToolTipText("Play");
        }

        Console.INSTANCE.revalidateAudioMenuVisibility();
    }

    /**
     * Returns whether audio is playing.
     *
     * @return whether audio is playing
     */
    public static boolean isAudioPlaying() {
        return innerAudioPlayer != null && innerAudioPlayer.isPlaying();
    }

    /**
     * Returns whether the widget is open.
     *
     * @return whether the widget is open
     */
    public static boolean isWidgetOpen() {
        return audioPlayerFrame != null;
    }

    /**
     * Refreshes the audio volume based on the audio volume slider.
     */
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

    /**
     * Handles a click from the play/pause button.
     */
    public static void handlePlayPauseButtonClick() {
        if (shouldSuppressClick()) {
            return;
        }

        checkNotNull(currentAudioFile);
        checkArgument(!uiLocked);

        if (isAudioPlaying()) {
            pauseAudio();
        } else {
            playAudio();
        }

        Console.INSTANCE.revalidateAudioMenuVisibility();
    }

    /**
     * Starts playing the current audio file.
     */
    private static void playAudio() {
        try {
            // object created outside
            if (innerAudioPlayer != null && !innerAudioPlayer.isKilled()) {
                innerAudioPlayer.play();
                lastAction = LastAction.Play;
                audioLocationUpdater.resumeTimer();
                audioProgressBarAnimator.setState(AudioProgressBarAnimator.State.RUNNING);
            }
            // resume
            else if (lastAction == LastAction.Pause) {
                innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
                lastAction = LastAction.Play;
                innerAudioPlayer.setLocation(pauseLocation);
                innerAudioPlayer.play();
                audioLocationUpdater.resumeTimer();
                audioProgressBarAnimator.setState(AudioProgressBarAnimator.State.RUNNING);
            }
            // spin off object
            else if (innerAudioPlayer == null) {
                innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
                lastAction = LastAction.Play;
                innerAudioPlayer.play();
                audioLocationUpdater.resumeTimer();
                audioProgressBarAnimator.setState(AudioProgressBarAnimator.State.RUNNING);
            }
            // standard play
            else {
                lastAction = LastAction.Play;
                innerAudioPlayer.play();
                audioLocationUpdater.resumeTimer();
                audioProgressBarAnimator.setState(AudioProgressBarAnimator.State.RUNNING);
            }

            pauseLocation = UNKNOWN_PAUSE_LOCATION;
            pauseLocationMillis = UNKNOWN_PAUSE_LOCATION;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * A call back for InnerAudioPlayers to invoke when they are killed.
     */
    static void playAudioCallback() {
        // user didn't click any buttons so we should try and find the next audio
        if (lastAction == LastAction.Play) {
            if (innerAudioPlayer != null) {
                innerAudioPlayer.kill();
                innerAudioPlayer = null;
            }

            if (repeatAudio) {
                innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
                audioLocationUpdater.setPercentIn(0);
                audioLocationUpdater.update(false);
                playAudio();
            } else if (!audioFileQueue.isEmpty()) {
                currentAudioFile.set(audioFileQueue.remove(0));
                innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
                revalidateFromAudioFileChange();
                playAudio();
            } else if (shuffleAudio) {
                currentAudioFile.set(getValidAudioFiles().get(getRandomIndex()));
                innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
                revalidateFromAudioFileChange();
                playAudio();
            } else {
                int currentIndex = getCurrentAudioIndex();

                int nextIndex = currentIndex + 1 == getValidAudioFiles().size() ? 0 : currentIndex + 1;

                currentAudioFile.set(getValidAudioFiles().get(nextIndex));

                innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());

                revalidateFromAudioFileChange();

                playAudio();
            }
        }
    }

    /**
     * A magic number to denote an undefined pause location.
     */
    private static final long UNKNOWN_PAUSE_LOCATION = -1L;

    /*
     * The location the previous InnerAudioPlayer was killed at, if available.
     */
    private static long pauseLocation;

    /**
     * The location in milliseconds the previous InnerAudioPlayer was paused at.
     */
    private static long pauseLocationMillis;

    /**
     * A magic number used to denote an unknown audio length;
     */
    private static final long UNKNOWN_AUDIO_LENGTH = -1;

    /**
     * The total length of the current (paused or playing) audio.
     */
    private static long audioTotalLength = UNKNOWN_AUDIO_LENGTH;

    /**
     * Returns the location in milliseconds into the current audio file.
     *
     * @return the location in milliseconds into the current audio file
     */
    public static long getMillisecondsIn() {
        return innerAudioPlayer != null ? innerAudioPlayer.getMillisecondsIn() : pauseLocationMillis;
    }

    /**
     * Pauses playback of the current audio file.
     */
    private static void pauseAudio() {
        if (innerAudioPlayer != null) {
            audioTotalLength = innerAudioPlayer.getTotalAudioLength();
            pauseLocationMillis = innerAudioPlayer.getMillisecondsIn();
            audioProgressBarAnimator.setState(AudioProgressBarAnimator.State.PAUSED);
            pauseLocation = innerAudioPlayer.kill();
            innerAudioPlayer = null;
            lastAction = LastAction.Pause;
            audioLocationUpdater.pauseTimer();
            refreshPlayPauseButtonIcon();
        }
    }

    /**
     * The number of milliseconds which will not trigger a song restart instead of previous audio skip action if
     * the skip back button is pressed in the inclusive [0, SECONDS_IN_RESTART_TOL].
     */
    private static final int MILLISECONDS_IN_RESTART_TOL = 5000;

    /**
     * Handles a click from the last button.
     */
    public static void handleLastAudioButtonClick() {
        if (shouldSuppressClick()) {
            return;
        }

        checkNotNull(currentAudioFile);
        checkArgument(!uiLocked);

        lastAction = LastAction.Skip;

        boolean shouldPlay = isAudioPlaying();

        if (shouldPlay) {
            pauseAudio();
        }

        if (pauseLocationMillis > MILLISECONDS_IN_RESTART_TOL) {
            audioLocationUpdater.pauseTimer();
            audioLocationUpdater.setPercentIn(0);

            innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
            audioLocationUpdater.update(false);

            if (shouldPlay) {
                playAudio();
            }

            return;
        }

        int currentIndex = getCurrentAudioIndex();
        int lastIndex = currentIndex == 0 ? getValidAudioFiles().size() - 1 : currentIndex - 1;

        currentAudioFile.set(getValidAudioFiles().get(lastIndex));

        innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
        audioLocationUpdater.update(false);
        audioLocationSliderUi.resetAnimation();

        revalidateFromAudioFileChange();

        if (shouldPlay) {
            playAudio();
        }
    }

    /**
     * Handles a click from the next audio button.
     */
    public static void handleNextAudioButtonClick() {
        if (shouldSuppressClick()) {
            return;
        }

        checkNotNull(currentAudioFile);
        checkArgument(!uiLocked);

        lastAction = LastAction.Skip;

        boolean shouldPlay = isAudioPlaying();

        pauseAudio();

        int currentIndex = getCurrentAudioIndex();
        int nextIndex = currentIndex == getValidAudioFiles().size() - 1 ? 0 : currentIndex + 1;

        if (shuffleAudio) {
            nextIndex = getRandomIndex();
        }

        currentAudioFile.set(getValidAudioFiles().get(nextIndex));
        innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
        audioLocationSliderUi.resetAnimation();

        revalidateFromAudioFileChange();

        if (shouldPlay) {
            playAudio();
        }
    }

    /**
     * Refreshes the valid audio files and returns the index of the current audio file.
     *
     * @return the index of the current audio file
     */
    private static int getCurrentAudioIndex() {
        ImmutableList<File> validAudioFiles = getValidAudioFiles();

        int currentIndex = 0;

        for (int i = 0 ; i < validAudioFiles.size() ; i++) {
            if (validAudioFiles.get(i).getAbsolutePath().equals(currentAudioFile.get().getAbsolutePath())) {
                currentIndex = i;
                break;
            }
        }

        return currentIndex;
    }

    /**
     * Whether the current audio should be repeated on conclusion.
     */
    private static boolean repeatAudio;

    /**
     * Handles a click from the repeat button.
     */
    public static void handleRepeatButtonClick() {
        // always before handle button methods
        checkNotNull(currentAudioFile);
        checkArgument(!uiLocked);

        repeatAudio = !repeatAudio;
    }

    /**
     * Whether the next audio file should be chosen at random upon completion of the current audio file.
     */
    private static boolean shuffleAudio;

    /**
     * Handles a click of the shuffle button.
     */
    public static void handleShuffleButtonClick() {
        checkNotNull(currentAudioFile);
        checkArgument(!uiLocked);

        shuffleAudio = !shuffleAudio;
    }

    /**
     * Adds the audio to the beginning of the audio file queue.
     *
     * @param audioFile the audio file to add to the beginning of the queue
     */
    public static void addAudioNext(File audioFile) {
        checkNotNull(audioFile);
        checkArgument(audioFile.exists());

        if (!isWidgetOpen()) {
            showGui(audioFile);
        } else {
            audioFileQueue.add(0, audioFile);
        }
    }

    /**
     * Adds the audio to the end of the audio file queue.
     *
     * @param audioFile the audio file to add to the end of the queue
     */
    public static void addAudioLast(File audioFile) {
        checkNotNull(audioFile);
        checkArgument(audioFile.exists());

        if (!isWidgetOpen()) {
            showGui(audioFile);
        } else {
            audioFileQueue.add(audioFile);
        }
    }

    /**
     * Returns the current audio file open by the AudioPlayer.
     *
     * @return the current audio file open by the AudioPlayer
     */
    public static File getCurrentAudio() {
        return currentAudioFile.get();
    }

    /**
     * Resets all objects and closes the audio player widget.
     */
    private static void killAndCloseWidget() {
        if (isAudioPlaying()) {
            pauseAudio();
        }

        currentAudioFile.set(null);

        if (audioPlayerFrame != null) {
            audioPlayerFrame.dispose(true);
            audioPlayerFrame = null;
        }

        if (audioVolumeLabelAnimator != null) {
            audioVolumeLabelAnimator.kill();
            audioVolumeLabelAnimator = null;
        }

        if (audioLocationUpdater != null) {
            audioLocationUpdater.kill();
            audioLocationUpdater = null;
        }

        if (scrollingTitleLabel != null) {
            scrollingTitleLabel.kill();
            scrollingTitleLabel = null;
        }

        if (innerAudioPlayer != null) {
            innerAudioPlayer.kill();
            innerAudioPlayer = null;
        }

        Console.INSTANCE.revalidateAudioMenuVisibility();

        endAllMusicFileAddedWatchers();
    }

    /**
     * The time in ms to delay possible ui interactions.
     */
    private static final int ACTION_TIMEOUT_MS = 50;

    /**
     * The last time a ui action was permitted.
     */
    private static long lastActionTime;

    /**
     * Returns whether to suppress a requested ui action such as a button click.
     *
     * @return whether to suppress a requested ui action such as a button click
     */
    private static boolean shouldSuppressClick() {
        if (System.currentTimeMillis() - lastActionTime >= ACTION_TIMEOUT_MS) {
            lastActionTime = System.currentTimeMillis();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Revalidates necessary components following an audio file change.
     */
    private static void revalidateFromAudioFileChange() {
        refreshFrameTitle();
        refreshAudioTitleLabel();
        refreshAlbumArt();
        refreshAudioProgressLabel();

        audioDreamified.set(isCurrentAudioDreamy());

        audioPlayerFrame.revalidateMenuIfVisible();
    }

    /**
     * Returns a random index of the validAudioFiles list.
     *
     * @return a random index of the validAudioFiles list
     */
    private static int getRandomIndex() {
        ImmutableList<File> validAudioFiles = getValidAudioFiles();

        if (validAudioFiles.size() == 1) {
            return 0;
        }

        LinkedList<Integer> integers = new LinkedList<>();

        for (int i = 0 ; i < validAudioFiles.size() ; i++) {
            if (i == getCurrentAudioIndex()) {
                continue;
            }

            integers.add(i);
        }

        return integers.get(NumberUtil.generateRandomInt(integers.size() - 1));
    }

    // --------------------------------
    // Phase Two Components and methods
    // --------------------------------

    private static final AtomicBoolean phaseTwoViewLocked = new AtomicBoolean(false);

    /**
     * The list of search results previously found.
     */
    private static final LinkedList<YoutubeSearchResult> searchResults = new LinkedList<>();

    /**
     * The length of the thumbnails.
     */
    private static final int thumbnailLength = 250;

    /**
     * The text pane used to display youtube search results.
     */
    private static JTextPane searchResultsPane;

    /**
     * The printing util used for printing out search results to the scroll pane.
     */
    private static StringUtil printingUtil;

    /**
     * The scroll pane for the search results pane.
     */
    private static CyderScrollPane searchResultsScroll;

    /**
     * The search button for phase two.
     */
    private static CyderButton searchButton;

    /**
     * The button used to go back to the main audio page.
     */
    private static CyderButton backButton;

    /**
     * The width of phase two components excluding the scroll pane.
     */
    private static final int phaseTwoWidth = 300;

    /**
     * The information label for displaying progress when a search is underway.
     */
    private static CyderLabel informationLabel;

    /**
     * The search field for downloading audio.
     */
    private static CyderModernTextField searchField;

    /**
     * The previously searched text.
     */
    private static String previousSearch;

    /**
     * The previous location of the search scroll pane.
     */
    private static int previousScrollLocation;

    /**
     * The default information label text.
     */
    private static final String DEFAULT_INFORMATION_LABEL_TEXT = "Search YouTube using the above field";

    /**
     * Performs operations necessary to transitioning from the search view to the {@link FrameView#FULL} view.
     */
    private static void goBackFromSearchView() {
        previousScrollLocation = searchResultsScroll.getVerticalScrollBar().getValue();
        onSearchView.set(false);
        setPhaseTwoComponentsVisible(false);
        audioPlayerFrame.hideMenu();
        setupAndShowFrameView(FrameView.FULL);
    }

    /**
     * The color used as the background for the search results scroll and information label.
     */
    private static final Color SCROLL_BACKGROUND_COLOR = new Color(30, 30, 30);

    /**
     * Constructs the search view where a user can search for and download audio from youtube.
     */
    private static void constructPhaseTwoView() {
        if (uiLocked || phaseTwoViewLocked.get()) {
            return;
        }

        currentFrameView.set(FrameView.SEARCH);

        phaseTwoViewLocked.set(true);

        setPhaseOneComponentsVisible(false);

        audioPlayerFrame.hideMenu();

        int yOff = 50;

        audioPlayerFrame.setSize(DEFAULT_FRAME_LEN, DEFAULT_FRAME_LEN);

        searchField = new CyderModernTextField();
        searchField.setHorizontalAlignment(JTextField.CENTER);
        searchField.setFont(CyderFonts.DEFAULT_FONT_SMALL);
        searchField.setForeground(CyderColors.vanilla);
        searchField.setUnderlineColor(CyderColors.vanilla);
        searchField.setRippleColor(CyderColors.regularPurple);
        searchField.setCaret(new CyderCaret(CyderColors.vanilla));
        searchField.setBackground(BACKGROUND_COLOR);
        searchField.setBounds((audioPlayerFrame.getWidth() - phaseTwoWidth) / 2, yOff, phaseTwoWidth, 40);
        searchField.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, CyderColors.vanilla));
        audioPlayerFrame.getContentPane().add(searchField);

        yOff += 50;

        searchButton = new CyderButton("Search");
        searchButton.setBorder(BorderFactory.createEmptyBorder());
        searchButton.setBackground(CyderColors.regularPurple);
        searchButton.setForeground(CyderColors.vanilla);
        searchButton.setFont(CyderFonts.DEFAULT_FONT);
        searchButton.setBounds((audioPlayerFrame.getWidth() - phaseTwoWidth) / 2 + 50, yOff,
                phaseTwoWidth - 50, 40);
        audioPlayerFrame.getContentPane().add(searchButton);
        searchField.addActionListener(e -> searchAndUpdate());
        searchButton.addActionListener(e -> searchAndUpdate());

        backButton = new CyderButton(" < ");
        backButton.setBorder(BorderFactory.createEmptyBorder());
        backButton.setBackground(CyderColors.regularPurple);
        backButton.setToolTipText("Back");
        backButton.setForeground(CyderColors.vanilla);
        backButton.setFont(CyderFonts.DEFAULT_FONT);
        backButton.setBounds((audioPlayerFrame.getWidth() - phaseTwoWidth) / 2, yOff, 40, 40);
        audioPlayerFrame.getContentPane().add(backButton);
        backButton.addActionListener(e -> goBackFromSearchView());

        yOff += 60;

        searchResultsPane = new JTextPane();
        searchResultsPane.setEditable(false);
        searchResultsPane.setAutoscrolls(false);
        searchResultsPane.setBounds((audioPlayerFrame.getWidth() - UI_ROW_WIDTH) / 2,
                yOff, UI_ROW_WIDTH, audioPlayerFrame.getWidth() - 20 - yOff);
        searchResultsPane.setFocusable(true);
        searchResultsPane.setOpaque(false);
        searchResultsPane.setBackground(Color.white);
        searchResultsPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchResultsPane.setAlignmentY(Component.CENTER_ALIGNMENT);

        DefaultCaret searchResultsPaneCaret = (DefaultCaret) searchResultsPane.getCaret();
        searchResultsPaneCaret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        StyleConstants.setAlignment(alignment, StyleConstants.ALIGN_CENTER);
        StyledDocument doc = searchResultsPane.getStyledDocument();
        doc.setParagraphAttributes(0, doc.getLength(), alignment, false);

        searchResultsScroll = new CyderScrollPane(searchResultsPane);
        searchResultsScroll.getViewport().setAutoscrolls(false);
        searchResultsScroll.setThumbSize(8);
        searchResultsScroll.getViewport().setOpaque(false);
        searchResultsScroll.setFocusable(true);
        searchResultsScroll.setOpaque(true);
        searchResultsScroll.setThumbColor(CyderColors.regularPink);
        searchResultsScroll.setBorder(new LineBorder(Color.black, 4));
        searchResultsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        searchResultsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        searchResultsScroll.setBounds((audioPlayerFrame.getWidth() - UI_ROW_WIDTH) / 2,
                yOff, UI_ROW_WIDTH, audioPlayerFrame.getWidth() - 20 - yOff);
        searchResultsScroll.setBackground(SCROLL_BACKGROUND_COLOR);

        informationLabel = new CyderLabel();
        informationLabel.setForeground(CyderColors.vanilla);
        informationLabel.setFont(CyderFonts.DEFAULT_FONT);
        informationLabel.setBackground(SCROLL_BACKGROUND_COLOR);
        informationLabel.setOpaque(true);
        informationLabel.setBorder(new LineBorder(Color.black, 4));
        informationLabel.setBounds((audioPlayerFrame.getWidth() - UI_ROW_WIDTH) / 2,
                yOff, UI_ROW_WIDTH, audioPlayerFrame.getWidth() - 20 - yOff);
        audioPlayerFrame.getContentPane().add(informationLabel);

        audioPlayerFrame.getContentPane().add(searchResultsScroll);
        searchResultsPane.revalidate();

        if (lastSearchResultsPage != null) {
            searchResultsPane.setDocument(lastSearchResultsPage);
            searchField.setText(previousSearch);
            hideInformationLabel();

            // yes there are two here, no I don't know why it works like this
            searchResultsScroll.getVerticalScrollBar().setValue(previousScrollLocation);
            searchResultsScroll.getVerticalScrollBar().setValue(previousScrollLocation);
        } else {
            showInformationLabel(DEFAULT_INFORMATION_LABEL_TEXT);
            searchResultsScroll.getVerticalScrollBar().setValue(0);
        }

        printingUtil = new StringUtil(new CyderOutputPane(searchResultsPane));

        phaseTwoViewLocked.set(false);
    }

    /**
     * Sets the phase two components to the visibility specified.
     *
     * @param visible whether the phase two components should be visible
     */
    @SuppressWarnings("SameParameterValue")
    private static void setPhaseTwoComponentsVisible(boolean visible) {
        if (searchField == null) {
            return;
        }

        searchField.setVisible(visible);
        searchButton.setVisible(visible);
        backButton.setVisible(visible);
        informationLabel.setVisible(visible);
        searchResultsPane.setVisible(visible);
        searchResultsScroll.setVisible(visible);
    }

    /**
     * Hides the information label.
     */
    private static void hideInformationLabel() {
        informationLabel.setVisible(false);
        informationLabel.setText("");
    }

    /**
     * Shows the information label with the provided text.
     *
     * @param text the text for the information label
     */
    private static void showInformationLabel(String text) {
        informationLabel.setText(text);
        informationLabel.setVisible(true);
    }

    /**
     * The number of search results to grab when searching youtube.
     */
    private static final int numSearchResults = 10;

    /**
     * A search result object to hold data in the results scroll pane.
     */
    private static record YoutubeSearchResult(String uuid, String title, String description,
                                              String channel, BufferedImage bi) {}

    /**
     * The alignment object used for menu alignment.
     */
    private static final SimpleAttributeSet alignment = new SimpleAttributeSet();

    /**
     * The string used for the information label when a youtube query is triggered.
     */
    private static final String SEARCHING = "Searching...";

    /**
     * The string use for download buttons.
     */
    private static final String DOWNLOAD = "Download";

    /**
     * The string use for download buttons during a mouse over event when the download is in progress.
     */
    private static final String CANCEL = "Cancel";

    /**
     * The button text for when a button should trigger an audio play event.
     */
    private static final String PLAY = "Play";

    /**
     * The button text for when a download is concluding and would typically say 100%.
     */
    private static final String FINISHING = "Finishing";

    /**
     * The information label text used for when no search results are found.
     */
    private static final String NO_RESULTS = "No results found";

    /**
     * The last search result output.
     */
    private static Document lastSearchResultsPage;

    /**
     * The formatting results string.
     */
    private static final String FORMATTING_RESULTS = "Formatting results...";

    /**
     * Searches YouTube for the provided text and updates the results pane with videos found.
     */
    private static void searchAndUpdate() {
        String rawFieldText = searchField.getText();

        if (StringUtil.isNullOrEmpty(rawFieldText) || rawFieldText.equalsIgnoreCase(previousSearch)) {
            return;
        }

        // Trim and replace multiple spaces with one
        String fieldText = rawFieldText.trim().replaceAll(CyderRegexPatterns.whiteSpaceRegex, CyderStrings.space);
        previousSearch = fieldText;

        String threadName = "YouTube Searcher, search: " + CyderStrings.quote + fieldText + CyderStrings.quote;
        CyderThreadRunner.submit(() -> {
            showInformationLabel(SEARCHING);

            Optional<YoutubeSearchResultPage> youtubeSearchResultPage =
                    getSearchResults(YoutubeUtil.buildYouTubeApiV3SearchQuery(numSearchResults, fieldText));

            if (youtubeSearchResultPage.isEmpty()) {
                showInformationLabel(NO_RESULTS);
                return;
            }

            searchResults.clear();

            for (YoutubeVideo video : youtubeSearchResultPage.get().getItems()) {
                searchResults.add(new YoutubeSearchResult(
                        video.getId().getVideoId(),
                        video.getSnippet().getTitle(),
                        video.getSnippet().getDescription(),
                        video.getSnippet().getChannelTitle(),
                        YoutubeUtil.getMaxResolutionSquareThumbnail(video.getId().getVideoId())
                                .orElse(defaultAlbumArtImage)));
            }

            // if user has searched for something else while getting the search results, don't update pane
            if (!fieldText.equals(searchField.getText())) {
                hideInformationLabel();
                return;
            }

            searchResultsScroll.setVisible(false);
            searchResultsPane.setVisible(false);
            searchResultsPane.setText("");

            showInformationLabel(FORMATTING_RESULTS);

            for (YoutubeSearchResult result : searchResults) {
                Optional<File> alreadyExistsOptional = AudioUtil.getCurrentUserMusicFileWithName(result.title);
                boolean alreadyExists = alreadyExistsOptional.isPresent();

                printSearchResultLabels(result);

                String url = YoutubeUtil.buildVideoUrl(result.uuid);
                YoutubeAudioDownload youtubeAudioDownload = new YoutubeAudioDownload();
                youtubeAudioDownload.setVideoLink(url);
                AtomicReference<YoutubeAudioDownload> downloadable = new AtomicReference<>(youtubeAudioDownload);
                AtomicBoolean mouseEntered = new AtomicBoolean(false);

                CyderButton downloadButton = new CyderButton();
                downloadButton.setLeftTextPadding(StringUtil.generateSpaces(5));
                downloadButton.setRightTextPadding(StringUtil.generateSpaces(4));
                downloadButton.setText(alreadyExists ? PLAY : DOWNLOAD);
                downloadButton.setBackground(CyderColors.regularPurple);
                downloadButton.setForeground(CyderColors.vanilla);
                downloadButton.setBorder(BorderFactory.createEmptyBorder());
                downloadButton.setFont(CyderFonts.DEFAULT_FONT.deriveFont(26f));
                downloadButton.setSize(phaseTwoWidth, 40);
                downloadButton.addActionListener(e -> {
                    if (downloadable.get().isDownloading()) {
                        downloadable.get().cancel();
                        return;
                    } else if (alreadyExists) {
                        playAudioFromSearchView(alreadyExistsOptional.get());
                        return;
                    } else if (downloadable.get().isDownloaded()) {
                        playAudioFromSearchView(downloadable.get().getAudioDownloadFile());
                        return;
                    }

                    if (downloadable.get().isCanceled()) {
                        downloadable.set(new YoutubeAudioDownload());
                        downloadable.get().setVideoLink(url);
                        downloadable.get().setOnCanceledCallback(() -> downloadButton.setText(DOWNLOAD));
                        downloadable.get().setOnDownloadedCallback(() -> {
                            downloadButton.setText(PLAY);
                            downloadButton.addActionListener(event ->
                                    playAudioFromSearchView(downloadable.get().getAudioDownloadFile()));
                        });
                    }

                    downloadable.get().downloadAudioAndThumbnail();

                    startDownloadUpdater(downloadable, downloadButton, mouseEntered);
                });
                downloadButton.addMouseListener(generateDownloadButtonMouseListener(downloadable,
                        mouseEntered, downloadButton, alreadyExists));
                downloadable.get().setOnCanceledCallback(() -> downloadButton.setText(DOWNLOAD));
                downloadable.get().setOnDownloadedCallback(() -> {
                    downloadButton.setText(PLAY);
                    downloadButton.addActionListener(event -> playAudioFromSearchView(
                            downloadable.get().getAudioDownloadFile()));
                });

                printingUtil.printlnComponent(downloadButton);
                printingUtil.println(CyderStrings.newline);
            }

            searchResultsPane.setCaretPosition(0);
            searchResultsScroll.setVisible(true);
            searchResultsPane.setVisible(true);
            hideInformationLabel();

            lastSearchResultsPage = searchResultsPane.getDocument();
        }, threadName);
    }

    /**
     * Switches to the main audio player view and plays the provided audio.
     *
     * @param audio the audio file to play after switching to the main view
     */
    private static void playAudioFromSearchView(File audio) {
        if (isAudioPlaying()) {
            pauseAudio();
        }

        currentAudioFile.set(audio);
        innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
        revalidateFromAudioFileChange();
        goBackFromSearchView();

        playAudio();
    }

    /**
     * Constructs and prints the title and channel labels for the provided youtube search result.
     *
     * @param result the youtube search result record
     */
    private static void printSearchResultLabels(YoutubeSearchResult result) {
        JLabel imageLabel = new JLabel(ImageUtil.toImageIcon(result.bi));
        imageLabel.setSize(thumbnailLength, thumbnailLength);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setBorder(new LineBorder(Color.black, 4));
        printingUtil.printlnComponent(imageLabel);

        printingUtil.println(CyderStrings.newline);

        CyderLabel titleLabel = new CyderLabel(result.title);
        titleLabel.setForeground(CyderColors.vanilla);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        printingUtil.printlnComponent(titleLabel);

        CyderLabel channelLabel = new CyderLabel(result.channel);
        channelLabel.setForeground(CyderColors.vanilla);
        channelLabel.setHorizontalAlignment(JLabel.CENTER);
        printingUtil.printlnComponent(channelLabel);

        printingUtil.println(CyderStrings.newline);
    }

    /**
     * Constructs a download button mouse listener using the provided props.
     *
     * @param downloadable   a reference to the download button's linked downloadable
     * @param mouseEntered   an atomic boolean to determine when the mouse is inside of the button
     * @param downloadButton the download button itself
     * @param alreadyExists  whether the audio this button is linked to has already been downloaded
     * @return a download button mouse listener
     */
    private static MouseAdapter generateDownloadButtonMouseListener(
            AtomicReference<YoutubeAudioDownload> downloadable, AtomicBoolean mouseEntered,
            CyderButton downloadButton, boolean alreadyExists) {
        return new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseEntered.set(true);

                if (downloadable.get().isDownloading()) {
                    downloadButton.setText(CANCEL);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseEntered.set(false);
                if (alreadyExists) return;

                if (downloadable.get().isDownloading() && !downloadable.get().isCanceled()) {
                    float progress = downloadable.get().getDownloadableProgress();

                    if (progress == completedProgress) {
                        downloadButton.setText(FINISHING);
                    } else {
                        downloadButton.setText(progress + "%");
                    }
                } else if (downloadable.get().isDownloaded()) {
                    downloadButton.setText(PLAY);
                } else {
                    downloadButton.setText(DOWNLOAD);
                }
            }
        };
    }

    /**
     * Starts the download updater to update the download button based on the current progress.
     *
     * @param downloadable   the youtube download
     * @param downloadButton the download button
     * @param mouseEntered   whether the mouse is currently in the button
     */
    private static void startDownloadUpdater(AtomicReference<YoutubeAudioDownload> downloadable,
                                             CyderButton downloadButton, AtomicBoolean mouseEntered) {

        String threadName = "YouTube audio downloader, name: " + CyderStrings.quote
                + downloadable.get().getDownloadableName() + CyderStrings.quote;
        CyderThreadRunner.submit(() -> {
            while (!downloadable.get().isDone()) {
                if (!mouseEntered.get()) {
                    float progress = downloadable.get().getDownloadableProgress();

                    if (progress == completedProgress) {
                        downloadButton.setText(FINISHING);
                    } else {
                        downloadButton.setText(progress + "%");
                    }
                }

                ThreadUtil.sleep(YoutubeConstants.DOWNLOAD_UPDATE_DELAY);
            }
        }, threadName);
    }

    /**
     * Returns the search results for a particular url query.
     *
     * @param url the constructed url to get youtube video results
     * @return the YoutubeSearchResultPage object if present, empty optional else
     */
    private static Optional<YoutubeSearchResultPage> getSearchResults(String url) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(url).openStream()))) {
            return Optional.of(SerializationUtil.fromJson(reader, YoutubeSearchResultPage.class));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Optional.empty();
    }

    /**
     * The music file added watchers.
     */
    private static final ArrayList<DirectoryWatcher> musicFileAddedWatchers = new ArrayList<>();

    /**
     * Stops all the music file added watchers.
     */
    private static void endAllMusicFileAddedWatchers() {
        musicFileAddedWatchers.forEach(DirectoryWatcher::stopWatching);
    }

    /**
     * Starts the directory watcher to compute the millis time of new audio files.
     */
    private static void startMusicFileAddedDirectoryWatcher() {
        DirectoryWatcher musicDirectoryWatcher = new DirectoryWatcher(
                Dynamic.buildDynamic(Dynamic.USERS.getFileName(),
                        Console.INSTANCE.getUuid(),
                        UserFile.MUSIC.getName()));

        WatchDirectorySubscriber subscriber = new WatchDirectorySubscriber() {
            @Override
            public void onEvent(DirectoryWatcher broker, WatchDirectoryEvent event, File eventFile) {
                if (FileUtil.validateExtension(eventFile, Extension.MP3.getExtension())) {
                    AudioUtil.getMillisMutagen(eventFile);
                }
            }
        };
        subscriber.subscribeTo(WatchDirectoryEvent.FILE_ADDED);
        musicDirectoryWatcher.addSubscriber(subscriber);

        musicFileAddedWatchers.add(musicDirectoryWatcher);
    }
}