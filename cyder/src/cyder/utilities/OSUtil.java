package cyder.utilities;

import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.ui.ConsoleFrame;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Methods that depend on the Operating System Cyder is running on are placed in this class.
 */
@SuppressWarnings({"unused", "WeakerAccess"}) /* Some methods have no use still, some methods aren't used yet */
public class OSUtil {

    public static final ArrayList<String> invalidWindowsFilenames = new ArrayList<>(){
        @Override
        public ArrayList<String> clone() throws AssertionError {
            throw new AssertionError("Attempted clone of final list");
        }

        {
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
     * Prevent illegal class instantiation.
     */
    public OSUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * Returns whether the provided filename is valid for the operating system
     * Cyder is currently running on.
     *
     * @param filename the desired filename
     * @return whether the provided filename is valid for the operating system
     * Cyder is currently running on
     */
    @SuppressWarnings("HardcodedFileSeparator") /* that's the point of this method */
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
    @SuppressWarnings("WeakerAccess") /* anything can access this since it's final */
    public static final String OPERATING_SYSTEM_NAME = System.getProperty("os.name");

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
        return OPERATING_SYSTEM_NAME.contains("win");
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
        return (OPERATING_SYSTEM_NAME.contains("nix")
                || OPERATING_SYSTEM_NAME.contains("nux")
                || OPERATING_SYSTEM_NAME.contains("aix"));
    }

    /**
     * Returns whether the operating system is Solaris.
     *
     * @return whether the operating system is Solaris
     */
    public static boolean isSolaris() {
        return OPERATING_SYSTEM_NAME.contains("sunos");
    }

    //end base operating system name/type setup logic

    /**
     * Opens the command shell for the operating system.
     */
    @SuppressWarnings({"CallToRuntimeExec", "HardcodedFileSeparator"}) /* the point of this method is to handle non-portability */
    public static void openShell() {
        try {
            switch (OPERATING_SYSTEM) {
                case WINDOWS:
                    Runtime.getRuntime().exec("cmd");
                    break;
                case UNIX:
                    //fall through
                case OSX:
                    String[] args = new String[]{"/bin/bash", "-c", "your_command", "with", "args"};
                    Process proc = new ProcessBuilder(args).start();
                    break;
                case UNKNOWN:
                    throw new RuntimeException("UNKNOWN OPERATING SYSTEM");
                default:
                    throw new IllegalStateException("Unknown operating system type: " + OPERATING_SYSTEM);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * The file separator character used for this operating system.
     */
    public static final String FILE_SEP = System.getProperty("file.separator");

    /**
     * Saves the provided file in the current user's files/ dir.
     *
     * @param name the filename to create
     * @return a File object representing the file that was created
     * @throws IllegalStateException if the file could not be created at this time
     */
    @SuppressWarnings("ResultOfMethodCallIgnored") /* The point is to create files so ignore */
    public static File createFileInUserSpace(String name) {
        if (!StringUtil.isNull(ConsoleFrame.getConsoleFrame().getUUID())) {
            File saveDir = new File("dynamic" + FILE_SEP
                    + "users" + FILE_SEP + ConsoleFrame.getConsoleFrame().getUUID()
                    + FILE_SEP + "Files");
            File createFile = new File(saveDir, name);

            if (createFile.exists())
                throw new IllegalStateException("Provided file already exists");

            try {
                if (!saveDir.exists())
                    saveDir.mkdir();

                createFile.createNewFile();
                return createFile;
            } catch (Exception ignored) {}
            //impossible to throw due to check, or is it?
        }

        throw new IllegalStateException("File could not be created at this time: " + name);
    }

    public static final String TMP_DIR_PATH = buildPath("dynamic","tmp");

    /**
     * Creates the provided file in the tmp/ directory.
     *
     * @param name the filename to create
     * @return a File object representing the file that was created
     * @throws IllegalStateException if the file could not be created
     */
    @SuppressWarnings("ResultOfMethodCallIgnored") /* Creating files */
    public static File createFileInSystemSpace(String name) {
        File tmpDir = new File(TMP_DIR_PATH);

        if (!tmpDir.exists())
            tmpDir.mkdir();

        File createFile = new File(TMP_DIR_PATH + FILE_SEP + name);

        if (createFile.exists())
            throw new IllegalStateException("Provided file already exists");
        try {
            createFile.createNewFile();
            Logger.log(Logger.Tag.SYSTEM_IO, "Created temperatory file: " + name);
            return createFile;
        } catch (Exception e) {
            //this shouldn't happen typically
            ExceptionHandler.handle(e);
        }

        throw new IllegalStateException("File could not be created at this time: " + name);
    }

    /**
     * Deletes the temperary directory if it exists.
     */
    public static void deleteTempDir() {
        try {
            OSUtil.delete(new File(OSUtil.TMP_DIR_PATH));
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
        if (directories == null)
            throw new IllegalArgumentException("Directories is null");
        if (directories.length == 0)
            throw new IllegalArgumentException("Directories length is null");

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
        if (directories == null)
            throw new IllegalArgumentException("Directories is null");
        if (directories.length == 0)
            throw new IllegalArgumentException("Directories length is null");

        StringBuilder ret = new StringBuilder();

        for (int i = 0; i < directories.length; i++) {
            ret.append(directories[i]);

            if (i != directories.length - 1)
                ret.append(FILE_SEP);
        }

        return new File(ret.toString());
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
     * Deletes the provided file/folder recursively.
     *
     * @param folder the folder/file to delete
     * @return whether the folder/file was successfully deleted
     */
    public static boolean delete(File folder) {
        Logger.log(Logger.Tag.ACTION, "Requested deletion of: " + folder.getAbsolutePath());

        File[] files = folder.listFiles();

        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    if (!delete(f)) {
                        return false;
                    }
                }

                else {
                    if (!f.delete()) {
                        return false;
                    }
                }
            }
        }

        boolean ret = folder.delete();

        if (!ret)
            Logger.log(Logger.Tag.ACTION, "[DELETION FAILED] " + folder.getAbsolutePath());

        return ret;
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
     * Zips the provided file/folder and deletes the original if successful and requested.
     *
     * @param source the file/dir to zip
     * @param destination the destination of the zip archive
     */
    public static void zip(final String source, final String destination)  {
        AtomicBoolean ret = new AtomicBoolean(true);

        String usedFileName;

        try {

            if (new File(destination).exists()) {
                int incrementer = 1;
                usedFileName = destination.replace(".zip","") + "_" + incrementer + ".zip";

                while (new File(usedFileName).exists()) {
                    incrementer++;
                    usedFileName = destination.replace(".zip","") + "_" + incrementer + ".zip";
                }
            } else {
                usedFileName = destination;
            }

            Path zipFile = Files.createFile(Paths.get(usedFileName));
            Path sourceDirPath = Paths.get(source);

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile));
                 Stream<Path> paths = Files.walk(sourceDirPath)) {
                paths.filter(path -> !Files.isDirectory(path)).forEach(path -> {
                    ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString());
                    try {
                        zipOutputStream.putNextEntry(zipEntry);
                        Files.copy(path, zipOutputStream);
                        zipOutputStream.closeEntry();
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                });
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);

            if (!(e instanceof NoSuchFileException))
                ret.set(false);
        }

        ret.get();
    }

    /**
     * Returns the UI scaling factor for the primary monitor.
     *
     * @return the UI scaling factor for the primary monitor
     */
    public static double getUIScale() {
        return 1.0;
        //todo this is affected by the starting monitor so maybe allow the user to be able to change this
        // requires a restart to try and think of a script for that?

        //todo move to user util and init with 1? shouldn't both of my monitors return 1?
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
        StringSelection selection = new StringSelection(clipboardContents);
        java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    //todo skip icons should jsut be triangle with bar on end

    //todo all class.subclass should be in an objects package within that package
    // since they're needed by something outside of the class

    //todo put markup (paint), image average, image pixelator, and image resizer all in a image factory widget
    // most methods should be in image utils probably. Use a layout for this, separate window for tools

    //todo finish CyderGrid methods and new convex hull
}
