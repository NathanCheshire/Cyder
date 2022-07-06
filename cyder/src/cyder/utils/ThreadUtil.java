package cyder.utils;

import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

import java.util.LinkedList;

/**
 * A utility class for getting thread names and counts.
 */
public final class ThreadUtil {
    /**
     * Suppress default constructor.
     */
    private ThreadUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the number of daemon threads active.
     *
     * @return the number of daemon threads active
     */
    public static int getDaemonThreadCount() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        return threadGroup.activeCount();
    }

    /**
     * Returns the number of threads active.
     *
     * @return the number of threads active
     */
    public static int getThreadCount() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        return threadGroup.activeCount();
    }

    /**
     * Returns an immutable list of the active daemon threads.
     *
     * @return an immutable list of the active daemon threads
     */
    public static ImmutableList<String> getDaemonThreads() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int num = threadGroup.activeCount();
        Thread[] printThreads = new Thread[num];
        threadGroup.enumerate(printThreads);

        LinkedList<String> ret = new LinkedList<>();

        for (int i = 0 ; i < num ; i++) {
            ret.add(printThreads[i].getName());
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns an immutable list of the active threads.
     *
     * @return an immutable list of the active threads
     */
    public static ImmutableList<String> getThreads() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int num = threadGroup.activeCount();
        Thread[] printThreads = new Thread[num];
        threadGroup.enumerate(printThreads);

        LinkedList<String> ret = new LinkedList<>();

        for (int i = 0 ; i < num ; i++) {
            if (!printThreads[i].isDaemon()) {
                ret.add(printThreads[i].getName());
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Sleeps on the currently executing thread for the provided amount of time in ms.
     * This method is intended to be used as a static import so that a method can
     * invoke this method without having to surround with a try/catch block or type
     * out all of {@link Thread#sleep(long)}.
     *
     * @param sleepTimeMs the time to sleep for in ms
     */
    public static void sleep(long sleepTimeMs) {
        try {
            Thread.sleep(sleepTimeMs);
        } catch (Exception ignored) {}
    }

    /**
     * Sleeps on the currently executing thread for the provided amount of time in ms.
     * This method is intended to be used as a static import so that a method can
     * invoke this method without having to surround with a try/catch block or type
     * out all of {@link Thread#sleep(long)}.
     *
     * @param sleepTimeMs   the time to sleep for in ms
     * @param sleepTimeNano the time to sleep for in nano seconds
     */
    public static void sleep(long sleepTimeMs, int sleepTimeNano) {
        try {
            Thread.sleep(sleepTimeMs, sleepTimeNano);
        } catch (Exception ignored) {}
    }
}
