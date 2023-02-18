package cyder.enumerations;

import cyder.logging.Logger;
import cyder.strings.CyderStrings;
import cyder.utils.OsUtil;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The folders/files contained in the dynamic directory.
 * Dynamic contains all the components which may be changed during runtime.
 * Anything outside of the dynamic directory should not be changed by during a runtime instance of Cyder.
 */
public enum Dynamic {
    /**
     * The temporary directory.
     */
    TEMP("tmp"),

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
     * The actual name of the file to create.
     */
    private final String fileName;

    /**
     * Constructs a new directory.
     *
     * @param fileName the actual name of the file the OS will display
     */
    Dynamic(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns the name of the directory to create.
     *
     * @return the name of the directory to create
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns a pointer file for this dynamic.
     *
     * @return a pointer file for this dynamic
     */
    public File getPointerFile() {
        return buildDynamic(fileName);
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

    /**
     * Ensures all dynamics are created.
     *
     * @return whether all dynamics were created.
     */
    public static boolean ensureDynamicsCreated() {
        File dynamic = Dynamic.buildDynamic();

        if (!dynamic.exists()) {
            if (!dynamic.mkdir()) {
                Logger.log("Could not create dynamic directory");
                return false;
            }
        }

        for (Dynamic dynamicDirectory : Dynamic.values()) {
            File currentDynamic = Dynamic.buildDynamic(dynamicDirectory.getFileName());

            if (dynamicDirectory == Dynamic.TEMP) {
                OsUtil.deleteFile(currentDynamic);
            }

            if (!currentDynamic.exists() && !OsUtil.createFile(currentDynamic, false)) {
                Logger.log("Failed to create dynamic directory"
                        + CyderStrings.colon
                        + CyderStrings.space
                        + currentDynamic.getName()
                        + CyderStrings.space
                        + "at location"
                        + CyderStrings.colon
                        + CyderStrings.space
                        + currentDynamic.getAbsolutePath());
                return false;
            }
        }

        return true;
    }
}
