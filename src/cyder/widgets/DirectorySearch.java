package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.ui.*;
import cyder.utilities.IOUtil;
import cyder.utilities.SystemUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;

public class DirectorySearch {
    private CyderFrame dirFrame;
    private CyderTextField dirField;
    private CyderScrollList cyderScrollList;
    private JLabel dirScrollLabel;

    private LinkedList<String> directoryNameList = new LinkedList<>();
    private LinkedList<File> directoryFileList = new LinkedList<>();

    private Stack<String> backward = new Stack<>();
    private Stack<String> foward = new Stack<>();

    private String currentDirectory = "c:/users/"
            + SystemUtil.getWindowsUsername() + "/";

    public DirectorySearch() {
        if (dirFrame != null)
            dirFrame.closeAnimation();

        dirFrame = new CyderFrame(620,470, CyderImages.defaultBackground);
        dirFrame.setTitle(new File("c:/users/"
                + SystemUtil.getWindowsUsername() + "/").getName());

        dirField = new CyderTextField(0);
        dirField.setBackground(Color.white);
        dirField.setText("c:/users/"
                + SystemUtil.getWindowsUsername() + "/");
        dirField.addActionListener(directoryEnterListener);
        dirField.setBounds(15 + 40 + 15,60,620 - 160,40);
        dirFrame.getContentPane().add(dirField);

        CyderButton last = new CyderButton(" < ");
        last.setFocusPainted(false);
        last.setForeground(CyderColors.navy);
        last.setBackground(CyderColors.regularRed);
        last.setFont(CyderFonts.weatherFontSmall);
        last.setBorder(new LineBorder(CyderColors.navy,5,false));
        last.setColors(CyderColors.regularRed);
        last.addActionListener(e -> {
            //todo
        });
        last.setBounds(15,50,40,60);
        dirFrame.getContentPane().add(last);

        CyderButton next = new CyderButton(" > ");
        next.setFocusPainted(false);
        next.setForeground(CyderColors.navy);
        next.setBackground(CyderColors.regularRed);
        next.setFont(CyderFonts.weatherFontSmall);
        next.setBorder(new LineBorder(CyderColors.navy,5,false));
        next.setColors(CyderColors.regularRed);
        next.addActionListener(e -> {
            //todo
        });
        next.setBounds(620 - 15 - 15 - 40,50,40,60);
        dirFrame.getContentPane().add(next);

        File chosenDir = new File("c:/users/"
                + SystemUtil.getWindowsUsername() + "/");
        File[] startDir = chosenDir.listFiles();

        Collections.addAll(directoryFileList, startDir);

        for (File file : directoryFileList) {
            directoryNameList.add(file.getName());
        }

        cyderScrollList = new CyderScrollList(600, 340, CyderScrollList.SelectionPolicy.SINGLE);
        cyderScrollList.setScrollFont(CyderFonts.weatherFontSmall.deriveFont(16f));

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

        dirScrollLabel = cyderScrollList.generateScrollList();
        dirScrollLabel.setBounds(10,120,600, 340);
        dirFrame.getContentPane().add(dirScrollLabel);

        ConsoleFrame.getConsoleFrame().setFrameRelative(dirFrame);
        dirFrame.setVisible(true);
        dirField.requestFocus();
    }

    private ActionListener directoryEnterListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            File ChosenDir = new File(dirField.getText());

            if (ChosenDir.isDirectory()) {
                refreshBasedOnDir(ChosenDir);
            } else if (ChosenDir.isFile()) {
                IOUtil.openFile(ChosenDir.getAbsolutePath());
            }
        }
    };

    private void refreshBasedOnDir(File directory) {
        System.out.println("put into back stack: " + dirField.getText());

        File[] files = directory.listFiles();

        cyderScrollList.clearElements();
        dirFrame.remove(dirScrollLabel);

        directoryFileList.clear();
        directoryNameList.clear();

        Collections.addAll(directoryFileList, files);

        for (File file : directoryFileList) {
            directoryNameList.add(file.getName());
        }

        cyderScrollList = new CyderScrollList(600, 340, CyderScrollList.SelectionPolicy.SINGLE);
        cyderScrollList.setScrollFont(CyderFonts.weatherFontSmall.deriveFont(16f));

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

        dirScrollLabel = cyderScrollList.generateScrollList();
        dirScrollLabel.setBounds(10,120,600, 340);
        dirFrame.getContentPane().add(dirScrollLabel);
        dirFrame.revalidate();
        dirFrame.repaint();
        dirFrame.setTitle(directory.getName());
    }
}
