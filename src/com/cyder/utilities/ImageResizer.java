package com.cyder.utilities;

import com.cyder.ui.CyderButton;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImageResizer {

    ImageIcon selected = new ImageIcon("src\\com\\cyder\\io\\pictures\\checkbox1.png");
    ImageIcon notSelected = new ImageIcon("src\\com\\cyder\\io\\pictures\\checkbox2.png");

    private JTextField xdim;
    private JTextField ydim;

    private JLabel previewLabel;
    private JLabel maintainAspectRatioLab;

    private Util imageUtil = new Util();
    private File resizeImage;

    private boolean maintainAspectRatio = true;

    public ImageResizer() {
        JFrame resizeFrame = new JFrame();
        resizeFrame.setResizable(false);
        resizeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        resizeFrame.setIconImage(imageUtil.getCyderIcon().getImage());

        JPanel parentPanel = new JPanel();
        parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.Y_AXIS));
        parentPanel.setBorder(new CompoundBorder(new LineBorder(imageUtil.navy,5,false),
                BorderFactory.createEmptyBorder(10,10,10,10)));

        CyderButton chooseFile = new CyderButton("Choose Image");
        chooseFile.setFont(imageUtil.weatherFontSmall);
        chooseFile.setForeground(imageUtil.navy);
        chooseFile.setBackground(imageUtil.regularRed);
        chooseFile.setColors(imageUtil.regularRed);
        chooseFile.addActionListener(e -> {
            try {
                File temp = imageUtil.getFile();

                if (temp != null && temp.getName().endsWith(".png")) {
                    resizeImage = temp;
                    ImageIcon im = checkImage(resizeImage);
                    previewLabel.setPreferredSize(new Dimension(im.getIconWidth(), im.getIconHeight()));
                    previewLabel.setIcon(im);
                    xdim.setText(String.valueOf(im.getIconWidth()));
                    ydim.setText(String.valueOf(im.getIconHeight()));
                    parentPanel.revalidate();
                    parentPanel.repaint();
                    resizeFrame.revalidate();
                    resizeFrame.repaint();
                    resizeFrame.pack();
                    resizeFrame.setLocationRelativeTo(null);
                }

                if (temp != null && !Files.probeContentType(Paths.get(resizeImage.getAbsolutePath())).endsWith("png")) {
                    resizeImage = null;
                }
            }

            catch (Exception ex) {
                imageUtil.handle(ex);
            }
        });

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2,3,10,20));

        JLabel xdimLabel = new JLabel("x pixels");
        xdimLabel.setForeground(imageUtil.navy);
        xdimLabel.setFont(imageUtil.weatherFontSmall);
        JPanel xcent = new JPanel();
        xcent.add(xdimLabel);
        centerPanel.add(xcent);

        chooseFile.setFocusPainted(false);
        centerPanel.add(chooseFile);

        JLabel ydimLabel = new JLabel("y pixels");
        ydimLabel.setForeground(imageUtil.navy);
        ydimLabel.setFont(imageUtil.weatherFontSmall);
        JPanel ycent = new JPanel();
        ycent.add(ydimLabel);
        centerPanel.add(ycent);

        xdim = new JTextField(5);
        xdim.setFont(imageUtil.weatherFontSmall);
        xdim.setBorder(new LineBorder(imageUtil.navy,5,false));
        xdim.setForeground(imageUtil.navy);
        xdim.setSelectionColor(imageUtil.selectionColor);
        xdim.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                //todo limit to only numeric digits
                //todo update fields based off of aspect ratio of the one just edited
            }
        });

        centerPanel.add(xdim);

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
                    //todo update jtextfields based off of last edited one
                }
            }
        });

        centerPanel.add(maintainAspectRatioLab);

        ydim = new JTextField(5);
        ydim.setFont(imageUtil.weatherFontSmall);
        ydim.setBorder(new LineBorder(imageUtil.navy,5,false));
        ydim.setForeground(imageUtil.navy);
        ydim.setSelectionColor(imageUtil.selectionColor);
        ydim.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                //todo limit to only numeric digits
                //todo update fields based off of aspect ratio of the one just edited
                //use key pressed for consumption if it's not right try that
            }
        });

        centerPanel.add(ydim);
        parentPanel.add(centerPanel);

        JLabel originalImage = new JLabel("Original Image");
        originalImage.setFont(imageUtil.weatherFontSmall);
        originalImage.setForeground(imageUtil.navy);
        JPanel origLabelPanel = new JPanel();
        origLabelPanel.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
        origLabelPanel.add(originalImage);
        parentPanel.add(origLabelPanel);

        previewLabel = new JLabel("");
        previewLabel.setBorder(new LineBorder(imageUtil.navy,5,false));
        previewLabel.setPreferredSize(new Dimension(100,100));
        JPanel previewPanel = new JPanel();
        previewPanel.add(previewLabel);
        parentPanel.add(previewPanel);

        CyderButton approve = new CyderButton("Approve Image");
        approve.setForeground(imageUtil.navy);
        approve.setFont(imageUtil.weatherFontSmall);
        approve.setBackground(imageUtil.regularRed);
        approve.setColors(imageUtil.regularRed);
        approve.setFocusPainted(false);
        approve.addActionListener(e -> {
            //todo replace resize image with newImage if it exists from preview
        });
        JPanel approvePanel = new JPanel();
        approvePanel.setBorder(BorderFactory.createEmptyBorder(30,10,0,10));
        approvePanel.add(approve);
        parentPanel.add(approvePanel);

        resizeFrame.add(parentPanel);
        resizeFrame.pack();
        resizeFrame.revalidate();
        resizeFrame.setVisible(true);
        resizeFrame.setTitle("Image Resizer");
        resizeFrame.setLocationRelativeTo(null);
    }

    private double getAspectRatio(ImageIcon im) {
        return ((double) im.getIconWidth() / (double) im.getIconHeight());
    }

    private ImageIcon checkImage(File im) {
        try {
            Dimension dim = imageUtil.getScreenSize();
            double screenX = dim.getWidth();
            double screenY = dim.getHeight();

            double aspectRatio = getAspectRatio(new ImageIcon(ImageIO.read(im)));
            aspectRatio = (aspectRatio == 1.0 ? 2.0 : aspectRatio);
            ImageIcon originalIcon = new ImageIcon(ImageIO.read(im));
            BufferedImage bi = ImageIO.read(im);

            int width = originalIcon.getIconWidth();
            int height = originalIcon.getIconHeight();

            if (width > 800 || height > 800) {
                aspectRatio = (aspectRatio < 1 ? 1/aspectRatio : aspectRatio);
                while (width > 800 || height > 800) {
                    width = (int) (width / aspectRatio);
                    height = (int) (height / aspectRatio);
                }
            }

            else {
                aspectRatio = (aspectRatio < 1 ? 1/aspectRatio : aspectRatio);
                while (width  < 400 || height < 400) {
                    width = (int) (width * aspectRatio);
                    height = (int) (height * aspectRatio);
                }
            }

            return new ImageIcon(bi.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        }

        catch (Exception e) {
            imageUtil.handle(e);
        }

        return null;
    }
}
