package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.enums.SliderShape;
import cyder.exception.FatalException;
import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.ui.CyderSliderUI;
import cyder.utilities.GetterUtil;
import cyder.utilities.NumberUtil;
import cyder.utilities.StringUtil;
import javazoom.jl.player.Player;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;

public class AudioPlayer {

    private enum LastAction {
        SKIP,PAUSE,STOP,RESUME,PLAY
    }

    private LastAction lastAction;

    //ui components
    private ScrollLabel musicScroll;
    private AudioLocation audioLocation;
    private JLabel musicTitleLabel;
    private CyderFrame musicFrame;
    private JSlider musicVolumeSlider;
    private JSlider audioLocationSlider;
    private JButton previousMusicButton;
    private JButton nextMusicButton;
    private JButton stopMusicButton;
    private JButton loopMusicButton;
    private JButton selectMusicDirButton;
    private JButton playPauseMusicButton;
    private JButton shuffleMusicButton;

    //audio booleans
    private boolean shuffleAudio;
    private boolean repeatAudio;
    private boolean miniPlayer;
    private boolean pinned;

    //music list
    private int musicIndex;
    private LinkedList<File> musicFiles;

    //JLayer objects
    private Player player;
    private BufferedInputStream bis;
    private FileInputStream fis;

    //resuming/audio stat vars
    private long pauseLocation;
    private long totalLength;

