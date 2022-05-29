package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.utilities.NumberUtil;
import cyder.utilities.StringUtil;
import cyder.utilities.TimeUtil;
import cyder.utilities.UserUtil;

import java.security.SecureRandom;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;

/**
 * A handler for printing out general response strings.
 */
public class GeneralPrintHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private GeneralPrintHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Handle()
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("shakespeare")) {
            if (NumberUtil.randInt(1, 2) == 1) {
                getInputHandler().println("Glamis hath murdered sleep, "
                        + "and therefore Cawdor shall sleep no more, Macbeth shall sleep no more.");
            } else {
                getInputHandler().println("To be, or not to be, that is the question: Whether 'tis nobler in "
                        + "the mind to suffer the slings and arrows of "
                        + "outrageous fortune, or to take arms against a sea of troubles and by opposing end them.");
            }
        } else if (getInputHandler().commandIs("asdf")) {
            getInputHandler().println("Who is the spiciest meme lord?");
        } else if (getInputHandler().commandIs("thor")) {
            getInputHandler().println("Piss off, ghost.");
        } else if (getInputHandler().commandIs("alextrebek")) {
            getInputHandler().println("Do you mean who is alex trebek?");
        } else if (StringUtil.isPalindrome(getInputHandler()
                .getCommand().replace(" ", ""))
                && getInputHandler().getCommand().length() > 3) {
            getInputHandler().println("Nice palindrome.");
        } else if (getInputHandler().commandIs("coinflip")) {
            double randGauss = new SecureRandom().nextGaussian();
            if (randGauss <= 0.0001) {
                getInputHandler().println("You're not going to believe this, but it landed on its side.");
            } else if (randGauss <= 0.5) {
                getInputHandler().println("It's Heads!");
            } else {
                getInputHandler().println("It's Tails!");
            }
        } else if (getInputHandler().commandIs("hello")
                || getInputHandler().commandIs("hi")) {
            switch (NumberUtil.randInt(1, 7)) {
                case 1:
                    getInputHandler().println("Hello, " + UserUtil.getCyderUser().getName() + ".");
                    break;
                case 2:
                    if (TimeUtil.isEvening()) {
                        getInputHandler().println("Good evening, "
                                + UserUtil.getCyderUser().getName() + ". How can I help?");
                    } else if (TimeUtil.isMorning()) {
                        getInputHandler().println("Good morning, "
                                + UserUtil.getCyderUser().getName() + ". How can I help?");
                    } else {
                        getInputHandler().println("Good afternoon, "
                                + UserUtil.getCyderUser().getName() + ". How can I help?");
                    }
                    break;
                case 3:
                    getInputHandler().println("What's up, " + UserUtil.getCyderUser().getName() + "?");
                    break;
                case 4:
                    getInputHandler().println("How are you doing, " + UserUtil.getCyderUser().getName() + "?");
                    break;
                case 5:
                    getInputHandler().println("Greetings, " + UserUtil.getCyderUser().getName() + ".");
                    break;
                case 6:
                    getInputHandler().println("I'm here....");
                    break;
                case 7:
                    getInputHandler().println("Go ahead...");
                    break;
            }
        } else if (getInputHandler().commandIs("bye")) {
            getInputHandler().println("Just say you won't let go.");
        } else if (getInputHandler().commandIs("time")) {
            getInputHandler().println(TimeUtil.weatherTime());
        } else if (getInputHandler().commandIs("lol")) {
            getInputHandler().println("My memes are better.");
        } else if (getInputHandler().commandIs("thanks")) {
            getInputHandler().println("You're welcome.");
        } else if (getInputHandler().commandIs("name")) {
            getInputHandler().println("My name is Cyder. I am a tool built by"
                    + " Nathan Cheshire for programmers/advanced users.");
        } else if (getInputHandler().commandIs("k")) {
            getInputHandler().println("Fun Fact: the letter \"K\" comes from the Greek letter kappa, which was taken "
                    + "from the Semitic kap, the symbol for an open hand. It is this very hand which "
                    + "will be slapping you in the face for saying \"k\" to me.");
        } else if (getInputHandler().commandIs("no")) {
            getInputHandler().println("Yes");
        } else if (getInputHandler().commandIs("nope")) {
            getInputHandler().println("yep");
        } else if (getInputHandler().commandIs("yes")) {
            getInputHandler().println("no");
        } else if (getInputHandler().commandIs("yep")) {
            getInputHandler().println("nope");
        } else if (getInputHandler().commandIs("jarvis")) {
            getInputHandler().println("*scoffs in Java* primitive loser AI");
        } else if (getInputHandler().commandIs("thanksgiving")) {
            int year = Calendar.getInstance().get(Calendar.YEAR);
            LocalDate RealTG = LocalDate.of(year, 11, 1)
                    .with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY));
            getInputHandler().println("Thanksgiving this year is on the " + RealTG.getDayOfMonth() + " of November.");
        } else if (getInputHandler().commandIs("fibonacci")) {
            for (long i : NumberUtil.fib(0, 1, 100))
                getInputHandler().println(i);
        } else if (getInputHandler().commandIs("break;")) {
            getInputHandler().println("Thankfully my pure console based infinite while loop days are over. <3 Nathan");
        } else if (getInputHandler().commandIs("why")) {
            getInputHandler().println("Why not?");
        } else if (getInputHandler().commandIs("why not")) {
            getInputHandler().println("Why?");
        } else if (getInputHandler().commandIs("groovy")) {
            getInputHandler().println("Kotlin is the best JVM lang.... I mean, Java is obviously the best!");
        } else if (getInputHandler().commandIs("&&")) {
            getInputHandler().println("||");
        } else if (getInputHandler().commandIs("||")) {
            getInputHandler().println("&&");
        } else if (getInputHandler().commandIs("&")) {
            getInputHandler().println("|");
        } else if (getInputHandler().commandIs("|")) {
            getInputHandler().println("&");
        } else if (getInputHandler().commandIs("espanol")) {
            getInputHandler().println("Tu hablas Espanol? Yo estudio Espanol mas-o-menos. Hay tu mi amigo?");
        } else if (getInputHandler().commandIs("look")) {
            getInputHandler().println("L()()K ---->> !FREE STUFF! <<---- L()()K");
        } else if (getInputHandler().commandIs("cyder")) {
            getInputHandler().println("That's my name, don't wear it out pls");
        } else if (getInputHandler().commandIs("home")) {
            getInputHandler().println("There's no place like localhost/127.0.0.1");
        } else if (getInputHandler().commandIs("love")) {
            getInputHandler().println("Sorry, " + UserUtil.getCyderUser().getName() +
                    ", but I don't understand human emotions or affections.");
        } else if (getInputHandler().commandIs("loop")) {
            getInputHandler().println("InputHandler.handle(\"loop\", true);");
        } else if (getInputHandler().commandIs("story")) {
            getInputHandler().println("It was a lazy day. Cyder was enjoying a deep sleep when suddenly "
                    + UserUtil.getCyderUser().getName() + " started talking to Cyder."
                    + " It was at this moment that Cyder knew its day had been ruined.");
        } else if (getInputHandler().commandIs("i hate you")) {
            getInputHandler().println("That's not very nice.");
        } else {
            ret = false;
        }

        return ret;
    }
}
