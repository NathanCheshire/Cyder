package cyder.user;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import cyder.annotations.Widget;
import cyder.audio.AudioPlayer;
import cyder.console.Console;
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
import cyder.ui.*;
import cyder.utils.*;
import cyder.widgets.ColorConverterWidget;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
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
    private static final int filesLabelPadding = 10;

    /**
     * The partition height for the files scroll on the files page.
     */
    private static final int FILE_SCROLL_PARTITION = 85;

    /**
     * The partition height for the files scroll buttons for the files page.
     */
    private static final int FILE_BUTTON_PARTITION = 100 - FILE_SCROLL_PARTITION;

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
    private static CyderFrame editUserFrame;

    /**
     * The names of the files for the files list.
     */
    private static final ArrayList<String> filesNameList = new ArrayList<>();

    /**
     * The label on which components are added for a specific preference page.
     */
    private static JLabel switchingLabel;

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
     * Suppress default constructor.
     */
    private UserEditor() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Widget(triggers = {"prefs", "edituser"}, description = "A widget to edit your user preferences and files")
    public static void showGui() {
        showGui(Page.FILES);
    }

    // todo console location saving doesn't work and sometimes
    //  messes up still, only save is not being disposed too

    // todo update design doc for startup

    /**
     * Shows the user editor with the provided page.
     *
     * @param page the page to show on the user editor
     */
    public static void showGui(Page page) {
        closeIfOpen();

        editUserFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT, CyderColors.vanilla);
        editUserFrame.setBackground(CyderColors.vanilla);
        editUserFrame.setTitle("Preferences");

        installDragLabelButtons();
        editUserFrame.finalizeAndShow();

        currentPage = page;
        page.getSwitchRunnable().run();
    }

    /**
     * Installs the drag label items to the edit user frame.
     */
    private static void installDragLabelButtons() {
        for (Page page : Page.values()) {
            JLabel textButton = CyderDragLabel.generateTextButton(page.getTitle(), page.getTitle(), () -> {
                if (currentPage != page) {
                    currentPage = page;

                    page.getSwitchRunnable().run();
                }
            });
            editUserFrame.getTopDragLabel().addRightButton(textButton, 0);
        }
    }

    /**
     * Closes the frame if open.
     */
    private static void closeIfOpen() {
        if (editUserFrame != null) {
            editUserFrame.dispose(true);
        }
    }

    /**
     * The separator between user folders and user files.
     */
    private static final String FILES_SCROLL_SEPARATOR = "/";

    /**
     * UserFile folders to show in the files scroll list.
     */
    @SuppressWarnings("Guava") /* Java being dumb */
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
     * Disables the files scroll buttons.
     */
    private static void disableFilesScrollButtons() {
        Preconditions.checkNotNull(addFileButton).setEnabled(false);
        openFileButton.setEnabled(false);
        renameFileButton.setEnabled(false);
        deleteFileButton.setEnabled(false);
    }

    /**
     * Enables the files scroll buttons.
     */
    private static void enableFilesScrollButtons() {
        Preconditions.checkNotNull(addFileButton).setEnabled(true);
        openFileButton.setEnabled(true);
        renameFileButton.setEnabled(true);
        deleteFileButton.setEnabled(true);
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

                    if (fileToAdd == null || StringUtil.isNull(fileToAdd.getName())) {
                        return;
                    }

                    UserFile copyLocation = FileUtil.isSupportedImageExtension(fileToAdd)
                            ? UserFile.BACKGROUNDS
                            : FileUtil.isSupportedAudioExtension(fileToAdd)
                            ? UserFile.MUSIC
                            : UserFile.FILES;

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
                            Console.INSTANCE.resizeBackgrounds();
                        }
                    } catch (Exception exception) {
                        editUserFrame.notify("Could not add file at this time");
                        ExceptionHandler.handle(exception);
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
     * The action listener for the rename file button.
     */
    private static final ActionListener renameFileButtonActionListener = e -> {
        try {
            LinkedList<String> selectedElements = filesScrollListReference.get().getSelectedElements();

            if (selectedElements.isEmpty()) {
                return;
            }

            if (selectedElements.size() > 1) {
                editUserFrame.notify("Sorry, but you can only rename one file at a time");
                return;
            }

            String selectedElement = selectedElements.get(0);
            File selectedFile = getFile(selectedElement);

            if (selectedFile == null || !selectedFile.exists()) {
                return;
            }

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

                if (StringUtil.isNull(newName)) {
                    return;
                }

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
            }, "User Editor File Renamer");
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
            } else {
                editUserFrame.notify("Could not delete " + filename + " at this time");
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
        return Console.INSTANCE.getCurrentBackground().getReferenceFile()
                .getAbsolutePath().equals(file.getAbsolutePath());
    }

    /**
     * Returns the user file represented by the provided name from the files list.
     * For example: backgrounds/Img.png would open Img.png if that file was present
     * in the current user's Backgrounds/ folder
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
     * Switches to the user files preference page, wiping past components
     * and regenerating the files scroll label in the process.
     */
    private static void switchToUserFiles() {
        CyderPanel buttonPanel = new CyderPanel(buttonGridLayout);
        buttonPanel.setSize(CONTENT_PANE_WIDTH, FILE_BUTTON_PARTITION * CONTENT_PANE_HEIGHT);

        filesPartitionedLayout.clearComponents();

        JLabel filesLabelRef = filesLabelReference.get();
        filesPartitionedLayout.addComponent(filesLabelRef == null
                ? loadingLabel
                : filesLabelRef, FILE_SCROLL_PARTITION);
        filesPartitionedLayout.addComponent(buttonPanel, FILE_BUTTON_PARTITION);

        revalidateFilesScroll();

        editUserFrame.setCyderLayout(filesPartitionedLayout);
    }

    /**
     * The inner border of the files label border.
     */
    private static final LineBorder LINE_BORDER = new LineBorder(CyderColors.navy, 3);

    /**
     * The outer border (padding) of the files label border.
     */
    private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(
            filesLabelPadding, filesLabelPadding, filesLabelPadding, filesLabelPadding);

    /**
     * The border for the files label (the generated files scroll).
     */
    private static final CompoundBorder filesLabelBorder = new CompoundBorder(LINE_BORDER, EMPTY_BORDER);

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
        filesPartitionedLayout.setComponent(loadingLabel, 0);

        CyderScrollList filesScrollList = filesScrollListReference.get();
        filesScrollList.removeAllElements();

        refreshFileLists();

        for (String element : filesNameList) {
            filesScrollList.addElement(element, () -> IOUtil.openFile(getFile(element)));
        }

        JLabel filesLabel = filesScrollList.generateScrollList();
        filesLabelReference.set(filesLabel);
        filesLabel.setSize(FILES_SCROLL_WIDTH, FILES_SCROLL_HEIGHT);
        filesLabel.setBackground(CyderColors.vanilla);
        filesLabel.setBorder(filesLabelBorder);

        filesPartitionedLayout.setComponent(filesLabel, 0);
    }

    /**
     * The width of the font scroll list.
     */
    private static final int FONT_SCROLL_WIDTH = CONTENT_PANE_WIDTH / 2 - 2 * 50;

    /**
     * The height of the font scroll list.
     */
    private static final int FONT_SCROLL_HEIGHT = CONTENT_PANE_HEIGHT - 2 * 100;

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

    private static final int fontButtonWidth = 130;
    private static final int fontButtonHeight = 40;

    private static final int paddingPartition = 5;
    private static final int surroundingPanelPartition = 5;
    private static final int fontLabelPartition = 80;

    private static final int fontLabelSize = 40;

    private static CyderLabel fontLabel;
    private static CyderButton applyFontButton;
    private static CyderPartitionedLayout fontPartitionedLayout;

    private static CyderTextField foregroundField;
    private static JTextField foregroundColorBlock;

    private static CyderTextField windowField;
    private static JTextField windowColorBlock;

    private static CyderTextField backgroundField;
    private static JTextField backgroundColorBlock;

    /**
     * Switches to the fonts and colors page.
     */
    private static void switchToFontAndColor() {
        CyderPartitionedLayout fontAndColorPartitionedLayout = new CyderPartitionedLayout();
        fontAndColorPartitionedLayout.setPartitionDirection(CyderPartitionedLayout.PartitionDirection.ROW);

        fontLabel = new CyderLabel("Fonts");
        fontLabel.setFont(new Font(UserUtil.getCyderUser().getFont(), Font.BOLD, fontLabelSize));
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
        fontAndColorPartitionedLayout.addComponent(fontPanel, 50);

        CyderLabel foregroundColorLabel = new CyderLabel("Foreground Color");
        foregroundColorLabel.setFont(CyderFonts.DEFAULT_FONT);
        foregroundColorLabel.setSize(300, 40);

        foregroundField = new CyderTextField(6);
        foregroundField.setHorizontalAlignment(JTextField.CENTER);
        foregroundField.setHexColorRegexMatcher();
        foregroundField.setText(UserUtil.getCyderUser().getForeground());
        foregroundField.setFont(CyderFonts.SEGOE_30);
        foregroundField.setToolTipText("Console input/output text color");
        foregroundField.addKeyListener(FrameUtil.generateKeyAdapter(false, false, true, () -> {
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
        windowField.addKeyListener(FrameUtil.generateKeyAdapter(false, false, true, () -> {
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
        backgroundField.addKeyListener(FrameUtil.generateKeyAdapter(false, false, true, () -> {
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
                if (outputBorder) {
                    Console.INSTANCE.getOutputScroll().setBorder(new LineBorder(backgroundColor, 3, false));
                    Console.INSTANCE.getOutputScroll().repaint();
                    Console.INSTANCE.getOutputScroll().revalidate();
                }
                if (inputBorder) {
                    Console.INSTANCE.getInputField().setBorder(new LineBorder(backgroundColor, 3, false));
                    Console.INSTANCE.getInputField().repaint();
                    Console.INSTANCE.getInputField().revalidate();
                }
            } catch (Exception ignored) {}
        }));
        backgroundField.setOpaque(false);
        backgroundField.setSize(160, 40);

        backgroundColorBlock = generateColorBlock(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground()),
                "Background color preview");

        CyderGridLayout colorGridLayout = new CyderGridLayout(1, 3);

        CyderPartitionedLayout foregroundPartitionedLayout = new CyderPartitionedLayout();
        foregroundPartitionedLayout.spacer(25);
        foregroundPartitionedLayout.addComponent(foregroundColorLabel, 25);
        CyderPartitionedLayout foregroundFieldPartitionedLayout = new CyderPartitionedLayout();
        foregroundFieldPartitionedLayout.setPartitionDirection(CyderPartitionedLayout.PartitionDirection.ROW);
        foregroundFieldPartitionedLayout.addComponent(generateHexInformationLabel(), 15);
        foregroundFieldPartitionedLayout.addComponent(foregroundField, 70);
        foregroundFieldPartitionedLayout.addComponent(foregroundColorBlock, 15);
        CyderPanel foregroundFieldPanel = new CyderPanel(foregroundFieldPartitionedLayout);
        foregroundFieldPanel.setSize(250, 40);
        foregroundPartitionedLayout.addComponent(foregroundFieldPanel, 25);
        foregroundPartitionedLayout.spacer(25);
        colorGridLayout.addComponent(new CyderPanel(foregroundPartitionedLayout));

        CyderPartitionedLayout windowPartitionedLayout = new CyderPartitionedLayout();
        windowPartitionedLayout.spacer(25);
        windowPartitionedLayout.addComponent(windowColorLabel, 25);
        CyderPartitionedLayout windowFieldPartitionedLayout = new CyderPartitionedLayout();
        windowFieldPartitionedLayout.setPartitionDirection(CyderPartitionedLayout.PartitionDirection.ROW);
        windowFieldPartitionedLayout.addComponent(generateHexInformationLabel(), 15);
        windowFieldPartitionedLayout.addComponent(windowField, 70);
        windowFieldPartitionedLayout.addComponent(windowColorBlock, 15);
        CyderPanel windowFieldPanel = new CyderPanel(windowFieldPartitionedLayout);
        windowFieldPanel.setSize(250, 40);
        windowPartitionedLayout.addComponent(windowFieldPanel, 25);
        windowPartitionedLayout.spacer(25);
        colorGridLayout.addComponent(new CyderPanel(windowPartitionedLayout));

        CyderPartitionedLayout backgroundPartitionedLayout = new CyderPartitionedLayout();
        backgroundPartitionedLayout.spacer(25);
        backgroundPartitionedLayout.addComponent(backgroundColorLabel, 25);
        CyderPartitionedLayout backgroundFieldPartitionedLayout = new CyderPartitionedLayout();
        backgroundFieldPartitionedLayout.setPartitionDirection(CyderPartitionedLayout.PartitionDirection.ROW);
        backgroundFieldPartitionedLayout.addComponent(generateHexInformationLabel(), 15);
        backgroundFieldPartitionedLayout.addComponent(backgroundField, 70);
        backgroundFieldPartitionedLayout.addComponent(backgroundColorBlock, 15);
        CyderPanel backgroundFieldPanel = new CyderPanel(backgroundFieldPartitionedLayout);
        backgroundFieldPanel.setSize(250, 40);
        backgroundPartitionedLayout.addComponent(backgroundFieldPanel, 25);
        backgroundPartitionedLayout.spacer(25);
        colorGridLayout.addComponent(new CyderPanel(backgroundPartitionedLayout));

        CyderPanel colorPanel = new CyderPanel(colorGridLayout);
        colorPanel.setSize(CONTENT_PANE_WIDTH / 2, CONTENT_PANE_HEIGHT);

        fontAndColorPartitionedLayout.addComponent(colorPanel, 50);

        editUserFrame.setCyderLayout(fontAndColorPartitionedLayout);
    }

    /**
     * The length of generated color blocks.
     */
    private static final int COLOR_BLOCK_LEN = 40;

    /**
     * Returns a color block of size 40,40 to use a color preview.
     *
     * @param backgroundColor the initial color of the preview block.
     * @param tooltip         the tooltip for the color block.
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
        ret.setSize(COLOR_BLOCK_LEN, COLOR_BLOCK_LEN);
        ret.setToolTipText(tooltip);
        ret.setBorder(new LineBorder(CyderColors.navy, 5, false));

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
                            fontLabel.setFont(new Font(fontName, Font.BOLD, fontLabelSize));
                        });
            }

            if (currentPage == Page.FONT_AND_COLOR) {
                JLabel fontLabel = fontScrollReference.get().generateScrollList();
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
        int defaultFontMetric = Integer.parseInt(Preference.get(Preference.FONT_METRIC).getDefaultValue().toString());
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
        if (UserUtil.getCyderUser().getOutputfill().equals("1")) {
            Console.INSTANCE.getOutputArea().setOpaque(true);
            Console.INSTANCE.getOutputArea().setBackground(defaultBackgroundColor);
            Console.INSTANCE.getOutputArea().repaint();
            Console.INSTANCE.getOutputArea().revalidate();
        }
        if (UserUtil.getCyderUser().getInputfill().equals("1")) {
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

    private static final int HEX_LABEL_WIDTH = 80;
    private static final int HEX_LABEL_HEIGHT = 40;

    /**
     * Returns a {@link CyderLabel} to indicate a text field accepts a hex value.
     *
     * @return a {@link CyderLabel} to indicate a text field accepts a hex value
     */
    private static CyderLabel generateHexInformationLabel() {
        CyderLabel hexLabel = new CyderLabel("Hex");
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

    // todo add things to label use magic text and print that label so that stuff is aligned
    /**
     * The width of the preferences scroll and pane for the preferences page.
     */
    private static final int PREF_WIDTH = 400;

    /**
     * The size of the preference checkboxes.
     */
    private static final int checkboxSize = 60;

    /**
     * The text used for preference labels.
     */
    private static final String PRINT_LABEL_CHECKBOX_MAGIC_TEXT = StringUtil.generateTextForCustomComponent(4);

    /**
     * Switches to the preferences preference page.
     */
    private static void switchToPreferences() {
        JTextPane preferencePane = new JTextPane();
        preferencePane.setEditable(false);
        preferencePane.setAutoscrolls(false);
        preferencePane.setBounds(switchingLabel.getWidth() / 2 - PREF_WIDTH / 2,
                10, PREF_WIDTH, switchingLabel.getHeight() - 20);
        preferencePane.setFocusable(true);
        preferencePane.setOpaque(false);
        preferencePane.setBackground(Color.white);

        // adding components
        StringUtil printingUtil = new StringUtil(new CyderOutputPane(preferencePane));

        // print title
        CyderLabel prefsTitle = new CyderLabel("Preferences");
        prefsTitle.setFont(CyderFonts.SEGOE_30);
        printingUtil.printlnComponent(prefsTitle);

        // print boolean userdata, i.e. (preferences)
        for (int i = 0 ; i < Preference.getPreferences().size() ; i++) {
            if (Preference.getPreferences().get(i).getDisplayName().equals("IGNORE"))
                continue;

            // label for information
            CyderLabel preferenceLabel = new CyderLabel(Preference.getPreferences().get(i).getDisplayName());
            preferenceLabel.setForeground(CyderColors.navy);
            preferenceLabel.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));
            preferenceLabel.setFont(CyderFonts.DEFAULT_FONT_SMALL);
            printingUtil.printComponent(preferenceLabel);
            printingUtil.print(StringUtil.generateNSpaces(20));

            Preference localPref = Preference.getPreferences().get(i);
            boolean selected = UserUtil.getUserDataById(localPref.getID()).equalsIgnoreCase("1");

            CyderCheckbox checkbox = new CyderCheckbox(selected);
            checkbox.setToolTipText(Preference.getPreferences().get(i).getTooltip());
            checkbox.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    UserUtil.setUserDataById(localPref.getID(), checkbox.isChecked() ? "1" : "0");

                    Preference.invokeRefresh(localPref.getID());
                }
            });

            checkbox.setSize(checkboxSize, checkboxSize);
            JLabel printLabel = new JLabel(PRINT_LABEL_CHECKBOX_MAGIC_TEXT);
            printLabel.setSize(checkboxSize, checkboxSize);
            checkbox.setBounds(0, 0, checkboxSize, checkboxSize);
            printLabel.add(checkbox);

            printingUtil.printlnComponent(printLabel);
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
        preferenceScroll.setBounds(switchingLabel.getWidth() / 2 - PREF_WIDTH / 2, 10,
                PREF_WIDTH, switchingLabel.getHeight() - 20);

        //set menu location to top
        preferencePane.setCaretPosition(0);

        switchingLabel.add(preferenceScroll);
        switchingLabel.revalidate();
    }

    /**
     * Switches to the field input preference page.
     */
    private static void switchToFieldInputs() {
        JTextPane fieldInputsPane = new JTextPane();
        fieldInputsPane.setEditable(false);
        fieldInputsPane.setAutoscrolls(false);
        fieldInputsPane.setBounds(0, 0, 720, 500);
        fieldInputsPane.setFocusable(true);
        fieldInputsPane.setOpaque(false);
        fieldInputsPane.setBackground(Color.white);

        //adding components
        StringUtil printingUtil = new StringUtil(new CyderOutputPane(fieldInputsPane));

        //print pairs here
        CyderLabel prefsTitle = new CyderLabel("Field Inputs");
        prefsTitle.setFont(CyderFonts.SEGOE_30);
        printingUtil.printlnComponent(prefsTitle);

        printingUtil.print("\n\n");

        CyderLabel changeUsernameLabel = new CyderLabel("Change username");
        printingUtil.printlnComponent(changeUsernameLabel);

        printingUtil.print("\n");

        String space = StringUtil.generateNSpaces(3);
        CyderButton changeUsernameButton = new CyderButton(space + "Change Username" + space);
        JTextField changeUsernameField = new JTextField(0);
        changeUsernameField.setHorizontalAlignment(JTextField.CENTER);
        changeUsernameField.addActionListener(e -> changeUsername(changeUsernameField));
        changeUsernameField.setToolTipText("Change account username to a valid alternative");
        changeUsernameField.setBackground(CyderColors.vanilla);
        changeUsernameField.setSelectionColor(CyderColors.selectionColor);
        changeUsernameField.setFont(new Font("Agency FB", Font.BOLD, 26));
        changeUsernameField.setForeground(CyderColors.navy);
        changeUsernameField.setCaretColor(CyderColors.navy);
        changeUsernameField.setCaret(new CyderCaret(CyderColors.navy));
        changeUsernameField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        changeUsernameField.setOpaque(true);
        CyderTextField.addAutoCapitalizationAdapter(changeUsernameField);
        printingUtil.printlnComponent(changeUsernameField);
        changeUsernameField.setText(UserUtil.getCyderUser().getName());

        printingUtil.print("\n");

        changeUsernameButton.setBackground(CyderColors.regularRed);
        changeUsernameButton.setBorder(new LineBorder(CyderColors.navy, 5, false));
        changeUsernameButton.setFont(CyderFonts.SEGOE_20);
        changeUsernameButton.addActionListener(e -> changeUsername(changeUsernameField));
        printingUtil.printlnComponent(changeUsernameButton);

        printingUtil.print("\n\n");

        CyderLabel changePasswordLabel = new CyderLabel("Change password");
        printingUtil.printlnComponent(changePasswordLabel);

        printingUtil.print("\n");

        //needed for focus traversal
        CyderPasswordField changePasswordConfField = new CyderPasswordField();

        CyderPasswordField changePasswordField = new CyderPasswordField();
        changePasswordField.setFont(changeUsernameField.getFont());
        changePasswordField.addActionListener(e -> changePasswordConfField.requestFocus());
        changePasswordField.setToolTipText("New Password");
        printingUtil.printlnComponent(changePasswordField);

        printingUtil.print("\n");

        // init button here to add listener to field
        CyderButton changePassword = new CyderButton("    Change Password    ");

        changePasswordConfField.addActionListener(e -> changePassword(changePasswordField, changePasswordConfField));
        changePasswordConfField.setFont(changePasswordField.getFont());
        changePasswordConfField.setToolTipText("New Password Confirmation");
        printingUtil.printlnComponent(changePasswordConfField);

        printingUtil.print("\n");

        changePassword.addActionListener(e -> changePassword(changePasswordField, changePasswordConfField));
        printingUtil.printlnComponent(changePassword);

        printingUtil.print("\n\n");

        CyderLabel consoleDatePatternLabel = new CyderLabel("Console Clock Date Pattern");
        consoleDatePatternLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkUtil.openUrl(CyderUrls.SIMPLE_DATE_PATTERN_GUIDE);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                consoleDatePatternLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                consoleDatePatternLabel.setForeground(CyderColors.navy);
            }
        });
        printingUtil.printlnComponent(consoleDatePatternLabel);

        printingUtil.print("\n");

        CyderButton validateDatePatternButton = new CyderButton(space + "Validate" + space);
        JTextField consoleDatePatternField = new JTextField(0);
        consoleDatePatternField.setHorizontalAlignment(JTextField.CENTER);
        consoleDatePatternField.addActionListener(e -> validateDatePattern(consoleDatePatternField));
        consoleDatePatternLabel.setToolTipText("Java date/time pattern to use for the console clock");
        consoleDatePatternField.setBackground(CyderColors.vanilla);
        consoleDatePatternField.setSelectionColor(CyderColors.selectionColor);
        consoleDatePatternField.setFont(CyderFonts.SEGOE_20);
        consoleDatePatternField.setForeground(CyderColors.navy);
        consoleDatePatternField.setCaretColor(CyderColors.navy);
        consoleDatePatternField.setCaret(new CyderCaret(CyderColors.navy));
        consoleDatePatternField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        consoleDatePatternField.setOpaque(true);
        printingUtil.printlnComponent(consoleDatePatternField);
        consoleDatePatternField.setText(UserUtil.getCyderUser().getConsoleclockformat());

        printingUtil.print("\n");

        validateDatePatternButton.addActionListener(e -> validateDatePattern(consoleDatePatternField));
        printingUtil.printlnComponent(validateDatePatternButton);

        printingUtil.print("\n\n");

        CyderLabel addMap = new CyderLabel("Add Maps");
        printingUtil.printlnComponent(addMap);

        printingUtil.print("\n");

        CyderButton addMapButton = new CyderButton(space + "Add Map" + space);
        JTextField addMapField = new JTextField(0);
        addMapField.setHorizontalAlignment(JTextField.CENTER);
        addMapField.addActionListener(e -> addMap(addMapField));
        addMapField.setToolTipText("Add format: map_name, PATH/TO/EXE or FILE or URL");
        addMapField.setBackground(CyderColors.vanilla);
        addMapField.setSelectionColor(CyderColors.selectionColor);
        addMapField.setFont(CyderFonts.SEGOE_20);
        addMapField.setForeground(CyderColors.navy);
        addMapField.setCaretColor(CyderColors.navy);
        addMapField.setCaret(new CyderCaret(CyderColors.navy));
        addMapField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        addMapField.setOpaque(true);
        printingUtil.printlnComponent(addMapField);

        printingUtil.print("\n");

        addMapButton.addActionListener(e -> addMap(addMapField));
        printingUtil.printlnComponent(addMapButton);

        printingUtil.print("\n\n");

        CyderLabel removeMap = new CyderLabel("Remove Map");
        printingUtil.printlnComponent(removeMap);

        printingUtil.print("\n");

        CyderButton removeMapButton = new CyderButton(space + "Remove Map" + space);
        JTextField removeMapField = new JTextField(0);
        removeMapField.setHorizontalAlignment(JTextField.CENTER);
        removeMapField.addActionListener(e -> removeMap(removeMapField));
        removeMapField.setToolTipText("Name of map to remove");
        removeMapField.setBackground(CyderColors.vanilla);
        removeMapField.setSelectionColor(CyderColors.selectionColor);
        removeMapField.setFont(CyderFonts.SEGOE_20);
        removeMapField.setForeground(CyderColors.navy);
        removeMapField.setCaretColor(CyderColors.navy);
        removeMapField.setCaret(new CyderCaret(CyderColors.navy));
        removeMapField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        removeMapField.setOpaque(true);
        printingUtil.printlnComponent(removeMapField);

        printingUtil.print("\n");

        removeMapButton.addActionListener(e -> removeMap(removeMapField));
        printingUtil.printlnComponent(removeMapButton);

        printingUtil.print("\n\n");

        CyderLabel deleteUserLabel = new CyderLabel("Delete User");
        printingUtil.printlnComponent(deleteUserLabel);

        CyderButton deleteUserButton = new CyderButton(space + "Delete user" + space);
        CyderPasswordField deletePasswordField = new CyderPasswordField();
        deletePasswordField.setToolTipText("Enter password to confirm account deletion");
        deletePasswordField.addActionListener(e -> deleteUser(deletePasswordField));
        printingUtil.printlnComponent(deletePasswordField);

        printingUtil.print("\n");

        deleteUserButton.addActionListener(e -> deleteUser(deletePasswordField));
        printingUtil.printlnComponent(deleteUserButton);

        printingUtil.print("\n\n");

        //more labels, fields, and if applicable, validation buttons here
        //format: \n\n to separate sections, \n to separate components within a section

        CyderScrollPane fieldInputsScroll = new CyderScrollPane(fieldInputsPane);
        fieldInputsScroll.setThumbSize(7);
        fieldInputsScroll.getViewport().setOpaque(false);
        fieldInputsScroll.setFocusable(true);
        fieldInputsScroll.setOpaque(false);
        fieldInputsScroll.setThumbColor(CyderColors.regularPink);
        fieldInputsScroll.setBackground(Color.white);
        fieldInputsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        fieldInputsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        fieldInputsScroll.setBounds(6, 5, 708, 490);

        //after everything is on pane, use this to center it
        StyledDocument doc = fieldInputsPane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        //set menu location to top
        fieldInputsPane.setCaretPosition(0);

        switchingLabel.add(fieldInputsScroll);
        switchingLabel.revalidate();
    }

    /**
     * Validates the weather key from the propkeys.ini file.
     *
     * @return whether the weather key was valid
     */
    private static boolean validateWeatherKey() {
        String openString = CyderUrls.OPEN_WEATHER_BASE
                + PropLoader.getString("default_weather_location")
                + "&appid=" + PropLoader.getString("weather_key")
                + "&units=imperial";

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(openString).openStream()))) {
            reader.readLine();
            return true;
        } catch (Exception ex) {
            ExceptionHandler.silentHandle(ex);
        }

        return false;
    }

    /**
     * Validates the ip key from the propkeys.ini file.
     *
     * @return whether the ip key was valid
     */
    private static boolean validateIpKey() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new URL(CyderUrls.IPDATA_BASE
                            + PropLoader.getString("ip_key")).openStream()));
            reader.close();
            return true;
        } catch (Exception ex) {
            ExceptionHandler.silentHandle(ex);
        }

        return false;
    }

    /**
     * Validates the youtube key from the propkeys.ini file.
     *
     * @return whether the youtube key was valid
     */
    private static boolean validateYoutubeApiKey() {
        String key = PropLoader.getString("youtube_api_3_key");

        if (!key.isEmpty()) {
            try {
                NetworkUtil.readUrl(CyderUrls.YOUTUBE_API_V3_SEARCH
                        + "?part=snippet&q=" + "gift+and+a+curse+skizzy+mars"
                        + "&type=video&key=" + key);
                return true;
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }

        return false;
    }

    private static final String confirmationString = "Final warning: you are about to"
            + " delete your Cyder account. All files, pictures, downloaded music, notes,"
            + " etc. will be deleted. Are you ABSOLUTELY sure you wish to continue?";

    private static void deleteUser(CyderPasswordField deletePasswordField) {
        String hashed = SecurityUtil.toHexString(SecurityUtil.getSha256(deletePasswordField.getPassword()));

        if (!SecurityUtil.toHexString(SecurityUtil.getSha256(hashed.toCharArray()))
                .equals(UserUtil.getCyderUser().getPass())) {
            editUserFrame.notify("Invalid password; user not deleted");
            deletePasswordField.setText("");
        } else {
            CyderThreadRunner.submit(() -> {
                boolean delete = GetterUtil.getInstance().getConfirmation(
                        new GetterUtil.Builder(confirmationString)
                                .setRelativeTo(editUserFrame));

                if (delete) {
                    Console.INSTANCE.closeFrame(false, true);

                    // close all frames, console is already closed
                    FrameUtil.closeAllFrames(true);

                    // attempt to delete directory
                    OSUtil.deleteFile(OSUtil.buildFile(Dynamic.PATH,
                            Dynamic.USERS.getDirectoryName(), Console.INSTANCE.getUuid()));

                    // exit with proper condition
                    OSUtil.exit(ExitCondition.UserDeleted);
                } else {
                    deletePasswordField.setText("");
                    editUserFrame.notify("Account not deleted");
                }
            }, "Account deletion confirmation");
        }
    }

    private static void changePassword(CyderPasswordField changePasswordField,
                                       CyderPasswordField changePasswordConfField) {
        char[] newPassword = changePasswordField.getPassword();
        char[] newPasswordConf = changePasswordConfField.getPassword();

        boolean alphabet = false;
        boolean number = false;

        // todo need a method to validate a username/password extract from UserCreator I assume and ad to userUtil
        for (char c : newPassword) {
            if (Character.isDigit(c))
                number = true;
            else if (Character.isAlphabetic(c))
                alphabet = true;

            if (number && alphabet)
                break;
        }

        if (newPassword.length < 5 || !number || !alphabet) {
            editUserFrame.notify("Sorry, " + UserUtil.getCyderUser().getName()
                    + ", " + "but your password must contain at least"
                    + " one number, one letter, and be 5 characters long");
        } else {
            if (!Arrays.equals(newPasswordConf, newPassword)) {
                editUserFrame.notify("Sorry, " + UserUtil.getCyderUser().getName() + ", " +
                        "but your provided passwords were not equal");
                changePasswordField.setText("");
                changePasswordConfField.setText("");
            } else {
                UserUtil.getCyderUser().setPass(SecurityUtil.toHexString(SecurityUtil.getSha256(
                        SecurityUtil.toHexString(SecurityUtil.getSha256(newPassword)).toCharArray())));
                editUserFrame.notify("Password successfully changed");
            }
        }

        changePasswordField.setText("");
        changePasswordConfField.setText("");

        Arrays.fill(newPasswordConf, '\0');
        Arrays.fill(newPassword, '\0');
    }

    // todo should just be simple as changing property, maybe need some callback architecture as well
    private static void changeUsername(JTextField changeUsernameField) {
        String newUsername = changeUsernameField.getText();
        if (!StringUtil.isNull(newUsername) && !newUsername.equalsIgnoreCase(UserUtil.getCyderUser().getName())) {
            UserUtil.getCyderUser().setName(newUsername);
            editUserFrame.notify("Username successfully changed to \"" + newUsername + "\"");
            Console.INSTANCE.getConsoleCyderFrame()
                    .setTitle(PropLoader.getString("version") + " Cyder [" + newUsername + "]");
            changeUsernameField.setText(UserUtil.getCyderUser().getName());
        }
    }

    private static void validateDatePattern(JTextField consoleDatePatternField) {
        String fieldText = StringUtil.getTrimmedText(consoleDatePatternField.getText());

        try {
            new SimpleDateFormat(fieldText).format(new Date());
            UserUtil.getCyderUser().setConsoleclockformat(fieldText);
            Console.INSTANCE.refreshClockText();
            consoleDatePatternField.setText(fieldText);
        } catch (Exception ex) {
            ExceptionHandler.silentHandle(ex);
        }
    }

    private static void addMap(JTextField addMapField) {
        if (!addMapField.getText().trim().isEmpty()) {
            if (!addMapField.getText().trim().contains(",")) {
                editUserFrame.notify("Invalid map format");
            } else {
                String[] parts = addMapField.getText().trim().split(",");

                if (parts.length != 2) {
                    editUserFrame.notify("Too many arguments");
                } else {
                    String name = parts[0].trim();
                    String path = parts[1].trim();

                    File pointerFile = new File(path);
                    boolean validLink;

                    try {
                        URL url = new URL(path);
                        URLConnection conn = url.openConnection();
                        conn.connect();
                        validLink = true;
                    } catch (Exception ex) {
                        validLink = false;
                    }

                    if ((!pointerFile.exists() || !pointerFile.isFile()) && !validLink && !pointerFile.isDirectory()) {
                        editUserFrame.notify("File does not exist or link is invalid");
                    } else {
                        if (!name.isEmpty()) {
                            LinkedList<MappedExecutable> exes = UserUtil.getCyderUser().getExecutables();
                            boolean exists = false;

                            for (MappedExecutable exe : exes) {
                                if (exe.getName().equalsIgnoreCase(name)) {
                                    exists = true;
                                    break;
                                }
                            }

                            if (exists) {
                                editUserFrame.notify("Mapped exe name already in use");
                            } else {
                                MappedExecutable addExe = new MappedExecutable(name, path);
                                LinkedList<MappedExecutable> newExes = UserUtil.getCyderUser().getExecutables();
                                newExes.add(addExe);
                                UserUtil.getCyderUser().setExecutables(newExes);

                                editUserFrame.notify("Mapped exe successfully added");
                                Console.INSTANCE.revalidateMenu();
                            }
                        } else {
                            editUserFrame.notify("Invalid map name");
                        }
                    }
                }

                addMapField.setText("");
            }
        }
    }

    /**
     * Removes the map from the user's list of maps.
     *
     * @param removeMapField the map to remove
     */
    private static void removeMap(JTextField removeMapField) {
        String text = removeMapField.getText().trim();

        if (!text.isEmpty()) {
            LinkedList<MappedExecutable> exes = UserUtil.getCyderUser().getExecutables();
            boolean found = false;

            for (MappedExecutable exe : exes) {
                if (exe.getName().equalsIgnoreCase(text)) {
                    found = true;
                    exes.remove(exe);
                    break;
                }
            }

            if (found) {
                UserUtil.getCyderUser().setExecutables(exes);
                editUserFrame.notify("Map successfully removed");
                Console.INSTANCE.revalidateMenu();
            } else {
                editUserFrame.notify("Could not locate specified map");
            }

            removeMapField.setText("");
        }
    }
}
