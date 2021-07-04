package cyder.handler;

import cyder.utilities.TimeUtil;

import java.io.File;

public class SessionLogger {
    private SessionLogger() {}

    private static File currentLog;

    //todo user logs and throws should eventually be converted to
    // binary data so that you can only access it through the program

    //todo add reading and writing calls to/from userdata to the log

    public enum Tag {
        CLIENT, CONSOLE_OUT, EXCEPTION, ACTION, LINK, EOL, UNKNOWN, SUGGESTION
    }

    public static void log(Tag tag, String stringRepresentation) {
        StringBuilder logBuilder = new StringBuilder("[" + TimeUtil.logTime() + "] ");

        switch (tag) {
            case CLIENT:
                logBuilder.append("[CLIENT]: ");

                break;
            case CONSOLE_OUT:
                logBuilder.append("[CONSOLE_OUT]: ");

                break;
            case EXCEPTION:
                logBuilder.append("[EXCEPTION]: ");

                break;
            case ACTION:
                logBuilder.append("[ACTION]: ");

                break;
            case LINK:
                logBuilder.append("[LINK]: ");

                break;
            case EOL:
                //todo maybe an exit method that you can pass a code that will log it and end the log first
                // and then go on to exit and everything else

                logBuilder.append("[EOL]: Log completed, exiting program with code: ");
                logBuilder.append(stringRepresentation);
                logBuilder.append(", exceptions thrown: ");
                logBuilder.append(countExceptions());
                break;
            case UNKNOWN:
                logBuilder.append("[UNKNOWN]: ");

                break;
            case SUGGESTION:
                logBuilder.append("[SUGGESTION]: ");

                break;
        }

        //todo write logBuilder.toString();
    }

    //attempt to figure out the tag and pass on to the above method
    public static void log(String stringRepresentation) {

    }

    public static void SessionLogger(File outputFile) {
        try {
            if (!outputFile.exists())
                outputFile.createNewFile();

            currentLog = outputFile;
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

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

    private static File getCurrentLog() {
        return currentLog;
    }

    private static int countExceptions() {
        //todo count exceptions thrown in currnet log file
        return 0;
    }
}
