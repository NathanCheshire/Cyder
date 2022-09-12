package cyder.ui.drag;

import com.google.common.base.Preconditions;
import cyder.annotations.ForReadability;
import cyder.constants.CyderColors;
import cyder.handlers.internal.Logger;
import cyder.ui.frame.CyderFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A minimize button for CyderFrame drag labels.
 */
public class MinimizeButton extends JLabel {
    /**
     * The default size of the minimize button.
     */
    private static final DragLabelButtonSize DEFAULT_SIZE = DragLabelButtonSize.SMALL;

    /**
     * The frame this minimize button will be placed on.
     */
    private final CyderFrame effectFrame;

    /**
     * The size this minimize button will be painted with.
     */
    private final DragLabelButtonSize size;

    /**
     * Whether the mouse is currently inside of this component.
     */
    private final AtomicBoolean mouseIn = new AtomicBoolean();

    /**
     * Whether this minimize button is focused.
     */
    private final AtomicBoolean focused = new AtomicBoolean();

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
        this.effectFrame = Preconditions.checkNotNull(effectFrame);
        this.size = Preconditions.checkNotNull(size);

        addMouseListener(generateMouseAdapter(this));
        addFocusListener(generateFocusAdapter(this));

        setToolTipText(MINIMIZE);
        setFocusable(true);
        setSize(size.getSize(), size.getSize());
        repaint();
    }

    /**
     * Generates the default mouse adapter for this minimize button.
     *
     * @param minimizeButton the minimize button for the mouse adapter to be added to
     * @return the mouse adapter
     */
    @ForReadability
    private MouseAdapter generateMouseAdapter(MinimizeButton minimizeButton) {
        Preconditions.checkNotNull(minimizeButton);

        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(Logger.Tag.UI_ACTION, this);
                effectFrame.minimizeAndIconify();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                minimizeButton.setMouseIn(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                minimizeButton.setMouseIn(false);
            }
        };
    }

    /**
     * Generates the default focus adapter for the provided minimize button button.
     *
     * @param minimizeButton the minimize button for the mouse adapter to be added to
     * @return the focus adapter
     */
    @ForReadability
    private FocusAdapter generateFocusAdapter(MinimizeButton minimizeButton) {
        Preconditions.checkNotNull(minimizeButton);

        return new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                minimizeButton.setFocused(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                minimizeButton.setFocused(false);
            }
        };
    }

    /**
     * Sets whether the mouse is inside of the bounds of this minimize button.
     * Repaint is also invoked.
     *
     * @param mouseIn whether the mouse is inside of the bounds of this minimize button
     */
    private void setMouseIn(boolean mouseIn) {
        this.mouseIn.set(mouseIn);
        repaint();
    }

    /**
     * Sets whether this minimize button is focused.
     * Repaint is also invoked.
     *
     * @param focused whether this minimize button is focused
     */
    private void setFocused(boolean focused) {
        this.focused.set(focused);
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
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(PAINT_PADDING, PAINT_PADDING);

        g2d.setColor(getPaintColor());
        g2d.setStroke(minimizeStroke);
        int minimizeYStart = getPaintLength() - MINIMIZE_BOTTOM_OFFSET;

        g2d.drawLine(0, minimizeYStart, getPaintLength(), minimizeYStart);
        super.paint(g);
    }

    /**
     * The focus and hover color for this minimize button.
     */
    private final Color focusedAndHoverColor = CyderColors.regularRed;

    /**
     * The default color for this minimize button.
     */
    private final Color defaultColor = CyderColors.vanilla;

    /**
     * Returns the color to paint for the minimize button based on the current state.
     *
     * @return the color to paint for the minimize button based on the current state
     */
    private Color getPaintColor() {
        if (mouseIn.get() || focused.get()) {
            return focusedAndHoverColor;
        } else {
            return defaultColor;
        }
    }
}
