package cyder.handlers.internal;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderStrings;
import cyder.cyderuser.UserCreator;
import cyder.cyderuser.UserEditor;
import cyder.enums.ScreenPosition;
import cyder.games.HangmanGame;
import cyder.games.TTTGame;
import cyder.genesis.GenesisShare;
import cyder.genesis.GenesisShare.Preference;
import cyder.handlers.external.AudioPlayer;
import cyder.handlers.external.DirectoryViewer;
import cyder.testing.DebugConsole;
import cyder.testing.ManualTests;
import cyder.testing.UnitTests;
import cyder.threads.BletchyThread;
import cyder.threads.MasterYoutubeThread;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderCaret;
import cyder.ui.CyderFrame;
import cyder.utilities.*;
import cyder.widgets.*;

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
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class InputHandler {
    private JTextPane outputArea;
    private MasterYoutubeThread masterYoutubeThread;
    private BletchyThread bletchyThread;
    private boolean userInputMode;
    private boolean finishPrinting;
    private String userInputDesc;
    private String operation;
    private UserEditor userEditor;

    private InputHandler() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    public InputHandler(JTextPane outputArea) {
        this.outputArea = outputArea;
        masterYoutubeThread = new MasterYoutubeThread(outputArea);
        bletchyThread = new BletchyThread(outputArea);
    }

    //handle methods ----------------------------------------------

    public void handle(String op, boolean userTriggered) throws Exception {
        //check for null link
        if (outputArea == null)
            throw new IllegalArgumentException("Output area not set");

        //init String vars
        this.operation = op;
        String firstWord = StringUtil.firstWord(operation);

        //log CLIENT input
        if (userTriggered)
            SessionHandler.log(SessionHandler.Tag.CLIENT, operation);
        else
            SessionHandler.log(SessionHandler.Tag.CLIENT, "[SIMULATED INPUT] " + operation);

        //pre-process checks --------------------------------------
        if (StringUtil.filterLanguage(operation) && UserUtil.getUserData("filterchat").equals("1")) {
            println("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", but that language is prohibited.");
        }
        //printing strings ----------------------------------------
        else if (hasWord("shakespeare")) {
            if (NumberUtil.randInt(1, 2) == 1) {
                println("Glamis hath murdered sleep, and therefore Cawdor shall sleep no more, Macbeth shall sleep no more.");
            } else {
                println("To be, or not to be, that is the question: Whether 'tis nobler in the mind to suffer the slings and arrows of "
                        + "outrageous fortune, or to take arms against a sea of troubles and by opposing end them.");
            }
        } else if (hasWord("asdf")) {
            println("Who is the spiciest meme lord?");
        } else if (hasWord("qwerty")) {
            println("I prefer Dvorak, but I also like Colemak, Maltron, and JCUKEN.");
        } else if (hasWord("thor")) {
            println("Piss off, ghost.");
        }  else if (hasWord("alex") && hasWord("trebek")) {
            println("Do you mean who is alex trebek?");
        } else if (StringUtil.isPalindrome(operation.replace(" ", "")) && operation.length() > 3) {
            println("Nice palindrome.");
        } else if ((hasWord("flip") && hasWord("coin")) ||
                (hasWord("heads") && hasWord("tails"))) {
            if (Math.random() <= 0.0001) {
                println("You're not going to beleive this, but it landed on its side.");
            } else if (Math.random() <= 0.5) {
                println("It's Heads!");
            } else {
                println("It's Tails!");
            }
        } else if ((eic("hello") || has("whats up") || hasWord("hi"))
                && (!hasWord("print") && !hasWord("bletchy") && !hasWord("echo")
                && !hasWord("wikipedia") && !hasWord("synonym") && !hasWord("define"))) {
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
        } else if (hasWord("bye") || (hasWord("james") && hasWord("arthur"))) {
            println("Just say you won't let go.");
        } else if (hasWord("time") && hasWord("what")) {
            println(TimeUtil.weatherTime());
        } else if (eic("die") || (hasWord("roll") && hasWord("die"))) {
            int Roll = ThreadLocalRandom.current().nextInt(1, 7);
            println("You rolled a " + Roll + ".");
        } else if (eic("lol")) {
            println("My memes are better.");
        } else if ((hasWord("thank") && hasWord("you")) || hasWord("thanks")) {
            println("You're welcome.");
        } else if (hasWord("you") && hasWord("cool")) {
            println("I know.");
        } else if (((hasWord("who") || hasWord("what")) && has("you"))) {
            println("My name is Cyder. I am a tool built by Nathan Cheshire for programmers/advanced users.");
        } else if (hasWord("helpful") && hasWord("you")) {
            println("I will always do my best to serve you.");
        } else if (eic("k")) {
            println("Fun Fact: the letter \"K\" comes from the Greek letter kappa, which was taken "
                    + "from the Semitic kap, the symbol for an open hand. It is this very hand which "
                    + "will be slapping you in the face for saying \"k\" to me.");
        } else if (eic("no")) {
            println("Yes");
        } else if (eic("nope")) {
            println("yep");
        } else if (eic("yes")) {
            println("no");
        } else if (eic("yep")) {
            println("nope");
        } else if (has("how can I help")) {
            println("That's my line :P");
        } else if (hasWord("siri") || hasWord("jarvis") || hasWord("alexa")) {
            println("*scoffs in Java* primitive loser AI");
        } else if (hasWord("when") && hasWord("thanksgiving")) {
            int year = Calendar.getInstance().get(Calendar.YEAR);
            LocalDate RealTG = LocalDate.of(year, 11, 1)
                    .with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY));
            println("Thanksgiving this year is on the " + RealTG.getDayOfMonth() + " of November.");
        } else if (hasWord("location") || (hasWord("where") && hasWord("am") && hasWord("i"))) {
            println("You are currently in " + IPUtil.getIpdata().getCity() + ", " +
                    IPUtil.getIpdata().getRegion() + " and your Internet Service Provider is "
                    + IPUtil.getIpdata().getAsn().getName());
        } else if (hasWord("fibonacci")) {
            for (long i : NumberUtil.fib(0, 1, 100))
                println(i);
        } else if ((hasWord("how") && hasWord("are") &&
                hasWord("you")) && !hasWord("age") && !hasWord("old")) {
            println("I am feeling like a programmed response. Thank you for asking.");
        } else if (hasWord("how") && hasWord("day")) {
            println("I was having fun until you started asking me questions.");
        } else if (eic("break;")) {
            println("Thankfully my pure console based infinite while loop days are over. <3 Nathan");
        } else if (hasWord("why")) {
            println("Why not?");
        } else if (hasWord("why not")) {
            println("Why?");
        } else if (hasWord("groovy")) {
            println("Kotlin is the best JVM lang.... I mean, Java is obviously the best!");
        } else if (hasWord("luck")) {
            if (Math.random() * 100 <= 0.001) {
                println("YOU WON!!");
            } else {
                println("You are not lucky today.");
            }
        } else if (has("are you sure") || has("are you certain")) {
            if (Math.random() <= 0.5) {
                println("No");
            } else {
                println("Yes");
            }
        } else if (eic("&&")) {
            println("||");
        } else if (eic("||")) {
            println("&&");
        } else if (eic("&")) {
            println("|");
        } else if (eic("|")) {
            println("&");
        } else if (hasWord("espanol")) {
            println("Tu Hablo Espanol? Yo estudio Espanol.");
        } else if (eic("look")) {
            println("L()()K ---->> !FREE STUFF! <<---- L()()K");
        } else if (eic("Cyder?")) {
            println("Yes?");
        } else if (hasWord("home")) {
            println("There's no place like localhost/127.0.0.1");
        } else if (hasWord("I") && hasWord("love")) {
            println("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", but I don't understand human emotions or affections.");
        } else if (eic("loop")) {
            println("InputHandler.handle(\"loop\", true);");
        } else if (hasWord("story") && hasWord("tell")) {
            println("It was a lazy day. Cyder was enjoying a deep sleep when suddenly " + ConsoleFrame.getConsoleFrame().getUsername() + " started talking to Cyder."
                    + " It was at this moment that Cyder knew its day had been ruined.");
        } else if (hasWord("there") && hasWord("no") && hasWord("internet")) {
            println("Sucks to be you.");
        } else if (eic("i hate you")) {
            println("That's not very nice.");
        }
        //printing imageicons -------------------------------------
        else if (eic("java")) {
            printlnImage("static/pictures/print/Duke.png");
        } else if ((hasWord("mississippi") && hasWord("state") && hasWord("university")) || eic("msu")) {
            printlnImage("static/pictures/print/msu.png");
        } else if (eic("nathan")) {
            printlnImage("static/pictures/print/me.png");
        } else if (hasWord("html") || hasWord("html5")) {
            printlnImage("static/pictures/print/html5.png");
        } else if (hasWord("css")) {
            printlnImage("static/pictures/print/css.png");
        }
        //threads -------------------------------------------------
        else if (hasWord("random") && hasWord("youtube")) {
            masterYoutubeThread = new MasterYoutubeThread(outputArea);
            masterYoutubeThread.start(1);
        } else if (hasWord("scrub")) {
            bletchyThread.bletchy("No you!", false, 50, true);
        } else if (hasWord("bletchy")) {
            bletchyThread.bletchy(operation, false, 50, true);
        } else if (hasWord("threads") && !hasWord("daemon")) {
            new StringUtil(outputArea).printThreads();
        } else if (hasWord("threads") && hasWord("daemon")) {
            new StringUtil(outputArea).printDaemonThreads();
        } else if (has("How old are you") || (hasWord("what") && hasWord("age"))) {
            bletchyThread.bletchy("As old as my tongue and a little bit older than my teeth, wait...",
                    false, 50, true);
        }
        //widgets -------------------------------------------------
        else if (hasWord("clock")) {
            ClockWidget.showGUI();
        } else if ((hasWord("youtube") && hasWord("thumbnail"))) {
            YoutubeUtil.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "YOUTUBE THUMBNAIL STEALER");
        } else if (hasWord("minecraft")) {
            MinecraftWidget.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "MINECRAFT");
        } else if ((hasWord("edit") && hasWord("user")) || eic("prefs")) {
            UserEditor.showGUI(0);
            SessionHandler.log(SessionHandler.Tag.ACTION, "USER EDITOR");
        } else if (hasWord("hash") || hasWord("hashser")) {
            new HashingWidget().showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "SHA256 HASHER");
        }  else if (eic("search") || eic("dir") || (hasWord("file") && hasWord("search")) || eic("directory") || eic("ls")) {
            DirectoryViewer.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "DIR SEARCH");
        } else if (hasWord("weather")) {
            new WeatherWidget().showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "WEATHER");
        } else if (eic("pin") || eic("login")) {
            LoginHandler.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "LOGIN WIDGET");
        } else if ((hasWord("create") || hasWord("new")) && hasWord("user")) {
            UserCreator.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "USER CREATOR");
        } else if ((hasWord("resize") && (hasWord("image")) ||
                (hasWord("picture") && hasWord("resize")))) {
            ImageResizerWidget.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "IMAGE RESIZER");
        } else if (hasWord("temperature") || eic("temp")) {
            new TemperatureWidget().showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "TEMPERATURE CONVERTER");
        } else if (has("click me")) {
            ClickWidget.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "CLICK ME");
        } else if (has("Father") && hasWord("day") && has("2021")) {
            CardWidget.FathersDay2021();
            SessionHandler.log(SessionHandler.Tag.ACTION, "CARD");
        } else if (hasWord("christmas") && hasWord("card") && hasWord("2020")) {
            CardWidget.Christmas2020();
            SessionHandler.log(SessionHandler.Tag.ACTION, "CARD");
        } else if (hasWord("christmas") && hasWord("card") && hasWord("2021")) {
            CardWidget.Christmas2021();
            SessionHandler.log(SessionHandler.Tag.ACTION, "CARD");
        }  else if (hasWord("number") && hasWord("word")) {
            NumberUtil.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "NUMBER TO WORD");
        } else if (hasWord("hangman")) {
            HangmanGame.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "HANGMAN");
        } else if (hasWord("rgb") || hasWord("hex") || (hasWord("color") && hasWord("converter"))) {
            ColorConverterWidget.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "COLOR CONVERTER");
        } else if (hasWord("pizza")) {
            PizzaWidget.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "PIZZA");
        } else if ((hasWord("pixelate") || hasWord("distort")) &&
                (hasWord("image") || hasWord("picture"))) {
            ImagePixelatorWidget.showGUI(null);
            SessionHandler.log(SessionHandler.Tag.ACTION, "IMAGE PIXELATOR");
        } else if (hasWord("file") && hasWord("signature")) {
            FileSignatureWidget.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "FILE SIGNATURE");
        } else if ((has("tic") && has("tac") && has("toe")) || eic("TTT")) {
            TTTGame.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "TIC TAC TOE");
        } else if (hasWord("note") || hasWord("notes")) {
            NotesWidget.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "NOTE EDITOR");
        } else if ((hasWord("mp3") || hasWord("music")) && !hasWord("stop")) {
            AudioPlayer.showGUI(null);
            SessionHandler.log(SessionHandler.Tag.ACTION, "AUDIO PLAYER");
        } else if (hasWord("phone") || hasWord("dialer") || hasWord("call")) {
            PhoneWidget.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "PHONE");
        } else if ((hasWord("calculator") || hasWord("calc")) && !has("graphing")) {
            CalculatorWidget.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "CALCULATOR");
        } else if (eic("spotlight") || (has("spotlight") && has("steal") && !has("wipe"))) {
            File saveDir = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Backgrounds");
            SpotlightUtil.saveSpotlights(saveDir);
            ConsoleFrame.getConsoleFrame().resizeBackgrounds();
            println("Spotlight images saved to your user's background/ directory");
        } else if (has("spotlight") && hasWord("wipe")) {
            SpotlightUtil.wipe();
        } else if (hasWord("convex") && hasWord("hull")) {
            ConvexHullWidget.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "CONVEX HULL");
        } else if (has("average") && (has("image") || hasWord("picture"))) {
            ImageAveragerWidget.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "IMAGE AVERAGER");
        } else if (hasWord("conway") || hasWord("conways")) {
            GameOfLifeWidget.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "CONWAYS");
        } else if (hasWord("birthday") && hasWord("card") && hasWord("2021")) {
            CardWidget.Birthday2021();
        } else if (hasWord("pathfinder") || hasWord("path")) {
            PathFinderWidget.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "PATHFINDER");
        } else if (hasWord("perlin")) {
            PerlinWidget.showGUI();
            SessionHandler.log(SessionHandler.Tag.ACTION, "PERLIN");
        }
        //ui and settings -----------------------------------------
        else if (eic("top left")) {
            ConsoleFrame.getConsoleFrame().setLocationOnScreen(ScreenPosition.TOP_LEFT);
        } else if (eic("top right")) {
            ConsoleFrame.getConsoleFrame().setLocationOnScreen(ScreenPosition.TOP_RIGHT);
        } else if (eic("bottom left")) {
            ConsoleFrame.getConsoleFrame().setLocationOnScreen(ScreenPosition.BOTTOM_LEFT);
        } else if (eic("bottom right")) {
            ConsoleFrame.getConsoleFrame().setLocationOnScreen(ScreenPosition.BOTTOM_RIGHT);
        } else if (eic("middle") || eic("center")) {
            ConsoleFrame.getConsoleFrame().setLocationOnScreen(ScreenPosition.CENTER);
        } else if (hasWord("frame") && has("title")) {
            Frame[] frames = Frame.getFrames();
            for (Frame f : frames)
                if (f instanceof CyderFrame) {
                    println(f.getTitle());
                } else {
                    println(f.getTitle());
                }
        } else if (hasWord("consolidate") && (hasWord("windows") || hasWord("frames"))) {
            if (hasWord("top") && hasWord("right")) {
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
            } else if (hasWord("bottom") && hasWord("right")) {
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
            } else if (hasWord("bottom") && hasWord("left")) {
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
        } else if (eic("dance")) {
            ConsoleFrame.getConsoleFrame().dance();
        }
        //program outsourcing ------------------------------------
        else if (hasWord("cyder") && has("dir")) {
            if (SecurityUtil.nathanLenovo()) {
                String CurrentDir = System.getProperty("user.dir");
                IOUtil.openFile(CurrentDir);
            } else {
                println("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() +
                        ", but you don't have permission to do that.");
            }
        } else if (eic("youtube word search")) {
            println("Enter the desired word you would like to find in a YouTube URL");
            setUserInputDesc("youtube word search");
            ConsoleFrame.getConsoleFrame().getInputField().requestFocus();
            setUserInputMode(true);
        } else if (firstWord.equalsIgnoreCase("echo")) {
            String[] sentences = operation.split(" ");
            for (int i = 1; i < sentences.length; i++) {
                print(sentences[i] + " ");
            }
            println("");
        } else if (eic("cmd") || (hasWord("command") && hasWord("prompt"))) {
            Desktop.getDesktop().open(new File("c:/windows/system32/cmd.exe"));
        } else if ((has("graphing") && has("calculator")) || has("desmos") || has("graphing")) {
            NetworkUtil.internetConnect("https://www.desmos.com/calculator");
        } else if (has("airHeads xtremes") || eic("candy")) {
            NetworkUtil.internetConnect("http://airheads.com/candy#xtremes");
        } else if (eic("404")) {
            NetworkUtil.internetConnect("http://google.com/=");
        } else if (hasWord("coffee")) {
            NetworkUtil.internetConnect("https://www.google.com/search?q=coffe+shops+near+me");
        } else if (hasWord("Quake") && (hasWord("three") || hasWord("3"))) {
            NetworkUtil.internetConnect("https://www.youtube.com/watch?v=p8u_k2LIZyo&ab_channel=Nemean");
        } else if (hasWord("triangle")) {
            NetworkUtil.internetConnect("https://www.triangle-calculator.com/");
        } else if (hasWord("board")) {
            NetworkUtil.internetConnect("http://gameninja.com//games//fly-squirrel-fly.html");
        }else if (hasWord("arduino")) {
            NetworkUtil.internetConnect("https://www.arduino.cc/");
        } else if (has("rasberry pi")) {
            NetworkUtil.internetConnect("https://www.raspberrypi.org/");
        }else if (eic("vexento")) {
            NetworkUtil.internetConnect("https://www.youtube.com/user/Vexento/videos");
        }else if (hasWord("papers") && hasWord("please")) {
            NetworkUtil.internetConnect("http://papersplea.se/");
        }else if (hasWord("donuts")) {
            NetworkUtil.internetConnect("https://www.dunkindonuts.com/en/food-drinks/donuts/donuts");
        }else if (hasWord("bai")) {
            NetworkUtil.internetConnect("http://www.drinkbai.com");
        } else if (has("occam") && hasWord("razor")) {
            NetworkUtil.internetConnect("http://en.wikipedia.org/wiki/Occam%27s_razor");
        } else if (eic("netsh")) {
            Desktop.getDesktop().open(new File("C:/Windows/system32/netsh.exe"));
        } else if (hasWord("paint")) {
            String param = "C:/Windows/system32/mspaint.exe";
            Runtime.getRuntime().exec(param);
        } else if (hasWord("rick") && hasWord("morty")) {
            println("Turned myself into a pickle morty! Boom! Big reveal; I'm a pickle!");
            NetworkUtil.internetConnect("https://www.youtube.com/watch?v=s_1lP4CBKOg");
        } else if (eic("about:blank")) {
            NetworkUtil.internetConnect("about:blank");
        }
        //playing audio -------------------------------------------
        else if (eic("hey")) {
            IOUtil.playAudio("static/audio/heyya.mp3");
        }  else if (eic("windows")) {
            IOUtil.playAudio("static/audio/windows.mp3");
        }  else if (hasWord("light") && hasWord("saber")) {
            IOUtil.playAudio("static/audio/Lightsaber.mp3");
        } else if (hasWord("xbox")) {
            IOUtil.playAudio("static/audio/xbox.mp3");
        } else if (has("star") && has("trek")) {
            IOUtil.playAudio("static/audio/StarTrek.mp3");
        } else if (has("toy") && has("story")) {
            IOUtil.playAudio("static/audio/TheClaw.mp3");
        } else if (has("stop") && has("music")) {
            IOUtil.stopAudio();
        } else if (eic("logic")) {
            IOUtil.playAudio("static/audio/commando.mp3");
        } else if (eic("1-800-273-8255") || eic("18002738255")) {
            IOUtil.playAudio("static/audio/1800.mp3");
        }
        //console commands ----------------------------------------
        else if (hasWord("background") && hasWord("color")) {
            String colorInput = operation.replaceAll("(?i)background","")
                    .replaceAll("(?i)color","").replace("#","")
                    .replace(" ", "");
            try {
                Color color = Color.decode("#" + colorInput);
                int w = ConsoleFrame.getConsoleFrame().getCurrentBackgroundImageIcon().getIconWidth();
                int h = ConsoleFrame.getConsoleFrame().getCurrentBackgroundImageIcon().getIconHeight();

                if (UserUtil.extractUser().getFullscreen().equals("1")) {
                    w = SystemUtil.getScreenWidth();
                    h = SystemUtil.getScreenHeight();
                }

                BufferedImage saveImage = ImageUtil.bufferedImageFromColor(w, h, color);

                String saveName = "Solid_" + colorInput + "Generated_Background.png";

                File saveFile = new File("dynamic/users/" + ConsoleFrame.getConsoleFrame().getUUID() +
                        "/Backgrounds/" + saveName);

                ImageIO.write(saveImage, "png", saveFile);

                ConsoleFrame.getConsoleFrame().revalidateBackgroundIndex();

                println("Background generated, set, and saved as a separate background file.");
            } catch (Exception e) {
                ErrorHandler.silentHandle(e);
                println("Background color command usage: background color #EC407A");
            }
        } else if (hasWord("fix") && hasWord("foreground")) {
            Color backgroundDom = ImageUtil.getDominantColor(ImageIO.read(
                    ConsoleFrame.getConsoleFrame().getCurrentBackgroundFile()));

            if ((backgroundDom.getRed() * 0.299 + backgroundDom.getGreen()
                    * 0.587 + backgroundDom.getBlue() * 0.114) > 186) {
                ConsoleFrame.getConsoleFrame().getOutputArea().setForeground(CyderColors.textBlack);
                ConsoleFrame.getConsoleFrame().getInputField().setForeground(CyderColors.textBlack);
                ConsoleFrame.getConsoleFrame().getInputField().setCaretColor(CyderColors.textBlack);
                ConsoleFrame.getConsoleFrame().getInputField().setCaret(new CyderCaret(CyderColors.textBlack));
                UserUtil.setUserData("Foreground",ColorUtil.rgbtohexString(CyderColors.textBlack));
            } else {
                ConsoleFrame.getConsoleFrame().getOutputArea().setForeground(CyderColors.textWhite);
                ConsoleFrame.getConsoleFrame().getInputField().setForeground(CyderColors.textWhite);
                ConsoleFrame.getConsoleFrame().getInputField().setCaretColor(CyderColors.textWhite);
                ConsoleFrame.getConsoleFrame().getInputField().setCaret(new CyderCaret(CyderColors.textWhite));
                UserUtil.setUserData("Foreground", ColorUtil.rgbtohexString(CyderColors.textWhite));
            }

            println("Foreground fixed");

            ConsoleFrame.getConsoleFrame().refreshBasedOnPrefs();
        } else if (eic("repaint")) {
            ConsoleFrame.getConsoleFrame().repaint();
            println("ConsoleFrame repainted");
        } else if (hasWord("disco")) {
            println("I hope you're not the only one at this party.");
            SystemUtil.disco(10);
        } else if (hasWord("java") && hasWord("properties")) {
            StatUtil.javaProperties();
        } else if (eic("panic")) {
            if (UserUtil.getUserData("minimizeonclose").equals("1")) {
                ConsoleFrame.getConsoleFrame().minimizeAll();
            } else {
                GenesisShare.exit(25);
            }
        } else if ((firstWord.equalsIgnoreCase("print") || firstWord.equalsIgnoreCase("println"))) {
            String[] sentences = operation.split(" ");

            for (int i = 1; i < sentences.length; i++) {
                print(sentences[i] + " ");
            }

            println("");
        } else if (hasWord("open cd")) {
            SystemUtil.openCD("D:\\");
        } else if (hasWord("close cd")) {
            SystemUtil.closeCD("D:\\");
        } else if (firstWord.equalsIgnoreCase("define")) {
            String defineWord = operation.replaceAll("(?i)define","").trim();
            println(StringUtil.define(defineWord));
        } else if (firstWord.equalsIgnoreCase("wikisum")) {
            String summaryWord = operation.replaceAll("(?i)wikisum","").trim();
            println(StringUtil.wikiSummary(summaryWord));
        } else if (hasWord("pixelate") && hasWord("background")) {
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
                        ErrorHandler.handle(e);
                    }
                },"Image Pixelator Getter thread").start();
            }
        } else if (eic("hide")) {
            ConsoleFrame.getConsoleFrame().minimize();
        } else if (hasWord("analyze") && hasWord("code")) {
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
        } else if (hasWord("press") && (hasWord("F17") || hasWord("f17"))) {
            new Robot().keyPress(KeyEvent.VK_F17);
        }  else if (hasWord("debug") && !eic("debug")) {
            StatUtil.allStats();
        } else if (eic("debug")) {
            DebugConsole.print("");
        } else if (hasWord("binary") && !has("dump")) {
            new Thread(() -> {
                String input = new GetterUtil().getString("Enter an Interger","Enter any iteger to be converted to binary", "Submit");

                try {
                    if (input.matches("[0-9]+") && !StringUtil.empytStr(input)) {
                        println(input + " converted to binary equals: " + Integer.toBinaryString(Integer.parseInt(input)));
                    } else {
                        println("Your value must only contain numbers.");
                    }
                } catch (Exception ignored) {}
            },"Binary Converter Getter Thread").start();
        } else if (hasWord("prime")) {
            try {
                String[] parts = operation.split(" ");

                if (parts.length == 2) {
                    int num = Integer.parseInt(parts[1]);

                    if (NumberUtil.isPrime(num)) {
                        println(num + " is a prime");
                    } else {
                        println(num + " is not a prime because it is divisible by: " + NumberUtil.primeFactors(num));
                    }
                } else {
                    println("Prime usage: prime number");
                }
            } catch (Exception e) {
                println("Prime usage: prime number");
                ErrorHandler.silentHandle(e);
            }
        } else if ((eic("quit") || eic("exit") || eic("leave") || eic("close")) &&
                (!has("music") && !has("dance") && !has("script"))) {
            if (UserUtil.getUserData("minimizeonclose").equals("1")) {
                ConsoleFrame.getConsoleFrame().minimizeAll();
            } else {
                GenesisShare.exit(25);
            }
        } else if (has("monitor")) {
            println(NetworkUtil.getMonitorStatsString());
        } else if (hasWord("network") && hasWord("devices")) {
            println(NetworkUtil.getNetworkDevicesString());
        } else if (hasWord("bindump")) {
            if (has("-f")) {
                String[] parts = operation.split("-f");

                if (parts.length != 2) {
                    println("Too much/too little args");
                } else {
                    File f = new File(parts[1].trim());

                    if (f.exists()) {
                        printlnPriority("0b" + IOUtil.getBinaryString(f));
                    } else {
                        println("File: " + parts[1].trim() + " does not exist.");
                    }
                }
            } else {
                println("Bindump usage: bindump -f /path/to/binary/file");
            }
        } else if (hasWord("hexdump")) {
            if (has("-f")) {
                String[] parts = operation.split("-f");

                if (parts.length != 2) {
                    println("Too much/too little args");
                } else {
                    File f = new File(parts[1].trim());

                    if (!f.exists())
                        throw new IllegalArgumentException("File does not exist");

                    if (StringUtil.getExtension(f).equalsIgnoreCase(".bin")) {
                        if (f.exists()) {
                            printlnPriority("0x" + IOUtil.getHexString(f).toUpperCase());
                        } else {
                            println("File: " + parts[1].trim() + " does not exist.");
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
        } else if (hasWord("barrel") && hasWord("roll")) {
            ConsoleFrame.getConsoleFrame().barrelRoll();
        } else if (eic("askew")) {
            ConsoleFrame.getConsoleFrame().rotateBackground(5);
        } else if (hasWord("logout")) {
           ConsoleFrame.getConsoleFrame().logout();
        } else if (hasWord("throw") && (hasWord("error") || hasWord("exception"))) {
            ConsoleFrame.getConsoleFrame().getInputField().setText("");
            throw new Exception("Error thrown on " + TimeUtil.userTime());
        } else if (hasWord("clear") && (hasWord("operation") ||
                hasWord("command")) && hasWord("list")) {
            ConsoleFrame.getConsoleFrame().clearOperationList();
            SessionHandler.log(SessionHandler.Tag.ACTION, "User cleared command history");
            println("Command history reset");
        } else if (hasWord("stop") && has("script")) {
            masterYoutubeThread.killAllYoutube();
            println("YouTube scripts have been killed.");
        } else if (hasWord("long") && hasWord("word")) {
            int count = 0;

            String[] words = operation.split(" ");

            for (String word : words) {
                if (word.equalsIgnoreCase("long")) {
                    count++;
                }
            }

            for (int i = 0; i < count; i++) {
                print("pneumonoultramicroscopicsilicovolcanoconiosis");
            }

            println("");
        } else if (eic("ip")) {
            println(InetAddress.getLocalHost().getHostAddress());
        }  else if (hasWord("computer") && hasWord("properties")) {
            println("This may take a second, since this feature counts your PC's free memory");
            StatUtil.computerProperties();
        } else if (hasWord("system") && hasWord("properties")) {
            StatUtil.systemProperties();
        } else if (hasWord("anagram")) {
            String[] parts = operation.split(" ");

            if (parts.length != 3) {
                println("Anagram usage: anagram word1 word2");
            } else if (parts[0].trim().length() == 0 || parts[1].trim().length() == 0 || parts[2].trim().length() == 0) {
                println("Anagram usage: anagram word1 word2");
            } else {
                if (StringUtil.areAnagrams(parts[1], parts[2])) {
                    println(parts[1] + " and " + parts[2] + " are anagrams of each other");
                } else {
                    println(parts[1] + " and " + parts[2] + " are not anagrams of each other");
                }
            }
        } else if (hasWord("reset") && hasWord("mouse")) {
            SystemUtil.resetMouse();
        } else if (eic("clc") || eic("cls") || eic("clear") || (hasWord("clear") && hasWord("screen"))) {
            clc();
        }  else if (hasWord("reset") && hasWord("clipboard")) {
            StringSelection selection = new StringSelection(null);
            java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            println("Clipboard has been reset.");
        } else if (eic("help")) {
            help();
        } else if (eic("controlc") && !outputArea.isFocusOwner()) {
            escapeThreads();
        } else if (hasWord("todo") || hasWord("todos")) {
            println("Total todos: " + StatUtil.totalTodos(new File("src")));
            println("Todos:");
            println("----------------------------------------");
            println(StatUtil.getTodos(new File("src")));
        } else if ((hasWord("wipe") || hasWord("clear")) && hasWord("logs")) {
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
        } else if (hasWord("count") && hasWord("logs")) {
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
        } else if (hasWord("open") && hasWord("current") && hasWord("log")) {
            if (SecurityUtil.nathanLenovo()) {
                IOUtil.openFileOutsideProgram(SessionHandler.getCurrentLog().getAbsolutePath());
            } else {
                println("Sorry, " + UserUtil.getUserData("name") + ", but you do not have permission " +
                        "to perform that operation.");
            }
        } else if (hasWord("open") && hasWord("last") && hasWord("log")) {
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
        } else if (firstWord.equalsIgnoreCase("play")) {
            boolean isURL = true;

            String input = operation.substring(5).trim();

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
                        ErrorHandler.handle(e);
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
                        ErrorHandler.handle(e);
                    }
                }, "Youtube Audio Download Waiter").start();
            }
        }  else if (firstWord.equalsIgnoreCase("pastebin")) {
            String[] parts = op.split(" ");

            if (parts.length != 2) {
                println("pastebin usage: pastebin [URL/UUID]\nExample: pastebin xa7sJvNm");
            } else {
                String urlString = "";

                if (parts[1].contains("pastebin.com")) {
                    urlString = parts[1];
                } else {
                    urlString = "https://pastebin.com/raw/" + parts[1];
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
                    ErrorHandler.silentHandle(e);
                    println("Improper pastebin url");
                }
            }
        } else if (eic("Demo mode")) {
            CyderFrame background = new CyderFrame(SystemUtil.getScreenWidth(), SystemUtil.getScreenHeight(),
                    CyderColors.consoleColor);
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
                    ErrorHandler.handle(ex);
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
        } else if (hasWord("xxx") || hasWord("tentacion") || has("rip x")) {
            SystemUtil.setCurrentCyderIcon(SystemUtil.xxxIcon);
            ConsoleFrame.getConsoleFrame().getConsoleCyderFrame()
                    .setIconImage(new ImageIcon("static/pictures/print/x.png").getImage());
        } else if (hasWord("issue") || hasWord("issues")) {
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
        } else if ((hasWord("code") || hasWord("codes")) && hasWord("exit")) {
            for (IOUtil.SystemData.ExitCondition exitCondition : IOUtil.getSystemData().getExitconditions()) {
               println(exitCondition.getCode() + ": " + exitCondition.getDescription());
            }
        } else if ((has("black") && has("panther")) || (hasWord("chadwick") && hasWord("boseman"))) {
            new Thread(() -> {
                clc();

                IOUtil.playAudio("static/audio/Kendrick Lamar - All The Stars.mp3");
                Font oldFont = outputArea.getFont();
                outputArea.setFont(new Font("BEYNO", Font.BOLD, oldFont.getSize()));

                new BletchyThread(outputArea).bletchy("RIP CHADWICK BOSEMAN",false, 15, false);

                try {
                    Thread.sleep(4000);
                } catch (Exception e) {
                    ErrorHandler.silentHandle(e);
                }

                outputArea.setFont(oldFont);
            },"Chadwick Boseman Thread").start();
        } else if (eic("dst") || has("daylight") && has("savings") && has("time")) {
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
        } else if (eic("test")) {
            DebugConsole.launchTests();
        } else if (eic("tests")) {
            println("Valid tests to call:\n");
            printUnitTests();
            printManualTests();
        } else if (has("network") && has("addresses")) {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

            for (NetworkInterface netint : Collections.list(nets)) {
                println("Display name: " + netint.getDisplayName());
                println("Name: " + netint.getName());

                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    println("InetAddress: " + inetAddress);
                }
            }
        } else if (hasWord("file") && hasWord("sizes")) {
            StatUtil.fileSizes();
        }
        //final attempt at unknown input --------------------------
        else {
            if (isURLCheck(operation)) {
                SessionHandler.log(SessionHandler.Tag.ACTION, "CONSOLE URL FUNCTION HANDLED");
            } else if (handleMath(operation)) {
                SessionHandler.log(SessionHandler.Tag.ACTION, "CONSOLE MATH FUNCTION HANDLED");
            } else if (evaluateExpression(operation)) {
                SessionHandler.log(SessionHandler.Tag.ACTION, "CONSOLE MATH HANDLED");
            } else if (preferenceCheck(operation)) {
                SessionHandler.log(SessionHandler.Tag.ACTION, "CONSOLE PREFERENCE TOGGLE HANDLED");
            } else if (manualTestCheck(operation)) {
                SessionHandler.log(SessionHandler.Tag.ACTION, "CONSOLE MANUAL TEST REFLECTION FIRE HANDLED");
            } else if (unitTestCheck(operation)) {
                SessionHandler.log(SessionHandler.Tag.ACTION, "CONSOLE UNIT TEST REFLECTION FIRE HANDLED");
            } else {
                unknownInput();
                //inform of valid tests in case they were trying to call a test
            }
        }
    }

    public void printUnitTests() {
        println("Unit Tests:");
        UnitTests ut = new UnitTests();

        for (Method m : ut.getClass().getMethods()) {
            if (m.getName().toLowerCase().contains("test"))
                println(m.getName());
        }
    }

    public void printManualTests() {
        println("Manual tests:");
        ManualTests mtw = new ManualTests();

        for (Method m : mtw.getClass().getMethods()) {
            if (m.getName().toLowerCase().contains("test"))
                println(m.getName());
        }
    }

    public boolean manualTestCheck(String operation) {
        operation = operation.toLowerCase();

        if (operation.contains("test")) {
            boolean ret = false;

            operation = operation.replace("test","").trim();
            ManualTests mtw = new ManualTests();

            for (Method m : mtw.getClass().getMethods()) {
                if (m.getName().toLowerCase().contains(operation.toLowerCase()) && m.getParameterTypes().length == 0) {
                    try {
                        m.invoke(mtw);
                        println("Invoking manual test: " + m.getName());
                        ret = true;
                    } catch (Exception e) {
                        ErrorHandler.handle(e);
                    }
                    break;
                }
            }

            return ret;
        } else return false;
    }

    public boolean unitTestCheck(String operation) {
        operation = operation.toLowerCase();

        if (operation.contains("test")) {
            boolean ret = false;

            operation = operation.replace("test","").trim();
            UnitTests tests = new UnitTests();

            for (Method m : tests.getClass().getMethods()) {
                if (m.getName().toLowerCase().contains(operation.toLowerCase()) && m.getParameterTypes().length == 0) {
                    try {
                        m.invoke(tests);
                        println("Invoking unit test: " + m.getName());
                        ret = true;
                    } catch (Exception e) {
                        ErrorHandler.handle(e);
                    }
                    break;
                }
            }

            return ret;
        } else return false;
    }

    public boolean isURLCheck(String operation) {
        boolean ret = false;

        try {
            URL url = new URL(operation);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            ret = true;
            NetworkUtil.internetConnect(operation);
        } catch (Exception ignored) {
            ret = false;
        }

        return ret;
    }

    public void handleSecond(String input) {
        try {
            String desc = getUserInputDesc();

            if (desc.equalsIgnoreCase("youtube word search") && input != null && !input.equals("")) {
                String browse = "https://www.google.com/search?q=allinurl:REPLACE site:youtube.com";
                browse = browse.replace("REPLACE", input).replace(" ", "+");
                NetworkUtil.internetConnect(browse);
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private boolean handleMath(String userInput) {
        int firstParen = userInput.indexOf("(");
        int comma = userInput.indexOf(",");
        int lastParen = userInput.indexOf(")");

        String mathop;
        double param1 = 0.0;
        double param2 = 0.0;

        try {
            if (firstParen != -1) {
                mathop = userInput.substring(0, firstParen);

                if (comma != -1) {
                    param1 = Double.parseDouble(userInput.substring(firstParen + 1, comma));

                    if (lastParen != -1) {
                        param2 = Double.parseDouble(userInput.substring(comma + 1, lastParen));
                    }
                } else if (lastParen != -1) {
                    param1 = Double.parseDouble(userInput.substring(firstParen + 1, lastParen));
                }

                if (mathop.equalsIgnoreCase("abs")) {
                    println(Math.abs(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("ceil")) {
                    println(Math.ceil(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("floor")) {
                    println(Math.floor(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("log")) {
                    println(Math.log(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("log10")) {
                    println(Math.log10(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("max")) {
                    println(Math.max(param1, param2));
                    return true;
                } else if (mathop.equalsIgnoreCase("min")) {
                    println(Math.min(param1, param2));
                    return true;
                } else if (mathop.equalsIgnoreCase("pow")) {
                    println(Math.pow(param1, param2));
                    return true;
                } else if (mathop.equalsIgnoreCase("round")) {
                    println(Math.round(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("sqrt")) {
                    println(Math.sqrt(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("convert2")) {
                    println(Integer.toBinaryString((int) (param1)));
                    return true;
                }
            }
        } catch (Exception e) {
            ErrorHandler.silentHandle(e);
        }

        return false;
    }

    private void unknownInput() {
        println("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", but I don't recognize that command." +
                " You can make a suggestion by clicking the \"Suggestion\" button.");
        ConsoleFrame.getConsoleFrame().flashSuggestionButton();
    }

    private boolean evaluateExpression(String userInput) {
        try {
            println(new DoubleEvaluator().evaluate(StringUtil.firstCharToLowerCase(userInput.trim())));
            return true;
        } catch (Exception ignored) {}

        return false;
    }

    //checks to see if a preference id was entered and if so, toggles it
    private boolean preferenceCheck(String op) {
        boolean ret = false;

        for (Preference pref : GenesisShare.getPrefs()) {
            if (op.equalsIgnoreCase(pref.getID()) && !pref.getDisplayName().equals("IGNORE")) {
                if (op.contains("1") || op.toLowerCase().contains("true")) {
                    UserUtil.setUserData(pref.getID(), "1");
                    println(pref.getDisplayName() + " set to true");
                } else if (op.contains("0") || op.toLowerCase().contains("false")) {
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

        return ret;
    }

    /**
     * Prints a suggestion as to what the user should do
     */
    public void help() {
        LinkedList<String> helps = new LinkedList<>();
        helps.add("Calculator");
        helps.add("Prefs");
        helps.add("Java");
        helps.add("Bletchy Bletchy Park");
        helps.add("Minecraft");
        helps.add("Pixelate background");
        helps.add("rgb");
        helps.add("Hangman");
        helps.add("Pizza");
        helps.add("Music");
        helps.add("Temperature");
        helps.add("Tic tac toe");
        helps.add("Top right");
        helps.add("Consolidate windows");
        helps.add("Hey");
        helps.add("Press F17");
        helps.add("Hexdump");
        helps.add("Random Youtube");
        helps.add("Dir");
        helps.add("Nathan");
        helps.add("Resize Image");

        println("Try typing: ");

        for (int i : NumberUtil.randInt(0,helps.size() - 1,10,false)) {
            println("•\t" + helps.get(i));
        }
    }

    public void setOutputArea(JTextPane outputArea) {
        this.outputArea = outputArea;
    }

    public JTextPane getOutputArea() {
        return this.outputArea;
    }

    public MasterYoutubeThread getMasterYoutube() {
        return masterYoutubeThread;
    }

    public BletchyThread getBletchyThread() {
        return bletchyThread;
    }

    /**
     * Getter for this instance's input mode
     * @return the value of user input mode
     */
    public boolean getUserInputMode() {
        return this.userInputMode;
    }

    /**
     * Set the value of secondary input mode
     * @param b the value of input mode
     */
    public void setUserInputMode(boolean b) {
        this.userInputMode = b;
    }

    /**
     * Returns the expected secondary input description
     * @return the input description
     */
    public String getUserInputDesc() {
        return this.userInputDesc;
    }

    /**
     * Sets this instance's secondary input description
     * @param s the description of the input we expect to receive next
     */
    public void setUserInputDesc(String s) {
        this.userInputDesc = s;
    }

    public void close() {
        masterYoutubeThread.killAllYoutube();
        bletchyThread.killBletchy();
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }

    //printing queue methods and logic ----------------------------

    //use this for adding/removing to/from both lists
    private Semaphore makePrintingThreadsafeAgain = new Semaphore(1);

    //don't ever add to these lists, call the respective print functions and let them
    // handle adding them to the lists

    //lists declared anonymously to allow for their add methods to be overridden
    // allowing for semaphore usage
    private LinkedList<Object> consolePrintingList = new LinkedList<>() {
        @Override
        public boolean add(Object e) {
            try {
                makePrintingThreadsafeAgain.acquire();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            boolean ret = super.add(e);
            makePrintingThreadsafeAgain.release();
            return ret;
        }
    };
    private LinkedList<Object> consolePriorityPrintingList = new LinkedList<>() {
        @Override
        public boolean add(Object e) {
            try {
                makePrintingThreadsafeAgain.acquire();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            boolean ret = super.add(e);
            makePrintingThreadsafeAgain.release();
            return ret;
        }
    };

    private boolean started = false;

    //console printing animation currently turned off do to concurrency issues such as
    // bletchy, YouTube thread, and drawing pictures and such, maybe we just throw everything no matter
    // what into a custom OutputQueue and from there determine how to store it and print it?
    public void startConsolePrintingAnimation() {
        if (started)
            return;

        started = true;

        consolePrintingList.clear();
        consolePriorityPrintingList.clear();

        int charTimeout = 20;
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
                    //regular will perform a typing animation on strings if no method
                    // is currently running, such as random youtube or bletchy, that would cause
                    // concurrency issues
                    else if (consolePrintingList.size() > 0) {
                        Object line = consolePrintingList.removeFirst();
                        SessionHandler.log(SessionHandler.Tag.CONSOLE_OUT,line);

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
                    } else if (consolePrintingList.isEmpty() && consolePriorityPrintingList.isEmpty()) {
                        //fix possible escape from last command
                        finishPrinting = false;
                    }

                    if (!finishPrinting && typingAnimationLocal)
                        Thread.sleep(lineTimeout);
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, "Console Printing Animation").start();
    }

    private int playInc = 0;
    private int playRate = 2;

    private void innerConsolePrint(char c) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(),
                    UserUtil.extractUser().getCapsmode().equals("1") ? String.valueOf(c).toUpperCase() : String.valueOf(c), null);
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
            ErrorHandler.silentHandle(e);
        }
    }
    
    public void printlnImage(ImageIcon icon) {
        consolePrintingList.add(icon);
        consolePrintingList.add("\n");
    }
    
    public void printImage(ImageIcon icon) {
        consolePrintingList.add(icon);
    }
    
    public void printlnImage(String filename) {
        consolePrintingList.add(new ImageIcon(filename));
        consolePrintingList.add("\n");
    }
    
    public void printImage(String filename) {
        consolePrintingList.add(new ImageIcon(filename));
    }

    public void printlnComponent(Component c) {
        consolePrintingList.add(c);
        consolePrintingList.add("\n");
}

    public void printComponent(Component c) {
        consolePrintingList.add(c);
    }
    
    public void print(String usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(usage);
        else
            consolePrintingList.add(usage);
    }
    
    public void print(int usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(Integer.toString(usage));
        else
            consolePrintingList.add(Integer.toString(usage));
    }
    
    public void print(double usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(Double.toString(usage));
        else
            consolePrintingList.add(Double.toString(usage));
    }

    public void print(boolean usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(Boolean.toString(usage));
        else
            consolePrintingList.add(Boolean.toString(usage));
    }
    
    public void print(float usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(Float.toString(usage));
        else
            consolePrintingList.add(Float.toString(usage));
    }

    public void print(long usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(Long.toString(usage));
        else
            consolePrintingList.add(Long.toString(usage));

    }
    
    public void print(char usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(String.valueOf(usage));
        else
            consolePrintingList.add(String.valueOf(usage));
    }
    
    public void print(Object usage) {
        if (MasterYoutubeThread.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(usage.toString());
        else
            consolePrintingList.add(usage.toString());
    }

    public void println(String usage) {
        print(usage + "\n");
    }

    public void println(int usage) {
        print(usage + "\n");
    }
    
    public void println(double usage) {
        print(usage + "\n");
    }

    public void println(boolean usage) {
        print(usage + "\n");
    }

    public void println(float usage) {
        print(usage + "\n");
    }
    
    public void println(long usage) {
        print(usage + "\n");
    }

    public void println(char usage) {
        print(usage + "\n");
    }
    
    public void println(Object usage) {
        print(usage + "\n");
    }

    //print lines method
    public void printlns(String[] lines) {
        for (String line : lines)
            println(line);
    }

    //repeat above methods but for priority queue should an object need quick printing

    public void printlnImagePriority(ImageIcon icon) {
        consolePrintingList.add(icon);
        consolePrintingList.add("\n");
    }

    public void printImagePriority(ImageIcon icon) {
        consolePriorityPrintingList.add(icon);
    }

    public void printlnImagePriority(String filename) {
        consolePriorityPrintingList.add(new ImageIcon(filename));
        consolePriorityPrintingList.add("\n");
    }

    public void printImagePriority(String filename) {
        consolePriorityPrintingList.add(new ImageIcon(filename));
    }

    public void printlnComponentPriority(Component c) {
        consolePriorityPrintingList.add(c);
        consolePriorityPrintingList.add("\n");
    }

    public void printComponentPriority(Component c) {
        consolePriorityPrintingList.add(c);
    }

    public void printPriority(String usage) {
        consolePriorityPrintingList.add(usage);
    }

    public void printPriority(int usage) {
        consolePriorityPrintingList.add(Integer.toString(usage));
    }

    public void printPriority(double usage) {
        consolePriorityPrintingList.add(Double.toString(usage));
    }

    public void printPriority(boolean usage) {
        consolePriorityPrintingList.add(Boolean.toString(usage));
    }

    public void printPriority(float usage) {
        consolePriorityPrintingList.add(Float.toString(usage));
      }

    public void printPriority(long usage) {
        consolePriorityPrintingList.add(Long.toString(usage));
    }

    public void printPriority(char usage) {
        consolePriorityPrintingList.add(String.valueOf(usage));
    }

    public void printPriority(Object usage) {
        consolePriorityPrintingList.add(usage.toString());
    }

    public void printlnPriority(String usage) {
        printPriority(usage + "\n");
    }

    public void printlnPriority(int usage) {
        printPriority(usage + "\n");
    }

    public void printlnPriority(double usage) {
        printPriority(usage + "\n");
    }

    public void printlnPriority(boolean usage) {
        printPriority(usage + "\n");
    }

    public void printlnPriority(float usage) {
        printPriority(usage + "\n");
    }

    public void printlnPriority(long usage) {
        printPriority(usage + "\n");
    }

    public void printlnPriority(char usage) {
        printPriority(usage + "\n");
    }

    public void printlnPriority(Object usage) {
        printPriority(usage + "\n");
    }
    
    //string comparison methods -----------------------------------
    
    private boolean eic(String eic) {
        return operation.equalsIgnoreCase(eic);
    }
    
    private boolean has(String compare) {
        return operation.toLowerCase().contains(compare.toLowerCase());
    }

    private boolean hasWord(String compare) {
        String[] words = operation.trim().split(" ");

        for (String word : words) {
            if (word.trim().equalsIgnoreCase(compare))
                return true;
        }

        return false;
    }

    //direct JTextPane manipulation methods -----------------------

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
            ErrorHandler.handle(e);
        }
    }

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
            ErrorHandler.handle(e);
        }
    }

    //control flow handlers ---------------------------------------

    private void escapeThreads() {
        //exit user input mode if in it
        setUserInputMode(false);
        //kill YouTube threads
        masterYoutubeThread.killAllYoutube();
        //kill bletchy threads
        bletchyThread.killBletchy();
        //kill system threads
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