package cyder.getter;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.annotations.ForReadability;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.ui.button.CyderButton;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.label.CyderLabel;
import cyder.ui.pane.CyderScrollList;
import cyder.user.UserUtil;
import cyder.utils.BoundsUtil;
import cyder.utils.OsUtil;
import cyder.utils.StringUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A getter utility for getting strings, confirmations, files, etc. from the user.
 */
public class GetterUtil {
    /**
     * To obtain an instance, use {@link GetterUtil#getInstance()}.
     */
    private GetterUtil() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns a GetterUtil instance.
     *
     * @return a GetterUtil instance
     */
    public static GetterUtil getInstance() {
        return new GetterUtil();
    }

    // --------------------
    // Frame tracking logic
    // --------------------

    /**
     * All the currently active get input frames associated with this instance.
     */
    private final ArrayList<CyderFrame> getInputFrames = new ArrayList<>();

    /**
     * Closes all get input frames associated with this instance.
     */
    public void closeAllGetInputFrames() {
        getInputFrames.forEach(frame -> frame.dispose(true));
    }

    /**
     * All the currently active get file frames associated with this instance.
     */
    private final ArrayList<CyderFrame> getFileFrames = new ArrayList<>();

    /**
     * Closes all get file frames associated with this instance.
     */
    public void closeAllGetFileFrames() {
        getFileFrames.forEach(frame -> frame.dispose(true));
    }

    /**
     * All the currently active get confirmation confirmation associated with this instance.
     */
    private final ArrayList<CyderFrame> getConfirmationFrames = new ArrayList<>();

    /**
     * Closes all get confirmation frames associated with this instance.
     */
    public void closeAllGetConfirmationFrames() {
        getConfirmationFrames.forEach(frame -> frame.dispose(true));
    }

    /**
     * Closes all getter frames associated with this instance.
     */
    public void closeAllGetFrames() {
        closeAllGetInputFrames();
        closeAllGetFileFrames();
        closeAllGetConfirmationFrames();
    }

    // ------------------------
    // End frame tracking logic
    // ------------------------

    /**
     * The minimum width for a get input popup.
     */
    private static final int GET_INPUT_MIN_WIDTH = 400;

    /**
     * The top and bottom padding for a string popup.
     */
    private static final int GET_INPUT_Y_PADDING = 10;

    /**
     * The left and right padding for a string popup.
     */
    private static final int GET_INPUT_X_PADDING = 40;

    /**
     * The line border for the get input's submit button.
     */
    private static final LineBorder GET_INPUT_SUBMIT_BUTTON_BORDER
            = new LineBorder(CyderColors.navy, 5, false);

