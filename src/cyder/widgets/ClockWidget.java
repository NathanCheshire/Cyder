package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderStrings;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ErrorHandler;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.ui.CyderSwitch;
import cyder.ui.CyderTextField;
import cyder.utilities.ColorUtil;
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
    private static boolean showSecondHand = true;
    private static JLabel digitalTimeAndDateLabel;
    private static boolean paintHourLabels = true;
    private static CyderSwitch paintHourLabelsSwitch;
    private static CyderSwitch showSecondHandSwitch;

    private static Color clockColor = CyderColors.intellijPink;

    private static boolean update;

    public static void showGUI() {
       if (clockFrame != null)
           clockFrame.dispose();

       update = true;
       showSecondHand = true;
       paintHourLabels = true;

        clockFrame = new CyderFrame(800,900) {
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

        final int[] secondTheta = {0};
        final int[] minuteTheta = {0};
        final int[] hourTheta = {0};

        clockLabel = new JLabel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                int labelLen = 640;
                int inset = 20;
                int boxLen = 20;

                //entonces our radius is as follows
                int r = (labelLen - inset * 2 - boxLen * 2) / 2;

                //center point to draw our hands from
                int centerX = labelLen / 2;
                int centerY = centerX;

                if (paintHourLabels) {
                    //draw numbers in the boxes
                } else {
                    //draw filled boxes
                }

                //draw hour hand

                //draw minute hand

                if (showSecondHand) {
                    //draw second hand
                }
            }
        };
        clockLabel.setBounds(80,100, 640, 640);
        clockLabel.setBorder(new LineBorder(CyderColors.navy, 5));
        clockFrame.getContentPane().add(clockLabel);

        new Thread(() -> {
            try {
                for (;;) {
                    if (!update)
                        break;
                    Thread.sleep(1000);

                    secondTheta[0] += 1;

                    if (secondTheta[0] > 0) {
                        secondTheta[0] = 0;
                        minuteTheta[0] += 1;

                        if (minuteTheta[0] > 0) {
                            minuteTheta[0] = 0;
                            hourTheta[0] += 1;

                            if (hourTheta[0] > 0) {
                                hourTheta[0] = 0;
                            }
                        }
                    }

                    clockLabel.repaint();
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"Clock Updater").start();

        paintHourLabelsSwitch = new CyderSwitch(320,50);
        paintHourLabelsSwitch.setOnText("Paint");
        paintHourLabelsSwitch.setOffText("Don't paint");
        paintHourLabelsSwitch.setToolTipText("Paint Hours");
        paintHourLabelsSwitch.setBounds(60, 760, 320, 50);
        paintHourLabelsSwitch.setButtonPercent(50);
        paintHourLabelsSwitch.setState(CyderSwitch.State.ON);
        clockFrame.getContentPane().add(paintHourLabelsSwitch);

        paintHourLabelsSwitch.getSwitchButton().addActionListener(e -> paintHourLabels = !paintHourLabels);

        showSecondHandSwitch = new CyderSwitch(320,50);
        showSecondHandSwitch.setOnText("Seconds");
        showSecondHandSwitch.setOffText("No Seconds");
        showSecondHandSwitch.setToolTipText("Show Second hand");
        showSecondHandSwitch.setBounds(60 + 40 + 320, 760, 320, 50);
        showSecondHandSwitch.setButtonPercent(50);
        showSecondHandSwitch.setState(CyderSwitch.State.ON);
        clockFrame.getContentPane().add(showSecondHandSwitch);

        showSecondHandSwitch.getSwitchButton().addActionListener(e -> showSecondHand = !showSecondHand);

        CyderTextField hexField = new CyderTextField(6);
        hexField.setRegexMatcher("[abcdefABCDEF0-9]*");
        hexField.setBounds(200, 830, 400, 40);
        hexField.addActionListener(e -> {
            String text = hexField.getText().trim();

            try {
                Color newColor = ColorUtil.hextorgbColor(text);
                clockColor = newColor;
                hexField.setText("");
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        });
        clockFrame.getContentPane().add(hexField);

        CyderLabel hexLabel = new CyderLabel("Hex:");
        hexLabel.setFont(CyderFonts.defaultFont);
        hexLabel.setBounds(145, 830, CyderFrame.getMinWidth("Hex:",hexLabel.getFont()), 40);
        clockFrame.getContentPane().add(hexLabel);

        clockFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
        clockFrame.setVisible(true);
    }
}
