package cyder.threads;

import cyder.handler.ErrorHandler;
import cyder.utilities.NumberUtil;
import cyder.utilities.StringUtil;

import javax.swing.*;
import java.util.LinkedList;

public class BletchyThread {

    private bletchyThread bletchThread;
    private JTextPane outputArea;
    private StringUtil su;
    private static boolean isActive = false;

    /**
     * Class instantiation requires a JTextPane so that we may use a StringUtil to append to the profvided JTextPane
     * @param outputArea - the JTextPane belonging to a ConsoleFrame to print to
     */
    public BletchyThread(JTextPane outputArea) {
        this.outputArea = outputArea;
        this.su = new StringUtil(outputArea);
    }

    /**
     * Invoke the bletchy decode animtion with the following  parameters.
     *
     * @param decodeString - the final string to decode and display after the bletchy animation has finished
     * @param useNumbers - a boolean depicting whether or not to use numbers in the alphabetic characters for the animation
     * @param miliDelay - the milisecond delay in between animation frames
     * @param useUnicode - a boolean depicting whether or not to use more than just latin letters and possibly numbers
     */
    public void bletchy(String decodeString, boolean useNumbers, int miliDelay, boolean useUnicode) {
        if (isActive())
            return;

        String[] print = bletchy(decodeString, useNumbers, useUnicode);
        bletchThread = new bletchyThread(print, miliDelay);
    }

    private class bletchyThread  {
        private boolean exit = false;

        bletchyThread(String[] print, int miliDelay) {
            new Thread(() -> {
                isActive = true;

                for (int i = 1 ; i < print.length ; i++) {
                    if (exit) {
                        return;
                    }

                    su.println(print[i]);

                    try {
                        Thread.sleep(miliDelay);
                    }

                    catch (Exception e) {
                        ErrorHandler.handle(e);
                    }

                    su.removeLastLine();
                }

                su.println(print[print.length - 1].toUpperCase());

                this.kill();
            },"bletchy printing thread").start();
        }

        public void kill() {
            this.exit = true;
            isActive = false;
        }
    }

    public static boolean isActive() {
        return isActive;
    }

    /**
     * Kills any and all bletchy printing threads
     */
    public void killBletchy() {
        if (bletchThread != null)
            bletchThread.kill();
    }

    /**
     * Returns an array of strings abiding by the parameters for a bletchy thread to print out
     * @param decodeString - the string to decode
     * @param useNumbers - a boolean turning on number usage
     * @param useUnicode - a boolean turning on random unicode chars
     * @return - the string array to be used by a bletchy thread
     */
    public String[] bletchy(String decodeString, boolean useNumbers, boolean useUnicode) {
        LinkedList<String> retList = new LinkedList<>();

        decodeString = decodeString.toLowerCase();
        decodeString = decodeString.replaceFirst("(?:bletchy)+", "").trim();
        final String s = decodeString;

        int len = s.length();

        Character[] alphas = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
        Character[] alphaNumeric = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                '0','1','2','3','4','5','6','7','8','9'};

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

                retList.add((s.substring(0,i) + current.substring(i, len)).toUpperCase());
            }
        }

        retList.add(s.toUpperCase());

        return retList.toArray(new String[0]);
    }
}
