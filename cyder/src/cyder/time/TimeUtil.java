package cyder.time;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.user.UserUtil;
import cyder.utils.StringUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/** Static utility class for things related to time/date queries and conversions. */
public final class TimeUtil {
    /** The number of milliseconds in a single second. */
    public static final double MILLISECONDS_IN_SECOND = 1000.0;

    /** The number of seconds in a single minute. */
    public static final double SECONDS_IN_MINUTE = 60.0;

    /** The number of minutes in a single hour. */
    public static final double MINUTES_IN_HOUR = 60.0;

    /** The number of hours in a single day. */
    public static final double HOURS_IN_DAY = 24.0;

    /** The number of days in a single month. */
    public static final double DAYS_IN_MONTH = 30.0;

    /** The number of months in a single year. */
    public static final double MONTHS_IN_YEAR = 12.0;

    /** The number of seconds in a single hour. */
    public static final int SECONDS_IN_HOUR = 3600;

    /** The abbreviation for a year. */
    public static final String YEAR_ABBREVIATION = "y";

    /** The abbreviation for a month. */
    public static final String MONTH_ABBREVIATION = "mo";

    /** The abbreviation for a day. */
    public static final String DAY_ABBREVIATION = "d";

    /** The abbreviation for an hour. */
    public static final String HOUR_ABBREVIATION = "h";

    /** The abbreviation for a minute. */
    public static final String MINUTE_ABBREVIATION = "m";

    /** The abbreviation for a second. */
    public static final String SECOND_ABBREVIATION = "s";

    /** The abbreviation for a millisecond. */
    public static final String MILLISECOND_ABBREVIATION = "ms";

    /** Suppress default constructor. */
    private TimeUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /** The calendar instance to use for calculations within this class. */
    private static final Calendar calendarInstance = Calendar.getInstance();

    /** The date formatter to use when the weather time is requested. */
    @SuppressWarnings("SpellCheckingInspection")
    public static final SimpleDateFormat weatherFormat
            = new SimpleDateFormat("h:mm:ss aa EEEEEEEEEEEEE MMMMMMMMMMMMMMMMMM dd, yyyy");

    /**
     * Returns the time used for the weather widget.
     *
     * @return the time used for the weather widget
     */
    public static String weatherTime() {
        return getFormattedTime(weatherFormat);
    }

    /** The date formatter to use when the log sub dir time is requested. */
    public static final SimpleDateFormat logSubDirFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Returns the time used for log subdirectories.
     *
     * @return the time used for log subdirectories
     */
    public static String logSubDirTime() {
        return getFormattedTime(logSubDirFormat);
    }

    /** The date formatter to use when the year is requested. */
    public static final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

    /**
     * Returns the current year in the format "yyyy".
     *
     * @return the current year in the format "yyyy"
     */
    public static int getYear() {
        return Integer.parseInt(getFormattedTime(yearFormat));
    }

    /** The date formatter to use when the log line time is requested. */
    public static final SimpleDateFormat LOG_TIME_FORMAT = new SimpleDateFormat("HH-mm-ss");

    /**
     * Returns the time used for log files.
     *
     * @return the time used for log files
     */
    public static String logTime() {
        return getFormattedTime(LOG_TIME_FORMAT);
    }

    /** The date formatter used for when a log line is being written to the log file. */
    public static final SimpleDateFormat LOG_LINE_TIME_FORMAT = new SimpleDateFormat("HH-mm-ss.SSS");

    /**
     * The time used for lines of log files.
     *
     * @return time used for lines of log files
     */
    public static String getLogLineTime() {
        return getFormattedTime(LOG_LINE_TIME_FORMAT);
    }

    /** The date formatter to use when formatting a date object to the notified at time. */
    public static final SimpleDateFormat notificationFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * Returns the time used for determining what time notifications were originally added to the queue at.
     *
     * @return the time used for determining what time notifications were originally added to the queue at
     */
    public static String notificationTime() {
        return getFormattedTime(notificationFormat);
    }

