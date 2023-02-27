package cyder.logging;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.constants.CyderRegexPatterns;
import cyder.enumerations.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.time.TimeUtil;
import cyder.utils.ArrayUtil;
import cyder.utils.ReflectionUtil;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import static cyder.logging.LoggingConstants.*;
import static cyder.strings.CyderStrings.*;

/**
 * Utilities necessary for the Cyder {@link Logger}.
 */
public final class LoggingUtil {
    /**
     * Suppress default constructor.
     */
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
    static boolean areLogLinesEquivalent(String logLine1, String logLine2) {
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
    static boolean matchesStandardLogLine(String line) {
        Preconditions.checkNotNull(line);

        return CyderRegexPatterns.standardLogLinePattern.matcher(line).matches();
    }

    /**
     * Splits the provided string if it exceeds {@link LoggingConstants#maxLogLineLength} at convent places.
     *
     * @param line the line to split if needed
     * @return the list of strings
     */
    static ImmutableList<String> checkLogLineLength(String line) {
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

        if (!line.isEmpty()) lines.add(line);

        return ImmutableList.copyOf(lines);
    }

    /**
     * Generates and returns a log line for when a log was deleted mid session.
     *
     * @return returns a log line for when a log was deleted mid session
     */
    static String getLogRecoveryDebugLine() {
        String time = surroundWithBrackets(TimeUtil.getLogLineTime());
        String debug = surroundWithBrackets(LogTag.DEBUG.toString());
        String message =
                "Log was deleted during runtime, recreating and restarting log at: " + TimeUtil.userReadableTime();

        return time + space + debug + colon + space + message;
    }

    /**
     * Constructs the string for the beginning of log lines using the provided tags with the time tag
     * inserted in the first position.
     * <p>
     * Example, passing "Exception", "My Exception"
     * would return "[11-27-32.322] [Exception] [My Exception]:"
     *
     * @param tag  the first tag without any brackets
     * @param tags the additional tags without brackets
     * @return the prepend for the beginning of a log line
     */
    static String constructTagsPrepend(String tag, String... tags) {
        Preconditions.checkNotNull(tag);
        Preconditions.checkArgument(!tag.isEmpty());

        for (String tagsTag : tags) {
            Preconditions.checkNotNull(tagsTag);
            Preconditions.checkArgument(!tagsTag.isEmpty());
        }

        ImmutableList<String> tagsList = new ImmutableList.Builder<String>()
                .add(tag)
                .addAll(ArrayUtil.toList(tags)).build();

        return constructTagsPrepend(tagsList);
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
        for (String tag : tags) {
            Preconditions.checkNotNull(tag);
            Preconditions.checkArgument(!tag.isEmpty());
        }

        StringBuilder ret = new StringBuilder();

        ret.append(surroundWithBrackets(TimeUtil.getLogLineTime())).append(space);
        ArrayUtil.forEachElementExcludingLast(tag ->
                ret.append(surroundWithBrackets(tag)).append(space), tags);
        ret.append(surroundWithBrackets(tags.get(tags.size() - 1))).append(colon).append(space);

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
        return extractLinesWithTag(logFile, tag).size();
    }

    /**
     * Extracts and returns a list of all lines in the provided log file which contains the provided tag.
     *
     * @param logFile the log file
     * @param tag     the tag to find
     * @return the lines containing the provided tag
     */
    static ImmutableList<String> extractLinesWithTag(File logFile, String tag) {
        Preconditions.checkNotNull(logFile);
        Preconditions.checkArgument(logFile.isFile());
        Preconditions.checkArgument(FileUtil.validateExtension(logFile, Extension.LOG.getExtension()));
        Preconditions.checkNotNull(tag);
        Preconditions.checkArgument(!tag.isEmpty());

        ArrayList<String> ret = new ArrayList<>();

        for (String line : FileUtil.getFileLines(logFile)) {
            for (String tags : extractTags(line)) {
                if (tags.contains(tag)) {
                    ret.add(line);
                    break;
                }
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Extracts all tags from the provided log line.
     * Note tags are strings which are surrounded with brackets before
     * the first colon, thus, this includes the time tag.
     *
     * @param logLine the log line.
     * @return the tags extracted from the log line
     */
    static ImmutableList<String> extractTags(String logLine) {
        Preconditions.checkNotNull(logLine);

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
        Preconditions.checkNotNull(line);
        Preconditions.checkArgument(!line.isEmpty());
        Preconditions.checkArgument(numLines > 0);

        String tag = openingBracket + numLines + X + closingBracket;

        if (line.contains(colon)) {
            StringBuilder builder = new StringBuilder(line);
            builder.insert(line.indexOf(colon), space + tag);
            return builder.toString();
        } else {
            return line + space + tag;
        }
    }

    /**
     * Writes the Cyder Ascii art from {@link LoggingConstants#cyderSignatureLines} to the provided file.
     *
     * @param file the file to write the ascii art to
     */
    static void writeCyderAsciiArtToFile(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isFile());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (String line : cyderSignatureLines) {
                writer.write(line);
                writer.newLine();
            }

            for (int i = 0 ; i < numNewLinesAfterCyderAsciiArt ; i++) writer.newLine();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Writes the bootstrap Ascii art to the provided file.
     *
     * @param file the file to write the boostrap Ascii art to
     */
    static void writeBoostrapAsciiArtToFile(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isFile());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (int i = 0 ; i < numNewLinesBeforeAndAfterBoostrapArt ; i++) writer.newLine();

            for (String line : boostrapLines) {
                writer.write(line);
                writer.newLine();
            }

            for (int i = 0 ; i < numNewLinesBeforeAndAfterBoostrapArt ; i++) writer.newLine();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Counts the number of objects created during the provided log.
     *
     * @param logFile the log file
     * @return the number of objects created during the provided log
     */
    static long countObjectsCreatedFromLog(File logFile) {
        Preconditions.checkNotNull(logFile);
        Preconditions.checkArgument(logFile.exists());
        Preconditions.checkArgument(logFile.isFile());
        Preconditions.checkArgument(FileUtil.validateExtension(logFile, Extension.LOG.getExtension()));

        ImmutableList<String> objectCreationLines = extractLinesWithTag(logFile, LogTag.OBJECT_CREATION.getLogName());

        long totalObjects = 0;

        for (String objectCreationLine : objectCreationLines) {
            Matcher matcher = objectsCreatedSinceLastDeltaPattern.matcher(objectCreationLine);
            if (matcher.matches()) {
                String objectsGroup = matcher.group(3); // todo magic number

                int lineObjects = 0;
                try {
                    lineObjects = Integer.parseInt(objectsGroup);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }

                totalObjects += lineObjects;
            }
        }

        return totalObjects;
    }

    /**
     * Computes and returns the runtime of the provided log file using the first and last found time tags.
     *
     * @param logFile the log file
     * @return the runtime in ms of the log file
     */
    static long getRuntimeFromLog(File logFile) {
        Preconditions.checkNotNull(logFile);
        Preconditions.checkArgument(logFile.exists());
        Preconditions.checkArgument(logFile.isFile());
        Preconditions.checkArgument(FileUtil.validateExtension(logFile, Extension.LOG.getExtension()));

        long ret = 0;

        String firstTimeString = "";
        String lastTimeString = "";

        for (String line : FileUtil.getFileLines(logFile)) {
            Matcher matcher = CyderRegexPatterns.standardLogLinePattern.matcher(line);
            if (matcher.matches()) {
                if (StringUtil.isNullOrEmpty(firstTimeString)) {
                    firstTimeString = matcher.group(1);
                }

                lastTimeString = matcher.group(1);
            }
        }

        if (!StringUtil.isNullOrEmpty(firstTimeString) && !StringUtil.isNullOrEmpty(lastTimeString)) {
            try {
                Date firstTimeDate = TimeUtil.LOG_LINE_TIME_FORMAT.parse(firstTimeString);
                Date lastTimeDate = TimeUtil.LOG_LINE_TIME_FORMAT.parse(lastTimeString);

                ret = lastTimeDate.getTime() - firstTimeDate.getTime();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }

        return ret;
    }

    /**
     * Returns the lines of the log file excluding any empty lines/Cyder Ascii art lines.
     *
     * @param logFile the log file
     * @return the lines of the log file excluding any empty lines/Cyder Ascii art lines
     */
    static ImmutableList<String> getLogLinesFromLog(File logFile) {
        Preconditions.checkNotNull(logFile);
        Preconditions.checkArgument(logFile.exists());
        Preconditions.checkArgument(logFile.isFile());
        Preconditions.checkArgument(FileUtil.validateExtension(logFile, Extension.LOG.getExtension()));

        boolean beforeFirstTimeTag = true;

        ArrayList<String> ret = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (CyderRegexPatterns.standardLogLinePattern.matcher(line).matches()) {
                    beforeFirstTimeTag = false;
                }

                if (!StringUtil.stripNewLinesAndTrim(line).isEmpty() && !beforeFirstTimeTag) {
                    ret.add(line);
                }
            }
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Surrounds the provided string with brackets.
     *
     * @param string the string to surround with brackets
     * @return the string with brackets surrounding it
     */
    public static String surroundWithBrackets(String string) {
        Preconditions.checkNotNull(string);

        return CyderStrings.openingBracket + string + CyderStrings.closingBracket;
    }

    /**
     * Returns whether the provided object or type should not be logged when serialized/deserialized.
     *
     * @param classOrType the class or type
     * @param <T>         the type, one of Class or type
     * @return whether the provided object or type should not be logged
     */
    static <T> boolean shouldIgnoreObjectSerializationOrDeserialization(T classOrType) {
        if (classOrType instanceof Class<?> clazz) {
            return StringUtil.in(ReflectionUtil.getBottomLevelClass(clazz),
                    true, Props.ignoreSerializationData.getValue().getList());
        } else if (classOrType instanceof Type type) {
            return StringUtil.in(type.getTypeName(),
                    true, Props.ignoreSerializationData.getValue().getList());
        }

        return true;
    }

    /**
     * Attempts to find the index of the searchFor char within {@link LoggingConstants#lineBreakInsertionTol}
     * chars of the left of the provided string. If found, returns the index of the first found searchFor char.
     *
     * @param line      the line to search through
     * @param searchFor the character to find
     * @return the index of the first searchFor char if found, -1 else
     */
    @ForReadability
    private static int checkLeftForSplitChar(String line, char searchFor) {
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
     * Attempts to find the index of the searchFor char within {@link LoggingConstants#lineBreakInsertionTol}
     * chars of the right of {@link LoggingConstants#maxLogLineLength}.
     * If found, returns the index of the first found searchFor char.
     *
     * @param line      the line to search through
     * @param searchFor the character to find
     * @return the index of the first searchFor char if found, -1 else
     */
    @ForReadability
    private static int checkRightForSplitChar(String line, char searchFor) {
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
}
