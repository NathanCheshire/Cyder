package cyder.handlers.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderRegexPatterns;
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
import cyder.utils.BoundsUtil;
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
     * error to the SessionLogger to be logged.
     *
     * @param e the exception we are handling and possibly informing the user of
     */
    public static void handle(Exception e) {
        try {
            Optional<String> optionalWrite = getPrintableException(e);
            if (optionalWrite.isPresent()) {
                String write = optionalWrite.get();
                if (!write.replaceAll(CyderRegexPatterns.whiteSpaceRegex, "").isEmpty()) {
                    LogTag logTag = Logger.hasLogStarted() ? LogTag.EXCEPTION : LogTag.DEBUG;
                    Logger.log(logTag, write);
                }
            }

            boolean consoleOpen = Console.INSTANCE.getUuid() != null && !Console.INSTANCE.isClosed();
            boolean silenceErrors = UserUtil.getCyderUser().getSilenceErrors().equals("1");
            if (consoleOpen && !silenceErrors) {
                showExceptionPane(e);
            }
        } catch (Exception uhOh) {
            Logger.log(LogTag.DEBUG, getPrintableException(e));
            Logger.log(LogTag.DEBUG, getPrintableException(uhOh));
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
     * The prefix for the thread name for exception popup animators.
     */
    private static final String exceptionPopupThreadAnimatorNamePrefix = "Exception Popup Opacity Animator: ";

    /**
     * The at keyword to split a stack trace at to find the first line number.
     */
    private static final String AT = "at";

    /**
     * The minimum opacity for exception popup animations.
     */
    private static final float minimumOpacity = 0.0f;

    /**
     * The maximum opacity for exception popup animations.
     */
    private static final float maximumOpacity = 1.0f;

    /**
     * The time the exception popup should be visible between fade-in and fade-out animations.
     */
    private static final int exceptionPopupVisibilityTime = 3000;

    /**
     * The exception string.
     */
    private static final String EXCEPTION = "Exception";

    /**
     * A newline character.
     */
    private static final String newline = "\n";

    /**
     * Shows a popup pane containing a preview of the exception.
     * If the user clicks on the popup, it vanishes immediately and the
     * current log is opened externally.
     *
     * @param e the exception to preview/show
     */
    private static void showExceptionPane(Exception e) {
        Optional<String> informTextOptional = getPrintableException(e);
        if (informTextOptional.isEmpty()) return;
        String informText = informTextOptional.get();

        AtomicBoolean escapeOpacityThread = new AtomicBoolean();
        String[] exceptionLines = informText.split(newline);

        // find max width of lines
        int width = 0;
        Font font = CyderFonts.DEFAULT_FONT_SMALL;

        for (int i = 0 ; i < 10 ; i++) {
            width = Math.max(width, StringUtil.getMinWidth(exceptionLines[i], font));
        }

        // find height of frame
        int height = StringUtil.getAbsoluteMinHeight(exceptionLines[0], font) * (ExceptionHandler.exceptionLines + 1);

        // form label string
        StringBuilder builder = new StringBuilder();
        builder.append(BoundsUtil.OPENING_HTML_TAG);

        for (int i = 0 ; i < ExceptionHandler.exceptionLines ; i++) {
            builder.append(exceptionLines[i]);

            if (i != exceptionLines.length - 1) {
                builder.append(BoundsUtil.BREAK_TAG);
            }
        }

        builder.append(BoundsUtil.CLOSING_HTML_TAG);

        CyderFrame borderlessFrame = CyderFrame.generateBorderlessFrame(
                width + 2 * offset, height + 2 * offset, exceptionRed);
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
        borderlessFrame.setOpacity(minimumOpacity);
        borderlessFrame.setVisible(true);

        String threadName = exceptionPopupThreadAnimatorNamePrefix + e.getMessage();
        CyderThreadRunner.submit(() -> {
            try {
                for (float i = minimumOpacity ; i <= maximumOpacity ; i += opacityShiftDelta) {
                    if (escapeOpacityThread.get()) return;
                    borderlessFrame.setOpacity(i);
                    borderlessFrame.repaint();
                    ThreadUtil.sleep(opacityTimeout);
                }

                borderlessFrame.setOpacity(maximumOpacity);
                borderlessFrame.repaint();

                ThreadUtil.sleep(exceptionPopupVisibilityTime);
                if (escapeOpacityThread.get()) return;

                for (float i = maximumOpacity ; i >= minimumOpacity ; i -= opacityShiftDelta) {
                    if (escapeOpacityThread.get()) return;
                    borderlessFrame.setOpacity(i);
                    borderlessFrame.repaint();
                    ThreadUtil.sleep(opacityTimeout);
                }

                borderlessFrame.setOpacity(minimumOpacity);
                borderlessFrame.repaint();

                if (escapeOpacityThread.get()) return;
                borderlessFrame.dispose(true);
            } catch (Exception ex) {
                Logger.log(LogTag.DEBUG, getPrintableException(ex));
            }
        }, threadName);

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
        if (e == null) {
            return Optional.empty();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();

        ImmutableList<StackTraceElement> stackTraceElements = ImmutableList.copyOf(e.getStackTrace());
        if (stackTraceElements.isEmpty()) return Optional.empty();
        int lineNumber = stackTraceElements.get(0).getLineNumber();

        String atRegex = CyderRegexPatterns.whiteSpaceRegex + AT + CyderRegexPatterns.whiteSpaceRegex;
        ImmutableList<String> splitStackAt = ImmutableList.copyOf(stackTrace.split(atRegex));

        StringBuilder exceptionPrintBuilder = new StringBuilder();
        if (!splitStackAt.isEmpty()) {
            exceptionPrintBuilder.append(newline + "Exception origin: ").append(splitStackAt.get(0));
        } else {
            exceptionPrintBuilder.append(newline + "Exception origin not found");
        }

        // line number
        if (lineNumber != 0) {
            exceptionPrintBuilder.append(newline + "From line: ").append(lineNumber);
        } else {
            exceptionPrintBuilder.append(newline + "Throwing line not found in stacktrace");
        }

        exceptionPrintBuilder.append(newline + "Trace: ").append(stackTrace);

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
        exceptionExit(message, condition, EXCEPTION);
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