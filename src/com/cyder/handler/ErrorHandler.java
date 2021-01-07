package com.cyder.handler;

import com.cyder.genesis.CyderMain;
import com.cyder.ui.ConsoleFrame;
import com.cyder.utilities.IOUtil;
import com.cyder.utilities.TimeUtil;
import com.cyder.widgets.GenericInform;

import java.io.*;

public class ErrorHandler {

    public static void handle(Exception e) {
        try {
            String user = ConsoleFrame.getUUID();
            File throwsDir = null;
            String eFileString = "";

            if (user == null) {
                throwsDir = new File("src/com/cyder/genesis/Throws");
                eFileString = "src/com/cyder/genesis/Throws/" + TimeUtil.errorTime() + ".error";
            }

            else {
                throwsDir = new File("src/users/" + ConsoleFrame.getUUID() + "/Throws");
                eFileString = "src/users/" + ConsoleFrame.getUUID() + "/Throws/" + TimeUtil.errorTime() + ".error";
            }

            if (!throwsDir.exists())
                throwsDir.mkdir();

            File eFile = new File(eFileString);
            eFile.createNewFile();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            String stackTrack = sw.toString();
            int lineNumber = e.getStackTrace()[0].getLineNumber();
            Class c = e.getClass();

            String write = "Error thrown by line: " + lineNumber + " from\n" + c +
                    "\n\nStack Trace:\n\n" + stackTrack;

            BufferedWriter errorWriter = new BufferedWriter(new FileWriter(eFileString));
            errorWriter.write(write);
            errorWriter.newLine();
            errorWriter.flush();
            errorWriter.close();

            if (IOUtil.getUserData("SilenceErrors").equals("0"))
                IOUtil.openFile(eFileString);
        }

        catch (Exception ex) {
            if (CyderMain.consoleFrame != null && CyderMain.consoleFrame.isVisible()) {
                //todo uncomment ConsoleFrame.notify(ex.getMessage());

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
}
