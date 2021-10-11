package cyder.widgets;

import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
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
        //start of font width and height calculation
        int w = 0;
        Font notificationFont = CyderFonts.defaultFontSmall;
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, notificationFont.isItalic(), true);

        //get minimum width for whole parsed string
        w = (int) notificationFont.getStringBounds(text, frc).getWidth() + 5;

        //get height of a line and set it as height increment too
        int h = (int) notificationFont.getStringBounds(text, frc).getHeight();
        int heightInc = h;

        while (w > SystemUtil.getScreenWidth() / 2) {
            int area = w * h;
            w /= 2;
            h = area / w;
        }

        String[] breakOccurences = text.split("<br/>");
        h += (breakOccurences.length * heightInc);

        if (h != heightInc)
            h += 10;

        //in case we're too short from html breaks, find the max width line and set it to w
        if (text.contains("<br/>"))
            w = 0;

        for (String line : text.split("<br/>")) {
            int thisW = (int) notificationFont.getStringBounds(Jsoup.clean(line, Safelist.none()), frc).getWidth() + 5;

            if (thisW > w) {
                w = thisW;
            }
        }

        return new int[]{w,h};
    }
}
