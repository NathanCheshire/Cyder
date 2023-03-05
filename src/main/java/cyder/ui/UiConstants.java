package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;

/**
 * Constants for Ui related applications.
 */
public final class UiConstants {
    /**
     * The value to indicate a frame is iconified.
     */
    public static final int FRAME_ICONIFIED = JFrame.ICONIFIED;

    /**
     * The value to indicate a frame is in a normal state.
     */
    public static final int FRAME_NORMAL = JFrame.NORMAL;

    /**
     * The thickness of default swing tooltip borders.
     */
    static final int TOOLTIP_BORDER_THICKNESS = 2;

    /**
     * The key to set set the ui manager's property to only allow a slider to be changed via left mouse button.
     */
    static final String SLIDER_ONLY_LEFT_MOUSE_DRAG = "Slider.onlyLeftMouseButtonDrag";

    /**
     * The tooltip background ui manager key.
     */
    static final String TOOLTIP_BACKGROUND = "ToolTip.background";

    /**
     * The tooltip border ui manager key.
     */
    static final String TOOLTIP_BORDER = "ToolTip.border";

    /**
     * The tooltip font ui manager key.
     */
    static final String TOOLTIP_FONT_KEY = "ToolTip.font";

    /**
     * The ui manager tooltip foreground key.
     */
    static final String TOOLTIP_FOREGROUND = "ToolTip.foreground";

    /**
     * The font used for default Java tooltips.
     */
    static final Font TOOLTIP_FONT = new CyderFonts.FontBuilder(CyderFonts.TAHOMA).setSize(20).generate();

    /**
     * The default color for the background of tooltips throughout Cyder
     */
    static final Color TOOLTIP_BORDER_COLOR = new Color(26, 32, 51);

    /**
     * The background used for tooltips
     */
    static final Color TOOLTIP_BACKGROUND_COLOR = Color.black;

    /**
     * The foreground used for tooltips
     */
    static final Color TOOLTIP_FOREGROUND_COLOR = CyderColors.regularPurple;

    /**
     * The ui manager tooltip border resource.
     */
    static final BorderUIResource TOOLTIP_BORDER_RESOURCE = new BorderUIResource(
            BorderFactory.createLineBorder(TOOLTIP_BORDER_COLOR, TOOLTIP_BORDER_THICKNESS, true));

    /**
     * Suppress default constructor.
     */
    private UiConstants() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
