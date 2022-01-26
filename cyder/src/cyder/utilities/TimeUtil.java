package cyder.utilities;

import cyder.constants.CyderStrings;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class TimeUtil {
    public TimeUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    public static String formatDate(LocalDateTime now, DateTimeFormatter dtf) {
        return now.format(dtf);
    }

    public static String formatCurrentDate(DateTimeFormatter dtf) {
        return dtf.format(LocalDate.now());
    }

    public static String weatherTime() {
        Calendar cal = Calendar.getInstance();
        Date Time = cal.getTime();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm:ss aa EEEEEEEEEEEEE MMMMMMMMMMMMMMMMMM dd, yyyy");
        return dateFormatter.format(Time);
    }

    //commonly used date patterns

    public static String errorTime() {
        return getTime("MMddyy-HH-mmaa");
    }

    public static String logSubDirTime() {
        return getTime("yyyy-MM-dd");
    }

    public static int getYear() {
        return Integer.parseInt(getTime("yyyy"));
    }

    public static String logTime() {
        return getTime("HH-mm-ss");
    }

    public static String notificationTime() {
        return getTime("HH:mm:ss");
    }

    public static String userTime() {
        return getTime("EEEEEEEEE, MM/dd/yyyy hh:mmaa zzz");
    }

    public static String consoleTime() {
        return getTime(UserUtil.extractUser().getConsoleclockformat());
    }

    public static String consoleSecondTime() {
        return getTime("EEEEEEEEE h:mm:ssaa");
    }

    public static String consoleNoSecondTime() {
        return getTime("EEEEEEEEE h:mmaa");
    }

    /**
     * Returns the time returned by running the provided string into a SimpleDatFormat object and formatting it.
     * @param datePattern the provided date pattern
     * @return the string representation of the current time
     */
    public static String getTime(String datePattern) {
        Date Time = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);
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
