package cyder.ui.drag.button;

import com.google.common.base.Preconditions;
import cyder.annotations.ForReadability;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.ToStringUtil;
import cyder.ui.UiUtil;
import cyder.ui.drag.DragLabelButtonSize;

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
    protected static final DragLabelButtonSize DEFAULT_SIZE =
            UiUtil.dragLabelButtonSizeFromString(Props.dragLabelButtonSize.getValue());

    /**
     * Whether this drag label button is focused.
     */
    private final AtomicBoolean focused = new AtomicBoolean();

    /**
     * Whether the mouse is currently inside of this drag label button.
     */
    private final AtomicBoolean mouseIn = new AtomicBoolean();

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
        setFocusPaintable(false);
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
                Logger.log(LogTag.UI_ACTION, CyderDragLabelButton.this);
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
    public void setFocusPaintable(boolean focusPaintable) {
        if (focusPaintable) addDefaultFocusAdapter();
        setFocusable(focusPaintable);
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
     * The actions to invoke on a mouse over event.
     */
    private final ArrayList<Runnable> mouseOverActions = new ArrayList<>();

    /**
     * The actions to invoke on a mouse exit event.
     */
    private final ArrayList<Runnable> mouseExitActions = new ArrayList<>();

    /**
     * The actions to invoke on a focus gained event.
     */
    private final ArrayList<Runnable> focusGainedActions = new ArrayList<>();

    /**
     * The actions to invoke on a focus lost event.
     */
    private final ArrayList<Runnable> focusLostActions = new ArrayList<>();

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
        clickActions.forEach(Runnable::run);
    }

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
        mouseExitActions.forEach(Runnable::run);
    }

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
        focusGainedActions.forEach(Runnable::run);
    }

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
        focusLostActions.forEach(Runnable::run);
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
                + ", parent=" + ToStringUtil.getComponentParentFrameRepresentation(this)
                + ", clickActions: " + clickActions.size()
                + ", mouseEnterActions: " + mouseOverActions.size()
                + ", mouseExitActions: " + mouseExitActions.size()
                + "}";
    }
}
