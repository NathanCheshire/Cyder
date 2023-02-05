package cyder.math

import com.google.common.collect.ImmutableList
import com.google.common.collect.Range
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.stream.IntStream
import kotlin.math.sqrt

/**
 * Tests for [NumberUtil]s.
 */
class NumberUtilTest {
    /**
     * Tests for the is prime method.
     */
    @Test
    fun testIsPrime() {
        assertTrue(NumberUtil.isPrime(0))
        assertTrue(NumberUtil.isPrime(1))
        assertTrue(NumberUtil.isPrime(2))
        assertTrue(NumberUtil.isPrime(5))
        assertTrue(NumberUtil.isPrime(7))
        assertTrue(NumberUtil.isPrime(11))
        assertTrue(NumberUtil.isPrime(41))
        assertTrue(NumberUtil.isPrime(1193))
        assertTrue(NumberUtil.isPrime(1783))
        assertTrue(NumberUtil.isPrime(6113))
        assertTrue(NumberUtil.isPrime(13441))

        assertFalse(NumberUtil.isPrime(40))
        assertFalse(NumberUtil.isPrime(80))
        assertFalse(NumberUtil.isPrime(180))
    }

    /**
     * Tests for the prime factors method.
     */
    @Test
    fun testPrimeFactors() {
        assertEquals(ImmutableList.of(2), NumberUtil.primeFactors(8))
        assertEquals(ImmutableList.of(2), NumberUtil.primeFactors(16))
        assertEquals(ImmutableList.of(3, 5), NumberUtil.primeFactors(15))
        assertEquals(ImmutableList.of(3, 5, 7), NumberUtil.primeFactors(315))
        assertEquals(ImmutableList.of(2, 5), NumberUtil.primeFactors(1000))
    }

    /**
     * Tests for the compute fibonacci method.
     */
    @Test
    fun testComputeFibonacci() {
        assertThrows(IllegalArgumentException::class.java) { NumberUtil.computeFibonacci(0, 0, 0) }
        assertThrows(IllegalArgumentException::class.java) { NumberUtil.computeFibonacci(-1, 0, 1) }
        assertThrows(IllegalArgumentException::class.java) { NumberUtil.computeFibonacci(0, -1, 1) }
        assertThrows(IllegalArgumentException::class.java) { NumberUtil.computeFibonacci(-1, -1, 1) }

        assertTrue(ImmutableList.of(0L, 1L, 1L, 2L, 3L, 5L).equals(NumberUtil.computeFibonacci(0, 1, 6)))
        assertEquals(ImmutableList.of(0L, 1L, 1L, 2L, 3L, 5L, 8L, 13L, 21L, 34L),
                NumberUtil.computeFibonacci(0, 1, 10))
    }

    /**
     * Tests for the compute nth fibonacci method.
     */
    @Test
    fun testComputeNthFibonacci() {
        assertThrows(IllegalArgumentException::class.java) { NumberUtil.computeNthFibonacci(-1) }

        assertEquals(0, NumberUtil.computeNthFibonacci(0))
        assertEquals(1, NumberUtil.computeNthFibonacci(1))
        assertEquals(1, NumberUtil.computeNthFibonacci(2))
        assertEquals(2, NumberUtil.computeNthFibonacci(3))
        assertEquals(3, NumberUtil.computeNthFibonacci(4))
        assertEquals(5, NumberUtil.computeNthFibonacci(5))
        assertEquals(55, NumberUtil.computeNthFibonacci(10))
        assertEquals(6765, NumberUtil.computeNthFibonacci(20))
        assertEquals(832040, NumberUtil.computeNthFibonacci(30))
    }

