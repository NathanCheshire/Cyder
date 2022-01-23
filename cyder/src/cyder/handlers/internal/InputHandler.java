package cyder.handlers.internal;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderNums;
import cyder.consts.CyderStrings;
import cyder.cyderuser.UserCreator;
import cyder.enums.ScreenPosition;
import cyder.genesis.GenesisShare;
import cyder.genesis.GenesisShare.Preference;
import cyder.handlers.external.AudioPlayer;
import cyder.helperscripts.PyExecutor;
import cyder.objects.MultiString;
import cyder.threads.BletchyThread;
import cyder.threads.MasterYoutubeThread;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderCaret;
import cyder.ui.CyderFrame;
import cyder.utilities.*;
import test.java.Debug;
import test.java.ManualTests;
import test.java.UnitTests;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

public class InputHandler {
    /**
     * The linked JTextPane.
     */
    private JTextPane outputArea;

    /**
     * boolean describing whether or not input should be passed to handle() or handleSecond().
     */
    private boolean userInputMode;

    /**
     * The description of the secondary input handleSecond will receive.
     */
    private String userInputDesc;

    /**
     * boolean describing whether or not to quickly append all remaining queued objects to the linked JTextPane.
     */
    private boolean finishPrinting;

    /**
     * The file to redirect the outputs of a command to if redirection is enabled.
     */
    private File redirectionFile;

    /**
     * Boolean describing whether or not possible command output should be redirected to the redirectionFile.
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
        //link JTextPane
        this.outputArea = outputArea;

        //init other JTextPane objects such as Threads

        //printing threads initialization with JTextPane and sem -------------------
        MasterYoutubeThread.initialize(outputArea, makePrintingThreadsafeAgain);
        BletchyThread.initialize(outputArea, makePrintingThreadsafeAgain);
    }

    //todo if command is not found attempt to find most similar one and if threshhold is 80% or above
    // then print that command as a suggestion

    //todo hwo would this even be matched? what can you parameterize?
    // if you can do indivial lines then maybe a python script to extract string args
    // from all commandIs("COMMAND EXTRACT HERE") and the paste in a json is the way to go

    /**
     * Handles preliminaries such as assumptions before passing input data to the subHandle methods.
     * Also sets the ops array to the found command and arguments
     *
     * @param command the command to handle preliminaries on before behind handled
     * @param userTriggered whether or not the provided operation was produced via a user
     * @return whether or not the process may proceed
     */
    private boolean handlePreliminaries(String command, boolean userTriggered) {
        //check for null link (should be impossible)
        if (outputArea == null)
            throw new IllegalStateException("Output area not set; what are you, some kind of European toy maker?");

        //if empty string don't do anything
        if (StringUtil.empytStr(command))
            return false;

        //reset redirection now since we have a new command
        redirection = false;
        redirectionFile = null;

        //trim command
        command = command.trim();

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
            SessionHandler.log(SessionHandler.Tag.CLIENT, this.command);
        } else {
            SessionHandler.log(SessionHandler.Tag.CLIENT, "[SIMULATED INPUT] " + this.command);
        }

