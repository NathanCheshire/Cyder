package com.cyder.widgets;

import com.cyder.constants.CyderColors;
import com.cyder.constants.CyderFonts;
import com.cyder.enums.TitlePosition;
import com.cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.*;

public class Cards {
    private static CyderFrame cf;

    public static void Christmas2020() {
        if (cf != null)
            cf.closeAnimation();

        cf = new CyderFrame(498,490, new ImageIcon("src/com/cyder/sys/pictures/Santa.gif"));
        cf.setTitle("Merry Christmas!");
        cf.setTitlePosition(TitlePosition.CENTER);

        cf.initResizing();
        cf.setMinimumSize(new Dimension(498,490));
        cf.setMaximumSize(new Dimension(498 * 2,498 * 2));
        cf.setSnapSize(new Dimension(1,1));
        cf.setBackground(CyderColors.vanila);
        cf.allowResizing(true);

        JLabel cardLabel = new JLabel("<html>Dear Mom and Dad,<br/>" +
                "Thank yall so much for everything yall do for me and espcially " +
                "everything yall did this year in particular such as the vacations, " +
                "college from home, picking me up for the wedding and, you know, providing" +
                " room and board (food and my room at home).<br/><br/>" +
                "Love,<br/>" +
                "Nathan</html>");
        cardLabel.setForeground(CyderColors.navy);
        cardLabel.setFont(CyderFonts.weatherFontSmall.deriveFont(24f));
        cardLabel.setHorizontalAlignment(JLabel.CENTER);
        cardLabel.setVerticalAlignment(JLabel.CENTER);
        cardLabel.setBounds(498 + 40,40,cf.getWidth() - 40, cf.getHeight() - 40);
        cf.getContentPane().add(cardLabel);

        cf.setLocationRelativeTo(null);
        cf.setVisible(true);
    }
}
