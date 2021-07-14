package cyder.genesis;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.exception.FatalException;
import cyder.handler.ErrorHandler;
import cyder.handler.PhotoViewer;
import cyder.ui.*;
import cyder.utilities.ColorUtil;
import cyder.utilities.GetterUtil;
import cyder.utilities.IOUtil;
import cyder.utilities.StringUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class UserEditor {
    private CyderFrame editUserFrame;
    private CyderScrollPane musicBackgroundScroll;

    private CyderButton addMusicBackground;
    private CyderButton openMusicBackground;
    private CyderButton deleteMusicBackground;
    private CyderButton renameMusicBackground;

    private JList<?> componentsList;
    private List<String> musicBackgroundNameList;
    private List<File> musicBackgroundList;
    private LinkedList<String> fontList = new LinkedList<>();
    private CyderButton changeUsername;
    private CyderButton changePassword;
    private CyderButton forwardPanel;
    private CyderButton backwardPanel;
    private JLabel switchingLabel;
    private int prefsPanelIndex;

    public UserEditor() {
        if (editUserFrame != null)
            editUserFrame.closeAnimation();

        editUserFrame = new CyderFrame(900, 700, CyderImages.defaultBackground);
        editUserFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);
        editUserFrame.setTitle("Edit User");
        editUserFrame.initializeResizing();
        editUserFrame.setMaximumSize(new Dimension(900,700));
        editUserFrame.setResizable(true);
        editUserFrame.addCloseListener(e -> ConsoleFrame.getConsoleFrame().refreshBasedOnPrefs());

        switchingLabel = new JLabel();
        switchingLabel.setForeground(new Color(255, 255, 255));
        switchingLabel.setBounds(90, 70, 720, 500);
        switchingLabel.setOpaque(true);
        switchingLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        switchingLabel.setBackground(new Color(255, 255, 255));
        editUserFrame.getContentPane().add(switchingLabel);

        switchToMusicAndBackgrounds();

        backwardPanel = new CyderButton("<");
        backwardPanel.setBackground(CyderColors.regularRed);
        backwardPanel.setColors(CyderColors.regularRed);
        backwardPanel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        backwardPanel.setFont(CyderFonts.weatherFontSmall);
        backwardPanel.addActionListener(e -> lastEditUser());
        backwardPanel.setBounds(20, 260, 50, 120);
        editUserFrame.getContentPane().add(backwardPanel);

        forwardPanel = new CyderButton(">");
        forwardPanel.setBackground(CyderColors.regularRed);
        forwardPanel.setColors(CyderColors.regularRed);
        forwardPanel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        forwardPanel.setFont(CyderFonts.weatherFontSmall);
        forwardPanel.addActionListener(e -> nextEditUser());
        forwardPanel.setBounds(830, 260, 50, 120);
        editUserFrame.getContentPane().add(forwardPanel);

        CyderTextField changeUsernameField = new CyderTextField(0);
        changeUsernameField.addActionListener(e -> changeUsername.doClick());
        changeUsernameField.setBackground(Color.white);
        changeUsernameField.setFont(CyderFonts.weatherFontSmall);
        changeUsernameField.setBounds(90, 590, 260, 40);
        editUserFrame.getContentPane().add(changeUsernameField);

        changeUsername = new CyderButton("Change Username");
        changeUsername.setBackground(CyderColors.regularRed);
        changeUsername.setColors(CyderColors.regularRed);
        changeUsername.setBorder(new LineBorder(CyderColors.navy, 5, false));
        changeUsername.setFont(CyderFonts.weatherFontSmall);
        changeUsername.addActionListener(e -> {
            String newUsername = changeUsernameField.getText();
            if (!StringUtil.empytStr(newUsername)) {
                IOUtil.changeUsername(newUsername);
                editUserFrame.inform("Username successfully changed", "");
                ConsoleFrame.getConsoleFrame().setTitle(IOUtil.getSystemData("Version") + " Cyder [" + newUsername + "]");
                changeUsernameField.setText("");
            }
        });
        changeUsername.setBounds(90, 640, 260, 40);
        editUserFrame.getContentPane().add(changeUsername);

        CyderButton deleteUser = new CyderButton("Delete User");
        deleteUser.setBackground(CyderColors.regularRed);
        deleteUser.setColors(CyderColors.regularRed);
        deleteUser.setBorder(new LineBorder(CyderColors.navy, 5, false));
        deleteUser.setFont(CyderFonts.weatherFontSmall);
        deleteUser.addActionListener(e -> {
            ConsoleFrame.getConsoleFrame().getInputHandler().println("Are you sure you want to permanently " +
                    "delete this account? This action cannot be undone! (yes/no)");
            ConsoleFrame.getConsoleFrame().getInputHandler().setUserInputMode(true);
            ConsoleFrame.getConsoleFrame().getInputField().requestFocus();
            ConsoleFrame.getConsoleFrame().getInputHandler().setUserInputDesc("deleteuser");
        });
        deleteUser.setBounds(375, 590, 150, 90);
        editUserFrame.getContentPane().add(deleteUser);

        JPasswordField changePasswordField = new JPasswordField(15);
        changePasswordField.addActionListener(e -> changePassword.doClick());
        changePasswordField.setFont(new Font("Agency FB",Font.BOLD, 20));
        changePasswordField.setSelectionColor(CyderColors.selectionColor);
        changePasswordField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        changePasswordField.setToolTipText("New password");
        changePasswordField.setBounds(550, 590, 260, 40);
        editUserFrame.getContentPane().add(changePasswordField);
        changePasswordField.setForeground(CyderColors.navy);
        changePasswordField.setCaretColor(CyderColors.navy);
        changePasswordField.setCaret(new CyderCaret(CyderColors.navy));

        changePassword = new CyderButton("Change Password");
        changePassword.setBackground(CyderColors.regularRed);
        changePassword.setColors(CyderColors.regularRed);
        changePassword.setBorder(new LineBorder(CyderColors.navy, 5, false));
        changePassword.setFont(CyderFonts.weatherFontSmall);
        changePassword.addActionListener(e -> {
            char[] newPassword = changePasswordField.getPassword();

            if (newPassword.length > 4) {
                IOUtil.changePassword(newPassword);
                editUserFrame.inform("Password successfully changed", "");
            } else {
                editUserFrame.inform("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", " +
                        "but your password must be greater than 4 characters for security reasons.", "");
            }
            changePasswordField.setText("");

            for (char c : newPassword) {
                c = '\0';
            }
        });
        changePassword.setBounds(550, 640, 260, 40);
        editUserFrame.getContentPane().add(changePassword);
        editUserFrame.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelative(editUserFrame);
    }

    private void initMusicBackgroundList() {
        File backgroundDir = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Backgrounds");
        File musicDir = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Music");

        musicBackgroundList = new LinkedList<>();
        musicBackgroundNameList = new LinkedList<>();

        for (File file : backgroundDir.listFiles()) {
            if (file.getName().endsWith((".png"))) {
                musicBackgroundList.add(file.getAbsoluteFile());
                musicBackgroundNameList.add("Backgrounds/" + StringUtil.getFilename(file));
            }
        }

        for (File file : musicDir.listFiles()) {
            if (file.getName().endsWith((".mp3"))) {
                musicBackgroundList.add(file.getAbsoluteFile());
                musicBackgroundNameList.add("Music/" + StringUtil.getFilename(file));
            }
        }

        String[] BackgroundsArray = new String[musicBackgroundNameList.size()];
        BackgroundsArray = musicBackgroundNameList.toArray(BackgroundsArray);

        //todo music and backgrounds list
        componentsList = new JList(BackgroundsArray);
        componentsList.setFont(CyderFonts.weatherFontSmall);
        componentsList.setForeground(CyderColors.navy);
        componentsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && componentsList.getSelectedIndex() != -1) {
                    openMusicBackground.doClick();
                }
            }
        });

        componentsList.setSelectionBackground(CyderColors.selectionColor);
    }

    private void nextEditUser() {
        switchingLabel.removeAll();
        switchingLabel.revalidate();
        switchingLabel.repaint();
        editUserFrame.revalidate();
        editUserFrame.repaint();

        prefsPanelIndex++;

        if (prefsPanelIndex == 3)
            prefsPanelIndex = 0;

        switch (prefsPanelIndex) {
            case 0:
                switchToMusicAndBackgrounds();
                break;
            case 1:
                switchToFontAndColor();
                break;

            case 2:
                switchToPreferences();
                break;
        }
    }

    private void lastEditUser() {
        switchingLabel.removeAll();
        switchingLabel.revalidate();
        switchingLabel.repaint();
        editUserFrame.revalidate();
        editUserFrame.repaint();

        prefsPanelIndex--;

        if (prefsPanelIndex == -1)
            prefsPanelIndex = 2;

        switch (prefsPanelIndex) {
            case 0:
                switchToMusicAndBackgrounds();
                break;
            case 1:
                switchToFontAndColor();
                break;

            case 2:
                switchToPreferences();
                break;
        }
    }

    private void switchToMusicAndBackgrounds() {
        JLabel BackgroundLabel = new JLabel("Music & Backgrounds", SwingConstants.CENTER);
        BackgroundLabel.setFont(CyderFonts.weatherFontBig);
        BackgroundLabel.setBounds(720 / 2 - 375 / 2, 10, 375, 40);
        switchingLabel.add(BackgroundLabel);

        initMusicBackgroundList();

        componentsList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        musicBackgroundScroll = new CyderScrollPane(componentsList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        musicBackgroundScroll.setSize(400, 400);
        musicBackgroundScroll.setFont(CyderFonts.weatherFontBig);
        musicBackgroundScroll.setThumbColor(CyderColors.regularRed);
        componentsList.setBackground(new Color(255, 255, 255));
        musicBackgroundScroll.getViewport().setBackground(new Color(0, 0, 0, 0));
        musicBackgroundScroll.setBounds(20, 60, 680, 360);
        switchingLabel.add(musicBackgroundScroll);

        addMusicBackground = new CyderButton("Add");
        addMusicBackground.setBorder(new LineBorder(CyderColors.navy, 5, false));
        addMusicBackground.setColors(CyderColors.regularRed);
        addMusicBackground.setFocusPainted(false);
        addMusicBackground.setBackground(CyderColors.regularRed);
        addMusicBackground.addActionListener(e -> {
            try {
                new Thread(() -> {
                    try {
                        //if this is too small or big, where is it resized and why is it too big?
                        File addFile = new GetterUtil().getFile("Choose file to add");

                        if (addFile == null || addFile.getName().equals("NULL"))
                            return;

                        for (File f : ConsoleFrame.getConsoleFrame().getBackgrounds()) {
                            if (addFile.getName().equals(f.getName())) {
                                editUserFrame.notify("Cannot add a background with the same name as a current one");
                                return;
                            }
                        }

                        Path copyPath = new File(addFile.getAbsolutePath()).toPath();

                        if (addFile != null && addFile.getName().endsWith(".png")) {
                            File Destination = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Backgrounds/" + addFile.getName());
                            Files.copy(copyPath, Destination.toPath());
                            initMusicBackgroundList();
                            musicBackgroundScroll.setViewportView(componentsList);
                            musicBackgroundScroll.revalidate();
                            musicBackgroundScroll.repaint();
                        } else if (addFile != null && addFile.getName().endsWith(".mp3")) {
                            File Destination = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Music/" + addFile.getName());
                            Files.copy(copyPath, Destination.toPath());
                            initMusicBackgroundList();
                            musicBackgroundScroll.setViewportView(componentsList);
                            musicBackgroundScroll.revalidate();
                        } else {
                            editUserFrame.inform("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", but you can only add PNGs and MP3s", "Error");
                        }

                        ConsoleFrame.getConsoleFrame().resizeBackgrounds();

                    } catch (Exception ex) {
                        ErrorHandler.handle(ex);
                    }
                }, "wait thread for GetterUtil().getFile()").start();
            } catch (Exception exc) {
                ErrorHandler.handle(exc);
            }
        });
        addMusicBackground.setFont(CyderFonts.weatherFontSmall);
        addMusicBackground.setBounds(20, 440, 155, 40);
        switchingLabel.add(addMusicBackground);

        openMusicBackground = new CyderButton("Open");
        openMusicBackground.setBorder(new LineBorder(CyderColors.navy, 5, false));
        openMusicBackground.setColors(CyderColors.regularRed);
        openMusicBackground.setFocusPainted(false);
        openMusicBackground.setBackground(CyderColors.regularRed);
        openMusicBackground.setFont(CyderFonts.weatherFontSmall);
        openMusicBackground.addActionListener(e -> {
            List<?> clickedSelectionList = componentsList.getSelectedValuesList();

            if (!clickedSelectionList.isEmpty()) {
                String ClickedSelection = clickedSelectionList.get(0).toString();

                File ClickedSelectionPath = null;

                for (int i = 0; i < musicBackgroundNameList.size(); i++) {
                    if (ClickedSelection.equals(musicBackgroundNameList.get(i))) {
                        ClickedSelectionPath = musicBackgroundList.get(i);
                        break;
                    }
                }

                if (ClickedSelectionPath != null) {
                    if (ClickedSelectionPath.getName().endsWith(".png")) {
                        PhotoViewer pv = new PhotoViewer(ClickedSelectionPath);
                    } else if (ClickedSelectionPath.getName().endsWith(".mp3")) {
                        IOUtil.mp3(ClickedSelectionPath.toString());
                    }
                }
            }
        });
        openMusicBackground.setBounds(20 + 155 + 20, 440, 155, 40);
        switchingLabel.add(openMusicBackground);

        renameMusicBackground = new CyderButton("Rename");
        renameMusicBackground.setBorder(new LineBorder(CyderColors.navy, 5, false));
        renameMusicBackground.setColors(CyderColors.regularRed);
        renameMusicBackground.addActionListener(e -> new Thread(() -> {
            try {
                if (!componentsList.getSelectedValuesList().isEmpty()) {
                    List clickedSelections = componentsList.getSelectedValuesList();
                    File selectedFile = null;

                    if (!clickedSelections.isEmpty()) {
                        String clickedSelection = clickedSelections.get(0).toString();

                        for (int i = 0; i < musicBackgroundNameList.size(); i++) {
                            if (clickedSelection.equals(musicBackgroundNameList.get(i))) {
                                selectedFile = musicBackgroundList.get(i);
                                break;
                            }
                        }

                        String oldname = StringUtil.getFilename(selectedFile);
                        String extension = StringUtil.getExtension(selectedFile);
                        String newname = new GetterUtil().getString("Rename","Enter any valid file name","Submit");

                        if (oldname.equals(newname) || newname.equals("NULL"))
                            return;

                        File renameTo = new File(selectedFile.getParent() + "/" + newname + extension);

                        if (renameTo.exists())
                            throw new IOException("file exists");

                        boolean success = selectedFile.renameTo(renameTo);

                        if (!success) {
                            throw new FatalException("File was not renamed");
                        } else {
                            editUserFrame.notify(selectedFile.getName() +
                                    " was successfully renamed to " + renameTo.getName());
                        }

                        initMusicBackgroundList();
                        musicBackgroundScroll.setViewportView(componentsList);
                        musicBackgroundScroll.revalidate();
                    }
                }
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        }, "Wait thread for getterUtil").start());

        renameMusicBackground.setBackground(CyderColors.regularRed);
        renameMusicBackground.setFont(CyderFonts.weatherFontSmall);
        renameMusicBackground.setBounds(20 + 155 + 20 + 155 + 20, 440, 155, 40);
        switchingLabel.add(renameMusicBackground);

        deleteMusicBackground = new CyderButton("Delete");
        deleteMusicBackground.setBorder(new LineBorder(CyderColors.navy, 5, false));
        deleteMusicBackground.setColors(CyderColors.regularRed);
        deleteMusicBackground.addActionListener(e -> {
            if (!componentsList.getSelectedValuesList().isEmpty()) {
                List<?> ClickedSelectionListMusic = componentsList.getSelectedValuesList();

                File ClickedSelectionPath = null;

                if (!ClickedSelectionListMusic.isEmpty()) {
                    String ClickedSelection = ClickedSelectionListMusic.get(0).toString();

                    for (int i = 0; i < musicBackgroundNameList.size(); i++) {
                        if (ClickedSelection.equals(musicBackgroundNameList.get(i))) {
                            ClickedSelectionPath = musicBackgroundList.get(i);

                            break;
                        }
                    }

                    if (ClickedSelection.equalsIgnoreCase(ConsoleFrame.getConsoleFrame().getCurrentBackgroundFile().getName().replace(".png", "")))
                        editUserFrame.inform("Unable to delete the background you are currently using", "Error");

                    else {
                        ClickedSelectionPath.delete();
                        initMusicBackgroundList();
                        musicBackgroundScroll.setViewportView(componentsList);
                        musicBackgroundScroll.revalidate();

                        if (ClickedSelection.endsWith(".mp3"))
                            ConsoleFrame.getConsoleFrame().getInputHandler()
                                    .println("Music: " + ClickedSelectionPath.getName()
                                            .replace(".mp3", "") + " successfully deleted.");
                        else if (ClickedSelection.endsWith(".png")) {
                            ConsoleFrame.getConsoleFrame().getInputHandler()
                                    .println("Background: " + ClickedSelectionPath.getName()
                                            .replace(".png", "") + " successfully deleted.");

                            LinkedList<File> paths = ConsoleFrame.getConsoleFrame().getBackgrounds();
                            for (int i = 0; i < paths.size(); i++) {
                                if (paths.get(i).equals(ConsoleFrame.getConsoleFrame().getCurrentBackgroundFile())) {
                                    ConsoleFrame.getConsoleFrame().setBackgroundIndex(i);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });

        deleteMusicBackground.setBackground(CyderColors.regularRed);
        deleteMusicBackground.setFont(CyderFonts.weatherFontSmall);
        deleteMusicBackground.setBounds(20 + 155 + 20 + 155 + 20 + 155 + 20, 440, 155, 40);
        switchingLabel.add(deleteMusicBackground);

        switchingLabel.revalidate();
    }

    private void switchToFontAndColor() {
        JLabel TitleLabel = new JLabel("Colors & Font", SwingConstants.CENTER);
        TitleLabel.setFont(CyderFonts.weatherFontBig);
        TitleLabel.setBounds(720 / 2 - 375 / 2, 10, 375, 40);
        switchingLabel.add(TitleLabel);

        int colorOffsetX = 340;
        int colorOffsetY = 10;

        JLabel ColorLabel = new JLabel("Text Color");
        ColorLabel.setFont(CyderFonts.weatherFontBig);
        ColorLabel.setForeground(CyderColors.navy);
        ColorLabel.setBounds(120 + colorOffsetX, 50 + colorOffsetY, 300, 30);
        switchingLabel.add(ColorLabel);

        JLabel hexLabel = new JLabel("HEX:");
        hexLabel.setFont(CyderFonts.weatherFontSmall);
        hexLabel.setForeground(CyderColors.navy);
        hexLabel.setBounds(30 + colorOffsetX, 110 + colorOffsetY, 70, 30);
        switchingLabel.add(hexLabel);

        JLabel rgbLabel = new JLabel("RGB:");
        rgbLabel.setFont(CyderFonts.weatherFontSmall);
        rgbLabel.setForeground(CyderColors.navy);
        rgbLabel.setBounds(30 + colorOffsetX, 180 + colorOffsetY, 70, 30);
        switchingLabel.add(rgbLabel);

        JTextField colorBlock = new JTextField();
        colorBlock.setBackground(CyderColors.navy);
        colorBlock.setFocusable(false);
        colorBlock.setCursor(null);
        colorBlock.setBackground(ColorUtil.hextorgbColor(IOUtil.getUserData("Foreground")));
        colorBlock.setToolTipText("Color Preview");
        colorBlock.setBorder(new LineBorder(CyderColors.navy, 5, false));
        colorBlock.setBounds(330 + colorOffsetX, 100 + colorOffsetY, 40, 120);
        switchingLabel.add(colorBlock);

        CyderTextField rgbField = new CyderTextField(0);

        CyderTextField hexField = new CyderTextField(0);
        hexField.setText(IOUtil.getUserData("Foreground"));
        hexField.setFont(CyderFonts.weatherFontBig);
        hexField.setToolTipText("Hex Value");
        JTextField finalHexField1 = hexField;
        JTextField finalRgbField = rgbField;
        hexField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    Color c = ColorUtil.hextorgbColor(finalHexField1.getText());
                    finalRgbField.setText(c.getRed() + "," + c.getGreen() + "," + c.getBlue());
                    colorBlock.setBackground(c);
                } catch (Exception ignored) {}
            }
        });
        hexField.setBounds(100 + colorOffsetX, 100 + colorOffsetY, 220, 50);
        hexField.setOpaque(false);
        switchingLabel.add(hexField);


        rgbField.setFont(CyderFonts.weatherFontBig);
        rgbField.setToolTipText("RGB Value");
        rgbField.setBackground(Color.white);
        Color c = ColorUtil.hextorgbColor(IOUtil.getUserData("Foreground"));
        rgbField.setText(c.getRed() + "," + c.getGreen() + "," + c.getBlue());
        JTextField finalRgbField1 = rgbField;
        rgbField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    String[] parts = finalRgbField1.getText().split(",");
                    Color c = new Color(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                    hexField.setText(ColorUtil.rgbtohexString(c));
                    colorBlock.setBackground(c);
                } catch (Exception e) {
                    ErrorHandler.silentHandle(e);
                }
            }
        });
        rgbField.setBounds(100 + colorOffsetX, 170 + colorOffsetY, 220, 50);
        rgbField.setOpaque(false);
        switchingLabel.add(rgbField);

        CyderButton applyColor = new CyderButton("Apply Color");
        applyColor.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        applyColor.setColors(CyderColors.regularRed);
        applyColor.setToolTipText("Apply");
        applyColor.setFont(CyderFonts.weatherFontSmall);
        applyColor.setFocusPainted(false);
        applyColor.setBackground(CyderColors.regularRed);
        applyColor.addActionListener(e -> {
            IOUtil.setUserData("Foreground", hexField.getText());
            Color updateC = ColorUtil.hextorgbColor(hexField.getText());

            ConsoleFrame.getConsoleFrame().getOutputArea().setForeground(updateC);
            ConsoleFrame.getConsoleFrame().getInputField().setForeground(updateC);
            ConsoleFrame.getConsoleFrame().getInputField().setCaretColor(updateC);
            ConsoleFrame.getConsoleFrame().getInputField().setCaret(new CyderCaret(updateC));

            ConsoleFrame.getConsoleFrame().getInputHandler()
                    .println("The Color [" + updateC.getRed() + "," + updateC.getGreen() + ","
                            + updateC.getBlue() + "] has been applied.");
        });
        applyColor.setBounds(450, 240 + colorOffsetY, 200, 40);
        switchingLabel.add(applyColor);

        JLabel FillLabel = new JLabel("Fill Color");
        FillLabel.setFont(CyderFonts.weatherFontBig);
        FillLabel.setForeground(CyderColors.navy);
        FillLabel.setBounds(120 + colorOffsetX, 330 + colorOffsetY, 300, 30);
        switchingLabel.add(FillLabel);

        JLabel hexLabelFill = new JLabel("HEX:");
        hexLabelFill.setFont(CyderFonts.weatherFontSmall);
        hexLabelFill.setForeground(CyderColors.navy);
        hexLabelFill.setBounds(30 + colorOffsetX, 390 + colorOffsetY, 70, 30);
        switchingLabel.add(hexLabelFill);

        JTextField colorBlockFill = new JTextField();
        colorBlockFill.setBackground(CyderColors.navy);
        colorBlockFill.setFocusable(false);
        colorBlockFill.setCursor(null);
        colorBlockFill.setBackground(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")));
        colorBlockFill.setToolTipText("Color Preview");
        colorBlockFill.setBorder(new LineBorder(CyderColors.navy, 5, false));
        colorBlockFill.setBounds(330 + colorOffsetX, 340 + colorOffsetY, 40, 120);
        switchingLabel.add(colorBlockFill);

        CyderTextField hexFieldFill = new CyderTextField(0);
        hexFieldFill.setText(IOUtil.getUserData("Background"));
        hexFieldFill.setFont(CyderFonts.weatherFontBig);
        hexFieldFill.setToolTipText("Input field and output area fill color if enabled");
        hexFieldFill.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    colorBlockFill.setBackground(ColorUtil.hextorgbColor(hexFieldFill.getText()));
                    IOUtil.setUserData("Background", hexFieldFill.getText());
                } catch (Exception ignored) {
                    if (hexFieldFill.getText().length() == 6)
                        editUserFrame.notify("Invalid color");
                }
            }
        });
        hexFieldFill.setBounds(100 + colorOffsetX, 380 + colorOffsetY, 220, 50);
        hexFieldFill.setOpaque(false);
        switchingLabel.add(hexFieldFill);

        JLabel FontLabel = new JLabel("Fonts");
        FontLabel.setFont(new Font(IOUtil.getUserData("Font"),Font.BOLD, 30));
        FontLabel.setForeground(CyderColors.navy);
        FontLabel.setBounds(150, 60, 300, 30);
        switchingLabel.add(FontLabel);

        //todo single element selection
        // upon single selection, single click addElementWithSingleClickAction(),
        // change the font of FontLabel to the selected one

        //todo add get selected element which returns the zero-th element
        //todo add elemental alignment to cyderscrolllist and set to center for this

        String[] Fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Collections.addAll(fontList, Fonts);

        CyderScrollList fontScrollList = new CyderScrollList(300, 300, CyderScrollList.SelectionPolicy.SINGLE);

        for (int i = 0 ; i < fontList.size() ; i++) {
            int finalI = i;
            class thisAction implements CyderScrollList.ScrollAction {
                @Override
                public void fire() {
                    FontLabel.setFont(new Font(fontList.get(finalI),Font.BOLD, 30));
                }
            }

            thisAction action = new thisAction();
            fontScrollList.addElementWithSingleCLickAction(fontList.get(i), action);
        }

        JLabel fontScrollLabel = fontScrollList.generateScrollList();
        fontScrollLabel.setBounds(50, 100, 300, 300);
        switchingLabel.add(fontScrollLabel);

        CyderButton applyFont = new CyderButton("Apply Font");
        applyFont.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        applyFont.setColors(CyderColors.regularRed);
        applyFont.setToolTipText("Apply");
        applyFont.setFont(CyderFonts.weatherFontSmall);
        applyFont.setFocusPainted(false);
        applyFont.setBackground(CyderColors.regularRed);
        applyFont.addActionListener(e -> {
            String selectedFont = fontScrollList.getSelectedElements().get(0);

            if (selectedFont != null) {
                Font ApplyFont = new Font(selectedFont, Font.BOLD, 30);
                ConsoleFrame.getConsoleFrame().getOutputArea().setFont(ApplyFont);
                ConsoleFrame.getConsoleFrame().getInputField().setFont(ApplyFont);
                IOUtil.setUserData("Font", selectedFont);
                ConsoleFrame.getConsoleFrame().getInputHandler().println("The font \"" + selectedFont + "\" has been applied.");
            }
        });
        applyFont.setBounds(100, 420, 200, 40);
        switchingLabel.add(applyFont);

        switchingLabel.revalidate();
    }

    private void switchToPreferences() {
        JTextPane preferencePane = new JTextPane();
        preferencePane.setEditable(false);
        preferencePane.setAutoscrolls(false);
        preferencePane.setBounds(0, 0, 720, 500);
        preferencePane.setFocusable(true);
        preferencePane.setOpaque(false);
        preferencePane.setBackground(Color.white);

        //adding components
        StringUtil printingUtil = new StringUtil(preferencePane);

        //print pairs here
        CyderLabel prefsTitle = new CyderLabel("Preferences");
        prefsTitle.setFont(CyderFonts.weatherFontBig);
        printingUtil.printlnComponent(prefsTitle);

        for (int i = 0 ; i < GenesisShare.getPrefs().size() ; i++) {
            if (GenesisShare.getPrefs().get(i).getTooltip().equals("IGNORE"))
                continue;

            int localIndex = i;

            CyderLabel preferenceLabel = new CyderLabel(GenesisShare.getPrefs().get(i).getDisplayName());
            preferenceLabel.setForeground(CyderColors.navy);
            preferenceLabel.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));
            preferenceLabel.setFont(CyderFonts.defaultFontSmall);
            printingUtil.printlnComponent(preferenceLabel);

            CyderButton preferenceButton = new CyderButton(
                    IOUtil.getUserData(GenesisShare.getPrefs().get(i).getID()).equals("1") ? "      On      " :
                            "      Off      ");
            preferenceButton.setColors(IOUtil.getUserData(GenesisShare.getPrefs().get(i).getID()).equals("1") ? CyderColors.regularGreen : CyderColors.regularRed);
            preferenceButton.setToolTipText(GenesisShare.getPrefs().get(i).getTooltip());
            preferenceButton.addActionListener(e -> {
                boolean wasSelected = IOUtil.getUserData((GenesisShare.getPrefs().get(localIndex).getID())).equalsIgnoreCase("1");
                IOUtil.setUserData(GenesisShare.getPrefs().get(localIndex).getID(), wasSelected ? "0" : "1");

                preferenceButton.setColors(wasSelected ? CyderColors.regularRed : CyderColors.regularGreen);
                preferenceButton.setText(wasSelected ? "      Off      " : "      On      ");

                ConsoleFrame.getConsoleFrame().refreshBasedOnPrefs();
            });

            printingUtil.printlnComponent(preferenceButton);
        }

        CyderScrollPane preferenceScroll = new CyderScrollPane(preferencePane);
        preferenceScroll.setThumbSize(7);
        preferenceScroll.getViewport().setOpaque(false);
        preferenceScroll.setFocusable(true);
        preferenceScroll.setOpaque(false);
        preferenceScroll.setThumbColor(CyderColors.intellijPink);
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
}
