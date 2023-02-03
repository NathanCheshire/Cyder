package cyder.time;

import com.google.common.base.Preconditions;

import static cyder.time.TimeUtil.*;

/**
 * Common units of time and conversions between them.
 */
public enum TimeUnit {
    MILLISECONDS,
    SECONDS,
    MINUTES,
    HOURS,
    DAYS,
    WEEKS,
    MONTHS,
    YEARS;

    /**
     * Converts the provided number of units to milliseconds.
     *
     * @param units the number of units
     * @return the number of milliseconds representing the provided quantity of units
     */
    public long toMillis(long units) {
        Preconditions.checkArgument(units >= 0);

        return switch (this) {
            case MILLISECONDS -> units;
            case SECONDS -> (long) (units * millisInSecond);
            case MINUTES -> (long) (units * millisInSecond * secondsInMinute);
            case HOURS -> (long) (units * millisInSecond * secondsInMinute * minutesInHour);
            case DAYS -> (long) (units * millisInSecond * secondsInMinute * minutesInHour * hoursInDay);
            case WEEKS -> (long) (units * millisInSecond * secondsInMinute * minutesInHour * hoursInDay * daysInWeek);
            case MONTHS -> (long) (units * millisInSecond * secondsInMinute * minutesInHour * hoursInDay * daysInMonth);
            case YEARS -> (long) (units * millisInSecond * secondsInMinute * minutesInHour * hoursInDay * daysInYear);
        };
    }

    /**
     * Converts the provided number of milliseconds to this unit.
     *
     * @param millis the number of milliseconds
     * @return the provided number of milliseconds converted to this unit
     */
    public long fromMillis(long millis) {
        Preconditions.checkArgument(millis >= 0);

        return switch (this) {
            case MILLISECONDS -> millis;
            case SECONDS -> (long) (millis / millisInSecond);
            case MINUTES -> (long) (millis / millisInSecond / secondsInMinute);
            case HOURS -> (long) (millis / millisInSecond / secondsInMinute / minutesInHour);
            case DAYS -> (long) (millis / millisInSecond / secondsInMinute / minutesInHour / hoursInDay);
            case WEEKS -> (long) (millis / millisInSecond / secondsInMinute / minutesInHour / hoursInDay / daysInWeek);
            case MONTHS -> (long) (millis / millisInSecond / secondsInMinute
                    / minutesInHour / hoursInDay / daysInMonth);
            case YEARS -> (long) (millis / millisInSecond / secondsInMinute / minutesInHour / hoursInDay / daysInYear);
        };
    }
}
