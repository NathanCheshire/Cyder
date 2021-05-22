package cyder.utilities;

import cyder.handler.ErrorHandler;

import java.awt.*;
import java.io.*;

public class InputUtil {

    private InputUtil() {} //private constructor to avoid object creation

    public static boolean confirmation(String input) {
        return (input.toLowerCase().contains("yes") || input.equalsIgnoreCase("y"));
    }

    public static String getString(String message) {
        try {
            File inputFile = new File("String.txt");

            inputFile.createNewFile();
            inputFile.delete();

            File f = new File("InputMessage.txt");
            f.createNewFile();

            BufferedWriter br = new BufferedWriter(new FileWriter("InputMessage.txt"));
            br.write(message);
            br.flush();
            br.close();

            Desktop.getDesktop().open(new File("sys/jars/StringChooser.jar"));

            f = new File("String.txt");
            f.delete();

            while (!f.exists()) {
                Thread.onSpinWait();
            }

            BufferedReader waitReader = new BufferedReader(new FileReader("String.txt"));

            Thread.sleep(200);

            String chosenString =  waitReader.readLine();

            waitReader.close();

            f.delete();
            new File("InputMessage.txt").delete();

            if (chosenString == null || chosenString.equals("null"))
                return "null";
            else
                return chosenString;
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return null;
    }
}
