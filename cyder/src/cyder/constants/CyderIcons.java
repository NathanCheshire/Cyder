package cyder.constants;

import cyder.exceptions.IllegalMethodException;
import cyder.user.UserUtil;
import cyder.utils.ImageUtil;
import cyder.utils.OSUtil;
import cyder.utils.ScreenUtil;

import javax.swing.*;

/**
 * Common ImageIcons used throughout Cyder for buttons
 */
public final class CyderIcons {
    /**
     * The Cyder logo.
     */
    public static final ImageIcon CYDER_ICON = new ImageIcon(
            OSUtil.buildPath("static", "pictures", "CyderIcon.png"));

    /**
     * The Cyder logo used to indicate a background process is running.
     */
    public static final ImageIcon CYDER_BUSY_ICON = new ImageIcon(
            OSUtil.buildPath("static", "pictures", "CyderBusyIcon.png"));

    /**
     * The x easter egg icon.
     */
    public static final ImageIcon X_ICON = new ImageIcon(
            OSUtil.buildPath("static", "pictures", "print", "x.png"));

    /**
     * The current icon to be used for CyderFrames.
     */
    private static ImageIcon currentCyderIcon = CYDER_ICON;

    /*
     * Minimize icons used for CyderFrame DragLabels
     */

    public static final ImageIcon minimizeIcon = new ImageIcon(
            OSUtil.buildPath("static", "pictures", "windowicons", "minimize1.png"));
    public static final ImageIcon minimizeIconHover = new ImageIcon(
            OSUtil.buildPath("static", "pictures", "windowicons", "minimize2.png"));

    /*
     * Change Size icons used for occasional frames to change the size of
     * the frame or in Console's case, switch the background.
     */

    public static final ImageIcon changeSizeIcon = new ImageIcon(
            OSUtil.buildPath("static", "pictures", "icons", "changesize1.png"));
    public static final ImageIcon changeSizeIconHover = new ImageIcon(
            OSUtil.buildPath("static", "pictures", "icons", "changesize2.png"));

    /*
     * Pin icons used for setting a frame to always on top,
     * pin3.png is used to indicate a frame is pinned to the Console
     */

    public static final ImageIcon pinIcon = new ImageIcon(
            OSUtil.buildPath("static", "pictures", "icons", "pin1.png"));
    public static final ImageIcon pinIconHover = new ImageIcon(
            OSUtil.buildPath("static", "pictures", "icons", "pin2.png"));
    public static final ImageIcon pinIconHoverPink = new ImageIcon(
            OSUtil.buildPath("static", "pictures", "icons", "pin3.png"));

    /*
     * Close icons used for closing CyderFrames
     */

    public static final ImageIcon closeIcon = new ImageIcon(
            OSUtil.buildPath("static", "pictures", "windowicons", "Close1.png"));
    public static final ImageIcon closeIconHover = new ImageIcon(
            OSUtil.buildPath("static", "pictures", "windowicons", "Close2.png"));

    /*
     * Menu icons used for console menus.
     */

    public static final ImageIcon menuIcon = new ImageIcon(
            OSUtil.buildPath("static", "pictures", "icons", "menu1.png"));
    public static final ImageIcon menuIconHover = new ImageIcon(
            OSUtil.buildPath("static", "pictures", "icons", "menu2.png"));

    /**
     * A default image with dimensions 1000x1000
     */
    public static final ImageIcon defaultBackground = generateDefaultBackground(1000, 1000);

    /**
     * A default image that spans the size of the primary display
     */
    public static final ImageIcon defaultBackgroundLarge = generateDefaultBackground(
            Math.max(ScreenUtil.getScreenWidth(), ScreenUtil.getScreenHeight()),
            Math.max(ScreenUtil.getScreenWidth(), ScreenUtil.getScreenHeight()));

    /**
     * Instantiation of images class not allowed
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
        if (UserUtil.getCyderUser() == null) {
            return ImageUtil.imageIconFromColor(CyderColors.regularBackgroundColor, width, height);
        } else {
            return ImageUtil.imageIconFromColor(UserUtil.getCyderUser().getDarkmode().equals("1")
                    ? CyderColors.darkModeBackgroundColor : CyderColors.regularBackgroundColor, width, height);
        }
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
