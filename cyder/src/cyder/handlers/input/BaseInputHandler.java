package cyder.handlers.input;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.annotations.Handle;
import cyder.console.Console;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.enums.IgnoreThread;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.BletchyThread;
import cyder.threads.CyderThreadRunner;
import cyder.threads.MasterYoutubeThread;
import cyder.threads.ThreadUtil;
import cyder.ui.CyderOutputPane;
import cyder.user.UserFile;
import cyder.user.UserUtil;
import cyder.utils.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * The base input handler used for linked JTextPane printing
 * operations and raw user input sub-handler triggering.
 */
@SuppressWarnings("SpellCheckingInspection")
public class BaseInputHandler {
    /**
     * The linked CyderOutputPane.
     */
    private final CyderOutputPane linkedOutputPane;

    /**
     * boolean describing whether to quickly append all remaining queued objects to the linked JTextPane.
     */
    private boolean shouldFinishPrinting;

    /**
     * The file to redirect the outputs of a command to if redirection is enabled.
     */
    private File redirectionFile;

    /**
     * Boolean describing whether possible command output should be redirected to the redirectionFile.
     */
    private boolean redirection;

    /**
     * The command that is being handled.
     */
    private String command;

    /**
     * The arguments of the command.
     */
    private final ArrayList<String> args = new ArrayList<>();

    /**
     * Suppress default constructor.
     */
    private BaseInputHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The robot used for screen operations.
     */
    private static Robot robot = setupRobot();

