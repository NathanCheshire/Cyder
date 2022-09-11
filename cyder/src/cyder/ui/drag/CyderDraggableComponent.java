package cyder.ui.drag;

import cyder.genesis.PropLoader;
import cyder.handlers.internal.Logger;
import cyder.utils.ReflectionUtil;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * A mouse motion listener to allow a component to be dragged during runtime.
 */
public class CyderDraggableComponent implements MouseMotionListener {
    private int xMouse;
    private int yMouse;

    /**
     * Constructs a new draggable component.
     */
    public CyderDraggableComponent() {
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void mouseDragged(MouseEvent e) {
        if (PropLoader.getBoolean("components_relocatable")) {
            JFrame refFrame = (JFrame) SwingUtilities.windowForComponent(e.getComponent());
            int x = (int) (e.getLocationOnScreen().getX() - refFrame.getX() - xMouse);
            int y = (int) (e.getLocationOnScreen().getY() - refFrame.getY() - yMouse);

            if (x >= 0 && y >= 0 && x < refFrame.getWidth() && y < refFrame.getHeight()) {
                e.getComponent().setLocation(x, y);
                Logger.log(Logger.Tag.DEBUG, x + "," + y);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void mouseMoved(MouseEvent e) {
        xMouse = e.getX();
        yMouse = e.getY();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
