package cyder.utilities;

import cyder.handlers.internal.ExceptionHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Static utilies having to do with files, their names, properties, and attributes.
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
    public static final String JPG_SIGNATURE = "FFD8FF";

    /**
     * Returns whether the provided file is a supported image file.
     *
     * @param f the file to determine if it is a supported image type
     * @return whether the provided file is a supported image file
     */
    public static boolean isSupportedImageExtension(File f) {
        //todo also validate the file signature
        return StringUtil.in(StringUtil.getExtension(f.getName()), true, SUPPORTED_IMAGE_EXTENSIONS);
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

    //todo StringUtil filename and extension methods should be here, look for other methods that should be here
    // also think about what else might be useful
}
