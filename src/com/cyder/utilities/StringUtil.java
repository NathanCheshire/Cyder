package com.cyder.utilities;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class StringUtil {
    private static JTextPane outputArea;
    private Util stringUtil;

    private Thread bletchyThread;

    public StringUtil(JTextPane outputArea) {
        this.outputArea = outputArea;
        stringUtil = new Util();
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

    private void print(String Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage, null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringUtil.handle(e);
        }
    }

    private void print(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Integer.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringUtil.handle(e);
        }
    }

    private void print(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Double.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringUtil.handle(e);
        }
    }

    private void print(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Boolean.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringUtil.handle(e);
        }
    }

    private void print(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Float.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringUtil.handle(e);
        }
    }

    private void print(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Long.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringUtil.handle(e);
        }
    }

    private void print(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), String.valueOf(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringUtil.handle(e);
        }
    }

    private void print(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString(), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringUtil.handle(e);
        }
    }

    private void println(String Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringUtil.handle(e);
        }
    }

    private void println(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringUtil.handle(e);
        }
    }

    private void println(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringUtil.handle(e);
        }
    }

    private void println(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringUtil.handle(e);
        }
    }

    private void println(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringUtil.handle(e);
        }
    }

    private void println(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringUtil.handle(e);
        }
    }

    private void println(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringUtil.handle(e);
        }
    }

    private void println(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString() + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            stringUtil.handle(e);
        }
    }

    private char[] reverseArray(char[] Array) {
        String reverse = new StringBuilder(new String(Array)).reverse().toString();
        return reverse.toCharArray();
    }

    private boolean startsWith(String op, String comp) {
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

    private boolean endsWith(String op, String comp) {
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
        decodeString = decodeString.toLowerCase();
        decodeString = decodeString.replaceFirst("(?:bletchy)+", "").trim();
        final String s = decodeString;

        bletchyThread = new Thread(() -> {
            int len = s.length();

            char[] alphas = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
            char[] alphaNumeric = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                    '0','1','2','3','4','5','6','7','8','9'};

            if (useNumbers)
                alphas = alphaNumeric;

            for (int i = 1 ; i < len ; i++) {
                for (int j = 0 ; j < 7 ; j++) {

                    String current = "";

                    for (int k = 0 ; k <= len ; k++) {
                        current += alphas[stringUtil.randInt(0,alphas.length - 1)];
                    }

                    println((s.substring(0,i) + current.substring(i, len)).toUpperCase());

                    try {
                        Thread.sleep(miliDelay);
                    }

                    catch (Exception e) {
                        stringUtil.handle(e);
                    }

                    outputArea.setText("");
                }
            }

            println(s.toUpperCase());
        });

        bletchyThread.start();
    }

    public void killBletchy() {
        //todo kill all bletchy and make it it's own thread stuff like youtube thread
    }

    public void setOutputArea(JTextPane jTextPane) {
        this.outputArea = jTextPane;
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

        for (int i = 1 ; i < len ; i++) {
            for (int j = 0 ; j < 7 ; j++) {

                String current = "";

                for (int k = 0 ; k <= len ; k++)
                    current += alphas[stringUtil.randInt(0,alphas.length - 1)];

                retList.add((s.substring(0,i) + current.substring(i, len)).toUpperCase());
            }
        }

        retList.add(s.toUpperCase());

        return retList.toArray(new String[0]);
    }
}