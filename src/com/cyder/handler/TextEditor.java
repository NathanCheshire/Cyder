package com.cyder.handler;

import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;
import com.cyder.ui.CyderScrollPane;
import com.cyder.utilities.GeneralUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.*;

public class TextEditor {

    private JFrame noteEditorFrame;
    private JTextField noteEditField;
    private GeneralUtil textGeneralUtil = new GeneralUtil();
    private JTextArea noteEditArea;

    public TextEditor(String filePath) {
        openNote(new File(filePath));
    }

    private void openNote(File File) {
        if (noteEditorFrame != null)
            textGeneralUtil.closeAnimation(noteEditorFrame);

        noteEditorFrame = new CyderFrame(600,625, new ImageIcon("src/com/cyder/io/pictures/DebugBackground.png"));
        noteEditorFrame.setTitle("Editing note: " + File.getName().replace(".txt", ""));

        noteEditField = new JTextField(20);
        noteEditField.setToolTipText("Change Name");
        noteEditField.setSelectionColor(textGeneralUtil.selectionColor);
        noteEditField.setText(File.getName().replaceFirst(".txt",""));
        noteEditField.setFont(textGeneralUtil.weatherFontSmall);
        noteEditField.setForeground(textGeneralUtil.navy);
        noteEditField.setBorder(new LineBorder(textGeneralUtil.navy,5,false));
        noteEditField.setBounds(50,50,600 - 50 - 50, 40);
        noteEditorFrame.getContentPane().add(noteEditField);

        noteEditArea = new JTextArea(20, 20);
        noteEditArea.setSelectedTextColor(textGeneralUtil.selectionColor);
        noteEditArea.setFont(textGeneralUtil.weatherFontSmall);
        noteEditArea.setForeground(textGeneralUtil.navy);
        noteEditArea.setEditable(true);
        noteEditArea.setAutoscrolls(true);
        noteEditArea.setLineWrap(true);
        noteEditArea.setWrapStyleWord(true);
        noteEditArea.setFocusable(true);

        CyderScrollPane noteScroll = new CyderScrollPane(noteEditArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        noteScroll.setThumbColor(textGeneralUtil.regularRed);
        noteScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        noteScroll.getViewport().setBorder(null);
        noteScroll.setViewportBorder(null);
        noteScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(textGeneralUtil.navy,5,false)));
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
            textGeneralUtil.handle(e);
        }

        CyderButton saveNote = new CyderButton("Save & Resign");
        saveNote.setColors(textGeneralUtil.regularRed);
        saveNote.setBorder(new LineBorder(textGeneralUtil.navy,5,false));
        saveNote.setFocusPainted(false);
        saveNote.setBackground(textGeneralUtil.regularRed);
        saveNote.setFont(textGeneralUtil.weatherFontSmall);
        saveNote.addActionListener(e -> {
            try {
                BufferedWriter SaveWriter = new BufferedWriter(new FileWriter(File, false));
                SaveWriter.write(noteEditArea.getText());
                SaveWriter.close();

                File newName = null;

                if (noteEditField.getText().length() > 0) {
                    newName = new File(File.getAbsolutePath().replace(File.getName(),noteEditField.getText() + ".txt"));
                    File.renameTo(newName);
                    textGeneralUtil.inform(newName.getName().replace(".txt", "") + " has been successfully saved","Saved", 400, 200);
                }

                else {
                    textGeneralUtil.inform(File.getName().replace(".txt", "") + " has been successfully saved","Saved", 400, 200);
                }

                textGeneralUtil.closeAnimation(noteEditorFrame);
            }

            catch (Exception exc) {
                textGeneralUtil.handle(exc);
            }
        });
        saveNote.setBounds(50,550,600 - 50 - 50, 40);
        noteEditorFrame.getContentPane().add(saveNote);

        noteEditorFrame.setVisible(true);
        noteEditArea.requestFocus();
        noteEditorFrame.setLocationRelativeTo(null);
    }
}
