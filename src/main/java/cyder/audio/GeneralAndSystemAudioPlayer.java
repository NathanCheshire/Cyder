package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.audio.player.AudioPlayer;
import cyder.console.Console;
import cyder.enumerations.Extension;
import cyder.exceptions.IllegalMethodException;
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
import java.util.Optional;

/**
 * Utilities related to playing general and system audio.
 */
public final class GeneralAndSystemAudioPlayer {
    /**
     * The name of the thread for playing general audio.
     */
    private static final String GENERAL_AUDIO_THREAD_NAME = "General Audio Player";

    /**
     * The thread name for the system audio player.
     */
    private static final String SYSTEM_AUDIO_PLAYER_THREAD_NAME = "System Audio Player";

    /**
     * An empty runnable.
     */
    private static final Runnable EMPTY_RUNNABLE = () -> {};

    /**
     * The list of paths of audio files to ignore when logging a play audio call.
     */
    private static final ImmutableList<String> ignoreLoggingAudioPaths = ImmutableList.of(
            StaticUtil.getStaticPath("chime.mp3"),
            StaticUtil.getStaticPath("typing.mp3")
    );

    /**
     * Player used to play general audio files that may be user terminated.
     */
    private static Player generalAudioPlayer;

    /**
     * The audio file currently playing via the {@link #generalAudioPlayer}.
     */
    private static File currentGeneralAudioFile = null;

    /**
     * Suppress default constructor.
     */
    private GeneralAndSystemAudioPlayer() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Plays the requested audio file using the general
     * JLayer player which can be terminated by the user.
     *
     * @param file the audio file to play
     */
    public static void playGeneralAudio(File file) {
        playGeneralAudioWithCompletionCallback(file, EMPTY_RUNNABLE);
    }

    /**
     * An encapsulated player class for playing singular audio files.
     */
    private static class InnerPlayer {
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
         * The runnable to invoke upon a cancel event.
         */
        private Runnable onCanceledCallback;

        /**
         * Constructs a new inner player object.
         *
         * @param audioFile the audio file this player will play
         */
        public InnerPlayer(File audioFile) {
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
            try {
                fis = new FileInputStream(audioFile);
                bis = new BufferedInputStream(fis);
                player = new Player(bis);
                player.play();
                stop();
                if (onCompletionCallback != null) onCompletionCallback.run();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                if (onCanceledCallback != null) onCanceledCallback.run();
            }
        }

        /**
         * Stops the player if playing and frees all resources allocated by this object.
         */
        public void stop() {
            try {
                if (fis != null) fis.close();
                fis = null;
                if (bis != null) bis.close();
                bis = null;
                if (player != null) player.close();
                player = null;
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }

        /**
         * Sets the callback to invoke upon a completion event.
         *
         * @param callback the callback to invoke upon a completion event
         */
        public void setOnCompletionCallback(Runnable callback) {
            onCompletionCallback = Preconditions.checkNotNull(callback);
        }

        /**
         * Sets the callback to invoke upon a cancel event.
         *
         * @param callback the callback to invoke upon a cancel event
         */
        public void setOnCancelCallback(Runnable callback) {
            onCanceledCallback = Preconditions.checkNotNull(callback);
        }
    }

    /**
     * Plays the requested audio file using the general
     * {@link Player} player which can be terminated by the user.
     *
     * @param file                 the audio file to play
     * @param onCompletionCallback the callback to invoke upon completion of playing the audio file
     */
    @SuppressWarnings("UnusedAssignment") /* Memory optimizations */
    public static void playGeneralAudioWithCompletionCallback(File file, Runnable onCompletionCallback) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(file));
        Preconditions.checkNotNull(onCompletionCallback);

        stopGeneralAudio();
        logAudio(file);
        Console.INSTANCE.showAudioButton();

        CyderThreadRunner.submit(() -> {
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);

                generalAudioPlayer = new Player(bis);
                currentGeneralAudioFile = file;

                generalAudioPlayer.play();
                if (generalAudioPlayer != null) generalAudioPlayer.close();
                currentGeneralAudioFile = null;

                FileUtil.closeIfNotNull(fis);
                FileUtil.closeIfNotNull(bis);

                fis = null;
                bis = null;
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            } finally {
                onCompletionCallback.run();
                Console.INSTANCE.revalidateAudioMenuVisibility();
            }
        }, GENERAL_AUDIO_THREAD_NAME);
    }

    /**
     * Returns whether general audio is playing.
     *
     * @return whether general audio is playing
     */
    public static boolean isGeneralAudioPlaying() {
        return generalAudioPlayer != null && !generalAudioPlayer.isComplete();
    }

    /**
     * Returns the file currently being played as general audio if present. Empty optional else.
     *
     * @return the file currently being played as general audio if present. Empty optional else
     */
    public static Optional<File> getCurrentGeneralAudioFile() {
        return Optional.ofNullable(currentGeneralAudioFile);
    }

    /**
     * Stops the current general audio if the provided file is playing.
     *
     * @param audioFile the audio file
     * @return whether any audio was stopped.
     */
    @CanIgnoreReturnValue
    public static boolean stopAudioIfFilePlaying(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(FileUtil.validateExtension(audioFile, Extension.MP3.getExtension()));

        if (audioFile.equals(currentGeneralAudioFile)) {
            stopGeneralAudio();
            return true;
        }

        return false;
    }

    /**
     * Plays the requested audio file using a new JLayer Player object
     * (this cannot be stopped util the MPEG is finished).
     *
     * @param file the audio file to play
     */
    @SuppressWarnings("UnusedAssignment") /* Memory management */
    public static void playSystemAudio(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(file));

        logAudio(file);

        CyderThreadRunner.submit(() -> {
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                Player player = new Player(bis);

                player.play();
                player.close();

                FileUtil.closeIfNotNull(fis);
                FileUtil.closeIfNotNull(bis);

                fis = null;
                bis = null;
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, SYSTEM_AUDIO_PLAYER_THREAD_NAME);
    }

    /**
     * Stops the audio currently playing. Note that this does not include
     * any system audio or AudioPlayer widget audio.
     */
    public static void stopGeneralAudio() {
        try {
            if (generalAudioPlayer != null && !generalAudioPlayer.isComplete()) {
                generalAudioPlayer.close();
                generalAudioPlayer = null;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            Console.INSTANCE.revalidateAudioMenuVisibility();
        }
    }

    /**
     * Stops any and all audio playing either through the audio player or the general player.
     */
    public static void stopAllAudio() {
        if (AudioPlayer.isAudioPlaying()) AudioPlayer.handlePlayPauseButtonClick();
        if (isGeneralAudioPlaying()) stopGeneralAudio();
    }

    /**
     * Pause audio if playing via AudioPlayer. If general audio is playing then that audio is stopped.
     */
    public static void pauseAudio() {
        if (AudioPlayer.isAudioPlaying()) AudioPlayer.handlePlayPauseButtonClick();
        if (isGeneralAudioPlaying()) stopGeneralAudio();
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