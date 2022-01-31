package cyder.handlers.internal;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.ui.*;
import cyder.user.UserCreator;
import cyder.genesis.CyderCommon;
import cyder.genesis.CyderSplash;
import cyder.user.UserFile;
import cyder.utilities.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/**
 * A widget to log into Cyder or any other way that the ConsoleFrame might be invoked.
 */
public class LoginHandler {
    /**
     * Instances of LoginHandler not permited.
     */
    private LoginHandler() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * The frame used for loggin into Cyder
     */
    private static CyderFrame loginFrame;

    /**
     * The input field for the login frame.
     */
    private static JPasswordField loginField;

    /**
     * Whether to perform the login frame typing animation.
     */
    private static boolean doLoginAnimations;

    /**
     * The current login mode used to determine what stage the user is at such as username, password, etc.
     */
    private static int loginMode;

    /**
     * The username of the user trying to login.
     */
    private static String username;

    /**
     * The string at the beginning of the input field.
     */
    private static final String bashString = OSUtil.getSystemUsername() + "@" + OSUtil.getComputerName() + ":~$ ";

    /**
     * Whether the login frame is closed.
     */
    private static boolean loginFrameClosed = true;

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
            StringUtil su = new StringUtil(new CyderOutputPane(refArea));

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

    @Widget(trigger = {"login","pin"}, description = "A widget to switch between Cyder users")
    public static void showGUI() {
        Logger.log(Logger.Tag.WIDGET_OPENED, "LOGIN");

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
        loginFrameClosed = false;
        loginFrame.addPreCloseAction(() -> loginFrameClosed = true);
        loginFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loginFrameClosed = true;
            }
        });

        //exiting handler if console frame isn't active
        if (ConsoleFrame.getConsoleFrame().isClosed()) {
            loginFrame.addPostCloseAction(() -> CyderCommon.exit(25));
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
                        Logger.log(Logger.Tag.CLIENT_IO, "[LOGIN FRAME] " + String.valueOf(input));
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
                                        CyderCommon.exit(25);

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

                            if (recognize(username, SecurityUtil.toHexString(
                                    SecurityUtil.getSHA256(input)), false)) {
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
        loginFrame.setLocationRelativeTo(CyderCommon.getDominantFrame() == loginFrame
                ? null : CyderCommon.getDominantFrame());
        CyderSplash.fastDispose();

        ArrayList<File> userJsons = new ArrayList<>();

        for (File user : new File(OSUtil.buildPath("dynamic","users")).listFiles()) {
            if (user.isDirectory()) {
                File json = new File(OSUtil.buildPath(user.getAbsolutePath(), UserFile.USERDATA.getName()));

                if (json.exists())
                    userJsons.add(json);
            }
        }

        if (userJsons.size() == 0)
            priorityPrintingList.add("No users found; please type \"create\"\n");

        loginTypingAnimation(loginArea);

        //in case this is after a corruption or logout, start frame checker again
        CyderCommon.resumeFrameChecker();
    }

    /**
     * Whether the login frame is closed.
     *
     * @return the login frame is closed
     */
    public static boolean isLoginFrameClosed() {
        return loginFrameClosed;
    }

    /**
     * Returns the login frame.
     *
     * @return the login frame
     */
    public static CyderFrame getLoginFrame() {
        return loginFrame;
    }

    /**
     * Begins the login sequence to figure out how to enter Cyder.
     */
    public static void determinCyderEntry() {
        //if on main development computer
        if (SecurityUtil.nathanLenovo()) {
            CyderSplash.setLoadingMessage("Checking for autocypher");

            //if autocyphering is enabled, attempt all cyphers
            if (IOUtil.getSystemData().isAutocypher()) {
                Logger.log(Logger.Tag.LOGIN, "AUTOCYPHER ATTEMPT");
                CyderSplash.setLoadingMessage("Autocyphering");
                boolean autoCypherPass = autoCypher();

                //if autocyphering fails, show teh login gui
                if (!autoCypherPass) {
                    Logger.log(Logger.Tag.LOGIN, "AUTOCYPHER FAIL");
                    showGUI();
                }
            }
            // if main development computer but cyphering is disabled, show the login gui
            else {
                showGUI();
            }
        }
        //otherwise if Cyder is released
        else if (IOUtil.getSystemData().isReleased()) {
            //log the start and show the login frame
            Logger.log(Logger.Tag.LOGIN, "CYDER STARTING IN RELEASED MODE");
            showGUI();
        }
        //otherwise exit
        else {
            CyderCommon.exit(-600);
        }
    }

    /**
     * Attempts to login a user based on the provided
     * name and already hashed password.
     *
     * @param name the provided user account name
     * @param hashedPass the password already having been
     *                   hashed (we hash it again in checkPassword method)
     * @return whether or not the name and pass combo was authenticated and logged in
     */
    public static boolean recognize(String name, String hashedPass, boolean autoCypherAttempt) {
        //initialize our return var
        boolean ret = false;

        //master try block to ensure something is always returned
        try {
            //first, if the we came here from login frame,
            // reset the echo char and text for security reasons
            if (loginFrame != null) {
                loginField.setEchoChar((char)0);
                loginField.setText(bashString);
            }

            //attempt to validate the name and password
            // and obtain the resulting uuid if checkPassword() succeeded
            String uuid = UserUtil.checkPassword(name, hashedPass);

            //if a user was found
            if (uuid != null) {
                //set the UUID (This is the only place in all of Cyder that setUUID should ever be called)
                ConsoleFrame.getConsoleFrame().setUUID(uuid);

                //set ret var
                ret = true;

                //stop login animations
                doLoginAnimations = false;

                //log the success login
                if (autoCypherAttempt) {
                    Logger.log(Logger.Tag.LOGIN, "AUTOCYPHER PASS, " + ConsoleFrame.getConsoleFrame().getUUID());
                    autoCypherAttempt = false;
                } else {
                    Logger.log(Logger.Tag.LOGIN, "STD LOGIN, " + ConsoleFrame.getConsoleFrame().getUUID());
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
                    Logger.log(Logger.Tag.LOGIN, "AUTOCYPHER FAIL");
                } else {
                    Logger.log(Logger.Tag.LOGIN, "LOGIN FAIL");
                }

                username = "";
                hashedPass = "";
                loginField.requestFocusInWindow();
            } else if (autoCypherAttempt) {
                autoCypherAttempt = false;
                Logger.log(Logger.Tag.LOGIN, "AUTOCYPHER FAIL");
            }
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
        } finally {
            //lastly we always return either true or false
            return ret;
        }
    }

    /**
     * Used for debugging, automatically logs the developer in if their account exists,
     * otherwise the program continues as normal.
     */
    public static boolean autoCypher() {
        boolean ret = false;

        try {
            ArrayList<IOUtil.DebugHash> cypherHashes = IOUtil.getDebugHashes();

            //for all cypher hashes, attempt to log in using one
            for (IOUtil.DebugHash hash : cypherHashes) {
                //if the login works, stop trying hashes
                if (recognize(hash.getName(), hash.getHashpass(), true)) {
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
