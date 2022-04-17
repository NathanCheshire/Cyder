package cyder.widgets;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderUrls;
import cyder.enums.Direction;
import cyder.enums.LoggerTag;
import cyder.enums.NotificationDirection;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderFrame;
import cyder.ui.objects.NotificationBuilder;
import cyder.utilities.*;
import cyder.utilities.objects.GetterBuilder;
import cyder.widgets.objects.WeatherData;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Vanilla
public class WeatherWidget {
    private JLabel locationLabel;
    private JLabel currentWeatherLabel;
    private JLabel changeLocationLabel;
    private JLabel temperatureLabel;
    private JLabel windSpeedLabel;
    private JLabel windDirectionLabel;
    private JLabel humidityLabel;
    private JLabel pressureLabel;
    private JLabel sunsetLabel;
    private JLabel sunriseLabel;
    private JLabel timezoneLabel;
    private JLabel currentTimeLabel;
    private JLabel currentWeatherIconLabel;
    private JLabel currentTemperatureLabel;

    private String sunrise = "0";
    private String sunset = "0";
    private String weatherIcon = "01d.png";
    private String weatherCondition = "";
    private String windSpeed = "0";
    private String visibility = "0";
    private String temperature = "0";
    private String humidity = "0";
    private String pressure = "0";
    private String feelsLike = "0";
    private String windBearing = "0";

    private String locationString = "";
    private String oldLocation = "";

    private String userCity = "";
    private String userState = "";
    private String userCountry = "";
    private String gmtOffset = "0";

    private JLabel minTempLabel;
    private JLabel maxTempLabel;

    private JButton closeWeather;
    private JButton minimizeWeather;

    private CyderFrame weatherFrame;

    private boolean useCustomLoc;
    private boolean update;

    private double minTemp;
    private double maxTemp;

    private int currentLocationGMTOffset;
    private boolean GMTset;

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
        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    //show gui method as per standard
    @Widget(triggers = "weather", description = "A widget that displays weather data for the current " +
            "city you are in. The location is also changeable")
    public static void showGUI() {
        getInstance().innerShowGUI();
    }

    /**
     * Shows the UI since we need to allow multiple instances of weather widget
     * while still having the public static showGUI() method with the @Widget annotation.
     */
    private void innerShowGUI() {
        if (NetworkUtil.isHighLatency()) {
            ConsoleFrame.INSTANCE.getConsoleCyderFrame().notify("Sorry, "
                    + UserUtil.getCyderUser().getName() + ", but"
                    + " this feature is suspended until a stable internet connection can be established");
            return;
        } else if(StringUtil.isNull(UserUtil.getCyderUser().getWeatherkey())) {
            ConsoleFrame.INSTANCE.getConsoleCyderFrame().inform("Sorry, but the Weather Key has "
                    + "not been set or is invalid, as a result, many features of Cyder will not work as"
                    + " intended. Please see the fields panel of the user editor to learn how to acquire "
                    + "a key and set it.","Weather Key Not Set");
            return;
        }

        repullWeatherStats();

        if (weatherFrame != null)
            weatherFrame.dispose();

        weatherFrame = new CyderFrame(480,640, new ImageIcon(
                ImageUtil.getImageGradient(480, 640,
                        new Color(205,119,130),
                        new Color(38,21,75),
                        new Color(89,85,161)))) {
            @Override
            public void dispose() {
                update = false;
                super.dispose();
            }
        };
        weatherFrame.setTitle("Weather");

        currentTimeLabel = new JLabel(getWeatherTime(), SwingConstants.CENTER);
        currentTimeLabel.setForeground(CyderColors.vanila);
        currentTimeLabel.setFont(CyderFonts.segoe20);
        currentTimeLabel.setBounds(0, 50, 480, 30);
        weatherFrame.getContentPane().add(currentTimeLabel);

        locationLabel = new JLabel(locationString, SwingConstants.CENTER);
        locationLabel.setForeground(CyderColors.vanila);
        locationLabel.setFont(CyderFonts.segoe20);
        locationLabel.setBounds(0, 85, 480, 30);
        weatherFrame.getContentPane().add(locationLabel);

        currentWeatherIconLabel = new JLabel(new ImageIcon("static/pictures/weather/" + weatherIcon + ".png")) {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g.setColor(CyderColors.navy);
                ((Graphics2D) g).setStroke(new BasicStroke(3));
                g2d.drawRoundRect(10, 10, 100, 100, 25, 25);
                super.paint(g);
            }
        };
        currentWeatherIconLabel.setBounds(180, 120, 120, 120);
        weatherFrame.getContentPane().add(currentWeatherIconLabel);

