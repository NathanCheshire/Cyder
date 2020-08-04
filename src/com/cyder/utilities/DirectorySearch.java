package com.cyder.utilities;

import com.cyder.ui.CyderScrollPane;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

public class DirectorySearch {
    private Util dirUtil = new Util();
    private JFrame dirFrame;
    private JTextField dirField;
    private CyderScrollPane dirScroll;
    private JList<?> directoryNameList;
    private JList<?> directoryList;
    private JPanel dirSearchParentPanel;

    public DirectorySearch() {
        if (dirFrame != null) {
            dirUtil.closeAnimation(dirFrame);
            dirFrame.dispose();
        }

        dirFrame = new JFrame();
        dirFrame.setTitle("Directory Search");
        dirFrame.setResizable(false);
        dirFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dirFrame.setIconImage(dirUtil.getCyderIcon().getImage());

        dirSearchParentPanel = new JPanel();
        dirSearchParentPanel.setLayout(new BorderLayout());

        dirField = new JTextField(40);
        dirField.setSelectionColor(dirUtil.selectionColor);
        dirField.setText(System.getProperty("user.dir"));
        dirField.setFont(dirUtil.weatherFontSmall);
        dirField.setForeground(dirUtil.navy);

        dirField.addActionListener(e -> {
            String newDir = dirField.getText();
            File ChosenDir = new File(newDir);

            if (ChosenDir.exists()) {
                if (ChosenDir.isDirectory()) {
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
                    directoryNameList.setFont(dirUtil.weatherFontSmall);
                    directoryNameList.setForeground(dirUtil.navy);
                    directoryNameList.setSelectionBackground(dirUtil.selectionColor);
                    directoryNameList.addMouseListener(directoryListener);
                    directoryNameList.addKeyListener(directoryEnterListener);
                    dirScroll.setViewportView(directoryNameList);
                    dirScroll.revalidate();
                    dirScroll.repaint();
                    dirSearchParentPanel.revalidate();
                    dirSearchParentPanel.repaint();
                    dirFrame.revalidate();
                    dirFrame.repaint();
                }

                else if (ChosenDir.isFile()) {
                    dirUtil.openFile(ChosenDir.getAbsolutePath());
                }
            }

            else {
                dirUtil.beep();
            }
        });

        JPanel dirFieldPanel = new JPanel();

        dirField.setBorder(new LineBorder(dirUtil.navy,5,false));

        dirFieldPanel.add(dirField);

        dirSearchParentPanel.add(dirFieldPanel, BorderLayout.PAGE_START);

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

        directoryNameList.setFont(dirUtil.weatherFontSmall);

        directoryNameList.setSelectionBackground(dirUtil.selectionColor);

        directoryNameList.setForeground(dirUtil.navy);

        directoryNameList.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList<String> theList = (JList) mouseEvent.getSource();

                if (mouseEvent.getClickCount() == 2) {
                    int index = theList.locationToIndex(mouseEvent.getPoint());

                    if (index >= 0) {
                        File ChosenDir = (File) directoryList.getModel().getElementAt(index);

                        if (ChosenDir.isDirectory()) {
                            dirField.setText(ChosenDir.toString());

                            directoryList = new JList(ChosenDir.listFiles());

                            File[] Files = ChosenDir.listFiles();

                            String[] Names = new String[0];
                            if (Files != null) {
                                Names = new String[Files.length];
                            }

                            for (int i = 0 ; i < Files.length ; i++) {
                                Names[i] = Files[i].getName();
                            }

                            directoryNameList = new JList(Names);

                            directoryNameList.setFont(dirUtil.weatherFontSmall);
                            directoryNameList.setForeground(dirUtil.navy);

                            directoryNameList.addMouseListener(directoryListener);

                            directoryNameList.setSelectionBackground(dirUtil.selectionColor);

                            directoryNameList.addKeyListener(directoryEnterListener);

                            dirScroll.setViewportView(directoryNameList);

                            dirScroll.revalidate();

                            dirScroll.repaint();

                            dirSearchParentPanel.revalidate();

                            dirSearchParentPanel.repaint();

                            dirFrame.revalidate();

                            dirFrame.repaint();
                        }

                        else if (ChosenDir.isFile()) {
                            dirUtil.openFile(ChosenDir.getAbsolutePath());
                        }
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent arg0) {

            }

            @Override
            public void mouseReleased(MouseEvent arg0) {

            }
        });

        directoryNameList.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                int index = directoryNameList.getSelectedIndex();

