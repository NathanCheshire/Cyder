package cyder.ui.drag.button;

import com.google.common.base.Preconditions;
import cyder.ui.drag.DragLabelButtonSize;

import java.awt.*;

/**
 * A menu button for a drag label.
 */
public class MenuButton extends CyderDragLabelButton {
    /**
     * The size this menu button will be painted with.
     */
    private DragLabelButtonSize size;

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

        setToolTipText(MENU);

        setSize(size.getSize(), size.getSize());
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
    public void paintDragLabelButton(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(PAINT_PADDING, PAINT_PADDING);

        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(getPaintColor());

        for (int i = 0 ; i <= getPaintLength() ; i++) {
            g2d.fillRect(i / 2, i, drawnRectangleLength, drawnRectangleLength);
            g2d.fillRect(getPaintLength() - i / 2, i, drawnRectangleLength, drawnRectangleLength);
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
        return MENU;
    }

}
