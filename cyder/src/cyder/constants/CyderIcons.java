package cyder.constants;

import cyder.exceptions.IllegalMethodException;
import cyder.user.UserUtil;
import cyder.utils.ImageUtil;
import cyder.utils.StaticUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Common ImageIcons used throughout Cyder for buttons
 */
public final class CyderIcons {
    /**
     * The Cyder logo.
     */
    public static final ImageIcon CYDER_ICON = new ImageIcon(StaticUtil.getStaticPath("CyderIcon.png"));

    /**
     * The Cyder logo used to indicate a background process is running.
     */
    public static final ImageIcon CYDER_BUSY_ICON = new ImageIcon(
            StaticUtil.getStaticPath("CyderBusyIcon.png"));

    /**
     * The x easter egg icon.
     */
    public static final ImageIcon X_ICON = new ImageIcon(StaticUtil.getStaticPath("x.png"));

    /**
     * The current icon to be used for CyderFrames.
     */
    private static ImageIcon currentCyderIcon = CYDER_ICON;

    private static final int DEFAULT_BACKGROUND_LEN = 1000;
    private static final int DEFAULT_LARGE_BACKGROUND_LEN = 2800;

    /**
     * A default image with dimensions 1000x1000
     */
    public static final ImageIcon defaultBackground = generateDefaultBackground(
            DEFAULT_BACKGROUND_LEN, DEFAULT_BACKGROUND_LEN);

    /**
     * A default image that spans the size of the primary display
     */
    public static final ImageIcon defaultBackgroundLarge = generateDefaultBackground(
            DEFAULT_LARGE_BACKGROUND_LEN,
            DEFAULT_LARGE_BACKGROUND_LEN);

    /**
     * Suppress default constructor.
     */
    private CyderIcons() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Generates a default icon of the requested dimensions.
     * The icon will change depending on whether dark mode
     * has been activated by the current user.
     *
     * @param width  the width of the image
     * @param height the height of the image
     * @return the ImageIcon of the requested dimensions
     */
    public static ImageIcon generateDefaultBackground(int width, int height) {
        Color color;
        if (UserUtil.getCyderUser() != null && UserUtil.getCyderUser().getDarkmode().equals("1")) {
            color = CyderColors.darkModeBackgroundColor;
        } else {
            color = CyderColors.regularBackgroundColor;
        }

        return ImageUtil.imageIconFromColor(color, width, height);
    }

    /**
     * Returns the currently set Cyder icon.
     *
     * @return the currently set Cyder icon
     */
    public static ImageIcon getCurrentCyderIcon() {
        return currentCyderIcon;
    }

    /**
     * Sets the current Cyder icon.
     *
     * @param currentCyderIcon the Cyder icon
     */
    public static void setCurrentCyderIcon(ImageIcon currentCyderIcon) {
        CyderIcons.currentCyderIcon = currentCyderIcon;
    }
}
