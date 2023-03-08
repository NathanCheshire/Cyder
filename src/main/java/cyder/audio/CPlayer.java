package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.console.Console;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadRunner;
import cyder.utils.StaticUtil;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;

/**
 * An encapsulated JLayer {@link Player} for playing singular audio files and stopping.
 */
public final class CPlayer {
    /**
     * The list of paths of audio files to ignore when logging a play audio call.
     */
    private static final ImmutableList<String> ignoreLoggingAudioPaths = ImmutableList.of(
            StaticUtil.getStaticPath("chime.mp3"),
            StaticUtil.getStaticPath("typing.mp3")
    );

    /**
     * The audio file.
     */
    private final File audioFile;

    /**
     * The file input stream for the audio file.
     */
    private FileInputStream fis;

    /**
     * The buffered input stream for the file input stream.
     */
    private BufferedInputStream bis;

    /**
     * The JLayer player object.
     */
    private Player player;

    /**
     * The runnable to invoke upon a completion event.
     */
    private Runnable onCompletionCallback;

    /**
     * Whether this player has been canceled.
     */
    private boolean canceled;

    /**
     * Whether this player is currently playing audio.
     */
    private boolean playing;

    /**
     * Constructs a new inner player object.
     *
     * @param audioFile the audio file this player will play
     */
    public CPlayer(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(audioFile));

        this.audioFile = audioFile;
    }

    /**
     * Plays the encapsulated audio file.
     */
    public void play() {
        String threadName = CyderStrings.quote + FileUtil.getFilename(audioFile)
                + CyderStrings.quote + " CPlayer";
        CyderThreadRunner.submit(() -> {
            try {
                logAudio(audioFile);
                canceled = false;
                playing = true;
                fis = new FileInputStream(audioFile);
                bis = new BufferedInputStream(fis);
                player = new Player(bis);
                player.play();
                if (!canceled && onCompletionCallback != null) onCompletionCallback.run();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            } finally {
                closeResources();
                playing = false;
                Console.INSTANCE.revalidateAudioMenuVisibility();
            }
        }, threadName);
    }

    /**
     * Cancels this player, the on completion callback will not be invoked if set.
     */
    public void cancel() {
        canceled = true;
        closeResources();
    }

    /**
     * Stops the player, the completion callback will be invoked if set.
     */
    public void stop() {
        closeResources();
    }

    /**
     * Closes all resources open by this player.
     */
    private void closeResources() {
        try {
            if (player != null) player.close();
            player = null;
            if (bis != null) bis.close();
            bis = null;
            if (fis != null) fis.close();
            fis = null;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Sets the callback to invoke upon a completion event.
     *
     * @param callback the callback to invoke upon a completion event
     * @return this player
     */
    @CanIgnoreReturnValue
    public CPlayer setOnCompletionCallback(Runnable callback) {
        onCompletionCallback = Preconditions.checkNotNull(callback);
        return this;
    }

    /**
     * Returns whether this player is currently playing audio.
     *
     * @return whether this player is currently playing audio
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * Returns the audio file of this player.
     *
     * @return the audio file of this player
     */
    public File getAudioFile() {
        return audioFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof CPlayer)) {
            return false;
        }

        CPlayer other = (CPlayer) o;
        return audioFile == other.audioFile
                && Objects.equals(other.onCompletionCallback, onCompletionCallback)
                && playing == other.playing
                && canceled == other.canceled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = audioFile.hashCode();
        ret = 31 * ret + Objects.hashCode(onCompletionCallback);
        ret = 31 * ret + Boolean.hashCode(playing);
        ret = 31 * ret + Boolean.hashCode(canceled);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CPlayer{" + "audioFile=" + audioFile
                + ", fis=" + fis
                + ", bis=" + bis
                + ", player=" + player
                + ", onCompletionCallback=" + onCompletionCallback
                + ", canceled=" + canceled
                + ", playing=" + playing
                + "}";
    }

    /**
     * Logs the provided audio file using an audio tag.
     *
     * @param file the file to log
     */
    private static void logAudio(File file) {
        String filePath = file.getAbsolutePath();
        if (!StringUtil.in(filePath, true, ignoreLoggingAudioPaths)) {
            Logger.log(LogTag.AUDIO, filePath);
        }
    }
}
