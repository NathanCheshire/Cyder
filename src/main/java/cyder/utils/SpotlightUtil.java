package main.java.cyder.utils;

import com.google.common.base.Preconditions;
import main.java.cyder.console.Console;
import main.java.cyder.enums.Extension;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.handlers.internal.ExceptionHandler;
import main.java.cyder.strings.CyderStrings;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

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
     * The users directory string.
     */
    private static final String USERS = "users";

    /**
     * The app data directory string.
     */
    private static final String APP_DATA = "AppData";

    /**
     * The local directory string.
     */
    private static final String LOCAL = "Local";

    /**
     * The packages directory string.
     */
    private static final String PACKAGES = "Packages";

    /**
     * The local state directory string.
     */
    private static final String LOCAL_STATE = "LocalState";

    /**
     * The assets directory string.
     */
    private static final String ASSETS = "Assets";

    /**
     * The minimum length each dimension of a spotlight must have in order
     * to be saved by the {@link #saveSpotlights(File)} method.
     */
    public static final int minimumSpotlightImageLength = 600;

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

                Console.INSTANCE.getInputHandler().println("Windows spotlight images wiped from directory:"
                        + CyderStrings.newline + CyderStrings.quote
                        + spotlightsDir.getAbsolutePath() + CyderStrings.quote);
                Console.INSTANCE.getInputHandler().println("Spotlights found: " + length);

                if (files != null && files.length > 0) {
                    Arrays.stream(files).forEach(OsUtil::deleteFile);
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
        return OsUtil.buildFile(OsUtil.WINDOWS_ROOT,
                USERS,
                OsUtil.getOsUsername(),
                APP_DATA,
                LOCAL,
                PACKAGES,
                CONTENT_DELIVERY_MANAGER_PREFIX + CONTENT_DELIVERY_MANAGER_SUFFIX,
                LOCAL_STATE,
                ASSETS);
    }

    /**
     * Saves the Windows spotlights to the provided directory.
     *
     * @param saveDir the directory o save the files to
     */
    public static void saveSpotlights(File saveDir) {
        Preconditions.checkState(OsUtil.isWindows());
        Preconditions.checkNotNull(saveDir);
        Preconditions.checkArgument(saveDir.exists());
        Preconditions.checkArgument(saveDir.isDirectory());

        try {
            File[] files = getSpotlightsDirectory().listFiles();

            if (files == null || files.length == 0) return;

            int spotlightIndex = 0;

            for (File spotlight : files) {
                ImageIcon icon = new ImageIcon(spotlight.getAbsolutePath());

                if (ImageUtil.isPortraitIcon(icon)) continue;
                if (icon.getIconWidth() < minimumSpotlightImageLength) continue;
                if (icon.getIconHeight() < minimumSpotlightImageLength) continue;

                String savePath = OsUtil.buildPath(saveDir.getAbsolutePath(),
                        spotlightIndex + Extension.PNG.getExtension());

                try {
                    Files.copy(spotlight.toPath(), Paths.get(savePath), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    ExceptionHandler.handle(e);
                }

                spotlightIndex++;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}
