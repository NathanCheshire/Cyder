package cyder.ui.frame.notification;

import com.google.common.base.Preconditions;
import cyder.bounds.BoundsString;
import cyder.bounds.BoundsUtil;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.strings.StringUtil;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.frame.CyderFrame;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static cyder.strings.CyderStrings.*;

/**
 * A notification expressed through a secondary pane if the content of
 * a different notification exceeded the allowable size.
 */
public class CyderInformNotification extends CyderNotification {
    /**
     * The top, bottom, left, and right padding between the frame and container.
     */
    private static final int notifyPadding = 20;

    /**
     * The font to use for the text of the notify label.
     */
    private static final Font notifyFont = CyderFonts.DEFAULT_FONT_SMALL;

    /**
     * Whether this notification has been killed.
     */
    private final AtomicBoolean killed = new AtomicBoolean();

    /**
     * Whether the appear method has been invoked.
     */
    private final AtomicBoolean appearInvoked = new AtomicBoolean();

    /**
     * Whether the disappear method has been invoked.
     */
    private final AtomicBoolean disappearInvoked = new AtomicBoolean();

    /**
     * The frame used to show the container of this notification.
     */
    private CyderFrame notificationFrame;

    /**
     * The container this notification will display.
     */
    private final Container container;

    /**
     * The frame to position the notification frame relative to.
     */
    private final CyderFrame relativeFrame;

    /**
     * The html text for this notification if a custom container is not specified.
     */
    private final String htmlText;

    /**
     * Constructs a new inform notification.
     *
     * @param builder the builder
     */
    public CyderInformNotification(NotificationBuilder builder, CyderFrame relativeFrame) {
        Preconditions.checkNotNull(builder);

        container = builder.getContainer();
        this.relativeFrame = relativeFrame;
        this.htmlText = builder.getHtmlText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void appear() {
        Preconditions.checkState(!appearInvoked.get());
        appearInvoked.set(true);

        if (StringUtil.isNullOrEmpty(htmlText)) {
            int width = container.getWidth() + 2 * CyderFrame.BORDER_LEN + 2 * notifyPadding;
            int height = container.getHeight() + CyderDragLabel.DEFAULT_HEIGHT +
                    CyderFrame.BORDER_LEN + 2 * notifyPadding;
            notificationFrame = new CyderFrame(width, height);
            container.setLocation(CyderFrame.BORDER_LEN + notifyPadding,
                    CyderDragLabel.DEFAULT_HEIGHT + notifyPadding);
            notificationFrame.getContentPane().add(container);
        } else {
            BoundsString bs = BoundsUtil.widthHeightCalculation(htmlText, notifyFont);
            JLabel label = new JLabel(bs.getText());
            label.setBounds(CyderFrame.BORDER_LEN + notifyPadding,
                    CyderDragLabel.DEFAULT_HEIGHT + notifyPadding, bs.getWidth(), bs.getHeight());
            label.setForeground(CyderColors.navy);
            label.setFont(notifyFont);
            int width = bs.getWidth() + 2 * CyderFrame.BORDER_LEN + 2 * notifyPadding;
            int height = bs.getHeight() + CyderDragLabel.DEFAULT_HEIGHT + CyderFrame.BORDER_LEN + 2 * notifyPadding;
            notificationFrame = new CyderFrame(width, height);
            notificationFrame.getContentPane().add(label);
        }

        String frameTitle = relativeFrame == null ? "Notification" : relativeFrame.getTitle()
                + space + openingParenthesis + "Notification" + closingParenthesis;
        notificationFrame.setTitle(frameTitle);
        notificationFrame.finalizeAndShow(relativeFrame);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void disappear() {
        Preconditions.checkState(appearInvoked.get());
        Preconditions.checkState(!disappearInvoked.get());
        disappearInvoked.set(true);

        if (notificationFrame != null && !notificationFrame.isDisposed()) {
            notificationFrame.dispose();
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
     * {@inheritDoc}
     */
    @Override
    public void setHovered(boolean ignored) {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getLabelText() {
        if (container instanceof JLabel label && !StringUtil.isNullOrEmpty(label.getText())) {
            return Optional.of(label.getText());
        }

        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setToStartAndEndingPosition() {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setToMidAnimationPosition() {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnimating() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContainerToString() {
        return container.toString();
    }
}
