package cyder.bounds;

import com.google.common.base.Preconditions;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.utils.StringUtil;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.regex.Pattern;

/**
 * Utility methods to calculate the needed space for a String of text.
 */
public final class BoundsUtil {
    /**
     * The default maximum width for returned bounds strings.
     */
    private static final int DEFAULT_MAX_WIDTH = 1200;

    /**
     * A closing paragraph tag.
     */
    public static final String CLOSING_P_TAG = "</p>";

    /**
     * An opening html tag.
     */
    public static final String OPENING_HTML_TAG = "<html>";

    /**
     * A closing html tag.
     */
    public static final String CLOSING_HTML_TAG = "</html>";

    /**
     * A closing div tag.
     */
    public static final String CLOSING_DIV_TAG = "</div>";

    /**
     * A break tag.
     */
    public static final String BREAK_TAG = "<br/>";

    /**
     * A opening bold tag.
     */
    public static final String OPENING_BOLD_TAG = "<b>";

    /**
     * A closing bold tag.
     */
    public static final String CLOSING_BOLD_TAG = "</b>";

    /**
     * The opening html tag char.
     */
    public static final char openingHtmlTagChar = '<';

    /**
     * The closing html tag char.
     */
    public static final char closingHtmlTagChar = '>';

    /**
     * The number of chars to look back or forward in a string to attempt to find a space to replace with a break tag.
     */
    private static final int numLookAroundForSpaceChars = 7;

