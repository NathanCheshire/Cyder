package cyder.utils;

import cyder.exceptions.IllegalMethodException;
import cyder.props.Props;
import cyder.strings.CyderStrings;

import java.awt.*;

/**
 * Utilities related to {@link java.awt.Font}s.
 */
public final class FontUtil {
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
        String fontMetricString = Props.fontMetric.getValue();

        return switch (fontMetricString.toLowerCase()) {
            case "bold" -> Font.BOLD;
            case "italic" -> Font.ITALIC;
            case "bold italic", "italic bold" -> Font.BOLD + Font.ITALIC;
            default -> Font.PLAIN;
        };
    }
}
