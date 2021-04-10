package com.cyder.widgets;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.enums.TitlePosition;
import com.cyder.handler.ErrorHandler;
import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;
import com.cyder.utilities.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.cyder.Constants.CyderStrings.DEFAULT_BACKGROUND_PATH;

public class WeatherWidget {
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

    private String sunrise;
    private String sunset;
    private String weatherIcon;
    private String weatherCondition;
    private String windSpeed;
    private String visibility;
    private String temperature;
    private String humidity;
    private String pressure;
    private String feelsLike;
    private String windBearing;

    private String locationString;

    private String userCity;
    private String userState;
    private String userStateAbr;
    private String isp;
    private String lat;
    private String lon;
    private String userCountry;
    private String userCountryAbr;
    private String userIP;
    private String userPostalCode;
    private String userFlagURL;
    private String gmtOffset;

    private JButton closeWeather;
    private JButton minimizeWeather;
    private JLabel currentTimeLabel;

    private CyderFrame weatherFrame;

    private boolean updateWeather;
    private boolean updateClock;
    private boolean useCustomLoc;

    private int timeOffset;
    private int currentLocationGMTOffset;
    private boolean GMTset;

    public WeatherWidget() {
        weatherStats();

        if (weatherFrame != null)
            weatherFrame.closeAnimation();

        weatherFrame = new CyderFrame(500,600,new ImageIcon("src/com/cyder/sys/pictures/Weather.png"));
        weatherFrame.setTitlePosition(TitlePosition.CENTER);
        weatherFrame.setTitle("Weather");

        currentTimeLabel = new JLabel();

        currentTimeLabel.setForeground(CyderColors.vanila);

        currentTimeLabel.setFont(CyderFonts.weatherFontSmall);

        currentTimeLabel.setBounds(16, 50, 600, 30);

        currentTimeLabel.setText(weatherTime());

        weatherFrame.getContentPane().add(currentTimeLabel, SwingConstants.CENTER);

        locationLabel = new JLabel();

        locationLabel.setForeground(CyderColors.vanila);

        locationLabel.setFont(CyderFonts.weatherFontSmall);

        locationLabel.setBounds(16, 85, 480, 30);

        locationLabel.setText(locationString);

        weatherFrame.getContentPane().add(locationLabel, SwingConstants.CENTER);

        JLabel currentWeatherIconLabel = new JLabel(new ImageIcon("src/com/cyder/sys/pictures/" + weatherIcon + ".png"));

        currentWeatherIconLabel.setBounds(16, 125, 100, 100);

        currentWeatherIconLabel.setBorder(new LineBorder(CyderColors.navy,5,false));

        weatherFrame.getContentPane().add(currentWeatherIconLabel);

        currentWeatherLabel = new JLabel();

        currentWeatherLabel.setForeground(CyderColors.vanila);

        currentWeatherLabel.setFont(CyderFonts.weatherFontSmall);

        currentWeatherLabel.setBounds(16, 255, 400, 30);

        currentWeatherLabel.setText(capsFirst(weatherCondition));

        weatherFrame.getContentPane().add(currentWeatherLabel);

        changeLocationLabel = new JLabel("Change Location");

        changeLocationLabel.setFont(CyderFonts.weatherFontSmall);

        changeLocationLabel.setForeground(CyderColors.vanila);

        changeLocationLabel.setBounds(165, 220,200,30);

        changeLocationLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                CyderFrame changeLocationFrame = new CyderFrame(600,310,new ImageIcon(DEFAULT_BACKGROUND_PATH));
                changeLocationFrame.setTitle("Change Location");


                JLabel explenation = new JLabel("<html>Enter your city, state, and country code separated by a comma" +
                        "<br/>Example: <p style=\"font-family:verdana\"><p style=\"color:rgb(45, 100, 220)\">New Orleans,LA,US</p></p></html>");

                explenation.setFont(CyderFonts.weatherFontSmall);
                explenation.setForeground(CyderColors.navy);
                explenation.setHorizontalAlignment(JLabel.CENTER);
                explenation.setVerticalAlignment(JLabel.CENTER);
                explenation.setBounds(40,40,520,170);
                changeLocationFrame.getContentPane().add(explenation);

                JTextField changeLocField = new JTextField(20);
                changeLocField.setBorder(new LineBorder(CyderColors.navy,5,false));
                changeLocField.setForeground(CyderColors.navy);
                changeLocField.setSelectionColor(CyderColors.selectionColor);
                changeLocField.setFont(CyderFonts.weatherFontSmall);
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
                        locationString = changeLocField.getText();

                        useCustomLoc = true;

                        new AnimationUtil().closeAnimation(changeLocationFrame);
                        weatherFrame.inform("Attempting to refresh and use the location \"" + locationString + "\" for weather.", "");
                        refreshWeatherNow();
                    }