    /**
     * Suppress default constructor.
     */
    private BoundsUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Calculates the needed width and height necessary to display the provided string using
     * the {@link CyderFonts#DEFAULT_FONT_SMALL} font. The object returned dictates the html-styled
     * string to use which is guaranteed to fit within a max width of {@link #DEFAULT_MAX_WIDTH}.
     * Callers should check to ensure the height is acceptable to their purpose.
     *
     * @param text the string to display
     * @return an object composed of the width, height, and the html-styled text with break tags inserted if needed
     */
    public static BoundsString widthHeightCalculation(String text) {
        return widthHeightCalculation(text, CyderFonts.DEFAULT_FONT_SMALL, DEFAULT_MAX_WIDTH);
    }

    /**
     * Calculates the needed width and height necessary to display the provided string. The object returned
     * dictates the html-styled string to use which is guaranteed to fit within a max width of
     * {@link #DEFAULT_MAX_WIDTH}. Callers should check to ensure the height is acceptable to their purpose.
     *
     * @param text the string to display
     * @param font the font to be used
     * @return an object composed of the width, height, and the html-styled text with break tags inserted if needed
     */
    public static BoundsString widthHeightCalculation(String text, Font font) {
        return widthHeightCalculation(text, font, DEFAULT_MAX_WIDTH);
    }

    /**
     * Calculates the needed width and height necessary to display the provided string. The object returned
     * dictates the html-styled string to use which is guaranteed to fit within the provided maximum width.
     * Callers should check to ensure the height is acceptable to their purpose.
     *
     * @param text     the string to display
     * @param maxWidth the maximum width allowed
     * @param font     the font to be used
     * @return an object composed of the width, height, and the html-styled text with break tags inserted if needed
     */
    public static BoundsString widthHeightCalculation(String text, Font font, int maxWidth) {
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(!text.isEmpty());
        Preconditions.checkNotNull(font);

        int widthAddition = 5;

        FontRenderContext fontRenderContext = new FontRenderContext(new AffineTransform(),
                font.isItalic(), true);
        int lineHeightForFont = StringUtil.getMinHeight(text, font);

        if (containsHtmlStyling(text)) {
            boolean inHtmlTag = false;
            StringBuilder htmlBuilder = new StringBuilder();
            StringBuilder currentLineBuilder = new StringBuilder();
            for (char c : text.toCharArray()) {
                if (c == openingHtmlTagChar && !inHtmlTag) {
                    htmlBuilder.append(currentLineBuilder);
                    currentLineBuilder = new StringBuilder(String.valueOf(c));
                    inHtmlTag = true;
                    continue;
                } else if (c == closingHtmlTagChar && inHtmlTag) {
                    inHtmlTag = false;
                    currentLineBuilder.append(c);

                    htmlBuilder.append(currentLineBuilder);
                    currentLineBuilder = new StringBuilder();

                    continue;
                }

                if (inHtmlTag) {
                    currentLineBuilder.append(c);
                    continue;
                }

                if (currentLineBuilder.toString().endsWith(BREAK_TAG)) {
                    htmlBuilder.append(currentLineBuilder);
                    currentLineBuilder = new StringBuilder(String.valueOf(c));
                    continue;
                }

                int currentLineWidth = StringUtil.getMinWidth(currentLineBuilder + String.valueOf(c), font);
                if (currentLineWidth > maxWidth) {
                    // Sweet, we can just replace the current char with the break tag
                    if (c == ' ') {
                        htmlBuilder.append(currentLineBuilder).append(BREAK_TAG);
                        currentLineBuilder = new StringBuilder();
                    } else {
                        String currentLine = currentLineBuilder.toString();
                        // Ensure we don't look back farther than we can
                        int numLookBack = Math.min(currentLine.length(), numLookAroundForSpaceChars);

                        boolean insertedSpace = false;
                        for (int i = currentLine.length() - 1 ; i > currentLine.length() - numLookBack - 1 ; i--) {
                            if (currentLine.charAt(i) == ' ') {
                                htmlBuilder.append(currentLine, 0, i)
                                        .append(BREAK_TAG)
                                        .append(currentLine.substring(i + 1 >= currentLine.length() ? i : i + 1));
                                currentLineBuilder = new StringBuilder(String.valueOf(c));
                                insertedSpace = true;
                                break;
                            }
                        }

                        // Unfortunately have to break up a word
                        if (!insertedSpace) {
                            htmlBuilder.append(currentLineBuilder).append(BREAK_TAG);
                            currentLineBuilder = new StringBuilder(String.valueOf(c));
                        }
                    }
                } else {
                    currentLineBuilder.append(c);
                }
            }

            htmlBuilder.append(currentLineBuilder);

            String[] lines = htmlBuilder.toString().split(BREAK_TAG);
            int necessaryHeight = lineHeightForFont * lines.length;

            int necessaryWidth = 0;
            for (String line : lines) {
                necessaryWidth = Math.max(necessaryWidth, StringUtil.getMinWidth(
                        Jsoup.clean(line, Safelist.none()), font));
            }

            if (!htmlBuilder.toString().startsWith(OPENING_HTML_TAG)) {
                htmlBuilder.insert(0, OPENING_HTML_TAG);
            }
            if (!htmlBuilder.toString().endsWith(CLOSING_HTML_TAG)) {
                htmlBuilder.append(CLOSING_HTML_TAG);
            }

            return new BoundsString(htmlBuilder.toString(), necessaryWidth, necessaryHeight);
        } else {
            // Non-html so we don't have to worry about where break tags fall
            // Preferably they are not in the middle of words

            String[] lines = text.split(BREAK_TAG);
            StringBuilder nonHtmlBuilder = new StringBuilder();

            for (int i = 0 ; i < lines.length ; i++) {
                int fullLineWidth = StringUtil.getMinWidth(lines[i], font) + widthAddition;
                if (fullLineWidth > maxWidth) {
                    // Number of lines to split the current line into
                    int neededLines = (int) Math.ceil(fullLineWidth / (double) maxWidth);
                    neededLines = Math.max(2, neededLines);

                    nonHtmlBuilder.append(insertBreaks(lines[i], neededLines));
                } else {
                    nonHtmlBuilder.append(lines[i]);
                }

                if (i != lines.length - 1) {
                    nonHtmlBuilder.append(BREAK_TAG);
                }
            }

            String nonHtml = nonHtmlBuilder.toString();
            String[] nonHtmlLines = nonHtml.split(BREAK_TAG);

            int w = 0;
            for (String line : nonHtmlLines) {
                int currentWidth = (int) (font.getStringBounds(line, fontRenderContext).getWidth() + widthAddition);
                w = Math.max(w, currentWidth);
            }

            int h = lineHeightForFont * nonHtmlLines.length;

            if (!nonHtml.startsWith(OPENING_HTML_TAG)) {
                nonHtml = OPENING_HTML_TAG + nonHtml;
            }
            if (!nonHtml.endsWith(CLOSING_HTML_TAG)) {
                nonHtml += CLOSING_HTML_TAG;
            }

            return new BoundsString(nonHtml, w, h);
        }
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
     * Inserts breaks into the text based on the amount of lines needed.
     * Note that HTML tags should NOT exist in this string and should be parsed
     * away prior to invoking this method.
     *
     * @param text          the raw text
     * @param requiredLines the number of lines required
     * @return the text with html line breaks inserted
     * @throws NullPointerException     if the provided text is null
     * @throws IllegalArgumentException if the provided text contains html tags or
     *                                  if the number of lines is less than 1
     */
    public static String insertBreaks(String text, int requiredLines) {
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(!containsHtmlStyling(text), text);
        Preconditions.checkArgument(requiredLines > 0);
        if (requiredLines == 1) return text;

        String ret = text;

        int splitFrequency = (int) Math.ceil((float) text.length() / (float) requiredLines);
        int numChars = text.length();

        int currentNumLines = 1;
        for (int i = splitFrequency ; i < numChars ; i += splitFrequency) {
            if (currentNumLines == requiredLines) break;

            if (ret.charAt(i) == ' ') {
                StringBuilder sb = new StringBuilder(ret);
                sb.deleteCharAt(i);
                sb.insert(i, BREAK_TAG);
                ret = sb.toString();
            } else {
                boolean breakInserted = false;

                // Check right for a space
                for (int j = i ; j < i + numLookAroundForSpaceChars ; j++) {
                    if (j < numChars) {
                        if (ret.charAt(j) == ' ') {
                            StringBuilder sb = new StringBuilder(ret);
                            sb.deleteCharAt(j);
                            sb.insert(j, BREAK_TAG);

                            ret = sb.toString();

                            breakInserted = true;
                            currentNumLines++;
                            break;
                        }
                    }
                }

                if (breakInserted) continue;

                // Check left for a space
                for (int j = i ; j > i - numLookAroundForSpaceChars ; j--) {
                    if (j > 0) {
                        if (ret.charAt(j) == ' ') {
                            StringBuilder sb = new StringBuilder(ret);
                            sb.deleteCharAt(j);
                            sb.insert(j, BREAK_TAG);

                            ret = sb.toString();

                            breakInserted = true;
                            currentNumLines++;
                            break;
                        }
                    }
                }

                if (!breakInserted) {
                    // Unfortunately have to just insert at the current index
                    StringBuilder sb = new StringBuilder(ret);
                    sb.insert(i, BREAK_TAG);
                    ret = sb.toString();
                }
            }
            currentNumLines++;
        }

        return ret;
    }

    /**
     * Returns the provided string after ensuring it is of the proper form.
     * For example, providing "raspberry" as the text will return:
     * <html><div style = 'text-align: center;'>raspberry{@link #CLOSING_DIV_TAG}{@link #CLOSING_HTML_TAG}
     *
     * @param html the text to insert a div style into
     * @return the string with a div style inserted
     */
    public static String addCenteringToHtml(String html) {
        Preconditions.checkNotNull(html);
        Preconditions.checkArgument(!html.isEmpty());

        StringBuilder ret = new StringBuilder();

        if (html.startsWith(OPENING_HTML_TAG)) {
            html = html.substring(OPENING_HTML_TAG.length());
        }

        if (html.endsWith(CLOSING_HTML_TAG)) {
            html = html.substring(0, html.length() - CLOSING_HTML_TAG.length());
        }

        ret.append(OPENING_HTML_TAG);
        ret.append("<div style='text-align: center;'>");
        ret.append(html);
        ret.append(CLOSING_DIV_TAG);
        ret.append(CLOSING_HTML_TAG);

        return ret.toString();
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
        return "<p style=\"color:rgb(" + r + ", " + g + ", " + b + ")\">" + text + CLOSING_P_TAG;
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

        return OPENING_BOLD_TAG + string + CLOSING_BOLD_TAG;
    }
}
