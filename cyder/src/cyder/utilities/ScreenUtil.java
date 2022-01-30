package cyder.utilities;

import java.awt.*;

/**
 * Static util class for utilities revolving around the possibility of multiple monitors/displays.
 */
public class ScreenUtil {
    /**
     * Returns the device that the provided frame is on.
     *
     * @param frame the frame to find the monitor of
     * @return the device that the provided frame is on
     */
    public static GraphicsDevice getDevice(Frame frame) {
        return frame.getGraphicsConfiguration().getDevice();
    }

    /**
     * Returns the number associated with the monitor the provided frame is on.
     *
     * @param frame the frame to find the monitor number of
     * @return the number associated with the monitor the provided frame is on
     */
    public static int getMonitorNumber(Frame frame) {
        return Integer.parseInt(frame.getGraphicsConfiguration().getDevice()
                .getIDstring().replaceAll("[^0-9]",""));
    }

    /**
     * Returns the width of the monitor the frame is currently on.
     *
     * @param frame the frame of which to find the monitor's width
     * @return the width of the monitor the frame is currently on
     */
    public static double getMonitorWidth(Frame frame) {
        return frame.getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds().getWidth();
    }

    /**
     * Returns the height of the monitor the frame is currently on.
     *
     * @param frame the frame of which to find the monitor's height
     * @return the height of the monitor the frame is currently on
     */
    public static double getMonitorHeight(Frame frame) {
        return frame.getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds().getHeight();
    }

    /**
     * Returns the bounds and offsets of the monitor that the provided Frame is on.
     *
     * @param frame the frame to find the bounds of
     * @return the bounds and offsets of the monitor that the provided Frame is on
     */
    public static Rectangle getMonitorBounds(Frame frame) {
        return frame.getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds();
    }

    /**
     * Returns the x offset of the monitor that the provided frame is on.
     *
     * @param frame the frame to find the monitors x offset of
     * @return the x offset of the monitor that the provided frame is on.
     */
    public static double getMonitorXOffset(Frame frame) {
        return getMonitorBounds(frame).getX();
    }

    /**
     * Returns the y offset of the monitor that the provided frame is on.
     *
     * @param frame the frame to find the monitors y offset of
     * @return the y offset of the monitor that the provided frame is on.
     */
    public static double getMonitorYOffset(Frame frame) {
        return getMonitorBounds(frame).getY();
    }
}
