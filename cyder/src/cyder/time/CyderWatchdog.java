package cyder.time;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.CyderTest;
import cyder.annotations.ForReadability;
import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.PropLoader;
import cyder.threads.CyderThreadRunner;
import cyder.threads.IgnoreThread;
import cyder.threads.ThreadUtil;
import cyder.utils.JvmUtil;
import cyder.utils.OsUtil;
import cyder.utils.SecurityUtil;

import javax.swing.*;
import java.lang.management.ManagementFactory;
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
    public static final int POLL_TIMEOUT = 100;

    /**
     * The standard name of the AWT-EventQueue-0 thread.
     */
    public static final String AWT_EVENT_QUEUE_0_NAME = "AWT-EventQueue-0";

    /**
     * The actual watchdog timer to detect a halt if it is not reset by the time a certain
     * value is reached.
     */
    private static final AtomicInteger watchdogCounter = new AtomicInteger();

    /**
     * The maximum number the watchdog counter can achieve before triggering a fatal reset.
     */
    public static final int MAX_WATCHDOG_FREEZE_MS = 5000;

    /**
     * The key to get whether the watchdog should be active from the props.
     */
    private static final String ACTIVATE_WATCHDOG = "activate_watchdog";

    /**
     * Whether the watchdog has been initialized and started.
     */
    private static final AtomicBoolean watchdogInitialized = new AtomicBoolean();

    /**
     * The name of the windows shell executable.
     */
    private static final String CMD_EXE = "cmd.exe";

    /**
     * The /C command line argument.
     */
    private static final String SLASH_C = "/C";

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
     * Upon a possible freeze event, the user will be informed and prompted to exit or restart Cyder.
     * <p>
     * Note: the watchdog will only start if the prop value <b>activate_watchdog</b> exists and is set to true.
     */
    public static void initializeWatchDog() {
        Preconditions.checkState(!watchdogInitialized.get());

        if (PropLoader.propExists(ACTIVATE_WATCHDOG)
                && !PropLoader.getBoolean(ACTIVATE_WATCHDOG)) {
            Logger.log(LogTag.WATCHDOG, "Watchdog skipped");
            return;
        }

        watchdogInitialized.set(true);

        CyderThreadRunner.submit(() -> {
            while (true) {
                try {
                    ThreadUtil.sleep(INITIALIZE_TIMEOUT_MS);

                    for (Thread thread : getCurrentThreads()) {
                        // Yes, this actually can and has happened
                        if (thread == null) continue;

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
     * Returns a list of current threads.
     *
     * @return a list of current threads
     */
    private static ImmutableList<Thread> getCurrentThreads() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        Thread[] currentThreads = new Thread[group.activeCount()];
        group.enumerate(currentThreads);
        return ImmutableList.copyOf(currentThreads);
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
                if (currentCyderState != ProgramState.NORMAL) {
                    Logger.log(LogTag.WATCHDOG, "Watchdog not incremented as "
                            + "current program state is: " + currentCyderState);
                    continue;
                } else if (JvmUtil.currentInstanceLaunchedWithDebug()) {
                    Logger.log(LogTag.WATCHDOG, "Watchdog not incremented as "
                            + "current jvm session was launched using debug");
                    continue;
                }

                watchdogCounter.getAndAdd(POLL_TIMEOUT);

                int currentFreezeLength = watchdogCounter.get();

                if (currentFreezeLength > maxSessionFreezeLength.get()) {
                    Logger.log(LogTag.WATCHDOG, "Max freeze detected by watchdog: "
                            + currentFreezeLength + "ms");
                    maxSessionFreezeLength.set(currentFreezeLength);
                }

                if (watchdogCounter.get() >= MAX_WATCHDOG_FREEZE_MS) {
                    Logger.log(LogTag.WATCHDOG, "UI halt detected by watchdog;"
                            + " checking if bootstrap is possible");
                    checkIfBoostrapPossible();
                }
            }
        }, IgnoreThread.CyderWatchdog.getName());
    }

    /**
     * Attempts to reset the watchdog counter using the AWT event dispatching thread.
     * If the thread is currently blocked, the counter will not be reset.
     */
    private static void attemptWatchdogReset() {
        SwingUtilities.invokeLater(() -> watchdogCounter.set(0));
    }

    @CyderTest
    public static void test() {
        // todo if this is present use it? if not use
        ManagementFactory.getRuntimeMXBean().getInputArguments();
        ManagementFactory.getRuntimeMXBean().getClassPath();
    }

    // todo don't bootstrap if in debug mode

    /**
     * Generates and returns a string array for a process to execute in order to attempt a bootstrap.
     *
     * @return a string array for a process to execute in order to attempt a bootstrap
     */
    private static String[] getBootstrapProcessCommand() {
        // todo can we some how get ALL of the arguments IntelliJ passes and just invoke that as a command?

        String javawPath = JvmUtil.getCurrentJavaWExe().getAbsolutePath();
        String jarPath = JvmUtil.getCyderJarReference().getAbsolutePath();

        String shutdownHash = SecurityUtil.generateUuid();
        String resumeLogHash = SecurityUtil.generateUuid();

        // todo need bootstrapper manager
        // todo start server socket on a specific port, prop configurable port

        // todo use official --args for shutdown hash and resume log hash
        return new String[]{CMD_EXE, SLASH_C, "todo other stuff", shutdownHash, resumeLogHash};
    }

    /**
     * Checks for whether a boostrap can be attempted and if possible, attempts to bootstrap.
     * The following conditions must be met in order for a boostrap to be attempted:
     *
     * <ul>
     *     <li>The JVM instance was launched from a jar file</li>
     *     <li>The operating system is {@link cyder.utils.OsUtil.OperatingSystem#WINDOWS}</li>
     *     <li>The current JVM instance was not launched with JDWP args (debug mode)</li>
     * </ul>
     */
    private static void checkIfBoostrapPossible() {
        try {
            if (!OsUtil.JAR_MODE) {
                // todo probably don't want to boostrap if launched from an IDE but see what happens
                onFailedBoostrap("Cyder was not launched from a jar file");
            } else if (!OsUtil.isWindows()) {
                // todo test on Kali, Process API might act different
                onFailedBoostrap("Invalid operating system: " + OsUtil.OPERATING_SYSTEM);
            } else if (JvmUtil.currentInstanceLaunchedWithDebug()) {
                onFailedBoostrap("Current JVM was launched with JDWP args");
            } else {
                onBootstrapConditionsMet();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            onFailedBoostrap(e.getMessage());
        }
    }

    /**
     * Invokes a boostrap attempt after all of the proper conditions
     * outlined in {@link #checkIfBoostrapPossible()} are met.
     */
    private static void onBootstrapConditionsMet() {
        Logger.log(LogTag.WATCHDOG, "Boostrap conditions met");


    }

    /**
     * Logs a watchdog tagged log message with the provided reason and exits
     * with the exit condition of {@link ExitCondition#WatchdogBootstrapFail}.
     *
     * @param reason
     */
    @ForReadability
    private static void onFailedBoostrap(String reason) {
        Preconditions.checkNotNull(reason);
        Preconditions.checkArgument(!reason.isEmpty());

        Logger.log(LogTag.WATCHDOG, "Failed to boostrap: " + reason);
        OsUtil.exit(ExitCondition.WatchdogBootstrapFail);
    }
}
