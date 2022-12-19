package main.java.cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import javazoom.jl.player.Player;
import main.java.cyder.console.Console;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.files.FileUtil;
import main.java.cyder.handlers.internal.ExceptionHandler;
import main.java.cyder.logging.LogTag;
import main.java.cyder.logging.Logger;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.strings.StringUtil;
import main.java.cyder.threads.CyderThreadRunner;
import main.java.cyder.utils.StaticUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Utilities related to playing general and system audio.
 */
public final class GeneralAndSystemAudioPlayer {
    /**
     * The name of the thread for playing general audio.
     */
    private static final String GENERAL_AUDIO_THREAD_NAME = "General Audio Player";

    /**
     * An empty runnable.
     */
    private static final Runnable EMPTY_RUNNABLE = () -> {};

    /**
     * The thread name for the system audio player.
     */
    private static final String SYSTEM_AUDIO_PLAYER_THREAD_NAME = "System Audio Player";

    /**
     * Player used to play general audio files that may be user terminated.
     */
    private static Player generalAudioPlayer;

    /**
     * The list of paths of audio files to ignore when logging a play audio call.
     */
    private static final ImmutableList<String> ignoreLoggingAudioPaths = ImmutableList.of(
            StaticUtil.getStaticPath("chime.mp3"),
            StaticUtil.getStaticPath("typing.mp3")
    );

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
     * Plays the requested audio file using the general
     * {@link Player} player which can be terminated by the user.
     *
     * @param file                 the audio file to play
     * @param onCompletionCallback the callback to invoke upon completion of playing the audio file
     */
    @SuppressWarnings("UnusedAssignment") /* Memory management */
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

                generalAudioPlayer.play();
                generalAudioPlayer.close();

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
     * Plays the requested audio file using a new JLayer Player object.
     * (this cannot be stopped util the MPEG is finished)
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