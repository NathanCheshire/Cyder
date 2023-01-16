package cyder.constants;

import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.ui.UiUtil;
import cyder.utils.ImageUtil;
import cyder.utils.StaticUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Common {@link ImageIcon}s used throughout Cyder.
 */
public final class CyderIcons {
    /**
     * The Cyder logo.
     */
    public static final ImageIcon CYDER_ICON = new ImageIcon(StaticUtil.getStaticPath("CyderIcon.png"));

    /**
     * The x easter egg icon.
     */
    public static final ImageIcon X_ICON = new ImageIcon(StaticUtil.getStaticPath("x.png"));

    /**
     * The length of the default background.
     */
    private static final int DEFAULT_BACKGROUND_LEN = 1000;

    /**
     * The length of the default large background.
     */
    private static final int DEFAULT_LARGE_BACKGROUND_LEN = Math.max(
            UiUtil.getDefaultMonitorWidth(),
            UiUtil.getDefaultMonitorHeight());

    /**
     * A default image with dimensions 1000x1000
     */
    public static final ImageIcon defaultBackground = generateDefaultBackground(
            DEFAULT_BACKGROUND_LEN,
            DEFAULT_BACKGROUND_LEN);

    /**
     * A default image that spans the size of the primary display
     */
    public static final ImageIcon defaultBackgroundLarge = generateDefaultBackground(
            DEFAULT_LARGE_BACKGROUND_LEN,
            DEFAULT_LARGE_BACKGROUND_LEN);

    /**
     * The length of the default solid background.
     */
    private static final int DEFAULT_SOLID_BACKGROUND_LEN = 800;

    /**
     * The default background to use for account creation when a network connection is unavailable.
     */
    public static final BufferedImage DEFAULT_USER_SOLID_COLOR_BACKGROUND
            = ImageUtil.toBufferedImage(ImageUtil.imageIconFromColor(Color.black,
            DEFAULT_SOLID_BACKGROUND_LEN, DEFAULT_SOLID_BACKGROUND_LEN));

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
        return ImageUtil.imageIconFromColor(CyderColors.regularBackgroundColor, width, height);
    }
}
