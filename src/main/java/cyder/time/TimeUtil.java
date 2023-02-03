package cyder.time;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.user.User;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;

/**
 * Static utility class for things related to time/date queries and conversions.
 */
@SuppressWarnings("SpellCheckingInspection") /* Date time patterns */
public final class TimeUtil {
    /**
     * The number of milliseconds in a single second.
     */
    public static final double MILLISECONDS_IN_SECOND = 1000.0;

    /**
     * The number of seconds in a single minute.
     */
    public static final double SECONDS_IN_MINUTE = 60.0;

    /**
     * The number of minutes in a single hour.
     */
    public static final double MINUTES_IN_HOUR = 60.0;

    /**
     * The number of hours in a single day.
     */
    public static final double HOURS_IN_DAY = 24.0;

    /**
     * The number of days in a single month.
     */
    public static final double DAYS_IN_MONTH = 30.0;

    /**
     * The number of months in a single year.
     */
    public static final double MONTHS_IN_YEAR = 12.0;

    /**
     * The number of seconds in a single hour.
     */
    public static final int SECONDS_IN_HOUR = 3600;

    /**
     * The abbreviation for a year.
     */
    public static final String YEAR_ABBREVIATION = "y";

    /**
     * The abbreviation for a month.
     */
    public static final String MONTH_ABBREVIATION = "mo";

    /**
     * The abbreviation for a day.
     */
    public static final String DAY_ABBREVIATION = "d";

    /**
     * The abbreviation for an hour.
     */
    public static final String HOUR_ABBREVIATION = "h";

    /**
     * The abbreviation for a minute.
     */
    public static final String MINUTE_ABBREVIATION = "m";

    /**
     * The abbreviation for a second.
     */
    public static final String SECOND_ABBREVIATION = "s";

    /**
     * The abbreviation for a millisecond.
     */
    public static final String MILLISECOND_ABBREVIATION = "ms";

    /**
     * The calendar instance to use for calculations within this class.
     */
    private static final Calendar calendarInstance = Calendar.getInstance();

    /**
     * The date formatter to use when the weather time is requested.
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static final SimpleDateFormat weatherFormat =
            new SimpleDateFormat("h:mm:ss aa EEEEEEEEEEEEE MMMMMMMMMMMMMMMMMM dd, yyyy");

    /**
     * The date formatter to use when the log sub dir time is requested.
     */
    public static final SimpleDateFormat logSubDirFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * The date formatter to use when the log line time is requested.
     */
    public static final SimpleDateFormat LOG_TIME_FORMAT = new SimpleDateFormat("HH-mm-ss");

    /**
     * The date formatter used for when a log line is being written to the log file.
     */
    public static final SimpleDateFormat LOG_LINE_TIME_FORMAT = new SimpleDateFormat("HH-mm-ss.SSS");

    /**
     * The date formatter to use when formatting a date object to the notified at time.
     */
    public static final SimpleDateFormat notificationFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * The date formatter to use when formatting a date object to the console clock time format.
     */
    public static final SimpleDateFormat userFormat = new SimpleDateFormat("EEEEEEEEE, MM/dd/yyyy hh:mmaa zzz");

    /**
     * The date formatter to use when formatting a date object to the console clock time format.
     */
    public static final SimpleDateFormat consoleSecondFormat = new SimpleDateFormat(User.DEFAULT_CONSOLE_CLOCK_FORMAT);

    /**
     * The date formatter to use when formatting a date object to the console clock time format without seconds.
     */
    public static final SimpleDateFormat consoleNoSecondFormat = new SimpleDateFormat("EEEEEEEEE h:mmaa");

    /**
     * An immutable map of month ordinals to their names.
     */
    public static final ImmutableMap<Integer, String> months = new ImmutableMap.Builder<Integer, String>()
            .put(1, "January")
            .put(2, "February")
            .put(3, "March")
            .put(4, "April")
            .put(5, "May")
            .put(6, "June")
            .put(7, "July")
            .put(8, "August")
            .put(9, "September")
            .put(10, "October")
            .put(11, "November")
            .put(12, "December")
            .build();

    /**
     * The range an hour must fall within to be counted as in the morning.
     */
    private static final Range<Integer> MORNING_RANGE = Range.closedOpen(0, 12);

    /**
     * The range an hour must fall within to be counted as in the after-noon.
     */
    private static final Range<Integer> AFTERNOON_RANGE = Range.closedOpen(12, 18);

