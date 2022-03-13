package cyder.widgets;

import com.google.gson.Gson;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.enums.Direction;
import cyder.enums.LoggerTag;
import cyder.enums.NotificationDirection;
import cyder.genesis.CyderShare;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderTextField;
import cyder.ui.objects.NotificationBuilder;
import cyder.utilities.IPUtil;
import cyder.utilities.ImageUtil;
import cyder.utilities.StringUtil;
import cyder.utilities.UserUtil;

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
        if (CyderShare.isHighLatency()) {
            ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().notify("Sorry, "
                    + UserUtil.getCyderUser().getName() + ", but"
                    + " this feature is suspended until a stable internet connection can be established");
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

        currentWeatherLabel = new JLabel(StringUtil.capsFirst(weatherCondition), SwingConstants.CENTER);
        currentWeatherLabel.setForeground(CyderColors.vanila);
        currentWeatherLabel.setFont(CyderFonts.segoe20);
        currentWeatherLabel.setBounds(0, 245, 480, 30);
        weatherFrame.getContentPane().add(currentWeatherLabel);

        weatherFrame.setMenuEnabled(true);
        weatherFrame.addMenuItem("Location", () -> {
            CyderFrame changeLocationFrame = new CyderFrame(600,310);
            changeLocationFrame.setBackground(CyderColors.vanila);
            changeLocationFrame.setTitle("Change Location");

            JLabel explenation = new JLabel("<html><div style='text-align: center;'>Enter your city, state, and country code separated by a comma" +
                    "<br/>Example: <p style=\"font-family:verdana\"><p style=\"color:rgb(45, 100, 220)\">New Orleans, LA, US</p></p></div></html>");

            explenation.setFont(CyderFonts.segoe20);
            explenation.setForeground(CyderColors.navy);
            explenation.setHorizontalAlignment(JLabel.CENTER);
            explenation.setVerticalAlignment(JLabel.CENTER);
            explenation.setBounds(40,40,520,170);
            changeLocationFrame.getContentPane().add(explenation);

            CyderTextField changeLocField = new CyderTextField(0);
            changeLocField.setHorizontalAlignment(JTextField.CENTER);
            changeLocField.setBackground(Color.white);
            changeLocField.setBounds(40,200,520,40);
            changeLocationFrame.getContentPane().add(changeLocField);

            CyderButton changeLoc = new CyderButton("Change Location");
            changeLocField.addActionListener(e1 -> changeLoc.doClick());
            changeLoc.addActionListener(e12 -> {
                try {
                    if (changeLocField.getText().trim().length() < 1)
                        return;

                    oldLocation = locationString;
                    String[] parts = changeLocField.getText().split(",");

                    StringBuilder sb = new StringBuilder();

                    for (int i = 0 ; i < parts.length ; i++) {
                        sb.append(StringUtil.capsFirst(parts[i].trim()).trim());

                        if (i != parts.length - 1)
                            sb.append(",");
                    }

                    locationString = sb.toString();
                    useCustomLoc = true;

                    changeLocationFrame.dispose();
                    weatherFrame.notify("Attempting to refresh weather stats for location \"" + locationString + "\"");

                    repullWeatherStats();
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                }
            });

            changeLoc.setBounds(40,250,520,40);
            changeLocationFrame.getContentPane().add(changeLoc);

            changeLocationFrame.setVisible(true);
            changeLocationFrame.setLocationRelativeTo(weatherFrame);
        });

        JLabel minTempLabel = new JLabel("");
        minTempLabel.setForeground(CyderColors.vanila);
        minTempLabel.setFont(CyderFonts.defaultFontSmall);
        weatherFrame.getContentPane().add(minTempLabel);

        JLabel maxTempLabel = new JLabel("");
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

        weatherFrame.setVisible(true);
        weatherFrame.setLocationRelativeTo(CyderShare.getDominantFrame());

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
                //noinspection ConditionalBreakInInfiniteLoop
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

    public static double map(double x, double in_min, double in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    public String getWeatherTime() {
        Calendar cal = Calendar.getInstance();
        Date Time = cal.getTime();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm:ssaa EEEEEEEEEEEEE, MMMMMMMMMMMMMMMMMM dd, yyyy");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            int timeOffset = Integer.parseInt(gmtOffset) / 3600;
            cal.add(Calendar.HOUR, timeOffset);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return dateFormatter.format(cal.getTime());
    }

    private void refreshWeather() {
        try {
            if (locationString.length() > 1) {
                String[] parts = locationString.split(",");
                StringBuilder sb = new StringBuilder();

                for (int i = 0 ; i < parts.length ; i++) {
                    sb.append(StringUtil.capsFirst(parts[i].trim()).trim());

                    if (i != parts.length - 1)
                        sb.append(", ");
                }

                locationLabel.setText(sb.toString());
            } else {
                locationLabel.setText("");
            }

            currentWeatherIconLabel.setIcon(new ImageIcon("static/pictures/weather/" + weatherIcon + ".png"));
            currentWeatherLabel.setText(StringUtil.capsFirst(weatherCondition));
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
            currentTemperatureLabel.setBounds(temperatureLineCenter - (width) / 2,
                    temperatureLabel.getY() - 3 - height, width, height);

            //redraw arrow
            windDirectionLabel.repaint();

            String[] parts = locationString.split(",");

            //frame title
            if (!parts[0].trim().isEmpty()) {
                String city = StringUtil.capsFirst(parts[0].trim()).trim();
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

    private String getTimezoneLabel() {
        return "GMT" + (Integer.parseInt(gmtOffset)/3600) + (IPUtil.getIpdata().getTime_zone().isIs_dst() ? " [DST Active]" : "");
    }

    private String correctedSunTime(String absoluteTime) {
        int hour = Integer.parseInt(absoluteTime.split(":")[0]);
        int minute = Integer.parseInt(absoluteTime.split(":")[1]);

        hour += (Integer.parseInt(gmtOffset) / 3600 - (currentLocationGMTOffset / 60 / 60));

        return hour + ":" + (minute < 10 ? "0" + minute : minute);
    }

    protected void repullWeatherStats() {
        CyderThreadRunner.submit(() -> {
            try {
                userCity = IPUtil.getIpdata().getCity();
                userState = IPUtil.getIpdata().getRegion();
                userCountry = IPUtil.getIpdata().getCountry_name();

                if (!useCustomLoc)
                    locationString = userCity + ", " + userState + ", " + userCountry;

                String key = UserUtil.getCyderUser().getWeatherkey();

                if (key.trim().isEmpty()) {
                    ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().inform("Sorry, but the Weather Key has not been set or is invalid" +
                            ", as a result, many features of Cyder will not work as intended. Please see the fields panel of the" +
                            " user editor to learn how to acquire a key and set it.","Weather Key Not Set");
                    return;
                }

                String OpenString = "https://api.openweathermap.org/data/2.5/weather?q=" +
                        locationString + "&appid=" + key + "&units=imperial";

                Gson gson = new Gson();
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

                    //frame title
                    if (!parts[0].trim().isEmpty()) {
                        String city = StringUtil.capsFirst(parts[0].trim()).trim();
                        weatherFrame.setTitle(city + StringUtil.getApostrophe(city) + " weather");
                    } else {
                        weatherFrame.setTitle("Weather");
                    }

                    ConsoleFrame.getConsoleFrame().revalidateMenu();
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
                refreshWeather();
            }
        },"Weather Stats Updater");
    }

    public static String getWindDirection(String wb) {
        return getWindDirection(Double.parseDouble(wb));
    }

    public static String getWindDirection(double bear) {
        while (bear > 360.0)
            bear -= 360.0;
        while (bear < 0.0)
            bear += 360.0;

        String ret = "";

        //northern hemisphere
       if (bear >= 0.0 && bear <= 180.0) {
           if (bear == 0.0)
               ret = "E";
           else if (bear == 180.0)
               ret = "W";
           else {
               //we now know it's north something
               ret += "N";

               if (bear > 90.0) {
                   ret += "W";
               } else if (bear < 90.0){
                   ret += "E";
               }
           }
       }
       //southern hemisphere excluding east and west directions
       else {
           //already know it must be S appended
           ret = "S";

           //is it east
           if (bear < 270.0)
               ret += "W";
           if (bear > 270.0)
               ret += "E";
       }

        return ret;
    }

    //weather object used for json serialization

    static class WeatherData {
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
            private double temp;
            private double feels_like;
            private double temp_min;
            private double temp_max;
            private double pressure;
            private double humidity;

            public double getTemp() {
                return temp;
            }

            public double getFeels_like() {
                return feels_like;
            }

            public double getTemp_min() {
                return temp_min;
            }

            public double getTemp_max() {
                return temp_max;
            }

            public double getPressure() {
                return pressure;
            }

            public double getHumidity() {
                return humidity;
            }

            public void setTemp(double temp) {
                this.temp = temp;
            }

            public void setFeels_like(double feels_like) {
                this.feels_like = feels_like;
            }

            public void setTemp_min(double temp_min) {
                this.temp_min = temp_min;
            }

            public void setTemp_max(double temp_max) {
                this.temp_max = temp_max;
            }

            public void setPressure(double pressure) {
                this.pressure = pressure;
            }

            public void setHumidity(double humidity) {
                this.humidity = humidity;
            }
        }

        public static class Wind {
            private double speed;
            private int deg;
            private double gust;

            public double getSpeed() {
                return speed;
            }

            public int getDeg() {
                return deg;
            }

            public double getGust() {
                return gust;
            }

            public void setSpeed(double speed) {
                this.speed = speed;
            }

            public void setDeg(int deg) {
                this.deg = deg;
            }

            public void setGust(double gust) {
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
