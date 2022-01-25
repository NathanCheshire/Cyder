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
        //todo
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
        //todo
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
        } catch (Exception ignored) {}
        //impossible to throw due to check, or is it?

        return null;
    }

    //todo redo image resizer and put cropper inside of it

    //todo component util for things such as rendering a component or getting a screenshot?

    //todo fix debug lines for consoleFrame

    //todo compact mode for console frame menu

    //todo changing the consoleframe background to something needs it's own method and param
    // to change/not change the size

    //todo should be able to set consoleFrame to image regardless of if it's saved

    //todo the way we resize a frame should affect what background is shown

    //todo remember console orientation on exiting Cyder

    //todo implement curl command

    //todo display all files inside of user editor now too
}
