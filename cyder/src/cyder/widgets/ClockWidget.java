package cyder.widgets;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import cyder.annotations.CyderAuthor;
import cyder.annotations.ForReadability;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.parsers.remote.ip.IPData;
import cyder.parsers.remote.weather.WeatherData;
import cyder.props.PropLoader;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import cyder.ui.drag.button.DragLabelTextButton;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.label.CyderLabel;
import cyder.ui.selection.CyderSwitch;
import cyder.utils.*;

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
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A clock widget for displaying the current time in a fancy and minimalistic format.
 */
@Vanilla
@CyderAuthor
public final class ClockWidget {
    /**
     * Suppress default constructor.
     */
    private ClockWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The clock frame.
     */
    private static CyderFrame clockFrame;

    /**
     * The custom label to paint.
     */
    private static JLabel clockLabel;

    /**
     * The digital time label.
     */
    private static JLabel digitalTimeAndDateLabel;

    /**
     * Whether to show the seconds hand
     */
    private static boolean showSecondHand = true;

    /**
     * Whether to show the hour numerals.
     */
    private static boolean paintHourLabels = true;

    /**
     * The default clock color
     */
    private static Color clockColor = CyderColors.getGuiThemeColor();

    /**
     * Whether to update the clock.
     */
    private static boolean shouldUpdate;

    private static final int[] currentSecond = {0};
    private static final int[] currentMinute = {0};
    private static final int[] currentHour = {0};

    /**
     * The default location for the clock.
     */
    private static final String DEFAULT_LOCATION = "Greenwich, London";

    /**
     * The GMT location string.
     */
    private static String currentLocation = DEFAULT_LOCATION;

    /**
     * The GMT offset for the current timezone.
     */
    private static int currentGmtOffset;

    /**
     * The taskbar button text to spawn a mini clock frame.
     */
    private static final String MINI = "Mini";

    /**
     * The tooltip for the mini button.
     */
    private static final String TOOLTIP = "Spawn a mini clock for the current location";

    /**
     * The description of this widget.
     */
    private static final String widgetDescription = "A clock widget capable of spawning"
            + " mini widgets and changing the time zone";

    /**
     * A joiner for joining strings on commas.
     */
    private static final Joiner commaJoiner = Joiner.on(",");

    /**
     * The list of roman numerals, organized by the angle made between the positive x axis.
     */
    private static final ImmutableList<String> romanNumerals = ImmutableList.of(
            "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "I", "II"
    );

    /**
     * The clock foreground hex color field.
     */
    private static CyderTextField hexField;

    /**
     * The widget frame title.
     */
    private static final String CLOCK = "Clock";

    /**
     * The frame width of the widget.
     */
    private static final int FRAME_WIDTH = 500;

    /**
     * The frame height of the widget.
     */
    private static final int FRAME_HEIGHT = 500;

