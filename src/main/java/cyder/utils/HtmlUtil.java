package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.constants.HtmlTags;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.awt.*;
import java.util.regex.Pattern;

/**
 * Utilities related to HTML formatting.
 */
public final class HtmlUtil {
    /**
     * Suppress default constructor.
     */
    private HtmlUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Adds bold tags to the provided string.
     *
     * @param string the string
     * @return the provided string with bold tags surrounding
     */
    public static String applyBold(String string) {
        Preconditions.checkNotNull(string);
        Preconditions.checkArgument(!string.isEmpty());

        return HtmlTags.openingBold + string + HtmlTags.closingBold;
    }

    /**
     * Returns a paragraph tag styled with the provided color for the provided text.
     *
     * @param text  the text to style
     * @param color the color for the styling
     * @return the styled paragraph
     */
    public static String generateColoredHtmlText(String text, Color color) {
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(!text.isEmpty());
        Preconditions.checkNotNull(color);

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        return "<p style=\"color:rgb(" + r + ", " + g + ", " + b + ")\">" + text + HtmlTags.closingP;
    }

    /**
     * Returns the provided string after ensuring it is of the proper form.
     * For example, providing "raspberry" as the text will return:
     * <html><div style = 'text-align: center;'>raspberry</div></html>
     *
     * @param html the text to insert a div style into
     * @return the string with a div style inserted
     */
    public static String addCenteringToHtml(String html) {
        Preconditions.checkNotNull(html);
        Preconditions.checkArgument(!html.isEmpty());

        StringBuilder ret = new StringBuilder();

        if (html.startsWith(HtmlTags.openingHtml)) {
            html = html.substring(HtmlTags.openingHtml.length());
        }

        if (html.endsWith(HtmlTags.closingHtml)) {
            html = html.substring(0, html.length() - HtmlTags.closingHtml.length());
        }

        ret.append(HtmlTags.openingHtml);
        ret.append("<div style='text-align: center;'>");
        ret.append(html);
        ret.append(HtmlTags.closingDiv);
        ret.append(HtmlTags.closingHtml);

        return ret.toString();
    }

    /**
     * Returns whether the provided string contains html.
     *
     * @param text the text
     * @return whether the provided string contains html
     */
    public static boolean containsHtmlStyling(String text) {
        Preconditions.checkNotNull(text);

        Pattern htmlPattern = Pattern.compile(".*<[^>]+>.*", Pattern.DOTALL);
        return htmlPattern.matcher(text).matches();
    }

    /**
     * Surrounds the provided html text with {@link HtmlTags#openingHtml} and {@link HtmlTags#closingHtml}.
     *
     * @param htmlText the html text to surround
     * @return the html text surrounded with html tags
     */
    public static String surroundWithHtmlTags(String htmlText) {
        Preconditions.checkNotNull(htmlText);

        return HtmlTags.openingHtml + htmlText + HtmlTags.closingHtml;
    }

    /**
     * Returns the number of regular words contained in the styled html text.
     *
     * @param htmlStyledText the html styled text
     * @return the number of words
     */
    public static int cleanAndCountWords(String htmlStyledText) {
        Preconditions.checkNotNull(htmlStyledText);
        Preconditions.checkArgument(!htmlStyledText.isEmpty());

        return StringUtil.countWords(Jsoup.clean(htmlStyledText, Safelist.none()));
    }
}
