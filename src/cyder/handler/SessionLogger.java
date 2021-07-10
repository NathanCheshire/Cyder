package cyder.handler;

import cyder.ui.ConsoleFrame;
import cyder.utilities.StringUtil;
import cyder.utilities.TimeUtil;

import javax.swing.*;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;

public class SessionLogger {
    private SessionLogger() {}

    private static File currentLog;
    private static long start;

    public enum Tag {
        CLIENT, CONSOLE_OUT, EXCEPTION, ACTION, LINK, EOL, UNKNOWN, SUGGESTION,
        SYSTEM_IO, CLIENT_IO, LOGIN, LOGOUT, ENTRY, EXIT, CORRUPTION
    }

    /**
     * The main log method to log an action associated with a type tag.
     * @param tag - the type of data we are logging
     * @param representation - the representation of the object
     * @param <T> - the object instance of representation
     */
    public static <T> void log(Tag tag, T representation) {
        StringBuilder logBuilder = new StringBuilder("[" + TimeUtil.logTime() + "] ");

        switch (tag) {
            case CLIENT:
                logBuilder.append("[CLIENT]: ");
                logBuilder.append(representation);
                break;
            case CONSOLE_OUT:
                logBuilder.append("[CONSOLE_OUT]: ");
                if (representation instanceof ImageIcon) {
                    logBuilder.append("[ICON] ");
                    logBuilder.append(representation);
                }
                else if (representation instanceof JComponent) {
                    logBuilder.append("[JCOMPONENT] ");
                    logBuilder.append(representation);
                } else {
                    logBuilder.append("[UNKNOWN CONSOLE_OUT TYPE (TODO add type)] ");
                    logBuilder.append(representation);
                }
                break;
            case EXCEPTION:
                logBuilder.append("[EXCEPTION]: ");
                logBuilder.append(representation);
                logBuilder.append("\n");
                break;
            case ACTION:
                logBuilder.append("[ACTION]: ");
                if (representation instanceof JComponent) {
                    logBuilder.append("[").append(((JComponent) representation).getName()).append("] ");
                }
                logBuilder.append(representation);
                break;
            case LINK:
                logBuilder.append("[LINK]: ");
                if (representation instanceof File) {
                    logBuilder.append("[").append(StringUtil.getExtension((File) representation)).append("] ");
                }
                logBuilder.append(representation);
                break;
            case EOL:
                logBuilder.append("[EOL]: Log completed, exiting program with code: ");
                logBuilder.append(representation);
                logBuilder.append(", exceptions thrown: ");
                logBuilder.append(countExceptions());
                break;
            case SUGGESTION:
                logBuilder.append("[SUGGESTION]: ");
                logBuilder.append(representation);
                break;
            case CLIENT_IO:
                //[CLIENT_IO]: [WRITE] [KEY] ROUNDWINDOWS [VALUE] 0
                //[CLIENT_IO]: [READ] [KEY] VERSION
                logBuilder.append("[CLIENT_IO]: ");

                if (!representation.toString().contains(","))
                    throw new IllegalArgumentException("CLIENT_IO representation incorrect data format");

                String[] parts = representation.toString().split(",");

                if (parts.length != 3 && parts.length != 2) {
                    throw new IllegalArgumentException("CLIENT_IO representation does not contain sufficient data");
                } else {
                    logBuilder.append("[");
                    logBuilder.append(parts[0].toUpperCase());
                    logBuilder.append("] ");
                    logBuilder.append("[KEY] ");
                    logBuilder.append(parts[1].toUpperCase());
                    logBuilder.append(" ");

                    if (parts[0].equalsIgnoreCase("WRITE")) {
                        logBuilder.append("[VALUE]");
                        logBuilder.append(parts[2].toUpperCase());
                    }
                }
                break;
            case SYSTEM_IO:
                //[SYSTEM_IO]: [WRITE] [KEY] VERSION [VALUE] SOULTREE
                //[SYSTEM_IO]: [READ] [KEY] VERSION
                logBuilder.append("[SYSTEM_IO]: ");

                if (!representation.toString().contains(","))
                    throw new IllegalArgumentException("SYSTEM_IO representation incorrect data format");

                String[] parters = representation.toString().split(",");

                if (parters.length != 3 && parters.length != 2) {
                    throw new IllegalArgumentException("SYSTEM_IO representation does not contain sufficient data");
                } else {
                    logBuilder.append("[");
                    logBuilder.append(parters[0].toUpperCase());
                    logBuilder.append("] ");
                    logBuilder.append("[KEY] ");
                    logBuilder.append(parters[1].toUpperCase());
                    logBuilder.append(" ");

                    if (parters[0].equalsIgnoreCase("WRITE")) {
                        logBuilder.append("[VALUE] ");
                        logBuilder.append(parters[2].toUpperCase());
                    }
                }
                break;
            case LOGIN:
                //[LOGIN]: [NATHAN] Autocyphered (STD Login)
                logBuilder.append("[LOGIN]: [");
                logBuilder.append(representation);
                logBuilder.append("]");
                break;
            case LOGOUT:
                //[LOGOUT]: [NATHAN]
                logBuilder.append("[LOGOUT]: [");
                logBuilder.append(representation);
                logBuilder.append("]");
                break;
            case ENTRY:
                //[ENTRY]: [WINUSER=NATHAN]
                start = System.currentTimeMillis();
                logBuilder.append("[ENTRY]: [WINUSER=");
                logBuilder.append(representation);
                logBuilder.append("]");
                break;
            case EXIT:
                //[EXIT]: [RUNTIME] 1h 24m 31s
                logBuilder.append("[ENTRY]: [RUNTIME] ");
                logBuilder.append(representation);
                break;
            case CORRUPTION:
                //[CORRUPTION]: [FILE] c:/users/nathan/downloads/CyderCorruptedUserData.zip
                break;
            case UNKNOWN:
                //[UNKNOWN]: CyderString.instance really anything that doesn't get caught above
                logBuilder.append("[UNKNOWN]: ");
                logBuilder.append(representation);
                break;
        }

        writeLine(logBuilder.toString());
    }

