package cyder.ui.frame.notification;

import com.google.common.base.Preconditions;
import cyder.ui.frame.CyderFrame;

/**
 * A notification queue for a particular {@link CyderFrame}.
 */
public class NotificationQueue {
    /**
     * The frame this queue is controlling.
     */
    private final CyderFrame controlFrame;

    /**
     * Constructs a new notification queue.
     *
     * @param controlFrame the frame this queue has control over
     */
    public NotificationQueue(CyderFrame controlFrame) {
        this.controlFrame = Preconditions.checkNotNull(controlFrame);
    }

    /**
     * Returns the frame this queue is controlling.
     *
     * @return the frame this queue is controlling
     */
    public CyderFrame getControlFrame() {
        return controlFrame;
    }

    public void notify(CyderNotificationAbstract notification) {
        Preconditions.checkNotNull(notification);

        // todo component is added to this: controlFrame.getContentPane();
    }
}
