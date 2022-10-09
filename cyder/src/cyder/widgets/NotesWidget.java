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
import cyder.layouts.CyderGridLayout;
import cyder.layouts.CyderPartitionedLayout;
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
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
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
        refreshNotesList();

        noteFrame = new CyderFrame(defaultFrameWidth, defaultFrameHeight);

        setupView(View.LIST);

        noteFrame.finalizeAndShow();
    }

    private static View currentView;

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
        framePartitionedLayout = new CyderPartitionedLayout();

        notesScrollList = new CyderScrollList(noteScrollLength, noteScrollLength,
                CyderScrollList.SelectionPolicy.MULTIPLE);
        notesList.forEach(noteFile -> notesScrollList.addElement(noteFile.getName(), () -> {
            // todo
        }));
        JLabel notesLabel = notesScrollList.generateScrollList();
        notesLabel.setSize(noteScrollLength, noteScrollLength);

        Dimension buttonSize = new Dimension(160, 40);

        CyderGridLayout buttonGridlayout = new CyderGridLayout(3, 1);

        CyderButton addButton = new CyderButton("Add");
        addButton.setSize(buttonSize);
        addButton.addActionListener(e -> addNoteAction());
        buttonGridlayout.addComponent(addButton);

        CyderButton openButton = new CyderButton("Open");
        openButton.setSize(buttonSize);
        openButton.addActionListener(e -> {
            // todo
        });
        buttonGridlayout.addComponent(openButton);

        CyderButton deleteButton = new CyderButton("Delete");
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

    private static final Dimension addViewButtonSize = new Dimension(160, 40);

    private static void setupAddView() {
        noteFrame.removeCyderLayoutPanel();
        noteFrame.repaint();

        CyderTextField noteNameField = new CyderTextField();
        noteNameField.setFont(new Font("Agency FB", Font.BOLD, 26));
        noteNameField.setForeground(CyderColors.navy);
        noteNameField.setCaret(new CyderCaret(CyderColors.navy));
        noteNameField.setBackground(CyderColors.vanilla);
        noteNameField.setSize(300, 40);
        noteNameField.setToolTipText("Note name");
        noteNameField.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, CyderColors.navy));

        int noteScrollHeight = noteScrollLength - 50;
        JTextPane noteArea = new JTextPane();
        noteArea.setSize(noteScrollLength, noteScrollHeight);
        noteArea.setBackground(CyderColors.vanilla);
        noteArea.setBorder(new LineBorder(CyderColors.navy, 5));
        noteArea.setFocusable(true);
        noteArea.setEditable(true);
        noteArea.setFont(Console.INSTANCE.generateUserFont());
        noteArea.setForeground(CyderColors.navy);
        noteArea.setCaret(new CyderCaret(CyderColors.navy));
        noteArea.setCaretColor(noteArea.getForeground());

        CyderScrollPane noteScroll = new CyderScrollPane(noteArea, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        noteScroll.setThumbColor(CyderColors.regularPink);
        noteScroll.setSize(noteScrollLength, noteScrollHeight);
        noteScroll.getViewport().setOpaque(false);
        noteScroll.setOpaque(false);
        noteScroll.setBorder(null);
        noteArea.setAutoscrolls(true);

        CyderButton backButton = new CyderButton("Back");
        backButton.setSize(addViewButtonSize);
        backButton.addActionListener(e -> setupView(View.LIST));

        CyderButton createButton = new CyderButton("Create");
        createButton.setSize(addViewButtonSize);
        createButton.addActionListener(e -> {
            // todo validate
        });

        CyderGridLayout buttonGridLayout = new CyderGridLayout(2, 1);
        buttonGridLayout.addComponent(createButton);
        buttonGridLayout.addComponent(backButton);
        CyderPanel buttonGridPanel = new CyderPanel(buttonGridLayout);
        buttonGridPanel.setSize(400, 50);

        CyderPartitionedLayout addNoteLayout = new CyderPartitionedLayout();
        addNoteLayout.spacer(2);
        addNoteLayout.addComponentMaintainSize(noteNameField);
        addNoteLayout.spacer(2);
        addNoteLayout.addComponentMaintainSize(noteScroll);
        addNoteLayout.spacer(2);
        addNoteLayout.addComponentMaintainSize(buttonGridPanel);
        addNoteLayout.spacer(2);

        noteFrame.setCyderLayout(addNoteLayout);
        revalidateFrameTitle();
        noteFrame.repaint();
    }

    private static void setupEditView() {
        // todo

        revalidateFrameTitle();
    }

    private static void revalidateFrameTitle() {
        switch (currentView) {
            case LIST -> {
                String name = UserUtil.getCyderUser().getName();
                noteFrame.setTitle(name + StringUtil.getApostrophe(name) + " notes");
            }
            case ADD -> noteFrame.setTitle("Add note");
            case EDIT -> noteFrame.setTitle("Editing note: "); // todo
        }
    }

    private static void addNoteAction() {
        setupView(View.ADD);
    }

    private static void deleteButtonAction() {
        Optional<String> selectedElement = notesScrollList.getSelectedElement();
        if (selectedElement.isEmpty()) return;

        notesList.stream().filter(noteFile ->
                noteFile.getName().equals(selectedElement.get())).forEach(OsUtil::deleteFile);
        refreshNotesList();
        notesScrollList.removeSelectedElement();


        notesScrollList = new CyderScrollList(noteScrollLength, noteScrollLength,
                CyderScrollList.SelectionPolicy.MULTIPLE);
        notesList.forEach(noteFile -> notesScrollList.addElement(noteFile.getName(), () -> {
            // todo
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
                FileUtil.getExtension(noteFile).equals(".txt")).forEach(notesList::add);
    }
}
