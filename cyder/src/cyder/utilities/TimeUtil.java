package cyder.utilities;

import cyder.constants.CyderStrings;
import cyder.ui.ConsoleFrame;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;

/**
 * Static utility class for things related to time/date queries and conversions.
 */
@SuppressWarnings({"unused", "SpellCheckingInspection"}) /* some methods unused still, date patterns */
public class TimeUtil {

    /**
     * Instantiation of TimeUtil class is not allowed.
     */
    public TimeUtil() {
        throw new IllegalStateException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Formats the provided date using the provided formatter
     *
     * @param now the LocalDateTime to format
     * @param dtf the formatter
     * @return the formatted date time
     */
    public static String formatDate(LocalDateTime now, DateTimeFormatter dtf) {
        return now.format(dtf);
    }

    /**
     * Formats the current date with the provided formatter.
     *
     * @param dtf the formatter to format the current date using
     * @return the formatted current date
     */
    public static String formatCurrentDate(DateTimeFormatter dtf) {
        return dtf.format(LocalDate.now());
    }

    /**
     * Returns the time used for the weather widget.
     *
     * @return the time used for the weather widget
     */
    public static String weatherTime() {
        return getTime("h:mm:ss aa EEEEEEEEEEEEE MMMMMMMMMMMMMMMMMM dd, yyyy");
    }

    /**
     * Returns the time used for log subdirectories.
     *
     * @return the time used for log subdirectories
     */
    public static String logSubDirTime() {
        return getTime("yyyy-MM-dd");
    }

    /**
     * Returns the current year in the format "yyyy".
     *
     * @return the current year in the format "yyyy"
     */
    public static int getYear() {
        return Integer.parseInt(getTime("yyyy"));
    }

    /**
     * Returns the time used for log files.
     *
     * @return the time used for log files
     */
    public static String logTime() {
        return getTime("HH-mm-ss");
    }

    /**
     * Returns the time used for determining what time notifications were originally added to the queue at.
     *
     * @return the time used for determining what time notifications were originally added to the queue at
     */
    public static String notificationTime() {
        return getTime("HH:mm:ss");
    }

    /**
     * Returns a nice, common, easy to read time.
     *
     * @return a nice, common, easy to read time
     */
    public static String userTime() {
        return getTime("EEEEEEEEE, MM/dd/yyyy hh:mmaa zzz");
    }

    /**
     * Returns the time formatted to the current console
     * clock format as set by the currently logged-in user.
     *
     * @return the time formatted to the current console
     * clock format as set by the currently logged-in user
     */
    public static String consoleTime() {
        if (ConsoleFrame.getConsoleFrame().getUUID() == null)
            throw new IllegalStateException("The console frame uuid is not set");

        return getTime(UserUtil.extractUser().getConsoleclockformat());
    }

    /**
     * Returns the default console clock format with seconds showing.
     *
     * @return the default console clock format with seconds showing
     */
    public static String consoleSecondTime() {
        return getTime("EEEEEEEEE h:mm:ssaa");
    }

    /**
     * Returns the default console clock format without seconds showing.
     *
     * @return the default console clock format without seconds
     */
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

    /**
     * Returns whether the current day is Christmas day.
     *
     * @return whether the current day is Christmas day
     */
    public static boolean isChristmas() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 12 && Date == 25);
    }

    /**
     * Returns whether the current day is halloween.
     *
     * @return whether the current day is halloween
     */
    public static boolean isHalloween() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 10 && Date == 31);
    }

    /**
     * Returns whether the current day is independence day (U.S. Holiday).
     *
     * @return whether the current day is independence day
     */
    public static boolean isIndependenceDay() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 7 && Date == 4);
    }

    /**
     * Returns whether the current day is Valentine's Day.
     *
     * @return whether the current day is Valentine's Day
     */
    public static boolean isValentinesDay() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 2 && Date == 14);
    }

    /**
     * Returns whether the current day is thanksgiving day.
     *
     * @return whether the current day is thanksgiving day
     */
    public static boolean isThanksgiving() {
        Calendar Checker = Calendar.getInstance();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        LocalDate RealTG = LocalDate.of(year, 11, 1).with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY));
        return (Month == 11 && Date == RealTG.getDayOfMonth());
    }

    /**
     * Returns whether the current day is April Fools' Day.
     *
     * @return whether the current day is April Fools' Day
     */
    public static boolean isAprilFoolsDay() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 4 && Date == 1);
    }

    /**
     * Returns whether the local time is past 6:00pm.
     *
     * @return whether the local time is past 6:00pm
     */
    public static boolean isEvening() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour > 17;
    }

    /**
     * Returns whether the local time is before 12:00pm.
     *
     * @return whether the local time is before 12:00pm
     */
    public static boolean isMorning() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour > 0 && hour < 11;
    }

    /**
     * Returns whether the current time is between 12:00pm and 6:00pm.
     *
     * @return whether the current time is between 12:00pm and 6:00pm
     */
    public static boolean isAfterNoon() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour > 11 && hour < 17;
    }

    //todo why does this return negative shit for random youtube?

    /**
     * Returns a string detailing how many years/months/days/hours/minutes/seconds
     * are represented by the given input parameter.
     *
     * @param msTime the raw long of ms
     * @return a String detailing how many years/months/days...
     *      are represented by the provided milliseconds.
     */
    public static String millisToFormattedString(long msTime) {
        StringBuilder sb = new StringBuilder();

        double years = 0;
        double months = 0;
        double days = 0;
        double hours = 0;
        double minutes = 0;
        double seconds;

        // seconds
        seconds = msTime / 1000.0;

        // convert seconds to minutes
        if (seconds >= 60) {
            minutes = Math.floor(seconds / 60.0);
        }

        // take away seconds that were converted
        seconds -=minutes * 60;

        // convert minutes to hours
        if (minutes >= 60) {
            hours = Math.floor(minutes / 60.0);
        }

        // take a way minutes that were converted
        minutes -= hours * 60;

        // convert hours to days
        if (hours >= 24) {
            days = Math.floor(hours / 24.0);
        }

        // take away hours that were converted
        hours -= days * 24;

        // convert days to months
        if (days >= 30) {
            months = Math.floor(days / 30.0);
        }

        // take away days that were converted
        days -= months * 30;

        // convert months to years
        if (months >= 12) {
            years = Math.floor(months / 12.0);
        }

        // take away months that were converted
        months -= years * 12;

        DecimalFormat format = new DecimalFormat("#.##");

        if (years != 0)
            sb.append(format.format(years)).append("y ");
        if (months != 0)
            sb.append(format.format(months)).append("mo ");
        if (days != 0)
            sb.append(format.format(days)).append("d ");
        if (hours != 0)
            sb.append(format.format(hours)).append("h ");
        if (minutes != 0)
            sb.append(format.format(minutes)).append("m ");
        if (seconds != 0)
            sb.append(format.format(seconds)).append("s ");

        String ret = sb.toString();

        // remove comma if starting with
        if (ret.startsWith(","))
            ret = ret.substring(1);

        return StringUtil.getTrimmedText(ret).trim();
    }

    /**
     * Converts the provided milliseconds to seconds.
     *
     * @param msTime the time represented in milliseconds
     * @return the provided milliseconds to seconds
     */
    public static double millisToSeconds(long msTime) {
        return msTime / 1000.0;
    }

    /**
     * Converts the provided milliseconds to minutes.
     *
     * @param msTime the time represented in milliseconds
     * @return the provided milliseconds to minutes
     */
    public static double millisToMinutes(long msTime) {
        return millisToSeconds(msTime) / 60.0;
    }

    /**
     * Converts the provided milliseconds to hours.
     *
     * @param msTime the time represented in milliseconds
     * @return the provided milliseconds to hours
     */
    public static double millisToHours(long msTime) {
        return millisToMinutes(msTime) / 60.0;
    }

    /**
     * Converts the provided milliseconds to days.
     *
     * @param msTime the time represented in milliseconds
     * @return the provided milliseconds to days
     */
    public static double millisToDays(long msTime) {
        return millisToHours(msTime) / 24.0;
    }

    /**
     * Converts the provided milliseconds to months (30 days).
     *
     * @param msTime the time represented in milliseconds
     * @return the provided milliseconds to months
     */
    public static double millisToMonths(long msTime) {
        return millisToDays(msTime) / 30.0;
    }

    /**
     * Converts the provided milliseconds to years.
     *
     * @param msTime the time represented in milliseconds
     * @return the provided milliseconds to years
     */
    public static double millisToYears(long msTime) {
        return millisToMonths(msTime) / 12.0;
    }
}
