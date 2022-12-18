package cyder.math;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import cyder.annotations.ForReadability;
import cyder.constants.CyderRegexPatterns;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * A util for working with numbers and not necessarily math.
 */
public final class NumberUtil {
    /**
     * The map of the base 10 limits of one, two, and three bytes.
     */
    public static final ImmutableMap<Integer, Integer> BIT_LIMITS = ImmutableMap.of(
            8, 255,
            16, 65535,
            24, 16777215
    );

    /**
     * Suppress default constructor.
     */
    private NumberUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns a random integer in the range [min, upperBound].
     *
     * @param lowerBound the minimum possible value to return (must be at least 0)
     * @param upperBound the upper bound of random range (included in the possible return values)
     * @return a random integer in the provided range [0, upperBound]
     */
    public static int randInt(int lowerBound, int upperBound) {
        return ThreadLocalRandom.current().nextInt((upperBound - lowerBound) + 1) + lowerBound;
    }

    /**
     * Returns a random integer in the range [0, upperBound].
     *
     * @param upperBound the upper bound of the random range (upperBound is included from the possible values)
     * @return a random integer in the range [0, upperBound]
     */
    public static int randInt(int upperBound) {
        return ThreadLocalRandom.current().nextInt((upperBound) + 1);
    }

    /**
     * Determines if the provided number if a prime.
     *
     * @param num the possibly prime number to validate
     * @return whether the provided number was a prime
     */
    public static boolean isPrime(int num) {
        boolean ret = true;

        for (int i = 2 ; i < Math.ceil(Math.sqrt(num)) ; i++)
            if (num % i == 0) {
                ret = false;
                break;
            }

        return ret;
    }

