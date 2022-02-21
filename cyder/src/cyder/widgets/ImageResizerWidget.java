package cyder.widgets;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.genesis.CyderCommon;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderButton;
import cyder.ui.CyderCheckbox;
import cyder.ui.CyderFrame;
import cyder.ui.CyderTextField;
import cyder.utilities.FileUtil;
import cyder.utilities.GetterUtil;
import cyder.utilities.ScreenUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageResizerWidget {

    private static CyderTextField xdim;
    private static CyderTextField ydim;

    private static double aspectRatio;

    private static JLabel previewLabel;

    private static File resizeImage;

    private static boolean leftLastEdited;
    private static boolean maintainAspectRatio = true;

    private ImageResizerWidget() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    @Widget(triggers = {"resizepictures", "resizeimages", "resize", "resizeiamge", "resizepicture"},
            description = "An image resizing widget to resize images")
    public static void showGUI() {
        Logger.log(Logger.Tag.WIDGET_OPENED, "IMAGE RESIZER");

        CyderFrame resizeFrame = new CyderFrame(800,800, CyderIcons.defaultBackground);
        resizeFrame.setTitle("Image Resizer");

        previewLabel = new JLabel();

        CyderButton chooseFile = new CyderButton("Choose Image");
        chooseFile.setFont(CyderFonts.segoe20);
        chooseFile.setForeground(CyderColors.navy);
        chooseFile.setBackground(CyderColors.regularRed);
        chooseFile.addActionListener(e -> {
            try {
                CyderThreadRunner.submit(() -> {
                  try {
                      File temp = new GetterUtil().getFile("Choose file to resize");

                      if (temp != null && FileUtil.isSupportedImageExtension(temp)) {
                          resizeImage = temp;

                          //add preview label with picture to frame
                          ImageIcon prevIcon = checkImage(temp);
                          previewLabel.setIcon(prevIcon);
                          previewLabel.setBorder(new LineBorder(CyderColors.navy, 3, false));
                          previewLabel.setBounds(400 - prevIcon.getIconWidth() / 2,450 - prevIcon.getIconHeight() / 2,prevIcon.getIconWidth(),prevIcon.getIconHeight());
                          resizeFrame.getContentPane().add(previewLabel);
                          resizeFrame.revalidate();
                          resizeFrame.repaint();

                          ImageIcon dimIcon = new ImageIcon(ImageIO.read(resizeImage));
                          xdim.setText(String.valueOf(dimIcon.getIconWidth()));
                          ydim.setText(String.valueOf(dimIcon.getIconHeight()));
                      }

                      if (temp != null &&
                              !FileUtil.isSupportedImageExtension(resizeImage)) {
                          resizeImage = null;
                      }
                  } catch (Exception ex) {
                      ExceptionHandler.handle(ex);
                  }
                }, "wait thread for GetterUtil().getFile()");
            }

            catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        });

        JLabel xdimLabel = new JLabel("x pixels");
        xdimLabel.setForeground(CyderColors.navy);
        xdimLabel.setFont(CyderFonts.segoe20);

        xdimLabel.setBounds(130,40, 100, 40);
        resizeFrame.getContentPane().add(xdimLabel);

        chooseFile.setBounds(400 - 180 / 2,40, 180, 40);
        resizeFrame.getContentPane().add(chooseFile);

        JLabel ydimLabel = new JLabel("y pixels");
        ydimLabel.setForeground(CyderColors.navy);
        ydimLabel.setFont(CyderFonts.segoe20);

        ydimLabel.setBounds(800 - 130 - 90,40, 100, 40);
        resizeFrame.getContentPane().add(ydimLabel);

        xdim = new CyderTextField(0);
        xdim.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
            leftLastEdited = true;
            int key = evt.getKeyCode();

            if (xdim.getText().length() > 4 || (!(key >= 48 && key <= 57) && !(key >= 37 && key <= 40) && !(key >= 96 && key <= 105) && key != 8 && key != 46)) {
                xdim.setText(xdim.getText().substring(0,xdim.getText().length() - 1));
                Toolkit.getDefaultToolkit().beep();
            }

            else if (maintainAspectRatio){
                if (xdim.getText().length() > 0) {
                    ydim.setText(String.valueOf(Math.round(Integer.parseInt(xdim.getText()) * 1.0 / aspectRatio)));
                }

                else {
                    ydim.setText("");
                }
            }
            }
        });

        xdim.setBounds(125,100, 100, 40);
        resizeFrame.getContentPane().add(xdim);

        CyderCheckbox maintainAspectRatioLab = new CyderCheckbox();
        maintainAspectRatioLab.setToolTipText("Maintain Aspect Ratio");
        maintainAspectRatioLab.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                maintainAspectRatio = !maintainAspectRatio;

                if (maintainAspectRatio) {
                    if (leftLastEdited) {
                        if (xdim.getText().length() > 0) {
                            ydim.setText(String.valueOf(Math.round(Integer.parseInt(xdim.getText()) * 1.0 / aspectRatio)));
                        }

                        else {
                            ydim.setText("");
                        }
                    }

                    else {
                        if (ydim.getText().length() > 0) {
                            xdim.setText(String.valueOf(Math.round(Integer.parseInt(ydim.getText()) * aspectRatio)));
                        }

                        else {
                            xdim.setText("");
                        }
                    }
                }
            }
        });

        maintainAspectRatioLab.setBounds(375,90, 50, 50);
        maintainAspectRatioLab.setSelected();
        resizeFrame.getContentPane().add(maintainAspectRatioLab);

        ydim = new CyderTextField(0);
        ydim.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
            leftLastEdited = false;
            int key = evt.getKeyCode();

            if (ydim.getText().length() > 4 || (!(key >= 48 && key <= 57) && !(key >= 37 && key <= 40) && !(key >= 96 && key <= 105) && key != 8 && key != 46)) {
                ydim.setText(ydim.getText().substring(0,ydim.getText().length() - 1));
                Toolkit.getDefaultToolkit().beep();
            }

            else if (maintainAspectRatio){
                if (ydim.getText().length() > 0) {
                    xdim.setText(String.valueOf(Math.round(Integer.parseInt(ydim.getText()) * aspectRatio)));
                }

                else {
                    xdim.setText("");
                }
            }
            }
        });

        ydim.setBounds(575,100, 100, 40);
        resizeFrame.getContentPane().add(ydim);

        JLabel originalImage = new JLabel("Preview Image");
        originalImage.setFont(CyderFonts.segoe20);
        originalImage.setForeground(CyderColors.navy);

        originalImage.setBounds(400 - 165 / 2,130, 180, 40);
        resizeFrame.getContentPane().add(originalImage);

        CyderButton approve = new CyderButton("Approve Image");
        approve.setForeground(CyderColors.navy);
        approve.setFont(CyderFonts.segoe20);
        approve.setBackground(CyderColors.regularRed);
        approve.setFocusPainted(false);
        approve.addActionListener(e -> {
            if (resizeImage == null) {
                resizeFrame.notify("Sorry, but you have no image selected to resize");
            }

            else if (xdim.getText().length() > 0 && ydim.getText().length() > 0) {
                try {
                    BufferedImage replace = resizeImage(resizeImage, Integer.parseInt(xdim.getText()), Integer.parseInt(ydim.getText()));
                    ImageIO.write(replace, "png", resizeImage);
                    resizeFrame.notify("The image \"" + resizeImage.getName() + "\" was successfully resized to " +
                            xdim.getText() + "x" + ydim.getText());
                }

                catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                }
            }
        });

        approve.setBounds(400 - 180 / 2,745, 180, 40);
        resizeFrame.getContentPane().add(approve);

        resizeFrame.setVisible(true);
        resizeFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }

    private static double getAspectRatio(ImageIcon im) {
        return ((double) im.getIconWidth() / (double) im.getIconHeight());
    }

    private static ImageIcon checkImage(File im) {
        try {
            double screenX = ScreenUtil.getScreenWidth();
            double screenY = ScreenUtil.getScreenHeight();

            aspectRatio = getAspectRatio(new ImageIcon(ImageIO.read(im)));

            ImageIcon originalIcon = new ImageIcon(ImageIO.read(im));
            BufferedImage bi = ImageIO.read(im);

            int width = originalIcon.getIconWidth();
            int height = originalIcon.getIconHeight();

            if (width > height) {
                width = 600;
                height = (int) (600 * 1.0 / aspectRatio);
            }

            else {
                height = 600;
                width = (int) (600 *  aspectRatio);
            }

            return new ImageIcon(bi.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        }

        catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return null;
    }

    private static BufferedImage resizeImage(File originalImage, int img_width, int img_height) {
        BufferedImage resizedImage = null;

        try {
            BufferedImage bi = ImageIO.read(originalImage);
            resizedImage = new BufferedImage(img_width, img_height, bi.getType());
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(bi, 0, 0, img_width, img_height, null);
            g.dispose();
        }

        catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return resizedImage;
    }
}
