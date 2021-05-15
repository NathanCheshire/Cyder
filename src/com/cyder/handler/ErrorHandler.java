package com.cyder.handler;

import com.cyder.genesis.CyderMain;
import com.cyder.ui.ConsoleFrame;
import com.cyder.utilities.IOUtil;
import com.cyder.utilities.TimeUtil;
import com.cyder.widgets.GenericInform;

import java.io.*;

public class ErrorHandler {

    /**
     * This method takes an exception, prints it to a string, and then writes that string to
     * either the user's Throws dir or the system throws dir
     * @param e the exception we are handling and possibly informing the user of
     */
    public static void handle(Exception e) {
        try {
            //find out whereto log the error
            String user = ConsoleFrame.getUUID();
            File throwsDir = null;
            String eFileString = "";

            //if no user, then put error in system throws folder
            if (user == null) {
                throwsDir = new File("src/com/cyder/genesis/Throws");
                eFileString = "src/com/cyder/genesis/Throws/" + TimeUtil.errorTime() + ".error";
            }

            else {
                throwsDir = new File("src/users/" + ConsoleFrame.getUUID() + "/Throws");
                eFileString = "src/users/" + ConsoleFrame.getUUID() + "/Throws/" + TimeUtil.errorTime() + ".error";
            }

            //make the dir if it doesn't exist
            if (!throwsDir.exists())
                throwsDir.mkdir();

            //make the file we are going to write to
            File eFile = new File(eFileString);
            eFile.createNewFile();

            //obtain a String object of the error and the line number
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            String stackTrack = sw.toString();
            int lineNumber = e.getStackTrace()[0].getLineNumber();
            Class c = e.getClass();

            //get our master string and write it to the file
            String write = "Error thrown by line: " + lineNumber + " from\n" + c +
                    "\n\nStack Trace:\n\n" + stackTrack;

            //write to file, flush, close
            BufferedWriter errorWriter = new BufferedWriter(new FileWriter(eFileString));
            errorWriter.write(write);
            errorWriter.newLine();
            errorWriter.flush();
            errorWriter.close();

            //todo don't open immediately, notify from right top and add a listener so that
            // a click on the inform() will open the file

            //if the user has show errors configured, then we open the file
            if (IOUtil.getUserData("SilenceErrors").equals("0"))
                IOUtil.openFile(eFileString);
        }

        //uh oh; error was thrown inside of here so we'll just generic inform the user of it
        catch (Exception ex) {
            if (CyderMain.consoleFrame != null && CyderMain.consoleFrame.isVisible()) {
                //todo uncomment me once ConsoleFrame is migrated
                //ConsoleFrame.notify(ex.getMessage());

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);

                String stackTrack = sw.toString();
                int lineNumber = ex.getStackTrace()[0].getLineNumber();
                Class c = ex.getClass();

                String write = "Error thrown by line: " + lineNumber + " from\n" + c +
                        "\n\nStack Trace:\n\n" + stackTrack;

                GenericInform.inform(write,"Error trace");
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
            //find out whereto log the error
            String user = ConsoleFrame.getUUID();
            File throwsDir = null;
            String eFileString = "";

            //if no user, then put error in system throws folder
            if (user == null) {
                throwsDir = new File("src/com/cyder/genesis/Throws");
                eFileString = "src/com/cyder/genesis/Throws/" + TimeUtil.errorTime() + ".error";
            }

            else {
                throwsDir = new File("src/users/" + ConsoleFrame.getUUID() + "/Throws");
                eFileString = "src/users/" + ConsoleFrame.getUUID() + "/Throws/" + TimeUtil.errorTime() + ".error";
            }

            //make the dir if it doesn't exist
            if (!throwsDir.exists())
                throwsDir.mkdir();

            //make the file we are going to write to
            File eFile = new File(eFileString);
            eFile.createNewFile();

            //obtain a String object of the error and the line number
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            String stackTrack = sw.toString();
            int lineNumber = e.getStackTrace()[0].getLineNumber();
            Class c = e.getClass();

            //get our master string and write it to the file
            String write = "Error thrown by line: " + lineNumber + " from\n" + c +
                    "\n\nStack Trace:\n\n" + stackTrack;

            //write to file, flush, close
            BufferedWriter errorWriter = new BufferedWriter(new FileWriter(eFileString));
            errorWriter.write(write);
            errorWriter.newLine();
            errorWriter.flush();
            errorWriter.close();
        }

        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
