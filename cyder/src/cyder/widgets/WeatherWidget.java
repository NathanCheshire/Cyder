package cyder.widgets;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import cyder.annotations.CyderAuthor;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.console.ConsoleFrame;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderUrls;
import cyder.enums.Direction;
import cyder.enums.NotificationDirection;
import cyder.genesis.PropLoader;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderFrame;
import cyder.utils.*;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private String sunrise = "0";

    /**
     * The sunset time in unix time format.
     */
    private String sunset = "0";

    /**
     * The current weather icon resource.
     */
    private String weatherIcon = "01d.png";

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
    private final AtomicBoolean stopUpdating = new AtomicBoolean(true);

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
    private boolean gmtSet;

    /**
     * Returns a new instance of weather widget.
     *
     * @return a new instance of weather widget
     */
    public static WeatherWidget getInstance() {
        return new WeatherWidget();
    }

    /**
     * Creates a new weather widget initialized to the user's current location.
     */
    private WeatherWidget() {
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    //show gui method as per standard
    @Widget(triggers = "weather", description = "A widget that displays weather data for the current " +
            "city you are in. The location is also changeable")
    public static void showGui() {
        getInstance().innerShowGui();
    }

    /**
     * Shows the UI since we need to allow multiple instances of weather widget
     * while still having the public static showGui() method with the @Widget annotation.
     */
    private void innerShowGui() {
        if (NetworkUtil.isHighLatency()) {
            ConsoleFrame.INSTANCE.getConsoleCyderFrame().notify("Sorry, "
                    + UserUtil.getCyderUser().getName() + ", but"
                    + " this feature is suspended until a stable internet connection can be established");
            return;
        } else if (StringUtil.isNull(PropLoader.getString("weather_key"))) {
            ConsoleFrame.INSTANCE.getConsoleCyderFrame().inform("Sorry, but the Weather Key has "
                    + "not been set or is invalid, as a result, many features of Cyder will not work as"
                    + " intended. Please see the fields panel of the user editor to learn how to acquire "
                    + "a key and set it.", "Weather Key Not Set");
            return;
        }

        repullWeatherStats();

        if (weatherFrame != null)
            weatherFrame.dispose();

        weatherFrame = new CyderFrame(480, 640) {
            @Override
            public void dispose() {
                stopUpdating.set(true);
                super.dispose();
            }
        };
        weatherFrame.setTitle("Weather");

        currentTimeLabel = new JLabel(getWeatherTime(), SwingConstants.CENTER);
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
            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g.setColor(CyderColors.navy);
                ((Graphics2D) g).setStroke(new BasicStroke(3));
                g2d.fillRoundRect(10, 10, 100, 160, 25, 25);
                super.paint(g);
            }
        };
        currentWeatherContainer.setBounds(180, 120, 120, 180);
        weatherFrame.getContentPane().add(currentWeatherContainer);

        currentWeatherIconLabel = new JLabel(new ImageIcon("static/pictures/weather/" + weatherIcon + ".png"));
        currentWeatherIconLabel.setBounds(0, 25, currentWeatherContainer.getWidth(),
                currentWeatherContainer.getHeight() / 2);
        currentWeatherContainer.add(currentWeatherIconLabel);

        currentWeatherLabel = new JLabel("", SwingConstants.CENTER);
        currentWeatherLabel.setForeground(CyderColors.vanilla);
        currentWeatherLabel.setFont(CyderFonts.SEGOE_20.deriveFont(18f));
        currentWeatherLabel.setBounds(0, currentWeatherContainer.getHeight() / 2, currentWeatherContainer.getWidth(),
                currentWeatherContainer.getHeight() / 2);
        currentWeatherContainer.add(currentWeatherLabel);

        JLabel sunriseLabelIcon = new JLabel(new ImageIcon("static/pictures/weather/sunrise.png")) {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g.setColor(CyderColors.navy);
                ((Graphics2D) g).setStroke(new BasicStroke(3));
                g2d.fillRoundRect(10, 10, 100, 160, 25, 25);
                super.paint(g);
            }
        };
        sunriseLabelIcon.setBounds(60, 120, 120, 180);
        weatherFrame.getContentPane().add(sunriseLabelIcon);

        sunriseLabel = new JLabel(sunrise + "am", SwingConstants.CENTER);
        sunriseLabel.setForeground(CyderColors.vanilla);
        sunriseLabel.setFont(CyderFonts.SEGOE_20);
        sunriseLabel.setBounds(0, sunriseLabelIcon.getHeight() / 2, sunriseLabelIcon.getWidth(),
                sunriseLabelIcon.getHeight() / 2);
        sunriseLabelIcon.add(sunriseLabel);

        JLabel sunsetLabelIcon = new JLabel(new ImageIcon("static/pictures/weather/sunset.png")) {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g.setColor(CyderColors.navy);
                ((Graphics2D) g).setStroke(new BasicStroke(3));
                g2d.fillRoundRect(10, 10, 100, 160, 25, 25);
                super.paint(g);
            }
        };
        sunsetLabelIcon.setBounds(480 - 60 - 120, 120, 120, 180);
        weatherFrame.getContentPane().add(sunsetLabelIcon);

        sunsetLabel = new JLabel(sunset + "pm", SwingConstants.CENTER);
        sunsetLabel.setForeground(CyderColors.vanilla);
        sunsetLabel.setFont(CyderFonts.SEGOE_20);
        sunsetLabel.setBounds(0, sunsetLabelIcon.getHeight() / 2, sunsetLabelIcon.getWidth(),
                sunsetLabelIcon.getHeight() / 2);
        sunsetLabelIcon.add(sunsetLabel);

        weatherFrame.setMenuEnabled(true);
        weatherFrame.addMenuItem("Location", () -> CyderThreadRunner.submit(() -> {
            String newLocation = GetterUtil.getInstance().getString(
                    new GetterUtil.Builder("Change Location")
                            .setRelativeTo(weatherFrame)
                            .setSubmitButtonText("Change Location")
                            .setInitialString(currentLocationString)
                            .setSubmitButtonColor(CyderColors.notificationForegroundColor)
                            .setLabelText("<html>Enter your city, state, and country code separated by a comma. "
                                    + "Example:<p style=\"color:rgb(45, 100, 220)\""
                                    + ">New Orleans, LA, US</p></html>"));

            try {
                if (StringUtil.isNull(newLocation))
                    return;

                previousLocationString = currentLocationString;
                String[] parts = newLocation.split(",");

                StringBuilder sb = new StringBuilder();

                for (int i = 0 ; i < parts.length ; i++) {
                    sb.append(StringUtil.capsFirstWords(parts[i].trim()).trim());

                    if (i != parts.length - 1)
                        sb.append(",");
                }

                currentLocationString = sb.toString();
                useCustomLoc = true;

                weatherFrame.notify("Attempting to refresh weather stats for location \""
                        + currentLocationString + "\"");

                repullWeatherStats();
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }, "Weather Location Changer"));

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
                g.setColor(CyderColors.navy);
                g.fillRect(3, 3, 400 - 6, 40 - 6);

                try {
                    // map temp val in width range
                    double tempVal = map(temperature, minTemp, maxTemp, 0, 400);

                    // draw current temp line
                    g.setColor(CyderColors.regularPink);
                    int line = (int) Math.round(tempVal);
                    g.fillRect(line + 3, 3, 6, 34);

                    // set min temp label
                    String minText = formatFloatMeasurement(minTemp) + "F";
                    minTempLabel.setText(minText);
                    minTempLabel.setSize(
                            StringUtil.getMinWidth(minText, minTempLabel.getFont()),
                            StringUtil.getMinHeight(minText, minTempLabel.getFont()));
                    minTempLabel.setLocation(10, (40 - minTempLabel.getHeight()) / 2);
                    customTempLabel.add(minTempLabel);

                    // current temp
                    String currentTemp = formatFloatMeasurement(temperature) + "F";
                    currentTempLabel.setText(currentTemp);
                    currentTempLabel.setSize(
                            StringUtil.getMinWidth(currentTemp, currentTempLabel.getFont()),
                            StringUtil.getMinHeight(currentTemp, currentTempLabel.getFont()));
                    currentTempLabel.setLocation(customTempLabel.getWidth() / 2 - currentTempLabel.getWidth() / 2,
                            customTempLabel.getHeight() / 2 - currentTempLabel.getHeight() / 2);
                    customTempLabel.add(currentTempLabel);

                    // set max temp label
                    String maxText = formatFloatMeasurement(maxTemp) + "F";
                    maxTempLabel.setText(maxText);
                    maxTempLabel.setSize(
                            StringUtil.getMinWidth(maxText, minTempLabel.getFont()),
                            StringUtil.getMinHeight(maxText, minTempLabel.getFont()));
                    maxTempLabel.setLocation(400 - maxTempLabel.getWidth(),
                            (40 - maxTempLabel.getHeight()) / 2);
                    customTempLabel.add(maxTempLabel);
                } catch (Exception e) {
                    ExceptionHandler.silentHandle(e);
                }

                // border last, 3px
                g.setColor(Color.black);
                // left
                g.fillRect(0, 0, 3, 40);
                // right
                g.fillRect(400 - 3, 0, 3, 40);
                // top
                g.fillRect(0, 0, 400, 3);
                // bottom
                g.fillRect(0, 40 - 3, 400, 3);
            }
        };
        customTempLabel.setBounds(40, 320, 400, 40);
        weatherFrame.getContentPane().add(customTempLabel);

        windSpeedLabel = new JLabel("", SwingConstants.CENTER);
        windSpeedLabel.setText(
                "Wind: " + windSpeed + "mph, " + windBearing + "deg (" + getWindDirection(windBearing) + ")");
        windSpeedLabel.setForeground(CyderColors.navy);
        windSpeedLabel.setFont(CyderFonts.SEGOE_20);
        windSpeedLabel.setBounds(0, 390, 480, 30);
        weatherFrame.getContentPane().add(windSpeedLabel);

        windDirectionLabel = new JLabel() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(Color.black);
                g.fillRect(0, 0, 50, 50);

                g.setColor(CyderColors.navy);
                g.fillRect(3, 3, getWidth() - 6, getHeight() - 6);

                int radius = 20;
                double theta = windBearing * Math.PI / 180.0;
                double x = radius * Math.cos(theta);
                double y = radius * Math.sin(theta);

                int drawToX = (int) Math.round(x);
                int drawToY = -(int) Math.round(y);

                ((Graphics2D) g).setStroke(new BasicStroke(3));
                g.setColor(CyderColors.regularPink);
                g.drawLine(getWidth() / 2, getHeight() / 2,
                        getWidth() / 2 + drawToX, getWidth() / 2 + drawToY);
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

        timezoneLabel = new JLabel("Timezone: " + getTimezoneLabel(), SwingConstants.CENTER);
        timezoneLabel.setForeground(CyderColors.navy);
        timezoneLabel.setFont(CyderFonts.SEGOE_20);
        timezoneLabel.setBounds(0, 580, 480, 30);
        weatherFrame.getContentPane().add(timezoneLabel);

        weatherFrame.finalizeAndShow();

        stopUpdating.set(false);

        CyderThreadRunner.submit(() -> {
            try {
                while (true) {
                    TimeUtil.sleepWithChecks(
                            1000 * 60 * updateFrequency, 10000, stopUpdating);
                    if (stopUpdating.get()) {
                        break;
                    }

                    repullWeatherStats();
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Weather Stats Updater");

        CyderThreadRunner.submit(() -> {
            try {
                while (stopUpdating.get()) {
                    Thread.sleep(1000);
                    currentTimeLabel.setText(getWeatherTime());
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Weather Clock Updater");
    }

    /**
     * Maps the value in from one range to the next.
     *
     * @param value       the value to map
     * @param oldRangeMin the old range's min
     * @param oldRangeMax the old range's max
     * @param newRangeMin the new range's min
     * @param newRangeMax the new range's max
     * @return the value mapped from the old range to the new range
     */
    public static double map(double value, double oldRangeMin, double oldRangeMax,
                             double newRangeMin, double newRangeMax) {
        return (value - oldRangeMin) * (newRangeMax - newRangeMin) / (oldRangeMax - oldRangeMin) + newRangeMin;
    }

    /**
     * Returns the current weather time.
     *
     * @return the current weather time
     */
    private String getWeatherTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormatter = TimeUtil.weatherFormat;
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            int timeOffset = Integer.parseInt(weatherDataGmtOffset) / 3600;
            cal.add(Calendar.HOUR, timeOffset);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return dateFormatter.format(cal.getTime());
    }

    /**
     * Refreshes the weather labels based off of the current vars.
     */
    private void refreshWeatherLabels() {
        try {
            if (currentLocationString.length() > 1) {
                String[] parts = currentLocationString.split(",");
                StringBuilder sb = new StringBuilder();

                boolean isLenThree = parts.length == 3;

                for (int i = 0 ; i < parts.length ; i++) {
                    if (isLenThree && i == 1 && parts[i].length() == 2) {
                        sb.append(parts[i].trim().toUpperCase());
                    } else {
                        sb.append(StringUtil.capsFirstWords(parts[i].trim()).trim());
                    }

                    if (i != parts.length - 1)
                        sb.append(", ");
                }

                locationLabel.setText(sb.toString());
            } else {
                locationLabel.setText("");
            }

            currentWeatherIconLabel.setIcon(new ImageIcon("static/pictures/weather/" + weatherIcon + ".png"));
            currentWeatherLabel.setText("<html><div style='text-align: center; vertical-align:bottom'>"
                    + StringUtil.capsFirstWords(weatherCondition).replace("\\s+", "<br/>") + "</html>");
            windSpeedLabel.setText(
                    "Wind: " + windSpeed + "mph, " + windBearing + "deg (" + getWindDirection(windBearing) + ")");
            humidityLabel.setText("Humidity: " + humidity + "%");

            pressureLabel.setText("Pressure: " + formatFloatMeasurement(pressure) + "atm");

            timezoneLabel.setText("Timezone: " + getTimezoneLabel());
            sunriseLabel.setText(correctedSunTime(sunrise) + "am");
            sunsetLabel.setText(correctedSunTime(sunset) + "pm");

            // repaint custom temperature drawing
            customTempLabel.repaint();

            int temperatureLineCenter = (int) Math.ceil(customTempLabel.getX() + map(temperature,
                    minTemp, maxTemp, 0, 400)) + 5;

            currentTempLabel.setText(temperature + "F");

            int width = StringUtil.getMinWidth(currentTempLabel.getText(), currentTempLabel.getFont());
            int height = StringUtil.getMinHeight(currentTempLabel.getText(), currentTempLabel.getFont());

            int minX = customTempLabel.getX();
            int maxX = customTempLabel.getX() + customTempLabel.getWidth() - width;

            int desiredX = temperatureLineCenter - (width) / 2;

            if (desiredX < minX) {
                desiredX = minX;
            }

            if (desiredX > maxX) {
                desiredX = maxX;
            }

            currentTempLabel.setBounds(desiredX, customTempLabel.getY() - 3 - height, width, height);

            //redraw arrow
            windDirectionLabel.repaint();

            String[] parts = currentLocationString.split(",");

            //frame title
            if (!parts[0].trim().isEmpty()) {
                String city = StringUtil.capsFirstWords(parts[0].trim()).trim();
                weatherFrame.setTitle(city + StringUtil.getApostrophe(city) + " weather");
            } else {
                weatherFrame.setTitle("Weather");
            }

            if (weatherFrame != null) {
                weatherFrame.notify(new CyderFrame.NotificationBuilder("Refreshed")
                        .setViewDuration(2000)
                        .setNotificationDirection(NotificationDirection.BOTTOM_LEFT)
                        .setArrowDir(Direction.LEFT));
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns the text for the timezone label.
     *
     * @return the text for the timezone label
     */
    private String getTimezoneLabel() {
        return "GMT" + (Integer.parseInt(weatherDataGmtOffset) / 3600)
                + (IPUtil.getIpData().getTime_zone().isIs_dst() ? " [DST Active]" : "");
    }

    /**
     * Returns the hh:mm time after accounting for the GMT offset.
     *
     * @param absoluteTime the absolute hh:mm time
     * @return the hh:mm time after accounting for the GMT offset
     */
    private String correctedSunTime(String absoluteTime) {
        Preconditions.checkNotNull(absoluteTime);
        Preconditions.checkArgument(absoluteTime.contains(":"));

        String[] parts = absoluteTime.split(":");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Illegal absolute time: " + absoluteTime);
        }

        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        hour += (Integer.parseInt(weatherDataGmtOffset) / 3600 - (parsedGmtOffset / 60 / 60));

        // hour, colon, 01,...,09,10,..., 59,01
        return hour + ":" + (minute < 10 ? "0" + minute : minute);
    }

    /**
     * The gson object to use for deserializing json data.
     */
    private static final Gson gson = new Gson();

    /**
     * Refreshes the weather stat variables.
     */
    protected void repullWeatherStats() {
        CyderThreadRunner.submit(() -> {
            try {
                userCity = IPUtil.getIpData().getCity();
                userState = IPUtil.getIpData().getRegion();
                userCountry = IPUtil.getIpData().getCountry_name();

                if (!useCustomLoc)
                    currentLocationString = userCity + ", " + userState + ", " + userCountry;

                String key = PropLoader.getString("weather_key");

                String OpenString = CyderUrls.OPEN_WEATHER_BASE +
                        currentLocationString + "&appid=" + key + "&units=imperial";

                WeatherData wd;

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new URL(OpenString).openStream()))) {
                    wd = gson.fromJson(reader, WeatherData.class);

                    sunrise = String.valueOf(wd.getSys().getSunrise());
                    sunset = String.valueOf(wd.getSys().getSunset());
                    weatherIcon = wd.getWeather().get(0).getIcon();
                    windSpeed = wd.getWind().getSpeed();
                    windBearing = wd.getWind().getDeg();
                    weatherCondition = wd.getWeather().get(0).getDescription();
                    pressure = wd.getMain().getPressure();
                    humidity = wd.getMain().getHumidity();
                    temperature = wd.getMain().getTemp();
                    weatherDataGmtOffset = String.valueOf(wd.getTimezone());

                    minTemp = wd.getMain().getTemp_min();
                    maxTemp = wd.getMain().getTemp_max();

                    lat = wd.getCoord().getLat();
                    lon = wd.getCoord().getLon();

                    try {
                        ImageIcon newBack = MapUtil.getMapView(lat, lon,
                                weatherFrame.getWidth(), weatherFrame.getHeight());
                        weatherFrame.setBackground(newBack);
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }

                    SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm");

                    sunrise = dateFormatter.format(new Date((long) Integer.parseInt(sunrise) * 1000));

                    Date SunsetTime = new Date((long) Integer.parseInt(sunset) * 1000);
                    sunset = dateFormatter.format(SunsetTime);

                    //calculate the offset from GMT + 0/Zulu time
                    if (!gmtSet) {
                        parsedGmtOffset = Integer.parseInt(weatherDataGmtOffset);
                        gmtSet = true;
                    }

                    //check for night/day icon
                    if (new Date().getTime() > SunsetTime.getTime()) {
                        weatherIcon = weatherIcon.replace("d", "n");
                    }

                    String[] parts = currentLocationString.split(",");

                    if (!parts[0].trim().isEmpty()) {
                        String city = StringUtil.capsFirstWords(parts[0].trim()).trim();
                        weatherFrame.setTitle(city + StringUtil.getApostrophe(city) + " weather");
                    } else {
                        weatherFrame.setTitle("Weather");
                    }

                    // needed to change the title on the menu
                    ConsoleFrame.INSTANCE.revalidateMenu();
                }
            } catch (FileNotFoundException e) {
                //invalid custom location so go back to the old one
                weatherFrame.notify("Sorry, but that location is invalid");
                currentLocationString = previousLocationString;
                useCustomLoc = false;
                ExceptionHandler.silentHandle(e);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            } finally {
                refreshWeatherLabels();
            }
        }, "Weather Stats Updater");
    }

    /**
     * Returns the wind direction String.
     *
     * @param bearing the bearing of the wind vector
     * @return the wind direction string
     */
    public static String getWindDirection(String bearing) {
        return getWindDirection(Double.parseDouble(bearing));
    }

    /**
     * Returns the wind direction string based off of the current wind bearing.
     *
     * @param bearing the current wind bearing
     * @return the wind direction string based off of the current wind bearing.
     */
    public static String getWindDirection(double bearing) {
        while (bearing > 360.0)
            bearing -= 360.0;
        while (bearing < 0.0)
            bearing += 360.0;

        String ret = "";

        // northern hemisphere
        if (bearing >= 0.0 && bearing <= 180.0) {
            if (bearing == 0.0)
                ret = "E";
            else if (bearing == 180.0)
                ret = "W";
            else {
                //we now know it's north something
                ret += "N";

                if (bearing > 90.0) {
                    ret += "W";
                } else if (bearing < 90.0) {
                    ret += "E";
                }
            }
        }
        // southern hemisphere excluding directly East and West
        else {
            //already know it must be S appended
            ret = "S";

            //is it east
            if (bearing < 270.0)
                ret += "W";
            if (bearing > 270.0)
                ret += "E";
        }

        return ret;
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

    /**
     * The json schema for weather data.
     */
    public static final class WeatherData {
        private Coord coord;
        private LinkedList<JsonWeather> weather;
        private String base;
        private Main main;
        private int visibility;
        private Wind wind;
        private Clouds clouds;
        private int dt;
        private Sys sys;
        private int timezone;
        private int id;
        private String name;
        private double cod;

        public Coord getCoord() {
            return coord;
        }

        public void setCoord(Coord coord) {
            this.coord = coord;
        }

        public LinkedList<JsonWeather> getWeather() {
            return weather;
        }

        public void setWeather(LinkedList<JsonWeather> weather) {
            this.weather = weather;
        }

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public Main getMain() {
            return main;
        }

        public void setMain(Main main) {
            this.main = main;
        }

        public int getVisibility() {
            return visibility;
        }

        public void setVisibility(int visibility) {
            this.visibility = visibility;
        }

        public Wind getWind() {
            return wind;
        }

        public void setWind(Wind wind) {
            this.wind = wind;
        }

        public Clouds getClouds() {
            return clouds;
        }

        public void setClouds(Clouds clouds) {
            this.clouds = clouds;
        }

        public int getDt() {
            return dt;
        }

        public void setDt(int dt) {
            this.dt = dt;
        }

        public Sys getSys() {
            return sys;
        }

        public void setSys(Sys sys) {
            this.sys = sys;
        }

        public int getTimezone() {
            return timezone;
        }

        public void setTimezone(int timezone) {
            this.timezone = timezone;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getCod() {
            return cod;
        }

        public void setCod(double cod) {
            this.cod = cod;
        }

        public static class Coord {
            private double lon;
            private double lat;

            public double getLon() {
                return lon;
            }

            public void setLon(double lon) {
                this.lon = lon;
            }

            public double getLat() {
                return lat;
            }

            public void setLat(double lat) {
                this.lat = lat;
            }
        }

        public static class JsonWeather {
            private int id;
            private String main;
            private String description;
            private String icon;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getMain() {
                return main;
            }

            public void setMain(String main) {
                this.main = main;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public String getIcon() {
                return icon;
            }

            public void setIcon(String icon) {
                this.icon = icon;
            }
        }

        public static class Main {
            private float temp;
            private float feels_like;
            private float temp_min;
            private float temp_max;
            private float pressure;
            private float humidity;

            public float getTemp() {
                return temp;
            }

            public void setTemp(float temp) {
                this.temp = temp;
            }

            public float getFeels_like() {
                return feels_like;
            }

            public void setFeels_like(float feels_like) {
                this.feels_like = feels_like;
            }

            public float getTemp_min() {
                return temp_min;
            }

            public void setTemp_min(float temp_min) {
                this.temp_min = temp_min;
            }

            public float getTemp_max() {
                return temp_max;
            }

            public void setTemp_max(float temp_max) {
                this.temp_max = temp_max;
            }

            public float getPressure() {
                return pressure;
            }

            public void setPressure(float pressure) {
                this.pressure = pressure;
            }

            public float getHumidity() {
                return humidity;
            }

            public void setHumidity(float humidity) {
                this.humidity = humidity;
            }
        }

        public static class Wind {
            private float speed;
            private int deg;
            private float gust;

            public float getSpeed() {
                return speed;
            }

            public void setSpeed(float speed) {
                this.speed = speed;
            }

            public int getDeg() {
                return deg;
            }

            public void setDeg(int deg) {
                this.deg = deg;
            }

            public float getGust() {
                return gust;
            }

            public void setGust(float gust) {
                this.gust = gust;
            }
        }

        public static class Clouds {
            private double all;

            public double getAll() {
                return all;
            }

            public void setAll(double all) {
                this.all = all;
            }
        }

        public static class Sys {
            private int type;
            private int id;
            private String country;
            private int sunrise;
            private int sunset;

            public void setType(int type) {
                this.type = type;
            }

            public void setId(int id) {
                this.id = id;
            }

            public void setCountry(String country) {
                this.country = country;
            }

            public void setSunrise(int sunrise) {
                this.sunrise = sunrise;
            }

            public void setSunset(int sunset) {
                this.sunset = sunset;
            }

            public int getType() {
                return type;
            }

            public int getId() {
                return id;
            }

            public String getCountry() {
                return country;
            }

            public int getSunrise() {
                return sunrise;
            }

            public int getSunset() {
                return sunset;
            }
        }
    }
}
