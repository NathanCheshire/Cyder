package cyder.genesis;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;

/**
 * Constants for the genesis package.
 */
public final class GenesisConstants {
    /**
     * The thickness of default swing tooltip borders.
     */
    private static final int TOOLTIP_BORDER_THICKNESS = 2;

    /**
     * The tooltip background ui manager key.
     */
    public static final String TOOLTIP_BACKGROUND = "ToolTip.background";

    /**
     * The tooltip border ui manager key.
     */
    public static final String TOOLTIP_BORDER = "ToolTip.border";

    /**
     * The tooltip font ui manager key.
     */
    public static final String TOOLTIP_FONT_KEY = "ToolTip.font";

    /**
     * The ui manager tooltip foreground key.
     */
    public static final String TOOLTIP_FOREGROUND = "ToolTip.foreground";

    /**
     * The key for getting the ui scale prop key.
     */
    public static final String UI_SCALE = "ui_scale";

    /**
     * The java 2s sun ui scaling enabled prop key.
     */
    public static final String UI_SCALE_ENABLED = "sun.java2d.uiScale.enabled";

    /**
     * The java 2s sun ui scaling prop key.
     */
    public static final String SUN_UI_SCALE = "sun.java2d.uiScale";

    /**
     * The ide ui scaling prop key.
     */
    public static final String IDE_SCALE = "ide.ui.scale";

    /**
     * The name to use for the temporary directory cleaning exit hook.
     */
    public static final String REMOVE_TEMP_DIRECTORY_HOOK_NAME = "cyder-temporary-directory-cleaner-exit-hook";

    /**
     * The font used for default Java tooltips.
     */
    public static final Font TOOLTIP_FONT = new CyderFonts.FontBuilder(CyderFonts.TAHOMA).setSize(20).generate();

    /**
     * The default color for the background of tooltips throughout Cyder
     */
    public static final Color tooltipBorderColor = new Color(26, 32, 51);

    /**
     * The background used for tooltips
     */
    public static final Color tooltipBackgroundColor = new Color(0, 0, 0);

    /**
     * The foreground used for tooltips
     */
    public static final Color tooltipForegroundColor = CyderColors.regularPurple;

    /**
     * The ui manager tooltip border resource.
     */
    public static final BorderUIResource TOOLTIP_BORDER_RESOURCE = new BorderUIResource(
            BorderFactory.createLineBorder(tooltipBorderColor, TOOLTIP_BORDER_THICKNESS, true));

    /**
     * Suppress default constructor.
     */
    private GenesisConstants() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
