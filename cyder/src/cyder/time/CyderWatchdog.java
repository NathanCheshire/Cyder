package cyder.time;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.annotations.ForReadability;
import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.Props;
import cyder.threads.CyderThreadRunner;
import cyder.threads.IgnoreThread;
import cyder.threads.ThreadUtil;
import cyder.utils.JvmUtil;
import cyder.utils.OsUtil;

import javax.swing.*;
import java.io.IOException;
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
     * The standard name of the AWT-EventQueue-0 thread.
     */
    public static final String AWT_EVENT_QUEUE_0_NAME = "AWT-EventQueue-0";

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
                        if (thread.getName().equals(AWT_EVENT_QUEUE_0_NAME)) {
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
     * @throws IllegalArgumentException if the provided thread
     *                                  is not named {@link CyderWatchdog#AWT_EVENT_QUEUE_0_NAME}
     */
    private static void startWatchDog(Thread awtEventQueueThread) {
        Preconditions.checkArgument(awtEventQueueThread.getName().equals(AWT_EVENT_QUEUE_0_NAME));

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
        invokeBoostrapIfConditionsMet();
    }

    /**
     * Attempts to reset the watchdog counter using the AWT event dispatching thread.
     * If the thread is currently blocked, the counter will not be reset.
     */
    private static void attemptWatchdogReset() {
        SwingUtilities.invokeLater(() -> watchdogCounter.set(0));
    }

    /**
     * Checks for whether a boostrap can be attempted and if possible, attempts to bootstrap.
     * The following conditions must be met in order for a boostrap to be attempted:
     *
     * <ul>
     *     <li>The operating system is {@link cyder.utils.OsUtil.OperatingSystem#WINDOWS}</li>
     *     <li>The attempt_boostrap prop is true if present</li>
     *     <li>The current JVM instance was not launched with JDWP args (debug mode)</li>
     * </ul>
     *
     * @return whether a bootstrap was possible and invoked
     */
    @CanIgnoreReturnValue
    private static boolean invokeBoostrapIfConditionsMet() {
        try {
            if (!OsUtil.isWindows()) {
                onFailedBoostrap("Invalid operating system: " + OsUtil.OPERATING_SYSTEM);
            } else if (JvmUtil.currentInstanceLaunchedWithDebug()) {
                onFailedBoostrap("Current JVM was launched with JDWP args");
            } else if (!Props.attemptBootstrap.getValue()) {
                onFailedBoostrap("attempt_boostrap prop set to false");
            } else {
                Logger.log(LogTag.WATCHDOG, "Boostrap conditions met");
                bootstrap();
                return true;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            onFailedBoostrap(e.getMessage());
        }

        return false;
    }

    // todo need handling of this, Logger needs to be re-done
    /**
     * The log file argument when starting an instance of Cyder.
     */
    private static final String LOG_FILE_ARGUMENT = "--log-file";

    /**
     * Invokes a boostrap attempt. This will request this instance of
     * Cyder to shutdown after the new instance starts.
     */
    private static void bootstrap() {
        ImmutableList<String> command = ImmutableList.of(
                JvmUtil.getFullJvmInvocationCommand(),
                LOG_FILE_ARGUMENT,
                Logger.getCurrentLogFile().getAbsolutePath()
        );

        try {
            // todo remove when ready to test
            if (false) OsUtil.executeShellCommand(command);

            // todo send and be done, new client should request to end this session and we should comply
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        // todo extract bootstrap methods out of Watchdog and move to Bootstrapper.java
    }

    // todo start writing to resume log file if present, insert bootstrap into it and then a debug call
    //  or actually bootstrap log tag and say bootstrap successful, if log file couldn't be used log that too

    // todo need to validate key props on start too? sufficient subroutine for that with a key validator util?
    // todo key util with validation and getter methods?

    // todo Prop class <T> of some type such as boolean, hold default value,
    // instead of get(String string) we'll accept this prop class and have overloaded methods for return type
    // if not exist in props, we return the default value

    /**
     * Logs a watchdog tagged log message with the provided reason and exits
     * with the exit condition of {@link ExitCondition#WatchdogBootstrapFail}.
     *
     * @param reason the reason the bootstrap  failed
     */
    @ForReadability
    private static void onFailedBoostrap(String reason) {
        Preconditions.checkNotNull(reason);
        Preconditions.checkArgument(!reason.isEmpty());

        Logger.log(LogTag.WATCHDOG, "Failed to boostrap: " + reason);
        OsUtil.exit(ExitCondition.WatchdogBootstrapFail);
    }
}
