package cyder.widgets;

import cyder.annotations.Widget;
import cyder.consts.CyderFonts;
import cyder.genesis.GenesisShare;
import cyder.handler.ErrorHandler;
import cyder.ui.*;
import cyder.utilities.SystemUtil;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Spotlight {

    @Widget("spotlight")
    public static void showGUI() {
        CyderFrame spotlightFrame = new CyderFrame(400,250);
        spotlightFrame.setTitle("Spotlight Stealer");

        CyderLabel label = new CyderLabel("Save directory");
        label.setFont(CyderFonts.weatherFontSmall);
        label.setBounds(100,50,200,40);
        spotlightFrame.add(label);

        CyderButton stealButton = new CyderButton("Steal");

        CyderTextField outputDirField = new CyderTextField(0);
        outputDirField.setToolTipText("Directory to save the images to");
        outputDirField.addActionListener(e -> stealButton.doClick());
        outputDirField.setBounds(100,100,200,40);
        spotlightFrame.getContentPane().add(outputDirField);

        stealButton.addActionListener(e -> {
            if (outputDirField.getText().trim().length() > 0) {
                String text = outputDirField.getText().trim();
                File saveDir = new File(text);

                if (saveDir.isDirectory()) {
                    saveSpotlights(saveDir);
                    spotlightFrame.notify("Spotlight images saved to: " + saveDir.getPath());
                    ConsoleFrame.getConsoleFrame().resizeBackgrounds();
                } else {
                    spotlightFrame.notify("Chosen save location is not a directory");
                }

                outputDirField.setText("");
            }
        });
        stealButton.setBounds(100,150, 200, 40);
        spotlightFrame.getContentPane().add(stealButton);

        spotlightFrame.setVisible(true);
       spotlightFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

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
