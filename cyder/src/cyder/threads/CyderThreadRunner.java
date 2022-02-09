package cyder.threads;

/**
 * A class used to submit runnables and executors.
 */
public final class CyderThreadRunner {
    //todo use this for all thread invokes so that we can log them

    /**
     * Immediately starts a thread with the provided
     * runnable named with the provided name.
     *
     * @param runnable the runnable to attach to the created thread
     * @param name the name of the created thread
     */
    public static void submit(Runnable runnable, String name) {
        new Thread(runnable, name).start();
    }
}
