package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.annotations.ForReadability;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
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
     * The top and bottom padding for a string popup.
     */
    private static final int GET_STRING_Y_PADDING = 10;

    /**
     * The left and right padding for a string popup.
     */
    private static final int GET_STRING_X_PADDING = 40;

    /**
     * The empty string to return for getString invocations which are canceled.
     */
    private static final String NULL = "NULL";

    /**
     * The line border for the get string's submit button.
     */
    private static final LineBorder GET_STRING_SUBMIT_BUTTON_BORDER
            = new LineBorder(CyderColors.navy, 5, false);

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
        checkNotNull(builder.getLabelText());

        AtomicReference<String> returnString = new AtomicReference<>();

        CyderThreadRunner.submit(() -> {
            try {
                BoundsUtil.BoundsString boundsString = BoundsUtil.widthHeightCalculation(
                        builder.getLabelText(), CyderFonts.DEFAULT_FONT, GET_STRING_MIN_WIDTH);

                int width = boundsString.width() + 2 * GET_STRING_X_PADDING;
                int height = boundsString.height() + 2 * GET_STRING_Y_PADDING;
                builder.setLabelText(boundsString.text());

                CyderFrame inputFrame = new CyderFrame(width, height, CyderIcons.defaultBackground);
                getStringFrames.add(inputFrame);
                inputFrame.addPreCloseAction(() -> getStringFrames.remove(inputFrame));
                inputFrame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);
                inputFrame.setTitle(builder.getTitle());

                int yOff = CyderDragLabel.DEFAULT_HEIGHT + GET_STRING_Y_PADDING;
                CyderLabel textLabel = new CyderLabel(builder.getLabelText());
                textLabel.setBounds(GET_STRING_X_PADDING, yOff, boundsString.width(), boundsString.height());
                inputFrame.getContentPane().add(textLabel);

                yOff += GET_STRING_Y_PADDING + boundsString.height();

                CyderTextField inputField = new CyderTextField(0);
                inputField.setHorizontalAlignment(JTextField.CENTER);
                inputField.setBackground(Color.white);

                String initialString = builder.getInitialString();
                if (!StringUtil.isNullOrEmpty(initialString)) inputField.setText(initialString);

                String tooltip = builder.getFieldTooltip();
                if (!StringUtil.isNullOrEmpty(tooltip)) inputField.setToolTipText(tooltip);

                int componentWidth = width - 2 * GET_STRING_X_PADDING;
                int componentHeight = 40;
                inputField.setBounds(GET_STRING_X_PADDING, yOff, componentWidth, componentHeight);
                inputFrame.getContentPane().add(inputField);

                yOff += GET_STRING_Y_PADDING + componentHeight;

                Runnable submitAction = () -> {
                    returnString.set((inputField.getText() == null
                            || inputField.getText().isEmpty() ? NULL : inputField.getText()));
                    inputFrame.dispose();
                };

                CyderButton submit = new CyderButton(builder.getSubmitButtonText());
                submit.setBackground(builder.getSubmitButtonColor());
                inputField.addActionListener(e -> submitAction.run());
                submit.setBorder(GET_STRING_SUBMIT_BUTTON_BORDER);
                submit.setFont(CyderFonts.SEGOE_20);
                submit.setForeground(CyderColors.navy);
                submit.addActionListener(e -> submitAction.run());
                submit.setBounds(GET_STRING_X_PADDING, yOff, componentWidth, componentHeight);
                inputFrame.getContentPane().add(submit);

                inputFrame.addPreCloseAction(() -> returnString.set((inputField.getText() == null
                        || inputField.getText().isEmpty()
                        || inputField.getText().equals(builder.getInitialString()) ? NULL : inputField.getText())));

                Component relativeTo = builder.getRelativeTo();
                if (relativeTo != null && builder.isDisableRelativeTo()) {
                    relativeTo.setEnabled(false);
                    inputFrame.addWindowListener(generateEnableComponentOnWindowCloseWindowAdapter(relativeTo));
                }

                inputFrame.setLocationRelativeTo(relativeTo);
                inputFrame.setVisible(true);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, getGetStringThreadName(builder));

        try {
            while (returnString.get() == null) {
                Thread.onSpinWait();
            }
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        return returnString.get();
    }

    @ForReadability
    private WindowAdapter generateEnableComponentOnWindowCloseWindowAdapter(Component frame) {
        return new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                frame.setEnabled(true);
            }
        };
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
    private final Stack<File> backward = new Stack<>() {
        @Override
        public File push(File file) {
            Preconditions.checkNotNull(file);

            if (isEmpty()) {
                return super.push(file);
            }

            String lastFilePath = peek().getAbsolutePath();
            if (!file.getAbsolutePath().equals(lastFilePath)) {
                return super.push(file);
            }

            throw new IllegalArgumentException("Cannot add duplicates sequentially");
        }
    };

    /**
     * The forward stack.
     */
    private final Stack<File> forward = new Stack<>() {
        @Override
        public File push(File file) {
            Preconditions.checkNotNull(file);

            if (isEmpty()) {
                return super.push(file);
            }

            String lastFilePath = peek().getAbsolutePath();
            if (!file.getAbsolutePath().equals(lastFilePath)) {
                return super.push(file);
            }

            throw new IllegalArgumentException("Cannot add duplicates sequentially");
        }
    };

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
     * Generates a window adapter for a get file frame to set the chosen file to empty on frame disposal
     * if no file is currently set.
     *
     * @param setOnFileChosen the set file atomic reference
     * @return the window adapter
     */
    @ForReadability
    private static WindowAdapter generateGetFileWindowAdapter(AtomicReference<File> setOnFileChosen) {
        return new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                File ref = setOnFileChosen.get();
                if (ref == null || StringUtil.isNullOrEmpty(ref.getName())) {
                    setOnFileChosen.set(NULL_FILE);
                }
            }
        };
    }

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
        referenceInitFrame.addWindowListener(generateGetFileWindowAdapter(setOnFileChosen));
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

                CyderTextField dirField = new CyderTextField(0);
                dirFieldRef.set(dirField);
                if (!StringUtil.isNullOrEmpty(builder.getFieldTooltip())) {
                    dirField.setToolTipText(builder.getFieldTooltip());
                }

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
                    directoryFrame.addWindowListener(generateEnableComponentOnWindowCloseWindowAdapter(relativeTo));
                }

                directoryFrame.setLocationRelativeTo(relativeTo);
                directoryFrame.setVisible(true);

                loadInitialGetFileFiles(builder, directoryFrame, mainComponentWidth,
                        mainComponentHeight, darkMode, tempLabel);
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
    private void loadInitialGetFileFiles(Builder builder, CyderFrame directoryFrame,
                                         int mainComponentWidth, int mainComponentHeight,
                                         boolean darkMode, JLabel tempLabel) {
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

    @ForReadability
    private String getGetFileThreadName(Builder builder) {
        return "GetFile Waiter Thread, title = \"" + builder.getTitle() + "\"";
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
            boolean notLastThingPushed = !backward.peek().equals(currentDirectory);
            if (backward.isEmpty() || notLastThingPushed) {
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
