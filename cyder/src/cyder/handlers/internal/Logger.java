package cyder.handlers.internal;

import cyder.constants.CyderStrings;
import cyder.ui.ConsoleFrame;
import cyder.utilities.*;

import javax.swing.*;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/**
 * Logger class used to log useful information about any Cyder instance from beginning at
 * runtime to exit at JVM termination.
 */
public class Logger {
    /**
     * Instances of Logger not allowed.
     */
    private Logger() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * The file that is currently being written to on log calls.
     */
    private static File currentLog;

    /**
     * The absolute start time of Cyder, initialized at runtime.
     */
    public static final long start = System.currentTimeMillis();

    /**
     * Supported tags for log entries
     */
    public enum Tag {
        CLIENT, // client typed something
        CONSOLE_OUT, // printing something to the console frame
        EXCEPTION, // an exception
        ACTION, // an action taken
        LINK, // a link in anyway is printed, represented, etc.
        SUGGESTION, // logging a suggestion
        SYSTEM_IO, // input or output to/from sys.json
        CLIENT_IO, // input or output to/from userdata file
        LOGIN, // user is logged in
        LOGOUT, // user is logged out
        JAVA_ARGS, // java args upon JVM entry to main
        ENTRY, // entry of program
        EXIT, // exit of program
        CORRUPTION, // corruption of userdata file
        DEBUG_PRINT, // used for debug printing
        HANDLE_METHOD, // used for boolean returning handle methods within InputHandler
        WIDGET_OPENED, // used if a widget from the widgets package was opened
        PREFERENCE_REFRESH, // used in Preferences class for when update functions are invoked
        THREAD, // used to log threads that are invoked
        UNKNOWN, // not sure/all else failed
    }

