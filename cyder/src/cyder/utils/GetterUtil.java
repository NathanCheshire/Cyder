package cyder.utils;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.annotations.ForReadability;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.ui.button.CyderButton;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.label.CyderLabel;
import cyder.ui.pane.CyderScrollList;
import cyder.user.UserUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A getter utility for getting strings, confirmations, files, etc. from the user.
 */
public class GetterUtil {
    /**
     * To obtain an instance, use {@link GetterUtil#getInstance()}.
     */
    private GetterUtil() {
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
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
     * All the currently active get string frames associated with this instance.
     */
    private final ArrayList<CyderFrame> getStringFrames = new ArrayList<>();

    /**
     * Closes all get string frames associated with this instance.
     */
    public void closeAllGetStringFrames() {
        for (CyderFrame frame : getStringFrames) {
            frame.dispose(true);
        }
    }

    /**
     * All the currently active get file frames associated with this instance.
     */
    private final ArrayList<CyderFrame> getFileFrames = new ArrayList<>();

    /**
     * Closes all get file frames associated with this instance.
     */
    public void closeAllGetFileFrames() {
        for (CyderFrame frame : getFileFrames) {
            frame.dispose(true);
        }
    }

    /**
     * All the currently active get string confirmation associated with this instance.
     */
    private final ArrayList<CyderFrame> getConfirmationFrames = new ArrayList<>();

    /**
     * Closes all get confirmation frames associated with this instance.
     */
    public void closeAllGetConfirmationFrames() {
        for (CyderFrame frame : getConfirmationFrames) {
            frame.dispose(true);
        }
    }

    /**
     * Closes all getter frames associated with this instance.
     */
    public void closeAllGetFrames() {
        closeAllGetStringFrames();
        closeAllGetFileFrames();
        closeAllGetConfirmationFrames();
    }

    // ------------------------
    // End frame tracking logic
    // ------------------------

    /**
     * The minimum width for a get string popup.
     */
    private static final int GET_STRING_MIN_WIDTH = 400;

    /**
     * The minimum height for a get string popup.
     */
    private static final int GET_STRING_MIN_HEIGHT = 170;

    /**
     * The top and bottom padding for a string popup.
     */
    private static final int getStringYPadding = 10;

    /**
     * The left and right padding for a string popup.
     */
    private static final int getStringXPadding = 40;

    /**
     * The empty string to return for getString invocations which are canceled.
     */
    private static final String NULL = "NULL";

    /**
     * Custom getString() method, see usage below for how to
     * setup so that the calling thread is not blocked.
     * <p>
     * USAGE:
     * <pre>
     *  {@code
     *  CyderThreadRunner.submit(() -> {
     *      try {
     *          String input = GetterUtil().getInstance().getString(getterBuilder);
     *          // Other operations using input
     *      } catch (Exception e) {
     *          ErrorHandler.handle(e);
     *      }
     *  }, "THREAD_NAME").start();
     *  }
     *  </pre>
     *
     * @param builder the builder to use
     * @return the user entered input string. NOTE: if any improper
     * input is attempted to be returned, this function returns
     * the string literal of NULL instead of {@code null}
     */
    public String getString(Builder builder) {
        checkNotNull(builder);

        AtomicReference<String> returnString = new AtomicReference<>();

        CyderThreadRunner.submit(() -> {
            try {
                int height = GET_STRING_MIN_HEIGHT;
                int width = GET_STRING_MIN_WIDTH;

                BoundsUtil.BoundsString bounds = null;

                if (!StringUtil.isNullOrEmpty(builder.getLabelText())) {
                    bounds = BoundsUtil.widthHeightCalculation(builder.getLabelText(),
                            CyderFonts.DEFAULT_FONT, GET_STRING_MIN_WIDTH);

                    height += bounds.height() + 2 * getStringYPadding;
                    width = bounds.width() + 2 * getStringXPadding;
                    builder.setLabelText(bounds.text());
                }

                CyderFrame inputFrame = new CyderFrame(width,
                        height, CyderIcons.defaultBackground);
                getStringFrames.add(inputFrame);
                inputFrame.addPreCloseAction(() -> getStringFrames.remove(inputFrame));
                inputFrame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);
                inputFrame.setTitle(builder.getTitle());

                int yOff = CyderDragLabel.DEFAULT_HEIGHT + getStringYPadding;
                if (bounds != null) {
                    CyderLabel textLabel = new CyderLabel(builder.getLabelText());
                    textLabel.setBounds(getStringXPadding, yOff, bounds.width(), bounds.height());
                    inputFrame.getContentPane().add(textLabel);

                    yOff += getStringYPadding + bounds.height();
                }

                CyderTextField inputField = new CyderTextField(0);
                inputField.setHorizontalAlignment(JTextField.CENTER);
                inputField.setBackground(Color.white);

                String initialString = builder.getInitialString();
                if (!StringUtil.isNullOrEmpty(initialString)) inputField.setText(initialString);

                String tooltip = builder.getFieldTooltip();
                if (!StringUtil.isNullOrEmpty(tooltip)) inputField.setToolTipText(tooltip);

                inputField.setBounds(getStringXPadding, yOff,
                        width - 2 * getStringXPadding, 40);
                inputFrame.getContentPane().add(inputField);

                yOff += getStringYPadding + 40;

                CyderButton submit = new CyderButton(builder.getSubmitButtonText());
                submit.setBackground(builder.getSubmitButtonColor());
                inputField.addActionListener(e1 -> {
                    returnString.set((inputField.getText() == null || inputField.getText().isEmpty() ?
                            NULL : inputField.getText()));
                    inputFrame.dispose();
                });
                submit.setBorder(new LineBorder(CyderColors.navy, 5, false));
                submit.setFont(CyderFonts.SEGOE_20);
                submit.setForeground(CyderColors.navy);
                submit.addActionListener(e12 -> {
                    returnString.set((inputField.getText() == null || inputField.getText().isEmpty() ?
                            NULL : inputField.getText()));
                    inputFrame.dispose();
                });
                submit.setBounds(getStringXPadding, yOff,
                        width - 2 * getStringXPadding, 40);
                inputFrame.getContentPane().add(submit);

                inputFrame.addPreCloseAction(() -> returnString.set((inputField.getText() == null
                        || inputField.getText().isEmpty()
                        || inputField.getText().equals(builder.getInitialString()) ? NULL : inputField.getText())));

                Component relativeTo = builder.getRelativeTo();

                if (relativeTo != null && builder.isDisableRelativeTo()) {
                    relativeTo.setEnabled(false);
                    inputFrame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            relativeTo.setEnabled(true);
                        }
                    });
                }

                inputFrame.setLocationRelativeTo(relativeTo);
                inputFrame.setVisible(true);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, getGetStringThreadName(builder));

