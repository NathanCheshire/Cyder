package main.java.cyder.genesis;

import com.google.common.collect.ImmutableList;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.logging.Logger;
import main.java.cyder.login.LoginHandler;
import main.java.cyder.props.PropLoader;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.subroutines.NecessarySubroutines;
import main.java.cyder.subroutines.SufficientSubroutines;
import main.java.cyder.time.CyderWatchdog;
import main.java.cyder.utils.JvmUtil;

import javax.swing.*;

import static main.java.cyder.genesis.GenesisConstants.*;

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
        JvmUtil.setJvmMainMethodArgs(ImmutableList.copyOf(arguments));

        PropLoader.reloadProps();

        addExitHooks();

        Logger.initialize();

        initUiAndSystemProps();

        CyderWatchdog.initializeWatchDog();

        NecessarySubroutines.executeSubroutines();

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
     * Adds the exit hooks to this Jvm.
     */
    private static void addExitHooks() {
        shutdownHooks.forEach(hook -> Runtime.getRuntime().addShutdownHook(hook));
    }
}