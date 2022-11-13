package cyder.utils;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.props.PropLoader;

import java.awt.*;

/**
 * Utilities related to {@link java.awt.Font}s.
 */
public final class FontUtil {
    /**
     * The key for retrieving the font metric from the props.
     */
    private static final String FONT_METRIC = "font_metric";

    /**
     * Suppress default constructor.
     */
    private FontUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the font metric from the props, {@link Font#BOLD} if absent.
     *
     * @return the font metric from the props
     */
    public static int getFontMetricFromProps() {
        String fontMetricString = PropLoader.getString(FONT_METRIC);

        return switch (fontMetricString.toLowerCase()) {
            case "bold" -> Font.BOLD;
            case "italic" -> Font.ITALIC;
            case "bold italic", "italic bold" -> Font.BOLD + Font.ITALIC;
            default -> Font.PLAIN;
        };
    }
}