                if (index >= 0) {
                    File ChosenDir = (File) directoryList.getModel().getElementAt(index);

                    if (ChosenDir.isDirectory()) {
                        dirField.setText(ChosenDir.toString());

                        directoryList = new JList(ChosenDir.listFiles());

                        File[] Files = ChosenDir.listFiles();

                        String[] Names = new String[0];
                        if (Files != null) {
                            Names = new String[Files.length];
                        }

                        for (int i = 0; i < Files.length; i++) {
                            Names[i] = Files[i].getName();
                        }

                        directoryNameList = new JList(Names);

                        directoryNameList.setFont(dirUtil.weatherFontSmall);

                        directoryNameList.setForeground(dirUtil.navy);

                        directoryNameList.setSelectionBackground(dirUtil.selectionColor);

                        directoryNameList.addMouseListener(directoryListener);

                        directoryNameList.addKeyListener(directoryEnterListener);

                        dirScroll.setViewportView(directoryNameList);

                        dirScroll.revalidate();

                        dirScroll.repaint();

                        dirSearchParentPanel.revalidate();

                        dirSearchParentPanel.repaint();

                        dirFrame.revalidate();

                        dirFrame.repaint();
                    }

                    else if (ChosenDir.isFile()) {
                        dirUtil.openFile(ChosenDir.getAbsolutePath());
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        dirScroll = new CyderScrollPane(directoryNameList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        dirScroll.setThumbColor(dirUtil.regularRed);

        dirScroll.setFont(dirUtil.weatherFontSmall);
        dirScroll.setForeground(dirUtil.navy);
        dirScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(dirUtil.navy,5,false)));

        dirSearchParentPanel.add(dirScroll, BorderLayout.CENTER);

        dirSearchParentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        dirFrame.add(dirSearchParentPanel);
        dirFrame.pack();
        dirFrame.setLocationRelativeTo(null);
        dirFrame.setVisible(true);
        dirField.requestFocus();
    }

    private MouseListener directoryListener = new MouseListener() {
        public void mouseClicked(MouseEvent mouseEvent) {
            JList theList = (JList) mouseEvent.getSource();

            if (mouseEvent.getClickCount() == 2) {
                int index = theList.locationToIndex(mouseEvent.getPoint());

                if (index >= 0) {
                    File ChosenDir = (File) directoryList.getModel().getElementAt(index);

                    if (ChosenDir.isDirectory()) {
                        dirField.setText(ChosenDir.toString());

                        directoryList = new JList(ChosenDir.listFiles());

                        File[] Files = ChosenDir.listFiles();

                        String[] Names = new String[Files.length];

                        for (int i = 0 ; i < Files.length ; i++) {
                            Names[i] = Files[i].getName();
                        }

                        directoryNameList = new JList(Names);

                        directoryNameList.setFont(dirUtil.weatherFontSmall);
                        directoryNameList.setForeground(dirUtil.navy);
                        directoryNameList.setSelectionBackground(dirUtil.selectionColor);

                        directoryNameList.addMouseListener(directoryListener);

                        directoryNameList.addKeyListener(directoryEnterListener);

                        dirScroll.setViewportView(directoryNameList);

                        dirScroll.revalidate();

                        dirScroll.repaint();

                        dirSearchParentPanel.revalidate();

                        dirSearchParentPanel.repaint();

                        dirFrame.revalidate();

                        dirFrame.repaint();
                    }

                    else if (ChosenDir.isFile()) {
                        dirUtil.openFile(ChosenDir.getAbsolutePath());
                    }
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent arg0) {

        }

        @Override
        public void mouseReleased(MouseEvent arg0) {

        }
    };

    private KeyListener directoryEnterListener = new KeyListener()
    {
        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(java.awt.event.KeyEvent e) {
            int index = directoryNameList.getSelectedIndex();

            if (index >= 0) {
                File ChosenDir = (File) directoryList.getModel().getElementAt(index);

                if (ChosenDir.isDirectory()) {
                    dirField.setText(ChosenDir.toString());

                    directoryList = new JList(ChosenDir.listFiles());

                    File[] Files = ChosenDir.listFiles();

                    String[] Names = new String[Files.length];

                    for (int i = 0 ; i < Files.length ; i++) {
                        Names[i] = Files[i].getName();
                    }

                    directoryNameList = new JList(Names);

                    directoryNameList.setFont(new Font("Sans Serif",Font.PLAIN, 18));

                    directoryNameList.addMouseListener(directoryListener);

                    directoryNameList.addKeyListener(directoryEnterListener);

                    dirScroll.setViewportView(directoryNameList);

                    dirScroll.revalidate();

                    dirScroll.repaint();

                    dirSearchParentPanel.revalidate();

                    dirSearchParentPanel.repaint();

                    dirFrame.revalidate();

                    dirFrame.repaint();
                }

                else if (ChosenDir.isFile()) {
                    dirUtil.openFile(ChosenDir.getAbsolutePath());
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    };
}
