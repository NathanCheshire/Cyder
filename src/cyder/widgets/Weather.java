package cyder.widgets;

import com.google.gson.Gson;
import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.enums.Direction;
import cyder.genesis.GenesisShare;
import cyder.handler.ErrorHandler;
import cyder.obj.WeatherData;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderTextField;
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
    private String weatherCondition = "0";
    private String windSpeed = "0";
    private String visibility = "0";
    private String temperature = "0";
    private String humidity = "0";
    private String pressure = "0";
    private String feelsLike = "0";
    private String windBearing = "0";

    private String locationString = "0";
    private String oldLocation = "0";

    private String userCity = "0";
    private String userState = "0";
    private String userStateAbr = "0";
    private String isp = "0";
    private String lat = "0";
    private String lon = "0";
    private String userCountry = "0";
    private String userCountryAbr = "0";
    private String userIP = "0";
    private String userPostalCode = "0";
    private String userFlagURL = "0";
    private String gmtOffset = "0";

    private JButton closeWeather;
    private JButton minimizeWeather;

    private CyderFrame weatherFrame;

    private boolean useCustomLoc;
    private boolean update;

    private int timeOffset;
    private int currentLocationGMTOffset;
    private boolean GMTset;

    public Weather() {
        if (GenesisShare.isQuesitonableInternet()) {
            ConsoleFrame.getConsoleFrame().notify("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", but" +
                    " this feature is suspended until a stable internet connection can be established");
            return;
        }

        repullWeatherStats();

        if (weatherFrame != null)
            weatherFrame.closeAnimation();

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
        weatherFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        weatherFrame.setTitle("Weather");

        currentTimeLabel = new JLabel();
        currentTimeLabel.setForeground(CyderColors.vanila);
        currentTimeLabel.setFont(CyderFonts.weatherFontSmall);
        currentTimeLabel.setBounds(16, 50, 600, 30);
        currentTimeLabel.setText(getWeatherTime());
        weatherFrame.getContentPane().add(currentTimeLabel, SwingConstants.CENTER);

        locationLabel = new JLabel();
        locationLabel.setForeground(CyderColors.vanila);
        locationLabel.setFont(CyderFonts.weatherFontSmall);
        locationLabel.setBounds(16, 85, 480, 30);
        locationLabel.setText(locationString);
        weatherFrame.getContentPane().add(locationLabel, SwingConstants.CENTER);

        currentWeatherIconLabel = new JLabel(new ImageIcon("sys/pictures/weather/" + weatherIcon + ".png"));
        currentWeatherIconLabel.setBounds(16, 125, 100, 100);
        currentWeatherIconLabel.setBorder(new LineBorder(CyderColors.navy,5,false));
        weatherFrame.getContentPane().add(currentWeatherIconLabel);

        sunriseLabel = new JLabel(new ImageIcon("sys/pictures/weather/sunrise.png"));
        sunriseLabel.setBounds(159, 136, 55, 48);
        weatherFrame.getContentPane().add(sunriseLabel);

        sunsetLabel = new JLabel(new ImageIcon("sys/pictures/weather/sunset.png"));
        sunsetLabel.setBounds(274, 136, 55, 48);
        weatherFrame.getContentPane().add(sunsetLabel);

        currentWeatherLabel = new JLabel();
        currentWeatherLabel.setForeground(CyderColors.vanila);
        currentWeatherLabel.setFont(CyderFonts.weatherFontSmall);
        currentWeatherLabel.setBounds(16, 255, 400, 30);
        currentWeatherLabel.setText(StringUtil.capsFirst(weatherCondition));
        weatherFrame.getContentPane().add(currentWeatherLabel);

        changeLocationLabel = new JLabel("Change Location");
        changeLocationLabel.setFont(CyderFonts.weatherFontSmall);
        changeLocationLabel.setForeground(CyderColors.vanila);
        changeLocationLabel.setBounds(165, 220,200,30);
        changeLocationLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                CyderFrame changeLocationFrame = new CyderFrame(600,310);
                changeLocationFrame.setBackground(CyderColors.vanila);
                changeLocationFrame.setTitle("Change Location");

                JLabel explenation = new JLabel("<html><div style='text-align: center;'>Enter your city, state, and country code separated by a comma" +
                        "<br/>Example: <p style=\"font-family:verdana\"><p style=\"color:rgb(45, 100, 220)\">New Orleans,LA,US</p></p></div></html>");

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
                        locationString = changeLocField.getText();

                        useCustomLoc = true;

                        AnimationUtil.closeAnimation(changeLocationFrame);
                        weatherFrame.inform("Attempting to refresh weather stats for location \"" + locationString + "\"", "Weather Update");
                        repullWeatherStats();
                    } catch (Exception ex) {
                        ErrorHandler.handle(ex);
                    }
                });

                changeLoc.setBounds(40,250,520,40);
                changeLocationFrame.getContentPane().add(changeLoc);

                changeLocationFrame.setVisible(true);
                changeLocationFrame.setLocationRelativeTo(weatherFrame);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                changeLocationLabel.setForeground(CyderColors.navy);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                changeLocationLabel.setForeground(CyderColors.vanila);
            }
        });
        weatherFrame.getContentPane().add(changeLocationLabel);

        temperatureLabel = new JLabel();
        temperatureLabel.setForeground(CyderColors.vanila);
        temperatureLabel.setFont(CyderFonts.weatherFontSmall);
        temperatureLabel.setBounds(16, 300, 300, 30);
        temperatureLabel.setText("Temperature: " + temperature + "F");
        weatherFrame.getContentPane().add(temperatureLabel);

        feelsLikeLabel = new JLabel();
        feelsLikeLabel.setForeground(CyderColors.vanila);
        feelsLikeLabel.setFont(CyderFonts.weatherFontSmall);
        feelsLikeLabel.setBounds(16, 345, 200, 30);
        feelsLikeLabel.setText("Feels like: " + feelsLike + "F");
        weatherFrame.getContentPane().add(feelsLikeLabel);

        windSpeedLabel = new JLabel();
        windSpeedLabel.setForeground(CyderColors.vanila);
        windSpeedLabel.setFont(CyderFonts.weatherFontSmall);
        windSpeedLabel.setBounds(16, 390, 300, 30);
        windSpeedLabel.setText("Wind Speed: " + windSpeed + "mph");
        weatherFrame.getContentPane().add(windSpeedLabel);

        windDirectionLabel = new JLabel();
        windDirectionLabel.setForeground(CyderColors.vanila);
        windDirectionLabel.setFont(CyderFonts.weatherFontSmall);
        windDirectionLabel.setBounds(16, 430, 400, 30);
        windDirectionLabel.setText("Wind Direction: " + windBearing + " Deg, " + getWindDirection(windBearing));
        weatherFrame.getContentPane().add(windDirectionLabel);

        humidityLabel = new JLabel();
        humidityLabel.setForeground(CyderColors.vanila);
        humidityLabel.setFont(CyderFonts.weatherFontSmall);
        humidityLabel.setBounds(16, 470, 300, 30);
        humidityLabel.setText("Humidity: " + humidity + "%");
        weatherFrame.getContentPane().add(humidityLabel, SwingConstants.CENTER);

        pressureLabel = new JLabel();
        pressureLabel.setForeground(CyderColors.vanila);
        pressureLabel.setFont(CyderFonts.weatherFontSmall);
        pressureLabel.setBounds(16, 510, 300, 30);
        pressureLabel.setText("Pressure: " + Double.parseDouble(pressure) / 1000 + "atm");
        weatherFrame.getContentPane().add(pressureLabel, SwingConstants.CENTER);

        timezoneLabel = new JLabel();
        timezoneLabel.setForeground(CyderColors.vanila);
        timezoneLabel.setFont(CyderFonts.weatherFontSmall);
        timezoneLabel.setBounds(16, 550, 400, 30);
        timezoneLabel.setText("Timezone: " + getTimezoneLabel());
        weatherFrame.getContentPane().add(timezoneLabel, SwingConstants.CENTER);

        sunriseLabel = new JLabel();
        sunriseLabel.setForeground(CyderColors.vanila);
        sunriseLabel.setFont(CyderFonts.weatherFontSmall);
        sunriseLabel.setBounds(150, 187, 125, 30);
        sunriseLabel.setText(sunrise + "am");
        weatherFrame.getContentPane().add(sunriseLabel, SwingConstants.CENTER);

        sunsetLabel = new JLabel();
        sunsetLabel.setForeground(CyderColors.vanila);
        sunsetLabel.setFont(CyderFonts.weatherFontSmall);
        sunsetLabel.setBounds(275, 189, 120, 30);
        sunsetLabel.setText(sunset + "pm");
        weatherFrame.getContentPane().add(sunsetLabel, SwingConstants.CENTER);

        weatherFrame.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelative(weatherFrame);

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

        weatherFrame.initializeResizing();
        weatherFrame.setMinimumSize(new Dimension(450,90));
        weatherFrame.setMaximumSize(new Dimension(450, 600));
        weatherFrame.setSnapSize(new Dimension(1,1));
    }

    public String getWeatherTime() {
        Calendar cal = Calendar.getInstance();
        Date Time = cal.getTime();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm:ss aa EEEEEEEEEEEEE MMMMMMMMMMMMMMMMMM dd, yyyy");
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
            locationLabel.setText(locationString);
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
                    locationString = userCity + "," + userState + "," + userCountry;

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
                }
            } catch (FileNotFoundException ignored) {
                //invalid custom location so go back to the old one
                weatherFrame.notify("Sorry, but that location is invalid");
                locationString = oldLocation;
                useCustomLoc = false;
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
