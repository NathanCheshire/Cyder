package cyder.consts;

public class CyderRegexPatterns {
    public static final String ipv4Pattern = "\\s*[0-9]{1,3}(\\s*|\\.[0-9]{1,3}\\s*|\\.[0-9]{1,3}\\.[0-9]{1,3}\\s*|\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\s*)";
    public static final String numberPattern = "[0-9]*";
    public static final String lettersPattern = "[A-Za-z]*";

    //zero or one number, zero or more spaces, zero or one dashes, zero or more spaces, zero or one parenthesis, 0 or 3 numbers
    // zero or one parenthesis, zero or more spaces, zero or one dashes, zero or more spaces, three numbers,
    // zero or more spaces, zero or one dashes, zero or more spaces, four numbers, ending whitespace

    //zero or more whitespace: \\s*
    // escape stuff by using \\ not \ since that is special in java
    //? before a pattern is zero or more of that char [0-9]? means zero or one of 0,1,2,...,8,9

    // 1 - ( 987 ) - 345 - 5634
    public static final String phoneNumberPattern = "\\s*[0-9]?\\s*[\\-]?\\s*[\\(]?\\s*[0-9]{0,3}\\s*[\\)]?\\s*[\\-]?\\s*[0-9]{3}\\s*[\\-]?\\s*[0-9]{4}\\s*";
    public static final String phoneNumberAreaCodeExtendedPattern = "";
}
