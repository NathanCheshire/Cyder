package cyder.threads;

import com.google.common.base.Preconditions;
import cyder.console.Console;
import cyder.handlers.internal.ExceptionHandler;
import cyder.time.TimeUtil;
import cyder.ui.pane.CyderOutputPane;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

/**
 * A manager for {@link YoutubeUuidChecker}s.
 */
public enum YoutubeUuidCheckerManager {
    /**
     * The YouTube UUID checker manager instance.
     */
    INSTANCE;

    /**
     * The linked {@link CyderOutputPane}.
     */
    private CyderOutputPane outputPane;

    /**
     * Whether any instances of helper YouTube threads are running.
     */
    private boolean isActive;

    /**
     * The number of urls checked during the current instance of the youtube thread(s).
     */
    private final AtomicInteger urlsChecked = new AtomicInteger();

    /**
     * The last time the user was notified of the current rate.
     */
    private long lastNotifyTime;

    /**
     * The frequency in seconds to notify the user of the time remaining until all youtube uuids have been checked.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final int notifyUserOfRateFrequency = 60;

    /**
     * The list of active YouTube uuid checkers.
     */
    private final ArrayList<YoutubeUuidChecker> youtubeUuidCheckers = new ArrayList<>();

    /**
     * The time this youtube thread session started.
     */
    private final AtomicLong startTime = new AtomicLong();

    /**
     * Attempts to acquire the output pane's semaphore.
     *
     * @return whether the lock was acquired
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean acquireLock() {
        try {
            outputPane.getSemaphore().acquire();
            return true;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            return false;
        }
    }

    /**
     * Releases the output pane's semaphore.
     */
    public void releaseLock() {
        outputPane.getSemaphore().release();
    }

    /**
     * Returns the number of active uuid checkers.
     *
     * @return the number of active uuid checkers
     */
    public int getActiveUuidCheckersLength() {
        return youtubeUuidCheckers.size();
    }

    /**
     * Whether the Youtube UUID checker manager has been initialized.
     */
    private final AtomicBoolean initialized = new AtomicBoolean();

    /**
     * Sets the master YouTube JTextPane, and its linked semaphore.
     *
     * @param outputPane the output pane to use for appending text to and to acquire the locking semaphore
     */
    public void initialize(CyderOutputPane outputPane) {
        Preconditions.checkNotNull(outputPane);
        Preconditions.checkArgument(!initialized.get());

        this.outputPane = outputPane;
    }

    /**
     * Kills any instances of helper YouTube threads that are currently running.
     */
    public void killAll() {
        youtubeUuidCheckers.forEach(YoutubeUuidChecker::kill);
        youtubeUuidCheckers.clear();

        isActive = false;
    }

    /**
     * Starts the provided number of YouTube helper threads to check UUIDs.
     *
     * @param number the number of threads to start
     */
    public void start(int number) {
        Preconditions.checkArgument(number > 0);
        Preconditions.checkState(!initialized.get());

        if (BletchyThread.isActive() || hasActiveCheckers()) {
            Console.INSTANCE.getConsoleCyderFrame().notify(
                    "Cannot start bletchy/youtube thread at the same time as another instance");
            return;
        }

        checkIfStartingFirstThreads();

        IntStream.range(0, number).forEach(i -> youtubeUuidCheckers.add(new YoutubeUuidChecker(outputPane)));

        Console.INSTANCE.getConsoleCyderFrame().notify("Type \"stop script\" or press ctrl + c to halt");

        isActive = true;
    }

    /**
     * Checks the size of the youtube uuid checkers list of active checkers. If empty,
     * resets the variables used to notify the user of the estimated time to completion.
     */
    private void checkIfStartingFirstThreads() {
        if (youtubeUuidCheckers.isEmpty()) {
            urlsChecked.set(0);
            startTime.set(System.currentTimeMillis());
            lastNotifyTime = System.currentTimeMillis();
        }
    }

    /**
     * Increments the urls checked counter.
     */
    public void incrementUrlsChecked() {
        urlsChecked.getAndIncrement();
        checkIfShouldNotifyOfRate();
    }

    /**
     * Returns whether one or more {@link YoutubeUuidChecker} are active.
     *
     * @return whether one or more {@link YoutubeUuidChecker} are active
     */
    public boolean hasActiveCheckers() {
        return isActive;
    }

    /**
     * Checks for whether the user should be notified of the current uuid check rate.
     */
    private void checkIfShouldNotifyOfRate() {
        if (System.currentTimeMillis() - lastNotifyTime > notifyUserOfRateFrequency * TimeUtil.SECONDS_IN_MINUTE) {
            notifyOfRate();
            lastNotifyTime = System.currentTimeMillis();
        }
    }

    /**
     * Notifies the user of the current calculated rate of urls checked each minute.
     */
    private void notifyOfRate() {
        long timeTaken = System.currentTimeMillis() - startTime.get();
        float urlsPerMs = urlsChecked.get() / (float) timeTaken;
        float urlsPerSecond = urlsPerMs * 1000.0f;
        float urlsPerMinute = (int) urlsPerSecond * 60.f;

        Console.INSTANCE.getConsoleCyderFrame().notify(
                "Current YouTube thread rate: " + urlsPerMinute + " / min");
    }
}
