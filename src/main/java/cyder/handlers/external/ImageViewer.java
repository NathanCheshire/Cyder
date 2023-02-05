package cyder.handlers.external;

import com.google.common.base.Preconditions;
import cyder.console.Console;
import cyder.files.DirectoryWatcher;
import cyder.files.FileUtil;
import cyder.files.WatchDirectoryEvent;
import cyder.files.WatchDirectorySubscriber;
import cyder.getter.GetInputBuilder;
import cyder.getter.GetterUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadFactory;
import cyder.threads.CyderThreadRunner;
import cyder.ui.drag.button.LeftButton;
import cyder.ui.drag.button.RightButton;
import cyder.ui.frame.CyderFrame;
import cyder.ui.frame.TitlePosition;
import cyder.user.UserDataManager;
import cyder.utils.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A widget which displays the images supported by Cyder in a provided directory.
 */
public class ImageViewer {
    /**
     * The next keyword.
     */
    private static final String NEXT = "Next";

    /**
     * The last keyword.
     */
    private static final String LAST = "Last";

    /**
     * The rename keyword.
     */
    private static final String RENAME = "Rename";

    /**
     * The maximum length of the image viewer frame.
     */
    private static final int maxFrameLength = 800;

    /**
     * The maximum dimension of the image viewer frame.
     */
    private static final Dimension maxFrameSize = new Dimension(maxFrameLength, maxFrameLength);

    /**
     * The list of valid image files in the current directory, not recursive.
     */
    private final ArrayList<File> validDirectoryImages = new ArrayList<>();

    /**
     * The watcher for the image directory.
     */
    private final DirectoryWatcher imageDirectoryWatcher;

    /**
     * The starting directory/file.
     */
    private final File imageDirectory;

    /**
     * The current index of the valid directory images list.
     */
    private int currentIndex;

    /**
     * The image frame.
     */
    private CyderFrame pictureFrame;

    /**
     * The next image button.
     */
    private RightButton nextButton;

    /**
     * The last image button.
     */
    private LeftButton lastButton;

    /**
     * Returns a new instance with the provided starting directory.
     *
     * @param imageDirectoryOrFile the image directory or an image file.
     *                             If a file is provided, the file's parent is used as the directory
     * @return a new instance
     */
    public static ImageViewer getInstance(File imageDirectoryOrFile) {
        return new ImageViewer(imageDirectoryOrFile);
    }

