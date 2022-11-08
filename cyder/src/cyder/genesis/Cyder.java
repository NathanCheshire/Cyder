package cyder.genesis;

import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.subroutines.NecessarySubroutines;
import cyder.genesis.subroutines.SufficientSubroutines;
import cyder.logging.Logger;
import cyder.login.LoginHandler;
import cyder.props.PropLoader;
import cyder.threads.CyderThreadRunner;
import cyder.time.CyderWatchdog;
import cyder.time.TimeUtil;
import cyder.utils.OsUtil;

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
     * The Jvm arguments provided when Cyder was started.
     */
    private static ImmutableList<String> jvmArguments;

    /**
     * Returns the Jvm arguments provided when Cyder was started.
     *
     * @return the Jvm arguments provided when Cyder was started
     */
    public static ImmutableList<String> getJvmArguments() {
        return jvmArguments;
    }

    /**
     * Setup and start the best program ever made :D
     *
     * @param arguments possible command line args passed in. Currently, these serve no purpose,
     *                  but we'll log them anyway (just like Big Brother would want)
     */
    public static void main(String[] arguments) {
        TimeUtil.setAbsoluteStartTime(System.currentTimeMillis());

        jvmArguments = ImmutableList.copyOf(arguments);

        PropLoader.loadProps();

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

    private static final String SLIDER_ONLY_LEFT_MOUSE_DRAG = "Slider.onlyLeftMouseButtonDrag";

    /**
     * Initializes all system and ui-manager toolkit key-value props.
     */
    private static void initUiAndSystemProps() {
        initUiManagerTooltipProps();
        initSystemProps();

        UIManager.put(SLIDER_ONLY_LEFT_MOUSE_DRAG, Boolean.TRUE);
    }

    /**
     * Initializes System.getProperty key/value pairs such as the ui scale.
     */
    private static void initSystemProps() {
        System.setProperty(UI_SCALE_ENABLED, Boolean.TRUE.toString());
        System.setProperty(SUN_UI_SCALE, PropLoader.getString(UI_SCALE));
        System.setProperty(IDE_SCALE, PropLoader.getString(UI_SCALE));
    }

    /**
     * The list of shutdown hooks to be added to this instance of Cyder.
     */
    private static final ImmutableList<Thread> shutdownHooks = ImmutableList.of(
            CyderThreadRunner.createThread(() -> OsUtil.deleteFile(
                            Dynamic.buildDynamic(Dynamic.TEMP.getDirectoryName()), false),
                    REMOVE_TEMP_DIRECTORY_HOOK_NAME)
    );

    /**
     * Adds the exit hooks to this Jvm.
     */
    private static void addExitHooks() {
        shutdownHooks.forEach(hook -> Runtime.getRuntime().addShutdownHook(hook));
    }
}