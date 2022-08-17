package cyder.utils;

import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Utilities for getting static resources.
 */
public final class StaticUtil {
    /**
     * Suppress default constructor.
     */
    private StaticUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The list of static files found when Cyder was first launched.
     */
    private static final ImmutableList<File> STATIC_FILES;

    /**
     * The name of the static directory which holds all the static files and resources needed by Cyder.
     */
    private static final String STATIC = "static";

    static {
        STATIC_FILES = FileUtil.getFiles(new File(STATIC));
    }

    /**
     * Returns a file reference to the first file found with the provided name in any subdirectory of the static directory.
     *
     * @param filename the name of the file to location such as "alpha" or "alpha.png"
     * @return a file reference to the first file found with the provided name
     * @throws FileNotFoundException if a file with the provided name cannot be located in any directory/subdirectory
     */
    public static File getStaticResource(String filename) throws FileNotFoundException {
        String extension = "";

        if (filename.contains(".")) {
            int splitIndex = filename.lastIndexOf('.');
            filename = filename.substring(splitIndex);
            extension = filename.substring(splitIndex + 1);
        }

        for (File staticFile : STATIC_FILES) {
            if (FileUtil.getFilename(staticFile).equals(filename)) {
                if (extension.isEmpty() || FileUtil.getExtension(staticFile).equals("." + extension)) {
                    return staticFile;
                }
            }
        }

        throw new FileNotFoundException("Could not find static file: " + filename
                + (extension.isEmpty() ? "" : ", extension: " + extension));
    }
}
