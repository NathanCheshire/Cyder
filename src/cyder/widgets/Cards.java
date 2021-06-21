package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;

import cyder.genesis.CyderMain;
import cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.*;

public class Cards {
    private static CyderFrame christmas2020Frame;
    private static CyderFrame fathersDay2021Frame;

    public static void Christmas2020() {
        if (christmas2020Frame != null)
            christmas2020Frame.closeAnimation();

        christmas2020Frame = new CyderFrame(498,490, new ImageIcon("sys/pictures/cards/Santa.gif"));
        christmas2020Frame.setTitle("Merry Christmas!");
        christmas2020Frame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

        christmas2020Frame.initializeBackgroundResizing();
        christmas2020Frame.setMinimumSize(new Dimension(498,490));
        christmas2020Frame.setMaximumSize(new Dimension(498 * 2,498 * 2));
        christmas2020Frame.setSnapSize(new Dimension(1,1));
        christmas2020Frame.setBackground(CyderColors.vanila);
        christmas2020Frame.setFrameResizing(true);

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
        cardLabel.setBounds(498 + 40,40, christmas2020Frame.getWidth() - 40, christmas2020Frame.getHeight() - 40);
        christmas2020Frame.getContentPane().add(cardLabel);

        christmas2020Frame.setLocationRelativeTo(CyderMain.consoleFrame);
        christmas2020Frame.setVisible(true);
    }

    public static void FathersDay2021() {
        if (fathersDay2021Frame != null)
            fathersDay2021Frame.closeAnimation();

        fathersDay2021Frame = new CyderFrame(800,721, new ImageIcon("sys/pictures/cards/Philmont.png"));
        fathersDay2021Frame.setTitle("Happy Father's Day!");
        fathersDay2021Frame.setBackground(CyderColors.navy);
        fathersDay2021Frame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

        JLabel cardLabel = new JLabel("<html>Happy fathers day to the best man I'll ever know. " +
                "Thanks so much for helping me move out from college everytime I need it and for all you do, " +
                "have done, continue to do, and will do.<br/><br/>Love,<br/>Nathan</html>");
        cardLabel.setForeground(CyderColors.navy);
        cardLabel.setFont(CyderFonts.weatherFontSmall.deriveFont(24f));
        cardLabel.setHorizontalAlignment(JLabel.CENTER);
        cardLabel.setVerticalAlignment(JLabel.CENTER);
        cardLabel.setBounds(5,40, fathersDay2021Frame.getWidth() - 40, 240);
        fathersDay2021Frame.getContentPane().add(cardLabel);

        fathersDay2021Frame.setLocationRelativeTo(CyderMain.consoleFrame);
        fathersDay2021Frame.setVisible(true);
    }
}