    /**
     * The range an hour must fall within to be counted as in the evening.
     */
    private static final Range<Integer> EVENING_RANGE = Range.closedOpen(18, 24);

    /**
     * The decimal formatter for the {@link #formatMillis(long)} method.
     */
    private static final DecimalFormat milliFormatter = new DecimalFormat("#.##");

    /**
     * The Christmas month day object.
     */
    private static final MonthDay christmas = new MonthDay(Calendar.DECEMBER + 1, 25);

    /**
     * The Halloween month day object.
     */
    private static final MonthDay halloween = new MonthDay(Calendar.OCTOBER + 1, 31);

    /**
     * The independence day month day object.
     */
    private static final MonthDay independenceDay = new MonthDay(Calendar.JULY + 1, 4);

    /**
     * The Valentines day month day object.
     */
    private static final MonthDay valentinesDay = new MonthDay(Calendar.FEBRUARY + 1, 14);

    /**
     * The month Thanksgiving falls in.
     */
    private static final int thanksgivingMonth = Calendar.NOVEMBER + 1;

    /**
     * The nth Thursday Thanksgiving falls on.
     */
    private static final int thanksgivingNthThursday = 4;

    /**
     * The April fools month day object.
     */
    private static final MonthDay aprilFoolsDay = new MonthDay(Calendar.APRIL + 1, 1);

    /**
     * The pi day month day object.
     */
    private static final MonthDay piDay = new MonthDay(Calendar.MARCH + 1, 14);

    /**
     * The new years day month day object.
     */
    private static final MonthDay newYearsDay = new MonthDay(Calendar.JANUARY + 1, 1);

    /**
     * The ground hog day month day object.
     */
    private static final MonthDay groundHogDay = new MonthDay(Calendar.FEBRUARY + 1, 2);

    /**
     * The month labor day falls in.
     */
    private static final int laborDayMonth = Calendar.SEPTEMBER + 1;

    /**
     * The nth Monday labor day falls on
     */
    private static final int nthMondayLaborDay = 1;

    /**
     * The number for the month of may
     */
    private static final int mayMonth = Calendar.MAY + 1;

    /**
     * The number of days which separate Mardi Grass from Easter Sunday.
     */
    private static final int MARDI_GRASS_EASTER_SEPARATION_DAYS = 47;

    /**
     * The range a minute value must fall within.
     */
    public static final Range<Integer> minuteRange = Range.closed(0, (int) TimeUtil.SECONDS_IN_MINUTE);

    /**
     * Suppress default constructor.
     */
    private TimeUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the time used for the weather widget.
     *
     * @return the time used for the weather widget
     */
    public static String weatherTime() {
        return getFormattedTime(weatherFormat);
    }

    /**
     * Returns the time used for log subdirectories.
     *
     * @return the time used for log subdirectories
     */
    public static String logSubDirTime() {
        return getFormattedTime(logSubDirFormat);
    }

    /**
     * Returns the time used for log files.
     *
     * @return the time used for log files
     */
    public static String logTime() {
        return getFormattedTime(LOG_TIME_FORMAT);
    }

    /**
     * The time used for lines of log files.
     *
     * @return time used for lines of log files
     */
    public static String getLogLineTime() {
        return getFormattedTime(LOG_LINE_TIME_FORMAT);
    }

    /**
     * Returns the time used for determining what time notifications were originally added to the queue at.
     *
     * @return the time used for determining what time notifications were originally added to the queue at
     */
    public static String notificationTime() {
        return getFormattedTime(notificationFormat);
    }

    /**
     * Returns a nice, easy to read time.
     *
     * @return a nice, easy to read time
     */
    public static String userReadableTime() {
        return getFormattedTime(userFormat);
    }

    /**
     * Returns the default console clock format with seconds showing.
     *
     * @return the default console clock format with seconds showing
     */
    public static String consoleSecondTime() {
        return getFormattedTime(consoleSecondFormat);
    }

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
        Preconditions.checkNotNull(datePattern);
        Preconditions.checkArgument(!datePattern.isEmpty());

