package cyder.weather;

import com.google.common.base.Preconditions;
import cyder.annotations.CyderAuthor;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.HtmlTags;
import cyder.enumerations.Extension;
import cyder.enumerations.Units;
import cyder.getter.GetInputBuilder;
import cyder.getter.GetterUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.math.AngleUtil;
import cyder.math.InterpolationUtil;
import cyder.network.IpDataManager;
import cyder.network.NetworkUtil;
import cyder.parsers.ip.IpData;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import cyder.ui.UiUtil;
import cyder.ui.drag.button.DragLabelTextButton;
import cyder.ui.frame.CyderFrame;
import cyder.ui.frame.notification.NotificationBuilder;
import cyder.user.UserDataManager;
import cyder.utils.HtmlUtil;
import cyder.utils.ImageUtil;
import cyder.utils.MapUtil;
import cyder.utils.StaticUtil;
import cyder.weather.parsers.WeatherData;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static cyder.strings.CyderStrings.*;

/**
 * A widget for showing the weather for a local area.
 */
@Vanilla
@CyderAuthor
public class WeatherWidget {
    /**
     * The current location label.
     */
    private JLabel locationLabel;

    /**
     * The current weather description label.
     */
    private JLabel currentWeatherLabel;

    /**
     * The custom painted temperature label container to hold the min, current, and max temperatures.
     */
    private JLabel customTempLabel;

    /**
     * The wind speed label.
     */
    private JLabel windSpeedLabel;

    /**
     * The custom painted wind direction label.
     */
    private JLabel windDirectionLabel;

    /**
     * The humidity label.
     */
    private JLabel humidityLabel;

    /**
     * The pressure label.
     */
    private JLabel pressureLabel;

    /**
     * The sunset label.
     */
    private JLabel sunsetLabel;

    /**
     * The sunrise label.
     */
    private JLabel sunriseLabel;

    /**
     * The timezone label.
     */
    private JLabel timezoneLabel;

    /**
     * The current time label.
     */
    private JLabel currentTimeLabel;

    /**
     * The current weather icon label.
     */
    private JLabel currentWeatherIconLabel;

    /**
     * The current temperature label
     */
    private JLabel currentTempLabel;

    /**
     * The min temperature label.
     */
    private JLabel minTempLabel;

    /**
     * The max temperature label.
     */
    private JLabel maxTempLabel;

    /**
     * The sunrise time in unix time format.
     */
    private long sunriseMillis = 0L;

    /**
     * The sunset time in unix time format.
     */
    private long sunsetMillis = 0L;

    /**
     * The sunrise time to display on the label.
     */
    private String sunriseFormatted = "";

    /**
     * The sunset time to display on the label.
     */
    private String sunsetFormatted = "";

    /**
     * The sunrise hour.
     */
    private int sunriseHour;

    /**
     * The sunset hour.
     */
    private int sunsetHour;

    /**
     * The current weather icon resource.
     */
    private String weatherIconId = "01d";

    /**
     * The current weather condition.
     */
    private String weatherCondition = "";

    /**
     * The current wind speed.
     */
    private float windSpeed = 0f;

    /**
     * The current temperature.
     */
    private float temperature = 0f;

    /**
     * The current humidity.
     */
    private float humidity = 0f;

    /**
     * The current pressure.
     */
    private float pressure = 0f;

    /**
     * The current wind direction.
     */
    private float windBearing = 0f;

    /**
     * The current location.
     */
    private String currentLocationString = "";

    /**
     * The previous location.
     */
    private String previousLocationString = "";

    /**
     * The currently set city.
     */
    private String userCity = "";

    /**
     * The currently set state.
     */
    private String userState = "";

    /**
     * The currently set country.
     */
    private String userCountry = "";

    /**
     * The weather frame.
     */
    private CyderFrame weatherFrame;

    /**
     * Whether to use the custom location.
     */
    private boolean useCustomLoc;

    /**
     * Whether to repull weather data every updateFrequency minutes.
     */
    private final AtomicBoolean stopUpdating = new AtomicBoolean(false);

    /**
     * The maximum temperature
     */
    private float minTemp;

    /**
     * The minimum temperature.
     */
    private float maxTemp;

    /**
     * The gmt offset for the current location.
     */
    private int parsedGmtOffset;

    /**
     * The last gmt offset returned when parsing weather data.
     */
    private String weatherDataGmtOffset = "0";

    /**
     * Whether the gmt offset has been set.
     */
    private boolean isGmtSet;

    /**
     * The width of the frame.
     */
    private static final int FRAME_WIDTH = 480;

    /**
     * The height of the frame.
     */
    private static final int FRAME_HEIGHT = 640;

    /**
     * The weather keyword.
     */
    private static final String WEATHER = "weather";

    /**
     * The default frame title.
     */
    private static final String DEFAULT_TITLE = WEATHER;

    /**
     * The shade color for the default background.
     */
    private static final Color shadeColor = new Color(89, 85, 161);

    /**
     * The primary color for the default background.
     */
    private static final Color primaryColorOne = new Color(205, 119, 130);

