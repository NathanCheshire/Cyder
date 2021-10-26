package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.enums.NotificationDirection;
import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.*;

public class Cards {
    private static CyderFrame christmas2020Frame;
    private static CyderFrame fathersDay2021Frame;
    private static CyderFrame birthday2021Frame;

    public static void Christmas2020() {
        if (christmas2020Frame != null)
            christmas2020Frame.dispose();

        christmas2020Frame = new CyderFrame(498,490, new ImageIcon("static/cards/Santa.gif"));
        christmas2020Frame.setTitle("Merry Christmas!");
        christmas2020Frame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

        christmas2020Frame.initializeResizing();
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

        int consoleX = ConsoleFrame.getConsoleFrame().getX();
        int consoleY = ConsoleFrame.getConsoleFrame().getY();
        int consoleW = ConsoleFrame.getConsoleFrame().getWidth();
        int consoleH = ConsoleFrame.getConsoleFrame().getHeight();

        christmas2020Frame.setLocation(consoleX + Math.abs((consoleW - christmas2020Frame.getWidth()) / 2),
                consoleY + Math.abs((consoleH - christmas2020Frame.getHeight()) / 2));
        christmas2020Frame.setVisible(true);
    }

    public static void FathersDay2021() {
        if (fathersDay2021Frame != null)
            fathersDay2021Frame.dispose();

        fathersDay2021Frame = new CyderFrame(800,721, new ImageIcon("static/cards/Philmont.png"));
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

        int consoleX = ConsoleFrame.getConsoleFrame().getX();
        int consoleY = ConsoleFrame.getConsoleFrame().getY();
        int consoleW = ConsoleFrame.getConsoleFrame().getWidth();
        int consoleH = ConsoleFrame.getConsoleFrame().getHeight();

        fathersDay2021Frame.setLocation(consoleX + Math.abs((consoleW - fathersDay2021Frame.getWidth()) / 2),
                consoleY + Math.abs((consoleH - fathersDay2021Frame.getHeight()) / 2));
        fathersDay2021Frame.setVisible(true);
    }
    public static void Birthday2021() {
        if (birthday2021Frame != null)
            birthday2021Frame.dispose();

        birthday2021Frame = new CyderFrame(800,600, new ImageIcon("static/cards/Confetti.png"));
        birthday2021Frame.setTitle("Happy Birthday Day!");
        birthday2021Frame.setBackground(CyderColors.navy);
        birthday2021Frame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

        JLabel cardLabel = new JLabel("<html>Happy Birthday Dad! Thanks for everything " +
                "this year such as helpng me move into BR and back home, the Destin and Kentucky " +
                "trips, and for bringing my desk up next weekend :D<br/><br/>Love,<br/>Nathan</html>");
        cardLabel.setForeground(CyderColors.navy);
        cardLabel.setFont(CyderFonts.weatherFontSmall.deriveFont(24f));
        cardLabel.setHorizontalAlignment(JLabel.CENTER);
        cardLabel.setVerticalAlignment(JLabel.CENTER);
        cardLabel.setBounds(5,40, birthday2021Frame.getWidth() - 40, 240);
        birthday2021Frame.getContentPane().add(cardLabel);

        int consoleX = ConsoleFrame.getConsoleFrame().getX();
        int consoleY = ConsoleFrame.getConsoleFrame().getY();
        int consoleW = ConsoleFrame.getConsoleFrame().getWidth();
        int consoleH = ConsoleFrame.getConsoleFrame().getHeight();

        birthday2021Frame.setLocation(consoleX + Math.abs((consoleW - birthday2021Frame.getWidth()) / 2),
                consoleY + Math.abs((consoleH - birthday2021Frame.getHeight()) / 2));
        birthday2021Frame.setVisible(true);

        try {
            new Thread(() -> {
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }

                birthday2021Frame.notify("WOOOOOO HOOOOOOOOOOOOO", 3000, NotificationDirection.TOP);
                birthday2021Frame.notify("WOOOOOO HOOOOOOOOOOOOO", 3000, NotificationDirection.TOP_LEFT);
                birthday2021Frame.notify("WOOOOOO HOOOOOOOOOOOOO", 3000, NotificationDirection.BOTTOM);
                birthday2021Frame.notify("WOOOOOO HOOOOOOOOOOOOO", 3000, NotificationDirection.TOP_RIGHT);
            },"Birthday card 2021 notification wait thread").start();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
