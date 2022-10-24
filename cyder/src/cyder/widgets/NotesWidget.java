package cyder.widgets;

import com.google.common.base.Preconditions;
import cyder.annotations.CyderAuthor;
import cyder.annotations.ForReadability;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.enums.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.getter.GetterUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.layouts.CyderGridLayout;
import cyder.layouts.CyderPartitionedLayout;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderPanel;
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
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A note taking widget.
 */
@Vanilla
@CyderAuthor
public final class NotesWidget {
    /**
     * The notes selection and creation list frame.
     */
    private static CyderFrame noteFrame;

    /**
     * The default frame width.
     */
    private static final int defaultFrameWidth = 600;

    /**
     * The default frame height.
     */
    private static final int defaultFrameHeight = 680;

    /**
     * The padding between the frame and the note contents scrolls.
     */
    private static final int noteScrollPadding = 25;

    /**
     * The notes list scroll.
     */
    private static CyderScrollList notesScrollList;

    /**
     * The length of the notes scroll.
     */
    private static final int noteScrollLength = defaultFrameWidth - 2 * noteScrollPadding;

    /**
     * The partitioned layout for the notes scroll view.
     */
    private static CyderPartitionedLayout framePartitionedLayout;

    /**
     * The current frame view.
     */
    private static View currentView;

    /**
     * The button size of most buttons.
     */
    private static final Dimension buttonSize = new Dimension(160, 40);

    /**
     * The new note contents area.
     */
    private static JTextPane newNoteArea;

    /**
     * The new note name field.
     */
    private static CyderTextField newNoteNameField;

    /**
     * The add button text.
     */
    private static final String ADD = "Add";

    /**
     * The open button text.
     */
    private static final String OPEN = "Open";

    /**
     * The delete button text.
     */
    private static final String DELETE = "Delete";

    /**
     * The possible widget views.
     */
    private enum View {
        LIST,
        ADD,
        EDIT,
    }

    /**
     * The description for this widget.
     */
    private static final String description = "A note taking widget that can save and display multiple notes";

    /**
     * The font for the note name fields.
     */
    private static final Font noteNameFieldFont = new Font("Agency FB", Font.BOLD, 26);

    /**
     * The border for the note name fields.
     */
    private static final Border noteNameFieldBorder
            = BorderFactory.createMatteBorder(0, 0, 4, 0, CyderColors.navy);

    /**
     * The height of the note scrolls.
     */
    private static final int noteScrollHeight = noteScrollLength - 50;

    /**
     * The back button text.
     */
    private static final String BACK = "Back";

    /**
     * The create button text.
     */
    private static final String CREATE = "Create";

    /**
     * The list of currently read notes from the user's notes directory.
     */
    private static final ArrayList<File> notesList = new ArrayList<>();

    /**
     * The exit text.
     */
    private static final String EXIT = "Exit";

    /**
     * The stay text (I told you that I never would).
     */
    private static final String STAY = "Stay";

    /**
     * The name of the thread which waits for a save confirmation.
     */
    private static final String NOTE_EDITOR_EXIT_CONFIRMATION_WAITER_THREAD_NAME = "Note editor exit confirmation";

    /**
     * The add note button text.
     */
    private static final String ADD_NOTE = "Add note";

    /**
     * The edit note name field.
     */
    private static CyderTextField editNoteNameField;

    /**
     * The edit note contents area.
     */
    private static JTextPane noteEditArea;

    /**
     * The save button text.
     */
    private static final String SAVE = "Save";

    /**
     * The currently being edited note file.
     */
    private static File currentNoteFile;

    /**
     * The confirmation message to display when a user attempts to close the frame when there are pending changes.
     */
    private static final String closingConfirmationMessage = "You have unsaved changes, are you sure you wish to exit?";

    /**
     * Whether the current note has unsaved changes.
     */
    private static boolean unsavedChanges = false;

    /**
     * Whether a new note being added has information that would be lost.
     */
    private static boolean newNoteContent = false;

    /**
     * The notification text to display when a note is saved.
     */
    private static final String SAVED_NOTE = "Saved note";

    /**
     * The last time the current note was saved at.
     */
    private static final AtomicLong lastSave = new AtomicLong();

    /**
     * The timeout between allowable note save actions.
     */
    private static final int SAVE_BUTTON_TIMEOUT = 500;

    /**
     * Suppress default constructor.
     */
    private NotesWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Widget(triggers = {"note", "notes"}, description = description)
    public static void showGui() {
        Preconditions.checkNotNull(Console.INSTANCE.getUuid());
        UiUtil.closeIfOpen(noteFrame);
        noteFrame = new CyderFrame(defaultFrameWidth, defaultFrameHeight);
        setupView(View.LIST);
        noteFrame.finalizeAndShow();
    }

