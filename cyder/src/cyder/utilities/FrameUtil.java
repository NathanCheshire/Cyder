package cyder.utilities;

import cyder.constants.CyderStrings;
import cyder.ui.CyderFrame;

import java.awt.*;
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
}
