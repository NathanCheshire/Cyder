package cyder.logging;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.time.TimeUtil;
import cyder.utils.StaticUtil;
import cyder.utils.StringUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;

import static cyder.constants.CyderStrings.*;

/** Utilities necessary for the Cyder logger. */
public final class LoggingUtil {
    /** Suppress default constructor. */
    private LoggingUtil() {
        throw new IllegalMethodException(ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the time tag placed at the beginning of all log statements.
     * Example: "[22-12-39] "
     *
     * @return the time tag placed at the beginning of all log statements
     */
    static String getLogTimeTag() {
        return openingBracket + TimeUtil.getLogLineTime() + closingBracket + space;
    }

    /**
     * Returns whether the two log lines are equivalent.
     *
     * @param logLine1 the first log line
     * @param logLine2 the second log line
     * @return whether the two log lines are equivalent
     */
    public static boolean areLogLinesEquivalent(String logLine1, String logLine2) {
        Preconditions.checkNotNull(logLine1);
        Preconditions.checkNotNull(logLine2);

        if (!matchesStandardLogLine(logLine1) || !matchesStandardLogLine(logLine2)) {
            return logLine1.equals(logLine2);
        }

        String timeTag1 = logLine1.substring(logLine1.indexOf(openingBracket),
                logLine2.indexOf(closingBracket) + 1).trim();
        String timeTag2 = logLine2.substring(logLine2.indexOf(openingBracket),
                logLine2.indexOf(closingBracket) + 1).trim();

        logLine1 = logLine1.replace(timeTag1, "");
        logLine2 = logLine2.replace(timeTag2, "");

        return !StringUtil.isNullOrEmpty(logLine1)
                && !StringUtil.isNullOrEmpty(logLine2)
                && logLine1.equalsIgnoreCase(logLine2);
    }

    /**
     * Returns whether the provided line is a standard log line.
     *
     * @param line the line
     * @return whether the provided line is a standard log line.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted") /* Readability */
    public static boolean matchesStandardLogLine(String line) {
        Preconditions.checkNotNull(line);

        if (!line.startsWith(openingBracket)) {
            return false;
        } else if (!line.contains(closingBracket)) {
            return false;
        }

        return CyderRegexPatterns.standardLogLinePattern.matcher(line).matches();
    }

    /** The maximum number of chars per line of a log. */
    public static final int MAX_LINE_LENGTH = 120;

    /**
     * Only check 10 chars to the left of a line unless we force a break regardless
     * of whether a space is at that char.
     */
    private static final int BREAK_INSERTION_TOL = 10;

    /** The chars to check to split at before splitting in between a line at whatever character a split index falls on. */
    private static final ImmutableList<Character> BREAK_CHARS
            = ImmutableList.of(' ', '/', '\'', '-', '_', '.', '=', ',', ':');

    /**
     * Returns the provided string with line breaks inserted if needed to ensure
     * the line length does not surpass {@link #MAX_LINE_LENGTH}.
     *
     * @param line the line to insert breaks in if needed
     * @return the formatted lines
     */
    public static LinkedList<String> insertBreaks(String line) {
        Preconditions.checkNotNull(line);

        LinkedList<String> lines = new LinkedList<>();

        while (line.length() > MAX_LINE_LENGTH) {
            boolean breakInserted = false;

            for (char splitChar : BREAK_CHARS) {
                if (line.charAt(MAX_LINE_LENGTH) == splitChar) {
                    lines.add(line.substring(0, MAX_LINE_LENGTH));
                    line = line.substring(MAX_LINE_LENGTH);
                    breakInserted = true;
                    break;
                }

                int leftSplitIndex = LoggingUtil.checkLeftForSplitChar(line, splitChar);
                if (leftSplitIndex != -1) {
                    lines.add(line.substring(0, leftSplitIndex));
                    line = line.substring(leftSplitIndex);
                    breakInserted = true;
                    break;
                }

                int rightSplitIndex = LoggingUtil.checkRightForSplitChar(line, splitChar);
                if (rightSplitIndex != -1) {
                    lines.add(line.substring(0, rightSplitIndex));
                    line = line.substring(rightSplitIndex);
                    breakInserted = true;
                    break;
                }
            }

            if (breakInserted) continue;
            // Couldn't find a split char from the list so split at the maximum index
            lines.add(line.substring(0, MAX_LINE_LENGTH));
            line = line.substring(MAX_LINE_LENGTH);
        }

        lines.add(line);

        return lines;
    }

    /**
     * Attempts to find the index of the split char within the final {@link #BREAK_INSERTION_TOL}
     * chars of the end of the provided string. If found, returns the index of the found splitChar.
     *
     * @param line      the line to search through
     * @param splitChar the character to split at
     * @return the index of the split char if found, -1 else
     */
    static int checkLeftForSplitChar(String line, char splitChar) {
        int ret = -1;

        for (int i = MAX_LINE_LENGTH - BREAK_INSERTION_TOL ; i < MAX_LINE_LENGTH ; i++) {
            if (line.charAt(i) == splitChar) {
                ret = i;
                break;
            }
        }

        return ret;
    }

    /**
     * Attempts to find the index of the split char within {@link #BREAK_INSERTION_TOL} chars of the right of
     * {@link #MAX_LINE_LENGTH}. If found, returns the index of the found splitChar.
     *
     * @param line      the line to search through
     * @param splitChar the character to split at
     * @return the index of the slit char if found, -1 else
     */
    static int checkRightForSplitChar(String line, char splitChar) {
        int ret = -1;

        for (int i = MAX_LINE_LENGTH ; i < MAX_LINE_LENGTH + BREAK_INSERTION_TOL ; i++) {
            if (i >= line.length()) break;
            if (line.charAt(i) == splitChar) {
                ret = i;
                break;
            }
        }

        return ret;
    }

    /**
     * Returns whether the provided string is empty or only contains whitespace and or a new line character.
     *
     * @param string the string
     * @return whether the provided string is empty or only contains whitespace and or a new line character
     */
    static boolean emptyOrNewline(String string) {
        Preconditions.checkNotNull(string);
        string = string.trim();

        return string.isEmpty() || string.equals(CyderStrings.newline);
    }

    /** The filename of the file that contains the Cyder signature to place at the top of log files. */
    private static final String SIGNATURE_FILE_NAME = "cyder.txt";

    /** The list of lines from cyder.txt depicting a sweet Cyder Ascii art logo. */
    private static ImmutableList<String> headerLogoLines = ImmutableList.of();

    static {
        try (BufferedReader bufferedReader = new BufferedReader(
                new FileReader(StaticUtil.getStaticResource(SIGNATURE_FILE_NAME)))) {
            LinkedList<String> set = new LinkedList<>();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                set.add(line);
            }

            headerLogoLines = ImmutableList.copyOf(set);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns the header logo lines for the top of log files.
     *
     * @return the header logo lines for the top of log files
     */
    static ImmutableList<String> getHeaderLogoLines() {
        return headerLogoLines;
    }

    /**
     * Generates and returns a log line for when a log was deleted mid session.
     *
     * @return returns a log line for when a log was deleted mid session
     */
    static String getLogRecoveryDebugLine() {
        return LoggingUtil.getLogTimeTag() + "[DEBUG]: [Log was deleted during runtime,"
                + " recreating and restarting log at: " + TimeUtil.userTime() + CyderStrings.closingBracket;
    }
}
