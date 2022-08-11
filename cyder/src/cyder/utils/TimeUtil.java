package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.ThreadUtil;
import cyder.user.UserUtil;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Static utility class for things related to time/date queries and conversions.
 */
public final class TimeUtil {
    /**
     * The calendar instance to use for calculations.
     */
    public static final Calendar calendarInstance = Calendar.getInstance();

    /**
     * Instantiation of TimeUtil class is not allowed.
     */
    private TimeUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The date formatter to use when the weather time is requested.
     */
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

    /**
     * The date formatter to use when the log sub dir time is requested.
     */
    public static final SimpleDateFormat logSubDirFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Returns the time used for log subdirectories.
     *
     * @return the time used for log subdirectories
     */
    public static String logSubDirTime() {
        return getFormattedTime(logSubDirFormat);
    }

    /**
     * The date formatter to use when the year is requested.
     */
    public static final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

    /**
     * Returns the current year in the format "yyyy".
     *
     * @return the current year in the format "yyyy"
     */
    public static int getYear() {
        return Integer.parseInt(getFormattedTime(yearFormat));
    }

    /**
     * The date formatter to use when the log line time is requested.
     */
    public static final SimpleDateFormat LOG_TIME_FORMAT = new SimpleDateFormat("HH-mm-ss");

    /**
     * Returns the time used for log files.
     *
     * @return the time used for log files
     */
    public static String logTime() {
        return getFormattedTime(LOG_TIME_FORMAT);
    }

    /**
     * The date formatter used for when a log line is being written to the log file.
     */
    public static final SimpleDateFormat LOG_LINE_TIME_FORMAT = new SimpleDateFormat("HH-mm-ss.SSS");

    /**
     * The time used for lines of log files.
     *
     * @return time used for lines of log files
     */
    public static String getLogLineTime() {
        return getFormattedTime(LOG_LINE_TIME_FORMAT);
    }

    /**
     * The date formatter to use when formatting a date object to the notified at time.
     */
    public static final SimpleDateFormat notificationFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * Returns the time used for determining what time notifications were originally added to the queue at.
     *
     * @return the time used for determining what time notifications were originally added to the queue at
     */
    public static String notificationTime() {
        return getFormattedTime(notificationFormat);
    }

    /**
     * The date formatter to use when formatting a date object to the console clock time format.
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static final SimpleDateFormat userFormat
            = new SimpleDateFormat("EEEEEEEEE, MM/dd/yyyy hh:mmaa zzz");

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

        return getTime(UserUtil.getCyderUser().getConsoleclockformat());
    }

    /**
     * The date formatter to use when formatting a date object to the console clock time format.
     */
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

