package cyder.widgets;

import com.google.gson.Gson;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.genesis.CyderShare;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.utilities.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * A clock widget for displaying the current time in a fancy and minimalistic format.
 */
@SuppressWarnings({"FieldMayBeFinal", "UnnecessaryLocalVariable"}) /* use of arrays on purpose, readability */
public class ClockWidget {
    /**
     * Supress default constructor.
     */
    private ClockWidget() {
        throw new IllegalStateException(CyderStrings.attemptedInstantiation);
    }

    private static CyderFrame clockFrame;
    private static JLabel clockLabel;

    private static JLabel digitalTimeAndDateLabel;

    private static boolean showSecondHand = true;
    private static boolean paintHourLabels = true;

    private static Color clockColor = CyderColors.navy;

    private static boolean update;

    private static int[] currentSecond = {0};
    private static int[] currentMinute = {0};
    private static int[] currentHour = {0};

    private static String currentLocation = "Greenwich, London";
    private static int currentGMTOffset = 0;

    @Widget(triggers = "clock", description = "A clock widget capable of spawning mini widgets and changing the time zone")
    //it's ya boi, Greenwich
    public static void showGUI() {
        CyderThreadRunner.submit(() -> {
            

            if (clockFrame != null)
                clockFrame.dispose();

            clockColor = CyderColors.guiThemeColor;

            update = true;
            showSecondHand = true;
            paintHourLabels = true;

            currentLocation  = IPUtil.getIpdata().getCity() + "," + IPUtil.getIpdata().getRegion() + ","
                    + IPUtil.getIpdata().getCountry_name();
            currentGMTOffset = gmtBasedOffLocation();

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

            digitalTimeAndDateLabel = new CyderLabel(getWeatherTime(currentGMTOffset));
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

                    //hour hand is decreased by 30%
                    r = (int) (r * 0.70);

                    double x;
                    double y;

                    int drawToX;
                    int drawToY;

                    g.setColor(clockColor);
                    ((Graphics2D) g).setStroke(new BasicStroke(6,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                    theta = (currentHour[0] * 30.0) + 270.0;

                    if (theta > 360.0)
                        theta -= 360.0;

                    theta = theta * Math.PI / 180.0;

                    x = r * Math.cos(theta);
                    y = - r * Math.sin(theta);

                    drawToX = (int) Math.round(x);
                    drawToY = - (int) Math.round(y);

                    g.setColor(clockColor);
                    ((Graphics2D) g).setStroke(new BasicStroke(6,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

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
                    ((Graphics2D) g).setStroke(new BasicStroke(6,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

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
                        ((Graphics2D) g).setStroke(new BasicStroke(6,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                        //draw second hand
                        g.drawLine(centerX, centerY, centerX + drawToX,  centerY + drawToY);
                    }

                    //draw center dot
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(CyderColors.navy);
                    ((Graphics2D) g).setStroke(new BasicStroke(6,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
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

            CyderThreadRunner.submit(() -> {
                try {
                    while (update) {
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

                        digitalTimeAndDateLabel.setText(getWeatherTime(currentGMTOffset));
                        clockLabel.repaint();
                    }
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            },"Clock Widget Updater");

            CyderSwitch paintHourLabelsSwitch = new CyderSwitch(320, 50);
            paintHourLabelsSwitch.setOnText("Labels");
            paintHourLabelsSwitch.setOffText("No Labels");
            paintHourLabelsSwitch.setToolTipText("Paint Hours");
            paintHourLabelsSwitch.setBounds(60, 760, 320, 50);
            paintHourLabelsSwitch.setButtonPercent(50);
            paintHourLabelsSwitch.setState(CyderSwitch.State.ON);
            clockFrame.getContentPane().add(paintHourLabelsSwitch);

            paintHourLabelsSwitch.getSwitchButton().addActionListener(e -> paintHourLabels = !paintHourLabels);

            CyderSwitch showSecondHandSwitch = new CyderSwitch(320, 50);
            showSecondHandSwitch.setOnText("Seconds");
            showSecondHandSwitch.setOffText("No Seconds");
            showSecondHandSwitch.setToolTipText("Show Second hand");
            showSecondHandSwitch.setBounds(60 + 40 + 320, 760, 320, 50);
            showSecondHandSwitch.setButtonPercent(50);
            showSecondHandSwitch.setState(CyderSwitch.State.ON);
            clockFrame.getContentPane().add(showSecondHandSwitch);

            showSecondHandSwitch.getSwitchButton().addActionListener(e -> showSecondHand = !showSecondHand);

            CyderTextField hexField = new CyderTextField(6);
            hexField.setHorizontalAlignment(JTextField.CENTER);
            hexField.setText(ColorUtil.rgbToHexString(clockColor));
            hexField.setToolTipText("Clock color");
            hexField.setRegexMatcher("[abcdefABCDEF0-9]*");
            hexField.setBounds(240, 830, 140, 40);
            hexField.addActionListener(e -> {
                String text = hexField.getText().trim();

                try {
                    Color newColor = ColorUtil.hexToRgb(text);
                    clockColor = newColor;
                    hexField.setText(ColorUtil.rgbToHexString(clockColor));
                    clockLabel.repaint();
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
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
            hexLabel.setBounds(60, 830, StringUtil.getMinWidth("Clock Color Hex:",hexLabel.getFont()), 40);
            clockFrame.getContentPane().add(hexLabel);

            CyderTextField locationField = new CyderTextField(0);
            locationField.setHorizontalAlignment(JTextField.CENTER);
            locationField.setText(currentLocation);
            locationField.setCaretPosition(0);
            locationField.setToolTipText("Current Location");
            locationField.addActionListener(e -> {
                String possibleLocation = locationField.getText().trim();

                if (possibleLocation.length() > 0) {
                    try {
                        String key = UserUtil.getCyderUser().getWeatherkey();

                        if (key.trim().length() == 0) {
                            ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().inform("Sorry, " +
                                    "but the Weather Key has not been set or is invalid" +
                                    ", as a result, many features of Cyder will not work as intended. " +
                                    "Please see the fields panel of the user editor to learn how to acquire " +
                                    "a key and set it.","Weather Key Not Set");
                            return;
                        }

                        String OpenString = "https://api.openweathermap.org/data/2.5/weather?q=" +
                                possibleLocation + "&appid=" + key + "&units=imperial";

                        Gson gson = new Gson();
                        WeatherWidget.WeatherData wd;

                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(OpenString).openStream()))) {
                            wd = gson.fromJson(reader, WeatherWidget.WeatherData.class);
                            currentGMTOffset = Integer.parseInt(String.valueOf(wd.getTimezone())) / 3600;
                            currentLocation = possibleLocation;

                            currentHour[0] = getUnitForCurrentGMT("h");
                            currentMinute[0] = getUnitForCurrentGMT("m");
                            currentSecond[0] = getUnitForCurrentGMT("s");

                            String build = "[" + wd.getCoord().getLat() + "," + wd.getCoord().getLon() + "]";

                            clockFrame.notify("Successfully updated location to " + wd.getName()
                                    + "<br/>GMT: " + currentGMTOffset + "<br/>" + build);
                        } catch (Exception exc) {
                            ExceptionHandler.silentHandle(exc);
                            clockFrame.notify("Failed to update loation");
                            locationField.setText(currentLocation);
                            locationField.setCaretPosition(0);
                        }
                    } catch (Exception ex) {
                        ExceptionHandler.silentHandle(ex);
                        clockFrame.notify("Failed to update loation");
                        locationField.setText(currentLocation);
                        locationField.setCaretPosition(0);
                    }
                }
            });
            locationField.setBounds(60 + 40 + 320, 830, 320, 50);
            clockFrame.getContentPane().add(locationField);

            clockFrame.setLocationRelativeTo(CyderShare.getDominantFrame());
            clockFrame.setVisible(true);
        }, "Clock Widget Initializer");
    }

    private static void spawnMiniClock() {
        final boolean[] updateMiniClock = {true};

        CyderFrame miniFrame = new CyderFrame(600,150) {
            @Override
            public void dispose() {
                updateMiniClock[0] = false;
                super.dispose();
            }
        };

        miniFrame.setTitle("Timezone: " + "(GMT" + currentGMTOffset + ")");
        miniFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

        JLabel currentTimeLabel = new JLabel(getWeatherTime(currentGMTOffset), SwingConstants.CENTER);
        currentTimeLabel.setForeground(CyderColors.navy);
        currentTimeLabel.setFont(CyderFonts.segoe20);
        currentTimeLabel.setBounds(0, 50, 600, 30);
        miniFrame.getContentPane().add(currentTimeLabel);

        if (currentLocation.trim().length() > 0) {
            String labelText = "(GMT" + currentGMTOffset + ")";

            if (currentLocation.trim().length() > 0) {
                labelText = StringUtil.formatCommas(currentLocation) + " " + ("(GMT" + currentGMTOffset + ")");
            }

            JLabel locationLabel = new JLabel(labelText, SwingConstants.CENTER);
            locationLabel.setForeground(CyderColors.navy);
            locationLabel.setFont(CyderFonts.segoe20);
            locationLabel.setBounds(0, 80, 600, 30);
            miniFrame.getContentPane().add(locationLabel);
        }

        CyderThreadRunner.submit(() -> {
            try {
                final int effectivelyFinal = currentGMTOffset;
                while (updateMiniClock[0]) {
                    Thread.sleep(500);
                    currentTimeLabel.setText(getWeatherTime(effectivelyFinal));
                }
            } catch (Exception e) {
                ExceptionHandler.silentHandle(e);
            }
        },"Mini Clock Updater [GMT" + currentGMTOffset + "]");

        miniFrame.setVisible(true);
        miniFrame.setLocationRelativeTo(clockFrame);
    }

    //gets the time with the format calculating in the current GMT offset
    private static String getWeatherTime(int gmtOffsetInHours) {
        Calendar cal = Calendar.getInstance();
        Date Time = cal.getTime();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm:ssaa EEEEEEEEEEEEE, MMMMMMMMMMMMMMMMMM dd, yyyy");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            cal.add(Calendar.HOUR, gmtOffsetInHours);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return dateFormatter.format(cal.getTime());
    }

    private static int getUnitForCurrentGMT(String unit) {
        Calendar cal = Calendar.getInstance();
        Date Time = cal.getTime();
        SimpleDateFormat dateFormatter = new SimpleDateFormat(unit);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            cal.add(Calendar.HOUR, currentGMTOffset);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Integer.parseInt(dateFormatter.format(cal.getTime()));
    }

    private static int gmtBasedOffLocation() {
        String key = UserUtil.getCyderUser().getWeatherkey();

        if (key.trim().length() == 0) {
            ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().inform("Sorry, " +
                    "but the Weather Key has not been set or is invalid" +
                    ", as a result, many features of Cyder will not work as intended. " +
                    "Please see the fields panel of the user editor to learn how to acquire a key" +
                    " and set it.","Weather Key Not Set");

            currentGMTOffset = 0;
            currentLocation = "Greenwich, London";
            return currentGMTOffset;
        }

        String OpenString = "https://api.openweathermap.org/data/2.5/weather?q=" +
                currentLocation + "&appid=" + key + "&units=imperial";

        Gson gson = new Gson();
        WeatherWidget.WeatherData wd;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(OpenString).openStream()))) {
            wd = gson.fromJson(reader, WeatherWidget.WeatherData.class);
            currentGMTOffset = Integer.parseInt(String.valueOf(wd.getTimezone())) / 3600;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            currentGMTOffset = 0;
            currentLocation = "Greenwich, London";
        }

        return currentGMTOffset;
    }
}
