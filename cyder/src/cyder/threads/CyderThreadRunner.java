package cyder.threads;

import cyder.handlers.internal.Logger;

import java.util.concurrent.Executors;

/**
 * A class used to submit runnables and executors.
 */
public final class CyderThreadRunner {
    /**
     * Immediately starts a thread with the provided
     * runnable named with the provided name.
     *
     * @param runnable the runnable to attach to the created thread
     * @param name the name of the created thread
     */
    public static void submit(Runnable runnable, String name) {
        Logger.log(Logger.Tag.THREAD, name);
        new Thread(runnable, name).start();
    }

    /**
     * Creates and returns a thread with the provided properties.
     *
     * @param runnable the runnable for the thread
     * @param name the name for the thread
     * @return the thread with the provided properties
     */
    public static Thread createThread(Runnable runnable, String name) {
        return new Thread(runnable, name);
    }

    /**
     * Starts a new single thread executor service with the provided name
     * and immediately submits the provided runnable.
     *
     * @param runnable the runnable to run on the executor service
     * @param name the name of the executor servicd to use
     */
    public static void submitForExecutor(Runnable runnable, String name) {
        Executors.newSingleThreadExecutor(new CyderThreadFactory(name)).submit(runnable);
    }
}
