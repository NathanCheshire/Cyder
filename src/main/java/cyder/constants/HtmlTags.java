package cyder.constants;

import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;

/**
 * HTML tags.
 */
@SuppressWarnings("unused")
public final class HtmlTags {
    /**
     * The opening character for html tags.
     */
    public static final String opening = "<";

    /**
     * The closing character for html tags.
     */
    public static final String closing = ">";

    /**
     * An opening paragraph tag.
     */
    public static final String openingP = "<p>";

    /**
     * A closing paragraph tag.
     */
    public static final String closingP = "</p>";

    /**
     * An opening html tag.
     */
    public static final String openingHtml = "<html>";

    /**
     * A closing html tag.
     */
    public static final String closingHtml = "</html>";

    /**
     * An opening div tag.
     */
    public static final String openingDiv = "<div>";

    /**
     * A closing div tag.
     */
    public static final String closingDiv = "</div>";

    /**
     * A break tag.
     */
    public static final String breakTag = "<br/>";

    /**
     * A opening bold tag.
     */
    public static final String openingBold = "<b>";

    /**
     * A closing bold tag.
     */
    public static final String closingBold = "</b>";

    /**
     * An opening div tag with the text-align property set to center and the vertical-align property set to bottom.
     */
    public static final String divTextAlignCenterVerticalAlignBottom =
            "<div style='text-align: center; vertical-align:bottom'>";

    /**
     * Suppress default constructor.
     */
    private HtmlTags() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
