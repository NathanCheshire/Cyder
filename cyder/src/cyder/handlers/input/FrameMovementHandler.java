package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.ui.CyderFrame;
import cyder.utils.FrameUtil;

import java.awt.*;

/**
 * Handles CyderFrame and Console movement commands.
 */
public class FrameMovementHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private FrameMovementHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle({"top left", "top right", "bottom left", "bottom right",
            "consolidate windows", "dance", "hide", "askew", "barrelroll", "middle", "center"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().inputWithoutSpacesIs("topleft")) {
            Console.INSTANCE.setLocationOnScreen(CyderFrame.ScreenPosition.TOP_LEFT);
        } else if (getInputHandler().inputWithoutSpacesIs("topright")) {
            Console.INSTANCE.setLocationOnScreen(CyderFrame.ScreenPosition.TOP_RIGHT);
        } else if (getInputHandler().inputWithoutSpacesIs("bottomleft")) {
            Console.INSTANCE.setLocationOnScreen(CyderFrame.ScreenPosition.BOTTOM_LEFT);
        } else if (getInputHandler().inputWithoutSpacesIs("bottomright")) {
            Console.INSTANCE.setLocationOnScreen(CyderFrame.ScreenPosition.BOTTOM_RIGHT);
        } else if (getInputHandler().inputWithoutSpacesIs("middle")
                || getInputHandler().inputWithoutSpacesIs("center")) {
            Console.INSTANCE.setLocationOnScreen(CyderFrame.ScreenPosition.CENTER);
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

                        int anchorX = Console.INSTANCE.getConsoleCyderFrame().getX()
                                + Console.INSTANCE.getConsoleCyderFrame().getWidth()
                                - f.getWidth();
                        int anchorY = Console.INSTANCE.getConsoleCyderFrame().getY();

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

                        int anchorX = Console.INSTANCE.getConsoleCyderFrame().getX()
                                + Console.INSTANCE.getConsoleCyderFrame().getWidth()
                                - f.getWidth();
                        int anchorY = Console.INSTANCE.getConsoleCyderFrame().getY()
                                + Console.INSTANCE.getConsoleCyderFrame().getHeight()
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

                        int anchorX = Console.INSTANCE.getConsoleCyderFrame().getX();
                        int anchorY = Console.INSTANCE.getConsoleCyderFrame().getY()
                                + Console.INSTANCE.getConsoleCyderFrame().getHeight()
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

                        int anchorX = Console.INSTANCE.getConsoleCyderFrame().getX();
                        int anchorY = Console.INSTANCE.getConsoleCyderFrame().getY();

                        f.setRestoreX(anchorX);
                        f.setRestoreY(anchorY);
                        f.setLocation(anchorX, anchorY);
                    }
                } else {
                    getInputHandler().println("Command usage: consolidate windows top left");
                }
            } else if (getInputHandler().checkArgsLength(2)
                    && (getInputHandler().getArg(1).equalsIgnoreCase("center")
                    || getInputHandler().getArg(1).equalsIgnoreCase("middle"))) {
                Point consoleCenter = Console.INSTANCE.getConsoleCyderFrame().getCenterPointOnScreen();
                int x = (int) consoleCenter.getX();
                int y = (int) consoleCenter.getY();

                for (CyderFrame f : FrameUtil.getCyderFrames()) {
                    if (f == Console.INSTANCE.getConsoleCyderFrame()) {
                        continue;
                    }

                    if (f.getState() == Frame.ICONIFIED) {
                        f.setState(Frame.NORMAL);
                    }

                    int anchorX = x - f.getWidth() / 2;
                    int anchorY = y - f.getHeight() / 2;

                    f.setRestoreX(anchorX);
                    f.setRestoreY(anchorY);
                    f.setLocation(anchorX, anchorY);
                }
            } else {
                getInputHandler().println("Command usage: consolidate windows top left");
            }
        } else if (getInputHandler().commandIs("dance")) {
            Console.INSTANCE.dance();
        } else if (getInputHandler().commandIs("hide")) {
            Console.INSTANCE.getConsoleCyderFrame().minimizeAnimation();
        } else if (getInputHandler().inputWithoutSpacesIs("barrelroll")) {
            Console.INSTANCE.getConsoleCyderFrame().barrelRoll();
        } else if (getInputHandler().commandIs("askew")) {
            Console.INSTANCE.getConsoleCyderFrame().rotateBackground(5);
        } else {
            ret = false;
        }

        return ret;
    }
}
