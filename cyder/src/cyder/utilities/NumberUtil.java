package cyder.utilities;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import static cyder.constants.CyderNumbers.INFINITY;
import static cyder.constants.CyderNumbers.NEG_INFINITY;

/**
 * A common Number methods class such as generating random
 * numbers or converting integers to their String representations
 */
public class NumberUtil {
    /**
     * Instantiation of NumberUtil is not allowed
     */
    private NumberUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Returns a random integer in the range [min, upperBound].
     *
     * @param min the minimum possible value to return (must be at least 0)
     * @param upperBound the upper bound of random range (included in the possible return values)
     * @return a random integer in the provided range [0, upperBound]
     */
    public static int randInt(int min, int upperBound) {
        return new Random().nextInt((upperBound - min) + 1) + min;
    }

    /**
     * Returns a random integer in the range [0, upperBound).
     *
     * @param upperBound the upper bound of the random range (upperBound is exluded from the possible values)
     * @return a random integer in the range [0, upperBound)
     */
    public static int randInt(int upperBound) {
        return new Random().nextInt((upperBound) + 1);
    }

    /**
     * Determines if the provided number if a prime.
     *
     * @param num the possibly prime number to validate
     * @return whether or not the provided number was a prime
     */
    public static boolean isPrime(int num) {
        boolean ret = true;

        for (int i = 2; i < Math.ceil(Math.sqrt(num)); i += 1)
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
    public static ArrayList<Integer> primeFactors(int num) {
        if (isPrime(num)) {
            ArrayList<Integer> numbers = new ArrayList<>();
            numbers.add(1);
            numbers.add(num);
            return numbers;
        } else {
            ArrayList<Integer> numbers = new ArrayList<>();

            for (int i = 2; i < Math.ceil(Math.sqrt(num)); i += 1) {
                if (num % i == 0) {
                    numbers.add(i);
                }
            }

            return numbers;
        }
    }

    /**
     * Calculates the fibonacci sequence given the initial values.
     *
     * @param a the first fibonacci number to use
     * @param b the second fibonacci number to use
     * @param numFibs the number of fibonacci numbers to return
     * @return the requested number of fibonacci numbers
     */
    public static LinkedList<Long> fib(long a, long b, int numFibs) {
        LinkedList<Long> ret = new LinkedList<>();
        ret.add(a);
        for (int i = 1; i < numFibs; i++) {
            ret.add(b);

            long next = a + b;

            a = b;
            b = next;
        }

        return ret;
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
     * @param wordRep the result of calling textField.getText() on (an integer in the form of a String)
     * @return the string representation for the provided raw text field input
     */
    public static String toWords(String wordRep) {
        //check for invalid input
        if (wordRep == null || wordRep.isEmpty())
            return "ERROR";

        //convert to a big interger
        BigInteger num = new BigInteger(wordRep);

        //check for zero
        if (num.compareTo(BigInteger.ZERO) == 0) {
            return "Zero";
        }

        //check for signage
        boolean negative = num.compareTo(BigInteger.ZERO) < 0;

        //pad with 0's so that we have all trios
        StringBuilder wordRepBuilder = new StringBuilder(wordRep.replace("-", ""));
        while (wordRepBuilder.length() % 3 != 0)
            wordRepBuilder.insert(0, "0");
        wordRep = wordRepBuilder.toString();

        //now split into trios array
        String[] baseTrios = java.util.Arrays.toString(wordRep.split("(?<=\\G...)"))
                .replace("[", "").replace("]", "")
                .replace(" ", "").split(",");

        //list of trios converted to strings
        LinkedList<String> trioStrings = new LinkedList<>();

        //convert all trios to strings
        for (String trio : baseTrios)
            trioStrings.add(trioToWords(Integer.parseInt(trio)));

        //reverse the list to build our number with correct prefixes
        LinkedList<String> reversed = new LinkedList<>();

        for (String str : trioStrings)
            reversed.push(str);

        //clear the list so that we can add back to it
        trioStrings.clear();

        //for all trios get the prefix and add and to the last prefix (first since reversed)
        for (int i = 0 ; i < reversed.size() ; i++) {
             trioStrings.add((i == 0  && reversed.size() > 1 ? " and " : "") + reversed.get(i) + getThousandsPrefix(i));
        }

        StringBuilder ret = new StringBuilder();

        if (negative)
            ret.append("Negative ");

        for (int i = trioStrings.size() - 1 ; i > -1 ; i--) {
            ret.append(trioStrings.get(i).trim());

            if (i != 0)
                ret.append(" ");
        }

        return ret.toString();
    }

    /**
     * Returns the word representation for a trio of base 10 digits.
     * Example: 123 will return "one-hundred twenty three"
     *
     * @param num the number to get a word representation for
     * @return the word representation for the provided trio of base 10 digits.
     */
    private static String trioToWords(int num) {
        if (num < 0 || num > 999)
            throw new IllegalArgumentException("Provided number is not in the required range of [0, 999]");

        int ones = num % 10;
        int tens = (num % 100) / 10;

        int below100 = ones + tens * 10;

        int hundreds = num / 100;

        String hundredsStr = (onesPlace[hundreds].equals("") ? "" : onesPlace[hundreds] + " hundred");
        String below100Str;

        if (below100 < 20 && below100 > 9) {
            below100Str = teens[below100 - 10].trim();
        } else {
            below100Str = (tensPlace[tens].trim() + " " + onesPlace[ones].trim()).trim();
        }

        return (hundredsStr + " " + below100Str).trim();
    }

    /**
     * String representations for all digits in the one's place
     */
    private static final String[] onesPlace = {"", "one", "two", "three", "four",
            "five", "six", "seven", "eight", "nine"};

    /**
     * Returns the word representation for any digit in the inclusive range [0, 9].
     *
     * @param num the number to get a word representation for
     * @return the word representation for any digit in the inclusive range [0, 9]
     */
    private static String wordForOnes(int num) {
        return onesPlace[num];
    }

    /**
     * String representations for all digits in the ten's place in base 10
     */
    private static final String[] tensPlace = {"", "", "twenty", "thirty", "fourty",
            "fifty", "sixty", "seventy", "eighty", "ninety"};

    /**
     * Returns the word representation for any digit in the ten's place in base 10.
     * Example: 0 and 1 return nothing whilst 9 returns ninety.
     *
     * @param num the number to get a word represenation for provided the number is in the ten's place
     * @return the word representation for the number in the ten's place in base 10
     */
    private static String wordForTens(int num) {
        return tensPlace[num];
    }

    /**
     * String representations for numbers in the range [10, 19]
     */
    private static final String[] teens = {"ten", "eleven", "twelve", "thirteen",
            "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};

    /**
     * Returns the word representation for a number in the inclusive range [10, 19].
     *
     * @param num the number to find a word representation of
     * @return the word representation for a number in the inclusive range [10, 19]
     */
    private static String wordForTeenNums(int num) {
        return teens[num - 10];
    }

    /**
     * String prefixes for digit trios in base 10
     */
    private static final String[] thousandPrefixes = {"", "-thousand", "-million", "-billion", "-trillion", "-quadrillion",
            "-quintillion", "-sextillion", "-septillion", "-octillion", "-nonillion",
            "-decillion", "-undecillion", "-duodecillion", "-tredecillion",
            "-quattuordecillion", "-quindecillion", "-sexdexillion", "-septendecillion",
            "-octodecillion", "-novemdecillion", "-vigintillion", "-centillion"};

    /**
     * Returns the prefix associated with the place of a trio of digits in base 10.
     * Example: 1 will return "-thousand" and 3 will return "-billion"
     *
     * @param trioPlace the place of the trio in its parent number
     * @return the prefix associated with the palce of a trio of digits in base 10
     */
    private static String getThousandsPrefix(int trioPlace) {
        return thousandPrefixes[trioPlace];
    }

    /**
     * Returns the requested amount of random numbers within the provided range.
     *
     * @param min the minimum random number possible
     * @param max the maximum random number possible
     * @param number the number of random elements desired
     * @param allowDuplicates allow duplicate random values for a pure random experience vs unique random elements
     * @return an array of ints of the desired size of random elements from min to max
     */
    public static int[] randInt(int min, int max, int number, boolean allowDuplicates) {
        if (max - min < number && !allowDuplicates)
            throw new IllegalArgumentException("Desired number of random elements cannot be met with provided range.");
        int[] ret = new int[number];

        if (!allowDuplicates) {
            LinkedList<Integer> uniqueInts = new LinkedList<>();

            while (uniqueInts.size() < number) {
                int rand = randInt(min, max);
                if (!uniqueInts.contains(rand)) {
                    uniqueInts.add(rand);
                }
            }

            for (int i = 0 ; i < uniqueInts.size() ; i++) {
                ret[i] = uniqueInts.get(i);
            }
        } else {
            for (int i = 0 ; i < number ; i++) {
                ret[i] = randInt(min,max);
            }
        }

        return ret;
    }

    /**
     * Adds the integers together if they do not overflow the maximum integer value.
     * If they do, returns positive infinity.
     *
     * @param a the first integer to add
     * @param b the second integer to add
     * @return the result of adding a to b guaranteed to not overflow
     */
    public static int addWithoutOverflow(int a, int b) {
        //convert to longs so that addition is guaranteed to work for integers
        long sum = (long) a + (long) b;

        //check bounds of sum and return correct value
        return (sum > INFINITY ? INFINITY : a + b);
    }

    /**
     * Subtracts the subtrahend from the minuend and returns the result guaranteed to not have underflowed.
     * If underflow does occur, NEG_INFINITY is returned.
     *
     * @param a minuend
     * @param b the subtrahend (value to subtract from a)
     * @return the result of subtracting b from a guaranteed to not underflow
     */
    public static int subtractWithoutUnderflow(int a, int b) {
        //convert to longs so that subtraction is guaranteed to work for integers
        long difference  = (long) a - (long) b;

        //check bounds of difference and return correct result
        return (difference < NEG_INFINITY ? NEG_INFINITY : a - b);
    }
}
