package cyder.ui;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.enums.Direction;
import cyder.enums.LoggerTag;
import cyder.enums.NotificationDirection;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.ui.objects.NotificationBuilder;
import cyder.utilities.ReflectionUtil;
import cyder.utilities.UserUtil;
import cyder.utilities.enums.NotificationType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;

// todo be able to download ffmpeg and ffprobe.exe if user confirms they want to

// todo be able to download ffmpeg.exe and ffprobe.exe, prompt user to download and setpaths automatically
//  OR set path via user editor, place in dynamic/exes

// todo audio player should be able to search for songs on youtube and display preview of top 10 results
//  and click on one to download

// todo dreamify checkbox for audio player, will need to generate wav first time in tmp and play from that
// after conversion finished, should be seamless audio transition

/**
 * A custom notification component used for CyderFrames.
 */
public class CyderNotification extends JLabel {
    /*
    Note: I am aware this could be a tagged class but per
          Effective Java item 23, tagged classes are
          a bad idea and class hierarchies should be used instead.
          I've opted to use an enum in the builder pattern
          used for notifications, however.
     */

    /**
     * The length of the notification arrow above the border.
     */
    public static final int DEFAULT_ARROW_LEN = 8;

    /**
     * The length of the border around the notification
     */
    public static final int DEFAULT_BORDER_LEN = 5;

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
    private static final int ANIMATION_DELAY = 8;

    /**
     * The increment between setLocation calls for the
     * notification during the animation through the parent container.
     */
    private static final int ANIMATION_INCREMENT = 8;

