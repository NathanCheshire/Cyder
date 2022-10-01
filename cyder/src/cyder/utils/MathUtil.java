package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import cyder.annotations.ForReadability;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

import java.awt.*;

// todo math package for the below
// todo geometry util
// todo interpolation util
// todo angle util

/**
 * General mathematical functions and methods.
 */
public final class MathUtil {
    /**
     * The number of degrees in a circle.
     */
    public static final int DEGREES_IN_CIRCLE = 360;

    /**
     * One hundred eighty degrees.
     */
    public static final int ONE_EIGHTY_DEGREES = 180;

    /**
     * The standard range of angle measurements in degree form.
     */
    public static final Range<Double> DEGREE_RANGE = Range.closedOpen(0d, (double) DEGREES_IN_CIRCLE);

    /**
     * The range for angles in degree format in the range [0, 180).
     */
    public static final Range<Double> ONE_EIGHTY_DEGREE_RANGE = Range.closedOpen(0d, (double) ONE_EIGHTY_DEGREES);

    /**
     * The standard range of angle measurements in radian form.
     */
    public static final Range<Double> RADIAN_RANGE = Range.closedOpen(0d, 2 * Math.PI);

    /**
     * Suppress default constructor.
     */
    private MathUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Finds the greatest common divisor of the provided integers.
     *
     * @param first  the first integer
     * @param second the second integer
     * @return the greatest common divisor of the provided integers
     */
    public static int gcd(int first, int second) {
        if (first < second) {
            return gcd(second, first);
        }
        if (first % second == 0) {
            return second;
        } else {
            return gcd(second, first % second);
        }
    }

    /**
     * Finds the least common multiple of the provided integers.
     *
     * @param first  the first integer
     * @param second the second integer
     * @return the least common multiple of the provided integers
     */
    public static int lcm(int first, int second) {
        return ((first * second) / gcd(first, second));
    }

    /**
     * Finds the lcm of the provided array.
     *
     * @param array the array to find the lcm of
     * @return the lcm of the provided array
     */
    public static int lcmArray(int[] array) {
        Preconditions.checkNotNull(array);

        return lcmArrayInner(array, 0, array.length);
    }

    /**
     * Helper method for finding the lcm of an Array.
     *
     * @param array the array to find the lcm of
     * @param start the starting index for the array
     * @param end   the ending index for the array
     * @return the lcm of the provided array
     */
    @ForReadability
    private static int lcmArrayInner(int[] array, int start, int end) {
        Preconditions.checkNotNull(array);

        if ((end - start) == 1) {
            return lcm(array[start], array[end - 1]);
        } else {
            return lcm(array[start], lcmArrayInner(array, start + 1, end));
        }
    }

    /**
     * Linearly interpolates between v0 and v1 for the provided t value in the range [0,1].
     *
     * @param v0 the first value
     * @param v1 the second value
     * @param t  the t value in the range [0,1]
     * @return the linearly interpolated value
     */
    public static float linearlyInterpolate(float v0, float v1, float t) {
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
        Preconditions.checkNotNull(point);

        deg = normalizeAngle360(deg);
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
     * Finds the minimum of the provided integer array.
     *
     * @param ints the array of ints
     * @return the minimum integer value found
     */
    public static int min(int... ints) {
        int min = Integer.MAX_VALUE;

        for (int i : ints) {
            if (i < min) {
                min = i;
            }
        }

        return min;
    }

    /**
     * Converts the angle in degrees to standard form of being in the range [0, 360).
     *
     * @param angle the angle in degrees
     * @return the angle in standard form with rotations removed
     */
    public static int normalizeAngle360(int angle) {
        angle = angle % DEGREES_IN_CIRCLE;
        if (angle < 0) angle += DEGREES_IN_CIRCLE;
        return angle;
    }

    /**
     * Converts the angle in degrees to standard form meaning in the range [0, 360).
     *
     * @param angle the angle in degrees
     * @return the angle in standard form with rotations removed
     */
    public static double normalizeAngle360(double angle) {
        angle = angle % DEGREES_IN_CIRCLE;
        if (angle < 0) angle += DEGREES_IN_CIRCLE;
        return angle;
    }

    /**
     * Normalizes the provided angle to be in the range [0, 180).
     *
     * @param angle the angle to normalize to the range [0, 180)
     * @return the normalized angle
     */
    public static double normalizeAngle180(double angle) {
        angle = angle % ONE_EIGHTY_DEGREES;
        if (angle < 0) angle += ONE_EIGHTY_DEGREES;
        return angle;
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

    /**
     * Maps the provided value in the original range to the second range.
     *
     * @param value the value to map
     * @param low1  the min value of the original range
     * @param high1 the max value of the original range
     * @param low2  the min value of the new range
     * @param high2 the max value of the new range
     * @return the mapped value
     */
    public static double rangeMap(double value, double low1, double high1, double low2, double high2) {
        return linearlyInterpolate(low2, high2, (value - low1) / (high1 - low1));
    }

    /**
     * Linearly interpolates between val1 and val2 where amt is the amount to interpolate between the two values.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @param amt    the alpha value
     * @return the linear interpolation
     */
    public static double linearlyInterpolate(double value1, double value2, double amt) {
        return ((value2 - value1) * amt) + value1;
    }
}
