package cyder.handlers.external;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.AnimationDirection;
import cyder.enums.LoggerTag;
import cyder.enums.SliderShape;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.CyderShare;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.user.UserFile;
import cyder.utilities.*;
import cyder.utilities.objects.GetterBuilder;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;

// todo need a failsafe so the audio controls on console are revalidated
//      and never displayed if the audio player frame isn't visible

/**
 * An audio player widget that only supports mp3 files at the moment.
 */
public class AudioPlayer {
    /**
     * An enum dictating the last button the user pressed.
     */
    private enum LastAction {
        /**
         * Skip forward or backward/
         */
        SKIP,
        /**
         * The play/pause button was toggled to invoke a pause.
         */
        PAUSE,
        /**
         * The stop button was invoked.
         */
        STOP,
        /**
         * The audio was resumed following a pause.
         */
        RESUME,
        /**
         * The audio was started after a stop.
         */
        PLAY
    }

    /**
     * The last action the user invoked.
     */
    private static LastAction lastAction;

    /**
     * The audio title scrolling label.
     */
    private static ScrollingTitleLabel scrollingTitleLabel;

    /**
     * The audio location progress bar.
     */
    private static AudioLocationBar audioLocationBar;

    /**
     * The audio title text label which is scrolled via the srollingTitleLabel object.
     */
    private static JLabel audioTitleLabel;

    /**
     * The container that holds the scrolling audio title so restrict its bounds.
     */
    private static JLabel audioTitleLabelContainer;

    /**
     * The master audio frame.
     */
    private static CyderFrame audioFrame;

    /**
     * The audio volume slider.
     */
    private static JSlider audioVolumeSlider;

    /**
     * The progress bar for the audio progress.
     */
    private static CyderProgressBar audioProgress;

    /**
     * Button to skip to the last audio if it exists.
     */
    private static JButton previousAudioButton;

    /**
     * Button to skip to the enxt audio if it exists.
     */
    private static JButton nextAudioButton;

    /**
     * Button to stop the audio.
     */
    private static JButton stopAudioButton;

    /**
     * Button to toggle whether the current audio will repeat when it concludes.
     */
    private static JButton loopAudioButton;

    /**
     * Button allowing user to select an mp3 file from any directory
     * and skip around to the neighboring files from that directory.
     */
    private static JButton selectAudioDirButton;

    /**
     * Button allowing a user to play/pause the currently playing audio.
     */
    private static JButton playPauseAudioButton;

    /**
     * Button to shuffle the audio files when the current audio file concludes.
     */
    private static JButton shuffleAudioButton;

    /**
     * The audio progress text which is placed over the audio location progress bar.
     */
    private static JLabel audioProgressLabel;

    /**
     * Whether to shuffle the audio files.
     */
    private static boolean shuffleAudio;

    /**
     * Whether to repeat the current audio file/
     */
    private static boolean repeatAudio;

    /**
     * The label to use for album art if available.
     */
    private static JLabel albumArtLabel;

    /**
     * The window state AudioPlayer is in.
     */
    private static PlayerWindowState windowState = PlayerWindowState.ALBUM_ART;

    /**
     * The possible states for the AudioPlayer.
     */
    private enum PlayerWindowState {
        ALBUM_ART, DEFAULT, MINI
    }

    /**
     * The index of the current audio file within audioFiles.
     */
    private static int audioIndex;

    /**
     * The currently available audio files.
     */
    private static final LinkedList<File> currentAudioFiles = new LinkedList<>();

    /**
     * The average reaction time of a user between when they
     * press pause and where the pause location should be.
     */
    private static final int pauseAudioReactionOffset = 10000;

    /**
     * The album art associated with the current audio file to use for the taskbar icon.
     */
    private static ImageIcon currentAlbumArt;

    /**
     * The JLayer player object for actually playing the audio files.
     */
    private static Player player;

    /**
     * The buffered input stream for the player.
     */
    private static BufferedInputStream bis;

    /**
     * The file input stream for the player.
     */
    private static FileInputStream fis;

    /**
     * The location the current audio file is paused at.
     */
    private static long pauseLocation;

    /**
     * The total length of the current audio file.
     */
    private static long totalLength;

    /**
     * The default text for the AudioFrame.
     */
    private static final String DEFAULT_TITLE = "Flash Player";

    /**
     * The default text for the audio title label.
     */
    private static final String DEFAULT_LABEL_TEXT = "No Audio Playing";

    /**
     * The time the user last pressed a button.
     */
    private static long lastActionTime;

    /**
     * The time in between button clicks to disable any action from being invoked.
     */
    private static final long actionTimeoutMS = 250;

    /**
     * The length of the album art icon.
     */
    private static final int albumArtLen = 200;

    /**
     * The queue of songs to play next before incrementing the audio index.
     */
    private static final ArrayList<File> queue = new ArrayList<>();

    /**
     * The background color of the audio frame.
     */
    public static final Color backgroundColor = new Color(8,23,52);

    /**
     * Instantiation of AudioPlayer not allowed.
     */
    private AudioPlayer() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Method for widget finder to invoke by using reflection to find the Widget annotation
     */
    @Widget(triggers = {"mp3", "music", "audio"}, description = "An audio playing widget")
    public static void showGUI() {
        // show the gui and attempmt to find audio files by passing null
        showGUI(null);
    }

