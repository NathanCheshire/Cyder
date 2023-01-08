package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.console.Console;
import cyder.constants.CyderRegexPatterns;
import cyder.enums.Dynamic;
import cyder.enums.ExitCondition;
import cyder.enums.SystemPropertyKey;
import cyder.exceptions.IllegalMethodException;
import cyder.exceptions.UnsupportedOsException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.managers.ProgramModeManager;
import cyder.meta.Cyder;
import cyder.strings.CyderStrings;
import cyder.user.UserUtil;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper methods to sort out differences between operating systems Cyder might be running on.
 */
public final class OsUtil {
    // todo this stuff should be in FileUtil or maybe even FileName util?

    /**
     * The invalid Com names which files may not contains on Windows.
     * These exist in Windows for backwards compatibility.
     */
    public static final ImmutableList<String> invalidWindowsComNames = ImmutableList.of(
            "COM1", "COM2", "COM3",
            "COM4", "COM5", "COM6",
            "COM7", "COM8", "COM9"
    );

    /**
     * The invalid LPT (Line Printer Terminal) names which files may not contains on Windows.
     * These exist in Windows for backwards compatibility.
     */
    public static final ImmutableList<String> invalidWindowsLptNames = ImmutableList.of(
            "LPT1", "LPT2", "LPT3",
            "LPT4", "LPT5", "LPT6",
            "LPT7", "LPT8", "LPT9"
    );

    /**
     * The additional invalid windows filenames aside from
     * {@link #invalidWindowsComNames} and {@link #invalidWindowsLptNames}.
     * These exist in Windows for backwards compatibility.
     */
    public static final ImmutableList<String> otherInvalidWindowsNames = ImmutableList.of(
            "CON", "PRN", "AUX", "NUL"
    );

    /**
     * A list of the restricted windows filenames due to backwards
     * compatibility and the nature of "APIs are forever".
     */
    public static final ImmutableList<String> invalidWindowsFilenames = new ImmutableList.Builder<String>()
            .addAll(invalidWindowsComNames)
            .addAll(invalidWindowsLptNames)
            .addAll(otherInvalidWindowsNames)
            .build();

    /**
     * The prefix the class resource must start with for the program to be counted as starting from a JAR file.
     */
    private static final String jarModeResourcePrefix = "jar:";

    /**
     * Whether Cyder is being run as a compiled JAR file.
     */
    public static final boolean JAR_MODE;

    static {
        URL resource = Cyder.class.getResource("Cyder.class");
        JAR_MODE = Objects.requireNonNull(resource).toString().startsWith(jarModeResourcePrefix);
        Logger.log(LogTag.DEBUG, "Jar mode set as: " + String.valueOf(JAR_MODE).toUpperCase());
        ProgramModeManager.INSTANCE.refreshProgramMode();
    }

    /**
     * Suppress default constructor.
     */
    private OsUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The list of invalid characters for a file name on unix based systems.
     */
    private static final ImmutableList<String> invalidUnixFilenameChars = ImmutableList.of(
            CyderStrings.forwardSlash, "<", ">", "|", "&", CyderStrings.colon
    );

    /**
     * Returns whether the provided filename is valid for the operating system
     * Cyder is currently running on.
     *
     * @param filename the desired filename
     * @return whether the provided filename is valid for the operating system
     * Cyder is currently running on
     */
    public static boolean isValidFilename(String filename) {
        filename = filename.trim();

        switch (OPERATING_SYSTEM) {
            case OSX:
                return !filename.contains(CyderStrings.forwardSlash) && !filename.contains(CyderStrings.nullChar);
            case WINDOWS:
                if (filename.matches(CyderRegexPatterns.windowsInvalidFilenameChars.pattern())) {
                    return false;
                }

                for (String invalidName : invalidWindowsFilenames) {
                    if (filename.equalsIgnoreCase(invalidName)) {
                        return false;
                    }
                }

                if (filename.contains(".")) {
                    String[] parts = filename.split("\\.");

                    for (String part : parts) {
                        for (String invalidName : invalidWindowsFilenames) {
                            if (part.equalsIgnoreCase(invalidName)) {
                                return false;
                            }
                        }
                    }
                }

                return !filename.endsWith(".");
            case UNIX:
                for (String invalidChar : invalidUnixFilenameChars) {
                    if (filename.contains(invalidChar)) return false;
                }

                break;
            case UNKNOWN:
                throw new UnsupportedOsException("Unknown operating system: " + OPERATING_SYSTEM_NAME);
        }

        return true;
    }

