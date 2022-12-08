package cyder.genesis;

import com.google.common.collect.ImmutableList;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.subroutines.NecessarySubroutines;
import cyder.genesis.subroutines.SufficientSubroutines;
import cyder.logging.Logger;
import cyder.login.LoginHandler;
import cyder.props.PropLoader;
import cyder.strings.CyderStrings;
import cyder.time.CyderWatchdog;
import cyder.utils.JvmUtil;

import javax.swing.*;

import static cyder.genesis.GenesisConstants.*;

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
     * Setup and start the best program ever made :D
     *
     * @param arguments possible command line args passed in. Currently, these serve no purpose,
     *                  but we'll log them anyway (just like Big Brother would want)
     */
    public static void main(String[] arguments) {
        JvmUtil.setJvmMainMethodArgs(ImmutableList.copyOf(arguments));

        PropLoader.reloadProps();

        addExitHooks();

        Logger.initialize();

        initUiAndSystemProps();

        CyderWatchdog.initializeWatchDog();

        NecessarySubroutines.execute();

        CyderSplash.INSTANCE.showSplash();

        SufficientSubroutines.execute();

        LoginHandler.showProperStartupFrame();
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
     * Initializes all ui-manager look and feel key-value props.
     */
    private static void initUiAndSystemProps() {
        initUiManagerTooltipProps();

        UIManager.put(SLIDER_ONLY_LEFT_MOUSE_DRAG, Boolean.TRUE);
    }

    /**
     * Adds the exit hooks to this Jvm.
     */
    private static void addExitHooks() {
        shutdownHooks.forEach(hook -> Runtime.getRuntime().addShutdownHook(hook));
    }
}