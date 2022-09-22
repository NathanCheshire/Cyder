package cyder.logging;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.enums.ExitCondition;
import cyder.enums.IgnoreThread;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import cyder.utils.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static java.lang.System.out;

// todo checking animation test with other check marks
// todo checking animation kind of breaks checkbox groups

/**
 * Logger class used to log useful information about any Cyder instance from beginning at
 * runtime to exit at JVM termination.
 */
public final class Logger {
    /**
     * Suppress default constructor.
     */
    private Logger() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The counter used to log the number of objects created each deltaT seconds.
     */
    private static final AtomicInteger objectCreationCounter = new AtomicInteger();

    /**
     * The total number of objects created for an instance of Cyder.
     */
    private static int totalObjectsCreated = 0;

    /**
     * The counter used to log the number of exceptions thrown
     * and handled during this session of Cyder.
     */
    private static final AtomicInteger exceptionsCounter = new AtomicInteger();

    /**
     * The rate at which to log the amount of objects created since the last log.
     */
    private static final int OBJECT_LOG_FREQUENCY = 5000;

    /**
     * The maximum number of chars per line of a log.
     */
    private static final int MAX_LINE_LENGTH = 120;

    /**
     * The number of spaces to prepend to a continuation line. This ensures wrapped lines are
     * started after the header such as "[hh-mm-ss.SSSS] " above it.
     */
    private static final int NEWLINE_SPACE_OFFSET = 15;
    // todo make dynamic

    /**
     * Whether the current log should not be written to again.
     */
    private static boolean logConcluded;

    /**
     * Whether the logger has been initialized.
     */
    private static final AtomicBoolean logStarted = new AtomicBoolean();

    /**
     * The log calls that were requested to be logged before the logger was initialized
     * and are awaiting logger initialization.
     */
    private static final ArrayList<AwaitingLog> awaitingLogCalls = new ArrayList<>();

    /**
     * The file that is currently being written to on log calls.
     */
    private static File currentLog;

    /**
     * The absolute start time of Cyder, initialized at runtime.
     */
    public static final long START_TIME = System.currentTimeMillis();

    /**
     * A record to hold a log call which cannot be written due to the logger
     * not being initialized yet.
     */
    private record AwaitingLog(String line, LogTag tag) {}

    /**
     * The prefix for the missing tag error message.
     */
    private static final String MISSING_TAG_CASE_ERROR_MESSAGE = "Handle case not found; "
            + "you're probably an idiot and added an enum to LoggerTag but forgot to handle it Logger.log(), Tag = ";

    /**
     * Returns whether the log has started.
     *
     * @return whether the log has started
     */
    public static boolean hasLogStarted() {
        return logStarted.get();
    }

    /**
     * Prints the provided string to {@link System}s output stream.
     *
     * @param string the string to print
     */
    public static void println(String string) {
        out.println(string);
    }

    /**
     * The text for when a log call was invoked after the log had concluded.
     */
    private static final String LOG_CONCLUDED = "LOG CALL AFTER LOG CONCLUDED";

    @ForReadability
    public static boolean emptyOrNewline(String string) {
        Preconditions.checkNotNull(string);
        string = string.trim();

        return string.isEmpty() || string.equals("\n");
    }

