package cyder.utilities;

import cyder.consts.CyderColors;
import cyder.handlers.ErrorHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ColorUtil {
    private ColorUtil() {} //private constructor to avoid object creation

    public static Color hextorgbColor(String hex) {
        if (hex.length() < 6)
            hex = String.valueOf(hex.charAt(0));

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
            Color backgroundDom = ImageUtil.getDominantColor(bi);

            if ((backgroundDom.getRed() * 0.299 + backgroundDom.getGreen()
                    * 0.587 + backgroundDom.getBlue() * 0.114) > 186) {
                ret = CyderColors.textBlack;
            } else {
                ret = CyderColors.textWhite;
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }

    public static Color whiteOrBlack(ImageIcon ico) {
        Color ret = null;

        try {
            Color backgroundDom = ImageUtil.getDominantColor(ImageUtil.getBi(ico));

            if ((backgroundDom.getRed() * 0.299 + backgroundDom.getGreen()
                    * 0.587 + backgroundDom.getBlue() * 0.114) > 186) {
                ret = CyderColors.textBlack;
            } else {
                ret = CyderColors.textWhite;
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }
}
