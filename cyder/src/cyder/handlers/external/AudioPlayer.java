package cyder.handlers.external;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.AnimationDirection;
import cyder.enums.SliderShape;
import cyder.genesis.CyderCommon;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.SessionHandler;
import cyder.ui.*;
import cyder.utilities.*;
import cyder.widgets.WidgetBase;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;

//todo java doc this class before closing <3

//todo throwing results from refreshing on files, limit the rate at which a user can press to say 100ms or so?

public class AudioPlayer implements WidgetBase {
    //last actions needed for logic
    private enum LastAction {
        SKIP,PAUSE,STOP,RESUME,PLAY
    }

    //current last action
    private static LastAction lastAction;

    //ui components
    private static ImprovedScrollLabel audioScroll;
    private static AudioLocation audioLocation;
    private static JLabel audioTitleLabel;
    private static CyderFrame audioFrame;
    private static JSlider audioVolumeSlider;
    private static CyderProgressBar audioProgress;
    private static JButton previousAudioButton;
    private static JButton nextAudioButton;
    private static JButton stopAudioButton;
    private static JButton loopAudioButton;
    private static JButton selectAudioDirButton;
    private static JButton playPauseAudioButton;
    private static JButton shuffleAudioButton;
    private static JLabel audioProgressLabel;

    //audio booleans
    private static boolean shuffleAudio;
    private static boolean repeatAudio;
    private static boolean miniPlayer;

    //current audio
    private static int audioIndex;
    private static ArrayList<File> audioFiles;

    private static final int pauseAudioReactionOffset = 10000;

    //album art
    private static ImageIcon currentAlbumArt;

    //JLayer objects
    private static Player player;
    private static BufferedInputStream bis;
    private static FileInputStream fis;

    //audio location vars
    private static long pauseLocation;
    private static long totalLength;

    private static final String DEFAULT_TITLE = "Flash Player";
    //todo variable for default label text too

    /**
     * The time the user last pressed a button.
     */
    private static long lastActionTime; //todo utilize me

    //private constructor since only one player per Cyder instance
    private AudioPlayer() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * Method for widget finder to invoke by using reflection to find the Widget annotation
     */
    @Widget(trigger = {"mp3", "music"}, description = "An audio playing widget")
    public static void showGUI() {
        showGUI(null);
    }