        sunriseLabel = new JLabel(sunrise + "am", SwingConstants.CENTER);
        sunriseLabel.setForeground(CyderColors.vanila);
        sunriseLabel.setFont(CyderFonts.segoe20);
        sunriseLabel.setBounds(0, 200, 480 / 2 - 50, 30);
        weatherFrame.getContentPane().add(sunriseLabel);

        sunsetLabel = new JLabel(sunset + "pm", SwingConstants.CENTER);
        sunsetLabel.setForeground(CyderColors.vanila);
        sunsetLabel.setFont(CyderFonts.segoe20);
        sunsetLabel.setBounds(290, 200, 480 - 290, 30);
        weatherFrame.getContentPane().add(sunsetLabel);

        JLabel sunriseLabelIcon = new JLabel(new ImageIcon("static/pictures/weather/sunrise.png"));
        sunriseLabelIcon.setBounds(60, 145, 55, 48);
        weatherFrame.getContentPane().add(sunriseLabelIcon);

        JLabel sunsetLabelIcon = new JLabel(new ImageIcon("static/pictures/weather/sunset.png"));
        sunsetLabelIcon.setBounds(480 - 55 - 60, 145, 55, 48);
        weatherFrame.getContentPane().add(sunsetLabelIcon);

        currentWeatherLabel = new JLabel("", SwingConstants.CENTER);
        currentWeatherLabel.setForeground(CyderColors.vanila);
        currentWeatherLabel.setFont(CyderFonts.segoe20);
        currentWeatherLabel.setBounds(0, 245, 480, 30);
        weatherFrame.getContentPane().add(currentWeatherLabel);

        weatherFrame.setMenuEnabled(true);
        weatherFrame.addMenuItem("Location", () -> {
            GetterBuilder builder = new GetterBuilder("Change Location");
            builder.setRelativeTo(weatherFrame);
            builder.setSubmitButtonText("Change Location");
            builder.setInitialString(locationString);
            builder.setSubmitButtonColor(CyderColors.notificationForegroundColor);
            builder.setLabelText("<html>Enter your city, state, and country code separated by a comma. "
                    + "Example:<p style=\"color:rgb(45, 100, 220)\">New Orleans, LA, US</p></html>");

            CyderThreadRunner.submit(() -> {
                String newLocation = GetterUtil.getInstance().getString(builder);

                try {
                    if (StringUtil.isNull(newLocation))
                        return;

                    oldLocation = locationString;
                    String[] parts = newLocation.split(",");

                    StringBuilder sb = new StringBuilder();

                    for (int i = 0 ; i < parts.length ; i++) {
                        sb.append(StringUtil.capsFirstWords(parts[i].trim()).trim());

                        if (i != parts.length - 1)
                            sb.append(",");
                    }

                    locationString = sb.toString();
                    useCustomLoc = true;

                    weatherFrame.notify("Attempting to refresh weather stats for location \""
                            + locationString + "\"");

                    repullWeatherStats();
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                }
            }, "Weather Location Changer");
        });

        minTempLabel = new JLabel("");
        minTempLabel.setForeground(CyderColors.vanila);
        minTempLabel.setFont(CyderFonts.defaultFontSmall);
        weatherFrame.getContentPane().add(minTempLabel);

        maxTempLabel = new JLabel("");
        maxTempLabel.setForeground(CyderColors.vanila);
        maxTempLabel.setFont(CyderFonts.defaultFontSmall);

