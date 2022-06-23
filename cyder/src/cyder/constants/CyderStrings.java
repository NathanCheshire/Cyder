package cyder.constants;

import javax.swing.*;

/**
 * Common strings used throughout Cyder.
 */
public final class CyderStrings {
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
    public static final String ATTEMPTED_INSTANTIATION = "Objects are not available for utility classes";

    /**
     * A statement I like using in code if something happens that shouldn't ever happen.
     */
    public static final String EUROPEAN_TOY_MAKER = "What are you, some kind of European toy maker?";

    /**
     * A bullet point character used for numerous purposes.
     */
    public static final String BULLET_POINT = "\u2022";

    /**
     * The downward pointing triangle character (▼).
     */
    public static final String DOWN_ARROW = "\u25BC";

    /**
     * A string used to denote something is not available.
     */
    public static final String NOT_AVAILABLE = "N/A";

    /**
     * Instantiation of constants class not allowed.
     */
    private CyderStrings() {
        throw new IllegalStateException(ATTEMPTED_INSTANTIATION);
    }
}