package com.cyder.handler;

import AppPackage.AnimationClass;
import com.cyder.ui.CyderButton;
import com.cyder.ui.DragLabel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;

//todo add scroll animation and instead of close button make it a change name button that opens a ui
//todo add DragLabel to panel instead of using clos button

public class PhotoViewer {

    private LinkedList<File> validImages = new LinkedList<>();
    private File startDir;
    private int currentIndex;

    private AnimationClass ac = new AnimationClass();

    private JFrame renameFrame;
    private DragLabel dl;
    private Util imageUtil = new Util();
    private JFrame pictureFrame;
    private CyderButton rename;
    private CyderButton close;
    private JPanel parentPanel;

    private JLabel pictureLabel;

    private int xMouse;
    private int yMouse;

    private CyderButton nextImage;
    private CyderButton lastImage;

    public PhotoViewer(File startDir) {
        this.startDir = startDir;
    }

    public void start() {
        initFiles();

        File ImageName = validImages.get(currentIndex);

        if (pictureFrame != null) {
            imageUtil.closeAnimation(pictureFrame);
            pictureFrame.dispose();
        }

        BufferedImage Image = null;

        try {
            Image = ImageIO.read(ImageName);
        }

        catch (Exception ex) {
            imageUtil.handle(ex);
        }

        pictureFrame = new JFrame();

        pictureFrame.setUndecorated(true);

        pictureFrame.setTitle(ImageName.getName().replace(".png", ""));

        pictureFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        parentPanel = new JPanel();

        parentPanel.setBorder(new LineBorder(imageUtil.navy,8,false));

        parentPanel.setLayout(new BorderLayout());

        pictureLabel = new JLabel(checkImage(ImageName));

        pictureLabel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();

                if (pictureFrame.isVisible() && pictureFrame != null) {
                    pictureFrame.setLocation(x - xMouse,y - yMouse);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                xMouse = e.getX();
                yMouse = e.getY();
            }
        });

        parentPanel.add(pictureLabel, BorderLayout.PAGE_START);

        JPanel buttonPanel = new JPanel();

        buttonPanel.setLayout(new GridLayout(1,4,5,5));

        lastImage = new CyderButton("Last");

        lastImage.setColors(imageUtil.regularRed);

        lastImage.setBorder(new LineBorder(imageUtil.navy,5,false));

        lastImage.setFocusPainted(false);

        lastImage.setBackground(imageUtil.regularRed);

        lastImage.setFont(imageUtil.weatherFontSmall);

        lastImage.addActionListener(e -> scrollBack());

        buttonPanel.add(lastImage);

        rename = new CyderButton("Rename");

        rename.setColors(imageUtil.regularRed);

        rename.setBorder(new LineBorder(imageUtil.navy,5,false));

        rename.setFocusPainted(false);

        rename.setBackground(imageUtil.regularRed);

        rename.setFont(imageUtil.weatherFontSmall);

        rename.addActionListener(e -> {
            if (renameFrame != null) {
                imageUtil.closeAnimation(renameFrame);
                renameFrame.dispose();
            }

            renameFrame = new JFrame();

            renameFrame.setIconImage(imageUtil.getCyderIcon().getImage());

            renameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            renameFrame.setTitle("Rename " + validImages.get(currentIndex).getName().replace(".png",""));

            JPanel pan = new JPanel();

            pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));

            JTextField renameField = new JTextField(20);

            renameField.setFont(imageUtil.weatherFontSmall);

            renameField.setForeground(imageUtil.navy);

            renameField.setBorder(new LineBorder(imageUtil.navy,5,false));

            CyderButton attemptRen = new CyderButton("Rename");

            attemptRen.setBackground(imageUtil.regularRed);

            attemptRen.setColors(imageUtil.regularRed);

            renameField.addActionListener(e1 -> attemptRen.doClick());

            JPanel a = new JPanel();

            a.add(renameField, SwingConstants.CENTER);

            pan.add(a);

            attemptRen.setBorder(new LineBorder(imageUtil.navy,5,false));

            attemptRen.setFont(imageUtil.weatherFontSmall);

            attemptRen.setForeground(imageUtil.navy);

            attemptRen.addActionListener(e12 -> {
                File oldName = new File(validImages.get(currentIndex).getAbsolutePath());
                File newName = new File(validImages.get(currentIndex).getAbsolutePath().replace(validImages.get(currentIndex).getName().replace(".png",""),renameField.getText()));
                oldName.renameTo(newName);
                imageUtil.inform("Successfully renamed to " + renameField.getText(),"",400,300);
                imageUtil.closeAnimation(renameFrame);
                renameFrame.dispose();
            });

            JPanel b = new JPanel();

            b.add(attemptRen, SwingConstants.CENTER);

            pan.add(b);

            pan.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            renameFrame.add(pan);

            renameFrame.pack();

            renameFrame.setVisible(true);

            renameFrame.setLocationRelativeTo(null);
        });

        buttonPanel.add(rename);

        close = new CyderButton("Close");

        close.setColors(imageUtil.regularRed);

        close.setBorder(new LineBorder(imageUtil.navy,5,false));

        close.setFocusPainted(false);

        close.setBackground(imageUtil.regularRed);

        close.setFont(imageUtil.weatherFontSmall);

        close.addActionListener(e -> {
            imageUtil.closeAnimation(pictureFrame);
            pictureFrame.dispose();
        });

        buttonPanel.add(close);

        nextImage = new CyderButton("Next");

        nextImage.setColors(imageUtil.regularRed);

        nextImage.setBorder(new LineBorder(imageUtil.navy,5,false));

        nextImage.setFocusPainted(false);

        nextImage.setBackground(imageUtil.regularRed);

        nextImage.setFont(imageUtil.weatherFontSmall);

        nextImage.addActionListener(e -> scrollFoward());

        buttonPanel.add(nextImage);

        buttonPanel.setBackground(imageUtil.navy);

        parentPanel.add(buttonPanel,BorderLayout.PAGE_END);

        pictureFrame.add(parentPanel);

        pictureLabel.repaint();

        parentPanel.repaint();

        pictureFrame.repaint();

        pictureFrame.pack();

        pictureFrame.setVisible(true);

        pictureFrame.setLocationRelativeTo(null);

        pictureFrame.setResizable(false);

        pictureFrame.setIconImage(imageUtil.getCyderIcon().getImage());
    }

    private void initFiles() {
        File[] possibles = null;

        if (startDir.isDirectory()) {
            possibles = startDir.listFiles();
        }

        else {
            possibles = startDir.getParentFile().listFiles();
        }



        for (File f : possibles) {
            if (f.getName().endsWith(".png"))
                validImages.add(f);
        }

        for (int i = 0 ; i < validImages.size() ; i++) {
           if (validImages.get(i).getAbsolutePath().equalsIgnoreCase(startDir.getAbsolutePath())) {
               currentIndex = i;
           }
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int type, int img_width, int img_height) {
        BufferedImage resizedImage = new BufferedImage(img_width, img_height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, img_width, img_height, null);
        g.dispose();

        return resizedImage;
    }

    private void scrollFoward() {
        try {
            if (currentIndex + 1 < validImages.size()) {
                currentIndex += 1;
                ImageIcon next = checkImage(validImages.get(currentIndex));

                pictureLabel.setIcon(next);
                pictureLabel.repaint();
                parentPanel.repaint();
                pictureFrame.pack();
                pictureFrame.revalidate();
                pictureFrame.setTitle(validImages.get(currentIndex).getName().replace(".png", ""));
                pictureFrame.setLocationRelativeTo(null);
            }
        }

        catch (Exception e) {
            imageUtil.handle(e);
        }
    }

    private void scrollBack() {
        try {
            if (currentIndex - 1 >= 0) {
                currentIndex--;
                ImageIcon last = checkImage(validImages.get(currentIndex));
                pictureLabel.setIcon(last);
                pictureLabel.repaint();
                parentPanel.repaint();
                pictureFrame.pack();
                pictureFrame.revalidate();
                pictureFrame.setTitle(validImages.get(currentIndex).getName().replace(".png", ""));
                pictureFrame.setLocationRelativeTo(null);
            }
        }

        catch (Exception e) {
            imageUtil.handle(e);
        }
    }

    private ImageIcon checkImage(File im) {
        try {
            Dimension dim = imageUtil.getScreenSize();
            double screenX = dim.getWidth();
            double screenY = dim.getHeight();

            double aspectRatio = getAspectRatio(new ImageIcon(ImageIO.read(im)));
            ImageIcon originalIcon = new ImageIcon(ImageIO.read(im));
            BufferedImage bi = ImageIO.read(im);

            int width = originalIcon.getIconWidth();
            int height = originalIcon.getIconHeight();


            while (width + 400 > screenX || height + 400 > screenY) {
                width = (int) (width / aspectRatio);
                height = (int) (height / aspectRatio);
            }

            return new ImageIcon(bi.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        }

        catch (Exception e) {
            imageUtil.handle(e);
        }

        return null;
    }

    private double getAspectRatio(ImageIcon im) {
        return ((double) im.getIconWidth() / (double) im.getIconHeight());
    }
}
