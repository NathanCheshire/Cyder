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
    private FrameUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    public static LinkedList<Frame> getFrames() {
        return new LinkedList<>(Arrays.asList(Frame.getFrames()));
    }

    public static LinkedList<CyderFrame> getCyderFrames() {
        LinkedList<CyderFrame> ret = new LinkedList<>();

        for (Frame f : Frame.getFrames())
            if (f instanceof CyderFrame)
                ret.add((CyderFrame) f);

        return ret;
    }

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
            if (frame.getName().equalsIgnoreCase(cyderFrameName)) {
                refFrame = frame;
                break;
            }
        }

        if (refFrame == null)
            throw new IllegalArgumentException("Valid CyderFrame with provided name does not exist");

        //todo substring the title to 10 chars
        File refFile = OSUtil.createFileInUserSpace(refFrame.getName() + TimeUtil.logSubDirTime() + ".png");
        return screenshotCyderFrame(refFrame, refFile);
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
}
