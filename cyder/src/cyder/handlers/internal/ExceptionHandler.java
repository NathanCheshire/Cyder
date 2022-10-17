package cyder.handlers.internal;

import com.google.common.base.Preconditions;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.frame.CyderFrame;
import cyder.user.UserUtil;
import cyder.utils.OsUtil;
import cyder.utils.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class to handle and log exceptions thrown throughout Cyder.
 */
public final class ExceptionHandler {
    /**
     * Suppress default constructor.
     */
    private ExceptionHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * This method takes an exception, prints it to a string, and then passes the
     * error to the SessionLogger to be logged
     *
     * @param e the exception we are handling and possibly informing the user of
     */
    public static void handle(Exception e) {
        try {
            Optional<String> write = getPrintableException(e);

            if (write.isPresent() && !write.get().trim().isEmpty()) {
                if (Logger.hasLogStarted()) {
                    Logger.log(LogTag.EXCEPTION, write.get());
                } else {
                    Logger.log(LogTag.DEBUG, write.get());
                }
            }

            // if user wants to be informed of exceptions
            boolean silenceErrors = UserUtil.getCyderUser().getSilenceErrors().equals("0");
            if (Console.INSTANCE.getUuid() != null &&
                    !Console.INSTANCE.isClosed() &&
                    silenceErrors) {

                showExceptionPane(e);
            }
        } catch (Exception ex) {
            // uh oh
            Logger.log(LogTag.DEBUG, getPrintableException(e));
        }
    }

    /**
     * The red color used for exception panes.
     */
    private static final Color exceptionRed = new Color(254, 157, 158);

    /**
     * The opacity delta to increment/decrement by.
     */
    private static final float opacityShiftDelta = 0.05f;

    /**
     * The number of lines to display on an exception preview.
     */
    private static final int exceptionLines = 10;

    /**
     * The insets offset for the exception label on the frame.
     */
    private static final int offset = 10;

    /**
     * The timeout between opacity increments/decrements.
     */
    private static final int opacityTimeout = 20;