    /**
     * The top-level operating system name (the operating system the JVM is currently being ran on).
     */
    public static final String OPERATING_SYSTEM_NAME = SystemPropertyKey.OS_NAME.getProperty();

    /**
     * Invokes a controlled program exit that eventually invokes {@link System#exit(int)}
     * which will in turn invoke the shutdown hooks.
     *
     * @param exitCondition the exiting code to describe why the program exited
     */
    public static void exit(ExitCondition exitCondition) {
        Preconditions.checkNotNull(exitCondition);

        UserUtil.blockFutureIo();

        try {
            Logger.log(LogTag.PROGRAM_EXIT, exitCondition);
        } catch (Exception ignored) {}

        System.exit(exitCondition.getCode());
    }

    /**
     * The primary operating systems.
     */
    public enum OperatingSystem {
        /**
         * Macintosh OS.
         */
        OSX("mac"),

        /**
         * The Windows operating system.
         */
        WINDOWS("win"),

        /**
         * Any Unix based operating system.
         */
        UNIX(ImmutableList.of("nix", "nux", "aix")),

        /**
         * The SunOS specific Unix operating system.
         */
        SOLARIS("sunos"),

        /**
         * An indeterminable operating system.
         */
        UNKNOWN("");

        /**
         * The substrings to detect this operating system
         */
        private final ImmutableList<String> substrings;

        OperatingSystem(String substring) {
            this(ImmutableList.of(substring));
        }

        OperatingSystem(ImmutableList<String> substrings) {
            this.substrings = substrings;
        }

        /**
         * Returns the substrings to detect this operating system.
         *
         * @return the substrings to detect this operating system
         */
        public ImmutableList<String> getSubstrings() {
            return substrings;
        }

