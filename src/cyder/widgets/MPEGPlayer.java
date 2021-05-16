package cyder.widgets;

import cyder.constants.CyderColors;
import cyder.enums.ArrowDirection;
import cyder.enums.StartDirection;
import cyder.enums.VanishDirection;
import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.ui.CyderSliderUI;
import cyder.utilities.IOUtil;
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
    private JButton selectMusicDir;
    private JButton playPauseMusic;
    private boolean musicStopped;
    private JButton lastMusic;
    private JButton nextMusic;
    private JButton stopMusic;
    private JButton loopMusic;
    private JLabel musicTitleLabel;
    private JLabel musicVolumeLabel;
    private int currentMusicIndex;
    private File[] musicFiles;
    private Player mp3Player;
    private BufferedInputStream bis;
    private FileInputStream fis;
    private long pauseLocation;
    private long songTotalLength;
    private boolean playIcon = true;
    private boolean loopAudio;

    //todo loop is broken if we skip to a new mp3 file
    //todo change buttons to cyderbuttons with special icons

    /**
     * This constructor takes an mp3 file to immediately start playing when FlashPlayer loads
     * @param StartPlaying - the mp3 file we want to start playing
     */
    public MPEGPlayer(File StartPlaying) {
        if (musicFrame != null)
            musicFrame.closeAnimation();

        musicFrame = new CyderFrame(1000,563,new ImageIcon("src/cyder//sys/pictures/mp3.png"));
        musicFrame.setTitle("Flash Player");

        musicFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
            if (mp3Player != null)
                mp3Player.close();
            }
        });

        ImageIcon mini1 = new ImageIcon("src/cyder//sys/pictures/minimize1.png");
        ImageIcon mini2 = new ImageIcon("src/cyder//sys/pictures/minimize2.png");

        ImageIcon close1 = new ImageIcon("src/cyder//sys/pictures/Close1.png");
        ImageIcon close2 = new ImageIcon("src/cyder//sys/pictures/Close2.png");

        musicTitleLabel = new JLabel("", SwingConstants.CENTER);

        musicTitleLabel.setBounds(310, 38, 400, 30);

        musicTitleLabel.setToolTipText("Currently Playing");

        musicTitleLabel.setFont(new Font("tahoma", Font.BOLD, 18));

        musicTitleLabel.setForeground(CyderColors.vanila);

        musicTitleLabel.setText("No Audio Currently Playing");

        musicFrame.getContentPane().add(musicTitleLabel);

        musicVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);

        CyderSliderUI UI = new CyderSliderUI(musicVolumeSlider);

        UI.setFillColor(CyderColors.vanila);
        UI.setOutlineColor(CyderColors.vanila);
        UI.setNewValColor(CyderColors.vanila);
        UI.setOldValColor(CyderColors.regularRed);
        UI.setStroke(new BasicStroke(3.0f));

        musicVolumeSlider.setUI(UI);

        musicVolumeSlider.setBounds(352, 499, 385, 63);

        musicVolumeSlider.setMinimum(0);

        musicVolumeSlider.setMaximum(100);

        musicVolumeSlider.setMajorTickSpacing(5);

        musicVolumeSlider.setMinorTickSpacing(1);

        musicVolumeSlider.setPaintTicks(false);

        musicVolumeSlider.setPaintLabels(false);

        musicVolumeSlider.setVisible(true);

        musicVolumeSlider.setValue(50);

        musicVolumeSlider.setFont(new Font("HeadPlane", Font.BOLD, 18));

        musicVolumeSlider.addChangeListener(e -> {
            Port.Info Source = Port.Info.SPEAKER;
            Port.Info Headphones = Port.Info.HEADPHONE;

            try {
                if (AudioSystem.isLineSupported(Source)) {
                    Port outline = (Port) AudioSystem.getLine(Source);
                    outline.open();

                    FloatControl VolumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);

                    VolumeControl.setValue((float) (musicVolumeSlider.getValue() * 0.01));
                    musicVolumeLabel.setText(musicVolumeSlider.getValue() + "%");
                }

                if (AudioSystem.isLineSupported(Headphones)) {
                    Port outline = (Port) AudioSystem.getLine(Headphones);
                    outline.open();

                    FloatControl VolumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);

                    VolumeControl.setValue((float) (musicVolumeSlider.getValue() * 0.01));
                    musicVolumeLabel.setText(musicVolumeSlider.getValue() + "%");
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

        musicVolumeLabel = new JLabel("", SwingConstants.CENTER);

        musicVolumeLabel.setBounds(250, 499, 100, 60);

        musicVolumeLabel.setToolTipText("");

        musicVolumeLabel.setFont(new Font("tahoma", Font.BOLD, 18));

        musicVolumeLabel.setForeground(CyderColors.vanila);

        musicVolumeLabel.setText(musicVolumeSlider.getValue() + "%");

        musicFrame.getContentPane().add(musicVolumeLabel);

        playPauseMusic = new JButton("");

        playPauseMusic.setToolTipText("play");

        playPauseMusic.addActionListener(e -> {
            if (mp3Player != null) {
                if (!playIcon) {
                    playPauseMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/play.png"));
                    playPauseMusic.setToolTipText("play");
                    playIcon = true;
                }

                else {
                    playPauseMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/Pause.png"));
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
                if (playIcon) {
                    playPauseMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/PlayHover.png"));
                }

                else {
                    playPauseMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/PauseHover.png"));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (playIcon) {
                    playPauseMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/play.png"));
                }

                else
                {
                    playPauseMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/Pause.png"));
                }
            }
        });

        playPauseMusic.setBounds(121, 263, 75, 75);

        ImageIcon Play = new ImageIcon("src/cyder//sys/pictures/play.png");

        playPauseMusic.setIcon(Play);

        musicFrame.getContentPane().add(playPauseMusic);

        playPauseMusic.setFocusPainted(false);

        playPauseMusic.setOpaque(false);

        playPauseMusic.setContentAreaFilled(false);

        playPauseMusic.setBorderPainted(false);

        lastMusic = new JButton("");

        lastMusic.setToolTipText("Last Audio");

        lastMusic.addActionListener(e -> {
            loopAudio = false;

            if (mp3Player != null) {
                if (currentMusicIndex - 1 >= 0) {
                    currentMusicIndex -= 1;
                    mp3Player.close();
                    mp3Player = null;
                    stopScrolling();
                    play(musicFiles[currentMusicIndex]);
                }

                else if (currentMusicIndex == 0) {
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
                lastMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/SkipBackHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lastMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/SkipBack.png"));
            }
        });

        lastMusic.setBounds(121, 363, 75, 75);

        ImageIcon Last = new ImageIcon("src/cyder//sys/pictures/SkipBack.png");

        lastMusic.setIcon(Last);

        musicFrame.getContentPane().add(lastMusic);

        lastMusic.setFocusPainted(false);

        lastMusic.setOpaque(false);

        lastMusic.setContentAreaFilled(false);

        lastMusic.setBorderPainted(false);

        nextMusic = new JButton("");

        nextMusic.setToolTipText("Next Audio");

        nextMusic.addActionListener(e -> {
            loopAudio = false;

            if (mp3Player != null) {
                if (currentMusicIndex + 1 <= musicFiles.length - 1) {
                    currentMusicIndex += 1;
                    mp3Player.close();
                    mp3Player = null;
                    stopScrolling();
                    play(musicFiles[currentMusicIndex]);
                }

                else if (currentMusicIndex + 1 == musicFiles.length) {
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
                nextMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/SkipHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nextMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/Skip.png"));
            }
        });

        nextMusic.setBounds(121, 463, 75, 75);

        ImageIcon Next = new ImageIcon("src/cyder//sys/pictures/Skip.png");

        nextMusic.setIcon(Next);

        musicFrame.getContentPane().add(nextMusic);

        nextMusic.setFocusPainted(false);

        nextMusic.setOpaque(false);

        nextMusic.setContentAreaFilled(false);

        nextMusic.setBorderPainted(false);

        loopMusic = new JButton("");

        loopMusic.setToolTipText("Loop Audio");

        loopMusic.addActionListener(e -> {
            if (!loopAudio) {
                loopMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/Repeat.png"));
                loopMusic.setToolTipText("Loop Audio");
                loopAudio = true;
            }

            else {
                loopMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/NoRepeat.png"));
                loopMusic.setToolTipText("Loop Audio");
                loopAudio = false;
            }
        });

        loopMusic.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (loopAudio)
                {
                    loopMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/RepeatHover.png"));
                }

                else
                {
                    loopMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/NoRepeatHover.png"));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (loopAudio)
                {
                    loopMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/Repeat.png"));
                }

                else
                {
                    loopMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/NoRepeat.png"));
                }
            }
        });

        loopMusic.setBounds(50, 363, 76, 76);

        ImageIcon Loop = new ImageIcon("src/cyder//sys/pictures/NoRepeat.png");

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

                musicTitleLabel.setText("No audio currently playing");
                playPauseMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/play.png"));
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
                stopMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/StopHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                stopMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/Stop.png"));
            }
        });

        stopMusic.setBounds(50, 263, 75, 75);

        ImageIcon Stop = new ImageIcon("src/cyder//sys/pictures/Stop.png");

        stopMusic.setIcon(Stop);

        musicFrame.getContentPane().add(stopMusic);

        stopMusic.setFocusPainted(false);

        stopMusic.setOpaque(false);

        stopMusic.setContentAreaFilled(false);

        stopMusic.setBorderPainted(false);

        selectMusicDir = new JButton("");

        selectMusicDir.setToolTipText("Open File");

        selectMusicDir.addActionListener(e -> {
            File SelectedFile = IOUtil.getFile();

            if (SelectedFile == null)
                return;

            if (!SelectedFile.toString().endsWith("mp3")) {
                if (mp3Player == null) {
                    GenericInform.inform("Sorry, " + ConsoleFrame.getUsername() + ", but that's not an mp3 file.","");
                }
            }

            else {
                File[] SelectedFileDir = SelectedFile.getParentFile().listFiles();
                ArrayList<File> ValidFiles = new ArrayList<>();
                for (int i = 0; i < (SelectedFileDir != null ? SelectedFileDir.length : 0); i++) {
                    if (SelectedFileDir[i].toString().endsWith(".mp3")) {
                        ValidFiles.add(SelectedFileDir[i]);
                    }
                }

                for (int j = 0 ; j < ValidFiles.size() ; j++) {
                    if (ValidFiles.get(j).equals(SelectedFile)) {
                        currentMusicIndex = j;
                    }
                }

                musicFiles = ValidFiles.toArray(new File[ValidFiles.size()]);
                play(musicFiles[currentMusicIndex]);
            }
        });

        selectMusicDir.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                selectMusicDir.setIcon(new ImageIcon("src/cyder//sys/pictures/SelectFileHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                selectMusicDir.setIcon(new ImageIcon("src/cyder//sys/pictures/SelectFile.png"));
            }
        });

        selectMusicDir.setBounds(50, 463, 75, 75);

        ImageIcon File = new ImageIcon("src/cyder//sys/pictures/SelectFile.png");

        selectMusicDir.setIcon(File);

        musicFrame.getContentPane().add(selectMusicDir);

        selectMusicDir.setFocusPainted(false);

        selectMusicDir.setOpaque(false);

        selectMusicDir.setContentAreaFilled(false);

        selectMusicDir.setBorderPainted(false);

        musicFrame.setLocationRelativeTo(null);
        musicFrame.setVisible(true);
        musicFrame.setAlwaysOnTop(true);
        musicFrame.setAlwaysOnTop(false);
        musicFrame.requestFocus();

        musicFrame.notify("Welcome to FlashPlayer (haha get it?)",3000, ArrowDirection.TOP, StartDirection.TOP, VanishDirection.TOP,415);

        //if not null then we call initMusi
        if (StartPlaying != null && !StartPlaying.getName().equals("")) {
            initMusic(StartPlaying);
        }

        else {
            try {
                //search the user's music dir for valid music and play the first one if it exists
                // this is because StartPlaying was not passed in

                File[] userMusicDir = new File("src/users/" + ConsoleFrame.getUUID() + "/Music/" ).listFiles();
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

                if (musicFiles.length != 0) {
                    play(musicFiles[currentMusicIndex]);
                }
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
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        //playing thread to handle play/pause icons
        new Thread(() -> {
            try {
                playPauseMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/Pause.png"));
                playPauseMusic.setToolTipText("Pause");

                playIcon = false;
                mp3Player.play();

                if (loopAudio) {
                    play(musicFiles[currentMusicIndex]);
                }

                playPauseMusic.setIcon(new ImageIcon("src/cyder//sys/pictures/play.png"));
                playPauseMusic.setToolTipText("play");
                playIcon = true;
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"Flash Player[" + musicFiles[currentMusicIndex].toString() + "]").start();
    }

    //after pause, resume music method
    private void resumeMusic() {
        startScrolling();
        new Thread(() -> {
            try {
                mp3Player.play();
                if (loopAudio) {
                    play(musicFiles[currentMusicIndex]);
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }).start();
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
                            while (true) {
                                String localTitle = musicFiles[currentMusicIndex].getName().replace(".mp3","");
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
                    }).start();
                }

                else {
                    effectLabel.setText(title);
                }
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }

        public void kill() {
            scroll = false;
            effectLabel.setText("No Audio Currently Playing");
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
