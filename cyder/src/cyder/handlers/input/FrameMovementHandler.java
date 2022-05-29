package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.utilities.FrameUtil;

import java.awt.*;

/**
 * Handles CyderFrame and ConsoleFrame movement commands.
 */
public class FrameMovementHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private FrameMovementHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Handle({"top left", "top right", "bottom left", "bottom right", "consolidate windows", "dance", "hide"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().inputWithoutSpacesIs("top left")) {
            ConsoleFrame.INSTANCE.setLocationOnScreen(ConsoleFrame.ScreenPosition.TOP_LEFT);
        } else if (getInputHandler().inputWithoutSpacesIs("top right")) {
            ConsoleFrame.INSTANCE.setLocationOnScreen(ConsoleFrame.ScreenPosition.TOP_RIGHT);
        } else if (getInputHandler().inputWithoutSpacesIs("bottom left")) {
            ConsoleFrame.INSTANCE.setLocationOnScreen(ConsoleFrame.ScreenPosition.BOTTOM_LEFT);
        } else if (getInputHandler().inputWithoutSpacesIs("bottom right")) {
            ConsoleFrame.INSTANCE.setLocationOnScreen(ConsoleFrame.ScreenPosition.BOTTOM_RIGHT);
        } else if (getInputHandler().inputWithoutSpacesIs("middle")
                || getInputHandler().inputWithoutSpacesIs("center")) {
            ConsoleFrame.INSTANCE.setLocationOnScreen(ConsoleFrame.ScreenPosition.CENTER);
        } else if (getInputHandler().inputWithoutSpacesIs("frametitles")) {
            Frame[] frames = Frame.getFrames();
            for (Frame f : frames)
                if (f instanceof CyderFrame) {
                    getInputHandler().println(f.getTitle());
                } else {
                    getInputHandler().println(f.getTitle());
                }
        } else if (getInputHandler().commandIs("consolidate")
                && getInputHandler().getArg(0).equalsIgnoreCase("windows")) {
            if (getInputHandler().checkArgsLength(3)) {
                if (getInputHandler().getArg(1).equalsIgnoreCase("top")
                        && getInputHandler().getArg(2).equalsIgnoreCase("right")) {
                    for (CyderFrame f : FrameUtil.getCyderFrames()) {
                        if (f.getState() == Frame.ICONIFIED) {
                            f.setState(Frame.NORMAL);
                        }

                        int anchorX = ConsoleFrame.INSTANCE.getConsoleCyderFrame().getX()
                                + ConsoleFrame.INSTANCE.getConsoleCyderFrame().getWidth()
                                - f.getWidth();
                        int anchorY = ConsoleFrame.INSTANCE.getConsoleCyderFrame().getY();

                        f.setRestoreX(anchorX);
                        f.setRestoreY(anchorY);
                        f.setLocation(anchorX, anchorY);
                    }
                } else if (getInputHandler().getArg(1).equalsIgnoreCase("bottom")
                        && getInputHandler().getArg(2).equalsIgnoreCase("right")) {
                    for (CyderFrame f : FrameUtil.getCyderFrames()) {
                        if (f.getState() == Frame.ICONIFIED) {
                            f.setState(Frame.NORMAL);
                        }

                        int anchorX = ConsoleFrame.INSTANCE.getConsoleCyderFrame().getX()
                                + ConsoleFrame.INSTANCE.getConsoleCyderFrame().getWidth()
                                - f.getWidth();
                        int anchorY = ConsoleFrame.INSTANCE.getConsoleCyderFrame().getY()
                                + ConsoleFrame.INSTANCE.getConsoleCyderFrame().getHeight()
                                - f.getHeight();

                        f.setRestoreX(anchorX);
                        f.setRestoreY(anchorY);
                        f.setLocation(anchorX, anchorY);
                    }
                } else if (getInputHandler().getArg(1).equalsIgnoreCase("bottom")
                        && getInputHandler().getArg(2).equalsIgnoreCase("left")) {
                    for (CyderFrame f : FrameUtil.getCyderFrames()) {
                        if (f.getState() == Frame.ICONIFIED) {
                            f.setState(Frame.NORMAL);
                        }

                        int anchorX = ConsoleFrame.INSTANCE.getConsoleCyderFrame().getX();
                        int anchorY = ConsoleFrame.INSTANCE.getConsoleCyderFrame().getY()
                                + ConsoleFrame.INSTANCE.getConsoleCyderFrame().getHeight()
                                - f.getHeight();

                        f.setRestoreX(anchorX);
                        f.setRestoreY(anchorY);
                        f.setLocation(anchorX, anchorY);
                    }
                } else if (getInputHandler().getArg(1).equalsIgnoreCase("top")
                        && getInputHandler().getArg(2).equalsIgnoreCase("left")) {
                    for (CyderFrame f : FrameUtil.getCyderFrames()) {
                        if (f.getState() == Frame.ICONIFIED) {
                            f.setState(Frame.NORMAL);
                        }

                        int anchorX = ConsoleFrame.INSTANCE.getConsoleCyderFrame().getX();
                        int anchorY = ConsoleFrame.INSTANCE.getConsoleCyderFrame().getY();

                        f.setRestoreX(anchorX);
                        f.setRestoreY(anchorY);
                        f.setLocation(anchorX, anchorY);
                    }
                }
            } else {
                getInputHandler().println("Command usage: consolidate windows top left");
            }
        } else if (getInputHandler().commandIs("dance")) {
            ConsoleFrame.INSTANCE.dance();
        } else if (getInputHandler().commandIs("hide")) {
            ConsoleFrame.INSTANCE.getConsoleCyderFrame().minimizeAnimation();
        } else {
            ret = false;
        }

        return ret;
    }
}
