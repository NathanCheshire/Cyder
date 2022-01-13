package cyder.ui;

import test.java.Debug;
import cyder.utilities.IOUtil;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class CyderDraggableComponent implements MouseMotionListener {
    int xMouse;
    int yMouse;

    @Override
    public void mouseDragged(MouseEvent e) {
        if (IOUtil.getSystemData().isUiloc()) {
            JFrame refFrame = (JFrame) SwingUtilities.windowForComponent(e.getComponent());
            int x = (int) (e.getLocationOnScreen().getX() - refFrame.getX() - xMouse);
            int y = (int) (e.getLocationOnScreen().getY() - refFrame.getY() - yMouse);

            if (x >= 0 && y >= 0 && x < refFrame.getWidth() && y < refFrame.getHeight()) {
                e.getComponent().setLocation(x,y);
                Debug.println(x + "," + y, false, false);
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        xMouse = e.getX();
        yMouse = e.getY();
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
