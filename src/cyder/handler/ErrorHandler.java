package cyder.handler;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.utilities.IOUtil;
import cyder.utilities.SystemUtil;

import javax.swing.*;
import java.awt.*;
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
                    IOUtil.getUserData("SilenceErrors").equals("0")) {
                System.out.println("\nOriginal error:\n");
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
            ex.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "ErrorHandler object, hash=" + this.hashCode();
    }

    private static void windowedError(String title, String message) {
        if ((title == null || title.length() == 0) && (message == null || message.length() == 0)) {
            System.out.println("Windowed error was passed null");
            return;
        }

        //bounds calculation for centered text
        String displayText = message.substring(0, Math.min(message.length(), 500));
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

        //window needs to be bigger than the label
        int windowWidth = w + 2 * 5;
        int windowHeight = h + 5 + 35;

        CyderFrame errorFrame = new CyderFrame(windowWidth, windowHeight, Color.white);
        errorFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        errorFrame.setTitle(title.length() == 0 ? "Null error message" : title);

        //label setup
        JLabel displayLabel = new JLabel(displayText);
        displayLabel.setForeground(CyderColors.navy);
        displayLabel.setFont(CyderFonts.defaultFontSmall);
        displayLabel.setHorizontalAlignment(JLabel.CENTER);
        displayLabel.setVerticalAlignment(JLabel.CENTER);
        displayLabel.setSize(w, h);
        displayLabel.setLocation(5, 35);
        errorFrame.getContentPane().add(displayLabel);

        errorFrame.setVisible(true);
        errorFrame.setAlwaysOnTop(true);
        errorFrame.setLocation(SystemUtil.getScreenWidth() - windowWidth,
                SystemUtil.getScreenHeight() - windowHeight);
    }
}