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
import cyder.math.AngleUtil;
import cyder.parsers.remote.ip.IPData;
import cyder.parsers.remote.weather.Coord;
import cyder.parsers.remote.weather.WeatherData;
import cyder.props.PropLoader;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.drag.button.DragLabelTextButton;
import cyder.ui.frame.CyderFrame;
import cyder.ui.label.CyderLabel;
import cyder.utils.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
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
    private static final AtomicBoolean shouldUpdateWidget = new AtomicBoolean(false);

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
    private static final int FRAME_HEIGHT = 600;

    /**
     * The hour date pattern.
     */
    private static final String hourDaterPattern = "HH";

    /**
     * The minute date pattern.
     */
    private static final String minuteDatePattern = "mm";

    /**
     * The second date pattern.
     */
    private static final String secondDatePattern = "ss";

    /**
     * An html break tag.
     */
    private static final String breakTag = "<br/>";

    /**
     * The font of the clock label.
     */
    private static final Font clockFont = new Font(CyderFonts.AGENCY_FB, Font.BOLD, 26);

    @Widget(triggers = "clock", description = widgetDescription)
    public static void showGui() {
        CyderThreadRunner.submit(() -> {
            UiUtil.closeIfOpen(clockFrame);

            clockColor = CyderColors.getGuiThemeColor();

            shouldUpdateWidget.set(true);

            IPData ipData = IPUtil.getIpData();
            currentLocation = commaJoiner.join(ipData.getCity(), ipData.getRegion(), ipData.getCountry_name());
            currentGmtOffset = getGmtFromUserLocation();

            clockFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT) {
                @Override
                public void dispose() {
                    shouldUpdateWidget.set(false);
                    super.dispose();
                }
            };
            clockFrame.setTitle(CLOCK);

            digitalTimeAndDateLabel = new CyderLabel(getCurrentTimeAccountingForOffset(currentGmtOffset));
            digitalTimeAndDateLabel.setFont(clockFont);
            int yPadding = 20;
            int xPadding = 10;
            digitalTimeAndDateLabel.setBounds(xPadding, CyderDragLabel.DEFAULT_HEIGHT + yPadding,
                    FRAME_WIDTH - 2 * xPadding, 40);
            clockFrame.getContentPane().add(digitalTimeAndDateLabel);

            int labelPadding = 20;
            int labelLen = FRAME_WIDTH - 2 * labelPadding;
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
                            g2d.setStroke(new BasicStroke(6));
                            int radius = 20;
                            int topLeftX = (int) (x - radius / 2 + center) + 5;
                            int topleftY = (int) (y - radius / 2 + center) - 10;

                            g.fillRect(topLeftX, topleftY, boxLen / 2, boxLen);

                            theta += thetaInc;
                        }
                    }

                    //current theta, and x,y pair to draw from the center to

                    //hour hand is decreased by 30%
                    float hourHandRatio = 0.70f;
                    r = (int) (r * hourHandRatio);

                    double x;
                    double y;

                    int drawToX;
                    int drawToY;

                    g.setColor(clockColor);
                    g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                    float threeQuartersRatio = 270.0f;
                    float oneHourAngle = 30.0f;
                    theta = (currentHour[0] * oneHourAngle) + threeQuartersRatio;
                    theta = AngleUtil.normalizeAngle360(theta);

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
                    float minuteHandRatio = 0.80f;
                    r = (int) (originalR * minuteHandRatio);

                    //current theta, and x,y pair to draw from the center to
                    theta = (currentMinute[0] / 60.0) * Math.PI * 2.0 + Math.PI * 1.5;
                    x = r * Math.cos(theta);
                    y = -r * Math.sin(theta);

                    drawToX = (int) Math.round(x);
                    drawToY = -(int) Math.round(y);

                    g.setColor(clockColor);
                    g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                    //draw minute hand
                    g.drawLine(center, center, center + drawToX, center + drawToY);

                    if (showSecondHand) {
                        //second hand is 85% of original r
                        float secondHandRatio = 0.85f;
                        r = (int) (originalR * secondHandRatio);

                        //current theta, and x,y pair to draw from the center to
                        theta = (currentSecond[0] / TimeUtil.SECONDS_IN_MINUTE) * Math.PI * 2.0f + Math.PI * 1.5;
                        x = r * Math.cos(theta);
                        y = -r * Math.sin(theta);

                        drawToX = (int) Math.round(x);
                        drawToY = -(int) Math.round(y);

                        g.setColor(clockColor);
                        g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

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
            clockLabel.setBounds(labelPadding, 100, labelLen, labelLen);
            clockLabel.setBorder(new LineBorder(CyderColors.navy, 5));
            clockFrame.getContentPane().add(clockLabel);

            int hour = Integer.parseInt(TimeUtil.getTime(hourDaterPattern));
            if (hour >= 12) hour -= 12;
            int minute = Integer.parseInt(TimeUtil.getTime(minuteDatePattern));
            int second = Integer.parseInt(TimeUtil.getTime(secondDatePattern));

            currentHour[0] = hour;
            currentMinute[0] = minute;
            currentSecond[0] = second;

            CyderThreadRunner.submit(() -> {
                while (shouldUpdateWidget.get()) {
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

            installDragLabelButtons();

            clockFrame.finalizeAndShow();
        }, CLOCK_WIDGET_INITIALIZER_THREAD_NAME);
    }

    @ForReadability
    private static void installDragLabelButtons() {
        DragLabelTextButton miniClockButton = DragLabelTextButton.generateTextButton(
                new DragLabelTextButton.Builder(MINI)
                        .setTooltip(TOOLTIP)
                        .setClickAction(ClockWidget::spawnMiniClock));
        clockFrame.getTopDragLabel().addRightButton(miniClockButton, 0);

        DragLabelTextButton colorButton =
                DragLabelTextButton.generateTextButton(new DragLabelTextButton.Builder(COLOR)
                        .setTooltip(THEME_COLOR)
                        .setClickAction(getColorButtonClickRunnable()));
        clockFrame.getTopDragLabel().addRightButton(colorButton, 0);

        DragLabelTextButton locationButton =
                DragLabelTextButton.generateTextButton(new DragLabelTextButton.Builder(LOCATION)
                        .setTooltip(CURRENT_LOCATION)
                        .setClickAction(getLocationButtonClickRunnable()));
        clockFrame.getTopDragLabel().addRightButton(locationButton, 0);
    }

    /**
     * The location string.
     */
    private static final String LOCATION = "Location";

    /**
     * The current location string.
     */
    private static final String CURRENT_LOCATION = "Current Location";

    /**
     * The set location string.
     */
    private static final String SET_LOCATION = "Set location";

    /**
     * The label text for the getter util for setting the current location.
     */
    private static final String locationLabelText = "Time Location<br/>Enter locations separated by commas";

    @ForReadability
    private static Runnable getLocationButtonClickRunnable() {
        return () -> CyderThreadRunner.submit(() -> {
            String possibleLocation = GetterUtil.getInstance().getString(new GetterUtil.Builder(LOCATION)
                    .setLabelText(locationLabelText)
                    .setInitialString(currentLocation)
                    .setSubmitButtonText(SET_LOCATION)
                    .setRelativeTo(clockFrame)
                    .setDisableRelativeTo(true));

            if (StringUtil.isNullOrEmpty(possibleLocation)) return;

            Optional<WeatherData> optionalWeatherData = getWeatherData(possibleLocation);

            if (optionalWeatherData.isEmpty()) {
                Console.INSTANCE.getConsoleCyderFrame().inform("Sorry, "
                        + "but the Weather Key has not been set or is invalid"
                        + ", as a result, many features of Cyder will not work as intended. "
                        + "Please see the fields panel of the user editor to learn how to acquire "
                        + "a key and set it.", "Weather Key Not Set");
                clockFrame.notify("Failed to update location");
                return;
            }

            WeatherData weatherData = optionalWeatherData.get();
            String timezoneString = String.valueOf(weatherData.getTimezone());

            int timezoneMinutes;
            try {
                timezoneMinutes = Integer.parseInt(timezoneString);
            } catch (Exception ignored) {
                clockFrame.notify("Failed to update location");
                return;
            }
            currentGmtOffset = timezoneMinutes / TimeUtil.SECONDS_IN_HOUR;
            currentLocation = StringUtil.capsFirstWords(possibleLocation);

            currentHour[0] = getUnitForCurrentGmt(GmtUnit.HOUR);
            currentMinute[0] = getUnitForCurrentGmt(GmtUnit.MINUTE);
            currentSecond[0] = getUnitForCurrentGmt(GmtUnit.SECOND);

            Coord coord = weatherData.getCoord();
            String build = openingBracket + coord.getLat() + "," + coord.getLon() + closingBracket;

            clockFrame.notify("Successfully updated location to " + weatherData.getName()
                    + breakTag + GMT + ":" + space + currentGmtOffset + breakTag + build);
        }, "tester");
    }

    /**
     * The color string.
     */
    private static final String COLOR = "Color";

    /**
     * The theme color string.
     */
    private static final String THEME_COLOR = "Theme color";

    /**
     * The regex for the get string color getter.
     */
    private static final String colorThemeFieldRegex = "[A-Fa-f0-9]{0,6}";

    /**
     * The name for the color theme color getter waiting thread.
     */
    private static final String CLOCK_COLOR_THEME_GETTER_WAITER = "Clock Color Theme Getter";

    /**
     * The builder for getting the theme color.
     */
    private static final GetterUtil.Builder themeColorBuilder = new GetterUtil.Builder(THEME_COLOR)
            .setRelativeTo(clockFrame)
            .setFieldRegex(colorThemeFieldRegex)
            .setInitialString(ColorUtil.rgbToHexString(clockColor));

    @ForReadability
    private static Runnable getColorButtonClickRunnable() {
        return () -> CyderThreadRunner.submit(() -> {
            String text = GetterUtil.getInstance().getString(themeColorBuilder).trim();
            if (StringUtil.isNullOrEmpty(text)) return;

            Color newColor;

            try {
                newColor = ColorUtil.hexStringToColor(text);
            } catch (Exception ignored) {
                clockFrame.notify("Could not parse input for hex color: " + text);
                return;
            }

            setClockColor(newColor);
        }, CLOCK_COLOR_THEME_GETTER_WAITER);
    }

    /**
     * The thread name for the clock widget initializer thread.
     */
    private static final String CLOCK_WIDGET_INITIALIZER_THREAD_NAME = "Clock Widget Initializer";

    /**
     * Sets the color of the clock to the provided color.
     *
     * @param clockColor the new clock color
     */
    @ForReadability
    private static void setClockColor(Color clockColor) {
        ClockWidget.clockColor = clockColor;
        clockLabel.repaint();
    }

    /**
     * The width of mini frames to spawn.
     */
    private static final int miniFrameWidth = 600;

    /**
     * The height of mini frames to spawn.
     */
    private static final int miniFrameHeight = 150;

    /**
     * An opening parenthesis.
     */
    private static final String openingParenthesis = "(";

    /**
     * A closing parenthesis.
     */
    private static final String closingParenthesis = ")";

    /**
     * A space.
     */
    private static final String space = " ";

    /**
     * The timezone colon text.
     */
    private static final String TIMEZONE = "Timezone:";

    /**
     * The timeout for mini clock updates.
     */
    private static final int miniClockUpdateTimeout = 500;

    /**
     * The mini clock thread prefix.
     */
    private static final String minClockUpdaterThreadNamePrefix = "Mini CLock Updater";

    /**
     * An opening bracket.
     */
    private static final String openingBracket = "[";

    /**
     * A closing bracket.
     */
    private static final String closingBracket = "]";

    /**
     * Spawns a mini clock with its own timer based off of the current location.
     */
    private static void spawnMiniClock() {
        AtomicBoolean updateMiniClock = new AtomicBoolean(true);

        CyderFrame miniFrame = new CyderFrame(miniFrameWidth, miniFrameHeight) {
            @Override
            public void dispose() {
                updateMiniClock.set(false);
                super.dispose();
            }
        };
        miniFrame.setTitle(TIMEZONE + space + openingParenthesis + GMT + currentGmtOffset + closingParenthesis);
        miniFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

        JLabel currentTimeLabel = new JLabel("", SwingConstants.CENTER);
        currentTimeLabel.setForeground(CyderColors.navy);
        currentTimeLabel.setFont(CyderFonts.SEGOE_20);
        currentTimeLabel.setBounds(0, 50, miniFrameWidth, 30);
        miniFrame.getContentPane().add(currentTimeLabel);

        if (!currentLocation.isEmpty()) {
            String labelText = StringUtil.formatCommas(currentLocation)
                    + space + openingParenthesis + GMT + currentGmtOffset + closingParenthesis;
            JLabel locationLabel = new JLabel(labelText, SwingConstants.CENTER);
            locationLabel.setForeground(CyderColors.navy);
            locationLabel.setFont(CyderFonts.SEGOE_20);
            locationLabel.setBounds(0, 80, miniFrameWidth, 30);
            miniFrame.getContentPane().add(locationLabel);
        }

        String threadName = minClockUpdaterThreadNamePrefix + space + openingBracket
                + GMT + currentGmtOffset + closingBracket;
        CyderThreadRunner.submit(() -> {
            int effectivelyFinal = currentGmtOffset;
            while (updateMiniClock.get()) {
                ThreadUtil.sleep(miniClockUpdateTimeout);
                currentTimeLabel.setText(getCurrentTimeAccountingForOffset(effectivelyFinal));
            }
        }, threadName);

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

    /**
     * Possible gmt units used for hands of the clock.
     */
    private enum GmtUnit {
        HOUR("h"),
        MINUTE("m"),
        SECOND("s");

        private final String unitString;
        private final SimpleDateFormat dateFormatter;

        GmtUnit(String unitString) {
            this.unitString = unitString;
            this.dateFormatter = new SimpleDateFormat(unitString);
        }

        /**
         * Returns the unit string for this gmt unit.
         *
         * @return the unit string for this gmt unit
         */
        public String getUnitString() {
            return unitString;
        }

        /**
         * Returns the date formatter for this gmt unit.
         *
         * @return the date formatter for this gmt unit
         */
        public SimpleDateFormat getDateFormatter() {
            return dateFormatter;
        }
    }

    /**
     * Returns the h/m/s provided accounting for the GMT offset
     *
     * @param unit the gmt unit, that of hour, minute, or second
     * @return the unit accounting for the GMT offset
     */
    private static int getUnitForCurrentGmt(GmtUnit unit) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = unit.getDateFormatter();
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

    /**
     * Sets whether to show the second hand.
     *
     * @param showSecondHand whether to show the second hand
     */
    @ForReadability
    public static void setShowSecondHand(boolean showSecondHand) {
        ClockWidget.showSecondHand = showSecondHand;

        if (clockLabel != null && shouldUpdateWidget.get()) {
            clockLabel.repaint();
        }
    }

    /**
     * Sets whether the hour labels should be painted.
     *
     * @param paintHourLabels whether the hour labels should be painted
     */
    @ForReadability
    public static void setPaintHourLabels(boolean paintHourLabels) {
        ClockWidget.paintHourLabels = paintHourLabels;

        if (clockLabel != null && shouldUpdateWidget.get()) {
            clockLabel.repaint();
        }
    }
}
