package cyder.utilities;

import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.LinkedList;

import static cyder.utilities.StringUtil.TaggedString;
import static cyder.utilities.StringUtil.TaggedStringType;

public class BoundsUtil {
    private BoundsUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * Calculates the needed height of an inform/dialog window.
     * @param text the string to display
     * @return an object composed of the width, height, and possibly corrected text to form the bounding box
     *           for the provided display string.
     */
    public static BoundsString widthHeightCalculation(String text) {
        return widthHeightCalculation(text, SystemUtil.getScreenWidth() / 2, CyderFonts.defaultFontSmall);
    }

    public static BoundsString widthHeightCalculation(String text, Font font) {
        return widthHeightCalculation(text, SystemUtil.getScreenWidth() / 2, font);
    }

    /**
     * Calculates the needed height for an inform/dialog window given the prefered width and text.
     * @param text the string to display
     * @param maxWidth the maximum width allowed
     * @param font the font to be used
     * @return an object composed of the width, height, and possibly corrected text to form the bounding box
     *           for the provided display string.
     */
    public static BoundsString widthHeightCalculation(final String text, int maxWidth, Font font) {
        BoundsString ret = new BoundsString();

        int widthAddition = 5;
        int heightAddition = 2;
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, font.isItalic(), true);
        int singleLineHeight = (int) font.getStringBounds(text, frc).getHeight() + heightAddition;

        //does the string contain any html? if so we have to be careful where we insert needed line breaks
        String[] parts = text.split("<br/>");
        StringBuilder sb = new StringBuilder();

        for (int j = 0 ; j < parts.length ; j++) {
            sb.append(Jsoup.clean(parts[j], Safelist.none()));

            if (j != parts.length - 1)
                sb.append("<br/>");
        }

        String htmlParsedAway = sb.toString();
        boolean containsHtml = text.length() != htmlParsedAway.length();

        //unfortunate
        if (containsHtml) {
            LinkedList<TaggedString> taggedStrings = new LinkedList<>();
            String textCopy = text;

            //while we still have text....
            while ((textCopy.contains("<") && textCopy.contains(">"))) {
                int firstOpeningTag = textCopy.indexOf("<");
                int firstClosingTag = textCopy.indexOf(">");

                //failsafe
                if (firstClosingTag == -1 || firstOpeningTag == -1 || firstClosingTag < firstOpeningTag)
                    break;

                String regularText = textCopy.substring(0, firstOpeningTag);
                String firstHtml = textCopy.substring(firstOpeningTag, firstClosingTag + 1);

                if (regularText.length() > 0)
                    taggedStrings.add(new TaggedString(regularText, TaggedStringType.TEXT));
                if (firstHtml.length() > 0)
                    taggedStrings.add(new TaggedString(firstHtml, TaggedStringType.HTML));

                textCopy = textCopy.substring(firstClosingTag + 1);
            }

            //if there's remaining text, it's just non-html
            if (textCopy.length() > 0)
                taggedStrings.add(new TaggedString(textCopy, TaggedStringType.TEXT));

            //now add breaks into the lines that are needed
            for (TaggedString taggedString : taggedStrings) {
                if (taggedString.getType() == TaggedStringType.TEXT) {
                    int fullLineWidth = (int) (font.getStringBounds(
                            taggedString.getText(), frc).getWidth() + widthAddition);

                    //evluate if the line is too long
                    if (fullLineWidth > maxWidth) {
                        //line is too long, figure out how many breaks to add
                        //first, how many multiples of current width does it take to get to max width?
                        int neededLines = (int) Math.ceil((double) fullLineWidth / (double) maxWidth);

                        //if only one line is the result somehow, ensure it's 2
                        neededLines = Math.max(2, neededLines);
                        taggedString.setText(insertBreaks(taggedString.getText(), neededLines));
                    }
                }
            }

            //recombine text into string
            StringBuilder htmlBuilder = new StringBuilder();

            for (TaggedString tg : taggedStrings)
                htmlBuilder.append(tg.getText());

            //figure out height
            String[] lines = htmlBuilder.toString().split("<br/>");
            int h = singleLineHeight * lines.length;

            //figure out width
            int w = 0;

            for (TaggedString ts : taggedStrings) {
                if (ts.getType() == TaggedStringType.HTML)
                    continue;

                String[] tsLines = ts.getText().split("<br/>");

                for (String line : tsLines) {
                    int lineWidth = (int) (font.getStringBounds(line, frc).getWidth() + widthAddition);

                    if (lineWidth > w)
                        w = lineWidth;
                }
            }

            ret = new BoundsString(w, h, htmlBuilder.toString());
        }
        //nice, we can just add line breaks wherever we need
        else {
            //only contains possible line breaks so split at those
            StringBuilder nonHtmlBuilder = new StringBuilder();
            String[] lines = text.split("<br/>");

            for (int i = 0 ; i < lines.length ; i++) {
                int fullLineWidth = (int) (font.getStringBounds(lines[i], frc).getWidth() + widthAddition);

                //evluate if the line is too long
                if (fullLineWidth > maxWidth) {
                    //line is too long, figure out how many breaks to add
                    //first, how many multiples of current width does it take to get to max width?
                    int neededLines = (int) Math.ceil((double) fullLineWidth / (double) maxWidth);

                    //if only one line is the result somehow, ensure it's 2
                    neededLines = Math.max(2, neededLines);
                    nonHtmlBuilder.append(insertBreaks(lines[i], neededLines));
                } else {
                    //line isn't too long, simply append it
                    nonHtmlBuilder.append(lines[i]);
                }

                //if we're not on the last line, add the original break we split at back in
                if (i != lines.length - 1) {
                    nonHtmlBuilder.append("<br/>");
                }
            }

            //more lines might exist now since we added breaks
            lines = nonHtmlBuilder.toString().split("<br/>");

            //finally figure out the width and height based on the amount of lines and the longest line
            int w = 0;
            int h = singleLineHeight * lines.length;
            String correctedNonHtml = nonHtmlBuilder.toString();

            for (String line : lines) {
                int currentWidth = (int) (font.getStringBounds(line, frc).getWidth() + widthAddition);

                if (currentWidth > w)
                    w = currentWidth;
            }

            //this should always run but to be safe check first
            if (!correctedNonHtml.startsWith("<html>")) {
                correctedNonHtml = "<html>" + correctedNonHtml + "</html>";
            }

            ret = new BoundsString(w, h, correctedNonHtml);
        }

