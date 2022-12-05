package cyder.logging;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.constants.CyderRegexPatterns;
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
import cyder.utils.ReflectionUtil;
import cyder.utils.StringUtil;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static cyder.constants.CyderStrings.*;
import static java.lang.System.out;

/**
 * Logger class used to log useful information about any Cyder instance from beginning at
 * runtime to exit at JVM termination.
 */
public final class Logger {
    /** Suppress default constructor. */
    private Logger() {
        throw new IllegalMethodException(ATTEMPTED_INSTANTIATION);
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

    /** The rate in ms at which to log the amount of objects created. */
    private static final int objectCreationLogFrequency = 5000;

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
    private static final ArrayList<String> awaitingLogCalls = new ArrayList<>();

    /** The file that is currently being written to on log calls. */
    private static File currentLog;

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
    private static final String LOG_CONCLUDED = "Log Call After Log Concluded";

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

    /** Writes the lines contained in static/txt/cyder.txt to the current log file. */
    private static void writeCyderAsciiArtToCurrentLogFile() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(currentLog, false))) {
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
    public static <T> void log(LogTag tag, T statement) {
        if (logConcluded) {
            println(LoggingUtil.constructTagsPrepend(LOG_CONCLUDED) + space + statement);
            return;
        } else if (statement instanceof String string && StringUtil.isNullOrEmpty(string)) {
            return;
        }

        if (!awaitingLogCalls.isEmpty() && logStarted.get()) {
            logAwaitingLogCalls();
        }

        ArrayList<String> tags = new ArrayList<>();
        StringBuilder logBuilder = new StringBuilder();

        // Unique tags have a case statement, default ones do not
        switch (tag) {
            case CONSOLE_OUT:
                // todo method for this
                tags.add(LogTag.CONSOLE_OUT.getLogName());
                switch (statement) {
                    case String string -> {
                        tags.add(ConsoleOutType.STRING.getLogTag());

                        logBuilder.append(string);
                    }
                    case ImageIcon icon -> {
                        // todo test for this
                        // [time] [image]: 15x40, dominant color=java.awt.Color(25,25,25)
                        tags.add(ConsoleOutType.IMAGE.getLogTag());
                        tags.add("Image");

                        logBuilder.append(colon)
                                .append(space)
                                .append(openingBracket)
                                .append(icon.getIconWidth()).append("x").append(icon.getIconHeight())
                                .append(closingBracket)
                                .append(comma)
                                .append(space)
                                .append("dominant color")
                                .append(colon)
                                .append(space)
                                .append(ColorUtil.getDominantColor(icon));
                    }
                    case JComponent jComponent -> {
                        tags.add(ConsoleOutType.J_COMPONENT.getLogTag());

                        logBuilder.append(jComponent);
                    }
                    case default -> {
                        tags.add(LoggingUtil.constructTagsPrepend(StringUtil.capsFirstWords(
                                ReflectionUtil.getBottomLevelClass(statement.getClass()))));

                        logBuilder.append(space)
                                .append(statement);
                    }
                }
                break;
            case EXCEPTION:
                tags.add(LogTag.EXCEPTION.getLogName());

                logBuilder.append(statement);

                exceptionsCounter.getAndIncrement();
                break;
            case LINK:
                tags.add(LogTag.LINK.getLogName());

                if (statement instanceof File file) {
                    tags.add(FileUtil.getExtension(file));

                    logBuilder.append(file.getAbsolutePath());
                } else {
                    logBuilder.append(statement);
                }

                break;
            case LOGOUT:
                tags.add(LogTag.LOGIN_OUTPUT.getLogName());
                tags.add("User");

                logBuilder.append(statement);
                break;
            case JVM_ENTRY:
                tags.add(LogTag.JVM_ENTRY.getLogName());

                logBuilder.append(statement);

                logStarted.set(true);

                break;
            case PROGRAM_EXIT:
                logConcluded = true;

                // todo use method

                formatAndWriteLine("todo method here", tag);
                return;
            case PREFERENCE:
                tags.add(LogTag.PREFERENCE.getLogName());
                tags.add("Key");
                logBuilder.append(statement);
                break;
            case OBJECT_CREATION:
                if (statement instanceof String) {
                    tags.add("Unique Object Created");

                    logBuilder.append(statement);
                } else {
                    objectCreationCounter.incrementAndGet();
                    return;
                }

                break;
            default:
                logBuilder.append(LoggingUtil.constructTagsPrepend(tag.getLogName()));
                logBuilder.append(statement);
                break;
        }

        // todo pass off to new method

        String logLine = LoggingUtil.constructTagsPrepend(tags) + logBuilder.toString().trim();
        if (logStarted.get()) {
            formatAndWriteLine(logLine, tag);
        } else {
            awaitingLogCalls.add(logLine);
        }
    }

    /** Logs the calls within awaitingLogCalls. */
    private static void logAwaitingLogCalls() {
        for (String awaitingLog : awaitingLogCalls) {
            // todo format and write
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

    // todo this is the new line

    /**
     * Writes the provided line to the current log file.
     * The provided tags and translated into proper tags with the time tag preceding all tags.
     * If the line exceeds that of {@link LoggingUtil#maxLogLineLength}
     * then the line is split where convenient.
     *
     * @param tags the tags
     * @param line the line
     */
    private static void write(List<String> tags, String line) {
        Preconditions.checkNotNull(tags);
        Preconditions.checkArgument(!tags.isEmpty());
        Preconditions.checkNotNull(line);
        Preconditions.checkArgument(!line.isEmpty());

        if (!logStarted.get()) {
            awaitingLogCalls.add(logLine);
        }

        // todo what if deleted mid runtime

        // todo log awaiting calls

        String prepend = LoggingUtil.constructTagsPrepend(tags);
        LinkedList<String> lines = LoggingUtil.checkLogLineLength(prepend + line);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(currentLog, true))) {
            for (int i = 0 ; i < lines.size() ; i++) {
                if (i != 0) {
                    bw.write(StringUtil.generateSpaces(continuationLineOffset));
                }

                bw.write(lines.get(i));
                bw.newLine();
            }
        } catch (Exception e) {
            log(LogTag.EXCEPTION, ExceptionHandler.getPrintableException(e));
        }
    }

    /**
     * Formats and writes the line to the current log file.
     *
     * @param line the line to write to the current log file
     * @param tag  the tag which was used to handle the constructed string to write
     */
    private static void formatAndWriteLine(String line, LogTag tag) {
        line = line.trim();

        if (!getCurrentLogFile().exists()) {
            generateAndSetLogFile();
            writeCyderAsciiArtToCurrentLogFile();
            writeLineToCurrentLogFile(LoggingUtil.checkLogLineLength(LoggingUtil.getLogRecoveryDebugLine()));
        }

        // todo this is messy
        if (tag != LogTag.EXCEPTION) {
            writeLineToCurrentLogFile(LoggingUtil.checkLogLineLength(line));
        } else {
            writeLineToCurrentLogFile(ImmutableList.copyOf(line.split(newline)));
        }

        println(line);
    }

    // todo this should only be called once

    /**
     * Writes the provided line to the current log file.
     * If the log line exceeds that of {@link LoggingUtil#maxLogLineLength}, the line is split
     * where necessary before being written to the log file.
     * <p>
     * Writes the lines to the current log file. The first one is not offset
     * whilst all lines after the first are offset by 11 spaces.
     * Writes the line to the current log file. The line should
     *
     * @param lines the lines to write to the current log file
     */
    private static void writeLineToCurrentLogFile(List<String> lines) { // todo accept singular string
        Preconditions.checkArgument(currentLog.exists());
        Preconditions.checkArgument(lines != null);
        Preconditions.checkArgument(!lines.isEmpty());

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(currentLog, true))) {
            for (int i = 0 ; i < lines.size() ; i++) {
                if (i != 0) {
                    bw.write(StringUtil.generateSpaces(continuationLineOffset));
                }

                bw.write(lines.get(i));
                bw.newLine();
            }
        } catch (Exception e) {
            log(LogTag.EXCEPTION, ExceptionHandler.getPrintableException(e));
        }
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
                            consolidateLines(logFile);
                            log(LogTag.DEBUG, "Consolidating lines of file: " + logFile.getName());
                        });
                    }
                });
    }

    /**
     * Consolidates duplicate lines next to each other of the provided log file.
     *
     * @param logFile the file to consolidate duplicate lines of
     */
    private static void consolidateLines(File logFile) {
        Preconditions.checkNotNull(logFile);
        Preconditions.checkArgument(logFile.exists());
        Preconditions.checkArgument(logFile.isFile());
        Preconditions.checkArgument(FileUtil.validateExtension(logFile, Extension.LOG.getExtension()));

        boolean beforeFirstTimeTag = true;

        ArrayList<String> preLogLines = new ArrayList<>();
        ArrayList<String> logLines = new ArrayList<>();
        // todo could probably have a structure and a method for this
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (beforeFirstTimeTag) {
                    preLogLines.add(line);
                } else if (!StringUtil.stripNewLinesAndTrim(line).isEmpty()) {
                    logLines.add(line);
                }

                if (CyderRegexPatterns.standardLogLinePattern.matcher(line).matches()) {
                    beforeFirstTimeTag = false;
                }
            }
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        int minLogLines = 2;
        if (logLines.size() < minLogLines) {
            return;
        }

        // todo this is kind of confusing
        ArrayList<String> writeLines = new ArrayList<>();
        String lastLine;
        String currentLine;
        int currentCount = 1;
        for (int i = 0 ; i < logLines.size() - 1 ; i++) {
            lastLine = logLines.get(i);
            currentLine = logLines.get(i + 1);

            if (LoggingUtil.areLogLinesEquivalent(lastLine, currentLine)) {
                currentCount++;
            } else {
                if (currentCount > 1) {
                    writeLines.add(generateConsolidationLine(lastLine, currentCount));
                } else {
                    writeLines.add(lastLine);
                }

                currentCount = 1;
            }
        }
        if (currentCount > 1) {
            writeLines.add(generateConsolidationLine(logLines.get(logLines.size() - 1), currentCount));
        } else {
            writeLines.add(logLines.get(logLines.size() - 1));
        }

        // Signature
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, false))) {
            for (String line : preLogLines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        // Actual lines
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            for (String line : writeLines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Generates a consolidation line for a line which is repeated back to back.
     *
     * @param line     the repeated line
     * @param numLines the number of times the line is repeated
     * @return the line
     */
    @ForReadability // todo util
    private static String generateConsolidationLine(String line, int numLines) {
        return line + space + openingBracket + numLines + "x" + closingBracket;
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
                        ImmutableList<String> tags = LoggingUtil.extractTags(line);
                        if (tags.contains("[EOL]")) {
                            containsEOL = true;
                            break;
                        } else if (tags.contains("[EXCEPTION]")) {
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
                                + space + ExitCondition.ExternalStop.getDescription()
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

    private static final String EOL = "Eol";
    private static final String EXIT_CONDITION = "Exit Condition";
    private static final String RUNTIME = "Runtime";
    private static final String EXCEPTIONS = StringUtil.getPlural(LogTag.EXCEPTION.getLogName()); // todo dynamic
    private static final String OBJECTS_CREATED = "Objects Created";
    private static final String THREADS_RAN = "Threads Ran";

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

        conclusionBuilder.append(LoggingUtil.surroundWithBrackets(EOL))
                .append(newline);

        conclusionBuilder.append(LoggingUtil.surroundWithBrackets(EXIT_CONDITION))
                .append(space)
                .append(condition.getCode())
                .append(space)
                .append(LoggingUtil.surroundWithBrackets(condition.getDescription()))
                .append(newline);

        conclusionBuilder.append(LoggingUtil.surroundWithBrackets(RUNTIME))
                .append(space)
                .append(TimeUtil.formatMillis(runtime))
                .append(newline);

        conclusionBuilder.append(LoggingUtil.surroundWithBrackets(EXCEPTIONS))
                .append(space)
                .append(exceptions)
                .append(newline);

        conclusionBuilder.append(LoggingUtil.surroundWithBrackets(OBJECTS_CREATED))
                .append(space)
                .append(objectsCreated)
                .append(newline);

        conclusionBuilder.append(LoggingUtil.surroundWithBrackets(THREADS_RAN))
                .append(space)
                .append(threadsRan)
                .append(newline);

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
                        logObjectsCreated(objectsCreated);
                    }

                    ThreadUtil.sleep(objectCreationLogFrequency);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, IgnoreThread.ObjectCreationLogger.getName());
    }

    /**
     * Logs the number of objects created since the last delta.
     *
     * @param objectsCreated the number of objects created since the last delta
     */
    @ForReadability
    private static void logObjectsCreated(int objectsCreated) {
        totalObjectsCreated += objectsCreated;

        String line = LoggingUtil.constructTagsPrepend(LogTag.OBJECT_CREATION.getLogName())
                + space + "Objects created since last delta"
                + space + openingParenthesis + objectCreationLogFrequency
                + TimeUtil.MILLISECOND_ABBREVIATION + closingParenthesis
                + colon + space + objectsCreated;

        formatAndWriteLine(line, LogTag.OBJECT_CREATION);
    }
}
