package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollList;
import cyder.utilities.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.LinkedList;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class ImageAverager {
    private LinkedList<File> files;
    private JLabel imagesScrollLabel;
    private CyderScrollList imagseScroll;
    private CyderFrame cf;
    private JLabel imageScrollLabelHolder;

    public ImageAverager() {
        files = new LinkedList<>();

        cf = new CyderFrame(500,830);
        cf.setTitle("Image Averager");

        imagseScroll = new CyderScrollList(680, 360, CyderScrollList.SelectionPolicy.SINGLE);
        imagseScroll.setBorder(null);

        imageScrollLabelHolder = new JLabel();
        imageScrollLabelHolder.setBounds(90,90,320,620);
        imageScrollLabelHolder.setBorder(new LineBorder(CyderColors.navy, 5));
        cf.getContentPane().add(imageScrollLabelHolder);

        imagesScrollLabel = imagseScroll.generateScrollList();
        imagesScrollLabel.setBounds(10, 10, 300, 600);
        imageScrollLabelHolder.add(imagesScrollLabel);

        imageScrollLabelHolder.setBackground(Color.white);
        imagesScrollLabel.setOpaque(true);
        imageScrollLabelHolder.setOpaque(true);
        imagesScrollLabel.setBackground(Color.white);

        CyderButton addButton = new CyderButton("Add Image");
        addButton.setBounds(100,40,300,40);
        cf.getContentPane().add(addButton);
        addButton.addActionListener(e -> new Thread(() -> {
            try {
                File input = new GetterUtil().getFile("select any png file");

                if (StringUtil.getExtension(input).equals(".png")) {
                    files.add(input);
                    revalidateScroll();
                } else {
                    cf.notify("Selected file is not a png");
                }
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        }, "wait thread for GetterUtil().getFile()").start());

        CyderButton remove = new CyderButton("Remove Image");
        remove.setBounds(100,720,300,40);
        cf.getContentPane().add(remove);
        remove.addActionListener(e -> {
            String matchName = imagseScroll.getSelectedElement();
            int removeIndex = -1;

            for (int i = 0 ; i < files.size() ; i++) {
                if (files.get(i).getName().equalsIgnoreCase(matchName)) {
                    removeIndex = i;
                    break;
                }
            }

            if (removeIndex != -1) {
                System.out.println(removeIndex);
                files.remove(removeIndex);
                revalidateScroll();
            }
        });

        CyderButton average = new CyderButton("Average Images");
        average.setBounds(100,780,300,40);
        cf.getContentPane().add(average);
        average.addActionListener(e -> compute());

        cf.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelative(cf);
    }

    private void revalidateScroll() {
        imagseScroll.removeAllElements();
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
            imagseScroll.addElement(files.get(j).getName(), action);
        }

        imagseScroll.setItemAlignemnt(StyleConstants.ALIGN_LEFT);
        imagesScrollLabel = imagseScroll.generateScrollList();
        imagesScrollLabel.setBackground(new Color(255,255,255));
        imagesScrollLabel.setBounds(10, 10, 300, 600);
        imageScrollLabelHolder.setBackground(CyderColors.vanila);

        imageScrollLabelHolder.add(imagesScrollLabel);
        imageScrollLabelHolder.revalidate();
        cf.revalidate();
    }

    private void compute() {
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

                BufferedImage saveImage = new BufferedImage(width, height, TYPE_INT_ARGB);
                Graphics2D graph = saveImage.createGraphics();
                graph.setPaint (new Color(255,255,255,0));
                graph.fillRect ( 0, 0, width, height);

                ComputeAverage(width, height, saveImage);

                ImageUtil.drawBufferedImage(saveImage);

//                File outFile = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() +
//                        "/Backgrounds/" + SecurityUtil.generateUUID() + ".png");
//                ImageIO.write(saveImage, "png", outFile);
//                cf.notify("Average computed and saved to your user's backgrounds/ directory");
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        } else if (files.size() == 1) {
            cf.notify("Please add at least two images");
        }
    }

    private void ComputeAverage(int width, int height, BufferedImage saveImage) {
        try {
            int[][] pixels = new int[height][width];
            int[][] divideBy = new int[height][width];

            //fill divideBy with zeros
            for (int y = 0 ; y < divideBy.length ; y++) {
                for (int x = 0 ; x < divideBy[0].length ; x++) {
                    divideBy[y][x] = 0;
                }
            }

            for (File file : files) {
                BufferedImage bufferImage = ImageIO.read(file);

                int[][] currentPixels = get2DRGBArr(bufferImage);

                int currentHeight = bufferImage.getHeight();
                int currentWidth = bufferImage.getWidth();
                int currentXOffset = (width - currentWidth) / 2;
                int currentYOffset = (height - currentHeight) / 2;

                for (int y = 0; y < currentPixels.length; y++) {
                    for (int x = 0; x < currentPixels[0].length; x++) {
                        pixels[y + currentYOffset][x + currentXOffset] += currentPixels[y][x];
                        pixels[y + currentYOffset][x + currentXOffset] += 1;
                    }
                }
            }

            for (int y = 0 ; y < pixels.length ; y++) {
                for (int x = 0 ; x < pixels[0].length ; x++) {
                    saveImage.setRGB(x, y, pixels[y][x] / (divideBy[y][x] == 0 ? 1 : divideBy[y][x]));
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private int[][] get2DRGBArr(BufferedImage image) {
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
}
