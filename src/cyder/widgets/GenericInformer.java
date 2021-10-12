package cyder.widgets;

import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.genobjects.BoundsString;
import cyder.handler.ErrorHandler;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.utilities.SystemUtil;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

public class GenericInformer {
    //returns the CyderFrame instance to be shown elsewhere
    public static CyderFrame informRet(String text, String title) {
        try {
            CyderLabel textLabel = new CyderLabel(text);
            BoundsString boundsString = widthHeightCalculation(text);
            textLabel.setText(boundsString.getText());
            textLabel.setBounds(10,35, boundsString.getWidth(), boundsString.getHeight());

            CyderFrame informFrame = new CyderFrame(boundsString.getWidth() + 40,
                    boundsString.getHeight() + 40, CyderImages.defaultBackgroundLarge);
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
            BoundsString boundsString = widthHeightCalculation(text);
            textLabel.setText(boundsString.getText());
            textLabel.setBounds(10,35, boundsString.getWidth(), boundsString.getHeight());

            CyderFrame informFrame = new CyderFrame(boundsString.getWidth() + 20,
                    boundsString.getHeight() + 40, CyderImages.defaultBackgroundLarge);
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
     * @return - an object composed of the width, height, and possibly corrected text to form the bounding box
     *           for the provided display string.
     */
    private static BoundsString widthHeightCalculation(String text) {
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
            int beforeArea = width * cumulativeHeight;
            cumulativeHeight += lineHeight;
            width = beforeArea / cumulativeHeight;
        }

        int numHeights = (int) Math.ceil(cumulativeHeight / lineHeight);
        int numChars = text.length();
        int splitEveryNthChar = (int) Math.floor(numChars / numHeights);

        //tolerance character limit
        int breakInsertionTol = 7;

        System.out.println(numHeights);
        System.out.println(numChars);
        System.out.println(splitEveryNthChar);

        //if splitting, strip away any line breaks in there already
        // only split though if we have no breaks needed in the text
        if (splitEveryNthChar < numChars && !text.contains("<br/>")) {
            text = Jsoup.clean(text, Safelist.none());
        }

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

                //final resort to just put it at the current index
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

        System.out.println(text);

        return new BoundsString(width, cumulativeHeight, text);
    }
}
