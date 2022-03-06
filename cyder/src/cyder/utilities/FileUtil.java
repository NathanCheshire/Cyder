package cyder.utilities;

import cyder.handlers.internal.ExceptionHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Static utilities having to do with files, their names, properties, and attributes.
 */
public class FileUtil {
    /**
     * The image formats Cyder supports.
     */
    public static final String[] SUPPORTED_IMAGE_EXTENSIONS = new String[] {".png", ".jpg", ".jpeg"};

    /**
     * The metadata signature for a png file.
     */
    public static final int[] PNG_SIGNATURE =  new int[]{0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    /**
     * The metadata signature for a jpg file.
     */
    public static final int[] JPG_SIGNATURE = new int[]{0xFF, 0xD8, 0xFF};

    /**
     * Returns whether the provided file is a supported image file by validating
     * the file extension and the file byte signature.
     *
     * @param f the file to determine if it is a supported image type
     * @return whether the provided file is a supported image file
     */
    public static boolean isSupportedImageExtension(File f) {
        return StringUtil.in(getExtension(f.getName()), true, SUPPORTED_IMAGE_EXTENSIONS)
                && (matchesSignature(f, PNG_SIGNATURE) || matchesSignature(f, JPG_SIGNATURE));
    }

    /**
     * Returns whether the given file matches the provided signature.
     * Example: passing a png image and an integer array of "89 50 4E 47 0D 0A 1A 0A"
     *          should return true
     *
     * @param file the file to validate
     * @param expectedSignature the expected file signature bytes
     * @return whether the given file matches the provided signature
     */
    public static boolean matchesSignature(File file, int[] expectedSignature) {
        if (file == null || expectedSignature == null || expectedSignature.length == 0)
            return false;
        if (!file.exists())
            return false;

        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            int[] headerBytes = new int[expectedSignature.length];

            for (int i = 0; i < expectedSignature.length; i++) {
                headerBytes[i] = inputStream.read();

                if (headerBytes[i] != expectedSignature[i]) {
                    return false;
                }
            }
        } catch (IOException ex) {
            ExceptionHandler.handle(ex);
            return false;
        }

        return true;
    }

    /**
     * Uses a regex to get the file name of the provided file, does not return the period.
     *
     * @param file the file of which to return the name of (this does not include the
     *             extension; use {@link File#getName()} )} to get the full filename + extension)
     * @return the file name requested
     */
    public static String getFilename(String file) {
        return file.replaceAll("\\.([^.]+)$", "");
    }

    /**
     * Uses a regex to get the file extension of the provided file, returns the period too.
     *
     * @param file the name of the file of which to return the extension of
     * @return the file extension requested
     */
    public static String getExtension(String file) {
        return file.replace(getFilename(file), "");
    }

    /**
     * Uses a regex to get the file name of the provided file, does not return the period.
     *
     * @param file the name of the file of which to return the name of (this does not include the
     *             extension; use {@link File#getName()})} to get the full filename + extension)
     * @return the file name requested
     */
    public static String getFilename(File file) {
        return file.getName().replaceAll("\\.([^.]+)$", "");
    }

    /**
     * Uses a regex to get the file extension of the provided file, returns the period too.
     *
     * @param file the file of which to return the extension of
     * @return the file extension requested
     */
    public static String getExtension(File file) {
        return file.getName().replace(getFilename(file), "");
    }

    /**
     * Supported font types that are loaded upon Cyder's start.
     */
    public static final ArrayList<String> validFontExtensions = new ArrayList<>() {{
        add(".ttf");
    }};

    /**
     * Returns whether the contents of the two files are equal.
     *
     * @param fileOne the first file
     * @param fileTwo the second file
     * @return whether the contents of the two file are equal
     */
    @SuppressWarnings("UnstableApiUsage") /* Guava */
    public static boolean fileContentsEqual(File fileOne, File fileTwo) {
        if (fileOne == null || fileTwo == null)
            return false;

        if (!fileOne.exists() || !fileTwo.exists())
            return false;

       boolean ret;

        try {
            ret = com.google.common.io.Files.equal(fileOne, fileTwo);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            ret = false;
        }

        return ret;
    }
}
