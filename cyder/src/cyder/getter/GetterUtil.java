package cyder.getter;

import com.google.common.collect.ImmutableList;
import cyder.bounds.BoundsString;
import cyder.bounds.BoundsUtil;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.ui.button.CyderButton;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.frame.FrameType;
import cyder.ui.label.CyderLabel;
import cyder.ui.pane.CyderScrollList;
import cyder.utils.StringUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static cyder.constants.CyderStrings.*;

/**
 * A getter utility for getting strings, confirmations, files, etc. from the user.
 */
public final class GetterUtil {
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
     * All the currently active get file frames associated with this instance.
     */
    private final ArrayList<CyderFrame> getFileFrames = new ArrayList<>();

    /**
     * All the currently active get confirmation confirmation associated with this instance.
     */
    private final ArrayList<CyderFrame> getConfirmationFrames = new ArrayList<>();

    /**
     * Closes all get input frames associated with this instance.
     */
    public void closeAllGetInputFrames() {
        getInputFrames.forEach(frame -> frame.dispose(true));
    }

    /**
     * Closes all get file frames associated with this instance.
     */
    public void closeAllGetFileFrames() {
        getFileFrames.forEach(frame -> frame.dispose(true));
    }

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
     * The minimum width for a get input frame.
     */
    private static final int getInputMinimumFrameWidth = 400;

    /**
     * The top and bottom padding for a get input popup.
     */
    private static final int getInputComponentYPadding = 10;

    /**
     * The left and right padding for a get input popup.
     */
    private static final int getInputFieldAndButtonXPadding = 40;

    /**
     * The height of the get input frame's input field and submit button.
     */
    private static final int getInputFieldAndButtonHeight = 40;

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
     *          String input = GetterUtil.getInstance().getInput(getInputBuilder);
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