    /** The date formatter to use when formatting a date object to the console clock time format. */
    @SuppressWarnings("SpellCheckingInspection")
    public static final SimpleDateFormat userFormat =
            new SimpleDateFormat("EEEEEEEEE, MM/dd/yyyy hh:mmaa zzz");

    /**
     * Returns a nice, common, easy to read time.
     *
     * @return a nice, common, easy to read time
     */
    public static String userTime() {
        return getFormattedTime(userFormat);
    }

    /**
     * Returns the time formatted to the current console
     * clock format as set by the currently logged-in user.
     *
     * @return the time formatted to the current console
     * clock format as set by the currently logged-in user
     */
    public static String userFormattedTime() {
        if (Console.INSTANCE.getUuid() == null)
            throw new IllegalStateException("The console uuid is not set");

        return getTime(UserUtil.getCyderUser().getConsoleClockFormat());
    }

    /** The date formatter to use when formatting a date object to the console clock time format. */
    @SuppressWarnings("SpellCheckingInspection")
    public static final SimpleDateFormat consoleSecondFormat = new SimpleDateFormat("EEEEEEEEE h:mm:ssaa");

    /**
     * Returns the default console clock format with seconds showing.
     *
     * @return the default console clock format with seconds showing
     */
    public static String consoleSecondTime() {
        return getFormattedTime(consoleSecondFormat);
    }

    /** The date formatter to use when formatting a date object to the console clock time format without seconds. */
    @SuppressWarnings("SpellCheckingInspection")
    public static final SimpleDateFormat consoleNoSecondFormat = new SimpleDateFormat("EEEEEEEEE h:mmaa");

    /**
     * Returns the default console clock format without seconds showing.
     *
     * @return the default console clock format without seconds
     */
    public static String consoleNoSecondTime() {
        return getFormattedTime(consoleNoSecondFormat);
    }

    /**
     * Returns the time returned by running the provided string
     * into a SimpleDatFormat object and formatting it.
     *
     * @param datePattern the provided date pattern
     * @return the string representation of the current time
     */
    public static String getTime(String datePattern) {
        return getFormattedTime(new SimpleDateFormat(datePattern));
    }

    /**
     * Formats a new date object using the provided formatter.
     *
     * @param formatter the formatter to format a new date object
     * @return the formatted date
     */
    public static String getFormattedTime(SimpleDateFormat formatter) {
        return formatter.format(new Date());
    }

    /**
     * Returns whether the current day is Christmas day.
     *
     * @return whether the current day is Christmas day
     */
    public static boolean isChristmas() {
        int Month = calendarInstance.get(Calendar.MONTH) + 1;
        int Date = calendarInstance.get(Calendar.DATE);
        return (Month == 12 && Date == 25);
    }

    /**
     * Returns whether the current day is halloween.
     *
     * @return whether the current day is halloween
     */
    public static boolean isHalloween() {
        int Month = calendarInstance.get(Calendar.MONTH) + 1;
        int Date = calendarInstance.get(Calendar.DATE);
        return (Month == 10 && Date == 31);
    }

    /**
     * Returns whether the current day is independence day (U.S. Holiday).
     *
     * @return whether the current day is independence day
     */
    public static boolean isIndependenceDay() {
        int Month = calendarInstance.get(Calendar.MONTH) + 1;
        int Date = calendarInstance.get(Calendar.DATE);
        return (Month == 7 && Date == 4);
    }

    /**
     * Returns whether the current day is Valentine's Day.
     *
     * @return whether the current day is Valentine's Day
     */
    public static boolean isValentinesDay() {
        int Month = calendarInstance.get(Calendar.MONTH) + 1;
        int Date = calendarInstance.get(Calendar.DATE);
        return (Month == 2 && Date == 14);
    }

