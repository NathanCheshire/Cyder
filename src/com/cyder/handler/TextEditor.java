package com.cyder.handler;

import com.cyder.ui.CyderButton;
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

    private void openNote(File textFile) {
        if (noteEditorFrame != null)
            textGeneralUtil.closeAnimation(noteEditorFrame);

        noteEditorFrame = new JFrame();

        noteEditorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        noteEditorFrame.setUndecorated(false);

        noteEditorFrame.setTitle("Editing note: " + textFile.getName().replace(".txt", ""));

        noteEditorFrame.setResizable(false);

        noteEditorFrame.setIconImage(textGeneralUtil.getCyderIcon().getImage());

        JPanel ParentPanel = new JPanel();

        noteEditorFrame.setContentPane(ParentPanel);

        ParentPanel.setLayout(new BorderLayout());

        noteEditField = new JTextField(20);

        noteEditField.setToolTipText("Change Name");

        noteEditField.setSelectionColor(textGeneralUtil.selectionColor);

        noteEditField.setText(textFile.getName().replaceFirst(".txt",""));

        noteEditField.setFont(textGeneralUtil.weatherFontSmall);

        noteEditField.setForeground(textGeneralUtil.navy);

        noteEditField.setBorder(new LineBorder(textGeneralUtil.navy,5,false));

        ParentPanel.add(noteEditField, BorderLayout.PAGE_START);

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
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        noteScroll.setThumbColor(textGeneralUtil.regularRed);

        noteScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        noteScroll.getViewport().setBorder(null);

        noteScroll.setViewportBorder(null);

        noteScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(textGeneralUtil.navy,5,false)));

        noteScroll.setPreferredSize(new Dimension(1000,1000));

        ParentPanel.add(noteScroll, BorderLayout.CENTER);

        try {
            BufferedReader InitReader = new BufferedReader(new FileReader(textFile));
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
                BufferedWriter SaveWriter = new BufferedWriter(new FileWriter(textFile, false));
                SaveWriter.write(noteEditArea.getText());
                SaveWriter.close();

                File newName = null;

                if (noteEditField.getText().length() > 0) {
                    newName = new File(textFile.getAbsolutePath().replace(textFile.getName(),noteEditField.getText() + ".txt"));
                    textFile.renameTo(newName);
                    textGeneralUtil.inform(newName.getName().replace(".txt", "") + " has been successfully saved.","", 400, 200);
                }

                else {
                    textGeneralUtil.inform(textFile.getName().replace(".txt", "") + " has been successfully saved.","", 400, 200);
                }

                textGeneralUtil.closeAnimation(noteEditorFrame);
            }

            catch (Exception exc) {
                textGeneralUtil.handle(exc);
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
