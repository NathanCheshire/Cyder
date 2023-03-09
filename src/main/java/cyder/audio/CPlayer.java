package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.console.Console;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.threads.CyderThreadRunner;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Objects;

/**
 * An encapsulated JLayer {@link Player} for playing singular audio files and stopping.
 */
public final class CPlayer {
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
    private final ArrayList<Runnable> onCompletionCallback;

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
        this.onCompletionCallback = new ArrayList<>();
    }

    /**
     * Plays the encapsulated audio file.
     */
    public void play() {
        CyderThreadRunner.submit(() -> {
            try {
                logAudio(audioFile);
                canceled = false;
                playing = true;
                fis = new FileInputStream(audioFile);
                bis = new BufferedInputStream(fis);
                player = new Player(bis);
                player.play();
                if (!canceled) onCompletionCallback.forEach(Runnable::run);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            } finally {
                closeResources();
                playing = false;
                Console.INSTANCE.revalidateAudioMenuVisibility();
            }
        }, audioFile.getAbsolutePath());
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
     * Adds the callback to invoke upon a completion event.
     *
     * @param callback the callback to invoke upon a completion event
     * @return this player
     */
    @CanIgnoreReturnValue
    public CPlayer addOnCompletionCallback(Runnable callback) {
        Preconditions.checkNotNull(callback);
        onCompletionCallback.add(callback);
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
     * Returns whether the provided audio file is equal to the encapsulated audio file.
     *
     * @param audioFile the audio file
     * @return whether the provided audio file is equal to the encapsulated audio file
     */
    public boolean isUsing(File audioFile) {
        return this.audioFile.equals(audioFile);
    }

    /**
     * Returns whether the audio file encapsulated by this player is a system audio file.
     *
     * @return whether the audio file encapsulated by this player is a system audio file
     */
    public boolean isSystemAudio() {
        return GeneralAudioPlayer.isSystemAudio(audioFile);
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
        if (!GeneralAudioPlayer.isSystemAudio(file)) Logger.log(LogTag.AUDIO, file.getAbsolutePath());
    }
}
