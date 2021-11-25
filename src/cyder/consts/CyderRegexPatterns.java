package cyder.consts;

public class CyderRegexPatterns {
    public static final String ipv4Pattern = "\\s*[0-9]{1,3}(\\s*|\\.[0-9]{1,3}\\s*|\\.[0-9]{1,3}\\.[0-9]{1,3}\\s*|\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\s*)";
    public static final String numberPattern = "[0-9]*";
    public static final String lettersPattern = "[A-Za-z]*";

    public static final String phoneNumberPattern = "\\s*[0-9]?\\s*[\\-]?\\s*[\\(]?\\s*[0-9]{0,3}\\s*[\\)]?\\s*[\\-]?\\s*[0-9]{3}\\s*[\\-]?\\s*[0-9]{4}\\s*";
    public static final String phoneNumberAreaCodeExtendedPattern = "\\s*[0-9]{0,2}\\s*[\\-]?\\s*[\\(]?\\s*[0-9]{0,3}\\s*[\\)]?\\s*[\\-]?\\s*[0-9]{3}\\s*[\\-]?\\s*[0-9]{4}\\s*";
}
