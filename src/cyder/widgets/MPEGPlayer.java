package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderImages;
import cyder.enums.SliderShape;
import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.ui.CyderSliderUI;
import cyder.utilities.GetterUtil;
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
import java.util.ArrayList;

public class MPEGPlayer {
    private ScrollLabel musicScroll;
    private CyderFrame musicFrame;
    private JSlider musicVolumeSlider;

    private boolean shuffleAudio;
    private boolean musicStopped;

    private JButton lastMusic;
    private JButton nextMusic;
    private JButton stopMusic;
    private JButton loopMusic;
    private JButton selectMusicDir;
    private JButton playPauseMusic;
    private JButton shuffleMusic;

    private JLabel musicTitleLabel;
    private int currentMusicIndex;
    private File[] musicFiles; //conv to linked list
    private Player mp3Player;
    private BufferedInputStream bis;
    private FileInputStream fis;
    private long pauseLocation;
    private long songTotalLength;
    private boolean playIcon = true;
    private boolean loopAudio;

    //todo error resuming music, tries to start at beggining
    //todo audio doesnt go to next audio when current ends
    //todo audio progress slider
    //todo add to drag label at specific index a window button like from console to make mini player with just
    // buttons and sliders (drag label still ofc since still a cyderframe)
    //todo implement shuffle feature
    //todo skipping after a song is on repeat and starts playing again made scrolling label glitch out

    //todo feed a youtube video and stream audio from (using ffmpeg) and get thumbnail and use as album art

