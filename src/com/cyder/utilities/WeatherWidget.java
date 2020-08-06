package com.cyder.utilities;

import com.cyder.ui.CyderButton;
import com.cyder.ui.DragLabel;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class WeatherWidget {
    private JLabel locationLabel;
    private JFrame changeLocationFrame;
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
    private JLabel visibilityLabel;

    private String customCity;
    private String customState;
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

    private JFrame weatherFrame;

    private boolean updateWeather;
    private boolean updateClock;
    private boolean useCustomLoc;

    private Util weatherUtil = new Util();

    public WeatherWidget() {
        useCustomLoc = false;
        weatherStats();

        if (weatherFrame != null) {
            weatherUtil.closeAnimation(weatherFrame);
            weatherFrame.dispose();
        }

        weatherFrame = new JFrame();

        weatherFrame.setTitle("Weather");

        weatherFrame.setSize(1080, 608);

        weatherFrame.setUndecorated(true);

        weatherFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel weatherLabel = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\Weather.png"));

        weatherFrame.setContentPane(weatherLabel);

        DragLabel weatherDragLabel = new DragLabel(1080,24, weatherFrame);

        weatherDragLabel.setBounds(0, 0, 1080, 24);

        weatherLabel.add(weatherDragLabel);

        currentTimeLabel = new JLabel();

        currentTimeLabel.setForeground(weatherUtil.vanila);

        currentTimeLabel.setFont(weatherUtil.weatherFontSmall);

        currentTimeLabel.setBounds(16, 50, 600, 30);

        currentTimeLabel.setText(weatherTime());

        weatherLabel.add(currentTimeLabel, SwingConstants.CENTER);

        locationLabel = new JLabel();

        locationLabel.setForeground(weatherUtil.vanila);

        locationLabel.setFont(weatherUtil.weatherFontSmall);

        locationLabel.setBounds(16, 80, 300, 30);

        locationLabel.setText(userCity + ", " + userStateAbr);

        weatherLabel.add(locationLabel, SwingConstants.CENTER);

        JLabel currentWeatherIconLabel = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\" + weatherIcon + ".png"));

        currentWeatherIconLabel.setBounds(16, 110, 100, 100);

        currentWeatherIconLabel.setBorder(new LineBorder(weatherUtil.navy,5,false));

        weatherLabel.add(currentWeatherIconLabel);

        currentWeatherLabel = new JLabel();

        currentWeatherLabel.setForeground(weatherUtil.vanila);

        currentWeatherLabel.setFont(weatherUtil.weatherFontSmall);

        currentWeatherLabel.setBounds(16, 212, 250, 30);

        currentWeatherLabel.setText(capsFirst(weatherCondition));

        weatherLabel.add(currentWeatherLabel);

        changeLocationLabel = new JLabel("Change Location");

        changeLocationLabel.setFont(weatherUtil.weatherFontSmall);

        changeLocationLabel.setForeground(weatherUtil.vanila);

        changeLocationLabel.setBounds(840, 550,200,30);

        changeLocationLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                changeLocationFrame = new JFrame();

                changeLocationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                changeLocationFrame.setTitle("Change Location");

                changeLocationFrame.setIconImage(weatherUtil.getCyderIcon().getImage());

                JPanel parent = new JPanel();

                parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));

                parent.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

                JLabel explenation = new JLabel("<html>Enter your city and state separated by a comma.<br/>" +
                        "Note that the format must be precise otherwise<br/>it will fail. " +
                        "Ex: \"New Orleans,LA\"</html>");

                explenation.setFont(weatherUtil.weatherFontSmall);

                explenation.setForeground(weatherUtil.navy);

                JPanel a = new JPanel();

                a.add(explenation, SwingConstants.CENTER);

                parent.add(a);

                JTextField changeLocField = new JTextField(20);

                changeLocField.setBorder(new LineBorder(weatherUtil.navy,5,false));

                changeLocField.setForeground(weatherUtil.navy);

                changeLocField.setFont(weatherUtil.weatherFontSmall);

                CyderButton changeLoc = new CyderButton("Change Location");

                changeLoc.setBorder(new LineBorder(weatherUtil.navy,5,false));

                changeLocField.addActionListener(e1 -> changeLoc.doClick());

                JPanel b = new JPanel();

                b.add(changeLocField, SwingConstants.CENTER);

                parent.add(b);

                changeLoc.setFont(weatherUtil.weatherFontSmall);

                changeLoc.setForeground(weatherUtil.navy);

                changeLoc.setColors(weatherUtil.regularRed);

                changeLoc.setBackground(weatherUtil.regularRed);

                changeLoc.addActionListener(e12 -> {
                    String[] parts = changeLocField.getText().split(",");
                    customCity = parts[0];
                    customState = parts[1];
                    useCustomLoc = true;
                    weatherUtil.closeAnimation(changeLocationFrame);
                    changeLocationFrame.dispose();
                    weatherUtil.inform("Attempting to refresh and use the location \"" + customCity + "\" for weather.", "",400, 300);
                    refreshWeatherNow();
                });

                JPanel c = new JPanel();

                c.add(changeLoc, SwingConstants.CENTER);

                parent.add(c);

                changeLocationFrame.add(parent);

                changeLocationFrame.setVisible(true);

                changeLocationFrame.pack();

                changeLocationFrame.setLocationRelativeTo(null);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                changeLocationLabel.setForeground(weatherUtil.navy);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                changeLocationLabel.setForeground(weatherUtil.vanila);
            }
        });

        weatherLabel.add(changeLocationLabel);

        temperatureLabel = new JLabel();

        temperatureLabel.setForeground(weatherUtil.vanila);

        temperatureLabel.setFont(weatherUtil.weatherFontSmall);

        temperatureLabel.setBounds(16, 270, 300, 30);

        temperatureLabel.setText("Temperature: " + temperature + "F");

        weatherLabel.add(temperatureLabel);

        feelsLikeLabel = new JLabel();

        feelsLikeLabel.setForeground(weatherUtil.vanila);

        feelsLikeLabel.setFont(weatherUtil.weatherFontSmall);

        feelsLikeLabel.setBounds(16, 310, 200, 30);

        feelsLikeLabel.setText("Feels like: " + feelsLike + "F");

        weatherLabel.add(feelsLikeLabel);

        windSpeedLabel = new JLabel();

        windSpeedLabel.setForeground(weatherUtil.vanila);

        windSpeedLabel.setFont(weatherUtil.weatherFontSmall);

        windSpeedLabel.setBounds(16, 390, 300, 30);

        windSpeedLabel.setText("Wind Speed: " + windSpeed + "mph");

        weatherLabel.add(windSpeedLabel);

        windDirectionLabel = new JLabel();

        windDirectionLabel.setForeground(weatherUtil.vanila);

        windDirectionLabel.setFont(weatherUtil.weatherFontSmall);

        windDirectionLabel.setBounds(16, 430, 300, 30);

        windDirectionLabel.setText("Wind Direction: " + windBearing + ", " + weatherUtil.getWindDirection(windBearing));

        weatherLabel.add(windDirectionLabel);

        humidityLabel = new JLabel();

        humidityLabel.setForeground(weatherUtil.vanila);

        humidityLabel.setFont(weatherUtil.weatherFontSmall);

        humidityLabel.setBounds(16, 470, 300, 30);

        humidityLabel.setText("Humidity: " + humidity + "%");

        weatherLabel.add(humidityLabel, SwingConstants.CENTER);

        pressureLabel = new JLabel();

        pressureLabel.setForeground(weatherUtil.vanila);

        pressureLabel.setFont(weatherUtil.weatherFontSmall);

        pressureLabel.setBounds(16, 510, 300, 30);

        pressureLabel.setText("Pressure: " + Double.parseDouble(pressure) / 1000 + "atm");

        weatherLabel.add(pressureLabel, SwingConstants.CENTER);

        visibilityLabel = new JLabel();

        visibilityLabel.setForeground(weatherUtil.vanila);

        visibilityLabel.setFont(weatherUtil.weatherFontSmall);

        visibilityLabel.setBounds(16, 550, 300, 30);

        visibilityLabel.setText("Visibility: " + Double.parseDouble(visibility) / 1000 + "mi");

        weatherLabel.add(visibilityLabel, SwingConstants.CENTER);

        sunriseLabel = new JLabel();

        sunriseLabel.setForeground(weatherUtil.vanila);

        sunriseLabel.setFont(weatherUtil.weatherFontSmall);

        sunriseLabel.setBounds(825, 517, 125, 30);

        sunriseLabel.setText(sunrise + "am");

        weatherLabel.add(sunriseLabel, SwingConstants.CENTER);

        sunsetLabel = new JLabel();

        sunsetLabel.setForeground(weatherUtil.vanila);

        sunsetLabel.setFont(weatherUtil.weatherFontSmall);

        sunsetLabel.setBounds(950, 519, 120, 30);

        sunsetLabel.setText(sunset + "pm");

        weatherLabel.add(sunsetLabel, SwingConstants.CENTER);

        weatherFrame.setVisible(true);

        weatherFrame.setLocationRelativeTo(null);

        weatherFrame.setIconImage(weatherUtil.getCyderIcon().getImage());

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
                weatherUtil.handle(e);
            }
        });

        TimeThread.start();
    }

    public String weatherTime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, Integer.parseInt(gmtOffset) / 3600);
        Date Time = cal.getTime();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm:ss aa EEEEEEEEEEEEE MMMMMMMMMMMMMMMMMM dd, yyyy");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormatter.format(Time);
    }

    private void refreshWeather() {
        Thread WeatherThread = new Thread(() -> {
            try {
                while (updateWeather) {
                    Thread.sleep(1800000);

                    weatherStats();

                    locationLabel.setText(userCity + ", " + userStateAbr);
                    currentWeatherLabel.setText(capsFirst(weatherCondition));
                    temperatureLabel.setText("Temperature: " + temperature + "F");
                    feelsLikeLabel.setText("Feels like: " + feelsLike);
                    windSpeedLabel.setText("Wind Speed: " + windSpeed + "mph");
                    windDirectionLabel.setText("Wind Direction: " + windBearing + ", " + weatherUtil.getWindDirection(windBearing));
                    humidityLabel.setText("Humidity: " + humidity + "%");
                    pressureLabel.setText("Pressure: " + Double.parseDouble(pressure) / 1000 + "atm");
                    visibilityLabel.setText("Visibility: " + Double.parseDouble(visibility) / 1000 + "mi");
                    sunriseLabel.setText(sunrise + "am");
                    sunsetLabel.setText(sunset + "pm");
                }
            } catch (Exception e) {
                weatherUtil.handle(e);
            }
        });

        WeatherThread.start();
    }

    private void refreshWeatherNow() {
        try {
            weatherStats();

            if (useCustomLoc) {
                locationLabel.setText(customCity + ", " + customState);
            }

            else {
                locationLabel.setText(userCity + ", " + userStateAbr);
            }

            currentWeatherLabel.setText(capsFirst(weatherCondition));
            temperatureLabel.setText("Temperature: " + temperature + "F");
            feelsLikeLabel.setText("Feels like: " + feelsLike);
            windSpeedLabel.setText("Wind Speed: " + windSpeed + "mph");
            windDirectionLabel.setText("Wind Direction: " + windBearing + ", " + weatherUtil.getWindDirection(windBearing));
            humidityLabel.setText("Humidity: " + humidity + "%");
            pressureLabel.setText("Pressure: " + Double.parseDouble(pressure) / 1000 + "atm");
            visibilityLabel.setText("Visibility: " + Double.parseDouble(visibility) / 1000 + "mi");
            sunriseLabel.setText(sunrise + "am");
            sunsetLabel.setText(sunset + "pm");
        }

        catch (Exception e) {
            weatherUtil.handle(e);
        }
    }

    protected void weatherStats() {
        try {
            getIPData();

            if (useCustomLoc) {
                userCity = customCity;
            }

            String OpenString = "https://api.openweathermap.org/data/2.5/weather?q=" + userCity + "&appid=" + weatherUtil.getWeatherKey() + "&units=imperial";

            URL URL = new URL(OpenString);
            BufferedReader WeatherReader = new BufferedReader(new InputStreamReader(URL.openStream()));
            String[] Fields = {"", ""};
            String Line;

            while ((Line = WeatherReader.readLine()) != null) {
                String[] LineArray = Line.replace("{", "").replace("}", "")
                        .replace(":", "").replace("\"", "").replace("[", "")
                        .replace("]", "").replace(":", "").split(",");

                Fields = weatherUtil.combineArrays(Fields, LineArray);
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

            Date Time = new Date();

            if (Time.getTime() > SunsetTime.getTime()) {
                weatherIcon = weatherIcon.replace("d", "n");
            }
        }

        catch (Exception e) {
            weatherUtil.handle(e);
        }
    }

    public String getWindDirection(String wb) {
        double bear = Double.parseDouble(wb);

        if (bear == 0) {
            return "N";
        } else if (bear == 90) {
            return "E";
        } else if (bear == 180) {
            return "S";
        } else if (bear == 270) {
            return "W";
        } else if (bear > 0 && bear < 90) {
            return "NE";
        } else if (bear > 90 && bear < 180) {
            return "SE";
        } else if (bear > 180 && bear < 270) {
            return "SW";
        } else if (bear > 270 && bear < 360) {
            return "NW";
        }

        return "NA";
    }

    public void getIPData() {
        try {
            String Key = weatherUtil.getIPKey();
            String url = "https://api.ipdata.co/?api-key=" + Key;

            URL Querry = new URL(url);

            BufferedReader BR = new BufferedReader(new InputStreamReader(Querry.openStream()));

            String CurrentLine;

            while ((CurrentLine = BR.readLine()) != null) {
                if (CurrentLine.contains("city")) {
                    userCity = (CurrentLine.replace("city", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"region\"")) {
                    userState = (CurrentLine.replace("region", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"region_code\"")) {
                    userStateAbr = (CurrentLine.replace("region_code", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("asn")) {
                    CurrentLine = BR.readLine();
                    CurrentLine = BR.readLine();
                    isp = (CurrentLine.replace("name", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"country_name\"")) {
                    userCountry = (CurrentLine.replace("country_name", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"country_code\"")) {
                    userCountryAbr = (CurrentLine.replace("country_code", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"latitude\"")) {
                    lat = (CurrentLine.replace("latitude", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"longitude\"")) {
                    lon = (CurrentLine.replace("longitude", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"ip\"")) {
                    userIP = (CurrentLine.replace("ip", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"flag\"")) {
                    userFlagURL = (CurrentLine.replace("\"flag\"", "").replace("\"","").replace(",", "").trim()).replaceFirst(":","");
                }

                else if (CurrentLine.contains("postal")) {
                    userPostalCode = (CurrentLine.replace("\"postal\"", "").replace("\"","").replace(",", "").replace(":", "").trim());
                }
            }
            BR.close();
        } catch (Exception e) {
            weatherUtil.handle(e);
        }
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
