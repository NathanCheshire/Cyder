package cyder.getter;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.SystemPropertyKey;
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
import cyder.utils.BoundsUtil;
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
     * The label which holds the scroll.
     */
    private JLabel directoryScrollLabel;

    /**
     * The last button for the file traversal stack.
     */
    private CyderButton lastDirectory;

    /**
     * The next button for the file traversal stack.
     */
    private CyderButton nextDirectory;

    /**
     * The list of strings to display for the current files.
     */
    private final LinkedList<String> filesNamesList = new LinkedList<>();

    /**
     * The files which correspond to the current files.
     */
    private final LinkedList<File> filesList = new LinkedList<>();

    /**
     * The backward stack.
     */
    private final Stack<File> backwardDirectories = new Stack<>();

    /**
     * The forward stack.
     */
    private final Stack<File> forwardDirectories = new Stack<>();

    /**
     * The current location for the file getter.
     */
    private File currentDirectory;

    /**
     * The directory frame for the file getter.
     */
    private CyderFrame directoryFrame;

    /**
     * The scroll view for the files.
     */
    private CyderScrollList directoryScrollList;

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

    /**
     * The null file.
     */
    private static final File NULL_FILE = new File(CyderStrings.NULL);

    /**
     * The text for the last button.
     */
    private static final String LAST_BUTTON_TEXT = CyderStrings.space + "<" + CyderStrings.space;

    /**
     * The text for the next button.
     */
    private static final String NEXT_BUTTON_TEXT = CyderStrings.space + ">" + CyderStrings.space;

    /**
     * The border for the next and last buttons.
     */
    private static final LineBorder BUTTON_BORDER = new LineBorder(CyderColors.navy, 5, false);

    /**
     * The width of the directory scroll component.
     */
    private static final int directoryScrollWidth = 600;

    /**
     * The height of the directory scroll component.
     */
    private static final int directoryScrollHeight = 400;

    /**
     * The padding value used for components near the get file frame.
     */
    private static final int padding = 10;

    /**
     * The size of the navigation buttons.
     */
    private static final int navButtonSize = 40;

    /**
     * The y value of the directory scroll component.
     */
    private static final int dirScrollYOffset = CyderDragLabel.DEFAULT_HEIGHT + navButtonSize + 2 * padding;

    /**
     * The label to let the user know the files are being loaded from the current directory for the file getter.
     */
    private final JLabel loadingFilesLabel = new JLabel();

    /**
     * The directory field.
     */
    private static CyderTextField directoryField;

    /**
     * The font for the items of the files scroll list.
     */
    private static final Font directoryScrollFont = new Font(CyderFonts.SEGOE_UI_BLACK, Font.BOLD, 16);

    /**
     * The x value of the directory field.
     */
    private static final int directoryFieldX = padding + navButtonSize + padding;

    /**
     * The directory field width.
     */
    private static final int directoryFieldWidth = directoryScrollWidth - 2 * navButtonSize - 2 * padding;

    /**
     * The y value of the components at the top of the frame.
     */
    private static final int topComponentY = CyderDragLabel.DEFAULT_HEIGHT + padding;

    /**
     * The padding from all other components/padding and the frame border.
     */
    private static final int frameXPadding = 15;

    /**
     * The width of the file chooser frame.
     */
    private static final int frameWidth = directoryScrollWidth + 2 * frameXPadding;

    /**
     * The padding on the bottom of the file chooser frame.
     */
    private static final int bottomFramePadding = 100;

    /**
     * The height of the file chooser frame.
     */
    private static final int frameHeight = directoryScrollHeight + navButtonSize + 2 * padding + bottomFramePadding;

    /**
     * The y value of the submit button.
     */
    private static final int submitButtonY = frameHeight - navButtonSize - 2 * padding;

    /**
     * The submit button.
     */
    private CyderButton submitButton;

    /**
     * The submit text for the submit button.
     */
    private static final String SUBMIT = "Submit";

    /**
     * Opens up frame with a field and a file chooser for the user to enter
     * a file location or navigate to a file/directory and submit it.
     * <p>
     * See usage below for how to setup usage of this method so that the calling thread is not blocked.
     * <p>
     * Usage:
     * <pre>
     *  {@code
     *  CyderThreadRunner.submit(() -> {
     *      try {
     *          String input = GetterUtil().getInstance().getFile(getInputBuilder);
     *          // Other operations using input
     *      } catch (Exception e) {
     *          ErrorHandler.handle(e);
     *      }
     *  }, "THREAD_NAME").start();
     *  }
     *  </pre>
     *
     * @param getFileBuilder the GetFileBuilder to use
     * @return the user chosen file or directory. Empty optional if a file was not chosen
     */
    // todo return optional
    public File getFile(Builder getFileBuilder) {
        checkNotNull(getFileBuilder);

        directoryFrame = new CyderFrame(frameWidth, frameHeight);

        getFileFrames.add(directoryFrame);
        directoryFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                File ref = setOnFileChosen.get();
                if (ref == null || StringUtil.isNullOrEmpty(ref.getName())) {
                    setOnFileChosen.set(NULL_FILE);
                }
            }
        });

        String threadName = "getFile waiter thread, title: "
                + CyderStrings.quote + getFileBuilder.getTitle() + CyderStrings.quote;
        CyderThreadRunner.submit(() -> {
            try {
                resetFileHistory();

                directoryFrame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);
                directoryFrame.addPreCloseAction(() -> getFileFrames.remove(directoryFrame));
                // todo post close actions from builder
                directoryFrame.setTitle(INITIAL_DIRECTORY_FRAME_TITLE);

                directoryField = new CyderTextField();
                directoryField.setBackground(CyderColors.vanilla);
                directoryField.setForeground(CyderColors.navy);
                directoryField.setBorder(new LineBorder(CyderColors.navy, 5, false));
                directoryField.addActionListener(e -> {
                    File chosenDir = new File(directoryField.getText());

                    if (chosenDir.isDirectory()) {
                        forwardDirectories.clear();
                        storeCurrentDirectory();
                        currentDirectory = chosenDir;
                        refreshFiles();
                    } else if (chosenDir.isFile()) {
                        setOnFileChosen.set(chosenDir);
                    }
                });
                directoryField.setBounds(directoryFieldX, topComponentY, directoryFieldWidth, topComponentY);
                directoryFrame.getContentPane().add(directoryField);

                lastDirectory = new CyderButton(LAST_BUTTON_TEXT);
                lastDirectory.setBorder(BUTTON_BORDER);
                lastDirectory.addActionListener(e -> {
                    if (!backwardDirectories.isEmpty() && !backwardDirectories.peek().equals(currentDirectory)) {
                        forwardDirectories.push(currentDirectory);
                        currentDirectory = backwardDirectories.pop();
                        refreshFiles();
                    }
                });
                lastDirectory.setBounds(padding, topComponentY, navButtonSize, navButtonSize);
                directoryFrame.getContentPane().add(lastDirectory);

                nextDirectory = new CyderButton(NEXT_BUTTON_TEXT);
                nextDirectory.setBorder(BUTTON_BORDER);
                nextDirectory.addActionListener(e -> {
                    if (!forwardDirectories.isEmpty() && !forwardDirectories.peek().equals(currentDirectory)) {
                        backwardDirectories.push(currentDirectory);
                        storeCurrentDirectory();
                        currentDirectory = forwardDirectories.pop();
                        refreshFiles();
                    }
                });
                int nextX = frameWidth - 2 * padding - navButtonSize;
                nextDirectory.setBounds(nextX, topComponentY, navButtonSize, navButtonSize);
                directoryFrame.getContentPane().add(nextDirectory);

                directoryScrollList = new CyderScrollList(directoryScrollWidth,
                        directoryScrollHeight, CyderScrollList.SelectionPolicy.SINGLE);
                directoryScrollList.setScrollFont(directoryScrollFont);

                submitButton = new CyderButton(SUBMIT);
                submitButton.setColors(CyderColors.regularPink);
                submitButton.setBounds(padding, submitButtonY, directoryScrollWidth, navButtonSize);
                submitButton.addActionListener(e -> {
                    Optional<String> optionalSelectedElement = directoryScrollList.getSelectedElement();
                    if (optionalSelectedElement.isEmpty()) return;
                    String selectedElement = optionalSelectedElement.get();

                    filesList.forEach(file -> {
                        if (file.getName().equals(selectedElement)) {
                            if (file.isFile()) {
                                setOnFileChosen.set(file);
                                // todo else if builder allows submission of directories
                            } else {
                                directoryFrame.toast("Cannot submit a directory");
                            }
                        }
                    });
                });
                directoryFrame.getContentPane().add(submitButton);

                setupLoadingFilesLabel();
                loadingFilesLabel.setVisible(true);
                directoryFrame.getContentPane().add(loadingFilesLabel);

                Component relativeTo = getFileBuilder.getRelativeTo();
                if (relativeTo != null && getFileBuilder.isDisableRelativeTo()) {
                    relativeTo.setEnabled(false);
                    directoryFrame.addPostCloseAction(generateGetterFramePostCloseAction(relativeTo));
                }

                directoryFrame.setLocationRelativeTo(relativeTo);
                directoryFrame.setVisible(true);

                String builderInitial = getFileBuilder.getInitialString();
                if (StringUtil.isNullOrEmpty(builderInitial))
                    builderInitial = SystemPropertyKey.USER_DIR.getProperty();
                currentDirectory = new File(builderInitial);
                refreshFiles();
                // todo initial directory text if present
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
            directoryFrame.dispose(true);
            resetFileHistory();
        }

        return setOnFileChosen.get().getName().equals(CyderStrings.NULL)
                ? null : setOnFileChosen.get();
    }

    /**
     * Resets the current directory and file navigation history.
     */
    private void resetFileHistory() {
        currentDirectory = NULL_FILE;
        backwardDirectories.clear();
        forwardDirectories.clear();
        filesList.clear();
        filesNamesList.clear();
    }

    /**
     * Refreshes the files scroll based off of the {@link #currentDirectory}.
     */
    private void refreshFiles() {
        CyderThreadRunner.submit(() -> {
            if (directoryScrollLabel != null) directoryFrame.remove(directoryScrollLabel);
            loadingFilesLabel.setVisible(true);

            setNavComponentsEnabled(false);

            filesList.clear();
            filesNamesList.clear();

            directoryFrame.setTitle(currentDirectory.getName());

            File[] currentDirectoryFiles = currentDirectory.listFiles();
            if (currentDirectoryFiles != null && currentDirectoryFiles.length > 0) {
                Collections.addAll(filesList, currentDirectoryFiles);
            }

            filesList.forEach(file -> filesNamesList.add(file.getName()));

            directoryScrollList.removeAllElements();

            IntStream.range(0, filesNamesList.size()).forEach(i -> {
                File file = filesList.get(i);
                String fileName = filesNamesList.get(i);

                Runnable singleClickAction = () -> {
                    String suffix = file.isDirectory() ? " (Directory)" : "";
                    submitButton.setText(SUBMIT + CyderStrings.colon + CyderStrings.space + fileName + suffix);
                };

                Runnable doubleClickAction = () -> {
                    if (file.isDirectory()) {
                        forwardDirectories.clear();
                        storeCurrentDirectory();
                        currentDirectory = file;
                        refreshFiles();
                    } else {
                        setOnFileChosen.set(file);
                    }
                };

                directoryScrollList.addElementWithSingleAndDoubleClickAction(
                        fileName, singleClickAction, doubleClickAction);
            });

            directoryScrollLabel = directoryScrollList.generateScrollList();
            directoryScrollLabel.setBounds(padding, dirScrollYOffset, directoryScrollWidth, directoryScrollHeight);
            directoryFrame.getContentPane().add(directoryScrollLabel);

            submitButton.setText(SUBMIT);

            loadingFilesLabel.setVisible(false);
            setNavComponentsEnabled(true);

            directoryField.setText(currentDirectory.getAbsolutePath());
            directoryField.requestFocus();
        }, FILE_GETTER_LOADER);
    }

    /**
     * Pushes the current directory to the backwards directory if the proper conditions are met.
     */
    private void storeCurrentDirectory() {
        if (backwardDirectories.isEmpty()) {
            backwardDirectories.push(currentDirectory);
        } else {
            File backward = backwardDirectories.peek();
            if (backward != null && !backward.getAbsolutePath().equals(currentDirectory.getAbsolutePath())) {
                backwardDirectories.push(currentDirectory);
            }
        }
    }

    /**
     * Sets up the loading files label.
     */
    private void setupLoadingFilesLabel() {
        loadingFilesLabel.setText(BoundsUtil.addCenteringToHtml(CyderStrings.LOADING));
        loadingFilesLabel.setHorizontalAlignment(JLabel.CENTER);
        loadingFilesLabel.setVerticalAlignment(JLabel.CENTER);
        loadingFilesLabel.setFont(CyderFonts.DEFAULT_FONT);
        loadingFilesLabel.setForeground(CyderColors.navy);
        loadingFilesLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        loadingFilesLabel.setOpaque(false);
        loadingFilesLabel.setBounds(padding, dirScrollYOffset, directoryScrollWidth, directoryScrollHeight);
        loadingFilesLabel.setVisible(false);
    }

    /**
     * Sets whether the file chooser nav buttons are enabled.
     *
     * @param enabled whether the file chooser nav buttons are enabled
     */
    private void setNavComponentsEnabled(boolean enabled) {
        nextDirectory.setEnabled(enabled);
        lastDirectory.setEnabled(enabled);
        directoryField.setEnabled(enabled);
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
     * @param getConfirmationBuilder the GetConfirmationBuilder to use
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
