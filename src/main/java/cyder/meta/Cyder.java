package cyder.meta;

import com.google.common.collect.ImmutableList;
import cyder.enumerations.ExitCondition;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.Logger;
import cyder.login.LoginHandler;
import cyder.props.PropLoader;
import cyder.session.InstanceSocketUtil;
import cyder.session.SessionManager;
import cyder.strings.CyderStrings;
import cyder.subroutines.NecessarySubroutines;
import cyder.subroutines.SufficientSubroutines;
import cyder.time.CyderWatchdog;
import cyder.utils.JvmUtil;

import javax.swing.*;

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


        CyderSplash.INSTANCE.setLoadingMessage("Ensuring singular instance");
        if (!InstanceSocketUtil.instanceSocketPortAvailable()) {
            // todo attempt to boostrap here, say what instance was shutdown
            // todo should there be a session ID argument so that instances on different ports are not affected?
            ExceptionHandler.exceptionExit("Multiple instances of Cyder not allowed",
                    ExitCondition.MultipleInstancesExit, "Multiple Instances");
            throw new FatalException("Multiple instances of Cyder are not allowed");
        }

        InstanceSocketUtil.startListening();


        CyderSplash.INSTANCE.showSplash();

        SufficientSubroutines.executeSubroutines();

        LoginHandler.showProperStartupFrame();
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