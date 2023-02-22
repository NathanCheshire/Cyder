package cyder.ui.frame.notification;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import cyder.bounds.BoundsString;
import cyder.bounds.BoundsUtil;
import cyder.constants.CyderColors;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.threads.CyderThreadFactory;
import cyder.threads.ThreadUtil;
import cyder.ui.frame.CyderFrame;
import cyder.utils.HtmlUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static cyder.strings.CyderStrings.quote;

/**
 * A controller for the notification queue system of a particular {@link CyderFrame}.
 */
public class NotificationController {
    /**
     * The foreground color used for notifications.
     */
    private static final Color notificationForegroundColor = CyderColors.regularPurple;

    /**
     * The maximum allowable notification width to frame width ratio.
     */
    private static final double maxNotificationToFrameWidthRatio = 0.85;

    /**
     * The maximum allowable notification height to frame height ratio.
     */
    private static final double maxNotificationToFrameHeightRatio = 0.45;

    /**
     * The duration gap between a notification disappearing and the next one appearing.
     */
    private static final Duration timeBetweenNotifications = Duration.ofMillis(800);

    /**
     * The font used for CyderFrame notifications (typically equivalent to segoe20)
     */
    private static final Font notificationFont = new Font("Segoe UI Black", Font.BOLD, 20);

    /**
     * The number of milliseconds per word a notification should be visible for.
     */
    private static final long msPerWord = 300;

    /**
     * The padding for notification containers and their painted container objects.
     */
    private static final int notificationPadding = 5;

