package cyder.handlers.external;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.genesis.CyderCommon;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.utilities.*;
import cyder.utilities.objects.GetterBuilder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;

@SuppressWarnings("FieldNotUsedInToString") /* why is this necessary? */
public class PhotoViewer {
    private final LinkedList<File> validImages = new LinkedList<>();
    private final File startDir;
    private int currentIndex;

    private CyderFrame pictureFrame;

    private final JButton nextImage;
    private final JButton lastImage;
    private final JButton renameButton;

    private int oldCenterX;
    private int oldCenterY;

    //start instance
    public PhotoViewer(File startDir) {
        this.startDir = startDir;
        initFiles();

        File currentImage = validImages.get(currentIndex);

        if (pictureFrame != null)
            pictureFrame.dispose();

        ImageIcon newImage;
        newImage = checkImage(currentImage);

        pictureFrame = new CyderFrame(newImage.getIconWidth(), newImage.getIconHeight(), newImage);
        pictureFrame.setBackground(CyderColors.guiThemeColor);
        pictureFrame.setTitle(FileUtil.getFilename(currentImage.getName()));
        pictureFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);
        pictureFrame.initializeResizing();
        pictureFrame.setResizable(true);
        pictureFrame.setSnapSize(new Dimension(1,1));
        pictureFrame.setMinimumSize(new Dimension(newImage.getIconWidth() / 2, newImage.getIconHeight() / 2));
        pictureFrame.setMaximumSize(new Dimension(newImage.getIconWidth(), newImage.getIconHeight()));
        pictureFrame.setVisible(true);

        pictureFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());

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
        nextImage.addActionListener(e -> scrollForward());
        nextImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                nextImage.setIcon(new ImageIcon("static/pictures/icons/nextPicture2.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nextImage.setIcon(new ImageIcon("static/pictures/icons/nextPicture1.png"));
            }
        });

        nextImage.setIcon(new ImageIcon("static/pictures/icons/nextPicture1.png"));
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
                lastImage.setIcon(new ImageIcon("static/pictures/icons/lastPicture2.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lastImage.setIcon(new ImageIcon("static/pictures/icons/lastPicture1.png"));
            }
        });

        lastImage.setIcon(new ImageIcon("static/pictures/icons/lastPicture1.png"));
        lastImage.setContentAreaFilled(false);
        lastImage.setBorderPainted(false);
        lastImage.setFocusPainted(false);
        pictureFrame.getTopDragLabel().addButton(lastImage, 0);
    }

    private void initFiles() {
        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            for (File f : files) {
                if (FileUtil.isSupportedImageExtension(f))
                    validImages.add(f);
            }
        } else {
            File parent = startDir.getParentFile();
            File[] neighbors = parent.listFiles();

            for (File f : neighbors) {
                if (FileUtil.isSupportedImageExtension(f))
                    validImages.add(f);
            }

            for (int i = 0 ; i < validImages.size() ; i++) {
                if (validImages.get(i).equals(startDir)) {
                    currentIndex = i;
                    break;
                }
            }
        }
    }
    private void scrollForward() {
        oldCenterX = pictureFrame.getX() + pictureFrame.getWidth() / 2;
        oldCenterY = pictureFrame.getY() + pictureFrame.getHeight() / 2;

        try {
            if (currentIndex + 1 < validImages.size()) {
                currentIndex += 1;
            } else {
                currentIndex = 0;
            }

            ImageIcon nextImage = checkImage(validImages.get(currentIndex));
            pictureFrame.setSize(nextImage.getIconWidth(),nextImage.getIconHeight());
            pictureFrame.setBackground(nextImage);
            pictureFrame.setLocation(oldCenterX - nextImage.getIconWidth() / 2,
                    oldCenterY - nextImage.getIconHeight() / 2);

            pictureFrame.setMinimumSize(new Dimension(nextImage.getIconWidth() / 2,
                    nextImage.getIconHeight() / 2));
            pictureFrame.setMaximumSize(new Dimension(nextImage.getIconWidth(),
                    nextImage.getIconHeight()));

            pictureFrame.refreshBackground();
            pictureFrame.setTitle(FileUtil.getFilename(validImages.get(currentIndex).getName()));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    private void scrollBack() {
        oldCenterX = pictureFrame.getX() + pictureFrame.getWidth() / 2;
        oldCenterY = pictureFrame.getY() + pictureFrame.getHeight() / 2;

        try {
            if (currentIndex - 1 >= 0) {
                currentIndex -= 1;
            } else {
                currentIndex = validImages.size() - 1;
            }

            ImageIcon previousImage = checkImage(validImages.get(currentIndex));
            pictureFrame.setSize(previousImage.getIconWidth(),previousImage.getIconHeight());
            pictureFrame.setBackground(previousImage);
            pictureFrame.setLocation(oldCenterX - previousImage.getIconWidth() / 2,
                    oldCenterY - previousImage.getIconHeight() / 2);

            pictureFrame.setMinimumSize(new Dimension(previousImage.getIconWidth() / 2,
                    previousImage.getIconHeight() / 2));
            pictureFrame.setMaximumSize(new Dimension(previousImage.getIconWidth(),
                    previousImage.getIconHeight()));

            pictureFrame.refreshBackground();
            pictureFrame.setTitle(FileUtil.getFilename(validImages.get(currentIndex).getName()));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    //returns a scaled down ImageIcon if the image file is too big
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
            ExceptionHandler.handle(e);
        }

        throw new IllegalStateException("Could not generate ImageIcon at this time");
    }

    private void rename() {
        File currentRename = new File(validImages.get(currentIndex).getAbsolutePath());
        File currentBackground = ConsoleFrame.getConsoleFrame()
                .getCurrentBackground().getReferenceFile().getAbsoluteFile();

        if (currentRename.getAbsolutePath().equals(currentBackground.getAbsolutePath())) {
           pictureFrame.notify("Sorry, " + UserUtil.getCyderUser().getName() + ", but you're not allowed to" +
                    " rename the background you are currently using");
            return;
        }

        CyderThreadRunner.submit(() -> {
           try {
               GetterBuilder builder = new GetterBuilder("Rename");
               builder.setRelativeTo(pictureFrame);
               builder.setFieldTooltip("Valid filename");
               builder.setSubmitButtonText("Rename");
               String name = GetterUtil.getInstance().getString(builder);
               if (!StringUtil.isNull(name)) {
                   File oldName = new File(validImages.get(currentIndex).getAbsolutePath());

                   String replaceOldName = FileUtil.getFilename(oldName);

                   File newName = new File(oldName.getAbsolutePath()
                           .replace(replaceOldName, name));
                   //noinspection ResultOfMethodCallIgnored
                   oldName.renameTo(newName);
                   pictureFrame.notify("Successfully renamed to " + name);

                   initFiles();

                   for (int i = 0 ; i < validImages.size() ; i++) {
                       if (validImages.get(i).getName().equals(name)) {
                           currentIndex = i;
                       }
                   }

                   pictureFrame.setTitle(name);
               }
           } catch (Exception e) {
               ExceptionHandler.handle(e);
           }
       }, "wait thread for GetterUtil().getString() " + this);
    }

    @Override
    public final String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
