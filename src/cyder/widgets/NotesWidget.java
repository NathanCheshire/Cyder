package cyder.widgets;

import cyder.annotations.Widget;
import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.consts.CyderStrings;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ErrorHandler;
import cyder.ui.*;
import cyder.utilities.StringUtil;
import cyder.utilities.SystemUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class NotesWidget {
    //main frame
    private static CyderFrame noteFrame;
    private static CyderScrollList cyderScrollList;
    private static CyderButton openNote;
    private static List<String> noteNameList;
    private static List<File> noteList;
    private static JLabel noteScrollLabel;

    //note editor components
    private static JTextArea noteEditArea;
    private static CyderTextField noteEditField;
    private static File currentUserNote;
    private static CyderFrame newNoteFrame;
    private static CyderTextField newNoteField;
    private static JTextArea newNoteArea;

    private static LinkedList<CyderFrame> noteFrames;

    private NotesWidget() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    } //no objects

    @Widget(trigger = "note", description = "A note taking widget that can save and display multiple notes")
    public static void showGUI() {
        if (ConsoleFrame.getConsoleFrame().getUUID() == null)
            return;

        if (noteFrame != null)
            noteFrame.dispose();

        noteFrames = new LinkedList<>();

        noteFrame = new CyderFrame(600,625, CyderImages.defaultBackground);
        noteFrame.setTitle(ConsoleFrame.getConsoleFrame().getUsername() +
                StringUtil.getApostrophe(ConsoleFrame.getConsoleFrame().getUsername()) + " notes");

        initializeNotesList();

        cyderScrollList = new CyderScrollList(520, 500, CyderScrollList.SelectionPolicy.SINGLE);

        for (int i = 0 ; i < noteNameList.size() ; i++) {
            int finalI = i;
            class thisAction implements CyderScrollList.ScrollAction {
                @Override
                public void fire() {
                    openNote(noteList.get(finalI));
                }
            }

            thisAction action = new thisAction();
            cyderScrollList.addElement(noteNameList.get(i), action);
        }

        noteScrollLabel = cyderScrollList.generateScrollList();
        noteScrollLabel.setBounds(40,40,520, 510);
        noteFrame.getContentPane().add(noteScrollLabel);

        CyderButton addNote = new CyderButton("Add Note");
        addNote.setBorder(new LineBorder(CyderColors.navy,5,false));
        addNote.setFocusPainted(false);
        addNote.setBackground(CyderColors.regularRed);
        addNote.setFont(CyderFonts.weatherFontSmall);
        addNote.addActionListener(e -> addNote());
        addNote.setBounds(40,560,160,40);
        noteFrame.getContentPane().add(addNote);

        openNote = new CyderButton("Open Note");
        openNote.setFocusPainted(false);
        openNote.setBorder(new LineBorder(CyderColors.navy,5,false));
        openNote.setBackground(CyderColors.regularRed);
        openNote.setFont(CyderFonts.weatherFontSmall);
        openNote.addActionListener(e -> {
            LinkedList<String> selectedNames = cyderScrollList.getSelectedElements();

            if (selectedNames.size() == 0)
                return;

            String selectedName = selectedNames.get(0);

            for (int i = 0 ; i < noteNameList.size() ; i++) {
                if (noteNameList.get(i).equals(selectedName)) {
                    openNote(noteList.get(i));
                    break;
                }
            }
        });
        openNote.setBounds(220,560,160,40);
        noteFrame.getContentPane().add(openNote);

        CyderButton deleteNote = new CyderButton("Delete Note");
        deleteNote.setBorder(new LineBorder(CyderColors.navy,5,false));
        deleteNote.setFocusPainted(false);
        deleteNote.setBackground(CyderColors.regularRed);
        deleteNote.setFont(CyderFonts.weatherFontSmall);
        deleteNote.addActionListener(e -> {
            LinkedList<String> selectedNames = cyderScrollList.getSelectedElements();

            if (selectedNames.size() == 0)
                return;

            String selectedName = selectedNames.get(0);

            for (int i = 0 ; i < noteNameList.size() ; i++) {
                if (noteNameList.get(i).equals(selectedName)) {
                    for (CyderFrame cf : noteFrames) {
                        if (cf != null) {
                            if (cf.getTitle().contains(selectedName)) {
                                noteFrames.remove(cf);
                                cf.dispose();
                            }
                        }
                    }

                    SystemUtil.deleteFolder(noteList.get(i));
                    initializeNotesList();

                    cyderScrollList.removeAllElements();
                    noteFrame.remove(noteScrollLabel);

                    initializeNotesList();

                    for (int j = 0 ; j < noteNameList.size() ; j++) {
                        int finalJ = j;
                        class thisAction implements CyderScrollList.ScrollAction {
                            @Override
                            public void fire() {
                                openNote(noteList.get(finalJ));
                            }
                        }

                        thisAction action = new thisAction();
                        cyderScrollList.addElement(noteNameList.get(j), action);
                    }

                    noteScrollLabel = cyderScrollList.generateScrollList();
                    noteScrollLabel.setBounds(40,40,520, 500);
                    noteFrame.getContentPane().add(noteScrollLabel);
                    noteFrame.revalidate();
                    noteFrame.repaint();

                    break;
                }
            }
        });
        deleteNote.setBounds(400,560,160,40);
        noteFrame.getContentPane().add(deleteNote);

        noteFrame.setVisible(true);
        noteFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    private static void addNote() {
        if (newNoteFrame != null)
            newNoteFrame.dispose();

        newNoteFrame = new CyderFrame(600,625, CyderImages.defaultBackground);
        newNoteFrame.setTitle("New note");

        newNoteField = new CyderTextField(0);
        newNoteField.setToolTipText("Title");
        newNoteField.setBounds(40,50,510,40);
        newNoteFrame.getContentPane().add(newNoteField);

        newNoteArea = new JTextArea(20,20);
        newNoteArea.setFont(CyderFonts.weatherFontSmall);
        newNoteArea.setToolTipText("Note contents");
        newNoteArea.setAutoscrolls(false);
        newNoteArea.setLineWrap(true);
        newNoteArea.setWrapStyleWord(true);
        newNoteArea.setSelectionColor(CyderColors.selectionColor);
        newNoteArea.setBorder(new LineBorder(new Color(0, 0, 0)));

        CyderScrollPane NewNoteScroll = new CyderScrollPane(newNoteArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        NewNoteScroll.setThumbColor(CyderColors.regularRed);
        NewNoteScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        NewNoteScroll.getViewport().setBorder(null);
        NewNoteScroll.setViewportBorder(null);

        NewNoteScroll.setBorder(new LineBorder(CyderColors.navy,5,false));
        NewNoteScroll.setBounds(40,120,510,440);
        newNoteFrame.getContentPane().add(NewNoteScroll);

        CyderButton submitNewNote = new CyderButton("Create Note");
        submitNewNote.setBorder(new LineBorder(CyderColors.navy,5,false));
        submitNewNote.setFocusPainted(false);
        submitNewNote.setBackground(CyderColors.regularRed);
        submitNewNote.setFont(CyderFonts.weatherFontSmall);
        submitNewNote.addActionListener(e -> {
            if (newNoteField.getText().trim().length() == 0 || newNoteArea.getText().trim().length() == 0) {
                newNoteFrame.notify("Please enter both a title and contents");
                return;
            }

            try {
                BufferedWriter NoteWriter = new BufferedWriter(new FileWriter(
                        "dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Notes/" +
                                newNoteField.getText() + ".txt",true));
                newNoteArea.write(NoteWriter);
                NoteWriter.close();
            }

            catch (Exception ex) {
                ErrorHandler.handle(ex);
            }

            newNoteFrame.dispose();

            initializeNotesList();

            cyderScrollList.removeAllElements();
            noteFrame.remove(noteScrollLabel);

            for (int i = 0 ; i < noteNameList.size() ; i++) {
                int finalI = i;
                class thisAction implements CyderScrollList.ScrollAction {
                    @Override
                    public void fire() {
                        openNote(noteList.get(finalI));
                    }
                }

                thisAction action = new thisAction();
                cyderScrollList.addElement(noteNameList.get(i), action);
            }
            noteScrollLabel = cyderScrollList.generateScrollList();
            noteScrollLabel.setBounds(40, 40, 520, 500);
            noteFrame.getContentPane().add(noteScrollLabel);
            noteFrame.revalidate();
            noteFrame.repaint();
        });
        submitNewNote.setBounds(50,570,600 - 50 - 50,40);
        newNoteFrame.getContentPane().add(submitNewNote);

        newNoteFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
        newNoteFrame.setVisible(true);
        newNoteField.requestFocus();
    }

    private static void initializeNotesList() {
        File dir = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Notes");
        noteList = new LinkedList<>();
        noteNameList = new LinkedList<>();

        for (File file : dir.listFiles()) {
            if (StringUtil.getExtension(file.getName()).equalsIgnoreCase((".txt"))) {
                noteList.add(file.getAbsoluteFile());
                noteNameList.add(StringUtil.getFilename(file.getName()));
            }
        }
    }

    private static void openNote(File File) {
        CyderFrame noteEditorFrame = new CyderFrame(600,625, CyderImages.defaultBackground);
        noteEditorFrame.setTitle("Editing note: " + File.getName().replace(".txt", ""));

        noteEditField = new CyderTextField(0);
        noteEditField.setToolTipText("Change Name");
        noteEditField.setText(File.getName().replaceFirst(".txt",""));
        noteEditField.setBounds(50,50,600 - 50 - 50, 40);
        noteEditorFrame.getContentPane().add(noteEditField);

        noteEditArea = new JTextArea(20, 20);
        noteEditArea.setSelectionColor(CyderColors.selectionColor);
        noteEditArea.setFont(CyderFonts.weatherFontSmall);
        noteEditArea.setForeground(CyderColors.navy);
        noteEditArea.setEditable(true);
        noteEditArea.setAutoscrolls(true);
        noteEditArea.setLineWrap(true);
        noteEditArea.setWrapStyleWord(true);
        noteEditArea.setFocusable(true);
        noteEditArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                try {
                    BufferedReader sameReader = new BufferedReader(new FileReader(currentUserNote));
                    StringBuilder contents = new StringBuilder();
                    String line;

                    while ((line = sameReader.readLine()) != null)
                        contents.append(line).append("\n");

                    //add closing confirmation if changes are not saved
                    if (!noteEditArea.getText().contentEquals(contents))
                        noteEditorFrame.setClosingConfirmation("Are you sure you wish to exit? Any unsaved work will be lost.");
                    else
                        noteEditorFrame.removeClosingConfirmation();
                } catch (Exception ex) {
                    ErrorHandler.handle(ex);
                }
            }
        });

        CyderScrollPane noteScroll = new CyderScrollPane(noteEditArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        noteScroll.setThumbColor(CyderColors.regularRed);
        noteScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        noteScroll.getViewport().setBorder(null);
        noteScroll.setViewportBorder(null);
        noteScroll.setBorder(new LineBorder(CyderColors.navy,5,false));
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
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        currentUserNote = File;

        CyderButton saveNote = new CyderButton("Save");
        saveNote.setBorder(new LineBorder(CyderColors.navy,5,false));
        saveNote.setFocusPainted(false);
        saveNote.setBackground(CyderColors.regularRed);
        saveNote.setFont(CyderFonts.weatherFontSmall);
        saveNote.addActionListener(e -> {
            try {
                BufferedReader sameReader = new BufferedReader(new FileReader(currentUserNote));
                StringBuilder contents = new StringBuilder();
                String line;

                while ((line = sameReader.readLine()) != null)
                    contents.append(line).append("\n");

                sameReader.close();

                //contents are equal and name is same so there is nothing to save so return
                if (noteEditArea.getText().contentEquals(contents) &&
                        noteEditField.getText().trim().equals(StringUtil.getFilename(currentUserNote))) {
                    noteEditorFrame.removeClosingConfirmation();
                    return;
                }

                BufferedWriter SaveWriter = new BufferedWriter(new FileWriter(currentUserNote, false));
                SaveWriter.write(noteEditArea.getText());
                SaveWriter.close();

                //saved so remove closing confirmation
                noteEditorFrame.removeClosingConfirmation();

                if (noteEditField.getText().length() > 0 && !StringUtil.getFilename(currentUserNote).equals(noteEditField.getText().trim())) {
                    File newName = new File(currentUserNote.getAbsolutePath().replace(
                            currentUserNote.getName(),noteEditField.getText().trim() + ".txt"));
                    boolean updated = File.renameTo(newName);

                    if (updated) {
                        noteEditorFrame.notify(newName.getName().replace(".txt", "") +
                                " has been successfully saved");
                    } else {
                         noteEditorFrame.notify("Could not rename note at this time");
                    }

                    initializeNotesList();

                    cyderScrollList.removeAllElements();
                    noteFrame.remove(noteScrollLabel);

                    for (int i = 0 ; i < noteNameList.size() ; i++) {
                        int finalI = i;
                        class thisAction implements CyderScrollList.ScrollAction {
                            @Override
                            public void fire() {
                                openNote(noteList.get(finalI));
                            }
                        }

                        thisAction action = new thisAction();
                        cyderScrollList.addElement(noteNameList.get(i), action);
                    }

                    noteScrollLabel = cyderScrollList.generateScrollList();
                    noteScrollLabel.setBounds(40, 40, 520, 500);
                    noteFrame.getContentPane().add(noteScrollLabel);
                    noteFrame.revalidate();
                    noteFrame.repaint();
                } else {
                    noteEditorFrame.notify(currentUserNote.getName().replace(".txt", "")
                            + " has been successfully saved");
                }
            }

            catch (Exception exc) {
                ErrorHandler.handle(exc);
            }
        });
        saveNote.setBounds(50,550,600 - 50 - 50, 40);
        noteEditorFrame.getContentPane().add(saveNote);

        noteEditorFrame.setVisible(true);
        noteEditArea.requestFocus();
        noteEditorFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());

        noteFrames.add(noteEditorFrame);
        noteEditorFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                noteFrames.remove(noteEditorFrame);
            }
        });
    }

    public void kill() {
        noteFrame.dispose();
    }
}
