package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.enums.SliderShape;
import cyder.exception.FatalException;
import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.ui.CyderSliderUI;
import cyder.utilities.NumberUtil;
import cyder.utilities.StringUtil;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;

public class AudioPlayer {

    //ui components
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

    //todo on pause: pauseLocation = totalLength - fis.available();
    //todo on resume: pass in pauseLocation
    //todo on stop: plauselocation = 0, reset other stuff

    //todo pinned mode will set always on top to true
    //todo change size button will get rid of title and sliders and resize

    //todo smaller button needs to end scrolling label/start and set all component bounds and to visible or not

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
                kill();
            }
        });
        musicFrame.initializeBackgroundResizing();
        musicFrame.setResizable(true);
        musicFrame.setMinimumSize(new Dimension(500, 155));
        musicFrame.setMaximumSize(new Dimension(500, 225));

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

        }));

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
            loopMusicButton.setIcon(new ImageIcon(repeatAudio ? "sys/pictures/music/RepeatHover.png" : "sys/pictures/music/Repeat.png"));
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
        previousMusicButton.addActionListener(e -> {

        });

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
        playPauseMusicButton.setToolTipText("play");
        playPauseMusicButton.addActionListener(e -> {

        });

        playPauseMusicButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                playPauseMusicButton.setIcon(new ImageIcon(player == null ? "sys/pictures/music/PlayHover.png" : "sys/pictures/music/PauseHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                playPauseMusicButton.setIcon(new ImageIcon(player == null ? "sys/pictures/music/play.png" : "sys/pictures/music/Pause.png"));
            }
        });

        playPauseMusicButton.setBounds(295, 105, 30, 30);
        playPauseMusicButton.setIcon(new ImageIcon("sys/pictures/music/play.png"));
        musicFrame.getContentPane().add(playPauseMusicButton);
        playPauseMusicButton.setFocusPainted(false);
        playPauseMusicButton.setOpaque(false);
        playPauseMusicButton.setContentAreaFilled(false);
        playPauseMusicButton.setBorderPainted(false);

        nextMusicButton = new JButton("");
        nextMusicButton.setToolTipText("Next Audio"); //change to skip
        nextMusicButton.addActionListener(e -> {

        });

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
        UI.setSliderShape(SliderShape.HOLLOW_CIRCLE);
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
        musicVolumeSlider.setValue(50);
        musicVolumeSlider.addChangeListener(e -> {

        });
        musicVolumeSlider.setOpaque(false);
        musicVolumeSlider.setToolTipText("Volume");
        musicVolumeSlider.setFocusable(false);
        musicVolumeSlider.repaint();
        musicFrame.getContentPane().add(musicVolumeSlider);

        audioLocationSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        CyderSliderUI UI2 = new CyderSliderUI(audioLocationSlider);
        UI2.setThumbStroke(new BasicStroke(2.0f));
        UI2.setSliderShape(SliderShape.HOLLOW_CIRCLE);
        UI2.setFillColor(CyderColors.vanila);
        UI2.setOutlineColor(CyderColors.vanila);
        UI2.setNewValColor(CyderColors.vanila);
        UI2.setOldValColor(CyderColors.regularRed);
        UI2.setStroke(new BasicStroke(2.0f));
        audioLocationSlider.setUI(UI2);
        audioLocationSlider.setBounds(55, 185, 385, 30);
        audioLocationSlider.setMinimum(0);
        audioLocationSlider.setMaximum(100);
        audioLocationSlider.setPaintTicks(false);
        audioLocationSlider.setPaintLabels(false);
        audioLocationSlider.setVisible(true);
        audioLocationSlider.setValue(50);
        audioLocationSlider.addChangeListener(e -> {

        });
        audioLocationSlider.setOpaque(false);
        audioLocationSlider.setToolTipText("Song location");
        audioLocationSlider.setFocusable(false);
        audioLocationSlider.repaint();
        musicFrame.getContentPane().add(audioLocationSlider);

        musicFrame.setLocationRelativeTo(null);
        musicFrame.setVisible(true);
        musicFrame.requestFocus();

        if (startPlaying != null && StringUtil.getExtension(startPlaying).equals("mp3")) {
            try {
                refreshMusic(startPlaying);
            } catch (FatalException e) {
                ErrorHandler.handle(e);
            }
        }

        else {
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
                        refreshMusic(f);
                }

                play(0);
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }
    }

    /**
     * Refreshes the music list and the music index incase files were added to the directory.
     * When starting pass the file that the user selected using the select music directory button.
     * On refresh, you may pass null and the program will infer where to look based on the current musicFile dir.
     */
    public void refreshMusic(File chosenFile) throws FatalException {
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

    /**
     * Ends any and all threads having to do with this object to free up resources. Resets variables.
     */
    public void kill() {
        this.stopScrolling();
        if (player != null) {
            player.close();
            player = null;
            bis = null;
            fis = null;
            pauseLocation = 0;
            totalLength = 0;
        }
        musicFrame.closeAnimation();
    }

    /**
     * Starts/resumes playing of the current audio file
     * @param start - the start location of the audio file; pass 0 to start at the beginning
     */
    public void play(long start) throws FatalException, IOException, JavaLayerException {
        if (start < 0)
            throw new FatalException("Starting posiotion less than 0");

        musicVolumeSlider.setValue(musicVolumeSlider.getValue());

        if (player != null) {
            player.close();
            player = null;
        } else {
            fis = new FileInputStream(musicFiles.get(musicIndex));
            bis = new BufferedInputStream(fis);
            player = new Player(bis);
            totalLength = fis.available();
        }

        playPauseMusicButton.setIcon(new ImageIcon("sys/pictures/music/Pause.png"));
        playPauseMusicButton.setToolTipText("Pause");

        try {
            startScrolling();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        //Thread to play music, if we call kill it will free up the resources and this thread will end
        new Thread(() -> {
            try {
                if (start != 0) {
                    if (start < totalLength) {
                        fis.skip(start);
                    } else {
                        fis.skip(0);
                    }
                }

                player.play();

                if (repeatAudio) {
                    play(0);
                } else if (shuffleAudio) {
                    musicIndex = NumberUtil.randInt(0, musicFiles.size() - 1);
                    play(0);
                } else {
                    if (musicIndex + 1 < musicFiles.size()) {
                        musicIndex++;
                        play(0);
                    }
                }

                playPauseMusicButton.setIcon(new ImageIcon("sys/pictures/music/play.png"));
                playPauseMusicButton.setToolTipText("play");
                player = null;
                stopScrolling();
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"Flash Player Music Thread[" + StringUtil.getFilename(musicFiles.get(musicIndex)) + "]").start();

        startScrolling();
    }

    boolean scroll = true;

    /**
     * End scrolling of the current scrolling label. This method exits the inner thread to free up resources.
     * To start scrolling again, this method will need to be called again to reinstantiate the label text.
     */
    public void stopScrolling() {
        this.scroll = false;
    }

    /**
     * Start scrolling if needed based off of the current audio file playing
     */
    public void startScrolling() {
        try {
            int maxLen = 30;
            int charScrollDelay = 200;

            if (StringUtil.getFilename(musicFiles.get(musicIndex)).length() > maxLen) {
                scroll = true;

                new Thread(() -> {
                    try {
                        while (scroll) {
                            String localTitle = StringUtil.getFilename(musicFiles.get(musicIndex));
                            int localLen = localTitle.length();
                            musicTitleLabel.setText(localTitle.substring(0,26));

                            if (!scroll)
                                return;

                            Thread.sleep(2000);

                            for (int i = 0 ; i <= localLen - 26; i++) {
                                if (!scroll)
                                    return;

                                musicTitleLabel.setText(localTitle.substring(i, i + 26));

                                if (!scroll)
                                    return;

                                Thread.sleep(charScrollDelay);
                            }

                            Thread.sleep(2000);

                            for (int i = localLen - 26 ; i >= 0 ; i--) {
                                if (!scroll)
                                    return;

                                musicTitleLabel.setText(localTitle.substring(i, i + 26));

                                if (!scroll)
                                    return;

                                Thread.sleep(charScrollDelay);
                            }
                        }
                    }

                    catch (Exception e) {
                        ErrorHandler.handle(e);
                    }
                },"Flash Player scrolling title thread[" + StringUtil.getFilename(musicFiles.get(musicIndex)) + "]").start();
            } else {
                musicTitleLabel.setText(StringUtil.getFilename(musicFiles.get(musicIndex)));
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
