package cyder.handlers.internal;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.CyderEntry;
import cyder.genesis.CyderCommon;
import cyder.genesis.CyderSplash;
import cyder.handlers.internal.objects.MonitorPoint;
import cyder.ui.*;
import cyder.user.User;
import cyder.user.UserCreator;
import cyder.utilities.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

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
     * The width of the login frame.
     */
    public static final int LOGIN_FRAME_WIDTH = 600;

    /**
     * The height of the login frame.
     */
    public static final int LOGIN_FRAME_HEIGHT = 400;

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
    private static final String defaultBashString = OSUtil.getSystemUsername() + "@" + OSUtil.getComputerName() + ":~$ ";

    /**
     * The bashstring currently being used.
     */
    private static String currentBashString = defaultBashString;

    /**
     * Whether the login frame is closed.
     */
    private static boolean loginFrameClosed = true;

    /**
     * The regular non-priority printing list for the login frame.
     */
    private static LinkedList<String> printingList = new LinkedList<>();

    /**
     * The priority printing list for the login frame.
     */
    private static LinkedList<String> priorityPrintingList = new LinkedList<>();

    /**
     * Begins the login typing animation and printing thread.
     *
     * @param referencePane the CyderOutputPane to use for appending and concurrency
     */
    private static void startTypingAnimation(CyderOutputPane referencePane) {
        //set the status of whether login animations are currently being performed
        doLoginAnimations = true;

        //clear only the printing list
        // (priority may have important things in it still)
        printingList.clear();

        //add the standard login animation
        printingList.add("Cyder version: " + IOUtil.getSystemData().getReleasedate() + "\n");
        printingList.add("Type \"help\" for a list of valid commands\n");
        printingList.add("Build: " + IOUtil.getSystemData().getVersion() +"\n");
        printingList.add("Author: Nathan Cheshire\n");
        printingList.add("Design JVM: 8+\n");
        printingList.add("Description: A programmer's swiss army knife\n");

        //timeouts for printing animation
        final int charTimeout = 25;
        final int lineTimeout = 400;

        //the actual thread that performs the printing animation
        new Thread(() -> {
            try {
                //while the animation should be performed
                while (doLoginAnimations && loginFrame != null)  {
                    //pull from the priority list first
                    if (priorityPrintingList.size() > 0) {
                        //ensure concurrency
                        referencePane.getSemaphore().acquire();

                        //pull the line and perform the animation
                        String line = priorityPrintingList.removeFirst();

                        for (char c : line.toCharArray()) {
                            referencePane.getStringUtil().print(String.valueOf(c));
                            Thread.sleep(charTimeout);
                        }

                        referencePane.getSemaphore().release();
                    }
                    //pull from the regular list second
                    else if (printingList.size() > 0) {
                        //ensure concurrency
                        referencePane.getSemaphore().acquire();

                        //pul the line and perform the animation
                        String line = printingList.removeFirst();

                        for (char c : line.toCharArray()) {
                            referencePane.getStringUtil().print(String.valueOf(c));
                            Thread.sleep(charTimeout);
                        }

                        referencePane.getSemaphore().release();
                    }

                    Thread.sleep(lineTimeout);
                }
            }

            catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        },"Login printing animation").start();

        //thread to update the input field caret position
        new Thread(() -> {
            try {
                while (doLoginAnimations && loginFrame != null) {
                    //reset caret pos
                    if (loginField.getCaretPosition() < currentBashString.length()) {
                        loginField.setCaretPosition(loginField.getPassword().length);
                    }

                    //if it doesn't start with bash string, reset it to start with bashString if it's not mode 2
                    if (loginMode != 2 && !String.valueOf(loginField.getPassword()).startsWith(currentBashString)) {
                        loginField.setText(currentBashString + String.valueOf(
                                loginField.getPassword()).replace(currentBashString, "").trim());
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
        showGUI(null);
    }

    /**
     * Shows the login frame.
     *
     * @param monitorPoint the monitor and point to place the login frame at
     */
    public static void showGUI(MonitorPoint monitorPoint) {
        //clear lists
        priorityPrintingList.clear();
        printingList.clear();

        //reset login mode
        loginMode = 0;

        //if the frame is still active, remove the program exit
        // post close action and dispose the frame immediately
        if (loginFrame != null) {
            loginFrame.removePostCloseActions();
            loginFrame.dispose(true);
        }

        //new anonymous CyderFrame so that we can control the login animation var
        loginFrame = new CyderFrame(LOGIN_FRAME_WIDTH, LOGIN_FRAME_HEIGHT,
                ImageUtil.imageIconFromColor(new Color(21,23,24)));
        loginFrame.setTitle("Cyder Login [" + IOUtil.getSystemData().getVersion() + " Build]");
        loginFrame.setBackground(new Color(21,23,24));

        //whether or not the frame is open or closed handling
        loginFrameClosed = false;
        loginFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loginFrameClosed = true;
                doLoginAnimations = false;

                //if the console frame isn't open, close the user creator if it's open
                if (ConsoleFrame.getConsoleFrame().isClosed()) {
                    UserCreator.close();
                }
            }
        });

        //exit cyder frame on disposal call of login frame if ConsoleFrame isn't active
        if (ConsoleFrame.getConsoleFrame().isClosed()) {
            loginFrame.addPostCloseAction(() -> CyderCommon.exit(25));
        }

        //printing animation output
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

        //field input
        loginField = new JPasswordField(20);
        loginField.setEchoChar((char)0);
        loginField.setText(currentBashString);
        loginField.setBounds(20, 340, 560, 40);
        loginField.setBackground(new Color(21,23,24));
        loginField.setBorder(null);
        loginField.setCaret(new CyderCaret(loginArea.getForeground()));
        loginField.setSelectionColor(CyderColors.selectionColor);
        loginField.setFont(new Font("Agency FB",Font.BOLD, 26));
        loginField.setForeground(new Color(85,181,219));
        loginField.setCaretColor(new Color(85,181,219));
        loginField.addActionListener(e -> loginField.requestFocusInWindow());
        loginField.addKeyListener(loginFieldAdapter);
        loginField.setCaretPosition(currentBashString.length());
        loginFrame.getContentPane().add(loginField);

        //give focus to the login field
        loginFrame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                loginField.requestFocus();
            }
        });

        //set visibility and location
        loginFrame.setVisible(true);

        if (monitorPoint == null) {
            loginFrame.setLocationRelativeTo(CyderCommon.getDominantFrame() == loginFrame
                    ? null : CyderCommon.getDominantFrame());
        } else {
            int centerX = monitorPoint.getX();
            int centerY = monitorPoint.getY();

            int requestedX = centerX - loginFrame.getWidth() / 2;
            int requestedY = centerY - loginFrame.getHeight() / 2;

            int requestedMonitor = monitorPoint.getMonitor();

            FrameUtil.requestFramePosition(requestedMonitor, requestedX, requestedY, loginFrame);
        }

        //dispose the splash frame immediately
        CyderSplash.fastDispose();

        //if no users were found, prompt the user to create one
        if (UserUtil.getUserCount() == 0)
            priorityPrintingList.add("No users found; please type \"create\"\n");

        //begin typing animations
        startTypingAnimation(new CyderOutputPane(loginArea));

        //resume the failsafe
        CyderCommon.resumeFrameChecker();
    }

    /**
     * The handler used to determine how to handle user input from the login field.
     */
    private static final KeyAdapter loginFieldAdapter = new KeyAdapter() {
        public void keyTyped(java.awt.event.KeyEvent evt) {
            if (evt.getKeyChar() == KeyEvent.VK_BACK_SPACE && loginMode != 2) {
                if (loginField.getPassword().length < currentBashString.toCharArray().length) {
                    evt.consume();
                    loginField.setText(currentBashString);
                }
            }

            //if enter was pressed
            else if (evt.getKeyChar() == '\n') {
                //get the input text
                char[] input = loginField.getPassword();
                String inputString = null;

                //if the login mode is not expecting a password
                if (loginMode != 2) {
                    inputString = new String(input).replace(currentBashString,"");

                    Logger.log(Logger.Tag.CLIENT_IO, "[LOGIN FRAME] " + String.valueOf(input));
                }

                switch (loginMode) {
                    //if not login mode, user could enter anything
                    case 0:
                        try {
                            if (inputString.equalsIgnoreCase("create")) {
                                UserCreator.showGUI();
                                loginField.setText(currentBashString);
                                loginMode = 0;
                            } else if (inputString.equalsIgnoreCase("login")) {
                                currentBashString = "Username: ";
                                loginField.setText(currentBashString);
                                priorityPrintingList.add("Awaiting Username\n");
                                loginMode = 1;
                            } else if (inputString.equalsIgnoreCase("quit")) {
                                loginFrame.dispose();
                                if (ConsoleFrame.getConsoleFrame().isClosed())
                                    CyderCommon.exit(25);

                            } else if (inputString.equalsIgnoreCase("help")) {
                                loginField.setText(currentBashString);
                                priorityPrintingList.add("Valid commands: create, login, quit, help\n");
                            } else {
                                loginField.setText(currentBashString);
                                priorityPrintingList.add("Unknown command; See \"help\" for help\n");
                            }
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }

                        break;
                    //expecting a username
                    case 1:
                        username = inputString;
                        loginMode = 2;
                        loginField.setEchoChar(CyderStrings.ECHO_CHAR);
                        loginField.setText("");
                        priorityPrintingList.add("Awaiting Password (hold shift to reveal password)\n");
                        currentBashString = defaultBashString;

                        break;
                    //expecting a password
                    case 2:
                        loginField.setEchoChar((char)0);
                        loginField.setText("");
                        priorityPrintingList.add("Attempting validation\n");

                        if (!recognize(username, SecurityUtil.toHexString(
                                SecurityUtil.getSHA256(input)), false)) {

                            loginField.setEchoChar((char)0);
                            loginField.setText(currentBashString);
                            loginField.setCaretPosition(loginField.getPassword().length);

                            priorityPrintingList.add("Login failed\n");
                            loginMode = 0;
                            username = "";

                        }

                        for (char c : input) {
                            c = '\0';
                        }

                        break;
                    default:
                        loginField.setText(currentBashString);
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

        //releasing shift sets the echo char back to the
        // obfuscated one if we are expecting a password
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SHIFT && loginMode == 2) {
                loginField.setEchoChar(CyderStrings.ECHO_CHAR);
            }
        }
    };

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
    public static void determineCyderEntry() {
        //if on main development computer
        if (SecurityUtil.nathanLenovo()) {
            CyderSplash.setLoadingMessage("Checking for autocypher");

            //if autocyphering is enabled, attempt all cyphers
            if (IOUtil.getSystemData().isAutocypher()) {
                Logger.log(Logger.Tag.LOGIN, "AUTOCYPHER ATTEMPT");
                CyderSplash.setLoadingMessage("Autocyphering");

                //if autocyphering fails, show teh login gui
                if (!autoCypher()) {
                    Logger.log(Logger.Tag.LOGIN, "AUTOCYPHER FAIL");
                    showGUI();
                }
            }
            //if main development computer but autocypher is disabled, show the login gui
            else {
                showGUI();
            }
        }
        //otherwise unreleased exit
        else if (!IOUtil.getSystemData().isReleased()) {
            ExceptionHandler.exceptionExit("Unreleased build of Cyder","Unreleased",-600);
        }
        //otherwise if Cyder is released/usage is permitted
        else {
            //log the start and show the login frame
            Logger.log(Logger.Tag.LOGIN, "CYDER STARTING IN RELEASED MODE");

            String loggedInUUD = UserUtil.getFirstLoggedInUser();

            if (loggedInUUD != null) {
                UserUtil.logoutAllUsers();

                ConsoleFrame.getConsoleFrame().setUUID(loggedInUUD);

                Logger.log(Logger.Tag.LOGIN, "[PREVIOUS SESSION RESUMED] " + loggedInUUD);

                ConsoleFrame.getConsoleFrame().launch(CyderEntry.PreviouslyLoggedIn);

            } else {
                showGUI();
            }
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
        boolean ret = false;

        //master try block to ensure something is always returned
        try {
            //attempt to validate the name and password
            // and obtain the resulting uuid if checkPassword() succeeded
            String uuid = checkPassword(name, hashedPass);

            if (uuid != null) {
                //set the UUID
                ConsoleFrame.getConsoleFrame().setUUID(uuid);

                ret = true;

                doLoginAnimations = false;

                Logger.log(Logger.Tag.LOGIN, (autoCypherAttempt
                        ? "[AUTOCYPHER PASS]: " : "[STD LOGIN]: ") + uuid);

                if (!ConsoleFrame.getConsoleFrame().isClosed()) {
                    ConsoleFrame.getConsoleFrame().closeConsoleFrame(false, true);
                }

                ConsoleFrame.getConsoleFrame().launch(autoCypherAttempt
                        ? CyderEntry.AutoCypher : CyderEntry.Login);

                //dispose login frame now to avoid final frame disposed checker seeing that there are no frames
                // and exiting the program when we have just logged in
                if (loginFrame != null) {
                    loginFrame.removePostCloseActions();
                    loginFrame.dispose(true);
                }
            }
            //autocypher fail
            else if (autoCypherAttempt) {
                Logger.log(Logger.Tag.LOGIN, "AUTOCYPHER FAIL");
            }
            //login frame fail
            else if (loginFrame != null && loginFrame.isVisible()) {
                Logger.log(Logger.Tag.LOGIN, "LOGIN FAIL");
                loginField.requestFocusInWindow();
            }

            //clean up calls
            autoCypherAttempt = false;
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
        } finally {
            return ret;
        }
    }

    /**
     * Used for debugging, automatically logs the developer
     * in if their account exists, otherwise the program continues as normal.
     */
    public static boolean autoCypher() {
        boolean ret = false;

        //try block ensure something is always returned
        try {
            //get all hashes
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

    /**
     * Checks whether or not the given name/pass combo is valid and if so, returns the UUID matched.
     * Otherwise, null is returned to represent that no user was found.
     *
     * @param name the username given
     * @param hashedPass the already once SHA256 hashed password
     * @return the uuid found associated with the name, password combo
     */
    public static String checkPassword(String name, String hashedPass) {
        String ret = null;

        try {
            for (File userJsonFile : UserUtil.getUserJsons()) {
                User user = UserUtil.extractUser(userJsonFile);

                //we always hash again here
                if (name.equalsIgnoreCase(user.getName()) && SecurityUtil.toHexString(SecurityUtil.getSHA256(
                                hashedPass.toCharArray())).equals(user.getPass())) {
                    ret = StringUtil.getFilename(userJsonFile.getParentFile().getName());
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }
}
