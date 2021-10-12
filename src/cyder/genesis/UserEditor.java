package cyder.genesis;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.enums.Direction;
import cyder.genobjects.User;
import cyder.handler.ErrorHandler;
import cyder.ui.*;
import cyder.utilities.*;
import cyder.widgets.AudioPlayer;
import cyder.widgets.ColorConverter;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UserEditor {
    private static CyderFrame editUserFrame;

    private static CyderButton addMusicBackground;
    private static CyderButton openMusicBackground;
    private static CyderButton deleteMusicBackground;
    private static CyderButton renameMusicBackground;

    private static List<String> musicBackgroundNameList;
    private static List<File> musicBackgroundList;

    private static JLabel musicBackgroundLabel;
    private static CyderScrollList musicBackgroundScroll;

    private static LinkedList<String> fontList = new LinkedList<>();

    private static CyderButton changeUsername;
    private static CyderButton changePassword;

    private static CyderButton forwardPanel;
    private static CyderButton backwardPanel;

    private static JLabel switchingLabel;
    private static int prefsPanelIndex;

    private UserEditor() {}

    public static void showGUI(int startingIndex) {
        if (editUserFrame != null)
            editUserFrame.dispose();

        editUserFrame = new CyderFrame(900, 700, CyderImages.defaultBackground);
        editUserFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);
        editUserFrame.setTitle("Edit User");
        editUserFrame.initializeResizing();
        editUserFrame.setMaximumSize(new Dimension(900,700));
        editUserFrame.setResizable(true);
        editUserFrame.addPreCloseAction(() -> ConsoleFrame.getConsoleFrame().refreshBasedOnPrefs());

        switchingLabel = new JLabel();
        switchingLabel.setForeground(new Color(255, 255, 255));
        switchingLabel.setBounds(90, 70, 720, 500);
        switchingLabel.setOpaque(true);
        switchingLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        switchingLabel.setBackground(new Color(255, 255, 255));
        editUserFrame.getContentPane().add(switchingLabel);

        prefsPanelIndex = startingIndex;

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
            case 3:
                switchToMappingLinks();
                break;
        }

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
        changeUsernameField.setToolTipText("New Username");
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
            if (!StringUtil.empytStr(newUsername) && !newUsername.equalsIgnoreCase(ConsoleFrame.getConsoleFrame().getUsername())) {
                IOUtil.changeUsername(newUsername);
                editUserFrame.inform("Username successfully changed to \"" + newUsername + "\"", "");
                ConsoleFrame.getConsoleFrame().setTitle(IOUtil.getSystemData().getVersion() + " Cyder [" + newUsername + "]");
                changeUsernameField.setText("");
            }
        });
        changeUsernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (changeUsernameField.getText().length() == 1) {
                    changeUsernameField.setText(changeUsernameField.getText().toUpperCase());
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (changeUsernameField.getText().length() == 1) {
                    changeUsernameField.setText(changeUsernameField.getText().toUpperCase());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (changeUsernameField.getText().length() == 1) {
                    changeUsernameField.setText(changeUsernameField.getText().toUpperCase());
                }
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

        CyderPasswordField changePasswordField = new CyderPasswordField();
        changePasswordField.setToolTipText("New Password");
        changePasswordField.setBounds(550, 590, 120, 40);
        editUserFrame.getContentPane().add(changePasswordField);

        CyderPasswordField changePasswordConfField = new CyderPasswordField();
        changePasswordConfField.addActionListener(e -> changePassword.doClick());
        changePasswordConfField.setToolTipText("New Password Confirmation");
        changePasswordConfField.setBounds(680, 590, 130, 40);
        editUserFrame.getContentPane().add(changePasswordConfField);

        changePassword = new CyderButton("Change Password");
        changePassword.setBackground(CyderColors.regularRed);
        changePassword.setColors(CyderColors.regularRed);
        changePassword.setBorder(new LineBorder(CyderColors.navy, 5, false));
        changePassword.setFont(CyderFonts.weatherFontSmall);
        changePassword.addActionListener(e -> {
            char[] newPassword = changePasswordField.getPassword();
            char[] newPasswordConf = changePasswordConfField.getPassword();

            if (newPassword.length > 4) {
                if (!Arrays.equals(newPasswordConf,newPassword)) {
                    editUserFrame.inform("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", " +
                            "but your provided passwords were not equal", "");
                    changePasswordField.setText("");
                    changePasswordConfField.setText("");
                } else {
                    IOUtil.changePassword(newPassword);
                    editUserFrame.inform("Password successfully changed", "");
                }
            } else {
                editUserFrame.inform("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", " +
                        "but your password must be greater than 4 characters for security reasons.", "");
            }
            changePasswordField.setText("");
            changePasswordConfField.setText("");

            for (char c : newPassword) {
                c = '\0';
            }
        });
        changePassword.setBounds(550, 640, 260, 40);
        editUserFrame.getContentPane().add(changePassword);
        editUserFrame.setVisible(true);

        editUserFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    private static void initMusicBackgroundList() {
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
    }

    private static void nextEditUser() {
        switchingLabel.removeAll();
        switchingLabel.revalidate();
        switchingLabel.repaint();
        editUserFrame.revalidate();
        editUserFrame.repaint();

        prefsPanelIndex++;

        if (prefsPanelIndex == 4)
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
            case 3:
                switchToMappingLinks();
                break;
        }
    }

    private static void lastEditUser() {
        switchingLabel.removeAll();
        switchingLabel.revalidate();
        switchingLabel.repaint();
        editUserFrame.revalidate();
        editUserFrame.repaint();

        prefsPanelIndex--;

        if (prefsPanelIndex == -1)
            prefsPanelIndex = 3;

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
            case 3:
                switchToMappingLinks();
                break;
        }
    }

    private static void switchToMusicAndBackgrounds() {
        JLabel BackgroundLabel = new JLabel("Music & Backgrounds", SwingConstants.CENTER);
        BackgroundLabel.setFont(CyderFonts.weatherFontBig);
        BackgroundLabel.setBounds(720 / 2 - 375 / 2, 10, 375, 40);
        switchingLabel.add(BackgroundLabel);

        initMusicBackgroundList();

        musicBackgroundScroll = new CyderScrollList(680, 360, CyderScrollList.SelectionPolicy.SINGLE);
        musicBackgroundScroll.setBorder(null);

        for (int i = 0 ; i < musicBackgroundNameList.size() ; i++) {
            int finalI = i;
            class thisAction implements CyderScrollList.ScrollAction {
                @Override
                public void fire() {
                   IOUtil.openFile(musicBackgroundList.get(finalI).getAbsolutePath());
                }
            }

            thisAction action = new thisAction();
            musicBackgroundScroll.addElement(musicBackgroundNameList.get(i), action);
        }

        musicBackgroundLabel = musicBackgroundScroll.generateScrollList();
        musicBackgroundLabel.setBounds(20, 60, 680, 360);
        editUserFrame.getContentPane().add(musicBackgroundLabel);
        switchingLabel.add(musicBackgroundLabel);

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
                            revalidateMusicBackgroundScroll();
                        } else if (addFile != null && addFile.getName().endsWith(".mp3")) {
                            File Destination = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Music/" + addFile.getName());
                            Files.copy(copyPath, Destination.toPath());
                            revalidateMusicBackgroundScroll();
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
            String element = musicBackgroundScroll.getSelectedElement();

            for (int i = 0 ; i < musicBackgroundNameList.size() ; i++) {
                if (element.equalsIgnoreCase(musicBackgroundNameList.get(i))) {
                    IOUtil.openFile(musicBackgroundList.get(i).getAbsolutePath());
                    break;
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
                if (!musicBackgroundScroll.getSelectedElements().isEmpty()) {
                    String clickedSelection = musicBackgroundScroll.getSelectedElements().get(0);
                    File selectedFile = null;

                    for (int i = 0; i < musicBackgroundNameList.size(); i++) {
                        if (clickedSelection.equals(musicBackgroundNameList.get(i))) {
                            selectedFile = musicBackgroundList.get(i);
                            break;
                        }
                    }

                    if ((AudioPlayer.getCurrentAudio() != null
                            && selectedFile.getAbsoluteFile().toString().equals(AudioPlayer.getCurrentAudio().getAbsoluteFile().toString())) ||
                            selectedFile.getAbsoluteFile().toString().equals(ConsoleFrame.getConsoleFrame()
                                    .getCurrentBackgroundFile().getAbsoluteFile().toString())) {
                        editUserFrame.notify("Cannot rename a file that is in use");
                    } else {
                        String oldname = StringUtil.getFilename(selectedFile);
                        String extension = StringUtil.getExtension(selectedFile);
                        GetterUtil gu = new GetterUtil();
                        gu.setRelativeFrame(editUserFrame);
                        String newname = gu.getString("Rename","Enter any valid file name",
                                "Submit", oldname);

                        if (oldname.equals(newname) || newname.equals("NULL"))
                            return;

                        File renameTo = new File(selectedFile.getParent() + "/" + newname + extension);

                        if (renameTo.exists())
                            throw new IOException("file exists");

                        boolean success = selectedFile.renameTo(renameTo);

                        if (!success) {
                            editUserFrame.notify("Could not rename file at this time");
                        } else {
                            editUserFrame.notify(selectedFile.getName() +
                                    " was successfully renamed to " + renameTo.getName());
                        }

                        revalidateMusicBackgroundScroll();
                        ConsoleFrame.getConsoleFrame().refreshBackgroundIndex();
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
            if (!musicBackgroundScroll.getSelectedElements().isEmpty()) {
                String clickedSelection = musicBackgroundScroll.getSelectedElements().get(0);
                File selectedFile = null;

                for (int i = 0; i < musicBackgroundNameList.size(); i++) {
                    if (clickedSelection.equals(musicBackgroundNameList.get(i))) {
                        selectedFile = musicBackgroundList.get(i);
                        break;
                    }
                }

                if (selectedFile.getAbsolutePath().equalsIgnoreCase(ConsoleFrame.getConsoleFrame()
                        .getCurrentBackgroundFile().getAbsolutePath())) {
                    editUserFrame.notify("Unable to delete the background you are currently using");
                } else if (AudioPlayer.getCurrentAudio() != null &&
                        selectedFile.getAbsolutePath().equalsIgnoreCase(AudioPlayer.getCurrentAudio().getAbsolutePath())) {
                    editUserFrame.notify("Unable to delete the audio you are currently playing");
                } else {
                    selectedFile.delete();

                    revalidateMusicBackgroundScroll();

                    if (StringUtil.getExtension(selectedFile).equals(".mp3"))
                        ConsoleFrame.getConsoleFrame().getInputHandler()
                                .println("Music: " + StringUtil.getFilename(selectedFile) + " successfully deleted.");
                    else if (StringUtil.getExtension(selectedFile).equals(".png")) {
                        ConsoleFrame.getConsoleFrame().getInputHandler()
                                .println("Background: " + StringUtil.getFilename(selectedFile) + " successfully deleted.");

                        ConsoleFrame.getConsoleFrame().refreshBackgroundIndex();
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

    private static void revalidateMusicBackgroundScroll() {
        initMusicBackgroundList();

        musicBackgroundScroll.removeAllElements();
        switchingLabel.remove(musicBackgroundLabel);

        for (int j = 0 ; j < musicBackgroundNameList.size() ; j++) {
            int finalJ = j;
            class thisAction implements CyderScrollList.ScrollAction {
                @Override
                public void fire() {
                    IOUtil.openFile(musicBackgroundList.get(finalJ).getAbsolutePath());
                }
            }

            thisAction action = new thisAction();
            musicBackgroundScroll.addElement(musicBackgroundNameList.get(j), action);
        }

        musicBackgroundLabel = musicBackgroundScroll.generateScrollList();
        musicBackgroundLabel.setBounds(20, 60, 680, 360);
        switchingLabel.add(musicBackgroundLabel);

        switchingLabel.revalidate();
    }

    private static void switchToFontAndColor() {
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
        hexLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ColorConverter.showGUI();
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
        foregroundColorBlock.setBackground(CyderColors.navy);
        foregroundColorBlock.setFocusable(false);
        foregroundColorBlock.setCursor(null);
        foregroundColorBlock.setBackground(ColorUtil.hextorgbColor(UserUtil.getUserData("Foreground")));
        foregroundColorBlock.setToolTipText("Color Preview");
        foregroundColorBlock.setBorder(new LineBorder(CyderColors.navy, 5, false));
        foregroundColorBlock.setBounds(330 + colorOffsetX, 100 + colorOffsetY, 40, 50);
        switchingLabel.add(foregroundColorBlock);

        CyderTextField foregroundField = new CyderTextField(6);
        foregroundField.setRegexMatcher("[A-Fa-f0-9]+");
        foregroundField.setText(UserUtil.getUserData("Foreground"));
        foregroundField.setFont(CyderFonts.weatherFontBig);
        foregroundField.setToolTipText("Hex Value");
        JTextField finalHexField1 = foregroundField;
        foregroundField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    Color c = ColorUtil.hextorgbColor(finalHexField1.getText());
                    foregroundColorBlock.setBackground(c);
                    UserUtil.setUserData("Foreground", foregroundField.getText());
                    Color updateC = ColorUtil.hextorgbColor(foregroundField.getText());

                    ConsoleFrame.getConsoleFrame().getOutputArea().setForeground(updateC);
                    ConsoleFrame.getConsoleFrame().getInputField().setForeground(updateC);
                    ConsoleFrame.getConsoleFrame().getInputField().setCaretColor(updateC);
                    ConsoleFrame.getConsoleFrame().getInputField().setCaret(new CyderCaret(updateC));
                } catch (Exception ignored) {}
            }
        });
        foregroundField.setBounds(100 + colorOffsetX, 100 + colorOffsetY, 220, 50);
        foregroundField.setOpaque(false);
        switchingLabel.add(foregroundField);

        JLabel windowThemeColorLabel = new JLabel("Window Color");
        windowThemeColorLabel.setFont(CyderFonts.weatherFontBig);
        windowThemeColorLabel.setForeground(CyderColors.navy);
        windowThemeColorLabel.setBounds(105 + colorOffsetX, 200, 300, 30);
        switchingLabel.add(windowThemeColorLabel);

        JLabel hexWindowLabel = new JLabel("HEX:");
        hexWindowLabel.setFont(CyderFonts.weatherFontSmall);
        hexWindowLabel.setForeground(CyderColors.navy);
        hexWindowLabel.setBounds(30 + colorOffsetX, 255, 70, 30);
        switchingLabel.add(hexWindowLabel);
        hexWindowLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ColorConverter.showGUI();
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
        windowColorBlock.setBackground(CyderColors.navy);
        windowColorBlock.setFocusable(false);
        windowColorBlock.setCursor(null);
        windowColorBlock.setBackground(ColorUtil.hextorgbColor(UserUtil.getUserData("windowcolor")));
        windowColorBlock.setToolTipText("Color Preview");
        windowColorBlock.setBorder(new LineBorder(CyderColors.navy, 5, false));
        windowColorBlock.setBounds(330 + colorOffsetX, 240 + colorOffsetY, 40, 50);
        switchingLabel.add(windowColorBlock);

        CyderTextField windowField = new CyderTextField(6);
        windowField.setRegexMatcher("[A-Fa-f0-9]+");
        windowField.setText(UserUtil.getUserData("windowcolor"));
        windowField.setFont(CyderFonts.weatherFontBig);
        windowField.setToolTipText("Window border color");
        windowField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    Color c = ColorUtil.hextorgbColor(windowField.getText());
                    windowColorBlock.setBackground(c);
                    UserUtil.setUserData("windowcolor", windowField.getText());

                    CyderColors.setGuiThemeColor(c);

                    for (Frame f : Frame.getFrames()) {
                        if (f instanceof CyderFrame)
                            f.repaint();
                    }
                } catch (Exception ignored) {}
            }
        });
        windowField.setBounds(100 + colorOffsetX, 240 + colorOffsetY, 220, 50);
        windowField.setOpaque(false);
        switchingLabel.add(windowField);

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
        hexLabelFill.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ColorConverter.showGUI();
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
        fillColorBlock.setBackground(CyderColors.navy);
        fillColorBlock.setFocusable(false);
        fillColorBlock.setCursor(null);
        fillColorBlock.setBackground(ColorUtil.hextorgbColor(UserUtil.getUserData("Background")));
        fillColorBlock.setToolTipText("Color Preview");
        fillColorBlock.setBorder(new LineBorder(CyderColors.navy, 5, false));
        fillColorBlock.setBounds(330 + colorOffsetX, 380 + colorOffsetY, 40, 50);
        switchingLabel.add(fillColorBlock);

        CyderTextField fillField = new CyderTextField(6);
        fillField.setRegexMatcher("[A-Fa-f0-9]+");
        fillField.setText(UserUtil.getUserData("Background"));
        fillField.setFont(CyderFonts.weatherFontBig);
        fillField.setToolTipText("Input field and output area fill color if enabled");
        fillField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    fillColorBlock.setBackground(ColorUtil.hextorgbColor(fillField.getText()));
                    UserUtil.setUserData("Background", fillField.getText());

                    if (UserUtil.getUserData("OutputFill").equals("1")) {
                        ConsoleFrame.getConsoleFrame().getOutputArea().setOpaque(true);
                        ConsoleFrame.getConsoleFrame().getOutputArea()
                                .setBackground(ColorUtil.hextorgbColor(UserUtil.getUserData("Background")));
                        ConsoleFrame.getConsoleFrame().getOutputArea().repaint();
                        ConsoleFrame.getConsoleFrame().getOutputArea().revalidate();
                    }

                    //input color fill
                    if (UserUtil.getUserData("InputFill").equals("1")) {
                        ConsoleFrame.getConsoleFrame().getInputField().setOpaque(true);
                        ConsoleFrame.getConsoleFrame().getInputField()
                                .setBackground(ColorUtil.hextorgbColor(UserUtil.getUserData("Background")));
                        ConsoleFrame.getConsoleFrame().getInputField().repaint();
                        ConsoleFrame.getConsoleFrame().getInputField().revalidate();
                    }
                } catch (Exception ignored) {}
            }
        });
        fillField.setBounds(100 + colorOffsetX, 380 + colorOffsetY, 220, 50);
        fillField.setOpaque(false);
        switchingLabel.add(fillField);

        JLabel FontLabel = new JLabel("FONTS", SwingConstants.CENTER);
        FontLabel.setFont(new Font(UserUtil.getUserData("Font"),Font.BOLD, 30));
        FontLabel.setForeground(CyderColors.navy);
        FontLabel.setBounds(50, 60, 300, 30);
        switchingLabel.add(FontLabel);

        CyderScrollList fontScrollList = new CyderScrollList(300, 300, CyderScrollList.SelectionPolicy.SINGLE);
        fontScrollList.setItemAlignemnt(StyleConstants.ALIGN_LEFT);

        AtomicReference<JLabel> fontScrollLabel = new AtomicReference<>(fontScrollList.generateScrollList());

        editUserFrame.notify("Loading fonts...", 2000, Direction.RIGHT);
        new Thread(() -> {
            String[] Fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            Collections.addAll(fontList, Fonts);

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

            fontScrollLabel.set(fontScrollList.generateScrollList());
            fontScrollLabel.get().setBounds(50, 100, 300, 300);
            switchingLabel.add(fontScrollLabel.get());
        },"Preference Font Loader").start();

        CyderButton applyFont = new CyderButton("Apply Font");
        applyFont.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        applyFont.setColors(CyderColors.regularRed);
        applyFont.setToolTipText("Apply");
        applyFont.setFont(CyderFonts.weatherFontSmall);
        applyFont.setFocusPainted(false);
        applyFont.setBackground(CyderColors.regularRed);
        applyFont.addActionListener(e -> {
            if (fontScrollList == null || fontScrollList.getSelectedElements().size() == 0)
                return;

            String selectedFont = fontScrollList.getSelectedElements().get(0);

            if (selectedFont != null) {
                Font ApplyFont = new Font(selectedFont, Font.BOLD, 30);
                ConsoleFrame.getConsoleFrame().getOutputArea().setFont(ApplyFont);
                ConsoleFrame.getConsoleFrame().getInputField().setFont(ApplyFont);
                UserUtil.setUserData("Font", selectedFont);
                ConsoleFrame.getConsoleFrame().getInputHandler().println("The font \"" + selectedFont + "\" has been applied.");
            }
        });
        applyFont.setBounds(50, 410, 140, 40);
        switchingLabel.add(applyFont);

        CyderButton resetValues = new CyderButton("Reset ALL");
        resetValues.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        resetValues.setColors(CyderColors.regularRed);
        resetValues.setToolTipText("Reset font and all colors");
        resetValues.setFont(CyderFonts.weatherFontSmall);
        resetValues.setFocusPainted(false);
        resetValues.setBackground(CyderColors.regularRed);
        resetValues.addActionListener(e -> {
            //foreground
            UserUtil.setUserData("foreground",UserUtil.getDefaultUser().getForeground());
            foregroundColorBlock.setBackground(ColorUtil.hextorgbColor(UserUtil.getDefaultUser().getForeground()));
            foregroundField.setText(UserUtil.getDefaultUser().getForeground());
            ConsoleFrame.getConsoleFrame().getOutputArea().setForeground(ColorUtil.hextorgbColor(UserUtil.getDefaultUser().getForeground()));
            ConsoleFrame.getConsoleFrame().getInputField().setForeground(ColorUtil.hextorgbColor(UserUtil.getDefaultUser().getForeground()));
            ConsoleFrame.getConsoleFrame().getInputField().setCaretColor(ColorUtil.hextorgbColor(UserUtil.getDefaultUser().getForeground()));
            ConsoleFrame.getConsoleFrame().getInputField().setCaret(new CyderCaret(ColorUtil.hextorgbColor(UserUtil.getDefaultUser().getForeground())));

            //font
            UserUtil.setUserData("font",UserUtil.getDefaultUser().getFont());
            Font ApplyFont = new Font(UserUtil.getDefaultUser().getFont(), Font.BOLD, 30);
            ConsoleFrame.getConsoleFrame().getOutputArea().setFont(ApplyFont);
            ConsoleFrame.getConsoleFrame().getInputField().setFont(ApplyFont);
            if (fontScrollList != null)
                fontScrollList.clearSelectedElements();
            FontLabel.setFont(ApplyFont);

            //background
            UserUtil.setUserData("background",UserUtil.getDefaultUser().getBackground());
            fillColorBlock.setBackground(ColorUtil.hextorgbColor(UserUtil.getDefaultUser().getBackground()));
            fillField.setText(UserUtil.getDefaultUser().getBackground());
            if (UserUtil.getUserData("OutputFill").equals("1")) {
                ConsoleFrame.getConsoleFrame().getOutputArea().setOpaque(true);
                ConsoleFrame.getConsoleFrame().getOutputArea()
                        .setBackground(ColorUtil.hextorgbColor(UserUtil.getUserData("Background")));
                ConsoleFrame.getConsoleFrame().getOutputArea().repaint();
                ConsoleFrame.getConsoleFrame().getOutputArea().revalidate();
            }

            //input color fill
            if (UserUtil.getUserData("InputFill").equals("1")) {
                ConsoleFrame.getConsoleFrame().getInputField().setOpaque(true);
                ConsoleFrame.getConsoleFrame().getInputField()
                        .setBackground(ColorUtil.hextorgbColor(UserUtil.getUserData("Background")));
                ConsoleFrame.getConsoleFrame().getInputField().repaint();
                ConsoleFrame.getConsoleFrame().getInputField().revalidate();
            }

            //windowcolor
            UserUtil.setUserData("windowcolor",UserUtil.getDefaultUser().getWindowColor());
            windowColorBlock.setBackground(ColorUtil.hextorgbColor(UserUtil.getDefaultUser().getWindowColor()));
            windowField.setText(UserUtil.getDefaultUser().getWindowColor());
            windowColorBlock.setBackground((ColorUtil.hextorgbColor(UserUtil.getDefaultUser().getWindowColor())));
            CyderColors.setGuiThemeColor((ColorUtil.hextorgbColor(UserUtil.getDefaultUser().getWindowColor())));

            for (Frame f : Frame.getFrames()) {
                if (f instanceof CyderFrame)
                    f.repaint();
            }

            //other defaults colors below

            switchingLabel.revalidate();

            editUserFrame.notify("Default fonts and colors set");
        });
        resetValues.setBounds(50 + 160, 410, 140, 40);
        switchingLabel.add(resetValues);

        switchingLabel.revalidate();
    }

    private static void switchToPreferences() {
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
            if (GenesisShare.getPrefs().get(i).getDisplayName().equals("IGNORE"))
                continue;

            int localIndex = i;

            CyderLabel preferenceLabel = new CyderLabel(GenesisShare.getPrefs().get(i).getDisplayName());
            preferenceLabel.setForeground(CyderColors.navy);
            preferenceLabel.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));
            preferenceLabel.setFont(CyderFonts.defaultFontSmall);
            printingUtil.printlnComponent(preferenceLabel);

            CyderButton preferenceButton = new CyderButton(
                    UserUtil.getUserData(GenesisShare.getPrefs().get(i).getID()).equals("1") ? "      On      " :
                            "      Off      ");
            preferenceButton.setColors(UserUtil.getUserData(GenesisShare.getPrefs().get(i).getID()).equals("1") ? CyderColors.regularGreen : CyderColors.regularRed);
            preferenceButton.setToolTipText(GenesisShare.getPrefs().get(i).getTooltip());
            preferenceButton.addActionListener(e -> {
                boolean wasSelected = UserUtil.getUserData((GenesisShare.getPrefs().get(localIndex).getID())).equalsIgnoreCase("1");
                UserUtil.setUserData(GenesisShare.getPrefs().get(localIndex).getID(), wasSelected ? "0" : "1");

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

    private static void switchToMappingLinks() {
        CyderLabel TitleLabel = new CyderLabel("Map Executables");
        TitleLabel.setBounds(720 / 2 - 375 / 2, 10, 375, 40);
        TitleLabel.setFont(CyderFonts.weatherFontBig);
        switchingLabel.add(TitleLabel);

        CyderLabel add = new CyderLabel("Add Map");
        add.setBounds(110,70,100,40);
        add.setFont(CyderFonts.weatherFontSmall);
        switchingLabel.add(add);

        CyderLabel remove = new CyderLabel("Remove Map");
        remove.setBounds(720 - 110 - 150,70,150,40);
        remove.setFont(CyderFonts.weatherFontSmall);
        switchingLabel.add(remove);

        CyderTextField addField = new CyderTextField(0);

        CyderButton addButton = new CyderButton("Add");
        addButton.setBounds(80,160,200,40);
        addButton.addActionListener(e -> {
            if (addField.getText().trim().length() > 0) {
                if (!addField.getText().trim().contains(",")) {
                    editUserFrame.notify("Invalid exe map format");
                } else {
                    String[] parts = addField.getText().trim().split(",");

                    if (parts.length != 2) {
                        editUserFrame.notify("Too many arguments");
                    } else {
                        String name = parts[0].trim();
                        String path = parts[1].trim();

                        File pointerFile = new File(path);

                        if (!pointerFile.exists() || !pointerFile.isFile()) {
                            editUserFrame.notify("File does not exist or is not a file");
                        } else {
                            if (name.length() > 0) {
                                LinkedList<User.MappedExecutable> exes = UserUtil.extractUser().getExecutables();
                                boolean exists = false;

                                for (User.MappedExecutable exe : exes) {
                                    if (exe.getName().equalsIgnoreCase(name)) {
                                        exists = true;
                                        break;
                                    }
                                }

                                if (exists) {
                                    editUserFrame.notify("Mapped exe name already in use");
                                } else {
                                    User.MappedExecutable addExe = new User.MappedExecutable(name, path);
                                    User user = UserUtil.extractUser();
                                    LinkedList<User.MappedExecutable> currentExes = user.getExecutables();
                                    currentExes.add(addExe);
                                    user.setExecutables(currentExes);
                                    UserUtil.setUserData(user);
                                    editUserFrame.notify("Mapped exe successfully added");
                                    ConsoleFrame.getConsoleFrame().revaliateMenu();
                                }
                            } else {
                                editUserFrame.notify("Invalid exe name");
                            }
                        }
                    }

                    addField.setText("");
                }
            }
        });
        switchingLabel.add(addButton);

        addField.setToolTipText("Add format: \"map name, PATH/TO/EXE OR FILE\"");
        addField.addActionListener(e -> addButton.doClick());
        addField.setBounds(80,110,200,40);
        switchingLabel.add(addField);

        CyderTextField removeField = new CyderTextField(0);

        CyderButton removeButton = new CyderButton("Remove");
        removeButton.setBounds(720 - 80 - 200,160,200,40);
        removeButton.addActionListener(e -> {
            String text = removeField.getText().trim();

            if (text.length() > 0) {
                LinkedList<User.MappedExecutable> exes = UserUtil.extractUser().getExecutables();
                boolean found = false;

                for (User.MappedExecutable exe : exes) {
                    if (exe.getName().equalsIgnoreCase(text)) {
                        found = true;
                        exes.remove(exe);
                        break;
                    }
                }

                if (found) {
                    User user = UserUtil.extractUser();
                    user.setExecutables(exes);
                    UserUtil.setUserData(user);
                    editUserFrame.notify("Exe successfully removed");
                    ConsoleFrame.getConsoleFrame().revaliateMenu();
                } else {
                    editUserFrame.notify("Could not locate specified exe");
                }

                removeField.setText("");
            }
        });
        switchingLabel.add(removeButton);

        removeField.setToolTipText("Name of already mapped executable to remove");
        removeField.addActionListener(e -> removeButton.doClick());
        removeField.setBounds(720 - 80 - 200,110,200,40);
        switchingLabel.add(removeField);

        CyderLabel ffmpegLabel = new CyderLabel("FFMPEG Path");
        ffmpegLabel.setBounds(80,300,200,40);
        ffmpegLabel.setFont(CyderFonts.weatherFontSmall);
        switchingLabel.add(ffmpegLabel);

        CyderLabel youtubedlLabel = new CyderLabel("YouTube-DL Path");
        youtubedlLabel.setBounds(720 - 80 - 200,300,200,40);
        youtubedlLabel.setFont(CyderFonts.weatherFontSmall);
        switchingLabel.add(youtubedlLabel);

        CyderTextField ffmpegField = new CyderTextField(0);
        CyderTextField youtubedlField = new CyderTextField(0);

        CyderButton validateFFMPEG = new CyderButton("Validate");
        validateFFMPEG.setBounds(80,390,200,40);
        validateFFMPEG.addActionListener(e -> {
            String text = ffmpegField.getText().trim();

            if (text.length() > 0) {
                File ffmpegMaybe = new File(text);
                if (ffmpegMaybe.exists() && ffmpegMaybe.isFile() &&
                        StringUtil.getExtension(ffmpegMaybe).equals(".exe")) {
                    User user = UserUtil.extractUser();
                    user.setFfmpegpath(text);
                    UserUtil.setUserData(user);
                    editUserFrame.notify("ffmpeg path sucessfully set");
                } else {
                    editUserFrame.notify("ffmpeg does not exist at the provided path");
                    ffmpegField.setText("");
                }
            }
        });
        switchingLabel.add(validateFFMPEG);

        CyderButton validateYoutubeDL = new CyderButton("Validate");
        validateYoutubeDL.setBounds(720 - 80 - 200,390,200,40);
        validateYoutubeDL.addActionListener(e -> {
            String text = youtubedlField.getText().trim();

            if (text.length() > 0) {
                File youtubeDLMaybe = new File(text);
                if (youtubeDLMaybe.exists() && youtubeDLMaybe.isFile() &&
                        StringUtil.getExtension(youtubeDLMaybe).equals(".exe")) {
                    User user = UserUtil.extractUser();
                    user.setYoutubedlpath(text);
                    UserUtil.setUserData(user);
                    editUserFrame.notify("youtube-dl path sucessfully set");
                } else {
                    editUserFrame.notify("youtube-dl does not exist at the provided path");
                    youtubedlField.setText("");
                }
            }
        });
        switchingLabel.add(validateYoutubeDL);

        ffmpegField.setToolTipText("Path to ffmpeg.exe");
        ffmpegField.addActionListener(e -> validateFFMPEG.doClick());
        ffmpegField.setBounds(80,340,200,40);
        switchingLabel.add(ffmpegField);

        youtubedlField.setToolTipText("Path to YouTube-dl.exe");
        youtubedlField.addActionListener(e -> validateYoutubeDL.doClick());
        youtubedlField.setBounds(720 - 80 - 200,340,200,40);
        switchingLabel.add(youtubedlField);

        ffmpegField.setText(UserUtil.getUserData("ffmpegpath"));
        youtubedlField.setText(UserUtil.getUserData("youtubedlpath"));

        //switchingLabel.add(stuff);
        switchingLabel.revalidate(); //720x500
    }
}
