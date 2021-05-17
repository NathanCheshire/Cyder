package cyder.algorithoms;

public class GroupTheory {
    private GroupTheory() {} //no instantiation

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

    public static String bezoutIdentity(int a, int b) {
        return "To be implemented";
    }

    public static int[] bezoutConstants() {
        return null; //https://planetcalc.com/8586/
    }
}
