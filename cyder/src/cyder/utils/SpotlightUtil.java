package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.enums.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Utility methods revolving around stealing the spotlight images for on the Windows file system.
 */
public final class SpotlightUtil {
    /**
     * Suppress default constructor.
     */
    private SpotlightUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The prefix for the content deliver manager folder which contains the spotlight image files.
     */
    public static final String CONTENT_DELIVERY_MANAGER_PREFIX = "Microsoft.Windows.ContentDeliveryManager_";

    /**
     * The default content delivery manager suffix
     */
    public static final String CONTENT_DELIVERY_MANAGER_SUFFIX = "cw5n1h2txyewy";

    /**
     * Wipes the windows spotlight directory. Windows will download new ones eventually.
     */
    public static void wipeSpotlights() {
        Preconditions.checkArgument(OsUtil.isWindows(), "Host OS is not Windows");

        File spotlightsDir = getSpotlightsDirectory();

        if (spotlightsDir.exists()) {
            try {
                File spotlightDirectory = getSpotlightsDirectory();

                File[] files = spotlightDirectory.listFiles();
                int length = files == null ? 0 : files.length;

                Console.INSTANCE.getInputHandler().println("Windows spotlight images wiped from directory:\n\""
                        + spotlightsDir.getAbsolutePath() + "\"");
                Console.INSTANCE.getInputHandler().println("Spotlights found: " + length);

                if (files != null && files.length > 0) {
                    for (File spotlight : files) {
                        OsUtil.deleteFile(spotlight);
                    }
                }

                files = spotlightDirectory.listFiles();
                length = files == null ? 0 : files.length;

                Console.INSTANCE.getInputHandler().println("Spotlights left: " + length);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
    }

    /**
     * Returns the parent directory of the spotlight images.
     *
     * @return the parent directory of the spotlight images
     */
    public static File getSpotlightsDirectory() {
        return new File(OsUtil.buildPath(
                OsUtil.WINDOWS_ROOT, "users", OsUtil.getOsUsername(),
                "AppData", "Local", "Packages", CONTENT_DELIVERY_MANAGER_PREFIX
                        + CONTENT_DELIVERY_MANAGER_SUFFIX, "LocalState", "Assets"));
    }

    /**
     * The minimum savable size of a spotlight.
     */
    public static final int MINIMUM_SIZE = 600;

    /**
     * Saves the Windows spotlights to the provided directory.
     *
     * @param saveDir the directory o save the files to
     */
    public static void saveSpotlights(File saveDir) {
        Preconditions.checkNotNull(saveDir);
        Preconditions.checkArgument(saveDir.isDirectory(), "Destination directory is not a folder");
        Preconditions.checkArgument(saveDir.exists(), "Destination directory does not exists");
        Preconditions.checkArgument(OsUtil.isWindows(), "Host OS is not Windows");

        try {
            int acc = 0;

            File[] files = getSpotlightsDirectory().listFiles();

            if (files == null || files.length == 0) {
                return;
            }

            for (File spotlight : files) {
                ImageIcon icon = new ImageIcon(spotlight.getAbsolutePath());

                // skip small previews and the weird vertical ones
                if (isPortrait(icon) || icon.getIconWidth() < MINIMUM_SIZE
                        || icon.getIconHeight() < MINIMUM_SIZE) {
                    continue;
                }

                Files.copy(spotlight.toPath(), Paths.get(saveDir + OsUtil.FILE_SEP
                                + acc + Extension.PNG.getExtension()),
                        StandardCopyOption.REPLACE_EXISTING);
                acc++;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns whether the provided icon is a portrait photo.
     *
     * @param icon the icon to test
     * @return whether the provided icon is a portrait photo
     */
    private static boolean isPortrait(ImageIcon icon) {
        Preconditions.checkNotNull(icon);

        return icon.getIconWidth() < icon.getIconHeight();
    }
}
