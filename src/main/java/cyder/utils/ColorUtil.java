package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import cyder.exceptions.IllegalMethodException;
import cyder.math.NumberUtil;
import cyder.strings.CyderStrings;

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
public final class ColorUtil {
    /**
     * The base for hexadecimal numbers.
     */
    public static final int HEX_BASE = 16;

    /**
     * The length of shorthand hex color strings.
     */
    private static final int shorthandHexLength = 3;

    /**
     * The regular length of hex color strings.
     */
    private static final int hexLength = 6;

    /**
     * The valid lengths a hex color must be.
     */
    private static final ImmutableList<Integer> validHexLengths = ImmutableList.of(shorthandHexLength, hexLength);

    /**
     * The string formatter used to convert a {@link Color} to a hex string.
     */
    private static final String RGB_TO_HEX_FORMAT = "%02X%02X%02X";

    /**
     * The maximum length the hashmap for the get dominant color method can grow.
     * This ensures we are only checking the top 100 colors of an image when determining the dominant color.
     */
    private static final int maxDominantColorCounterHashMapLength = 100;

    /**
     * The minimum opacity.
     */
    private static final int minOpacity = 0;

    /**
     * The maximum opacity.
     */
    private static final int maxOpacity = 255;

    /**
     * The range for opacity values for Java's {@link Color} objects.
     */
    public static final Range<Integer> opacityRange = Range.closed(minOpacity, maxOpacity);

    /**
     * Suppress default constructor.
     */
    private ColorUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

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

        hex = hex.replace(CyderStrings.hash, "");
        Preconditions.checkArgument(validHexLengths.contains(hex.length()));

        if (hex.length() == shorthandHexLength) {
            hex = expandShorthandHexColor(hex);
        }

        int r = Integer.valueOf(hex.substring(0, 2), HEX_BASE);
        int g = Integer.valueOf(hex.substring(2, 4), HEX_BASE);
        int b = Integer.valueOf(hex.substring(4, 6), HEX_BASE);
        return new Color(r, g, b);
    }

    /**
     * Converts a three digit shorthand hex color code into a full six digit hex color code.
     *
     * @param shorthandHex the shorthand hex
     * @return the full six digit hex code
     */
    public static String expandShorthandHexColor(String shorthandHex) {
        checkNotNull(shorthandHex);
        checkArgument(shorthandHex.length() == shorthandHexLength);

        StringBuilder newHex = new StringBuilder();
        shorthandHex.chars().mapToObj(i -> (char) i)
                .forEach(character -> newHex.append(character).append(character));
        return newHex.toString();
    }

    /**
     * Computes and returns the inverse of the provided color.
     * This is done by subtracting the provided red, green, and blue colors
     * from 255 and using the resulting numbers to form the new color.
     *
     * @param color the color to calculate the inverse of
     * @return the inverse of the provided color
     */
    @SuppressWarnings("ConstantConditions") /* Unboxing is safe in this context */
    public static Color getInverseColor(Color color) {
        checkNotNull(color);

        int eightBitLimit = NumberUtil.BIT_LIMITS.get(8);

        return new Color(eightBitLimit - color.getRed(),
                eightBitLimit - color.getGreen(),
                eightBitLimit - color.getBlue());
    }

    /**
     * Converts the provided hex string representing a color to rgb form.
     *
     * @param hex the hex string to convert to rgb form
     * @return the rgb form of the provided hex color string
     */
    public String hexToRgbString(String hex) {
        checkNotNull(hex);

        hex = hex.replace(CyderStrings.hash, "");
        if (hex.length() == shorthandHexLength) hex = expandShorthandHexColor(hex);

        return Integer.valueOf(hex.substring(0, 2), HEX_BASE)
                + CyderStrings.comma + Integer.valueOf(hex.substring(2, 4), HEX_BASE)
                + CyderStrings.comma + Integer.valueOf(hex.substring(4, 6), HEX_BASE);
    }

    /**
     * Converts the provided color into an RGB hex representation.
     *
     * @param color the color to convert to hex representation
     * @return the hex representation of the provided color
     */
    public static String toRgbHexString(Color color) {
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

        Map<Integer, Integer> colorCounter = new HashMap<>(maxDominantColorCounterHashMapLength);

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

        return getDominantColor(ImageUtil.toBufferedImage(imageIcon));
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
     * Returns the gray-scale text color which should be used when overlaying
     * text on the provided buffered image.
     *
     * @param bi the buffered image
     * @return the gray-scale text color to use
     */
    public static Color getSuitableOverlayTextColor(BufferedImage bi) {
        checkNotNull(bi);

        return getInverseColor(getDominantGrayscaleColor(bi));
    }

    /**
     * Returns the dominant color of the provided buffered image gray-scaled.
     *
     * @param bi the buffered image
     * @return the closest gray-scale color the provided buffered image's dominant color
     */
    public static Color getDominantGrayscaleColor(BufferedImage bi) {
        checkNotNull(bi);

        Color dominant = getDominantColor(bi);
        int avg = (dominant.getRed() + dominant.getGreen() + dominant.getBlue()) / 3;
        return new Color(avg, avg, avg);
    }

    /**
     * Returns the middle point of the two colors.
     *
     * @param color1 the first color
     * @param color2 the second color
     * @return the middle point of the two colors
     */
    public static Color getMiddleColor(Color color1, Color color2) {
        checkNotNull(color1);
        checkNotNull(color2);

        int r = color1.getRed() + color2.getRed();
        int g = color1.getGreen() + color2.getGreen();
        int b = color1.getBlue() + color2.getBlue();

        return new Color(r / 2, g / 2, b / 2);
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

    /**
     * Generates a list of eight colors for a transition from the flash color to the provided default color.
     *
     * @param flashColor   the flash color to start with
     * @param defaultColor the default color to fade to
     * @return the list of flash colors fading from flash color to default color
     */
    public static ImmutableList<Color> getFlashColors(Color flashColor, Color defaultColor) {
        checkNotNull(flashColor);
        checkNotNull(defaultColor);
        checkArgument(!flashColor.equals(defaultColor));

        Color middle = getMiddleColor(flashColor, defaultColor);
        Color lessFlash = getMiddleColor(middle, flashColor);
        Color lessDefault = getMiddleColor(middle, defaultColor);

        Color beforeLessFlash = getMiddleColor(lessFlash, flashColor);
        Color afterLessFlash = getMiddleColor(lessFlash, middle);

        Color beforeLessDefault = getMiddleColor(lessDefault, middle);
        Color afterLessDefault = getMiddleColor(lessDefault, defaultColor);

        return ImmutableList.of(flashColor, beforeLessFlash, lessFlash, afterLessFlash,
                middle, beforeLessDefault, lessDefault, afterLessDefault, defaultColor);
    }

    /**
     * Sets the opacity of the provided color to the provided opacity and returns a new color object.
     *
     * @param color   the color
     * @param opacity the opacity of the color
     * @return the color with the requested opacity.
     */
    public static Color setColorOpacity(Color color, int opacity) {
        Preconditions.checkNotNull(color);
        if (opacity > maxOpacity) opacity = maxOpacity;
        if (opacity < minOpacity) opacity = minOpacity;

        return new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }
}
