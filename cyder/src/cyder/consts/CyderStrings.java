package cyder.consts;

import javax.swing.*;

public class CyderStrings {
    /**
     * Here string used for debugging.
     */
    public static final String HERE = "here";

    /**
     * General renowned sentence in English for using all 26 latin chars.
     */
    public static final String QUICK_BROWN_FOX = "The quick brown fox jumps over the lazy dog";

    /**
     * The echo char to use for any instance of CyderPasswordField.
     */
    public static final char ECHO_CHAR = new JPasswordField().getEchoChar();

    /**
     * Error message for static classes upon attempted instantiation.
     */
    public static final String attemptedClassInstantiation = "States are not available for static classes";

    /**
     * The standard separator string used for printing within the standard output area.
     */
    public static final String commentSepString = "----------------------------------------";

    /**
     * A bulletpoint character used for numerous purposes.
     */
    public static final String bulletPoint = "\u2022";

    private CyderStrings() {
        throw new IllegalStateException(attemptedClassInstantiation);
    }
}