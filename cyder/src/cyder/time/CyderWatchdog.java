package cyder.time;

import com.google.common.base.Preconditions;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A watchdog timer for Cyder to detect a freeze on the GUI and kill the application.
 */
public final class CyderWatchdog {
    /**
     * Suppress default constructor.
     */
    private CyderWatchdog() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

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
    private static final String ACTIVE_WATCHDOG = "activate_watchdog";

    /**
     * Whether the watchdog has been initialized and started.
     */
    private static final AtomicBoolean watchdogInitialized = new AtomicBoolean();

    /**
     * Waits for the AWT-EventQueue-0 thread to spawn and then polls the thread's state
     * every {@link CyderWatchdog#POLL_TIMEOUT} checking to ensure the thread is not frozen.
     * Upon a possible freeze event, the user will be informed and prompted to exit or restart Cyder.
     * <p>
     * Note: the watchdog will only start if the prop value <b>activate_watchdog</b> exists and is set to true.
     */
    public static void initializeWatchDog() {
        if (!PropLoader.getBoolean(ACTIVE_WATCHDOG)) {
            Logger.log(LogTag.WATCHDOG, "Watchdog skipped");
            return;
        }

        if (watchdogInitialized.get()) return;
        watchdogInitialized.set(true);

        CyderThreadRunner.submit(() -> {
            OUTER:
            while (true) {
                try {
                    ThreadUtil.sleep(INITIALIZE_TIMEOUT_MS);

                    // get thread group and enumerate over threads
                    ThreadGroup group = Thread.currentThread().getThreadGroup();
                    Thread[] currentThreads = new Thread[group.activeCount()];
                    group.enumerate(currentThreads);

                    for (Thread thread : currentThreads) {
                        // yes, this actually can happen
                        if (thread == null) continue;

                        // thread found so start actual watchdog timer and break out of initializer
                        if (thread.getName().equals(AWT_EVENT_QUEUE_0_NAME)) {
                            startWatchDog(thread);
                            break OUTER;
                        }
                    }
                } catch (Exception e) {
                    Logger.log(LogTag.WATCHDOG, ExceptionHandler.getPrintableException(e));
                }
            }
        }, IgnoreThread.WatchdogInitializer.getName());
    }

    /**
     * Starts the watchdog checker after the AWT-EventQueue-0 thread has been started.
     *
     * @param awtEventQueueThread the AWT-EventQueue-0 thread
     * @throws IllegalArgumentException if the provided thread
     *                                  is not named {@link CyderWatchdog#AWT_EVENT_QUEUE_0_NAME}
     */
    private static void startWatchDog(Thread awtEventQueueThread) {
        Preconditions.checkArgument(awtEventQueueThread.getName().equals(AWT_EVENT_QUEUE_0_NAME),
                "Improper provided thread for watchdog timer");

        AtomicInteger maxSessionFreezeLength = new AtomicInteger();

        CyderThreadRunner.submit(() -> {
            while (true) {
                try {
                    ThreadUtil.sleep(POLL_TIMEOUT);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }

                attemptWatchdogReset();

                Thread.State currentState = awtEventQueueThread.getState();
                if (currentState == Thread.State.RUNNABLE) {
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
                            Logger.log(LogTag.WATCHDOG, "JAR_MODE detected; attempting to "
                                    + "locate jar to boostrap from");
                            bootstrap();
                        } else {
                            Logger.log(LogTag.WATCHDOG, "JAR_MODE is not active thus "
                                    + "no jar can be located to boostrap from; exiting Cyder");
                            OsUtil.exit(ExitCondition.WatchdogTimeout);
                        }
                    }
                } else {
                    watchdogCounter.set(0);
                }
            }
        }, IgnoreThread.CyderWatchdog.getName());
    }

    @ForReadability
    private static void attemptWatchdogReset() {
        SwingUtilities.invokeLater(() -> watchdogCounter.set(0));
    }

    /**
     * Attempts to boostrap Cyder by quitting and opening a new instance.
     */
    private static void bootstrap() {
        // todo spawn a new Cyder process, maybe use python if possible to invoke launching the jar?
        // todo if this fails exit with code WatchdogBootstrapFail

        // boot_strap.py possible or can we just start a new process using the process api
    }

    @CyderTest
    public static void test() {
        try {
            ArrayList<String> standardOutput = new ArrayList<>();
            ArrayList<String> errorOutput = new ArrayList<>();

            String javaHome = System.getProperty("java.home");
            File f = new File(javaHome);
            f = new File(f, "bin");
            f = new File(f, "javaw.exe");
            System.out.println(f + "    exists: " + f.exists());


            // JvmUtil.getCyderJarReference();
            File file = new File("C:/users/nathan/downloads/test.bat");

            String shutdownHash = SecurityUtil.generateUuid();
            String resumeLogHash = SecurityUtil.generateUuid();

            Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/C",
                    file.getAbsolutePath(), shutdownHash, resumeLogHash});
            process.waitFor();
            process.getOutputStream().close();

            String outputLine;
            BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((outputLine = outReader.readLine()) != null) standardOutput.add(outputLine);
            outReader.close();

            String errorLine;
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((errorLine = errorReader.readLine()) != null) errorOutput.add(errorLine);
            errorReader.close();

            int i = 0;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}
