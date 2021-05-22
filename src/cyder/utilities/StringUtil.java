package cyder.utilities;

import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    private static JTextPane outputArea;

    private StringUtil() {}

    //todo links to input handler

    public StringUtil(JTextPane outputArea) {
        this.outputArea = outputArea;
    }

    private boolean userInputMode;
    private String userInputDesc;

    private bletchyThread bletchThread;

    public boolean getUserInputMode() {
        return this.userInputMode;
    }

    public void setUserInputMode(boolean b) {
        this.userInputMode = b;
    }

    public String getUserInputDesc() {
        return this.userInputDesc;
    }
    public void setUserInputDesc(String s) {
        this.userInputDesc = s;
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
            ErrorHandler.handle(e);
        }
    }

    public void print(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Integer.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Double.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Boolean.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Float.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Long.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), String.valueOf(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString(), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(String Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString() + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static char[] reverseArray(char[] Array) {
        String reverse = new StringBuilder(new String(Array)).reverse().toString();
        return reverse.toCharArray();
    }

    public static boolean startsWith(String op, String comp) {
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

    public static boolean endsWith(String op, String comp) {
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

    //todo move inner class to threads package
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
                        ErrorHandler.handle(e);
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

    public static String[] bletchy(String decodeString, boolean useNumbers) {
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
                    current += alphas[NumberUtil.randInt(0,alphas.length - 1)];

                retList.add((s.substring(0,i) + current.substring(i, len)).toUpperCase());
            }
        }

        retList.add(s.toUpperCase());

        return retList.toArray(new String[0]);
    }

    public void printArr(Object[] arr) {
        for (Object o : arr) println(o);
    }

    public void logSuggestion(String input) {
        try {
            if (input != null && !input.equals("") && !filterLanguage(input) && input.length() > 10 && !filterLanguage(input)) {
                File suggestionsFile = new File("sys/text/suggestions.txt");

                if (!suggestionsFile.exists())
                    suggestionsFile.mkdir();

                BufferedWriter sugWriter = new BufferedWriter(new FileWriter(suggestionsFile, true));

                sugWriter.write("User " + ConsoleFrame.getUsername() + " at " + new TimeUtil().weatherTime() + " made the suggestion: ");
                sugWriter.write(System.getProperty("line.separator"));

                sugWriter.write(input);

                sugWriter.write(System.getProperty("line.separator"));
                sugWriter.write(System.getProperty("line.separator"));

                sugWriter.flush();
                sugWriter.close();

                println("Request registered.");
                sugWriter.close();
            }
        }

        catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }

    public static String getApostrophe(String name) {
        if (name.endsWith("s"))
            return "'";
        else
            return "'s";
    }

    public static boolean empytStr(String s) {
        return (s == null ? null: (s == null) || (s.trim().equals("")) || (s.trim().length() == 0));
    }

    public static String fillString(int count, String c) {
        StringBuilder sb = new StringBuilder(count);

        for (int i = 0; i < count; i++) {
            sb.append(c);
        }

        return sb.toString();
    }

    public static String firstWord(String Word) {
        String[] sentences = Word.split(" ");
        return sentences[0];
    }

    public static boolean isPalindrome(String word) {
        return Arrays.equals(word.toLowerCase().toCharArray(), reverseArray(word.toLowerCase().toCharArray()));
    }

    public static String firstNumber(String Search) {
        Pattern Pat = Pattern.compile("\\d+");
        Matcher m = Pat.matcher(Search);
        return m.find() ? m.group() : null;
    }

    public static String[] combineArrays(String[] a, String[] b) {
        int length = a.length + b.length;
        String[] result = new String[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static String capsFirst(String Word) {
        StringBuilder SB = new StringBuilder(Word.length());
        String[] Words = Word.split(" ");

        for (String word : Words) {
            SB.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }

        return SB.toString();
    }

    public static String filterLeet(String filter) {
        return filter.replace("!","i").replace("3","e")
                .replace("4","a").replace("@","a")
                .replace("5","s").replace("7","t")
                .replace("0","o").replace("9","g")
                .replace("%", "i").replace("#","h")
                .replace("$","s").replace("1","i");
    }

    public static boolean hasWord(String userInput, String blockedWord) {
        userInput.toLowerCase();
        blockedWord.toLowerCase();

        if (blockedWord.equals(userInput) ||
                blockedWord.contains(' ' + userInput + ' ') ||
                blockedWord.contains(' ' + userInput) ||
                blockedWord.contains(userInput + ' '))
            return true;

        else return blockedWord.contains(userInput + ' ');
    }

    public static boolean filterLanguage(String userInput) {
        try {
            BufferedReader vReader = new  BufferedReader(new FileReader("sys/text/v.txt"));
            String blockedWord = vReader.readLine();
            userInput = filterLeet(userInput.toLowerCase());

            while(blockedWord != null)  {
                if (hasWord(userInput, blockedWord)) {
                    vReader.close();
                    return true;
                }

                blockedWord = vReader.readLine();
            }

            vReader.close();
        }

        catch (Exception ex) {
            ErrorHandler.handle(ex);
        }

        return false;
    }

    public static String firstCharToLowerCase(String str) {
        if (str == null || str.length() == 0)
            return "";

        if(str.length() == 1)
            return str.toLowerCase();
        else return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
}