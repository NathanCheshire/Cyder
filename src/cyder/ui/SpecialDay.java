package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.enums.Direction;
import cyder.utilities.TimeUtil;

import javax.swing.*;

//todo consolidate with console frame
public class SpecialDay {
    private boolean kill = false;
    private JLayeredPane parentPanel;

    public SpecialDay(JLayeredPane parentPanel) {
        this.parentPanel = parentPanel;

        if (!kill) {
            if (TimeUtil.isChristmas())
                notify("Merry Christmas!", 3000, Direction.TOP,Direction.TOP,Direction.TOP, 200);

            if (TimeUtil.isHalloween())
                notify("Happy Halloween!", 3000, Direction.TOP,Direction.TOP,Direction.TOP, 200);

            if (TimeUtil.isIndependenceDay())
                notify("Happy 4th of July!", 3000, Direction.TOP,Direction.TOP,Direction.TOP, 200);

            if (TimeUtil.isThanksgiving())
                notify("Happy Thanksgiving!", 3000, Direction.TOP,Direction.TOP,Direction.TOP, 230);

            if (TimeUtil.isAprilFoolsDay())
                notify("Happy April Fool Day!", 3000, Direction.TOP,Direction.TOP,Direction.TOP, 250);

            kill = true;
        }
    }

    //todo this will go away and we will call, ConsoleFrame.notify
    public void notify(String htmltext, int viewDuration, Direction arrowDir, Direction startDir, Direction vanishDir, int width) {

    }
}
