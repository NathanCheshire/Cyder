package main.java.cyder.math;

import com.google.common.base.Preconditions;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.strings.CyderStrings;

import java.awt.*;

/**
 * Utilities related to geometry.
 */
public final class GeometryUtil {
    /**
     * Suppress default constructor.
     */
    private GeometryUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Rotates the provided point by deg degrees in euclidean space.
     *
     * @param point the point to rotate
     * @param deg   the degrees to rotate the point by, counter-clockwise
     * @return the new point
     */
    public static Point rotatePoint(Point point, double deg) {
        Preconditions.checkNotNull(point);

        deg = AngleUtil.normalizeAngle360(deg);
        double rad = Math.toRadians(deg);

        double sinRad = Math.sin(rad);
        double cosRad = Math.cos(rad);

        return new Point((int) (point.x * cosRad - point.y * sinRad), (int) (point.x * sinRad + point.y * cosRad));
    }

    /**
     * Determines if the rectangles intersect i.e. overlap each other.
     *
     * @param rectangle1 the first rectangle
     * @param rectangle2 the second rectangle
     * @return whether the rectangles intersect each other
     */
    public static boolean rectanglesOverlap(Rectangle rectangle1, Rectangle rectangle2) {
        Preconditions.checkNotNull(rectangle1);
        Preconditions.checkNotNull(rectangle2);

        return rectangle2.x < rectangle1.x + rectangle1.width
                && rectangle2.x + rectangle2.width > rectangle1.x
                && rectangle2.y < rectangle1.y + rectangle1.height
                && rectangle2.y + rectangle2.height > rectangle1.y;
    }

    /**
     * Returns whether the provided point is inside of or on the rectangle.
     *
     * @param point  the point of interest
     * @param bounds the bounds to test for the point being inside of or on
     * @return whether the provided point is inside of or on the rectangle
     */
    public static boolean pointInOrOnRectangle(Point point, Rectangle bounds) {
        Preconditions.checkNotNull(point);
        Preconditions.checkNotNull(bounds);

        return pointInRectangle(point, bounds) || pointOnRectangle(point, bounds);
    }

    /**
     * Returns whether the provided point is inside of the rectangle.
     *
     * @param point  the point of interest
     * @param bounds the rectangle to test for the point being inside of
     * @return whether the provided point is inside of the rectangle
     */
    public static boolean pointInRectangle(Point point, Rectangle bounds) {
        Preconditions.checkNotNull(point);
        Preconditions.checkNotNull(bounds);

        return (point.x > bounds.getX() && point.x < bounds.x + bounds.width)
                && (point.y > bounds.getY() && point.y < bounds.y + bounds.height);
    }

    /**
     * Returns whether the provided point is on the rectangle.
     *
     * @param point  the point of interest
     * @param bounds the rectangle to test for the point being on
     * @return whether the provided point is on the rectangle
     */
    public static boolean pointOnRectangle(Point point, Rectangle bounds) {
        Preconditions.checkNotNull(point);
        Preconditions.checkNotNull(bounds);

        return pointOnTopOfRectangle(point, bounds)
                || pointOnBottomOfRectangle(point, bounds)
                || pointOnLeftOfRectangle(point, bounds)
                || pointOnRightOfRectangle(point, bounds);
    }

    /**
     * Returns whether the provided point is on the top line of the rectangle.
     *
     * @param point  the point of interest
     * @param bounds the rectangle to test for the point being on the top line of
     * @return whether the provided point is on the top line of the rectangle
     */
    public static boolean pointOnTopOfRectangle(Point point, Rectangle bounds) {
        Preconditions.checkNotNull(point);
        Preconditions.checkNotNull(bounds);

        return point.y == bounds.y  // same y value as top line
                && point.x >= bounds.x // left most point or greater
                && point.x <= bounds.x + bounds.width;  // right most point or less
    }

    /**
     * Returns whether the provided point is on the right line of the rectangle.
     *
     * @param point  the point of interest
     * @param bounds the rectangle to test for the point being on the right line of
     * @return whether the provided point is on the right line of the rectangle
     */
    public static boolean pointOnRightOfRectangle(Point point, Rectangle bounds) {
        Preconditions.checkNotNull(point);
        Preconditions.checkNotNull(bounds);

        return point.x == bounds.x + bounds.width // same x value as right line
                && point.y >= bounds.y // top most point
                && point.y <= bounds.y + bounds.height; // bottom most point
    }

    /**
     * Returns whether the provided point is on the left line of the rectangle.
     *
     * @param point  the point of interest
     * @param bounds the rectangle to test for the point being on the left line of
     * @return whether the provided point is on the left line of the rectangle
     */
    public static boolean pointOnLeftOfRectangle(Point point, Rectangle bounds) {
        Preconditions.checkNotNull(point);
        Preconditions.checkNotNull(bounds);

        return point.x == bounds.x // same x value as left line
                && point.y >= bounds.y // top most point
                && point.y <= bounds.y + bounds.height;  // bottom most point
    }

    /**
     * Returns whether the provided point is on the bottom line of the rectangle.
     *
     * @param point  the point of interest
     * @param bounds the rectangle to test for the point being on the bottom line of
     * @return whether the provided point is on the left bottom of the rectangle
     */
    public static boolean pointOnBottomOfRectangle(Point point, Rectangle bounds) {
        Preconditions.checkNotNull(point);
        Preconditions.checkNotNull(bounds);

        return point.y == bounds.y + bounds.height  // same y value as bottom line
                && point.x >= bounds.x // left most point or greater
                && point.x <= bounds.x + bounds.width;  // right most point or less
    }
}
