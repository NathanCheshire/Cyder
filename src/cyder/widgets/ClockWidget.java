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

    private static JLabel digitalTimeAndDateLabel;

    private static boolean showSecondHand = true;
    private static boolean paintHourLabels = true;

    private static CyderSwitch paintHourLabelsSwitch;
    private static CyderSwitch showSecondHandSwitch;

    private static Color clockColor = CyderColors.navy;

    private static boolean update;

    private static int[] secondTheta = {0};
    private static int[] minuteTheta = {0};
    private static int[] hourTheta = {0};

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
                    int numPoints = 12;
                    double theta = 0.0;
                    double thetaInc = 360.0 / numPoints;

                    String[] numerals = {"III","IV","V","VI","VII","VIII","IX","X","XI","XII","I","II"};

                    //drawing center points
                    for (int i = 0 ; i < numPoints ; i++) {
                        double rads = theta * Math.PI / 180.0;

                        double x = r * Math.cos(rads);
                        double y = r * Math.sin(rads);

                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setColor(clockColor);
                        ((Graphics2D) g).setStroke(new BasicStroke(6));
                        int radius = 20;
                        int topLeftX = (int) (x - radius / 2 + centerX);
                        int topleftY = (int) (y - radius / 2 + centerY);

                        String minText = numerals[i];
                        FontMetrics fm = g.getFontMetrics();
                        g.setColor(clockColor);
                        g.setFont(CyderFonts.defaultFont);
                        g.drawString(minText, topLeftX - boxLen / 2, topleftY + boxLen / 2);

                        theta += thetaInc;
                    }
                } else {
                    int numPoints = 12;
                    double theta = 0.0;
                    double thetaInc = 360.0 / numPoints;

                    //drawing center points
                    for (int i = 0 ; i < numPoints ; i++) {
                        double rads = theta * Math.PI / 180.0;

                        double x = r * Math.cos(rads);
                        double y = r * Math.sin(rads);

                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setColor(clockColor);
                        ((Graphics2D) g).setStroke(new BasicStroke(6));
                        int radius = 20;
                        int topLeftX = (int) (x - radius / 2 + centerX) - 5;
                        int topleftY = (int) (y - radius / 2 + centerY) - 10;

                        g.fillRect(topLeftX, topleftY, boxLen / 2, boxLen);

                        theta += thetaInc;
                    }
                }

                //current theta, and x,y pair to draw from the center to
                double theta = (hourTheta[0] / 12.0) * Math.PI * 2.0 + Math.PI * 1.5;

                double x = r * Math.cos(theta);
                double y = r * Math.sin(theta);

                int drawToX = (int) Math.round(x);
                int drawToY = - (int) Math.round(y);

                g.setColor(clockColor);
                ((Graphics2D) g).setStroke(new BasicStroke(6));

                //tood draw hour hand

                //current theta, and x,y pair to draw from the center to
                theta = (minuteTheta[0] / 60.0) * Math.PI * 2.0 + Math.PI * 1.5;
                x = r * Math.cos(theta);
                y = - r * Math.sin(theta);

                drawToX = (int) Math.round(x);
                drawToY = - (int) Math.round(y);

                g.setColor(clockColor);
                ((Graphics2D) g).setStroke(new BasicStroke(6));

                //draw minute hand
                g.drawLine(centerX, centerY, centerX + drawToX,  centerY + drawToY);

                if (showSecondHand) {
                    //current theta, and x,y pair to draw from the center to
                    theta = (secondTheta[0] / 60.0) * Math.PI * 2.0 + Math.PI * 1.5;
                    x = r * Math.cos(theta);
                    y = - r * Math.sin(theta);

                    drawToX = (int) Math.round(x);
                    drawToY = - (int) Math.round(y);

                    g.setColor(clockColor);
                    ((Graphics2D) g).setStroke(new BasicStroke(6));

                    //draw second hand
                    g.drawLine(centerX, centerY, centerX + drawToX,  centerY + drawToY);
                }

                //draw center dot
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CyderColors.navy);
                ((Graphics2D) g).setStroke(new BasicStroke(6));
                int radius = 20;
                g.fillOval(centerX - radius / 2, centerY - radius / 2, radius, radius);
            }
        };
        clockLabel.setBounds(80,100, 640, 640);
        clockLabel.setBorder(new LineBorder(CyderColors.navy, 5));
        clockFrame.getContentPane().add(clockLabel);

        //figure out starting theta for hour, minute, second
        int hour = Integer.parseInt(TimeUtil.getTime("HH"));

        if (hour >= 12)
            hour -= 12;

        int minute = Integer.parseInt(TimeUtil.getTime("mm"));
        int second = Integer.parseInt(TimeUtil.getTime("ss"));

        hourTheta[0] = hour;
        minuteTheta[0] = minute;
        secondTheta[0] = second;

        new Thread(() -> {
            try {
                for (;;) {
                    if (!update)
                        break;
                    Thread.sleep(1000);

                    //increment seconds
                    secondTheta[0] += 1;

                    if (secondTheta[0] > 60) {
                        secondTheta[0] = 0;
                        minuteTheta[0] += 1;

                        if (minuteTheta[0] > 60) {
                            minuteTheta[0] = 0;
                            hourTheta[0] += 1;

                            if (hourTheta[0] > 60) {
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
