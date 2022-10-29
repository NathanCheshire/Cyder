package cyder.ui.drag.button;

import com.google.common.base.Preconditions;
import cyder.ui.drag.DragLabelButtonSize;
import cyder.ui.frame.CyderFrame;

import java.awt.*;

/**
 * A minimize button for CyderFrame drag labels.
 */
public class MinimizeButton extends CyderDragLabelButton {
    /**
     * The size this minimize button will be painted with.
     */
    private DragLabelButtonSize size;

    /**
     * Constructs a new minimize button.
     *
     * @param effectFrame the frame this minimize button will be on
     */
    public MinimizeButton(CyderFrame effectFrame) {
        this(effectFrame, DEFAULT_SIZE);
    }

    /**
     * The text for the minimize button.
     */
    private static final String MINIMIZE = "Minimize";

    /**
     * Constructs a new minimize button.
     *
     * @param effectFrame the frame this minimize button will be on
     * @param size        the size of this minimize button
     */
    public MinimizeButton(CyderFrame effectFrame, DragLabelButtonSize size) {
        Preconditions.checkNotNull(effectFrame);
        this.size = Preconditions.checkNotNull(size);

        setClickAction(effectFrame::minimizeAndIconify);
        setToolTipText(MINIMIZE);

        setSize(size.getSize(), size.getSize());
        repaint();
    }

    /**
     * Returns the actual size of the painted minimize button after accounting for padding.
     *
     * @return the actual size of the painted minimize button after accounting for padding
     */
    private int getPaintLength() {
        Preconditions.checkNotNull(size);
        return size.getSize() - 2 * PAINT_PADDING;
    }

    /**
     * The padding between the edges of the painted minimize button.
     */
    private static final int PAINT_PADDING = 4;

    /**
     * The stroke to paint with for this minimize button.
     */
    private static final BasicStroke minimizeStroke = new BasicStroke(2);

    /**
     * The offset from the bottom for drawing this minimize button.
     */
    private static final int MINIMIZE_BOTTOM_OFFSET = 4;

    /**
     * {@inheritDoc}
     */
    @Override
    public void paintDragLabelButton(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(PAINT_PADDING, PAINT_PADDING);

        g2d.setColor(getPaintColor());
        g2d.setStroke(minimizeStroke);
        int minimizeYStart = getPaintLength() - MINIMIZE_BOTTOM_OFFSET;

        g2d.drawLine(0, minimizeYStart, getPaintLength(), minimizeYStart);
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
        return MINIMIZE;
    }
}
