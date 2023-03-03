package cyder.animation;

import com.google.common.base.Preconditions;
import cyder.enumerations.Direction;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;

import java.awt.*;

/**
 * Utilities to animate components.
 */
public final class AnimationUtil {
    /**
     * Suppress default constructor.
     */
    private AnimationUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
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
     * @param endY      the ending y value
     * @param delay     the ms delay in between increments
     * @param increment the increment value
     * @param component the component to move
     */
    public static void componentDown(int startY, int endY, int delay, int increment, Component component) {
        Preconditions.checkNotNull(component);

        if (component.getY() == startY) {
            CyderThreadRunner.submit(() -> {
                for (int i = startY ; i <= endY ; i += increment) {
                    ThreadUtil.sleep(delay);
                    component.setLocation(component.getX(), i);
                }
                component.setLocation(component.getX(), endY);
            }, "Component Down Animator, component=" + component);
        }
    }

    /**
     * Moves the provided component from the starting value to the
     * ending value by the increment amount, sleeping for the specified millisecond delay
     * in between increments.
     *
     * @param startX    the starting x value
     * @param endX      the ending x value
     * @param delay     the ms delay in between increments
     * @param increment the increment value
     * @param component the component to move
     */
    public static void componentLeft(int startX, int endX, int delay, int increment, Component component) {
        Preconditions.checkNotNull(component);

        if (component.getX() == startX) {
            CyderThreadRunner.submit(() -> {
                for (int i = startX ; i >= endX ; i -= increment) {
                    ThreadUtil.sleep(delay);
                    component.setLocation(i, component.getY());
                }
                component.setLocation(endX, component.getY());
            }, "Component Left Animator, component=" + component);
        }
    }

    /**
     * Moves the provided component from the starting value to the
     * ending value by the increment amount, sleeping for the specified millisecond delay
     * in between increments.
     *
     * @param startX    the starting x value
     * @param endX      the ending x value
     * @param delay     the ms delay in between increments
     * @param increment the increment value
     * @param component the component to move
     */
    public static void componentRight(int startX, int endX, int delay, int increment, Component component) {
        Preconditions.checkNotNull(component);

        if (component.getX() == startX) {
            CyderThreadRunner.submit(() -> {
                for (int i = startX ; i <= endX ; i += increment) {
                    ThreadUtil.sleep(delay);
                    component.setLocation(i, component.getY());
                }
                component.setLocation(endX, component.getY());
            }, "Component Right Animator, component=" + component);
        }
    }
}
