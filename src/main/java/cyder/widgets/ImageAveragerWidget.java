package cyder.widgets;

import com.google.common.base.Preconditions;
import cyder.annotations.*;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.enums.CyderInspection;
import cyder.enums.Dynamic;
import cyder.enums.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.getter.GetFileBuilder;
import cyder.getter.GetterUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadRunner;
import cyder.ui.UiUtil;
import cyder.ui.button.CyderButton;
import cyder.ui.drag.button.DragLabelTextButton;
import cyder.ui.frame.CyderFrame;
import cyder.ui.pane.CyderScrollList;
import cyder.user.UserFile;
import cyder.user.UserUtil;
import cyder.utils.ImageUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

/**
 * A widget to average images together.
 */
@Vanilla
@CyderAuthor
public final class ImageAveragerWidget {
    /**
     * The scroll label for the selected images.
     */
    private static JLabel imagesScrollLabel;

    /**
     * The actual scroll container to hold the scroll label.
     */
    private static CyderScrollList imagesScroll;

    /**
     * The averaging frame.
     */
    private static CyderFrame averagerFrame;

    /**
     * The component to hold the scroll on top of it.
     */
    private static JLabel imageScrollLabelHolder;

    /**
     * Suppress default constructor.
     */
    private ImageAveragerWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The widget description.
     */
    private static final String description = "A widget that adds multiple images "
            + "together and divides by the total to obtain an average base image";

    /**
     * The title of the widget frame.
     */
    private static final String FRAME_TITLE = "Image Averager";

    /**
     * The save text.
     */
    private static final String SAVE = "Save";

    /**
     * The save image text.
     */
    private static final String SAVE_IMAGE = "Save Image";

    /**
     * The width of the widget frame.
     */
    private static final int FRAME_WIDTH = 600;

    /**
     * The height of the widget frame.
     */
    private static final int FRAME_HEIGHT = 640;

    /**
     * The length of the images scroll list.
     */
    private static final int imagesScrollLen = 400;

    /**
     * The builder for the getter util instance to add a file.
     */
    private static final GetFileBuilder builder = new GetFileBuilder("Select any image file")
            .setRelativeTo(averagerFrame);

    /**
     * The maximum image length that the preview frame can display.
     */
    private static final int maxImageLength = 800;

    /**
     * The combined files name separator.
     */
    private static final String UNDERSCORE = "_";

    /**
     * The add image button text.
     */
    private static final String ADD_IMAGE = "Add Image";

    /**
     * The remove images button text.
     */
    private static final String REMOVE_IMAGES = "Remove Selected Images";

    /**
     * The average images button text.
     */
    private static final String AVERAGE_IMAGES = "Average Images";

    /**
     * The thread name for the waiter thread for the add file getter.
     */
    private static final String IMAGE_AVERAGER_ADD_FILE_WAITER_THREAD_NAME = "Image Averager Add File Waiter";

    /**
     * The alpha value for pixels with a non-present alpha value.
     */
    private static final int EMPTY_ALPHA = 16777216;

    /**
     * The length of data for data elements containing an alpha byte.
     */
    private static final int alphaPixelLength = 4;

    /**
     * The length of data for data elements without an alpha byte.
     */
    private static final int noAlphaPixelLength = 3;

    /**
     * Shows the image averaging widget.
     */
    @SuppressCyderInspections(CyderInspection.WidgetInspection)
    @Widget(triggers = {"average images", "average pictures"}, description = description)
    public static void showGui() {
        UiUtil.closeIfOpen(averagerFrame);

        averagerFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT);
        averagerFrame.setTitle(FRAME_TITLE);

        imagesScroll = new CyderScrollList(imagesScrollLen, imagesScrollLen, CyderScrollList.SelectionPolicy.MULTIPLE);
        imagesScroll.setBorder(null);

        imageScrollLabelHolder = new JLabel();
        imageScrollLabelHolder.setBounds(90, 40, 420, 420);
        imageScrollLabelHolder.setBorder(new LineBorder(CyderColors.navy, 5));
        averagerFrame.getContentPane().add(imageScrollLabelHolder);

        imagesScrollLabel = imagesScroll.generateScrollList();
        imagesScrollLabel.setBounds(10, 10, imagesScrollLen, imagesScrollLen);
        imageScrollLabelHolder.add(imagesScrollLabel);

        imageScrollLabelHolder.setBackground(Color.white);
        imagesScrollLabel.setOpaque(true);
        imageScrollLabelHolder.setOpaque(true);
        imagesScrollLabel.setBackground(Color.white);

        CyderButton addFileButton = new CyderButton(ADD_IMAGE);
        addFileButton.setBounds(90, 480, 420, 40);
        averagerFrame.getContentPane().add(addFileButton);
        addFileButton.addActionListener(e -> addButtonAction());

        CyderButton removeSelectedImagesButton = new CyderButton(REMOVE_IMAGES);
        removeSelectedImagesButton.setBounds(90, 530, 420, 40);
        averagerFrame.getContentPane().add(removeSelectedImagesButton);
        removeSelectedImagesButton.addActionListener(e -> removeSelectedImagesButtonAction());

