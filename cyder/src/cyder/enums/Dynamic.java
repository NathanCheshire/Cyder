package cyder.enums;

/**
 * The folders contained in the dynamic directory.
 * Dynamic contains all the components which may be changed during runtime.
 * Anything outside of the dynamic directory should not be changed by Cyder.
 */
public enum Dynamic {
    /**
     * The temporary directory.
     */
    TEMP("tmp"),

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
     * The path from the top level Cyder directory to the dynamic root.
     */
    public static final String PATH = "dynamic";

    /**
     * The actual name of the directory to create.
     */
    private final String directoryName;

    /**
     * Constructs a new directory.
     *
     * @param directoryName the actual name of the directory the OS will display
     */
    Dynamic(String directoryName) {
        this.directoryName = directoryName;
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
