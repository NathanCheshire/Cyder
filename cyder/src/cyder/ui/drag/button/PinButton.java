package cyder.ui.drag.button;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import cyder.annotations.ForReadability;
import cyder.console.Console;
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
 * A pin button for CyderFrame drag labels.
 */
public class PinButton extends JLabel {
    /**
     * The default width/height given to this pin button.
     */
    public static final DragLabelButtonSize DEFAULT_SIZE = DragLabelButtonSize.SMALL;

    /**
     * The current state of this pin button.
     */
    private PinState currentState;

    /**
     * The frame this pin button will be placed on.
     */
    private final CyderFrame effectFrame;

    /**
     * The size this pin button will be painted with.
     */
    private final DragLabelButtonSize size;

    /**
     * Whether the mouse is currently inside of this component.
     */
    private final AtomicBoolean mouseIn = new AtomicBoolean();

    /**
     * Whether this pin button is focused.
     */
    private final AtomicBoolean focused = new AtomicBoolean();

    /**
     * Constructs a new pin button with a default state of {@link PinState#DEFAULT}.
     *
     * @param effectFrame the frame this pin button will be on
     */
    public PinButton(CyderFrame effectFrame) {
        this(effectFrame, PinState.DEFAULT);
    }

    /**
     * Constructs a new pin button with a state of {@link #DEFAULT_SIZE}.
     *
     * @param effectFrame  the frame this pin button will be on
     * @param initialState the starting state of this pin button
     */
    public PinButton(CyderFrame effectFrame, PinState initialState) {
        this(effectFrame, initialState, DEFAULT_SIZE);
    }

    /**
     * Constructs a new pin button.
     *
     * @param effectFrame  the frame this pin button will be on
     * @param initialState the initial state of the pin button
     * @param size         the size of the pin button
     */
    public PinButton(CyderFrame effectFrame, PinState initialState, DragLabelButtonSize size) {
        this.effectFrame = Preconditions.checkNotNull(effectFrame);
        this.currentState = Preconditions.checkNotNull(initialState);
        this.size = Preconditions.checkNotNull(size);

        addMouseListener(generateMouseAdapter());
        addFocusListener(generateFocusAdapter());

        refreshTooltip();
        setFocusable(true);
        setSize(size.getSize(), size.getSize());
        repaint();
    }

    /**
     * Generates the default mouse adapter for this pin button
     *
     * @return the mouse adapter
     */
    @ForReadability
    private MouseAdapter generateMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                incrementState();
                Logger.log(Logger.Tag.UI_ACTION, this);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setMouseIn(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setMouseIn(false);
            }
        };
    }

    /**
     * Generates the default focus adapter for this pin button.
     *
     * @return the focus adapter
     */
    @ForReadability
    private FocusAdapter generateFocusAdapter() {
        return new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setFocused(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                setFocused(false);
            }
        };
    }

    /**
     * Sets whether the mouse is inside of the bounds of this pin button.
     * Repaint is also invoked.
     *
     * @param mouseIn whether the mouse is inside of the bounds of this pin button
     */
    private void setMouseIn(boolean mouseIn) {
        this.mouseIn.set(mouseIn);
        repaint();
    }

    /**
     * Sets whether this pin button is focused.
     * Repaint is also invoked.
     *
     * @param focused whether this pin button is focused
     */
    private void setFocused(boolean focused) {
        this.focused.set(focused);
        repaint();
    }

    /**
     * Refreshes the tooltip of this pin button based on the current state.
     */
    private void refreshTooltip() {
        setToolTipText(currentState.getTooltip());
    }

    /**
     * The immutable map of console pin button states to their successor states.
     */
    private static final ImmutableMap<PinState, PinState> consolePinButtonStates = ImmutableMap.of(
            PinState.DEFAULT, PinState.CONSOLE_PINNED,
            PinState.CONSOLE_PINNED, PinState.DEFAULT
    );

    /**
     * The immutable map of pin button states to their successor states.
     */
    private static final ImmutableMap<PinState, PinState> pinButtonStates = ImmutableMap.of(
            PinState.DEFAULT, PinState.FRAME_PINNED,
            PinState.FRAME_PINNED, PinState.PINNED_TO_CONSOLE,
            PinState.PINNED_TO_CONSOLE, PinState.DEFAULT
    );

    /**
     * Returns the next state for this pin button.
     *
     * @return the next state for this pin button
     */
    private PinState getNextState() {
        if (Console.INSTANCE.getConsoleCyderFrame().equals(effectFrame)) {
            return consolePinButtonStates.get(currentState);
        } else {
            return pinButtonStates.get(currentState);
        }
    }

    /**
     * Returns the current state of this pin button.
     *
     * @return the current state of this pin button
     */
    public PinState getCurrentState() {
        return currentState;
    }

    /**
     * Sets the state of this pin to the next state.
     */
    private void incrementState() {
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
    public void setState(PinState newState) {
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
     * The padding between the edges of the painted pin button.
     */
    private static final int PAINT_PADDING = 4;

    /**
     * Returns the actual size of the painted pin button after accounting for padding.
     *
     * @return the actual size of the painted pin button after accounting for padding
     */
    private int getPaintLength() {
        Preconditions.checkNotNull(size);
        return size.getSize() - 2 * PAINT_PADDING;
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
     * Returns the color to paint for the pin button based on the current state.
     *
     * @return the color to paint for the pin button based on the current state
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
    public enum PinState {
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

        PinState(String tooltip, Color currentColor, Color nextColor) {
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
