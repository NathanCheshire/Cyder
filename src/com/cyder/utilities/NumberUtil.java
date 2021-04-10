package com.cyder.utilities;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.enums.ArrowDirection;
import com.cyder.enums.TitlePosition;
import com.cyder.handler.ErrorHandler;
import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;
import com.cyder.ui.CyderLabel;
import com.cyder.ui.CyderTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import static com.cyder.Constants.CyderStrings.DEFAULT_BACKGROUND_PATH;

public class NumberUtil {
    private static CyderFrame numFrame;

    public static int randInt(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    public static String toBinary(int value) {
        String bin;

        if (value > 0) {
            int colExp = 0;
            int val = value;

            while (Math.pow(2, colExp) <= value) {
                colExp = colExp + 1;
            }

            bin = "";

            do {
                colExp--;
                int columnWeight = (int) Math.pow(2, colExp);

                if (columnWeight <= val) {
                    bin += "1";
                    val -= columnWeight;
                }

                else
                    bin += "0";
            }

            while (colExp > 0);

            return bin;
        }

        return "NaN";
    }

    public static boolean isPrime(int num) {
        ArrayList<Integer> Numbers = new ArrayList<>();

        for (int i = 3 ; i < Math.ceil(Math.sqrt(num)) ; i += 2)
            if (num % i == 0)
                Numbers.add(i);

        return Numbers.isEmpty();
    }

    public static LinkedList<Long> fib(long a, long b, int numFibs) {
        LinkedList<Long> ret = new LinkedList();
        ret.add(a);
        for (int i = 1 ; i < numFibs ; i++) {
            ret.add(b);

            long next = a + b;

            a = b;
            b = next;
        }

        return ret;
    }

    public static int totalCodeLines(File startDir) {
        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            for (File f : files)
                ret += totalCodeLines(f);
        }

        else if (startDir.getName().endsWith(".java")) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line = "";
                int localRet = 0;

                while ((line = lineReader.readLine()) != null)
                    localRet++;

                return localRet;
            }

            catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        }

        return ret;
    }

    public static void numberToWord() {
        if (numFrame != null)
            numFrame.closeAnimation();

        numFrame = new CyderFrame(600,230, new ImageIcon(DEFAULT_BACKGROUND_PATH));
        numFrame.setTitle("Number To Words");
        numFrame.setTitlePosition(TitlePosition.CENTER);
        numFrame.initResizing();

        CyderLabel label = new CyderLabel("<html>Enter any number to be converted into word form<html/>");
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        label.setBounds(40,40,600 - 80, 80);
        numFrame.getContentPane().add(label);

        CyderTextField numField = new CyderTextField(40);
        numField.setCharLimit(68);
        numField.setBounds(40,120, 600- 80, 40);
        numFrame.getContentPane().add(numField);

        numField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if(!(evt.getKeyChar() == KeyEvent.VK_MINUS) && !(evt.getKeyChar() >= '0' && evt.getKeyChar() <= '9')) {
                    Toolkit.getDefaultToolkit().beep();
                    evt.consume();
                }
            }
        });

        CyderButton find = new CyderButton("Find");
        find.setColors(CyderColors.regularRed);
        find.setFont(CyderFonts.weatherFontSmall);
        find.setBackground(CyderColors.regularRed);
        find.setBounds(40,170, 600- 80, 40);
        find.addActionListener(e -> toWords(numField.getText()));
        numFrame.getContentPane().add(find);

        numFrame.setVisible(true);
        numFrame.enterAnimation();
        numFrame.setLocationRelativeTo(null);
    }

    private static void toWords(String wordRep) {
        if (wordRep == null || wordRep.length() == 0)
            return;

        BigInteger num = new BigInteger(wordRep);
        if (num.compareTo(BigInteger.ZERO) == 0) {
            numFrame.notify("Zero you idiot",3000, ArrowDirection.TOP,165);
            return;
        }

        boolean negative = num.compareTo(BigInteger.ZERO) < 0;
        wordRep = wordRep.replace("-","");

        while (wordRep.length() % 3 != 0)
            wordRep = "0" + wordRep;

        String[] arr = java.util.Arrays.toString(wordRep.split("(?<=\\G...)")).replace("[","").replace("]","").replace(" ","").split(",");
        LinkedList<Integer> trioNums = new LinkedList<>();
        LinkedList<String> trioStrings = new LinkedList<>();

        for (String str : arr)
            trioNums.add(Integer.parseInt(str));

        for (int trio : trioNums)
            trioStrings.add(trioToWords(trio));

        LinkedList<String> reversed = new LinkedList<>();

        for (String str : trioStrings)
            reversed.push(str);

        trioStrings.clear();

        for (int i = 0 ; i < reversed.size() ; i++) {
            String currentNum = reversed.get(i);
            String prefix = prefix(i);
            String add = currentNum + prefix;

            if (add.trim().length() == 0 || add.trim().charAt(0) == '-')
                continue;

            trioStrings.push(add);
        }

        String build = "";

        for (String trioStr : trioStrings)
            build += trioStr.trim() + " ";

        String neg = negative ? "negative " : "";

        numFrame.inform("<html>" + neg + build.trim() + "</html>","Conversion");
    }

    private static String trioToWords(int num) {
        int ones = num % 10;
        int tens = (num % 100) / 10;

        int below100 = ones + tens * 10;

        int hundreds = num / 100;

        String hundredsStr = (onesPlace[ hundreds].equals("") ? "" : onesPlace[ hundreds] + " hundred");
        String below100Str;

        if (below100 < 20 && below100 > 9) {
            below100Str = teens[below100 - 10];
        }

        else {
            below100Str = tensPlace[tens] + " " + onesPlace[ones];
        }

        return (hundredsStr + " " + below100Str);
    }

    private static String[] onesPlace = {"","one","two","three","four","five","six","seven","eight","nine"};
    private static String wordForOnes(int num) {
        return onesPlace[num];
    }

    private static String[] tensPlace = {"","","twenty","thirty","fourty","fifty","sixty","seventy","eighty","ninety"};
    private static String wordForTens(int num) {
        return tensPlace[num];
    }

    private static String[] teens = {"ten","eleven","twelve","thirteen","fourteen","fifteen","sixteen","seventeen","eighteen","nineteen"};
    private static String wordForTeenNums(int num) {
        return teens[num - 10];
    }

    private static String[] prefixes = {"","-thousand","-million","-billion","-trillion","-quadrillion",
            "-quintillion","-sextillion","-septillion", "-octillion", "-nonillion",
            "-decillion", "-undecillion", "-duodecillion", "-tredecillion",
            "-quattuordecillion","-quindecillion", "-sexdexillion", "-septendecillion",
            "-octodecillion", "-novemdecillion", "-vigintillion","-centillion"};
    private static String prefix(int trioPlace) {
        return prefixes[trioPlace];
    }
}
