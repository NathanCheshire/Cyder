package cyder.audio;

import com.google.common.base.Preconditions;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.threads.CyderThreadRunner;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.atomic.AtomicReference;

/** Utilities related to playing general and system audio. */
public final class GeneralAndSystemAudioPlayer {
    /** The name of the thread for playing general audio. */
    private static final String GENERAL_AUDIO_THREAD_NAME = "General Audio Player";

    /** An empty runnable. */
    private static final Runnable EMPTY_RUNNABLE = () -> {};

    /** The thread name for the system audio player. */
    private static final String SYSTEM_AUDIO_PLAYER_THREAD_NAME = "System Audio Player";

    /** Player used to play general audio files that may be user terminated. */
    private static Player generalAudioPlayer;

    /** Suppress default constructor. */
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
    public static void playGeneralAudioWithCompletionCallback(File file, Runnable onCompletionCallback) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(file));
        Preconditions.checkNotNull(onCompletionCallback);

        try {
            stopGeneralAudio();
            FileInputStream fis = new FileInputStream(file);
            generalAudioPlayer = new Player(fis);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        Logger.log(LogTag.AUDIO, file.getAbsoluteFile());
        Console.INSTANCE.showAudioButton();

        CyderThreadRunner.submit(() -> {
            try {
                generalAudioPlayer.play();
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
    public static void playSystemAudio(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(file));

        AtomicReference<FileInputStream> fis = new AtomicReference<>();
        AtomicReference<BufferedInputStream> bis = new AtomicReference<>();
        AtomicReference<Player> newSystemPlayer = new AtomicReference<>();

        try {
            fis.set(new FileInputStream(file));
            bis.set(new BufferedInputStream(fis.get()));
            newSystemPlayer.set(new Player(bis.get()));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        // todo ignore typing animation sound
        Logger.log(LogTag.AUDIO, "[SYSTEM AUDIO] " + file.getAbsolutePath());

        CyderThreadRunner.submit(() -> {
            try {
                newSystemPlayer.get().play();
                newSystemPlayer.get().close();

                FileUtil.closeIfNotNull(fis.get());
                FileUtil.closeIfNotNull(bis.get());

                fis.set(null);
                bis.set(null);
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

    /** Stops any and all audio playing either through the audio player or the general player. */
    public static void stopAllAudio() {
        if (isGeneralAudioPlaying()) stopGeneralAudio();
        if (AudioPlayer.isAudioPlaying()) AudioPlayer.handlePlayPauseButtonClick();
    }

    /** Pause audio if playing via AudioPlayer. If general audio is playing then that audio is stopped. */
    public static void pauseAudio() {
        if (AudioPlayer.isAudioPlaying()) AudioPlayer.handlePlayPauseButtonClick();
        if (isGeneralAudioPlaying()) stopGeneralAudio();
    }
}