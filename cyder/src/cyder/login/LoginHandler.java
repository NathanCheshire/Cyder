package cyder.login;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.annotations.Widget;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.enums.ExitCondition;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.CyderSplash;
import cyder.genesis.ProgramModeManager;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.PropLoader;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.field.CyderCaret;
import cyder.ui.field.CyderPasswordField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.pane.CyderOutputPane;
import cyder.ui.pane.CyderScrollPane;
import cyder.user.User;
import cyder.user.UserCreator;
import cyder.user.UserUtil;
import cyder.utils.ImageUtil;
import cyder.utils.OsUtil;
import cyder.utils.SecurityUtil;
import cyder.utils.StringUtil;

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
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import static cyder.constants.CyderStrings.*;

/**
 * A widget to log into Cyder or any other way that the Console might be invoked.
 */
public final class LoginHandler {
    /**
     * Suppress default constructor.
     */
    private LoginHandler() {
        throw new IllegalMethodException(ATTEMPTED_INSTANTIATION);
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
     * The text area for the login outputs.
     */
    private static JTextPane loginArea;

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
            OsUtil.getOsUsername() + "@" + OsUtil.getComputerName() + ":~$ ";

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
     * The current login mode
     */
    private static LoginMode loginMode = LoginMode.INPUT;

    /**
     * The key used to obtain the value of the auto cypher prop.
     */
    private static final String AUTOCYPHER = "autocypher";

    /**
     * The key used to obtain the value of the released prop.
     */
    private static final String RELEASED = "released";

    /**
     * The key used to pull the auto cypher username from the props.
     */
    private static final String DEBUG_HASH_NAME = "debug_hash_name";

    /**
     * The key used to pull the auto cypher password from the props (already SHA256 singly hashed).
     */
    private static final String DEBUG_HASH_PASSWORD = "debug_hash_password";

    /**
     * The key to get the version of Cyder from the props.
     */
    private static final String VERSION = "version";

    /**
     * The echo char to use when a password field's text should be visible and not obfuscated.
     */
    private static final char DEFAULT_FIELD_ECHO_CHAR = (char) 0;

    /**
     * The key to get the release date prop.
     */
    private static final String RELEASE_DATE = "release_date";

    /**
     * The timeout in ms between char prints.
     */
    private static final int charTimeout = 20;

    /**
     * The timeout in me between line prints.
     */
    private static final int lineTimeout = 400;

    /**
     * The name for the login printing animation thread.
     */
    private static final String LOGIN_PRINTING_ANIMATION_THREAD_NAME = "Login printing animation";

    /**
     * The create command trigger.
     */
    private static final String CREATE = "create";

    /**
     * The login command trigger.
     */
    private static final String LOGIN = "login";

    /**
     * The quit command trigger.
     */
    private static final String QUIT = "quit";

    /**
     * The help command trigger.
     */
    private static final String HELP = "help";

    /**
     * The valid commands.
     */
    private static final ImmutableList<String> validCommands = ImmutableList.of(CREATE, LOGIN, QUIT, HELP);

    /**
     * The prefix for when the login mode is username.
     */
    private static final String usernameModePrefix = "Username: ";

    /**
     * The prefix for the login frame title.
     */
    private static final String titlePrefix = "Cyder Login" + space;

    /**
     * The uuid for the user attempting to log in.
     */
    private static String recognizedUuid = "";

    /**
     * A newline character.
     */
    private static final char newline = '\n';

    /**
     * Begins the login typing animation and printing thread.
     */
    private static void startTypingAnimation() {
        doLoginAnimations = true;
        printingList.clear();

        String cyderVersion = PropLoader.getString(VERSION);
        String cyderReleaseDate = PropLoader.getString(RELEASE_DATE);
        ImmutableList<String> standardPrints = ImmutableList.of(
                "Cyder version: " + cyderVersion + newline,
                "Type " + quote + "help" + quote + " for a list of valid commands" + newline,
                "Build: " + cyderReleaseDate + newline,
                "Author: Nate Cheshire" + newline,
                "Design JVM: 17" + newline,
                "Description: A programmer's swiss army knife" + newline
        );
        printingList.addAll(standardPrints);

        CyderOutputPane referencePane = new CyderOutputPane(loginArea);

        CyderThreadRunner.submit(() -> {
            try {
                while (doLoginAnimations && loginFrame != null) {
                    Semaphore semaphore = referencePane.getSemaphore();
                    if (!priorityPrintingList.isEmpty()) {
                        semaphore.acquire();
                        String line = priorityPrintingList.removeFirst();
                        Logger.log(LogTag.LOGIN_OUTPUT, line);

                        for (char c : line.toCharArray()) {
                            referencePane.getStringUtil().print(String.valueOf(c));
                            ThreadUtil.sleep(charTimeout);
                        }
                        semaphore.release();
                    } else if (!printingList.isEmpty()) {
                        semaphore.acquire();
                        String line = printingList.removeFirst();
                        Logger.log(LogTag.LOGIN_OUTPUT, line);

                        for (char c : line.toCharArray()) {
                            referencePane.getStringUtil().print(String.valueOf(c));
                            ThreadUtil.sleep(charTimeout);
                        }
                        semaphore.release();
                    }

                    ThreadUtil.sleep(lineTimeout);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, LOGIN_PRINTING_ANIMATION_THREAD_NAME);
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

        loginFrame = new CyderFrame(LOGIN_FRAME_WIDTH, LOGIN_FRAME_HEIGHT,
                ImageUtil.imageIconFromColor(backgroundColor));
        loginFrame.setTitle(getLoginFrameTitle());
        loginFrame.setBackground(backgroundColor);
        loginFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loginFrameClosed = true;
                doLoginAnimations = false;
                if (Console.INSTANCE.isClosed()) OsUtil.exit(ExitCondition.GenesisControlledExit);
            }

            public void windowOpened(WindowEvent e) {
                loginField.requestFocus();
            }
        });
        loginFrameClosed = false;

        loginArea = new JTextPane();
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
        loginField.setEchoChar(DEFAULT_FIELD_ECHO_CHAR);
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
        loginField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent event) {
                loginFieldEnterAction(event);
            }

            public void keyPressed(KeyEvent event) {
                fixLoginFieldCaretPosition();
            }

            public void keyReleased(KeyEvent event) {
                fixLoginFieldCaretPosition();
            }
        });

        shiftShowsPassword = CyderPasswordField.addShiftShowsPasswordListener(loginField);
        shiftShowsPassword.set(false);

        loginField.setCaretPosition(currentBashString.length());
        loginFrame.getContentPane().add(loginField);
        loginFrame.finalizeAndShow();

        CyderSplash.INSTANCE.fastDispose();
        if (UserUtil.noUsers()) printlnPriority("No users found; please type " + quote + CREATE + quote);
        startTypingAnimation();
    }

    /**
     * Checks for the caret position in the login field being before the current bash string
     */
    private static void fixLoginFieldCaretPosition() {
        if (loginMode != LoginMode.PASSWORD) {
            int caretPosition = loginField.getCaretPosition();
            int length = loginField.getPassword().length;
            String prefix = switch (loginMode) {
                case INPUT -> defaultBashString;
                case USERNAME -> usernameModePrefix;
                default -> throw new IllegalStateException("Invalid login mode: " + loginMode);
            };
            boolean beforeBashString = caretPosition < prefix.length();
            if (beforeBashString) loginField.setCaretPosition(length);
        }
    }

    /**
     * Returns the title to use for the login frame.
     *
     * @return the title to use for the login frame
     */
    @ForReadability
    public static String getLoginFrameTitle() {
        String cyderVersion = PropLoader.getString(VERSION);
        return titlePrefix + openingBracket + cyderVersion + space + "Build" + closingBracket;
    }

    /**
     * Checks for whether the backspace is being pressed and whether that will
     * result in part of the bash string being deleted.
     *
     * @param keyChar the key char being typed
     * @return whether the backspace is being pressed and whether that will
     * result in part of the bash string being deleted
     */
    @ForReadability
    private static boolean checkForBackspaceIntoBashString(char keyChar) {
        boolean backSpaceAndBashString = keyChar == KeyEvent.VK_BACK_SPACE && loginMode != LoginMode.PASSWORD;
        if (backSpaceAndBashString) {
            if (loginField.getPassword().length < currentBashString.length()) {
                loginField.setText(currentBashString);
            }

            return true;
        }

        return false;
    }

    /**
     * The actions to invoke when a key is pressed in the login field.
     *
     * @param event the key event
     */
    @ForReadability
    private static void loginFieldEnterAction(KeyEvent event) {
        char keyChar = event.getKeyChar();
        if (checkForBackspaceIntoBashString(keyChar)) {
            event.consume();
            return;
        }

        if (keyChar != newline) return;

        String userInput = "";
        char[] fieldInput = loginField.getPassword();
        if (loginMode != LoginMode.PASSWORD) {
            userInput = new String(fieldInput).substring(currentBashString.length());
            Logger.log(LogTag.LOGIN_FIELD, userInput);
        }

        switch (loginMode) {
            case INPUT -> handleInputEnter(userInput);
            case USERNAME -> handleUsernameEnter(userInput);
            case PASSWORD -> {
                handlePasswordEnter(fieldInput);
                Arrays.fill(fieldInput, '\0');
            }
            default -> {
                loginField.setText(currentBashString);
                throw new IllegalArgumentException("Error resulting from login shell default case trigger");
            }
        }
    }

    /**
     * Handles the user entering input when the login mode is {@link LoginMode#INPUT}.
     *
     * @param userInput the current field input with the bash string parsed away
     */
    private static void handleInputEnter(String userInput) {
        if (userInput.equalsIgnoreCase(CREATE)) {
            UserCreator.showGui();
            loginField.setText(currentBashString);
        } else if (userInput.equalsIgnoreCase(LOGIN)) {
            currentBashString = usernameModePrefix;
            loginField.setText(currentBashString);
            printlnPriority("Awaiting Username");
            loginMode = LoginMode.USERNAME;
        } else if (userInput.equalsIgnoreCase(QUIT)) {
            loginFrame.dispose();
        } else if (userInput.equalsIgnoreCase(HELP)) {
            loginField.setText(currentBashString);
            printlnPriority("Valid commands: ");
            validCommands.forEach(command -> LoginHandler.printlnPriority(
                    BULLET_POINT + space + command));
        } else {
            loginField.setText(currentBashString);
            printlnPriority("Unknown command; See " + quote + HELP + quote + " for " + HELP);
        }
    }

    /**
     * Handles the user entering input when the login mode is {@link LoginMode#USERNAME}.
     *
     * @param userInput the current field input with the bash string parsed away
     */
    private static void handleUsernameEnter(String userInput) {
        username = userInput;
        loginMode = LoginMode.PASSWORD;
        loginField.setEchoChar(ECHO_CHAR);
        loginField.setText("");
        printlnPriority("Awaiting Password (hold shift to reveal password)");
        currentBashString = defaultBashString;
        shiftShowsPassword.set(true);
    }

    /**
     * Handles the user entering input when the login mode is {@link LoginMode#PASSWORD}.
     *
     * @param fieldInput the current field input
     */
    private static void handlePasswordEnter(char[] fieldInput) {
        loginField.setEchoChar(DEFAULT_FIELD_ECHO_CHAR);
        shiftShowsPassword.set(false);
        loginField.setText("");
        printlnPriority("Attempting validation");

        String hashedPassword = SecurityUtil.toHexString(SecurityUtil.getSha256(fieldInput));
        if (recognize(username, hashedPassword, false)) {
            loginFrame.dispose(true);
            return;
        }

        loginField.setEchoChar(DEFAULT_FIELD_ECHO_CHAR);
        loginField.setText(currentBashString);
        loginField.setCaretPosition(currentBashString.length());

        printlnPriority("Login failed");
        loginMode = LoginMode.INPUT;
        username = "";
    }

    /**
     * Adds the provided line follows by a new line character to the priority printing list
     *
     * @param line the line to add
     */
    private static void printlnPriority(String line) {
        priorityPrintingList.add(line + newline);
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
     * Returns whether Cyder was initially started with a successful AutoCypher.
     *
     * @return whether Cyder was initially started with a successful AutoCypher
     */
    public static boolean wasStartedViaAutoCypher() {
        return startedViaAutoCypher;
    }

    /**
     * Whether Cyder was initially started with a successful AutoCypher
     */
    private static boolean startedViaAutoCypher = false;

    /**
     * Begins the login sequence to figure out how to enter Cyder and which frame to show, that of
     * the console, the login frame, or an exception pane.
     */
    public static void showProperStartupFrame() {
        boolean autoCypher = PropLoader.getBoolean(AUTOCYPHER);
        if (autoCypher) {
            // todo test for props even exiting
            String name = PropLoader.getString(DEBUG_HASH_NAME);
            String password = PropLoader.getString(DEBUG_HASH_PASSWORD);
            startedViaAutoCypher = recognize(name, password, true);
            ProgramModeManager.INSTANCE.refreshProgramMode();
            if (!startedViaAutoCypher) showGui();
            return;
        }

        boolean released = PropLoader.getBoolean(RELEASED);
        if (!released) {
            ExceptionHandler.exceptionExit("Unreleased build of Cyder", ExitCondition.NotReleased);
            return;
        }

        Optional<String> optionalUuid = UserUtil.getFirstLoggedInUser();
        if (optionalUuid.isEmpty()) {
            showGui();
            return;
        }

        String loggedInUuid = optionalUuid.get();
        Logger.log(LogTag.CONSOLE_LOAD, "Found previously logged in user: " + loggedInUuid);
        UserUtil.logoutAllUsers();
        Console.INSTANCE.setUuid(loggedInUuid);
        Console.INSTANCE.launch();
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
        recognizedUuid = "";

        PasswordCheckResult result = checkPassword(name, hashedPass);
        switch (result) {
            case FAILED -> handleFailedPasswordCheck(autoCypherAttempt);
            case UNKNOWN_USER -> printlnPriority("Unknown user");
            case SUCCESS -> {
                handleSuccessfulPasswordCheck();
                return true;
            }
            default -> throw new IllegalArgumentException("Invalid password status: " + result);
        }

        return false;
    }

    /**
     * Handles a failed check password invocation.
     *
     * @param autoCypherAttempt whether the recognize invocation was invoked with an autocypher
     */
    private static void handleFailedPasswordCheck(boolean autoCypherAttempt) {
        printlnPriority(autoCypherAttempt ? "AutoCypher failed" : "Incorrect password");
        loginField.requestFocusInWindow();
    }

    /**
     * Handles a successful check password invocation.
     */
    private static void handleSuccessfulPasswordCheck() {
        Preconditions.checkState(!recognizedUuid.isEmpty());

        Console.INSTANCE.setUuid(recognizedUuid);

        doLoginAnimations = false;

        if (!Console.INSTANCE.isClosed()) Console.INSTANCE.closeFrame(false, true);
        Console.INSTANCE.launch();
    }

    /**
     * Checks whether the given name/pass combo is valid and if so, returns the UUID matched.
     * Otherwise, an empty optional is returned to represent that no user was found.
     *
     * @param providedUsername the username given
     * @param hashedPass       the singly-hashed password
     * @return the result of checking for the a user with the provided name and password
     */
    private static PasswordCheckResult checkPassword(String providedUsername, String hashedPass) {
        ArrayList<String> names = new ArrayList<>();
        for (File userJson : UserUtil.getUserJsons()) {
            names.add(UserUtil.extractUser(userJson).getName());
        }

        boolean namePresent = StringUtil.in(providedUsername, true, names);
        if (!namePresent) return PasswordCheckResult.UNKNOWN_USER;

        for (File userJsonFile : UserUtil.getUserJsons()) {
            User user = UserUtil.extractUser(userJsonFile);
            String username = user.getName();
            String password = user.getPass();

            String providedPassword = SecurityUtil.toHexString(SecurityUtil.getSha256(hashedPass.toCharArray()));

            if (providedUsername.equalsIgnoreCase(username) && providedPassword.equals(password)) {
                recognizedUuid = userJsonFile.getParentFile().getName();
                return PasswordCheckResult.SUCCESS;
            }
        }

        return PasswordCheckResult.FAILED;
    }
}
