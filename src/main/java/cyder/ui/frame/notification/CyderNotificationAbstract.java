package cyder.ui.frame.notification;

import cyder.ui.frame.CyderFrame;

import javax.swing.*;
import java.awt.*;

/**
 * A notification for a {@link CyderFrame}.
 * todo rename this to CyderNotification and use this and not the interface for accepting methods.
 */
public abstract class CyderNotificationAbstract extends JLabel implements ICyderNotification {
    /**
     * The magic number used to denote a notification should be shown until dismissed.
     */
    private static final int showUntilDismissed = -1;

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        paintLogic(g);
    }

    /**
     * Returns whether the provided duration is indicative that a notification
     * should remain visible until dismissed by a user.
     *
     * @param duration the duration
     * @return whether the provided duration is indicative that a notification
     * should remain visible until dismissed by a user
     */
    boolean shouldRemainVisibleUntilDismissed(long duration) {
        return duration == showUntilDismissed;
    }
}
