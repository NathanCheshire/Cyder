package cyder.handlers.external;

import cyder.annotations.Widget;
import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderIcons;
import cyder.consts.CyderStrings;
import cyder.genesis.GenesisShare;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollList;
import cyder.ui.CyderTextField;
import cyder.utilities.IOUtil;
import cyder.utilities.SystemUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;

public class DirectoryViewer {
    //all ui elements
    private static CyderFrame dirFrame;
    private static CyderTextField dirField;
    private static CyderScrollList cyderScrollList;
    private static JLabel dirScrollLabel;
    private static CyderButton last;
    private static CyderButton next;

    //corresponding lists
    private static LinkedList<String> directoryNameList = new LinkedList<>();
    private static LinkedList<File> directoryFileList = new LinkedList<>();

    //stacks for traversal
    private static Stack<File> backward = new Stack<>();
    private static Stack<File> forward = new Stack<>();

    //where we currently are
    private static File currentDirectory = new File("c:\\users\\"
            + SystemUtil.getWindowsUsername() + "\\Downloads");

    //private constructor since static
    private DirectoryViewer() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    //as per standard, method
    @Widget(trigger = "dir", description = "A directory navigator widget")
    public static void showGUI() {
        //if a frame is already open, close it
        if (dirFrame != null)
            dirFrame.dispose();

        //frame setup
        dirFrame = new CyderFrame(630,510, CyderIcons.defaultBackground);
        dirFrame.setTitle(currentDirectory.getName());

        //field setup
        dirField = new CyderTextField(0);
        dirField.setBackground(Color.white);
        dirField.setText(currentDirectory.getAbsolutePath());
        dirField.addActionListener(e -> {
            File ChosenDir = new File(dirField.getText());

            if (ChosenDir.isDirectory()) {
                refreshBasedOnDir(ChosenDir, true);
            } else if (ChosenDir.isFile()) {
                IOUtil.openFile(ChosenDir.getAbsolutePath());
            }
        });
        dirField.setBounds(60,40,500,40);
        dirFrame.getContentPane().add(dirField);

        //last setup
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
                refreshBasedOnDir(null, false);
            }
        });
        last.setBounds(10,40,40,40);
        dirFrame.getContentPane().add(last);

        //next setup
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
                refreshBasedOnDir(null, false);
            }
        });
        next.setBounds(620 - 50,40,40, 40);
        dirFrame.getContentPane().add(next);

        File chosenDir = new File("c:/users/"
                + SystemUtil.getWindowsUsername() + "/");
        File[] startDir = chosenDir.listFiles();

        Collections.addAll(directoryFileList, startDir);

        for (File file : directoryFileList) {
            directoryNameList.add(file.getName());
        }

        //files scroll list setup
        cyderScrollList = new CyderScrollList(600, 400, CyderScrollList.SelectionPolicy.SINGLE);
        cyderScrollList.setScrollFont(CyderFonts.segoe20.deriveFont(16f));

        //adding things to the list and setting up actions for what to do when an element is clicked
        for (int i = 0 ; i < directoryNameList.size() ; i++) {
            int finalI = i;
            class thisAction implements CyderScrollList.ScrollAction {
                @Override
                public void fire() {
                    if (directoryFileList.get(finalI).isDirectory()) {
                        refreshBasedOnDir(directoryFileList.get(finalI), true);
                    } else {
                        IOUtil.openFile(directoryFileList.get(finalI).getAbsolutePath());
                    }
                }
            }

            thisAction action = new thisAction();
            cyderScrollList.addElement(directoryNameList.get(i), action);
        }

        //generate the scroll label
        dirScrollLabel = cyderScrollList.generateScrollList();
        dirScrollLabel.setBounds(10,90,600, 400);
        dirFrame.getContentPane().add(dirScrollLabel);

        //final frame setup
        dirFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
        dirFrame.setVisible(true);
        dirField.requestFocus();
    }

    private static void refreshBasedOnDir(File directory, boolean wipeForward) {
        if (wipeForward) {
            forward.clear();
            if (backward.isEmpty() || !backward.peek().equals(currentDirectory)) {
                backward.push(currentDirectory);
            }
            currentDirectory = directory;
        }

        File[] files = currentDirectory.listFiles();
        cyderScrollList.removeAllElements();
        dirFrame.remove(dirScrollLabel);
        directoryFileList.clear();
        directoryNameList.clear();
        Collections.addAll(directoryFileList, files);
        for (File file : directoryFileList) {
            directoryNameList.add(file.getName());
        }
        cyderScrollList = new CyderScrollList(600, 400, CyderScrollList.SelectionPolicy.SINGLE);
        cyderScrollList.setScrollFont(CyderFonts.segoe20.deriveFont(16f));
        for (int i = 0 ; i < directoryNameList.size() ; i++) {
            int finalI = i;
            class thisAction implements CyderScrollList.ScrollAction {
                @Override
                public void fire() {
                    if (directoryFileList.get(finalI).isDirectory()) {
                        refreshBasedOnDir(directoryFileList.get(finalI), true);
                    } else {
                        IOUtil.openFile(directoryFileList.get(finalI).getAbsolutePath());
                    }
                }
            }
            thisAction action = new thisAction();
            cyderScrollList.addElement(directoryNameList.get(i), action);
        }
        dirScrollLabel = cyderScrollList.generateScrollList();
        dirScrollLabel.setBounds(10,90,600, 400);
        dirFrame.getContentPane().add(dirScrollLabel);
        dirFrame.revalidate();
        dirFrame.repaint();
        dirFrame.setTitle(currentDirectory.getName());
        dirField.setText(currentDirectory.getAbsolutePath());
    }
}
