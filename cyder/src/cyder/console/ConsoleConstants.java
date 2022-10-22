package cyder.console;

import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;
import cyder.enums.Direction;
import cyder.exceptions.IllegalMethodException;
import cyder.utils.StaticUtil;

import javax.swing.*;
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
    public static final String RELEASED_ENTER = RELEASED + CyderStrings.space + ENTER;

    /**
     * The width of the taskbar menu label.
     */
    public static final int TASKBAR_MENU_WIDTH = 110;

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
     * The value to indicate a frame is iconified.
     */
    public static final int FRAME_ICONIFIED = JFrame.ICONIFIED;

    /**
     * The value to indicate a frame is in a normal state.
     */
    public static final int FRAME_NORMAL = JFrame.NORMAL;

    /**
     * The font used for the clock label.
     */
    public static final Font CONSOLE_CLOCK_FONT = new Font("Agency FB", Font.BOLD, 25);

    /**
     * The music file for the f17 easter egg.
     */
    public static final File F_17_MUSIC_FILE = StaticUtil.getStaticResource("f17.mp3");

    /**
     * Suppress default constructor.
     */
    private ConsoleConstants() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
