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
    public static CyderFrame informRet(String text, String title) {
        try {
            CyderLabel textLabel = new CyderLabel(text);

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

            textLabel.setBounds(10,35, w, h);

            CyderFrame informFrame = new CyderFrame(w + 40, h + 40, CyderImages.defaultBackgroundLarge);
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

            textLabel.setBounds(10,35, w, h);

            CyderFrame informFrame = new CyderFrame(w + 20, h + 40, CyderImages.defaultBackgroundLarge);
            informFrame.setTitle(title);
            informFrame.add(textLabel);

            informFrame.setVisible(true);
            informFrame.setLocationRelativeTo(relativeTo);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Given the provided text, returns a bouding box for the text, not the window.
     * @param text - the text to be used for the calculaiton
     * @return - the width and height for the provided text
     */
    public static Dimension getBoundingBox(String text) {


        return null;
    }

    /**
     * Given the provided text, returns the width needed to fit the text onto a frame with the provided height
     * @param text - the text to be used for the calculaiton
     * @param font - the font to use for the text label
     * @param height - the decided upon height for the text bounds
     * @return - the width for the provided text
     */
    public static int getTextWidth(String text, Font font, int height) {
        //whitespace safety
        text = text.trim();

        if (text.length() == 0)
            throw new IllegalArgumentException("Text contains no chars (aside from possible whitespace)");
        else if (height < CyderFrame.getMinHeight(text, font) || height > SystemUtil.getScreenHeight())
            throw new IllegalArgumentException("Invalid height parameter:");

        int ret = 0;

        //todo make the height exactly "height" and figure out the minimum width of the box needed

        return ret;
    }


    /**
     * Given the provided text, returns the height needed to fit the text onto a frame with the provided width
     * @param text - the tex to be used for the calculation
     * @param font - the font to use for the text label
     * @param width - the decided upon width for the text bouds
     * @return - the height for the provided text
     */
    public static int getTextHeight(String text, Font font, int width) {
        text = text.trim();
        int minimumAllowedTextWidth = 100;

        if (text.length() == 0)
            throw new IllegalArgumentException("Text contains no chars (aside from possible whitespace)");
        else if (width < minimumAllowedTextWidth || width > SystemUtil.getScreenWidth())
            throw new IllegalArgumentException("Invalid width parameter");

        int ret = 0;

       //todo now make sure all lines are of width "width" or close to it, break line if possible

        return ret;
    }
}
