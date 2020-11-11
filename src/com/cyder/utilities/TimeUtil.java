package com.cyder.utilities;

import com.cyder.ui.Notification;

import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    //todo correction for timezone method
    private Util timeUtil = new Util();

    public void notify(String htmltext, int delay, int arrowDir, int vanishDir, JLayeredPane parent, int width) {
        Notification consoleNotification = new Notification();

        int w = 200;
        int h = 120;

        consoleNotification.setWidth(w);
        consoleNotification.setHeight(h);
        consoleNotification.setArrow(arrowDir);

        JLabel text = new JLabel();
        text.setText("<html>"+ "This is a message<br/>This is a message<br/>This is a message<br/>This is a message" +"</html>");
        //todo ahhh so for every break +30 on height it
        text.setFont(timeUtil.weatherFontSmall);
        text.setForeground(timeUtil.navy);
        text.setBounds(14,10,w * 2,h);
        consoleNotification.add(text);
        consoleNotification.setBounds(parent.getWidth() - (w + 30),30,w * 2,h * 2);
        parent.add(consoleNotification,1,0);
        parent.repaint();

        consoleNotification.vanish(vanishDir, parent, delay);
    }

    public String formatDate(LocalDateTime now, DateTimeFormatter dtf) {
        return now.format(dtf);
    }

    public String formatCurrentDate(DateTimeFormatter dtf) {
        return dtf.format(LocalDate.now());
    }
}
