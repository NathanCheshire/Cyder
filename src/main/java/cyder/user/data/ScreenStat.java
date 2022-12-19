package main.java.cyder.user.data;

import com.google.common.base.Preconditions;
import main.java.cyder.enums.Direction;
import main.java.cyder.logging.LogTag;
import main.java.cyder.logging.Logger;

/**
 * A class to store statistics about the Console frame and its location on the monitor.
 */
public class ScreenStat {
    /**
     * The x coordinate the console is at. This value may seem out of bounds due to
     * multiple monitors placed relative to the primary monitor.
     */
    private int consoleX;

    /**
     * The y coordinate the console is at. This value may seem out of bounds due to
     * multiple monitors placed relative to the primary monitor.
     */
    private int consoleY;

    /**
     * The width of the console.
     */
    private int consoleWidth;

    /**
     * The height of the console.
     */
    private int consoleHeight;

    /**
     * The integer id of the monitor the console is on.
     */
    private int monitor;

    /**
     * Whether the console is in always on top mode.
     */
    private boolean consoleOnTop;

    /**
     * The direction the console is currently oriented in.
     */
    private Direction consoleDirection;

    /**
     * Constructs a new ScreenStat object.
     *
     * @param consoleX         the x location of the console
     * @param consoleY         the y location of the console
     * @param consoleWidth     the width of the console
     * @param consoleHeight    the height of the console
     * @param monitor          the monitor id the console is on
     * @param consoleOnTop     whether the console is in always on top mode
     * @param consoleDirection the direction the console is oriented in
     */
    public ScreenStat(int consoleX, int consoleY, int consoleWidth,
                      int consoleHeight, int monitor,
                      boolean consoleOnTop, Direction consoleDirection) {
        Preconditions.checkNotNull(consoleDirection);

        this.consoleX = consoleX;
        this.consoleY = consoleY;
        this.consoleWidth = consoleWidth;
        this.consoleHeight = consoleHeight;
        this.monitor = monitor;
        this.consoleOnTop = consoleOnTop;
        this.consoleDirection = consoleDirection;

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * The default x and y value of a screen stat.
     */
    private static final int DEFAULT_X_Y_VALUE = Integer.MIN_VALUE;

    /**
     * The default width and height of a screen stat.
     */
    private static final int DEFAULT_WIDTH_HEIGHT = Integer.MAX_VALUE;

    /**
     * The default monitor of a screen stat.
     */
    private static final int DEFAULT_MONITOR = Integer.MAX_VALUE;

    /**
     * The default stat of the on top member of a screen stat.
     */
    private static final boolean DEFAULT_ON_TOP_VALUE = false;

    /**
     * The default direction of a scree stat.
     */
    private static final Direction DEFAULT_DIRECTION = Direction.TOP;

    /**
     * Constructs a default invalid screen stat object.
     */
    public ScreenStat() {
        this(DEFAULT_X_Y_VALUE, DEFAULT_X_Y_VALUE,
                DEFAULT_WIDTH_HEIGHT, DEFAULT_WIDTH_HEIGHT,
                DEFAULT_MONITOR, DEFAULT_ON_TOP_VALUE, DEFAULT_DIRECTION);
    }

    /**
     * Returns the x value of the console.
     *
     * @return the x value of the console
     */
    public int getConsoleX() {
        return consoleX;
    }

    /**
     * Sets the x value of the console.
     *
     * @param consoleX the x value of the console
     */
    public void setConsoleX(int consoleX) {
        this.consoleX = consoleX;
    }

    /**
     * Returns the y value of the console.
     *
     * @return the y value of the console
     */
    public int getConsoleY() {
        return consoleY;
    }

    /**
     * Sets the y value of the console.
     *
     * @param consoleY the y value of the console
     */
    public void setConsoleY(int consoleY) {
        this.consoleY = consoleY;
    }

    /**
     * Returns the width of the console.
     *
     * @return the width of the console
     */
    public int getConsoleWidth() {
        return consoleWidth;
    }

    /**
     * Sets the width of the console.
     *
     * @param consoleWidth the width of the console
     */
    public void setConsoleWidth(int consoleWidth) {
        this.consoleWidth = consoleWidth;
    }

    /**
     * Returns the height of the console.
     *
     * @return the height of the console
     */
    public int getConsoleHeight() {
        return consoleHeight;
    }

    /**
     * Sets the height of the console,
     *
     * @param consoleHeight the height of the console
     */
    public void setConsoleHeight(int consoleHeight) {
        this.consoleHeight = consoleHeight;
    }

    /**
     * Returns the integer id of the monitor the console is on.
     *
     * @return the integer id of the monitor the console is on
     */
    public int getMonitor() {
        return monitor;
    }

    /**
     * Sets the integer id of the monitor the console is on.
     *
     * @param monitor the integer id of the monitor the console is on
     */
    public void setMonitor(int monitor) {
        this.monitor = monitor;
    }

    /**
     * Returns whether the console is in always on top mode.
     *
     * @return whether the console is in always on top mode
     */
    public boolean isConsoleOnTop() {
        return consoleOnTop;
    }

    /**
     * Sets whether the console is in always on top mode.
     *
     * @param consoleOnTop whether the console is in always on top mode
     */
    public void setConsoleOnTop(boolean consoleOnTop) {
        this.consoleOnTop = consoleOnTop;
    }

    /**
     * Returns the direction the console is oriented in.
     *
     * @return the direction the console is oriented in
     */
    public Direction getConsoleDirection() {
        return consoleDirection;
    }

    /**
     * Sets the direction the console is oriented in.
     *
     * @param consoleDirection the direction the console is oriented in
     */
    public void setConsoleDirection(Direction consoleDirection) {
        this.consoleDirection = Preconditions.checkNotNull(consoleDirection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ScreenStat{"
                + "consoleX=" + consoleX
                + ", consoleY=" + consoleY
                + ", consoleWidth=" + consoleWidth
                + ", consoleHeight=" + consoleHeight
                + ", monitor=" + monitor
                + ", consoleOnTop=" + consoleOnTop
                + ", consoleDirection=" + consoleDirection
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof ScreenStat)) {
            return false;
        }

        ScreenStat other = (ScreenStat) o;

        return consoleX == other.consoleX
                && consoleY == other.consoleY
                && consoleWidth == other.consoleWidth
                && consoleHeight == other.consoleHeight
                && monitor == other.monitor
                && consoleOnTop == other.consoleOnTop
                && consoleDirection == other.consoleDirection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(consoleX);
        ret = 31 * ret + Integer.hashCode(consoleY);
        ret = 31 * ret + Integer.hashCode(consoleWidth);
        ret = 31 * ret + Integer.hashCode(consoleHeight);
        ret = 31 * ret + Integer.hashCode(monitor);
        ret = 31 * ret + Boolean.hashCode(consoleOnTop);
        ret = 31 * ret + consoleDirection.hashCode();
        return ret;
    }
}
