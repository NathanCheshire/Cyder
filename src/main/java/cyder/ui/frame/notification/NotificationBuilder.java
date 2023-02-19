package cyder.ui.frame.notification;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.time.TimeUtil;

import javax.swing.*;
import java.time.Duration;

import static cyder.strings.CyderStrings.quote;

/**
 * A builder for a {@link CyderNotificationAbstract}.
 */
public final class NotificationBuilder {
    /**
     * The magic number used to denote a notification should be shown until dismissed.
     */
    private static final int showUntilDismissed = -1;

    /**
     * The default view duration.
     */
    private static final Duration defaultViewDuration = Duration.ofSeconds(5);

    /**
     * The html styled text to display.
     */
    private final String htmlText;

    /**
     * The duration the notification should be visible for in ms not counting the animation period.
     */
    private long viewDuration = defaultViewDuration.toMillis();

    /**
     * The runnable to invoke upon the notification being killed by a user.
     */
    private Runnable onKillAction;

    /**
     * The direction for the notification to appear/disappear from/to.
     */
    private NotificationDirection notificationDirection = NotificationDirection.TOP;

    /**
     * The custom container for the notification. If this is not provided a label is generated
     * which holds the html styled text.
     */
    private JLabel container;

    /**
     * Whether the view duration should be auto-calculated.
     */
    private boolean shouldCalculateViewDuration;

    /**
     * The time the notification was originally constructed at.
     */
    private final String constructionTime;

    /**
     * Default constructor for a Notification with the required parameters for the Notification.
     *
     * @param htmlText the html styled text to display
     */
    public NotificationBuilder(String htmlText) {
        Preconditions.checkNotNull(htmlText);
        Preconditions.checkArgument(!htmlText.isEmpty());

        this.htmlText = htmlText;

        constructionTime = TimeUtil.notificationTime();

        Logger.log(LogTag.OBJECT_CREATION, this);
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
    public long getViewDuration() {
        return viewDuration;
    }

    /**
     * Sets the view duration for the notification.
     *
     * @param viewDuration the view duration for the notification
     * @return this NotificationBuilder
     */
    @CanIgnoreReturnValue
    public NotificationBuilder setViewDuration(long viewDuration) {
        this.viewDuration = viewDuration;
        return this;
    }

    /**
     * Sets the view duration to {@link #showUntilDismissed} to indicate the notification
     * should be shown until dismissed by a user.
     *
     * @return this NotificationBuilder
     */
    @CanIgnoreReturnValue
    public NotificationBuilder setShowNotificationUntilDismissed() {
        this.viewDuration = showUntilDismissed;
        return this;
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
     * @return this NotificationBuilder
     */
    @CanIgnoreReturnValue
    public NotificationBuilder setOnKillAction(Runnable onKillAction) {
        this.onKillAction = Preconditions.checkNotNull(onKillAction);
        return this;
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
     * @return this NotificationBuilder
     */
    @CanIgnoreReturnValue
    public NotificationBuilder setNotificationDirection(NotificationDirection notificationDirection) {
        this.notificationDirection = Preconditions.checkNotNull(notificationDirection);
        return this;
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
     * @return this NotificationBuilder
     */
    public NotificationBuilder setContainer(JLabel container) {
        Preconditions.checkNotNull(container);
        Preconditions.checkArgument(container.getWidth() > 0);
        Preconditions.checkArgument(container.getHeight() > 0);

        this.container = container;
        return this;
    }

    /**
     * Returns the time at which this object was created.
     *
     * @return the time at which this object was created
     */
    public String getConstructionTime() {
        return constructionTime;
    }

    /**
     * Returns whether the view duration should be auto-calculated.
     *
     * @return whether the view duration should be auto-calculated
     */
    public boolean shouldCalculateViewDuration() {
        return shouldCalculateViewDuration;
    }

    /**
     * Sets whether the view duration should be auto-calculated.
     *
     * @param shouldCalculateViewDuration whether the view duration should be auto-calculated
     * @return this builder
     */
    @CanIgnoreReturnValue
    public NotificationBuilder setShouldCalculateViewDuration(boolean shouldCalculateViewDuration) {
        this.shouldCalculateViewDuration = shouldCalculateViewDuration;
        return this;
    }

    /**
     * Returns whether the provided duration is indicative of a notification
     * which should remain visible until dismissed by a user.
     *
     * @param duration the duration
     * @return whether the provided duration is indicative of a notification
     * which should remain visible until dismissed by a user
     */
    // todo remove
    public static boolean shouldRemainVisibleUntilDismissed(long duration) {
        return duration == showUntilDismissed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof NotificationBuilder)) {
            return false;
        }

        NotificationBuilder other = (NotificationBuilder) o;

        return viewDuration == other.viewDuration
                && constructionTime.equals(other.constructionTime)
                && Objects.equal(htmlText, other.htmlText)
                && Objects.equal(onKillAction, other.onKillAction)
                && Objects.equal(notificationDirection, other.notificationDirection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Long.hashCode(viewDuration);
        ret = 31 * ret + constructionTime.hashCode();
        ret = 31 * ret + htmlText.hashCode();
        ret = 31 * ret + notificationDirection.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "NotificationBuilder{"
                + "htmlText=" + quote + htmlText + quote
                + ", viewDuration=" + viewDuration
                + ", onKillAction=" + onKillAction
                + ", notificationDirection=" + notificationDirection
                + ", container=" + container
                + ", calculateViewDuration=" + shouldCalculateViewDuration
                + ", notifyTime=" + quote + constructionTime + quote
                + "}";
    }
}
