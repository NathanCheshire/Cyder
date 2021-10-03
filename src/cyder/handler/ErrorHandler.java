package cyder.handler;

import cyder.test.DebugConsole;
import cyder.ui.ConsoleFrame;
import cyder.utilities.UserUtil;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorHandler {

    /**
     * This method takes an exception, prints it to a string, and then passes the
     * error to the SessionLogger to be logged
     * @param e the exception we are handling and possibly informing the user of
     */
    public static void handle(Exception e) {
        try {
            //obtain a String object of the error and the line number
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            String stackTrack = sw.toString();
            int lineNumber = e.getStackTrace()[0].getLineNumber();
            Class c = e.getClass();

            //get our master string and write it to the
            String message = e.getMessage();
            String write = e.getMessage() == null ? "" :
                    "\n" + e.getMessage() + "\nThrown from:\n" + stackTrack.split("\\s+at\\s+")[1] + "\n"
                            + "StackTrace:\n" + stackTrack;

            if (write.trim().length() > 0)
                SessionLogger.log(SessionLogger.Tag.EXCEPTION, write);

            //if the user has show errors configured, then we open the file
            if (ConsoleFrame.getConsoleFrame().getUUID() != null &&
                    !ConsoleFrame.getConsoleFrame().isClosed() &&
                    UserUtil.getUserData("SilenceErrors").equals("0")) {
                DebugConsole.println("\nOriginal error:\n");
                e.printStackTrace();
                windowedError(message, write);
            }
        }

        //uh oh; error was thrown inside of here so we'll just generic inform the user of it
        catch (Exception ex) {
            if (!ConsoleFrame.getConsoleFrame().isClosed()) {
                ex.printStackTrace();

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);

                ex.printStackTrace(pw);
                ex.printStackTrace();

                String stackTrack = sw.toString();
                int lineNumber = ex.getStackTrace()[0].getLineNumber();
                Class c = ex.getClass();

                String write = e.getMessage() == null ? "" :
                        "\n" + e.getMessage() + "\nThrown from:\n" + stackTrack.split("\\s+at\\s+")[1] + "\n"
                                + "StackTrace:\n" + stackTrack;

                windowedError(ex.getMessage(), write);
            }
        }
    }

    /**
     * This method handles an exception the same way as {@link ErrorHandler#handle(Exception)} (String)}
     * except it does so without informing the user/developer/etc.
     * @param e the exception to be silently handled
     */
    public static void silentHandle(Exception e) {
        try {
            //obtain a String object of the error and the line number
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            String stackTrack = sw.toString();
            int lineNumber = e.getStackTrace()[0].getLineNumber();
            Class c = e.getClass();

            //get our master string and write it to the
            String write = e.getMessage() == null ? "" :
                    "\n" + e.getMessage() + "\nThrown from:\n" + stackTrack.split("\\s+at\\s+")[1] + "\n"
                            + "StackTrace:\n" + stackTrack;

            if (write.trim().length() > 0)
                SessionLogger.log(SessionLogger.Tag.EXCEPTION, write);
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }

    @Override
    public String toString() {
        return "ErrorHandler object, hash=" + this.hashCode();
    }

    private static void windowedError(String title, String message) {
        if ((title == null || title.length() == 0) && (message == null || message.length() == 0)) {
            DebugConsole.println("Windowed error was passed null");
            return;
        }

        DebugConsole.println(title.length() == 0 ? "Null error message" : title);
        DebugConsole.println(message);
    }
}