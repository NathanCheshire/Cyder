package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.enums.AnimationDirection;
import cyder.enums.SliderShape;
import cyder.handler.ErrorHandler;
import cyder.ui.*;
import cyder.utilities.*;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.player.Player;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
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
import java.util.LinkedList;

public class AudioPlayer {

    private enum LastAction {
        SKIP,PAUSE,STOP,RESUME,PLAY
    }

    private LastAction lastAction;

    private File currentAudio;

    //ui components
    private ScrollLabel audioScroll;
    private AudioLocation audioLocation;
    private JLabel audioTitleLabel;
    private CyderFrame audioFrame;
    private JSlider audioVolumeSlider;
    private CyderProgressBar audioProgress;
    private JButton previousAudioButton;
    private JButton nextAudioButton;
    private JButton stopAudioButton;
    private JButton loopAudioButton;
    private JButton selectAudioDirButton;
    private JButton playPauseAudioButton;
    private JButton shuffleAudioButton;
    private JLabel audioProgressLabel;

    //audio booleans
    private boolean shuffleAudio;
    private boolean repeatAudio;
    private boolean miniPlayer;
    private boolean pinned;

    //audio list
    private int audioIndex;
    private LinkedList<File> audioFiles;

    //JLayer objects
    private Player player;
    private BufferedInputStream bis;
    private FileInputStream fis;

    //resuming/audio stat vars
    private long pauseLocation;
    private long totalLength;

