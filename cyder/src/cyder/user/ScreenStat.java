package cyder.user;

import cyder.enums.Direction;
import cyder.handlers.internal.Logger;
import cyder.utils.ReflectionUtil;

/**
 * A class to store statistics about the Console and where it is.
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
        this.consoleX = consoleX;
        this.consoleY = consoleY;
        this.consoleWidth = consoleWidth;
        this.consoleHeight = consoleHeight;
        this.monitor = monitor;
        this.consoleOnTop = consoleOnTop;
        this.consoleDirection = consoleDirection;

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Constructs a default invalid screen stat object.
     */
    public ScreenStat() {
        this(-Integer.MAX_VALUE, -Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
                Integer.MAX_VALUE, false, Direction.TOP);
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
        this.consoleDirection = consoleDirection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
