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
                //notification("Merry Christmas!", 3000, Notification.TOP_ARROW, Notification.TOP_VANISH,parentPanel, 200);

            if (mainUtil.isHalloween())
                //notification("Happy Halloween!", 3000, Notification.TOP_ARROW, Notification.TOP_VANISH,parentPanel, 200);

            if (mainUtil.isIndependenceDay())
                //notification("Happy 4th of July!", 3000, Notification.TOP_ARROW, Notification.TOP_VANISH,parentPanel, 200);

            if (mainUtil.isThanksgiving())
                //notification("Happy Thanksgiving!", 3000, Notification.TOP_ARROW, Notification.TOP_VANISH,parentPanel, 230);

            if (mainUtil.isAprilFoolsDay())
                //notification("Happy April Fools Day!", 3000, Notification.TOP_ARROW, Notification.TOP_VANISH,parentPanel, 250);

            kill = true;
        }
    }
}
