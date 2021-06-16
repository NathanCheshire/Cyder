package cyder.handler;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.genesis.CyderMain;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.utilities.IOUtil;
import cyder.utilities.SystemUtil;
import cyder.utilities.TimeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
                //todo ConsoleFrame.notify(ex.getMessage());

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
        //setup frame
        CyderFrame errorFrame = new CyderFrame();
        errorFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        errorFrame.setTitle(title);
        errorFrame.initializeBackgroundResizing();
        errorFrame.setResizable(true);
        errorFrame.setMaximumSize(new Dimension(800,800)); //this isn't working!!????
        errorFrame.setBackground(CyderColors.vanila);
        errorFrame.setMaximumSize(new Dimension(1000, 1000));
        errorFrame.setMinimumSize(new Dimension(200, 200));

        //bounds calculation for centered text
        String displayText = message.substring(0,message.length() > 500 ? 500 : message.length());
        displayText = "<html><div style='text-align: center;'>" + displayText + "</div></html>";
        int w = CyderFrame.getMinWidth(displayText, CyderFonts.defaultFontSmall) + 10; //extra 10 to be safe
        int h = CyderFrame.getMinHeight(displayText, CyderFonts.defaultFontSmall) + 2; //1 pixel for top and bottom
        int heightIncrement = h;

        //move dimensions from width to height if needed
        while (w > SystemUtil.getScreenWidth() / 2) {
            w /= 2;
            h += heightIncrement;
        }

        h += heightIncrement;

        //label setup
        JLabel displayLabel = new JLabel(displayText);
        displayLabel.setForeground(CyderColors.navy);
        displayLabel.setFont(CyderFonts.defaultFontSmall);
        displayLabel.setHorizontalAlignment(JLabel.CENTER);
        displayLabel.setVerticalAlignment(JLabel.CENTER);
        displayLabel.setSize(w, h);
        displayLabel.setLocation(5, 35);
        errorFrame.add(displayLabel);
        displayLabel.setToolTipText(errorFilePath == null ? "Error stack trace" : "Click to open error file");

        //mouse listener to open file on click
        displayLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                errorFrame.closeAnimation();
                IOUtil.openFile(errorFilePath);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                displayLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                displayLabel.setForeground(CyderColors.navy);
            }
        });

        //window needs to be bigger than the label
        int windowWidth = w + 2 * 5;
        int windowHeight = h + 5 + 35;

        errorFrame.setBounds(SystemUtil.getScreenWidth() - windowWidth,
                SystemUtil.getScreenHeight() - windowHeight, windowWidth, windowHeight);
        errorFrame.setVisible(true);
        errorFrame.setAlwaysOnTop(true);
    }
}