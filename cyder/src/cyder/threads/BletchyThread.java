package cyder.threads;

import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderOutputPane;
import cyder.utilities.NumberUtil;
import cyder.utilities.ReflectionUtil;
import cyder.utilities.StringUtil;

import javax.swing.*;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class BletchyThread {
    /**
     * Inner animator class.
     */
    private static BletchyAnimator bletchyAnimator;

    /**
     * Whether or not bletchyAnimator is active.
     */
    private static boolean isActive = false;

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
     * @param semaphore the semaphore to use to block other text being added to the linked JTextPane
     */
    public static void initialize(JTextPane outputArea, Semaphore semaphore) {
        stringUtil = new StringUtil(new CyderOutputPane(outputArea));
        printingSemaphore = semaphore;
    }

    /**
     * Invoke the bletchy decode animtion with the following parameters on the linked JTextPane.
     *
     * @param decodeString the final string to decode and display after the bletchy animation has finished
     * @param useNumbers a boolean depicting whether or not to use numbers in the alphabetic characters for the animation
     * @param miliDelay the milisecond delay in between animation frames
     * @param useUnicode a boolean depicting whether or not to use more than just latin letters and possibly numbers
     */
    public static void bletchy(String decodeString, boolean useNumbers, int miliDelay, boolean useUnicode) {
        //starting not permitting if bletchy or this is already underway
        if (BletchyThread.isActive() || MasterYoutubeThread.isActive()) {
            ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().notify("Cannot start bletchy/youtube thread" +
                    " at the same time as another instance.");
            return;
        }

        //invoke the thread with passed params
        bletchyAnimator = new BletchyAnimator(getBletchyArray(decodeString, useNumbers, useUnicode), miliDelay);
    }

    /**
     * Inner class used to invoke the bletchy animation.
     */
    private static class BletchyAnimator {
        BletchyAnimator(String[] print, int miliDelay) {
            CyderThreadRunner.submit(() -> {
                try {
                    isActive = true;

                    printingSemaphore.acquire();

                    for (int i = 1 ; i < print.length ; i++) {
                        //check exit condition
                        if (!isActive) {
                            printingSemaphore.release();
                            return;
                        }


                        //print iteration

                        stringUtil.println(print[i]);

                        //timeout
                        Thread.sleep(miliDelay);

                        //remove iteration
                        stringUtil.removeLastLine();
                    }

                    //since we removed the last line, add the final value back
                    stringUtil.println(print[print.length - 1]);
                    printingSemaphore.release();

                    this.kill();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                    this.kill();
                }
            },"bletchy printing thread: finalString = " + print[print.length - 1]);
        }

        /**
         * Kills this bletchy threaad
         */
        public void kill() {
            isActive = false;
        }
    }

    /**
     * Returns whether or not this BletchyThread has a BletchyAnimation thread underway.
     *
     * @return whether or not this BletchyThread has a BletchyAnimation thread underway
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
     * Character array of all lowercase latin chars.
     */
    private static Character[] alphas =
            {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};

    /**
     * Character array of all lowercase latin chars and the base 10 numbers.
     */
    private static Character[] alphaNumeric =
            {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            '0','1','2','3','4','5','6','7','8','9'};

    /**
     * Returns an array of Strings abiding by the parameters for a bletchy thread to print.
     *
     * @param decodeString the string to decode
     * @param useNumbers a boolean turning on number usage
     * @param useUnicode a boolean turning on random unicode chars
     * @return the string array to be used by a bletchy thread
     */
    private static String[] getBletchyArray(final String decodeString, boolean useNumbers, boolean useUnicode) {
        LinkedList<String> retList = new LinkedList<>();

        final String decodeUsage = decodeString.toLowerCase().trim();

        int len = decodeUsage.length();

        if (useNumbers)
            alphas = alphaNumeric;

        if (useUnicode) {
            LinkedList<Character> chars = new LinkedList<>();

            //card suites
            for (int index = 9824; index <= 9835; index++)
                chars.add((char) index);

            //cool looking unicode chars
            for (int index = 880; index <= 1023; index++)
                chars.add((char) index);

            alphas = chars.toArray(new Character[chars.size()]);
        }

        int iterationsPerChar = 7;

        for (int i = 1 ; i < len ; i++) {
            for (int j = 0 ; j < iterationsPerChar ; j++) {

                String current = "";

                for (int k = 0 ; k <= len ; k++)
                    current += alphas[NumberUtil.randInt(0,alphas.length - 1)];

                retList.add((decodeUsage.substring(0,i) + current.substring(i, len)).toUpperCase());
            }
        }

        retList.add(decodeUsage);

        return retList.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
