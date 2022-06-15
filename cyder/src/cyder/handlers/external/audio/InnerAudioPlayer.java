package cyder.handlers.external.audio;

import com.google.common.base.Preconditions;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.utilities.FileUtil;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * An inner class for easily playing a single audio file
 */
class InnerAudioPlayer {
    private final File audioFile;

    public InnerAudioPlayer(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());

        this.audioFile = audioFile;

        setup();
    }

    private boolean killed;
    private boolean isPaused;

    /**
     * The file input stream to grab the audio data.
     */
    private FileInputStream fis;

    /**
     * The JLayer player used to play the audio.
     */
    private Player audioPlayer;

    /**
     * The location the current audio file was paused/stopped at.
     */
    private long pauseLocation;

    /**
     * The total audio length of the current audio file.
     */
    private long totalAudioLength;

    /**
     * Performs necessary setup actions such as refreshing the title label.
     */
    private void setup() {
        if (!killed) {
            AudioPlayer.refreshAudioTitleLabel();
            // todo maybe other actions here
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void play() {
        try {
            fis = new FileInputStream(audioFile);
            totalAudioLength = fis.available();
            fis.skip(Math.max(0, pauseLocation));

            BufferedInputStream bis = new BufferedInputStream(fis);

            ConsoleFrame.INSTANCE.revalidateAudioMenuVisibility();

            // todo how to handle these
            lastAction = AudioPlayer.LastAction.Play;
            audioLocationUpdater.resumeTimer();

            audioPlayer = new Player(bis);

            isPaused = false;

            CyderThreadRunner.submit(() -> {
                try {
                    audioPlayer.play();

                    ConsoleFrame.INSTANCE.revalidateAudioMenuVisibility();

                    FileUtil.closeIfNotNull(fis);
                    FileUtil.closeIfNotNull(bis);

                    if (audioPlayer != null) {
                        audioPlayer.close();
                        audioPlayer = null;
                        fis = null;
                    }

                    if (!killed && !isPaused) {
                        AudioPlayer.playAudioCallback();
                    }
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "AudioPlayer Play Audio Thread [" + FileUtil.getFilename(audioFile) + "]");

            AudioPlayer.refreshPlayPauseButtonIcon();
        } catch (Exception ignored) {}
    }

    /**
     * The amount to offset a pause request by so that a sequential play
     * request sounds like it was paused at that instant.
     */
    private static final int PAUSE_AUDIO_REACTION_OFFSET = 10000;

    /**
     * Pauses the audio player.
     */
    public void pause() {
        if (isPaused) {
            return;
        }

        try {
            pauseLocation = totalAudioLength - fis.available() - PAUSE_AUDIO_REACTION_OFFSET;
            audioPlayer.close();
            audioPlayer = null;
            fis = null;
            isPaused = true;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public void resume() {
        if (!isPaused) {
            throw new IllegalMethodException("Audio is not paused");
        }

        play();
    }

    public boolean isKilled() {
        return killed;
    }

    // todo need to refresh button on actions
    // todo slider needs to immediately reset position when audio changes

    public void kill() {
        this.killed = true;

        if (audioPlayer != null) {
            audioPlayer.close();
        }

        audioPlayer = null;
        fis = null;
    }

    public long getTotalAudioLength() {
        return totalAudioLength;
    }

    public void setLocation(long pauseLocation) {
        this.pauseLocation = pauseLocation;
    }

    /**
     * Returns whether audio is currently being played via this InnerAudioPlayer.
     *
     * @return whether audio is currently being played via this InnerAudioPlayer
     */
    public boolean isPlaying() {
        return audioPlayer != null && !audioPlayer.isComplete();
    }

    /**
     * Returns the raw pause location of the exact number of bytes played by fis.
     *
     * @return the raw pause location of the exact number of bytes played by fis
     * @throws IllegalArgumentException if the raw pause location could not be polled
     */
    public long getRemainingBytes() {
        try {
            if (fis != null) {
                return totalAudioLength - fis.available();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new IllegalArgumentException("Could not poll remaining bytes");
    }

    public long getMillisecondsIn() {
        return 0; // todo implement me
    }

    public boolean isPaused() {
        return isPaused;
    }
}
