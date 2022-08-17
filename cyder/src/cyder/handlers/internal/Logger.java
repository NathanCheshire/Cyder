package cyder.handlers.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.enums.ExitCondition;
import cyder.enums.IgnoreThread;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.utils.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static java.lang.System.out;

/**
 * Logger class used to log useful information about any Cyder instance from beginning at
 * runtime to exit at JVM termination.
 */
public final class Logger {
    /**
     * Instances of Logger not allowed.
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

    /**
     * Whether the current log should not be written to again.
     */
    private static boolean logConcluded;

    /**
     * Whether the logger has been initialized.
     */
    private static boolean logStarted;

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
    private record AwaitingLog(String line, Tag tag) {}

    /**
     * Returns whether the log has started.
     *
     * @return whether the log has started
     */
    public static boolean isLogStarted() {
        return logStarted;
    }

    /**
     * Calls string.valueOf on the provided generic and prints to the debug console
     * using the debug tag.
     * Note this method does not save anything to the current log.
     *
     * @param representation the object to debug print
     */
    public static <T> void Debug(T representation) {
        println(getLogTimeTag() + "[" + Tag.DEBUG.logName + "]: " + representation);
    }

    /**
     * Prints the provided string to {@link System}s output stream.
     *
     * @param string the string to print
     */
    public static void println(String string) {
        out.println(string);
    }

    private static final String STRING = "STRING";
    private static final String IMAGE = "IMAGE";
    private static final String J_COMPONENT = "J_COMPONENT";
    private static final String UNKNOWN_CONSOLE_OUT = "UNKNOWN CONSOLE OUT";
    private static final String LOG_CONCLUDED = "LOG CALL AFTER LOG CONCLUDED";