        return getFormattedTime(new SimpleDateFormat(datePattern));
    }

    /**
     * Formats a new date object using the provided formatter.
     *
     * @param formatter the formatter to format a new date object
     * @return the formatted date
     */
    public static String getFormattedTime(SimpleDateFormat formatter) {
        Preconditions.checkNotNull(formatter);

        return formatter.format(new Date());
    }

    // -------------------------------
    // Special day computation methods
    // -------------------------------

    /**
     * Returns whether {@link MonthDay#TODAY} is Christmas day.
     *
     * @return whether {@link MonthDay#TODAY} is Christmas day
     */
    public static boolean isChristmas() {
        return isChristmas(MonthDay.TODAY);
    }

    /**
     * Returns whether the provided month day is Christmas.
     *
     * @param monthDay the month day object
     * @return whether the provided month day is Christmas
     */
    public static boolean isChristmas(MonthDay monthDay) {
        Preconditions.checkNotNull(monthDay);

        return monthDay.equals(christmas);
    }

    /**
     * Returns whether {@link MonthDay#TODAY} is halloween.
     *
     * @return whether {@link MonthDay#TODAY} is halloween
     */
    public static boolean isHalloween() {
        return isHalloween(MonthDay.TODAY);
    }

    /**
     * Returns whether the provided month day day is halloween.
     *
     * @param monthDay the month day object
     * @return whether the provided month day is halloween
     */
    public static boolean isHalloween(MonthDay monthDay) {
        Preconditions.checkNotNull(monthDay);

        return monthDay.equals(halloween);
    }

    /**
     * Returns whether {@link MonthDay#TODAY} is independence day (U.S. Holiday).
     *
     * @return whether {@link MonthDay#TODAY} is independence day
     */
    public static boolean isIndependenceDay() {
        return isIndependenceDay(MonthDay.TODAY);
    }

    /**
     * Returns whether the provided day is independence day (U.S. Holiday).
     *
     * @param monthDay the month day object
     * @return whether the provided day is independence day
     */
    public static boolean isIndependenceDay(MonthDay monthDay) {
        Preconditions.checkNotNull(monthDay);

        return monthDay.equals(independenceDay);
    }

    /**
     * Returns whether {@link MonthDay#TODAY} is Valentine's Day.
     *
     * @return whether {@link MonthDay#TODAY} is Valentine's Day
     */
    public static boolean isValentinesDay() {
        return isValentinesDay(MonthDay.TODAY);
    }

    /**
     * Returns whether the provided day is Valentine's Day.
     *
     * @param monthDay the month day object
     * @return whether the provided day is Valentine's Day
     */
    public static boolean isValentinesDay(MonthDay monthDay) {
        Preconditions.checkNotNull(monthDay);

        return monthDay.equals(valentinesDay);
    }

    /**
     * Returns whether {@link MonthDay#TODAY} is thanksgiving day.
     *
     * @return whether {@link MonthDay#TODAY} is thanksgiving day
     */
    public static boolean isThanksgiving() {
        return isThanksgiving(MonthDay.TODAY);
    }

    /**
     * Returns whether the provided day is thanksgiving day.
     *
     * @param monthDay the month day object
     * @return whether the provided day is thanksgiving day
     */
    public static boolean isThanksgiving(MonthDay monthDay) {
        Preconditions.checkNotNull(monthDay);

        return monthDay.equals(getThanksgiving(getCurrentYear()));
    }

    /**
     * Returns whether {@link MonthDay#TODAY} is April Fools' Day.
     *
     * @return whether {@link MonthDay#TODAY} is April Fools' Day
     */
    public static boolean isAprilFoolsDay() {
        return isAprilFoolsDay(MonthDay.TODAY);
    }

    /**
     * Returns whether the provided day is April Fools' Day.
     *
     * @param monthDay the month day object
     * @return whether the provided day is April Fools' Day
     */
    public static boolean isAprilFoolsDay(MonthDay monthDay) {
        Preconditions.checkNotNull(monthDay);

        return monthDay.equals(aprilFoolsDay);
    }

    /**
     * Returns whether {@link MonthDay#TODAY} is Pi day.
     *
     * @return whether {@link MonthDay#TODAY} is Pi day
     */
    public static boolean isPiDay() {
        return isPiDay(MonthDay.TODAY);
    }

    /**
     * Returns whether the provided day is Pi day.
     *
     * @param monthDay the month day object
     * @return whether the provided day is Pi day
     */
    public static boolean isPiDay(MonthDay monthDay) {
        Preconditions.checkNotNull(monthDay);

        return monthDay.equals(piDay);
    }

    /**
     * Returns whether {@link MonthDay#TODAY} is Easter.
     *
     * @return whether {@link MonthDay#TODAY} is Easter
     */
    public static boolean isEaster() {
        return isEaster(getEasterSundayDate(getCurrentYear()));
    }

    /**
     * Returns whether the provided day is Easter.
     *
     * @param monthDay the month day object
     * @return whether the provided day is Easter
     */
    public static boolean isEaster(MonthDay monthDay) {
        Preconditions.checkNotNull(monthDay);

        return monthDay.equals(getEasterSundayDate(getCurrentYear()));
    }

    /**
     * Returns whether {@link MonthDay#TODAY} is new years day.
     *
     * @return whether {@link MonthDay#TODAY} is new years day
     */
    public static boolean isNewYearsDay() {
        return isNewYearsDay(MonthDay.TODAY);
    }

    /**
     * Returns whether the provided day is new years day.
     *
     * @param monthDay the month day object
     * @return whether the provided day is new years day
     */
    public static boolean isNewYearsDay(MonthDay monthDay) {
        Preconditions.checkNotNull(monthDay);

        return monthDay.equals(newYearsDay);
    }

    /**
     * Returns whether {@link MonthDay#TODAY} is new ground hog day.
     *
     * @return whether {@link MonthDay#TODAY} is new ground hog day
     */
    public static boolean isGroundHogDay() {
        return isGroundHogDay(MonthDay.TODAY);
    }

    /**
     * Returns whether the provided day is new ground hog day.
     *
     * @param monthDay the month day object
     * @return whether the provided day is new ground hog day
     */
    public static boolean isGroundHogDay(MonthDay monthDay) {
        Preconditions.checkNotNull(monthDay);

        return monthDay.equals(groundHogDay);
    }

    /**
     * Returns whether {@link MonthDay#TODAY} is Mardi Grass.
     *
     * @return whether {@link MonthDay#TODAY} is Mardi Grass
     */
    public static boolean isMardiGrassDay() {
        return isMardiGrassDay(MonthDay.TODAY);
    }

    /**
     * Returns whether the provided day is Mardi Grass.
     *
     * @param monthDay the month day object
     * @return whether the provided day is Mardi Grass
     */
    public static boolean isMardiGrassDay(MonthDay monthDay) {
        Preconditions.checkNotNull(monthDay);

        return monthDay.equals(getMardiGrassDay(getCurrentYear()));
    }

    /**
     * Returns whether {@link MonthDay#TODAY} is labor day.
     *
     * @return whether {@link MonthDay#TODAY} is labor day
     */
    public static boolean isLaborDay() {
        return isLaborDay(MonthDay.TODAY);
    }

    /**
     * Returns whether the provided day is labor day.
     *
     * @param monthDay the month day object
     * @return whether the provided day is labor day
     */
    public static boolean isLaborDay(MonthDay monthDay) {
        Preconditions.checkNotNull(monthDay);

        return monthDay.equals(getLaborDay(getCurrentYear()));
    }

    /**
     * Returns whether {@link MonthDay#TODAY}} is Memorial day.
     *
     * @return whether {@link MonthDay#TODAY} }is Memorial day
     */
    public static boolean isMemorialDay() {
        return isMemorialDay(MonthDay.TODAY);
    }

    /**
     * Returns whether the provided day is Memorial day.
     *
     * @param monthDay the month day object
     * @return whether the provided day is Memorial day
     */
    public static boolean isMemorialDay(MonthDay monthDay) {
        Preconditions.checkNotNull(monthDay);

        return monthDay.equals(getMemorialDay(getCurrentYear()));
    }

    /**
     * Returns whether the provided date is the provided special day.
     *
     * @param monthDay    the month day to check for being the special day
     * @param specialDays the special day to compare to the provided date
     * @return whether the provided date is the provided special day
     */
    public static boolean isSpecialDay(MonthDay monthDay, SpecialDay specialDays) {
        Preconditions.checkNotNull(monthDay);
        Preconditions.checkNotNull(specialDays);

        return switch (specialDays) {
            case NEW_YEARS_DAY -> isNewYearsDay(monthDay);
            case GROUND_HOG_DAY -> isGroundHogDay(monthDay);
            case MARDI_GRASS_DAY -> isMardiGrassDay(monthDay);
            case EASTER -> isEaster(monthDay);
            case MEMORIAL_DAY -> isMemorialDay(monthDay);
            case INDEPENDENCE_DAY -> isIndependenceDay(monthDay);
            case LABOR_DAY -> isLaborDay(monthDay);
            case HALLOWEEN -> isHalloween(monthDay);
            case THANKSGIVING -> isThanksgiving(monthDay);
            case CHRISTMAS -> isChristmas(monthDay);
        };
    }

    /**
     * Returns the month day object for Memorial day for the provided year.
     *
     * @param year the year
     * @return the month day object for Memorial day for the provided year
     */
    public static MonthDay getMemorialDay(int year) {
        LocalDate memorialDate = LocalDate.of(year, mayMonth, 1)
                .with(TemporalAdjusters.lastInMonth(DayOfWeek.MONDAY));
        return new MonthDay(mayMonth, memorialDate.getDayOfMonth());
    }

    /**
     * Returns the labor day month day object for the provided year.
     *
     * @param year the year
     * @return the labor day month day object for the provided year
     */
    public static MonthDay getLaborDay(int year) {
        LocalDate laborDate = LocalDate.of(year, laborDayMonth, nthMondayLaborDay)
                .with(TemporalAdjusters.dayOfWeekInMonth(1, DayOfWeek.MONDAY));
        return new MonthDay(laborDayMonth, laborDate.getDayOfMonth());
    }


    /**
     * Returns the month day object for Mardi Grass for the provided year.
     *
     * @param year the year
     * @return the month day object for Mardi Grass for the provided year
     */
    public static MonthDay getMardiGrassDay(int year) {
        MonthDay easter = getEasterSundayDate(year);
        LocalDate easterLocalDate = LocalDate.of(year, easter.getMonth(), easter.getDate());
        LocalDate mardiGrassDate = easterLocalDate.minusDays(MARDI_GRASS_EASTER_SEPARATION_DAYS);
        return new MonthDay(mardiGrassDate.getMonthValue(), mardiGrassDate.getDayOfMonth());
    }

    /**
     * Returns the thanksgiving month date for the provided year.
     *
     * @param year the year to return the thanksgiving month date of
     * @return the thanksgiving month date for the provided year
     */
    public static MonthDay getThanksgiving(int year) {
        LocalDate thanksgivingDay = LocalDate.of(year, thanksgivingMonth, 1)
                .with(TemporalAdjusters.dayOfWeekInMonth(thanksgivingNthThursday, DayOfWeek.THURSDAY));

        return new MonthDay(thanksgivingMonth, thanksgivingDay.getDayOfMonth());
    }

    /**
     * Returns the month day object representing when easter sunday is for the provided year.
     *
     * @param year the year to find the date of easter sunday
     * @return the month day object representing when easter sunday is for the provided year
     */
    public static MonthDay getEasterSundayDate(int year) {
        Preconditions.checkArgument(year >= 0);

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
     * Returns the current year.
     *
     * @return the current year
     */
    public static int getCurrentYear() {
        return calendarInstance.get(Calendar.YEAR);
    }

    /**
     * Returns a string representing the date easter is on for the current year.
     *
     * @return a string representing the date easter is on for the current year
     */
    public static String getEasterSundayString() {
        MonthDay sundayDate = getEasterSundayDate(getCurrentYear());

        return monthFromNumber(sundayDate.getMonth())
                + CyderStrings.space + formatNumberSuffix(sundayDate.getDate());
    }

    /**
     * Returns the number formatted with a suffix proper to its value.
     * Example, 1 returns "1st".
     *
     * @param dateInMonth the date to format, this should typically be between
     *                    1 and 31 but technically this method can handle any value
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
        if (months.containsKey(monthNumber)) {
            return months.get(monthNumber);
        }

        throw new IllegalArgumentException("Invalid month ordinal: " + monthNumber);
    }

    /**
     * Returns whether the local time is past 6:00pm.
     *
     * @return whether the local time is past 6:00pm
     */
    public static boolean isEvening() {
        return EVENING_RANGE.contains(calendarInstance.get(Calendar.HOUR_OF_DAY));
    }

    /**
     * Returns whether the local time is before 12:00pm.
     *
     * @return whether the local time is before 12:00pm
     */
    public static boolean isMorning() {
        return MORNING_RANGE.contains(calendarInstance.get(Calendar.HOUR_OF_DAY));
    }

    /**
     * Returns whether the current time is between 12:00pm and 6:00pm.
     *
     * @return whether the current time is between 12:00pm and 6:00pm
     */
    public static boolean isAfterNoon() {
        return AFTERNOON_RANGE.contains(calendarInstance.get(Calendar.HOUR_OF_DAY));
    }

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

        if (ret.startsWith(CyderStrings.comma)) ret = ret.substring(1);
        if (ret.length() == 0) return 0 + MILLISECOND_ABBREVIATION;

        return StringUtil.getTrimmedText(ret).trim();
    }

    // ------------------
    // Conversion methods
    // ------------------

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
}
