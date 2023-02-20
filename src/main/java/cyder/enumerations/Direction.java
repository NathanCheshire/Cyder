package cyder.enumerations;

/**
 * A standard cardinal direction.
 */
public enum Direction {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM;

    /**
     * Returns whether the direction points horizontally.
     *
     * @param direction the direction
     * @return whether the direction points horizontally
     */
    public static boolean isHorizontal(Direction direction) {
        return direction == LEFT || direction == RIGHT;
    }

    /**
     * Returns whether the direction points vertically.
     *
     * @param direction the direction
     * @return whether the direction points vertically
     */
    public static boolean isVertical(Direction direction) {
        return direction == TOP || direction == BOTTOM;
    }
}
