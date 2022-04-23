package cyder.genesis;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.enums.IgnoreThread;
import cyder.enums.LoggerTag;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.utilities.OSUtil;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A watchdog timer for Cyder to detect a freeze on the GUI and kill the application.
 */
public class CyderWatchdog {
    /**
     * Suppress default constructor.
     */
    private CyderWatchdog() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * The time in ms to wait between checking for the first appearance of AWT-EventQueue-0.
     */
    public static final int INITIALIZE_TIMEOUT = 3000;

    /**
     * The time in ms to wait between checking the AWT-EventQueue-0 thread for its status.
     */
    public static final int POLL_TIMEOUT = 1000;

    /**
     * The standard name of the AWT-EventQueue-0 thread.
     */
    public static final String AWT_EVENT_QUEUE_0_NAME = "AWT-EventQueue-0";

    /**
     * The actual watchdog timer to detect a hault if it is not reset by the time a certain
     * value is reached.
     */
    private static final AtomicInteger watchdogCounter = new AtomicInteger();

    /**
     * The maximum number the watchdog counter can achieve before triggering a fatal reset.
     */
    public static final int MAX_WATCHDOG_COUNT = 5;

    /**
     * Waits for the AWT-EventQueue-0 thread to spawn and then polls the thread's state
     * every {@link CyderWatchdog#POLL_TIMEOUT} checking to ensure the thread is not frozen.
     * Upon a possible freeze event, the user will be informed and prompted to exit or restart Cyder.
     */
    public static void initializeWatchDog() {
        CyderThreadRunner.submit(() -> {
            OUTER:
                while (true) {
                    try {
                        // timeout first
                        Thread.sleep(INITIALIZE_TIMEOUT);

                        // get thread group and enumerate over threads
                        ThreadGroup group = Thread.currentThread().getThreadGroup();
                        Thread[] currentThreads = new Thread[group.activeCount()];
                        group.enumerate(currentThreads);

                        for (Thread thread : currentThreads) {
                            // thread found so start actual watchdog timer and break out of initializer
                            if (thread.getName().equals(AWT_EVENT_QUEUE_0_NAME)) {
                                startWatchDog(thread);
                                break OUTER;
                            }
                        }
                    } catch (Exception e) {
                        Logger.Debug(ExceptionHandler.getPrintableException(e));
                    }
            }
        }, IgnoreThread.WatchdogInitializer.getName());
    }

    /**
     * Starts the watchdog checker after the AWT-EventQueue-0 thread has been started.
     *
     * @param awtEventQueueThread the AWT-EventQueue-0 thread
     * @throws IllegalArgumentException if the provided thread
     * is not named {@link CyderWatchdog#AWT_EVENT_QUEUE_0_NAME}
     */
    private static void startWatchDog(Thread awtEventQueueThread) {
        Preconditions.checkArgument(awtEventQueueThread.getName().equals(AWT_EVENT_QUEUE_0_NAME),
                "Improper provided thread for watchdog timer");

        CyderThreadRunner.submit(() -> {
            while (true) {
                try {
                    Thread.sleep(POLL_TIMEOUT);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }

                // reset watchdog timer using AWT EventQueue thread
                SwingUtilities.invokeLater(() -> {
                    watchdogCounter.set(0);
                });

                Thread.State currentState = awtEventQueueThread.getState();

                if (currentState == Thread.State.RUNNABLE) {
                    watchdogCounter.getAndIncrement();

                    int get = watchdogCounter.get();

                    // log if getting close to a timeout
                    if (get > MAX_WATCHDOG_COUNT / 2) {
                        Logger.log(LoggerTag.DEBUG, "Watchdog timer over halfway "
                                + "to timeout, value = " + get);
                    }

                    if (watchdogCounter.get() == MAX_WATCHDOG_COUNT) {
                        Logger.log(LoggerTag.DEBUG, "Hault detected by watchdog,");

                        boolean tmpJarMode = true;
                        if (OSUtil.JAR_MODE) {
                            Logger.log(LoggerTag.DEBUG, "JAR_MODE detected; attempting to " +
                                    "locate jar to boostrap from");
                            bootstrap();
                        } else {
                            Logger.log(LoggerTag.DEBUG, "JAR_MODE is not active thus " +
                                    "no jar can be located to boostrap from; exiting Cyder");
                            OSUtil.exit(ExitCondition.WatchdogTimeout);
                        }
                    }
                } else {
                    watchdogCounter.set(0);
                }
            }
        }, IgnoreThread.CyderWatchdog.getName());
    }

    /**
     * Attempts to boostrap Cyder by quitting and opening a new instance.
     */
    private static void bootstrap() {
        // todo start a python process to bootstrap ourself.
        //  Might have to unbind the socket to ensure a singular instance ever exists.
    }
}
