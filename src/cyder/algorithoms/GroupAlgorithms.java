package cyder.algorithoms;

import cyder.consts.CyderStrings;

public class GroupAlgorithms {

    private GroupAlgorithms() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    } //no instantiation

    public static int gcd(int a, int b) {
        if (a < b)
            return gcd(b, a);
        if (a % b == 0)
            return b;
        else
            return gcd(b, a % b);
    }

    public static int lcm(int a, int b) {
        return ((a * b) / gcd(a, b));
    }

    public static int lcmArray(int[] arr) {
        return lcmArrayInner(arr, 0, arr.length);
    }

    protected static int lcmArrayInner(int[] arr, int start, int end) {
        if ((end - start) == 1)
            return lcm(arr[start], arr[end - 1]);
        else
            return lcm(arr[start], lcmArrayInner(arr, start + 1, end));
    }

    //Precise method, which guarantees v = v1 when t = 1.
    // This method is monotonic only when v0 * v1 < 0.
    // Lerping between same values might not produce the same value
    public static float lerp(float v0, float v1, float t) {
        return (1 - t) * v0 + t * v1;
    }

    @Override
    public String toString() {
        return "GroupTheory object, hash=" + this.hashCode();
    }
}
