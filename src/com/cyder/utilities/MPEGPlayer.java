package com.cyder.utilities;

import com.cyder.ui.CyderSliderUI;
import com.cyder.ui.DragLabel;
import javazoom.jl.player.Player;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Port;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class MPEGPlayer {
    private ScrollLabel musicScroll;
    private JFrame musicFrame;
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
    private boolean repeatAudio;

    Util musicUtil = new Util();

    public MPEGPlayer(File StartPlaying, String username, String UUID) {
        musicUtil.setUsername(username);
        musicUtil.setUserUUID(UUID);

        if (musicFrame != null) {
            musicUtil.closeAnimation(musicFrame);
            musicFrame.dispose();
        }

        musicFrame = new JFrame();

        musicFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        musicFrame.setUndecorated(true);

        musicFrame.setTitle("Music Player");

        musicFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (mp3Player != null)
                    mp3Player.close();
            }
        });

        musicFrame.setIconImage(musicUtil.getCyderIcon().getImage());

        musicFrame.setBounds(0, 0, 1000, 563);

        JLabel musicLabel = new JLabel();

        musicLabel.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\mp3.png"));

        musicLabel.setBounds(0, 0, 1000, 563);

        musicFrame.setContentPane(musicLabel);

        ImageIcon mini1 = new ImageIcon("src\\com\\cyder\\io\\pictures\\minimize1.png");
        ImageIcon mini2 = new ImageIcon("src\\com\\cyder\\io\\pictures\\minimize2.png");

        ImageIcon close1 = new ImageIcon("src\\com\\cyder\\io\\pictures\\Close1.png");
        ImageIcon close2 = new ImageIcon("src\\com\\cyder\\io\\pictures\\Close2.png");

        DragLabel musicDragLabel = new DragLabel(1000,22,musicFrame);
        musicDragLabel.setBackground(new Color(20,20,20));
        musicDragLabel.setBounds(0, 0, 1000, 22);
        musicLabel.add(musicDragLabel);

        musicTitleLabel = new JLabel("", SwingConstants.CENTER);

        musicTitleLabel.setBounds(280, 38, 400, 30);

        musicTitleLabel.setToolTipText("Currently Playing");

        musicTitleLabel.setFont(new Font("tahoma", Font.BOLD, 18));

        musicTitleLabel.setForeground(musicUtil.vanila);

        musicTitleLabel.setText("No Audio Currently Playing");

        musicLabel.add(musicTitleLabel);

        musicVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);

        CyderSliderUI UI = new CyderSliderUI(musicVolumeSlider);

        UI.setFillColor(musicUtil.vanila);
        UI.setOutlineColor(musicUtil.vanila);
        UI.setNewValColor(musicUtil.vanila);
        UI.setOldValColor(musicUtil.vanila);
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

            if (AudioSystem.isLineSupported(Source)) {
                try {
                    Port outline = (Port) AudioSystem.getLine(Source);
                    outline.open();
                    FloatControl VolumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);
                    VolumeControl.setValue((float) (musicVolumeSlider.getValue() * 0.01));
                    musicVolumeLabel.setText(musicVolumeSlider.getValue() + "%");
                } catch (Exception ex) {
                    musicUtil.handle(ex);
                }
            }

            if (AudioSystem.isLineSupported(Headphones)) {
                try {
                    Port outline = (Port) AudioSystem.getLine(Headphones);
                    outline.open();
                    FloatControl VolumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);
                    VolumeControl.setValue((float) (musicVolumeSlider.getValue() * 0.01));
                    musicVolumeLabel.setText(musicVolumeSlider.getValue() + "%");
                } catch (Exception exc) {
                    musicUtil.handle(exc);
                }
            }
        });

        musicVolumeSlider.setOpaque(false);

        musicVolumeSlider.setToolTipText("Volume");

        musicVolumeSlider.setFocusable(false);

        musicLabel.add(musicVolumeSlider);

        musicVolumeLabel = new JLabel("", SwingConstants.CENTER);

        musicVolumeLabel.setBounds(250, 499, 100, 60);

        musicVolumeLabel.setToolTipText("");

        musicVolumeLabel.setFont(new Font("tahoma", Font.BOLD, 18));

        musicVolumeLabel.setForeground(musicUtil.vanila);

        musicVolumeLabel.setText(musicVolumeSlider.getValue() + "%");

        musicLabel.add(musicVolumeLabel);

        playPauseMusic = new JButton("");

        playPauseMusic.setToolTipText("play");

        playPauseMusic.addActionListener(e -> {
            if (mp3Player != null) {
                if (!playIcon) {
                    playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\play.png"));
                    playPauseMusic.setToolTipText("play");
                    playIcon = true;
                }

                else {
                    playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Pause.png"));
                    playPauseMusic.setToolTipText("Pause");
                    playIcon = false;
                }

                if (playIcon) {
                    try {
                        pauseLocation = fis.available();
                    }

                    catch (Exception exc) {
                        musicUtil.handle(exc);
                    }

                    stopScrolling();
                    mp3Player.close();
                }

                else {
                    try {
                        fis = new FileInputStream(musicFiles[currentMusicIndex]);
                        bis = new BufferedInputStream(fis);
                        mp3Player = new Player(bis);

                        if (pauseLocation == 0) {
                            fis.skip(0);
                        }

                        else {

                            if (songTotalLength - pauseLocation <= 0) {
                                fis.skip(0);
                            }

                            else {
                                fis.skip(songTotalLength - pauseLocation);
                            }
                        }

                        resumeMusic();
                    }

                    catch (Exception ex) {
                        musicUtil.handle(ex);
                    }
                }
            }
        });

        playPauseMusic.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (playIcon) {
                    playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\PlayHover.png"));
                }

                else {
                    playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\PauseHover.png"));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (playIcon)
                {
                    playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\play.png"));
                }

                else
                {
                    playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Pause.png"));
                }
            }
        });

        playPauseMusic.setBounds(121, 263, 75, 75);

        ImageIcon Play = new ImageIcon("src\\com\\cyder\\io\\pictures\\play.png");

        playPauseMusic.setIcon(Play);

        musicLabel.add(playPauseMusic);

        playPauseMusic.setFocusPainted(false);

        playPauseMusic.setOpaque(false);

        playPauseMusic.setContentAreaFilled(false);

        playPauseMusic.setBorderPainted(false);

        lastMusic = new JButton("");

        lastMusic.setToolTipText("Last Audio");

        lastMusic.addActionListener(e -> {
            repeatAudio = false;

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

        lastMusic.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lastMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\SkipBackHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lastMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\SkipBack.png"));
            }
        });

        lastMusic.setBounds(121, 363, 75, 75);

        ImageIcon Last = new ImageIcon("src\\com\\cyder\\io\\pictures\\SkipBack.png");

        lastMusic.setIcon(Last);

        musicLabel.add(lastMusic);

        lastMusic.setFocusPainted(false);

        lastMusic.setOpaque(false);

        lastMusic.setContentAreaFilled(false);

        lastMusic.setBorderPainted(false);

        nextMusic = new JButton("");

        nextMusic.setToolTipText("Next Audio");

        nextMusic.addActionListener(e -> {
            repeatAudio = false;

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

        nextMusic.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                nextMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\SkipHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nextMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Skip.png"));
            }
        });

        nextMusic.setBounds(121, 463, 75, 75);

        ImageIcon Next = new ImageIcon("src\\com\\cyder\\io\\pictures\\Skip.png");

        nextMusic.setIcon(Next);

        musicLabel.add(nextMusic);

        nextMusic.setFocusPainted(false);

        nextMusic.setOpaque(false);

        nextMusic.setContentAreaFilled(false);

        nextMusic.setBorderPainted(false);

        loopMusic = new JButton("");

        loopMusic.setToolTipText("Loop Audio");

        loopMusic.addActionListener(e -> {
            if (!repeatAudio) {
                loopMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Repeat.png"));
                loopMusic.setToolTipText("Loop Audio");
                repeatAudio = true;
            }

            else {
                loopMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\NoRepeat.png"));
                loopMusic.setToolTipText("Loop Audio");
                repeatAudio = false;
            }
        });

        loopMusic.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (repeatAudio)
                {
                    loopMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\RepeatHover.png"));
                }

                else
                {
                    loopMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\NoRepeatHover.png"));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (repeatAudio)
                {
                    loopMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Repeat.png"));
                }

                else
                {
                    loopMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\NoRepeat.png"));
                }
            }
        });

        loopMusic.setBounds(50, 363, 76, 76);

        ImageIcon Loop = new ImageIcon("src\\com\\cyder\\io\\pictures\\NoRepeat.png");

        loopMusic.setIcon(Loop);

        musicLabel.add(loopMusic);

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
                playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\play.png"));
                playPauseMusic.setToolTipText("play");
                playIcon = true;
                pauseLocation = 0;
                songTotalLength = 0;
                musicStopped = true;
                stopScrolling();
            }
        });

        stopMusic.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                stopMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\StopHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                stopMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Stop.png"));
            }
        });

        stopMusic.setBounds(50, 263, 75, 75);

        ImageIcon Stop = new ImageIcon("src\\com\\cyder\\io\\pictures\\Stop.png");

        stopMusic.setIcon(Stop);

        musicLabel.add(stopMusic);

        stopMusic.setFocusPainted(false);

        stopMusic.setOpaque(false);

        stopMusic.setContentAreaFilled(false);

        stopMusic.setBorderPainted(false);

        selectMusicDir = new JButton("");

        selectMusicDir.setToolTipText("Open File");

        selectMusicDir.addActionListener(e -> {
            File SelectedFile = musicUtil.getFile();

            if (!SelectedFile.toString().endsWith("mp3")) {
                if (mp3Player == null) {
                    musicUtil.inform("Sorry, " + musicUtil.getUsername() + ", but that's not an mp3 file.","", 400, 200);
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

        selectMusicDir.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                selectMusicDir.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\SelectFileHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                selectMusicDir.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\SelectFile.png"));
            }
        });

        selectMusicDir.setBounds(50, 463, 75, 75);

        ImageIcon File = new ImageIcon("src\\com\\cyder\\io\\pictures\\SelectFile.png");

        selectMusicDir.setIcon(File);

        musicLabel.add(selectMusicDir);

        selectMusicDir.setFocusPainted(false);

        selectMusicDir.setOpaque(false);

        selectMusicDir.setContentAreaFilled(false);

        selectMusicDir.setBorderPainted(false);

        musicFrame.setLocationRelativeTo(null);

        musicFrame.setVisible(true);

        musicFrame.setAlwaysOnTop(true);

        musicFrame.setAlwaysOnTop(false);

        musicFrame.requestFocus();

        if (StartPlaying != null && !StartPlaying.getName().equals("")) {
            initMusic(StartPlaying);
        }

        else {
            try {
                File[] SelectedFileDir = new File("src\\com\\cyder\\users\\" + musicUtil.getUserUUID() + "\\Music\\" ).listFiles();
                ArrayList<File> ValidFiles = new ArrayList<>();
                if (SelectedFileDir == null)
                    return;

                for (int i = 0; i < SelectedFileDir.length; i++) {
                    if (SelectedFileDir[i].toString().endsWith(".mp3")) {
                        ValidFiles.add(SelectedFileDir[i]);

                        if (File.equals(ValidFiles.get(i))) {
                            currentMusicIndex = i;
                        }
                    }
                }

                musicFiles = ValidFiles.toArray(new File[ValidFiles.size()]);

                if (musicFiles.length != 0) {
                    play(musicFiles[currentMusicIndex]);
                }
            }

            catch (Exception e) {
                musicUtil.handle(e);
            }
        }
    }

    private void initMusic(File File) {
        File[] SelectedFileDir = File.getParentFile().listFiles();
        ArrayList<File> ValidFiles = new ArrayList<>();

        for (java.io.File file : SelectedFileDir) {
            if (file.toString().endsWith(".mp3")) {
                ValidFiles.add(file);
            }
        }

        for (int i = 0 ; i < ValidFiles.size() ; i++) {
            if (ValidFiles.get(i).equals(File)) {
                currentMusicIndex = i;
            }
        }

        musicFiles = ValidFiles.toArray(new File[ValidFiles.size()]);
        play(musicFiles[currentMusicIndex]);
    }

    public void kill() {
        if (mp3Player != null)
            this.mp3Player.close();
        musicUtil.closeAnimation(musicFrame);
        musicFrame.dispose();
    }

    private void play(File path) {
        try {
            if (mp3Player != null) {
                mp3Player.close();
                mp3Player = null;
            }

            fis = new FileInputStream(path.toString());
            bis = new BufferedInputStream(fis);
            mp3Player = new Player(bis);
            songTotalLength = fis.available();
            startScrolling();
        }

        catch (Exception e) {
            musicUtil.handle(e);
        }

        new Thread(() -> {
            try {
                playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Pause.png"));
                playPauseMusic.setToolTipText("Pause");

                playIcon = false;
                mp3Player.play();

                if (repeatAudio) {
                    play(musicFiles[currentMusicIndex]);
                }

                playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\play.png"));
                playPauseMusic.setToolTipText("play");
                playIcon = true;
            }

            catch (Exception e) {
                musicUtil.handle(e);
            }
        }).start();
    }

    private void resumeMusic() {
        startScrolling();
        new Thread(() -> {
            try {
                mp3Player.play();
                if (repeatAudio) {
                    play(musicFiles[currentMusicIndex]);
                }
            } catch (Exception e) {
                musicUtil.handle(e);
            }
        }).start();
    }

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
                            musicUtil.handle(e);
                        }
                    }).start();
                }

                else {
                    effectLabel.setText(title);
                }
            }

            catch (Exception e) {
                musicUtil.handle(e);
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
