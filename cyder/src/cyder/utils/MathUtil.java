package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

import java.awt.*;

/**
 * General mathematical functions and methods.
 */
public class MathUtil {
    /**
     * Suppress default constructor.
     */
    private MathUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Finds the greatest common divisor of the provided integers
     *
     * @param a the first integer
     * @param b the second integer
     * @return the greatest common divisor of the provided integers
     */
    public static int gcd(int a, int b) {
        if (a < b)
            return gcd(b, a);
        if (a % b == 0)
            return b;
        else
            return gcd(b, a % b);
    }

    /**
     * Finds the least common multiple of the provided integers
     *
     * @param a the first integer
     * @param b the second integer
     * @return the least common multiple of the provided integers
     */
    public static int lcm(int a, int b) {
        return ((a * b) / gcd(a, b));
    }

    /**
     * Finds the lcm of the provided array.
     *
     * @param arr the array to find the lcm of
     * @return the lcm of the provided array
     */
    public static int lcmArray(int[] arr) {
        return lcmArrayInner(arr, 0, arr.length);
    }

    /**
     * Helper method for finding the lcm of an Array
     *
     * @param arr   the array to find the lcm of
     * @param start the starting index for the array
     * @param end   the ending index for the array
     * @return the lcm of the provided array
     */
    private static int lcmArrayInner(int[] arr, int start, int end) {
        if ((end - start) == 1) {
            return lcm(arr[start], arr[end - 1]);
        } else {
            return lcm(arr[start], lcmArrayInner(arr, start + 1, end));
        }
    }

    /**
     * Linearly interpolates between v0 and v1 for the provided t value in the range [0,1]
     *
     * @param v0 the first value
     * @param v1 the second value
     * @param t  the t value in the range [0,1]
     * @return the linearly interpolated value
     */
    public static float linearInterpolate(float v0, float v1, float t) {
        return (1 - t) * v0 + t * v1;
    }

    /**
     * Rotates the provided point by deg degrees in euclidean space.
     *
     * @param point the point to rotate
     * @param deg   the degrees to rotate the point by, counter-clockwise
     * @return the new point
     */
    public static Point rotatePoint(Point point, double deg) {
        double rad = Math.toRadians(deg);
        double sinRad = Math.sin(rad);
        double cosRad = Math.cos(rad);
        return new Point((int) (point.x * cosRad - point.y * sinRad), (int) (point.x * sinRad + point.y * cosRad));
    }

    /**
     * Determines if the rectangles intersect i.e. overlap each other.
     *
     * @param r1 the first rectangle
     * @param r2 the second rectangle
     * @return whether the rectangles intersect each other
     */
    public static boolean rectanglesOverlap(Rectangle r1, Rectangle r2) {
        return r2.x < r1.x + r1.width
                && r2.x + r2.width > r1.x
                && r2.y < r1.y + r1.height
                && r2.y + r2.height > r1.y;
    }

    /**
     * Finds the minimum of the provided integer array.
     *
     * @param ints the array of ints
     * @return the minimum integer value found
     */
    public static int min(int... ints) {
        int min = Integer.MAX_VALUE;

        for (int i : ints)
            if (i < min) {
                min = i;
            }

        return min;
    }

    /**
     * The number of degrees in a circle.
     */
    public static final int DEGREES_IN_CIRCLE = 360;

    /**
     * Converts the angle in degrees to standard form of being in the range [0, 360).
     *
     * @param angle the angle in degrees
     * @return the angle in standard form with rotations removed
     */
    public static int convertAngleToStdForm(int angle) {
        angle = angle % DEGREES_IN_CIRCLE;

        if (angle < 0) {
            angle += DEGREES_IN_CIRCLE;
        }

        return angle;
    }

    /**
     * Converts the angle in degrees to standard form meaning in the range [0, 360).
     *
     * @param angle the angle in degrees
     * @return the angle in standard form with rotations removed
     */
    public static double convertAngleToStdForm(double angle) {
        angle = angle % DEGREES_IN_CIRCLE;

        if (angle < 0) {
            angle += DEGREES_IN_CIRCLE;
        }

        return angle;
    }

    /**
     * Returns whether the provided point is inside of or on the rectangle.
     *
     * @param point      the point of interest
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
     * @param point      the point of interest
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
     * @param point the point of interest
     * @param bounds the rectangle to test for the point being on
     * @return whether the provided point is on the rectangle
     */
    public static boolean pointOnRectangle(Point point, Rectangle bounds) {
        Preconditions.checkNotNull(point);
        Preconditions.checkNotNull(bounds);

       return pointOnTopOfRectangle(point,bounds)
               || pointOnBottomOfRectangle(point,bounds)
               || pointOnLeftOfRectangle(point,bounds)
               || pointOnRightOfRectangle(point,bounds);
    }

    /**
     * Returns whether the provided point is on the top line of the rectangle.
     *
     * @param point the point of interest
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
     * @param point the point of interest
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
     * @param point the point of interest
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
     * @param point the point of interest
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