        //check for requested redirection
        if (args.size() > 1 && getArg(0).equals(">")) {
            String filename = getArg(1);

            if (filename.trim().length() > 0) {
                //check for validity of requested filename
                if (IOUtil.isValidFilenameWindows(filename)) { //todo make dynamic
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
                && StringUtil.filterLanguage(this.command, true)) {
            println("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", but that language is prohibited.");
            return false;
        }

        return true;
    }

    /**
     * Handles the input and provides output if necessary to the linked JTextPane.
     *
     * @param op the operation that is being handled
     * @param userTriggered whether or not the provided op was produced via a user
     * @throws Exception for numerous reasons such as if the JTextPane is not linked
     */
    public void handle(String op, boolean userTriggered) throws Exception {
        if (!handlePreliminaries(op, userTriggered)) {
            SessionHandler.log(SessionHandler.Tag.HANDLE_METHOD, "FAILED PRELIMINARIES");
        }
        //primary checks
        else if (generalPrintsCheck() ||
                printImageCheck() ||
                ReflectionUtil.openWidget((command.trim() + " " + argsToString().trim()).trim()) ||
                cyderFrameMovementCheck() ||
                externalOpenerCheck() ||
                audioCommandCheck() ||
                generalCommandCheck()) {
            SessionHandler.log(SessionHandler.Tag.HANDLE_METHOD, "PRIMARY HANDLE");
        }
        //final checks
        else if (isURLCheck(command) ||
                handleMath(command) ||
                evaluateExpression(command) ||
                preferenceCheck(command) ||
                manualTestCheck(command) ||
                unitTestCheck(command)) {
            SessionHandler.log(SessionHandler.Tag.HANDLE_METHOD, "FINAL HANDLE");
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
                println("To be, or not to be, that is the question: Whether 'tis nobler in the mind to suffer the slings and arrows of "
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
            if (Math.random() <= 0.0001) {
                println("You're not going to beleive this, but it landed on its side.");
            } else if (Math.random() <= 0.5) {
                println("It's Heads!");
            } else {
                println("It's Tails!");
            }
        } else if (commandIs("hello") || commandIs("hi")) {
            int choice = NumberUtil.randInt(1, 7);

            switch (choice) {
                case 1:
                    println("Hello, " + ConsoleFrame.getConsoleFrame().getUsername() + ".");
                    break;
                case 2:
                    if (TimeUtil.isEvening())
                        println("Good evening, " + ConsoleFrame.getConsoleFrame().getUsername() + ". How can I help?");
                    else if (TimeUtil.isMorning())
                        println("Good monring, " + ConsoleFrame.getConsoleFrame().getUsername() + ". How can I help?");
                    else
                        println("Good afternoon, " + ConsoleFrame.getConsoleFrame().getUsername() + ". How can I help?");
                    break;
                case 3:
                    println("What's up, " + ConsoleFrame.getConsoleFrame().getUsername() + "?");
                    break;
                case 4:
                    println("How are you doing, " + ConsoleFrame.getConsoleFrame().getUsername() + "?");
                    break;
                case 5:
                    println("Greetings, " + ConsoleFrame.getConsoleFrame().getUsername() + ".");
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
                    + IPUtil.getIpdata().getAsn().getName());
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
        } else if (commandIs("luck")) {
            if (Math.random() * 100 <= 0.001) {
                println("YOU WON!!");
            } else {
                println("You are not lucky today.");
            }
        } else if (commandIs("&&")) {
            println("||");
        } else if (commandIs("||")) {
            println("&&");
        } else if (commandIs("&")) {
            println("|");
        } else if (commandIs("|")) {
            println("&");
        } else if (commandIs("espanol")) {
            println("Tu Hablo Espanol? Yo estudio Espanol.");
        } else if (commandIs("look")) {
            println("L()()K ---->> !FREE STUFF! <<---- L()()K");
        } else if (commandIs("Cyder?")) {
            println("Yes?");
        } else if (commandIs("home")) {
            println("There's no place like localhost/127.0.0.1");
        } else if (commandIs("love")) {
            println("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", but I don't understand human emotions or affections.");
        } else if (commandIs("loop")) {
            println("InputHandler.handle(\"loop\", true);");
        } else if (commandIs("story")) {
            println("It was a lazy day. Cyder was enjoying a deep sleep when suddenly " + ConsoleFrame.getConsoleFrame().getUsername() + " started talking to Cyder."
                    + " It was at this moment that Cyder knew its day had been ruined.");
        } else if (commandIs("i hate you")) {
            println("That's not very nice.");
        } else ret = false;

        if (ret)
            SessionHandler.log(SessionHandler.Tag.HANDLE_METHOD, "GENERAL PRINT COMMAND HANDLED");
        return ret;
    }

    private boolean printImageCheck() {
        boolean ret = true;

        if (commandIs("java")) {
            printlnImage("static/pictures/print/Duke.png");
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
            BletchyThread.bletchy("As old as my tongue and a little bit older than my teeth, wait...",
                    false, 50, true);
        } else ret = false;

        if (ret)
            SessionHandler.log(SessionHandler.Tag.HANDLE_METHOD, "PRINT IMAGE COMMAND HANDLED");
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
            SessionHandler.log(SessionHandler.Tag.HANDLE_METHOD, "CYDERFRAME MOVEMENT COMMAND HANDLED");
        return ret;
    }

    private boolean externalOpenerCheck() throws Exception {
        boolean ret = true;

        if (commandIs("YoutubeWordSearch")) {
            if (checkArgsLength(1)) {
                String input = getArg(0);
                String browse = "https://www.google.com/search?q=allinurl:REPLACE site:youtube.com";
                browse = browse.replace("REPLACE", input).replace(" ", "+");
                NetworkUtil.internetConnect(browse);
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
            Desktop.getDesktop().open(new File("c:/windows/system32/cmd.exe"));
        } else if (commandIs("desmos")) {
            NetworkUtil.internetConnect("https://www.desmos.com/calculator");
        } else if (commandIs("404")) {
            NetworkUtil.internetConnect("http://google.com/=");
        } else if (commandIs("coffee")) {
            NetworkUtil.internetConnect("https://www.google.com/search?q=coffe+shops+near+me");
        } else if (commandIs("quake3")) {
            NetworkUtil.internetConnect("https://www.youtube.com/watch?v=p8u_k2LIZyo&ab_channel=Nemean");
        } else if (commandIs("triangle")) {
            NetworkUtil.internetConnect("https://www.triangle-calculator.com/");
        } else if (commandIs("board")) {
            NetworkUtil.internetConnect("http://gameninja.com//games//fly-squirrel-fly.html");
        }else if (commandIs("arduino")) {
            NetworkUtil.internetConnect("https://www.arduino.cc/");
        } else if (commandIs("rasberrypi")) {
            NetworkUtil.internetConnect("https://www.raspberrypi.org/");
        }else if (commandIs("vexento")) {
            NetworkUtil.internetConnect("https://www.youtube.com/user/Vexento/videos");
        }else if (commandIs("papersplease")) {
            NetworkUtil.internetConnect("http://papersplea.se/");
        }else if (commandIs("donut")) {
            NetworkUtil.internetConnect("https://www.dunkindonuts.com/en/food-drinks/donuts/donuts");
        }else if (commandIs("bai")) {
            NetworkUtil.internetConnect("http://www.drinkbai.com");
        } else if (commandIs("occamrazor")) {
            NetworkUtil.internetConnect("http://en.wikipedia.org/wiki/Occam%27s_razor");
        } else if (commandIs("netsh")) {
            Desktop.getDesktop().open(new File("C:/Windows/system32/netsh.exe"));
        } else if (commandIs("paint")) {
            //todo soon our own custom painter
        } else if (commandIs("rickandmorty")) {
            println("Turned myself into a pickle morty! Boom! Big reveal; I'm a pickle!");
            NetworkUtil.internetConnect("https://www.youtube.com/watch?v=s_1lP4CBKOg");
        } else if (commandIs("about:blank")) {
            NetworkUtil.internetConnect("about:blank");
        } else ret = false;

        if (ret)
            SessionHandler.log(SessionHandler.Tag.HANDLE_METHOD, "EXTERNAL OPENER COMMAND HANDLED");
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
            SessionHandler.log(SessionHandler.Tag.HANDLE_METHOD, "AUDIO COMMAND HANDLED");
        return ret;
    }

    private boolean generalCommandCheck() throws Exception {
        boolean ret = true;

        if (commandIs("createuser")) {
            UserCreator.showGUI();
        } else if (commandIs("backgroundcolor")) {
            if (checkArgsLength(1)) {
                try {
                    Color color = Color.decode("#" + getArg(0));
                    int w = ConsoleFrame.getConsoleFrame().getCurrentBackgroundImageIcon().getIconWidth();
                    int h = ConsoleFrame.getConsoleFrame().getCurrentBackgroundImageIcon().getIconHeight();

                    if (UserUtil.extractUser().getFullscreen().equals("1")) {
                        w = SystemUtil.getScreenWidth();
                        h = SystemUtil.getScreenHeight();
                    }

                    BufferedImage saveImage = ImageUtil.bufferedImageFromColor(w, h, color);

                    String saveName = "Solid_" + getArg(0) + "Generated_Background.png";

                    File saveFile = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() +
                            "/Backgrounds/" + saveName);

                    ImageIO.write(saveImage, "png", saveFile);

                    ConsoleFrame.getConsoleFrame().revalidateBackgroundIndex();

                    println("Background generated, set, and saved as a separate background file.");
                } catch (Exception e) {
                    println("Background color command usage: backgroundcolor EC407A");
                    ExceptionHandler.silentHandle(e);
                }
            } else {
                println("Background color command usage: backgroundcolor EC407A");
            }
        } else if (commandIs("fixforeground")) {
            Color backgroundDom = ImageUtil.getDominantColor(ImageIO.read(
                    ConsoleFrame.getConsoleFrame().getCurrentBackgroundFile()));

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

            println("Foreground fixed");

            ConsoleFrame.getConsoleFrame().refreshBasedOnPrefs();
        } else if (commandIs("repaint")) {
            ConsoleFrame.getConsoleFrame().repaint();
            println("ConsoleFrame repainted");
        } else if (commandIs("disco")) {
            println("I hope you're not the only one at this party.");
            SystemUtil.disco(10);
        } else if (commandIs("javaproperties")) {
            StatUtil.javaProperties();
        } else if (commandIs("panic")) {
            if (UserUtil.getUserData("minimizeonclose").equals("1")) {
                ConsoleFrame.getConsoleFrame().minimizeAll();
            } else {
                GenesisShare.exit(25);
            }
        } else if (commandIs("opendrive")) {
            SystemUtil.openCD("D:\\");
        } else if (commandIs("closedrive")) {
            SystemUtil.closeCD("D:\\");
        } else if (commandIs("define")) {
            if (args.size() > 0) {
                println(StringUtil.define(argsToString()));
            } else {
                println("define usage: define YOUR_WORD/expression");
            }
        } else if (commandIs("wikisum")) {
            if (args.size() > 0) {
                println(StringUtil.wikiSummary(argsToString()));
            } else {
                println("wikisum usage: wikisum YOUR_WORD/expression");
            }
        } else if (commandIs("pixelate")) {
            if (ImageUtil.solidColor(ConsoleFrame.getConsoleFrame().getCurrentBackgroundFile())) {
                println("Silly " + ConsoleFrame.getConsoleFrame().getUsername() + "; your background " +
                        "is a solid color :P");
            } else {
                new Thread(() -> {
                    String input = new GetterUtil().getString("Pixel size","Enter any integer", "Pixelate");

                    try {
                        int pixelSize = Integer.parseInt(input);

                        if (pixelSize > 0) {
                            BufferedImage img = ImageUtil.pixelate(ImageIO.read(ConsoleFrame.getConsoleFrame().
                                    getCurrentBackgroundFile().getAbsoluteFile()), pixelSize);

                            String searchName = ConsoleFrame.getConsoleFrame().getCurrentBackgroundFile().getName()
                                    .replace(".png", "") + "_Pixelated_Pixel_Size_" + pixelSize + ".png";

                            File saveFile = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() +
                                    "/Backgrounds/" + searchName);

                            ImageIO.write(img, "png", saveFile);

                            LinkedList<File> backgrounds = ConsoleFrame.getConsoleFrame().getBackgrounds();

                            println("Background pixelated and saved as a separate background file.");
                            ConsoleFrame.getConsoleFrame().setFullscreen(false);

                            for (int i = 0; i < backgrounds.size(); i++) {
                                if (backgrounds.get(i).getName().equals(searchName)) {
                                    ConsoleFrame.getConsoleFrame().setBackgroundIndex(i);
                                    ConsoleFrame.getConsoleFrame().repaint();
                                }
                            }
                        }
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                },"Image Pixelator Getter thread").start();
            }
        } else if (commandIs("hide")) {
            ConsoleFrame.getConsoleFrame().minimize();
        } else if (commandIs("analyzecode")) {
            new Thread(() -> {
                File startDir = new File("src");

                int totalLines = StatUtil.totalLines(startDir);
                int codeLines = StatUtil.totalJavaLines(startDir);
                int blankLines = StatUtil.totalBlankLines(startDir);
                int commentLines = StatUtil.totalComments(startDir);
                int javaFiles = StatUtil.totalJavaFiles(startDir);

                println("Total lines: " + totalLines);
                println("Code lines: " + codeLines);
                println("Blank lines: " + blankLines);
                println("Comment lines: " + commentLines);
                println("Java Files: " + javaFiles);

                double ratio = ((double) codeLines / (double) commentLines);
                println("Code to comment ratio: " + new DecimalFormat("#0.00").format(ratio));
            }, "Code Analyzer").start();
        } else if (commandIs("f17")) {
            new Robot().keyPress(KeyEvent.VK_F17);
        }  else if (commandIs("debugstats")) {
            StatUtil.allStats();
        } else if (commandIs("debug")) {
            Debug.print("");
        } else if (commandIs("binary")) {
            if (checkArgsLength(1) && getArg(0).matches("[0-9]+")) {
                new Thread(() -> {
                    try {
                        println(getArg(0) + " converted to binary equals: "
                                + Integer.toBinaryString(Integer.parseInt(getArg(0))));
                    } catch (Exception ignored) {}
                },"Binary Converter Getter Thread").start();

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
                ConsoleFrame.getConsoleFrame().minimizeAll();
            } else {
                GenesisShare.exit(25);
            }
        } else if (commandIs("monitors")) {
            println(NetworkUtil.getMonitorStatsString());
        } else if (commandIs("networkdevices")) {
            println(NetworkUtil.getNetworkDevicesString());
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

                    if (StringUtil.getExtension(f).equalsIgnoreCase(".bin")) {
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

                        printlnPriority(sb.toString());
                    }
                }
            } else {
                println("Hexdump usage: hexdump -f /path/to/binary/file");
            }
        } else if (commandIs("barrelroll")) {
            ConsoleFrame.getConsoleFrame().barrelRoll();
        } else if (commandIs("askew")) {
            ConsoleFrame.getConsoleFrame().rotateBackground(5);
        } else if (commandIs("logout")) {
            ConsoleFrame.getConsoleFrame().logout();
        } else if (commandIs("throw")) {
            ExceptionHandler.handle(new Exception("Error thrown on " + TimeUtil.userTime()));
        } else if (commandIs("clearops")) {
            ConsoleFrame.getConsoleFrame().clearOperationList();
            SessionHandler.log(SessionHandler.Tag.ACTION, "User cleared command history");
            println("Command history reset");
        } else if (commandIs("stopscript")) {
            MasterYoutubeThread.killAll();
            println("YouTube scripts have been killed.");
        } else if (commandIs("longword")) {
            int count = 0;

            String[] words = command.split(" ");

            for (String word : words) {
                if (word.equalsIgnoreCase("long")) {
                    count++;
                }
            }

            for (int i = 0; i < count; i++) {
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
        } else if (commandIs("resetmouse")) {
            SystemUtil.resetMouse();
        } else if (commandIs("clc") ||
                commandIs("cls") ||
                commandIs("clear")) {
            clc();
        } else if (commandIs("mouse")) {
            if (checkArgsLength(2)) {
                SystemUtil.setMouseLoc(Integer.parseInt(getArg(0)), Integer.parseInt(getArg(1)));
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
            int total = StatUtil.totalTodos(new File("src"));

            if (total > 0) {
                println("Total todos: " + total);
                println("Todos:");
                println("----------------------------------------");
                println(StatUtil.getTodos(new File("src")));
            } else {
                println("No todos found, good job");
            }
        } else if (commandIs("wipelogs")) {
            if (SecurityUtil.nathanLenovo()) {
                File[] logDirs = new File("logs").listFiles();
                int count = 0;

                for (File logDir : logDirs) {
                    for (File log : logDir.listFiles()) {
                        if (StringUtil.getExtension(log).equals(".log")
                                && !log.equals(SessionHandler.getCurrentLog())) {
                            log.delete();
                            count++;
                        }
                    }
                }

                println("Deleted " + count + " log" + (count == 1 ? "" : "s"));
            } else {
                println("Sorry, " + UserUtil.getUserData("name") + ", " +
                        "but you do not have permission to perform that operation.");
            }
        } else if (commandIs("countlogs")) {
            if (SecurityUtil.nathanLenovo()) {
                File[] logDirs = new File("logs").listFiles();
                int count = 0;
                int days = 0;

                for (File logDir : logDirs) {
                    days++;
                    for (File log : logDir.listFiles()) {
                        if (StringUtil.getExtension(log).equals(".log")
                                && !logDir.equals(SessionHandler.getCurrentLog())) {
                            count++;
                        }
                    }
                }

                println("Number of log dirs: " + days);
                println("Number of logs: " + count);
            } else {
                println("Sorry, " + UserUtil.getUserData("name") + ", " +
                        "but you do not have permission to perform that operation.");
            }
        } else if (commandIs("opencurrentlog")) {
            if (SecurityUtil.nathanLenovo()) {
                IOUtil.openFileOutsideProgram(SessionHandler.getCurrentLog().getAbsolutePath());
            } else {
                println("Sorry, " + UserUtil.getUserData("name") + ", but you do not have permission " +
                        "to perform that operation.");
            }
        } else if (commandIs("openlastlog")) {
            if (SecurityUtil.nathanLenovo()) {
                File[] logs = SessionHandler.getCurrentLog().getParentFile().listFiles();

                if (logs.length == 1) {
                    println("No previous logs found");
                } else if (logs.length > 1) {
                    IOUtil.openFileOutsideProgram(logs[logs.length - 2].getAbsolutePath());
                }
            } else {
                println("Sorry, " + UserUtil.getUserData("name") + ", but you do not have permission " +
                        "to perform that operation.");
            }
        } else if (commandIs("play")) {
            boolean isURL = true;

            String input = argsToString();

            try {
                URL url = new URL(input);
                URLConnection conn = url.openConnection();
                conn.connect();
            } catch (Exception e) {
                isURL = false;
            }

            if (isURL) {
                new Thread(() -> {
                    try {
                        if (YoutubeUtil.isPlaylist(input)) {
                            String playlistID = input.replace("https://www.youtube.com/playlist?list=","");

                            println("Starting download of playlist: " +
                                    NetworkUtil.getURLTitle("https://www.youtube.com/playlist?list=" + playlistID));
                            Future<LinkedList<java.io.File>> downloadedFiles =
                                    YoutubeUtil.downloadPlaylist(playlistID,"dynamic/users/"
                                            + ConsoleFrame.getConsoleFrame().getUUID() + "/Music/");

                            //wait for all music to be downloaded
                            while (!downloadedFiles.isDone()) {
                                Thread.onSpinWait();
                            }

                            if (downloadedFiles.get() != null && !downloadedFiles.get().isEmpty()) {
                                println("Download complete; all songs added to mp3 queue");

                                //play the songs
                                for (File song : downloadedFiles.get()) {
                                    AudioPlayer.addToMp3Queue(song);
                                }
                            }
                        } else {
                            String videoURL = input;
                            Future<java.io.File> downloadedFile = YoutubeUtil.download(videoURL, "dynamic/users/"
                                    + ConsoleFrame.getConsoleFrame().getUUID() + "/Music/");

                            String videoTitle = NetworkUtil.getURLTitle(videoURL)
                                    .replaceAll("(?i) - YouTube", "").trim();
                            println("Starting download of: " + videoTitle);

                            while (!downloadedFile.isDone()) {
                                Thread.onSpinWait();
                            }

                            if (downloadedFile.get() != null && downloadedFile.get().exists()) {
                                println("Download complete and added to mp3 queue");

                                //play the song
                                AudioPlayer.addToMp3Queue(downloadedFile.get());
                            }
                        }
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }, "Youtube Audio Download Waiter").start();
            } else {
                new Thread(() -> {
                    try {
                        String userQuery = input;

                        println("Searching youtube for: " + userQuery);
                        String UUID = YoutubeUtil.getFirstUUID(userQuery);
                        String videoURL = "https://www.youtube.com/watch?v=" + UUID;
                        Future<java.io.File> downloadedFile = YoutubeUtil.download(videoURL, "dynamic/users/"
                                + ConsoleFrame.getConsoleFrame().getUUID() + "/Music/");

                        String videoTitle = NetworkUtil.getURLTitle(videoURL)
                                .replaceAll("(?i) - YouTube", "").trim();
                        println("Starting download of: " + videoTitle);

                        while (!downloadedFile.isDone()) {
                            Thread.onSpinWait();
                        }

                        if (downloadedFile.get() != null && downloadedFile.get().exists()) {
                            println("Download complete and added to mp3 queue");

                            AudioPlayer.addToMp3Queue(downloadedFile.get());
                        }
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }, "Youtube Audio Download Waiter").start();
            }
        }  else if (commandIs("pastebin")) {
            if (checkArgsLength(1)) {
                String urlString = "";
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
        } else if (commandIs("demomode")) {
            CyderFrame background = new CyderFrame(SystemUtil.getScreenWidth(), SystemUtil.getScreenHeight(),
                    CyderColors.navyComplementary);
            background.setTitle(StringUtil.capsFirst(IOUtil.getSystemData().getVersion()) + " Demo");

            JButton snapButton = new JButton("Center");
            snapButton.setForeground(CyderColors.vanila);
            snapButton.setFont(CyderFonts.defaultFontSmall);
            snapButton.addActionListener(e -> {
                background.setSize(SystemUtil.getScreenWidth(), SystemUtil.getScreenHeight());
                background.setLocationRelativeTo(null);
            });
            snapButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    snapButton.setForeground(CyderColors.regularRed);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    snapButton.setForeground(CyderColors.vanila);
                }
            });

            JButton pictureButton = new JButton("Shoot");
            pictureButton.setForeground(CyderColors.vanila);
            pictureButton.setFont(CyderFonts.defaultFontSmall);
            pictureButton.addActionListener(e -> {
                try {
                    Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                    BufferedImage bufferedImage = null;
                    bufferedImage = new Robot().createScreenCapture(rectangle);
                    File file = new File("c:/users/"
                            + SystemUtil.getWindowsUsername() + "/downloads/CyderCapture_" + TimeUtil.logSubDirTime() + ".png");
                    boolean status = ImageIO.write(bufferedImage, "png", file);
                    ConsoleFrame.getConsoleFrame().notify("Screen shot " +
                            (status ? "successfully" : "unsuccessfully") + " saved to your downloads folder");
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                }
            });
            pictureButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    pictureButton.setForeground(CyderColors.regularRed);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    pictureButton.setForeground(CyderColors.vanila);
                }
            });

            pictureButton.setContentAreaFilled(false);
            pictureButton.setBorderPainted(false);
            pictureButton.setFocusPainted(false);
            background.getTopDragLabel().addButton(pictureButton, 0);

            snapButton.setContentAreaFilled(false);
            snapButton.setBorderPainted(false);
            snapButton.setFocusPainted(false);
            background.getTopDragLabel().addButton(snapButton, 0);

            ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().setAlwaysOnTop(true);
            background.initializeResizing();
            background.setFrameResizing(false);
            background.setVisible(true);
            background.setLocationRelativeTo(null);
        } else if (commandIs("xxx")) {
            SystemUtil.setCurrentCyderIcon(SystemUtil.xxxIcon);
            ConsoleFrame.getConsoleFrame().getConsoleCyderFrame()
                    .setIconImage(new ImageIcon("static/pictures/print/x.png").getImage());
            IOUtil.playAudio("static/audio/x.mp3");
        } else if (commandIs("issues")) {
            new Thread(() -> {
                GitHubUtil.Issue[] issues = GitHubUtil.getIssues();
                println(issues.length + " issue" + (issues.length == 1 ? "" : "s") + " found:\n");
                println("----------------------------------------");

                for (GitHubUtil.Issue issue: issues) {
                    println("Issue #" + issue.number);
                    println(issue.title);
                    println(issue.body);
                    println("----------------------------------------");
                }
            }, "GitHub issue printer").start();
        } else if (commandIs("exitcodes")) {
            for (IOUtil.SystemData.ExitCondition exitCondition : IOUtil.getSystemData().getExitconditions()) {
                println(exitCondition.getCode() + ": " + exitCondition.getDescription());
            }
        } else if (commandIs("blackpanther")|| commandIs("chadwickboseman")) {
            new Thread(() -> {
                clc();

                IOUtil.playAudio("static/audio/Kendrick Lamar - All The Stars.mp3");
                Font oldFont = outputArea.getFont();
                outputArea.setFont(new Font("BEYNO", Font.BOLD, oldFont.getSize()));
                BletchyThread.bletchy("RIP CHADWICK BOSEMAN",false, 15, false);

                try {
                    //wait to reset font to original font
                    Thread.sleep(4000);
                } catch (Exception e) {
                    ExceptionHandler.silentHandle(e);
                }

                outputArea.setFont(oldFont);
            },"Chadwick Boseman Thread").start();
        } else if (commandIs("dst")) {
            new Thread(() -> {
                String location = IPUtil.getIpdata().getCity() + ", "
                        + IPUtil.getIpdata().getRegion() + ", "
                        +  IPUtil.getIpdata().getCountry_name();

                if (IPUtil.getIpdata().getTime_zone().isIs_dst()) {
                    println("Yes, DST is underway in " + location + ".");
                } else {
                    println("No, DST is not underway in " + location + ".");
                }
            }, "DST Checker").start();
        } else if (commandIs("test")) {
            Debug.launchTests();
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
            new Thread(() -> {
                println("Finding bad words:");
                StatUtil.findBadWords();
                println("Concluded");
            }, "Bad Word Code Searcher").start();
        } else if (commandIs("usb")) {
            PyExecutor.executeUSBq();
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
            ArrayList<MultiString> widgets = ReflectionUtil.findWidgets();
            println("Found " + widgets.size() + " widgets:");
            println("-------------------------------------");

            for (MultiString multiString : widgets) {
                if (multiString.getSize() == 2) {
                    println("Trigger: " + multiString.get(0) + "\nDescription: " + multiString.get(1));
                    println("-------------------------------------");
                } else throw new IllegalStateException("@Widget annotation found with param length not 2");
            }
        } else ret = false;

         if (ret)
             SessionHandler.log(SessionHandler.Tag.HANDLE_METHOD, "GENERAL COMMAND HANDLED");

         return ret;
    }

    //sub-handle methods in the order they appear above --------------------------

    /**
     * Checks of the provided command is a URL and if so, opens a connection to it.
     *
     * @param command the command to attempt to open as a URL
     * @return whether or not the command was indeed a valid URL
     */
    private boolean isURLCheck(String command) {
        boolean ret = false;

        try {
            URL url = new URL(command);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            ret = true;
            NetworkUtil.internetConnect(command);
        } catch (Exception ignored) {}

        //log before returning
        if (ret)
            SessionHandler.log(SessionHandler.Tag.HANDLE_METHOD, "CONSOLE URL FUNCTION HANDLED");
        return ret;
    }

    /**
     * Determines if the command was a simple evaluatable function such as floor() or pow().
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
     * @return whether or not the command was a simple math library call
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
            SessionHandler.log(SessionHandler.Tag.HANDLE_METHOD, "CONSOLE MATH FUNCTION HANDLED");
        return ret;
    }

    /**
     * Determines if the provided command was a mathematical expression and if so, evaluates it.
     *
     * @param command the command to attempt to evaluate as a mathematical expression
     * @return whether or not the command was a mathematical expression
     */
    private boolean evaluateExpression(String command) {
        boolean ret = false;

        try {
            println(new DoubleEvaluator().evaluate(StringUtil.firstCharToLowerCase(command.trim())));
            ret = true;
        } catch (Exception ignored) {}

        if (ret)
            SessionHandler.log(SessionHandler.Tag.HANDLE_METHOD, "CONSOLE MATH HANDLED");
        return ret;
    }

    /**
     * Checks the command for an intended preference toggle and if so, toggles the preference.
     * The user may include 1, true, 0, or false with the command to specify the value of the targeted preference.
     *
     * @param targetedPreference the preference to change
     * @return whether or not a preference was toggled/handled
     */
    private boolean preferenceCheck(String targetedPreference) {
        boolean ret = false;

        for (Preference pref : GenesisShare.getPrefs()) {
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

                ConsoleFrame.getConsoleFrame().refreshBasedOnPrefs();
                ret = true;
            }
        }

        if (ret)
            SessionHandler.log(SessionHandler.Tag.HANDLE_METHOD, "CONSOLE PREFERENCE TOGGLE HANDLED");
        return ret;
    }

