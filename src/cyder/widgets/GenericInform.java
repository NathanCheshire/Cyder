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


public class GenericInform {
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
}
