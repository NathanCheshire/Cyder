package cyder.meta;

import com.google.common.collect.ImmutableList;
import cyder.enumerations.ExitCondition;
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
import cyder.subroutines.NecessarySubroutines;
import cyder.subroutines.SufficientSubroutines;
import cyder.time.CyderWatchdog;
import cyder.utils.JvmUtil;

import javax.swing.*;
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
        Logger.determineInitializationSequence();
        initUiAndSystemProps();
        CyderWatchdog.initializeWatchDog();
        NecessarySubroutines.executeSubroutines();
        bindToInstanceSocket();
        CyderSplash.INSTANCE.showSplash();
        SufficientSubroutines.executeSubroutines();
        LoginHandler.showProperStartupFrame();
    }

    /**
     * Attempts to bind to the set instance socket. If unavailable, and localhost remote shutdown requests
     * are enabled, a communication message is sent through the socket in an attempt to shutdown another
     * instance of Cyder already bound to the instance port.
     */
    private static void bindToInstanceSocket() {
        CyderSplash.INSTANCE.setLoadingMessage("Ensuring singular instance");
        if (!InstanceSocketUtil.instanceSocketPortAvailable()) {
            if (Props.localhostShutdownRequestsEnabled.getValue()) {
                try {
                    int port = Props.instanceSocketPort.getValue();
                    String password = Props.localhostShutdownRequestPassword.getValue();
                    Future<CyderCommunicationMessage> futureMessage =
                            InstanceSocketUtil.sendRemoteShutdownRequest("localhost", port, password);
                    while (!futureMessage.isDone()) Thread.onSpinWait();
                    CyderCommunicationMessage message = futureMessage.get();
                    Logger.log(LogTag.DEBUG, "Received shutdown response message from session "
                            + message.getSessionId() + ", content: " + message.getContent());
                    long startedWaitingTime = System.currentTimeMillis();
                    while (!NetworkUtil.localPortAvailable(port)) {
                        if (System.currentTimeMillis() - startedWaitingTime >= 5000) {
                            throw new FatalException("Failed to bind to instance port");
                        }
                        Thread.onSpinWait();
                    }

                    return;
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }

            // todo concerns about log calls from both instances for a small period of time?

            // todo should there be a session ID argument so that instances on different ports are not affected?

            // todo figure this out below here for what to throw and what to say to user
            ExceptionHandler.exceptionExit("Multiple instances of Cyder not allowed",
                    ExitCondition.MultipleInstancesExit, "Multiple Instances");
            throw new FatalException("Instance port unavailable; change the socket port config or"
                    + " terminate the program using the instance port");
        }
        InstanceSocketUtil.startListening();
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