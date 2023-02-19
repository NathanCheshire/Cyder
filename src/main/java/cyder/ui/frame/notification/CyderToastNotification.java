package cyder.ui.frame.notification;

import com.google.common.base.Preconditions;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A toast notification, similar to the Android API's toast notifications.
 * The toast has no direction arrow painted on it and performs an opacity
 * fade-in and fade-out animation at the bottom center of the frame.
 */
public class CyderToastNotification extends CyderNotificationAbstract {
    /**
     * Whether this toast notification has been killed.
     */
    private final AtomicBoolean killed = new AtomicBoolean();

    /**
     * Constructs a new toast notification.
     *
     * @param builder the builder to construct this toast notification from
     */
    public CyderToastNotification(NotificationBuilder builder) {
        Preconditions.checkNotNull(builder);

        // todo params
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paintLogic(Graphics g) {
        Preconditions.checkNotNull(g);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void appear() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disappear() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void kill() {
        killed.set(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isKilled() {
        return killed.get();
    }
}
