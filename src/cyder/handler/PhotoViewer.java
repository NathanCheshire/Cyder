package cyder.handler;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.utilities.GetterUtil;
import cyder.utilities.StringUtil;
import cyder.widgets.GenericInform;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class PhotoViewer {
    private LinkedList<File> validImages = new LinkedList<>();
    private File startDir;
    private int currentIndex;

    private CyderFrame pictureFrame;

    private JButton nextImage;
    private JButton lastImage;
    private JButton renameButton;

    private int oldCenterX;
    private int oldCenterY;

    //start instance
    public PhotoViewer(File startDir) {
        this.startDir = startDir;
    }

    public void start() {
        initFiles();

        File imageName = validImages.get(currentIndex);

        if (pictureFrame != null)
            pictureFrame.closeAnimation();

        ImageIcon newImage = null;
        try {
            newImage = new ImageIcon(ImageIO.read(imageName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        pictureFrame = new CyderFrame(newImage.getIconWidth(), newImage.getIconHeight(), newImage);
        pictureFrame.setBackground(CyderColors.navy);
        pictureFrame.setTitle(imageName.getName().replace(".png", ""));
        pictureFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);
        pictureFrame.initializeResizing();
        pictureFrame.setResizable(true);
        pictureFrame.setBackgroundResizing(true);
        pictureFrame.setSnapSize(new Dimension(1,1));
        pictureFrame.setMinimumSize(new Dimension(newImage.getIconWidth() / 2, newImage.getIconHeight() / 2));
        pictureFrame.setMaximumSize(new Dimension(newImage.getIconWidth(), newImage.getIconHeight()));

        renameButton = new JButton("Rename");
        renameButton.setForeground(CyderColors.vanila);
        renameButton.setFont(CyderFonts.defaultFontSmall);
        renameButton.setToolTipText("Rename image");
        renameButton.addActionListener(e -> rename());
        renameButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                renameButton.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                renameButton.setForeground(CyderColors.vanila);
            }
        });

        renameButton.setContentAreaFilled(false);
        renameButton.setBorderPainted(false);
        renameButton.setFocusPainted(false);
        pictureFrame.getTopDragLabel().addButton(renameButton, 0);

        nextImage = new JButton("");
        nextImage.setToolTipText("Next image");
        nextImage.addActionListener(e -> scrollFoward());
        nextImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                nextImage.setIcon(new ImageIcon("sys/pictures/icons/nextPicture2.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nextImage.setIcon(new ImageIcon("sys/pictures/icons/nextPicture1.png"));
            }
        });

        nextImage.setIcon(new ImageIcon("sys/pictures/icons/nextPicture1.png"));
        nextImage.setContentAreaFilled(false);
        nextImage.setBorderPainted(false);
        nextImage.setFocusPainted(false);
        pictureFrame.getTopDragLabel().addButton(nextImage, 1);

        lastImage = new JButton("");
        lastImage.setToolTipText("Previous image");
        lastImage.addActionListener(e -> scrollBack());
        lastImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                lastImage.setIcon(new ImageIcon("sys/pictures/icons/lastPicture2.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lastImage.setIcon(new ImageIcon("sys/pictures/icons/lastPicture1.png"));
            }
        });

        lastImage.setIcon(new ImageIcon("sys/pictures/icons/lastPicture1.png"));
        lastImage.setContentAreaFilled(false);
        lastImage.setBorderPainted(false);
        lastImage.setFocusPainted(false);
        pictureFrame.getTopDragLabel().addButton(lastImage, 0);

        pictureFrame.setVisible(true);
        pictureFrame.setLocationRelativeTo(null);
    }

    private void initFiles() {
        validImages.clear();

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
    private void scrollFoward() {
        oldCenterX = pictureFrame.getX() + pictureFrame.getWidth() / 2;
        oldCenterY = pictureFrame.getY() + pictureFrame.getHeight() / 2;

        try {
            if (currentIndex + 1 < validImages.size()) {
                currentIndex += 1;
            } else {
                currentIndex = 0;
            }

            pictureFrame.setBackground(new ImageIcon(validImages.get(currentIndex).getAbsolutePath()));
            pictureFrame.setLocation(oldCenterX - pictureFrame.getWidth() / 2, oldCenterY - pictureFrame.getHeight() / 2);

        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void scrollBack() {

    }

    //returns a scaled down imageicon if the image file is too big
    private ImageIcon checkImage(File im) {
        try {
            ImageIcon originalIcon = new ImageIcon(ImageIO.read(im));
            BufferedImage bi = ImageIO.read(im);
            int width = originalIcon.getIconWidth();
            int height = originalIcon.getIconHeight();

            if (width > height) {
                int scaledHeight = 800 * height / width;
                return new ImageIcon(bi.getScaledInstance(800, scaledHeight, Image.SCALE_SMOOTH));
            } else if (height > width) {
                int scaledWidth = 800 * width / height;
                return new ImageIcon(bi.getScaledInstance(scaledWidth, 800, Image.SCALE_SMOOTH));
            } else {
                return new ImageIcon(bi.getScaledInstance(800, 800, Image.SCALE_SMOOTH));
            }
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return null;
    }

    private double getAspectRatio(ImageIcon im) {
        return ((double) im.getIconWidth() / (double) im.getIconHeight());
    }

    private void rename() {
        File currentRename = new File(validImages.get(currentIndex).getAbsolutePath());
        File currentBackground = ConsoleFrame.getCurrentBackgroundFile().getAbsoluteFile();

        if (currentRename.getAbsolutePath().equals( currentBackground.getAbsolutePath())) {
           pictureFrame.notify("Sorry, " + ConsoleFrame.getUsername() + ", but you're not allowed to" +
                    " rename the background you are currently using");
            return;
        }

        new Thread(() -> {
           try {
               String name = new GetterUtil().getString("Rename","Valid filename","Rename");
               if (name != null && name.length() > 0 && !name.equals("NULL")) {
                   File oldName = new File(validImages.get(currentIndex).getAbsolutePath());
                   File newName = new File(validImages.get(currentIndex).getAbsolutePath().replace(validImages.get(currentIndex).getName().replace(".png",""),name));
                   oldName.renameTo(newName);
                   GenericInform.inform("Successfully renamed to " + name,"");

                   initFiles();

                   for (int i = 0 ; i < validImages.size() ; i++) {
                       if (validImages.get(i).getName().equals(name)) {
                           currentIndex = i;
                       }
                   }

                   pictureFrame.setTitle(name);
               }
           } catch (Exception e) {
               ErrorHandler.handle(e);
           }
       }, "wait thread for GetterUtil().getString() " + this).start();
    }

    @Override
    public String toString() {
        return "PhotoViewer object[" + validImages.get(currentIndex).getAbsolutePath() + "], hash=" + this.hashCode();
    }
}
