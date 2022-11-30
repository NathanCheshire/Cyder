package cyder.threads;

import com.google.common.base.Preconditions;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.ui.pane.CyderOutputPane;

import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** A controlling class for {@link YoutubeUuidChecker}s. */
public final class MasterYoutubeThread {
    /** The linked JTextPane to output YouTube UUIDs to. */
    private static JTextPane outputArea;

    /** Whether any instances of helper YouTube threads are running. */
    private static boolean isActive;

    /** The linked JTextPane's semaphore object to use to block text from being appended to it during script execution. */
    private static Semaphore semaphore;

    /** Suppress default constructor. */
    private MasterYoutubeThread() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the semaphore used for the youtube thread started (this class).
     *
     * @return the semaphore associated with the linked JTextPane object
     */
    @SuppressWarnings("ProtectedMemberInFinalClass") /* YouTube thread access */
    protected static Semaphore getSemaphore() {
        return semaphore;
    }

    /** Accessor list of helper threads that actually check the UUIDs. */
    private static final ArrayList<YoutubeUuidChecker> YOUTUBE_UUID_CHECKERS = new ArrayList<>();

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

    /** Kills any instances of helper YouTube threads that are currently running. */
    public static void killAll() {
        YOUTUBE_UUID_CHECKERS.forEach(YoutubeUuidChecker::kill);
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
            Console.INSTANCE.getConsoleCyderFrame().notify("Cannot start bletchy/youtube thread"
                    + " at the same time as another instance.");
            return;
        }

        urlsChecked.set(0);
        startTime.set(System.currentTimeMillis());
        lastNotifyTime = System.currentTimeMillis();

        for (int i = 0 ; i < number ; i++) {
            YoutubeUuidChecker current = new YoutubeUuidChecker(outputArea, i);
            YOUTUBE_UUID_CHECKERS.add(current);
        }

        Console.INSTANCE.getConsoleCyderFrame().notify("Type \"stop script\" or press ctrl + c to halt");
        isActive = true;
    }

    /** The number of urls checked during the current instance of the youtube thread(s). */
    private static final AtomicInteger urlsChecked = new AtomicInteger();

    /** The last time the user was notified of the current rate. */
    private static long lastNotifyTime;

    /** The frequency to notify the user of the time remaining until all youtube uuids have been checked. */
    private static final int NOTIFY_SECOND_FREQUENCY = 60;

    /** Increments the urls checked counter. */
    public static void incrementUrlsChecked() {
        urlsChecked.getAndIncrement();

        if (System.currentTimeMillis() - lastNotifyTime > NOTIFY_SECOND_FREQUENCY * 1000) {
            notifyOfRate();
            lastNotifyTime = System.currentTimeMillis();
        }
    }

    /** The time this youtube thread session started. */
    private static final AtomicLong startTime = new AtomicLong();

    /** Notifies the user of the current calculated rate of urls checked each minute. */
    private static void notifyOfRate() {
        long timeTaken = System.currentTimeMillis() - startTime.get();
        float urlsPerMs = urlsChecked.get() / (float) timeTaken;
        float urlsPerSecond = urlsPerMs * 1000.0f;
        float urlsPerMinute = (int) urlsPerSecond * 60.f;

        Console.INSTANCE.getConsoleCyderFrame().notify(
                "Current YouTube thread rate: " + urlsPerMinute + " / min");
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
