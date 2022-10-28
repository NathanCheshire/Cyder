package cyder.threads;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.math.NumberUtil;
import cyder.props.PropLoader;
import cyder.ui.pane.CyderOutputPane;
import cyder.utils.StringUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/**
 * A class used to perform bletchy animations on a specific JTextPane.
 */
public final class BletchyThread {
    /**
     * Suppress default constructor.
     */
    private BletchyThread() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The inner animator class.
     */
    private static BletchyAnimator bletchyAnimator;

    /**
     * Whether this animator is active.
     */
    private static boolean isActive;

    /**
     * Common printing methods Cyder uses for JTextPane access.
     */
    private static StringUtil stringUtil;

    /**
     * Semaphore for the linked JTextPane to block other print calls during an animation.
     */
    private static Semaphore printingSemaphore;

    /**
     * Printing utility thread that prints a decoding animation to the linked JTextPane, blocking any
     * other print calls while underway.
     *
     * @param outputArea the output pane belonging to a Console to print to
     */
    public static void initialize(CyderOutputPane outputArea) {
        Preconditions.checkNotNull(outputArea);

        stringUtil = new StringUtil(outputArea);
        printingSemaphore = outputArea.getSemaphore();
    }

    /**
     * Invoke the bletchy decode animation with the following parameters on the linked JTextPane.
     *
     * @param decodeString the final string to decode and display after
     *                     the bletchy animation has finished
     * @param useNumbers   a boolean depicting whether to use
     *                     numbers in the alphabetic characters for the animation
     * @param milliDelay   the millisecond delay in between animation frames
     * @param useUnicode   a boolean depicting whether to use
     *                     more than just latin letters and possibly numbers
     */
    public static void bletchy(String decodeString, boolean useNumbers, int milliDelay, boolean useUnicode) {
        Preconditions.checkNotNull(decodeString);
        Preconditions.checkArgument(!decodeString.isEmpty());
        Preconditions.checkArgument(milliDelay > 0);

        if (isActive() || MasterYoutubeThread.isActive()) {
            Console.INSTANCE.getConsoleCyderFrame().notify("Cannot start bletchy/youtube thread"
                    + " at the same time as another instance.");
        } else {
            kill();
            bletchyAnimator = new BletchyAnimator(getBletchyArray(decodeString, useNumbers, useUnicode), milliDelay);
            bletchyAnimator.start();
        }
    }

    /**
     * Inner class used to invoke the bletchy animation.
     */
    private static class BletchyAnimator {
        /**
         * The sequential strings to print between delays.
         */
        private final String[] prints;

        /**
         * The delay in ms between prints.
         */
        private final int milliDelay;

        /**
         * Constructs and a new BletchyAnimator thread.
         *
         * @param print      the string array to print and remove the last
         *                   line of until the final index is printed
         * @param milliDelay the delay in ms between prints
         */
        BletchyAnimator(String[] print, int milliDelay) {
            Preconditions.checkNotNull(print);

            this.prints = Preconditions.checkNotNull(print);
            this.milliDelay = milliDelay;
        }

        /**
         * Starts the bletchy animation this animator is setup to perform.
         */
        public void start() {
            String threadName = "Bletchy printing thread, finalString: "
                    + CyderStrings.quote + prints[prints.length - 1] + CyderStrings.quote;
            CyderThreadRunner.submit(() -> {
                try {
                    isActive = true;

                    printingSemaphore.acquire();

                    Arrays.stream(prints).forEach(print -> {
                        if (!isActive) {
                            return;
                        }

                        stringUtil.println(print);

                        ThreadUtil.sleep(milliDelay);

                        stringUtil.removeLastElement();
                        stringUtil.removeLastElement();

                        if (stringUtil.documentContainsMoreThanDefaultElements()) stringUtil.println("");
                    });

                    stringUtil.println(prints[prints.length - 1]);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                } finally {
                    printingSemaphore.release();
                    kill();
                }
            }, threadName);
        }

        /**
         * Kills this bletchy thread.
         */
        public void kill() {
            isActive = false;
        }

        /**
         * Returns whether this animator is active.
         *
         * @return whether this animator is active
         */
        public boolean isActive() {
            return isActive;
        }
    }

