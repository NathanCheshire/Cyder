package cyder.logging;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
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
import cyder.utils.*;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

import static cyder.constants.CyderStrings.*;
import static cyder.logging.LoggingConstants.*;
import static cyder.logging.LoggingUtil.*;
import static java.lang.System.out;

/**
 * Logger class used to log useful information about any Cyder instance from beginning at
 * runtime to exit at JVM termination.
 */
public final class Logger {
    /**
     * Suppress default constructor.
     */
    private Logger() {
        throw new IllegalMethodException(ATTEMPTED_INSTANTIATION);
    }

    /**
     * The counter used to log the number of objects created each deltaT seconds.
     */
    private static final AtomicInteger objectCreationCounter = new AtomicInteger();

    /**
     * The counter used to log the number of exceptions thrown
     * and handled during this session of Cyder.
     */
    private static final AtomicInteger exceptionsCounter = new AtomicInteger();

    /**
     * Whether the logger has been initialized.
     */
    private static final AtomicBoolean logStarted = new AtomicBoolean();

    /**
     * Whether the object creation logger has been started.
     */
    private static final AtomicBoolean objectCreationLoggerStarted = new AtomicBoolean();

    /**
     * The log calls that were requested to be logged before the logger was initialized
     * and are awaiting logger initialization.
     */
    private static final ArrayList<String> awaitingLogCalls = new ArrayList<>();

    /**
     * The total number of objects created for an instance of Cyder.
     */
    private static int totalObjectsCreated = 0;

    /**
     * Whether the current log should not be written to again.
     */
    private static boolean logConcluded;

    /**
     * The file that is currently being written to on log calls.
     */
    private static File currentLog;

    /**
     * Prints the provided string to {@link System}s output stream.
     *
     * @param string the string to print
     */
    public static void println(String string) {
        out.println(string);
    }

