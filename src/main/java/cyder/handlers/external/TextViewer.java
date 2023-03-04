package cyder.handlers.external;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.enumerations.Extension;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.layouts.CyderPartitionedLayout;
import cyder.strings.CyderStrings;
import cyder.ui.button.CyderButton;
import cyder.ui.field.CyderCaret;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.pane.CyderScrollPane;
import cyder.utils.OsUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A handler for viewing text files.
 */
public class TextViewer {
    /**
     * The file currently being displayed.
     */
    private File file;

    /**
     * The default frame width.
     */
    private static final int defaultFrameWidth = 600;

    /**
     * The default frame height.
     */
    private static final int defaultFrameHeight = 680;

    /**
     * The font for the name field.
     */
    private static final Font nameFieldFont = new Font("Agency FB", Font.BOLD, 26);

    /**
     * The border for the name field.
     */
    private static final Border nameFieldBorder
            = BorderFactory.createMatteBorder(0, 0, 4, 0, CyderColors.navy);

    /**
     * The padding between the frame and the contents scrolls.
     */
    private static final int scrollPadding = 25;

    /**
     * The length of the scroll.
     */
    private static final int scrollLength = defaultFrameWidth - 2 * scrollPadding;

    /**
     * The height of the scroll.
     */
    private static final int scrollHeight = scrollLength - 50;

    /**
     * The save button text.
     */
    private static final String SAVE = "Save";

    /**
     * The file contents area.
     */
    private static JTextPane contentsArea;

    /**
     * The file name field.
     */
    private static CyderTextField nameField;

    /**
     * The frame for this text editor.
     */
    private CyderFrame textFrame;

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
     *
     * @return whether the gui opened successfully
     */
    @CanIgnoreReturnValue
    public boolean showGui() {
        textFrame = new CyderFrame.Builder()
                .setWidth(defaultFrameWidth)
                .setHeight(defaultFrameHeight)
                .build();

        nameField = new CyderTextField();
        nameField.setFont(nameFieldFont);
        nameField.setForeground(CyderColors.navy);
        nameField.setCaret(new CyderCaret(CyderColors.navy));
        nameField.setBackground(CyderColors.vanilla);
        nameField.setSize(300, 40);
        nameField.setToolTipText("Filename");
        nameField.setBorder(nameFieldBorder);
        nameField.setText(FileUtil.getFilename(file));

        contentsArea = new JTextPane();
        contentsArea.setText(getCurrentFileContents());
        contentsArea.setSize(scrollLength, scrollHeight);
        contentsArea.setBackground(CyderColors.vanilla);
        contentsArea.setBorder(new LineBorder(CyderColors.navy, 5));
        contentsArea.setFocusable(true);
        contentsArea.setEditable(true);
        contentsArea.setSelectionColor(CyderColors.selectionColor);
        contentsArea.setFont(Console.INSTANCE.generateUserFont());
        contentsArea.setForeground(CyderColors.navy);
        contentsArea.setCaret(new CyderCaret(CyderColors.navy));
        contentsArea.setCaretColor(contentsArea.getForeground());

        CyderScrollPane contentsScroll = new CyderScrollPane(contentsArea,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        contentsScroll.setThumbColor(CyderColors.regularPink);
        contentsScroll.setSize(scrollLength, scrollHeight);
        contentsScroll.getViewport().setOpaque(false);
        contentsScroll.setOpaque(false);
        contentsScroll.setBorder(null);
        contentsArea.setAutoscrolls(true);

        CyderButton saveButton = new CyderButton(SAVE);
        saveButton.setSize(new Dimension(contentsScroll.getWidth(), 40));
        saveButton.addActionListener(e -> saveButtonAction());

        CyderPartitionedLayout textLayout = new CyderPartitionedLayout();
        textLayout.spacer(2);
        textLayout.addComponentMaintainSize(nameField);
        textLayout.spacer(2);
        textLayout.addComponentMaintainSize(contentsScroll);
        textLayout.spacer(2);
        textLayout.addComponentMaintainSize(saveButton);
        textLayout.spacer(2);

        revalidateTitle();
        textFrame.setCyderLayout(textLayout);
        textFrame.finalizeAndShow();

        return true;
    }

    /**
     * Reads and returns the contents of the file.
     *
     * @return the contents of the file
     */
    private String getCurrentFileContents() {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new IllegalStateException("Could not read contents of current note file");
    }

    /**
     * The actions to invoke when the save button is pressed.
     */
    private void saveButtonAction() {
        String nameContents = nameField.getTrimmedText();
        if (nameContents.toLowerCase().endsWith(Extension.TXT.getExtension())) {
            nameContents = nameContents.substring(0, nameContents.length() - Extension.TXT.getExtension().length());
        }

        String requestedName = nameContents + Extension.TXT.getExtension();

        if (!FileUtil.isValidFilename(requestedName)) {
            textFrame.notify("Invalid filename");
            return;
        }

        File parent = file.getParentFile();
        File createFile = OsUtil.buildFile(parent.getAbsolutePath(), requestedName);
        if (!OsUtil.createFile(createFile, true)) {
            textFrame.notify("Could not create file: \"" + requestedName + CyderStrings.quote);
        }
        if (!OsUtil.deleteFile(file)) {
            textFrame.notify("Could not update contents of file");
        }

        file = createFile;

        String contents = contentsArea.getText();
        try (BufferedWriter write = new BufferedWriter(new FileWriter(file))) {
            write.write(contents);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        textFrame.notify("Saved file and contents under: \"" + requestedName + CyderStrings.quote);
    }

    /**
     * Revalidates the title of the frame.
     */
    private void revalidateTitle() {
        textFrame.setTitle(FileUtil.getFilename(file));
    }

    /**
     * Saves the contents to the file.
     *
     * @param contents the contents to save.
     */
    private void saveContentsToFile(String contents) {
        Preconditions.checkNotNull(contents);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(contents);
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }
}
