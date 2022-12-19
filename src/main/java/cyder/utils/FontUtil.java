package main.java.cyder.utils;

import com.google.common.collect.Range;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.props.Props;
import main.java.cyder.strings.CyderStrings;

import java.awt.*;

/**
 * Utilities related to {@link java.awt.Font}s.
 */
public final class FontUtil {
    /**
     * The allowable range for font metrics.
     * These consist of the following and their additions:
     * <ul>
     *     <li>{@link Font#PLAIN}</li>
     *     <li>{@link Font#BOLD}</li>
     *     <li>{@link Font#ITALIC}</li>
     * </ul>
     */
    public static final Range<Integer> fontMetricRange = Range.closed(0, 3);

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

    /**
     * Returns whether the provided font metric is within the allowable range,
     * that of Font.PLAIN, Font.BOLD, Font.ITALIC or a combination of these.
     * In other words, the provided metric must be contained in {@link #fontMetricRange}.
     *
     * @param metric the font metric
     * @return whether the provided metric is in the allowable bounds
     */
    public static boolean isValidFontMetric(int metric) {
        return fontMetricRange.contains(metric);
    }
}
