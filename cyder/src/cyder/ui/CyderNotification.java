package cyder.ui;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.enums.Direction;
import cyder.enums.LoggerTag;
import cyder.enums.NotificationDirection;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
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
    /**
     * The width of the text label of the notification.
     */
    private int textWidth;

    /**
     * The height of the text label the notification.
     */
    private int textHeight;

    /**
     * The length of the notification arrow.
     */
    public static final int DEFAULT_ARROW_LEN = 6;

    /**
     * The arow length of this notification.
     */
    private int arrowLen = DEFAULT_ARROW_LEN;

    /**
     * Returns the arrow length for this notification.
     *
     * @return the arrow length for this notificaiton
     */
    public int getArrowLen() {
        return arrowLen;
    }

    /**
     * Sets the arrow length for this notification.
     *
     * @param arrowLen the arrow length for this notificaiton
     */
    public void setArrowLen(int arrowLen) {
        this.arrowLen = arrowLen;
    }

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
     * Whether to draw the arrow for a notification
     * or to hide it for a toast.
     */
    private boolean drawArrow = true;

    /**
     * Whether to draw the arrow on the notification or to disable it for a toast.
     *
     * @return whether to draw the arrow on the notification or to disable it for a toast
     */
    public boolean shouldDrawArrow() {
        return drawArrow;
    }

    /**
     * Sets whether to draw the arrow on the notification.
     *
     * @param drawArrow whether to draw the arrow on the notification
     */
    public void setDrawArrow(boolean drawArrow) {
        this.drawArrow = drawArrow;
    }

    /**
     * The x offset for the custom painted component.
     */
    private static final int xOff = 14;
    // todo diff
    /**
     * Returns the text x-offset from 0,0. 14 for notifications.
     *
     * @return the text x-offset from 0,0. 14 for notifications
     */
    public static int getTextXOffset() {
        return xOff;
    }

    /**
     * The y offset for the custom painted component.
     */
    private static final int yOff = 16;
    // todo diff
    /**
     * Returns the text y-offset from 0,0. 16 for notifications.
     *
     * @return the text y-offset from 0,0. 16 for notifications
     */
    public static int getTextYOffset() {
        return yOff;
    }

    /**
     * Sets the width.
     *
     * @param w the width
     */
    public void setTextWidth(int w) {
        textWidth = w;
    }

    /**
     * Sets the height.
     *
     * @param h the height
     */
    public void setTextHeight(int h) {
        textHeight = h;
    }

    /**
     * Returns the actual width of the component accounting
     * for the arrow and custom painted component.
     *
     * @return the width of the component as a whole
     */
    @Override
    public int getWidth() {
        // todo
        return textWidth + getTextXOffset() * 2 + ((arrowDir == Direction.LEFT
                || arrowDir == Direction.RIGHT) ? 6 : 0);
    }

    /**
     * Returns the actual height of the component accounting
     * for the arrow and custom painted component.
     *
     * @return the height of the component as a whole
     */
    @Override
    public int getHeight() {
        // todo
        return textHeight + getTextYOffset() * 2 + ((arrowDir == Direction.BOTTOM
                || arrowDir == Direction.TOP) ? arrowLen : 0);
    }

    /**
     * Sets the opacity to the provided value and repaints the notificaiton.
     *
     * @param opacity the notificaiton opacity
     */
    private void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    /**
     * The opacity for the notificaiton/toast.
     */
    private int opacity = 255;

    /**
     * The label the notification's text is on to change the opacity of
     */
    private JLabel textLabel;

    /**
     * Sets the text label of this notification so that we can control the opacity here.
     *
     * @param textLabel the text label on this notification
     */
    public void setTextLabel(JLabel textLabel) {
        this.textLabel = textLabel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;

        // if it's a toast, hide label temporarily
        if (textLabel != null && opacity < 128) {
            textLabel.setVisible(false);
        } else {
            textLabel.setVisible(true);
        }

        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHints(qualityHints);

        // draw the border, bigger bounds //todo yeah so changing this color does nothing....
        Color borderColor = CyderColors.regularPink;
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

    // ---------------------------------------------------
    // QueuedNotification components of notification class
    // ---------------------------------------------------

    /*
    Note: I am aware this could be a tagged class but per
          Effective Java item 23, tagged classes are generally
          a bad idea and class hierarchies should be used instead.
     */

    /**
     * The html styled text of the notificaiton.
     */
    private final String htmlText;

    /**
     * The duration the notification lasts after fully visible.
     */
    private final int duration;

    /**
     * The direction the arrow is painted.
     */
    private final Direction arrowDir;

    /**
     * The direction the notification appears/vanishes from/to.
     */
    private final NotificationDirection notificationDirection;

    /**
     * The action to invoke if the notification is killed via a user.
     */
    private final Runnable onKillAction;

    /**
     * The time the notificaiton was queued at.
     */
    private final String time;

    /**
     * A custom container for the notification. Needed for rare cases.
     */
    private final Container contianer;

    /**
     * The background of the notification. Only applicable if a
     * custom container is being used.
     */
    private Color notificationBackground = CyderColors.navy;

    /**
     * Constructs a new CyderNotification.
     *
     * @param text the html text for the eventual notification to display
     * @param dur the duration in miliseconds the notification should last for. Use 0 for auto-calculation
     * @param arrowDir the arrow direction
     * @param notificationDirection the notification direction
     * @param onKillAction the action to perform if the notification is dismissed by the user
     */
    public CyderNotification(String text, int dur, Direction arrowDir,
                              NotificationDirection notificationDirection,
                              Runnable onKillAction, Container container,
                              Color notificationBackground, String time) {
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(text.length() > 3);
        htmlText = text;

        duration = dur;
        contianer = container;
        this.arrowDir = arrowDir;
        this.notificationDirection = notificationDirection;
        this.onKillAction = onKillAction;
        this.notificationBackground = notificationBackground;
        this.time = time;

        killed = false;

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
     * Returns the html text.
     *
     * @return the html text
     */
    public String getHtmlText() {
        return htmlText;
    }

    /**
     * Returns the duration.
     *
     * @return the duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Returns the arrow direction.
     * @return the arrow direction
     */
    public Direction getArrowDir() {
        return arrowDir;
    }

    /**
     * Returns the notification direction.
     *
     * @return the notification direction
     */
    public NotificationDirection getNotificationDirection() {
        return notificationDirection;
    }

    /**
     * Returns the on kill action of this notification.
     *
     * @return the on kill action of this notification
     */
    public Runnable getOnKillAction() {
        return onKillAction;
    }

    /**
     * Returns the time this notifcation was addedd to the queue at.
     *
     * @return the time this notifcation was addedd to the queue at
     */
    public String getTime() {
        return time;
    }

    /**
     * The custom container for this notification.
     *
     * @return custom container for this notification
     */
    public Container getContianer() {
        return contianer;
    }

    /**
     * Returns the notification background for a
     * notification with a custom component.
     *
     * @return the notification background
     */
    public Color getNotificationBackground() {
        return notificationBackground;
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

            return arrowDir == other.getArrowDir()
                    && getWidth() == other.getWidth()
                    && getHeight() == other.getHeight()
                    && getHtmlText().equals(other.getHtmlText())
                    && getTime().equals(other.getTime());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = arrowDir.hashCode();
        ret = 31 * ret + Integer.hashCode(getWidth());
        ret = 31 * ret + Integer.hashCode(getHeight());
        ret = 31 * ret + htmlText.hashCode();
        ret = 31 * ret + time.hashCode();
        return ret;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }
}