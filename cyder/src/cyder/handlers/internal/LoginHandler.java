package cyder.handlers.internal;

import cyder.annotations.Widget;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.CyderEntry;
import cyder.enums.ExitCondition;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.CyderSplash;
import cyder.genesis.PropLoader;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.*;
import cyder.user.User;
import cyder.user.UserCreator;
import cyder.user.UserUtil;
import cyder.utils.*;

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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A widget to log into Cyder or any other way that the Console might be invoked.
 */
public final class LoginHandler {
    /**
     * Instances of LoginHandler not permitted.
     */
    private LoginHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
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
     * The username of the user trying to log in.
     */
    private static String username;

    /**
     * The string at the beginning of the input field.
     */
    private static final String defaultBashString =
            OSUtil.getOsUsername() + "@" + OSUtil.getComputerName() + ":~$ ";

    /**
     * The BashString currently being used.
     */
    private static String currentBashString = defaultBashString;

    /**
     * Whether the login frame is closed.
     */
    private static boolean loginFrameClosed = true;

    /**
     * The background color of the login frame.
     */
    public static final Color backgroundColor = new Color(21, 23, 24);

    /**
     * The color used for the output area text.
     */
    public static final Color textColor = new Color(85, 181, 219);

    /**
     * The atomic boolean to control whether shift shows the password of the password field.
     */
    private static AtomicBoolean shiftShowsPassword;

    /**
     * The regular non-priority printing list for the login frame.
     */
    private static final LinkedList<String> printingList = new LinkedList<>();

    /**
     * The priority printing list for the login frame.
     */
    private static final LinkedList<String> priorityPrintingList = new LinkedList<>();

    /**
     * The font for the login field.
     */
    private static final Font loginFieldFont = new Font("Agency FB", Font.BOLD, 26);

    /**
     * The valid login modes.
     */
    private enum LoginMode {
        /**
         * Expecting general input.
         */
        INPUT,

        /**
         * Expecting a username.
         */
        USERNAME,

        /**
         * Expecting a password.
         */
        PASSWORD
    }

