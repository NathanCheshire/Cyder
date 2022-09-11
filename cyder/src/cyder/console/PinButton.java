package cyder.console;

import com.google.common.base.Preconditions;
import cyder.annotations.ForReadability;
import cyder.constants.CyderColors;
import cyder.ui.CyderDragLabel;
import cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A pin button for CyderFrames which also handles the special case for the
 * Console frame which does not have the pin to frame option.
 */
public class PinButton extends JLabel {
    /**
     * The default width/height given to this icon button.
     */
    public static final Size DEFAULT_SIZE = Size.SMALL;

    /**
     * The current state of this pin button.
     */
    private State currentState;

    /**
     * The frame this pin button will be placed on.
     */
    private final CyderFrame effectFrame;

    /**
     * The size this pin button will be painted with.
     */
    private final Size size;

    /**
     * Whether the mouse is currently inside of this component.
     */
    private final AtomicBoolean mouseIn = new AtomicBoolean();

    /**
     * Whether this pin button is focused.
     */
    private final AtomicBoolean focused = new AtomicBoolean();

    /**
     * Constructs a new pin button with a default state of {@link State#DEFAULT}.
     *
     * @param effectFrame the frame this pin button will be on
     */
    public PinButton(CyderFrame effectFrame) {
        this(effectFrame, State.DEFAULT);
    }

    /**
     * Constructs a new pin button with a state of {@link #DEFAULT_SIZE}.
     *
     * @param effectFrame  the frame this pin button will be on
     * @param initialState the starting state of this pin button
     */
    public PinButton(CyderFrame effectFrame, State initialState) {
        this(effectFrame, initialState, DEFAULT_SIZE);
    }

    /**
     * Constructs a new pin button.
     *
     * @param effectFrame  the frame this pin button will be on
     * @param initialState the initial state of the pin button
     * @param size         the size of the pin button
     */
    public PinButton(CyderFrame effectFrame, State initialState, Size size) {
        this.effectFrame = Preconditions.checkNotNull(effectFrame);
        this.currentState = Preconditions.checkNotNull(initialState);
        this.size = Preconditions.checkNotNull(size);

        addMouseListener(generateMouseAdapter(this));
        addFocusListener(generateFocusAdapter(this));

        refreshTooltip();
        setFocusable(true);
        setSize(DEFAULT_SIZE.getSize(), DEFAULT_SIZE.getSize());
        repaint();
    }