                    catch (Exception ex) {
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

        timezoneLabel.setText("Timezone stats: " + getTimezoneLabel());

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

        //todo change sunrise and sunset icons to actual icons and not the background image

        weatherFrame.setVisible(true);
        weatherFrame.setLocationRelativeTo(null);
        weatherFrame.initResizing();
        weatherFrame.setSnapSize(new Dimension(1,1));
        weatherFrame.setMinimumSize(new Dimension(weatherFrame.getWidth(),weatherFrame.getHeight()));
        weatherFrame.setMaximumSize(new Dimension(weatherFrame.getWidth() + 200, weatherFrame.getHeight() + 200));
        weatherFrame.setBackgroundResizing(true);

        updateClock = true;
        refreshClock();
        updateWeather = true;
        refreshWeather();
    }

    private void refreshClock() {
        Thread TimeThread = new Thread(() -> {
            try {
                while (updateClock) {
                    Thread.sleep(1000);
                    currentTimeLabel.setText(weatherTime());
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        });

        TimeThread.start();
    }

    public String weatherTime() {
        Calendar cal = Calendar.getInstance();
        timeOffset = Integer.parseInt(gmtOffset) / 3600;
        cal.add(Calendar.HOUR, timeOffset);
        Date Time = cal.getTime();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm:ss aa EEEEEEEEEEEEE MMMMMMMMMMMMMMMMMM dd, yyyy");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormatter.format(Time);
    }

    private void refreshWeather() {
        Thread WeatherThread = new Thread(() -> {
            try {
                //todo change to executor
                while (updateWeather) {
                    Thread.sleep(1800000);

                    weatherStats();
                    locationLabel.setText(locationString);

                    currentWeatherLabel.setText(capsFirst(weatherCondition));
                    temperatureLabel.setText("Temperature: " + temperature + "F");
                    feelsLikeLabel.setText("Feels like: " + feelsLike);
                    windSpeedLabel.setText("Wind Speed: " + windSpeed + "mph");
                    windDirectionLabel.setText("Wind Direction: " + windBearing + " Deg, " + getWindDirection(windBearing));
                    humidityLabel.setText("Humidity: " + humidity + "%");
                    pressureLabel.setText("Pressure: " + Double.parseDouble(pressure) / 1000 + "atm");
                    timezoneLabel.setText("Timezone stats: " + getTimezoneLabel());
                    sunriseLabel.setText(correctedSunTime(sunrise) + "am");
                    sunsetLabel.setText(correctedSunTime(sunset) + "pm");
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        });

        WeatherThread.start();
    }

    private void refreshWeatherNow() {
        try {
            weatherStats();
            locationLabel.setText(locationString);

            currentWeatherLabel.setText(capsFirst(weatherCondition));
            temperatureLabel.setText("Temperature: " + temperature + "F");
            feelsLikeLabel.setText("Feels like: " + feelsLike);
            windSpeedLabel.setText("Wind Speed: " + windSpeed + "mph");
            windDirectionLabel.setText("Wind Direction: " + windBearing + " Deg, " + getWindDirection(windBearing));
            humidityLabel.setText("Humidity: " + humidity + "%");
            pressureLabel.setText("Pressure: " + Double.parseDouble(pressure) / 1000 + "atm");
            timezoneLabel.setText("Timezone stats: " + getTimezoneLabel());
            sunriseLabel.setText(correctedSunTime(sunrise) + "am");
            sunsetLabel.setText(correctedSunTime(sunset) + "pm");
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private String getTimezoneLabel() {
        TimeZone tz = TimeZone.getDefault();

        int offset = Integer.parseInt(gmtOffset) * 1000;
        String[] availableIDs = tz.getAvailableIDs(offset);

        String one = "";
        String two = "";

        for (String availableID : availableIDs) {
            if (availableID.length() == 3) {
                one = availableID;
            }

            else if (availableID.contains("GMT"))
                two = availableID;
        }

        return one + "," + two;
    }

    private String correctedSunTime(String absoluteTime) {
        int hour = Integer.parseInt(absoluteTime.split(":")[0]);
        int minute = Integer.parseInt(absoluteTime.split(":")[1]);

        hour += (Integer.parseInt(gmtOffset) / 3600 - (currentLocationGMTOffset / 60 / 60));

        return hour + ":" + minute;
    }

    protected void weatherStats() {
        try {
            IPUtil ipu = new IPUtil();

            userCity = ipu.getUserCity();
            userState = ipu.getUserState();
            userCountry = ipu.getUserCountry();

            if (!useCustomLoc)
                locationString = userCity + "," + userState + "," + userCountry;

            String OpenString = "";

            OpenString = "https://api.openweathermap.org/data/2.5/weather?q=" +
                        locationString + "&appid=" + IOUtil.getSystemData("Weather") + "&units=imperial";

            URL URL = new URL(OpenString);
            BufferedReader WeatherReader = new BufferedReader(new InputStreamReader(URL.openStream()));
            String[] Fields = {"", ""};
            String Line;

            while ((Line = WeatherReader.readLine()) != null) {
                String[] LineArray = Line.replace("{", "").replace("}", "")
                        .replace(":", "").replace("\"", "").replace("[", "")
                        .replace("]", "").replace(":", "").split(",");

                Fields = new StringUtil().combineArrays(Fields, LineArray);
            }

            WeatherReader.close();

            for (String field : Fields) {
                if (field.contains("sunrise")) {
                    sunrise = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("sunset")) {
                    sunset = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("icon")) {
                    weatherIcon = field.replace("icon", "");
                }
                else if (field.contains("speed")) {
                    windSpeed = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("deg")) {
                    windBearing = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("description")) {
                    weatherCondition = field.replace("description", "");
                }
                else if (field.contains("visibility")) {
                    visibility = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("feels_like")) {
                    feelsLike = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("pressure")) {
                    pressure = field.replaceAll("[^\\d.]", "");
                    pressure = pressure.substring(0, Math.min(pressure.length(), 4));
                }
                else if (field.contains("humidity")) {
                    humidity = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("temp")) {
                    temperature = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("timezone")) {
                    gmtOffset = field.replaceAll("[^0-9\\-]", "");
                }
            }

            SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm");

            Date SunriseTime = new Date((long) Integer.parseInt(sunrise) * 1000);
            sunrise = dateFormatter.format(SunriseTime);

            Date SunsetTime = new Date((long) Integer.parseInt(sunset) * 1000);
            sunset = dateFormatter.format(SunsetTime);

            if (!GMTset) {
                currentLocationGMTOffset = Integer.parseInt(gmtOffset);
                GMTset = true;
            }

            Date Time = new Date();

            if (Time.getTime() > SunsetTime.getTime()) {
                weatherIcon = weatherIcon.replace("d", "n");
            }
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
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

    private String capsFirst(String Word) {
        StringBuilder SB = new StringBuilder(Word.length());
        String[] Words = Word.split(" ");

        for (String word : Words) {
            SB.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }

        return SB.toString();
    }
}
