package cyder.console;

import com.google.common.collect.ImmutableList;
import cyder.enums.Direction;
import cyder.exceptions.IllegalMethodException;
import cyder.props.Props;
import cyder.strings.CyderStrings;
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
    static final Dimension MINIMUM_SIZE = new Dimension(600, 600);

    /**
     * The console snap size.
     */
    static final Dimension SNAP_SIZE = initializeSnapSize();

    /**
     * Returns the value for {@link #SNAP_SIZE}
     *
     * @return the value for {@link #SNAP_SIZE}
     */
    private static Dimension initializeSnapSize() {
        int len = Props.consoleSnapSize.getValue();
        return new Dimension(len, len);
    }

    /**
     * The possible audio files to play if the starting user background is grayscale.
     */
    static final ImmutableList<File> GRAYSCALE_AUDIO_PATHS = ImmutableList.of(
            StaticUtil.getStaticResource("badapple.mp3"),
            StaticUtil.getStaticResource("beetlejuice.mp3"),
            StaticUtil.getStaticResource("blackorwhite.mp3"));

    /**
     * The thickness of the border around the input field and output area when enabled.
     */
    static final int FIELD_BORDER_THICKNESS = 3;

    /**
     * The default console direction.
     */
    static final Direction DEFAULT_CONSOLE_DIRECTION = Direction.TOP;

    /**
     * The prefix for the input field bash string.
     */
    static final String BASH_STRING_PREFIX = "@Cyder:~$" + CyderStrings.space;

    /**
     * The horizontal padding between the input/output fields and the frame bounds.
     */
    static final int FIELD_X_PADDING = 15;

    /**
     * The vertical padding between the input and output fields and the frame bounds.
     */
    static final int FIELD_Y_PADDING = 15;

    /**
     * The height of the input field.
     */
    static final int INPUT_FIELD_HEIGHT = 100;

    /**
     * The input map focus key to allow drag label buttons to be triggered via the {@link KeyEvent#VK_ENTER} key.
     */
    static final String BUTTON_INPUT_FOCUS_MAP_KEY = "Button.focusInputMap";

    /**
     * The pressed keyword for input focus mapping.
     */
    static final String PRESSED = "pressed";

    /**
     * The released keyword for input focus mapping.
     */
    static final String RELEASED = "released";

    /**
     * The enter keyword for input focus mapping.
     */
    static final String ENTER = "ENTER";

    /**
     * The released enter keyword for input focus mapping.
     */
    static final String RELEASED_ENTER = RELEASED + CyderStrings.space + ENTER;

    /**
     * The width of the taskbar menu label.
     */
    static final int TASKBAR_MENU_WIDTH = 110;

    /**
     * The keycode used to detect the f17 key being pressed and invoke the easter egg.
     */
    static final int F_17_KEY_CODE = 17;

    /**
     * The offset for special function key codes and normal ones.
     */
    static final int SPECIAL_FUNCTION_KEY_CODE_OFFSET = 13;

    /**
     * The fullscreen timeout between background animation increments.
     */
    static final int FULLSCREEN_TIMEOUT = 1;

    /**
     * The fullscreen increment for background animations.
     */
    static final int FULLSCREEN_INCREMENT = 20;

    /**
     * The default timeout between background animation increments.
     */
    static final int DEFAULT_TIMEOUT = 5;

    /**
     * The default increment for background animations.
     */
    static final int DEFAULT_INCREMENT = 8;

    /**
     * The x,y padding value for title notifications.
     */
    static final int NOTIFICATION_PADDING = 20;

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
    static final Font CONSOLE_CLOCK_FONT = initializeConsoleClockFont();

    /**
     * Returns the font for the console clock.
     *
     * @return the font for the console clock
     */
    private static Font initializeConsoleClockFont() {
        String fontName = Props.consoleClockFontName.getValue();
        int fontSize = Props.consoleClockFontSize.getValue();

        return new Font(fontName, Font.BOLD, fontSize);
    }

    /**
     * The music file for the f17 easter egg.
     */
    static final File F_17_MUSIC_FILE = StaticUtil.getStaticResource("f17.mp3");

    /**
     * Suppress default constructor.
     */
    private ConsoleConstants() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
