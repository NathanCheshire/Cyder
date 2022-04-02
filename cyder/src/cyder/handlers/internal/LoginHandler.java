package cyder.handlers.internal;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.CyderEntry;
import cyder.enums.DebugHash;
import cyder.enums.ExitCondition;
import cyder.enums.LoggerTag;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.CyderShare;
import cyder.genesis.CyderSplash;
import cyder.handlers.ConsoleFrame;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderCaret;
import cyder.ui.CyderFrame;
import cyder.ui.CyderOutputPane;
import cyder.ui.CyderScrollPane;
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;

/**
 * A widget to log into Cyder or any other way that the ConsoleFrame might be invoked.
 */
public class LoginHandler {
    /**
     * Instances of LoginHandler not permitted.
     */
    private LoginHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
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
     * The frame used for logging into Cyder
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
     * The username of the user trying to log in.
     */
    private static String username;

    /**
     * The string at the beginning of the input field.
     */
    private static final String defaultBashString = OSUtil.getSystemUsername() + "@" + OSUtil.getComputerName() + ":~$ ";

    /**
     * The BashString currently being used.
     */
    private static String currentBashString = defaultBashString;

    /**
     * Whether the login frame is closed.
     */
    private static boolean loginFrameClosed = true;

    /**
     * The regular non-priority printing list for the login frame.
     */
    private static final LinkedList<String> printingList = new LinkedList<>();

    /**
     * The priority printing list for the login frame.
     */
    private static final LinkedList<String> priorityPrintingList = new LinkedList<>();

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
        printingList.add("Cyder version: " + CyderShare.VERSION + "\n");
        printingList.add("Type \"help\" for a list of valid commands\n");
        printingList.add("Build: " + CyderShare.RELEASE_DATE +"\n");
        printingList.add("Author: Nathan Cheshire\n");
        printingList.add("Design JVM: 8+\n");
        printingList.add("Description: A programmer's swiss army knife\n");

        //timeouts for printing animation
        final int charTimeout = 25;
        final int lineTimeout = 400;

        //the actual thread that performs the printing animation
        CyderThreadRunner.submit(() -> {
            try {
                //while the animation should be performed
                while (doLoginAnimations && loginFrame != null)  {
                    //pull from the priority list first
                    if (!priorityPrintingList.isEmpty()) {
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
                    else if (!printingList.isEmpty()) {
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
        },"Login printing animation");

        //thread to update the input field caret position
        CyderThreadRunner.submit(() -> {
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
        },"Login Input Caret Position Updater");
    }

    /**
     * Shows the login frame.
     */
    @Widget(triggers = {"login","pin"}, description = "A widget to switch between Cyder users")
    public static void showGUI() {
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
                ImageUtil.imageIconFromColor(new Color(21,23,24), 1, 1));
        loginFrame.setTitle("Cyder Login [" + CyderShare.VERSION + " Build]");
        loginFrame.setBackground(new Color(21,23,24));

        //whether the frame is open or closed handling
        loginFrameClosed = false;
        loginFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loginFrameClosed = true;
                doLoginAnimations = false;

                //if the console frame isn't open, close the user creator if it's open
                if (ConsoleFrame.INSTANCE.isClosed()) {
                    UserCreator.close();
                }
            }
        });

