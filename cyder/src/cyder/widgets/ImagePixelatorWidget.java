package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.DynamicDirectory;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.ui.CyderTextField;
import cyder.user.UserFile;
import cyder.utils.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

@Vanilla
@CyderAuthor
public class ImagePixelatorWidget {
    private static ImageIcon displayIcon;
    private static ImageIcon originalIcon;
    private static File currentFile;
    private static JLabel previewLabel;
    private static CyderTextField integerField;

    /**
     * Suppress default constructor.
     */
    private ImagePixelatorWidget() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Widget(triggers = {"pixelatepicture", "pixelateimage", "pixelator"},
            description = "A simple image pixelator widget that transforms" +
                    " the image into an image depicted of the specified number of pixels")
    public static void showGui() {
        showGui(null);
    }

    public static void showGui(File startPNG) {
        CyderFrame pixelFrame = new CyderFrame(800, 800, CyderIcons.defaultBackground);
        pixelFrame.setTitle("Image Pixelator");

        CyderLabel pixelSize = new CyderLabel("Pixel Size");
        pixelSize.setFont(CyderFonts.defaultFontSmall.deriveFont(28f));
        int w = StringUtil.getMinWidth(pixelSize.getText(), pixelSize.getFont());
        int h = StringUtil.getMinHeight(pixelSize.getText(), pixelSize.getFont());
        pixelSize.setBounds(400 - w / 2, 30 + 20, w, h);
        pixelFrame.getContentPane().add(pixelSize);

        CyderButton chooseImage = new CyderButton("Choose Image");
        chooseImage.setToolTipText("PNGs");
        chooseImage.setBounds(50, 100, 200, 40);
        pixelFrame.getContentPane().add(chooseImage);
        chooseImage.addActionListener(e -> {
            try {
                CyderThreadRunner.submit(() -> {
                    try {
                        File temp = GetterUtil.getInstance().getFile(
                                new GetterUtil.Builder("Choose file to resize")
                                        .setRelativeTo(pixelFrame));

                        if (temp != null && FileUtil.isSupportedImageExtension(temp)) {
                            currentFile = temp;

                            displayIcon = checkImage(temp);
                            originalIcon = new ImageIcon(ImageIO.read(temp));
                            previewLabel.setIcon(displayIcon);
                            integerField.setCharLimit(String.valueOf(originalIcon.getIconWidth()).length());
                            previewLabel.revalidate();
                            pixelFrame.revalidate();
                            previewLabel.repaint();
                            pixelFrame.repaint();
                        } else if (temp != null && !FileUtil.isSupportedImageExtension(temp)) {
                            currentFile = null;
                            displayIcon = null;
                        }
                    } catch (Exception ex) {
                        ExceptionHandler.handle(ex);
                    }
                }, "wait thread for GetterUtil().getFile()");
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        });

        integerField = new CyderTextField(0);
        integerField.setKeyEventRegexMatcher("[0-9]*");
        integerField.setBounds(300, 100, 200, 40);
        integerField.setToolTipText("How many old pixels should be combined into a new pixel?");
        pixelFrame.getContentPane().add(integerField);
        integerField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    int pixelSize = -1;

                    if (integerField.getText() != null && !integerField.getText().isEmpty()) {
                        if (Integer.parseInt(integerField.getText()) == 0) {
                            pixelSize = 1;
                        } else {
                            pixelSize = Integer.parseInt(integerField.getText());
                        }
                    }

                    displayIcon = new ImageIcon(ImageUtil.pixelate(checkImageBi(currentFile), pixelSize));
                    previewLabel.setIcon(displayIcon);
                    previewLabel.revalidate();
                    pixelFrame.revalidate();
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                }
            }
        });

        CyderButton approveImage = new CyderButton("Approve Image");
        approveImage.setToolTipText("Saves to downloads folder");
        approveImage.setBounds(800 - 50 - 200, 100, 200, 40);
        pixelFrame.getContentPane().add(approveImage);
        approveImage.addActionListener(e -> {
            if (integerField.getText() != null && !integerField.getText().isEmpty()) {
                int pixel = Integer.parseInt(integerField.getText());

                if (pixel > 1) {
                    try {
                        BufferedImage saveImage = ImageUtil.pixelate(ImageIO.read(currentFile), pixel);
                        File saveFile = new File(OSUtil.buildPath(
                                DynamicDirectory.DYNAMIC_PATH, "users", ConsoleFrame.INSTANCE.getUUID(),
                                UserFile.FILES.getName(), FileUtil.getFilename(currentFile)
                                        + "_Pixelated_Pixel_Size_" + pixel + ".png"));

                        ImageIO.write(saveImage, "png", saveFile);

                        displayIcon = null;
                        originalIcon = null;
                        currentFile = null;

                        previewLabel.setIcon(null);

                        integerField.setText("");

                        pixelFrame.notify("Successfully saved pixelated image to your files/ directory");
                    } catch (Exception ex) {
                        ExceptionHandler.handle(ex);
                    }
                }
            }
        });

        previewLabel = new JLabel();
        previewLabel.setBounds(50, 170, 800 - 100, 610);
        previewLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        pixelFrame.getContentPane().add(previewLabel);

        pixelFrame.finalizeAndShow();

        if (startPNG != null && FileUtil.isSupportedImageExtension(startPNG)) {
            try {
                currentFile = startPNG;
                displayIcon = checkImage(startPNG);
                previewLabel.setIcon(displayIcon);
                previewLabel.revalidate();
                pixelFrame.revalidate();
                originalIcon = new ImageIcon(ImageIO.read(startPNG));
                integerField.setCharLimit(String.valueOf(originalIcon.getIconWidth()).length());
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
    }

    private static ImageIcon checkImage(File icon) {
        ImageIcon ret = null;

        try {
            BufferedImage resizedImg = new BufferedImage(700, 610, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resizedImg.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(ImageIO.read(icon), 0, 0, 700, 610, null);
            g2.dispose();

            ret = new ImageIcon(resizedImg);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    private static BufferedImage checkImageBi(File icon) {
        BufferedImage ret = null;

        try {
            BufferedImage resizedImg = new BufferedImage(700, 610, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resizedImg.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(ImageIO.read(icon), 0, 0, 700, 610, null);
            g2.dispose();

            ret = resizedImg;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    private double getAspectRatio(ImageIcon im) {
        return ((double) im.getIconWidth() / (double) im.getIconHeight());
    }
}
