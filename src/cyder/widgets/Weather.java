package cyder.widgets;

import com.google.gson.Gson;
import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.enums.Direction;
import cyder.genesis.GenesisShare;
import cyder.handler.ErrorHandler;
import cyder.genobjects.WeatherData;
import cyder.ui.*;
import cyder.utilities.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Weather {
    private JLabel locationLabel;
    private JLabel currentWeatherLabel;
    private JLabel changeLocationLabel;
    private JLabel temperatureLabel;
    private JLabel feelsLikeLabel;
    private JLabel windSpeedLabel;
    private JLabel windDirectionLabel;
    private JLabel humidityLabel;
    private JLabel pressureLabel;
    private JLabel sunsetLabel;
    private JLabel sunriseLabel;
    private JLabel timezoneLabel;
    private JLabel currentTimeLabel;
    private JLabel currentWeatherIconLabel;

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
    private String userStateAbr = "";
    private String isp = "";
    private String lat = "";
    private String lon = "";
    private String userCountry = "";
    private String userCountryAbr = "";
    private String userIP = "";
    private String userPostalCode = "";
    private String userFlagURL = "";
    private String gmtOffset = "0";

    private JButton closeWeather;
    private JButton minimizeWeather;

    private CyderFrame weatherFrame;

    private boolean useCustomLoc;
    private boolean update;

    private int timeOffset;
    private int currentLocationGMTOffset;
    private boolean GMTset;

    //nothing on constructor
    public Weather() {}

    //show gui method as per standard
    public void showGUI() {
        if (GenesisShare.isQuesitonableInternet()) {
            ConsoleFrame.getConsoleFrame().notify("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", but" +
                    " this feature is suspended until a stable internet connection can be established");
            return;
        }

        repullWeatherStats();

        if (weatherFrame != null)
            weatherFrame.dispose();

        weatherFrame = new CyderFrame(480,600, new ImageIcon(
                ImageUtil.getImageGradient(480, 600,
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
        currentTimeLabel.setFont(CyderFonts.weatherFontSmall);
        currentTimeLabel.setBounds(0, 50, 480, 30);
        weatherFrame.getContentPane().add(currentTimeLabel);

        locationLabel = new JLabel(locationString, SwingConstants.CENTER);
        locationLabel.setForeground(CyderColors.vanila);
        locationLabel.setFont(CyderFonts.weatherFontSmall);
        locationLabel.setBounds(0, 85, 480, 30);
        weatherFrame.getContentPane().add(locationLabel);

        currentWeatherIconLabel = new JLabel(new ImageIcon("sys/pictures/weather/" + weatherIcon + ".png"));
        currentWeatherIconLabel.setBounds(480 / 2 - 50, 130, 100, 100);
        currentWeatherIconLabel.setBorder(new LineBorder(CyderColors.navy,5,false));
        weatherFrame.getContentPane().add(currentWeatherIconLabel);

        sunriseLabel = new JLabel(sunrise + "am", SwingConstants.CENTER);
        sunriseLabel.setForeground(CyderColors.vanila);
        sunriseLabel.setFont(CyderFonts.weatherFontSmall);
        sunriseLabel.setBounds(0, 200, 480 / 2 - 50, 30);
        weatherFrame.getContentPane().add(sunriseLabel);

        sunsetLabel = new JLabel(sunset + "pm", SwingConstants.CENTER);
        sunsetLabel.setForeground(CyderColors.vanila);
        sunsetLabel.setFont(CyderFonts.weatherFontSmall);
        sunsetLabel.setBounds(290, 200, 480 - 290, 30);
        weatherFrame.getContentPane().add(sunsetLabel);

        JLabel sunriseLabelIcon = new JLabel(new ImageIcon("sys/pictures/weather/sunrise.png"));
        sunriseLabelIcon.setBounds(60, 145, 55, 48);
        weatherFrame.getContentPane().add(sunriseLabelIcon);

        JLabel sunsetLabelIcon = new JLabel(new ImageIcon("sys/pictures/weather/sunset.png"));
        sunsetLabelIcon.setBounds(480 - 55 - 60, 145, 55, 48);
        weatherFrame.getContentPane().add(sunsetLabelIcon);

        currentWeatherLabel = new JLabel(StringUtil.capsFirst(weatherCondition), SwingConstants.CENTER);
        currentWeatherLabel.setForeground(CyderColors.vanila);
        currentWeatherLabel.setFont(CyderFonts.weatherFontSmall);
        currentWeatherLabel.setBounds(0, 255, 480, 30);
        weatherFrame.getContentPane().add(currentWeatherLabel);


        JButton changeLocButton = new JButton("Location");
        changeLocButton.setForeground(CyderColors.vanila);
        changeLocButton.setFont(CyderFonts.defaultFontSmall);
        changeLocButton.setToolTipText("Change Location");
        changeLocButton.addActionListener(e -> {
            CyderFrame changeLocationFrame = new CyderFrame(600,310);
            changeLocationFrame.setBackground(CyderColors.vanila);
            changeLocationFrame.setTitle("Change Location");

            JLabel explenation = new JLabel("<html><div style='text-align: center;'>Enter your city, state, and country code separated by a comma" +
                    "<br/>Example: <p style=\"font-family:verdana\"><p style=\"color:rgb(45, 100, 220)\">New Orleans, LA, US</p></p></div></html>");

            explenation.setFont(CyderFonts.weatherFontSmall);
            explenation.setForeground(CyderColors.navy);
            explenation.setHorizontalAlignment(JLabel.CENTER);
            explenation.setVerticalAlignment(JLabel.CENTER);
            explenation.setBounds(40,40,520,170);
            changeLocationFrame.getContentPane().add(explenation);

            CyderTextField changeLocField = new CyderTextField(0);
            changeLocField.setBackground(Color.white);
            changeLocField.setBounds(40,200,520,40);
            changeLocationFrame.getContentPane().add(changeLocField);

            CyderButton changeLoc = new CyderButton("Change Location");
            changeLoc.setBorder(new LineBorder(CyderColors.navy,5,false));
            changeLocField.addActionListener(e1 -> changeLoc.doClick());
            changeLoc.setFont(CyderFonts.weatherFontSmall);
            changeLoc.setForeground(CyderColors.navy);
            changeLoc.setColors(CyderColors.regularRed);
            changeLoc.setBackground(CyderColors.regularRed);
            changeLoc.addActionListener(e12 -> {
                try {
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

                    AnimationUtil.closeAnimation(changeLocationFrame);
                    weatherFrame.notify("Attempting to refresh weather stats for location \"" + locationString + "\"");

                    repullWeatherStats();
                } catch (Exception ex) {
                    ErrorHandler.handle(ex);
                }
            });

            changeLoc.setBounds(40,250,520,40);
            changeLocationFrame.getContentPane().add(changeLoc);

            changeLocationFrame.setVisible(true);
            changeLocationFrame.setLocationRelativeTo(weatherFrame);
        });
        changeLocButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                changeLocButton.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                changeLocButton.setForeground(CyderColors.vanila);
            }
        });

        changeLocButton.setContentAreaFilled(false);
        changeLocButton.setBorderPainted(false);
        changeLocButton.setFocusPainted(false);
        weatherFrame.getTopDragLabel().addButton(changeLocButton, 0);

        temperatureLabel = new JLabel("Temperature: " + temperature + "F", SwingConstants.CENTER);
        temperatureLabel.setForeground(CyderColors.vanila);
        temperatureLabel.setFont(CyderFonts.weatherFontSmall);
        temperatureLabel.setBounds(0, 300, 480, 30);
        weatherFrame.getContentPane().add(temperatureLabel);

        feelsLikeLabel = new JLabel("Feels like: " + feelsLike + "F", SwingConstants.CENTER);
        feelsLikeLabel.setForeground(CyderColors.vanila);
        feelsLikeLabel.setFont(CyderFonts.weatherFontSmall);
        feelsLikeLabel.setBounds(0, 345, 480, 30);
        weatherFrame.getContentPane().add(feelsLikeLabel);

        windSpeedLabel = new JLabel("Wind Speed: " + windSpeed + "mph", SwingConstants.CENTER);
        windSpeedLabel.setForeground(CyderColors.vanila);
        windSpeedLabel.setFont(CyderFonts.weatherFontSmall);
        windSpeedLabel.setBounds(0, 390, 480, 30);
        weatherFrame.getContentPane().add(windSpeedLabel);

        windDirectionLabel = new JLabel("Wind Direction: " + windBearing + " Deg, " + getWindDirection(windBearing),
                SwingConstants.CENTER);
        windDirectionLabel.setForeground(CyderColors.vanila);
        windDirectionLabel.setFont(CyderFonts.weatherFontSmall);
        windDirectionLabel.setBounds(0, 430, 480, 30);
        weatherFrame.getContentPane().add(windDirectionLabel);

        humidityLabel = new JLabel("Humidity: " + humidity + "%", SwingConstants.CENTER);
        humidityLabel.setForeground(CyderColors.vanila);
        humidityLabel.setFont(CyderFonts.weatherFontSmall);
        humidityLabel.setBounds(0, 470, 480, 30);
        weatherFrame.getContentPane().add(humidityLabel);

        pressureLabel = new JLabel("Pressure: " + Double.parseDouble(pressure) / 1000 + "atm",
                SwingConstants.CENTER);
        pressureLabel.setForeground(CyderColors.vanila);
        pressureLabel.setFont(CyderFonts.weatherFontSmall);
        pressureLabel.setBounds(0, 510, 480, 30);
        weatherFrame.getContentPane().add(pressureLabel);

        timezoneLabel = new JLabel("Timezone: " + getTimezoneLabel(), SwingConstants.CENTER);
        timezoneLabel.setForeground(CyderColors.vanila);
        timezoneLabel.setFont(CyderFonts.weatherFontSmall);
        timezoneLabel.setBounds(0, 550, 480, 30);
        weatherFrame.getContentPane().add(timezoneLabel);

        weatherFrame.setVisible(true);
        weatherFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());

        update = true;

        new Thread(() -> {
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
                ErrorHandler.handle(e);
            }
        },"Weather Stats Updater").start();

        new Thread(() -> {
            try {
                for (;;) {
                    if (!update)
                        break;
                    Thread.sleep(1000);
                    currentTimeLabel.setText(getWeatherTime());
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"Weather Clock Updater").start();
    }

    public String getWeatherTime() {
        Calendar cal = Calendar.getInstance();
        Date Time = cal.getTime();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm:ssaa EEEEEEEEEEEEE, MMMMMMMMMMMMMMMMMM dd, yyyy");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            timeOffset = Integer.parseInt(gmtOffset) / 3600;
            cal.add(Calendar.HOUR, timeOffset);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            return dateFormatter.format(cal.getTime());
        }
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


            currentWeatherIconLabel.setIcon(new ImageIcon("sys/pictures/weather/" + weatherIcon + ".png"));
            currentWeatherLabel.setText(StringUtil.capsFirst(weatherCondition));
            temperatureLabel.setText("Temperature: " + temperature + "F");
            feelsLikeLabel.setText("Feels like: " + feelsLike + "F");
            windSpeedLabel.setText("Wind Speed: " + windSpeed + "mph");
            windDirectionLabel.setText("Wind Direction: " + windBearing + " Deg, " + getWindDirection(windBearing));
            humidityLabel.setText("Humidity: " + humidity + "%");
            pressureLabel.setText("Pressure: " + Double.parseDouble(pressure) / 1000 + "atm");
            timezoneLabel.setText("Timezone: " + getTimezoneLabel());
            sunriseLabel.setText(correctedSunTime(sunrise) + "am");
            sunsetLabel.setText(correctedSunTime(sunset) + "pm");

            String[] parts = locationString.split(",");

            //frame title
            if (parts[0].trim().length() > 0) {
                String city = StringUtil.capsFirst(parts[0].trim()).trim();
                weatherFrame.setTitle(city + StringUtil.getApostrophe(city) + " weather");
            } else {
                weatherFrame.setTitle("Weather");
            }

            if (weatherFrame != null)
                weatherFrame.notify("Refreshed", 2000, Direction.RIGHT);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private String getTimezoneLabel() {
        return "GMT" + (Integer.parseInt(gmtOffset)/3600);
    }

    private String correctedSunTime(String absoluteTime) {
        int hour = Integer.parseInt(absoluteTime.split(":")[0]);
        int minute = Integer.parseInt(absoluteTime.split(":")[1]);

        hour += (Integer.parseInt(gmtOffset) / 3600 - (currentLocationGMTOffset / 60 / 60));

        return hour + ":" + (minute < 10 ? "0" + minute : minute);
    }

    protected void repullWeatherStats() {
        new Thread(() -> {
            try {
                userCity = IPUtil.getIpdata().getCity();
                userState = IPUtil.getIpdata().getRegion();
                userCountry = IPUtil.getIpdata().getCountry_name();

                if (!useCustomLoc)
                    locationString = userCity + ", " + userState + ", " + userCountry;

                String OpenString = "";

                OpenString = "https://api.openweathermap.org/data/2.5/weather?q=" +
                        locationString + "&appid=" + IOUtil.getSystemData().getWeatherkey() + "&units=imperial";

                Gson gson = new Gson();
                WeatherData wd = null;

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
                    if (parts[0].trim().length() > 0) {
                        String city = StringUtil.capsFirst(parts[0].trim()).trim();
                        weatherFrame.setTitle(city + StringUtil.getApostrophe(city) + " weather");
                    } else {
                        weatherFrame.setTitle("Weather");
                    }
                }
            } catch (FileNotFoundException e) {
                //invalid custom location so go back to the old one
                weatherFrame.notify("Sorry, but that location is invalid");
                locationString = oldLocation;
                useCustomLoc = false;
                ErrorHandler.silentHandle(e);
            } catch (Exception e) {
                ErrorHandler.handle(e);
            } finally {
                refreshWeather();
            }
        },"Weather Stats Updater").start();
    }

    public String getWindDirection(String wb) {
        double bear = Double.parseDouble(wb);

        if (bear > 360)
            bear -= 360;

        String ret = "";

        if (bear > 270 || bear < 90)
            ret += "N";
        else if (bear == 270)
            return "W";
        else if (bear == 90)
            return "E";
        else
            ret += "S";

        if (bear > 0 && bear < 180)
            ret += "E";
        else if (bear == 180)
            return "S";
        else if (bear == 0 || bear == 360)
            return "N";
        else
            ret += "W";

        return ret;
    }
}
