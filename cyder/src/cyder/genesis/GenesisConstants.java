package cyder.genesis;

import com.google.common.collect.ImmutableList;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadRunner;
import cyder.utils.OsUtil;

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
     * The name to use for the temporary directory cleaning exit hook.
     */
    static final String REMOVE_TEMP_DIRECTORY_HOOK_NAME = "cyder-temporary-directory-cleaner-exit-hook";

    /**
     * The font used for default Java tooltips.
     */
    static final Font TOOLTIP_FONT = new CyderFonts.FontBuilder(CyderFonts.TAHOMA).setSize(20).generate();

    /**
     * The default color for the background of tooltips throughout Cyder
     */
    static final Color tooltipBorderColor = new Color(26, 32, 51);

    /**
     * The background used for tooltips
     */
    static final Color tooltipBackgroundColor = new Color(0, 0, 0);

    /**
     * The foreground used for tooltips
     */
    static final Color tooltipForegroundColor = CyderColors.regularPurple;

    /**
     * The ui manager tooltip border resource.
     */
    static final BorderUIResource TOOLTIP_BORDER_RESOURCE = new BorderUIResource(
            BorderFactory.createLineBorder(tooltipBorderColor, TOOLTIP_BORDER_THICKNESS, true));

    /**
     * The list of shutdown hooks to be added to this instance of Cyder.
     */
    static final ImmutableList<Thread> shutdownHooks = ImmutableList.of(
            CyderThreadRunner.createThread(() ->
                            OsUtil.deleteFile(Dynamic.buildDynamic(Dynamic.TEMP.getDirectoryName()), false),
                    REMOVE_TEMP_DIRECTORY_HOOK_NAME)
    );

    /**
     * Suppress default constructor.
     */
    private GenesisConstants() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
