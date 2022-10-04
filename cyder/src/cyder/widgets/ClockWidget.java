package cyder.widgets;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
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
import cyder.user.UserUtil;
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
import java.util.concurrent.atomic.AtomicInteger;

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

    private static final AtomicInteger currentSecond = new AtomicInteger();
    private static final AtomicInteger currentMinute = new AtomicInteger();
    private static final AtomicInteger currentHour = new AtomicInteger();

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

    /**
     * The y padding for the digital time and date label.
     */
    private static final int digitalTimeAndDateLabelYPadding = 20;

    /**
     * The x padding for the digital time and date label.
     */
    private static final int digitalTimeAndDateLabelXPadding = 10;

    /**
     * The clock label padding between the label ends and the frame.
     */
    private static final int clockLabelPadding = 20;

    /**
     * The length of the clock label.
     */
    private static final int clockLabelLength = FRAME_WIDTH - 2 * clockLabelPadding;

    @Widget(triggers = "clock", description = widgetDescription)
    public static void showGui() {
        CyderThreadRunner.submit(() -> {
            UiUtil.closeIfOpen(clockFrame);

            clockColor = CyderColors.getGuiThemeColor();

            shouldUpdateWidget.set(true);
            setShowSecondHand(UserUtil.getCyderUser().getShowSecondHand().equals("1"));
            setPaintHourLabels(UserUtil.getCyderUser().getPaintClockLabels().equals("1"));

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
            digitalTimeAndDateLabel.setBounds(digitalTimeAndDateLabelXPadding,
                    CyderDragLabel.DEFAULT_HEIGHT + digitalTimeAndDateLabelYPadding,
                    FRAME_WIDTH - 2 * digitalTimeAndDateLabelXPadding, 40);
            clockFrame.getContentPane().add(digitalTimeAndDateLabel);

            clockLabel = new JLabel() {
                @Override
                public void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;

                    int innerLabelPadding = 20;
                    int hourLabelLen = 20;

                    int r = (clockLabelLength - innerLabelPadding * 2 - hourLabelLen * 2) / 2;
                    int originalR = r;

                    //center point to draw our hands from
                    int labelCenter = clockLabelLength / 2;

                    //vars used in if
                    int numPoints = romanNumerals.size();
                    double theta = 0.0;
                    double thetaInc = AngleUtil.DEGREES_IN_CIRCLE / numPoints;
                    if (paintHourLabels) {
                        g2d.setStroke(new BasicStroke(6));
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setColor(clockColor);

                        //drawing center points
                        for (int i = 0 ; i < numPoints ; i++) {
                            double radians = theta * Math.PI / AngleUtil.ONE_EIGHTY_DEGREES;
                            double x = r * Math.cos(radians);
                            double y = r * Math.sin(radians);

                            int boxLength = 20;
                            int topLeftX = (int) (x - boxLength / 2 + labelCenter) + 10;
                            int topleftY = (int) (y - boxLength / 2 + labelCenter);

                            String minText = romanNumerals.get(i);

                            g.setColor(clockColor);
                            g.setFont(CyderFonts.DEFAULT_FONT);
                            g.drawString(minText, topLeftX - hourLabelLen / 2, topleftY + hourLabelLen / 2);

                            theta += thetaInc;
                        }
                    } else {
                        g2d.setStroke(new BasicStroke(6));
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setColor(clockColor);

                        //drawing center points
                        for (int i = 0 ; i < numPoints ; i++) {
                            double currentRadians = theta * Math.PI / AngleUtil.ONE_EIGHTY_DEGREES;
                            double x = r * Math.cos(currentRadians);
                            double y = r * Math.sin(currentRadians);

                            int xAdditive = 0;
                            int yAdditive = -10;
                            int topLeftX = (int) (x - hourLabelLen / 2 + labelCenter) + xAdditive;
                            int topleftY = (int) (y - hourLabelLen / 2 + labelCenter) + yAdditive;

                            g.fillRect(topLeftX, topleftY, hourLabelLen, hourLabelLen);

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
                    theta = currentHour.get() * oneHourAngle + threeQuartersRatio;
                    theta = AngleUtil.normalizeAngle360(theta);

                    theta = theta * Math.PI / AngleUtil.ONE_EIGHTY_DEGREES;

                    x = r * Math.cos(theta);
                    y = -r * Math.sin(theta);

                    drawToX = (int) Math.round(x);
                    drawToY = -(int) Math.round(y);

                    g.setColor(clockColor);
                    g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                    //draw hour hand
                    g.drawLine(labelCenter, labelCenter, labelCenter + drawToX, labelCenter + drawToY);

                    //minute hand is 20% decrease
                    float minuteHandRatio = 0.80f;
                    r = (int) (originalR * minuteHandRatio);

                    //current theta, and x,y pair to draw from the center to
                    theta = (currentMinute.get() / TimeUtil.MINUTES_IN_HOUR) * Math.PI * 2.0 + Math.PI * 1.5;
                    x = r * Math.cos(theta);
                    y = -r * Math.sin(theta);

                    drawToX = (int) Math.round(x);
                    drawToY = -(int) Math.round(y);

                    g.setColor(clockColor);
                    g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                    //draw minute hand
                    g.drawLine(labelCenter, labelCenter, labelCenter + drawToX, labelCenter + drawToY);

                    if (showSecondHand) {
                        //second hand is 85% of original r
                        float secondHandRatio = 0.85f;
                        r = (int) (originalR * secondHandRatio);

                        //current theta, and x,y pair to draw from the center to
                        theta = (currentSecond.get() / TimeUtil.SECONDS_IN_MINUTE) * Math.PI * 2.0f + Math.PI * 1.5;
                        x = r * Math.cos(theta);
                        y = -r * Math.sin(theta);

                        drawToX = (int) Math.round(x);
                        drawToY = -(int) Math.round(y);

                        g.setColor(clockColor);
                        g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                        //draw second hand
                        g.drawLine(labelCenter, labelCenter, labelCenter + drawToX, labelCenter + drawToY);
                    }

                    //draw center dot
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(CyderColors.navy);
                    g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                    int radius = 20;

                    g.fillOval(labelCenter - radius / 2, labelCenter - radius / 2, radius, radius);
                }
            };
            clockLabel.setBounds(clockLabelPadding, 100, clockLabelLength, clockLabelLength);
            clockLabel.setBorder(new LineBorder(CyderColors.navy, 5));
            clockFrame.getContentPane().add(clockLabel);

            int hour = Integer.parseInt(TimeUtil.getTime(hourDaterPattern));
            if (hour >= TimeUtil.HOURS_IN_DAY / 2) hour -= (TimeUtil.HOURS_IN_DAY / 2);
            int minute = Integer.parseInt(TimeUtil.getTime(minuteDatePattern));
            int second = Integer.parseInt(TimeUtil.getTime(secondDatePattern));

            currentHour.set(hour);
            currentMinute.set(minute);
            currentSecond.set(second);

            startUpdating();

            installDragLabelButtons();

            clockFrame.finalizeAndShow();
        }, CLOCK_WIDGET_INITIALIZER_THREAD_NAME);
    }

    /**
     * The thread name for the clock time updater.
     */
    private static final String CLOCK_UPDATER_THREAD_NAME = "Clock Time Updater";

    @ForReadability
    private static void startUpdating() {
        CyderThreadRunner.submit(() -> {
            while (shouldUpdateWidget.get()) {
                ThreadUtil.sleep((long) TimeUtil.MILLISECONDS_IN_SECOND);
                currentSecond.getAndIncrement();

                if (currentSecond.get() == TimeUtil.SECONDS_IN_MINUTE) {
                    currentSecond.set((int) (currentSecond.get() - TimeUtil.SECONDS_IN_MINUTE));
                    currentMinute.getAndIncrement();

                    if (currentMinute.get() == TimeUtil.MINUTES_IN_HOUR) {
                        currentMinute.set((int) (currentMinute.get() - TimeUtil.MINUTES_IN_HOUR));
                        currentHour.getAndIncrement();

                        if (currentHour.get() == TimeUtil.HOURS_IN_DAY / 2) {
                            currentHour.set(0);
                        }
                    }
                }

                digitalTimeAndDateLabel.setText(getCurrentTimeAccountingForOffset(currentGmtOffset));
                clockLabel.repaint();
            }
        }, CLOCK_UPDATER_THREAD_NAME);
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

            currentHour.set(getUnitForCurrentGmt(GmtUnit.HOUR));
            currentMinute.set(getUnitForCurrentGmt(GmtUnit.MINUTE));
            currentSecond.set(getUnitForCurrentGmt(GmtUnit.SECOND));

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

        JLabel currentTimeLabel =
                new JLabel(getCurrentTimeAccountingForOffset(currentGmtOffset), SwingConstants.CENTER);
        currentTimeLabel.setForeground(CyderColors.navy);
        currentTimeLabel.setFont(CyderFonts.SEGOE_20);
        currentTimeLabel.setBounds(0, 50, miniFrameWidth, 30);
        miniFrame.getContentPane().add(currentTimeLabel);

        if (!currentLocation.isEmpty()) {
            String locationString = StringUtil.formatCommas(currentLocation);
            String labelText = locationString
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
            // Localize since the global can change
            int localGmtOffset = currentGmtOffset;
            while (updateMiniClock.get()) {
                ThreadUtil.sleep(miniClockUpdateTimeout);
                currentTimeLabel.setText(getCurrentTimeAccountingForOffset(localGmtOffset));
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
     * The app id argument.
     */
    private static final String APP_ID = "&appid=";

    /**
     * The units argument for the weather data.
     */
    private static final String UNITS_ARG = "&units=";

    /**
     * Possible measurement scales, that of imperial or metric.
     */
    private enum MeasurementScale {
        /**
         * The imperial measurement scale.
         */
        IMPERIAL("imperial"),

        /**
         * The metric measurement scale.
         */
        METRIC("metric");

        private final String weatherDataRepresentation;

        MeasurementScale(String weatherDataRepresentation) {
            this.weatherDataRepresentation = weatherDataRepresentation;
        }

        /**
         * Returns the weather data representation for this measurement scale.
         *
         * @return the weather data representation for this measurement scale
         */
        public String getWeatherDataRepresentation() {
            return weatherDataRepresentation;
        }
    }

    // todo weather util? weather package soon

    /**
     * Returns the weather data object for the provided location string if available. Empty optional else.
     *
     * @param locationString the location string such as "Starkville,Ms,USA"
     * @return the weather data object for the provided location string if available. Empty optional else
     */
    private static Optional<WeatherData> getWeatherData(String locationString) {
        Preconditions.checkNotNull(locationString);
        Preconditions.checkArgument(!locationString.isEmpty());

        String weatherKey = PropLoader.getString(WEATHER_KEY);

        if (weatherKey.isEmpty()) {
            return Optional.empty();
        }

        String OpenString = CyderUrls.OPEN_WEATHER_BASE + locationString + APP_ID
                + weatherKey + UNITS_ARG + MeasurementScale.IMPERIAL.getWeatherDataRepresentation();

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
