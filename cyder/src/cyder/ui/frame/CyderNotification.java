package cyder.ui.frame;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.enums.Direction;
import cyder.enums.NotificationDirection;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.drag.CyderDragLabel;
import cyder.user.UserUtil;
import cyder.utils.ReflectionUtil;
import cyder.utils.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * A custom notification component used for CyderFrames.
 */
public class CyderNotification extends JLabel {
    /**
     * The length of the notification arrow above the border.
     */
    public static final int DEFAULT_ARROW_LEN = 8;

    /**
     * The length of the border around the notification
     */
    public static final int DEFAULT_BORDER_LEN = 5;

    /**
     * The arrow length of this notification.
     * This supports changing the arrow length in the future if needed.
     */
    private final int arrowLen = DEFAULT_ARROW_LEN;

    /**
     * The border length of this notification.
     * This supports changing the arrow length in the future if needed.
     */
    private final int borderLen = DEFAULT_BORDER_LEN;

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
    private final CyderFrame.NotificationBuilder builder;

    /**
     * Whether the notification is currently being hovered over by the user's mouse.
     */
    private boolean isHovered;

    /**
     * Constructs a new CyderNotification.
     *
     * @param builder the notification builder to construct the notification
     *                when it is pulled from the notification queue for
     *                the frame it was notified from.
     */
    public CyderNotification(CyderFrame.NotificationBuilder builder) {
        Preconditions.checkNotNull(builder);
        this.builder = builder;

        addMouseListener(UiUtil.generateCommonUiLogMouseAdapter());

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
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
    public CyderFrame.NotificationBuilder getBuilder() {
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

        // this is the width x height of what we will be surrounding
        int componentWidth = builder.getContainer().getWidth();
        int componentHeight = builder.getContainer().getHeight();

        // artificially inflate the width and height to draw the border
        componentHeight += (borderLen * 2);
        componentWidth += (borderLen * 2);

        // obtain painting object
        Graphics2D graphics2D = (Graphics2D) g;

        // containers are set to invisible if the opacity is less than half
        builder.getContainer().setVisible(builder.getNotificationType() != NotificationType.TOAST || opacity >= 128);

        // some fancy rendering or whatever
        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHints(qualityHints);

        // draw the bigger shape to hold the smaller one
        Color borderColor = CyderColors.notificationBorderColor;

        if (isHovered) {
            borderColor = borderColor.darker();
        }

        graphics2D.setPaint(new Color(borderColor.getRed(), borderColor.getGreen(),
                borderColor.getBlue(), opacity));

        GeneralPath outlinePath = new GeneralPath();

        int curveInc = 2;

        // already at 0,0
        // move out of way to draw since arrow might be left or right
        int x = 0;
        int y = 0;

        if (builder.getArrowDir() == Direction.LEFT) {
            x = arrowLen;
        }

        if (builder.getArrowDir() == Direction.TOP) {
            y = arrowLen;
        }

        // always 4 more down due to curve up 2 and then another 2
        y += 2 * curveInc;

        outlinePath.moveTo(x, y);

        // curve up 2 and right 2, twice
        outlinePath.curveTo(x, y, x + 2, y - 2, x + 4, y - 4);

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
        //noinspection UnusedAssignment
        y -= componentHeight;

        // close and fill
        outlinePath.closePath();
        graphics2D.fill(outlinePath);

        // draw the border arrow if not a toast
        if (builder.getNotificationType() != NotificationType.TOAST) {
            int len = arrowLen;

            int halfCompWidth = componentWidth / 2;
            int halfCompHeight = componentHeight / 2;

            switch (builder.getArrowDir()) {
                case TOP -> {
                    // top so we know that the x needs to be offset by 4 and the height by arrow len
                    outlinePath.moveTo(2 * 2 + halfCompWidth - len, len);
                    outlinePath.lineTo(2 * 2 + halfCompWidth, 0);
                    outlinePath.lineTo(2 * 2 + (halfCompWidth) + len, len);
                    outlinePath.lineTo(2 * 2 + halfCompWidth - len, len);
                }
                case LEFT -> {
                    // left so we know that the x needs to be offset by arrow len and the height by 4
                    outlinePath.moveTo(len, 2 * 2 + halfCompHeight - len);
                    outlinePath.lineTo(0, 2 * 2 + halfCompHeight);
                    outlinePath.lineTo(len, 2 * 2 + halfCompHeight + len);
                    outlinePath.moveTo(len, 2 * 2 + halfCompHeight - len);
                }
                case RIGHT -> {
                    // right so we know that the x needs to be offset by 4 * 2 + componentWidth
                    // and the height by 2 * 2 + componentHeight / 2 - len
                    outlinePath.moveTo(2 * 2 * 2 + componentWidth, 2 * 2 + halfCompHeight - len);
                    outlinePath.lineTo(2 * 2 * 2 + componentWidth + len, 2 * 2 + halfCompHeight);
                    outlinePath.lineTo(2 * 2 * 2 + componentWidth, 2 * 2 + halfCompHeight + len);
                    outlinePath.moveTo(2 * 2 * 2 + componentWidth, 2 * 2 + halfCompHeight - len);
                }
                case BOTTOM -> {
                    // bottom so x axis is middle but y is all the way down
                    outlinePath.moveTo(2 * 2 + halfCompWidth - len, 2 * 2 * 2 + componentHeight);
                    outlinePath.lineTo(2 * 2 + halfCompWidth, 2 * 2 * 2 + componentHeight + len);
                    outlinePath.lineTo(2 * 2 + halfCompWidth + len, 2 * 2 * 2 + componentHeight);
                    outlinePath.lineTo(2 * 2 + halfCompWidth - len, 2 * 2 * 2 + componentHeight);
                }
            }
        }

        // close and fill
        outlinePath.closePath();
        graphics2D.fill(outlinePath);

        // reset component width and height to original
        componentHeight -= (borderLen * 2);
        componentWidth -= (borderLen * 2);

        /*
        There's some duplicate code here you could possibly clean up but it's easier to read this way

        Now draw the inner shape, accounting for the border len offset
         */

        Color fillColor = CyderColors.notificationBackgroundColor;

        if (isHovered) {
            fillColor = fillColor.darker();
        }

        graphics2D.setPaint(new Color(fillColor.getRed(), fillColor.getGreen(),
                fillColor.getBlue(), opacity));

        GeneralPath fillPath = new GeneralPath();

        // already at 0,0 but need to be reset
        // move out of way to draw since arrow might be left or right
        x = 0;
        y = 0;

        if (builder.getArrowDir() == Direction.LEFT) {
            x = arrowLen;
        }

        if (builder.getArrowDir() == Direction.TOP) {
            y = arrowLen;
        }

        // always 4 more down due to curve up 2 and then another 2
        y += 2 * curveInc;

        // offset inward for fill shape
        x += borderLen;
        y += borderLen;

        fillPath.moveTo(x, y);

        // curve up 2 and right 2, twice
        fillPath.curveTo(x, y, x + 2, y - 2, x + 4, y - 4);

        // new x,y we're at
        x += 4;
        y -= 4;

        // line right for component width
        fillPath.lineTo(x + componentWidth, y);

        // new x
        x += componentWidth;

        // curve down 2 and right 2, twice
        fillPath.curveTo(x, y, x + 2, y + 2, x + 4, y + 4);

        // new x,y we're at
        x += 4;
        y += 4;

        // line down for component height
        fillPath.lineTo(x, y + componentHeight);

        // new y
        y += componentHeight;

        // curve down 2 and left 2, twice
        fillPath.curveTo(x, y, x - 2, y + 2, x - 4, y + 4);

        // new x,y we're at
        x -= 4;
        y += 4;

        // line left for component width
        fillPath.lineTo(x - componentWidth, y);

        // new x
        x -= componentWidth;

        // curve up 2 and left 2, twice
        fillPath.curveTo(x, y, x - 2, y - 2, x - 4, y - 4);

        // new x,y we're at
        x -= 4;
        y -= 4;

        // line up for component height
        fillPath.lineTo(x, y - componentHeight);

        // new y
        //noinspection UnusedAssignment
        y -= componentHeight;

        // close and fill
        fillPath.closePath();
        graphics2D.fill(fillPath);

        // draw the border arrow if not a toast
        if (builder.getNotificationType() != NotificationType.TOAST) {
            int len = arrowLen;
            int halfCompWidth = componentWidth / 2;
            int halfCompHeight = componentHeight / 2;

            switch (builder.getArrowDir()) {
                case TOP -> {
                    // top so we know that the x needs to be offset
                    // by 2 * 2 + border and the height by border + arrow len
                    fillPath.moveTo(2 * 2 + borderLen + halfCompWidth - len, len + borderLen);
                    fillPath.lineTo(2 * 2 + borderLen + halfCompWidth, borderLen);
                    fillPath.lineTo(2 * 2 + borderLen + (halfCompWidth) + len, len + borderLen);
                    fillPath.lineTo(2 * 2 + borderLen - len, len + borderLen);
                }
                case LEFT -> {
                    // left so we know that the x needs to be offset
                    // by arrow len + border and the height by 2 * 2 + border
                    fillPath.moveTo(len + borderLen, 2 * 2 + borderLen + halfCompHeight - len);
                    fillPath.lineTo(borderLen, 2 * 2 + borderLen + halfCompHeight);
                    fillPath.lineTo(len + borderLen, 2 * 2 + borderLen + halfCompHeight + len);
                    fillPath.moveTo(len + borderLen, 2 * 2 + borderLen + halfCompHeight - len);
                }
                case RIGHT -> {
                    // right so we know that the x needs to be offset by 2 * 2 * 2 + componentWidth + borderLen
                    // and the height by 2 * 2 + componentHeight / 2 - len + borderLen
                    fillPath.moveTo(2 * 2 * 2 + borderLen + componentWidth,
                            2 * 2 + halfCompHeight - len + borderLen);
                    fillPath.lineTo(2 * 2 * 2 + borderLen + componentWidth + len,
                            2 * 2 + halfCompHeight + borderLen);
                    fillPath.lineTo(2 * 2 * 2 + borderLen + componentWidth,
                            2 * 2 + halfCompHeight + len + borderLen);
                    fillPath.moveTo(2 * 2 * 2 + borderLen + componentWidth,
                            2 * 2 + halfCompHeight - len + borderLen);
                }
                case BOTTOM -> {
                    // bottom so we know that the x needs to be offset by 2 * 2 + width / 2 + border len
                    // and y needs to be offset 2 * 2 * 2 + height + border len
                    fillPath.moveTo(2 * 2 + halfCompWidth - len + borderLen,
                            2 * 2 * 2 + componentHeight + borderLen);
                    fillPath.lineTo(2 * 2 + halfCompWidth + borderLen,
                            2 * 2 * 2 + componentHeight + len + borderLen);
                    fillPath.lineTo(2 * 2 + halfCompWidth + len + borderLen,
                            2 * 2 * 2 + componentHeight + borderLen);
                    fillPath.lineTo(2 * 2 + halfCompWidth - len + borderLen,
                            2 * 2 * 2 + componentHeight + borderLen);
                }
            }
        }

        // close and fill
        fillPath.closePath();
        graphics2D.fill(fillPath);

        /*
        Done with custom component drawing
         */

        // label is offset by border plus the arrow if applicable and the curvature
        int labelOffX = (builder.getArrowDir() == Direction.LEFT ? arrowLen : 0) + borderLen + 2 * 2;
        int labelOffY = (builder.getArrowDir() == Direction.TOP ? arrowLen : 0) + borderLen + 2 * 2;

        builder.getContainer().setBounds(labelOffX, labelOffY, componentWidth, componentHeight);

        boolean in = false;

        for (Component c : getComponents()) {
            if (c.equals(builder.getContainer())) {
                in = true;
                break;
            }
        }

        if (!in) {
            add(builder.getContainer());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        // border, container, curvature
        int ret = 2 * borderLen + builder.getContainer().getWidth() + 2 * 2 * 2;

        if (builder.getArrowDir() == Direction.LEFT || builder.getArrowDir() == Direction.RIGHT) {
            ret += 2 * arrowLen;
        }

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        // border, container, curvature
        int ret = 2 * borderLen + builder.getContainer().getHeight() + 2 * 2 * 2;

        if (builder.getArrowDir() == Direction.TOP || builder.getArrowDir() == Direction.BOTTOM) {
            ret += 2 * arrowLen;
        }

        return ret;
    }

    /**
     * Animates in the notification on the parent container.
     * The components position is expected to have already
     * been set out of bounds on the parent.
     *
     * @param notificationDirection the direction for the notification to enter and exit from
     */
    public void appear(NotificationDirection notificationDirection, Component parent, int delay) {
        CyderThreadRunner.submit(() -> {
            try {
                if (builder.getNotificationType() == NotificationType.TOAST) {
                    // centered on x, y has offset of 10 pixels from bottom
                    setBounds(parent.getWidth() / 2 - getWidth() / 2,
                            parent.getHeight() - getHeight() - 10, getWidth(), getHeight());

                    opacity = 0;
                    setVisible(true);

                    for (int i = 0 ; i < 256 ; i += 2) {
                        if (UserUtil.getCyderUser().getDoAnimations().equals("0")) {
                            break;
                        }

                        opacity = i;
                        repaint();
                        ThreadUtil.sleep(2);
                    }

                    opacity = 255;
                    repaint();
                } else {
                    int bottomOffset = 5;

                    switch (notificationDirection) {
                        case TOP -> {
                            setBounds(parent.getWidth() / 2 - getWidth() / 2,
                                    CyderDragLabel.DEFAULT_HEIGHT - getHeight(), getWidth(), getHeight());
                            setVisible(true);
                            for (int i = getY() ; i < CyderDragLabel.DEFAULT_HEIGHT ; i += ANIMATION_INCREMENT) {
                                if (killed || UserUtil.getCyderUser().getDoAnimations().equals("0")) {
                                    break;
                                }

                                setLocation(getX(), i);
                                ThreadUtil.sleep(ANIMATION_DELAY);
                            }
                            setLocation(getX(), CyderDragLabel.DEFAULT_HEIGHT - 1);
                        }
                        case TOP_RIGHT -> {
                            setBounds(parent.getWidth() + getWidth(),
                                    CyderDragLabel.DEFAULT_HEIGHT, getWidth(), getHeight());
                            setVisible(true);
                            for (int i = getX() ; i > parent.getWidth() - getWidth() + 5 ; i -= ANIMATION_INCREMENT) {
                                if (killed || UserUtil.getCyderUser().getDoAnimations().equals("0")) {
                                    break;
                                }

                                setLocation(i, getY());
                                ThreadUtil.sleep(ANIMATION_DELAY);
                            }
                            setLocation(parent.getWidth() - getWidth() + 5, getY());
                        }
                        case TOP_LEFT -> {
                            setBounds(-getWidth(), CyderDragLabel.DEFAULT_HEIGHT, getWidth(), getHeight());
                            setVisible(true);
                            for (int i = getX() ; i < 5 ; i += ANIMATION_INCREMENT) {
                                if (killed || UserUtil.getCyderUser().getDoAnimations().equals("0")) {
                                    break;
                                }

                                setLocation(i, getY());
                                ThreadUtil.sleep(ANIMATION_DELAY);
                            }
                            setLocation(2, getY());
                        }
                        case LEFT -> {
                            // note drag label used here to center on content pane
                            setBounds(-getWidth(), CyderDragLabel.DEFAULT_HEIGHT
                                    + parent.getHeight() / 2 - getHeight() / 2, getWidth(), getHeight());
                            setVisible(true);
                            for (int i = getX() ; i < 5 ; i += ANIMATION_INCREMENT) {
                                if (killed || UserUtil.getCyderUser().getDoAnimations().equals("0")) {
                                    break;
                                }

                                setLocation(i, getY());
                                ThreadUtil.sleep(ANIMATION_DELAY);
                            }
                            setLocation(2, CyderDragLabel.DEFAULT_HEIGHT
                                    + parent.getHeight() / 2 - getHeight() / 2);
                        }
                        case RIGHT -> {
                            // note drag label used here to center on content pane
                            setBounds(parent.getWidth() + getWidth(), CyderDragLabel.DEFAULT_HEIGHT
                                    + parent.getHeight() / 2 - getHeight() / 2, getWidth(), getHeight());
                            setVisible(true);
                            for (int i = getX() ; i > parent.getWidth() - getWidth() + 5 ; i -= ANIMATION_INCREMENT) {
                                if (killed || UserUtil.getCyderUser().getDoAnimations().equals("0")) {
                                    break;
                                }

                                setLocation(i, getY());
                                ThreadUtil.sleep(ANIMATION_DELAY);
                            }
                            setLocation(parent.getWidth() - getWidth() + 5,
                                    CyderDragLabel.DEFAULT_HEIGHT + parent.getHeight() / 2 - getHeight() / 2);
                        }
                        case BOTTOM -> {
                            setBounds(parent.getWidth() / 2 - getWidth() / 2, parent.getHeight()
                                    + getHeight(), getWidth(), getHeight());
                            setVisible(true);
                            for (int i = getY() ; i > parent.getHeight() - getHeight() + 5 ; i -= ANIMATION_INCREMENT) {
                                if (killed || UserUtil.getCyderUser().getDoAnimations().equals("0")) {
                                    break;
                                }

                                setLocation(getX(), i);
                                ThreadUtil.sleep(ANIMATION_DELAY);
                            }
                            setBounds(parent.getWidth() / 2 - getWidth() / 2,
                                    parent.getHeight() - getHeight() + arrowLen, getWidth(), getHeight());
                        }
                        case BOTTOM_LEFT -> {
                            setBounds(-getWidth(), parent.getHeight() - getHeight()
                                    - bottomOffset, getWidth(), getHeight());
                            setVisible(true);
                            for (int i = getX() ; i < 5 ; i += ANIMATION_INCREMENT) {
                                if (killed || UserUtil.getCyderUser().getDoAnimations().equals("0")) {
                                    break;
                                }

                                setLocation(i, getY());
                                ThreadUtil.sleep(ANIMATION_DELAY);
                            }
                            setLocation(2, parent.getHeight() - getHeight() - bottomOffset);
                        }
                        case BOTTOM_RIGHT -> {
                            setBounds(parent.getWidth() + getWidth(), parent.getHeight()
                                    - getHeight() - bottomOffset, getWidth(), getHeight());
                            setVisible(true);
                            for (int i = getX() ; i > parent.getWidth() - getWidth() + 5 ; i -= ANIMATION_INCREMENT) {
                                if (killed || UserUtil.getCyderUser().getDoAnimations().equals("0")) {
                                    break;
                                }

                                setLocation(i, getY());
                                ThreadUtil.sleep(ANIMATION_DELAY);
                            }
                            setLocation(parent.getWidth() - getWidth() + 5,
                                    parent.getHeight() - getHeight() - bottomOffset);
                        }
                        default -> throw new IllegalStateException(
                                "Illegal Notification Direction: " + notificationDirection);
                    }
                }

                // call vanish now visible and not set to stay until dismissed
                if (UserUtil.getCyderUser().getPersistentnotifications().equals("0") && delay != -1) {
                    vanish(notificationDirection, parent, delay);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Notification Appear Animator");
    }

    /**
     * Kill the notification by stopping all animation threads
     * and setting this visibility to false.
     * <p>
     * Note: you should not make a killed notification
     * visible again via {@link Component#setVisible(boolean)}.
     */
    public void kill() {
        Container parent = getParent();

        if (parent != null) {
            parent.remove(this);
            parent.repaint();
        }

        setVisible(false);
        killed = true;
    }

    /**
     * Returns whether this notification has been killed.
     *
     * @return whether this notification has been killed
     */
    public boolean isKilled() {
        return killed;
    }

    /**
     * This method to be used in combination with an already visible
     * notification to immediately move it off of the parent until it is not visible.
     * Upon completing the animation, the notification is removed from the parent.
     *
     * @param notificationDirection the direction to exit to
     * @param parent                the component the notification is on. Used for bounds calculations
     * @param delay                 the delay before vanish
     */
    protected void vanish(NotificationDirection notificationDirection, Component parent, int delay) {
        CyderThreadRunner.submit(() -> {
            try {
                // delay before vanishing
                ThreadUtil.sleep(delay);

                if (builder.getNotificationType() == NotificationType.TOAST) {
                    for (int i = 255 ; i >= 0 ; i -= 2) {
                        if (killed || UserUtil.getCyderUser().getDoAnimations().equals("0")) {
                            break;
                        }

                        opacity = i;
                        repaint();
                        ThreadUtil.sleep(2);
                    }

                    Container parentComponent = getParent();

                    if (parentComponent != null) {
                        parentComponent.remove(this);
                        setVisible(false);
                        parentComponent.repaint();
                    }
                } else {
                    switch (notificationDirection) {
                        case TOP:
                            for (int i = getY() ; i > -getHeight() ; i -= ANIMATION_INCREMENT) {
                                if (killed || UserUtil.getCyderUser().getDoAnimations().equals("0")) {
                                    break;
                                }

                                setBounds(getX(), i, getWidth(), getHeight());
                                ThreadUtil.sleep(ANIMATION_DELAY);
                            }
                            break;
                        case BOTTOM:
                            for (int i = getY() ; i < parent.getHeight() - 5 ; i += ANIMATION_INCREMENT) {
                                if (killed || UserUtil.getCyderUser().getDoAnimations().equals("0")) {
                                    break;
                                }

                                setBounds(getX(), i, getWidth(), getHeight());
                                ThreadUtil.sleep(ANIMATION_DELAY);
                            }
                            break;
                        case TOP_LEFT:
                        case LEFT:
                        case BOTTOM_LEFT:
                            for (int i = getX() ; i > -getWidth() + 5 ; i -= ANIMATION_INCREMENT) {
                                if (killed || UserUtil.getCyderUser().getDoAnimations().equals("0")) {
                                    break;
                                }

                                setBounds(i, getY(), getWidth(), getHeight());
                                ThreadUtil.sleep(ANIMATION_DELAY);
                            }
                            break;
                        case RIGHT:
                        case BOTTOM_RIGHT:
                        case TOP_RIGHT:
                            for (int i = getX() ; i < parent.getWidth() - 5 ; i += ANIMATION_INCREMENT) {
                                if (killed || UserUtil.getCyderUser().getDoAnimations().equals("0")) {
                                    break;
                                }

                                setBounds(i, getY(), getWidth(), getHeight());
                                ThreadUtil.sleep(ANIMATION_DELAY);
                            }
                            break;
                    }
                }

                setVisible(false);
                repaint();
                kill();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Notification Vanish Animator");
    }

    // -------------------------------------------------------
    // Primary methods to override according to Effective Java
    // -------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("ConstantConditions")  // might not be always true in future
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
        return ReflectionUtil.commonCyderUiToString(this);
    }

    /**
     * Returns whether the notification is currently drawn as being hovered.
     *
     * @return whether the notification is currently drawn as being hovered
     */
    public boolean isHovered() {
        return isHovered;
    }

    /**
     * Sets whether the notification is currently drawn as being hovered.
     *
     * @param hovered whether the notification is currently drawn as being hovered
     */
    public void setHovered(boolean hovered) {
        isHovered = hovered;
    }

    /**
     * The possible notification types.
     */
    public enum NotificationType {
        /**
         * A common notification with an arrow on any cardinal side.
         */
        NOTIFICATION,
        /**
         * A toast emulating Android's toast.
         */
        TOAST
    }
}