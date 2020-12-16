package com.cyder.widgets;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.handler.ErrorHandler;
import com.cyder.ui.CyderFrame;
import com.cyder.utilities.GeneralUtil;
import com.cyder.utilities.ImageUtil;

import javax.swing.*;

public class GenericInform {
    public static void inform(String text, String title, int width, int height) {
        try {
            CyderFrame informFrame = new CyderFrame(width,height,new ImageIcon(new ImageUtil().imageFromColor(width,height, CyderColors.vanila)));
            informFrame.setTitle(title);

            JLabel desc = new JLabel("<html><div style='text-align: center;'>" + text + "</div></html>");

            desc.setHorizontalAlignment(JLabel.CENTER);
            desc.setVerticalAlignment(JLabel.CENTER);
            ImageUtil iu = new ImageUtil();
            desc.setForeground(CyderColors.navy);
            desc.setFont(CyderFonts.weatherFontSmall.deriveFont(22f));
            desc.setBounds(10, 35, width - 20, height - 35 * 2);

            informFrame.getContentPane().add(desc);

            informFrame.setVisible(true);
            informFrame.setLocationRelativeTo(null);
            informFrame.setAlwaysOnTop(true);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
