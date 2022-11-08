package cyder.ui.drag;

import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.PropLoader;
import cyder.utils.StringUtil;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * A mouse motion listener to allow a component to be dragged during runtime.
 */
public class CyderDraggableComponent implements MouseMotionListener {
    /**
     * The key to obtain whether the components are relocatable from the props.
     */
    private static final String COMPONENTS_RELOCATABLE = "components_relocatable";

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
        if (!PropLoader.getBoolean(COMPONENTS_RELOCATABLE)) return;

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
        return StringUtil.commonCyderToString(this);
    }
}
