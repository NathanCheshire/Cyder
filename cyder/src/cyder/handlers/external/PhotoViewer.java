package cyder.handlers.external;

import com.google.common.base.Preconditions;
import cyder.builders.GetterBuilder;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderFrame;
import cyder.ui.CyderIconButton;
import cyder.utilities.FileUtil;
import cyder.utilities.GetterUtil;
import cyder.utilities.StringUtil;
import cyder.utilities.UserUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;

/**
 * A widget which displays the images supported by Cyder in a provided directory.
 */
public class PhotoViewer {
    /**
     * The list of valid image files in the current directory, not recursive.
     */
    private final LinkedList<File> validDirectoryImages = new LinkedList<>();

    /**
     * The starting directory/file.
     */
    private final File startDir;

    /**
     * The current index of the valid directory images list.
     */
    private int currentIndex;

    /**
     * The image frame.
     */
    private CyderFrame pictureFrame;

    /**
     * Returns a new instance of photo viewer with the provided starting directory.
     *
     * @param startDir the starting directory
     * @return a new instance of the PhotoViewer
     */
    public static PhotoViewer getInstance(File startDir) {
        return new PhotoViewer(startDir);
    }

    /**
     * Creates a new photo viewer object.
     *
     * @param startDir the starting directory of the photo viewer.
     *                 If a file is provided, the file's parent is
     *                 used as the starting directory
     */
    private PhotoViewer(File startDir) {
        Preconditions.checkNotNull(startDir);
        Preconditions.checkArgument(startDir.exists());

        if (startDir.isFile()) {
            this.startDir = startDir;
        } else {
            this.startDir = startDir.getParentFile();
        }
    }

    /**
     * Opens the instance of photo viewer.
     */
    public void showGui() {
        Logger.log(Logger.Tag.OBJECT_CREATION, this);

        refreshValidFiles();

        File currentImage = validDirectoryImages.get(0);

        if (startDir.isFile()) {
            for (File validDirectoryImage : validDirectoryImages) {
                if (validDirectoryImage.equals(startDir)) {
                    currentImage = validDirectoryImage;
                    break;
                }
            }
        }

        ImageIcon newImage;
        newImage = scaleImageIfNeeded(currentImage);

        pictureFrame = new CyderFrame(newImage.getIconWidth(), newImage.getIconHeight(), newImage);
        pictureFrame.setBackground(Color.BLACK);
        revalidateTitle(FileUtil.getFilename(currentImage.getName()));
        pictureFrame.setVisible(true);

        pictureFrame.finalizeAndShow();

        pictureFrame.setMenuEnabled(true);
        pictureFrame.addMenuItem("Rename", this::rename);

        ImageIcon nextIcon = new ImageIcon("static/pictures/icons/nextPicture1.png");
        ImageIcon nextIconHover = new ImageIcon("static/pictures/icons/nextPicture2.png");
        CyderIconButton next = new CyderIconButton("Next", nextIcon, nextIconHover, null);
        next.setSize(nextIcon.getIconWidth(), nextIconHover.getIconHeight());
        next.addActionListener(e -> transition(true));
        pictureFrame.getTopDragLabel().addButton(next, 0);

        ImageIcon lastIcon = new ImageIcon("static/pictures/icons/lastPicture1.png");
        ImageIcon lastIconHover = new ImageIcon("static/pictures/icons/lastPicture2.png");
        CyderIconButton last = new CyderIconButton("Last", lastIcon, lastIconHover, null);
        last.setSize(lastIcon.getIconWidth(), lastIcon.getIconHeight());
        last.addActionListener(e -> transition(false));
        pictureFrame.getTopDragLabel().addButton(last, 0);
    }

