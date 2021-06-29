package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderStrings;
import cyder.ui.*;
import cyder.utilities.ImageUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ThumbnailStealer {
    public ThumbnailStealer() {
        CyderFrame uuidFrame = new CyderFrame(400,240, new ImageIcon(CyderStrings.DEFAULT_BACKGROUND_PATH));
        uuidFrame.setTitle("Thumbnail Stealer");
        uuidFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);

        CyderLabel label = new CyderLabel("Enter any valid YouTube UUID");
        label.setFont(label.getFont().deriveFont(22f));
        int labelWidth = CyderFrame.getMinWidth("Enter any valid YouTube UUID", label.getFont());
        label.setBounds(400 / 2 - labelWidth / 2, 60, labelWidth, 30);
        uuidFrame.add(label);

        CyderTextField inputField = new CyderTextField(30);
        inputField.setBounds(200 - labelWidth / 2, 100, labelWidth, 40);
        inputField.setToolTipText("Must be a valid UUID");
        uuidFrame.add(inputField);

        CyderButton stealButton = new CyderButton("Submit");
        stealButton.setBounds(200 - labelWidth / 2, 160, labelWidth, 40);
        uuidFrame.add(stealButton);
        stealButton.setToolTipText("View Thumbnail");
        stealButton.addActionListener(e -> {
            try {
                BufferedImage Thumbnail = ImageIO.read(new URL(
                        "https://img.youtube.com/vi/" + inputField.getText().trim() + "/maxresdefault.jpg"));
                Thumbnail = ImageUtil.resizeImage(Thumbnail, Thumbnail.getType(), Thumbnail.getWidth(), Thumbnail.getHeight());

                CyderFrame thumbnailFrame = new CyderFrame(Thumbnail.getWidth() + 10, Thumbnail.getHeight() + 60, new ImageIcon(Thumbnail));
                thumbnailFrame.setBackground(CyderColors.navy);
                thumbnailFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
                thumbnailFrame.setTitle(inputField.getText().trim());

                CyderButton addToBackgrounds = new CyderButton("Set as background");
                addToBackgrounds.setBounds(20, Thumbnail.getHeight() + 10, Thumbnail.getWidth() - 40, 40);
                addToBackgrounds.addActionListener(e1 -> {
                    String thumbnailURL = "https://img.youtube.com/vi/" + inputField.getText().trim() + "/maxresdefault.jpg";
                    try {
                        BufferedImage save = ImageIO.read(new URL(thumbnailURL));
                        File saveFile = new File("users/" + ConsoleFrame.getUUID()
                                + "/Backgrounds/" +inputField.getText().trim() + ".png");
                        ImageIO.write(save, "png", saveFile);
                        thumbnailFrame.notify("Successfully saved as a background file." +
                                " You may view this by switching the background or by typing \"prefs\" " +
                                "to view your profile settings.");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                thumbnailFrame.add(addToBackgrounds);

                thumbnailFrame.setVisible(true);
                thumbnailFrame.setLocationRelativeTo(uuidFrame);
                uuidFrame.dispose();
            }

            catch (Exception exc) {
                uuidFrame.notify("Invalid URL");
            }
        });

        uuidFrame.setVisible(true);
        uuidFrame.setLocationRelativeTo(null);
    }
}
