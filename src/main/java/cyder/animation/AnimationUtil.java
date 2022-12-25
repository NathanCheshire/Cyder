package cyder.animation;

import com.google.common.base.Preconditions;
import cyder.console.ConsoleConstants;
import cyder.enums.Direction;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.frame.CyderFrame;
import cyder.utils.UiUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Utilities to animate components.
 */
public final class AnimationUtil {
    /**
     * The amount of nanoseconds to sleep by when performing a close
     * animation on a {@link Frame} via {@link #closeAnimation(Frame)}.
     */
    private static final int CLOSE_ANIMATION_TIMEOUT_NS = 500;

    /**
     * The amount of display pixels to decrement by when performing a close animation.
     */
    private static final int CLOSE_ANIMATION_INC = 15;

    /**
     * The amount of nanoseconds to sleep by when performing a minimize
     * animation on a {@link CyderFrame} via {@link #minimizeAnimation(JFrame)}.
     */
    public static final int MINIMIZE_ANIMATION_TIMEOUT_NS = 250;

    /**
     * The amount of display pixels to increment by when performing a minimize animation.
     */
    public static final int MINIMIZE_ANIMATION_INC = 15;

    /**
     * Suppress default constructor.
     */
    private AnimationUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Moves the specified frame object up until it is no longer visible then invokes dispose
     *
     * @param frame the frame object to close
     */
    public static void closeAnimation(Frame frame) {
        Preconditions.checkNotNull(frame);

        if (frame instanceof CyderFrame) {
            ((CyderFrame) frame).disableDragging();
        }

        if (UiUtil.notNullAndVisible(frame)) {
            Point point = frame.getLocationOnScreen();
            int x = (int) point.getX();
            int y = (int) point.getY();

            for (int i = y ; i >= -frame.getHeight() ; i -= CLOSE_ANIMATION_INC) {
                ThreadUtil.sleep(0, CLOSE_ANIMATION_TIMEOUT_NS);
                frame.setLocation(x, i);
            }

            frame.dispose();
        }
    }

    /**
     * Moves the specified JFrame object down until it is no longer
     * visible then sets the frame's state to {@link JFrame#ICONIFIED}.
     * This method works for anything that inherits from {@link JFrame}.
     *
     * @param frame the frame object to minimize and iconify
     */
    public static void minimizeAnimation(JFrame frame) {
        Preconditions.checkNotNull(frame);

        boolean isCyderFrame = frame instanceof CyderFrame;
        CyderFrame cyderFrameReference = null;
        if (isCyderFrame) cyderFrameReference = (CyderFrame) frame;

        boolean wasEnabled = isCyderFrame && cyderFrameReference.isDraggingEnabled();
        if (isCyderFrame) cyderFrameReference.disableDragging();

        Point point = frame.getLocationOnScreen();
        int x = (int) point.getX();
        int y = (int) point.getY();

        int monitorHeight = (int) frame.getGraphicsConfiguration().getDevice()
                .getDefaultConfiguration().getBounds().getHeight();

        for (int i = y ; i <= monitorHeight ; i += MINIMIZE_ANIMATION_INC) {
            ThreadUtil.sleep(0, MINIMIZE_ANIMATION_TIMEOUT_NS);
            frame.setLocation(x, i);
        }

        frame.setState(ConsoleConstants.FRAME_ICONIFIED);

        if (isCyderFrame && wasEnabled) {
            cyderFrameReference.enableDragging();
        }
    }

    /**
     * Animates the provided component from the starting value to the ending value
     * using the provided direction, delay, and increment.
     *
     * @param direction the direction of animation (to)
     * @param start     the starting value
     * @param end       the ending value
     * @param delay     the delay in ms
     * @param increment the increment in px
     * @param component the component
     */
    public static void animateComponentMovement(Direction direction, int start, int end,
                                                int delay, int increment, Component component) {
        Preconditions.checkNotNull(component);
        Preconditions.checkNotNull(direction);
        Preconditions.checkArgument(increment > 0);

        switch (direction) {
            case LEFT -> componentLeft(start, end, delay, increment, component);
            case RIGHT -> componentRight(start, end, delay, increment, component);
            case TOP -> componentUp(start, end, delay, increment, component);
            case BOTTOM -> componentDown(start, end, delay, increment, component);
            default -> throw new IllegalArgumentException("Invalid direction provided: " + direction);
        }
    }

    /**
     * Moves the provided component from the starting value to the
     * ending value by the increment amount, sleeping for the specified millisecond delay
     * in between increments.
     *
     * @param startY    the starting y value
     * @param endY      the ending y value
     * @param delay     the ms delay in between increments
     * @param increment the increment value
     * @param component the component to move
     */
    public static void componentUp(int startY, int endY, int delay, int increment, Component component) {
        Preconditions.checkNotNull(component);

        if (component.getY() == startY) {
            CyderThreadRunner.submit(() -> {
                for (int i = startY ; i >= endY ; i -= increment) {
                    ThreadUtil.sleep(delay);
                    component.setLocation(component.getX(), i);
                }
                component.setLocation(component.getX(), endY);
            }, "Component Up Animator, component=" + component);
        }
    }

    /**
     * Moves the provided component from the starting value to the
     * ending value by the increment amount, sleeping for the specified millisecond delay
     * in between increments.
     *
     * @param startY    the starting y value
     * @param stopY     the ending y value
     * @param delay     the ms delay in between increments
     * @param increment the increment value
     * @param component the component to move
     */
    public static void componentDown(int startY, int stopY, int delay, int increment, Component component) {
        Preconditions.checkNotNull(component);

        if (component.getY() == startY) {
            CyderThreadRunner.submit(() -> {
                for (int i = startY ; i <= stopY ; i += increment) {
                    ThreadUtil.sleep(delay);
                    component.setLocation(component.getX(), i);
                }
                component.setLocation(component.getX(), stopY);
            }, "Component Down Animator, component=" + component);
        }
    }

    /**
     * Moves the provided component from the starting value to the
     * ending value by the increment amount, sleeping for the specified millisecond delay
     * in between increments.
     *
     * @param startX    the starting x value
     * @param stopX     the ending x value
     * @param delay     the ms delay in between increments
     * @param increment the increment value
     * @param component the component to move
     */
    public static void componentLeft(int startX, int stopX, int delay, int increment, Component component) {
        Preconditions.checkNotNull(component);

        if (component.getX() == startX) {
            CyderThreadRunner.submit(() -> {
                for (int i = startX ; i >= stopX ; i -= increment) {
                    ThreadUtil.sleep(delay);
                    component.setLocation(i, component.getY());
                }
                component.setLocation(stopX, component.getY());
            }, "Component Left Animator, component=" + component);
        }
    }

    /**
     * Moves the provided component from the starting value to the
     * ending value by the increment amount, sleeping for the specified millisecond delay
     * in between increments.
     *
     * @param startX    the starting x value
     * @param stopX     the ending x value
     * @param delay     the ms delay in between increments
     * @param increment the increment value
     * @param component the component to move
     */
    public static void componentRight(int startX, int stopX, int delay, int increment, Component component) {
        Preconditions.checkNotNull(component);

        if (component.getX() == startX) {
            CyderThreadRunner.submit(() -> {
                for (int i = startX ; i <= stopX ; i += increment) {
                    ThreadUtil.sleep(delay);
                    component.setLocation(i, component.getY());
                }
                component.setLocation(stopX, component.getY());
            }, "Component Right Animator, component=" + component);
        }
    }
}
