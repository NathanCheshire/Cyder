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
import cyder.constants.HtmlTags;
import cyder.exceptions.IllegalMethodException;
import cyder.getter.GetInputBuilder;
import cyder.getter.GetterUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.math.AngleUtil;
import cyder.network.IpDataManager;
import cyder.parsers.ip.IpData;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import cyder.ui.UiUtil;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.drag.button.DragLabelTextButton;
import cyder.ui.frame.CyderFrame;
import cyder.ui.frame.enumerations.TitlePosition;
import cyder.ui.label.CyderLabel;
import cyder.user.UserDataManager;
import cyder.utils.ColorUtil;
import cyder.weather.WeatherUtil;
import cyder.weather.parsers.Coord;
import cyder.weather.parsers.WeatherData;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import static cyder.strings.CyderStrings.*;

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
    private static final Joiner commaJoiner = Joiner.on(CyderStrings.comma);

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
    private static final String locationLabelText = "Time Location "
            + HtmlTags.breakTag + "Enter locations separated by commas";

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
     * The thread name for the clock widget initializer thread.
     */
    private static final String CLOCK_WIDGET_INITIALIZER_THREAD_NAME = "Clock Widget Initializer";

    /**
     * The width of mini frames to spawn.
     */
    private static final int miniFrameWidth = 600;

    /**
     * The height of mini frames to spawn.
     */
    private static final int miniFrameHeight = 150;

    /**
     * The timezone colon text.
     */
    private static final String TIMEZONE = "Timezone:";

    /**
     * The timeout for mini clock updates.
     */
    private static final Duration miniClockUpdateTimeout = Duration.ofMillis(500);

    /**
     * The mini clock thread prefix.
     */
    private static final String minClockUpdaterThreadNamePrefix = "Mini CLock Updater";

    /**
     * The time formatter for getting the current time accounting for the gmt offset.
     */
    private static final SimpleDateFormat timeFormatter = TimeUtil.weatherFormat;

    /**
     * The GMT timezone string ID.
     */
    private static final String GMT = "GMT";

    /**
     * The thread name for the clock time updater.
     */
    private static final String CLOCK_UPDATER_THREAD_NAME = "Clock Time Updater";

    /**
     * The delay between clock updates.
     */
    private static final Duration CLOCK_UPDATER_TIMEOUT = Duration.ofMillis(250);

    /**
     * The builder for getting the theme color.
     */
    private static GetInputBuilder themeColorBuilder = null;

    /**
     * The GMT timezone object.
     */
    private static final TimeZone gmtTimezone = TimeZone.getTimeZone(GMT);

    @Widget(triggers = "clock", description = widgetDescription)
    public static void showGui() {
        CyderThreadRunner.submit(() -> {
            UiUtil.closeIfOpen(clockFrame);

            clockColor = CyderColors.getGuiThemeColor();

            shouldUpdateWidget.set(true);
            setShowSecondHand(UserDataManager.INSTANCE.shouldShowClockWidgetSecondHand());
            setPaintHourLabels(UserDataManager.INSTANCE.shouldPaintClockHourLabels());

            IpData data = IpDataManager.INSTANCE.getIpData();
            currentLocation = commaJoiner.join(data.getCity(), data.getRegion(), data.getCountry_name());
            currentGmtOffset = getGmtFromUserLocation();

            CyderFrame.Builder builder = new CyderFrame.Builder()
                    .setWidth(FRAME_WIDTH)
                    .setHeight(FRAME_HEIGHT)
                    .setTitle(CLOCK);
            clockFrame = new CyderFrame(builder) {
                @Override
                public void dispose() {
                    shouldUpdateWidget.set(false);
                    super.dispose();
                }
            };

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

            startClockUpdater();
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
        int hour = Integer.parseInt(TimeUtil.getTime(hourDaterPattern)) % ((int) TimeUtil.hoursInDay / 2);

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

        double minuteTheta = (minute / TimeUtil.minutesInHour) * Math.PI * 2.0 + Math.PI * 1.5;
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

        double secondTheta = (second / TimeUtil.secondsInMinute)
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
     * Starts the updating thread for the clock.
     */
    private static void startClockUpdater() {
        CyderThreadRunner.submit(() -> {
            while (shouldUpdateWidget.get()) {
                ThreadUtil.sleep(CLOCK_UPDATER_TIMEOUT.toMillis());
                digitalTimeAndDateLabel.setText(getCurrentTimeAccountingForOffset(currentGmtOffset));
                clockLabel.repaint();
            }
        }, CLOCK_UPDATER_THREAD_NAME);
    }

    /**
     * Installs the drag label buttons for this widget.
     */
    private static void installDragLabelButtons() {
        DragLabelTextButton miniClockButton = new DragLabelTextButton.Builder(MINI)
                .setTooltip(TOOLTIP)
                .setClickAction(ClockWidget::spawnMiniClock)
                .build();
        clockFrame.getTopDragLabel().addRightButton(miniClockButton, 0);

        DragLabelTextButton colorButton = new DragLabelTextButton.Builder(COLOR)
                .setTooltip(THEME_COLOR)
                .setClickAction(getColorButtonClickRunnable())
                .build();
        clockFrame.getTopDragLabel().addRightButton(colorButton, 0);

        DragLabelTextButton locationButton = new DragLabelTextButton.Builder(LOCATION)
                .setTooltip(CURRENT_LOCATION)
                .setClickAction(getLocationButtonClickRunnable())
                .build();
        clockFrame.getTopDragLabel().addRightButton(locationButton, 0);
    }

    /**
     * Returns the runnable for the location drag label button.
     *
     * @return the runnable for the location drag label button
     */
    private static Runnable getLocationButtonClickRunnable() {
        return () -> CyderThreadRunner.submit(() -> {
            Optional<String> optionalPossibleLocation = GetterUtil.getInstance().getInput(
                    new GetInputBuilder(LOCATION, locationLabelText)
                            .setInitialFieldText(currentLocation)
                            .setSubmitButtonText(SET_LOCATION)
                            .setRelativeTo(clockFrame)
                            .setDisableRelativeTo(true));
            if (optionalPossibleLocation.isEmpty()) return;
            String possibleLocation = optionalPossibleLocation.get();

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
            currentGmtOffset = timezoneMinutes / TimeUtil.secondsInHour;
            currentLocation = StringUtil.capsFirstWords(possibleLocation);

            Coord coord = weatherData.getCoord();
            String build = CyderStrings.openingBracket + coord.getLat() + CyderStrings.comma
                    + coord.getLon() + CyderStrings.closingBracket;

            clockFrame.notify("Successfully updated location to " + weatherData.getName()
                    + HtmlTags.breakTag + GMT + CyderStrings.colon + space
                    + currentGmtOffset + HtmlTags.breakTag + build);
        }, "tester");
    }

    /**
     * Returns the runnable for the drag label color button.
     *
     * @return the runnable for the drag label color button
     */
    private static Runnable getColorButtonClickRunnable() {
        if (themeColorBuilder == null) {
            themeColorBuilder = new GetInputBuilder(THEME_COLOR, "Theme color")
                    .setRelativeTo(clockFrame)
                    .setFieldRegex(colorThemeFieldRegex)
                    .setInitialFieldText(ColorUtil.toRgbHexString(clockColor));
        }

        return () -> CyderThreadRunner.submit(() -> {
            Optional<String> optionalColor = GetterUtil.getInstance().getInput(themeColorBuilder);
            if (optionalColor.isEmpty()) return;
            String colorText = optionalColor.get().trim();

            Color newColor;

            try {
                newColor = ColorUtil.hexStringToColor(colorText);
            } catch (Exception ignored) {
                clockFrame.notify("Could not parse input for hex color: " + colorText);
                return;
            }

            setClockColor(newColor);
        }, CLOCK_COLOR_THEME_GETTER_WAITER);
    }

    /**
     * Sets the color of the clock to the provided color.
     *
     * @param clockColor the new clock color
     */
    @ForReadability
    private static void setClockColor(Color clockColor) {
        Preconditions.checkNotNull(clockColor);

        ClockWidget.clockColor = clockColor;
        clockLabel.repaint();
    }

    /**
     * Spawns a mini clock with its own timer based off of the current location.
     */
    private static void spawnMiniClock() {
        AtomicBoolean updateMiniClock = new AtomicBoolean(true);

        CyderFrame.Builder builder = new CyderFrame.Builder()
                .setWidth(miniFrameWidth)
                .setHeight(miniFrameHeight)
                .setTitle(TIMEZONE + space + openingParenthesis + GMT + currentGmtOffset + closingParenthesis);
        CyderFrame miniFrame = new CyderFrame(builder) {
            @Override
            public void dispose() {
                updateMiniClock.set(false);
                super.dispose();
            }
        };
        miniFrame.setTitlePosition(TitlePosition.CENTER);

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

        String threadName = minClockUpdaterThreadNamePrefix + space + CyderStrings.openingBracket
                + GMT + currentGmtOffset + CyderStrings.closingBracket;
        CyderThreadRunner.submit(() -> {
            // Localize since the global can change
            int localGmtOffset = currentGmtOffset;
            while (updateMiniClock.get()) {
                ThreadUtil.sleep(miniClockUpdateTimeout.toMillis());
                currentTimeLabel.setText(getCurrentTimeAccountingForOffset(localGmtOffset));
            }
        }, threadName);

        miniFrame.setVisible(true);
        miniFrame.setLocationRelativeTo(clockFrame);
    }

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
        String key = Props.weatherKey.getValue();

        if (key.isEmpty()) {
            Console.INSTANCE.getConsoleCyderFrame().inform("Sorry, "
                    + "but the Weather Key has not been set or is invalid"
                    + ", as a result, many features of Cyder will not work as intended. "
                    + "Please see the fields panel of the user editor to learn how to acquire a key"
                    + " and set it.", "Weather Key Not Set");

            resetGmtOffset();
            return 0;
        }

        Optional<WeatherData> optionalWeatherData = WeatherUtil.getWeatherData(currentLocation);
        if (optionalWeatherData.isEmpty()) {
            resetGmtOffset();
            return 0;
        }

        WeatherData weatherData = optionalWeatherData.get();
        currentGmtOffset = Integer.parseInt(String.valueOf(weatherData.getTimezone())) / TimeUtil.secondsInHour;
        return currentGmtOffset;
    }

    /**
     * Resets the GMT offset to 0.
     */
    private static void resetGmtOffset() {
        currentGmtOffset = 0;
        currentLocation = DEFAULT_LOCATION;
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
