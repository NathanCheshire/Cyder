package cyder.ui.drag.button;

import com.google.common.base.Preconditions;
import cyder.ui.drag.DragLabelButtonSize;

import java.awt.*;

/**
 * A change size button button for CyderFrame drag labels.
 */
public class ChangeSizeButton extends CyderDragLabelButton {
    /**
     * The size this change size button will be painted with.
     */
    private DragLabelButtonSize size;

    /**
     * Constructs a new change size button.
     */
    public ChangeSizeButton() {
        this(DEFAULT_SIZE);
    }

    /**
     * The text for the change size button.
     */
    private static final String CHANGE_SIZE = "Change size";

    /**
     * Constructs a new change size button.
     *
     * @param size the size of this change size button
     */
    public ChangeSizeButton(DragLabelButtonSize size) {
        this.size = Preconditions.checkNotNull(size);

        setToolTipText(CHANGE_SIZE);

        setSize(size.getSize(), size.getSize());
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSize(DragLabelButtonSize size) {
        this.size = Preconditions.checkNotNull(size);
        repaint();
    }
}
