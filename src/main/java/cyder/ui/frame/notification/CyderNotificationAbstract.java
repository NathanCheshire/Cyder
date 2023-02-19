package cyder.ui.frame.notification;

import cyder.ui.frame.CyderFrame;

import java.awt.*;

/**
 * A notification for a {@link CyderFrame}.
 */
public abstract class CyderNotificationAbstract {
    /**
     * The logic to invoke to paint the custom notification.
     *
     * @param g the graphics object for painting
     */
    public abstract void paintLogic(Graphics g);

    /**
     * Causes the notification to appear on the frame.
     */
    public abstract void appear();

    /**
     * Causes the notification to disappear from the frame.
     */
    public abstract void disappear();

    /**
     * Kills the animation and removes this from the parent frame.
     */
    public abstract void kill();

    /**
     * Returns whether this notification has been killed.
     * A kill may be invoked via {@link #kill()}.
     *
     * @return whether this notification has been killed
     */
    public abstract boolean isKilled();
}
