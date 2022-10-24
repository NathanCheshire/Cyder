package cyder.constants;

/**
 * Common strings used throughout Cyder.
 */
public final class CyderStrings {
    /**
     * A string to be displayed when an illegal/{@link Deprecated} constructor is invoked.
     */
    public static final String ILLEGAL_CONSTRUCTOR = "Illegal constructor";

    /**
     * A string used as a an error message for when a method has failed to be overridden and implemented.
     */
    public static final String NOT_IMPLEMENTED = "Method not implemented";

    /**
     * General renowned sentence in English for using all 26 latin chars.
     */
    public static final String QUICK_BROWN_FOX = "The quick brown fox jumps over the lazy dog";

    /**
     * Error message for static classes upon attempted instantiation.
     */
    public static final String ATTEMPTED_INSTANTIATION = "Instances are not permitted for this class";

    /**
     * A statement I like using in code if something happens that shouldn't ever happen.
     */
    public static final String EUROPEAN_TOY_MAKER = "What are you, some kind of European toy maker?";

    /**
     * A bullet point character used for numerous purposes.
     */
    public static final String BULLET_POINT = "\u2022";

    /**
     * The echo char to use for any instance of CyderPasswordField.
     */
    public static final char ECHO_CHAR = '\u2022';

    /**
     * The downward pointing triangle character (▼).
     */
    public static final String DOWN_ARROW = "\u25BC";

    /**
     * A string used to denote something is not available.
     */
    public static final String NOT_AVAILABLE = "N/A";

    /**
     * Don't change your number.
     */
    public static final String JENNY = "8675309";

    /**
     * The string used for loading labels when waiting for future processes to complete.
     */
    public static final String LOADING = "Loading...";

    /**
     * An opening bracket.
     */
    public static final String openingBracket = "[";

    /**
     * A closing bracket.
     */
    public static final String closingBracket = "]";

    /**
     * An opening parenthesis.
     */
    public static final String openingParenthesis = "(";

    /**
     * A closing parenthesis.
     */
    public static final String closingParenthesis = ")";

    /**
     * A comma.
     */
    public static final String comma = ",";

    /**
     * A space.
     */
    public static final String space = " ";

    /**
     * A colon.
     */
    public static final String colon = ":";

    /**
     * A quote.
     */
    public static final String quote = "\"";

    /**
     * A forward slash.
     */
    public static final String forwardSlash = "/";

    /**
     * A back slash.
     */
    public static final String backSlash = "\\";

    /**
     * A hash.
     */
    public static final String hash = "#";

    /**
     * A dash.
     */
    public static final String dash = "-";

    /**
     * A newline character.
     */
    public static final String newline = "\n";

    /**
     * The NULL string.
     */
    public static final String NULL = "NULL";

    /**
     * The NUL string.
     */
    public static final String NUL = "NUL";

    /**
     * Suppress default constructor.
     */
    private CyderStrings() {
        throw new IllegalStateException(ATTEMPTED_INSTANTIATION);
    }
}