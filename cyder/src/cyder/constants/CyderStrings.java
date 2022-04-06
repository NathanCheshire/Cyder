package cyder.constants;

import javax.swing.*;

/**
 * Common strings used throughout Cyder.
 */
public class CyderStrings {
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
    public static final String attemptedInstantiation = "States are not available for static classes";

    /**
     * The standard separator string used for printing within the standard output area.
     */
    public static final String commentSepString = "----------------------------------------";

    /**
     * A statement I like using in code if something happens that shouldn't ever happen.
     */
    public static final String europeanToymaker = "What are you, some kind of European toy maker?";

    /**
     * A bulletpoint character used for numerous purposes.
     */
    public static final String bulletPoint = "\u2022";

    /**
     * The downward pointing triangle character (▼).
     */
    public static final String downArrow = "\u25BC";

    /**
     * Instantiation of constants class not allowed.
     */
    private CyderStrings() {
        throw new IllegalStateException(attemptedInstantiation);
    }
}