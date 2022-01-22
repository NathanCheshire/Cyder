package cyder.handlers.internal;

import cyder.consts.CyderColors;
import cyder.consts.CyderStrings;
import cyder.genesis.CyderSplash;
import cyder.genesis.GenesisShare;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderCaret;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollPane;
import cyder.cyderuser.UserCreator;
import cyder.utilities.*;
import cyder.utilities.IOUtil.SystemData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class LoginHandler {
    private LoginHandler() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    //Login widget --------------------------------------------------------------------

    private static CyderFrame loginFrame;
    private static JPasswordField loginField;
    private static boolean doLoginAnimations;
    private static int loginMode;
    private static String username;
    private static final String bashString = SystemUtil.getWindowsUsername() + "@" + SystemUtil.getComputerName() + ":~$ ";
    private static String consoleBashString;
    private static boolean closed = true;

    private static boolean autoCypherAttempt;

    private static LinkedList<String> printingList = new LinkedList<>();
    private static LinkedList<String> priorityPrintingList = new LinkedList<>();

    private static Semaphore printingSem = new Semaphore(1);

    private static void loginTypingAnimation(JTextPane refArea) {
        //apparently we need it a second time to fix a bug :/
        doLoginAnimations = true;

        printingList.clear();
        printingList.add("Cyder version: " + IOUtil.getSystemData().getReleasedate() + "\n");
        printingList.add("Type \"help\" for a list of valid commands\n");
        printingList.add("Build: " + IOUtil.getSystemData().getVersion() +"\n");
        printingList.add("Author: Nathan Cheshire\n");
        printingList.add("Design OS: Windows 10+\n");
        printingList.add("Design JVM: 8+\n");
        printingList.add("Description: A programmer's swiss army knife\n");

        final int charTimeout = 25;
        final int lineTimeout = 400;

        new Thread(() -> {
            StringUtil su = new StringUtil(refArea);

            try {
                while (doLoginAnimations && loginFrame != null)  {
                    if (priorityPrintingList.size() > 0) {
                        printingSem.acquire();

                        String line = priorityPrintingList.removeFirst();

                        for (char c : line.toCharArray()) {
                            su.print(String.valueOf(c));
                            Thread.sleep(charTimeout);
                        }

                        printingSem.release();
                    } else if (printingList.size() > 0) {
                        printingSem.acquire();

                        String line = printingList.removeFirst();

                        for (char c : line.toCharArray()) {
                            su.print(String.valueOf(c));
                            Thread.sleep(charTimeout);
                        }

                        printingSem.release();
                    }

                    Thread.sleep(lineTimeout);
                }
            }

            catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        },"Login printing animation").start();

        new Thread(() -> {
            try {
                while (doLoginAnimations && loginFrame != null) {
                    //reset caret pos
                    if (loginField.getCaretPosition() < bashString.length()) {
                        loginField.setCaretPosition(loginField.getPassword().length);
                    }

                    //if it doesn't start with bash string, reset it to start with bashString if it's not mode 2
                    if (loginMode != 2 && !String.valueOf(loginField.getPassword()).startsWith(bashString)) {
                        loginField.setText(bashString + String.valueOf(loginField.getPassword()).replace(bashString, "").trim());
                        loginField.setCaretPosition(loginField.getPassword().length);
                    }

                    Thread.sleep(50);
                }
            }

            catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        },"Login Input Caret Position Updater").start();
    }

    public static void showGUI() {
        SessionHandler.log(SessionHandler.Tag.WIDGET_OPENED, "LOGIN");

        priorityPrintingList.clear();
        printingList.clear();
        doLoginAnimations = true;
        loginMode = 0;

        if (loginFrame != null) {
            loginFrame.removePostCloseActions();
            loginFrame.dispose(true);
        }

        loginFrame = new CyderFrame(600, 400,
                ImageUtil.imageIconFromColor(new Color(21,23,24))) {
            @Override
            public void dispose() {
                doLoginAnimations = false;
                super.dispose();
            }
        };
        loginFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);
        loginFrame.setTitle("Cyder Login [" + IOUtil.getSystemData().getVersion() + " Build]");
        loginFrame.setBackground(new Color(21,23,24));

        //close handling
        closed = false;
        loginFrame.addPreCloseAction(() -> closed = true);
        loginFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                closed = true;
            }
        });

        //exiting handler if console frame isn't active
        if (ConsoleFrame.getConsoleFrame().isClosed()) {
            loginFrame.addPostCloseAction(() -> GenesisShare.exit(25));
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
        loginScroll.setThumbColor(CyderColors.regularPink);
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
                        SessionHandler.log(SessionHandler.Tag.CLIENT_IO, "[LOGIN FRAME] " + String.valueOf(input));
                    }

                    switch (loginMode) {
                        case 0:
                            try {
                                char[] lowerCased = String.valueOf(input).toLowerCase().toCharArray();

                                if (Arrays.equals(lowerCased,"create".toCharArray())) {
                                    UserCreator.showGUI();
                                    loginField.setText(bashString);
                                    loginMode = 0;
                                } else if (Arrays.equals(lowerCased,"login".toCharArray())) {
                                    loginField.setText(bashString);
                                    priorityPrintingList.add("Awaiting Username\n");
                                    loginMode = 1;
                                } else if (Arrays.equals(lowerCased,"quit".toCharArray())) {
                                    loginFrame.dispose();
                                    if (ConsoleFrame.getConsoleFrame().isClosed())
                                        GenesisShare.exit(25);

                                } else if (Arrays.equals(lowerCased,"h".toCharArray()) || Arrays.equals(lowerCased,"help".toCharArray())) {
                                    loginField.setText(bashString);
                                    priorityPrintingList.add("Valid commands: create, login, quit, help\n");
                                } else {
                                    loginField.setText(bashString);
                                    priorityPrintingList.add("Unknown command; See \"help\" for help\n");
                                }
                            } catch (Exception e) {
                                ExceptionHandler.handle(e);
                            }

                            break;
                        case 1:
                            username = new String(input);
                            loginMode = 2;
                            loginField.setEchoChar(CyderStrings.ECHO_CHAR);
                            loginField.setText("");
                            priorityPrintingList.add("Awaiting Password (hold shift to reveal password)\n");

                            break;
                        case 2:
                            loginField.setEchoChar((char)0);
                            loginField.setText("");
                            priorityPrintingList.add("Attempting validation\n");

                            if (recognize(username, SecurityUtil.toHexString(SecurityUtil.getSHA256(input)))) {
                                doLoginAnimations = false;
                            } else {
                                loginField.setText(bashString);
                                loginField.setCaretPosition(loginField.getPassword().length);
                                priorityPrintingList.add("Login failed\n");
                                loginMode = 0;
                            }

                            for (char c : input)
                                c = '\0';

                            break;
                        default:
                            loginField.setText(bashString);
                            throw new IllegalArgumentException("Error resulting from login shell default case trigger");
                    }
                }
            }

            //holding shift allows the user to see their password
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    loginField.setEchoChar((char)0);
                }
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT && loginMode == 2) {
                    loginField.setEchoChar(CyderStrings.ECHO_CHAR);
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

        loginFrame.addPreCloseAction(() -> {
            if (ConsoleFrame.getConsoleFrame().isClosed()) {
                UserCreator.close();
            }
        });

        loginFrame.setVisible(true);
        loginFrame.setLocationRelativeTo(GenesisShare.getDominantFrame() == loginFrame ? null : GenesisShare.getDominantFrame());
        CyderSplash.getSplashFrame().dispose(true);

        LinkedList<File> userJsons = new LinkedList<>();

        for (File user : new File("dynamic/users").listFiles()) {
            if (user.isDirectory()) {
                File json = new File(user.getAbsolutePath() + "/userdata.json");

                if (json.exists())
                    userJsons.add(json);
            }
        }

        if (userJsons.size() == 0)
            priorityPrintingList.add("No users found; please type \"create\"\n");

        loginTypingAnimation(loginArea);

        //in case this is after a corruption or logout, start frame checker again
        GenesisShare.resumeFrameChecker();
    }

    public static boolean isClosed() {
        return closed;
    }

    public static CyderFrame getLoginFrame() {
        return loginFrame;
    }

    //login handling methods ------------------------------------------------------

    /**
     * Begins the login sequence to figure out how to enter Cyder. Autocyphers all the autocyphers if enabled
     * and autocyphers exist. Otherwise if the program is released show the login widget. Any failures will lead
     * to the login widget showing up no matter what and the program will only exit
     */
    public static void beginLogin() {
        //figure out how to enter program
        if (SecurityUtil.nathanLenovo()) {
            CyderSplash.setLoadingMessage("Checking for autocypher");

            if (IOUtil.getSystemData().isAutocypher()) {
                SessionHandler.log(SessionHandler.Tag.LOGIN, "AUTOCYPHER ATTEMPT");
                CyderSplash.setLoadingMessage("Autocyphering");

                if (!autoCypher()) {
                    SessionHandler.log(SessionHandler.Tag.LOGIN, "AUTOCYPHER FAIL");
                    showGUI();
                }
            } else showGUI();
        } else if (IOUtil.getSystemData().isReleased()) {
            SessionHandler.log(SessionHandler.Tag.LOGIN, "CYDER STARTING IN RELEASED MODE");
            showGUI();
        } else GenesisShare.exit(-600);
    }

    /**
     * Attempts to log in a user based on the inputed name and already hashed password
     * @param name the provided user account name
     * @param hashedPass the password already having been hashed (we hash it again in checkPassword method)
     * @return whether or not the name and pass combo was authenticated and logged in
     */
    public static boolean recognize(String name, String hashedPass) {
        boolean ret = false;

        try {
            //fix login field if the frame is still open
            if (loginFrame != null) {
                loginField.setEchoChar((char)0);
                loginField.setText(bashString);
            }

            //check password will set the console frame uuid if it finds a userdata.json that matches the provided name and hash
            if (UserUtil.checkPassword(name, hashedPass)) {
                //if they're already logged in
                if (UserUtil.isLoggedIn(ConsoleFrame.getConsoleFrame().getUUID())) {
                    loginFrame.notify("Sorry, but that user is already logged in");

                    //idk how this would be possible but sure
                    if (ConsoleFrame.getConsoleFrame().isClosed())
                        ConsoleFrame.getConsoleFrame().setUUID(null);

                    return false;
                } else {
                    //log out all people as a precaution
                    UserUtil.logoutAllUsers();

                    //this is the only time loggedin is EVER set to 1
                    UserUtil.setUserData("loggedin","1");
                }

                //set ret var
                ret = true;

                //stop login animations
                doLoginAnimations = false;

                //log the success login
                if (autoCypherAttempt) {
                    SessionHandler.log(SessionHandler.Tag.LOGIN, "AUTOCYPHER PASS, " + ConsoleFrame.getConsoleFrame().getUUID());
                    autoCypherAttempt = false;
                } else {
                    SessionHandler.log(SessionHandler.Tag.LOGIN, "STD LOGIN, " + ConsoleFrame.getConsoleFrame().getUUID());
                }

                //reset console frame if it's already open
                if (!ConsoleFrame.getConsoleFrame().isClosed()) {
                    ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().removePostCloseActions();
                    ConsoleFrame.getConsoleFrame().closeConsoleFrame(false);
                }

                //open the console frame
                ConsoleFrame.getConsoleFrame().start();

                //dispose login frame now to avoid final frame disposed checker seeing that there are no frames
                // and exiting the program when we have just logged in
                if (loginFrame != null) {
                    loginFrame.removePostCloseActions();
                    loginFrame.dispose(true);
                }
            } else if (loginFrame != null && loginFrame.isVisible()) {
                loginField.setText("");

                if (autoCypherAttempt) {
                    //rest autocypher
                    autoCypherAttempt = false;
                    SessionHandler.log(SessionHandler.Tag.LOGIN, "AUTOCYPHER FAIL");
                } else {
                    SessionHandler.log(SessionHandler.Tag.LOGIN, "LOGIN FAIL");
                }

                username = "";
                hashedPass = "";
                loginField.requestFocusInWindow();
            } else if (autoCypherAttempt) {
                autoCypherAttempt = false;
                SessionHandler.log(SessionHandler.Tag.LOGIN, "AUTOCYPHER FAIL");
            }
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
        } finally {
            return ret;
        }
    }

    /**
     * Used for debugging, automatically logs the developer in if their account exists,
     * otherwise the program continues as normal
     */
    public static boolean autoCypher() {
        boolean ret = false;

        try {
            LinkedList<SystemData.Hash> cypherHashes = IOUtil.getSystemData().getCypherhashes();
            autoCypherAttempt = true;

            //for all cypher hashes, attempt to log in using one
            for (SystemData.Hash hash : cypherHashes) {
                //if the login works, stop trying hashes
                if (recognize(hash.getName(), hash.getHashpass())) {
                    ret = true;
                    break;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            ret = false;
        } finally {
            return ret;
        }
    }
}