    /**
     * Constructor that launches the AudioPlayer
     * @param startPlaying - the audio file to start playing upon successful launch of the AudioPlayer.
     *                     Pass {@code null} to avoid starting audio upon launch.
     */
    public AudioPlayer(File startPlaying) {
        if (audioFrame != null)
            audioFrame.closeAnimation();

        audioFrame = new CyderFrame(500,225,
                new ImageIcon(ImageUtil.bufferedImageFromColor(500,225,new Color(8,23,52))));
        audioFrame.setBackground(new Color(8,23,52));
        audioFrame.setTitle("Flash Player");
        audioFrame.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (player != null)
                stopAudio();

            kill();
            audioFrame.setRestoreX(SystemUtil.getScreenWidth() / 2 - audioFrame.getWidth() / 2);
            audioFrame.setRestoreY( SystemUtil.getScreenHeight() / 2 - audioFrame.getHeight() / 2);
            }
        });
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
                changeSize.setIcon(new ImageIcon("sys/pictures/icons/ChangeSize2.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                changeSize.setIcon(new ImageIcon("sys/pictures/icons/ChangeSize1.png"));
            }
        });

        changeSize.setIcon(new ImageIcon("sys/pictures/icons/ChangeSize1.png"));
        changeSize.setContentAreaFilled(false);
        changeSize.setBorderPainted(false);
        changeSize.setFocusPainted(false);
        audioFrame.getTopDragLabel().addButton(changeSize, 1);

        JButton pinButton = new JButton("");
        pinButton.setToolTipText("Pin window");
        pinButton.addActionListener(e -> {
            pinned = !pinned;
            setPinned(pinned);
            if (pinned)
                pinButton.setIcon(new ImageIcon("sys/pictures/icons/pin2.png"));
            else
                pinButton.setIcon(new ImageIcon("sys/pictures/icons/pin.png"));
        });
        pinButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                pinButton.setIcon(new ImageIcon(pinned ? "sys/pictures/icons/pin.png" : "sys/pictures/icons/pin2.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                pinButton.setIcon(new ImageIcon(pinned ? "sys/pictures/icons/pin2.png" : "sys/pictures/icons/pin.png"));
            }
        });

        pinButton.setIcon(new ImageIcon("sys/pictures/icons/pin.png"));
        pinButton.setContentAreaFilled(false);
        pinButton.setBorderPainted(false);
        pinButton.setFocusPainted(false);
        audioFrame.getTopDragLabel().addButton(pinButton, 1);

        audioTitleLabel = new JLabel("", SwingConstants.CENTER);
        audioTitleLabel.setBounds(50, 40, 400, 30);
        audioTitleLabel.setToolTipText("Currently Playing");
        audioTitleLabel.setFont(new Font("tahoma", Font.BOLD, 18));
        audioTitleLabel.setForeground(CyderColors.vanila);
        audioTitleLabel.setText("No Audio Playing");
        audioFrame.getContentPane().add(audioTitleLabel);

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
                ErrorHandler.handle(ex);
                ex.printStackTrace();
            }
        }, "wait thread for GetterUtil().getFile()").start());

        selectAudioDirButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                selectAudioDirButton.setIcon(new ImageIcon("sys/pictures/music/SelectFileHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                selectAudioDirButton.setIcon(new ImageIcon("sys/pictures/music/SelectFile.png"));
            }
        });

        selectAudioDirButton.setBounds(55, 105, 30, 30);
        ImageIcon File = new ImageIcon("sys/pictures/music/SelectFile.png");
        selectAudioDirButton.setIcon(File);
        audioFrame.getContentPane().add(selectAudioDirButton);

        loopAudioButton = new JButton("");
        loopAudioButton.setToolTipText("Loop audio");
        loopAudioButton.addActionListener(e -> {
            loopAudioButton.setIcon(new ImageIcon(repeatAudio ? "sys/pictures/music/Repeat.png" : "sys/pictures/music/RepeatHover.png"));
            loopAudioButton.setToolTipText("Loop Audio");
            repeatAudio = !repeatAudio;
        });

        loopAudioButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loopAudioButton.setIcon(new ImageIcon(repeatAudio ? "sys/pictures/music/Repeat.png" : "sys/pictures/music/RepeatHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loopAudioButton.setIcon(new ImageIcon(repeatAudio ? "sys/pictures/music/RepeatHover.png" : "sys/pictures/music/Repeat.png"));
            }
        });

        loopAudioButton.setBounds(115, 105, 30, 30);
        loopAudioButton.setIcon(new ImageIcon("sys/pictures/music/Repeat.png"));
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
                previousAudioButton.setIcon(new ImageIcon("sys/pictures/music/SkipBackHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                previousAudioButton.setIcon(new ImageIcon("sys/pictures/music/SkipBack.png"));
            }
        });

        previousAudioButton.setBounds(175, 105, 30, 30);
        previousAudioButton.setIcon(new ImageIcon("sys/pictures/music/SkipBack.png"));
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
                ex.printStackTrace();
                ErrorHandler.handle(ex);
            }
        });

        stopAudioButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                stopAudioButton.setIcon(new ImageIcon("sys/pictures/music/StopHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                stopAudioButton.setIcon(new ImageIcon("sys/pictures/music/Stop.png"));
            }
        });

        stopAudioButton.setBounds(235, 105, 30, 30);
        stopAudioButton.setIcon(new ImageIcon("sys/pictures/music/Stop.png"));
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
                ex.printStackTrace();
                ErrorHandler.handle(ex);
            }
        });

        playPauseAudioButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                playPauseAudioButton.setIcon(new ImageIcon(player == null ? "sys/pictures/music/PlayHover.png" : "sys/pictures/music/PauseHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                playPauseAudioButton.setIcon(new ImageIcon(player == null ? "sys/pictures/music/Play.png" : "sys/pictures/music/Pause.png"));
            }
        });

        playPauseAudioButton.setBounds(295, 105, 30, 30);
        playPauseAudioButton.setIcon(new ImageIcon("sys/pictures/music/Play.png"));
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
                nextAudioButton.setIcon(new ImageIcon("sys/pictures/music/SkipHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nextAudioButton.setIcon(new ImageIcon("sys/pictures/music/Skip.png"));
            }
        });

        nextAudioButton.setBounds(355, 105, 30, 30);
        nextAudioButton.setIcon(new ImageIcon("sys/pictures/music/Skip.png"));
        audioFrame.getContentPane().add(nextAudioButton);
        nextAudioButton.setFocusPainted(false);
        nextAudioButton.setOpaque(false);
        nextAudioButton.setContentAreaFilled(false);
        nextAudioButton.setBorderPainted(false);

        shuffleAudioButton = new JButton("");
        shuffleAudioButton.setToolTipText("Shuffle audio");
        shuffleAudioButton.addActionListener(e -> {
            if (!shuffleAudio) {
                shuffleAudioButton.setIcon(new ImageIcon("sys/pictures/music/Shuffle.png"));
                shuffleAudio = true;
            } else {
                shuffleAudioButton.setIcon(new ImageIcon("sys/pictures/music/ShuffleHover.png"));
                shuffleAudio = false;
            }
        });

        shuffleAudioButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                shuffleAudioButton.setIcon(new ImageIcon(shuffleAudio ? "sys/pictures/music/Shuffle.png" : "sys/pictures/music/ShuffleHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                shuffleAudioButton.setIcon(new ImageIcon(shuffleAudio ? "sys/pictures/music/ShuffleHover.png" : "sys/pictures/music/Shuffle.png"));
            }
        });

        shuffleAudioButton.setBounds(405, 105, 30, 30);
        shuffleAudioButton.setIcon(new ImageIcon("sys/pictures/music/Shuffle.png"));
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
        UI.setStroke(new BasicStroke(2.0f));
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
        audioProgressLabel.setBounds(55, 190, 385, 25);
        audioFrame.getContentPane().add(audioProgressLabel);

        audioProgress = new CyderProgressBar(CyderProgressBar.HORIZONTAL, 0, 10000);
        CyderProgressUI ui = new CyderProgressUI();
        ui.setColors(new Color[]{CyderColors.intellijPink, CyderColors.tooltipForegroundColor});
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

        ConsoleFrame.getConsoleFrame().setFrameRelative(audioFrame);
        audioFrame.setVisible(true);
        audioFrame.requestFocus();

        if (startPlaying != null && StringUtil.getExtension(startPlaying).equals(".mp3")) {
            refreshAudioFiles(startPlaying);
            startAudio();
        } else {
            try {
                File userAudioDir = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Music/" );

                if (!userAudioDir.exists())
                    throw new IllegalArgumentException("User music directory does not exist.");

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
                e.printStackTrace();
                ErrorHandler.handle(e);
            }
        }
    }

    /**
     * Refreshes the {@code Port.Info.SPEAKER} or {@code Port.Info.HEADPHONE} volume.
     */
    public void refreshAudio() {
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
        } catch (LineUnavailableException ex) {
            ErrorHandler.handle(ex);
        }
    }

    /**
     * Refreshes the audio list and the audio index incase files were added to the directory.
     * When starting pass the file that the user selected using the select audio directory button.
     * On refresh, you may pass null and the program will infer where to look based on the current audioFile dir.
     */
    public void refreshAudioFiles(File refreshOnFile) {
        if (audioFiles == null)
            audioFiles = new LinkedList<>();

        if (refreshOnFile == null) {
            if (audioFiles == null || audioFiles.size() == 0)
                throw new IllegalArgumentException("No music files were found to refresh on");

            refreshOnFile = audioFiles.get(audioIndex);
        }

        File neighboringFiles[] = refreshOnFile.getParentFile().listFiles();

        for (File file : neighboringFiles)
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
     * Pauses the audio if anything is currently playing in preparation to resume at the current location.
     */
    public void pauseAudio() {
        lastAction = LastAction.PAUSE;
        try {
            pauseLocation = totalLength - fis.available() - 14000;

            if (player != null)
                player.close();
            player = null;

            playPauseAudioButton.setIcon(new ImageIcon("sys/pictures/music/Play.png"));
            playPauseAudioButton.setToolTipText("Play");
        } catch (Exception e) {
            e.printStackTrace();
            ErrorHandler.handle(e);
        }
    }

    /**
     * Stops the audio and all threads in their tracks.
     */
    public void stopAudio() {
        lastAction = LastAction.STOP;
       try {
           if (audioScroll != null)
               audioScroll.kill();
           audioScroll = null;

           if (audioLocation != null)
               audioLocation.kill();
           audioLocation = null;

           if (player != null)
               player.close();
           player = null;
           bis = null;
           fis = null;

           pauseLocation = 0;
           totalLength = 0;

           audioTitleLabel.setText("No Audio Playing");
           audioProgress.setValue(0);
           audioProgressLabel.setText("");

           playPauseAudioButton.setIcon(new ImageIcon("sys/pictures/music/Play.png"));
           playPauseAudioButton.setToolTipText("Play");

           refreshAudio();
       } catch (Exception e) {
           e.printStackTrace();
           ErrorHandler.handle(e);
       }
    }

    /**
     * Skips to the current audio file's predecesor if it exists in the directory.
     */
    public void previousAudio() {
        refreshAudioFiles(null);
        lastAction = LastAction.SKIP;
        try {
            stopAudio();

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

            startAudio();
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorHandler.handle(ex);
        }
    }

    /**
     * Skips to the current audio file's successor if it exists in the directory.
     */
    public void nextAudio() {
        refreshAudioFiles(null);
        lastAction = LastAction.SKIP;
        try {
            stopAudio();

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

            startAudio();
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorHandler.handle(ex);
        }
    }

    /**
     * Kills all threads and resets all variables to their defaults before invoking dispose on the audio frame.
     */
    public void kill() {
        stopAudio();

        if (player != null)
            this.player.close();

        if (audioScroll != null)
            audioScroll.kill();

        if (audioLocation != null)
            audioLocation.kill();

        player = null;
        audioScroll = null;
        audioLocation = null;

        audioProgress.setValue(0);
        audioProgressLabel.setText("");

        audioFiles = null;

        audioFrame.closeAnimation();
    }

    /**
     * Starts playin audio from the current index.
     */
    public void startAudio() {
        new Thread(() -> {
            try {
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

                //these occasionally throws NullPtrExep if the user spams buttons so we'll ignore that
                try {
                    player = new Player(bis);
                    totalLength = fis.available();
                } catch (Exception ignored) {}

                pauseLocation = 0;

                if (!miniPlayer) {
                    audioTitleLabel.setText(StringUtil.getFilename(audioFiles.get(audioIndex)));
                    audioScroll = new ScrollLabel(audioTitleLabel);
                    audioLocation = new AudioLocation(audioProgress);
                }

                playPauseAudioButton.setIcon(new ImageIcon("sys/pictures/music/Pause.png"));
                playPauseAudioButton.setToolTipText("Pause");

                lastAction = LastAction.PLAY;

                //on spam of skip button, music player hangs for about 10 seconds
                // and throws an error then catches up eventually
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

                    if (repeatAudio) {
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
                e.printStackTrace();
                ErrorHandler.handle(e);
            }
        },"Flash Player Audio Thread[" + StringUtil.getFilename(audioFiles.get(audioIndex)) + "]").start();
    }

    /**
     * Resumes audio at the current audio file at the passed in byte value.
     * @param startPosition - the byte value to skip to when starting the audio
     */
    public void resumeAudio(long startPosition) {
        if (lastAction == LastAction.STOP) {
            startAudio();
        } else if (lastAction == LastAction.PAUSE) {
            lastAction = LastAction.RESUME;
            new Thread(() -> {
                try {
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

                    //these occasionally throws NullPtrExep if the user spams buttons so we'll ignore that
                    try {
                        player = new Player(bis);
                        totalLength = fis.available();
                    } catch (Exception ignored) {}

                    pauseLocation = 0;

                    if (!miniPlayer) {
                        audioTitleLabel.setText(StringUtil.getFilename(audioFiles.get(audioIndex)));
                        audioScroll = new ScrollLabel(audioTitleLabel);
                        audioLocation = new AudioLocation(audioProgress);
                    }

                    playPauseAudioButton.setIcon(new ImageIcon("sys/pictures/music/Pause.png"));
                    playPauseAudioButton.setToolTipText("Pause");

                    lastAction = LastAction.PLAY;

                    fis.skip(startPosition);
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

                        if (repeatAudio) {
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
                    e.printStackTrace();
                    ErrorHandler.handle(e);
                }
            },"Flash Player Audio Thread[" + StringUtil.getFilename(audioFiles.get(audioIndex)) + "]").start();
        }
    }

    /**
     * private inner class for the audio location slider
     */
    private class AudioLocation {
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
                            double milisIn = percentIn * totalMilis;
                            double milisLeft = percentLeft * totalMilis;

                            audioProgressLabel.setText(formatMilis(milisIn) + " played, "
                                    + formatMilis(milisLeft) + " left");
                            Thread.sleep(250);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },"Flash Player Progress Thread[" + StringUtil.getFilename(audioFiles.get(audioIndex)) + "]").start();
            } catch (Exception e) {
                e.printStackTrace();
                ErrorHandler.handle(e);
            }
        }

        public void kill() {
            update = false;
        }
    }

    /**
     * private inner class for the scrolling song label
     */
    private class ScrollLabel {
        private JLabel effectLabel;
        boolean scroll;

        ScrollLabel(JLabel effectLabel) {
            scroll = true;
            this.effectLabel = effectLabel;

            try {
                int maxLen = 30;
                int charScrollDelay = 200;

                String localTitle = StringUtil.getFilename(audioFiles.get(audioIndex));

                if (localTitle.length() > maxLen) {
                    scroll = true;

                    new Thread(() -> {
                        try {
                            OUTER:
                                while (scroll) {
                                    int localLen = localTitle.length();
                                    audioTitleLabel.setText(localTitle.substring(0,26));

                                    if (!scroll)
                                        break;

                                    Thread.sleep(2000);

                                    for (int i = 0 ; i <= localLen - 26; i++) {
                                        if (!scroll)
                                            break OUTER;

                                        audioTitleLabel.setText(localTitle.substring(i, i + 26));

                                        if (!scroll)
                                            break OUTER;

                                        Thread.sleep(charScrollDelay);
                                    }

                                    Thread.sleep(2000);

                                    for (int i = localLen - 26 ; i >= 0 ; i--) {
                                        if (!scroll)
                                            break OUTER;

                                        audioTitleLabel.setText(localTitle.substring(i, i + 26));

                                        if (!scroll)
                                            break OUTER;

                                        Thread.sleep(charScrollDelay);
                                    }
                                }
                        }

                        catch (Exception e) {
                            e.printStackTrace();
                            ErrorHandler.handle(e);
                        }
                    },"Flash Player scrolling title thread[" + StringUtil.getFilename(audioFiles.get(audioIndex)) + "]").start();
                } else {
                    audioTitleLabel.setText(StringUtil.getFilename(audioFiles.get(audioIndex)));
                }
            } catch (Exception e) {
                e.printStackTrace();
                ErrorHandler.handle(e);
            }
        }

        public void kill() {
            this.scroll = false;
        }
    }

    /**
     * Sets the value of miniPlayer, the boolean determining whether the player is in smaller view or not.
     * @param b - the boolean value of miniPlayer
     */
    public void setMiniPlayer(boolean b) {
        this.miniPlayer = b;
    }

    /**
     * Standard getter for miniPlayer value.
     * @return - miniPlayer value
     */
    public boolean getMiniPlayer() {
        return this.miniPlayer;
    }

    /**
     * Sets the value for pinning the frame on top.
     * @param b - the value determining whether or not the frame is always on top
     */
    public void setPinned(boolean b) {
        this.pinned = b;
        audioFrame.setAlwaysOnTop(this.pinned);
    }

    /**
     * Standard getter for pinned boolean.
     * @return - the boolean of pinned
     */
    public boolean getPinned() {
        return this.pinned;
    }

    /**
     * Sets the AudioPlayer to mini mode.
     */
    public void enterMiniPlayer() {
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
    public void exitMiniPlayer() {
        audioTitleLabel.setText(StringUtil.getFilename(audioFiles.get(audioIndex)));
        audioScroll = new ScrollLabel(audioTitleLabel);
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

    public File getCurrentAudio() {
        if (audioFiles == null)
            return null;
        else
            return audioFiles.get(audioIndex);
    }

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
            ErrorHandler.handle(e);
        } finally {
            return milisRet;
        }
    }

    public static String formatMilis(double milis) {
        StringBuilder sb = new StringBuilder();

        double seconds = milis / 1000.0;
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
}
