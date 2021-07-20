package cyder.genesis;

import cyder.handler.ErrorHandler;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.Player;

import java.io.FileInputStream;
import java.io.InputStream;

public class PausablePlayer {

    private enum Status {
        NOTSTARTED, PLAYING, PAUSED, FINISHED
    }

    //the JLayer player behind the class
    private final Player player;

    //locking object used to communicate with player thread
    private final Object playerLock = new Object();

    //status variable
    private Status playerStatus = Status.NOTSTARTED;

    public PausablePlayer(InputStream inputStream) throws Exception {
        this.player = new Player(inputStream);
    }

    public PausablePlayer(InputStream inputStream, AudioDevice audioDevice) throws Exception {
        this.player = new Player(inputStream, audioDevice);
    }

    /**
     * Starts playback (resumes if paused)
     */
    public void play()  {
        synchronized (playerLock) {
            switch (playerStatus) {
                case NOTSTARTED:
                    final Thread t = new Thread(this::playInternal,"Audio Player Interval Thread");
                    t.setDaemon(true);
                    t.setPriority(Thread.MAX_PRIORITY);
                    playerStatus = Status.PLAYING;
                    t.start();
                    break;
                case PAUSED:
                    resume();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Pauses playback. Returns true if new state is PAUSED.
     */
    public boolean pause() {
        synchronized (playerLock) {
            if (playerStatus == Status.PLAYING) {
                playerStatus = Status.PAUSED;
            }
            return playerStatus == Status.PAUSED;
        }
    }

    /**
     * Resumes the player from the stopped location
     */
    public boolean resume() {
        synchronized (playerLock) {
            if (playerStatus == Status.PAUSED) {
                playerStatus = Status.PLAYING;
                playerLock.notifyAll();
            }
            return playerStatus == Status.PLAYING;
        }
    }

    /**
     * Stops the player
     */
    public void stop() {
        synchronized (playerLock) {
            playerStatus = Status.FINISHED;
            playerLock.notifyAll();
        }
    }

    private void playInternal() {
        while (playerStatus != Status.FINISHED) {
            try {
                if (!player.play(1)) {
                    break;
                }
            } catch (Exception e) {
                break;
            }

            synchronized (playerLock) {
                while (playerStatus == Status.PAUSED) {
                    try {
                        playerLock.wait();
                    } catch (Exception e) {
                        break;
                    }
                }
            }
        }
        close();
    }

    /**
     * Close the player
     */
    public void close() {
        synchronized (playerLock) {
            playerStatus = Status.FINISHED;
        }
        try {
            player.close();
        } catch (Exception e) {
            ErrorHandler.silentHandle(e);
        }
    }

    //usage
    public static void main(String[] argv) {
        try {
            PausablePlayer player = new PausablePlayer(new FileInputStream(
                    "users/8657469f-418b-348f-ab79-8993fb4c2b84/" +
                    "Music/Machine Gun Kelly & blackbear - my ex’s best friend (Lyrics).mp3"));

            //valid calls
            player.play();
            player.pause();
            player.resume();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}