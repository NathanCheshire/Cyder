package cyder.utilities;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.enums.LoggerTag;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.utilities.objects.BoundsString;
import cyder.utilities.objects.GetterBuilder;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A getter utility for getting strings, confirmations, files, etc. from the user.
 */
public class GetterUtil {
    /**
     * To obtain an instance, use {@link GetterUtil#getInstance()}.
     */
    private GetterUtil() {
        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    /**
     * Returns a GetterUtil instance.
     *
     * @return a GetterUtil instance
     */
    public static GetterUtil getInstance() {
        return new GetterUtil();
    }

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
     * The lefta nd right padding for a string popup.
     */
    private static final int getStringXPadding = 40;

    /**
     * Custom getString() method, see usage below for how to
     *  setup so that the calling thread is not blocked.
     *
     * USAGE:
     *  <pre>
     *  {@code
     *  CyderThreadRunner.submit(() -> {
     *      try {
     *          String input = GetterUtil().getInstance().getString(getterBuilder);
     *          //other operations using input
     *      } catch (Exception e) {
     *          ErrorHandler.handle(e);
     *      }
     *  }, "THREAD_NAME").start();
     *  }
     *  </pre>
     *
     * @param builder the builder pattern to use
     * @return the user entered input string. NOTE: if any improper
     * input is ateempted to be returned, this function returns
     * the string literal of "NULL" instead of {@code null}
     */
    public String getString(GetterBuilder builder) {
        AtomicReference<String> returnString = new AtomicReference<>();

        CyderThreadRunner.submit(() -> {
            try {
                int height = GET_STRING_MIN_HEIGHT;
                int width = GET_STRING_MIN_WIDTH;

                BoundsString bounds = null;

                if (!StringUtil.isNull(builder.getLabelText())) {
                    bounds = BoundsUtil.widthHeightCalculation(builder.getLabelText(),
                            CyderFonts.defaultFont, GET_STRING_MIN_WIDTH);

                    height += bounds.getHeight() + 2 * getStringYPadding;
                    width = bounds.getWidth() + 2 * getStringXPadding;
                    builder.setLabelText(bounds.getText());
                }

                CyderFrame inputFrame = new CyderFrame(width,
                        height, CyderIcons.defaultBackground);
                inputFrame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);
                inputFrame.setTitle(builder.getTitle());

                int yOff = CyderDragLabel.DEFAULT_HEIGHT + getStringYPadding;

                if (bounds != null) {
                    CyderLabel textLabel = new CyderLabel(builder.getLabelText());
                    textLabel.setBounds(getStringXPadding, yOff, bounds.getWidth(), bounds.getHeight());
                    inputFrame.getContentPane().add(textLabel);
                }

                yOff += getStringYPadding + bounds.getHeight();

                CyderTextField inputField = new CyderTextField(0);
                inputField.setHorizontalAlignment(JTextField.CENTER);
                inputField.setBackground(Color.white);

                if (!StringUtil.isNull(builder.getInitialString())) {
                    inputField.setText(builder.getInitialString());
                }

                if (!StringUtil.isNull(builder.getFieldTooltip())) {
                    inputField.setToolTipText(builder.getFieldTooltip());
                }

                inputField.setBounds(getStringXPadding, yOff,
                        width - 2 * getStringXPadding,40);
                inputFrame.getContentPane().add(inputField);

                yOff += getStringYPadding + 40;

                CyderButton submit = new CyderButton(builder.getSubmitButtonText());
                submit.setBackground(builder.getSubmitButtonColor());
                inputField.addActionListener(e1 -> {
                    returnString.set((inputField.getText() == null || inputField.getText().isEmpty() ?
                            "NULL" : inputField.getText()));
                    inputFrame.dispose();
                });
                submit.setBorder(new LineBorder(CyderColors.navy,5,false));
                submit.setFont(CyderFonts.segoe20);
                submit.setForeground(CyderColors.navy);
                submit.addActionListener(e12 -> {
                    returnString.set((inputField.getText() == null || inputField.getText().isEmpty() ?
                            "NULL" : inputField.getText()));
                    inputFrame.dispose();
                });
                submit.setBounds(getStringXPadding, yOff,
                        width - 2 * getStringXPadding,40);
                inputFrame.getContentPane().add(submit);

                inputFrame.addPreCloseAction(() -> {
                    returnString.set((inputField.getText() == null || inputField.getText().isEmpty() ?
                            "NULL" : inputField.getText()));
                });

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

                inputFrame.setAlwaysOnTop(true);
                inputFrame.setLocationRelativeTo(relativeTo);
                inputFrame.setVisible(true);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "getString() thread, title = [" + builder.getTitle() + "]");

        try {
            while (returnString.get() == null) {
                Thread.onSpinWait();
            }
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        return returnString.get();
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
    private final AtomicReference<CyderFrame> dirFrameAtomicRef = new AtomicReference<>();

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
     * Custom getFile method, see usage below for how to setup so that the program doesn't
     * spin wait on the main GUI thread forever. Ignoring the below setup
     * instructions will make the application spin wait possibly forever.
     *
     * USAGE:
     * <pre>
     * {@code
     *   CyderThreadRunner.submit(() -> {
     *         try {
     *             File input = GetterUtil().getInstance().getFile(getterBuilder);
     *             //other operations using input
     *         } catch (Exception e) {
     *             ErrorHandler.handle(e);
     *         }
     *  }, THREAD_NAME).start();
     * }
     * </pre>
     * @param builder the builder to use for the required params
     * @return the user-chosen file
     */
    public File getFile(GetterBuilder builder) {
        boolean darkMode = UserUtil.getCyderUser().getDarkmode().equals("1");
        dirFrameAtomicRef.set(new CyderFrame(630,510, darkMode
                ? CyderColors.darkModeBackgroundColor : CyderColors.regularBackgroundColor));

        CyderThreadRunner.submit(() -> {
            try {
                //reset needed vars in case an instance was already ran
                backward.clear();
                forward.clear();
                directoryFileList.clear();
                directoryNameList.clear();

                CyderFrame refFrame = dirFrameAtomicRef.get();
                refFrame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);

                // tmp title for case of adding to taskbar before pwd is known
                refFrame.setTitle("File getter");

                dirFieldRef.set(new CyderTextField(0));
                if (!StringUtil.isNull(builder.getFieldTooltip()))
                    dirFieldRef.get().setToolTipText(builder.getFieldTooltip());
                dirFieldRef.get().setBackground(darkMode ? CyderColors.darkModeBackgroundColor : Color.white);
                dirFieldRef.get().setForeground(darkMode ? CyderColors.defaultDarkModeTextColor : CyderColors.navy);
                dirFieldRef.get().setBorder(new LineBorder(darkMode ? CyderColors.defaultDarkModeTextColor
                        : CyderColors.navy, 5, false));
                dirFieldRef.get().addActionListener(e -> {
                    File ChosenDir = new File(dirFieldRef.get().getText());

                    if (ChosenDir.isDirectory()) {
                        refreshBasedOnDir(ChosenDir, true);
                    } else if (ChosenDir.isFile()) {
                        setOnFileChosen.set(ChosenDir);
                    }
                });
                dirFieldRef.get().setBounds(60,40,500,40);
                refFrame.getContentPane().add(dirFieldRef.get());
                dirFieldRef.get().setEnabled(false);

                last = new CyderButton(" < ");
                last.setFocusPainted(false);
                last.setForeground(CyderColors.navy);
                last.setBackground(CyderColors.regularRed);
                last.setFont(CyderFonts.segoe20);
                last.setBorder(new LineBorder(CyderColors.navy,5,false));
                last.addActionListener(e -> {
                    //we may only go back if there's something in the back and it's different from where we are now
                    if (backward != null && !backward.isEmpty() && !backward.peek().equals(currentDirectory)) {
                        //traversing so push where we are to forward
                        forward.push(currentDirectory);

                        //get where we're going
                        currentDirectory = backward.pop();

                        //now simply refresh based on currentDir
                        refreshBasedOnDir(currentDirectory, false);
                    }
                });
                last.setBounds(10,40,40,40);
                refFrame.getContentPane().add(last);
                last.setEnabled(false);

                next = new CyderButton(" > ");
                next.setFocusPainted(false);
                next.setForeground(CyderColors.navy);
                next.setBackground(CyderColors.regularRed);
                next.setFont(CyderFonts.segoe20);
                next.setBorder(new LineBorder(CyderColors.navy,5,false));
                next.addActionListener(e -> {
                    //only traverse forward if the stack is not empty and forward is different from where we are
                    if (forward != null && !forward.isEmpty() && !forward.peek().equals(currentDirectory)) {
                        //push where we are
                        backward.push(currentDirectory);

                        //figure out where we need to go
                        currentDirectory = forward.pop();

                        //refresh based on where we should go
                        refreshBasedOnDir(currentDirectory, false);
                    }
                });
                next.setBounds(620 - 50,40,40, 40);
                refFrame.getContentPane().add(next);
                next.setEnabled(false);

                // label to show where files will be
                JLabel tempLabel = new JLabel();
                tempLabel.setBorder(new LineBorder(darkMode ? CyderColors.defaultDarkModeTextColor
                        : CyderColors.navy, 5, false));
                tempLabel.setOpaque(false);
                tempLabel.setBounds(10,90,600, 400);
                refFrame.getContentPane().add(tempLabel);

                Component relativeTo = builder.getRelativeTo();

                if (relativeTo != null && builder.isDisableRelativeTo()) {
                    relativeTo.setEnabled(false);
                    refFrame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            relativeTo.setEnabled(true);
                        }
                    });
                }

                refFrame.setLocationRelativeTo(relativeTo);
                refFrame.setVisible(true);

                refFrame.notify("Loading files...");

                // load possibly intense stuff on separate thread
                CyderThreadRunner.submit(() -> {
                    // init current directory
                    if (!StringUtil.isNull(builder.getInitialString())
                            && new File(builder.getInitialString()).exists()) {
                        currentDirectory = new File(builder.getInitialString());
                    } else {
                        currentDirectory = new File(OSUtil.USER_DIR);
                    }

                    refFrame.setTitle(currentDirectory.getName());

                    Collections.addAll(directoryFileList, currentDirectory.listFiles());

                    for (File file : directoryFileList) {
                        directoryNameList.add(file.getName());
                    }

                    cyderScrollListRef.set(new CyderScrollList(600, 400,
                            CyderScrollList.SelectionPolicy.SINGLE, darkMode));
                    cyderScrollListRef.get().setScrollFont(CyderFonts.segoe20.deriveFont(16f));

                    //adding things to the list and setting up actions for what to do when an element is clicked
                    for (int i = 0 ; i < directoryNameList.size() ; i++) {
                        int finalI = i;
                        cyderScrollListRef.get().addElement(directoryNameList.get(i), () -> {
                            if (directoryFileList.get(finalI).isDirectory()) {
                                refreshBasedOnDir(directoryFileList.get(finalI), false);
                            } else {
                                setOnFileChosen.set(directoryFileList.get(finalI));
                            }
                        });
                    }

                    dirScrollLabel = cyderScrollListRef.get().generateScrollList();
                    dirScrollLabel.setBounds(10,90,600, 400);
                    refFrame.getContentPane().add(dirScrollLabel);

                    next.setEnabled(true);
                    last.setEnabled(true);

                    dirFieldRef.get().setText(currentDirectory.getAbsolutePath());
                    dirFieldRef.get().setEnabled(true);
                    dirFieldRef.get().requestFocus();

                    refFrame.revokeAllNotifications();
                }, "File Getter Loader");
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, " getFile() thread, title = [" + builder.getTitle() + "]");

        try {
            while (setOnFileChosen.get() == null) {
                Thread.onSpinWait();
            }
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        } finally {
            dirFrameAtomicRef.get().dispose();
        }

        return setOnFileChosen.get().getName().equals("NULL") ? null : setOnFileChosen.get();
    }

    /**
     * Refreshes the current file list based on the provided file.
     *
     * @param directory the directory/file to refresh on
     * @param wipeForward whether to clear the forward traversal stack
     */
    private void refreshBasedOnDir(File directory, boolean wipeForward) {
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
        dirFrameAtomicRef.get().remove(dirScrollLabel);

        // if given a file, use its parent
        if (directory.isFile()) {
            directory = directory.getParentFile();
        }

        // set to new directory
        currentDirectory = directory;

        // wipe lists
        directoryNameList.clear();
        directoryFileList.clear();

        // init new files list
        Collections.addAll(directoryFileList, currentDirectory.listFiles());

        // regenerate names list
        for (File file : directoryFileList) {
            directoryNameList.add(file.getName());
        }

        // remake scroll list object
        cyderScrollListRef.set(new CyderScrollList(600, 400,
                CyderScrollList.SelectionPolicy.SINGLE, cyderScrollListRef.get().isDarkMode()));
        cyderScrollListRef.get().setScrollFont(CyderFonts.segoe20.deriveFont(16f));

        // generate clickable components to add to the list
        for (int i = 0; i < directoryNameList.size() ; i++) {
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
        dirScrollLabel.setBounds(10,90,600, 400);
        dirFrameAtomicRef.get().getContentPane().add(dirScrollLabel);

        // revalidate, set title, set pwd text
        dirFrameAtomicRef.get().revalidate();
        dirFrameAtomicRef.get().repaint();
        dirFrameAtomicRef.get().setTitle(currentDirectory.getName());

        dirFieldRef.get().setText(currentDirectory.getAbsolutePath());
    }

    /**
     * Custom getInput() method, see usage below for how to
     *  setup so that the calling thread is not blocked.
     *
     * USAGE:
     *  <pre>
     *  {@code
     *  CyderThreadRunner.submit(() -> {
     *      try {
     *          String input = GetterUtil().getInstance().getConfirmation(getterBuilder);
     *          //other operations using input
     *      } catch (Exception e) {
     *          ErrorHandler.handle(e);
     *      }
     *  }, "THREAD_NAME").start();
     *  }
     *  </pre>
     *
     * @param builder the builder pattern to use
     * @return whether the user confirmed the operation
     */
    public boolean getConfirmation(GetterBuilder builder) {
        AtomicReference<Boolean> ret = new AtomicReference<>();
        ret.set(null);
        AtomicReference<CyderFrame> frameReference = new AtomicReference<>();

        CyderThreadRunner.submit(() -> {
            try {
                CyderLabel textLabel = new CyderLabel();

                BoundsString bs = BoundsUtil.widthHeightCalculation(builder.getInitialString(), textLabel.getFont());
                int w = bs.getWidth();
                int h = bs.getHeight();
                textLabel.setText(bs.getText());

                CyderFrame frame = new CyderFrame(w + 40,
                        h + 25 + 20 + 40 + 40, CyderIcons.defaultBackgroundLarge);
                frameReference.set(frame);
                frame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);
                frame.setTitle(builder.getTitle());
                frame.addPreCloseAction(() -> {
                    if (ret.get() != Boolean.TRUE) {
                        ret.set(Boolean.FALSE);
                    }
                });

                textLabel.setBounds(10,35, w, h);
                frame.getContentPane().add(textLabel);

                //accounting for offset above
                w += 40;

                CyderButton yes = new CyderButton(builder.getYesButtonText());
                yes.setColors(builder.getSubmitButtonColor());
                yes.addActionListener(e -> ret.set(Boolean.TRUE));
                yes.setBounds(20,35 + h + 20, (w - 60) / 2, 40);
                frame.getContentPane().add(yes);

                CyderButton no = new CyderButton(builder.getNoButtonText());
                no.setColors(builder.getSubmitButtonColor());
                no.addActionListener(e -> ret.set(Boolean.FALSE));
                no.setBounds(20 + 20 + ((w - 60) / 2),35 + h + 20, (w - 60) / 2, 40);
                frame.getContentPane().add(no);

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
        }, " getConfirmation() thread, title = [" + builder.getTitle() + "]");

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
}
