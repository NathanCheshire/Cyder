package cyder.watchdog;

import com.google.common.base.Preconditions;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.meta.ProgramState;
import cyder.meta.ProgramStateManager;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadRunner;
import cyder.threads.IgnoreThread;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import cyder.utils.JvmUtil;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A watchdog timer for Cyder to detect a freeze on the GUI and kill the application.
 */
public final class CyderWatchdog {
    /**
     * The time in ms to wait between checking for the first appearance of AWT-EventQueue-0.
     */
    public static final int INITIALIZE_TIMEOUT_MS = 3000;

    /**
     * The time in ms to wait between checking the AWT-EventQueue-0 thread for its status.
     */
    public static final int POLL_TIMEOUT = Props.watchdogPollTimeout.getValue();

    /**
     * The watchdog counter to detect a halt if it is not reset by the time
     * {@link #MAX_WATCHDOG_FREEZE_MS} value is reached.
     */
    private static final AtomicInteger watchdogCounter = new AtomicInteger();

    /**
     * The maximum number the watchdog counter can achieve before triggering a fatal reset.
     */
    public static final int MAX_WATCHDOG_FREEZE_MS = 5000;

    /**
     * Whether the watchdog has been initialized and started.
     */
    private static final AtomicBoolean watchdogInitialized = new AtomicBoolean();

    /**
     * The previous state of the awt event queue thread.
     */
    private static Thread.State currentAwtEventQueueThreadState;

    /**
     * Suppress default constructor.
     */
    private CyderWatchdog() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the current state of the awt event queue thread.
     *
     * @return the current state of the awt event queue thread
     */
    public static Thread.State getCurrentAwtEventQueueThreadState() {
        return currentAwtEventQueueThreadState;
    }

    /**
     * Waits for the AWT-EventQueue-0 thread to spawn and then polls the thread's state
     * every {@link CyderWatchdog#POLL_TIMEOUT} checking to ensure the thread is not frozen.
     * Upon a possible freeze event, the system will exit and attempt to bootstrap if possible.
     * Note: the Watchdog will only start if the prop value <b>activate_watchdog</b> exists and is set to true.
     */
    public static void initializeWatchDog() {
        Preconditions.checkState(!watchdogInitialized.get());

        if (!Props.activateWatchdog.getValue()) {
            Logger.log(LogTag.WATCHDOG, "Watchdog deactivated from props");
            return;
        } else if (JvmUtil.currentInstanceLaunchedWithDebug()) {
            Logger.log(LogTag.WATCHDOG, "Watchdog skipped as current JVM session was launched using debug");
            return;
        }

        watchdogInitialized.set(true);

        CyderThreadRunner.submit(() -> {
            while (true) {
                try {
                    ThreadUtil.sleep(INITIALIZE_TIMEOUT_MS);

                    for (Thread thread : ThreadUtil.getCurrentThreads()) {
                        if (thread.getName().equals(IgnoreThread.AwtEventQueue0.getName())) {
                            startWatchDog(thread);
                            return;
                        }
                    }
                } catch (Exception e) {
                    Logger.log(LogTag.WATCHDOG, ExceptionHandler.getPrintableException(e));
                }
            }
        }, IgnoreThread.WatchdogInitializer.getName());
    }

    /**
     * Returns whether the watchdog counter should be incremented if the watch thread is in a particular state.
     *
     * @param threadState the state
     * @return whether the watchdog counter should be incremented if the watch thread is in a particular state
     */
    public static boolean shouldIncrementWatchdogForThreadState(Thread.State threadState) {
        Preconditions.checkNotNull(threadState);

        return switch (threadState) {
            case NEW, WAITING, TERMINATED -> false;
            case RUNNABLE, BLOCKED, TIMED_WAITING -> true;
        };
    }

    /**
     * Starts the watchdog checker after the AWT-EventQueue-0 thread has been started.
     *
     * @param awtEventQueueThread the AWT-EventQueue-0 thread
     * @throws IllegalArgumentException if the provided thread is not the {@link IgnoreThread#AwtEventQueue0} thread
     */
    private static void startWatchDog(Thread awtEventQueueThread) {
        Preconditions.checkArgument(awtEventQueueThread.getName().equals(IgnoreThread.AwtEventQueue0.getName()));

        AtomicInteger maxSessionFreezeLength = new AtomicInteger();

        currentAwtEventQueueThreadState = awtEventQueueThread.getState();

        CyderThreadRunner.submit(() -> {
            while (true) {
                ThreadUtil.sleep(POLL_TIMEOUT);

                attemptWatchdogReset();

                currentAwtEventQueueThreadState = awtEventQueueThread.getState();

                ProgramState currentCyderState = ProgramStateManager.INSTANCE.getCurrentProgramState();

                if (currentCyderState.isShouldIncrementWatchdog()) {
                    if (shouldIncrementWatchdogForThreadState(currentAwtEventQueueThreadState)) {
                        watchdogCounter.getAndAdd(POLL_TIMEOUT);
                    }
                } else {
                    Logger.log(LogTag.WATCHDOG, "Watchdog not incremented as"
                            + " Cyder program state is: " + currentCyderState);
                }

                int currentFreezeLength = watchdogCounter.get();

                if (currentFreezeLength > maxSessionFreezeLength.get()) {
                    Logger.log(LogTag.WATCHDOG, "New max freeze detected by watchdog: "
                            + currentFreezeLength + TimeUtil.MILLISECOND_ABBREVIATION);
                    maxSessionFreezeLength.set(currentFreezeLength);
                }

                if (watchdogCounter.get() >= MAX_WATCHDOG_FREEZE_MS) {
                    onUiHaltDetected();
                    break;
                }
            }
        }, IgnoreThread.CyderWatchdog.getName());
    }

    /**
     * The actions to invoke when a UI halt is detected by the watchdog.
     */
    private static void onUiHaltDetected() {
        Logger.log(LogTag.WATCHDOG, "UI halt detected by watchdog; checking if bootstrap is possible");
        BoostrapUtil.invokeBoostrapIfConditionsMet();
    }

    /**
     * Attempts to reset the watchdog counter using the AWT event dispatching thread.
     * If the thread is currently blocked, the counter will not be reset.
     */
    private static void attemptWatchdogReset() {
        SwingUtilities.invokeLater(() -> watchdogCounter.set(0));
    }
}
