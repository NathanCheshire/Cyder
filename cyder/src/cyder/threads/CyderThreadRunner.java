package cyder.threads;

import cyder.handlers.internal.Logger;

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

    //todo add in executor services
}
