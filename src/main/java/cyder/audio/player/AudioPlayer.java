package cyder.audio.player;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.annotations.CyderAuthor;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.audio.AudioUtil;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderUrls;
import cyder.enumerations.CyderInspection;
import cyder.enumerations.Dynamic;
import cyder.enumerations.Extension;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.getter.GetFileBuilder;
import cyder.getter.GetInputBuilder;
import cyder.getter.GetterUtil;
import cyder.handlers.external.ImageViewer;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.math.NumberUtil;
import cyder.messaging.MessagingUtil;
import cyder.network.NetworkUtil;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadFactory;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.button.CyderButton;
import cyder.ui.button.CyderIconButton;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.drag.button.ChangeSizeButton;
import cyder.ui.drag.button.CloseButton;
import cyder.ui.drag.button.MinimizeButton;
import cyder.ui.drag.button.PinButton;
import cyder.ui.field.CyderCaret;
import cyder.ui.field.CyderModernTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.frame.enumerations.MenuType;
import cyder.ui.frame.notification.NotificationBuilder;
import cyder.ui.label.CyderLabel;
import cyder.ui.pane.CyderOutputPane;
import cyder.ui.pane.CyderScrollPane;
import cyder.ui.slider.CyderSliderUi;
import cyder.ui.slider.ThumbShape;
import cyder.user.UserDataManager;
import cyder.user.UserFile;
import cyder.utils.*;
import cyder.youtube.YouTubeAudioDownload;
import cyder.youtube.YouTubeConstants;
import cyder.youtube.YouTubeUtil;
import cyder.youtube.parsers.YouTubeSearchResultPage;
import cyder.youtube.parsers.YouTubeVideo;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Port;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
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
     * The available views for this widget.
     */
    enum View {
        /**
         * All ui elements visible.
         */
        FULL,

        /**
         * Album art hidden.
         */
        HIDDEN_ALBUM_ART,

        /**
         * Mini audio player mode.
         */
        MINI,

        /**
         * Searching YouTube for a video's audio to download.
         */
        SEARCH,
    }

    /**
     * Primary ui control actions.
     */
    private enum LastAction {
        /**
         * The user pressed play.
         */
        Play,

        /**
         * The user pressed skip back or skip forward.
         */
        Skip,

        /**
         * The audio was skipped
         */
        Pause,

        /**
         * The user changed the audio location.
         */
        Scrub,

        /**
         * An audio file was chosen using the file chooser menu option.
         */
        FileChosen,

        /**
         * Something else not yet handled.
         */
        Unknown,
    }

    /**
     * The width and height of the audio frame.
     */
    private static final int defaultFrameLength = 600;

    /**
     * The width and height of the album art label.
     */
    private static final int albumArtSize = 300;

    /**
     * The number to subtract from the frame height when the frame is in mini mode.
     */
    private static final int miniFrameHeightOffset = 150;

    /**
     * The number to subtract from the frame height when the frame is in hidden art mode.
     */
    private static final int hiddenArtHeightOffset = 40;

    /**
     * The width of a primary ui control row.
     */
    private static final int fullRowWidth = (int) (albumArtSize * 1.5);

    /**
     * The height of a primary ui control row.
     */
    private static final int fullRowHeight = 40;

    /**
     * The label used to hold the album art or default album art if no album
     * art exists if the audio player is in the standard audio view.
     */
    private static final JLabel albumArtLabel = new JLabel();

    /**
     * The border width of black borders placed on some ui components.
     */
    private static final int borderWidth = 3;

    /**
     * The file representing the default album art to use if the frame is
     * in the standard audio view and the current audio file has no linked album art.
     */
    private static final File defaultAlbumArt = StaticUtil.getStaticResource("Default.png");

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
     * The audio volume percent label which appears on change of the audio volume.
     */
    private static final CyderLabel audioVolumePercentLabel = new CyderLabel("");

    /**
     * The size of the primary audio control buttons.
     */
    private static final Dimension CONTROL_BUTTON_SIZE = new Dimension(30, 30);

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
    private static final AtomicReference<View> currentView = new AtomicReference<>(View.FULL);

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
     * The audio progress bar animation controller.
     */
    private static final AudioProgressBarAnimator audioProgressBarAnimator
            = new AudioProgressBarAnimator(audioLocationSlider, audioLocationSliderUi);

    /**
     * The stroke for the audio volume and location sliders.
     */
    private static final BasicStroke sliderStroke = new BasicStroke(2.0f);

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
     * The total percent of a completed YouTube audio download.
     */
    private static final float completedProgress = 100.0f;

    /**
     * The switch view mode text.
     */
    private static final String SWITCH_VIEW_MODE = "Switch view mode";

    /**
     * Whether the frame is currently being setup.
     */
    private static final AtomicBoolean settingUpFrame = new AtomicBoolean();

    /**
     * The name of the audio player preliminary handler thread.
     */
    private static final String audioPlayerPreliminaryHandlerThreadName = "AudioPlayer Preliminary Handler";

    /**
     * The getter util instance used to get input from the user
     * for exporting waveform files and for choosing local audio files.
     */
    private static final GetterUtil getterUtil = GetterUtil.getInstance();

    /**
     * The name of the default audio mp3 file and album art png file.
     */
    private static final String defaultAudioFileName = "Logic - Cocaine";

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
     * The actual object that plays audio.
     */
    private static InnerAudioPlayer innerAudioPlayer;

    /**
     * The default album art image.
     */
    private static BufferedImage defaultAlbumArtImage;

    static {
        try {
            defaultAlbumArtImage = ImageUtil.read(defaultAlbumArt);
        } catch (Exception ignored) {}
    }

    /**
     * The audio player frame.
     */
    private static CyderFrame audioPlayerFrame;

    /**
     * The album art directory for the current Cyder user.
     */
    private static File currentUserAlbumArtDir;

    /**
     * The play pause icon button.
     */
    private static JButton playPauseButton;

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
        File userMusicDir = Dynamic.buildDynamic(Dynamic.USERS.getFileName(),
                Console.INSTANCE.getUuid(), UserFile.MUSIC.getName());

        File[] userMusicFiles = userMusicDir.listFiles((dir, name) ->
                FileUtil.isSupportedAudioExtension(OsUtil.buildFile(userMusicDir.getAbsolutePath(), name)));

        if (userMusicFiles != null && userMusicFiles.length > 0) {
            showGui(ArrayUtil.getRandomElement(userMusicFiles));
        } else {
            if (OsUtil.isWindows()) {
                Optional<File> optionalMp3File = AudioUtil.getFirstMp3FileForWindowsUser();
                optionalMp3File.ifPresent(AudioPlayer::showGui);
                if (optionalMp3File.isPresent()) return;
            }

            showGui(cloneDefaultAudioForCurrentUser());
        }
    }

    /**
     * Shows the audio player gui starting with the provided mp3 file.
     * The loading of the widget is performed in a separate thread to avoid freezing the UI thread.
     *
     * @param mp3File the mp3 file to show the audio player frame on
     * @throws NullPointerException     if mp3File is null
     * @throws IllegalArgumentException if mp3File does not exist, or is not a supported audio file
     */
    public static void showGui(File mp3File) {
        checkNotNull(mp3File);
        checkArgument(mp3File.exists());
        checkArgument(FileUtil.isSupportedAudioExtension(mp3File));

        if (settingUpFrame.get()) return;
        settingUpFrame.compareAndSet(false, true);

        CyderThreadFactory threadFactory = new CyderThreadFactory("AudioPlayer loader, mp3File: " + mp3File);
        Executors.newSingleThreadExecutor(threadFactory).submit(() -> {
            currentAudioFile.set(mp3File);
            audioDreamified.set(isCurrentAudioDreamy());

            if (isWidgetOpen()) {
                if (currentView.get() == View.SEARCH) onBackPressedFromSearchView();
                boolean audioPlaying = isAudioPlaying();
                if (audioPlaying) pauseAudio();
                revalidateAfterAudioFileChange();
                innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
                innerAudioPlayer.setLocation(0);
                audioLocationUpdater.setPercentIn(0f);
                audioLocationUpdater.update(false);
                if (audioPlaying) playAudio();
                return;
            }

            cacheAudioLengthsOfCurrentDirectory();

            currentUserAlbumArtDir = Dynamic.buildDynamic(Dynamic.USERS.getFileName(),
                    Console.INSTANCE.getUuid(), UserFile.MUSIC.getName(), UserFile.ALBUM_ART);

            audioPlayerFrame = new CyderFrame(defaultFrameLength, defaultFrameLength, BACKGROUND_COLOR);
            refreshFrameTitle();
            addChangeSizeButtonToTopDragLabel();
            audioPlayerFrame.setMenuType(MenuType.PANEL);
            audioPlayerFrame.setMenuEnabled(true);
            audioPlayerFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    onFrameClosingOrClosed();
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    onFrameClosingOrClosed();
                }
            });
            installFrameMenuItems();

            /*
             All components which will ever be on the frame for the audio player are added now and their sizes set.
             The bounds are set in the view switcher.
             The sizes are almost never set outside the construction below.
             */

            albumArtLabel.setSize(albumArtSize, albumArtSize);
            albumArtLabel.setOpaque(true);
            albumArtLabel.setBackground(BACKGROUND_COLOR);
            albumArtLabel.setBorder(new LineBorder(Color.BLACK, borderWidth));
            audioPlayerFrame.getContentPane().add(albumArtLabel);

            albumArtLabel.add(dreamyLabel);
            dreamyLabel.setSize(albumArtLabel.getSize());
            dreamyLabel.setFont(dreamyLabel.getFont().deriveFont(150f));
            dreamyLabel.setVisible(false);

            audioTitleLabelContainer.setSize(fullRowWidth, fullRowHeight);
            audioTitleLabel.setSize(fullRowWidth, fullRowHeight);
            audioTitleLabel.setText(DEFAULT_AUDIO_TITLE);
            audioTitleLabel.setFont(CyderFonts.DEFAULT_FONT_SMALL);
            audioTitleLabel.setForeground(CyderColors.vanilla);

            audioTitleLabelContainer.add(audioTitleLabel, SwingConstants.CENTER);
            audioPlayerFrame.getContentPane().add(audioTitleLabelContainer);

            shuffleAudioButton.setSize(CONTROL_BUTTON_SIZE);
            audioPlayerFrame.getContentPane().add(shuffleAudioButton);

            lastAudioButton.setSize(CONTROL_BUTTON_SIZE);
            audioPlayerFrame.getContentPane().add(lastAudioButton);

            /*
            Note to maintainers: play pause button is special and is the only one of the primary control buttons
            initialized here. The others are initialized using CyderIconButtons as class level final members.
             */

            playPauseButton = new JButton();
            refreshPlayPauseButtonIcon();
            playPauseButton.setFocusPainted(false);
            playPauseButton.setFocusable(true);
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
            playPauseButton.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    playPauseButton.setIcon(isAudioPlaying() ? pauseIconHover : playIconHover);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    playPauseButton.setIcon(isAudioPlaying() ? pauseIcon : playIcon);
                }
            });
            playPauseButton.setSize(CONTROL_BUTTON_SIZE);
            audioPlayerFrame.getContentPane().add(playPauseButton);

            setupFocusTraversal();

            nextAudioButton.setSize(CONTROL_BUTTON_SIZE);
            audioPlayerFrame.getContentPane().add(nextAudioButton);

            repeatAudioButton.setSize(CONTROL_BUTTON_SIZE);
            audioPlayerFrame.getContentPane().add(repeatAudioButton);

            audioLocationSliderUi.setThumbStroke(sliderStroke);
            audioLocationSliderUi.setThumbShape(ThumbShape.CIRCLE);
            audioLocationSliderUi.setThumbRadius(25);
            audioLocationSliderUi.setThumbFillColor(CyderColors.vanilla);
            audioLocationSliderUi.setThumbOutlineColor(CyderColors.vanilla);
            audioLocationSliderUi.setRightThumbColor(trackNewColor);
            audioLocationSliderUi.setLeftThumbColor(CyderColors.vanilla);
            audioLocationSliderUi.setTrackStroke(sliderStroke);
            audioLocationSliderUi.setAnimationEnabled(true);
            audioLocationSliderUi.setAnimationLen(75);

            audioLocationSlider.setSize(fullRowWidth, fullRowHeight);
            audioLocationSlider.setMinorTickSpacing(1);
            audioLocationSlider.setValue(DEFAULT_LOCATION_SLIDER_VALUE);
            audioLocationSlider.setMinimum(DEFAULT_LOCATION_SLIDER_MIN_VALUE);
            audioLocationSlider.setMaximum(DEFAULT_LOCATION_SLIDER_MAX_VALUE);
            audioLocationSlider.setUI(audioLocationSliderUi);
            audioLocationSlider.setPaintTicks(false);
            audioLocationSlider.setPaintLabels(false);
            audioLocationSlider.setVisible(true);
            audioLocationSlider.addChangeListener(e -> {
                if (audioTotalLength == unknownAudioLength || audioTotalLength == 0) {
                    audioTotalLength = FileUtil.getTotalBytes(currentAudioFile.get());
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
                            float newPercentIn =
                                    (float) audioLocationSlider.getValue() / audioLocationSlider.getMaximum();

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
                            if (audioTotalLength == unknownAudioLength || audioTotalLength == 0) {
                                audioTotalLength = FileUtil.getTotalBytes(currentAudioFile.get());
                            }

                            float newPercentIn =
                                    (float) audioLocationSlider.getValue() / audioLocationSlider.getMaximum();
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

            secondsInLabel.setSize(fullRowWidth / 4, fullRowHeight);
            secondsInLabel.setText("");
            secondsInLabel.setHorizontalAlignment(JLabel.LEFT);
            secondsInLabel.setForeground(CyderColors.vanilla);
            audioPlayerFrame.getContentPane().add(secondsInLabel);
            secondsInLabel.setFocusable(false);

            totalSecondsLabel.setSize(fullRowWidth / 4, fullRowHeight);
            totalSecondsLabel.setText("");
            totalSecondsLabel.setHorizontalAlignment(JLabel.RIGHT);
            totalSecondsLabel.setForeground(CyderColors.vanilla);
            audioPlayerFrame.getContentPane().add(totalSecondsLabel);
            totalSecondsLabel.setFocusable(false);

            audioLocationUpdater = new AudioLocationUpdater(secondsInLabel, totalSecondsLabel, currentView,
                    currentAudioFile, audioLocationSliderPressed, audioLocationSlider);

            audioVolumeSliderUi.setThumbStroke(sliderStroke);
            audioVolumeSliderUi.setThumbShape(ThumbShape.CIRCLE);
            audioVolumeSliderUi.setThumbRadius(25);
            audioVolumeSliderUi.setThumbFillColor(CyderColors.vanilla);
            audioVolumeSliderUi.setThumbOutlineColor(CyderColors.vanilla);
            audioVolumeSliderUi.setRightThumbColor(trackNewColor);
            audioVolumeSliderUi.setLeftThumbColor(CyderColors.vanilla);
            audioVolumeSliderUi.setTrackStroke(sliderStroke);

            audioVolumePercentLabel.setForeground(CyderColors.vanilla);
            audioVolumePercentLabel.setSize(100, 40);
            audioVolumePercentLabel.setVisible(false);
            audioPlayerFrame.getContentPane().add(audioVolumePercentLabel);

            if (audioVolumeLabelAnimator != null) audioVolumeLabelAnimator.kill();
            audioVolumeLabelAnimator = new AudioVolumeLabelAnimator(audioVolumePercentLabel);

            audioVolumeSlider.setSize(fullRowWidth, fullRowHeight);
            audioPlayerFrame.getContentPane().add(audioVolumeSlider);
            audioVolumeSlider.setUI(audioVolumeSliderUi);
            audioVolumeSlider.setMinimum(0);
            audioVolumeSlider.setMaximum(100);
            audioVolumeSlider.setPaintTicks(false);
            audioVolumeSlider.setPaintLabels(false);
            audioVolumeSlider.setVisible(true);
            audioVolumeSlider.setValue(UserDataManager.INSTANCE.getAudioPlayerVolumePercent());
            audioVolumeSlider.addChangeListener(e -> {
                if (uiLocked) return;

                int percent = audioVolumeSlider.getValue();
                UserDataManager.INSTANCE.setAudioPlayerVolumePercent(percent);

                refreshAudioLine();
                audioVolumePercentLabel.setText(percent + "%");
                audioVolumeLabelAnimator.onValueChanged();
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

            setAudioPlayerComponentsVisible(false);
            setupAndShowFrameView(View.FULL);
            audioPlayerFrame.finalizeAndShow();
            Console.INSTANCE.revalidateAudioMenuVisibility();

            if (!AudioUtil.ffmpegInstalled() || !AudioUtil.youTubeDlInstalled()) {
                downloadFfmpegAndYouTubeDl();
            } else {
                settingUpFrame.compareAndSet(true, false);
            }
        });
    }

    /**
     * Adds the change size button to the top drag label.
     */
    private static void addChangeSizeButtonToTopDragLabel() {
        Preconditions.checkState(audioPlayerFrame.getTopDragLabel().getRightButtonList().size() == 3);

        ChangeSizeButton changeSizeButton = new ChangeSizeButton();
        changeSizeButton.setToolTipText(SWITCH_VIEW_MODE);
        changeSizeButton.setClickAction(() -> {
            switch (currentView.get()) {
                case FULL -> setupAndShowFrameView(View.HIDDEN_ALBUM_ART);
                case HIDDEN_ALBUM_ART -> setupAndShowFrameView(View.MINI);
                case MINI -> setupAndShowFrameView(View.FULL);
                case SEARCH -> onBackPressedFromSearchView();
            }
        });
        audioPlayerFrame.getTopDragLabel().addRightButton(changeSizeButton, 1);
    }

    /**
     * The actions to invoke when the frame is closing or closed. Caught via the {@link WindowListener}.
     */
    private static void onFrameClosingOrClosed() {
        // no other pre/post close window Runnables
        // should be added or window listeners
        killAndCloseWidget();
    }

    /**
     * Sets up the focus traversal system for the primary control components.
     */
    private static void setupFocusTraversal() {
        ImmutableList<Component> buttons = audioPlayerFrame.getTopDragLabel().getRightButtonList();
        MinimizeButton minimizeButton = (MinimizeButton) buttons.get(0);
        ChangeSizeButton changeSizeButton = (ChangeSizeButton) buttons.get(1);
        PinButton pinButton = (PinButton) buttons.get(2);
        CloseButton closeButton = (CloseButton) buttons.get(3);

        shuffleAudioButton.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                lastAudioButton.requestFocus();
            }
        });
        lastAudioButton.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                playPauseButton.requestFocus();
            }
        });
        playPauseButton.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                nextAudioButton.requestFocus();
            }
        });
        nextAudioButton.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                repeatAudioButton.requestFocus();
            }
        });
        repeatAudioButton.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                minimizeButton.requestFocus();
            }
        });

        minimizeButton.setFocusPaintable(true);
        changeSizeButton.setFocusPaintable(true);
        pinButton.setFocusPaintable(true);
        closeButton.setFocusPaintable(true);

        minimizeButton.addFocusLostAction(changeSizeButton::requestFocus);
        changeSizeButton.addFocusLostAction(pinButton::requestFocus);
        pinButton.addFocusLostAction(closeButton::requestFocus);
    }

    /**
     * Attempts to download FFmpeg and YouTube-dl for downloading and processing audio files.
     */
    private static void downloadFfmpegAndYouTubeDl() {
        CyderThreadRunner.submit(() -> {
            try {
                lockUi();

                audioPlayerFrame.notify("Attempting to download FFmpeg and YouTube-dl");

                Future<Boolean> passedPreliminaries = attemptToDownloadFfmpegAndYoutubeDl();
                while (!passedPreliminaries.isDone()) Thread.onSpinWait();

                // wait to start playing if downloading
                if (!passedPreliminaries.get()) {
                    audioPlayerFrame.revokeAllNotifications();

                    /*
                    Note to maintainers, cannot cache this because relative to is the frame.
                     */
                    new InformHandler.Builder("Could not download necessary "
                            + "binaries. Try to install both ffmpeg and YouTube-dl and try again")
                            .setTitle("Network Error")
                            .setRelativeTo(audioPlayerFrame)
                            .setPostCloseAction(() -> {
                                killAndCloseWidget();
                                NetworkUtil.openUrl(CyderUrls.FFMPEG_INSTALLATION);
                                NetworkUtil.openUrl(CyderUrls.YOUTUBE_DL_INSTALLATION);
                            }).inform();
                } else {
                    audioPlayerFrame.revokeAllNotifications();
                    unlockUi();
                    audioPlayerFrame.notify("Successfully downloaded necessary binaries");
                    settingUpFrame.compareAndSet(true, false);
                }
            } catch (Exception e) {
                unlockUi();
                settingUpFrame.compareAndSet(true, false);
                ExceptionHandler.handle(e);
            }
        }, audioPlayerPreliminaryHandlerThreadName);
    }

    /**
     * Sets the visibility of all audio player components to the value of visible.
     *
     * @param visible whether to set audio player components to visible
     */
    public static void setAudioPlayerComponentsVisible(boolean visible) {
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

        audioVolumeSlider.setVisible(visible);
        audioLocationSlider.setVisible(visible);
    }

    /**
     * Clones the default audio file and album art to the user's music directory.
     *
     * @return the copied audio file
     * @throws FatalException if copying either of the files fails
     */
    @CanIgnoreReturnValue
    private static File cloneDefaultAudioForCurrentUser() {
        File audioFile = StaticUtil.getStaticResource(defaultAudioFileName + Extension.MP3);
        File audioDirectory = UserFile.MUSIC.getFilePointer();
        File newAudioFile = OsUtil.buildFile(UserFile.MUSIC.getFilePointer()
                .getAbsolutePath(), audioFile.getName());

        File albumArtFile = StaticUtil.getStaticResource(defaultAudioFileName + Extension.PNG);
        File albumArtDirectory = OsUtil.buildFile(audioDirectory.getAbsolutePath(), UserFile.ALBUM_ART);
        File newArtFile = OsUtil.buildFile(albumArtDirectory.getAbsolutePath(), albumArtFile.getName());

        try {
            Files.copy(audioFile.toPath(), newAudioFile.toPath());
            Files.copy(albumArtFile.toPath(), newArtFile.toPath());
        } catch (IOException e) {
            throw new FatalException(e.getMessage());
        }

        return newAudioFile;
    }

    /**
     * The thread factory for the {@link #cacheAudioLengthsOfCurrentDirectory()} method.
     */
    private static final CyderThreadFactory audioLengthsOfCurrentDirectoryCacherThreadFactory =
            new CyderThreadFactory("AudioPlayer neighboring audio files length calculation cacher");

    /**
     * The future task of the {@link #cacheAudioLengthsOfCurrentDirectory()} method.
     */
    private static ListenableFuture<Void> audioLengthsOfCurrentDirectoryCacher;

    /**
     * Starts a new thread to cache the length of all audio files returned by {@link #getValidAudioFiles()}.
     */
    private static void cacheAudioLengthsOfCurrentDirectory() {
        if (audioLengthsOfCurrentDirectoryCacher != null) {
            audioLengthsOfCurrentDirectoryCacher.cancel(true);
        }

        audioLengthsOfCurrentDirectoryCacher = Futures.submit(() -> getValidAudioFiles().forEach(audioFile -> {
            try {
                AudioUtil.getMillisFfprobe(audioFile);
            } catch (Exception ignored) {
                // Don't care in this scenario
            }
        }), Executors.newSingleThreadExecutor(audioLengthsOfCurrentDirectoryCacherThreadFactory));
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

    /**
     * Attempts to download ffmpeg and youtube-dl.
     *
     * @return whether ffmpeg and youtube-dl were downloaded successfully.
     */
    @SuppressWarnings("RedundantIfStatement") /* Readability */
    private static Future<Boolean> attemptToDownloadFfmpegAndYoutubeDl() {
        CyderThreadFactory factory = new CyderThreadFactory("AudioPlayer Preliminary Handler");
        return Executors.newSingleThreadExecutor(factory).submit(() -> {
            if (!AudioUtil.youTubeDlInstalled()) {
                Future<Boolean> downloadedYoutubeDl = AudioUtil.downloadYoutubeDl();
                while (!downloadedYoutubeDl.isDone()) Thread.onSpinWait();
                if (!downloadedYoutubeDl.get()) return false;
            }

            if (!AudioUtil.ffmpegInstalled()) {
                Future<Boolean> ffmpegDownloaded = AudioUtil.downloadFfmpegStack();
                while (!ffmpegDownloaded.isDone()) Thread.onSpinWait();
                if (!ffmpegDownloaded.get()) return false;
            }

            return true;
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

        audioPlayerFrame.addMenuItem("Export wav", AudioPlayer::onExportWavMenuItemPressed);
        audioPlayerFrame.addMenuItem("Export mp3", AudioPlayer::onExportMp3MenuItemPressed);
        audioPlayerFrame.addMenuItem("Waveform", AudioPlayer::onWaveformExporterMenuItemPressed);
        audioPlayerFrame.addMenuItem("Search", AudioPlayer::onSearchMenuItemPressed, onSearchView);
        audioPlayerFrame.addMenuItem("Local Audio", AudioPlayer::onLocalAudioFileMenuItemPressed);
        audioPlayerFrame.addMenuItem("Dreamify", AudioPlayer::onDreamifyMenuItemPressed, audioDreamified);
    }

    /**
     * The menu item to export the current audio as a wav.
     */
    private static void onExportWavMenuItemPressed() {
        if (wavExporterLocked.get() || uiLocked) return;

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
    }

    /**
     * The menu item for exporting the current audio as an mp3.
     */
    private static void onExportMp3MenuItemPressed() {
        if (mp3ExporterLocked.get() || uiLocked) return;

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
    }

    /**
     * The logic for the export waveform menu option.
     */
    private static void onWaveformExporterMenuItemPressed() {
        if (waveformExporterLocked.get() || uiLocked) return;

        CyderThreadRunner.submit(() -> {
            getterUtil.closeAllGetInputFrames();
            Optional<String> optionalSaveName = getterUtil.getInput(
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
                        saveName + Extension.PNG.getExtension());

                Future<BufferedImage> waveform = MessagingUtil.generateLargeWaveform(currentAudioFile.get());

                waveformExporterLocked.set(true);

                while (!waveform.isDone()) {
                    Thread.onSpinWait();
                }

                waveformExporterLocked.set(false);

                try {
                    ImageIO.write(waveform.get(), Extension.PNG.getExtensionWithoutPeriod(),
                            saveFile.getAbsoluteFile());
                    audioPlayerFrame.notify(new NotificationBuilder("Saved waveform to your files directory")
                            .setOnKillAction(() -> ImageViewer.getInstance(saveFile).showGui()));
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                    audioPlayerFrame.notify("Could not save waveform at this time");
                }
            } else {
                audioPlayerFrame.notify("Invalid filename for " + OsUtil.OPERATING_SYSTEM_NAME);
            }

        }, "AudioPlayer Waveform Exporter");
    }

    /**
     * The menu item for searching YouTube for songs.
     */
    private static void onSearchMenuItemPressed() {
        if (onSearchView.get()) {
            onBackPressedFromSearchView();
        } else {
            onSearchView.set(true);
            constructSearchYouTubeView();
        }
    }

    /**
     * The menu item for choosing a local audio file.
     */
    private static void onLocalAudioFileMenuItemPressed() {
        if (chooseFileLocked.get() || uiLocked) return;

        CyderThreadRunner.submit(() -> {
            chooseFileLocked.set(true);
            getterUtil.closeAllGetFileFrames();

            GetFileBuilder builder = new GetFileBuilder("Local audio file chooser")
                    .setAllowFolderSubmission(false)
                    .setAllowFileSubmission(true)
                    .setAllowableFileExtensions(ImmutableList.of(Extension.MP3.getExtension()))
                    .setRelativeTo(audioPlayerFrame);

            Optional<File> optionalFile = getterUtil.getFile(builder);
            if (optionalFile.isEmpty()) return;
            File chosenFile = optionalFile.get();
            boolean differentDirectory = !chosenFile.getParentFile().equals(getCurrentAudio().getParentFile());
            chooseFileLocked.set(false);
            if (differentDirectory) cacheAudioLengthsOfCurrentDirectory();
            lastAction = LastAction.FileChosen;
            if (currentView.get() == View.SEARCH) onBackPressedFromSearchView();
            boolean audioPlaying = isAudioPlaying();
            if (audioPlaying) pauseAudio();
            currentAudioFile.set(chosenFile);
            revalidateAfterAudioFileChange();
            if (audioPlaying) playAudio();
        }, "AudioPlayer Local File Chooser");
    }

    /**
     * A callback used when dreamifying an audio file finishes.
     *
     * @param dreamyAudio the dreamified audio file to play
     */
    private static void dreamyAudioCallback(File dreamyAudio) {
        float percentIn = (float) audioLocationSlider.getValue() / audioLocationSlider.getMaximum();
        boolean audioPlaying = isAudioPlaying();
        if (audioPlaying) pauseAudio();

        currentAudioFile.set(dreamyAudio);

        revalidateAfterAudioFileChange();

        innerAudioPlayer = new InnerAudioPlayer(dreamyAudio);
        innerAudioPlayer.setLocation((long) (percentIn * FileUtil.getTotalBytes(dreamyAudio)));
        audioLocationUpdater.setPercentIn(percentIn);
        audioLocationUpdater.update(false);

        if (audioPlaying) playAudio();
        dreamifierLocked.set(false);
        audioPlayerFrame.notify("Successfully dreamified audio");
    }

    /**
     * The item menu to toggle between dreamify states of an audio file.
     */
    private static void onDreamifyMenuItemPressed() {
        if (dreamifierLocked.get() || uiLocked) return;
        if (currentAudioFile.get() == null) return;

        String currentAudioFilename = FileUtil.getFilename(currentAudioFile.get());

        if (currentAudioFilename.endsWith(AudioUtil.DREAMY_SUFFIX)) {
            String nonDreamyName = currentAudioFilename.substring(0,
                    currentAudioFilename.length() - AudioUtil.DREAMY_SUFFIX.length());

            if (!attemptToFindAndPlayAudioFileWithName(nonDreamyName))
                audioPlayerFrame.notify("Could not find audio's non-dreamy equivalent");
            return;
        }

        File userMusicDir = Dynamic.buildDynamic(Dynamic.USERS.getFileName(),
                Console.INSTANCE.getUuid(), UserFile.MUSIC.getName());

        String dreamyAudioFileName = currentAudioFilename + AudioUtil.DREAMY_SUFFIX;
        if (userMusicDir.exists() && attemptToFindAndPlayAudioFileWithName(dreamyAudioFileName)) return;

        String threadName = "Audio Dreamifier: " + currentAudioFilename;
        CyderThreadRunner.submit(() -> {
            audioPlayerFrame.notify(new NotificationBuilder("Dreamifying"
                    + CyderStrings.space + CyderStrings.quote + FileUtil.getFilename(currentAudioFile.get())
                    + CyderStrings.quote).setViewDuration(10000));
            dreamifierLocked.set(true);

            Future<Optional<File>> dreamifiedAudioFuture = AudioUtil.dreamifyAudio(currentAudioFile.get());
            while (!dreamifiedAudioFuture.isDone()) Thread.onSpinWait();
            Optional<File> dreamifiedAudio = Optional.empty();

            try {
                dreamifiedAudio = dreamifiedAudioFuture.get();
            } catch (Exception ignored) {}

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
        }, threadName);
    }

    /**
     * Attempts to find the audio file in the current directory with the provided name.
     * If found, and if audio was playing, this audio file is played
     *
     * @param audioFileName the name of the audio file to look for
     * @return whether the audio was found and handled
     */
    private static boolean attemptToFindAndPlayAudioFileWithName(String audioFileName) {
        Optional<File> optionalNonDreamyAudioFile = getValidAudioFiles().stream()
                .filter(validAudioFile -> FileUtil.getFilename(validAudioFile).equals(audioFileName))
                .findFirst();
        if (optionalNonDreamyAudioFile.isPresent()) {
            File nonDreamyAudioFile = optionalNonDreamyAudioFile.get();

            float percentIn = (float) audioLocationSlider.getValue() / audioLocationSlider.getMaximum();
            boolean audioPlaying = isAudioPlaying();
            if (audioPlaying) pauseAudio();

            currentAudioFile.set(nonDreamyAudioFile);
            revalidateAfterAudioFileChange();

            innerAudioPlayer = new InnerAudioPlayer(nonDreamyAudioFile);
            innerAudioPlayer.setLocation((long) (percentIn * FileUtil.getTotalBytes(nonDreamyAudioFile)));
            audioLocationUpdater.setPercentIn(percentIn);
            audioLocationUpdater.update(false);

            if (audioPlaying) playAudio();
            audioDreamified.set(isCurrentAudioDreamy());
            audioPlayerFrame.revalidateMenu();
        }

        return optionalNonDreamyAudioFile.isPresent();
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
    private static final int primaryButtonSpacing = (int) ((1.5 * albumArtSize - 5 * 30) / 6);

    /**
     * Sets component visibilities and locations based on the provided frame view.
     *
     * @param view the requested frame view
     */
    private static void setupAndShowFrameView(View view) {
        setAudioPlayerComponentsVisible(false);
        setYouTubeSearchViewComponentsVisible(false);

        switch (view) {
            case FULL -> {
                setAudioPlayerComponentsVisible(true);
                currentView.set(View.FULL);
                audioPlayerFrame.setSize(defaultFrameLength, defaultFrameLength);

                // set location of all components needed
                int xOff = defaultFrameLength / 2 - albumArtSize / 2;
                int yOff = CyderDragLabel.DEFAULT_HEIGHT;
                yOff += 20;
                albumArtLabel.setLocation(xOff, yOff);
                yOff += albumArtSize + yComponentPadding;
                refreshAlbumArt();

                // xOff of rest of components is len s.t. the total
                // width is 1.5x the width of the  album art label
                xOff = (int) (defaultFrameLength / 2 - (1.5 * albumArtSize) / 2);
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
                totalSecondsLabel.setLocation(xOff + fullRowWidth - fullRowWidth / 4, yOff + 20);
                audioVolumePercentLabel.setLocation(defaultFrameLength / 2 - audioVolumePercentLabel.getWidth() / 2,
                        yOff + 35);
                yOff += 40 + yComponentPadding;
                audioVolumeSlider.setLocation(xOff, yOff);
            }
            case HIDDEN_ALBUM_ART -> {
                setAudioPlayerComponentsVisible(true);
                audioPlayerFrame.setSize(defaultFrameLength, defaultFrameLength
                        - albumArtSize - hiddenArtHeightOffset);
                albumArtLabel.setVisible(false);
                albumArtLabel.setBorder(null);

                // set location of all components needed
                int xOff;
                int yOff = CyderDragLabel.DEFAULT_HEIGHT + 10;

                // xOff of rest of components is s.t. the total width is 1.5x width of album art label
                xOff = (int) (defaultFrameLength / 2 - (1.5 * albumArtSize) / 2);
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
                totalSecondsLabel.setLocation(xOff + fullRowWidth - fullRowWidth / 4, yOff + 20);

                audioVolumePercentLabel.setLocation(
                        defaultFrameLength / 2 - audioVolumePercentLabel.getWidth() / 2, yOff + 35);
                yOff += 40 + yComponentPadding;
                audioVolumeSlider.setLocation(xOff, yOff);
                currentView.set(View.HIDDEN_ALBUM_ART);
            }
            case MINI -> {
                currentView.set(View.MINI);
                setAudioPlayerComponentsVisible(true);
                audioPlayerFrame.setSize(defaultFrameLength, defaultFrameLength
                        - albumArtSize - miniFrameHeightOffset);
                albumArtLabel.setVisible(false);
                albumArtLabel.setBorder(null);

                // set location of all components needed
                int xOff;
                int yOff = CyderDragLabel.DEFAULT_HEIGHT + 10;

                // xOff of rest of components is s.t. the total width is 1.5x width of album art label
                xOff = (int) (defaultFrameLength / 2 - (1.5 * albumArtSize) / 2);
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
        File audioFile = currentAudioFile.get();
        audioPlayerFrame.setTitle(audioFile == null ? DEFAULT_FRAME_TITLE : getDisplayNameForAudio(audioFile));
    }

    /**
     * Returns the name for the provided audio file to display to the user
     * both as the audio frame title and on the scrolling audio label.
     *
     * @param audioFile the audio file
     * @return the display name for the audio file
     */
    public static String getDisplayNameForAudio(File audioFile) {
        String name = isAudioFileDreamy(audioFile)
                ? getUserReadableNameForDreamyAudio(audioFile)
                : FileUtil.getFilename(audioFile);
        return StringUtil.capsFirstWord(StringUtil.getTrimmedText(name));
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
        if (currentView.get() != View.FULL) {
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
                customAlbumArt = new ImageIcon(ImageUtil.read(defaultAlbumArt));
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        ImageIcon regularIcon = ImageUtil.ensureFitsInBounds(customAlbumArt,
                new Dimension(albumArtSize, albumArtSize));
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

        Console.INSTANCE.revalidateConsoleTaskbarMenu();
    }

    /**
     * Returns whether the current audio file is a dreamy audio file.
     *
     * @return whether the current audio file is a dreamy audio file
     */
    private static boolean isCurrentAudioDreamy() {
        return isAudioFileDreamy(currentAudioFile.get());
    }

    /**
     * Returns whether the provided audio file is dreamy.
     *
     * @param audioFile the audio file
     * @return whether the provided audio file is dreamy
     */
    private static boolean isAudioFileDreamy(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(audioFile));

        return FileUtil.getFilename(audioFile).endsWith(AudioUtil.DREAMY_SUFFIX);
    }

    /**
     * Returns the user readable version for the provided dreamy audio file.
     *
     * @param audioFile the audio file
     * @return the user readable version for the provided dreamy audio file
     */
    private static String getUserReadableNameForDreamyAudio(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(audioFile));
        Preconditions.checkArgument(isAudioFileDreamy(audioFile));

        String name = FileUtil.getFilename(audioFile);
        return name.substring(0, name.length() - AudioUtil.DREAMY_SUFFIX.length()) + CyderStrings.space + "(dreamy)";
    }

    /**
     * Terminates the current ScrollingTitleLabel object controlling the title label
     * in the title label container and creates a new instance based on the current audio file's title.
     */
    static void refreshAudioTitleLabel() {
        String text = getDisplayNameForAudio(currentAudioFile.get());

        // end old object
        if (scrollingTitleLabel != null) {
            // if the same title then do not update
            if (scrollingTitleLabel.localTitle().equals(text)) return;
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
        if (currentView.get() == View.MINI) {
            return;
        }

        if (audioLocationUpdater != null) {
            audioLocationUpdater.kill();
        }

        audioLocationUpdater = new AudioLocationUpdater(secondsInLabel, totalSecondsLabel, currentView,
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

            pauseLocation = unknownPauseLocation;
            pauseLocationMillis = unknownPauseLocation;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * A call back for {@link InnerAudioPlayer}s to invoke when they naturally conclude and are not killed.
     */
    static void playAudioCallback() {
        if (lastAction != LastAction.Play) return;

        if (innerAudioPlayer != null) {
            innerAudioPlayer.kill();
            innerAudioPlayer = null;
        }

        if (repeatAudio.get()) {
            innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
            audioLocationUpdater.setPercentIn(0);
            audioLocationUpdater.update(false);
            playAudio();
        } else if (!audioFileQueue.isEmpty()) {
            currentAudioFile.set(audioFileQueue.remove(0));
            innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
            revalidateAfterAudioFileChange();
            playAudio();
        } else if (shuffleAudio.get()) {
            currentAudioFile.set(getValidAudioFiles().get(getRandomAudioIndex()));
            innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
            revalidateAfterAudioFileChange();
            playAudio();
        } else {
            int currentIndex = getCurrentAudioIndex();
            int nextIndex = currentIndex + 1 == getValidAudioFiles().size() ? 0 : currentIndex + 1;
            currentAudioFile.set(getValidAudioFiles().get(nextIndex));
            innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
            revalidateAfterAudioFileChange();
            playAudio();
        }

    }

    /*
     * The location the previous InnerAudioPlayer was killed at, if available.
     */
    private static long pauseLocation;

    /**
     * The location in milliseconds the previous InnerAudioPlayer was paused at.
     */
    private static long pauseLocationMillis;

    /**
     * A magic number to denote an undefined pause location.
     */
    private static final long unknownPauseLocation = -1L;

    /**
     * A magic number used to denote an unknown audio length;
     */
    private static final long unknownAudioLength = -1;

    /**
     * The total length of the current (paused or playing) audio.
     */
    private static long audioTotalLength = unknownAudioLength;

    /**
     * Returns the location in milliseconds into the current audio file.
     *
     * @return the location in milliseconds into the current audio file
     */
    public static long getMillisecondsIn() {
        return innerAudioPlayer != null ? innerAudioPlayer.getMillisecondsIn() : pauseLocationMillis;
    }

    /**
     * Pauses playback of the current audio file if {@link #innerAudioPlayer} is not null.
     */
    private static void pauseAudio() {
        if (innerAudioPlayer == null) return;

        audioTotalLength = innerAudioPlayer.getTotalAudioLength();
        pauseLocationMillis = innerAudioPlayer.getMillisecondsIn();
        audioProgressBarAnimator.setState(AudioProgressBarAnimator.State.PAUSED);
        pauseLocation = innerAudioPlayer.kill();
        innerAudioPlayer = null;
        lastAction = LastAction.Pause;
        audioLocationUpdater.pauseTimer();
        refreshPlayPauseButtonIcon();
    }

    /**
     * The range a pause location must fall within in order for a skip back action to occur. Any value outside
     * of the first 5000ms will cause the current audio to be restarted on a skip back invocation.
     */
    private static final Range<Long> millisecondsIntoAudioToSkipBack = Range.closed(0L, 5000L);

    /**
     * Handles a click from the last button.
     */
    public static void handleLastAudioButtonClick() {
        if (shouldSuppressClick()) return;
        checkNotNull(currentAudioFile);
        checkArgument(!uiLocked);

        lastAction = LastAction.Skip;

        boolean wasPlayingAudio = isAudioPlaying();
        if (wasPlayingAudio) pauseAudio();

        if (!millisecondsIntoAudioToSkipBack.contains(pauseLocationMillis)) {
            audioLocationUpdater.pauseTimer();
            audioLocationUpdater.setPercentIn(0);
            innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
            audioLocationUpdater.update(false);
        } else {
            int currentIndex = getCurrentAudioIndex();
            int lastIndex = currentIndex == 0 ? getValidAudioFiles().size() - 1 : currentIndex - 1;

            currentAudioFile.set(getValidAudioFiles().get(lastIndex));

            innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
            audioLocationUpdater.update(false);
            audioLocationSliderUi.resetAnimation();
            revalidateAfterAudioFileChange();
        }

        if (wasPlayingAudio) playAudio();
    }

    /**
     * Handles a click from the next audio button.
     */
    public static void handleNextAudioButtonClick() {
        if (shouldSuppressClick()) return;
        checkNotNull(currentAudioFile);
        checkArgument(!uiLocked);

        lastAction = LastAction.Skip;

        boolean shouldPlay = isAudioPlaying();

        pauseAudio();

        int currentIndex = getCurrentAudioIndex();
        int nextIndex = currentIndex == getValidAudioFiles().size() - 1 ? 0 : currentIndex + 1;

        if (shuffleAudio.get()) {
            nextIndex = getRandomAudioIndex();
        }

        currentAudioFile.set(getValidAudioFiles().get(nextIndex));
        innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
        audioLocationSliderUi.resetAnimation();

        revalidateAfterAudioFileChange();

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
    private static final AtomicBoolean repeatAudio = new AtomicBoolean(false);

    /**
     * Handles a click from the repeat button.
     */
    public static void handleRepeatButtonClick() {
        checkNotNull(currentAudioFile);
        checkArgument(!uiLocked);

        boolean repeatAudioValue = repeatAudio.get();
        repeatAudio.compareAndSet(repeatAudioValue, !repeatAudioValue);
    }

    /**
     * Whether the next audio file should be chosen at random upon completion of the current audio file.
     */
    private static final AtomicBoolean shuffleAudio = new AtomicBoolean(false);

    /**
     * Handles a click of the shuffle button.
     */
    public static void handleShuffleButtonClick() {
        checkNotNull(currentAudioFile);
        checkArgument(!uiLocked);

        boolean shuffleAudioValue = shuffleAudio.get();
        shuffleAudio.compareAndSet(shuffleAudioValue, !shuffleAudioValue);
    }

    /**
     * Adds the audio to the beginning of the audio file queue.
     *
     * @param audioFile the audio file to add to the beginning of the queue
     */
    public static void playAudioNext(File audioFile) {
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
    public static void playAudioLast(File audioFile) {
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
        if (isAudioPlaying()) pauseAudio();

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
    }

    /**
     * The time in ms to delay primary ui interactions, that of play, pause, and skip.
     */
    private static final int primaryActionThrottleDelay = 50;

    /**
     * The last time a ui action was permitted.
     */
    private static Instant lastActionTime = Instant.ofEpochMilli(0);

    /**
     * Returns whether to suppress a requested ui action such as a button click.
     *
     * @return whether to suppress a requested ui action such as a button click
     */
    private static boolean shouldSuppressClick() {
        Instant now = Instant.now();
        if (now.minusMillis(lastActionTime.toEpochMilli()).toEpochMilli() >= primaryActionThrottleDelay) {
            lastActionTime = now;
            return false;
        } else {
            return true;
        }
    }

    /**
     * Revalidates necessary components following an audio file change.
     */
    private static void revalidateAfterAudioFileChange() {
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
    private static int getRandomAudioIndex() {
        return NumberUtil.getRandomIndex(0, getValidAudioFiles().size(), getCurrentAudioIndex());
    }

    // ----------------------------------
    // Search View components and methods
    // ----------------------------------

    private static final AtomicBoolean searchYouTubeViewLocked = new AtomicBoolean(false);

    /**
     * The list of search results previously found.
     */
    private static final LinkedList<YoutubeSearchResult> searchResults = new LinkedList<>();

    /**
     * The length of the thumbnails printed to the search view {@link #searchResultsPane}.
     */
    private static final int searchViewThumbnailLength = 250;

    /**
     * The default information label text.
     */
    private static final String DEFAULT_INFORMATION_LABEL_TEXT = "Search YouTube using the above field";

    /**
     * The color used as the background for the search results scroll and information label.
     */
    private static final Color searchViewScrollBackground = new Color(30, 30, 30);

    /**
     * The width of the search view components excluding the scroll pane.
     */
    private static final int searchViewComponentWidth = 300;

    /**
     * The text pane used to display YouTube search results.
     */
    private static JTextPane searchResultsPane;

    /**
     * The printing util used for printing out search results to the scroll pane.
     */
    private static StringUtil searchResultsPrintingUtil;

    /**
     * The scroll pane for the search results pane.
     */
    private static CyderScrollPane searchResultsScroll;

    /**
     * The search button for the search view two.
     */
    private static CyderButton searchViewSearchButton;

    /**
     * The button used to go back to the main audio page.
     */
    private static CyderButton searchBackButton;

    /**
     * The information label placed in the center of the search pane for displaying progress when a search is underway.
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
     * Performs operations necessary to transitioning from the search view to the {@link View#FULL} view.
     */
    private static void onBackPressedFromSearchView() {
        previousScrollLocation = searchResultsScroll.getVerticalScrollBar().getValue();
        onSearchView.set(false);
        setYouTubeSearchViewComponentsVisible(false);
        audioPlayerFrame.hideMenu();
        setupAndShowFrameView(View.FULL);
    }

    /**
     * Constructs the search view where a user can search for and download audio from YouTube.
     */
    private static void constructSearchYouTubeView() {
        if (uiLocked || searchYouTubeViewLocked.get()) return;

        currentView.set(View.SEARCH);

        searchYouTubeViewLocked.set(true);

        setAudioPlayerComponentsVisible(false);
        audioVolumePercentLabel.setVisible(false);

        audioPlayerFrame.hideMenu();

        int yOff = 50;

        audioPlayerFrame.setSize(defaultFrameLength, defaultFrameLength);

        searchField = new CyderModernTextField();
        searchField.setHorizontalAlignment(JTextField.CENTER);
        searchField.setFont(CyderFonts.DEFAULT_FONT_SMALL);
        searchField.setForeground(CyderColors.vanilla);
        searchField.setUnderlineColor(CyderColors.vanilla);
        searchField.setRippleColor(CyderColors.regularPurple);
        searchField.setCaret(new CyderCaret(CyderColors.vanilla));
        searchField.setBackground(BACKGROUND_COLOR);
        searchField.setBounds((audioPlayerFrame.getWidth() - searchViewComponentWidth) / 2, yOff,
                searchViewComponentWidth, 40);
        searchField.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, CyderColors.vanilla));
        audioPlayerFrame.getContentPane().add(searchField);

        yOff += 50;

        searchViewSearchButton = new CyderButton("Search");
        searchViewSearchButton.setBorder(BorderFactory.createEmptyBorder());
        searchViewSearchButton.setBackground(CyderColors.regularPurple);
        searchViewSearchButton.setForeground(CyderColors.vanilla);
        searchViewSearchButton.setFont(CyderFonts.DEFAULT_FONT);
        searchViewSearchButton.setBounds((audioPlayerFrame.getWidth() - searchViewComponentWidth) / 2 + 50, yOff,
                searchViewComponentWidth - 50, 40);
        audioPlayerFrame.getContentPane().add(searchViewSearchButton);
        searchField.addActionListener(e -> searchAndUpdate());
        searchViewSearchButton.addActionListener(e -> searchAndUpdate());

        searchBackButton = new CyderButton(" < ");
        searchBackButton.setBorder(BorderFactory.createEmptyBorder());
        searchBackButton.setBackground(CyderColors.regularPurple);
        searchBackButton.setToolTipText("Back");
        searchBackButton.setForeground(CyderColors.vanilla);
        searchBackButton.setFont(CyderFonts.DEFAULT_FONT);
        searchBackButton.setBounds((audioPlayerFrame.getWidth() - searchViewComponentWidth) / 2, yOff, 40, 40);
        audioPlayerFrame.getContentPane().add(searchBackButton);
        searchBackButton.addActionListener(e -> onBackPressedFromSearchView());

        yOff += 60;

        searchResultsPane = new JTextPane();
        searchResultsPane.setEditable(false);
        searchResultsPane.setAutoscrolls(false);
        searchResultsPane.setBounds((audioPlayerFrame.getWidth() - fullRowWidth) / 2,
                yOff, fullRowWidth, audioPlayerFrame.getWidth() - 20 - yOff);
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
        searchResultsScroll.setBounds((audioPlayerFrame.getWidth() - fullRowWidth) / 2,
                yOff, fullRowWidth, audioPlayerFrame.getWidth() - 20 - yOff);
        searchResultsScroll.setBackground(searchViewScrollBackground);

        informationLabel = new CyderLabel();
        informationLabel.setForeground(CyderColors.vanilla);
        informationLabel.setFont(CyderFonts.DEFAULT_FONT);
        informationLabel.setBackground(searchViewScrollBackground);
        informationLabel.setOpaque(true);
        informationLabel.setBorder(new LineBorder(Color.black, 4));
        informationLabel.setBounds((audioPlayerFrame.getWidth() - fullRowWidth) / 2,
                yOff, fullRowWidth, audioPlayerFrame.getWidth() - 20 - yOff);
        audioPlayerFrame.getContentPane().add(informationLabel);

        audioPlayerFrame.getContentPane().add(searchResultsScroll);
        searchResultsPane.revalidate();

        if (lastSearchResultsPage != null) {
            searchResultsPane.setDocument(lastSearchResultsPage);
            searchField.setText(previousSearch);
            hideInformationLabel();

            /*
            Note to maintainers: this is a necessary bodge for the intended functionality to work.
             */
            searchResultsScroll.getVerticalScrollBar().setValue(previousScrollLocation);
            searchResultsScroll.getVerticalScrollBar().setValue(previousScrollLocation);
        } else {
            showInformationLabel(DEFAULT_INFORMATION_LABEL_TEXT);
            searchResultsScroll.getVerticalScrollBar().setValue(0);
        }

        searchResultsPrintingUtil = new StringUtil(new CyderOutputPane(searchResultsPane));

        searchYouTubeViewLocked.set(false);
    }

    /**
     * Sets the YouTube search view components to the visibility specified.
     *
     * @param visible whether the YouTube search view components should be visible
     */
    @SuppressWarnings("SameParameterValue")
    private static void setYouTubeSearchViewComponentsVisible(boolean visible) {
        if (searchField == null) return;
        searchField.setVisible(visible);
        searchViewSearchButton.setVisible(visible);
        searchBackButton.setVisible(visible);
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
     * The number of search results to grab when searching YouTube.
     */
    private static final int numSearchResults = 10;

    /**
     * A search result object to hold data in the results scroll pane.
     */
    private static record YoutubeSearchResult(String uuid,
                                              String title,
                                              String description,
                                              String channel,
                                              BufferedImage bi) {}

    /**
     * The alignment object used for menu alignment.
     */
    private static final SimpleAttributeSet alignment = new SimpleAttributeSet();

    /**
     * The string used for the information label when a YouTube query is triggered.
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
     * The formatting results string.
     */
    private static final String FORMATTING_RESULTS = "Formatting results...";

    /**
     * The last search result output.
     */
    private static Document lastSearchResultsPage;

    /**
     * Searches YouTube for the provided text and updates the results pane with videos found.
     */
    private static void searchAndUpdate() {
        String rawFieldText = searchField.getText();
        if (StringUtil.isNullOrEmpty(rawFieldText) || rawFieldText.equalsIgnoreCase(previousSearch)) return;

        // Trim and replace multiple spaces with one
        String fieldText = rawFieldText.trim().replaceAll(CyderRegexPatterns.whiteSpaceRegex, CyderStrings.space);
        previousSearch = fieldText;

        String threadName = "YouTube Searcher, search: " + CyderStrings.quote + fieldText + CyderStrings.quote;
        CyderThreadRunner.submit(() -> {
            showInformationLabel(SEARCHING);

            Optional<YouTubeSearchResultPage> youTubeSearchResultPage =
                    getSearchResults(YouTubeUtil.buildYouTubeApiV3SearchQuery(numSearchResults, fieldText));

            if (youTubeSearchResultPage.isEmpty()) {
                showInformationLabel(NO_RESULTS);
                return;
            }

            searchResults.clear();

            for (YouTubeVideo video : youTubeSearchResultPage.get().getItems()) {
                BufferedImage image = ImageUtil.ensureFitsInBounds(
                        YouTubeUtil.getMaxResolutionSquareThumbnail(video.getId().getVideoId())
                                .orElse(defaultAlbumArtImage),
                        new Dimension(albumArtSize, albumArtSize));

                searchResults.add(new YoutubeSearchResult(
                        video.getId().getVideoId(),
                        video.getSnippet().getTitle(),
                        video.getSnippet().getDescription(),
                        video.getSnippet().getChannelTitle(),
                        image));
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

            searchResults.forEach(result -> {
                Optional<File> alreadyExistsOptional = AudioUtil.getCurrentUserMusicFileWithName(result.title);
                boolean alreadyExists = alreadyExistsOptional.isPresent();

                printSearchResultToPane(result);

                String url = YouTubeUtil.buildVideoUrl(result.uuid);
                YouTubeAudioDownload youTubeAudioDownload = new YouTubeAudioDownload();
                youTubeAudioDownload.setVideoLink(url);
                AtomicReference<YouTubeAudioDownload> downloadable = new AtomicReference<>(youTubeAudioDownload);
                AtomicBoolean mouseEntered = new AtomicBoolean(false);

                CyderButton downloadButton = new CyderButton();
                downloadButton.setLeftTextPadding(StringUtil.generateSpaces(5));
                downloadButton.setRightTextPadding(StringUtil.generateSpaces(4));
                downloadButton.setText(alreadyExists ? PLAY : DOWNLOAD);
                downloadButton.setBackground(CyderColors.regularPurple);
                downloadButton.setForeground(CyderColors.vanilla);
                downloadButton.setBorder(BorderFactory.createEmptyBorder());
                downloadButton.setFont(CyderFonts.DEFAULT_FONT.deriveFont(26f));
                downloadButton.setSize(searchViewComponentWidth, 40);
                downloadButton.addActionListener(e -> CyderThreadRunner.submit(() -> {
                    if (downloadable.get().isDownloading()) {
                        downloadable.get().cancel();
                    } else if (alreadyExists) {
                        onPlayDownloadedAudioPressedFromSearchView(alreadyExistsOptional.get());
                    } else if (downloadable.get().isDownloaded()) {
                        onPlayDownloadedAudioPressedFromSearchView(downloadable.get().getAudioDownloadFile());
                    } else {
                        if (downloadable.get().isCanceled()) {
                            downloadable.set(new YouTubeAudioDownload());
                            downloadable.get().setVideoLink(url);
                            downloadable.get().setOnCanceledCallback(() -> downloadButton.setText(DOWNLOAD));
                            downloadable.get().setOnDownloadedCallback(() -> {
                                downloadButton.setText(PLAY);
                                downloadButton.addActionListener(event -> onPlayDownloadedAudioPressedFromSearchView(
                                        downloadable.get().getAudioDownloadFile()));
                            });
                        }

                        downloadable.get().downloadAudioAndThumbnail();
                        startDownloadUpdater(downloadable, downloadButton, mouseEntered);
                    }
                }, "AudioPlayer search downloader, audio: " + result.title()));
                downloadButton.addMouseListener(generateDownloadButtonMouseListener(downloadable,
                        mouseEntered, downloadButton, alreadyExists));
                downloadable.get().setOnCanceledCallback(() -> downloadButton.setText(DOWNLOAD));
                downloadable.get().setOnDownloadedCallback(() -> {
                    downloadButton.setText(PLAY);
                    downloadButton.addActionListener(e ->
                            onPlayDownloadedAudioPressedFromSearchView(downloadable.get().getAudioDownloadFile()));
                });

                searchResultsPrintingUtil.printlnComponent(downloadButton);
                searchResultsPrintingUtil.println(CyderStrings.newline);
            });

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
    private static void onPlayDownloadedAudioPressedFromSearchView(File audio) {
        if (isAudioPlaying()) pauseAudio();

        currentAudioFile.set(audio);
        innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
        revalidateAfterAudioFileChange();
        onBackPressedFromSearchView();

        playAudio();
    }

    /**
     * Constructs and prints the title and channel labels for the provided YouTube search result.
     *
     * @param result the YouTube search result record
     */
    private static void printSearchResultToPane(YoutubeSearchResult result) {
        JLabel imageLabel = new JLabel(ImageUtil.toImageIcon(result.bi));
        imageLabel.setSize(searchViewThumbnailLength, searchViewThumbnailLength);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setBorder(new LineBorder(Color.black, 4));
        searchResultsPrintingUtil.printlnComponent(imageLabel);

        searchResultsPrintingUtil.println(CyderStrings.newline);

        CyderLabel titleLabel = new CyderLabel(result.title);
        titleLabel.setForeground(CyderColors.vanilla);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        searchResultsPrintingUtil.printlnComponent(titleLabel);

        CyderLabel channelLabel = new CyderLabel(result.channel);
        channelLabel.setForeground(CyderColors.vanilla);
        channelLabel.setHorizontalAlignment(JLabel.CENTER);
        searchResultsPrintingUtil.printlnComponent(channelLabel);

        searchResultsPrintingUtil.println(CyderStrings.newline);
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
            AtomicReference<YouTubeAudioDownload> downloadable,
            AtomicBoolean mouseEntered,
            CyderButton downloadButton,
            boolean alreadyExists) {
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
     * @param downloadable   the YouTube download
     * @param downloadButton the download button
     * @param mouseEntered   whether the mouse is currently in the button
     */
    private static void startDownloadUpdater(AtomicReference<YouTubeAudioDownload> downloadable,
                                             CyderButton downloadButton,
                                             AtomicBoolean mouseEntered) {
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

                ThreadUtil.sleep(YouTubeConstants.DOWNLOAD_UPDATE_DELAY);
            }
        }, threadName);
    }

    /**
     * Returns the search results for a particular url query.
     *
     * @param url the constructed url to get YouTube video results
     * @return the YoutubeSearchResultPage object if present, empty optional else
     */
    private static Optional<YouTubeSearchResultPage> getSearchResults(String url) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            return Optional.of(SerializationUtil.fromJson(reader, YouTubeSearchResultPage.class));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Optional.empty();
    }
}