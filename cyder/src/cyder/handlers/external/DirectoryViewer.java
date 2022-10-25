package cyder.handlers.external;

import com.google.common.base.Preconditions;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.threads.CyderThreadRunner;
import cyder.ui.button.CyderButton;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.pane.CyderScrollList;
import cyder.user.UserUtil;
import cyder.utils.IoUtil;
import cyder.utils.OsUtil;
import cyder.utils.UiUtil;

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
    private static File currentDirectory = new File(OsUtil.USER_DIR);

    /**
     * Suppress default constructor.
     */
    private DirectoryViewer() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Widget(triggers = "dir", description = "A directory navigator widget")
    public static void showGui() {
        showGui(new File(OsUtil.USER_DIR));
    }

    /**
     * The width of the scroll view.
     */
    private static final int SCROLL_WIDTH = 600;

    /**
     * The height of the scroll view.
     */
    private static final int SCROLL_HEIGHT = 400;

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

        UiUtil.closeIfOpen(dirFrame);

        boolean darkMode = UserUtil.getCyderUser().getDarkmode().equals("1");

        dirFrame = new CyderFrame(630, 510, darkMode
                ? CyderColors.darkModeBackgroundColor : CyderColors.regularBackgroundColor);
        dirFrame.setTitle(currentDirectory.getName());

        dirField = new CyderTextField();
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
                IoUtil.openFile(ChosenDir.getAbsolutePath());
            }
        });
        dirField.setBounds(60, 40, 500, 40);
        dirFrame.getContentPane().add(dirField);

        CyderButton last = new CyderButton(" < ");
        last.setFocusPainted(false);
        last.setForeground(CyderColors.navy);
        last.setBackground(CyderColors.regularRed);
        last.setFont(CyderFonts.SEGOE_20);
        last.setBorder(new LineBorder(CyderColors.navy, 5, false));
        last.addActionListener(e -> {
            //we may only go back if there's something in the back, and it's different from where we are now
            if (!backward.isEmpty() && !backward.peek().equals(currentDirectory)) {
                // Traversing so push where we are to forward
                forward.push(currentDirectory);
                // Get where we're going
                currentDirectory = backward.pop();
                // Now simply refresh based on currentDir
                refreshBasedOnDir(currentDirectory, false);
            }
        });
        last.setBounds(10, 40, 40, 40);
        dirFrame.getContentPane().add(last);

        CyderButton next = new CyderButton(" > ");
        next.setFocusPainted(false);
        next.setForeground(CyderColors.navy);
        next.setBackground(CyderColors.regularRed);
        next.setFont(CyderFonts.SEGOE_20);
        next.setBorder(new LineBorder(CyderColors.navy, 5, false));
        next.addActionListener(e -> {
            // Only traverse forward if the stack is not empty and forward is different from where we are
            if (!forward.isEmpty() && !forward.peek().equals(currentDirectory)) {
                // Push where we are
                backward.push(currentDirectory);
                // Figure out where we need to go
                currentDirectory = forward.pop();
                // Refresh based on where we should go
                refreshBasedOnDir(currentDirectory, false);
            }
        });
        next.setBounds(620 - 50, 40, 40, 40);
        dirFrame.getContentPane().add(next);

        JLabel tempLabel = new JLabel();
        tempLabel.setText("<html><div align=\"center\">Loading files...</div></html>");
        tempLabel.setHorizontalAlignment(JLabel.CENTER);
        tempLabel.setVerticalAlignment(JLabel.CENTER);
        tempLabel.setFont(CyderFonts.DEFAULT_FONT);
        tempLabel.setBorder(new LineBorder(darkMode ? CyderColors.defaultDarkModeTextColor
                : CyderColors.navy, 5, false));
        tempLabel.setOpaque(false);
        tempLabel.setBounds(10, 90, SCROLL_WIDTH, SCROLL_HEIGHT);
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

            cyderScrollList = new CyderScrollList(SCROLL_WIDTH, SCROLL_HEIGHT,
                    CyderScrollList.SelectionPolicy.SINGLE, darkMode);
            cyderScrollList.setScrollFont(CyderFonts.SEGOE_20.deriveFont(16f));
            cyderScrollList.removeAllElements();

            for (int i = 0 ; i < currentFileNames.size() ; i++) {
                int finalI = i;
                cyderScrollList.addElementWithDoubleClickAction(currentFileNames.get(i), () -> {
                    if (currentFiles.get(finalI).isDirectory()) {
                        refreshBasedOnDir(currentFiles.get(finalI), true);
                    } else {
                        IoUtil.openFile(currentFiles.get(finalI).getAbsolutePath());
                    }
                });
            }

            dirScrollLabel = cyderScrollList.generateScrollList();
            dirScrollLabel.setBounds(10, 90, SCROLL_WIDTH, SCROLL_HEIGHT);
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
        if (wipeForward) {
            forward.clear();

            // If not last thing pushed
            if (backward.isEmpty() || !backward.peek().equals(currentDirectory)) {
                backward.push(currentDirectory);
            }
        }

        cyderScrollList.removeAllElements();
        dirFrame.remove(dirScrollLabel);

        if (directory.isFile()) {
            directory = directory.getParentFile();
        }

        currentDirectory = directory;

        currentFiles.clear();
        currentFileNames.clear();

        File[] currentDirFiles = currentDirectory.listFiles();

        if (currentDirFiles != null && currentDirFiles.length > 0) {
            Collections.addAll(currentFiles, currentDirFiles);
        }

        // Regenerate names list
        for (File file : currentFiles) {
            currentFileNames.add(file.getName());
        }

        // Remake scroll list object
        cyderScrollList = new CyderScrollList(SCROLL_WIDTH, SCROLL_HEIGHT,
                CyderScrollList.SelectionPolicy.SINGLE, cyderScrollList.isDarkMode());
        cyderScrollList.setScrollFont(CyderFonts.SEGOE_20.deriveFont(16f));

        for (int i = 0 ; i < currentFileNames.size() ; i++) {
            int eye = i;

            cyderScrollList.addElementWithDoubleClickAction(currentFileNames.get(i), () -> {
                if (currentFiles.get(eye).isDirectory()) {
                    refreshBasedOnDir(currentFiles.get(eye), true);
                } else {
                    IoUtil.openFile(currentFiles.get(eye).getAbsolutePath());
                }
            });
        }
        dirScrollLabel = cyderScrollList.generateScrollList();
        dirScrollLabel.setBounds(10, 90, SCROLL_WIDTH, SCROLL_HEIGHT);
        dirFrame.getContentPane().add(dirScrollLabel);

        dirFrame.revalidate();
        dirFrame.repaint();
        dirFrame.setTitle(currentDirectory.getName());
        dirField.setText(currentDirectory.getAbsolutePath());
    }
}
