package cyder.meta;

import com.google.common.collect.ImmutableList;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.login.LoginHandler;
import cyder.network.NetworkUtil;
import cyder.props.PropLoader;
import cyder.props.Props;
import cyder.session.CyderCommunicationMessage;
import cyder.session.InstanceSocketUtil;
import cyder.session.SessionManager;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.subroutines.NecessarySubroutines;
import cyder.subroutines.SufficientSubroutines;
import cyder.time.CyderWatchdog;
import cyder.utils.JvmUtil;

import javax.swing.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static cyder.meta.MetaConstants.*;

/**
 * The main Cyder entry point that performs checks on data and
 * environment variables to ensure a successful start can happen.
 */
public final class Cyder {
    /**
     * Suppress default constructor.
     */
    private Cyder() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The main Cyder entry point.
     *
     * @param arguments the command line arguments passed in
     */
    public static void main(String[] arguments) {
        SessionManager.INSTANCE.initializeSessionId();
        JvmUtil.setAndParseJvmMainMethodArgs(ImmutableList.copyOf(arguments));
        PropLoader.reloadProps();
        JvmUtil.logMainMethodArgs(JvmUtil.getJvmMainMethodArgs());
        addExitHooks();
        Logger.initialize();
        initUiAndSystemProps();
        CyderWatchdog.initializeWatchDog();
        NecessarySubroutines.executeSubroutines();
        attemptToBindToInstanceSocket();
        CyderSplash.INSTANCE.showSplash();
        SufficientSubroutines.executeSubroutines();
        LoginHandler.showProperStartupFrame();
    }

    private enum SocketBindAttemptResult {
        PORT_AVAILABLE(true, "Port available"),
        REMOTE_SHUTDOWN_REQUESTS_DISABLED(false, "Remote shutdown requests are disabled"),
        INVALID_PORT(false, "The instance socket port is invalid"),
        PASSWORD_NOT_SET(false, "The remote shutdown request password is not set"),
        REMOTE_SHUTDOWN_REQUEST_DENIED(false, "The remote shutdown request was denied"),
        FAILURE_WHILE_ATTEMPTING_REMOTE_SHUTDOWN(false,
                "An exception occurred while attempting to shutdown a remote instance"),
        TIMED_OUT_AFTER_SUCCESSFUL_REMOTE_SHUTDOWN(false,
                "A remote shutdown was successful but the instance port failed to free"),
        SUCCESS_AFTER_REMOTE_SHUTDOWN(true, "A remote shutdown request was successful and the port freed up"),
        PORT_UNAVAILABLE(false, "The port was unavailable and non-responsive to a remote shutdown request");

        /**
         * Whether this socket bind attempt result is a success.
         */
        private final boolean successful;

        /**
         * The message for this result.
         */
        private final String message;

        SocketBindAttemptResult(boolean successful, String message) {
            this.successful = successful;
            this.message = message;
        }

        /**
         * Returns whether this bind attempt result is a success.
         *
         * @return whether this bind attempt result is a success
         */
        public boolean isSuccessful() {
            return successful;
        }

        /**
         * Returns the message for this result.
         *
         * @return the message for this result
         */
        public String getMessage() {
            return message;
        }
    }

    private static SocketBindAttemptResult getSocketBindAttemptResult() {
        if (InstanceSocketUtil.instanceSocketPortAvailable()) return SocketBindAttemptResult.PORT_AVAILABLE;
        boolean shutdownRequestsEnabled = Props.localhostShutdownRequestsEnabled.getValue();
        if (!shutdownRequestsEnabled) return SocketBindAttemptResult.REMOTE_SHUTDOWN_REQUESTS_DISABLED;
        int port = Props.instanceSocketPort.getValue();
        if (!NetworkUtil.portRange.contains(port)) return SocketBindAttemptResult.INVALID_PORT;
        String password = Props.localhostShutdownRequestPassword.getValue();
        if (StringUtil.isNullOrEmpty(password)) return SocketBindAttemptResult.PASSWORD_NOT_SET;

        try {
            Future<CyderCommunicationMessage> futureMessage =
                    InstanceSocketUtil.sendRemoteShutdownRequest("localhost", port, password);
            while (!futureMessage.isDone()) Thread.onSpinWait();
            CyderCommunicationMessage message = futureMessage.get();
            String content = message.getContent();
            InstanceSocketUtil.RemoteShutdownRequestResult result =
                    InstanceSocketUtil.RemoteShutdownRequestResult.fromMessage(content);
            if (!result.isShouldComply()) return SocketBindAttemptResult.REMOTE_SHUTDOWN_REQUEST_DENIED;
            long startedWaitingTime = System.currentTimeMillis();
            while (!NetworkUtil.localPortAvailable(port)) {
                if (System.currentTimeMillis() - startedWaitingTime >= 5000) {
                    return SocketBindAttemptResult.TIMED_OUT_AFTER_SUCCESSFUL_REMOTE_SHUTDOWN;
                }
                Thread.onSpinWait();
            }
            return SocketBindAttemptResult.SUCCESS_AFTER_REMOTE_SHUTDOWN;
        } catch (InterruptedException | ExecutionException e) {
            ExceptionHandler.handle(e);
            return SocketBindAttemptResult.FAILURE_WHILE_ATTEMPTING_REMOTE_SHUTDOWN;
        } catch (FatalException e) {
            ExceptionHandler.handle(e);
            return SocketBindAttemptResult.PORT_UNAVAILABLE;
        }
    }

    /**
     * Attempts to bind to the set instance socket. If unavailable, and localhost remote shutdown requests
     * are enabled, a communication message is sent through the socket in an attempt to shutdown another
     * instance of Cyder already bound to the instance port.
     */
    private static void attemptToBindToInstanceSocket() {
        CyderSplash.INSTANCE.setLoadingMessage("Ensuring singular instance");
        Logger.log(LogTag.NETWORK, "Attempting to bind to instance socket port: "
                + Props.instanceSocketPort.getValue());
        SocketBindAttemptResult result = getSocketBindAttemptResult();
        Logger.log(LogTag.NETWORK, "Instance socket bind attempt result: " + result.getMessage());
        if (result.isSuccessful()) {
            InstanceSocketUtil.startListening();
        } else {
            throw new FatalException("Failed to bind to socket instance; " + result.getMessage());
        }
    }

    /**
     * Initializes all ui-manager look and feel key-value props.
     */
    private static void initUiAndSystemProps() {
        initUiManagerTooltipProps();

        UIManager.put(SLIDER_ONLY_LEFT_MOUSE_DRAG, Boolean.TRUE);
    }

    /**
     * Initializes UIManager tooltip key-value props.
     */
    private static void initUiManagerTooltipProps() {
        UIManager.put(TOOLTIP_BACKGROUND, tooltipBackgroundColor);
        UIManager.put(TOOLTIP_BORDER, TOOLTIP_BORDER_RESOURCE);
        UIManager.put(TOOLTIP_FONT_KEY, TOOLTIP_FONT);
        UIManager.put(TOOLTIP_FOREGROUND, tooltipForegroundColor);
    }

    /**
     * Adds the exit hooks to the JVM.
     */
    private static void addExitHooks() {
        shutdownHooks.forEach(hook -> Runtime.getRuntime().addShutdownHook(hook));
    }
}