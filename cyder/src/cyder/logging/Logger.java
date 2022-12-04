package cyder.logging;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.enums.ExitCondition;
import cyder.enums.Extension;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.props.Props;
import cyder.threads.CyderThreadRunner;
import cyder.threads.IgnoreThread;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import cyder.utils.ColorUtil;
import cyder.utils.OsUtil;
import cyder.utils.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.out;

/**
 * Logger class used to log useful information about any Cyder instance from beginning at
 * runtime to exit at JVM termination.
 */
public final class Logger {
    /** Suppress default constructor. */
    private Logger() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /** The counter used to log the number of objects created each deltaT seconds. */
    private static final AtomicInteger objectCreationCounter = new AtomicInteger();

    /** The total number of objects created for an instance of Cyder. */
    private static int totalObjectsCreated = 0;

    /**
     * The counter used to log the number of exceptions thrown
     * and handled during this session of Cyder.
     */
    private static final AtomicInteger exceptionsCounter = new AtomicInteger();

    /** The rate at which to log the amount of objects created since the last log. */
    private static final int OBJECT_LOG_FREQUENCY = 5000;

    // todo why not use method for this?
    /**
     * The number of spaces to prepend to a continuation line. This ensures wrapped lines are
     * started after the header such as "[hh-mm-ss.SSSS] " above it.
     */
    private static final int continuationLineOffset = TimeUtil.getLogLineTime().length() + 3;

    /** Whether the current log should not be written to again. */
    private static boolean logConcluded;

    /** Whether the logger has been initialized. */
    private static final AtomicBoolean logStarted = new AtomicBoolean();

    /**
     * The log calls that were requested to be logged before the logger was initialized
     * and are awaiting logger initialization.
     */
    private static final ArrayList<AwaitingLog> awaitingLogCalls = new ArrayList<>();

    /** The file that is currently being written to on log calls. */
    private static File currentLog;

    /**
     * A record to hold a log call which cannot be written due to the logger
     * not being initialized yet.
     */
    private record AwaitingLog(String line, LogTag tag) {}

    /** The prefix for the missing tag error message. */
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

    /** The text for when a log call was invoked after the log had concluded. */
    private static final String LOG_CONCLUDED = "LOG CALL AFTER LOG CONCLUDED";

    /**
     * Logs the provided statement to the log file, using the calling class name as the tag.
     *
     * @param statement the statement to log
     * @param <T>       the type of statement
     */
    public static <T> void log(T statement) {
        // todo use bottom level class name as tag
        System.out.println(statement);
    }

