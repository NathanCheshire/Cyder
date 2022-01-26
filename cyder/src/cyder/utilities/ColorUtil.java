package cyder.utilities;

import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.handlers.internal.ExceptionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class ColorUtil {
    private ColorUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    } //private constructor to avoid object creation

    public static Color hextorgbColor(String hex) {
        if (hex.length() < 6) {
            if (hex.length() == 0)
                hex = "000000";

            hex = String.valueOf(hex.charAt(0));
        }

        while (hex.length() < 6) {
            hex += hex.charAt(0);
        }

        return new Color(Integer.valueOf(hex.substring(0,2),16),Integer.valueOf(hex.substring(2,4),16),Integer.valueOf(hex.substring(4,6),16));
    }

    public static Color inverse(Color color) {
        return new Color(255 - color.getRed(),
                255 - color.getGreen(),
                255 - color.getBlue());
    }

    public String hextorgbString(String hex) {
        return Integer.valueOf(hex.substring(0,2),16) + "," + Integer.valueOf(hex.substring(2,4),16) + "," + Integer.valueOf(hex.substring(4,6),16);
    }

    public static String rgbtohexString(Color c) {
        return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    public static Color whiteOrBlack(BufferedImage bi) {
        Color ret = null;

        try {
            Color backgroundDom = getDominantColor(bi);

            if ((backgroundDom.getRed() * 0.299 + backgroundDom.getGreen()
                    * 0.587 + backgroundDom.getBlue() * 0.114) > 186) {
                ret = CyderColors.defaultLightModeTextColor;
            } else {
                ret = CyderColors.defaultDarkModeTextColor;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            return ret;
        }
    }

    public static Color whiteOrBlack(ImageIcon ico) {
        Color ret = null;

        try {
            Color backgroundDom = getDominantColor(ImageUtil.getBi(ico));

            if ((backgroundDom.getRed() * 0.299 + backgroundDom.getGreen()
                    * 0.587 + backgroundDom.getBlue() * 0.114) > 186) {
                ret = CyderColors.defaultLightModeTextColor;
            } else {
                ret = CyderColors.defaultDarkModeTextColor;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            return ret;
        }
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

    public static Color getDominantColorOpposite(BufferedImage image) {
        Color c = getDominantColor(image);
        return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), c.getAlpha());
    }

    public static Color getDominantColorOpposite(ImageIcon image) {
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

    public static Color getOppositeColor(Color c) {
        return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), c.getAlpha());
    }
}
