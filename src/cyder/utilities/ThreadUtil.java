package cyder.utilities;

import cyder.consts.CyderStrings;

public class ThreadUtil {
    private ThreadUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    public static int getDaemonThreadCount() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        return threadGroup.activeCount();
    }

    public static int getThreadCount() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        return threadGroup.activeCount();
    }
}
