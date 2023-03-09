package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.audio.player.AudioPlayer;
import cyder.console.Console;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.utils.StaticUtil;
import javazoom.jl.player.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Utilities related to playing general and system audio.
 */
public final class GeneralAudioPlayer {
    // todo allow clients to register
    /**
     * The list of audio files to ignore when logging.
     */
    private static final ArrayList<File> systemAudioFiles = new ArrayList<>();

    /**
     * The player used to play general audio that may be user terminated.
     */
    private static CPlayer generalPlayer;

    /**
     * The list of system players which are currently playing audio.
     */
    private static final LinkedList<CPlayer> systemPlayers = new LinkedList<>();

    /**
     * Suppress default constructor.
     */
    private GeneralAudioPlayer() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Plays the provided audio file. The general audio player is used if this is not a system sound.
     * Otherwise, a new {@link CPlayer} instance is used to start the requested system sound.
     *
     * @param audioFile the audio file to play
     */
    public static void playAudio(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(audioFile));

        if (isSystemAudio(audioFile)) {
            CPlayer systemPlayer = new CPlayer(audioFile);
            systemPlayer.setOnCompletionCallback(() -> systemPlayers.remove(systemPlayer));
            systemPlayers.add(systemPlayer);
            systemPlayer.play();
        } else {
            stopGeneralAudio();
            Console.INSTANCE.showAudioButton();

            generalPlayer = new CPlayer(audioFile);
            generalPlayer.play();
        }
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
        Console.INSTANCE.showAudioButton();

        generalPlayer = new CPlayer(file).setOnCompletionCallback(onCompletionCallback);
        generalPlayer.play();
    }

    /**
     * Returns whether general audio is playing.
     *
     * @return whether general audio is playing
     */
    public static boolean isGeneralAudioPlaying() {
        return generalPlayer != null && generalPlayer.isPlaying();
    }

    /**
     * Returns whether general audio or {@link AudioPlayer} audio is currently playing.
     *
     * @return whether general audio or {@link AudioPlayer} audio is currently playing
     */
    public static boolean generalOrAudioPlayerAudioPlaying() {
        return isGeneralAudioPlaying() || AudioPlayer.isAudioPlaying();
    }

    /**
     * Stops the current general audio if the provided file is playing.
     *
     * @param audioFile the audio file
     * @return whether any audio was stopped.
     */
    @CanIgnoreReturnValue
    public static boolean stopAudio(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(audioFile));

        if (generalPlayer != null && audioFile.equals(generalPlayer.getAudioFile())) {
            stopGeneralAudio();
            return true;
        }

        return false;
    }

    /**
     * Stops the audio currently playing. Note that this does not include
     * any system audio or AudioPlayer widget audio.
     */
    public static void stopGeneralAudio() {
        if (generalPlayer != null) generalPlayer.stop();
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
     * Returns a list of the current active system audio players.
     *
     * @return a list of the current active system audio players
     */
    public static ImmutableList<CPlayer> getSystemPlayers() {
        return ImmutableList.copyOf(systemPlayers);
    }

    /**
     * Adds the provided audio file to the system audio files list.
     *
     * @param audioFile the audio file
     */
    public static void registerSystemAudio(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(FileUtil.isSupportedAudioExtension(audioFile));

        systemAudioFiles.add(audioFile);
    }

    /**
     * Registers the default system audio files.
     */
    public static void registerDefaultSystemAudioFiles() {
        try {
            File systemAudio = StaticUtil.getStaticResource("system_audio.txt");
            ImmutableList<String> contents = FileUtil.getFileLines(systemAudio);
            for (String fileName : contents) {
                if (StringUtil.isNullOrEmpty(fileName)) continue;
                File systemAudioFile = StaticUtil.getStaticResource(fileName);
                Logger.log(LogTag.DEBUG, "Registering system audio file: "
                        + systemAudioFile.getAbsolutePath());
                registerSystemAudio(systemAudioFile);
            }
        } catch (Exception e) {
            throw new FatalException("Failed to register default system audio files: " + e.getMessage());
        }
    }

    /**
     * Returns whether the provided audio file is a system audio file.
     *
     * @param audioFile the audio file
     * @return whether the provided audio file is a system audio file
     */
    public static boolean isSystemAudio(File audioFile) {
        Preconditions.checkNotNull(audioFile);

        return systemAudioFiles.contains(audioFile);
    }
}