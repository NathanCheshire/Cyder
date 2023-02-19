package cyder.ui.frame.notification;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.enumerations.Direction;
import cyder.threads.ThreadUtil;
import cyder.ui.frame.CyderFrame;
import cyder.user.UserDataManager;
import cyder.utils.ColorUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
     * The opacity this notification should be painted as.
     */
    private final AtomicInteger opacity = new AtomicInteger(ColorUtil.opacityRange.upperEndpoint());

    /**
     * Whether the mouse is currently hovered over this notification.
     */
    private final AtomicBoolean isHovered = new AtomicBoolean();

    /**
     * The duration this notification should be visible for.
     */
    private final Duration visibleDuration;

    /**
     * The direction the notification should animate from.
     */
    private final NotificationDirection notificationDirection;

    /**
     * The direction the arrow should be painted on.
     */
    private final Direction arrowDirection;

    /**
     * The container for this notification.
     */
    private final JLabel container;

    /**
     * Constructs a new toast notification.
     *
     * @param builder the builder to construct this toast notification from
     */
    public CyderToastNotification(NotificationBuilder builder) {
        Preconditions.checkNotNull(builder);

        this.visibleDuration = Duration.ofMillis(builder.getViewDuration());
        this.notificationDirection = builder.getNotificationDirection();
        this.arrowDirection = notificationDirection.getArrowDirection();
        this.container = builder.getContainer();
    }

    private static final int arrowLength = 8;
    private static final int borderLength = 5;

    /**
     * {@inheritDoc}
     */
    @Override
    public void paintLogic(Graphics g) {
        Preconditions.checkNotNull(g);

        Graphics2D g2d = (Graphics2D) g;
        addRenderingHints(g2d);

        opacity.set(ColorUtil.opacityRange.lowerEndpoint());

        paintOutline(g2d);
        paintFill(g2d);

        // todo label text
    }

    /**
     * Adds rendering hints to the provided graphics 2D object.
     *
     * @param g2d the graphics 2D object
     */
    private void addRenderingHints(Graphics2D g2d) {
        RenderingHints qualityHints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHints(qualityHints);
    }

    private void paintOutline(Graphics2D g2d) {
        // todo move color to this class or parent perhaps
        Color borderColor = CyderColors.notificationBorderColor;
        if (isHovered.get()) borderColor = borderColor.darker();
        g2d.setPaint(ColorUtil.setColorOpacity(borderColor, opacity.get()));

        // this is the width x height of what we will be surrounding
        int componentWidth = container.getWidth();
        int componentHeight = container.getHeight();

        // artificially inflate the width and height to draw the border
        componentHeight += (borderLength * 2);
        componentWidth += (borderLength * 2);

        GeneralPath outlinePath = new GeneralPath();

        int curveInc = 2;

        // already at 0,0
        // move out of way to draw since arrow might be left or right
        int x = 0;
        int y = 0;

        if (arrowDirection == Direction.LEFT) {
            x = arrowLength;
        }

        if (arrowDirection == Direction.TOP) {
            y = arrowLength;
        }

        // always 4 more down due to curve up 2 and then another 2
        y += 2 * curveInc;

        outlinePath.moveTo(x, y);

        // curve up 2 and right 2, twice
        outlinePath.curveTo(x, y, x + curveInc, y - curveInc, x + 2 * curveInc, y - 2 * curveInc);
        // new x,y we are at after curing
        x += (2 * curveInc);
        y -= (2 * curveInc);

        // line from top left point to right for component top
        outlinePath.lineTo(x + componentWidth, y);
        // new x
        x += componentWidth;
        // curve down 2 and right 2, twice
        outlinePath.curveTo(x, y, x + curveInc, y + curveInc, x + 2 * curveInc, y + 2 * curveInc);

        // new x,y we're at
        x += (2 * curveInc);
        y += (2 * curveInc);

        // line down for component height
        outlinePath.lineTo(x, y + componentHeight);

        // new y
        y += componentHeight;

        // curve down 2 and left 2, twice
        outlinePath.curveTo(x, y, x - curveInc, y + curveInc, x - 2 * curveInc, y + 2 * curveInc);

        // new x,y we're at
        x -= (2 * curveInc);
        y += (2 * curveInc);

        // line left for component width
        outlinePath.lineTo(x - componentWidth, y);

        // new x
        x -= componentWidth;

        // curve up 2 and left 2, twice
        outlinePath.curveTo(x, y, x - curveInc, y - curveInc, x - 2 * curveInc, y - 2 * curveInc);

        // new x,y we're at
        x -= (2 * curveInc);
        y -= (2 * curveInc);

        // line up for component height
        outlinePath.lineTo(x, y - componentHeight);

        //noinspection UnusedAssignment - want to ensure (x, y) is always up to date for maintainability
        y -= componentHeight;

        // close and fill
        outlinePath.closePath();
        g2d.fill(outlinePath);
    }

    private void paintFill(Graphics2D g2d) {
        int componentHeight = container.getWidth();
        int componentWidth = container.getHeight();
        Color fillColor = CyderColors.notificationBackgroundColor;
        if (isHovered.get()) fillColor = fillColor.darker();
        g2d.setPaint(ColorUtil.setColorOpacity(fillColor, opacity.get()));

        GeneralPath fillPath = new GeneralPath();

        // already at 0,0 but need to be reset
        // move out of way to draw since arrow might be left or right
        int x = 0;
        int y = 0;

        if (arrowDirection == Direction.LEFT) {
            x = arrowLength;
        }

        if (arrowDirection == Direction.TOP) {
            y = arrowLength;
        }

        int curveInc = 2;

        // always 4 more down due to curve up 2 and then another 2
        y += 2 * curveInc;

        // offset inward for fill shape
        x += borderLength;
        y += borderLength;

        fillPath.moveTo(x, y);

        // curve up 2 and right 2, twice
        fillPath.curveTo(x, y, x + curveInc, y - curveInc, x + 2 * curveInc, y - 2 * curveInc);

        // new x,y we're at
        x += 2 * curveInc;
        y -= 2 * curveInc;

        // line right for component width
        fillPath.lineTo(x + componentWidth, y);

        // new x
        x += componentWidth;

        // curve down 2 and right 2, twice
        fillPath.curveTo(x, y, x + curveInc, y + curveInc, x + 2 * curveInc, y + 2 * curveInc);

        // new x,y we're at
        x += 2 * curveInc;
        y += 2 * curveInc;

        // line down for component height
        fillPath.lineTo(x, y + componentHeight);

        // new y
        y += componentHeight;

        // curve down 2 and left 2, twice
        fillPath.curveTo(x, y, x - curveInc, y + curveInc, x - 2 * curveInc, y + 2 * curveInc);

        // new x,y we're at
        x -= 2 * curveInc;
        y += 2 * curveInc;

        // line left for component width
        fillPath.lineTo(x - componentWidth, y);

        // new x
        x -= componentWidth;

        // curve up 2 and left 2, twice
        fillPath.curveTo(x, y, x - curveInc, y - curveInc, x - 2 * curveInc, y - 2 * curveInc);

        // new x,y we're at
        x -= 2 * curveInc;
        y -= 2 * curveInc;

        // line up for component height
        fillPath.lineTo(x, y - componentHeight);

        // new y
        //noinspection UnusedAssignment
        y -= componentHeight;

        // close and fill
        fillPath.closePath();
        g2d.fill(fillPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void appear() {
        CyderFrame parent = (CyderFrame) getParent();

        // centered on x, y has offset of 10 pixels from bottom
        int bottomOffset = 10;
        setBounds(parent.getWidth() / 2 - getWidth() / 2,
                parent.getHeight() - getHeight() - bottomOffset,
                getWidth(), getHeight());

        opacity.set(0);
        setVisible(true);

        int animationStep = 2;
        for (int i = ColorUtil.opacityRange.lowerEndpoint()
             ; i < ColorUtil.opacityRange.upperEndpoint() ; i += animationStep) {
            if (!UserDataManager.INSTANCE.shouldDoAnimations()) {
                break;
            }

            opacity.set(i);
            repaint();
            int animationDelay = 2;
            ThreadUtil.sleep(animationDelay);
        }

        opacity.set(ColorUtil.opacityRange.upperEndpoint());
        repaint();

        if (!UserDataManager.INSTANCE.shouldPersistNotifications()
                && !shouldRemainVisibleUntilDismissed(visibleDuration.toMillis())) {
            ThreadUtil.sleepSeconds(visibleDuration.toMillis());
            disappear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void disappear() {
        int animationStep = 2;
        for (int i = ColorUtil.opacityRange.upperEndpoint()
             ; i >= ColorUtil.opacityRange.lowerEndpoint() ; i -= animationStep) {
            if (shouldStopAnimation()) break;
            opacity.set(i);
            repaint();
            int animationDelay = 2;
            ThreadUtil.sleep(animationDelay);
        }

        opacity.set(ColorUtil.opacityRange.lowerEndpoint());
        repaint();

        CyderFrame parent = (CyderFrame) getParent();
        if (parent != null) {
            parent.remove(this);
            setVisible(false);
            parent.repaint();
        }
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

    /**
     * Sets whether this notification should be painted as hovered.
     *
     * @param hovered whether this notification should be painted as hovered
     */
    public void setHovered(boolean hovered) {
        isHovered.set(hovered);
        repaint();
    }

    /**
     * Returns whether a current animation should be stopped depending
     * on the state of killed and the user's animation preference.
     *
     * @return whether a current animation should be stopped
     */
    private boolean shouldStopAnimation() {
        return killed.get() || !UserDataManager.INSTANCE.shouldDoAnimations();
    }
}