    public AudioPlayer(File startPlaying) {
        if (musicFrame != null)
            musicFrame.closeAnimation();

        musicFrame = new CyderFrame(500,225);
        musicFrame.setBackground(new Color(8,23,52));
        musicFrame.setTitle("Flash Player");
        musicFrame.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (player != null)
                stopAudio();
            }
        });
        musicFrame.initializeBackgroundResizing();
        musicFrame.setResizable(true);
        musicFrame.setMinimumSize(new Dimension(500, 155));
        musicFrame.setMaximumSize(new Dimension(500, 225));

        JButton changeSize = new JButton("");
        changeSize.setToolTipText("Toggle Miniplayer");
        changeSize.addActionListener(e -> {
           if (!miniPlayer) {
               enterMiniPlayer();
               miniPlayer = !miniPlayer;
           } else {
               exitMiniPlayer();
               miniPlayer = !miniPlayer;
           }
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
        musicFrame.getDragLabel().addButton(changeSize, 1);

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
        musicFrame.getDragLabel().addButton(pinButton, 1);

        musicTitleLabel = new JLabel("", SwingConstants.CENTER);
        musicTitleLabel.setBounds(50, 40, 400, 30);
        musicTitleLabel.setToolTipText("Currently Playing");
        musicTitleLabel.setFont(new Font("tahoma", Font.BOLD, 18));
        musicTitleLabel.setForeground(CyderColors.vanila);
        musicTitleLabel.setText("No Audio Playing");
        musicFrame.getContentPane().add(musicTitleLabel);

        selectMusicDirButton = new JButton("");
        selectMusicDirButton.setFocusPainted(false);
        selectMusicDirButton.setOpaque(false);
        selectMusicDirButton.setContentAreaFilled(false);
        selectMusicDirButton.setBorderPainted(false);
        selectMusicDirButton.setToolTipText("Select audio");
        selectMusicDirButton.addActionListener(e -> new Thread(() -> {
            try {
                File selectedChildFile = new GetterUtil().getFile("Choose any mp3 file to startAudio");
                if (selectedChildFile != null) {
                    if (!selectedChildFile.toString().endsWith("mp3")) {
                        musicFrame.notify("Sorry, " + ConsoleFrame.getUsername() + ", but that's not an mp3 file.");
                    } else if (selectedChildFile != null){
                        refreshAudioFiles(selectedChildFile);
                        startAudio();
                    }
                }
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
                ex.printStackTrace();
            }
        }, "wait thread for GetterUtil().getFile()").start());

        selectMusicDirButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                selectMusicDirButton.setIcon(new ImageIcon("sys/pictures/music/SelectFileHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                selectMusicDirButton.setIcon(new ImageIcon("sys/pictures/music/SelectFile.png"));
            }
        });

        selectMusicDirButton.setBounds(55, 105, 30, 30);
        ImageIcon File = new ImageIcon("sys/pictures/music/SelectFile.png");
        selectMusicDirButton.setIcon(File);
        musicFrame.getContentPane().add(selectMusicDirButton);

        loopMusicButton = new JButton("");
        loopMusicButton.setToolTipText("Loop audio");
        loopMusicButton.addActionListener(e -> {
            loopMusicButton.setIcon(new ImageIcon(repeatAudio ? "sys/pictures/music/Repeat.png" : "sys/pictures/music/RepeatHover.png"));
            loopMusicButton.setToolTipText("Loop Audio");
            repeatAudio = !repeatAudio;
        });

        loopMusicButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loopMusicButton.setIcon(new ImageIcon(repeatAudio ? "sys/pictures/music/Repeat.png" : "sys/pictures/music/RepeatHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loopMusicButton.setIcon(new ImageIcon(repeatAudio ? "sys/pictures/music/RepeatHover.png" : "sys/pictures/music/Repeat.png"));
            }
        });

        loopMusicButton.setBounds(115, 105, 30, 30);
        loopMusicButton.setIcon(new ImageIcon("sys/pictures/music/Repeat.png"));
        musicFrame.getContentPane().add(loopMusicButton);
        loopMusicButton.setFocusPainted(false);
        loopMusicButton.setOpaque(false);
        loopMusicButton.setContentAreaFilled(false);
        loopMusicButton.setBorderPainted(false);

        previousMusicButton = new JButton("");
        previousMusicButton.setToolTipText("Previous audio");
        previousMusicButton.addActionListener(e -> previousAudio());

        previousMusicButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                previousMusicButton.setIcon(new ImageIcon("sys/pictures/music/SkipBackHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                previousMusicButton.setIcon(new ImageIcon("sys/pictures/music/SkipBack.png"));
            }
        });

        previousMusicButton.setBounds(175, 105, 30, 30);
        previousMusicButton.setIcon(new ImageIcon("sys/pictures/music/SkipBack.png"));
        musicFrame.getContentPane().add(previousMusicButton);
        previousMusicButton.setFocusPainted(false);
        previousMusicButton.setOpaque(false);
        previousMusicButton.setContentAreaFilled(false);
        previousMusicButton.setBorderPainted(false);

        stopMusicButton = new JButton("");
        stopMusicButton.setToolTipText("Stop");
        stopMusicButton.addActionListener(e -> {
            try {
                stopAudio();
            } catch (Exception ex) {
                ex.printStackTrace();
                ErrorHandler.handle(ex);
            }
        });

        stopMusicButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                stopMusicButton.setIcon(new ImageIcon("sys/pictures/music/StopHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                stopMusicButton.setIcon(new ImageIcon("sys/pictures/music/Stop.png"));
            }
        });

        stopMusicButton.setBounds(235, 105, 30, 30);
        stopMusicButton.setIcon(new ImageIcon("sys/pictures/music/Stop.png"));
        musicFrame.getContentPane().add(stopMusicButton);
        stopMusicButton.setFocusPainted(false);
        stopMusicButton.setOpaque(false);
        stopMusicButton.setContentAreaFilled(false);
        stopMusicButton.setBorderPainted(false);

        playPauseMusicButton = new JButton("");
        playPauseMusicButton.setToolTipText("Play");
        playPauseMusicButton.addActionListener(e -> {
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

        playPauseMusicButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                playPauseMusicButton.setIcon(new ImageIcon(player == null ? "sys/pictures/music/PlayHover.png" : "sys/pictures/music/PauseHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                playPauseMusicButton.setIcon(new ImageIcon(player == null ? "sys/pictures/music/Play.png" : "sys/pictures/music/Pause.png"));
            }
        });

        playPauseMusicButton.setBounds(295, 105, 30, 30);
        playPauseMusicButton.setIcon(new ImageIcon("sys/pictures/music/Play.png"));
        musicFrame.getContentPane().add(playPauseMusicButton);
        playPauseMusicButton.setFocusPainted(false);
        playPauseMusicButton.setOpaque(false);
        playPauseMusicButton.setContentAreaFilled(false);
        playPauseMusicButton.setBorderPainted(false);

        nextMusicButton = new JButton("");
        nextMusicButton.setToolTipText("Next Audio");
        nextMusicButton.addActionListener(e -> nextAudio());

        nextMusicButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                nextMusicButton.setIcon(new ImageIcon("sys/pictures/music/SkipHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nextMusicButton.setIcon(new ImageIcon("sys/pictures/music/Skip.png"));
            }
        });

        nextMusicButton.setBounds(355, 105, 30, 30);
        nextMusicButton.setIcon(new ImageIcon("sys/pictures/music/Skip.png"));
        musicFrame.getContentPane().add(nextMusicButton);
        nextMusicButton.setFocusPainted(false);
        nextMusicButton.setOpaque(false);
        nextMusicButton.setContentAreaFilled(false);
        nextMusicButton.setBorderPainted(false);

        shuffleMusicButton = new JButton("");
        shuffleMusicButton.setToolTipText("Shuffle audio");
        shuffleMusicButton.addActionListener(e -> {
            if (!shuffleAudio) {
                shuffleMusicButton.setIcon(new ImageIcon("sys/pictures/music/Shuffle.png"));
                shuffleAudio = true;
            } else {
                shuffleMusicButton.setIcon(new ImageIcon("sys/pictures/music/ShuffleHover.png"));
                shuffleAudio = false;
            }
        });

        shuffleMusicButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                shuffleMusicButton.setIcon(new ImageIcon(shuffleAudio ? "sys/pictures/music/Shuffle.png" : "sys/pictures/music/ShuffleHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                shuffleMusicButton.setIcon(new ImageIcon(shuffleAudio ? "sys/pictures/music/ShuffleHover.png" : "sys/pictures/music/Shuffle.png"));
            }
        });

        shuffleMusicButton.setBounds(405, 105, 30, 30);
        shuffleMusicButton.setIcon(new ImageIcon("sys/pictures/music/Shuffle.png"));
        musicFrame.getContentPane().add(shuffleMusicButton);
        shuffleMusicButton.setFocusPainted(false);
        shuffleMusicButton.setOpaque(false);
        shuffleMusicButton.setContentAreaFilled(false);
        shuffleMusicButton.setBorderPainted(false);

        musicVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        CyderSliderUI UI = new CyderSliderUI(musicVolumeSlider);
        UI.setThumbStroke(new BasicStroke(2.0f));
        UI.setSliderShape(SliderShape.CIRCLE);
        UI.setThumbDiameter(25);
        UI.setFillColor(CyderColors.vanila);
        UI.setOutlineColor(CyderColors.vanila);
        UI.setNewValColor(CyderColors.vanila);
        UI.setOldValColor(CyderColors.regularRed);
        UI.setStroke(new BasicStroke(2.0f));
        musicVolumeSlider.setUI(UI);
        musicVolumeSlider.setBounds(55, 155, 385, 40);
        musicVolumeSlider.setMinimum(0);
        musicVolumeSlider.setMaximum(100);
        musicVolumeSlider.setPaintTicks(false);
        musicVolumeSlider.setPaintLabels(false);
        musicVolumeSlider.setVisible(true);
        musicVolumeSlider.setValue(15);
        musicVolumeSlider.addChangeListener(e -> refreshAudio());
        musicVolumeSlider.setOpaque(false);
        musicVolumeSlider.setToolTipText("Volume");
        musicVolumeSlider.setFocusable(false);
        musicVolumeSlider.repaint();
        musicFrame.getContentPane().add(musicVolumeSlider);

        audioLocationSlider = new JSlider(JSlider.HORIZONTAL, 0, 10000, 0);
        CyderSliderUI UI2 = new CyderSliderUI(audioLocationSlider);
        UI2.setThumbStroke(new BasicStroke(2.0f));
        UI2.setSliderShape(SliderShape.NONE);
        UI2.setFillColor(CyderColors.vanila);
        UI2.setOutlineColor(CyderColors.vanila);
        UI2.setNewValColor(CyderColors.vanila);
        UI2.setOldValColor(CyderColors.regularRed);
        UI2.setStroke(new BasicStroke(2.0f));
        audioLocationSlider.setUI(UI2);
        audioLocationSlider.setBounds(55, 185, 385, 30);
        audioLocationSlider.setMinimum(0);
        audioLocationSlider.setMaximum(10000);
        audioLocationSlider.setPaintTicks(false);
        audioLocationSlider.setPaintLabels(false);
        audioLocationSlider.setVisible(true);
        audioLocationSlider.setValue(0);
        audioLocationSlider.setOpaque(false);
        audioLocationSlider.setToolTipText("Audio Location");
        audioLocationSlider.setFocusable(false);
        audioLocationSlider.repaint();
        musicFrame.getContentPane().add(audioLocationSlider);

        musicFrame.setLocationRelativeTo(null);
        musicFrame.setVisible(true);
        musicFrame.requestFocus();

        if (startPlaying != null && StringUtil.getExtension(startPlaying).equals(".mp3")) {
            try {
                refreshAudioFiles(startPlaying);
                startAudio();
            } catch (FatalException e) {
                e.printStackTrace();
                ErrorHandler.handle(e);
            }
        } else {
            try {
                File userMusicDir = new File("users/" + ConsoleFrame.getUUID() + "/Music/" );

                if (!userMusicDir.exists())
                    throw new FatalException("User music directory does not exist.");

                File[] userFiles = userMusicDir.listFiles();

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

    public void refreshAudio() {
        try {
            if (AudioSystem.isLineSupported(Port.Info.SPEAKER)) {
                Port outline = (Port) AudioSystem.getLine(Port.Info.SPEAKER);
                outline.open();
                FloatControl volumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);
                volumeControl.setValue((float) (musicVolumeSlider.getValue() * 0.01));
            }

            if (AudioSystem.isLineSupported(Port.Info.HEADPHONE)) {
                Port outline = (Port) AudioSystem.getLine(Port.Info.HEADPHONE);
                outline.open();
                FloatControl volumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);
                volumeControl.setValue((float) (musicVolumeSlider.getValue() * 0.01));
            }
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
            ErrorHandler.handle(ex);
        }
    }

    /**
     * Refreshes the music list and the music index incase files were added to the directory.
     * When starting pass the file that the user selected using the select music directory button.
     * On refresh, you may pass null and the program will infer where to look based on the current musicFile dir.
     */
    public void refreshAudioFiles(File chosenFile) throws FatalException {
        if (musicFiles == null)
            musicFiles = new LinkedList<>();

        if (chosenFile == null) {
            if (musicFiles.size() == 0)
                throw new FatalException("Chosen file is null with no files in musicFiles to get parent from");
            else {
                chosenFile = musicFiles.get(0);
            }
        }

        File neighboringFiles[] = chosenFile.getParentFile().listFiles();

        for (File file : neighboringFiles)
            if (StringUtil.getExtension(file).equals(".mp3"))
                musicFiles.add(file);

        for (int i = 0 ; i < musicFiles.size() ; i++) {
            if (musicFiles.get(i).equals(chosenFile))
                musicIndex = i;
        }

        if (musicFiles.size() == 0)
            musicFiles = null;
    }

    public void pauseAudio() {
        lastAction = LastAction.PAUSE;
        try {
            if (audioLocation != null)
                audioLocation.kill();
            audioLocation = null;

            pauseLocation = totalLength - fis.available() - 14000;

            if (player != null)
                player.close();
            player = null;
            bis = null;
            fis = null;

            playPauseMusicButton.setIcon(new ImageIcon("sys/pictures/music/Pause.png"));
            playPauseMusicButton.setToolTipText("Pause");
        } catch (Exception e) {
            e.printStackTrace();
            ErrorHandler.handle(e);
        }
    }

    public void stopAudio() {
        lastAction = LastAction.STOP;
       try {
           if (musicScroll != null)
               musicScroll.kill();
           musicScroll = null;

           if (audioLocation != null)
               audioLocation.kill();
           audioLocation = null;
           audioLocationSlider.setValue(0);

           if (player != null)
               player.close();
           player = null;
           bis = null;
           fis = null;

           pauseLocation = 0;
           totalLength = 0;

           musicTitleLabel.setText("No Audio Playing");
           audioLocationSlider.setValue(0);

           playPauseMusicButton.setIcon(new ImageIcon("sys/pictures/music/Play.png"));
           playPauseMusicButton.setToolTipText("Play");

           refreshAudio();
       } catch (Exception e) {
           e.printStackTrace();
           ErrorHandler.handle(e);
       }
    }

    public void previousAudio() {
        lastAction = LastAction.SKIP;
        try {
            stopAudio();

            if (shuffleAudio) {
                int newMusicIndex = NumberUtil.randInt(0, musicFiles.size() - 1);
                while (newMusicIndex == musicIndex)
                    newMusicIndex = NumberUtil.randInt(0, musicFiles.size() - 1);

                musicIndex = newMusicIndex;

            } else {
                if (musicIndex - 1 > -1) {
                    musicIndex--;
                } else {
                    musicIndex = musicFiles.size() - 1;
                }
            }

            startAudio();
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorHandler.handle(ex);
        }
    }

    public void nextAudio() {
        lastAction = LastAction.SKIP;
        try {
            stopAudio();

            if (shuffleAudio) {
                int newMusicIndex = NumberUtil.randInt(0, musicFiles.size() - 1);
                while (newMusicIndex == musicIndex)
                    newMusicIndex = NumberUtil.randInt(0, musicFiles.size() - 1);

                musicIndex = newMusicIndex;

            } else {
                if (musicIndex + 1 < musicFiles.size()) {
                    musicIndex++;
                } else {
                    musicIndex = 0;
                }
            }

            startAudio();
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorHandler.handle(ex);
        }
    }

    public void kill() {
        stopAudio();

        if (player != null)
            this.player.close();

        if (musicScroll != null)
            musicScroll.kill();

        if (audioLocation != null)
            audioLocation.kill();

        player = null;
        musicScroll = null;
        audioLocation = null;

        audioLocationSlider.setValue(0);

        musicFrame.closeAnimation();
    }

    public void startAudio() {
        new Thread(() -> {
            try {
                refreshAudio();
                fis = new FileInputStream(musicFiles.get(musicIndex));
                bis = new BufferedInputStream(fis);

                if (player != null)
                    player.close();
                player = null;

                if (musicScroll != null)
                    musicScroll.kill();
                musicScroll = null;

                if (audioLocation != null)
                    audioLocation.kill();
                audioLocation = null;

                player = new Player(bis);
                totalLength = fis.available();
                pauseLocation = 0;

                if (!miniPlayer) {
                    musicTitleLabel.setText(StringUtil.getFilename(musicFiles.get(musicIndex)));
                    musicScroll = new ScrollLabel(musicTitleLabel);
                    audioLocation = new AudioLocation(audioLocationSlider);
                }

                playPauseMusicButton.setIcon(new ImageIcon("sys/pictures/music/Pause.png"));
                playPauseMusicButton.setToolTipText("Pause");

                lastAction = LastAction.PLAY;

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
                        int newIndex = NumberUtil.randInt(0,musicFiles.size() - 1);
                        while (newIndex == musicIndex)
                            newIndex = NumberUtil.randInt(0,musicFiles.size() - 1);

                        musicIndex = newIndex;
                        startAudio();
                    } else if (musicIndex + 1 < musicFiles.size()) {
                        musicIndex++;
                        startAudio();
                    } else {
                        musicIndex = 0;
                        startAudio();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                ErrorHandler.handle(e);
            }
        },"Flash Player Music Thread[" + StringUtil.getFilename(musicFiles.get(musicIndex)) + "]").start();
    }

    public void resumeAudio(long startPosition) {
        lastAction = LastAction.RESUME;
        //todo last thing to implement after everything else has been implemented and tested
        // should be same as above except resuming at specific byte
    }

    private class AudioLocation {
        private JSlider effectSlider;
        boolean update;

        AudioLocation(JSlider effectSlider) {
            this.effectSlider = effectSlider;
            update = true;

            try {
                new Thread( () -> {
                    while (update) {
                        try {
                            if (totalLength == 0 || fis == null || !audioLocationSlider.isVisible())
                                return;

                            double place = ((double) (totalLength - fis.available()) /
                                    (double) totalLength) * audioLocationSlider.getMaximum();
                            audioLocationSlider.setValue((int) place);
                            Thread.sleep(250);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },"Flash Player Progress Thread[" + StringUtil.getFilename(musicFiles.get(musicIndex)) + "]").start();
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
     * Class used to make the scrolling song label if a song's title is too long to fit on the frame
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

                if (StringUtil.getFilename(musicFiles.get(musicIndex)).length() > maxLen) {
                    scroll = true;

                    new Thread(() -> {
                        try {
                            OUTER:
                                while (scroll) {
                                    String localTitle = StringUtil.getFilename(musicFiles.get(musicIndex));
                                    int localLen = localTitle.length();
                                    musicTitleLabel.setText(localTitle.substring(0,26));

                                    if (!scroll)
                                        break;

                                    Thread.sleep(2000);

                                    for (int i = 0 ; i <= localLen - 26; i++) {
                                        if (!scroll)
                                            break OUTER;

                                        musicTitleLabel.setText(localTitle.substring(i, i + 26));

                                        if (!scroll)
                                            break OUTER;

                                        Thread.sleep(charScrollDelay);
                                    }

                                    Thread.sleep(2000);

                                    for (int i = localLen - 26 ; i >= 0 ; i--) {
                                        if (!scroll)
                                            break OUTER;

                                        musicTitleLabel.setText(localTitle.substring(i, i + 26));

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
                    },"Flash Player scrolling title thread[" + StringUtil.getFilename(musicFiles.get(musicIndex)) + "]").start();
                } else {
                    musicTitleLabel.setText(StringUtil.getFilename(musicFiles.get(musicIndex)));
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

    public void setMiniPlayer(boolean b) {
        this.miniPlayer = b;
    }

    public boolean getMiniPlayer() {
        return this.miniPlayer;
    }

    public void setPinned(boolean b) {
        this.pinned = b;
        if (this.pinned)
            musicFrame.setAlwaysOnTop(true);
        else
            musicFrame.setAlwaysOnTop(false);
    }

    public boolean getPinned(boolean b) {
        return this.pinned;
    }

    public void enterMiniPlayer() {
        if (musicScroll != null)
            musicScroll.kill();
        musicScroll = null;

        if (audioLocation != null)
            audioLocation.kill();
        audioLocation = null;

        audioLocationSlider.setVisible(false);
        musicVolumeSlider.setVisible(false);
        musicTitleLabel.setVisible(false);

        musicFrame.setSize(500,100);
        musicFrame.setMinimumSize(new Dimension(500, 100));
        musicFrame.setMaximumSize(new Dimension(500, 100));

        selectMusicDirButton.setLocation(selectMusicDirButton.getX(), 50);
        loopMusicButton.setLocation(loopMusicButton.getX(), 50);
        previousMusicButton.setLocation(previousMusicButton.getX(), 50);
        stopMusicButton.setLocation(stopMusicButton.getX(), 50);
        playPauseMusicButton.setLocation(playPauseMusicButton.getX(), 50);
        nextMusicButton.setLocation(nextMusicButton.getX(), 50);
        shuffleMusicButton.setLocation(shuffleMusicButton.getX(), 50);
    }

    public void exitMiniPlayer() {
        musicTitleLabel.setText(StringUtil.getFilename(musicFiles.get(musicIndex)));
        musicScroll = new ScrollLabel(musicTitleLabel);
        audioLocation = new AudioLocation(audioLocationSlider);

        audioLocationSlider.setVisible(true);
        musicVolumeSlider.setVisible(true);
        musicTitleLabel.setVisible(true);

        musicFrame.setSize(500,225);
        musicFrame.setMinimumSize(new Dimension(500, 155));
        musicFrame.setMaximumSize(new Dimension(500, 225));

        selectMusicDirButton.setLocation(selectMusicDirButton.getX(), 105);
        loopMusicButton.setLocation(loopMusicButton.getX(), 105);
        previousMusicButton.setLocation(previousMusicButton.getX(), 105);
        stopMusicButton.setLocation(stopMusicButton.getX(), 105);
        playPauseMusicButton.setLocation(playPauseMusicButton.getX(), 105);
        nextMusicButton.setLocation(nextMusicButton.getX(), 105);
        shuffleMusicButton.setLocation(shuffleMusicButton.getX(), 105);
    }
}