    /**
     * The main log method to log an action associated with a type tag.
     * @param tag the type of data we are logging
     * @param representation the representation of the object
     * @param <T> the object instance of representation
     */
    public static <T> void log(Tag tag, T representation) {
        String initialTimeTag = "[" + TimeUtil.logTime() + "] ";
        StringBuilder logBuilder = new StringBuilder(initialTimeTag);

        switch (tag) {
            case CLIENT:
                //user inputs to the console
                logBuilder.append("[CLIENT]: ");
                logBuilder.append(representation);
                break;
            case CONSOLE_OUT:
                logBuilder.append("[CONSOLE_OUT]: ");
                if (representation instanceof String) {
                    logBuilder.append("[STRING] ");
                    logBuilder.append(representation);
                } else if (representation instanceof ImageIcon) {
                    logBuilder.append("[ICON] ");
                    logBuilder.append(representation);
                }
                //jcomponent print outs
                else if (representation instanceof JComponent) {
                    logBuilder.append("[JCOMPONENT] ");
                    logBuilder.append(representation);
                }
                //other console print outs
                else {
                    logBuilder.append("[UNKNOWN CONSOLE_OUT TYPE] ");
                    logBuilder.append(representation);
                }
                break;
            case EXCEPTION:
                //any exceptions thrown are passed from errorhandler to here
                logBuilder.append("[EXCEPTION]: ");
                logBuilder.append(representation);
                break;
            case ACTION:
                logBuilder.append("[ACTION]: ");
                logBuilder.append(representation);
                break;
            case LINK:
                //files opened, links opened
                logBuilder.append("[LINK]: ");
                if (representation instanceof File) {
                    logBuilder.append("[").append(FileUtil.getExtension((File) representation)).append("] ");
                }
                logBuilder.append(representation);
                break;
            case SUGGESTION:
                logBuilder.append("[SUGGESTION]: ").append(representation);
                break;
            case CLIENT_IO:
                //userdata read or write
                //[CLIENT_IO]: [SET] [KEY] NAME [VALUE] NATHAN
                //[CLIENT_IO]: [GET] [KEY] VERSION [RETURN VALUE] 9.2.21
                logBuilder.append("[CLIENT_IO]: ");
                logBuilder.append(representation);
                break;
            case SYSTEM_IO:
                //systemdata read or write
                //[SYSTEM_IO]: [SET] [KEY] VERSION [VALUE] SOULTREE
                //[SYSTEM_IO]: [GET] [KEY] VERSION [RETURN VALUE] 9.2.21
                logBuilder.append("[SYSTEM_IO]: ");
                logBuilder.append(representation);
                break;
            case LOGIN:
                //user logged in using recognize method
                //[LOGIN]: [NATHAN] Autocyphered (STD Login)
                logBuilder.append("[LOGIN]: [");
                logBuilder.append(representation);
                logBuilder.append("]");
                break;
            case LOGOUT:
                //[LOGOUT]: [NATHAN]
                logBuilder.append("[LOGOUT]: ");
                logBuilder.append(representation);
                break;
            case JAVA_ARGS:
                //[JAVA ARGS]: (possible args) [LOCATION] New Orleans, LA
                logBuilder.append("[JAVA ARGS]: ");
                logBuilder.append(representation);
                break;
            case ENTRY:
                //[ENTRY]: [WINUSER=NATHAN]
                logBuilder.append("[ENTRY]: [");
                logBuilder.append(representation);
                logBuilder.append("]");
                break;
            case EXIT:
                //right before genesisshare.exit exits
                //[EXIT]: [RUNTIME] 1h 24m 31s
                logBuilder.append("[EXIT]: [RUNTIME] ");
                logBuilder.append(getRuntime()).append("\n");

                //end log
                logBuilder.append("[").append(TimeUtil.logTime()).append("] [EOL]: Log completed, exiting Cyder with exit code: ");
                logBuilder.append(representation);
                logBuilder.append(" [");
                logBuilder.append(getCodeDescription((int) representation));
                logBuilder.append("]");
                logBuilder.append(", exceptions thrown: ");
                logBuilder.append(countExceptions());

                //write
                writeLine(logBuilder.toString());

                //return to caller to exit immediately
                return;
            case CORRUPTION:
                //before user corruption method is called
                //[CORRUPTION]: [FILE] c:/users/nathan/downloads/CyderCorruptedUserData.zip
                logBuilder.append("[CORRUPTION]: userdir saved to: ").append(representation);
                break;
            case UNKNOWN:
                //[UNKNOWN]: CyderString.instance really anything that doesn't get caught above
                logBuilder.append("[UNKNOWN]: ");
                logBuilder.append(representation);
                break;
            case DEBUG_PRINT:
                logBuilder.append("[DEBUG]: ");
                logBuilder.append(representation);
                break;
            case HANDLE_METHOD:
                logBuilder.append("[HANDLE]: ");
                logBuilder.append(representation);
                break;
            case WIDGET_OPENED:
                logBuilder.append("[WIDGET OPENED]: ");
                logBuilder.append(representation);
                break;
            case PREFERENCE_REFRESH:
                logBuilder.append("[PREFERENCE REFRESH INVOKED]: ");
                logBuilder.append(representation);
                break;
            case THREAD:
                logBuilder.append("[THREAD STARTED]: ");
                logBuilder.append(representation);
                break;
            default:
                //this is here and not UNKNOWN as the default so that we can detect if
                // a log tag was added but not implemented
                throw new IllegalArgumentException("Handle case not found; you're probably an " +
                        "idiot and added an enum type but forgot to implement it here");
        }

        //write to log file
        if (logBuilder.toString().equalsIgnoreCase(initialTimeTag))
            throw new IllegalArgumentException("Attempting to write nothing to the log file");
        writeLine(logBuilder.toString());
    }

    /**
     * Attempt to figure out what Tag representation should be and log it.
     * @param representation the object we are trying to log
     * @param <T> any object
     */
    public static <T> void log(T representation) {
        if (representation instanceof JComponent) {
            LinkedList<Element> elements = new LinkedList<>();
            ElementIterator iterator = new ElementIterator(ConsoleFrame.getConsoleFrame().getOutputArea().getStyledDocument());
            Element element;

            while ((element = iterator.next()) != null) {
                elements.add(element);
            }

            for (Element value : elements) {
                if (value.toString().toLowerCase().contains(representation.toString().toLowerCase())) {
                    log(Tag.CONSOLE_OUT, representation);
                    return;
                }
            }
            log(Tag.CONSOLE_OUT, representation);
        } else if (representation instanceof File) {
            log(Tag.LINK, representation);
        } else if (representation.toString().toLowerCase().contains("exception")) {
            log(Tag.EXCEPTION, representation);
        } else if (representation.toString().contains("CLIENT_IO")) {
            log(Tag.CLIENT_IO, representation);
        } else if (representation.toString().contains("SYSTEM_IO")) {
            log(Tag.SYSTEM_IO, representation);
        } else {
            ArrayList<String> ops = ConsoleFrame.getConsoleFrame().getCommandHistory();
            for (String op : ops) {
                if (op.toLowerCase().contains(representation.toString())) {
                    log(Tag.CLIENT, representation);
                    return;
                }
            }
            log(Tag.UNKNOWN, representation);
        }
    }

