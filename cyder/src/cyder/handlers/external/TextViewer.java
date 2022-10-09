package cyder.handlers.external;

import com.google.common.base.Preconditions;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;

import javax.swing.*;
import java.io.File;

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
        this.file = Preconditions.checkNotNull(file);
    }

    /**
     * Opens the text viewer gui.
     */
    public void showGui() {
        // todo copy from note widget
    }
}
