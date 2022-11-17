package cyder.time;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
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
                    Logger.log(LogTag.WATCHDOG, "Halt detected by watchdog");

                    if (OsUtil.JAR_MODE) {
                        if (OsUtil.OPERATING_SYSTEM == OsUtil.OperatingSystem.WINDOWS) {
                            Logger.log(LogTag.WATCHDOG, "JAR_MODE detected; attempting to "
                                    + "locate jar to boostrap from");
                            bootstrap();
                        } else {
                            Logger.log(LogTag.WATCHDOG, "Operating system is not Windows, found to be "
                                    + OsUtil.OPERATING_SYSTEM + ". Thus bootstrap cannot occur");
                            OsUtil.exit(ExitCondition.WatchdogBootstrapFail);
                        }
                    } else {
                        Logger.log(LogTag.WATCHDOG, "JAR_MODE is not active thus "
                                + "no jar can be located to boostrap from; exiting Cyder");
                        OsUtil.exit(ExitCondition.WatchdogTimeout);
                    }
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

    /**
     * Generates and returns a string array for a process to execute in order to attempt a bootstrap.
     *
     * @return a string array for a process to execute in order to attempt a bootstrap
     */
    private static String[] getBootstrapProcessCommand() {
        String javawPath = JvmUtil.getCurrentJavaWExe().getAbsolutePath();
        String jarPath = JvmUtil.getCyderJarReference().getAbsolutePath();

        String shutdownHash = SecurityUtil.generateUuid();
        String resumeLogHash = SecurityUtil.generateUuid();

        return new String[]{CMD_EXE, SLASH_C, javawPath, jarPath, shutdownHash, resumeLogHash};
    }

    /**
     * Attempts to boostrap Cyder by quitting and opening a new instance.
     * The same log file will be used and resumed if the bootstrap process succeeds.
     */
    private static void bootstrap() {
        try {
            Process process = Runtime.getRuntime().exec(getBootstrapProcessCommand());
            process.waitFor();
            process.getOutputStream().close();

            // todo now need part to wait to receiving shutdown hash, can test with small program
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            OsUtil.exit(ExitCondition.WatchdogBootstrapFail);
        }
    }
}