    /**
     * The primary color for the default background.
     */
    private static final Color primaryColorTwo = new Color(38, 21, 75);

    /**
     * The default frame background.
     */
    private static final BufferedImage defaultBackground = ImageUtil.getImageGradient(FRAME_WIDTH, FRAME_HEIGHT,
            primaryColorOne, primaryColorTwo, shadeColor);

    /**
     * The number of seconds in a singular hour.
     */
    private static final int SECONDS_IN_HOUR = 3600;

    /**
     * The post meridiem string
     */
    private static final String PM = "pm";

    /**
     * The ante meridiem string.
     */
    private static final String AM = "am";

    /**
     * The component width for the custom temperature label.
     */
    private static final int customTempLabelWidth = 400;

    /**
     * The name for the waiting thread for changing the widget's location.
     */
    private static final String WEATHER_LOCATION_CHANGER_THREAD_NAME = "Weather Location Changer";

    /**
     * The description for the @Widget annotation.
     */
    private static final String widgetDescription = "A widget that displays weather data for the current " +
            "city you are in. The location is also changeable";

    /**
     * The getter util instance for changing the weather location.
     */
    private final GetterUtil getterUtilInstance = GetterUtil.getInstance();

    /**
     * The instances of weather widget for this Cyder session.
     */
    private static final ArrayList<WeatherWidget> instances = new ArrayList<>();

    /**
     * The gmt keyword.
     */
    private static final String GMT = "GMT";

    /**
     * The number of comma separated parts for a valid location string.
     */
    private static final int cityStateCountryFormatLen = 3;

    /**
     * The index of the state abbreviation in a city state country string.
     */
    private static final int stateIndex = 1;

    /**
     * The length of USA state abbreviations.
     */
    private static final int stateAbbrLen = 2;

    /**
     * The day time identifier.
     */
    private static final String DAY_IMAGE_ID = "d";

    /**
     * The night time identifier.
     */
    private static final String NIGHT_IMAGE_ID = "n";

    /**
     * The value to add to the center x value for the temperature label within the custom painted component.
     */
    private static final int temperatureLineCenterAdditive = 5;

    /**
     * The refreshed keyword.
     */
    private static final String REFRESHED = "Refreshed";

    /**
     * The dst active bracketed text.
     */
    private static final String DST_ACTIVE = "DST Active";

    /**
     * The date formatter for the sunrise and sunset times.
     */
    private static final SimpleDateFormat sunriseSunsetFormat = new SimpleDateFormat("h:mm");

    /**
     * The builder for acquiring the map view.
     */
    private final MapUtil.Builder mapViewBuilder = new MapUtil.Builder(
            FRAME_WIDTH, FRAME_HEIGHT, Props.mapQuestApiKey.getValue())
            .setFilterWaterMark(true)
            .setScaleBarLocation(MapUtil.ScaleBarLocation.BOTTOM);

    /**
     * The north cardinal direction abbreviation.
     */
    private static final String NORTH = "N";

    /**
     * The south cardinal direction abbreviation.
     */
    private static final String SOUTH = "S";

    /**
     * The east cardinal direction abbreviation.
     */
    private static final String EAST = "E";

    /**
     * The west cardinal direction abbreviation.
     */
    private static final String WEST = "W";

    /**
     * The change location text.
     */
    private static final String CHANGE_LOCATION = "Change Location";

    /**
     * The color for the styled text for the example location.
     */
    private static final Color exampleColor = new Color(45, 100, 220);

    /**
     * The example location.
     */
    private static final String exampleChangeLocationText = Props.defaultLocation.getValue();

    /**
     * The styled example change location text.
     */
    private static final String styledExampleText = HtmlUtil.generateColoredHtmlText(
            exampleChangeLocationText, exampleColor);

    /**
     * The complete change location html styled text to show on the string getter's label.
     */
    private static final String changeLocationHtmlText = HtmlTags.openingHtml
            + "Enter your city, state (if applicable), and country code separated by a comma. Example: "
            + HtmlTags.breakTag + styledExampleText + HtmlTags.closingHtml;

    /**
     * The thread name for the weather stats updater.
     */
    private static final String WEATHER_STATS_UPDATER_THREAD_NAME = "Weather Stats Updater";

    /**
     * The thread name for the weather clock updater.
     */
    private static final String WEATHER_CLOCK_UPDATER_THREAD_NAME = "Weather Clock Updater";

    /**
     * The frequency to update the weather stats.
     */
    private static final Duration updateStatsFrequency = Duration.ofMinutes(5);

    /**
     * The frequency to check the exit condition for the stats updater.
     */
    private static final Duration checkUpdateStatsExitConditionFrequency = Duration.ofSeconds(10);

    /**
     * The last notification text following a location change attempt.
     */
    private String refreshingNotificationText;

    /**
     * The decimal formatter for the result.
     */
    private static final DecimalFormat floatMeasurementFormatter = new DecimalFormat("#.####");

