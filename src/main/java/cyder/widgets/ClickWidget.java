package main.java.cyder.widgets;

import main.java.cyder.annotations.CyderAuthor;
import main.java.cyder.annotations.ForReadability;
import main.java.cyder.annotations.Vanilla;
import main.java.cyder.annotations.Widget;
import main.java.cyder.constants.CyderColors;
import main.java.cyder.constants.CyderFonts;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.handlers.internal.ExceptionHandler;
import main.java.cyder.math.NumberUtil;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.ui.frame.CyderFrame;
import main.java.cyder.ui.frame.FrameType;
import main.java.cyder.utils.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A widget for extreme boredom.
 */
@Vanilla
@CyderAuthor
public final class ClickWidget {
    /**
     * Suppress default instantiation.
     */
    private ClickWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The frame title and label text.
     */
    private static final String CLICK_ME = "Click Me";

    /**
     * The frame width.
     */
    private static final int FRAME_WIDTH = 220;

    /**
     * The frame height.
     */
    private static final int FRAME_HEIGHT = 100;

    /**
     * The widget frame.
     */
    private static CyderFrame clickMeFrame;

    /**
     * The offset between the monitor bounds and the possible locations to place the frame at.
     */
    private static final int monitorMinOffset = 200;

    /**
     * The font for the click me label.
     */
    private static final Font clickMeLabelFont = new Font(CyderFonts.SEGOE_UI_BLACK, Font.BOLD, 22);

    @Widget(triggers = "click me", description = "A troll widget that pops open a new window every time it is clicked")
    public static void showGui() {
        try {
            UiUtil.closeIfOpen(clickMeFrame);

            clickMeFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT);
            clickMeFrame.setAutoFastClose(true);
            clickMeFrame.setTitle(CLICK_ME);
            clickMeFrame.setFrameType(FrameType.POPUP);
            clickMeFrame.setBackground(CyderColors.vanilla);

            JLabel clickMeLabel = new JLabel(CLICK_ME);
            clickMeLabel.setHorizontalAlignment(JLabel.CENTER);
            clickMeLabel.setVerticalAlignment(JLabel.CENTER);
            clickMeLabel.setForeground(CyderColors.navy);
            clickMeLabel.setFont(clickMeLabelFont);
            clickMeLabel.setBounds(30, 40, 150, 40);
            clickMeLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    changeFramePosition();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    clickMeLabel.setForeground(CyderColors.regularRed);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    clickMeLabel.setForeground(CyderColors.navy);
                }
            });

            clickMeFrame.getContentPane().add(clickMeLabel);

            clickMeFrame.finalizeAndShow();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * The logic for when the click me label is pressed.
     */
    @ForReadability
    private static void changeFramePosition() {
        Rectangle bounds = clickMeFrame.getMonitorBounds();
        int minX = (int) bounds.getMinX();
        int maxX = (int) (minX + bounds.getHeight());
        int minY = (int) bounds.getMinY();
        int maxY = (int) (minY + bounds.getHeight());

        int randomX = NumberUtil.randInt(minX + monitorMinOffset, maxX - monitorMinOffset);
        int randomY = NumberUtil.randInt(minY + monitorMinOffset, maxY - monitorMinOffset);

        clickMeFrame.setLocation(randomX, randomY);
    }
}
