package cyder.handler;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.enums.ScreenPosition;
import cyder.games.Hangman;
import cyder.games.TicTacToe;
import cyder.genesis.GenesisShare;
import cyder.genesis.GenesisShare.Preference;
import cyder.genesis.Login;
import cyder.genesis.UserCreator;
import cyder.genesis.UserEditor;
import cyder.threads.BletchyThread;
import cyder.threads.MasterYoutube;
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
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

public class InputHandler {
    private JTextPane outputArea;
    private MasterYoutube masterYoutube;
    private BletchyThread bletchyThread;
    private boolean userInputMode;
    private boolean finishPrinting;
    private String userInputDesc;
    private String operation;
    private String anagram;
    private UserEditor userEditor;

    private InputHandler() {} //no instantiation without a valid JTextPane to use

    public InputHandler(JTextPane outputArea) {
        this.outputArea = outputArea;
        masterYoutube = new MasterYoutube(outputArea);
        bletchyThread = new BletchyThread(outputArea);
    }

    //handle methods ----------------------------------------------

    public void handle(String op, boolean userTriggered) throws Exception{
        //check for null link
        if (outputArea == null)
            throw new IllegalArgumentException("Output area not set");

        //init String vars
        this.operation = op;
        String firstWord = StringUtil.firstWord(operation);

        //log CLIENT input
        if (userTriggered)
            SessionLogger.log(SessionLogger.Tag.CLIENT, operation);
        else
            SessionLogger.log(SessionLogger.Tag.CLIENT, "[SIMULATED INPUT] " + operation);

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
                && (!hasWord("print") && !hasWord("bletchy") && !hasWord("echo") &&
                !hasWord("youtube") && !hasWord("google") && !hasWord("wikipedia") &&
                !hasWord("synonym") && !hasWord("define"))) {
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
            printlnImage("sys/pictures/print/Duke.png");
        } else if ((hasWord("mississippi") && hasWord("state") && hasWord("university")) || eic("msu")) {
            printlnImage("sys/pictures/print/msu.png");
        } else if (eic("nathan")) {
            printlnImage("sys/pictures/print/me.png");
        } else if (hasWord("html") || hasWord("html5")) {
            printlnImage("sys/pictures/print/html5.png");
        } else if (hasWord("css")) {
            printlnImage("sys/pictures/print/css.png");
        }
        //threads -------------------------------------------------
        else if (hasWord("random") && hasWord("youtube")) {
            masterYoutube = new MasterYoutube(outputArea);
            masterYoutube.start(1);
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
        else if ((hasWord("youtube") && hasWord("thumbnail"))) {
            YoutubeUtil.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "YOUTUBE THUMBNAIL STEALER");
        } else if (hasWord("minecraft")) {
            Minecraft.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "MINECRAFT");
        } else if ((hasWord("edit") && hasWord("user")) || eic("prefs")) {
            UserEditor.showGUI(0);
            SessionLogger.log(SessionLogger.Tag.ACTION, "USER EDITOR");
        } else if (hasWord("hash") || hasWord("hashser")) {
            new Hasher().showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "SHA256 HASHER");
        }  else if (eic("search") || eic("dir") || (hasWord("file") && hasWord("search")) || eic("directory") || eic("ls")) {
            DirectorySearch.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "DIR SEARCH");
        } else if (hasWord("weather")) {
            new Weather().showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "WEATHER");
        } else if (eic("pin") || eic("login")) {
            Login.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "LOGIN WIDGET");
        } else if ((hasWord("create") || hasWord("new")) && hasWord("user")) {
            UserCreator.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "USER CREATOR");
        } else if ((hasWord("resize") && (hasWord("image")) ||
                (hasWord("picture") && hasWord("resize")))) {
            ImageResizer.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "IMAGE RESIZER");
        } else if (hasWord("temperature") || eic("temp")) {
            new TempConverter().showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "TEMPERATURE CONVERTER");
        } else if (has("click me")) {
            ClickMe.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "CLICK ME");
        } else if (has("Father") && hasWord("day") && has("2021")) {
            Cards.FathersDay2021();
            SessionLogger.log(SessionLogger.Tag.ACTION, "CARD");
        } else if (hasWord("christmas") && hasWord("card") && hasWord("2020")) {
            Cards.Christmas2020();
            SessionLogger.log(SessionLogger.Tag.ACTION, "CARD");
        } else if (hasWord("number") && hasWord("word")) {
            NumberUtil.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "NUMBER TO WORD");
        } else if (hasWord("hangman")) {
            Hangman.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "HANGMAN");
        } else if (hasWord("rgb") || hasWord("hex") || (hasWord("color") && hasWord("converter"))) {
            ColorConverter.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "COLOR CONVERTER");
        } else if (hasWord("pizza")) {
            Pizza.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "PIZZA");
        } else if ((hasWord("pixelate") || hasWord("distort")) &&
                (hasWord("image") || hasWord("picture"))) {
            ImagePixelator.showGUI(null);
            SessionLogger.log(SessionLogger.Tag.ACTION, "IMAGE PIXELATOR");
        } else if (hasWord("file") && hasWord("signature")) {
            FileSignatureChecker.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "FILE SIGNATURE");
        } else if ((has("tic") && has("tac") && has("toe")) || eic("TTT")) {
            TicTacToe.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "TIC TAC TOE");
        } else if (hasWord("note") || hasWord("notes")) {
            Notes.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "NOTE EDITOR");
        } else if ((hasWord("mp3") || hasWord("music")) && !hasWord("stop")) {
            AudioPlayer.showGUI(null);
            SessionLogger.log(SessionLogger.Tag.ACTION, "AUDIO PLAYER");
        } else if (hasWord("phone") || hasWord("dialer") || hasWord("call")) {
            Phone.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "PHONE");
        } else if ((hasWord("calculator") || hasWord("calc")) && !has("graphing")) {
            Calculator.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "CALCULATOR");
        } else if (eic("spotlight")) {
            Spotlight.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "SPOTLIGHT");
        } else if (hasWord("convex") && hasWord("hull")) {
            ConvexHull.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "CONVEX HULL");
        } else if (has("average") && (has("image") || hasWord("picture"))) {
            ImageAverager.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "IMAGE AVERAGER");
        } else if (hasWord("conway") || hasWord("conways")) {
            Conways.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "CONWAYS");
        } else if (hasWord("birthday") && hasWord("card") && hasWord("2021")) {
            Cards.Birthday2021();
        } else if (hasWord("pathfinder") || hasWord("path")) {
            PathFinder.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "PATHFINDER");
        } else if (hasWord("perlin")) {
            PerlinNoise.showGUI();
            SessionLogger.log(SessionLogger.Tag.ACTION, "PERLIN");
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
            for (Frame f : Frame.getFrames())
                if (f instanceof CyderFrame)
                    ((CyderFrame) (f)).dance();
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
            Desktop.getDesktop().open(new File("C:\\Windows\\system32\\netsh.exe"));
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
            IOUtil.playAudio("sys/audio/heyya.mp3",this);
        }  else if (eic("windows")) {
            IOUtil.playAudio("sys/audio/windows.mp3",this);
        }  else if (hasWord("light") && hasWord("saber")) {
            IOUtil.playAudio("sys/audio/Lightsaber.mp3",this);
        } else if (hasWord("xbox")) {
            IOUtil.playAudio("sys/audio/xbox.mp3",this);
        } else if (has("star") && has("trek")) {
            IOUtil.playAudio("sys/audio/StarTrek.mp3",this);
        } else if (has("toy") && has("story")) {
            IOUtil.playAudio("sys/audio/TheClaw.mp3",this);
        } else if (has("stop") && has("music")) {
            IOUtil.stopAudio();
        } else if (eic("logic")) {
            IOUtil.playAudio("sys/audio/commando.mp3",this);
        } else if (eic("1-800-273-8255") || eic("18002738255")) {
            IOUtil.playAudio("sys/audio/1800.mp3",this);
        }
        //console commands ----------------------------------------
        else if (hasWord("background") && hasWord("color")) {
            String colorInput = operation.replaceAll("(?i)background","")
                    .replaceAll("(?i)color","").replace("#","")
                    .replace(" ", "");
            try {
                Color color = Color.decode("#" + colorInput);
                BufferedImage saveImage = ImageUtil.bufferedImageFromColor(
                        ConsoleFrame.getConsoleFrame().getCurrentBackgroundImageIcon().getIconWidth(),
                        ConsoleFrame.getConsoleFrame().getCurrentBackgroundImageIcon().getIconHeight(),
                        color);

                String saveName = "Solid_" + colorInput + "Generated_Background.png";

                File saveFile = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() +
                        "/Backgrounds/" + saveName);

                ImageIO.write(saveImage, "png", saveFile);

                LinkedList<File> backgrounds = ConsoleFrame.getConsoleFrame().getBackgrounds();
                ConsoleFrame.getConsoleFrame().setFullscreen(false);

                for (int i = 0; i < backgrounds.size(); i++) {
                    if (backgrounds.get(i).getName().equals(saveName)) {
                        ConsoleFrame.getConsoleFrame().setBackgroundIndex(i);
                        ConsoleFrame.getConsoleFrame().repaint();
                        break;
                    }
                }

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
            println("How many iterations would you like to disco for? (Enter a positive integer)");
            setUserInputMode(true);
            ConsoleFrame.getConsoleFrame().getInputField().requestFocus();
            setUserInputDesc("disco");
        }   else if (hasWord("java") && hasWord("properties")) {
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
        } else if (hasWord("debug") && hasWord("menu")) {
            StatUtil.debugMenu();
        } else if (hasWord("pixelate") && hasWord("background")) {
            if (ImageUtil.solidColor(ConsoleFrame.getConsoleFrame().getCurrentBackgroundFile())) {
                println("Silly " + ConsoleFrame.getConsoleFrame().getUsername() + "; your background " +
                        "is a solid color :P");
            } else {
                println("Enter your pixel size (a positive integer)");
                setUserInputDesc("pixelatebackground");
                setUserInputMode(true);
                ConsoleFrame.getConsoleFrame().getInputField().requestFocus();
            }
        }  else if ((hasWord("delete") ||
                hasWord("remove")) &&
                (hasWord("user") ||
                        hasWord("account"))) {
            println("Are you sure you want to permanently delete this account? This action cannot be undone! (yes/no)");
            setUserInputMode(true);
            ConsoleFrame.getConsoleFrame().getInputField().requestFocus();
            setUserInputDesc("deleteuser");
        } else if (has("alphabet") && (hasWord("sort") ||
                hasWord("organize") || hasWord("arrange"))) {
            println("Enter your word to be alphabetically rearranged");
            ConsoleFrame.getConsoleFrame().getInputField().requestFocus();
            setUserInputMode(true);
            setUserInputDesc("alphabetize");
        } else if (eic("hide")) {
            ConsoleFrame.getConsoleFrame().minimize();
        } else if (hasWord("analyze") && hasWord("code")) {
            File startDir = new File("src");

            println("Lines of code: " + StatUtil.totalJavaLines(startDir));
            println("Number of java files: " + StatUtil.totalJavaFiles(startDir));
            println("Number of comments: " + StatUtil.totalComments(startDir));
            println("Blank lines: " + StatUtil.totalBlankLines(startDir));
            println("Total: " + (StatUtil.totalBlankLines(startDir) + StatUtil.totalJavaLines(startDir)));
        } else if (hasWord("press") && (hasWord("F17") || hasWord("f17"))) {
            new Robot().keyPress(KeyEvent.VK_F17);
        }  else if (hasWord("debug") && hasWord("windows")) {
            StatUtil.allStats();
        } else if (hasWord("binary") && !has("dump")) {
            println("Enter a decimal number to be converted to binary.");
            ConsoleFrame.getConsoleFrame().getInputField().requestFocus();
            setUserInputMode(true);
            setUserInputDesc("binary");
        } else if (hasWord("prime")) {
            println("Enter any positive integer and I will tell you if it's prime and what it's divisible by.");
            setUserInputDesc("prime");
            ConsoleFrame.getConsoleFrame().getInputField().requestFocus();
            setUserInputMode(true);
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
            GenesisShare.suspendFrameChecker();
            for (Frame f : Frame.getFrames()) {
                if (f instanceof CyderFrame)
                    f.dispose();
            }
            IOUtil.stopAudio();
            ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().removePostCloseActions();
            ConsoleFrame.getConsoleFrame().close();
            Login.showGUI();
        } else if (hasWord("throw") && hasWord("error")) {
            ConsoleFrame.getConsoleFrame().getInputField().setText("");
            throw new Exception("Error thrown on " + TimeUtil.userTime());
        } else if (hasWord("clear") && (hasWord("operation") ||
                hasWord("command")) && hasWord("list")) {
            ConsoleFrame.getConsoleFrame().clearOperationList();
            SessionLogger.log(SessionLogger.Tag.ACTION, "User cleared command history");
            println("Command history reset");
        } else if (hasWord("stop") && has("script")) {
            masterYoutube.killAllYoutube();
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
            println("This function will tell you if two"
                    + " words are anagrams of each other."
                    + "\nEnter your first word...");
            setUserInputDesc("anagram1");
            ConsoleFrame.getConsoleFrame().getInputField().requestFocus();
            setUserInputMode(true);
        } else if (hasWord("url")) {
            ConsoleFrame.getConsoleFrame().getInputField().requestFocus();
            setUserInputMode(true);
            setUserInputDesc("url");
            println("Enter your desired URL");
        } else if (hasWord("reset") && hasWord("mouse")) {
            SystemUtil.resetMouse();
        } else if (eic("logoff")) {
            println("Are you sure you want to log off your computer? This is not Cyder we are talking about (Enter yes/no)");
            setUserInputDesc("logoff");
            ConsoleFrame.getConsoleFrame().getInputField().requestFocus();
            setUserInputMode(true);
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
        } else if ((hasWord("wipe") || hasWord("clear")) && hasWord("logs")) {
            if (SecurityUtil.nathanLenovo()) {
                File[] logs = new File("logs").listFiles();
                int count = 0;

                for (File log : logs) {
                    if (StringUtil.getExtension(log).equals(".log")
                        && !log.equals(SessionLogger.getCurrentLog())) {
                        log.delete();
                        count++;
                    }
                }

                println("Deleted " + count + " log" + (count == 1 ? "" : "s"));
            } else {
                println("Sorry, " + UserUtil.getUserData("name") + ", " +
                        "but you do not have permission to perform that operation.");
            }
        } else if (hasWord("count") && hasWord("logs")) {
            if (SecurityUtil.nathanLenovo()) {
                File[] logs = new File("logs").listFiles();
                int count = 0;

                for (File log : logs) {
                    if (StringUtil.getExtension(log).equals(".log")
                            && !log.equals(SessionLogger.getCurrentLog())) {
                        count++;
                    }
                }

                println("Number of logs: " + count);
            } else {
                println("Sorry, " + UserUtil.getUserData("name") + ", " +
                        "but you do not have permission to perform that operation.");
            }
        } else if (hasWord("open") && hasWord("current") && hasWord("log")) {
            if (SecurityUtil.nathanLenovo()) {
                IOUtil.openFileOutsideProgram(SessionLogger.getCurrentLog().getAbsolutePath());
            } else {
                println("Sorry, " + UserUtil.getUserData("name") + ", but you do not have permission " +
                        "to perform that operation.");
            }
        } else if (hasWord("open") && hasWord("last") && hasWord("log")) {
            if (SecurityUtil.nathanLenovo()) {
                File[] logs = new File("logs").listFiles();

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

            String input = operation.replaceAll("(?i)play","").trim();

            try {
                URL url = new URL(input);
                URLConnection conn = url.openConnection();
                conn.connect();
            } catch (Exception e) {
                ErrorHandler.silentHandle(e);
                isURL = false;
            }

            if (isURL) {
                new Thread(() -> {
                    try {
                        String videoURL = input;
                        Future<java.io.File> downloadedFile = YoutubeUtil.download(videoURL, "users/"
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
                        Future<java.io.File> downloadedFile = YoutubeUtil.download(videoURL, "users/"
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
        } else if (hasWord("steal") && hasWord("windows") && hasWord("backgrounds")) {
            File saveDir = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Backgrounds");
            Spotlight.saveSpotlights(saveDir);
            ConsoleFrame.getConsoleFrame().resizeBackgrounds();
            ConsoleFrame.getConsoleFrame().getInputHandler()
                    .println("Spotlight images saved to your user's background/ directory");
        } else if (firstWord.equalsIgnoreCase("pastebin")) {
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
                            + SystemUtil.getWindowsUsername() + "/downloads/CyderCapture_" + TimeUtil.logFileTime() + ".png");
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
            SystemUtil.setCurrentCyderIcon(new ImageIcon("sys/pictures/print/x.png"));
            ConsoleFrame.getConsoleFrame().getConsoleCyderFrame()
                    .setIconImage(new ImageIcon("sys/pictures/print/x.png").getImage());
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
        }
        //final attempt at unknown input --------------------------
        else {
            if (handleMath(operation)) {

            } else if (evaluateExpression(operation)) {

            } else if (preferenceCheck(operation)) {

            } else {
                unknownInput();
            }
        }
    }

    public void handleSecond(String input) {
        if (outputArea == null)
            throw new IllegalArgumentException("Output area not set");

        try {
            String desc = getUserInputDesc();

            if (desc.equalsIgnoreCase("url") && !StringUtil.empytStr(input)) {
                NetworkUtil.internetConnect(new URI(input));
            } else if (desc.equalsIgnoreCase("prime") && input != null && !input.equals("")) {
                int num = Integer.parseInt(input);

                if (num <= 0) {
                    println("The inger " + num + " is not a prime number because it is negative.");
                } else if (num == 1) {
                    println("The inger 1 is not a prime number by the definition of a prime number.");
                } else if (num == 2) {
                    println("The integer 2 is indeed a prime number.");
                }

                ArrayList<Integer> Numbers = new ArrayList<>();

                for (int i = 3; i < Math.ceil(Math.sqrt(num)); i += 2) {
                    if (num % i == 0) {
                        Numbers.add(i);
                    }
                }

                if (Numbers.isEmpty()) {
                    println("The integer " + num + " is indeed a prime number.");
                } else {
                    println("The integer " + num + " is not a prime number because it is divisible by " + Numbers);
                }
            } else if (desc.equalsIgnoreCase("google") && input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ", "+");
                println("Attempting to connect...");
                NetworkUtil.internetConnect("https://www.google.com/search?q=" + input);
            } else if (desc.equalsIgnoreCase("youtube") && input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ", "+");
                println("Attempting to connect...");
                NetworkUtil.internetConnect("https://www.youtube.com/results?search_query=" + input);
            } else if (desc.equalsIgnoreCase("math") && input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ", "+");
                println("Attempting to connect...");
                NetworkUtil.internetConnect("https://www.wolframalpha.com/input/?i=" + input);
            } else if (desc.equalsIgnoreCase("binary")) {
                if (input.matches("[0-9]+") && !StringUtil.empytStr(input)) {
                    println(input + " converted to binary equals: " + Integer.toBinaryString(Integer.parseInt(input)));
                } else {
                    println("Your value must only contain numbers.");
                }
            } else if (desc.equalsIgnoreCase("disco") && input != null && !input.equals("")) {
                println("I hope you're not the only one at this party.");
                SystemUtil.disco(Integer.parseInt(input));
            } else if (desc.equalsIgnoreCase("youtube word search") && input != null && !input.equals("")) {
                String browse = "https://www.google.com/search?q=allinurl:REPLACE site:youtube.com";
                browse = browse.replace("REPLACE", input).replace(" ", "+");
                NetworkUtil.internetConnect(browse);
            } else if (desc.equalsIgnoreCase("anagram1")) {
                println("Enter your second word");
                anagram = input;
                ConsoleFrame.getConsoleFrame().getInputField().requestFocus();
                setUserInputMode(true);
                setUserInputDesc("anagram2");
            } else if (desc.equalsIgnoreCase("anagram2")) {
                if (anagram.length() != input.length()) {
                    println("These words are not anagrams of each other.");
                } else if (anagram.equalsIgnoreCase(input)) {
                    println("These words are in fact anagrams of each other.");
                } else {
                    char[] W1C = anagram.toLowerCase().toCharArray();
                    char[] W2C = input.toLowerCase().toCharArray();
                    Arrays.sort(W1C);
                    Arrays.sort(W2C);

                    if (Arrays.equals(W1C, W2C)) {
                        println("These words are in fact anagrams of each other.");
                    } else {
                        println("These words are not anagrams of each other.");
                    }
                }

                anagram = "";
            } else if (desc.equalsIgnoreCase("alphabetize")) {
                char[] Sorted = input.toCharArray();
                Arrays.sort(Sorted);
                println("\"" + input + "\" alphabetically organized is \"" + new String(Sorted) + "\".");
            } else if (desc.equalsIgnoreCase("suggestion")) {
                logSuggestion(input);
                ConsoleFrame.getConsoleFrame().notify("Suggestion logged; " +
                        "please remember to send your logs directory to Nathan so that" +
                        " he can make Cyder better for us all");
            } else if (desc.equalsIgnoreCase("addbackgrounds")) {
                if (StringUtil.isConfirmation(input)) {
                    UserEditor.showGUI(0);
                    NetworkUtil.internetConnect("https://images.google.com/");
                } else
                    println("Okay nevermind then");
            } else if (desc.equalsIgnoreCase("logoff")) {
                if (StringUtil.isConfirmation(input)) {
                    String shutdownCmd = "shutdown -l";
                    Runtime.getRuntime().exec(shutdownCmd);
                } else
                    println("Okay nevermind then");
            } else if (desc.equalsIgnoreCase("deleteuser")) {
                if (!StringUtil.isConfirmation(input)) {
                    println("User " + ConsoleFrame.getConsoleFrame().getUsername() + " was not removed.");
                    return;
                }

                ConsoleFrame.getConsoleFrame().close();
                SystemUtil.deleteFolder(new File("users/" + ConsoleFrame.getConsoleFrame().getUUID()));

                String dep = SecurityUtil.getDeprecatedUUID();

                File renamed = new File("users/" + dep);
                while (renamed.exists()) {
                    dep = SecurityUtil.getDeprecatedUUID();
                    renamed = new File("users/" + dep);
                }

                File old = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID());
                old.renameTo(renamed);

                Frame[] frames = Frame.getFrames();

                for (Frame f : frames)
                    f.dispose();

                GenesisShare.exit(-56);
            } else if (desc.equalsIgnoreCase("pixelatebackground")) {
                BufferedImage img = ImageUtil.pixelate(ImageIO.read(ConsoleFrame.getConsoleFrame().
                        getCurrentBackgroundFile().getAbsoluteFile()), Integer.parseInt(input));

                String searchName = ConsoleFrame.getConsoleFrame().getCurrentBackgroundFile().getName()
                        .replace(".png", "") + "_Pixelated_Pixel_Size_" + Integer.parseInt(input) + ".png";

                File saveFile = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() +
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

    public void logSuggestion(String suggestion) {
        SessionLogger.log(SessionLogger.Tag.SUGGESTION,  suggestion);
    }

    public void setOutputArea(JTextPane outputArea) {
        this.outputArea = outputArea;
    }

    public JTextPane getOutputArea() {
        return this.outputArea;
    }

    public MasterYoutube getMasterYoutube() {
        return masterYoutube;
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
        masterYoutube.killAllYoutube();
        bletchyThread.killBletchy();
    }

    @Override
    public String toString() {
        return "InputHandler object, hash=" + this.hashCode() +
                "\nLinked outputArea: " + this.outputArea + "";
    }

    //printing queue methods and logic ----------------------------

    //don't ever add to these lists, call the respective print functions and let them
    // handle adding them to the lists
    private LinkedList<Object> consolePrintingList = new LinkedList<>();
    private LinkedList<Object> consolePriorityPrintingList = new LinkedList<>();

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
                while (!ConsoleFrame.getConsoleFrame().isClosed()) {
                    //priority simply appends to the console
                    if (consolePriorityPrintingList.size() > 0) {
                        Object line = consolePriorityPrintingList.removeFirst();
                        SessionLogger.log(SessionLogger.Tag.CONSOLE_OUT,line);

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
                    // is currently running, such as RY or Bletchy, that would cause
                    // concurrency issues
                    else if (consolePrintingList.size() > 0){
                        Object line = consolePrintingList.removeFirst();
                        SessionLogger.log(SessionLogger.Tag.CONSOLE_OUT,line);

                        if (line instanceof String) {
                            if (UserUtil.getUserData("typinganimation").equals("1")) {
                                GenesisShare.getPrintingSem().acquire();
                                for (char c : ((String) line).toCharArray()) {
                                    innerConsolePrint(c);

                                    if (!finishPrinting)
                                        Thread.sleep(charTimeout);
                                }
                                GenesisShare.getPrintingSem().release();
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

                    if (!finishPrinting)
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
            document.insertString(document.getLength(), String.valueOf(c), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());

            if (playInc == playRate - 1) {
                if (!finishPrinting && UserUtil.extractUser().getTypinganimationsound().equals("1")) {
                    IOUtil.playSystemAudio("sys/audio/Typing.mp3");
                    playInc = 0;
                }
            } else {
                playInc++;
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //specifically print and println methods ----------------------
    
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
        if (MasterYoutube.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(usage);
        else
            consolePrintingList.add(usage);
    }
    
    public void print(int usage) {
        if (MasterYoutube.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(Integer.toString(usage));
        else
            consolePrintingList.add(Integer.toString(usage));
    }
    
    public void print(double usage) {
        if (MasterYoutube.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(Double.toString(usage));
        else
            consolePrintingList.add(Double.toString(usage));
    }

    public void print(boolean usage) {
        if (MasterYoutube.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(Boolean.toString(usage));
        else
            consolePrintingList.add(Boolean.toString(usage));
    }
    
    public void print(float usage) {
        if (MasterYoutube.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(Float.toString(usage));
        else
            consolePrintingList.add(Float.toString(usage));
    }

    public void print(long usage) {
        if (MasterYoutube.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(Long.toString(usage));
        else
            consolePrintingList.add(Long.toString(usage));
    }
    
    public void print(char usage) {
        if (MasterYoutube.isActive() || BletchyThread.isActive())
            consolePriorityPrintingList.add(String.valueOf(usage));
        else
            consolePrintingList.add(String.valueOf(usage));
    }
    
    public void print(Object usage) {
        if (MasterYoutube.isActive() || BletchyThread.isActive())
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
        masterYoutube.killAllYoutube();
        //kill bletchy threads
        bletchyThread.killBletchy();
        //kill system threads
        SystemUtil.killThreads();
        //stop music
        IOUtil.stopAllAudio();
        //cancel dancing threads
        for (Frame f : Frame.getFrames()) {
            if (f instanceof CyderFrame)
                ((CyderFrame) (f)).setControl_c_threads(true);
        }
        //finish printing anything in printing queue
        finishPrinting = true;
        //inform user we escaped
        consolePriorityPrintingList.add("Escaped\n");
    }
}