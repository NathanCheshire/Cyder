package cyder.ui.objects;

import cyder.enums.Direction;
import cyder.enums.NotificationDirection;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderFrame;
import cyder.utilities.ReflectionUtil;

import java.awt.*;

/**
 * Class for storing a notification's information.
 */
public final class QueuedNotification {
    private String htmlText;
    private int duration;
    private Direction arrowDir;
    private NotificationDirection notificationDirection;
    private CyderFrame.ClickAction onKillAction;
    private String time;
    private Container contianer;
    private Color notificationBackground;

    /**
     * A notification that hasn't been notified to the user yet
     * and is waiting in the CyderFrame's notification queue.
     *
     * @param text the html text for the eventual notification to display
     * @param dur the duration in miliseconds the notification should last for. Use 0 for auto-calculation
     * @param arrowDir the arrow direction
     * @param notificationDirection the notification direction
     * @param onKillAction the action to perform if the notification is dismissed by the user
     */
    public QueuedNotification(String text, int dur, Direction arrowDir,
                              NotificationDirection notificationDirection,
                              CyderFrame.ClickAction onKillAction, Container container,
                              Color notificationBackground, String time) {
        htmlText = text;
        duration = dur;
        contianer = container;

        this.arrowDir = arrowDir;
        this.notificationDirection = notificationDirection;
        this.onKillAction = onKillAction;
        this.notificationBackground = notificationBackground;
        this.time = time;

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    public void setHtmlText(String htmlText) {
        this.htmlText = htmlText;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setArrowDir(Direction arrowDir) {
        this.arrowDir = arrowDir;
    }

    public void setNotificationDirection(NotificationDirection notificationDirection) {
        this.notificationDirection = notificationDirection;
    }

    public String getHtmlText() {
        return htmlText;
    }

    public int getDuration() {
        return duration;
    }

    public Direction getArrowDir() {
        return arrowDir;
    }

    public NotificationDirection getNotificationDirection() {
        return notificationDirection;
    }

    public CyderFrame.ClickAction getOnKillAction() {
        return onKillAction;
    }

    public void setOnKillAction(CyderFrame.ClickAction onKillAction) {
        this.onKillAction = onKillAction;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Container getContianer() {
        return contianer;
    }

    public void setContianer(Container contianer) {
        this.contianer = contianer;
    }

    public Color getNotificationBackground() {
        return notificationBackground;
    }

    public void setNotificationBackground(Color notificaitonBackground) {
        notificationBackground = notificaitonBackground;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = htmlText.hashCode();
        ret = 31 * ret + Integer.hashCode(duration);
        ret = 31 * ret + arrowDir.hashCode();
        ret = 31 * ret + notificationDirection.hashCode();
        ret = 31 * ret + onKillAction.hashCode();
        ret = 31 * ret + time.hashCode();
        ret = 31 * ret + contianer.hashCode();
        ret = 31 * ret + notificationBackground.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof QueuedNotification)) {
            return false;
        } else {
            //guaranteed to succeed
            QueuedNotification otherNotification = (QueuedNotification) o;

            return htmlText.equals(otherNotification.htmlText)
                    && duration == otherNotification.duration
                    && arrowDir == otherNotification.arrowDir
                    && notificationDirection == otherNotification.notificationDirection
                    && onKillAction == otherNotification.onKillAction
                    && time.equals(otherNotification.time)
                    && contianer == otherNotification.contianer
                    && notificationBackground == otherNotification.notificationBackground;
        }
    }
}