        //exit cyder frame on disposal call of login frame if ConsoleFrame isn't active
        if (ConsoleFrame.INSTANCE.isClosed()) {
            loginFrame.addPostCloseAction(() -> CyderShare.exit(ExitCondition.GenesisControlledExit));
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
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
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

        loginFrame.setLocationRelativeTo(CyderShare.getDominantFrame() == loginFrame
                ? null : CyderShare.getDominantFrame());

        //dispose the splash frame immediately
        CyderSplash.fastDispose();

        //if no users were found, prompt the user to create one
        if (UserUtil.getUserCount() == 0)
            priorityPrintingList.add("No users found; please type \"create\"\n");

        //begin typing animations
        startTypingAnimation(new CyderOutputPane(loginArea));
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

                    Logger.log(LoggerTag.CLIENT, "[LOGIN FRAME] " + String.valueOf(input));
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
                                if (ConsoleFrame.INSTANCE.isClosed())
                                    CyderShare.exit(ExitCondition.GenesisControlledExit);

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

                        // password security
                        Arrays.fill(input, '\0');

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
        CyderSplash.setLoadingMessage("Checking for an AutoCypher");

        //if AutoCyphering is enabled, attempt all cyphers
        if (CyderShare.AUTO_CYPHER) {
            Logger.log(LoggerTag.LOGIN, "AUTOCYPHER ATTEMPT");
            CyderSplash.setLoadingMessage("Auto Cyphering");

            //if AutoCyphering fails, show the login gui
            if (!autoCypher()) {
                Logger.log(LoggerTag.LOGIN, "AUTOCYPHER FAIL");
                showGUI();
            }
        }
        // otherwise, unreleased exit
        else if (!CyderShare.RELEASED) {
            ExceptionHandler.exceptionExit("Unreleased build of Cyder","Exception", ExitCondition.NotReleased);
        }
        // otherwise, if Cyder is released/usage is permitted
        else {
            //log the start and show the login frame
            Logger.log(LoggerTag.LOGIN, "CYDER STARTING IN RELEASED MODE");

            Optional<String> optionalUUID = UserUtil.getFirstLoggedInUser();

            if (optionalUUID.isPresent()) {
                String loggedInUUID = optionalUUID.get();

                UserUtil.logoutAllUsers();

                ConsoleFrame.INSTANCE.setUUID(loggedInUUID);

                Logger.log(LoggerTag.LOGIN,  CyderEntry.PreviouslyLoggedIn.getName().toUpperCase()
                        + ", " + loggedInUUID);

                ConsoleFrame.INSTANCE.launch(CyderEntry.PreviouslyLoggedIn);
            } else {
                showGUI();
            }
        }
    }

    /**
     * Attempts to log in a user based on the provided
     * name and already hashed password.
     *
     * @param name the provided user account name
     * @param hashedPass the password already having been
     *                   hashed (we hash it again in checkPassword method)
     * @return whether the name and pass combo was authenticated and logged in
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
                ConsoleFrame.INSTANCE.setUUID(uuid);

                ret = true;

                doLoginAnimations = false;

                Logger.log(LoggerTag.LOGIN, (autoCypherAttempt
                        ? "AUTOCYPHER PASS, " : "[STD LOGIN], ") + uuid);

                if (!ConsoleFrame.INSTANCE.isClosed()) {
                    ConsoleFrame.INSTANCE.closeConsoleFrame(false, true);
                }

                ConsoleFrame.INSTANCE.launch(autoCypherAttempt
                        ? CyderEntry.AutoCypher : CyderEntry.Login);

                // and exiting the program when we have just logged in
                if (loginFrame != null) {
                    loginFrame.removePostCloseActions();
                    loginFrame.dispose(true);
                }
            }
            // autocypher fail
            else if (autoCypherAttempt) {
                Logger.log(LoggerTag.LOGIN, "AUTOCYPHER FAIL");
            }
            // login frame fail
            else if (loginFrame != null && loginFrame.isVisible()) {
                Logger.log(LoggerTag.LOGIN, "LOGIN FAIL");
                loginField.requestFocusInWindow();
            }

            // reset vars
            autoCypherAttempt = false;
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
        }

        return ret;
    }

    /**
     * Used for debugging, automatically logs the developer
     * in if their account exists, otherwise the program proceeds as normal.
     */
    public static boolean autoCypher() {
        boolean ret = false;

        // try block to ensure something is always returned
        try {
            for (DebugHash hash : DebugHash.values()) {
                //if the login works, stop trying hashes
                if (recognize(hash.getName(), hash.getPass(), true)) {
                    ret = true;
                    break;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            ret = false;
        }

        return ret;
    }

    /**
     * Checks whether the given name/pass combo is valid and if so, returns the UUID matched.
     * Otherwise, null is returned to represent that no user was found.
     *
     * @param name the username given
     * @param hashedPass the already once SHA256 hashed password
     * @return the uuid found associated with the name, password combo
     */
    public static String checkPassword(String name, String hashedPass) {
        String ret = null;

        try {
            LinkedList<String> userNames = new LinkedList<>();

            for (File userJsonFile : UserUtil.getUserJsons()) {
                userNames.add(UserUtil.extractUser(userJsonFile).getName());
            }

            if (!StringUtil.in(name, true, userNames)) {
                priorityPrintingList.add("Username not found\n");
                return ret;
            }

            for (File userJsonFile : UserUtil.getUserJsons()) {
                User user = UserUtil.extractUser(userJsonFile);

                //we always hash again here
                if (name.equalsIgnoreCase(user.getName()) && SecurityUtil.toHexString(SecurityUtil.getSHA256(
                                hashedPass.toCharArray())).equals(user.getPass())) {
                    ret = FileUtil.getFilename(userJsonFile.getParentFile().getName());
                    break;
                }
            }

            priorityPrintingList.add("Incorrect password\n");
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }
}