    /**
     * Constructor that accepts a file in case we want to use a different file.
     * @param outputFile the file to write the log to
     */
    public static void initialize(File outputFile) {
        try {
            if (!outputFile.exists())
                outputFile.createNewFile();

            currentLog = outputFile;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Constructor for the logger to create a file and write to for the current session.
     */
    public static void initialize() {
        //create the log file
        generateAndSetLogFile();

        //fix last line of log files
        fixLogs();

        //consolidate lines of log files
        consolidateLines();

        //zip past log directories
        zipPastLogs();
    }

    /**
     * Getter for current log file
     * @return the log file associated with the current session
     */
    public static File getCurrentLog() {
        return currentLog;
    }

    /**
     * Creates the log file if it is not set/DNE
     * @return whether or not the file was created successfully
     */
    private static boolean generateAndSetLogFile() {
        try {
            File logsDir = new File("logs");
            logsDir.mkdir();

            String logSubDirName = TimeUtil.logSubDirTime();

            File logSubDir = new File("logs/" + logSubDirName);
            logSubDir.mkdir();

            String logFileName = TimeUtil.logTime();

            int number = 1;
            File logFile = new File("logs/" + logSubDirName + "/" + logFileName + "-" + number + ".log");

            while (logFile.exists()) {
                number++;
                logFile = new File("logs/" + logSubDirName + "/" + logFileName + "-" + number + ".log");
            }

            boolean success = logFile.createNewFile();

            if (success)
                currentLog = logFile;
            else
                throw new RuntimeException("Log file not created");

            return success;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return false;
    }

    private static final Semaphore writingSemaphore = new Semaphore(1);

    /**
     * Writes the line to the current log file and releases resources once done.
     * @param line the single line to write
     */
    private static void writeLine(String line) {
        //if we have to make a new line
        String recoveryLine = null;

        //if the current log doesn't exist, find a unique file name and make it
        if (!getCurrentLog().exists()) {
            generateAndSetLogFile();

            recoveryLine = "[log file/directory was deleted during runtime, recreating and restarting log: "
                    + TimeUtil.userTime() + "]";
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(currentLog,true))) {
            writingSemaphore.acquire();

            if (recoveryLine != null) {
                bw.write(recoveryLine.trim());
            }

            bw.write(line.trim());
            bw.newLine();

            writingSemaphore.release();
        } catch(Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            System.out.println(line.trim());
        }
    }

    /**
     * Counts the exceptions in the current log folder. This is used when closing the log to provide
     *  an exceptions summary.
     *
     * @return the int number of exceptions thrown in this Cyder session
     */
    private static int countExceptions() {
        int ret = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(currentLog))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("[EXCEPTION]"))
                    ret++;
            }
        } catch(Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    private static String getRuntime() {
        long milis = System.currentTimeMillis() - start;
        int seconds = 0;
        int hours = 0;
        int minutes = 0;

        while (milis > 1000) {
            seconds++;
            milis -= 1000;
        }

        while (seconds > 60) {
            minutes++;
            seconds -= 60;
        }

        while (minutes > 60) {
            hours++;
            minutes -= 60;
        }

        StringBuilder ret = new StringBuilder();

        if (hours != 0)
            ret.append(hours).append("h ");
        if (minutes != 0)
            ret.append(minutes).append("m ");
        if (seconds != 0)
            ret.append(seconds).append("s ");

        String retString = ret.toString().trim();

        return retString.length() == 0 ? "s" : retString;
    }

    private static String getCodeDescription(int code) {
        String ret = "UNKNOWN EXIT CODE";

        try {
            ArrayList<IOUtil.ExitCondition> conditions = IOUtil.getExitConditions();

            for (IOUtil.ExitCondition condition : conditions) {
                if (condition.getCode() == code) {
                    ret = condition.getDescription();
                    break;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * Zips the log files of the past.
     */
    public static void zipPastLogs() {
        File topLevelLogsDir = new File("logs");

        if (!topLevelLogsDir.exists()) {
            topLevelLogsDir.mkdir();
            return;
        }

        for (File subLogDir : topLevelLogsDir.listFiles()) {
            if (!FileUtil.getFilename(subLogDir.getName()).equals(TimeUtil.logSubDirTime())
                    && !FileUtil.getExtension(subLogDir).equalsIgnoreCase(".zip")) {
                if (new File(subLogDir.getAbsolutePath() + ".zip").exists()) {
                    OSUtil.deleteFolder(subLogDir);
                } else {
                    OSUtil.zip(subLogDir.getAbsolutePath(), subLogDir.getAbsolutePath() + ".zip");
                }
            }
        }
    }

    /**
     * Consolidates the lines of all non-zipped files within the logs directory.
     */
    public static void consolidateLines() {
        for (File subLogDir : new File("logs").listFiles()) {
            if (FileUtil.getExtension(subLogDir).equalsIgnoreCase(".zip"))
                continue;

            for (File logFile : subLogDir.listFiles())
                consolidateLines(logFile);
        }
    }

    /**
     * Consolidates duplicate lines next to each other of the provided file.
     *
     * @param file the file to consolidate duplicate lines of
     */
    public static void consolidateLines(File file) {
        if (!file.exists())
            throw new IllegalArgumentException("Provided file does not exist: " + file);
        else if (!FileUtil.getExtension(file).equalsIgnoreCase(".log"))
            throw new IllegalArgumentException("Provided file is not a log file: " + file);

        ArrayList<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = null;

            while ((line = br.readLine()) != null)
                lines.add(line);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        if (lines.size() < 2)
            return;

        ArrayList<String> writeLines = new ArrayList<>();

        String lastLine = lines.get(0);
        String currentLine = lines.get(1);
        int currentCount = 1;

        for (int i = 0 ; i < lines.size() - 1; i++) {
            lastLine = lines.get(i);
            currentLine = lines.get(i + 1);

            if (logLinesEquivalent(lastLine, currentLine)) {
                currentCount++;
            } else {
                if (currentCount > 1) {
                    writeLines.add(lastLine + " [" + currentCount + "x]");
                } else{
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

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file,false))) {
            for (String line : writeLines) {
                bw.write(line.trim());
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
    public static boolean logLinesEquivalent(String logLine1, String logLine2) {
        logLine1 = logLine1.trim();
        logLine2 = logLine2.trim();

        if (!logLine1.startsWith("[") || !logLine1.contains("]")
            || !logLine2.startsWith("[") || !logLine2.contains("]"))
            return logLine1.equals(logLine2);

        String timeTag1 = logLine1.substring(logLine1.indexOf("["), logLine2.indexOf("]") + 1).trim();
        String timeTag2 = logLine2.substring(logLine2.indexOf("["), logLine2.indexOf("]") + 1).trim();

        logLine1 = logLine1.replace(timeTag1, "");
        logLine2 = logLine2.replace(timeTag2, "");

        return !StringUtil.isNull(logLine1) && !StringUtil.isNull(logLine2) && logLine1.equals(logLine2);
    }

    /**
     * Upon entry this method attempts to fix any user logs that ended abruptly (an exit code of -1)
     * as a result of an IDE stop or OS Task Manager Stop.
     */
    public static void fixLogs() {
        try {
            for (File logDir : new File("logs").listFiles()) {
                //for all directories of days of logs
                if (FileUtil.getExtension(logDir).equalsIgnoreCase(".zip"))
                    continue;

                for (File log : logDir.listFiles()) {
                    if (!log.equals(Logger.getCurrentLog())) {
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
                            //usually an IDE stop but sometimes the program exits,
                            // with exit condition 1 due to something failing on startup
                            // which is why this says "crashed unexpectedly"
                            String logBuilder = "[" + TimeUtil.logTime() + "] [EOL]: " +
                                    "Log completed, Cyder crashed unexpectedly: " +
                                    "exit code: -200 [External Stop], exceptions thrown: " + exceptions;

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
}
