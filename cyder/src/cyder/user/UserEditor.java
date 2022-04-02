package cyder.user;

import cyder.annotations.Widget;
import cyder.constants.*;
import cyder.enums.ExitCondition;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.CyderShare;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.external.AudioPlayer;
import cyder.handlers.external.PhotoViewer;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.ui.objects.CyderBackground;
import cyder.user.objects.MappedExecutable;
import cyder.utilities.*;
import cyder.utilities.objects.GetterBuilder;
import cyder.widgets.ColorConverterWidget;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An editor for user preferences, files, colors, fonts, and more.
 */
public class UserEditor {
    /**
     * The user editor frame.
     */
    private static CyderFrame editUserFrame;

    /**
     * The names of the files for the files list.
     */
    private static List<String> filesNameList;

    /**
     * The user files list.
     */
    private static List<File> filesList;

    /**
     * The label on which components are added for a specific preference page.
     */
    private static JLabel switchingLabel;

    /**
     * The index the user editor is at.
     */
    private static int prefsPanelIndex;

    /**
     * Suppress default constructor.
     */
    private UserEditor() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Widget(triggers = {"prefs", "edituser"}, description = "A widget to edit your user preferences and files")
    public static void showGUI() {
        showGUI(0);
    }

    public static void showGUI(int startingIndex) {
        if (editUserFrame != null)
            editUserFrame.dispose();

        editUserFrame = new CyderFrame(720 + 2 * 5,
                500 + 5 + CyderDragLabel.DEFAULT_HEIGHT + 25, CyderIcons.defaultBackground);
        editUserFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);
        editUserFrame.setTitle("Preferences");

        switchingLabel = new JLabel();
        switchingLabel.setForeground(new Color(255, 255, 255));
        switchingLabel.setBounds(5, CyderDragLabel.DEFAULT_HEIGHT + 30, 720, 500);
        switchingLabel.setOpaque(true);
        switchingLabel.setBackground(new Color(255, 255, 255));
        editUserFrame.getContentPane().add(switchingLabel);

        prefsPanelIndex = startingIndex;

        editUserFrame.addMenuItem("Files", () -> {
            revalidateOnMenuItemClicked();
            switchToUserFiles();
            prefsPanelIndex = 0;
        });
        editUserFrame.addMenuItem("Fonts", () -> {
            if (prefsPanelIndex == 1)
                return;

            revalidateOnMenuItemClicked();
            switchToFontAndColor();
            prefsPanelIndex = 1;
        });
        editUserFrame.addMenuItem("Colors", () -> {
            if (prefsPanelIndex == 1)
                return;

            revalidateOnMenuItemClicked();
            switchToFontAndColor();
            prefsPanelIndex = 1;
        });
        editUserFrame.addMenuItem("Preferences", () -> {
            revalidateOnMenuItemClicked();
            switchToPreferences();
            prefsPanelIndex = 2;
        });
        editUserFrame.addMenuItem("Fields", () -> {
            revalidateOnMenuItemClicked();
            switchToFieldInputs();
            prefsPanelIndex = 3;
        });

        editUserFrame.setCurrentMenuType(CyderFrame.MenuType.RIBBON);
        editUserFrame.lockMenuOut();

        revalidateOnMenuItemClicked();
        switchToUserFiles();

