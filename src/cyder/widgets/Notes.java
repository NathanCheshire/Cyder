package cyder.widgets;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollPane;
import cyder.utilities.StringUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

import static cyder.constants.CyderStrings.DEFAULT_BACKGROUND_PATH;

public class Notes {
    private static CyderFrame noteEditorFrame;
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

    private static String UUID = "";

    public Notes() {
        this.UUID = ConsoleFrame.getUUID();

        if (UUID == null)
            return;

        if (noteFrame != null)
            noteFrame.closeAnimation();

        noteFrame = new CyderFrame(600,625, new ImageIcon(DEFAULT_BACKGROUND_PATH));
        noteFrame.setTitle(ConsoleFrame.getUsername() + StringUtil.getApostrophe(ConsoleFrame.getUsername()) + " notes");

        initializeNotesList();

        fileSelectionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        fileSelectionList.setFont(CyderFonts.weatherFontSmall);
        fileSelectionList.setForeground(CyderColors.navy);
        fileSelectionList.setSelectionBackground(CyderColors.selectionColor);
        noteListScroll = new CyderScrollPane(fileSelectionList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        noteListScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(CyderColors.navy,5,false)));

        noteListScroll.setThumbColor(CyderColors.regularRed);
        noteListScroll.setFont(CyderFonts.weatherFontSmall);
        noteListScroll.setForeground(CyderColors.navy);
        noteListScroll.setBounds(40,40,600 - 80, 700 - 100 - 100);
        noteFrame.getContentPane().add(noteListScroll);

        CyderButton addNote = new CyderButton("Add Note");
        addNote.setColors(CyderColors.regularRed);
        addNote.setBorder(new LineBorder(CyderColors.navy,5,false));
        addNote.setFocusPainted(false);
        addNote.setBackground(CyderColors.regularRed);
        addNote.setFont(CyderFonts.weatherFontSmall);
        addNote.addActionListener(e -> addNote());
        addNote.setBounds(50,550,150,50);
        noteFrame.getContentPane().add(addNote);

        openNote = new CyderButton("Open Note");
        openNote.setColors(CyderColors.regularRed);
        openNote.setFocusPainted(false);
        openNote.setBorder(new LineBorder(CyderColors.navy,5,false));
        openNote.setBackground(CyderColors.regularRed);
        openNote.setFont(CyderFonts.weatherFontSmall);
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
        deleteNote.setColors(CyderColors.regularRed);
        deleteNote.setBorder(new LineBorder(CyderColors.navy,5,false));
        deleteNote.setFocusPainted(false);
        deleteNote.setBackground(CyderColors.regularRed);
        deleteNote.setFont(CyderFonts.weatherFontSmall);
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
            newNoteFrame.closeAnimation();

        newNoteFrame = new CyderFrame(600,625, new ImageIcon(DEFAULT_BACKGROUND_PATH));
        newNoteFrame.setTitle("New note");

        JLabel FileNameLabel = new JLabel("Note Title");
        FileNameLabel.setFont(CyderFonts.weatherFontSmall);
        FileNameLabel.setForeground(CyderColors.navy);
        FileNameLabel.setBounds(240,40,150,40);
        newNoteFrame.getContentPane().add(FileNameLabel);

        newNoteField = new JTextField(30);
        newNoteField.setFont(CyderFonts.weatherFontSmall);
        newNoteField.setForeground(CyderColors.navy);
        newNoteField.setBorder(new LineBorder(CyderColors.navy,5,false));
        newNoteField.setSelectionColor(CyderColors.selectionColor);
        newNoteField.setBounds(150,80,300,40);
        newNoteFrame.getContentPane().add(newNoteField);

        JLabel contentLabel = new JLabel("Contents");
        contentLabel.setFont(CyderFonts.weatherFontSmall);
        contentLabel.setForeground(CyderColors.navy);
        contentLabel.setBounds(245,140,150,40);
        newNoteFrame.getContentPane().add(contentLabel);

        newNoteArea = new JTextArea(20,20);
        newNoteArea.setFont(CyderFonts.weatherFontSmall);
        newNoteArea.setAutoscrolls(false);
        newNoteArea.setLineWrap(true);
        newNoteArea.setWrapStyleWord(true);
        newNoteArea.setSelectedTextColor(CyderColors.selectionColor);
        newNoteArea.setBorder(new LineBorder(new Color(0, 0, 0)));