    /**
     * Opens up frame with a field and a label for the user to enter input to be returned.
     * <p>
     * See usage below for how to setup usage of this method so that the calling thread is not blocked.
     * <p>
     * Usage:
     * <pre>
     *  {@code
     *  CyderThreadRunner.submit(() -> {
     *      try {
     *          String input = GetterUtil().getInstance().getInput(getInputBuilder);
     *          // Other operations using input
     *      } catch (Exception e) {
     *          ErrorHandler.handle(e);
     *      }
     *  }, "THREAD_NAME").start();
     *  }
     *  </pre>
     *
     * @param getInputBuilder the GetInputBuilder to use
     * @return the user entered input if present. Empty optional in the case of nothing/pure whitespace
     */
    public Optional<String> getInput(GetInputBuilder getInputBuilder) {
        checkNotNull(getInputBuilder);
        checkNotNull(getInputBuilder.getLabelText());

        AtomicReference<String> returnString = new AtomicReference<>();

        String threadName = "GetInput waiter thread, title: "
                + CyderStrings.quote + getInputBuilder.getFrameTitle() + CyderStrings.quote;
        CyderThreadRunner.submit(() -> {
            BoundsUtil.BoundsString boundsString = BoundsUtil.widthHeightCalculation(
                    getInputBuilder.getLabelText(),
                    getInputBuilder.getLabelFont(),
                    GET_INPUT_MIN_WIDTH);

            int width = boundsString.width() + 2 * GET_INPUT_X_PADDING;
            int height = boundsString.height() + 2 * GET_INPUT_Y_PADDING;
            String parsedLabelText = boundsString.text();

            int componentWidth = width - 2 * GET_INPUT_X_PADDING;
            int componentHeight = 40;

            int frameHeight = CyderDragLabel.DEFAULT_HEIGHT + height
                    + 2 * componentHeight + 3 * GET_INPUT_Y_PADDING;
            CyderFrame inputFrame = new CyderFrame(width, frameHeight, CyderIcons.defaultBackground);
            getInputFrames.add(inputFrame);
            inputFrame.addPreCloseAction(() -> getInputFrames.remove(inputFrame));
            inputFrame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);
            inputFrame.setTitle(getInputBuilder.getFrameTitle());
            getInputBuilder.getOnDialogDisposalRunnables().forEach(inputFrame::addPostCloseAction);

            int yOff = CyderDragLabel.DEFAULT_HEIGHT + GET_INPUT_Y_PADDING;
            CyderLabel textLabel = new CyderLabel(parsedLabelText);
            textLabel.setForeground(getInputBuilder.getLabelColor());
            textLabel.setFont(getInputBuilder.getLabelFont());
            textLabel.setBounds(GET_INPUT_X_PADDING, yOff, boundsString.width(), boundsString.height());
            inputFrame.getContentPane().add(textLabel);

            yOff += GET_INPUT_Y_PADDING + boundsString.height();

            CyderTextField inputField = new CyderTextField();
            inputField.setHorizontalAlignment(JTextField.CENTER);
            inputField.setBackground(Color.white);

            String fieldText = getInputBuilder.getInitialFieldText();
            if (!StringUtil.isNullOrEmpty(fieldText)) inputField.setText(fieldText);

            String fieldHintText = getInputBuilder.getFieldHintText();
            if (!StringUtil.isNullOrEmpty(fieldHintText)) inputField.setHintText(fieldHintText);

            String fieldRegex = getInputBuilder.getFieldRegex();
            if (!StringUtil.isNullOrEmpty(fieldRegex)) inputField.setKeyEventRegexMatcher(fieldRegex);

            inputField.setForeground(getInputBuilder.getFieldForeground());
            inputField.setFont(getInputBuilder.getFieldFont());
            inputField.setBounds(GET_INPUT_X_PADDING, yOff, componentWidth, componentHeight);
            inputFrame.getContentPane().add(inputField);

            yOff += GET_INPUT_Y_PADDING + componentHeight;

            Runnable preCloseAction = () -> {
                String input = inputField.getTrimmedText();
                returnString.set(input.isEmpty() ? CyderStrings.NULL : input);
            };
            inputFrame.addPreCloseAction(preCloseAction);

            Runnable submitAction = () -> {
                preCloseAction.run();
                inputFrame.dispose();
            };

            CyderButton submitButton = new CyderButton(getInputBuilder.getSubmitButtonText());
            submitButton.setBackground(getInputBuilder.getSubmitButtonColor());
            inputField.addActionListener(e -> submitAction.run());
            submitButton.setBorder(GET_INPUT_SUBMIT_BUTTON_BORDER);
            submitButton.setFont(getInputBuilder.getSubmitButtonFont());
            submitButton.setForeground(CyderColors.navy);
            submitButton.addActionListener(e -> submitAction.run());
            submitButton.setBounds(GET_INPUT_X_PADDING, yOff, componentWidth, componentHeight);
            inputFrame.getContentPane().add(submitButton);

            Component relativeTo = getInputBuilder.getRelativeTo();
            if (relativeTo != null && getInputBuilder.isDisableRelativeTo()) {
                relativeTo.setEnabled(false);
                inputFrame.addPostCloseAction(generateGetterFramePostCloseAction(relativeTo));
            }
            inputFrame.setLocationRelativeTo(relativeTo);
            inputFrame.setVisible(true);
        }, threadName);

        while (returnString.get() == null) {
            Thread.onSpinWait();
        }