    /**
     * Creates and returns a new instance.
     *
     * @param imageDirectoryOrFile the image directory or an image file.
     *                             If a file is provided, the file's parent is used as the directory
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist
     */
    private ImageViewer(File imageDirectoryOrFile) {
        Preconditions.checkNotNull(imageDirectoryOrFile);
        Preconditions.checkArgument(imageDirectoryOrFile.exists());

        this.imageDirectory = imageDirectoryOrFile;

        File watchDirectory = imageDirectoryOrFile.isFile()
                ? imageDirectoryOrFile.getParentFile() : imageDirectoryOrFile;
        this.imageDirectoryWatcher = new DirectoryWatcher(watchDirectory);

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Opens the instance of.
     *
     * @return whether the gui opened the image successfully
     */
    public Future<Boolean> showGui() {
        return Executors.newSingleThreadExecutor(generateThreadFactory()).submit(() -> {
            refreshValidFiles();

            File currentImage = validDirectoryImages.get(0);

            if (imageDirectory.isFile()) {
                for (File validDirectoryImage : validDirectoryImages) {
                    if (validDirectoryImage.equals(imageDirectory)) {
                        currentImage = validDirectoryImage;
                        break;
                    }
                }
            }

            ImageIcon newImage = scaleImageIfNeeded(currentImage);

            pictureFrame = new CyderFrame(newImage.getIconWidth(), newImage.getIconHeight(), newImage);
            pictureFrame.setBackground(Color.BLACK);
            pictureFrame.setTitlePosition(TitlePosition.CENTER);
            revalidateTitle(FileUtil.getFilename(currentImage.getName()));
            pictureFrame.setVisible(true);
            pictureFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    imageDirectoryWatcher.stopWatching();
                }
            });

            pictureFrame.finalizeAndShow();

            pictureFrame.setMenuEnabled(true);
            pictureFrame.addMenuItem(RENAME, this::rename);

            nextButton = new RightButton();
            nextButton.setToolTipText(NEXT);
            nextButton.setClickAction(() -> transition(true));
            pictureFrame.getTopDragLabel().addRightButton(nextButton, 0);

            lastButton = new LeftButton();
            lastButton.setToolTipText(LAST);
            lastButton.setClickAction(() -> transition(false));
            pictureFrame.getTopDragLabel().addRightButton(lastButton, 0);

            revalidateNavigationButtonVisibility();
            startDirectoryWatcher();

            return true;
        });
    }

    /**
     * Generates and returns a new {@link CyderThreadFactory} for the loading {@link Executor}.
     *
     * @return a new {@link CyderThreadFactory} for the loading {@link Executor}
     */
    private CyderThreadFactory generateThreadFactory() {
        return new CyderThreadFactory("ImageViewer showGui thread, initial directory: " + imageDirectory);
    }

    /**
     * Refreshes the {@link #validDirectoryImages} list.
     */
    private void refreshValidFiles() {
        validDirectoryImages.clear();

        File[] neighbors = imageDirectory.isDirectory()
                ? imageDirectory.listFiles()
                : imageDirectory.getParentFile().listFiles();
        if (neighbors == null || neighbors.length == 0) return;
        Arrays.stream(neighbors).filter(FileUtil::isSupportedImageExtension).forEach(validDirectoryImages::add);
    }

    /**
     * Transitions to a new image in the directory if more exist.
     *
     * @param forward whether to transition forwards.
     *                If false, the direction traversed is backwards
     */
    private void transition(boolean forward) {
        refreshValidFiles();

        if (validDirectoryImages.size() <= 1) return;

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
     * Returns a scaled image icon for the provided image
     * file if the image is bigger than MAX_LEN x MAX_LEN.
     *
     * @param imageFile the image file to process
     * @return the ImageIcon from the image file guaranteed to be no bigger than MAX_LEN x MAX_LEN
     */
    private ImageIcon scaleImageIfNeeded(File imageFile) {
        try {
            BufferedImage bufferedImage = ImageUtil.read(imageFile);
            bufferedImage = ImageUtil.ensureFitsInBounds(bufferedImage, maxFrameSize);
            return ImageUtil.toImageIcon(bufferedImage);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new IllegalStateException("Could not generate ImageIcon for file: " + imageFile.getAbsolutePath());
    }

    /**
     * Attempts to rename the current image file if not in use by the Console.
     */
    private void rename() {
        File currentRename = new File(validDirectoryImages.get(currentIndex).getAbsolutePath());
        File currentBackground = Console.INSTANCE
                .getCurrentBackground().getReferenceFile().getAbsoluteFile();

        if (currentRename.getAbsolutePath().equals(currentBackground.getAbsolutePath())) {
            pictureFrame.notify("Sorry, " + UserDataManager.INSTANCE.getUsername()
                    + ", but you're not allowed to" + " rename the background you are currently using");
            return;
        }

        CyderThreadRunner.submit(() -> {
            try {
                Optional<String> optionalName = GetterUtil.getInstance().getInput(
                        new GetInputBuilder("Rename", "New filename for " + CyderStrings.quote
                                + validDirectoryImages.get(currentIndex).getName() + CyderStrings.quote)
                                .setRelativeTo(pictureFrame)
                                .setInitialFieldText(FileUtil.getFilename(validDirectoryImages.get(currentIndex)))
                                .setSubmitButtonText("Rename"));
                if (optionalName.isEmpty()) {
                    pictureFrame.notify("File not renamed");
                    return;
                }
                String name = optionalName.get();

                File oldName = new File(validDirectoryImages.get(currentIndex).getAbsolutePath());
                String replaceOldName = FileUtil.getFilename(oldName);
                File newName = new File(oldName.getAbsolutePath().replace(replaceOldName, name));

                if (oldName.renameTo(newName)) {
                    pictureFrame.notify("Successfully renamed to \"" + name + CyderStrings.quote);

                    refreshValidFiles();

                    // Update index based on new name
                    for (int i = 0 ; i < validDirectoryImages.size() ; i++) {
                        if (FileUtil.getFilename(validDirectoryImages.get(i)).equals(name)) {
                            currentIndex = i;
                        }
                    }

                    revalidateTitle(name);
                } else {
                    pictureFrame.notify("Could not rename at this time");
                }

            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "ImageViewer File Renamer");
    }

    /**
     * Revalidates the frame title based on the provided name.
     *
     * @param title the title of the frame
     */
    public void revalidateTitle(String title) {
        title = title.trim();

        try {
            BufferedImage bi = ImageUtil.read(validDirectoryImages.get(currentIndex));
            pictureFrame.setTitle(title + CyderStrings.space + CyderStrings.openingBracket
                    + bi.getWidth() + "x" + bi.getHeight() + CyderStrings.closingBracket);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            pictureFrame.setTitle(title);
        }
    }

    /**
     * Starts the directory watcher to update the visibilities of the
     * next and last buttons based on the contents of the image directory.
     */
    private void startDirectoryWatcher() {
        WatchDirectorySubscriber subscriber = new WatchDirectorySubscriber() {
            @Override
            public void onEvent(DirectoryWatcher broker, WatchDirectoryEvent event, File eventFile) {
                refreshValidFiles();
                revalidateNavigationButtonVisibility();
            }
        };
        subscriber.subscribeTo(WatchDirectoryEvent.FILE_ADDED,
                WatchDirectoryEvent.FILE_DELETED, WatchDirectoryEvent.FILE_MODIFIED);
        imageDirectoryWatcher.addSubscriber(subscriber);
        imageDirectoryWatcher.startWatching();
    }

    /**
     * Revalidates the visibility of the navigation buttons.
     */
    private void revalidateNavigationButtonVisibility() {
        refreshValidFiles();
        setNavigationButtonsVisibility(validDirectoryImages.size() > 1);
    }

    /**
     * Sets the visibility of the navigation buttons.
     *
     * @param visible the visibility of the navigation buttons
     */
    private void setNavigationButtonsVisibility(boolean visible) {
        nextButton.setVisible(visible);
        lastButton.setVisible(visible);
    }
}
