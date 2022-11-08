package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.LinkedList;

/**
 * Utility methods to calculate the needed space for a String of text.
 */
public final class BoundsUtil {
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
     * Suppress default constructor.
     */
    private BoundsUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The default maximum width for returned bounds strings.
     */
    private static final int DEFAULT_MAX_WIDTH = 1200;

    /**
     * Calculates the needed height of an inform/dialog window.
     *
     * @param text the string to display
     * @return an object composed of the width, height, and possibly corrected text to form the bounding box
     * for the provided display string.
     */
    public static BoundsString widthHeightCalculation(String text) {
        return widthHeightCalculation(text, CyderFonts.DEFAULT_FONT_SMALL, DEFAULT_MAX_WIDTH);
    }

    public static BoundsString widthHeightCalculation(String text, Font font) {
        return widthHeightCalculation(text, font, DEFAULT_MAX_WIDTH);
    }

    /**
     * Calculates the needed height for an inform/dialog window given the preferred width and text.
     *
     * @param text     the string to display
     * @param maxWidth the maximum width allowed
     * @param font     the font to be used
     * @return an object composed of the width, height, and possibly corrected text to form the bounding box
     * for the provided display string.
     */
    public static BoundsString widthHeightCalculation(String text, Font font, int maxWidth) {
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(!text.isEmpty());
        Preconditions.checkNotNull(font);

        BoundsString ret;

        int widthAddition = 5;
        int heightAddition = 2;

        // find height for a single line of text
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, font.isItalic(), true);
        int singleLineHeight = (int) font.getStringBounds(text, frc).getHeight() + heightAddition;

        // does the string contain any html? if so we have to be
        // careful where we insert needed line breaks
        String[] parts = text.split(BREAK_TAG);

        // parse away all html except for break tags and add back in between parts
        StringBuilder sb = new StringBuilder();

        for (int j = 0 ; j < parts.length ; j++) {
            sb.append(Jsoup.clean(parts[j], Safelist.none()));

            if (j != parts.length - 1) {
                sb.append(BREAK_TAG);
            }
        }

        // if the regular text length is not equal to the
        // parsed text aside from line breaks then the string is html formatted
        if (text.length() != sb.toString().length()) {
            // init tagged strings
            LinkedList<TaggedString> taggedStrings = new LinkedList<>();
            String textCopy = text;

            // while we still have text
            while ((textCopy.contains("<") && textCopy.contains(">"))) {
                // get indices of the next tag
                int firstOpeningTag = textCopy.indexOf("<");
                int firstClosingTag = textCopy.indexOf(">");

                // failsafe break
                if (firstClosingTag == -1 || firstOpeningTag == -1 || firstClosingTag < firstOpeningTag) {
                    break;
                }

                // get the text and html
                String regularText = textCopy.substring(0, firstOpeningTag);
                String firstHtml = textCopy.substring(firstOpeningTag, firstClosingTag + 1);

                // add tagged strings
                if (!regularText.isEmpty()) {
                    taggedStrings.add(new TaggedString(regularText, TaggedString.Type.TEXT));
                }

                if (!firstHtml.isEmpty()) {
                    taggedStrings.add(new TaggedString(firstHtml, TaggedString.Type.HTML));
                }

                // move text copy along
                textCopy = textCopy.substring(firstClosingTag + 1);
            }

            // if there's remaining text, it's non-html
            if (!textCopy.isEmpty())
                taggedStrings.add(new TaggedString(textCopy, TaggedString.Type.TEXT));

            // now add breaks into the lines that are needed
            for (int i = 0 ; i < taggedStrings.size() ; i++) {
                // if tagged as text
                if (taggedStrings.get(i).type() == TaggedString.Type.TEXT) {
                    // get full line width
                    int fullLineWidth = (int) (font.getStringBounds(taggedStrings.get(i).text(),
                            frc).getWidth() + widthAddition);

                    // evaluate if the line is too long
                    if (fullLineWidth > maxWidth) {
                        // line is too long, figure out how many breaks to add
                        // first, how many multiples of current width does it take to get to max width?
                        int neededLines = (int) Math.ceil((double) fullLineWidth / (double) maxWidth);

                        // if only one line is the result, ensure 2 since it is larger than allowable width
                        neededLines = Math.max(2, neededLines);

                        // set the tagged string text to the insertion of the text with breaks
                        taggedStrings.set(i, new TaggedString(insertBreaks(taggedStrings.get(i).text(),
                                neededLines), taggedStrings.get(i).type()));
                    }
                }
            }

            // recombine text into string
            StringBuilder htmlBuilder = new StringBuilder();

            for (TaggedString tg : taggedStrings) {
                htmlBuilder.append(tg.text());
            }

            // figure out the height based on the number of break tags
            String[] lines = htmlBuilder.toString().split(BREAK_TAG);
            int h = singleLineHeight * lines.length;

            // figure out the width based off of the lines after breaking
            int w = 0;

            // for all lines
            for (TaggedString ts : taggedStrings) {
                // if non format text
                if (ts.type() == TaggedString.Type.HTML) {
                    continue;
                }

                // get breaks of the tagged string
                String[] tsLines = ts.text().split(BREAK_TAG);

                // for all lines
                for (String line : tsLines) {
                    int lineWidth = (int) (font.getStringBounds(line, frc).getWidth() + widthAddition);

                    // if width is greatest so far
                    if (lineWidth > w) {
                        w = lineWidth;
                    }
                }
            }

            String retString = htmlBuilder.toString();

            // if for some reason the text is not surrounded with html tags, add them
            if (!retString.startsWith(OPENING_HTML_TAG)) {
                retString = OPENING_HTML_TAG + retString;
            }

            if (!retString.endsWith(CLOSING_HTML_TAG)) {
                retString += CLOSING_HTML_TAG;
            }

            // now we have max line width, height for all lines, and formatted text
            ret = new BoundsString(w, h, retString);
        }
        // no formatting so we can just add breaks
        // normally without having to worry where we we place them
        else {
            // only contains possible line breaks so split at those
            StringBuilder nonHtmlBuilder = new StringBuilder();
            String[] lines = text.split(BREAK_TAG);

            // for all lines
            for (int i = 0 ; i < lines.length ; i++) {
                int fullLineWidth = (int) (font.getStringBounds(lines[i], frc).getWidth() + widthAddition);

                // evaluate if the line is too long
                if (fullLineWidth > maxWidth) {
                    //line is too long, figure out how many breaks to add
                    //first, how many multiples of current width does it take to get to max width?
                    int neededLines = (int) Math.ceil((double) fullLineWidth / (double) maxWidth);

                    //if only one line is the result somehow, ensure it's 2
                    neededLines = Math.max(2, neededLines);
                    nonHtmlBuilder.append(insertBreaks(lines[i], neededLines));
                }
                // line isn't too long, simply append it
                else {
                    nonHtmlBuilder.append(lines[i]);
                }

                // if not on the last line, add the original break to the end we split at it again
                if (i != lines.length - 1) {
                    nonHtmlBuilder.append(BREAK_TAG);
                }
            }

            // more lines might exist now since we're done adding breaks
            lines = nonHtmlBuilder.toString().split(BREAK_TAG);

            // finally figure out the width and height based
            // on the amount of lines
            int w = 0;
            int h = singleLineHeight * lines.length;
            String correctedNonHtml = nonHtmlBuilder.toString();

            for (String line : lines) {
                int currentWidth = (int) (font.getStringBounds(line, frc).getWidth() + widthAddition);

                if (currentWidth > w) w = currentWidth;
            }

            // if for some reason the text is not surrounded with html tags, add them
            if (!correctedNonHtml.startsWith(OPENING_HTML_TAG)) {
                correctedNonHtml = OPENING_HTML_TAG + correctedNonHtml;
            }

            if (!correctedNonHtml.endsWith(CLOSING_HTML_TAG)) {
                correctedNonHtml += CLOSING_HTML_TAG;
            }

            ret = new BoundsString(w, h, correctedNonHtml);
        }

