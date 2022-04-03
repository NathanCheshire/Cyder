package cyder.constants;

import cyder.exceptions.IllegalMethodException;

import java.util.regex.Pattern;

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
     */
    public static final String commentPattern = "\\s*[/]{2}.*|\\s*[/][*].*|\\s*[*].*|\\s*.*[*][/]\\s*";

    /**
     * Regex of all invalid characters for a filename on Windows.
     */
    public static final String windowsInvalidFilenameChars = "[*?|/\":<>\\\\']+";

    /**
     * Regex for rgb color or hex color such as 00FF00 or 0,255,0.
     */
    public static final String rgbOrHex = "((\\d{1,3})|(\\d{1,3},)|(\\d{1,3},\\d{1,3})|(\\d{1,3}," +
            "\\d{1,3},)|(\\d{1,3},\\d{1,3},\\d{1,3}))|([0-9A-Fa-f]{0,6})";

    /**
     * Regex for a hex color value.
     */
    public static final String hexPattern = "[0-9A-Fa-f]{0,6}";

    /**
     * Regex for a rgb color value.
     */
    public static final String rgbPattern = "((\\d{1,3})|(\\d{1,3},)|(\\d{1,3},\\d{1,3})|(\\d{1,3},\\d{1,3},)" +
            "|(\\d{1,3},\\d{1,3},\\d{1,3}))";

    /**
     * The pattern for matching carriage returns.
     */
    public static final Pattern newLinePattern = Pattern.compile("\\R");

    /**
     * Prevent illegal class instantiation.
     */
    private CyderRegexPatterns() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * The pattern used to grab the youtube-dl progress from the process.
     */
    public static final Pattern updatePattern  = Pattern.compile(
            "\\s*\\[download]\\s*([0-9]{1,3}.[0-9]%)\\s*of\\s*([0-9A-Za-z.]+)" +
                    "\\s*at\\s*([0-9A-Za-z./]+)\\s*ETA\\s*([0-9:]+)");

    /**
     * The pattern used to scrape the youtube uuids returned from the youtube api v3 instead
     * of using JSON serialization via GSON.
     */
    public static final Pattern youtubeApiV3UuidPattern = Pattern.compile(
            "\"resourceId\":\\s*\\{\\s*\n\\s*\"kind\":\\s*\"youtube#video\",\\s*\n\\s*\"" +
                    "videoId\":\\s*\"(.*)\"\\s*\n\\s*},");

    /**
     * The pattern sued to webscrape the isp from a google search.
     */
    public static final Pattern whereAmIPattern = Pattern.compile("^\\s*<p class=\"isp\">(.*)</p>\\s*$");

    /**
     * The pattern used to extract the uuid from a youtube video.
     */
    public static final Pattern extractYoutubeUuidPattern
            = Pattern.compile("(?<=youtu.be/|watch\\?v=|/videos/|embed/)[^#&?]*");

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

    /**
     * The pattern used to validate whether a Url is constructed properly.
     */
    public static final Pattern urlFormationPattern = Pattern.compile("\\b(?:(https?|ftp|file)://" +
            "|www\\.)?[-A-Z0-9+&#/%?=~_|$!:,.;]*[A-Z0-9+&@#/%=~_|$]\\.[-A-Z0-9+&@#/%?=~_|$!:,.;]*[A-Z0-9+&@#/%=~_|$]",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
}
