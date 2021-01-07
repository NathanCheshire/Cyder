package com.cyder.widgets;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.handler.ErrorHandler;
import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;
import com.cyder.utilities.IOUtil;
import com.cyder.utilities.SystemUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImageResizer {

    ImageIcon selected = new ImageIcon("src/com/cyder/sys/pictures/checkbox1.png");
    ImageIcon notSelected = new ImageIcon("src/com/cyder/sys/pictures/checkbox2.png");

    private JTextField xdim;
    private JTextField ydim;

    private double aspectRatio;

    private JLabel previewLabel;
    private JLabel maintainAspectRatioLab;

    private File resizeImage;

    private boolean leftLastEdited;
    private boolean maintainAspectRatio = true;

    public ImageResizer() {
        CyderFrame resizeFrame = new CyderFrame(800,800,new ImageIcon("src/com/cyder/sys/pictures/DebugBackground.png"));
        resizeFrame.setTitle("Image Resizer");

        previewLabel = new JLabel();

        CyderButton chooseFile = new CyderButton("Choose Image");
        chooseFile.setFont(CyderFonts.weatherFontSmall);
        chooseFile.setForeground(CyderColors.navy);
        chooseFile.setBackground(CyderColors.regularRed);
        chooseFile.setColors(CyderColors.regularRed);
        chooseFile.addActionListener(e -> {
            try {
                File temp = IOUtil.getFile();

                if (temp != null && temp.getName().endsWith(".png")) {
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

                if (temp != null && !Files.probeContentType(Paths.get(resizeImage.getAbsolutePath())).endsWith("png")) {
                    resizeImage = null;
                }
            }

            catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        });

        JLabel xdimLabel = new JLabel("x pixels");
        xdimLabel.setForeground(CyderColors.navy);
        xdimLabel.setFont(CyderFonts.weatherFontSmall);

        xdimLabel.setBounds(130,40, 100, 40);
        resizeFrame.getContentPane().add(xdimLabel);

        chooseFile.setBounds(400 - 180 / 2,40, 180, 40);
        resizeFrame.getContentPane().add(chooseFile);

        JLabel ydimLabel = new JLabel("y pixels");
        ydimLabel.setForeground(CyderColors.navy);
        ydimLabel.setFont(CyderFonts.weatherFontSmall);

        ydimLabel.setBounds(800 - 130 - 90,40, 100, 40);
        resizeFrame.getContentPane().add(ydimLabel);

        xdim = new JTextField(5);
        xdim.setFont(CyderFonts.weatherFontSmall);
        xdim.setBorder(new LineBorder(CyderColors.navy,5,false));
        xdim.setForeground(CyderColors.navy);
        xdim.setSelectionColor(CyderColors.selectionColor);
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
                    ydim.setText(Math.round(Integer.parseInt(xdim.getText()) * 1.0 / aspectRatio) + "");
                }

                else {
                    ydim.setText("");
                }
            }
            }
        });

        xdim.setBounds(125,100, 100, 40);
        resizeFrame.getContentPane().add(xdim);

        maintainAspectRatioLab = new JLabel();
        maintainAspectRatioLab.setToolTipText("Maintain Aspect Ratio");
        maintainAspectRatioLab.setHorizontalAlignment(JLabel.CENTER);
        maintainAspectRatioLab.setIcon(selected);
        maintainAspectRatioLab.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            if (maintainAspectRatio) {
                maintainAspectRatioLab.setIcon(notSelected);
                maintainAspectRatio = !maintainAspectRatio;
            }

            else {
                maintainAspectRatioLab.setIcon(selected);
                maintainAspectRatio = !maintainAspectRatio;

                if (leftLastEdited) {
                    if (xdim.getText().length() > 0) {
                        ydim.setText(Math.round(Integer.parseInt(xdim.getText()) * 1.0 / aspectRatio) + "");
                    }

                    else {
                        ydim.setText("");
                    }
                }

                else {
                    if (ydim.getText().length() > 0) {
                        xdim.setText(Math.round(Integer.parseInt(ydim.getText()) * aspectRatio) + "");
                    }

                    else {
                        xdim.setText("");
                    }
                }
            }
            }
        });

        maintainAspectRatioLab.setBounds(350,75, 100, 100);
        resizeFrame.getContentPane().add(maintainAspectRatioLab);

        ydim = new JTextField(5);
        ydim.setFont(CyderFonts.weatherFontSmall);
        ydim.setBorder(new LineBorder(CyderColors.navy,5,false));
        ydim.setForeground(CyderColors.navy);
        ydim.setSelectionColor(CyderColors.selectionColor);
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
                    xdim.setText(Math.round(Integer.parseInt(ydim.getText()) * aspectRatio) + "");
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
        originalImage.setFont(CyderFonts.weatherFontSmall);
        originalImage.setForeground(CyderColors.navy);

        originalImage.setBounds(400 - 165 / 2,150, 180, 40);
        resizeFrame.getContentPane().add(originalImage);

        CyderButton approve = new CyderButton("Approve Image");
        approve.setForeground(CyderColors.navy);
        approve.setFont(CyderFonts.weatherFontSmall);
        approve.setBackground(CyderColors.regularRed);
        approve.setColors(CyderColors.regularRed);
        approve.setFocusPainted(false);
        approve.addActionListener(e -> {
            if (resizeImage == null) {
                GenericInform.inform("Sorry, but you have no image selected to resize","Exception");
            }

            else if (xdim.getText().length() > 0 && ydim.getText().length() > 0) {
                try {
                    BufferedImage replace = resizeImage(resizeImage, Integer.parseInt(xdim.getText()), Integer.parseInt(ydim.getText()));
                    ImageIO.write(replace, "png", resizeImage);
                    GenericInform.inform("The image \"" + resizeImage.getName() + "\" was successfully resized to " +
                            xdim.getText() + "x" + ydim.getText(),"Success");
                    resizeFrame.closeAnimation();
                }

                catch (Exception ex) {
                    ErrorHandler.handle(ex);
                }
            }
        });

        approve.setBounds(400 - 180 / 2,735, 180, 40);
        resizeFrame.getContentPane().add(approve);

        resizeFrame.setVisible(true);
        resizeFrame.setLocationRelativeTo(null);
    }

    private double getAspectRatio(ImageIcon im) {
        return ((double) im.getIconWidth() / (double) im.getIconHeight());
    }

    private ImageIcon checkImage(File im) {
        try {
            Dimension dim = SystemUtil.getScreenSize();
            double screenX = dim.getWidth();
            double screenY = dim.getHeight();

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
            ErrorHandler.handle(e);
        }

        return null;
    }

    private BufferedImage resizeImage(File originalImage, int img_width, int img_height) {
        BufferedImage resizedImage = null;

        try {
            BufferedImage bi = ImageIO.read(originalImage);
            resizedImage = new BufferedImage(img_width, img_height, bi.getType());
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(bi, 0, 0, img_width, img_height, null);
            g.dispose();
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return resizedImage;
    }
}
