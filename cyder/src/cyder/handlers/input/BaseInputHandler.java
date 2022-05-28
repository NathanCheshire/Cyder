package cyder.handlers.input;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.ClassPath;
import cyder.annotations.Handle;
import cyder.annotations.ManualTest;
import cyder.common.Suggestion;
import cyder.common.WidgetDescription;
import cyder.constants.*;
import cyder.enums.DynamicDirectory;
import cyder.enums.ExitCondition;
import cyder.enums.IgnoreThread;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.BletchyThread;
import cyder.threads.CyderThreadRunner;
import cyder.threads.MasterYoutubeThread;
import cyder.ui.CyderCaret;
import cyder.ui.CyderOutputPane;
import cyder.ui.CyderSliderUI;
import cyder.user.Preferences;
import cyder.user.User;
import cyder.user.UserCreator;
import cyder.user.UserFile;
import cyder.utilities.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
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
     * The handlers to attempt to find a matching handle for.
     */
    private static final ImmutableList<Class<?>> primaryHandlers = ImmutableList.of(
            PixelationHandler.class,
            GitHandler.class,
            PrintImageHandler.class,
            PlayAudioHandler.class
    );

    /**
     * The handlers which have no exact handle and instead perform checks on the command directly.
     */
    private static final ImmutableList<Class<?>> finalHandlers = ImmutableList.of(
            GeneralPrintHandler.class,
            FrameMovementHandler.class,
            UrlHandler.class,
            PreferenceHandler.class,
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
                        if (trigger.equalsIgnoreCase(getCommand())) {
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

        // handlers didn't work so now use final ones
        try {
            //noinspection StatementWithEmptyBody
            if (ReflectionUtil.openWidget((commandAndArgsToString()))
                    || generalCommandCheck()) {

            } else //noinspection StatementWithEmptyBody
                if (mathExpressionCheck(commandAndArgsToString())
                        || manualTestCheck(commandAndArgsToString())) {

                } else {
                    unknownInput();
                }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            ConsoleFrame.INSTANCE.getInputField().setText("");
        }
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
        if (args.size() <= 2)
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
                    DynamicDirectory.DYNAMIC_PATH, "users",
                    ConsoleFrame.INSTANCE.getUUID(),
                    UserFile.FILES.getName(), requestedFilename);

            if (redirectionFile.exists())
                OSUtil.deleteFile(redirectionFile);

            if (!OSUtil.createFile(redirectionFile)) {
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

    private boolean generalCommandCheck() throws IOException {
        boolean ret = true;

        if (commandIs("createuser")) {
            UserCreator.showGui();
        } else if (commandIs("backgroundcolor")) {
            if (checkArgsLength(1)) {
                try {
                    int w = ConsoleFrame.INSTANCE.getConsoleCyderFrame().getWidth();
                    int h = ConsoleFrame.INSTANCE.getConsoleCyderFrame().getHeight();

                    if (UserUtil.getCyderUser().getFullscreen().equals("1")) {
                        w = ScreenUtil.getScreenWidth();
                        h = ScreenUtil.getScreenHeight();
                    }

                    BufferedImage saveImage = ImageUtil.bufferedImageFromColor(
                            Color.decode("#" + getArg(0).replace("#", "")), w, h);

                    String saveName = "Solid_" + getArg(0) + "Generated_Background.png";

                    File saveFile = OSUtil.buildFile(DynamicDirectory.DYNAMIC_PATH, "users",
                            ConsoleFrame.INSTANCE.getUUID(), UserFile.BACKGROUNDS.getName(), saveName);

                    ImageIO.write(saveImage, "png", saveFile);

                    println("Background generated, set, and saved as a separate background file.");

                    ConsoleFrame.INSTANCE.setBackgroundFile(saveFile);
                } catch (Exception e) {
                    println("Background color command usage: backgroundcolor EC407A");
                    ExceptionHandler.silentHandle(e);
                }
            } else {
                println("Background color command usage: backgroundcolor EC407A");
            }
        } else if (commandIs("fixforeground")) {
            Color backgroundDom = ColorUtil.getDominantColor(ImageIO.read(
                    ConsoleFrame.INSTANCE.getCurrentBackground().getReferenceFile()));

            if ((backgroundDom.getRed() * 0.299 + backgroundDom.getGreen()
                    * 0.587 + backgroundDom.getBlue() * 0.114) > 186) {
                ConsoleFrame.INSTANCE.getOutputArea().setForeground(CyderColors.defaultLightModeTextColor);
                ConsoleFrame.INSTANCE.getInputField().setForeground(CyderColors.defaultLightModeTextColor);
                ConsoleFrame.INSTANCE.getInputField().setCaretColor(CyderColors.defaultLightModeTextColor);
                ConsoleFrame.INSTANCE.getInputField().setCaret(new CyderCaret(CyderColors.defaultLightModeTextColor));
                UserUtil.getCyderUser().setForeground(ColorUtil.rgbToHexString(CyderColors.defaultLightModeTextColor));
            } else {
                ConsoleFrame.INSTANCE.getOutputArea().setForeground(CyderColors.defaultDarkModeTextColor);
                ConsoleFrame.INSTANCE.getInputField().setForeground(CyderColors.defaultDarkModeTextColor);
                ConsoleFrame.INSTANCE.getInputField().setCaretColor(CyderColors.defaultDarkModeTextColor);
                ConsoleFrame.INSTANCE.getInputField().setCaret(new CyderCaret(CyderColors.defaultDarkModeTextColor));
                UserUtil.getCyderUser().setForeground(ColorUtil.rgbToHexString(CyderColors.defaultDarkModeTextColor));
            }

            Preferences.invokeRefresh("foreground");
            println("Foreground fixed");
        } else if (commandIs("repaint")) {
            ConsoleFrame.INSTANCE.revalidate(false, false);
            println("ConsoleFrame repainted");
        } else if (commandIs("javaproperties")) {
            for (String prop : StatUtil.getJavaProperties()) {
                println(prop);
            }
        } else if (commandIs("panic")) {
            if (UserUtil.getCyderUser().getMinimizeonclose().equals("1")) {
                FrameUtil.minimizeAllFrames();
            } else {
                OSUtil.exit(ExitCondition.GenesisControlledExit);
            }
        } else if (commandIs("define")) {
            if (!args.isEmpty()) {
                println(StringUtil.getDefinition(argsToString()));
            } else {
                println("define usage: define YOUR_WORD/expression");
            }
        } else if (commandIs("wikisum")) {
            if (!args.isEmpty()) {
                println(StringUtil.getWikipediaSummary(argsToString()));
            } else {
                println("wikisum usage: wikisum YOUR_WORD/expression");
            }
        } else if (commandIs("analyzecode")) {
            if (OSUtil.JAR_MODE) {
                println("Code analyzing is not available when in Jar mode");
            } else {
                if (checkArgsLength(0) || checkArgsLength(1)) {
                    File startDir = new File("cyder");

                    if (checkArgsLength(1)) {
                        startDir = new File(getArg(0));

                        if (!startDir.exists()) {
                            println("Invalid root directory");
                            startDir = new File("cyder");
                        }
                    }

                    File finalStartDir = startDir;

                    CyderThreadRunner.submit(() -> {
                        int codeLines = StatUtil.totalJavaLines(finalStartDir);
                        int commentLines = StatUtil.totalComments(finalStartDir);

                        println("Total lines: " + StatUtil.totalLines(finalStartDir));
                        println("Code lines: " + codeLines);
                        println("Blank lines: " + StatUtil.totalBlankLines(finalStartDir));
                        println("Comment lines: " + commentLines);
                        println("Classes: " + ReflectionUtil.CYDER_CLASSES.size());

                        float ratio = ((float) codeLines / (float) commentLines);
                        println("Code to comment ratio: " + new DecimalFormat("#0.00").format(ratio));
                    }, "Code Analyzer");
                } else {
                    println("analyzecode usage: analyzecode [path/to/the/root/directory] " +
                            "(leave path blank to analyze Cyder)");
                }
            }
        } else if (commandIs("debugstats")) {
            CyderThreadRunner.submit(() -> {
                try {
                    for (String prop : StatUtil.getComputerMemorySpaces()) {
                        println(prop);
                    }

                    for (String prop : StatUtil.getJavaProperties()) {
                        println(prop);
                    }

                    for (String prop : StatUtil.getSystemProperties()) {
                        println(prop);
                    }

                    Future<StatUtil.DebugStats> futureStats = StatUtil.getDebugProps();

                    while (!futureStats.isDone()) {
                        Thread.onSpinWait();
                    }

                    StatUtil.DebugStats stats = futureStats.get();

                    for (String line : stats.lines()) {
                        println(line);
                    }

                    println(stats.countryFlag());
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "Debug Stat Finder");
        } else if (commandIs("binary")) {
            if (checkArgsLength(1)
                    && CyderRegexPatterns.numberPattern.matcher(getArg(0)).matches()) {
                CyderThreadRunner.submit(() -> {
                    try {
                        println(getArg(0) + " converted to binary equals: "
                                + Integer.toBinaryString(Integer.parseInt(getArg(0))));
                    } catch (Exception ignored) {
                    }
                }, "Binary Converter");
            } else {
                println("Your value must only contain numbers.");
            }
        } else if (commandIs("prime")) {
            if (checkArgsLength(1)) {
                int num = Integer.parseInt(getArg(0));

                if (NumberUtil.isPrime(num)) {
                    println(num + " is a prime");
                } else {
                    println(num + " is not a prime because it is divisible by: " + NumberUtil.primeFactors(num));
                }
            } else {
                println("Prime usage: prime NUMBER");
            }
        } else if (commandIs("quit") ||
                commandIs("exit") ||
                commandIs("leave") ||
                commandIs("close")) {
            if (UserUtil.getCyderUser().getMinimizeonclose().equals("1")) {
                FrameUtil.minimizeAllFrames();
            } else {
                ConsoleFrame.INSTANCE.closeConsoleFrame(true, false);
            }
        } else if (commandIs("monitors")) {
            println(OSUtil.getMonitorStatsString());
        } else if (commandIs("networkdevices")) {
            println(OSUtil.getNetworkDevicesString());
        } else if (commandIs("bindump")) {
            if (checkArgsLength(2)) {
                if (!getArg(0).equals("-f")) {
                    println("Bindump usage: bindump -f /path/to/binary/file");
                } else {
                    File f = new File(getArg(1));

                    if (f.exists()) {
                        printlnPriority("0b" + IOUtil.getBinaryString(f));
                    } else {
                        println("File: " + getArg(0) + " does not exist.");
                    }
                }
            } else {
                println("Bindump usage: bindump -f /path/to/binary/file");
            }
        } else if (commandIs("hexdump")) {
            if (checkArgsLength(2)) {
                if (!getArg(0).equals("-f")) {
                    println("Hexdump usage: hexdump -f /path/to/binary/file");
                } else {
                    File f = new File(getArg(1));

                    if (!f.exists())
                        throw new IllegalArgumentException("File does not exist");

                    if (FileUtil.getExtension(f).equalsIgnoreCase(".bin")) {
                        if (f.exists()) {
                            printlnPriority("0x" + IOUtil.getHexString(f).toUpperCase());
                        } else {
                            println("File: " + getArg(1) + " does not exist.");
                        }
                    } else {
                        InputStream inputStream = new FileInputStream(f);
                        int numberOfColumns = 10;

                        StringBuilder sb = new StringBuilder();

                        long streamPtr = 0;
                        while (inputStream.available() > 0) {
                            long col = streamPtr++ % numberOfColumns;
                            sb.append(String.format("%02x ", inputStream.read()));
                            if (col == (numberOfColumns - 1)) {
                                sb.append("\n");
                            }
                        }

                        inputStream.close();

                        printlnPriority(sb.toString());
                    }
                }
            } else {
                println("Hexdump usage: hexdump -f /path/to/binary/file");
            }
        } else if (commandIs("barrelroll")) {
            ConsoleFrame.INSTANCE.getConsoleCyderFrame().barrelRoll();
        } else if (commandIs("askew")) {
            ConsoleFrame.INSTANCE.getConsoleCyderFrame().rotateBackground(5);
        } else if (commandIs("logout")) {
            ConsoleFrame.INSTANCE.logout();
        } else if (commandIs("throw")) {
            ExceptionHandler.handle(new Exception("Big boi exceptions; " +
                    "\"I chase your love around figure 8, I need you more than I can take\""));
        } else if (commandIs("clearops")) {
            ConsoleFrame.INSTANCE.clearCommandHistory();
            Logger.log(Logger.Tag.HANDLE_METHOD, "User cleared command history");
            println("Command history reset");
        } else if (commandIs("stopscript")) {
            MasterYoutubeThread.killAll();
            println("YouTube scripts have been killed.");
        } else if (commandIs("longword")) {
            for (int i = 0 ; i < args.size() ; i++) {
                print("pneumonoultramicroscopicsilicovolcanoconiosis");
            }

            println("");
        } else if (commandIs("stopmusic")) {
            IOUtil.stopGeneralAudio();
        } else if (commandIs("ip")) {
            println(InetAddress.getLocalHost().getHostAddress());
        } else if (inputWithoutSpacesIs("computerproperties")) {
            println("This may take a second since this feature counts your PC's free memory");

            CyderThreadRunner.submit(() -> {
                for (String prop : StatUtil.getComputerMemorySpaces()) {
                    println(prop);
                }
            }, "Computer Memory Computer");
        } else if (inputWithoutSpacesIs("systemproperties")) {
            for (String prop : StatUtil.getSystemProperties()) {
                println(prop);
            }
        } else if (commandIs("anagram")) {
            if (checkArgsLength(2)) {
                if (StringUtil.areAnagrams(getArg(0), getArg(1))) {
                    println(getArg(0) + " and " + getArg(1) + " are anagrams of each other");
                } else {
                    println(getArg(0) + " and " + getArg(1) + " are not anagrams of each other");
                }
            } else {
                println("Anagram usage: anagram word1 word2");
            }
        } else if (commandIs("clc") ||
                commandIs("cls") ||
                commandIs("clear")) {
            outputArea.getJTextPane().setText("");
        } else if (commandIs("mouse")) {
            if (checkArgsLength(2)) {
                OSUtil.setMouseLoc(Integer.parseInt(getArg(0)), Integer.parseInt(getArg(1)));
            } else {
                println("Mouse command usage: mouse X_PIXEL, Y_PIXEL");
            }
        } else if (commandIs("clearclip")) {
            StringSelection selection = new StringSelection(null);
            java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            println("Clipboard has been reset.");
        } else if (commandIs("help")) {
            help();
        } else if (commandIs("wipelogs")) {
            OSUtil.deleteFile(OSUtil.buildFile(
                    DynamicDirectory.DYNAMIC_PATH, DynamicDirectory.LOGS.getDirectoryName()));
            println("Logs wiped");
        } else if (commandIs("countlogs")) {
            File[] logDirs = new File(OSUtil.buildPath(
                    DynamicDirectory.DYNAMIC_PATH, DynamicDirectory.LOGS.getDirectoryName())).listFiles();
            int count = 0;
            int days = 0;

            if (logDirs != null && logDirs.length > 0) {
                for (File logDir : logDirs) {
                    days++;

                    File[] logDirFiles = logDir.listFiles();

                    if (logDirFiles != null && logDirFiles.length > 0) {
                        for (File log : logDirFiles) {
                            if (FileUtil.getExtension(log).equals(".log")
                                    && !logDir.equals(Logger.getCurrentLog())) {
                                count++;
                            }
                        }
                    }
                }
            }

            println("Number of log dirs: " + days);
            println("Number of logs: " + count);
        } else if (commandIs("opencurrentlog")) {
            IOUtil.openFileOutsideProgram(Logger.getCurrentLog().getAbsolutePath());
        } else if (commandIs("openlastlog")) {
            File[] logs = Logger.getCurrentLog().getParentFile().listFiles();

            if (logs != null) {
                if (logs.length == 1) {
                    println("No previous logs found");
                } else if (logs.length > 1) {
                    IOUtil.openFileOutsideProgram(logs[logs.length - 2].getAbsolutePath());
                }
            }
        } else if (commandIs("play")) {
            if (StringUtil.isNull(argsToString())) {
                println("Play command usage: Play [video_url/playlist_url/search query]");
            }

            CyderThreadRunner.submit(() -> {
                String url = argsToString();

                if (YoutubeUtil.isPlaylistUrl(url)) {
                    YoutubeUtil.downloadPlaylist(url);
                } else {
                    String extractedUuid = argsToString().replace(CyderUrls.YOUTUBE_VIDEO_HEADER, "");

                    if (extractedUuid.replace(" ", "").length() != 11) {
                        println("Searching youtube for: " + url);
                        String uuid = YoutubeUtil.getFirstUUID(url);
                        url = CyderUrls.YOUTUBE_VIDEO_HEADER + uuid;
                    }

                    YoutubeUtil.downloadVideo(url);
                }
            }, "YouTube Download Initializer Thread");
        } else if (commandIs("pastebin")) {
            if (checkArgsLength(1)) {
                String urlString;
                if (getArg(0).contains("pastebin.com")) {
                    urlString = getArg(0);
                } else {
                    urlString = CyderUrls.PASTEBIN_RAW_BASE + getArg(1);
                }

                try {
                    URL url = new URL(urlString);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        println(line);
                    }

                    reader.close();
                } catch (Exception e) {
                    ExceptionHandler.silentHandle(e);
                    println("Unknown pastebin url/UUID");
                }
            } else {
                println("pastebin usage: pastebin [URL/UUID]\nExample: pastebin xa7sJvNm");
            }
        } else if (commandIs("screenshot")) {
            if (!args.isEmpty()) {
                if (getArg(0).equalsIgnoreCase("frames")) {
                    FrameUtil.screenshotCyderFrames();
                    println("Successfully saved to your Files directory");
                } else {
                    if (!FrameUtil.screenshotCyderFrame(argsToString())) {
                        println("CyderFrame not found");
                    } else {
                        println("Successfully saved to your Files directory");
                    }
                }
            } else {
                println("Screenshot command usage: screenshot [FRAMES or FRAME_NAME]");
            }
        } else if (commandIs("dst")) {
            CyderThreadRunner.submit(() -> {
                String location = IPUtil.getIpdata().getCity() + ", "
                        + IPUtil.getIpdata().getRegion() + ", "
                        + IPUtil.getIpdata().getCountry_name();

                if (IPUtil.getIpdata().getTime_zone().isIs_dst()) {
                    println("Yes, DST is underway in " + location + ".");
                } else {
                    println("No, DST is not underway in " + location + ".");
                }
            }, "DST Checker");
        } else if (commandIs("tests")) {
            println("Valid tests to call:\n");
            printlns(ReflectionUtil.getManualTests());
        } else if (commandIs("networkaddresses")) {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

            for (NetworkInterface netint : Collections.list(nets)) {
                println("Display name: " + netint.getDisplayName());
                println("Name: " + netint.getName());

                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    println("InetAddress: " + inetAddress);
                }
            }
        } else if (commandIs("filesizes")) {
            for (StatUtil.FileSize fileSize : StatUtil.fileSizes()) {
                println(fileSize.name() + ": " + OSUtil.formatBytes(fileSize.size()));
            }
        } else if (commandIs("badwords")) {
            if (OSUtil.JAR_MODE) {
                println("Bad words not available in jar mode");
            } else {
                CyderThreadRunner.submit(() -> {
                    println("Finding bad words:");

                    for (String line : StatUtil.findBadWords()) {
                        println(line);
                    }

                    println("Concluded");
                }, "Bad Word Finder");
            }
        } else if (commandIs("usb")) {
            CyderThreadRunner.submit(() -> {
                try {
                    println("Finding connected USB devices");
                    Future<ArrayList<String>> futureLines = IOUtil.getUsbDevices();

                    while (futureLines != null && !futureLines.isDone()) {
                        Thread.onSpinWait();
                    }

                    if (futureLines != null && futureLines.get() != null) {
                        ArrayList<String> lines = futureLines.get();

                        println("Devices connected to " + OSUtil.getComputerName() + " via USB protocol:");
                        for (String line : lines) {
                            println(line);
                        }
                    }
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                }
            }, "Usb Device Finder");
        } else if (commandIs("number2string") || commandIs("number2word")) {
            if (checkArgsLength(1)) {
                if (CyderRegexPatterns.numberPattern.matcher(getArg(0)).matches()) {
                    println(NumberUtil.toWords(getArg(0)));
                } else {
                    println("Could not parse input as number: " + getArg(0));
                }
            } else {
                println("Command usage: number2string YOUR_INTEGER");
            }
        } else if (commandIs("widgets")) {
            ArrayList<WidgetDescription> descriptions = ReflectionUtil.getWidgetDescriptions();

            println("Found " + descriptions.size() + " widgets:");
            println("-------------------------------------");

            for (WidgetDescription description : descriptions) {
                StringBuilder triggers = new StringBuilder();

                for (int i = 0 ; i < description.triggers().length ; i++) {
                    triggers.append(description.triggers()[i]);

                    if (i != description.triggers().length - 1)
                        triggers.append(", ");
                }

                println("Name: " + description.name());
                println("Description: " + description.description() + "\nTriggers: ["
                        + triggers.toString().trim() + "]");
                println("-------------------------------------");
            }
        } else if (commandIs("jarmode")) {
            println(OSUtil.JAR_MODE ? "Cyder is currently running from a JAR"
                    : "Cyder is currently running from a non-JAR source");
        } else if (commandIs("git")) {
            if (!checkArgsLength(2)) {
                println("Supported git commands: clone");
            } else {
                if (getArg(0).equalsIgnoreCase("clone")) {
                    CyderThreadRunner.submit(() -> {
                        try {
                            Future<Optional<Boolean>> cloned = GitHubUtil.cloneRepoToDirectory(
                                    getArg(1), UserUtil.getUserFile(UserFile.FILES.getName()));

                            while (!cloned.isDone()) {
                                Thread.onSpinWait();
                            }

                            if (cloned.get().isPresent()) {
                                if (cloned.get().get() == Boolean.TRUE)
                                    println("Clone finished");
                                else
                                    print("Clone failed");
                            } else {
                                println("Clone failed");
                            }
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }
                    }, "Git Cloner, repo: " + getArg(1));
                } else {
                    println("Supported git commands: clone");
                }
            }
        } else if (commandIs("wipe")) {
            if (checkArgsLength(1)) {
                File requestedDeleteFile = new File(OSUtil.buildPath(
                        DynamicDirectory.DYNAMIC_PATH, "users",
                        ConsoleFrame.INSTANCE.getUUID(), getArg(0)));
                if (requestedDeleteFile.exists()) {
                    if (requestedDeleteFile.isDirectory()) {
                        if (OSUtil.deleteFile(requestedDeleteFile)) {
                            println("Successfully deleted: " + requestedDeleteFile.getAbsolutePath());
                        } else {
                            println("Could not delete folder at this time");
                        }
                    } else if (requestedDeleteFile.isFile()) {
                        if (OSUtil.deleteFile(requestedDeleteFile)) {
                            println("Successfully deleted " + requestedDeleteFile.getAbsolutePath());
                        } else {
                            println("Unable to delete file at this time");
                        }
                    } else {
                        throw new IllegalStateException(
                                "File is not a file nor directory. " + CyderStrings.europeanToymaker);
                    }
                } else {
                    println("Requested file does not exist: " + requestedDeleteFile.getAbsolutePath());
                }
            } else {
                print("Wipe command usage: wipe [directory/file within your user directory]");
            }
        } else if (commandIs("originalchams")) {
            ConsoleFrame.INSTANCE.originalChams();
        } else if (commandIs("opacity")) {
            JSlider opacitySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 100);
            opacitySlider.setBounds(0, 0, 300, 50);
            CyderSliderUI UI = new CyderSliderUI(opacitySlider);
            UI.setThumbStroke(new BasicStroke(2.0f));
            UI.setSliderShape(CyderSliderUI.SliderShape.CIRCLE);
            UI.setThumbDiameter(35);
            UI.setFillColor(CyderColors.navy);
            UI.setOutlineColor(CyderColors.vanila);
            UI.setNewValColor(CyderColors.vanila);
            UI.setOldValColor(CyderColors.regularPink);
            UI.setTrackStroke(new BasicStroke(3.0f));
            opacitySlider.setUI(UI);
            opacitySlider.setMinimum(0);
            opacitySlider.setMaximum(100);
            opacitySlider.setPaintTicks(false);
            opacitySlider.setPaintLabels(false);
            opacitySlider.setVisible(true);
            opacitySlider.setValue(100);
            opacitySlider.addChangeListener(e -> {
                ConsoleFrame.INSTANCE.getConsoleCyderFrame()
                        .setOpacity(opacitySlider.getValue() / 100.0f);
                opacitySlider.repaint();
            });
            opacitySlider.setOpaque(false);
            opacitySlider.setToolTipText("Opacity");
            opacitySlider.setFocusable(false);
            opacitySlider.repaint();

            println(opacitySlider);
        } else if (commandIs("pwd")) {
            println(OSUtil.USER_DIR);
        } else if (commandIs("download")) {
            if (checkArgsLength(1)) {
                if (NetworkUtil.isURL(getArg(0))) {
                    String responseName = NetworkUtil.getURLTitle(getArg(0));
                    String saveName = SecurityUtil.generateUUID();

                    if (responseName != null) {
                        if (!responseName.isEmpty()) {
                            saveName = responseName;
                        }
                    }

                    File saveFile = new File(OSUtil.buildPath(DynamicDirectory.DYNAMIC_PATH, "users",
                            ConsoleFrame.INSTANCE.getUUID(), UserFile.FILES.getName(), saveName));

                    // clear text as soon as possible
                    ConsoleFrame.INSTANCE.getInputField().setText("");

                    println("Saving file: " + saveName + " to files directory");

                    CyderThreadRunner.submit(() -> {
                        try {
                            if (NetworkUtil.downloadResource(getArg(0), saveFile)) {
                                println("Successfully saved");
                            } else {
                                println("Error: could not download at this time");
                            }
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }
                    }, "File URL Downloader");
                } else {
                    println("Invalid url");
                }
            } else {
                println("download usage: download [YOUR LINK]");
            }
        } else if (commandIs("curl")) {
            if (checkArgsLength(1)) {
                if (NetworkUtil.isURL(getArg(0))) {
                    URL url = new URL(getArg(0));
                    HttpURLConnection http = (HttpURLConnection) url.openConnection();

                    println(NetworkUtil.readUrl(getArg(0)));
                    println("Response: " + http.getResponseCode() + " " + http.getResponseMessage());

                    http.disconnect();
                } else {
                    println("Invalid url");
                }
            } else {
                println("Curl command usage: curl URL");
            }
        } else if (commandIs("whereami")) {
            CyderThreadRunner.submit(() -> {
                try {
                    String url = CyderUrls.LOCATION_URL;

                    Document locationDocument = Jsoup.connect(url).get();
                    Elements primary = locationDocument.getElementsByClass("desktop-title-content");
                    Elements secondary = locationDocument.getElementsByClass("desktop-title-subcontent");

                    println("You are currently in " + primary.text() + ", " + secondary.text());

                    String isp = "NOT FOUND";

                    String[] lines = NetworkUtil.readUrl(CyderUrls.ISP_URL).split("\n");

                    for (String line : lines) {
                        Matcher matcher = CyderRegexPatterns.whereAmIPattern.matcher(line);
                        if (matcher.find()) {
                            isp = matcher.group(1);
                        }
                    }

                    println("Your ISP is " + StringUtil.capsFirstWords(isp));
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "Location Finder");
        } else if (commandIs("whoami")) {
            println(OSUtil.getComputerName() + OSUtil.FILE_SEP
                    + StringUtil.capsFirstWords(UserUtil.getCyderUser().getName()));
        } else if (commandIs("freeze")) {
            //noinspection StatementWithEmptyBody
            while (true) {
            }
        } else if (commandAndArgsToString().equalsIgnoreCase("wipe spotlights")) {
            SpotlightUtil.wipeSpotlights();
        } else if (commandIs("toast")) {
            ConsoleFrame.INSTANCE.getConsoleCyderFrame().toast("A toast to you, sir/madam");
        }
        //calls that will result in threads being spun off or thread operations
        else if (commandIs("randomyoutube")) {
            MasterYoutubeThread.start(1);
        } else if (commandIs("scrub")) {
            BletchyThread.bletchy("No you!", false, 50, true);
        } else if (commandIs("bletchy")) {
            BletchyThread.bletchy(argsToString(), false, 50, true);
        } else if (commandIs("threads")) {
            ThreadUtil.printThreads();
        } else if (commandIs("daemonthreads")) {
            ThreadUtil.printDaemonThreads();
        } else if (commandIs("age")) {
            BletchyThread.bletchy("I am somewhere between 69 and 420 years old.",
                    true, 50, false);
        } else if (commandIs("easter")) {
            println("Easter Sunday is on " + TimeUtil.getEasterSundayString());
        } else
            ret = false;

        if (ret) {
            Logger.log(Logger.Tag.HANDLE_METHOD, "GENERAL COMMAND HANDLED");
        }

        return ret;
    }

    /**
     * Determines if the provided command was a mathematical expression and if so, evaluates it.
     *
     * @param command the command to attempt to evaluate as a mathematical expression
     * @return whether the command was a mathematical expression
     */
    private boolean mathExpressionCheck(String command) {
        boolean ret = false;

        try {
            println(new DoubleEvaluator().evaluate(StringUtil.firstCharToLowerCase(command.trim())));
            ret = true;
        } catch (Exception ignored) {
        }

        if (ret) {
            Logger.log(Logger.Tag.HANDLE_METHOD, "Console math handled");
        }

        return ret;
    }


    /**
     * Determines if the command intended to invoke a manual test from manual tests.
     *
     * @param command the command to attempt to recognize as a manual test
     * @return whether the command was handled as a manual test call
     */
    private boolean manualTestCheck(String command) {
        boolean ret = false;

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.CYDER_CLASSES) {
            Class<?> classer = classInfo.load();

            for (Method m : classer.getMethods()) {
                if (m.isAnnotationPresent(ManualTest.class)) {
                    String trigger = m.getAnnotation(ManualTest.class).value();
                    if (trigger.equalsIgnoreCase(command)) {
                        try {
                            println("Invoking manual test " + m.getName());
                            m.invoke(classer);
                            ret = true;
                            break;
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }
                    }
                }
            }
        }

        if (ret) {
            Logger.log(Logger.Tag.HANDLE_METHOD, "Manual test fired");
        }

        return ret;
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
     * Prints the suggestions as recommendations to the user for what to use Cyder for.
     */
    private void help() {
        println("Try typing: ");

        for (Suggestion suggestion : Suggestion.values()) {
            println(CyderStrings.bulletPoint + "\t" + suggestion.getCommand()
                    + "\n\tDescription: " + suggestion.getDescription());
        }
    }

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
        Preconditions.checkArgument(redirectionFile.exists(), "Redirection file does not exist");

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