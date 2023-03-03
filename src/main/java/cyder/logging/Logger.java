package cyder.logging;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.enumerations.Dynamic;
import cyder.enumerations.ExitCondition;
import cyder.enumerations.Extension;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.meta.CyderArguments;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.strings.ToStringUtil;
import cyder.threads.CyderThreadRunner;
import cyder.threads.IgnoreThread;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import cyder.utils.*;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static cyder.logging.LoggingConstants.*;
import static cyder.logging.LoggingUtil.*;
import static cyder.strings.CyderStrings.*;
import static java.lang.System.out;

/**
 * A logging class used to log useful information about the Cyder instance throughout the JVM runtime.
 */
public final class Logger {
    /**
     * The stack walker instance ot use if a class invokes log without providing a log tag.
     */
    private static final StackWalker stackWalker = StackWalker.getInstance();

    /**
     * The counter used to log the number of specific objects created each deltaT seconds.
     */
    private static final ConcurrentHashMap<Class<?>, AtomicInteger> objectCreationCounter = new ConcurrentHashMap<>();

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
     * Whether the logger has been initialized already.
     */
    private static final AtomicBoolean loggerInitialized = new AtomicBoolean();

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
     * Suppress default constructor.
     */
    private Logger() {
        throw new IllegalMethodException(ATTEMPTED_INSTANTIATION);
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
        Preconditions.checkState(!loggerInitialized.get());
        loggerInitialized.set(true);

        if (Props.wipeLogsOnStart.getValue()) Dynamic.LOGS.delete();
        generateAndSetLogFile();
        setupLogFileWithAsciiArt();
        log(LogTag.LOGGER_INITIALIZATION, "Os username: " + OsUtil.getOsUsername());
        startObjectCreationLogger();
        concludeLogs();
        consolidateLogLines();
        zipPastLogs();
    }

    /**
     * The actions to invoke when a log file has been generated and set to write Ascii art to the file as the header.
     * The Cyder Ascii art is always written. The boostrap Ascii art is written if this instance was started from
     * a bootstrap attempt.
     */
    private static void setupLogFileWithAsciiArt() {
        writeCyderAsciiArtToFile(currentLog, false);
        if (JvmUtil.mainMethodArgumentPresent(CyderArguments.BOOSTRAP.getName())) {
            writeBoostrapAsciiArtToFile(currentLog);
        }
    }

