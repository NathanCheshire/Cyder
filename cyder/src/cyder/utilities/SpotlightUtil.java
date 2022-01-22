package cyder.utilities;

import cyder.consts.CyderStrings;
import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.ConsoleFrame;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class SpotlightUtil {
    private SpotlightUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * Gets the windows spotlight directory. I'm not sure if it could chane since according to Google
     * source it's staticly set at Microsoft.Windows.ContentDeliveryManager_cw5n1h2txyewy. To be safe, however
     * this method exists
     * @return the name of the directory containing the Windows spotlight images
     */
    public static String getWindowsContentDeliveryManagerDir() {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            throw new IllegalArgumentException("Host OS is not windows");
        }

        String ret = "";

        File spotlightsParentDir = new File("C:/Users/" + SystemUtil.getWindowsUsername() + "/AppData/Local/Packages");

        for (File possibleSpotlightDir : spotlightsParentDir.listFiles()) {
            if (possibleSpotlightDir.getName().contains("Microsoft.Windows.ContentDeliveryManager_")) {
                ret = possibleSpotlightDir.getName();
                break;
            }
        }

        return ret;
    }

    /**
     * Wipes the windows spotlight directory, windows will download new ones eventually
     */
    public static void wipe() {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            throw new IllegalArgumentException("Host OS is not windows");
        }

        File spotlightsDir = new File("C:/Users/" +
                SystemUtil.getWindowsUsername() + "/AppData/Local/Packages/" +
                getWindowsContentDeliveryManagerDir() + "/LocalState/Assets");

        try {
            int filesFound = 0;

            for (File spotlight : spotlightsDir.listFiles()) {
                spotlight.delete();
                filesFound++;
            }

            int filesLeft = 0;

            for (File spotlight : spotlightsDir.listFiles()) {
                filesLeft++;
            }

            ConsoleFrame.getConsoleFrame().getInputHandler().println("Found " + filesFound + " " +
                    StringUtil.getPlural(filesFound, "spotlight") + "\nDeleted " + filesLeft +
                    " " + StringUtil.getPlural(filesLeft, "spotlight"));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public static void saveSpotlights(File saveDir) {
        if (!saveDir.isDirectory()) {
            throw new IllegalArgumentException("Destination directory is not a folder");
        } else if (!saveDir.exists()) {
            throw new IllegalArgumentException("Destination directory does not exists");
        } else if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            throw new IllegalArgumentException("Host OS is not windows");
        }

        try {
            File spotlightsDir = new File("C:/Users/" +
                    SystemUtil.getWindowsUsername() + "/AppData/Local/Packages/" +
                    getWindowsContentDeliveryManagerDir() + "/LocalState/Assets");

            File[] spotlights = spotlightsDir.listFiles();

            for (int i = 0 ; i < spotlights.length ; i++) {
                ImageIcon icon = new ImageIcon(spotlights[i].getAbsolutePath());

                if (icon.getIconHeight() > icon.getIconWidth() || icon.getIconWidth() < 600 || icon.getIconHeight() < 600)
                    continue;

                Files.copy(spotlights[i].toPath(), Paths.get(saveDir + "/" + i + ".png"), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}
