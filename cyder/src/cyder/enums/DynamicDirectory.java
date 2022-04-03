package cyder.enums;

/**
 * The folders contained in the dynamic directory.
 * Dynamic contains all the components which may be changed during runtime.
 * Anything outside of the dynamic directory should not be changed by Cyder.
 */
public enum DynamicDirectory {
    /**
     * The temporary directory.
     */
    TEMPORARY("tmp"),

    /**
     * The backup directory for user json backups.
     */
    BACKUP("backup"),

    /**
     * The users directory.
     */
    USERS("users"),

    /**
     * The logs directory.
     */
    LOGS("logs"),

    /**
     * The executables directory.
     */
    EXES("exes");

    /**
     * The actual name of the directory to create.
     */
    private final String directoryName;

    /**
     * Construccts a new DynamicDirectory.
     *
     * @param directoryname the actual name of the
     *                      directory the OS will display.
     */
    DynamicDirectory(String directoryname) {
        directoryName = directoryname;
    }

    /**
     * Returns the name of the directory to create.
     *
     * @return the name of the directory to create
     */
    public String getDirectoryName() {
        return directoryName;
    }
}
