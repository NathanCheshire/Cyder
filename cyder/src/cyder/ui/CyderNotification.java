package cyder.ui;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.enums.LoggerTag;
import cyder.enums.NotificationDirection;
import cyder.enums.NotificationType;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.ui.objects.NotificationBuilder;
import cyder.utilities.ReflectionUtil;
import cyder.utilities.UserUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;

/**
 * A custom notification component used for CyderFrames.
 */
public class CyderNotification extends JLabel {
    /*
    Note: I am aware this could be a tagged class but per
          Effective Java item 23, tagged classes are generally
          a bad idea and class hierarchies should be used instead.
          I've opted to use an enum in the builder pattern
           used for notifications, however.
     */

    /**
     * The builder to construct this notification/toast.
     */
    private final NotificationBuilder builder;

    /**
     * Constructs a new CyderNotification.
     *
     * @param builder the notification builder to construct the notification
     *                when it is pulled from the notificaiton queue for
     *                the frame it was notified from.
     */
    public CyderNotification(NotificationBuilder builder) {
        Preconditions.checkNotNull(builder);
        this.builder = builder;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(LoggerTag.UI_ACTION, e.getComponent());
            }
        });

        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    /**
     * Suppress default constructor.
     */
    private CyderNotification() {
        throw new IllegalMethodException("Instantiation not allowed without valid parameters");
    }

    /**
     * The length of the notification arrow.
     */
    public static final int DEFAULT_ARROW_LEN = 6;

    /**
     * The arow length of this notification.
     * This supports changing the arrow length in the future if needed.
     */
    private final int arrowLen = DEFAULT_ARROW_LEN;

    /**
     * Whether this notification has been killed.
     */
    private boolean killed;

    /**
     * The animation delay for the notification
     * moving through its parent container.
     */
    private static final int DELAY = 8;

    /**
     * The increment between setLocation calls for the
     * notification during the animation through the parent container.
     */
    private static final int INCREMENT = 8;

    /**
     * The opacity for the toast animation if the type is a toast.
     */
    private int opacity = 255;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;

        if (builder.getNotificationType() == NotificationType.TOAST) {
            builder.getContainer()
        }

        // if it's a toast, hide label temporarily
        if (textLabel != null && opacity < 128) {
            textLabel.setVisible(false);
        } else {
            textLabel.setVisible(true);
        }

        // some fancy shit or whatever
        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHints(qualityHints);

        // draw the border, bigger bounds //todo yeah so changing this color does nothing....
        Color borderColor = CyderColors.notificationBorderColor;
        graphics2D.setPaint(new Color(
                borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), opacity));

        GeneralPath outlinePath = new GeneralPath();

        outlinePath.moveTo(8, 8 + 2);

        outlinePath.curveTo(8, 8 + 2,10,6 + 2, 12, 4 + 2);
        outlinePath.lineTo(textWidth + 14 + 2, 4 + 2);

        outlinePath.curveTo(textWidth + 14 + 2, 4 + 2,
                textWidth + 16 + 2, 6 + 2,
                textWidth + 18 + 2, 8 + 2);
        outlinePath.lineTo(textWidth + 18 + 2, textHeight + 10 + 2 + 2);

        outlinePath.curveTo(textWidth + 18 + 2, textHeight + 10 + 2 + 2,
                textWidth + 16 + 2, textHeight + 12 + 2  + 2,
                textWidth + 14 + 2, textHeight + 14 + 2  + 2);
        outlinePath.lineTo(12, textHeight + 14 + 2 + 2);

        outlinePath.curveTo(12, textHeight + 14 + 2 + 2,
                10, textHeight + 12 + 2 + 2,
                8, textHeight + 10 + 2 + 2);
        outlinePath.lineTo( 8, 8 + 2);

        // draw the border arrow, bigger b ounds
        if (drawArrow) {
            switch (arrowDir) {
                case TOP:
                    outlinePath.moveTo(6 + textWidth / 2, 6 + 2);
                    outlinePath.lineTo(14 + textWidth / 2, -2 + 2);
                    outlinePath.lineTo(22 + textWidth / 2, 6 + 2);
                    outlinePath.lineTo(6 + textWidth / 2, 6 + 2);
                    outlinePath.closePath();
                    graphics2D.fill(outlinePath);
                    break;
                case LEFT:
                    outlinePath.moveTo(8, 2 + textHeight / 2 + 2);
                    outlinePath.lineTo(2, 10 + textHeight / 2 + 2);
                    outlinePath.lineTo(8, 18 + textHeight / 2 + 2);
                    outlinePath.lineTo(8, 2 + textHeight / 2 + 2);
                    outlinePath.closePath();
                    graphics2D.fill(outlinePath);
                    break;
                case RIGHT:
                    outlinePath.moveTo(18 + textWidth, 2 + textHeight / 2 + 2);
                    outlinePath.lineTo(26 + textWidth, 10 + textHeight / 2 + 2);
                    outlinePath.lineTo(18 + textWidth, 18 + textHeight / 2 + 2);
                    outlinePath.lineTo(18 + textWidth, 2 + textHeight / 2 + 2);
                    outlinePath.closePath();
                    graphics2D.fill(outlinePath);
                    break;
                case BOTTOM:
                    outlinePath.moveTo(8 + textWidth / 2, 16 + textHeight + 2);
                    outlinePath.lineTo(14 + textWidth / 2, 22 + textHeight + 2);
                    outlinePath.lineTo(20 + textWidth / 2, 16 + textHeight + 2);
                    outlinePath.lineTo(8 + textWidth / 2, 16 + textHeight + 2);
                    outlinePath.closePath();
                    graphics2D.fill(outlinePath);
                    break;
            }
        }

        // fill the background color, smaller bounds
        Color backgroundColor = CyderColors.notificationBackgroundColor;
        graphics2D.setPaint(new Color(
                backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), opacity));

        GeneralPath fillPath = new GeneralPath();

        fillPath.moveTo(10, 10 + 2);

        fillPath.curveTo(10, 10 + 2,12,8 + 2, 14, 6 + 2);
        fillPath.lineTo(textWidth + 14, 6 + 2);

        fillPath.curveTo(textWidth + 14, 6 + 2,
                textWidth + 16, 8 + 2, textWidth + 18, 10 + 2);
        fillPath.lineTo(textWidth + 18, textHeight + 10 + 2);

        fillPath.curveTo(textWidth + 18, textHeight + 10 + 2,
                textWidth + 16, textHeight + 12 + 2, textWidth + 14, textHeight + 14 + 2);
        fillPath.lineTo(14, textHeight + 14 + 2);

        fillPath.curveTo(14, textHeight + 14 + 2,
                12, textHeight + 12 + 2, 10, textHeight + 10 + 2);
        fillPath.lineTo( 10, 10 + 2);

        fillPath.closePath();
        graphics2D.fill(fillPath);

        // draw the arrow fill, smaller bounds
        if (drawArrow) {
            switch (arrowDir) {
                case TOP:
                    fillPath.moveTo(8 + textWidth / 2, 6 + 2);
                    fillPath.lineTo(14 + textWidth / 2, 2);
                    fillPath.lineTo(20 + textWidth / 2, 6 + 2);
                    fillPath.lineTo(8 + textWidth / 2, 6 + 2);
                    fillPath.closePath();
                    graphics2D.fill(fillPath);
                    break;
                case LEFT:
                    fillPath.moveTo(10, 4 + textHeight / 2 + 2);
                    fillPath.lineTo(4, 10 + textHeight / 2 + 2);
                    fillPath.lineTo(10, 16 + textHeight / 2 + 2);
                    fillPath.lineTo(10, 4 + textHeight / 2 + 2);
                    fillPath.closePath();
                    graphics2D.fill(fillPath);
                    break;
                case RIGHT:
                    fillPath.moveTo(18 + textWidth, 4 + textHeight / 2 + 2);
                    fillPath.lineTo(24 + textWidth, 10 + textHeight / 2 + 2);
                    fillPath.lineTo(18 + textWidth, 16 + textHeight / 2 + 2);
                    fillPath.lineTo(18 + textWidth, 4 + textHeight / 2 + 2);
                    fillPath.closePath();
                    graphics2D.fill(fillPath);
                    break;
                case BOTTOM:
                    fillPath.moveTo(8 + textWidth / 2, 14 + textHeight + 2);
                    fillPath.lineTo(14 + textWidth / 2, 20 + textHeight + 2);
                    fillPath.lineTo(20 + textWidth / 2, 14 + textHeight + 2);
                    fillPath.lineTo(8 + textWidth / 2, 14 + textHeight + 2);
                    fillPath.closePath();
                    graphics2D.fill(fillPath);
            }
        }
    }

    /**
     * Animates in the notification on the parent container.
     * The components position is expected to have already
     * been set out of bounds on the parent.
     *
     * @param notificationDirection the direction for the notification to enter and exit from.
     */
    public void appear(NotificationDirection notificationDirection, Component parent, int delay) {
        CyderThreadRunner.submit(() -> {
            try {
                // if a toast
                if (!drawArrow) {
                    // centered on x, y has offset of 10 pixels from bottom
                    setBounds(getX(), parent.getHeight() - getHeight() + 10, getWidth(), getHeight());
                    opacity = 0;
                    setVisible(true);

                    for (int i = 0 ; i < 256 ; i++) {
                        opacity = i;
                        repaint();
                        Thread.sleep(2);
                    }
                } else {
                    // location is expected to have been set already
                    setVisible(true);


                    switch (notificationDirection) {
                        case TOP:
                            for (int i = getY(); i < CyderDragLabel.DEFAULT_HEIGHT; i += INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(getX(), i, getWidth(), getHeight());
                                Thread.sleep(DELAY);
                            }
                            setBounds(getX(), CyderDragLabel.DEFAULT_HEIGHT - 1, getWidth(), getHeight());
                            break;
                        case TOP_RIGHT:
                            for (int i = getX(); i > parent.getWidth() - getWidth() + 5; i -= INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(i, getY(), getWidth(), getHeight());
                                Thread.sleep(DELAY);
                            }
                            setBounds(parent.getWidth() - getWidth() + 5, getY(), getWidth(), getHeight());
                            break;
                        case TOP_LEFT:
                            for (int i = getX(); i < 5; i += INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(i, getY(), getWidth(), getHeight());
                                Thread.sleep(DELAY);
                            }
                            setBounds(2, getY(), getWidth(), getHeight());
                            break;
                        case LEFT:
                            for (int i = getX() ; i < 5 ; i+= INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(i, getY(), getWidth(), getHeight());
                                Thread.sleep(DELAY);
                            }
                            setBounds(2, parent.getHeight() / 2 - getHeight() / 2, getWidth(), getHeight());
                            break;
                        case RIGHT:
                            for (int i = getX(); i > parent.getWidth() - getWidth() + 5; i -= INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(i, getY(), getWidth(), getHeight());
                                Thread.sleep(DELAY);
                            }
                            setBounds(parent.getWidth() - getWidth() + 5,
                                    parent.getHeight() / 2 - getHeight() / 2, getWidth(), getHeight());
                            break;
                        case BOTTOM:
                            for (int i = getY(); i > parent.getHeight() - getHeight() + 5; i -= INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(getX(), i, getWidth(), getHeight());
                                Thread.sleep(DELAY);
                            }
                            setBounds(getX(), parent.getHeight() - getHeight() + 10, getWidth(), getHeight());
                            break;
                        case BOTTOM_LEFT:
                            for (int i = getX(); i < 5; i += INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(i, getY(), getWidth(), getHeight());
                                Thread.sleep(DELAY);
                            }
                            setBounds(2, parent.getHeight() - getHeight() + 10, getWidth(), getHeight());
                            break;
                        case BOTTOM_RIGHT:
                            for (int i = getX(); i > parent.getWidth() - getWidth() + 5; i -= INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(i, getY(), getWidth(), getHeight());
                                Thread.sleep(DELAY);
                            }
                            setBounds(parent.getWidth() - getWidth() + 5,
                                    parent.getHeight() - getHeight() + 10, getWidth(), getHeight());
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + notificationDirection);
                    }
                }

                // call vanish now visible and not set to stay until dismissed
                if (UserUtil.getCyderUser().getPersistentnotifications().equals("0") && delay != -1) {
                    vanish(notificationDirection, parent, delay);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        },"Notification Appear Animatior");
    }

    /**
     * Kill the notification by stopping all animation threads
     * and setting this visibility to false.
     *
     * Note: you should not make a killed notification
     * visible again via {@link Component#setVisible(boolean)}.
     */
    public void kill() {
        killed = true;
        setVisible(false);
    }

    /**
     * The direction this notification should vanish
     * if no vanish direction can be provided.
     */
    private NotificationDirection vanishDirection;

    /**
     * Sets the vanish direction for this notification.
     *
     * @param vanishDirection the vanish direction for this notification
     */
    public void setVanishDirection(NotificationDirection vanishDirection) {
        this.vanishDirection = vanishDirection;
    }

    /**
     * Vanishes the current notification using the currently set notification direction.
     *
     * @param parent the component the notification is on
     */
    protected void vanish(Component parent) {
        vanish(vanishDirection, parent, 0);
    }

    /**
     * This method to be used in combination with an already visible
     * notification to immediately move it off of the parent until it is not visible.
     * Upon completing the animation, the notification is removed from the parent.
     *
     * @param notificationDirection the direction to exit to
     * @param parent the component the notification is on. Used for bounds calculations
     * @param delay the delay before vanish
     */
    protected void vanish(NotificationDirection notificationDirection, Component parent, int delay) {
        CyderThreadRunner.submit(() -> {
            try {
                // delay before vanishing
                Thread.sleep(delay);

                // if a toast
                if (!drawArrow) {
                    for (int i = 255 ; i >= 0 ; i--) {
                        opacity = i;
                        repaint();
                        Thread.sleep(2);
                    }

                    Container parentcomp = getParent();
                    parentcomp.remove(this);
                    setVisible(false);
                    parentcomp.repaint();
                } else {
                    switch(notificationDirection) {
                        case TOP:
                            for (int i = getY() ; i > - getHeight() ; i -= INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(getX(), i, getWidth(), getHeight());
                                Thread.sleep(DELAY);
                            }
                            break;
                        case BOTTOM:
                            for (int i = getY() ; i < parent.getHeight() - 5 ; i += INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(getX(), i, getWidth(), getHeight());
                                Thread.sleep(DELAY);
                            }
                            break;
                        case TOP_LEFT:
                        case LEFT:
                        case BOTTOM_LEFT:
                            for (int i = getX() ; i > -getWidth() + 5 ; i -= INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(i, getY(), getWidth(), getHeight());
                                Thread.sleep(DELAY);
                            }
                            break;
                        case RIGHT:
                        case BOTTOM_RIGHT:
                        case TOP_RIGHT:
                            for (int i = getX() ; i < parent.getWidth() - 5 ; i += INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(i, getY(), getWidth(), getHeight());
                                Thread.sleep(DELAY);
                            }
                            break;
                    }
                }

                // if stil visible, remove and set visibility to false
                if (isVisible()) {
                    getParent().remove(this);
                }

                setVisible(false);
            } catch (Exception e) {
               ExceptionHandler.handle(e);
            }
        },"Notificaiton Vanish Animator");
    }

    // -------------------------------------------------------
    // Primary methods to override according to Effective Java
    // -------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof CyderNotification)) {
            return false;
        } else {
            CyderNotification other = (CyderNotification) o;

            // TODO
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        // TODO
        return 0;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }
}