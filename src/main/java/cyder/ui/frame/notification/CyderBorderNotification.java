package cyder.ui.frame.notification;

import com.google.common.util.concurrent.Futures;
import cyder.threads.ThreadUtil;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.frame.CyderFrame;
import cyder.user.UserDataManager;
import cyder.utils.ColorUtil;

import java.awt.*;
import java.awt.geom.GeneralPath;

public final class CyderBorderNotification extends CyderToastNotification {
    /**
     * The direction this border notification should appear and disappear from.
     */
    private final NotificationDirection notificationDirection;

    /**
     * Constructs a new border notification.
     *
     * @param builder the builder to construct this border notification from
     */
    public CyderBorderNotification(NotificationBuilder builder) {
        super(builder);
        this.notificationDirection = builder.getNotificationDirection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        paintArrowBorder(g2d);
        paintArrowFill(g2d);
    }

    /**
     * Paints the arrow border on this.
     *
     * @param g2d the 2D graphics object to paint with
     */
    private void paintArrowBorder(Graphics2D g2d) {
        g2d.setColor(ColorUtil.setColorOpacity(notificationBorderColor, opacity.get()));

        // Artificially inflate length to draw arrow
        int componentWidth = container.getWidth() + 2 * borderLength;
        int componentHeight = container.getHeight() + 2 * borderLength;

        int halfCompWidth = componentWidth / 2;
        int halfCompHeight = componentHeight / 2;

        GeneralPath outlinePath = new GeneralPath();

        switch (arrowDirection) {
            case TOP -> {
                outlinePath.moveTo(2 * 2 + halfCompWidth - arrowLength, arrowLength);
                outlinePath.lineTo(2 * 2 + halfCompWidth, 0);
                outlinePath.lineTo(2 * 2 + (halfCompWidth) + arrowLength, arrowLength);
                outlinePath.lineTo(2 * 2 + halfCompWidth - arrowLength, arrowLength);
            }
            case LEFT -> {
                outlinePath.moveTo(arrowLength, 2 * 2 + halfCompHeight - arrowLength);
                outlinePath.lineTo(0, 2 * 2 + halfCompHeight);
                outlinePath.lineTo(arrowLength, 2 * 2 + halfCompHeight + arrowLength);
                outlinePath.moveTo(arrowLength, 2 * 2 + halfCompHeight - arrowLength);
            }
            case RIGHT -> {
                outlinePath.moveTo(2 * 2 * 2 + componentWidth, 2 * 2 + halfCompHeight - arrowLength);
                outlinePath.lineTo(2 * 2 * 2 + componentWidth + arrowLength, 2 * 2 + halfCompHeight);
                outlinePath.lineTo(2 * 2 * 2 + componentWidth, 2 * 2 + halfCompHeight + arrowLength);
                outlinePath.moveTo(2 * 2 * 2 + componentWidth, 2 * 2 + halfCompHeight - arrowLength);
            }
            case BOTTOM -> {
                outlinePath.moveTo(2 * 2 + halfCompWidth - arrowLength, 2 * 2 * 2 + componentHeight);
                outlinePath.lineTo(2 * 2 + halfCompWidth, 2 * 2 * 2 + componentHeight + arrowLength);
                outlinePath.lineTo(2 * 2 + halfCompWidth + arrowLength, 2 * 2 * 2 + componentHeight);
                outlinePath.lineTo(2 * 2 + halfCompWidth - arrowLength, 2 * 2 * 2 + componentHeight);
            }
        }

        outlinePath.closePath();
        g2d.fill(outlinePath);
    }

    /**
     * Paints the fill of the arrow on this.
     *
     * @param g2d the 2D graphics object to paint with
     */
    private void paintArrowFill(Graphics2D g2d) {
        g2d.setColor(ColorUtil.setColorOpacity(notificationBackgroundColor, opacity.get()));

        GeneralPath fillPath = new GeneralPath();

        int componentWidth = container.getWidth();
        int componentHeight = container.getHeight();

        int halfCompWidth = container.getWidth() / 2;
        int halfCompHeight = container.getHeight() / 2;

        switch (arrowDirection) {
            case TOP -> {
                fillPath.moveTo(2 * 2 + borderLength + halfCompWidth - arrowLength, arrowLength + borderLength);
                fillPath.lineTo(2 * 2 + borderLength + halfCompWidth, borderLength);
                fillPath.lineTo(2 * 2 + borderLength + (halfCompWidth) + arrowLength, arrowLength + borderLength);
                fillPath.lineTo(2 * 2 + borderLength - arrowLength, arrowLength + borderLength);
            }
            case LEFT -> {
                fillPath.moveTo(arrowLength + borderLength, 2 * 2 + borderLength + halfCompHeight - arrowLength);
                fillPath.lineTo(borderLength, 2 * 2 + borderLength + halfCompHeight);
                fillPath.lineTo(arrowLength + borderLength, 2 * 2 + borderLength + halfCompHeight + arrowLength);
                fillPath.moveTo(arrowLength + borderLength, 2 * 2 + borderLength + halfCompHeight - arrowLength);
            }
            case RIGHT -> {
                fillPath.moveTo(2 * 2 * 2 + borderLength + componentWidth,
                        2 * 2 + halfCompHeight - arrowLength + borderLength);
                fillPath.lineTo(2 * 2 * 2 + borderLength + componentWidth + arrowLength,
                        2 * 2 + halfCompHeight + borderLength);
                fillPath.lineTo(2 * 2 * 2 + borderLength + componentWidth,
                        2 * 2 + halfCompHeight + arrowLength + borderLength);
                fillPath.moveTo(2 * 2 * 2 + borderLength + componentWidth,
                        2 * 2 + halfCompHeight - arrowLength + borderLength);
            }
            case BOTTOM -> {
                fillPath.moveTo(2 * 2 + halfCompWidth - arrowLength + borderLength,
                        2 * 2 * 2 + componentHeight + borderLength);
                fillPath.lineTo(2 * 2 + halfCompWidth + borderLength,
                        2 * 2 * 2 + componentHeight + arrowLength + borderLength);
                fillPath.lineTo(2 * 2 + halfCompWidth + arrowLength + borderLength,
                        2 * 2 * 2 + componentHeight + borderLength);
                fillPath.lineTo(2 * 2 + halfCompWidth - arrowLength + borderLength,
                        2 * 2 * 2 + componentHeight + borderLength);
            }
        }

        fillPath.closePath();
        g2d.fill(fillPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void appear() {
        if (appearInvoked.get()) return;
        appearInvoked.set(true);

        Futures.submit(() -> {
            // todo method for set to starting position
            ThreadUtil.sleepSeconds(2);
            setBounds(-getWidth(), CyderDragLabel.DEFAULT_HEIGHT + 5, getWidth(), getHeight());

            switch (notificationDirection) {
                case TOP_LEFT -> {
                    setVisible(true);

                    for (int i = getX() ; i < CyderFrame.BORDER_LEN ; i += 8) {
                        if (shouldStopAnimation()) break;
                        setLocation(i, getY());
                        ThreadUtil.sleep(8);
                    }
                    setLocation(2, getY());
                }
            }

            // todo method for set to ending position
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
    public void disappear() {
        //super.disappear();
    }
}
