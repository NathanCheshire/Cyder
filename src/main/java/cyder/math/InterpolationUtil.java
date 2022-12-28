package cyder.math;

import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;

/**
 * Utilities related to interpolation.
 */
public final class InterpolationUtil {
    /**
     * Suppress default constructor.
     */
    private InterpolationUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Maps the provided value in the original range to the second range.
     *
     * @param value the value to map
     * @param low1  the min value of the original range
     * @param high1 the max value of the original range
     * @param low2  the min value of the new range
     * @param high2 the max value of the new range
     * @return the value mapped from the old range to the new range
     */
    public static double rangeMap(double value, double low1, double high1, double low2, double high2) {
        return linearlyInterpolate(low2, high2, (value - low1) / (high1 - low1));
    }

    /**
     * Linearly interpolates between val1 and val2 where amount is the amount to interpolate between the two values.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @param amount the amount to interpolate between the two values value.
     *               Typically this value falls in the range [0.0, 1.0]
     * @return the linearly interpolated value
     */
    private static double linearlyInterpolate(double value1, double value2, double amount) {
        return ((value2 - value1) * amount) + value1;
    }
}
