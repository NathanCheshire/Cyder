package cyder.constants;

import cyder.exceptions.IllegalMethodException;

import java.awt.*;

/**
 * Common colors used throughout Cyder
 */
public class CyderColors {
    //begin regular colors

    /**
     * A common color used for selected text such as in CyderFields
     */
    public static final Color selectionColor = new Color(204, 153, 0);

    /**
     * A default green color, never to be changed
     */
    public static final Color regularGreen = new Color(60, 167, 92);

    /**
     * A default blue color, never to be changed
     */
    public static final Color regularBlue = new Color(38, 168, 255);

    /**
     * A default red color, never to be changed
     */
    public static final Color regularRed = new Color(223, 85, 83);

    /**
     * A default pink color, never to be changed
     */
    public static final Color regularPink = new Color(236, 64, 122);

    /**
     * An alternative red color
     */
    public static final Color snapchatRed = new Color(242, 59, 87);

    /**
     * A default orange color, never to be changed
     */
    public static final Color regularOrange = new Color(255, 140, 0);

    //begin console colors

    /**
     * The default color for the background of tooltips throughout Cyder
     */
    public static final Color tooltipBorderColor = new Color(26, 32, 51);

    /**
     * The background used for tooltips
     */
    public static final Color tooltipBackgroundColor = new Color(0, 0, 0);

    //begin color mode text vars

    /**
     * The default text color to use for text when dark mode is active
     */
    public static final Color defaultDarkModeTextColor = new Color(240, 240, 240);

    /**
     * The default text color to use for text when dark mode is active
     */
    public static final Color defaultLightModeTextColor = new Color(16, 16, 16);

    /**
     * An empty color
     */
    public static final Color nullus = new Color(0, 0, 0, 0);

    //navy colors

    /**
     * A default navy color that is used extensively throughout Cyder
     */
    public static final Color navy = new Color(26, 32, 51);

    /**
     * A complementary color to navy to be used in conjunction with {@code CyderColors.navy}
     */
    public static final Color navyComplementary = new Color(39, 40, 34);

    //begin notification colors

    /**
     * The color used for notification borders
     */
    public static final Color notificationBorderColor = new Color(26, 32, 51);

    /**
     * The standard Cyder purple color.
     */
    public static final Color regularPurple = new Color(85, 85, 255);

    /**
     * The foreground used for tooltips
     */
    public static final Color tooltipForegroundColor = regularPurple;

    /**
     * The foreground color used for notifications
     */
    public static final Color notificationForegroundColor = new Color(85, 85, 255);

    /**
     * The background used for notifications
     */
    public static final Color notificationBackgroundColor = new Color(0, 0, 0);

    //begin button colors

    /**
     * The default button color
     */
    public static Color buttonColor = new Color(223, 85, 83);

    //begin taskbar colors

    /**
     * The default taskbar border color for components that are always present in the taskbar
     */
    public static final Color taskbarDefaultColor = new Color(137, 84, 160);

    /**
     * A common white color used throughout Cyder
     */
    public static final Color vanila = new Color(252, 252, 252);

    /**
     * The color to use for frame borders and other ui components throughout Cyder
     */
    private static Color guiThemeColor = navy;

    /**
     * The background color for frames when dark mode is active.
     */
    public static final Color darkModeBackgroundColor = new Color(30, 30, 30);

    /**
     * The background color for frames when dark mode is not active.
     */
    public static final Color regularBackgroundColor = vanila;

    /**
     * The primary badge svg color.
     */
    public static final Color svgPrimary = Color.decode("#055383");

    /**
     * The secondary badge svg color.
     */
    public static final Color svgSecondary = Color.decode("#5593C7");

    /**
     * The brown dirt color used for the perlin widget.
     */
    public static final Color brownDirt = new Color(131, 101, 57);

    /**
     * Instantiation of the CyderColors class is not allowed
     */
    private CyderColors() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Sets the current window border color/UI color
     *
     * @param c the color to use for frame borders and other ui components
     */
    public static void setGuiThemeColor(Color c) {
        guiThemeColor = c;
    }

    public static Color getGuiThemeColor() {
        return guiThemeColor;
    }
}
