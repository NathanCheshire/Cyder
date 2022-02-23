package cyder.handlers.internal;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import cyder.constants.CyderColors;
import cyder.constants.CyderIcons;
import cyder.constants.CyderNumbers;
import cyder.constants.CyderStrings;
import cyder.enums.*;
import cyder.genesis.CyderCommon;
import cyder.handlers.external.AudioPlayer;
import cyder.python.PyExecutor;
import cyder.threads.BletchyThread;
import cyder.threads.CyderThreadRunner;
import cyder.threads.MasterYoutubeThread;
import cyder.ui.*;
import cyder.user.Preferences;
import cyder.user.UserCreator;
import cyder.user.UserFile;
import cyder.utilities.*;
import cyder.utilities.objects.WidgetDescription;
import org.jetbrains.annotations.Nullable;
import test.java.ManualTests;
import test.java.UnitTests;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"unused", "ConstantConditions"})
/* some methods have yet to be utilized, arg lengths are always checked before accessing*/
public class InputHandler {
    /**
     * The linked CyderOutputPane.
     */
    private final CyderOutputPane outputArea;

    /**
     * boolean describing whether input should be passed to handle() or handleSecond().
     */
    private boolean userInputMode;

    /**
     * The description of the secondary input handleSecond will receive.
     */
    private String userInputDesc;

    /**
     * boolean describing whether to quickly append all remaining queued objects to the linked JTextPane.
     */
    private boolean finishPrinting;

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
    private ArrayList<String> args;

    /**
     * Private constructor to avoid incorrect instantiation.
     */
    private InputHandler() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * Default constructor with required JTextPane.
     *
     * @param outputArea the JTextPane to output pictures/components/text/etc. to
     */
    public InputHandler(JTextPane outputArea) {
        // link JTextPane
        this.outputArea = new CyderOutputPane(outputArea);

        //init other JTextPane objects such as Threads

        //printing threads initialization with JTextPane and sem -------------------
        MasterYoutubeThread.initialize(outputArea, makePrintingThreadsafeAgain);
        BletchyThread.initialize(outputArea, makePrintingThreadsafeAgain);
    }

    /**
     * Handles preliminaries such as assumptions before passing input data to the subHandle methods.
     * Also sets the ops array to the found command and arguments
     *
     * @param command the command to handle preliminaries on before behind handled
     * @param userTriggered whether the provided operation was produced via a user
     * @return whether the process may proceed
     */
    @SuppressWarnings("ResultOfMethodCallIgnored") /* file deletions and creations*/
    private boolean handlePreliminaries(String command, boolean userTriggered) {
        //check for null link (should be impossible)
        if (outputArea == null)
            throw new IllegalStateException("Output area not set. " + CyderStrings.europeanToymaker);

        //if empty string don't do anything
        if (StringUtil.isNull(command))
            return false;

        //reset redirection now since we have a new command
        redirection = false;
        redirectionFile = null;

        //trim command
        command = command.trim();
        this.command = command;

        //reset args
        args = new ArrayList<>();

        //set args ArrayList
        if (command.contains(" ")) {
            String[] arrArgs = command.split(" ");
            this.command = arrArgs[0];

            //add all that have length greater than 0 after trimming
            // and that are not the first since that is "command"
            for (int i = 1 ; i < arrArgs.length ; i++) {
                if (arrArgs[i].trim().length() > 0) {
                    args.add(arrArgs[i].trim());
                }
            }
        }

        //log input as user triggered or simulated client input
        if (userTriggered) {
            Logger.log(Logger.Tag.CLIENT, this.command);
        } else {
            Logger.log(Logger.Tag.CLIENT, "[SIMULATED INPUT] " + this.command);
        }

        //check for requested redirection
        //noinspection ConstantConditions, safe due to checking arg size
        if (args.size() > 1 && getArg(0).equals(">")) {
            String filename = getArg(1);

            //noinspection ConstantConditions, safe due to checking arg size
            if (filename.trim().length() > 0) {
                //check for validity of requested filename
                if (OSUtil.isValidFilename(filename)) {
                    redirection = true;

                    //acquire sem to ensure file is not being written to
                    try {
                        redirectionSem.acquire();
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }

                    //create the file name
                    redirectionFile = new File("dynamic/users/" +
                            ConsoleFrame.getConsoleFrame().getUUID() + "/Files/" + filename);

                    //create file for current use
                    try {
                        if (redirectionFile.exists())
                            redirectionFile.delete();
                        redirectionFile.createNewFile();
                    } catch (Exception ignored) {
                        redirection = false;
                        redirectionFile = null;
                    }

                    //release sem
                    redirectionSem.release();
                }
            }
        }

        //check for bad language if filterchat
        if (UserUtil.getUserData("filterchat").equals("1")
                && StringUtil.containsBlockedWords(this.command, true)) {
            println("Sorry, " + UserUtil.extractUser().getName() + ", but that language is prohibited.");
            return false;
        }

        return true;
    }

    /**
     * Handles the input and provides output if necessary to the linked JTextPane.
     *
     * @param op the operation that is being handled
     * @param userTriggered whether the provided op was produced via a user
     * @throws Exception for numerous reasons such as if the JTextPane is not linked
     */
    public final void handle(String op, boolean userTriggered) throws Exception {
        if (!handlePreliminaries(op, userTriggered)) {
            Logger.log(Logger.Tag.HANDLE_METHOD, "FAILED PRELIMINARIES");
        }
        //primary checks
        else if (generalPrintsCheck() ||
                printImageCheck() ||
                ReflectionUtil.openWidget((argsAndCommandToString()).trim()) ||
                cyderFrameMovementCheck() ||
                externalOpenerCheck() ||
                audioCommandCheck() ||
                generalCommandCheck()) {
            Logger.log(Logger.Tag.HANDLE_METHOD, "PRIMARY HANDLE");
        }
        //final checks
        else if (isURLCheck(command) ||
                handleMath(command) ||
                evaluateExpression(command) ||
                preferenceCheck(command) ||
                manualTestCheck(command) ||
                unitTestCheck(command)) {
            Logger.log(Logger.Tag.HANDLE_METHOD, "FINAL HANDLE");
        } else unknownInput();

        //clean up routines --------------------------------------

        //just to be safe...
        ConsoleFrame.getConsoleFrame().getInputField().setText("");
    }

    //primary sections of handle methods

