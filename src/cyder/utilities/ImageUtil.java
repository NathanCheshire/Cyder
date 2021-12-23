package cyder.utilities;

import cyder.consts.CyderStrings;
import cyder.enums.Direction;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ErrorHandler;
import cyder.ui.CyderFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ImageUtil {

    private ImageUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    private static CyderFrame pixelFrame;

    public static BufferedImage pixelate(BufferedImage imageToPixelate, int pixelSize) {
        BufferedImage pixelateImage = new BufferedImage(
                imageToPixelate.getWidth(),
                imageToPixelate.getHeight(),
                imageToPixelate.getType());

        for (int y = 0; y < imageToPixelate.getHeight(); y += pixelSize) {
            for (int x = 0; x < imageToPixelate.getWidth(); x += pixelSize) {
                BufferedImage croppedImage = getCroppedImage(imageToPixelate, x, y, pixelSize, pixelSize);
                Color dominantColor = getDominantColor(croppedImage);

                for (int yd = y; (yd < y + pixelSize) && (yd < pixelateImage.getHeight()); yd++)
                    for (int xd = x; (xd < x + pixelSize) && (xd < pixelateImage.getWidth()); xd++)
                        pixelateImage.setRGB(xd, yd, dominantColor.getRGB());

            }
        }

        return pixelateImage;
    }

    /**
     * Crops the specified bufferedImage to the new bounds and returns a new buffered image
     * @param image the buffered image to crop
     * @param startx the starting x pixel within the image
     * @param starty the starting y pixel within the image
     * @param width the width of the new image
     * @param height the height of the new image
     * @return the requested cropped image
     */
    public static BufferedImage getCroppedImage(BufferedImage image, int startx, int starty, int width, int height) {
        if (startx < 0)
            startx = 0;

        if (starty < 0)
            starty = 0;

        if (startx > image.getWidth())
            startx = image.getWidth();

        if (starty > image.getHeight())
            starty = image.getHeight();

        if (startx + width > image.getWidth())
            width = image.getWidth() - startx;

        if (starty + height > image.getHeight())
            height = image.getHeight() - starty;

        return image.getSubimage(startx, starty, width, height);
    }

    public static Color getDominantColor(BufferedImage image) {
        Map<Integer, Integer> colorCounter = new HashMap<>(100);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int currentRGB = image.getRGB(x, y);
                int count = colorCounter.getOrDefault(currentRGB, 0);
                colorCounter.put(currentRGB, count + 1);
            }
        }

        return getDominantColor(colorCounter);
    }

    public static Color getDominantColorOpposite(BufferedImage image) {
        Color c = getDominantColor(image);
        return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), c.getAlpha());
    }

    public static Color getDominantColor(Map<Integer, Integer> colorCounter) {
        int dominantRGB = colorCounter.entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
                .get()
                .getKey();

        return new Color(dominantRGB);
    }

    public static BufferedImage bufferedImageFromColor(int width, int height, Color c) {
        BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bi.createGraphics();

        graphics.setPaint(c);
        graphics.fillRect ( 0, 0, width, height);

        return bi;
    }

    public static ImageIcon imageIconFromColor(Color c) {
        BufferedImage im = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics();
        g.setPaint(c);
        g.fillRect(0, 0, 1, 1);
        return new ImageIcon(im);
    }

    public static ImageIcon imageIconFromColor(Color c, int width, int height) {
        BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics();
        g.setPaint(c);
        g.fillRect(0, 0, width, height);
        return new ImageIcon(im);
    }

    public static BufferedImage resizeImage(int x, int y, ImageIcon icon) {
        BufferedImage ReturnImage = null;

        try {
            Image ConsoleImage = icon.getImage();
            Image TransferImage = ConsoleImage.getScaledInstance(x, y, Image.SCALE_SMOOTH);
            ReturnImage = new BufferedImage(TransferImage.getWidth(null), TransferImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D bGr = ReturnImage.createGraphics();

            bGr.drawImage(TransferImage, 0, 0, null);
            bGr.dispose();
            return ReturnImage;
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return ReturnImage;
    }

    public static BufferedImage resizeImage(int x, int y, File UneditedImage) {
        BufferedImage ReturnImage = null;

        try {
            File CurrentConsole = UneditedImage;
            Image ConsoleImage = ImageIO.read(CurrentConsole);
            Image TransferImage = ConsoleImage.getScaledInstance(x, y, Image.SCALE_SMOOTH);
            ReturnImage = new BufferedImage(TransferImage.getWidth(null), TransferImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D bGr = ReturnImage.createGraphics();

            bGr.drawImage(TransferImage, 0, 0, null);
            bGr.dispose();
            return ReturnImage;
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return ReturnImage;
    }

    public BufferedImage getBi(File imageFile) {
        try {
            return ImageIO.read(imageFile);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return null;
    }

    public static BufferedImage getBi(ImageIcon im) {
        BufferedImage bi = new BufferedImage(im.getIconWidth(), im.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.createGraphics();
        im.paintIcon(null, g, 0,0);
        return bi;
    }

    public static BufferedImage getBi(String filename) {
        try {
            return ImageIO.read(new File(filename));
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return null;
    }

    /**
     * Returns the passed background file accounting for set rotate direction. For example, if the direction
     * is left this method will return a left rotated image. Use {@link cyder.ui.ConsoleFrame#getCurrentBackgroundImageIcon}
     * to get the image without possible rotations applied. This method is used to set the console background when the background
     * is altered or updated.
     * @param bufferedImage the path to the file
     * @param consoleDirection the cardinal direction of rotation
     * @return the rotated image
     */
    public static BufferedImage getRotatedImage(String bufferedImage, Direction consoleDirection) {
        switch(consoleDirection) {
            case TOP:
                return ImageUtil.getBi(bufferedImage);
            case RIGHT:
                return ImageUtil.rotateImageByDegrees(ImageUtil.getBi(bufferedImage),90);
            case BOTTOM:
                return ImageUtil.rotateImageByDegrees(ImageUtil.getBi(bufferedImage),180);
            case LEFT:
                return ImageUtil.rotateImageByDegrees(ImageUtil.getBi(bufferedImage),-90);
        }

        return null;
    }

    public static BufferedImage rotateImageByDegrees(BufferedImage img, double angle) {
        double rads = Math.toRadians(angle);

        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));

        int w = img.getWidth();
        int h = img.getHeight();

        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        at.rotate(rads, w / 2, h / 2);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    public static int xOffsetForCenterJLabel(int compWidth, String title) {
        return (int) Math.floor(5 + (compWidth / 2.0)) - (((int) Math.ceil(14 * title.length())) / 2);
    }

    public static void drawBufferedImage(BufferedImage bi) {
        CyderFrame frame = new CyderFrame(bi.getWidth(), bi.getHeight(), new ImageIcon(bi));
        frame.setVisible(true);
        frame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    public static void drawImageIcon(ImageIcon icon) {
        CyderFrame frame = new CyderFrame(icon.getIconWidth(), icon.getIconHeight(), icon);
        frame.setTitle(icon.getDescription() == null || icon.getDescription().length() == 0 ? "" : icon.getDescription());
        frame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        frame.setVisible(true);
        frame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int type, int img_width, int img_height) {
        BufferedImage resizedImage = new BufferedImage(img_width, img_height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, img_width, img_height, null);
        g.dispose();

        return resizedImage;
    }

    public static double getAspectRatio(BufferedImage im) {
        return ((double) im.getWidth() / (double) im.getHeight());
    }

    public int getScreenResolution() {
        return Toolkit.getDefaultToolkit().getScreenResolution();
    }

    public static ImageIcon resizeImage(ImageIcon srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg.getImage(), 0, 0, w, h, null);
        g2.dispose();

        return new ImageIcon(resizedImg);
    }

    /** the two images must be of the same size in order to merge them into one image
     *
     * @param newImage the new image (image to be placed to the dir[ection] of the old image)
     * @param oldImage the old image (image to be placed center)
     * @param dir the direction to place the newImage relative to the oldImage
     * @return the combined image
     */
    public static ImageIcon combineImages(ImageIcon oldImage, ImageIcon newImage, Direction dir) {
        ImageIcon ret = null;

        if (oldImage.getIconWidth() != newImage.getIconWidth() || oldImage.getIconHeight() != newImage.getIconHeight())
            return ret;

        try {
            BufferedImage bi1 = ImageIcon2BufferedImage(oldImage);
            BufferedImage bi2 = ImageIcon2BufferedImage(newImage);

            int width = 0;
            int height = 0;
            BufferedImage combined = null;
            Graphics2D g2 = null;

            switch (dir) {
                case LEFT:
                    width = 2 * newImage.getIconWidth();
                    height = newImage.getIconHeight();

                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();

                    g2.drawImage(bi1, null, width / 2, 0);
                    g2.drawImage(bi2, null, 0, 0);
                    g2.dispose();

                    break;
                case RIGHT:
                    width = 2 * newImage.getIconWidth();
                    height = newImage.getIconHeight();

                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();

                    g2.drawImage(bi1, null, 0, 0);
                    g2.drawImage(bi2, null, width / 2, 0);
                    g2.dispose();

                    break;
                case TOP:
                    width = newImage.getIconWidth();
                    height = 2 * newImage.getIconHeight();

                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();

                    g2.drawImage(bi1, null, 0, height / 2);
                    g2.drawImage(bi2, null, 0, 0);
                    g2.dispose();

                    break;
                case BOTTOM:
                    width = newImage.getIconWidth();
                    height = 2 * newImage.getIconHeight();

                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();

                    g2.drawImage(bi1, null, 0, 0);
                    g2.drawImage(bi2, null, 0, height / 2);
                    g2.dispose();

                    break;
                default:
                    throw new IllegalArgumentException("Somehow an invalid direction was specified");
            }

            ret = new ImageIcon(combined);

        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return ret;
    }

    public static BufferedImage ImageIcon2BufferedImage(ImageIcon icon) {
        BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.createGraphics();
        icon.paintIcon(null, g, 0,0);
        g.dispose();
        return bi;
    }

    public static boolean checkFileSignature(File checkFile, int[] signature) {
        boolean ret = true;

        try  {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(checkFile));
            int[] headerBytes = new int[signature.length];

            for (int i = 0; i < signature.length; i++) {
                headerBytes[i] = inputStream.read();
                if (headerBytes[i] != signature[i]) {
                    ret = false;
                }
            }
        } catch (IOException ex) {
            ErrorHandler.handle(ex);
        }

        return ret;
    }

    public static BufferedImage getImageGradient(int width, int height, Color shadeColor, Color primaryRight, Color primaryLeft) {
        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = ret.createGraphics();

        GradientPaint primary = new GradientPaint(0f, 0f, primaryLeft, height, 0f, primaryRight);
        GradientPaint shade = new GradientPaint(0f, 0f,
                new Color(shadeColor.getRed(), shadeColor.getGreen(), shadeColor.getBlue(), 0), 0f, 600, shadeColor);
        g2.setPaint(primary);
        g2.fillRect(0, 0, width, height);
        g2.setPaint(shade);
        g2.fillRect(0, 0, width, height);

        g2.dispose();

        return ret;
    }

    public static boolean whiteImage(ImageIcon icon) {
        try {
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            int[] pixels = new int[w * h];
            PixelGrabber pg = new PixelGrabber(icon.getImage(), 0, 0, w, h, pixels, 0, w);
            pg.grabPixels();
            boolean allWhite = true;
            for (int pixel : pixels) {
                Color color = new Color(pixel);
                if (color.getRGB() != Color.WHITE.getRGB()) {
                    allWhite = false;
                    break;
                }
            }

            return allWhite;
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return false;
    }

    public static boolean blackImage(ImageIcon icon) {
        try {
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            int[] pixels = new int[w * h];
            PixelGrabber pg = new PixelGrabber(icon.getImage(), 0, 0, w, h, pixels, 0, w);
            pg.grabPixels();
            boolean allBlack = true;
            for (int pixel : pixels) {
                Color color = new Color(pixel);
                if (color.getRGB() != Color.BLACK.getRGB()) {
                    allBlack = false;
                    break;
                }
            }

            return allBlack;
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return false;
    }

    public static boolean grayScaleImage(File pathToFile) {
        try {
            Image icon = new ImageIcon(ImageIO.read(pathToFile)).getImage();
            int w = icon.getWidth(null);
            int h = icon.getHeight(null);
            int[] pixels = new int[w * h];
            PixelGrabber pg = new PixelGrabber(icon, 0, 0, w, h, pixels, 0, w);
            pg.grabPixels();
            boolean allBlack = true;
            for (int pixel : pixels) {
                Color color = new Color(pixel);
                if (color.getRed() != color.getGreen() || color.getRed() != color.getBlue()) {
                    allBlack = false;
                    break;
                }
            }

            return allBlack;
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return false;
    }

    public static boolean solidColor(File pathToFile) {
        boolean ret = true;

        try {
            Image icon = new ImageIcon(ImageIO.read(pathToFile)).getImage();
            int w = icon.getWidth(null);
            int h = icon.getHeight(null);
            int[] pixels = new int[w * h];
            PixelGrabber pg = new PixelGrabber(icon, 0, 0, w, h, pixels, 0, w);
            pg.grabPixels();
            Color firstColor = new Color(pixels[0]);
            for (int i = 1 ; i < pixels.length ; i++) {
                if (!new Color(pixels[i]).equals(firstColor)) {
                    ret = false;
                    break;
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return ret;
    }

    public static boolean imageIconsEqual(ImageIcon first, ImageIcon second) {
        boolean ret = true;

        try {
            Image firstImage = first.getImage();
            Image secondImage = second.getImage();

            int w1 = firstImage.getWidth(null);
            int h1 = firstImage.getHeight(null);

            int w2 = secondImage.getWidth(null);
            int h2 = secondImage.getHeight(null);

            if (w1 != w2 || h1 != h2) {
                ret = false;
            } else {
                int[] pixels1 = new int[w1 * h1];
                int[] pixels2 = new int[w2 * h2];

                PixelGrabber pg1 = new PixelGrabber(firstImage, 0, 0, w1, h2, pixels1, 0, w1);
                pg1.grabPixels();

                PixelGrabber pg2 = new PixelGrabber(secondImage, 0, 0, w2, h2, pixels2, 0, w2);
                pg2.grabPixels();

                if (pixels1.length != pixels2.length) {
                    ret = false;
                } else {
                    for (int i = 1 ; i < pixels1.length ; i++) {
                        if (pixels1[i] != pixels2[i]) {
                            ret = false;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }

    public static boolean imagesEqual(Image firstImage, Image secondImage) {
        boolean ret = true;

        try {
            int w1 = firstImage.getWidth(null);
            int h1 = firstImage.getHeight(null);

            int w2 = secondImage.getWidth(null);
            int h2 = secondImage.getHeight(null);

            if (w1 != w2 || h1 != h2) {
                ret = false;
            } else {
                int[] pixels1 = new int[w1 * h1];
                int[] pixels2 = new int[w2 * h2];

                PixelGrabber pg1 = new PixelGrabber(firstImage, 0, 0, w1, h2, pixels1, 0, w1);
                pg1.grabPixels();

                PixelGrabber pg2 = new PixelGrabber(secondImage, 0, 0, w2, h2, pixels2, 0, w2);
                pg2.grabPixels();

                if (pixels1.length != pixels2.length) {
                    ret = false;
                } else {
                    for (int i = 1 ; i < pixels1.length ; i++) {
                        if (pixels1[i] != pixels2[i]) {
                            ret = false;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }
}