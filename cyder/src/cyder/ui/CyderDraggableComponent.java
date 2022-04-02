package cyder.ui;

import cyder.enums.LoggerTag;
import cyder.genesis.CyderShare;
import cyder.handlers.internal.Logger;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class CyderDraggableComponent implements MouseMotionListener {
    private int xMouse;
    private int yMouse;

    public CyderDraggableComponent() {
        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    @Override
    public final void mouseDragged(MouseEvent e) {
        if (CyderShare.COMPONENTS_RELOCATABLE) {
            JFrame refFrame = (JFrame) SwingUtilities.windowForComponent(e.getComponent());
            int x = (int) (e.getLocationOnScreen().getX() - refFrame.getX() - xMouse);
            int y = (int) (e.getLocationOnScreen().getY() - refFrame.getY() - yMouse);

            if (x >= 0 && y >= 0 && x < refFrame.getWidth() && y < refFrame.getHeight()) {
                e.getComponent().setLocation(x,y);
                Logger.log(LoggerTag.DEBUG, x + "," + y);
            }
        }
    }

    @Override
    public final void mouseMoved(MouseEvent e) {
        xMouse = e.getX();
        yMouse = e.getY();
    }

    @Override
    public final String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
