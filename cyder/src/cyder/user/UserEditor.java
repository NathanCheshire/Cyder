package cyder.user;

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
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.utils.*;
import cyder.widgets.ColorConverterWidget;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
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
    private static final AtomicReference<JLabel> filesLabelRef = new AtomicReference<>();

    /**
     * The reference used for the cyder scroll list.
     */
    private static final AtomicReference<CyderScrollList> filesScrollListRef = new AtomicReference<>();

    /**
     * The height of the ribbon menu for the edit user frame.
     */
    private static final int MENU_HEIGHT = 30;

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
            - CyderDragLabel.DEFAULT_HEIGHT - MENU_HEIGHT;

    /**
     * The possible pages of the user editor.
     */
    public enum Page {
        FILES("Files", UserEditor::switchToUserFiles),
        FONT_AND_COLOR("Font & Color", UserEditor::switchToFontAndColor),
        PREFERENCES("Preferences", UserEditor::switchToPreferences),
        FIELDS("Fields", UserEditor::switchToFieldInputs);

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

        switchingLabel = new JLabel();
        switchingLabel.setForeground(Color.white);
        switchingLabel.setBounds(CyderFrame.BORDER_LEN, CyderDragLabel.DEFAULT_HEIGHT + MENU_HEIGHT,
                CONTENT_PANE_WIDTH, CONTENT_PANE_HEIGHT);
        switchingLabel.setOpaque(true);
        switchingLabel.setBackground(CyderColors.vanilla);
        editUserFrame.getContentPane().add(switchingLabel);

        installMenu();
        editUserFrame.finalizeAndShow();

        currentPage = page;
        page.getSwitchRunnable().run();
    }

    /**
     * Installs the menu items to the edit user frame.
     */
    private static void installMenu() {
        for (Page page : Page.values()) {
            editUserFrame.addMenuItem(page.getTitle(), () -> {
                if (currentPage != page) {
                    currentPage = page;

                    revalidateOnMenuItemClicked();
                    page.getSwitchRunnable().run();
                }
            });
        }

        editUserFrame.setMenuType(CyderFrame.MenuType.RIBBON);
        editUserFrame.lockMenuOut();
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
     * Refreshes the contents of {@link #filesNameList}.
     * Note this method does not update the ui based on the updated contents.
     */
    private static void refreshFileLists() {
        filesNameList.clear();

        // todo surely an optimization can be made here, enum of user file which needs
        //  to be a dir linked to a function to determine if it should be added
        File backgroundDir = UserUtil.getUserFile(UserFile.BACKGROUNDS.getName());
        File[] backgroundFiles = backgroundDir.listFiles();
        if (backgroundFiles != null && backgroundFiles.length > 0) {
            for (File file : backgroundFiles) {
                if (FileUtil.isSupportedImageExtension(file)) {
                    filesNameList.add(UserFile.BACKGROUNDS.getName() + "/" + file.getName());
                }
            }
        }

        File musicDir = UserUtil.getUserFile(UserFile.MUSIC.getName());
        File[] musicFiles = musicDir.listFiles();
        if (musicFiles != null && musicFiles.length > 0) {
            for (File file : musicFiles) {
                if (FileUtil.isSupportedAudioExtension(file)) {
                    filesNameList.add(UserFile.MUSIC.getName() + "/" + file.getName());
                }
            }
        }

        File filesDir = UserUtil.getUserFile(UserFile.FILES.getName());
        File[] fileFiles = filesDir.listFiles();
        if (fileFiles != null && fileFiles.length > 0) {
            for (File file : fileFiles) {
                filesNameList.add(UserFile.FILES.getName() + "/" + file.getName());
            }
        }
    }

    /**
     * Revalidates the necessary items before switching to a new preferences page.
     */
    private static void revalidateOnMenuItemClicked() {
        switchingLabel.removeAll();
        switchingLabel.revalidate();
        switchingLabel.repaint();
        editUserFrame.revalidate();
        editUserFrame.repaint();
    }

    /**
     * Switches to the user files preference page.
     */
    private static void switchToUserFiles() {
        JLabel titleLabel = new JLabel(Page.FILES.getTitle(), SwingConstants.CENTER);
        titleLabel.setFont(CyderFonts.SEGOE_30);
        titleLabel.setForeground(CyderColors.navy);

        revalidateFilesScroll();

        CyderButton addFileButton = new CyderButton("Add");
        addFileButton.setBorder(new LineBorder(CyderColors.navy, 5, false));
        addFileButton.setFocusPainted(false);
        addFileButton.setBackground(CyderColors.regularRed);
        addFileButton.addActionListener(addFileButtonActionListener);
        addFileButton.setFont(CyderFonts.SEGOE_20);

        CyderButton openFileButton = new CyderButton("Open");
        openFileButton.setBorder(new LineBorder(CyderColors.navy, 5, false));
        openFileButton.setFocusPainted(false);
        openFileButton.setBackground(CyderColors.regularRed);
        openFileButton.setFont(CyderFonts.SEGOE_20);
        openFileButton.addActionListener(openFileButtonActionListener);

        CyderButton renameFileButton = new CyderButton("Rename");
        renameFileButton.setBorder(new LineBorder(CyderColors.navy, 5, false));
        renameFileButton.addActionListener(renameFileButtonActionListener);
        renameFileButton.setFont(CyderFonts.SEGOE_20);

        CyderButton deleteFileButton = new CyderButton("Delete");
        deleteFileButton.setBorder(new LineBorder(CyderColors.navy, 5, false));
        deleteFileButton.addActionListener(deleteFileButtonActionListener);
        deleteFileButton.setBackground(CyderColors.regularRed);
        deleteFileButton.setFont(CyderFonts.SEGOE_20);

        switchingLabel.revalidate();
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
                    File parentFolder = UserUtil.getUserFile(copyLocation.getName());
                    if (parentFolder.exists() && parentFolder.isDirectory()) {
                        uniqueNameAndExtension = FileUtil.findUniqueName(fileToAdd, parentFolder);
                    }

                    try {
                        String copyFolderPath = UserUtil.getUserFile(copyLocation.getName()).getAbsolutePath();
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
    private static final ActionListener openFileButtonActionListener = e -> {
        String selectedScrollElement = filesScrollListRef.get().getSelectedElement();

        for (String fileName : filesNameList) {
            if (selectedScrollElement.equals(fileName)) {
                File file = getFile(selectedScrollElement);

                if (file.exists()) {
                    IOUtil.openFile(file);
                }

                break;
            }
        }
    };

    /**
     * The action listener for the rename file button.
     */
    private static final ActionListener renameFileButtonActionListener = e -> {
        try {
            if (!filesScrollListRef.get().getSelectedElements().isEmpty()) {
                String selectedElement = filesScrollListRef.get().getSelectedElements().get(0);
                File selectedFile = getFile(selectedElement);

                if (selectedFile == null || !selectedFile.exists()) {
                    return;
                }

                String[] parts = selectedElement.split("/");
                String userDirectory = parts[0];
                String filename = parts[1];

                if (isOpenInAudioPlayer(selectedFile)) {
                    editUserFrame.notify("Cannot rename file open in audio player");
                    return;
                } else if (isConsoleBackground(selectedFile)) {
                    editUserFrame.notify("Cannot rename current console background");
                    return;
                }

                String newName = GetterUtil.getInstance().getString(
                        new GetterUtil.Builder("Rename " + filename)
                                .setFieldTooltip("Enter a valid file name (extension will be handled)")
                                .setRelativeTo(editUserFrame)
                                .setSubmitButtonText("Rename")
                                .setInitialString(filename));

                if (StringUtil.isNull(newName)) {
                    return;
                }

                String newFilenameAndExtension = newName + FileUtil.getExtension(selectedFile);

                if (OSUtil.isValidFilename(newFilenameAndExtension)) {
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
            }
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
                Console.INSTANCE.getUuid(), UserFile.MUSIC.getName(),
                "AlbumArt"); // todo album art needs to be extract to user file somehow

        if (albumArtDir.exists()) {
            File[] albumArtFiles = albumArtDir.listFiles();

            if (albumArtFiles != null && albumArtFiles.length > 0) {

            }
        }

        return true;
    }

    /**
     * The action listener for the delete file button.
     */
    private static final ActionListener deleteFileButtonActionListener = e -> {
        if (!filesScrollListRef.get().getSelectedElements().isEmpty()) {
            String selectedElement = filesScrollListRef.get().getSelectedElements().get(0);
            File selectedFile = getFile(selectedElement);

            if (selectedFile == null || !selectedFile.exists()) {
                return;
            }

            String[] parts = selectedElement.split("/");
            String userDirectory = parts[0];
            String filename = parts[1];

            if (isOpenInAudioPlayer(selectedFile)) {
                editUserFrame.notify("Cannot delete audio file open in audio player");
                return;
            } else if (isConsoleBackground(selectedFile)) {
                editUserFrame.notify("Cannot delete current console background");
                return;
            }

            if (OSUtil.deleteFile(selectedFile)) {
                switch (userDirectory) {
                    case "Backgrounds" -> editUserFrame.notify("Deleted background file: " + filename);
                    case "Music" -> editUserFrame.notify("Deleted music file: " + filename);
                    default -> editUserFrame.notify("Deleted file: " + filename);
                }
            } else {
                editUserFrame.notify("Could not delete file at this time");
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
        return AudioPlayer.getCurrentAudio().getAbsolutePath().equals(file.getAbsolutePath());
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

        String[] parts = name.split("/");
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

    // todo redo

    /**
     * Revalidates the user files scroll.
     */
    private static void revalidateFilesScroll() {
        if (filesLabelRef.get() != null) {
            switchingLabel.remove(filesLabelRef.get());
            filesLabelRef.set(null);
        }

        if (filesScrollListRef.get() != null) {
            filesScrollListRef.get().removeAllElements();
            filesScrollListRef.set(null);
        }

        // ensure lists are updated
        refreshFileLists();

        // todo size
        CyderScrollList filesScroll = new CyderScrollList(680, 360, CyderScrollList.SelectionPolicy.SINGLE);
        filesScroll.setBorder(null);
        filesScrollListRef.set(filesScroll);

        for (int i = 0 ; i < filesNameList.size() ; i++) {
            int finalI = i;
            // todo method for runnable
            filesScroll.addElement(filesNameList.get(i),
                    () -> {
                        //                        // if photo viewer can handle
                        //                        if (FileUtil.isSupportedImageExtension(filesList.get(finalI))) {
                        //                            PhotoViewer pv = PhotoViewer.getInstance(filesList.get(finalI));
                        //                            pv.setRenameCallback(UserEditor::revalidateFilesScroll);
                        //                            pv.showGui();
                        //                        } else if (filesList.get(finalI).isDirectory()) {
                        //                            DirectoryViewer.showGui(filesList.get(finalI));
                        //                        } else {
                        //                            IOUtil.openFile(filesList.get(finalI).getAbsolutePath());
                        //                        }
                    });
        }

        JLabel filesLabel;
        filesLabel = filesScroll.generateScrollList();
        filesLabel.setBounds(20, 60, 680, 360);
        filesLabel.setBackground(CyderColors.vanilla);
        filesLabel.setBorder(new CompoundBorder(
                new LineBorder(CyderColors.navy, 3),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        filesLabelRef.set(filesLabel);
        switchingLabel.add(filesLabel);
    }

    /**
     * Switches to the fonts and colors preference page.
     */
    @SuppressWarnings("MagicConstant") /* check font metric */
    private static void switchToFontAndColor() {
        JLabel titleLabel = new JLabel(Page.FONT_AND_COLOR.getTitle(), SwingConstants.CENTER);
        titleLabel.setFont(CyderFonts.SEGOE_30);
        titleLabel.setForeground(CyderColors.navy);

        JLabel colorLabel = new JLabel("Text Color");
        colorLabel.setFont(CyderFonts.SEGOE_30);
        colorLabel.setForeground(CyderColors.navy);

        JLabel hexLabel = new JLabel("HEX:");
        hexLabel.setFont(CyderFonts.SEGOE_20);
        hexLabel.setForeground(CyderColors.navy);
        // todo there should be a static utility method of CyderLabel to generate this common mouse listener
        hexLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ColorConverterWidget.getInstance().innerShowGui();
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

        JTextField foregroundColorBlock = new JTextField();
        foregroundColorBlock.setHorizontalAlignment(JTextField.CENTER);
        foregroundColorBlock.setBackground(CyderColors.navy);
        foregroundColorBlock.setFocusable(false);
        foregroundColorBlock.setCursor(null);
        foregroundColorBlock.setBackground(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getForeground()));
        foregroundColorBlock.setToolTipText("Color Preview");
        foregroundColorBlock.setBorder(new LineBorder(CyderColors.navy, 5, false));

        CyderTextField foregroundField = new CyderTextField(6);
        foregroundField.setHorizontalAlignment(JTextField.CENTER);
        foregroundField.setKeyEventRegexMatcher("[A-Fa-f0-9]{0,6}");
        foregroundField.setText(UserUtil.getCyderUser().getForeground());
        foregroundField.setFont(CyderFonts.SEGOE_30);
        foregroundField.setToolTipText("Console input/output text color");
        foregroundField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    Color c = ColorUtil.hexStringToColor(foregroundField.getText());
                    foregroundColorBlock.setBackground(c);
                    UserUtil.getCyderUser().setForeground(foregroundField.getText());
                    Color updateC = ColorUtil.hexStringToColor(foregroundField.getText());

                    Console.INSTANCE.getOutputArea().setForeground(updateC);
                    Console.INSTANCE.getInputField().setForeground(updateC);
                    Console.INSTANCE.getInputField().setCaretColor(updateC);
                    Console.INSTANCE.getInputField().setCaret(new CyderCaret(updateC));
                } catch (Exception ignored) {}
            }
        });
        foregroundField.setOpaque(false);

        JLabel windowThemeColorLabel = new JLabel("Window Color");
        windowThemeColorLabel.setFont(CyderFonts.SEGOE_30);
        windowThemeColorLabel.setForeground(CyderColors.navy);

        JLabel hexWindowLabel = new JLabel("HEX:");
        hexWindowLabel.setFont(CyderFonts.SEGOE_20);
        hexWindowLabel.setForeground(CyderColors.navy);
        hexWindowLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ColorConverterWidget.getInstance().innerShowGui();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hexWindowLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hexWindowLabel.setForeground(CyderColors.navy);
            }
        });

        JTextField windowColorBlock = new JTextField();
        windowColorBlock.setHorizontalAlignment(JTextField.CENTER);
        windowColorBlock.setBackground(CyderColors.navy);
        windowColorBlock.setFocusable(false);
        windowColorBlock.setCursor(null);
        windowColorBlock.setBackground(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getWindowcolor()));
        windowColorBlock.setToolTipText("Color Preview");
        windowColorBlock.setBorder(new LineBorder(CyderColors.navy, 5, false));

        CyderTextField windowField = new CyderTextField(6);
        windowField.setHorizontalAlignment(JTextField.CENTER);
        // todo this regex is common there should be a set method of the text field to set the regex to this
        windowField.setKeyEventRegexMatcher("[A-Fa-f0-9]{0,6}");
        windowField.setText(UserUtil.getCyderUser().getWindowcolor());
        windowField.setFont(CyderFonts.SEGOE_30);
        windowField.setToolTipText("Window border color");
        // todo extract
        windowField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    Color c = ColorUtil.hexStringToColor(windowField.getText());
                    windowColorBlock.setBackground(c);
                    UserUtil.getCyderUser().setWindowcolor(windowField.getText());

                    CyderColors.setGuiThemeColor(c);

                    Preference.invokeRefresh(Preference.WINDOW_COLOR);
                } catch (Exception ignored) {}
            }
        });
        windowField.setOpaque(false);

        JLabel FillLabel = new JLabel("Fill Color");
        FillLabel.setFont(CyderFonts.SEGOE_30);
        FillLabel.setForeground(CyderColors.navy);

        JLabel hexLabelFill = new JLabel("Hex: ");
        hexLabelFill.setFont(CyderFonts.SEGOE_20);
        hexLabelFill.setForeground(CyderColors.navy);
        hexLabelFill.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ColorConverterWidget.getInstance().innerShowGui();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hexLabelFill.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hexLabelFill.setForeground(CyderColors.navy);
            }
        });

        JTextField fillColorBlock = new JTextField();
        fillColorBlock.setHorizontalAlignment(JTextField.CENTER);
        fillColorBlock.setBackground(CyderColors.navy);
        fillColorBlock.setFocusable(false);
        fillColorBlock.setCursor(null);
        fillColorBlock.setBackground(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground()));
        fillColorBlock.setToolTipText("Color Preview");
        fillColorBlock.setBorder(new LineBorder(CyderColors.navy, 5, false));

        CyderTextField fillField = new CyderTextField(6);
        fillField.setHorizontalAlignment(JTextField.CENTER);
        fillField.setKeyEventRegexMatcher("[A-Fa-f0-9]{0,6}");
        fillField.setText(UserUtil.getCyderUser().getBackground());
        fillField.setFont(CyderFonts.SEGOE_30);
        fillField.setToolTipText("Input field and output area fill color if enabled");
        fillField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    fillColorBlock.setBackground(ColorUtil.hexStringToColor(fillField.getText()));
                    UserUtil.getCyderUser().setBackground(fillField.getText());

                    if (UserUtil.getCyderUser().getOutputfill().equals("1")) {
                        Console.INSTANCE.getOutputArea().setOpaque(true);
                        Console.INSTANCE.getOutputArea()
                                .setBackground(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground()));
                        Console.INSTANCE.getOutputArea().repaint();
                        Console.INSTANCE.getOutputArea().revalidate();
                    }
                    if (UserUtil.getCyderUser().getInputfill().equals("1")) {
                        Console.INSTANCE.getInputField().setOpaque(true);
                        Console.INSTANCE.getInputField()
                                .setBackground(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground()));
                        Console.INSTANCE.getInputField().repaint();
                        Console.INSTANCE.getInputField().revalidate();
                    }
                    if (UserUtil.getCyderUser().getOutputborder().equals("1")) {
                        Console.INSTANCE.getOutputScroll()
                                .setBorder(UserUtil.getCyderUser().getOutputborder().equals("1")
                                        ? new LineBorder(
                                        ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground()),
                                        3, false)
                                        : BorderFactory.createEmptyBorder());
                        Console.INSTANCE.getOutputScroll().repaint();
                        Console.INSTANCE.getOutputScroll().revalidate();
                    }
                    if (UserUtil.getCyderUser().getInputborder().equals("1")) {
                        Console.INSTANCE.getInputField()
                                .setBorder(UserUtil.getCyderUser().getInputborder().equals("1")
                                        ? new LineBorder(
                                        ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground()),
                                        3, false)
                                        : BorderFactory.createEmptyBorder());
                        Console.INSTANCE.getInputField().repaint();
                        Console.INSTANCE.getInputField().revalidate();
                    }
                } catch (Exception ignored) {}
            }
        });
        fillField.setOpaque(false);

        JLabel FontLabel = new JLabel("FONTS", SwingConstants.CENTER);
        FontLabel.setFont(new Font(UserUtil.getCyderUser().getFont(), Font.BOLD, 30));
        FontLabel.setForeground(CyderColors.navy);

        // todo size
        AtomicReference<CyderScrollList> fontScrollRef = new AtomicReference<>(
                new CyderScrollList(300, 300, CyderScrollList.SelectionPolicy.SINGLE));
        fontScrollRef.get().setItemAlignment(StyleConstants.ALIGN_LEFT);

        // todo text string in CyderStrings that others use too
        CyderLabel tempLabel = new CyderLabel("Loading...");
        tempLabel.setFont(CyderFonts.DEFAULT_FONT);
        tempLabel.setBackground(CyderColors.vanilla);
        tempLabel.setBorder(new LineBorder(CyderColors.navy, 5));
        tempLabel.setOpaque(true);

        // todo load method
        CyderThreadRunner.submit(() -> {
            LinkedList<String> fontList = new LinkedList<>();
            Collections.addAll(fontList, GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames());

            int metric = Integer.parseInt(PropLoader.getString("font_metric"));
            int size = Integer.parseInt(UserUtil.getCyderUser().getFontsize());

            if (NumberUtil.isValidFontMetric(metric)) {
                for (String fontName : fontList) {
                    Font font = new Font(fontName, metric, size);

                    fontScrollRef.get().addElementWithSingleCLickAction(fontName, () -> FontLabel.setFont(font));
                }
            }

            if (currentPage == Page.FONT_AND_COLOR) {
                JLabel fontLabel = fontScrollRef.get().generateScrollList();
                fontLabel.setBounds(50, 100, 300, 300);
                switchingLabel.remove(tempLabel);

                if (currentPage == Page.FONT_AND_COLOR) {
                    switchingLabel.add(fontLabel);
                }
            }
        }, "Preferences Frame Font Loader");

        CyderButton applyFontButton = new CyderButton("Apply Font");
        applyFontButton.setToolTipText("Apply"); // todo update this tooltip with the currently selected font name?
        applyFontButton.setFont(CyderFonts.SEGOE_20);
        applyFontButton.setFocusPainted(false);
        applyFontButton.setBackground(CyderColors.regularRed);
        applyFontButton.addActionListener(e -> {
            if (fontScrollRef.get() == null || fontScrollRef.get().getSelectedElements().isEmpty())
                return;

            String selectedFont = fontScrollRef.get().getSelectedElements().get(0);

            if (selectedFont != null) {
                UserUtil.getCyderUser().setFont(selectedFont);
                Font applyFont = new Font(selectedFont,
                        Integer.parseInt(PropLoader.getString("font_metric")),
                        Integer.parseInt(UserUtil.getCyderUser().getFontsize()));
                Console.INSTANCE.getOutputArea().setFont(applyFont);
                Console.INSTANCE.getInputField().setFont(applyFont);

                editUserFrame.notify("Applied font: " + selectedFont);
            }
        });

        CyderButton resetValues = new CyderButton("Reset all");
        resetValues.setToolTipText("Reset font and colors");
        resetValues.setFont(CyderFonts.SEGOE_20);
        resetValues.setFocusPainted(false);
        resetValues.setBackground(CyderColors.regularRed);
        resetValues.addActionListener(e -> {
            // todo we can probably get away without building a default user
            User defaultUser = UserUtil.buildDefaultUser();

            // Foreground here
            UserUtil.getCyderUser().setForeground(defaultUser.getForeground());
            foregroundColorBlock.setBackground(ColorUtil.hexStringToColor(defaultUser.getForeground()));
            foregroundField.setText(defaultUser.getForeground());

            // Apply foreground to console fields and carets
            Console.INSTANCE.getOutputArea().setForeground(
                    ColorUtil.hexStringToColor(defaultUser.getForeground()));
            Console.INSTANCE.getInputField().setForeground(
                    ColorUtil.hexStringToColor(defaultUser.getForeground()));
            Console.INSTANCE.getInputField().setCaretColor(
                    ColorUtil.hexStringToColor(defaultUser.getForeground()));
            Console.INSTANCE.getInputField().setCaret(
                    new CyderCaret(ColorUtil.hexStringToColor(defaultUser.getForeground())));

            // Font preference
            UserUtil.getCyderUser().setForeground(defaultUser.getForeground());
            Font applyFont = new Font(defaultUser.getFont(), Font.BOLD, 30);

            // Font console
            Console.INSTANCE.getOutputArea().setFont(applyFont);
            Console.INSTANCE.getInputField().setFont(applyFont);

            // Font here
            if (fontScrollRef.get() != null) {
                fontScrollRef.get().clearSelectedElements();
            }
            FontLabel.setFont(applyFont);

            // Background color preference and here
            UserUtil.getCyderUser().setBackground(defaultUser.getBackground());
            fillColorBlock.setBackground(ColorUtil.hexStringToColor(defaultUser.getBackground()));
            fillField.setText(defaultUser.getBackground());

            // Background if enabled
            if (UserUtil.getCyderUser().getOutputfill().equals("1")) {
                Console.INSTANCE.getOutputArea().setOpaque(true);
                Console.INSTANCE.getOutputArea()
                        .setBackground(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground()));
                Console.INSTANCE.getOutputArea().repaint();
                Console.INSTANCE.getOutputArea().revalidate();
            }
            if (UserUtil.getCyderUser().getInputfill().equals("1")) {
                Console.INSTANCE.getInputField().setOpaque(true);
                Console.INSTANCE.getInputField()
                        .setBackground(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground()));
                Console.INSTANCE.getInputField().repaint();
                Console.INSTANCE.getInputField().revalidate();
            }

            // Window color preference and here
            UserUtil.getCyderUser().setWindowcolor(defaultUser.getWindowcolor());
            windowColorBlock.setBackground(ColorUtil.hexStringToColor(defaultUser.getWindowcolor()));
            windowField.setText(defaultUser.getWindowcolor());
            windowColorBlock.setBackground((ColorUtil.hexStringToColor(defaultUser.getWindowcolor())));

            // Window color elsewhere
            CyderColors.setGuiThemeColor((ColorUtil.hexStringToColor(defaultUser.getWindowcolor())));
            Preference.invokeRefresh(Preference.WINDOW_COLOR);

            if (fontScrollRef.get() != null) {
                JScrollBar scrollBar = fontScrollRef.get().getScrollPane().getVerticalScrollBar();
                scrollBar.setValue(scrollBar.getMinimum());
            }

            switchingLabel.revalidate();
            editUserFrame.notify("Default fonts and colors set");
        });

        switchingLabel.revalidate();
    }

    // todo add things to label use magic text and print that label
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