    /**
     * Tests for the compute nth catalan method.
     */
    @Test
    fun testComputeNthCatalan() {
        assertThrows(IllegalArgumentException::class.java) { NumberUtil.computeNthCatalan(-1) }

        assertEquals(1, NumberUtil.computeNthCatalan(0))
        assertEquals(1, NumberUtil.computeNthCatalan(1))
        assertEquals(2, NumberUtil.computeNthCatalan(2))
        assertEquals(5, NumberUtil.computeNthCatalan(3))
        assertEquals(14, NumberUtil.computeNthCatalan(4))
        assertEquals(42, NumberUtil.computeNthCatalan(5))
        assertEquals(16796, NumberUtil.computeNthCatalan(10))
    }

    /**
     * Tests for the compute factorial method.
     */
    @Test
    fun testComputeFactorial() {
        assertThrows(IllegalArgumentException::class.java) { NumberUtil.computeFactorial(-1) }

        assertEquals(1, NumberUtil.computeFactorial(0).toInt())
        assertEquals(1, NumberUtil.computeFactorial(1).toInt())
        assertEquals(2, NumberUtil.computeFactorial(2).toInt())
        assertEquals(6, NumberUtil.computeFactorial(3).toInt())
        assertEquals(24, NumberUtil.computeFactorial(4).toInt())
        assertEquals(120, NumberUtil.computeFactorial(5).toInt())
    }

    /**
     * Tests for the generate random ints method.
     */
    @Test
    fun testGenerateRandomInts() {
        assertThrows(IllegalArgumentException::class.java) {
            NumberUtil.generateRandomInts(0, 0, 5, false)
        }
        assertThrows(IllegalArgumentException::class.java) {
            NumberUtil.generateRandomInts(0, 5, 0, false)
        }
        assertThrows(IllegalArgumentException::class.java) {
            NumberUtil.generateRandomInts(0, 3, 5, false)
        }

        assertEquals(ImmutableList.of(0, 1, 2, 3, 4, 5),
                NumberUtil.generateRandomInts(0, 5, 6, false))

        assertDoesNotThrow { NumberUtil.generateRandomInts(-10, 10, 10, true) }
        assertDoesNotThrow { NumberUtil.generateRandomInts(-10, 10, 10, false) }
    }

    /**
     * Tests for the calculate magnitude method.
     */
    @Test
    fun testCalculateMagnitude() {
        assertEquals(0.0, NumberUtil.calculateMagnitude(0.0))
        assertEquals(0.0, NumberUtil.calculateMagnitude(0.0, 0.0))
        assertEquals(0.0, NumberUtil.calculateMagnitude(0.0, 0.0, 0.0))

        assertEquals(1.0, NumberUtil.calculateMagnitude(1.0))
        assertEquals(sqrt(2.0), NumberUtil.calculateMagnitude(1.0, 1.0))
        assertEquals(sqrt(3.0), NumberUtil.calculateMagnitude(1.0, 1.0, 1.0))

        assertEquals(5.0, NumberUtil.calculateMagnitude(5.0))
        assertEquals(11.180339887498949, NumberUtil.calculateMagnitude(5.0, 10.0))
        assertEquals(sqrt(5.0), NumberUtil.calculateMagnitude(1.0, 2.0))
        assertEquals(1.9999999999999998, NumberUtil.calculateMagnitude(1.0, sqrt(3.0)))
    }

    /**
     * Tests for the min method.
     */
    @Test
    fun testMin() {
        assertEquals(0, NumberUtil.min(0))
        assertEquals(-1, NumberUtil.min(0, -1))
        assertEquals(-1, NumberUtil.min(-1, -1))
        assertEquals(-10, NumberUtil.min(-1, -10))
        assertEquals(200, NumberUtil.min(200, 300))
        assertEquals(200, NumberUtil.min(200, 300, 400, 500))
        assertEquals(4, NumberUtil.min(200, 300, 400, 500, 4))
    }

    /**
     * Tests for the gcd method.
     */
    @Test
    fun testGcd() {
        assertEquals(1, NumberUtil.gcd(4, 5))
        assertEquals(2, NumberUtil.gcd(2, 4))
        assertEquals(4, NumberUtil.gcd(4, 8))
        assertEquals(6, NumberUtil.gcd(6, 18))
        assertEquals(1, NumberUtil.gcd(5, 11))
        assertEquals(6, NumberUtil.gcd(12, 18))
    }

