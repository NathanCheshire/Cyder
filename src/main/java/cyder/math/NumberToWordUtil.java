package cyder.math;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import cyder.constants.CyderRegexPatterns;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utilities for converting a number to it's word form.
 */
public final class NumberToWordUtil {
    /**
     * The character to denote a negative number.
     */
    private static final String negativeChar = CyderStrings.dash;

    /**
     * The regex for determining if an input is equal to zero
     */
    private static final String zeroRegex = "\\s*-?\\s*[0]+\\s*";

    /**
     * The regex for splitting a string into groups of three.
     */
    private static final String groupsOfThreeDigitsRegex = "(?<=\\G...)";

    /**
     * The odd range for most language system's numbers. Words in this range
     * must commonly be memorized in Latin based languages.
     */
    private static final Range<Integer> tenToTwentyRange = Range.closedOpen(10, 20);

    /**
     * String representations for all digits in the one's place.
     */
    private static final ImmutableList<String> onesPlaceStrings = ImmutableList.of(
            "",
            "one",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine"
    );

    /**
     * String representations for all digits in the ten's place in base 10.
     */
    private static final ImmutableList<String> tensPlaceStrings = ImmutableList.of(
            "",
            "",
            "twenty",
            "thirty",
            "forty",
            "fifty",
            "sixty",
            "seventy",
            "eighty",
            "ninety"
    );

    /**
     * String representations for numbers in {@link #tenToTwentyRange}.
     */
    private static final ImmutableList<String> tenToTwentyStrings = ImmutableList.of(
            "ten",
            "eleven",
            "twelve",
            "thirteen",
            "fourteen",
            "fifteen",
            "sixteen",
            "seventeen",
            "eighteen",
            "nineteen"
    );

    /**
     * String prefixes for digit trios in base 10.
     */
    private static final ImmutableList<String> thousandPrefixes = ImmutableList.of(
            "",
            "-thousand",
            "-million",
            "-billion",
            "-trillion",
            "-quadrillion",
            "-quintillion",
            "-sextillion",
            "-septillion",
            "-octillion",
            "-nonillion",
            "-decillion",
            "-undecillion",
            "-duodecillion",
            "-tredecillion",
            "-quattuordecillion",
            "-quindecillion",
            "-sexdexillion",
            "-septendecillion",
            "-octodecillion",
            "-novemdecillion",
            "-vigintillion",
            "-centillion"
    );

    /**
     * The pattern used to determine if user input is valid for the to words method.
     */
    private static final Pattern validUserInputPattern = Pattern.compile("\\s*-?" + CyderRegexPatterns.numberPattern);

    /**
     * The range a trio must be in.
     */
    private static final Range<Integer> THOUSAND_RANGE = Range.closedOpen(0, 1000);

    /**
     * Suppress default constructor.
     */
    private NumberToWordUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the string representation for the provided integer.
     *
     * @param userInput the number of find a string representation for
     * @return the string representation for the provided integer
     */
    public static String toWords(final String userInput) {
        Preconditions.checkNotNull(userInput);
        Preconditions.checkArgument(!userInput.isEmpty());
        Preconditions.checkArgument(validUserInputPattern.matcher(userInput).matches());

        boolean negative = userInput.contains(negativeChar);
        if (userInput.matches(zeroRegex)) return "Zero";

        String positiveUserInput = userInput.replace(negativeChar, "");

        int trioLength = 3;
        StringBuilder paddingPositiveUserInputBuilder = new StringBuilder(positiveUserInput);
        while (paddingPositiveUserInputBuilder.length() % trioLength != 0) {
            paddingPositiveUserInputBuilder.insert(0, 0);
        }

        String paddedPositiveUserInput = paddingPositiveUserInputBuilder.toString();
        ImmutableList<String> paddedUserInputTrioStrings = ImmutableList.copyOf(
                paddedPositiveUserInput.split(groupsOfThreeDigitsRegex));

        ImmutableList<String> reversedComputedTrios = ImmutableList.copyOf(paddedUserInputTrioStrings.stream()
                        .map(trioString -> trioToWords(Integer.parseInt(trioString)))
                        .collect(Collectors.toList()))
                .reverse();

        ArrayList<String> prefixedTrios = new ArrayList<>();
        IntStream.range(0, reversedComputedTrios.size()).forEach(i -> {
            boolean firstAndReversedSizeGreaterThanOne = i == 0 && reversedComputedTrios.size() > 1;
            boolean secondToLastDigitIsZero = paddedUserInputTrioStrings
                    .get(paddedUserInputTrioStrings.size() - 1).charAt(1) == '0';
            boolean lastDigitNotZero = paddedUserInputTrioStrings
                    .get(paddedUserInputTrioStrings.size() - 1).charAt(2) != '0';

            boolean conditionsMet = firstAndReversedSizeGreaterThanOne
                    && secondToLastDigitIsZero && lastDigitNotZero;

            String prefix = conditionsMet ? " and " : "";

            prefixedTrios.add(prefix + reversedComputedTrios.get(i) + thousandPrefixes.get(i));
        });

        StringBuilder wordBuilder = new StringBuilder(negative ? "Negative " : "");

        IntStream.range(0, prefixedTrios.size()).forEach(index ->
                wordBuilder.append(prefixedTrios.get(prefixedTrios.size() - index - 1).trim())
                        .append(CyderStrings.space));

        return StringUtil.capsFirstWords(StringUtil.getTrimmedText(wordBuilder.toString()));
    }

    /**
     * Returns the word representation for a three digit number in base 10.
     * For example, providing 123 will return "one-hundred twenty three".
     *
     * @param number the number to get a word representation for
     * @return the word representation for the provided trio of base 10 digits
     */
    private static String trioToWords(int number) {
        Preconditions.checkArgument(THOUSAND_RANGE.contains(number));

        int onesDigit = number % 10;
        int tensDigit = (number % 100) / 10;
        int hundredsDigit = number / 100;

        int onesAndTensDigits = onesDigit + 10 * tensDigit;

        String hundredsDigitString = onesPlaceStrings.get(hundredsDigit);
        String hundredsPlaceString = "";
        if (!StringUtil.isNullOrEmpty(hundredsDigitString)) {
            hundredsPlaceString = hundredsDigitString + CyderStrings.space + "hundred";
        }

        String onesAndTensString = tensPlaceStrings.get(tensDigit)
                + CyderStrings.space + onesPlaceStrings.get(onesDigit);
        if (tenToTwentyRange.contains(onesAndTensDigits)) {
            onesAndTensString = tenToTwentyStrings.get(onesAndTensDigits - tenToTwentyStrings.size());
        }

        return StringUtil.capsFirstWords(
                StringUtil.getTrimmedText((hundredsPlaceString + CyderStrings.space + onesAndTensString)));
    }
}
