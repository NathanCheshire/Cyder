package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.enums.Direction;
import cyder.enums.Dynamic;
import cyder.enums.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.math.AngleUtil;
import cyder.network.NetworkUtil;
import cyder.process.Program;
import cyder.snakes.PythonArgument;
import cyder.snakes.PythonCommand;
import cyder.snakes.PythonFunctionsWrapper;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadFactory;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.frame.CyderFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static cyder.strings.CyderStrings.quote;
import static cyder.strings.CyderStrings.space;

/**
 * Static utility methods revolving around Image manipulation.
 */
@SuppressWarnings("unused") /* jpg formats */
public final class ImageUtil {
    /**
     * The name of the thread which blurs an image.
     */
    private static final String GAUSSIAN_IMAGE_BLURER_THREAD_NAME = "Gaussian Image Blurer Thread";

    /**
     * Suppress default constructor.
     */
    private ImageUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Pixelates the provided bufferedImage.
     *
     * @param imageToPixelate the image to pixelate
     * @param pixelSize       the number of old pixels to represent a single new "pixel"
     * @return a buffered image in the same size as the original with new, bigger pixel blocks
     */
    public static BufferedImage pixelateImage(BufferedImage imageToPixelate, int pixelSize) {
        Preconditions.checkNotNull(imageToPixelate);
        Preconditions.checkArgument(pixelSize > 1);

        BufferedImage pixelateImage = new BufferedImage(
                imageToPixelate.getWidth(),
                imageToPixelate.getHeight(),
                imageToPixelate.getType());

        for (int y = 0 ; y < imageToPixelate.getHeight() ; y += pixelSize) {
            for (int x = 0 ; x < imageToPixelate.getWidth() ; x += pixelSize) {
                BufferedImage croppedImage = cropImage(imageToPixelate, x, y, pixelSize, pixelSize);
                Color dominantColor = ColorUtil.getDominantColor(croppedImage);

                for (int yd = y ; (yd < y + pixelSize) && (yd < pixelateImage.getHeight()) ; yd++) {
                    for (int xd = x ; (xd < x + pixelSize) && (xd < pixelateImage.getWidth()) ; xd++) {
                        pixelateImage.setRGB(xd, yd, dominantColor.getRGB());
                    }
                }

            }
        }

        return pixelateImage;
    }

    /**
     * Crops the specified bufferedImage to the new bounds and returns a new buffered image.
     *
     * @param image  the buffered image to crop
     * @param x      the starting x pixel within the image
     * @param y      the starting y pixel within the image
     * @param width  the width of the new image
     * @param height the height of the new image
     * @return the requested cropped image
     */
    public static BufferedImage cropImage(BufferedImage image,
                                          int x, int y, int width, int height) {
        Preconditions.checkNotNull(image);
        Preconditions.checkArgument(x >= 0);
        Preconditions.checkArgument(y >= 0);
        Preconditions.checkArgument(width <= image.getWidth());
        Preconditions.checkArgument(height <= image.getHeight());

        if (x == 0 && y == 0 && width == image.getWidth() && height == image.getHeight()) {
            return image;
        }

        if (x + width > image.getWidth()) {
            x = 0;
            width = image.getWidth();
        }

        if (y + height > image.getHeight()) {
            y = 0;
            height = image.getHeight();
        }

        return image.getSubimage(x, y, width, height);
    }

    /**
     * Crops the specified ImageIcon to the new bounds and returns a new ImageIcon.
     *
     * @param image  the ImageIcon image to crop
     * @param x      the starting x pixel within the image
     * @param y      the starting y pixel within the image
     * @param width  the width of the new image
     * @param height the height of the new image
     * @return the requested cropped image
     */
    public static ImageIcon cropImage(ImageIcon image,
                                      int x, int y, int width, int height) {
        Preconditions.checkNotNull(image);
        Preconditions.checkArgument(x >= 0);
        Preconditions.checkArgument(y >= 0);
        Preconditions.checkArgument(width <= image.getIconWidth());
        Preconditions.checkArgument(height <= image.getIconHeight());

        if (x + width > image.getIconWidth()) {
            x = 0;
            width = image.getIconWidth();
        }

        if (y + height > image.getIconHeight()) {
            y = 0;
            height = image.getIconHeight();
        }

        return toImageIcon(toBufferedImage(image).getSubimage(x, y, width, height));
    }

