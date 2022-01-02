package cyder.consts;

public class CyderRegexPatterns {
    public static final String ipv4Pattern = "\\s*[0-9]{1,3}(\\s*|\\.[0-9]{1,3}\\s*|\\.[0-9]{1,3}\\.[0-9]{1,3}\\s*|\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\s*)";
    public static final String numberPattern = "[0-9]*";
    public static final String lettersPattern = "[A-Za-z]*";

    public static final String phoneNumberPattern = "\\s*[0-9]?\\s*[\\-]?\\s*[\\(]?\\s*[0-9]{0,3}\\s*[\\)]?\\s*[\\-]?\\s*[0-9]{3}\\s*[\\-]?\\s*[0-9]{4}\\s*";
    public static final String phoneNumberAreaCodeExtendedPattern = "\\s*[0-9]{0,2}\\s*[\\-]?\\s*[\\(]?\\s*[0-9]{0,3}\\s*[\\)]?\\s*[\\-]?\\s*[0-9]{3}\\s*[\\-]?\\s*[0-9]{4}\\s*";

    public static final String numbersAndLettersPattern = "[a-zA-Z0-9]*";

    //patterns for code ananlysis
    public static final String commentPattern = "\\s*[/]{2}.*|\\s*[/][*].*|\\s*[*].*|\\s*.*[*][/]\\s*";

    private CyderRegexPatterns() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    public String zeroOrMoreWhiteSpace() {
        return "\\s*";
    }

    public String oneOrMoreWhiteSpace() {
        return "\\s+";
    }

    /**
     * Returns the regex pattern for an optional char
     * @param chars chars enclosed in square brackets that should be optional
     * @return the string you gave with "?" at the end basically
     */
    public String optionalChar(String chars) {
        return chars + "?";
    }

    /**
     * Returns a regex representing n successive digits
     * @param n the number of digits to accept
     * @return regex representing n successive digits, no more, no less
     */
    public String nDigits(int n) {
        return "[0-9]{" + n + "}";
    }

    /**
     * Same as {@code nDigits(String)} except the number of digits may be in the inclusive range of [0, n]
     * @param n the max number of digits
     * @return a regex representing 0 to n digits
     */
    public String zeroToNDigits(int n) {
        if (n == 0)
            throw new IllegalArgumentException("Zero digits is not allowed for a regex");
        return "[0-9]{0," + n + "}";
    }
}
