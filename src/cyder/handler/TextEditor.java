package cyder.handler;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollPane;
import cyder.widgets.GenericInform;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.*;

import static cyder.constants.CyderStrings.DEFAULT_BACKGROUND_PATH;

public class TextEditor {

    private CyderFrame noteEditorFrame;
    private JTextField noteEditField;
    private JTextArea noteEditArea;

    public TextEditor(String filePath) {
        openTextFile(new File(filePath));
    }

    private void openTextFile(File File) {
        if (noteEditorFrame != null)
            noteEditorFrame.closeAnimation();

        noteEditorFrame = new CyderFrame(600,625, new ImageIcon(DEFAULT_BACKGROUND_PATH));
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
        //todo this border here is messed up
        noteEditArea.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
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

        CyderButton saveNote = new CyderButton("Save & Resign");
        saveNote.setColors(CyderColors.regularRed);
        saveNote.setBorder(new LineBorder(CyderColors.navy,5,false));
        saveNote.setFocusPainted(false);
        saveNote.setBackground(CyderColors.regularRed);
        saveNote.setFont(CyderFonts.weatherFontSmall);
        saveNote.addActionListener(e -> {
            try {
                BufferedWriter SaveWriter = new BufferedWriter(new FileWriter(File, false));
                SaveWriter.write(noteEditArea.getText());
                SaveWriter.close();

                File newName = null;

                if (noteEditField.getText().length() > 0) {
                    newName = new File(File.getAbsolutePath().replace(File.getName(),noteEditField.getText() + ".txt"));
                    File.renameTo(newName);
                    GenericInform.inform(newName.getName().replace(".txt", "") + " has been successfully saved","Saved");
                }

                else {
                    GenericInform.inform(File.getName().replace(".txt", "") + " has been successfully saved","Saved");
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
