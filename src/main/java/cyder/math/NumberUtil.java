package cyder.math;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.math.BigIntegerMath;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.annotations.ForReadability;
import cyder.constants.CyderRegexPatterns;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
    public static int generateRandomInt(int lowerBound, int upperBound) {
        return ThreadLocalRandom.current().nextInt((upperBound - lowerBound) + 1) + lowerBound;
    }

    /**
     * Returns a random integer in the range [0, upperBound].
     *
     * @param upperBound the upper bound of the random range (upperBound is included from the possible values)
     * @return a random integer in the range [0, upperBound]
     */
    public static int generateRandomInt(int upperBound) {
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
        ArrayList<Integer> ret = new ArrayList<>() {
            /**
             * Adds the provided integer to the array list if it is not already contained.
             *
             * @param i the integer to add
             * @return whether the integer was added
             */
            @Override
            @CanIgnoreReturnValue
            public boolean add(Integer i) {
                if (!contains(i)) {
                    return super.add(i);
                }

                return false;
            }
        };

        while (num % 2 == 0) {
            ret.add(2);
            num /= 2;
        }

        for (int i = 3 ; i <= Math.sqrt(num) ; i += 2) {
            while (num % i == 0) {
                ret.add(i);
                num /= i;
            }
        }

        if (num > 2) {
            ret.add(num);
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
        Preconditions.checkArgument(numbers > 0);
        Preconditions.checkArgument(a >= 0);
        Preconditions.checkArgument(b >= 0);

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
     * Computes the nth number of the Fibonacci sequence.
     *
     * @param n the index of the Fibonacci number to compute
     * @return the nth number of the Fibonacci sequence
     */
    public static int computeNthFibonacci(int n) {
        Preconditions.checkArgument(n >= 0);

        int a = 0;
        int b = 1;
        int c;

        if (n == 0) return a;

        for (int i = 2 ; i <= n ; i++) {
            c = a + b;
            a = b;
            b = c;
        }

        return b;
    }

    /**
     * Computes the nth Catalan number.
     *
     * @param n the index of the Catalan number to compute
     * @return the nth Catalan number
     */
    public static int computeNthCatalan(int n) {
        Preconditions.checkArgument(n >= 0);

        return computeFactorial(2 * n).divide(computeFactorial(n).multiply(computeFactorial(n + 1))).intValue();
    }

    /**
     * Computes the factorial of the provided number.
     * Note this method does not calculate the Gamma function definition of factorial.
     * Thus, this method only works for n >= 0.
     *
     * @param n the number to compute the factorial of
     * @return the factorial of the provided number
     */
    public static BigInteger computeFactorial(int n) {
        Preconditions.checkArgument(n >= 0);

        return BigIntegerMath.factorial(n);
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

    // todo number to words util

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

    // todo number to words util

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

    // todo number to words util

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
    public static ImmutableList<Integer> generateRandomInts(int min, int max,
                                                            int numInts, boolean allowDuplicates) {
        Preconditions.checkArgument(min < max);
        Preconditions.checkArgument(numInts > 0);

        if (!allowDuplicates) {
            Preconditions.checkArgument(max - min + 1 >= numInts);
        }

        if (!allowDuplicates) {
            ArrayList<Integer> uniqueInts = new ArrayList<>(numInts);

            while (uniqueInts.size() < numInts) {
                int rand = generateRandomInt(min, max);

                if (!uniqueInts.contains(rand)) {
                    uniqueInts.add(rand);
                }
            }

            Collections.sort(uniqueInts);
            return ImmutableList.copyOf(uniqueInts);
        }

        ArrayList<Integer> ret = new ArrayList<>();
        for (int i = 0 ; i < numInts ; i++) {
            ret.add(generateRandomInt(min, max));
        }

        Collections.sort(ret);
        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns the magnitude of the vector represented by the provided values.
     *
     * @param first  the first dimensional value
     * @param others the other n dimensional values
     * @return the magnitude of the vector represented by the provided values.
     */
    public static double calculateMagnitude(double first, double... others) {
        double summedSquares = Math.pow(first, 2);
        for (double other : others) {
            summedSquares += Math.pow(other, 2);
        }

        return Math.sqrt(summedSquares);
    }

    /**
     * Finds the minimum of the provided integer array.
     *
     * @param firstInt the first integer
     * @param ints     the other integers
     * @return the minimum integer value found
     */
    public static int min(int firstInt, int... ints) {
        int min = firstInt;

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
     * @param numbers the list of numbers to find the lcm of
     * @return the lcm of the provided array
     */
    public static int lcm(List<Integer> numbers) {
        Preconditions.checkNotNull(numbers);
        Preconditions.checkArgument(!numbers.isEmpty());

        return lcmInner(numbers, 0, numbers.size());
    }

    /**
     * Helper method for finding the lcm of a list.
     *
     * @param list  the list to find the lcm of
     * @param start the starting index for the list
     * @param end   the ending index for the list
     * @return the lcm of the provided list
     */
    private static int lcmInner(List<Integer> list, int start, int end) {
        Preconditions.checkNotNull(list);
        Preconditions.checkArgument(!list.isEmpty());

        if ((end - start) == 1) {
            return lcm(list.get(start), list.get(end - 1));
        } else {
            return lcm(list.get(start), lcmInner(list, start + 1, end));
        }
    }
}
