package cyder.utilities;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.enums.DynamicDirectory;
import cyder.enums.ExitCondition;
import cyder.enums.LoggerTag;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.Cyder;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InputHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper methods to sort out differences between operating systems Cyder might be running on.
 */
public class OSUtil {
    /**
     * A list of the restricted windows filenames due to backwards compatibility
     * and the nature of "APIs are forever".
     */
    public static final ArrayList<String> invalidWindowsFilenames = new ArrayList<>(){{
        add("CON");
        add("PRN");
        add("AUX");
        add("NUL");
        add("COM1");
        add("COM2");
        add("COM3");
        add("COM4");
        add("COM5");
        add("COM6");
        add("COM7");
        add("COM8");
        add("COM9");
        add("LPT1");
        add("LPT2");
        add("LPT3");
        add("LPT4");
        add("LPT5");
        add("LPT6");
        add("LPT7");
        add("LPT8");
        add("LPT9");
    }};

    /**
     * Whether Cyder is being run as a compiled JAR file.
     */
    public static final boolean JAR_MODE = Objects.requireNonNull(
            Cyder.class.getResource("Cyder.class")).toString().startsWith("jar:");

    /**
     * Prevent illegal class instantiation.
     */
    public OSUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

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
                return filename.contains("/") || filename.contains("\0");
            case WINDOWS:
                //invalid chars for Windows in a filename
                if (filename.matches(CyderRegexPatterns.windowsInvalidFilenameChars))
                    return false;

                //invalid filenames for windows, reserved names for backwards compatibility reasons
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
                //root dir
                if (filename.contains("/") || filename.contains(">") || filename.contains("<")
                        || filename.contains("|") || filename.contains(":") || filename.contains("&"))
                    return false;

