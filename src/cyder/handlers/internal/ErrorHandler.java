package cyder.handlers.internal;

import cyder.consts.CyderStrings;
import cyder.testing.DebugConsole;
import cyder.ui.ConsoleFrame;
import cyder.utilities.UserUtil;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorHandler {
    private ErrorHandler() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * This method takes an exception, prints it to a string, and then passes the
     * error to the SessionLogger to be logged
     * @param e the exception we are handling and possibly informing the user of
     */
    public static void handle(Exception e) {
        //always print to the IDE console, this saves time in the long run
        e.printStackTrace();

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
                SessionHandler.log(SessionHandler.Tag.EXCEPTION, write);

            //if the user has show errors configured, then we open the file
            if (ConsoleFrame.getConsoleFrame().getUUID() != null &&
                    !ConsoleFrame.getConsoleFrame().isClosed() &&
                    UserUtil.getUserData("SilenceErrors").equals("0")) {
                silentHandleWithoutLogging(e);
            }
        }

        //uh oh; error was thrown inside of here so we'll just generic inform the user of it
        catch (Exception ex) {
            silentHandleWithoutLogging(ex);
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
                SessionHandler.log(SessionHandler.Tag.EXCEPTION, write);
        } catch (Exception ex) {
            silentHandleWithoutLogging(ex);
        }
    }

    /**
     * Handles the exception by displaying a CyderFrame with the exception on it
     * (does not log the message. As such, this method should only be used in rare scenarios)
     * @param e the exception to be displayed
     */
    private static void silentHandleWithoutLogging(Exception e) {
        String title = e.getMessage();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrack = sw.toString();

        if (title != null && title.length() != 0 && stackTrack != null || stackTrack.length() != 0) {
            DebugConsole.println(title + "\n" + stackTrack);
        }
    }
}