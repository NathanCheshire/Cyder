package cyder.ui.frame.notification;

import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.drag.CyderDragLabel;
import cyder.user.UserDataManager;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;

/**
 * A custom notification component used for CyderFrames.
 */
public class CyderNotification extends JLabel {
    /**
     * The animation delay for the notification
     * moving through its parent container.
     */
    private static final Duration animationDelay = Duration.ofMillis(8);

    /**
     * The increment between setLocation calls for the
     * notification during the animation through the parent container.
     */
    private static final int animationIncrement = 8;

    public void appear(NotificationDirection notificationDirection, Component parent, long viewDuration) {
        CyderThreadRunner.submit(() -> {
            try {
                int bottomOffset = 5;

                switch (notificationDirection) {
                    case TOP -> {
                        setBounds(parent.getWidth() / 2 - getWidth() / 2,
                                CyderDragLabel.DEFAULT_HEIGHT - getHeight(), getWidth(), getHeight());
                        setVisible(true);
                        for (int i = getY() ; i < CyderDragLabel.DEFAULT_HEIGHT ; i += animationIncrement) {
                            if (shouldStopAnimation()) {
                                break;
                            }

                            setLocation(getX(), i);
                            ThreadUtil.sleep(animationDelay.toMillis());
                        }
                        setLocation(getX(), CyderDragLabel.DEFAULT_HEIGHT - 1);
                    }
                    case TOP_RIGHT -> {
                        setBounds(parent.getWidth() + getWidth(),
                                CyderDragLabel.DEFAULT_HEIGHT, getWidth(), getHeight());
                        setVisible(true);
                        for (int i = getX() ; i > parent.getWidth() - getWidth() + 5 ; i -= animationIncrement) {
                            if (shouldStopAnimation()) {
                                break;
                            }

                            setLocation(i, getY());
                            ThreadUtil.sleep(animationDelay.toMillis());
                        }
                        setLocation(parent.getWidth() - getWidth() + 5, getY());
                    }
                    case TOP_LEFT -> {
                        setBounds(-getWidth(), CyderDragLabel.DEFAULT_HEIGHT, getWidth(), getHeight());
                        setVisible(true);
                        for (int i = getX() ; i < 5 ; i += animationIncrement) {
                            if (shouldStopAnimation()) {
                                break;
                            }

                            setLocation(i, getY());
                            ThreadUtil.sleep(animationDelay.toMillis());
                        }
                        setLocation(2, getY());
                    }
                    case LEFT -> {
                        // note drag label used here to center on content pane
                        setBounds(-getWidth(), CyderDragLabel.DEFAULT_HEIGHT
                                + parent.getHeight() / 2 - getHeight() / 2, getWidth(), getHeight());
                        setVisible(true);
                        for (int i = getX() ; i < 5 ; i += animationIncrement) {
                            if (shouldStopAnimation()) {
                                break;
                            }

                            setLocation(i, getY());
                            ThreadUtil.sleep(animationDelay.toMillis());
                        }
                        setLocation(2, CyderDragLabel.DEFAULT_HEIGHT
                                + parent.getHeight() / 2 - getHeight() / 2);
                    }
                    case RIGHT -> {
                        // note drag label used here to center on content pane
                        setBounds(parent.getWidth() + getWidth(), CyderDragLabel.DEFAULT_HEIGHT
                                + parent.getHeight() / 2 - getHeight() / 2, getWidth(), getHeight());
                        setVisible(true);
                        for (int i = getX() ; i > parent.getWidth() - getWidth() + 5 ; i -= animationIncrement) {
                            if (shouldStopAnimation()) {
                                break;
                            }

                            setLocation(i, getY());
                            ThreadUtil.sleep(animationDelay.toMillis());
                        }
                        setLocation(parent.getWidth() - getWidth() + 5,
                                CyderDragLabel.DEFAULT_HEIGHT + parent.getHeight() / 2 - getHeight() / 2);
                    }
                    case BOTTOM -> {
                        setBounds(parent.getWidth() / 2 - getWidth() / 2, parent.getHeight()
                                + getHeight(), getWidth(), getHeight());
                        setVisible(true);
                        for (int i = getY() ; i > parent.getHeight() - getHeight() + 5 ; i -= animationIncrement) {
                            if (shouldStopAnimation()) {
                                break;
                            }

                            setLocation(getX(), i);
                            ThreadUtil.sleep(animationDelay.toMillis());
                        }
                        setBounds(parent.getWidth() / 2 - getWidth() / 2,
                                parent.getHeight() - getHeight() + 8, getWidth(), getHeight()); // 8 is arrow len
                    }
                    case BOTTOM_LEFT -> {
                        setBounds(-getWidth(), parent.getHeight() - getHeight()
                                - bottomOffset, getWidth(), getHeight());
                        setVisible(true);
                        for (int i = getX() ; i < 5 ; i += animationIncrement) {
                            if (shouldStopAnimation()) {
                                break;
                            }

                            setLocation(i, getY());
                            ThreadUtil.sleep(animationDelay.toMillis());
                        }
                        setLocation(2, parent.getHeight() - getHeight() - bottomOffset);
                    }
                    case BOTTOM_RIGHT -> {
                        setBounds(parent.getWidth() + getWidth(), parent.getHeight()
                                - getHeight() - bottomOffset, getWidth(), getHeight());
                        setVisible(true);
                        for (int i = getX() ; i > parent.getWidth() - getWidth() + 5 ; i -= animationIncrement) {
                            if (shouldStopAnimation()) {
                                break;
                            }

                            setLocation(i, getY());
                            ThreadUtil.sleep(animationDelay.toMillis());
                        }
                        setLocation(parent.getWidth() - getWidth() + 5,
                                parent.getHeight() - getHeight() - bottomOffset);
                    }
                    default -> throw new IllegalStateException(
                            "Illegal Notification Direction: " + notificationDirection);
                }


                if (!UserDataManager.INSTANCE.shouldPersistNotifications() && viewDuration != -1) {
                    vanish(notificationDirection, parent, viewDuration);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Notification Appear Animator");
    }

    /**
     * This method to be used in combination with an already visible
     * notification to immediately move it off of the parent until it is not visible.
     * Upon completing the animation, the notification is removed from the parent.
     *
     * @param notificationDirection the direction to exit to
     * @param parent                the component the notification is on. Used for bounds calculations
     * @param visibleTime           the delay before vanish
     */
    public void vanish(NotificationDirection notificationDirection, Component parent, long visibleTime) {
        CyderThreadRunner.submit(() -> {
            try {
                ThreadUtil.sleep(visibleTime);

                switch (notificationDirection) {
                    case TOP:
                        for (int i = getY() ; i > -getHeight() ; i -= animationIncrement) {
                            if (shouldStopAnimation()) {
                                break;
                            }

                            setBounds(getX(), i, getWidth(), getHeight());
                            ThreadUtil.sleep(animationDelay.toMillis());
                        }
                        break;
                    case BOTTOM:
                        for (int i = getY() ; i < parent.getHeight() - 5 ; i += animationIncrement) {
                            if (shouldStopAnimation()) {
                                break;
                            }

                            setBounds(getX(), i, getWidth(), getHeight());
                            ThreadUtil.sleep(animationDelay.toMillis());
                        }
                        break;
                    case TOP_LEFT:
                    case LEFT:
                    case BOTTOM_LEFT:
                        for (int i = getX() ; i > -getWidth() + 5 ; i -= animationIncrement) {
                            if (shouldStopAnimation()) {
                                break;
                            }

                            setBounds(i, getY(), getWidth(), getHeight());
                            ThreadUtil.sleep(animationDelay.toMillis());
                        }
                        break;
                    case RIGHT:
                    case BOTTOM_RIGHT:
                    case TOP_RIGHT:
                        for (int i = getX() ; i < parent.getWidth() - 5 ; i += animationIncrement) {
                            if (shouldStopAnimation()) {
                                break;
                            }

                            setBounds(i, getY(), getWidth(), getHeight());
                            ThreadUtil.sleep(animationDelay.toMillis());
                        }
                        break;
                }


                setVisible(false);
                repaint();
                // todo kill self
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Notification Vanish Animator");
    }

    private boolean shouldStopAnimation() {
        return false;
    }
}