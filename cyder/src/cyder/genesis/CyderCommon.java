package cyder.genesis;

import cyder.constants.CyderStrings;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.LoginHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadFactory;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.utilities.FrameUtil;
import cyder.utilities.UserUtil;

import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Methods c ommon to all of Cyder that don't exactly belong in a util class.
 */
public class CyderCommon {
    /**
     * Instantiation of CyderCommon class not allowed
     */
    private CyderCommon() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * Boolean describing the state of the frame checker failsafe.
     */
    private static boolean frameCheckerSuspended = false;
    private static boolean checkerStarted = false;

    /**
     * Suspends the frame checker.
     */
    public static void suspendFrameChecker() {
        frameCheckerSuspended = true;
    }

    /**
     * Starts the framechecker if it was paused.
     */
    public static void resumeFrameChecker() {
        frameCheckerSuspended = false;
    }

    /**
     * Starts the frame disposal failsafe.
     */
    public static void startFinalFrameDisposedChecker() {
        if (checkerStarted)
            return;

        checkerStarted = true;
        frameCheckerSuspended = true;

        Executors.newSingleThreadScheduledExecutor(new CyderThreadFactory(
                "Final Frame Disposed Checker")).scheduleAtFixedRate(() -> {
            if (!frameCheckerSuspended) {
                int validFrames = 0;

                for (CyderFrame f : FrameUtil.getCyderFrames()) {
                    if (f.isShowing()) {
                        validFrames++;
                    }
                }

                if (validFrames < 1) {
                    CyderCommon.exit(120);
                }
            }
        }, 10, 5, SECONDS);
    }

    /**
     * Controled program exit that calls System.exit which will also invoke the shutdown hook.
     *
     * @param code the exiting code to describe why the program exited (0 is standard
     *             but for this program, the key/value pairs in Sys.json are followed)
     */
    public static void exit(int code) {
        try {
            //sign the user out
            if (ConsoleFrame.getConsoleFrame().getUUID() != null)
                UserUtil.setUserData("loggedin","0");

            //ensures IO finishes and is not invoked again
            UserUtil.blockFutureIO();

            //log exit
            Logger.log(Logger.Tag.EXIT, code);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            System.exit(code);
        }
    }

    /**
     * Returns the dominant frame for Cyder.
     *
     * @return the dominant frame for Cyder
     */
    public static CyderFrame getDominantFrame() {
        if (!ConsoleFrame.getConsoleFrame().isClosed() && ConsoleFrame.getConsoleFrame() != null) {
            return ConsoleFrame.getConsoleFrame().getConsoleCyderFrame();
        } else if (!LoginHandler.isLoginFrameClosed() && LoginHandler.getLoginFrame() != null){
            return LoginHandler.getLoginFrame();
        }
        //other possibly dominant/stand-alone frame checks here
        else return null;
    }

    /**
     * Whether or not connection to the internet is slow.
     */
    private static boolean highLatency;

    /**
     * Returns whether or not connection to the internet is slow.
     *
     * @return whether or not connection to the internet is slow.
     */
    public static boolean isHighLatency() {
        return highLatency;
    }

    /**
     * Sets the value of highLatency.
     *
     * @param highLatency the value of high latency
     */
    public static void setHighLatency(boolean highLatency) {
        CyderCommon.highLatency = highLatency;
    }

    /**
     * The time at which Cyder was first started.
     */
    private static long absoluteStartTime = 0;

    /**
     * The time at which the console frame first appeared.
     */
    private static long consoleStartTime = 0;

    /**
     * Returns the absolute start time of Cyder.
     *
     * @return the absolute start time of Cyder
     */
    public static long getAbsoluteStartTime() {
        return absoluteStartTime;
    }

    /**
     * Sets the absolute start time of Cyder.
     *
     * @param absoluteStartTime the absolute start time of Cyder
     */
    public static void setAbsoluteStartTime(long absoluteStartTime) {
        if (CyderCommon.absoluteStartTime != 0)
            throw new IllegalArgumentException("Absolute Start Time already set");

        CyderCommon.absoluteStartTime = absoluteStartTime;
    }

    /**
     * Returns the time at which the console frame first appeared visible.
     * This is not affected by a user logout and successive login.
     *
     * @return the time at which the console frame first appeared visible
     */
    public static long getConsoleStartTime() {
        return consoleStartTime;
    }

    /**
     * Sets the time the console frame was shown.
     *
     * @param consoleStartTime the time the console frame was shown
     */
    public static void setConsoleStartTime(long consoleStartTime) {
        if (CyderCommon.consoleStartTime != 0)
            return;

        CyderCommon.consoleStartTime = consoleStartTime;
    }

    /**
     * Whether or not Cyder is being ran as a compiled JAR file.
     */
    public static final boolean JAR_MODE = Cyder.class.getResource("Cyder.class").toString().startsWith("jar:");
}
