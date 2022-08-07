package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.CyderFrame;
import cyder.user.UserUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

/**
 * Utilities to control, update, modify, and create CyderFrames.
 */
public final class FrameUtil {
    /**
     * Suppress default constructor.
     */
    private FrameUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns a list of frames currently opened by this Jvm instance.
     *
     * @return a list of frames currently opened by this Jvm instance
     */
    public static ImmutableList<Frame> getFrames() {
        return ImmutableList.copyOf(Frame.getFrames());
    }

    /**
     * Returns a list of CyderFrames currently opened by this instance.
     *
     * @return a list of CyderFrames currently opened by this instance
     */
    public static ImmutableList<CyderFrame> getCyderFrames() {
        ArrayList<CyderFrame> ret = new ArrayList<>();

        for (Frame f : Frame.getFrames()) {
            if (f instanceof CyderFrame) {
                ret.add((CyderFrame) f);
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns a list of non CyderFrame frame objects opened by this instance.
     *
     * @return a list of non CyderFrame frame objects opened by this instance
     */
    public static ImmutableList<Frame> getNonCyderFrames() {
        ArrayList<Frame> ret = new ArrayList<>();

        for (Frame f : Frame.getFrames()) {
            if (!(f instanceof CyderFrame)) {
                ret.add(f);
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Saves a screenshot of all CyderFrames to the user's Files/ directory.
     */
    public static void screenshotCyderFrames() {
        for (CyderFrame frame : getCyderFrames()) {
            if (frame.isVisible() && frame.getWidth() >= CyderFrame.MINIMUM_WIDTH
                    && frame.getHeight() >= CyderFrame.MINIMUM_HEIGHT) {
                screenshotCyderFrame(frame);
            }
        }
    }

    /**
     * Saves a screenshot of the CyderFrame with the provided name to the user's Files/ directory.
     *
     * @param cyderFrameName the name of the CyderFrame to screenshot
     * @return whether the screenshot was successfully saved
     */
    public static boolean screenshotCyderFrame(String cyderFrameName) {
        CyderFrame refFrame = null;

        for (CyderFrame frame : getCyderFrames()) {
            if (frame.getTitle().equalsIgnoreCase(cyderFrameName)) {
                refFrame = frame;
                break;
            }
        }

        if (refFrame == null) {
            return false;
        }

        String saveName = refFrame.getTitle().substring(0, Math.min(15, refFrame.getTitle().length()));
        File refFile = UserUtil.createFileInUserSpace(saveName + "_" + TimeUtil.logTime()
                + "_" + TimeUtil.logTime() + ".png");
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
     */
    public static void screenshotCyderFrame(CyderFrame cyderFrame) {
        Preconditions.checkNotNull(cyderFrame);

        String saveName = cyderFrame.getTitle().substring(0,
                Math.min(MAX_FRAME_TITLE_FILE_LENGTH, cyderFrame.getTitle().length()));
        File refFile = UserUtil.createFileInUserSpace(saveName.trim() + "_"
                + TimeUtil.logTime().trim() + ".png");
        screenshotCyderFrame(cyderFrame, refFile);
    }

    /**
     * Saves a screenshot of the provided CyderFrame to the provided reference file.
     *
     * @param frame the CyderFrame to take a screenshot of
     * @return whether the screenshot was successfully saved
     */
    public static boolean screenshotCyderFrame(CyderFrame frame, File saveFile) {
        Preconditions.checkNotNull(frame);
        Preconditions.checkNotNull(saveFile);

        boolean ret = false;

        try {
            ret = ImageIO.write(ImageUtil.screenshotComponent(frame), ImageUtil.PNG_FORMAT, saveFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * Attempts to set the provided frame to the monitor specified,
     * if valid, with the provided starting location.
     *
     * @param requestedMonitor the id number of the monitor to place the frame on, if invalid,
     *                         the frame is placed in the center of the primary display
     * @param requestedX       the x value to set the frame to
     * @param requestedY       the y value to set the frame to
     * @param frame            the frame to set the location/size of
     */
    public static void requestFramePosition(int requestedMonitor, int requestedX,
                                            int requestedY, CyderFrame frame) {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screenDevices = graphicsEnvironment.getScreenDevices();
        Rectangle requestedScreenBounds;

        //if the monitor is valid, use its bounds
        if (requestedMonitor > -1 && requestedMonitor < screenDevices.length) {
            requestedScreenBounds = screenDevices[requestedMonitor].getDefaultConfiguration().getBounds();
        } else if (screenDevices.length > 0) {
            requestedScreenBounds = screenDevices[0].getDefaultConfiguration().getBounds();
        } else {
            throw new IllegalStateException("No monitors were found. " + CyderStrings.EUROPEAN_TOY_MAKER);
        }

        int monitorX = requestedScreenBounds.x;
        int monitorY = requestedScreenBounds.y;
        int monitorWidth = requestedScreenBounds.width;
        int monitorHeight = requestedScreenBounds.height;

        //if too far right, set to max x for this monitor
        if (requestedX + frame.getWidth() > monitorX + monitorWidth) {
            requestedX = monitorX + monitorWidth - frame.getWidth();
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

    /**
     * Closes all instances of {@link Frame} by invoking {@link Frame#dispose()} on all instances.
     */
    public static void closeAllFrames() {
        for (Frame frame : Frame.getFrames()) {
            frame.dispose();
        }
    }

    /**
     * Closes all instances of Frame. If a frame is an instance of CyderFrame,
     * fastClose follows the value provided.
     *
     * @param fastClose    whether to fastClose any instances of CyderFrame
     * @param ignoreFrames frames to not dispose if encountered
     */
    public static void closeAllFrames(boolean fastClose, Frame... ignoreFrames) {
        for (Frame frame : Frame.getFrames()) {
            boolean skip = false;

            if (ignoreFrames.length > 0) {
                for (Frame ignoreFrame : ignoreFrames) {
                    if (ignoreFrame == frame) {
                        skip = true;
                        break;
                    }
                }
            }

            if (skip) {
                continue;
            }

            if (frame instanceof CyderFrame) {
                ((CyderFrame) frame).dispose(fastClose);
            } else {
                frame.dispose();
            }
        }
    }

    /**
     * Closes all instances of CyderFrame.
     *
     * @param fastClose whether to invoke fast close on all CyderFrames found
     */
    public static void closeAllCyderFrames(boolean fastClose) {
        for (CyderFrame f : getCyderFrames()) {
            f.dispose(fastClose);
        }
    }

    /**
     * Repaints all valid instances of CyderFrame.
     */
    public static void repaintCyderFrames() {
        for (CyderFrame frame : getCyderFrames()) {
            frame.repaint();
        }
    }

    /**
     * Minimizes all {@link Frame} instances by setting their state to {@link Frame#ICONIFIED}.
     * Found {@link CyderFrame}s have their {@link CyderFrame#minimizeAnimation()} invoked instead.
     */
    public static void minimizeAllFrames() {
        for (Frame f : getFrames()) {
            if (f instanceof CyderFrame) {
                ((CyderFrame) f).minimizeAnimation();
            } else {
                f.setState(Frame.ICONIFIED);
            }
        }
    }

    /**
     * Generates the common runnable invoked when a CyderFrame TaskbarIcon is clicked in the Console menu.
     *
     * @param frame the CyderFrame to create the runnable for
     * @return the common runnable invoked when a CyderFrame TaskbarIcon is clicked in the Console menu
     */
    public static Runnable generateCommonFrameTaskbarIconRunnable(CyderFrame frame) {
        Preconditions.checkNotNull(frame);

        return () -> {
            if (frame.getState() == Frame.NORMAL) {
                frame.minimizeAnimation();
            } else {
                frame.setState(Frame.NORMAL);
            }
        };
    }

    /**
     * The index which determines which color to choose for the border color.
     */
    private static int colorIndex;

    /**
     * Returns the color to be associated with a CyderFrame's TaskbarIcon border color.
     *
     * @return the color to be associated with a CyderFrame's TaskbarIcon border color
     */
    public static Color getTaskbarBorderColor() {
        Color ret = CyderColors.TASKBAR_BORDER_COLORS.get(colorIndex);
        colorIndex++;

        if (colorIndex > CyderColors.TASKBAR_BORDER_COLORS.size() - 1) {
            colorIndex = 0;
        }

        return ret;
    }

    /**
     * Generates a key adapter to use for a field to invoke the provided runnable.
     *
     * @param typed    whether the runnable should be invoked when a key is typed
     * @param pressed  whether the runnable should be invoked when a key is pressed
     * @param released whether the runnable should be invoked when a key is released
     * @param runnable the runnable to invoke
     * @return the generated key adapter
     */
    public static KeyAdapter generateKeyAdapter(boolean typed, boolean pressed, boolean released, Runnable runnable) {
        return new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (typed) {
                    runnable.run();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (pressed) {
                    runnable.run();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (released) {
                    runnable.run();
                }
            }
        };
    }
}
