package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.button.CyderButton;
import cyder.ui.field.CyderCaret;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.pane.CyderScrollList;
import cyder.ui.pane.CyderScrollPane;
import cyder.user.UserFile;
import cyder.user.UserUtil;
import cyder.utils.FileUtil;
import cyder.utils.OsUtil;
import cyder.utils.StringUtil;
import cyder.utils.UiUtil;

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

@Vanilla
@CyderAuthor
public class NotesWidget {
    /**
     * The notes selection and creation list frame.
     */
    private static CyderFrame noteFrame;

    /**
     * The list of notes.
     */
    private static CyderScrollList cyderScrollList;

    /**
     * The list of note names.
     */
    private static List<String> noteNameList;

    /**
     * The list of the note files.
     */
    private static List<File> noteList;

    /**
     * The generated label from the notes scroll list.
     */
    private static JLabel noteScrollLabel;

    //note editor components
    private static JTextArea noteEditArea;
    private static CyderTextField noteEditField;
    private static File currentUserNote;
    private static CyderFrame newNoteFrame;
    private static CyderTextField newNoteField;
    private static JTextArea newNoteArea;

    /**
     * The list of currently active note editor frames.
     */
    private static LinkedList<CyderFrame> noteFrames;

    /**
     * Suppress default constructor.
     */
    private NotesWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Widget(triggers = {"note", "notes"}, description
            = "A note taking widget that can save and display multiple notes")
    public static void showGui() {
        if (Console.INSTANCE.getUuid() == null)
            return;

        UiUtil.closeIfOpen(noteFrame);

        noteFrames = new LinkedList<>();

        noteFrame = new CyderFrame(600, 625, CyderIcons.defaultBackground);
        noteFrame.setTitle(UserUtil.getCyderUser().getName() +
                StringUtil.getApostrophe(UserUtil.getCyderUser().getName()) + " notes");

        initializeNotesList();

        cyderScrollList = new CyderScrollList(520, 500, CyderScrollList.SelectionPolicy.SINGLE);

        for (int i = 0 ; i < noteNameList.size() ; i++) {
            int finalI = i;
            cyderScrollList.addElement(noteNameList.get(i),
                    () -> openNote(noteList.get(finalI)));
        }

        noteScrollLabel = cyderScrollList.generateScrollList();
        noteScrollLabel.setBounds(40, 40, 520, 510);
        noteFrame.getContentPane().add(noteScrollLabel);

        CyderButton addNote = new CyderButton("Add Note");
        addNote.setBorder(new LineBorder(CyderColors.navy, 5, false));
        addNote.setFocusPainted(false);
        addNote.setBackground(CyderColors.regularRed);
        addNote.setFont(CyderFonts.SEGOE_20);
        addNote.addActionListener(e -> addNote());
        addNote.setBounds(40, 560, 160, 40);
        noteFrame.getContentPane().add(addNote);

        CyderButton openNote = new CyderButton("Open Note");
        openNote.setFocusPainted(false);
        openNote.setBorder(new LineBorder(CyderColors.navy, 5, false));
        openNote.setBackground(CyderColors.regularRed);
        openNote.setFont(CyderFonts.SEGOE_20);
        openNote.addActionListener(e -> {
            LinkedList<String> selectedNames = cyderScrollList.getSelectedElements();

            if (selectedNames.isEmpty())
                return;

            String selectedName = selectedNames.get(0);

            for (int i = 0 ; i < noteNameList.size() ; i++) {
                if (noteNameList.get(i).equals(selectedName)) {
                    openNote(noteList.get(i));
                    break;
                }
            }
        });
        openNote.setBounds(220, 560, 160, 40);
        noteFrame.getContentPane().add(openNote);

        CyderButton deleteNote = new CyderButton("Delete Note");
        deleteNote.setBorder(new LineBorder(CyderColors.navy, 5, false));
        deleteNote.setFocusPainted(false);
        deleteNote.setBackground(CyderColors.regularRed);
        deleteNote.setFont(CyderFonts.SEGOE_20);
        deleteNote.addActionListener(e -> {
            LinkedList<String> selectedNames = cyderScrollList.getSelectedElements();

            if (selectedNames.isEmpty())
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

                    OsUtil.deleteFile(noteList.get(i));
                    initializeNotesList();

                    cyderScrollList.removeAllElements();
                    noteFrame.remove(noteScrollLabel);

                    initializeNotesList();

                    for (int j = 0 ; j < noteNameList.size() ; j++) {
                        int finalJ = j;
                        cyderScrollList.addElement(noteNameList.get(j),
                                () -> openNote(noteList.get(finalJ)));
                    }

                    noteScrollLabel = cyderScrollList.generateScrollList();
                    noteScrollLabel.setBounds(40, 40, 520, 500);
                    noteFrame.getContentPane().add(noteScrollLabel);
                    noteFrame.revalidate();
                    noteFrame.repaint();

                    break;
                }
            }
        });
        deleteNote.setBounds(400, 560, 160, 40);
        noteFrame.getContentPane().add(deleteNote);

        noteFrame.finalizeAndShow();
    }

    private static void addNote() {
        UiUtil.closeIfOpen(newNoteFrame);

        newNoteFrame = new CyderFrame(600, 625, CyderIcons.defaultBackground);
        newNoteFrame.setTitle("New note");

        newNoteField = new CyderTextField(0);
        newNoteField.setHorizontalAlignment(JTextField.CENTER);
        newNoteField.setToolTipText("Title");
        newNoteField.setBounds(40, 50, 510, 40);
        newNoteFrame.getContentPane().add(newNoteField);

        newNoteArea = new JTextArea(20, 20);
        newNoteArea.setFont(CyderFonts.SEGOE_20);
        newNoteArea.setToolTipText("Note contents");
        newNoteArea.setAutoscrolls(false);
        newNoteArea.setLineWrap(true);
        newNoteArea.setWrapStyleWord(true);
        newNoteArea.setSelectionColor(CyderColors.selectionColor);
        newNoteArea.setBorder(new LineBorder(new Color(0, 0, 0)));
        newNoteArea.setCaret(new CyderCaret(CyderColors.navy));

        CyderScrollPane NewNoteScroll = new CyderScrollPane(newNoteArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        NewNoteScroll.setThumbColor(CyderColors.regularRed);
        NewNoteScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        NewNoteScroll.getViewport().setBorder(null);
        NewNoteScroll.setViewportBorder(null);

        NewNoteScroll.setBorder(new LineBorder(CyderColors.navy, 5, false));
        NewNoteScroll.setBounds(40, 120, 510, 440);
        newNoteFrame.getContentPane().add(NewNoteScroll);

        CyderButton submitNewNote = new CyderButton("Create Note");
        submitNewNote.setBorder(new LineBorder(CyderColors.navy, 5, false));
        submitNewNote.setFocusPainted(false);
        submitNewNote.setBackground(CyderColors.regularRed);
        submitNewNote.setFont(CyderFonts.SEGOE_20);
        submitNewNote.addActionListener(e -> {
            if (newNoteField.getText().trim().isEmpty() || newNoteArea.getText().trim().isEmpty()) {
                newNoteFrame.notify("Please enter both a title and contents");
                return;
            }

            try {
                BufferedWriter NoteWriter = new BufferedWriter(
                        new FileWriter(OsUtil.buildFile(
                                Dynamic.PATH,
                                Dynamic.USERS.getDirectoryName(),
                                Console.INSTANCE.getUuid(),
                                UserFile.NOTES.getName(),
                                newNoteField.getText().trim() + ".txt"), true));
                newNoteArea.write(NoteWriter);
                NoteWriter.close();
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }

            newNoteFrame.dispose();

            initializeNotesList();

            cyderScrollList.removeAllElements();
            noteFrame.remove(noteScrollLabel);

            for (int i = 0 ; i < noteNameList.size() ; i++) {
                int finalI = i;
                cyderScrollList.addElement(noteNameList.get(i),
                        () -> openNote(noteList.get(finalI)));
            }
            noteScrollLabel = cyderScrollList.generateScrollList();
            noteScrollLabel.setBounds(40, 40, 520, 500);
            noteFrame.getContentPane().add(noteScrollLabel);
            noteFrame.revalidate();
            noteFrame.repaint();
        });
        submitNewNote.setBounds(50, 570, 600 - 50 - 50, 40);
        newNoteFrame.getContentPane().add(submitNewNote);

        newNoteFrame.finalizeAndShow();
        newNoteField.requestFocus();
    }

    /**
     * Initializes the notes list.
     *
     * @throws IllegalStateException if the notes parent directory DNE
     */
    private static void initializeNotesList() throws IllegalStateException {
        File dir = OsUtil.buildFile(
                Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(),
                Console.INSTANCE.getUuid(),
                UserFile.NOTES.getName());

        if (!dir.exists()) {
            throw new IllegalStateException("Parent note directory not found");
        }

        noteList = new LinkedList<>();
        noteNameList = new LinkedList<>();

        File[] files = dir.listFiles();

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (FileUtil.getExtension(file.getName()).equalsIgnoreCase((".txt"))) {
                    noteList.add(file.getAbsoluteFile());
                    noteNameList.add(FileUtil.getFilename(file.getName()));
                }
            }
        }
    }

    private static void openNote(File File) {
        CyderFrame noteEditorFrame = new CyderFrame(600, 625, CyderIcons.defaultBackground);
        noteEditorFrame.setTitle("Editing note: " + File.getName().replace(".txt", ""));

        noteEditField = new CyderTextField(0);
        noteEditField.setHorizontalAlignment(JTextField.CENTER);
        noteEditField.setToolTipText("Change Name");
        noteEditField.setText(File.getName().replaceFirst(".txt", ""));
        noteEditField.setBounds(50, 50, 600 - 50 - 50, 40);
        noteEditorFrame.getContentPane().add(noteEditField);

        noteEditArea = new JTextArea(20, 20);
        noteEditArea.setSelectionColor(CyderColors.selectionColor);
        noteEditArea.setFont(CyderFonts.SEGOE_20);
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
                        noteEditorFrame.setClosingConfirmation("Are you sure you wish to exit?" +
                                " Any unsaved work will be lost.");
                    else
                        noteEditorFrame.removeClosingConfirmation();
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                }
            }
        });
        noteEditArea.setCaret(new CyderCaret(CyderColors.navy));

        CyderScrollPane noteScroll = new CyderScrollPane(noteEditArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        noteScroll.setThumbColor(CyderColors.regularRed);
        noteScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        noteScroll.getViewport().setBorder(null);
        noteScroll.setViewportBorder(null);
        noteScroll.setBorder(new LineBorder(CyderColors.navy, 5, false));
        noteScroll.setBounds(50, 120, 600 - 50 - 50, 400);
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
            ExceptionHandler.handle(e);
        }

        currentUserNote = File;

        CyderButton saveNote = new CyderButton("Save");
        saveNote.setBorder(new LineBorder(CyderColors.navy, 5, false));
        saveNote.setFocusPainted(false);
        saveNote.setBackground(CyderColors.regularRed);
        saveNote.setFont(CyderFonts.SEGOE_20);
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
                        noteEditField.getText().trim().equals(FileUtil.getFilename(currentUserNote))) {
                    noteEditorFrame.removeClosingConfirmation();
                    return;
                }

                BufferedWriter SaveWriter = new BufferedWriter(new FileWriter(currentUserNote, false));
                SaveWriter.write(noteEditArea.getText());
                SaveWriter.close();

                //saved so remove closing confirmation
                noteEditorFrame.removeClosingConfirmation();

                if (!noteEditField.getText().isEmpty() && !FileUtil.getFilename(currentUserNote)
                        .equals(noteEditField.getText().trim())) {
                    File newName = new File(currentUserNote.getAbsolutePath().replace(
                            currentUserNote.getName(), noteEditField.getText().trim() + ".txt"));
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
                        cyderScrollList.addElement(noteNameList.get(i), () -> openNote(noteList.get(finalI)));
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
            } catch (Exception exc) {
                ExceptionHandler.handle(exc);
            }
        });
        saveNote.setBounds(50, 550, 600 - 50 - 50, 40);
        noteEditorFrame.getContentPane().add(saveNote);

        noteEditArea.requestFocus();
        noteEditorFrame.finalizeAndShow();

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
