package cyder.handlers.internal.objects;

import java.awt.*;

/**
 * Object specifying a point on a specific monitor.
 */
public class MonitorPoint {
    private int x;
    private int y;
    private int monitor;

    public MonitorPoint(int x, int y, int monitor) {
        this.x = x;
        this.y = y;
        this.monitor = monitor;
    }

    public MonitorPoint(Point p, int monitor) {
        this.x = (int) p.getX();
        this.y = (int) p.getY();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getMonitor() {
        return monitor;
    }

    public void setMonitor(int monitor) {
        this.monitor = monitor;
    }

    /**
     * Returns the bounds for the monitor at the stored index.
     *
     * @return the bounds for the monitor at the stored index
     */
    public Rectangle getMonitorBounds() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getScreenDevices()[monitor].getDefaultConfiguration().getBounds();
    }

    /**
     * Returns whether the monitor index is a valid monitor.
     *
     * @return whether the monitor index is a valid monitor
     */
    public boolean validateMonitor() {
        return monitor > -1 && monitor
                < GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;
    }

    @Override
    public String toString() {
        return this.x + ", " + this.y + ", monitor: " + this.monitor;
    }

    @Override
    public int hashCode() {
        int ret = Integer.hashCode(x);
        ret = 31 * ret + Integer.hashCode(y);
        ret = 31 * ret + Integer.hashCode(monitor);
        return ret;
    }
}
