package cyder.logging;

import com.google.common.collect.ImmutableList;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.strings.CyderStrings;
import cyder.utils.StaticUtil;

import java.util.regex.Pattern;

/**
 * Constants utilized throughout the logging package.
 */
public final class LoggingConstants {
    /**
     * The number of new lines to write after ascii art is written to a log file.
     */
    static final int numNewLinesAfterCyderAsciiArt = 2;

    /**
     * The number of new lines to write before the boostrap ascii art.
     */
    static final int numNewLinesBeforeBoostrapAsciiArt = 2;

    /**
     * The rate in ms at which to log the amount of objects created.
     */
    static final int objectCreationLogFrequency = 5000;

    /**
     * The end of log tag string.
     */
    static final String EOL = "Eol";

    /**
     * The end of log text.
     */
    static final String END_OF_LOG = "End Of Log";

    /**
     * The exit condition tag string.
     */
    static final String EXIT_CONDITION = "Exit Condition";

    /**
     * The runtime tag string.
     */
    static final String RUNTIME = "Runtime";

    /**
     * The exception tag string.
     */
    static final String EXCEPTION = "Exception";

    /**
     * The objects created tag string.
     */
    static final String OBJECTS_CREATED = "Objects Created";

    /**
     * The threads ran tag string.
     */
    static final String THREADS_RAN = "Threads Ran";

    /**
     * The user string.
     */
    static final String USER = "User";

    /**
     * The pattern for extracting parts from an objects created since last delta log call.
     */
    static final Pattern objectsCreatedSinceLastDeltaPattern = Pattern.compile("\\[(.*)].*\\((.*)ms\\):\\s*(.*)");

    /**
     * The objects created since last delta string.
     */
    static final String objectsCreatedSinceLastDelta = "Total objects created since last delta";

    /**
     * Only check 10 chars to the left of a line unless we force a break regardless
     * of whether a space is at that char.
     */
    static final int lineBreakInsertionTol = 10;

    /**
     * The chars to check to split at before splitting in between a line at whatever character a split index falls on.
     * The order in which these chars occur is their order of precedence.
     */
    static final ImmutableList<Character> breakChars = ImmutableList.of(
            ' ',
            '/',
            '\\',
            '-',
            '_',
            '.',
            '=',
            ',',
            ':',
            '\"',
            '\''
    );

    /**
     * The delay between JVM entry and starting the object creation logging thread.
     */
    static final int INITIAL_OBJECT_CREATION_LOGGER_TIMEOUT = 3000;

    /**
     * The maximum number of chars per line of a log.
     */
    static final int maxLogLineLength = 120;

    /**
     * The list of lines from cyder.txt depicting a sweet Cyder Ascii art logo.
     */
    static final ImmutableList<String> cyderSignatureLines = ImmutableList.copyOf(
            FileUtil.getFileLines(StaticUtil.getStaticResource("cyder.txt"))
    );

    /**
     * The list of lines from cyder.txt depicting a sweet Cyder Ascii art logo.
     */
    static final ImmutableList<String> boostrapLines = ImmutableList.copyOf(
            FileUtil.getFileLines(StaticUtil.getStaticResource("bootstrap.txt"))
    );

    /**
     * The group to extract from the {@link #objectsCreatedSinceLastDeltaPattern}
     * to determine the number of objects created logged.
     */
    static final int objectsCreatedGroupInLine = 3;

    /**
     * Suppress default constructor.
     */
    private LoggingConstants() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
