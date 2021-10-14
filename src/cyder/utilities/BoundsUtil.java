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
     * @param text - the string to display
     * @return - an object composed of the width, height, and possibly corrected text to form the bounding box
     *           for the provided display string.
     */
    public static BoundsString widthHeightCalculation(String text) {
        return widthHeightCalculation(text, SystemUtil.getScreenWidth() / 2);
    }

    /**
     * Calculates the needed height for an inform/dialog window given the prefered width and text.
     * @param text - the string to display
     * @param maxWidth - the maximum width allowed
     * @return - an object composed of the width, height, and possibly corrected text to form the bounding box
     *           for the provided display string.
     */
    public static BoundsString widthHeightCalculation(String text, int maxWidth) {
        //needed width
        int width = 0;

        //font, transform, and rendercontext vars
        Font notificationFont = CyderFonts.defaultFontSmall;
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, notificationFont.isItalic(), true);

        //get minimum width for whole parsed string
        width = (int) notificationFont.getStringBounds(text, frc).getWidth() + 5;

        //the height of a singular line of text
        int lineHeight = (int) notificationFont.getStringBounds(text, frc).getHeight() + 2;
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
        int splitEveryNthChar = (int) Math.floor(numChars / numHeights);

        //tolerance character limit
        int breakInsertionTol = 7;

        //if splitting, strip away any line breaks in there already
        // only split though if we have no breaks needed in the text
        if (splitEveryNthChar < numChars && !text.contains("<br/>")) {
            text = Jsoup.clean(text, Safelist.none());
        }

        SPLITTING:
        for (int i = splitEveryNthChar ; i < numChars ; i += splitEveryNthChar) {
            //is index a space? if so, replace it with a break
            if (text.charAt(i) == ' ') {
                StringBuilder sb = new StringBuilder(text);
                sb.deleteCharAt(i);
                sb.insert(i - 1,"<br/>");
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
                System.out.println("adding break at: " + i);
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
                int currentWidth = (int) notificationFont.getStringBounds(line, frc).getWidth() + 5;

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

        //if no extra height was needed, add 10 anyway so that the line of text isn't cut off
        if (cumulativeHeight == lineHeight)
            cumulativeHeight += 20;

        return new BoundsString(width, cumulativeHeight, text);
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
    }
}
