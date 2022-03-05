package cyder.algorithoms;

import cyder.constants.CyderStrings;

import java.awt.*;

/**
 * General math methods
 */
public class GeneralMath {
    private GeneralMath() {
        throw new IllegalStateException(CyderStrings.attemptedInstantiation);
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
     * @param b the second interger
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
     * @param arr the array to find the lcm of
     * @param start the starting index for the array
     * @param end the ending index for the array
     * @return the lcm of the provided array
     */
    private static int lcmArrayInner(int[] arr, int start, int end) {
        if ((end - start) == 1)
            return lcm(arr[start], arr[end - 1]);
        else
            return lcm(arr[start], lcmArrayInner(arr, start + 1, end));
    }

    //Precise method, which guarantees v = v1 when t = 1.
    // This method is monotonic only when v0 * v1 < 0.
    // Lerping between same values might not produce the same value

    /**
     * Linearly interpolates between v0 and v1 for the provided t value in the range [0,1]
     *
     * @param v0 the first value
     * @param v1 the second value
     * @param t the t value in the range [0,1]
     * @return the linearly interpolated value
     */
    public static float lerp(float v0, float v1, float t) {
        return (1 - t) * v0 + t * v1;
    }

    /**
     * Rotates the provided point by rad radians in euclidean space.
     *
     * @param point the point to ratate
     * @param rad the radians to rotate the point by, counter-clockwise
     * @return the new point
     */
    public static Point rotatePoint(Point point, double rad) {
        return new Point((int) (point.x * Math.cos(rad) - point.y * Math.sin(rad)),
                         (int) (point.x * Math.sin(rad) + point.y * Math.cos(rad)));
    }
}
