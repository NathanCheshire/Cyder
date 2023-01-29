package cyder.login;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.annotations.Widget;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.enums.ExitCondition;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.managers.CyderVersionManager;
import cyder.managers.ProgramModeManager;
import cyder.meta.CyderSplash;
import cyder.props.Props;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.field.CyderCaret;
import cyder.ui.field.CyderPasswordField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.pane.CyderOutputPane;
import cyder.ui.pane.CyderScrollPane;
import cyder.user.User;
import cyder.user.UserUtil;
import cyder.user.creation.UserCreator;
import cyder.utils.ArrayUtil;
import cyder.utils.ImageUtil;
import cyder.utils.OsUtil;
import cyder.utils.SecurityUtil;

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
import java.util.concurrent.atomic.AtomicBoolean;

import static cyder.strings.CyderStrings.*;

/**
 * A widget to log into Cyder or any other way that the Console might be invoked.
 */
public final class LoginHandler {
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
    private static final String defaultBashString = OsUtil.getOsUsername() + "@" + OsUtil.getComputerName() + ":~$ ";

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
     * The echo char to use when a password field's text should be visible and not obfuscated.
     */
    private static final char DEFAULT_FIELD_ECHO_CHAR = (char) 0;

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
    private static final String LOGIN_PRINTING_ANIMATION_THREAD_NAME = "Login Printing Animation";

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
    private static final String usernameModePrefix = "Username" + colon + space;

    /**
     * The prefix for the login frame title.
     */
    private static final String titlePrefix = "Cyder Login" + space;

    /**
     * The uuid for the user attempting to log in.
     */
    private static String recognizedUuid = "";

    /**
     * Whether Cyder was initially started with a successful AutoCypher
     */
    private static boolean startedViaAutoCypher = false;

    /**
     * The list of standard prints to print to the login output pane.
     */
    private static final ImmutableList<String> standardPrints = ImmutableList.of(
            "Cyder version: " + CyderVersionManager.INSTANCE.getVersion() + newline,
            "Type " + quote + HELP + quote + " for a list of valid commands" + newline,
            "Build: " + CyderVersionManager.INSTANCE.getReleaseDate() + newline,
            "Author: Nate Cheshire" + newline,
            "Description: A programmer's swiss army knife" + newline
    );

    /**
     * Suppress default constructor.
     */
    private LoginHandler() {
        throw new IllegalMethodException(ATTEMPTED_INSTANTIATION);
    }

    // todo perhaps we can encapsulate printing animation logic in a cyder output pane?

