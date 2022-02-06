package cyder.utilities;

import java.io.File;

/**
 * Static utilies having to do with files, their names, properties, and attributes.
 */
public class FileUtil {
    public static final String[] SUPPORTED_IMAGE_EXTENSIONS = new String[] {".png", ".jpg", ".jpeg"};

    /**
     * Returns whether the provided file is a supported image file.
     *
     * @param f the file to determine if it is a supported image type
     * @return whether the provided file is a supported image file
     */
    public static boolean isSupportedImageExtension(File f) {
        return StringUtil.in(StringUtil.getExtension(f.getName()), true, SUPPORTED_IMAGE_EXTENSIONS);
    }

    //todo some stuff from String util should be here 
}
