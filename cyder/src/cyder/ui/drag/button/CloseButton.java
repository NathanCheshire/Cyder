package cyder.ui.drag.button;

import com.google.common.base.Preconditions;
import cyder.ui.drag.DragLabelButtonSize;

import java.awt.*;

/**
 * A close button for CyderFrame drag labels.
 */
public class CloseButton extends CyderDragLabelButton {
    /**
     * The size this close button will be painted with.
     */
    private DragLabelButtonSize size;

    /**
     * Constructs a new close button.
     */
    public CloseButton() {
        this(DEFAULT_SIZE);
    }

    /**
     * The text for the close button.
     */
    private static final String CLOSE = "Close";

    /**
     * Constructs a new close button.
     *
     * @param size the size of this close button
     */
    public CloseButton(DragLabelButtonSize size) {
        this.size = Preconditions.checkNotNull(size);

        setToolTipText(CLOSE);

        setSize(size.getSize(), size.getSize());
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

    // todo technically all of these should check g not null since the method is exposed
    /**
     * {@inheritDoc}
     */
    @Override
    public void paintDragLabelButton(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(PAINT_PADDING, PAINT_PADDING);
        g2d.setColor(getPaintColor());

        for (int i = 0 ; i < getPaintLength() ; i++) {
            g2d.drawRect(i, i, drawnRectangleLength, drawnRectangleLength);
        }

        for (int i = 0 ; i < getPaintLength() ; i++) {
            g2d.drawRect(i, getPaintLength() - i - secondLineSubtrahend, drawnRectangleLength, drawnRectangleLength);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSize(DragLabelButtonSize size) {
        this.size = Preconditions.checkNotNull(size);
        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpecificStringRepresentation() {
        return CLOSE;
    }
}
