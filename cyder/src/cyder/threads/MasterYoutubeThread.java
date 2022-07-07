package cyder.threads;

import com.google.common.base.Preconditions;
import cyder.console.ConsoleFrame;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.ui.CyderOutputPane;

import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public final class MasterYoutubeThread {
    /**
     * The linked JTextPane to output YouTube UUIDs to.
     */
    private static JTextPane outputArea;

    /**
     * Whether any instances of helper YouTube threads are running.
     */
    private static boolean isActive;

    /**
     * The linked JTextPane's semaphore object to use to block text from being appended to it during script execution.
     */
    private static Semaphore semaphore;

    /**
     * Suppress default constructor.
     */
    private MasterYoutubeThread() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the semaphore used for the youtube thread started (this class).
     *
     * @return the semaphore associated with the linked JTextPane object
     */
    protected static Semaphore getSemaphore() {
        return semaphore;
    }

    /**
     * Accessor list of helper threads that actually check the UUIDs.
     */
    private static final ArrayList<YoutubeThread> youtubeThreads = new ArrayList<>();

    /**
     * Sets the master YouTube JTextPane, and its linked semaphore.
     *
     * @param outputPane the output pane to use for appending text to and to acquire the locking semaphore
     */
    public static void initialize(CyderOutputPane outputPane) {
        Preconditions.checkNotNull(outputPane);

        MasterYoutubeThread.outputArea = outputPane.getJTextPane();
        MasterYoutubeThread.semaphore = outputPane.getSemaphore();
    }

    /**
     * Kills any instances of helper YouTube threads that are currently running.
     */
    public static void killAll() {
        for (YoutubeThread ytt : youtubeThreads) {
            ytt.kill();
        }

        isActive = false;
    }

    /**
     * Starts the provided number of YouTube helper threads to check UUIDs.
     *
     * @param number the number of threads to start
     */
    public static void start(int number) {
        Preconditions.checkArgument(number > 0);
        Preconditions.checkNotNull(outputArea);
        Preconditions.checkNotNull(semaphore);

        if (BletchyThread.isActive() || isActive()) {
            ConsoleFrame.INSTANCE.getConsoleCyderFrame().notify("Cannot start bletchy/youtube thread" +
                    " at the same time as another instance.");
            return;
        }

        for (int i = 0 ; i < number ; i++) {
            YoutubeThread current = new YoutubeThread(outputArea, i);
            youtubeThreads.add(current);
        }

        ConsoleFrame.INSTANCE.getConsoleCyderFrame().notify(
                "Type \"stopscript\" or press ctrl + c to stop the YouTube thread.");
        isActive = true;
    }

    /**
     * Returns whether this instance of the YouTube script is running.
     *
     * @return whether this instance of the YouTube script is running
     */
    public static boolean isActive() {
        return isActive;
    }
}
