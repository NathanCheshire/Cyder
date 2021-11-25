package cyder.consts;

public class CyderRegexPatterns {
    public static final String ipv4Pattern = "\\s*[0-9]{1,3}(\\s*|\\.[0-9]{1,3}\\s*|\\.[0-9]{1,3}\\.[0-9]{1,3}\\s*|\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\s*)";
    public static final String numberPattern = "[0-9]*";
    public static final String lettersPattern = "[A-Za-z]*";

    //zero or one number, zero or more spaces, zero or one dashes, zero or more spaces, zero or one parenthesis, 0 or 3 numbers
    // zero or one parenthesis, zero or more spaces, zero or one dashes, zero or more spaces, three numbers,
    // zero or more spaces, zero or one dashes, zero or more spaces, four numbers, ending whitespace
    public static final String phoneNumberPattern = "";
    public static final String phoneNumberAreaCodeExtendedPattern = "";
}
