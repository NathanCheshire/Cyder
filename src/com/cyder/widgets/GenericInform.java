package com.cyder.widgets;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.enums.TitlePosition;
import com.cyder.handler.ErrorHandler;
import com.cyder.utilities.ImageUtil;
import com.cyder.ui.CyderFrame;
import com.cyder.utilities.SystemUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

public class GenericInform {
    public static void inform(String text, String title) {
        try {
            //define the font we are using so that if we change it, we don't break the bounds calculation
            Font usageFont = CyderFonts.weatherFontSmall.deriveFont(22f);

            //affine transform preserves lines and parallelism but not necessarily distance and angles
            // from this we figure out the width and height we need for our text
            AffineTransform affinetransform = new AffineTransform();
            FontRenderContext frc = new FontRenderContext(affinetransform,true,true);
            int width = (int)(usageFont.getStringBounds(text, frc).getWidth());
            int height = (int)(usageFont.getStringBounds(text, frc).getHeight());

            //if calculated width is greater than half of the screen width, take away half and add it to the bottom
            while (width > SystemUtil.getScreenWidth() * 0.50) {
                width /= 2;
                height *= 2;
            }

            //offset variables to ensure text does not cover window bounds or dragLabel
            int heightOffset = 40;
            int widthOffset = 10;

            //obtain cyderframe object of background color
            CyderFrame informFrame = new CyderFrame(width + widthOffset * 2,height + heightOffset * 2,
                    new ImageIcon(new ImageUtil().imageFromColor(width + widthOffset * 2,height + heightOffset * 2, CyderColors.vanila)));
            informFrame.setTitlePosition(TitlePosition.CENTER);
            informFrame.setTitle(title);

            //make sure the text can wrap using HTML tags
            if (!text.startsWith("<html>"))
                text = "<html><div style='text-align: center;'>" + text + "</div></html>";

            //init the label object and set properties
            JLabel desc = new JLabel(text);

            desc.setHorizontalAlignment(JLabel.CENTER);
            desc.setVerticalAlignment(JLabel.CENTER);
            ImageUtil iu = new ImageUtil();
            desc.setForeground(CyderColors.navy);
            desc.setFont(usageFont);

            //set the label bounds
            desc.setBounds(10, 35, width, height + heightOffset);

            //add the label to the frame, and regular finalizing frame method calls
            informFrame.getContentPane().add(desc);

            informFrame.setVisible(true);
            informFrame.setLocationRelativeTo(null);
            informFrame.setAlwaysOnTop(true);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static void informRelative(String text, String title, Component relativeTo) {
        try {
            //define the font we are using so that if we change it, we don't break the bounds calculation
            Font usageFont = CyderFonts.weatherFontSmall.deriveFont(22f);

            //affine transform preserves lines and parallelism but not necessarily distance and angles
            // from this we figure out the width and height we need for our text
            AffineTransform affinetransform = new AffineTransform();
            FontRenderContext frc = new FontRenderContext(affinetransform,true,true);
            int width = (int)(usageFont.getStringBounds(text, frc).getWidth());
            int height = (int)(usageFont.getStringBounds(text, frc).getHeight());

            //if calculated width is greater than half of the screen width, take away half and add it to the bottom
            while (width > SystemUtil.getScreenWidth() * 0.50) {
                width /= 2;
                height *= 2;
            }

            //offset variables to ensure text does not cover window bounds or dragLabel
            int heightOffset = 40;
            int widthOffset = 10;

            //obtain cyderframe object of background color
            CyderFrame informFrame = new CyderFrame(width + widthOffset * 2,height + heightOffset * 2,
                    new ImageIcon(new ImageUtil().imageFromColor(width + widthOffset * 2,height + heightOffset * 2, CyderColors.vanila)));
            informFrame.setTitlePosition(TitlePosition.CENTER);
            informFrame.setTitle(title);

            //make sure the text can wrap using HTML tags
            if (!text.startsWith("<html>"))
                text = "<html><div style='text-align: center;'>" + text + "</div></html>";

            //init the label object and set properties
            JLabel desc = new JLabel(text);

            desc.setHorizontalAlignment(JLabel.CENTER);
            desc.setVerticalAlignment(JLabel.CENTER);
            ImageUtil iu = new ImageUtil();
            desc.setForeground(CyderColors.navy);
            desc.setFont(usageFont);

            //set the label bounds
            desc.setBounds(10, 35, width, height + heightOffset);

            //add the label to the frame, and regular finalizing frame method calls
            informFrame.getContentPane().add(desc);

            informFrame.setVisible(true);
            informFrame.setLocationRelativeTo(relativeTo);
            informFrame.setAlwaysOnTop(true);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
