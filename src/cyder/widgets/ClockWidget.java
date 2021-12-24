package cyder.widgets;

import cyder.annotations.Widget;
import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderStrings;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ErrorHandler;
import cyder.ui.*;
import cyder.utilities.ColorUtil;
import cyder.utilities.TimeUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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

    private static int[] currentSecond = {0};
    private static int[] currentMinute = {0};
    private static int[] currentHour = {0};

    private static String[] timezones = {"Eastern (GMT-5)","Central (GMT-6)","Mountain (GMT-7)","Pacific (GMT-8)"};
    private static int[] correspondingGMToffsets = {-5, -6, -7, -8};

    private static String currentLocation = "Greenwich, London";
    private static int currentGMTOffset = 0;

    private static CyderComboBox timezoneCombo;

    @Widget("Clock") //it's ya boi, Greenwich
    public static void showGUI() {
       if (clockFrame != null)
           clockFrame.dispose();

       update = true;
       showSecondHand = true;
       paintHourLabels = true;

       //todo init currentGMToffset and currentLocation

        clockFrame = new CyderFrame(800,900) {
            @Override
            public void dispose() {
                update = false;
                super.dispose();
            }
        };
        clockFrame.setTitle("Clock");

        //spawn mini mode for current timezone button
        JButton spawnMini = new JButton("Mini");
        spawnMini.setForeground(CyderColors.vanila);
        spawnMini.setFont(CyderFonts.defaultFontSmall);
        spawnMini.setToolTipText("Spawn a mini clock for the current location");
        spawnMini.addActionListener(e -> spawnMiniClock());
        spawnMini.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                spawnMini.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                spawnMini.setForeground(CyderColors.vanila);
            }
        });

        spawnMini.setContentAreaFilled(false);
        spawnMini.setBorderPainted(false);
        spawnMini.setFocusPainted(false);
        clockFrame.getTopDragLabel().addButton(spawnMini, 0);

        //todo change location label to different area, add area label below time label too
        // for this we'll need to store the city, state, and country

        //todo make multiple instances possible by removing static modifiers

        //todo update screen shots on README: pathfinder, console with mp3, analyze code output

        digitalTimeAndDateLabel = new CyderLabel(TimeUtil.weatherTime());
        digitalTimeAndDateLabel.setFont(CyderFonts.defaultFont);
        digitalTimeAndDateLabel.setBounds(10,60, 780, 40);
        clockFrame.getContentPane().add(digitalTimeAndDateLabel);

        clockLabel = new JLabel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                int labelLen = 640;
                int inset = 20;
                int boxLen = 20;

                //entonces our radius is as follows
                int r = (labelLen - inset * 2 - boxLen * 2) / 2;
                int originalR = r;

                //center point to draw our hands from
                int centerX = labelLen / 2;
                int centerY = centerX;

                //vars used in if
                int numPoints = 12;
                double theta = 0.0;
                double thetaInc = 360.0 / numPoints;
                if (paintHourLabels) {
                    //draw numbers in the boxes

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
                        int topLeftX = (int) (x - radius / 2 + centerX) + 10;
                        int topleftY = (int) (y - radius / 2 + centerY);

                        String minText = numerals[i];
                        FontMetrics fm = g.getFontMetrics();
                        g.setColor(clockColor);
                        g.setFont(CyderFonts.defaultFont);
                        g.drawString(minText, topLeftX - boxLen / 2, topleftY + boxLen / 2);

                        theta += thetaInc;
                    }
                } else {
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
                        int topLeftX = (int) (x - radius / 2 + centerX) + 5;
                        int topleftY = (int) (y - radius / 2 + centerY) - 10;

                        g.fillRect(topLeftX, topleftY, boxLen / 2, boxLen);

                        theta += thetaInc;
                    }
                }

                //current theta, and x,y pair to draw from the center to
                theta = (currentHour[0] / 12.0) * Math.PI * 2.0 + Math.PI * 1.5;

                //hour hand is decreased by 30%
                r = (int) (r * 0.70);

                double x = r * Math.cos(theta);
                double y = r * Math.sin(theta);

                int drawToX = (int) Math.round(x);
                int drawToY = - (int) Math.round(y);

                g.setColor(clockColor);
                ((Graphics2D) g).setStroke(new BasicStroke(6));

                theta = (currentHour[0] * 30.0) + 270.0;

                if (theta > 360.0)
                    theta -= 360.0;

                theta = theta * Math.PI / 180.0;

                x = r * Math.cos(theta);
                y = - r * Math.sin(theta);

                drawToX = (int) Math.round(x);
                drawToY = - (int) Math.round(y);

                g.setColor(clockColor);
                ((Graphics2D) g).setStroke(new BasicStroke(6));

                //draw hour hand
                g.drawLine(centerX, centerY, centerX + drawToX,  centerY + drawToY);

                //minute hand is 20% decrease
                r = (int) (originalR * 0.80);

                //current theta, and x,y pair to draw from the center to
                theta = (currentMinute[0] / 60.0) * Math.PI * 2.0 + Math.PI * 1.5;
                x = r * Math.cos(theta);
                y = - r * Math.sin(theta);

                drawToX = (int) Math.round(x);
                drawToY = - (int) Math.round(y);

                g.setColor(clockColor);
                ((Graphics2D) g).setStroke(new BasicStroke(6));

                //draw minute hand
                g.drawLine(centerX, centerY, centerX + drawToX,  centerY + drawToY);

                if (showSecondHand) {
                    //second hand is 85% of original r
                    r = (int) (originalR * 0.85);

                    //current theta, and x,y pair to draw from the center to
                    theta = (currentSecond[0] / 60.0) * Math.PI * 2.0 + Math.PI * 1.5;
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

        currentHour[0] = hour;
        currentMinute[0] = minute;
        currentSecond[0] = second;

        new Thread(() -> {
            try {
                for (;;) {
                    if (!update)
                        break;
                    Thread.sleep(1000);

                    //increment seconds
                    currentSecond[0] += 1;

                    if (currentSecond[0] == 60) {
                        currentSecond[0] -= 60;
                        currentMinute[0] += 1;

                        if (currentMinute[0] == 60) {
                            currentMinute[0] -= 60;
                            currentHour[0] += 1;

                            if (currentHour[0] == 60) {
                                currentHour[0] -= 60;
                            }
                        }
                    }

                    digitalTimeAndDateLabel.setText(TimeUtil.weatherTime());
                    clockLabel.repaint();
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"Clock Widget Updater").start();

        paintHourLabelsSwitch = new CyderSwitch(320,50);
        paintHourLabelsSwitch.setOnText("Labels");
        paintHourLabelsSwitch.setOffText("No Labels");
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
        hexField.setText(ColorUtil.rgbtohexString(clockColor));
        hexField.setToolTipText("Clock color");
        hexField.setRegexMatcher("[abcdefABCDEF0-9]*");
        hexField.setBounds(240, 830, 140, 40);
        hexField.addActionListener(e -> {
            String text = hexField.getText().trim();

            try {
                Color newColor = ColorUtil.hextorgbColor(text);
                clockColor = newColor;
                hexField.setText(ColorUtil.rgbtohexString(clockColor));
                clockLabel.repaint();
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        });
        clockFrame.getContentPane().add(hexField);

        CyderLabel hexLabel = new CyderLabel("Clock Color Hex:");
        hexLabel.setFont(CyderFonts.defaultFont);
        hexLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ColorConverterWidget.showGUI();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hexLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hexLabel.setForeground(CyderColors.navy);
            }
        });
        hexLabel.setBounds(60, 830, CyderFrame.getMinWidth("Clock Color Hex:",hexLabel.getFont()), 40);
        clockFrame.getContentPane().add(hexLabel);

        timezoneCombo = new CyderComboBox(320, 40, timezones);
        timezoneCombo.setLocation(60 + 40 + 320,830);
        timezoneCombo.getComboSwitchButton().addActionListener(e -> {
            //todo
            System.out.println("Update based on: " + timezones[timezoneCombo.getIndex()]);
        });
        clockFrame.getContentPane().add(timezoneCombo);

        clockFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
        clockFrame.setVisible(true);
    }

    private static void spawnMiniClock() {
        final boolean[] updateMiniClock = {true};

        CyderFrame miniFrame = new CyderFrame(500,150) {
            @Override
            public void dispose() {
                updateMiniClock[0] = false;
                super.dispose();
            }
        };
        miniFrame.setTitle("Time: " + (currentLocation.trim().length() == 0 ?
                ("(GMT" + currentGMTOffset + ")") : currentLocation + " " + ("(GMT" + currentGMTOffset + ")")));
        miniFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

        JLabel currentTimeLabel = new JLabel(getWeatherTime(currentGMTOffset * 3600 + ""), SwingConstants.CENTER);
        currentTimeLabel.setForeground(CyderColors.navy);
        currentTimeLabel.setFont(CyderFonts.weatherFontSmall);
        currentTimeLabel.setBounds(0, 50, 500, 30);
        miniFrame.getContentPane().add(currentTimeLabel);

        if (currentLocation.trim().length() > 0) {
            JLabel locationLabel = new JLabel((currentLocation.trim().length() == 0 ?
                    ("(GMT" + currentGMTOffset + ")") : currentLocation + " " + ("(GMT" + currentGMTOffset + ")")), SwingConstants.CENTER);
            locationLabel.setForeground(CyderColors.navy);
            locationLabel.setFont(CyderFonts.weatherFontSmall);
            locationLabel.setBounds(0, 80, 500, 30);
            miniFrame.getContentPane().add(locationLabel);
        }

        new Thread(() -> {
            try {
                for (;;) {
                    if (!updateMiniClock[0])
                        break;
                    Thread.sleep(500);
                    currentTimeLabel.setText(getWeatherTime(currentGMTOffset * 3600 + ""));
                }
            } catch (Exception e) {
                ErrorHandler.silentHandle(e);
            }
        },"Mini Clock Updater [" + currentGMTOffset + "]").start();

        miniFrame.setVisible(true);
        miniFrame.setLocationRelativeTo(clockFrame);
    }

    //gets the time with the format calculating in the current GMT offset
    private static String getWeatherTime(String gmtOffsetInSeconds) {
        Calendar cal = Calendar.getInstance();
        Date Time = cal.getTime();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm:ssaa EEEEEEEEEEEEE, MMMMMMMMMMMMMMMMMM dd, yyyy");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            int timeOffset = Integer.parseInt(gmtOffsetInSeconds) / 3600;
            cal.add(Calendar.HOUR, timeOffset);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            return dateFormatter.format(cal.getTime());
        }
    }
}
