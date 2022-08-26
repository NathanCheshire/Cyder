package cyder.console;

import com.google.common.base.Preconditions;
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
    private State currentState;
    private final CyderFrame effectFrame;
    private final AtomicBoolean mouseIn = new AtomicBoolean();

    public PinButton(CyderFrame effectFrame) {
        this(effectFrame, State.DEFAULT);
    }

    public PinButton(CyderFrame effectFrame, State initialState) {
        this.effectFrame = Preconditions.checkNotNull(effectFrame);
        this.currentState = Preconditions.checkNotNull(initialState);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                currentState = getNextState();
                refreshTooltip();
                repaint();
                refreshPinnedState();
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
        });

        refreshTooltip();
        setSize(22, 22);
        repaint();
    }

    public void refreshTooltip() {
        setToolTipText(currentState.getTooltip());
    }

    public void refreshPinnedState() {
        if (isForConsole()) {
            effectFrame.setAlwaysOnTop(currentState == State.CONSOLE_PINNED);
        } else {
            effectFrame.setPinned(currentState != State.DEFAULT);
            effectFrame.setConsolePinned(currentState == State.PINNED_TO_CONSOLE);
        }
    }

    private boolean isForConsole() {
        return Console.INSTANCE.getConsoleCyderFrame() == effectFrame;
    }

    /**
     * Returns the next state for this pin button depending on whether
     * effect frame is equal to {@link Console#INSTANCE}'s CyderFrame.
     *
     * @return the next state for this pin button
     */
    private State getNextState() {
        if (isForConsole()) {
            return currentState == State.DEFAULT
                    ? State.CONSOLE_PINNED : State.DEFAULT;
        } else return switch (currentState) {
            case DEFAULT -> State.FRAME_PINNED;
            case FRAME_PINNED -> State.PINNED_TO_CONSOLE;
            case PINNED_TO_CONSOLE -> State.DEFAULT;
            case CONSOLE_PINNED -> throw new IllegalStateException("Illegal state for regular frame");
        };
    }

    public void setState(State newState) {
        this.currentState = Preconditions.checkNotNull(newState);
        refreshTooltip();
        refreshPinnedState();
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g.translate(4, 4);

        int w = 14;
        int h = 14;
        int[] xPoints = {0, w, w / 2, 0};
        int[] yPoints = {0, 0, h, 0};

        g2d.setColor(mouseIn.get() ? currentState.getNextColor() : currentState.getCurrentColor());
        g2d.fillPolygon(xPoints, yPoints, xPoints.length);
        super.paint(g);
    }

    public enum State {
        DEFAULT("Pin", CyderColors.vanilla, CyderColors.regularRed),
        FRAME_PINNED("Pin to console", CyderColors.regularRed, CyderColors.regularPink),
        CONSOLE_PINNED("Unpin", CyderColors.regularRed, CyderColors.vanilla),
        PINNED_TO_CONSOLE("Unpin", CyderColors.regularPink, CyderColors.vanilla);

        private final String tooltip;
        private final Color currentColor;
        private final Color nextColor;

        State(String tooltip, Color currentColor, Color nextColor) {
            this.tooltip = tooltip;
            this.currentColor = currentColor;
            this.nextColor = nextColor;
        }

        public String getTooltip() {
            return tooltip;
        }

        public Color getCurrentColor() {
            return currentColor;
        }

        public Color getNextColor() {
            return nextColor;
        }
    }
}
