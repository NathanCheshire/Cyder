package cyder.threads;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.CyderOutputPane;
import cyder.utilities.NumberUtil;
import cyder.utilities.StringUtil;

import javax.swing.*;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/**
 * A class used to perform bletchy animations on a specific JTextPane.
 */
public class BletchyThread {
    /**
     * Suppress default constructor..
     */
    private BletchyThread() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Inner animator class.
     */
    private static BletchyAnimator bletchyAnimator;

    /**
     * Whether bletchyAnimator is active.
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
     * @param outputArea the JTextPane belonging to a ConsoleFrame to print to
     * @param semaphore  the semaphore to use to block other text being added to the linked JTextPane
     */
    public static void initialize(JTextPane outputArea, Semaphore semaphore) {
        Preconditions.checkNotNull(outputArea);
        Preconditions.checkNotNull(semaphore);

        stringUtil = new StringUtil(new CyderOutputPane(outputArea));
        printingSemaphore = semaphore;
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
            ConsoleFrame.INSTANCE.getConsoleCyderFrame().notify(
                    "Cannot start bletchy/youtube thread" +
                            " at the same time as another instance.");
            return;
        }

        bletchyAnimator = new BletchyAnimator(getBletchyArray(decodeString, useNumbers, useUnicode), milliDelay);
    }

    /**
     * Inner class used to invoke the bletchy animation.
     */
    private static class BletchyAnimator {
        /**
         * Constructs and starts a new BletchyAnimator thread.
         *
         * @param print      the string array to print and remove the last
         *                   line of until the final index is printed
         * @param milliDelay the delay in ms between prints
         */
        BletchyAnimator(String[] print, int milliDelay) {
            CyderThreadRunner.submit(() -> {
                try {
                    isActive = true;

                    printingSemaphore.acquire();

                    for (int i = 1 ; i < print.length ; i++) {
                        if (!isActive) {
                            printingSemaphore.release();
                            return;
                        }

                        stringUtil.println(print[i]);
                        Thread.sleep(milliDelay);
                        stringUtil.removeLastLine();
                    }

                    // print final string
                    stringUtil.println(print[print.length - 1]);
                    printingSemaphore.release();
                    kill();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                    kill();
                }
            }, "Bletchy printing thread, finalString = " + print[print.length - 1]);
        }

        /**
         * Kills this bletchy thread.
         */
        public void kill() {
            isActive = false;
        }
    }

    /**
     * Returns whether this BletchyThread has a BletchyAnimation thread underway.
     *
     * @return whether this BletchyThread has a BletchyAnimation thread underway
     */
    public static boolean isActive() {
        return isActive;
    }

    /**
     * Kills any and all bletchy printing threads
     */
    public static void kill() {
        if (bletchyAnimator != null)
            bletchyAnimator.kill();
    }

    /**
     * Character array of all lowercase latin characters.
     */
    private static Character[] lowercaseAlphabet =
            {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
                    'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    /**
     * Character array of all lowercase latin characters and the base 10 numbers.
     */
    private static final Character[] lowercaseAlphabetAndBase10 =
            {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
                    'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

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

        if (useNumbers) {
            lowercaseAlphabet = lowercaseAlphabetAndBase10;
        }

        if (useUnicode) {
            LinkedList<Character> chars = new LinkedList<>();

            // card suites
            for (int index = 9824 ; index <= 9835 ; index++) {
                chars.add((char) index);
            }

            //cool looking unicode chars
            for (int index = 880 ; index <= 1023 ; index++) {
                chars.add((char) index);
            }

            lowercaseAlphabet = chars.toArray(new Character[0]);
        }

        int iterationsPerChar = 7;

        for (int i = 1 ; i < len ; i++) {
            for (int j = 0 ; j < iterationsPerChar ; j++) {

                StringBuilder current = new StringBuilder();

                for (int k = 0 ; k <= len ; k++) {
                    current.append(lowercaseAlphabet[NumberUtil.randInt(0,
                            lowercaseAlphabet.length - 1)]);
                }

                retList.add((decodeUsage.substring(0, i) + current.substring(i, len)).toUpperCase());
            }
        }

        retList.add(decodeUsage);

        return retList.toArray(new String[0]);
    }
}
