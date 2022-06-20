package cyder.handlers.external;

import com.google.common.base.Preconditions;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollList;
import cyder.ui.CyderTextField;
import cyder.utils.IOUtil;
import cyder.utils.OSUtil;
import cyder.utils.UserUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;

/**
 * A directory navigation widget.
 */
public class DirectoryViewer {
    /**
     * The frame for the directory widget.
     */
    private static CyderFrame dirFrame;

    /**
     * The field to display the current directory and to allow manual paths to be entered.
     */
    private static CyderTextField dirField;

    /**
     * The scroll list component to display the current files
     */
    private static CyderScrollList cyderScrollList;

    /**
     * The directory scroll label. Needed to allow removal when files are changed.
     */
    private static JLabel dirScrollLabel;

    /**
     * The names of the current files.
     */
    private static final LinkedList<String> currentFileNames = new LinkedList<>();

    /**
     * The current files.
     */
    private static final LinkedList<File> currentFiles = new LinkedList<>();

    /**
     * Stack to traverse backwards through history of viewed directories.
     */
    private static final Stack<File> backward = new Stack<>();

    /**
     * Stack to traverse forward through history of viewed directories.
     */
    private static final Stack<File> forward = new Stack<>();

    /**
     * The current location of the directory widget.
     */
    private static File currentDirectory = new File(OSUtil.USER_DIR);

    /**
     * Restrict default constructor.
     */
    private DirectoryViewer() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Widget(triggers = "dir", description = "A directory navigator widget")
    public static void showGui() {
        showGui(new File(OSUtil.USER_DIR));
    }

