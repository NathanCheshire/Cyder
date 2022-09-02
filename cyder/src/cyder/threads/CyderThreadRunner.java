package cyder.threads;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.Logger;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class used to submit runnables and executors.
 */
public final class CyderThreadRunner {
    /**
     * Suppress default instantiation.
     */
    private CyderThreadRunner() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Creates and immediately starts a thread with the provided
     * runnable named with the provided name.
     *
     * @param runnable the runnable to attach to the created thread
     * @param name     the name of the created thread
     */
    public static void submit(Runnable runnable, String name) {
        Preconditions.checkNotNull(runnable);
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.isEmpty());

        Logger.log(Logger.Tag.THREAD, name);
        new Thread(runnable, name).start();
    }

    /**
     * Creates and returns a thread with the provided properties.
     *
     * @param runnable the runnable for the thread
     * @param name     the name for the thread
     * @return the thread with the provided properties
     */
    public static Thread createThread(Runnable runnable, String name) {
        Preconditions.checkNotNull(runnable);
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.isEmpty());

        return new Thread(runnable, name);
    }

    /**
     * Constructs a new thread to run the provided runnable at the provided fixed rate.
     *
     * @param runnable  the runnable to execute at the specified frequency
     * @param name      the name of the thread
     * @param frequency the frequency to execute the runnable
     */
    public static void scheduleAtFixedRate(Runnable runnable, String name, Duration frequency) {
        scheduleAtFixedRate(runnable, name, frequency, null);
    }

    private static String generateFixedRateSchedulerThreadName(String name, Duration frequency) {
        return "Fixed Rate Scheduler, task=[" + name + "], rate=" + frequency;
    }

    /**
     * Constructs a new thread to run the provided runnable at the provided fixed rate.
     *
     * @param runnable   the runnable to execute at the specified frequency
     * @param name       the name of the thread
     * @param frequency  the frequency to execute the runnable
     * @param shouldExit the condition to check to stop executing at the fixed rate
     */
    public static void scheduleAtFixedRate(Runnable runnable, String name, Duration frequency,
                                           AtomicBoolean shouldExit) {
        Preconditions.checkNotNull(runnable);
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.isEmpty());
        Preconditions.checkNotNull(frequency);

        submit(() -> {
            while (true) {
                if (shouldExit != null && shouldExit.get()) {
                    return;
                }

                submit(runnable, name);
                ThreadUtil.sleep(frequency.toMillis());
            }
        }, generateFixedRateSchedulerThreadName(name, frequency));
    }
}
