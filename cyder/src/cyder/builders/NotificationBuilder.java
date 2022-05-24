package cyder.builders;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import cyder.enums.Direction;
import cyder.enums.NotificationDirection;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderNotification;
import cyder.utilities.ReflectionUtil;
import cyder.utilities.TimeUtil;

import javax.swing.*;

/**
 * Notification Builder for CyderFrame notifications as opposed to telescoping constructors.
 */
public final class NotificationBuilder {
    /**
     * The minimum allowable char length for a notification.
     */
    public static final int MINIMUM_TEXT_LENGTH = 2;

    // -------------------
    // Required parameters
    // -------------------

    /**
     * The html styled text to display.
     */
    private final String htmlText;

    // -------------------
    // Optional parameters
    // -------------------

    /**
     * The duration the notification should be visible for in ms not counting the animation period.
     */
    private int viewDuration = 5000;

    /**
     * The direction to draw the notification arrow.
     */
    private Direction arrowDir = Direction.TOP;

    /**
     * The runnable to invoke upon the notification being killed by a user.
     */
    private Runnable onKillAction;

    /**
     * The direction for the notification to appear/disappear from/to.
     */
    private NotificationDirection notificationDirection = NotificationDirection.TOP;

    /**
     * The type of notification, i.e. notification vs toast.
     */
    private CyderNotification.NotificationType notificationType = CyderNotification.NotificationType.NOTIFICATION;

    /**
     * The custom container for the notification. If this is not provided a label is generated
     * which holds the html styled text.
     */
    private JLabel container;

    /**
     * The time the notification was originally constructed at.
     */
    private final String notifyTime;

    /**
     * Default constructor for a Notification with the required parameters for the Notification.
     *
     * @param htmlText the html styled text to display
     */
    public NotificationBuilder(String htmlText) {
        Preconditions.checkNotNull(htmlText);
        Preconditions.checkArgument(htmlText.length() >= MINIMUM_TEXT_LENGTH,
                "HTML text length is less than " + MINIMUM_TEXT_LENGTH);

        this.htmlText = htmlText;

        notifyTime = TimeUtil.notificationTime();

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns the html text for the notification.
     *
     * @return the html text for the notification
     */
    public String getHtmlText() {
        return htmlText;
    }

    /**
     * Returns the view duration for the notification.
     *
     * @return the view duration for the notification
     */
    public int getViewDuration() {
        return viewDuration;
    }

    /**
     * Sets the view duration for the notification.
     *
     * @param viewDuration the view duration for the notification
     */
    public void setViewDuration(int viewDuration) {
        this.viewDuration = viewDuration;
    }

    /**
     * Returns the arrow direction for the notification.
     *
     * @return the arrow direction for the notification
     */
    public Direction getArrowDir() {
        return arrowDir;
    }

    /**
     * Sets the arrow direction for the notification.
     *
     * @param arrowDir the arrow direction for the notification
     */
    public void setArrowDir(Direction arrowDir) {
        this.arrowDir = arrowDir;
    }

    /**
     * Returns the on kill action for this notification.
     *
     * @return the on kill action for this notification
     */
    public Runnable getOnKillAction() {
        return onKillAction;
    }

    /**
     * Sets the on kill action for this notification.
     *
     * @param onKillAction the on kill action for this notification
     */
    public void setOnKillAction(Runnable onKillAction) {
        this.onKillAction = onKillAction;
    }

    /**
     * Returns the notification direction for this notification.
     *
     * @return the notification direction for this notification
     */
    public NotificationDirection getNotificationDirection() {
        return notificationDirection;
    }

    /**
     * Sets the notification direction for this notification.
     *
     * @param notificationDirection the notification direction for this notification
     */
    public void setNotificationDirection(NotificationDirection notificationDirection) {
        this.notificationDirection = notificationDirection;
    }

    /**
     * Returns the container for this notification.
     * This takes the place of the text container.
     *
     * @return the container for this notification
     */
    public JLabel getContainer() {
        return container;
    }

    /**
     * Sets the custom container for this notification.
     * This takes the place of the text container.
     *
     * @param container the JLabel container for this notification
     */
    public void setContainer(JLabel container) {
        Preconditions.checkNotNull(container);
        Preconditions.checkArgument(container.getWidth() > 0);
        Preconditions.checkArgument(container.getHeight() > 0);

        this.container = container;
    }

    /**
     * Returns the time at which this object was created.
     *
     * @return the time at which this object was created
     */
    public String getNotifyTime() {
        return notifyTime;
    }

    /**
     * Returns the notification type of this notification.
     *
     * @return the notification type of this notification
     */
    public CyderNotification.NotificationType getNotificationType() {
        return notificationType;
    }

    /**
     * Sets the notification type of this notification.
     *
     * @param notificationType the notification type of this notification
     */
    public void setNotificationType(CyderNotification.NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    // -----------------------------------------------
    // Methods to override according to Effective Java
    // -----------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NotificationBuilder that = (NotificationBuilder) o;

        return viewDuration == that.viewDuration
                && notifyTime.equals(that.notifyTime)
                && Objects.equal(htmlText, that.htmlText)
                && arrowDir == that.arrowDir
                && Objects.equal(onKillAction, that.onKillAction)
                && notificationDirection == that.notificationDirection
                && notificationType == that.notificationType
                && Objects.equal(container, that.container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(
                viewDuration,
                notifyTime,
                htmlText,
                arrowDir,
                onKillAction,
                notificationDirection,
                notificationType,
                container);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
