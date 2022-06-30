package cyder.utils;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Utilities to animate components.
 */
public class AnimationUtil {
    /**
     * Suppress default constructor.
     */
    private AnimationUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The amount of nanoseconds to sleep by when performing a close
     * animation on a {@link Frame} via {@link #closeAnimation(Frame)}.
     */
    public static final int CLOSE_ANIMATION_NANO_TIMEOUT = 500;

    /**
     * The amount of display pixels to decrement by when performing a close animation.
     */
    public static final int CLOSE_ANIMATION_INC = 15;

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

                for (int i = y ; i >= -frame.getHeight() ; i -= CLOSE_ANIMATION_INC) {
                    Thread.sleep(0, CLOSE_ANIMATION_NANO_TIMEOUT);
                    frame.setLocation(x, i);
                }

                frame.dispose();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * The amount of nanoseconds to sleep by when performing a minimize
     * animation on a {@link CyderFrame} via {@link #minimizeAnimation(CyderFrame)}.
     */
    public static final int MINIMIZE_ANIMATION_NANO_TIMEOUT = 250;

    /**
     * The amount of display pixels to increment by when performing a minimize animation.
     */
    public static final int MINIMIZE_ANIMATION_INC = 15;

    /**
     * Moves the specified cyderFrame object down until it is no longer
     * visible then sets the cyderFrame's state to Frame.ICONIFIED.
     * This method works for anything that inherits from JFrame
     *
     * @param cyderFrame the CyderFrame object to minimize and iconify
     */
    public static void minimizeAnimation(CyderFrame cyderFrame) {
        boolean wasEnabled = cyderFrame.isDraggingEnabled();

        cyderFrame.disableDragging();

        Point point = cyderFrame.getLocationOnScreen();
        int x = (int) point.getX();
        int y = (int) point.getY();

        try {
            for (int i = y ; i <= ScreenUtil.getScreenHeight() ; i += MINIMIZE_ANIMATION_INC) {
                Thread.sleep(0, MINIMIZE_ANIMATION_NANO_TIMEOUT);
                cyderFrame.setLocation(x, i);
            }

            cyderFrame.setState(JFrame.ICONIFIED);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        if (wasEnabled) {
            cyderFrame.enableDragging();
        }
    }

    /**
     * Moves the provided component from the starting value to the
     * ending value by the increment amount, sleeping for the specified millisecond delay
     * in between increments.
     *
     * @param startY     the starting y value
     * @param endY      the ending y value
     * @param delay     the ms delay in between increments
     * @param increment the increment value
     * @param comp      the component to move
     */
    public static void moveComponentUp(int startY, int endY, int delay, int increment, Component comp) {
        if (comp.getY() == startY)
            CyderThreadRunner.submit(() -> {
                for (int i = startY ; i >= endY ; i -= increment) {
                    try {
                        Thread.sleep(delay);
                        comp.setLocation(comp.getX(), i);
                    } catch (InterruptedException e) {
                        ExceptionHandler.handle(e);
                    }
                }
                comp.setLocation(comp.getX(), endY);
            }, "Component Up Animator, comp=" + comp);
    }

    /**
     * Moves the provided component from the starting value to the
     * ending value by the increment amount, sleeping for the specified millisecond delay
     * in between increments.
     *
     * @param startY     the starting y value
     * @param stopY      the ending y value
     * @param delay     the ms delay in between increments
     * @param increment the increment value
     * @param comp      the component to move
     */
    public static void moveComponentDown(int startY, int stopY, int delay, int increment, Component comp) {
        if (comp.getY() == startY)
            CyderThreadRunner.submit(() -> {
                for (int i = startY ; i <= stopY ; i += increment) {
                    try {
                        Thread.sleep(delay);
                        comp.setLocation(comp.getX(), i);
                    } catch (InterruptedException e) {
                        ExceptionHandler.handle(e);
                    }
                }
                comp.setLocation(comp.getX(), stopY);
            }, "Component Down Animator, comp=" + comp);
    }

    /**
     * Moves the provided component from the starting value to the
     * ending value by the increment amount, sleeping for the specified millisecond delay
     * in between increments.
     *
     * @param startX     the starting x value
     * @param stopX      the ending x value
     * @param delay     the ms delay in between increments
     * @param increment the increment value
     * @param comp      the component to move
     */
    public static void moveComponentLeft(int startX, int stopX, int delay, int increment, Component comp) {
        if (comp.getX() == startX)
            CyderThreadRunner.submit(() -> {
                for (int i = startX ; i >= stopX ; i -= increment) {
                    try {
                        Thread.sleep(delay);
                        comp.setLocation(i, comp.getY());
                    } catch (InterruptedException e) {
                        ExceptionHandler.handle(e);
                    }
                }
                comp.setLocation(stopX, comp.getY());
            }, "Component Left Animator, comp=" + comp);
    }

    /**
     * Moves the provided component from the starting value to the
     * ending value by the increment amount, sleeping for the specified millisecond delay
     * in between increments.
     *
     * @param startX     the starting x value
     * @param stopX      the ending x value
     * @param delay     the ms delay in between increments
     * @param increment the increment value
     * @param comp      the component to move
     */
    public static void moveComponentRight(int startX, int stopX, int delay, int increment, Component comp) {
        if (comp.getX() == startX)
            CyderThreadRunner.submit(() -> {
                for (int i = startX ; i <= stopX ; i += increment) {
                    try {
                        Thread.sleep(delay);
                        comp.setLocation(i, comp.getY());
                    } catch (InterruptedException e) {
                        ExceptionHandler.handle(e);
                    }
                }
                comp.setLocation(stopX, comp.getY());
            }, "Component Right Animator, comp=" + comp);
    }
}