    /**
     * Returns a buffered image of the specified color.
     *
     * @param color  the color of the requested image
     * @param width  the width of the requested image
     * @param height the height of the requested image
     * @return the buffered image of the provided color and dimensions
     */
    public static BufferedImage bufferedImageFromColor(Color color, int width, int height) {
        Preconditions.checkNotNull(color);
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);

        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bi.createGraphics();

        graphics.setPaint(color);
        graphics.fillRect(0, 0, width, height);

        return bi;
    }

    /**
     * Returns an ImageIcon of the requested color.
     *
     * @param color  the color of the requested image
     * @param width  the width of the requested image
     * @param height the height of the requested image
     * @return the image of the requested color and dimensions
     */
    public static ImageIcon imageIconFromColor(Color color, int width, int height) {
        Preconditions.checkNotNull(color);
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);

        BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics();
        g.setPaint(color);
        g.fillRect(0, 0, width, height);
        return new ImageIcon(im);
    }

    /**
     * Returns an ImageIcon of the requested color of the size 1x1.
     *
     * @param color the color of the requested image
     * @return the image of the requested color and dimensions
     */
    public static ImageIcon imageIconFromColor(Color color) {
        return imageIconFromColor(color, 1, 1);
    }

    /**
     * Resizes the provided ImageIcon to the requested dimensions.
     *
     * @param width  the width of the requested image
     * @param height the height of the requested image
     * @param icon   the ImageIcon to resize
     * @return the resized image
     */
    public static BufferedImage resizeImage(int width, int height, ImageIcon icon) {
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        Preconditions.checkNotNull(icon);

        BufferedImage ReturnImage = null;

        try {
            Image ConsoleImage = icon.getImage();
            Image TransferImage = ConsoleImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ReturnImage = new BufferedImage(TransferImage.getWidth(null),
                    TransferImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D bGr = ReturnImage.createGraphics();

            bGr.drawImage(TransferImage, 0, 0, null);
            bGr.dispose();
            return ReturnImage;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ReturnImage;
    }

    /**
     * Returns the image at the provided location resized.
     *
     * @param width     the width to resize to
     * @param height    the height to resize to
     * @param imageFile the File representing an image
     * @return the resized image
     */
    public static BufferedImage resizeImage(int width, int height, File imageFile) {
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        Preconditions.checkNotNull(imageFile);

        BufferedImage ReturnImage = null;

        try {
            Image ConsoleImage = read(imageFile);
            Image TransferImage = ConsoleImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ReturnImage = new BufferedImage(TransferImage.getWidth(null),
                    TransferImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D bGr = ReturnImage.createGraphics();

            bGr.drawImage(TransferImage, 0, 0, null);
            bGr.dispose();
            return ReturnImage;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ReturnImage;
    }

    /**
     * Returns the buffered image converted to an ImageIcon.
     *
     * @param image a buffered image to convert
     * @return the image icon after conversion
     */
    public static ImageIcon toImageIcon(BufferedImage image) {
        Preconditions.checkNotNull(image);

        return new ImageIcon(image);
    }

    /**
     * Converts the provided ImageIcon to a BufferedImage.
     *
     * @param icon the image icon to convert
     * @return the buffered image after converting
     */
    public static BufferedImage toBufferedImage(ImageIcon icon) {
        Preconditions.checkNotNull(icon);

        BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        return bi;
    }

    /**
     * The degrees representing a default image.
     */
    private static final int ZERO_DEGREES = 0;

    /**
     * The degrees representing a singular right rotation.
     */
    private static final int NINETY_DEGREES = 90;

    /**
     * The degrees representing a double right or left rotation.
     */
    private static final int ONE_EIGHTY_DEGREES = 180;

    /**
     * Returns the rotated background file.
     *
     * @param filepath  the path to the file
     * @param direction the direction of rotation
     * @return the rotated image
     * @throws IllegalArgumentException if the buffered image cannot be loaded from the provided path
     */
    public static BufferedImage getRotatedImage(String filepath, Direction direction) {
        Preconditions.checkNotNull(filepath);
        Preconditions.checkArgument(!filepath.isEmpty());
        Preconditions.checkNotNull(direction);

        BufferedImage bufferedImage = null;

        try {
            bufferedImage = read(new File(filepath));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        if (bufferedImage == null) {
            throw new IllegalArgumentException("Could not get buffered image from path: " + filepath);
        }

        return switch (direction) {
            case TOP -> rotateImage(bufferedImage, ZERO_DEGREES);
            case RIGHT -> rotateImage(bufferedImage, NINETY_DEGREES);
            case BOTTOM -> rotateImage(bufferedImage, ONE_EIGHTY_DEGREES);
            case LEFT -> rotateImage(bufferedImage, -NINETY_DEGREES);
        };
    }

    /**
     * Rotates the provided buffered image by the requested angle in degrees.
     *
     * @param img     the buffered image to rotate
     * @param degrees the angle to rotate by in degrees
     * @return the rotated image
     */
    public static BufferedImage rotateImage(BufferedImage img, double degrees) {
        Preconditions.checkNotNull(img);

        degrees = AngleUtil.normalizeAngle360(degrees);

        double rads = Math.toRadians(degrees);

        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));

        int w = img.getWidth();
        int h = img.getHeight();

        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2.0, (newHeight - h) / 2.0);

        at.rotate(rads, w / 2.0, h / 2.0);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    /**
     * Rotates the provided ImageIcon by the requested angle in degrees
     *
     * @param imageIcon the image icon to rotate
     * @param degrees   the angle to rotate by in degrees
     * @return the rotated image
     */
    public static ImageIcon rotateImage(ImageIcon imageIcon, double degrees) {
        BufferedImage img = toBufferedImage(imageIcon);

        degrees = AngleUtil.normalizeAngle360(degrees);

        double rads = Math.toRadians(degrees);

        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));

        int w = img.getWidth();
        int h = img.getHeight();

        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2.0, (newHeight - h) / 2.0);

        at.rotate(rads, w / 2.0, h / 2.0);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return new ImageIcon(rotated);
    }

    /**
     * The title of the draw buffered image frame.
     */
    private static final String defaultDrawBufferedImageTitle = "BufferedImage";

    /**
     * Draws the provided buffered image to a CyderFrame and displays it.
     *
     * @param bi the buffered image to display
     * @return the frame reference
     */
    @CanIgnoreReturnValue
    public static CyderFrame drawImage(BufferedImage bi) {
        Preconditions.checkNotNull(bi);

        return drawImage(new ImageIcon(bi), defaultDrawBufferedImageTitle);
    }

    /**
     * Draws the provided buffered image to a CyderFrame and displays it.
     *
     * @param bi         the buffered image to display
     * @param frameTitle the title of the frame
     * @return the frame reference
     */
    @CanIgnoreReturnValue
    public static CyderFrame drawImage(BufferedImage bi, String frameTitle) {
        Preconditions.checkNotNull(bi);
        Preconditions.checkNotNull(frameTitle);
        Preconditions.checkArgument(!frameTitle.isEmpty());

        return drawImage(new ImageIcon(bi), frameTitle);
    }

    /**
     * The title of the draw image icon frame.
     */
    private static final String defaultDrawImageIconTitle = "ImageIcon";

    /**
     * Draws the provided image icon to a CyderFrame and displays it.
     *
     * @param icon the icon to display
     * @return the frame reference
     */
    @CanIgnoreReturnValue
    public static CyderFrame drawImage(ImageIcon icon) {
        Preconditions.checkNotNull(icon);

        return drawImage(icon, defaultDrawImageIconTitle);
    }

    /**
     * Draws the provided image icon to a CyderFrame and displays it.
     *
     * @param icon       the icon to display
     * @param frameTitle the title of the frame
     * @return the frame reference
     */
    @CanIgnoreReturnValue
    public static CyderFrame drawImage(ImageIcon icon, String frameTitle) {
        Preconditions.checkNotNull(icon);
        Preconditions.checkNotNull(frameTitle);
        Preconditions.checkArgument(!frameTitle.isEmpty());

        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();

        CyderFrame frame = new CyderFrame(iconWidth + 2 * CyderFrame.BORDER_LEN,
                icon.getIconHeight() + CyderFrame.BORDER_LEN + CyderDragLabel.DEFAULT_HEIGHT);
        frame.setTitle(CyderStrings.openingBracket + icon.getIconWidth() + CyderStrings.X + icon.getIconHeight()
                + CyderStrings.closingBracket + space + frameTitle);

        JLabel label = new JLabel(icon);
        label.setBounds(CyderFrame.BORDER_LEN, CyderDragLabel.DEFAULT_HEIGHT, iconWidth, iconHeight);
        frame.getContentPane().add(label);

        frame.finalizeAndShow();

        return frame;
    }

    /**
     * Resizes the provided buffered image.
     *
     * @param image  the original buffered image to resize
     * @param type   the image type
     * @param width  the width of the new image
     * @param height the height of the new image
     * @return the resized buffered image
     */
    public static BufferedImage resizeImage(BufferedImage image,
                                            int type, int width, int height) {
        Preconditions.checkNotNull(image);
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);

        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
    }

    /**
     * Resizes the provided ImageIcon to have the requested
     * dimensions using bilinear interpolation.
     *
     * @param image the image to resize
     * @param w     the width of the new image
     * @param h     the height of the new image
     * @return the resized image
     */
    public static ImageIcon resizeImage(ImageIcon image, int w, int h) {
        Preconditions.checkNotNull(image);
        Preconditions.checkArgument(w > 0);
        Preconditions.checkArgument(h > 0);

        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(image.getImage(), 0, 0, w, h, null);
        g2.dispose();

        return new ImageIcon(resizedImg);
    }

    /**
     * Combines the provided ImageIcons into one image by placing
     * one relative to the other and taking into account
     * the possible rotation direction provided.
     * <p>
     * The two images must be of the same size in order to merge them into one image.
     *
     * @param newImage  the new image (image to be placed to the direction of the old image)
     * @param oldImage  the old image (image to be placed center)
     * @param direction the direction to place the newImage relative to the oldImage
     * @return the combined image
     */
    public static ImageIcon combineImages(ImageIcon oldImage, ImageIcon newImage, Direction direction) {
        Preconditions.checkNotNull(oldImage);
        Preconditions.checkNotNull(newImage);
        Preconditions.checkNotNull(direction);
        Preconditions.checkArgument(oldImage.getIconWidth() == newImage.getIconWidth());
        Preconditions.checkArgument(oldImage.getIconHeight() == newImage.getIconHeight());

        ImageIcon ret = null;

        try {
            BufferedImage bi1 = toBufferedImage(oldImage);
            BufferedImage bi2 = toBufferedImage(newImage);

            int width;
            int height;
            BufferedImage combined;
            Graphics2D g2;

            switch (direction) {
                case LEFT -> {
                    width = 2 * newImage.getIconWidth();
                    height = newImage.getIconHeight();
                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();
                    g2.drawImage(bi1, null, width / 2, 0);
                    g2.drawImage(bi2, null, 0, 0);
                    g2.dispose();
                }
                case RIGHT -> {
                    width = 2 * newImage.getIconWidth();
                    height = newImage.getIconHeight();
                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();
                    g2.drawImage(bi1, null, 0, 0);
                    g2.drawImage(bi2, null, width / 2, 0);
                    g2.dispose();
                }
                case TOP -> {
                    width = newImage.getIconWidth();
                    height = 2 * newImage.getIconHeight();
                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();
                    g2.drawImage(bi1, null, 0, height / 2);
                    g2.drawImage(bi2, null, 0, 0);
                    g2.dispose();
                }
                case BOTTOM -> {
                    width = newImage.getIconWidth();
                    height = 2 * newImage.getIconHeight();
                    combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    g2 = combined.createGraphics();
                    g2.drawImage(bi1, null, 0, 0);
                    g2.drawImage(bi2, null, 0, height / 2);
                    g2.dispose();
                }
                default -> throw new IllegalArgumentException("Invalid direction: " + direction);
            }

            ret = new ImageIcon(combined);

        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * Returns an image gradient following the provided parameters.
     *
     * @param width        the width of the resulting image
     * @param height       the height of the resulting image
     * @param shadeColor   the color to mix/shade in when merging the left and right colors
     * @param primaryRight the primary color for the left
     * @param primaryLeft  the primary color for the left
     * @return an image gradient
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public static BufferedImage getImageGradient(int width, int height,
                                                 Color shadeColor, Color primaryRight, Color primaryLeft) {
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        Preconditions.checkNotNull(shadeColor);
        Preconditions.checkNotNull(primaryLeft);
        Preconditions.checkNotNull(primaryRight);

        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = ret.createGraphics();

        GradientPaint primary = new GradientPaint(0f, 0f, primaryLeft, height, 0f, primaryRight);
        GradientPaint shade = new GradientPaint(0f, 0f, new Color(shadeColor.getRed(),
                shadeColor.getGreen(), shadeColor.getBlue(), 0), 0f, 600, shadeColor);
        g2.setPaint(primary);
        g2.fillRect(0, 0, width, height);
        g2.setPaint(shade);
        g2.fillRect(0, 0, width, height);

        g2.dispose();

        return ret;
    }

    /**
     * Returns whether the image at the provided path is a gray scale image.
     * This is determined if the for all pixels, the red, green, and blue bits are equal.
     *
     * @param file the path to the image file
     * @return whether the image is gray scale
     * @throws IOException if the provided file could not be read
     */
    public static boolean isGrayscale(File file) throws IOException {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        return isGrayscale(read(file));
    }

    /**
     * Returns whether the provided image is a gray scale image.
     * This is determined if the for all pixels, the red, green, and blue bits are equal.
     *
     * @param bi the image to determine grayscale from
     * @return whether the image is gray scale
     */
    public static boolean isGrayscale(BufferedImage bi) {
        Image icon = new ImageIcon(bi).getImage();

        int w = icon.getWidth(null);
        int h = icon.getHeight(null);
        int[] pixels = new int[w * h];

        PixelGrabber pg = new PixelGrabber(icon, 0, 0, w, h, pixels, 0, w);

        try {
            pg.grabPixels();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        boolean allGrayscale = true;

        for (int pixel : pixels) {
            Color color = new Color(pixel);
            if (color.getRed() != color.getGreen() || color.getRed() != color.getBlue()) {
                allGrayscale = false;
                break;
            }
        }

        return allGrayscale;
    }

    /**
     * Returns the provided image converted to grayscale.
     *
     * @param bi the image to convert to grayscale
     * @return the image converted to grayscale
     */
    public static BufferedImage grayscaleImage(BufferedImage bi) {
        Preconditions.checkNotNull(bi);

        int width = bi.getWidth();
        int height = bi.getHeight();

        BufferedImage ret = new BufferedImage(width, height, bi.getType());

        for (int i = 0 ; i < width ; i++) {
            for (int j = 0 ; j < height ; j++) {
                int p = bi.getRGB(i, j);

                int a = (p >> 24) & 0xff;

                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                int avg = (r + g + b) / 3;

                p = (a << 24) | (avg << 16) | (avg << 8) | avg;

                ret.setRGB(i, j, p);
            }
        }

        return ret;
    }

    /**
     * Returns whether the image represented by the provided file is a solid color.
     *
     * @param file the path to the file
     * @return whether the image is a solid color
     * @throws IOException if the provided file could nto be read from
     */
    public static boolean isSolidColor(File file) throws IOException {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        return isSolidColor(read(file));
    }

    /**
     * Returns whether the image is a solid color.
     *
     * @param bi the buffered image
     * @return whether the image is a solid color
     */
    public static boolean isSolidColor(BufferedImage bi) {
        Preconditions.checkNotNull(bi);

        boolean ret = true;

        try {
            Image icon = new ImageIcon(bi).getImage();
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
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * Returns whether the provided ImageIcons are equal.
     *
     * @param first  the first image icon
     * @param second the second image icon
     * @return whether the provided ImageIcons are equal
     */
    public static boolean areImagesEqual(ImageIcon first, ImageIcon second) {
        Preconditions.checkNotNull(first);
        Preconditions.checkNotNull(second);

        return areImagesEqual(first.getImage(), second.getImage());
    }

    /**
     * Returns whether the two images represent the same pixel data.
     *
     * @param firstImage  the first image
     * @param secondImage the second image
     * @return whether the two images represent the same pixel data
     */
    public static boolean areImagesEqual(Image firstImage, Image secondImage) {
        Preconditions.checkNotNull(firstImage);
        Preconditions.checkNotNull(secondImage);

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
            ExceptionHandler.handle(e);
            ret = false;
        }

        return ret;
    }

    /**
     * Returns whether the provided image is a horizontal image.
     *
     * @param image the image
     * @return whether the provided image is a horizontal image
     */
    public static boolean isHorizontalImage(BufferedImage image) {
        Preconditions.checkNotNull(image);
        return image.getWidth() > image.getHeight();
    }

    /**
     * Returns whether the provided image is a vertical image.
     *
     * @param image the image
     * @return whether the provided image is a vertical image
     */
    public static boolean isVerticalImage(BufferedImage image) {
        Preconditions.checkNotNull(image);
        return image.getWidth() < image.getHeight();
    }

    /**
     * Returns whether the provided image is a square image.
     *
     * @param image the image
     * @return whether the provided image is a square image
     */
    public static boolean isSquareImage(BufferedImage image) {
        Preconditions.checkNotNull(image);
        return image.getWidth() == image.getHeight();
    }

    /**
     * Returns a buffered image for the provided component.
     *
     * @param component the component to take a picture of
     * @return the buffered image representing the provided component
     */
    public static BufferedImage screenshotComponent(Component component) {
        Preconditions.checkNotNull(component);

        BufferedImage image = new BufferedImage(
                component.getWidth(),
                component.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        component.paint(image.getGraphics());

        return image;
    }

    /**
     * Returns whether the two images from the provided files are equal in content.
     *
     * @param file1 the first file
     * @param file2 the second file
     * @return whether the two images from the provided files are equal in content
     */
    public static boolean compareImage(File file1, File file2) {
        Preconditions.checkNotNull(file1);
        Preconditions.checkNotNull(file2);
        Preconditions.checkArgument(file1.exists());
        Preconditions.checkArgument(file2.exists());

        try {
            BufferedImage bi1 = read(file1);
            DataBuffer db1 = bi1.getData().getDataBuffer();
            int size = db1.getSize();

            BufferedImage bi2 = read(file2);
            DataBuffer db2 = bi2.getData().getDataBuffer();
            int size2 = db2.getSize();

            if (size == size2) {
                for (int i = 0 ; i < size ; i++) {
                    if (db1.getElem(i) != db2.getElem(i)) {
                        return false;
                    }
                }

                return true;
            }
        } catch (Exception ignored) {
            return false;
        }

        return false;
    }

    /**
     * Returns whether the provided file is a valid image file meaning it can be read by {@link ImageIO}.
     *
     * @param file the file to check for image validity
     * @return whether the provided file is a valid image file
     */
    public static boolean isValidImage(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(!file.isDirectory());

        try {
            read(file);
            return true;
        } catch (Exception ignored) {}

        return false;
    }

    /**
     * Returns the provided image file after applying a gaussian blur to it.
     *
     * @param imageFile the image file to blur and output a blurred copy in the same directory
     * @param radius    the radius of the Gaussian blur
     * @return the provided image file after applying a gaussian blur
     */
    public static Future<Optional<File>> gaussianBlur(File imageFile, int radius) {
        Preconditions.checkNotNull(imageFile);
        Preconditions.checkArgument(imageFile.exists());
        Preconditions.checkArgument(radius > 2);
        Preconditions.checkArgument(radius % 2 != 0);
        Preconditions.checkState(Program.PYTHON.isInstalled());

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(GAUSSIAN_IMAGE_BLURER_THREAD_NAME)).submit(() -> {
            try {
                String command = PythonArgument.COMMAND.getFullArgument()
                        + space + PythonCommand.BLUR.getCommand()
                        + space + PythonArgument.INPUT.getFullArgument()
                        + space + quote + imageFile.getAbsolutePath() + quote
                        + space + PythonArgument.RADIUS.getFullArgument()
                        + space + radius;
                Future<String> futureResult = PythonFunctionsWrapper.invokeCommand(command);
                while (!futureResult.isDone()) Thread.onSpinWait();
                String result = futureResult.get();

                String parsedResult = PythonCommand.BLUR.parseResponse(result);

                File resultingBlurredImage = new File(parsedResult);
                if (resultingBlurredImage.exists()) {
                    return Optional.of(resultingBlurredImage);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            return Optional.empty();
        });
    }

    /**
     * The maximum alpha value.
     */
    private static final int MAX_ALPHA = 255;

    /**
     * Sets the alpha value of all pixels within the buffered image to the provided value.
     *
     * @param bi    the buffered image to alter
     * @param alpha the alpha value to set all the pixels to
     * @return the altered buffered image
     */
    public static BufferedImage setAlphaOfPixels(BufferedImage bi, int alpha) {
        Preconditions.checkNotNull(bi);
        Preconditions.checkArgument(alpha >= 0);
        Preconditions.checkArgument(alpha <= MAX_ALPHA);

        BufferedImage ret = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int x = 0 ; x < bi.getWidth() ; x++) {
            for (int y = 0 ; y < bi.getHeight() ; y++) {
                int rgb = bi.getRGB(x, y);
                int mc = (alpha << 24) | 0x00ffffff;
                byte colorByte = (byte) (rgb & mc);

                ret.setRGB(x, y, colorByte);
            }
        }

        return ret;
    }

    /**
     * Saves the provided buffered image to the temporary directory.
     *
     * @param bi the buffered image to save
     * @return whether the image was successfully saved
     */
    @CanIgnoreReturnValue
    public static boolean saveImageToTemporaryDirectory(BufferedImage bi, String saveName) {
        Preconditions.checkNotNull(bi);
        Preconditions.checkNotNull(saveName);
        Preconditions.checkArgument(!saveName.isEmpty());

        try {
            File tmpDir = Dynamic.buildDynamic(Dynamic.TEMP.getFileName(),
                    saveName + Extension.PNG.getExtension());
            ImageIO.write(bi, Extension.PNG.getExtensionWithoutPeriod(), tmpDir);
            return true;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return false;
    }

    /**
     * Returns the buffered image read from the provided url.
     *
     * @param url the url to read a buffered image from
     * @return the buffered image read from the provided url
     * @throws IOException if the provided resource cannot be loaded
     */
    public static BufferedImage read(String url) throws IOException {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());
        Preconditions.checkArgument(NetworkUtil.isValidUrl(url));

        return ImageIO.read(new URL(url));
    }

    /**
     * Returns the buffered image read from the provided file.
     *
     * @param file the file
     * @return the buffered image read from the file
     * @throws IOException if the image cannot be read
     */
    public static BufferedImage read(File file) throws IOException {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        return ImageIO.read(file);
    }

    /**
     * Returns the buffered image read from the provided input stream.
     *
     * @param inputStream the input stream
     * @return the buffered image read from the provided input stream
     * @throws IOException if the image cannot be read
     */
    public static BufferedImage read(InputStream inputStream) throws IOException {
        Preconditions.checkNotNull(inputStream);

        return ImageIO.read(inputStream);
    }

    /**
     * Returns a new buffered image resized to fit within the provided dimension.
     *
     * @param image     the image to ensure fits in the provided dimension
     * @param dimension the width and height the image must fit in
     * @return a new buffered image resized to fit within the provided dimension
     */
    public static BufferedImage ensureFitsInBounds(BufferedImage image, Dimension dimension) {
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(dimension);

        BufferedImage ret = copy(image);

        if (isHorizontalImage(image) && image.getWidth() > dimension.getWidth()) {
            float ratio = image.getHeight() / (float) image.getWidth();
            int width = (int) dimension.getWidth();
            int height = (int) (dimension.getHeight() * ratio);
            ret = resizeImage(ret, ret.getType(), width, height);
        } else if (isVerticalImage(image) && image.getHeight() > dimension.getHeight()) {
            float ratio = image.getWidth() / (float) image.getHeight();
            int width = (int) (dimension.getWidth() * ratio);
            int height = (int) dimension.getHeight();
            ret = resizeImage(ret, ret.getType(), width, height);
        } else if (isSquareImage(image) && image.getWidth() > dimension.getWidth()) {
            int len = (int) dimension.getWidth();
            ret = resizeImage(ret, ret.getType(), len, len);
        }

        return ret;
    }

    /**
     * Returns a new ImageIcon resized to fit within the provided dimension.
     *
     * @param icon      the image to ensure fits in the provided dimension
     * @param dimension the width and height the image must fit in
     * @return a new image image resized to fit within the provided dimension
     */
    public static ImageIcon ensureFitsInBounds(ImageIcon icon, Dimension dimension) {
        Preconditions.checkNotNull(icon);
        Preconditions.checkNotNull(dimension);

        return toImageIcon(ensureFitsInBounds(toBufferedImage(icon), dimension));
    }

    /**
     * Returns a copy of the provided image, leaving the reference untouched.
     *
     * @param image the image to copy
     * @return the copied image
     */
    public static BufferedImage copy(BufferedImage image) {
        Preconditions.checkNotNull(image);

        ColorModel colorModel = image.getColorModel();
        boolean isAlphaPreMultiplied = colorModel.isAlphaPremultiplied();
        return new BufferedImage(colorModel, image.copyData(null), isAlphaPreMultiplied, null);
    }

    /**
     * Returns an image icon no bigger than originalIcon x originalIcon.
     *
     * @param originalIcon the icon to resize if needed
     * @param length       the side length of the image
     * @return a new icon that is guaranteed to be at most originalIcon x originalIcon
     */
    public static ImageIcon resizeIfLengthExceeded(ImageIcon originalIcon, int length) {
        Preconditions.checkNotNull(originalIcon);
        Preconditions.checkArgument(length > 0);

        BufferedImage bi = toBufferedImage(originalIcon);

        int width = originalIcon.getIconWidth();
        int height = originalIcon.getIconHeight();

        if (width > height) {
            int scaledHeight = length * height / width;
            return new ImageIcon(bi.getScaledInstance(length, scaledHeight, Image.SCALE_SMOOTH));
        } else if (height > width) {
            int scaledWidth = length * width / height;
            return new ImageIcon(bi.getScaledInstance(scaledWidth, length, Image.SCALE_SMOOTH));
        } else {
            return new ImageIcon(bi.getScaledInstance(length, length, Image.SCALE_SMOOTH));
        }
    }

    /**
     * Returns whether the provided icon is a portrait icon meaning its height is greater than its width.
     *
     * @param icon the icon to test
     * @return whether the provided icon is a portrait photo
     */
    public static boolean isPortraitIcon(ImageIcon icon) {
        Preconditions.checkNotNull(icon);

        return icon.getIconWidth() < icon.getIconHeight();
    }
}