    /**
     * Constructs and returns a new robot instance.
     *
     * @return a new robot instance
     */
    private static Robot setupRobot() {
        try {
            return new Robot();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return null;
    }

    /**
     * Returns the common robot object.
     *
     * @return the common robot object
     */
    public final Robot getRobot() {
        if (robot == null) {
            robot = setupRobot();
        }

        return robot;
    }

    /**
     * Constructs a new base input handler liked to the provided {@link JTextPane}.
     *
     * @param outputArea the JTextPane object to append content to
     */
    public BaseInputHandler(JTextPane outputArea) {
        this.linkedOutputPane = new CyderOutputPane(Preconditions.checkNotNull(outputArea));
        initializeSpecialThreads();
        clearLists();
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Sets up the custom thread objects to be managed by this {@link BaseInputHandler}.
     */
    @ForReadability
    private void initializeSpecialThreads() {
        MasterYoutubeThread.initialize(linkedOutputPane);
        BletchyThread.initialize(linkedOutputPane);
    }

    /**
     * Clears the console printing lists.
     */
    @ForReadability
    private void clearLists() {
        consolePrintingList.clear();
        consolePriorityPrintingList.clear();
    }

    /**
     * The handles which contain specific triggers for pre-determined commands.
     */
    public static final ImmutableList<Class<?>> primaryHandlers = ImmutableList.of(
            PixelationHandler.class,
            GitHandler.class,
            ImageHandler.class,
            PlayAudioHandler.class,
            ColorHandler.class,
            NetworkHandler.class,
            StatHandler.class,
            NumberHandler.class,
            ThreadHandler.class,
            UiHandler.class,
            FileHandler.class,
            FrameMovementHandler.class,
            PropHandler.class
    );

    /**
     * The handles which have do not have specific triggers
     * and instead perform checks and operations on the raw command.
     */
    public static final ImmutableList<Class<?>> finalHandlers = ImmutableList.of(
            GeneralPrintHandler.class,
            WidgetHandler.class,
            MathHandler.class,
            UrlHandler.class,
            PreferenceHandler.class,
            ManualTestHandler.class,
            WrappedCommandHandler.class
    );

    /**
     * Handles the input and provides output if necessary to the linked JTextPane.
     *
     * @param op            the operation that is being handled
     * @param userTriggered whether the provided op was produced via a user
     */
    public final void handle(String op, boolean userTriggered) {
        if (!handlePreliminaries(op, userTriggered)) {
            Logger.log(Logger.Tag.HANDLE_METHOD, "Failed handle preliminaries for op: " + op);
            return;
        }

        if (redirectionHandler != null) {
            for (Method method : redirectionHandler.getMethods()) {
                if (method.isAnnotationPresent(Handle.class)) {
                    try {
                        if (method.invoke(redirectionHandler) instanceof Boolean bool && bool) {
                            return;
                        }
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }
            }
        }

        for (Class<?> handle : primaryHandlers) {
            for (Method method : handle.getMethods()) {
                if (method.isAnnotationPresent(Handle.class)) {
                    String[] triggers = method.getAnnotation(Handle.class).value();

                    for (String trigger : triggers) {
                        if (commandAndArgsToString().startsWith(trigger)) {
                            try {
                                if (method.getParameterCount() == 0) {
                                    if (method.invoke(handle) instanceof Boolean bool && bool) {
                                        return;
                                    }
                                }
                            } catch (Exception e) {
                                ExceptionHandler.handle(e);
                            }
                        }
                    }
                }
            }
        }

        for (Class<?> handle : finalHandlers) {
            for (Method method : handle.getMethods()) {
                if (method.isAnnotationPresent(Handle.class)) {
                    try {
                        if (method.getParameterCount() == 0) {
                            if (method.invoke(handle) instanceof Boolean bool && bool) {
                                return;
                            }
                        }
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }
            }
        }

        unknownInput();
    }

    /**
     * Handles preliminaries such as argument/command parsing and redirection checks
     * before passing input data to the jhandle methods.
     *
     * @param command       the command to handle preliminaries on before behind handled
     * @param userTriggered whether the provided operation was produced via a user
     * @return whether preliminary checks successfully completed
     */
    private boolean handlePreliminaries(String command, boolean userTriggered) {
        this.command = Preconditions.checkNotNull(command).trim();
        Preconditions.checkNotNull(linkedOutputPane);

        resetMembers();

        if (StringUtil.isNullOrEmpty(this.command)) {
            return false;
        }

        String commandAndArgsToString = commandAndArgsToString();
        Logger.log(Logger.Tag.CLIENT,
                (userTriggered ? "" : "[SIMULATED INPUT]: ") + commandAndArgsToString);

        if (UserUtil.getCyderUser().getFilterchat().equals("1")) {
            StringUtil.BlockedWordResult result = checkFoulLanguage();
            if (result.failed()) {
                println("Sorry, " + UserUtil.getCyderUser().getName() + ", but that language"
                        + " is prohibited,  word: " + result.triggerWord());
                return false;
            }
        }

        parseArgsFromCommand();
        redirectionCheck();

        return true;
    }

    /**
     * Parses the current command into arguments and a command.
     */
    private void parseArgsFromCommand() {
        String[] parts = command.split(CyderRegexPatterns.whiteSpaceRegex);
        if (parts.length > 1) {
            args.clear();
            args.addAll(Arrays.asList(parts).subList(1, parts.length));
        }
    }

    /**
     * Checks for whether the provided string contains blocked words.
     *
     * @return the blocked word if found
     */
    private StringUtil.BlockedWordResult checkFoulLanguage() {
        return StringUtil.containsBlockedWords(command, true);
    }

    /**
     * Resets redirection, redirection file, and the arguments array.
     */
    private void resetMembers() {
        redirection = false;
        redirectionFile = null;
        args.clear();
    }

    /**
     * Checks for a requested redirection and attempts to create the file if valid.
     */
    private void redirectionCheck() {
        if (args.size() < 2) return;
        String secondToLastArg = args.get(args.size() - 2);
        if (!secondToLastArg.equalsIgnoreCase(">")) return;

        String requestedFilename = args.get(args.size() - 1);

        if (!OSUtil.isValidFilename(requestedFilename)) {
            failedRedirection();
            return;
        }

        redirection = true;

        try {
            redirectionSem.acquire();

            redirectionFile = OSUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(),
                    Console.INSTANCE.getUuid(), UserFile.FILES.getName(), requestedFilename).getAbsoluteFile();

            if (redirectionFile.exists()) {
                OSUtil.deleteFile(redirectionFile);
            }

            if (!OSUtil.createFile(redirectionFile, true)) {
                failedRedirection();
            }
        } catch (Exception ignored) {
            failedRedirection();
        } finally {
            redirectionSem.release();
        }
    }

    /**
     * Handles a failed redirection attempt.
     */
    private void failedRedirection() {
        redirection = false;
        redirectionFile = null;

        println("Error: could not redirect output");
    }

    /**
     * The tolerance value that the similar command function must be at or above
     * to be passed off as a legit recommendation.
     */
    private static final float SIMILAR_COMMAND_TOL = 0.80f;

    /**
     * The key for getting the tolerance value from the props.
     */
    private static final String AUTO_TRIGGER_SIMILAR_COMMAND_TOLERANCE_KEY = "auto_trigger_similar_command_tolerance";

    /**
     * The name of the thread for handling unknown input.
     */
    private static final String UNKNOWN_INPUT_HANDLER_THREAD_NAME = "Unknown Input Handler";

    /**
     * The key for whether a command should be auto-invoked if the specified tolerance is met.
     */
    private static final String AUTO_TRIGGER_SIMILAR_COMMANDS_KEY = "auto_trigger_similar_commands";

    /**
     * The final handle method for if all other handle methods failed.
     */
    private void unknownInput() {
        CyderThreadRunner.submit(() -> {
            ReflectionUtil.SimilarCommand similarCommandObj = ReflectionUtil.getSimilarCommand(command);
            boolean wrapShell = UserUtil.getCyderUser().getWrapshell().equalsIgnoreCase("1");

            if (similarCommandObj.command().isPresent()) {
                String similarCommand = similarCommandObj.command().get();
                double tolerance = similarCommandObj.tolerance();
                if (tolerance == 1.0) return;

                if (!StringUtil.isNullOrEmpty(similarCommand)) {
                    Logger.log(Logger.Tag.DEBUG, "Similar command to \""
                            + command + "\" found with tolerance of " + tolerance
                            + ", command = \"" + similarCommand + "\"");

                    if (!wrapShell) {
                        boolean autoTrigger = PropLoader.getBoolean(AUTO_TRIGGER_SIMILAR_COMMANDS_KEY);
                        boolean toleranceMet =
                                tolerance >= PropLoader.getFloat(AUTO_TRIGGER_SIMILAR_COMMAND_TOLERANCE_KEY);

                        if (tolerance >= SIMILAR_COMMAND_TOL) {
                            if (autoTrigger && toleranceMet) {
                                println(UNKNOWN_COMMAND + "; Invoking similar command: \"" + similarCommand + "\"");
                                handle(similarCommand, false);
                            } else {
                                println(UNKNOWN_COMMAND + "; Most similar command: \"" + similarCommand + "\"");
                            }

                            return;
                        }
                    }
                }
            }

            if (wrapShell) {
                wrapShellLogic();
            } else {
                println(UNKNOWN_COMMAND);
            }
        }, UNKNOWN_INPUT_HANDLER_THREAD_NAME);
    }

    /**
     * The logic performed when it is known that a wrap shell action should be taken.
     */
    @ForReadability
    private void wrapShellLogic() {
        println(UNKNOWN_COMMAND + ", passing to operating system native shell (" + OSUtil.getShellName() + ")");

        CyderThreadRunner.submit(() -> {
            try {
                Process process = createAndStartWrapShellProcess();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                process.waitFor();

                while ((line = reader.readLine()) != null) {
                    println(line);

                    if (escapeWrapShell) {
                        process.destroy();
                        break;
                    }
                }

                escapeWrapShell = false;
            } catch (Exception ignored) {
                println(UNKNOWN_COMMAND);
            }
        }, WRAP_SHELL_THREAD_NAME);
    }

    @ForReadability
    private Process createAndStartWrapShellProcess() throws IOException {
        LinkedList<String> processArgs = new LinkedList<>(args);
        processArgs.add(0, command);
        ProcessBuilder builder = new ProcessBuilder(processArgs);
        builder.redirectErrorStream(true);
        return builder.start();
    }

    /**
     * Used to escape the terminal wrapper.
     */
    private boolean escapeWrapShell;

    /**
     * The name of the thread when wrapping the shell
     */
    private static final String WRAP_SHELL_THREAD_NAME = "Wrap Shell Thread";

    /**
     * The text to print for an unknown command.
     */
    private static final String UNKNOWN_COMMAND = "Unknown command";

    /**
     * Returns the output area's {@link JTextPane}.
     *
     * @return the output area's {@link JTextPane}
     */
    public final JTextPane getJTextPane() {
        return linkedOutputPane.getJTextPane();
    }

    /**
     * Ends any custom threads such as YouTube or bletchy
     * that may have been invoked via this input handler.
     */
    public final void killThreads() {
        MasterYoutubeThread.killAll();
        BletchyThread.kill();
    }

    /**
     * Semaphore for adding objects to both consolePrintingList and consolePriorityPrintingList.
     */
    private final Semaphore printingListLock = new Semaphore(1);

    /**
     * Acquires the printingListLock.
     */
    @ForReadability
    private void lockLists() {
        try {
            printingListLock.acquire();
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }

    /**
     * Releases the printingListLock.
     */
    @ForReadability
    private void unlockLists() {
        printingListLock.release();
    }

    /**
     * The printing list for non-important outputs.
     * DO NOT ADD DIRECTLY TO THIS LIST UNLESS YOU ARE A PRINT METHOD.
     */
    private final LinkedList<Object> consolePrintingList = new LinkedList<>() {
        @Override
        public boolean add(Object e) {
            lockLists();
            boolean ret = super.add(e);
            startConsolePrintingAnimationIfNeeded();
            unlockLists();
            return ret;
        }
    };

    /*
    Note to maintainers: these lists are anonymously declared to allow for their add methods
    to have additional functionality such as thread-safety via the semaphore.
     */

    /**
     * The priority printing list for important outputs.
     * DO NOT ADD DIRECTLY TO THIS LIST UNLESS YOU ARE A PRINT METHOD.
     */
    private final LinkedList<Object> consolePriorityPrintingList = new LinkedList<>() {
        @Override
        public boolean add(Object e) {
            lockLists();
            boolean ret = super.add(e);
            startConsolePrintingAnimationIfNeeded();
            unlockLists();
            return ret;
        }
    };

    /**
     * Whether the printing animation thread is running
     */
    private boolean printingAnimationRunning;

    /**
     * Begins the typing animation for the Console if it has not already started.
     */
    private void startConsolePrintingAnimationIfNeeded() {
        if (printingAnimationRunning) return;
        CyderThreadRunner.submit(consolePrintingRunnable, IgnoreThread.ConsolePrintingAnimation.getName());
    }

    /**
     * Returns whether both the printing lists are empty.
     *
     * @return whether both the printing lists are empty
     */
    @ForReadability
    private boolean listsEmpty() {
        return consolePrintingList.isEmpty() && consolePriorityPrintingList.isEmpty();
    }

    /**
     * The delay between updating the value of typing animation from the current user's userdata.
     */
    private static final int USER_DATA_POLL_FREQUENCY_MS = 3000;

    /**
     * The key for getting the timeout between printing lines from the props file.
     */
    private static final String PRINATING_ANIMATION_LINE_KEY = "printing_animation_line_timeout";

    /**
     * Returns whether the typing animation should be performed.
     *
     * @return whether the typing animation should be performed
     */
    @ForReadability
    private boolean shouldDoTypingAnimation() {
        return UserUtil.getCyderUser().getTypinganimation().equals("1");
    }

    /**
     * The console printing animation runnable.
     */
    private final Runnable consolePrintingRunnable = () -> {
        try {
            printingAnimationRunning = true;

            boolean shouldDoTypingAnimation = shouldDoTypingAnimation();
            boolean shouldDoTypingSound = shouldDoTypingSound();
            long lastPull = System.currentTimeMillis();
            int lineTimeout = PropLoader.getInteger(PRINATING_ANIMATION_LINE_KEY);

            while (!Console.INSTANCE.isClosed() && !listsEmpty()) {
                if (System.currentTimeMillis() - lastPull > USER_DATA_POLL_FREQUENCY_MS) {
                    lastPull = System.currentTimeMillis();
                    shouldDoTypingAnimation = shouldDoTypingAnimation();
                    shouldDoTypingSound = shouldDoTypingSound();
                }

                if (!consolePriorityPrintingList.isEmpty()) {
                    Object line = removeAndLog(consolePriorityPrintingList);

                    if (redirection) {
                        redirectionWrite(line);
                    } else {
                        switch (line) {
                            case JComponent jComponent -> insertJComponent(jComponent);
                            case ImageIcon imageIcon -> insertImageIcon(imageIcon);
                            case default -> insertAsString(line);
                        }
                    }
                } else if (!consolePrintingList.isEmpty()) {
                    Object line = removeAndLog(consolePrintingList);

                    if (redirection) {
                        redirectionWrite(line);
                    } else {
                        switch (line) {
                            case String string:
                                if (shouldDoTypingAnimation) {
                                    if (shouldFinishPrinting) {
                                        insertAsString(string);
                                    } else {
                                        innerPrintString(string, shouldDoTypingSound);
                                    }
                                } else {
                                    insertAsString(line);
                                }
                                break;
                            case JComponent jComponent:
                                insertJComponent(jComponent);
                                break;
                            case ImageIcon imageIcon:
                                insertImageIcon(imageIcon);
                                break;
                            default:
                                insertAsString(line);
                                break;
                        }
                    }
                } else {
                    shouldFinishPrinting = false;
                }

                if (!shouldFinishPrinting && shouldDoTypingAnimation) ThreadUtil.sleep(lineTimeout);
            }

            printingAnimationRunning = false;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    };

    /**
     * Removes, logs, and returns the first element from the provided list.
     *
     * @param list the list to perform the operations on
     * @return the object removed from the list
     */
    private Object removeAndLog(LinkedList<Object> list) {
        Preconditions.checkNotNull(list);
        Preconditions.checkArgument(!list.isEmpty());

        Object ret = list.removeFirst();
        Logger.log(Logger.Tag.CONSOLE_OUT, ret);
        return ret;
    }

    // -----------------------
    // Document insert methods
    // -----------------------

    /**
     * Inserts the provided object into the current outputArea.
     *
     * @param object the object to insert
     */
    private void insertAsString(Object object) {
        Preconditions.checkNotNull(object);

        StyledDocument document = (StyledDocument) getJTextPane().getDocument();

        try {
            document.insertString(document.getLength(), String.valueOf(object), null);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        getJTextPane().setCaretPosition(getJTextPane().getDocument().getLength());
    }

    /**
     * Inserts the provided component into the current outputArea.
     *
     * @param component the component to insert
     */
    private void insertJComponent(JComponent component) {
        Preconditions.checkNotNull(component);

        String componentUuid = SecurityUtil.generateUuid();
        Style cs = getJTextPane().getStyledDocument().addStyle(componentUuid, null);
        StyleConstants.setComponent(cs, component);

        try {
            getJTextPane().getStyledDocument().insertString(
                    getJTextPane().getStyledDocument().getLength(), componentUuid, cs);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Inserts the provided image icon into the current outputArea.
     *
     * @param imageIcon the iamge icon to insert
     */
    private void insertImageIcon(ImageIcon imageIcon) {
        Preconditions.checkNotNull(imageIcon);
        Preconditions.checkNotNull(getJTextPane());

        getJTextPane().insertIcon(imageIcon);
    }

    // ---------------------------
    // End document insert methods
    // ---------------------------

    /**
     * The frequency at which to play a typing sound effect if enabled.
     */
    private static final int TYPING_SOUND_FREQUENCY = 2;

    /**
     * The increment we are currently on for inner char printing.
     * Used to determine when to play a typing animation sound.
     */
    private int typingSoundInc;

    /**
     * The path to the typing sound effect.
     */
    private final String typingSoundPath = StaticUtil.getStaticPath("typing.mp3");

    /**
     * The key for getting the timeout between printing characters
     * to the output area if printing animation is enabled.
     */
    private static final String PRINTING_ANIMATION_CHAR_TIMEOUT_KEY = "printing_animation_char_timeout";

    /**
     * Returns whether the current user has typing sound enabled.
     *
     * @return whether the current user has typing sound enabled
     */
    @ForReadability
    private boolean shouldDoTypingSound() {
        return UserUtil.getCyderUser().getTypingsound().equals("1");
    }

    /**
     * Prints the string to the output area checking for
     * typing sound, finish printing, and other parameters.
     * <p>
     * Note: this method is blocking and SHOULD NOT be used as a
     * substitute for the default print/println methods.
     *
     * @param line        the string to append to the output area
     * @param typingSound whether the typing sound should be played
     */
    private void innerPrintString(String line, boolean typingSound) {
        Preconditions.checkNotNull(line);

        try {
            linkedOutputPane.getSemaphore().acquire();

            for (char c : line.toCharArray()) {
                String character = String.valueOf(c);
                String insertCharacter = UserUtil.getCyderUser().getCapsmode().equals("1")
                        ? character.toUpperCase() : character;

                StyledDocument document = (StyledDocument) getJTextPane().getDocument();

                document.insertString(document.getLength(), insertCharacter, null);

                getJTextPane().setCaretPosition(getJTextPane().getDocument().getLength());

                if (typingSoundInc == TYPING_SOUND_FREQUENCY) {
                    if (!shouldFinishPrinting && typingSound) {
                        IOUtil.playSystemAudio(typingSoundPath, false);
                        typingSoundInc = 0;
                    }
                } else {
                    typingSoundInc++;
                }

                if (!shouldFinishPrinting) {
                    ThreadUtil.sleep(PropLoader.getInteger(PRINTING_ANIMATION_CHAR_TIMEOUT_KEY));
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            linkedOutputPane.getSemaphore().release();
        }
    }

    // -----------------------
    // document entity removal
    // -----------------------

    private static final String ICON = "icon";
    private static final String COMPONENT = "component";

    /**
     * Removes the last entity added to the JTextPane whether it's a component,
     * icon, or string of multi-lined text.
     * <p>
     * In more detail, this method figures out what it'll be removing and then determines how many calls
     * are needed to {@link StringUtil#removeLastLine()}
     */
    public final void removeLastEntity() {
        try {
            boolean removeTwoLines = false;

            LinkedList<Element> elements = new LinkedList<>();
            ElementIterator iterator = new ElementIterator(getJTextPane().getStyledDocument());
            Element element;
            while ((element = iterator.next()) != null) {
                elements.add(element);
            }

            int leafs = 0;

            for (Element value : elements) {
                if (value.getElementCount() == 0) {
                    leafs++;
                }
            }

            int passedLeafs = 0;

            for (Element value : elements) {
                if (value.getElementCount() == 0) {
                    if (passedLeafs + 3 != leafs) {
                        passedLeafs++;
                        continue;
                    }

                    if (value.toString().toLowerCase().contains(ICON)
                            || value.toString().toLowerCase().contains(COMPONENT)) {
                        removeTwoLines = true;
                    }
                }
            }

            linkedOutputPane.getSemaphore().acquire();

            if (removeTwoLines) {
                removeLastLine();
            }

            removeLastLine();

            linkedOutputPane.getSemaphore().release();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns the last line of text on the linked JTextPane.
     * Text is easier to get and return as opposed to general components.
     *
     * @return the last line of text on the linked JTextPane
     */
    public final String getLastTextLine() {
        return linkedOutputPane.getStringUtil().getLastTextLine();
    }

    /**
     * Removes the last line added to the linked JTextPane. This could appear to remove nothing,
     * but really be removing just a newline (line break) character.
     */
    private void removeLastLine() {
        linkedOutputPane.getStringUtil().removeLastLine();
    }

    // -----------------
    // Redirection logic
    // -----------------

    /**
     * Semaphore used to ensure all things that need to be written to the redirectionFile are written to it.
     * This also ensures that multiple redirections aren't performed at the same time.
     */
    private final Semaphore redirectionSem = new Semaphore(1);

    /**
     * The error message to print if redirection fails.
     */
    private static final String REDIRECTION_ERROR_MESSAGE = "Could not redirect output";

    /**
     * Writes the provided object to the redirection file instead of the JTextPane.
     *
     * @param object the object to invoke toString() on and write to the current redirectionFile
     */
    private void redirectionWrite(Object object) {
        Preconditions.checkNotNull(object);

        if (!redirectionFile.exists()) {
            if (!OSUtil.createFile(redirectionFile, true)) {
                println(REDIRECTION_ERROR_MESSAGE);
                println(object);
                return;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(redirectionFile, true))) {
            redirectionSem.acquire();
            writer.write(String.valueOf(object));
            Logger.log(Logger.Tag.CONSOLE_REDIRECTION, redirectionFile);
            redirectionSem.release();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    private static final String ESCAPED = "Escaped";

    /**
     * Stops all threads invoked, sets the userInputMode to false,
     * stops any audio playing, and finishes printing anything in the printing lists.
     */
    public final void escapeThreads() {
        killThreads();
        escapeWrapShell = true;
        IOUtil.stopGeneralAudio();
        Console.INSTANCE.stopDancing();
        shouldFinishPrinting = true;
        println(ESCAPED);
        resetHandlers();
    }

    /**
     * Resets the handle iterations and redirection handler.
     */
    public void resetHandlers() {
        handleIterations = 0;
        redirectionHandler = null;
    }

    /**
     * The iteration the current handler is on.
     */
    private int handleIterations = 0;

    /**
     * Returns the current handle iteration.
     *
     * @return the current handle iteration
     */
    public int getHandleIterations() {
        return handleIterations;
    }

    /**
     * Sets the current handle iteration.
     *
     * @param handleIterations the current handle iteration
     */
    public void setHandleIterations(int handleIterations) {
        this.handleIterations = handleIterations;
    }

    /**
     * The current handler to send the input to.
     */
    private Class<?> redirectionHandler;

    /**
     * Returns the current redirection handler.
     *
     * @return the current redirection handler
     */
    public Class<?> getRedirectionHandler() {
        return redirectionHandler;
    }

    /**
     * Sets the current redirection handler.
     *
     * @param redirectionHandler the current redirection handler
     */
    public void setRedirectionHandler(Class<?> redirectionHandler) {
        Preconditions.checkNotNull(redirectionHandler);

        this.redirectionHandler = redirectionHandler;
    }

    /**
     * Returns the current user issued command.
     *
     * @return the current user issued command
     */
    public final String getCommand() {
        return command;
    }

    /**
     * Determines if the current command equals the provided text ignoring case.
     *
     * @param compare the string to check for case-insensitive equality to command
     * @return if the current command equals the provided text ignoring case
     */
    protected boolean commandIs(String compare) {
        Preconditions.checkNotNull(compare);

        return command.equalsIgnoreCase(compare);
    }

    /**
     * Returns whether the arguments array contains the expected number of arguments.
     * For example, if the user entered "consolidate windows middle" the command is "consolidate"
     * and the args are "windows" and "middle".
     *
     * @param expectedSize the expected size of the command arguments
     * @return whether the arguments array contains the expected number of arguments
     */
    protected boolean checkArgsLength(int expectedSize) {
        return args.size() == expectedSize;
    }

    /**
     * Returns the command argument at the provided index.
     * Returns null if the index is out of bounds instead of throwing.
     *
     * @param index the index to retrieve the command argument of
     * @return the command argument at the provided index
     */
    protected String getArg(int index) {
        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index < args.size());

        return args.get(index);
    }

    /**
     * Returns the size of the arguments list.
     *
     * @return the size of the arguments list
     */
    protected int getArgsSize() {
        return args.size();
    }

    /**
     * Returns the arguments in String form separated by spaces.
     *
     * @return the arguments in String form separated by spaces
     */
    protected String argsToString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0 ; i < args.size() ; i++) {
            sb.append(args.get(i));

            if (i != args.size() - 1) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    /**
     * Returns the original user input, that of the command followed by the arguments.
     *
     * @return the original user input, that of the command followed by the arguments
     */
    protected String commandAndArgsToString() {
        return (command.trim() + " " + argsToString()).trim();
    }

    /**
     * Returns whether the provided string matches the command and
     * arguments strung together with whitespace removed.
     *
     * @param match the string to match to
     * @return whether the provided string matched the command args with whitespace removed
     */
    protected boolean inputIgnoringSpacesMatches(String match) {
        Preconditions.checkNotNull(match);

        return match.replaceAll(CyderRegexPatterns.whiteSpaceRegex, "").equalsIgnoreCase(
                commandAndArgsToString().replaceAll(CyderRegexPatterns.whiteSpaceRegex, ""));
    }

    // ---------------------
    // Generic print methods
    // ---------------------

    /**
     * Prints the provided tee.
     *
     * @param tee the tee to print
     */
    public final <T> void print(T tee) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive()) {
            consolePriorityPrintingList.add(tee);
        } else {
            consolePrintingList.add(tee);
        }
    }

    /**
     * Prints the provided tee followed by a newline.
     *
     * @param tee the tee to print
     */
    public final <T> void println(T tee) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive()) {
            consolePriorityPrintingList.add(tee + "\n");
        } else {
            consolePrintingList.add(tee + "\n");
        }
    }

    /**
     * Adds the provided tee to the priority printing list.
     *
     * @param tee the tee to add to the priority printing list
     */
    public final <T> void printPriority(T tee) {
        consolePriorityPrintingList.add(tee);
    }

    /**
     * Adds the provided tee and a newline to the priority printing list.
     *
     * @param tee the tee to add to the priority printing list
     */
    public final <T> void printlnPriority(T tee) {
        consolePriorityPrintingList.add(tee + "\n");
    }

    /**
     * Prints the provided String lines to the linked JTextPane.
     * Note that new lines are automatically added in this so the passed
     * array may be strings that do not end with new lines.
     *
     * @param lines the lines to print to the JTextPane
     */
    public final synchronized void printlns(String[] lines) {
        for (String line : lines) {
            println(line);
        }
    }

    /**
     * Prints the provided String lines to the linked JTextPane.
     * Note that new lines are automatically added in this so the passed
     * array may be strings that do not end with new lines.
     *
     * @param lines the lines to print to the JTextPane
     */
    public final synchronized void printlns(List<String> lines) {
        for (String line : lines) {
            println(line);
        }
    }
}