        try {
            // todo can we guarantee these while loops that wait for input will exit if the frames are disposed?
            while (returnString.get() == null) {
                Thread.onSpinWait();
            }
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        return returnString.get();
    }

    @ForReadability
    private String getGetStringThreadName(Builder builder) {
        return "GetString Waiter thread, title = \"" + builder.getTitle() + "\"";
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

    /**
     * The null file.
     */
    private static final File NULL_FILE = new File(NULL);

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
        CyderFrame referenceInitFrame = new CyderFrame(630, 510, backgroundColor);

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

                directoryFrame.setTitle(INITIAL_DIRECTORY_FRAME_TITLE);

                dirFieldRef.set(new CyderTextField(0));
                if (!StringUtil.isNullOrEmpty(builder.getFieldTooltip())) {
                    dirFieldRef.get().setToolTipText(builder.getFieldTooltip());
                }
                dirFieldRef.get().setBackground(darkMode ? CyderColors.darkModeBackgroundColor : Color.white);
                dirFieldRef.get().setForeground(darkMode ? CyderColors.defaultDarkModeTextColor : CyderColors.navy);
                dirFieldRef.get().setBorder(new LineBorder(textColor, 5, false));
                dirFieldRef.get().addActionListener(e -> {
                    File ChosenDir = new File(dirFieldRef.get().getText());

                    if (ChosenDir.isDirectory()) {
                        refreshBasedOnDir(ChosenDir, true);
                    } else if (ChosenDir.isFile()) {
                        setOnFileChosen.set(ChosenDir);
                    }
                });
                dirFieldRef.get().setBounds(60, 40, 500, 40);
                directoryFrame.getContentPane().add(dirFieldRef.get());
                dirFieldRef.get().setEnabled(false);

                last = new CyderButton(LAST_BUTTON_TEXT);
                last.setFocusPainted(false);
                last.setForeground(CyderColors.navy);
                last.setBackground(CyderColors.regularRed);
                last.setFont(CyderFonts.SEGOE_20);
                last.setBorder(BUTTON_BORDER);
                last.addActionListener(e -> {
                    //we may only go back if there's something in the back and it's different from where we are now
                    if (!backward.isEmpty() && !backward.peek().equals(currentDirectory)) {
                        //traversing so push where we are to forward
                        forward.push(currentDirectory);

                        //get where we're going
                        currentDirectory = backward.pop();

                        //now simply refresh based on currentDir
                        refreshBasedOnDir(currentDirectory, false);
                    }
                });
                last.setBounds(10, 40, 40, 40);
                directoryFrame.getContentPane().add(last);
                last.setEnabled(false);

                next = new CyderButton(NEXT_BUTTON_TEXT);
                next.setFocusPainted(false);
                next.setForeground(CyderColors.navy);
                next.setBackground(CyderColors.regularRed);
                next.setFont(CyderFonts.SEGOE_20);
                next.setBorder(BUTTON_BORDER);
                next.addActionListener(e -> {
                    //only traverse forward if the stack is not empty and forward is different from where we are
                    if (!forward.isEmpty() && !forward.peek().equals(currentDirectory)) {
                        //push where we are
                        backward.push(currentDirectory);

                        //figure out where we need to go
                        currentDirectory = forward.pop();

                        //refresh based on where we should go
                        refreshBasedOnDir(currentDirectory, false);
                    }
                });
                next.setBounds(620 - 50, 40, 40, 40);
                directoryFrame.getContentPane().add(next);
                next.setEnabled(false);

                int mainComponentWidth = 600;
                int mainComponentHeight = 400;

                // label to show where files will be
                JLabel tempLabel = new JLabel();
                tempLabel.setText("<html><div align=\"center\">Loading...</div></html>");
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
                    directoryFrame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            relativeTo.setEnabled(true);
                        }
                    });
                }

                directoryFrame.setLocationRelativeTo(relativeTo);
                directoryFrame.setVisible(true);

                // Load files
                CyderThreadRunner.submit(() -> {
                    String path = !StringUtil.isNullOrEmpty(builder.getInitialString())
                            && new File(builder.getInitialString()).exists()
                            ? builder.getInitialString() : OSUtil.USER_DIR;
                    currentDirectory = new File(path);

                    directoryFrame.setTitle(currentDirectory.getName());

                    File[] currentDirectoryFiles = currentDirectory.listFiles();
                    if (currentDirectoryFiles != null && currentDirectoryFiles.length > 0) {
                        Collections.addAll(directoryFileList, currentDirectoryFiles);
                    }

                    for (File file : directoryFileList) {
                        directoryNameList.add(file.getName());
                    }

                    cyderScrollListRef.set(new CyderScrollList(mainComponentWidth, mainComponentHeight,
                            CyderScrollList.SelectionPolicy.SINGLE, darkMode));
                    cyderScrollListRef.get().setScrollFont(CyderFonts.SEGOE_20.deriveFont(16f));

                    //adding things to the list and setting up actions for what to do when an element is clicked
                    IntStream.of(directoryNameList.size()).forEach(index ->
                            cyderScrollListRef.get().addElement(directoryNameList.get(index), () -> {
                                if (directoryFileList.get(index).isDirectory()) {
                                    refreshBasedOnDir(directoryFileList.get(index), false);
                                } else {
                                    setOnFileChosen.set(directoryFileList.get(index));
                                }
                            }));

                    dirScrollLabel = cyderScrollListRef.get().generateScrollList();
                    dirScrollLabel.setBounds(10, 90, mainComponentWidth, mainComponentHeight);
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
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, getGetFileThreadName(builder));

        try {
            while (setOnFileChosen.get() == null) {
                Thread.onSpinWait();
            }
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
        } finally {
            directoryFrameReference.get().dispose();
        }

        return setOnFileChosen.get().getName().equals(NULL) ? null : setOnFileChosen.get();
    }

    @ForReadability
    private String getGetFileThreadName(Builder builder) {
        return "GetFile Waiter Thread, title = \"" + builder.getTitle() + "\"";
    }

    /**
     * Refreshes the current file list based on the provided file.
     *
     * @param directory   the directory/file to refresh on
     * @param wipeForward whether to clear the forward traversal stack
     */
    @ForReadability
    private void refreshBasedOnDir(File directory, boolean wipeForward) {
        checkNotNull(directory);

        // clear the forward list
        if (wipeForward) {
            forward.clear();

            // if not last thing pushed
            if (backward.isEmpty() || !backward.peek().equals(currentDirectory)) {
                backward.push(currentDirectory);
            }
        }

        // remove old scroll
        cyderScrollListRef.get().removeAllElements();
        directoryFrameReference.get().remove(dirScrollLabel);

        // if given a file, use its parent
        if (directory.isFile()) {
            directory = directory.getParentFile();
        }

        // set to new directory
        currentDirectory = directory;

        // wipe lists
        directoryNameList.clear();
        directoryFileList.clear();

        File[] currentDirectoryFiles = currentDirectory.listFiles();
        if (currentDirectoryFiles != null && currentDirectoryFiles.length > 0) {
            Collections.addAll(directoryFileList, currentDirectoryFiles);
        }

        // regenerate names list
        for (File file : directoryFileList) {
            directoryNameList.add(file.getName());
        }

        // remake scroll list object
        cyderScrollListRef.set(new CyderScrollList(600, 400,
                CyderScrollList.SelectionPolicy.SINGLE, cyderScrollListRef.get().isDarkMode()));
        cyderScrollListRef.get().setScrollFont(CyderFonts.SEGOE_20.deriveFont(16f));

        // generate clickable components to add to the list
        for (int i = 0 ; i < directoryNameList.size() ; i++) {
            int eye = i;

            cyderScrollListRef.get().addElement(directoryNameList.get(i), () -> {
                if (directoryFileList.get(eye).isDirectory()) {
                    refreshBasedOnDir(directoryFileList.get(eye), true);
                } else {
                    setOnFileChosen.set(directoryFileList.get(eye));
                }
            });
        }
        dirScrollLabel = cyderScrollListRef.get().generateScrollList();
        dirScrollLabel.setBounds(10, 90, 600, 400);
        directoryFrameReference.get().getContentPane().add(dirScrollLabel);

        // revalidate, set title, set pwd text
        directoryFrameReference.get().revalidate();
        directoryFrameReference.get().repaint();
        directoryFrameReference.get().setTitle(currentDirectory.getName());

        dirFieldRef.get().setText(currentDirectory.getAbsolutePath());
    }

    /**
     * Custom getInput() method, see usage below for how to
     * setup so that the calling thread is not blocked.
     * <p>
     * USAGE:
     * <pre>
     *  {@code
     *  CyderThreadRunner.submit(() -> {
     *      try {
     *          String input = GetterUtil().getInstance().getConfirmation(getterBuilder);
     *          // Other operations using input
     *      } catch (Exception e) {
     *          ErrorHandler.handle(e);
     *      }
     *  }, "THREAD_NAME").start();
     *  }
     *  </pre>
     *
     * @param builder the builder to use
     * @return whether the user confirmed the operation
     */
    public boolean getConfirmation(Builder builder) {
        checkNotNull(builder);

        AtomicReference<Boolean> ret = new AtomicReference<>();
        ret.set(null);
        AtomicReference<CyderFrame> frameReference = new AtomicReference<>();

        CyderThreadRunner.submit(() -> {
            try {
                CyderLabel textLabel = new CyderLabel();

                BoundsUtil.BoundsString boundsString = BoundsUtil.widthHeightCalculation(
                        builder.getInitialString(), textLabel.getFont());
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

                frame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);
                frame.setTitle(builder.getTitle());
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

                CyderButton yesButton = new CyderButton(builder.getYesButtonText());
                yesButton.setColors(builder.getSubmitButtonColor());
                yesButton.addActionListener(e -> ret.set(Boolean.TRUE));
                yesButton.setBounds(frameHorizontalPadding, currentY, buttonWidth, yesNoButtonHeight);
                frame.getContentPane().add(yesButton);

                int noButtonX = frameHorizontalPadding + buttonWidth + buttonInnerSpacing;
                CyderButton noButton = new CyderButton(builder.getNoButtonText());
                noButton.setColors(builder.getSubmitButtonColor());
                noButton.addActionListener(e -> ret.set(Boolean.FALSE));
                noButton.setBounds(noButtonX, currentY, buttonWidth, yesNoButtonHeight);
                frame.getContentPane().add(noButton);

                Component relativeTo = builder.getRelativeTo();
                if (relativeTo != null && builder.isDisableRelativeTo()) {
                    relativeTo.setEnabled(false);
                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            relativeTo.setEnabled(true);
                        }
                    });
                }

                frame.setLocationRelativeTo(relativeTo);
                frame.setVisible(true);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, getGetConfirmationThreadName(builder));

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

    @ForReadability
    private String getGetConfirmationThreadName(Builder builder) {
        return "GetConfirmation Waiter Thread, title = \"" + builder.getTitle() + "\"";
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
         * The button text for the submit button for some getter frames.
         */
        private String submitButtonText = "Submit";

        /**
         * The field tooltip to display for getter frames which contain a text field.
         */
        private String fieldTooltip = "Input";

        /**
         * The text field regex to use for getter frames which contain a text field.
         */
        private String fieldRegex;

        /**
         * Te component to set the getter frame relative to.
         */
        private Component relativeTo;

        /**
         * The color of the submit button for most getter frames.
         */
        private Color submitButtonColor = CyderColors.regularRed;

        /**
         * The initial text of the field for getter frames which have a text field.
         */
        private String initialString = "";

        /**
         * The label text for getter frames which use a label.
         */
        private String labelText = "";

        /**
         * The text for confirming an operation.
         */
        private String yesButtonText = "Yes";

        /**
         * the text for denying an operation.
         */
        private String noButtonText = "No";

        /**
         * Whether to disable the component the getter frame was
         * set relative to while the relative frame is open.
         */
        private boolean disableRelativeTo;

        /**
         * The minimum text length of a getter frame title.
         */
        public static final int MINIMUM_TITLE_LENGTH = 3;

        /**
         * Constructs a new GetterBuilder.
         *
         * @param title the frame title/the text for confirmations
         */
        public Builder(String title) {
            this.title = checkNotNull(title);
            checkArgument(title.length() >= MINIMUM_TITLE_LENGTH);

            Logger.log(Logger.Tag.OBJECT_CREATION, this);
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
         * Returns the submit button text for getter frames which get field input from the user.
         *
         * @return the submit button text for getter frames which get field input from the user
         */
        public String getSubmitButtonText() {
            return submitButtonText;
        }

        /**
         * Sets the submit button text for getter frames which get field input from the user.
         *
         * @param submitButtonText the submit button text for getter frames which get field input from the user
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setSubmitButtonText(String submitButtonText) {
            this.submitButtonText = submitButtonText;
            return this;
        }

        /**
         * Returns the field tooltip text for getter frames which get field input from the user.
         *
         * @return the field tooltip text for getter frames which get field input from the user
         */
        public String getFieldTooltip() {
            return fieldTooltip;
        }

        /**
         * Sets the field tooltip text for getter frames which get field input from the user.
         *
         * @param fieldTooltip the field tooltip text for getter frames which get field input from the user
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setFieldTooltip(String fieldTooltip) {
            this.fieldTooltip = fieldTooltip;
            return this;
        }

        /**
         * returns the field regex for getter frames which get field input from the user.
         *
         * @return the field regex for getter frames which get field input from the user
         */
        public String getFieldRegex() {
            return fieldRegex;
        }

        /**
         * Sets the field regex for getter frames which get field input from the user.
         *
         * @param fieldRegex the field regex for getter frames which get field input from the user
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setFieldRegex(String fieldRegex) {
            this.fieldRegex = fieldRegex;
            return this;
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
         * Returns the button background color for the submit button for getter frames which get input from a user.
         *
         * @return the button background color for the submit button for getter frames which get input from a user
         */
        public Color getSubmitButtonColor() {
            return submitButtonColor;
        }

        /**
         * Sets the button background color for the submit button for getter frames which get input from a user.
         *
         * @param submitButtonColor the button background color for the
         *                          submit button for getter frames which get input from a user
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setSubmitButtonColor(Color submitButtonColor) {
            this.submitButtonColor = submitButtonColor;
            return this;
        }

        /**
         * Returns the initial field text for getter frames which have an input field.
         *
         * @return the initial field text for getter frames which have an input field
         */
        public String getInitialString() {
            return initialString;
        }

        /**
         * Sets the initial field text for getter frames which have an input field.
         *
         * @param initialString the initial field text for getter frames which have an input field
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setInitialString(String initialString) {
            this.initialString = initialString;
            return this;
        }

        /**
         * Returns the text to display on the button for approving a requested operation.
         *
         * @return the text to display on the button for approving a requested operation
         */
        public String getYesButtonText() {
            return yesButtonText;
        }

        /**
         * Sets the text to display on the button for approving a requested operation.
         *
         * @param yesButtonText the text to display on the button for approving a requested operation
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setYesButtonText(String yesButtonText) {
            this.yesButtonText = yesButtonText;
            return this;
        }

        /**
         * Returns the text to display on the button for denying a requested operation.
         *
         * @return the text to display on the button for denying a requested operation
         */
        public String getNoButtonText() {
            return noButtonText;
        }

        /**
         * Sets the text to display on the button for denying a requested operation.
         *
         * @param noButtonText the text to display on the button for denying a requested operation
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setNoButtonText(String noButtonText) {
            this.noButtonText = noButtonText;
            return this;
        }

        /**
         * Returns the label text for getter frames which have a primary information label.
         *
         * @return the label text for getter frames which have a primary information label
         */
        public String getLabelText() {
            return labelText;
        }

        /**
         * Sets the label text for getter frames which have a primary information label.
         *
         * @param labelText the label text for getter frames which have a primary information label
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setLabelText(String labelText) {
            this.labelText = labelText;
            return this;
        }

        /**
         * Returns whether to disable the relativeTo component while the getter frame is active.
         *
         * @return whether to disable the relativeTo component while the getter frame is active
         */
        public boolean isDisableRelativeTo() {
            return disableRelativeTo;
        }

        /**
         * Sets whether to disable the relativeTo component while the getter frame is active.
         *
         * @param disableRelativeTo whether to disable the relativeTo component while the getter frame is active
         * @return this builder
         */
        @CanIgnoreReturnValue
        public Builder setDisableRelativeTo(boolean disableRelativeTo) {
            this.disableRelativeTo = disableRelativeTo;
            return this;
        }
    }
}