    @Widget(triggers = "clock", description = widgetDescription)
    public static void showGui() {
        CyderThreadRunner.submit(() -> {
            UiUtil.closeIfOpen(clockFrame);

            clockColor = CyderColors.getGuiThemeColor();

            shouldUpdate = true;
            showSecondHand = true;
            paintHourLabels = true;

            IPData ipData = IPUtil.getIpData();
            currentLocation = commaJoiner.join(ipData.getCity(), ipData.getRegion(), ipData.getCountry_name());
            currentGmtOffset = getGmtFromUserLocation();

            clockFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT) {
                @Override
                public void dispose() {
                    shouldUpdate = false;
                    super.dispose();
                }
            };
            clockFrame.setTitle(CLOCK);

            DragLabelTextButton miniClockButton = DragLabelTextButton.generateTextButton(
                    new DragLabelTextButton.Builder(MINI)
                            .setTooltip(TOOLTIP)
                            .setClickAction(ClockWidget::spawnMiniClock));
            clockFrame.getTopDragLabel().addRightButton(miniClockButton, 0);

            digitalTimeAndDateLabel = new CyderLabel(getCurrentTimeAccountingForOffset(currentGmtOffset));
            digitalTimeAndDateLabel.setFont(CyderFonts.DEFAULT_FONT);
            digitalTimeAndDateLabel.setBounds(10, 60, 780, 40);
            clockFrame.getContentPane().add(digitalTimeAndDateLabel);

            int labelLen = 640;
            float circleDegrees = 360.0f;
            float oneEightyDegrees = 180.0f;
            clockLabel = new JLabel() {
                @Override
                public void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;

                    int inset = 20;
                    int boxLen = 20;

                    int r = (labelLen - inset * 2 - boxLen * 2) / 2;
                    int originalR = r;

                    //center point to draw our hands from
                    int center = labelLen / 2;

                    //vars used in if
                    int numPoints = romanNumerals.size();
                    double theta = 0.0;
                    double thetaInc = circleDegrees / numPoints;
                    if (paintHourLabels) {
                        //drawing center points
                        for (int i = 0 ; i < numPoints ; i++) {
                            double rads = theta * Math.PI / oneEightyDegrees;

                            double x = r * Math.cos(rads);
                            double y = r * Math.sin(rads);

                            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2d.setColor(clockColor);
                            g2d.setStroke(new BasicStroke(6));

                            int radius = 20;
                            int topLeftX = (int) (x - radius / 2 + center) + 10;
                            int topleftY = (int) (y - radius / 2 + center);

                            String minText = romanNumerals.get(i);
                            g.setColor(clockColor);
                            g.setFont(CyderFonts.DEFAULT_FONT);
                            g.drawString(minText, topLeftX - boxLen / 2, topleftY + boxLen / 2);

                            theta += thetaInc;
                        }
                    } else {
                        //drawing center points
                        for (int i = 0 ; i < numPoints ; i++) {
                            double rads = theta * Math.PI / oneEightyDegrees;

                            double x = r * Math.cos(rads);
                            double y = r * Math.sin(rads);

                            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2d.setColor(clockColor);
                            ((Graphics2D) g).setStroke(new BasicStroke(6));
                            int radius = 20;
                            int topLeftX = (int) (x - radius / 2 + center) + 5;
                            int topleftY = (int) (y - radius / 2 + center) - 10;

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
                    g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                    theta = (currentHour[0] * 30.0) + 270.0;
                    theta = MathUtil.normalizeAngle360(theta);

                    theta = theta * Math.PI / oneEightyDegrees;

                    x = r * Math.cos(theta);
                    y = -r * Math.sin(theta);

                    drawToX = (int) Math.round(x);
                    drawToY = -(int) Math.round(y);

                    g.setColor(clockColor);
                    g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                    //draw hour hand
                    g.drawLine(center, center, center + drawToX, center + drawToY);

                    //minute hand is 20% decrease
                    r = (int) (originalR * 0.80);

                    //current theta, and x,y pair to draw from the center to
                    theta = (currentMinute[0] / 60.0) * Math.PI * 2.0 + Math.PI * 1.5;
                    x = r * Math.cos(theta);
                    y = -r * Math.sin(theta);

                    drawToX = (int) Math.round(x);
                    drawToY = -(int) Math.round(y);

                    g.setColor(clockColor);
                    ((Graphics2D) g).setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                    //draw minute hand
                    g.drawLine(center, center, center + drawToX, center + drawToY);

                    if (showSecondHand) {
                        //second hand is 85% of original r
                        r = (int) (originalR * 0.85);

                        //current theta, and x,y pair to draw from the center to
                        theta = (currentSecond[0] / 60.0) * Math.PI * 2.0 + Math.PI * 1.5;
                        x = r * Math.cos(theta);
                        y = -r * Math.sin(theta);

                        drawToX = (int) Math.round(x);
                        drawToY = -(int) Math.round(y);

                        g.setColor(clockColor);
                        ((Graphics2D) g).setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                        //draw second hand
                        g.drawLine(center, center, center + drawToX, center + drawToY);
                    }

                    //draw center dot
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(CyderColors.navy);
                    g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                    int radius = 20;

                    g.fillOval(center - radius / 2, center - radius / 2, radius, radius);
                }
            };
            clockLabel.setBounds(80, 100, labelLen, labelLen);
            clockLabel.setBorder(new LineBorder(CyderColors.navy, 5));
            clockFrame.getContentPane().add(clockLabel);

            //figure out starting theta for hour, minute, second
            int hour = Integer.parseInt(TimeUtil.getTime("HH"));
            if (hour >= 12) hour -= 12;
            int minute = Integer.parseInt(TimeUtil.getTime("mm"));
            int second = Integer.parseInt(TimeUtil.getTime("ss"));

            currentHour[0] = hour;
            currentMinute[0] = minute;
            currentSecond[0] = second;

            CyderThreadRunner.submit(() -> {
                while (shouldUpdate) {
                    ThreadUtil.sleep((long) TimeUtil.MILLISECONDS_IN_SECOND);

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

                    digitalTimeAndDateLabel.setText(getCurrentTimeAccountingForOffset(currentGmtOffset));
                    clockLabel.repaint();
                }
            }, "Clock Widget Updater");

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
            showSecondHandSwitch.getSwitchButton().addActionListener(e -> showSecondHand = !showSecondHand);
            clockFrame.getContentPane().add(showSecondHandSwitch);

            hexField = new CyderTextField(6);
            hexField.setHorizontalAlignment(JTextField.CENTER);
            hexField.setText(ColorUtil.rgbToHexString(clockColor));
            hexField.setToolTipText("Clock color");
            hexField.setHexColorRegexMatcher();
            hexField.setBounds(240, 830, 140, 40);
            hexField.addActionListener(e -> hexFieldFieldAction());
            clockFrame.getContentPane().add(hexField);

            CyderLabel hexLabel = new CyderLabel("Clock Color Hex:");
            hexLabel.setFont(CyderFonts.DEFAULT_FONT);
            hexLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    ColorConverterWidget.getInstance().innerShowGui();
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
            hexLabel.setBounds(60, 830, StringUtil.getMinWidth("Clock Color Hex:", hexLabel.getFont()), 40);
            clockFrame.getContentPane().add(hexLabel);

            CyderTextField locationField = new CyderTextField();
            locationField.setHorizontalAlignment(JTextField.CENTER);
            locationField.setText(currentLocation);
            locationField.setCaretPosition(0);
            locationField.setToolTipText("Current Location");
            locationField.addActionListener(e -> {
                String possibleLocation = locationField.getText().trim();

                if (!possibleLocation.isEmpty()) {
                    try {
                        String key = PropLoader.getString("weather_key");

                        if (key.trim().isEmpty()) {
                            Console.INSTANCE.getConsoleCyderFrame().inform("Sorry, " +
                                    "but the Weather Key has not been set or is invalid" +
                                    ", as a result, many features of Cyder will not work as intended. " +
                                    "Please see the fields panel of the user editor to learn how to acquire " +
                                    "a key and set it.", "Weather Key Not Set");
                            return;
                        }

                        String OpenString = CyderUrls.OPEN_WEATHER_BASE +
                                possibleLocation + "&appid=" + key + "&units=imperial";

                        WeatherData wd;

                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                                new URL(OpenString).openStream()))) {
                            wd = SerializationUtil.fromJson(reader, WeatherData.class);
                            currentGmtOffset = Integer.parseInt(String.valueOf(wd.getTimezone()))
                                    / TimeUtil.SECONDS_IN_HOUR;
                            currentLocation = possibleLocation;

                            currentHour[0] = getUnitForCurrentGmt("h");
                            currentMinute[0] = getUnitForCurrentGmt("m");
                            currentSecond[0] = getUnitForCurrentGmt("s");

                            String build = "[" + wd.getCoord().getLat() + "," + wd.getCoord().getLon() + "]";

                            clockFrame.notify("Successfully updated location to " + wd.getName()
                                    + "<br/>GMT: " + currentGmtOffset + "<br/>" + build);
                        } catch (Exception exc) {
                            ExceptionHandler.silentHandle(exc);
                            clockFrame.notify("Failed to update location");
                            locationField.setText(currentLocation);
                            locationField.setCaretPosition(0);
                        }
                    } catch (Exception ex) {
                        ExceptionHandler.silentHandle(ex);
                        clockFrame.notify("Failed to update location");
                        locationField.setText(currentLocation);
                        locationField.setCaretPosition(0);
                    }
                }
            });
            locationField.setBounds(60 + 40 + 320, 830, 320, 50);
            clockFrame.getContentPane().add(locationField);

