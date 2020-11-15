package com.cyder.utilities;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.Collections;

public class StringUtil {
    private static JTextPane outputArea;
    private Util stringUtil;

    public StringUtil(JTextPane outputArea) {
        this.outputArea = outputArea;
        stringUtil = new Util();
    }

    public void help(JTextPane outputArea) {
        this.outputArea = outputArea;

        //todo make this dynamic so that it can show every command and a description of it too
        String[] Helps = {"Pixalte a Picture",
                "Home",
                "Mathsh",
                "Pizza",
                "Vexento",
                "Youtube",
                "note",
                "Create a User",
                "Binary",
                "Font",
                "Color",
                "Preferences",
                "Hasher",
                "Directory Search",
                "Tic Tac Toe",
                "Youtube Thumbnail",
                "Java",
                "Tell me a story",
                "Coffee",
                "Papers Please",
                "Delete User",
                "YouTube Word Search",
                "System Properties",
                "Donuts",
                "System Sounds",
                "Weather",
                "Music",
                "mp3",
                "dance",
                "hangman",
                "youtube script"};

        ArrayList<Integer> UniqueIndexes = new ArrayList<>();

        for (int i = 0; i < Helps.length; i++)
            UniqueIndexes.add(i);

        Collections.shuffle(UniqueIndexes);
        println("Try typing:");

        for (int i = 0; i < 10; i++)
            println(Helps[UniqueIndexes.get(i)]);
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
}