    /**
     * Sets up the frame for the provided view.
     *
     * @param view the view
     */
    private static void setupView(View view) {
        Preconditions.checkNotNull(view);
        currentView = view;

        switch (view) {
            case LIST -> setupListView();
            case ADD -> setupAddView();
            case EDIT -> setupEditView();
        }
    }

    /**
     * Sets up and shows the notes list view.
     */
    private static void setupListView() {
        refreshNotesList();

        currentNoteFile = null;

        framePartitionedLayout = new CyderPartitionedLayout();

        JLabel notesLabel = regenerateAndGetNotesScrollLabel();

        CyderGridLayout buttonGridlayout = new CyderGridLayout(3, 1);

        CyderButton addButton = new CyderButton(ADD);
        addButton.setSize(buttonSize);
        addButton.addActionListener(e -> setupView(View.ADD));
        buttonGridlayout.addComponent(addButton);

        CyderButton openButton = new CyderButton(OPEN);
        openButton.setSize(buttonSize);
        openButton.addActionListener(e -> openButtonAction());
        buttonGridlayout.addComponent(openButton);

        CyderButton deleteButton = new CyderButton(DELETE);
        deleteButton.setSize(buttonSize);
        deleteButton.addActionListener(e -> deleteButtonAction());
        buttonGridlayout.addComponent(deleteButton);

        CyderPanel buttonPanel = new CyderPanel(buttonGridlayout);
        buttonPanel.setSize(notesLabel.getWidth(), 50);

        framePartitionedLayout.spacer(2);
        framePartitionedLayout.addComponentMaintainSize(notesLabel,
                CyderPartitionedLayout.PartitionAlignment.TOP);
        framePartitionedLayout.spacer(2);
        framePartitionedLayout.addComponent(buttonPanel);
        framePartitionedLayout.spacer(2);

        noteFrame.setCyderLayout(framePartitionedLayout);
        noteFrame.repaint();
        revalidateFrameTitle();
    }

