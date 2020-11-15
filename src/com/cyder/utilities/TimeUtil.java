package com.cyder.utilities;

import com.cyder.ui.Notification;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

    private Util timeUtil;

    private int gmtOffset;

    private String userCity;
    private String userState;
    private String userCountry;

    private JFrame consoleFrame;

    public TimeUtil() {
        timeUtil = new Util();
        initGMTOffset();
    }

    //todo
    //tu.notify("<html>Internet connection<br/><br/>slow or unavailable<br/><br/><br/><br/><br/>test</html>",
    //                        3000, Notification.TOP_ARROW, Notification.TOP_VANISH, parentPane,300);
    public void notify(String htmltext, int delay, int arrowDir, int vanishDir, JLayeredPane parent, int width) {
        Notification consoleNotification = new Notification();

        int w = width;
        int h = 30;

        consoleNotification.setArrow(arrowDir);

        JLabel text = new JLabel();
        text.setText(htmltext);

        int lastIndex = 0;

        while(lastIndex != -1){

            lastIndex = text.getText().indexOf("<br/>",lastIndex);

            if(lastIndex != -1){
                h += 30;
                lastIndex += "<br/>".length();
            }
        }

        consoleNotification.setWidth(w);
        consoleNotification.setHeight(h);

        text.setFont(timeUtil.weatherFontSmall);
        text.setForeground(timeUtil.navy);
        text.setBounds(14,10,w * 2,h);
        consoleNotification.add(text);
        consoleNotification.setBounds(parent.getWidth() - (w + 30),30,w * 2,h * 2);
        parent.add(consoleNotification,1,0);
        parent.repaint();

        consoleNotification.vanish(vanishDir, parent, delay);
    }

    public String formatDate(LocalDateTime now, DateTimeFormatter dtf) {
        return now.format(dtf);
    }

    public String formatCurrentDate(DateTimeFormatter dtf) {
        return dtf.format(LocalDate.now());
    }

    public LinkedList<String> getTimezoneIDs() {
        TimeZone tz = TimeZone.getDefault();

        int offset = gmtOffset * 1000;
        String[] availableIDs = tz.getAvailableIDs(offset);

        LinkedList<String> timezoneIDs = new LinkedList<>();

        Collections.addAll(timezoneIDs, availableIDs);

        return timezoneIDs;
    }

    //returns gmt offset for current loc in seconds
    public int getGMTOffsetSeconds() {
        return gmtOffset;
    }

    //returns gmt offset for current loc, so slidell is -6, miami is -5
    public int getGMTOffsetHours() {
        return gmtOffset / 60 / 60;
    }

    private void initGMTOffset() {
        try {
            getIPData();

            String OpenString = "https://api.openweathermap.org/data/2.5/weather?q=" +
                    userCity + "," + userState + "," + userCountry + "&appid=" + timeUtil.getWeatherKey() + "&units=imperial";

            URL URL = new URL(OpenString);
            BufferedReader WeatherReader = new BufferedReader(new InputStreamReader(URL.openStream()));
            String[] Fields = {"", ""};
            String Line;

            while ((Line = WeatherReader.readLine()) != null) {
                String[] LineArray = Line.replace("{", "").replace("}", "")
                        .replace(":", "").replace("\"", "").replace("[", "")
                        .replace("]", "").replace(":", "").split(",");

                Fields = timeUtil.combineArrays(Fields, LineArray);
            }

            WeatherReader.close();

            for (String field : Fields) {
                if (field.contains("timezone")) {
                    gmtOffset = Integer.parseInt(field.replaceAll("[^0-9\\-]", ""));
                }
            }
        }

        catch (Exception e) {
            timeUtil.handle(e);
        }
    }

    //todo we need to have a class level IP data util that refreshes constantly that other classes can pull from
    private void getIPData() {
        try {
            String Key = timeUtil.getIPKey();
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

                else if (CurrentLine.contains("\"country_name\"")) {
                    userCountry = (CurrentLine.replace("country_name", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }
            }

            BR.close();
        }

        catch (Exception e) {
            timeUtil.handle(e);
        }
    }

    public void closeAtHourMinute(int Hour, int Minute, JFrame consoleFrame) {
        Calendar CloseCalendar = Calendar.getInstance();

        this.consoleFrame = consoleFrame;

        CloseCalendar.add(Calendar.DAY_OF_MONTH, 0);
        CloseCalendar.set(Calendar.HOUR_OF_DAY, Hour);
        CloseCalendar.set(Calendar.MINUTE, Minute);
        CloseCalendar.set(Calendar.SECOND, 0);
        CloseCalendar.set(Calendar.MILLISECOND, 0);

        long HowMany = (CloseCalendar.getTimeInMillis() - System.currentTimeMillis());
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.schedule(this::exit,HowMany, TimeUnit.MILLISECONDS);
    }

    private void exit() {
        timeUtil.closeAnimation(consoleFrame);
        System.exit(0);
    }
}