        temperatureLabel = new JLabel() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(CyderColors.navy);
                g.fillRect(3,3,400 - 6,40 - 6);

                try {
                    // map temp val in width range
                    double tempVal = map(Double.parseDouble(temperature),
                            minTemp, maxTemp, 0 , 400);

                    // draw current temp line
                    g.setColor(CyderColors.regularPink);
                    int line = (int) Math.round(tempVal);
                    g.fillRect(line + 3, 3, 6, 34);

                    // set min temp label
                    String minText = minTemp  + "F";
                    minTempLabel.setText(minText);
                    minTempLabel.setSize(
                            StringUtil.getMinWidth(minText, minTempLabel.getFont()),
                            StringUtil.getMinHeight(minText, minTempLabel.getFont()));
                    minTempLabel.setLocation(10, (40 - minTempLabel.getHeight()) / 2);
                    temperatureLabel.add(minTempLabel);

                    // set max temp label
                    String maxText = maxTemp  + "F";
                    maxTempLabel.setText(maxText);
                    maxTempLabel.setSize(
                            StringUtil.getMinWidth(maxText, minTempLabel.getFont()),
                            StringUtil.getMinHeight(maxText, minTempLabel.getFont()));
                    maxTempLabel.setLocation(400 - maxTempLabel.getWidth(),
                            (40 - maxTempLabel.getHeight()) / 2);
                    temperatureLabel.add(maxTempLabel);

                } catch (Exception e) {
                    ExceptionHandler.silentHandle(e);
                }

