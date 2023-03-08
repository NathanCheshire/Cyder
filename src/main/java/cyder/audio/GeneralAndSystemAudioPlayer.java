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

/**
 * Utilities related to playing general and system audio.
 */
public final class GeneralAndSystemAudioPlayer {
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
    private static CPlayer generalAudioPlayer;

    /**
     * Suppress default constructor.
     */
    private GeneralAndSystemAudioPlayer() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * An encapsulated JLayer {@link Player} for playing singular audio files and stopping.
     */
    private static class CPlayer {
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
            String threadName = FileUtil.getFilename(audioFile) + " Audio Player";
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
    }

    /**
     * Plays the requested audio file using the general
     * JLayer player which can be terminated by the user.
     *
     * @param file the audio file to play
     */
    public static void playGeneralAudio(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isFile());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(file));

        stopGeneralAudio();
        logAudio(file);
        Console.INSTANCE.showAudioButton();

        generalAudioPlayer = new CPlayer(file);
        generalAudioPlayer.play();
    }

    /**
     * Plays the requested audio file using the general
     * {@link Player} player which can be terminated by the user.
     *
     * @param file                 the audio file to play
     * @param onCompletionCallback the callback to invoke upon completion of playing the audio file
     */
    public static void playGeneralAudioWithCompletionCallback(File file, Runnable onCompletionCallback) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(file));
        Preconditions.checkNotNull(onCompletionCallback);

        stopGeneralAudio();
        logAudio(file);
        Console.INSTANCE.showAudioButton();

        generalAudioPlayer = new CPlayer(file).setOnCompletionCallback(onCompletionCallback);
        generalAudioPlayer.play();
    }

    /**
     * Returns whether general audio is playing.
     *
     * @return whether general audio is playing
     */
    public static boolean isGeneralAudioPlaying() {
        return generalAudioPlayer != null && generalAudioPlayer.isPlaying();
    }

    public static boolean generalOrAudioPlayerAudioPlaying() {
        return GeneralAndSystemAudioPlayer.isGeneralAudioPlaying() || AudioPlayer.isAudioPlaying();
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

        if (generalAudioPlayer != null && audioFile.equals(generalAudioPlayer.getAudioFile())) {
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
    public static void playSystemAudio(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(file));

        logAudio(file);
        new CPlayer(file).play();
    }

    /**
     * Stops the audio currently playing. Note that this does not include
     * any system audio or AudioPlayer widget audio.
     */
    public static void stopGeneralAudio() {
        if (generalAudioPlayer != null) generalAudioPlayer.stop();
        Console.INSTANCE.revalidateAudioMenuVisibility();
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