package cyder.utilities;

import cyder.constants.CyderStrings;
import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.CyderFrame;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

public class FrameUtil {
    /**
     * Instantiation of frame util not allowed.
     */
    private FrameUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * Returns a list of frames currently opened by this instance.
     *
     * @return a list of frames currently opened by this instance
     */
    public static LinkedList<Frame> getFrames() {
        return new LinkedList<>(Arrays.asList(Frame.getFrames()));
    }

    /**
     * Returns a list of CyderFrames currently opened by this instance.
     *
     * @return a list of CyderFrames currently opened by this instance
     */
    public static LinkedList<CyderFrame> getCyderFrames() {
        LinkedList<CyderFrame> ret = new LinkedList<>();

        for (Frame f : Frame.getFrames())
            if (f instanceof CyderFrame)
                ret.add((CyderFrame) f);

        return ret;
    }

    /**
     * Returns a list of non CyderFrame frame objects opened by this instance.
     *
     * @return a list of non CyderFrame frame objects opened by this instance
     */
    public static LinkedList<Frame> getNonCyderFrames() {
        LinkedList<Frame> ret = new LinkedList<>();

        for (Frame f : Frame.getFrames())
            if (!(f instanceof CyderFrame))
                ret.add(f);

        return ret;
    }

    /**
     * Saves a screenshot of all CyderFrames to the user's Files/ directory.
     */
    public static void screenshotCyderFrames() {
        for (CyderFrame frame : getCyderFrames()) {
            if (frame.isVisible()
                    && frame.getWidth() >= CyderFrame.MINIMUM_WIDTH
                    && frame.getHeight() >= CyderFrame.MINIMUM_HEIGHT)  {
                screenshotCyderFrame(frame);
            }
        }
    }

    /**
     * Saves a screenshot of the CyderFrame with the provided name to the user's Files/ directory.
     *
     * @param cyderFrameName the name of the CyderFrame to screenshot
     * @return whether or not the screenshot was successfully saved
     */
    public static boolean screenshotCyderFrame(String cyderFrameName) {
        CyderFrame refFrame = null;

        for (CyderFrame frame : getCyderFrames()) {
            if (frame.getTitle().equalsIgnoreCase(cyderFrameName)) {
                refFrame = frame;
                break;
            }
        }

        if (refFrame == null)
            return false;

        String saveName = refFrame.getTitle().substring(0, Math.min(15, refFrame.getTitle().length()));
        File refFile = OSUtil.createFileInUserSpace(saveName + "_" + TimeUtil.logSubDirTime() + ".png");
        return screenshotCyderFrame(refFrame, refFile);
    }

    /**
     * The max allowable length when including a frame's title in a filename.
     */
    public static final int MAX_FRAME_TITLE_FILE_LENGTH = 15;

    /**
     * Saves a screenshot of the CyderFrame with the provided name to the user's Files/ directory.
     *
     * @param cyderFrame the CyderFrame to screenshot
     * @return whether or not the screenshot was successfully saved
     */
    public static boolean screenshotCyderFrame(CyderFrame cyderFrame) {
        if (cyderFrame == null)
            throw new IllegalArgumentException("Valid CyderFrame with provided name does not exist");

        String saveName = cyderFrame.getTitle().substring(0,
                Math.min(MAX_FRAME_TITLE_FILE_LENGTH, cyderFrame.getTitle().length()));
        File refFile = OSUtil.createFileInUserSpace(saveName + "_" + TimeUtil.logSubDirTime() + ".png");
        return screenshotCyderFrame(cyderFrame, refFile);
    }

    /**
     * Saves a screenshot of the provided CyderFrame to the provided reference file.
     *
     * @param frame the CyderFrame to take a screenshot of
     * @return whether or not the screenshot was successfully saved
     */
    public static boolean screenshotCyderFrame(CyderFrame frame, File saveFile) {
        if (frame == null)
            throw new IllegalArgumentException("Provided frame is null");
        if (saveFile == null)
            throw new IllegalArgumentException("Provided file is null");

        boolean ret = false;

        try {
            ret = ImageIO.write(ImageUtil.getScreenShot(frame), "png", saveFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * Attempts to set the provided frame to the monitor specified,
     * if valid, with the provided starting location.
     *
     * @param requestedMonitor the id number of the monitor to place the frame on
     * @param requestedX the x value to set the frame to
     * @param requestedY the y value to set the frame to
     * @param frame the frame to set the location/size of
     */
    public static void requestFramePosition(int requestedMonitor, int requestedX,
                                            int requestedY, CyderFrame frame) {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screenDevices = graphicsEnvironment.getScreenDevices();
        Rectangle requestedScreenBounds = null;

        //if the monitor is valid, use its bounds
        if (requestedMonitor > -1 && requestedMonitor < screenDevices.length) {
            requestedScreenBounds = screenDevices[requestedMonitor].getDefaultConfiguration().getBounds();
        } else if (screenDevices.length > 0) {
            requestedScreenBounds = screenDevices[0].getDefaultConfiguration().getBounds();
        } else {
            throw new IllegalStateException("No monitors were found. " + CyderStrings.europeanToymaker);
        }

        int monitorX = requestedScreenBounds.x;
        int monitorY = requestedScreenBounds.y;
        int monitorWidth = requestedScreenBounds.width;
        int monitorHeight = requestedScreenBounds.height;

        //if too far right, set to max x for this monitor
        if (requestedX + frame.getWidth() > monitorX + monitorWidth) {
            requestedX = monitorX  + monitorWidth - frame.getWidth();
        }

        //if too far left, set to min x for this monitor
        else if (requestedX < monitorX) {
            requestedX = monitorX;
        }

        //if too far down, set to max y for this monitor
        if (requestedY + frame.getHeight() > monitorY + monitorHeight) {
            requestedY = monitorY + monitorHeight - frame.getHeight();
        }

        //if too far up, set to min y
        else if (requestedY < monitorY) {
            requestedY = monitorY;
        }

        //set the location to the calculated location
        frame.setLocation(requestedX, requestedY);
    }
}
