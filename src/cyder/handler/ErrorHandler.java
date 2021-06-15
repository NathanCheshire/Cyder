package cyder.handler;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.genesis.CyderMain;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.ui.DragLabel;
import cyder.utilities.IOUtil;
import cyder.utilities.SystemUtil;
import cyder.utilities.TimeUtil;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
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

            throwsDir = new File("throws");
            eFileString = "throws/" + TimeUtil.errorTime() + ".error";

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
            String message = e.getMessage();
            String write = message == null ? "" : message + "\n" + c + "\n" + "Error thrown by line: " + lineNumber +
                    "\n\nStack Trace:\n\n" + stackTrack;

            //write to file, flush, close
            BufferedWriter errorWriter = new BufferedWriter(new FileWriter(eFileString));
            errorWriter.write(write);
            errorWriter.newLine();
            errorWriter.flush();
            errorWriter.close();

            //if the user has show errors configured, then we open the file
            if (IOUtil.getUserData("SilenceErrors").equals("0")) {
                windowedError(message, write, eFileString);
            }
        }

        //uh oh; error was thrown inside of here so we'll just generic inform the user of it
        catch (Exception ex) {
            if (CyderMain.consoleFrame != null && CyderMain.consoleFrame.isVisible()) {
                //todo uncomment me once ConsoleFrame migration is complete
                //ConsoleFrame.notify(ex.getMessage());

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);

                ex.printStackTrace(pw);
                ex.printStackTrace();

                String stackTrack = sw.toString();
                int lineNumber = ex.getStackTrace()[0].getLineNumber();
                Class c = ex.getClass();

                String write = ex.getMessage() + "\n" + c + "\n" + "Error thrown by line: " + lineNumber +
                        "\n\nStack Trace:\n\n" + stackTrack;

                windowedError(ex.getMessage(), write, null);
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

            throwsDir = new File("throws");
            eFileString = "throws/" + TimeUtil.errorTime() + ".error";

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

            //get our master string and write it to the
            String message = e.getMessage();
            String write = message == null ? "" : message + "\n" + c + "\n" + "Error thrown by line: " + lineNumber +
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

    @Override
    public String toString() {
        return "ErrorHandler object, hash=" + this.hashCode();
    }

    private static void windowedError(String title, String message, String errorFilePath) {
        CyderFrame errorFrame = new CyderFrame();
        errorFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        errorFrame.setTitle(title);
        errorFrame.initializeBackgroundResizing();
        errorFrame.setResizable(true);
        errorFrame.setMaximumSize(new Dimension(800,800));
        errorFrame.setBackground(CyderColors.vanila);

        String displayText = message.substring(0,message.length() > 500 ? 500 : message.length());
        int w = 0;
        int h = 0;

        //calculate text bounds needed and set label bounds
        //add label to error frame and set error frame bounds
        //test with multiple error types

        errorFrame.setBounds(SystemUtil.getScreenWidth() - w,
                SystemUtil.getScreenHeight() - errorFrame.getHeight(), w + 2 * 5, h + 30 + 2 * 5);
        errorFrame.setVisible(true);
    }
}