package cyder.watchdog;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.enumerations.ExitCondition;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.meta.CyderArguments;
import cyder.props.Props;
import cyder.session.SessionManager;
import cyder.strings.CyderStrings;
import cyder.utils.JvmUtil;
import cyder.utils.OsUtil;

import java.io.IOException;

/**
 * Utilities related to bootstrapping in the attempt of a UI thread freeze.
 */
public final class BoostrapUtil {
    /**
     * Suppress default constructor.
     */
    private BoostrapUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
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
    public static boolean invokeBoostrapIfConditionsMet() {
        try {
            if (!OsUtil.isWindows()) {
                onFailedBoostrap("Invalid operating system: " + OsUtil.OPERATING_SYSTEM);
            } else if (JvmUtil.currentInstanceLaunchedWithDebug()) {
                onFailedBoostrap("Current JVM was launched with JDWP args");
            } else if (!Props.attemptBootstrap.getValue()) {
                onFailedBoostrap("attempt_boostrap prop set to false");
            } else {
                Logger.log(LogTag.WATCHDOG, "Boostrap conditions met, attempting bootstrap");
                bootstrap();
                return true;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            onFailedBoostrap(e.getMessage());
        }

        return false;
    }

    /**
     * Invokes a boostrap attempt. This will request this instance of
     * Cyder to shutdown after the new instance starts.
     */
    private static void bootstrap() {
        ImmutableList<String> command = ImmutableList.of(
                JvmUtil.getFullJvmInvocationCommand(),
                CyderArguments.SESSION_ID.constructFullParameter(),
                SessionManager.INSTANCE.getSessionId(),
                CyderArguments.BOOSTRAP.constructFullParameter()
        );

        try {
            OsUtil.executeShellCommand(command);
        } catch (IOException e) {
            onFailedBoostrap("Boostrap failed: " + e.getMessage());
        }
    }

    /**
     * Logs a watchdog tagged log message with the provided reason and exits
     * with the exit condition of {@link ExitCondition#WatchdogBootstrapFail}.
     *
     * @param reason the reason the bootstrap  failed
     */
    private static void onFailedBoostrap(String reason) {
        Logger.log(LogTag.WATCHDOG, "Failed to boostrap: " + reason);
        OsUtil.exit(ExitCondition.WatchdogBootstrapFail);
    }
}
