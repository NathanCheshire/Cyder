package cyder.handlers.external.audio;

import cyder.annotations.CyderAuthor;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.enums.CyderInspection;
import cyder.enums.DynamicDirectory;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.external.PhotoViewer;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.messaging.MessagingUtils;
import cyder.threads.CyderThreadFactory;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.user.UserFile;
import cyder.utilities.*;
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

// todo progress bar needs to move smoothly even if 1s audio length

// todo need a custom component for progress bar that's easily draggable, throttle updating on drag events

// todo dreamifying doesn't perfectly resume audio from where it was before dreamifying/un-dreamifying

// todo there's just general bugs from determining how long an audio is too
// todo revalidating audio menu doesn't work when dreamifying I guess

/**
 * An audio player widget which can also download YouTube video audio and thumbnails.
 */
@Vanilla
@CyderAuthor
@SuppressCyderInspections(CyderInspection.VanillaInspection)
public class AudioPlayer {
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
    private static final File DEFAULT_ALBUM_ART = OSUtil.buildFile(
            "static", "pictures", "music", "Default.png");

    /**
     * The format of the waveform image to export.
     */
    private static final String WAVEFORM_EXPORT_FORMAT = "png";

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
     * The label placed over the audio progress bar displaying how many seconds into the current audio
     * we are and how many seconds are remaining/how long the audio is in total.
     */
    private static final CyderLabel audioProgressLabel = new CyderLabel();

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
     * The play icon.
     */
    private static final ImageIcon playIcon = new ImageIcon(
            "static/pictures/music/Play.png");

    /**
     * The play icon for hover events.
     */
    private static final ImageIcon playIconHover = new ImageIcon(
            "static/pictures/music/PlayHover.png");

    /**
     * The pause icon.
     */
    private static final ImageIcon pauseIcon = new ImageIcon(
            "static/pictures/music/Pause.png");

    /**
     * The pause icon for hover events.
     */
    private static final ImageIcon pauseIconHover = new ImageIcon(
            "static/pictures/music/PauseHover.png");

    /**
     * The next icon.
     */
    private static final ImageIcon nextIcon = new ImageIcon(
            "static/pictures/music/Skip.png");

    /**
     * The next icon for hover events.
     */
    private static final ImageIcon nextIconHover = new ImageIcon(
            "static/pictures/music/SkipHover.png");

    /**
     * The last icon.
     */
    private static final ImageIcon lastIcon = new ImageIcon(
            "static/pictures/music/SkipBack.png");

    /**
     * The last icon for hover events.
     */
    private static final ImageIcon lastIconHover = new ImageIcon(
            "static/pictures/music/SkipBackHover.png");

    /**
     * The repeat icon.
     */
    private static final ImageIcon repeatIcon = new ImageIcon(
            "static/pictures/music/Repeat.png");

    /**
     * The repeat icon for hover events.
     */
    private static final ImageIcon repeatIconHover = new ImageIcon(
            "static/pictures/music/RepeatHover.png");

    /**
     * The shuffle icon.
     */
    private static final ImageIcon shuffleIcon = new ImageIcon(
            "static/pictures/music/Shuffle.png");

    /**
     * The shuffle icon for hover events.
     */
    private static final ImageIcon shuffleIconHover = new ImageIcon(
            "static/pictures/music/ShuffleHover.png");

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

    /**
     * The play next audio icon button.
     */
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

    /**
     * The repeat audio icon button.
     */
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

    /**
     * The shuffle audio icon button.
     */
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
     * The list of valid audio files within the current directory that the audio player may play.
     */
    private static final ArrayList<File> validAudioFiles = new ArrayList<>();

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
     * The alternate view icon.
     */
    private static final ImageIcon alternateView = new ImageIcon("static/pictures/icons/ChangeSize1.png");

    /**
     * The alternate view hover icon.
     */
    private static final ImageIcon alternateViewHover = new ImageIcon("static/pictures/icons/ChangeSize2.png");

