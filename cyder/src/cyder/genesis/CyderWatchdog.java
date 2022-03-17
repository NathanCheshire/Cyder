package cyder.genesis;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.enums.LoggerTag;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;

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
    public static final int POLL_TIMEOUT = 3000;

    /**
     * The standard name of the AWT-EventQueue-0 thread.
     */
    public static final String AWT_EVENT_QUEUE_0_NAME = "AWT-EventQueue-0";

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
                        ExceptionHandler.handleWithoutLogging(e);
                    }
            }
        }, "Watchdog Initializer");
    }

    private static boolean HAULTED;

    /**
     * Returns whether a hault has been detected.
     *
     * @return whether a hault has been detected
     */
    public static boolean getHAULTED() {
        return HAULTED;
    }

    // I'm not entirely sure this watchdog is fool-proof and won't be
    // triggered by other actions and operations throughout Cyder.
    // Only time will tell if my conjecture is correct, however.

    /**
     * Starts the watchdog checker after the AWT-EventQueue-0 thread has been started.
     *
     * @param awtEventQueueThread the AWT-EventQueue-0 thread
     */
    private static void startWatchDog(Thread awtEventQueueThread) {
        Preconditions.checkArgument(awtEventQueueThread.getName().equals(AWT_EVENT_QUEUE_0_NAME),
                "Improper thread for watchdog timer");

        CyderThreadRunner.submit(() -> {
            while (true) {
                try {
                    Thread.sleep(POLL_TIMEOUT);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }

                Thread.State currentState = awtEventQueueThread.getState();

                Logger.log(LoggerTag.THREAD_STATUS, "name = "
                        + AWT_EVENT_QUEUE_0_NAME + ", state = " + currentState);
                System.out.println("HAULT POSSIBLE, state = " + currentState);

                // todo disabled until a proper algorithm can be derived
                if (currentState == Thread.State.RUNNABLE) {
                    // HAULTED = true;
                    // todo start a python process to bootstrap ourself
                    //CyderShare.exit(ExitCondition.WatchdogCatch);
                    // break;
                }
            }
        }, "Cyder Watchdog");
    }

    // todo python package should essentailly go away, static should have a python directory

    //todo when logging json write, log levenstein distance between last and current
    // just store last thing written so you dont have to read and then write
}
