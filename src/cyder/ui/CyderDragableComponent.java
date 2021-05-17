package cyder.ui;

import cyder.utilities.IOUtil;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class CyderDragableComponent implements MouseMotionListener {
    int xMouse;
    int yMouse;

    @Override
    public void mouseDragged(MouseEvent e) {
        if (IOUtil.getSystemData("UILOC").equals("1")) {
            JFrame refFrame = (JFrame) SwingUtilities.windowForComponent(e.getComponent());
            int x = (int) (e.getLocationOnScreen().getX() - refFrame.getX() - xMouse);
            int y = (int) (e.getLocationOnScreen().getY() - refFrame.getY() - yMouse);

            if (x >= 0 && y >= 0 && x < refFrame.getWidth() && y < refFrame.getHeight()) {
                e.getComponent().setLocation(x,y);
                System.out.println(x + "," + y);
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        xMouse = e.getX();
        yMouse = e.getY();
    }
}
