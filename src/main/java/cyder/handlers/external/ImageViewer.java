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
import cyder.threads.CyderThreadFactory;
import cyder.threads.CyderThreadRunner;
import cyder.ui.drag.button.LeftButton;
import cyder.ui.drag.button.RightButton;
import cyder.ui.frame.CyderFrame;
import cyder.ui.frame.enumerations.TitlePosition;
import cyder.user.UserDataManager;
import cyder.utils.ArrayUtil;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static cyder.strings.CyderStrings.*;

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
     * The getter util instance used to acquire the new filename from the user during a rename image attempt.
     */
    private final GetterUtil getterUtil = GetterUtil.getInstance();

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
     * Creates and returns a new instance.
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
     * Shows this ImageViewer instance.
     *
     * @return whether the the image was successfully loaded and opened
     */
    public Future<Boolean> showGui() {
        return Executors.newSingleThreadExecutor(generateThreadFactory()).submit(() -> {
            refreshImageFiles();

            File currentImage = getCurrentImageFile();

            ImageIcon newImage = scaleImageIfNeeded(currentImage);
            pictureFrame = new CyderFrame.Builder()
                    .setWidth(newImage.getIconWidth())
                    .setHeight(newImage.getIconHeight())
                    .setBackgroundIcon(newImage)
                    .setBackgroundColor(Color.BLACK)
                    .build();
            pictureFrame.setTitlePosition(TitlePosition.CENTER);
            revalidateTitle(FileUtil.getFilename(currentImage.getName()));
            pictureFrame.setVisible(true);
            pictureFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    imageDirectoryWatcher.stopWatching();
                }
            });

            pictureFrame.setMenuButtonShown(true);
            pictureFrame.addMenuItem(RENAME, this::onRenameButtonClicked);

            nextButton = new RightButton();
            nextButton.setToolTipText(NEXT);
            nextButton.setClickAction(this::transitionForward);
            pictureFrame.getTopDragLabel().addRightButton(nextButton, 0);

            lastButton = new LeftButton();
            lastButton.setToolTipText(LAST);
            lastButton.setClickAction(this::transitionBackward);
            pictureFrame.getTopDragLabel().addRightButton(lastButton, 0);

            revalidateNavigationButtonVisibility();
            startDirectoryWatcher();

            pictureFrame.finalizeAndShow();

            return true;
        });
    }

    /**
     * Returns a reference to the current image file if possible. The first image is returned otherwise.
     *
     * @return a reference to the current image file. The first image is returned otherwise
     */
    private File getCurrentImageFile() {
        AtomicReference<File> currentImage = new AtomicReference<>(validDirectoryImages.get(0));
        if (imageDirectory.isFile()) {
            validDirectoryImages.stream()
                    .filter(image -> image.equals(imageDirectory))
                    .findFirst().ifPresent(currentImage::set);
        }
        return currentImage.get();
    }

    /**
     * Generates and returns a new {@link CyderThreadFactory} for the loading {@link Executor}.
     *
     * @return a new {@link CyderThreadFactory} for the loading {@link Executor}
     */
    private CyderThreadFactory generateThreadFactory() {
        return new CyderThreadFactory("ImageViewer showGui thread, directory" + colon + space + imageDirectory);
    }

    /**
     * Refreshes the {@link #validDirectoryImages} list based on the currently set {@link #imageDirectory}.
     */
    private void refreshImageFiles() {
        validDirectoryImages.clear();

        File[] neighbors = imageDirectory.isDirectory()
                ? imageDirectory.listFiles()
                : imageDirectory.getParentFile().listFiles();
        if (ArrayUtil.nullOrEmpty(neighbors)) return;
        Arrays.stream(neighbors).filter(FileUtil::isSupportedImageExtension).forEach(validDirectoryImages::add);
    }

    /**
     * Transitions to the next image if possible.
     */
    private void transitionForward() {
        refreshImageFiles();
        if (validDirectoryImages.size() < 2) return;
        currentIndex = currentIndex == validDirectoryImages.size() - 1 ? 0 : currentIndex + 1;
        revalidateFromTransition();
    }

    /**
     * Transitions to the previous image if possible.
     */
    private void transitionBackward() {
        refreshImageFiles();
        if (validDirectoryImages.size() < 2) return;
        currentIndex = currentIndex == 0 ? validDirectoryImages.size() - 1 : currentIndex - 1;
        revalidateFromTransition();
    }

    /**
     * The logic to perform following a transition.
     */
    private void revalidateFromTransition() {
        Point oldCenterPoint = pictureFrame.getCenterPointOnScreen();
        ImageIcon image = scaleImageIfNeeded(validDirectoryImages.get(currentIndex));
        pictureFrame.setSize(image.getIconWidth(), image.getIconHeight());
        pictureFrame.setBackground(image);
        pictureFrame.setCenterPoint(oldCenterPoint);
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
        Preconditions.checkNotNull(imageFile);
        Preconditions.checkArgument(imageFile.exists());
        Preconditions.checkArgument(imageFile.isFile());

        try {
            BufferedImage bufferedImage = ImageUtil.read(imageFile);
            bufferedImage = ImageUtil.ensureFitsInBounds(bufferedImage, maxFrameSize);
            return ImageUtil.toImageIcon(bufferedImage);
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate ImageIcon for file" + colon
                    + space + imageFile.getAbsolutePath() + ", error: " + e.getMessage());
        }
    }

    /**
     * The actions to invoke when the rename menu item is pressed.
     */
    private void onRenameButtonClicked() {
        File currentRename = new File(validDirectoryImages.get(currentIndex).getAbsolutePath());
        File currentBackground = Console.INSTANCE
                .getCurrentBackground().getReferenceFile().getAbsoluteFile();

        if (currentRename.getAbsolutePath().equals(currentBackground.getAbsolutePath())) {
            pictureFrame.notify("Sorry, " + UserDataManager.INSTANCE.getUsername()
                    + ", but you're not allowed to rename the background you are currently using");
            return;
        }

        getterUtil.closeAllGetInputFrames();

        String initialFieldText = FileUtil.getFilename(validDirectoryImages.get(currentIndex));

        CyderThreadRunner.submit(() -> {
            try {
                GetInputBuilder builder = new GetInputBuilder(RENAME, "New filename for"
                        + space + quote + validDirectoryImages.get(currentIndex).getName() + quote)
                        .setRelativeTo(pictureFrame)
                        .setInitialFieldText(initialFieldText)
                        .setSubmitButtonText(RENAME);
                Optional<String> optionalName = getterUtil.getInput(builder);
                if (optionalName.isEmpty() || optionalName.get().equals(initialFieldText)) return;

                String requestedName = optionalName.get();

                File oldFileReference = new File(validDirectoryImages.get(currentIndex).getAbsolutePath());
                File newFileReference = new File(oldFileReference.getAbsolutePath()
                        .replace(FileUtil.getFilename(oldFileReference), requestedName));

                if (oldFileReference.renameTo(newFileReference)) {
                    pictureFrame.notify("Successfully renamed to" + space + quote + requestedName + quote);

                    refreshImageFiles();
                    IntStream.range(0, validDirectoryImages.size())
                            .forEach(index -> {
                                if (FileUtil.getFilename(validDirectoryImages.get(index)).equals(requestedName)) {
                                    currentIndex = index;
                                }
                            });
                    revalidateTitle(requestedName);
                } else {
                    pictureFrame.notify("Could not rename at this time");
                }

            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "ImageViewer File Renamer, file" + colon + space + initialFieldText);
    }

    /**
     * Revalidates the frame title based on the provided name.
     *
     * @param title the title of the frame
     */
    public void revalidateTitle(String title) {
        Preconditions.checkNotNull(title);

        title = title.trim();

        try {
            BufferedImage image = ImageUtil.read(validDirectoryImages.get(currentIndex));
            int width = image.getWidth();
            int height = image.getHeight();
            pictureFrame.setTitle(title + space + openingBracket + width + "x" + height + closingBracket);
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
                refreshImageFiles();
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
        refreshImageFiles();
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
