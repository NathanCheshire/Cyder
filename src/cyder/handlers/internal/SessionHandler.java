package cyder.handlers.internal;

import cyder.consts.CyderStrings;
import cyder.ui.ConsoleFrame;
import cyder.utilities.IOUtil;
import cyder.utilities.IOUtil.SystemData;
import cyder.utilities.StringUtil;
import cyder.utilities.TimeUtil;

import javax.swing.*;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;

public class SessionHandler {
    private SessionHandler() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    private static File currentLog;

    //absolute start of Cyder, class loading
    private static long start = System.currentTimeMillis();

    public enum Tag {
        CLIENT, //client typed something
        CONSOLE_OUT, //printing something to the console frame
        EXCEPTION, //an exception
        ACTION, // an action taken
        LINK, // a link in anyway is printed, represented, etc.
        UNKNOWN, // not sure
        SUGGESTION, // logging a suggestion
        SYSTEM_IO, // input or output to/from sys.json
        CLIENT_IO, // input or output to/from userdata.json
        LOGIN, // user is logged in
        LOGOUT, // user is logged out
        JAVA_ARGS, // java args upon JVM entry to main
        ENTRY, // entry of program
        EXIT, // exit of program
        CORRUPTION, // corruption of userdata.json
        PRIVATE_MESSAGE_SENT, //sending a message through the chat view
        PRIVATE_MESSAGE_RECEIVED, //received a message through the chat view
    }

    /**
     * The main log method to log an action associated with a type tag.
     * @param tag the type of data we are logging
     * @param representation the representation of the object
     * @param <T> the object instance of representation
     */
    public static <T> void log(Tag tag, T representation) {
        StringBuilder logBuilder = new StringBuilder("[" + TimeUtil.logTime() + "] ");

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
            case ACTION: //catch actions for more cyder components in future like text field
                logBuilder.append("[ACTION]: ");
                logBuilder.append(representation);
                break;
            case LINK:
                //files opened, links opened
                logBuilder.append("[LINK]: ");
                if (representation instanceof File) {
                    logBuilder.append("[").append(StringUtil.getExtension((File) representation)).append("] ");
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

                //exit using the exit code right after logging it
                System.exit(Integer.parseInt(String.valueOf(representation)));
                break;
            case CORRUPTION:
                //before user corruption method is called
                //[CORRUPTION]: [FILE] c:/users/nathan/downloads/CyderCorruptedUserData.zip
                logBuilder.append("[CORRUPTION]: userdir saved to: ").append(representation);
                break;
            case PRIVATE_MESSAGE_SENT:
                //[PRIVATE MESSAGE]: [RECEIVED FROM SAM (UUID here)] Check discord.
                logBuilder.append("[PRIVATE MESSAGE]: ");
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
     * @param outputFile the file to write the log to
     */
    public static void SessionLogger(File outputFile) {
        try {
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
        //create the log file
        generateAndSetLogFile();
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
            ErrorHandler.handle(e);
        }

        return false;
    }

    /**
     * Writes the line to the current log file and releases resources once done.
     * @param line the single line to write
     */
    private static void writeLine(String line) {
        try {
            //if the current log doesn't exist, find a unique file name and make it
            if (!getCurrentLog().exists()) {
                generateAndSetLogFile();

                FileWriter fw = new FileWriter(currentLog,true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write("[log file/directory was deleted during runtime, recreating and restarting log: " + TimeUtil.userTime() + "]");
                bw.write(line.trim());
                bw.newLine();
                bw.close();
            }
            //otherwise just write to the current log file
            else {
                FileWriter fw = new FileWriter(currentLog,true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(line.trim());
                bw.newLine();
                bw.close();
            }
        } catch(Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Counts the exceptions in the current log folder. This is used when closing the log to provide
     *  an exceptions summary.
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

        String retString = ret.toString().trim();

        return retString.length() == 0 ? "s" : retString;
    }

    private static String getCodeDescription(int code) {
        String ret = "UNKNOWN EXIT CODE";

        try {
            LinkedList<SystemData.ExitCondition> conditions = IOUtil.getSystemData().getExitconditions();

            for (SystemData.ExitCondition condition : conditions) {
                if (condition.getCode() == code) {
                    ret = condition.getDescription();
                    break;
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return ret;
    }
}