    /**
     * Generates the default mouse adapter for the provided icon button.
     * A click increments the state and hovers show a mouse over animation.
     *
     * @param iconButton the icon button for the mouse adapter to be added to
     * @return the mouse adapter
     */
    @ForReadability
    private static MouseAdapter generateMouseAdapter(PinButton iconButton) {
        Preconditions.checkNotNull(iconButton);

        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                iconButton.incrementState();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                iconButton.setMouseIn(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                iconButton.setMouseIn(false);
            }
        };
    }

    /**
     * Generates the default focus adapter for the provided icon button.
     * Focus/de-focus shows a focus animation.
     *
     * @param iconButton the icon button for the mouse adapter to be added to
     * @return the focus adapter
     */
    @ForReadability
    private static FocusAdapter generateFocusAdapter(PinButton iconButton) {
        Preconditions.checkNotNull(iconButton);

        return new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                iconButton.setFocused(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                iconButton.setFocused(false);
            }
        };
    }

    /**
     * Sets whether the mouse is inside of the bounds of this pin button.
     * Repaint is also invoked.
     *
     * @param mouseIn whether the mouse is inside of the bounds of this pin button
     */
    public void setMouseIn(boolean mouseIn) {
        this.mouseIn.set(mouseIn);
        repaint();
    }

    /**
     * Sets whether this pin button is focused.
     * Repaint is also invoked.
     *
     * @param focused whether this pin button is focused
     */
    public void setFocused(boolean focused) {
        this.focused.set(focused);
        repaint();
    }

    /**
     * Valid sizes for an icon button.
     */
    public enum Size {
        /**
         * The default size of small.
         */
        SMALL(22),

        /**
         * A slightly larger icon button size.
         */
        MEDIUM(26),

        /**
         * A larger icon button size to fill the entire height of a default {@link CyderDragLabel}.
         */
        LARGE(30),

        /**
         * The icon should be drawn to take up the most space it can of its parent.
         */
        FULL_DRAG_LABEL(Integer.MAX_VALUE);

        /**
         * The size of this icon button.
         */
        private final int size;

        Size(int size) {
            this.size = size;
        }

        /**
         * Returns the size this icon button should be drawn with.
         *
         * @return the size this icon button should be drawn with
         */
        public int getSize() {
            return size;
        }
    }

    /**
     * Refreshes the tooltip of this pin button based on the current state.
     */
    public void refreshTooltip() {
        setToolTipText(currentState.getTooltip());
    }

    /**
     * The exception message for an invalid icon button state.
     */
    private static final String ILLEGAL_STATE_FOR_REGULAR_FRAME = "Illegal state for regular frame";

    /**
     * Returns the next state for this pin button depending on whether
     * effect frame is equal to {@link Console#INSTANCE}'s CyderFrame.
     *
     * @return the next state for this pin button
     */
    private State getNextState() {
        if (Console.INSTANCE.getConsoleCyderFrame().equals(effectFrame)) {
            return currentState == State.DEFAULT ? State.CONSOLE_PINNED : State.DEFAULT;
        } else return switch (currentState) {
            case DEFAULT -> State.FRAME_PINNED;
            case FRAME_PINNED -> State.PINNED_TO_CONSOLE;
            case PINNED_TO_CONSOLE -> State.DEFAULT;
            case CONSOLE_PINNED -> throw new IllegalStateException(ILLEGAL_STATE_FOR_REGULAR_FRAME);
        };
    }

    /**
     * Returns the current state of this pin button.
     *
     * @return the current state of this pin button
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * Sets the state of this pin button to the next state.
     */
    public void incrementState() {
        currentState = getNextState();
        refreshTooltip();
        repaint();

        refreshStateBasedOnState();
    }

    /**
     * Sets the state of this pin button.
     *
     * @param newState the state of this pin button
     */
    public void setState(State newState) {
        currentState = Preconditions.checkNotNull(newState);
        refreshTooltip();
        repaint();

        refreshStateBasedOnState();
    }

    /**
     * Refreshes based on the currently set state.
     */
    private void refreshStateBasedOnState() {
        effectFrame.refreshAlwaysOnTop();
    }

    /**
     * The padding between the edges of the painted icon button.
     */
    private static final int PAINT_PADDING = 4;

    /**
     * Returns the actual size of the painted icon button after accounting for padding.
     *
     * @return the actual size of the painted icon button after accounting for padding
     */
    private int getPaintLength() {
        Preconditions.checkNotNull(size);
        return size.size - 2 * PAINT_PADDING;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(PAINT_PADDING, PAINT_PADDING);

        int[] pinButtonPolygonXPoints = new int[]{0, getPaintLength(), getPaintLength() / 2, 0};
        int[] pinButtonPolygonYPoints = new int[]{0, 0, getPaintLength(), 0};

        g2d.setColor(getPaintColor());
        g2d.fillPolygon(pinButtonPolygonXPoints, pinButtonPolygonYPoints, pinButtonPolygonYPoints.length);
        super.paint(g);
    }

    /**
     * Returns the color to paint for the icon button based on the current state.
     *
     * @return the color to paint for the icon button based on the current state
     */
    private Color getPaintColor() {
        if (focused.get()) {
            return getCurrentState().getNextColor();
        } else {
            if (mouseIn.get()) {
                return getCurrentState().getNextColor();
            } else {
                return getCurrentState().getCurrentColor();
            }
        }
    }

    /**
     * The possible states of pin buttons.
     */
    public enum State {
        /**
         * The default state of frames.
         */
        DEFAULT("Pin", CyderColors.vanilla, CyderColors.regularRed),

        /**
         * A regular frame is pinned on top.
         */
        FRAME_PINNED("Pin to console", CyderColors.regularRed, CyderColors.regularPink),

        /**
         * The console frame is pinned on top.
         */
        CONSOLE_PINNED("Unpin", CyderColors.regularRed, CyderColors.vanilla),

        /**
         * A regular frame is console pinned.
         */
        PINNED_TO_CONSOLE("Unpin", CyderColors.regularPink, CyderColors.vanilla);

        private final String tooltip;
        private final Color currentColor;
        private final Color nextColor;

        State(String tooltip, Color currentColor, Color nextColor) {
            this.tooltip = tooltip;
            this.currentColor = currentColor;
            this.nextColor = nextColor;
        }

        /**
         * Returns the tooltip text for this pin button.
         *
         * @return the tooltip text for this pin button
         */
        public String getTooltip() {
            return tooltip;
        }

        /**
         * Returns the color for this pin button.
         *
         * @return the color for this pin button
         */
        public Color getCurrentColor() {
            return currentColor;
        }

        /**
         * Returns the next color for this pin button.
         *
         * @return the next color for this pin button
         */
        public Color getNextColor() {
            return nextColor;
        }
    }
}