    /**
     * This constructor takes an mp3 file to immediately start playing when FlashPlayer loads
     * @param StartPlaying - the mp3 file we want to start playing
     */
    public MPEGPlayer(File StartPlaying) {
        if (musicFrame != null)
            musicFrame.closeAnimation();

        musicFrame = new CyderFrame(1000,563);
        musicFrame.setBackground(new Color(8,23,52));
        musicFrame.setTitle("Flash Player");
        musicFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
            if (mp3Player != null)
                mp3Player.close();
            stopScrolling();
            }
        });

        musicTitleLabel = new JLabel("", SwingConstants.CENTER);
        musicTitleLabel.setBounds(310, 38, 400, 30);
        musicTitleLabel.setToolTipText("Currently Playing");
        musicTitleLabel.setFont(new Font("tahoma", Font.BOLD, 18));
        musicTitleLabel.setForeground(CyderColors.vanila);
        musicTitleLabel.setText("No Audio Playing");
        musicFrame.getContentPane().add(musicTitleLabel);

        musicVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
        CyderSliderUI UI = new CyderSliderUI(musicVolumeSlider);
        UI.setSliderShape(SliderShape.HOLLOW_CIRCLE);
        UI.setFillColor(CyderColors.vanila);
        UI.setOutlineColor(CyderColors.vanila);
        UI.setNewValColor(CyderColors.vanila);
        UI.setOldValColor(CyderColors.regularRed);
        UI.setStroke(new BasicStroke(2.0f));
        musicVolumeSlider.setUI(UI);
        musicVolumeSlider.setBounds(352, 499, 385, 63);
        musicVolumeSlider.setMinimum(0);
        musicVolumeSlider.setMaximum(100);
        musicVolumeSlider.setPaintTicks(false);
        musicVolumeSlider.setPaintLabels(false);
        musicVolumeSlider.setVisible(true);
        musicVolumeSlider.setValue(50);
        musicVolumeSlider.addChangeListener(e -> {
            Port.Info speaker = Port.Info.SPEAKER;
            Port.Info headphone = Port.Info.HEADPHONE;

            try {
                if (AudioSystem.isLineSupported(speaker)) {
                    Port outline = (Port) AudioSystem.getLine(speaker);
                    outline.open();

                    FloatControl volumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);

                    volumeControl.setValue((float) (musicVolumeSlider.getValue() * 0.01));
                }

                if (AudioSystem.isLineSupported(headphone)) {
                    Port outline = (Port) AudioSystem.getLine(headphone);
                    outline.open();

                    FloatControl volumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);

                    volumeControl.setValue((float) (musicVolumeSlider.getValue() * 0.01));
                }
            } catch (LineUnavailableException ex) {
                ErrorHandler.handle(ex);
            }
        });

        musicVolumeSlider.setOpaque(false);
        musicVolumeSlider.setToolTipText("Volume");
        musicVolumeSlider.setFocusable(false);
        musicVolumeSlider.repaint();
        musicFrame.getContentPane().add(musicVolumeSlider);

        playPauseMusic = new JButton("");
        playPauseMusic.setToolTipText("play");
        playPauseMusic.addActionListener(e -> {
            if (mp3Player != null) {
                if (!playIcon) {
                    playPauseMusic.setIcon(new ImageIcon("sys/pictures/music/play.png"));
                    playPauseMusic.setToolTipText("play");
                    playIcon = true;
                } else {
                    playPauseMusic.setIcon(new ImageIcon("sys/pictures/music/Pause.png"));
                    playPauseMusic.setToolTipText("Pause");
                    playIcon = false;
                }

                if (playIcon) {
                    try {
                        pauseLocation = fis.available();
                    } catch (Exception exc) {
                        ErrorHandler.handle(exc);
                    }

                    stopScrolling();
                    mp3Player.close();
                }

                else {
                    try {
                        fis = new FileInputStream(musicFiles[currentMusicIndex]);
                        bis = new BufferedInputStream(fis);
                        mp3Player = new Player(bis);

                        if (pauseLocation == 0)
                            fis.skip(0);

                        else {
                            if (songTotalLength - pauseLocation <= 0)
                                fis.skip(0);
                            else
                                fis.skip(songTotalLength - pauseLocation - 14000);
                        }

                        resumeMusic();
                    } catch (Exception ex) {
                        ErrorHandler.handle(ex);
                    }
                }
            }
        });

        playPauseMusic.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                playPauseMusic.setIcon(new ImageIcon(playIcon ? "sys/pictures/music/PlayHover.png" : "sys/pictures/music/PauseHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                playPauseMusic.setIcon(new ImageIcon(playIcon ? "sys/pictures/music/play.png" : "sys/pictures/music/Pause.png"));
            }
        });

        playPauseMusic.setBounds(121, 263, 30, 30);
        ImageIcon Play = new ImageIcon("sys/pictures/music/play.png");
        playPauseMusic.setIcon(Play);
        musicFrame.getContentPane().add(playPauseMusic);
        playPauseMusic.setFocusPainted(false);
        playPauseMusic.setOpaque(false);
        playPauseMusic.setContentAreaFilled(false);
        playPauseMusic.setBorderPainted(false);

        lastMusic = new JButton("");
        lastMusic.setToolTipText("Last Audio"); //change to previous
        lastMusic.addActionListener(e -> {
            if (mp3Player != null) {
                if (currentMusicIndex - 1 >= 0) {
                    currentMusicIndex -= 1;
                    mp3Player.close();
                    mp3Player = null;
                    stopScrolling();
                    play(musicFiles[currentMusicIndex]);
                } else if (currentMusicIndex == 0) {
                    currentMusicIndex = musicFiles.length - 1;
                    mp3Player.close();
                    mp3Player = null;
                    stopScrolling();
                    play(musicFiles[currentMusicIndex]);
                }
            }
        });

        lastMusic.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                lastMusic.setIcon(new ImageIcon("sys/pictures/music/SkipBackHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lastMusic.setIcon(new ImageIcon("sys/pictures/music/SkipBack.png"));
            }
        });

        lastMusic.setBounds(50, 363, 30, 30);

        ImageIcon Last = new ImageIcon("sys/pictures/music/SkipBack.png");
        lastMusic.setIcon(Last);
        musicFrame.getContentPane().add(lastMusic);
        lastMusic.setFocusPainted(false);
        lastMusic.setOpaque(false);
        lastMusic.setContentAreaFilled(false);
        lastMusic.setBorderPainted(false);

        nextMusic = new JButton("");
        nextMusic.setToolTipText("Next Audio"); //change to skip
        nextMusic.addActionListener(e -> {
            if (mp3Player != null) {
                if (currentMusicIndex + 1 <= musicFiles.length - 1) {
                    currentMusicIndex += 1;
                    mp3Player.close();
                    mp3Player = null;
                    stopScrolling();
                    play(musicFiles[currentMusicIndex]);
                } else if (currentMusicIndex + 1 == musicFiles.length) {
                    currentMusicIndex = 0;
                    mp3Player.close();
                    mp3Player = null;
                    stopScrolling();
                    play(musicFiles[currentMusicIndex]);
                }
            }
        });

        nextMusic.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                nextMusic.setIcon(new ImageIcon("sys/pictures/music/SkipHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nextMusic.setIcon(new ImageIcon("sys/pictures/music/Skip.png"));
            }
        });

        nextMusic.setBounds(121, 363, 30, 30);

        ImageIcon Next = new ImageIcon("sys/pictures/music/Skip.png");
        nextMusic.setIcon(Next);
        musicFrame.getContentPane().add(nextMusic);
        nextMusic.setFocusPainted(false);
        nextMusic.setOpaque(false);
        nextMusic.setContentAreaFilled(false);
        nextMusic.setBorderPainted(false);

        loopMusic = new JButton("");
        loopMusic.setToolTipText("Loop Audio");
        loopMusic.addActionListener(e -> {
            loopMusic.setIcon(new ImageIcon(loopAudio ? "sys/pictures/music/RepeatHover.png" : "sys/pictures/music/Repeat.png"));
            loopMusic.setToolTipText("Loop Audio");
            loopAudio = !loopAudio;
        });

        loopMusic.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loopMusic.setIcon(new ImageIcon(loopAudio ? "sys/pictures/music/Repeat.png" : "sys/pictures/music/RepeatHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loopMusic.setIcon(new ImageIcon(loopAudio ? "sys/pictures/music/RepeatHover.png" : "sys/pictures/music/Repeat.png"));
            }
        });

        loopMusic.setBounds(121, 463, 30, 30);
        ImageIcon Loop = new ImageIcon("sys/pictures/music/Repeat.png");
        loopMusic.setIcon(Loop);
        musicFrame.getContentPane().add(loopMusic);
        loopMusic.setFocusPainted(false);
        loopMusic.setOpaque(false);
        loopMusic.setContentAreaFilled(false);
        loopMusic.setBorderPainted(false);

        stopMusic = new JButton("");
        stopMusic.setToolTipText("Stop");
        stopMusic.addActionListener(e -> {
            if (mp3Player != null) {
                mp3Player.close();
                loopAudio = false;


                musicTitleLabel.setText("No Audio Playing");
                playPauseMusic.setIcon(new ImageIcon("sys/pictures/music/play.png"));
                playPauseMusic.setToolTipText("play");

                musicStopped = true;
                playIcon = true;

                pauseLocation = 0;
                songTotalLength = 0;

                stopScrolling();
            }
        });

        stopMusic.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                stopMusic.setIcon(new ImageIcon("sys/pictures/music/StopHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                stopMusic.setIcon(new ImageIcon("sys/pictures/music/Stop.png"));
            }
        });

        stopMusic.setBounds(50, 263, 30, 30);
        ImageIcon Stop = new ImageIcon("sys/pictures/music/Stop.png");
        stopMusic.setIcon(Stop);
        musicFrame.getContentPane().add(stopMusic);
        stopMusic.setFocusPainted(false);
        stopMusic.setOpaque(false);
        stopMusic.setContentAreaFilled(false);
        stopMusic.setBorderPainted(false);

        selectMusicDir = new JButton("");
        selectMusicDir.setToolTipText("Open File");
        selectMusicDir.addActionListener(e -> new Thread(() -> {
            try {
                File selectedChildFile = new GetterUtil().getFile("Choose any mp3 file to play");
                if (!selectedChildFile.toString().endsWith("mp3")) {
                    if (mp3Player == null)
                        musicFrame.notify("Sorry, " + ConsoleFrame.getUsername() + ", but that's not an mp3 file.");
                }

                else if (selectedChildFile != null){
                    File[] neighboringFiles = selectedChildFile.getParentFile().listFiles();
                    ArrayList<File> validFiles = new ArrayList<>();

                    for (int i = 0; i < (neighboringFiles != null ? neighboringFiles.length : 0); i++) {
                        if (StringUtil.getExtension(neighboringFiles[i]).equals(".mp3"))
                            validFiles.add(neighboringFiles[i]);
                    }

                    for (int j = 0 ; j < validFiles.size() ; j++) {
                        if (validFiles.get(j).equals(selectedChildFile))
                            currentMusicIndex = j;
                    }

                    musicFiles = validFiles.toArray(new File[validFiles.size()]);
                    play(musicFiles[currentMusicIndex]);
                }
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        }, "wait thread for GetterUtil().getFile()").start());

        selectMusicDir.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                selectMusicDir.setIcon(new ImageIcon("sys/pictures/music/SelectFileHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                selectMusicDir.setIcon(new ImageIcon("sys/pictures/music/SelectFile.png"));
            }
        });

        selectMusicDir.setBounds(50, 463, 30, 30);
        ImageIcon File = new ImageIcon("sys/pictures/music/SelectFile.png");
        selectMusicDir.setIcon(File);
        musicFrame.getContentPane().add(selectMusicDir);

        shuffleMusic = new JButton("");
        shuffleMusic.setToolTipText("Shuffle");
        shuffleMusic.addActionListener(e -> {
            if (!shuffleAudio) { //!shuffle music
                shuffleMusic.setIcon(new ImageIcon("sys/pictures/music/Shuffle.png"));
                shuffleAudio = true;
            } else {
                shuffleMusic.setIcon(new ImageIcon("sys/pictures/music/ShuffleHover.png"));
                shuffleAudio = false;
            }
        });

        shuffleMusic.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                shuffleMusic.setIcon(new ImageIcon(shuffleAudio ? "sys/pictures/music/Shuffle.png" : "sys/pictures/music/ShuffleHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                shuffleMusic.setIcon(new ImageIcon(shuffleAudio ? "sys/pictures/music/ShuffleHover.png" : "sys/pictures/music/Shuffle.png"));
            }
        });

        shuffleMusic.setBounds(192, 463, 30, 30);
        shuffleMusic.setIcon(new ImageIcon("sys/pictures/music/Shuffle.png"));
        musicFrame.getContentPane().add(shuffleMusic);
        shuffleMusic.setFocusPainted(false);
        shuffleMusic.setOpaque(false);
        shuffleMusic.setContentAreaFilled(false);
        shuffleMusic.setBorderPainted(false);

        selectMusicDir.setFocusPainted(false);
        selectMusicDir.setOpaque(false);
        selectMusicDir.setContentAreaFilled(false);
        selectMusicDir.setBorderPainted(false);

        musicFrame.setLocationRelativeTo(null);
        musicFrame.setVisible(true);
        musicFrame.setAlwaysOnTop(true);
        musicFrame.setAlwaysOnTop(false);
        musicFrame.requestFocus();

        //if not null then we call initMusic
        if (StartPlaying != null && !StartPlaying.getName().equals(""))
            initMusic(StartPlaying);

        else {
            try {
                //search the user's music dir for valid music and play the first one if it exists
                //this is because a starting file was not passed in
                File[] userMusicDir = new File("users/" + ConsoleFrame.getUUID() + "/Music/" ).listFiles();
                ArrayList<File> validFiles = new ArrayList<>();
                if (userMusicDir == null)
                    return;

                for (int i = 0; i < userMusicDir.length; i++) {
                    if (userMusicDir[i].toString().endsWith(".mp3")) {
                        validFiles.add(userMusicDir[i]);

                        if (File.equals(validFiles.get(i))) {
                            currentMusicIndex = i;
                        }
                    }
                }

                musicFiles = validFiles.toArray(new File[validFiles.size()]);

                if (musicFiles.length != 0)
                    play(musicFiles[currentMusicIndex]);
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }
    }

    //given a file, we will find all neighboriong mp3 files in the same directory
    private void initMusic(File File) {
        File[] SelectedFileDir = File.getParentFile().listFiles();
        ArrayList<File> ValidFiles = new ArrayList<>();

        for (File file : SelectedFileDir) {
            if (file.toString().endsWith(".mp3")) {
                ValidFiles.add(file);
            }
        }

        //now that we've found all other music files, start playing the one originally requested
        for (int i = 0 ; i < ValidFiles.size() ; i++) {
            if (ValidFiles.get(i).equals(File)) {
                currentMusicIndex = i;
            }
        }

        musicFiles = ValidFiles.toArray(new File[ValidFiles.size()]);
        play(musicFiles[currentMusicIndex]);
    }

    //close player and exit
    public void kill() {
        if (mp3Player != null)
            this.mp3Player.close();
        stopScrolling();
        musicFrame.closeAnimation();
    }

    //main play method
    private void play(File path) {
        try {
            //close if something is playing
            if (mp3Player != null) {
                mp3Player.close();
                mp3Player = null;
            }

            //re-init player from bis and fis
            fis = new FileInputStream(path.toString());
            bis = new BufferedInputStream(fis);
            mp3Player = new Player(bis);
            songTotalLength = fis.available();
            startScrolling();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        musicVolumeSlider.setValue(musicVolumeSlider.getValue());

        //playing thread to handle play/pause icons
        new Thread(() -> {
            try {
                playPauseMusic.setIcon(new ImageIcon("sys/pictures/music/Pause.png"));
                playPauseMusic.setToolTipText("Pause");

                playIcon = false;
                mp3Player.play();

                if (loopAudio)
                    play(musicFiles[currentMusicIndex]);

                playPauseMusic.setIcon(new ImageIcon("sys/pictures/music/play.png"));
                playPauseMusic.setToolTipText("play");
                playIcon = true;
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"Flash Player Music Thread[" + musicFiles[currentMusicIndex].toString() + "]").start();
    }

    //after pause, resume music method
    private void resumeMusic() {
        startScrolling();
        new Thread(() -> {
            try {
                mp3Player.play();
                if (loopAudio)
                    play(musicFiles[currentMusicIndex]);
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"Flash Player Music Thread[" + musicFiles[currentMusicIndex].toString() + "]").start();
    }

    //private class to handle the scrolling title label
    private class ScrollLabel {
        private JLabel effectLabel;
        boolean scroll;

        ScrollLabel(JLabel effLab) {
            effectLabel = effLab;
            scroll = true;

            try {
                int maxLen = 30;
                int delay = 200;
                String title = musicFiles[currentMusicIndex].getName().replace(".mp3","");
                int len = title.length();

                if (len > maxLen) {

                    scroll = true;

                    new Thread(() -> {
                        try {
                            while (scroll) {
                                String localTitle = StringUtil.getFilename(musicFiles[currentMusicIndex]);
                                int localLen = localTitle.length();
                                effectLabel.setText(localTitle.substring(0,26));

                                if (!scroll)
                                    return;

                                Thread.sleep(2000);

                                for (int i = 0 ; i <= localLen - 26; i++) {
                                    if (!scroll)
                                        return;

                                    effectLabel.setText(localTitle.substring(i, i + 26));

                                    if (!scroll)
                                        return;

                                    Thread.sleep(delay);
                                }

                                Thread.sleep(2000);

                                for (int i = localLen - 26 ; i >= 0 ; i--) {
                                    if (!scroll)
                                        return;

                                    effectLabel.setText(localTitle.substring(i, i + 26));

                                    if (!scroll)
                                        return;

                                    Thread.sleep(delay);
                                }
                            }
                        }

                        catch (Exception e) {
                            ErrorHandler.handle(e);
                        }
                    },"Flash Player scrolling title thread[" + musicFiles[currentMusicIndex].toString() + "]").start();
                } else {
                    effectLabel.setText(title);
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }

        public void kill() {
            scroll = false;
            effectLabel.setText("No Audio Playing");
        }
    }

    private void stopScrolling() {
        if (musicScroll != null)
            musicScroll.kill();
    }

    private void startScrolling() {
        musicScroll = new ScrollLabel(musicTitleLabel);
    }
}