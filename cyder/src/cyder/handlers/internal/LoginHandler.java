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
        //clear lists
        priorityPrintingList.clear();
        printingList.clear();

        //reset login mode
        loginMode = LoginMode.INPUT;

        //if the frame is still active, remove the program exit
        // post close action and dispose the frame immediately
        if (loginFrame != null) {
            loginFrame.removePostCloseActions();
            loginFrame.dispose(true);
        }

        //new anonymous CyderFrame so that we can control the login animation var
        loginFrame = new CyderFrame(LOGIN_FRAME_WIDTH, LOGIN_FRAME_HEIGHT,
                ImageUtil.imageIconFromColor(backgroundColor, 1, 1));
        loginFrame.setTitle(generateLoginFrameTitle());
        loginFrame.setBackground(backgroundColor);

        //whether the frame is open or closed handling
        loginFrameClosed = false;
        loginFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loginFrameClosed = true;
                doLoginAnimations = false;

                if (Console.INSTANCE.isClosed()) {
                    OSUtil.exit(ExitCondition.GenesisControlledExit);
                }
            }
        });

        //printing animation output
        JTextPane loginArea = new JTextPane();
        loginArea.setBounds(20, 40, 560, 280);
        loginArea.setBackground(new Color(21, 23, 24));
        loginArea.setBorder(null);
        loginArea.setFocusable(false);
        loginArea.setEditable(false);
        loginArea.setFont(new Font("Agency FB", Font.BOLD, 26));
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

        //field input
        loginField = new JPasswordField(20);
        loginField.setEchoChar((char) 0);
        loginField.setText(currentBashString);
        loginField.setBounds(20, 340, 560, 40);
        loginField.setBackground(new Color(21, 23, 24));
        loginField.setBorder(null);
        loginField.setCaret(new CyderCaret(loginArea.getForeground()));
        loginField.setSelectionColor(CyderColors.selectionColor);
        loginField.setFont(new Font("Agency FB", Font.BOLD, 26));
        loginField.setForeground(new Color(85, 181, 219));
        loginField.setCaretColor(new Color(85, 181, 219));
        loginField.addActionListener(e -> loginField.requestFocusInWindow());
        loginField.addKeyListener(loginFieldAdapter);

        shiftShowsPassword = CyderPasswordField.addShiftShowsPasswordListener(loginField);
        shiftShowsPassword.set(false);

        loginField.setCaretPosition(currentBashString.length());
        loginFrame.getContentPane().add(loginField);

        //give focus to the login field
        loginFrame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                loginField.requestFocus();
            }
        });
        loginFrame.finalizeAndShow();

        CyderSplash.INSTANCE.fastDispose();

        if (UserUtil.getUserCount() == 0) {
            priorityPrintingList.add("No users found; please type \"create\"\n");
        }

        startTypingAnimation(new CyderOutputPane(loginArea));
    }

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
            }

            //if enter was pressed
            else if (evt.getKeyChar() == '\n') {
                //get the input text
                char[] input = loginField.getPassword();
                String inputString = null;

                //if the login mode is not expecting a password
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
     * Begins the login sequence to figure out how to enter Cyder.
     */
    public static void determineCyderEntry() {
        CyderSplash.INSTANCE.setLoadingMessage("Checking for an AutoCypher");

        //if AutoCyphering is enabled, attempt all cyphers
        if (PropLoader.getBoolean("autocypher")) {
            Logger.log(Logger.Tag.LOGIN, "AutoCypher Attempt");
            CyderSplash.INSTANCE.setLoadingMessage("Auto Cyphering");

            //if AutoCyphering fails, show the login gui
            if (!autoCypher()) {
                Logger.log(Logger.Tag.LOGIN, "AutoCypher Fail");
                showGui();
            }
        }
        // otherwise, unreleased exit
        else if (!PropLoader.getBoolean("released")) {
            ExceptionHandler.exceptionExit("Unreleased build of Cyder",
                    "Exception", ExitCondition.NotReleased);
        }
        // otherwise, if Cyder is released/usage is permitted
        else {
            //log the start and show the login frame
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
     * Attempts to log in a user based on the provided
     * name and an already hashed password.
     *
     * @param name       the provided user account name
     * @param hashedPass the password already having been hashed
     * @return whether the name and pass combo was authenticated and logged in
     */
    public static boolean recognize(String name, String hashedPass, boolean autoCypherAttempt) {
        boolean ret = false;

        try {
            Optional<String> optionalUuid = checkPassword(name, hashedPass);

            if (optionalUuid.isPresent()) {
                Console.INSTANCE.setUUID(optionalUuid.get());

                ret = true;

                doLoginAnimations = false;

                if (!Console.INSTANCE.isClosed()) {
                    Console.INSTANCE.closeFrame(false, true);
                }

                Console.INSTANCE.launch(autoCypherAttempt
                        ? CyderEntry.AutoCypher : CyderEntry.Login);
            } else if (autoCypherAttempt) {
                Logger.log(Logger.Tag.LOGIN, CyderEntry.AutoCypher.getFailMessage());
            } else if (loginFrame != null && loginFrame.isVisible()) {
                Logger.log(Logger.Tag.LOGIN, CyderEntry.Login.getFailMessage());
                loginField.requestFocusInWindow();
            }
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
        try {
            if (recognize(PropLoader.getString("debug_hash_name"),
                    PropLoader.getString("debug_hash_password"), true)) {
                return true;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return false;
    }

    /**
     * Checks whether the given name/pass combo is valid and if so, returns the UUID matched.
     * Otherwise, an empty optional is returned to represent that no user was found.
     *
     * @param name       the username given
     * @param hashedPass the already once SHA256 hashed password
     * @return the uuid found associated with the name, password combo
     */
    public static Optional<String> checkPassword(String name, String hashedPass) {
        try {
            LinkedList<String> userNames = new LinkedList<>();

            for (File userJsonFile : UserUtil.getUserJsons()) {
                userNames.add(UserUtil.extractUser(userJsonFile).getName());
            }

            if (!StringUtil.in(name, true, userNames)) {
                priorityPrintingList.add("Username not found\n");
                return Optional.empty();
            }

            for (File userJsonFile : UserUtil.getUserJsons()) {
                User user = UserUtil.extractUser(userJsonFile);

                //we always hash again here
                if (name.equalsIgnoreCase(user.getName()) && SecurityUtil.toHexString(SecurityUtil.getSHA256(
                        hashedPass.toCharArray())).equals(user.getPass())) {
                    return Optional.of(FileUtil.getFilename(userJsonFile.getParentFile().getName()));
                }
            }

            priorityPrintingList.add("Incorrect password\n");
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Optional.empty();
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
