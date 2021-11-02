package cyder.genesis;

import cyder.consts.CyderColors;
import cyder.consts.CyderStrings;
import cyder.handler.ErrorHandler;
import cyder.handler.SessionLogger;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderCaret;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollPane;
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

public class Login {
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

    private static void loginTypingAnimation(JTextPane refArea) {
        //apparently we need it a second time to fix a bug :/
        doLoginAnimations = true;

        printingList.clear();
        printingList.add("Cyder version: " + IOUtil.getSystemData().getReleasedate() + "\n");
        printingList.add("Type \"h\" for a list of valid commands\n");
        printingList.add("Build: " + IOUtil.getSystemData().getVersion() +"\n");
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
                    if (loginMode != 2 && !String.valueOf(loginField.getPassword()).startsWith(bashString)) {
                        loginField.setText(bashString);
                        loginField.setCaretPosition(loginField.getPassword().length);
                    }

                    Thread.sleep(50);
                }
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"Login Input Caret Position Updater").start();
    }

    public static void showGUI() {
        printingList.clear();
        priorityPrintingList.clear();
        doLoginAnimations = true;
        loginMode = 0;

        if (loginFrame != null) {
            loginFrame.removePostCloseActions();
            loginFrame.dispose();
        }

        IOUtil.cleanUsers();

        loginFrame = new CyderFrame(600, 400,
                ImageUtil.imageIconFromColor(new Color(21,23,24))) {
            @Override
            public void dispose() {
                doLoginAnimations = false;
                super.dispose();
            }
        };
        loginFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);
        loginFrame.setTitle(IOUtil.getSystemData().getVersion() + " Cyder login");
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
                        SessionLogger.log(SessionLogger.Tag.CLIENT_IO, "[LOGIN FRAME] " + String.valueOf(input));
                    }

                    switch (loginMode) {
                        case 0:
                            try {
                                if (Arrays.equals(input,"create".toCharArray())) {
                                    UserCreator.showGUI();
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
                                    loginFrame.dispose();
                                    if (ConsoleFrame.getConsoleFrame().isClosed())
                                        GenesisShare.exit(25);

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
                            loginField.setEchoChar(CyderStrings.ECHO_CHAR);
                            loginField.setText("");
                            priorityPrintingList.add("Awaiting Password\n");

                            break;
                        case 2:
                            loginField.setEchoChar((char)0);
                            if (recognize(username, SecurityUtil.toHexString(SecurityUtil.getSHA256(input)))) {
                                doLoginAnimations = false;
                            } else {
                                loginField.setText(bashString);
                                loginField.setCaretPosition(loginField.getPassword().length);
                                priorityPrintingList.add("Could not recognize user\n");

                                if (input != null)
                                    for (char c: input)
                                        c = '\0';

                                loginMode = 0;
                            }

                            break;
                        default:
                            loginField.setText(bashString);
                            throw new IllegalArgumentException("Error resulting from login shell");
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

        File Users = new File("dynamic/users/");
        String[] directories = Users.list((current, name) -> new File(current, name).isDirectory());

        loginFrame.addPreCloseAction(() -> {
            if (ConsoleFrame.getConsoleFrame().isClosed()) {
                UserCreator.close();
            }
        });

        loginFrame.setVisible(true);
        loginFrame.setLocationRelativeTo(GenesisShare.getDominantFrame() == loginFrame ? null : GenesisShare.getDominantFrame());

        if (directories != null && directories.length == 0)
            priorityPrintingList.add("No users found; please type \"create\"\n");

        loginTypingAnimation(loginArea);

        //in case this is after a corruption, start frame checker again
        GenesisShare.resumeFrameChecker();
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
            if (loginFrame != null) {
                loginField.setEchoChar((char)0);
                loginField.setText(bashString);
            }

            if (UserUtil.checkPassword(name, hashedPass)) {
                if (UserUtil.isLoggedIn(ConsoleFrame.getConsoleFrame().getUUID())) {
                    loginFrame.notify("Sorry, but that user is already logged in");

                    if (!ConsoleFrame.getConsoleFrame().isClosed())
                        ConsoleFrame.getConsoleFrame().setUUID(null);

                    return false;
                } else {
                    UserUtil.setUserData("loggedin","true");
                }

                //set ret var
                ret = true;

                //stop animations
                doLoginAnimations = false;

                //log the success login
                if (autoCypherAttempt) {
                    SessionLogger.log(SessionLogger.Tag.LOGIN, "AUTOCYPHER PASS");
                    autoCypherAttempt = false;
                } else {
                    SessionLogger.log(SessionLogger.Tag.LOGIN, "STD LOGIN");
                }

                //reset console frame if it's already open
                if (!ConsoleFrame.getConsoleFrame().isClosed()) {
                    ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().removePostCloseActions();
                    ConsoleFrame.getConsoleFrame().close();
                }

                //close all frames
                for (Frame f : Frame.getFrames())
                    f.dispose();

                //open the console frame
                ConsoleFrame.getConsoleFrame().start();

                //dispose login frame now to avoid final frame disposed checker seeing that there are no frames
                // and exiting the program when we have just logged in
                if (loginFrame != null) {
                    loginFrame.removePostCloseActions();
                    loginFrame.dispose();
                }
            } else if (loginFrame != null && loginFrame.isVisible()) {
                loginField.setText("");

                if (autoCypherAttempt) {
                    autoCypherAttempt = false;
                    SessionLogger.log(SessionLogger.Tag.LOGIN, "AUTOCYPHER FAIL");
                } else {
                    SessionLogger.log(SessionLogger.Tag.LOGIN, "LOGIN FAIL");
                }

                username = "";
                loginField.requestFocusInWindow();
            } else if (autoCypherAttempt) {
                autoCypherAttempt = false;
                SessionLogger.log(SessionLogger.Tag.LOGIN, "AUTOCYPHER FAIL");
                Login.showGUI();
            }
        } catch (Exception e) {
            ErrorHandler.silentHandle(e);
        } finally {
            return ret;
        }
    }

    /**
     * Used for debugging, automatically logs the developer in if their account exists,
     * otherwise the program continues as normal
     */
    public static void autoCypher() {
        try {
            LinkedList<SystemData.Hash> cypherHashes = IOUtil.getSystemData().getCypherhashes();
            autoCypherAttempt = true;

            //for all cypher hashes, attempt to log in using one
            for (SystemData.Hash hash : cypherHashes) {
                //if the login works, stop trying hashes
                if (recognize(hash.getName(), hash.getHashpass()))
                    break;
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
            showGUI();
        }
    }

    public static boolean isClosed() {
        return closed;
    }

    public static CyderFrame getLoginFrame() {
        return loginFrame;
    }
}
