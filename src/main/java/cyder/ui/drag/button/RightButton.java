package main.java.cyder.ui.drag.button;

import com.google.common.base.Preconditions;
import main.java.cyder.ui.drag.DragLabelButtonSize;

import java.awt.*;

/**
 * An arrow button pointing right.
 */
public class RightButton extends CyderDragLabelButton {
    /**
     * The size this right button will be painted with.
     */
    private DragLabelButtonSize size;

    /**
     * Constructs a new right button.
     */
    public RightButton() {
        this(DEFAULT_SIZE);
    }

    /**
     * The text for the right button.
     */
    private static final String RIGHT = "Right";

    /**
     * Constructs a new right button.
     *
     * @param size the size of this right button
     */
    public RightButton(DragLabelButtonSize size) {
        this.size = Preconditions.checkNotNull(size);

        setToolTipText(RIGHT);

        setSize(size.getSize(), size.getSize());
        repaint();
    }

    /**
     * Returns the actual size of the painted right button after accounting for padding.
     *
     * @return the actual size of the painted right button after accounting for padding
     */
    private int getPaintLength() {
        Preconditions.checkNotNull(size);
        return size.getSize() - 2 * PAINT_PADDING;
    }

    /**
     * The padding between the edges of the painted right button.
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
    public void paintDragLabelButton(Graphics g) {
        Preconditions.checkNotNull(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(PAINT_PADDING, PAINT_PADDING);

        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(getPaintColor());

        for (int i = 0 ; i < getPaintLength() ; i++) {
            g2d.fillRect(i, i / 2, drawnRectangleLength, drawnRectangleLength);
            g2d.fillRect(i, getPaintLength() - i / 2, drawnRectangleLength, drawnRectangleLength);
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
        return RIGHT;
    }
}

