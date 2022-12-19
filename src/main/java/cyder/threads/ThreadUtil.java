package main.java.cyder.threads;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.strings.CyderStrings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * A utility class for querying threads, names, and counts.
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

    /**
     * Sleeps on the current thread for the specified amount of time,
     * checking the escapeCondition for truth every checkConditionFrequency ms.
     *
     * @param sleepTime               the total time to sleep for
     * @param checkConditionFrequency the frequency to check the escapeCondition
     * @param shouldExit              the supplier to determine whether to stop sleeping
     */
    public static void sleepWithChecks(long sleepTime, long checkConditionFrequency,
                                       Supplier<Boolean> shouldExit) {
        Preconditions.checkNotNull(shouldExit);
        Preconditions.checkArgument(sleepTime > 0);
        Preconditions.checkArgument(checkConditionFrequency > 0);
        Preconditions.checkArgument(sleepTime > checkConditionFrequency);

        long acc = 0;
        while (acc < sleepTime) {
            sleep(checkConditionFrequency);
            acc += checkConditionFrequency;

            if (shouldExit.get()) {
                break;
            }
        }
    }

    /**
     * Sleeps on the current thread for the specified amount of time,
     * checking the escapeCondition for truth every checkConditionFrequency ms.
     *
     * @param sleepTime               the total time to sleep for
     * @param checkConditionFrequency the frequency to check the escapeCondition
     * @param escapeCondition         the condition to stop sleeping if true
     */
    public static void sleepWithChecks(long sleepTime,
                                       long checkConditionFrequency,
                                       AtomicBoolean escapeCondition) {
        Preconditions.checkNotNull(escapeCondition);
        Preconditions.checkArgument(sleepTime > 0);
        Preconditions.checkArgument(checkConditionFrequency > 0);
        Preconditions.checkArgument(sleepTime > checkConditionFrequency);

        long acc = 0;
        while (acc < sleepTime) {
            ThreadUtil.sleep(checkConditionFrequency);
            acc += checkConditionFrequency;

            if (escapeCondition.get()) {
                break;
            }
        }
    }

    /**
     * Returns a list of non-null threads in the current thread group.
     * Note this method takes precautions to not return null values or null list.
     *
     * @return a list of non-null threads in the current thread group
     */
    public static ImmutableList<Thread> getCurrentThreads() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        Thread[] currentThreads = new Thread[group.activeCount()];
        group.enumerate(currentThreads);

        ArrayList<Thread> ret = new ArrayList<>();
        for (Thread thread : currentThreads) {
            if (thread != null) ret.add(thread);
        }

        return ImmutableList.copyOf(ret);
    }
}
