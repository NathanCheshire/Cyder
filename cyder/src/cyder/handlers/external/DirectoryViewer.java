package cyder.handlers.external;

import com.google.common.base.Preconditions;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.ui.button.CyderButton;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.pane.CyderScrollList;
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
public final class DirectoryViewer {
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

        dirFrame = new CyderFrame(630, 510, CyderColors.regularBackgroundColor);
        dirFrame.setTitle(currentDirectory.getName());

        dirField = new CyderTextField();
        dirField.setBackground(Color.white);
        dirField.setForeground(CyderColors.navy);
        dirField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        dirField.setText(currentDirectory.getAbsolutePath());
        dirField.addActionListener(e -> {
            File ChosenDir = new File(dirField.getText());
            if (ChosenDir.isDirectory()) {
                refreshOnDirectory(ChosenDir, true);
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
                refreshOnDirectory(currentDirectory, false);
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
                refreshOnDirectory(currentDirectory, false);
            }
        });
        next.setBounds(620 - 50, 40, 40, 40);
        dirFrame.getContentPane().add(next);

        JLabel tempLabel = new JLabel();
        tempLabel.setText("<html><div align=\"center\">Loading files...</div></html>");
        tempLabel.setHorizontalAlignment(JLabel.CENTER);
        tempLabel.setVerticalAlignment(JLabel.CENTER);
        tempLabel.setFont(CyderFonts.DEFAULT_FONT);
        tempLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        tempLabel.setOpaque(false);
        tempLabel.setBounds(10, 90, SCROLL_WIDTH, SCROLL_HEIGHT);
        dirFrame.getContentPane().add(tempLabel);

        dirFrame.finalizeAndShow();
        dirField.requestFocus();

        refreshOnDirectory(initialDirectory, false);
    }

    /**
     * Refreshes the files scroll list based on the contents of the provided directory.
     * If a file is provided, the parent is used to refresh on.
     *
     * @param directoryOrFile the starting directory.
     * @param wipeForward     whether to wipe the forward directory
     */
    private static void refreshOnDirectory(File directoryOrFile, boolean wipeForward) {
        Preconditions.checkNotNull(directoryOrFile);
        Preconditions.checkArgument(directoryOrFile.exists());

        if (directoryOrFile.isFile()) {
            directoryOrFile = directoryOrFile.getParentFile();
        }

        currentDirectory = directoryOrFile;

        if (backward.isEmpty()) {
            backward.push(directoryOrFile);
        } else {
            if (!backward.pop().getAbsolutePath().equals(directoryOrFile.getAbsolutePath())) {
                backward.push(directoryOrFile);
            }
        }

        cyderScrollList.removeAllElements();
        dirFrame.remove(dirScrollLabel);

        currentFiles.clear();
        currentFileNames.clear();

        File[] localDirectoryFiles = currentDirectory.listFiles();
        if (localDirectoryFiles != null && localDirectoryFiles.length > 0) {
            Collections.addAll(currentFiles, localDirectoryFiles);
        }
        currentFiles.forEach(file -> currentFileNames.add(file.getName()));

        cyderScrollList = new CyderScrollList(SCROLL_WIDTH, SCROLL_HEIGHT, CyderScrollList.SelectionPolicy.SINGLE);
        cyderScrollList.setScrollFont(CyderFonts.SEGOE_20.deriveFont(16f));

        for (int i = 0 ; i < currentFileNames.size() ; i++) {
            int eye = i;

            cyderScrollList.addElementWithDoubleClickAction(currentFileNames.get(i), () -> {
                if (currentFiles.get(eye).isDirectory()) {
                    refreshOnDirectory(currentFiles.get(eye), true);
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
