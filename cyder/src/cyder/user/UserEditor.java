package cyder.user;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import cyder.annotations.ForReadability;
import cyder.annotations.Widget;
import cyder.audio.AudioPlayer;
import cyder.console.Console;
import cyder.console.ConsoleConstants;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.enums.Dynamic;
import cyder.enums.ExitCondition;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.layouts.CyderGridLayout;
import cyder.layouts.CyderPartitionedLayout;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderPanel;
import cyder.ui.button.CyderButton;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.drag.button.DragLabelTextButton;
import cyder.ui.field.CyderCaret;
import cyder.ui.field.CyderPasswordField;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.label.CyderLabel;
import cyder.ui.pane.CyderOutputPane;
import cyder.ui.pane.CyderScrollList;
import cyder.ui.pane.CyderScrollPane;
import cyder.ui.selection.CyderCheckbox;
import cyder.utils.*;
import cyder.widgets.ColorConverterWidget;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An editor for user preferences, files, colors, fonts, and more.
 */
public final class UserEditor {
    /**
     * The frame size of the user editor.
     */
    private static final int FRAME_WIDTH = 800;

    /**
     * The height of the user editor.
     */
    private static final int FRAME_HEIGHT = 600;

    /**
     * The remaining width of the frame that the content pane may encompass.
     */
    private static final int CONTENT_PANE_WIDTH = FRAME_WIDTH - 2 * CyderFrame.BORDER_LEN;

    /**
     * The remaining height of the frame that the content pane may encompass.
     */
    private static final int CONTENT_PANE_HEIGHT = FRAME_HEIGHT - 2 * CyderFrame.BORDER_LEN
            - CyderDragLabel.DEFAULT_HEIGHT;

    /**
     * The padding between the partitioned area and the files scroll generated label.
     */
    private static final int filesLabelPadding = 20;

    /**
     * The partition height of the files header label on the files page.
     */
    private static final int FILE_HEADER_LABEL_PARTITION = 10;

    /**
     * The partition height for the files scroll on the files page.
     */
    private static final int FILE_SCROLL_PARTITION = 75;

    /**
     * The partition height for the files scroll buttons for the files page.
     */
    private static final int FILE_BUTTON_PARTITION = 15;

    /**
     * The width of the files scroll.
     */
    private static final int FILES_SCROLL_WIDTH = CONTENT_PANE_WIDTH - 2 * filesLabelPadding;

    /**
     * The height of the files scroll.
     */
    private static final int FILES_SCROLL_HEIGHT =
            (int) ((FILE_SCROLL_PARTITION / 100.0f) * CONTENT_PANE_HEIGHT - 2 * filesLabelPadding);