    /**
     * Refreshes the valid files list.
     */
    private void refreshValidFiles() {
        validDirectoryImages.clear();

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            if (files == null || files.length == 0) {
                return;
            }

            for (File f : files) {
                if (FileUtil.isSupportedImageExtension(f)) {
                    validDirectoryImages.add(f);
                }
            }
        } else {
            File parent = startDir.getParentFile();
            File[] neighbors = parent.listFiles();

            if (neighbors == null || neighbors.length == 0) {
                return;
            }

            for (File f : neighbors) {
                if (FileUtil.isSupportedImageExtension(f)) {
                    validDirectoryImages.add(f);
                }
            }
        }
    }

    /**
     * Transitions to a new image in the directory if more exist.
     *
     * @param forward whether to transition forwards.
     *                If false, the direction traversed is backwards.
     */
    private void transition(boolean forward) {
        refreshValidFiles();

        if (validDirectoryImages.size() <= 1)
            return;

        // change the index
        if (forward) {
            if (currentIndex + 1 < validDirectoryImages.size()) {
                currentIndex += 1;
            } else {
                currentIndex = 0;
            }
        } else {
            if (currentIndex - 1 >= 0) {
                currentIndex -= 1;
            } else {
                currentIndex = validDirectoryImages.size() - 1;
            }
        }

        Point center = pictureFrame.getCenterPointOnScreen();

        ImageIcon newImage = scaleImageIfNeeded(validDirectoryImages.get(currentIndex));
        pictureFrame.setSize(newImage.getIconWidth(), newImage.getIconHeight());
        pictureFrame.setBackground(newImage);

        pictureFrame.setLocation((int) (center.getX() - newImage.getIconWidth() / 2),
                (int) (center.getY() - newImage.getIconHeight() / 2));

        pictureFrame.refreshBackground();
        revalidateTitle(FileUtil.getFilename(validDirectoryImages.get(currentIndex).getName()));
    }

    /**
     * The maximum length of the photo viewer frame.
     */
    private static final int MAX_LEN = 800;

    /**
     * Returns a scaled image icon for the provided image
     * file if the image is bigger than MAX_LEN x MAX_LEN.
     *
     * @param imageFile the image file to process
     * @return the ImageIcon from the image file guaranteed to be no bigger than MAX_LEN x MAX_LEN
     */
    private ImageIcon scaleImageIfNeeded(File imageFile) {
        try {
            ImageIcon originalIcon = new ImageIcon(ImageIO.read(imageFile));
            BufferedImage bi = ImageIO.read(imageFile);

            int width = originalIcon.getIconWidth();
            int height = originalIcon.getIconHeight();

            if (width > height) {
                int scaledHeight = MAX_LEN * height / width;
                return new ImageIcon(bi.getScaledInstance(MAX_LEN, scaledHeight, Image.SCALE_SMOOTH));
            } else if (height > width) {
                int scaledWidth = MAX_LEN * width / height;
                return new ImageIcon(bi.getScaledInstance(scaledWidth, MAX_LEN, Image.SCALE_SMOOTH));
            } else {
                return new ImageIcon(bi.getScaledInstance(MAX_LEN, MAX_LEN, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new IllegalStateException("Could not generate ImageIcon at this time");
    }

    /**
     * Attempts to rename the current image file if not in use by the ConsoleFrame.
     */
    private void rename() {
        File currentRename = new File(validDirectoryImages.get(currentIndex).getAbsolutePath());
        File currentBackground = ConsoleFrame.INSTANCE
                .getCurrentBackground().referenceFile().getAbsoluteFile();

        if (currentRename.getAbsolutePath().equals(currentBackground.getAbsolutePath())) {
            pictureFrame.notify("Sorry, " + UserUtil.getCyderUser().getName()
                    + ", but you're not allowed to" + " rename the background you are currently using");
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
                    File oldName = new File(validDirectoryImages.get(currentIndex).getAbsolutePath());

                    String replaceOldName = FileUtil.getFilename(oldName);

                    File newName = new File(oldName.getAbsolutePath()
                            .replace(replaceOldName, name));
                    boolean renamed = oldName.renameTo(newName);

                    if (renamed) {
                        pictureFrame.notify("Successfully renamed to " + name);

                        refreshValidFiles();

                        // update index based on new name
                        for (int i = 0 ; i < validDirectoryImages.size() ; i++) {
                            if (FileUtil.getFilename(validDirectoryImages.get(i)).equals(name)) {
                                currentIndex = i;
                            }
                        }

                        revalidateTitle(name);

                        // invoke callback
                        if (onRenameCallback != null) {
                            onRenameCallback.run();
                        }
                    } else {
                        pictureFrame.notify("Could not rename at this time");
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "PhotoViewer Image Renamer: " + this);
    }

    /**
     * The callback to run whenever a photo is renamed.
     */
    private Runnable onRenameCallback;

    /**
     * Invokes the provided runnable whenever a file is renamed via this photo viewer instance.
     *
     * @param runnable the runnable to invoke.
     */
    public void setRenameCallback(Runnable runnable) {
        Preconditions.checkNotNull(runnable);
        onRenameCallback = runnable;
    }

    /**
     * Revalidates the frame title based on the provided name.
     *
     * @param title the title of the frame
     */
    public void revalidateTitle(String title) {
        try {
            BufferedImage bi = ImageIO.read(validDirectoryImages.get(currentIndex));
            pictureFrame.setTitle("[" + bi.getWidth() + "x" + bi.getHeight() + "] " + title);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            pictureFrame.setTitle(title);
        }
    }
}
