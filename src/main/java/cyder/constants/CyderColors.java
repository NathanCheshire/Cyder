package cyder.constants;

import com.google.common.collect.ImmutableList;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.user.UserDataManager;

import java.awt.*;

/**
 * Common colors used throughout Cyder.
 */
@SuppressWarnings("unused")
public final class CyderColors {
    /**
     * A common color used for selected text such as in CyderFields.
     */
    public static final Color selectionColor = new Color(204, 153, 0);

    /**
     * A default green color, never to be changed.
     */
    public static final Color regularGreen = new Color(60, 167, 92);

    /**
     * A default blue color, never to be changed.
     */
    public static final Color regularBlue = new Color(38, 168, 255);

    /**
     * A default red color, never to be changed.
     */
    public static final Color regularRed = new Color(223, 85, 83);

    /**
     * A default pink color, never to be changed.
     */
    public static final Color regularPink = new Color(236, 64, 122);

    /**
     * An alternative red color.
     */
    public static final Color snapchatRed = new Color(242, 59, 87);

    /**
     * A default orange color, never to be changed.
     */
    public static final Color regularOrange = new Color(255, 140, 0);

    /**
     * The default text color to use for text when dark mode is active.
     */
    public static final Color defaultDarkModeTextColor = new Color(240, 240, 240);

    /**
     * The default text color to use for text when dark mode is active.
     */
    public static final Color defaultLightModeTextColor = new Color(16, 16, 16);

    /**
     * An empty color.
     */
    public static final Color empty = new Color(0, 0, 0, 0);

    /**
     * A default navy color that is used extensively throughout Cyder.
     */
    public static final Color navy = new Color(26, 32, 51);

    /**
     * The color used for notification borders.
     */
    public static final Color notificationBorderColor = new Color(26, 32, 51);

    /**
     * The standard Cyder purple color.
     */
    public static final Color regularPurple = new Color(85, 85, 255);

    /**
     * The background used for notifications.
     */
    public static final Color notificationBackgroundColor = new Color(0, 0, 0);

    /**
     * The default button color.
     */
    public static Color buttonColor = new Color(223, 85, 83);

    /**
     * The default taskbar border color for components that are always present in the taskbar.
     */
    public static final Color taskbarDefaultColor = new Color(137, 84, 160);

    /**
     * A common white color used throughout Cyder.
     */
    public static final Color vanilla = new Color(252, 252, 252);

    /**
     * The background color for frames when dark mode is active.
     */
    public static final Color darkModeBackgroundColor = new Color(30, 30, 30);

    /**
     * The background color for frames when dark mode is not active.
     */
    public static final Color regularBackgroundColor = vanilla;

    /**
     * The brown dirt color used for the perlin widget.
     */
    public static final Color brownDirt = new Color(131, 101, 57);

    /**
     * The possible border colors to use for the taskbar icon
     */
    public static final ImmutableList<Color> TASKBAR_BORDER_COLORS = ImmutableList.of(
            new Color(22, 124, 237),
            new Color(254, 49, 93),
            new Color(249, 122, 18)
    );

    /**
     * Suppress default constructor.
     */
    private CyderColors() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The color to use for frame borders and other ui components throughout Cyder.
     */
    private static Color guiThemeColor = navy;

    /**
     * Sets the current window border color/UI color.
     *
     * @param c the color to use for frame borders and other ui components
     */
    public static void setGuiThemeColor(Color c) {
        guiThemeColor = c;
    }

    /**
     * Returns the current gui theme color.
     *
     * @return the current gui theme color
     */
    public static Color getGuiThemeColor() {
        return guiThemeColor;
    }

    /**
     * Instantiates the gui theme color based on the current user's data.
     */
    public static void refreshGuiThemeColor() {
        setGuiThemeColor(UserDataManager.INSTANCE.getFrameColor());
    }
}
