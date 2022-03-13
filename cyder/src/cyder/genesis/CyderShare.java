package cyder.genesis;

import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.enums.LoggerTag;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.handlers.internal.LoginHandler;
import cyder.test.ManualTests;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.utilities.UserUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * Methods common to all Cyder that don't exactly belong in a utility class.
 */
@SuppressWarnings("FieldCanBeLocal") /* we want to declare some vars values on their line and not in the method */
public class CyderShare {
    /**
     * Instantiation of CyderCommon class not allowed
     */
    private CyderShare() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Controlled program exit that calls System.exit which will also invoke the shutdown hook.
     *
     * @param exitCondition the exiting code to describe why the program exited (0 is standard
     *             but for this program, the key/value pairs in {@link ExitCondition} are followed)
     */
    public static void exit(ExitCondition exitCondition) {
        try {
            //ensures IO finishes and is not invoked again
            UserUtil.blockFutureIO();

            //log exit
            Logger.log(LoggerTag.EXIT, exitCondition);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        System.exit(exitCondition.getCode());
    }

    /**
     * Returns the dominant frame for Cyder.
     *
     * @return the dominant frame for Cyder
     */
    public static @Nullable CyderFrame getDominantFrame() {
        if (!ConsoleFrame.getConsoleFrame().isClosed()) {
            if (ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().getState() == JFrame.ICONIFIED) {
                return null;
            } else return ConsoleFrame.getConsoleFrame().getConsoleCyderFrame();
        } else if (!LoginHandler.isLoginFrameClosed() && LoginHandler.getLoginFrame() != null){
            return LoginHandler.getLoginFrame();
        }
        //other possibly dominant/stand-alone frame checks here
        else return null;
    }

    /**
     * Whether connection to the internet is slow.
     */
    private static boolean highLatency;

    /**
     * Returns whether connection to the internet is slow.
     *
     * @return whether connection to the internet is slow.
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
        CyderShare.highLatency = highLatency;
    }

    /**
     * The time at which Cyder was first started.
     */
    private static long absoluteStartTime;

    /**
     * The time at which the console frame first appeared.
     */
    private static long consoleStartTime;

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
    static void setAbsoluteStartTime(long absoluteStartTime) {
        if (CyderShare.absoluteStartTime != 0)
            throw new IllegalArgumentException("Absolute Start Time already set");

        CyderShare.absoluteStartTime = absoluteStartTime;
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
        if (CyderShare.consoleStartTime != 0)
            return;

        CyderShare.consoleStartTime = consoleStartTime;
    }

    /**
     * Whether Cyder is being run as a compiled JAR file.
     */
    public static final boolean JAR_MODE = Objects.requireNonNull(
            Cyder.class.getResource("Cyder.class")).toString().startsWith("jar:");

    /**
     * Whether Cyder is currently released.
     */
    private static final boolean released = false;

    /**
     * Returns whether Cyder is currently released.
     *
     * @return whether Cyder is currently released
     */
    public static boolean isReleased() {
        return released;
    }

    /**
     * Whether Cyder is in fast testing mode.
     */
    private static final boolean fastTestingMode = false;

    /**
     * Returns whether Cyder is in fast testing mode.
     *
     * @return whether Cyder is in fast testing mode
     */
    static boolean isFastTestingMode() {
        return fastTestingMode;
    }

    /**
     * The name of the current Cyder version.
     */
    public static final String VERSION = "Insomnia";

    /**
     * The release date of the current Cyder version.
     */
    public static final String RELEASE_DATE = "22.3.6";

    /**
     * Whether components can be moved on their parent.
     */
    private static final boolean componentsRelocatable = false;

    /**
     * Returns whether components can be moved on their parent.
     *
     * @return whether components can be moved on their parent
     */
    public static boolean areComponentsRelocatable() {
        return componentsRelocatable;
    }

    /**
     * Whether normal testing mode is on.
     */
    private static final boolean testingMode = true;

    /**
     * Returns whether normal testing mode is active.
     * This will execute {@link ManualTests#launchTests()} upon Cyder start.
     *
     * @return whether normal testing mode is active
     */
    public static boolean isTestingMode() {
        return testingMode;
    }

    /**
     * Whether auto cypher is active.
     */
    private static final boolean autoCypher = true;

    /**
     * Returns whether auto cypher is active.
     *
     * @return whether auto cypher is active
     */
    public static boolean isAutoCypher() {
        return autoCypher;
    }
}
