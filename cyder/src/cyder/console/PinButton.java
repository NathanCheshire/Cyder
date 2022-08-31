package cyder.console;

import com.google.common.base.Preconditions;
import cyder.annotations.ForReadability;
import cyder.constants.CyderColors;
import cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A pin button for CyderFrames which also handles the special case for the
 * Console frame which does not have the pin to frame option.
 */
public class PinButton extends JLabel {
    /**
     * The current state of this pin button.
     */
    private State currentState;

    /**
     * The frame this pin button will be placed on.
     */
    private final CyderFrame effectFrame;

    /**
     * Whether the mouse is currently inside of this component.
     */
    private final AtomicBoolean mouseIn = new AtomicBoolean();

    /**
     * Constructs a new pin button with a default state of {@link State#DEFAULT}.
     *
     * @param effectFrame constructs a new pin button
     */
    public PinButton(CyderFrame effectFrame) {
        this(effectFrame, State.DEFAULT);
    }

    /**
     * Constructs a new pin button.
     *
     * @param effectFrame  the frame this pin button will be on
     * @param initialState the initial state of the pin button
     */
    public PinButton(CyderFrame effectFrame, State initialState) {
        this.effectFrame = Preconditions.checkNotNull(effectFrame);
        this.currentState = Preconditions.checkNotNull(initialState);

        addMouseListener(mouseAdapter);

        refreshTooltip();
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        repaint();
    }

    @ForReadability
    @SuppressWarnings("FieldCanBeLocal")
    private final MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            currentState = getNextState();
            refreshTooltip();
            repaint();
            effectFrame.refreshAlwaysOnTop();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            mouseIn.set(true);
            repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            mouseIn.set(false);
            repaint();
        }
    };

    /**
     * The default width given to this component.
     */
    private static final int DEFAULT_WIDTH = 22;

    /**
     * The default height given to this component.
     */
    private static final int DEFAULT_HEIGHT = 22;

    /**
     * Refreshes the tooltip of this pin button based on the current state.
     */
    public void refreshTooltip() {
        setToolTipText(currentState.getTooltip());
    }

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
            case CONSOLE_PINNED -> throw new IllegalStateException("Illegal state for regular frame");
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
     * Sets the state of this pin button.
     *
     * @param newState the state of this pin button
     */
    public void setState(State newState) {
        this.currentState = Preconditions.checkNotNull(newState);
        refreshTooltip();
        effectFrame.refreshAlwaysOnTop();
        repaint();
    }

    private static final int paintWidth = 14;
    private static final int paintHeight = 14;

    private static final int translateX = 4;
    private static final int translateY = 4;

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g.translate(translateX, translateY);

        int[] xPoints = {0, paintWidth, paintWidth / 2, 0};
        int[] yPoints = {0, 0, paintHeight, 0};

        g2d.setColor(mouseIn.get() ? currentState.getNextColor() : currentState.getCurrentColor());
        g2d.fillPolygon(xPoints, yPoints, xPoints.length);
        super.paint(g);
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