    /**
     * Constructor that launches the AudioPlayer
     * @param startPlaying the audio file to start playing upon successful launch of the AudioPlayer.
     * Pass {@code null} to avoid starting audio upon launch.
     */
    public static void showGUI(File startPlaying) {
        SessionHandler.log(SessionHandler.Tag.WIDGET_OPENED, "AUDIO PLAYER");

        queue = new LinkedList<>();

        if (audioFrame != null) {
            stopAudio();
            refreshAudioFiles(startPlaying);

            if (audioFiles.size() != 0)
                startAudio();

            return;
        }

        if (startPlaying == null) {
            refreshAudioFiles(startPlaying);
        }

        if (IOUtil.generalAudioPlaying())
            IOUtil.stopAudio();

        Color backgroundColor = new Color(8,23,52);
        audioFrame = new CyderFrame(500,225,
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
                     audioFiles = null;
                     audioIndex = -1;
                 }
             }
        );

        audioFrame.initializeResizing();
        audioFrame.setResizable(true);
        audioFrame.setMinimumSize(new Dimension(500, 155));
        audioFrame.setMaximumSize(new Dimension(500, 225));

        JButton changeSize = new JButton("");
        changeSize.setToolTipText("Toggle Miniplayer");
        changeSize.addActionListener(e -> {
            if (!miniPlayer) {
                enterMiniPlayer();
            } else {
                exitMiniPlayer();
            }
            miniPlayer = !miniPlayer;
        });
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

        JLabel audioTitleLabelContainer = new JLabel();
        audioTitleLabelContainer.setBounds(100, 40, 300, 30);
        audioFrame.getContentPane().add(audioTitleLabelContainer);

        audioTitleLabel = new JLabel();
        audioTitleLabel.setToolTipText("Currently Playing");
        audioTitleLabel.setFont(new Font("tahoma", Font.BOLD, 18));
        audioTitleLabel.setForeground(CyderColors.vanila);
        audioTitleLabel.setText("No Audio Playing");
        audioTitleLabelContainer.add(audioTitleLabel);

        audioTitleLabel.setBounds(audioTitleLabel.getParent().getWidth() / 2
                - CyderFrame.getAbsoluteMinWidth(audioTitleLabel.getText(),
                audioTitleLabel.getFont()) / 2, audioTitleLabel.getY(),
                CyderFrame.getMinWidth(audioTitleLabel.getText(), audioTitleLabel.getFont()),
                audioTitleLabel.getParent().getHeight());

        selectAudioDirButton = new JButton("");
        selectAudioDirButton.setFocusPainted(false);
        selectAudioDirButton.setOpaque(false);
        selectAudioDirButton.setContentAreaFilled(false);
        selectAudioDirButton.setBorderPainted(false);
        selectAudioDirButton.setToolTipText("Select audio");
        selectAudioDirButton.addActionListener(e -> new Thread(() -> {
            try {
                File selectedChildFile = new GetterUtil().getFile("Choose any mp3 file to startAudio");
                if (selectedChildFile != null) {
                    if (!selectedChildFile.toString().endsWith("mp3")) {
                        audioFrame.notify("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", but that's not an mp3 file.");
                    } else if (selectedChildFile != null){
                        stopAudio();
                        refreshAudioFiles(selectedChildFile);
                        startAudio();
                    }
                }
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
                ExceptionHandler.handle(ex);
            } finally {
                refreshAudioFiles(null);
            }
        }, "wait thread for GetterUtil().getFile()").start());

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

        selectAudioDirButton.setBounds(55, 105, 30, 30);
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

        loopAudioButton.setBounds(115, 105, 30, 30);
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

        previousAudioButton.setBounds(175, 105, 30, 30);
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

        stopAudioButton.setBounds(235, 105, 30, 30);
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

        playPauseAudioButton.setBounds(295, 105, 30, 30);
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

        nextAudioButton.setBounds(355, 105, 30, 30);
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

        shuffleAudioButton.setBounds(405, 105, 30, 30);
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
        audioVolumeSlider.setBounds(55, 155, 385, 40);
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
        audioProgressLabel.setFont(CyderFonts.defaultFontSmall.deriveFont(20f));
        audioProgressLabel.setForeground(CyderColors.vanila);
        audioProgressLabel.setFocusable(false);
        audioProgressLabel.setBounds(55, 190, 385, 25);
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
        CyderProgressUI ui = new CyderProgressUI();
        ui.setColors(new Color[]{CyderColors.regularPink, CyderColors.tooltipForegroundColor});
        ui.setAnimationDirection(AnimationDirection.LEFT_TO_RIGHT);
        ui.setShape(CyderProgressUI.Shape.SQUARE);
        audioProgress.setUI(ui);
        audioProgress.setMinimum(0);
        audioProgress.setMaximum(10000);
        audioProgress.setBorder(new LineBorder(Color.black, 2));
        audioProgress.setBounds(55, 190, 385, 25);
        audioProgress.setVisible(true);
        audioProgress.setValue(0);
        audioProgress.setOpaque(false);
        audioProgress.setToolTipText("Audio Location");
        audioProgress.setFocusable(false);
        audioProgress.repaint();
        audioFrame.getContentPane().add(audioProgress);

        audioFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
        audioFrame.setVisible(true);
        audioFrame.requestFocus();

        if (startPlaying != null && StringUtil.getExtension(startPlaying).equals(".mp3")) {
            refreshAudioFiles(startPlaying);
            startAudio();
        } else {
            try {
                File userAudioDir = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Music/" );

                if (!userAudioDir.exists()) {
                    userAudioDir.mkdir();
                    return;
                }

                File[] userFiles = userAudioDir.listFiles();

                if (userFiles.length == 0) {
                    return;
                }

                for (File f : userFiles) {
                    if (StringUtil.getExtension(f).equals(".mp3"))
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
     * Adds the given file to the queue. If the player is not open, then it plays the requested audio.
     * @param audioFile the audio to play
     */
    public static void addToMp3Queue(File audioFile) {
        if (audioPlaying()) {
            addToQueue(audioFile);
        } else if (windowOpen()){
            refreshAudioFiles(audioFile);
            startAudio();
        } else {
            showGUI(audioFile);
        }
    }

    /**
     * Returns the associated JLayer player
     */
    public static Player getPlayer() {
        return player;
    }

    /**
     * Determines whether or not the audio widget is currently playing audio.
     * If player is closed, then player is set to null so this will always work.
     * @return whether or not audio is playing via the AudioPlayer
     */
    public static boolean audioPlaying() {
        return player != null && !player.isComplete();
    }

    /**
     * Returns whether or not any audio has been paused. This is indicated via
     * a value other than 0 for pauseLocaiton. This method is used for ConsoleFrame's
     * audio menu currently.
     * @return whether or not any audio is paused
     */
    public static boolean isPaused() {
        return pauseLocation != 0;
    }

    /**
     * Returns whether or not the audioplayer widget is currently open
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
        //if audio files have not been created, create it
        if (audioFiles == null)
            audioFiles = new ArrayList<>();

        //if provided file is null
        if (refreshOnFile == null) {
            //if no audio files to refresh on
            if (audioFiles.size() == 0) {
                //get the music directory of the user
                File[] userMusicFiles = UserUtil.getUserMusicDir().listFiles();

                if (userMusicFiles.length > 0) {
                    refreshOnFile = userMusicFiles[0];
                } else {
                    refreshOnFile = null;
                    return;
                }
            } else {
                if (audioIndex > audioFiles.size() - 1) {
                    refreshOnFile = null;
                } else {
                    //audio files exists
                    refreshOnFile = audioFiles.get(audioIndex);
                }
            }
        }

        //wipe the audio files since we're refreshing based on refreshOnFile now
        audioFiles.clear();

        for (File file : refreshOnFile.getParentFile().listFiles())
            if (StringUtil.getExtension(file).equals(".mp3"))
                audioFiles.add(file);

        for (int i = 0; i < audioFiles.size() ; i++) {
            if (audioFiles.get(i).equals(refreshOnFile))
                audioIndex = i;
        }

        if (audioFiles.size() == 0)
            audioFiles = null;
    }

    /**
     * Pauses the audio if anything is currently playing
     * in preparation to resume at the current location.
     */
    public static void pauseAudio() {
        if (audioFiles.size() == 0)
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
        if (audioFiles.size() == 0)
            return;

        //set last action
        lastAction = LastAction.STOP;

        try {
            //end audio scroll label
            if (audioScroll != null)
                audioScroll.kill();
            audioScroll = null;

            //reset audio title now that scrolling has ended
            if (audioTitleLabel != null) {
                audioTitleLabel.setText("No Audio Playing");
                audioTitleLabel.setBounds(audioTitleLabel.getParent().getWidth() / 2
                                - CyderFrame.getAbsoluteMinWidth(audioTitleLabel.getText(),
                                audioTitleLabel.getFont()) / 2, audioTitleLabel.getY(),
                        CyderFrame.getMinWidth(audioTitleLabel.getText(), audioTitleLabel.getFont()),
                        audioTitleLabel.getParent().getHeight());
            }

            //end audio location progress bar
            if (audioLocation != null)
                audioLocation.kill();
            audioLocation = null;

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
                audioFrame.setIconImage(SystemUtil.getCurrentCyderIcon().getImage());
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
        if (audioFiles.size() == 0)
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
                int nextAudioIndex = NumberUtil.randInt(0, audioFiles.size() - 1);

                while (nextAudioIndex == audioIndex)
                    nextAudioIndex = NumberUtil.randInt(0, audioFiles.size() - 1);

                audioIndex = nextAudioIndex;
            } else {
                if (audioIndex - 1 > -1) {
                    audioIndex--;
                } else {
                    audioIndex = audioFiles.size() - 1;
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
        if (audioFiles.size() == 0)
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
                int nextAudioIndex = NumberUtil.randInt(0, audioFiles.size() - 1);
                while (nextAudioIndex == audioIndex)
                    nextAudioIndex = NumberUtil.randInt(0, audioFiles.size() - 1);

                audioIndex = nextAudioIndex;

            } else {
                if (audioIndex + 1 < audioFiles.size()) {
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
     * before invoking dispose on the audio frame
     */
    public static void killWidget() {
        stopAudio();

        //player ending calls
        if (player != null)
            player.close();

        //scrolllabel ending calls
        if (audioScroll != null)
            audioScroll.kill();

        //location progress bar ending calls
        if (audioLocation != null)
            audioLocation.kill();

        //null sets
        player = null;
        audioScroll = null;
        audioLocation = null;
        audioFiles = null;

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
        new Thread(() -> {
            try {
                refreshAudioFiles(null);
                refreshAudio();
                fis = new FileInputStream(audioFiles.get(audioIndex));
                bis = new BufferedInputStream(fis);

                if (player != null)
                    player.close();
                player = null;

                if (audioScroll != null)
                    audioScroll.kill();
                audioScroll = null;

                if (audioLocation != null)
                    audioLocation.kill();
                audioLocation = null;

                //these occasionally throw NullPtrExep if the user spams buttons so we'll ignore that
                try {
                    player = new Player(bis);
                    totalLength = fis.available();
                } catch (Exception ignored) {}

                pauseLocation = 0;

                if (!miniPlayer) {
                    audioTitleLabel.setText(StringUtil.getFilename(audioFiles.get(audioIndex)));
                    audioScroll = new ImprovedScrollLabel(audioTitleLabel);
                    audioLocation = new AudioLocation(audioProgress);
                }

                playPauseAudioButton.setIcon(new ImageIcon("static/pictures/music/Pause.png"));
                playPauseAudioButton.setToolTipText("Pause");
                ConsoleFrame.getConsoleFrame().revalidateAudioMenu();

                //album art if possible
                if (refreshAlbumArt()) {
                    audioFrame.setIconImage(currentAlbumArt.getImage());
                    audioFrame.setCustomTaskbarIcon(currentAlbumArt);
                    audioFrame.setUseCustomTaskbarIcon(true);
                } else {
                    audioFrame.setIconImage(SystemUtil.getCurrentCyderIcon().getImage());
                    audioFrame.setUseCustomTaskbarIcon(false);
                }

                lastAction = LastAction.PLAY;

                refreshFrameTitle();

                SessionHandler.log(SessionHandler.Tag.ACTION,
                        "[AUDIO PLAYER] " + audioFiles.get(audioIndex).getName());
                //on spam of skip button, music player hangs for about 10 seconds
                // and throws an error then catches up eventually
                player.play();

                //after player concludes the following lines of code are executed

                if (audioLocation != null)
                    audioLocation.kill();
                audioLocation = null;

                //we end up here if player is ended
                if (lastAction == LastAction.PAUSE || lastAction == LastAction.STOP) {
                    //paused/stopped for a reason so do nothing as of now
                } else {
                    stopAudio();
                    refreshAudioFiles(null);
                    refreshAudio();

                    if (!queue.isEmpty()) {
                        String playPath = queue.pop().getAbsolutePath();

                        for (int i = 0 ; i < audioFiles.size() ; i++) {
                            if (audioFiles.get(i).getAbsolutePath().equalsIgnoreCase(playPath)) {
                                audioIndex = i;
                                break;
                            }
                        }

                        startAudio();
                    } else if (repeatAudio) {
                        startAudio();
                    } else if (shuffleAudio) {
                        int newIndex = NumberUtil.randInt(0, audioFiles.size() - 1);
                        while (newIndex == audioIndex)
                            newIndex = NumberUtil.randInt(0, audioFiles.size() - 1);

                        audioIndex = newIndex;
                        startAudio();
                    } else if (audioIndex + 1 < audioFiles.size()) {
                        audioIndex++;
                        startAudio();
                    } else if (audioFiles.size() > 1) {
                        //loop back around to the beginning as long as more than one song
                        audioIndex = 0;
                        startAudio();
                    }
                }

            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        },DEFAULT_TITLE + " Audio Thread[" + StringUtil.getFilename(audioFiles.get(audioIndex)) + "]").start();
    }

    /**
     * Resumes audio at the current audio file at the previously paused position.
     */
    public static void resumeAudio() {
        resumeAudio(pauseLocation);
    }

    /**
     * Resumes audio at the current audio file at the passed in byte value.
     * @param startPosition the byte value to skip to when starting the audio
     */
    public static void resumeAudio(long startPosition) {
        if (audioFiles.size() == 0)
            return;

        if (lastAction == LastAction.STOP) {
            startAudio();
        } else if (lastAction == LastAction.PAUSE) {
            lastAction = LastAction.RESUME;
            new Thread(() -> {
                try {
                    refreshAudioFiles(null);
                    refreshAudio();
                    fis = new FileInputStream(audioFiles.get(audioIndex));
                    totalLength = fis.available();
                    //in case for some weird reason startPosition is before the file then we set startPosition to 0
                    fis.skip(startPosition < 0 ? 0 : startPosition);
                    bis = new BufferedInputStream(fis);

                    if (player != null)
                        player.close();
                    player = null;

                    if (audioLocation != null)
                        audioLocation.kill();
                    audioLocation = null;

                    //these occasionally throws NullPtrExep if the user spams buttons so we'll ignore that
                    try {
                        player = new Player(bis);
                    } catch (Exception ignored) {}

                    if (!miniPlayer) {
                        audioLocation = new AudioLocation(audioProgress);
                    }

                    playPauseAudioButton.setIcon(new ImageIcon("static/pictures/music/Pause.png"));
                    playPauseAudioButton.setToolTipText("Pause");
                    ConsoleFrame.getConsoleFrame().revalidateAudioMenu();

                    lastAction = LastAction.PLAY;

                    refreshFrameTitle();

                    SessionHandler.log(SessionHandler.Tag.ACTION,"[AUDIO PLAYER] " + audioFiles.get(audioIndex).getName());
                    player.play();

                    if (audioLocation != null)
                        audioLocation.kill();
                    audioLocation = null;

                    if (lastAction == LastAction.PAUSE || lastAction == LastAction.STOP) {
                        //paused/stopped for a reason so do nothing as of now
                    } else {
                        stopAudio();
                        refreshAudioFiles(null);
                        refreshAudio();

                        if (!queue.isEmpty()) {
                            String playPath = queue.pop().getAbsolutePath();

                            for (int i = 0 ; i < audioFiles.size() ; i++) {
                                if (audioFiles.get(i).getAbsolutePath().equalsIgnoreCase(playPath)) {
                                    audioIndex = i;
                                    break;
                                }
                            }

                            startAudio();
                        } else if (repeatAudio) {
                            startAudio();
                        } else if (shuffleAudio) {
                            int newIndex = NumberUtil.randInt(0, audioFiles.size() - 1);
                            while (newIndex == audioIndex)
                                newIndex = NumberUtil.randInt(0, audioFiles.size() - 1);

                            audioIndex = newIndex;
                            startAudio();
                        } else if (audioIndex + 1 < audioFiles.size()) {
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
            },DEFAULT_TITLE + " Audio Thread[" + StringUtil.getFilename(audioFiles.get(audioIndex)) + "]").start();
        }
    }

    /**
     * private inner class for the audio location slider
     */
    private static class AudioLocation {
        private CyderProgressBar effectBar;
        boolean update;
        DecimalFormat format = new DecimalFormat("##.#");

        AudioLocation(CyderProgressBar effectBar) {
            this.effectBar = effectBar;
            update = true;
            audioProgress.setStringPainted(true);
            try {
                new Thread( () -> {
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

                            float totalMilis = audioFileDuration(audioFiles.get(audioIndex));

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
                        } catch (Exception e) {
                            ExceptionHandler.silentHandle(e);
                        }
                    }
                },DEFAULT_TITLE + " Progress Thread[" + StringUtil.getFilename(audioFiles.get(audioIndex)) + "]").start();
            } catch (Exception e) {
                ExceptionHandler.silentHandle(e);
            }
        }

        public void kill() {
            update = false;
        }
    }

    private static class ImprovedScrollLabel {
        private JLabel effectLabel;
        boolean scroll;

        ImprovedScrollLabel(JLabel effectLabel) {
            scroll = true;
            this.effectLabel = effectLabel;

            try {
                String localTitle = StringUtil.getFilename(audioFiles.get(audioIndex).getName());
                effectLabel.setText(localTitle);

                int parentX = effectLabel.getParent().getX();
                int parentY = effectLabel.getParent().getY();

                int parentWidth = effectLabel.getParent().getWidth();
                int parentHeight = effectLabel.getParent().getHeight();

                int minWidth = CyderFrame.getMinWidth(localTitle, effectLabel.getFont());
                effectLabel.setSize(minWidth, parentHeight);

                if (minWidth - 12 > parentWidth) {
                    effectLabel.setLocation(0,0);

                    scroll = true;
                    int miliTimeout = 8;
                    int milipause = 5000;
                    int initialMiliPause = 3000;

                    new Thread(() -> {
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
                    },DEFAULT_TITLE + " scrolling title thread[" + StringUtil.getFilename(audioFiles.get(audioIndex)) + "]").start();
                } else {
                    String text = StringUtil.getFilename(audioFiles.get(audioIndex));
                    effectLabel.setText(text);
                    effectLabel.setLocation(effectLabel.getParent().getWidth() / 2
                            - CyderFrame.getAbsoluteMinWidth(text, effectLabel.getFont()) / 2, effectLabel.getY());
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
            this.scroll = false;
        }
    }

    /**
     * Sets the value of miniPlayer, the boolean determining whether the player is in smaller view or not.
     * @param b the boolean value of miniPlayer
     */
    public void setMiniPlayer(boolean b) {
        this.miniPlayer = b;
    }

    /**
     * Standard getter for miniPlayer value.
     * @return miniPlayer value
     */
    public boolean getMiniPlayer() {
        return this.miniPlayer;
    }

    /**
     * Sets the AudioPlayer to mini mode.
     */
    public static void enterMiniPlayer() {
        if (audioScroll != null)
            audioScroll.kill();
        audioScroll = null;

        if (audioLocation != null)
            audioLocation.kill();
        audioLocation = null;

        audioProgress.setVisible(false);
        audioVolumeSlider.setVisible(false);
        audioTitleLabel.setVisible(false);

        audioFrame.setSize(500,100);
        audioFrame.setMinimumSize(new Dimension(500, 100));
        audioFrame.setMaximumSize(new Dimension(500, 100));

        selectAudioDirButton.setLocation(selectAudioDirButton.getX(), 50);
        loopAudioButton.setLocation(loopAudioButton.getX(), 50);
        previousAudioButton.setLocation(previousAudioButton.getX(), 50);
        stopAudioButton.setLocation(stopAudioButton.getX(), 50);
        playPauseAudioButton.setLocation(playPauseAudioButton.getX(), 50);
        nextAudioButton.setLocation(nextAudioButton.getX(), 50);
        shuffleAudioButton.setLocation(shuffleAudioButton.getX(), 50);
    }

    /**
     * Exits mini mode if the player is in mini mode.
     */
    public static void exitMiniPlayer() {
        audioTitleLabel.setText(StringUtil.getFilename(audioFiles.get(audioIndex)));
        audioScroll = new ImprovedScrollLabel(audioTitleLabel);
        audioLocation = new AudioLocation(audioProgress);

        audioProgress.setVisible(true);
        audioVolumeSlider.setVisible(true);
        audioTitleLabel.setVisible(true);

        audioFrame.setSize(500,225);
        audioFrame.setMinimumSize(new Dimension(500, 155));
        audioFrame.setMaximumSize(new Dimension(500, 225));

        selectAudioDirButton.setLocation(selectAudioDirButton.getX(), 105);
        loopAudioButton.setLocation(loopAudioButton.getX(), 105);
        previousAudioButton.setLocation(previousAudioButton.getX(), 105);
        stopAudioButton.setLocation(stopAudioButton.getX(), 105);
        playPauseAudioButton.setLocation(playPauseAudioButton.getX(), 105);
        nextAudioButton.setLocation(nextAudioButton.getX(), 105);
        shuffleAudioButton.setLocation(shuffleAudioButton.getX(), 105);
    }

    public static File getCurrentAudio() {
        if (audioFiles == null || lastAction == LastAction.STOP)
            return null;
        else return audioFiles.get(audioIndex);
    }

    /**
     * Get's the total duration of an audio file
     * @param audioFile the provided audio file
     * @return the time in ms that it takes to comlete the audio file
     */
    public static float audioFileDuration(File audioFile) {
        float milisRet = 0;

        try {
            Header h = null;
            FileInputStream fis = new FileInputStream(audioFile);
            Bitstream bitstream = new Bitstream(fis);
            h = bitstream.readFrame();

            int size = h.calculate_framesize();
            float ms_per_frame = h.ms_per_frame();
            int maxSize = h.max_number_of_frames(10000);
            float t = h.total_ms(size);
            long tn = 0;
            tn = fis.getChannel().size();

            int min = h.min_number_of_frames(500);
            milisRet = h.total_ms((int) tn);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            return milisRet;
        }
    }

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

    private static LinkedList<File> queue;

    /**
     * Adds the provided audio file to the queue
     * @param f the audio file to play when all the other songs in the queue are finished
     */
    public static void addToQueue(File f) {
        queue.push(f);
    }

    /**
     * Refreshes the currentAlbumArt ImageIcon based on the current audio at audioIndex if
     * an album art file exists with the same name as the audio file.
     *
     * @return boolean describing whether or not album art exists
     */
    public static boolean refreshAlbumArt() {
        try {
            if (audioFiles == null || audioFiles.size() == 0)
                return false;

            String currentName = StringUtil.getFilename(audioFiles.get(audioIndex));

            File albumArtDir = new File("dynamic/users/"
                    + ConsoleFrame.getConsoleFrame().getUUID() + "/Music/AlbumArt");

            if (!albumArtDir.exists() || !albumArtDir.isDirectory()) {
                currentAlbumArt = null;
                return false;
            }

            //for all the album arts
            for (File f : albumArtDir.listFiles()) {
                if (StringUtil.getFilename(f).equals(currentName)) {
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
        if (lastAction == LastAction.STOP)
            audioFrame.setTitle(DEFAULT_TITLE);
        else
            audioFrame.setTitle(StringUtil.getFilename(audioFiles.get(audioIndex).getName()));
        ConsoleFrame.getConsoleFrame().revalidateMenu();
    }
}