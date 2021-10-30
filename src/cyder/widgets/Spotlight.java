package cyder.widgets;

import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;
import cyder.utilities.SystemUtil;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Spotlight {
    /**
     * Wipes the windows spotlight directory, windows will download new ones eventually
     */
    public static void wipe() {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            throw new IllegalArgumentException("Host OS is not windows");
        }

        File spotlightsDir = new File("C:/Users/" +
                SystemUtil.getWindowsUsername() + "/AppData/Local/Packages/" +
                "Microsoft.Windows.ContentDeliveryManager_cw5n1h2txyewy/LocalState/Assets");

        try {
            int filesFound = 0;

            for (File spotlight : spotlightsDir.listFiles()) {
                spotlight.delete();
                filesFound++;
            }

            ConsoleFrame.getConsoleFrame().getInputHandler().println("Found " + filesFound + " spotlights");
        } catch (Exception e) {
            ErrorHandler.handle(e);
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
                    "Microsoft.Windows.ContentDeliveryManager_cw5n1h2txyewy/LocalState/Assets");

            File[] spotlights = spotlightsDir.listFiles();

            for (int i = 0 ; i < spotlights.length ; i++) {
                ImageIcon icon = new ImageIcon(spotlights[i].getAbsolutePath());

                if (icon.getIconHeight() > icon.getIconWidth() || icon.getIconWidth() < 600 || icon.getIconHeight() < 600)
                    continue;

                Files.copy(spotlights[i].toPath(), Paths.get(saveDir + "/" + i + ".png"), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
