package com.cyder.utilities;

import java.awt.*;

public class ColorUtil {
    private ColorUtil() {} //private constructor to avoid object creation

    public static Color hextorgbColor(String hex) {
        return new Color(Integer.valueOf(hex.substring(0,2),16),Integer.valueOf(hex.substring(2,4),16),Integer.valueOf(hex.substring(4,6),16));
    }

    public String hextorgbString(String hex) {
        return Integer.valueOf(hex.substring(0,2),16) + "," + Integer.valueOf(hex.substring(2,4),16) + "," + Integer.valueOf(hex.substring(4,6),16);
    }

    public static String rgbtohexString(Color c) {
        return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}
