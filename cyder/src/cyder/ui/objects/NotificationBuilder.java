package cyder.ui.objects;

import com.google.common.base.Preconditions;
import cyder.enums.Direction;
import cyder.enums.LoggerTag;
import cyder.enums.NotificationDirection;
import cyder.handlers.internal.Logger;
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
    private CyderFrame.ClickAction onKillAction;
    private NotificationDirection notificationDirection = NotificationDirection.TOP;
    private Container container;
    private Color notificationBackground;

    /**
     * Default constructor for a Notification with the required parameters for the Notification.
     *
     * @param htmlText the html styled text to display
     */
    public NotificationBuilder(String htmlText) {
        Preconditions.checkArgument(htmlText != null, "HTML text is null");
        Preconditions.checkArgument(htmlText.length() >= MINIMUM_TEXT_LENGTH,
                "HTML text length is less than " + MINIMUM_TEXT_LENGTH);

        this.htmlText = htmlText;
        Logger.log(LoggerTag.OBJECT_CREATION, this);
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
