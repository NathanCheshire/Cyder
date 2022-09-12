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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A change size button button for CyderFrame drag labels.
 */
public class ChangeSizeButton extends JLabel {
    /**
     * The default size of the change size button.
     */
    private static final DragLabelButtonSize DEFAULT_SIZE = DragLabelButtonSize.SMALL;

    /**
     * The frame this change size button will be placed on.
     */
    private final CyderFrame effectFrame;

    /**
     * The size this change size button will be painted with.
     */
    private final DragLabelButtonSize size;

    /**
     * Whether the mouse is currently inside of this component.
     */
    private final AtomicBoolean mouseIn = new AtomicBoolean();

    /**
     * Whether this change size button is focused.
     */
    private final AtomicBoolean focused = new AtomicBoolean();

    /**
     * Constructs a new change size button.
     *
     * @param effectFrame the frame this change size button will be on
     */
    public ChangeSizeButton(CyderFrame effectFrame) {
        this(effectFrame, DEFAULT_SIZE);
    }

    /**
     * The text for the change size button.
     */
    private static final String CHANGE_SIZE = "Change size";

    /**
     * Constructs a new change size button.
     *
     * @param effectFrame the frame this change size button will be on
     * @param size        the size of this change size button
     */
    public ChangeSizeButton(CyderFrame effectFrame, DragLabelButtonSize size) {
        this.effectFrame = Preconditions.checkNotNull(effectFrame);
        this.size = Preconditions.checkNotNull(size);

        addMouseListener(generateMouseAdapter(this));
        addFocusListener(generateFocusAdapter(this));

        setToolTipText(CHANGE_SIZE);
        setFocusable(true);
        setSize(size.getSize(), size.getSize());
        repaint();
    }

    /**
     * The default action to invoke when this change size button is pressed.
     */
    private void defaultChangeSizeAction() {
        Logger.log(Logger.Tag.UI_ACTION, this);
        effectFrame.dispose();
    }

    /**
     * The action to invoke when this change size button is pressed.
     */
    private Runnable changeSizeAction = this::defaultChangeSizeAction;

    /**
     * Sets the change size action to the provided runnable.
     *
     * @param changeSizeAction the action to invoke when this change size button is pressed
     */
    public void setChangeSizeAction(Runnable changeSizeAction) {
        this.changeSizeAction = Preconditions.checkNotNull(changeSizeAction);
    }

    /**
     * Generates the default mouse adapter for this change size button.
     *
     * @param changeSizeButton the change size button for the mouse adapter to be added to
     * @return the mouse adapter
     */
    @ForReadability
    private MouseAdapter generateMouseAdapter(ChangeSizeButton changeSizeButton) {
        Preconditions.checkNotNull(changeSizeButton);

        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                changeSizeAction.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                changeSizeButton.setMouseIn(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                changeSizeButton.setMouseIn(false);
            }
        };
    }

    /**
     * Generates the default focus adapter for the provided change size button button.
     *
     * @param changeSizeButton the change size button for the mouse adapter to be added to
     * @return the focus adapter
     */
    @ForReadability
    private FocusAdapter generateFocusAdapter(ChangeSizeButton changeSizeButton) {
        Preconditions.checkNotNull(changeSizeButton);

        return new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                changeSizeButton.setFocused(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                changeSizeButton.setFocused(false);
            }
        };
    }

    /**
     * Sets whether the mouse is inside of the bounds of this change size button.
     * Repaint is also invoked.
     *
     * @param mouseIn whether the mouse is inside of the bounds of this change size button
     */
    private void setMouseIn(boolean mouseIn) {
        this.mouseIn.set(mouseIn);
        repaint();
    }

    /**
     * Sets whether this change size button is focused.
     * Repaint is also invoked.
     *
     * @param focused whether this change size button is focused
     */
    private void setFocused(boolean focused) {
        this.focused.set(focused);
        repaint();
    }

    /**
     * Returns the actual size of the painted change size button after accounting for padding.
     *
     * @return the actual size of the painted change size button after accounting for padding
     */
    private int getPaintLength() {
        Preconditions.checkNotNull(size);
        return size.getSize() - 2 * PAINT_PADDING;
    }

    /**
     * The padding between the edges of the painted change size button.
     */
    private static final int PAINT_PADDING = 4;

    /**
     * The stroke for painting the change size button.
     */
    private static final BasicStroke changeSizeStroke = new BasicStroke(2);

    /**
     * The offset from the top of for the painting the change size button.
     */
    private static final int changeSizeYOffset = 1;

    /**
     * The value to subtract from the paint length when painting the change size button.
     */
    private static final int changeSizeHeightSubtrahend = 2;

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(PAINT_PADDING, PAINT_PADDING);

        g2d.setColor(getPaintColor());
        g2d.setStroke(changeSizeStroke);
        int changeSizeHeight = getPaintLength() - changeSizeHeightSubtrahend;

        g2d.drawRect(0, changeSizeYOffset, getPaintLength(), changeSizeHeight);
        super.paint(g);
    }

    /**
     * The focus and hover color for this change size button.
     */
    private final Color focusedAndHoverColor = CyderColors.regularRed;

    /**
     * The default color for this change size button.
     */
    private final Color defaultColor = CyderColors.vanilla;

    /**
     * Returns the color to paint for the change size button based on the current state.
     *
     * @return the color to paint for the change size button based on the current state
     */
    private Color getPaintColor() {
        if (mouseIn.get() || focused.get()) {
            return focusedAndHoverColor;
        } else {
            return defaultColor;
        }
    }
}
