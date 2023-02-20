package cyder.ui.frame.notification;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import cyder.threads.CyderThreadFactory;
import cyder.ui.frame.CyderFrame;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static cyder.strings.CyderStrings.quote;

/**
 * A controller for the notification queue system of a particular {@link CyderFrame}.
 */
public class NotificationController {
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

    // todo notify methods accepting string or builder and for each type such as border notification or toast
    //  builders will be transformed into subclasses of CyderNotificationAbstract

    /**
     * Starts the notification queue if necessary.
     */
    private synchronized void startQueueIfNecessary() {
        if (queueRunning.get() || notificationQueue.isEmpty()) return;
        queueRunning.set(true);

        Futures.submit(() -> {

        }, queueExecutor);
    }
}