                // border last, 3px
                g.setColor(Color.black);
                // left
                g.fillRect(0,0,3,40);
                // right
                g.fillRect(400 - 3,0,3,40);
                // top
                g.fillRect(0, 0, 400, 3);
                // bottom
                g.fillRect(0, 40 - 3, 400, 3);
            }
        };
        temperatureLabel.setBounds(40, 320, 400, 40);
        weatherFrame.getContentPane().add(temperatureLabel);

        currentTemperatureLabel = new JLabel();
        weatherFrame.getContentPane().add(currentTemperatureLabel);
        currentTemperatureLabel.setFont(CyderFonts.defaultFontSmall);
        currentTemperatureLabel.setForeground(CyderColors.vanila);

        windSpeedLabel = new JLabel("", SwingConstants.CENTER);
        windSpeedLabel.setText("Wind: " + windSpeed + "mph, " + windBearing + "deg (" + getWindDirection(windBearing) + ")");
        windSpeedLabel.setForeground(CyderColors.vanila);
        windSpeedLabel.setFont(CyderFonts.segoe20);
        windSpeedLabel.setBounds(0, 390, 480, 30);
        weatherFrame.getContentPane().add(windSpeedLabel);

        windDirectionLabel = new JLabel() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(Color.black);
                g.fillRect(0,0, 50, 50);

                g.setColor(CyderColors.navy);
                g.fillRect(3, 3, getWidth() - 6, getHeight() - 6);

                int radius = 20;
                double theta = Double.parseDouble(windBearing) * Math.PI / 180.0;
                double x = radius * Math.cos(theta);
                double y = radius * Math.sin(theta);

                int drawToX = (int) Math.round(x);
                int drawToY = - (int) Math.round(y);

                ((Graphics2D) g).setStroke(new BasicStroke(3));
                g.setColor(CyderColors.regularPink);
                g.drawLine(getWidth() / 2, getHeight() / 2,
                        getWidth() / 2 + drawToX,  getWidth() / 2 + drawToY);
            }
        };
        windDirectionLabel.setBounds(weatherFrame.getWidth() / 2 - 50 / 2, 430, 50, 50);
        weatherFrame.getContentPane().add(windDirectionLabel);

        humidityLabel = new JLabel("Humidity: " + humidity + "%", SwingConstants.CENTER);
        humidityLabel.setForeground(CyderColors.vanila);
        humidityLabel.setFont(CyderFonts.segoe20);
        humidityLabel.setBounds(0, 500, 480, 30);
        weatherFrame.getContentPane().add(humidityLabel);

        pressureLabel = new JLabel("Pressure: " + Double.parseDouble(pressure) / 1000 + "atm",
                SwingConstants.CENTER);
        pressureLabel.setForeground(CyderColors.vanila);
        pressureLabel.setFont(CyderFonts.segoe20);
        pressureLabel.setBounds(0, 540, 480, 30);
        weatherFrame.getContentPane().add(pressureLabel);

        timezoneLabel = new JLabel("Timezone: " + getTimezoneLabel(), SwingConstants.CENTER);
        timezoneLabel.setForeground(CyderColors.vanila);
        timezoneLabel.setFont(CyderFonts.segoe20);
        timezoneLabel.setBounds(0, 580, 480, 30);
        weatherFrame.getContentPane().add(timezoneLabel);

        weatherFrame.finalizeAndShow();

        update = true;

        CyderThreadRunner.submit(() -> {
            try {
                EXIT:
                    for(;;) {
                        for (int i = 0 ; i < 60 * 5 ; i++) {
                            if (!update)
                                break EXIT;
                            Thread.sleep(60 * 5);
                        }
                        repullWeatherStats();
                    }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        },"Weather Stats Updater");

        CyderThreadRunner.submit(() -> {
            try {
                for (;;) {
                    if (!update)
                        break;
                    Thread.sleep(1000);
                    currentTimeLabel.setText(getWeatherTime());
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        },"Weather Clock Updater");
    }

    /**
     * Maps the value in from one range to the next.
     *
     * @param value the value to map
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
        Date Time = cal.getTime();
        SimpleDateFormat dateFormatter = TimeUtil.weatherFormat;
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            int timeOffset = Integer.parseInt(gmtOffset) / 3600;
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
            if (locationString.length() > 1) {
                String[] parts = locationString.split(",");
                StringBuilder sb = new StringBuilder();

                for (int i = 0 ; i < parts.length ; i++) {
                    sb.append(StringUtil.capsFirstWords(parts[i].trim()).trim());

                    if (i != parts.length - 1)
                        sb.append(", ");
                }

                locationLabel.setText(sb.toString());
            } else {
                locationLabel.setText("");
            }

            currentWeatherIconLabel.setIcon(new ImageIcon("static/pictures/weather/" + weatherIcon + ".png"));
            currentWeatherLabel.setText(StringUtil.capsFirstWords(weatherCondition));
            windSpeedLabel.setText("Wind: " + windSpeed + "mph, " + windBearing + "deg (" + getWindDirection(windBearing) + ")");
            humidityLabel.setText("Humidity: " + humidity + "%");
            pressureLabel.setText("Pressure: " + Double.parseDouble(pressure) / 1000 + "atm");
            timezoneLabel.setText("Timezone: " + getTimezoneLabel());
            sunriseLabel.setText(correctedSunTime(sunrise) + "am");
            sunsetLabel.setText(correctedSunTime(sunset) + "pm");

            // repaint custom temperature drawing
            temperatureLabel.repaint();

            int temperatureLineCenter = (int) Math.ceil(temperatureLabel.getX() + map(Double.parseDouble(temperature),
                                minTemp, maxTemp, 0 , 400)) + 5;

            currentTemperatureLabel.setText(temperature + "F");

            int width = StringUtil.getMinWidth(currentTemperatureLabel.getText(), currentTemperatureLabel.getFont());
            int height = StringUtil.getMinHeight(currentTemperatureLabel.getText(), currentTemperatureLabel.getFont());

            int minX = temperatureLabel.getX();
            int maxX = temperatureLabel.getX() + temperatureLabel.getWidth() - width;

            int desiredX = temperatureLineCenter - (width) / 2;

            if (desiredX < minX) {
                desiredX = minX;
            }

            if (desiredX > maxX) {
                desiredX = maxX;
            }

            currentTemperatureLabel.setBounds(desiredX, temperatureLabel.getY() - 3 - height, width, height);

            //redraw arrow
            windDirectionLabel.repaint();

            String[] parts = locationString.split(",");

            //frame title
            if (!parts[0].trim().isEmpty()) {
                String city = StringUtil.capsFirstWords(parts[0].trim()).trim();
                weatherFrame.setTitle(city + StringUtil.getApostrophe(city) + " weather");
            } else {
                weatherFrame.setTitle("Weather");
            }

            if (weatherFrame != null) {
                NotificationBuilder builder = new NotificationBuilder("Refreshed");
                builder.setViewDuration(2000);
                builder.setNotificationDirection(NotificationDirection.BOTTOM_LEFT);
                builder.setArrowDir(Direction.LEFT);
                weatherFrame.notify(builder);
            }
        }

        catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns the text for the timezone label.
     *
     * @return the text for the timezone label
     */
    private String getTimezoneLabel() {
        return "GMT" + (Integer.parseInt(gmtOffset) / 3600)
                + (IPUtil.getIpdata().getTime_zone().isIs_dst() ? " [DST Active]" : "");
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

        hour += (Integer.parseInt(gmtOffset) / 3600 - (currentLocationGMTOffset / 60 / 60));

        // hour, colon, 01,...,09,10,..., 59,01
        return hour + ":" + (minute < 10 ? "0" + minute : minute);
    }

    // todo surely the temperature range is for this hour? make it for the day
    // todo a lot of methods here don't make sense, optimize weather widget

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
                userCity = IPUtil.getIpdata().getCity();
                userState = IPUtil.getIpdata().getRegion();
                userCountry = IPUtil.getIpdata().getCountry_name();

                if (!useCustomLoc)
                    locationString = userCity + ", " + userState + ", " + userCountry;

                String key = UserUtil.getCyderUser().getWeatherkey();

                String OpenString = CyderUrls.OPEN_WEATHER_BASE +
                        locationString + "&appid=" + key + "&units=imperial";

                WeatherData wd;

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(OpenString).openStream()))) {
                    wd = gson.fromJson(reader, WeatherData.class);

                    sunrise = String.valueOf(wd.getSys().getSunrise());
                    sunset = String.valueOf(wd.getSys().getSunset());
                    weatherIcon = wd.getWeather().get(0).getIcon();
                    windSpeed = String.valueOf(wd.getWind().getSpeed());
                    windBearing = String.valueOf(wd.getWind().getDeg());
                    weatherCondition = wd.getWeather().get(0).getDescription();
                    visibility = String.valueOf(wd.getVisibility());
                    feelsLike = String.valueOf(wd.getMain().getFeels_like());
                    pressure = String.valueOf(wd.getMain().getPressure()).substring(0, Math.min(pressure.length(), 4));
                    humidity = String.valueOf(wd.getMain().getHumidity());
                    temperature = String.valueOf(wd.getMain().getTemp());
                    gmtOffset = String.valueOf(wd.getTimezone());

                    minTemp = wd.getMain().getTemp_min();
                    maxTemp = wd.getMain().getTemp_max();

                    SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm");

                    sunrise = dateFormatter.format(new Date((long) Integer.parseInt(sunrise) * 1000));

                    Date SunsetTime = new Date((long) Integer.parseInt(sunset) * 1000);
                    sunset = dateFormatter.format(SunsetTime);

                    //calculate the offset from GMT + 0/Zulu time
                    if (!GMTset) {
                        currentLocationGMTOffset = Integer.parseInt(gmtOffset);
                        GMTset = true;
                    }

                    //check for night/day icon
                    if (new Date().getTime() > SunsetTime.getTime()) {
                        weatherIcon = weatherIcon.replace("d", "n");
                    }

                    String[] parts = locationString.split(",");

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
                locationString = oldLocation;
                useCustomLoc = false;
                ExceptionHandler.silentHandle(e);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            } finally {
                refreshWeatherLabels();
            }
        },"Weather Stats Updater");
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
     * @return the wind directionstring based off of the current wind bearing.
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
               } else if (bearing < 90.0){
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
}