    /**
     * Creates a new weather widget initialized to the user's current location.
     */
    private WeatherWidget() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Shows a new weather widget instance.
     */
    @Widget(triggers = "weather", description = widgetDescription)
    public static void showGui() {
        getInstance().innerShowGui();
    }

    /**
     * Returns a new instance of the weather widget.
     *
     * @return a new instance of the weather widget
     */
    private static WeatherWidget getInstance() {
        WeatherWidget instance = new WeatherWidget();
        instances.add(instance);
        return instance;
    }

    /**
     * Shows the UI since we need to allow multiple instances of weather widget
     * while still having the public static showGui() method with the @Widget annotation.
     */
    private void innerShowGui() {
        if (NetworkUtil.isHighLatency()) {
            Console.INSTANCE.getConsoleCyderFrame().notify("Sorry, "
                    + UserDataManager.INSTANCE.getUsername() + ", but this feature"
                    + " is suspended until a stable internet connection can be established");
            return;
        } else if (!Props.weatherKey.valuePresent()) {
            Console.INSTANCE.getConsoleCyderFrame().inform("Sorry, but the Weather Key has"
                    + " not been set or is invalid, as a result, many features of Cyder will not work as"
                    + " intended. Please see the fields panel of the user editor to learn how to acquire"
                    + " a key and set it.", "Weather Key Not Set");
            return;
        }

        repullWeatherStats();

        UiUtil.closeIfOpen(weatherFrame);
        weatherFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT, CyderColors.regularBlue) {
            @Override
            public void dispose() {
                onWeatherFrameDisposed();
                super.dispose();
            }
        };
        weatherFrame.setBackground(defaultBackground);
        weatherFrame.setTitle(DEFAULT_TITLE);

        currentTimeLabel = new JLabel(getWeatherTimeAccountForGmtOffset(), SwingConstants.CENTER);
        currentTimeLabel.setForeground(CyderColors.navy);
        currentTimeLabel.setFont(CyderFonts.SEGOE_20);
        currentTimeLabel.setBounds(0, 50, 480, 30);
        weatherFrame.getContentPane().add(currentTimeLabel);

        locationLabel = new JLabel(currentLocationString, SwingConstants.CENTER);
        locationLabel.setForeground(CyderColors.navy);
        locationLabel.setFont(CyderFonts.SEGOE_20);
        locationLabel.setBounds(0, 85, 480, 30);
        weatherFrame.getContentPane().add(locationLabel);

        final int strokeWidth = 3;

