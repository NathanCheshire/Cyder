package com.cyder.ui;

import com.cyder.utilities.Util;

import javax.swing.*;

public class SpecialDay {
    private boolean kill = false;
    private JLayeredPane parentPanel;

    private Util mainUtil;

    public SpecialDay(JLayeredPane parentPanel) {
        mainUtil = new Util();

        if (!kill) {
            if (mainUtil.isChristmas())
                notify("Merry Christmas!", 3000, Notification.TOP_ARROW, Notification.TOP_VANISH,parentPanel, 200);

            if (mainUtil.isHalloween())
                notify("Happy Halloween!", 3000, Notification.TOP_ARROW, Notification.TOP_VANISH,parentPanel, 200);

            if (mainUtil.isIndependenceDay())
                notify("Happy 4th of July!", 3000, Notification.TOP_ARROW, Notification.TOP_VANISH,parentPanel, 200);

            if (mainUtil.isThanksgiving())
                notify("Happy Thanksgiving!", 3000, Notification.TOP_ARROW, Notification.TOP_VANISH,parentPanel, 230);

            if (mainUtil.isAprilFoolsDay())
                notify("Happy April Fools Day!", 3000, Notification.TOP_ARROW, Notification.TOP_VANISH,parentPanel, 250);

            kill = true;
        }
    }

    public void notify(String htmltext, int delay, int arrowDir, int vanishDir, JLayeredPane parent, int width) {
        Notification consoleNotification = new Notification();

        int w = width;
        int h = 30;

        consoleNotification.setWidth(w);
        consoleNotification.setHeight(h);
        consoleNotification.setArrow(arrowDir);

        JLabel text = new JLabel(htmltext);
        text.setFont(mainUtil.weatherFontSmall);
        text.setForeground(mainUtil.navy);
        text.setBounds(14,10,w * 2,h);
        consoleNotification.add(text);
        consoleNotification.setBounds(parent.getWidth() - (w + 30),30,w * 2,h * 2);
        parent.add(consoleNotification,1,0);
        parent.repaint();

        consoleNotification.vanish(vanishDir, parent, delay);
    }
}