    /**
     * Starts the directory viewer in the provided initial directory.
     *
     * @param initialDirectory the initial directory to start in
     */
    public static void showGui(File initialDirectory) {
        Preconditions.checkNotNull(initialDirectory);
        Preconditions.checkArgument(initialDirectory.exists());

        if (initialDirectory.isFile()) {
            initialDirectory = initialDirectory.getParentFile();
        }

        if (dirFrame != null) {
            dirFrame.dispose();
        }

        boolean darkMode = UserUtil.getCyderUser().getDarkmode().equals("1");

        dirFrame = new CyderFrame(630, 510, darkMode
                ? CyderColors.darkModeBackgroundColor : CyderColors.regularBackgroundColor);
        dirFrame.setTitle(currentDirectory.getName());

        dirField = new CyderTextField(0);
        dirField.setBackground(darkMode ? CyderColors.darkModeBackgroundColor : Color.white);
        dirField.setForeground(darkMode ? CyderColors.defaultDarkModeTextColor : CyderColors.navy);
        dirField.setBorder(new LineBorder(darkMode ? CyderColors.defaultDarkModeTextColor
                : CyderColors.navy, 5, false));
        dirField.setText(currentDirectory.getAbsolutePath());
        dirField.addActionListener(e -> {
            File ChosenDir = new File(dirField.getText());

            if (ChosenDir.isDirectory()) {
                refreshBasedOnDir(ChosenDir, true);
            } else if (ChosenDir.isFile()) {
                IOUtil.openFile(ChosenDir.getAbsolutePath());
            }
        });
        dirField.setBounds(60, 40, 500, 40);
        dirFrame.getContentPane().add(dirField);

        CyderButton last = new CyderButton(" < ");
        last.setFocusPainted(false);
        last.setForeground(CyderColors.navy);
        last.setBackground(CyderColors.regularRed);
        last.setFont(CyderFonts.segoe20);
        last.setBorder(new LineBorder(CyderColors.navy, 5, false));
        last.addActionListener(e -> {
            //we may only go back if there's something in the back, and it's different from where we are now
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
        dirFrame.getContentPane().add(last);

        CyderButton next = new CyderButton(" > ");
        next.setFocusPainted(false);
        next.setForeground(CyderColors.navy);
        next.setBackground(CyderColors.regularRed);
        next.setFont(CyderFonts.segoe20);
        next.setBorder(new LineBorder(CyderColors.navy, 5, false));
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
        dirFrame.getContentPane().add(next);

        // label to show where files will be
        JLabel tempLabel = new JLabel();
        tempLabel.setText("<html><div align=\"center\">Loading files...</div></html>");
        tempLabel.setHorizontalAlignment(JLabel.CENTER);
        tempLabel.setVerticalAlignment(JLabel.CENTER);
        tempLabel.setFont(CyderFonts.defaultFont);
        tempLabel.setBorder(new LineBorder(darkMode ? CyderColors.defaultDarkModeTextColor
                : CyderColors.navy, 5, false));
        tempLabel.setOpaque(false);
        tempLabel.setBounds(10, 90, 600, 400);
        dirFrame.getContentPane().add(tempLabel);

        dirFrame.finalizeAndShow();
        dirField.requestFocus();

        File finalInitialDirectory = initialDirectory;
        CyderThreadRunner.submit(() -> {
            currentDirectory = finalInitialDirectory;
            File chosenDir = currentDirectory;

            currentFileNames.clear();
            currentFiles.clear();

            File[] chosenDirFiles = chosenDir.listFiles();

            if (chosenDirFiles != null && chosenDirFiles.length > 0) {
                Collections.addAll(currentFiles, chosenDirFiles);
            }

            for (File file : currentFiles) {
                currentFileNames.add(file.getName());
            }

            cyderScrollList = new CyderScrollList(600, 400,
                    CyderScrollList.SelectionPolicy.SINGLE, darkMode);
            cyderScrollList.setScrollFont(CyderFonts.segoe20.deriveFont(16f));
            cyderScrollList.removeAllElements();

            for (int i = 0 ; i < currentFileNames.size() ; i++) {
                int finalI = i;
                cyderScrollList.addElement(currentFileNames.get(i), () -> {
                    if (currentFiles.get(finalI).isDirectory()) {
                        refreshBasedOnDir(currentFiles.get(finalI), true);
                    } else {
                        IOUtil.openFile(currentFiles.get(finalI).getAbsolutePath());
                    }
                });
            }

            dirScrollLabel = cyderScrollList.generateScrollList();
            dirScrollLabel.setBounds(10, 90, 600, 400);
            dirFrame.getContentPane().add(dirScrollLabel);

            dirFrame.remove(tempLabel);

            dirFrame.revokeAllNotifications();
        }, "Directory file loader");
    }

    /**
     * Refreshes the current file list based on the provided file.
     *
     * @param directory   the directory/file to refresh on
     * @param wipeForward whether to clear the forward traversal stack
     */
    private static void refreshBasedOnDir(File directory, boolean wipeForward) {
        // clear the forward list
        if (wipeForward) {
            forward.clear();

            // if not last thing pushed
            if (backward.isEmpty() || !backward.peek().equals(currentDirectory)) {
                backward.push(currentDirectory);
            }
        }

        // remove old scroll
        cyderScrollList.removeAllElements();
        dirFrame.remove(dirScrollLabel);

        // if given a file, use its parent
        if (directory.isFile()) {
            directory = directory.getParentFile();
        }

        // set to new directory
        currentDirectory = directory;

        // wipe lists
        currentFiles.clear();
        currentFileNames.clear();

        File[] currentDirFiles = currentDirectory.listFiles();

        if (currentDirFiles != null && currentDirFiles.length > 0) {
            Collections.addAll(currentFiles, currentDirFiles);
        }

        // regenerate names list
        for (File file : currentFiles) {
            currentFileNames.add(file.getName());
        }

        // remake scroll list object
        cyderScrollList = new CyderScrollList(600, 400,
                CyderScrollList.SelectionPolicy.SINGLE, cyderScrollList.isDarkMode());
        cyderScrollList.setScrollFont(CyderFonts.segoe20.deriveFont(16f));

        for (int i = 0 ; i < currentFileNames.size() ; i++) {
            int eye = i;

            cyderScrollList.addElement(currentFileNames.get(i), () -> {
                if (currentFiles.get(eye).isDirectory()) {
                    refreshBasedOnDir(currentFiles.get(eye), true);
                } else {
                    IOUtil.openFile(currentFiles.get(eye).getAbsolutePath());
                }
            });
        }
        dirScrollLabel = cyderScrollList.generateScrollList();
        dirScrollLabel.setBounds(10, 90, 600, 400);
        dirFrame.getContentPane().add(dirScrollLabel);

        dirFrame.revalidate();
        dirFrame.repaint();
        dirFrame.setTitle(currentDirectory.getName());
        dirField.setText(currentDirectory.getAbsolutePath());
    }
}