        CyderScrollPane NewNoteScroll = new CyderScrollPane(newNoteArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        NewNoteScroll.setThumbColor(CyderColors.regularRed);
        NewNoteScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        NewNoteScroll.getViewport().setBorder(null);
        NewNoteScroll.setViewportBorder(null);

        NewNoteScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(CyderColors.navy,5,false)));
        NewNoteScroll.setBounds(50,180,600 - 50 - 50,380);
        newNoteFrame.getContentPane().add(NewNoteScroll);

        CyderButton submitNewNote = new CyderButton("Create Note");
        submitNewNote.setBorder(new LineBorder(CyderColors.navy,5,false));
        submitNewNote.setFocusPainted(false);
        submitNewNote.setColors(CyderColors.regularRed);
        submitNewNote.setBackground(CyderColors.regularRed);
        submitNewNote.setFont(CyderFonts.weatherFontSmall);
        submitNewNote.addActionListener(e -> {
            try {
                BufferedWriter NoteWriter = new BufferedWriter(new FileWriter(
                        "users/" + UUID + "/Notes/" + newNoteField.getText() + ".txt",true));
                newNoteArea.write(NoteWriter);
                NoteWriter.close();
            }

            catch (Exception ex) {
                ErrorHandler.handle(ex);
            }

            newNoteFrame.closeAnimation();

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
        File dir = new File("users/" + UUID + "/Notes");
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

        fileSelectionList.setFont(CyderFonts.weatherFontSmall);

        fileSelectionList.setForeground(CyderColors.navy);

        fileSelectionList.setSelectionBackground(CyderColors.selectionColor);
    }

    private void openNote(File File) {
        if (noteEditorFrame != null)
            noteEditorFrame.closeAnimation();

        noteEditorFrame = newNoteFrame = new CyderFrame(600,625, new ImageIcon(DEFAULT_BACKGROUND_PATH));
        noteEditorFrame.setTitle("Editing note: " + File.getName().replace(".txt", ""));

        noteEditField = new JTextField(20);
        noteEditField.setToolTipText("Change Name");
        noteEditField.setSelectionColor(CyderColors.selectionColor);
        noteEditField.setText(File.getName().replaceFirst(".txt",""));
        noteEditField.setFont(CyderFonts.weatherFontSmall);
        noteEditField.setForeground(CyderColors.navy);
        noteEditField.setBorder(new LineBorder(CyderColors.navy,5,false));
        noteEditField.setBounds(50,50,600 - 50 - 50, 40);
        noteEditorFrame.getContentPane().add(noteEditField);

        noteEditArea = new JTextArea(20, 20);
        noteEditArea.setSelectedTextColor(CyderColors.selectionColor);
        noteEditArea.setFont(CyderFonts.weatherFontSmall);
        noteEditArea.setForeground(CyderColors.navy);
        noteEditArea.setEditable(true);
        noteEditArea.setAutoscrolls(true);
        noteEditArea.setLineWrap(true);
        noteEditArea.setWrapStyleWord(true);
        noteEditArea.setFocusable(true);

        CyderScrollPane noteScroll = new CyderScrollPane(noteEditArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        noteScroll.setThumbColor(CyderColors.regularRed);
        noteScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        noteScroll.getViewport().setBorder(null);
        noteScroll.setViewportBorder(null);
        noteScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(CyderColors.navy,5,false)));
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
            ErrorHandler.handle(e);
        }

        currentUserNote = File;

        CyderButton saveNote = new CyderButton("Save & Resign");
        saveNote.setColors(CyderColors.regularRed);
        saveNote.setBorder(new LineBorder(CyderColors.navy,5,false));
        saveNote.setFocusPainted(false);
        saveNote.setBackground(CyderColors.regularRed);
        saveNote.setFont(CyderFonts.weatherFontSmall);
        saveNote.addActionListener(e -> {
            try {
                BufferedWriter SaveWriter = new BufferedWriter(new FileWriter(currentUserNote, false));
                SaveWriter.write(noteEditArea.getText());
                SaveWriter.close();

                File newName = null;

                if (noteEditField.getText().length() > 0) {
                    newName = new File(File.getAbsolutePath().replace(File.getName(),noteEditField.getText() + ".txt"));
                    File.renameTo(newName);
                    GenericInform.inform(newName.getName().replace(".txt", "") + " has been successfully saved","Saved");
                    initializeNotesList();
                    noteListScroll.setViewportView(fileSelectionList);
                    noteListScroll.revalidate();
                }

                else {
                    GenericInform.inform(currentUserNote.getName().replace(".txt", "") + " has been successfully saved","Saved");
                }

                noteEditorFrame.closeAnimation();
            }

            catch (Exception exc) {
                ErrorHandler.handle(exc);
            }
        });
        saveNote.setBounds(50,550,600 - 50 - 50, 40);
        noteEditorFrame.getContentPane().add(saveNote);

        noteEditorFrame.setVisible(true);
        noteEditArea.requestFocus();
        noteEditorFrame.setLocationRelativeTo(null);
    }
}
