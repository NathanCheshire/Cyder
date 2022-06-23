package cyder.handlers.external.audio;

import com.google.gson.Gson;
import cyder.annotations.CyderAuthor;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.enums.CyderInspection;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.external.PhotoViewer;
import cyder.handlers.external.audio.youtube.YoutubeSearchResultPage;
import cyder.handlers.external.audio.youtube.YoutubeVideo;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.handlers.internal.Logger;
import cyder.messaging.MessagingUtils;
import cyder.threads.CyderThreadFactory;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.user.UserFile;
import cyder.utils.*;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Port;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
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
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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
                        case SEARCH -> {
                            setPhaseTwoComponentsVisible(false);
                            setupAndShowFrameView(FrameView.FULL);
                        }
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
        File userMusicDir = OSUtil.buildFile(
                Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(),
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

        currentUserAlbumArtDir = OSUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(),
                ConsoleFrame.INSTANCE.getUUID(), UserFile.MUSIC.getName(), "AlbumArt");

        audioPlayerFrame = new CyderFrame(DEFAULT_FRAME_LEN, DEFAULT_FRAME_LEN, BACKGROUND_COLOR);
        refreshFrameTitle();
        audioPlayerFrame.getTopDragLabel().addButton(switchFrameAudioView, 1);
        audioPlayerFrame.setCurrentMenuType(CyderFrame.MenuType.PANEL);
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

        audioLocationSliderUi.setThumbStroke(new BasicStroke(2.0f));
        audioLocationSliderUi.setThumbShape(CyderSliderUi.ThumbShape.CIRCLE);
        audioLocationSliderUi.setThumbRadius(25);
        audioLocationSliderUi.setThumbFillColor(CyderColors.vanilla);
        audioLocationSliderUi.setThumbOutlineColor(CyderColors.vanilla);
        audioLocationSliderUi.setRightThumbColor(trackNewColor);
        audioLocationSliderUi.setLeftThumbColor(CyderColors.vanilla);
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
            if (audioTotalLength == UNKNOWN_AUDIO_LENGTH || audioTotalLength == 0) {
                audioTotalLength = AudioUtil.getTotalBytes(currentAudioFile.get());
            }

            audioLocationUpdater.setPercentIn((float) audioLocationSlider.getValue()
                    / audioLocationSlider.getMaximum());

            audioLocationUpdater.update(true);
        });
        audioLocationSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                audioLocationSliderPressed.set(true);

                audioLocationSliderUi.setThumbRadius(BIG_THUMB_SIZE);
                audioLocationSlider.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (audioLocationSliderPressed.get()) {
                    audioLocationSliderPressed.set(false);

                    boolean wasPlaying = isAudioPlaying();

                    if (wasPlaying) {
                        float newPercentIn = (float) audioLocationSlider.getValue() / audioLocationSlider.getMaximum();
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

        audioVolumeSliderUi.setThumbStroke(new BasicStroke(2.0f));
        audioVolumeSliderUi.setThumbShape(CyderSliderUi.ThumbShape.CIRCLE);
        audioVolumeSliderUi.setThumbRadius(25);
        audioVolumeSliderUi.setThumbFillColor(CyderColors.vanilla);
        audioVolumeSliderUi.setThumbOutlineColor(CyderColors.vanilla);
        audioVolumeSliderUi.setRightThumbColor(trackNewColor);
        audioVolumeSliderUi.setLeftThumbColor(CyderColors.vanilla);
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

        ConsoleFrame.INSTANCE.revalidateAudioMenuVisibility();

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
     * Installs all the menu options on the AudioPlayer frame.
     */
    private static void installFrameMenuItems() {
        audioPlayerFrame.clearMenuItems();

        audioPlayerFrame.addMenuItem("Export wav", exportWavMenuItem);
        audioPlayerFrame.addMenuItem("Export mp3", exportMp3MenuItem);
        audioPlayerFrame.addMenuItem("Waveform", waveformExporterMenuItem);
        audioPlayerFrame.addMenuItem("Search", searchMenuItem);
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
                                Dynamic.PATH,
                                Dynamic.USERS.getDirectoryName(),
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
    };

    /**
     * The menu item for exporting the current audio as an mp3.
     */
    private static final Runnable exportMp3MenuItem = () -> {
        if (mp3ExporterLocked.get() || uiLocked) {
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
                                Dynamic.PATH,
                                Dynamic.USERS.getDirectoryName(),
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
    };

    /**
     * The logic for the export waveform menu option.
     */
    private static final Runnable waveformExporterMenuItem = () -> {
        if (waveformExporterLocked.get() || uiLocked) {
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
                            Dynamic.PATH,
                            Dynamic.USERS.getDirectoryName(),
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
    };

    /**
     * The menu item for searching youtube for songs.
     */
    private static final Runnable searchMenuItem = AudioPlayer::constructPhaseTwoView;

    /**
     * The menu item for choosing a local audio file.
     */
    private static final Runnable chooseFileMenuItem = () -> {
        if (chooseFileLocked.get() || uiLocked) {
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
        audioPlayerFrame.notify(new CyderFrame.NotificationBuilder("Dreamifying \""
                + FileUtil.getFilename(currentAudioFile.get()) + "\"").setViewDuration(10000));
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
        File targetFile = OSUtil.buildFile(
                Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(),
                ConsoleFrame.INSTANCE.getUUID(),
                UserFile.MUSIC.getName(),
                dreamifiedFile.getName());

        Logger.log(Logger.Tag.DEBUG, "Lock released on file: " + dreamifiedFile.getAbsolutePath());

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
            refreshAudioFiles();

            String nonDreamyName = currentAudioFilename.substring(0,
                    currentAudioFilename.length() - AudioUtil.DREAMY_SUFFIX.length());

            if (attemptFindNonDreamyAudio(nonDreamyName)) {
                return;
            }

            audioPlayerFrame.notify("Could not find audio's non-dreamy equivalent");
            return;
        }

        File userMusicDir = OSUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(), ConsoleFrame.INSTANCE.getUUID(),
                UserFile.MUSIC.getName());

        // Not dreamified so attempt to find previously dreamified file if exists
        if (userMusicDir.exists()) {
            refreshAudioFiles();

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
        for (File validAudioFile : validAudioFiles) {
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
        for (File validAudioFile : validAudioFiles) {
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
            case HIDDEN_ART -> {
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
                currentFrameView.set(FrameView.HIDDEN_ART);
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
    static void refreshAudioTitleLabel() {
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

        audioLocationUpdater = new AudioLocationUpdater(secondsInLabel, totalSecondsLabel, currentFrameView,
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
    static void refreshPlayPauseButtonIcon() {
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

        ConsoleFrame.INSTANCE.revalidateAudioMenuVisibility();
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
            }
            // resume
            else if (lastAction == LastAction.Pause) {
                innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
                lastAction = LastAction.Play;
                innerAudioPlayer.setLocation(pauseLocation);
                innerAudioPlayer.play();
                audioLocationUpdater.resumeTimer();
            }
            // spin off object
            else if (innerAudioPlayer == null) {
                innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
                lastAction = LastAction.Play;
                innerAudioPlayer.play();
                audioLocationUpdater.resumeTimer();
            }
            // standard play
            else {
                lastAction = LastAction.Play;
                innerAudioPlayer.play();
                audioLocationUpdater.resumeTimer();
            }

            pauseLocation = UNKNOWN_PAUSE_LOCATION;
            pauseLocationMillis = UNKNOWN_PAUSE_LOCATION;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

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
                currentAudioFile.set(validAudioFiles.get(getRandomIndex()));
                innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
                revalidateFromAudioFileChange();
                playAudio();
            } else {
                int currentIndex = getCurrentAudioIndex();

                int nextIndex = currentIndex + 1 == validAudioFiles.size() ? 0 : currentIndex + 1;

                currentAudioFile.set(validAudioFiles.get(nextIndex));

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

        refreshAudioFiles();

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
        int lastIndex = currentIndex == 0 ? validAudioFiles.size() - 1 : currentIndex - 1;

        currentAudioFile.set(validAudioFiles.get(lastIndex));

        revalidateFromAudioFileChange();

        innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());
        audioLocationUpdater.update(false);

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
        int nextIndex = currentIndex == validAudioFiles.size() - 1 ? 0 : currentIndex + 1;

        if (shuffleAudio) {
            nextIndex = getRandomIndex();
        }

        currentAudioFile.set(validAudioFiles.get(nextIndex));
        innerAudioPlayer = new InnerAudioPlayer(currentAudioFile.get());

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
     * Returns a random index of the validAudioFiles list.
     *
     * @return a random index of the validAudioFiles list
     */
    private static int getRandomIndex() {
        if (validAudioFiles.size() == 1) {
            return 0;
        }

        LinkedList<Integer> ints = new LinkedList<>();

        for (int i = 0 ; i < validAudioFiles.size() ; i++) {
            if (i == getCurrentAudioIndex()) {
                continue;
            }

            ints.add(i);
        }

        return ints.get(NumberUtil.randInt(0, ints.size() - 1));
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
    private static final int bufferedImageLen = 250;

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
    private static CyderTextField searchField;

    /**
     * The previously searched text.
     */
    private static String previousSearch;

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

        searchField = new CyderTextField();
        searchField.setFont(new Font("Agency FB", Font.BOLD, 26));
        searchField.setForeground(CyderColors.vanilla);
        searchField.setCaret(new CyderCaret(CyderColors.vanilla));
        searchField.setBackground(BACKGROUND_COLOR);
        searchField.setBounds((audioPlayerFrame.getWidth() - phaseTwoWidth) / 2, yOff, phaseTwoWidth, 40);
        searchField.setToolTipText("Search");
        searchField.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, CyderColors.vanilla));
        audioPlayerFrame.getContentPane().add(searchField);

        yOff += 50;

        searchButton = new CyderButton("Search");
        searchButton.setBorder(new LineBorder(Color.black, 4));
        searchButton.setBackground(CyderColors.regularPurple);
        searchButton.setForeground(CyderColors.vanilla);
        searchButton.setBorder(new LineBorder(Color.black, 3));
        searchButton.setBounds((audioPlayerFrame.getWidth() - phaseTwoWidth) / 2, yOff, phaseTwoWidth, 40);
        audioPlayerFrame.getContentPane().add(searchButton);
        searchField.addActionListener(e -> searchAndUpdate(searchField.getText()));
        searchButton.addActionListener(e -> searchAndUpdate(searchField.getText()));

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

        StyleConstants.setAlignment(alignment, StyleConstants.ALIGN_CENTER);
        StyledDocument doc = searchResultsPane.getStyledDocument();
        doc.setParagraphAttributes(0, doc.getLength(), alignment, false);

        searchResultsScroll = new CyderScrollPane(searchResultsPane);
        searchResultsScroll.setThumbSize(8);
        searchResultsScroll.getViewport().setOpaque(false);
        searchResultsScroll.setFocusable(true);
        searchResultsScroll.setOpaque(false);
        searchResultsScroll.setThumbColor(CyderColors.regularPink);
        searchResultsScroll.setBorder(new LineBorder(Color.black, 4));
        searchResultsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        searchResultsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        searchResultsScroll.setBounds((audioPlayerFrame.getWidth() - UI_ROW_WIDTH) / 2,
                yOff, UI_ROW_WIDTH, audioPlayerFrame.getWidth() - 20 - yOff);

        informationLabel = new CyderLabel();
        informationLabel.setForeground(CyderColors.vanilla);
        informationLabel.setFont(CyderFonts.DEFAULT_FONT);
        informationLabel.setBackground(BACKGROUND_COLOR);
        informationLabel.setOpaque(true);
        informationLabel.setBorder(new LineBorder(Color.black, 4));
        informationLabel.setBounds((audioPlayerFrame.getWidth() - UI_ROW_WIDTH) / 2,
                yOff, UI_ROW_WIDTH, audioPlayerFrame.getWidth() - 20 - yOff);
        audioPlayerFrame.getContentPane().add(informationLabel);
        showInformationLabel("Search YouTube using the above field");

        searchResultsPane.setCaretPosition(0);
        audioPlayerFrame.getContentPane().add(searchResultsScroll);
        searchResultsPane.revalidate();

        printingUtil = new StringUtil(new CyderOutputPane(searchResultsPane));

        phaseTwoViewLocked.set(false);
    }

    /**
     * Sets the phase two components to the visibility specified.
     *
     * @param visible whether the phase two components should be visible.
     */
    @SuppressWarnings("SameParameterValue")
    private static void setPhaseTwoComponentsVisible(boolean visible) {
        searchField.setVisible(visible);
        searchButton.setVisible(visible);
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
        informationLabel.setVisible(true);
        informationLabel.setText(text);
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

    private static final String printButtonPadding = "    ";

    /**
     * Searches YouTube for the provided text and updates the results pane with videos found.
     *
     * @param fieldText the text to search youtube for
     */
    private static void searchAndUpdate(String fieldText) {
        if (StringUtil.isNull(fieldText) || fieldText.equalsIgnoreCase(previousSearch)) {
            return;
        }

        previousSearch = fieldText;

        // todo a results label would be nice that also shows the number of pages
        // todo step buttons on side of field too to navigate the pages of results

        // todo back button for phase 2

        // todo going to next audio doesn't work?

        // todo similar command finder still broken?

        CyderThreadRunner.submit(() -> {
            showInformationLabel("Searching...");

            Optional<YoutubeSearchResultPage> youtubeSearchResultPage = getSearchResults(
                    YoutubeUtil.buildYouTubeApiV3SearchQuery(numSearchResults, fieldText));

            if (youtubeSearchResultPage.isPresent()) {
                searchResults.clear();

                for (YoutubeVideo video : youtubeSearchResultPage.get().getItems()) {
                    String uuid = video.getId().getVideoId();
                    searchResults.add(new YoutubeSearchResult(
                            uuid,
                            video.getSnippet().getTitle(),
                            video.getSnippet().getDescription(),
                            video.getSnippet().getChannelTitle(),
                            getMaxResolutionSquareThumbnail(uuid)));
                }

                // if user has search for something else, don't update pane
                if (!fieldText.equals(searchField.getText())) {
                    hideInformationLabel();
                    return;
                }

                searchResultsPane.setText("");

                for (YoutubeSearchResult result : searchResults) {
                    AtomicBoolean isDownloading = new AtomicBoolean();

                    ImageIcon defaultIcon = ImageUtil.toImageIcon(result.bi);

                    JLabel imageLabel = new JLabel(defaultIcon);
                    imageLabel.setSize(bufferedImageLen, bufferedImageLen);
                    imageLabel.setHorizontalAlignment(JLabel.CENTER);
                    imageLabel.setBorder(new LineBorder(Color.black, 4));
                    printingUtil.printlnComponent(imageLabel);

                    printingUtil.println("\n");

                    CyderLabel titleLabel = new CyderLabel(result.title);
                    titleLabel.setForeground(CyderColors.vanilla);
                    titleLabel.setHorizontalAlignment(JLabel.CENTER);
                    printingUtil.printlnComponent(titleLabel);

                    CyderLabel channelLabel = new CyderLabel(result.channel);
                    channelLabel.setForeground(CyderColors.vanilla);
                    channelLabel.setHorizontalAlignment(JLabel.CENTER);
                    printingUtil.printlnComponent(channelLabel);

                    printingUtil.println("\n");

                    String videoUrl = YoutubeUtil.buildYoutubeVideoUrl(result.uuid);
                    YoutubeUtil.YoutubeDownload downloadable = new YoutubeUtil.YoutubeDownload(videoUrl);
                    // todo start this and be able to kill it too
                    // todo need to not print stuff to console always
                    // todo make this it's own object

                    CyderButton downloadButton = new CyderButton() {
                        @Override
                        public void setText(String text) {
                            super.setText(printButtonPadding + text + printButtonPadding);
                        }
                    };
                    downloadButton.setText("Download");
                    downloadButton.setBorder(new LineBorder(Color.black, 4));
                    downloadButton.setBackground(CyderColors.regularPurple);
                    downloadButton.setForeground(CyderColors.vanilla);
                    downloadButton.setBorder(new LineBorder(Color.black, 3));
                    downloadButton.setSize(phaseTwoWidth, 40);
                    downloadButton.addActionListener(e -> {
                        // todo
                    });
                    downloadButton.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            if (isDownloading.get()) {
                                downloadButton.setText("Cancel");
                            }
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            if (isDownloading.get()) {
                                // todo need to update this
                                downloadButton.setText(downloadable.getDownloadableProgress() + "%");
                            } else {
                                downloadButton.setText("Download");
                            }
                        }
                    });

                    printingUtil.printlnComponent(downloadButton);

                    printingUtil.println("\n");
                }

                searchResultsPane.setCaretPosition(0);
                hideInformationLabel();
            } else {
                showInformationLabel("No results found :(");
            }
        }, "YouTube Search Updater");
    }

    @SuppressWarnings({"SuspiciousNameCombination", "ConstantConditions"})
    private static BufferedImage getMaxResolutionSquareThumbnail(String uuid) {
        Optional<BufferedImage> optionalBi = YoutubeUtil.getMaxResolutionThumbnail(uuid);

        BufferedImage bi = optionalBi.orElse(null);

        if (bi == null) {
            try {
                bi = ImageIO.read(DEFAULT_ALBUM_ART);
            } catch (Exception ignored) {}
        }

        int width = bi.getWidth();
        int height = bi.getHeight();

        if (width < height) {
            bi = ImageUtil.getCroppedImage(bi, 0, (height - width) / 2, width, width);
        } else if (height < width) {
            bi = ImageUtil.getCroppedImage(bi, (width - height) / 2, 0, height, height);
        } else {
            bi = ImageUtil.getCroppedImage(bi, 0, 0, width, height);
        }

        bi = ImageUtil.resizeImage(bi, bi.getType(), bufferedImageLen, bufferedImageLen);

        return bi;
    }

    /**
     * The gson object used for queries from youtube.
     */
    private static final Gson gson = new Gson();

    /**
     * Returns the search results for a particular url query.
     *
     * @param url the constructed url to get youtube video results
     * @return the YoutubeSearchResultPage object if present, empty optional else
     */
    private static Optional<YoutubeSearchResultPage> getSearchResults(String url) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(url).openStream()))) {
            return Optional.of(gson.fromJson(reader, YoutubeSearchResultPage.class));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Optional.empty();
    }
}