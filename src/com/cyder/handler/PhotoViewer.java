package com.cyder.handler;

import AppPackage.AnimationClass;
import com.cyder.ui.CyderButton;
import com.cyder.ui.DragLabel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;

//todo add scroll animation and instead of close button make it a change name button that opens a ui
//todo add DragLabel to panel instead of using close button
//todo scroll button to side arrows that trigger the animation
//todo change title on drag label

public class PhotoViewer {

    private LinkedList<File> validImages = new LinkedList<>();
    private File startDir;
    private int currentIndex;

    private AnimationClass ac = new AnimationClass();

    private Util imageUtil = new Util();

    private JFrame renameFrame;
    private DragLabel dl;
    private JFrame pictureFrame;
    private JPanel parentPanel;

    private JLabel pictureLabel;

    private CyderButton nextImage;
    private CyderButton lastImage;
    private JLabel rename;

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
        } catch (Exception ex) {
            imageUtil.handle(ex);
        }

        pictureFrame = new JFrame();
        pictureFrame.setUndecorated(true);
        pictureFrame.setTitle(ImageName.getName().replace(".png", ""));
        pictureFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        parentPanel = new JPanel();
        parentPanel.setLayout(null);
        pictureFrame.setContentPane(parentPanel);

        ImageIcon size = checkImage(ImageName);
        pictureLabel = new JLabel(size);

        parentPanel.setSize(size.getIconWidth(),size.getIconHeight());
        parentPanel.setBorder(new LineBorder(imageUtil.navy,8,false));
        parentPanel.add(pictureLabel);

        dl = new DragLabel(parentPanel.getWidth(), 30, pictureFrame);
        dl.setBounds(0,0, parentPanel.getWidth(), 30);
        parentPanel.add(dl);

        //todo add next and last buttons to side of picturelabel

        rename = new JLabel("Rename");
        rename.setForeground(imageUtil.vanila);
        rename.setFont(imageUtil.weatherFontSmall);
        rename.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
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
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                rename.setForeground(imageUtil.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rename.setForeground(imageUtil.vanila);
            }
        });

        rename.setBounds(parentPanel.getWidth() / 2 - 40, 5, 150, 20);
        dl.add(rename);

        pictureFrame.setSize(size.getIconWidth(),size.getIconHeight());
        pictureFrame.setVisible(true);
        pictureFrame.setLocationRelativeTo(null);
        pictureFrame.setIconImage(imageUtil.getCyderIcon().getImage());
    }

    private void initFiles() {
        File[] possibles = null;

        if (startDir.isDirectory()) {
            possibles = startDir.listFiles();
        } else {
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
                Dimension d = new Dimension(next.getIconWidth(), next.getIconHeight());

                pictureLabel.setSize(d);
                pictureLabel.setIcon(next);
                pictureLabel.repaint();

                parentPanel.setSize(d);
                parentPanel.repaint();

                pictureFrame.setSize(d);
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
                Dimension d = new Dimension(last.getIconWidth(), last.getIconHeight());

                pictureLabel.setSize(d);
                pictureLabel.setIcon(last);
                pictureLabel.repaint();

                parentPanel.setSize(d);
                parentPanel.repaint();

                pictureFrame.setSize(d);
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
