package cyder.console

import com.google.common.collect.ImmutableList
import cyder.enumerations.Direction
import cyder.props.Props
import cyder.strings.CyderStrings
import cyder.utils.StaticUtil
import java.awt.Dimension
import java.awt.Font
import java.io.File

/**
 * Constants used for the [Console].
 */
internal class ConsoleConstants private constructor() {
    companion object {
        /**
         * The absolute minimum size allowable for the Console.
         */
        @JvmField
        val MINIMUM_SIZE = Dimension(400, 400)

        /**
         * The console snap size.
         */
        @JvmField
        val SNAP_SIZE = initializeSnapSize()

        /**
         * Returns the value for [.SNAP_SIZE]
         *
         * @return the value for [.SNAP_SIZE]
         */
        private fun initializeSnapSize(): Dimension {
            val len = Props.consoleSnapSize.value
            return Dimension(len, len)
        }

        /**
         * The possible audio files to play if the starting user background is grayscale.
         */
        @JvmField
        val GRAYSCALE_AUDIO_PATHS: ImmutableList<File> = ImmutableList.of(
                StaticUtil.getStaticResource("badapple.mp3"),
                StaticUtil.getStaticResource("beetlejuice.mp3"),
                StaticUtil.getStaticResource("blackorwhite.mp3"))

        /**
         * The thickness of the border around the input field and output area when enabled.
         */
        const val FIELD_BORDER_THICKNESS = 3

        /**
         * The default console direction.
         */
        @JvmField
        val DEFAULT_CONSOLE_DIRECTION = Direction.TOP

        /**
         * The prefix for the input field bash string.
         */
        const val BASH_STRING_PREFIX = "@Cyder:~$" + CyderStrings.space

        /**
         * The horizontal padding between the input/output fields and the frame bounds.
         */
        const val FIELD_X_PADDING = 15

        /**
         * The vertical padding between the input and output fields and the frame bounds.
         */
        const val FIELD_Y_PADDING = 15

        /**
         * The height of the input field.
         */
        const val INPUT_FIELD_HEIGHT = 100

        /**
         * The input map focus key to allow drag label buttons to be triggered via the enter key.
         */
        const val BUTTON_INPUT_FOCUS_MAP_KEY = "Button.focusInputMap"

        /**
         * The pressed keyword for input focus mapping.
         */
        const val PRESSED = "pressed"

        /**
         * The released keyword for input focus mapping.
         */
        const val RELEASED = "released"

        /**
         * The enter keyword for input focus mapping.
         */
        const val ENTER = "ENTER"

        /**
         * The released enter keyword for input focus mapping.
         */
        const val RELEASED_ENTER = RELEASED + CyderStrings.space + ENTER

        /**
         * The width of the taskbar menu label.
         */
        const val TASKBAR_MENU_WIDTH = 110

        /**
         * The keycode used to detect the f17 key being pressed and invoke the easter egg.
         */
        const val F_17_KEY_CODE = 17

        /**
         * The offset for special function key codes and normal ones.
         */
        const val SPECIAL_FUNCTION_KEY_CODE_OFFSET = 13

        /**
         * The fullscreen timeout between background animation increments.
         */
        const val fullscreenBackgroundAnimationTimeout = 1

        /**
         * The fullscreen increment for background animations.
         */
        const val fullscreenBackgroundAnimationIncrement = 20

        /**
         * The default timeout between background animation increments.
         */
        const val defaultBackgroundAnimationTimeout = 5

        /**
         * The default increment for background animations.
         */
        const val defaultBackgroundAnimationIncrement = 8

        /**
         * The x,y padding value for title notifications.
         */
        const val titleNotificationPadding = 20

        /**
         * The font used for the clock label.
         */
        @JvmField
        val CONSOLE_CLOCK_FONT = initializeConsoleClockFont()

        /**
         * Returns the font for the console clock.
         *
         * @return the font for the console clock
         */
        private fun initializeConsoleClockFont(): Font {
            val fontName = Props.consoleClockFontName.value
            val fontSize = Props.consoleClockFontSize.value
            return Font(fontName, Font.BOLD, fontSize)
        }

        /**
         * The music file for the f17 easter egg.
         */
        @JvmField
        val F_17_MUSIC_FILE = StaticUtil.getStaticResource("f17.mp3")!!
    }
}