package cyder.widgets;

import com.google.common.base.Preconditions;
import cyder.annotations.CyderAuthor;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
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
import cyder.utils.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

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

    private static final int defaultFrameWidth = 600;
    private static final int defaultFrameHeight = 680;
    private static final int noteScrollPadding = 25;
    private static CyderScrollList notesScrollList;

    private static final int noteScrollLength = defaultFrameWidth - 2 * noteScrollPadding;

    private static CyderPartitionedLayout framePartitionedLayout;

    private static View currentView;

    private static final Dimension commonButtonSizes = new Dimension(160, 40);

    private static JTextPane newNoteArea;
    private static CyderTextField newNoteNameField;

    private static final String TXT_EXTENSION = ".txt";

    private static final String ADD = "Add";
    private static final String OPEN = "Open";
    private static final String DELETE = "Delete";

    private enum View {
        LIST,
        ADD,
        EDIT,
    }

    /**
     * Suppress default constructor.
     */
    private NotesWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Widget(triggers = {"note", "notes"}, description
            = "A note taking widget that can save and display multiple notes")
    public static void showGui() {
        if (Console.INSTANCE.getUuid() == null) return;
        UiUtil.closeIfOpen(noteFrame);

        noteFrame = new CyderFrame(defaultFrameWidth, defaultFrameHeight);

        setupView(View.LIST);

        noteFrame.finalizeAndShow();
    }

    private static void setupView(View view) {
        Preconditions.checkNotNull(view);
        currentView = view;

        switch (view) {
            case LIST -> setupListView();
            case ADD -> setupAddView();
            case EDIT -> setupEditView();
        }
    }

    private static void setupListView() {
        refreshNotesList();

        framePartitionedLayout = new CyderPartitionedLayout();

        notesScrollList = new CyderScrollList(noteScrollLength, noteScrollLength,
                CyderScrollList.SelectionPolicy.MULTIPLE);
        notesList.forEach(noteFile -> notesScrollList.addElement(noteFile.getName(), () -> {
            currentNoteFile = noteFile;
            setupView(View.EDIT);
        }));
        JLabel notesLabel = notesScrollList.generateScrollList();
        notesLabel.setSize(noteScrollLength, noteScrollLength);

        Dimension buttonSize = new Dimension(160, 40);

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
        framePartitionedLayout.addComponentMaintainSize(notesLabel, CyderPartitionedLayout.PartitionAlignment.TOP);
        framePartitionedLayout.spacer(2);
        framePartitionedLayout.addComponent(buttonPanel);
        framePartitionedLayout.spacer(2);

        noteFrame.setCyderLayout(framePartitionedLayout);
        noteFrame.repaint();
        revalidateFrameTitle();
    }

    private static final Font noteNameFieldFont = new Font("Agency FB", Font.BOLD, 26);
    private static final Border noteNameFieldBorder
            = BorderFactory.createMatteBorder(0, 0, 4, 0, CyderColors.navy);
    private static final int noteScrollHeight = noteScrollLength - 50;
    private static final String BACK = "Back";
    private static final String CREATE = "Create";

    private static void setupAddView() {
        noteFrame.removeCyderLayoutPanel();
        noteFrame.repaint();

        newNoteNameField = new CyderTextField();
        newNoteNameField.setFont(noteNameFieldFont);
        newNoteNameField.setForeground(CyderColors.navy);
        newNoteNameField.setCaret(new CyderCaret(CyderColors.navy));
        newNoteNameField.setBackground(CyderColors.vanilla);
        newNoteNameField.setSize(300, 40);
        newNoteNameField.setToolTipText("Note name");
        newNoteNameField.setBorder(noteNameFieldBorder);

        newNoteArea = new JTextPane();
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
        backButton.setSize(commonButtonSizes);
        backButton.addActionListener(e -> setupView(View.LIST));

        CyderButton createButton = new CyderButton(CREATE);
        createButton.setSize(commonButtonSizes);
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

    private static File currentNoteFile;

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
        if (noteName.toLowerCase().endsWith(TXT_EXTENSION)) noteName = noteName.substring(0, noteName.length() - 4);

        String requestedName = noteName + TXT_EXTENSION;

        if (!OsUtil.isValidFilename(requestedName)) {
            noteFrame.notify("Invalid filename");
            return;
        }

        File createFile = OsUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(),
                Console.INSTANCE.getUuid(), UserFile.NOTES.getName(), requestedName);
        if (!OsUtil.createFile(createFile, true)) {
            noteFrame.notify("Could not create file: \"" + requestedName + "\"");
        }

        String contents = newNoteArea.getText();
        try (BufferedWriter write = new BufferedWriter(new FileWriter(createFile))) {
            write.write(contents);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        setupView(View.LIST);
        noteFrame.notify("Added note file: \"" + requestedName + "\"");
    }

    private static String getCurrentNoteContents() {
        try (BufferedReader reader = new BufferedReader(new FileReader(currentNoteFile))) {
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line);
                contentBuilder.append("\n");
            }

            return contentBuilder.toString();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new IllegalStateException("Could not get contents of current note file");
    }

    /**
     * Sets up and shows the edit note view.
     */
    private static void setupEditView() {
        Preconditions.checkNotNull(currentNoteFile);

        noteFrame.removeCyderLayoutPanel();
        noteFrame.repaint();

        CyderTextField editNoteNameField = new CyderTextField();
        editNoteNameField.setFont(noteNameFieldFont);
        editNoteNameField.setForeground(CyderColors.navy);
        editNoteNameField.setCaret(new CyderCaret(CyderColors.navy));
        editNoteNameField.setBackground(CyderColors.vanilla);
        editNoteNameField.setSize(300, 40);
        editNoteNameField.setToolTipText("Note name");
        editNoteNameField.setBorder(noteNameFieldBorder);
        editNoteNameField.setText(FileUtil.getFilename(currentNoteFile));

        JTextPane noteEditArea = new JTextPane();
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

        CyderScrollPane noteScroll = new CyderScrollPane(noteEditArea, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        noteScroll.setThumbColor(CyderColors.regularPink);
        noteScroll.setSize(noteScrollLength, noteScrollHeight);
        noteScroll.getViewport().setOpaque(false);
        noteScroll.setOpaque(false);
        noteScroll.setBorder(null);
        noteEditArea.setAutoscrolls(true);

        CyderButton saveButton = new CyderButton("Save");
        saveButton.setSize(commonButtonSizes);
        saveButton.addActionListener(e -> {
            // todo extract me
            String newFilename = editNoteNameField.getTrimmedText() + TXT_EXTENSION;
            if (!OsUtil.isValidFilename(newFilename)) {
                noteFrame.notify("Invalid filename: \"" + newFilename + "\"");
                return;
            }

            if (!OsUtil.deleteFile(currentNoteFile)) {
                noteFrame.notify("Failed to update note contents");
                return;
            }

            File newFile = OsUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(),
                    Console.INSTANCE.getUuid(), UserFile.NOTES.getName(), newFilename);
            if (!OsUtil.createFile(newFile, true)) {
                noteFrame.notify("Failed to update not contents");
                return;
            }

            currentNoteFile = newFile;

            String contents = noteEditArea.getText();
            if (contents.isEmpty()) return;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(newFile))) {
                writer.write(contents);
            } catch (Exception exception) {
                ExceptionHandler.handle(exception);
            }
        });

        CyderButton backButton = new CyderButton(BACK);
        backButton.setSize(commonButtonSizes);
        backButton.addActionListener(e -> {
            CyderThreadRunner.submit(() -> {
                // todo extract me
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

                boolean pendingChanges = pendingChangesBuilder.length() > 0;
                if (pendingChanges) {
                    GetterUtil.Builder builder = new GetterUtil.Builder("Pending changes")
                            .setDisableRelativeTo(true)
                            .setInitialString(pendingChangesBuilder.toString())
                            .setRelativeTo(noteFrame)
                            .setYesButtonText("Exit")
                            .setNoButtonText("Don't exit");
                    boolean shouldGoBack = GetterUtil.getInstance().getConfirmation(builder);
                    if (!shouldGoBack) return;
                }

                setupView(View.LIST);
            }, "Note editor exit confirmation");
        });

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

        noteFrame.setCyderLayout(editNoteLayout);
        revalidateFrameTitle();
        noteFrame.repaint();
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
            case ADD -> noteFrame.setTitle("Add note");
            case EDIT -> {
                String name = currentNoteFile == null ? "" : FileUtil.getFilename(currentNoteFile);
                noteFrame.setTitle("Editing note: " + name);
            }
        }
    }

    private static void deleteButtonAction() {
        Optional<String> selectedElement = notesScrollList.getSelectedElement();
        if (selectedElement.isEmpty()) return;

        notesList.stream().filter(noteFile ->
                noteFile.getName().equals(selectedElement.get())).forEach(OsUtil::deleteFile);
        refreshNotesList();
        notesScrollList.removeSelectedElement();

        // todo duplicate logic here with setup for view
        notesScrollList = new CyderScrollList(noteScrollLength, noteScrollLength,
                CyderScrollList.SelectionPolicy.MULTIPLE);
        notesList.forEach(noteFile -> notesScrollList.addElement(noteFile.getName(), () -> {
            currentNoteFile = noteFile;
            setupView(View.EDIT);
        }));
        JLabel notesLabel = notesScrollList.generateScrollList();
        notesLabel.setSize(noteScrollLength, noteScrollLength);
        framePartitionedLayout.setComponent(notesLabel, 1);
        noteFrame.repaint();
    }

    private static final ArrayList<File> notesList = new ArrayList<>();

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
                FileUtil.getExtension(noteFile).equals(TXT_EXTENSION)).forEach(notesList::add);
    }
}