            clockFrame.finalizeAndShow();
        }, "Clock Widget Initializer");
    }

    @ForReadability
    private static void hexFieldFieldAction() {
        String text = hexField.getTrimmedText();

        try {
            clockColor = ColorUtil.hexStringToColor(text);
            hexField.setText(ColorUtil.rgbToHexString(clockColor));
            clockLabel.repaint();
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Spawns a mini clock with its own timer based off of the current location.
     */
    private static void spawnMiniClock() {
        AtomicBoolean updateMiniClock = new AtomicBoolean(true);

        int miniFrameWidth = 600;
        int miniFrameHeight = 150;
        CyderFrame miniFrame = new CyderFrame(miniFrameWidth, miniFrameHeight) {
            @Override
            public void dispose() {
                updateMiniClock.set(false);
                super.dispose();
            }
        };

        miniFrame.setTitle("Timezone: " + "(" + GMT + currentGmtOffset + ")");
        miniFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

        JLabel currentTimeLabel =
                new JLabel(getCurrentTimeAccountingForOffset(currentGmtOffset), SwingConstants.CENTER);
        currentTimeLabel.setForeground(CyderColors.navy);
        currentTimeLabel.setFont(CyderFonts.SEGOE_20);
        currentTimeLabel.setBounds(0, 50, miniFrameWidth, 30);
        miniFrame.getContentPane().add(currentTimeLabel);

        if (!currentLocation.trim().isEmpty()) {
            String labelText = StringUtil.formatCommas(currentLocation) + " " + "(" + GMT + currentGmtOffset + ")";

            JLabel locationLabel = new JLabel(labelText, SwingConstants.CENTER);
            locationLabel.setForeground(CyderColors.navy);
            locationLabel.setFont(CyderFonts.SEGOE_20);
            locationLabel.setBounds(0, 80, miniFrameWidth, 30);
            miniFrame.getContentPane().add(locationLabel);
        }

        int miniClockUpdateTimeout = 500;
        CyderThreadRunner.submit(() -> {
            int effectivelyFinal = currentGmtOffset;
            while (updateMiniClock.get()) {
                ThreadUtil.sleep(miniClockUpdateTimeout);
                currentTimeLabel.setText(getCurrentTimeAccountingForOffset(effectivelyFinal));
            }
        }, "Mini Clock Updater [" + GMT + currentGmtOffset + "]");

        miniFrame.setVisible(true);
        miniFrame.setLocationRelativeTo(clockFrame);
    }

    /**
     * The time formatter for getting the current time accounting for the gmt offset.
     */
    private static final SimpleDateFormat timeFormatter = TimeUtil.weatherFormat;

    /**
     * Returns the current time accounting for the GMT offset by adding
     * the number of hours to the returned time.
     *
     * @param gmtOffsetInHours the GMT offset for the location
     * @return the current time accounting for the GMT offset
     */
    private static String getCurrentTimeAccountingForOffset(int gmtOffsetInHours) {
        Calendar calendar = Calendar.getInstance();

        timeFormatter.setTimeZone(TimeZone.getTimeZone(GMT));

        try {
            calendar.add(Calendar.HOUR, gmtOffsetInHours);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return timeFormatter.format(calendar.getTime());
    }

    /**
     * The GMT timezone string ID.
     */
    private static final String GMT = "GMT";

    /**
     * The GMT timezone object.
     */
    private static final TimeZone gmtTimezone = TimeZone.getTimeZone(GMT);

    // todo should be enum or something like actually passing minute or second

    /**
     * Returns the h/m/s provided accounting for the GMT offset
     *
     * @param unit the h, m, or s unit
     * @return the unit accounting for the GMT offset
     */
    private static int getUnitForCurrentGmt(String unit) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat(unit);
        dateFormatter.setTimeZone(gmtTimezone);

        try {
            calendar.add(Calendar.HOUR, currentGmtOffset);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Integer.parseInt(dateFormatter.format(calendar.getTime()));
    }

    /**
     * The key for obtaining the weather data key from the props.
     */
    private static final String WEATHER_KEY = "weather_key";

    /**
     * Returns the GMT based off of the current location.
     *
     * @return the GMT based off of the current location
     */
    private static int getGmtFromUserLocation() {
        String key = PropLoader.getString(WEATHER_KEY);

        if (key.isEmpty()) {
            Console.INSTANCE.getConsoleCyderFrame().inform("Sorry, "
                    + "but the Weather Key has not been set or is invalid"
                    + ", as a result, many features of Cyder will not work as intended. "
                    + "Please see the fields panel of the user editor to learn how to acquire a key"
                    + " and set it.", "Weather Key Not Set");

            return resetAndGetDefaultGmtOffset();
        }

        Optional<WeatherData> optionalWeatherData = getWeatherData(currentLocation);
        if (optionalWeatherData.isEmpty()) {
            return resetAndGetDefaultGmtOffset();
        }

        WeatherData weatherData = optionalWeatherData.get();
        currentGmtOffset = Integer.parseInt(String.valueOf(weatherData.getTimezone())) / TimeUtil.SECONDS_IN_HOUR;
        return currentGmtOffset;
    }

    /**
     * Returns the weather data object for the provided location string if available. Empty optional else.
     *
     * @param locationString the location string such as "Starkville,Ms,USA"
     * @return the weather data object for the provided location string if available. Empty optional else
     */
    private static Optional<WeatherData> getWeatherData(String locationString) {
        String key = PropLoader.getString(WEATHER_KEY);

        if (key.isEmpty()) {
            return Optional.empty();
        }

        String OpenString = CyderUrls.OPEN_WEATHER_BASE + locationString + "&appid=" + key + "&units=imperial";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(OpenString).openStream()))) {
            return Optional.of(SerializationUtil.fromJson(reader, WeatherData.class));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Optional.empty();
    }

    @ForReadability
    private static int resetAndGetDefaultGmtOffset() {
        currentGmtOffset = 0;
        currentLocation = DEFAULT_LOCATION;
        return currentGmtOffset;
    }
}
