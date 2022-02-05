package cyder.widgets;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.genesis.CyderCommon;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollList;
import cyder.utilities.FileUtil;
import cyder.utilities.GetterUtil;
import cyder.utilities.IOUtil;
import cyder.utilities.StringUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.LinkedList;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class ImageAveragerWidget implements WidgetBase {
    private static LinkedList<File> files;
    private static JLabel imagesScrollLabel;
    private static CyderScrollList imagesScroll;
    private static CyderFrame cf;
    private static JLabel imageScrollLabelHolder;

    private ImageAveragerWidget() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    @Widget(trigger = {"average images", "average pictures"}, description = "A widget that adds multiple images " +
            "together and divides by the total to obtain an average base image")
    public static void showGUI() {
        Logger.log(Logger.Tag.WIDGET_OPENED, "AVERAGE IMAGES");

        files = new LinkedList<>();

        cf = new CyderFrame(600,640);
        cf.setTitle("Image Averager");

        imagesScroll = new CyderScrollList(400, 400, CyderScrollList.SelectionPolicy.SINGLE);
        imagesScroll.setBorder(null);

        imageScrollLabelHolder = new JLabel();
        imageScrollLabelHolder.setBounds(90,40,420,420);
        imageScrollLabelHolder.setBorder(new LineBorder(CyderColors.navy, 5));
        cf.getContentPane().add(imageScrollLabelHolder);

        imagesScrollLabel = imagesScroll.generateScrollList();
        imagesScrollLabel.setBounds(10, 10, 400, 400);
        imageScrollLabelHolder.add(imagesScrollLabel);

        imageScrollLabelHolder.setBackground(Color.white);
        imagesScrollLabel.setOpaque(true);
        imageScrollLabelHolder.setOpaque(true);
        imagesScrollLabel.setBackground(Color.white);

        CyderButton addButton = new CyderButton("Add Image");
        addButton.setBounds(90,480,420,40);
        cf.getContentPane().add(addButton);
        addButton.addActionListener(e -> new Thread(() -> {
            try {
                File input = new GetterUtil().getFile("select any png file");

                if (FileUtil.isSupportedImageExtension(input)) {
                    files.add(input);
                    revalidateScroll();
                } else {
                    cf.notify("Selected file is not a supported image file");
                }
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }, "wait thread for GetterUtil().getFile()").start());

        CyderButton remove = new CyderButton("Remove Image");
        remove.setBounds(90,530,420,40);
        cf.getContentPane().add(remove);
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
        average.setBounds(90,580,420,40);
        cf.getContentPane().add(average);
        average.addActionListener(e -> compute());

        cf.setVisible(true);
        cf.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }

    private static void revalidateScroll() {
        imagesScroll.removeAllElements();
        imageScrollLabelHolder.remove(imagesScrollLabel);

        for (int j = 0 ; j < files.size() ; j++) {
            int finalJ = j;
            class thisAction implements CyderScrollList.ScrollAction {
                @Override
                public void fire() {
                    IOUtil.openFile(files.get(finalJ).getAbsolutePath());
                }
            }

            thisAction action = new thisAction();
            imagesScroll.addElement(files.get(j).getName(), action);
        }

        imagesScroll.setItemAlignemnt(StyleConstants.ALIGN_LEFT);
        imagesScrollLabel = imagesScroll.generateScrollList();
        imagesScrollLabel.setBounds(10, 10, 400, 400);
        imageScrollLabelHolder.setBackground(CyderColors.vanila);

        imageScrollLabelHolder.add(imagesScrollLabel);
        imageScrollLabelHolder.revalidate();
        cf.revalidate();
    }

    private static void compute() {
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
                graph.setPaint(new Color(0,0,0,0));
                graph.fillRect(0, 0, width, height);

                //compute the average based on max width, height, and the BI to write to
                ComputeAverage(width, height, saveImage);

                CyderFrame drawFrame = new CyderFrame(saveImage.getWidth(), saveImage.getHeight(), new ImageIcon(saveImage));

                JButton save = new JButton("Save");
                save.setForeground(CyderColors.vanila);
                save.setFont(CyderFonts.defaultFontSmall);
                save.setToolTipText("Save image");
                save.addActionListener(e -> {
                    try {
                        File outFile = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() +
                                "/Backgrounds/" + combineImageNames() + ".png");
                        ImageIO.write(saveImage, "png", outFile);
                        cf.notify("Average computed and saved to your user's backgrounds/ directory");
                        drawFrame.dispose();
                    } catch (Exception ex) {
                        ExceptionHandler.handle(ex);
                    }
                });
                save.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        save.setForeground(CyderColors.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        save.setForeground(CyderColors.vanila);
                    }
                });

                save.setContentAreaFilled(false);
                save.setBorderPainted(false);
                save.setFocusPainted(false);
                drawFrame.getTopDragLabel().addButton(save, 0);

                drawFrame.getTopDragLabel().add(save,1);
                drawFrame.setVisible(true);
                drawFrame.setLocationRelativeTo(cf);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        } else if (files.size() == 1) {
            cf.notify("Please add at least two images");
        }
    }

    private static void ComputeAverage(int width, int height, BufferedImage saveImage) {
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
                int[][] currentPixels = get2DRGBArr(bufferImage);

                //get current dimensions and offsets
                int currentHeight = bufferImage.getHeight();
                int currentWidth = bufferImage.getWidth();
                int currentXOffset = (width - currentWidth) / 2;
                int currentYOffset = (height - currentHeight) / 2;

                //loop through current data
                for (int y = 0; y < currentPixels.length; y++) {
                    for (int x = 0; x < currentPixels[0].length; x++) {
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

    private static int[][] get2DRGBArr(BufferedImage image) {
        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        int[][] result = new int[height][width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
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

            for (int pixel = 0, row = 0, col = 0; pixel + 2 < pixels.length; pixel += pixelLength) {
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

    private static String combineImageNames() {
        StringBuilder ret = new StringBuilder();

        for (File f : files) {
            ret.append(StringUtil.getFilename(f.getName())).append("_");
        }

        return ret.substring(0, ret.toString().length() - 1);
    }
}
