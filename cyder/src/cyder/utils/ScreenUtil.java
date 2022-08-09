package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

import java.awt.*;

/**
 * Static util class for utilities revolving around the possibility of multiple monitors/displays.
 */
public final class ScreenUtil {
    /**
     * Suppress default constructor.
     */
    private ScreenUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the width of the primary display.
     *
     * @return the width of the primary display
     */
    public static int getScreenWidth() {
        return (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    }

    /**
     * Returns the height of the primary display.
     *
     * @return the height of the primary display
     */
    public static int getScreenHeight() {
        return (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    }

    /**
     * Returns the device that the provided frame is on.
     *
     * @param frame the frame to find the monitor of
     * @return the device that the provided frame is on
     */
    public static GraphicsDevice getDevice(Frame frame) {
        Preconditions.checkNotNull(frame);

        return frame.getGraphicsConfiguration().getDevice();
    }

    /**
     * Returns the number associated with the monitor the provided frame is on.
     *
     * @param frame the frame to find the monitor number of
     * @return the number associated with the monitor the provided frame is on
     */
    public static int getMonitorNumber(Frame frame) {
        Preconditions.checkNotNull(frame);

        return Integer.parseInt(frame.getGraphicsConfiguration().getDevice()
                .getIDstring().replaceAll(CyderRegexPatterns.nonNumberRegex, ""));
    }

    /**
     * Returns the width of the monitor the frame is currently on.
     *
     * @param frame the frame of which to find the monitor's width
     * @return the width of the monitor the frame is currently on
     */
    public static double getMonitorWidth(Frame frame) {
        Preconditions.checkNotNull(frame);

        return frame.getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds().getWidth();
    }

    /**
     * Returns the height of the monitor the frame is currently on.
     *
     * @param frame the frame of which to find the monitor's height
     * @return the height of the monitor the frame is currently on
     */
    public static double getMonitorHeight(Frame frame) {
        Preconditions.checkNotNull(frame);

        return frame.getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds().getHeight();
    }

    /**
     * Returns the bounds and offsets of the monitor that the provided Frame is on.
     *
     * @param frame the frame to find the bounds of
     * @return the bounds and offsets of the monitor that the provided Frame is on
     */
    public static Rectangle getMonitorBounds(Frame frame) {
        Preconditions.checkNotNull(frame);

        return frame.getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds();
    }

    /**
     * Returns the bounds and offsets of the monitor.
     *
     * @param monitorIndex the index of the monitor to return the bounds of
     * @return the bounds and offsets of the monitor
     */
    public static Rectangle getMonitorBounds(int monitorIndex) {
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

        if (monitorIndex > 0 && monitorIndex < devices.length) {
            return devices[monitorIndex].getDefaultConfiguration().getBounds();
        }

        throw new IllegalArgumentException("Provided monitor index is invalid");
    }

    /**
     * Returns the x offset of the monitor that the provided frame is on.
     *
     * @param frame the frame to find the monitors x offset of
     * @return the x offset of the monitor that the provided frame is on.
     */
    public static double getMonitorXOffset(Frame frame) {
        Preconditions.checkNotNull(frame);

        return getMonitorBounds(frame).getX();
    }

    /**
     * Returns the y offset of the monitor that the provided frame is on.
     *
     * @param frame the frame to find the monitors y offset of
     * @return the y offset of the monitor that the provided frame is on.
     */
    public static double getMonitorYOffset(Frame frame) {
        Preconditions.checkNotNull(frame);

        return getMonitorBounds(frame).getY();
    }
}
