package cyder.utilities;

import java.io.File;

/**
 * Static utilies having to do with files, their names, properties, and attributes.
 */
public class FileUtil {
    /**
     * The image formats Cyder supports.
     */
    public static final String[] SUPPORTED_IMAGE_EXTENSIONS = new String[] {".png", ".jpg", ".jpeg"};

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

    //todo StringUtil filename and extension methods should be here, look for other methods that should be here
    // also think about what else might be useful
}