    /**
     * The opacity for the toast animation if the type is a toast.
     */
    private int opacity = 255;

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
     * Returns the notification builder for this notification.
     *
     * @return the notification builder for this notification
     */
    public NotificationBuilder getBuilder() {
        return builder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(Graphics g) {
        // ensure criteria met
        Preconditions.checkNotNull(builder);
        Preconditions.checkNotNull(builder.getContainer());

        int componentWidth = builder.getContainer().getWidth();
        int componentHeight = builder.getContainer().getHeight();

        // obtain painting object
        Graphics2D graphics2D = (Graphics2D) g;

        // containers are set to invisible if the opacity is less than half
        builder.getContainer().setVisible(builder.getNotificationType() != NotificationType.TOAST || opacity >= 128);

        // some fancy rendering or whatever
        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHints(qualityHints);

        // draw the bigger shape to hold the smaller one
        Color borderColor = CyderColors.regularPink;
        graphics2D.setPaint(new Color(borderColor.getRed(), borderColor.getGreen(),
                                      borderColor.getBlue(), opacity));

        GeneralPath outlinePath = new GeneralPath();

        int curveInc = 2;

        // already at 0,0
        // move out of way to draw since arrow might be left or right
        int x = 0;
        int y = 0;

        if (builder.getArrowDir() == Direction.LEFT) {
            x = DEFAULT_ARROW_LEN;
        }

        if (builder.getArrowDir() == Direction.TOP) {
            y = DEFAULT_ARROW_LEN;
        }

        // always 4 more down due to curve up 2 and then another 2
        y += 2 * curveInc;

        outlinePath.moveTo(x, y);

        // curve up 2 and right 2, twice
        outlinePath.curveTo(x, y,x + 2,y - 2, x + 4, y - 4);

        // new x,y we're at
        x += 4;
        y -= 4;

        // line right for component width
        outlinePath.lineTo(x + componentWidth, y);

        // new x
        x += componentWidth;

        // curve down 2 and right 2, twice
        outlinePath.curveTo(x, y, x + 2, y + 2, x + 4, y + 4);

        // new x,y we're at
        x += 4;
        y += 4;

        // line down for component height
        outlinePath.lineTo(x, y + componentHeight);

        // new y
        y += componentHeight;

        // curve down 2 and left 2, twice
        outlinePath.curveTo(x, y, x - 2, y + 2, x - 4, y + 4);

        // new x,y we're at
        x -= 4;
        y += 4;

        // line left for component width
        outlinePath.lineTo(x - componentWidth, y);

        // new x
        x -= componentWidth;

        // curve up 2 and left 2, twice
        outlinePath.curveTo(x, y, x - 2, y - 2, x - 4, y - 4);

        // new x,y we're at
        x -= 4;
        y -= 4;

        // line up for component height
        outlinePath.lineTo(x, y - componentHeight);

        // new y
        y -= componentHeight;

        // close and fill
        outlinePath.closePath();
        graphics2D.fill(outlinePath);

        // draw the border arrow if not a toast
        if (builder.getNotificationType() != NotificationType.TOAST) {
            int len = DEFAULT_ARROW_LEN;

            switch (builder.getArrowDir()) {
                case TOP:
                    // top so we know that the x needs to be offset by 4 and the height by arrow len
                    outlinePath.moveTo(2 * 2 + componentWidth / 2 - len, len);
                    outlinePath.lineTo(2 * 2 + componentWidth / 2, 0);
                    outlinePath.lineTo(2 * 2 + (componentWidth / 2) +  len, len);
                    outlinePath.lineTo(2 * 2 + componentWidth / 2 - len, len);

                    break;
                case LEFT:
                    // left so we know that the x needs to be offset by arrow len and the height by 4
                    outlinePath.moveTo(len, 2 * 2 + componentHeight / 2 - len);
                    outlinePath.lineTo(0, 2 * 2 + componentHeight / 2);
                    outlinePath.lineTo(len, 2 * 2 + componentHeight / 2 + len);
                    outlinePath.moveTo(len, 2 * 2 + componentHeight / 2 - len);

                    break;
                case RIGHT:
                    // right so we know that the x needs to be offset by 4 * 2 + componentWidth
                    // and the height by 2 * 2 + componentHeight / 2 - len
                    outlinePath.moveTo(2 * 2 * 2 + componentWidth, 2 * 2 + componentHeight / 2 - len);
                    outlinePath.lineTo(2 * 2 * 2 + componentWidth + len, 2 * 2 + componentHeight / 2);
                    outlinePath.lineTo(2 * 2 * 2 + componentWidth, 2 * 2 + componentHeight / 2 + len);
                    outlinePath.moveTo(2 * 2 * 2 + componentWidth, 2 * 2 + componentHeight / 2 - len);

                    break;
                case BOTTOM:

                    break;
            }
        }

        // close and fill
        outlinePath.closePath();
        graphics2D.fill(outlinePath);

        // fill the background color, smaller bounds
//        Color backgroundColor = CyderColors.notificationBackgroundColor;
//        graphics2D.setPaint(new Color(
//                backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), opacity));
//
//        GeneralPath fillPath = new GeneralPath();
//
//        fillPath.moveTo(10, 10 + 2);

//        fillPath.curveTo(10, 10 + 2,12,8 + 2, 14, 6 + 2);
//        fillPath.lineTo(textWidth + 14, 6 + 2);
//
//        fillPath.curveTo(textWidth + 14, 6 + 2,
//                textWidth + 16, 8 + 2, textWidth + 18, 10 + 2);
//        fillPath.lineTo(textWidth + 18, textHeight + 10 + 2);
//
//        fillPath.curveTo(textWidth + 18, textHeight + 10 + 2,
//                textWidth + 16, textHeight + 12 + 2, textWidth + 14, textHeight + 14 + 2);
//        fillPath.lineTo(14, textHeight + 14 + 2);
//
//        fillPath.curveTo(14, textHeight + 14 + 2,
//                12, textHeight + 12 + 2, 10, textHeight + 10 + 2);
//        fillPath.lineTo( 10, 10 + 2);

//        fillPath.closePath();
//        graphics2D.fill(fillPath);

        // draw the arrow fill, smaller bounds
        if (builder.getNotificationType() == NotificationType.TOAST) {
            switch (builder.getArrowDir()) {
//                case TOP:
//                    fillPath.moveTo(8 + textWidth / 2, 6 + 2);
//                    fillPath.lineTo(14 + textWidth / 2, 2);
//                    fillPath.lineTo(20 + textWidth / 2, 6 + 2);
//                    fillPath.lineTo(8 + textWidth / 2, 6 + 2);
//                    fillPath.closePath();
//                    graphics2D.fill(fillPath);
//                    break;
//                case LEFT:
//                    fillPath.moveTo(10, 4 + textHeight / 2 + 2);
//                    fillPath.lineTo(4, 10 + textHeight / 2 + 2);
//                    fillPath.lineTo(10, 16 + textHeight / 2 + 2);
//                    fillPath.lineTo(10, 4 + textHeight / 2 + 2);
//                    fillPath.closePath();
//                    graphics2D.fill(fillPath);
//                    break;
//                case RIGHT:
//                    fillPath.moveTo(18 + textWidth, 4 + textHeight / 2 + 2);
//                    fillPath.lineTo(24 + textWidth, 10 + textHeight / 2 + 2);
//                    fillPath.lineTo(18 + textWidth, 16 + textHeight / 2 + 2);
//                    fillPath.lineTo(18 + textWidth, 4 + textHeight / 2 + 2);
//                    fillPath.closePath();
//                    graphics2D.fill(fillPath);
//                    break;
//                case BOTTOM:
//                    fillPath.moveTo(8 + textWidth / 2, 14 + textHeight + 2);
//                    fillPath.lineTo(14 + textWidth / 2, 20 + textHeight + 2);
//                    fillPath.lineTo(20 + textWidth / 2, 14 + textHeight + 2);
//                    fillPath.lineTo(8 + textWidth / 2, 14 + textHeight + 2);
//                    fillPath.closePath();
//                    graphics2D.fill(fillPath);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        int ret = 2 * DEFAULT_BORDER_LEN + builder.getContainer().getWidth();

        if (builder.getArrowDir() == Direction.LEFT) {
            ret += DEFAULT_ARROW_LEN;
        } else if (builder.getArrowDir() == Direction.RIGHT) {
            ret -= DEFAULT_ARROW_LEN;
        }

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        int ret = 2 * DEFAULT_BORDER_LEN + builder.getContainer().getHeight();

        if (builder.getArrowDir() == Direction.TOP) {
            ret += DEFAULT_ARROW_LEN;
        } else if (builder.getArrowDir() == Direction.BOTTOM) {
            ret -= DEFAULT_ARROW_LEN;
        }

        return ret;
    }

    /**
     * Returns the height of this notification accounting for the custom
     * painting of the container, border, and arrow. This width is intended
     * to be used forcomponent centering.
     *
     * @return the height of this custom notification component
     */
    public int getTrueWidth() {
        return (builder.getArrowDir() == Direction.LEFT || builder.getArrowDir() == Direction.RIGHT
                ? arrowLen : 0) + 2 * DEFAULT_BORDER_LEN + builder.getContainer().getWidth();
    }

    /**
     * Returns the height of this notification accounting for the custom
     * painting of the container, border, and arrow. This height is intended
     * to be used for component centering.
     *
     * @return the height of this custom notification component
     */
    public int getTrueHeight() {
        return (builder.getArrowDir() == Direction.TOP || builder.getArrowDir() == Direction.BOTTOM
                ? arrowLen : 0) + 2 * DEFAULT_BORDER_LEN + builder.getContainer().getHeight();
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
                if (builder.getNotificationType() == NotificationType.TOAST) {
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
                    // location is expected to have been set already by parent before invoking appear method
                    setVisible(true);

                    switch (notificationDirection) {
                        case TOP:
                            for (int i = getY(); i < CyderDragLabel.DEFAULT_HEIGHT; i += ANIMATION_INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(getX(), i, getWidth(), getHeight());
                                Thread.sleep(ANIMATION_DELAY);
                            }
                            setBounds(getX(), CyderDragLabel.DEFAULT_HEIGHT - 1, getWidth(), getHeight());
                            break;
                        case TOP_RIGHT:
                            for (int i = getX(); i > parent.getWidth() - getTrueWidth() + 5; i -= ANIMATION_INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(i, getY(), getTrueWidth(), getHeight());
                                Thread.sleep(ANIMATION_DELAY);
                            }
                            setBounds(parent.getWidth() - getTrueWidth() + 5, getY(), getTrueWidth(), getHeight());
                            break;
                        case TOP_LEFT:
                            for (int i = getX(); i < 5; i += ANIMATION_INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(i, getY(), getWidth(), getHeight());
                                Thread.sleep(ANIMATION_DELAY);
                            }
                            setBounds(2, getY(), getWidth(), getHeight());
                            break;
                        case LEFT:
                            for (int i = getX() ; i < 5 ; i+= ANIMATION_INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(i, getY(), getWidth(), getHeight());
                                Thread.sleep(ANIMATION_DELAY);
                            }
                            setBounds(2, parent.getHeight() / 2 - getHeight() / 2, getWidth(), getHeight());
                            break;
                        case RIGHT:
                            for (int i = getX(); i > parent.getWidth() - getWidth() + 5; i -= ANIMATION_INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(i, getY(), getWidth(), getHeight());
                                Thread.sleep(ANIMATION_DELAY);
                            }
                            setBounds(parent.getWidth() - getWidth() + 5,
                                    parent.getHeight() / 2 - getHeight() / 2, getWidth(), getHeight());
                            break;
                        case BOTTOM:
                            for (int i = getY(); i > parent.getHeight() - getHeight() + 5; i -= ANIMATION_INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(getX(), i, getWidth(), getHeight());
                                Thread.sleep(ANIMATION_DELAY);
                            }
                            setBounds(getX(), parent.getHeight() - getHeight() + 10, getWidth(), getHeight());
                            break;
                        case BOTTOM_LEFT:
                            for (int i = getX(); i < 5; i += ANIMATION_INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(i, getY(), getWidth(), getHeight());
                                Thread.sleep(ANIMATION_DELAY);
                            }
                            setBounds(2, parent.getHeight() - getHeight() + 10, getWidth(), getHeight());
                            break;
                        case BOTTOM_RIGHT:
                            for (int i = getX(); i > parent.getWidth() - getWidth() + 5; i -= ANIMATION_INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(i, getY(), getWidth(), getHeight());
                                Thread.sleep(ANIMATION_DELAY);
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

                if (builder.getNotificationType() == NotificationType.TOAST) {
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
                            for (int i = getY() ; i > - getHeight() ; i -= ANIMATION_INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(getX(), i, getWidth(), getHeight());
                                Thread.sleep(ANIMATION_DELAY);
                            }
                            break;
                        case BOTTOM:
                            for (int i = getY() ; i < parent.getHeight() - 5 ; i += ANIMATION_INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(getX(), i, getWidth(), getHeight());
                                Thread.sleep(ANIMATION_DELAY);
                            }
                            break;
                        case TOP_LEFT:
                        case LEFT:
                        case BOTTOM_LEFT:
                            for (int i = getX() ; i > -getWidth() + 5 ; i -= ANIMATION_INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(i, getY(), getWidth(), getHeight());
                                Thread.sleep(ANIMATION_DELAY);
                            }
                            break;
                        case RIGHT:
                        case BOTTOM_RIGHT:
                        case TOP_RIGHT:
                            for (int i = getX() ; i < parent.getWidth() - 5 ; i += ANIMATION_INCREMENT) {
                                if (killed)
                                    break;

                                setBounds(i, getY(), getWidth(), getHeight());
                                Thread.sleep(ANIMATION_DELAY);
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
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CyderNotification that = (CyderNotification) o;

        return arrowLen == that.arrowLen
                && killed == that.killed
                && opacity == that.opacity
                && Objects.equal(builder, that.builder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(arrowLen, killed, opacity, builder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }
}