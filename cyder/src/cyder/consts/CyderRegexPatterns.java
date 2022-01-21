package cyder.consts;

public final class CyderRegexPatterns {
    /**
     * Pattern used to validate an IPV4 address
     */
    public static final String ipv4Pattern =
            "\\s*[0-9]{1,3}(\\s*|\\.[0-9]{1,3}\\s*|\\.[0-9]{1,3}\\.[0-9]{1,3}\\s*|\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\s*)";

    /**
     * Pattern used to identify 0 or more numbers
     */
    public static final String numberPattern = "[0-9]*";

    /**
     * Pattern to identify 0 or more letters
     */
    public static final String lettersPattern = "[A-Za-z]*";

    /**
     * Pattern to identify common phone number patterns
     */
    public static final String phoneNumberPattern =
            "\\s*[0-9]?\\s*[\\-]?\\s*[\\(]?\\s*[0-9]{0,3}\\s*[\\)]?\\s*[\\-]?\\s*[0-9]{3}\\s*[\\-]?\\s*[0-9]{4}\\s*";

    /**
     * Pattern to identify common phone number patterns with an
     * extended region code (+1 is american and the numbers range
     * from 1-9 but if they were extended, this pattern would match those new numbers)
     */
    public static final String phoneNumberRegionCodeExtendedPattern =
            "\\s*[0-9]{0,2}\\s*[\\-]?\\s*[\\(]?\\s*[0-9]{0,3}\\s*[\\)]?\\s*[\\-]?\\s*[0-9]{3}\\s*[\\-]?\\s*[0-9]{4}\\s*";

    /**
     * Pattern to match zero or more numbers and letters
     */
    public static final String numbersAndLettersPattern = "[a-zA-Z0-9]*";

    /**
     * Regex pattern to determine if a line is a comment.
     * An interesting note, a line such as this will not be counted as a comment:
     * System.out.println(""); BLOCK COMMENT
     * System.out.println("" BLOCK COMMENT);
     * Even though these are valid comments
     */
    public static final String commentPattern = "\\s*[/]{2}.*|\\s*[/][*].*|\\s*[*].*|\\s*.*[*][/]\\s*";

    /**
     * Illegal class instantiation
     */
    private CyderRegexPatterns() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    public static final String openGroup = "(";
    public static final String closeGroup = ")";
    public static final String digit = "\\d";
    public static final String nonDigit = "\\D";
    public static final String whitespace = "\\s";
    public static final String nonWhitespace = "\\S";
    public static final String letterNumberUnderscore = "\\w";
    public static final String nonLetterNumberUnderscore = "\\W";
    public static final String to = "-";
    public static final String previousOptional = "?";
    public static final String or = "|";
    public static final String zeroOrMore = "*";
    public static final String oneOrMore = "+";

    /**
     * Retuns a regex pattern for the previous char/sequence to be repeated n times.
     *
     * @param n the number of times to repeat the previous char/sequence
     * @return a regex pattern for the previous char/sequence to be repeated n times
     */
    public static String nTimes(int n) {
        return "{" + n + "}";
    }

    /**
     * Returns a regex pattern for the previous char/sequence to be reapted in the range [n, m].
     *
     * @param n the lower bound
     * @param m the upper bound
     * @return a regex pattern for the previous char/sequence to be reapted in the range [n, m]
     */
    public static String nToMTimes(int n, int m) {
        return "{" + n + "," + m + "}";
    }

    public static final String beginLine = "^";
    public static final String endLine = "$";
    public static final String lattin = "\\{IsLatin}";
    public static final String ascii = "\\p{ASCII}";
    public static final String anyChar = ".";
    public static final String openCharClass = "[";
    public static final String closeCharClass = "]";
    public static final String notCharClass = "^";
}
