package cyder.audio.player;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.audio.AudioUtil;
import cyder.console.Console;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadRunner;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * An audio playing class which simply plays an audio file and returns the audio location when killed.
 */
final class InnerAudioPlayer {
    /**
     * The amount to offset a pause request by so that a sequential play
     * request sounds like it was paused at that instant.
     */
    private static final int PAUSE_AUDIO_REACTION_OFFSET = 10000;

    /**
     * The name of the setup thread for getting the milliseconds of the audio file.
     */
    private static final String SETUP_THREAD_NAME = "InnerAudioPlayer Setup Thread";

    /**
     * The maximum number of times an instance of this class can
     * attempt to play itself after a failure before quitting.
     */
    private static final int maxAttemptedSelfStarts = 10;

    /**
     * The one and only file this audio player can play.
     */
    private final File audioFile;

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
     * The audio player used to play audio.
     */
    private Player audioPlayer;

    /**
     * The file input stream used to calculate byte values outside of the play method.
     */
    private FileInputStream fis;

    /**
     * The number of times this audio player has attempted to invoke {@link #play()}
     * from within play after an exception was thrown.
     */
    private int selfStarts = 0;

    /**
     * Constructs a new InnerAudioPlay.
     *
     * @param audioFile the audio file for this object to handle
     */
    public InnerAudioPlayer(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());

        this.audioFile = audioFile;

        AudioPlayer.refreshAudioTitleLabel();
        initializeMillis();
    }

    /**
     * Initializes the milliseconds of {@link #audioFile}.
     */
    private void initializeMillis() {
        CyderThreadRunner.submit(() -> {
            try {
                this.totalMilliSeconds = AudioUtil.getMillisFfprobe(audioFile);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                this.totalMilliSeconds = 0;
            }
        }, SETUP_THREAD_NAME);
    }

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

            String threadName = "AudioPlayer Play Audio Thread" + CyderStrings.space + CyderStrings.openingBracket
                    + FileUtil.getFilename(audioFile) + CyderStrings.closingBracket;
            CyderThreadRunner.submit(() -> {
                try {
                    audioPlayer.play();

                    Console.INSTANCE.revalidateAudioMenuVisibility();

                    FileUtil.closeIfNotNull(fis);
                    FileUtil.closeIfNotNull(bis);
                    audioPlayer = null;
                    if (!killed) AudioPlayer.playAudioCallback();
                } catch (Exception possiblyIgnored) {
                    if (selfStarts < maxAttemptedSelfStarts) {
                        selfStarts++;
                        play();
                    } else {
                        ExceptionHandler.handle(possiblyIgnored);
                    }
                }
            }, threadName);

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
     * Kills the player if playing audio and returns the location to resume a new player object at.
     *
     * @return the location in bytes to resume a new player object at if desired
     */
    @CanIgnoreReturnValue
    public long kill() {
        long resumeLocation;
        try {
            resumeLocation = totalAudioLength - fis.available() - PAUSE_AUDIO_REACTION_OFFSET;
        } catch (Exception ignored) {
            resumeLocation = 0L;
        }

        this.killed = true;
        if (audioPlayer != null) audioPlayer.close();
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
        float percentIn;
        try {
            percentIn = (totalAudioLength - fis.available()) / (float) totalAudioLength;
        } catch (Exception ignored) {
            percentIn = 0f;
        }

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