        editUserFrame.finalizeAndShow();
    }

    /**
     * Initializes the user files list and corresponding name list.
     */
    private static void initFilesList() {
        File backgroundDir = UserUtil.getUserFile(UserFile.BACKGROUNDS.getName());
        File musicDir = UserUtil.getUserFile(UserFile.MUSIC.getName());
        File filesDir = UserUtil.getUserFile(UserFile.FILES.getName());

        filesList = new LinkedList<>();
        filesNameList = new LinkedList<>();

        for (File file : backgroundDir.listFiles()) {
            if (FileUtil.isSupportedImageExtension(file)) {
                filesList.add(file.getAbsoluteFile());
                filesNameList.add(OSUtil.buildPath(
                        UserFile.BACKGROUNDS.getName(), FileUtil.getFilename(file)));
            }
        }

        for (File file : musicDir.listFiles()) {
            if (file.getName().endsWith((".mp3"))) {
                filesList.add(file.getAbsoluteFile());
                filesNameList.add(OSUtil.buildPath(
                        UserFile.MUSIC.getName(), FileUtil.getFilename(file)));
            }
        }

        for (File file : filesDir.listFiles()) {
            filesList.add(file.getAbsoluteFile());
            filesNameList.add(OSUtil.buildPath(
                    UserFile.FILES.getName(), FileUtil.getFilename(file)));
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
        JLabel titleLabel = new JLabel("Files", SwingConstants.CENTER);
        titleLabel.setFont(CyderFonts.segoe30);
        titleLabel.setBounds(720 / 2 - 375 / 2, 10, 375, 40);
        switchingLabel.add(titleLabel);

        initFilesList();

        CyderScrollList filesScroll = new CyderScrollList(680, 360, CyderScrollList.SelectionPolicy.SINGLE);
        filesScroll.setBorder(null);

        // forward reference
        AtomicReference<JLabel> filesLabelRef = new AtomicReference<>();
        JLabel filesLabel = null;
        filesLabelRef.set(filesLabel);

        for (int i = 0; i < filesNameList.size() ; i++) {
            int finalI = i;
            filesScroll.addElement(filesNameList.get(i),
            () -> {
                // if photo viewer can handle
                if (FileUtil.isSupportedImageExtension(filesList.get(finalI))) {
                    PhotoViewer pv = PhotoViewer.getInstance(filesList.get(finalI));
                    pv.setRenameCallback(() -> revalidateFilesScroll(filesScroll, filesLabelRef.get()));
                    pv.showGUI();
                } else {
                    IOUtil.openFile(filesList.get(finalI).getAbsolutePath());
                }
            });
        }

        filesLabel = filesScroll.generateScrollList();
        filesLabelRef.set(filesLabel);
        filesLabel.setBounds(20, 60, 680, 360);
        editUserFrame.getContentPane().add(filesLabel);
        switchingLabel.add(filesLabel);

        CyderButton addFile1 = new CyderButton("Add");
        addFile1.setBorder(new LineBorder(CyderColors.navy, 5, false));
        addFile1.setFocusPainted(false);
        addFile1.setBackground(CyderColors.regularRed);
        addFile1.addActionListener(e -> {
            try {
                CyderThreadRunner.submit(() -> {
                    try {
                        GetterBuilder builder = new GetterBuilder("Add File");
                        builder.setRelativeTo(editUserFrame);
                        File addFile = GetterUtil.getInstance().getFile(builder);

                        if (addFile == null || addFile.getName().equals("NULL"))
                            return;

                        for (CyderBackground background : ConsoleFrame.INSTANCE.getBackgrounds()) {
                            if (addFile.getName().equals(background.getReferenceFile().getName())) {
                                editUserFrame.notify("Cannot add a background with the same name as a current one");
                                return;
                            }
                        }

                        Path copyPath = new File(addFile.getAbsolutePath()).toPath();
                        String folderName;

                        if (FileUtil.isSupportedImageExtension(addFile)) {
                            folderName = UserFile.BACKGROUNDS.getName();
                        } else if (addFile.getName().endsWith(".mp3")) {
                            folderName = UserFile.MUSIC.getName();
                        } else {
                            folderName = UserFile.FILES.getName();
                        }

                        File destination = new File(UserUtil.getUserFile(
                                folderName).getAbsolutePath() + OSUtil.FILE_SEP + addFile.getName());
                        Files.copy(copyPath, destination.toPath());

                        revalidateFilesScroll(filesScroll, filesLabelRef.get());

                        if (folderName.equalsIgnoreCase(UserFile.BACKGROUNDS.getName()))
                            ConsoleFrame.INSTANCE.resizeBackgrounds();

                    } catch (Exception ex) {
                        ExceptionHandler.handle(ex);
                    }
                }, "wait thread for GetterUtil().getFile()");
            } catch (Exception exc) {
                ExceptionHandler.handle(exc);
            }
        });
        addFile1.setFont(CyderFonts.segoe20);
        addFile1.setBounds(20, 440, 155, 40);
        switchingLabel.add(addFile1);

        CyderButton openFile = new CyderButton("Open");
        openFile.setBorder(new LineBorder(CyderColors.navy, 5, false));
        openFile.setFocusPainted(false);
        openFile.setBackground(CyderColors.regularRed);
        openFile.setFont(CyderFonts.segoe20);
        openFile.addActionListener(e -> {
            String element = filesScroll.getSelectedElement();

            for (int i = 0; i < filesNameList.size() ; i++) {
                if (element.equalsIgnoreCase(filesNameList.get(i))) {
                    IOUtil.openFile(filesList.get(i).getAbsolutePath());
                    break;
                }
            }
        });
        openFile.setBounds(20 + 155 + 20, 440, 155, 40);
        switchingLabel.add(openFile);

        CyderButton renameFile = new CyderButton("Rename");
        renameFile.setBorder(new LineBorder(CyderColors.navy, 5, false));
        renameFile.addActionListener(e -> CyderThreadRunner.submit(() -> {
            try {
                if (!filesScroll.getSelectedElements().isEmpty()) {
                    String clickedSelection = filesScroll.getSelectedElements().get(0);
                    File selectedFile = null;

                    for (int i = 0; i < filesNameList.size(); i++) {
                        if (clickedSelection.equals(filesNameList.get(i))) {
                            selectedFile = filesList.get(i);
                            break;
                        }
                    }

                    if ((AudioPlayer.getCurrentAudio() != null
                            && selectedFile.getAbsoluteFile().toString().equals(
                               AudioPlayer.getCurrentAudio().getAbsoluteFile().toString()))
                            || selectedFile.getAbsoluteFile().toString().equals(
                               ConsoleFrame.INSTANCE.getCurrentBackground().getReferenceFile()
                                       .getAbsoluteFile().toString())) {
                        editUserFrame.notify("Cannot rename a file that is in use");
                    } else {
                        String oldName = FileUtil.getFilename(selectedFile);
                        String extension = FileUtil.getExtension(selectedFile);

                        GetterBuilder builder = new GetterBuilder("Rename");
                        builder.setFieldTooltip("Enter any valid file name");
                        builder.setRelativeTo(editUserFrame);
                        builder.setSubmitButtonText("Submit");
                        builder.setInitialString(oldName);
                        String newName = GetterUtil.getInstance().getString(builder);

                        if (oldName.equals(newName) || StringUtil.isNull(newName)) {
                            return;
                        }

                        File renameTo = new File(selectedFile.getParent() + "/" + newName + extension);

                        if (renameTo.exists()) {
                            throw new IOException("file exists");
                        }

                        //rename file to new name
                        boolean success = selectedFile.renameTo(renameTo);

                        if (!success) {
                            editUserFrame.notify("Could not rename file at this time");
                        } else {
                            editUserFrame.notify(selectedFile.getName() +
                                    " was successfully renamed to " + renameTo.getName());

                            //was it a music file?
                            if (extension.equals(".mp3")) {
                                File albumArtDir = new File("dynamic/users/"
                                        + ConsoleFrame.INSTANCE.getUUID() + "/Music/AlbumArt");

                                if (albumArtDir.exists()) {
                                    //try to find a file with the same name as oldName
                                    File refFile = null;

                                    for (File f : albumArtDir.listFiles()) {
                                        if (FileUtil.getFilename(f).equals(oldName)) {
                                            refFile = f;
                                            break;
                                        }
                                    }

                                    //found corresponding album art so rename it as well
                                    if (refFile != null) {
                                        File artRename = new File("dynamic/users/"
                                                + ConsoleFrame.INSTANCE.getUUID()
                                                + "/Music/AlbumArt/" + newName + ".png");

                                        if (artRename.exists())
                                            throw new IOException("album art file exists");

                                        //rename file to new name
                                        boolean albumRenSuccess = refFile.renameTo(artRename);

                                        if (!albumRenSuccess)
                                            throw new RuntimeException("Could not rename music's corresponding album art");
                                    }
                                }
                            }
                        }

                        revalidateFilesScroll(filesScroll, filesLabelRef.get());
                    }
                }
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }, "Wait thread for getterUtil"));

        renameFile.setBackground(CyderColors.regularRed);
        renameFile.setFont(CyderFonts.segoe20);
        renameFile.setBounds(20 + 155 + 20 + 155 + 20, 440, 155, 40);
        switchingLabel.add(renameFile);

        CyderButton deleteFile = new CyderButton("Delete");
        deleteFile.setBorder(new LineBorder(CyderColors.navy, 5, false));
        deleteFile.addActionListener(e -> {
            if (!filesScroll.getSelectedElements().isEmpty()) {
                String clickedSelection = filesScroll.getSelectedElements().get(0);
                File selectedFile = null;

                for (int i = 0; i < filesNameList.size(); i++) {
                    if (clickedSelection.equals(filesNameList.get(i))) {
                        selectedFile = filesList.get(i);
                        break;
                    }
                }

                if (selectedFile.getAbsolutePath().equalsIgnoreCase(ConsoleFrame.INSTANCE
                        .getCurrentBackground().getReferenceFile().getAbsolutePath())) {
                    editUserFrame.notify("Unable to delete the background you are currently using");
                } else if (AudioPlayer.getCurrentAudio() != null &&
                        selectedFile.getAbsolutePath().equalsIgnoreCase(AudioPlayer.getCurrentAudio().getAbsolutePath())) {
                    editUserFrame.notify("Unable to delete the audio you are currently playing");
                } else {
                    boolean deleted = selectedFile.delete();

                    if (deleted) {
                        revalidateFilesScroll(filesScroll, filesLabelRef.get());

                        if (FileUtil.getExtension(selectedFile).equals(".mp3"))
                            ConsoleFrame.INSTANCE.getInputHandler()
                                    .println("Music: " + FileUtil.getFilename(selectedFile) + " successfully deleted.");
                        else if (FileUtil.isSupportedImageExtension(selectedFile)) {
                            ConsoleFrame.INSTANCE.getInputHandler()
                                    .println("Background: " + FileUtil.getFilename(selectedFile) + " successfully deleted.");
                        } else {
                            ConsoleFrame.INSTANCE.getInputHandler()
                                    .println("File: " + FileUtil.getFilename(selectedFile) + " successfully deleted.");
                        }

                        if (FileUtil.getExtension(selectedFile.getName()).equals(".mp3")) {
                            //attempt to find album art
                            String name = FileUtil.getFilename(selectedFile.getName());

                            //find corresponding album art and delete
                            for (File f : new File("dynamic/users/"
                                    + ConsoleFrame.INSTANCE.getUUID() + "/Music/AlbumArt").listFiles()) {
                                if (FileUtil.getFilename(f).equals(name)) {
                                    f.delete();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });

        deleteFile.setBackground(CyderColors.regularRed);
        deleteFile.setFont(CyderFonts.segoe20);
        deleteFile.setBounds(20 + 155 + 20 + 155 + 20 + 155 + 20, 440, 155, 40);
        switchingLabel.add(deleteFile);

        switchingLabel.revalidate();
    }

    /**
     * Revalidates the user files scroll.
     *
     * @param filesScroll the scroll to revalidate
     * @param filesLabel the label which the scroll generates
     */
    private static void revalidateFilesScroll(CyderScrollList filesScroll, JLabel filesLabel) {
        initFilesList();

        filesScroll.removeAllElements();
        switchingLabel.remove(filesLabel);

        for (int j = 0; j < filesNameList.size() ; j++) {
            int finalJ = j;
            filesScroll.addElement(filesNameList.get(j),
                    () -> IOUtil.openFile(filesList.get(finalJ).getAbsolutePath()));
        }

        filesLabel = filesScroll.generateScrollList();
        filesLabel.setBounds(20, 60, 680, 360);
        switchingLabel.add(filesLabel);

        switchingLabel.revalidate();
        ConsoleFrame.INSTANCE.loadBackgrounds();
    }

    /**
     * Switches to the fonts and colors preference page.
     */
    private static void switchToFontAndColor() {
        JLabel TitleLabel = new JLabel("Colors & Font", SwingConstants.CENTER);
        TitleLabel.setFont(CyderFonts.segoe30);
        TitleLabel.setBounds(720 / 2 - 375 / 2, 10, 375, 40);
        switchingLabel.add(TitleLabel);

        int colorOffsetX = 340;
        int colorOffsetY = 10;

        JLabel ColorLabel = new JLabel("Text Color");
        ColorLabel.setFont(CyderFonts.segoe30);
        ColorLabel.setForeground(CyderColors.navy);
        ColorLabel.setBounds(120 + colorOffsetX, 50 + colorOffsetY, 300, 30);
        switchingLabel.add(ColorLabel);

        JLabel hexLabel = new JLabel("HEX:");
        hexLabel.setFont(CyderFonts.segoe20);
        hexLabel.setForeground(CyderColors.navy);
        hexLabel.setBounds(30 + colorOffsetX, 110 + colorOffsetY, 70, 30);
        switchingLabel.add(hexLabel);
        hexLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ColorConverterWidget.getInstance().innerShowGUI();
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
        foregroundColorBlock.setBackground(ColorUtil.hexToRgb(UserUtil.getCyderUser().getForeground()));
        foregroundColorBlock.setToolTipText("Color Preview");
        foregroundColorBlock.setBorder(new LineBorder(CyderColors.navy, 5, false));
        foregroundColorBlock.setBounds(330 + colorOffsetX, 100 + colorOffsetY, 40, 50);
        switchingLabel.add(foregroundColorBlock);

        CyderTextField foregroundField = new CyderTextField(6);
        foregroundField.setHorizontalAlignment(JTextField.CENTER);
        foregroundField.setRegexMatcher("[A-Fa-f0-9]{0,6}");
        foregroundField.setText(UserUtil.getCyderUser().getForeground());
        foregroundField.setFont(CyderFonts.segoe30);
        foregroundField.setToolTipText("Console input/output text color");
        foregroundField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    Color c = ColorUtil.hexToRgb(foregroundField.getText());
                    foregroundColorBlock.setBackground(c);
                    UserUtil.getCyderUser().setForeground(foregroundField.getText());
                    Color updateC = ColorUtil.hexToRgb(foregroundField.getText());

                    ConsoleFrame.INSTANCE.getOutputArea().setForeground(updateC);
                    ConsoleFrame.INSTANCE.getInputField().setForeground(updateC);
                    ConsoleFrame.INSTANCE.getInputField().setCaretColor(updateC);
                    ConsoleFrame.INSTANCE.getInputField().setCaret(new CyderCaret(updateC));
                } catch (Exception ignored) {}
            }
        });
        foregroundField.setBounds(100 + colorOffsetX, 100 + colorOffsetY, 220, 50);
        foregroundField.setOpaque(false);
        switchingLabel.add(foregroundField);

        JLabel windowThemeColorLabel = new JLabel("Window Color");
        windowThemeColorLabel.setFont(CyderFonts.segoe30);
        windowThemeColorLabel.setForeground(CyderColors.navy);
        windowThemeColorLabel.setBounds(105 + colorOffsetX, 200, 300, 30);
        switchingLabel.add(windowThemeColorLabel);

        JLabel hexWindowLabel = new JLabel("HEX:");
        hexWindowLabel.setFont(CyderFonts.segoe20);
        hexWindowLabel.setForeground(CyderColors.navy);
        hexWindowLabel.setBounds(30 + colorOffsetX, 255, 70, 30);
        switchingLabel.add(hexWindowLabel);
        hexWindowLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ColorConverterWidget.getInstance().innerShowGUI();
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
        windowColorBlock.setBackground(ColorUtil.hexToRgb(UserUtil.getCyderUser().getWindowcolor()));
        windowColorBlock.setToolTipText("Color Preview");
        windowColorBlock.setBorder(new LineBorder(CyderColors.navy, 5, false));
        windowColorBlock.setBounds(330 + colorOffsetX, 240 + colorOffsetY, 40, 50);
        switchingLabel.add(windowColorBlock);

        CyderTextField windowField = new CyderTextField(6);
        windowField.setHorizontalAlignment(JTextField.CENTER);
        windowField.setRegexMatcher("[A-Fa-f0-9]{0,6}");
        windowField.setText(UserUtil.getCyderUser().getWindowcolor());
        windowField.setFont(CyderFonts.segoe30);
        windowField.setToolTipText("Window border color");
        windowField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    Color c = ColorUtil.hexToRgb(windowField.getText());
                    windowColorBlock.setBackground(c);
                    UserUtil.getCyderUser().setWindowcolor(windowField.getText());

                    CyderColors.setGuiThemeColor(c);

                    Preferences.invokeRefresh("windowcolor");
                } catch (Exception ignored) {}
            }
        });
        windowField.setBounds(100 + colorOffsetX, 240 + colorOffsetY, 220, 50);
        windowField.setOpaque(false);
        switchingLabel.add(windowField);

        JLabel FillLabel = new JLabel("Fill Color");
        FillLabel.setFont(CyderFonts.segoe30);
        FillLabel.setForeground(CyderColors.navy);
        FillLabel.setBounds(120 + colorOffsetX, 330 + colorOffsetY, 300, 30);
        switchingLabel.add(FillLabel);

        JLabel hexLabelFill = new JLabel("HEX:");
        hexLabelFill.setFont(CyderFonts.segoe20);
        hexLabelFill.setForeground(CyderColors.navy);
        hexLabelFill.setBounds(30 + colorOffsetX, 390 + colorOffsetY, 70, 30);
        switchingLabel.add(hexLabelFill);
        hexLabelFill.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ColorConverterWidget.getInstance().innerShowGUI();
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
        fillColorBlock.setBackground(ColorUtil.hexToRgb(UserUtil.getCyderUser().getBackground()));
        fillColorBlock.setToolTipText("Color Preview");
        fillColorBlock.setBorder(new LineBorder(CyderColors.navy, 5, false));
        fillColorBlock.setBounds(330 + colorOffsetX, 380 + colorOffsetY, 40, 50);
        switchingLabel.add(fillColorBlock);

        CyderTextField fillField = new CyderTextField(6);
        fillField.setHorizontalAlignment(JTextField.CENTER);
        fillField.setRegexMatcher("[A-Fa-f0-9]{0,6}");
        fillField.setText(UserUtil.getCyderUser().getBackground());
        fillField.setFont(CyderFonts.segoe30);
        fillField.setToolTipText("Input field and output area fill color if enabled");
        fillField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    fillColorBlock.setBackground(ColorUtil.hexToRgb(fillField.getText()));
                    UserUtil.getCyderUser().setBackground(fillField.getText());

                    if (UserUtil.getCyderUser().getOutputfill().equals("1")) {
                        ConsoleFrame.INSTANCE.getOutputArea().setOpaque(true);
                        ConsoleFrame.INSTANCE.getOutputArea()
                                .setBackground(ColorUtil.hexToRgb(UserUtil.getCyderUser().getBackground()));
                        ConsoleFrame.INSTANCE.getOutputArea().repaint();
                        ConsoleFrame.INSTANCE.getOutputArea().revalidate();
                    }

                    //input color fill
                    if (UserUtil.getCyderUser().getInputfill().equals("1")) {
                        ConsoleFrame.INSTANCE.getInputField().setOpaque(true);
                        ConsoleFrame.INSTANCE.getInputField()
                                .setBackground(ColorUtil.hexToRgb(UserUtil.getCyderUser().getBackground()));
                        ConsoleFrame.INSTANCE.getInputField().repaint();
                        ConsoleFrame.INSTANCE.getInputField().revalidate();
                    }
                } catch (Exception ignored) {}
            }
        });
        fillField.setBounds(100 + colorOffsetX, 380 + colorOffsetY, 220, 50);
        fillField.setOpaque(false);
        switchingLabel.add(fillField);

        JLabel FontLabel = new JLabel("FONTS", SwingConstants.CENTER);
        FontLabel.setFont(new Font(UserUtil.getCyderUser().getFont(),Font.BOLD, 30));
        FontLabel.setForeground(CyderColors.navy);
        FontLabel.setBounds(50, 60, 300, 30);
        switchingLabel.add(FontLabel);

        AtomicReference<CyderScrollList> fontScrollRef = new AtomicReference<>(
                new CyderScrollList(300, 300, CyderScrollList.SelectionPolicy.SINGLE));
        fontScrollRef.get().setItemAlignment(StyleConstants.ALIGN_LEFT);

        // label to show where fonts will be
        CyderLabel tempLabel = new CyderLabel("Loading...");
        tempLabel.setFont(CyderFonts.defaultFont);
        tempLabel.setBackground(CyderColors.vanila);
        tempLabel.setBorder(new LineBorder(CyderColors.navy, 5));
        tempLabel.setOpaque(true);
        tempLabel.setBounds(50,100,300, 300);
        switchingLabel.add(tempLabel);

        CyderThreadRunner.submit(() -> {
            LinkedList<String> fontList = new LinkedList<>();
            Collections.addAll(fontList, GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames());

            for (int i = 0 ; i < fontList.size() ; i++) {
                int finalI = i;
                fontScrollRef.get().addElementWithSingleCLickAction(fontList.get(i),
                        () -> FontLabel.setFont(new Font(fontList.get(finalI),
                        Integer.parseInt(UserUtil.getCyderUser().getFontmetric()),
                        Integer.parseInt(UserUtil.getCyderUser().getFontsize()))));
            }

            if (prefsPanelIndex == 1) {
                JLabel fontLabel = fontScrollRef.get().generateScrollList();
                fontLabel.setBounds(50, 100, 300, 300);
                switchingLabel.remove(tempLabel);

                if (prefsPanelIndex == 1) {
                    switchingLabel.add(fontLabel);
                }
            }
        },"Preference Font Loader");

        CyderButton applyFont = new CyderButton("Apply Font");
        applyFont.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        applyFont.setToolTipText("Apply");
        applyFont.setFont(CyderFonts.segoe20);
        applyFont.setFocusPainted(false);
        applyFont.setBackground(CyderColors.regularRed);
        applyFont.addActionListener(e -> {
            if (fontScrollRef.get() == null || fontScrollRef.get().getSelectedElements().isEmpty())
                return;

            String selectedFont = fontScrollRef.get().getSelectedElements().get(0);

            if (selectedFont != null) {
                UserUtil.getCyderUser().setFont(selectedFont);
                Font ApplyFont = new Font(selectedFont,
                        Integer.parseInt(UserUtil.getCyderUser().getFontmetric()),
                        Integer.parseInt(UserUtil.getCyderUser().getFontsize()));
                ConsoleFrame.INSTANCE.getOutputArea().setFont(ApplyFont);
                ConsoleFrame.INSTANCE.getInputField().setFont(ApplyFont);
                ConsoleFrame.INSTANCE.getInputHandler().println("The font \"" + selectedFont + "\" has been applied.");
            }
        });
        applyFont.setBounds(50, 410, 140, 40);
        switchingLabel.add(applyFont);

        CyderButton resetValues = new CyderButton("Reset ALL");
        resetValues.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        resetValues.setToolTipText("Reset font and all colors");
        resetValues.setFont(CyderFonts.segoe20);
        resetValues.setFocusPainted(false);
        resetValues.setBackground(CyderColors.regularRed);
        resetValues.addActionListener(e -> {
            User defaultUser = UserUtil.buildDefaultUser();

            // reset foreground
            UserUtil.getCyderUser().setForeground(defaultUser.getForeground());
            foregroundColorBlock.setBackground(ColorUtil.hexToRgb(defaultUser.getForeground()));
            foregroundField.setText(defaultUser.getForeground());

            // apply to input field, output area, and carets
            ConsoleFrame.INSTANCE.getOutputArea().setForeground(
                    ColorUtil.hexToRgb(defaultUser.getForeground()));
            ConsoleFrame.INSTANCE.getInputField().setForeground(
                    ColorUtil.hexToRgb(defaultUser.getForeground()));
            ConsoleFrame.INSTANCE.getInputField().setCaretColor(
                    ColorUtil.hexToRgb(defaultUser.getForeground()));
            ConsoleFrame.INSTANCE.getInputField().setCaret(
                    new CyderCaret(ColorUtil.hexToRgb(defaultUser.getForeground())));

            // reset font
            UserUtil.getCyderUser().setForeground(defaultUser.getFont());
            Font ApplyFont = new Font(defaultUser.getFont(), Font.BOLD, 30);
            ConsoleFrame.INSTANCE.getOutputArea().setFont(ApplyFont);
            ConsoleFrame.INSTANCE.getInputField().setFont(ApplyFont);

            // reset the font on preference editor
            if (fontScrollRef.get() != null) {
                fontScrollRef.get().clearSelectedElements();
            }

            FontLabel.setFont(ApplyFont);

            // reset background color
            UserUtil.getCyderUser().setBackground(defaultUser.getBackground());
            fillColorBlock.setBackground(ColorUtil.hexToRgb(defaultUser.getBackground()));
            fillField.setText(defaultUser.getBackground());

            // reset output fill if active
            if (UserUtil.getCyderUser().getOutputfill().equals("1")) {
                ConsoleFrame.INSTANCE.getOutputArea().setOpaque(true);
                ConsoleFrame.INSTANCE.getOutputArea()
                        .setBackground(ColorUtil.hexToRgb(UserUtil.getCyderUser().getBackground()));
                ConsoleFrame.INSTANCE.getOutputArea().repaint();
                ConsoleFrame.INSTANCE.getOutputArea().revalidate();
            }

            // reset input fill if active
            if (UserUtil.getCyderUser().getInputfill().equals("1")) {
                ConsoleFrame.INSTANCE.getInputField().setOpaque(true);
                ConsoleFrame.INSTANCE.getInputField()
                        .setBackground(ColorUtil.hexToRgb(UserUtil.getCyderUser().getBackground()));
                ConsoleFrame.INSTANCE.getInputField().repaint();
                ConsoleFrame.INSTANCE.getInputField().revalidate();
            }

            // window color
            UserUtil.getCyderUser().setWindowcolor(defaultUser.getWindowcolor());
            windowColorBlock.setBackground(ColorUtil.hexToRgb(defaultUser.getWindowcolor()));
            windowField.setText(defaultUser.getWindowcolor());
            windowColorBlock.setBackground((ColorUtil.hexToRgb(defaultUser.getWindowcolor())));
            CyderColors.setGuiThemeColor((ColorUtil.hexToRgb(defaultUser.getWindowcolor())));
            Preferences.invokeRefresh("windowcolor");

            // set scroll list position to top
            if (fontScrollRef.get() != null) {
                fontScrollRef.get().getScrollPane().getVerticalScrollBar().setValue(
                        fontScrollRef.get().getScrollPane().getVerticalScrollBar().getMinimum());
            }

            // other defaults colors below
            switchingLabel.revalidate();
            editUserFrame.notify("Default fonts and colors set");
        });
        resetValues.setBounds(50 + 160, 410, 140, 40);
        switchingLabel.add(resetValues);

        switchingLabel.revalidate();
    }

    /**
     * Switches to the preferences preference page.
     * TODO: use CyderSwitch for this instead of checkboxes
     */
    private static void switchToPreferences() {
        JTextPane preferencePane = new JTextPane();
        preferencePane.setEditable(false);
        preferencePane.setAutoscrolls(false);
        preferencePane.setBounds(0, 0, 720, 500);
        preferencePane.setFocusable(true);
        preferencePane.setOpaque(false);
        preferencePane.setBackground(Color.white);

        //adding components
        StringUtil printingUtil = new StringUtil(new CyderOutputPane(preferencePane));

        //print pairs here
        CyderLabel prefsTitle = new CyderLabel("Preferences");
        prefsTitle.setFont(CyderFonts.segoe30);
        printingUtil.printlnComponent(prefsTitle);

        for (int i = 0; i < Preferences.getPreferences().size() ; i++) {
            if (Preferences.getPreferences().get(i).getDisplayName().equals("IGNORE"))
                continue;

            int localIndex = i;

            CyderLabel preferenceLabel = new CyderLabel(Preferences.getPreferences().get(i).getDisplayName());
            preferenceLabel.setForeground(CyderColors.navy);
            preferenceLabel.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));
            preferenceLabel.setFont(CyderFonts.defaultFontSmall);
            printingUtil.printlnComponent(preferenceLabel);

            //separate the components
            printingUtil.print("\n");

            //check boxes to toggle preferences
            // todo this is the bodge required to do a checkbox with the proper bounds in a loop
            JLabel togglePrefLabel = new JLabel("<html>SEPARATOR<br/>SEPARATOR<br/>SEPARATOR<br/>SEPARATOR</html>") {
                @Override
                public void paintComponent(Graphics g) {
                    boolean setSelected = UserUtil.getUserDataById((Preferences
                            .getPreferences().get(localIndex).getID())).equalsIgnoreCase("1");

                    int xOffset = switchingLabel.getWidth() / 2 - 35;
                    Color background = CyderColors.navy;
                    int sideLength = 50;
                    int borderWidth = 3;

                    Graphics2D graphics2D = (Graphics2D) g;

                    //common part extracted
                    graphics2D.setPaint(background);
                    GeneralPath outlinePath = new GeneralPath();
                    outlinePath.moveTo(xOffset, 0);
                    outlinePath.lineTo(sideLength + xOffset,0);
                    outlinePath.lineTo(sideLength + xOffset,sideLength);

                    if (setSelected) {
                        outlinePath.lineTo(xOffset,sideLength);
                        outlinePath.lineTo(xOffset,0);
                        outlinePath.closePath();
                        graphics2D.fill(outlinePath);

                        //move enter check down
                        int yTranslate = 4;

                        graphics2D.setColor(CyderColors.regularPink);

                        //thickness of line drawn
                        graphics2D.setStroke(new BasicStroke(5));

                        int cornerOffset = 5;
                        graphics2D.drawLine(xOffset + sideLength - borderWidth - cornerOffset,
                                borderWidth + cornerOffset + yTranslate,
                                xOffset + sideLength / 2, sideLength / 2 + yTranslate);

                        //length from center to bottom most check point
                        int secondaryDip = 5;
                        graphics2D.drawLine(xOffset + sideLength / 2,
                                sideLength / 2 + yTranslate,
                                xOffset + sideLength / 2 - secondaryDip,
                                sideLength / 2 + secondaryDip + yTranslate);

                        //length from bottom most part back up
                        int lengthUp = 9;
                        graphics2D.drawLine(xOffset + sideLength / 2 - secondaryDip,
                                sideLength / 2 + secondaryDip + yTranslate,
                                xOffset + sideLength / 2 - secondaryDip - lengthUp,
                                sideLength / 2 + secondaryDip - lengthUp + yTranslate);

                    } else {
                        outlinePath.lineTo(xOffset,50);
                        outlinePath.lineTo(xOffset,0);
                        outlinePath.closePath();
                        graphics2D.fill(outlinePath);

                        graphics2D.setPaint(CyderColors.vanila);
                        GeneralPath fillPath = new GeneralPath();
                        fillPath.moveTo(borderWidth + xOffset, borderWidth);
                        fillPath.lineTo(xOffset + sideLength - borderWidth,borderWidth);
                        fillPath.lineTo(xOffset + sideLength - borderWidth,sideLength - borderWidth);
                        fillPath.lineTo(xOffset + borderWidth,sideLength - borderWidth);
                        fillPath.lineTo(xOffset + borderWidth,borderWidth);
                        fillPath.closePath();
                        graphics2D.fill(fillPath);
                    }
                }
            };

            togglePrefLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    String localID = Preferences.getPreferences().get(localIndex).getID();

                    boolean wasSelected = UserUtil.getUserDataById(localID).equalsIgnoreCase("1");
                    UserUtil.setUserDataById(localID, wasSelected ? "0" : "1");

                    Preferences.invokeRefresh(localID);
                    togglePrefLabel.repaint();
                }
            });
            togglePrefLabel.setToolTipText(Preferences.getPreferences().get(localIndex).getTooltip());
            printingUtil.printlnComponent(togglePrefLabel);
        }

        CyderScrollPane preferenceScroll = new CyderScrollPane(preferencePane);
        preferenceScroll.setThumbSize(7);
        preferenceScroll.getViewport().setOpaque(false);
        preferenceScroll.setFocusable(true);
        preferenceScroll.setOpaque(false);
        preferenceScroll.setThumbColor(CyderColors.regularPink);
        preferenceScroll.setBackground(Color.white);
        preferenceScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        preferenceScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        preferenceScroll.setBounds(6, 5, 708, 490);

        //after everything is on pane, use this to center it
        StyledDocument doc = preferencePane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

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
        prefsTitle.setFont(CyderFonts.segoe30);
        printingUtil.printlnComponent(prefsTitle);

        printingUtil.print("\n\n");

        CyderLabel changeUsernameLabel = new CyderLabel("Change username");
        printingUtil.printlnComponent(changeUsernameLabel);

        printingUtil.print("\n");

        CyderButton changeUsernameButton = new CyderButton("   Change Username   ");
        JTextField changeUsernameField = new JTextField(0);
        changeUsernameField.setHorizontalAlignment(JTextField.CENTER);
        changeUsernameField.addActionListener(e -> changeUsernameButton.doClick());
        changeUsernameField.setToolTipText("Change account username to a valid alternative");
        changeUsernameField.setBackground(CyderColors.vanila);
        changeUsernameField.setSelectionColor(CyderColors.selectionColor);
        changeUsernameField.setFont(new Font("Agency FB",Font.BOLD, 26));
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
        changeUsernameButton.setFont(CyderFonts.segoe20);
        changeUsernameButton.addActionListener(e -> {
            String newUsername = changeUsernameField.getText();
            if (!StringUtil.isNull(newUsername) && !newUsername.equalsIgnoreCase(UserUtil.getCyderUser().getName())) {
                IOUtil.changeUsername(newUsername);
                editUserFrame.notify("Username successfully changed to \"" + newUsername + "\"");
                ConsoleFrame.INSTANCE.getConsoleCyderFrame()
                        .setTitle(CyderShare.VERSION + " Cyder [" + newUsername + "]");
                changeUsernameField.setText(UserUtil.getCyderUser().getName());
            }
        });
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

        changePasswordConfField.addActionListener(e -> changePassword.doClick());
        changePasswordConfField.setFont(changePasswordField.getFont());
        changePasswordConfField.setToolTipText("New Password Confirmation");
        printingUtil.printlnComponent(changePasswordConfField);

        printingUtil.print("\n");

        changePassword.addActionListener(e -> {
            char[] newPassword = changePasswordField.getPassword();
            char[] newPasswordConf = changePasswordConfField.getPassword();

            boolean alphabet = false;
            boolean number = false;

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
                if (!Arrays.equals(newPasswordConf,newPassword)) {
                    editUserFrame.notify("Sorry, " + UserUtil.getCyderUser().getName() + ", " +
                            "but your provided passwords were not equal");
                    changePasswordField.setText("");
                    changePasswordConfField.setText("");
                } else {
                    IOUtil.changePassword(newPassword);
                    editUserFrame.notify("Password successfully changed");
                }
            }

            changePasswordField.setText("");
            changePasswordConfField.setText("");

            Arrays.fill(newPasswordConf, '\0');
            Arrays.fill(newPassword, '\0');
        });
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

        CyderButton validateDatePatternButton = new CyderButton("   Validate   ");
        JTextField consoleDatePatternField = new JTextField(0);
        consoleDatePatternField.setHorizontalAlignment(JTextField.CENTER);
        consoleDatePatternField.addActionListener(e -> validateDatePatternButton.doClick());
        consoleDatePatternLabel.setToolTipText("Java date/time pattern to use for the console clock");
        consoleDatePatternField.setBackground(CyderColors.vanila);
        consoleDatePatternField.setSelectionColor(CyderColors.selectionColor);
        consoleDatePatternField.setFont(CyderFonts.segoe20);
        consoleDatePatternField.setForeground(CyderColors.navy);
        consoleDatePatternField.setCaretColor(CyderColors.navy);
        consoleDatePatternField.setCaret(new CyderCaret(CyderColors.navy));
        consoleDatePatternField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        consoleDatePatternField.setOpaque(true);
        printingUtil.printlnComponent(consoleDatePatternField);
        consoleDatePatternField.setText(UserUtil.getCyderUser().getConsoleclockformat());

        printingUtil.print("\n");

        validateDatePatternButton.addActionListener(e -> {
            String fieldText = StringUtil.getTrimmedText(consoleDatePatternField.getText());

            try {
                // if success, valid date pattern
                new SimpleDateFormat(fieldText).format(new Date());

                //valid so write and refresh ConsoleClock
                UserUtil.getCyderUser().setConsoleclockformat(fieldText);
                ConsoleFrame.INSTANCE.refreshClockText();
                consoleDatePatternField.setText(fieldText);
            } catch (Exception ex) {
                ExceptionHandler.silentHandle(ex);
            }
        });
        printingUtil.printlnComponent(validateDatePatternButton);

        printingUtil.print("\n\n");

        CyderLabel addMap = new CyderLabel("Add Maps");
        printingUtil.printlnComponent(addMap);

        printingUtil.print("\n");

        CyderButton addMapButton = new CyderButton("    Add Map    ");
        JTextField addMapField = new JTextField(0);
        addMapField.setHorizontalAlignment(JTextField.CENTER);
        addMapField.addActionListener(e -> addMapButton.doClick());
        addMapField.setToolTipText("Add format: map_name, PATH/TO/EXE or FILE or URL");
        addMapField.setBackground(CyderColors.vanila);
        addMapField.setSelectionColor(CyderColors.selectionColor);
        addMapField.setFont(CyderFonts.segoe20);
        addMapField.setForeground(CyderColors.navy);
        addMapField.setCaretColor(CyderColors.navy);
        addMapField.setCaret(new CyderCaret(CyderColors.navy));
        addMapField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        addMapField.setOpaque(true);
        printingUtil.printlnComponent(addMapField);

        printingUtil.print("\n");

        addMapButton.addActionListener(e -> {
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
                                    ConsoleFrame.INSTANCE.revalidateMenu();
                                }
                            } else {
                                editUserFrame.notify("Invalid map name");
                            }
                        }
                    }

                    addMapField.setText("");
                }
            }
        });
        printingUtil.printlnComponent(addMapButton);

        printingUtil.print("\n\n");

        CyderLabel removeMap = new CyderLabel("Remove Map");
        printingUtil.printlnComponent(removeMap);

        printingUtil.print("\n");

        CyderButton removeMapButton = new CyderButton("    Remove Map   ");
        JTextField removeMapField = new JTextField(0);
        removeMapField.setHorizontalAlignment(JTextField.CENTER);
        removeMapField.addActionListener(e -> removeMapButton.doClick());
        removeMapField.setToolTipText("Name of map to remove");
        removeMapField.setBackground(CyderColors.vanila);
        removeMapField.setSelectionColor(CyderColors.selectionColor);
        removeMapField.setFont(CyderFonts.segoe20);
        removeMapField.setForeground(CyderColors.navy);
        removeMapField.setCaretColor(CyderColors.navy);
        removeMapField.setCaret(new CyderCaret(CyderColors.navy));
        removeMapField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        removeMapField.setOpaque(true);
        printingUtil.printlnComponent(removeMapField);

        printingUtil.print("\n");

        removeMapButton.addActionListener(e -> {
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
                    ConsoleFrame.INSTANCE.revalidateMenu();
                } else {
                    editUserFrame.notify("Could not locate specified map");
                }

                removeMapField.setText("");
            }
        });
        printingUtil.printlnComponent(removeMapButton);

        printingUtil.print("\n\n");

        CyderLabel ffmpegLabel = new CyderLabel("FFMPEG Path");
        printingUtil.printlnComponent(ffmpegLabel);

        printingUtil.print("\n");

        CyderButton validateFfmpegButton = new CyderButton("    Validate Path   ");
        JTextField ffmpegField = new JTextField(0);
        ffmpegField.setHorizontalAlignment(JTextField.CENTER);
        ffmpegField.addActionListener(e -> validateFfmpegButton.doClick());
        ffmpegField.setToolTipText("Path to ffmpeg.exe");
        ffmpegField.setBackground(CyderColors.vanila);
        ffmpegField.setSelectionColor(CyderColors.selectionColor);
        ffmpegField.setFont(CyderFonts.segoe20);
        ffmpegField.setForeground(CyderColors.navy);
        ffmpegField.setCaretColor(CyderColors.navy);
        ffmpegField.setCaret(new CyderCaret(CyderColors.navy));
        ffmpegField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        ffmpegField.setOpaque(true);
        printingUtil.printlnComponent(ffmpegField);
        ffmpegField.setText(UserUtil.getCyderUser().getFfmpegpath());

        printingUtil.print("\n");

        validateFfmpegButton.addActionListener(e -> {
            String text = ffmpegField.getText().trim();

            if (!text.isEmpty()) {
                File ffmpegMaybe = new File(text);
                if (ffmpegMaybe.exists() && ffmpegMaybe.isFile() &&
                        FileUtil.getExtension(ffmpegMaybe).equals(".exe")) {
                    UserUtil.getCyderUser().setFfmpegpath(text);
                    editUserFrame.notify("ffmpeg path successfully set");
                } else {
                    editUserFrame.notify("ffmpeg does not exist at the provided path");
                    ffmpegField.setText(UserUtil.getCyderUser().getFfmpegpath());
                }
            }
        });
        printingUtil.printlnComponent(validateFfmpegButton);

        printingUtil.print("\n\n");

        CyderLabel youtubeDLLabel = new CyderLabel("YouTubeDL Path");
        printingUtil.printlnComponent(youtubeDLLabel);

        printingUtil.print("\n");

        CyderButton validateYouTubeDL = new CyderButton("   Validate Path  ");
        JTextField youtubedlField = new JTextField(0);
        youtubedlField.setHorizontalAlignment(JTextField.CENTER);
        youtubedlField.addActionListener(e -> validateYouTubeDL.doClick());
        youtubedlField.setToolTipText("Path to youtubedl.exe");
        youtubedlField.setBackground(CyderColors.vanila);
        youtubedlField.setSelectionColor(CyderColors.selectionColor);
        youtubedlField.setFont(CyderFonts.segoe20);
        youtubedlField.setForeground(CyderColors.navy);
        youtubedlField.setCaretColor(CyderColors.navy);
        youtubedlField.setCaret(new CyderCaret(CyderColors.navy));
        youtubedlField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        youtubedlField.setOpaque(true);
        printingUtil.printlnComponent(youtubedlField);
        youtubedlField.setText(UserUtil.getCyderUser().getYoutubedlpath());

        printingUtil.print("\n");

        validateYouTubeDL.addActionListener(e -> {
            String text = youtubedlField.getText().trim();

            if (!text.isEmpty()) {
                File youtubeDLMaybe = new File(text);
                if (youtubeDLMaybe.exists() && youtubeDLMaybe.isFile() &&
                        FileUtil.getExtension(youtubeDLMaybe).equals(".exe")) {
                    UserUtil.getCyderUser().setYoutubedlpath(text);
                    editUserFrame.notify("youtube-dl path successfully set");
                } else {
                    editUserFrame.notify("youtube-dl does not exist at the provided path");
                    youtubedlField.setText(UserUtil.getCyderUser().getYoutubedlpath());
                }
            }
        });
        printingUtil.printlnComponent(validateYouTubeDL);

        printingUtil.print("\n\n");

        CyderLabel deleteUserLabel = new CyderLabel("Delete User");
        printingUtil.printlnComponent(deleteUserLabel);

        CyderButton deleteUserButton = new CyderButton("    Delete user    ");
        CyderPasswordField deletePasswordField = new CyderPasswordField();
        deletePasswordField.setToolTipText("Enter password to confirm account deletion");
        deletePasswordField.addActionListener(e -> deleteUserButton.doClick());
        printingUtil.printlnComponent(deletePasswordField);

        printingUtil.print("\n");

        deleteUserButton.addActionListener(e -> {
            String hashed = SecurityUtil.toHexString(SecurityUtil.getSHA256(deletePasswordField.getPassword()));

            if (!SecurityUtil.toHexString(SecurityUtil.getSHA256(hashed.toCharArray()))
                    .equals(UserUtil.getCyderUser().getPass())) {
                editUserFrame.notify("Invalid password; user not deleted");
                deletePasswordField.setText("");
            } else {
                CyderThreadRunner.submit(() -> {
                    GetterBuilder builder = new GetterBuilder("Final warning: you are about to"
                            + " delete your Cyder account. All files, pictures, downloaded music, notes," +
                            " etc. will be deleted. Are you ABSOLUTELY sure you wish to continue?");
                    builder.setRelativeTo(editUserFrame);
                    boolean delete = GetterUtil.getInstance().getConfirmation(builder);

                    if (delete) {
                        ConsoleFrame.INSTANCE.closeConsoleFrame(false, true);

                        // close all frames, console frame is already closed
                        FrameUtil.closeAllFrames(true);

                        // attempt to delete directory
                        OSUtil.delete(new File("dynamic/users/" + ConsoleFrame.INSTANCE.getUUID()));

                        // exit with proper condition
                        CyderShare.exit(ExitCondition.UserDeleted);
                    } else {
                       deletePasswordField.setText("");
                       editUserFrame.notify("Account not deleted");
                    }
               },"Account deletion confirmation");
            }
        });
        printingUtil.printlnComponent(deleteUserButton);

        printingUtil.print("\n\n");

        CyderLabel weatherKeyLabel = new CyderLabel("Weather Key (Click me to get a key)");
        weatherKeyLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkUtil.openUrl(CyderUrls.OPEN_WEATHER_SIGN_UP);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                weatherKeyLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                weatherKeyLabel.setForeground(CyderColors.navy);
            }
        });
        printingUtil.printlnComponent(weatherKeyLabel);

        printingUtil.print("\n");

        JTextField weatherKeyField = new JTextField(0);
        weatherKeyField.setHorizontalAlignment(JTextField.CENTER);
        CyderButton validateWeatherKey = new CyderButton("   Validate Key  ");
        weatherKeyField.setToolTipText("Your personal OpenWeatherAPI key");
        weatherKeyField.setBackground(CyderColors.vanila);
        weatherKeyField.setSelectionColor(CyderColors.selectionColor);
        weatherKeyField.setFont(CyderFonts.segoe20);
        weatherKeyField.setForeground(CyderColors.navy);
        weatherKeyField.setCaretColor(CyderColors.navy);
        weatherKeyField.setCaret(new CyderCaret(CyderColors.navy));
        weatherKeyField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        weatherKeyField.setOpaque(true);
        printingUtil.printlnComponent(weatherKeyField);
        weatherKeyField.setText(UserUtil.getCyderUser().getWeatherkey());

        printingUtil.print("\n");

        validateWeatherKey.addActionListener(e -> CyderThreadRunner.submit(() -> {
            String text = weatherKeyField.getText().trim();

            if (!text.isEmpty()) {
                String OpenString = CyderUrls.OPEN_WEATHER_BASE +
                        //default location
                        "Austin,Tx,USA" + "&appid=" + text + "&units=imperial";

                boolean valid = false;

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(OpenString).openStream()));
                    reader.read();
                    valid = true;
                    reader.close();
                } catch (Exception ex) {
                    ExceptionHandler.silentHandle(ex);
                }

                if (valid) {
                    UserUtil.getCyderUser().setWeatherkey(text);
                    editUserFrame.notify("Weather key validated and set");
                } else {
                    editUserFrame.notify("Invalid weather key");
                    weatherKeyField.setText(UserUtil.getCyderUser().getWeatherkey());
                }
            }
        }, "Weather key validator"));
        printingUtil.printlnComponent(validateWeatherKey);

        printingUtil.print("\n\n");

        CyderLabel ipKeyLabel = new CyderLabel("IP Key (Click me to get a key)");
        ipKeyLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkUtil.openUrl(CyderUrls.IPDATA_SIGN_UP);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ipKeyLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ipKeyLabel.setForeground(CyderColors.navy);
            }
        });
        printingUtil.printlnComponent(ipKeyLabel);

        printingUtil.print("\n");

        JTextField ipKeyField = new JTextField(0);
        ipKeyField.setHorizontalAlignment(JTextField.CENTER);
        CyderButton validateIpKey = new CyderButton("   Validate Key  ");
        ipKeyField.setToolTipText("Your personal IPData key");
        ipKeyField.setBackground(CyderColors.vanila);
        ipKeyField.setSelectionColor(CyderColors.selectionColor);
        ipKeyField.setFont(CyderFonts.segoe20);
        ipKeyField.setForeground(CyderColors.navy);
        ipKeyField.setCaretColor(CyderColors.navy);
        ipKeyField.setCaret(new CyderCaret(CyderColors.navy));
        ipKeyField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        ipKeyField.setOpaque(true);
        printingUtil.printlnComponent(ipKeyField);
        ipKeyField.setText(UserUtil.getCyderUser().getIpkey());

        printingUtil.print("\n");

        validateIpKey.addActionListener(e -> CyderThreadRunner.submit(() -> {
            String text = ipKeyField.getText().trim();

            if (!text.isEmpty()) {
                String url = CyderUrls.IPDATA_BASE + text;

                boolean valid = false;

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
                    valid = true;
                    reader.close();
                } catch (Exception ex) {
                    ExceptionHandler.silentHandle(ex);
                }

                if (valid) {
                    UserUtil.getCyderUser().setIpkey(text);
                    editUserFrame.notify("IP key validated and set");

                    // pull data from the IP key
                    IPUtil.parseData();
                } else {
                    editUserFrame.notify("Invalid IP key");
                    ipKeyField.setText(UserUtil.getCyderUser().getIpkey());
                }
            }
        }, "IP key validator"));
        printingUtil.printlnComponent(validateIpKey);

        printingUtil.print("\n\n");

        CyderLabel fontMetricLabel = new CyderLabel("Font Metric");
        printingUtil.printlnComponent(fontMetricLabel);

        printingUtil.print("\n");

        JTextField fontMetricField = new JTextField(0);
        fontMetricField.setHorizontalAlignment(JTextField.CENTER);
        fontMetricField.setToolTipText("Font metrics: 0 = plain, 1 = bold, 2 = italic, 3 = bold + italic");
        fontMetricField.setBackground(CyderColors.vanila);
        fontMetricField.setSelectionColor(CyderColors.selectionColor);
        fontMetricField.setFont(new Font(
                UserUtil.getCyderUser().getFont(),
                Integer.parseInt(UserUtil.getCyderUser().getFontmetric()),
                20));
        fontMetricField.setForeground(CyderColors.navy);
        fontMetricField.setCaretColor(CyderColors.navy);
        fontMetricField.setCaret(new CyderCaret(CyderColors.navy));
        fontMetricField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        fontMetricField.setOpaque(true);
        printingUtil.printlnComponent(fontMetricField);
        fontMetricField.setText(UserUtil.getCyderUser().getFontmetric());
        fontMetricField.addActionListener(e -> {
            String numbers = fontMetricField.getText().replace("[^0-9]+","");

            if (!numbers.isEmpty()) {
                int number = Integer.parseInt(numbers);

                if (number < 0 || number > 3) {
                    fontMetricField.setText(UserUtil.getCyderUser().getFontmetric());
                    editUserFrame.notify("Font metric has to be in the list [0,1,2,3]");
                    return;
                }

                UserUtil.getCyderUser().setFontmetric(numbers);

                fontMetricField.setFont(new Font(
                        UserUtil.getCyderUser().getFont(), number,
                        Integer.parseInt(UserUtil.getCyderUser().getFontsize())));
                Preferences.invokeRefresh("fontmetric");
            } else {
                fontMetricField.setText(UserUtil.getCyderUser().getFontmetric());
                editUserFrame.notify("Font metric has to be in the list [0,1,2,3]");
            }
        });

        printingUtil.println("\n\n");

        CyderLabel youtubeKeyLabel = new CyderLabel("YouTubeAPI3 Key (Click me to get a key)");
        youtubeKeyLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkUtil.openUrl(CyderUrls.YOUTUBE_API_V3_SIGN_UP);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                youtubeKeyLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                youtubeKeyLabel.setForeground(CyderColors.navy);
            }
        });
        printingUtil.printlnComponent(youtubeKeyLabel);

        printingUtil.print("\n\n");

        JTextField youtubeAPI3Field = new JTextField(0);
        youtubeAPI3Field.setHorizontalAlignment(JTextField.CENTER);
        CyderButton validateYoutubeAPI = new CyderButton("   Validate Key  ");
        youtubeAPI3Field.setToolTipText("Your personal YouTubeAPI3 key");
        youtubeAPI3Field.setBackground(CyderColors.vanila);
        youtubeAPI3Field.setSelectionColor(CyderColors.selectionColor);
        youtubeAPI3Field.setFont(CyderFonts.segoe20);
        youtubeAPI3Field.setForeground(CyderColors.navy);
        youtubeAPI3Field.setCaretColor(CyderColors.navy);
        youtubeAPI3Field.setCaret(new CyderCaret(CyderColors.navy));
        youtubeAPI3Field.setBorder(new LineBorder(CyderColors.navy, 5, false));
        youtubeAPI3Field.setOpaque(true);
        printingUtil.printlnComponent(youtubeAPI3Field);
        youtubeAPI3Field.setText(UserUtil.getCyderUser().getYouTubeAPI3Key());

        printingUtil.print("\n");

        validateYoutubeAPI.addActionListener(e -> CyderThreadRunner.submit(() -> {
            String text = youtubeAPI3Field.getText().trim();

            if (!text.isEmpty()) {
                try {
                    NetworkUtil.readUrl(CyderUrls.YOUTUBE_API_V3_SEARCH +
                            "?part=snippet&q=gift+and+a+curse+skizzy+mars&type=video&key=" + text);
                    UserUtil.getCyderUser().setYouTubeAPI3Key(text);
                    editUserFrame.notify("YouTubeAPI3 key successfully set");
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                    editUserFrame.notify("Invalid api key");
                    youtubeAPI3Field.setText(UserUtil.getCyderUser().getYouTubeAPI3Key());
                }
            }
        }, "YouTubeAPI3 key validator"));
        printingUtil.printlnComponent(validateYoutubeAPI);

        printingUtil.print("\n");

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
}