    /**
     * Tests for the lcm method.
     */
    @Test
    fun testLcm() {
        assertEquals(40, NumberUtil.lcm(ImmutableList.of(1, 5, 8)))
        assertEquals(24, NumberUtil.lcm(ImmutableList.of(2, 6, 8)))
        assertEquals(105, NumberUtil.lcm(ImmutableList.of(5, 7, 15)))
    }

    /**
     * Tests for the get random index method.
     */
    @Test
    fun testGetRandomIndex() {
        assertThrows(IllegalArgumentException::class.java) { NumberUtil.getRandomIndex(0, 0) }
        assertThrows(IllegalArgumentException::class.java) { NumberUtil.getRandomIndex(0, -1) }
        assertThrows(IllegalArgumentException::class.java) { NumberUtil.getRandomIndex(6, 5) }

        assertThrows(IllegalArgumentException::class.java) {
            NumberUtil.getRandomIndex(0, 1, 0, 1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            NumberUtil.getRandomIndex(5, 10, 5, 6, 7, 8, 9, 10)
        }

        assertDoesNotThrow { NumberUtil.getRandomIndex(0, 10, 0) }
        assertDoesNotThrow { NumberUtil.getRandomIndex(0, 10, 0, 1) }
        assertDoesNotThrow { NumberUtil.getRandomIndex(0, 10, 0, 1, 2) }
        assertDoesNotThrow { NumberUtil.getRandomIndex(0, 10, 0, 1, 2, 3) }
        assertDoesNotThrow { NumberUtil.getRandomIndex(0, 10, 0, 1, 2, 3, 4) }
        assertDoesNotThrow { NumberUtil.getRandomIndex(0, 10, 0, 1, 2, 3, 4, 5) }
        assertDoesNotThrow { NumberUtil.getRandomIndex(0, 10, 0, 1, 2, 3, 4, 5, 6) }
        assertDoesNotThrow { NumberUtil.getRandomIndex(0, 10, 0, 1, 2, 3, 4, 5, 6, 7) }
        assertDoesNotThrow { NumberUtil.getRandomIndex(0, 10, 0, 1, 2, 3, 4, 5, 6, 7, 8) }
        assertDoesNotThrow { NumberUtil.getRandomIndex(0, 10, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9) }
        assertThrows(IllegalArgumentException::class.java) {
            NumberUtil.getRandomIndex(0, 10, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        }

        assertDoesNotThrow { NumberUtil.getRandomIndex(5, 10) }
        assertDoesNotThrow { NumberUtil.getRandomIndex(5, 10, 5) }
        assertDoesNotThrow { NumberUtil.getRandomIndex(5, 10, 5, 6) }
        assertDoesNotThrow { NumberUtil.getRandomIndex(5, 10, 5, 6, 7) }
        assertDoesNotThrow { NumberUtil.getRandomIndex(5, 10, 5, 6, 7, 8) }
        assertDoesNotThrow { NumberUtil.getRandomIndex(5, 10, 5, 6, 7, 8, 9) }
        assertThrows(IllegalArgumentException::class.java) {
            NumberUtil.getRandomIndex(5, 10, 5, 6, 7, 8, 9, 10)
        }

        /*
        Test inclusive range generation 100x
         */
        IntStream.range(0, 99).forEach {
            assertTrue { Range.closed(5, 10).contains(NumberUtil.getRandomIndex(5, 10)) }
            assertTrue { Range.closed(6, 10).contains(NumberUtil.getRandomIndex(6, 10)) }
            assertTrue { Range.closed(7, 10).contains(NumberUtil.getRandomIndex(7, 10)) }
            assertTrue { Range.closed(8, 10).contains(NumberUtil.getRandomIndex(8, 10)) }
            assertTrue { Range.closed(9, 10).contains(NumberUtil.getRandomIndex(9, 10)) }
        }
    }
}