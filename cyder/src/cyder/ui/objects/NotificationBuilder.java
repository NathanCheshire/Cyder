package cyder.ui.objects;

import cyder.enums.Direction;
import cyder.enums.NotificationDirection;
import cyder.ui.CyderFrame;

import java.awt.*;

public final class NotificationBuilder {
    //required params
    private final String htmltext;

    //optional params
    private int viewDuration = 5000;
    private Direction arrowDir = Direction.TOP;
    private CyderFrame.ClickAction onKillAction = null;
    private NotificationDirection notificationDirection = NotificationDirection.TOP;
    private Container container = null;
    private Color notificationBackground = null;

    public NotificationBuilder(String htmlText) {
        if (htmlText == null || htmlText.length() < 3)
            throw new IllegalArgumentException("Html text is null or less than 3 chars");

        this.htmltext = htmlText;
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

    public String getHtmltext() {
        return htmltext;
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