    /**
     * Shows a popup pane containing a preview of the exception.
     * If the user clicks on the popup, it vanishes immediately and the
     * current log is opened externally.
     *
     * @param e the exception to preview/show
     */
    private static void showExceptionPane(Exception e) {
        Optional<String> informTextOptional = getPrintableException(e);

        // if exception pretty stack present
        if (informTextOptional.isPresent()) {
            AtomicBoolean escapeOpacityThread = new AtomicBoolean();
            escapeOpacityThread.set(false);

            // split at the newlines the method adds
            String[] lines = informTextOptional.get().split("\n");

            // find max width of lines
            int width = 0;
            Font font = CyderFonts.DEFAULT_FONT_SMALL;

            for (int i = 0 ; i < 10 ; i++) {
                width = Math.max(width, StringUtil.getMinWidth(lines[i], font));
            }

            // find height of frame
            int height = StringUtil.getAbsoluteMinHeight(lines[0], font) * (exceptionLines + 1);

            // form label string
            StringBuilder builder = new StringBuilder();
            builder.append("<html>");

            for (int i = 0 ; i < exceptionLines ; i++) {
                builder.append(lines[i]);

                if (i != lines.length - 1) {
                    builder.append("<br/>");
                }
            }

            builder.append("</html>");

            // generate frame
            CyderFrame borderlessFrame = CyderFrame.generateBorderlessFrame(width + 2 * offset,
                    height + 2 * offset, exceptionRed);
            borderlessFrame.setTitle(e.getMessage());
            borderlessFrame.setFrameType(CyderFrame.FrameType.POPUP);

            // generate label for text
            JLabel label = new JLabel(builder.toString());
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    escapeOpacityThread.set(true);
                    borderlessFrame.dispose(true);
                }
            });
            label.setForeground(CyderColors.navy);
            label.setFont(CyderFonts.DEFAULT_FONT_SMALL);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.CENTER);
            label.setBounds(offset, offset, width, height);
            borderlessFrame.getContentPane().add(label);

            borderlessFrame.setLocationOnScreen(CyderFrame.ScreenPosition.BOTTOM_RIGHT);

            // start opacity animation
            borderlessFrame.setOpacity(0.0f);
            borderlessFrame.setVisible(true);

            CyderThreadRunner.submit(() -> {
                try {
                    for (float i = 0.0f ; i <= 1 ; i += opacityShiftDelta) {
                        if (escapeOpacityThread.get()) return;

                        borderlessFrame.setOpacity(i);
                        borderlessFrame.repaint();
                        ThreadUtil.sleep(opacityTimeout);
                    }

                    borderlessFrame.setOpacity(1.0f);
                    borderlessFrame.repaint();

                    ThreadUtil.sleep(5000);

                    if (escapeOpacityThread.get()) return;

                    for (float i = 1.0f ; i >= 0 ; i -= opacityShiftDelta) {
                        if (escapeOpacityThread.get()) return;

                        borderlessFrame.setOpacity(i);
                        borderlessFrame.repaint();
                        ThreadUtil.sleep(opacityTimeout);
                    }

                    if (escapeOpacityThread.get()) return;

                    borderlessFrame.dispose(true);
                } catch (Exception ex) {
                    Logger.log(LogTag.DEBUG, getPrintableException(ex));
                }
            }, "Exception Popup Opacity Animator: " + e.getMessage());
        }
    }

    /**
     * This method handles an exception the same way as {@link ExceptionHandler#handle(Exception)} (String)}
     * except it does so without informing the user/developer/etc.
     *
     * @param e the exception to be silently handled
     */
    public static void silentHandle(Exception e) {
        try {
            Optional<String> optionalWrite = getPrintableException(e);
            if (optionalWrite.isEmpty()) return;
            String write = optionalWrite.get();
            Logger.log(LogTag.EXCEPTION, write);
        } catch (Exception ex) {
            Logger.log(LogTag.DEBUG, getPrintableException(ex));
        }
    }

    /**
     * Generates a printable version of the exception.
     *
     * @param e the exception to return a printable version of
     * @return Optional String possibly containing exception details and trace
     */
    public static Optional<String> getPrintableException(Exception e) {
        // Should be highly unlikely if not impossible
        if (e == null) return Optional.empty();

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

        // line number
        if (lineNumber != 0) {
            exceptionPrintBuilder.append("\nFrom line: ").append(lineNumber);
        } else {
            exceptionPrintBuilder.append("\nThrowing line not found");
        }

        //trace
        exceptionPrintBuilder.append("\nTrace: ").append(stackTrace);

        return Optional.of(exceptionPrintBuilder.toString());
    }

    /**
     * Shows a popup with the provided error message. When the opened popup frame is disposed,
     * Cyder exits.
     *
     * @param message   the message of the popup
     * @param condition the exit condition to log when exiting
     * @param title     the title of the popup
     */
    public static void exceptionExit(String message, ExitCondition condition, String title) {
        Preconditions.checkNotNull(message);
        Preconditions.checkArgument(!message.isEmpty());
        Preconditions.checkNotNull(condition);
        Preconditions.checkNotNull(title);
        Preconditions.checkArgument(!title.isEmpty());

        InformHandler.inform(new InformHandler.Builder(message)
                .setTitle(title)
                .setPostCloseAction(() -> OsUtil.exit(condition)));
    }

    /**
     * Shows a popup with the provided error message. When the opened popup frame is disposed,
     * Cyder exits.
     *
     * @param message   the message of the popup
     * @param condition the exit condition to log when exiting
     */
    public static void exceptionExit(String message, ExitCondition condition) {
        exceptionExit(message, condition, "Exception");
    }

    /**
     * Validates the provided condition, throwing a fatal exception if false.
     *
     * @param condition          the condition to validate
     * @param fatalExceptionText the exception text if the condition is false
     */
    public static void checkFatalCondition(boolean condition, String fatalExceptionText) {
        Preconditions.checkNotNull(fatalExceptionText);
        Preconditions.checkArgument(!fatalExceptionText.isEmpty());

        if (!condition) {
            throw new FatalException(fatalExceptionText);
        }
    }
}