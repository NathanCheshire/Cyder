package cyder.user;

import cyder.enums.Direction;
import cyder.handlers.internal.Logger;
import cyder.utilities.ReflectionUtil;

/**
 * A class to store statistics about the ConsoleFrame and where it is.
 */
public class ScreenStat {
    /**
     * The x coordinate the console frame is at. This value may seem out of bounds due to
     * multiple monitors placed relative to the primary monitor.
     */
    private int consoleX;

    /**
     * The y coordinate the console frame is at. This value may seem out of bounds due to
     * multiple monitors placed relative to the primary monitor.
     */
    private int consoleY;

    /**
     * The width of the console frame.
     */
    private int consoleWidth;

    /**
     * The height of the console frame.
     */
    private int consoleHeight;

    /**
     * The integer id of the monitor the console frame is on.
     */
    private int monitor;

    /**
     * Whether the console is in always on top mode.
     */
    private boolean consoleOnTop;

    /**
     * The direction the console frame is currently oriented in.
     */
    private Direction consoleFrameDirection;

    /**
     * Constructs a new ScreenStat object.
     *
     * @param consoleX              the x location of the console frame
     * @param consoleY              the y location of the console frame
     * @param consoleWidth          the width of the console frame
     * @param consoleHeight         the height of the console frame
     * @param monitor               the monitor id the console frame is on
     * @param consoleOnTop          whether the console frame is in always on top mode
     * @param consoleFrameDirection the direction the console frame is oriented in
     */
    public ScreenStat(int consoleX, int consoleY, int consoleWidth,
                      int consoleHeight, int monitor,
                      boolean consoleOnTop, Direction consoleFrameDirection) {
        this.consoleX = consoleX;
        this.consoleY = consoleY;
        this.consoleWidth = consoleWidth;
        this.consoleHeight = consoleHeight;
        this.monitor = monitor;
        this.consoleOnTop = consoleOnTop;
        this.consoleFrameDirection = consoleFrameDirection;

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns the x value of the console frame.
     *
     * @return the x value of the console frame
     */
    public int getConsoleX() {
        return consoleX;
    }

    /**
     * Sets the x value of the console frame.
     *
     * @param consoleX the x value of the console frame
     */
    public void setConsoleX(int consoleX) {
        this.consoleX = consoleX;
    }

    /**
     * Returns the y value of the console frame.
     *
     * @return the y value of the console frame
     */
    public int getConsoleY() {
        return consoleY;
    }

    /**
     * Sets the y value of the console frame.
     *
     * @param consoleY the y value of the console frame
     */
    public void setConsoleY(int consoleY) {
        this.consoleY = consoleY;
    }

    /**
     * Returns the width of the console frame.
     *
     * @return the width of the console frame
     */
    public int getConsoleWidth() {
        return consoleWidth;
    }

    /**
     * Sets the width of the console frame.
     *
     * @param consoleWidth the width of the console frame
     */
    public void setConsoleWidth(int consoleWidth) {
        this.consoleWidth = consoleWidth;
    }

    /**
     * Returns the height of the console frame.
     *
     * @return the height of the console frame
     */
    public int getConsoleHeight() {
        return consoleHeight;
    }

    /**
     * Sets the height of the console frame,
     *
     * @param consoleHeight the height of the console frame
     */
    public void setConsoleHeight(int consoleHeight) {
        this.consoleHeight = consoleHeight;
    }

    /**
     * Returns the integer id of the monitor the console frame is on.
     *
     * @return the integer id of the monitor the console frame is on
     */
    public int getMonitor() {
        return monitor;
    }

    /**
     * Sets the integer id of the monitor the console frame is on.
     *
     * @param monitor the integer id of the monitor the console frame is on
     */
    public void setMonitor(int monitor) {
        this.monitor = monitor;
    }

    /**
     * Returns whether the console frame is in always on top mode.
     *
     * @return whether the console frame is in always on top mode
     */
    public boolean isConsoleOnTop() {
        return consoleOnTop;
    }

    /**
     * Sets whether the console frame is in always on top mode.
     *
     * @param consoleOnTop whether the console frame is in always on top mode
     */
    public void setConsoleOnTop(boolean consoleOnTop) {
        this.consoleOnTop = consoleOnTop;
    }

    /**
     * Returns the direction the console frame is oriented in.
     *
     * @return the direction the console frame is oriented in
     */
    public Direction getConsoleFrameDirection() {
        return consoleFrameDirection;
    }

    /**
     * Sets the direction the console frame is oriented in.
     *
     * @param consoleFrameDirection the direction the console frame is oriented in
     */
    public void setConsoleFrameDirection(Direction consoleFrameDirection) {
        this.consoleFrameDirection = consoleFrameDirection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