    /**
     * The prefix for the tooltip on notifications.
     */
    private static final String tooltipPrefix = "Notified at: ";

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
     * Whether this controller has been killed.
     */
    private final AtomicBoolean killed;

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
        queueExecutor = Executors.newSingleThreadExecutor(generateCyderThreadFactory());
        queueRunning = new AtomicBoolean();
        killed = new AtomicBoolean();
    }

    /**
     * Generates the thread factory for the notification queue executor service.
     *
     * @return the thread factory for the notification queue executor service
     */
    private CyderThreadFactory generateCyderThreadFactory() {
        String threadName = "Notification Controller Queue, frame: " + quote + controlFrame.getTitle() + quote;
        return new CyderThreadFactory(threadName);
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

        JLabel mouseEventLabel = generateContainerIfNeededAndGenerateMouseEventLabel(builder);
        CyderToastNotification toastNotification = new CyderToastNotification(builder);
        mouseEventLabel.addMouseListener(generateMouseAdapter(toastNotification, builder));
        notificationQueue.add(toastNotification);
        startQueueIfNecessary();
    }

    /**
     * Adds a border notification with the provided text to the queue.
     *
     * @param htmlText the text to show on the border notification
     */
    public synchronized void borderNotify(String htmlText) {
        Preconditions.checkNotNull(htmlText);
        Preconditions.checkArgument(!htmlText.isEmpty());

        borderNotify(new NotificationBuilder(htmlText));
    }

    /**
     * Adds a border notification to the queue using the provided builder for properties.
     *
     * @param builder the builder
     */
    public synchronized void borderNotify(NotificationBuilder builder) {
        Preconditions.checkNotNull(builder);

        JLabel mouseEventLabel = generateContainerIfNeededAndGenerateMouseEventLabel(builder);
        CyderBorderNotification toastNotification = new CyderBorderNotification(builder);
        mouseEventLabel.addMouseListener(generateMouseAdapter(toastNotification, builder));
        notificationQueue.add(toastNotification);
        startQueueIfNecessary();
    }

    /**
     * Generates the text container for the notification if a custom container is not specified and creates
     * the mouse event label for the interaction listener to be added to.
     *
     * @param builder the builder
     * @return the mouse event label for the notification
     */
    private JLabel generateContainerIfNeededAndGenerateMouseEventLabel(NotificationBuilder builder) {
        Preconditions.checkNotNull(builder);

        String tooltip = tooltipPrefix + builder.getConstructionTime();

        JLabel mouseEventLabel;
        if (builder.getContainer() == null) {
            BoundsString bounds = BoundsUtil.widthHeightCalculation(
                    builder.getHtmlText(), notificationFont, getMaximumAllowableWidth());
            int notificationWidth = bounds.getWidth() + notificationPadding;
            int notificationHeight = bounds.getHeight() + notificationPadding;
            // todo check for being larger than allowable dimension
            String notificationText = bounds.getText();
            if (builder.shouldCalculateViewDuration()) {
                builder.setViewDuration(msPerWord * HtmlUtil.cleanAndCountWords(notificationText));
            }

            JLabel textContainerLabel = new JLabel(notificationText);
            textContainerLabel.setSize(notificationWidth, notificationHeight);
            textContainerLabel.setFont(notificationFont);
            textContainerLabel.setForeground(notificationForegroundColor);

            mouseEventLabel = generateAndAddMouseEventLabel(textContainerLabel, tooltip);
            builder.setContainer(textContainerLabel);
        } else {
            // todo check for custom container being too big
            mouseEventLabel = generateAndAddMouseEventLabel(builder.getContainer(), tooltip);
        }

        return mouseEventLabel;
    }

    /**
     * Kills this notification controller, revoking all notifications currently displaying and clearing the queue.
     */
    public void kill() {
        killed.set(true);
        notificationQueue.clear();
        currentNotification.kill();
    }

    /**
     * Revokes the current notification from the control frame without performing the disappear animation.
     */
    public void revokeCurrentNotification() {
        revokeCurrentNotification(false);
    }

    /**
     * Revokes the current notification from the control frame.
     *
     * @param animate whether the notification should be animated away or instantly killed and removed
     */
    public void revokeCurrentNotification(boolean animate) {
        if (animate) {
            currentNotification.disappear();
        } else {
            currentNotification.kill();
        }
    }

    /**
     * Revokes the notification being shown or in the queue with the provided text.
     *
     * @param expectedText the expected text for the notification
     */
    public synchronized void revokeNotification(String expectedText) {
        if (currentNotification != null) {
            Optional<String> optionalText = currentNotification.getLabelText();
            if (optionalText.isPresent() && optionalText.get().equals(expectedText)) currentNotification.kill();
        }

        notificationQueue.removeIf(notification -> {
            Optional<String> optionalText = notification.getLabelText();
            return optionalText.isPresent() && optionalText.get().equals(expectedText);
        });
    }

    /**
     * Revokes all notifications currently showing and from the queue.
     */
    public void revokeAllNotifications() {
        notificationQueue.clear();
        if (currentNotification != null) currentNotification.kill();
    }

    /**
     * Generates and adds a label, placed on the provided container, with the provided tooltip.
     * The label is set to the same size as the parent container.
     *
     * @param parentContainer the container the label will be added on top of
     * @param tooltip         the tooltip text for the mouse event label
     * @return the label which was added to the parent container
     */
    private JLabel generateAndAddMouseEventLabel(Container parentContainer, String tooltip) {
        Preconditions.checkNotNull(parentContainer);
        Preconditions.checkNotNull(tooltip);

        JLabel mouseEventLabel = new JLabel();
        mouseEventLabel.setBounds(0, 0, parentContainer.getWidth(), parentContainer.getHeight());
        mouseEventLabel.setToolTipText(tooltip);
        parentContainer.add(mouseEventLabel);
        return mouseEventLabel;
    }

    /**
     * Returns the maximum allowable width for a notification given the current width of the control frame.
     *
     * @return the maximum allowable width for a notification given the current width of the control frame
     */
    private int getMaximumAllowableWidth() {
        return (int) Math.ceil(controlFrame.getWidth() * maxNotificationToFrameWidthRatio);
    }

    /**
     * Returns the maximum allowable height for a notification given the current height of the control frame.
     *
     * @return the maximum allowable height for a notification given the current height of the control frame
     */
    private int getMaxAllowableHeight() {
        return (int) Math.ceil(controlFrame.getWidth() * maxNotificationToFrameHeightRatio);
    }

    /**
     * Starts the notification queue if necessary.
     */
    private synchronized void startQueueIfNecessary() {
        if (queueRunning.get() || notificationQueue.isEmpty()) return;
        queueRunning.set(true);

        Futures.submit(() -> {
            while (!notificationQueue.isEmpty() && !killed.get()) {
                currentNotification = notificationQueue.remove(0);
                controlFrame.getTrueContentPane().add(currentNotification, JLayeredPane.DRAG_LAYER);
                currentNotification.appear();
                // todo be able to add tags to a log call, [Notification] [Test Frame]:
                Logger.log(LogTag.UI_ACTION, "Notification invoked");
                while (!currentNotification.isKilled()) Thread.onSpinWait();
                ThreadUtil.sleep(timeBetweenNotifications.toMillis());
            }

            queueRunning.set(false);
        }, queueExecutor);
    }

    /**
     * Generates a mouse adapter for a notification container.
     *
     * @param notification the notification this mouse adapter will trigger events for
     * @param builder      the builder used to construct the notification
     * @return the mouse adapter
     */
    private static MouseAdapter generateMouseAdapter(CyderNotificationAbstract notification,
                                                     NotificationBuilder builder) {
        Preconditions.checkNotNull(notification);
        Preconditions.checkNotNull(builder);

        Runnable onKillAction = builder.getOnKillAction();
        Container container = builder.getContainer();

        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onKillAction == null) {
                    notification.disappear();
                } else {
                    notification.kill();
                    onKillAction.run();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                container.setForeground(notificationForegroundColor.darker());
                notification.setHovered(true);
                notification.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                container.setForeground(notificationForegroundColor);
                notification.setHovered(true);
                notification.repaint();
            }
        };
    }
}
