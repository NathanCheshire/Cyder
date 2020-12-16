package com.cyder.utilities;

import com.cyder.handler.ErrorHandler;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

    private GeneralUtil timeGeneralUtil;
    private int gmtOffset;
    private IPUtil InternetProtocolUtil;

    public TimeUtil() {
        timeGeneralUtil = new GeneralUtil();
        InternetProtocolUtil = new IPUtil();

        initGMTOffset();
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

    public static String weatherTime() {
        Calendar cal = Calendar.getInstance();
        Date Time = cal.getTime();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm:ss aa EEEEEEEEEEEEE MMMMMMMMMMMMMMMMMM dd, yyyy");
        return dateFormatter.format(Time);
    }

    private void initGMTOffset() {
        try {
            String OpenString = "https://api.openweathermap.org/data/2.5/weather?q=" +
                    InternetProtocolUtil.getUserCity() + "," + InternetProtocolUtil.getUserState() + "," +
                    InternetProtocolUtil.getUserCountry() + "&appid=" + timeGeneralUtil.getWeatherKey() + "&units=imperial";

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
                if (field.contains("timezone")) {
                    gmtOffset = Integer.parseInt(field.replaceAll("[^0-9\\-]", ""));
                }
            }
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static void closeAtHourMinute(int Hour, int Minute, JFrame consoleFrame) {
        Calendar CloseCalendar = Calendar.getInstance();

        CloseCalendar.add(Calendar.DAY_OF_MONTH, 0);
        CloseCalendar.set(Calendar.HOUR_OF_DAY, Hour);
        CloseCalendar.set(Calendar.MINUTE, Minute);
        CloseCalendar.set(Calendar.SECOND, 0);
        CloseCalendar.set(Calendar.MILLISECOND, 0);

        long HowMany = (CloseCalendar.getTimeInMillis() - System.currentTimeMillis());
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.schedule(() -> {
            new AnimationUtil().closeAnimation(consoleFrame);
            System.exit(0);
        }, HowMany, TimeUnit.MILLISECONDS);
    }

    public static String errorTime() {
        DateFormat dateFormat = new SimpleDateFormat("MMddyy-HH-mmaa");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String userTime() {
        Date Time = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEEEEEEE, MM/dd/yyyy hh:mmaa zzz");
        return dateFormatter.format(Time);
    }

    public static String consoleTime() {
        Date Time = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEEEEEEE h:mmaa");
        return dateFormatter.format(Time);
    }

    public static String consoleSecondTime() {
        Date Time = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEEEEEEE h:mm:ssaa");
        return dateFormatter.format(Time);
    }

    public static boolean isChristmas() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 12 && Date == 25);
    }

    public static boolean isHalloween() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 10 && Date == 31);
    }

    public static boolean isIndependenceDay() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 7 && Date == 4);
    }

    public static boolean isThanksgiving() {
        Calendar Checker = Calendar.getInstance();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        LocalDate RealTG = LocalDate.of(year, 11, 1).with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY));
        return (Month == 11 && Date == RealTG.getDayOfMonth());
    }

    public static boolean isAprilFoolsDay() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 4 && Date == 1);
    }
}