    /**
     * The date formatter to use when formatting a date object to the console clock time format without seconds.
     */
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
        int year = calendarInstance.get(Calendar.YEAR);
        int Month = calendarInstance.get(Calendar.MONTH) + 1;
        int Date = calendarInstance.get(Calendar.DATE);
        LocalDate RealTG = LocalDate.of(year, 11, 1)
                .with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY));
        return (Month == 11 && Date == RealTG.getDayOfMonth());
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

    /**
     * A record used to represent a month and date such as June 2nd.
     */
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
        return monthFromNumber(sundayDate.month) + " " + formatNumberSuffix(sundayDate.date);
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

    public static final double MILLISECONDS_IN_SECOND = 1000;
    public static final double SECONDS_IN_MINUTE = 60.0;
    public static final double MINUTES_IN_HOUR = 60.0;
    public static final double HOURS_IN_DAY = 24.0;
    public static final double DAYS_IN_MONTH = 30.0;
    public static final double MONTHS_IN_YEAR = 12.0;

    /**
     * Returns a string detailing how many years/months/days/hours/minutes/seconds
     * are represented by the given input parameter.
     *
     * @param milliseconds the number of milliseconds to convert
     * @return a String detailing how many years/months/days...
     * are represented by the provided milliseconds.
     */
    public static String millisToFormattedString(long milliseconds) {
        StringBuilder sb = new StringBuilder();

        double years = 0;
        double months = 0;
        double days = 0;
        double hours = 0;
        double minutes = 0;
        double seconds = 0;

        // convert milliseconds to seconds
        if (milliseconds >= MILLISECONDS_IN_SECOND) {
            seconds = Math.floor(milliseconds / MILLISECONDS_IN_SECOND);
        }

        // take away milliseconds that were converted
        milliseconds -= seconds * MILLISECONDS_IN_SECOND;

        // convert seconds to minutes
        if (seconds >= SECONDS_IN_MINUTE) {
            minutes = Math.floor(seconds / SECONDS_IN_MINUTE);
        }

        // take away seconds that were converted
        seconds -= minutes * SECONDS_IN_MINUTE;

        // convert minutes to hours
        if (minutes >= MINUTES_IN_HOUR) {
            hours = Math.floor(minutes / MINUTES_IN_HOUR);
        }

        // take a way minutes that were converted
        minutes -= hours * MINUTES_IN_HOUR;

        // convert hours to days
        if (hours >= HOURS_IN_DAY) {
            days = Math.floor(hours / HOURS_IN_DAY);
        }

        // take away hours that were converted
        hours -= days * HOURS_IN_DAY;

        // convert days to months
        if (days >= DAYS_IN_MONTH) {
            months = Math.floor(days / DAYS_IN_MONTH);
        }

        // take away days that were converted
        days -= months * DAYS_IN_MONTH;

        // convert months to years
        if (months >= MONTHS_IN_YEAR) {
            years = Math.floor(months / MONTHS_IN_YEAR);
        }

        // take away months that were converted
        months -= years * MONTHS_IN_YEAR;

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
        if (milliseconds != 0)
            sb.append(format.format(milliseconds)).append("ms ");

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

    /**
     * The time at which Cyder was first started.
     */
    private static long absoluteStartTime;

    /**
     * The time at which the console first appeared.
     */
    private static long consoleStartTime;

    /**
     * Returns the absolute start time of Cyder.
     *
     * @return the absolute start time of Cyder
     */
    public static long getAbsoluteStartTime() {
        return absoluteStartTime;
    }

    /**
     * Sets the absolute start time of Cyder.
     *
     * @param absoluteStartTime the absolute start time of Cyder
     */
    public static void setAbsoluteStartTime(long absoluteStartTime) {
        if (TimeUtil.absoluteStartTime != 0)
            throw new IllegalArgumentException("Absolute Start Time already set");

        TimeUtil.absoluteStartTime = absoluteStartTime;
    }

    /**
     * Returns the time at which the console first appeared visible.
     * This is not affected by a user logout and successive login.
     *
     * @return the time at which the console first appeared visible
     */
    public static long getConsoleStartTime() {
        return consoleStartTime;
    }

    /**
     * Sets the time the console was shown.
     *
     * @param consoleStartTime the time the console was shown
     */
    public static void setConsoleStartTime(long consoleStartTime) {
        if (TimeUtil.consoleStartTime != 0)
            return;

        TimeUtil.consoleStartTime = consoleStartTime;
    }

    /**
     * Sleeps on the current thread for the specified amount of time,
     * checking the escapeCondition for truth every checkConditionFrequency ms.
     *
     * @param sleepTime               the total time to sleep for
     * @param checkConditionFrequency the frequency to check the escapeCondition
     * @param escapeCondition         the condition to stop sleeping if true
     */
    public static void sleepWithChecks(long sleepTime,
                                       long checkConditionFrequency, AtomicBoolean escapeCondition) {
        Preconditions.checkNotNull(escapeCondition);
        Preconditions.checkArgument(sleepTime > 0);
        Preconditions.checkArgument(checkConditionFrequency > 0);
        Preconditions.checkArgument(sleepTime > checkConditionFrequency);

        long acc = 0;

        while (acc < sleepTime) {
            ThreadUtil.sleep(checkConditionFrequency);
            acc += checkConditionFrequency;

            if (escapeCondition.get()) {
                break;
            }
        }
    }

    /**
     * Sleeps on the current thread for the specified amount of time,
     * checking the escapeCondition for truth every checkConditionFrequency ms.
     *
     * @param sleepTime               the total time to sleep for
     * @param checkConditionFrequency the frequency to check the escapeCondition
     * @param shouldExit              the function to evaluate to determine whether to stop sleeping
     */
    public static void sleepWithChecks(long sleepTime, long checkConditionFrequency,
                                       Function<Void, Boolean> shouldExit) {
        Preconditions.checkNotNull(shouldExit);
        Preconditions.checkArgument(sleepTime > 0);
        Preconditions.checkArgument(checkConditionFrequency > 0);
        Preconditions.checkArgument(sleepTime > checkConditionFrequency);

        long acc = 0;

        while (acc < sleepTime) {
            ThreadUtil.sleep(checkConditionFrequency);
            acc += checkConditionFrequency;

            if (shouldExit.apply(null)) {
                break;
            }
        }
    }

    private static final String MOON_PHASE_URL = "https://www.timeanddate.com/moon/phases/";
    private static final String CURRENT_MOON_ID = "cur-moon";
    private static final String CURRENT_MOON_PERCENT_ID = "cur-moon-percent";
    private static final String PHASE_ID = "qlook";
    private static final String SRC = "SRC";
    private static final String IMG = "img";
    private static final String A_TAG = "a";

    /**
     * A record representing a moon phase as defined by stats from
     * <a href="https://www.moongiant.com/phase/today/">moongiant</a>
     */
    public record MoonPhase(String phase, double illumination, String urlImage) {}

    /**
     * Returns the current moon phase.
     *
     * @return the current moon phase if present. Empty optional else
     * @throws IllegalCallerException if any of the following cannot be found: phase, illumination, url image
     */
    public static Optional<MoonPhase> getCurrentMoonPhase() {
        String phase = null;
        double illumination = -1;
        String urlImage = null;

        try {
            Document doc = Jsoup.connect(MOON_PHASE_URL).get();

            // Getting image
            Element moonImageContainer = doc.getElementById(CURRENT_MOON_ID);
            if (moonImageContainer != null) {
                Elements moonImageElements = moonImageContainer.getAllElements();

                if (moonImageElements.size() > 0) {
                    Element imageElement = moonImageElements.get(0).select(IMG).first();

                    if (imageElement != null) {
                        urlImage = imageElement.absUrl(SRC);
                    }
                }
            }

            // Getting illumination
            Element moonPercentElement = doc.getElementById(CURRENT_MOON_PERCENT_ID);
            if (moonPercentElement != null) {
                illumination = Double.parseDouble(moonPercentElement.text().replace("%", ""));
            }

            // Getting phase
            Element phaseElement = doc.getElementById(PHASE_ID);
            if (phaseElement != null) {
                phase = phaseElement.select(A_TAG).text();
            }
        } catch (HttpStatusException e) {
            return Optional.empty();
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
            return Optional.empty();
        }

        if (phase == null) {
            throw new IllegalCallerException("Could not find phase");
        } else if (illumination == -1) {
            throw new IllegalCallerException("Could not find illumination");
        } else if (urlImage == null) {
            throw new IllegalCallerException("Could not find url image");
        }

        return Optional.of(new MoonPhase(phase, illumination, urlImage));
    }
}
