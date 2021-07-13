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


public class GenericInform {
    public static void inform(String text, String title) {
        informRelative(text, title, null);
    }

    public static void informRelative(String text, String title, Component relativeTo) {
        try {
            CyderLabel textLabel = new CyderLabel(text);

            //start of font width and height calculation
            int w = 0;
            Font notificationFont = CyderFonts.weatherFontSmall;
            AffineTransform affinetransform = new AffineTransform();
            FontRenderContext frc = new FontRenderContext(affinetransform, notificationFont.isItalic(), true);

            //get minimum width for whole parsed string
            w = (int) notificationFont.getStringBounds(text, frc).getWidth() + 5;

            //get height of a line and set it as height increment too
            int h = (int) notificationFont.getStringBounds(text, frc).getHeight();
            int heightInc = h;

            while (w > SystemUtil.getScreenWidth() / 4) {
                int area = w * h;
                w /= 2;
                h = area / w;
            }

            String[] breakOccurences = text.split("<br/>");
            h += (breakOccurences.length * heightInc);

            if (h != heightInc)
                h += 10;

            textLabel.setBounds(10,35, w, h);

            CyderFrame informFrame = new CyderFrame(w + 20, h + 40, CyderImages.defaultBackgroundLarge);
            informFrame.setTitle("Inform");
            informFrame.add(textLabel);

            informFrame.setVisible(true);
            informFrame.setLocationRelativeTo(relativeTo);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
