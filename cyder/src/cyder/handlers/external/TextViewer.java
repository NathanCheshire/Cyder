package cyder.handlers.external;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.button.CyderButton;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.pane.CyderScrollPane;
import cyder.utils.ReflectionUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.*;

/**
 * A handler for viewing raw text.
 */
public class TextViewer {
    /**
     * The text viewing frame.
     */
    private CyderFrame textEditorFrame;

    /**
     * The field to change the name of the file.
     */
    private CyderTextField textNameEditField;

    /**
     * The area to edit and view the contents of the file in.
     */
    private JTextArea textEditArea;

    /**
     * The file currently being displayed.
     */
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
        textEditorFrame = new CyderFrame(600, 625, CyderIcons.defaultBackground);
        textEditorFrame.setTitle("Editing: " + file.getName().replace(".txt", ""));

        textNameEditField = new CyderTextField();
        textNameEditField.setHorizontalAlignment(JTextField.CENTER);
        textNameEditField.setBackground(Color.white);
        textNameEditField.setToolTipText("Change Name");
        textNameEditField.setText(file.getName().replaceFirst(".txt", ""));
        textNameEditField.setBounds(50, 50, 600 - 50 - 50, 40);
        textEditorFrame.getContentPane().add(textNameEditField);

        textEditArea = new JTextArea(20, 20);
        textEditArea.setSelectionColor(CyderColors.selectionColor);
        textEditArea.setFont(CyderFonts.SEGOE_20);
        textEditArea.setForeground(CyderColors.navy);
        textEditArea.setEditable(true);
        textEditArea.setAutoscrolls(true);
        textEditArea.setLineWrap(true);
        textEditArea.setWrapStyleWord(true);
        textEditArea.setFocusable(true);

        CyderScrollPane textScroll = new CyderScrollPane(textEditArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        textScroll.setBorder(new LineBorder(CyderColors.navy, 5, false));
        textScroll.setBounds(50, 120, 500, 400);
        textEditorFrame.getContentPane().add(textScroll);

        try {
            BufferedReader textReader = new BufferedReader(new FileReader(file));
            String line = textReader.readLine();

            while (line != null) {
                textEditArea.append(line + "\n");
                line = textReader.readLine();
            }

            textReader.close();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        CyderButton saveText = new CyderButton("Save & Resign");
        saveText.setBorder(new LineBorder(CyderColors.navy, 5, false));
        saveText.setFocusPainted(false);
        saveText.setBackground(CyderColors.regularRed);
        saveText.setFont(CyderFonts.SEGOE_20);
        saveText.addActionListener(e -> {
            try {
                BufferedWriter SaveWriter = new BufferedWriter(new FileWriter(file, false));
                SaveWriter.write(textEditArea.getText());
                SaveWriter.close();

                File newName;

                if (!textNameEditField.getText().isEmpty()) {
                    newName = new File(
                            file.getAbsolutePath().replace(file.getName(), textNameEditField.getText() + ".txt"));
                    if (file.renameTo(newName)) {
                        textEditorFrame.notify(newName.getName().replace(".txt", "")
                                + " has been successfully saved");
                    } else {
                        textEditorFrame.notify("Could not rename file at this time");
                    }
                } else {
                    textEditorFrame.notify(file.getName().replace(".txt", "")
                            + " has been successfully saved");
                }

                textEditorFrame.dispose();
            } catch (Exception exc) {
                ExceptionHandler.handle(exc);
            }
        });
        saveText.setBounds(50, 550, 600 - 50 - 50, 40);
        textEditorFrame.getContentPane().add(saveText);

        textEditorFrame.finalizeAndShow();
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