        /**
         * Returns whether this operating system is the host OS for Cyder.
         *
         * @return whether this operating system is the host OS for Cyder
         */
        public boolean isCurrentOperatingSystem() {
            for (String substring : getSubstrings()) {
                if (OPERATING_SYSTEM_NAME.toLowerCase().contains(substring)) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * The standard operating system enum.
     */
    public static final OperatingSystem OPERATING_SYSTEM;

    static {
        if (isWindows()) {
            OPERATING_SYSTEM = OperatingSystem.WINDOWS;
        } else if (OperatingSystem.OSX.isCurrentOperatingSystem()) {
            OPERATING_SYSTEM = OperatingSystem.OSX;
        } else if (OperatingSystem.UNIX.isCurrentOperatingSystem()) {
            OPERATING_SYSTEM = OperatingSystem.UNIX;
        } else {
            OPERATING_SYSTEM = OperatingSystem.UNKNOWN;
        }
    }

    /**
     * The file separator character used for this operating system.
     */
    public static final String FILE_SEP = SystemPropertyKey.FILE_SEPARATOR.getProperty();

    /**
     * The maximum number of times something should be attempted to be deleted.
     */
    public static final int MAX_FILE_DELETION_ATTEMPTS = 500;

    /**
     * The maximum number of times something should be attempted to be created.
     */
    public static final int MAX_FILE_CREATION_ATTEMPTS = 500;

    /**
     * The default user directory.
     */
    public static final String USER_DIR = SystemPropertyKey.USER_DIR.getProperty();

    /**
     * The root of the Windows file system.
     */
    public static final String WINDOWS_ROOT = "c:/";

    /**
     * Returns whether the operating system is windows.
     *
     * @return whether the operating system is windows
     */
    public static boolean isWindows() {
        return OperatingSystem.WINDOWS.isCurrentOperatingSystem();
    }

    /**
     * Returns whether the operating system is OSX.
     *
     * @return whether the operating system is OSX
     */
    public static boolean isOsx() {
        return OperatingSystem.OSX.isCurrentOperatingSystem();
    }

    /**
     * The length of the prefixes for running a system command.
     * For windows this is "cmd.exe" and "/C" and for Unix systems
     * this is "sh" and "-c".
     */
    private static final int commandPrefixLength = 2;

    /**
     * The sh string for a Unix system shell command.
     */
    private static final String SH = "sh";

    /**
     * The -c string for a Unix system shell command.
     */
    private static final String DASH_C = "-c";

    /**
     * The cmd.exe string for a Windows system shell command.
     */
    private static final String CMD_EXE = "cmd.exe";

    /**
     * The /C string for a Windows system shell command.
     */
    private static final String SLASH_C = "/C";

    /**
     * Executes the provided command using the operating system's shell.
     *
     * @param commandParts the command parts to execute using the Java {@link Runtime} API.
     * @throws IOException if an IO exception occurs
     */
    public static void executeShellCommand(List<String> commandParts) throws IOException {
        Preconditions.checkNotNull(commandParts);
        Preconditions.checkArgument(!commandParts.isEmpty());

        String[] commandPartsArr = new String[commandParts.size() + commandPrefixLength];

        switch (OPERATING_SYSTEM) {
            case OSX, UNIX, SOLARIS -> {
                commandPartsArr[0] = SH;
                commandPartsArr[1] = DASH_C;
            }
            case WINDOWS -> {
                commandPartsArr[0] = CMD_EXE;
                commandPartsArr[1] = SLASH_C;
            }
            case UNKNOWN -> throw new IllegalStateException("Unsupported operating system: " + OPERATING_SYSTEM);
        }

        int index = commandPrefixLength;
        for (String part : commandParts) {
            commandPartsArr[index] = part;
            index++;
        }

        Runtime.getRuntime().exec(commandPartsArr);
    }

    /**
     * Executes the provided command using the operating system's shell.
     *
     * @param command the command to execute using the Java {@link Runtime} API.
     * @throws IOException if an IO exception occurs
     */
    public static void executeShellCommand(String command) throws IOException {
        Preconditions.checkNotNull(command);
        Preconditions.checkArgument(!command.isEmpty());

        executeShellCommand(ImmutableList.of(command));
    }

    /**
     * The start keyword for launching the Windows command shell.
     */
    private static final String START = "start";

    /**
     * Opens the command shell for the operating system.
     */
    public static void openShell() {
        try {
            switch (OPERATING_SYSTEM) {
                case OSX, UNIX, SOLARIS -> throw new IllegalStateException("Operating system not yet supported: "
                        + OPERATING_SYSTEM);
                case WINDOWS -> executeShellCommand(START);
                case UNKNOWN -> throw new IllegalStateException("Unsupported operating system: " + OPERATING_SYSTEM);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns the name of the shell this operating system defaults to.
     *
     * @return the name of the shell this operating system defaults to
     */
    public static String getShellName() {
        return switch (OPERATING_SYSTEM) {
            case OSX -> "Terminal (Bash)";
            case WINDOWS -> "Command prompt";
            case UNIX, SOLARIS -> "Bash";
            case UNKNOWN -> "Unknown Shell";
        };
    }

    /**
     * Builds the provided strings into a filepath by inserting the OS' path separators.
     * Example: ["alpha","beta","gamma","delta.txt"] on Windows would return
     * alpha\beta\gamma\delta.txt
     *
     * @param directories the names of directories to add one after the other
     * @return the formatted path
     */
    public static String buildPath(String... directories) {
        checkNotNull(directories);
        checkArgument(directories.length > 0);

        StringBuilder ret = new StringBuilder();

        for (int i = 0 ; i < directories.length ; i++) {
            ret.append(directories[i]);

            if (i != directories.length - 1) {
                ret.append(FILE_SEP);
            }
        }

        return ret.toString();
    }

    /**
     * Builds the provided strings into a file by inserting the OS' path
     * separators between the provided path strings.
     * Example: buildFile("alpha","beta","gamma","delta.txt") on Windows would be equivalent to typing:
     * new File("alpha\beta\gamma\delta.txt")
     *
     * @param directories the names of directories to add one after the other
     * @return a reference to a file which may or may not exist
     */
    public static File buildFile(String... directories) {
        return new File(buildPath(directories));
    }

    /**
     * Returns the username of the operating system user.
     *
     * @return the username of the operating system user
     */
    public static String getOsUsername() {
        return SystemPropertyKey.USER_NAME.getProperty();
    }

    /**
     * Returns the name of the computer Cyder is currently running on.
     *
     * @return the name of the computer Cyder is currently running on
     */
    public static String getComputerName() {
        String name = CyderStrings.NOT_AVAILABLE;

        try {
            InetAddress address = InetAddress.getLocalHost();
            name = address.getHostName();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return name;
    }

    /**
     * Sets the location of the mouse on screen.
     *
     * @param x the x value to set the mouse to
     * @param y the y value to set the mouse to
     */
    public static void setMouseLocation(int x, int y) {
        try {
            Console.INSTANCE.getInputHandler().getRobot().mouseMove(x, y);
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Sets the mouse to the middle of the provided component.
     *
     * @param component the component to move the mouse to the center of
     */
    public static void setMouseLocation(Component component) {
        checkNotNull(component);

        try {
            Point topLeft = component.getLocationOnScreen();

            int x = (int) (topLeft.getX() + component.getWidth() / 2);
            int y = (int) (topLeft.getY() + component.getHeight() / 2);

            setMouseLocation(x, y);
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Deletes the provided file/folder recursively.
     *
     * @param file the folder/file to delete
     * @return whether the folder/file was successfully deleted
     */
    @CanIgnoreReturnValue
    public static boolean deleteFile(File file) {
        checkNotNull(file);

        return deleteFile(file, true);
    }

    /**
     * Deletes the provided file/folder recursively.
     *
     * @param fileOrFolder the folder/file to delete
     * @param log          whether to log the delete operation. Ideally this is
     *                     always true but some rare cases require logging to be skipped
     * @return whether the folder/file was successfully deleted
     */
    @CanIgnoreReturnValue
    public static boolean deleteFile(File fileOrFolder, boolean log) {
        checkNotNull(fileOrFolder);

        if (log) {
            Logger.log(LogTag.SYSTEM_IO, "Requested deletion of: " + fileOrFolder.getAbsolutePath());
        }

        // directory means recursive case to delete contents
        if (fileOrFolder.isDirectory()) {
            File[] files = fileOrFolder.listFiles();

            if (files != null && files.length != 0) {
                Arrays.stream(files).forEach(file -> deleteFile(file, log));
            }
        }

        // contents deleted so now can delete as if it was a file if it isn't
        int inc = 0;
        while (inc < MAX_FILE_DELETION_ATTEMPTS) {
            if (fileOrFolder.delete()) {
                return true;
            }

            inc++;
        }

        if (fileOrFolder.exists() && log) {
            Logger.log(LogTag.SYSTEM_IO, DELETION_FAILED_TAG
                    + " file: " + fileOrFolder.getAbsolutePath());
        }

        return false;
    }

    /**
     * The deletion failed tag.
     */
    private static final String DELETION_FAILED_TAG = "[DELETION FAILED]";

    /**
     * Creates the provided file/folder if possible.
     *
     * @param file   the file/folder to attempt to create
     * @param isFile whether to treat the file as a directory or as a file
     * @return whether the file/folder could be created
     */
    public static boolean createFile(File file, boolean isFile) {
        checkNotNull(file);

        try {
            int attempts = 0;
            while (attempts < MAX_FILE_CREATION_ATTEMPTS) {
                boolean created;

                if (isFile) {
                    created = file.createNewFile();
                } else {
                    created = file.mkdirs();
                }

                if (file.exists() && created) {
                    return true;
                }

                attempts++;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return false;
    }

    /**
     * The return record from {@link #getNetworkDevices()}.
     */
    public record NetworkDevice(String displayName, String name) {}

    /**
     * Returns a list of all network devices connected to the host.
     *
     * @return a list of all network devices connected to the host
     */
    public static ImmutableList<NetworkDevice> getNetworkDevices() {
        ArrayList<NetworkDevice> ret = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();

            Collections.list(networks).forEach(networkInterface
                    -> ret.add(new NetworkDevice(networkInterface.getDisplayName(), networkInterface.getName())));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Sets the operating system's clipboard to the provided String.
     *
     * @param clipboardContents the String to set the operating system's clipboard to
     */
    public static void setClipboard(String clipboardContents) {
        checkNotNull(clipboardContents);

        StringSelection selection = new StringSelection(clipboardContents);
        java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    /**
     * Returns whether the provided binary could be found by invoking
     * the base command in the native shell.
     *
     * @param invokeCommand the invoke command such as ffmpeg for ffmpeg
     * @return whether the binary could be located
     */
    public static boolean isBinaryInstalled(String invokeCommand) {
        checkNotNull(invokeCommand);
        checkArgument(!invokeCommand.isEmpty());

        try {
            Runtime.getRuntime().exec(invokeCommand);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Returns whether the provided exe exist in the dynamic/exes directory.
     *
     * @param filename the filename of the exe such as ffmpeg.exe
     * @return whether the file could be located
     */
    public static boolean isBinaryInExes(String filename) {
        checkNotNull(filename);
        checkArgument(!filename.isEmpty());

        File exes = Dynamic.buildDynamic(Dynamic.EXES.getFileName());

        if (exes.exists()) {
            File[] exeFiles = exes.listFiles();

            if (exeFiles != null && exeFiles.length > 0) {
                for (File exe : exeFiles) {
                    if (exe.getName().equalsIgnoreCase(filename)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * The decimal formatter used to format file byte numbers.
     */
    private static final DecimalFormat BYTE_FORMATTER = new DecimalFormat("##.###");

    /**
     * The amount necessary to turn said many lower units into the next unit up.
     */
    public static final float coalesceSpace = 1024.0f;

    /**
     * The prefix for a terabyte.
     */
    public static final String TERABYTE_PREFIX = "TB";

    /**
     * The prefix for a gigabyte.
     */
    public static final String GIGABYTE_PREFIX = "GB";

    /**
     * The prefix for a megabyte.
     */
    public static final String MEGABYTE_PREFIX = "MB";

    /**
     * The prefix for a kilobyte.
     */
    public static final String KILOBYTE_PREFIX = "KB";

    /**
     * The bytes word.
     */
    public static final String BYTES = "bytes";

    /**
     * Formats the provided number of bytes in a human-readable format.
     * Example: passing 1024MB would return 1GB.
     *
     * @param bytes the raw number of bytes to coalesce
     * @return a formatted string detailing the number of bytes provided
     */
    public static String formatBytes(float bytes) {
        boolean negative = bytes < 0.0f;
        if (negative) bytes *= -1.0f;
        String sign = negative ? CyderStrings.dash : "";

        if (bytes >= coalesceSpace) {
            float kilo = bytes / coalesceSpace;

            if (kilo >= coalesceSpace) {
                float mega = kilo / coalesceSpace;

                if (mega >= coalesceSpace) {
                    float giga = mega / coalesceSpace;

                    if (giga >= coalesceSpace) {
                        float tera = giga / coalesceSpace;
                        return sign + (BYTE_FORMATTER.format(tera) + TERABYTE_PREFIX);
                    } else
                        return sign + (BYTE_FORMATTER.format(giga) + GIGABYTE_PREFIX);
                } else
                    return sign + (BYTE_FORMATTER.format(mega) + MEGABYTE_PREFIX);
            } else
                return sign + (BYTE_FORMATTER.format(kilo) + KILOBYTE_PREFIX);
        } else {
            return sign + bytes + CyderStrings.space + BYTES;
        }
    }
}
