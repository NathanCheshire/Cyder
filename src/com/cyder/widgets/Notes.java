package com.cyder.widgets;

import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;
import com.cyder.ui.CyderScrollPane;
import com.cyder.utilities.GeneralUtil;
import com.cyder.utilities.StringUtil;

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
    private CyderFrame noteEditorFrame;
    private JTextArea noteEditArea;
    private JTextField noteEditField;
    private File currentUserNote;
    private CyderFrame newNoteFrame;
    private JTextField newNoteField;
    private JTextArea newNoteArea;
    private CyderFrame noteFrame;
    private CyderScrollPane noteListScroll;
    private JList<?> fileSelectionList;
    private List<String> noteNameList;
    private List<File> noteList;
    private CyderButton openNote;

    private String UUID = "";

    private GeneralUtil noteGeneralUtil = new GeneralUtil();

    public Notes(String UUID) {
        this.UUID = UUID;

        if (noteFrame != null)
            noteGeneralUtil.closeAnimation(noteFrame);

        noteFrame = new CyderFrame(600,625, new ImageIcon("src/com/cyder/io/pictures/DebugBackground.png"));
        noteFrame.setTitle(noteGeneralUtil.getUsername() + new StringUtil().getApostrophe(noteGeneralUtil.getUsername()) + " notes");

        initializeNotesList();

        fileSelectionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        fileSelectionList.setFont(noteGeneralUtil.weatherFontSmall);
        fileSelectionList.setForeground(noteGeneralUtil.navy);
        fileSelectionList.setSelectionBackground(noteGeneralUtil.selectionColor);
        noteListScroll = new CyderScrollPane(fileSelectionList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        noteListScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(noteGeneralUtil.navy,5,false)));

        noteListScroll.setThumbColor(noteGeneralUtil.regularRed);
        noteListScroll.setFont(noteGeneralUtil.weatherFontSmall);
        noteListScroll.setForeground(noteGeneralUtil.navy);
        noteListScroll.setBounds(40,40,600 - 80, 700 - 100 - 100);
        noteFrame.getContentPane().add(noteListScroll);

        CyderButton addNote = new CyderButton("Add Note");
        addNote.setColors(noteGeneralUtil.regularRed);
        addNote.setBorder(new LineBorder(noteGeneralUtil.navy,5,false));
        addNote.setFocusPainted(false);
        addNote.setBackground(noteGeneralUtil.regularRed);
        addNote.setFont(noteGeneralUtil.weatherFontSmall);
        addNote.addActionListener(e -> addNote());
        addNote.setBounds(50,550,150,50);
        noteFrame.getContentPane().add(addNote);

        openNote = new CyderButton("Open Note");
        openNote.setColors(noteGeneralUtil.regularRed);
        openNote.setFocusPainted(false);
        openNote.setBorder(new LineBorder(noteGeneralUtil.navy,5,false));
        openNote.setBackground(noteGeneralUtil.regularRed);
        openNote.setFont(noteGeneralUtil.weatherFontSmall);
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
        openNote.setBounds(225,550,150,50);
        noteFrame.getContentPane().add(openNote);

        CyderButton deleteNote = new CyderButton("Delete Note");
        deleteNote.setColors(noteGeneralUtil.regularRed);
        deleteNote.setBorder(new LineBorder(noteGeneralUtil.navy,5,false));
        deleteNote.setFocusPainted(false);
        deleteNote.setBackground(noteGeneralUtil.regularRed);
        deleteNote.setFont(noteGeneralUtil.weatherFontSmall);
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
        deleteNote.setBounds(400,550,150,50);
        noteFrame.getContentPane().add(deleteNote);

        noteFrame.setVisible(true);
        noteFrame.setLocationRelativeTo(null);
    }

    private void addNote() {
        if (newNoteFrame != null)
            noteGeneralUtil.closeAnimation(newNoteFrame);

        newNoteFrame = new CyderFrame(600,625, new ImageIcon("src/com/cyder/io/pictures/DebugBackground.png"));
        newNoteFrame.setTitle("New note");

        JLabel FileNameLabel = new JLabel("Note Title");
        FileNameLabel.setFont(noteGeneralUtil.weatherFontSmall);
        FileNameLabel.setForeground(noteGeneralUtil.navy);
        FileNameLabel.setBounds(240,40,150,40);
        newNoteFrame.getContentPane().add(FileNameLabel);

        newNoteField = new JTextField(30);
        newNoteField.setFont(noteGeneralUtil.weatherFontSmall);
        newNoteField.setForeground(noteGeneralUtil.navy);
        newNoteField.setBorder(new LineBorder(noteGeneralUtil.navy,5,false));
        newNoteField.setSelectionColor(noteGeneralUtil.selectionColor);
        newNoteField.setBounds(150,80,300,40);
        newNoteFrame.getContentPane().add(newNoteField);

        JLabel contentLabel = new JLabel("Contents");
        contentLabel.setFont(noteGeneralUtil.weatherFontSmall);
        contentLabel.setForeground(noteGeneralUtil.navy);
        contentLabel.setBounds(245,140,150,40);
        newNoteFrame.getContentPane().add(contentLabel);

        newNoteArea = new JTextArea(20,20);
        newNoteArea.setFont(noteGeneralUtil.weatherFontSmall);
        newNoteArea.setAutoscrolls(false);
        newNoteArea.setLineWrap(true);
        newNoteArea.setWrapStyleWord(true);
        newNoteArea.setSelectedTextColor(noteGeneralUtil.selectionColor);
        newNoteArea.setBorder(new LineBorder(new Color(0, 0, 0)));

        CyderScrollPane NewNoteScroll = new CyderScrollPane(newNoteArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        NewNoteScroll.setThumbColor(noteGeneralUtil.regularRed);
        NewNoteScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        NewNoteScroll.getViewport().setBorder(null);
        NewNoteScroll.setViewportBorder(null);

        NewNoteScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(noteGeneralUtil.navy,5,false)));
        NewNoteScroll.setBounds(50,180,600 - 50 - 50,380);
        newNoteFrame.getContentPane().add(NewNoteScroll);

        CyderButton submitNewNote = new CyderButton("Create Note");
        submitNewNote.setBorder(new LineBorder(noteGeneralUtil.navy,5,false));
        submitNewNote.setFocusPainted(false);
        submitNewNote.setColors(noteGeneralUtil.regularRed);
        submitNewNote.setBackground(noteGeneralUtil.regularRed);
        submitNewNote.setFont(noteGeneralUtil.weatherFontSmall);
        submitNewNote.addActionListener(e -> {
            try {
                BufferedWriter NoteWriter = new BufferedWriter(new FileWriter(
                        "src/users/" + UUID + "/Notes/" + newNoteField.getText() + ".txt",true));
                newNoteArea.write(NoteWriter);
                NoteWriter.close();
            }

            catch (Exception ex) {
                noteGeneralUtil.handle(ex);
            }

            noteGeneralUtil.closeAnimation(newNoteFrame);

            initializeNotesList();

            noteListScroll.setViewportView(fileSelectionList);
            noteListScroll.revalidate();
        });
        submitNewNote.setBounds(50,180 + 390,600 - 50 - 50,40);
        newNoteFrame.getContentPane().add(submitNewNote);

        newNoteFrame.setLocationRelativeTo(null);
        newNoteFrame.setVisible(true);
        newNoteField.requestFocus();
    }

    private void initializeNotesList() {
        File dir = new File("src/users/" + UUID + "/Notes");
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

        fileSelectionList.setFont(noteGeneralUtil.weatherFontSmall);

        fileSelectionList.setForeground(noteGeneralUtil.navy);

        fileSelectionList.setSelectionBackground(noteGeneralUtil.selectionColor);
    }

    private void openNote(File File) {
        if (noteEditorFrame != null)
            noteGeneralUtil.closeAnimation(noteEditorFrame);

        noteEditorFrame = newNoteFrame = new CyderFrame(600,625, new ImageIcon("src/com/cyder/io/pictures/DebugBackground.png"));
        noteEditorFrame.setTitle("Editing note: " + File.getName().replace(".txt", ""));

        noteEditField = new JTextField(20);
        noteEditField.setToolTipText("Change Name");
        noteEditField.setSelectionColor(noteGeneralUtil.selectionColor);
        noteEditField.setText(File.getName().replaceFirst(".txt",""));
        noteEditField.setFont(noteGeneralUtil.weatherFontSmall);
        noteEditField.setForeground(noteGeneralUtil.navy);
        noteEditField.setBorder(new LineBorder(noteGeneralUtil.navy,5,false));
        noteEditField.setBounds(50,50,600 - 50 - 50, 40);
        noteEditorFrame.getContentPane().add(noteEditField);

        noteEditArea = new JTextArea(20, 20);
        noteEditArea.setSelectedTextColor(noteGeneralUtil.selectionColor);
        noteEditArea.setFont(noteGeneralUtil.weatherFontSmall);
        noteEditArea.setForeground(noteGeneralUtil.navy);
        noteEditArea.setEditable(true);
        noteEditArea.setAutoscrolls(true);
        noteEditArea.setLineWrap(true);
        noteEditArea.setWrapStyleWord(true);
        noteEditArea.setFocusable(true);

        CyderScrollPane noteScroll = new CyderScrollPane(noteEditArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        noteScroll.setThumbColor(noteGeneralUtil.regularRed);
        noteScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        noteScroll.getViewport().setBorder(null);
        noteScroll.setViewportBorder(null);
        noteScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(noteGeneralUtil.navy,5,false)));
        noteScroll.setBounds(50,120,600 - 50 - 50, 400);
        noteEditorFrame.getContentPane().add(noteScroll);

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
            noteGeneralUtil.handle(e);
        }

        currentUserNote = File;

        CyderButton saveNote = new CyderButton("Save & Resign");
        saveNote.setColors(noteGeneralUtil.regularRed);
        saveNote.setBorder(new LineBorder(noteGeneralUtil.navy,5,false));
        saveNote.setFocusPainted(false);
        saveNote.setBackground(noteGeneralUtil.regularRed);
        saveNote.setFont(noteGeneralUtil.weatherFontSmall);
        saveNote.addActionListener(e -> {
            try {
                BufferedWriter SaveWriter = new BufferedWriter(new FileWriter(currentUserNote, false));
                SaveWriter.write(noteEditArea.getText());
                SaveWriter.close();

                File newName = null;

                if (noteEditField.getText().length() > 0) {
                    newName = new File(File.getAbsolutePath().replace(File.getName(),noteEditField.getText() + ".txt"));
                    File.renameTo(newName);
                    noteGeneralUtil.inform(newName.getName().replace(".txt", "") + " has been successfully saved","Saved", 400, 200);
                    initializeNotesList();
                    noteListScroll.setViewportView(fileSelectionList);
                    noteListScroll.revalidate();
                }

                else {
                    noteGeneralUtil.inform(currentUserNote.getName().replace(".txt", "") + " has been successfully saved","Saved", 400, 200);
                }

                noteGeneralUtil.closeAnimation(noteEditorFrame);
            }

            catch (Exception exc) {
                noteGeneralUtil.handle(exc);
            }
        });
        saveNote.setBounds(50,550,600 - 50 - 50, 40);
        noteEditorFrame.getContentPane().add(saveNote);

        noteEditorFrame.setVisible(true);
        noteEditArea.requestFocus();
        noteEditorFrame.setLocationRelativeTo(null);
    }
}