        return ret;
    }

    /**
     * Inserts breaks into the raw text based on the amount of lines needed.
     * Note that break tags may NOT exist in this string and should be parsed
     * away prior to invoking this method.
     *
     * @param rawText  the raw text
     * @param numLines the number of lines required
     * @return the text with html line breaks inserted
     */
    public static String insertBreaks(String rawText, int numLines) {
        if (numLines == 1) return rawText;

        // The mutated string we will return
        String ret = rawText;

        // The ideal place to split the string is the len divided by the number of chars
        int splitEveryNthChar = (int) Math.ceil((float) rawText.length() / (float) numLines);
        int numChars = rawText.length();

        // We can look for a space within a tolerance
        // of 7 chars both sides of a given char
        int breakInsertionTol = 7;

        // The number of lines in we currently are
        int currentLines = 1;

        for (int i = splitEveryNthChar ; i < numChars ; i += splitEveryNthChar) {
            if (currentLines == numLines) break;

            // Found space at char, insert a break and replace the space
            if (ret.charAt(i) == ' ') {
                StringBuilder sb = new StringBuilder(ret);
                sb.deleteCharAt(i);
                sb.insert(i, BREAK_TAG);
                ret = sb.toString();
            } else {
                boolean spaceFound = false;

                // Check right for a space
                for (int j = i ; j < i + breakInsertionTol ; j++) {
                    // is j valid
                    if (j < numChars) {
                        // is it a space
                        if (ret.charAt(j) == ' ') {
                            StringBuilder sb = new StringBuilder(ret);
                            sb.deleteCharAt(j);
                            sb.insert(j, BREAK_TAG);
                            ret = sb.toString();
                            spaceFound = true;
                            currentLines++;
                            break;
                        }
                    }
                }

                if (spaceFound) continue;

                // Check left for a space
                for (int j = i ; j > i - breakInsertionTol ; j--) {
                    // is j valid
                    if (j > 0) {
                        // is it a space
                        if (ret.charAt(j) == ' ') {
                            StringBuilder sb = new StringBuilder(ret);
                            sb.deleteCharAt(j);
                            sb.insert(j, BREAK_TAG);
                            ret = sb.toString();
                            spaceFound = true;
                            currentLines++;
                            break;
                        }
                    }
                }

                if (spaceFound) continue;

                /*
                    The final resort is to just split at the current index and add a break.
                    There shouldn't be any html formatting at this point so this is safe to do.
                */

                StringBuilder sb = new StringBuilder(ret);
                sb.insert(i, BREAK_TAG);
                ret = sb.toString();
            }
            currentLines++;
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
     * Constructs a new BoundsString object.
     * <p>
     * A String associated with the width and height it should fit in
     * based on it's contents which possibly contain html formatting.
     *
     * @param width  the width for the string
     * @param height the height for the string
     * @param text   the string text
     */
    public record BoundsString(int width, int height, String text) {}

    /**
     * A record representing a segment of text as either being raw text or an html tag
     */
    public record TaggedString(String text, Type type) {
        /**
         * The type a given String is: HTML or TEXT
         */
        public enum Type {
            HTML, TEXT
        }
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
