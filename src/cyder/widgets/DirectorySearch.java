package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.ui.CyderButton;
import cyder.ui.CyderCaret;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollPane;
import cyder.utilities.IOUtil;

import javax.swing.*;
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

        dirFrame = new CyderFrame(620,470, CyderImages.defaultBackground);
        dirFrame.setTitle(new File(System.getProperty("user.dir")).getName());

        dirField = new JTextField(40);
        dirField.setSelectionColor(CyderColors.selectionColor);
        dirField.setFont(CyderFonts.weatherFontSmall);
        dirField.setForeground(CyderColors.navy);
        dirField.setCaretColor(CyderColors.navy);
        dirField.setCaret(new CyderCaret(CyderColors.navy));
        dirField.setText(System.getProperty("user.dir"));
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

                dirFrame.setTitle(ChosenDir.getName());

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
        directoryNameList.addKeyListener(directoryKeyListener);
        directoryNameList.setBounds(10,120,580,340);

        directoryList.setMinimumSize(new Dimension(500, 340));
        directoryList.setMaximumSize(new Dimension(500, 340));
        directoryNameList.setMinimumSize(new Dimension(500, 340));
        directoryNameList.setMaximumSize(new Dimension(500, 340));

        dirScroll = new CyderScrollPane(directoryNameList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        dirScroll.setThumbColor(CyderColors.regularRed);

        dirScroll.setBorder(new LineBorder(CyderColors.navy,5,false));
        dirScroll.addKeyListener(directoryKeyListener);

        directoryNameList.setBackground(new Color(255,255,255));
        dirScroll.getViewport().setBackground(new Color(255,255,255));

        dirScroll.setForeground(CyderColors.navy);
        dirScroll.setFont(CyderFonts.weatherFontSmall);

        dirScroll.setBounds(10,120,600,340);
        dirFrame.getContentPane().add(dirScroll);

        dirFrame.setLocationRelativeTo(null);
        dirFrame.setVisible(true);
        dirField.requestFocus();
    }

    private KeyListener directoryKeyListener = new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e) {
            if (e.getKeyCode() != KeyEvent.VK_ENTER);
                e.consume();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() != KeyEvent.VK_ENTER);
            e.consume();
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() != KeyEvent.VK_ENTER);
            e.consume();
        }
    };

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

                    if (Files != null)
                        Names = new String[Files.length];

                    if (Files != null)
                        for (int i = 0 ; i < Files.length ; i++)
                            Names[i] = Files[i].getName();

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

    private KeyListener directoryEnterListener = new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e) {
            if (e.getKeyCode() != KeyEvent.VK_ENTER);
                e.consume();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() != KeyEvent.VK_ENTER);
                e.consume();

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

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() != KeyEvent.VK_ENTER);
                e.consume();
        }
    };
}