    /**
     * Creates the top level logs directory, the log sub-directory for today,
     * and the log file for this session if it is not generated or set.
     */
    private static void generateAndSetLogFile() {
        try {
            File logsDir = Dynamic.buildDynamic(Dynamic.LOGS.getFileName());
            if (!logsDir.exists() && !logsDir.mkdir()) {
                throw new FatalException("Failed to create logs directory");
            }

            String logSubDirName = TimeUtil.logSubDirTime();
            File logSubDir = Dynamic.buildDynamic(Dynamic.LOGS.getFileName(), logSubDirName);
            if (!logSubDir.exists() && !logSubDir.mkdir()) {
                throw new FatalException("Failed to create log directory for current day");
            }

            File proposedLogFile = new File(TimeUtil.logTime() + Extension.LOG.getExtension());
            String uniqueFilename = FileUtil.constructUniqueName(proposedLogFile, logSubDir);
            File logFile = Dynamic.buildDynamic(
                    Dynamic.LOGS.getFileName(), logSubDirName, uniqueFilename);

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
        log(ImmutableList.of(ReflectionUtil.getBottomLevelClass(stackWalker.getCallerClass())), statement);
    }

    /**
     * Logs the provided statement to the log file.
     *
     * @param tags      the tags to prefix the statement
     * @param statement the statement to log preceding the tags
     * @param <T>       the type of the statement
     */
    public static <T> void log(List<String> tags, T statement) {
        Preconditions.checkNotNull(tags);
        Preconditions.checkArgument(!tags.isEmpty());
        Preconditions.checkNotNull(statement);

        if (checkForAttemptedWhitespaceLogCall(statement)) return;

        constructLogLinesAndLog(tags, statement.toString());
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

        if (checkForAttemptedWhitespaceLogCall(statement)) return;

        ArrayList<String> tags = new ArrayList<>();
        StringBuilder logBuilder = new StringBuilder();

        // Unique tags have a case statement, default ones do not
        switch (tag) {
            case CONSOLE_OUT:
                tags.add(tag.getLogName());
                switch (statement) {
                    case String string -> {
                        tags.add(ConsoleOutType.STRING.toString());
                        logBuilder.append(string);
                    }
                    case ImageIcon icon -> {
                        tags.add(ConsoleOutType.IMAGE.toString());
                        logBuilder.append("dimensions=")
                                .append(icon.getIconWidth()).append(CyderStrings.X).append(icon.getIconHeight())
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
                tags.add(tag.getLogName());
                logBuilder.append(statement);

                exceptionsCounter.getAndIncrement();
                break;
            case LINK:
                tags.add(tag.getLogName());

                if (statement instanceof File file) {
                    tags.add(FileUtil.getExtension(file));
                    logBuilder.append(file.getAbsolutePath());
                } else {
                    logBuilder.append(statement);
                }

                break;
            case LOGOUT:
                tags.add(tag.getLogName());
                tags.add(USER);
                logBuilder.append(statement);
                break;
            case LOGGER_INITIALIZATION:
                tags.add(tag.getLogName());
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
            case USER_DATA:
                tags.add(tag.getLogName());
                tags.add("Key");
                logBuilder.append(statement);
                break;
            case OBJECT_CREATION:
                if (statement instanceof String) {
                    tags.add(LogTag.OBJECT_CREATION.getLogName());
                    logBuilder.append(statement);
                } else {
                    if (objectCreationCounter.containsKey(statement.getClass())) {
                        objectCreationCounter.get(statement.getClass()).getAndIncrement();
                    } else {
                        objectCreationCounter.put(statement.getClass(), new AtomicInteger(1));
                    }

                    return;
                }

                break;
            case OBJECT_DESERIALIZATION, OBJECT_SERIALIZATION:
                if (shouldIgnoreObjectSerializationOrDeserialization(statement)) return;

                String action = tag == LogTag.OBJECT_SERIALIZATION ? "Serialized" : "Deserialized";

                if (statement instanceof Class<?> clazz) {
                    tags.add(tag.getLogName());
                    logBuilder.append(action).append(space).append(ReflectionUtil.getBottomLevelClass(clazz));
                } else if (statement instanceof Type type) {
                    tags.add(tag.getLogName());
                    logBuilder.append(action).append(space).append(type.getTypeName());
                }

                break;
            case UI_ACTION:
                tags.add(tag.getLogName());
                if (statement instanceof Component component) {
                    tags.add(ToStringUtil.getComponentParentFrameRepresentation(component));
                }
                logBuilder.append(statement);
                break;
            default:
                tags.add(tag.getLogName());
                logBuilder.append(statement);
                break;
        }

        constructLogLinesAndLog(tags, logBuilder.toString());
    }

    /**
     * Checks for whether the provided statement was null or pure whitespace and if so, logs the attempted
     * invalid log call if specified in the props.
     *
     * @param statement the statement to test for being null or empty
     * @param <T>       the type of statement
     * @return whether the statement was null or empty
     */
    private static <T> boolean checkForAttemptedWhitespaceLogCall(T statement) {
        if (statement instanceof String string && StringUtil.isNullOrEmpty(string)) {
            if (Props.logAttemptedNewlineOrWhitespaceCalls.getValue()) {
                // todo count newlines, carriage returns, tabs, spaces, etc.
                log(LogTag.DEBUG, "Null or purely whitespace log statement, length " + string.length());
            }

            return true;
        }

        return false;
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
            onLogFileDeletedMidRuntime();
        }

        boolean isException = tags.contains(LogTag.EXCEPTION.getLogName());
        String prepend = constructTagsPrepend(tags);
        String rawWriteLine = prepend + line;

        ImmutableList<String> lengthCheckedLines = isException
                ? ImmutableList.of(rawWriteLine)
                : checkLogLineLength(rawWriteLine);

        ArrayList<String> prefixedLines = new ArrayList<>();

        for (int i = 0 ; i < lengthCheckedLines.size() ; i++) {
            String prefixSpacing = "";
            if (i != 0 && !isException) {
                prefixSpacing = StringUtil.generateSpaces(prepend.length());
            }

            String writeLine = prefixSpacing + lengthCheckedLines.get(i).trim();
            prefixedLines.add(writeLine);
        }

        writeRawLinesToCurrentLogFile(ImmutableList.copyOf(prefixedLines));
    }

    /**
     * Writes the provided lines directly to the current log file without any processing
     *
     * @param lines the raw lines to write directory to the current log file
     */
    private static void writeRawLinesToCurrentLogFile(ImmutableList<String> lines) {
        Preconditions.checkNotNull(lines);

        if (!logStarted.get()) {
            awaitingLogCalls.addAll(lines);
            return;
        }

        if (!currentLog.exists()) {
            onLogFileDeletedMidRuntime();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentLog, true))) {
            if (!awaitingLogCalls.isEmpty()) {
                for (String awaitingLogLine : awaitingLogCalls) {
                    out.println(awaitingLogLine);
                    writer.write(awaitingLogLine);
                    writer.newLine();
                }

                awaitingLogCalls.clear();
            }

            for (String line : lines) {
                if (!logConcluded) {
                    out.println(line);
                    writer.write(line);
                    writer.newLine();
                } else {
                    out.println("Log call after log completed: " + line);
                }
            }
        } catch (Exception e) {
            log(LogTag.EXCEPTION, ExceptionHandler.getPrintableException(e));
        }
    }

