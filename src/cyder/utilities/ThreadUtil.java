package cyder.utilities;

public class ThreadUtil {
    public static int getDaemonThreadCount() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        return threadGroup.activeCount();
    }

    public static int getThreadCount() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        return threadGroup.activeCount();
    }
}
