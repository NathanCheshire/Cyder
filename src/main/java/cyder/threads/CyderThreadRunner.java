package cyder.threads;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import cyder.audio.GeneralAudioPlayer;
import cyder.exceptions.IllegalMethodException;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.CyderStrings;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class used to submit runnables and executors.
 */
public final class CyderThreadRunner {
    /**
     * The threads ran for this session of Cyder.
     */
    private static final AtomicInteger threadsRan = new AtomicInteger();

    /**
     * Suppress default constructor.
     */
    private CyderThreadRunner() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the number of threads CyderThreadRunner has created and started.
     *
     * @return the number of threads CyderThreadRunner has created and started
     */
    public static int getThreadsRan() {
        return threadsRan.get();
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

        logThread(name);
        new Thread(runnable, name).start();
        threadsRan.incrementAndGet();
    }

    /**
     * Submits the provided supplier to be ran by a new runnable.
     *
     * @param supplier the supplier to run
     * @param name     the name of the thread to run the supplier
     * @param <T>      the type the supplier returns
     */
    public static <T> void submitSupplier(Supplier<T> supplier, String name) {
        submit(supplier::get, name);
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

    /**
     * Constructs a new thread to run the provided runnable at the provided fixed rate.
     *
     * @param runnable   the runnable to execute at the specified frequency
     * @param name       the name of the thread
     * @param frequency  the frequency to execute the runnable
     * @param shouldExit the condition to check to stop executing at the fixed rate
     */
    public static void scheduleAtFixedRate(Runnable runnable,
                                           String name,
                                           Duration frequency,
                                           AtomicBoolean shouldExit) {
        Preconditions.checkNotNull(runnable);
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.isEmpty());
        Preconditions.checkNotNull(frequency);

        String threadName = "Fixed Rate Scheduler, task=[" + name + "], rate=" + frequency;
        submit(() -> {
            while (true) {
                if (shouldExit != null && shouldExit.get()) {
                    return;
                }

                submit(runnable, name);
                ThreadUtil.sleep(frequency.toMillis());
            }
        }, threadName);
    }

    /**
     * Logs the running of the thread with the provided name.
     *
     * @param name the name of the thread being ran
     */
    private static void logThread(String name) {
        if (!GeneralAudioPlayer.isSystemAudio(new File(name))) Logger.log(LogTag.THREAD_STARTED, name);
    }
}