    /**
     * Determines if the command intended to invoke a manual test from test/ManualTests.
     *
     * @param command the command to attempt to recognize as a manual test
     * @return whether or not the command was handled as a manual test call
     */
    private boolean manualTestCheck(String command) {
        boolean ret = false;

        command = command.toLowerCase();

        if (command.contains("test")) {
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
            SessionHandler.log(SessionHandler.Tag.HANDLE_METHOD, "CONSOLE MANUAL TEST REFLECTION FIRE HANDLED");
        return ret;
    }

    /**
     * Determines if the command intended to invoke a unit test.
     *
     * @param command the unit test to invoke if recognized from unit tests
     * @return whether or not the command was recognized as a unit test call
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
            SessionHandler.log(SessionHandler.Tag.HANDLE_METHOD, "CONSOLE UNIT TEST REFLECTION FIRE HANDLED");
        return ret;
    }

    /**
     * The final handle method for if all other handle methods failed.
     */
    private void unknownInput() {
        try {
            println("Unknown command");
            ConsoleFrame.getConsoleFrame().flashSuggestionButton();

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

                    SessionHandler.log(SessionHandler.Tag.ACTION, "Similar command to \""
                            + command + "\" found with tol of " + tol + ", command = \"" + parts[0] + "\"");

                    if (tol > CyderNums.SIMILAR_COMMAND_TOL) {
                        println("Most similar command: \"" + parts[0] + "\"");
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    //end handle methods --------------------------------

    /**
     * Returns the current user issued command.
     *
     * @return the current user issued command
     */
    public String getCommand() {
        return this.command;
    }

    /**
     * Returns whether or not the arguments array contains the expected number of arguments.
     *
     * @param expectedSize the expected size of the command arguments
     * @return whether or not the arguments array contains the expected number of arguments
     */
    public boolean checkArgsLength(int expectedSize) {
        return this.args.size() == expectedSize;
    }

    /**
     * Returns the command argument at the provided index.
     * Returns null if the index is out of bounds instead of throwing.
     *
     * @param index the index to retreive the command argument of
     * @return the command argument at the provided index
     */
    public String getArg(int index) {
        if (index + 1 <= args.size() && index >= 0) {
            return args.get(index);
        } else return null;
    }

    /**
     * Returns the arguments in String form separated by spaces.
     *
     * @return the arguments in String form separated by spaces
     */
    public String argsToString() {
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
    public String argsAndCommandToString() {
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
    public void printUnitTests() {
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
    public void printManualTests() {
        println("Manual tests:");
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
    public void handleSecond(String input) {
        try {
            String desc = getUserInputDesc();
            //tests on desc which should have been set from the first handle method
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Prints the suggestions as recommendations to the user for what to use Cyder for.
     */
    public void help() {
        println("Try typing: ");

        for (IOUtil.Suggestion suggestion : IOUtil.getSuggestions()) {
            println(CyderStrings.bulletPoint + "\t" + suggestion.getCommand()
                    + ": Result: " + suggestion.getResult());
        }
    }

    /**
     * Links a different JTextPane as this input handler's outputArea.
     *
     * @param outputArea the JTextPane to link
     */
    public void setOutputArea(JTextPane outputArea) {
        this.outputArea = outputArea;
    }

    /**
     * Standard getter for the currently linked JTextPane.
     *
     * @return the linked JTextPane
     */
    public JTextPane getOutputArea() {
        return this.outputArea;
    }

    /**
     * Getter for this instance's input mode. Used by ConsoleFrame to trigger a handleSecond call.
     *
     * @return the value of user input mode
     */
    public boolean getUserInputMode() {
        return this.userInputMode;
    }

    /**
     * Set the value of secondary input mode. Used by ConsoleFrame to trigger a handleSecond call.
     *
     * @param b the value of input mode
     */
    public void setUserInputMode(boolean b) {
        this.userInputMode = b;
    }

    /**
     * Returns the expected secondary input description.
     *
     * @return the input description
     */
    public String getUserInputDesc() {
        return this.userInputDesc;
    }

    /**
     * Sets this instance's secondary input description.
     *
     * @param s the description of the input we expect to receive next
     */
    public void setUserInputDesc(String s) {
        this.userInputDesc = s;
    }

    /**
     * Ends any custom threads such as youtube or bletchy
     * that may have been invoked via this input handler.
     */
    public void killThreads() {
        MasterYoutubeThread.killAll();
        BletchyThread.kill();
    }

    /**
     * Returns a String representation of this input handler object.
     *
     * @return a String representation of this input handler object
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }

    //printing queue methods and logic ----------------------------

    /**
     * Semaphore for adding objects to both consolePrintingList and consolePriorityPrintingList.
     */
    private Semaphore makePrintingThreadsafeAgain = new Semaphore(1);

    /**
     * the printing list of non-important outputs.
     * Directly adding to this list should not be performed. Instead use a print/println statement.
     * List is declared anonymously to allow for their add methods to be overridden to allow for
     * Semaphore usage implying thread safety when calling print statements.
     */
    private LinkedList<Object> consolePrintingList = new LinkedList<>() {
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
     * Directly adding to this list should not be performed. Instead use a print/println statement.
     * List is declared anonymously to allow for their add methods to be overridden to allow for
     * Semaphore usage implying thread safety when calling print statements.
     */
    private LinkedList<Object> consolePriorityPrintingList = new LinkedList<>() {
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
     * Boolean describing whether or not the console printing animation thread has been invoked and begun.
     */
    private boolean printingAnimationInvoked = false;

    /**
     * Begins the printing animation for the linked JTextPane. The typing animation is only
     * used if the user preference is enabled.
     */
    public void startConsolePrintingAnimation() {
        if (printingAnimationInvoked)
            return;

        printingAnimationInvoked = true;

        consolePrintingList.clear();
        consolePriorityPrintingList.clear();

        int charTimeout = 15;
        int lineTimeout = 200;

        new Thread(() -> {
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
                        SessionHandler.log(SessionHandler.Tag.CONSOLE_OUT,line);

                        if (redirection) {
                            redirectionWrite(line);
                        } else {
                            if (line instanceof String) {
                                StyledDocument document = (StyledDocument) outputArea.getDocument();
                                document.insertString(document.getLength(), (String) line, null);
                                outputArea.setCaretPosition(outputArea.getDocument().getLength());
                            } else if (line instanceof JComponent) {
                                String componentUUID = SecurityUtil.generateUUID();
                                Style cs = outputArea.getStyledDocument().addStyle(componentUUID, null);
                                StyleConstants.setComponent(cs, (Component) line);
                                outputArea.getStyledDocument().insertString(outputArea.getStyledDocument().getLength(), componentUUID, cs);
                            } else if (line instanceof ImageIcon) {
                                outputArea.insertIcon((ImageIcon) line);
                            } else {
                                println("[UNKNOWN OBJECT]: " + line);
                            }
                        }
                    }
                    //regular will perform a typing animation on strings if no method
                    // is currently running, such as random youtube or bletchy, that would cause
                    // concurrency issues
                    else if (consolePrintingList.size() > 0) {
                        Object line = consolePrintingList.removeFirst();
                        SessionHandler.log(SessionHandler.Tag.CONSOLE_OUT,line);

                        if (redirection) {
                            redirectionWrite(line);
                        } else {
                            if (line instanceof String) {
                                if (typingAnimationLocal) {
                                    if (finishPrinting) {
                                        StyledDocument document = (StyledDocument) outputArea.getDocument();
                                        document.insertString(document.getLength(), (String) line, null);
                                        outputArea.setCaretPosition(outputArea.getDocument().getLength());
                                    } else {
                                        GenesisShare.getPrintingSem().acquire();
                                        for (char c : ((String) line).toCharArray()) {
                                            innerConsolePrint(c);

                                            if (!finishPrinting)
                                                Thread.sleep(charTimeout);
                                        }
                                        GenesisShare.getPrintingSem().release();
                                    }
                                } else {
                                    StyledDocument document = (StyledDocument) outputArea.getDocument();
                                    document.insertString(document.getLength(), (String) line, null);
                                    outputArea.setCaretPosition(outputArea.getDocument().getLength());
                                }
                            } else if (line instanceof JComponent) {
                                String componentUUID = SecurityUtil.generateUUID();
                                Style cs = outputArea.getStyledDocument().addStyle(componentUUID, null);
                                StyleConstants.setComponent(cs, (Component) line);
                                outputArea.getStyledDocument().insertString(outputArea.getStyledDocument().getLength(), componentUUID, cs);
                            } else if (line instanceof ImageIcon) {
                                outputArea.insertIcon((ImageIcon) line);
                            } else {
                                println("[UNKNOWN OBJECT]: " + line);
                            }
                        }
                    } else if (consolePrintingList.isEmpty() && consolePriorityPrintingList.isEmpty()) {
                        //fix possible escape from last command
                        finishPrinting = false;
                    }

                    if (!finishPrinting && typingAnimationLocal)
                        Thread.sleep(lineTimeout);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Console Printing Animation").start();
    }

    /**
     * The increment we are currently on for inner char printing.
     * Used to determine when to play a typing animation sound.
     */
    private static int playInc = 0;

    /**
     * The frequency at which we should play a printing animation sound.
     */
    private static int playRate = 2;

    /**
     * Appends the provided char to the linked JTextPane and plays
     * a printing animation sound if playInc == playRate.
     *
     * @param c the character to append to the JTextPane
     */
    private void innerConsolePrint(char c) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(),
                    UserUtil.extractUser().getCapsmode().equals("1")
                            ? String.valueOf(c).toUpperCase() : String.valueOf(c), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());

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
    public void printlnImage(ImageIcon icon) {
        consolePrintingList.add(icon);
        consolePrintingList.add("\n");
    }

    /**
     * Prints the provided ImageIcon to the linked JTextPane.
     *
     * @param icon the icon to print to the linked JTextPane
     */
    public void printImage(ImageIcon icon) {
        consolePrintingList.add(icon);
    }

    /**
     * Prints the provided ImageIcon and a new line to the linked JTextPane.
     *
     * @param filename the filename of the image to print to the JTextPane
     */
    public void printlnImage(String filename) {
        consolePrintingList.add(new ImageIcon(filename));
        consolePrintingList.add("\n");
    }

    /**
     * Prints the provided ImageIcon and a new line to the linked JTextPane.
     *
     * @param filename the filename of the image to print to the JTextPane
     */
    public void printImage(String filename) {
        consolePrintingList.add(new ImageIcon(filename));
    }

    /**
     * Prints the provided component and a new line to the linked JTextPane.
     *
     * @param c the component to print to the JTextPane
     */
    public void printlnComponent(Component c) {
        consolePrintingList.add(c);
        consolePrintingList.add("\n");
    }

    /**
     * Prints the provided component to the linked JTextPane.
     *
     * @param c the component to print to the JTextPane
     */
    public void printComponent(Component c) {
        consolePrintingList.add(c);
    }

    /**
     * Prints the provided String to the linked JTextPane.
     *
     * @param usage the string to print to the JTextPane
     */
    public void print(String usage) {
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
    public void print(int usage) {
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
    public void print(double usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(Double.toString(usage));
        else
            consolePrintingList.add(Double.toString(usage));
    }

    /**
     * Prints the provided boolean to the linked JTextPane.
     *
     * @param usage the boolean to print to the JTextxPane
     */
    public void print(boolean usage) {
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
    public void print(float usage) {
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
    public void print(long usage) {
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
    public void print(char usage) {
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
    public void print(Object usage) {
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
    public void println(String usage) {
        print(usage + "\n");
    }

    /**
     * Prints the provided int and a newline to the linked JTextPane.
     *
     * @param usage the int to print to the JTextPane
     */
    public void println(int usage) {
        print(usage + "\n");
    }

    /**
     * Prints the provided double and a newline to the linked JTextPane.
     *
     * @param usage the double to print to the JTextPane
     */
    public void println(double usage) {
        print(usage + "\n");
    }

    /**
     * Prints the provided boolean and a newline to the linked JTextPane.
     *
     * @param usage the boolean to print to the JTextPane
     */
    public void println(boolean usage) {
        print(usage + "\n");
    }

    /**
     * Prints the provided float and a newline to the linked JTextPane.
     *
     * @param usage the float to print to the JTextPane
     */
    public void println(float usage) {
        print(usage + "\n");
    }

    /**
     * Prints the provided long and a newline to the linked JTextPane.
     *
     * @param usage the long to print to the JTextPane
     */
    public void println(long usage) {
        print(usage + "\n");
    }

    /**
     * Prints the provided char and a newline to the linked JTextPane.
     *
     * @param usage the char to print to the JTextPane
     */
    public void println(char usage) {
        print(usage + "\n");
    }

    /**
     * Prints the provided object and a newline to the linked JTextPane.
     *
     * @param usage the object to print to the JTextPane
     */
    public void println(Object usage) {
        print(usage + "\n");
    }

    /**
     * Prints the provided String lines to the linked JTextPane.
     * Note that new lines are automatically added in this so the passed
     * array may be strings that do not end with new lines.
     *
     * @param lines the lines to print to the JTextPane
     */
    public void printlns(String[] lines) {
        for (String line : lines)
            println(line);
    }

    /**
     * Priority printing method to print the provided ImageIcon and a new line to the JTextPane.
     *
     * @param icon the icon to print to the JTextPane
     */
    public void printlnImagePriority(ImageIcon icon) {
        consolePrintingList.add(icon);
        consolePrintingList.add("\n");
    }

    /**
     * Priority printing method to print the provided ImageIcon to the JTextPane.
     *
     * @param icon the icon to print to the JTextPane
     */
    public void printImagePriority(ImageIcon icon) {
        consolePriorityPrintingList.add(icon);
    }

    /**
     * Priority printing method to print the provided ImageIcon and a new line to the JTextPane.
     *
     * @param filename the filename of the icon to print to the JTextPane
     */
    public void printlnImagePriority(String filename) {
        consolePriorityPrintingList.add(new ImageIcon(filename));
        consolePriorityPrintingList.add("\n");
    }

    /**
     * Priority printing method to print the provided String to the JTextPane.
     *
     * @param filename the filename of the icon to print to the JTextPane
     */
    public void printImagePriority(String filename) {
        consolePriorityPrintingList.add(new ImageIcon(filename));
    }

    /**
     * Priority printing method to print the provided Component and a new line to the JTextPane.
     *
     * @param c the component to print to the JTextPane
     */
    public void printlnComponentPriority(Component c) {
        consolePriorityPrintingList.add(c);
        consolePriorityPrintingList.add("\n");
    }

    /**
     * Priority printing method to print the provided Component to the JTextPane.
     *
     * @param c the component to print to the JTextPane
     */
    public void printComponentPriority(Component c) {
        consolePriorityPrintingList.add(c);
    }

    /**
     * Priority printing method to print the provided String to the JTextPane.
     *
     * @param usage the string to print to the JTextPane
     */
    public void printPriority(String usage) {
        consolePriorityPrintingList.add(usage);
    }

    /**
     * Priority printing method to print the provided int to the JTextPane.
     *
     * @param usage the int to print to the JTextPane
     */
    public void printPriority(int usage) {
        consolePriorityPrintingList.add(Integer.toString(usage));
    }

    /**
     * Priority printing method to print the provided double to the JTextPane.
     *
     * @param usage the double to print to the JTextPane
     */
    public void printPriority(double usage) {
        consolePriorityPrintingList.add(Double.toString(usage));
    }

    /**
     * Priority printing method to print the provided boolean to the JTextPane.
     *
     * @param usage the boolean to print to the JTextPane
     */
    public void printPriority(boolean usage) {
        consolePriorityPrintingList.add(Boolean.toString(usage));
    }

    /**
     * Priority printing method to print the provided float to the JTextPane.
     *
     * @param usage the float to print to the JTextPane
     */
    public void printPriority(float usage) {
        consolePriorityPrintingList.add(Float.toString(usage));
    }

    /**
     * Priority printing method to print the provided long to the JTextPane.
     *
     * @param usage the long to print to the JTextPane
     */
    public void printPriority(long usage) {
        consolePriorityPrintingList.add(Long.toString(usage));
    }

    /**
     * Priority printing method to print the provided char to the JTextPane.
     *
     * @param usage the char to print to the JTextPane
     */
    public void printPriority(char usage) {
        consolePriorityPrintingList.add(String.valueOf(usage));
    }

    /**
     * Priority printing method to print the provided object to the JTextPane.
     *
     * @param usage the object to print to the JTextPane
     */
    public void printPriority(Object usage) {
        consolePriorityPrintingList.add(usage.toString());
    }

    /**
     * Priority printing method to print the provided String and a new line to the JTextPane.
     *
     * @param usage the string to print to the JTextPane
     */
    public void printlnPriority(String usage) {
        printPriority(usage + "\n");
    }

    /**
     * Priority printing method to print the provided int and a new line to the JTextPane.
     *
     * @param usage the provided int to print to the JTextPane
     */
    public void printlnPriority(int usage) {
        printPriority(usage + "\n");
    }

    /**
     * Priority printing method to print the provided double and a new line to the JTextPane.
     *
     * @param usage the double to print to the JTextPane
     */
    public void printlnPriority(double usage) {
        printPriority(usage + "\n");
    }

    /**
     * Priority printing method to print the provided boolean and a new line to the JTextPane.
     *
     * @param usage the boolean to print to the JTextPane
     */
    public void printlnPriority(boolean usage) {
        printPriority(usage + "\n");
    }

    /**
     * Priority printing method to print the provided float and a new line to the JTextPane.
     *
     * @param usage the float to print to the JTextPane
     */
    public void printlnPriority(float usage) {
        printPriority(usage + "\n");
    }

    /**
     * Priority printing method to print the provided long and a new line to the JTextPane.
     *
     * @param usage the long to print to the JTextPane
     */
    public void printlnPriority(long usage) {
        printPriority(usage + "\n");
    }

    /**
     * Priority printing method to print the provided char and a new line to the JTextPane.
     *
     * @param usage the char to print to the JTextPane
     */
    public void printlnPriority(char usage) {
        printPriority(usage + "\n");
    }

    /**
     * Priority printing method to print the provided object and a new line to the JTextPane.
     *
     * @param usage the object to print to the JTextPane
     */
    public void printlnPriority(Object usage) {
        printPriority(usage + "\n");
    }

    /**
     * Determines if the current command equals the provided text ignoring case.
     *
     * @param compare the string to check for case insensitive equalty to command
     * @return if the current command equals the provided text ignoring case
     */
    private boolean commandIs(String compare) {
        return command.equalsIgnoreCase(compare);
    }

    /**
     * Removes all components from the linked JTextPane.
     */
    private void clc() {
        outputArea.setText("");
    }

    /**
     * Removes the last "thing" addeed to the JTextPane whether it's a component,
     *  icon, or string of multi-llined text.
     *
     *  In more detail, this method figures out what it'll be removing and then determines how many calls
     *   are needed to {@link StringUtil#removeLastLine()}
     */
    public void removeLast() {
        try {
            boolean removeTwoLines = false;

            LinkedList<Element> elements = new LinkedList<>();
            ElementIterator iterator = new ElementIterator(outputArea.getStyledDocument());
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

            GenesisShare.getPrintingSem().acquire();

            if (removeTwoLines) {
                removeLastLine();
            }

            removeLastLine();

            GenesisShare.getPrintingSem().release();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns the last line of text on the linked JTextPane.
     * Text is easier to get and return as opposedto general components.
     *
     * @return the last line of text on the linked JTextPane
     */
    public String getLastTextLine() {
        String text = outputArea.getText();
        String[] lines = text.split("\n");
        return lines[lines.length - 1];
    }

    /**
     * Removes the last line added to the linked JTextPane. This could appear to remove nothing,
     *  but really be removing just a newline (line break) character.
     */
    public void removeLastLine() {
        try {
            LinkedList<Element> elements = new LinkedList<>();
            ElementIterator iterator = new ElementIterator(outputArea.getStyledDocument());
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
                    if (passedLeafs + 2 != leafs) {
                        passedLeafs++;
                        continue;
                    }

                    outputArea.getStyledDocument().remove(value.getStartOffset(),
                            value.getEndOffset() - value.getStartOffset());
                }
            }
        } catch (BadLocationException ignored) {}
        catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    //redirection logic ---------------------------------------

    /**
     * Semaphore used to ensure all things that need to
     * be written to the redirectionFile are written to it.
     * This also ensures that multiple redirections
     */
    Semaphore redirectionSem = new Semaphore(1);

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
    public void escapeThreads() {
        //exit user input mode if in it
        setUserInputMode(false);

        //kill threads
        killThreads();
        SystemUtil.killThreads();

        //stop music
        IOUtil.stopAudio();

        //cancel dancing threads
        ConsoleFrame.getConsoleFrame().stopDancing();

        //finish printing anything in printing queue
        finishPrinting = true;

        //inform user we escaped
        consolePriorityPrintingList.add("Escaped\n");
    }
}