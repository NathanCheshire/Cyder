package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.consts.CyderStrings;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ErrorHandler;
import cyder.ui.*;
import cyder.utilities.GetterUtil;
import cyder.utilities.ImageUtil;
import cyder.utilities.StringUtil;
import cyder.utilities.SystemUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImagePixelatorWidget {
    private static ImageIcon displayIcon;
    private static ImageIcon originalIcon;
    private static File currentFile;
    private static JLabel previewLabel;
    private static CyderTextField integerField;

    private ImagePixelatorWidget() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    public static void showGUI(File startPNG) {
        CyderFrame pixelFrame = new CyderFrame(800,800, CyderImages.defaultBackground);
        pixelFrame.setTitle("Image Pixelator");

        CyderLabel pixelSize = new CyderLabel("Pixel Size");
        pixelSize.setFont(CyderFonts.defaultFontSmall.deriveFont(28f));
        int w = CyderFrame.getMinWidth(pixelSize.getText(), pixelSize.getFont());
        int h = CyderFrame.getMinHeight(pixelSize.getText(), pixelSize.getFont());
        pixelSize.setBounds(400 - w / 2, 30 + 20, w, h);
        pixelFrame.getContentPane().add(pixelSize);

        CyderButton chooseImage = new CyderButton("Choose Image");
        chooseImage.setToolTipText("PNGs");
        chooseImage.setBounds(50,100,200,40);
        pixelFrame.getContentPane().add(chooseImage);
        chooseImage.addActionListener(e -> {
            try {
                new Thread(() -> {
                    try {
                        File temp = new GetterUtil().getFile("Choose file to resize");

                        if (temp != null && temp.getName().toLowerCase().endsWith(".png")) {
                            currentFile = temp;

                            displayIcon = checkImage(temp);
                            originalIcon = new ImageIcon(ImageIO.read(temp));
                            previewLabel.setIcon(displayIcon);
                            integerField.setCharLimit(String.valueOf(originalIcon.getIconWidth()).length());
                            previewLabel.revalidate();
                            pixelFrame.revalidate();
                            previewLabel.repaint();
                            pixelFrame.repaint();
                        } else if (temp != null && !StringUtil.getExtension(temp).equalsIgnoreCase(".png")) {
                            currentFile = null;
                            displayIcon = null;
                        }
                    } catch (Exception ex) {
                        ErrorHandler.handle(ex);
                    }
                }, "wait thread for GetterUtil().getFile()").start();
            }

            catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        });

        integerField = new CyderTextField(0);
        integerField.setRegexMatcher("[0-9]*");
        integerField.setBounds(300,100,200,40);
        integerField.setToolTipText("How many old pixels should be combined into a new pixel?");
        pixelFrame.getContentPane().add(integerField);
        integerField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    int pixelSize = 1;

                    if (integerField.getText() != null && integerField.getText().length() > 0) {
                        if (Integer.parseInt(integerField.getText()) == 0) {
                            pixelSize = 1;
                        } else {
                            pixelSize = Integer.parseInt(integerField.getText());
                        }
                    }

                    displayIcon = new ImageIcon(ImageUtil.pixelate(checkImageBi(currentFile),pixelSize));
                    previewLabel.setIcon(displayIcon);
                    previewLabel.revalidate();
                    pixelFrame.revalidate();
                } catch (Exception ex) {
                    ErrorHandler.handle(ex);
                }
            }
        });

        CyderButton approveImage = new CyderButton("Approve Image");
        approveImage.setToolTipText("Saves to downloads folder");
        approveImage.setBounds(800 - 50 - 200,100,200,40);
        pixelFrame.getContentPane().add(approveImage);
        approveImage.addActionListener(e -> {
            if (integerField.getText() != null && integerField.getText().length() > 0) {
                int pixel = Integer.parseInt(integerField.getText());

                if (pixel > 1) {
                    try {
                        BufferedImage saveImage = ImageUtil.pixelate(ImageIO.read(currentFile), pixel);
                        String saveName = StringUtil.getFilename(currentFile) +
                                 "_Pixelated_Pixel_Size_" + pixel + ".png";
                        File saveFile = new File("c:/users/"
                                + SystemUtil.getWindowsUsername() + "/downloads/" + saveName);

                        ImageIO.write(saveImage, "png", saveFile);

                        displayIcon = null;
                        originalIcon = null;
                        currentFile = null;
                        previewLabel.setIcon(null);
                        integerField.setText("");

                        pixelFrame.notify("Successfully saved pixelated image to your downloads folder");
                    } catch (Exception ex) {
                        ErrorHandler.handle(ex);
                    }
                }
            }
        });

        previewLabel = new JLabel();
        previewLabel.setBounds(50,170, 800 - 100, 610);
        previewLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        pixelFrame.getContentPane().add(previewLabel);

        pixelFrame.setVisible(true);
        pixelFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());

        if (startPNG != null && StringUtil.getExtension(startPNG).equalsIgnoreCase(".png")) {
            try {
                currentFile = startPNG;
                displayIcon = checkImage(startPNG);
                previewLabel.setIcon(displayIcon);
                previewLabel.revalidate();
                pixelFrame.revalidate();
                originalIcon = new ImageIcon(ImageIO.read(startPNG));
                integerField.setCharLimit(String.valueOf(originalIcon.getIconWidth()).length());
            } catch (Exception e) {
                ErrorHandler.handle(e);
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
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
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
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }

    private double getAspectRatio(ImageIcon im) {
        return ((double) im.getIconWidth() / (double) im.getIconHeight());
    }
}