        JLabel currentWeatherContainer = new JLabel() {
            private static final int arcLen = 25;
            private static final int offset = 10;

            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g.setColor(CyderColors.navy);
                g2d.setStroke(new BasicStroke(strokeWidth));
                g2d.fillRoundRect(offset, offset, 100, 160, arcLen, arcLen);
                super.paint(g);
            }
        };
        currentWeatherContainer.setBounds(180, 120, 120, 180);
        weatherFrame.getContentPane().add(currentWeatherContainer);

        currentWeatherIconLabel = new JLabel(generateCurrentWeatherIcon());
        currentWeatherIconLabel.setBounds(0, 25, currentWeatherContainer.getWidth(),
                currentWeatherContainer.getHeight() / 2);
        currentWeatherContainer.add(currentWeatherIconLabel);

        currentWeatherLabel = new JLabel("", SwingConstants.CENTER);
        currentWeatherLabel.setForeground(CyderColors.vanilla);
        currentWeatherLabel.setFont(CyderFonts.SEGOE_20.deriveFont(18f));
        currentWeatherLabel.setBounds(0, currentWeatherContainer.getHeight() / 2,
                currentWeatherContainer.getWidth(), currentWeatherContainer.getHeight() / 2);
        currentWeatherContainer.add(currentWeatherLabel);

        ImageIcon sunriseIcon = new ImageIcon(StaticUtil.getStaticPath("sunrise.png"));
        JLabel sunriseLabelIcon = new JLabel(sunriseIcon) {
            private static final int arcLen = 25;
            private static final int offset = 10;

            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g.setColor(CyderColors.navy);
                g2d.setStroke(new BasicStroke(strokeWidth));
                g2d.fillRoundRect(offset, offset, 100, 160, arcLen, arcLen);
                super.paint(g);
            }
        };
        sunriseLabelIcon.setBounds(60, 120, 120, 180);
        weatherFrame.getContentPane().add(sunriseLabelIcon);

        sunriseLabel = new JLabel(sunriseFormatted + AM, SwingConstants.CENTER);
        sunriseLabel.setForeground(CyderColors.vanilla);
        sunriseLabel.setFont(CyderFonts.SEGOE_20);
        sunriseLabel.setBounds(0, sunriseLabelIcon.getHeight() / 2, sunriseLabelIcon.getWidth(),
                sunriseLabelIcon.getHeight() / 2);
        sunriseLabelIcon.add(sunriseLabel);

        ImageIcon sunsetIcon = new ImageIcon(StaticUtil.getStaticPath("sunset.png"));
        JLabel sunsetLabelIcon = new JLabel(sunsetIcon) {
            private static final int arcLen = 25;
            private static final int offset = 10;

            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g.setColor(CyderColors.navy);
                ((Graphics2D) g).setStroke(new BasicStroke(strokeWidth));
                g2d.fillRoundRect(offset, offset, 100, 160, arcLen, arcLen);
                super.paint(g);
            }
        };
        sunsetLabelIcon.setBounds(480 - 60 - 120, 120, 120, 180);
        weatherFrame.getContentPane().add(sunsetLabelIcon);

        sunsetLabel = new JLabel(sunsetFormatted + PM, SwingConstants.CENTER);
        sunsetLabel.setForeground(CyderColors.vanilla);
        sunsetLabel.setFont(CyderFonts.SEGOE_20);
        sunsetLabel.setBounds(0, sunsetLabelIcon.getHeight() / 2, sunsetLabelIcon.getWidth(),
                sunsetLabelIcon.getHeight() / 2);
        sunsetLabelIcon.add(sunsetLabel);

        GetInputBuilder changeLocationBuilder = new GetInputBuilder(CHANGE_LOCATION, changeLocationHtmlText)
                .setRelativeTo(weatherFrame)
                .setSubmitButtonText(CHANGE_LOCATION)
                .setLabelFont(CyderFonts.DEFAULT_FONT_SMALL)
                .setInitialFieldText(currentLocationString)
                .setSubmitButtonColor(CyderColors.regularPurple);

        DragLabelTextButton locationButton = new DragLabelTextButton.Builder("Change Location")
                .setClickAction(() -> CyderThreadRunner.submit(() -> {
                    getterUtilInstance.closeAllGetFrames();
                    Optional<String> optionalNewLocation = getterUtilInstance.getInput(changeLocationBuilder);

                    try {
                        if (optionalNewLocation.isEmpty()) return;
                        String newLocation = optionalNewLocation.get();
                        previousLocationString = currentLocationString;
                        ArrayList<String> parts = Arrays.stream(newLocation.split(CyderStrings.comma))
                                .map(string -> StringUtil.capsFirstWords(string.trim()))
                                .collect(Collectors.toCollection(ArrayList::new));
                        currentLocationString = StringUtil.joinParts(parts, CyderStrings.comma);
                        useCustomLoc = true;

                        refreshingNotificationText = "Attempting to refresh weather stats for location "
                                + CyderStrings.quote + currentLocationString + CyderStrings.quote;
                        weatherFrame.notify(refreshingNotificationText);

                        repullWeatherStats();
                    } catch (Exception ex) {
                        ExceptionHandler.handle(ex);
                    }
                }, WEATHER_LOCATION_CHANGER_THREAD_NAME)).build();

        weatherFrame.getTopDragLabel().addLeftButton(locationButton, 0);

        minTempLabel = new JLabel();
        minTempLabel.setForeground(CyderColors.vanilla);
        minTempLabel.setFont(CyderFonts.DEFAULT_FONT_SMALL);
        weatherFrame.getContentPane().add(minTempLabel);

        maxTempLabel = new JLabel();
        maxTempLabel.setForeground(CyderColors.vanilla);
        maxTempLabel.setFont(CyderFonts.DEFAULT_FONT_SMALL);

        currentTempLabel = new JLabel();
        currentTempLabel.setForeground(CyderColors.regularPink);
        currentTempLabel.setFont(CyderFonts.DEFAULT_FONT_SMALL);

        customTempLabel = new JLabel() {
            private static final int borderLen = 3;
            private static final int componentHeight = 40;

            @Override
            public void paintComponent(Graphics g) {
                int w = customTempLabelWidth - 2 * borderLen;
                int h = componentHeight - 2 * borderLen;
                g.setColor(CyderColors.navy);
                g.fillRect(borderLen, borderLen, w, h);

                int mappedTemperatureValue = (int) Math.round(
                        InterpolationUtil.rangeMap(temperature, minTemp, maxTemp, 0, customTempLabelWidth));
                int yOffset = 3;
                int lineWidth = 6;
                g.setColor(CyderColors.regularPink);
                g.fillRect(mappedTemperatureValue + yOffset, borderLen,
                        lineWidth, componentHeight - 2 * yOffset);

                String minTempText = formatFloatMeasurement(minTemp) + "F";
                minTempLabel.setText(minTempText);
                minTempLabel.setSize(
                        StringUtil.getMinWidth(minTempText, minTempLabel.getFont()),
                        StringUtil.getMinHeight(minTempText, minTempLabel.getFont()));
                minTempLabel.setLocation(10, (componentHeight - minTempLabel.getHeight()) / 2);
                customTempLabel.add(minTempLabel);

                String currentTempText = formatFloatMeasurement(temperature) + "F";
                currentTempLabel.setText(currentTempText);
                currentTempLabel.setSize(
                        StringUtil.getMinWidth(currentTempText, currentTempLabel.getFont()),
                        StringUtil.getMinHeight(currentTempText, currentTempLabel.getFont()));
                currentTempLabel.setLocation(customTempLabel.getWidth() / 2 - currentTempLabel.getWidth() / 2,
                        customTempLabel.getHeight() / 2 - currentTempLabel.getHeight() / 2);
                customTempLabel.add(currentTempLabel);

                // set max temp label
                String maxText = formatFloatMeasurement(maxTemp) + "F";
                maxTempLabel.setText(maxText);
                maxTempLabel.setSize(
                        StringUtil.getMinWidth(maxText, minTempLabel.getFont()),
                        StringUtil.getMinHeight(maxText, minTempLabel.getFont()));
                maxTempLabel.setLocation(customTempLabelWidth - maxTempLabel.getWidth(),
                        (componentHeight - maxTempLabel.getHeight()) / 2);
                customTempLabel.add(maxTempLabel);

                g.setColor(Color.black);

                g.fillRect(0, 0, borderLen, componentHeight);
                g.fillRect(customTempLabelWidth - borderLen, 0, borderLen, componentHeight);
                g.fillRect(0, 0, customTempLabelWidth, borderLen);
                g.fillRect(0, componentHeight - borderLen, customTempLabelWidth, borderLen);
            }
        };
        customTempLabel.setBounds(40, 320, customTempLabelWidth, 40);
        weatherFrame.getContentPane().add(customTempLabel);

        windSpeedLabel = new JLabel("", SwingConstants.CENTER);
        windSpeedLabel.setText("Wind: " + windSpeed + Units.MILES_PER_HOUR.getAbbreviation()
                + comma + space + windBearing + Units.DEGREES.getAbbreviation() + space + openingParenthesis
                + getWindDirection(windBearing) + CyderStrings.closingParenthesis);
        windSpeedLabel.setForeground(CyderColors.navy);
        windSpeedLabel.setFont(CyderFonts.SEGOE_20);
        windSpeedLabel.setBounds(0, 390, 480, 30);
        weatherFrame.getContentPane().add(windSpeedLabel);

        windDirectionLabel = new JLabel() {
            private static final int length = 50;
            private static final int borderLength = 3;
            private static final int arrowWidth = 3;
            private static final int arrowRadius = 20;

            private static final Color backgroundColor = Color.black;
            private static final Color innerColor = CyderColors.navy;
            private static final Color arrowColor = CyderColors.regularPink;

            @Override
            public void paintComponent(Graphics g) {
                g.setColor(backgroundColor);
                g.fillRect(0, 0, length, length);

                g.setColor(innerColor);
                g.fillRect(borderLength, borderLength,
                        getWidth() - 2 * borderLength, getHeight() - 2 * borderLength);

                double theta = windBearing * Math.PI / 180.0;
                double x = arrowRadius * Math.cos(theta);
                double y = arrowRadius * Math.sin(theta);

                int drawToX = (int) Math.round(x);
                int drawToY = -(int) Math.round(y);

                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(new BasicStroke(arrowWidth));

                int w = getWidth();
                int h = getHeight();

                g.setColor(arrowColor);
                g.drawLine(w / 2, h / 2, w / 2 + drawToX, w / 2 + drawToY);
            }
        };
        windDirectionLabel.setBounds(weatherFrame.getWidth() / 2 - 50 / 2, 430, 50, 50);
        weatherFrame.getContentPane().add(windDirectionLabel);

        humidityLabel = new JLabel("Humidity: " + humidity + "%", SwingConstants.CENTER);
        humidityLabel.setForeground(CyderColors.navy);
        humidityLabel.setFont(CyderFonts.SEGOE_20);
        humidityLabel.setBounds(0, 500, 480, 30);
        weatherFrame.getContentPane().add(humidityLabel);

        pressureLabel = new JLabel("Pressure: " + pressure + Units.ATMOSPHERES.getAbbreviation(),
                SwingConstants.CENTER);
        pressureLabel.setForeground(CyderColors.navy);
        pressureLabel.setFont(CyderFonts.SEGOE_20);
        pressureLabel.setBounds(0, 540, 480, 30);
        weatherFrame.getContentPane().add(pressureLabel);

        timezoneLabel = new JLabel("Timezone: " + getGmtTimezoneLabelText(), SwingConstants.CENTER);
        timezoneLabel.setForeground(CyderColors.navy);
        timezoneLabel.setFont(CyderFonts.SEGOE_20);
        timezoneLabel.setBounds(0, 580, 480, 30);
        weatherFrame.getContentPane().add(timezoneLabel);

        weatherFrame.finalizeAndShow();

        startWeatherStatsUpdater();
        startUpdatingClock();
    }

    /**
     * The actions to invoke when this weather frame is disposed.
     */
    private void onWeatherFrameDisposed() {
        stopUpdating.set(true);
        instances.remove(this);
        getterUtilInstance.closeAllGetFrames();
    }

    /**
     * Starts the thread to update the current time label.
     */
    private void startUpdatingClock() {
        CyderThreadRunner.submit(() -> {
            while (!stopUpdating.get()) {
                ThreadUtil.sleep((long) TimeUtil.millisInSecond);
                currentTimeLabel.setText(getWeatherTimeAccountForGmtOffset());
            }
        }, WEATHER_CLOCK_UPDATER_THREAD_NAME);
    }

    /**
     * Starts the thread to update the weather stats.
     */
    private void startWeatherStatsUpdater() {
        CyderThreadRunner.submit(() -> {
            try {
                while (true) {
                    ThreadUtil.sleepWithChecks(updateStatsFrequency.toMillis(),
                            checkUpdateStatsExitConditionFrequency.toMillis(), stopUpdating);
                    if (stopUpdating.get()) break;
                    repullWeatherStats();
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, WEATHER_STATS_UPDATER_THREAD_NAME);
    }

    /**
     * Returns the current weather time correct based on the current gmt offset.
     *
     * @return the current weather time correct based on the current gmt offset
     */
    private String getWeatherTimeAccountForGmtOffset() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormatter = TimeUtil.weatherFormat;
        dateFormatter.setTimeZone(TimeZone.getTimeZone(GMT));

        try {
            int timeOffset = Integer.parseInt(weatherDataGmtOffset) / SECONDS_IN_HOUR;
            calendar.add(Calendar.HOUR, timeOffset);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return dateFormatter.format(calendar.getTime());
    }

    /**
     * Refreshes the weather labels based off of the current vars.
     */
    private void refreshWeatherLabels() {
        if (currentLocationString.length() > 1) {
            String[] parts = currentLocationString.split(CyderStrings.comma);
            StringBuilder sb = new StringBuilder();

            for (int i = 0 ; i < parts.length ; i++) {
                String part = parts[i].trim();

                boolean properLength = parts.length == cityStateCountryFormatLen;
                boolean isState = i == stateIndex;
                boolean stateAbbreviationLength = part.length() == stateAbbrLen;
                if (properLength && isState && stateAbbreviationLength) {
                    sb.append(part.toUpperCase());
                } else {
                    sb.append(StringUtil.capsFirstWords(part));
                }

                if (i != parts.length - 1) sb.append(", ");
            }

            locationLabel.setText(sb.toString());
        } else {
            locationLabel.setText("");
        }

        currentWeatherIconLabel.setIcon(generateCurrentWeatherIcon());

        currentWeatherLabel.setText(HtmlTags.openingHtml
                + HtmlTags.divTextAlignCenterVerticalAlignBottom
                + StringUtil.capsFirstWords(weatherCondition)
                .replaceAll(CyderRegexPatterns.whiteSpaceRegex, HtmlTags.breakTag)
                + HtmlTags.closingHtml);

        windSpeedLabel.setText("Wind" + colon + space + windSpeed + Units.MILES_PER_HOUR.getAbbreviation()
                + CyderStrings.comma + space + windBearing + Units.DEGREES.getAbbreviation() + space
                + CyderStrings.openingParenthesis + getWindDirection(windBearing)
                + CyderStrings.closingParenthesis);
        humidityLabel.setText("Humidity: " + humidity + "%");
        pressureLabel.setText("Pressure: " + formatFloatMeasurement(pressure) + Units.ATMOSPHERES.getAbbreviation());
        timezoneLabel.setText("Timezone: " + getGmtTimezoneLabelText());

        String sunriseMeridiemModifier = sunriseHour < 12 ? AM : PM;
        String sunsetMeridiemModifier = sunsetHour >= 12 ? AM : PM;

        sunriseLabel.setText(formatTimeAccountingForGmtOffset(sunriseFormatted) + sunriseMeridiemModifier);
        sunsetLabel.setText(formatTimeAccountingForGmtOffset(sunsetFormatted) + sunsetMeridiemModifier);

        customTempLabel.repaint();
        currentTempLabel.setText(temperature + "F");

        int tempLabelWidth = StringUtil.getMinWidth(currentTempLabel.getText(), currentTempLabel.getFont());
        int tempLabelHeight = StringUtil.getMinHeight(currentTempLabel.getText(), currentTempLabel.getFont());

        int tempLabelPadding = 3;
        currentTempLabel.setBounds(calculateTemperatureLineCenter(temperature, minTemp, maxTemp),
                customTempLabel.getY() - tempLabelPadding - tempLabelHeight, tempLabelWidth, tempLabelHeight);

        windDirectionLabel.repaint();

        String splitCity = currentLocationString.split(CyderStrings.comma)[0];
        refreshFrameTitle(splitCity);

        if (weatherFrame != null) {
            weatherFrame.revokeAllNotifications();
            weatherFrame.toast(new NotificationBuilder(REFRESHED).setViewDuration(1000));
        }
    }

    /**
     * Returns an ImageIcon for the current weather state.
     *
     * @return an ImageIcon for the current weather state
     */
    private ImageIcon generateCurrentWeatherIcon() {
        long sunsetTime = new Date(sunsetMillis * 1000).getTime();
        long currentTime = new Date().getTime();

        boolean isAfterSunset = currentTime > sunsetTime;
        String weatherIconIdAndTime = weatherIconId.replaceAll(CyderRegexPatterns.englishLettersRegex, "")
                + (isAfterSunset ? NIGHT_IMAGE_ID : DAY_IMAGE_ID);

        return StaticUtil.getImageIcon(weatherIconIdAndTime + Extension.PNG.getExtension());
    }

    /**
     * Calculates the x center for the current temperature within the temperature label.
     *
     * @param temperature the current temperature
     * @param minTemp     the minimum temperature
     * @param maxTemp     the maximum temperature
     * @return the x center for the current temperature within the temperature label
     */
    private int calculateTemperatureLineCenter(float temperature, float minTemp, float maxTemp) {
        int tempLabelWidth = StringUtil.getMinWidth(currentTempLabel.getText(), currentTempLabel.getFont());

        int customTempLabelMinX = customTempLabel.getX();
        int customTempLabelMaxX = customTempLabel.getX() + customTempLabel.getWidth() - tempLabelWidth;

        int temperatureLineCenter = (int) Math.ceil(customTempLabel.getX() + InterpolationUtil.rangeMap(
                temperature, minTemp, maxTemp, 0, customTempLabelWidth)) + temperatureLineCenterAdditive;
        temperatureLineCenter -= (tempLabelWidth) / 2;

        if (temperatureLineCenter < customTempLabelMinX) {
            temperatureLineCenter = customTempLabelMinX;
        }

        if (temperatureLineCenter > customTempLabelMaxX) {
            temperatureLineCenter = customTempLabelMaxX;
        }

        return temperatureLineCenter;
    }

    /**
     * Refreshes the frame title based on the provided city.
     *
     * @param city the city to display in the frame title
     */
    private void refreshFrameTitle(String city) {
        Preconditions.checkNotNull(city);

        city = city.trim();

        if (!city.isEmpty()) {
            String correctedCityName = StringUtil.capsFirstWords(city).trim();
            weatherFrame.setTitle(correctedCityName + StringUtil.getApostropheSuffix(correctedCityName)
                    + CyderStrings.space + WEATHER);
        } else {
            weatherFrame.setTitle(DEFAULT_TITLE);
        }
    }

    /**
     * Returns the text for the timezone label. For example, if weatherDataGmtOffset is -18000
     * and DST is active, then the method will return "GMT-5 [DST Active].
     *
     * @return the text for the timezone label
     */
    private String getGmtTimezoneLabelText() {
        IpData data = IpDataManager.INSTANCE.getIpData();

        String gmtPart = GMT + (Integer.parseInt(weatherDataGmtOffset) / SECONDS_IN_HOUR);
        String dstPart = data.getTime_zone().isIs_dst() ?
                CyderStrings.space + CyderStrings.openingBracket + DST_ACTIVE + CyderStrings.closingBracket : "";

        return gmtPart + dstPart;
    }

    /**
     * Returns the hh:mm time after accounting for the GMT offset.
     *
     * @param absoluteTime the absolute hh:mm time
     * @return the hh:mm time after accounting for the GMT offset
     */
    private String formatTimeAccountingForGmtOffset(String absoluteTime) {
        Preconditions.checkNotNull(absoluteTime);
        Preconditions.checkArgument(absoluteTime.contains(colon));

        String[] splitTime = absoluteTime.split(colon);
        Preconditions.checkState(splitTime.length == 2);

        int hour = Integer.parseInt(splitTime[0]);
        int minute = Integer.parseInt(splitTime[1]);

        hour += (Integer.parseInt(weatherDataGmtOffset) / SECONDS_IN_HOUR - (parsedGmtOffset / SECONDS_IN_HOUR));

        return hour + colon + formatMinutes(minute);
    }

    /**
     * Formats the provided minutes to always have two digits.
     *
     * @param minute the minutes value
     * @return the formatted minutes string
     */
    private String formatMinutes(int minute) {
        Preconditions.checkArgument(TimeUtil.minuteRange.contains(minute));

        return minute < 10 ? "0" + minute : String.valueOf(minute);
    }

    /**
     * Refreshes the weather stat variables.
     */
    private void repullWeatherStats() {
        CyderThreadRunner.submit(() -> {
            IpData data = IpDataManager.INSTANCE.getIpData();

            userCity = data.getCity();
            userState = data.getRegion();
            userCountry = data.getCountry_name();

            if (!useCustomLoc) currentLocationString = userCity + ", " + userState + ", " + userCountry;

            Optional<WeatherData> optionalWeatherData = WeatherUtil.getWeatherData(currentLocationString);
            if (optionalWeatherData.isEmpty()) {
                weatherFrame.revokeNotification(refreshingNotificationText);
                weatherFrame.notify("Sorry, but that location is invalid");
                currentLocationString = previousLocationString;
                useCustomLoc = false;
                return;
            }

            WeatherData weatherData = optionalWeatherData.get();
            sunriseMillis = Long.parseLong(String.valueOf(weatherData.getSys().getSunrise()));
            sunsetMillis = Long.parseLong(String.valueOf(weatherData.getSys().getSunset()));
            weatherIconId = weatherData.getWeather().get(0).getIcon();
            windSpeed = weatherData.getWind().getSpeed();
            windBearing = weatherData.getWind().getDeg();
            weatherCondition = weatherData.getWeather().get(0).getDescription();
            pressure = weatherData.getMain().getPressure();
            humidity = weatherData.getMain().getHumidity();
            temperature = weatherData.getMain().getTemp();
            weatherDataGmtOffset = String.valueOf(weatherData.getTimezone());
            minTemp = weatherData.getMain().getTemp_min();
            maxTemp = weatherData.getMain().getTemp_max();

            refreshMapBackground();

            Date sunrise = new Date((long) (sunriseMillis * TimeUtil.millisInSecond));
            sunriseFormatted = sunriseSunsetFormat.format(sunrise);
            Calendar sunriseCalendar = GregorianCalendar.getInstance();
            sunriseCalendar.setTimeInMillis(sunriseMillis);
            sunriseHour = sunriseCalendar.get(Calendar.HOUR);

            Date sunset = new Date((long) (sunsetMillis * TimeUtil.millisInSecond));
            sunsetFormatted = sunriseSunsetFormat.format(sunset);
            Calendar sunsetCalendar = GregorianCalendar.getInstance();
            sunsetCalendar.setTimeInMillis(sunsetMillis);
            sunsetHour = sunsetCalendar.get(Calendar.HOUR);

            if (!isGmtSet) {
                parsedGmtOffset = Integer.parseInt(weatherDataGmtOffset);
                isGmtSet = true;
            }

            refreshWeatherLabels();

            Console.INSTANCE.revalidateConsoleTaskbarMenu();
        }, WEATHER_STATS_UPDATER_THREAD_NAME);
    }

    /**
     * Refreshes the map background of the weather frame. If not enabled, hides the map.
     * If enabled, shows the map.
     */
    public void refreshMapBackground() {
        try {
            if (UserDataManager.INSTANCE.shouldDrawWeatherMap()) {
                ImageIcon newMapBackground = mapViewBuilder.setLocationString(currentLocationString).getMapView();
                weatherFrame.setBackground(newMapBackground);
            } else {
                weatherFrame.setBackground(defaultBackground);
            }

            refreshReadableLabels(UserDataManager.INSTANCE.shouldDrawWeatherMap());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            weatherFrame.notify("Could not refresh map background");
        }
    }

    /**
     * Refreshes the labels with raw text on them based on the current visibility of the background map.
     *
     * @param mapVisible whether the map is visible
     */
    private void refreshReadableLabels(boolean mapVisible) {
        if (mapVisible) {
            currentTimeLabel.setForeground(CyderColors.navy);
            locationLabel.setForeground(CyderColors.navy);
            windSpeedLabel.setForeground(CyderColors.navy);
            humidityLabel.setForeground(CyderColors.navy);
            pressureLabel.setForeground(CyderColors.navy);
            timezoneLabel.setForeground(CyderColors.navy);
        } else {
            currentTimeLabel.setForeground(CyderColors.vanilla);
            locationLabel.setForeground(CyderColors.vanilla);
            windSpeedLabel.setForeground(CyderColors.vanilla);
            humidityLabel.setForeground(CyderColors.vanilla);
            pressureLabel.setForeground(CyderColors.vanilla);
            timezoneLabel.setForeground(CyderColors.vanilla);
        }
    }

    /**
     * Refreshes the map background of all weather instances.
     */
    public static void refreshAllMapBackgrounds() {
        instances.forEach(WeatherWidget::refreshMapBackground);
    }

    /**
     * Returns the wind direction string based off of the current wind bearing.
     *
     * @param bearing the current wind bearing
     * @return the wind direction string based off of the current wind bearing
     */
    public String getWindDirection(double bearing) {
        bearing = AngleUtil.normalizeAngle360(bearing);

        StringBuilder ret = new StringBuilder();

        if (AngleUtil.angleInNorthernHemisphere(bearing)) {
            ret.append(NORTH);

            if (bearing > AngleUtil.NINETY_DEGREES) {
                ret.append(WEST);
            } else if (bearing < AngleUtil.NINETY_DEGREES) {
                ret.append(EAST);
            }
        } else if (AngleUtil.angleInSouthernHemisphere(bearing)) {
            ret.append(SOUTH);

            if (bearing < AngleUtil.TWO_SEVENTY_DEGREES) {
                ret.append(WEST);
            } else if (bearing > AngleUtil.TWO_SEVENTY_DEGREES) {
                ret.append(EAST);
            }
        } else if (AngleUtil.angleIsEast(bearing)) {
            ret.append(EAST);
        } else if (AngleUtil.angleIsWest(bearing)) {
            ret.append(WEST);
        }

        return ret.toString();
    }

    /**
     * Returns the float formatted using {@link #floatMeasurementFormatter}.
     *
     * @param measurement the float measurement to format
     * @return the formatted measurement
     */
    private static String formatFloatMeasurement(float measurement) {
        return floatMeasurementFormatter.format(measurement);
    }
}
