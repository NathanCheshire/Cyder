package cyder.genesis;

import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;

/**
 * Constants for the genesis package.
 */
public final class Constants {
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
    public static final String TOOLTIP_FONT = "ToolTip.font";

    /**
     * The ui manager tooltip foreground key.
     */
    public static final String TOOLTIP_FOREGROUND = "ToolTip.foreground";

    /**
     * The ui manager tooltip border resource.
     */
    public static final BorderUIResource TOOLTIP_BORDER_RESOURCE = new BorderUIResource(
            BorderFactory.createLineBorder(CyderColors.tooltipBorderColor, TOOLTIP_BORDER_THICKNESS, true));

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
    public static final String CLEANER_EXIT_HOOK = "cyder-temporary-directory-cleaner-exit-hook";

    /**
     * Suppress default constructor.
     */
    private Constants() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