        String ret = returnString.get();
        return StringUtil.isNullOrEmpty(ret)
                ? Optional.empty()
                : Optional.of(ret);
    }

    /**
     * The label which holds the scroll
     */
    private JLabel dirScrollLabel;

    /**
     * The last button for the file traversal stack.
     */
    private CyderButton last;

    /**
     * The next button for the file traversal stack./
     */
    private CyderButton next;

    /**
     * The list of strings to display for the current files.
     */
    private final LinkedList<String> directoryNameList = new LinkedList<>();

    /**
     * The files which correspond to the current files.
     */
    private final LinkedList<File> directoryFileList = new LinkedList<>();

    /**
     * The backward stack.
     */
    private final Stack<File> backward = new Stack<>();

    /**
     * The forward stack.
     */
    private final Stack<File> forward = new Stack<>();

    /**
     * The current location for the file getter.
     */
    private File currentDirectory;

    /**
     * The frame for the file getter.
     */
    private final AtomicReference<CyderFrame> directoryFrameReference = new AtomicReference<>();

    /**
     * The scroll view for the files.
     */
    private final AtomicReference<CyderScrollList> cyderScrollListRef = new AtomicReference<>();

    /**
     * The field for the user to enter a directory/file directly.
     */
    private final AtomicReference<CyderTextField> dirFieldRef = new AtomicReference<>();

    /**
     * The file to return once chosen.
     */
    private final AtomicReference<File> setOnFileChosen = new AtomicReference<>();

    /**
     * The thread name for the file getter threads which load the initial directory's files.
     */
    private static final String FILE_GETTER_LOADER = "File Getter Loader";

    /**
     * The initial title for file getter frames.
     */
    private static final String INITIAL_DIRECTORY_FRAME_TITLE = "File getter";

    // todo do away
    /**
     * The null file.
     */
    private static final File NULL_FILE = new File(CyderStrings.NULL);

    /**
     * The text for the last button.
     */
    private static final String LAST_BUTTON_TEXT = " < ";

    /**
     * The text for the next button.
     */
    private static final String NEXT_BUTTON_TEXT = " > ";

    /**
     * The border for the next and last buttons.
     */
    private static final LineBorder BUTTON_BORDER = new LineBorder(CyderColors.navy, 5, false);

    // todo optional

    /**
     * Custom getFile method, see usage below for how to setup so that the program doesn't
     * spin wait on the main GUI thread forever. Ignoring the below setup
     * instructions will make the application spin wait possibly forever.
     * <p>
     * USAGE:
     * <pre>
     * {@code
     *   CyderThreadRunner.submit(() -> {
     *         try {
     *             File input = GetterUtil().getInstance().getFile(getterBuilder);
     *             // Other operations using input
     *         } catch (Exception e) {
     *             ErrorHandler.handle(e);
     *         }
     *  }, THREAD_NAME).start();
     * }
     * </pre>
     *
     * @param builder the builder to use for the required params
     * @return the user-chosen file
     */
    public File getFile(Builder builder) {
        checkNotNull(builder);

        boolean darkMode = UserUtil.getCyderUser().getDarkmode().equals("1");
        Color backgroundColor = darkMode ? CyderColors.darkModeBackgroundColor : CyderColors.regularBackgroundColor;
        Color textColor = darkMode ? CyderColors.defaultDarkModeTextColor : CyderColors.navy;

        int frameWidth = 630;
        int frameHeight = 510;
        CyderFrame referenceInitFrame = new CyderFrame(frameWidth, frameHeight, backgroundColor);

        getFileFrames.add(referenceInitFrame);
        referenceInitFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                File ref = setOnFileChosen.get();
                if (ref == null || StringUtil.isNullOrEmpty(ref.getName())) {
                    setOnFileChosen.set(NULL_FILE);
                }
            }
        });
        directoryFrameReference.set(referenceInitFrame);

        String threadName = "GetFile Waiter Thread, title = \"" + builder.getTitle() + CyderStrings.quote;
        CyderThreadRunner.submit(() -> {
            try {
                //reset needed vars in case an instance was already ran
                backward.clear();
                forward.clear();
                directoryFileList.clear();
                directoryNameList.clear();

                CyderFrame directoryFrame = directoryFrameReference.get();
                directoryFrame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);
                directoryFrame.addPreCloseAction(() -> getFileFrames.remove(directoryFrame));
                // todo post close actions from builder

                directoryFrame.setTitle(INITIAL_DIRECTORY_FRAME_TITLE);

                CyderTextField dirField = new CyderTextField();
                dirFieldRef.set(dirField);

                LineBorder dirFieldLineBorder = new LineBorder(textColor, 5, false);
                dirField.setBackground(backgroundColor);
                dirField.setForeground(textColor);
                dirField.setBorder(dirFieldLineBorder);
                dirField.addActionListener(e -> {
                    File ChosenDir = new File(dirField.getText());

                    if (ChosenDir.isDirectory()) {
                        refreshBasedOnDir(ChosenDir, true);
                    } else if (ChosenDir.isFile()) {
                        setOnFileChosen.set(ChosenDir);
                    }
                });

                int dirFieldXOffset = 60;
                int dirFieldYOffset = 40;
                int dirFieldWidth = 500;
                int dirFieldHeight = 40;
                dirField.setBounds(dirFieldXOffset, dirFieldYOffset, dirFieldWidth, dirFieldHeight);
                directoryFrame.getContentPane().add(dirField);
                dirField.setEnabled(false);

                int buttonSize = 40;

                last = new CyderButton(LAST_BUTTON_TEXT);
                last.setBorder(BUTTON_BORDER);
                last.addActionListener(e -> {
                    if (!backward.isEmpty() && !backward.peek().equals(currentDirectory)) {
                        forward.push(currentDirectory);
                        currentDirectory = backward.pop();
                        refreshBasedOnDir(currentDirectory, false);
                    }
                });
                int lastXOffset = 10;
                int buttonYOffset = 40;
                last.setBounds(lastXOffset, buttonYOffset, buttonSize, buttonSize);
                directoryFrame.getContentPane().add(last);
                last.setEnabled(false);

                next = new CyderButton(NEXT_BUTTON_TEXT);
                next.setBorder(BUTTON_BORDER);
                next.addActionListener(e -> {
                    if (!forward.isEmpty() && !forward.peek().equals(currentDirectory)) {
                        backward.push(currentDirectory);
                        currentDirectory = forward.pop();
                        refreshBasedOnDir(currentDirectory, false);
                    }
                });
                int buttonPadding = 10;
                next.setBounds(frameWidth - 2 * buttonPadding - buttonSize, buttonYOffset, buttonSize, buttonSize);
                directoryFrame.getContentPane().add(next);
                next.setEnabled(false);

                int mainComponentWidth = 600;
                int mainComponentHeight = 400;

                // label to show where files will be
                JLabel tempLabel = new JLabel();
                tempLabel.setText(BoundsUtil.addCenteringToHtml(CyderStrings.LOADING));
                tempLabel.setHorizontalAlignment(JLabel.CENTER);
                tempLabel.setVerticalAlignment(JLabel.CENTER);
                tempLabel.setFont(CyderFonts.DEFAULT_FONT);
                tempLabel.setForeground(darkMode ? CyderColors.defaultDarkModeTextColor : CyderColors.navy);
                tempLabel.setBorder(new LineBorder(darkMode ? CyderColors.defaultDarkModeTextColor
                        : CyderColors.navy, 5, false));
                tempLabel.setOpaque(false);
                tempLabel.setBounds(10, 90, mainComponentWidth, mainComponentHeight);
                directoryFrame.getContentPane().add(tempLabel);

                Component relativeTo = builder.getRelativeTo();
                if (relativeTo != null && builder.isDisableRelativeTo()) {
                    relativeTo.setEnabled(false);
                    directoryFrame.addPostCloseAction(generateGetterFramePostCloseAction(relativeTo));
                }

                directoryFrame.setLocationRelativeTo(relativeTo);
                directoryFrame.setVisible(true);

                loadInitialGetFileFiles(builder, directoryFrame, mainComponentWidth,
                        mainComponentHeight, darkMode, tempLabel);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, threadName);

        try {
            while (setOnFileChosen.get() == null) {
                Thread.onSpinWait();
            }
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
        } finally {
            directoryFrameReference.get().dispose();
        }

        return setOnFileChosen.get().getName().equals(CyderStrings.NULL)
                ? null
                : setOnFileChosen.get();
    }

    @ForReadability
    private void loadInitialGetFileFiles(Builder builder, CyderFrame directoryFrame,
                                         int mainComponentWidth, int mainComponentHeight,
                                         boolean darkMode, JLabel tempLabel) {
        CyderThreadRunner.submit(() -> {
            String path = !StringUtil.isNullOrEmpty(builder.getInitialString())
                    && new File(builder.getInitialString()).exists()
                    ? builder.getInitialString() : OsUtil.USER_DIR;
            currentDirectory = new File(path);

            directoryFrame.setTitle(currentDirectory.getName());

            File[] currentDirectoryFiles = currentDirectory.listFiles();
            if (currentDirectoryFiles != null && currentDirectoryFiles.length > 0) {
                Collections.addAll(directoryFileList, currentDirectoryFiles);
            }

            directoryFileList.forEach(file -> directoryNameList.add(file.getName()));

            cyderScrollListRef.set(new CyderScrollList(mainComponentWidth, mainComponentHeight,
                    CyderScrollList.SelectionPolicy.SINGLE, darkMode));
            cyderScrollListRef.get().setScrollFont(CyderFonts.SEGOE_20.deriveFont(16f));

            // Setup click actions
            IntStream.range(0, directoryNameList.size()).forEach(index -> {
                String labelText = directoryNameList.get(index);
                Runnable clickRunnable = () -> {
                    boolean isDirectory = directoryFileList.get(index).isDirectory();
                    File directoryOrFile = directoryFileList.get(index);
                    if (isDirectory) {
                        refreshBasedOnDir(directoryOrFile, false);
                    } else {
                        setOnFileChosen.set(directoryOrFile);
                    }
                };

                cyderScrollListRef.get().addElement(labelText, clickRunnable);
            });

            int dirScrollXOffset = 10;
            int dirScrollYOffset = 90;
            dirScrollLabel = cyderScrollListRef.get().generateScrollList();
            dirScrollLabel.setBounds(dirScrollXOffset, dirScrollYOffset,
                    mainComponentWidth, mainComponentHeight);
            directoryFrame.getContentPane().add(dirScrollLabel);

            next.setEnabled(true);
            last.setEnabled(true);

            dirFieldRef.get().setText(currentDirectory.getAbsolutePath());
            dirFieldRef.get().setEnabled(true);
            dirFieldRef.get().requestFocus();

            tempLabel.setVisible(false);
            directoryFrame.getContentPane().remove(tempLabel);

            backward.push(currentDirectory);
        }, FILE_GETTER_LOADER);
    }

    /**
     * Refreshes the current file list based on the provided file.
     *
     * @param refreshDirectory the directory/file to refresh on
     * @param wipeForward      whether to clear the forward traversal stack
     */
    @ForReadability
    private void refreshBasedOnDir(File refreshDirectory, boolean wipeForward) {
        checkNotNull(refreshDirectory);

        if (wipeForward) {
            forward.clear();
            if (backward.isEmpty() || !backward.peek().equals(currentDirectory)) {
                backward.push(currentDirectory);
            }
        }

        CyderFrame directoryFrame = directoryFrameReference.get();
        CyderScrollList scrollList = cyderScrollListRef.get();

        scrollList.removeAllElements();
        directoryFrame.remove(dirScrollLabel);

        if (refreshDirectory.isFile()) refreshDirectory = refreshDirectory.getParentFile();
        currentDirectory = refreshDirectory;

        directoryNameList.clear();
        directoryFileList.clear();

        File[] currentDirectoryFiles = currentDirectory.listFiles();
        if (currentDirectoryFiles != null && currentDirectoryFiles.length > 0) {
            Collections.addAll(directoryFileList, currentDirectoryFiles);
        }

        directoryFileList.forEach(directoryFile -> directoryNameList.add(directoryFile.getName()));

        int scrollWidth = 600;
        int scrollHeight = 400;
        cyderScrollListRef.set(new CyderScrollList(scrollWidth, scrollHeight,
                CyderScrollList.SelectionPolicy.SINGLE, scrollList.isDarkMode()));
        scrollList.setScrollFont(CyderFonts.SEGOE_20.deriveFont(16f));

        IntStream.range(0, directoryNameList.size()).forEach(index -> {
            String labelName = directoryNameList.get(index);
            Runnable labelClickRunnable = () -> {
                if (directoryFileList.get(index).isDirectory()) {
                    refreshBasedOnDir(directoryFileList.get(index), true);
                } else {
                    setOnFileChosen.set(directoryFileList.get(index));
                }
            };
            scrollList.addElement(labelName, labelClickRunnable);
        });
        dirScrollLabel = scrollList.generateScrollList();

        int xOffset = 10;
        int yOffset = 90;
        dirScrollLabel.setBounds(xOffset, yOffset, scrollWidth, scrollHeight);

        directoryFrame.getContentPane().add(dirScrollLabel);
        directoryFrame.revalidate();
        directoryFrame.repaint();
        directoryFrame.setTitle(currentDirectory.getName());

        dirFieldRef.get().setText(currentDirectory.getAbsolutePath());
    }

    /**
     * Opens up a frame with a label and a yes/no button for the user to confirm or deny some action.
     * <p>
     * See usage below for how to setup usage of this method so that the calling thread is not blocked.
     * <p>
     * Usage:
     * <pre>
     *  {@code
     *  CyderThreadRunner.submit(() -> {
     *      try {
     *          String input = GetterUtil().getInstance().getConfirmation(getConfirmationBuilder);
     *          // Other operations using input
     *      } catch (Exception e) {
     *          ErrorHandler.handle(e);
     *      }
     *  }, "THREAD_NAME").start();
     *  }
     *  </pre>
     *
     * @param getConfirmationBuilder the getConfirmationBuilder to use
     * @return whether the user approved the requested action
     */
    public boolean getConfirmation(GetConfirmationBuilder getConfirmationBuilder) {
        checkNotNull(getConfirmationBuilder);

        AtomicReference<Boolean> ret = new AtomicReference<>();
        ret.set(null);
        AtomicReference<CyderFrame> frameReference = new AtomicReference<>();

        String threadName = "GetConfirmation Waiter Thread, title = \""
                + getConfirmationBuilder.getFrameTitle() + CyderStrings.quote;
        CyderThreadRunner.submit(() -> {
            try {
                CyderLabel textLabel = new CyderLabel();
                textLabel.setForeground(getConfirmationBuilder.getLabelColor());
                textLabel.setFont(getConfirmationBuilder.getLabelFont());

                BoundsUtil.BoundsString boundsString = BoundsUtil.widthHeightCalculation(
                        getConfirmationBuilder.getLabelText(), textLabel.getFont());
                int textWidth = boundsString.width();
                int textHeight = boundsString.height();
                textLabel.setText(boundsString.text());

                int frameHorizontalPadding = 20;
                int yesNoButtonHeight = 40;
                int frameTopPadding = 40;
                int textBottomPadding = 20;
                int buttonBottomPadding = 25;

                int frameWidth = 2 * frameHorizontalPadding + textWidth;
                int frameHeight = frameTopPadding + textHeight + textBottomPadding
                        + yesNoButtonHeight + buttonBottomPadding;
                CyderFrame frame = new CyderFrame(frameWidth, frameHeight, CyderIcons.defaultBackgroundLarge);
                getConfirmationFrames.add(frame);
                frameReference.set(frame);
                getConfirmationBuilder.getOnDialogDisposalRunnables().forEach(frame::addPostCloseAction);

                frame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);
                frame.setTitle(getConfirmationBuilder.getFrameTitle());
                frame.addPreCloseAction(() -> {
                    if (ret.get() != Boolean.TRUE) ret.set(Boolean.FALSE);

                    getConfirmationFrames.remove(frame);
                });

                int currentY = frameTopPadding;

                int textLabelPadding = 10;
                textLabel.setBounds(textLabelPadding, currentY, textWidth, textHeight);
                frame.getContentPane().add(textLabel);
                currentY += textHeight + textBottomPadding;

                int numButtons = 2;
                int buttonInnerSpacing = 30;
                int buttonWidth = (frameWidth - buttonInnerSpacing - 2 * frameHorizontalPadding) / numButtons;

                CyderButton yesButton = new CyderButton(getConfirmationBuilder.getYesButtonText());
                yesButton.setColors(getConfirmationBuilder.getYesButtonColor());
                yesButton.setFont(getConfirmationBuilder.getYesButtonFont());
                yesButton.addActionListener(e -> ret.set(Boolean.TRUE));
                yesButton.setBounds(frameHorizontalPadding, currentY, buttonWidth, yesNoButtonHeight);
                frame.getContentPane().add(yesButton);

                int noButtonX = frameHorizontalPadding + buttonWidth + buttonInnerSpacing;
                CyderButton noButton = new CyderButton(getConfirmationBuilder.getNoButtonText());
                noButton.setColors(getConfirmationBuilder.getNoButtonColor());
                yesButton.setFont(getConfirmationBuilder.getNoButtonFont());
                noButton.addActionListener(e -> ret.set(Boolean.FALSE));
                noButton.setBounds(noButtonX, currentY, buttonWidth, yesNoButtonHeight);
                frame.getContentPane().add(noButton);

                Component relativeTo = getConfirmationBuilder.getRelativeTo();
                if (relativeTo != null && getConfirmationBuilder.isDisableRelativeTo()) {
                    relativeTo.setEnabled(false);
                    frame.addPostCloseAction(generateGetterFramePostCloseAction(relativeTo));
                }

                frame.setLocationRelativeTo(relativeTo);
                frame.setVisible(true);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, threadName);

        try {
            while (ret.get() == null) {
                Thread.onSpinWait();
            }

            frameReference.get().removePreCloseActions();
            frameReference.get().dispose();
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        return ret.get();
    }

    /**
     * Generates and returns a runnable to run as a post-close frame action for all getter frames.
     *
     * @param relativeTo the relative to component
     * @return the runnable
     */
    private Runnable generateGetterFramePostCloseAction(Component relativeTo) {
        return () -> {
            boolean onTop = false;
            boolean isFrame = relativeTo instanceof Frame;
            Frame frame = isFrame ? (Frame) relativeTo : null;
            if (isFrame) onTop = frame.isAlwaysOnTop();

            relativeTo.setEnabled(true);

            if (isFrame) frame.setAlwaysOnTop(true);
            if (isFrame) frame.setAlwaysOnTop(onTop);
        };
    }

    /**
     * A builder for a getter frame.
     */
    public static class Builder {
        /**
         * The title of the frame.
         */
        private final String title;

        /**
         * Te component to set the getter frame relative to.
         */
        private Component relativeTo;

        /**
         * Constructs a new GetterBuilder.
         *
         * @param title the frame title/the text for confirmations
         */
        public Builder(String title) {
            checkNotNull(title);

            this.title = title;

            Logger.log(LogTag.OBJECT_CREATION, this);
        }

        /**
         * Returns the title of the getter frame.
         *
         * @return the title of the getter frame
         */
        public String getTitle() {
            return title;
        }

        /**
         * Returns the relative to component to set the getter frame relative to.
         *
         * @return the relative to component to set the getter frame relative to
         */
        public Component getRelativeTo() {
            return relativeTo;
        }

        /**
         * Sets the relative to component to set the getter frame relative to.
         *
         * @param relativeTo the relative to component to set the getter frame relative to
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setRelativeTo(Component relativeTo) {
            this.relativeTo = relativeTo;
            return this;
        }

        /**
         * Returns the initial field text for getter frames which have an input field.
         *
         * @return the initial field text for getter frames which have an input field
         */
        public String getInitialString() {
            return "";
        }

        /**
         * Returns whether to disable the relativeTo component while the getter frame is active.
         *
         * @return whether to disable the relativeTo component while the getter frame is active
         */
        public boolean isDisableRelativeTo() {
            return false;
        }
    }
}
