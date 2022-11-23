package cyder.time;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
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
import java.net.ServerSocket;
import java.net.Socket;
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
            if (!OsUtil.isWindows()) {
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

        String shutdownHash = SecurityUtil.generateUuid();
        String resumeLogHash = SecurityUtil.generateUuid();

        // todo need bootstrapper manager

        // todo use official --args for shutdown hash and resume log hash
        String[] str = new String[]{CMD_EXE, SLASH_C, "todo other stuff", shutdownHash, resumeLogHash};

        try {
            // todo need a method in process util to run a string array command and get output from
            Runtime.getRuntime().exec(new String[]{CMD_EXE, SLASH_C, JvmUtil.getFullJvmInvocationCommand()});

            int shutdownSocketPort = 8888;
            if (PropLoader.propExists("shutdown_socket_port")) {
                shutdownSocketPort = PropLoader.getInteger("shutdown_socket_port");
            }

            ServerSocket shutdownSocket = new ServerSocket(shutdownSocketPort);
            System.out.println("Awaiting data on shutdown socket...");
            Socket newCyderInstance = shutdownSocket.accept(); // blocking

            // todo design json schema for messages coming from clients
            // message: shutdown, password: hash of Vexento or whatever the prop says

            String receivedHash = "hash";
            String hashedLocalhostShutdownRequestPassword = "hash";
            if (hashedLocalhostShutdownRequestPassword.contains(receivedHash)) {

            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        // todo get command, generate hashes, send, and start socket in sep process

        // todo need to make local host shutdown request api for purposes of testing bootstrap
        // localhost_shutdown_requests_enabled : true
        // auto_comply_to_localhost_shutdown_requests : false
        // localhost_shutdown_request_password : Vexento todo we should definitely hash this before sending it over the socket
    }

    // instance_socket_port : 8888
    // auto_attempt_bootstrap: true
    // prefer_javaw_over_java_when_bootstrapping: false

    // todo should receive a hash on the boostrap socket port and then log the EOS (end of session) and then
    //  let the new instance draw some kind of a separator, maybe like Cyder art but BOOSTRAP instead and then
    //  write all of it's stuff down and say something about successfully bootstrapped

    // todo need to validate key props on start too? sufficient subroutine for that with a key validator util?
    // todo key util with validation and getter methods?

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
