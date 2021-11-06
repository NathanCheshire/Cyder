package cyder.handlers.external;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ErrorHandler;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollPane;
import cyder.ui.CyderTextField;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.*;

public class TextViewer {

    private CyderFrame textEditorFrame;
    private CyderTextField textNameEditField;
    private JTextArea textEditArea;

    public TextViewer(String filePath) {
        openTextFile(new File(filePath));
    }

    private void openTextFile(File File) {
        if (textEditorFrame != null)
            textEditorFrame.dispose();

        textEditorFrame = new CyderFrame(600,625, CyderImages.defaultBackground);
        textEditorFrame.setTitle("Editing: " + File.getName().replace(".txt", ""));

        textNameEditField = new CyderTextField(0);
        textNameEditField.setBackground(Color.white);
        textNameEditField.setToolTipText("Change Name");
        textNameEditField.setText(File.getName().replaceFirst(".txt",""));
        textNameEditField.setBounds(50,50,600 - 50 - 50, 40);
        textEditorFrame.getContentPane().add(textNameEditField);

        textEditArea = new JTextArea(20, 20);
        textEditArea.setSelectionColor(CyderColors.selectionColor);
        textEditArea.setFont(CyderFonts.weatherFontSmall);
        textEditArea.setForeground(CyderColors.navy);
        textEditArea.setEditable(true);
        textEditArea.setAutoscrolls(true);
        textEditArea.setLineWrap(true);
        textEditArea.setWrapStyleWord(true);
        textEditArea.setFocusable(true);

        CyderScrollPane textScroll = new CyderScrollPane(textEditArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        textScroll.setBorder(new LineBorder(CyderColors.navy,5,false));
        textScroll.setBounds(50,120,500, 400);
        textEditorFrame.getContentPane().add(textScroll);

        try {
            BufferedReader textReader = new BufferedReader(new FileReader(File));
            String line = textReader.readLine();

            while (line != null) {
                textEditArea.append(line + "\n");
                line = textReader.readLine();
            }

            textReader.close();
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        CyderButton saveText = new CyderButton("Save & Resign");
        saveText.setBorder(new LineBorder(CyderColors.navy,5,false));
        saveText.setFocusPainted(false);
        saveText.setBackground(CyderColors.regularRed);
        saveText.setFont(CyderFonts.weatherFontSmall);
        saveText.addActionListener(e -> {
            try {
                BufferedWriter SaveWriter = new BufferedWriter(new FileWriter(File, false));
                SaveWriter.write(textEditArea.getText());
                SaveWriter.close();

                File newName = null;

                if (textNameEditField.getText().length() > 0) {
                    newName = new File(File.getAbsolutePath().replace(File.getName(), textNameEditField.getText() + ".txt"));
                    File.renameTo(newName);
                    textEditorFrame.notify(newName.getName().replace(".txt", "")
                            + " has been successfully saved");
                }

                else {
                    textEditorFrame.notify(File.getName().replace(".txt", "")
                            + " has been successfully saved");
                }

                textEditorFrame.dispose();
            }

            catch (Exception exc) {
                ErrorHandler.handle(exc);
            }
        });
        saveText.setBounds(50,550,600 - 50 - 50, 40);
        textEditorFrame.getContentPane().add(saveText);

        textEditorFrame.setVisible(true);
        textEditArea.requestFocus();
        textEditorFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    @Override
    public String toString() {
        return "TextEditor object, hash=" + this.hashCode();
    }
}
