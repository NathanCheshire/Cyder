package cyder.widgets;

import cyder.exception.FatalException;
import cyder.handler.ErrorHandler;
import cyder.ui.CyderFrame;
import cyder.utilities.StringUtil;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import javax.swing.*;
import java.io.*;
import java.util.LinkedList;

public class AudioPlayer {

    //ui components
    private JLabel musicTitleLabel;
    private CyderFrame musicFrame;
    private JSlider musicVolumeSlider;
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

    public AudioPlayer(File startPlaying) {

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
     * Ends any and all threads having to do with this object to free up resources
     */
    public void kill() {
        this.stopScrolling();
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


        //todo on pause,
        pauseLocation = totalLength - fis.available();

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

                //finished with song?
                // handle shuffle / loop here

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
            String title = "TODO";

            if (title.length() > maxLen) {
                scroll = true;

                new Thread(() -> {
                    try {
                        while (scroll) {
                            String localTitle = StringUtil.getFilename("TODO");
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
                },"Flash Player scrolling title thread[" + "MUSIC NAME HERE" + "]").start();
            } else {
                musicTitleLabel.setText(title);
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
