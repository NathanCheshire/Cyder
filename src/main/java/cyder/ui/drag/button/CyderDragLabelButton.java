package main.java.cyder.ui.drag.button;

import com.google.common.base.Preconditions;
import main.java.cyder.annotations.ForReadability;
import main.java.cyder.exceptions.FatalException;
import main.java.cyder.logging.LogTag;
import main.java.cyder.logging.Logger;
import main.java.cyder.props.Props;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.strings.ToStringUtils;
import main.java.cyder.ui.drag.DragLabelButtonSize;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An icon button for a drag label.
 */
public abstract class CyderDragLabelButton extends JLabel implements ICyderDragLabelButton {
    /**
     * The default size of a drag label button.
     */
    public static final DragLabelButtonSize DEFAULT_SIZE;

    static {
        // todo method for this, in UiUtil maybe
        String size = Props.dragLabelButtonSize.getValue();

        DEFAULT_SIZE = switch (size) {
            case "small" -> DragLabelButtonSize.SMALL;
            case "medium" -> DragLabelButtonSize.MEDIUM;
            case "large" -> DragLabelButtonSize.LARGE;
            case "full_drag_label" -> DragLabelButtonSize.FULL_DRAG_LABEL;
            default -> throw new FatalException("Invalid drag label button size specified by prop: " + size);
        };
    }

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
        addLogOnClickActionMouseAdapter();
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
                if (e.getButton() == MouseEvent.BUTTON1) {
                    invokeClickActions();
                }
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
     * Adds the mouse adapter to lock the click on click actions.
     */
    @ForReadability
    public void addLogOnClickActionMouseAdapter() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(LogTag.UI_ACTION, CyderDragLabelButton.this.toString());
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
     * {@inheritDoc}
     */
    @Override
    public boolean getFocused() {
        return focused.get();
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
    public boolean getMouseIn() {
        return mouseIn.get();
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
        paintDragLabelButton(g);
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
     * {}
     */
    public boolean mouseIn() {
        return mouseIn.get();
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

    /**
     * Sets the size of this drag label button.
     *
     * @param size the size of this drag label button
     */
    abstract public void setSize(DragLabelButtonSize size);

    // -----------
    // Hooks logic
    // -----------

    /**
     * The actions to invoke when this button is pressed.
     */
    private final ArrayList<Runnable> clickActions = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClickAction(Runnable clickAction) {
        Preconditions.checkNotNull(clickAction);
        clearClickActions();
        clickActions.add(clickAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addClickAction(Runnable clickAction) {
        Preconditions.checkNotNull(clickAction);
        clickActions.add(clickAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeClickAction(Runnable clickAction) {
        Preconditions.checkNotNull(clickAction);
        clickActions.remove(clickAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearClickActions() {
        clickActions.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invokeClickActions() {
        for (Runnable clickAction : clickActions) {
            clickAction.run();
        }
    }

    /**
     * The actions to invoke on a mouse over event.
     */
    private final ArrayList<Runnable> mouseOverActions = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMouseOverAction(Runnable mouseOverAction) {
        Preconditions.checkNotNull(mouseOverAction);
        clearMouseOverActions();
        mouseOverActions.add(mouseOverAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMouseOverAction(Runnable mouseOverAction) {
        Preconditions.checkNotNull(mouseOverAction);

        mouseOverActions.add(mouseOverAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeMouseOverAction(Runnable mouseOverAction) {
        Preconditions.checkNotNull(mouseOverAction);

        mouseOverActions.remove(mouseOverAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearMouseOverActions() {
        mouseOverActions.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invokeMouseOverActions() {
        for (Runnable mouseOverAction : mouseOverActions) {
            mouseOverAction.run();
        }
    }

    /**
     * The actions to invoke on a mouse exit event.
     */
    private final ArrayList<Runnable> mouseExitActions = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMouseExitAction(Runnable mouseExitAction) {
        Preconditions.checkNotNull(mouseExitAction);
        clearMouseExitActions();
        mouseExitActions.add(mouseExitAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMouseExitAction(Runnable mouseExitAction) {
        Preconditions.checkNotNull(mouseExitAction);

        mouseExitActions.add(mouseExitAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeMouseExitAction(Runnable mouseExitAction) {
        Preconditions.checkNotNull(mouseExitAction);

        mouseExitActions.remove(mouseExitAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearMouseExitActions() {
        mouseExitActions.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invokeMouseExitActions() {
        for (Runnable mouseExitAction : mouseExitActions) {
            mouseExitAction.run();
        }
    }

    /**
     * The actions to invoke on a focus gained event.
     */
    private final ArrayList<Runnable> focusGainedActions = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFocusGainedAction(Runnable focusGainedAction) {
        Preconditions.checkNotNull(focusGainedAction);
        clearFocusGainedActions();
        focusGainedActions.add(focusGainedAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFocusGainedAction(Runnable focusGainedAction) {
        Preconditions.checkNotNull(focusGainedAction);

        focusGainedActions.add(focusGainedAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFocusGainedAction(Runnable focusGainedAction) {
        Preconditions.checkNotNull(focusGainedAction);

        focusGainedActions.remove(focusGainedAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearFocusGainedActions() {
        focusGainedActions.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invokeFocusGainedActions() {
        for (Runnable focusGainedAction : focusGainedActions) {
            focusGainedAction.run();
        }
    }

    /**
     * The actions to invoke on a focus lost event.
     */
    private final ArrayList<Runnable> focusLostActions = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFocusLostAction(Runnable focusLostAction) {
        Preconditions.checkNotNull(focusLostAction);
        clearFocusLostActions();
        focusLostActions.add(focusLostAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFocusLostAction(Runnable focusLostAction) {
        Preconditions.checkNotNull(focusLostAction);

        focusLostActions.add(focusLostAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFocusLostAction(Runnable focusLostAction) {
        Preconditions.checkNotNull(focusLostAction);

        focusLostActions.remove(focusLostAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearFocusLostActions() {
        focusLostActions.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invokeFocusLostActions() {
        for (Runnable focusLostAction : focusLostActions) {
            focusLostAction.run();
        }
    }

    /**
     * The logic to invoke to paint the drag label button.
     *
     * @param g the graphics object
     */
    abstract public void paintDragLabelButton(Graphics g);

    /**
     * Returns a special string representation of this drag label button to
     * be logged when the button is clicked to differentiate it from other buttons.
     *
     * @return a special string representation of this drag label button
     */
    abstract public String getSpecificStringRepresentation();

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CyderDragLabelButton {"
                + "rep: " + CyderStrings.quote + getSpecificStringRepresentation() + CyderStrings.quote
                + ", x=" + getX()
                + ", y=" + getY()
                + ", w=" + getWidth()
                + ", h=" + getHeight()
                + ", parent=" + ToStringUtils.getComponentParentFrameRepresentation(this)
                + ", clickActions: " + clickActions.size()
                + ", mouseEnterActions: " + mouseOverActions.size()
                + ", mouseExitActions: " + mouseExitActions.size()
                + "}";
    }
}
