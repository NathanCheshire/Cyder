package cyder.ui;

import cyder.constants.CyderColors;
import cyder.enums.Direction;
import cyder.enums.NotificationDirection;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.SessionHandler;
import cyder.utilities.ReflectionUtil;
import cyder.utilities.UserUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;

public class Notification extends JLabel {

    private int arrowSize = 6;

    private int width = 300;
    private int height = 300;

    private Direction ArrowType = Direction.TOP;

    private boolean killed;
    private static int delay = 10;
    private static int increment = 8;

    private Color backgroundColor = CyderColors.notificationBackgroundColor;

    public Notification() {
        killed = false;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SessionHandler.log(SessionHandler.Tag.ACTION, e.getComponent());
            }
        });
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public static int getIncrement() {
        return increment;
    }

    public static int getDelay() {
        return delay;
    }

    public static int getTextXOffset() {
        return 14; //offset from 0,0
    }

    public static int getTextYOffset() {
        return 16; //offset from 0,0
    }

    public void setArrowSize(int arrowSize) {
        this.arrowSize = arrowSize;
    }

    public int getArrowSize() {
        return this.arrowSize;
    }

    public void setWidth(int w) {
        this.width = w;
    }

    public void setHeight(int h) {
        this.height = h;
    }

    public void setArrow(Direction type) {
        this.ArrowType = type;
    }

    public Direction getArrow() {
        return this.ArrowType;
    }

    /**
     * Custom width getter since this is a custom paint component.
     * @return the width plus the x-offset of 14 twice for both sides with the arrow size
     * added in if the arrow is on the left or right.
     */
    @Override
    public int getWidth() {
        return this.width + getTextXOffset() * 2 + ((ArrowType == Direction.LEFT || ArrowType == Direction.RIGHT) ? arrowSize : 0);
    }

    /**
     * Custom height getter since this is a custom paint component.
     * @return the height plus the y-offset of 16 twice for both sides with the arrow size
     * added in if the arrow is on the top or bottom.
     */
    @Override
    public int getHeight() {
        return this.height + getTextYOffset() * 2 + ((ArrowType == Direction.BOTTOM || ArrowType == Direction.TOP) ? arrowSize : 0);
    }

    @Override
    public void setToolTipText(String text) {
        super.setToolTipText(text);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D graphics2D = (Graphics2D) g;

        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHints(qualityHints);


        graphics2D.setPaint(CyderColors.notificationBorderColor);

        GeneralPath outlinePath = new GeneralPath();

        outlinePath.moveTo(8, 8 + 2);

        outlinePath.curveTo(8, 8 + 2,10,6 + 2, 12, 4 + 2);
        outlinePath.lineTo(this.width + 14 + 2, 4 + 2);

        outlinePath.curveTo(this.width + 14 + 2, 4 + 2, this.width + 16 + 2, 6 + 2, this.width + 18 + 2, 8 + 2);
        outlinePath.lineTo(this.width + 18 + 2, this.height + 10 + 2 + 2);

        outlinePath.curveTo(this.width + 18 + 2, this.height + 10 + 2 + 2, this.width + 16 + 2, this.height + 12 + 2  + 2, this.width + 14 + 2, this.height + 14 + 2  + 2);
        outlinePath.lineTo(12, this.height + 14 + 2 + 2);

        outlinePath.curveTo(12, this.height + 14 + 2 + 2, 10, this.height + 12 + 2 + 2, 8, this.height + 10 + 2 + 2);
        outlinePath.lineTo( 8, 8 + 2);

        switch (ArrowType) {
            case TOP:
                outlinePath.moveTo(6 + this.width / 2, 6 + 2);
                outlinePath.lineTo(14 + this.width / 2, -2 + 2);
                outlinePath.lineTo(22 + this.width / 2, 6 + 2);
                outlinePath.lineTo(6 + this.width / 2, 6 + 2);
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
                outlinePath.moveTo(18 + this.width, 2 + height / 2 + 2);
                outlinePath.lineTo(26 + this.width, 10 + height / 2 + 2);
                outlinePath.lineTo(18 + this.width, 18 + height / 2 + 2);
                outlinePath.lineTo(18 + this.width, 2 + height / 2 + 2);
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

        graphics2D.setPaint(this.backgroundColor);

        GeneralPath fillPath = new GeneralPath();

        fillPath.moveTo(10, 10 + 2);

        fillPath.curveTo(10, 10 + 2,12,8 + 2, 14, 6 + 2);
        fillPath.lineTo(this.width + 14, 6 + 2);

        fillPath.curveTo(this.width + 14, 6 + 2, this.width + 16, 8 + 2, this.width + 18, 10 + 2);
        fillPath.lineTo(this.width + 18, this.height + 10 + 2);

        fillPath.curveTo(this.width + 18, this.height + 10 + 2, this.width + 16, this.height + 12 + 2, this.width + 14, this.height + 14 + 2);
        fillPath.lineTo(14, this.height + 14 + 2);

        fillPath.curveTo(14, this.height + 14 + 2, 12, this.height + 12 + 2, 10, this.height + 10 + 2);
        fillPath.lineTo( 10, 10 + 2);

        fillPath.closePath();
        graphics2D.fill(fillPath);

        switch (ArrowType) {
            case TOP:
                fillPath.moveTo(8 + this.width / 2, 6 + 2);
                fillPath.lineTo(14 + this.width / 2, 2);
                fillPath.lineTo(20 + this.width / 2, 6 + 2);
                fillPath.lineTo(8 + this.width / 2, 6 + 2);
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
                fillPath.moveTo(18 + this.width, 4 + height / 2 + 2);
                fillPath.lineTo(24 + this.width, 10 + height / 2 + 2);
                fillPath.lineTo(18 + this.width, 16 + height / 2 + 2);
                fillPath.lineTo(18 + this.width, 4 + height / 2 + 2);
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
     * This method to be used with an already initialized component. Expected that the component's starting
     * location is already set.
     * @param notificationDirection the direction for the notification to enter and exit from.
     */
    public void appear(NotificationDirection notificationDirection, Component parent, int delay) {
        new Thread(() -> {
            try {
                setVisible(true);
                switch (notificationDirection) {
                    case TOP:
                        for (int i = getY(); i < DragLabel.getDefaultHeight(); i += this.increment) {
                            if (killed)
                                break;

                            setBounds(getX(), i, getWidth(), getHeight());
                            Thread.sleep(this.delay);
                        }
                        setBounds(getX(), DragLabel.getDefaultHeight() - 1, getWidth(), getHeight());
                        break;
                    case TOP_RIGHT:
                        for (int i = getX(); i > parent.getWidth() - this.getWidth() + 5; i -= this.increment) {
                            if (killed)
                                break;

                            setBounds(i, getY(), getWidth(), getHeight());
                            Thread.sleep(this.delay);
                        }
                        setBounds(parent.getWidth() - this.getWidth() + 5, getY(), getWidth(), getHeight());
                        break;
                    case TOP_LEFT:
                        for (int i = getX(); i < 5; i += this.increment) {
                            if (killed)
                                break;

                            setBounds(i, getY(), getWidth(), getHeight());
                            Thread.sleep(this.delay);
                        }
                        setBounds(2, getY(), getWidth(), getHeight());
                        break;
                    case CENTER_LEFT:
                        for (int i = getX() ; i < 5 ; i+= this.increment) {
                            if (killed)
                                break;

                            setBounds(i, getY(), getWidth(), getHeight());
                            Thread.sleep(this.delay);
                        }
                        setBounds(2, parent.getHeight() / 2 - this.getHeight() / 2, getWidth(), getHeight());
                        break;
                    case CENTER_RIGHT:
                        for (int i = getX(); i > parent.getWidth() - this.getWidth() + 5; i -= this.increment) {
                            if (killed)
                                break;

                            setBounds(i, getY(), getWidth(), getHeight());
                            Thread.sleep(this.delay);
                        }
                        setBounds(parent.getWidth() - this.getWidth() + 5, parent.getHeight() / 2 - this.getHeight() / 2, getWidth(), getHeight());
                        break;
                    case BOTTOM:
                        for (int i = getY(); i > parent.getHeight() - this.getHeight() + 5; i -= this.increment) {
                            if (killed)
                                break;

                            setBounds(getX(), i, getWidth(), getHeight());
                            Thread.sleep(this.delay);
                        }
                        setBounds(getX(), parent.getHeight() - this.getHeight() + 10, getWidth(), getHeight());
                        break;
                    case BOTTOM_LEFT:
                        for (int i = getX(); i < 5; i += this.increment) {
                            if (killed)
                                break;

                            setBounds(i, getY(), getWidth(), getHeight());
                            Thread.sleep(this.delay);
                        }
                        setBounds(2, parent.getHeight() - this.getHeight() + 10, getWidth(), getHeight());
                        break;
                    case BOTTOM_RIGHT:
                        for (int i = getX(); i > parent.getWidth() - this.getWidth() + 5; i -= this.increment) {
                            if (killed)
                                break;

                            setBounds(i, getY(), getWidth(), getHeight());
                            Thread.sleep(this.delay);
                        }
                        setBounds(parent.getWidth() - this.getWidth() + 5, parent.getHeight() - this.getHeight() + 10, getWidth(), getHeight());
                        break;
                }

                //now that it's visible, call vanish with the proper delay if enabled
                if (UserUtil.getUserData("persistentnotifications").equals("0") && delay != -1)
                    this.vanish(notificationDirection, parent, delay);
            }

            catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        },"notification appear animation").start();
    }

    /**
     * Kill any notification by stopping all animation threads and setting this visibility to false.
     * Must re-initialize notification object using constructor; you shouldn't make a killed notification
     * visible again via {@link Component#setVisible(boolean)}.
     */
    public void kill() {
        killed = true;
        this.setVisible(false);
    }

    /**
     * This method to be used in combination with an already visible notification to immediately move it off of the
     * parent until it is not visible. Upon completing the animation, the notification is removed from the parent.
     * @param notificationDirection the direction to exit to.
     * @param parent the component the notification is on. Used for bounds calculations.
     * @param delay the delay before vanish.
     */
    protected void vanish(NotificationDirection notificationDirection, Component parent, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);

                switch(notificationDirection) {
                    case TOP:
                        for (int i = getY() ; i > - getHeight() ; i -= this.increment) {
                            if (killed)
                                break;

                            setBounds(getX(), i, getWidth(), getHeight());
                            Thread.sleep(this.delay);
                        }
                        break;
                    case BOTTOM:
                        for (int i = getY() ; i < parent.getHeight() - 5 ; i += this.increment) {
                            if (killed)
                                break;

                            setBounds(getX(), i, getWidth(), getHeight());
                            Thread.sleep(this.delay);
                        }
                        break;
                    case TOP_LEFT:
                    case CENTER_LEFT:
                    case BOTTOM_LEFT:
                        for (int i = getX() ; i > -getWidth() + 5 ; i -= this.increment) {
                            if (killed)
                                break;

                            setBounds(i, getY(), getWidth(), getHeight());
                            Thread.sleep(this.delay);
                        }
                        break;
                    case CENTER_RIGHT:
                    case BOTTOM_RIGHT:
                    case TOP_RIGHT:
                        for (int i = getX() ; i < parent.getWidth() - 5 ; i += this.increment) {
                            if (killed)
                                break;

                            setBounds(i, getY(), getWidth(), getHeight());
                            Thread.sleep(this.delay);
                        }
                        break;
                }

                if (this.isVisible() && this != null) {
                    this.getParent().remove(this);
                    this.setVisible(false);
                }
            }

            catch (Exception e) {
               ExceptionHandler.handle(e);
            }
        },"notification vanish animater").start();
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }
}