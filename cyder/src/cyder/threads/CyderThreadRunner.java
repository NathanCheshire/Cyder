package cyder.threads;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.Logger;

/**
 * A class used to submit runnables and executors.
 */
public final class CyderThreadRunner {
    /**
     * Restrict default instantiation.
     */
    private CyderThreadRunner() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

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
}
