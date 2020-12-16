package com.cyder.widgets;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.enums.TitlePosition;
import com.cyder.handler.ErrorHandler;
import com.cyder.ui.CyderFrame;
import com.cyder.utilities.GeneralUtil;
import com.cyder.utilities.ImageUtil;
import com.cyder.utilities.NumberUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ClickMe {
    public static void clickMe() {
        try {
            CyderFrame clickMeFrame = new CyderFrame(220,100, new ImageIcon(new ImageUtil().imageFromColor(220,100, CyderColors.vanila)));
            clickMeFrame.setTitlePosition(TitlePosition.CENTER);
            clickMeFrame.setTitle("");

            JLabel dismiss = new JLabel("ClickMe Me!");
            dismiss.setHorizontalAlignment(JLabel.CENTER);
            dismiss.setVerticalAlignment(JLabel.CENTER);
            dismiss.setForeground(CyderColors.navy);
            dismiss.setFont(CyderFonts.weatherFontBig.deriveFont(22f));
            dismiss.setBounds(30, 40, 150, 40);
            dismiss.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    clickMeFrame.dispose();
                    clickMe();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    dismiss.setForeground(CyderColors.regularRed);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    dismiss.setForeground(CyderColors.navy);
                }
            });

            clickMeFrame.getContentPane().add(dismiss);

            clickMeFrame.setVisible(true);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
            Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
            clickMeFrame.setLocation(NumberUtil.randInt(0, (int) (rect.getMaxX() - 200)),NumberUtil.randInt(0,(int) rect.getMaxY() - 200));
            clickMeFrame.setAlwaysOnTop(true);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
