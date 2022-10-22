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
import cyder.weather.WeatherUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
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

    /**
     * The hour hand ratio for the radius.
     */
    private static final float hourHandRatio = 0.65f;

    /**
     * The minute hand ratio for the radius.
     */
    private static final float minuteHandRatio = 0.75f;

    /**
     * The second hand ratio for the radius.
     */
    private static final float secondHandRatio = 0.80f;

    /**
     * The radius of the center dot.
     */
    private static final int centerDotRadius = 20;

    /**
     * The additive for the starting y values of numeral labels.
     */
    private static final int numeralLabelTopLeftYAdditive = -10;

    /**
     * The length of hour box labels.
     */
    private static final int hourBoxLabelLength = 20;

    /**
     * The padding between the edges of the clock label and painted attributes.
     */
    private static final int innerLabelPadding = 20;

    /**
     * The maximum radius for clock hands.
     */
    private static final int maxHandRadius = (clockLabelLength - innerLabelPadding * 2 - hourBoxLabelLength * 2) / 2;

    /**
     * The center of the clock label.
     */
    private static final int clockLabelCenter = clockLabelLength / 2;

    /**
     * The increment for painting hours.
     */
    private static final double hourThetaInc = AngleUtil.THREE_SIXTY_DEGREES / romanNumerals.size();

    /**
     * The radius for the hour hand.
     */
    private static final int hourHandRadius = (int) (maxHandRadius * hourHandRatio);

    /**
     * The radius for the minute hand.
     */
    private static final int minuteHandRadius = (int) (maxHandRadius * minuteHandRatio);

    /**
     * The radius for the second hand.
     */
    private static final int secondHandRadius = (int) (maxHandRadius * secondHandRatio);

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

                    g2d.setStroke(new BasicStroke(6));
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(clockColor);
                    g2d.setFont(CyderFonts.DEFAULT_FONT);

                    if (paintHourLabels) {
                        paintHourLabels(g2d);
                    } else {
                        paintHourBoxes(g2d);
                    }

                    g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                    drawHourHand(g2d);
                    drawMinuteHand(g2d);

                    if (showSecondHand) drawSecondHand(g2d);

                    drawCenterDot(g2d);
                }
            };
            clockLabel.setBounds(clockLabelPadding, 100, clockLabelLength, clockLabelLength);
            clockLabel.setBorder(new LineBorder(CyderColors.navy, 5));
            clockFrame.getContentPane().add(clockLabel);

            startUpdating();
            installDragLabelButtons();

            clockFrame.finalizeAndShow();
        }, CLOCK_WIDGET_INITIALIZER_THREAD_NAME);
    }

    /**
     * Draws the hour hand on the clock.
     *
     * @param g2d the 2D graphics object
     */
    @ForReadability
    private static void drawHourHand(Graphics2D g2d) {
        int hour = Integer.parseInt(TimeUtil.getTime(hourDaterPattern)) % ((int) TimeUtil.HOURS_IN_DAY / 2);

        float oneHourAngle = (float) (AngleUtil.THREE_SIXTY_DEGREES / romanNumerals.size());
        double hourTheta = hour * oneHourAngle + AngleUtil.TWO_SEVENTY_DEGREES;
        hourTheta = AngleUtil.normalizeAngle360(hourTheta) * Math.PI / AngleUtil.ONE_EIGHTY_DEGREES;
        int hourHandDrawToX = (int) Math.round(hourHandRadius * Math.cos(hourTheta));
        int hourHandDrawToY = (int) Math.round(hourHandRadius * Math.sin(hourTheta));
        g2d.drawLine(clockLabelCenter, clockLabelCenter,
                clockLabelCenter + hourHandDrawToX,
                clockLabelCenter + hourHandDrawToY);
    }

    /**
     * Draws the minute hand on the clock.
     *
     * @param g2d the 2D graphics object
     */
    @ForReadability
    private static void drawMinuteHand(Graphics2D g2d) {
        int minute = Integer.parseInt(TimeUtil.getTime(minuteDatePattern));

        double minuteTheta = (minute / TimeUtil.MINUTES_IN_HOUR) * Math.PI * 2.0 + Math.PI * 1.5;
        int minuteHandDrawToX = (int) Math.round(minuteHandRadius * Math.cos(minuteTheta));
        int minuteHandDrawToY = (int) Math.round(minuteHandRadius * Math.sin(minuteTheta));
        g2d.drawLine(clockLabelCenter, clockLabelCenter,
                clockLabelCenter + minuteHandDrawToX,
                clockLabelCenter + minuteHandDrawToY);
    }

    /**
     * Draws the second hand on the clock.
     *
     * @param g2d the current 2D graphics object
     */
    @ForReadability
    private static void drawSecondHand(Graphics2D g2d) {
        int second = Integer.parseInt(TimeUtil.getTime(secondDatePattern));

        double secondTheta = (second / TimeUtil.SECONDS_IN_MINUTE)
                * Math.PI * 2.0f + Math.PI * 1.5;
        int secondHandDrawToX = (int) Math.round(secondHandRadius * Math.cos(secondTheta));
        int secondHandDrawToY = (int) Math.round(secondHandRadius * Math.sin(secondTheta));
        g2d.drawLine(clockLabelCenter, clockLabelCenter,
                clockLabelCenter + secondHandDrawToX,
                clockLabelCenter + secondHandDrawToY);
    }

    /**
     * Draws the center dot on the clock.
     *
     * @param g2d the current 2D graphics object
     */
    @ForReadability
    private static void drawCenterDot(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(CyderColors.navy);
        g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.fillOval(clockLabelCenter - centerDotRadius / 2, clockLabelCenter - centerDotRadius / 2,
                centerDotRadius, centerDotRadius);
    }

    /**
     * Draws the hour boxes on the clock.
     *
     * @param g2d the current 2D graphics object
     */
    @ForReadability
    private static void paintHourBoxes(Graphics2D g2d) {
        double hourTheta = 0.0;

        for (int i = 0 ; i < romanNumerals.size() ; i++) {
            double currentRadians = hourTheta * Math.PI / AngleUtil.ONE_EIGHTY_DEGREES;
            double x = maxHandRadius * Math.cos(currentRadians);
            double y = maxHandRadius * Math.sin(currentRadians);

            int topLeftX = (int) (x - hourBoxLabelLength / 2 + clockLabelCenter);
            int topleftY = (int) (y - hourBoxLabelLength / 2 + clockLabelCenter) + numeralLabelTopLeftYAdditive;

            g2d.fillRect(topLeftX, topleftY, hourBoxLabelLength, hourBoxLabelLength);

            hourTheta += hourThetaInc;
        }
    }

    /**
     * Draws the hour labels on the clock.
     *
     * @param g2d the current 2D graphics object
     */
    @ForReadability
    private static void paintHourLabels(Graphics2D g2d) {
        double hourTheta = 0.0;

        for (int i = 0 ; i < romanNumerals.size() ; i++) {
            double radians = hourTheta * Math.PI / AngleUtil.ONE_EIGHTY_DEGREES;
            double x = maxHandRadius * Math.cos(radians);
            double y = maxHandRadius * Math.sin(radians);

            int topLeftX = (int) (x - hourBoxLabelLength / 2 + clockLabelCenter) + 10;
            int topleftY = (int) (y - hourBoxLabelLength / 2 + clockLabelCenter);

            String minText = romanNumerals.get(i);
            g2d.drawString(minText, topLeftX - hourBoxLabelLength / 2,
                    topleftY + hourBoxLabelLength / 2);

            hourTheta += hourThetaInc;
        }
    }

    /**
     * The thread name for the clock time updater.
     */
    private static final String CLOCK_UPDATER_THREAD_NAME = "Clock Time Updater";

    /**
     * The delay between clock updates.
     */
    private static final int CLOCK_UPDATER_TIMEOUT = 250;

    @ForReadability
    private static void startUpdating() {
        CyderThreadRunner.submit(() -> {
            while (shouldUpdateWidget.get()) {
                ThreadUtil.sleep(CLOCK_UPDATER_TIMEOUT);
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

            Optional<WeatherData> optionalWeatherData = WeatherUtil.getWeatherData(possibleLocation);

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

            Coord coord = weatherData.getCoord();
            String build = openingBracket + coord.getLat() + "," + coord.getLon() + closingBracket;

            clockFrame.notify("Successfully updated location to " + weatherData.getName()
                    + breakTag + GMT + CyderStrings.colon + space + currentGmtOffset + breakTag + build);
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
     * Returns the GMT based off of the current location.
     *
     * @return the GMT based off of the current location
     */
    private static int getGmtFromUserLocation() {
        String key = PropLoader.getString(WeatherUtil.WEATHER_KEY);

        if (key.isEmpty()) {
            Console.INSTANCE.getConsoleCyderFrame().inform("Sorry, "
                    + "but the Weather Key has not been set or is invalid"
                    + ", as a result, many features of Cyder will not work as intended. "
                    + "Please see the fields panel of the user editor to learn how to acquire a key"
                    + " and set it.", "Weather Key Not Set");

            return resetAndGetDefaultGmtOffset();
        }

        Optional<WeatherData> optionalWeatherData = WeatherUtil.getWeatherData(currentLocation);
        if (optionalWeatherData.isEmpty()) {
            return resetAndGetDefaultGmtOffset();
        }

        WeatherData weatherData = optionalWeatherData.get();
        currentGmtOffset = Integer.parseInt(String.valueOf(weatherData.getTimezone())) / TimeUtil.SECONDS_IN_HOUR;
        return currentGmtOffset;
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