    /**
     * Returns whether the current day is thanksgiving day.
     *
     * @return whether the current day is thanksgiving day
     */
    public static boolean isThanksgiving() {
        MonthDay thanksgivingMonthDay = getThanksgiving(calendarInstance.get(Calendar.YEAR));

        int currentMonth = calendarInstance.get(Calendar.MONTH) + 1;
        int currentDate = calendarInstance.get(Calendar.DATE);

        return thanksgivingMonthDay.month() == currentMonth && thanksgivingMonthDay.date() == currentDate;
    }

    /**
     * Returns the thanksgiving month date for the provided year.
     *
     * @param year the year to return the thanksgiving month date of
     * @return the thanksgiving month date for the provided year
     */
    public static MonthDay getThanksgiving(int year) {
        LocalDate thanksGiving = LocalDate.of(year, 11, 1)
                .with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY));
        return new MonthDay(11, thanksGiving.getDayOfMonth());
    }

    /**
     * Returns whether the current day is April Fools' Day.
     *
     * @return whether the current day is April Fools' Day
     */
    public static boolean isAprilFoolsDay() {
        int Month = calendarInstance.get(Calendar.MONTH) + 1;
        int Date = calendarInstance.get(Calendar.DATE);
        return (Month == 4 && Date == 1);
    }

    /**
     * Returns whether the current day is Pi day.
     *
     * @return whether the current day is Pi day
     */
    public static boolean isPiDay() {
        int Month = calendarInstance.get(Calendar.MONTH) + 1;
        int Date = calendarInstance.get(Calendar.DATE);
        return (Month == 3 && Date == 14);
    }

    /**
     * Returns whether the current day is Easter.
     *
     * @return whether the current day is Easter
     */
    public static boolean isEaster() {
        int m = calendarInstance.get(Calendar.MONTH) + 1;
        int d = calendarInstance.get(Calendar.DATE);
        MonthDay sundayDate = getEasterSundayDate(calendarInstance.get(Calendar.YEAR));

        return (m == sundayDate.month && d == sundayDate.date);
    }

    public static boolean isDeveloperBirthday() {
        int Month = calendarInstance.get(Calendar.MONTH) + 1;
        int Date = calendarInstance.get(Calendar.DATE);
        return (Month == 6 && Date == 2);
    }

    /** A record used to represent a month and date such as June 2nd. */
    public record MonthDay(int month, int date) {}

    /**
     * Returns an int array representing the date easter is on for the given year.
     *
     * @param year the year to find the date of easter sunday
     * @return the easter sunday date; 4,13 would correspond to April 13th
     */
    public static MonthDay getEasterSundayDate(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int g = (8 * b + 13) / 25;
        int h = (19 * a + b - d - g + 15) % 30;
        int j = c / 4;
        int k = c % 4;
        int m = (a + 11 * h) / 319;
        int r = (2 * e + 2 * j - k - h + m + 32) % 7;
        int n = (h - m + r + 90) / 25;
        int p = (h - m + r + n + 19) % 32;

        return new MonthDay(n, p);
    }

    /**
     * Returns a string representing the date easter is on for the current year.
     *
     * @return a string representing the date easter is on for the current year
     */
    public static String getEasterSundayString() {
        MonthDay sundayDate = getEasterSundayDate(Calendar.getInstance().get(Calendar.YEAR));
        return monthFromNumber(sundayDate.month) + CyderStrings.space + formatNumberSuffix(sundayDate.date);
    }

    /**
     * Returns the number formatted with a suffix proper to its value.
     * Example, 1 returns "1st".
     *
     * @param dateInMonth the date to format, this should typically be between
     *                    1 and 31 but technically this method can handle any value.
     * @return the number with a proper suffix appended
     */
    public static String formatNumberSuffix(int dateInMonth) {
        int j = dateInMonth % 10;
        int k = dateInMonth % 100;

        if (j == 1 && k != 11) {
            return dateInMonth + "st";
        } else if (j == 2 && k != 12) {
            return dateInMonth + "nd";
        } else if (j == 3 && k != 13) {
            return dateInMonth + "rd";
        } else {
            return dateInMonth + "th";
        }
    }

    /**
     * Converts the month number to the month string.
     * Example, 12 returns "December".
     *
     * @param monthNumber the month's index starting at 1 for January
     * @return the String representation of the month
     */
    public static String monthFromNumber(int monthNumber) {
        return switch (monthNumber) {
            case 1 -> "January";
            case 2 -> "February";
            case 3 -> "March";
            case 4 -> "April";
            case 5 -> "May";
            case 6 -> "June";
            case 7 -> "July";
            case 8 -> "August";
            case 9 -> "September";
            case 10 -> "October";
            case 11 -> "November";
            case 12 -> "December";
            default -> throw new IllegalStateException("Invalid month code: " + monthNumber);
        };
    }

    /**
     * Returns whether the local time is past 6:00pm.
     *
     * @return whether the local time is past 6:00pm
     */
    public static boolean isEvening() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour >= 18;
    }

    private static final Range<Integer> MORNING_RANGE = Range.openClosed(0, 12);

    /**
     * Returns whether the local time is before 12:00pm.
     *
     * @return whether the local time is before 12:00pm
     */
    public static boolean isMorning() {
        return MORNING_RANGE.contains(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
    }

    private static final Range<Integer> AFTERNOON_RANGE = Range.openClosed(12, 18);

    /**
     * Returns whether the current time is between 12:00pm and 6:00pm.
     *
     * @return whether the current time is between 12:00pm and 6:00pm
     */
    public static boolean isAfterNoon() {
        return AFTERNOON_RANGE.contains(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
    }

    /** The decimal formatter for the {@link #formatMillis(long)} method. */
    private static final DecimalFormat milliFormatter = new DecimalFormat("#.##");

    /**
     * Returns a string detailing how many years/months/days/hours/minutes/seconds
     * are represented by the given input parameter.
     *
     * @param milliseconds the number of milliseconds to convert
     * @return a String detailing how many years/months/days...
     * are represented by the provided milliseconds.
     */
    public static String formatMillis(long milliseconds) {
        Preconditions.checkArgument(milliseconds >= 0);

        StringBuilder sb = new StringBuilder();

        double years = 0;
        double months = 0;
        double days = 0;
        double hours = 0;
        double minutes = 0;
        double seconds = 0;

        // Convert milliseconds to seconds
        if (milliseconds >= MILLISECONDS_IN_SECOND) {
            seconds = Math.floor(milliseconds / MILLISECONDS_IN_SECOND);
        }

        // Take away milliseconds that were converted
        milliseconds -= seconds * MILLISECONDS_IN_SECOND;

        // Convert seconds to minutes
        if (seconds >= SECONDS_IN_MINUTE) {
            minutes = Math.floor(seconds / SECONDS_IN_MINUTE);
        }

        // Take away seconds that were converted
        seconds -= minutes * SECONDS_IN_MINUTE;

        // Convert minutes to hours
        if (minutes >= MINUTES_IN_HOUR) {
            hours = Math.floor(minutes / MINUTES_IN_HOUR);
        }

        // Take a way minutes that were converted
        minutes -= hours * MINUTES_IN_HOUR;

        // Convert hours to days
        if (hours >= HOURS_IN_DAY) {
            days = Math.floor(hours / HOURS_IN_DAY);
        }

        // Take away hours that were converted
        hours -= days * HOURS_IN_DAY;

        // Convert days to months
        if (days >= DAYS_IN_MONTH) {
            months = Math.floor(days / DAYS_IN_MONTH);
        }

        // Take away days that were converted
        days -= months * DAYS_IN_MONTH;

        // Convert months to years
        if (months >= MONTHS_IN_YEAR) {
            years = Math.floor(months / MONTHS_IN_YEAR);
        }

        // Take away months that were converted
        months -= years * MONTHS_IN_YEAR;

        if (years != 0) {
            sb.append(milliFormatter.format(years)).append(YEAR_ABBREVIATION).append(CyderStrings.space);
        }
        if (months != 0) {
            sb.append(milliFormatter.format(months)).append(MONTH_ABBREVIATION).append(CyderStrings.space);
        }
        if (days != 0) {
            sb.append(milliFormatter.format(days)).append(DAY_ABBREVIATION).append(CyderStrings.space);
        }
        if (hours != 0) {
            sb.append(milliFormatter.format(hours)).append(HOUR_ABBREVIATION).append(CyderStrings.space);
        }
        if (minutes != 0) {
            sb.append(milliFormatter.format(minutes)).append(MINUTE_ABBREVIATION).append(CyderStrings.space);
        }
        if (seconds != 0) {
            sb.append(milliFormatter.format(seconds)).append(SECOND_ABBREVIATION).append(CyderStrings.space);
        }
        if (milliseconds != 0) {
            sb.append(milliFormatter.format(milliseconds)).append(MILLISECOND_ABBREVIATION).append(CyderStrings.space);
        }

        String ret = sb.toString();

        if (ret.startsWith(",")) ret = ret.substring(1);
        if (ret.length() == 0) return 0 + MILLISECOND_ABBREVIATION;

        return StringUtil.getTrimmedText(ret).trim();
    }

    /**
     * Converts the provided milliseconds to seconds.
     *
     * @param msTime the time represented in milliseconds
     * @return the provided milliseconds to seconds
     */
    public static double millisToSeconds(long msTime) {
        return msTime / MILLISECONDS_IN_SECOND;
    }

    /**
     * Converts the provided milliseconds to minutes.
     *
     * @param msTime the time represented in milliseconds
     * @return the provided milliseconds to minutes
     */
    public static double millisToMinutes(long msTime) {
        return millisToSeconds(msTime) / SECONDS_IN_MINUTE;
    }

    /**
     * Converts the provided milliseconds to hours.
     *
     * @param msTime the time represented in milliseconds
     * @return the provided milliseconds to hours
     */
    public static double millisToHours(long msTime) {
        return millisToMinutes(msTime) / MINUTES_IN_HOUR;
    }

    /**
     * Converts the provided milliseconds to days.
     *
     * @param msTime the time represented in milliseconds
     * @return the provided milliseconds to days
     */
    public static double millisToDays(long msTime) {
        return millisToHours(msTime) / HOURS_IN_DAY;
    }

    /**
     * Converts the provided milliseconds to months (30 days).
     *
     * @param msTime the time represented in milliseconds
     * @return the provided milliseconds to months
     */
    public static double millisToMonths(long msTime) {
        return millisToDays(msTime) / DAYS_IN_MONTH;
    }

    /**
     * Converts the provided milliseconds to years.
     *
     * @param msTime the time represented in milliseconds
     * @return the provided milliseconds to years
     */
    public static double millisToYears(long msTime) {
        return millisToMonths(msTime) / MONTHS_IN_YEAR;
    }

    // -----------------------------------
    // Relative timing methods and members
    // -----------------------------------

    /** The time at which the console was first shown. */
    private static final AtomicLong consoleFirstShownTime = new AtomicLong(0);

    /**
     * Returns the time at which the console was first shown.
     * This is not affected by a user logout and successive login.
     *
     * @return the time at which the console was first shown
     */
    public static long getConsoleFirstShownTime() {
        return consoleFirstShownTime.get();
    }

    /**
     * Sets the time at which the console was first shown.
     *
     * @param consoleFirstShownTimeMs the time at which the console was first shown
     */
    public static void setConsoleFirstShownTime(long consoleFirstShownTimeMs) {
        if (consoleFirstShownTime.get() != 0) return;

        consoleFirstShownTime.set(consoleFirstShownTimeMs);
    }
}
