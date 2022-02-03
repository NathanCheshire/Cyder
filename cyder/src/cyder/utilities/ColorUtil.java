package cyder.utilities;

import cyder.constants.CyderStrings;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilies for things pertaining to colors and color conversions.
 */
public class ColorUtil {
    /**
     * Instantiation of util class not allowed.
     */
    private ColorUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * Converts the provided hex string to a Color object.
     *
     * @param hex the hex string to convert to an object
     * @return the hex string converted to an object
     */
    public static Color hextorgbColor(String hex) {
        //get rid of possible octothorpe
        hex = hex.replace("#","");

        //if shorthand hex notation, convert to official notation
        if (hex.length() == 3) {
            StringBuilder hexBuilder = new StringBuilder();

            for (char c : hex.toCharArray()) {
                hexBuilder.append(c).append(c);
            }

            //if provided hex was "#3F5" it is now "33FF55"
            hex = hexBuilder.toString();
        }

        if (hex.length() < 6) {
            if (hex.length() == 0)
                hex = "000000";

            hex = String.valueOf(hex.charAt(0));
        }

        while (hex.length() < 6) {
            hex += hex.charAt(0);
        }

        return new Color(
                Integer.valueOf(hex.substring(0,2),16),
                Integer.valueOf(hex.substring(2,4),16),
                Integer.valueOf(hex.substring(4,6),16));
    }

    /**
     * Computes and returns the inverse of the provided color.
     *
     * @param color the color to calculate the inverse of
     * @return the inverse of the provided color
     */
    public static Color inverse(Color color) {
        return new Color(255 - color.getRed(),
                255 - color.getGreen(),
                255 - color.getBlue());
    }

    /**
     * Converts the provided hex string representing a color to rgb form.
     *
     * @param hex the hex string to convert to rgb form
     * @return the rgb form of the provided hex color string
     */
    public String hextorgbString(String hex) {
        return Integer.valueOf(hex.substring(0,2),16)
                + "," + Integer.valueOf(hex.substring(2,4),16)
                + "," + Integer.valueOf(hex.substring(4,6),16);
    }

    /**
     * Converts the provided color object into hex string representation.
     *
     * @param c the color to convert to a hex string
     * @return the hex string of the provided color
     */
    public static String rgbtohexString(Color c) {
        return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Determines the dominant color of the provided BufferedImage.
     *
     * @param image the image to find the dominant color of
     * @return the dominant color of the provided image
     */
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

    /**
     * Determines the dominant color of the provided ImageIcon.
     *
     * @param imageIcon the image to find the dominant color of
     * @return the dominant color of the provided image
     */
    public static Color getDominantColor(ImageIcon imageIcon) {
        BufferedImage bi = ImageUtil.getBi(imageIcon);

        Map<Integer, Integer> colorCounter = new HashMap<>(100);

        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                int currentRGB = bi.getRGB(x, y);
                int count = colorCounter.getOrDefault(currentRGB, 0);
                colorCounter.put(currentRGB, count + 1);
            }
        }

        return getDominantColor(colorCounter);
    }

    /**
     * Calculates the opposite dominant color of the provided image.
     *
     * @param image the provided image to calculate the dominant color inverse of
     * @return the opposite of the dominant color of the provided image
     */
    public static Color getDominantColorOpposite(BufferedImage image) {
        Color c = getDominantColor(image);
        return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), c.getAlpha());
    }

    /**
     * Calculates the opposite dominant color of the provided image.
     *
     * @param image the image to calculate the dominant opposite color of
     * @return the opposite dominant color of the provided image
     */
    public static Color getDominantColorOpposite(ImageIcon image) {
        Color c = getDominantColor(image);
        return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), c.getAlpha());
    }

    /**
     * Finds the dominant color of the provided color counter.
     * Used for calcualting the dominant color of an image.
     *
     * @param colorCounter the color counter object to use to calculate the dominant rgb value
     * @return the dominant color
     */
    public static Color getDominantColor(Map<Integer, Integer> colorCounter) {
        int dominantRGB = colorCounter.entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
                .get()
                .getKey();

        return new Color(dominantRGB);
    }

    /**
     * Calculates the opposite color of the provided color.
     *
     * @param c the color to calculate the opposite of
     * @return the opposite of the provided color
     */
    public static Color getOppositeColor(Color c) {
        return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), c.getAlpha());
    }
}