    /**
     * Initializes the logger for logging by invoking the following actions:
     *
     * <ul>
     *     <li>Wiping past logs if enabled</li>
     *     <li>Generating and setting the current log file</li>
     *     <li>Writing the Cyder Ascii art to the generated log file</li>
     *     <li>Logging the JVM entry with the OS username</li>
     *     <li>Starting the object creation logger</li>
     *     <li>Concluding past logs which may have ended abruptly</li>
     *     <li>Consolidating past log lines</li>
     *     <li>Zipping past logs directories</li>
     * </ul>
     */
    public static void initialize() {
        // todo ensure not already called

        if (Props.wipeLogsOnStart.getValue()) {
            OsUtil.deleteFile(Dynamic.buildDynamic(Dynamic.LOGS.getDirectoryName()));
        }

        generateAndSetLogFile();
        writeCyderAsciiArtToFile(currentLog);
        log(LogTag.JVM_ENTRY, OsUtil.getOsUsername());
        startObjectCreationLogger();
        concludeLogs();
        consolidateLogLines();
        zipPastLogs();
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
     * @param statement the statement to log preceding the tags
     * @param <T>       the type of the statement
     */
    public static <T> void log(T statement) {
        log(ReflectionUtil.getBottomLevelClass(StackWalker.getInstance().getCallerClass()), statement);
    }

    /**
     * Logs the provided statement to the log file.
     *
     * @param tag       the primary log tag
     * @param statement the statement to log preceding the tags
     * @param <T>       the type of the statement
     */
    public static <T> void log(String tag, T statement) {
        constructLogLinesAndLog(ImmutableList.of(tag), statement.toString());
    }

    /**
     * The main log method to log an action associated with a type tag.
     *
     * @param tag       the type of data we are logging
     * @param statement the statement to log preceding the tags
     * @param <T>       the type of the statement
     */
    public static <T> void log(LogTag tag, T statement) {
        Preconditions.checkNotNull(tag);
        Preconditions.checkNotNull(statement);

        if (statement instanceof String string && StringUtil.isNullOrEmpty(string)) return;

        ArrayList<String> tags = new ArrayList<>();
        StringBuilder logBuilder = new StringBuilder();

        // Unique tags have a case statement, default ones do not
        switch (tag) {
            case CONSOLE_OUT:
                tags.add(LogTag.CONSOLE_OUT.getLogName());
                switch (statement) {
                    case String string -> {
                        tags.add(ConsoleOutType.STRING.toString());
                        logBuilder.append(string);
                    }
                    case ImageIcon icon -> {
                        tags.add(ConsoleOutType.IMAGE.toString());
                        logBuilder.append(colon)
                                .append(space)
                                .append(openingBracket)
                                .append(icon.getIconWidth()).append(CyderStrings.X).append(icon.getIconHeight())
                                .append(closingBracket)
                                .append(comma)
                                .append(space)
                                .append("dominant color")
                                .append(colon)
                                .append(space)
                                .append(ColorUtil.getDominantColor(icon));
                    }
                    case JComponent jComponent -> {
                        tags.add(ConsoleOutType.J_COMPONENT.toString());
                        logBuilder.append(jComponent);
                    }
                    case default -> {
                        tags.add(constructTagsPrepend(StringUtil.capsFirstWords(
                                ReflectionUtil.getBottomLevelClass(statement.getClass()))));
                        logBuilder.append(statement);
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

                if (statement instanceof ExitCondition exitCondition) {
                    concludeLog(currentLog,
                            exitCondition,
                            JvmUtil.getRuntime(),
                            exceptionsCounter.get(),
                            totalObjectsCreated,
                            CyderThreadRunner.getThreadsRan());
                } else {
                    throw new FatalException("Provided statement is not of type ExitCondition, statement: "
                            + statement + ", class: " + ReflectionUtil.getBottomLevelClass(statement.getClass()));
                }

                return;
            case PREFERENCE:
                tags.add(LogTag.PREFERENCE.getLogName());
                tags.add("Key");
                logBuilder.append(statement);
                break;
            case OBJECT_CREATION:
                if (statement instanceof String) {
                    tags.add(LogTag.OBJECT_CREATION.getLogName());
                    logBuilder.append(statement);
                } else {
                    objectCreationCounter.incrementAndGet();
                    return;
                }

                break;
            default:
                tags.add(tag.getLogName());

                logBuilder.append(statement);
                break;
        }

        constructLogLinesAndLog(tags, logBuilder.toString());
    }

    /**
     * Returns the current log file.
     *
     * @return the log file associated with the current session
     */
    public static File getCurrentLogFile() {
        return currentLog;
    }

    /**
     * Constructs lines from the tags and line and writes them to the current log file.
     * The provided tags and translated into proper tags with the time tag preceding all tags.
     * If the line exceeds that of {@link LoggingConstants#maxLogLineLength}
     * then the line is split where convenient.
     *
     * @param tags the tags
     * @param line the line
     */
    private static void constructLogLinesAndLog(List<String> tags, String line) {
        Preconditions.checkNotNull(tags);
        Preconditions.checkArgument(!tags.isEmpty());
        Preconditions.checkNotNull(line);
        Preconditions.checkArgument(!line.isEmpty());

        if (logStarted.get() && currentLog == null) {
            generateAndSetLogFile();
            writeCyderAsciiArtToFile(currentLog);
            awaitingLogCalls.addAll(checkLogLineLength(getLogRecoveryDebugLine()));
        }

        boolean isException = tags.contains(LogTag.EXCEPTION.getLogName());
        String prepend = constructTagsPrepend(tags);
        String rawWriteLine = prepend + space + line;

        ImmutableList<String> lines = isException
                ? ImmutableList.of(rawWriteLine)
                : checkLogLineLength(rawWriteLine);

        writeRawLinesToCurrentLogFile(lines, tags.contains(LogTag.EXCEPTION.getLogName()), prepend);
    }

    /**
     * Writes the provided lines directly to the current log file without any processing
     *
     * @param lines       the raw lines to write directory to the current log file
     * @param isException whether the provided lines represent an exception log
     * @param prepend     the spacing prepend for continuation lines if there are more than one lines
     */
    private static void writeRawLinesToCurrentLogFile(ImmutableList<String> lines,
                                                      boolean isException,
                                                      String prepend) {
        Preconditions.checkNotNull(lines);
        Preconditions.checkNotNull(prepend);

        if (!logStarted.get()) {
            for (int i = 0 ; i < lines.size() ; i++) {
                String prefixSpacing = i != 0 && !isException
                        ? StringUtil.generateSpaces(prepend.length()) : "";

                String writeLine = prefixSpacing + lines.get(i);
                awaitingLogCalls.add(writeLine);
            }

            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentLog, true))) {
            if (!awaitingLogCalls.isEmpty() && logStarted.get()) {
                for (String awaitingLogLine : awaitingLogCalls) {
                    writer.write(awaitingLogLine);
                    writer.newLine();

                    println(awaitingLogLine);
                }

                awaitingLogCalls.clear();
            }

            for (int i = 0 ; i < lines.size() ; i++) {
                String prefixSpacing = "";
                if (i != 0 && !isException) {
                    prefixSpacing = StringUtil.generateSpaces(prepend.length());
                }

                String writeLine = prefixSpacing + lines.get(i);

                if (!logConcluded) {
                    writer.write(writeLine);
                    writer.newLine();
                } else {
                    println("Log call after log completed: " + writeLine);
                }

                println(writeLine);
            }
        } catch (Exception e) {
            log(LogTag.EXCEPTION, ExceptionHandler.getPrintableException(e));
        }
    }

    /**
     * Zips the log files of the past.
     */
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

        for (File subLogDir : subLogDirs) {
            // Skip current log parent directory
            if (subLogDir.getAbsolutePath().equals(getCurrentLogFile().getParentFile().getAbsolutePath())) continue;
            if (FileUtil.getExtension(subLogDir).equals(Extension.ZIP.getExtension())) continue;

            String destinationZipPath = subLogDir.getAbsolutePath() + Extension.ZIP.getExtension();
            File destinationZip = new File(destinationZipPath);

            if (!destinationZip.exists()) {
                Logger.log(LogTag.DEBUG, "Zipping past sub log dir: " + subLogDir.getAbsolutePath());

                FileUtil.zip(subLogDir.getAbsolutePath(), destinationZipPath);
            }

            OsUtil.deleteFile(subLogDir);
        }
    }

