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
            File throwsDir = new File("src/users/" + ConsoleFrame.getUUID() + "/Throws/");

            if (!throwsDir.exists())
                throwsDir.mkdir();

            String eFileString = "src/users/" + ConsoleFrame.getUUID() + "/Throws/" + TimeUtil.errorTime() + ".error";
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

            if (IOUtil.getUserData("SilenceErrors").equals("1"))
                return;
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

                GenericInform.inform(write,"Error trace",600,600);
                //todo don't have to pass width and height anymore, it should make it a square based on calculations
            }
        }
    }
}
