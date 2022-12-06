package cyder.logging;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.enums.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.time.TimeUtil;
import cyder.utils.ArrayUtil;
import cyder.utils.StaticUtil;
import cyder.utils.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static cyder.constants.CyderStrings.*;

/** Utilities necessary for the Cyder logger. */
public final class LoggingUtil {
    /** The file that contains the Cyder signature to place at the top of log files. */
    private static final File cyderSignatureFile = StaticUtil.getStaticResource("cyder.txt");

    /** The list of lines from cyder.txt depicting a sweet Cyder Ascii art logo. */
    private static ImmutableList<String> cyderSignatureLines = ImmutableList.of();

    static {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(cyderSignatureFile))) {
            LinkedList<String> set = new LinkedList<>();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                set.add(line);
            }

            cyderSignatureLines = ImmutableList.copyOf(set);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /** The maximum number of chars per line of a log. */
    public static final int maxLogLineLength = 120;

    /**
     * Only check 10 chars to the left of a line unless we force a break regardless
     * of whether a space is at that char.
     */
    private static final int lineBreakInsertionTol = 10;

    /** The chars to check to split at before splitting in between a line at whatever character a split index falls on. */
    private static final ImmutableList<Character> breakChars = ImmutableList.of(
            ' ',
            '/',
            '\"',
            '\'',
            '\\',
            '-',
            '_',
            '.',
            '=',
            ',',
            ':'
    );

    /** The delay between JVM entry and starting the object creation logging thread. */
    static final int INITIAL_OBJECT_CREATION_LOGGER_TIMEOUT = 3000;

    /** Suppress default constructor. */
    private LoggingUtil() {
        throw new IllegalMethodException(ATTEMPTED_INSTANTIATION);
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

        if (StringUtil.isNullOrEmpty(logLine1) || StringUtil.isNullOrEmpty(logLine2)) {
            return logLine1.equals(logLine2);
        }

        if (matchesStandardLogLine(logLine1) && matchesStandardLogLine(logLine2)) {
            String timeTag1 = logLine1.substring(logLine1.indexOf(openingBracket),
                    logLine2.indexOf(closingBracket) + 1).trim();
            String timeTag2 = logLine2.substring(logLine2.indexOf(openingBracket),
                    logLine2.indexOf(closingBracket) + 1).trim();

            logLine1 = logLine1.replace(timeTag1, "");
            logLine2 = logLine2.replace(timeTag2, "");

            return logLine1.equalsIgnoreCase(logLine2);
        }

        return logLine1.equals(logLine2);
    }

    /**
     * Returns whether the provided line is a standard log line.
     *
     * @param line the line
     * @return whether the provided line is a standard log line.
     */
    public static boolean matchesStandardLogLine(String line) {
        Preconditions.checkNotNull(line);

        return CyderRegexPatterns.standardLogLinePattern.matcher(line).matches();
    }

    /**
     * Splits the provided string if it exceeds {@link LoggingUtil#maxLogLineLength} at convent places.
     *
     * @param line the line to split if needed
     * @return the list of strings
     */
    public static ImmutableList<String> checkLogLineLength(String line) {
        Preconditions.checkNotNull(line);

        LinkedList<String> lines = new LinkedList<>();

        while (line.length() > maxLogLineLength) {
            boolean breakInserted = false;

            for (char splitChar : breakChars) {
                if (line.charAt(maxLogLineLength) == splitChar) {
                    lines.add(line.substring(0, maxLogLineLength));
                    line = line.substring(maxLogLineLength);
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
            lines.add(line.substring(0, maxLogLineLength));
            line = line.substring(maxLogLineLength);
        }

        // Add remaining line
        lines.add(line);

        return ImmutableList.copyOf(lines);
    }

    /**
     * Attempts to find the index of the searchFor char within {@link #lineBreakInsertionTol} chars of the left of
     * the provided string. If found, returns the index of the first found searchFor char.
     *
     * @param line      the line to search through
     * @param searchFor the character to find
     * @return the index of the first searchFor char if found, -1 else
     */
    static int checkLeftForSplitChar(String line, char searchFor) {
        Preconditions.checkNotNull(line);
        Preconditions.checkArgument(!line.isEmpty());
        Preconditions.checkArgument(breakChars.contains(searchFor));

        int ret = -1;

        for (int i = maxLogLineLength - lineBreakInsertionTol ; i < maxLogLineLength ; i++) {
            if (line.charAt(i) == searchFor) {
                ret = i;
                break;
            }
        }

        return ret;
    }

    /**
     * Attempts to find the index of the searchFor char within {@link #lineBreakInsertionTol} chars of the right of
     * {@link #maxLogLineLength}. If found, returns the index of the first found searchFor char.
     *
     * @param line      the line to search through
     * @param searchFor the character to find
     * @return the index of the first searchFor char if found, -1 else
     */
    static int checkRightForSplitChar(String line, char searchFor) {
        Preconditions.checkNotNull(line);
        Preconditions.checkArgument(!line.isEmpty());
        Preconditions.checkArgument(breakChars.contains(searchFor));

        int ret = -1;

        for (int i = maxLogLineLength ; i < maxLogLineLength + lineBreakInsertionTol ; i++) {
            if (i >= line.length()) break;
            if (line.charAt(i) == searchFor) {
                ret = i;
                break;
            }
        }

        return ret;
    }

    /**
     * Returns the header logo lines for the top of log files.
     *
     * @return the header logo lines for the top of log files
     */
    static ImmutableList<String> getCyderSignatureLines() {
        return cyderSignatureLines;
    }

    /**
     * Generates and returns a log line for when a log was deleted mid session.
     *
     * @return returns a log line for when a log was deleted mid session
     */
    static String getLogRecoveryDebugLine() {
        String time = surroundWithBrackets(TimeUtil.getLogLineTime());
        String debug = surroundWithBrackets(LogTag.DEBUG.toString());
        String message = "Log was deleted during runtime, recreating and restarting log at: " + TimeUtil.userTime();

        return time + space + debug + colon + space + message;
    }

    /**
     * Surrounds the provided string with brackets.
     *
     * @param string the string to surround with brackets
     * @return the string with brackets surrounding it
     */
    static String surroundWithBrackets(String string) {
        Preconditions.checkNotNull(string);

        return CyderStrings.openingBracket + string + CyderStrings.closingBracket;
    }

    /**
     * Constructs the string for the beginning of log lines using the provided tags with the time tag
     * inserted in the first position.
     * <p>
     * Example, passing "Exception", "My Exception"
     * would return "[11-27-32.322] [Exception] [My Exception]:"
     *
     * @param tags the tags without brackets
     * @return the prepend for the beginning of a log line
     */
    static String constructTagsPrepend(String... tags) {
        Preconditions.checkNotNull(tags);
        Preconditions.checkArgument(!ArrayUtil.isEmpty(tags));

        return constructTagsPrepend(ArrayUtil.toList(tags));
    }

    /**
     * Constructs the string for the beginning of log lines using the provided tags with the time tag
     * inserted in the first position.
     * <p>
     * Example, passing a list with the contents of "Exception" and "My Exception"
     * would return "[11-27-32.322] [Exception] [My Exception]:"
     *
     * @param tags the tags without brackets
     * @return the prepend for the beginning of a log line
     */
    static String constructTagsPrepend(List<String> tags) {
        Preconditions.checkNotNull(tags);
        Preconditions.checkArgument(!tags.isEmpty());

        StringBuilder ret = new StringBuilder();

        ret.append(surroundWithBrackets(TimeUtil.getLogLineTime())).append(space);
        ArrayUtil.forEachElementExcludingLast(tag ->
                ret.append(surroundWithBrackets(tag)).append(space), tags);
        ret.append(surroundWithBrackets(tags.get(tags.size() - 1))).append(colon);

        return ret.toString();
    }

    /**
     * Counts the number of exceptions the provided log file contains.
     *
     * @param logFile the log file
     * @return the number of exception the provided log file contains
     */
    static int countExceptions(File logFile) {
        return countTags(logFile, LogTag.EXCEPTION.getLogName());
    }

    /**
     * Counts the number of threads ran in the provided log file.
     *
     * @param logFile the log file
     * @return the number of threads ran in the provided log file
     */
    static int countThreadsRan(File logFile) {
        return countTags(logFile, LogTag.THREAD_STARTED.getLogName());
    }

    /**
     * Counts the number of time the provided tag appears in the provided log file.
     * A tag, for example, is a specific string surrounded by brackets before the first
     * colon of a log line.
     *
     * @param logFile the log file
     * @param tag     the tag to count the occurrences of
     * @return the number of times the tag occurs in the provided log file
     */
    static int countTags(File logFile, String tag) {
        Preconditions.checkNotNull(logFile);
        Preconditions.checkArgument(logFile.isFile());
        Preconditions.checkArgument(FileUtil.validateExtension(logFile, Extension.LOG.getExtension()));
        Preconditions.checkNotNull(tag);
        Preconditions.checkArgument(!tag.isEmpty());

        ArrayList<String> fileLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileLines.add(line);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        int ret = 0;

        for (String line : fileLines) {
            for (String tags : extractTags(line)) {
                if (tags.contains(tag)) {
                    ret++;
                }
            }
        }

        return ret;
    }

    /**
     * Extracts all tags from the provided log line.
     * Note tags are strings which are surrounded with brackets before the first colon.
     *
     * @param logLine the log line.
     * @return the tags extracted from the log line
     */
    static ImmutableList<String> extractTags(String logLine) {
        ArrayList<String> ret = new ArrayList<>();

        if (logLine.contains(CyderStrings.colon)) {
            String firstPart = logLine.split(CyderStrings.colon)[0];

            while (firstPart.contains(CyderStrings.openingBracket)
                    && firstPart.contains(CyderStrings.closingBracket)) {
                int start = firstPart.indexOf(CyderStrings.openingBracket);
                int end = firstPart.indexOf(CyderStrings.closingBracket);

                String tag = firstPart.substring(start, end + 1);
                ret.add(tag);
                firstPart = firstPart.substring(end + 1);
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Generates a consolidation line for a line which is repeated back to back.
     *
     * @param line     the repeated line
     * @param numLines the number of times the line is repeated
     * @return the line
     */
    static String generateConsolidationLine(String line, int numLines) {
        return line + space + openingBracket + numLines + "x" + closingBracket;
    }
}
