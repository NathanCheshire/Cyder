package cyder.ui.frame.notification;

import cyder.enumerations.Direction;

/**
 * The supported locations for {@link CyderNotification}s.
 */
public enum NotificationDirection {
    /**
     * The notification will appear at the top left of the frame.
     */
    TOP_LEFT,

    /**
     * The notification will appear at the top center of the frame.
     */
    TOP,

    /**
     * The notification will appear at the top right of the frame.
     */
    TOP_RIGHT,

    /**
     * The notification will appear on the left of the frame.
     */
    LEFT,

    /**
     * The notification will appear on the right of the frame.
     */
    RIGHT,

    /**
     * The notification will appear at the bottom left of the frame.
     */
    BOTTOM_LEFT,

    /**
     * The notification will appear at the bottom of the frame.
     */
    BOTTOM,

    /**
     * The notification will appear at the bottom right of the frame.
     */
    BOTTOM_RIGHT;

    /**
     * Returns the arrow direction for this notification direction.
     *
     * @return the arrow direction for this notification direction
     */
    public Direction getArrowDirection() {
        return switch (this) {
            case TOP_LEFT, LEFT, BOTTOM_LEFT -> Direction.LEFT;
            case TOP -> Direction.TOP;
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> Direction.RIGHT;
            case BOTTOM -> Direction.BOTTOM;
        };
    }
}
