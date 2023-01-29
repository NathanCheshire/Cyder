package cyder.handlers.input;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.ClassPath;
import cyder.annotations.ForReadability;
import cyder.annotations.Handle;
import cyder.annotations.Widget;
import cyder.audio.GeneralAndSystemAudioPlayer;
import cyder.console.Console;
import cyder.constants.CyderRegexPatterns;
import cyder.enums.Dynamic;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.*;
import cyder.ui.pane.CyderOutputPane;
import cyder.user.UserDataManager;
import cyder.user.UserFile;
import cyder.utils.OsUtil;
import cyder.utils.ReflectionUtil;
import cyder.utils.SecurityUtil;
import cyder.utils.StaticUtil;
import cyder.youtube.YouTubeDownloadManager;
import org.apache.commons.text.similarity.JaroWinklerDistance;

import javax.swing.*;
import javax.swing.text.ElementIterator;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The base input handler used for linked JTextPane printing
 * operations and raw user input sub-handler triggering.
 */
@SuppressWarnings("SpellCheckingInspection") /* Cyder specific words */
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
     * The handles which contain specific triggers for pre-determined commands.
     */
    public static final ImmutableList<Class<? extends InputHandler>> primaryHandlers = ImmutableList.of(
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
    public static final ImmutableList<Class<? extends InputHandler>> finalHandlers = ImmutableList.of(
            GeneralPrintHandler.class,
            WidgetHandler.class,
            MathHandler.class,
            UrlHandler.class,
            UserDataHandler.class,
            GuiTestHandler.class,
            TestHandler.class,
            WrappedCommandHandler.class
    );

    /**
     * Suppress default constructor.
     */
    private BaseInputHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Constructs a new base input handler liked to the provided {@link JTextPane}.
     *
     * @param outputArea the JTextPane object to append content to
     */
    public BaseInputHandler(JTextPane outputArea) {
        Preconditions.checkNotNull(outputArea);

        this.linkedOutputPane = new CyderOutputPane(outputArea);
        initializeSpecialThreads();
        clearLists();
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Releases resources acquired and initialized via the constructor.
     */
    public void deactivate() {
        deactivateSpecialThreads();
        killThreads();
    }

    /**
     * Sets up the custom thread objects to be managed by this {@link BaseInputHandler}, that of the following:
     * <ul>
     *     <li>{@link YoutubeUuidCheckerManager}</li>
     *     <li>{@link BletchyAnimationManager}</li>
     * </ul>
     */
    private void initializeSpecialThreads() {
        YoutubeUuidCheckerManager.INSTANCE.initialize(linkedOutputPane);
        BletchyAnimationManager.INSTANCE.initialize(linkedOutputPane);
    }

    /**
     * Releases the resources initialied by the {@link #initializeSpecialThreads()} method.
     */
    private void deactivateSpecialThreads() {
        YoutubeUuidCheckerManager.INSTANCE.deactivate();
        BletchyAnimationManager.INSTANCE.deactivate();
    }

    /**
     * Clears the console printing lists.
     */
    private void clearLists() {
        consolePrintingList.clear();
        consolePriorityPrintingList.clear();
    }

    /**
     * The input type associated with possible handle strings.
     */
    private enum InputType {
        /**
         * The input was user generated.
         */
        USER,

        /**
         * The input is the result of a similar command invocation.
         */
        SIMILAR_COMMAND
    }

    /**
     * Handles the input and provides output if necessary to the linked JTextPane.
     *
     * @param op the operation that is being handled
     */
    public void handle(String op) {
        handle(op, InputType.USER);
    }

    /**
     * Handles the input and provides output if necessary to the linked JTextPane.
     *
     * @param op        the operation that is being handled
     * @param inputType the input type
     */
    public final void handle(String op, InputType inputType) {
        if (!handlePreliminaries(op, inputType)) return;
        if (attemptRedirection()) return;
        if (attemptPrimaryHandlers()) return;
        if (attemptFinalHandlers()) return;

        unknownInput();
    }

    /**
     * Attempts to pass the current command input to the {@link #redirectionHandler} if not null.
     *
     * @return whether the {@link #redirectionHandler} handled the current command input
     */
    private boolean attemptRedirection() {
        if (redirectionHandler != null) {
            for (Method method : redirectionHandler.getMethods()) {
                if (method.isAnnotationPresent(Handle.class)) {
                    if (method.getParameterCount() != 0) continue;

                    Object invocationResult;
                    try {
                        invocationResult = method.invoke(redirectionHandler);
                    } catch (Exception e) {
                        throw new FatalException(e.getMessage());
                    }

                    if (invocationResult instanceof Boolean bool) return bool;
                }
            }
        }

        return false;
    }

    /**
     * Attempts to handle the current command input using the {@link #primaryHandlers}.
     *
     * @return whether a primary handler handled the current command input
     */
    private boolean attemptPrimaryHandlers() {
        for (Class<?> handle : primaryHandlers) {
            for (Method method : handle.getMethods()) {
                if (method.isAnnotationPresent(Handle.class)) {
                    if (method.getParameterCount() != 0) continue;

                    String[] triggers = method.getAnnotation(Handle.class).value();
                    for (String trigger : triggers) {
                        if (commandAndArgsToString().startsWith(trigger)) {
                            Object invocationResult;
                            try {
                                invocationResult = method.invoke(handle);
                            } catch (Exception e) {
                                throw new FatalException(e.getMessage());
                            }

                            if (invocationResult instanceof Boolean bool) return bool;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Attempts to handle the current command input using the {@link #finalHandlers}.
     *
     * @return whether a final handler handled the current command input
     */
    private boolean attemptFinalHandlers() {
        for (Class<?> handle : finalHandlers) {
            for (Method method : handle.getMethods()) {
                if (method.isAnnotationPresent(Handle.class)) {
                    if (method.getParameterCount() != 0) continue;

                    Object invocationResult;
                    try {
                        invocationResult = method.invoke(handle);
                    } catch (Exception e) {
                        throw new FatalException(e.getMessage());
                    }

                    if (invocationResult instanceof Boolean bool) return bool;
                }
            }
        }

        return false;
    }

    /**
     * Handles preliminaries such as argument/command parsing and redirection checks
     * before passing input data to the jhandle methods.
     *
     * @param command   the command to handle preliminaries on before behind handled
     * @param inputType the input type
     * @return whether preliminary checks successfully completed
     */
    private boolean handlePreliminaries(String command, InputType inputType) {
        Preconditions.checkNotNull(linkedOutputPane);
        Preconditions.checkNotNull(command);
        this.command = command.trim();

        resetMembers();

        if (StringUtil.isNullOrEmpty(this.command)) {
            Logger.log(LogTag.HANDLE_METHOD, "Failed preliminaries for empty/null operation");
            return false;
        }

        String commandAndArgsToString = commandAndArgsToString();
        Logger.log(LogTag.CLIENT, (inputType == InputType.SIMILAR_COMMAND
                ? "" : "[Similar Command]: ") + commandAndArgsToString);

        if (UserDataManager.INSTANCE.shouldFilterchat()) {
            StringUtil.BlockedWordResult result = checkFoulLanguage();
            if (result.failed()) {
                println("Sorry, " + UserDataManager.INSTANCE.getUsername() + ", but that language"
                        + " is prohibited, word: " + CyderStrings.quote + result.triggerWord() + CyderStrings.quote);
                Logger.log(LogTag.HANDLE_METHOD, "Failed preliminaries due to prohibited language");
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
        String[] parts = this.command.split(CyderRegexPatterns.whiteSpaceRegex);
        this.command = parts[0];
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
     * Resets redirection, the redirection file, and the arguments array.
     */
    private void resetMembers() {
        redirection = false;
        redirectionFile = null;
        args.clear();
    }

    /**
     * The char for redirecting input to a file.
     */
    private static final String REDIRECTION_CHAR = ">";

    /**
     * Checks for a requested redirection and attempts to create the file if valid.
     */
    private void redirectionCheck() {
        if (args.size() < 2) return;
        String secondToLastArg = args.get(args.size() - 2);
        if (!secondToLastArg.equalsIgnoreCase(REDIRECTION_CHAR)) return;

        String requestedFilename = args.get(args.size() - 1);

        if (!OsUtil.isValidFilename(requestedFilename)) {
            failedRedirection();
            return;
        }

        redirection = true;

        try {
            redirectionLock.acquire();

            redirectionFile = Dynamic.buildDynamic(Dynamic.USERS.getFileName(),
                    Console.INSTANCE.getUuid(), UserFile.FILES.getName(), requestedFilename).getAbsoluteFile();

            if (redirectionFile.exists()) {
                OsUtil.deleteFile(redirectionFile);
            }

            if (!OsUtil.createFile(redirectionFile, true)) {
                failedRedirection();
            }
        } catch (Exception ignored) {
            failedRedirection();
        } finally {
            redirectionLock.release();
        }
    }

    /**
     * Handles a failed redirection attempt.
     */
    private void failedRedirection() {
        redirection = false;
        redirectionFile = null;

        println("Failed to redirect output");
    }

    /**
     * The tolerance value that the similar command function must be at or above
     * to be passed off as a legit recommendation.
     */
    private static final float SIMILAR_COMMAND_TOL = 0.80f;

    /**
     * The final handle method for if all other handle methods failed.
     */
    private void unknownInput() {
        SimilarCommand similarCommandObj = getSimilarCommand(command);
        boolean wrapShell = UserDataManager.INSTANCE.shouldWrapShell();

        if (similarCommandObj.command().isPresent()) {
            String similarCommand = similarCommandObj.command().get();
            double tolerance = similarCommandObj.tolerance();
            if (tolerance == 1.0) return;

            if (!StringUtil.isNullOrEmpty(similarCommand)) {
                Logger.log(LogTag.DEBUG, "Similar command to " + CyderStrings.quote
                        + command + CyderStrings.quote + " found with tolerance of " + tolerance
                        + ", command: " + CyderStrings.quote + similarCommand + CyderStrings.quote);

                if (!wrapShell) {
                    boolean autoTrigger = Props.autoTriggerSimilarCommands.getValue();
                    boolean toleranceMet = tolerance >= Props.autoTriggerSimilarCommandTolerance.getValue();

                    if (tolerance >= SIMILAR_COMMAND_TOL) {
                        if (autoTrigger && toleranceMet) {
                            println(UNKNOWN_COMMAND + "; Invoking similar command: "
                                    + CyderStrings.quote + similarCommand + CyderStrings.quote);
                            handle(similarCommand, InputType.SIMILAR_COMMAND);
                        } else {
                            println(UNKNOWN_COMMAND + "; Most similar command: "
                                    + CyderStrings.quote + similarCommand + CyderStrings.quote);
                        }

                        return;
                    }
                }
            }
        }

        if (wrapShell) {
            CyderThreadRunner.submit(this::performWrapShell, "Unknown Input Shell Wrapper, input: "
                    + StringUtil.joinParts(args, CyderStrings.space));

        } else {
            println(UNKNOWN_COMMAND);
        }
    }

    /**
     * A record representing a found similar command how close the
     * found command is to the original string.
     */
    private record SimilarCommand(Optional<String> command, double tolerance) {}

    /**
     * Finds the most similar command to the unrecognized one provided.
     *
     * @param command the user entered command to attempt to find a similar command to
     * @return the most similar command to the one provided
     */
    private static SimilarCommand getSimilarCommand(String command) {
        Preconditions.checkNotNull(command);
        Preconditions.checkArgument(!command.isEmpty());

        String mostSimilarTrigger = "";
        float mostSimilarRatio = 0.0f;

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
            Class<?> clazz = classInfo.load();

            for (Method m : clazz.getMethods()) {
                ImmutableList<String> triggers = ImmutableList.of();

                if (m.isAnnotationPresent(Handle.class)) {
                    triggers = ImmutableList.copyOf(m.getAnnotation(Handle.class).value());
                } else if (m.isAnnotationPresent(Widget.class)) {
                    triggers = ImmutableList.copyOf(m.getAnnotation(Widget.class).triggers());
                }

                for (String trigger : triggers) {
                    double ratio = new JaroWinklerDistance().apply(trigger, command);

                    if (ratio > mostSimilarRatio) {
                        mostSimilarRatio = (float) ratio;
                        mostSimilarTrigger = trigger;
                    }
                }
            }
        }

        return new SimilarCommand(StringUtil.isNullOrEmpty(mostSimilarTrigger)
                ? Optional.empty()
                : Optional.of(mostSimilarTrigger), mostSimilarRatio);
    }

    /**
     * The actions performed when it is known that a wrap shell action should be taken.
     */
    private void performWrapShell() {
        println(UNKNOWN_COMMAND + ", passing to operating system native shell" + CyderStrings.space
                + CyderStrings.openingParenthesis + OsUtil.getShellName() + CyderStrings.closingParenthesis);

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

    /**
     * Creates and returns the process that invokes the command and args as an operating system command.
     *
     * @return the created process after starting
     * @throws IOException if any IO errors occur when starting the process
     */
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
        YoutubeUuidCheckerManager.INSTANCE.killAll();
        BletchyAnimationManager.INSTANCE.kill();
    }

    /**
     * Semaphore for adding objects to both consolePrintingList and consolePriorityPrintingList.
     */
    private final Semaphore printingListAddLock = new Semaphore(1);

    /**
     * Acquires the printingListAddLock.
     * This method should only be called from inside one of the printing lists overriden add method.
     */
    @ForReadability
    private void lockAddingToLists() {
        try {
            printingListAddLock.acquire();
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }

    /**
     * Releases the printingListAddLock.
     * This method should only be called from inside one of the printing lists overriden add method.
     */
    @ForReadability
    private void unlockAddingToLists() {
        printingListAddLock.release();
    }

    /**
     * The printing list for non-important outputs.
     * DO NOT ADD DIRECTLY TO THIS LIST UNLESS YOU ARE A PRINT METHOD.
     */
    private final LinkedList<Object> consolePrintingList = new LinkedList<>() {
        @Override
        public boolean add(Object e) {
            lockAddingToLists();
            boolean ret = super.add(e);
            startConsolePrintingAnimationIfNeeded();
            unlockAddingToLists();
            return ret;
        }
    };

    /*
    Note to maintainers: these lists are anonymously declared to allow for their add methods
    to have additional functionality such as thread-safety via printingListAddLock.
     */

    /**
     * The priority printing list for important outputs.
     * DO NOT ADD DIRECTLY TO THIS LIST UNLESS YOU ARE A PRINT METHOD.
     */
    private final LinkedList<Object> consolePriorityPrintingList = new LinkedList<>() {
        @Override
        public boolean add(Object e) {
            lockAddingToLists();
            boolean ret = super.add(e);
            startConsolePrintingAnimationIfNeeded();
            unlockAddingToLists();
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
        printingAnimationRunning = true;
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
     * Returns whether the typing animation should be performed.
     *
     * @return whether the typing animation should be performed
     */
    @ForReadability
    private boolean shouldDoTypingAnimation() {
        return UserDataManager.INSTANCE.shouldShowTypingAnimation();
    }

    /**
     * Returns whether the typing animation sound should be played.
     *
     * @return whether the typing animation sound should be played
     */
    @ForReadability
    private boolean shouldDoTypingSound() {
        return UserDataManager.INSTANCE.shouldPlayTypingSound();
    }

    /**
     * The console printing animation runnable.
     */
    private final Runnable consolePrintingRunnable = () -> {
        try {
            boolean shouldDoTypingAnimation = shouldDoTypingAnimation();
            boolean shouldDoTypingSound = shouldDoTypingSound();
            long lastPollTime = System.currentTimeMillis();
            int lineTimeout = Props.printingAnimationLineTimeout.getValue();

            while (!Console.INSTANCE.isClosed() && !listsEmpty()) {
                if (System.currentTimeMillis() - lastPollTime > USER_DATA_POLL_FREQUENCY_MS) {
                    lastPollTime = System.currentTimeMillis();
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
        Logger.log(LogTag.CONSOLE_OUT, ret);
        return ret;
    }

    // -----------------------
    // Document insert methods
    // -----------------------

    /**
     * Inserts the provided object into the current outputArea after
     * invoking {@link String#valueOf(Object))} on the provided object.
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
    private static final int TYPING_ANIMATION_SOUND_FREQUENCY = Props.printingAnimationSoundFrequency.getValue();

    /**
     * The number of characters appended for the the current printing animation.
     * Used to determine when to play a typing animation sound.
     */
    private final AtomicInteger typingAnimationCharsInserted = new AtomicInteger();

    /**
     * The file for the typing sound effect.
     */
    private final File typingSoundFile = StaticUtil.getStaticResource("typing.mp3");

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
            if (!linkedOutputPane.acquireLock()) {
                throw new FatalException("Failed to acquire output pane lock");
            }

            for (char c : line.toCharArray()) {
                String character = String.valueOf(c);
                String insertChar = UserDataManager.INSTANCE.isCapsMode()
                        ? character.toUpperCase()
                        : character;

                StyledDocument document = (StyledDocument) getJTextPane().getDocument();
                document.insertString(document.getLength(), insertChar, null);

                getJTextPane().setCaretPosition(getJTextPane().getDocument().getLength());

                if (typingAnimationCharsInserted.get() == TYPING_ANIMATION_SOUND_FREQUENCY) {
                    if (!shouldFinishPrinting && typingSound) {
                        GeneralAndSystemAudioPlayer.playSystemAudio(typingSoundFile);
                        typingAnimationCharsInserted.set(0);
                    }
                } else {
                    typingAnimationCharsInserted.getAndIncrement();
                }

                if (!shouldFinishPrinting) {
                    ThreadUtil.sleep(Props.printingAnimationCharTimeout.getValue());
                }
            }

            typingAnimationCharsInserted.set(0);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            linkedOutputPane.releaseLock();
        }
    }

    // -----------------------
    // Document entity removal
    // -----------------------

    /**
     * The deafult number of elements in a document.
     */
    private static final int defaultDocumentEntities = 3;

    /**
     * The number of times to call {@link #removeLastElement()} from within {@link #removeLastEntity()}.
     */
    private static final int removeLastElementCalls = 2;

    /**
     * Removes the last entity added to the JTextPane by invoking {@link #removeLastElement()} twice due
     * to a new line always being printed last. If there are other elements present after the remove, a newline
     * is added back to the document.
     */
    public final void removeLastEntity() {
        try {
            ElementIterator iterator = new ElementIterator(getJTextPane().getStyledDocument());
            int count = 0;
            while (iterator.next() != null) count++;

            if (!linkedOutputPane.acquireLock()) {
                throw new FatalException("Failed to acquire output pane lock");
            }

            removeLastElement();
            removeLastElement();
            if (count > defaultDocumentEntities + removeLastElementCalls) println("");

            linkedOutputPane.releaseLock();
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
     * Removes the last line added to the linked JTextPane such as a component, image icon, string, or newline.
     */
    private void removeLastElement() {
        linkedOutputPane.getStringUtil().removeLastElement();
    }

    // -----------------
    // Redirection logic
    // -----------------

    /**
     * The lock used to ensure output is properly written to the {@link #redirectionFile}
     * This also ensures that multiple redirections aren't performed at the same time.
     */
    private final Semaphore redirectionLock = new Semaphore(1);

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
            if (!OsUtil.createFile(redirectionFile, true)) {
                println(REDIRECTION_ERROR_MESSAGE);
                println(object);
                return;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(redirectionFile, true))) {
            redirectionLock.acquire();
            writer.write(String.valueOf(object));
            Logger.log(LogTag.CONSOLE_REDIRECTION, "Console output was redirected to: "
                    + redirectionFile.getAbsolutePath());
            redirectionLock.release();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * The escaped string.
     */
    private static final String ESCAPED = "Escaped";

    /**
     * Stops all threads invoked, sets the userInputMode to false,
     * stops any audio playing, and finishes printing anything in the printing lists.
     */
    public final void escapeThreads() {
        killThreads();
        escapeWrapShell = true;
        GeneralAndSystemAudioPlayer.stopGeneralAudio();
        YouTubeDownloadManager.INSTANCE.cancelAllActiveDownloads();
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
        return this.command;
    }

    /**
     * Determines if the current command equals the provided text ignoring case.
     *
     * @param compare the string to check for case-insensitive equality to command
     * @return if the current command equals the provided text ignoring case
     */
    protected boolean commandIs(String compare) {
        Preconditions.checkNotNull(compare);

        return this.command.equalsIgnoreCase(compare);
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
     * Returns whether there are no args associated with the most recently issued command.
     *
     * @return whether there are no args associated with the most recently issued command
     */
    protected boolean noArgs() {
        return args.isEmpty();
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
            if (i != args.size() - 1) sb.append(CyderStrings.space);
        }

        return sb.toString();
    }

    /**
     * Returns the original user input, that of the command followed by the arguments.
     *
     * @return the original user input, that of the command followed by the arguments
     */
    protected String commandAndArgsToString() {
        return (this.command.trim() + CyderStrings.space + argsToString()).trim();
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

    /**
     * Returns whether the current command and args to string starts with the provided string.
     *
     * @param startsWith the string
     * @return whether the current command and args to string starts with the provided string
     */
    protected boolean inputIgnoringSpacesAndCaseStartsWith(String startsWith) {
        Preconditions.checkNotNull(startsWith);

        return commandAndArgsToString().replaceAll(CyderRegexPatterns.whiteSpaceRegex, "").toLowerCase()
                .startsWith(startsWith.replaceAll(CyderRegexPatterns.whiteSpaceRegex, "").toLowerCase());
    }

    // -------------------------------------------
    // Utils for print methods and synchronization
    // -------------------------------------------

    /**
     * Returns whether a YouTube or bletchy thread is running.
     *
     * @return whether a YouTube or bletchy thread is running
     */
    @ForReadability
    private boolean threadsActive() {
        return YoutubeUuidCheckerManager.INSTANCE.hasActiveCheckers()
                || BletchyAnimationManager.INSTANCE.isActive();
    }

    // ---------------------
    // Generic print methods
    // ---------------------

    /**
     * The printing semaphore.
     */
    private final Semaphore printingSemaphore = new Semaphore(1);

    /**
     * Aqquires the printing lock.
     */
    private void acquirePrintingLock() {
        try {
            printingSemaphore.acquire();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Releases the printing lock.
     */
    private void releasePrintingLock() {
        printingSemaphore.release();
    }

    /**
     * Prints the provided type.
     *
     * @param type the type to print
     */
    public final <T> void print(T type) {
        Preconditions.checkNotNull(type);

        if (threadsActive()) {
            consolePriorityPrintingList.add(type);
        } else {
            consolePrintingList.add(type);
        }
    }

    /**
     * Prints the provided type followed by a newline.
     *
     * @param type the type to print
     */
    public final <T> void println(T type) {
        Preconditions.checkNotNull(type);

        if (threadsActive()) {
            consolePriorityPrintingList.add(type);
            consolePriorityPrintingList.add(CyderStrings.newline);
        } else {
            acquirePrintingLock();
            consolePrintingList.add(type);
            consolePrintingList.add(CyderStrings.newline);
            releasePrintingLock();
        }
    }

    /**
     * Adds the provided type to the priority printing list.
     *
     * @param type the type to add to the priority printing list
     */
    public final <T> void printPriority(T type) {
        Preconditions.checkNotNull(type);

        consolePriorityPrintingList.add(type);
    }

    /**
     * Adds the provided type and a newline to the priority printing list.
     *
     * @param type the type to add to the priority printing list
     */
    public final <T> void printlnPriority(T type) {
        Preconditions.checkNotNull(type);

        consolePriorityPrintingList.add(type);
        consolePriorityPrintingList.add(CyderStrings.newline);
    }

    /**
     * Prints the provided String lines to the linked JTextPane.
     * Note that new lines are automatically added in this so the passed
     * array may be strings that do not end with new lines.
     *
     * @param lines the lines to print to the JTextPane
     */
    public final void printlns(String[] lines) {
        Arrays.stream(lines).forEach(this::println);
    }

    /**
     * Prints the provided String lines to the linked JTextPane.
     * Note that new lines are automatically added in this so the passed
     * array may be strings that do not end with new lines.
     *
     * @param lines the lines to print to the JTextPane
     */
    public final void printlns(List<String> lines) {
        lines.forEach(this::println);
    }

    // -----------------------------------------------------------------
    // Printed labels which require font, font metric, font size,
    // and foreground updating as long as they are contained in the list
    // -----------------------------------------------------------------

    /**
     * The list of labels appended to the Console's text pane which require updating whenever
     * the following events occur:
     *
     * <ul>
     *     <li>User font changed</li>
     *     <li>User font metric changed</li>
     *     <li>User font size changed</li>
     *     <li>User foreground color changed</li>
     * </ul>
     */
    private final ArrayList<JLabel> printedLabels = new ArrayList<>();

    /**
     * Adds the providede label to the printed labels list. This label will have its properties updated when
     * the following events occurs:
     *
     * <ul>
     *     <li>User font changed</li>
     *     <li>User font metric changed</li>
     *     <li>User font size changed</li>
     *     <li>User foreground color changed</li>
     * </ul>
     *
     * @param label the label to update when the outlined events occur
     */
    public void addPrintedLabel(JLabel label) {
        Preconditions.checkNotNull(label);
        Preconditions.checkArgument(!printedLabels.contains(label));

        printedLabels.add(label);
    }

    /**
     * Removes the provided label from the printed labels list. This label will no longer be updated.
     *
     * @param label the label to remove from the list
     */
    public void removePrintedLabel(JLabel label) {
        Preconditions.checkNotNull(label);
        Preconditions.checkArgument(printedLabels.contains(label));

        printedLabels.remove(label);
    }

    /**
     * Clears the printed labels list. These labels will no longer be updated if one of the following events occurs:
     *
     * <ul>
     *     <li>User font changed</li>
     *     <li>User font metric changed</li>
     *     <li>User font size changed</li>
     *     <li>User foreground color changed</li>
     * </ul>
     */
    public void clearPrintedLabels() {
        printedLabels.clear();
    }

    /**
     * Refreshes the following properties of all labels current in the printed labels list:
     *
     * <ul>
     *     <li>Foreground</li>
     *     <li>Font</li>
     *     <li>Font metric</li>
     *     <li>Font size</li>
     * </ul>
     */
    public void refreshPrintedLabels() {
        printedLabels.forEach(label -> {
            label.setForeground(UserDataManager.INSTANCE.getForegroundColor());
            label.setFont(Console.INSTANCE.generateUserFont());
        });
    }
}