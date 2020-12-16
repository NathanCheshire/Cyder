package com.cyder.utilities;

public class InputUtil {
    public static boolean confirmation(String input) {
        return (input.toLowerCase().contains("yes") || input.equalsIgnoreCase("y"));
    }

    //todo get input method with cyderframe and wait for file to be written to before returning

    public static String getInput(String message, String title) {
        String ret = null;

        //todo show stuff and wait for file

        while (ret == null)
            Thread.onSpinWait();

        //todo close stuff

        return ret;
    }
}