    /**
     * The user editor frame.
     */
    private static CyderFrame editUserFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT, CyderColors.vanilla);

    /**
     * The names of the files for the files list.
     */
    private static final ArrayList<String> filesNameList = new ArrayList<>();

    /**
     * The reference used for the files scroll list label.
     */
    private static final AtomicReference<JLabel> filesLabelReference = new AtomicReference<>();

    /**
     * The reference used for the cyder scroll list.
     */
    private static final AtomicReference<CyderScrollList> filesScrollListReference = new AtomicReference<>();

    static {
        CyderScrollList filesScrollList = new CyderScrollList(
                FILES_SCROLL_WIDTH, FILES_SCROLL_HEIGHT, CyderScrollList.SelectionPolicy.MULTIPLE);
        filesScrollList.setBorder(null);
        filesScrollListReference.set(filesScrollList);
    }

    /**
     * The possible pages of the user editor.
     */
    public enum Page {
        FIELDS("Fields", UserEditor::switchToFieldInputs),
        PREFERENCES("Preferences", UserEditor::switchToPreferences),
        FONT_AND_COLOR("Font & Color", UserEditor::switchToFontAndColor),
        FILES("Files", UserEditor::switchToUserFiles);

        /**
         * The frame title and id of this page.
         */
        private final String title;

        /**
         * The runnable to invoke to load the page when the menu item is clicked.
         */
        private final Runnable switchRunnable;

        Page(String title, Runnable runnable) {
            this.title = title;
            this.switchRunnable = runnable;

            Logger.log(Logger.Tag.OBJECT_CREATION, this);
        }

        /**
         * Returns the title for this page.
         *
         * @return the title for this page
         */
        public String getTitle() {
            return title;
        }

        /**
         * Returns the runnable to invoke when this page is clicked.
         *
         * @return the runnable to invoke when this page is clicked
         */
        public Runnable getSwitchRunnable() {
            return switchRunnable;
        }
    }

    /**
     * The current page of the user editor.
     */
    private static Page currentPage = Page.FILES;

    /**
     * The thickness of the border for the input and output areas if enabled.
     */
    public static final int inputOutputBorderThickness = 3;

    /**
     * Suppress default constructor.
     */
    private UserEditor() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Widget(triggers = {"prefs", "edit user"}, description = "A widget to edit your user preferences and files")
    public static void showGui() {
        showGui(Page.FILES);
    }

    /**
     * Shows the user editor with the provided page.
     *
     * @param page the page to show on the user editor
     */
    public static void showGui(Page page) {
        UiUtil.closeIfOpen(editUserFrame);

        editUserFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT, CyderColors.vanilla);
        editUserFrame.setBackground(CyderColors.vanilla);
        editUserFrame.setTitle("Preferences");
        installDragLabelButtons();
        Console.INSTANCE.addToFrameTaskbarExceptions(editUserFrame);
        editUserFrame.finalizeAndShow();

        currentPage = page;
        page.getSwitchRunnable().run();
    }

    /**
     * Installs the drag label items to the edit user frame.
     */
    private static void installDragLabelButtons() {
        for (Page page : Page.values()) {
            DragLabelTextButton menuButton = DragLabelTextButton.generateTextButton(
                    new DragLabelTextButton.Builder(page.getTitle())
                            .setTooltip(page.getTitle())
                            .setClickAction(() -> {
                                if (currentPage != page) {
                                    currentPage = page;

                                    page.getSwitchRunnable().run();
                                }
                            }));

            editUserFrame.getTopDragLabel().addRightButton(menuButton, 0);
        }
    }

    /**
     * The separator between user folders and user files.
     */
    private static final String FILES_SCROLL_SEPARATOR = "/";

    /**
     * UserFile folders to show in the files scroll list.
     */
    @SuppressWarnings("Guava") /* IntelliJ thinking it can show up Guava */
    private enum ScrollListFolder {
        BACKGROUNDS(UserFile.BACKGROUNDS, FileUtil::isSupportedImageExtension),
        MUSIC(UserFile.MUSIC, FileUtil::isSupportedAudioExtension),
        FILES(UserFile.FILES, (ignored) -> true);

        private final UserFile userFile;
        private final Function<File, Boolean> shouldShowFunction;

        ScrollListFolder(UserFile userFile, Function<File, Boolean> shouldShowFunction) {
            this.userFile = userFile;
            this.shouldShowFunction = shouldShowFunction;
        }

        /**
         * Returns the user file for this scroll list folder.
         *
         * @return the user file for this scroll list folder
         */
        public UserFile getUserFile() {
            return userFile;
        }

        /**
         * Returns the function which determines ifa given file is shown on the scroll.
         *
         * @return the function which determines ifa given file is shown on the scroll
         */
        public Function<File, Boolean> getShouldShowFunction() {
            return shouldShowFunction;
        }
    }

    /**
     * Refreshes the contents of {@link #filesNameList}. Note this method does not update the
     * ui based on the updated contents, {@link #revalidateFilesScroll()} should be used to
     * regenerate the scroll list with the new contents.
     */
    private static void refreshFileLists() {
        filesNameList.clear();

        for (ScrollListFolder folder : ScrollListFolder.values()) {
            File directoryReference = UserUtil.getUserFile(folder.getUserFile());
            File[] directoryFiles = directoryReference.listFiles();
            if (directoryFiles != null && directoryFiles.length > 0) {
                for (File file : directoryFiles) {
                    if (Boolean.TRUE.equals(folder.getShouldShowFunction().apply(file))) {
                        filesNameList.add(folder.getUserFile().getName() + FILES_SCROLL_SEPARATOR + file.getName());
                    }
                }
            }
        }
    }

    /**
     * Sets whether the files scroll buttons are enabled.
     *
     * @param enabled whether to enable the files scroll buttons
     */
    private static void setFilesScrollButtonsEnabled(boolean enabled) {
        Preconditions.checkNotNull(addFileButton).setEnabled(enabled);
        openFileButton.setEnabled(enabled);
        renameFileButton.setEnabled(enabled);
        deleteFileButton.setEnabled(enabled);
    }

    /**
     * The action listener for the add file button.
     */
    private static final ActionListener addFileButtonActionListener = e -> {
        try {
            CyderThreadRunner.submit(() -> {
                try {
                    File fileToAdd = GetterUtil.getInstance().getFile(
                            new GetterUtil.Builder("Add File").setRelativeTo(editUserFrame));

                    if (fileToAdd == null || StringUtil.isNullOrEmpty(fileToAdd.getName())) {
                        return;
                    }

                    UserFile copyLocation = determineCopyLocation(fileToAdd);

                    String uniqueNameAndExtension = fileToAdd.getName();
                    File parentFolder = UserUtil.getUserFile(copyLocation);
                    if (parentFolder.exists() && parentFolder.isDirectory()) {
                        uniqueNameAndExtension = FileUtil.findUniqueName(fileToAdd, parentFolder);
                    }

                    try {
                        String copyFolderPath = UserUtil.getUserFile(copyLocation).getAbsolutePath();
                        File copyFile = OSUtil.buildFile(copyFolderPath, uniqueNameAndExtension);
                        Files.copy(fileToAdd.toPath(), copyFile.toPath());

                        revalidateFilesScroll();

                        if (copyLocation.getName().equals(UserFile.BACKGROUNDS.getName())) {
                            Console.INSTANCE.reloadBackgrounds();
                        }
                    } catch (Exception ex) {
                        editUserFrame.notify("Could not add file at this time");
                        ExceptionHandler.handle(ex);
                    }
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                }
            }, "User Editor File Adder");
        } catch (Exception exc) {
            ExceptionHandler.handle(exc);
        }
    };

    /**
     * Returns the user file to place the provided file in.
     *
     * @param file the file which will be moved to the returned user folder
     * @return the user file to place the provided file in
     */
    @ForReadability
    private static UserFile determineCopyLocation(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkState(file.exists());

        if (FileUtil.isSupportedImageExtension(file)) {
            return UserFile.BACKGROUNDS;
        } else if (FileUtil.isSupportedAudioExtension(file)) {
            return UserFile.MUSIC;
        } else {
            return UserFile.FILES;
        }
    }

    /**
     * The action listener for the open file button.
     */
    private static final ActionListener openFileButtonActionListener = e -> openFile();

    /**
     * The action to open a file when the open file button is clicked.
     */
    private static void openFile() {
        LinkedList<String> selectedScrollElements = filesScrollListReference.get().getSelectedElements();

        for (String selectedScrollElement : selectedScrollElements) {
            for (String fileName : filesNameList) {
                if (selectedScrollElement.equals(fileName)) {
                    File file = getFile(selectedScrollElement);

                    if (file.exists()) {
                        IOUtil.openFile(file);
                    }

                    break;
                }
            }
        }
    }

    /**
     * The name of the thread for renaming af ile.
     */
    private static final String USER_EDITOR_FILE_RENAMER_THREAD_NAME = "User Editor File Renamer";

    /**
     * The action listener for the rename file button.
     */
    private static final ActionListener renameFileButtonActionListener = e -> {
        try {
            LinkedList<String> selectedElements = filesScrollListReference.get().getSelectedElements();

            if (selectedElements.isEmpty()) return;

            if (selectedElements.size() > 1) {
                editUserFrame.notify("Sorry, but you can only rename one file at a time");
                return;
            }

            String selectedElement = selectedElements.get(0);
            File selectedFile = getFile(selectedElement);

            if (selectedFile == null || !selectedFile.exists()) return;

            String[] parts = selectedElement.split(FILES_SCROLL_SEPARATOR);
            String userDirectory = parts[0];
            String filename = parts[1];

            if (isOpenInAudioPlayer(selectedFile)) {
                editUserFrame.notify("Cannot rename file open in audio player");
                return;
            } else if (isConsoleBackground(selectedFile)) {
                editUserFrame.notify("Cannot rename current console background");
                return;
            }

            CyderThreadRunner.submit(() -> {
                String newName = GetterUtil.getInstance().getString(
                        new GetterUtil.Builder("Rename " + filename)
                                .setFieldTooltip("Enter a valid file name (extension will be handled)")
                                .setRelativeTo(editUserFrame)
                                .setSubmitButtonText("Rename")
                                .setInitialString(FileUtil.getFilename(selectedFile)));

                if (StringUtil.isNullOrEmpty(newName)) return;

                String newFilenameAndExtension = newName + FileUtil.getExtension(selectedFile);

                if (!OSUtil.isValidFilename(newFilenameAndExtension)) {
                    editUserFrame.notify("Invalid filename; file not renamed");
                    return;
                }

                if (renameRequestedFile(selectedFile, newFilenameAndExtension)) {
                    switch (userDirectory) {
                        case "Backgrounds" -> editUserFrame.notify("Renamed background file");
                        case "Music" -> editUserFrame.notify("Renamed music file");
                        default -> editUserFrame.notify("Renamed file");
                    }

                    revalidateFilesScroll();
                } else {
                    editUserFrame.notify("Failed to rename file");
                }
            }, USER_EDITOR_FILE_RENAMER_THREAD_NAME);
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    };

    /**
     * Attempts to rename the provided file to the new proposed name + old extension.
     * If the file was successfully renamed and it was a music file, the album art is also renamed
     * to match the new name if present.
     *
     * @param referenceFile the file to rename
     * @param proposedName  the requested name and extension to rename the file to, such as "Hello.txt"
     */
    private static boolean renameRequestedFile(File referenceFile, String proposedName) {
        Preconditions.checkNotNull(referenceFile);
        Preconditions.checkArgument(referenceFile.exists());
        Preconditions.checkNotNull(proposedName);
        Preconditions.checkArgument(!proposedName.isEmpty());
        Preconditions.checkArgument(OSUtil.isValidFilename(proposedName));

        String oldAlbumArtName = FileUtil.getFilename(referenceFile);
        File newReferenceFile = OSUtil.buildFile(referenceFile.getParentFile().getAbsolutePath(), proposedName);
        if (!referenceFile.renameTo(newReferenceFile)) {
            return false;
        }

        // Attempt to find album art file to rename
        File albumArtDir = OSUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(),
                Console.INSTANCE.getUuid(), UserFile.MUSIC.getName(), UserFile.ALBUM_ART);

        if (albumArtDir.exists()) {
            File[] albumArtFiles = albumArtDir.listFiles();

            if (albumArtFiles != null && albumArtFiles.length > 0) {
                File renameMe = null;

                for (File targetFile : albumArtFiles) {
                    if (FileUtil.getFilename(targetFile).equals(oldAlbumArtName)) {
                        renameMe = targetFile;
                        break;
                    }
                }

                if (renameMe != null) {
                    String namePart = proposedName.split("\\.")[0];
                    File newAlbumArtFile = OSUtil.buildFile(renameMe.getParentFile().getAbsolutePath(),
                            namePart + "." + ImageUtil.PNG_FORMAT);
                    if (!renameMe.renameTo(newAlbumArtFile)) {
                        Console.INSTANCE.getInputHandler().println("Failed to rename album art: "
                                + FileUtil.getFilename(renameMe));
                    } else {
                        Logger.log(Logger.Tag.DEBUG, "Renamed album art file for reference file: "
                                + oldAlbumArtName + ", renamed to: " + namePart);
                    }
                }
            }
        }

        return true;
    }

    /**
     * The action listener for the delete file button.
     */
    private static final ActionListener deleteFileButtonActionListener = e -> {
        LinkedList<String> selectedElements = filesScrollListReference.get().getSelectedElements();

        if (selectedElements.isEmpty()) {
            return;
        }

        for (String selectedElement : selectedElements) {
            File selectedFile = getFile(selectedElement);

            if (selectedFile == null || !selectedFile.exists()) {
                return;
            }

            String[] parts = selectedElement.split(FILES_SCROLL_SEPARATOR);
            String userDirectory = parts[0];
            String filename = parts[1];

            if (isOpenInAudioPlayer(selectedFile)) {
                editUserFrame.notify("Cannot delete " + filename + " as it is open in audio player");
                return;
            } else if (isConsoleBackground(selectedFile)) {
                editUserFrame.notify("Cannot delete current console background: " + filename);
                return;
            }

            if (OSUtil.deleteFile(selectedFile)) {
                switch (userDirectory) {
                    case "Backgrounds" -> editUserFrame.notify("Deleted background file: " + filename);
                    case "Music" -> editUserFrame.notify("Deleted music file: " + filename);
                    default -> editUserFrame.notify("Deleted file: " + filename);
                }

                revalidateFilesScroll();
            } else {
                editUserFrame.notify("Could not delete " + filename);
            }
        }
    };

    /**
     * Returns whether the provided file is the currently open file in the audio player.
     *
     * @param file the file
     * @return whether the provided file is the currently open file in the audio player
     */
    private static boolean isOpenInAudioPlayer(File file) {
        Preconditions.checkNotNull(file);

        File refFile = AudioPlayer.getCurrentAudio();
        return refFile != null && refFile.getAbsolutePath().equals(file.getAbsolutePath());
    }

    /**
     * Returns whether the provided file is the current set background.
     *
     * @param file the file
     * @return whether the provided file is the current set background
     */
    private static boolean isConsoleBackground(File file) {
        Preconditions.checkNotNull(file);

        return Console.INSTANCE.getCurrentBackground().getReferenceFile()
                .getAbsolutePath().equals(file.getAbsolutePath());
    }

    /**
     * Returns the user file represented by the provided name from the files list.
     * For example: backgrounds/Img.png would open Img.png if that file was present
     * in the current user's Backgrounds/ folder.
     *
     * @param name the file name such as "Backgrounds/img.jpg"
     * @return the user file represented by the provided name from the files list
     * @throws NoSuchElementException if the provided file cannot be found
     */
    private static File getFile(String name) {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.isEmpty());

        String[] parts = name.split(FILES_SCROLL_SEPARATOR);
        String folderName = parts[0];
        String fileName = parts[1];

        Optional<File> file;

        if (folderName.equalsIgnoreCase(UserFile.BACKGROUNDS.getName())) {
            file = attemptToFindInUserFiles(fileName, UserFile.BACKGROUNDS);
        } else if (folderName.equalsIgnoreCase(UserFile.MUSIC.getName())) {
            file = attemptToFindInUserFiles(fileName, UserFile.MUSIC);
        } else {
            file = attemptToFindInUserFiles(fileName, UserFile.FILES);
        }

        return file.orElseThrow();
    }

    /**
     * Returns the file reference to the file in the provided user file directory if found. Empty optional else.
     *
     * @param name     the name of the file such as myFile.png
     * @param userFile the user file directory to search through
     * @return the file reference to the file in the provided user file directory if found. Empty optional else
     */
    private static Optional<File> attemptToFindInUserFiles(String name, UserFile userFile) {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.isEmpty());
        Preconditions.checkNotNull(userFile);

        File userFilesDirectory = OSUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(),
                Console.INSTANCE.getUuid(), userFile.getName());

        File[] files = userFilesDirectory.listFiles();

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.getName().equals(name)) {
                    return Optional.of(file);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * The width of the the files scroll buttons.
     */
    private static final int buttonWidth = 175;

    /**
     * The height of the files scroll buttons.
     */
    private static final int buttonHeight = 40;

    /**
     * The number of horizontal cells for the buttons layout.
     */
    private static final int BUTTON_X_CELLS = 4;

    /**
     * The number of vertical cells for the buttons layout.
     */
    private static final int BUTTON_Y_CELLS = 1;

    /**
     * The add file button for user files.
     */
    private static final CyderButton addFileButton = new CyderButton("Add");

    /**
     * The open file button for user files.
     */
    private static final CyderButton openFileButton = new CyderButton("Open");

    /**
     * The rename file button for user files.
     */
    private static final CyderButton renameFileButton = new CyderButton("Rename");

    /**
     * The delete file button for user files.
     */
    private static final CyderButton deleteFileButton = new CyderButton("Delete");

    /**
     * The grid layout for the file action buttons.
     */
    private static final CyderGridLayout buttonGridLayout;

    static {
        buttonGridLayout = new CyderGridLayout(BUTTON_X_CELLS, BUTTON_Y_CELLS);

        addFileButton.addActionListener(addFileButtonActionListener);
        addFileButton.setSize(buttonWidth, buttonHeight);

        openFileButton.addActionListener(openFileButtonActionListener);
        openFileButton.setSize(buttonWidth, buttonHeight);

        renameFileButton.addActionListener(renameFileButtonActionListener);
        renameFileButton.setSize(buttonWidth, buttonHeight);

        deleteFileButton.addActionListener(deleteFileButtonActionListener);
        deleteFileButton.setSize(buttonWidth, buttonHeight);

        buttonGridLayout.addComponent(addFileButton);
        buttonGridLayout.addComponent(openFileButton);
        buttonGridLayout.addComponent(renameFileButton);
        buttonGridLayout.addComponent(deleteFileButton);
    }

    /**
     * The partitioned layout for the files page.
     */
    private static final CyderPartitionedLayout filesPartitionedLayout = new CyderPartitionedLayout();

    /**
     * The files label header label to display the page title.
     */
    private static final CyderLabel filesHeaderLabel;

    /**
     * The width of the files header label.
     */
    private static final int FILES_HEADER_LABEL_WIDTH = 350;

    /**
     * The height of the files header label.
     */
    private static final int FILES_HEADER_LABEL_HEIGHT = 50;

    /**
     * The text for the file header label.
     */
    private static final String FILES_BACKGROUNDS_AND_MUSIC = "Files, Backgrounds, and Music";

    static {
        filesHeaderLabel = new CyderLabel(FILES_BACKGROUNDS_AND_MUSIC);
        filesHeaderLabel.setFont(CyderFonts.DEFAULT_FONT);
        filesHeaderLabel.setSize(FILES_HEADER_LABEL_WIDTH, FILES_HEADER_LABEL_HEIGHT);
    }

    /**
     * Switches to the user files preference page, wiping past components
     * and regenerating the files scroll label in the process.
     */
    private static void switchToUserFiles() {
        CyderPanel buttonPanel = new CyderPanel(buttonGridLayout);
        buttonPanel.setSize(CONTENT_PANE_WIDTH, FILE_BUTTON_PARTITION * CONTENT_PANE_HEIGHT);

        filesPartitionedLayout.clearComponents();

        JLabel filesLabelRef = filesLabelReference.get();
        if (filesLabelRef == null) filesLabelRef = loadingLabel;
        filesPartitionedLayout.addComponent(filesHeaderLabel, FILE_HEADER_LABEL_PARTITION);
        filesPartitionedLayout.addComponent(filesLabelRef, FILE_SCROLL_PARTITION);
        filesPartitionedLayout.addComponent(buttonPanel, FILE_BUTTON_PARTITION);

        revalidateFilesScroll();

        editUserFrame.setCyderLayout(filesPartitionedLayout);
    }

    /**
     * The label to display when updating the files scroll list.
     */
    private static final CyderLabel loadingLabel = new CyderLabel(CyderStrings.LOADING);

    /**
     * Revalidates the contents of the user files scroll, regenerates the label,
     * updates the corresponding atomic references, and updates the UI based on the new
     * atomic reference values.
     */
    private static void revalidateFilesScroll() {
        filesPartitionedLayout.setComponent(loadingLabel, 1);

        CyderScrollList filesScrollList = filesScrollListReference.get();
        filesScrollList.removeAllElements();

        refreshFileLists();

        for (String element : filesNameList) {
            filesScrollList.addElement(element, () -> IOUtil.openFile(getFile(element)));
        }

        JLabel filesLabel = filesScrollList.generateScrollList();
        filesLabelReference.set(filesLabel);
        filesLabel.setBackground(CyderColors.vanilla);
        filesLabel.setBorder(null);
        filesLabel.setLocation(5, 5);

        JLabel parentBorderLabel = new JLabel();
        parentBorderLabel.setSize(FILES_SCROLL_WIDTH + 2 * 5, FILES_SCROLL_HEIGHT + 2 * 5);
        parentBorderLabel.setOpaque(true);
        parentBorderLabel.setBackground(CyderColors.vanilla);
        parentBorderLabel.setBorder(new LineBorder(CyderColors.navy, 5));
        parentBorderLabel.add(filesLabel);

        filesPartitionedLayout.setComponent(parentBorderLabel, 1);
    }

    /**
     * The x padding between the left and right frame bounds for the files scroll.
     */
    private static final int FILES_SCROLL_X_PADDING = 50;

    /**
     * The y padding between the top and bottom frame bounds for the files scroll.
     */
    private static final int FILES_SCROLL_Y_PADDING = 100;

    /**
     * The width of the font scroll list.
     */
    private static final int FONT_SCROLL_WIDTH = CONTENT_PANE_WIDTH / 2 - 2 * FILES_SCROLL_X_PADDING;

    /**
     * The height of the font scroll list.
     */
    private static final int FONT_SCROLL_HEIGHT = CONTENT_PANE_HEIGHT - 2 * FILES_SCROLL_Y_PADDING;

    /**
     * The reference to the font scroll list.
     */
    private static final AtomicReference<CyderScrollList> fontScrollReference = new AtomicReference<>();

    static {
        CyderScrollList scrollList = new CyderScrollList(FONT_SCROLL_WIDTH, FONT_SCROLL_HEIGHT,
                CyderScrollList.SelectionPolicy.SINGLE);
        scrollList.setItemAlignment(StyleConstants.ALIGN_LEFT);
        fontScrollReference.set(scrollList);
    }

    /**
     * The width of the apply font button.
     */
    private static final int fontButtonWidth = 130;

    /**
     * The height of the apply font button.
     */
    private static final int fontButtonHeight = 40;

    /**
     * The padding for the top and bottom of the font layout.
     */
    private static final int paddingPartition = 5;

    /**
     * The partition for the font label and bottom secondary spacer.
     */
    private static final int surroundingPanelPartition = 5;

    /**
     * The partition for the font files scroll.
     */
    private static final int fontLabelPartition = 80;

    /**
     * The font label font size.
     */
    private static final int fontLabelFontSize = 40;

    /**
     * The font label which the font is updated on to show the user what the selected font looks like.
     */
    private static CyderLabel fontLabel;

    /**
     * The button to apply the font.
     */
    private static CyderButton applyFontButton;

    /**
     * The partition layout for the font components.
     */
    private static CyderPartitionedLayout fontPartitionedLayout;

    /**
     * The text field for the foreground hex color.
     */
    private static CyderTextField foregroundField;

    /**
     * The color block preview for the foreground color.
     */
    private static JTextField foregroundColorBlock;

    /**
     * The text field for the window hex color.
     */
    private static CyderTextField windowField;

    /**
     * The color block preview for the window color.
     */
    private static JTextField windowColorBlock;

    /**
     * The text field for the background hex color.
     */
    private static CyderTextField backgroundField;

    /**
     * The color block preview for the background color.
     */
    private static JTextField backgroundColorBlock;

    /**
     * The partition space for the font and color panel within the horizontal partitioned layout.
     */
    private static final int fontColorHorizontalPartition = 50;

    /**
     * The partition length for a color panel.
     */
    private static final int colorPartitionedLayoutSpacerLen = 25;

    /**
     * The header label partition length for color labels.
     */
    private static final int colorHeaderLabelPartition = 25;

    /**
     * The partition for the hex information label.
     */
    private static final int hexInformationLabelPartition = 15;

    /**
     * The partition length for the color field.
     */
    private static final int colorFieldPartitionSpace = 70;

    /**
     * The partition length for the color block.
     */
    private static final int colorBlockPartitionSpace = 15;

    /**
     * The partition length for the color panel.
     */
    private static final int colorPanelPartitionSpace = 25;

    /**
     * The width for color field panels.
     */
    private static final int colorFieldPanelWidth = 250;

    /**
     * The height for color field panels.
     */
    private static final int colorFieldPanelHeight = 40;

    /**
     * Switches to the fonts and colors page.
     */
    private static void switchToFontAndColor() {
        CyderPartitionedLayout fontAndColorPartitionedLayout = new CyderPartitionedLayout();
        fontAndColorPartitionedLayout.setPartitionDirection(CyderPartitionedLayout.PartitionDirection.ROW);

        fontLabel = new CyderLabel("Fonts");
        fontLabel.setFont(new Font(UserUtil.getCyderUser().getFont(), Font.BOLD, fontLabelFontSize));
        fontLabel.setSize(FONT_SCROLL_WIDTH, 60);

        CyderLabel loadingLabel = new CyderLabel(CyderStrings.LOADING);
        loadingLabel.setFont(CyderFonts.DEFAULT_FONT);
        loadingLabel.setSize(FONT_SCROLL_WIDTH, FONT_SCROLL_HEIGHT);
        loadingLabel.setBackground(CyderColors.vanilla);
        loadingLabel.setBorder(new LineBorder(CyderColors.navy, 5));
        loadingLabel.setOpaque(true);

        CyderGridLayout gridLayout = new CyderGridLayout(2, 1);

        applyFontButton = new CyderButton("Apply Font");
        applyFontButton.setSize(fontButtonWidth, fontButtonHeight);
        applyFontButton.addActionListener(applyFontButtonActionListener);
        gridLayout.addComponent(applyFontButton);

        CyderButton resetFontAndColorButton = new CyderButton("Reset All");
        resetFontAndColorButton.setSize(fontButtonWidth, fontButtonHeight);
        resetFontAndColorButton.setToolTipText("Reset font and colors");
        resetFontAndColorButton.addActionListener(resetFontAndColorButtonActionListener);
        gridLayout.addComponent(resetFontAndColorButton);

        CyderPanel fontButtonGridPanel = new CyderPanel(gridLayout);
        fontButtonGridPanel.setSize(FONT_SCROLL_WIDTH, 60);

        fontPartitionedLayout = new CyderPartitionedLayout();
        fontPartitionedLayout.spacer(paddingPartition);
        fontPartitionedLayout.addComponent(fontLabel, surroundingPanelPartition);
        fontPartitionedLayout.addComponent(loadingLabel, fontLabelPartition);
        fontPartitionedLayout.addComponent(fontButtonGridPanel, surroundingPanelPartition);
        fontPartitionedLayout.spacer(paddingPartition);

        loadFonts();

        CyderPanel fontPanel = new CyderPanel(fontPartitionedLayout);
        fontPanel.setSize(CONTENT_PANE_WIDTH / 2, CONTENT_PANE_HEIGHT);
        fontAndColorPartitionedLayout.addComponent(fontPanel, fontColorHorizontalPartition);

        CyderLabel foregroundColorLabel = new CyderLabel("Foreground Color");
        foregroundColorLabel.setFont(CyderFonts.DEFAULT_FONT);
        foregroundColorLabel.setSize(300, 40);

        foregroundField = new CyderTextField(6);
        foregroundField.setHorizontalAlignment(JTextField.CENTER);
        foregroundField.setHexColorRegexMatcher();
        foregroundField.setText(UserUtil.getCyderUser().getForeground());
        foregroundField.setFont(CyderFonts.SEGOE_30);
        foregroundField.setToolTipText("Console input/output text color");
        foregroundField.addKeyListener(UiUtil.generateKeyAdapter(false, false, true, () -> {
            try {
                Color foregroundColor = ColorUtil.hexStringToColor(foregroundField.getText());
                foregroundColorBlock.setBackground(foregroundColor);
                UserUtil.getCyderUser().setForeground(foregroundField.getText());
                Console.INSTANCE.getOutputArea().setForeground(foregroundColor);
                Console.INSTANCE.getInputField().setForeground(foregroundColor);
                Console.INSTANCE.getInputField().setCaretColor(foregroundColor);
                Console.INSTANCE.getInputField().setCaret(new CyderCaret(foregroundColor));
                Preference.invokeRefresh(Preference.FOREGROUND);
            } catch (Exception ignored) {}
        }));
        foregroundField.setSize(160, 40);
        foregroundField.setOpaque(false);

        foregroundColorBlock = generateColorBlock(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getForeground()),
                "Foreground color preview");

        CyderLabel windowColorLabel = new CyderLabel("Window Color");
        windowColorLabel.setFont(CyderFonts.DEFAULT_FONT);
        windowColorLabel.setSize(300, 40);

        windowField = new CyderTextField(6);
        windowField.setHorizontalAlignment(JTextField.CENTER);
        windowField.setHexColorRegexMatcher();
        windowField.setText(UserUtil.getCyderUser().getWindowcolor());
        windowField.setFont(CyderFonts.SEGOE_30);
        windowField.setToolTipText("Window border color");
        windowField.addKeyListener(UiUtil.generateKeyAdapter(false, false, true, () -> {
            try {
                Color requestedWindowColor = ColorUtil.hexStringToColor(windowField.getText());
                windowColorBlock.setBackground(requestedWindowColor);
                UserUtil.getCyderUser().setWindowcolor(windowField.getText());
                CyderColors.setGuiThemeColor(requestedWindowColor);
                Preference.invokeRefresh(Preference.WINDOW_COLOR);
            } catch (Exception ignored) {}
        }));
        windowField.setOpaque(false);
        windowField.setSize(160, 40);

        windowColorBlock = generateColorBlock(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getWindowcolor()),
                "Window color preview");

        CyderLabel backgroundColorLabel = new CyderLabel("Background Color");
        backgroundColorLabel.setFont(CyderFonts.DEFAULT_FONT);
        backgroundColorLabel.setSize(300, 40);

        backgroundField = new CyderTextField(6);
        backgroundField.setHorizontalAlignment(JTextField.CENTER);
        backgroundField.setHexColorRegexMatcher();
        backgroundField.setText(UserUtil.getCyderUser().getBackground());
        backgroundField.setFont(CyderFonts.SEGOE_30);
        backgroundField.setToolTipText("Input field and output area fill color if enabled");
        backgroundField.addKeyListener(UiUtil.generateKeyAdapter(false, false, true, () -> {
            try {
                String backgroundColorString = backgroundField.getText();
                Color backgroundColor = ColorUtil.hexStringToColor(backgroundColorString);
                backgroundColorBlock.setBackground(backgroundColor);
                UserUtil.getCyderUser().setBackground(backgroundColorString);

                boolean outputFill = UserUtil.getCyderUser().getOutputfill().equals("1");
                boolean inputFill = UserUtil.getCyderUser().getInputfill().equals("1");
                boolean outputBorder = UserUtil.getCyderUser().getOutputborder().equals("1");
                boolean inputBorder = UserUtil.getCyderUser().getInputborder().equals("1");

                if (outputFill) {
                    Console.INSTANCE.getOutputArea().setOpaque(true);
                    Console.INSTANCE.getOutputArea().setBackground(backgroundColor);
                    Console.INSTANCE.getOutputArea().repaint();
                    Console.INSTANCE.getOutputArea().revalidate();
                }
                if (inputFill) {
                    Console.INSTANCE.getInputField().setOpaque(true);
                    Console.INSTANCE.getInputField().setBackground(backgroundColor);
                    Console.INSTANCE.getInputField().repaint();
                    Console.INSTANCE.getInputField().revalidate();
                }

                LineBorder inputOutputBorder = new LineBorder(backgroundColor, inputOutputBorderThickness, false);
                if (outputBorder) {
                    Console.INSTANCE.getOutputScroll().setBorder(inputOutputBorder);
                    Console.INSTANCE.getOutputScroll().repaint();
                    Console.INSTANCE.getOutputScroll().revalidate();
                }
                if (inputBorder) {
                    Console.INSTANCE.getInputField().setBorder(inputOutputBorder);
                    Console.INSTANCE.getInputField().repaint();
                    Console.INSTANCE.getInputField().revalidate();
                }
            } catch (Exception ignored) {}
        }));
        backgroundField.setOpaque(false);
        backgroundField.setSize(160, 40);

        backgroundColorBlock = generateColorBlock(ColorUtil.hexStringToColor(
                UserUtil.getCyderUser().getBackground()), "Background color preview");

        CyderGridLayout colorGridLayout = new CyderGridLayout(1, 3);
        CyderPartitionedLayout foregroundPartitionedLayout = new CyderPartitionedLayout();
        foregroundPartitionedLayout.spacer(colorPartitionedLayoutSpacerLen);
        foregroundPartitionedLayout.addComponent(foregroundColorLabel, colorHeaderLabelPartition);
        CyderPartitionedLayout foregroundFieldPartitionedLayout = new CyderPartitionedLayout();
        foregroundFieldPartitionedLayout.setPartitionDirection(CyderPartitionedLayout.PartitionDirection.ROW);
        foregroundFieldPartitionedLayout.addComponent(generateHexInformationLabel(), hexInformationLabelPartition);
        foregroundFieldPartitionedLayout.addComponent(foregroundField, colorFieldPartitionSpace);
        foregroundFieldPartitionedLayout.addComponent(foregroundColorBlock, colorBlockPartitionSpace);
        CyderPanel foregroundFieldPanel = new CyderPanel(foregroundFieldPartitionedLayout);
        foregroundFieldPanel.setSize(colorFieldPanelWidth, colorFieldPanelHeight);
        foregroundPartitionedLayout.addComponent(foregroundFieldPanel, colorPanelPartitionSpace);
        foregroundPartitionedLayout.spacer(colorPartitionedLayoutSpacerLen);
        colorGridLayout.addComponent(new CyderPanel(foregroundPartitionedLayout));

        CyderPartitionedLayout windowPartitionedLayout = new CyderPartitionedLayout();
        windowPartitionedLayout.spacer(colorPartitionedLayoutSpacerLen);
        windowPartitionedLayout.addComponent(windowColorLabel, colorHeaderLabelPartition);
        CyderPartitionedLayout windowFieldPartitionedLayout = new CyderPartitionedLayout();
        windowFieldPartitionedLayout.setPartitionDirection(CyderPartitionedLayout.PartitionDirection.ROW);
        windowFieldPartitionedLayout.addComponent(generateHexInformationLabel(), hexInformationLabelPartition);
        windowFieldPartitionedLayout.addComponent(windowField, colorFieldPartitionSpace);
        windowFieldPartitionedLayout.addComponent(windowColorBlock, colorBlockPartitionSpace);
        CyderPanel windowFieldPanel = new CyderPanel(windowFieldPartitionedLayout);
        windowFieldPanel.setSize(colorFieldPanelWidth, colorFieldPanelHeight);
        windowPartitionedLayout.addComponent(windowFieldPanel, colorPanelPartitionSpace);
        windowPartitionedLayout.spacer(colorPartitionedLayoutSpacerLen);
        colorGridLayout.addComponent(new CyderPanel(windowPartitionedLayout));

        CyderPartitionedLayout backgroundPartitionedLayout = new CyderPartitionedLayout();
        backgroundPartitionedLayout.spacer(colorPartitionedLayoutSpacerLen);
        backgroundPartitionedLayout.addComponent(backgroundColorLabel, colorHeaderLabelPartition);
        CyderPartitionedLayout backgroundFieldPartitionedLayout = new CyderPartitionedLayout();
        backgroundFieldPartitionedLayout.setPartitionDirection(CyderPartitionedLayout.PartitionDirection.ROW);
        backgroundFieldPartitionedLayout.addComponent(generateHexInformationLabel(), hexInformationLabelPartition);
        backgroundFieldPartitionedLayout.addComponent(backgroundField, colorFieldPartitionSpace);
        backgroundFieldPartitionedLayout.addComponent(backgroundColorBlock, colorBlockPartitionSpace);
        CyderPanel backgroundFieldPanel = new CyderPanel(backgroundFieldPartitionedLayout);
        backgroundFieldPanel.setSize(colorFieldPanelWidth, colorFieldPanelHeight);
        backgroundPartitionedLayout.addComponent(backgroundFieldPanel, colorPanelPartitionSpace);
        backgroundPartitionedLayout.spacer(colorPartitionedLayoutSpacerLen);
        colorGridLayout.addComponent(new CyderPanel(backgroundPartitionedLayout));

        CyderPanel colorPanel = new CyderPanel(colorGridLayout);
        colorPanel.setSize(CONTENT_PANE_WIDTH / 2, CONTENT_PANE_HEIGHT);
        fontAndColorPartitionedLayout.addComponent(colorPanel, fontColorHorizontalPartition);
        editUserFrame.setCyderLayout(fontAndColorPartitionedLayout);
    }

    /**
     * The length of generated color blocks.
     */
    private static final int COLOR_BLOCK_LEN = 40;

    /**
     * The thickness of the color block border.
     */
    private static final int colorBlockBorderThickness = 5;

    /**
     * The line border for generated color blocks.
     */
    private static final LineBorder colorBlockBorder = new LineBorder(
            CyderColors.navy, colorBlockBorderThickness, false);

    /**
     * Returns a color block of size 40,40 to use a color preview.
     *
     * @param backgroundColor the initial color of the preview block
     * @param tooltip         the tooltip for the color block
     * @return the color block
     */
    private static JTextField generateColorBlock(Color backgroundColor, String tooltip) {
        Preconditions.checkNotNull(backgroundColor);
        Preconditions.checkNotNull(tooltip);
        Preconditions.checkArgument(!tooltip.isEmpty());

        JTextField ret = new JTextField();
        ret.setHorizontalAlignment(JTextField.CENTER);
        ret.setFocusable(false);
        ret.setCursor(null);
        ret.setBackground(backgroundColor);
        ret.setForeground(backgroundColor);
        ret.setSelectedTextColor(backgroundColor);
        ret.setSize(COLOR_BLOCK_LEN, COLOR_BLOCK_LEN);
        ret.setToolTipText(tooltip);
        ret.setBorder(colorBlockBorder);

        return ret;
    }

    /**
     * Loads the fonts from the local {@link GraphicsEnvironment} and updates the fonts scroll list.
     */
    private static void loadFonts() {
        CyderThreadRunner.submit(() -> {
            fontScrollReference.get().removeAllElements();

            LinkedList<String> fontList = new LinkedList<>();
            Collections.addAll(fontList, GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames());

            CyderScrollList reference = fontScrollReference.get();
            for (String fontName : fontList) {
                reference.addElementWithSingleCLickAction(fontName,
                        () -> {
                            applyFontButton.setToolTipText("Apply font: " + fontName);
                            fontLabel.setFont(new Font(fontName, Font.BOLD, fontLabelFontSize));
                        });
            }

            if (currentPage == Page.FONT_AND_COLOR) {
                CyderScrollList scrollList = fontScrollReference.get();
                scrollList.selectElement(UserUtil.getCyderUser().getFont());
                JLabel fontLabel = scrollList.generateScrollList();
                fontLabel.setSize(FONT_SCROLL_WIDTH, FONT_SCROLL_HEIGHT);
                fontPartitionedLayout.setComponent(fontLabel, 2);
            }
        }, "Preferences Font Loader");
    }

    /**
     * The key for retrieving the font metric from the props.
     */
    private static final String FONT_METRIC = "font_metric";

    /**
     * The action listener for the apply font button.
     */
    @SuppressWarnings("MagicConstant") /* font metrics are always checked */
    private static final ActionListener applyFontButtonActionListener = e -> {
        CyderScrollList reference = fontScrollReference.get();
        if (reference == null || reference.getSelectedElements().isEmpty()) {
            return;
        }

        String selectedFont = reference.getSelectedElements().get(0);

        if (selectedFont != null) {
            UserUtil.getCyderUser().setFont(selectedFont);

            int requestedFontMetric = Integer.parseInt(PropLoader.getString(FONT_METRIC));
            if (!NumberUtil.isValidFontMetric(requestedFontMetric)) {
                requestedFontMetric = Font.BOLD;
            }

            int requestedFontSize = Integer.parseInt(UserUtil.getCyderUser().getFontsize());

            Font applyFont = new Font(selectedFont, requestedFontMetric, requestedFontSize);
            Console.INSTANCE.getOutputArea().setFont(applyFont);
            Console.INSTANCE.getInputField().setFont(applyFont);

            editUserFrame.notify("Applied font: " + selectedFont);
        }
    };

    @SuppressWarnings("MagicConstant") /* font metrics are always checked */
    private static final ActionListener resetFontAndColorButtonActionListener = e -> {
        String defaultForeground = Preference.get(Preference.FOREGROUND).getDefaultValue().toString();
        Color defaultForegroundColor = ColorUtil.hexStringToColor(defaultForeground);

        String defaultBackground = Preference.get(Preference.BACKGROUND).getDefaultValue().toString();
        Color defaultBackgroundColor = ColorUtil.hexStringToColor(defaultBackground);

        String defaultWindow = Preference.get(Preference.WINDOW_COLOR).getDefaultValue().toString();
        Color defaultWindowColor = ColorUtil.hexStringToColor(defaultWindow);

        String defaultFontName = Preference.get(Preference.FONT).getDefaultValue().toString();
        int defaultFontMetric =
                Integer.parseInt(Preference.get(Preference.FONT_METRIC).getDefaultValue().toString());
        int defaultFontSize = Integer.parseInt(Preference.get(Preference.FONT_SIZE).getDefaultValue().toString());

        UserUtil.getCyderUser().setForeground(defaultForeground);
        foregroundColorBlock.setBackground(defaultForegroundColor);
        foregroundField.setText(defaultForeground);
        Console.INSTANCE.getOutputArea().setForeground(defaultForegroundColor);
        Console.INSTANCE.getInputField().setForeground(defaultForegroundColor);
        Console.INSTANCE.getInputField().setCaretColor(defaultForegroundColor);
        Console.INSTANCE.getInputField().setCaret(new CyderCaret(defaultForegroundColor));
        Preference.invokeRefresh(Preference.FOREGROUND);

        Font applyFont = new Font(defaultFontName, defaultFontMetric, defaultFontSize);
        Console.INSTANCE.getOutputArea().setFont(applyFont);
        Console.INSTANCE.getInputField().setFont(applyFont);
        if (fontScrollReference.get() != null) {
            fontScrollReference.get().clearSelectedElements();
        }
        fontLabel.setFont(applyFont);
        if (fontScrollReference.get() != null) {
            JScrollBar scrollBar = fontScrollReference.get().getScrollPane().getVerticalScrollBar();
            scrollBar.setValue(scrollBar.getMinimum());
        }
        Preference.invokeRefresh(Preference.FONT);

        UserUtil.getCyderUser().setBackground(defaultBackground);
        backgroundColorBlock.setBackground(defaultBackgroundColor);
        backgroundField.setText(defaultBackground);

        boolean outputFill = UserUtil.getCyderUser().getOutputfill().equals("1");
        boolean inputFill = UserUtil.getCyderUser().getInputfill().equals("1");
        if (outputFill) {
            Console.INSTANCE.getOutputArea().setOpaque(true);
            Console.INSTANCE.getOutputArea().setBackground(defaultBackgroundColor);
            Console.INSTANCE.getOutputArea().repaint();
            Console.INSTANCE.getOutputArea().revalidate();
        }
        if (inputFill) {
            Console.INSTANCE.getInputField().setOpaque(true);
            Console.INSTANCE.getInputField().setBackground(defaultBackgroundColor);
            Console.INSTANCE.getInputField().repaint();
            Console.INSTANCE.getInputField().revalidate();
        }
        Preference.invokeRefresh(Preference.BACKGROUND);

        UserUtil.getCyderUser().setWindowcolor(defaultWindow);
        windowColorBlock.setBackground(defaultWindowColor);
        windowField.setText(defaultWindow);
        windowColorBlock.setBackground((defaultWindowColor));
        CyderColors.setGuiThemeColor((defaultWindowColor));
        Preference.invokeRefresh(Preference.WINDOW_COLOR);
        Preference.invokeRefresh(Preference.WINDOW_COLOR);

        editUserFrame.notify("Default fonts and colors reset");
    };

    /**
     * The width of the hex label.
     */
    private static final int HEX_LABEL_WIDTH = 80;

    /**
     * The height of the the hex label.
     */
    private static final int HEX_LABEL_HEIGHT = 40;

    /**
     * The hex label text.
     */
    private static final String HEX = "Hex";

    /**
     * Returns a {@link CyderLabel} to indicate a text field accepts a hex value.
     *
     * @return a {@link CyderLabel} to indicate a text field accepts a hex value
     */
    private static CyderLabel generateHexInformationLabel() {
        CyderLabel hexLabel = new CyderLabel(HEX);
        hexLabel.setFont(CyderFonts.DEFAULT_FONT);
        hexLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ColorConverterWidget.getInstance().innerShowGui(editUserFrame);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hexLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hexLabel.setForeground(CyderColors.navy);
            }
        });
        hexLabel.setSize(HEX_LABEL_WIDTH, HEX_LABEL_HEIGHT);

        return hexLabel;
    }

    /**
     * The width of components printed to the preferences scroll.
     */
    private static final int PRINTED_PREF_COMPONENT_WIDTH = 400;

    /**
     * The height of components printed to the preferences scroll.
     */
    private static final int PRINTED_PREF_COMPONENT_HEIGHT = 100;

    /**
     * The size of the preference checkboxes.
     */
    private static final int checkboxSize = 60;

    /**
     * The text used for preference labels.
     */
    private static final String PRINT_LABEL_MAGIC_TEXT = StringUtil.generateTextForCustomComponent(6);

    /**
     * Switches to the preferences preference page.
     */
    private static void switchToPreferences() {
        CyderPartitionedLayout preferencesPartitionedLayout = new CyderPartitionedLayout();

        CyderLabel prefsTitle = new CyderLabel("Preferences");
        prefsTitle.setFont(CyderFonts.DEFAULT_FONT);
        prefsTitle.setSize(200, 40);

        JTextPane preferencePane = new JTextPane();
        preferencePane.setEditable(false);
        preferencePane.setAutoscrolls(false);
        preferencePane.setSize(CONTENT_PANE_WIDTH, CONTENT_PANE_HEIGHT);
        preferencePane.setFocusable(true);
        preferencePane.setOpaque(false);
        preferencePane.setBackground(Color.white);

        StringUtil printingUtil = new StringUtil(new CyderOutputPane(preferencePane));

        for (Preference preference : Preference.getPreferences()) {
            if (preference.getIgnoreForToggleSwitches()) {
                continue;
            }

            JLabel preferenceContentLabel = new JLabel(PRINT_LABEL_MAGIC_TEXT);
            preferenceContentLabel.setSize(PRINTED_PREF_COMPONENT_WIDTH, PRINTED_PREF_COMPONENT_HEIGHT);

            CyderLabel preferenceNameLabel = new CyderLabel(preference.getDisplayName());
            preferenceNameLabel.setFont(CyderFonts.DEFAULT_FONT_SMALL);
            preferenceNameLabel.setBounds((int) (PRINTED_PREF_COMPONENT_WIDTH * 0.40), 0,
                    PRINTED_PREF_COMPONENT_WIDTH / 2,
                    PRINTED_PREF_COMPONENT_HEIGHT);
            preferenceContentLabel.add(preferenceNameLabel);

            boolean selected = UserUtil.getUserDataById(preference.getID()).equalsIgnoreCase("1");
            CyderCheckbox checkbox = new CyderCheckbox(selected);
            checkbox.setToolTipText(preference.getTooltip());
            checkbox.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    UserUtil.setUserDataById(preference.getID(), checkbox.isChecked() ? "1" : "0");
                    Preference.invokeRefresh(preference.getID());
                }
            });
            checkbox.setBounds(PRINTED_PREF_COMPONENT_WIDTH - checkboxSize / 2,
                    PRINTED_PREF_COMPONENT_HEIGHT / 2 - checkboxSize / 2 + 10, checkboxSize, checkboxSize);
            preferenceContentLabel.add(checkbox);

            printingUtil.printlnComponent(preferenceContentLabel);
            printingUtil.println("");
        }

        CyderScrollPane preferenceScroll = new CyderScrollPane(preferencePane);
        preferenceScroll.setThumbSize(8);
        preferenceScroll.getViewport().setOpaque(false);
        preferenceScroll.setFocusable(true);
        preferenceScroll.setOpaque(false);
        preferenceScroll.setThumbColor(CyderColors.regularPink);
        preferenceScroll.setBackground(Color.white);
        preferenceScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        preferenceScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        preferenceScroll.setSize(CONTENT_PANE_WIDTH - 50, CONTENT_PANE_HEIGHT - 100);
        preferenceScroll.setBorder(new LineBorder(CyderColors.navy, 5));
        preferencePane.setCaretPosition(0);

        preferencesPartitionedLayout.addComponent(prefsTitle, 10);
        preferencesPartitionedLayout.addComponent(preferenceScroll, 90);

        editUserFrame.setCyderLayout(preferencesPartitionedLayout);
    }

    /**
     * The password field for a new password.
     */
    private static CyderPasswordField newPasswordField;

    /**
     * The password confirmation field for a new password.
     */
    private static CyderPasswordField newPasswordConfirmationField;

    /**
     * The label for opening the java guide for date patterns.
     */
    private static JLabel changeConsoleDatePatternLabel;

    /**
     * The width of printed field panels.
     */
    private static final int fieldsPagePanelWidth = CONTENT_PANE_WIDTH - 100;

    /**
     * The height of printed field panels.
     */
    private static final int fieldsPagePanelHeight = 300;

    /**
     * The height of printed field header labels.
     */
    private static final int fieldHeaderLabelHeight = 50;

    /**
     * The width of a component on a printed field panel.
     */
    private static final int fieldMainComponentWidth = 300;

    /**
     * The height of a component on a printed field panel.
     */
    private static final int fieldMainComponentHeight = 40;

    /**
     * Switches to the field input preference page.
     */
    private static void switchToFieldInputs() {
        CyderLabel prefsTitle = new CyderLabel("Field Inputs");
        prefsTitle.setFont(CyderFonts.DEFAULT_FONT);
        prefsTitle.setSize(CONTENT_PANE_WIDTH, 50);

        JTextPane fieldInputsPane = new JTextPane();
        fieldInputsPane.setEditable(false);
        fieldInputsPane.setAutoscrolls(false);
        fieldInputsPane.setFocusable(true);
        fieldInputsPane.setOpaque(false);
        fieldInputsPane.setBackground(Color.white);

        StringUtil printingUtil = new StringUtil(new CyderOutputPane(fieldInputsPane));

        CyderScrollPane fieldInputsScroll = new CyderScrollPane(fieldInputsPane);
        fieldInputsScroll.setThumbSize(7);
        fieldInputsScroll.getViewport().setOpaque(false);
        fieldInputsScroll.setFocusable(true);
        fieldInputsScroll.setOpaque(false);
        fieldInputsScroll.setThumbColor(CyderColors.regularPink);
        fieldInputsScroll.setBackground(Color.white);
        fieldInputsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        fieldInputsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        fieldInputsScroll.setBorder(new LineBorder(CyderColors.navy, 5));
        fieldInputsScroll.setSize(CONTENT_PANE_WIDTH - 50, CONTENT_PANE_HEIGHT - 100);

        JLabel changeUsernameLabel = new CyderLabel("Change Username");
        changeUsernameLabel.setForeground(CyderColors.navy);
        changeUsernameLabel.setFont(CyderFonts.DEFAULT_FONT_SMALL);
        changeUsernameLabel.setHorizontalAlignment(JLabel.CENTER);
        changeUsernameLabel.setSize(fieldsPagePanelWidth, fieldHeaderLabelHeight);

        CyderTextField newUsernameField = new CyderTextField();
        newUsernameField.setHorizontalAlignment(JTextField.CENTER);
        newUsernameField.setSize(fieldMainComponentWidth, fieldMainComponentHeight);
        newUsernameField.addActionListener(e -> {
            attemptChangeUsername(newUsernameField.getTrimmedText());
            newUsernameField.setText(UserUtil.getCyderUser().getName());
        });
        newUsernameField.setText(UserUtil.getCyderUser().getName());

        CyderButton changeUsernameButton = new CyderButton("Change username");
        changeUsernameButton.setSize(fieldMainComponentWidth, fieldMainComponentHeight);
        changeUsernameButton.setToolTipText("Change username");
        changeUsernameButton.addActionListener(e -> {
            attemptChangeUsername(newUsernameField.getTrimmedText());
            newUsernameField.setText(UserUtil.getCyderUser().getName());
        });

        CyderGridLayout changeUsernameLayout = new CyderGridLayout(1, 3);
        changeUsernameLayout.addComponent(changeUsernameLabel);
        changeUsernameLayout.addComponent(newUsernameField);
        changeUsernameLayout.addComponent(changeUsernameButton);

        CyderPanel changeUsernamePanel = new CyderPanel(changeUsernameLayout);
        changeUsernamePanel.setText(StringUtil.generateTextForCustomComponent(12));
        changeUsernamePanel.setSize(fieldsPagePanelWidth, fieldsPagePanelHeight);
        printingUtil.printlnComponent(changeUsernamePanel);

        JLabel changePasswordLabel = new CyderLabel("Change Password");
        changePasswordLabel.setForeground(CyderColors.navy);
        changePasswordLabel.setFont(CyderFonts.DEFAULT_FONT_SMALL);
        changePasswordLabel.setHorizontalAlignment(JLabel.CENTER);
        changePasswordLabel.setSize(fieldsPagePanelWidth, fieldHeaderLabelHeight);

        newPasswordField = new CyderPasswordField();
        newPasswordField.setToolTipText("New password");
        newPasswordField.setHorizontalAlignment(JTextField.CENTER);
        newPasswordField.setSize(fieldMainComponentWidth, fieldMainComponentHeight);
        newPasswordField.addActionListener(e -> {
            changePassword(newPasswordField.getPassword(), newPasswordConfirmationField.getPassword());
            newPasswordField.setText("");
            newPasswordConfirmationField.setText("");
        });

        newPasswordConfirmationField = new CyderPasswordField();
        newPasswordConfirmationField.setToolTipText("New password confirmation");
        newPasswordConfirmationField.setHorizontalAlignment(JTextField.CENTER);
        newPasswordConfirmationField.setSize(fieldMainComponentWidth, fieldMainComponentHeight);
        newPasswordConfirmationField.addActionListener(e -> {
            changePassword(newPasswordField.getPassword(), newPasswordConfirmationField.getPassword());
            newPasswordField.setText("");
            newPasswordConfirmationField.setText("");
        });

        CyderButton changePasswordButton = new CyderButton("Change password");
        changePasswordButton.setSize(fieldMainComponentWidth, fieldMainComponentHeight);
        changePasswordButton.setToolTipText("Change password");
        changePasswordButton.addActionListener(e -> {
            changePassword(newPasswordField.getPassword(), newPasswordConfirmationField.getPassword());
            newPasswordField.setText("");
            newPasswordConfirmationField.setText("");
        });

        CyderGridLayout changePasswordLayout = new CyderGridLayout(1, 4);
        changePasswordLayout.addComponent(changePasswordLabel);
        changePasswordLayout.addComponent(newPasswordField);
        changePasswordLayout.addComponent(newPasswordConfirmationField);
        changePasswordLayout.addComponent(changePasswordButton);

        CyderPanel changePasswordPanel = new CyderPanel(changePasswordLayout);
        changePasswordPanel.setText(StringUtil.generateTextForCustomComponent(14));
        changePasswordPanel.setSize(fieldsPagePanelWidth, fieldsPagePanelHeight);
        printingUtil.printlnComponent(changePasswordPanel);

        changeConsoleDatePatternLabel = new CyderLabel("Change Console Date Pattern");
        changeConsoleDatePatternLabel.setForeground(CyderColors.navy);
        changeConsoleDatePatternLabel.addMouseListener(consoleDatePatternLabelMouseListener);
        changeConsoleDatePatternLabel.setFont(CyderFonts.DEFAULT_FONT_SMALL);
        changeConsoleDatePatternLabel.setHorizontalAlignment(JLabel.CENTER);
        changeConsoleDatePatternLabel.setSize(fieldsPagePanelWidth, fieldHeaderLabelHeight);

        CyderTextField changeConsoleDatePatternField = new CyderTextField();
        changeConsoleDatePatternField.setHorizontalAlignment(JTextField.CENTER);
        changeConsoleDatePatternField.setSize(fieldMainComponentWidth, fieldMainComponentHeight);
        changeConsoleDatePatternField.addActionListener(e -> {
            setConsoleDatePattern(changeConsoleDatePatternField.getTrimmedText());
            changeConsoleDatePatternField.setText(UserUtil.getCyderUser().getConsoleclockformat());
        });
        changeConsoleDatePatternField.setText(UserUtil.getCyderUser().getConsoleclockformat());

        CyderButton changeConsoleDaterPatternButton = new CyderButton("Change date pattern");
        changeConsoleDaterPatternButton.setSize(fieldMainComponentWidth, fieldMainComponentHeight);
        changeConsoleDaterPatternButton.setToolTipText("Change console date pattern");
        changeConsoleDaterPatternButton.addActionListener(e -> {
            setConsoleDatePattern(changeConsoleDatePatternField.getTrimmedText());
            changeConsoleDatePatternField.setText(UserUtil.getCyderUser().getConsoleclockformat());
        });

        CyderGridLayout changeConsoleDaterPatternLayout = new CyderGridLayout(1, 3);
        changeConsoleDaterPatternLayout.addComponent(changeConsoleDatePatternLabel);
        changeConsoleDaterPatternLayout.addComponent(changeConsoleDatePatternField);
        changeConsoleDaterPatternLayout.addComponent(changeConsoleDaterPatternButton);

        CyderPanel changeConsoleDaterPatternPanel = new CyderPanel(changeConsoleDaterPatternLayout);
        changeConsoleDaterPatternPanel.setText(StringUtil.generateTextForCustomComponent(12));
        changeConsoleDaterPatternPanel.setSize(fieldsPagePanelWidth, fieldsPagePanelHeight);
        printingUtil.printlnComponent(changeConsoleDaterPatternPanel);

        JLabel deleteUserLabel = new CyderLabel("Delete User");
        deleteUserLabel.setForeground(CyderColors.navy);
        deleteUserLabel.setFont(CyderFonts.DEFAULT_FONT_SMALL);
        deleteUserLabel.setHorizontalAlignment(JLabel.CENTER);
        deleteUserLabel.setSize(fieldsPagePanelWidth, fieldHeaderLabelHeight);

        CyderPasswordField deleteUserConfirmationField = new CyderPasswordField();
        deleteUserConfirmationField.setToolTipText("Password confirmation");
        deleteUserConfirmationField.setHorizontalAlignment(JTextField.CENTER);
        deleteUserConfirmationField.setSize(fieldMainComponentWidth, fieldMainComponentHeight);
        deleteUserConfirmationField.addActionListener(e -> {
            deleteUser(deleteUserConfirmationField.getPassword());
            deleteUserConfirmationField.setText("");
        });

        CyderButton deleteUserButton = new CyderButton("Delete user");
        deleteUserButton.setSize(fieldMainComponentWidth, fieldMainComponentHeight);
        deleteUserButton.setToolTipText("Delete user");
        deleteUserButton.addActionListener(e -> {
            deleteUser(deleteUserConfirmationField.getPassword());
            deleteUserConfirmationField.setText("");
        });

        CyderGridLayout deleteUserLayout = new CyderGridLayout(1, 3);
        deleteUserLayout.addComponent(deleteUserLabel);
        deleteUserLayout.addComponent(deleteUserConfirmationField);
        deleteUserLayout.addComponent(deleteUserButton);

        CyderPanel deleteUserPanel = new CyderPanel(deleteUserLayout);
        deleteUserPanel.setText(StringUtil.generateTextForCustomComponent(12));
        deleteUserPanel.setSize(fieldsPagePanelWidth, fieldsPagePanelHeight);
        printingUtil.printlnComponent(deleteUserPanel);

        JLabel addMapLabel = new CyderLabel("Add Map");
        addMapLabel.setForeground(CyderColors.navy);
        addMapLabel.setFont(CyderFonts.DEFAULT_FONT_SMALL);
        addMapLabel.setHorizontalAlignment(JLabel.CENTER);
        addMapLabel.setSize(fieldsPagePanelWidth, fieldHeaderLabelHeight);

        CyderTextField addMapNameField = new CyderTextField();
        addMapNameField.setToolTipText("New Map Name");
        addMapNameField.setHorizontalAlignment(JTextField.CENTER);
        addMapNameField.setSize(fieldMainComponentWidth, fieldMainComponentHeight);

        CyderTextField addMapLinkField = new CyderTextField();
        addMapLinkField.setToolTipText("New Map Link");
        addMapLinkField.setHorizontalAlignment(JTextField.CENTER);
        addMapLinkField.setSize(fieldMainComponentWidth, fieldMainComponentHeight);

        CyderButton addMapButton = new CyderButton("Add map");
        addMapButton.setSize(fieldMainComponentWidth, fieldMainComponentHeight);
        addMapButton.addActionListener(e -> {
            if (!StringUtil.isNullOrEmpty(addMapNameField.getText())
                    && !StringUtil.isNullOrEmpty(addMapLinkField.getText())) {
                addMap(addMapNameField.getTrimmedText(), addMapLinkField.getTrimmedText());
                addMapNameField.setText("");
                addMapLinkField.setText("");
            }
        });

        CyderButton showCurrentMapsButton = new CyderButton("Show current maps");
        showCurrentMapsButton.setSize(fieldMainComponentWidth, fieldMainComponentHeight);
        showCurrentMapsButton.addActionListener(e -> showCurrentMaps());

        CyderGridLayout addMapLayout = new CyderGridLayout(1, 5);
        addMapLayout.addComponent(addMapLabel);
        addMapLayout.addComponent(addMapNameField);
        addMapLayout.addComponent(addMapLinkField);
        addMapLayout.addComponent(addMapButton);
        addMapLayout.addComponent(showCurrentMapsButton);

        CyderPanel addMapPanel = new CyderPanel(addMapLayout);
        addMapPanel.setText(StringUtil.generateTextForCustomComponent(16));
        addMapPanel.setSize(fieldsPagePanelWidth, fieldsPagePanelHeight);
        printingUtil.printlnComponent(addMapPanel);

        JLabel removeMapLabel = new CyderLabel("Remove Map");
        removeMapLabel.setForeground(CyderColors.navy);
        removeMapLabel.setFont(CyderFonts.DEFAULT_FONT_SMALL);
        removeMapLabel.setHorizontalAlignment(JLabel.CENTER);
        removeMapLabel.setSize(fieldsPagePanelWidth, fieldHeaderLabelHeight);

        CyderTextField removeMapNameField = new CyderTextField();
        removeMapNameField.setToolTipText("Remove Map Name");
        removeMapNameField.setHorizontalAlignment(JTextField.CENTER);
        removeMapNameField.setSize(fieldMainComponentWidth, fieldMainComponentHeight);

        CyderButton removeMapButton = new CyderButton("Remove map");
        removeMapButton.setSize(fieldMainComponentWidth, fieldMainComponentHeight);
        removeMapButton.addActionListener(e -> {
            if (!StringUtil.isNullOrEmpty(removeMapNameField.getText())) {
                removeMap(removeMapNameField.getText());
                removeMapNameField.setText("");
            }
        });

        CyderButton showCurrentMapsButtonForRemoveSection = new CyderButton("Show current maps");
        showCurrentMapsButtonForRemoveSection.setSize(fieldMainComponentWidth, fieldMainComponentHeight);
        showCurrentMapsButtonForRemoveSection.addActionListener(e -> showCurrentMaps());

        CyderGridLayout removeMapLayout = new CyderGridLayout(1, 4);
        removeMapLayout.addComponent(removeMapLabel);
        removeMapLayout.addComponent(removeMapNameField);
        removeMapLayout.addComponent(removeMapButton);
        removeMapLayout.addComponent(showCurrentMapsButtonForRemoveSection);

        CyderPanel removeMapPanel = new CyderPanel(removeMapLayout);
        removeMapPanel.setText(StringUtil.generateTextForCustomComponent(14));
        removeMapPanel.setSize(fieldsPagePanelWidth, fieldsPagePanelHeight);
        printingUtil.printlnComponent(removeMapPanel);

        StyledDocument doc = fieldInputsPane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        fieldInputsPane.setCaretPosition(0);

        CyderPartitionedLayout fieldsPartitionedInput = new CyderPartitionedLayout();
        fieldsPartitionedInput.addComponent(prefsTitle, 10);
        fieldsPartitionedInput.addComponent(fieldInputsScroll, 90);
        editUserFrame.setCyderLayout(fieldsPartitionedInput);
    }

    /**
     * The mouse listener for the console date pattern label.
     */
    private static final MouseListener consoleDatePatternLabelMouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            NetworkUtil.openUrl(CyderUrls.SIMPLE_DATE_PATTERN_GUIDE);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            changeConsoleDatePatternLabel.setForeground(CyderColors.regularRed);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            changeConsoleDatePatternLabel.setForeground(CyderColors.vanilla);
        }
    };

    /**
     * The string to prompt the user with when confirming an account deletion.
     */
    private static final String confirmationString = "Final warning: you are about to"
            + " delete your Cyder account. All files, pictures, downloaded music, notes,"
            + " romantic partners, and the world will be deleted. Are you ABSOLUTELY sure you wish to continue?";

    /**
     * The name for the waiter thread to confirm account deletion.
     */
    private static final String ACCOUNT_DELETION_CONFIRMATION = "Account Deletion Confirmation";

    /**
     * Attempts to delete the current user's account if the password is valid.
     *
     * @param password the confirmation password
     */
    private static void deleteUser(char[] password) {
        Preconditions.checkNotNull(password);

        String doublyHashedPassword = SecurityUtil.toHexString(SecurityUtil.getSha256(
                SecurityUtil.toHexString(SecurityUtil.getSha256(password)).toCharArray()));

        if (!doublyHashedPassword.equals(UserUtil.getCyderUser().getPass())) {
            editUserFrame.notify("Invalid password; user not deleted");
            return;
        }

        CyderThreadRunner.submit(() -> {
            boolean delete = GetterUtil.getInstance().getConfirmation(
                    new GetterUtil.Builder(confirmationString)
                            .setRelativeTo(editUserFrame));

            if (!delete) {
                editUserFrame.notify("Account not deleted");
                return;
            }

            Console.INSTANCE.closeFrame(false, true);

            UiUtil.closeAllFrames(true);

            OSUtil.deleteFile(OSUtil.buildFile(Dynamic.PATH,
                    Dynamic.USERS.getDirectoryName(), Console.INSTANCE.getUuid()));

            OSUtil.exit(ExitCondition.UserDeleted);
        }, ACCOUNT_DELETION_CONFIRMATION);
    }

    /**
     * Attempts to change the user's password if the passwords match.
     *
     * @param newPassword     the new requested password
     * @param newPasswordConf the confirmation of the new requested password
     */
    private static void changePassword(char[] newPassword, char[] newPasswordConf) {
        Preconditions.checkNotNull(newPassword);
        Preconditions.checkNotNull(newPasswordConf);

        UserUtil.Validation passwordValid = UserUtil.validatePassword(newPassword, newPasswordConf);

        if (passwordValid.valid()) {
            UserUtil.getCyderUser().setPass(SecurityUtil.toHexString(SecurityUtil.getSha256(
                    SecurityUtil.toHexString(SecurityUtil.getSha256(newPassword)).toCharArray())));
            editUserFrame.notify("Password successfully changed");
        } else {
            editUserFrame.notify(passwordValid.message());
        }

        Arrays.fill(newPasswordConf, '\0');
        Arrays.fill(newPassword, '\0');
    }

    /**
     * Attempts to change the current user's username to the provided one.
     *
     * @param newUsername the requested new username
     */
    private static void attemptChangeUsername(String newUsername) {
        Preconditions.checkNotNull(newUsername);

        UserUtil.Validation validUsername = UserUtil.validateUsername(newUsername);

        if (validUsername.valid()) {
            UserUtil.getCyderUser().setName(newUsername);
            editUserFrame.notify("Username successfully changed to \"" + newUsername + "\"");
            Console.INSTANCE.refreshConsoleSuperTitle();
        } else {
            editUserFrame.notify(validUsername.message());
        }
    }

    /**
     * Sets the console date pattern to the requested one provided it is valid.
     *
     * @param consoleDatePattern the new console date pattern
     */
    private static void setConsoleDatePattern(String consoleDatePattern) {
        Preconditions.checkNotNull(consoleDatePattern);
        Preconditions.checkArgument(!consoleDatePattern.isEmpty());
        Preconditions.checkArgument(validateDatePattern(consoleDatePattern));

        UserUtil.getCyderUser().setConsoleclockformat(consoleDatePattern);
        Console.INSTANCE.refreshClockText();
    }

    /**
     * Returns whether the provided date pattern is valid.
     *
     * @param datePattern the user-entered date pattern
     * @return whether the provided date pattern is valid
     */
    @SuppressWarnings("unused") /* Validation object, no difference because of String pool */
    private static boolean validateDatePattern(String datePattern) {
        Preconditions.checkNotNull(datePattern);
        Preconditions.checkArgument(!datePattern.isEmpty());

        try {
            String validated = new SimpleDateFormat(datePattern).format(new Date());
            return true;
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
        }

        return false;
    }

    /**
     * An escaped quote character.
     */
    private static final String ESCAPED_QUOTE = "\"";

    /**
     * The maps keyword.
     */
    private static final String MAPS = "maps";

    /**
     * Shows an information pane of the current user's maps.
     */
    private static void showCurrentMaps() {
        StringBuilder informationBuilder = new StringBuilder();

        LinkedList<MappedExecutable> exes = UserUtil.getCyderUser().getExecutables();

        for (MappedExecutable exe : exes) {
            informationBuilder.append(ESCAPED_QUOTE)
                    .append(exe.getName())
                    .append(ESCAPED_QUOTE)
                    .append(" maps to: ")
                    .append(ESCAPED_QUOTE)
                    .append(exe.getFilepath())
                    .append(ESCAPED_QUOTE)
                    .append(BoundsUtil.BREAK_TAG);
        }

        String username = UserUtil.getCyderUser().getName();
        String mapsString = informationBuilder.toString();
        editUserFrame.inform(mapsString.isEmpty()
                ? "No maps found for " + UserUtil.getCyderUser().getName()
                : mapsString, username + StringUtil.getApostrophe(username) + " " + MAPS);
    }

    /**
     * Adds a new map to the current users list of maps.
     *
     * @param name the name of the map
     * @param link the link to the file/url
     */
    private static void addMap(String name, String link) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(link);
        Preconditions.checkArgument(!name.isEmpty());
        Preconditions.checkArgument(!link.isEmpty());

        File referenceFile = new File(link);
        boolean isValidFile = referenceFile.isFile();
        boolean isValidLink = NetworkUtil.isValidUrl(link);

        if (!isValidFile && !isValidLink) {
            editUserFrame.notify("Could not locate local file/link is invalid");
            return;
        }

        LinkedList<MappedExecutable> exes = UserUtil.getCyderUser().getExecutables();
        boolean exists = false;

        for (MappedExecutable exe : exes) {
            if (exe.getName().equalsIgnoreCase(name)) {
                exists = true;
                break;
            }
        }

        if (exists) {
            editUserFrame.notify("Map name already exists");
            return;
        }

        MappedExecutable addExe = new MappedExecutable(name, link);
        LinkedList<MappedExecutable> newExes = UserUtil.getCyderUser().getExecutables();
        newExes.add(addExe);
        UserUtil.getCyderUser().setExecutables(newExes);

        editUserFrame.notify("Successfully added map \"" + name + "\" linking to: \"" + link + "\"");
        Console.INSTANCE.revalidateMenu();
    }

    /**
     * Removes the map from the user's list of maps.
     *
     * @param name the name of the map to remove
     */
    private static void removeMap(String name) {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.isEmpty());

        LinkedList<MappedExecutable> exes = UserUtil.getCyderUser().getExecutables();
        boolean found = false;

        for (MappedExecutable exe : exes) {
            if (exe.getName().equalsIgnoreCase(name)) {
                found = true;
                exes.remove(exe);
                break;
            }
        }

        if (!found) {
            editUserFrame.notify("Could not locate map with specified name");
            return;
        }

        UserUtil.getCyderUser().setExecutables(exes);
        editUserFrame.notify("Removed map \"" + name + "\" successfully removed");
        Console.INSTANCE.revalidateMenu();
    }

    /**
     * Returns whether the user editor is open.
     *
     * @return whether the user editor is open
     */
    public static boolean isOpen() {
        return editUserFrame.isVisible();
    }

    /**
     * Returns whether the edit user frame is currently minimized.
     *
     * @return whether the edit user frame is currently minimized
     */
    public static boolean isMinimized() {
        return editUserFrame.getState() == ConsoleConstants.FRAME_ICONIFIED;
    }

    /**
     * Minimizes the edit user frame.
     */
    public static void minimize() {
        editUserFrame.minimizeAndIconify();
    }

    /**
     * Restores the frame to the normal frame position of minimized.
     */
    public static void restore() {
        if (isMinimized()) {
            editUserFrame.setState(ConsoleConstants.FRAME_NORMAL);
        }
    }

    /**
     * Returns the frame associated with the user editor.
     *
     * @return the frame associated with the user editor
     */
    public static CyderFrame getEditUserFrame() {
        return editUserFrame;
    }
}
