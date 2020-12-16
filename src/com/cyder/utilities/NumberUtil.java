package com.cyder.utilities;

import com.cyder.handler.ErrorHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

public class NumberUtil {
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
}
