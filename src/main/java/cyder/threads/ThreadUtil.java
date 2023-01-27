package cyder.threads;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
    public static ImmutableList<String> getDaemonThreadNames() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        Thread[] printThreads = new Thread[threadGroup.activeCount()];
        threadGroup.enumerate(printThreads);

        return ImmutableList.copyOf(Arrays.stream(printThreads)
                .map(Thread::getName).collect(Collectors.toList()));
    }

    /**
     * Returns an immutable list of the active threads.
     *
     * @return an immutable list of the active threads
     */
    public static ImmutableList<String> getThreadNames() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        Thread[] printThreads = new Thread[threadGroup.activeCount()];
        threadGroup.enumerate(printThreads);

        return ImmutableList.copyOf(Arrays.stream(printThreads)
                .filter(thread -> !thread.isDaemon())
                .map(Thread::getName).collect(Collectors.toList()));
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

        return ImmutableList.copyOf(Arrays.stream(currentThreads)
                .filter(thread -> thread != null).collect(Collectors.toList()));
    }

    /**
     * Sleeps on the currently executing thread for the provided amount of time in ms.
     * This method is intended to be used as a static helper so that a method can
     * invoke this method without having to surround with a try/catch block or type
     * out all of {@link Thread#sleep(long)}.
     *
     * @param sleepTimeMs the time to sleep for in ms
     */
    public static void sleep(long sleepTimeMs) {
        Preconditions.checkArgument(sleepTimeMs >= 0);

        try {
            Thread.sleep(sleepTimeMs);
        } catch (Exception ignored) {}
    }

    /**
     * Sleeps on the currently executing thread for the provided amount of time in ms.
     * This method is intended to be used as a static helper so that a method can
     * invoke this method without having to surround with a try/catch block or type
     * out all of {@link Thread#sleep(long)}.
     *
     * @param sleepTimeMs   the time to sleep for in ms
     * @param sleepTimeNano the time to sleep for in nano seconds
     */
    public static void sleep(long sleepTimeMs, int sleepTimeNano) {
        Preconditions.checkArgument(sleepTimeMs >= 0);
        Preconditions.checkArgument(sleepTimeNano >= 0);

        try {
            Thread.sleep(sleepTimeMs, sleepTimeNano);
        } catch (Exception ignored) {}
    }

    /**
     * Sleeps on the currently executing thread for the provided amount of time in seconds.
     * This method is intended to be used as a static helper so that a method can
     * invoke this method without having to surround with a try/catch block or type
     * out all of {@link Thread#sleep(long)}.
     *
     * @param seconds the time to sleep for in seconds
     */
    public static void sleepSeconds(long seconds) {
        Preconditions.checkArgument(seconds > 0);

        try {
            Thread.sleep(seconds * 1000);
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
        Preconditions.checkArgument(sleepTime > 0);
        Preconditions.checkArgument(checkConditionFrequency > 0);
        Preconditions.checkArgument(sleepTime > checkConditionFrequency);
        Preconditions.checkNotNull(shouldExit);

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
        Preconditions.checkArgument(sleepTime > 0);
        Preconditions.checkArgument(checkConditionFrequency > 0);
        Preconditions.checkArgument(sleepTime > checkConditionFrequency);
        Preconditions.checkNotNull(escapeCondition);

        long acc = 0;
        while (acc < sleepTime) {
            ThreadUtil.sleep(checkConditionFrequency);
            acc += checkConditionFrequency;

            if (escapeCondition.get()) {
                break;
            }
        }
    }
}
