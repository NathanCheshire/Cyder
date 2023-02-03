package cyder.time;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Special days, holidays even.
 */
public enum SpecialDay {
    NEW_YEARS_DAY("Happy New Years!"),
    GROUND_HOG_DAY("Six more weeks?"),
    MARDI_GRASS_DAY("Happy Mardi Grass!"),
    EASTER("Happy Easter!"),
    MEMORIAL_DAY("Happy Memorial Day! Thank you to all those who support our Nation"),
    INDEPENDENCE_DAY("Happy Independence Day!"),
    LABOR_DAY("Happy Labor Day!"),
    HALLOWEEN("Happy Halloween!"),
    THANKSGIVING("Happy Thanksgiving!"),
    CHRISTMAS("Merry Christmas");

    /**
     * The notification message for this special day
     */
    private final String notificationMessage;

    SpecialDay(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    /**
     * Returns the notification message for this special day.
     *
     * @return the notification message for this special day
     */
    public String getNotificationMessage() {
        return notificationMessage;
    }

    /**
     * Returns whether this special day is today.
     *
     * @return whether this special day is today
     */
    public boolean isToday() {
        return TimeUtil.isSpecialDay(MonthDay.TODAY, this);
    }

    /**
     * Returns the special days of today.
     *
     * @return the special days of today
     */
    public static ImmutableList<SpecialDay> getSpecialDaysOfToday() {
        return ImmutableList.copyOf(Arrays.stream(values()).filter(SpecialDay::isToday).collect(Collectors.toList()));
    }
}
