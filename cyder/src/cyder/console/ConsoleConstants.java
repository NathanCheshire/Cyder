package cyder.console;

import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;
import cyder.enums.Direction;
import cyder.exceptions.IllegalMethodException;
import cyder.utils.StaticUtil;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Constants used for the {@link Console}.
 */
public final class ConsoleConstants {
    /**
     * The absolute minimum size allowable for the Console.
     */
    public static final Dimension MINIMUM_SIZE = new Dimension(600, 600);

    /**
     * The possible audio files to play if the starting user background is grayscale.
     */
    public static final ImmutableList<File> GRAYSCALE_AUDIO_PATHS = ImmutableList.of(
            StaticUtil.getStaticResource("badapple.mp3"),
            StaticUtil.getStaticResource("beetlejuice.mp3"),
            StaticUtil.getStaticResource("blackorwhite.mp3"));

    /**
     * The thickness of the border around the input field and output area when enabled.
     */
    public static final int FIELD_BORDER_THICKNESS = 3;

    /**
     * The default console direction.
     */
    public static final Direction DEFAULT_CONSOLE_DIRECTION = Direction.TOP;

    /**
     * The prefix for the input field bash string.
     */
    public static final String BASH_STRING_PREFIX = "@Cyder:~$ ";

    /**
     * The horizontal padding between the input/output fields and the frame bounds.
     */
    public static final int FIELD_X_PADDING = 15;

    /**
     * The vertical padding between the input and output fields and the frame bounds.
     */
    public static final int FIELD_Y_PADDING = 15;

    /**
     * The height of the input field.
     */
    public static final int INPUT_FIELD_HEIGHT = 100;

    /**
     * The input map focus key to allow drag label buttons to be triggered via the {@link KeyEvent#VK_ENTER} key.
     */
    public static final String BUTTON_INPUT_FOCUS_MAP_KEY = "Button.focusInputMap";

    /**
     * The pressed keyword for input focus mapping.
     */
    public static final String PRESSED = "pressed";

    /**
     * The released keyword for input focus mapping.
     */
    public static final String RELEASED = "released";

    /**
     * The enter keyword for input focus mapping.
     */
    public static final String ENTER = "ENTER";

    /**
     * The released enter keyword for input focus mapping.
     */
    public static final String RELEASED_ENTER = RELEASED + " " + ENTER;

    /**
     * The width of the taskbar menu label.
     */
    public static final int TASKBAR_MENU_WIDTH = 110;

    /**
     * The list function key codes above the default 12 function keys which Windows is capable of handling.
     */
    public static final ImmutableList<Integer> SPECIAL_FUNCTION_KEY_CODES = ImmutableList.of(
            KeyEvent.VK_F13,
            KeyEvent.VK_F14,
            KeyEvent.VK_F15,
            KeyEvent.VK_F16,
            KeyEvent.VK_F17,
            KeyEvent.VK_F18,
            KeyEvent.VK_F19,
            KeyEvent.VK_F20,
            KeyEvent.VK_F21,
            KeyEvent.VK_F22,
            KeyEvent.VK_F23,
            KeyEvent.VK_F24);

    /**
     * The keycode used to detect the f17 key being pressed and invoke the easter egg.
     */
    public static final int F_17_KEY_CODE = 17;

    /**
     * The offset for special function key codes and normal ones.
     */
    public static final int SPECIAL_FUNCTION_KEY_CODE_OFFSET = 13;

    /**
     * The fullscreen timeout between background animation increments.
     */
    public static final int FULLSCREEN_TIMEOUT = 1;

    /**
     * The fullscreen increment for background animations.
     */
    public static final int FULLSCREEN_INCREMENT = 20;

    /**
     * The default timeout between background animation increments.
     */
    public static final int DEFAULT_TIMEOUT = 5;

    /**
     * The default increment for background animations.
     */
    public static final int DEFAULT_INCREMENT = 8;

    /**
     * The x,y padding value for title notifications.
     */
    public static final int NOTIFICATION_PADDING = 20;

    /**
     * Suppress default constructor.
     */
    private ConsoleConstants() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
