package cyder.utilities;

import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.ConsoleFrame;

import java.awt.*;
import java.io.File;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Methods that depend on the Operating System Cyder is running on are placed in this class.
 */
public class OSUtil {

    public static final String[] invalidWindowsFilenames = new String[]{"CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8",
            "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

    /**
     * Returns whether or not the provided filename is valid for the operating system
     * Cyder is currently running on.
     *
     * @param filename the desired filename
     * @return whether or not the provided filename is valid for the operating system
     * Cyder is currently running on
     */
    public static boolean isValidFilename(String filename) {
        filename = filename.trim();

        switch (OPERATING_SYSTEM) {
            case OSX:
                return filename.contains("/") || filename.contains("\0");
            case WINDOWS:
                //invalid chars for Windows in a filename
                if (filename.matches("[*?|/\":<>\\\\']+"))
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
     * Returns whether or not the operating system is windows.
     *
     * @return whether or not the operating system is windows
     */
    public static boolean isWindows() {
        return OPERATING_SYSTEM_NAME.contains("win");
    }

    /**
     * Returns whether or not the operating system is OSX.
     *
     * @return whether or not the operating system is OSX
     */
    public static boolean isOSX() {
        return OPERATING_SYSTEM_NAME.contains("mac");
    }

    /**
     * Returns whether or not the operating system is unix based.
     * (yes this includes OSX systems too. If you need to test for
     * OSX specifically then call {@link OSUtil#isOSX()})
     *
     * @return whether or not the operating system is unix based
     */
    public static boolean isUnix() {
        return (OPERATING_SYSTEM_NAME.contains("nix")
                || OPERATING_SYSTEM_NAME.contains("nux")
                || OPERATING_SYSTEM_NAME.contains("aix"));
    }

    /**
     * Returns whether or not the operating system is Solaris.
     *
     * @return whether or not the operating system is Solaris
     */
    public static boolean isSolaris() {
        return OPERATING_SYSTEM_NAME.contains("sunos");
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
     */
    public static File createFileInUserSpace(String name) {
        if (!StringUtil.empytStr(ConsoleFrame.getConsoleFrame().getUUID())) {
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
            } catch (Exception ignored) {
            }
            //impossible to throw due to check, or is it?
        }

        return null;
    }

    /**
     * Creates the provided file in the tmp/ directory.
     *
     * @param name the filename to create
     * @return a File object representing the file that was created
     */
    public static File createFileInSystemSpace(String name) {
        File saveDir = new File("cyder" + FILE_SEP + "src"
                + FILE_SEP + "cyder" + FILE_SEP + "tmp");
        File createFile = new File(saveDir, name);

        if (!saveDir.exists())
            saveDir.mkdir();

        if (createFile.exists())
            throw new IllegalStateException("Provided file already exists");

        try {
            createFile.createNewFile();
            return createFile;
        } catch (Exception e) {
            //this shouldn't happen typically
            ExceptionHandler.handle(e);
        }

        return null;
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
     */
    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                }

                else {
                    f.delete();
                }
            }
        }

        folder.delete();
    }

    /**
     * Returns a list of all files contained within the startDir and sub directories
     * that have the specified extension.
     *
     * @param startDir the starting directory
     * @param extension the specified extension. Ex. ".java" (Pass null to ignore file extensions)
     * @return an ArrayList of all files with the given extension found within the startDir and
     * sub directories
     */
    public static ArrayList<File> getFiles(File startDir, String extension) {
        if (startDir == null)
            throw new IllegalArgumentException("Start directory is null");

        //init return set
        ArrayList<File> ret = new ArrayList<>();

        //should be directory but test anyway
        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            for (File f : files)
                ret.addAll(getFiles(f, extension));

        }
        else if (extension == null) {
            ret.add(startDir);
        }

        else if (StringUtil.getExtension(startDir).equals(extension)) {
            ret.add(startDir);
        }

        return ret;
    }

    /**
     * Zips the provided file/folder and deletes the original if successful and requested.
     *
     * @param source the file/dir to zip
     * @param destination the destination of the zip archive
     * @param deleteOnSuccess whether to delete the original file/directory
     * @return whether the zipping was successful
     */
    public static boolean zip(final String source, final String destination, boolean deleteOnSuccess)  {
        boolean ret = zip(source,destination);

        if (ret) {
            deleteFolder(new File(source));
        }

        return ret;
    }

    /**
     * Zips the provided file/folder.
     *
     * @param source the file/dir to zip
     * @param destination the destination of the zip archive
     * @return whether the zipping was successful
     */
    public static boolean zip(final String source, final String destination) {
        AtomicBoolean ret = new AtomicBoolean(true);

        try {
            Path zipFile = Files.createFile(Paths.get(destination));
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

        return ret.get();
    }

    //todo dispose thread doesn't resolve?

    //todo make gifs like devon crawford's A* readme

    //todo wierd login frame big frame bug?

    //todo wipe "UserFile" command
    //todo wipeall command that essentailly resets the user to as if they just created their user

    //todo make sure closing console frame doesn't save it's y to the top, bug

    //todo widgetpackages.json will go away since we're going to use guava to get all widgets from all packages

    //todo command finder needs to be able to execute on it's own and take into a file which should be
    // generated before jar compilation,
    //todo make this a command to regenerate the list of valid commands
    // from InputHandler which the script will then look through

    //todo user redis for storing user statistics in memory instead of constant IO to/from files

    //todo redo image resizer and put cropper inside of it

    //todo be able to drag the height of the console menu and
    // remember the relative percentage of the height in userdata

    //todo changing the consoleframe background to something needs it's own method and param
    // to change/not change the size

    //todo fix weird bug with setting console orientation when it originally loads
    // and you can do ctrl + up and it refreshes a little

    //todo remember console orientation on exiting Cyder

    //todo code analyzing and reflection and such needs to be disabled if JAR_MODE is on
}
