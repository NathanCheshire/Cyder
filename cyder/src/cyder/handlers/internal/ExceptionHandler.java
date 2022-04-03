package cyder.handlers.internal;

import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.enums.LoggerTag;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.objects.InformBuilder;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderFrame;
import cyder.utilities.*;
import cyder.utilities.objects.BoundsString;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExceptionHandler {
    /**
     * Restrict default constructor.
     */
    private ExceptionHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * This method takes an exception, prints it to a string, and then passes the
     * error to the SessionLogger to be logged
     * @param e the exception we are handling and possibly informing the user of
     */
    public static void handle(Exception e) {
        try {
            Optional<String> write = getPrintableException(e);

            if (write.isPresent() && !write.get().trim().isEmpty())
                Logger.log(LoggerTag.EXCEPTION, write.get());

            // if user wants to be informed of exceptions
            if (ConsoleFrame.INSTANCE.getUUID() != null &&
                    !ConsoleFrame.INSTANCE.isClosed() &&
                    UserUtil.getCyderUser().getSilenceerrors().equals("0")) {

               showExceptionPane(e);
            }
        } catch (Exception ex) {
            // uh oh
            Logger.Debug(getPrintableException(e));
        }
    }

    /**
     * The red color used for exception panes.
     */
    private static final Color exceptionRed = new Color(254, 157, 158);

    /**
     * The white color used for exception pane text.
     */
    private static final Color exceptionWhite = new Color(254, 254, 254);

    /**
     * The opacity delta to increment/decrement by.
     */
    private static final float opacityShiftDelta = 0.05f;

    /**
     * Shows a popup pane containing a preview of the exception.
     * If the user clicks on the popup, it vanishes immediately and the
     * current log is opened externally.
     *
     * @param e the exception to preview/show
     */
    private static void showExceptionPane(Exception e) {
        Optional<String> informTextOptional = getPrintableException(e);

        if (informTextOptional.isPresent()) {
            AtomicBoolean escapeOpacityThread = new AtomicBoolean();
            escapeOpacityThread.set(false);

            BoundsString bounds = BoundsUtil.widthHeightCalculation(informTextOptional.get().replace("\n", ""),
                    CyderFonts.defaultFontSmall, 500);

            String informText = informTextOptional.get();

            // todo think of better way to do borderless frames
            CyderFrame borderlessFrame = new CyderFrame(bounds.getWidth() + 20,
                    bounds.getHeight() + 20, exceptionRed, "borderless");
            borderlessFrame.setTitle(e.getMessage());

            JLabel label = new JLabel("<html>" + informText + "</html>");
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    escapeOpacityThread.set(true);

                    borderlessFrame.dispose(true);

                    IOUtil.openFileOutsideProgram(Logger.getCurrentLog().getAbsolutePath());
                }
            });
            label.setForeground(exceptionWhite);
            label.setFont(CyderFonts.defaultFontSmall);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.CENTER);
            label.setBounds(10, 10, bounds.getWidth(), bounds.getHeight());
            borderlessFrame.getContentPane().add(label);

            borderlessFrame.setLocation(ScreenUtil.getScreenWidth() - borderlessFrame.getWidth(),
                    ScreenUtil.getScreenHeight() - borderlessFrame.getHeight());
            borderlessFrame.setAlwaysOnTop(true);

            borderlessFrame.setOpacity(0.0f);
            borderlessFrame.setVisible(true);

            CyderThreadRunner.submit(() -> {
                try {
                    for (float i = 0.0f ; i <= 1 ; i += opacityShiftDelta) {
                        if (escapeOpacityThread.get())
                            return;

                        borderlessFrame.setOpacity(i);
                        borderlessFrame.repaint();
                        Thread.sleep(20);
                    }

                    borderlessFrame.setOpacity(1.0f);
                    borderlessFrame.repaint();
                    Thread.sleep(5000);

                    if (escapeOpacityThread.get())
                        return;

                    for (float i = 1.0f ; i >= 0 ; i -= opacityShiftDelta) {
                        if (escapeOpacityThread.get())
                            return;

                        borderlessFrame.setOpacity(i);
                        borderlessFrame.repaint();
                        Thread.sleep(20);
                    }

                    if (escapeOpacityThread.get())
                        return;

                    borderlessFrame.dispose(true);
                } catch (Exception ex) {
                    Logger.Debug(getPrintableException(ex));
                }
            }, "Exception Popup Opacity Animator: " + e.getMessage());
        }
    }

    /**
     * This method handles an exception the same way as {@link ExceptionHandler#handle(Exception)} (String)}
     * except it does so without informing the user/developer/etc.
     * @param e the exception to be silently handled
     */
    public static void silentHandle(Exception e) {
        try {
            Optional<String> write = getPrintableException(e);

            if (write.isPresent() && !write.get().trim().isEmpty())
                Logger.log(LoggerTag.EXCEPTION, write.get());
        } catch (Exception ex) {
            // uh oh
            Logger.Debug(getPrintableException(e));
        }
    }

    /**
     * Generates a printable version of the exception.
     *
     * @param e the exception to return a printable version of
     * @return Optional String possibly containing exception details and trace
     */
    public static Optional<String> getPrintableException(Exception e) {
        //should be highly unlikely if not impossible
        if (e == null)
            return Optional.empty();

        //init streams to get information from the Exception
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        //print to the stream
        e.printStackTrace(pw);

        //full stack trace
        int lineNumber = e.getStackTrace()[0].getLineNumber();
        String stackTrace = sw.toString();

        //one or more white space, "at" literal, one or more white space
        String[] stackSplit = stackTrace.split("\\s+at\\s+");

        StringBuilder exceptionPrintBuilder = new StringBuilder();

        if (stackSplit.length > 1) {
            exceptionPrintBuilder.append("\nException origin: ").append(stackSplit[1]);
        } else {
            exceptionPrintBuilder.append("\nException origin not found");
        }

        //line number
        if (lineNumber != 0)
            exceptionPrintBuilder.append("\nFrom line: ").append(lineNumber);
        else
            exceptionPrintBuilder.append("\nThrowing line not found");

        //trace
        exceptionPrintBuilder.append("\nTrace: ").append(stackTrace);

        return Optional.of(exceptionPrintBuilder.toString());
    }

    /**
     * Shows a popup with the provided error message. When the opened popup frame is disposed,
     * Cyder exits.
     *
     * @param message the message of the popup
     * @param title the title of the popup
     * @param condition the exit condition to log when exiting
     */
    public static void exceptionExit(String message, String title, ExitCondition condition) {
        InformBuilder builder = new InformBuilder(message);
        builder.setTitle(title);
        builder.setPostCloseAction(() -> OSUtil.exit(condition));
        InformHandler.inform(builder);
    }
}