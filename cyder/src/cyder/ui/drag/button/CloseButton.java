package cyder.ui.drag.button;

import com.google.common.base.Preconditions;
import cyder.annotations.ForReadability;
import cyder.constants.CyderColors;
import cyder.handlers.internal.Logger;
import cyder.ui.drag.DragLabelButtonSize;
import cyder.ui.frame.CyderFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A close button for CyderFrame drag labels.
 */
public class CloseButton extends JLabel {
    /**
     * The default size of the close button.
     */
    private static final DragLabelButtonSize DEFAULT_SIZE = DragLabelButtonSize.SMALL;

    /**
     * The frame this close button will be placed on.
     */
    private final CyderFrame effectFrame;

    /**
     * The size this close button will be painted with.
     */
    private final DragLabelButtonSize size;

    /**
     * Whether the mouse is currently inside of this component.
     */
    private final AtomicBoolean mouseIn = new AtomicBoolean();

    /**
     * Whether this close button is focused.
     */
    private final AtomicBoolean focused = new AtomicBoolean();

    /**
     * Constructs a new close button.
     *
     * @param effectFrame the frame this close button will be on
     */
    public CloseButton(CyderFrame effectFrame) {
        this(effectFrame, DEFAULT_SIZE);
    }

    /**
     * The text for the close button.
     */
    private static final String CLOSE = "Close";

    /**
     * Constructs a new close button.
     *
     * @param effectFrame the frame this close button will be on
     * @param size        the size of this close button
     */
    public CloseButton(CyderFrame effectFrame, DragLabelButtonSize size) {
        this.effectFrame = Preconditions.checkNotNull(effectFrame);
        this.size = Preconditions.checkNotNull(size);

        addMouseListener(generateMouseAdapter(this));
        addFocusListener(generateFocusAdapter(this));

        setToolTipText(CLOSE);
        setFocusable(true);
        setSize(size.getSize(), size.getSize());
        repaint();
    }

    /**
     * The default action to invoke when this close button is pressed.
     */
    private void defaultCloseAction() {
        Logger.log(Logger.Tag.UI_ACTION, this);
        effectFrame.dispose();
    }

    /**
     * The action to invoke when this close button is pressed.
     */
    private Runnable closeAction = this::defaultCloseAction;

    /**
     * Sets the close action to the provided runnable.
     *
     * @param closeAction the action to invoke when this close button is pressed
     */
    public void setCloseAction(Runnable closeAction) {
        this.closeAction = Preconditions.checkNotNull(closeAction);
    }

    /**
     * Generates the default mouse adapter for this close button.
     *
     * @param closeButton the close button for the mouse adapter to be added to
     * @return the mouse adapter
     */
    @ForReadability
    private MouseAdapter generateMouseAdapter(CloseButton closeButton) {
        Preconditions.checkNotNull(closeButton);

        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                closeAction.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setMouseIn(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setMouseIn(false);
            }
        };
    }

    /**
     * Generates the default focus adapter for the provided close button button.
     *
     * @param closeButton the close button for the mouse adapter to be added to
     * @return the focus adapter
     */
    @ForReadability
    private FocusAdapter generateFocusAdapter(CloseButton closeButton) {
        Preconditions.checkNotNull(closeButton);

        return new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                closeButton.setFocused(true);
                runFocusGainedActions();
            }

            @Override
            public void focusLost(FocusEvent e) {
                closeButton.setFocused(false);
                runFocusLostActions();
            }
        };
    }

    /**
     * Sets whether the mouse is inside of the bounds of this close button.
     * Repaint is also invoked.
     *
     * @param mouseIn whether the mouse is inside of the bounds of this close button
     */
    private void setMouseIn(boolean mouseIn) {
        this.mouseIn.set(mouseIn);
        repaint();
    }

    /**
     * Sets whether this close button is focused.
     * Repaint is also invoked.
     *
     * @param focused whether this close button is focused
     */
    private void setFocused(boolean focused) {
        this.focused.set(focused);
        repaint();
    }

    /**
     * Returns the actual size of the painted close button after accounting for padding.
     *
     * @return the actual size of the painted close button after accounting for padding
     */
    private int getPaintLength() {
        Preconditions.checkNotNull(size);
        return size.getSize() - 2 * PAINT_PADDING;
    }

    /**
     * The padding between the edges of the painted close button.
     */
    private static final int PAINT_PADDING = 4;

    /**
     * The length of the rectangles drawn for this close button.
     */
    private static final int drawnRectangleLength = 2;

    /**
     * The value to subtract from the second line drawn by the rectangles.
     */
    private static final int secondLineSubtrahend = 1;

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(PAINT_PADDING, PAINT_PADDING);
        g2d.setColor(getPaintColor());

        for (int i = 0 ; i < getPaintLength() ; i++) {
            g2d.drawRect(i, i, drawnRectangleLength, drawnRectangleLength);
        }

        for (int i = 0 ; i < getPaintLength() ; i++) {
            g2d.drawRect(i, getPaintLength() - i - secondLineSubtrahend, drawnRectangleLength, drawnRectangleLength);
        }

        super.paint(g);
    }

    /**
     * The focus and hover color for this close button.
     */
    private final Color focusedAndHoverColor = CyderColors.regularRed;

    /**
     * The default color for this close button.
     */
    private final Color defaultColor = CyderColors.vanilla;

    /**
     * Returns the color to paint for the close button based on the current state.
     *
     * @return the color to paint for the close button based on the current state
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
