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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utilities related to playing general and system audio.
 */
public final class GeneralAudioPlayer {
    /**
     * The list of audio files to ignore when logging.
     */
    private static final ArrayList<File> systemAudioFiles = new ArrayList<>();

    /**
     * Whether the default system audio has been registered.
     */
    private static final AtomicBoolean defaultSystemAudioRegistered = new AtomicBoolean();

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

        playAudio(new CPlayer(audioFile));
    }

    /**
     * Plays the provided player.
     *
     * @param player the player to play
     */
    public static void playAudio(CPlayer player) {
        Preconditions.checkNotNull(player);

        if (player.isSystemAudio()) {
            systemPlayers.add(player);
            player.addOnCompletionCallback(() -> systemPlayers.remove(player));
            player.play();
        } else {
            stopGeneralAudio();
            Console.INSTANCE.showAudioButton();

            generalPlayer = player;
            generalPlayer.play();
        }
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

        if (generalPlayer != null && generalPlayer.isUsing(audioFile)) {
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
    public static synchronized void registerDefaultSystemAudioFiles() {
        Preconditions.checkState(!defaultSystemAudioRegistered.get());
        defaultSystemAudioRegistered.set(true);

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