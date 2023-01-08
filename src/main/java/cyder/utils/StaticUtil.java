package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.CyderStrings;

import java.io.File;

/**
 * Utilities for getting static resources.
 */
public final class StaticUtil {
    // todo saving from a left or right screen stat doesn't work, image is stretched
    // todo cache this if possible
    /**
     * The list of static files found when Cyder was first launched.
     */
    private static final ImmutableList<File> staticFiles;

    /**
     * The map of static folders located.
     */
    private static final ImmutableMap<String, File> staticFolders;

    /**
     * The name of the static directory which holds all the static files and resources needed by Cyder.
     */
    private static final String STATIC = "static";

    static {
        staticFiles = FileUtil.getFiles(new File(STATIC));
        Logger.log(LogTag.SYSTEM_IO, "Loaded " + staticFiles.size() + " static files");

        staticFolders = FileUtil.getFolders(new File(STATIC));
        Logger.log(LogTag.SYSTEM_IO, "Loaded " + staticFolders.size() + " static folders");
    }

    /**
     * Suppress default constructor.
     */
    private StaticUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the path to the first file found with the provided name in any subdirectory of the static directory.
     *
     * @param filename the name of the file to location such as "alpha" or "alpha.png"
     * @return the complete path to the first file found with the provided name
     * @throws IllegalArgumentException if a file with the provided name cannot be located in any directory/subdirectory
     */
    public static String getStaticPath(String filename) {
        return getStaticResource(filename).getAbsolutePath();
    }

    /**
     * Returns a file reference to the first file found with the provided name in any subdirectory of the static directory.
     *
     * @param filename the name of the file to location such as "alpha" or "alpha.png"
     * @return a file reference to the first file found with the provided name
     * @throws IllegalArgumentException if a file with the provided name cannot be located in any directory/subdirectory
     */
    public static File getStaticResource(String filename) {
        Preconditions.checkNotNull(filename);
        Preconditions.checkArgument(!filename.isEmpty());

        String extension = "";

        if (filename.contains(".")) {
            int splitIndex = filename.lastIndexOf('.');
            extension = filename.substring(splitIndex + 1);
            filename = filename.substring(0, splitIndex);
        }

        for (File staticFile : staticFiles) {
            if (FileUtil.getFilename(staticFile).equalsIgnoreCase(filename)) {
                if (extension.isEmpty() || FileUtil.getExtension(staticFile).equalsIgnoreCase("." + extension)) {
                    return staticFile;
                }
            }
        }

        throw new IllegalArgumentException("Could not find static file: " + filename
                + (extension.isEmpty() ? "" : ", extension: " + extension));
    }

    /**
     * Returns the directory found with the provided name.
     *
     * @param folderName the name of the directory to locate
     * @return a reference to the directory
     * @throws IllegalArgumentException if the provided directory cannot be found
     */
    public static File getStaticDirectory(String folderName) {
        Preconditions.checkNotNull(folderName);
        Preconditions.checkArgument(!folderName.isEmpty());
        Preconditions.checkArgument(staticFolders.containsKey(folderName));

        return staticFolders.get(folderName);
    }
}
