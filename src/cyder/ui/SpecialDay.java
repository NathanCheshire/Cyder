package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.enums.ArrowDirection;
import cyder.enums.StartDirection;
import cyder.enums.VanishDirection;
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
                notify("Merry Christmas!", 3000, ArrowDirection.TOP,StartDirection.TOP,VanishDirection.TOP, 200);

            if (TimeUtil.isHalloween())
                notify("Happy Halloween!", 3000, ArrowDirection.TOP,StartDirection.TOP,VanishDirection.TOP, 200);

            if (TimeUtil.isIndependenceDay())
                notify("Happy 4th of July!", 3000, ArrowDirection.TOP,StartDirection.TOP,VanishDirection.TOP, 200);

            if (TimeUtil.isThanksgiving())
                notify("Happy Thanksgiving!", 3000, ArrowDirection.TOP,StartDirection.TOP,VanishDirection.TOP, 230);

            if (TimeUtil.isAprilFoolsDay())
                notify("Happy April Fool Day!", 3000, ArrowDirection.TOP,StartDirection.TOP,VanishDirection.TOP, 250);

            kill = true;
        }
    }

    //todo this will go away and we will call, ConsoleFrame.notify
    public void notify(String htmltext, int viewDuration, ArrowDirection arrowDir, StartDirection startDir, VanishDirection vanishDir, int width) {
        Notification frameNotification = new Notification();

        int w = width;
        int h = 30;

        frameNotification.setArrow(arrowDir);

        JLabel text = new JLabel();
        text.setText(htmltext);

        int lastIndex = 0;

        while(lastIndex != -1){

            lastIndex = text.getText().indexOf("<br/>",lastIndex);

            if(lastIndex != -1){
                h += 30;
                lastIndex += "<br/>".length();
            }
        }

        frameNotification.setWidth(w);
        frameNotification.setHeight(h);

        text.setFont(CyderFonts.weatherFontSmall);
        text.setForeground(CyderColors.navy);
        text.setBounds(14,10,w * 2,h);
        frameNotification.add(text);

        if (startDir == StartDirection.LEFT)
            frameNotification.setBounds(0,30,w * 2,h * 2);
        else if (startDir == StartDirection.RIGHT)
            frameNotification.setBounds(this.parentPanel.getWidth() - (w + 30),32,w * 2,h * 2);
        else
            frameNotification.setBounds(this.parentPanel.getWidth() / 2 - (w / 2),32,w * 2,h * 2);

        this.parentPanel.add(frameNotification,1,0);
        this.parentPanel.repaint();

        frameNotification.appear(startDir, this.parentPanel);
        frameNotification.vanish(vanishDir, this.parentPanel, viewDuration);
    }
}
