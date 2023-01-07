package cyder.ui.drag;

import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.Props;
import cyder.strings.ToStringUtil;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * A {@link MouseMotionListener} to allow a component to be dragged during on its parent during runtime.
 * This class is valid for any component added to an instance of a {@link JFrame}.
 */
public class CyderDraggableComponent implements MouseMotionListener {
    /**
     * The current x location of the mouse relative to the parent component\.
     */
    private int xMouse;

    /**
     * The current y location of the mouse relative to the parent component\.
     */
    private int yMouse;

    /**
     * Constructs a new draggable component.
     */
    public CyderDraggableComponent() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void mouseDragged(MouseEvent e) {
        if (!Props.componentsRelocatable.getValue()) return;

        JFrame refFrame = (JFrame) SwingUtilities.windowForComponent(e.getComponent());
        int x = (int) (e.getLocationOnScreen().getX() - refFrame.getX() - xMouse);
        int y = (int) (e.getLocationOnScreen().getY() - refFrame.getY() - yMouse);

        if (x >= 0 && y >= 0 && x < refFrame.getWidth() && y < refFrame.getHeight()) {
            e.getComponent().setLocation(x, y);
            Logger.log(LogTag.UI_ACTION, x + "," + y);
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
        return ToStringUtil.commonCyderToString(this);
    }
}