    /**
     * The main log method to log an action associated with a type tag.
     *
     * @param tag            the type of data we are logging
     * @param representation the representation of the object
     * @param <T>            the object instance of representation
     */
    @SuppressWarnings("IfCanBeSwitch")
    public static <T> void log(Tag tag, T representation) {
        if (logConcluded) {
            println(getLogTimeTag() + Tag.constructLogTagPrepend(LOG_CONCLUDED) + representation);
            return;
        } else if (representation instanceof String && (((String) representation).trim().isEmpty()
                || representation.equals("\n"))) {
            log(Tag.DEBUG, "Attempted to log a new or empty line");
            return;
        }

        if (logStarted) {
            logAwaitingLogCalls();
        }

        StringBuilder logBuilder = new StringBuilder(getLogTimeTag());

        switch (tag) {
            case CLIENT:
                logBuilder.append(Tag.CLIENT.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case CONSOLE_OUT:
                logBuilder.append(Tag.CONSOLE_OUT.constructLogTagPrepend());
                if (representation instanceof String) {
                    logBuilder.append(Tag.constructLogTagPrepend(STRING));
                    logBuilder.append(representation);
                } else if (representation instanceof ImageIcon icon) {
                    logBuilder.append(Tag.constructLogTagPrepend(IMAGE));

                    int width = icon.getIconWidth();
                    int height = icon.getIconHeight();
                    Color dominantColor = ColorUtil.getDominantColor(icon);

                    logBuilder.append("Image: [")
                            .append(width)
                            .append("x")
                            .append(height)
                            .append("], dominant color: ")
                            .append(dominantColor);
                } else if (representation instanceof JComponent) {
                    logBuilder.append(Tag.constructLogTagPrepend(J_COMPONENT));
                    logBuilder.append(representation);
                } else {
                    logBuilder.append(Tag.constructLogTagPrepend(UNKNOWN_CONSOLE_OUT));
                    logBuilder.append(representation);
                }
                break;
            case EXCEPTION:
                logBuilder.append(Tag.EXCEPTION.constructLogTagPrepend());
                logBuilder.append(representation);
                exceptionsCounter.getAndIncrement();
                break;
            case LINK:
                logBuilder.append(Tag.LINK.constructLogTagPrepend());
                if (representation instanceof File) {
                    logBuilder.append("[").append(FileUtil.getExtension((File) representation)).append("] ");
                }
                logBuilder.append(representation);
                break;
            case SUGGESTION:
                logBuilder.append(Tag.SUGGESTION.constructLogTagPrepend()).append(representation);
                break;
            case SYSTEM_IO:
                logBuilder.append(Tag.SYSTEM_IO.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case LOGIN:
                logBuilder.append(Tag.LOGIN.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case LOGOUT:
                logBuilder.append(Tag.LOGOUT.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case JVM_ARGS:
                logBuilder.append(Tag.JVM_ARGS.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case JVM_ENTRY:
                logBuilder.append(Tag.JVM_ENTRY.constructLogTagPrepend());
                logBuilder.append(representation);

                logStarted = true;

                break;
            case EXIT:
                logBuilder.append(Tag.EXIT.constructLogTagPrepend());
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
                logBuilder.append(Tag.CORRUPTION.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case DEBUG:
                logBuilder.append(Tag.DEBUG.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case HANDLE_METHOD:
                logBuilder.append(Tag.HANDLE_METHOD.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case WIDGET_OPENED:
                logBuilder.append(Tag.WIDGET_OPENED.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case PREFERENCE_REFRESH:
                logBuilder.append(Tag.PREFERENCE_REFRESH.constructLogTagPrepend());
                logBuilder.append("Key = ").append(representation);
                break;
            case THREAD:
                logBuilder.append(Tag.THREAD.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case OBJECT_CREATION:
                if (!(representation instanceof String)) {
                    objectCreationCounter.incrementAndGet();
                    return;
                } else {
                    logBuilder.append(Tag.constructLogTagPrepend("UNIQUE OBJECT CREATED"));
                    logBuilder.append(representation);
                }

                break;
            case AUDIO:
                logBuilder.append(Tag.AUDIO.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case UI_ACTION:
                logBuilder.append(Tag.UI_ACTION.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case CONSOLE_LOAD:
                logBuilder.append(Tag.CONSOLE_LOAD.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case FONT_LOADED:
                logBuilder.append(Tag.FONT_LOADED);
                logBuilder.append(representation);
                break;
            case THREAD_STATUS:
                if (representation instanceof String) {
                    logBuilder.append("THREAD STATUS POLLED");
                    logBuilder.append(representation);
                } else if (representation instanceof Thread) {
                    logBuilder.append(Tag.THREAD_STATUS);
                    logBuilder.append("name = ").append(((Thread) representation).getName()).append(", state = ")
                            .append(((Thread) representation).getState());
                } else {
                    logBuilder.append("THREAD");
                    logBuilder.append(representation);
                }

                break;
            case CONSOLE_REDIRECTION:
                logBuilder.append(Tag.CONSOLE_REDIRECTION.constructLogTagPrepend());
                logBuilder.append("console output was redirected to files/").append(representation);
                break;
            case CRUD_OP:
                logBuilder.append(Tag.CRUD_OP.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case PROP_LOADED:
                logBuilder.append(Tag.PROP_LOADED.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            case LOADING_MESSAGE:
                logBuilder.append(Tag.LOADING_MESSAGE.constructLogTagPrepend());
                logBuilder.append(representation);
                break;
            default:
                //this is here and not UNKNOWN as the default so that we can detect if
                // a log tag was added but not implemented
                throw new IllegalArgumentException("Handle case not found; you're probably an " +
                        "idiot and added an enum to LoggerTag but forgot to handle it Logger.log(), Tag = " + tag);
        }

        String logLine = logBuilder.toString().trim();

        if (logLine.length() <= getLogTimeTag().trim().length()) {
            log(Tag.EXCEPTION, "Log call resulted in nothing build; tag = " + tag);
        } else if (!logStarted) {
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
            OSUtil.deleteFile(OSUtil.buildFile(Dynamic.PATH,
                    Dynamic.LOGS.getDirectoryName()), false);
        }

        generateAndSetLogFile();

        writeCyderAsciiArt();

        // first log tag call should always be a JVM_ENTRY tag
        log(Tag.JVM_ENTRY, OSUtil.getOsUsername());

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
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(
                StaticUtil.getStaticResource("cyder.txt"))) ;
             BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(currentLog, true))) {
            String line;

            while ((line = bufferedReader.readLine()) != null) {
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
     * Creates the log file if it is not set/DNE.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void generateAndSetLogFile() {
        try {
            // ensure logs dir exists
            File logsDir = OSUtil.buildFile(Dynamic.PATH,
                    Dynamic.LOGS.getDirectoryName());
            logsDir.mkdir();

            // if dir for today's logs doesn't exists, create
            String logSubDirName = TimeUtil.logSubDirTime();
            File logSubDir = OSUtil.buildFile(Dynamic.PATH,
                    Dynamic.LOGS.getDirectoryName(), logSubDirName);
            logSubDir.mkdir();

            // actual log file
            String logFileName = TimeUtil.logTime();

            // ensure uniqueness
            int number = 1;
            File logFile = OSUtil.buildFile(Dynamic.PATH,
                    Dynamic.LOGS.getDirectoryName(), logSubDirName, logFileName + ".log");
            while (logFile.exists()) {
                number++;
                logFile = OSUtil.buildFile(Dynamic.PATH,
                        Dynamic.LOGS.getDirectoryName(),
                        logSubDirName, logFileName + "-" + number + ".log");

            }

            // found unique file so create
            if (logFile.createNewFile()) {
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
    private static synchronized void formatAndWriteLine(String line, Tag tag) {
        // just to be safe, we'll add in the 11 spaces in this method
        line = line.trim();

        // if log file was deleted mid operation, regenerate and add message
        if (!getCurrentLog().exists()) {
            generateAndSetLogFile();

            writeLines(insertBreaks(getLogTimeTag() + "[DEBUG]: [Log was deleted during runtime," +
                    " recreating and restarting log at: " + TimeUtil.userTime() + "]"));
        } else {
            // if not an exception, break up line if too long
            if (tag != Tag.EXCEPTION) {
                writeLines(insertBreaks(line));
            } else {
                try {
                    String[] lines = line.split("\n");
                    writeLines(lines);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }

            // print to standard output
            println(line);
        }
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
            Debug(ExceptionHandler.getPrintableException(e));
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
            Debug(ExceptionHandler.getPrintableException(e));
        }
    }

    /**
     * The chars to check to split at before splitting in between a line at whatever character a split index falls on.
     */
    private static final ImmutableList<Character> BREAK_CHARS = ImmutableList.of(' ', '/', '\'', '-', '_', '.');

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
        line = line.trim();

        LinkedList<String> ret = new LinkedList<>();

        while (line.length() > MAX_LINE_LENGTH) {
            line = line.trim();

            for (char splitChar : BREAK_CHARS) {
                if (line.length() < MAX_LINE_LENGTH) {
                    break;
                }

                // Able to split at char at current position
                if (line.charAt(MAX_LINE_LENGTH) == splitChar) {
                    ret.add(line.substring(0, MAX_LINE_LENGTH + 1));
                    line = line.substring(MAX_LINE_LENGTH + 1);
                    break;
                } else {
                    // Check left for splitChar
                    for (int i = MAX_LINE_LENGTH ; i > MAX_LINE_LENGTH - BREAK_INSERTION_TOL ; i--) {
                        // Found the split char
                        if (line.charAt(i) == splitChar) {
                            ret.add(line.substring(0, i).trim());
                            line = line.substring(i).trim();
                            break;
                        }
                        // End of the chars to check so just split at limit
                        else if (i == MAX_LINE_LENGTH - BREAK_INSERTION_TOL + 1) {
                            ret.add(line.substring(0, MAX_LINE_LENGTH).trim());
                            line = line.substring(MAX_LINE_LENGTH).trim();
                            break;
                        }
                    }
                }
            }
        }

        ret.add(line.trim());

        return ret;
    }

    /**
     * Calculates the run time of Cyder.
     *
     * @return the run time of Cyder
     */
    private static String getRuntime() {
        return TimeUtil.millisToFormattedString(System.currentTimeMillis() - START_TIME);
    }

    /**
     * Zips the log files of the past.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void zipPastLogs() {
        File topLevelLogsDir = OSUtil.buildFile(Dynamic.PATH,
                Dynamic.LOGS.getDirectoryName());

        if (!topLevelLogsDir.exists()) {
            topLevelLogsDir.mkdir();
            return;
        }

        File[] subLogDirs = topLevelLogsDir.listFiles();

        if (subLogDirs == null || subLogDirs.length == 0)
            return;

        // for all sub log dirs
        for (File subLogDir : subLogDirs) {
            // if it's not the current log and is not a zip file
            if (!FileUtil.getFilename(subLogDir.getName()).equals(TimeUtil.logSubDirTime())
                    && !FileUtil.getExtension(subLogDir).equalsIgnoreCase(".zip")) {
                // if a zip file for the directory exists, delete the dir
                if (new File(subLogDir.getAbsolutePath() + ".zip").exists()) {
                    OSUtil.deleteFile(subLogDir);
                } else {
                    FileUtil.zip(subLogDir.getAbsolutePath(), subLogDir.getAbsolutePath() + ".zip");
                }
            }
        }
    }

    /**
     * Consolidates the lines of all non-zipped files within the logs/SubLogDir directory.
     */
    public static void consolidateLogLines() {
        File logsDir = OSUtil.buildFile(Dynamic.PATH,
                Dynamic.LOGS.getDirectoryName());

        if (!logsDir.exists())
            return;

        File[] subLogDirs = logsDir.listFiles();

        if (subLogDirs == null || subLogDirs.length == 0)
            return;

        for (File subLogDir : subLogDirs) {
            if (FileUtil.getExtension(subLogDir).equalsIgnoreCase(".zip"))
                continue;

            File[] logFiles = subLogDir.listFiles();

            if (logFiles == null || logFiles.length == 0)
                continue;

            for (File logFile : logFiles)
                consolidateLines(logFile);
        }
    }

    /**
     * A pattern for the start of a standard log line used to find the time of the log call.
     */
    private static final Pattern standardLogLine =
            Pattern.compile("\\s+\\[(\\d+-\\d+-\\d+)|(\\d+-\\d+-\\d+-\\d+)]\\s+.*");

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

            if (logLinesEquivalent(lastLine, currentLine)) {
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
    private static boolean logLinesEquivalent(String logLine1, String logLine2) {
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

        return !StringUtil.isNull(logLine1) && !StringUtil.isNull(logLine2) && logLine1.equalsIgnoreCase(logLine2);
    }

    /**
     * Upon entry this method attempts to fix any user logs that ended abruptly (an exit code of -1 )
     * as a result of an IDE stop or OS Task Manager Stop.
     */
    public static void concludeLogs() {
        try {
            File logDir = OSUtil.buildFile(Dynamic.PATH,
                    Dynamic.LOGS.getDirectoryName());

            if (!logDir.exists())
                return;

            File[] logDirs = logDir.listFiles();

            if (logDirs == null || logDirs.length == 0)
                return;

            for (File subLogDir : logDirs) {
                //for all directories of days of logs
                if (FileUtil.getExtension(subLogDir).equalsIgnoreCase(".zip"))
                    continue;

                File[] logs = subLogDir.listFiles();

                if (logs == null || logs.length == 0)
                    return;

                for (File log : logs) {
                    if (!log.equals(getCurrentLog())) {
                        BufferedReader br = new BufferedReader(new FileReader(log));
                        String line;
                        boolean containsEOL = false;

                        int exceptions = 0;

                        while ((line = br.readLine()) != null) {
                            if (line.contains("[EOL]") || line.contains("[EXTERNAL STOP]")) {
                                containsEOL = true;
                                break;
                            } else if (line.contains("[EXCEPTION]")) {
                                exceptions++;
                            }
                        }

                        br.close();

                        if (!containsEOL) {
                            // usually an IDE stop but sometimes the program exits,
                            // with exit condition 1 due to something failing on startup
                            // which is why this says "crashed unexpectedly"
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
     * Starts the object creation logger to log object creation calls every deltaT seconds.
     */
    private static void startObjectCreationLogger() {
        CyderThreadRunner.submit(() -> {
            try {
                // initial timeout from program initialization
                ThreadUtil.sleep(3000);

                while (true) {
                    if (objectCreationCounter.get() > 0) {
                        int objectsCreated = objectCreationCounter.getAndSet(0);
                        totalObjectsCreated += objectsCreated;

                        formatAndWriteLine(getLogTimeTag() + "[OBJECT CREATION]: "
                                + "Objects created since last delta (" + OBJECT_LOG_FREQUENCY + "ms): "
                                + objectsCreated, Tag.OBJECT_CREATION);
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

    /**
     * Supported tags for log entries
     */
    public enum Tag {
        /**
         * The cyder user typed something through the console input field.
         */
        CLIENT("CLIENT"),
        /**
         * Whatever is printed/appended to the CyderTextPane from the console.
         */
        CONSOLE_OUT("CONSOLE OUT"),
        /**
         * Something that would have been appended to the Cyder text pane was piped to a file.
         */
        CONSOLE_REDIRECTION("CONSOLE PRINT REDIRECTION"),
        /**
         * An exception was thrown and handled by the ExceptionHandler.
         */
        EXCEPTION("EXCEPTION"),
        /**
         * Audio played/stopped/paused/etc.
         */
        AUDIO("AUDIO"),
        /**
         * Frame control actions.
         */
        UI_ACTION("UI"),
        /**
         * A link was printed or opened.
         */
        LINK("LINK"),
        /**
         * A user made a suggestion which will probably be ignored.
         */
        SUGGESTION("SUGGESTION"),
        /**
         * IO by Cyder typically to/from a json file but usually to files within {@link Dynamic#PATH}
         */
        SYSTEM_IO("SYSTEM IO"),
        /**
         * A user starts Cyder or enters the main program, that of the Console.
         */
        LOGIN("LOGIN"),
        /**
         * A user logs out of Cyder, not necessarily a program exit.
         */
        LOGOUT("LOGOUT"),
        /**
         * When Cyder.java is first invoked by the JVM, we log certain properties about
         * the JVM/JRE and send them to the Cyder backend as well.
         */
        JVM_ARGS("JVM"),
        /**
         * JVM program entry.
         */
        JVM_ENTRY("ENTRY"),
        /**
         * Program controlled exit, right before EOL tags.
         */
        EXIT("EXIT"),
        /**
         * A user became corrupted invoking the userJsonCorrupted method.
         */
        CORRUPTION("CORRUPTION"),
        /**
         * A quick debug information statement.
         */
        DEBUG("DEBUG"),
        /**
         * A type of input was handled via the InputHandler.
         */
        HANDLE_METHOD("HANDLE"),
        /**
         * A widget was opened via the reflection method.
         */
        WIDGET_OPENED("WIDGET"),
        /**
         * A userdata which exists as a Preference object was toggled between states and refreshed.
         */
        PREFERENCE_REFRESH("PREFERENCE REFRESH"),
        /**
         * A thread was spun up and started by CyderThreadRunner.
         */
        THREAD("THREAD STARTED"),
        /**
         * When an object's constructor is invoked.
         */
        OBJECT_CREATION("OBJECT CREATION"),
        /**
         * The console was loaded.
         */
        CONSOLE_LOAD("CONSOLE LOADED"),
        /**
         * A font was loaded by the sub-routine from the fonts/ directory.
         */
        FONT_LOADED("FONT LOADED"),
        /**
         * A prop from props.ini was loaded.
         */
        PROP_LOADED("PROP LOADED"),
        /**
         * The status of a thread, typically AWT-EventQueue-0.
         */
        THREAD_STATUS("THREAD STATUS"),
        /**
         * A Create (PUT), Read (GET), Update (POST), or Delete (DELETE) operation was performed
         * on the Cyder backend.
         */
        CRUD_OP("BACKEND CRUD"),
        /**
         * The CyderSplash loading message was set.
         */
        LOADING_MESSAGE("LOADING MESSAGE");

        /**
         * The name to be written to the log file when this tag is logged
         */
        private final String logName;

        /**
         * Constructs a new Tag object.
         *
         * @param logName the name to be written to the log file when this tag is logged
         */
        Tag(String logName) {
            this.logName = logName;
        }

        /**
         * Returns the log name of this log tag.
         *
         * @return the log name of this log tag
         */
        public String getLogName() {
            return logName;
        }

        /**
         * Constructs a log tag prepend for this log tag.
         *
         * @return a log tag prepend for this log tag
         */
        public String constructLogTagPrepend() {
            return "[" + this.logName + "]: ";
        }

        /**
         * Constructs the string value prepended to the log line before the representation.
         *
         * @param tagString the string representation of an exclusive log tag
         * @return the string to prepend to the log line
         */
        public static String constructLogTagPrepend(String tagString) {
            return "[" + tagString + "]: ";
        }

        @Override
        public String toString() {
            return constructLogTagPrepend(logName);
        }
    }
}
