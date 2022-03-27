package cyder.ui;

import cyder.constants.CyderColors;
import cyder.enums.Direction;
import cyder.enums.LoggerTag;
import cyder.enums.NotificationDirection;
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
    // ---------------------------------------
    // UI components of notification class
    // ---------------------------------------

    /**
     * The main axis length of the arrow.
     */
    private int arrowSize = 6;

    /**
     * The width of the notificaiton.
     */
    private int width = 300;

    /**
     * The height of the notification.
     */
    private int height = 300;

    /**
     * The arrow direction of the notification.
     */
    private Direction ArrowType = Direction.TOP;

    /**
     * Whether this notification has been killed.
     */
    private boolean killed;

    /**
     * The animation delay for the notification
     * moving through its parent container.
     */
    private static final int delay = 8;

    /**
     * The increment between setLocation calls for the
     * notification during the animation through the parent container.
     */
    private static final int increment = 8;

    /**
     * The background color of the notification.
     */
    private Color backgroundColor = CyderColors.notificationBackgroundColor;

    /**
     * Constructs a new notification.
     */
    public CyderNotification() {
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
     * Returns the background color.
     *
     * @return the background color
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color of this notificaiton.
     *
     * @param backgroundColor the background color of this notificaiton
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Returns the animation increment of this notification.
     *
     * @return the animation increment of this notification
     */
    public static int getIncrement() {
        return increment;
    }

    /**
     * Returns the delay of this notification.
     *
     * @return the delay of this notification
     */
    public static int getDelay() {
        return delay;
    }

    /**
     * Returns the text x-offset from 0,0. 14 for notifications.
     *
     * @return the text x-offset from 0,0. 14 for notifications
     */
    public static int getTextXOffset() {
        return 14;
    }

    /**
     * Returns the text y-offset from 0,0. 16 for notifications.
     *
     * @return the text y-offset from 0,0. 16 for notifications
     */
    public static int getTextYOffset() {
        return 16;
    }

    /**
     * Sets the arrow size.
     *
     * @param arrowSize the arrow size
     */
    public void setArrowSize(int arrowSize) {
        this.arrowSize = arrowSize;
    }

    /**
     * Returns the arrow size.
     *
     * @return the arrow size
     */
    public int getArrowSize() {
        return arrowSize;
    }

    /**
     * Sets the width.
     *
     * @param w the width
     */
    public void setWidth(int w) {
        width = w;
    }

    /**
     * Sets the height.
     *
     * @param h the height
     */
    public void setHeight(int h) {
        height = h;
    }

    /**
     * Sets the arrow direction.
     *
     * @param type the arrow direction
     */
    public void setArrow(Direction type) {
        ArrowType = type;
    }

    /**
     * Returns the arrow direction.
     *
     * @return the arrow direction
     */
    public Direction getArrow() {
        return ArrowType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        // overridden to account for custom paint component with arrow
        // width is actually x-offset of 14 * 2  and arrow size added in if applicable
        return width + getTextXOffset() * 2 + ((ArrowType == Direction.LEFT
                || ArrowType == Direction.RIGHT) ? arrowSize : 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        // overridden to account for custom paint component with arrow
        // height is actually y-offset of 16 * 2 and arrow size added in if applicable
        return height + getTextYOffset() * 2 + ((ArrowType == Direction.BOTTOM
                || ArrowType == Direction.TOP) ? arrowSize : 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;

        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHints(qualityHints);


        graphics2D.setPaint(CyderColors.notificationBorderColor);

        GeneralPath outlinePath = new GeneralPath();

        outlinePath.moveTo(8, 8 + 2);

        outlinePath.curveTo(8, 8 + 2,10,6 + 2, 12, 4 + 2);
        outlinePath.lineTo(width + 14 + 2, 4 + 2);

        outlinePath.curveTo(width + 14 + 2, 4 + 2,
                width + 16 + 2, 6 + 2,
                width + 18 + 2, 8 + 2);
        outlinePath.lineTo(width + 18 + 2, height + 10 + 2 + 2);

        outlinePath.curveTo(width + 18 + 2, height + 10 + 2 + 2,
                width + 16 + 2, height + 12 + 2  + 2,
                width + 14 + 2, height + 14 + 2  + 2);
        outlinePath.lineTo(12, height + 14 + 2 + 2);

        outlinePath.curveTo(12, height + 14 + 2 + 2,
                10, height + 12 + 2 + 2,
                8, height + 10 + 2 + 2);
        outlinePath.lineTo( 8, 8 + 2);

        switch (ArrowType) {
            case TOP:
                outlinePath.moveTo(6 + width / 2, 6 + 2);
                outlinePath.lineTo(14 + width / 2, -2 + 2);
                outlinePath.lineTo(22 + width / 2, 6 + 2);
                outlinePath.lineTo(6 + width / 2, 6 + 2);
                outlinePath.closePath();
                graphics2D.fill(outlinePath);
                break;
            case LEFT:
                outlinePath.moveTo(8, 2 + height / 2 + 2);
                outlinePath.lineTo(2, 10 + height / 2 + 2);
                outlinePath.lineTo(8, 18 + height / 2 + 2);
                outlinePath.lineTo(8, 2 + height / 2 + 2);
                outlinePath.closePath();
                graphics2D.fill(outlinePath);
                break;
            case RIGHT:
                outlinePath.moveTo(18 + width, 2 + height / 2 + 2);
                outlinePath.lineTo(26 + width, 10 + height / 2 + 2);
                outlinePath.lineTo(18 + width, 18 + height / 2 + 2);
                outlinePath.lineTo(18 + width, 2 + height / 2 + 2);
                outlinePath.closePath();
                graphics2D.fill(outlinePath);
                break;
            case BOTTOM:
                outlinePath.moveTo(8 + width / 2, 16 + height + 2);
                outlinePath.lineTo(14 + width / 2, 22 + height + 2);
                outlinePath.lineTo(20 + width / 2, 16 + height + 2);
                outlinePath.lineTo(8 + width / 2, 16 + height + 2);
                outlinePath.closePath();
                graphics2D.fill(outlinePath);
                break;
        }

        graphics2D.setPaint(backgroundColor);

        GeneralPath fillPath = new GeneralPath();

        fillPath.moveTo(10, 10 + 2);

        fillPath.curveTo(10, 10 + 2,12,8 + 2, 14, 6 + 2);
        fillPath.lineTo(width + 14, 6 + 2);

        fillPath.curveTo(width + 14, 6 + 2,
                width + 16, 8 + 2, width + 18, 10 + 2);
        fillPath.lineTo(width + 18, height + 10 + 2);

        fillPath.curveTo(width + 18, height + 10 + 2,
                width + 16, height + 12 + 2, width + 14, height + 14 + 2);
        fillPath.lineTo(14, height + 14 + 2);

        fillPath.curveTo(14, height + 14 + 2,
                12, height + 12 + 2, 10, height + 10 + 2);
        fillPath.lineTo( 10, 10 + 2);

        fillPath.closePath();
        graphics2D.fill(fillPath);

        switch (ArrowType) {
            case TOP:
                fillPath.moveTo(8 + width / 2, 6 + 2);
                fillPath.lineTo(14 + width / 2, 2);
                fillPath.lineTo(20 + width / 2, 6 + 2);
                fillPath.lineTo(8 + width / 2, 6 + 2);
                fillPath.closePath();
                graphics2D.fill(fillPath);
                break;
            case LEFT:
                fillPath.moveTo(10, 4 + height / 2 + 2);
                fillPath.lineTo(4, 10 + height / 2 + 2);
                fillPath.lineTo(10, 16 + height / 2 + 2);
                fillPath.lineTo(10, 4 + height / 2 + 2);
                fillPath.closePath();
                graphics2D.fill(fillPath);
                break;
            case RIGHT:
                fillPath.moveTo(18 + width, 4 + height / 2 + 2);
                fillPath.lineTo(24 + width, 10 + height / 2 + 2);
                fillPath.lineTo(18 + width, 16 + height / 2 + 2);
                fillPath.lineTo(18 + width, 4 + height / 2 + 2);
                fillPath.closePath();
                graphics2D.fill(fillPath);
                break;
            case BOTTOM:
                fillPath.moveTo(8 + width / 2, 14 + height + 2);
                fillPath.lineTo(14 + width / 2, 20 + height + 2);
                fillPath.lineTo(20 + width / 2, 14 + height + 2);
                fillPath.lineTo(8 + width / 2, 14 + height + 2);
                fillPath.closePath();
                graphics2D.fill(fillPath);
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
                setVisible(true);
                switch (notificationDirection) {
                    case TOP:
                        for (int i = getY(); i < CyderDragLabel.DEFAULT_HEIGHT; i += increment) {
                            if (killed)
                                break;

                            setBounds(getX(), i, getWidth(), getHeight());
                            Thread.sleep(CyderNotification.delay);
                        }
                        setBounds(getX(), CyderDragLabel.DEFAULT_HEIGHT - 1, getWidth(), getHeight());
                        break;
                    case TOP_RIGHT:
                        for (int i = getX(); i > parent.getWidth() - getWidth() + 5; i -= increment) {
                            if (killed)
                                break;

                            setBounds(i, getY(), getWidth(), getHeight());
                            Thread.sleep(CyderNotification.delay);
                        }
                        setBounds(parent.getWidth() - getWidth() + 5, getY(), getWidth(), getHeight());
                        break;
                    case TOP_LEFT:
                        for (int i = getX(); i < 5; i += increment) {
                            if (killed)
                                break;

                            setBounds(i, getY(), getWidth(), getHeight());
                            Thread.sleep(CyderNotification.delay);
                        }
                        setBounds(2, getY(), getWidth(), getHeight());
                        break;
                    case LEFT:
                        for (int i = getX() ; i < 5 ; i+= increment) {
                            if (killed)
                                break;

                            setBounds(i, getY(), getWidth(), getHeight());
                            Thread.sleep(CyderNotification.delay);
                        }
                        setBounds(2, parent.getHeight() / 2 - getHeight() / 2, getWidth(), getHeight());
                        break;
                    case RIGHT:
                        for (int i = getX(); i > parent.getWidth() - getWidth() + 5; i -= increment) {
                            if (killed)
                                break;

                            setBounds(i, getY(), getWidth(), getHeight());
                            Thread.sleep(CyderNotification.delay);
                        }
                        setBounds(parent.getWidth() - getWidth() + 5,
                                parent.getHeight() / 2 - getHeight() / 2, getWidth(), getHeight());
                        break;
                    case BOTTOM:
                        for (int i = getY(); i > parent.getHeight() - getHeight() + 5; i -= increment) {
                            if (killed)
                                break;

                            setBounds(getX(), i, getWidth(), getHeight());
                            Thread.sleep(CyderNotification.delay);
                        }
                        setBounds(getX(), parent.getHeight() - getHeight() + 10, getWidth(), getHeight());
                        break;
                    case BOTTOM_LEFT:
                        for (int i = getX(); i < 5; i += increment) {
                            if (killed)
                                break;

                            setBounds(i, getY(), getWidth(), getHeight());
                            Thread.sleep(CyderNotification.delay);
                        }
                        setBounds(2, parent.getHeight() - getHeight() + 10, getWidth(), getHeight());
                        break;
                    case BOTTOM_RIGHT:
                        for (int i = getX(); i > parent.getWidth() - getWidth() + 5; i -= increment) {
                            if (killed)
                                break;

                            setBounds(i, getY(), getWidth(), getHeight());
                            Thread.sleep(CyderNotification.delay);
                        }
                        setBounds(parent.getWidth() - getWidth() + 5,
                                parent.getHeight() - getHeight() + 10, getWidth(), getHeight());
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + notificationDirection);
                }

                //now that it's visible, call vanish with the proper delay if enabled
                if (UserUtil.getUserData("persistentnotifications").equals("0") && delay != -1)
                    vanish(notificationDirection, parent, delay);
            }

            catch (Exception e) {
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
                Thread.sleep(delay);

                switch(notificationDirection) {
                    case TOP:
                        for (int i = getY() ; i > - getHeight() ; i -= increment) {
                            if (killed)
                                break;

                            setBounds(getX(), i, getWidth(), getHeight());
                            Thread.sleep(CyderNotification.delay);
                        }
                        break;
                    case BOTTOM:
                        for (int i = getY() ; i < parent.getHeight() - 5 ; i += increment) {
                            if (killed)
                                break;

                            setBounds(getX(), i, getWidth(), getHeight());
                            Thread.sleep(CyderNotification.delay);
                        }
                        break;
                    case TOP_LEFT:
                    case LEFT:
                    case BOTTOM_LEFT:
                        for (int i = getX() ; i > -getWidth() + 5 ; i -= increment) {
                            if (killed)
                                break;

                            setBounds(i, getY(), getWidth(), getHeight());
                            Thread.sleep(CyderNotification.delay);
                        }
                        break;
                    case RIGHT:
                    case BOTTOM_RIGHT:
                    case TOP_RIGHT:
                        for (int i = getX() ; i < parent.getWidth() - 5 ; i += increment) {
                            if (killed)
                                break;

                            setBounds(i, getY(), getWidth(), getHeight());
                            Thread.sleep(CyderNotification.delay);
                        }
                        break;
                }

                if (isVisible() && this != null) {
                    getParent().remove(this);
                    setVisible(false);
                }
            }

            catch (Exception e) {
               ExceptionHandler.handle(e);
            }
        },"Notificaiton Vanish Animator");
    }

    // ---------------------------------------------------
    // QueuedNotification components of notification class
    // ---------------------------------------------------

    /**
     * The html styled text of the notificaiton.
     */
    private String htmlText;

    /**
     * The duration the notification lasts after fully visible.
     */
    private int duration;

    /**
     * The direction the arrow is painted.
     */
    private Direction arrowDir;

    /**
     * The direction the notification appears/vanishes from/to.
     */
    private NotificationDirection notificationDirection;

    /**
     * The action to invoke if the notification is killed via a user.
     */
    private Runnable onKillAction;

    /**
     * The time the notificaiton was queued at.
     */
    private String time;

    /**
     * A custom container for the notification. Needed for rare cases.
     */
    private Container contianer;

    /**
     * The background of the notification. Only applicable if a
     * custom container is being used.
     */
    private Color notificationBackground;

    /**
     * Initialies the variables needed to draw the component to the frame.
     *
     * @param text the html text for the eventual notification to display
     * @param dur the duration in miliseconds the notification should last for. Use 0 for auto-calculation
     * @param arrowDir the arrow direction
     * @param notificationDirection the notification direction
     * @param onKillAction the action to perform if the notification is dismissed by the user
     */
    public void initializeQueueVars(String text, int dur, Direction arrowDir,
                              NotificationDirection notificationDirection,
                              Runnable onKillAction, Container container,
                              Color notificationBackground, String time) {
        htmlText = text;
        duration = dur;
        contianer = container;
        this.arrowDir = arrowDir;
        this.notificationDirection = notificationDirection;
        this.onKillAction = onKillAction;
        this.notificationBackground = notificationBackground;
        this.time = time;
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

            return getArrow() == other.getArrow()
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
        int ret = getArrow().hashCode();
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