    private boolean generalPrintsCheck() {
        boolean ret = true;

        if (commandIs("shakespeare")) {
            if (NumberUtil.randInt(1, 2) == 1) {
                println("Glamis hath murdered sleep, and therefore Cawdor shall sleep no more, Macbeth shall sleep no more.");
            } else {
                println("To be, or not to be, that is the question: Whether 'tis nobler in " +
                        "the mind to suffer the slings and arrows of "
                        + "outrageous fortune, or to take arms against a sea of troubles and by opposing end them.");
            }
        } else if (commandIs("asdf")) {
            println("Who is the spiciest meme lord?");
        } else if (commandIs("thor")) {
            println("Piss off, ghost.");
        }  else if (commandIs("alextrebek")) {
            println("Do you mean who is alex trebek?");
        } else if (StringUtil.isPalindrome(command.replace(" ", "")) && command.length() > 3) {
            println("Nice palindrome.");
        } else if (commandIs("coinflip")) {
            double randGauss = new SecureRandom().nextGaussian();
            if (randGauss <= 0.0001) {
                println("You're not going to believe this, but it landed on its side.");
            } else if (randGauss <= 0.5) {
                println("It's Heads!");
            } else {
                println("It's Tails!");
            }
        } else if (commandIs("hello") || commandIs("hi")) {
            int choice = NumberUtil.randInt(1, 7);

            switch (choice) {
                case 1:
                    println("Hello, " + UserUtil.extractUser().getName() + ".");
                    break;
                case 2:
                    if (TimeUtil.isEvening())
                        println("Good evening, " + UserUtil.extractUser().getName() + ". How can I help?");
                    else if (TimeUtil.isMorning())
                        println("Good morning, " + UserUtil.extractUser().getName() + ". How can I help?");
                    else
                        println("Good afternoon, " + UserUtil.extractUser().getName() + ". How can I help?");
                    break;
                case 3:
                    println("What's up, " + UserUtil.extractUser().getName() + "?");
                    break;
                case 4:
                    println("How are you doing, " + UserUtil.extractUser().getName() + "?");
                    break;
                case 5:
                    println("Greetings, " + UserUtil.extractUser().getName() + ".");
                    break;
                case 6:
                    println("I'm here....");
                    break;
                case 7:
                    println("Go ahead...");
                    break;
            }
        } else if (commandIs("bye")) {
            println("Just say you won't let go.");
        } else if (commandIs("time")) {
            println(TimeUtil.weatherTime());
        } else if (commandIs("lol")) {
            println("My memes are better.");
        } else if (commandIs("thanks")) {
            println("You're welcome.");
        }  else if (commandIs("name")) {
            println("My name is Cyder. I am a tool built by Nathan Cheshire for programmers/advanced users.");
        } else if (commandIs("k")) {
            println("Fun Fact: the letter \"K\" comes from the Greek letter kappa, which was taken "
                    + "from the Semitic kap, the symbol for an open hand. It is this very hand which "
                    + "will be slapping you in the face for saying \"k\" to me.");
        } else if (commandIs("no")) {
            println("Yes");
        } else if (commandIs("nope")) {
            println("yep");
        } else if (commandIs("yes")) {
            println("no");
        } else if (commandIs("yep")) {
            println("nope");
        } else if (commandIs("jarvis")) {
            println("*scoffs in Java* primitive loser AI");
        } else if (commandIs("thanksgiving")) {
            int year = Calendar.getInstance().get(Calendar.YEAR);
            LocalDate RealTG = LocalDate.of(year, 11, 1)
                    .with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY));
            println("Thanksgiving this year is on the " + RealTG.getDayOfMonth() + " of November.");
        } else if (commandIs("location") || commandIs("whereami")) {
            println("You are currently in " + IPUtil.getIpdata().getCity() + ", " +
                    IPUtil.getIpdata().getRegion() + " and your Internet Service Provider is "
                    + StringUtil.capsCheck(IPUtil.getIpdata().getAsn().getName()));
        } else if (commandIs("fibonacci")) {
            for (long i : NumberUtil.fib(0, 1, 100))
                println(i);
        } else if (commandIs("break;")) {
            println("Thankfully my pure console based infinite while loop days are over. <3 Nathan");
        } else if (commandIs("why")) {
            println("Why not?");
        } else if (commandIs("why not")) {
            println("Why?");
        } else if (commandIs("groovy")) {
            println("Kotlin is the best JVM lang.... I mean, Java is obviously the best!");
        } else if (commandIs("&&")) {
            println("||");
        } else if (commandIs("||")) {
            println("&&");
        } else if (commandIs("&")) {
            println("|");
        } else if (commandIs("|")) {
            println("&");
        } else if (commandIs("espanol")) {
            println("Tu hablas Espanol? Yo estudio Espanol mas-o-menos. Hay tu mi amigo?");
        } else if (commandIs("look")) {
            println("L()()K ---->> !FREE STUFF! <<---- L()()K");
        } else if (commandIs("Cyder?")) {
            println("Yes?");
        } else if (commandIs("home")) {
            println("There's no place like localhost/127.0.0.1");
        } else if (commandIs("love")) {
            println("Sorry, " + UserUtil.extractUser().getName() + ", but I don't understand human emotions or affections.");
        } else if (commandIs("loop")) {
            println("InputHandler.handle(\"loop\", true);");
        } else if (commandIs("story")) {
            println("It was a lazy day. Cyder was enjoying a deep sleep when suddenly "
                    + UserUtil.extractUser().getName() + " started talking to Cyder."
                    + " It was at this moment that Cyder knew its day had been ruined.");
        } else if (commandIs("i hate you")) {
            println("That's not very nice.");
        } else ret = false;

        if (ret)
            Logger.log(Logger.Tag.HANDLE_METHOD, "GENERAL PRINT COMMAND HANDLED");
        return ret;
    }

    private boolean printImageCheck() {
        boolean ret = true;

        if (commandIs("java")) {
            printlnImage("static/pictures/print/duke.png");
        } else if (commandIs("msu")) {
            printlnImage("static/pictures/print/msu.png");
        } else if (commandIs("nathan")) {
            printlnImage("static/pictures/print/me.png");
        } else if (commandIs("html")) {
            printlnImage("static/pictures/print/html5.png");
        } else if (commandIs("css")) {
            printlnImage("static/pictures/print/css.png");
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
        } else ret = false;

        if (ret)
            Logger.log(Logger.Tag.HANDLE_METHOD, "PRINT IMAGE COMMAND HANDLED");
        return ret;
    }

    private boolean cyderFrameMovementCheck() {
        boolean ret = true;

        if (commandIs("top left")) {
            ConsoleFrame.getConsoleFrame().setLocationOnScreen(ScreenPosition.TOP_LEFT);
        } else if (commandIs("top right")) {
            ConsoleFrame.getConsoleFrame().setLocationOnScreen(ScreenPosition.TOP_RIGHT);
        } else if (commandIs("bottom left")) {
            ConsoleFrame.getConsoleFrame().setLocationOnScreen(ScreenPosition.BOTTOM_LEFT);
        } else if (commandIs("bottom right")) {
            ConsoleFrame.getConsoleFrame().setLocationOnScreen(ScreenPosition.BOTTOM_RIGHT);
        } else if (commandIs("middle") || commandIs("center")) {
            ConsoleFrame.getConsoleFrame().setLocationOnScreen(ScreenPosition.CENTER);
        } else if (commandIs("frametitles")) {
            Frame[] frames = Frame.getFrames();
            for (Frame f : frames)
                if (f instanceof CyderFrame) {
                    println(f.getTitle());
                } else {
                    println(f.getTitle());
                }
        } else if (commandIs("consolidatewindows")) {
            if (checkArgsLength(1)) {
                if (getArg(0).equalsIgnoreCase("topright")) {
                    for (Frame f : Frame.getFrames()) {
                        if (f.getState() == Frame.ICONIFIED) {
                            f.setState(Frame.NORMAL);

                            if (f instanceof CyderFrame) {
                                ((CyderFrame) f).setRestoreX(ConsoleFrame.getConsoleFrame().getX()
                                        + ConsoleFrame.getConsoleFrame().getWidth() - f.getWidth());
                                ((CyderFrame) f).setRestoreY(ConsoleFrame.getConsoleFrame().getY());
                            }
                        }

                        f.setLocation(ConsoleFrame.getConsoleFrame().getX() + ConsoleFrame.getConsoleFrame().getWidth()
                                - f.getWidth(), ConsoleFrame.getConsoleFrame().getY());
                    }
                } else if (getArg(0).equalsIgnoreCase("bottomright")) {
                    for (Frame f : Frame.getFrames()) {
                        if (f.getState() == Frame.ICONIFIED) {
                            f.setState(Frame.NORMAL);

                            if (f instanceof CyderFrame) {
                                ((CyderFrame) f).setRestoreX(ConsoleFrame.getConsoleFrame().getX()
                                        + ConsoleFrame.getConsoleFrame().getWidth() - f.getWidth());
                                ((CyderFrame) f).setRestoreY(ConsoleFrame.getConsoleFrame().getY()
                                        + ConsoleFrame.getConsoleFrame().getHeight() - f.getHeight());
                            }
                        }

                        f.setLocation(ConsoleFrame.getConsoleFrame().getX() + ConsoleFrame.getConsoleFrame().getWidth()
                                - f.getWidth(), ConsoleFrame.getConsoleFrame().getY() +
                                ConsoleFrame.getConsoleFrame().getHeight() - f.getHeight());
                    }
                } else if (getArg(0).equalsIgnoreCase("bottomleft")) {
                    for (Frame f : Frame.getFrames()) {
                        if (f.getState() == Frame.ICONIFIED) {
                            f.setState(Frame.NORMAL);

                            if (f instanceof CyderFrame) {
                                ((CyderFrame) f).setRestoreX(ConsoleFrame.getConsoleFrame().getX());
                                ((CyderFrame) f).setRestoreY(ConsoleFrame.getConsoleFrame().getY()
                                        + ConsoleFrame.getConsoleFrame().getHeight() - f.getHeight());
                            }
                        }

                        f.setLocation(ConsoleFrame.getConsoleFrame().getX(), ConsoleFrame.getConsoleFrame().getY() +
                                ConsoleFrame.getConsoleFrame().getHeight() - f.getHeight());
                    }
                } else {
                    for (Frame f : Frame.getFrames()) {
                        if (f.getState() == Frame.ICONIFIED) {
                            f.setState(Frame.NORMAL);

                            if (f instanceof CyderFrame) {
                                ((CyderFrame) f).setRestoreX(ConsoleFrame.getConsoleFrame().getX());
                                ((CyderFrame) f).setRestoreY(ConsoleFrame.getConsoleFrame().getY());
                            }
                        }

                        f.setLocation(ConsoleFrame.getConsoleFrame().getX(), ConsoleFrame.getConsoleFrame().getY());
                    }
                }
            } else {
                println("Command usage: consolidatewindows topleft");
            }
        } else if (commandIs("dance")) {
            ConsoleFrame.getConsoleFrame().dance();
        } else ret = false;

        if (ret)
            Logger.log(Logger.Tag.HANDLE_METHOD, "CYDERFRAME MOVEMENT COMMAND HANDLED");
        return ret;
    }

    private boolean externalOpenerCheck() throws Exception {
        boolean ret = true;

        if (commandIs("YoutubeWordSearch")) {
            if (checkArgsLength(1)) {
                String input = getArg(0);
                String browse = "https://www.google.com/search?q=allinurl:REPLACE site:youtube.com";
                browse = browse.replace("REPLACE", input).replace(" ", "+");
                NetworkUtil.openUrl(browse);
            } else {
                println("YoutubeWordSearch usage: YoutubeWordSearch WORD_TO_FIND");
            }
        } else if (commandIs("echo") || commandIs("print") || commandIs("println")) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0 ; i < args.size() ; i++) {
                //print arg plus a space unless last argument
                sb.append(args.get(i)).append(i == args.size() - 1 ? "" : " ");
            }

            //ending new line
            println(sb.toString());
        } else if (commandIs("cmd")) {
            OSUtil.openShell();
            Desktop.getDesktop().open(new File("c:/windows/system32/cmd.exe"));
        } else if (commandIs("desmos")) {
            NetworkUtil.openUrl("https://www.desmos.com/calculator");
        } else if (commandIs("404")) {
            NetworkUtil.openUrl("http://google.com/=");
        } else if (commandIs("coffee")) {
            NetworkUtil.openUrl("https://www.google.com/search?q=coffe+shops+near+me");
        } else if (commandIs("quake3")) {
            NetworkUtil.openUrl("https://www.youtube.com/watch?v=p8u_k2LIZyo&ab_channel=Nemean");
        } else if (commandIs("triangle")) {
            NetworkUtil.openUrl("https://www.triangle-calculator.com/");
        } else if (commandIs("board")) {
            NetworkUtil.openUrl("http://gameninja.com//games//fly-squirrel-fly.html");
        }else if (commandIs("arduino")) {
            NetworkUtil.openUrl("https://www.arduino.cc/");
        } else if (commandIs("rasberrypi")) {
            NetworkUtil.openUrl("https://www.raspberrypi.org/");
        }else if (commandIs("vexento")) {
            NetworkUtil.openUrl("https://www.youtube.com/user/Vexento/videos");
        }else if (commandIs("papersplease")) {
            NetworkUtil.openUrl("http://papersplea.se/");
        }else if (commandIs("donut")) {
            NetworkUtil.openUrl("https://www.dunkindonuts.com/en/food-drinks/donuts/donuts");
        }else if (commandIs("bai")) {
            NetworkUtil.openUrl("http://www.drinkbai.com");
        } else if (commandIs("occamrazor")) {
            NetworkUtil.openUrl("http://en.wikipedia.org/wiki/Occam%27s_razor");
        } else if (commandIs("paint")) {
            throw new IllegalCallerException("Unimplemented yet; custom paint program coming soon");
        } else if (commandIs("rickandmorty")) {
            println("Turned myself into a pickle morty! Boom! Big reveal; I'm a pickle!");
            NetworkUtil.openUrl("https://www.youtube.com/watch?v=s_1lP4CBKOg");
        } else if (commandIs("about:blank")) {
            NetworkUtil.openUrl("about:blank");
        } else ret = false;

        if (ret)
            Logger.log(Logger.Tag.HANDLE_METHOD, "EXTERNAL OPENER COMMAND HANDLED");
        return ret;
    }

    private boolean audioCommandCheck() {
        boolean ret = true;

        if (commandIs("hey")) {
            IOUtil.playAudio("static/audio/heyya.mp3");
        }  else if (commandIs("windows")) {
            IOUtil.playAudio("static/audio/windows.mp3");
        }  else if (commandIs("lightsaber")) {
            IOUtil.playAudio("static/audio/Lightsaber.mp3");
        } else if (commandIs("xbox")) {
            IOUtil.playAudio("static/audio/xbox.mp3");
        } else if (commandIs("startrek")) {
            IOUtil.playAudio("static/audio/StarTrek.mp3");
        } else if (commandIs("toystory")) {
            IOUtil.playAudio("static/audio/TheClaw.mp3");
        } else if (commandIs("stopmusic")) {
            IOUtil.stopAudio();
        } else if (commandIs("logic")) {
            IOUtil.playAudio("static/audio/commando.mp3");
        } else if (commandIs("1-800-273-8255") || commandIs("18002738255")) {
            IOUtil.playAudio("static/audio/1800.mp3");
        } else ret = false;

        if (ret)
            Logger.log(Logger.Tag.HANDLE_METHOD, "AUDIO COMMAND HANDLED");
        return ret;
    }

    private boolean generalCommandCheck() throws Exception {
        boolean ret = true;

        if (commandIs("createuser")) {
            UserCreator.showGUI();
        } else if (commandIs("backgroundcolor")) {
            if (checkArgsLength(1)) {
                try {
                    int w = ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().getWidth();
                    int h = ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().getHeight();

                    if (UserUtil.extractUser().getFullscreen().equals("1")) {
                        w = ScreenUtil.getScreenWidth();
                        h = ScreenUtil.getScreenHeight();
                    }

                    BufferedImage saveImage = ImageUtil.bufferedImageFromColor(w, h, Color.decode("#" + getArg(0)));

                    String saveName = "Solid_" + getArg(0) + "Generated_Background.png";

                    File saveFile = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() +
                            "/Backgrounds/" + saveName);

                    ImageIO.write(saveImage, "png", saveFile);

                    println("Background generated, set, and saved as a separate background file.");

                    ConsoleFrame.getConsoleFrame().setBackgroundFile(saveFile);
                } catch (Exception e) {
                    println("Background color command usage: backgroundcolor EC407A");
                    ExceptionHandler.silentHandle(e);
                }
            } else {
                println("Background color command usage: backgroundcolor EC407A");
            }
        } else if (commandIs("fixforeground")) {
            Color backgroundDom = ColorUtil.getDominantColor(ImageIO.read(
                    ConsoleFrame.getConsoleFrame().getCurrentBackground().getReferenceFile()));

            if ((backgroundDom.getRed() * 0.299 + backgroundDom.getGreen()
                    * 0.587 + backgroundDom.getBlue() * 0.114) > 186) {
                ConsoleFrame.getConsoleFrame().getOutputArea().setForeground(CyderColors.defaultLightModeTextColor);
                ConsoleFrame.getConsoleFrame().getInputField().setForeground(CyderColors.defaultLightModeTextColor);
                ConsoleFrame.getConsoleFrame().getInputField().setCaretColor(CyderColors.defaultLightModeTextColor);
                ConsoleFrame.getConsoleFrame().getInputField().setCaret(new CyderCaret(CyderColors.defaultLightModeTextColor));
                UserUtil.setUserData("Foreground",ColorUtil.rgbtohexString(CyderColors.defaultLightModeTextColor));
            } else {
                ConsoleFrame.getConsoleFrame().getOutputArea().setForeground(CyderColors.defaultDarkModeTextColor);
                ConsoleFrame.getConsoleFrame().getInputField().setForeground(CyderColors.defaultDarkModeTextColor);
                ConsoleFrame.getConsoleFrame().getInputField().setCaretColor(CyderColors.defaultDarkModeTextColor);
                ConsoleFrame.getConsoleFrame().getInputField().setCaret(new CyderCaret(CyderColors.defaultDarkModeTextColor));
                UserUtil.setUserData("Foreground", ColorUtil.rgbtohexString(CyderColors.defaultDarkModeTextColor));
            }

            Preferences.invokeRefresh("foreground");
            println("Foreground fixed");
        } else if (commandIs("repaint")) {
            ConsoleFrame.getConsoleFrame().revalidate(false, false);
            println("ConsoleFrame repainted");
        } else if (commandIs("javaproperties")) {
            StatUtil.javaProperties();
        } else if (commandIs("panic")) {
            if (UserUtil.getUserData("minimizeonclose").equals("1")) {
                FrameUtil.minimizeAllFrames();
            } else {
                CyderCommon.exit(ExitCondition.GenesisControlledExit);
            }
        } else if (commandIs("define")) {
            if (args.size() > 0) {
                println(StringUtil.getDefinition(argsToString()));
            } else {
                println("define usage: define YOUR_WORD/expression");
            }
        } else if (commandIs("wikisum")) {
            if (args.size() > 0) {
                println(StringUtil.getWikipediaSummary(argsToString()));
            } else {
                println("wikisum usage: wikisum YOUR_WORD/expression");
            }
        } else if (commandIs("pixelate") && checkArgsLength(0)) {
            if (ImageUtil.solidColor(ConsoleFrame.getConsoleFrame().getCurrentBackground().getReferenceFile())) {
                println("Silly " + UserUtil.extractUser().getName() + "; your background " +
                        "is a solid color :P");
            } else {
                CyderThreadRunner.submit(() -> {
                    String input =
                            new GetterUtil().getString("Pixel size","Enter any integer", "Pixelate");

                    if (StringUtil.isNull(input))
                        return;

                    try {
                        int pixelSize = Integer.parseInt(input);

                        if (pixelSize > 0) {
                            BufferedImage img = ImageUtil.pixelate(ImageIO.read(ConsoleFrame.getConsoleFrame().
                                    getCurrentBackground().getReferenceFile().getAbsoluteFile()), pixelSize);

                            String newName = FileUtil.getFilename(ConsoleFrame.getConsoleFrame()
                                    .getCurrentBackground().getReferenceFile().getName())
                                    + "_Pixelated_Pixel_Size_" + pixelSize + ".png";

                            File saveFile = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() +
                                    "/Backgrounds/" + newName);

                            ImageIO.write(img, "png", saveFile);

                            println("Background pixelated and saved as a separate background file.");

                            ConsoleFrame.getConsoleFrame().setBackgroundFile(saveFile);
                        }
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }, "Image Pixelator");
            }
        } else if (commandIs("hide")) {
            ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().minimizeAnimation();
        } else if (commandIs("analyzecode")) {
            if (CyderCommon.JAR_MODE) {
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
                       if (finalStartDir != null) {
                           int totalLines = StatUtil.totalLines(finalStartDir);
                           int codeLines = StatUtil.totalJavaLines(finalStartDir);
                           int blankLines = StatUtil.totalBlankLines(finalStartDir);
                           int commentLines = StatUtil.totalComments(finalStartDir);
                           int javaFiles = StatUtil.totalJavaFiles(finalStartDir);

                           println("Total lines: " + totalLines);
                           println("Code lines: " + codeLines);
                           println("Blank lines: " + blankLines);
                           println("Comment lines: " + commentLines);
                           println("Java Files: " + javaFiles);

                           double ratio = ((double) codeLines / (double) commentLines);
                           println("Code to comment ratio: " + new DecimalFormat("#0.00").format(ratio));
                       }
                   }, "Code Analyzer");
                } else {
                    println("analyzecode usage: analyzecode [path/to/the/root/directory] " +
                            "(leave path blank to analyze Cyder)");
                }
            }
        } else if (commandIs("f17")) {
            new Robot().keyPress(KeyEvent.VK_F17);
        }  else if (commandIs("debugstats")) {
            StatUtil.allStats();
        } else if (commandIs("binary")) {
            if (checkArgsLength(1) && getArg(0).matches("[0-9]+")) {
                 CyderThreadRunner.submit(() -> {
                     try {
                         println(getArg(0) + " converted to binary equals: "
                                 + Integer.toBinaryString(Integer.parseInt(getArg(0))));
                     } catch (Exception ignored) {}
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
            if (UserUtil.getUserData("minimizeonclose").equals("1")) {
                FrameUtil.minimizeAllFrames();
            } else {
                ConsoleFrame.getConsoleFrame().closeConsoleFrame(true, false);
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
                    File f = new File(getArg(1).trim());

                    if (f.exists()) {
                        printlnPriority("0b" + IOUtil.getBinaryString(f));
                    } else {
                        println("File: " + getArg(0).trim() + " does not exist.");
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
                    File f = new File(getArg(1).trim());

                    if (!f.exists())
                        throw new IllegalArgumentException("File does not exist");

                    if (FileUtil.getExtension(f).equalsIgnoreCase(".bin")) {
                        if (f.exists()) {
                            printlnPriority("0x" + IOUtil.getHexString(f).toUpperCase());
                        } else {
                            println("File: " + getArg(1).trim() + " does not exist.");
                        }
                    } else {
                        InputStream inputStream = new FileInputStream(f);
                        int numberOfColumns = 10;

                        StringBuilder sb = new StringBuilder();

                        long streamPtr = 0;
                        while (inputStream.available() > 0) {
                            final long col = streamPtr++ % numberOfColumns;
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
            ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().barrelRoll();
        } else if (commandIs("askew")) {
            ConsoleFrame.getConsoleFrame().rotateBackground(5);
        } else if (commandIs("logout")) {
            ConsoleFrame.getConsoleFrame().logout();
        } else if (commandIs("throw")) {
            ExceptionHandler.handle(new Exception("Error thrown on " + TimeUtil.userTime()));
        } else if (commandIs("clearops")) {
            ConsoleFrame.getConsoleFrame().clearCommandHistory();
            Logger.log(Logger.Tag.ACTION, "User cleared command history");
            println("Command history reset");
        } else if (commandIs("stopscript")) {
            MasterYoutubeThread.killAll();
            println("YouTube scripts have been killed.");
        } else if (commandIs("longword")) {
            for (int i = 0; i < args.size(); i++) {
                print("pneumonoultramicroscopicsilicovolcanoconiosis");
            }

            println("");
        } else if (commandIs("ip")) {
            println(InetAddress.getLocalHost().getHostAddress());
        }  else if (commandIs("computerproperties")) {
            println("This may take a second, since this feature counts your PC's free memory");
            StatUtil.computerProperties();
        } else if (commandIs("systemproperties")) {
            StatUtil.systemProperties();
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
            clc();
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
        } else if (commandIs("todos")) {
            if (CyderCommon.JAR_MODE) {
                println("Todos not available in jar mode");
            } else {
                int total = StatUtil.totalTodos(new File("cyder"));

                if (total > 0) {
                    println("Total todos: " + total);
                    println("Todos:");
                    println("----------------------------------------");
                    println(StatUtil.getTodos(new File("cyder")));
                } else {
                    println("No todos found, good job");
                }
            }
        } else if (commandIs("wipelogs")) {
            File[] logDirs = new File("logs").listFiles();
            int count = 0;

            for (File logDir : logDirs) {
                for (File log : logDir.listFiles()) {
                    if (FileUtil.getExtension(log).equals(".log")
                            && !log.equals(Logger.getCurrentLog())) {
                        //noinspection ResultOfMethodCallIgnored
                        log.delete();
                        count++;
                    }
                }
            }

            println("Deleted " + count + " log" + (count == 1 ? "" : "s"));
        } else if (commandIs("countlogs")) {
            File[] logDirs = new File("logs").listFiles();
            int count = 0;
            int days = 0;

            for (File logDir : logDirs) {
                days++;
                for (File log : logDir.listFiles()) {
                    if (FileUtil.getExtension(log).equals(".log")
                            && !logDir.equals(Logger.getCurrentLog())) {
                        count++;
                    }
                }
            }

            println("Number of log dirs: " + days);
            println("Number of logs: " + count);
        } else if (commandIs("opencurrentlog")) {
            IOUtil.openFileOutsideProgram(Logger.getCurrentLog().getAbsolutePath());
        } else if (commandIs("openlastlog")) {
            File[] logs = Logger.getCurrentLog().getParentFile().listFiles();

            if (logs.length == 1) {
                println("No previous logs found");
            } else if (logs.length > 1) {
                IOUtil.openFileOutsideProgram(logs[logs.length - 2].getAbsolutePath());
            }

        } else if (commandIs("play")) {
            boolean isURL = true;

            String url = argsToString();

            try {
                URL urlObj = new URL(url);
                URLConnection conn = urlObj.openConnection();
                conn.connect();
            } catch (Exception ignored) {
                isURL = false;
            }

            // it is a valid url so let youtube-dl try to download it
            if (isURL) {
                String saveDir = OSUtil.buildPath("dynamic",
                        "users",ConsoleFrame.getConsoleFrame().getUUID(), "Music");
                String extension = ".mp3";

                Runtime rt = Runtime.getRuntime();

                String parsedAsciiSaveName =
                        StringUtil.parseNonAscii(NetworkUtil.getURLTitle(url))
                                .replace("- YouTube","").trim();

                println("Downloading audio as: " + parsedAsciiSaveName + extension);

                // remove trailing periods
                while (parsedAsciiSaveName.endsWith("."))
                    parsedAsciiSaveName = parsedAsciiSaveName.substring(0, parsedAsciiSaveName.length() - 1);

                // if for some reason this case happens, account for it
                if (parsedAsciiSaveName.length() == 0)
                    parsedAsciiSaveName = SecurityUtil.generateUUID();

                final String finalParsedAsciiSaveName = parsedAsciiSaveName;

                String[] commands = {
                        "youtube-dl",
                        url,
                        "--extract-audio",
                        "--audio-format","mp3",
                        "--output", new File(saveDir).getAbsolutePath()
                        + OSUtil.FILE_SEP + finalParsedAsciiSaveName + ".%(ext)s"
                };

                CyderThreadRunner.submit(() -> {
                    try {
                        Process proc = rt.exec(commands);

                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                        // progress label for this download to update
                        CyderProgressBar audioProgress = new CyderProgressBar(CyderProgressBar.HORIZONTAL, 0, 10000);
                        CyderProgressUI ui = new CyderProgressUI();
                        ui.setColors(new Color[]{CyderColors.regularPink, CyderColors.regularBlue});
                        ui.setAnimationDirection(AnimationDirection.LEFT_TO_RIGHT);
                        ui.setShape(CyderProgressUI.Shape.SQUARE);
                        audioProgress.setUI(ui);
                        audioProgress.setMinimum(0);
                        audioProgress.setMaximum(10000);
                        audioProgress.setBorder(new LineBorder(Color.black, 2));
                        audioProgress.setBounds(0,0,400, 40);
                        audioProgress.setVisible(true);
                        audioProgress.setValue(0);
                        audioProgress.setOpaque(false);
                        audioProgress.setFocusable(false);
                        audioProgress.repaint();
                        printlnComponent(audioProgress);

                        Pattern updatePattern  = Pattern.compile(
                                "\\s*\\[download]\\s*([0-9]{1,3}.[0-9]%)\\s*of\\s*([0-9A-Za-z.]+)" +
                                        "\\s*at\\s*([0-9A-Za-z./]+)\\s*ETA\\s*([0-9:]+)");

                        String fileSize = null;

                        String outputString;

                        while ((outputString = stdInput.readLine()) != null) {
                            Matcher updateMatcher = updatePattern.matcher(outputString);

                            if (updateMatcher.find()) {
                                float progress = Float.parseFloat(updateMatcher.group(1)
                                        .replaceAll("[^0-9.]",""));
                                audioProgress.setValue((int) ((progress / 100.0) * audioProgress.getMaximum()));

                                if (fileSize == null) {
                                    fileSize = updateMatcher.group(2);
                                    println("Download size: " + fileSize);
                                }

                                // todo try and display in a better way
                                audioProgress.setToolTipText("Progress: " + progress + "%, Rate: "
                                        + updateMatcher.group(3) + ", ETA: " + updateMatcher.group(4));
                            }
                        }

                        File savedFile = new File(OSUtil.buildPath(saveDir, finalParsedAsciiSaveName + extension));

                        // get thumbnail url and file name to save it as
                        BufferedImage save = YoutubeUtil.getSquareThumbnail(url);
                        String name = finalParsedAsciiSaveName + ".png";

                        // init album art dir
                        File albumArtDir = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID()
                                + "/Music/AlbumArt");

                        // create if not there
                        if (!albumArtDir.exists())
                            albumArtDir.mkdir();

                        // create the reference file and save to it
                        File saveAlbumArt = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID()
                                + "/Music/AlbumArt/" + name);
                        ImageIO.write(save, "png", saveAlbumArt);

                        println("Download complete: saved as " + finalParsedAsciiSaveName + extension
                                + " and added to mp3 queue");
                        AudioPlayer.addToMp3Queue(savedFile);

                        ui.stopAnimationTimer();
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                        println("An exception occured while attempting to download: " + argsToString());
                    }
                }, "YouTube Download Progress Updater");
            } else {
                println("Play usage: Play [video URL supported by youtube-dl]");
            }
        }  else if (commandIs("pastebin")) {
            if (checkArgsLength(1)) {
                String urlString;
                if (getArg(0).contains("pastebin.com")) {
                    urlString = getArg(0);
                } else {
                    urlString = "https://pastebin.com/raw/" + getArg(1);
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
            if (args.size() > 0) {
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
        } else if (commandIs("xxx")) {
            CyderIcons.setCurrentCyderIcon(CyderIcons.xxxIcon);
            ConsoleFrame.getConsoleFrame().getConsoleCyderFrame()
                    .setIconImage(new ImageIcon("static/pictures/print/x.png").getImage());
            IOUtil.playAudio("static/audio/x.mp3");
        } else if (commandIs("issues")) {
            CyderThreadRunner.submit(() -> {
                GitHubUtil.Issue[] issues = GitHubUtil.getIssues();
                println(issues.length + " issue" + (issues.length == 1 ? "" : "s") + " found:\n");
                println("----------------------------------------");

                for (GitHubUtil.Issue issue: issues) {
                    println("Issue #" + issue.number);
                    println(issue.title);
                    println(issue.body);
                    println("----------------------------------------");
                }
            }, "GitHub Issue Getter");
        } else if (commandIs("blackpanther")|| commandIs("chadwickboseman")) {
            CyderThreadRunner.submit(() -> {
                clc();

                IOUtil.playAudio("static/audio/Kendrick Lamar - All The Stars.mp3");
                Font oldFont = outputArea.getJTextPane().getFont();
                outputArea.getJTextPane().setFont(new Font("BEYNO", Font.BOLD, oldFont.getSize()));
                BletchyThread.bletchy("RIP CHADWICK BOSEMAN",
                        false, 15, false);

                try {
                    //wait to reset font to original font
                    Thread.sleep(4000);
                } catch (Exception e) {
                    ExceptionHandler.silentHandle(e);
                }

                outputArea.getJTextPane().setFont(oldFont);
            }, "Chadwick Boseman Easteregg");
        } else if (commandIs("dst")) {
            CyderThreadRunner.submit(() -> {
                String location = IPUtil.getIpdata().getCity() + ", "
                        + IPUtil.getIpdata().getRegion() + ", "
                        +  IPUtil.getIpdata().getCountry_name();

                if (IPUtil.getIpdata().getTime_zone().isIs_dst()) {
                    println("Yes, DST is underway in " + location + ".");
                } else {
                    println("No, DST is not underway in " + location + ".");
                }
            }, "DST Checker");
        } else if (commandIs("test")) {
            ManualTests.launchTests();
        } else if (commandIs("tests")) {
            println("Valid tests to call:\n");
            printUnitTests();
            printManualTests();
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
            StatUtil.fileSizes();
        } else if (commandIs("badwords") ) {
            if (CyderCommon.JAR_MODE) {
                println("Bad words not available in jar mode");
            } else {
                CyderThreadRunner.submit(() -> {
                    println("Finding bad words:");
                    StatUtil.findBadWords();
                    println("Concluded");
                }, "Bad Word Finder");
            }
        } else if (commandIs("usb")) {
            CyderThreadRunner.submit(() -> {
               try {
                   Future<ArrayList<String>> futureLines = PyExecutor.executeUSBq();

                   while (!futureLines.isDone()) {
                       Thread.onSpinWait();
                   }

                   ArrayList<String> lines = futureLines.get();

                   for (String line : lines)  {
                       println(line);
                   }
               } catch (Exception ex) {
                   ExceptionHandler.handle(ex);
               }
            }, "USB Device Finder");
        } else if (commandIs("number2string") || commandIs("number2word")) {
            if (checkArgsLength(1)) {
                if (getArg(0).matches("[0-9]+")) {
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

                for (int i = 0 ; i < description.getTriggers().length ; i++) {
                    triggers.append(description.getTriggers()[i]);

                    if (i != description.getTriggers().length - 1)
                        triggers.append(", ");
                }

                println("Name: " + description.getName());
                println("Description: " + description.getDescription() + "\nTriggers: ["
                        + triggers.toString().trim() + "]");
                println("-------------------------------------");
            }
        } else if (commandIs("jarmode")) {
            println(CyderCommon.JAR_MODE ? "Cyder is currently running from a JAR"
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
                        "dynamic","users", ConsoleFrame.getConsoleFrame().getUUID(), getArg(0)));
                if (requestedDeleteFile.exists()) {
                    if (requestedDeleteFile.isDirectory()) {
                        if (OSUtil.delete(requestedDeleteFile)) {
                            println("Successfully deleted: " + requestedDeleteFile.getAbsolutePath());
                        } else {
                            println("Could not delete folder at this time");
                        }
                    } else if (requestedDeleteFile.isFile()) {
                        if (requestedDeleteFile.delete()) {
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
            ConsoleFrame.getConsoleFrame().originalChams();
        } else if (commandIs("opacity")) {
            JSlider opacitySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 100);
            opacitySlider.setBounds(0,0, 300, 50);
            CyderSliderUI UI = new CyderSliderUI(opacitySlider);
            UI.setThumbStroke(new BasicStroke(2.0f));
            UI.setSliderShape(SliderShape.CIRCLE);
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
                ConsoleFrame.getConsoleFrame().getConsoleCyderFrame()
                        .setOpacity(opacitySlider.getValue() / 100.0f);
                opacitySlider.repaint();
            });
            opacitySlider.setOpaque(false);
            opacitySlider.setToolTipText("Opacity");
            opacitySlider.setFocusable(false);
            opacitySlider.repaint();

            printlnComponent(opacitySlider);
        } else if (commandIs("pwd")) {
            println(System.getProperty("user.dir"));
        } else if (commandIs("download")) {
            if (checkArgsLength(1)) {
                if (NetworkUtil.isURL(getArg(0))) {
                    String responseName = NetworkUtil.getURLTitle(getArg(0));
                    String saveName = SecurityUtil.generateUUID();

                    if (responseName != null) {
                        if (responseName.length() > 0) {
                            saveName = responseName;
                        }
                    }

                    File saveFile = new File(OSUtil.buildPath("dynamic","users",
                            ConsoleFrame.getConsoleFrame().getUUID(), UserFile.FILES.getName(), saveName));

                    // clear text as soon as possible
                    ConsoleFrame.getConsoleFrame().getInputField().setText("");

                    println("Saving file: " + saveName + " to files directory");

                    CyderThreadRunner.submit(() -> {
                        if (NetworkUtil.downloadResource(getArg(0), saveFile)) {
                            println("Successfully saved");
                        } else {
                            println("Error: could not download at this time");
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
        }

        else ret = false;

         if (ret)
             Logger.log(Logger.Tag.HANDLE_METHOD, "GENERAL COMMAND HANDLED");

         return ret;
    }

    //sub-handle methods in the order they appear above --------------------------

    /**
     * Checks of the provided command is a URL and if so, opens a connection to it.
     *
     * @param command the command to attempt to open as a URL
     * @return whether the command was indeed a valid URL
     */
    private boolean isURLCheck(String command) {
        boolean ret = false;

        try {
            URL url = new URL(command);
            url.openConnection();
            ret = true;
            NetworkUtil.openUrl(command);
        } catch (Exception ignored) {}

        // log before returning
        if (ret) {
            Logger.log(Logger.Tag.HANDLE_METHOD, "CONSOLE URL FUNCTION HANDLED");
        }

        return ret;
    }

    /**
     * Determines if the command was a simple evaluable function such as floor() or pow().
     * Valid expressions:
     *      abs - 1 arg, returns the absolute value
     *      ceil - 1 arg, returns the ceiling
     *      floor - 1 arg, returns the floor
     *      log - 1 arg, returns the natural log
     *      log10 - 1 arg, returns the base 10 log
     *      max - 2 args, returns the max
     *      min - 2 args, returns the min
     *      pow - 2 args, returns the first raised to the power of the second
     *      round - 1 arg, round the arg to a whole number
     *      sqrt - 1 arg, returns the sqrt of the number
     *      convert2 - 1 arg, converts the number to binary
     *
     * @param command the command to attempt to evaluate as a simple math library call
     * @return whether the command was a simple math library call
     */
    private boolean handleMath(String command) {
        boolean ret = true;

        try {
            int firstParen = command.indexOf("(");
            int comma = command.indexOf(",");
            int lastParen = command.indexOf(")");

            String mathop;
            double param1 = 0.0;
            double param2 = 0.0;

            if (firstParen != -1) {
                mathop = command.substring(0, firstParen);

                if (comma != -1) {
                    param1 = Double.parseDouble(command.substring(firstParen + 1, comma));

                    if (lastParen != -1) {
                        param2 = Double.parseDouble(command.substring(comma + 1, lastParen));
                    }
                } else if (lastParen != -1) {
                    param1 = Double.parseDouble(command.substring(firstParen + 1, lastParen));
                }

                if (mathop.equalsIgnoreCase("abs")) {
                    println(Math.abs(param1));
                } else if (mathop.equalsIgnoreCase("ceil")) {
                    println(Math.ceil(param1));
                } else if (mathop.equalsIgnoreCase("floor")) {
                    println(Math.floor(param1));
                } else if (mathop.equalsIgnoreCase("log")) {
                    println(Math.log(param1));
                } else if (mathop.equalsIgnoreCase("log10")) {
                    println(Math.log10(param1));
                } else if (mathop.equalsIgnoreCase("max")) {
                    println(Math.max(param1, param2));
                } else if (mathop.equalsIgnoreCase("min")) {
                    println(Math.min(param1, param2));
                } else if (mathop.equalsIgnoreCase("pow")) {
                    println(Math.pow(param1, param2));
                } else if (mathop.equalsIgnoreCase("round")) {
                    println(Math.round(param1));
                } else if (mathop.equalsIgnoreCase("sqrt")) {
                    println(Math.sqrt(param1));
                } else if (mathop.equalsIgnoreCase("convert2")) {
                    println(Integer.toBinaryString((int) (param1)));
                } else ret = false;
            } else ret = false;
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
            ret = false;
        }

        //log before returning
        if (ret)
            Logger.log(Logger.Tag.HANDLE_METHOD, "CONSOLE MATH FUNCTION HANDLED");
        return ret;
    }

    /**
     * Determines if the provided command was a mathematical expression and if so, evaluates it.
     *
     * @param command the command to attempt to evaluate as a mathematical expression
     * @return whether the command was a mathematical expression
     */
    private boolean evaluateExpression(String command) {
        boolean ret = false;

        try {
            println(new DoubleEvaluator().evaluate(StringUtil.firstCharToLowerCase(command.trim())));
            ret = true;
        } catch (Exception ignored) {}

        if (ret)
            Logger.log(Logger.Tag.HANDLE_METHOD, "CONSOLE MATH HANDLED");
        return ret;
    }

    /**
     * Checks the command for an intended preference toggle and if so, toggles the preference.
     * The user may include 1, true, 0, or false with the command to specify the value of the targeted preference.
     *
     * @param targetedPreference the preference to change
     * @return whether a preference was toggled/handled
     */
    private boolean preferenceCheck(String targetedPreference) {
        boolean ret = false;

        for (Preferences.Preference pref : Preferences.getPreferences()) {
            if (targetedPreference.equalsIgnoreCase(pref.getID()) && !pref.getDisplayName().equals("IGNORE")) {
                if (targetedPreference.contains("1") || targetedPreference.toLowerCase().contains("true")) {
                    UserUtil.setUserData(pref.getID(), "1");
                    println(pref.getDisplayName() + " set to true");
                } else if (targetedPreference.contains("0") || targetedPreference.toLowerCase().contains("false")) {
                    UserUtil.setUserData(pref.getID(), "0");
                    println(pref.getDisplayName() + " set to false");
                } else {
                    String newVal = UserUtil.getUserData(pref.getID()).equals("1") ? "0" : "1";
                    UserUtil.setUserData(pref.getID(), newVal);
                    println(pref.getDisplayName() + " set to " + (newVal.equals("1") ? "true" : "false"));
                }

                Preferences.invokeRefresh(pref.getID());
                ret = true;
            }
        }

        if (ret)
            Logger.log(Logger.Tag.HANDLE_METHOD, "CONSOLE PREFERENCE TOGGLE HANDLED");
        return ret;
    }

    /**
     * Determines if the command intended to invoke a manual test from test/ManualTests.
     *
     * @param command the command to attempt to recognize as a manual test
     * @return whether the command was handled as a manual test call
     */
    private boolean manualTestCheck(String command) {
        boolean ret = false;

        command = command.toLowerCase();

        if (command.contains("test")) {
            //noinspection InstantiationOfUtilityClass
            ManualTests mtw = new ManualTests();

            for (Method m : mtw.getClass().getMethods()) {
                if (m.getName().equalsIgnoreCase(command) && m.getParameterTypes().length == 0) {
                    try {
                        m.invoke(mtw);
                        println("Invoking manual test: " + m.getName());
                        ret = true;
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                    break;
                }
            }
        }

        if (ret)
            Logger.log(Logger.Tag.HANDLE_METHOD, "CONSOLE MANUAL TEST REFLECTION FIRE HANDLED");
        return ret;
    }

    /**
     * Determines if the command intended to invoke a unit test.
     *
     * @param command the unit test to invoke if recognized from unit tests
     * @return whether the command was recognized as a unit test call
     */
    private boolean unitTestCheck(String command) {
        boolean ret = false;

        command = command.toLowerCase();

        if (command.contains("test")) {
            UnitTests tests = new UnitTests();

            for (Method m : tests.getClass().getMethods()) {
                if (m.getName().equalsIgnoreCase(command) && m.getParameterTypes().length == 0) {
                    try {
                        m.invoke(tests);
                        println("Invoking unit test: " + m.getName());
                        ret = true;
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                    break;
                }
            }
        }

        if (ret)
            Logger.log(Logger.Tag.HANDLE_METHOD, "CONSOLE UNIT TEST REFLECTION FIRE HANDLED");
        return ret;
    }

    /**
     * The final handle method for if all other handle methods failed.
     */
    private void unknownInput() {
        CyderThreadRunner.submit(() -> {
            try {
                println("Unknown command");
                ConsoleFrame.getConsoleFrame().flashSuggestionButton(4);

                Future<Optional<String>> similarCommand = ReflectionUtil.getSimilarCommand(command);

                //wait for script to finish
                while (!similarCommand.isDone()) {
                    Thread.onSpinWait();
                }

                //if future returned and not null/empty value
                if (similarCommand.get().isPresent()) {
                    String simCom = similarCommand.get().get();

                    if (!StringUtil.isNull(simCom)) {
                        assert simCom.contains(",");
                        String[] parts = simCom.split(",");
                        assert parts.length == 2;

                        float tol = Float.parseFloat(parts[1]);

                        Logger.log(Logger.Tag.ACTION, "Similar command to \""
                                + command + "\" found with tol of " + tol + ", command = \"" + parts[0] + "\"");

                        if (tol > CyderNumbers.SIMILAR_COMMAND_TOL) {
                            println("Most similar command: \"" + parts[0] + "\"");
                        }
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Unknown Input Handler");
    }

    //end handle methods --------------------------------

    /**
     * Returns the current user issued command.
     *
     * @return the current user issued command
     */
    public final String getCommand() {
        return this.command;
    }

    /**
     * Returns whether the arguments array contains the expected number of arguments.
     *
     * @param expectedSize the expected size of the command arguments
     * @return whether the arguments array contains the expected number of arguments
     */
    private boolean checkArgsLength(int expectedSize) {
        return this.args.size() == expectedSize;
    }

    /**
     * Returns the command argument at the provided index.
     * Returns null if the index is out of bounds instead of throwing.
     *
     * @param index the index to retrieve the command argument of
     * @return the command argument at the provided index
     */
    private @Nullable String getArg(int index) {
        if (index + 1 <= args.size() && index >= 0) {
            return args.get(index);
        } else throw new IllegalArgumentException("Provided index is out of bounds: " + index
                + ", argument size: " + args.size());
    }

    /**
     * Returns the arguments in String form separated by spaces.
     *
     * @return the arguments in String form separated by spaces
     */
    private String argsToString() {
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
    private String argsAndCommandToString() {
        StringBuilder sb = new StringBuilder();
        sb.append(command.trim()).append(" ");

        for (int i = 0 ; i < args.size() ; i++) {
            sb.append(args.get(i)).append(i == args.size() - 1 ? "" : " ");
        }

        return sb.toString();
    }

    //end argument/command accessors --------------------------------

    /**
     * Prints the available unit tests to the linked JTextPane.
     */
    public final void printUnitTests() {
        println("Unit Tests:");
        UnitTests ut = new UnitTests();

        for (Method m : ut.getClass().getMethods()) {
            if (m.getName().toLowerCase().contains("test"))
                println(m.getName());
        }
    }

    /**
     * Prints the available manual tests that follow the standard
     * naming convention to the linked JTextPane.
     */
    public final void printManualTests() {
        println("Manual tests:");
        //noinspection InstantiationOfUtilityClass
        ManualTests mtw = new ManualTests();

        for (Method m : mtw.getClass().getMethods()) {
            if (m.getName().toLowerCase().contains("test"))
                println(m.getName());
        }
    }

    // end printing tests ----------------------------------

    /**
     * Handles a secondary input following a subsequent operation that was sent to handle().
     *
     * @param input the secondary input to handle
     */
    public final void handleSecond(String input) {
        try {
            String desc = getUserInputDesc();

            //tests on desc which should have been set from the first handle method
            if (desc.equals("YOUR PREVIOUSLY SET DESC") && StringUtil.hasWord(input, "YOUR WORD")) {
                println("");
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

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
        return this.outputArea.getJTextPane();
    }

    /**
     * Getter for this instance's input mode. Used by ConsoleFrame to trigger a handleSecond call.
     *
     * @return the value of user input mode
     */
    public final boolean getUserInputMode() {
        return this.userInputMode;
    }

    /**
     * Set the value of secondary input mode. Used by ConsoleFrame to trigger a handleSecond call.
     *
     * @param b the value of input mode
     */
    public final void setUserInputMode(boolean b) {
        this.userInputMode = b;
    }

    /**
     * Returns the expected secondary input description.
     *
     * @return the input description
     */
    private String getUserInputDesc() {
        return this.userInputDesc;
    }

    /**
     * Sets this instance's secondary input description.
     *
     * @param s the description of the input we expect to receive next
     */
    public final void setUserInputDesc(String s) {
        this.userInputDesc = s;
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
     * Returns a String representation of this input handler object.
     *
     * @return a String representation of this input handler object
     */
    @Override
    public final String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }

    //printing queue methods and logic ----------------------------

    /**
     * Semaphore for adding objects to both consolePrintingList and consolePriorityPrintingList.
     */
    private final Semaphore makePrintingThreadsafeAgain = new Semaphore(1);

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
                makePrintingThreadsafeAgain.acquire();
            } catch (InterruptedException ex) {
                ExceptionHandler.handle(ex);
            }
            boolean ret = super.add(e);
            makePrintingThreadsafeAgain.release();
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
                makePrintingThreadsafeAgain.acquire();
            } catch (InterruptedException ex) {
                ExceptionHandler.handle(ex);
            }
            boolean ret = super.add(e);
            makePrintingThreadsafeAgain.release();
            return ret;
        }
    };

    /**
     * Boolean describing whether the console printing animation thread has been invoked and begun.
     */
    private boolean printingAnimationInvoked = false;

    /**
     * Begins the printing animation for the linked JTextPane. The typing animation is only
     * used if the user preference is enabled.
     */
    public final void startConsolePrintingAnimation() {
        if (printingAnimationInvoked)
            return;

        printingAnimationInvoked = true;

        consolePrintingList.clear();
        consolePriorityPrintingList.clear();

        int charTimeout = 15;
        int lineTimeout = 200;

        CyderThreadRunner.submit(() -> {
            try {
                boolean typingAnimationLocal = UserUtil.getUserData("typinganimation").equals("1");
                long lastPull = System.currentTimeMillis();
                long dataPullTimeout = 3000;

                while (!ConsoleFrame.getConsoleFrame().isClosed()) {
                    //update typingAnimationLocal every 3 seconds to reduce resource usage
                    if (System.currentTimeMillis() - lastPull > dataPullTimeout) {
                        lastPull = System.currentTimeMillis();
                        typingAnimationLocal = UserUtil.getUserData("typinganimation").equals("1");
                    }

                    if (consolePriorityPrintingList.size() > 0) {
                        Object line = consolePriorityPrintingList.removeFirst();
                        Logger.log(Logger.Tag.CONSOLE_OUT,line);

                        if (redirection) {
                            redirectionWrite(line);
                        } else {
                            if (line instanceof String) {
                                StyledDocument document = (StyledDocument) outputArea.getJTextPane().getDocument();
                                document.insertString(document.getLength(), (String) line, null);
                                outputArea.getJTextPane().setCaretPosition(outputArea.getJTextPane().getDocument().getLength());
                            } else if (line instanceof JComponent) {
                                String componentUUID = SecurityUtil.generateUUID();
                                Style cs = outputArea.getJTextPane().getStyledDocument().addStyle(componentUUID, null);
                                StyleConstants.setComponent(cs, (Component) line);
                                outputArea.getJTextPane().getStyledDocument().insertString(outputArea.getJTextPane()
                                        .getStyledDocument().getLength(), componentUUID, cs);
                            } else if (line instanceof ImageIcon) {
                                outputArea.getJTextPane().insertIcon((ImageIcon) line);
                            } else {
                                println("[UNKNOWN OBJECT]: " + line);
                            }
                        }
                    }
                    //regular will perform a typing animation on strings if no method
                    // is currently running, such as random YouTube or bletchy, that would cause
                    // concurrency issues
                    else if (consolePrintingList.size() > 0) {
                        Object line = consolePrintingList.removeFirst();
                        Logger.log(Logger.Tag.CONSOLE_OUT,line);

                        if (redirection) {
                            redirectionWrite(line);
                        } else {
                            if (line instanceof String) {
                                if (typingAnimationLocal) {
                                    if (finishPrinting) {
                                        StyledDocument document = (StyledDocument) outputArea.getJTextPane().getDocument();
                                        document.insertString(document.getLength(), (String) line, null);
                                        outputArea.getJTextPane().setCaretPosition(outputArea.getJTextPane()
                                                .getDocument().getLength());
                                    } else {
                                        outputArea.getSemaphore().acquire();
                                        for (char c : ((String) line).toCharArray()) {
                                            innerConsolePrint(c);

                                            if (!finishPrinting)
                                                //noinspection BusyWait
                                                Thread.sleep(charTimeout);
                                        }
                                        outputArea.getSemaphore().release();
                                    }
                                } else {
                                    StyledDocument document = (StyledDocument) outputArea.getJTextPane().getDocument();
                                    document.insertString(document.getLength(), (String) line, null);
                                    outputArea.getJTextPane().setCaretPosition(outputArea.getJTextPane().getDocument().getLength());
                                }
                            } else if (line instanceof JComponent) {
                                String componentUUID = SecurityUtil.generateUUID();
                                Style cs = outputArea.getJTextPane().getStyledDocument().addStyle(componentUUID, null);
                                StyleConstants.setComponent(cs, (Component) line);
                                outputArea.getJTextPane().getStyledDocument().insertString(
                                        outputArea.getJTextPane().getStyledDocument().getLength(), componentUUID, cs);
                            } else if (line instanceof ImageIcon) {
                                outputArea.getJTextPane().insertIcon((ImageIcon) line);
                            } else {
                                println("[UNKNOWN OBJECT]: " + line);
                            }
                        }
                    }
                    // lists are empty
                    else {
                        //fix possible escape from last command
                        finishPrinting = false;
                    }

                    if (!finishPrinting && typingAnimationLocal)
                        //noinspection BusyWait
                        Thread.sleep(lineTimeout);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Console Printing Animation");
    }

    /**
     * The increment we are currently on for inner char printing.
     * Used to determine when to play a typing animation sound.
     */
    private static int playInc = 0;

    /**
     * The frequency at which we should play a printing animation sound.
     */
    private static final int playRate = 2;

    /**
     * Appends the provided char to the linked JTextPane and plays
     * a printing animation sound if playInc == playRate.
     *
     * @param c the character to append to the JTextPane
     */
    private void innerConsolePrint(char c) {
        try {
            boolean capsMode = false;

            //this sometimes throws so we ignore it
            try {
                capsMode = UserUtil.extractUser().getCapsmode().equals("1");
            } catch (Exception ignored) {}

            StyledDocument document = (StyledDocument) outputArea.getJTextPane().getDocument();
            document.insertString(document.getLength(),
                    capsMode ? String.valueOf(c).toUpperCase() : String.valueOf(c), null);
            outputArea.getJTextPane().setCaretPosition(outputArea.getJTextPane().getDocument().getLength());

            if (playInc == playRate - 1) {
                if (!finishPrinting && UserUtil.extractUser().getTypingsound().equals("1")) {
                    IOUtil.playSystemAudio("static/audio/Typing.mp3");
                    playInc = 0;
                }
            } else {
                playInc++;
            }
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
        }
    }

    /**
     * Prints the provided ImageIcon and a new line to the linked JTextPane.
     *
     * @param icon the icon to print to the linked JTextPane
     */
    public final void printlnImage(ImageIcon icon) {
        consolePrintingList.add(icon);
        consolePrintingList.add("\n");
    }

    /**
     * Prints the provided ImageIcon to the linked JTextPane.
     *
     * @param icon the icon to print to the linked JTextPane
     */
    public final void printImage(ImageIcon icon) {
        consolePrintingList.add(icon);
    }

    /**
     * Prints the provided ImageIcon and a new line to the linked JTextPane.
     *
     * @param filename the filename of the image to print to the JTextPane
     */
    private void printlnImage(String filename) {
        consolePrintingList.add(new ImageIcon(filename));
        consolePrintingList.add("\n");
    }

    /**
     * Prints the provided ImageIcon and a new line to the linked JTextPane.
     *
     * @param filename the filename of the image to print to the JTextPane
     */
    public final void printImage(String filename) {
        consolePrintingList.add(new ImageIcon(filename));
    }

    /**
     * Prints the provided component and a new line to the linked JTextPane.
     *
     * @param c the component to print to the JTextPane
     */
    public final void printlnComponent(Component c) {
        consolePrintingList.add(c);
        consolePrintingList.add("\n");
    }

    /**
     * Prints the provided component to the linked JTextPane.
     *
     * @param c the component to print to the JTextPane
     */
    public final void printComponent(Component c) {
        consolePrintingList.add(c);
    }

    /**
     * Prints the provided String to the linked JTextPane.
     *
     * @param usage the string to print to the JTextPane
     */
    public final void print(String usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(usage);
        else
            consolePrintingList.add(usage);
    }

    /**
     * Prints the provided int to the linked JTextPane.
     *
     * @param usage the int to print to the JTextPane
     */
    public final void print(int usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(Integer.toString(usage));
        else
            consolePrintingList.add(Integer.toString(usage));
    }

    /**
     * Prints the provided double to the linked JTextPane.
     *
     * @param usage the double to print to the JTextPane
     */
    public final void print(double usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(Double.toString(usage));
        else
            consolePrintingList.add(Double.toString(usage));
    }

    /**
     * Prints the provided boolean to the linked JTextPane.
     *
     * @param usage the boolean to print to the JTextPane
     */
    public final void print(boolean usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(Boolean.toString(usage));
        else
            consolePrintingList.add(Boolean.toString(usage));
    }

    /**
     * Prints the provided float to the linked JTextPane.
     *
     * @param usage the float to print to the JTextPane
     */
    public final void print(float usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(Float.toString(usage));
        else
            consolePrintingList.add(Float.toString(usage));
    }

    /**
     * Prints the provided long to the linked JTextPane.
     *
     * @param usage the long to print to the JTextPane
     */
    public final void print(long usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(Long.toString(usage));
        else
            consolePrintingList.add(Long.toString(usage));

    }

    /**
     * Prints the provided char to the JTextPane
     *
     * @param usage the char to print to the JTextPane
     */
    public final void print(char usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(String.valueOf(usage));
        else
            consolePrintingList.add(String.valueOf(usage));
    }

    /**
     * Prints the provided object to the JTextPane
     *
     * @param usage the object to print to the JTextPane
     */
    public final void print(Object usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(usage.toString());
        else
            consolePrintingList.add(usage.toString());
    }

    /**
     * Prints the provided String and a newline to the linked JTextPane.
     *
     * @param usage the string to print to the JTextPane
     */
    public final void println(String usage) {
        print(usage + "\n");
    }

    /**
     * Prints the provided int and a newline to the linked JTextPane.
     *
     * @param usage the int to print to the JTextPane
     */
    public final void println(int usage) {
        print(usage + "\n");
    }

    /**
     * Prints the provided double and a newline to the linked JTextPane.
     *
     * @param usage the double to print to the JTextPane
     */
    public final void println(double usage) {
        print(usage + "\n");
    }

    /**
     * Prints the provided boolean and a newline to the linked JTextPane.
     *
     * @param usage the boolean to print to the JTextPane
     */
    public final void println(boolean usage) {
        print(usage + "\n");
    }

    /**
     * Prints the provided float and a newline to the linked JTextPane.
     *
     * @param usage the float to print to the JTextPane
     */
    public final void println(float usage) {
        print(usage + "\n");
    }

    /**
     * Prints the provided long and a newline to the linked JTextPane.
     *
     * @param usage the long to print to the JTextPane
     */
    public final void println(long usage) {
        print(usage + "\n");
    }

    /**
     * Prints the provided char and a newline to the linked JTextPane.
     *
     * @param usage the char to print to the JTextPane
     */
    public final void println(char usage) {
        print(usage + "\n");
    }

    /**
     * Prints the provided object and a newline to the linked JTextPane.
     *
     * @param usage the object to print to the JTextPane
     */
    public final void println(Object usage) {
        print(usage + "\n");
    }

    /**
     * Prints the provided String lines to the linked JTextPane.
     * Note that new lines are automatically added in this so the passed
     * array may be strings that do not end with new lines.
     *
     * @param lines the lines to print to the JTextPane
     */
    public final void printlns(String[] lines) {
        for (String line : lines)
            println(line);
    }

    /**
     * Priority printing method to print the provided ImageIcon and a new line to the JTextPane.
     *
     * @param icon the icon to print to the JTextPane
     */
    public final void printlnImagePriority(ImageIcon icon) {
        consolePrintingList.add(icon);
        consolePrintingList.add("\n");
    }

    /**
     * Priority printing method to print the provided ImageIcon to the JTextPane.
     *
     * @param icon the icon to print to the JTextPane
     */
    public final void printImagePriority(ImageIcon icon) {
        consolePriorityPrintingList.add(icon);
    }

    /**
     * Priority printing method to print the provided ImageIcon and a new line to the JTextPane.
     *
     * @param filename the filename of the icon to print to the JTextPane
     */
    public final void printlnImagePriority(String filename) {
        consolePriorityPrintingList.add(new ImageIcon(filename));
        consolePriorityPrintingList.add("\n");
    }

    /**
     * Priority printing method to print the provided String to the JTextPane.
     *
     * @param filename the filename of the icon to print to the JTextPane
     */
    public final void printImagePriority(String filename) {
        consolePriorityPrintingList.add(new ImageIcon(filename));
    }

    /**
     * Priority printing method to print the provided Component and a new line to the JTextPane.
     *
     * @param c the component to print to the JTextPane
     */
    public final void printlnComponentPriority(Component c) {
        consolePriorityPrintingList.add(c);
        consolePriorityPrintingList.add("\n");
    }

    /**
     * Priority printing method to print the provided Component to the JTextPane.
     *
     * @param c the component to print to the JTextPane
     */
    public final void printComponentPriority(Component c) {
        consolePriorityPrintingList.add(c);
    }

    /**
     * Priority printing method to print the provided String to the JTextPane.
     *
     * @param usage the string to print to the JTextPane
     */
    private void printPriority(String usage) {
        consolePriorityPrintingList.add(usage);
    }

    /**
     * Priority printing method to print the provided int to the JTextPane.
     *
     * @param usage the int to print to the JTextPane
     */
    public final void printPriority(int usage) {
        consolePriorityPrintingList.add(Integer.toString(usage));
    }

    /**
     * Priority printing method to print the provided double to the JTextPane.
     *
     * @param usage the double to print to the JTextPane
     */
    public final void printPriority(double usage) {
        consolePriorityPrintingList.add(Double.toString(usage));
    }

    /**
     * Priority printing method to print the provided boolean to the JTextPane.
     *
     * @param usage the boolean to print to the JTextPane
     */
    public final void printPriority(boolean usage) {
        consolePriorityPrintingList.add(Boolean.toString(usage));
    }

    /**
     * Priority printing method to print the provided float to the JTextPane.
     *
     * @param usage the float to print to the JTextPane
     */
    public final void printPriority(float usage) {
        consolePriorityPrintingList.add(Float.toString(usage));
    }

    /**
     * Priority printing method to print the provided long to the JTextPane.
     *
     * @param usage the long to print to the JTextPane
     */
    public final void printPriority(long usage) {
        consolePriorityPrintingList.add(Long.toString(usage));
    }

    /**
     * Priority printing method to print the provided char to the JTextPane.
     *
     * @param usage the char to print to the JTextPane
     */
    public final void printPriority(char usage) {
        consolePriorityPrintingList.add(String.valueOf(usage));
    }

    /**
     * Priority printing method to print the provided object to the JTextPane.
     *
     * @param usage the object to print to the JTextPane
     */
    public final void printPriority(Object usage) {
        consolePriorityPrintingList.add(usage.toString());
    }

    /**
     * Priority printing method to print the provided String and a new line to the JTextPane.
     *
     * @param usage the string to print to the JTextPane
     */
    private void printlnPriority(String usage) {
        printPriority(usage + "\n");
    }

    /**
     * Priority printing method to print the provided int and a new line to the JTextPane.
     *
     * @param usage the provided int to print to the JTextPane
     */
    public final void printlnPriority(int usage) {
        printPriority(usage + "\n");
    }

    /**
     * Priority printing method to print the provided double and a new line to the JTextPane.
     *
     * @param usage the double to print to the JTextPane
     */
    public final void printlnPriority(double usage) {
        printPriority(usage + "\n");
    }

    /**
     * Priority printing method to print the provided boolean and a new line to the JTextPane.
     *
     * @param usage the boolean to print to the JTextPane
     */
    public final void printlnPriority(boolean usage) {
        printPriority(usage + "\n");
    }

    /**
     * Priority printing method to print the provided float and a new line to the JTextPane.
     *
     * @param usage the float to print to the JTextPane
     */
    public final void printlnPriority(float usage) {
        printPriority(usage + "\n");
    }

    /**
     * Priority printing method to print the provided long and a new line to the JTextPane.
     *
     * @param usage the long to print to the JTextPane
     */
    public final void printlnPriority(long usage) {
        printPriority(usage + "\n");
    }

    /**
     * Priority printing method to print the provided char and a new line to the JTextPane.
     *
     * @param usage the char to print to the JTextPane
     */
    public final void printlnPriority(char usage) {
        printPriority(usage + "\n");
    }

    /**
     * Priority printing method to print the provided object and a new line to the JTextPane.
     *
     * @param usage the object to print to the JTextPane
     */
    public final void printlnPriority(Object usage) {
        printPriority(usage + "\n");
    }

    /**
     * Determines if the current command equals the provided text ignoring case.
     *
     * @param compare the string to check for case-insensitive equality to command
     * @return if the current command equals the provided text ignoring case
     */
    private boolean commandIs(String compare) {
        return command.equalsIgnoreCase(compare);
    }

    /**
     * Removes all components from the linked JTextPane.
     */
    private void clc() {
        outputArea.getJTextPane().setText("");
    }

    /**
     * Removes the last "thing" added to the JTextPane whether it's a component,
     *  icon, or string of multi-lined text.
     *
     *  In more detail, this method figures out what it'll be removing and then determines how many calls
     *   are needed to {@link StringUtil#removeLastLine()}
     */
    public final void removeLast() {
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

                    if (value.toString().toLowerCase().contains("icon") || value.toString().toLowerCase().contains("component")) {
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
    public final String getLastTextLine() {
        return outputArea.getStringUtil().getLastTextLine();
    }

    /**
     * Removes the last line added to the linked JTextPane. This could appear to remove nothing,
     *  but really be removing just a newline (line break) character.
     */
    private void removeLastLine() {
       outputArea.getStringUtil().removeLastLine();
    }

    //redirection logic ---------------------------------------

    /**
     * Semaphore used to ensure all things that need to
     * be written to the redirectionFile are written to it.
     * This also ensures that multiple redirections
     */
    private final Semaphore redirectionSem = new Semaphore(1);

    /**
     * Writes the provided object to the redirection file instead of the JTextPane.
     *
     * @param object the object to invoke toString() on and write to the current redirectionFile
     */
    private void redirectionWrite(Object object) {
        if (!redirectionFile.exists())
            throw new IllegalStateException("Redirection file does not exist");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(redirectionFile, true))) {
            redirectionSem.acquire();
            writer.write(String.valueOf(object));
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
        //exit user input mode if in it
        setUserInputMode(false);

        //kill threads
        killThreads();

        //stop music
        IOUtil.stopAudio();

        //cancel dancing threads
        ConsoleFrame.getConsoleFrame().stopDancing();

        //finish printing anything in printing queue
        finishPrinting = true;

        //inform user we escaped
        consolePrintingList.add("Escaped\n");
    }
}