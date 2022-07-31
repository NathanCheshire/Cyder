package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.CyderInspection;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderButton;
import cyder.ui.CyderDragLabel;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollList;
import cyder.user.UserFile;
import cyder.utils.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

/**
 * A widget to average images together.
 */
@Vanilla
@CyderAuthor
public final class ImageAveragerWidget {
    /**
     * The list of selected files to average together.
     */
    private static ArrayList<File> files;

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
     * Instantiation of class not permitted.
     */
    private ImageAveragerWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Shows the image averaging widget.
     */
    @SuppressCyderInspections(CyderInspection.WidgetInspection)
    @Widget(triggers = {"average images", "average pictures"}, description = "A widget that adds multiple images " +
            "together and divides by the total to obtain an average base image")
    public static void showGui() {
        files = new ArrayList<>();

        if (averagerFrame != null)
            averagerFrame.dispose(true);

        averagerFrame = new CyderFrame(600, 640);
        averagerFrame.setTitle("Image Averager");

        imagesScroll = new CyderScrollList(400, 400, CyderScrollList.SelectionPolicy.SINGLE);
        imagesScroll.setBorder(null);

        imageScrollLabelHolder = new JLabel();
        imageScrollLabelHolder.setBounds(90, 40, 420, 420);
        imageScrollLabelHolder.setBorder(new LineBorder(CyderColors.navy, 5));
        averagerFrame.getContentPane().add(imageScrollLabelHolder);

        imagesScrollLabel = imagesScroll.generateScrollList();
        imagesScrollLabel.setBounds(10, 10, 400, 400);
        imageScrollLabelHolder.add(imagesScrollLabel);

        imageScrollLabelHolder.setBackground(Color.white);
        imagesScrollLabel.setOpaque(true);
        imageScrollLabelHolder.setOpaque(true);
        imagesScrollLabel.setBackground(Color.white);

        CyderButton addButton = new CyderButton("Add Image");
        addButton.setBounds(90, 480, 420, 40);
        averagerFrame.getContentPane().add(addButton);
        addButton.addActionListener(e -> CyderThreadRunner.submit(() -> {
            try {
                File input = GetterUtil.getInstance().getFile(
                        new GetterUtil.Builder("Select any image file").setRelativeTo(averagerFrame));

                if (FileUtil.isSupportedImageExtension(input)) {
                    files.add(input);
                    revalidateScroll();
                } else {
                    averagerFrame.notify("Selected file is not a supported image file");
                }
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }, "wait thread for GetterUtil().getFile()"));

        CyderButton remove = new CyderButton("Remove Image");
        remove.setBounds(90, 530, 420, 40);
        averagerFrame.getContentPane().add(remove);
        remove.addActionListener(e -> {
            String matchName = imagesScroll.getSelectedElement();
            int removeIndex = -1;

            for (int i = 0 ; i < files.size() ; i++) {
                if (files.get(i).getName().equalsIgnoreCase(matchName)) {
                    removeIndex = i;
                    break;
                }
            }

            if (removeIndex != -1) {
                files.remove(removeIndex);
                revalidateScroll();
            }
        });

        CyderButton average = new CyderButton("Average Images");
        average.setColors(CyderColors.regularPink);
        average.setBounds(90, 580, 420, 40);
        averagerFrame.getContentPane().add(average);
        average.addActionListener(e -> averageButtonAction());

        averagerFrame.finalizeAndShow();
    }

    /**
     * Revalidates the chosen images viewport.
     */
    private static void revalidateScroll() {
        imagesScroll.removeAllElements();
        imageScrollLabelHolder.remove(imagesScrollLabel);

        for (int j = 0 ; j < files.size() ; j++) {
            int finalJ = j;
            imagesScroll.addElement(files.get(j).getName(),
                    () -> IOUtil.openFile(files.get(finalJ).getAbsolutePath()));
        }

        imagesScroll.setItemAlignment(StyleConstants.ALIGN_LEFT);
        imagesScrollLabel = imagesScroll.generateScrollList();
        imagesScrollLabel.setBounds(10, 10, 400, 400);
        imageScrollLabelHolder.setBackground(CyderColors.vanilla);

        imageScrollLabelHolder.add(imagesScrollLabel);
        imageScrollLabelHolder.revalidate();
        averagerFrame.revalidate();
    }

    /**
     * Action performed when the user clicks the compute button.
     */
    private static void averageButtonAction() {
        if (files.size() > 1) {
            try {
                int width = 0;
                int height = 0;

                for (File file : files) {
                    BufferedImage currentImage = ImageIO.read(file);

                    if (currentImage.getWidth() > width) {
                        width = currentImage.getWidth();
                    }

                    if (currentImage.getHeight() > height) {
                        height = currentImage.getHeight();
                    }
                }

                //alpha since possibility of alpha channel
                BufferedImage saveImage = new BufferedImage(width, height, TYPE_INT_ARGB);
                Graphics2D graph = saveImage.createGraphics();

                //init transparent image
                graph.setPaint(new Color(0, 0, 0, 0));
                graph.fillRect(0, 0, width, height);

                //compute the average based on max width, height, and the BI to write to
                computerAverage(width, height, saveImage);

                ImageIcon previewImage = checkImage(new ImageIcon(saveImage));

                CyderFrame drawFrame = new CyderFrame(
                        previewImage.getIconWidth(),
                        previewImage.getIconHeight(),
                        previewImage);

                JLabel save = CyderDragLabel.generateTextButton("Save", "Save Image", () -> {
                    try {
                        File outFile = OSUtil.buildFile(Dynamic.PATH,
                                Dynamic.USERS.getDirectoryName(),
                                Console.INSTANCE.getUuid(), UserFile.BACKGROUNDS.getName(),
                                combineImageNames() + "." + ImageUtil.PNG_FORMAT);

                        ImageIO.write(saveImage, ImageUtil.PNG_FORMAT, outFile);
                        averagerFrame.notify("Average computed and saved to your user's backgrounds/ directory");
                        drawFrame.dispose();
                    } catch (Exception ex) {
                        ExceptionHandler.handle(ex);
                    }
                });
                drawFrame.getTopDragLabel().addRightButton(save, 0);

                drawFrame.getTopDragLabel().add(save, 1);
                drawFrame.setVisible(true);
                drawFrame.setLocationRelativeTo(averagerFrame);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        } else if (files.size() == 1) {
            averagerFrame.notify("Please add at least two images");
        }
    }

    /**
     * Computes the average of the images inside of the files array list and
     * modifies saveImage to have the resulting calculated pixel average.
     *
     * @param width     the width of the resulting image
     * @param height    the height of the resulting image
     * @param saveImage the reference image to save the averaged image to
     */
    private static void computerAverage(int width, int height, BufferedImage saveImage) {
        try {
            //running add array
            int[][] pixels = new int[height][width];
            //averaging array
            int[][] divideBy = new int[height][width];

            //fill averaging with zeros
            for (int y = 0 ; y < divideBy.length ; y++) {
                for (int x = 0 ; x < divideBy[0].length ; x++) {
                    divideBy[y][x] = 0;
                }
            }

            //for all files
            for (File file : files) {
                //create bi object
                BufferedImage bufferImage = ImageIO.read(file);

                //get pixel data
                int[][] currentPixels = get2DRgbArray(bufferImage);

                //get current dimensions and offsets
                int currentHeight = bufferImage.getHeight();
                int currentWidth = bufferImage.getWidth();
                int currentXOffset = (width - currentWidth) / 2;
                int currentYOffset = (height - currentHeight) / 2;

                //loop through current data
                for (int y = 0 ; y < currentPixels.length ; y++) {
                    for (int x = 0 ; x < currentPixels[0].length ; x++) {
                        //add in current data to master array accounting for offset
                        pixels[y + currentYOffset][x + currentXOffset] += currentPixels[y][x];
                        //add in data to say this pixel was added to
                        divideBy[y + currentYOffset][x + currentXOffset] += 1;
                    }
                }
            }

            //can't divide by 0 so for the pixel values that were not changed, divide by a 1
            for (int y = 0 ; y < divideBy.length ; y++) {
                for (int x = 0 ; x < divideBy[0].length ; x++) {
                    if (divideBy[y][x] == 0) {
                        divideBy[y][x] = 1;
                    }
                }
            }

            //write pixel data to image
            for (int y = 0 ; y < pixels.length ; y++) {
                for (int x = 0 ; x < pixels[0].length ; x++) {
                    saveImage.setRGB(x, y, pixels[y][x] / divideBy[y][x]);
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
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

        int[][] result = new int[height][width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0 ; pixel + 3 < pixels.length ; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;

                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;

            for (int pixel = 0, row = 0, col = 0 ; pixel + 2 < pixels.length ; pixel += pixelLength) {
                int argb = 0;
                argb -= 16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;

                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return result;
    }

    /**
     * Returns a string of the filenames from the files array
     * list combined and separated by an underscore.
     *
     * @return the combined file names
     */
    private static String combineImageNames() {
        StringBuilder ret = new StringBuilder();

        for (File f : files) {
            ret.append(FileUtil.getFilename(f.getName())).append("_");
        }

        return ret.substring(0, ret.toString().length() - 1);
    }

    /**
     * Returns an image icon no bigger than 800x800.
     *
     * @param originalIcon the icon to resize if needed
     * @return a new icon that is guaranteed to be at most 800x800
     */
    private static ImageIcon checkImage(ImageIcon originalIcon) {
        BufferedImage bi = ImageUtil.getBufferedImage(originalIcon);
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
}
