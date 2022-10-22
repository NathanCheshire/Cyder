package cyder.enums;

import cyder.utils.OsUtil;

import java.io.File;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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

    /**
     * Builds the provided strings into a dynamic file by inserting the OS' path
     * separators between the provided path strings and inserting
     * the dynamic keyword at the beginning.
     *
     * @param directories the names of directories to add one after the other
     * @return a reference to a file which may or may not exist
     */
    public static File buildDynamic(String... directories) {
        checkNotNull(directories);
        checkArgument(directories.length > 0);

        StringBuilder pathString = new StringBuilder(PATH);
        pathString.append(OsUtil.FILE_SEP);

        for (int i = 0 ; i < directories.length ; i++) {
            pathString.append(directories[i]);

            if (i != directories.length - 1) {
                pathString.append(OsUtil.FILE_SEP);
            }
        }

        return new File(pathString.toString());
    }
}
