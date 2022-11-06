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
    private static CyderFrame directoryFrame;

    /**
     * The field to display the current directory and to allow manual paths to be entered.
     */
    private static CyderTextField directoryField;

    /**
     * The directory scroll label. Needed to allow removal when files are changed.
     */
    private static JLabel dirScrollLabel = new JLabel();

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
    private static File currentDirectory;

    /**
     * The width of the scroll view.
     */
    private static final int SCROLL_WIDTH = 600;

    /**
     * The height of the scroll view.
     */
    private static final int SCROLL_HEIGHT = 400;

    /**
     * The scroll list component to display the current files
     */
    private static CyderScrollList cyderScrollList = new CyderScrollList(SCROLL_WIDTH, SCROLL_HEIGHT);

    /**
     * The loading files label.
     */
    private static final JLabel loadingFilesLabel = new JLabel();

    static {
        loadingFilesLabel.setText("<html><div align=\"center\">Loading files...</div></html>");
        loadingFilesLabel.setHorizontalAlignment(JLabel.CENTER);
        loadingFilesLabel.setVerticalAlignment(JLabel.CENTER);
        loadingFilesLabel.setFont(CyderFonts.DEFAULT_FONT);
        loadingFilesLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        loadingFilesLabel.setOpaque(false);
        loadingFilesLabel.setBounds(10, 90, SCROLL_WIDTH, SCROLL_HEIGHT);
    }

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

        currentDirectory = initialDirectory;

        UiUtil.closeIfOpen(directoryFrame);

        directoryFrame = new CyderFrame(630, 510, CyderColors.regularBackgroundColor);
        directoryFrame.setTitle(currentDirectory.getName());

        directoryField = new CyderTextField();
        directoryField.setBackground(Color.white);
        directoryField.setForeground(CyderColors.navy);
        directoryField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        directoryField.setText(currentDirectory.getAbsolutePath());
        directoryField.addActionListener(e -> {
            File chosenDir = new File(directoryField.getText());

            if (chosenDir.isDirectory()) {
                forward.clear();
                storeCurrentDirectory();
                currentDirectory = chosenDir;
                refreshFiles();
            } else if (chosenDir.isFile()) {
                IoUtil.openFile(chosenDir);
            }
        });
        directoryField.setBounds(60, 40, 500, 40);
        directoryFrame.getContentPane().add(directoryField);

        CyderButton last = new CyderButton(" < ");
        last.setFocusPainted(false);
        last.setForeground(CyderColors.navy);
        last.setBackground(CyderColors.regularRed);
        last.setFont(CyderFonts.SEGOE_20);
        last.setBorder(new LineBorder(CyderColors.navy, 5, false));
        last.addActionListener(e -> {
            if (!backward.isEmpty() && !backward.peek().equals(currentDirectory)) {
                forward.push(currentDirectory);
                currentDirectory = backward.pop();
                refreshFiles();
            }
        });
        last.setBounds(10, 40, 40, 40);
        directoryFrame.getContentPane().add(last);

        CyderButton next = new CyderButton(" > ");
        next.setFocusPainted(false);
        next.setForeground(CyderColors.navy);
        next.setBackground(CyderColors.regularRed);
        next.setFont(CyderFonts.SEGOE_20);
        next.setBorder(new LineBorder(CyderColors.navy, 5, false));
        next.addActionListener(e -> {
            if (!forward.isEmpty() && !forward.peek().equals(currentDirectory)) {
                backward.push(currentDirectory);
                storeCurrentDirectory();
                currentDirectory = forward.pop();
                refreshFiles();
            }
        });
        next.setBounds(620 - 50, 40, 40, 40);
        directoryFrame.getContentPane().add(next);

        loadingFilesLabel.setVisible(true);
        directoryFrame.getContentPane().add(loadingFilesLabel);

        directoryFrame.finalizeAndShow();
        directoryField.requestFocus();

        refreshFiles();
    }

    /**
     * Stores teh current directory as a previous location if necessary.
     */
    private static void storeCurrentDirectory() {
        if (backward.isEmpty()) {
            backward.push(currentDirectory);
        } else {
            File backwardFile = backward.peek();
            if (backwardFile != null && !backwardFile.getAbsolutePath().equals(currentDirectory.getAbsolutePath())) {
                backward.push(currentDirectory);
            }
        }
    }

    /**
     * Refreshes the files scroll list based on the current directory.
     */
    private static void refreshFiles() {
        loadingFilesLabel.setVisible(true);

        cyderScrollList.removeAllElements();
        directoryFrame.remove(dirScrollLabel);

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
            int finalI = i;
            cyderScrollList.addElementWithDoubleClickAction(currentFileNames.get(i), () -> {
                File file = currentFiles.get(finalI);

                if (file.isDirectory()) {
                    forward.clear();
                    storeCurrentDirectory();
                    currentDirectory = file;
                    refreshFiles();
                } else {
                    IoUtil.openFile(currentFiles.get(finalI).getAbsolutePath());
                }
            });
        }

        dirScrollLabel = cyderScrollList.generateScrollList();
        dirScrollLabel.setBounds(10, 90, SCROLL_WIDTH, SCROLL_HEIGHT);
        directoryFrame.getContentPane().add(dirScrollLabel);

        loadingFilesLabel.setVisible(false);

        directoryFrame.revalidate();
        directoryFrame.repaint();
        directoryFrame.setTitle(currentDirectory.getName());
        directoryField.setText(currentDirectory.getAbsolutePath());
    }
}
