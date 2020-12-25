package com.cyder.ui;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.enums.ArrowDirection;
import com.cyder.enums.VanishDirection;
import com.cyder.utilities.TimeUtil;

import javax.swing.*;

//todo consolidate with console frame
public class SpecialDay {
    private boolean kill = false;
    private JLayeredPane parentPanel;

    public SpecialDay(JLayeredPane parentPanel) {

        if (!kill) {
            if (TimeUtil.isChristmas())
                //todo notify("Merry Christmas!", 3000, ArrowDirection.TOP,parentPanel, 200);

            if (TimeUtil.isHalloween())
                //todo notify("Happy Halloween!", 3000, ArrowDirection.TOP, VanishDirection.TOP,parentPanel, 200);

            if (TimeUtil.isIndependenceDay())
                //todo notify("Happy 4th of July!", 3000, ArrowDirection.TOP, VanishDirection.TOP,parentPanel, 200);

            if (TimeUtil.isThanksgiving())
                //todo notify("Happy Thanksgiving!", 3000, ArrowDirection.TOP, VanishDirection.TOP,parentPanel, 230);

            if (TimeUtil.isAprilFoolsDay())
                //todo notify("Happy April Fools Day!", 3000, ArrowDirection.TOP, VanishDirection.TOP,parentPanel, 250);

            kill = true;
        }
    }
}
