package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
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

public class DirectorySearch {
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
            + SystemUtil.getWindowsUsername() + "\\");

    //private constructor since static
    private DirectorySearch() {}

    //as per standard, method
    public static void showGUI() {
        //if a frame is already open, close it
        if (dirFrame != null)
            dirFrame.dispose();

        //frame setup
        dirFrame = new CyderFrame(630,510, CyderImages.defaultBackground);
        dirFrame.setTitle(currentDirectory.getName());

        //field setup
        dirField = new CyderTextField(0);
        dirField.setBackground(Color.white);
        dirField.setText(currentDirectory.getAbsolutePath());
        dirField.addActionListener(e -> {
            File ChosenDir = new File(dirField.getText());

            if (ChosenDir.isDirectory()) {
                refreshBasedOnDir(ChosenDir);
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
        last.setFont(CyderFonts.weatherFontSmall);
        last.setBorder(new LineBorder(CyderColors.navy,5,false));
        last.setColors(CyderColors.regularRed);
        last.addActionListener(e -> {
            //we may only go back if there's something in the back and it's different from where we are now
            if (backward != null && !backward.isEmpty() && !backward.peek().equals(currentDirectory)) {
                //traversing so push where we are to forward
                forward.push(currentDirectory);

                //get where we're going
                currentDirectory = backward.pop();

                //now simply refresh based on currentDir
                refreshFromTraversalButton();
            }
        });
        last.setBounds(10,40,40,40);
        dirFrame.getContentPane().add(last);

        //next setup
        next = new CyderButton(" > ");
        next.setFocusPainted(false);
        next.setForeground(CyderColors.navy);
        next.setBackground(CyderColors.regularRed);
        next.setFont(CyderFonts.weatherFontSmall);
        next.setBorder(new LineBorder(CyderColors.navy,5,false));
        next.setColors(CyderColors.regularRed);
        next.addActionListener(e -> {
            //only traverse forward if the stack is not empty and forward is different from where we are
            if (forward != null && !forward.isEmpty() && !forward.peek().equals(currentDirectory)) {
                //push where we are
                backward.push(currentDirectory);

                //figure out where we need to go
                currentDirectory = forward.pop();

                //refresh based on where we should go
                refreshFromTraversalButton();
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
        cyderScrollList.setScrollFont(CyderFonts.weatherFontSmall.deriveFont(16f));

        //adding things to the list and setting up actions for what to do when an element is clicked
        for (int i = 0 ; i < directoryNameList.size() ; i++) {
            int finalI = i;
            class thisAction implements CyderScrollList.ScrollAction {
                @Override
                public void fire() {
                    if (directoryFileList.get(finalI).isDirectory()) {
                        refreshBasedOnDir(directoryFileList.get(finalI));
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

    //general refresh method that doesn't clear the stacks
    private static void refreshFromTraversalButton() {
        //get files
        File[] files = currentDirectory.listFiles();

        //remove old files
        cyderScrollList.removeAllElements();
        dirFrame.remove(dirScrollLabel);

        //wipe name and files lists
        directoryFileList.clear();
        directoryNameList.clear();

        //add new files arr to LL
        Collections.addAll(directoryFileList, files);

        //get corresponding names for name list
        for (File file : directoryFileList) {
            directoryNameList.add(file.getName());
        }

        //setup scroll
        cyderScrollList = new CyderScrollList(600, 400, CyderScrollList.SelectionPolicy.SINGLE);
        cyderScrollList.setScrollFont(CyderFonts.weatherFontSmall.deriveFont(16f));

        //add new items to scroll and actions
        for (int i = 0 ; i < directoryNameList.size() ; i++) {
            int finalI = i;
            class thisAction implements CyderScrollList.ScrollAction {
                @Override
                public void fire() {
                    if (directoryFileList.get(finalI).isDirectory()) {
                        refreshBasedOnDir(directoryFileList.get(finalI));
                    } else {
                        IOUtil.openFile(directoryFileList.get(finalI).getAbsolutePath());
                    }
                }
            }

            thisAction action = new thisAction();
            cyderScrollList.addElement(directoryNameList.get(i), action);
        }

        //regenerate scroll
        dirScrollLabel = cyderScrollList.generateScrollList();
        dirScrollLabel.setBounds(10,90,600, 400);
        dirFrame.getContentPane().add(dirScrollLabel);

        //frame revalidation
        dirFrame.revalidate();
        dirFrame.repaint();
        dirFrame.setTitle(currentDirectory.getName());
        dirField.setText(currentDirectory.getAbsolutePath());
    }

    //refresh button that clears the back stack
    private static void refreshBasedOnDir(File directory) {
        //clear forward since a new path
        forward.clear();

        //before where we were is wiped, put it in backwards if it's not the last
        if (backward.isEmpty() || !backward.peek().equals(currentDirectory)) {
            backward.push(currentDirectory);
        }

        //this is our current now
        currentDirectory = directory;

        //get files to display
        File[] files = directory.listFiles();

        //remove old list
        cyderScrollList.removeAllElements();
        dirFrame.remove(dirScrollLabel);

        //clear display lists
        directoryFileList.clear();
        directoryNameList.clear();

        //add array files to LL files
        Collections.addAll(directoryFileList, files);

        //add corresponding names of files to names list
        for (File file : directoryFileList) {
            directoryNameList.add(file.getName());
        }

        //regenerate scroll
        cyderScrollList = new CyderScrollList(600, 400, CyderScrollList.SelectionPolicy.SINGLE);
        cyderScrollList.setScrollFont(CyderFonts.weatherFontSmall.deriveFont(16f));

        //add items with coresponding actions to scroll
        for (int i = 0 ; i < directoryNameList.size() ; i++) {
            int finalI = i;
            class thisAction implements CyderScrollList.ScrollAction {
                @Override
                public void fire() {
                    if (directoryFileList.get(finalI).isDirectory()) {
                        refreshBasedOnDir(directoryFileList.get(finalI));
                    } else {
                        IOUtil.openFile(directoryFileList.get(finalI).getAbsolutePath());
                    }
                }
            }

            thisAction action = new thisAction();
            cyderScrollList.addElement(directoryNameList.get(i), action);
        }

        //generate scroll and add it
        dirScrollLabel = cyderScrollList.generateScrollList();
        dirScrollLabel.setBounds(10,90,600, 400);
        dirFrame.getContentPane().add(dirScrollLabel);

        //frame revalidation
        dirFrame.revalidate();
        dirFrame.repaint();
        dirFrame.setTitle(directory.getName());
        dirField.setText(directory.getAbsolutePath());
    }
}
