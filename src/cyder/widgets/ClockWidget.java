package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderStrings;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ErrorHandler;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.utilities.TimeUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ClockWidget {
    private ClockWidget() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    private static CyderFrame clockFrame;
    private static JLabel clockLabel;
    private static boolean showSecondHand;
    private static JLabel digitalTimeAndDateLabel;
    private static boolean paintHourLabels;

    private static Color clockColor = CyderColors.intellijPink;

    private static boolean update;

    public static void showGUI() {
       if (clockFrame != null)
           clockFrame.dispose();

       update = true;

        clockFrame = new CyderFrame(800,850) {
            @Override
            public void dispose() {
                update = false;
                super.dispose();
            }
        };
        clockFrame.setTitle("Clock");

        digitalTimeAndDateLabel = new CyderLabel(TimeUtil.weatherTime());
        digitalTimeAndDateLabel.setFont(CyderFonts.defaultFont);
        digitalTimeAndDateLabel.setBounds(10,60, 780, 40);
        clockFrame.getContentPane().add(digitalTimeAndDateLabel);

        new Thread(() -> {
            try {
                for (;;) {
                    if (!update)
                        break;
                    Thread.sleep(1000);
                    digitalTimeAndDateLabel.setText(TimeUtil.weatherTime());
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"Clock Label Updater").start();

        clockLabel = new JLabel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        clockLabel.setBounds(80,100, 640, 640);
        clockLabel.setBorder(new LineBorder(CyderColors.navy, 5));
        clockFrame.getContentPane().add(clockLabel);

        //switchers for paint hour labels and seconds hand on right

        //hex color field on bottom left

        clockFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
        clockFrame.setVisible(true);
    }
}
