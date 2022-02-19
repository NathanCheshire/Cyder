package cyder.ui.objects;

import cyder.enums.Direction;
import cyder.enums.NotificationDirection;
import cyder.ui.CyderFrame;

import java.awt.*;

/**
 * Notification Builder for CyderFrame notifications as opposed to telescoping constructors.
 */
public final class NotificationBuilder {
    /**
     * The minimum allowable char length for a notification.
     */
    public static final int MINIMUM_TEXT_LENGTH = 2;

    //required params
    private final String htmlText;

    //optional params
    private int viewDuration = 5000;
    private Direction arrowDir = Direction.TOP;
    private CyderFrame.ClickAction onKillAction = null;
    private NotificationDirection notificationDirection = NotificationDirection.TOP;
    private Container container = null;
    private Color notificationBackground = null;

    /**
     * Default constructor for a Notification with the required parameters for the Notification.
     *
     * @param htmlText the html styled text to display
     */
    public NotificationBuilder(String htmlText) {
        if (htmlText == null || htmlText.length() < MINIMUM_TEXT_LENGTH)
            throw new IllegalArgumentException("Html text is null or less than " + MINIMUM_TEXT_LENGTH + " chars");

        this.htmlText = htmlText;
    }

    public void setViewDuration(int viewDuration) {
        this.viewDuration = viewDuration;
    }

    public void setArrowDir(Direction arrowDir) {
        this.arrowDir = arrowDir;
    }

    public void setOnKillAction(CyderFrame.ClickAction onKillAction) {
        this.onKillAction = onKillAction;
    }

    public void setNotificationDirection(NotificationDirection notificationDirection) {
        this.notificationDirection = notificationDirection;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public void setNotificationBackground(Color notificationBackground) {
        this.notificationBackground = notificationBackground;
    }

    public String getHtmlText() {
        return htmlText;
    }

    public int getViewDuration() {
        return viewDuration;
    }

    public Direction getArrowDir() {
        return arrowDir;
    }

    public CyderFrame.ClickAction getOnKillAction() {
        return onKillAction;
    }

    public NotificationDirection getNotificationDirection() {
        return notificationDirection;
    }

    public Container getContainer() {
        return container;
    }

    public Color getNotificationBackground() {
        return notificationBackground;
    }
}
