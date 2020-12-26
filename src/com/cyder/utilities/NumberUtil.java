package com.cyder.utilities;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.enums.TitlePosition;
import com.cyder.handler.ErrorHandler;
import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;
import com.cyder.ui.CyderLabel;
import com.cyder.ui.CyderTextField;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

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

        numFrame = new CyderFrame(600,210, new ImageIcon("src/com/cyder/sys/pictures/DebugBackground.png"));
        numFrame.setTitle("Number To Words");
        numFrame.setTitlePosition(TitlePosition.CENTER);
        numFrame.initResizing();

        CyderLabel label = new CyderLabel("Enter any number to be converted into word form");
        label.setBounds(40,40,600 - 80, 80);
        numFrame.getContentPane().add(label);

        CyderTextField numField = new CyderTextField(40);
        numField.setBounds(40,120, 600- 80, 40);
        numFrame.getContentPane().add(numField);

        CyderButton find = new CyderButton("Find");
        find.setColors(CyderColors.regularRed);
        find.setFont(CyderFonts.weatherFontSmall);
        find.setBackground(CyderColors.regularRed);
        find.setBounds(40,170, 600- 80, 30);
        numFrame.getContentPane().add(find);

        numFrame.setVisible(true);
        numFrame.enterAnimation();
        numFrame.setLocationRelativeTo(null);
    }
}
