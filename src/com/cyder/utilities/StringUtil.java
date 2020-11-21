package com.cyder.utilities;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class StringUtil {
    private static JTextPane outputArea;
    private GeneralUtil stringGeneralUtil;

    private bletchyThread bletchThread;

    public StringUtil(JTextPane outputArea) {
        this.outputArea = outputArea;
        stringGeneralUtil = new GeneralUtil();
    }

    public void setOutputArea(JTextPane jTextPane) {
        this.outputArea = jTextPane;
    }

    public void help(JTextPane outputArea) {
        this.outputArea = outputArea;

        String[] Helps = {
                "askew",
                "mp3",
                "Home",
                "note",
                "Font",
                "Java",
                "logic",
                "Pizza",
                "Color",
                "Music",
                "dance",
                "logout",
                "Mathsh",
                "Binary",
                "Hasher",
                "Coffee",
                "Donuts",
                "threads",
                "weather",
                "Vexento",
                "Youtube",
                "Weather",
                "hangman",
                "edit user",
                "long word",
                "rgb to hex",
                "barrel roll",
                "Preferences",
                "Tic Tac Toe",
                "Delete User",
                "resize image",
                "Create a User",
                "Papers Please",
                "System Sounds",
                "youtube script",
                "Tell me a story",
                "Directory Search",
                "Youtube Thumbnail",
                "System Properties",
                "Pixelate a Picture",
                "YouTube Word Search",
        };

        ArrayList<Integer> UniqueIndexes = new ArrayList<>();

        for (int i = 0; i < Helps.length; i++)
            UniqueIndexes.add(i);

        Collections.shuffle(UniqueIndexes);
        println("Try typing:");

        for (int i = 0; i < 10; i++)
            println(Helps[UniqueIndexes.get(i)]);
    }

    public void print(String Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage, null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringGeneralUtil.handle(e);
        }
    }

    public void print(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Integer.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringGeneralUtil.handle(e);
        }
    }

    public void print(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Double.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringGeneralUtil.handle(e);
        }
    }

    public void print(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Boolean.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringGeneralUtil.handle(e);
        }
    }

    public void print(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Float.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringGeneralUtil.handle(e);
        }
    }

    public void print(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Long.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringGeneralUtil.handle(e);
        }
    }

    public void print(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), String.valueOf(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringGeneralUtil.handle(e);
        }
    }

    public void print(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString(), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringGeneralUtil.handle(e);
        }
    }

    public void println(String Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringGeneralUtil.handle(e);
        }
    }

    public void println(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringGeneralUtil.handle(e);
        }
    }

    public void println(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringGeneralUtil.handle(e);
        }
    }

    public void println(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringGeneralUtil.handle(e);
        }
    }

    public void println(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringGeneralUtil.handle(e);
        }
    }

    public void println(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringGeneralUtil.handle(e);
        }
    }

    public void println(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringGeneralUtil.handle(e);
        }
    }

    public void println(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString() + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringGeneralUtil.handle(e);
        }
    }

    public char[] reverseArray(char[] Array) {
        String reverse = new StringBuilder(new String(Array)).reverse().toString();
        return reverse.toCharArray();
    }

    public boolean startsWith(String op, String comp) {
        char[] opA = op.toLowerCase().toCharArray();

        char[] compA = comp.toLowerCase().toCharArray();

        for (int i = 0 ; i < comp.length() ; i++) {
            if (Math.min(compA.length, opA.length) >= i) {
                return false;
            }

            if (Math.min(compA.length, opA.length) < i && compA[i] != opA[i]) {
                return false;
            }

            else if (Math.min(compA.length, opA.length) < i && compA[i] == opA[i] && i == compA.length - 1 && opA[i+1] == ' ') {
                return true;
            }
        }

        return false;
    }

    public boolean endsWith(String op, String comp) {
        char[] opA = reverseArray(op.toLowerCase().toCharArray());
        char[] compA = reverseArray(comp.toLowerCase().toCharArray());

        for (int i = 0 ; i < comp.length() ; i++) {
            if (Math.min(opA.length, compA.length) >= i) {
                return false;
            }

            if (i < Math.min(opA.length, compA.length) && compA[i] != opA[i]) {
                return false;
            }

            else if (compA[i] == opA[i] && i == compA.length - 1 && opA[i+1] == ' ') {
                return true;
            }
        }

        return false;
    }

    public void bletchy(String decodeString, boolean useNumbers, int miliDelay) {
        String[] print = bletchy(decodeString,useNumbers);

        bletchThread = new bletchyThread(print,miliDelay);
    }

    private class bletchyThread  {
        private boolean exit = false;

        bletchyThread(String[] print, int miliDelay) {
            new Thread(() -> {
                for (int i = 1 ; i < print.length ; i++) {
                    if (exit) {
                        println("Escaped");
                        return;
                    }

                    println(print[i]);

                    try {
                        Thread.sleep(miliDelay);
                    }

                    catch (Exception e) {
                        stringGeneralUtil.handle(e);
                    }

                    outputArea.setText("");
                }

                println(print[print.length - 1].toUpperCase());

                this.kill();
            }).start();
        }

        public void kill() {
            this.exit = true;
        }
    }

    public void killBletchy() {
        if (bletchThread != null)
            bletchThread.kill();
    }

    public String[] bletchy(String decodeString, boolean useNumbers) {
        LinkedList<String> retList = new LinkedList<>();

        decodeString = decodeString.toLowerCase();
        decodeString = decodeString.replaceFirst("(?:bletchy)+", "").trim();
        final String s = decodeString;

        int len = s.length();

        char[] alphas = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
        char[] alphaNumeric = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                '0','1','2','3','4','5','6','7','8','9'};

        if (useNumbers)
            alphas = alphaNumeric;

        int iterationsPerChar = 7;

        for (int i = 1 ; i < len ; i++) {
            for (int j = 0 ; j < iterationsPerChar ; j++) {

                String current = "";

                for (int k = 0 ; k <= len ; k++)
                    current += alphas[stringGeneralUtil.randInt(0,alphas.length - 1)];

                retList.add((s.substring(0,i) + current.substring(i, len)).toUpperCase());
            }
        }

        retList.add(s.toUpperCase());

        return retList.toArray(new String[0]);
    }

    public void printArr(Object[] arr) {
        for (Object o : arr) println(o);
    }
}