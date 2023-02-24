package cyder.ui.frame.notification;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import cyder.enumerations.Direction;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadFactory;
import cyder.threads.ThreadUtil;
import cyder.user.UserDataManager;
import cyder.utils.ColorUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A toast notification, similar to the Android API's toast notifications.
 * The toast has no direction arrow painted on it and performs an opacity
 * fade-in and fade-out animation at the bottom center of the frame.
 */
public class CyderToastNotification extends CyderNotification {
    /**
     * The offset from the bottom of the frame toast notifications are placed.
     */
    private static final int toastBottomOffset = 10;

    /**
     * The delay between animation steps.
     */
    private static final int animationDelay = 2;

    /**
     * The opacity step every {@link #animationDelay}.
     */
    private static final int opacityStep = 2;

    /**
     * The length for curves when painting the notification fill and outline.
     */
    private static final int curveLength = 2;

    /**
     * The length of the arrow.
     */
    protected static final int arrowLength = 8;

    /**
     * The length of the border
     */
    protected static final int borderLength = 5;

    /**
     * Whether {@link #appear()} has been invoked on this.
     */
    protected final AtomicBoolean appearInvoked = new AtomicBoolean();

    /**
     * Whether {@link #disappear()} has been invoked on this.
     */
    protected final AtomicBoolean disappearInvoked = new AtomicBoolean();

    /**
     * Whether this is currently performing an animation.
     */
    protected final AtomicBoolean animating = new AtomicBoolean();

    /**
     * Whether this notification has been killed.
     */
    protected final AtomicBoolean killed = new AtomicBoolean();

    /**
     * The opacity this notification should be painted as.
     */
    protected final AtomicInteger opacity = new AtomicInteger(ColorUtil.opacityRange.upperEndpoint());

    /**
     * Whether the mouse is currently hovered over this notification.
     */
    private final AtomicBoolean isHovered = new AtomicBoolean();

    /**
     * The executor service for performing the disappear animation.
     */
    protected final ExecutorService appearAnimationService =
            Executors.newSingleThreadExecutor(new CyderThreadFactory("Notification Appear Animation"));

    /**
     * The executor service for performing the disappear animation.
     */
    protected final ExecutorService disappearAnimationService =
            Executors.newSingleThreadExecutor(new CyderThreadFactory("Notification Disappear Animation"));

    /**
     * The duration this notification should be visible for.
     */
    protected final Duration visibleDuration;

    /**
     * The direction the arrow should be painted on.
     */
    protected final Direction arrowDirection;

    /**
     * The container for this notification.
     */
    protected final JLabel container;

    /**
     * The html-styled text this notification holds if not using a custom container from the builder.
     */
    private final String htmlText;

    /**
     * Constructs a new toast notification.
     *
     * @param builder the builder to construct this toast notification from
     */
    public CyderToastNotification(NotificationBuilder builder) {
        Preconditions.checkNotNull(builder);
        long duration = builder.getViewDuration();
        Preconditions.checkArgument(duration > 0);
        this.visibleDuration = Duration.ofMillis(duration);
        this.arrowDirection = builder.getNotificationDirection().getArrowDirection();
        this.container = builder.getContainer();
        this.htmlText = builder.getHtmlText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        int ret = 2 * borderLength + container.getWidth() + 2 * 2 * curveLength;
        if (Direction.isHorizontal(arrowDirection)) ret += arrowLength;
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        int ret = 2 * borderLength + container.getHeight() + 2 * 2 * curveLength;
        if (Direction.isVertical(arrowDirection)) ret += arrowLength;
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        Preconditions.checkNotNull(g);

        Graphics2D g2d = (Graphics2D) g;
        addRenderingHints(g2d);

        opacity.set(opacity.get());

        paintOutline(g2d);
        paintFill(g2d);

        int x = arrowLength + 2 * curveLength;
        if (arrowDirection == Direction.LEFT) x += arrowLength;
        int y = arrowLength + 2 * curveLength;
        if (arrowDirection == Direction.TOP) y += arrowLength;

        container.setBounds(x, y, container.getWidth(), container.getHeight());
        container.setVisible(true);
        if (!Arrays.asList(getComponents()).contains(container)) add(container);
        super.paint(g);
    }

