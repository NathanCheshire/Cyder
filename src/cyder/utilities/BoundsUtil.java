package cyder.utilities;

import cyder.consts.CyderFonts;
import cyder.genobjects.BoundsString;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.LinkedList;

public class BoundsUtil {
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
    public static BoundsString widthHeightCalculation(String text, int maxWidth, Font font) {
        //needed width
        int width = 0;

        //font, transform, and rendercontext vars
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, font.isItalic(), true);

        //get minimum width for whole parsed string
        width = (int) font.getStringBounds(text, frc).getWidth() + 5;

        //the height of a singular line of text
        int lineHeight = (int) font.getStringBounds(text, frc).getHeight() + 2;
        //cumulative height needed
        int cumulativeHeight = lineHeight;

        //width may never be greater than half of the screen width
        while (width > maxWidth) {
            int beforeArea = width * cumulativeHeight;
            cumulativeHeight += lineHeight;
            width = beforeArea / cumulativeHeight;
        }

        int numHeights = (int) Math.ceil(cumulativeHeight / lineHeight);
        int numChars = text.length();
        int splitEveryNthChar = (int) Math.ceil(numChars / numHeights);

        //tolerance character limit
        int breakInsertionTol = 7;

        //if splitting, and no breaks, strip away html, otherwise, strip away html excluding breaks
        if (splitEveryNthChar < numChars && !text.contains("<br/>")) {
            text = Jsoup.clean(text, Safelist.none());
        } else {
            String[] parts = text.split("<br/>");
            StringBuilder sb = new StringBuilder();

            for (int j = 0 ; j < parts.length ; j++) {
                sb.append(Jsoup.clean(parts[j], Safelist.none()));

                if (j != parts.length - 1)
                    sb.append("<br/>");
            }

            text = sb.toString();
        }

        //number of chars might have changed so revalidate the following vars
        numChars = text.length();
        splitEveryNthChar = (int) Math.ceil(numChars / numHeights);
        width = (int) font.getStringBounds(text, frc).getWidth() + 5;
        lineHeight = (int) font.getStringBounds(text, frc).getHeight() + 2;
        cumulativeHeight = lineHeight;

        while (width > maxWidth) {
            int beforeArea = width * cumulativeHeight;
            cumulativeHeight += lineHeight;
            width = beforeArea / cumulativeHeight;
        }

        int currentLineCount = text.split("<br/>").length;

        SPLITTING:
            for (int i = splitEveryNthChar ; i < numChars ; i += splitEveryNthChar) {
                if (currentLineCount == numHeights)
                    break;
                else
                    currentLineCount++;

                //is index a space? if so, replace it with a break
                if (text.charAt(i) == ' ') {
                    StringBuilder sb = new StringBuilder(text);
                    sb.deleteCharAt(i);
                    sb.insert(i,"<br/>");
                    text = sb.toString();
                } else {
                    boolean spaceFound = false;

                    //check right for a space
                    for (int j = i ; j < i + breakInsertionTol ; j++) {
                        //is j valid
                        if (j < numChars) {
                            //is it a space
                            if (text.charAt(j) == ' ') {
                                StringBuilder sb = new StringBuilder(text);
                                sb.deleteCharAt(j);
                                sb.insert(j,"<br/>");
                                text = sb.toString();
                                spaceFound = true;
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
                            if (text.charAt(j) == ' ') {
                                StringBuilder sb = new StringBuilder(text);
                                sb.deleteCharAt(j);
                                sb.insert(j,"<br/>");
                                text = sb.toString();
                                spaceFound = true;
                                break;
                            }
                        }
                    }

                    if (spaceFound)
                        continue;

                    //final resort to just put it at the current index as long as we're not in the middle of a line break
                    String breakString = "<br/>";
                    int brLen = breakString.length();
                    String[] breaks = text.split("<br/>");
                    int breaksPassed = 0;

                    LinkedList<BreakPosition> breakPositions = new LinkedList<>();

                    for (int j = 0 ; j < breaks.length ; j++) {
                        //first
                        if (j == 0) {
                            //first break starts after first element's length and lasts from that value + the length of a break string
                            breakPositions.add(new BreakPosition(breaks[j].length(), breaks[j].length() + brLen - 1));
                        }
                        //stuff before this exists so we can save computation time
                        else {
                            //start of this current break is the end of the last break position + our current length
                            int startingIndex = breakPositions.get(j - 1).getEnd() + breaks[j].length();
                            int endIndex = startingIndex + brLen - 1;
                            breakPositions.add(new BreakPosition(startingIndex, endIndex));
                        }
                    }

                    boolean insideBreak = false;

                    //now we have all the breakPositions, let's print them to make sure they're correct
                    for (BreakPosition bp : breakPositions) {
                        //are we inside of this current break
                        if (i >= bp.getStart() + brLen && i <= bp.getEnd() + brLen) {
                            //we're inside so we don't need to check the other breaks
                            insideBreak = true;
                            break;
                        }
                    }

                    //if we're inside of a break, then we don't need to add one here
                    if (insideBreak)
                        continue;


                    //we don't actually need to add this break or any other ones if the last line length is short enough
                    String[] lines = text.split("<br/>");

                    for (int j = 1 ; j < lines.length ; j++) {
                        int maxBefore = lines[1].length();
                        for (int k = 1 ; k < j ; k++) {
                            if (lines[k].length() > maxBefore) {
                                maxBefore = lines[k].length();
                            }
                        }

                        if (maxBefore + brLen > lines[j].length()) {
                            break SPLITTING;
                        }
                    }

                    StringBuilder sb = new StringBuilder(text);
                    sb.insert(i,"<br/>");
                    text = sb.toString();
                }
            }

            //account for html line breaks
            if (text.contains("<br/>")) {
                //account for breaks in the height
                String[] breakOccurences = text.split("<br/>");
                cumulativeHeight += (breakOccurences.length * lineHeight);

                //reset width
                width = 0;

                //get the maximum width after accounting for line breaks
                String lines[] = text.split("<br/>");
                for (String line : lines) {
                    int currentWidth = (int) font.getStringBounds(line, frc).getWidth() + 5;

                    if (currentWidth > width) {
                        width = currentWidth;
                    }
                }

                //fix height
                cumulativeHeight = lines.length * lineHeight;

                //fix html if needed
                if (!text.startsWith("<html>")) {
                    text = "<html>" + text + "</html>";
                }
            }
        //done with inserting breaks

        //if no extra height was needed, add 10 anyway so that the line of text isn't cut off
        if (cumulativeHeight == lineHeight)
            cumulativeHeight += 10;

        return new BoundsString(width, cumulativeHeight, text);
    }

