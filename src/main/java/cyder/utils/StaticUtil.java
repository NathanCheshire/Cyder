package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.CyderStrings;

import java.io.File;
import java.util.LinkedHashMap;

/**
 * Utilities for getting static resources.
 */
public final class StaticUtil {
    /**
     * The name of the static directory which holds all the static files and resources needed by Cyder.
     */
    private static final String STATIC = "static";

    /**
     * The map of static files located.
     */
    private static final ImmutableMap<String, File> staticFiles;

    /**
     * The map of static folders located.
     */
    private static final ImmutableMap<String, File> staticFolders;

    static {
        staticFiles = getStaticFiles();
        Logger.log(LogTag.SYSTEM_IO, "Loaded " + staticFiles.size() + " static files");

        staticFolders = getStaticFolders();
        Logger.log(LogTag.SYSTEM_IO, "Loaded " + staticFolders.size() + " static folders");
    }

    /**
     * Suppress default constructor.
     */
    private StaticUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the path to the file found with the provided name/path in any subdirectory of the static directory.
     *
     * @param filename the name of the file, with or without the extension
     *                 or as much of the relative file path as possible
     * @return the absolute path to the file
     * @throws IllegalArgumentException if a file cannot be found matching the provided name/path
     */
    public static String getStaticPath(String filename) {
        return getStaticResource(filename).getAbsolutePath();
    }

    /**
     * Returns a file reference to the file found with the provided name/path
     * in any subdirectory of the static directory.
     *
     * @param filename the name of the file, with or without the extension
     *                 or as much of the relative file path as possible. For example,
     *                 providing "cyder.txt" or "txt/cyder.txt" are valid options
     * @return a file reference to the static file if found
     * @throws IllegalArgumentException if a file cannot be found matching the provided name/path
     */
    public static File getStaticResource(String filename) {
        Preconditions.checkNotNull(filename);
        Preconditions.checkArgument(!filename.isEmpty());

        if (staticFiles.containsKey(filename)) {
            return staticFiles.get(filename);
        }

        for (String fileKey : staticFiles.keySet()) {
            if (fileKey.contains(filename)) {
                return staticFiles.get(fileKey);
            }
        }

        throw new IllegalArgumentException("Could not find static file: " + filename);
    }

    /**
     * Returns the directory found with the provided name.
     *
     * @param folderName the name of the directory to locate or as much of the relative path as possible.
     *                   For example, providing "Default.png", "audio/Default.png", or "pictures/audio/Default.png"
     *                   will likely all return the same result
     * @return a file reference to the directory
     * @throws IllegalArgumentException if the provided directory cannot be found
     */
    public static File getStaticDirectory(String folderName) {
        Preconditions.checkNotNull(folderName);
        Preconditions.checkArgument(!folderName.isEmpty());

        if (staticFolders.containsKey(folderName)) {
            return staticFolders.get(folderName);
        } else {
            for (String staticFolderNameKey : staticFolders.keySet()) {
                if (staticFolderNameKey.contains(folderName)) {
                    return staticFolders.get(staticFolderNameKey);
                }
            }
        }

        throw new FatalException("Could not find static directory with name: " + folderName);
    }

    /**
     * Returns a map of relative file names to file pointers. For example, a static file located relatively to
     * the static directory with the path "static/txt/cyder.txt" will produce a key of "txt/cyder.txt".
     *
     * @return a map of relative file names to file pointers
     */
    private static ImmutableMap<String, File> getStaticFiles() {
        LinkedHashMap<String, File> ret = new LinkedHashMap<>();

        FileUtil.getFiles(new File(STATIC), true)
                .forEach(folder -> ret.put(folder.getPath().replaceAll("\\\\+", "/"), folder));

        return ImmutableMap.copyOf(ret);
    }

    /**
     * Returns a map of relative folder names to folder file pointers. For example, a static directory located
     * relatively to the static directory with the path "static/pictures/audio" will produce a key of "pictures/audio".
     *
     * @return a map of folder names to folder file pointers
     */
    private static ImmutableMap<String, File> getStaticFolders() {
        LinkedHashMap<String, File> ret = new LinkedHashMap<>();

        FileUtil.getFolders(new File(STATIC), true)
                .forEach(folder -> ret.put(folder.getPath().replaceAll("\\\\+", "/"), folder));

        return ImmutableMap.copyOf(ret);
    }
}
