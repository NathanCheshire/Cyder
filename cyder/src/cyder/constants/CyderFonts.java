package cyder.constants;

import cyder.exceptions.IllegalMethodException;

import java.awt.*;

/**
 * Common fonts used throughout Cyder.
 * <p>
 * Format for new fonts: NAME_SIZE unless there is a rare exception that applies.
 */
public final class CyderFonts {
    /**
     * Segoe font at size 20.
     */
    public static final Font SEGOE_20 = new Font("Segoe UI Black", Font.BOLD, 20);

    /**
     * Segoe font at size 30.
     */
    public static final Font SEGOE_30 = new Font("Segoe UI Black", Font.BOLD, 30);

    /**
     * Agency FB font at size 22.
     */
    public static final Font AGENCY_FB_22 = new Font("Agency FB", Font.BOLD, 22);

    /**
     * Agency FB font at size 30.
     */
    public static final Font AGENCY_FB_30 = new Font("Agency FB", Font.BOLD, 30);

    /**
     * The default small font.
     */
    public static final Font DEFAULT_FONT_SMALL = AGENCY_FB_22;

    /**
     * The default font.
     */
    public static final Font DEFAULT_FONT = AGENCY_FB_30;

    /**
     * The font used for CyderFrame title labels (typically equivalent to agencyFB22).
     */
    public static final Font DEFAULT_FRAME_TITLE_FONT = new Font("Agency FB", Font.BOLD, 22);

    /**
     * The font used for CyderFrame notifications (typically equivalent to segoe20)
     */
    public static final Font NOTIFICATION_FONT = new Font("Segoe UI Black", Font.BOLD, 20);

    /**
     * The font used for default Java tooltips
     */
    public static final Font TOOLTIP_FONT = new Font("tahoma", Font.BOLD, 20);

    /**
     * The font used for the console clock label.
     */
    public static final Font CONSOLE_CLOCK_FONT = new Font("Segoe UI Black", Font.BOLD, 21);

    /**
     * No class instantiation allowed for CyderFonts
     */
    private CyderFonts() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
