package cyder.ui.frame.notification;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import cyder.bounds.BoundsString;
import cyder.bounds.BoundsUtil;
import cyder.constants.CyderColors;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadFactory;
import cyder.threads.ThreadUtil;
import cyder.ui.frame.CyderFrame;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static cyder.strings.CyderStrings.quote;
import static cyder.strings.CyderStrings.space;

/**
 * A controller for the notification queue system of a particular {@link CyderFrame}.
 */
public class NotificationController {
    /**
     * The duration gap between a notification disappearing and the next one appearing.
     */
    private static final Duration timeBetweenNotifications = Duration.ofMillis(800);

    /**
     * The index of the notification layer to place notifications on for {@link CyderFrame}s.
     */
    private static final int notificationLayer = JLayeredPane.POPUP_LAYER;

    /**
     * The font used for CyderFrame notifications (typically equivalent to segoe20)
     */
    private static final Font notificationFont = new Font("Segoe UI Black", Font.BOLD, 20);

    /**
     * The frame this queue is controlling.
     */
    private final CyderFrame controlFrame;

    /**
     * The executor service for running the notification queue when necessary for the control frame.
     */
    private final ExecutorService queueExecutor;

    /**
     * Whether the notification queue is currently running.
     */
    private final AtomicBoolean queueRunning;

    /**
     * The notification queue to pull from.
     */
    private final ArrayList<CyderNotificationAbstract> notificationQueue = new ArrayList<>();

    /**
     * The notification currently being shown/animated.
     */
    private CyderNotificationAbstract currentNotification;

    /**
     * Constructs a new notification controller.
     *
     * @param controlFrame the frame this controller has control over
     */
    public NotificationController(CyderFrame controlFrame) {
        Preconditions.checkNotNull(controlFrame);

        this.controlFrame = controlFrame;

        String threadName = "Notification Controller Queue, frame: " + quote + controlFrame.getTitle() + quote;
        queueExecutor = Executors.newSingleThreadExecutor(new CyderThreadFactory(threadName));

        queueRunning = new AtomicBoolean(false);
    }

    /**
     * Returns the frame this controller is controlling.
     *
     * @return the frame this controller is controlling
     */
    public CyderFrame getControlFrame() {
        return controlFrame;
    }

    /**
     * Adds a toast notification with the provided text to the queue.
     *
     * @param htmlText the text to show on the toast
     */
    public synchronized void toast(String htmlText) {
        Preconditions.checkNotNull(htmlText);
        Preconditions.checkArgument(!htmlText.isEmpty());

        toast(new NotificationBuilder(htmlText));
    }

    /**
     * Adds a toast notification to the queue using the provided builder for properties.
     *
     * @param builder the builder
     */
    public synchronized void toast(NotificationBuilder builder) {
        Preconditions.checkNotNull(builder);

        int notificationPadding = 5;

        double maximumAllowableWidth = Math.ceil(controlFrame.getWidth() * 0.85); // todo
        double maximumAllowableHeight = Math.ceil(controlFrame.getWidth() * 0.45); // todo
        BoundsString bounds = BoundsUtil.widthHeightCalculation(
                builder.getHtmlText(), notificationFont, (int) maximumAllowableWidth);
        int notificationWidth = bounds.getWidth() + notificationPadding;
        int notificationHeight = bounds.getHeight() + notificationPadding;
        String notificationText = bounds.getText();

        // todo ensure width and height do not exceed max allowable, if so, we'll want to inform but not yet

        String tooltip = "Notified at:" + space + builder.getConstructionTime();

        // Null indicates we are intended to generate a label for the html text
        Container customContainer = builder.getContainer();
        if (customContainer == null) {
            JLabel textContainerLabel = new JLabel(notificationText);
            textContainerLabel.setSize(notificationWidth, notificationHeight);
            textContainerLabel.setFont(notificationFont);
            textContainerLabel.setForeground(CyderColors.notificationForegroundColor);

            JLabel interactionLabel = new JLabel();
            interactionLabel.setSize(notificationWidth, notificationHeight);
            interactionLabel.setToolTipText(tooltip);
            // todo mouse listener to dismiss

            textContainerLabel.add(interactionLabel);
            builder.setContainer(textContainerLabel);
        } else {
            int containerWidth = customContainer.getWidth();
            int containerHeight = customContainer.getHeight();

            if (containerWidth > maximumAllowableWidth || containerHeight > maximumAllowableHeight) {
                // todo inform notification
            }

            JLabel interactionLabel = new JLabel();
            interactionLabel.setSize(containerWidth, containerHeight);
            interactionLabel.setToolTipText(tooltip);
            // todo mouse listener to dismiss
            customContainer.add(interactionLabel);
        }

        long duration = builder.getViewDuration();
        if (builder.shouldCalculateViewDuration()) {
            duration = 300L * StringUtil.countWords(Jsoup.clean(notificationText, Safelist.none())); // todo
        }
        builder.setViewDuration(duration);
    }

    // todo bordering notification methods

    /**
     * Starts the notification queue if necessary.
     */
    private synchronized void startQueueIfNecessary() {
        if (queueRunning.get() || notificationQueue.isEmpty()) return;
        queueRunning.set(true);

        Futures.submit(() -> {
            while (!notificationQueue.isEmpty()) {
                currentNotification = notificationQueue.remove(0);
                currentNotification.setVisible(false);
                controlFrame.getIconPane().add(currentNotification, notificationLayer);
                currentNotification.appear();
                while (!currentNotification.isKilled()) Thread.onSpinWait();
                ThreadUtil.sleep(timeBetweenNotifications.toMillis());
            }
        }, queueExecutor);
    }

    /**
     * Generates a mouse adapter for a notification container.
     *
     * @param notification        the notification this mouse adapter will trigger events for
     * @param builder             the builder used to construct the notification
     * @param enterExitColorShift whether to darken/un-darken the notification on mouse enter/exit events
     * @return the mouse adapter
     */
    private static MouseAdapter generateMouseAdapter(CyderNotificationAbstract notification,
                                                     NotificationBuilder builder,
                                                     boolean enterExitColorShift) {
        Runnable onKillAction = builder.getOnKillAction();
        Container container = builder.getContainer();

        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onKillAction != null) {
                    notification.kill();
                    onKillAction.run();
                } else {
                    notification.disappear();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!enterExitColorShift) return;
                container.setForeground(CyderColors.notificationForegroundColor.darker());
                notification.setHovered(true);
                notification.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!enterExitColorShift) return;
                container.setForeground(CyderColors.notificationForegroundColor);
                notification.setHovered(true);
                notification.repaint();
            }
        };
    }
}
