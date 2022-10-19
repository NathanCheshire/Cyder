package cyder.logging;

import com.google.common.base.Preconditions;
import cyder.annotations.ForReadability;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.enums.ExitCondition;
import cyder.enums.Extension;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.props.PropLoader;
import cyder.threads.CyderThreadRunner;
import cyder.threads.IgnoreThread;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import cyder.utils.ColorUtil;
import cyder.utils.FileUtil;
import cyder.utils.OsUtil;
import cyder.utils.StringUtil;

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

    @ForReadability
    private static final int BRACKETS_SPACE_LEN = 3;

    /**
     * The number of spaces to prepend to a continuation line. This ensures wrapped lines are
     * started after the header such as "[hh-mm-ss.SSSS] " above it.
     */
    private static final int NEWLINE_SPACE_OFFSET = TimeUtil.getLogLineTime().length() + BRACKETS_SPACE_LEN;

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
            println(LoggingUtil.getLogTimeTag() + LogTag.constructLogTagPrepend(LOG_CONCLUDED) + representation);
            return;
        } else if (representation instanceof String string && LoggingUtil.emptyOrNewline(string)) {
            log(LogTag.DEBUG, "Attempted to log a new or empty line");
            return;
        }

        if (logStarted.get()) {
            logAwaitingLogCalls();
        }

        StringBuilder logBuilder = new StringBuilder(LoggingUtil.getLogTimeTag());

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
                    logBuilder.append(ConsoleOut.IMAGE.getLogTag());

                    int width = icon.getIconWidth();
                    int height = icon.getIconHeight();
                    Color dominantColor = ColorUtil.getDominantColor(icon);

                    logBuilder.append("Image: [").append(width).append("x")
                            .append(height).append("], dominant color: ")
                            .append(dominantColor);
                } else if (representation instanceof JComponent) {
                    logBuilder.append(ConsoleOut.J_COMPONENT.getLogTag());
                    logBuilder.append(representation);
                } else {
                    logBuilder.append(LogTag.constructLogTagPrepend(
                            StringUtil.capsFirstWords(representation.getClass().toString())));
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
            case LOGIN_FIELD:
                logBuilder.append(LogTag.LOGIN_FIELD.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case LOGIN_OUTPUT:
                logBuilder.append(LogTag.LOGIN_OUTPUT.constructLogTagPrepend());
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
                logBuilder.append("Runtime: ");
                logBuilder.append(getRuntime());
                formatAndWriteLine(logBuilder.toString(), tag);

                StringBuilder eolBuilder = new StringBuilder();
                eolBuilder.append(LoggingUtil.getLogTimeTag());
                eolBuilder.append("[EOL]: ");
                eolBuilder.append("Log completed, exiting Cyder with exit code: ");

                String exitCodeRepresentation;
                if (representation instanceof ExitCondition exitCondition) {
                    exitCodeRepresentation = exitCondition.getCode() + " [" + exitCondition.getDescription() + "], ";
                } else {
                    exitCodeRepresentation = "Error parsing exit condition: " + representation + ", ";
                }

                eolBuilder.append(exitCodeRepresentation);

                eolBuilder.append(exceptionsCounter.get() == 0
                        ? "no exceptions thrown"
                        : "exceptions thrown: " + exceptionsCounter.get());

                eolBuilder.append(", total objects created: ")
                        .append(totalObjectsCreated)
                        .append(", threads ran: ")
                        .append(CyderThreadRunner.getThreadsRan())
                        .append("\n");

                formatAndWriteLine(eolBuilder.toString(), tag);
                logConcluded = true;

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
            case THREAD_STARTED:
                logBuilder.append(LogTag.THREAD_STARTED.constructLogTagPrepend());
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

        if (logLine.length() <= LoggingUtil.getLogTimeTag().trim().length()) {
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
     * Writes the lines contained in static/txt/cyder.txt to the current log file.
     */
    private static void writeCyderAsciiArt() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(currentLog, true))) {
            for (String line : LoggingUtil.getHeaderLogoLines()) {
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

            File proposedLogFile = new File(TimeUtil.logTime() + Extension.LOG.getExtension());
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
            writeLines(LoggingUtil.insertBreaks(LoggingUtil.getLogRecoveryDebugLine()));
        }

        if (tag != LogTag.EXCEPTION) {
            writeLines(LoggingUtil.insertBreaks(line));
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
     * Calculates the run time of Cyder.
     *
     * @return the run time of Cyder
     */
    private static String getRuntime() {
        return TimeUtil.formatMillis(System.currentTimeMillis() - START_TIME);
    }

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
                    && !FileUtil.getExtension(subLogDir).equalsIgnoreCase(Extension.ZIP.getExtension())) {
                if (new File(subLogDir.getAbsolutePath() + Extension.ZIP.getExtension()).exists()) {
                    OsUtil.deleteFile(subLogDir);
                } else {
                    FileUtil.zip(subLogDir.getAbsolutePath(),
                            subLogDir.getAbsolutePath() + Extension.ZIP.getExtension());
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
                .filter(subLogDir -> !FileUtil.getExtension(subLogDir).equalsIgnoreCase(Extension.ZIP.getExtension()))
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
    private static final Pattern standardLogLinePattern =
            Pattern.compile("\\s*\\[\\d+-\\d+-\\d+\\.\\d+]\\s*.*");

    /**
     * Consolidates duplicate lines next to each other of the provided file.
     *
     * @param file the file to consolidate duplicate lines of
     */
    private static void consolidateLines(File file) {
        Preconditions.checkArgument(file.exists(), "Provided file does not exist: " + file);
        Preconditions.checkArgument(FileUtil.getExtension(file).equalsIgnoreCase(Extension.LOG.getExtension()),
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

                if (standardLogLinePattern.matcher(line).matches()) {
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
     * Fixes any logs lacking/not ending in an EOL tag.
     */
    public static void concludeLogs() {
        try {
            File logDir = OsUtil.buildFile(Dynamic.PATH, Dynamic.LOGS.getDirectoryName());

            if (!logDir.exists()) return;

            File[] logDirs = logDir.listFiles();

            if (logDirs == null || logDirs.length == 0) return;

            for (File subLogDir : logDirs) {
                if (FileUtil.getExtension(subLogDir).equalsIgnoreCase(Extension.ZIP.getExtension())) continue;

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
                            String logBuilder = LoggingUtil.getLogTimeTag() + "[EOL]: "
                                    + "Log completed, Cyder crashed unexpectedly: "
                                    + "exit code: " + ExitCondition.ExternalStop.getCode()
                                    + " " + ExitCondition.ExternalStop.getDescription()
                                    + ", exceptions thrown: " + exceptions;

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
