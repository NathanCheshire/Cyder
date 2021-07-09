package cyder.genesis;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.exception.FatalException;
import cyder.handler.ErrorHandler;
import cyder.obj.Preference;
import cyder.threads.CyderThreadFactory;
import cyder.ui.*;
import cyder.utilities.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
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

                for (char c: Password) {
                    c = '\0';
                }

                username = "";

                loginField.requestFocusInWindow();
            }
        } catch (Exception e) {
            ErrorHandler.silentHandle(e);
        }
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