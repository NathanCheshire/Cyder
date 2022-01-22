package cyder.widgets;

import cyder.annotations.Widget;
import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderStrings;
import cyder.enums.NotificationDirection;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;

import javax.swing.*;
import java.awt.*;

/**
 * Naming convention: you should name the card with the name of the holiday followed by the year without any spaces.
 * Example: Christmas2022() or Halloween2020() could be a method. These methods will then be automatically invoked on that particular day
 * See special day events in ConsoleFrame for an example on how these methods are invoked and why
 */
public class CardWidget {
    private static CyderFrame christmas2020Frame;
    private static CyderFrame christmas2021Frame;
    private static CyderFrame fathersDay2021Frame;
    private static CyderFrame birthday2021Frame;

    public CardWidget() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    } //public for reflection for auto calls of cards

    @Widget(trigger = "Christmas card 2020", description = "Christmas card for the year of 2020")
    public static void Christmas2020() {
        if (christmas2020Frame != null)
            christmas2020Frame.dispose();

        christmas2020Frame = new CyderFrame(498,490, new ImageIcon("static/pictures/cards/Santa.gif"));
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
        cardLabel.setFont(CyderFonts.segoe20.deriveFont(24f));
        cardLabel.setHorizontalAlignment(JLabel.CENTER);
        cardLabel.setVerticalAlignment(JLabel.CENTER);
        cardLabel.setBounds(498 + 40,40, christmas2020Frame.getWidth() - 40, christmas2020Frame.getHeight() - 40);
        christmas2020Frame.getContentPane().add(cardLabel);

        christmas2020Frame.setLocationRelativeTo(GenesisShare.getDominantFrame());
        christmas2020Frame.setVisible(true);
    }

    @Widget(trigger = "FathersDay card 2021", description = "Fathers day card for the year of 2021")
    public static void FathersDay2021() {
        if (fathersDay2021Frame != null)
            fathersDay2021Frame.dispose();

        fathersDay2021Frame = new CyderFrame(800,721, new ImageIcon("static/pictures/cards/Philmont.png"));
        fathersDay2021Frame.setTitle("Happy Father's Day!");
        fathersDay2021Frame.setBackground(CyderColors.navy);
        fathersDay2021Frame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

        JLabel cardLabel = new JLabel("<html>Happy fathers day to the best man I'll ever know. " +
                "Thanks so much for helping me move out from college everytime I need it and for all you do, " +
                "have done, continue to do, and will do.<br/><br/>Love,<br/>Nathan</html>");
        cardLabel.setForeground(CyderColors.navy);
        cardLabel.setFont(CyderFonts.segoe20.deriveFont(24f));
        cardLabel.setHorizontalAlignment(JLabel.CENTER);
        cardLabel.setVerticalAlignment(JLabel.CENTER);
        cardLabel.setBounds(5,40, fathersDay2021Frame.getWidth() - 40, 240);
        fathersDay2021Frame.getContentPane().add(cardLabel);

        fathersDay2021Frame.setLocationRelativeTo(GenesisShare.getDominantFrame());
        fathersDay2021Frame.setVisible(true);
    }

    @Widget(trigger = "Birthday card 2021", description = "Birthday card for my dad for the year 2021")
    public static void Birthday2021() {
        if (birthday2021Frame != null)
            birthday2021Frame.dispose();

        birthday2021Frame = new CyderFrame(800,600, new ImageIcon("static/pictures/cards/Confetti.png"));
        birthday2021Frame.setTitle("Happy Birthday Day!");
        birthday2021Frame.setBackground(CyderColors.navy);
        birthday2021Frame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

        JLabel cardLabel = new JLabel("<html>Happy Birthday Dad! Thanks for everything " +
                "this year such as helpng me move into BR and back home, the Destin and Kentucky " +
                "trips, and for bringing my desk up next weekend :D<br/><br/>Love,<br/>Nathan</html>");
        cardLabel.setForeground(CyderColors.navy);
        cardLabel.setFont(CyderFonts.segoe20.deriveFont(24f));
        cardLabel.setHorizontalAlignment(JLabel.CENTER);
        cardLabel.setVerticalAlignment(JLabel.CENTER);
        cardLabel.setBounds(5,40, birthday2021Frame.getWidth() - 40, 240);
        birthday2021Frame.getContentPane().add(cardLabel);

        birthday2021Frame.setLocationRelativeTo(GenesisShare.getDominantFrame());
        birthday2021Frame.setVisible(true);

        try {
            new Thread(() -> {
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }

                birthday2021Frame.notify("WOOOOOO HOOOOOOOOOOOOO", 3000, NotificationDirection.TOP, null);
                birthday2021Frame.notify("WOOOOOO HOOOOOOOOOOOOO", 3000, NotificationDirection.TOP_LEFT, null);
                birthday2021Frame.notify("WOOOOOO HOOOOOOOOOOOOO", 3000, NotificationDirection.BOTTOM, null);
                birthday2021Frame.notify("WOOOOOO HOOOOOOOOOOOOO", 3000, NotificationDirection.TOP_RIGHT, null);
            },"Birthday card 2021 notification wait thread").start();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    @Widget(trigger = "Christmas card 2021", description = "Christmas card for the year of 2021")
    public static void Christmas2021() {
        if (christmas2021Frame != null)
            christmas2021Frame.dispose();

        christmas2021Frame = new CyderFrame(800,800);
        christmas2021Frame.setTitle("Merry Christmas!");
        christmas2021Frame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

        CyderLabel cardLabel = new CyderLabel("<html>Dear Mom and Dad,<br/><br/>" +
                "Thank yall so much for everything this year. 2021 went by insanely quickly " +
                "and I wish I had more time at college, time at home, and so on. " +
                "But time waits for no one, so I too must push forward to the next stage of my life." +
                "<br/><br/>I can't wait to start my career at Camgian and be back in Starkville with all my friends where it now truly feels like home. " +
                "Feel free to swing by or fly up one weekend to see me or perhaps do something for Karla's spring break." +
                " Thanks for this vacation, Destin for fall break, and putting up with me while I lived at home this past spring semester." +
                "<br/><br/>Love,<br/>" +
                "Nathan</html>");
        cardLabel.setFont(CyderFonts.segoe20.deriveFont(24f));
        cardLabel.setBounds(40, 40, christmas2021Frame.getWidth() - 80,christmas2021Frame.getHeight() - 80);
        christmas2021Frame.getContentPane().add(cardLabel);
        cardLabel.setRippleChars(25);
        cardLabel.setRippleMsTimeout(20);
        cardLabel.setRippling(true);

        christmas2021Frame.addPreCloseAction(() -> cardLabel.setRippling(false));

        christmas2021Frame.setLocationRelativeTo(GenesisShare.getDominantFrame());
        christmas2021Frame.setVisible(true);
    }
}
