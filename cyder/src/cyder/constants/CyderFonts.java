package cyder.constants;

import java.awt.*;

/**
 * Common fonts used throughout Cyder.
 *
 * Format for new fonts: fontNameFontSize unless there is a rare exception that applies
 */
public class CyderFonts {
    //segoe fonts
    public static final Font segoe20 = new Font("Segoe UI Black", Font.BOLD, 20);
    public static final Font segoe30 = new Font("Segoe UI Black", Font.BOLD, 30);

    //agency fonts
    public static final Font agencyFB22 = new Font("Agency FB", Font.BOLD, 22);
    public static final Font agencyFB30 = new Font("Agency FB", Font.BOLD, 30);

    public static final Font defaultFontSmall = agencyFB22;
    public static final Font defaultFont = agencyFB30;

    //tahoma fonts
    public static final Font tahoma20 = new Font("tahoma", Font.BOLD, 20);
    public static final Font tahoma30 = new Font("tahoma", Font.BOLD, 30);

    /**
     * The font used for CyderFrame title labels (typically equivalent to agencyFB22)
     */
    public static final Font frameTitleFont = new Font("Agency FB", Font.BOLD, 22);

    /**
     * The font used for CyderFrame notifications (typically equivalent to segoe20)
     */
    public static final Font notificationFont = new Font("Segoe UI Black", Font.BOLD, 20);

    /**
     * The font used for default Java tooltips
     */
    public static final Font javaTooltipFont = new Font("tahoma", Font.BOLD, 20);

    /**
     * No class instantiation allowed for CyderFonts
     */
    private CyderFonts() {
        throw new IllegalStateException(CyderStrings.attemptedInstantiation);
    }
}
