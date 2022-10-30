package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.audio.AudioPlayer;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.enums.Extension;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.file.FileUtil;
import cyder.handlers.external.PhotoViewer;
import cyder.handlers.external.TextViewer;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.network.NetworkUtil;
import cyder.props.PropLoader;
import cyder.threads.CyderThreadRunner;
import javazoom.jl.player.Player;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributes;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utilities related to local computer IO.
 */
public final class IoUtil {
    /**
     * Suppress default constructor.
     */
    private IoUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Player used to play general audio files that may be user terminated.
     */
    private static Player player;

    /**
     * Opens the provided file outside of the program regardless of whether a
     * handler exists for the file (e.g.: TextHandler, AudioPlayer, etc.).
     *
     * @param file the file to open
     */
    public static void openFileOutsideProgram(File file) {
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
    public static void openFileOutsideProgram(String filePath) {
        Preconditions.checkNotNull(filePath);
        Preconditions.checkArgument(!filePath.isEmpty());

        openFileOutsideProgram(new File(filePath));
    }

    /**
     * Determines whether the provided string is a link or a file/directory path and then opens it.
     *
     * @param fileOrLink the link/file to open
     */
    public static void openOutsideProgram(String fileOrLink) {
        Preconditions.checkNotNull(fileOrLink);
        Preconditions.checkArgument(!fileOrLink.isEmpty());

        boolean validLink;

        try {
            URL url = new URL(fileOrLink);
            URLConnection connection = url.openConnection();
            connection.connect();
            validLink = true;
        } catch (Exception ex) {
            validLink = false;
        }

        if (validLink) {
            NetworkUtil.openUrl(fileOrLink);
        } else {
            openFileOutsideProgram(fileOrLink);
        }
    }

    /**
     * The key for obtaining the AutoCypher prop from the props file.
     */
    private static final String AUTOCYPHER = "autocypher";

    /**
     * The thread name for the jvm args logger.
     */
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

                boolean autoCypher = PropLoader.getBoolean(AUTOCYPHER);
                Logger.log(LogTag.JVM_ARGS, autoCypher ? "JVM args hidden due to AutoCypher" : argBuilder);
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
    public static void openFile(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        String extension = FileUtil.getExtension(file);

        if (extension.equals(Extension.TXT.getExtension())) {
            TextViewer.getInstance(file).showGui();
        } else if (FileUtil.isSupportedImageExtension(file)) {
            PhotoViewer.getInstance(file).showGui();
        } else if (FileUtil.isSupportedAudioExtension(file)) {
            AudioPlayer.showGui(file);
        } else {
            openFileOutsideProgram(file);
        }
    }

    /**
     * Opens the provided file, possibly inside of the program if a handler exists for it.
     *
     * @param filePath the path to the file to open
     */
    public static void openFile(String filePath) {
        Preconditions.checkNotNull(filePath);
        Preconditions.checkArgument(!filePath.isEmpty());

        openFile(new File(filePath));
    }

    /**
     * The name of the thread for playing general audio.
     */
    private static final String IO_UTIL_GENERAL_AUDIO_THREAD_NAME = "IOUtil General Audio";

    /**
     * Plays the requested audio file using the general IOUtil JLayer player which can be terminated by the user.
     *
     * @param filePath the path to the audio file to play
     */
    public static void playGeneralAudio(String filePath) {
        Preconditions.checkNotNull(filePath);
        Preconditions.checkArgument(!filePath.isEmpty());

        playGeneralAudio(new File(filePath));
    }

    /**
     * Plays the requested audio file using the general IOUtil JLayer player which can be terminated by the user.
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
     * Returns whether general audio is playing.
     *
     * @return whether general audio is playing
     */
    public static boolean isGeneralAudioPlaying() {
        return player != null && !player.isComplete();
    }

    /**
     * Plays the requested audio file using a new JLayer Player object.
     * (this cannot be stopped util the mpeg is finished)
     *
     * @param filePath the path to the audio file to play
     */
    public static void playSystemAudio(String filePath) {
        Preconditions.checkNotNull(filePath);
        Preconditions.checkArgument(!filePath.isEmpty());

        playSystemAudio(filePath, true);
    }

    /**
     * The thread name for the system audio player.
     */
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

    /**
     * Stops any and all audio playing either through the audio player or the general player.
     */
    public static void stopAllAudio() {
        if (isGeneralAudioPlaying()) {
            stopGeneralAudio();
        }

        if (AudioPlayer.isAudioPlaying()) {
            AudioPlayer.handlePlayPauseButtonClick();
        }
    }

    /**
     * Pause audio if playing via AudioPlayer. If general audio is playing then that audio is stopped.
     */
    public static void pauseAudio() {
        if (AudioPlayer.isAudioPlaying()) {
            AudioPlayer.handlePlayPauseButtonClick();
        }

        if (isGeneralAudioPlaying()) {
            stopGeneralAudio();
        }
    }

    // todo make own class for this and probably close to needing a file package

    /**
     * A legacy DOS attribute of a file.
     */
    private record DosAttribute(String name, String value) {}

    /**
     * The is archive dos attribute.
     */
    private static final String IS_ARCHIVE = "isArchive";

    /**
     * The is hidden dos attribute.
     */
    private static final String IS_HIDDEN = "isHidden";

    /**
     * The is read only dos attribute.
     */
    private static final String IS_READ_ONLY = "isReadOnly";

    /**
     * The is system dos attribute.
     */
    private static final String IS_SYSTEM = "isSystem";

    /**
     * The is creation time dos attribute.
     */
    private static final String CREATION_TIME = "creationTime";

    /**
     * The is directory dos attribute.
     */
    private static final String IS_DIRECTORY = "isDirectory";

    /**
     * The is hidden dos attribute.
     */
    private static final String IS_OTHER = "isOther";

    /**
     * The is symbolic link dos attribute.
     */
    private static final String IS_SYMBOLIC_LINK = "isSymbolicLink";

    /**
     * The last access time dos attribute.
     */
    private static final String LAST_ACCESS_TIME = "lastAccessTime";

    /**
     * The last modified time dos attribute.
     */
    private static final String LAST_MODIFIED_TIME = "lastModifiedTime";

    /**
     * Gets DOS attributes of the provided file.
     *
     * @param file the file to obtain the attributes of
     * @return the DOS attributes of the file
     */
    public static ImmutableList<DosAttribute> getDosAttributes(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        try {
            DosFileAttributes attr = Files.readAttributes(Paths.get(file.getPath()), DosFileAttributes.class);
            return ImmutableList.of(
                    new DosAttribute(IS_ARCHIVE, String.valueOf(attr.isArchive())),
                    new DosAttribute(IS_HIDDEN, String.valueOf(attr.isHidden())),
                    new DosAttribute(IS_READ_ONLY, String.valueOf(attr.isReadOnly())),
                    new DosAttribute(IS_SYSTEM, String.valueOf(attr.isSystem())),
                    new DosAttribute(CREATION_TIME, String.valueOf(attr.creationTime())),
                    new DosAttribute(IS_DIRECTORY, String.valueOf(attr.isDirectory())),
                    new DosAttribute(IS_OTHER, String.valueOf(attr.isOther())),
                    new DosAttribute(IS_SYMBOLIC_LINK, String.valueOf(attr.isSymbolicLink())),
                    new DosAttribute(LAST_ACCESS_TIME, String.valueOf(attr.lastAccessTime())),
                    new DosAttribute(LAST_MODIFIED_TIME, String.valueOf(attr.lastModifiedTime())));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ImmutableList.of();
    }

    /**
     * Returns the size of the provided file in bytes.
     *
     * @param file the file to calculate the size of
     * @return the size in bytes of the file
     */
    public static long getFileSize(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        long ret = 0;
        try {
            ret = Files.readAttributes(Paths.get(file.getPath()), DosFileAttributes.class).size();
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * Returns a binary string for the provided binary file.
     *
     * @param file the binary file of pure binary contents
     * @return the String of binary data from the file
     */
    public static String getBinaryString(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.getExtension(file).equalsIgnoreCase(Extension.BIN.getExtension()));

        try {
            BufferedReader fis = new BufferedReader(new FileReader(file));
            String stringBytes = fis.readLine();
            fis.close();
            return stringBytes;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new IllegalCallerException("Could not read binary file");
    }

    /**
     * Returns a hex string for the provided binary file.
     *
     * @param file the binary file of pure binary contents
     * @return the String of hex data from the file
     */
    public static String getHexString(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.getExtension(file).equalsIgnoreCase(Extension.BIN.getExtension()));

        try {
            BufferedReader fis = new BufferedReader(new FileReader(file));
            String[] stringBytes = fis.readLine().split("(?<=\\G........)");
            StringBuilder sb = new StringBuilder();

            for (String stringByte : stringBytes) {
                sb.append(Integer.toString(Integer.parseInt(stringByte, 2), 16));
            }

            fis.close();
            return sb.toString();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new IllegalCallerException("Could not read binary file");
    }
}