    /**
     * Constructor that launches the AudioPlayer.
     *
     * @param startPlaying the audio file to start playing upon successful launch of the AudioPlayer.
     * Pass {@code null} to attempt to find valid audio files from the user's Music/ directory.
     */
    public static void showGUI(File startPlaying) {
        // frame is already open so act as if another song was requested to be played immediately
        if (audioFrame != null) {
            // stop any audio playing
            stopAudio();

            // refresh based on the file
            refreshAudioFiles(startPlaying);

            // if audio files were found then play
            // (might be startPlaying if it was not null and is avalid mp3 file)
            if (!currentAudioFiles.isEmpty()) {
                startAudio();
            }

            // no construction of ui required so return
            return;
        }

        // wipe the queue
        queue.clear();

        if (startPlaying == null) {
            refreshAudioFiles(startPlaying);
        }

        if (IOUtil.generalAudioPlaying())
            IOUtil.stopAudio();

        audioFrame = new CyderFrame(500,480,
                new ImageIcon(ImageUtil.bufferedImageFromColor(500,225, backgroundColor)));
        audioFrame.setBackground(backgroundColor);
        audioFrame.setTitle(DEFAULT_TITLE);
        audioFrame.addWindowListener(
            new WindowAdapter() {
                 //to be safe, upon the window closing and the window closed events
                 // we kill the widget
                 @Override
                 public void windowClosed(WindowEvent e) {
                     killWidget();
                     currentAudioFiles.clear();
                     audioIndex = -1;
                 }
             }
        );

        JButton changeSize = new JButton("");
        changeSize.setToolTipText("Toggle Window State");
        changeSize.addActionListener(e -> incrementWindowState());
        changeSize.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                changeSize.setIcon(CyderIcons.changeSizeIconHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                changeSize.setIcon(CyderIcons.changeSizeIcon);
            }
        });

        changeSize.setIcon(CyderIcons.changeSizeIcon);
        changeSize.setContentAreaFilled(false);
        changeSize.setBorderPainted(false);
        changeSize.setFocusPainted(false);
        audioFrame.getTopDragLabel().addButton(changeSize, 2);

        audioTitleLabelContainer = new JLabel();
        audioFrame.getContentPane().add(audioTitleLabelContainer);

        albumArtLabel = new JLabel();
        albumArtLabel = new JLabel();
        audioFrame.getContentPane().add(albumArtLabel);

        audioTitleLabel = new JLabel();
        audioTitleLabel.setToolTipText("Currently Playing");
        audioTitleLabel.setFont(new Font("tahoma", Font.BOLD, 18));
        audioTitleLabel.setForeground(CyderColors.vanila);
        audioTitleLabel.setText(DEFAULT_LABEL_TEXT);
        audioTitleLabelContainer.add(audioTitleLabel);

        selectAudioDirButton = new JButton("");
        selectAudioDirButton.setFocusPainted(false);
        selectAudioDirButton.setOpaque(false);
        selectAudioDirButton.setContentAreaFilled(false);
        selectAudioDirButton.setBorderPainted(false);
        selectAudioDirButton.setToolTipText("Select audio");
        selectAudioDirButton.addActionListener(e -> CyderThreadRunner.submit(() -> {
            try {
                CyderThreadRunner.submit(() -> {
                    GetterBuilder builder = new GetterBuilder("Choose any mp3 file to startAudio");
                    builder.setRelativeTo(audioFrame);
                    File selectedChildFile = GetterUtil.getInstance().getFile(builder);
                    if (selectedChildFile != null) {
                        if (!selectedChildFile.toString().endsWith("mp3")) {
                            audioFrame.notify("Sorry, " + UserUtil.getCyderUser().getName() + ", but that's not an mp3 file.");
                        } else if (selectedChildFile != null){
                            stopAudio();
                            refreshAudioFiles(selectedChildFile);
                            startAudio();
                        }
                    }
                }, "AudioPlayer getFile()");
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            } finally {
                refreshAudioFiles(null);
            }
        }, "wait thread for GetterUtil().getFile()"));

        selectAudioDirButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                selectAudioDirButton.setIcon(new ImageIcon("static/pictures/music/SelectFileHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                selectAudioDirButton.setIcon(new ImageIcon("static/pictures/music/SelectFile.png"));
            }
        });

        ImageIcon File = new ImageIcon("static/pictures/music/SelectFile.png");
        selectAudioDirButton.setIcon(File);
        audioFrame.getContentPane().add(selectAudioDirButton);

        loopAudioButton = new JButton("");
        loopAudioButton.setToolTipText("Loop audio");
        loopAudioButton.addActionListener(e -> {
            loopAudioButton.setIcon(new ImageIcon(repeatAudio ? "static/pictures/music/Repeat.png" : "static/pictures/music/RepeatHover.png"));
            loopAudioButton.setToolTipText("Loop Audio");
            repeatAudio = !repeatAudio;
        });

        loopAudioButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loopAudioButton.setIcon(new ImageIcon(repeatAudio ? "static/pictures/music/Repeat.png" : "static/pictures/music/RepeatHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loopAudioButton.setIcon(new ImageIcon(repeatAudio ? "static/pictures/music/RepeatHover.png" : "static/pictures/music/Repeat.png"));
            }
        });

        loopAudioButton.setIcon(new ImageIcon("static/pictures/music/Repeat.png"));
        audioFrame.getContentPane().add(loopAudioButton);
        loopAudioButton.setFocusPainted(false);
        loopAudioButton.setOpaque(false);
        loopAudioButton.setContentAreaFilled(false);
        loopAudioButton.setBorderPainted(false);

        previousAudioButton = new JButton("");
        previousAudioButton.setToolTipText("Previous audio");
        previousAudioButton.addActionListener(e -> previousAudio());

        previousAudioButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                previousAudioButton.setIcon(new ImageIcon("static/pictures/music/SkipBackHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                previousAudioButton.setIcon(new ImageIcon("static/pictures/music/SkipBack.png"));
            }
        });

        previousAudioButton.setIcon(new ImageIcon("static/pictures/music/SkipBack.png"));
        audioFrame.getContentPane().add(previousAudioButton);
        previousAudioButton.setFocusPainted(false);
        previousAudioButton.setOpaque(false);
        previousAudioButton.setContentAreaFilled(false);
        previousAudioButton.setBorderPainted(false);

        stopAudioButton = new JButton("");
        stopAudioButton.setToolTipText("Stop");
        stopAudioButton.addActionListener(e -> {
            try {
                stopAudio();
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        });

        stopAudioButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                stopAudioButton.setIcon(new ImageIcon("static/pictures/music/StopHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                stopAudioButton.setIcon(new ImageIcon("static/pictures/music/Stop.png"));
            }
        });

        stopAudioButton.setIcon(new ImageIcon("static/pictures/music/Stop.png"));
        audioFrame.getContentPane().add(stopAudioButton);
        stopAudioButton.setFocusPainted(false);
        stopAudioButton.setOpaque(false);
        stopAudioButton.setContentAreaFilled(false);
        stopAudioButton.setBorderPainted(false);

        playPauseAudioButton = new JButton("");
        playPauseAudioButton.setToolTipText("Play");
        playPauseAudioButton.addActionListener(e -> {
            try {
                if (player != null) {
                    pauseAudio();
                } else {
                    resumeAudio(pauseLocation);
                }
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        });

        playPauseAudioButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                playPauseAudioButton.setIcon(new ImageIcon(player == null ? "static/pictures/music/PlayHover.png" : "static/pictures/music/PauseHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                playPauseAudioButton.setIcon(new ImageIcon(player == null ? "static/pictures/music/Play.png" : "static/pictures/music/Pause.png"));
            }
        });

        playPauseAudioButton.setIcon(new ImageIcon("static/pictures/music/Play.png"));
        audioFrame.getContentPane().add(playPauseAudioButton);
        playPauseAudioButton.setFocusPainted(false);
        playPauseAudioButton.setOpaque(false);
        playPauseAudioButton.setContentAreaFilled(false);
        playPauseAudioButton.setBorderPainted(false);

        nextAudioButton = new JButton("");
        nextAudioButton.setToolTipText("Next Audio");
        nextAudioButton.addActionListener(e -> nextAudio());

        nextAudioButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                nextAudioButton.setIcon(new ImageIcon("static/pictures/music/SkipHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nextAudioButton.setIcon(new ImageIcon("static/pictures/music/Skip.png"));
            }
        });

        nextAudioButton.setIcon(new ImageIcon("static/pictures/music/Skip.png"));
        audioFrame.getContentPane().add(nextAudioButton);
        nextAudioButton.setFocusPainted(false);
        nextAudioButton.setOpaque(false);
        nextAudioButton.setContentAreaFilled(false);
        nextAudioButton.setBorderPainted(false);

        shuffleAudioButton = new JButton("");
        shuffleAudioButton.setToolTipText("Shuffle audio");
        shuffleAudioButton.addActionListener(e -> {
            if (!shuffleAudio) {
                shuffleAudioButton.setIcon(new ImageIcon("static/pictures/music/Shuffle.png"));
                shuffleAudio = true;
            } else {
                shuffleAudioButton.setIcon(new ImageIcon("static/pictures/music/ShuffleHover.png"));
                shuffleAudio = false;
            }
        });

        shuffleAudioButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                shuffleAudioButton.setIcon(new ImageIcon(shuffleAudio ? "static/pictures/music/Shuffle.png" : "static/pictures/music/ShuffleHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                shuffleAudioButton.setIcon(new ImageIcon(shuffleAudio ? "static/pictures/music/ShuffleHover.png" : "static/pictures/music/Shuffle.png"));
            }
        });

        shuffleAudioButton.setIcon(new ImageIcon("static/pictures/music/Shuffle.png"));
        audioFrame.getContentPane().add(shuffleAudioButton);
        shuffleAudioButton.setFocusPainted(false);
        shuffleAudioButton.setOpaque(false);
        shuffleAudioButton.setContentAreaFilled(false);
        shuffleAudioButton.setBorderPainted(false);

        audioVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        CyderSliderUI UI = new CyderSliderUI(audioVolumeSlider);
        UI.setThumbStroke(new BasicStroke(2.0f));
        UI.setSliderShape(SliderShape.CIRCLE);
        UI.setThumbDiameter(25);
        UI.setFillColor(CyderColors.vanila);
        UI.setOutlineColor(CyderColors.vanila);
        UI.setNewValColor(CyderColors.vanila);
        UI.setOldValColor(CyderColors.regularRed);
        UI.setTrackStroke(new BasicStroke(2.0f));
        audioVolumeSlider.setUI(UI);
        audioVolumeSlider.setMinimum(0);
        audioVolumeSlider.setMaximum(100);
        audioVolumeSlider.setPaintTicks(false);
        audioVolumeSlider.setPaintLabels(false);
        audioVolumeSlider.setVisible(true);
        audioVolumeSlider.setValue(50);
        audioVolumeSlider.addChangeListener(e -> refreshAudio());
        audioVolumeSlider.setOpaque(false);
        audioVolumeSlider.setToolTipText("Volume");
        audioVolumeSlider.setFocusable(false);
        audioVolumeSlider.repaint();
        audioFrame.getContentPane().add(audioVolumeSlider);

        audioProgressLabel = new CyderLabel("");
        audioProgressLabel.setFont(CyderFonts.defaultFontSmall);
        audioProgressLabel.setForeground(CyderColors.vanila);
        audioProgressLabel.setFocusable(false);
        audioFrame.getContentPane().add(audioProgressLabel);
        audioProgressLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (fis != null && player != null) {
                    pauseAudio();
                    long skipLocation = (long) (((double) e.getX() / (double) audioProgressLabel.getWidth()) * totalLength);
                    pauseLocation = skipLocation;
                    resumeAudio(skipLocation);
                }
            }
        });

        audioProgress = new CyderProgressBar(CyderProgressBar.HORIZONTAL, 0, 10000);
        audioProgress.setSize(385, 35);
        CyderProgressUI ui = new CyderProgressUI();
        ui.setColors(new Color[]{Color.decode("#C33764"), Color.decode("#1D2671")});
        ui.setAnimationDirection(AnimationDirection.LEFT_TO_RIGHT);
        ui.setShape(CyderProgressUI.Shape.SQUARE);
        audioProgress.setUI(ui);
        audioProgress.setMinimum(0);
        audioProgress.setMaximum(10000);
        audioProgress.setBorder(new LineBorder(Color.black, 4));
        audioProgress.setVisible(false);
        audioProgress.setValue(0);
        audioProgress.setOpaque(false);
        audioProgress.setToolTipText("Audio Location");
        audioProgress.setFocusable(false);
        audioProgress.repaint();
        audioFrame.getContentPane().add(audioProgress);

        audioFrame.setLocationRelativeTo(CyderShare.getDominantFrame());
        audioFrame.setVisible(true);
        audioFrame.requestFocus();

        if (startPlaying != null && FileUtil.getExtension(startPlaying).equals(".mp3")) {
            refreshAudioFiles(startPlaying);
            startAudio();
        } else {
            try {
                File userAudioDir = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Music/" );

                if (!userAudioDir.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    userAudioDir.mkdir();
                    return;
                }

                File[] userFiles = userAudioDir.listFiles();

                if (userFiles.length == 0) {
                    return;
                }

                for (File f : userFiles) {
                    if (FileUtil.getExtension(f).equals(".mp3"))
                        refreshAudioFiles(f);
                }

                startAudio();
            }

            catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
    }

    /**
     * Adds the given file to the queue.
     * If the player is not open, the Player is started and begins playing the audioFile.
     *
     * @param audioFile the audio to play
     */
    public static synchronized void addToMp3Queue(File audioFile) {
        if (audioPlaying()) {
            queue.add(audioFile);
        } else if (windowOpen()) {
            refreshAudioFiles(audioFile);
            startAudio();
        } else {
            showGUI(audioFile);
        }
    }

    /**
     * Returns the associated JLayer player.
     */
    public static Player getPlayer() {
        return player;
    }

    /**
     * Determines whether or not the audio widget is currently playing audio.
     * If player is closed, then player is set to null so this will always work.
     *
     * @return whether or not audio is playing via the AudioPlayer
     */
    public static boolean audioPlaying() {
        return player != null && !player.isComplete();
    }

    /**
     * Returns whether or not any audio has been paused. This is indicated via
     * a value other than 0 for pauseLocaiton. This method is used for ConsoleFrame's
     * audio menu currently.
     *
     * @return whether or not any audio is paused
     */
    public static boolean isPaused() {
        return pauseLocation != 0;
    }

    /**
     * Returns whether or not the audioplayer widget is currently open.
     *
     * @return whether or not the AudioPlayer frame is open
     */
    public static boolean windowOpen() {
        return audioFrame != null;
    }

    /**
     * Refreshes the {@code Port.Info.SPEAKER} and {@code Port.Info.HEADPHONE} volume level.
     */
    public static void refreshAudio() {
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
     * Refreshes the audio list and the audio index in case audio files were added to the working directory.
     * When starting pass the file that the user selected using the select audio directory button.
     * On refresh, you may pass null and the program will infer where to look based on the current audioFile dir.
     * If null is passed without any valid audio files, an IllegalArgumentException will be thrown.
     */
    public static void refreshAudioFiles(File refreshOnFile) {
        //if provided file is null
        if (refreshOnFile == null) {
            //if no audio files to refresh on
            if (currentAudioFiles.isEmpty()) {
                //get the music directory of the user
                File[] userMusicFiles = UserUtil.getUserFile(UserFile.MUSIC.getName()).listFiles();

                if (userMusicFiles.length > 0) {
                    refreshOnFile = userMusicFiles[0];
                } else return;
            } else {
                if (audioIndex > currentAudioFiles.size() - 1) {
                    refreshOnFile = null;
                } else {
                    //audio files exists
                    refreshOnFile = currentAudioFiles.get(audioIndex);
                }
            }
        }

        //wipe the audio files since we're refreshing based on refreshOnFile now
        currentAudioFiles.clear();

        for (File file : refreshOnFile.getParentFile().listFiles())
            if (FileUtil.getExtension(file).equals(".mp3"))
                currentAudioFiles.add(file);

        for (int i = 0; i < currentAudioFiles.size() ; i++) {
            if (currentAudioFiles.get(i).equals(refreshOnFile))
                audioIndex = i;
        }
    }

    /**
     * Pauses the audio if anything is currently playing
     * in preparation to resume at the current location.
     */
    public static void pauseAudio() {
        if (currentAudioFiles.isEmpty())
            return;

        //set last action
        lastAction = LastAction.PAUSE;

        try {
            //figure out where we are
            pauseLocation = totalLength - fis.available() - pauseAudioReactionOffset;

            //careful here to close the player
            if (player != null)
                player.close();
            player = null;

            //set icon and tooltip
            playPauseAudioButton.setIcon(new ImageIcon("static/pictures/music/Play.png"));
            playPauseAudioButton.setToolTipText("Play");

            //revalidate the console audio menu
            ConsoleFrame.getConsoleFrame().revalidateAudioMenu();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Stops the audio and all visual indication threads.
     */
    public static void stopAudio() {
        if (currentAudioFiles.isEmpty())
            return;

        //set last action
        lastAction = LastAction.STOP;

        try {
            //end audio scroll label
            if (scrollingTitleLabel != null)
                scrollingTitleLabel.kill();
            scrollingTitleLabel = null;

            if (windowState == PlayerWindowState.ALBUM_ART)
                enterAlbumArtPlayer();

            //end audio location progress bar
            if (audioLocationBar != null)
                audioLocationBar.kill();
            audioLocationBar = null;

            //reset the progress bar text
            if (audioProgress != null)
                audioProgress.setValue(0);
            audioProgressLabel.setText("");

            //close the player and associated buffers/streams
            if (player != null)
                player.close();
            player = null;
            bis = null;
            fis = null;

            //reset location vars
            pauseLocation = 0;
            totalLength = 0;

            //reset icons/tooltips
            playPauseAudioButton.setIcon(new ImageIcon("static/pictures/music/Play.png"));
            playPauseAudioButton.setToolTipText("Play");

            //revalidate console frame's audio menu
            ConsoleFrame.getConsoleFrame().revalidateAudioMenu();

            //take away the custom icon since no audio is playing nor paused
            if (audioFrame != null) {
                audioFrame.setIconImage(CyderIcons.getCurrentCyderIcon().getImage());
                audioFrame.setUseCustomTaskbarIcon(false);
            }

            //refresh the audio volume
            refreshAudio();

            //refresh the title
            refreshFrameTitle();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Skips to the current audio file's predecesor if it exists in the directory.
     */
    public static void previousAudio() {
        if (currentAudioFiles.isEmpty() || !shouldAllowAction())
            return;

        //refresh files just to be safe
        refreshAudioFiles(null);

        //set last action
        lastAction = LastAction.SKIP;

        try {
            //stop any audio playing
            stopAudio();

            //take into account shuffling to find an audio index
            if (shuffleAudio) {
                int nextAudioIndex = NumberUtil.randInt(0, currentAudioFiles.size() - 1);

                while (nextAudioIndex == audioIndex)
                    nextAudioIndex = NumberUtil.randInt(0, currentAudioFiles.size() - 1);

                audioIndex = nextAudioIndex;
            } else {
                if (audioIndex - 1 > -1) {
                    audioIndex--;
                } else {
                    audioIndex = currentAudioFiles.size() - 1;
                }
            }

            //now that an index has been chosen and set, start the audio again
            startAudio();
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Skips to the current audio file's successor if it exists in the directory.
     */
    public static void nextAudio() {
        if (currentAudioFiles.isEmpty() || !shouldAllowAction())
            return;

        //just to be safe
        refreshAudioFiles(null);

        //set last action
        lastAction = LastAction.SKIP;

        try {
            //stop any audio playing
            stopAudio();

            //find an audio index
            if (shuffleAudio) {
                int nextAudioIndex = NumberUtil.randInt(0, currentAudioFiles.size() - 1);
                while (nextAudioIndex == audioIndex)
                    nextAudioIndex = NumberUtil.randInt(0, currentAudioFiles.size() - 1);

                audioIndex = nextAudioIndex;

            } else {
                if (audioIndex + 1 < currentAudioFiles.size()) {
                    audioIndex++;
                } else {
                    audioIndex = 0;
                }
            }

            //now that an audio index has been found, play audio
            startAudio();
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Kills all threads and resets all variables to their defaults
     * before invoking dispose on the audio frame.
     */
    public static void killWidget() {
        stopAudio();

        //player ending calls
        if (player != null)
            player.close();

        //scrolllabel ending calls
        if (scrollingTitleLabel != null)
            scrollingTitleLabel.kill();

        //location progress bar ending calls
        if (audioLocationBar != null)
            audioLocationBar.kill();

        //null sets
        player = null;
        scrollingTitleLabel = null;
        audioLocationBar = null;
        currentAudioFiles.clear();

        //default stes
        audioProgress.setValue(0);
        audioProgressLabel.setText("");

        //exiting widget
        if (audioFrame != null && !audioFrame.isDisposed())
            audioFrame.dispose();
        audioFrame = null;

        if (!IOUtil.generalAudioPlaying())
            ConsoleFrame.getConsoleFrame().animateOutAndRemoveAudioControls();
    }

    /**
     * Starts playing audio from the current index.
     */
    public static void startAudio() {
        //invoke thread since we are starting playing from the beginning of the current audio index
        CyderThreadRunner.submit(() -> {
            try {
                //make sure files are in order
                refreshAudioFiles(null);

                //refresh the audio levels
                refreshAudio();

                //initialize file input stream and buffered input stream
                fis = new FileInputStream(currentAudioFiles.get(audioIndex));
                bis = new BufferedInputStream(fis);

                //close player if it isn't null and it is playing
                if (player != null && !player.isComplete())
                    player.close();

                //kill the audio scroll label if running
                if (scrollingTitleLabel != null)
                    scrollingTitleLabel.kill();

                //kill audio location progress bar if running
                if (audioLocationBar != null)
                    audioLocationBar.kill();

                //these occasionally throw NullPtrExep if the user spams buttons so we'll ignore that
                //noinspection CatchMayIgnoreException
                try {
                    //initialize player
                    player = new Player(bis);

                    //get the length
                    totalLength = fis.available();
                } catch (Exception e) {
                    if (!(e instanceof  NullPointerException)) {
                        ExceptionHandler.handle(e);
                    }
                }

                //reset pause location
                pauseLocation = 0;

                //if not in mini player mode, initalize these views
                if (windowState != PlayerWindowState.MINI) {
                    audioTitleLabel.setText(StringUtil.capsFirst(FileUtil.getFilename(currentAudioFiles.get(audioIndex))));
                    scrollingTitleLabel = new ScrollingTitleLabel(audioTitleLabel);
                    audioLocationBar = new AudioLocationBar(audioProgress);
                }

                //set the play/pause icons
                playPauseAudioButton.setIcon(new ImageIcon("static/pictures/music/Pause.png"));
                playPauseAudioButton.setToolTipText("Pause");
                ConsoleFrame.getConsoleFrame().revalidateAudioMenu();

                //album art if possible
                if (refreshAlbumArt()) {
                    audioFrame.setIconImage(currentAlbumArt.getImage());
                    audioFrame.setCustomTaskbarIcon(currentAlbumArt);
                    audioFrame.setUseCustomTaskbarIcon(true);
                } else {
                    audioFrame.setIconImage(CyderIcons.getCurrentCyderIcon().getImage());
                    audioFrame.setUseCustomTaskbarIcon(false);
                }

                if (windowState == PlayerWindowState.ALBUM_ART)
                    enterAlbumArtPlayer();

                //set last action so we know how to handle future actions
                lastAction = LastAction.PLAY;

                //refresh the frame title based on the new audio that's playing
                refreshFrameTitle();

                //log the audio we're playing
                Logger.log(LoggerTag.AUDIO,
                        "[AUDIO PLAYER] " + currentAudioFiles.get(audioIndex).getName());

                //playing blocks until the audio finishes
                try {
                    player.play();
                } catch (JavaLayerException jle) {
                    //this occasionally throws so if it does, invoke method again
                    startAudio();
                }

                //player has concluded at this point

                if (audioLocationBar != null)
                    audioLocationBar.kill();

                //based on the last action, determine if to play next audio
                if (lastAction != LastAction.PAUSE && lastAction != LastAction.STOP) {
                    //close resources as if stop had been called
                    stopAudio();

                    //ensure audio files are up to date
                    refreshAudioFiles(null);

                    //update audio levels
                    refreshAudio();


                    if (repeatAudio) {
                        startAudio();
                    }
                    //if there's stuff in the queue then we need to play it
                    else if (!queue.isEmpty()) {
                        String playPath = queue.remove(0).getAbsolutePath();

                        for (int i = 0; i < currentAudioFiles.size() ; i++) {
                            if (currentAudioFiles.get(i).getAbsolutePath().equalsIgnoreCase(playPath)) {
                                audioIndex = i;
                                break;
                            }
                        }

                        startAudio();
                    }
                    //shuffle audio if needed
                    else if (shuffleAudio) {
                        int newIndex = NumberUtil.randInt(0, currentAudioFiles.size() - 1);
                        while (newIndex == audioIndex)
                            newIndex = NumberUtil.randInt(0, currentAudioFiles.size() - 1);

                        audioIndex = newIndex;
                        startAudio();
                    }
                    //increment audio if available
                    else if (audioIndex + 1 < currentAudioFiles.size()) {
                        audioIndex++;
                        startAudio();
                    }
                    //if out of range, loop back around to beginning
                    else if (currentAudioFiles.size() > 1) {
                        //loop back around to the beginning as long as more than one song
                        audioIndex = 0;
                        startAudio();
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        },DEFAULT_TITLE + " Audio Thread [" + FileUtil.getFilename(currentAudioFiles.get(audioIndex)) + "]");
    }

    /**
     * Resumes audio at of current audio file
     * at the previously paused position.
     */
    public static void resumeAudio() {
        if (currentAudioFiles.isEmpty())
            return;

        resumeAudio(pauseLocation);
    }

    /**
     * Resumes audio at the current audio file at the passed in byte value.
     *
     * @param startPosition the byte value to skip to when starting the audio
     */
    public static void resumeAudio(long startPosition) {
        if (currentAudioFiles.isEmpty())
            return;

        if (lastAction == LastAction.STOP) {
            startAudio();
        } else if (lastAction == LastAction.PAUSE) {
            lastAction = LastAction.RESUME;
            CyderThreadRunner.submit(() -> {
                try {
                    refreshAudioFiles(null);
                    refreshAudio();
                    fis = new FileInputStream(currentAudioFiles.get(audioIndex));
                    totalLength = fis.available();
                    //in case for some weird reason startPosition is before the file then we set startPosition to 0
                    fis.skip(startPosition < 0 ? 0 : startPosition);
                    bis = new BufferedInputStream(fis);

                    if (player != null)
                        player.close();
                    player = null;

                    if (audioLocationBar != null)
                        audioLocationBar.kill();
                    audioLocationBar = null;

                    //these occasionally throws NullPtrExep if the user spams buttons so we'll ignore that
                    try {
                        player = new Player(bis);
                    } catch (Exception ignored) {}

                    if (windowState != PlayerWindowState.MINI) {
                        audioLocationBar = new AudioLocationBar(audioProgress);
                    }

                    playPauseAudioButton.setIcon(new ImageIcon("static/pictures/music/Pause.png"));
                    playPauseAudioButton.setToolTipText("Pause");
                    ConsoleFrame.getConsoleFrame().revalidateAudioMenu();

                    lastAction = LastAction.PLAY;

                    refreshFrameTitle();

                    Logger.log(LoggerTag.AUDIO,"[AUDIO PLAYER] " + currentAudioFiles.get(audioIndex).getName());

                    try {
                        player.play();
                    } catch (Exception ignored) {
                        resumeAudio(pauseLocation);
                    }

                    if (audioLocationBar != null)
                        audioLocationBar.kill();
                    audioLocationBar = null;

                    if (lastAction != LastAction.PAUSE && lastAction != LastAction.STOP) {
                        stopAudio();
                        refreshAudioFiles(null);
                        refreshAudio();

                        if (!queue.isEmpty()) {
                            String playPath = queue.remove(0).getAbsolutePath();

                            for (int i = 0; i < currentAudioFiles.size() ; i++) {
                                if (currentAudioFiles.get(i).getAbsolutePath().equalsIgnoreCase(playPath)) {
                                    audioIndex = i;
                                    break;
                                }
                            }

                            startAudio();
                        } else if (repeatAudio) {
                            startAudio();
                        } else if (shuffleAudio) {
                            int newIndex = NumberUtil.randInt(0, currentAudioFiles.size() - 1);
                            while (newIndex == audioIndex)
                                newIndex = NumberUtil.randInt(0, currentAudioFiles.size() - 1);

                            audioIndex = newIndex;
                            startAudio();
                        } else if (audioIndex + 1 < currentAudioFiles.size()) {
                            audioIndex++;
                            startAudio();
                        } else {
                            //loop back around to the beginning
                            audioIndex = 0;
                            startAudio();
                        }
                    }

                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            },DEFAULT_TITLE + " Audio Thread [" + FileUtil.getFilename(currentAudioFiles.get(audioIndex)) + "]");
        }
    }

    /**
     * Private inner class for the audio location progress bar and label.
     */
    private static class AudioLocationBar {
        boolean update;
        DecimalFormat format = new DecimalFormat("##.#");

        AudioLocationBar(CyderProgressBar effectBar) {
            update = true;
            audioProgress.setStringPainted(true);
            try {
                CyderThreadRunner.submit( () -> {
                    while (update) {
                        try {
                            if (totalLength == 0 || fis == null || !audioProgress.isVisible())
                                return;

                            double place = ((double) (totalLength - fis.available()) /
                                    (double) totalLength) * audioProgress.getMaximum();
                            audioProgress.setValue((int) place);

                            double percentIn = (((double) audioProgress.getValue() /
                                    (double) audioProgress.getMaximum()));
                            double percentLeft = 1.0 - percentIn;

                            float totalMilis = audioFileDuration(currentAudioFiles.get(audioIndex));

                            int totalSeconds = (int) (totalMilis / 1000.0);
                            int secondsIn = (int) (percentIn * totalSeconds);

                            int secondsLeft = totalSeconds - secondsIn;

                            if (UserUtil.getUserData("audiolength").equals("1")) {
                                audioProgressLabel.setText(formatSeconds(secondsIn) + " played, "
                                        + formatSeconds(totalMilis / 1000.0) + " total");
                            } else {
                                audioProgressLabel.setText(formatSeconds(secondsIn) + " played, "
                                        + formatSeconds(secondsLeft) + " left");
                            }

                            Thread.sleep(250);
                        } catch (IOException ignored) {
                            // straight up ignored since sometimes this
                            // throws every second due to the stream closing
                        } catch (InterruptedException e) {
                            // no one really cares about this but still
                            ExceptionHandler.handle(e);
                        }
                    }
                },DEFAULT_TITLE + " Progress Thread [" + FileUtil.getFilename(currentAudioFiles.get(audioIndex)) + "]");
            } catch (Exception e) {
                ExceptionHandler.silentHandle(e);
            }
        }

        public void kill() {
            update = false;
        }
    }

    /**
     * Private inner class for the scrolling audio label.
     */
    private static class ScrollingTitleLabel {
        boolean scroll;

        ScrollingTitleLabel(JLabel effectLabel) {
            scroll = true;

            try {
                String localTitle = StringUtil.capsFirst(FileUtil.getFilename(currentAudioFiles.get(audioIndex).getName()));
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
                                    if (!scroll)
                                        break;

                                    effectLabel.setLocation(effectLabel.getX() - 1, effectLabel.getY());
                                    Thread.sleep(miliTimeout);
                                    goBack++;
                                }

                                sleepWithChecks(milipause);

                                while (goBack > 0) {
                                    if (!scroll)
                                        break;

                                    effectLabel.setLocation(effectLabel.getX() + 1, effectLabel.getY());
                                    Thread.sleep(miliTimeout);
                                    goBack--;
                                }

                                sleepWithChecks(milipause);
                            }
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }
                    },DEFAULT_TITLE + " scrolling title thread ["
                            + FileUtil.getFilename(currentAudioFiles.get(audioIndex)) + "]");
                } else {
                    String text = StringUtil.capsFirst(FileUtil.getFilename(currentAudioFiles.get(audioIndex)));
                    effectLabel.setText(text);
                    effectLabel.setLocation(effectLabel.getParent().getWidth() / 2
                            - StringUtil.getAbsoluteMinWidth(text, effectLabel.getFont()) / 2, effectLabel.getY());
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }

        /**
         * Sleeps for the designated amount of time, breaking every 50ms to check for a stop call.
         *
         * @param sleepTime the total length to sleep for
         */
        public void sleepWithChecks(long sleepTime) {
            try {
                long acc = 0;

                while (acc < sleepTime) {
                    Thread.sleep(50);
                    acc += 50;

                    if (!scroll)
                        break;
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }

        public void kill() {
            scroll = false;
        }
    }

    /**
     * Increments to the next window state.
     */
    public static void incrementWindowState() {
        switch (windowState) {
            case ALBUM_ART:
                windowState = PlayerWindowState.DEFAULT;
                enterDefaultPlayer();
                break;
            case DEFAULT:
                windowState = PlayerWindowState.MINI;
                enterMiniPlayer();
                break;
            case MINI:
                windowState = PlayerWindowState.ALBUM_ART;
                enterAlbumArtPlayer();
                break;
            default:
                throw new IllegalArgumentException("Illegal window state: " + windowState);
        }
    }

    /**
     * Sets the AudioPlayer to mini mode.
     */
    public static void enterMiniPlayer() {
        if (scrollingTitleLabel != null)
            scrollingTitleLabel.kill();
        scrollingTitleLabel = null;

        if (audioLocationBar != null)
            audioLocationBar.kill();
        audioLocationBar = null;

        audioProgress.setVisible(false);
        audioVolumeSlider.setVisible(false);
        audioTitleLabel.setVisible(false);

        audioFrame.setSize(500,100);

        selectAudioDirButton.setLocation(selectAudioDirButton.getX(), 50);
        loopAudioButton.setLocation(loopAudioButton.getX(), 50);
        previousAudioButton.setLocation(previousAudioButton.getX(), 50);
        stopAudioButton.setLocation(stopAudioButton.getX(), 50);
        playPauseAudioButton.setLocation(playPauseAudioButton.getX(), 50);
        nextAudioButton.setLocation(nextAudioButton.getX(), 50);
        shuffleAudioButton.setLocation(shuffleAudioButton.getX(), 50);

        albumArtLabel.setVisible(false);
    }

    /**
     * Enters the default mode for the audio player.
     */
    public static void enterDefaultPlayer() {
        audioFrame.setSize(500,240);
        albumArtLabel.setVisible(false);

        // just to be safe
        audioProgress.setVisible(true);
        audioVolumeSlider.setVisible(true);
        audioTitleLabel.setVisible(true);

        audioProgress.setBounds(55, 190, 385, 35);
        audioProgressLabel.setBounds(55, 190, 385, 35);
        audioVolumeSlider.setBounds(55, 155, 385, 40);
        shuffleAudioButton.setBounds(405, 105, 30, 30);
        nextAudioButton.setBounds(355, 105, 30, 30);
        playPauseAudioButton.setBounds(295, 105, 30, 30);
        stopAudioButton.setBounds(235, 105, 30, 30);
        previousAudioButton.setBounds(175, 105, 30, 30);
        loopAudioButton.setBounds(115, 105, 30, 30);
        selectAudioDirButton.setBounds(55, 105, 30, 30);
        audioTitleLabelContainer.setBounds(100, 40, 300, 30);
    }

    /**
     * Enters the album art window state.
     */
    public static void enterAlbumArtPlayer() {
        // if no art, increment to default player and let it handle
        if (currentAlbumArt == null) {
            incrementWindowState();
        } else {
            refreshAlbumArt();

            audioFrame.setSize(500,480);
            albumArtLabel.setBounds((audioFrame.getWidth() - albumArtLen) / 2, 60, albumArtLen, albumArtLen);
            albumArtLabel.setIcon(ImageUtil.resizeImage(currentAlbumArt, albumArtLen, albumArtLen));
            albumArtLabel.setVisible(true);

            audioProgress.setVisible(true);
            audioVolumeSlider.setVisible(true);
            audioTitleLabel.setVisible(true);

            // new audio location progress bar
            audioLocationBar = new AudioLocationBar(audioProgress);

            int yIncrement = 230;

            audioTitleLabelContainer.setBounds(100, 40 + yIncrement, 300, 30);

            audioProgress.setBounds(55, 190 + yIncrement, 385, 35);
            audioProgressLabel.setBounds(55, 190 + yIncrement, 385, 35);

            audioVolumeSlider.setBounds(55, 155 + yIncrement, 385, 40);
            shuffleAudioButton.setBounds(405, 105 + yIncrement, 30, 30);
            nextAudioButton.setBounds(355, 105 + yIncrement, 30, 30);
            playPauseAudioButton.setBounds(295, 105 + yIncrement, 30, 30);
            stopAudioButton.setBounds(235, 105 + yIncrement, 30, 30);
            previousAudioButton.setBounds(175, 105 + yIncrement, 30, 30);
            loopAudioButton.setBounds(115, 105 + yIncrement, 30, 30);
            selectAudioDirButton.setBounds(55, 105 + yIncrement, 30, 30);

            //end audio scroll label
            if (scrollingTitleLabel != null)
                scrollingTitleLabel.kill();
            scrollingTitleLabel = new ScrollingTitleLabel(audioTitleLabel);
        }
    }

    /**
     * Returns the current audio file at the current audio index if found, null else.
     *
     * @return the current audio file at the current audio index if found, null else
     */
    public static File getCurrentAudio() {
        if (currentAudioFiles.isEmpty() || lastAction == LastAction.STOP)
            return null;
        else
            return currentAudioFiles.get(audioIndex);
    }

    /**
     * Get's the total duration of an audio file.
     *
     * @param audioFile the provided audio file
     * @return the time in ms that it takes to comlete the audio file
     */
    public static float audioFileDuration(File audioFile) {
        float milisRet = 0;

        try {
            Header h;
            FileInputStream fis = new FileInputStream(audioFile);
            Bitstream bitstream = new Bitstream(fis);
            h = bitstream.readFrame();

            int size = h.calculate_framesize();
            float ms_per_frame = h.ms_per_frame();
            int maxSize = h.max_number_of_frames(10000);
            float t = h.total_ms(size);
            long tn;
            tn = fis.getChannel().size();

            int min = h.min_number_of_frames(500);
            milisRet = h.total_ms((int) tn);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return milisRet;
    }

    /**
     * Formats the provided number of seconds into an a String representation
     * in the format: 3h 2m 14s (thge final second value is rounded up).
     *
     * @param seconds the amount of seconds to convert to String
     * @return the provided seconds amount formatted to String
     */
    public static String formatSeconds(double seconds) {
        StringBuilder sb = new StringBuilder();

        int minutes = 0;
        int hours = 0;

        while (seconds >= 60) {
            minutes++;
            seconds -= 60.0;
        }

        while (minutes >= 60) {
            hours++;
            minutes -= 60.0;
        }

        if (hours > 0) {
            sb.append(hours).append("h ");
        }

        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }

        sb.append((int) Math.ceil(seconds)).append("s");

        return sb.toString();
    }

    /**
     * Refreshes the currentAlbumArt ImageIcon based on the current audio at audioIndex if
     * an album art file exists with the same name as the audio file.
     *
     * @return boolean describing whether or not album art exists
     */
    public static boolean refreshAlbumArt() {
        try {
            if (currentAudioFiles.isEmpty())
                return false;

            String currentName = FileUtil.getFilename(currentAudioFiles.get(audioIndex));

            File albumArtDir = new File("dynamic/users/"
                    + ConsoleFrame.getConsoleFrame().getUUID() + "/Music/AlbumArt");

            if (!albumArtDir.exists() || !albumArtDir.isDirectory()) {
                currentAlbumArt = null;
                return false;
            }

            //for all the album arts
            for (File f : albumArtDir.listFiles()) {
                if (FileUtil.getFilename(f).equals(currentName)) {
                    currentAlbumArt = new ImageIcon(ImageIO.read(f));
                    return true;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        currentAlbumArt = null;
        return false;
    }

    /**
     * Refreshes the frame's painted title, super title, and console frame menu name.
     */
    public static void refreshFrameTitle() {
        if (lastAction == LastAction.STOP) {
            audioFrame.setTitle(DEFAULT_TITLE);
        } else {
            audioFrame.setTitle(StringUtil.capsFirst(FileUtil.getFilename(currentAudioFiles.get(audioIndex).getName())));
        }

        ConsoleFrame.getConsoleFrame().revalidateMenu();
    }

    /**
     * Determines whether or not the button click should be a
     * llowed based on how long ago the last click was.
     *
     * @return whether the button click should be allowed to to pass
     */
    private static boolean shouldAllowAction() {
        if (System.currentTimeMillis() - lastActionTime > actionTimeoutMS) {
            lastActionTime = System.currentTimeMillis();
            return true;
        } else return false;
    }
}