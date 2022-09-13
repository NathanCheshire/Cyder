package cyder.ui.drag.button;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.ui.drag.DragLabelButtonSize;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
        setSize(size);
        addEnterListenerKeyAdapter();
        addDefaultMouseAdapter();
        setForConsole(false);
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
                setMouseIn(true);
                invokeMouseOverActions();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setMouseIn(false);
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
                setFocused(true);
                invokeFocusGainedActions();
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                setFocused(false);
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
    public void setForConsole(boolean forConsole) {
        if (forConsole) {
            addDefaultFocusAdapter();
        }

        setFocusable(forConsole);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        throw new IllegalMethodException(CyderStrings.NOT_IMPLEMENTED);
    }

    /**
     * The color to use when painting the default state of this button.
     */
    private Color paintColor = defaultColor;

    /**
     * The color to use when painting the hover and focus states of this button.
     */
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

    /**
     * Adds the default key adapter to invoke all click actions on the enter key press.
     */
    public void addEnterListenerKeyAdapter() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    invokeClickActions();
                }
            }
        });
    }

    /*
    Note to maintainers: the below method forces extending classes to override
    the below method which also forces them to keep track of their own non-final size variable.
     */

    /**
     * Sets the size of this drag label button.
     *
     * @param size the size of this drag label button
     */
    @Override
    public void setSize(DragLabelButtonSize size) {
        throw new IllegalMethodException(CyderStrings.NOT_IMPLEMENTED);
    }
}