    /**
     * Consolidates the lines of all non-zipped files within the logs/SubLogDir directory.
     */
    public static void consolidateLogLines() {
        File logsDir = Dynamic.buildDynamic(Dynamic.LOGS.getDirectoryName());

        if (!logsDir.exists()) return;

        File[] subLogDirs = logsDir.listFiles();

        if (subLogDirs == null || subLogDirs.length == 0) return;

        for (File subLogDir : subLogDirs) {
            if (FileUtil.getExtension(subLogDir).equalsIgnoreCase(Extension.ZIP.getExtension())) continue;

            File[] logFiles = subLogDir.listFiles();

            if (logFiles == null || logFiles.length == 0) continue;

            for (File logFile : logFiles) {
                log(LogTag.DEBUG, "Consolidating lines of file: " + logFile.getName());
                consolidateLines(logFile);
            }
        }
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

            if (areLogLinesEquivalent(lastLine, currentLine)) {
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
     * Fixes any logs lacking/not ending in an "End Of Log" tag.
     */
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

                    if (countTags(log, EOL) < 1) {
                        // todo make a method for this
                        ImmutableList<String> objectCreationLines = extractLinesWithTag(
                                log, LogTag.OBJECT_CREATION.getLogName());

                        long objectsCreated = 0;

                        for (String objectCreationLine : objectCreationLines) {
                            Matcher matcher = objectsCreatedSinceLastDeltaPattern.matcher(objectCreationLine);
                            if (matcher.matches()) {
                                String objectsGroup = matcher.group(3);

                                int objects = 0;
                                try {
                                    objects = Integer.parseInt(objectsGroup);
                                } catch (Exception e) {
                                    ExceptionHandler.handle(e);
                                }

                                objectsCreated += objects;
                            }
                        }

                        // todo method for this
                        String firstTimeString = "";
                        String lastTimeString = "";

                        for (String line : FileUtil.getFileLines(log)) {
                            Matcher matcher = CyderRegexPatterns.standardLogLinePattern.matcher(line);
                            if (matcher.matches()) {
                                if (StringUtil.isNullOrEmpty(firstTimeString)) {
                                    firstTimeString = matcher.group(1);
                                }

                                lastTimeString = matcher.group(1);
                            }
                        }

                        Date firstTimeDate = TimeUtil.LOG_LINE_TIME_FORMAT.parse(firstTimeString);
                        Date lastTimeDate = TimeUtil.LOG_LINE_TIME_FORMAT.parse(lastTimeString);

                        long millis = lastTimeDate.getTime() - firstTimeDate.getTime();

                        concludeLog(log,
                                ExitCondition.TrueExternalStop,
                                millis,
                                countExceptions(log),
                                objectsCreated,
                                countThreadsRan(log));
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    // todo sometimes continuation is missing a char, meaning the carry over is not aligned properly

    /**
     * Concludes the provided log file using the provided parameters.
     *
     * @param file           the log file to conclude
     * @param condition      the exit condition
     * @param runtime        the runtime in ms of the log
     * @param exceptions     the exceptions thrown in the log
     * @param objectsCreated the objects created during the log
     * @param threadsRan     the number of threads ran during the log
     */
    private static void concludeLog(File file,
                                    ExitCondition condition,
                                    long runtime,
                                    int exceptions,
                                    long objectsCreated,
                                    int threadsRan) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isFile());
        Preconditions.checkArgument(FileUtil.validateExtension(file, Extension.LOG.getExtension()));
        Preconditions.checkNotNull(condition);
        Preconditions.checkArgument(runtime >= 0);
        Preconditions.checkArgument(exceptions >= 0);
        Preconditions.checkArgument(objectsCreated >= 0);
        Preconditions.checkArgument(threadsRan >= 0);

        StringBuilder conclusionBuilder = new StringBuilder();

        conclusionBuilder.append(constructTagsPrepend(EOL))
                .append(space)
                .append("End Of Log")
                .append(newline);

        conclusionBuilder.append(constructTagsPrepend(EXIT_CONDITION))
                .append(space)
                .append(condition.getCode())
                .append(comma)
                .append(space)
                .append(condition.getDescription())
                .append(newline);

        conclusionBuilder.append(constructTagsPrepend(RUNTIME))
                .append(space)
                .append(TimeUtil.formatMillis(runtime))
                .append(newline);

        conclusionBuilder.append(constructTagsPrepend(StringUtil.getPlural(exceptions, EXCEPTION)))
                .append(space)
                .append(exceptions)
                .append(newline);

        conclusionBuilder.append(constructTagsPrepend(OBJECTS_CREATED))
                .append(space)
                .append(objectsCreated)
                .append(newline);

        conclusionBuilder.append(constructTagsPrepend(THREADS_RAN))
                .append(space)
                .append(threadsRan);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            String write = conclusionBuilder.toString();
            println(write);
            writer.write(write);
            writer.newLine();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Starts the object creation logger to log object creation calls every deltaT seconds.
     */
    private static void startObjectCreationLogger() {
        Preconditions.checkState(!objectCreationLoggerStarted.get());

        objectCreationLoggerStarted.set(true)

        CyderThreadRunner.submit(() -> {
            try {
                ThreadUtil.sleep(INITIAL_OBJECT_CREATION_LOGGER_TIMEOUT);

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
    private static void logObjectsCreated(int objectsCreated) {
        totalObjectsCreated += objectsCreated;

        String line = objectsCreatedSinceLastDelta
                + space + openingParenthesis + objectCreationLogFrequency
                + TimeUtil.MILLISECOND_ABBREVIATION + closingParenthesis
                + colon + space + objectsCreated;

        log(LogTag.OBJECT_CREATION, line);
    }
}
