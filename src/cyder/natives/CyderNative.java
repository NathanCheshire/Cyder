package cyder.natives;

public class CyderNative {
    static {
        System.loadLibrary("CyderNative");
    }

    public static native void runNatives();
}
