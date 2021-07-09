package cyder.genesis;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.exception.FatalException;
import cyder.handler.ErrorHandler;
import cyder.handler.PhotoViewer;
import cyder.obj.Preference;
import cyder.threads.CyderThreadFactory;
import cyder.ui.*;
import cyder.utilities.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

public class CyderMain {
    //todo login widget
    private CyderFrame loginFrame;
    private JPasswordField loginField;
    private boolean doLoginAnimations;
    private int loginMode;
    private String username;
    private final String bashString = SystemUtil.getWindowsUsername() + "@Cyder:~$ ";
    private String consoleBashString;

    //todo create user widget
    private CyderFrame createUserFrame;
    private JPasswordField newUserPasswordconf;
    private JPasswordField newUserPassword;
    private JTextField newUserName;
    private CyderButton createNewUser;
    private CyderButton chooseBackground;
    private File createUserBackground;

    //todo pixelating widget
    private File pixelateFile;

    /**
     * start the best program ever made
     * @param CA - the arguments passed in
     */
    public static void main(String[] CA)  {
        new CyderMain(CA);
    }

    /**
     * Shouldn't be entered but once
     * @param CA - Arguments that we are going to log
     */
    private CyderMain(String[] CA) {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "exit-hook"));

        initSystemProperties();
        initUIManager();

        IOUtil.cleanUsers();
        IOUtil.deleteTempDir();
        IOUtil.logArgs(CA);
        IOUtil.cleanErrors();
        IOUtil.cleanSandbox();

        startFinalFrameDisposedChecker();

        if (SecurityUtil.nathanLenovo()) {
            autoCypher();
        } else if (IOUtil.getSystemData("Released").equals("1")) {
            login();
        } else {
            try {
                GenesisShare.getExitingSem().acquire();
                GenesisShare.getExitingSem().release();
                GenesisShare.exit(-600);
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }
    }

    /**
     * Initializes System.getProperty key/value pairs
     */
    private void initSystemProperties() {
        System.setProperty("sun.java2d.uiScale", IOUtil.getSystemData("UISCALE"));
    }

    /**
     * Initializes UIManager.put key/value pairs
     */
    private void initUIManager() {
        UIManager.put("ToolTip.background", CyderColors.tooltipBackgroundColor);
        UIManager.put("ToolTip.border", new BorderUIResource(BorderFactory.createLineBorder(CyderColors.tooltipBorderColor, 2, true)));
        UIManager.put("ToolTip.font", CyderFonts.tahoma.deriveFont(22f));
        UIManager.put("ToolTip.foreground", CyderColors.tooltipForegroundColor);
        UIManager.put("Slider.onlyLeftMouseButtonDrag", Boolean.TRUE);
    }

    /**
     * Used for debugging, automatically logs me in if my account exists,
     * otherwise the program continues as normal
     */
    private void autoCypher() {
        try {
            File autoCypher = new File("../autocypher.txt");
            File Users = new File("users/");

            if (autoCypher.exists() && Users.listFiles().length != 0) {
                BufferedReader ac = new BufferedReader(new FileReader(autoCypher));

                String line = ac.readLine();
                String[] parts = line.split(":");

                if (parts.length == 2 && !parts[0].equals("") && !parts[1].equals("")) {
                    ac.close();
                    recognize(parts[0], parts[1].toCharArray());
                }
            } else
                login();
        } catch (Exception e) {
            ErrorHandler.handle(e);
            login();
        }
    }







    private void startFinalFrameDisposedChecker() {
        Executors.newSingleThreadScheduledExecutor(
                new CyderThreadFactory("Final Frame Disposed Checker")).scheduleAtFixedRate(() -> {
            Frame[] frames = Frame.getFrames();
            int validFrames = 0;

            for (Frame f : frames) {
                if (f.isShowing()) {
                    validFrames++;
                }
            }

            if (validFrames < 1) {
                GenesisShare.exit(120);
            }
        }, 10, 5, SECONDS);
    }


    private LinkedList<String> printingList = new LinkedList<>();
    private LinkedList<String> priorityPrintingList = new LinkedList<>();

    //login widget
    private void loginTypingAnimation(JTextPane refArea) {
        printingList.clear();
        printingList.add("Cyder version: " + IOUtil.getSystemData("Date") + "\n");
        printingList.add("Type \"h\" for a list of valid commands\n");
        printingList.add("Build: Soultree\n");
        printingList.add("Author: Nathan Cheshire\n");
        printingList.add("Design OS: Windows 10+\n");
        printingList.add("Design JVM: 8+\n");
        printingList.add("Description: A programmer's swiss army knife\n");

        int charTimeout = 40;
        int lineTimeout = 500;

        new Thread(() -> {
            StringUtil su = new StringUtil(refArea);

            try {
                while (doLoginAnimations && loginFrame != null)  {
                    if (priorityPrintingList.size() > 0) {
                        String line = priorityPrintingList.removeFirst();

                        for (char c : line.toCharArray()) {
                            su.print(String.valueOf(c));
                            Thread.sleep(charTimeout);
                        }
                    } else if (printingList.size() > 0) {
                        String line = printingList.removeFirst();

                        for (char c : line.toCharArray()) {
                            su.print(String.valueOf(c));
                            Thread.sleep(charTimeout);
                        }
                    }

                    Thread.sleep(lineTimeout);
                }
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"login printing animation").start();

        new Thread(() -> {
            try {
                while (doLoginAnimations && loginFrame != null) {
                    if (loginField.getCaretPosition() < bashString.length()) {
                        loginField.setCaretPosition(loginField.getPassword().length);
                    }

                    //if it doesn't start with bash string, reset it to it
                    if (!String.valueOf(loginField.getPassword()).startsWith(bashString) && loginMode != 2) {
                        loginField.setText(bashString);
                    }

                    Thread.sleep(50);
                }
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"Login Input Caret Position Updater").start();
    }

    //login widget
    public void login() {
        doLoginAnimations = true;
        loginMode = 0;

        if (loginFrame != null)
            loginFrame.closeAnimation();

        IOUtil.cleanUsers();

        loginFrame = new CyderFrame(600, 400) {
            @Override
            public void dispose() {
                doLoginAnimations = false;
                super.dispose();
            }
        };
        loginFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);
        loginFrame.setTitle(IOUtil.getSystemData("Version") + " login");
        loginFrame.setBackground(new Color(21,23,24));

        if (ConsoleFrame.getConsoleFrame().isClosed()) {
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }

        JTextPane loginArea = new JTextPane();
        loginArea.setBounds(20, 40, 560, 280);
        loginArea.setBackground(new Color(21,23,24));
        loginArea.setBorder(null);
        loginArea.setFocusable(false);
        loginArea.setEditable(false);
        loginArea.setFont(new Font("Agency FB",Font.BOLD, 26));
        loginArea.setForeground(new Color(85,181,219));
        loginArea.setCaretColor(loginArea.getForeground());

        CyderScrollPane loginScroll = new CyderScrollPane(loginArea,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        loginScroll.setThumbColor(CyderColors.intellijPink);
        loginScroll.setBounds(20, 40, 560, 280);
        loginScroll.getViewport().setOpaque(false);
        loginScroll.setOpaque(false);
        loginScroll.setBorder(null);
        loginArea.setAutoscrolls(true);

        loginFrame.getContentPane().add(loginScroll);

        loginField = new JPasswordField(20);
        loginField.setEchoChar((char)0);
        loginField.setText(bashString);
        loginField.setBounds(20, 340, 560, 40);
        loginField.setBackground(new Color(21,23,24));
        loginField.setBorder(null);
        loginField.setCaret(new CyderCaret(loginArea.getForeground()));
        loginField.setSelectionColor(CyderColors.selectionColor);
        loginField.setFont(new Font("Agency FB",Font.BOLD, 26));
        loginField.setForeground(new Color(85,181,219));
        loginField.setCaretColor(new Color(85,181,219));
        loginField.addActionListener(e -> loginField.requestFocusInWindow());
        loginField.addKeyListener(new KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
            if (evt.getKeyChar() == KeyEvent.VK_BACK_SPACE && loginMode != 2) {
                if (loginField.getPassword().length < bashString.toCharArray().length) {
                    evt.consume();
                    loginField.setText(bashString);
                }
            }

            else if (evt.getKeyChar() == '\n') {
                char[] input = loginField.getPassword();

                if (loginMode != 2) {
                    char[] newInput = new char[input.length - bashString.toCharArray().length];

                    //copy input to new input with offset
                    if (input.length - bashString.length() >= 0) {
                        System.arraycopy(input, bashString.length(), newInput, 0,
                                input.length - bashString.length());
                    }

                    input = newInput.clone();
                }

                switch (loginMode) {
                    case 0:
                        try {
                            if (Arrays.equals(input,"create".toCharArray())) {
                                createUser();
                                loginField.setText(bashString);
                                loginMode = 0;
                            } else if (Arrays.equals(input,"login".toCharArray())) {
                                loginField.setText(bashString);
                                priorityPrintingList.add("Awaiting Username\n");
                                loginMode = 1;
                            } else if (Arrays.equals(input,"login admin".toCharArray())) {
                                loginField.setText(bashString);
                                priorityPrintingList.add("Feature not yet implemented\n");
                                loginMode = 0;
                            } else if (Arrays.equals(input,"quit".toCharArray())) {
                                loginFrame.closeAnimation();
                            } else if (Arrays.equals(input,"h".toCharArray())) {
                                loginField.setText(bashString);
                                priorityPrintingList.add("Valid commands: create, login, login admin, quit, h\n");
                            } else {
                                loginField.setText(bashString);
                                priorityPrintingList.add("Unknown command; See \"h\" for help\n");
                            }
                        } catch (Exception e) {
                            ErrorHandler.handle(e);
                        }

                        break;
                    case 1:
                        username = new String(input);
                        loginMode = 2;
                        loginField.setEchoChar('*');
                        loginField.setText("");
                        priorityPrintingList.add("Awaiting Password\n");

                        break;

                    case 2:
                        loginField.setEchoChar((char)0);

                        try {
                            Robot rob = new Robot();
                            rob.keyPress(KeyEvent.VK_BACK_SPACE);
                            rob.keyRelease(KeyEvent.VK_BACK_SPACE);
                        } catch (Exception e) {
                            ErrorHandler.handle(e);
                        }

                        recognize(username,input);
                        priorityPrintingList.add("Could not recognize user\n");

                        if (input != null)
                            for (char c: input)
                                c = '\0';

                        loginMode = 0;
                        break;

                    default:
                        loginField.setText(bashString);
                        try {
                            throw new FatalException("Error resulting from login shell");
                        } catch (FatalException e) {
                            ErrorHandler.handle(e);
                        }
                }
            }
            }
        });

        loginField.setCaretPosition(bashString.length());
        loginFrame.getContentPane().add(loginField);

        loginFrame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                loginField.requestFocus();
            }
        });

        File Users = new File("users/");
        String[] directories = Users.list((current, name) -> new File(current, name).isDirectory());

        loginFrame.setVisible(true);
        loginFrame.enterAnimation();

        if (directories != null && directories.length == 0)
            priorityPrintingList.add("No users found; please type \"create\"");

        loginTypingAnimation(loginArea);
    }

    //login widget
    private void recognize(String Username, char[] Password) {
        try {
            if (loginFrame != null) {
                loginField.setEchoChar((char)0);
                loginField.setText(bashString);
            }

            if (SecurityUtil.checkPassword(Username, SecurityUtil.toHexString(SecurityUtil.getSHA(Password)))) {
                doLoginAnimations = false;

                if (!ConsoleFrame.getConsoleFrame().isClosed()) {
                    ConsoleFrame.getConsoleFrame().close();
                }

                ConsoleFrame.getConsoleFrame().start();

                //dispose login frame now to avoid final frame disposed checker seeing that there are no frames
                // and exiting the program when we have just logged in
                if (loginFrame != null) {
                    loginFrame.closeAnimation();
                }

                //this if block needs to be in console, stuff to do specifically for user on first login
                if (IOUtil.getUserData("IntroMusic").equals("1")) {
                    LinkedList<String> musicList = new LinkedList<>();

                    File userMusicDir = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Music");

                    String[] fileNames = userMusicDir.list();

                    if (fileNames != null) {
                        for (String fileName : fileNames) {
                            if (fileName.endsWith(".mp3")) {
                                musicList.add(fileName);
                            }
                        }
                    }

                    //todo does intro music still work?
                    if (!musicList.isEmpty()) {
                        IOUtil.playAudio("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Music/" +
                                (fileNames[NumberUtil.randInt(0, fileNames.length - 1)]),
                                ConsoleFrame.getConsoleFrame().getInputHandler().getOutputArea());
                    } else {
                        IOUtil.playAudio("sys/audio/Ride.mp3",
                                ConsoleFrame.getConsoleFrame().getInputHandler().getOutputArea());
                    }
                }
            } else if (loginFrame != null && loginFrame.isVisible()) {
                loginField.setText("");

                for (char c: Password)
                    c = '\0';
                username = "";

                loginField.requestFocusInWindow();
            }
        } catch (Exception e) {
            ErrorHandler.silentHandle(e);
        }
    }

    //todo everything below here needs to be their own methods: login method, create user method,
    // edit user method

    //Edit user vars
    private CyderFrame editUserFrame;
    private CyderScrollPane musicBackgroundScroll;
    private CyderButton addMusicBackground;
    private CyderButton openMusicBackground;
    private CyderButton deleteMusicBackground;
    private CyderButton renameMusicBackground;
    private JList<?> componentsList;
    private List<String> musicBackgroundNameList;
    private List<File> musicBackgroundList;
    private JList fontList;
    private CyderButton changeUsername;
    private CyderButton changePassword;
    private CyderButton forwardPanel;
    private CyderButton backwardPanel;
    private JLabel switchingLabel;
    private int prefsPanelIndex;

    public void editUser() {
        if (editUserFrame != null)
            editUserFrame.closeAnimation();

        editUserFrame = new CyderFrame(900, 700, CyderImages.defaultBackground);
        editUserFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);
        editUserFrame.setTitle("Edit User");
        editUserFrame.initializeResizing();
        editUserFrame.setMaximumSize(new Dimension(900,700));
        editUserFrame.setResizable(true);

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

        JTextField changeUsernameField = new JTextField(10);
        changeUsernameField.addActionListener(e -> changeUsername.doClick());
        changeUsernameField.setFont(CyderFonts.weatherFontSmall);
        changeUsernameField.setSelectionColor(CyderColors.selectionColor);
        changeUsernameField.setBorder(new LineBorder(CyderColors.navy, 5, false));
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
                //todo test this
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

        JPasswordField changePasswordField = new JPasswordField(10);
        changePasswordField.addActionListener(e -> changePassword.doClick());
        changePasswordField.setFont(CyderFonts.weatherFontSmall);
        changePasswordField.setSelectionColor(CyderColors.selectionColor);
        changePasswordField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        changePasswordField.setToolTipText("New password");
        changePasswordField.setBounds(550, 590, 260, 40);
        editUserFrame.getContentPane().add(changePasswordField);

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

        editUserFrame.enterAnimation();
    }

    public void initMusicBackgroundList() {
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

        JTextField rgbField = new JTextField(CyderColors.navy.getRed() + "," + CyderColors.navy.getGreen() + "," + CyderColors.navy.getBlue());

        JTextField hexField = new JTextField(IOUtil.getUserData("Foreground"));
        hexField.setSelectionColor(CyderColors.selectionColor);
        hexField.setFont(CyderFonts.weatherFontBig);
        hexField.setForeground(CyderColors.navy);
        hexField.setCaretColor(CyderColors.navy);
        hexField.setCaret(new CyderCaret(CyderColors.navy));
        hexField.setToolTipText("Hex Value");
        hexField.setBorder(new LineBorder(CyderColors.navy, 5, false));
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

        rgbField.setSelectionColor(CyderColors.selectionColor);
        rgbField.setFont(CyderFonts.weatherFontBig);
        rgbField.setForeground(CyderColors.navy);
        rgbField.setCaretColor(CyderColors.navy);
        rgbField.setCaret(new CyderCaret(CyderColors.navy));
        rgbField.setToolTipText("RGB Value");
        Color c = ColorUtil.hextorgbColor(IOUtil.getUserData("Foreground"));
        rgbField.setText(c.getRed() + "," + c.getGreen() + "," + c.getBlue());
        rgbField.setBorder(new LineBorder(CyderColors.navy, 5, false));
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
            IOUtil.writeUserData("Foreground", hexField.getText());
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

        JTextField hexFieldFill = new JTextField(String.format("#%02X%02X%02X", CyderColors.navy.getRed(),
                CyderColors.navy.getGreen(), CyderColors.navy.getBlue()).replace("#", ""));

        hexFieldFill.setText(IOUtil.getUserData("Background"));
        hexFieldFill.setSelectionColor(CyderColors.selectionColor);
        hexFieldFill.setFont(CyderFonts.weatherFontBig);
        hexFieldFill.setForeground(CyderColors.navy);
        hexFieldFill.setCaretColor(CyderColors.navy);
        hexFieldFill.setCaret(new CyderCaret(CyderColors.navy));
        hexFieldFill.setToolTipText("Input field and output area fill color if enabled");
        hexFieldFill.setBorder(new LineBorder(CyderColors.navy, 5, false));
        hexFieldFill.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
            try {
                colorBlockFill.setBackground(ColorUtil.hextorgbColor(hexFieldFill.getText()));
                IOUtil.writeUserData("Background", hexFieldFill.getText());
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

        String[] Fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        fontList = new JList(Fonts);
        fontList.setSelectionBackground(CyderColors.selectionColor);
        fontList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        fontList.setFont(CyderFonts.weatherFontSmall);

        for (int i = 0 ; i < Fonts.length ; i++)
            if (Fonts[i].equals(IOUtil.getUserData("Font")))
                fontList.setSelectedIndex(i);


        CyderScrollPane FontListScroll = new CyderScrollPane(fontList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        FontListScroll.setThumbColor(CyderColors.intellijPink);
        FontListScroll.setBorder(new LineBorder(CyderColors.navy, 5, false));

        CyderButton applyFont = new CyderButton("Apply Font");
        applyFont.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        applyFont.setColors(CyderColors.regularRed);
        applyFont.setToolTipText("Apply");
        applyFont.setFont(CyderFonts.weatherFontSmall);
        applyFont.setFocusPainted(false);
        applyFont.setBackground(CyderColors.regularRed);
        applyFont.addActionListener(e -> {
            String FontS = (String) fontList.getSelectedValue();

            if (FontS != null) {
                //todo does stuff like this work?
                Font ApplyFont = new Font(FontS, Font.BOLD, 30);
                ConsoleFrame.getConsoleFrame().getOutputArea().setFont(ApplyFont);
                ConsoleFrame.getConsoleFrame().getInputField().setFont(ApplyFont);
                IOUtil.writeUserData("Font", FontS);
                ConsoleFrame.getConsoleFrame().getInputHandler().println("The font \"" + FontS + "\" has been applied.");
            }
        });
        applyFont.setBounds(100, 420, 200, 40);
        switchingLabel.add(applyFont);

        fontList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                applyFont.doClick();
            } else {
                try {
                    FontLabel.setFont(new Font(fontList.getSelectedValue().toString(), Font.BOLD, 30));
                } catch (Exception ex) {
                    ErrorHandler.handle(ex);
                }
            }
            }
        });

        fontList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            JList t = (JList) e.getSource();
            int index = t.locationToIndex(e.getPoint());

            FontLabel.setFont(new Font(t.getModel().getElementAt(index).toString(), Font.BOLD, 30));
            }
        });

        FontListScroll.setBounds(50, 100, 300, 300);
        switchingLabel.add(FontListScroll, Component.CENTER_ALIGNMENT);

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
                IOUtil.writeUserData(GenesisShare.getPrefs().get(localIndex).getID(), wasSelected ? "0" : "1");

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



    //CreateUser class in genesis
    public void createUser() {
        createUserBackground = null;

        if (createUserFrame != null)
            createUserFrame.closeAnimation();

        createUserFrame = new CyderFrame(356, 473, CyderImages.defaultBackground);
        createUserFrame.setTitle("Create User");

        JLabel NameLabel = new JLabel("Username: ", SwingConstants.CENTER);
        NameLabel.setFont(CyderFonts.weatherFontSmall);
        NameLabel.setBounds(120, 30, 121, 30);
        createUserFrame.getContentPane().add(NameLabel);

        newUserName = new JTextField(15);
        newUserName.setSelectionColor(CyderColors.selectionColor);
        newUserName.setFont(CyderFonts.weatherFontSmall);
        newUserName.setForeground(CyderColors.navy);
        newUserName.setCaretColor(CyderColors.navy);
        newUserName.setCaret(new CyderCaret(CyderColors.navy));
        newUserName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if (newUserName.getText().length() > 15) {
                    evt.consume();
                }
            }
        });

        newUserName.setBorder(new LineBorder(new Color(0, 0, 0)));
        newUserName.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");

                if (newUserName.getText().length() == 1) {
                    newUserName.setText(newUserName.getText().toUpperCase());
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");

                if (newUserName.getText().length() == 1) {
                    newUserName.setText(newUserName.getText().toUpperCase());
                }
            }

            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");

                if (newUserName.getText().length() == 1) {
                    newUserName.setText(newUserName.getText().toUpperCase());
                }
            }
        });

        newUserName.setBorder(new LineBorder(CyderColors.navy, 5, false));
        newUserName.setBounds(60, 70, 240, 40);
        createUserFrame.getContentPane().add(newUserName);

        JLabel passwordLabel = new JLabel("Password: ", SwingConstants.CENTER);
        passwordLabel.setFont(CyderFonts.weatherFontSmall);
        passwordLabel.setForeground(CyderColors.navy);
        passwordLabel.setBounds(60, 120, 240, 30);
        createUserFrame.getContentPane().add(passwordLabel);

        JLabel matchPasswords = new JLabel("Passwords match", SwingConstants.CENTER);

        newUserPassword = new JPasswordField(15);
        newUserPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordconf.getPassword())) {
                matchPasswords.setText("Passwords match");
                matchPasswords.setForeground(CyderColors.regularGreen);
            } else {
                matchPasswords.setText("Passwords do not match");
                matchPasswords.setForeground(CyderColors.regularRed);
            }
            }
        });
        newUserPassword.setBorder(new LineBorder(CyderColors.navy, 5, false));
        newUserPassword.setBounds(60, 160, 240, 40);
        newUserPassword.setSelectionColor(CyderColors.selectionColor);
        newUserPassword.setFont(new Font("Agency FB",Font.BOLD, 20));
        newUserPassword.setForeground(CyderColors.navy);
        newUserPassword.setCaretColor(CyderColors.navy);
        newUserPassword.setCaret(new CyderCaret(CyderColors.navy));

        createUserFrame.getContentPane().add(newUserPassword);

        JLabel passwordLabelConf = new JLabel("Confirm Password: ", SwingConstants.CENTER);
        passwordLabelConf.setFont(CyderFonts.weatherFontSmall);
        passwordLabelConf.setForeground(CyderColors.navy);
        passwordLabelConf.setBounds(60, 210, 240, 30);
        createUserFrame.getContentPane().add(passwordLabelConf);

        newUserPasswordconf = new JPasswordField(15);
        newUserPasswordconf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordconf.getPassword())) {
                matchPasswords.setText("Passwords match");
                matchPasswords.setForeground(CyderColors.regularGreen);
            } else {
                matchPasswords.setText("Passwords do not match");
                matchPasswords.setForeground(CyderColors.regularRed);
            }
            }
        });

        newUserPasswordconf.setBorder(new LineBorder(CyderColors.navy, 5, false));
        newUserPasswordconf.setSelectionColor(CyderColors.selectionColor);
        newUserPasswordconf.setFont(new Font("Agency FB",Font.BOLD, 20));
        newUserPasswordconf.setForeground(CyderColors.navy);
        newUserPasswordconf.setCaretColor(CyderColors.navy);
        newUserPasswordconf.setCaret(new CyderCaret(CyderColors.navy));
        newUserPasswordconf.setBounds(60, 250, 240, 40);
        createUserFrame.getContentPane().add(newUserPasswordconf);

        matchPasswords.setFont(CyderFonts.weatherFontSmall);
        matchPasswords.setForeground(CyderColors.regularGreen);
        matchPasswords.setBounds(32, 300, 300, 30);
        createUserFrame.getContentPane().add(matchPasswords);

        chooseBackground = new CyderButton("Choose background");
        chooseBackground.setToolTipText("ClickMe me to choose a background");
        chooseBackground.setFont(CyderFonts.weatherFontSmall);
        chooseBackground.setBackground(CyderColors.regularRed);
        chooseBackground.setColors(CyderColors.regularRed);
        chooseBackground.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    new Thread(() -> {
                      try {
                          File temp = new GetterUtil().getFile("Choose new user's background file");
                          if (temp != null) {
                              createUserBackground = temp;
                          }

                          if (temp != null && !Files.probeContentType(Paths.get(createUserBackground.getAbsolutePath())).endsWith("png")) {
                              createUserBackground = null;
                          }
                      } catch (Exception ex) {
                          ErrorHandler.handle(ex);
                      }
                   }, "wait thread for GetterUtil().getFile()").start();
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                try {
                    if (createUserBackground != null) {
                        chooseBackground.setText(createUserBackground.getName());
                    } else {
                        chooseBackground.setToolTipText("No File Chosen");
                    }
                } catch (Exception ex) {
                    ErrorHandler.handle(ex);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                chooseBackground.setToolTipText("Choose background");
            }
        });

        chooseBackground.setBorder(new LineBorder(CyderColors.navy, 5, false));
        chooseBackground.setBounds(60, 340, 240, 40);
        createUserFrame.getContentPane().add(chooseBackground);

        createNewUser = new CyderButton("Create User");
        createNewUser.setFont(CyderFonts.weatherFontSmall);
        createNewUser.setBackground(CyderColors.regularRed);
        createNewUser.setColors(CyderColors.regularRed);
        createNewUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    String uuid = SecurityUtil.generateUUID();
                    File folder = new File("users/" + uuid);

                    while (folder.exists()) {
                        uuid = SecurityUtil.generateUUID();
                        folder = new File("users/" + uuid);
                    }

                    char[] pass = newUserPassword.getPassword();
                    char[] passconf = newUserPasswordconf.getPassword();

                    if (StringUtil.empytStr(newUserName.getText()) || pass == null || passconf == null
                            || uuid.equals("") || pass.equals("") || passconf.equals("") || uuid.length() == 0) {
                        createUserFrame.inform("Sorry, but one of the required fields was left blank.\nPlease try again.", "");
                        newUserPassword.setText("");
                        newUserPasswordconf.setText("");
                    } else if (!Arrays.equals(pass, passconf) && pass.length > 0) {
                        createUserFrame.inform("Sorry, but your passwords did not match. Please try again.", "");
                        newUserPassword.setText("");
                        newUserPasswordconf.setText("");
                    } else if (pass.length < 5) {
                        createUserFrame.inform("Sorry, but your password length should be greater than\n"
                                + "four characters for security reasons. Please add more characters.", "");

                        newUserPassword.setText("");
                        newUserPasswordconf.setText("");
                    } else {
                        if (createUserBackground == null) {
                            Image img = CyderImages.defaultBackground.getImage();

                            BufferedImage bi = new BufferedImage(img.getWidth(null),
                                    img.getHeight(null),BufferedImage.TYPE_INT_RGB);

                            Graphics2D g2 = bi.createGraphics();
                            g2.drawImage(img, 0, 0, null);
                            g2.dispose();
                            ImageIO.write(bi, "png", new File("users/" + uuid + "/Backgrounds/Default.png"));
                        }

                        File NewUserFolder = new File("users/" + uuid);
                        File backgrounds = new File("users/" + uuid + "/Backgrounds");
                        File music = new File("users/" + uuid + "/Music");
                        File notes = new File("users/" + uuid + "/Notes");

                        NewUserFolder.mkdirs();
                        backgrounds.mkdir();
                        music.mkdir();
                        notes.mkdir();

                        if (createUserBackground != null) {
                            ImageIO.write(ImageIO.read(createUserBackground), "png",
                                    new File("users/" + uuid + "/Backgrounds/" + createUserBackground.getName()));
                        }

                        //todo this will use binary writing when we switch
                        // so we'll change to .bin and such
                        BufferedWriter newUserWriter = new BufferedWriter(new FileWriter(
                                "users/" + uuid + "/Userdata.txt"));

                        LinkedList<String> data = new LinkedList<>();
                        data.add("Name:" + newUserName.getText().trim());
                        data.add("Password:" + SecurityUtil.toHexString(SecurityUtil.getSHA(pass)));

                        for (Preference pref : GenesisShare.getPrefs()) {
                            data.add(pref.getID() + ":" + pref.getDefaultValue());
                        }

                        for (String d : data) {
                            newUserWriter.write(d);
                            newUserWriter.newLine();
                        }

                        newUserWriter.close();

                        createUserFrame.closeAnimation();
                        createUserFrame.inform("The new user \"" + newUserName.getText().trim() + "\" has been created successfully.", "");
                        createUserFrame.closeAnimation();

                        //attempt to log in new user if it's the only user
                        if (new File("users/").length() == 1) {
                            loginFrame.closeAnimation();
                            recognize(newUserName.getText().trim(), pass);
                        }
                    }

                    for (char c : pass)
                        c = '\0';

                    for (char c : passconf)
                        c = '\0';

                } catch (Exception ex) {
                    ErrorHandler.handle(ex);
                }
            }
        });

        createNewUser.setBorder(new LineBorder(CyderColors.navy, 5, false));
        createNewUser.setFont(CyderFonts.weatherFontSmall);
        createNewUser.setBounds(60, 390, 240, 40);
        createUserFrame.getContentPane().add(createNewUser);

        if (!ConsoleFrame.getConsoleFrame().isClosed()) {
            ConsoleFrame.getConsoleFrame().setFrameRelativeTo(createUserFrame);
        } else if (loginFrame != null && loginFrame.isActive() && loginFrame.isVisible()) {
            createUserFrame.setLocationRelativeTo(loginFrame);
        } else {
            createUserFrame.setLocationRelativeTo(null);
        }

        createUserFrame.setVisible(true);
        newUserName.requestFocus();
    }

    /**
     * This is called from the shutdown hook, things imperitive to do
     * no matter what, before we close, System.exit has already been called here
     * so you shouldn't do any reading or writing to files or anything with locks/semaphores
     */
    private void shutdown() {
        //delete temp dir
        IOUtil.deleteTempDir();
    }
}