package cyder.utilities;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Utililties revolving around animations, typically using threads.
 */
public class AnimationUtil {
    /**
     * Suppress default constructor.
     */
    private AnimationUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Moves the specified frame object up until it is no longer visible then invokes dispose
     *
     * @param frame the frame object to close
     */
    public static void closeAnimation(Frame frame) {
        if (frame instanceof CyderFrame) {
            ((CyderFrame) frame).disableDragging();
        }

        try {
            if (frame != null && frame.isVisible()) {
                Point point = frame.getLocationOnScreen();
                int x = (int) point.getX();
                int y = (int) point.getY();

                for (int i = y; i >= -frame.getHeight(); i -= 15) {
                    Thread.sleep(0, 500);
                    frame.setLocation(x, i);
                }

                frame.dispose();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Moves the specified frame object down until it is no longer visible then sets the frame's state
     * to Frame.ICONIFIED. This method works for anything that inherits from JFrame
     *
     * @param frame the frame object to minimize and iconify
     */
    public static void minimizeAnimation(JFrame frame) {
        boolean wasEnabled = ((CyderFrame) frame).isDraggingEnabled();

        if (frame instanceof CyderFrame) {
            ((CyderFrame) frame).disableDragging();
        }

        Point point = frame.getLocationOnScreen();
        int x = (int) point.getX();
        int y = (int) point.getY();

        try {
            for (int i = y; i <= ScreenUtil.getScreenHeight(); i += 15) {
                Thread.sleep(0, 250);
                frame.setLocation(x, i);
            }

            frame.setState(JFrame.ICONIFIED);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        if (frame instanceof CyderFrame && wasEnabled) {
            ((CyderFrame) frame).enableDragging();
        }
    }

    /**
     * Master method to animate any component up in a separate thread
     *
     * @param start     the starting y value
     * @param stop      the ending y value
     * @param delay     the ms delay in between increments
     * @param increment the increment value
     * @param comp      the component to move
     */
    public static void componentUpSepThread(int start, int stop, int delay, int increment, Component comp) {
        if (comp.getY() == start)
            CyderThreadRunner.submit(() -> {
                for (int i = start; i >= stop; i -= increment) {
                    try {
                        Thread.sleep(delay);
                        comp.setLocation(comp.getX(), i);
                    } catch (InterruptedException e) {
                        ExceptionHandler.handle(e);
                    }
                }
                comp.setLocation(comp.getX(), stop);
            }, "component up thread");
    }

    /**
     * Master method to animate any component down in a separate thread
     *
     * @param start     the starting y value
     * @param stop      the ending y value
     * @param delay     the ms delay in between increments
     * @param increment the increment value
     * @param comp      the component to move
     */
    public static void componentDownSepThread(int start, int stop, int delay, int increment, Component comp) {
        if (comp.getY() == start)
            CyderThreadRunner.submit(() -> {
                for (int i = start; i <= stop; i += increment) {
                    try {
                        Thread.sleep(delay);
                        comp.setLocation(comp.getX(), i);
                    } catch (InterruptedException e) {
                        ExceptionHandler.handle(e);
                    }
                }
                comp.setLocation(comp.getX(), stop);
            }, "component down thread");
    }

    /**
     * Master method to animate any component left in a separate thread
     *
     * @param start     the starting x value
     * @param stop      the ending x value
     * @param delay     the ms delay in between increments
     * @param increment the increment value
     * @param comp      the component to move
     */
    public static void componentLeftSepThread(int start, int stop, int delay, int increment, Component comp) {
        if (comp.getX() == start)
            CyderThreadRunner.submit(() -> {
                for (int i = start; i >= stop; i -= increment) {
                    try {
                        Thread.sleep(delay);
                        comp.setLocation(i, comp.getY());
                    } catch (InterruptedException e) {
                        ExceptionHandler.handle(e);
                    }
                }
                comp.setLocation(stop, comp.getY());
            }, "component left thread");
    }

    /**
     * Master method to animate any component right in a separate thread
     *
     * @param start     the starting x value
     * @param stop      the ending x value
     * @param delay     the ms delay in between increments
     * @param increment the increment value
     * @param comp      the component to move
     */
    public static void componentRightSepThread(int start, int stop, int delay, int increment, Component comp) {
        if (comp.getX() == start)
            CyderThreadRunner.submit(() -> {
                for (int i = start; i <= stop; i += increment) {
                    try {
                        Thread.sleep(delay);
                        comp.setLocation(i, comp.getY());
                    } catch (InterruptedException e) {
                        ExceptionHandler.handle(e);
                    }
                }
                comp.setLocation(stop, comp.getY());
            }, "component right thread");
    }
}
