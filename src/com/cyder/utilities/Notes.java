package com.cyder.utilities;

import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderScrollPane;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Notes {
    private JFrame noteEditorFrame;
    private JTextArea noteEditArea;
    private JTextField noteEditField;
    private File currentUserNote;
    private JFrame newNoteFrame;
    private JTextField newNoteField;
    private JTextArea newNoteArea;
    private JFrame noteFrame;
    private CyderScrollPane noteListScroll;
    private JList<?> fileSelectionList;
    private List<String> noteNameList;
    private List<File> noteList;
    private CyderButton openNote;

    private String UUID = "";

    private Util noteUtil = new Util();

    public Notes(String UUID) {
        this.UUID = UUID;

        if (noteFrame != null)
            noteUtil.closeAnimation(noteFrame);

        noteFrame = new JFrame();

        noteFrame.setResizable(false);

        noteFrame.setTitle("Notes");

        noteFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        noteFrame.setResizable(false);

        noteFrame.setIconImage(noteUtil.getCyderIcon().getImage());

        JPanel ParentPanel = new JPanel();

        ParentPanel.setLayout(new BoxLayout(ParentPanel,BoxLayout.Y_AXIS));

        ParentPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(noteUtil.navy,5,false)));

        initializeNotesList();

        fileSelectionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        fileSelectionList.setFont(noteUtil.weatherFontSmall);

        fileSelectionList.setForeground(noteUtil.navy);

        fileSelectionList.setSelectionBackground(noteUtil.selectionColor);

        noteListScroll = new CyderScrollPane(fileSelectionList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        noteListScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(noteUtil.navy,5,false)));

        noteListScroll.setThumbColor(noteUtil.regularRed);

        noteListScroll.setPreferredSize(new Dimension(570,300));

        noteListScroll.setFont(noteUtil.weatherFontSmall);

        noteListScroll.setForeground(noteUtil.navy);

        ParentPanel.add(noteListScroll);

        JPanel ButtonPanel = new JPanel();

        ButtonPanel.setLayout(new GridLayout(1,3,5,5));

        CyderButton addNote = new CyderButton("Add Note");

        addNote.setColors(noteUtil.regularRed);

        addNote.setBorder(new LineBorder(noteUtil.navy,5,false));

        ButtonPanel.add(addNote);

        addNote.setFocusPainted(false);

        addNote.setBackground(noteUtil.regularRed);

        addNote.setFont(noteUtil.weatherFontSmall);

        addNote.addActionListener(e -> addNote());

        openNote = new CyderButton("Open Note");

        openNote.setColors(noteUtil.regularRed);

        ButtonPanel.add(openNote);

        openNote.setFocusPainted(false);

        openNote.setBorder(new LineBorder(noteUtil.navy,5,false));

        openNote.setBackground(noteUtil.regularRed);

        openNote.setFont(noteUtil.weatherFontSmall);

        openNote.addActionListener(e -> {
            List<?> ClickedSelectionList = fileSelectionList.getSelectedValuesList();

            if (!ClickedSelectionList.isEmpty()) {
                String ClickedSelection = ClickedSelectionList.get(0).toString();

                File ClickedSelectionPath = null;

                for (int i = 0; i < noteNameList.size() ; i++) {
                    if (ClickedSelection.equals(noteNameList.get(i))) {
                        ClickedSelectionPath = noteList.get(i);
                        break;
                    }
                }

                openNote(ClickedSelectionPath);
            }
        });

        CyderButton deleteNote = new CyderButton("Delete Note");

        deleteNote.setColors(noteUtil.regularRed);

        deleteNote.setBorder(new LineBorder(noteUtil.navy,5,false));

        ButtonPanel.add(deleteNote);

        deleteNote.setFocusPainted(false);

        deleteNote.setBackground(noteUtil.regularRed);

        deleteNote.setFont(noteUtil.weatherFontSmall);

        deleteNote.addActionListener(e -> {
            List<?> ClickedSelectionList = fileSelectionList.getSelectedValuesList();

            if (!ClickedSelectionList.isEmpty()) {
                String ClickedSelection = ClickedSelectionList.get(0).toString();

                File ClickedSelectionPath = null;

                for (int i = 0; i < noteNameList.size() ; i++) {
                    if (ClickedSelection.equals(noteNameList.get(i))) {
                        ClickedSelectionPath = noteList.get(i);
                        break;
                    }
                }

                if (ClickedSelectionPath != null) {
                    ClickedSelectionPath.delete();
                }
                initializeNotesList();
                noteListScroll.setViewportView(fileSelectionList);
                noteListScroll.revalidate();
            }
        });

        ParentPanel.add(ButtonPanel);

        ParentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        noteFrame.add(ParentPanel);

        noteFrame.setVisible(true);

        noteFrame.pack();

        noteFrame.requestFocus();

        noteFrame.setLocationRelativeTo(null);
    }

    private void addNote() {
        if (newNoteFrame != null)
            noteUtil.closeAnimation(newNoteFrame);

        newNoteFrame = new JFrame();

        newNoteFrame.setResizable(false);

        newNoteFrame.setTitle("New note");

        newNoteFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        newNoteFrame.setIconImage(noteUtil.getCyderIcon().getImage());

        JPanel ParentPanel = new JPanel();

        ParentPanel.setLayout(new BoxLayout(ParentPanel,BoxLayout.Y_AXIS));

        JLabel FileNameLabel = new JLabel("Note Title");

        FileNameLabel.setFont(noteUtil.weatherFontSmall);

        JPanel TopPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        TopPanel.add(FileNameLabel,SwingConstants.CENTER);

        ParentPanel.add(TopPanel,SwingConstants.CENTER);

        newNoteField = new JTextField(30);

        newNoteField.setFont(noteUtil.weatherFontSmall);

        newNoteField.setForeground(noteUtil.navy);

        newNoteField.setBorder(new LineBorder(noteUtil.navy,5,false));

        newNoteField.setSelectionColor(noteUtil.selectionColor);

        JPanel MiddlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        MiddlePanel.add(newNoteField);

        ParentPanel.add(MiddlePanel);

        JLabel NoteTextLabel = new JLabel("Note Contents");

        NoteTextLabel.setFont(noteUtil.weatherFontSmall);

        JPanel BottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        BottomPanel.add(NoteTextLabel);

        ParentPanel.add(BottomPanel);

        newNoteArea = new JTextArea(20,20);

        newNoteArea.setFont(noteUtil.weatherFontSmall);

        newNoteArea.setAutoscrolls(false);

        newNoteArea.setLineWrap(true);

        newNoteArea.setWrapStyleWord(true);

        newNoteArea.setSelectedTextColor(noteUtil.selectionColor);

        newNoteArea.setBorder(new LineBorder(new Color(0, 0, 0)));

        CyderScrollPane NewNoteScroll = new CyderScrollPane(newNoteArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        NewNoteScroll.setThumbColor(noteUtil.regularRed);

        NewNoteScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        NewNoteScroll.getViewport().setBorder(null);

        NewNoteScroll.setViewportBorder(null);

        NewNoteScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(noteUtil.navy,5,false)));

        NewNoteScroll.setPreferredSize(new Dimension(570,780));

        ParentPanel.add(NewNoteScroll);

        CyderButton submitNewNote = new CyderButton("Create Note");

        submitNewNote.setBorder(new LineBorder(noteUtil.navy,5,false));

        submitNewNote.setFocusPainted(false);

        submitNewNote.setColors(noteUtil.regularRed);

        submitNewNote.setBackground(noteUtil.regularRed);

        submitNewNote.setFont(noteUtil.weatherFontSmall);

        submitNewNote.addActionListener(e -> {
            try {
                BufferedWriter NoteWriter = new BufferedWriter(new FileWriter(
                        "src\\com\\cyder\\users\\" + UUID + "\\Notes\\" + newNoteField.getText() + ".txt",true));
                newNoteArea.write(NoteWriter);
                NoteWriter.close();
            }

            catch (Exception ex) {
                noteUtil.handle(ex);
            }

            noteUtil.closeAnimation(newNoteFrame);

            initializeNotesList();

            noteListScroll.setViewportView(fileSelectionList);
            noteListScroll.revalidate();
        });

        JPanel ButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        ButtonPanel.add(submitNewNote);

        ButtonPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        ParentPanel.add(ButtonPanel);

        ParentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        newNoteFrame.add(ParentPanel);

        newNoteFrame.pack();

        newNoteFrame.setLocationRelativeTo(null);

        newNoteFrame.setVisible(true);

        newNoteField.requestFocus();
    }

    private void initializeNotesList() {
        File dir = new File("src\\com\\cyder\\users\\" + UUID + "\\Notes");
        noteList = new LinkedList<>();
        noteNameList = new LinkedList<>();

        for (File file : dir.listFiles()) {
            if (file.getName().endsWith((".txt"))) {
                noteList.add(file.getAbsoluteFile());
                noteNameList.add(file.getName().replace(".txt", ""));
            }
        }

        String[] NotesArray = new String[noteNameList.size()];
        NotesArray = noteNameList.toArray(NotesArray);
        fileSelectionList = new JList(NotesArray);
        fileSelectionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && fileSelectionList.getSelectedIndex() != -1) {
                    openNote.doClick();
                }
            }
        });

        fileSelectionList.setFont(noteUtil.weatherFontSmall);

        fileSelectionList.setForeground(noteUtil.navy);

        fileSelectionList.setSelectionBackground(noteUtil.selectionColor);
    }

    private void openNote(File File) {
        if (noteEditorFrame != null)
            noteUtil.closeAnimation(noteEditorFrame);

        noteEditorFrame = new JFrame();

        noteEditorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        noteEditorFrame.setUndecorated(false);

        noteEditorFrame.setTitle("Editing note: " + File.getName().replace(".txt", ""));

        noteEditorFrame.setResizable(false);

        noteEditorFrame.setIconImage(noteUtil.getCyderIcon().getImage());

        JPanel ParentPanel = new JPanel();

        noteEditorFrame.setContentPane(ParentPanel);

        ParentPanel.setLayout(new BorderLayout());

        noteEditField = new JTextField(20);

        noteEditField.setToolTipText("Change Name");

        noteEditField.setSelectionColor(noteUtil.selectionColor);

        noteEditField.setText(File.getName().replaceFirst(".txt",""));

        noteEditField.setFont(noteUtil.weatherFontSmall);

        noteEditField.setForeground(noteUtil.navy);

        noteEditField.setBorder(new LineBorder(noteUtil.navy,5,false));

        ParentPanel.add(noteEditField, BorderLayout.PAGE_START);

        noteEditArea = new JTextArea(20, 20);

        noteEditArea.setSelectedTextColor(noteUtil.selectionColor);

        noteEditArea.setFont(noteUtil.weatherFontSmall);

        noteEditArea.setForeground(noteUtil.navy);

        noteEditArea.setEditable(true);

        noteEditArea.setAutoscrolls(true);

        noteEditArea.setLineWrap(true);

        noteEditArea.setWrapStyleWord(true);

        noteEditArea.setFocusable(true);

        CyderScrollPane noteScroll = new CyderScrollPane(noteEditArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        noteScroll.setThumbColor(noteUtil.regularRed);

        noteScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        noteScroll.getViewport().setBorder(null);

        noteScroll.setViewportBorder(null);

        noteScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(noteUtil.navy,5,false)));

        noteScroll.setPreferredSize(new Dimension(570,780));

        ParentPanel.add(noteScroll, BorderLayout.CENTER);

        try {
            BufferedReader InitReader = new BufferedReader(new FileReader(File));
            String Line = InitReader.readLine();

            while (Line != null) {
                noteEditArea.append(Line + "\n");
                Line = InitReader.readLine();
            }

            InitReader.close();
        }

        catch (Exception e) {
            noteUtil.handle(e);
        }

        currentUserNote = File;

        CyderButton saveNote = new CyderButton("Save & Resign");

        saveNote.setColors(noteUtil.regularRed);

        saveNote.setBorder(new LineBorder(noteUtil.navy,5,false));

        saveNote.setFocusPainted(false);

        saveNote.setBackground(noteUtil.regularRed);

        saveNote.setFont(noteUtil.weatherFontSmall);

        saveNote.addActionListener(e -> {
            try {
                BufferedWriter SaveWriter = new BufferedWriter(new FileWriter(currentUserNote, false));
                SaveWriter.write(noteEditArea.getText());
                SaveWriter.close();

                File newName = null;

                if (noteEditField.getText().length() > 0) {
                    newName = new File(File.getAbsolutePath().replace(File.getName(),noteEditField.getText() + ".txt"));
                    File.renameTo(newName);
                    noteUtil.inform(newName.getName().replace(".txt", "") + " has been successfully saved.","", 400, 200);
                    initializeNotesList();
                    noteListScroll.setViewportView(fileSelectionList);
                    noteListScroll.revalidate();
                }

                else {
                    noteUtil.inform(currentUserNote.getName().replace(".txt", "") + " has been successfully saved.","", 400, 200);
                }

                noteUtil.closeAnimation(noteEditorFrame);
            }

            catch (Exception exc) {
                noteUtil.handle(exc);
            }
        });

        JPanel SavePanel = new JPanel();

        ParentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        SavePanel.add(saveNote, SwingConstants.CENTER);

        ParentPanel.add(SavePanel, BorderLayout.PAGE_END);

        noteEditorFrame.pack();

        noteEditorFrame.setVisible(true);

        noteEditArea.requestFocus();

        noteEditorFrame.setLocationRelativeTo(null);
    }
}