    /**
     * Sets up and shows the add note view.
     */
    private static void setupAddView() {
        noteFrame.removeCyderLayoutPanel();
        noteFrame.repaint();

        newNoteNameField = new CyderTextField();
        newNoteNameField.addKeyListener(getAddNoteKeyListener());
        newNoteNameField.setFont(noteNameFieldFont);
        newNoteNameField.setForeground(CyderColors.navy);
        newNoteNameField.setCaret(new CyderCaret(CyderColors.navy));
        newNoteNameField.setBackground(CyderColors.vanilla);
        newNoteNameField.setSize(300, 40);
        newNoteNameField.setToolTipText("Note name");
        newNoteNameField.setBorder(noteNameFieldBorder);

        newNoteArea = new JTextPane();
        newNoteArea.addKeyListener(getAddNoteKeyListener());
        newNoteArea.setSize(noteScrollLength, noteScrollHeight);
        newNoteArea.setBackground(CyderColors.vanilla);
        newNoteArea.setBorder(new LineBorder(CyderColors.navy, 5));
        newNoteArea.setFocusable(true);
        newNoteArea.setEditable(true);
        newNoteArea.setSelectionColor(CyderColors.selectionColor);
        newNoteArea.setFont(Console.INSTANCE.generateUserFont());
        newNoteArea.setForeground(CyderColors.navy);
        newNoteArea.setCaret(new CyderCaret(CyderColors.navy));
        newNoteArea.setCaretColor(newNoteArea.getForeground());

        CyderScrollPane noteScroll = new CyderScrollPane(newNoteArea, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        noteScroll.setThumbColor(CyderColors.regularPink);
        noteScroll.setSize(noteScrollLength, noteScrollHeight);
        noteScroll.getViewport().setOpaque(false);
        noteScroll.setOpaque(false);
        noteScroll.setBorder(null);
        newNoteArea.setAutoscrolls(true);

        CyderButton backButton = new CyderButton(BACK);
        backButton.setSize(buttonSize);
        backButton.addActionListener(e -> setupView(View.LIST));

        CyderButton createButton = new CyderButton(CREATE);
        createButton.setSize(buttonSize);
        createButton.addActionListener(e -> createNoteAction());

        CyderGridLayout buttonGridLayout = new CyderGridLayout(2, 1);
        buttonGridLayout.addComponent(createButton);
        buttonGridLayout.addComponent(backButton);
        CyderPanel buttonGridPanel = new CyderPanel(buttonGridLayout);
        buttonGridPanel.setSize(400, 50);

        CyderPartitionedLayout addNoteLayout = new CyderPartitionedLayout();
        addNoteLayout.spacer(2);
        addNoteLayout.addComponentMaintainSize(newNoteNameField);
        addNoteLayout.spacer(2);
        addNoteLayout.addComponentMaintainSize(noteScroll);
        addNoteLayout.spacer(2);
        addNoteLayout.addComponentMaintainSize(buttonGridPanel);
        addNoteLayout.spacer(2);

        noteFrame.setCyderLayout(addNoteLayout);
        revalidateFrameTitle();
        noteFrame.repaint();
    }

    /**
     * Returns a key listener for the add note name field and content area.
     *
     * @return a key listener for the add note name field and content area
     */
    private static KeyListener getAddNoteKeyListener() {
        return new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                refreshNewNoteChanges();
            }
        };
    }

    /**
     * The actions to invoke when the open button is pressed.
     */
    private static void openButtonAction() {
        Optional<String> optionalFile = notesScrollList.getSelectedElement();
        if (optionalFile.isEmpty()) return;
        String noteName = optionalFile.get();

        notesList.stream().filter(noteFile -> noteFile.getName().equals(noteName))
                .forEach(noteFile -> currentNoteFile = noteFile);

        setupView(View.EDIT);
    }

    /**
     * The actions to invoke when the create new note button is clicked in the add note view.
     */
    private static void createNoteAction() {
        String noteName = newNoteNameField.getTrimmedText();
        if (noteName.toLowerCase().endsWith(Extension.TXT.getExtension())) {
            noteName = noteName.substring(0, noteName.length() - Extension.TXT.getExtension().length());
        }

        String requestedName = noteName + Extension.TXT.getExtension();

        if (!OsUtil.isValidFilename(requestedName)) {
            noteFrame.notify("Invalid filename");
            return;
        }

        File createFile = OsUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(),
                Console.INSTANCE.getUuid(), UserFile.NOTES.getName(), requestedName);
        if (!OsUtil.createFile(createFile, true)) {
            noteFrame.notify("Could not create file: \"" + requestedName + CyderStrings.quote);
        }

        String contents = newNoteArea.getText();
        try (BufferedWriter write = new BufferedWriter(new FileWriter(createFile))) {
            write.write(contents);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        setupView(View.LIST);
        noteFrame.notify("Added note file: \"" + requestedName + CyderStrings.quote);
    }

    /**
     * Reads and returns the contents of the current note file.
     *
     * @return the contents of the current note file
     */
    private static String getCurrentNoteContents() {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(currentNoteFile.getAbsolutePath()));
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new IllegalStateException("Could not read contents of current note file");
    }

    /**
     * Sets up and shows the edit note view.
     */
    private static void setupEditView() {
        Preconditions.checkNotNull(currentNoteFile);

        noteFrame.removeCyderLayoutPanel();
        noteFrame.repaint();

        editNoteNameField = new CyderTextField();
        editNoteNameField.addKeyListener(getEditNoteKeyListener());
        editNoteNameField.setFont(noteNameFieldFont);
        editNoteNameField.setForeground(CyderColors.navy);
        editNoteNameField.setCaret(new CyderCaret(CyderColors.navy));
        editNoteNameField.setBackground(CyderColors.vanilla);
        editNoteNameField.setSize(300, 40);
        editNoteNameField.setToolTipText("Note name");
        editNoteNameField.setBorder(noteNameFieldBorder);
        editNoteNameField.setText(FileUtil.getFilename(currentNoteFile));

        noteEditArea = new JTextPane();
        noteEditArea.addKeyListener(getEditNoteKeyListener());
        noteEditArea.setText(getCurrentNoteContents());
        noteEditArea.setSize(noteScrollLength, noteScrollHeight);
        noteEditArea.setBackground(CyderColors.vanilla);
        noteEditArea.setBorder(new LineBorder(CyderColors.navy, 5));
        noteEditArea.setFocusable(true);
        noteEditArea.setEditable(true);
        noteEditArea.setSelectionColor(CyderColors.selectionColor);
        noteEditArea.setFont(Console.INSTANCE.generateUserFont());
        noteEditArea.setForeground(CyderColors.navy);
        noteEditArea.setCaret(new CyderCaret(CyderColors.navy));
        noteEditArea.setCaretColor(noteEditArea.getForeground());

        CyderScrollPane noteScroll = new CyderScrollPane(noteEditArea,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        noteScroll.setThumbColor(CyderColors.regularPink);
        noteScroll.setSize(noteScrollLength, noteScrollHeight);
        noteScroll.getViewport().setOpaque(false);
        noteScroll.setOpaque(false);
        noteScroll.setBorder(null);
        noteEditArea.setAutoscrolls(true);

        CyderButton saveButton = new CyderButton(SAVE);
        saveButton.setSize(buttonSize);
        saveButton.addActionListener(e -> editNoteSaveButtonAction());
        lastSave.set(System.currentTimeMillis());

        CyderButton backButton = new CyderButton(BACK);
        backButton.setSize(buttonSize);
        backButton.addActionListener(e -> editBackButtonAction());

        CyderGridLayout buttonGridLayout = new CyderGridLayout(2, 1);
        buttonGridLayout.addComponent(saveButton);
        buttonGridLayout.addComponent(backButton);
        CyderPanel buttonGridPanel = new CyderPanel(buttonGridLayout);
        buttonGridPanel.setSize(400, 50);

        CyderPartitionedLayout editNoteLayout = new CyderPartitionedLayout();
        editNoteLayout.spacer(2);
        editNoteLayout.addComponentMaintainSize(editNoteNameField);
        editNoteLayout.spacer(2);
        editNoteLayout.addComponentMaintainSize(noteScroll);
        editNoteLayout.spacer(2);
        editNoteLayout.addComponentMaintainSize(buttonGridPanel);
        editNoteLayout.spacer(2);

        refreshUnsavedChanges();
        noteFrame.setCyderLayout(editNoteLayout);
        revalidateFrameTitle();
        noteFrame.repaint();
    }

    /**
     * Returns a key listener for the edit note name field and content area.
     *
     * @return a key listener for the edit note name field and content area
     */
    private static KeyListener getEditNoteKeyListener() {
        return new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                refreshUnsavedChanges();
            }
        };
    }

    /**
     * The actions to invoke when the save button on the edit note view is pressed.
     */
    private static void editNoteSaveButtonAction() {
        if (System.currentTimeMillis() < lastSave.get() + SAVE_BUTTON_TIMEOUT) return;
        lastSave.set(System.currentTimeMillis());

        String newFilename = editNoteNameField.getTrimmedText() + Extension.TXT.getExtension();
        if (!OsUtil.isValidFilename(newFilename)) {
            noteFrame.notify("Invalid filename: \"" + newFilename + CyderStrings.quote);
            return;
        }

        if (!OsUtil.deleteFile(currentNoteFile)) {
            noteFrame.notify("Failed to update note contents");
            return;
        }

        File newFile = OsUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(),
                Console.INSTANCE.getUuid(), UserFile.NOTES.getName(), newFilename);
        if (!OsUtil.createFile(newFile, true)) {
            noteFrame.notify("Failed to update note contents");
            return;
        }

        currentNoteFile = newFile;

        String contents = noteEditArea.getText();
        if (contents.isEmpty()) return;
        saveToCurrentNote(contents);
    }

    /**
     * Saves the provided contents to the current note file.
     *
     * @param contents the contents to save
     */
    private static void saveToCurrentNote(String contents) {
        Preconditions.checkNotNull(contents);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentNoteFile))) {
            writer.write(contents);
            noteFrame.notify(SAVED_NOTE);
            setUnsavedChanges(false);
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }

    /**
     * The action to invoke when the back button on the edit note view is pressed.
     */
    private static void editBackButtonAction() {
        CyderThreadRunner.submit(() -> {
            String currentName = editNoteNameField.getTrimmedText();
            String currentContents = noteEditArea.getText();

            StringBuilder pendingChangesBuilder = new StringBuilder();
            String currentlySavedName = FileUtil.getFilename(currentNoteFile);
            if (!currentName.equals(currentlySavedName)) {
                pendingChangesBuilder.append("Note name pending changes");
            }

            String currentSavedContents = getCurrentNoteContents();
            if (!currentContents.equals(currentSavedContents)) {
                if (!pendingChangesBuilder.isEmpty()) {
                    pendingChangesBuilder.append(", ");
                }
                pendingChangesBuilder.append("Note contents pending changes");
            }

            boolean pendingChanges = !pendingChangesBuilder.isEmpty();
            if (pendingChanges) {
                GetterUtil.Builder builder = new GetterUtil.Builder("Pending changes")
                        .setRelativeTo(noteFrame)
                        .setDisableRelativeTo(true)
                        .setInitialString(pendingChangesBuilder.toString())
                        .setYesButtonText(EXIT)
                        .setNoButtonText(STAY);
                boolean shouldGoBack = GetterUtil.getInstance().getConfirmation(builder);
                if (!shouldGoBack) return;
            }

            setupView(View.LIST);
        }, NOTE_EDITOR_EXIT_CONFIRMATION_WAITER_THREAD_NAME);
    }

    /**
     * Revalidates the frame title based on the current view.
     */
    private static void revalidateFrameTitle() {
        switch (currentView) {
            case LIST -> {
                String name = UserUtil.getCyderUser().getName();
                noteFrame.setTitle(name + StringUtil.getApostrophe(name) + " notes");
            }
            case ADD -> noteFrame.setTitle(ADD_NOTE);
            case EDIT -> {
                String name = currentNoteFile == null ? "" : FileUtil.getFilename(currentNoteFile);
                noteFrame.setTitle("Editing note: " + name);
            }
        }
    }

    /**
     * The actions to invoke when the delete button is pressed.
     */
    private static void deleteButtonAction() {
        Optional<String> selectedElement = notesScrollList.getSelectedElement();
        if (selectedElement.isEmpty()) return;

        notesList.stream().filter(noteFile ->
                noteFile.getName().equals(selectedElement.get())).forEach(OsUtil::deleteFile);
        refreshNotesList();
        notesScrollList.removeSelectedElement();

        JLabel notesLabel = regenerateAndGetNotesScrollLabel();
        framePartitionedLayout.setComponent(notesLabel, 1);
        noteFrame.repaint();
    }

    /**
     * Regenerates the notes scroll list and the JLabel and returns the generated component.
     *
     * @return the generated component
     */
    @ForReadability
    private static JLabel regenerateAndGetNotesScrollLabel() {
        notesScrollList = new CyderScrollList(noteScrollLength, noteScrollLength,
                CyderScrollList.SelectionPolicy.SINGLE);
        notesList.forEach(noteFile -> notesScrollList.addElement(noteFile.getName(), () -> {
            currentNoteFile = noteFile;
            setupView(View.EDIT);
        }));
        JLabel notesLabel = notesScrollList.generateScrollList();
        notesLabel.setSize(noteScrollLength, noteScrollLength);
        return notesLabel;
    }

    /**
     * Refreshes the contents of the notes list.
     */
    private static void refreshNotesList() throws IllegalStateException {
        File dir = OsUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(),
                Console.INSTANCE.getUuid(), UserFile.NOTES.getName());
        if (!dir.exists()) return;

        notesList.clear();

        File[] noteFiles = dir.listFiles();
        if (noteFiles == null) return;

        Arrays.stream(noteFiles).filter(noteFile ->
                FileUtil.getExtension(noteFile).equals(Extension.TXT.getExtension())).forEach(notesList::add);
    }

    /**
     * Refreshes the state of {@link #unsavedChanges}.
     */
    private static void refreshUnsavedChanges() {
        boolean filenameDifferent = !FileUtil.getFilename(currentNoteFile).equals(editNoteNameField.getText());
        boolean contentsDifferent = !getCurrentNoteContents().equals(noteEditArea.getText());
        setUnsavedChanges(contentsDifferent || filenameDifferent);
    }

    /**
     * Refreshes the state of {@link #newNoteContent}.
     */
    private static void refreshNewNoteChanges() {
        boolean filenameContents = !newNoteNameField.getTrimmedText().isEmpty();
        boolean contents = !newNoteArea.getText().isEmpty();
        setNewNoteContent(filenameContents || contents);
    }

    /**
     * Sets whether there are unsaved changes in the current note or note being created and thus
     * whether a closing confirmation should be displayed if the frame is attempted to be disposed.
     *
     * @param newUnsavedChangesValue whether there are unsaved changes.
     */
    private static void setUnsavedChanges(boolean newUnsavedChangesValue) {
        if (unsavedChanges == newUnsavedChangesValue) return;
        unsavedChanges = newUnsavedChangesValue;
        if (newNoteContent) return;

        if (newUnsavedChangesValue) {
            noteFrame.setClosingConfirmation(closingConfirmationMessage);
        } else {
            noteFrame.removeClosingConfirmation();
        }
    }

    /**
     * Sets whether there is an unsaved note with contents that would be lost if the frame was disposed
     * and thus whether a closing confirmation should be displayed if the frame is attempted to be disposed.
     *
     * @param newNoteContainsContent whether there is a new note present with unsaved changes
     */
    private static void setNewNoteContent(boolean newNoteContainsContent) {
        if (newNoteContent == newNoteContainsContent) return;
        newNoteContent = newNoteContainsContent;
        if (unsavedChanges) return;

        if (newNoteContainsContent) {
            noteFrame.setClosingConfirmation(closingConfirmationMessage);
        } else {
            noteFrame.removeClosingConfirmation();
        }
    }
}
