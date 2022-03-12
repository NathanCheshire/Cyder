package cyder.widgets;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.CyderFrame;
import cyder.utilities.NumberUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ClickWidget {
    /**
     * Restrict default instantiation.
     */
    private ClickWidget() {
        throw new IllegalStateException(CyderStrings.attemptedInstantiation);
    }

    @Widget(triggers = "clickme", description = "A troll widget that pops open a new window every time it is clicked")
    public static void showGUI() {
        try {
            CyderFrame clickMeFrame = new CyderFrame(220,100) {
                @Override
                public void dispose() {
                    dispose(true);
                }
            };
            clickMeFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
            clickMeFrame.setTitle("Click Me");
            clickMeFrame.setFrameType(CyderFrame.FrameType.POPUP);
            clickMeFrame.setBackground(CyderColors.vanila);

            JLabel dismiss = new JLabel("Click Me!");
            dismiss.setHorizontalAlignment(JLabel.CENTER);
            dismiss.setVerticalAlignment(JLabel.CENTER);
            dismiss.setForeground(CyderColors.navy);
            dismiss.setFont(CyderFonts.segoe30.deriveFont(22f));
            dismiss.setBounds(30, 40, 150, 40);
            dismiss.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    clickMeFrame.dispose(true);
                    showGUI();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    dismiss.setForeground(CyderColors.regularRed);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    dismiss.setForeground(CyderColors.navy);
                }
            });

            clickMeFrame.getContentPane().add(dismiss);
            clickMeFrame.setVisible(true);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
            Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
            clickMeFrame.setLocation(NumberUtil.randInt(0, (int) (rect.getMaxX() - 200)),NumberUtil.randInt(0,(int) rect.getMaxY() - 200));
            clickMeFrame.setAlwaysOnTop(true);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}
