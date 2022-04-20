package cyder.handlers.external;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollPane;
import cyder.ui.CyderTextField;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.*;

public class TextViewer {
    private CyderFrame textEditorFrame;
    private CyderTextField textNameEditField;
    private JTextArea textEditArea;
    private final File file;

    /**
     * Returns a new text viewer instance to view the provided file.
     *
     * @param file the file to view
     * @return a text viewer instance
     */
    public static TextViewer getInstance(File file) {
        return new TextViewer(file);
    }

    /**
     * Constructs a new text viewer with the provided file.
     *
     * @param file the file to display for the text file
     */
    private TextViewer(File file) {
        this.file = file;
    }

    /**
     * Opens the text viewer gui.
     */
    public void showGui() {
        textEditorFrame = new CyderFrame(600,625, CyderIcons.defaultBackground);
        textEditorFrame.setTitle("Editing: " + file.getName().replace(".txt", ""));

        textNameEditField = new CyderTextField(0);
        textNameEditField.setHorizontalAlignment(JTextField.CENTER);
        textNameEditField.setBackground(Color.white);
        textNameEditField.setToolTipText("Change Name");
        textNameEditField.setText(file.getName().replaceFirst(".txt",""));
        textNameEditField.setBounds(50,50,600 - 50 - 50, 40);
        textEditorFrame.getContentPane().add(textNameEditField);

        textEditArea = new JTextArea(20, 20);
        textEditArea.setSelectionColor(CyderColors.selectionColor);
        textEditArea.setFont(CyderFonts.segoe20);
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
            BufferedReader textReader = new BufferedReader(new FileReader(file));
            String line = textReader.readLine();

            while (line != null) {
                textEditArea.append(line + "\n");
                line = textReader.readLine();
            }

            textReader.close();
        }

        catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        CyderButton saveText = new CyderButton("Save & Resign");
        saveText.setBorder(new LineBorder(CyderColors.navy,5,false));
        saveText.setFocusPainted(false);
        saveText.setBackground(CyderColors.regularRed);
        saveText.setFont(CyderFonts.segoe20);
        saveText.addActionListener(e -> {
            try {
                BufferedWriter SaveWriter = new BufferedWriter(new FileWriter(file, false));
                SaveWriter.write(textEditArea.getText());
                SaveWriter.close();

                File newName = null;

                if (!textNameEditField.getText().isEmpty()) {
                    newName = new File(file.getAbsolutePath().replace(file.getName(), textNameEditField.getText() + ".txt"));
                    file.renameTo(newName);
                    textEditorFrame.notify(newName.getName().replace(".txt", "")
                            + " has been successfully saved");
                }

                else {
                    textEditorFrame.notify(file.getName().replace(".txt", "")
                            + " has been successfully saved");
                }

                textEditorFrame.dispose();
            }

            catch (Exception exc) {
                ExceptionHandler.handle(exc);
            }
        });
        saveText.setBounds(50,550,600 - 50 - 50, 40);
        textEditorFrame.getContentPane().add(saveText);

        textEditorFrame.finalizeAndShow();
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
