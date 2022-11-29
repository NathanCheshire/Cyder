package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.Future;

/**
 * An inner class for easily playing a single audio file
 */
public class InnerAudioPlayer {
    /**
     * The one and only file this audio player can play.
     */
    private final File audioFile;

    /**
     * Constructs a new InnerAudioPlay.
     *
     * @param audioFile the audio file for this object to handle
     */
    public InnerAudioPlayer(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());

        this.audioFile = audioFile;

        setup();
    }

    /**
     * Whether this object
     */
    private boolean killed;

    /**
     * The location the current audio file was paused/stopped at.
     */
    private long pauseLocation;

    /**
     * The total audio length of the current audio file.
     */
    private long totalAudioLength;

    /**
     * The total number of milliseconds in this audio file.
     */
    private int totalMilliSeconds = 0;

    /**
     * The name of the setup thread for getting the milliseconds of the audio file.
     */
    private static final String SETUP_THREAD_NAME = "InnerAudioPlayer Setup Thread";

    /**
     * Performs necessary setup actions such as refreshing the title label.
     */
    private void setup() {
        AudioPlayer.refreshAudioTitleLabel();

        CyderThreadRunner.submit(() -> {
            try {
                Future<Integer> futureTotalMilliSeconds = AudioUtil.getMillisMutagen(audioFile);
                this.totalMilliSeconds = futureTotalMilliSeconds.get();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, SETUP_THREAD_NAME);
    }

    /**
     * The audio player used to play audio.
     */
    private Player audioPlayer;

    /**
     * The file input stream used to calculate byte values outside of the play method.
     */
    private FileInputStream fis;

    /**
     * The maximum number of times a replay can be invoked.
     */
    private static final int MAX_REPLAYS = 10;

    /**
     * The number of times this audio player has attempted to invoke {@link #play()}
     * from within play after an exception was thrown.
     */
    private int replays = 0;

    /**
     * Starts playing the provided audio file at the optionally provided location.
     */
    public void play() {
        try {
            fis = new FileInputStream(audioFile);
            totalAudioLength = fis.available();
            long bytesSkipped = fis.skip(Math.max(0, pauseLocation));

            if (fis.available() != totalAudioLength - bytesSkipped) {
                throw new IllegalStateException("FileInputStream failed to skip requested bytes: " + bytesSkipped);
            }

            BufferedInputStream bis = new BufferedInputStream(fis);

            Console.INSTANCE.revalidateAudioMenuVisibility();

            audioPlayer = new Player(bis);

            CyderThreadRunner.submit(() -> {
                try {
                    audioPlayer.play();

                    Console.INSTANCE.revalidateAudioMenuVisibility();

                    FileUtil.closeIfNotNull(fis);
                    FileUtil.closeIfNotNull(bis);
                    audioPlayer = null;

                    if (!killed) {
                        AudioPlayer.playAudioCallback();
                    }
                } catch (Exception possibleIgnored) {
                    if (replays < MAX_REPLAYS) {
                        replays++;
                        play();
                    } else {
                        ExceptionHandler.handle(possibleIgnored);
                    }
                }
            }, "AudioPlayer Play Audio Thread [" + FileUtil.getFilename(audioFile)
                    + CyderStrings.closingBracket);

            AudioPlayer.refreshPlayPauseButtonIcon();
        } catch (Exception ignored) {}
    }

    /**
     * Returns whether this object is playing audio.
     *
     * @return whether this object is playing audio
     */
    public boolean isPlaying() {
        return audioPlayer != null;
    }

    /**
     * Pauses the audio player.
     */
    public void stop() {
        audioPlayer.close();
    }

    /**
     * Returns whether this inner audio player has been killed.
     *
     * @return whether this inner audio player has been killed
     */
    public boolean isKilled() {
        return killed;
    }

    /**
     * The amount to offset a pause request by so that a sequential play
     * request sounds like it was paused at that instant.
     */
    private static final int PAUSE_AUDIO_REACTION_OFFSET = 10000;

    /**
     * Kills the player if playing audio and returns the location to resume a new player object at.
     *
     * @return the location in bytes to resume a new player object at if desired
     */
    @CanIgnoreReturnValue
    public long kill() {
        long resumeLocation = 0L;

        try {
            resumeLocation = totalAudioLength - fis.available() - PAUSE_AUDIO_REACTION_OFFSET;
        } catch (Exception ignored) {}

        this.killed = true;

        if (audioPlayer != null) {
            audioPlayer.close();
        }

        fis = null;

        return resumeLocation;
    }

    /**
     * Returns the total computed audio length (bytes).
     *
     * @return the total computed audio length (bytes)
     */
    public long getTotalAudioLength() {
        return totalAudioLength;
    }

    /**
     * Sets the location this player should start playing at when {@link InnerAudioPlayer#play()} is invoked.
     *
     * @param pauseLocation the location this player should start from
     */
    public void setLocation(long pauseLocation) {
        this.pauseLocation = pauseLocation;
    }

    /**
     * Returns the percent into the current audio this player object is.
     *
     * @return the percent into the current audio this player object is
     */
    public float getPercentIn() {
        float percentIn = 0f;

        try {
            percentIn = (totalAudioLength - fis.available()) / (float) totalAudioLength;
        } catch (Exception ignored) {}

        return percentIn;
    }

    /**
     * Returns the milliseconds into the current audio this player object is.
     *
     * @return the milliseconds into the current audio this player object is
     */
    public long getMillisecondsIn() {
        return (long) (totalMilliSeconds * getPercentIn());
    }
}