    /**
     * Adds rendering hints to the provided graphics 2D object.
     *
     * @param g2d the graphics 2D object
     */
    private void addRenderingHints(Graphics2D g2d) {
        Preconditions.checkNotNull(g2d);

        RenderingHints qualityHints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHints(qualityHints);
    }

    /**
     * Paints the outline (border) on this.
     *
     * @param g2d the 2D graphics object
     */
    private void paintOutline(Graphics2D g2d) {
        Preconditions.checkNotNull(g2d);

        int componentWidth = container.getWidth();
        int componentHeight = container.getHeight();
        Color borderColor = notificationBorderColor;
        if (isHovered.get()) borderColor = borderColor.darker();
        g2d.setPaint(ColorUtil.setColorOpacity(borderColor, opacity.get()));

        GeneralPath outlinePath = new GeneralPath();

        // Artificially inflate to account for border
        componentHeight += (borderLength * 2);
        componentWidth += (borderLength * 2);

        // Starting point is shifted if arrow needs to be painted on a starting side
        int x = 0;
        int y = 0;
        if (arrowDirection == Direction.LEFT) {
            x = arrowLength;
        } else if (arrowDirection == Direction.TOP) {
            y = arrowLength;
        }

        // Y starts two curves down since we curve up first before going right
        y += 2 * curveLength;

        outlinePath.moveTo(x, y);
        outlinePath.curveTo(x, y,
                x + curveLength, y - curveLength,
                x + 2 * curveLength, y - 2 * curveLength);
        x += (2 * curveLength);
        y -= (2 * curveLength);
        outlinePath.lineTo(x + componentWidth, y);
        x += componentWidth;
        outlinePath.curveTo(x, y,
                x + curveLength, y + curveLength,
                x + 2 * curveLength, y + 2 * curveLength);
        x += (2 * curveLength);
        y += (2 * curveLength);
        outlinePath.lineTo(x, y + componentHeight);
        y += componentHeight;
        outlinePath.curveTo(x, y,
                x - curveLength, y + curveLength,
                x - 2 * curveLength, y + 2 * curveLength);
        x -= (2 * curveLength);
        y += (2 * curveLength);
        outlinePath.lineTo(x - componentWidth, y);
        x -= componentWidth;
        outlinePath.curveTo(x, y, x - curveLength, y - curveLength, x - 2 * curveLength, y - 2 * curveLength);
        x -= (2 * curveLength);
        y -= (2 * curveLength);
        outlinePath.lineTo(x, y - componentHeight);
        //noinspection UnusedAssignment
        y -= componentHeight;
        outlinePath.closePath();
        g2d.fill(outlinePath);
    }