                break;
            case UNKNOWN:
                throw new IllegalStateException("Unknown operating system: " + OPERATING_SYSTEM_NAME);
        }

        return false;
    }

    /**
     * The raw operating system name.
     */
    public static final String OPERATING_SYSTEM_NAME = System.getProperty("os.name");

    /**
     * Controlled program exit that calls System.exit which will also invoke the shutdown hook.
     *
     * @param exitCondition the exiting code to describe why the program exited (0 is standard
     *             but for this program, the key/value pairs in {@link ExitCondition} are followed)
     */
    public static void exit(ExitCondition exitCondition) {
        try {
            //ensures IO finishes and is not invoked again
            UserUtil.blockFutureIO();

            //log exit
            Logger.log(LoggerTag.EXIT, exitCondition);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        System.exit(exitCondition.getCode());
    }

    /**
     * The three primary operating systems.
     */
    public enum OperatingSystem {
        OSX, WINDOWS, UNIX, UNKNOWN
    }

    /**
     * The standard operating system enum.
     */
    public static final OperatingSystem OPERATING_SYSTEM = initializeOperatingSystem();

    /**
     * The file separator character used for this operating system.
     */
    public static final String FILE_SEP = System.getProperty("file.separator");

    /**
     * The maximum number of times something should be attepted to be deleted.
     */
    public static final int MAX_DELETION_ATTEMPTS = 500;

    /**
     * The maximum number of times something should be attempted to be created.
     */
    public static final int MAX_CREATION_ATTEMPTS = 500;

    /**
     * The default user directory.
     */
    public static final String USER_DIR = System.getProperty("user.dir");

    /**
     * The root of the Windows file system.
     */
    public static final String C_COLON_SLASH = "c:/";

    /**
     * Initializes the operating system enum type.
     *
     * @return the operating system Cyder was started, compiled, and ran on.
     */
    private static OperatingSystem initializeOperatingSystem() {
        if (OPERATING_SYSTEM != null)
            throw new IllegalStateException("Operating System already set");

        if (isWindows()) {
            return OperatingSystem.WINDOWS;
        } else if (isOSX()) {
            return OperatingSystem.OSX;
        } else if (isUnix()) {
            return OperatingSystem.UNIX;
        } else return OperatingSystem.UNKNOWN;
    }

    /**
     * Returns whether the operating system is windows.
     *
     * @return whether the operating system is windows
     */
    public static boolean isWindows() {
        return OPERATING_SYSTEM_NAME.toLowerCase().contains("win");
    }

    /**
     * Returns whether the operating system is OSX.
     *
     * @return whether the operating system is OSX
     */
    public static boolean isOSX() {
        return OPERATING_SYSTEM_NAME.toLowerCase().contains("mac");
    }

    /**
     * Returns whether the operating system is unix based.
     * (yes this includes OSX systems too. If you need to test for
     * OSX specifically then call {@link OSUtil#isOSX()})
     *
     * @return whether the operating system is unix based
     */
    public static boolean isUnix() {
        return (OPERATING_SYSTEM_NAME.toLowerCase().contains("nix")
                || OPERATING_SYSTEM_NAME.toLowerCase().contains("nux")
                || OPERATING_SYSTEM_NAME.toLowerCase().contains("aix"));
    }

    /**
     * Returns whether the operating system is Solaris.
     *
     * @return whether the operating system is Solaris
     */
    public static boolean isSolaris() {
        return OPERATING_SYSTEM_NAME.toLowerCase().contains("sunos");
    }

    //end base operating system name/type setup logic

    /**
     * Opens the command shell for the operating system.
     */
    public static void openShell() {
        try {
            switch (OPERATING_SYSTEM) {
                case WINDOWS:
                    Runtime.getRuntime().exec("cmd");
                    break;
                case UNIX:
                    //fall through
                case OSX:
                    String[] args = {"/bin/bash", "-c"};
                    Process proc = new ProcessBuilder(args).start();
                    break;
                case UNKNOWN:
                default:
                    throw new FatalException("Unknown operating system type: " + OPERATING_SYSTEM);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
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

        for (int i = 0; i < directories.length; i++) {
            ret.append(directories[i]);

            if (i != directories.length - 1)
                ret.append(FILE_SEP);
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
    public static String getSystemUsername() {
        return System.getProperty("user.name");
    }

    /**
     * Returns the name of the computer Cyder is currently running on.
     *
     * @return the name of the computer Cyder is currently running on
     */
    public static String getComputerName() {
        String name = "N/A";

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
    public static void setMouseLoc(int x, int y) {
        try {
            Robot Rob = new Robot();
            Rob.mouseMove(x, y);
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Sets the mouse to the middle of the provided component.
     *
     * @param c the component to move the mouse to the center of
     */
    public static void setMouseLoc(Component c) {
        checkNotNull(c);

        try {
            Point topleft = c.getLocationOnScreen();

            int x = (int) (topleft.getX() + c.getWidth() / 2);
            int y = (int) (topleft.getY() + c.getHeight() / 2);

            Robot Rob = new Robot();
            Rob.mouseMove(x, y);
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Deletes the provided file/folder recursively.
     *
     * @param folder the folder/file to delete
     * @return whether the folder/file was successfully deleted
     */
    @CanIgnoreReturnValue
    public static boolean deleteFile(File folder) {
        checkNotNull(folder);

        return deleteFile(folder, true);
    }

    /**
     * Deletes the provided file/folder recursively.
     *
     * @param folder the folder/file to delete
     * @param log whether to log the delete operation. Ideally this is
     *            always true but some rare cases require loggin to be skipped.
     * @return whether the folder/file was successfully deleted
     */
    @CanIgnoreReturnValue
    public static boolean deleteFile(File folder, boolean log) {
        checkNotNull(folder);

        if (log) {
            Logger.log(LoggerTag.SYSTEM_IO, "Requested deletion of: " + folder.getAbsolutePath());
        }

        // directory means recursive case to delete contents
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files.length != 0) {
                for (File file : files) {
                    deleteFile(file, log);
                }
            }
        }

        // contents deleted so now can delete as if it was a file if it isn't
        int inc = 0;
        while (inc < MAX_DELETION_ATTEMPTS) {
            if (folder.delete()) {
                return true;
            }

            inc++;
        }

        if (folder.exists() && log) {
            Logger.log(LoggerTag.SYSTEM_IO, "[DELETION FAILED] file: "
                    + folder.getAbsolutePath());
        }

        return false;
    }

    /**
     * Creates the provided file/folder if possible.
     *
     * @param file the file/folder to attempt to create
     * @return whether the file/fodler could be created
     */
    public static boolean createFile(File file) {
        checkNotNull(file);

        try {
            int inc = 0;
            while (inc < MAX_CREATION_ATTEMPTS) {
                // figure out type to create
                if (file.isFile()) {
                    file.createNewFile();
                } else {
                    file.mkdirs();
                }

                // success
                if (file.exists()) {
                    return true;
                }

                inc++;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return false;
    }

    /**
     * Returns a list of all files contained within the startDir and subdirectories
     * that have the specified extension.
     *
     * @param startDir the starting directory
     * @param extension the specified extension. Ex. ".java" (Pass null to ignore file extensions)
     * @return an ArrayList of all files with the given extension found within the startDir and
     * subdirectories
     */
    public static ArrayList<File> getFiles(File startDir, String extension) {
        checkNotNull(startDir);
        checkArgument(startDir.exists());
        checkNotNull(extension);
        checkArgument(!extension.isEmpty());

        if (startDir == null)
            throw new IllegalArgumentException("Start directory is null");

        // init return set
        ArrayList<File> ret = new ArrayList<>();

        // should be directory but test anyway
        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files == null)
                return ret;

            for (File f : files)
                ret.addAll(getFiles(f, extension));

        } else if (extension == null) {
            ret.add(startDir);
        } else if (FileUtil.getExtension(startDir).equals(extension)) {
            ret.add(startDir);
        }

        return ret;
    }

    /**
     * Returns the UI scaling factor for the primary monitor.
     *
     * @return the UI scaling factor for the primary monitor
     */
    public static double getUIScale() {
        return 1.0; // todo make dynamic and configurable
    }

    /**
     * Returns a string representation of all the network devices connected to the host.
     *
     * @return a string representation of all the network devices connected to the host
     */
    public static String getNetworkDevicesString() {
        StringBuilder sb = new StringBuilder();

        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

            for (NetworkInterface netint : Collections.list(nets)) {
                sb.append("Display name:").append(netint.getDisplayName()).append("\n");
                sb.append("Name:").append(netint.getName()).append("\n");
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return sb.toString();
    }

    /**
     * Returns a string representation of all the monitors connected to the host.
     *
     * @return a string representation of all the monitors connected to the host
     */
    public static String getMonitorStatsString() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < gs.length; i++) {
            DisplayMode dm = gs[i].getDisplayMode();
            sb.append(i);
            sb.append(", width: ");
            sb.append(dm.getWidth());
            sb.append(", height: ");
            sb.append(dm.getHeight());
            sb.append(", bit depth: ");
            sb.append(dm.getBitDepth());
            sb.append(", refresh rate: ");
            sb.append(dm.getRefreshRate());
            sb.append("\n");
        }

        return sb.toString();
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
     * Executes the provided process and prints the output to the provided input handler.
     *
     * Note that this is executed on the current thread so surround invokation of this method
     * with a new thread to avoid blocking the calling thread.
     *
     * @param pipeTo the input handle to print the output to
     * @param builder the process builder to run
     */
    public static void runAndPrintProcess(InputHandler pipeTo, ProcessBuilder builder) {
        checkNotNull(pipeTo);
        checkNotNull(builder);

        try {
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                pipeTo.println(line);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Executes the provided processes successively and prints the output to the provided input handler.
     *
     * @param pipeTo the input handle to print the output to
     * @param builders the process builders to run
     */
    public static void runAndPrintProcessesSuccessive(InputHandler pipeTo, ProcessBuilder... builders) {
        checkNotNull(pipeTo, "pipeTo is null");
        checkNotNull(builders, "builders are null");
        checkArgument(builders.length > 0, "must be at least one builder");

        CyderThreadRunner.submit(() -> {
            for (ProcessBuilder builder : builders) {
                runAndPrintProcess(pipeTo, builder);
            }
        }, "Successive Process Runner, pipeTo = " + pipeTo + ", builders.length() = " + builders.length);
    }

    /**
     * Ensures the dynamic directory and all DynamicDirectories are generated.
     */
    public static void ensureDynamicsCreated() {
        File dynamic = new File(DynamicDirectory.DYNAMIC_PATH);

        if (!dynamic.exists()) {
            dynamic.mkdir();
        }

        for (DynamicDirectory dynamicDirectory : DynamicDirectory.values()) {
            File currentDynamic = buildFile(DynamicDirectory.DYNAMIC_PATH,
                    dynamicDirectory.getDirectoryName());

            if (dynamicDirectory == DynamicDirectory.TEMPORARY) {
                deleteFile(currentDynamic);
            }

            createFile(currentDynamic);
        }
    }

    /**
     * Returns whether the provided binary could be found by invoking
     * the base command in the native shell.
     *
     * @param invokeCommand the invoke command such as ffmpeg for ffmpeg.
     * @return whether the binary could be located
     */
    public static boolean isBinaryInstalled(String invokeCommand) {
        checkNotNull(invokeCommand);
        checkArgument(!invokeCommand.isEmpty());

        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(invokeCommand);
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

        File exes = buildFile(DynamicDirectory.DYNAMIC_PATH,
                DynamicDirectory.EXES.getDirectoryName());

        if (exes.exists()) {
            for (File exe : exes.listFiles()) {
                if (exe.getName().equalsIgnoreCase(filename)) {
                    return true;
                }
            }
        }

        return false;
    }
}