        String threadName = "GetInput waiter thread, title: " + quote + getInputBuilder.getFrameTitle() + quote;
        CyderThreadRunner.submit(() -> {
            BoundsString boundsString = BoundsUtil.widthHeightCalculation(
                    getInputBuilder.getLabelText(),
                    getInputBuilder.getLabelFont(),
                    getInputMinimumFrameWidth);

            int textWidth = boundsString.getWidth() + 2 * getInputFieldAndButtonXPadding;
            int textHeight = boundsString.getHeight() + 2 * getInputComponentYPadding;
            String parsedLabelText = boundsString.getText();

            int fieldAndButtonWidth = textWidth - 2 * getInputFieldAndButtonXPadding;
            int frameHeight = CyderDragLabel.DEFAULT_HEIGHT + textHeight
                    + 2 * getInputFieldAndButtonHeight + 4 * getInputComponentYPadding;

            CyderFrame inputFrame = new CyderFrame(textWidth, frameHeight, CyderIcons.defaultBackground);
            getInputFrames.add(inputFrame);
            inputFrame.addPreCloseAction(() -> getInputFrames.remove(inputFrame));
            inputFrame.setFrameType(FrameType.INPUT_GETTER);
            inputFrame.setTitle(getInputBuilder.getFrameTitle());
            getInputBuilder.getOnDialogDisposalRunnables().forEach(inputFrame::addPostCloseAction);

            int yOff = CyderDragLabel.DEFAULT_HEIGHT + getInputComponentYPadding;
            CyderLabel textLabel = new CyderLabel(parsedLabelText);
            textLabel.setForeground(getInputBuilder.getLabelColor());
            textLabel.setFont(getInputBuilder.getLabelFont());
            textLabel.setBounds(getInputFieldAndButtonXPadding, yOff,
                    boundsString.getWidth(), boundsString.getHeight());
            inputFrame.getContentPane().add(textLabel);

            yOff += getInputComponentYPadding + boundsString.getHeight();

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
            inputField.setBounds(getInputFieldAndButtonXPadding, yOff, fieldAndButtonWidth,
                    getInputFieldAndButtonHeight);
            inputFrame.getContentPane().add(inputField);

            yOff += getInputComponentYPadding + getInputFieldAndButtonHeight;

            Runnable preCloseAction = () -> {
                String input = inputField.getTrimmedText();
                returnString.set(input.isEmpty() ? NULL : input);
            };
            inputFrame.addPreCloseAction(preCloseAction);

            Runnable submitAction = () -> {
                preCloseAction.run();
                inputFrame.dispose();
            };

            CyderButton submitButton = new CyderButton(getInputBuilder.getSubmitButtonText());
            submitButton.setBackground(getInputBuilder.getSubmitButtonColor());
            inputField.addActionListener(e -> submitAction.run());
            submitButton.setBorder(new LineBorder(CyderColors.navy, 5, false));
            submitButton.setFont(getInputBuilder.getSubmitButtonFont());
            submitButton.setForeground(CyderColors.navy);
            submitButton.addActionListener(e -> submitAction.run());
            submitButton.setBounds(getInputFieldAndButtonXPadding, yOff, fieldAndButtonWidth,
                    getInputFieldAndButtonHeight);
            inputFrame.getContentPane().add(submitButton);

            CyderFrame relativeTo = getInputBuilder.getRelativeTo();
            if (relativeTo != null && getInputBuilder.isDisableRelativeTo()) {
                relativeTo.setEnabled(false);
                inputFrame.addPostCloseAction(() -> {
                    relativeTo.setEnabled(true);
                    relativeTo.toFront();
                });
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
    private static final File nullFile = new File(NULL);

    /**
     * The text for the last button.
     */
    private static final String LAST_BUTTON_TEXT = space + "<" + space;

    /**
     * The text for the next button.
     */
    private static final String NEXT_BUTTON_TEXT = space + ">" + space;

    /**
     * The border for the next and last buttons.
     */
    private static final LineBorder getFileButtonBorder
            = new LineBorder(CyderColors.navy, 5, false);

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
    private static final int getFilePadding = 10;

    /**
     * The size of the navigation buttons.
     */
    private static final int navButtonSize = 40;

    /**
     * The y value of the directory scroll component.
     */
    private static final int dirScrollYOffset = CyderDragLabel.DEFAULT_HEIGHT + navButtonSize + 2 * getFilePadding;

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
    private static final int directoryFieldX = getFilePadding + navButtonSize + getFilePadding;

    /**
     * The directory field width.
     */
    private static final int directoryFieldWidth = directoryScrollWidth - 2 * navButtonSize - 2 * getFilePadding;

    /**
     * The y value of the components at the top of the get file frame.
     */
    private static final int getFileTopComponentY = CyderDragLabel.DEFAULT_HEIGHT + getFilePadding;

    /**
     * The padding from all other components/padding and the get file frame border.
     */
    private static final int getFileFrameXPadding = 15;

    /**
     * The width of the file chooser frame.
     */
    private static final int fileChooserFrameWidth = directoryScrollWidth + 2 * getFileFrameXPadding;

    /**
     * The padding on the bottom of the file chooser frame.
     */
    private static final int fileChooserBottomFramePadding = 100;

    /**
     * The height of the file chooser frame.
     */
    private static final int frameHeight = directoryScrollHeight + navButtonSize
            + 2 * getFilePadding + fileChooserBottomFramePadding;

    /**
     * The y value of the submit file button.
     */
    private static final int submitFileButtonY = frameHeight - navButtonSize - 2 * getFilePadding;

    /**
     * The submit file button.
     */
    private CyderButton submitFileButton;

    /**
     * The submit text for the submit button.
     */
    private static final String SUBMIT = "Submit";

    /**
     * Whether the submit button text should be updated to reflect the currently selected file/directory.
     */
    private final AtomicBoolean shouldUpdateSubmitButtonText = new AtomicBoolean();

    /**
     * The directory text.
     */
    private static final String DIRECTORY = "Directory";

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
     *          File userChosenFile = GetterUtil.getInstance().getFile(getInputBuilder);
     *          // Other operations using userChosenFile
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
    public Optional<File> getFile(GetFileBuilder getFileBuilder) {
        checkNotNull(getFileBuilder);

        directoryFrame = new CyderFrame(fileChooserFrameWidth, frameHeight);

        getFileFrames.add(directoryFrame);
        directoryFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                File ref = setOnFileChosen.get();
                if (ref == null || StringUtil.isNullOrEmpty(ref.getName())) {
                    setOnFileChosen.set(nullFile);
                }
            }
        });

        String threadName = "getFile waiter thread, title: " + quote + getFileBuilder.getFrameTitle() + quote;
        CyderThreadRunner.submit(() -> {
            try {
                resetFileHistory();

                directoryFrame.setFrameType(FrameType.INPUT_GETTER);
                directoryFrame.addPreCloseAction(() -> getFileFrames.remove(directoryFrame));
                getFileBuilder.getOnDialogDisposalRunnables().forEach(directoryFrame::addPostCloseAction);
                directoryFrame.setTitle(INITIAL_DIRECTORY_FRAME_TITLE);

                directoryField = new CyderTextField();
                directoryField.setBackground(CyderColors.vanilla);
                directoryField.setForeground(getFileBuilder.getFieldForeground());
                directoryField.setFont(getFileBuilder.getFieldFont());
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
                directoryField.setBounds(directoryFieldX, getFileTopComponentY, directoryFieldWidth,
                        getFileTopComponentY);
                directoryFrame.getContentPane().add(directoryField);

                lastDirectory = new CyderButton(LAST_BUTTON_TEXT);
                lastDirectory.setToolTipText("Previous directory");
                lastDirectory.setBorder(getFileButtonBorder);
                lastDirectory.addActionListener(e -> {
                    if (!backwardDirectories.isEmpty() && !backwardDirectories.peek().equals(currentDirectory)) {
                        forwardDirectories.push(currentDirectory);
                        currentDirectory = backwardDirectories.pop();
                        refreshFiles();
                    }
                });
                lastDirectory.setBounds(getFilePadding, getFileTopComponentY, navButtonSize, navButtonSize);
                directoryFrame.getContentPane().add(lastDirectory);

                nextDirectory = new CyderButton(NEXT_BUTTON_TEXT);
                nextDirectory.setToolTipText("Next directory");
                nextDirectory.setBorder(getFileButtonBorder);
                nextDirectory.addActionListener(e -> {
                    if (!forwardDirectories.isEmpty() && !forwardDirectories.peek().equals(currentDirectory)) {
                        backwardDirectories.push(currentDirectory);
                        storeCurrentDirectory();
                        currentDirectory = forwardDirectories.pop();
                        refreshFiles();
                    }
                });
                int nextX = fileChooserFrameWidth - 2 * getFilePadding - navButtonSize;
                nextDirectory.setBounds(nextX, getFileTopComponentY, navButtonSize, navButtonSize);
                directoryFrame.getContentPane().add(nextDirectory);

                directoryScrollList = new CyderScrollList(directoryScrollWidth,
                        directoryScrollHeight, CyderScrollList.SelectionPolicy.SINGLE);
                directoryScrollList.setScrollFont(directoryScrollFont);

                String buttonText = getFileBuilder.getSubmitButtonText();
                shouldUpdateSubmitButtonText.set(buttonText.equals(SUBMIT));

                submitFileButton = new CyderButton(buttonText);
                submitFileButton.setColors(getFileBuilder.getSubmitButtonColor());
                submitFileButton.setFont(getFileBuilder.getSubmitButtonFont());
                submitFileButton.setBounds(getFilePadding, submitFileButtonY, directoryScrollWidth, navButtonSize);
                submitFileButton.addActionListener(e -> {
                    Optional<String> optionalSelectedElement = directoryScrollList.getSelectedElement();
                    if (optionalSelectedElement.isEmpty()) return;
                    String selectedElement = optionalSelectedElement.get();

                    filesList.forEach(file -> {
                        if (file.getName().equals(selectedElement)) {
                            boolean allowFileSubmissions = getFileBuilder.isAllowFileSubmission();
                            boolean allowFolderSubmissions = getFileBuilder.isAllowFolderSubmission();

                            if (file.isFile()) {
                                if (allowFileSubmissions) {
                                    ImmutableList<String> extensions = getFileBuilder.getAllowableFileExtensions();
                                    if (!extensions.isEmpty()) {
                                        boolean set = false;

                                        for (String extension : extensions) {
                                            if (selectedElement.endsWith(extension)) {
                                                setOnFileChosen.set(file);
                                                set = true;
                                                break;
                                            }
                                        }

                                        if (!set) {
                                            StringBuilder extensionBuilder = new StringBuilder();
                                            extensionBuilder.append(openingBracket);

                                            for (int i = 0 ; i < extensions.size() ; i++) {
                                                extensionBuilder.append(extensions.get(i));
                                                if (i != extensions.size() - 1) extensionBuilder.append(", ");
                                            }

                                            extensionBuilder.append(closingBracket);
                                            directoryFrame.toast("File must be one of " + extensionBuilder);
                                        }
                                    } else {
                                        setOnFileChosen.set(file);
                                    }
                                } else {
                                    directoryFrame.toast("Cannot submit a file");
                                }
                            }

                            if (file.isDirectory()) {
                                if (allowFolderSubmissions) {
                                    setOnFileChosen.set(file);
                                } else {
                                    directoryFrame.toast("Cannot submit a folder");
                                }
                            }
                        }
                    });
                });
                directoryFrame.getContentPane().add(submitFileButton);

                setupLoadingFilesLabel();
                loadingFilesLabel.setVisible(true);
                directoryFrame.getContentPane().add(loadingFilesLabel);

                CyderFrame relativeTo = getFileBuilder.getRelativeTo();
                if (relativeTo != null && getFileBuilder.isDisableRelativeTo()) {
                    relativeTo.setEnabled(false);
                    directoryFrame.addPostCloseAction(() -> {
                        relativeTo.setEnabled(true);
                        relativeTo.toFront();
                    });
                }
                directoryFrame.setLocationRelativeTo(relativeTo);
                directoryFrame.setVisible(true);

                currentDirectory = getFileBuilder.getInitialDirectory();
                refreshFiles();
                String fieldText = getFileBuilder.getInitialFieldText();
                if (!StringUtil.isNullOrEmpty(fieldText)) directoryField.setText(fieldText);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, threadName);

        try {
            while (setOnFileChosen.get() == null) {
                Thread.onSpinWait();
            }
        } catch (Exception ignored) {} finally {
            directoryFrame.dispose(true);
            resetFileHistory();
        }

        boolean nullFile = setOnFileChosen.get().getName().equals(NULL);
        if (nullFile) {
            return Optional.empty();
        } else {
            return Optional.of(setOnFileChosen.get());
        }
    }

    /**
     * Resets the current directory and file navigation history.
     */
    private void resetFileHistory() {
        currentDirectory = nullFile;
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
                    if (shouldUpdateSubmitButtonText.get()) {
                        String suffix = file.isDirectory()
                                ? space + openingParenthesis + DIRECTORY + closingParenthesis : "";
                        submitFileButton.setText(SUBMIT + colon + space + fileName + suffix);
                    }
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
            directoryScrollLabel.setBounds(getFilePadding, dirScrollYOffset,
                    directoryScrollWidth, directoryScrollHeight);
            directoryFrame.getContentPane().add(directoryScrollLabel);

            if (shouldUpdateSubmitButtonText.get()) {
                submitFileButton.setText(SUBMIT);
            }

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
        loadingFilesLabel.setText(StringUtil.addCenteringToHtml(LOADING));
        loadingFilesLabel.setHorizontalAlignment(JLabel.CENTER);
        loadingFilesLabel.setVerticalAlignment(JLabel.CENTER);
        loadingFilesLabel.setFont(CyderFonts.DEFAULT_FONT);
        loadingFilesLabel.setForeground(CyderColors.navy);
        loadingFilesLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        loadingFilesLabel.setOpaque(false);
        loadingFilesLabel.setBounds(getFilePadding, dirScrollYOffset, directoryScrollWidth, directoryScrollHeight);
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
     * The padding of the confirmation buttons.
     */
    private static final int confirmationButtonXPadding = 20;

    /**
     * The height of the confirmation buttons.
     */
    private static final int confirmationButtonHeight = 40;

    /**
     * The padding between the top of the frame and confirmation frame components.
     */
    private static final int confirmationFrameTopPadding = 40;

    /**
     * The padding between the bottom of the confirmation text and the next components.
     */
    private static final int confirmationTextBottomPadding = 20;

    /**
     * The padding between the confirmation buttons and the bottom of the frame.
     */
    private static final int confirmationButtonBottomPadding = 25;

    /**
     * The padding between the frame edges and the confirmation label.
     */
    private static final int confirmationTextLabelXPadding = 10;

    /**
     * The number of confirmation buttons.
     */
    private static final int numConfirmationButtons = 2;

    /**
     * The padding between the confirmation buttons.
     */
    private static final int yesNoButtonPadding = 30;

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
     *          boolean actionApproved = GetterUtil.getInstance().getConfirmation(getConfirmationBuilder);
     *          // Other operations using actionApproved
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

        AtomicReference<Boolean> ret = new AtomicReference<>(null);
        AtomicReference<CyderFrame> frameReference = new AtomicReference<>();

        String threadName = "getConfirmation waiter thread, title: "
                + quote + getConfirmationBuilder.getFrameTitle() + quote;
        CyderThreadRunner.submit(() -> {
            try {
                CyderLabel textLabel = new CyderLabel();
                textLabel.setForeground(getConfirmationBuilder.getLabelColor());
                textLabel.setFont(getConfirmationBuilder.getLabelFont());

                BoundsString boundsString = BoundsUtil.widthHeightCalculation(
                        getConfirmationBuilder.getLabelText(), textLabel.getFont());
                int textWidth = boundsString.getWidth();
                int textHeight = boundsString.getHeight();
                textLabel.setText(boundsString.getText());

                int confirmationFrameWidth = 2 * confirmationButtonXPadding + textWidth;
                int confirmationFrameHeight = confirmationFrameTopPadding + textHeight
                        + confirmationTextBottomPadding + confirmationButtonHeight + confirmationButtonBottomPadding;
                int confirmationButtonWidth = (confirmationFrameWidth - yesNoButtonPadding
                        - 2 * confirmationButtonXPadding) / numConfirmationButtons;
                int noButtonX = confirmationButtonXPadding + confirmationButtonWidth + yesNoButtonPadding;

                CyderFrame confirmationFrame = new CyderFrame(
                        confirmationFrameWidth, confirmationFrameHeight, CyderIcons.defaultBackgroundLarge);
                getConfirmationFrames.add(confirmationFrame);
                frameReference.set(confirmationFrame);
                getConfirmationBuilder.getOnDialogDisposalRunnables().forEach(confirmationFrame::addPostCloseAction);

                confirmationFrame.setFrameType(FrameType.INPUT_GETTER);
                confirmationFrame.setTitle(getConfirmationBuilder.getFrameTitle());
                confirmationFrame.addPreCloseAction(() -> {
                    if (ret.get() != Boolean.TRUE) {
                        ret.set(Boolean.FALSE);
                    }

                    getConfirmationFrames.remove(confirmationFrame);
                });

                int currentY = confirmationFrameTopPadding;

                textLabel.setBounds(confirmationTextLabelXPadding, currentY, textWidth, textHeight);
                confirmationFrame.getContentPane().add(textLabel);
                currentY += textHeight + confirmationTextBottomPadding;

                CyderButton yesButton = new CyderButton(getConfirmationBuilder.getYesButtonText());
                yesButton.setColors(getConfirmationBuilder.getYesButtonColor());
                yesButton.setFont(getConfirmationBuilder.getYesButtonFont());
                yesButton.addActionListener(e -> ret.set(Boolean.TRUE));
                yesButton.setBounds(confirmationButtonXPadding, currentY,
                        confirmationButtonWidth, confirmationButtonHeight);
                confirmationFrame.getContentPane().add(yesButton);

                CyderButton noButton = new CyderButton(getConfirmationBuilder.getNoButtonText());
                noButton.setColors(getConfirmationBuilder.getNoButtonColor());
                yesButton.setFont(getConfirmationBuilder.getNoButtonFont());
                noButton.addActionListener(e -> ret.set(Boolean.FALSE));
                noButton.setBounds(noButtonX, currentY, confirmationButtonWidth, confirmationButtonHeight);
                confirmationFrame.getContentPane().add(noButton);

                CyderFrame relativeTo = getConfirmationBuilder.getRelativeTo();
                if (relativeTo != null && getConfirmationBuilder.isDisableRelativeTo()) {
                    relativeTo.setEnabled(false);
                    confirmationFrame.addPostCloseAction(() -> {
                        relativeTo.setEnabled(true);
                        relativeTo.toFront();
                    });
                }

                confirmationFrame.setLocationRelativeTo(relativeTo);
                confirmationFrame.setVisible(true);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, threadName);

        while (ret.get() == null) Thread.onSpinWait();
        frameReference.get().dispose();
        return ret.get();
    }
}