    /**
     * Attempt to figure out what Tag representation should be and log it.
     * @param representation - the object we are trying to log
     * @param <T> - any object
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
            ArrayList<String> ops = ConsoleFrame.getConsoleFrame().getOperationList();
            for (String op : ops) {
                if (op.toLowerCase().contains(representation.toString())) {
                    log(Tag.CLIENT);
                    return;
                }
            }
            log(Tag.UNKNOWN, representation);
        }
    }

    /**
     * Constructor that accepts a file in case we want to use a different file.
     * @param outputFile - the file to write the log to
     */
    public static void SessionLogger(File outputFile) {
        try {
            if (!StringUtil.getFilename(outputFile.getParent()).equals("logs"))
                throw new IllegalArgumentException("Attempting to place a log file outside of the logs directory");

            if (!outputFile.exists())
                outputFile.createNewFile();

            currentLog = outputFile;
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Constructor for the logger to create a file and write to for the current session.
     */
    public static void SessionLogger() {
        try {
            File logsDir = new File("logs");
            if (!logsDir.exists())
                logsDir.mkdir();

            String uniqueLogString = TimeUtil.logFileTime();

            int number = 1;
            File logFile = new File("throws/" + uniqueLogString + "-" + number + ".log");

            while (logFile.exists()) {
                number++;
                logFile = new File("throws/" + uniqueLogString + "-" + number + ".log");
            }

            logFile.createNewFile();
            SessionLogger(logFile);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Getter for current log file
     * @return - the log file associated with the current session
     */
    public static File getCurrentLog() {
        return currentLog;
    }

    /**
     * Writes the line to the current log file and releases resources once done.
     * @param line - the single line to write
     */
    private static void writeLine(String line) {
        try (BufferedWriter br = new BufferedWriter(new FileWriter(currentLog))) {
           br.write(line);
           br.newLine();
        } catch(Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Counts the exceptions in the current log folder. This is used when closing the log to provide
     *  an exceptions summary.
     * @return - the int number of exceptions thrown in this Cyder session
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
            ErrorHandler.handle(e);
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

        return ret.toString().trim();
    }
}