    private static LoginMode loginMode = LoginMode.INPUT;

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
        printingList.add("Cyder version: " + PropLoader.getString("version") + "\n");
        printingList.add("Type \"help\" for a list of valid commands\n");
        printingList.add("Build: " + PropLoader.getBoolean("release_date") + "\n");
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
                while (doLoginAnimations && loginFrame != null) {
                    //pull from the priority list first
                    if (!priorityPrintingList.isEmpty()) {
                        //ensure concurrency
                        referencePane.getSemaphore().acquire();

                        //pull the line and perform the animation
                        String line = priorityPrintingList.removeFirst();

                        for (char c : line.toCharArray()) {
                            referencePane.getStringUtil().print(String.valueOf(c));
                            ThreadUtil.sleep(charTimeout);
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
                            ThreadUtil.sleep(charTimeout);
                        }

                        referencePane.getSemaphore().release();
                    }

                    ThreadUtil.sleep(lineTimeout);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Login printing animation");

        //thread to update the input field caret position
        CyderThreadRunner.submit(() -> {
            try {
                while (doLoginAnimations && loginFrame != null) {
                    //reset caret pos
                    if (loginField.getCaretPosition() < currentBashString.length()) {
                        loginField.setCaretPosition(loginField.getPassword().length);
                    }

                    //if it doesn't start with bash string, reset it to start with bashString if it's not mode 2
                    if (loginMode != LoginMode.PASSWORD && !String.valueOf(loginField.getPassword()).startsWith(currentBashString)) {
                        loginField.setText(currentBashString + String.valueOf(
                                loginField.getPassword()).replace(currentBashString, "").trim());
                        loginField.setCaretPosition(loginField.getPassword().length);
                    }

                    ThreadUtil.sleep(50);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Login Input Caret Position Updater");
    }

    /**
     * Shows the login frame.
     */
    @Widget(triggers = {"login", "pin"}, description = "A widget to switch between Cyder users")
    public static void showGui() {
        priorityPrintingList.clear();
        printingList.clear();
        loginMode = LoginMode.INPUT;

        if (loginFrame != null) {
            loginFrame.removePostCloseActions();
            loginFrame.dispose(true);
        }

        loginFrame = new CyderFrame(
                LOGIN_FRAME_WIDTH, LOGIN_FRAME_HEIGHT,
                ImageUtil.imageIconFromColor(backgroundColor));
        loginFrame.setTitle(generateLoginFrameTitle());
        loginFrame.setBackground(backgroundColor);
        loginFrame.addWindowListener(loginFrameWindowAdapter);
        loginFrameClosed = false;

        JTextPane loginArea = new JTextPane();
        loginArea.setBounds(20, 40, 560, 280);
        loginArea.setBackground(backgroundColor);
        loginArea.setBorder(null);
        loginArea.setFocusable(false);
        loginArea.setEditable(false);
        loginArea.setFont(loginFieldFont);
        loginArea.setForeground(textColor);
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

        loginField = new JPasswordField(20);
        loginField.setEchoChar((char) 0);
        loginField.setText(currentBashString);
        loginField.setBounds(20, 340, 560, 40);
        loginField.setBackground(backgroundColor);
        loginField.setBorder(null);
        loginField.setCaret(new CyderCaret(loginArea.getForeground()));
        loginField.setSelectionColor(CyderColors.selectionColor);
        loginField.setFont(loginFieldFont);
        loginField.setForeground(textColor);
        loginField.setCaretColor(textColor);
        loginField.addActionListener(e -> loginField.requestFocusInWindow());
        loginField.addKeyListener(loginFieldAdapter);

        shiftShowsPassword = CyderPasswordField.addShiftShowsPasswordListener(loginField);
        shiftShowsPassword.set(false);

        loginField.setCaretPosition(currentBashString.length());
        loginFrame.getContentPane().add(loginField);
        loginFrame.finalizeAndShow();

        CyderSplash.INSTANCE.fastDispose();

        if (UserUtil.getUserCount() == 0) {
            priorityPrintingList.add("No users found; please type \"create\"\n");
        }

        startTypingAnimation(new CyderOutputPane(loginArea));
    }

    /**
     * The window adapter for the login frame to exit Cyder if the login frame is the only program frame open.
     */
    private static final WindowAdapter loginFrameWindowAdapter = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            loginFrameClosed = true;
            doLoginAnimations = false;

            if (Console.INSTANCE.isClosed()) {
                OSUtil.exit(ExitCondition.GenesisControlledExit);
            }
        }

        public void windowOpened(WindowEvent e) {
            loginField.requestFocus();
        }
    };

    /**
     * The handler used to determine how to handle user input from the login field.
     */
    private static final KeyAdapter loginFieldAdapter = new KeyAdapter() {
        public void keyTyped(java.awt.event.KeyEvent evt) {
            if (evt.getKeyChar() == KeyEvent.VK_BACK_SPACE && loginMode != LoginMode.PASSWORD) {
                if (loginField.getPassword().length < currentBashString.toCharArray().length) {
                    evt.consume();
                    loginField.setText(currentBashString);
                }
            } else if (evt.getKeyChar() == '\n') { // Enter key
                char[] input = loginField.getPassword();
                String inputString = null;

                if (loginMode != LoginMode.PASSWORD) {
                    inputString = new String(input).replace(currentBashString, "");

                    Logger.log(Logger.Tag.CLIENT, "[LOGIN FRAME] " + String.valueOf(input));
                }

                switch (loginMode) {
                    case INPUT:
                        try {
                            if (inputString.equalsIgnoreCase("create")) {
                                UserCreator.showGui();
                                loginField.setText(currentBashString);
                                loginMode = LoginMode.INPUT;
                            } else if (inputString.equalsIgnoreCase("login")) {
                                currentBashString = "Username: ";
                                loginField.setText(currentBashString);
                                priorityPrintingList.add("Awaiting Username\n");
                                loginMode = LoginMode.USERNAME;
                            } else if (inputString.equalsIgnoreCase("quit")) {
                                loginFrame.dispose();
                                if (Console.INSTANCE.isClosed())
                                    OSUtil.exit(ExitCondition.GenesisControlledExit);

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
                    case USERNAME:
                        username = inputString;
                        loginMode = LoginMode.PASSWORD;
                        loginField.setEchoChar(CyderStrings.ECHO_CHAR);
                        loginField.setText("");
                        priorityPrintingList.add("Awaiting Password (hold shift to reveal password)\n");
                        currentBashString = defaultBashString;
                        shiftShowsPassword.set(true);

                        break;
                    case PASSWORD:
                        loginField.setEchoChar((char) 0);
                        shiftShowsPassword.set(false);
                        loginField.setText("");
                        priorityPrintingList.add("Attempting validation\n");

                        if (!recognize(username, SecurityUtil.toHexString(
                                SecurityUtil.getSHA256(input)), false)) {

                            loginField.setEchoChar((char) 0);
                            loginField.setText(currentBashString);
                            loginField.setCaretPosition(loginField.getPassword().length);

                            priorityPrintingList.add("Login failed\n");
                            loginMode = LoginMode.INPUT;
                            username = "";
                        }

                        Arrays.fill(input, '\0');

                        break;
                    default:
                        loginField.setText(currentBashString);
                        throw new IllegalArgumentException("Error resulting from login shell default case trigger");
                }
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
     * The key used to obtain the value of the auto cypher prop.
     */
    private static final String AUTOCYPHER = "autocypher";

    /**
     * The key used to obtain the value of the released prop.
     */
    private static final String RELEASED = "released";

    /**
     * Begins the login sequence to figure out how to enter Cyder.
     */
    public static void determineCyderEntry() {
        CyderSplash.INSTANCE.setLoadingMessage("Checking for an AutoCypher");

        if (PropLoader.getBoolean(AUTOCYPHER)) {
            Logger.log(Logger.Tag.LOGIN, "AutoCypher Attempt");
            CyderSplash.INSTANCE.setLoadingMessage("Auto Cyphering");

            if (!autoCypher()) {
                Logger.log(Logger.Tag.LOGIN, "AutoCypher Fail");
                showGui();
            }
        } else if (!PropLoader.getBoolean(RELEASED)) {
            ExceptionHandler.exceptionExit("Unreleased build of Cyder",
                    "Exception", ExitCondition.NotReleased);
        } else {
            Logger.log(Logger.Tag.LOGIN, "CYDER STARTING IN RELEASED MODE");

            Optional<String> optionalUUID = UserUtil.getFirstLoggedInUser();

            if (optionalUUID.isPresent()) {
                String loggedInUUID = optionalUUID.get();

                UserUtil.logoutAllUsers();

                Console.INSTANCE.setUUID(loggedInUUID);

                Logger.log(Logger.Tag.LOGIN, CyderEntry.PreviouslyLoggedIn.getName().toUpperCase()
                                + ", " + loggedInUUID);

                Console.INSTANCE.launch(CyderEntry.PreviouslyLoggedIn);
            } else {
                showGui();
            }
        }
    }

    /**
     * The status returned by an {@link CheckPasswordStatus} call.
     */
    private enum CheckPasswordStatus {
        /**
         * The login failed due to invalid credentials.
         */
        FAILED,
        /**
         * The login failed due to not being able to locate the username.
         */
        UNKNOWN_USER,
        /**
         * The login succeeded and the {@link #findUuid(String)} method should be invoked
         * to find the uuid and load the other user files.
         */
        SUCCESS
    }

    /**
     * Attempts to log in a user based on the provided
     * name and an already hashed password.
     *
     * @param name       the provided user account name
     * @param hashedPass the password already having been hashed
     * @return whether the name and pass combo was authenticated and logged in
     */
    public static boolean recognize(String name, String hashedPass, boolean autoCypherAttempt) {
        switch (checkPassword(name, hashedPass)) {
            case FAILED -> {
                if (autoCypherAttempt) {
                    priorityPrintingList.add("Autocypher failed");
                    Logger.log(Logger.Tag.LOGIN, CyderEntry.AutoCypher.getFailMessage());
                } else if (loginFrame != null && loginFrame.isVisible()) {
                    priorityPrintingList.add("Incorrect password\n");
                    Logger.log(Logger.Tag.LOGIN, CyderEntry.Login.getFailMessage());
                    loginField.requestFocusInWindow();
                }
            }
            case UNKNOWN_USER -> priorityPrintingList.add("Unknown user\n");
            case SUCCESS -> {
                String uuid = findUuid(name);

                Console.INSTANCE.setUUID(uuid);

                doLoginAnimations = false;

                if (!Console.INSTANCE.isClosed()) {
                    Console.INSTANCE.closeFrame(false, true);
                }

                Console.INSTANCE.launch(autoCypherAttempt ? CyderEntry.AutoCypher : CyderEntry.Login);
                return true;
            }
        }

        return false;
    }

    /**
     * The key used to pull the auto cypher username from the props.
     */
    private static final String DEBUG_HASH_NAME = "debug_hash_name";

    /**
     * The key used to pull the auto cypher password from the props (already SHA256 singly hashed).
     */
    private static final String DEBUG_HASH_PASSWORD = "debug_hash_name";

    /**
     * Attempts to automatically log in the developer if {@link #DEBUG_HASH_NAME}
     * and {@link #DEBUG_HASH_PASSWORD} exists within props.
     */
    public static boolean autoCypher() {
        return recognize(PropLoader.getString(DEBUG_HASH_NAME),
                PropLoader.getString(DEBUG_HASH_PASSWORD), true);
    }

    /**
     * Checks whether the given name/pass combo is valid and if so, returns the UUID matched.
     * Otherwise, an empty optional is returned to represent that no user was found.
     *
     * @param name       the username given
     * @param hashedPass the already once SHA256 hashed password
     * @return the result of checking for the a user with the provided name and password
     */
    public static CheckPasswordStatus checkPassword(String name, String hashedPass) {
        LinkedList<String> names = new LinkedList<>();

        for (File userJsonFile : UserUtil.getUserJsons()) {
            names.add(UserUtil.extractUser(userJsonFile).getName());
        }

        if (!StringUtil.in(name, true, names)) {
            return CheckPasswordStatus.UNKNOWN_USER;
        }

        for (File userJsonFile : UserUtil.getUserJsons()) {
            User user = UserUtil.extractUser(userJsonFile);

            if (name.equalsIgnoreCase(user.getName()) && SecurityUtil.toHexString(
                    SecurityUtil.getSHA256(hashedPass.toCharArray())).equals(user.getPass())) {
                return CheckPasswordStatus.SUCCESS;
            }
        }

        return CheckPasswordStatus.FAILED;
    }

    /**
     * Finds and returns the uuid for the user with the provided name.
     *
     * @return the uuid for the user with the provided name
     * @throws IllegalArgumentException if a user with the provided name cannot be found
     */
    public static String findUuid(String name) {
        for (File userJsonFile : UserUtil.getUserJsons()) {
            User user = UserUtil.extractUser(userJsonFile);

            if (name.equalsIgnoreCase(user.getName())) {
                return FileUtil.getFilename(userJsonFile);
            }
        }

        throw new IllegalArgumentException("User folder not found for name: " + name);
    }

    /**
     * Returns the title to use for the login frame.
     *
     * @return the title to use for the login frame
     */
    public static String generateLoginFrameTitle() {
        return "Cyder Login [" + PropLoader.getString("version") + " Build]";
    }
}