    /**
     * Begins the login typing animation and printing thread.
     */
    private static void startTypingAnimation() {
        doLoginAnimations = true;
        printingList.clear();

        printingList.addAll(standardPrints);

        CyderOutputPane outputPane = new CyderOutputPane(loginArea);

        CyderThreadRunner.submit(() -> {
            try {
                while (doLoginAnimations && loginFrame != null) {
                    if (!priorityPrintingList.isEmpty()) {
                        if (!outputPane.acquireLock()) {
                            throw new FatalException("Failed to acquire output pane lock");
                        }

                        String line = priorityPrintingList.removeFirst();
                        Logger.log(LogTag.LOGIN_OUTPUT, line);

                        ArrayUtil.toList(line.toCharArray()).forEach(charizard -> {
                            outputPane.getStringUtil().print(String.valueOf(charizard));
                            ThreadUtil.sleep(charTimeout);
                        });

                        outputPane.releaseLock();
                    } else if (!printingList.isEmpty()) {
                        if (!outputPane.acquireLock()) {
                            throw new FatalException("Failed to acquire output pane lock");
                        }

                        String line = printingList.removeFirst();
                        Logger.log(LogTag.LOGIN_OUTPUT, line);

                        ArrayUtil.toList(line.toCharArray()).forEach(charizard -> {
                            outputPane.getStringUtil().print(String.valueOf(charizard));
                            ThreadUtil.sleep(charTimeout);
                        });

                        outputPane.releaseLock();
                    }

                    ThreadUtil.sleep(lineTimeout);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, LOGIN_PRINTING_ANIMATION_THREAD_NAME);
    }

    /**
     * Shows the login frame
     */
    @Widget(triggers = {"login", "pin"}, description = "A widget to switch between Cyder users")
    public static void showGui() {
        showGui(null);
    }

    /**
     * Shows the login frame relative to the provided point.
     *
     * @param centerPoint the point to place the center of the login frame at
     */
    public static void showGui(Point centerPoint) {
        priorityPrintingList.clear();
        printingList.clear();
        loginMode = LoginMode.INPUT;

        if (loginFrame != null) {
            loginFrame.removePostCloseActions();
            loginFrame.dispose(true);
        }

        loginFrame = new CyderFrame(LOGIN_FRAME_WIDTH, LOGIN_FRAME_HEIGHT,
                ImageUtil.imageIconFromColor(backgroundColor));
        String title = titlePrefix + openingBracket + CyderVersionManager.INSTANCE.getVersion()
                + space + "Build" + closingBracket;
        loginFrame.setTitle(title);
        loginFrame.setBackground(backgroundColor);
        loginFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loginFrameClosed = true;
                doLoginAnimations = false;
                if (Console.INSTANCE.isClosed()) {
                    OsUtil.exit(ExitCondition.StandardControlledExit);
                }
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
            @Override
            public void keyTyped(KeyEvent event) {
                loginFieldEnterAction(event);
            }

            @Override
            public void keyPressed(KeyEvent event) {
                correctLoginFieldCaretPosition();
            }

            @Override
            public void keyReleased(KeyEvent event) {
                correctLoginFieldCaretPosition();
            }
        });

        shiftShowsPassword = CyderPasswordField.addShiftShowsPasswordListener(loginField);
        shiftShowsPassword.set(false);

        loginField.setCaretPosition(currentBashString.length());
        loginFrame.getContentPane().add(loginField);
        if (centerPoint == null) {
            loginFrame.finalizeAndShow();
        } else {
            loginFrame.finalizeAndShow(centerPoint);
        }

        CyderSplash.INSTANCE.fastDispose();
        if (UserUtil.noCyderUsers()) printlnPriority("No users found; please type " + quote + CREATE + quote);
        startTypingAnimation();
    }

    /**
     * Checks for the caret position in the login field being before the current bash string
     */
    private static void correctLoginFieldCaretPosition() {
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

        if (keyChar != newline.charAt(0)) {
            return;
        }

        String userInput = "";
        char[] fieldInput = loginField.getPassword();
        if (loginMode != LoginMode.PASSWORD) {
            userInput = new String(fieldInput).substring(currentBashString.length());
            Logger.log(LogTag.LOGIN_INPUT, userInput);
        }

        switch (loginMode) {
            case INPUT -> onInputEntered(userInput);
            case USERNAME -> onUsernameEntered(userInput);
            case PASSWORD -> onPasswordEntered(fieldInput);
        }

        Arrays.fill(fieldInput, '\0');
    }

    /**
     * Handles the user entering input when the login mode is {@link LoginMode#INPUT}.
     *
     * @param userInput the current field input with the bash string parsed away
     */
    private static void onInputEntered(String userInput) {
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
    private static void onUsernameEntered(String userInput) {
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
    private static void onPasswordEntered(char[] fieldInput) {
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
     * Begins the login sequence to figure out how to enter Cyder and which frame to show, that of
     * the console, the login frame, or an exception pane.
     */
    public static void showProperStartupFrame() {
        boolean autoCypher = Props.autocypher.getValue();

        if (autoCypher) {
            boolean debugHashNamePresent = Props.autocypherName.valuePresent();
            boolean debugHashPasswordPresent = Props.autocypherPassword.valuePresent();

            if (debugHashNamePresent && debugHashPasswordPresent) {
                String name = Props.autocypherName.getValue();
                String password = Props.autocypherPassword.getValue();

                startedViaAutoCypher = recognize(name, password, true);
                ProgramModeManager.INSTANCE.refreshProgramMode();
                if (startedViaAutoCypher) return;
            }

            showGui();
            return;
        }

        boolean released = CyderVersionManager.INSTANCE.isReleased();
        if (!released) {
            ExceptionHandler.exceptionExit("Unreleased build of Cyder", ExitCondition.NotReleased);
            return;
        }

        Optional<String> optionalUuid = UserUtil.getMostRecentLoggedInUser();
        if (optionalUuid.isPresent()) {
            String loggedInUuid = optionalUuid.get();
            Logger.log(LogTag.CONSOLE_LOAD, "Found previously logged in user: " + loggedInUuid);
            UserUtil.logoutAllUsers();
            Console.INSTANCE.initializeAndLaunch(loggedInUuid);
            return;
        }

        showGui();
    }

    /**
     * Rests the state variables prior to the logic of {@link #recognize(String, String, boolean)}.
     */
    private static void resetRecognitionVars() {
        recognizedUuid = "";
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
        resetRecognitionVars();

        switch (validateUsernamePassword(name, hashedPass)) {
            case FAILED -> {
                printlnPriority(autoCypherAttempt ? "AutoCypher failed" : "Incorrect password");
                loginField.requestFocusInWindow();
                return false;
            }
            case UNKNOWN_USER -> {
                printlnPriority("Unknown user");
                return false;
            }
            case SUCCESS -> {
                Preconditions.checkState(!recognizedUuid.isEmpty());

                if (!Console.INSTANCE.isClosed()) {
                    Console.INSTANCE.releaseResourcesAndCloseFrame(false);
                    Console.INSTANCE.logoutCurrentUser();
                }

                doLoginAnimations = false;

                Console.INSTANCE.initializeAndLaunch(recognizedUuid);

                return true;
            }
            default -> throw new IllegalArgumentException("Invalid password status");
        }
    }

    /**
     * Checks whether the given name/pass combo is valid and returns the result of the check.
     *
     * @param providedUsername     the username given
     * @param singlyHashedPassword the singly-hashed password
     * @return the result of checking for the a user with the provided name and password
     */
    private static PasswordCheckResult validateUsernamePassword(String providedUsername, String singlyHashedPassword) {
        ArrayList<String> names = new ArrayList<>();
        UserUtil.getUserJsons().forEach(userJson -> names.add(UserUtil.extractUser(userJson).getUsername()));

        if (names.stream().map(String::toLowerCase).noneMatch(name -> name.equalsIgnoreCase(providedUsername))) {
            return PasswordCheckResult.UNKNOWN_USER;
        }

        for (File userJsonFile : UserUtil.getUserJsons()) {
            User user = UserUtil.extractUser(userJsonFile);
            if (providedUsername.equalsIgnoreCase(user.getUsername())
                    && SecurityUtil.hashAndHex(singlyHashedPassword).equals(user.getPassword())) {
                recognizedUuid = userJsonFile.getParentFile().getName();
                return PasswordCheckResult.SUCCESS;
            }
        }

        return PasswordCheckResult.FAILED;
    }
}