        CyderButton average = new CyderButton(AVERAGE_IMAGES);
        average.setBackground(CyderColors.regularPink);
        average.setBounds(90, 580, 420, 40);
        averagerFrame.getContentPane().add(average);
        average.addActionListener(e -> averageButtonAction());

        averagerFrame.finalizeAndShow();
    }

    /**
     * The action to invoke when the remove selected images button is pressed.
     */
    @ForReadability
    private static void removeSelectedImagesButtonAction() {
        LinkedList<String> selectedElements = imagesScroll.getSelectedElements();
        for (String selectedElement : selectedElements) {
            currentFiles.remove(selectedElement);
        }

        imagesScroll.removeSelectedElements();
        revalidateImagesScroll();
    }

    private static final HashMap<String, File> currentFiles = new HashMap<>();

    /**
     * Revalidates the chosen images scroll view.
     */
    private static void revalidateImagesScroll() {
        imagesScroll.removeAllElements();
        imageScrollLabelHolder.remove(imagesScrollLabel);

        currentFiles.forEach((filename, file) -> {
            Runnable openFileRunnable = () -> FileUtil.openResource(file.getAbsolutePath(), true);
            imagesScroll.addElementWithDoubleClickAction(filename, openFileRunnable);
        });

        imagesScroll.setItemAlignment(StyleConstants.ALIGN_LEFT);
        imagesScrollLabel = imagesScroll.generateScrollList();
        imagesScrollLabel.setBounds(10, 10, imagesScrollLen, imagesScrollLen);
        imageScrollLabelHolder.setBackground(CyderColors.vanilla);

        imageScrollLabelHolder.add(imagesScrollLabel);

        imageScrollLabelHolder.revalidate();
        imageScrollLabelHolder.repaint();

        averagerFrame.revalidate();
        averagerFrame.repaint();
    }

    /**
     * The action to invoke when the add file button is pressed.
     */
    private static void addButtonAction() {
        CyderThreadRunner.submit(() -> {
            try {
                Optional<File> optionalFile = GetterUtil.getInstance().getFile(builder);
                if (optionalFile.isEmpty()) return;
                File addFile = optionalFile.get();
                if (StringUtil.isNullOrEmpty(FileUtil.getFilename(addFile))) return;

                boolean supportedImage = FileUtil.isSupportedImageExtension(addFile);
                if (!supportedImage) {
                    averagerFrame.notify("Selected file is not a supported image file");
                    return;
                }

                currentFiles.put(addFile.getName(), addFile);
                revalidateImagesScroll();
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }, IMAGE_AVERAGER_ADD_FILE_WAITER_THREAD_NAME);
    }

    /**
     * Action performed when the user clicks the compute button.
     */
    private static void averageButtonAction() {
        if (currentFiles.size() < 2) {
            averagerFrame.notify("Please add at least two images");
            return;
        }

        AtomicInteger width = new AtomicInteger();
        AtomicInteger height = new AtomicInteger();

        currentFiles.forEach((filename, file) -> {
            try {
                BufferedImage currentImage = ImageUtil.read(file);
                width.set(Math.max(currentImage.getWidth(), width.get()));
                height.set(Math.max(currentImage.getHeight(), height.get()));
            } catch (Exception e) {
                averagerFrame.inform("IO Failure", "Failed to read image file: "
                        + file.getAbsolutePath());
            }
        });

        BufferedImage saveImage = computerAverage(width.get(), height.get());
        ImageIcon previewImage = ImageUtil.resizeIfLengthExceeded(new ImageIcon(saveImage), maxImageLength);

        String saveImageName = combineImageNames() + Extension.PNG.getExtension();
        File outputFile = Dynamic.buildDynamic(Dynamic.USERS.getFileName(),
                Console.INSTANCE.getUuid(), UserFile.BACKGROUNDS.getName(), saveImageName);

        CyderFrame drawFrame = new CyderFrame(previewImage.getIconWidth(), previewImage.getIconHeight(), previewImage);
        DragLabelTextButton saveButton = new DragLabelTextButton.Builder(SAVE)
                .setTooltip(SAVE_IMAGE)
                .setClickAction(() -> {
                    boolean saved = saveImage(saveImage, outputFile);
                    if (!saved) {
                        averagerFrame.notify("Could not save average at this time");
                        return;
                    }

                    averagerFrame.notify("Average computed and saved to "
                            + StringUtil.getApostropheSuffix(UserUtil.getCyderUser().getName())
                            + "backgrounds/ directory");
                    drawFrame.dispose(true);
                }).build();
        drawFrame.getTopDragLabel().addRightButton(saveButton, 0);

        drawFrame.setVisible(true);
        drawFrame.setLocationRelativeTo(averagerFrame);
    }

    /**
     * Saves the provided buffered image to the current user's backgrounds folder using the provided name.
     * Png format is used as the image format.
     *
     * @param saveImage  the buffered image to save
     * @param outputFile the file to save the buffered image to
     * @return whether the saving operation was successful
     */
    @ForReadability
    private static boolean saveImage(BufferedImage saveImage, File outputFile) {
        Preconditions.checkNotNull(saveImage);
        Preconditions.checkNotNull(outputFile);

        try {
            ImageIO.write(saveImage, Extension.PNG.getExtensionWithoutPeriod(), outputFile);
            return true;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return false;
    }

    /**
     * Computes the average of the images inside of the files array list and
     * modifies saveImage to have the resulting calculated pixel average.
     *
     * @param width  the width of the resulting image
     * @param height the height of the resulting image
     */
    private static BufferedImage computerAverage(int width, int height) {
        BufferedImage saveImage = new BufferedImage(width, height, TYPE_INT_ARGB);
        Graphics2D g2d = saveImage.createGraphics();
        g2d.setPaint(CyderColors.empty);
        g2d.fillRect(0, 0, width, height);

        /*
            This should be save to hold the sum of all pixel data.
            Adding a precondition check for file.size() * 256 < Integer.MAX_VALUE
            is even flagged by IntelliJ as always true
         */
        int[][] pixelSum = new int[height][width];
        int[][] divideBy = new int[height][width];

        for (int y = 0 ; y < divideBy.length ; y++) {
            for (int x = 0 ; x < divideBy[0].length ; x++) {
                divideBy[y][x] = 0;
            }
        }

        ArrayList<File> averageFiles = new ArrayList<>();
        currentFiles.forEach((filename, file) -> averageFiles.add(file));

        for (File currentImageFile : averageFiles) {
            BufferedImage currentImage;
            try {
                currentImage = ImageUtil.read(currentImageFile);
            } catch (Exception e) {
                averagerFrame.inform("IO Failure", "Failed to read image file: "
                        + currentImageFile.getAbsolutePath());
                ExceptionHandler.handle(e);
                continue;
            }

            int currentHeight = currentImage.getHeight();
            int currentWidth = currentImage.getWidth();

            int[][] currentPixels = get2DRgbArray(currentImage);
            int currentXOffset = (width - currentWidth) / 2;
            int currentYOffset = (height - currentHeight) / 2;

            for (int y = 0 ; y < currentPixels.length ; y++) {
                for (int x = 0 ; x < currentPixels[0].length ; x++) {
                    pixelSum[y + currentYOffset][x + currentXOffset] += currentPixels[y][x];
                    divideBy[y + currentYOffset][x + currentXOffset] += 1;
                }
            }
        }

        for (int y = 0 ; y < pixelSum.length ; y++) {
            for (int x = 0 ; x < pixelSum[0].length ; x++) {
                int dividend = pixelSum[y][x];
                int divisor = divideBy[y][x];
                if (divisor == 0) divisor = 1;
                int quotient = dividend / divisor;
                saveImage.setRGB(x, y, quotient);
            }
        }

        return saveImage;
    }

    /**
     * Returns a two dimensional integer array representing the pixel data of the provided buffered image.
     *
     * @param image the image to find the pixel data of
     * @return an array of pixel data
     */
    private static int[][] get2DRgbArray(BufferedImage image) {
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        int width = image.getWidth();
        int height = image.getHeight();

        boolean hasAlphaChannel = image.getAlphaRaster() != null;
        int[][] ret = new int[height][width];

        if (hasAlphaChannel) {
            for (int pixel = 0, row = 0, col = 0 ; pixel + 3 < pixels.length ; pixel += alphaPixelLength) {
                int argb = 0;

                argb += (((int) pixels[pixel] & 0xff) << 24);
                argb += (((int) pixels[pixel + 3] & 0xff) << 16);
                argb += (((int) pixels[pixel + 2] & 0xff) << 8);
                argb += ((int) pixels[pixel + 1] & 0xff);

                ret[row][col] = argb;
                col++;

                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            for (int pixel = 0, row = 0, col = 0 ; pixel + 2 < pixels.length ; pixel += noAlphaPixelLength) {
                int argb = 0;

                argb -= EMPTY_ALPHA;
                argb += (((int) pixels[pixel + 2] & 0xff) << 16);
                argb += (((int) pixels[pixel + 1] & 0xff) << 8);
                argb += ((int) pixels[pixel] & 0xff);

                ret[row][col] = argb;
                col++;

                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return ret;
    }

    /**
     * Returns a string of the filenames from the files array
     * list combined and separated by an underscore.
     *
     * @return the combined file names
     */
    @ForReadability
    private static String combineImageNames() {
        StringBuilder ret = new StringBuilder();

        int finalIndex = currentFiles.size() - 1;
        AtomicInteger currentIndex = new AtomicInteger();
        currentFiles.forEach((filename, file) -> {
            ret.append(FileUtil.getFilename(file.getName()));
            if (currentIndex.get() != finalIndex) ret.append(UNDERSCORE);
            currentIndex.getAndIncrement();
        });

        return ret.toString();
    }
}
