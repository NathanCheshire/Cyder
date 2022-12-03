package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.audio.AudioPlayer;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.enums.Extension;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.handlers.external.DirectoryViewer;
import cyder.handlers.external.PhotoViewer;
import cyder.handlers.external.TextViewer;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.network.NetworkUtil;
import cyder.props.Props;
import cyder.threads.CyderThreadRunner;
import javazoom.jl.player.Player;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/** Utilities related to local computer IO. */
public final class IoUtil {
    /** Suppress default constructor. */
    private IoUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /** Player used to play general audio files that may be user terminated. */
    private static Player player;

    // todo if this is a url account for that

    /**
     * Opens the provided file outside of the program regardless of whether a
     * handler exists for the file ({@link TextViewer}, {@link AudioPlayer}, etc.).
     *
     * @param file the file to open
     */
    public static void openFileUsingNativeProgram(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        Desktop desktop = Desktop.getDesktop();

        try {
            URI uri = file.toURI();
            desktop.browse(uri);
        } catch (Exception e) {
            try {
                String path = file.getAbsolutePath();

                if (OsUtil.OPERATING_SYSTEM == OsUtil.OperatingSystem.WINDOWS) {
                    Runtime.getRuntime().exec("explorer.exe /select," + path);
                } else {
                    throw new FatalException("Could not open file; tell Nathan to fix me");
                }

                Logger.log(LogTag.LINK, path);
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }
    }

    /**
     * Opens the provided file outside of the program regardless of whether a
     * handler exists for the file (e.g.: TextHandler, AudioPlayer, etc.).
     *
     * @param filePath the path to the file to open
     */
    public static void openFileUsingNativeProgram(String filePath) {
        Preconditions.checkNotNull(filePath);
        Preconditions.checkArgument(!filePath.isEmpty());

        openFileUsingNativeProgram(new File(filePath));
    }

    // todo files package

    public enum CyderFileHandler {
        TEXT(file -> {
            return FileUtil.getExtension(file).equals(Extension.TXT.getExtension());
        }, file -> {
            TextViewer.getInstance(file).showGui();
        }),
        AUDIO(FileUtil::isSupportedAudioExtension, AudioPlayer::showGui),
        IMAGE(FileUtil::isSupportedImageExtension, file -> PhotoViewer.getInstance(file).showGui()),
        DIRECTORY(File::isDirectory, DirectoryViewer::showGui);

        /**
         * The function to determine whether a Cyder file handler exists for the provided file type.
         */
        private final Function<File, Boolean> cyderHandlerExists;

        /**
         * The consumer to open the provided file using this Cyder file handler if the file is valid and exists.
         */
        private final Consumer<File> openFileUsingCyderHandler;

        CyderFileHandler(Function<File, Boolean> cyderHandlerExists, Consumer<File> openFileUsingCyderHandler) {
            this.cyderHandlerExists = cyderHandlerExists;
            this.openFileUsingCyderHandler = openFileUsingCyderHandler;
        }

        /**
         * Returns whether this handler should be used for the provided file.
         *
         * @param file the file this handler might be used to open if valid
         * @return whether this handler should be used for the provided file
         */
        public boolean shouldUseForFile(File file) {
            Preconditions.checkNotNull(file);

            return cyderHandlerExists.apply(file);
        }

        /**
         * Opens the provided file using this Cyder handler.
         *
         * @param file the file to open
         */
        public void open(File file) {
            Preconditions.checkNotNull(file);
            Preconditions.checkArgument(file.exists());
            Preconditions.checkState(cyderHandlerExists.apply(file));

            openFileUsingCyderHandler.accept(file);
        }
    }

    public void openFileOrLink(String fileOrLink, boolean useCyderHandlerIfPossible) {
        File referenceFile = new File(fileOrLink);
        boolean referenceFileExists = referenceFile.exists();

        if (referenceFileExists && useCyderHandlerIfPossible) {
            for (CyderFileHandler handler : CyderFileHandler.values()) {
                if (handler.shouldUseForFile(referenceFile)) {
                    handler.open(referenceFile);
                    return;
                }
            }
        }

        // todo validate link
    }

    /** The thread name for the jvm args logger. */
    private static final String JVM_ARGS_LOGGER_THREAD_NAME = "JVM Args Logger";

    /**
     * Logs any possible command line arguments passed in to Cyder upon starting.
     * Appends JVM Command Line Arguments along with the start location to the log.
     *
     * @param cyderArgs the command line arguments passed in
     */
    public static void logArgs(ImmutableList<String> cyderArgs) {
        Preconditions.checkNotNull(cyderArgs);

        CyderThreadRunner.submit(() -> {
            try {
                // build string of all JVM args
                StringBuilder argBuilder = new StringBuilder();

                for (int i = 0 ; i < cyderArgs.size() ; i++) {
                    if (i != 0) {
                        argBuilder.append(",");
                    }

                    argBuilder.append(cyderArgs.get(i));
                }

                NetworkUtil.IspQueryResult result = NetworkUtil.getIspAndNetworkDetails();

                argBuilder.append("city = ").append(result.city())
                        .append(", state = ").append(result.state())
                        .append(", country = ").append(result.country())
                        .append(", ip = ").append(result.ip())
                        .append(", isp = ").append(result.isp())
                        .append(", hostname = ").append(result.hostname());

                boolean autoCypher = Props.autocypher.getValue();
                Logger.log(LogTag.JVM_ARGS, autoCypher ? "JVM args obfuscated due to AutoCypher" : argBuilder);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, JVM_ARGS_LOGGER_THREAD_NAME);
    }

    /**
     * Opens the provided file, possibly inside of the program if a handler exists for it.
     *
     * @param file the file to open
     */
    public static void openFileUsingCyderHandlerIfPossible(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        String extension = FileUtil.getExtension(file);

        if () {
            TextViewer.getInstance(file).showGui();
        } else if (FileUtil.isSupportedImageExtension(file)) {
            PhotoViewer.getInstance(file).showGui();
        } else if (FileUtil.isSupportedAudioExtension(file)) {
            AudioPlayer.showGui(file);
        } else {
            openFileUsingNativeProgram(file);
        }
    }

    /**
     * Opens the provided file, possibly inside of the program if a handler exists for it.
     *
     * @param filePath the path to the file to open
     */
    public static void openFileUsingCyderHandlerIfPossible(String filePath) {
        Preconditions.checkNotNull(filePath);
        Preconditions.checkArgument(!filePath.isEmpty());

        openFileUsingCyderHandlerIfPossible(new File(filePath));
    }

    /** The name of the thread for playing general audio. */
    private static final String IO_UTIL_GENERAL_AUDIO_THREAD_NAME = "IoUtil General Audio";

    /**
     * Plays the requested audio file using the general IoUtil
     * JLayer player which can be terminated by the user.
     *
     * @param file the audio file to play
     */
    public static void playGeneralAudio(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        try {
            stopGeneralAudio();
            FileInputStream FileInputStream = new FileInputStream(file);
            player = new Player(FileInputStream);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        Logger.log(LogTag.AUDIO, file.getAbsoluteFile());
        Console.INSTANCE.showAudioButton();

        CyderThreadRunner.submit(() -> {
            try {
                player.play();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            } finally {
                Console.INSTANCE.revalidateAudioMenuVisibility();
            }
        }, IO_UTIL_GENERAL_AUDIO_THREAD_NAME);
    }

    /**
     * Plays the requested audio file using the general IoUtil
     * JLayer player which can be terminated by the user.
     *
     * @param file                 the audio file to play
     * @param onCompletionCallback the callback to invoke upon completion of playing the audio file
     */
    public static void playGeneralAudioWithCompletionCallback(File file, Runnable onCompletionCallback) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkNotNull(onCompletionCallback);

        try {
            stopGeneralAudio();
            FileInputStream FileInputStream = new FileInputStream(file);
            player = new Player(FileInputStream);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        Logger.log(LogTag.AUDIO, file.getAbsoluteFile());
        Console.INSTANCE.showAudioButton();

        CyderThreadRunner.submit(() -> {
            try {
                player.play();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            } finally {
                onCompletionCallback.run();
                Console.INSTANCE.revalidateAudioMenuVisibility();
            }
        }, IO_UTIL_GENERAL_AUDIO_THREAD_NAME);
    }

    /**
     * Returns whether general audio is playing.
     *
     * @return whether general audio is playing
     */
    public static boolean isGeneralAudioPlaying() {
        return player != null && !player.isComplete();
    }

    /**
     * Plays the requested audio file using a new JLayer Player object
     * (this cannot be stopped util the mpeg is finished).
     *
     * @param filePath the path to the audio file to play
     */
    public static void playSystemAudio(String filePath) {
        Preconditions.checkNotNull(filePath);
        Preconditions.checkArgument(!filePath.isEmpty());

        playSystemAudio(filePath, true);
    }

    /** The thread name for the system audio player. */
    private static final String SYSTEM_AUDIO_PLAYER_THREAD_NAME = "System Audio Player";

    /**
     * Plays the requested audio file using a new JLayer Player object.
     * (this cannot be stopped util the mpeg is finished)
     *
     * @param filePath the path to the audio file to play
     * @param log      whether to log the system audio play request
     */
    public static void playSystemAudio(String filePath, boolean log) {
        Preconditions.checkNotNull(filePath);
        Preconditions.checkArgument(!filePath.isEmpty());

        AtomicReference<FileInputStream> fis = new AtomicReference<>();
        AtomicReference<BufferedInputStream> bis = new AtomicReference<>();
        AtomicReference<Player> newSystemPlayer = new AtomicReference<>();

        try {
            fis.set(new FileInputStream(filePath));
            bis.set(new BufferedInputStream(fis.get()));
            newSystemPlayer.set(new Player(bis.get()));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        if (log) {
            Logger.log(LogTag.AUDIO, "[SYSTEM AUDIO] " + filePath);
        }

        CyderThreadRunner.submit(() -> {
            try {
                newSystemPlayer.get().play();
                newSystemPlayer.get().close();
                FileUtil.closeIfNotNull(fis.get());
                FileUtil.closeIfNotNull(bis.get());
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
            if (player != null && !player.isComplete()) {
                player.close();
                player = null;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            Console.INSTANCE.revalidateAudioMenuVisibility();
        }
    }

    /** Stops any and all audio playing either through the audio player or the general player. */
    public static void stopAllAudio() {
        if (isGeneralAudioPlaying()) {
            stopGeneralAudio();
        }

        if (AudioPlayer.isAudioPlaying()) {
            AudioPlayer.handlePlayPauseButtonClick();
        }
    }

    /** Pause audio if playing via AudioPlayer. If general audio is playing then that audio is stopped. */
    public static void pauseAudio() {
        if (AudioPlayer.isAudioPlaying()) {
            AudioPlayer.handlePlayPauseButtonClick();
        }

        if (isGeneralAudioPlaying()) {
            stopGeneralAudio();
        }
    }
}