    /**
     * The main log method to log an action associated with a type tag.
     *
     * @param tag            the type of data we are logging
     * @param representation the representation of the object
     * @param <T>            the object instance of representation
     */
    @SuppressWarnings("IfCanBeSwitch")
    public static <T> void log(LogTag tag, T representation) {
        if (logConcluded) {
            println(getLogTimeTag() + LogTag.constructLogTagPrepend(LOG_CONCLUDED) + representation);
            return;
        } else if (representation instanceof String string && emptyOrNewline(string)) {
            log(LogTag.DEBUG, "Attempted to log a new or empty line");
            return;
        }

        if (logStarted.get()) {
            logAwaitingLogCalls();
        }

        StringBuilder logBuilder = new StringBuilder(getLogTimeTag());

        switch (tag) {
            case CLIENT:
                logBuilder.append(LogTag.CLIENT.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case CONSOLE_OUT:
                logBuilder.append(LogTag.CONSOLE_OUT.constructLogTagPrepend());
                if (representation instanceof String) {
                    logBuilder.append(ConsoleOut.STRING.getLogTag());
                    logBuilder.append(representation);
                } else if (representation instanceof ImageIcon icon) {
                    logBuilder.append(LogTag.constructLogTagPrepend(ConsoleOut.IMAGE.getLogTag()));

                    int width = icon.getIconWidth();
                    int height = icon.getIconHeight();
                    Color dominantColor = ColorUtil.getDominantColor(icon);

                    logBuilder.append("Image: [").append(width).append("x")
                            .append(height).append("], dominant color: ")
                            .append(dominantColor);
                } else if (representation instanceof JComponent) {
                    logBuilder.append(LogTag.constructLogTagPrepend(ConsoleOut.J_COMPONENT.getLogTag()));
                    logBuilder.append(representation);
                } else {
                    logBuilder.append(LogTag.constructLogTagPrepend(ConsoleOut.UNKNOWN.getLogTag()));
                    logBuilder.append(representation);
                }
                break;
            case EXCEPTION:
                logBuilder.append(LogTag.EXCEPTION.constructLogTagPrepend());
                logBuilder.append(representation);
                exceptionsCounter.getAndIncrement();
                break;
            case LINK:
                logBuilder.append(LogTag.LINK.constructLogTagPrepend());
                if (representation instanceof File) {
                    logBuilder.append("[").append(FileUtil.getExtension((File) representation)).append("] ");
                }
                logBuilder.append(representation);
                break;
            case SUGGESTION:
                logBuilder.append(LogTag.SUGGESTION.constructLogTagPrepend()).append(representation);
                break;
            case SYSTEM_IO:
                logBuilder.append(LogTag.SYSTEM_IO.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case LOGIN:
                logBuilder.append(LogTag.LOGIN.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case LOGOUT:
                logBuilder.append(LogTag.LOGOUT.constructLogTagPrepend());
                logBuilder.append("[CyderUser = ").append(representation).append("]");
                break;
            case JVM_ARGS:
                logBuilder.append(LogTag.JVM_ARGS.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case JVM_ENTRY:
                logBuilder.append(LogTag.JVM_ENTRY.constructLogTagPrepend());
                logBuilder.append(representation);

                logStarted.set(true);

                break;
            case EXIT:
                logBuilder.append(LogTag.EXIT.constructLogTagPrepend());
                logBuilder.append("[RUNTIME] ");
                logBuilder.append(getRuntime());

                formatAndWriteLine(logBuilder.toString(), tag);

                StringBuilder eolBuilder = new StringBuilder();
                eolBuilder.append(getLogTimeTag());
                eolBuilder.append("[EOL]: ");
                eolBuilder.append("Log completed, exiting Cyder with exit code: ");

                if (representation instanceof ExitCondition condition) {
                    eolBuilder.append(condition.getCode());

                    eolBuilder.append(" [");
                    eolBuilder.append(condition.getDescription());
                    eolBuilder.append("], ");

                    eolBuilder.append(exceptionsCounter.get() == 0
                            ? "no exceptions thrown" : "exceptions thrown: " + exceptionsCounter.get());

                    eolBuilder.append(", total objects created: ")
                            .append(totalObjectsCreated)
                            .append("\n");

                    formatAndWriteLine(eolBuilder.toString(), tag);
                    logConcluded = true;
                } else {
                    throw new FatalException("Error parsing exit condition: " + representation);
                }

                return;
            case CORRUPTION:
                logBuilder.append(LogTag.CORRUPTION.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case DEBUG:
                logBuilder.append(LogTag.DEBUG.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case HANDLE_METHOD:
                logBuilder.append(LogTag.HANDLE_METHOD.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case WIDGET_OPENED:
                logBuilder.append(LogTag.WIDGET_OPENED.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case PREFERENCE_REFRESH:
                logBuilder.append(LogTag.PREFERENCE_REFRESH.constructLogTagPrepend());
                logBuilder.append("Key = ").append(representation);
                break;
            case THREAD:
                logBuilder.append(LogTag.THREAD.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case OBJECT_CREATION:
                if (!(representation instanceof String)) {
                    objectCreationCounter.incrementAndGet();
                    return;
                } else {
                    logBuilder.append(LogTag.constructLogTagPrepend("UNIQUE OBJECT CREATED"));
                    logBuilder.append(representation);
                }

                break;
            case AUDIO:
                logBuilder.append(LogTag.AUDIO.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case UI_ACTION:
                logBuilder.append(LogTag.UI_ACTION.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case CONSOLE_LOAD:
                logBuilder.append(LogTag.CONSOLE_LOAD.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case FONT_LOADED:
                logBuilder.append(LogTag.FONT_LOADED);
                logBuilder.append(representation);
                break;
            case THREAD_STATUS:
                if (representation instanceof String) {
                    logBuilder.append("THREAD STATUS POLLED");
                    logBuilder.append(representation);
                } else if (representation instanceof Thread) {
                    logBuilder.append(LogTag.THREAD_STATUS);
                    logBuilder.append("name = ").append(((Thread) representation).getName()).append(", state = ")
                            .append(((Thread) representation).getState());
                } else {
                    logBuilder.append("THREAD");
                    logBuilder.append(representation);
                }

                break;
            case CONSOLE_REDIRECTION:
                logBuilder.append(LogTag.CONSOLE_REDIRECTION.constructLogTagPrepend());
                logBuilder.append("console output was redirected to files/").append(representation);
                break;
            case CRUD_OP:
                logBuilder.append(LogTag.CRUD_OP.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case PROP_LOADED:
                logBuilder.append(LogTag.PROP_LOADED.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case LOADING_MESSAGE:
                logBuilder.append(LogTag.LOADING_MESSAGE.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case USER_GET:
                logBuilder.append(LogTag.USER_GET.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            default:
                throw new IllegalArgumentException(MISSING_TAG_CASE_ERROR_MESSAGE + tag);
        }

        String logLine = logBuilder.toString().trim();

        if (logLine.length() <= getLogTimeTag().trim().length()) {
            log(LogTag.EXCEPTION, "Log call resulted in nothing built; tag = " + tag);
        } else if (!logStarted.get()) {
            awaitingLogCalls.add(new AwaitingLog(logLine, tag));
        } else {
            formatAndWriteLine(logLine, tag);
        }
    }

    /**
     * The key used to obtain the prop value for whether past
     * logs should be deleted on the start of a new Cyder session.
     */
    private static final String WIPE_LOGS_ON_START = "wipe_logs_on_start";

    /**
     * Initializes the logger for logging by generating the log file, starts
     * the object creation logger, concludes un-concluded logs, consolidates past log lines,
     * and zips the past logs.
     */
    public static void initialize() {
        if (PropLoader.getBoolean(WIPE_LOGS_ON_START)) {
            OsUtil.deleteFile(OsUtil.buildFile(Dynamic.PATH, Dynamic.LOGS.getDirectoryName()));
        }

        generateAndSetLogFile();
        writeCyderAsciiArt();

        log(LogTag.JVM_ENTRY, OsUtil.getOsUsername());

        startObjectCreationLogger();
        concludeLogs();
        consolidateLogLines();
        zipPastLogs();
    }

    /**
     * Logs the calls within awaitingLogCalls.
     */
    private static void logAwaitingLogCalls() {
        for (AwaitingLog awaitingLog : awaitingLogCalls) {
            formatAndWriteLine(awaitingLog.line, awaitingLog.tag);
        }

        awaitingLogCalls.clear();
    }

    /**
     * Returns the current log file.
     *
     * @return the log file associated with the current session
     */
    public static File getCurrentLog() {
        return currentLog;
    }

    /**
     * The filename of the file that contains the Cyder signature to place at the top of log files.
     */
    private static final String SIGNATURE_FILE_NAME = "cyder.txt";

    /**
     * The list of lines from cyder.txt depicting a sweet Cyder Ascii art logo.
     */
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
     * Writes the lines contained in static/txt/cyder.txt to the current log file.
     */
    private static void writeCyderAsciiArt() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(currentLog, true))) {
            for (String line : headerLogoLines) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

            bufferedWriter.newLine();
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Creates the top level logs directory, the log sub-directory for today,
     * and the log file for this session if it is not generated or set.
     */
    private static void generateAndSetLogFile() {
        try {
            File logsDir = OsUtil.buildFile(Dynamic.PATH, Dynamic.LOGS.getDirectoryName());
            if (!logsDir.exists() && !logsDir.mkdir()) {
                throw new FatalException("Failed to create logs directory");
            }

            String logSubDirName = TimeUtil.logSubDirTime();
            File logSubDir = OsUtil.buildFile(Dynamic.PATH, Dynamic.LOGS.getDirectoryName(), logSubDirName);
            if (!logSubDir.exists() && !logSubDir.mkdir()) {
                throw new FatalException("Failed to create log directory for current day");
            }

            File proposedLogFile = new File(TimeUtil.logTime() + ".log");
            String uniqueFilename = FileUtil.findUniqueName(proposedLogFile, logSubDir);
            File logFile = OsUtil.buildFile(Dynamic.PATH,
                    Dynamic.LOGS.getDirectoryName(), logSubDirName, uniqueFilename);

            if (OsUtil.createFile(logFile, true)) {
                currentLog = logFile;
            } else {
                throw new FatalException("Log file not created");
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Formats and writes the line to the current log file.
     *
     * @param line the line to write to the current log file
     * @param tag  the tag which was used to handle the constructed string to write
     */
    private static synchronized void formatAndWriteLine(String line, LogTag tag) {
        line = line.trim();

        if (logFileDeletedMidRuntime()) {
            generateAndSetLogFile();
            writeLines(insertBreaks(getLogRecoveryDebugLine()));
        }

        if (tag != LogTag.EXCEPTION) {
            writeLines(insertBreaks(line));
        } else {
            writeLines(line.split("\n"));
        }

        println(line);
    }

    @ForReadability
    private static boolean logFileDeletedMidRuntime() {
        return !getCurrentLog().exists();
    }

    /**
     * Generates and returns a log line for when a log was deleted mid session.
     *
     * @return returns a log line for when a log was deleted mid session
     */
    @ForReadability
    private static String getLogRecoveryDebugLine() {
        return getLogTimeTag() + "[DEBUG]: [Log was deleted during runtime,"
                + " recreating and restarting log at: " + TimeUtil.userTime() + "]";
    }

    /**
     * Writes the lines to the current log file. The first one is not offset
     * whilst all lines after the first are offset by 11 spaces.
     *
     * @param lines the lines to write to the current log file
     */
    private static void writeLines(String... lines) {
        Preconditions.checkArgument(currentLog.exists());
        Preconditions.checkArgument(lines != null);
        Preconditions.checkArgument(lines.length > 0);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(currentLog, true))) {
            for (int i = 0 ; i < lines.length ; i++) {
                if (i != 0) {
                    bw.write(StringUtil.generateNSpaces(NEWLINE_SPACE_OFFSET));
                }

                // to be safe, remove new lines and trim even though there should be none
                bw.write(StringUtil.stripNewLinesAndTrim(lines[i]));
                bw.newLine();
            }
        } catch (Exception e) {
            log(LogTag.DEBUG, ExceptionHandler.getPrintableException(e));
        }
    }

    /**
     * Writes the lines to the current log file. The first one is not offset
     * whilst all lines after the first are offset by 11 spaces.
     *
     * @param lines the lines to write to the current log file
     */
    private static void writeLines(LinkedList<String> lines) {
        Preconditions.checkArgument(currentLog.exists());
        Preconditions.checkArgument(lines != null);
        Preconditions.checkArgument(!lines.isEmpty());

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(currentLog, true))) {
            for (int i = 0 ; i < lines.size() ; i++) {
                if (i != 0) {
                    bw.write(StringUtil.generateNSpaces(NEWLINE_SPACE_OFFSET));
                }

                // to be safe, remove new lines and trim even though there should be none
                bw.write(StringUtil.stripNewLinesAndTrim(lines.get(i)));
                bw.newLine();
            }
        } catch (Exception e) {
            log(LogTag.DEBUG, ExceptionHandler.getPrintableException(e));
        }
    }

    /**
     * The chars to check to split at before splitting in between a line at whatever character a split index falls on.
     */
    private static final ImmutableList<Character> BREAK_CHARS
            = ImmutableList.of(' ', '/', '\'', '-', '_', '.', '=', ',', ':');

    /**
     * Only check 10 chars to the left of a line unless we force a break regardless
     * of whether a space is at that char.
     */
    private static final int BREAK_INSERTION_TOL = 10;

    /**
     * Returns the provided string with line breaks inserted if needed to ensure
     * the line length does not surpass {@link Logger#MAX_LINE_LENGTH}.
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

                int leftSplitIndex = checkLeftForSplitChar(line, splitChar);
                if (leftSplitIndex != -1) {
                    lines.add(line.substring(0, leftSplitIndex));
                    line = line.substring(leftSplitIndex);
                    breakInserted = true;
                    break;
                }

                int rightSplitIndex = checkRightForSplitChar(line, splitChar);
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
    private static int checkLeftForSplitChar(String line, char splitChar) {
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
    private static int checkRightForSplitChar(String line, char splitChar) {
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
     * Calculates the run time of Cyder.
     *
     * @return the run time of Cyder
     */
    private static String getRuntime() {
        return TimeUtil.formatMillis(System.currentTimeMillis() - START_TIME);
    }

    /**
     * The extension for a zip file.
     */
    private static final String ZIP_EXTENSION = ".zip";

    /**
     * Zips the log files of the past.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void zipPastLogs() {
        File topLevelLogsDir = OsUtil.buildFile(Dynamic.PATH, Dynamic.LOGS.getDirectoryName());

        if (!topLevelLogsDir.exists()) {
            topLevelLogsDir.mkdir();
            return;
        }

        File[] subLogDirs = topLevelLogsDir.listFiles();

        if (subLogDirs == null || subLogDirs.length == 0) return;

        Arrays.stream(subLogDirs).forEach(subLogDir -> {
            // If it's not the current log and is not a zip file
            if (!FileUtil.getFilename(subLogDir.getName()).equals(TimeUtil.logSubDirTime())
                    && !FileUtil.getExtension(subLogDir).equalsIgnoreCase(ZIP_EXTENSION)) {
                if (new File(subLogDir.getAbsolutePath() + ZIP_EXTENSION).exists()) {
                    OsUtil.deleteFile(subLogDir);
                } else {
                    FileUtil.zip(subLogDir.getAbsolutePath(), subLogDir.getAbsolutePath() + ZIP_EXTENSION);
                }
            }
        });
    }

    /**
     * Consolidates the lines of all non-zipped files within the logs/SubLogDir directory.
     */
    public static void consolidateLogLines() {
        File logsDir = OsUtil.buildFile(Dynamic.PATH, Dynamic.LOGS.getDirectoryName());

        if (!logsDir.exists()) return;

        File[] subLogDirs = logsDir.listFiles();

        if (subLogDirs == null || subLogDirs.length == 0) return;

        Arrays.stream(subLogDirs)
                .filter(subLogDir -> !FileUtil.getExtension(subLogDir).equalsIgnoreCase(ZIP_EXTENSION))
                .forEach(subLogDir -> {
                    File[] logFiles = subLogDir.listFiles();

                    if (logFiles != null && logFiles.length > 0) {
                        Arrays.stream(logFiles).forEach(Logger::consolidateLines);
                    }
                });
    }

    /**
     * A pattern for the start of a standard log line used to find the time of the log call.
     */
    private static final Pattern standardLogLine =
            Pattern.compile("\\s*\\[\\d+-\\d+-\\d+\\.\\d+]\\s*.*");

    /**
     * Consolidates duplicate lines next to each other of the provided file.
     *
     * @param file the file to consolidate duplicate lines of
     */
    private static void consolidateLines(File file) {
        Preconditions.checkArgument(file.exists(), "Provided file does not exist: " + file);
        Preconditions.checkArgument(FileUtil.getExtension(file).equalsIgnoreCase(".log"),
                "Provided file does not exist: " + file);

        boolean beforeFirstTimeTag = true;

        ArrayList<String> prelines = new ArrayList<>();
        ArrayList<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (beforeFirstTimeTag) {
                    prelines.add(line);
                } else if (!StringUtil.stripNewLinesAndTrim(line).isEmpty()) {
                    lines.add(line);
                }

                if (standardLogLine.matcher(line).matches()) {
                    beforeFirstTimeTag = false;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        if (lines.size() < 2) {
            return;
        }

        ArrayList<String> writeLines = new ArrayList<>();

        String lastLine;
        String currentLine;
        int currentCount = 1;

        for (int i = 0 ; i < lines.size() - 1 ; i++) {
            lastLine = lines.get(i);
            currentLine = lines.get(i + 1);

            if (areLogLinesEquivalent(lastLine, currentLine)) {
                currentCount++;
            } else {
                if (currentCount > 1) {
                    writeLines.add(lastLine + " [" + currentCount + "x]");
                } else {
                    writeLines.add(lastLine);
                }

                currentCount = 1;
            }
        }

        if (currentCount > 1) {
            writeLines.add(lines.get(lines.size() - 1) + " [" + currentCount + "x]");
        } else {
            writeLines.add(lines.get(lines.size() - 1));
        }

        // prelines
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String line : prelines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        // actual log lines
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            for (String line : writeLines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns whether the two log lines are equivalent.
     *
     * @param logLine1 the first log line
     * @param logLine2 the second log line
     * @return whether the two log lines are equivalent
     */
    private static boolean areLogLinesEquivalent(String logLine1, String logLine2) {
        Preconditions.checkNotNull(logLine1);
        Preconditions.checkNotNull(logLine2);

        // if not full line tags, directly compare
        if (!logLine1.startsWith("[")
                || !logLine1.contains("]")
                || !logLine2.contains("]")
                || !logLine2.startsWith("["))
            return logLine1.equals(logLine2);

        // guaranteed to have square braces now
        String timeTag1 = logLine1.substring(logLine1.indexOf("["), logLine2.indexOf("]") + 1).trim();
        String timeTag2 = logLine2.substring(logLine2.indexOf("["), logLine2.indexOf("]") + 1).trim();

        logLine1 = logLine1.replace(timeTag1, "");
        logLine2 = logLine2.replace(timeTag2, "");

        return !StringUtil.isNullOrEmpty(logLine1) && !StringUtil.isNullOrEmpty(logLine2) &&
                logLine1.equalsIgnoreCase(logLine2);
    }

    /**
     * Fixes any logs lacking/not ending in an EOL tag.
     */
    public static void concludeLogs() {
        try {
            File logDir = OsUtil.buildFile(Dynamic.PATH, Dynamic.LOGS.getDirectoryName());

            if (!logDir.exists()) return;

            File[] logDirs = logDir.listFiles();

            if (logDirs == null || logDirs.length == 0) return;

            for (File subLogDir : logDirs) {
                if (FileUtil.getExtension(subLogDir).equalsIgnoreCase(ZIP_EXTENSION)) continue;

                File[] logs = subLogDir.listFiles();

                if (logs == null || logs.length == 0) return;

                for (File log : logs) {
                    if (!log.equals(getCurrentLog())) {
                        BufferedReader reader = new BufferedReader(new FileReader(log));
                        String line;
                        boolean containsEOL = false;

                        int exceptions = 0;

                        while ((line = reader.readLine()) != null) {
                            if (line.contains("[EOL]") || line.contains("[EXTERNAL STOP]")) {
                                containsEOL = true;
                                break;
                            } else if (line.contains("[EXCEPTION]")) {
                                exceptions++;
                            }
                        }

                        reader.close();

                        if (!containsEOL) {
                            /*
                             Usually an IDE stop but sometimes the program exits,
                             with exit condition 1 due to something failing on startup
                             which is why this says "crashed unexpectedly"
                             */
                            String logBuilder = getLogTimeTag() + "[EOL]: " +
                                    "Log completed, Cyder crashed unexpectedly: " +
                                    "exit code: " + ExitCondition.ExternalStop.getCode() +
                                    " " + ExitCondition.ExternalStop.getDescription() +
                                    ", exceptions thrown: " + exceptions;

                            Files.write(Paths.get(log.getAbsolutePath()),
                                    (logBuilder).getBytes(), StandardOpenOption.APPEND);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * The delay between JVM entry and starting the object creation logging thread.
     */
    private static final int INITIAL_OBJECT_CREATION_LOGGER_TIMEOUT = 3000;

    /**
     * Starts the object creation logger to log object creation calls every deltaT seconds.
     */
    private static void startObjectCreationLogger() {
        CyderThreadRunner.submit(() -> {
            try {
                ThreadUtil.sleep(INITIAL_OBJECT_CREATION_LOGGER_TIMEOUT);

                while (true) {
                    if (objectCreationCounter.get() > 0) {
                        int objectsCreated = objectCreationCounter.getAndSet(0);
                        totalObjectsCreated += objectsCreated;

                        formatAndWriteLine(getLogTimeTag() + "[OBJECT CREATION]: "
                                + "Objects created since last delta (" + OBJECT_LOG_FREQUENCY + "ms): "
                                + objectsCreated, LogTag.OBJECT_CREATION);
                    }

                    ThreadUtil.sleep(OBJECT_LOG_FREQUENCY);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, IgnoreThread.ObjectCreationLogger.getName());
    }

    /**
     * Returns the time tag placed at the beginning of all log statements.
     * Example: "[22-12-39] "
     *
     * @return the time tag placed at the beginning of all log statements
     */
    private static String getLogTimeTag() {
        return "[" + TimeUtil.getLogLineTime() + "] ";
    }

}
