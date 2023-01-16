package cyder.handlers.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.HtmlTags;
import cyder.enums.ExitCondition;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.frame.CyderFrame;
import cyder.ui.frame.FrameType;
import cyder.ui.frame.ScreenPosition;
import cyder.user.UserDataManager;
import cyder.utils.OsUtil;

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
    private static final int shownExceptionLines = 10;

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
     * The name of the thread to animate out exception popups.
     */
    private static final String exceptionPopupDisposeAnimatorThreadName = "Exception popup dispose animator";

    /**
     * The font to use for exception popups.
     */
    private static final Font exceptionPopupFont = CyderFonts.DEFAULT_FONT_SMALL;

    /**
     * The fatal exception default text.
     */
    private static final String FATAL_EXCEPTION = "Fatal exception";

    /**
     * Suppress default constructor.
     */
    private ExceptionHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Handles an exception by converting it to a string representation and passing it to the {@link Logger}.
     *
     * @param exception the exception to handle
     */
    public static void handle(Exception exception) {
        try {
            Optional<String> optionalWrite = getPrintableException(exception);
            if (optionalWrite.isPresent()) {
                String write = optionalWrite.get();
                if (!write.replaceAll(CyderRegexPatterns.whiteSpaceRegex, "").isEmpty()) {
                    Logger.log(LogTag.EXCEPTION, write);

                    boolean consoleOpen = Console.INSTANCE.getUuid() != null && !Console.INSTANCE.isClosed();
                    boolean silenceErrors = UserDataManager.INSTANCE.shouldSilenceErrors();
                    if (consoleOpen && !silenceErrors) {
                        String message = "Exception";
                        if (exception != null) {
                            String exceptionMessage = exception.getMessage();
                            if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
                                message = exceptionMessage;
                            }
                        }
                        showExceptionPane(write, message);
                    }
                }
            }
        } catch (Exception uhOh) {
            if (exception != null) exception.printStackTrace();
            uhOh.printStackTrace();
        }
    }

    /**
     * Shows a popup pane containing a preview of the exception.
     * If the user clicks on the popup, it vanishes immediately and the
     * current log is opened externally.
     *
     * @param printableException the printable exception text
     * @param exceptionMessage   the result of invoking {@link Exception#getMessage()}
     */
    private static void showExceptionPane(String printableException, String exceptionMessage) {
        Preconditions.checkNotNull(printableException);
        Preconditions.checkArgument(!printableException.isEmpty());
        Preconditions.checkNotNull(exceptionMessage);
        Preconditions.checkArgument(!exceptionMessage.isEmpty());

        AtomicBoolean escapeOpacityThread = new AtomicBoolean();
        ImmutableList<String> exceptionLines = ImmutableList.copyOf(printableException.split(CyderStrings.newline));

        int labelWidth = 0;
        for (int i = 0 ; i < shownExceptionLines ; i++) {
            int currentLineWidth = StringUtil.getMinWidth(exceptionLines.get(i), exceptionPopupFont);
            labelWidth = Math.max(labelWidth, currentLineWidth);
        }

        int lineHeight = StringUtil.getAbsoluteMinHeight(exceptionLines.get(0), exceptionPopupFont);
        int labelHeight = lineHeight * shownExceptionLines + 2 * offset;

        StringBuilder builder = new StringBuilder(HtmlTags.openingHtml);
        for (int i = 0 ; i < shownExceptionLines ; i++) {
            builder.append(exceptionLines.get(i));
            if (i != exceptionLines.size() - 1) builder.append(HtmlTags.breakTag);
        }
        builder.append(HtmlTags.closingHtml);

        int frameWidth = labelWidth + 2 * offset;
        int frameHeight = labelHeight + 2 * offset;
        CyderFrame borderlessFrame = CyderFrame.generateBorderlessFrame(frameWidth, frameHeight, exceptionRed);
        borderlessFrame.setTitle(exceptionMessage);
        borderlessFrame.setFrameType(FrameType.POPUP);

        String labelText = builder.toString();
        JLabel label = generatePopupLabel(labelText, escapeOpacityThread, borderlessFrame);
        label.setBounds(offset, offset, labelWidth, labelHeight);
        borderlessFrame.getContentPane().add(label);

        borderlessFrame.setLocationOnScreen(ScreenPosition.BOTTOM_RIGHT);
        borderlessFrame.setOpacity(minimumOpacity);
        borderlessFrame.setVisible(true);

        String threadName = exceptionPopupThreadAnimatorNamePrefix + exceptionMessage;
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
                ex.printStackTrace();
            }
        }, threadName);
    }

    /**
     * Generates the label for exception popups.
     *
     * @param labelText           the label text
     * @param escapeOpacityThread the atomic boolean for escaping the opacity animation.
     * @param frame               the frame the returned JLabel will be added to
     * @return the generated label
     */
    @ForReadability
    private static JLabel generatePopupLabel(String labelText, AtomicBoolean escapeOpacityThread, CyderFrame frame) {
        JLabel label = new JLabel(labelText);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                escapeOpacityThread.set(true);

                CyderThreadRunner.submit(() -> {
                    for (float i = frame.getOpacity() ; i >= minimumOpacity ; i -= opacityShiftDelta) {
                        frame.setOpacity(i);
                        frame.repaint();
                        ThreadUtil.sleep(opacityTimeout / 2);
                    }

                    frame.dispose(true);
                }, exceptionPopupDisposeAnimatorThreadName);
            }
        });
        label.setForeground(CyderColors.navy);
        label.setFont(CyderFonts.DEFAULT_FONT_SMALL);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        return label;
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
        StackTraceElement stackTraceElement = stackTraceElements.get(0);

        int lineNumber = stackTraceElement.getLineNumber();
        String lineNumberRepresentation = lineNumber == 0
                ? "Throwing line not found in stacktrace"
                : "From line: " + lineNumber;

        String declaringClass = stackTraceElement.getClassName();
        String declaringClassRepresentation = "Declaring class: " + declaringClass;

        String filename = stackTraceElement.getFileName();
        String filenameRepresentation = "Filename: " + filename;

        String atRegex = CyderRegexPatterns.whiteSpaceRegex + AT + CyderRegexPatterns.whiteSpaceRegex;
        ImmutableList<String> splitStackAt = ImmutableList.copyOf(stackTrace.split(atRegex));

        StringBuilder retBuilder = new StringBuilder(CyderStrings.newline);
        if (!splitStackAt.isEmpty()) {
            String origin = splitStackAt.get(0);
            retBuilder.append("Exception origin: ").append(origin);
        } else {
            retBuilder.append("Exception origin not found");
        }

        retBuilder.append(CyderStrings.newline).append(filenameRepresentation);
        retBuilder.append(CyderStrings.newline).append(declaringClassRepresentation);
        retBuilder.append(CyderStrings.newline).append(lineNumberRepresentation);
        retBuilder.append(CyderStrings.newline).append("Trace: ").append(stackTrace);
        return Optional.of(retBuilder.toString());
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
     * @param condition the condition to validate
     */
    public static void checkFatalCondition(boolean condition) {
        if (!condition) {
            throw new FatalException(FATAL_EXCEPTION);
        }
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