    /**
     * The main log method to log an action associated with a type tag.
     *
     * @param tag       the type of data we are logging
     * @param statement the statement of the object
     * @param <T>       the object instance of statement
     */
    @SuppressWarnings("IfCanBeSwitch") /* Readability */
    public static <T> void log(LogTag tag, T statement) {
        if (logConcluded) {
            println(LoggingUtil.getLogTimeTag() + LogTag.constructLogTagPrepend(LOG_CONCLUDED) + statement);
            return;
        } else if (statement instanceof String string && StringUtil.isNullOrEmpty(string)) {
            return;
        }

        if (logStarted.get()) {
            logAwaitingLogCalls();
        }

        StringBuilder logBuilder = new StringBuilder(LoggingUtil.getLogTimeTag());

        switch (tag) {
            case CLIENT:
                logBuilder.append(LogTag.CLIENT.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case CONSOLE_OUT:
                logBuilder.append(LogTag.CONSOLE_OUT.constructLogTagPrepend());
                if (statement instanceof String) {
                    logBuilder.append(ConsoleOutType.STRING.getLogTag());
                    logBuilder.append(statement);
                } else if (statement instanceof ImageIcon icon) {
                    logBuilder.append(ConsoleOutType.IMAGE.getLogTag());

                    int width = icon.getIconWidth();
                    int height = icon.getIconHeight();
                    Color dominantColor = ColorUtil.getDominantColor(icon);

                    logBuilder.append("Image: [").append(width).append("x")
                            .append(height).append("], dominant color: ")
                            .append(dominantColor);
                } else if (statement instanceof JComponent) {
                    logBuilder.append(ConsoleOutType.J_COMPONENT.getLogTag());
                    logBuilder.append(statement);
                } else {
                    logBuilder.append(LogTag.constructLogTagPrepend(
                            StringUtil.capsFirstWords(statement.getClass().toString())));
                    logBuilder.append(statement);
                }
                break;
            case EXCEPTION:
                logBuilder.append(LogTag.EXCEPTION.constructLogTagPrepend());
                logBuilder.append(statement);
                exceptionsCounter.getAndIncrement();
                break;
            case LINK:
                logBuilder.append(LogTag.LINK.constructLogTagPrepend());
                if (statement instanceof File) {
                    logBuilder.append(CyderStrings.openingBracket)
                            .append(FileUtil.getExtension((File) statement)).append("] ");
                }
                logBuilder.append(statement);
                break;
            case SUGGESTION:
                logBuilder.append(LogTag.SUGGESTION.constructLogTagPrepend()).append(statement);
                break;
            case SYSTEM_IO:
                logBuilder.append(LogTag.SYSTEM_IO.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case LOGIN_FIELD:
                logBuilder.append(LogTag.LOGIN_FIELD.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case LOGIN_OUTPUT:
                logBuilder.append(LogTag.LOGIN_OUTPUT.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case LOGOUT:
                logBuilder.append(LogTag.LOGOUT.constructLogTagPrepend());
                logBuilder.append(CyderStrings.openingBracket).append("CyderUser = ")
                        .append(statement).append(CyderStrings.closingBracket);
                break;
            case JVM_ARGS:
                logBuilder.append(LogTag.JVM_ARGS.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case JVM_ENTRY:
                logBuilder.append(LogTag.JVM_ENTRY.constructLogTagPrepend());
                logBuilder.append(statement);

                logStarted.set(true);

                break;
            case EXIT:
                logBuilder.append(LogTag.EXIT.constructLogTagPrepend());
                logBuilder.append("Runtime: ");
                logBuilder.append(getRuntime());
                formatAndWriteLine(logBuilder.toString(), tag);

                StringBuilder eolBuilder = new StringBuilder();
                eolBuilder.append(LoggingUtil.getLogTimeTag());
                eolBuilder.append("[EOL]: ");
                eolBuilder.append("Log completed, exiting Cyder with exit code: ");

                String exitCodeRepresentation;
                if (statement instanceof ExitCondition exitCondition) {
                    exitCodeRepresentation = exitCondition.getCode() + " [" + exitCondition.getDescription() + "], ";
                } else {
                    exitCodeRepresentation = "Error parsing exit condition: " + statement + ", ";
                }

                eolBuilder.append(exitCodeRepresentation);

                eolBuilder.append(exceptionsCounter.get() == 0
                        ? "no exceptions thrown"
                        : "exceptions thrown: " + exceptionsCounter.get());

                eolBuilder.append(", total objects created: ")
                        .append(totalObjectsCreated)
                        .append(", threads ran: ")
                        .append(CyderThreadRunner.getThreadsRan())
                        .append(CyderStrings.newline);

                formatAndWriteLine(eolBuilder.toString(), tag);
                logConcluded = true;

                return;
            case USER_CORRUPTION:
                logBuilder.append(LogTag.USER_CORRUPTION.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case DEBUG:
                logBuilder.append(LogTag.DEBUG.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case HANDLE_METHOD:
                logBuilder.append(LogTag.HANDLE_METHOD.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case WIDGET_OPENED:
                logBuilder.append(LogTag.WIDGET_OPENED.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case PREFERENCE:
                logBuilder.append(LogTag.PREFERENCE.constructLogTagPrepend());
                logBuilder.append("Key = ").append(statement);
                break;
            case THREAD_STARTED:
                logBuilder.append(LogTag.THREAD_STARTED.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case OBJECT_CREATION:
                if (!(statement instanceof String)) {
                    objectCreationCounter.incrementAndGet();
                    return;
                } else {
                    logBuilder.append(LogTag.constructLogTagPrepend("UNIQUE OBJECT CREATED"));
                    logBuilder.append(statement);
                }

                break;
            case AUDIO:
                logBuilder.append(LogTag.AUDIO.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case UI_ACTION:
                logBuilder.append(LogTag.UI_ACTION.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case CONSOLE_LOAD:
                logBuilder.append(LogTag.CONSOLE_LOAD.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case FONT_LOADED:
                logBuilder.append(LogTag.FONT_LOADED);
                logBuilder.append(statement);
                break;
            case THREAD_STATUS:
                if (statement instanceof String) {
                    logBuilder.append("THREAD STATUS POLLED");
                    logBuilder.append(statement);
                } else if (statement instanceof Thread) {
                    logBuilder.append(LogTag.THREAD_STATUS);
                    logBuilder.append("name = ").append(((Thread) statement).getName()).append(", state = ")
                            .append(((Thread) statement).getState());
                } else {
                    logBuilder.append("THREAD");
                    logBuilder.append(statement);
                }

                break;
            case CONSOLE_REDIRECTION:
                logBuilder.append(LogTag.CONSOLE_REDIRECTION.constructLogTagPrepend());
                logBuilder.append("console output was redirected to files/").append(statement);
                break;
            case LOADING_MESSAGE:
                logBuilder.append(LogTag.LOADING_MESSAGE.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case USER_GET:
                logBuilder.append(LogTag.USER_GET.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case PROPS_ACTION:
                logBuilder.append(LogTag.PROPS_ACTION.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case PYTHON:
                logBuilder.append(LogTag.PYTHON.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case NETWORK:
                logBuilder.append(LogTag.NETWORK.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case HANDLE_WARNING:
                logBuilder.append(LogTag.HANDLE_WARNING.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case WATCHDOG:
                logBuilder.append(LogTag.WATCHDOG.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case WIDGET_WARNING:
                logBuilder.append(LogTag.WIDGET_WARNING.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case VANILLA_WARNING:
                logBuilder.append(LogTag.VANILLA_WARNING.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case GUI_TEST_WARNING:
                logBuilder.append(LogTag.GUI_TEST_WARNING.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            case CYDER_TEST_WARNING:
                logBuilder.append(LogTag.CYDER_TEST_WARNING.constructLogTagPrepend());
                logBuilder.append(statement);
                break;
            default:
                throw new IllegalArgumentException(MISSING_TAG_CASE_ERROR_MESSAGE + tag);
        }

        String logLine = logBuilder.toString().trim();

        if (logLine.length() <= LoggingUtil.getLogTimeTag().trim().length()) {
            log(LogTag.EXCEPTION, "Log call resulted in nothing built; tag = " + tag);
        } else if (!logStarted.get()) {
            awaitingLogCalls.add(new AwaitingLog(logLine, tag));
        } else {
            formatAndWriteLine(logLine, tag);
        }
    }

    /**
     * Initializes the logger for logging by invoking the following actions:
     *
     * <ul>
     *     <li>Wiping past logs if enabled</li>
     *     <li>Generating and setting the current log file</li>
     *     <li>Logging the JVM entry with the OS' username</li>
     *     <li>Starting the object creation logger</li>
     *     <li>Concluding past logs which may have ended abruptly</li>
     *     <li>Consolidating past log lines</li>
     *     <li>Zipping past logs</li>
     * </ul>
     */
    public static void initialize() {
        if (Props.wipeLogsOnStart.getValue()) {
            OsUtil.deleteFile(Dynamic.buildDynamic(Dynamic.LOGS.getDirectoryName()));
        }

        generateAndSetLogFile();
        writeCyderAsciiArtToCurrentLogFile();
        log(LogTag.JVM_ENTRY, OsUtil.getOsUsername());
        startObjectCreationLogger();
        concludeLogs();
        consolidateLogLines();
        zipPastLogs();
    }

    /** Logs the calls within awaitingLogCalls. */
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
    public static File getCurrentLogFile() {
        return currentLog;
    }

    /** The number of new lines to write after ascii art is written to a log file. */
    private static final int numNewLinesAfterAsciiArt = 2;

    /** Writes the lines contained in static/txt/cyder.txt to the current log file. */
    private static void writeCyderAsciiArtToCurrentLogFile() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(currentLog, true))) {
            for (String line : LoggingUtil.getCyderSignatureLines()) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

            for (int i = 0 ; i < numNewLinesAfterAsciiArt ; i++) {
                bufferedWriter.newLine();
            }

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
            File logsDir = Dynamic.buildDynamic(Dynamic.LOGS.getDirectoryName());
            if (!logsDir.exists() && !logsDir.mkdir()) {
                throw new FatalException("Failed to create logs directory");
            }

            String logSubDirName = TimeUtil.logSubDirTime();
            File logSubDir = Dynamic.buildDynamic(Dynamic.LOGS.getDirectoryName(), logSubDirName);
            if (!logSubDir.exists() && !logSubDir.mkdir()) {
                throw new FatalException("Failed to create log directory for current day");
            }

            File proposedLogFile = new File(TimeUtil.logTime() + Extension.LOG.getExtension());
            String uniqueFilename = FileUtil.constructUniqueName(proposedLogFile, logSubDir);
            File logFile = Dynamic.buildDynamic(
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
            writeLines(LoggingUtil.ensureProperLength(LoggingUtil.getLogRecoveryDebugLine()));
        }

        if (tag != LogTag.EXCEPTION) {
            writeLines(LoggingUtil.ensureProperLength(line));
        } else {
            writeLines(line.split(CyderStrings.newline));
        }

        println(line);
    }

    @ForReadability
    private static boolean logFileDeletedMidRuntime() {
        return !getCurrentLogFile().exists();
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
                    bw.write(StringUtil.generateSpaces(continuationLineOffset));
                }

                // to be safe, remove new lines and trim even though there should be none
                bw.write(StringUtil.stripNewLinesAndTrim(lines[i]));
                bw.newLine();
            }
        } catch (Exception e) {
            log(LogTag.EXCEPTION, ExceptionHandler.getPrintableException(e));
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
                    bw.write(StringUtil.generateSpaces(continuationLineOffset));
                }

                // to be safe, remove new lines and trim even though there should be none
                bw.write(StringUtil.stripNewLinesAndTrim(lines.get(i)));
                bw.newLine();
            }
        } catch (Exception e) {
            log(LogTag.EXCEPTION, ExceptionHandler.getPrintableException(e));
        }
    }

    /**
     * Calculates the run time of Cyder.
     *
     * @return the run time of Cyder
     */
    private static String getRuntime() {
        return TimeUtil.formatMillis(ManagementFactory.getRuntimeMXBean().getUptime());
    }

    /** Zips the log files of the past. */
    private static void zipPastLogs() {
        File topLevelLogsDir = Dynamic.buildDynamic(Dynamic.LOGS.getDirectoryName());

        if (!topLevelLogsDir.exists()) {
            if (!topLevelLogsDir.mkdir()) {
                throw new FatalException("Failed to create logs dir");
            }

            return;
        }

        File[] subLogDirs = topLevelLogsDir.listFiles();
        if (subLogDirs == null || subLogDirs.length == 0) return;

        Arrays.stream(subLogDirs)
                .filter(subLogDir -> !subLogDir.getAbsolutePath().equals(getCurrentLogFile().getAbsolutePath()))
                .filter(subLogDir -> !FileUtil.getExtension(subLogDir).equals(Extension.ZIP.getExtension()))
                .forEach(subLogDir -> {
                    String destinationZipPath = subLogDir.getAbsolutePath() + Extension.ZIP.getExtension();
                    File destinationZip = new File(destinationZipPath);

                    // A zip already exists somehow
                    if (destinationZip.exists()) {
                        OsUtil.deleteFile(subLogDir);
                    }

                    FileUtil.zip(subLogDir.getAbsolutePath(), destinationZipPath);
                });
    }

    /** Consolidates the lines of all non-zipped files within the logs/SubLogDir directory. */
    public static void consolidateLogLines() {
        File logsDir = Dynamic.buildDynamic(Dynamic.LOGS.getDirectoryName());

        if (!logsDir.exists()) return;

        File[] subLogDirs = logsDir.listFiles();

        if (subLogDirs == null || subLogDirs.length == 0) return;

        Arrays.stream(subLogDirs)
                .filter(subLogDir -> !FileUtil.getExtension(subLogDir).equalsIgnoreCase(Extension.ZIP.getExtension()))
                .forEach(subLogDir -> {
                    File[] logFiles = subLogDir.listFiles();

                    if (logFiles != null && logFiles.length > 0) {
                        Arrays.stream(logFiles).forEach(logFile -> {
                            Logger.consolidateLines(logFile);
                            Logger.log(LogTag.DEBUG, "Consolidating lines of file: " + logFile.getName());
                        });
                    }
                });
    }

    /**
     * Consolidates duplicate lines next to each other of the provided log file.
     *
     * @param file the file to consolidate duplicate lines of
     */
    private static void consolidateLines(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isFile());
        Preconditions.checkArgument(FileUtil.validateExtension(file, Extension.LOG.getExtension()));

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

                if (CyderRegexPatterns.standardLogLinePattern.matcher(line).matches()) {
                    beforeFirstTimeTag = false;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        int minLogLines = 2;
        if (lines.size() >= minLogLines) {
            ArrayList<String> writeLines = new ArrayList<>();

            String lastLine;
            String currentLine;
            int currentCount = 1;

            for (int i = 0 ; i < lines.size() - 1 ; i++) {
                lastLine = lines.get(i);
                currentLine = lines.get(i + 1);

                if (LoggingUtil.areLogLinesEquivalent(lastLine, currentLine)) {
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

            // Signature
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
                for (String line : prelines) {
                    writer.write(line);
                    writer.newLine();
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            // Actual lines
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                for (String line : writeLines) {
                    writer.write(line);
                    writer.newLine();
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
    }

    /** Fixes any logs lacking/not ending in an "End Of Log" tag. */
    public static void concludeLogs() {
        try {
            File logDir = Dynamic.buildDynamic(Dynamic.LOGS.getDirectoryName());
            if (!logDir.exists()) return;

            File[] logDirs = logDir.listFiles();
            if (logDirs == null || logDirs.length == 0) return;

            for (File subLogDir : logDirs) {
                if (!subLogDir.isDirectory()) continue;

                File[] logs = subLogDir.listFiles();
                if (logs == null || logs.length == 0) return;

                for (File log : logs) {
                    if (log.equals(getCurrentLogFile())) continue;
                    BufferedReader reader = new BufferedReader(new FileReader(log));
                    String line;
                    boolean containsEOL = false;

                    int exceptions = 0;

                    while ((line = reader.readLine()) != null) {
                        if (line.contains("[EOL]")) {
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
                        String logBuilder = LoggingUtil.getLogTimeTag() + "[EOL]: "
                                + "Log completed, Cyder crashed unexpectedly: "
                                + "exit code: " + ExitCondition.ExternalStop.getCode()
                                + CyderStrings.space + ExitCondition.ExternalStop.getDescription()
                                + ", exceptions thrown: " + exceptions;

                        Files.write(Paths.get(log.getAbsolutePath()),
                                (logBuilder).getBytes(), StandardOpenOption.APPEND);
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    private static int countExceptions(File logFile) {
        return countTags(logFile, "[EXCEPTION]");
    }

    private static int countThreadsRan(File logFile) {
        return countTags(logFile, "[THREAD STARTED]");
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
    private static int countTags(File logFile, String tag) {
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
    private static ImmutableList<String> extractTags(String logLine) {
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

    private static final String EOL = "EOL";
    private static final String EXIT_CONDITION = "EXIT_CONDITION";
    private static final String RUNTIME = "RUNTIME";
    private static final String EXCEPTIONS = "EXCEPTIONS";
    private static final String OBJECTS_CREATED = "OBJECTS CREATED";
    private static final String THREADS_RAN = "THREADS RAN";

    private static void concludeLog(File file, ExitCondition condition,
                                    long runtime,
                                    int exceptions, int objectsCreated, int threadsRan) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isFile());
        Preconditions.checkArgument(FileUtil.validateExtension(file), Extension.LOG.getExtension());
        Preconditions.checkNotNull(condition);
        Preconditions.checkArgument(runtime >= 0);
        Preconditions.checkArgument(exceptions >= 0);
        Preconditions.checkArgument(objectsCreated >= 0);
        Preconditions.checkArgument(threadsRan >= 0);

        StringBuilder conclusionBuilder = new StringBuilder();

        conclusionBuilder.append(LoggingUtil.surroundWithBrackets(EOL));
        conclusionBuilder.append(CyderStrings.newline);

        conclusionBuilder.append(LoggingUtil.surroundWithBrackets(EXIT_CONDITION))
                .append(CyderStrings.space)
                .append(condition.getCode())
                .append(CyderStrings.space)
                .append(LoggingUtil.surroundWithBrackets(condition.getDescription()))
                .append(CyderStrings.newline);

        conclusionBuilder.append(LoggingUtil.surroundWithBrackets(RUNTIME))
                .append(CyderStrings.space)
                .append(TimeUtil.formatMillis(runtime))
                .append(CyderStrings.newline);

        conclusionBuilder.append(LoggingUtil.surroundWithBrackets(EXCEPTIONS))
                .append(CyderStrings.space)
                .append(exceptions)
                .append(CyderStrings.newline);

        conclusionBuilder.append(LoggingUtil.surroundWithBrackets(OBJECTS_CREATED))
                .append(CyderStrings.space)
                .append(objectsCreated)
                .append(CyderStrings.newline);

        conclusionBuilder.append(LoggingUtil.surroundWithBrackets(THREADS_RAN))
                .append(CyderStrings.space)
                .append(threadsRan)
                .append(CyderStrings.newline);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(conclusionBuilder.toString());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }



    /** Starts the object creation logger to log object creation calls every deltaT seconds. */
    private static void startObjectCreationLogger() {
        CyderThreadRunner.submit(() -> {
            try {
                ThreadUtil.sleep(LoggingUtil.INITIAL_OBJECT_CREATION_LOGGER_TIMEOUT);

                while (true) {
                    int objectsCreated = objectCreationCounter.getAndSet(0);
                    if (objectsCreated > 0) {
                        totalObjectsCreated += objectsCreated;

                        formatAndWriteLine(LoggingUtil.getLogTimeTag() + "[OBJECT CREATION]: "
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
}
