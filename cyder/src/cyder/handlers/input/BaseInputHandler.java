package cyder.handlers.input;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.Handle;
import cyder.constants.CyderNumbers;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.enums.DynamicDirectory;
import cyder.enums.IgnoreThread;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.BletchyThread;
import cyder.threads.CyderThreadRunner;
import cyder.threads.MasterYoutubeThread;
import cyder.ui.CyderOutputPane;
import cyder.user.User;
import cyder.user.UserFile;
import cyder.utils.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/* some methods have yet to be utilized, arg lengths are always checked before accessing*/
@SuppressWarnings("SpellCheckingInspection")
public class BaseInputHandler {
    /**
     * The linked CyderOutputPane.
     */
    private final CyderOutputPane outputArea;

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
    @SuppressWarnings("unused")
    private BaseInputHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * The robot used for screen operations.
     */
    private static final Robot robot = initializeRobot();

    /**
     * Constructs and returns the base input handler robot.
     *
     * @return the base input handler robot
     */
    private static Robot initializeRobot() {
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
        return robot;
    }

    /**
     * Constructs a new base input handler liked to the provided {@link JTextPane}.
     *
     * @param outputArea the JTextPane object to append content to
     */
    public BaseInputHandler(JTextPane outputArea) {
        Preconditions.checkNotNull(outputArea);

        this.outputArea = new CyderOutputPane(outputArea);

        initializeThreads();

        startConsolePrintingAnimation();

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Sets up the custom thread objects managed by this base.
     */
    private void initializeThreads() {
        MasterYoutubeThread.initialize(outputArea.getJTextPane(), printingListLock);
        BletchyThread.initialize(outputArea.getJTextPane(), printingListLock);
    }

    /**
     * The handlers which contain specific triggers for commands.
     */
    public static final ImmutableList<Class<?>> primaryHandlers = ImmutableList.of(
            PixelationHandler.class,
            GitHandler.class,
            PrintImageHandler.class,
            PlayAudioHandler.class,
            ColorHandler.class,
            NetworkHandler.class,
            StatHandler.class,
            NumberHandler.class,
            ThreadHandler.class,
            UiHandler.class,
            FileHandler.class,
            FrameMovementHandler.class
    );

    /**
     * The handlers which have dont have specific triggers and instead perform checks on the command directly.
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
            Logger.log(Logger.Tag.HANDLE_METHOD, "FAILED PRELIMINARIES");
            return;
        }

        // check redirection handler first

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

        // next check partitioned handlers

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

        // final handlers

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
        // generalCommandCheck()
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
        Preconditions.checkNotNull(command);
        Preconditions.checkNotNull(outputArea);

        resetMembers();

        this.command = command.trim();

        if (StringUtil.isNull(this.command)) {
            return false;
        }

        Logger.log(Logger.Tag.CLIENT, userTriggered
                ? commandAndArgsToString() : "[SIMULATED INPUT] \"" + this.command + "\"");

        if (checkFoulLanguage()) {
            println("Sorry, " + UserUtil.getCyderUser().getName() + ", but that language is prohibited.");
            return false;
        }

        parseArgs();

        redirectionCheck();

        return true;
    }

    /**
     * Parses the current command into arguments and a command.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void parseArgs() {
        String[] parts = command.split(CyderRegexPatterns.whiteSpaceRegex);
        if (parts.length > 1) {
            Arrays.stream(parts).map(String::trim).toArray(i -> parts);

            args.addAll(Arrays.stream(parts).filter(i -> !Objects.equals(i, parts[0])).collect(Collectors.toList()));

            command = parts[0];
        }
    }

    /**
     * Checks for whether the provided string contains blocked words.
     *
     * @return whether the provided string contains blocked words
     */
    private boolean checkFoulLanguage() {
        return UserUtil.getCyderUser().getFilterchat().equals("1")
                && StringUtil.containsBlockedWords(command, true);
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
        if (args.size() < 2)
            return;
        if (!args.get(args.size() - 2).equalsIgnoreCase(">"))
            return;

        String requestedFilename = args.get(args.size() - 1);

        if (!OSUtil.isValidFilename(requestedFilename)) {
            failedRedirection();
            return;
        }

        redirection = true;

        try {
            redirectionSem.acquire();

            redirectionFile = OSUtil.buildFile(
                    DynamicDirectory.DYNAMIC_PATH,
                    DynamicDirectory.USERS.getDirectoryName(),
                    ConsoleFrame.INSTANCE.getUUID(),
                    UserFile.FILES.getName(), requestedFilename).getAbsoluteFile();

            if (redirectionFile.exists())
                OSUtil.deleteFile(redirectionFile);

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
     * The final handle method for if all other handle methods failed.
     */
    private void unknownInput() {
        CyderThreadRunner.submit(() -> {
            try {
                ReflectionUtil.SimilarCommand similarCommand = ReflectionUtil.getSimilarCommand(command);

                if (similarCommand.command().isPresent()) {
                    String simCom = similarCommand.command().get();
                    double tol = similarCommand.tolerance();

                    if (!StringUtil.isNull(simCom)) {
                        Logger.log(Logger.Tag.DEBUG, "Similar command to \""
                                + command + "\" found with tol of " + tol + ", command = \"" + simCom + "\"");

                        if (tol >= CyderNumbers.SIMILAR_COMMAND_TOL) {
                            println("Unknown command; Most similar command: \"" + simCom + "\"");
                        } else {
                            wrapShellCheck();
                        }
                    }
                } else {
                    wrapShellCheck();
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Unknown Input Handler");
    }

    /**
     * Used to escape the terminal wrapper.
     */
    private boolean escapeWrapShell;

    /**
     * Checks for wrap shell mode and passes the args and command to the native termainl.
     */
    private void wrapShellCheck() {
        if (UserUtil.getCyderUser().getWrapshell().equalsIgnoreCase("1")) {
            println("Unknown command, passing to native shell");

            CyderThreadRunner.submit(() -> {
                try {
                    args.add(0, command);
                    ProcessBuilder builder = new ProcessBuilder(args);
                    builder.redirectErrorStream(true);
                    Process process = builder.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;

                    process.waitFor();

                    while ((line = reader.readLine()) != null) {
                        println(line);

                        if (escapeWrapShell)
                            break;
                    }

                    escapeWrapShell = false;
                } catch (Exception ignored) {
                    println("Unknown command");
                }
            }, "Wrap Shell Thread");
        } else {
            println("Unknown command");
        }
    }

    // end printing tests ----------------------------------

    /**
     * Standard getter for the currently linked JTextPane.
     *
     * @return the linked JTextPane
     */
    public final JTextPane getOutputArea() {
        return outputArea.getJTextPane();
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
     * the printing list of non-important outputs.
     * Directly adding to this list should not be performed. Instead, use a print/println statement.
     * List is declared anonymously to allow for their add methods to be overridden to allow for
     * Semaphore usage implying thread safety when calling print statements.
     */
    private final LinkedList<Object> consolePrintingList = new LinkedList<>() {
        @Override
        public LinkedList<Object> clone() throws AssertionError {
            throw new AssertionError();
        }

        @Override
        public boolean add(Object e) {
            try {
                printingListLock.acquire();
            } catch (InterruptedException ex) {
                ExceptionHandler.handle(ex);
            }
            boolean ret = super.add(e);
            printingListLock.release();
            return ret;
        }
    };

    /**
     * The priority printing list of important outputs.
     * Directly adding to this list should not be performed. Instead, use a print/println statement.
     * List is declared anonymously to allow for their add methods to be overridden to allow for
     * Semaphore usage implying thread safety when calling print statements.
     */
    private final LinkedList<Object> consolePriorityPrintingList = new LinkedList<>() {
        @Override
        public LinkedList<Object> clone() throws AssertionError {
            throw new AssertionError();
        }

        @Override
        public boolean add(Object e) {
            try {
                printingListLock.acquire();
            } catch (InterruptedException ex) {
                ExceptionHandler.handle(ex);
            }

            boolean ret = super.add(e);
            printingListLock.release();
            return ret;
        }
    };

    /**
     * Boolean describing whether the console printing animation thread has been invoked and begun.
     */
    private boolean printingAnimationInvoked;

    /**
     * Begins the typing animation for the ConsoleFrame.
     * The typing animation is only used if the user preference is enabled.
     */
    public final void startConsolePrintingAnimation() {
        if (printingAnimationInvoked) {
            return;
        }

        printingAnimationInvoked = true;
        consolePrintingList.clear();
        consolePriorityPrintingList.clear();

        CyderThreadRunner.submit(consolePrintingRunnable, IgnoreThread.ConsolePrintingAnimation.getName());
    }

    /**
     * The console printing animation runnable.
     */
    private final Runnable consolePrintingRunnable = new Runnable() {
        @Override public void run() {
            try {
                boolean typingAnimationLocal = UserUtil.getCyderUser().getTypinganimation().equals("1");
                long lastPull = System.currentTimeMillis();
                long dataPullTimeout = 3000;
                int lineTimeout = PropLoader.getInteger("printing_animation_line_timeout");

                while (!ConsoleFrame.INSTANCE.isClosed()) {
                    // update typingAnimationLocal every 3 seconds to reduce resource usage
                    if (System.currentTimeMillis() - lastPull > dataPullTimeout) {
                        lastPull = System.currentTimeMillis();
                        typingAnimationLocal = UserUtil.getCyderUser().getTypinganimation().equals("1");
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
                                case String s:
                                    if (typingAnimationLocal) {
                                        if (shouldFinishPrinting) {
                                            insertAsString(s);
                                        } else {
                                            innerPrintString(s);
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

                    if (!shouldFinishPrinting && typingAnimationLocal) {
                        Thread.sleep(lineTimeout);
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
    };

    /**
     * Removes, logs, and returns the first element from the provided list.
     *
     * @param list the list to perform the operations on.
     * @return the object removed from the list
     */
    private Object removeAndLog(LinkedList<Object> list) {
        Preconditions.checkNotNull(list);
        Preconditions.checkArgument(list.size() > 0);

        Object ret = list.removeFirst();
        Logger.log(Logger.Tag.CONSOLE_OUT, ret);
        return ret;
    }

    // -----------------------
    // document insert methods
    // -----------------------

    /**
     * Inserts the provided object into the current outputArea.
     *
     * @param object the object to insert
     */
    private void insertAsString(Object object) {
        StyledDocument document = (StyledDocument) outputArea.getJTextPane().getDocument();

        try {
            document.insertString(document.getLength(), String.valueOf(object), null);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        outputArea.getJTextPane().setCaretPosition(outputArea.getJTextPane().getDocument().getLength());
    }

    /**
     * Inserts the provided component into the current outputArea.
     *
     * @param component the component to insert
     */
    private void insertJComponent(JComponent component) {
        Preconditions.checkNotNull(component);
        Preconditions.checkNotNull(outputArea.getJTextPane());

        String componentUUID = SecurityUtil.generateUUID();

        Style cs = outputArea.getJTextPane()
                .getStyledDocument().addStyle(componentUUID, null);

        StyleConstants.setComponent(cs, component);

        try {
            outputArea.getJTextPane().getStyledDocument()
                    .insertString(outputArea.getJTextPane().getStyledDocument().getLength(), componentUUID, cs);
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
        Preconditions.checkNotNull(outputArea.getJTextPane());

        outputArea.getJTextPane().insertIcon(imageIcon);
    }

    /**
     * The frequency at which to play a typing sound effect if enabled.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final int typingSoundFrequency = 2;

    /**
     * The increment we are currently on for inner char printing.
     * Used to determine when to play a typing animation sound.
     */
    private int typingSoundInc;

    /**
     * The path to the typing sound effect.
     */
    private final String typingSoundPath = OSUtil.buildPath("static", "audio", "typing.mp3");

    /**
     * Prints the string to the output area checking for
     * typing sound, finish printing, and other parameters.
     * <p>
     * Note: this method is locking and SHOULD NOT be used as a
     * substitute for the default print/println methods.
     *
     * @param line the string to append to the output area
     */
    private void innerPrintString(String line) {
        try {
            outputArea.getSemaphore().acquire();

            User localUser = UserUtil.getCyderUser();
            boolean shouldDoSound = localUser.getTypingsound().equals("1");

            for (char c : line.toCharArray()) {
                String character = String.valueOf(c);
                String insertCharacter = localUser.getCapsmode().equals("1") ? character.toUpperCase() : character;

                StyledDocument document = (StyledDocument) outputArea.getJTextPane().getDocument();

                document.insertString(document.getLength(), insertCharacter, null);

                outputArea.getJTextPane().setCaretPosition(outputArea.getJTextPane().getDocument().getLength());

                if (typingSoundInc == typingSoundFrequency - 1) {
                    if (!shouldFinishPrinting && shouldDoSound) {
                        IOUtil.playSystemAudio(typingSoundPath, false);
                        typingSoundInc = 0;
                    }
                } else {
                    typingSoundInc++;
                }

                if (!shouldFinishPrinting) {
                    Thread.sleep(PropLoader.getInteger("printing_animation_char_timeout"));
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            outputArea.getSemaphore().release();
        }
    }

    // -----------------------
    // document entity removal
    // -----------------------

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
            ElementIterator iterator = new ElementIterator(outputArea.getJTextPane().getStyledDocument());
            Element element;
            while ((element = iterator.next()) != null) {
                elements.add(element);
            }

            int leafs = 0;

            for (Element value : elements)
                if (value.getElementCount() == 0)
                    leafs++;

            int passedLeafs = 0;

            for (Element value : elements) {
                if (value.getElementCount() == 0) {
                    if (passedLeafs + 3 != leafs) {
                        passedLeafs++;
                        continue;
                    }

                    if (value.toString().toLowerCase().contains("icon") ||
                            value.toString().toLowerCase().contains("component")) {
                        removeTwoLines = true;
                    }
                }
            }

            outputArea.getSemaphore().acquire();

            if (removeTwoLines) {
                removeLastLine();
            }

            removeLastLine();

            outputArea.getSemaphore().release();
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
    @SuppressWarnings("unused")
    private final String getLastTextLine() {
        return outputArea.getStringUtil().getLastTextLine();
    }

    /**
     * Removes the last line added to the linked JTextPane. This could appear to remove nothing,
     * but really be removing just a newline (line break) character.
     */
    private void removeLastLine() {
        outputArea.getStringUtil().removeLastLine();
    }

    // -----------------
    // redirection logic
    // -----------------

    /**
     * Semaphore used to ensure all things that need to
     * be written to the redirectionFile are written to it.
     * This also ensures that multiple redirections
     * aren't performed at the same time.
     */
    private final Semaphore redirectionSem = new Semaphore(1);

    /**
     * Writes the provided object to the redirection file instead of the JTextPane.
     *
     * @param object the object to invoke toString() on and write to the current redirectionFile
     */
    private void redirectionWrite(Object object) {
        Preconditions.checkNotNull(object);

        if (!redirectionFile.exists()) {
            if (!OSUtil.createFile(redirectionFile, true)) {
                println("Could not redirect output");
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

    /**
     * Stops all threads invoked, sets the userInputMode to false,
     * stops any audio playing, and finishes printing anything in the printing lists.
     */
    public final void escapeThreads() {
        killThreads();
        escapeWrapShell = true;
        IOUtil.stopGeneralAudio();
        ConsoleFrame.INSTANCE.stopDancing();
        shouldFinishPrinting = true;
        println("Escaped");
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
        return command.equalsIgnoreCase(compare);
    }

    /**
     * Returns whether the arguments array contains the expected number of arguments.
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
        if (index + 1 <= args.size() && index >= 0) {
            return args.get(index);
        }

        throw new IllegalArgumentException("Provided index is out of bounds: "
                + index + ", argument size: " + args.size());
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
            sb.append(args.get(i)).append(i == args.size() - 1 ? "" : " ");
        }

        return sb.toString();
    }

    /**
     * Returns the original user input, that of command followed by the arguments.
     *
     * @return the original user input, that of command followed by the arguments
     */
    protected String commandAndArgsToString() {
        StringBuilder sb = new StringBuilder();
        sb.append(command.trim()).append(" ");

        for (int i = 0 ; i < args.size() ; i++) {
            sb.append(args.get(i)).append(i == args.size() - 1 ? "" : " ");
        }

        return sb.toString().trim();
    }

    /**
     * Returns whether the provided string matches the command and
     * arguments strung together with whitespace removed.
     *
     * @param match the string to match to
     * @return whether the provided string matched the command args with whitespace removed
     */
    protected boolean inputWithoutSpacesIs(String match) {
        Preconditions.checkArgument(!match.contains("\\s+"));
        return match.equalsIgnoreCase(commandAndArgsToString().replaceAll("\\s+", ""));
    }

    // ---------------------
    // generic print methods
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
            consolePriorityPrintingList.add(tee);
            consolePriorityPrintingList.add("\n");
        } else {
            consolePrintingList.add(tee);
            consolePrintingList.add("\n");
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
        consolePriorityPrintingList.add(tee);
        consolePriorityPrintingList.add("\n");
    }

    /**
     * Prints the provided String lines to the linked JTextPane.
     * Note that new lines are automatically added in this so the passed
     * array may be strings that do not end with new lines.
     *
     * @param lines the lines to print to the JTextPane
     */
    public final void printlns(String[] lines) {
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
    public final void printlns(List<String> lines) {
        for (String line : lines) {
            println(line);
        }
    }
}