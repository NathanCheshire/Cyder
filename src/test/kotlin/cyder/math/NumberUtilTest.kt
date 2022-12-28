package cyder.math

import com.google.common.collect.ImmutableList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

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
}