        return ret;
    }

    public static String insertBreaks(String rawText, int numLines) {
        if (numLines == 1)
            return rawText;

        String ret = rawText;

        int splitEveryNthChar = (int) Math.ceil(rawText.length() / numLines);
        int numChars = rawText.length();
        int breakInsertionTol = 7;

        int currentLines = 1;

        for (int i = splitEveryNthChar ; i < numChars ; i += splitEveryNthChar) {
            //if line goal is reached, exit
            if (currentLines == numLines)
                break;

            //is index a space? if so, replace it with a break
            if (ret.charAt(i) == ' ') {
                StringBuilder sb = new StringBuilder(ret);
                sb.deleteCharAt(i);
                sb.insert(i,"<br/>");
                ret = sb.toString();
            } else {
                boolean spaceFound = false;

                //check right for a space
                for (int j = i ; j < i + breakInsertionTol ; j++) {
                    //is j valid
                    if (j < numChars) {
                        //is it a space
                        if (ret.charAt(j) == ' ') {
                            StringBuilder sb = new StringBuilder(ret);
                            sb.deleteCharAt(j);
                            sb.insert(j,"<br/>");
                            ret = sb.toString();
                            spaceFound = true;
                            currentLines++;
                            break;
                        }
                    }
                }

                if (spaceFound)
                    continue;

                //check left for a space
                for (int j = i ; j > i - breakInsertionTol ; j--) {
                    //is j valid
                    if (j > 0) {
                        //is it a space
                        if (ret.charAt(j) == ' ') {
                            StringBuilder sb = new StringBuilder(ret);
                            sb.deleteCharAt(j);
                            sb.insert(j,"<br/>");
                            ret = sb.toString();
                            spaceFound = true;
                            currentLines++;
                            break;
                        }
                    }
                }

                if (spaceFound)
                    continue;

                //final resort to just put it at the current index as long as we're not in the middle of a line break
                //we shouldn't be in a line break since this is for html having been parsed away

                StringBuilder sb = new StringBuilder(ret);
                sb.insert(i,"<br/>");
                ret = sb.toString();
            }
            currentLines++;
        }

        return ret;
    }

    //adds <div style='text-align: center;'> to the provided html string
    public static String addCenteringToHTML(String html) {
        StringBuilder ret = new StringBuilder();

        if (html.startsWith("<html>")) {
            ret.append("<html><div style='text-align: center;'>")
                    .append(html, 6, html.length() - 6).append("</html>");
        } else {
            ret.append("<html><div style='text-align: center;'>").append(html).append("</html>");
        }

        return ret.toString();
    }

    //inner classes and enums

    public static class BoundsString {
        private int width;
        private int height;
        private String text;

        public BoundsString(int width, int height, String text) {
            this.width = width;
            this.height = height;
            this.text = text;
        }

        public BoundsString() {}

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "[" + this.width + "x" + this.height + "]\nText:\n" + this.text;
        }
    }

    private static class BreakPosition {
        private int start;
        private int end;

        public BreakPosition(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        @Override
        public String toString() {
            return "[" + this.start + " -> " + this.end + "]";
        }
    }
}
