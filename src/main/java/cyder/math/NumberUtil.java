package cyder.math;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.math.BigIntegerMath;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.utils.ArrayUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
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

        ArrayList<Long> ret = new ArrayList<>();
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

    /**
     * Finds the least common multiple of the provided integers.
     *
     * @param first  the first integer
     * @param second the second integer
     * @return the least common multiple of the provided integers
     */
    private static int lcm(int first, int second) {
        return ((first * second) / gcd(first, second));
    }

    /**
     * Returns a random integer in the range of [min, max] guaranteed not to be one of the provided ignore values.
     *
     * @param min          the minimum possible value
     * @param max          the maximum possible value
     * @param ignoreValues the values which should be excluded which fall within the range
     * @return a random index in the range [min, max] but not one of the ignore values
     */
    public static int getRandomIndex(int min, int max, int... ignoreValues) {
        Preconditions.checkArgument(min < max);
        Preconditions.checkArgument(max - min + 1 > ignoreValues.length);

        ImmutableList<Integer> ignoreInts = ArrayUtil.toList(ignoreValues);
        ArrayList<Integer> possibleInts = IntStream.rangeClosed(min, max)
                .filter(value -> !ignoreInts.contains(value))
                .boxed().collect(Collectors.toCollection(ArrayList::new));
        return ArrayUtil.getRandomElement(possibleInts);
    }
}
