package cyder.parsers.remote.ip;

import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.util.Objects;

/**
 * An object for parsing returned ip data timezone objects.
 */
public class TimeZone {
    /**
     * The name of the timezone.
     */
    private String name;

    /**
     * The abbreviation of the timezone.
     */
    private String abbr;

    /**
     * The UTC offset of the Timezone.
     */
    private String offset;

    /**
     * Whether or not Daylight Savings have been accounted for.
     */
    private boolean is_dst;

    /**
     * The exact current time in the Timezone the IP Address belongs to adjusted for Daylight Savings.
     */
    private String current_time;

    /**
     * Creates a new timezone object.
     */
    public TimeZone() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the name of the timezone.
     *
     * @return the name of the timezone
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the abbreviation of the timezone.
     *
     * @return the abbreviation of the timezone
     */
    public String getAbbr() {
        return abbr;
    }

    /**
     * Returns the UTC offset of the Timezone.
     *
     * @return the UTC offset of the Timezone.
     */
    public String getOffset() {
        return offset;
    }

    /**
     * Returns whether or not Daylight Savings have been accounted for.
     *
     * @return whether or not Daylight Savings have been accounted for
     */
    public boolean isIs_dst() {
        return is_dst;
    }

    /**
     * Returns the exact current time in the Timezone the IP Address belongs to adjusted for Daylight Savings.
     *
     * @return the exact current time in the Timezone the IP Address belongs to adjusted for Daylight Savings
     */
    public String getCurrent_time() {
        return current_time;
    }

    /**
     * Sets the name of the timezone.
     *
     * @param name the name of the timezone
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the abbreviation of the timezone.
     *
     * @param abbr the abbreviation of the timezone
     */
    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    /**
     * Sets the UTC offset of the Timezone.
     *
     * @param offset the UTC offset of the Timezone.
     */
    public void setOffset(String offset) {
        this.offset = offset;
    }

    /**
     * Sets whether or not Daylight Savings have been accounted for.
     *
     * @param is_dst whether or not Daylight Savings have been accounted for
     */
    public void setIs_dst(boolean is_dst) {
        this.is_dst = is_dst;
    }

    /**
     * Sets the exact current time in the Timezone the IP Address belongs to adjusted for Daylight Savings.
     *
     * @param current_time the exact current time in the Timezone the IP Address belongs to adjusted for Daylight Savings
     */
    public void setCurrent_time(String current_time) {
        this.current_time = current_time;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof TimeZone)) {
            return false;
        }

        TimeZone other = (TimeZone) o;

        return Objects.equals(name, other.name)
                && Objects.equals(abbr, other.abbr)
                && Objects.equals(offset, other.offset)
                && Objects.equals(is_dst, other.is_dst)
                && Objects.equals(current_time, other.current_time);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = 0;

        if (name != null) ret = 31 * ret + name.hashCode();
        if (abbr != null) ret = 31 * ret + abbr.hashCode();
        if (offset != null) ret = 31 * ret + offset.hashCode();
        ret = 31 * ret + Boolean.hashCode(is_dst);
        if (current_time != null) ret = 31 * ret + current_time.hashCode();

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TimeZone{"
                + "name=\"" + name + "\", "
                + "abbr=\"" + abbr + "\", "
                + "offset=\"" + offset + "\", "
                + "is_dst=" + is_dst + ", "
                + "current_time=\"" + current_time + "\""
                + "}";
    }
}
