package cyder.ui.frame.notification;

/**
 * An interface for the necessary methods a {@link CyderNotificationAbstract} must implement.
 */
public interface ICyderNotification {
    /**
     * Causes the notification to appear on the frame.
     */
    void appear();

    /**
     * Causes the notification to disappear from the frame.
     */
    void disappear();

    /**
     * Kills the animation and removes this from the parent frame.
     */
    void kill();

    /**
     * Returns whether this notification has been killed.
     * A kill may be invoked via {@link #kill()}.
     *
     * @return whether this notification has been killed
     */
    boolean isKilled();

    /**
     * Sets whether this notification should be painted as hovered.
     *
     * @param hovered whether this notification should be painted as hovered
     */
    void setHovered(boolean hovered);
}
