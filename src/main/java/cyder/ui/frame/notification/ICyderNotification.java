package cyder.ui.frame.notification;

import java.util.Optional;

/**
 * An interface for the necessary methods a {@link CyderNotification} must implement.
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

    /**
     * Returns the text this notification is displaying text if present.
     * An empty optional indicates this notification is displaying a custom container.
     *
     * @return whether the text this notification is displaying if present
     */
    Optional<String> getLabelText();

    /**
     * Sets the position of this notification to the start/end of the animation.
     */
    void setToStartAndEndingPosition();

    /**
     * Sets the location of this notification to the middle point of the animation.
     * That is, the point where the enter animation is completed and the notification is waiting
     * to invoke disappear.
     */
    void setToMidAnimationPosition();

    /**
     * Returns whether this notification is currently in the middle of an animation.
     *
     * @return whether this notification is currently in the middle of an animation
     */
    boolean isAnimating();

    /**
     * Returns the result of invoking toString() on the container.
     *
     * @return the result of invoking toString() on the container
     */
    String getContainerToString();
}
