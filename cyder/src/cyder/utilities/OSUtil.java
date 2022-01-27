package cyder.utilities;

import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.ConsoleFrame;

import java.io.File;

/**
 * Methods that depend on the Operating System Cyder is running on are placed in this class.
 */
public class OSUtil {

    /**
     * Returns whether or not the provided filename is valid for the operating system
     * Cyder is currently running on.
     *
     * @param filename the desired filename
     * @return whether or not the provided filename is valid for the operating system
     *         Cyder is currently running on
     */
    public static boolean isValidFilename(String filename) {
        //todo system independent

        switch (OPERATING_SYSTEM) {
            case OSX:
                //todo method
                break;
            case WINDOWS:
                //todo copy over and fix issues
                break;
            case UNIX:
                //todo method
                break;
            case UNKNOWN:
                throw new IllegalStateException("Unknown operating system: " + OPERATING_SYSTEM_NAME);
        }

        return false;
    }

    /**
     * Returns whether or not the provided filename is valid for the operating system
     * Cyder is currently running on and whether or not the filename follows standard naming procedures.
     *
     * @param filename the desired filename
     * @return Returns whether or not the provided filename is valid for the operating system
     *      * Cyder is currently running on and whether or not the filename follows standard naming procedures
     */
    public static boolean isValidAndStandardFilename(String filename) {
        //todo system independent
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
                    String[] args = new String[] {"/bin/bash", "-c", "your_command", "with", "args"};
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
            } catch (Exception ignored) {}
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

        for (int i = 0 ; i < directories.length ; i++) {
            ret.append(directories[i]);

            if (i != directories.length - 1)
                ret.append(FILE_SEP);
        }

        return ret.toString();
    }

    //todo command finder needs to be able to execute on it's own and take into a file which should be
    // generated before jar compilation,

    //todo redo image resizer and put cropper inside of it

    //todo compact text mode needs to refresh panes when toggled

    //todo be able to drag the height of the console menu and
    // remember the relative percentage of the height in userdata

    //todo changing the consoleframe background to something needs it's own method and param
    // to change/not change the size

    //todo fix weird bug with setting console orientation when it originally loads
    // and you can do ctrl + up and it refreshes a little

    //todo should be able to set consoleFrame to image regardless of if it's a saved file or not

    //todo the way we resize a frame should affect what way the background recedes

    //todo remember console orientation on exiting Cyder

    //todo display all files inside of user editor now too

    //todo code analyzing and reflection and such needs to be disabled if JAR_MODE is on

    //todo trim any multiple spaces out of console clock format input field

    //todo make a method in cydertextfield to get trimmed text that consolidates any whitespace down to
    // one space and then trims the string too

    //todo logic to fix log with exit code that it wasn't found should also consolidate duplicate lines
}