    /**
     * Paints the fill on this.
     *
     * @param g2d the 2D graphics object
     */
    private void paintFill(Graphics2D g2d) {
        Preconditions.checkNotNull(g2d);

        int componentWidth = container.getWidth();
        int componentHeight = container.getHeight();
        Color fillColor = notificationBackgroundColor;
        if (isHovered.get()) fillColor = fillColor.darker();
        g2d.setPaint(ColorUtil.setColorOpacity(fillColor, opacity.get()));

        GeneralPath fillPath = new GeneralPath();

        // Starting point is shifted if arrow needs to be painted on a starting side
        int x = 0;
        int y = 0;
        if (arrowDirection == Direction.LEFT) {
            x = arrowLength;
        } else if (arrowDirection == Direction.TOP) {
            y = arrowLength;
        }

        // Y starts two curves down since we curve up first before going right
        y += 2 * curveLength;

        // Offset due to border
        x += borderLength;
        y += borderLength;

        fillPath.moveTo(x, y);
        fillPath.curveTo(x, y,
                x + curveLength, y - curveLength,
                x + 2 * curveLength, y - 2 * curveLength);
        x += 2 * curveLength;
        y -= 2 * curveLength;
        fillPath.lineTo(x + componentWidth, y);
        x += componentWidth;
        fillPath.curveTo(x, y,
                x + curveLength, y + curveLength,
                x + 2 * curveLength, y + 2 * curveLength);
        x += 2 * curveLength;
        y += 2 * curveLength;
        fillPath.lineTo(x, y + componentHeight);
        y += componentHeight;
        fillPath.curveTo(x, y,
                x - curveLength, y + curveLength,
                x - 2 * curveLength, y + 2 * curveLength);
        x -= 2 * curveLength;
        y += 2 * curveLength;
        fillPath.lineTo(x - componentWidth, y);
        x -= componentWidth;
        fillPath.curveTo(x, y,
                x - curveLength, y - curveLength,
                x - 2 * curveLength, y - 2 * curveLength);
        x -= 2 * curveLength;
        y -= 2 * curveLength;
        fillPath.lineTo(x, y - componentHeight);
        //noinspection UnusedAssignment
        y -= componentHeight;
        fillPath.closePath();
        g2d.fill(fillPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void appear() {
        if (appearInvoked.get()) return;
        if (animating.get()) return;
        appearInvoked.set(true);
        animating.set(true);

        Futures.submit(() -> {
            setToStartAndEndingPosition();
            opacity.set(ColorUtil.opacityRange.lowerEndpoint());
            setVisible(true);

            for (int i = ColorUtil.opacityRange.lowerEndpoint()
                 ; i < ColorUtil.opacityRange.upperEndpoint() ; i += opacityStep) {
                if (shouldStopAnimation()) break;
                opacity.set(i);
                setToStartAndEndingPosition();
                repaint();
                ThreadUtil.sleep(animationDelay);
            }

            animating.set(false);
            opacity.set(ColorUtil.opacityRange.upperEndpoint());
            repaint();

            /*
            Note to maintainers: yes, there are two checks here for the user preference of persisting notifications.
            This is to address the case where the user toggles it while a notification is present.
             */
            if (UserDataManager.INSTANCE.shouldPersistNotifications()) return;
            if (shouldRemainVisibleUntilDismissed(visibleDuration.toMillis())) return;
            ThreadUtil.sleep(visibleDuration.toMillis());
            if (UserDataManager.INSTANCE.shouldPersistNotifications()) return;
            disappear();
        }, appearAnimationService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void disappear() {
        Preconditions.checkState(appearInvoked.get());
        Preconditions.checkState(!animating.get());
        if (disappearInvoked.get()) return;
        disappearInvoked.set(true);
        animating.set(true);

        Futures.submit(() -> {
            setToStartAndEndingPosition();
            opacity.set(ColorUtil.opacityRange.upperEndpoint());

            for (int i = ColorUtil.opacityRange.upperEndpoint()
                 ; i >= ColorUtil.opacityRange.lowerEndpoint() ; i -= opacityStep) {
                if (shouldStopAnimation()) break;
                opacity.set(i);
                setToStartAndEndingPosition();
                repaint();
                ThreadUtil.sleep(animationDelay);
            }

            opacity.set(ColorUtil.opacityRange.lowerEndpoint());
            repaint();

            setVisible(false);
            Container parent = getParent();
            if (parent != null) {
                parent.remove(this);
                parent.repaint();
            }

            kill();
        }, disappearAnimationService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void kill() {
        killed.set(true);
        animating.set(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isKilled() {
        return killed.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHovered(boolean hovered) {
        isHovered.set(hovered);
        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getLabelText() {
        if (StringUtil.isNullOrEmpty(htmlText)) return Optional.empty();
        return Optional.of(htmlText);
    }

    /**
     * Returns whether a current animation should be stopped depending
     * on the state of killed and the user's animation preference.
     *
     * @return whether a current animation should be stopped
     */
    protected boolean shouldStopAnimation() {
        return isKilled() || !UserDataManager.INSTANCE.shouldDoAnimations();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setToStartAndEndingPosition() {
        int parentWidth = getParent().getWidth();
        int parentHeight = getParent().getHeight();
        int ourWidth = getWidth();
        int ourHeight = getHeight();

        setBounds(parentWidth / 2 - ourWidth / 2,
                parentHeight - ourHeight - toastBottomOffset, ourWidth, ourHeight);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnimating() {
        return animating.get();
    }
}
