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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
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

    private static CyderButton changePassword;

    private static CyderButton forwardPanel;
    private static CyderButton backwardPanel;

    private static JLabel switchingLabel;
    private static int prefsPanelIndex;

    private UserEditor() {}

    public static void showGUI(int startingIndex) {
        if (editUserFrame != null)
            editUserFrame.dispose();

        editUserFrame = new CyderFrame(900, 620, CyderImages.defaultBackground);
        editUserFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);
        editUserFrame.setTitle("Edit User");
        editUserFrame.initializeResizing();
        editUserFrame.setMaximumSize(new Dimension(900,620));
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
                switchToFieldInputs();
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

        //todo make universal color for button to be changed too
        // (in preparation for dark mode, white buttons and gray backgrounds) wih black frame borders

        //todo update to java 16 if possible? why is it so hard?

        //todo notification single lines need to be smaller

        //todo add more notification directions, bottom left, bottom right, center left, center right
        //todo notification enum needed for this

        forwardPanel = new CyderButton(">");
        forwardPanel.setBackground(CyderColors.regularRed);
        forwardPanel.setColors(CyderColors.regularRed);
        forwardPanel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        forwardPanel.setFont(CyderFonts.weatherFontSmall);
        forwardPanel.addActionListener(e -> nextEditUser());
        forwardPanel.setBounds(830, 260, 50, 120);
        editUserFrame.getContentPane().add(forwardPanel);

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
                switchToFieldInputs();
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
                switchToFieldInputs();
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

            if (prefsPanelIndex == 1) {
                fontScrollLabel.get().setBounds(50, 100, 300, 300);
                switchingLabel.add(fontScrollLabel.get());
            }
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

            //reset font scroll position
            fontScrollList.getScrollPane().getVerticalScrollBar().setValue(
                    fontScrollList.getScrollPane().getVerticalScrollBar().getMinimum());

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

            //separate the components
            printingUtil.print("\n");

            //check boxes to toggle preferences
            JLabel togglePrefLabel = new JLabel("<html>SEPARATOR<br/>SEPARATOR<br/>SEPARATOR<br/>SEPARATOR</html>") {
                @Override
                public void paintComponent(Graphics g) {
                    boolean setSelected = UserUtil.getUserData(
                            (GenesisShare.getPrefs().get(localIndex).getID())).equalsIgnoreCase("1");

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

                        graphics2D.setColor(CyderColors.intellijPink);

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
                    //clicks make it here, but not toggled properly and not updated in menu

                    String localID = GenesisShare.getPrefs().get(localIndex).getID();

                    boolean wasSelected = UserUtil.getUserData(localID).equalsIgnoreCase("1");
                    UserUtil.setUserData(localID, wasSelected ? "0" : "1");

                    ConsoleFrame.getConsoleFrame().refreshBasedOnPrefs();
                    togglePrefLabel.repaint();
                }
            });
            togglePrefLabel.setToolTipText(GenesisShare.getPrefs().get(localIndex).getTooltip());
            printingUtil.printlnComponent(togglePrefLabel);
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

    private static void switchToFieldInputs() {
        JTextPane fieldInputsPane = new JTextPane();
        fieldInputsPane.setEditable(false);
        fieldInputsPane.setAutoscrolls(false);
        fieldInputsPane.setBounds(0, 0, 720, 500);
        fieldInputsPane.setFocusable(true);
        fieldInputsPane.setOpaque(false);
        fieldInputsPane.setBackground(Color.white);

        //adding components
        StringUtil printingUtil = new StringUtil(fieldInputsPane);

        //print pairs here
        CyderLabel prefsTitle = new CyderLabel("Field Inputs");
        prefsTitle.setFont(CyderFonts.weatherFontBig);
        printingUtil.printlnComponent(prefsTitle);

        printingUtil.print("\n\n");

        CyderLabel changeUsernameLabel = new CyderLabel("Change username");
        printingUtil.printlnComponent(changeUsernameLabel);

        printingUtil.print("\n");

        CyderButton changeUsernameButton = new CyderButton("   Change Username   ");
        JTextField changeUsernameField = new JTextField(0);
        changeUsernameField.addActionListener(e -> changeUsernameButton.doClick());
        changeUsernameField.setToolTipText("Change account username to a valid alternative");
        changeUsernameField.setBackground(CyderColors.vanila);
        changeUsernameField.setSelectionColor(CyderColors.selectionColor);
        changeUsernameField.setFont(new Font("Agency FB",Font.BOLD, 20));
        changeUsernameField.setForeground(CyderColors.navy);
        changeUsernameField.setCaretColor(CyderColors.navy);
        changeUsernameField.setCaret(new CyderCaret(CyderColors.navy));
        changeUsernameField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        changeUsernameField.setOpaque(true);
        CyderTextField.addAutoCapitalizationAdapter(changeUsernameField);
        printingUtil.printlnComponent(changeUsernameField);
        changeUsernameField.setText(UserUtil.extractUser().getName());

        printingUtil.print("\n");

        changeUsernameButton.setBackground(CyderColors.regularRed);
        changeUsernameButton.setColors(CyderColors.regularRed);
        changeUsernameButton.setBorder(new LineBorder(CyderColors.navy, 5, false));
        changeUsernameButton.setFont(CyderFonts.weatherFontSmall);
        changeUsernameButton.addActionListener(e -> {
            String newUsername = changeUsernameField.getText();
            if (!StringUtil.empytStr(newUsername) && !newUsername.equalsIgnoreCase(ConsoleFrame.getConsoleFrame().getUsername())) {
                IOUtil.changeUsername(newUsername);
                editUserFrame.notify("Username successfully changed to \"" + newUsername + "\"");
                ConsoleFrame.getConsoleFrame().setTitle(IOUtil.getSystemData().getVersion() + " Cyder [" + newUsername + "]");
                changeUsernameField.setText(UserUtil.extractUser().getName());
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

        changePasswordConfField.addActionListener(e -> changePassword.doClick());
        changePasswordConfField.setFont(changePasswordField.getFont());
        changePasswordConfField.setToolTipText("New Password Confirmation");
        printingUtil.printlnComponent(changePasswordConfField);

        printingUtil.print("\n");

        changePassword = new CyderButton("    Change Password    ");
        changePassword.addActionListener(e -> {
            char[] newPassword = changePasswordField.getPassword();
            char[] newPasswordConf = changePasswordConfField.getPassword();

            if (newPassword.length > 4) {
                if (!Arrays.equals(newPasswordConf,newPassword)) {
                    editUserFrame.notify("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", " +
                            "but your provided passwords were not equal");
                    changePasswordField.setText("");
                    changePasswordConfField.setText("");
                } else {
                    IOUtil.changePassword(newPassword);
                    editUserFrame.notify("Password successfully changed");
                }
            } else {
                editUserFrame.notify("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", " +
                        "but your password must be greater than 4 characters for security reasons.");
            }

            changePasswordField.setText("");
            changePasswordConfField.setText("");

            for (char c : newPassword) {
                c = '\0';
            }
        });
        printingUtil.printlnComponent(changePassword);

        printingUtil.print("\n\n");

        CyderLabel consoleDatePatternLabel = new CyderLabel("ConsoleClock Date Pattern");
        printingUtil.printlnComponent(consoleDatePatternLabel);

        printingUtil.print("\n");

        CyderButton validateDatePatternButton = new CyderButton("   Validate   ");
        JTextField consoleDatePatternField = new JTextField(0);
        consoleDatePatternField.addActionListener(e -> validateDatePatternButton.doClick());
        consoleDatePatternLabel.setToolTipText("Java date/time pattern to use for the console clock");
        consoleDatePatternField.setBackground(CyderColors.vanila);
        consoleDatePatternField.setSelectionColor(CyderColors.selectionColor);
        consoleDatePatternField.setFont(CyderFonts.weatherFontSmall);
        consoleDatePatternField.setForeground(CyderColors.navy);
        consoleDatePatternField.setCaretColor(CyderColors.navy);
        consoleDatePatternField.setCaret(new CyderCaret(CyderColors.navy));
        consoleDatePatternField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        consoleDatePatternField.setOpaque(true);
        printingUtil.printlnComponent(consoleDatePatternField);
        consoleDatePatternField.setText(UserUtil.extractUser().getConsoleclockformat());

        printingUtil.print("\n");

        validateDatePatternButton.addActionListener(e -> {
            String fieldText = consoleDatePatternField.getText().trim();

            try {
                Date Time = new Date();
                SimpleDateFormat dateFormatter = new SimpleDateFormat(fieldText);
                String formatted =  dateFormatter.format(Time);

                //valid so write and refresh consoleclock
                UserUtil.setUserData("consoleclockformat",fieldText);
                ConsoleFrame.getConsoleFrame().refreshClockText();
            } catch (Exception ex) {
                ErrorHandler.silentHandle(ex);
            }
        });
        printingUtil.printlnComponent(validateDatePatternButton);

        printingUtil.print("\n\n");

        CyderLabel addMap = new CyderLabel("Add Mapped Execuatable");
        printingUtil.printlnComponent(addMap);

        printingUtil.print("\n");

        CyderButton addMapButton = new CyderButton("    Add Map    ");
        JTextField addMapField = new JTextField(0);
        addMapField.addActionListener(e -> addMapButton.doClick());
        addMapField.setToolTipText("Add format: \"map name, PATH/TO/EXE OR FILE\"");
        addMapField.setBackground(CyderColors.vanila);
        addMapField.setSelectionColor(CyderColors.selectionColor);
        addMapField.setFont(CyderFonts.weatherFontSmall);
        addMapField.setForeground(CyderColors.navy);
        addMapField.setCaretColor(CyderColors.navy);
        addMapField.setCaret(new CyderCaret(CyderColors.navy));
        addMapField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        addMapField.setOpaque(true);
        printingUtil.printlnComponent(addMapField);

        printingUtil.print("\n");

        addMapButton.addActionListener(e -> {
            if (addMapField.getText().trim().length() > 0) {
                if (!addMapField.getText().trim().contains(",")) {
                    editUserFrame.notify("Invalid exe map format");
                } else {
                    String[] parts = addMapField.getText().trim().split(",");

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
        removeMapField.addActionListener(e -> removeMapButton.doClick());
        removeMapField.setToolTipText("Name of already mapped executable to remove");
        removeMapField.setBackground(CyderColors.vanila);
        removeMapField.setSelectionColor(CyderColors.selectionColor);
        removeMapField.setFont(CyderFonts.weatherFontSmall);
        removeMapField.setForeground(CyderColors.navy);
        removeMapField.setCaretColor(CyderColors.navy);
        removeMapField.setCaret(new CyderCaret(CyderColors.navy));
        removeMapField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        removeMapField.setOpaque(true);
        printingUtil.printlnComponent(removeMapField);

        printingUtil.print("\n");

        removeMapButton.addActionListener(e -> {
            String text = removeMapField.getText().trim();

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
        ffmpegField.addActionListener(e -> validateFfmpegButton.doClick());
        ffmpegField.setToolTipText("Path to ffmpeg.exe");
        ffmpegField.setBackground(CyderColors.vanila);
        ffmpegField.setSelectionColor(CyderColors.selectionColor);
        ffmpegField.setFont(CyderFonts.weatherFontSmall);
        ffmpegField.setForeground(CyderColors.navy);
        ffmpegField.setCaretColor(CyderColors.navy);
        ffmpegField.setCaret(new CyderCaret(CyderColors.navy));
        ffmpegField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        ffmpegField.setOpaque(true);
        printingUtil.printlnComponent(ffmpegField);
        ffmpegField.setText(UserUtil.getUserData("ffmpegpath"));

        printingUtil.print("\n");

        validateFfmpegButton.addActionListener(e -> {
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
                    ffmpegField.setText(UserUtil.getUserData("ffmpegpath"));
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
        youtubedlField.addActionListener(e -> validateYouTubeDL.doClick());
        youtubedlField.setToolTipText("Path to youtubedl.exe");
        youtubedlField.setBackground(CyderColors.vanila);
        youtubedlField.setSelectionColor(CyderColors.selectionColor);
        youtubedlField.setFont(CyderFonts.weatherFontSmall);
        youtubedlField.setForeground(CyderColors.navy);
        youtubedlField.setCaretColor(CyderColors.navy);
        youtubedlField.setCaret(new CyderCaret(CyderColors.navy));
        youtubedlField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        youtubedlField.setOpaque(true);
        printingUtil.printlnComponent(youtubedlField);
        youtubedlField.setText(UserUtil.getUserData("youtubedlpath"));

        printingUtil.print("\n");

        validateYouTubeDL.addActionListener(e -> {
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
                    youtubedlField.setText(UserUtil.getUserData("youtubedlpath"));
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

            if (!SecurityUtil.toHexString(SecurityUtil.getSHA256(hashed.toCharArray())).equals(UserUtil.extractUser().getPass())) {
                editUserFrame.notify("Sorry, but the password you entered was incorrect; user not deleted");
                deletePasswordField.setText("");
                return;
            }

            ConsoleFrame.getConsoleFrame().close();
            SystemUtil.deleteFolder(new File("users/" + ConsoleFrame.getConsoleFrame().getUUID()));

            String dep = SecurityUtil.getDeprecatedUUID();

            File renamed = new File("users/" + dep);
            while (renamed.exists()) {
                dep = SecurityUtil.getDeprecatedUUID();
                renamed = new File("users/" + dep);
            }

            File old = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID());
            old.renameTo(renamed);

            Frame[] frames = Frame.getFrames();

            for (Frame f : frames)
                f.dispose();

            GenesisShare.exit(-56);
        });
        printingUtil.printlnComponent(deleteUserButton);

        //more labels, fields, and if applicable, validation buttons here

        CyderScrollPane fieldInputsScroll = new CyderScrollPane(fieldInputsPane);
        fieldInputsScroll.setThumbSize(7);
        fieldInputsScroll.getViewport().setOpaque(false);
        fieldInputsScroll.setFocusable(true);
        fieldInputsScroll.setOpaque(false);
        fieldInputsScroll.setThumbColor(CyderColors.intellijPink);
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