    /**
     * Zips the log files of the past.
     */
    private static void zipPastLogs() {
        File topLevelLogsDir = Dynamic.buildDynamic(Dynamic.LOGS.getFileName());

        if (!topLevelLogsDir.exists()) {
            if (!topLevelLogsDir.mkdir()) {
                throw new FatalException("Failed to create logs dir");
            }

            return;
        }

        File[] subLogDirs = topLevelLogsDir.listFiles();
        if (ArrayUtil.nullOrEmpty(subLogDirs)) return;

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
    private static void consolidateLogLines() {
        File logsDir = Dynamic.buildDynamic(Dynamic.LOGS.getFileName());

        if (!logsDir.exists()) return;

        File[] subLogDirs = logsDir.listFiles();

        if (ArrayUtil.nullOrEmpty(subLogDirs)) return;

        Arrays.stream(subLogDirs)
                .filter(subLogDir -> !FileUtil.getExtension(subLogDir).equalsIgnoreCase(Extension.ZIP.getExtension()))
                .forEach(subLogDir -> {
                    File[] logFiles = subLogDir.listFiles();

                    if (!ArrayUtil.nullOrEmpty(logFiles)) {
                        Arrays.stream(logFiles).filter(logFile -> !logFile.equals(Logger.getCurrentLogFile()))
                                .forEach(logFile -> {
                                    log(LogTag.DEBUG, "Consolidating lines of file: " + logFile.getName());
                                    consolidateLines(logFile);
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

        ImmutableList<String> fileLines = FileUtil.getFileLines(logFile);
        ArrayList<String> logLines = new ArrayList<>();

        boolean firstLogLineFound = false;
        for (String fileLine : fileLines) {
            if (!firstLogLineFound && LoggingUtil.matchesStandardLogLine(fileLine)) firstLogLineFound = true;
            if (firstLogLineFound) logLines.add(fileLine);
        }

        // If there's only one line, consolidating doesn't make sense now does it?
        if (logLines.size() < 2) return;

        ArrayList<String> writeLines = new ArrayList<>();

        String lastLine;
        String currentLine = "";
        int currentCount = 1;

        for (int i = 1 ; i < logLines.size() ; i++) {
            lastLine = logLines.get(i - 1);
            currentLine = logLines.get(i);

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

        // Last read line hasn't been added yet
        if (currentCount > 1) {
            writeLines.add(generateConsolidationLine(currentLine, currentCount));
        } else {
            writeLines.add(currentLine);
        }

        FileUtil.writeLinesToFile(logFile, writeLines, true);
    }

    /**
     * Fixes any logs lacking/not ending in an "End Of Log" tag.
     */
    private static void concludeLogs() {
        File logDir = Dynamic.buildDynamic(Dynamic.LOGS.getFileName());
        if (!logDir.exists()) return;

        File[] subLogDirs = logDir.listFiles();
        if (ArrayUtil.nullOrEmpty(subLogDirs)) return;

        Arrays.stream(subLogDirs)
                .filter(File::isDirectory)
                .filter(subLogDir -> ArrayUtil.nullOrEmpty(subLogDir.listFiles()))
                .forEach(subLogDir -> {
                    //noinspection ConstantConditions, safe due to filtering
                    Arrays.stream(subLogDir.listFiles())
                            .filter(logFile -> !logFile.equals(currentLog))
                            .filter(logFile -> countTags(logFile, EOL) < 1)
                            .forEach(logFile -> {
                                try {
                                    concludeLog(logFile,
                                            ExitCondition.TrueExternalStop,
                                            getRuntimeFromLog(logFile),
                                            countExceptions(logFile),
                                            countObjectsCreatedFromLog(logFile),
                                            countThreadsRan(logFile));
                                } catch (Exception e) {
                                    ExceptionHandler.handle(e);
                                }
                            });
                });
    }

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

        String write = constructTagsPrepend(EOL)
                + END_OF_LOG
                + newline
                + constructTagsPrepend(EXIT_CONDITION)
                + condition.getCode()
                + comma
                + space
                + condition.getDescription()
                + newline
                + constructTagsPrepend(RUNTIME)
                + TimeUtil.formatMillis(runtime)
                + newline
                + constructTagsPrepend(StringUtil.getWordFormBasedOnNumber(exceptions, EXCEPTION))
                + exceptions
                + newline
                + constructTagsPrepend(OBJECTS_CREATED)
                + objectsCreated
                + newline
                + constructTagsPrepend(THREADS_RAN)
                + threadsRan;

        if (file.equals(currentLog)) out.println(write);
        FileUtil.writeLinesToFile(file, ImmutableList.of(write), true);
    }

    /**
     * Starts the object creation logger to log object creation calls every deltaT seconds.
     */
    private static void startObjectCreationLogger() {
        Preconditions.checkState(!objectCreationLoggerStarted.get());

        objectCreationLoggerStarted.set(true);

        CyderThreadRunner.submit(() -> {
            try {
                ThreadUtil.sleep(INITIAL_OBJECT_CREATION_LOGGER_TIMEOUT);

                while (true) {
                    logSpecificObjectsCreated();
                    logTotalObjectsCreated();
                    ThreadUtil.sleep(objectCreationLogFrequency);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, IgnoreThread.ObjectCreationLogger.getName());
    }

    /**
     * Logs the specific objects created as configured via the {@link Props#specificObjectCreationLogs} prop.
     */
    private static void logSpecificObjectsCreated() {
        objectCreationCounter.keySet().forEach(key -> {
            String bottomLevelClassName = ReflectionUtil.getBottomLevelClass(key);
            if (StringUtil.in(bottomLevelClassName, true,
                    Props.specificObjectCreationLogs.getValue().getList())) {
                String line = bottomLevelClassName + space + "objects created since last delta"
                        + space + openingParenthesis + objectCreationLogFrequency
                        + TimeUtil.MILLISECOND_ABBREVIATION + closingParenthesis
                        + colon + space + objectCreationCounter.get(key).get();

                Logger.log(LogTag.OBJECT_CREATION, line);
            }
        });
    }

    /**
     * Logs the total number of objects created from {@link #objectCreationCounter}.
     */
    private static void logTotalObjectsCreated() {
        int currentObjectsCreated = getTotalObjectsCreated();
        objectCreationCounter.clear();
        if (currentObjectsCreated > 0) {
            totalObjectsCreated += currentObjectsCreated;

            String line = objectsCreatedSinceLastDelta
                    + space + openingParenthesis + objectCreationLogFrequency
                    + TimeUtil.MILLISECOND_ABBREVIATION + closingParenthesis
                    + colon + space + currentObjectsCreated;

            log(LogTag.OBJECT_CREATION, line);
        }
    }

    /**
     * Returns the total objects created contained in {@link #objectCreationCounter}.
     *
     * @return the total objects created contained in {@link #objectCreationCounter}
     */
    private static int getTotalObjectsCreated() {
        return objectCreationCounter.keySet().stream().mapToInt(key -> objectCreationCounter.get(key).get()).sum();
    }

    /**
     * The actions to invoke when the log file is deleted mid-runtime.
     */
    private static void onLogFileDeletedMidRuntime() {
        generateAndSetLogFile();
        writeCyderAsciiArtToFile(currentLog, false);
        awaitingLogCalls.addAll(checkLogLineLength(getLogRecoveryDebugLine()));
    }
}
