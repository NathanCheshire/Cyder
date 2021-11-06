package cyder.utilities;

import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ErrorHandler;
import cyder.threads.CyderThreadFactory;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

    private int gmtOffset;
    public TimeUtil() {
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
            String key = UserUtil.extractUser().getWeatherkey();

            if (key.trim().length() == 0) {
                ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().inform("Sorry, but the Weather Key has not been set or is invalid" +
                        ", as a result, many features of Cyder will not work as intended. Please see the fields panel of the" +
                        " user editor to learn how to acquire a key and set it.","Weather Key Not Set");
                return;
            }

            String OpenString = "https://api.openweathermap.org/data/2.5/weather?q=" +
                    IPUtil.getIpdata().getCity() + "," + IPUtil.getIpdata().getRegion()+ "," +
                    IPUtil.getIpdata().getCountry_name() + "&appid=" + key + "&units=imperial";

            URL URL = new URL(OpenString);
            BufferedReader WeatherReader = new BufferedReader(new InputStreamReader(URL.openStream()));
            String[] Fields = {"", ""};
            String Line;

            while ((Line = WeatherReader.readLine()) != null) {
                String[] LineArray = Line.replace("{", "").replace("}", "")
                        .replace(":", "").replace("\"", "").replace("[", "")
                        .replace("]", "").replace(":", "").split(",");

                Fields = StringUtil.combineArrays(Fields, LineArray);
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

    public static void closeAtHourMinute(int Hour, int Minute, CyderFrame cf) {
        Calendar CloseCalendar = Calendar.getInstance();

        CloseCalendar.add(Calendar.DAY_OF_MONTH, 0);
        CloseCalendar.set(Calendar.HOUR_OF_DAY, Hour);
        CloseCalendar.set(Calendar.MINUTE, Minute);
        CloseCalendar.set(Calendar.SECOND, 0);
        CloseCalendar.set(Calendar.MILLISECOND, 0);

        Executors.newSingleThreadScheduledExecutor(
                new CyderThreadFactory("Scheduled Close Waiter [hour=" + Hour + ", minute=" + Minute + "]")).schedule(() -> {
            cf.dispose();

            try {
                if (UserUtil.getUserData("minimizeonclose").equals("1")) {
                    ConsoleFrame.getConsoleFrame().minimizeAll();
                } else {
                    GenesisShare.getExitingSem().acquire();
                    GenesisShare.getExitingSem().release();
                    GenesisShare.exit(66);
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, (CloseCalendar.getTimeInMillis() - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
    }

    public static String errorTime() {
        DateFormat dateFormat = new SimpleDateFormat("MMddyy-HH-mmaa");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String logFileTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String logTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
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
        SimpleDateFormat dateFormatter = new SimpleDateFormat(UserUtil.extractUser().getConsoleclockformat());
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

    public static boolean isEvening() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour > 17;
    }

    public static boolean isMorning() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour > 0 && hour < 11;
    }

    public static boolean isAfterNoon() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour > 11 && hour < 17;
    }

    /**
     * Returns a string detailing how many years/months/days/hours/minutes/seconds
     *  are represented by the given input parameter
     * @param msTime the raw long of ms
     */
    public static String milisToFormattedString(long msTime) {
        StringBuilder sb = new StringBuilder();

        double years = 0;
        double months = 0;
        double days = 0;
        double hours = 0;
        double minutes = 0;
        double seconds = 0;

        seconds = msTime / 1000;

        while (seconds >= 60) {
            seconds -= 60;
            minutes++;
        }

        while (minutes >= 60) {
            minutes -= 60;
            hours++;
        }

        while (hours >= 24) {
            hours -= 24;
            days++;
        }

        while (days >= 30) {
            days -= 30;
            months++;
        }

        while (months >= 12) {
            months -= 12;
            years++;
        }

        DecimalFormat format = new DecimalFormat("#.##");

        if (years != 0)
            sb.append("years: ").append(format.format(years));
        if (months != 0)
            sb.append(", months: ").append(format.format(months));
        if (days != 0)
            sb.append(", days: ").append(format.format(days));
        if (hours != 0)
            sb.append(", hours: ").append(format.format(hours));
        if (minutes != 0)
            sb.append(", minutes: ").append(format.format(minutes));
        if (seconds != 0)
            sb.append(", seconds: ").append(format.format(seconds));

        String ret = sb.toString();

        if (ret.startsWith(","))
            ret = ret.substring(1);
        return ret.trim();
    }

    /**
     * Returns a string detailing how many years/months/days/hours/minutes/seconds
     *  are represented by the given input parameter
     * @param msTime the raw long of ms
     */
    public static String milisToFormattedString(double msTime) {
        StringBuilder sb = new StringBuilder();

        double years = 0;
        double months = 0;
        double days = 0;
        double hours = 0;
        double minutes = 0;
        double seconds = 0;

        seconds = msTime / 1000;

        minutes = Math.floor(seconds / 60.0);
        seconds -= minutes * 60.0;

        hours = Math.floor(minutes / 60.0);
        minutes -= hours * 60.0;

        days = Math.floor(hours / 24.0);
        hours -= days * 24.0;

        months = Math.floor(days / 30.0);
        days -= months * 30.0;

        years = Math.floor(months / 12.0);
        months -= years * 12.0;

        DecimalFormat format = new DecimalFormat("#.##");

        if (years != 0)
            sb.append("years: ").append(format.format(years));
        if (months != 0)
            sb.append(", months: ").append(format.format(months));
        if (days != 0)
            sb.append(", days: ").append(format.format(days));
        if (hours != 0)
            sb.append(", hours: ").append(format.format(hours));
        if (minutes != 0)
            sb.append(", minutes: ").append(format.format(minutes));
        if (seconds != 0)
            sb.append(", seconds: ").append(format.format(seconds));

        String ret = sb.toString();

        if (ret.startsWith(","))
            ret = ret.substring(1);
        return ret.trim();
    }

    public static double milisToSeconds(long msTime) {
        return msTime / 1000;
    }

    public static double milisToMinutes(long msTime) {
        return msTime / 1000 / 60;
    }

    public static double milisToHours(long msTime) {
        return msTime / 1000 / 60 / 60;
    }

    public static double milisToDays(long msTime) {
        return msTime / 1000 / 60 / 60 / 24;
    }

    public static double milisToMonths(long msTime) {
        return msTime / 1000 / 60 / 60 / 24 / 30;
    }

    public static double milisToYears(long msTime) {
        return msTime / 1000 / 60 / 60 / 24 / 30 / 12;
    }
}