    /**
     * Returns whether this BletchyThread has an animation thread underway.
     *
     * @return whether this BletchyThread has an animation thread underway
     */
    public static boolean isActive() {
        return isActive;
    }

    /**
     * Kills any and all bletchy printing threads
     */
    public static void kill() {
        if (bletchyAnimator != null) {
            bletchyAnimator.kill();
        }
    }

    /**
     * Character list of all lowercase latin characters.
     */
    private static final ImmutableList<Character> LOWERCASE_ALPHABET
            = ImmutableList.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
            'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z');

    /**
     * Character list of all lowercase latin characters and the base 10 numbers.
     */
    private static final ImmutableList<Character> LOWERCASE_ALPHABET_AND_NUMBERS
            = ImmutableList.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9');

    /**
     * The Unicode index of the starting card suite character.
     */
    private static final int minCardSuiteIndex = 9824;

    /**
     * The Unicode index of the ending card suite character.
     */
    private static final int maxCardSuiteIndex = 9835;

    /**
     * Character list of all unicode card suit characters.
     */
    private static final ImmutableList<Character> CARD_SUITS;

    static {
        LinkedList<Character> ret = new LinkedList<>();

        for (int i = minCardSuiteIndex ; i <= maxCardSuiteIndex ; i++) {
            ret.add((char) i);
        }

        CARD_SUITS = ImmutableList.copyOf(ret);
    }

    /**
     * Character list of unicode chars used for bletchy animations.
     */
    private static final ImmutableList<Character> UNICODE_CHARS;

    /**
     * The starting index of the unicode chars to use for bletchy animations.
     */
    private static final int UNICODE_START_INDEX = 880;

    /**
     * The ending index of the unicode chars to use for bletchy animations.
     */
    private static final int UNICODE_END_INDEX = 1023;

    static {
        LinkedList<Character> ret = new LinkedList<>();

        for (int index = UNICODE_START_INDEX ; index <= UNICODE_END_INDEX ; index++) {
            ret.add((char) index);
        }

        UNICODE_CHARS = ImmutableList.copyOf(ret);
    }

    /**
     * The key to get the bletchy animation iterations per char from the props.
     */
    private static final String BLETCHY_ANIMATION_ITERATIONS_PER_CHAR = "bletchy_animation_iterations_per_char";

    /**
     * The number of bletchy animation iterations per decode character.
     */
    private static final int ITERATIONS_PER_CHAR = PropLoader.getInteger(BLETCHY_ANIMATION_ITERATIONS_PER_CHAR);

    /**
     * Returns an array of Strings abiding by the parameters for a bletchy thread to print.
     *
     * @param decodeString the string to decode
     * @param useNumbers   a boolean turning on number usage
     * @param useUnicode   a boolean turning on random unicode chars
     * @return the string array to be used by a bletchy thread
     */
    private static String[] getBletchyArray(String decodeString, boolean useNumbers, boolean useUnicode) {
        Preconditions.checkNotNull(decodeString);
        Preconditions.checkArgument(!decodeString.isEmpty());

        LinkedList<String> retList = new LinkedList<>();

        String decodeUsage = decodeString.toLowerCase().trim();
        int len = decodeUsage.length();

        LinkedList<Character> charsToUse = new LinkedList<>();

        if (useNumbers) {
            charsToUse.addAll(LOWERCASE_ALPHABET_AND_NUMBERS);
        } else {
            charsToUse.addAll(LOWERCASE_ALPHABET);
        }

        if (useUnicode) {
            charsToUse.addAll(CARD_SUITS);
            charsToUse.addAll(UNICODE_CHARS);
        }

        for (int i = 1 ; i < len ; i++) {
            for (int j = 0 ; j < ITERATIONS_PER_CHAR ; j++) {

                StringBuilder current = new StringBuilder();

                for (int k = 0 ; k <= len ; k++) {
                    current.append(charsToUse.get(NumberUtil.randInt(charsToUse.size() - 1)));
                }

                retList.add((decodeUsage.substring(0, i) + current.substring(i, len)).toUpperCase());
            }
        }

        retList.add(decodeUsage);

        return retList.toArray(new String[0]);
    }
}