    /**
     * Returns a list of the prime factors of the provided integer.
     * If the provided integer is a prime, 1 and num are returned.
     *
     * @param num the number to find prime factors of
     * @return a list of prime factors of num
     */
    public static ImmutableList<Integer> primeFactors(int num) {
        ArrayList<Integer> ret = new ArrayList<>();

        for (int i = 2 ; i < Math.ceil(Math.sqrt(num)) ; i++) {
            if (num % i == 0) {
                ret.add(i);
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Calculates the fibonacci sequence using the initial values.
     *
     * @param a       the first fibonacci number to use
     * @param b       the second fibonacci number to use
     * @param numbers the number of fibonacci numbers to return
     * @return the requested number of fibonacci numbers
     */
    public static ImmutableList<Long> computeFibonacci(long a, long b, int numbers) {
        LinkedList<Long> ret = new LinkedList<>();
        ret.add(a);

        for (int i = 1 ; i < numbers ; i++) {
            ret.add(b);

            long next = a + b;
            a = b;
            b = next;
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns the string representation for the provided integer.
     *
     * @param num the number of find a string representation for
     * @return the string representation for the provided integer
     */
    public static String toWords(int num) {
        return toWords(String.valueOf(num));
    }

    /**
     * The negative string.
     */
    private static final String NEGATIVE = "Negative";

    /**
     * The zero string.
     */
    private static final String ZERO = "Zero";

    /**
     * The character to denote a negative number.
     */
    private static final String NEGATIVE_CHAR = CyderStrings.dash;

    /**
     * Returns the string representation for the provided raw text field input straight from a user.
     *
     * @param word the result of calling textField.getText() on (an integer in the form of a String)
     * @return the string representation for the provided raw text field input
     */
    public static String toWords(String word) {
        Preconditions.checkNotNull(word);
        Preconditions.checkArgument(!word.isEmpty());
        if (word.contains(NEGATIVE_CHAR)) Preconditions.checkArgument(word.startsWith(NEGATIVE_CHAR));

        boolean negative = false;
        try {
            BigInteger num = new BigInteger(word);

            if (num.compareTo(BigInteger.ZERO) == 0) return ZERO;

            negative = num.compareTo(BigInteger.ZERO) < 0;
        } catch (Exception ignored) {
            // word not parsable by BigInteger
        }

        word = word.replace(NEGATIVE_CHAR, "");

        StringBuilder wordRepBuilder = new StringBuilder(word);
        while (wordRepBuilder.length() % 3 != 0) {
            wordRepBuilder.insert(0, 0);
        }
        word = wordRepBuilder.toString();

        ImmutableList<String> baseTrios = splitToTrios(word);
        ArrayList<String> trioStrings = new ArrayList<>(baseTrios.size());
        baseTrios.forEach(trio -> trioStrings.add(trioToWords(Integer.parseInt(trio))));

        ArrayList<String> reversed = new ArrayList<>(trioStrings.size());
        trioStrings.forEach(trioString -> reversed.add(0, trioString));
        trioStrings.clear();

        for (int i = 0 ; i < reversed.size() ; i++) {
            boolean firstAndReversedSizeGreaterThanOne = i == 0 && reversed.size() > 1;
            boolean secondToLastDigitIsZero = baseTrios.get(baseTrios.size() - 1).charAt(1) == '0';
            boolean lastDigitNotZero = baseTrios.get(baseTrios.size() - 1).charAt(2) != '0';
            trioStrings.add((firstAndReversedSizeGreaterThanOne && secondToLastDigitIsZero && lastDigitNotZero
                    ? " and " : "") + reversed.get(i) + getThousandsPrefix(i));
        }

        StringBuilder wordFormBuilder = new StringBuilder();
        if (negative) wordFormBuilder.append(NEGATIVE).append(CyderStrings.space);

        int trioStringsSize = trioStrings.size();
        IntStream.range(0, trioStringsSize).forEach(index -> {
            wordFormBuilder.append(trioStrings.get(trioStringsSize - index - 1).trim());
            wordFormBuilder.append(CyderStrings.space);
        });

        return wordFormBuilder.toString().trim();
    }

    @ForReadability
    private static ImmutableList<String> splitToTrios(String combinedWord) {
        String[] trios = combinedWord.split("(?<=\\G...)");
        ArrayList<String> ret = new ArrayList<>(trios.length);

        for (String trio : trios) {
            ret.add(trio.replaceAll(CyderRegexPatterns.whiteSpaceRegex, "")
                    .replace(CyderStrings.openingBracket, "")
                    .replace(CyderStrings.closingBracket, ""));
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * The range a trio must be in.
     */
    private static final Range<Integer> THOUSAND_RANGE = Range.closedOpen(0, 1000);

    /**
     * The hundreds word.
     */
    private static final String HUNDRED = "hundred";

    /**
     * Returns the word representation for a trio of base 10 digits.
     * Example: 123 will return "one-hundred twenty three"
     *
     * @param num the number to get a word representation for
     * @return the word representation for the provided trio of base 10 digits
     */
    private static String trioToWords(int num) {
        Preconditions.checkArgument(THOUSAND_RANGE.contains(num));

        int onesDigit = num % 10;
        int tensDigit = (num % 100) / 10;
        int onesAndTensNumber = onesDigit + 10 * tensDigit;
        int hundredsDigit = num / 100;

        String hundredsDigitString = ONES_STRINGS.get(hundredsDigit);
        String hundredsString = StringUtil.isNullOrEmpty(hundredsDigitString)
                ? "" : hundredsDigitString + CyderStrings.space + HUNDRED;

        String belowOneHundredString;
        belowOneHundredString = TEENS_RANGE.contains(onesAndTensNumber)
                ? TEEN_STRINGS.get(onesAndTensNumber - 10)
                : TENS_STRINGS.get(tensDigit) + CyderStrings.space + ONES_STRINGS.get(onesDigit);

        return StringUtil.getTrimmedText((hundredsString + CyderStrings.space + belowOneHundredString));
    }

    /**
     * The range for an integer to be in to be in the teen range.
     */
    private static final Range<Integer> TEENS_RANGE = Range.closedOpen(10, 20);

    /**
     * String representations for all digits in the one's place.
     */
    private static final ImmutableList<String> ONES_STRINGS = ImmutableList.of(
            "",
            "one",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine");

    /**
     * Returns the word representation for any digit in the inclusive range [0, 9].
     *
     * @param num the number to get a word representation for
     * @return the word representation for any digit in the inclusive range [0, 9]
     */
    private static String getOnesPlaceWord(int num) {
        Preconditions.checkArgument(num >= 0 && num <= 9);

        return ONES_STRINGS.get(num);
    }

    /**
     * String representations for all digits in the ten's place in base 10.
     */
    private static final ImmutableList<String> TENS_STRINGS = ImmutableList.of(
            "",
            "",
            "twenty",
            "thirty",
            "forty",
            "fifty",
            "sixty",
            "seventy",
            "eighty",
            "ninety");

    /**
     * String representations for numbers in the range [10, 19].
     */
    private static final ImmutableList<String> TEEN_STRINGS = ImmutableList.of(
            "ten",
            "eleven",
            "twelve",
            "thirteen",
            "fourteen",
            "fifteen",
            "sixteen",
            "seventeen",
            "eighteen",
            "nineteen");

    /**
     * String prefixes for digit trios in base 10.
     */
    private static final ImmutableList<String> THOUSAND_PREFIXES = ImmutableList.of(
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
            "-centillion");

    /**
     * Returns the prefix associated with the place of a trio of digits in base 10.
     * Example: 1 will return "-thousand" and 3 will return "-billion"
     *
     * @param trioPlace the place of the trio in its parent number
     * @return the prefix associated with the palace of a trio of digits in base 10
     */
    private static String getThousandsPrefix(int trioPlace) {
        Preconditions.checkArgument(trioPlace >= 0 && trioPlace < THOUSAND_PREFIXES.size());

        return THOUSAND_PREFIXES.get(trioPlace);
    }

    /**
     * Returns the requested amount of random numbers within the provided range.
     *
     * @param min             the minimum random number possible
     * @param max             the maximum random number possible
     * @param numInts         the number of random elements desired
     * @param allowDuplicates allow duplicate random values for a pure random experience vs unique random elements
     * @return an array of ints of the desired size of random elements from min to max
     */
    public static ImmutableList<Integer> randInt(int min, int max, int numInts, boolean allowDuplicates) {
        if (allowDuplicates) {
            Preconditions.checkArgument(max - min >= numInts);
        }

        if (!allowDuplicates) {
            ArrayList<Integer> uniqueInts = new ArrayList<>(numInts);

            while (uniqueInts.size() < numInts) {
                int rand = randInt(min, max);

                if (!uniqueInts.contains(rand)) {
                    uniqueInts.add(rand);
                }
            }

            return ImmutableList.copyOf(uniqueInts);
        }

        ArrayList<Integer> ret = new ArrayList<>();
        for (int i = 0 ; i < numInts ; i++) {
            ret.add(randInt(min, max));
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns the magnitude of the vector represented by the provided values.
     *
     * @param first  the first dimensional value
     * @param second the second dimensional value
     * @param others the other n dimensional values
     * @return the magnitude of the vector represented by the provided values.
     */
    public static double calculateMagnitude(double first, double second, double... others) {
        double summedSquares = Math.pow(first, 2) + Math.pow(second, 2);
        for (double other : others) {
            summedSquares += Math.pow(other, 2);
        }

        return Math.sqrt(summedSquares);
    }

    /**
     * Finds the minimum of the provided integer array.
     *
     * @param ints the array of ints
     * @return the minimum integer value found
     */
    public static int min(int... ints) {
        int min = Integer.MAX_VALUE;

        for (int i : ints) {
            if (i < min) {
                min = i;
            }
        }

        return min;
    }

    /**
     * Finds the greatest common divisor of the provided integers.
     *
     * @param first  the first integer
     * @param second the second integer
     * @return the greatest common divisor of the provided integers
     */
    public static int gcd(int first, int second) {
        if (first < second) {
            return gcd(second, first);
        }
        if (first % second == 0) {
            return second;
        } else {
            return gcd(second, first % second);
        }
    }

    /**
     * Finds the least common multiple of the provided integers.
     *
     * @param first  the first integer
     * @param second the second integer
     * @return the least common multiple of the provided integers
     */
    public static int lcm(int first, int second) {
        return ((first * second) / gcd(first, second));
    }

    /**
     * Finds the lcm of the provided array.
     *
     * @param array the array to find the lcm of
     * @return the lcm of the provided array
     */
    public static int lcmArray(int[] array) {
        Preconditions.checkNotNull(array);

        return lcmArrayInner(array, 0, array.length);
    }

    /**
     * Helper method for finding the lcm of an Array.
     *
     * @param array the array to find the lcm of
     * @param start the starting index for the array
     * @param end   the ending index for the array
     * @return the lcm of the provided array
     */
    @ForReadability
    private static int lcmArrayInner(int[] array, int start, int end) {
        Preconditions.checkNotNull(array);

        if ((end - start) == 1) {
            return lcm(array[start], array[end - 1]);
        } else {
            return lcm(array[start], lcmArrayInner(array, start + 1, end));
        }
    }
}
