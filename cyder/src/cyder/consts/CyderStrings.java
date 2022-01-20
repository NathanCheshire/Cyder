package cyder.consts;

import javax.swing.*;

public class CyderStrings {
    public static final String HERE = "here";

    public static final String LENGTH_ZERO = "";
    public static final String LENGTH_ONE = "0";
    public static final String LENGTH_TWO = "01";
    public static final String LENGTH_THREE = "012";
    public static final String LENGTH_FOUR = "0123";
    public static final String LENGTH_FIVE = "01234";
    public static final String LENGTH_SIX = "102345";
    public static final String LENGTH_SEVEN = "0123456";
    public static final String LENGTH_EIGHT = "01234567";
    public static final String LENGTH_NINE = "012345678";

    public static final String QUICK_BROWN_FOX = "The quick brown fox jumps over the lazy dog";

    public static final char ECHO_CHAR = new JPasswordField().getEchoChar();

    //instantiation of a static class message
    public static final String attemptedClassInstantiation = "States are not available for static classes";

    public static final String commentSepString = "----------------------------------------";

    public static final String bulletPoint = "\u2022";

    private CyderStrings() {
        throw new IllegalStateException(attemptedClassInstantiation);
    }
}