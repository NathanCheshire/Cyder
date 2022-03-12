package cyder.threads;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.ui.ConsoleFrame;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class MasterYoutubeThread {
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
     * Restrict default instantiation.
     */
    private MasterYoutubeThread() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Protected getter for semaphore so helper threads may acquire when needed.
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
     * @param _outputArea the JTextPane to use for appending text to
     * @param _semaphore the semaphore to use to block other text from being appended while thread is underway
     */
    public static void initialize(JTextPane _outputArea, Semaphore _semaphore) {
        outputArea = _outputArea;
        semaphore = _semaphore;
    }

    /**
     * Kills any instances of helper YouTube threads that are currently running.
     */
    public static void killAll() {
        for (YoutubeThread ytt : youtubeThreads)
            ytt.kill();

        isActive = false;
    }

    /**
     * Starts the provided number of YouTube helper threads to check UUIDs.
     *
     * @param number the number of threads to start
     */
    public static void start(int number) {
        if (number < 0)
            throw new IllegalArgumentException("Provided number of threads to start is invalid: " + number);
        if (outputArea == null)
            throw new IllegalStateException("OutputArea not yet linked");
        if (semaphore == null)
            throw new IllegalStateException("Semaphore is not yet linked");

        //if this is running or a bletchy
        if (BletchyThread.isActive() || isActive()) {
            ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().notify("Cannot start bletchy/youtube thread" +
                    " at the same time as another instance.");
            return;
        }

        //initialize and add threads to list
        for (int i = 0; i < number; i++) {
            YoutubeThread current = new YoutubeThread(outputArea, i);
            youtubeThreads.add(current);
        }

        // say how to stop scripts
        ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().notify("Type \"stopscript\" or press ctrl + c to stop the YouTube thread.");
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

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