    /**
     * The button to be placed in the audio player drag label button list to switch frame views.
     */
    private static final CyderIconButton switchFrameAudioView = new CyderIconButton(
            "Switch Mode", alternateView, alternateViewHover,
            new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    switch (currentFrameView.get()) {
                        case FULL -> setupAndShowFrameView(FrameView.HIDDEN_ART);
                        case HIDDEN_ART -> setupAndShowFrameView(FrameView.MINI);
                        case MINI -> setupAndShowFrameView(FrameView.FULL);
                        default -> throw new IllegalArgumentException(
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
     * Possible ways a user can interact with the audio player.
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
     * Suppress default constructor.
     */
    private AudioPlayer() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Allow widget to be found via reflection.
     */
    @Widget(triggers = {"mp3", "wav", "music", "audio"}, description = "An advanced audio playing widget")
    public static void showGui() {
        File userMusicDir = OSUtil.buildFile(
                DynamicDirectory.DYNAMIC_PATH,
                DynamicDirectory.USERS.getDirectoryName(),
                ConsoleFrame.INSTANCE.getUUID(),
                UserFile.MUSIC.getName());

        File[] userMusicFiles = userMusicDir.listFiles((dir, name) -> FileUtil.isSupportedAudioExtension(name));

        if (userMusicFiles != null && userMusicFiles.length > 0) {
            showGui(userMusicFiles[NumberUtil.randInt(0, userMusicFiles.length - 1)]);
        } else
            throw new IllegalArgumentException("Could not find any user audio files");
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
        refreshAudioFiles();

        audioDreamified.set(isCurrentAudioDreamy());

        // if frame is open, stop whatever audio is playing or
        // paused and begin playing the requested audio
        if (isWidgetOpen()) {
            pauseAudio();
            pauseLocation = 0;

            revalidateFromAudioFileChange();

            playAudio();

            return;
        }

        currentUserAlbumArtDir = OSUtil.buildFile(DynamicDirectory.DYNAMIC_PATH,
                DynamicDirectory.USERS.getDirectoryName(),
                ConsoleFrame.INSTANCE.getUUID(), UserFile.MUSIC.getName(), "AlbumArt");

        audioPlayerFrame = new CyderFrame(DEFAULT_FRAME_LEN, DEFAULT_FRAME_LEN, BACKGROUND_COLOR);
        refreshFrameTitle();
        audioPlayerFrame.getTopDragLabel().addButton(switchFrameAudioView, 1);
        audioPlayerFrame.setCurrentMenuType(CyderFrame.MenuType.PANEL);
        audioPlayerFrame.setMenuEnabled(true);
        audioPlayerFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // no other pre- / post-close window Runnables
                // should be added or window listeners
                killAndCloseWidget();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                // no other pre- / post-close window Runnables
                // should be added or window listeners
                killAndCloseWidget();
            }
        });
        installFrameMenuItems();

        /*
         All components which will ever be on the frame for phase 1 are added now and their sizes set.
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
        audioTitleLabel.setFont(CyderFonts.defaultFontSmall);
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

        audioLocationSliderUi.setThumbStroke(new BasicStroke(2.0f));
        audioLocationSliderUi.setSliderShape(CyderSliderUi.SliderShape.CIRCLE);
        audioLocationSliderUi.setThumbDiameter(25);
        audioLocationSliderUi.setFillColor(CyderColors.vanilla);
        audioLocationSliderUi.setOutlineColor(CyderColors.vanilla);
        audioLocationSliderUi.setNewValColor(trackNewColor);
        audioLocationSliderUi.setOldValColor(CyderColors.vanilla);
        audioLocationSliderUi.setTrackStroke(new BasicStroke(2.0f));

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
        });
        audioLocationSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                audioLocationSliderPressed.set(true);

                audioLocationSliderUi.setThumbDiameter(BIG_THUMB_SIZE);
                audioLocationSlider.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (audioLocationSliderPressed.get()) {
                    audioLocationSliderPressed.set(false);

                    float newPercentIn = (float) audioLocationSlider.getValue() / audioLocationSlider.getMaximum();
                    long resumeLocation = (long) (newPercentIn * totalAudioLength);

                    pauseAudio();
                    pauseLocation = resumeLocation;
                    audioLocationUpdater.setPercentIn(newPercentIn);
                    playAudio();
                }

                audioLocationSliderUi.setThumbDiameter(THUMB_SIZE);
                audioLocationSlider.repaint();
            }
        });
        audioLocationSlider.setOpaque(false);
        audioLocationSlider.setFocusable(false);
        audioLocationSlider.repaint();
        audioPlayerFrame.getContentPane().add(audioLocationSlider);

        audioProgressLabel.setSize(UI_ROW_WIDTH, UI_ROW_HEIGHT);
        audioProgressLabel.setText("");
        audioProgressLabel.setForeground(CyderColors.vanilla);
        audioPlayerFrame.getContentPane().add(audioProgressLabel);
        audioProgressLabel.setFocusable(false);

        if (audioLocationUpdater != null) {
            audioLocationUpdater.kill();
        }

        audioLocationUpdater = new AudioLocationUpdater(audioProgressLabel, currentFrameView,
                currentAudioFile, audioLocationSliderPressed, audioLocationSlider);

        audioVolumeSliderUi.setThumbStroke(new BasicStroke(2.0f));
        audioVolumeSliderUi.setSliderShape(CyderSliderUi.SliderShape.CIRCLE);
        audioVolumeSliderUi.setThumbDiameter(25);
        audioVolumeSliderUi.setFillColor(CyderColors.vanilla);
        audioVolumeSliderUi.setOutlineColor(CyderColors.vanilla);
        audioVolumeSliderUi.setNewValColor(trackNewColor);
        audioVolumeSliderUi.setOldValColor(CyderColors.vanilla);
        audioVolumeSliderUi.setTrackStroke(new BasicStroke(2.0f));

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
                audioVolumeSliderUi.setThumbDiameter(BIG_THUMB_SIZE);
                audioVolumeSlider.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                audioVolumeSliderUi.setThumbDiameter(THUMB_SIZE);
                audioVolumeSlider.repaint();
            }
        });
        audioVolumeSlider.setOpaque(false);
        audioVolumeSlider.setToolTipText("Volume");
        audioVolumeSlider.setFocusable(false);
        audioVolumeSlider.repaint();
        refreshAudioLine();

        setUiComponentsVisible(false);

        setupAndShowFrameView(FrameView.FULL);

        audioPlayerFrame.finalizeAndShow();

        ConsoleFrame.INSTANCE.revalidateAudioMenuVisibility();

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
                    ExceptionHandler.handle(e);
                }
            }, "AudioPlayer Preliminary Handler");
        }
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

        audioProgressLabel.setVisible(visible);

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
     * Installs all the menu options on the AudioPlayer frame.
     */
    private static void installFrameMenuItems() {
        audioPlayerFrame.clearMenuItems();

        audioPlayerFrame.addMenuItem("Export wav", () -> {
            if (wavExporterLocked.get()) {
                return;
            }

            if (FileUtil.validateExtension(currentAudioFile.get(), ".wav")) {
                audioPlayerFrame.notify("This file is already a wav");
            } else if (FileUtil.validateExtension(currentAudioFile.get(), ".mp3")) {
                CyderThreadRunner.submit(() -> {
                    Future<Optional<File>> wavConvertedFile = AudioUtil.mp3ToWav(currentAudioFile.get());

                    wavExporterLocked.set(true);

                    while (!wavConvertedFile.isDone()) {
                        Thread.onSpinWait();
                    }

                    wavExporterLocked.set(false);

                    try {
                        if (wavConvertedFile.get().isPresent()) {
                            File moveTo = OSUtil.buildFile(
                                    DynamicDirectory.DYNAMIC_PATH,
                                    DynamicDirectory.USERS.getDirectoryName(),
                                    ConsoleFrame.INSTANCE.getUUID(),
                                    UserFile.MUSIC.getName(),
                                    FileUtil.getFilename(wavConvertedFile.get().get()) + ".wav");

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
        });
        audioPlayerFrame.addMenuItem("Export mp3", () -> {
            if (mp3ExporterLocked.get()) {
                return;
            }

            if (FileUtil.validateExtension(currentAudioFile.get(), ".mp3")) {
                audioPlayerFrame.notify("This file is already an mp3");
            } else if (FileUtil.validateExtension(currentAudioFile.get(), ".wav")) {
                CyderThreadRunner.submit(() -> {
                    Future<Optional<File>> mp3ConvertedFile = AudioUtil.wavToMp3(currentAudioFile.get());

                    mp3ExporterLocked.set(true);

                    while (!mp3ConvertedFile.isDone()) {
                        Thread.onSpinWait();
                    }

                    mp3ExporterLocked.set(false);

                    try {
                        if (mp3ConvertedFile.get().isPresent()) {
                            File moveTo = OSUtil.buildFile(
                                    DynamicDirectory.DYNAMIC_PATH,
                                    DynamicDirectory.USERS.getDirectoryName(),
                                    ConsoleFrame.INSTANCE.getUUID(),
                                    UserFile.MUSIC.getName(),
                                    FileUtil.getFilename(mp3ConvertedFile.get().get()) + ".mp3");

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
        });
        audioPlayerFrame.addMenuItem("Waveform", () -> {
            if (waveformExporterLocked.get()) {
                return;
            }

            CyderThreadRunner.submit(() -> {
                String saveName = GetterUtil.getInstance().getString(new GetterUtil.Builder("Export Waveform")
                        .setRelativeTo(audioPlayerFrame)
                        .setLabelText("Enter a name to export the waveform as")
                        .setSubmitButtonText("Save to files"));

                if (!StringUtil.isNull(saveName)) {
                    if (OSUtil.isValidFilename(saveName)) {
                        File saveFile = OSUtil.buildFile(
                                DynamicDirectory.DYNAMIC_PATH,
                                DynamicDirectory.USERS.getDirectoryName(),
                                ConsoleFrame.INSTANCE.getUUID(),
                                UserFile.FILES.getName(),
                                saveName + "." + WAVEFORM_EXPORT_FORMAT);

                        Future<BufferedImage> waveform = MessagingUtils.generateLargeWaveform(currentAudioFile.get());

                        waveformExporterLocked.set(true);

                        while (!waveform.isDone()) {
                            Thread.onSpinWait();
                        }

                        waveformExporterLocked.set(false);

                        try {
                            ImageIO.write(waveform.get(), WAVEFORM_EXPORT_FORMAT, saveFile.getAbsoluteFile());
                            audioPlayerFrame.notify(new CyderFrame.NotificationBuilder
                                    ("Saved waveform to your files directory")
                                    .setOnKillAction(() -> PhotoViewer.getInstance(saveFile).showGui()));
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
            if (chooseFileLocked.get()) {
                return;
            }

            CyderThreadRunner.submit(() -> {
                chooseFileLocked.set(true);
                File chosenFile =
                        GetterUtil.getInstance().getFile(new GetterUtil.Builder("Choose an mp3 or wav file")
                                .setRelativeTo(audioPlayerFrame));

                chooseFileLocked.set(false);

                if (chosenFile != null && FileUtil.isSupportedAudioExtension(chosenFile)) {
                    lastAction = LastAction.FileChosen;

                    pauseAudio();

                    currentAudioFile.set(chosenFile);
                    pauseLocation = 0;
                    totalAudioLength = 0;

                    revalidateFromAudioFileChange();

                    playAudio();
                } else {
                    audioPlayerFrame.notify("Invalid file chosen");
                }
            }, "AudioPlayer File Chooser");
        });
        audioPlayerFrame.addMenuItem("Dreamify", () -> {
            if (dreamifierLocked.get()) {
                return;
            }

            if (currentAudioFile.get() != null) {
                String currentAudioFilename = FileUtil.getFilename(currentAudioFile.get());

                // already dreamified so attempt to find non-dreamy version
                if (currentAudioFilename.endsWith(AudioUtil.DREAMY_SUFFIX)) {
                    refreshAudioFiles();

                    String nonDreamyStdName = currentAudioFilename.substring(0,
                            currentAudioFilename.length() - AudioUtil.DREAMY_SUFFIX.length());

                    for (File validAudioFile : validAudioFiles) {
                        String localFilename = FileUtil.getFilename(validAudioFile);

                        if (localFilename.equals(nonDreamyStdName)) {
                            long unDreamifyPauseLoc = getRawPauseLocation();

                            audioDreamified.set(false);
                            audioPlayerFrame.revalidateMenu();

                            currentAudioFile.set(validAudioFile);

                            boolean resume = isAudioPlaying();
                            pauseAudio();

                            revalidateFromAudioFileChange();

                            if (resume) {
                                pauseLocation = unDreamifyPauseLoc;
                                playAudio();
                            }

                            return;
                        }
                    }

                    audioPlayerFrame.notify("Could not find audio's non-dreamy equivalent");
                    return;
                }

                File userMusicDir = OSUtil.buildFile(
                        DynamicDirectory.DYNAMIC_PATH,
                        DynamicDirectory.USERS.getDirectoryName(),
                        ConsoleFrame.INSTANCE.getUUID(),
                        UserFile.MUSIC.getName());

                if (userMusicDir.exists()) {
                    File[] audioFiles = userMusicDir.listFiles();

                    if (audioFiles != null && audioFiles.length > 0) {
                        for (File audioFile : audioFiles) {
                            if ((currentAudioFilename + AudioUtil.DREAMY_SUFFIX)
                                    .equalsIgnoreCase(FileUtil.getFilename(audioFile))) {

                                if (isAudioPlaying()) {
                                    pauseAudio();
                                }

                                audioDreamified.set(true);
                                audioPlayerFrame.revalidateMenu();

                                currentAudioFile.set(audioFile);

                                revalidateFromAudioFileChange();

                                playAudio();

                                return;
                            }
                        }
                    }
                }

                CyderThreadRunner.submit(() -> {
                    audioPlayerFrame.notify(new CyderFrame.NotificationBuilder(
                            "Dreamifying \"" + FileUtil.getFilename(currentAudioFile.get()) + "\"").setViewDuration(0));

                    dreamifierLocked.set(true);

                    Future<Optional<File>> dreamifiedAudio = AudioUtil.dreamifyAudio(currentAudioFile.get());

                    while (!dreamifiedAudio.isDone()) {
                        Thread.onSpinWait();
                    }

                    Optional<File> present = Optional.empty();

                    dreamifierLocked.set(false);

                    try {
                        present = dreamifiedAudio.get();
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }

                    if (present.isPresent()) {
                        try {
                            File destinationFile = OSUtil.buildFile(
                                    DynamicDirectory.DYNAMIC_PATH,
                                    DynamicDirectory.USERS.getDirectoryName(),
                                    ConsoleFrame.INSTANCE.getUUID(),
                                    UserFile.MUSIC.getName(),
                                    present.get().getName());

                            // copy dreamified audio to user's music dir
                            Files.copy(Paths.get(present.get().getAbsolutePath()),
                                    Paths.get(destinationFile.getAbsolutePath()));

                            if (isAudioPlaying()) {
                                pauseAudio();
                            }

                            currentAudioFile.set(destinationFile.getAbsoluteFile());

                            revalidateFromAudioFileChange();

                            playAudio();

                            audioDreamified.set(true);
                            audioPlayerFrame.revalidateMenu();
                            audioPlayerFrame.notify("Successfully dreamified audio");
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }
                    } else {
                        audioDreamified.set(false);
                        audioPlayerFrame.revalidateMenu();
                        audioPlayerFrame.notify("Could not dreamify audio at this time");
                    }
                }, "Audio Dreamifier");
            }
        }, audioDreamified);
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
        setUiComponentsVisible(false);

        switch (view) {
            case FULL -> {
                setUiComponentsVisible(true);
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
                audioProgressLabel.setLocation(xOff, yOff);
                audioVolumePercentLabel.setLocation(DEFAULT_FRAME_LEN / 2 - audioVolumePercentLabel.getWidth() / 2,
                        yOff + 35);
                yOff += 40 + yComponentPadding;
                audioVolumeSlider.setLocation(xOff, yOff);
            }
            case HIDDEN_ART -> {
                setUiComponentsVisible(true);
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
                audioProgressLabel.setLocation(xOff, yOff);

                audioVolumePercentLabel.setLocation(
                        DEFAULT_FRAME_LEN / 2 - audioVolumePercentLabel.getWidth() / 2, yOff + 35);
                yOff += 40 + yComponentPadding;
                audioVolumeSlider.setLocation(xOff, yOff);
                currentFrameView.set(FrameView.HIDDEN_ART);
            }
            case MINI -> {
                currentFrameView.set(FrameView.MINI);
                setUiComponentsVisible(true);
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
                audioProgressLabel.setVisible(false);
                audioVolumeSlider.setVisible(false);
                audioProgressLabel.setVisible(false);
                audioLocationSlider.setVisible(false);
                if (audioLocationUpdater != null) {
                    audioLocationUpdater.kill();
                    audioLocationUpdater = null;
                }
            }
            default -> throw new IllegalArgumentException("Unsupported frame view to switch to: " + view);
        }
    }

    /**
     * The default frame title.
     */
    private static final String DEFAULT_FRAME_TITLE = "Audio Player";

    /**
     * The maximum allowable characters on the title label.
     */
    private static final int MAX_TITLE_LENGTH = 40;

    /**
     * Refreshes the audio frame title.
     */
    private static void refreshFrameTitle() {
        String title = DEFAULT_FRAME_TITLE;

        if (currentAudioFile.get() != null) {
            title = StringUtil.capsFirst(StringUtil.getTrimmedText(FileUtil.getFilename(currentAudioFile.get())));

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

        File albumArtFilePng = OSUtil.buildFile(currentUserAlbumArtDir.getAbsolutePath(),
                name + ".png");
        File albumArtFileJpg = OSUtil.buildFile(currentUserAlbumArtDir.getAbsolutePath(),
                name + ".jpg");

        ImageIcon customAlbumArt = null;

        try {
            if (albumArtFilePng.exists()) {
                customAlbumArt = new ImageIcon(ImageIO.read(albumArtFilePng));
            } else if (albumArtFileJpg.exists()) {
                customAlbumArt = new ImageIcon(ImageIO.read(albumArtFileJpg));
            } else {
                customAlbumArt = new ImageIcon(ImageIO.read(DEFAULT_ALBUM_ART));
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        ImageIcon regularIcon = ImageUtil.resizeImage(customAlbumArt,
                ALBUM_ART_LABEL_SIZE, ALBUM_ART_LABEL_SIZE);
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

        ConsoleFrame.INSTANCE.revalidateMenu();
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
    private static void refreshAudioTitleLabel() {
        String text = StringUtil.capsFirst(FileUtil.getFilename(currentAudioFile.get().getName()));

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

        audioLocationUpdater = new AudioLocationUpdater(audioProgressLabel, currentFrameView,
                currentAudioFile, audioLocationSliderPressed, audioLocationSlider);
    }

    /**
     * Refreshes the list of valid audio files based on the files
     * within the same directory as the current audio file
     */
    private static void refreshAudioFiles() {
        checkNotNull(currentAudioFile);

        validAudioFiles.clear();

        File parentDirectory = currentAudioFile.get().getParentFile();

        if (parentDirectory.exists()) {
            File[] siblings = parentDirectory.listFiles();

            if (siblings != null && siblings.length > 0) {
                for (File sibling : siblings) {
                    if (FileUtil.isSupportedAudioExtension(sibling)) {
                        validAudioFiles.add(sibling);
                    }
                }
            }
        }
    }

    /**
     * Refreshes the icon of the play/pause button.
     */
    private static void refreshPlayPauseButtonIcon() {
        if (isAudioPlaying()) {
            playPauseButton.setIcon(pauseIcon);
            playPauseButton.setToolTipText("Pause");
        } else {
            playPauseButton.setIcon(playIcon);
            playPauseButton.setToolTipText("Play");
        }
    }

    /**
     * Returns whether audio is playing.
     *
     * @return whether audio is playing
     */
    public static boolean isAudioPlaying() {
        return audioPlayer != null && !audioPlayer.isComplete();
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
     * Refreshes the totalAudioLength based on the current audio file.
     */
    public static void refreshAudioTotalLength() {
        try {
            FileInputStream fileInputStream = new FileInputStream(currentAudioFile.get());
            totalAudioLength = fileInputStream.available();
            fileInputStream.close();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
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
    }

    /**
     * The file input stream to grab the audio data.
     */
    private static FileInputStream fis;

    /**
     * The buffered input stream for the file input stream.
     */
    private static BufferedInputStream bis;

    /**
     * The JLayer player used to play the audio.
     */
    private static Player audioPlayer;

    /**
     * The location the current audio file was paused/stopped at.
     */
    private static long pauseLocation;

    /**
     * The total audio length of the current audio file.
     */
    private static long totalAudioLength;

    /**
     * The amount to offset a pause request by so that a sequential play
     * request sounds like it was paused at that instant.
     */
    private static final int PAUSE_AUDIO_REACTION_OFFSET = 10000;

    /**
     * The semaphore used to ensure it is impossible for more than one audio to be playing at any one time.
     */
    private static final Semaphore audioPlayingSemaphore = new Semaphore(1);

    // todo maybe a class for actually playing audio that we can kill too so that the previous audio not yet concluded
    //  bug goes away

    /**
     * Starts playing the current audio file.
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    private static void playAudio() {
        try {
            if (isAudioPlaying()) {
                throw new IllegalStateException("Previous audio not yet concluded");
            }

            refreshAudioTitleLabel();

            fis = new FileInputStream(currentAudioFile.get());
            bis = new BufferedInputStream(fis);

            totalAudioLength = fis.available();

            fis.skip(Math.max(0, pauseLocation));

            audioPlayer = new Player(bis);

            AtomicBoolean ignoredExceptionThrown = new AtomicBoolean(false);

            CyderThreadRunner.submit(() -> {
                try {
                    refreshPlayPauseButtonIcon();
                    ConsoleFrame.INSTANCE.revalidateAudioMenuVisibility();

                    lastAction = LastAction.Play;
                    audioPlayingSemaphore.acquire();
                    audioLocationUpdater.resumeTimer();

                    audioPlayer.play();

                    refreshPlayPauseButtonIcon();
                    ConsoleFrame.INSTANCE.revalidateAudioMenuVisibility();
                } catch (Exception ignored) {
                    ignoredExceptionThrown.set(true);
                } finally {
                    audioPlayingSemaphore.release();

                    if (ignoredExceptionThrown.get()) {
                        // occasionally JLayer likes to throw for no apparently reason,
                        // so we'll just reset resources and play again
                        pauseAudio();
                        playAudio();
                    }
                }

                try {
                    closeIfNotNull(fis);
                    closeIfNotNull(bis);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }

                closePlayerObject();

                // if the widget is closed
                if (currentAudioFile == null) {
                    return;
                }

                // todo here we can check for if not killed in this thread worker class

                // no user interaction so proceed naturally
                if (lastAction == LastAction.Play) {
                    // in case audio files were added need to get index again
                    int currentAudioIndex = getCurrentAudioIndex();
                    currentAudioFile.set(validAudioFiles.get(currentAudioIndex));

                    pauseLocation = 0;
                    totalAudioLength = 0;

                    // repeat audio is first priority
                    if (repeatAudio) {
                        playAudio();
                    }
                    // pull from queue next
                    else if (!audioFileQueue.isEmpty()) {
                        currentAudioFile.set(audioFileQueue.remove(0));

                        revalidateFromAudioFileChange();

                        playAudio();
                    }
                    // shuffle audio takes next priority
                    else if (shuffleAudio) {
                        currentAudioFile.set(audioFileQueue.get(
                                NumberUtil.randInt(0, audioFileQueue.size() - 1)));

                        revalidateFromAudioFileChange();

                        playAudio();
                    }
                    // last of priorities is so choose the next audio file
                    else {
                        int currentIndex = 0;

                        for (int i = 0 ; i < validAudioFiles.size() ; i++) {
                            if (validAudioFiles.get(i).getAbsolutePath()
                                    .equals(currentAudioFile.get().getAbsolutePath())) {
                                currentIndex = i;
                                break;
                            }
                        }

                        // loop back around if exceeds bounds
                        int nextIndex = currentIndex + 1 == validAudioFiles.size()
                                ? 0 : currentIndex + 1;

                        currentAudioFile.set(validAudioFiles.get(nextIndex));

                        revalidateFromAudioFileChange();

                        playAudio();
                    }
                }
            }, "AudioPlayer Play Audio Thread [" + FileUtil.getFilename(currentAudioFile.get()) + "]");
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Pauses playback of the current audio file.
     */
    private static void pauseAudio() {
        lastAction = LastAction.Pause;

        audioLocationUpdater.pauseTimer();

        try {
            if (fis != null) {
                pauseLocation = totalAudioLength - fis.available() - PAUSE_AUDIO_REACTION_OFFSET;
                fis.close();
                fis = null;
            }
        } catch (Exception ignored) {
        }

        closeIfNotNull(bis);
        closePlayerObject();
        refreshPlayPauseButtonIcon();
    }

    /**
     * Ends and closes the audio player JLayer object if not null.
     */
    private static void closePlayerObject() {
        if (audioPlayer != null) {
            audioPlayer.close();
            audioPlayer = null;
        }
    }

    /**
     * Closes the provided input stream if not null and assigns the reference to null;
     *
     * @param is the input stream to close and assign to null
     */
    @SuppressWarnings("UnusedAssignment")
    public static void closeIfNotNull(InputStream is) {
        if (is != null) {
            try {
                is.close();
                is = null;
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
    }

    /**
     * The number of seconds which will not trigger a song restart instead of previous audio skip action if
     * the skip back button is pressed in the inclusive [0, SECONDS_IN_RESTART_TOL].
     */
    private static final int SECONDS_IN_RESTART_TOL = 5;

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

        pauseAudio();

        float totalSeconds = AudioUtil.getMillisFast(currentAudioFile.get()) / 1000.0f;
        int secondsIn = (int) Math.ceil(totalSeconds * pauseLocation / totalAudioLength);

        pauseLocation = 0;
        totalAudioLength = 0;

        refreshAudioFiles();

        if (secondsIn > SECONDS_IN_RESTART_TOL) {
            playAudio();
            return;
        }

        int currentIndex = getCurrentAudioIndex();
        int lastIndex = currentIndex == 0 ? validAudioFiles.size() - 1 : currentIndex - 1;

        currentAudioFile.set(validAudioFiles.get(lastIndex));

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

        pauseLocation = 0;
        totalAudioLength = 0;

        int currentIndex = getCurrentAudioIndex();
        int nextIndex = currentIndex == validAudioFiles.size() - 1 ? 0 : currentIndex + 1;

        currentAudioFile.set(validAudioFiles.get(nextIndex));

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
        refreshAudioFiles();

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
    @SuppressWarnings("unused")
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
        pauseLocation = 0;
        totalAudioLength = 0;

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

        ConsoleFrame.INSTANCE.revalidateAudioMenuVisibility();
    }

    /**
     * The time in ms to delay possible ui interactions.
     */
    private static final int ACTION_TIMEOUT_MS = 600;

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
        refreshAudioFiles();
        refreshAudioProgressLabel();

        audioDreamified.set(isCurrentAudioDreamy());

        audioPlayerFrame.revalidateMenu();
    }

    /**
     * Returns the raw pause location of the exact number of bytes played by fis.
     *
     * @return the raw pause location of the exact number of bytes played by fis
     * @throws IllegalArgumentException if the raw pause location could not be polled
     */
    private static long getRawPauseLocation() {
        try {
            if (fis != null) {
                return totalAudioLength - fis.available();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new IllegalArgumentException("Could not poll raw pause location");
    }

    // --------------------
    // Search YouTube View
    // --------------------

    // todo after audio player completely working and tested start with other views
}