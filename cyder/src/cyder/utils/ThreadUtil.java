package cyder.utils;

import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

import java.util.LinkedList;

/**
 * A utility class for getting thread names and counts.
 */
public class ThreadUtil {
    /**
     * Suppress default constructor.
     */
    private ThreadUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
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
}