    /**
     * Calculates the needed height for an inform/dialog window given the prefered width and text.
     * @param text the string to display
     * @param maxWidth the maximum width allowed
     * @param font the font to be used
     * @return an object composed of the width, height, and possibly corrected text to form the bounding box
     *           for the provided display string.
     */
    public static BoundsString widthHeightCalculationNewLogic(final String text, int maxWidth, Font font) {
        BoundsString ret = new BoundsString();

        //pseudo code: still want to return a minimum bounding box less than max width no matter what
        // this bounding box should try to make all the lines the same length, thus, we should split
        // at exiting line breaks and then figure out line lengths from there

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
        boolean containsHtml = text.length() == htmlParsedAway.length();

        //unfortunate
        if (containsHtml) {
            LinkedList<TaggedString> taggedStrings = new LinkedList<>();
            String textCopy = text;

            //somehow split into separate arrays of html and pure text
            // then we can essentially follow the procedure below but with
            // adding back in the html as needed
        }
        //nice, we can just add line breaks wherever we need
        else {
            //only contains some line breaks so split at those
            StringBuilder nonHtmlBuilder = new StringBuilder();
            String[] lines = text.split("<br/>");

            //for all of the already pre-defined lines determined by breaks already in the code
            for (int i = 0 ; i < lines.length ; i++) {
                int currentWidth = (int) (font.getStringBounds(lines[i], frc).getWidth() + widthAddition);

                //if it's too big, insert breaks
                if (currentWidth > maxWidth) {
                    int insertXBreaks = (int) Math.ceil(currentWidth / maxWidth) - 1;
                    nonHtmlBuilder.append(insertBreaks(lines[i], insertXBreaks)).append("<br/>");
                } else {
                    nonHtmlBuilder.append(lines[i]);
                }

                //if it's not the last line, add the break separator for the line back in
                if (i != lines.length - 1) {
                    sb.append("<br/>");
                }
            }

            //finally figure out the width and height based on the amount of lines and the longest line
            int w = 0;
            int h = heightAddition * lines.length;
            String correctedNonHtml = nonHtmlBuilder.toString();
            lines = correctedNonHtml.split("<br/>");

            for (String line : lines) {
                int currentWidth = (int) (font.getStringBounds(line, frc).getWidth() + widthAddition);

                if (currentWidth > w)
                    w = currentWidth;
            }

            ret = new BoundsString(w, h, correctedNonHtml);
        }

        return ret;
    }

    private static String insertBreaks(String rawText, int numBreaks) {
        String ret = rawText;

        int splitEveryNthChar = (int) Math.ceil(rawText.length() / numBreaks);
        int numChars = rawText.length();
        int breakInsertionTol = 7;

        for (int i = splitEveryNthChar ; i < numChars ; i += splitEveryNthChar) {
            //is index a space? if so, replace it with a break
            if (rawText.charAt(i) == ' ') {
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

    public static class TaggedString {
        private String string;
        private StringType type;

        public TaggedString(String string, StringType type) {
            this.string = string;
            this.type = type;
        }

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }

        public StringType getType() {
            return type;
        }

        public void setType(StringType type) {
            this.type = type;
        }
    }

    public enum StringType {
        HTML, TEXT
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
