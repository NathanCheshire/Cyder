package cyder.time;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;

/**
 * A class used to represent a month and date such as July 4th.
 */
@Immutable
public final class MonthDay {
    /**
     * The month number.
     */
    private final int month;

    /**
     * The date number.
     */
    private final int date;

    /**
     * Constructs a new month day object.
     *
     * @param month the month number starting at 1 for January
     * @param date  the date of the month
     */
    public MonthDay(int month, int date) {
        Preconditions.checkArgument(month > 0 && month <= 12);
        Preconditions.checkArgument(date > 0 && date <= 31);

        this.month = month;
        this.date = date;
    }

    /**
     * Returns the month number.
     *
     * @return the month number
     */
    public int getMonth() {
        return month;
    }

    /**
     * Returns the date number.
     *
     * @return the date number
     */
    public int getDate() {
        return date;
    }

    /**
     * Returns the month String for the month.
     *
     * @return the month String for the month.
     */
    public String getMonthString() {
        return TimeUtil.months.get(month);
    }

    /**
     * Returns the date String for the month.
     * For example, if date were "5", "5th" would be returned.
     *
     * @return the date String for the month
     */
    public String getDateString() {
        return TimeUtil.formatNumberSuffix(date);
    }

    /**
     * Returns the month date string.
     * This is the result of concatenating {@link #getMonthString()}, " and ",
     * and {@link #getDateString()}.
     *
     * @return the month date string
     */
    public String getMonthDateString() {
        return getMonthString() + " the " + getDateString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "MonthDay{"
                + "month=" + month
                + ", date=" + date
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(month);
        ret = 31 * ret + Integer.hashCode(date);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof MonthDay)) {
            return false;
        }

        MonthDay other = (MonthDay) o;
        return other.date == date
                && other.month == month;
    }
}
