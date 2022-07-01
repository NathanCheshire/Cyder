package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities to color operations and conversions.
 */
public class ColorUtil {
    /**
     * Suppress default constructor.
     */
    private ColorUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The base for hexadecimal numbers.
     */
    public static final int HEX_BASE = 16;

    /**
     * Converts the provided hex string to a {@link Color} object.
     * Shorthand hex notation may be used. For example, passing "#345", "345", "334455",
     * or "#334455" will all return the same result.
     *
     * @param hex the hex string to convert to an object
     * @return the hex string converted to an object
     * @throws IllegalArgumentException if the provided string is null, empty, or not of length three or six
     */
    public static Color hexStringToColor(String hex) {
        checkNotNull(hex);
        checkArgument(!hex.isEmpty());

        hex = hex.replace("#", "");
        Preconditions.checkArgument(hex.length() == 3 || hex.length() == 6);

        // If shorthand notation, expand to full
        if (hex.length() == 3) {
            StringBuilder hexBuilder = new StringBuilder();

            for (char c : hex.toCharArray()) {
                hexBuilder.append(c).append(c);
            }

            hex = hexBuilder.toString();
        }

        return new Color(
                Integer.valueOf(hex.substring(0, 2), HEX_BASE),
                Integer.valueOf(hex.substring(2, 4), HEX_BASE),
                Integer.valueOf(hex.substring(4, 6), HEX_BASE));
    }

    /**
     * The maximum integer value a color can hold in any one of its red, green,
     * or blue values (the unsigned 8-bit integer limit).
     */
    public static final int BASE_8_LIMIT = 255;

    /**
     * Computes and returns the inverse of the provided color.
     * This is done by subtracting the provided red, green, and blue colors
     * from 255 and using the resulting numbers to form the new color.
     *
     * @param color the color to calculate the inverse of
     * @return the inverse of the provided color
     */
    public static Color getInverseColor(Color color) {
        checkNotNull(color);

        return new Color(BASE_8_LIMIT - color.getRed(),
                BASE_8_LIMIT - color.getGreen(),
                BASE_8_LIMIT - color.getBlue());
    }

    /**
     * Converts the provided hex string representing a color to rgb form.
     * Note the hex string must be in six-digit standard hex form.
     *
     * @param hex the hex string to convert to rgb form
     * @return the rgb form of the provided hex color string
     */
    public String hexToRgbString(String hex) {
        checkNotNull(hex);

        hex = hex.replace("#","");

        checkArgument(hex.length() == 6);

        return Integer.valueOf(hex.substring(0, 2), HEX_BASE)
                + "," + Integer.valueOf(hex.substring(2, 4), HEX_BASE)
                + "," + Integer.valueOf(hex.substring(4, 6), HEX_BASE);
    }

    /**
     * The string formatter used to convert a {@link Color} to a hex string.
     */
    private static final String RGB_TO_HEX_FORMAT = "%02X%02X%02X";

    /**
     * Converts the provided color object into hex string representation.
     *
     * @param color the color to convert to a hex string
     * @return the hex string of the provided color
     */
    public static String rgbToHexString(Color color) {
        checkNotNull(color);

        return String.format(RGB_TO_HEX_FORMAT, color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Returns the dominant color of the provided BufferedImage.
     *
     * @param image the image to find the dominant color of
     * @return the dominant color of the provided image
     */
    public static Color getDominantColor(BufferedImage image) {
        checkNotNull(image);

        Map<Integer, Integer> colorCounter = new HashMap<>(100);

        for (int x = 0 ; x < image.getWidth() ; x++) {
            for (int y = 0 ; y < image.getHeight() ; y++) {
                int currentRGB = image.getRGB(x, y);
                int count = colorCounter.getOrDefault(currentRGB, 0);
                colorCounter.put(currentRGB, count + 1);
            }
        }

        return getDominantColor(colorCounter);
    }

    /**
     * Determines the dominant color of the provided ImageIcon.
     *
     * @param imageIcon the image to find the dominant color of
     * @return the dominant color of the provided image
     */
    public static Color getDominantColor(ImageIcon imageIcon) {
        checkNotNull(imageIcon);
        return getDominantColor(ImageUtil.getBufferedImage(imageIcon));
    }

    /**
     * Calculates the opposite dominant color of the provided image.
     *
     * @param image the provided image to calculate the dominant color inverse of
     * @return the opposite of the dominant color of the provided image
     */
    public static Color getDominantColorInverse(BufferedImage image) {
        checkNotNull(image);
        return getInverseColor(getDominantColor(image));
    }

    /**
     * Calculates the opposite dominant color of the provided image.
     *
     * @param image the provided image to calculate the dominant color inverse of
     * @return the opposite of the dominant color of the provided image
     */
    public static Color getDominantColorInverse(ImageIcon image) {
        checkNotNull(image);
        return getInverseColor(getDominantColor(ImageUtil.toBufferedImage(image)));
    }

    /**
     * Blends the two colors together using the provided ratio of c1 to c2.
     *
     * @param c1    the first color to blend
     * @param c2    the second color to blend
     * @param ratio the blend ratio
     * @return the blended color
     */
    public static Color blendColors(int c1, int c2, float ratio) {
        if (ratio > 1f) {
            ratio = 1f;
        } else if (ratio < 0f) {
            ratio = 0f;
        }

        float iRatio = 1.0f - ratio;

        int a1 = (c1 >> 24 & 0xff);
        int r1 = ((c1 & 0xff0000) >> 16);
        int g1 = ((c1 & 0xff00) >> 8);
        int b1 = (c1 & 0xff);

        int a2 = (c2 >> 24 & 0xff);
        int r2 = ((c2 & 0xff0000) >> 16);
        int g2 = ((c2 & 0xff00) >> 8);
        int b2 = (c2 & 0xff);

        int a = (int) ((a1 * iRatio) + (a2 * ratio));
        int r = (int) ((r1 * iRatio) + (r2 * ratio));
        int g = (int) ((g1 * iRatio) + (g2 * ratio));
        int b = (int) ((b1 * iRatio) + (b2 * ratio));

        return new Color(a << 24 | r << 16 | g << 8 | b);
    }

    /**
     * Returns the gray-scale text color which should be used when overlaying
     * text on the provided buffered image.
     *
     * @param bi the buffered image
     * @return the gray-scale text color to use
     */
    public static Color getSuitableOverlayTextColor(BufferedImage bi) {
        return getInverseColor(getDominantGrayscaleColor(bi));
    }

    /**
     * Returns the dominant color of the provided buffered image gray-scaled.
     *
     * @param bi the buffered image
     * @return the closest gray-scale color the provided buffered image's dominant color
     */
    public static Color getDominantGrayscaleColor(BufferedImage bi) {
        Color dominant = getDominantColor(bi);
        int avg = (dominant.getRed() + dominant.getGreen() + dominant.getBlue()) / 3;
        return new Color(avg, avg, avg);
    }

    /**
     * Finds the dominant color of the provided color counter.
     * Used for calculating the dominant color of an image.
     *
     * @param colorCounter the color counter object to use to calculate the dominant rgb value
     * @return the dominant color
     */
    @SuppressWarnings({"ComparatorMethodParameterNotUsed", "OptionalGetWithoutIsPresent"})
    private static Color getDominantColor(Map<Integer, Integer> colorCounter) {
        checkNotNull(colorCounter);

        int dominantRGB = colorCounter.entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
                .get()
                .getKey();

        return new Color(dominantRGB);
    }
}
