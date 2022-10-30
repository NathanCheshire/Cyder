package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

/**
 * Utilities related to arrays.
 */
public final class ArrayUtil {
    /**
     * Suppress default constructor.
     */
    private ArrayUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the last element of the provided array.
     *
     * @param arr the array
     * @param <T> the type of element contained in the array
     * @return the last element of the array.
     */
    public static <T> T getLastElement(T[] arr) {
        Preconditions.checkNotNull(arr);
        Preconditions.checkArgument(arr.length > 0);

        return arr[arr.length - 1];
    }
}
