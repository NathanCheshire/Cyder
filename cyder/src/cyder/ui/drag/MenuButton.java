package cyder.ui.drag;

import com.google.common.base.Preconditions;
import cyder.annotations.ForReadability;
import cyder.constants.CyderColors;
import cyder.handlers.internal.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A menu button for a drag label.
 */
public class MenuButton extends JLabel {
    /**
     * The default size of the menu button.
     */
    private static final DragLabelButtonSize DEFAULT_SIZE = DragLabelButtonSize.SMALL;

    /**
     * The size this menu button will be painted with.
     */
    private final DragLabelButtonSize size;

    /**
     * Whether the mouse is currently inside of this component.
     */
    private final AtomicBoolean mouseIn = new AtomicBoolean();

    /**
     * Whether this menu button is focused.
     */
    private final AtomicBoolean focused = new AtomicBoolean();

    /**
     * The text for the menu button.
     */
    private static final String MENU = "Menu";

    /**
     * Constructs a new menu button.
     */
    public MenuButton() {
        this(DEFAULT_SIZE);
    }

    /**
     * Constructs a new menu button.
     *
     * @param size the size of this menu button
     */
    public MenuButton(DragLabelButtonSize size) {
        this.size = Preconditions.checkNotNull(size);

        addMouseListener(generateMouseAdapter(this));
        addFocusListener(generateFocusAdapter(this));

        setToolTipText(MENU);
        setFocusable(true);
        setSize(size.getSize(), size.getSize());
        repaint();
    }

    /**
     * The default action to invoke when this menu button is pressed.
     */
    private void defaultMenuAction() {
        Logger.log(Logger.Tag.UI_ACTION, this);
    }

    /**
     * The action to invoke when this menu button is pressed.
     */
    private Runnable menuAction = this::defaultMenuAction;

    /**
     * Sets the menu action to the provided runnable.
     *
     * @param menuAction the action to invoke when this menu button is pressed
     */
    public void setMenuAction(Runnable menuAction) {
        this.menuAction = Preconditions.checkNotNull(menuAction);
    }

    /**
     * Generates the default mouse adapter for this menu button.
     *
     * @param menuButton the menu button for the mouse adapter to be added to
     * @return the mouse adapter
     */
    @ForReadability
    private MouseAdapter generateMouseAdapter(MenuButton menuButton) {
        Preconditions.checkNotNull(menuButton);

        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                menuAction.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                menuButton.setMouseIn(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                menuButton.setMouseIn(false);
            }
        };
    }

    /**
     * Generates the default focus adapter for the provided menu button button.
     *
     * @param menuButton the menu button for the mouse adapter to be added to
     * @return the focus adapter
     */
    @ForReadability
    private FocusAdapter generateFocusAdapter(MenuButton menuButton) {
        Preconditions.checkNotNull(menuButton);

        return new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                menuButton.setFocused(true);
                runFocusGainedActions();
            }

            @Override
            public void focusLost(FocusEvent e) {
                menuButton.setFocused(false);
                runFocusLostActions();
            }
        };
    }

    /**
     * Sets whether the mouse is inside of the bounds of this menu button.
     * Repaint is also invoked.
     *
     * @param mouseIn whether the mouse is inside of the bounds of this menu button
     */
    private void setMouseIn(boolean mouseIn) {
        this.mouseIn.set(mouseIn);
        repaint();
    }

    /**
     * Sets whether this menu button is focused.
     * Repaint is also invoked.
     *
     * @param focused whether this menu button is focused
     */
    private void setFocused(boolean focused) {
        this.focused.set(focused);
        repaint();
    }

    /**
     * Returns the actual size of the painted menu button after accounting for padding.
     *
     * @return the actual size of the painted menu button after accounting for padding
     */
    private int getPaintLength() {
        Preconditions.checkNotNull(size);
        return size.getSize() - 2 * PAINT_PADDING;
    }

    /**
     * The padding between the edges of the painted menu button.
     */
    private static final int PAINT_PADDING = 4;

    /**
     * The size of the rectangles to draw in the paint method.
     */
    private static final int drawnRectangleLength = 2;

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(PAINT_PADDING, PAINT_PADDING);

        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(getPaintColor());

        for (int i = 0 ; i <= getPaintLength() ; i++) {
            g2d.fillRect(i / 2, i, drawnRectangleLength, drawnRectangleLength);
            g2d.fillRect(getPaintLength() - i / 2, i, drawnRectangleLength, drawnRectangleLength);
        }

        super.paint(g);
    }

    /**
     * The focus and hover color for this menu button.
     */
    private final Color focusedAndHoverColor = CyderColors.regularRed;

    /**
     * The default color for this menu button.
     */
    private final Color defaultColor = CyderColors.vanilla;

    /**
     * Returns the color to paint for the menu button based on the current state.
     *
     * @return the color to paint for the menu button based on the current state
     */
    private Color getPaintColor() {
        if (mouseIn.get() || focused.get()) {
            return focusedAndHoverColor;
        } else {
            return defaultColor;
        }
    }

    /**
     * The list of focus lost actions.
     */
    private final ArrayList<Runnable> focusLostActions = new ArrayList<>();

    /**
     * The list of focus gained actions.
     */
    private final ArrayList<Runnable> focusGainedActions = new ArrayList<>();

    /**
     * Adds the provided focus lost action to the focus lost actions list.
     *
     * @param focusLostAction the focus lost action
     */
    public void addFocusLostAction(Runnable focusLostAction) {
        Preconditions.checkNotNull(focusLostAction);

        focusLostActions.add(focusLostAction);
    }

    /**
     * Removes the provided focus lost action from the focus lost actions list.
     *
     * @param focusLostAction the focus lost action
     */
    public void removeFocusLostAction(Runnable focusLostAction) {
        Preconditions.checkNotNull(focusLostAction);

        focusLostActions.remove(focusLostAction);
    }

    /**
     * Adds the provided focus gained action to the focus lost actions list.
     *
     * @param focusGainedAction the focus gained action
     */
    public void addFocusGainedAction(Runnable focusGainedAction) {
        Preconditions.checkNotNull(focusGainedAction);

        focusGainedActions.add(focusGainedAction);
    }

    /**
     * Removes the provided focus gained action from the focus lost actions list.
     *
     * @param focusGainedAction the focus gained action
     */
    public void removeFocusGainedAction(Runnable focusGainedAction) {
        Preconditions.checkNotNull(focusGainedAction);

        focusGainedActions.remove(focusGainedAction);
    }

    /**
     * Runs all the focus gained actions.
     */
    private void runFocusGainedActions() {
        for (Runnable focusGainedAction : focusGainedActions) {
            focusGainedAction.run();
        }
    }

    /**
     * Runs all the focus lost actions.
     */
    private void runFocusLostActions() {
        for (Runnable focusLostAction : focusLostActions) {
            focusLostAction.run();
        }
    }
}
