package cyder.ui.drag.button;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.ui.drag.DragLabelButtonSize;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An icon button for a drag label.
 */
public class CyderDragLabelButton extends JLabel implements ICyderDragLabelButton {
    /**
     * Constructs a new drag label button.
     */
    public CyderDragLabelButton() {
        this(DEFAULT_SIZE);
    }

    /**
     * Constructs a new drag label button with the provided size.
     *
     * @param size the size of the drag label button.
     */
    public CyderDragLabelButton(DragLabelButtonSize size) {
        Preconditions.checkNotNull(size);
        throw new IllegalMethodException(CyderStrings.NOT_IMPLEMENTED);
    }

    /**
     * The logic to invoke to paint the button.
     *
     * @param g the graphics object
     * @throws IllegalMethodException always unless overridden by base class
     */
    @Override
    public void paintLogic(Graphics g) {
        throw new IllegalMethodException(CyderStrings.NOT_IMPLEMENTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDefaultMouseAdapter() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                invokeClickActions();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                invokeMouseOverActions();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                invokeMouseExitActions();
                repaint();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDefaultFocusAdapter() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                invokeFocusGainedActions();
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                invokeFocusLostActions();
                repaint();
            }
        });
    }

    /**
     * Whether this drag label button is focused.
     */
    private final AtomicBoolean focused = new AtomicBoolean();

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFocused(boolean focused) {
        this.focused.set(focused);
        repaint();
    }

    /**
     * Whether the mouse is currently inside of this drag label button.
     */
    private final AtomicBoolean mouseIn = new AtomicBoolean();

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMouseIn(boolean mouseIn) {
        this.mouseIn.set(mouseIn);
        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        paintLogic(g);
        super.paint(g);
    }

    /**
     * The color to use when painting the default state of this button.
     */
    @SuppressWarnings("UnusedAssignment")
    private Color paintColor = defaultColor;

    /**
     * The color to use when painting the hover and focus states of this button.
     */
    @SuppressWarnings("UnusedAssignment")
    private Color hoverAndFocusPaintColor = defaultHoverAndFocusColor;

    /**
     * Sets the color to paint for the default state of this button.
     *
     * @param paintColor the paint color
     */
    public void setPaintColor(Color paintColor) {
        Preconditions.checkNotNull(paintColor);

        this.paintColor = paintColor;
    }

    /**
     * Sets the color to paint for the hover and focus state of this button.
     *
     * @param hoverAndFocusPaintColor the hover and focus paint color
     */
    public void setHoverAndFocusPaintColor(Color hoverAndFocusPaintColor) {
        Preconditions.checkNotNull(hoverAndFocusPaintColor);

        this.hoverAndFocusPaintColor = hoverAndFocusPaintColor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getPaintColor() {
        if (mouseIn.get() || focused.get()) {
            return hoverAndFocusPaintColor;
        } else {
            return paintColor;
        }
    }
}
