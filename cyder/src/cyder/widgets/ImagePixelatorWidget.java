package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.ForReadability;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.enums.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.getter.GetFileBuilder;
import cyder.getter.GetterUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.layouts.CyderPartitionedLayout;
import cyder.threads.CyderThreadRunner;
import cyder.ui.button.CyderButton;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.label.CyderLabel;
import cyder.user.UserFile;
import cyder.user.UserUtil;
import cyder.utils.ImageUtil;
import cyder.utils.OsUtil;
import cyder.utils.StringUtil;
import cyder.utils.UiUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;

/**
 * An image pixelator widget.
 */
@Vanilla
@CyderAuthor
public final class ImagePixelatorWidget {
    /**
     * The current icon that is being displayed.
     */
    private static ImageIcon currentDisplayImageIcon;

    /**
     * The current raw image file.
     */
    private static File currentFile;

    /**
     * The image pixelation preview label.
     */
    private static JLabel previewLabel;

    /**
     * The pixelation size field.
     */
    private static CyderTextField pixelSizeField;

    /**
     * Suppress default constructor.
     */
    private ImagePixelatorWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The widget description
     */
    private static final String description = "A simple image pixelator widget that transforms"
            + " the image into an image depicted of the specified number of pixels";

    /**
     * The width of the frame.
     */
    private static final int FRAME_WIDTH = 700;

    /**
     * The height of the frame.
     */
    private static final int FRAME_HEIGHT = 950;

    /**
     * The widget frame title.
     */
    private static final String IMAGE_PIXELATOR = "Image Pixelator";

    /**
     * The pixel size label text.
     */
    private static final String PIXEL_SIZE = "Pixel Size";

    /**
     * The choose image button text.
     */
    private static final String CHOOSE_IMAGE = "Choose Image";

    /**
     * The widget frame.
     */
    private static CyderFrame pixelFrame;

    /**
     * The border for the preview label.
     */
    private static final LineBorder previewLabelBorder = new LineBorder(CyderColors.navy, 5, false);

    /**
     * The maximum length for the preview label image.
     */
    private static final int previewImageMaxLen = 600;

    /**
     * The name of the waiter thread for the get file getter util instance.
     */
    private static final String CHOOSE_IMAGE_WAITER_THREAD_NAME = "Choose Image Waiter Thread";

    /**
     * The builder for the choose file button.
     */
    private static final GetFileBuilder getterUtilBuilder = new GetFileBuilder("Choose file to resize")
            .setRelativeTo(pixelFrame);

    /**
     * The image files text.
     */
    private static final String IMAGE_FILES = "Image files";

    /**
     * The pixel label font.
     */
    private static final Font pixelLabelFont = new Font(CyderFonts.AGENCY_FB, Font.BOLD, 28);

    /**
     * The approve image label.
     */
    private static final String APPROVE_IMAGE = "Approve Image";

    /**
     * The pixelated pixel size part of the name when saving a pixelated image.
     */
    private static final String PIXELATED_PIXEL_SIZE = "_Pixelated_Pixel_Size_";

    /**
     * The regex for the pixel size field to restrict the input to numbers.
     */
    private static final String pixelSizeFieldRegexMatcher = "[0-9]*";

    /**
     * The length of primary components.
     */
    private static final int componentLength = 300;

    /**
     * The height of primary components.
     */
    private static final int componentHeight = 40;

    /**
     * The partition length for primary components.
     */
    private static final int componentPartition = 7;

    @Widget(triggers = {"pixelate picture", "pixelate image", "pixelator"}, description = description)
    public static void showGui() {
        showGui(null);
    }

    /**
     * Shows the widget ui with the provided image as the preview image.
     *
     * @param imageFile the image to pixelate
     */
    public static void showGui(File imageFile) {
        UiUtil.closeIfOpen(pixelFrame);

        pixelFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT);
        pixelFrame.setTitle(IMAGE_PIXELATOR);

        float remainingPartition = CyderPartitionedLayout.MAX_PARTITION - 4 * componentPartition;

        CyderPartitionedLayout partitionedLayout = new CyderPartitionedLayout();
        partitionedLayout.setPartitionDirection(CyderPartitionedLayout.PartitionDirection.COLUMN);

        CyderLabel pixelSizeLabel = new CyderLabel(PIXEL_SIZE);
        pixelSizeLabel.setFont(pixelLabelFont);
        pixelSizeLabel.setSize(componentLength, componentHeight);
        partitionedLayout.addComponent(pixelSizeLabel, componentPartition);

        pixelSizeField = new CyderTextField();
        pixelSizeField.setKeyEventRegexMatcher(pixelSizeFieldRegexMatcher);
        pixelSizeField.setSize(componentLength, componentHeight);
        pixelSizeField.addKeyListener(pixelSizeFieldKeyAdapter);
        partitionedLayout.addComponent(pixelSizeField, componentPartition);

        CyderButton chooseImage = new CyderButton(CHOOSE_IMAGE);
        chooseImage.setToolTipText(IMAGE_FILES);
        chooseImage.setSize(componentLength, componentHeight);
        chooseImage.addActionListener(e -> chooseImageButtonAction());
        partitionedLayout.addComponent(chooseImage, componentPartition);

        CyderButton approveImageButton = new CyderButton(APPROVE_IMAGE);
        approveImageButton.setSize(componentLength, componentHeight);
        approveImageButton.addActionListener(e -> approveImageAction());
        partitionedLayout.addComponent(approveImageButton, componentPartition);

