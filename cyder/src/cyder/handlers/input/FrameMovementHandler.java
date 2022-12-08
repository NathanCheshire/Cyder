package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.console.Console;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.ui.frame.CyderFrame;
import cyder.ui.frame.ScreenPosition;
import cyder.utils.UiUtil;

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

    /**
     * The degrees to rotate the console pane by when making a frame askew.
     */
    private static final int ASKEW_DEGREE = 5;

    @Handle({"top left", "top right", "bottom left", "bottom right",
            "consolidate windows", "dance", "hide", "askew", "barrelroll", "middle", "center"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().inputIgnoringSpacesMatches("topleft")) {
            Console.INSTANCE.setLocationOnScreen(ScreenPosition.TRUE_TOP_LEFT);
        } else if (getInputHandler().inputIgnoringSpacesMatches("topright")) {
            Console.INSTANCE.setLocationOnScreen(ScreenPosition.TRUE_TOP_RIGHT);
        } else if (getInputHandler().inputIgnoringSpacesMatches("bottomleft")) {
            Console.INSTANCE.setLocationOnScreen(ScreenPosition.TRUE_BOTTOM_LEFT);
        } else if (getInputHandler().inputIgnoringSpacesMatches("bottomright")) {
            Console.INSTANCE.setLocationOnScreen(ScreenPosition.TRUE_BOTTOM_RIGHT);
        } else if (getInputHandler().inputIgnoringSpacesMatches("middle")
                || getInputHandler().inputIgnoringSpacesMatches("center")) {
            Console.INSTANCE.setLocationOnScreen(ScreenPosition.TRUE_CENTER);
        } else if (getInputHandler().inputIgnoringSpacesMatches("frame titles")) {
            for (Frame f : UiUtil.getFrames()) {
                getInputHandler().println(f.getTitle());
            }
        } else if (getInputHandler().commandIs("consolidate")
                && getInputHandler().getArg(0).equalsIgnoreCase("windows")) {
            if (getInputHandler().checkArgsLength(3)) {
                if (getInputHandler().getArg(1).equalsIgnoreCase("top")
                        && getInputHandler().getArg(2).equalsIgnoreCase("right")) {
                    for (CyderFrame f : UiUtil.getCyderFrames()) {
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
                    for (CyderFrame f : UiUtil.getCyderFrames()) {
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
                    for (CyderFrame f : UiUtil.getCyderFrames()) {
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
                    for (CyderFrame f : UiUtil.getCyderFrames()) {
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

                for (CyderFrame f : UiUtil.getCyderFrames()) {
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
            Console.INSTANCE.getConsoleCyderFrame().minimizeAndIconify();
        } else if (getInputHandler().inputIgnoringSpacesMatches("barrelroll")) {
            Console.INSTANCE.getConsoleCyderFrame().barrelRoll();
        } else if (getInputHandler().commandIs("askew")) {
            Console.INSTANCE.getConsoleCyderFrame().rotateBackground(ASKEW_DEGREE);
        } else {
            ret = false;
        }

        return ret;
    }
}
