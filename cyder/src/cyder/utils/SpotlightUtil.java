package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Utility methods revolving around stealing the spotlight images for on the Windows file system.
 */
public class SpotlightUtil {
    /**
     * Suppress default constructor.
     */
    private SpotlightUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * The prefix for the content deliver manager folder which contains the spotlight image files.
     */
    public static final String contentDeliveryManager = "Microsoft.Windows.ContentDeliveryManager_";

    /**
     * The default content delivery manager suffix
     */
    @SuppressWarnings("unused")
    public static final String CONTENT_DELIVERY_MANAGER_SUFFIX = "cw5n1h2txyewy";

    /**
     * Returns the windows spotlight directory. I'm not sure if it could change since
     * according to Google, it's statically set to "Microsoft.Windows.ContentDeliveryManager_"
     * with {@link #CONTENT_DELIVERY_MANAGER_SUFFIX} tacked onto the end.
     * To be safe however, this method exists.
     *
     * @return the name of the directory containing the Windows spotlight images
     */
    public static String getWindowsContentDeliveryManagerDir() {
        Preconditions.checkArgument(OSUtil.isWindows(), "Host OS is not an instance of Windows");

        File spotlightParent = new File(OSUtil.buildPath(
                OSUtil.C_COLON_SLASH, "users", OSUtil.getSystemUsername(),
                "AppData", "Local", "Packages"));

        File[] files = spotlightParent.listFiles();

        if (files == null || files.length == 0) {
            return null;
        }

        for (File possibleSpotlightDir : files) {
            if (possibleSpotlightDir.getName().contains(contentDeliveryManager)) {
                return possibleSpotlightDir.getName();
            }
        }

        return null;
    }

    /**
     * Wipes the windows spotlight directory. Windows will download new ones eventually.
     */
    public static void wipeSpotlights() {
        Preconditions.checkArgument(OSUtil.isWindows(), "Host OS is not Windows");

        if (getWindowsContentDeliveryManagerDir() != null) {
            try {
                File spotlightDirectory = getSpotlightsDirectory();

                File[] files = spotlightDirectory.listFiles();
                int length = files == null ? 0 : files.length;

                ConsoleFrame.INSTANCE.getInputHandler().println("Windows spotlight images wiped from directory: "
                        + getWindowsContentDeliveryManagerDir());
                ConsoleFrame.INSTANCE.getInputHandler().println("Spotights found: " + length);

                if (files != null && files.length > 0) {
                    for (File spotlight : files) {
                        OSUtil.deleteFile(spotlight);
                    }
                }

                files = spotlightDirectory.listFiles();
                length = files == null ? 0 : files.length;

                ConsoleFrame.INSTANCE.getInputHandler().println("Spotights left: " + length);
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
        String local = getWindowsContentDeliveryManagerDir();
        Preconditions.checkNotNull(local);

        return new File(OSUtil.buildPath(
                OSUtil.C_COLON_SLASH, "users", OSUtil.getSystemUsername(),
                "AppData", "Local", "Packages", local, "LocalState", "Assets"));
    }

    /**
     * Saves the Windows spotlights to the provided directory.
     *
     * @param saveDir the directory o save the files to
     */
    public static void saveSpotlights(File saveDir) {
        Preconditions.checkNotNull(saveDir);
        Preconditions.checkArgument(saveDir.isDirectory(), "Destination directory is not a folder");
        Preconditions.checkArgument(saveDir.exists(), "Destination directory does not exists");
        Preconditions.checkArgument(OSUtil.isWindows(), "Host OS is not Windows");

        try {

            if (getWindowsContentDeliveryManagerDir() != null) {
                int acc = 0;

                File[] files = getSpotlightsDirectory().listFiles();

                if (files == null || files.length == 0) {
                    return;
                }

                for (File spotlight : files) {
                    ImageIcon icon = new ImageIcon(spotlight.getAbsolutePath());

                    // skip small previews and the weird vertical ones
                    if (icon.getIconHeight() > icon.getIconWidth()
                            || icon.getIconWidth() < 600 || icon.getIconHeight() < 600) {
                        continue;
                    }

                    Files.copy(spotlight.toPath(), Paths.get(saveDir + OSUtil.FILE_SEP + acc + ".png"),
                            StandardCopyOption.REPLACE_EXISTING);
                    acc++;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}
