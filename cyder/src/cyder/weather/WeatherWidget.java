package cyder.weather;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import cyder.annotations.CyderAuthor;
import cyder.annotations.ForReadability;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.enums.Extension;
import cyder.getter.GetInputBuilder;
import cyder.getter.GetterUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.math.AngleUtil;
import cyder.network.IpUtil;
import cyder.network.NetworkUtil;
import cyder.parsers.remote.weather.WeatherData;
import cyder.props.PropLoader;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import cyder.ui.frame.CyderFrame;
import cyder.user.UserUtil;
import cyder.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static cyder.constants.CyderStrings.colon;

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
    private String sunriseMillis = "0";

    /**
     * The sunset time in unix time format.
     */
    private String sunsetMillis = "0";

    /**
     * The sunrise time to display on the label.
     */
    private String sunriseFormatted = "";

    /**
     * The sunset time to display on the label.
     */
    private String sunsetFormatted = "";

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
     * The current latitude.
     */
    private double lat = 0d;

    /**
     * The current longitude.
     */
    private double lon = 0d;

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
     * The frequency at which to update the weather data at in minutes.
     */
    private final int updateFrequency = 5;

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
     * The key for obtaining the weather key from the props.
     */
    private static final String WEATHER_KEY = "weather_key";

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
    private static final LinkedList<WeatherWidget> instances = new LinkedList<>();

    /**
     * Returns a new instance of weather widget.
     *
     * @return a new instance of weather widget
     */
    public static WeatherWidget getInstance() {
        WeatherWidget instance = new WeatherWidget();
        instances.add(instance);
        return instance;
    }

    /**
     * Creates a new weather widget initialized to the user's current location.
     */
    private WeatherWidget() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    @Widget(triggers = "weather", description = widgetDescription)
    public static void showGui() {
        getInstance().innerShowGui();
    }

    /**
     * Shows the UI since we need to allow multiple instances of weather widget
     * while still having the public static showGui() method with the @Widget annotation.
     */
    private void innerShowGui() {
        if (NetworkUtil.isHighLatency()) {
            Console.INSTANCE.getConsoleCyderFrame().notify("Sorry, "
                    + UserUtil.getCyderUser().getName() + ", but this feature "
                    + "is suspended until a stable internet connection can be established");
            return;
        } else if (StringUtil.isNullOrEmpty(PropLoader.getString(WEATHER_KEY))) {
            Console.INSTANCE.getConsoleCyderFrame().inform("Sorry, but the Weather Key has "
                    + "not been set or is invalid, as a result, many features of Cyder will not work as"
                    + " intended. Please see the fields panel of the user editor to learn how to acquire "
                    + "a key and set it.", "Weather Key Not Set");
            return;
        }

        repullWeatherStats();

        UiUtil.closeIfOpen(weatherFrame);
        WeatherWidget thisInstance = this;
        weatherFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT, CyderColors.regularBlue) {
            @Override
            public void dispose() {
                stopUpdating.set(true);
                instances.remove(thisInstance);
                getterUtilInstance.closeAllGetFrames();
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

        JLabel currentWeatherContainer = new JLabel() {
            private static final int arcLen = 25;
            private static final int offset = 10;

            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g.setColor(CyderColors.navy);
                g2d.setStroke(new BasicStroke(3));
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

        ImageIcon sunriseIcon = new ImageIcon(
                OsUtil.buildPath("static", "pictures", WEATHER, "sunrise.png"));
        JLabel sunriseLabelIcon = new JLabel(sunriseIcon) {
            private static final int arcLen = 25;
            private static final int offset = 10;
            private static final int strokeWidth = 3;

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

        ImageIcon sunsetIcon = new ImageIcon("static/pictures/weather/sunset.png");
        JLabel sunsetLabelIcon = new JLabel(sunsetIcon) {
            private static final int arcLen = 25;
            private static final int offset = 10;
            private static final int strokeWidth = 3;

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

        weatherFrame.setMenuEnabled(true);
        weatherFrame.addMenuItem("Location", () -> CyderThreadRunner.submit(() -> {
            getterUtilInstance.closeAllGetFrames();
            Optional<String> optionalNewLocation = getterUtilInstance.getInput(changeLocationBuilder);

            try {
                if (optionalNewLocation.isEmpty()) return;
                String newLocation = optionalNewLocation.get();
                previousLocationString = currentLocationString;
                String[] newLocationParts = newLocation.split(CyderStrings.comma);

                StringBuilder sb = new StringBuilder();
                for (int i = 0 ; i < newLocationParts.length ; i++) {
                    sb.append(StringUtil.capsFirstWords(newLocationParts[i].trim()));

                    if (i != newLocationParts.length - 1) sb.append(CyderStrings.comma);
                }

                currentLocationString = sb.toString();
                useCustomLoc = true;

                weatherFrame.notify("Attempting to refresh weather stats for location \""
                        + currentLocationString + CyderStrings.quote);

                repullWeatherStats();

                weatherFrame.hideMenu();
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }, WEATHER_LOCATION_CHANGER_THREAD_NAME));

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
            @Override
            public void paintComponent(Graphics g) {
                int w = customTempLabelWidth - 2 * borderLen;
                int h = componentHeight - 2 * borderLen;
                g.setColor(CyderColors.navy);
                g.fillRect(borderLen, borderLen, w, h);

                int mappedTemperatureValue = (int) Math.round(map(temperature, minTemp, maxTemp));
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
                paintCustomBorder(g);
            }

            /**
             * The length of the border around this component.
             */
            private static final int borderLen = 3;

            /**
             * The height of this component
             */
            public static final int componentHeight = 40;

            @ForReadability
            private void paintCustomBorder(Graphics g) {
                g.fillRect(0, 0, borderLen, componentHeight);
                g.fillRect(customTempLabelWidth - borderLen, 0, borderLen, componentHeight);
                g.fillRect(0, 0, customTempLabelWidth, borderLen);
                g.fillRect(0, componentHeight - borderLen, customTempLabelWidth, borderLen);
            }
        };
        customTempLabel.setBounds(40, 320, customTempLabelWidth, 40);
        weatherFrame.getContentPane().add(customTempLabel);

        windSpeedLabel = new JLabel("", SwingConstants.CENTER);
        windSpeedLabel.setText("Wind: " + windSpeed + "mph, " + windBearing
                + "deg (" + getWindDirection(windBearing) + CyderStrings.closingParenthesis);
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

        pressureLabel = new JLabel("Pressure: " + pressure + "atm",
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
    private static final String exampleChangeLocationText = "New Orleans, LA, US";

    /**
     * The styled example change location text.
     */
    private static final String styledExampleText = BoundsUtil.generateColoredHtmlText(
            exampleChangeLocationText, exampleColor);

    /**
     * The complete change location html styled text to show on the string getter's label.
     */
    private static final String changeLocationHtmlText = BoundsUtil.OPENING_HTML_TAG
            + "Enter your city, state, and country code separated by a comma. Example: "
            + styledExampleText + BoundsUtil.CLOSING_HTML_TAG;

    /**
     * The builder for changing the current weather location.
     */
    private final GetInputBuilder changeLocationBuilder = new GetInputBuilder(CHANGE_LOCATION, changeLocationHtmlText)
            .setRelativeTo(weatherFrame)
            .setSubmitButtonText(CHANGE_LOCATION)
            .setInitialFieldText(currentLocationString)
            .setSubmitButtonColor(CyderColors.notificationForegroundColor);

    /**
     * The thread name for the weather stats updater.
     */
    private static final String WEATHER_STATS_UPDATER_THREAD_NAME = "Weather Stats Updater";

    /**
     * The thread name for the weather clock updater.
     */
    private static final String WEATHER_CLOCK_UPDATER_THREAD_NAME = "Weather Clock Updater";

    /**
     * Starts the thread to update the current time label.
     */
    @ForReadability
    private void startUpdatingClock() {
        CyderThreadRunner.submit(() -> {
            while (!stopUpdating.get()) {
                ThreadUtil.sleep((long) TimeUtil.MILLISECONDS_IN_SECOND);
                currentTimeLabel.setText(getWeatherTimeAccountForGmtOffset());
            }
        }, WEATHER_CLOCK_UPDATER_THREAD_NAME);
    }

    /**
     * Starts the thread to update the weather stats.
     */
    @ForReadability
    private void startWeatherStatsUpdater() {
        CyderThreadRunner.submit(() -> {
            try {
                int sleepTime = updateFrequency * 1000 * 60;
                int checkFrequency = 1000 * 10;

                while (true) {
                    ThreadUtil.sleepWithChecks(sleepTime, checkFrequency, stopUpdating);
                    if (stopUpdating.get()) {
                        break;
                    }

                    repullWeatherStats();
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, WEATHER_STATS_UPDATER_THREAD_NAME);
    }

    /**
     * Maps the value from the old range to the range [0, customTempLabelWidth].
     *
     * @param value       the value to map
     * @param oldRangeMin the old range's min
     * @param oldRangeMax the old range's max
     * @return the value mapped from the old range to the new range
     */
    private double map(double value, double oldRangeMin, double oldRangeMax) {
        return (value - oldRangeMin) * (float) customTempLabelWidth / (oldRangeMax - oldRangeMin);
    }

    /**
     * The gmt keyword.
     */
    private static final String GMT = "GMT";

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
     * The length for a string with two commas containing city, state, and country.
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

    @ForReadability
    private boolean isRepresentativeOfStateAbbreviation(int locationStringSplitLength, int currentIndex, String part) {
        return locationStringSplitLength == cityStateCountryFormatLen
                && currentIndex == stateIndex
                && part.length() == stateAbbrLen;
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

                if (isRepresentativeOfStateAbbreviation(parts.length, i, part)) {
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

        String centeringDivText = "<div style='text-align: center; vertical-align:bottom'>";
        currentWeatherLabel.setText(BoundsUtil.OPENING_HTML_TAG
                + centeringDivText
                + StringUtil.capsFirstWords(weatherCondition)
                .replaceAll(CyderRegexPatterns.whiteSpaceRegex, "<br/>")
                + BoundsUtil.CLOSING_HTML_TAG);

        windSpeedLabel.setText("Wind: " + windSpeed + "mph, " + windBearing
                + "deg (" + getWindDirection(windBearing) + CyderStrings.closingParenthesis);
        humidityLabel.setText("Humidity: " + humidity + "%");
        pressureLabel.setText("Pressure: " + formatFloatMeasurement(pressure) + "atm");
        timezoneLabel.setText("Timezone: " + getGmtTimezoneLabelText());

        // todo using AM or PM should be determined by determining if the time truly is after/before 12:00am
        sunriseLabel.setText(accountForGmtOffset(sunriseFormatted) + AM);
        sunsetLabel.setText(accountForGmtOffset(sunsetFormatted) + PM);

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

        if (weatherFrame != null) weatherFrame.toast(
                new CyderFrame.NotificationBuilder(REFRESHED).setViewDuration(1000));
    }

    /**
     * The regex to target all latin characters.
     */
    private static final String replaceLettersRegex = "[a-zA-Z]+";

    /**
     * The day time identifier.
     */
    private static final String D = "d";

    /**
     * The night time identifier.
     */
    private static final String N = "n";

    @ForReadability
    private ImageIcon generateCurrentWeatherIcon() {
        long sunsetTime = new Date((long) Integer.parseInt(sunsetMillis) * 1000).getTime();
        long currentTime = new Date().getTime();

        boolean isAfterSunset = currentTime > sunsetTime;
        String weatherIconIdAndTime = weatherIconId.replaceAll(replaceLettersRegex, "")
                + (isAfterSunset ? N : D);

        return new ImageIcon(OsUtil.buildPath("static", "pictures", WEATHER,
                weatherIconIdAndTime + Extension.PNG.getExtension()));
    }

    /**
     * The value to add to the center x value for the temperature label within the custom painted component.
     */
    private static final int temperatureLineCenterAdditive = 5;

    @ForReadability
    private int calculateTemperatureLineCenter(float temperature, float minTemp, float maxTemp) {
        int tempLabelWidth = StringUtil.getMinWidth(currentTempLabel.getText(), currentTempLabel.getFont());

        int customTempLabelMinX = customTempLabel.getX();
        int customTempLabelMaxX = customTempLabel.getX() + customTempLabel.getWidth() - tempLabelWidth;

        int temperatureLineCenter = (int) Math.ceil(customTempLabel.getX()
                + map(temperature, minTemp, maxTemp)) + temperatureLineCenterAdditive;
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
     * The refreshed keyword.
     */
    private static final String REFRESHED = "Refreshed";

    /**
     * Refreshes the frame title based on the provided city.
     *
     * @param city the city to display in the frame title
     */
    @ForReadability
    private void refreshFrameTitle(String city) {
        Preconditions.checkNotNull(city);
        city = city.trim();

        if (!city.isEmpty()) {
            String correctedCityName = StringUtil.capsFirstWords(city).trim();
            weatherFrame.setTitle(correctedCityName + StringUtil.getApostrophe(correctedCityName)
                    + CyderStrings.space + WEATHER);
        } else {
            weatherFrame.setTitle(DEFAULT_TITLE);
        }
    }

    /**
     * The dst active bracketed text.
     */
    private static final String DST_ACTIVE = "[DST Active]";

    /**
     * Returns the text for the timezone label. For example, if weatherDataGmtOffset is -18000
     * and DST is active, then the method will return "GMT-5 [DST Active].
     *
     * @return the text for the timezone label
     */
    private String getGmtTimezoneLabelText() {
        String gmtPart = GMT + (Integer.parseInt(weatherDataGmtOffset) / SECONDS_IN_HOUR);
        String dstPart = IpUtil.getIpData().getTime_zone().isIs_dst() ? CyderStrings.space + DST_ACTIVE : "";

        return gmtPart + dstPart;
    }

    /**
     * Returns the hh:mm time after accounting for the GMT offset.
     *
     * @param absoluteTime the absolute hh:mm time
     * @return the hh:mm time after accounting for the GMT offset
     */
    private String accountForGmtOffset(String absoluteTime) {
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
     * The range a minute value must fall within.
     */
    private static final Range<Integer> minuteRange = Range.closed(0, (int) TimeUtil.SECONDS_IN_MINUTE);

    /**
     * Formats the provided minutes to always have two digits.
     *
     * @param minute the minutes value
     * @return the formatted minutes string
     */
    @ForReadability
    private String formatMinutes(int minute) {
        Preconditions.checkArgument(minuteRange.contains(minute));

        if (minute < 10) {
            return "0" + minute;
        } else {
            return String.valueOf(minute);
        }
    }

    /**
     * Refreshes the weather stat variables.
     */
    protected void repullWeatherStats() {
        CyderThreadRunner.submit(() -> {

            userCity = IpUtil.getIpData().getCity();
            userState = IpUtil.getIpData().getRegion();
            userCountry = IpUtil.getIpData().getCountry_name();

            if (!useCustomLoc) currentLocationString = userCity + ", " + userState + ", " + userCountry;

            Optional<WeatherData> optionalWeatherData = WeatherUtil.getWeatherData(currentLocationString);
            if (optionalWeatherData.isEmpty()) {
                weatherFrame.notify("Sorry, but that location is invalid");
                currentLocationString = previousLocationString;
                useCustomLoc = false;
                return;
            }

            WeatherData weatherData = optionalWeatherData.get();
            sunriseMillis = String.valueOf(weatherData.getSys().getSunrise());
            sunsetMillis = String.valueOf(weatherData.getSys().getSunset());
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
            lat = weatherData.getCoord().getLat();
            lon = weatherData.getCoord().getLon();

            refreshMapBackground();

            SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm");
            sunriseFormatted = dateFormatter.format(new Date((long) Integer.parseInt(sunriseMillis) * 1000));

            Date sunsetTime = new Date((long) Integer.parseInt(sunsetMillis) * 1000);
            sunsetFormatted = dateFormatter.format(sunsetTime);

            setGmtIfNotSet();

            String[] currentLocationParts = currentLocationString.split(CyderStrings.comma);
            String currentLocationCityPart = currentLocationParts[0].trim();

            if (!currentLocationCityPart.isEmpty()) {
                String city = StringUtil.capsFirstWords(currentLocationCityPart);
                weatherFrame.setTitle(city + StringUtil.getApostrophe(city) + CyderStrings.space + WEATHER);
            } else {
                weatherFrame.setTitle(DEFAULT_TITLE);
            }

            refreshWeatherLabels();

            Console.INSTANCE.revalidateMenu();
        }, WEATHER_STATS_UPDATER_THREAD_NAME);
    }

    /**
     * The height of the mapbox watermark.
     */
    private static final int MAP_BOX_WATERMARK_HEIGHT = 25;

    /**
     * Refreshes the map background of the weather frame. If not enabled, hides the map.
     * If enabled, shows the map.
     */
    public void refreshMapBackground() {
        try {
            boolean displayMap = UserUtil.getCyderUser().getWeatherMap().equals("1");

            if (displayMap) {
                int h = FRAME_HEIGHT + MAP_BOX_WATERMARK_HEIGHT;
                ImageIcon newMapBackground = MapUtil.getMapView(lat, lon, FRAME_WIDTH, h);
                newMapBackground = ImageUtil.cropImage(newMapBackground, 0, 0, FRAME_WIDTH, FRAME_HEIGHT);
                weatherFrame.setBackground(newMapBackground);
            } else {
                weatherFrame.setBackground(defaultBackground);
            }

            refreshReadableLabels(displayMap);
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
            weatherFrame.notify("Could not refresh map background");
        }
    }

    /**
     * Refreshes the labels with raw text on them based on the current visibility of the background map.
     *
     * @param mapVisible whether the map is visible
     */
    @ForReadability
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
     * Calculates the timezone offset from GMT0/Zulu time if not yet performed.
     */
    @ForReadability
    private void setGmtIfNotSet() {
        if (!isGmtSet) {
            parsedGmtOffset = Integer.parseInt(weatherDataGmtOffset);
            isGmtSet = true;
        }
    }

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
     * Returns the wind direction String.
     *
     * @param bearing the bearing of the wind vector
     * @return the wind direction string
     */
    public String getWindDirection(String bearing) {
        return getWindDirection(Double.parseDouble(bearing));
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

        if (inNorthernHemisphere(bearing)) {
            ret.append(NORTH);

            if (bearing > AngleUtil.NINETY_DEGREES) {
                ret.append(WEST);
            } else if (bearing < AngleUtil.NINETY_DEGREES) {
                ret.append(EAST);
            }
        } else if (inSouthernHemisphere(bearing)) {
            ret.append(SOUTH);

            if (bearing < AngleUtil.TWO_SEVENTY_DEGREES) {
                ret.append(WEST);
            } else if (bearing > AngleUtil.TWO_SEVENTY_DEGREES) {
                ret.append(EAST);
            }
        } else if (isEast(bearing)) {
            ret.append(EAST);
        } else if (isWest(bearing)) {
            ret.append(WEST);
        }

        return ret.toString();
    }

    @ForReadability
    private static boolean inNorthernHemisphere(double bearing) {
        return bearing > 0.0 && bearing < AngleUtil.ONE_EIGHTY_DEGREES;
    }

    @ForReadability
    private static boolean inSouthernHemisphere(double bearing) {
        return bearing > AngleUtil.ONE_EIGHTY_DEGREES && bearing < AngleUtil.THREE_SIXTY_DEGREES;
    }

    @ForReadability
    private static boolean isEast(double bearing) {
        return bearing == 0.0;
    }

    @ForReadability
    private static boolean isWest(double bearing) {
        return bearing == AngleUtil.ONE_EIGHTY_DEGREES;
    }

    /**
     * The max length of the string returned by {@link WeatherWidget#formatFloatMeasurement(float)}.
     */
    private static final int MAX_FLOAT_MEASUREMENT_LENGTH = 5;

    /**
     * Returns a formatted string no greater than {@link WeatherWidget#MAX_FLOAT_MEASUREMENT_LENGTH}.
     *
     * @param measurement the float measurement to format
     * @return the formatted measurement
     */
    private static String formatFloatMeasurement(float measurement) {
        String string = String.valueOf(measurement);
        return string.substring(0, string.length() > MAX_FLOAT_MEASUREMENT_LENGTH - 1
                ? MAX_FLOAT_MEASUREMENT_LENGTH : string.length());
    }
}
