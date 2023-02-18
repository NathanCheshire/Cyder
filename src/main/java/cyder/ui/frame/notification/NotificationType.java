package cyder.ui.frame.notification;

import cyder.ui.frame.CyderFrame;

/**
 * The possible types for a {@link CyderFrame} notification.
 */
public enum NotificationType {
    /**
     * A common notification with an arrow on a single cardinal side.
     */
    NOTIFICATION,

    /**
     * A toast emulating an Android toast.
     */
    TOAST
}
