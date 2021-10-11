package cyder.widgets;

import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.handler.ErrorHandler;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.utilities.SystemUtil;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

public class GenericInformer {
    //returns the CyderFrame instance to be shown elsewhere
    public static CyderFrame informRet(String text, String title) {
        try {
            CyderLabel textLabel = new CyderLabel(text);
            int[] widthHeight = widthHeightCalculation(text);
            textLabel.setBounds(10,35, widthHeight[0], widthHeight[1]);

            CyderFrame informFrame = new CyderFrame(widthHeight[0] + 40,
                    widthHeight[1] + 40, CyderImages.defaultBackgroundLarge);
            informFrame.setTitle(title);
            informFrame.add(textLabel);

            return informFrame;
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return null;
    }

    public static void inform(String text, String title) {
        informRelative(text, title, null);
    }

    public static void informRelative(String text, String title, Component relativeTo) {
        try {
            CyderLabel textLabel = new CyderLabel(text);
            int[] widthHeight = widthHeightCalculation(text);
            textLabel.setBounds(10,35, widthHeight[0], widthHeight[1]);

            CyderFrame informFrame = new CyderFrame(widthHeight[0] + 20,
                    widthHeight[1] + 40, CyderImages.defaultBackgroundLarge);
            informFrame.setTitle(title);
            informFrame.add(textLabel);

            informFrame.setVisible(true);
            informFrame.setLocationRelativeTo(relativeTo);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Inner logic to calculate the needed width and height of an inform/dialog window.
     * Typically more width is favored over height
     * @param text - the string to display
     * @return - an array composed of the width followed by the height to form the bounding box
     *           for the provided display string.
     */
    private static int[] widthHeightCalculation(String text) {
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
        while (width > SystemUtil.getScreenWidth() / 2) {
            int area = width * cumulativeHeight;
            width /= 2;
            cumulativeHeight = area / width;
        }

        int numHeights = (int) Math.ceil(cumulativeHeight / lineHeight);
        int numChars = text.length();
        int splitEveryNthChar = (int) Math.floor(numChars / numHeights);

        //tolerance character limit
        int breakInsertionTol = 7;

        //todo adding breaks isn't exactly working
        for (int i = splitEveryNthChar ; i < numChars ; i += splitEveryNthChar) {

            if (text.charAt(i) == ' ') {
                text = text.substring(0, splitEveryNthChar).trim() + "<br/>" + text.substring(splitEveryNthChar).trim();
            } else {
                boolean spaceFound = false;

                for (int j = splitEveryNthChar ; j > splitEveryNthChar - 8 ; j--) {
                    if (j == ' ') {
                        if (j < 0 || j > numChars)
                            break;

                        text = text.substring(0, splitEveryNthChar - j) + "<br/>" + text.substring(splitEveryNthChar - j);
                        spaceFound = true;
                        break;
                    }
                }

                if (spaceFound)
                    continue;

                for (int j = splitEveryNthChar ; j < splitEveryNthChar + 8 ; j++) {
                    if (j == ' ') {
                        if (j < 0 || j > numChars)
                            break;
                        text = text.substring(0, splitEveryNthChar + j) + "<br/>" + text.substring(splitEveryNthChar + j);
                        spaceFound = true;
                        break;
                    }
                }

                if (!spaceFound) {
                    //last restort just insert a break here in between the chars
                    text = text.substring(0, splitEveryNthChar) + "<br/>" + text.substring(splitEveryNthChar);
                    System.out.println(text);
                }
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
        } //else width and height are fine

        //if no extra height was needed, add 10 anyway so that the line of text isn't cut off
        if (cumulativeHeight == lineHeight)
            cumulativeHeight += 20;

        return new int[]{width,cumulativeHeight};
    }
}
