package cyder.genesis;

import com.google.common.collect.ImmutableList;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.subroutines.NecessarySubroutines;
import cyder.genesis.subroutines.SufficientSubroutines;
import cyder.handlers.internal.Logger;
import cyder.handlers.internal.LoginHandler;
import cyder.threads.CyderThreadRunner;
import cyder.utils.OSUtil;
import cyder.utils.TimeUtil;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.io.File;

/**
 * The main Cyder entry point that performs checks on data and
 * environment variables to ensure a successful start can happen.
 */
public final class Cyder {
    /**
     * Instantiation of top level program genesis not permitted.
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

        addExitHook();

        Logger.initialize();

        initUiAndSystemProps();

        CyderWatchdog.initializeWatchDog();

        NecessarySubroutines.execute();

        CyderSplash.INSTANCE.showSplash();

        SufficientSubroutines.execute();

        LoginHandler.determineCyderEntry();
    }

    /**
     * Initializes UIManager tooltip key-value props.
     */
    private static void initUiManagerTooltipProps() {
        UIManager.put("ToolTip.background", CyderColors.tooltipBackgroundColor);
        UIManager.put("ToolTip.border", new BorderUIResource(BorderFactory.createLineBorder(
                CyderColors.tooltipBorderColor, 2, true)));
        UIManager.put("ToolTip.font", CyderFonts.TOOLTIP_FONT);
        UIManager.put("ToolTip.foreground", CyderColors.tooltipForegroundColor);
    }

    /**
     * Initializes all system and ui-manager toolkit key-value props.
     */
    private static void initUiAndSystemProps() {
        initUiManagerTooltipProps();
        initSystemProps();

        UIManager.put("Slider.onlyLeftMouseButtonDrag", Boolean.TRUE);
    }

    /**
     * Initializes System.getProperty key/value pairs such as the ui scale.
     */
    private static void initSystemProps() {
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("sun.java2d.uiScale", PropLoader.getString("ui_scale"));
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("ide.ui.scale", PropLoader.getString("ui_scale"));
    }

    /**
     * The name to use for the exit hook thread.
     */
    public static final String EXIT_HOOK_NAME = "exit-hook";

    /**
     * Adds the exit hook to this Jvm.
     */
    private static void addExitHook() {
        Runtime.getRuntime().addShutdownHook(CyderThreadRunner.createThread(() -> {
            File deleteDirectory = OSUtil.buildFile(Dynamic.PATH, Dynamic.TEMP.getDirectoryName());
            OSUtil.deleteFile(deleteDirectory, false);
        }, EXIT_HOOK_NAME));
    }
}