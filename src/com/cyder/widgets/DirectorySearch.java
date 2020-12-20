package com.cyder.widgets;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;
import com.cyder.ui.CyderScrollPane;
import com.cyder.utilities.IOUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Stack;

public class DirectorySearch {
    private CyderFrame dirFrame;
    private JTextField dirField;
    private CyderScrollPane dirScroll;
    private JList<?> directoryNameList;
    private JList<?> directoryList;

    private Stack<String> backward = new Stack<>();
    private Stack<String> foward = new Stack<>();

    private String rightNow = System.getProperty("user.dir");
    public DirectorySearch() {
        if (dirFrame != null)
            dirFrame.closeAnimation();

        dirFrame = new CyderFrame(620,470, new ImageIcon("src/com/cyder/sys/pictrures/DebugBackground.png"));
        dirFrame.setTitle(new File(System.getProperty("user.dir")).getName());

        dirField = new JTextField(40);
        dirField.setSelectionColor(CyderColors.selectionColor);
        dirField.setText(System.getProperty("user.dir"));
        dirField.setFont(CyderFonts.weatherFontSmall);
        dirField.setForeground(CyderColors.navy);
        dirField.addActionListener(directoryFieldListener);
        dirField.setBorder(new LineBorder(CyderColors.navy,5,false));
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
            if (!backward.empty()) {
                foward.push(rightNow);
                File ChosenDir = new File(backward.pop());
                rightNow = ChosenDir.toString();

                dirField.setText(ChosenDir.toString());
                directoryList = new JList(ChosenDir.listFiles());

                File[] Files = ChosenDir.listFiles();
                String[] Names = new String[Files.length];

                for (int i = 0 ; i < Files.length ; i++) {
                    Names[i] = Files[i].getName();
                }

                directoryNameList = new JList(Names);
                directoryNameList.setFont(CyderFonts.weatherFontSmall);
                directoryNameList.setForeground(CyderColors.navy);
                directoryNameList.setSelectionBackground(CyderColors.selectionColor);
                directoryNameList.addMouseListener(directoryListener);
                directoryNameList.addKeyListener(directoryEnterListener);

                dirScroll.setViewportView(directoryNameList);
                dirScroll.revalidate();
                dirScroll.repaint();

                dirFrame.revalidate();
                dirFrame.repaint();
            }
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
            if (!foward.empty()) {
                backward.push(rightNow);
                File ChosenDir = new File(foward.pop());
                rightNow = ChosenDir.toString();

                dirField.setText(ChosenDir.toString());
                directoryList = new JList(ChosenDir.listFiles());

                File[] Files = ChosenDir.listFiles();
                String[] Names = new String[Files.length];

                for (int i = 0 ; i < Files.length ; i++) {
                    Names[i] = Files[i].getName();
                }

                directoryNameList = new JList(Names);
                directoryNameList.setFont(CyderFonts.weatherFontSmall);
                directoryNameList.setForeground(CyderColors.navy);
                directoryNameList.setSelectionBackground(CyderColors.selectionColor);
                directoryNameList.addMouseListener(directoryListener);
                directoryNameList.addKeyListener(directoryEnterListener);

                dirScroll.setViewportView(directoryNameList);
                dirScroll.revalidate();
                dirScroll.repaint();

                dirFrame.setTitle(ChosenDir.getName());

                dirFrame.revalidate();
                dirFrame.repaint();
            }
        });
        next.setBounds(620 - 15 - 15 - 40,50,40,60);
        dirFrame.getContentPane().add(next);

        File[] DirFiles = new File(System.getProperty("user.dir")).listFiles();

        directoryList = new JList(DirFiles);
        directoryList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        File ChosenDir = new File(System.getProperty("user.dir"));

        directoryList = new JList(ChosenDir.listFiles());

        File[] Files = ChosenDir.listFiles();
        String[] Names = new String[0];

        if (Files != null) {
            Names = new String[Files.length];
        }

        if (Files != null) {
            for (int i = 0 ; i < Files.length ; i++) {
                Names[i] = Files[i].getName();
            }
        }

        directoryNameList = new JList(Names);
        directoryNameList.setFont(CyderFonts.weatherFontSmall);
        directoryNameList.setSelectionBackground(CyderColors.selectionColor);
        directoryNameList.setForeground(CyderColors.navy);
        directoryNameList.addMouseListener(directoryListener);

        directoryNameList.addKeyListener(directoryEnterListener);
        dirScroll = new CyderScrollPane(directoryNameList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        dirScroll.setThumbColor(CyderColors.regularRed);

        dirScroll.setForeground(CyderColors.navy);
        dirScroll.setFont(CyderFonts.weatherFontSmall);
        dirScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(CyderColors.navy,5,false)));
        dirScroll.setBounds(10,120,600,470 - 120 - 10);
        dirFrame.getContentPane().add(dirScroll);

        dirFrame.setLocationRelativeTo(null);
        dirFrame.setVisible(true);
        dirField.requestFocus();
    }

    private MouseListener directoryListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent mouseEvent) {
            JList theList = (JList) mouseEvent.getSource();

            if (mouseEvent.getClickCount() == 2) {
                int index = theList.locationToIndex(mouseEvent.getPoint());

                if (index >= 0) {
                    File ChosenDir = (File) directoryList.getModel().getElementAt(index);

                    if (ChosenDir.isDirectory()) {
                        if (!foward.isEmpty() && !foward.peek().equals(rightNow))
                            foward.clear();

                        if (!backward.isEmpty() && !backward.peek().equals(rightNow) && !foward.isEmpty() && !foward.peek().equals(rightNow))
                            backward.push(rightNow);
                        else if (backward.isEmpty());
                            backward.push(rightNow);

                        rightNow = ChosenDir.toString();

                        dirField.setText(ChosenDir.toString());

                        directoryList = new JList(ChosenDir.listFiles());

                        File[] Files = ChosenDir.listFiles();
                        String[] Names = new String[Files.length];

                        for (int i = 0 ; i < Files.length ; i++) {
                            Names[i] = Files[i].getName();
                        }

                        directoryNameList = new JList(Names);

                        directoryNameList.setFont(CyderFonts.weatherFontSmall);
                        directoryNameList.setForeground(CyderColors.navy);
                        directoryNameList.setSelectionBackground(CyderColors.selectionColor);

                        directoryNameList.addMouseListener(directoryListener);
                        directoryNameList.addKeyListener(directoryEnterListener);

                        dirScroll.setViewportView(directoryNameList);
                        dirScroll.revalidate();
                        dirScroll.repaint();

                        dirFrame.setTitle(ChosenDir.getName());

                        dirFrame.revalidate();
                        dirFrame.repaint();
                    }

                    else if (ChosenDir.isFile()) {
                        IOUtil.openFile(ChosenDir.getAbsolutePath());
                    }
                }
            }
        }
    };

    private ActionListener directoryFieldListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String newDir = dirField.getText();
            File ChosenDir = new File(newDir);

            if (ChosenDir.exists()) {
                if (ChosenDir.isDirectory()) {
                    if (!foward.isEmpty() && !foward.peek().equals(rightNow))
                        foward.clear();

                    if (!backward.isEmpty() && !backward.peek().equals(rightNow) && !foward.isEmpty() && !foward.peek().equals(rightNow))
                        backward.push(rightNow);
                    else if (backward.isEmpty());
                        backward.push(rightNow);

                    rightNow = ChosenDir.toString();

                    directoryList = new JList(ChosenDir.listFiles());
                    File[] Files = ChosenDir.listFiles();
                    String[] Names = new String[0];
                    if (Files != null) {
                        Names = new String[Files.length];
                    }
                    if (Files != null) {
                        for (int i = 0 ; i < Files.length ; i++) {
                            Names[i] = Files[i].getName();
                        }
                    }

                    directoryNameList = new JList(Names);
                    directoryNameList.setFont(CyderFonts.weatherFontSmall);
                    directoryNameList.setForeground(CyderColors.navy);
                    directoryNameList.setSelectionBackground(CyderColors.selectionColor);
                    directoryNameList.addMouseListener(directoryListener);
                    directoryNameList.addKeyListener(directoryEnterListener);
                    dirScroll.setViewportView(directoryNameList);
                    dirScroll.revalidate();
                    dirScroll.repaint();
                    dirFrame.revalidate();
                    dirFrame.repaint();

                    dirFrame.setTitle(ChosenDir.getName());
                }

                else if (ChosenDir.isFile()) {
                    IOUtil.openFile(ChosenDir.getAbsolutePath());
                }
            }

            else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    };

    private KeyListener directoryEnterListener = new KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent e) {
            int index = directoryNameList.getSelectedIndex();

            if (index >= 0) {
                File ChosenDir = (File) directoryList.getModel().getElementAt(index);

                if (ChosenDir.isDirectory()) {
                    if (!foward.isEmpty() && !foward.peek().equals(rightNow))
                        foward.clear();

                    if (!backward.isEmpty() && !backward.peek().equals(rightNow) && !foward.isEmpty() && !foward.peek().equals(rightNow))
                        backward.push(rightNow);
                    else if (backward.isEmpty());
                        backward.push(rightNow);

                    rightNow = ChosenDir.toString();

                    dirField.setText(ChosenDir.toString());
                    directoryList = new JList(ChosenDir.listFiles());

                    File[] Files = ChosenDir.listFiles();
                    String[] Names = new String[Files.length];

                    for (int i = 0 ; i < Files.length ; i++) {
                        Names[i] = Files[i].getName();
                    }

                    directoryNameList = new JList(Names);
                    directoryNameList.setFont(CyderFonts.weatherFontSmall);
                    directoryNameList.addMouseListener(directoryListener);
                    directoryNameList.addKeyListener(directoryEnterListener);

                    dirScroll.setViewportView(directoryNameList);
                    dirScroll.revalidate();
                    dirScroll.repaint();

                    dirFrame.setTitle(ChosenDir.getName());

                    dirFrame.revalidate();
                    dirFrame.repaint();
                }

                else if (ChosenDir.isFile()) {
                    IOUtil.openFile(ChosenDir.getAbsolutePath());
                }
            }
        }
    };

    private void printStacks() {
        System.out.println("backwards:");
        for (int i = backward.size() - 1; i >= 0 ; i--) {
            System.out.println(backward.get(i));
        }

        System.out.println("fowards:");
        for (int i = foward.size() - 1; i >= 0 ; i--) {
            System.out.println(foward.get(i));
        }
    }
}
