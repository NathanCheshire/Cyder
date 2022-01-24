package cyder.utilities;

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


}