        previewLabel = new JLabel();
        previewLabel.setSize(previewImageMaxLen, previewImageMaxLen);
        previewLabel.setBorder(previewLabelBorder);
        partitionedLayout.addComponent(previewLabel, (int) remainingPartition);

        attemptToSetFileAsImage(imageFile);

        pixelFrame.setCyderLayout(partitionedLayout);
        pixelFrame.finalizeAndShow();
    }

    /**
     * The actions to invoke when the approve image button is pressed.
     */
    private static void approveImageAction() {
        String pixelInput = pixelSizeField.getText();
        if (StringUtil.isNullOrEmpty(pixelInput)) return;

        int pixelSize;
        try {
            pixelSize = Integer.parseInt(pixelInput);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            pixelFrame.notify("Could not parse input as an integer:" + pixelInput);
            return;
        }

        if (pixelSize == 1) {
            pixelFrame.notify("Pixel size is already 1");
            return;
        }

        BufferedImage currentBufferedImage;
        try {
            currentBufferedImage = ImageUtil.read(currentFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            pixelFrame.notify("Failed to read image file: " + currentFile.getAbsolutePath());
            return;
        }

        BufferedImage saveImage = ImageUtil.pixelateImage(currentBufferedImage, pixelSize);

        String currentFilename = FileUtil.getFilename(currentFile);
        String saveName = currentFilename + PIXELATED_PIXEL_SIZE + pixelSize + Extension.PNG.getExtension();
        File saveFile = new File(OsUtil.buildPath(Dynamic.PATH, Dynamic.USERS.getDirectoryName(),
                Console.INSTANCE.getUuid(), UserFile.FILES.getName(), saveName));

        try {
            ImageIO.write(saveImage, Extension.PNG.getExtensionWithoutPeriod(), saveFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            pixelFrame.notify("Failed to write pixelated image");
            return;
        }

        pixelFrame.notify("Successfully saved pixelated image to "
                + StringUtil.getApostrophe(UserUtil.getCyderUser().getName())
                + " files/ directory");
    }

    /**
     * The key listener for the pixel size field.
     */
    private static final KeyListener pixelSizeFieldKeyAdapter = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            try {
                String input = pixelSizeField.getText();
                if (input.isEmpty()) return;

                int pixelSize = -1;
                try {
                    pixelSize = Integer.parseInt(pixelSizeField.getText());
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                }

                if (pixelSize == -1) {
                    pixelFrame.notify("Could not parse input as integer: " + input);
                    return;
                }

                if (pixelSize == 0 || pixelSize == 1) return;

                BufferedImage bufferedImage = ImageUtil.pixelateImage(ImageUtil.read(currentFile), pixelSize);
                currentDisplayImageIcon = ImageUtil.resizeIfLengthExceeded(
                        ImageUtil.toImageIcon(bufferedImage), previewImageMaxLen);
                previewLabel.setIcon(currentDisplayImageIcon);

                refreshPreviewLabelSize();
                repaintPreviewLabelAndFrame();
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }
    };

    /**
     * The actions to invoke when the choose image button is pressed.
     */
    @ForReadability
    private static void chooseImageButtonAction() {
        CyderThreadRunner.submit(() -> {
            Optional<File> optionalFile = GetterUtil.getInstance().getFile(getterUtilBuilder);
            if (optionalFile.isEmpty()) return;
            attemptToSetFileAsImage(optionalFile.get());
        }, CHOOSE_IMAGE_WAITER_THREAD_NAME);
    }

    /**
     * Attempts to read the provided file and set it as the current image file to be pixelated.
     *
     * @param imageFile the file to read and set as the current image file to be pixelated
     */
    @ForReadability
    private static void attemptToSetFileAsImage(File imageFile) {
        if (imageFile == null || !imageFile.exists() || !FileUtil.isSupportedImageExtension(imageFile)) {
            currentFile = null;
            currentDisplayImageIcon = null;

            repaintPreviewLabelAndFrame();

            return;
        }

        currentFile = imageFile;

        BufferedImage newBufferedImage;
        try {
            newBufferedImage = ImageUtil.read(imageFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            pixelFrame.notify("Could not read chosen file: " + imageFile.getAbsolutePath());
            return;
        }

        currentDisplayImageIcon = ImageUtil.resizeIfLengthExceeded(
                ImageUtil.toImageIcon(newBufferedImage), previewImageMaxLen);
        previewLabel.setIcon(currentDisplayImageIcon);

        refreshPreviewLabelSize();
        repaintPreviewLabelAndFrame();
    }

    /**
     * Refreshes the size of the preview label based on the current icon.
     */
    @ForReadability
    private static void refreshPreviewLabelSize() {
        ImageIcon previewIcon = (ImageIcon) previewLabel.getIcon();
        if (previewIcon == null) {
            previewLabel.setSize(previewImageMaxLen, previewImageMaxLen);
        } else {
            previewLabel.setSize(previewIcon.getIconWidth(), previewIcon.getIconHeight());
        }
    }

    /**
     * Revalidates and repaints the previewLabel and pixelFrame.
     */
    @ForReadability
    private static void repaintPreviewLabelAndFrame() {
        previewLabel.revalidate();
        previewLabel.repaint();

        pixelFrame.revalidate();
        pixelFrame.repaint();
    }
}
