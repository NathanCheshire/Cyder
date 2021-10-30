package cyder.widgets;

import cyder.handler.ErrorHandler;
import cyder.utilities.SystemUtil;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Spotlight {
    public static void saveSpotlights(File saveDir) {
        if (!saveDir.isDirectory()) {
            throw new IllegalArgumentException("Destination directory is not a folder");
         } else if (!saveDir.exists()) {
            throw new IllegalArgumentException("Destination directory does not exists");
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
