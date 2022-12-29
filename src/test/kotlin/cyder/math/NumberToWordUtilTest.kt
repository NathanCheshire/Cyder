package cyder.math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * Tests for [NumberToWordUtil]s.
 */
class NumberToWordUtilTest {
    /**
     * Tests for the to words method.
     */
    @Test
    fun testToWords() {
        assertThrows(NullPointerException::class.java) { NumberToWordUtil.toWords(null) }
        assertThrows(IllegalArgumentException::class.java) { NumberToWordUtil.toWords("") }
        assertThrows(IllegalArgumentException::class.java) { NumberToWordUtil.toWords("10e4") }
        assertThrows(IllegalArgumentException::class.java) { NumberToWordUtil.toWords("10.5") }
        assertThrows(IllegalArgumentException::class.java) { NumberToWordUtil.toWords("100_005") }
        assertThrows(IllegalArgumentException::class.java) { NumberToWordUtil.toWords("100_005.500") }
        assertThrows(IllegalArgumentException::class.java) { NumberToWordUtil.toWords("100.005.500") }
        assertThrows(IllegalArgumentException::class.java) { NumberToWordUtil.toWords("-100.005-500") }

        assertEquals("Zero", NumberToWordUtil.toWords("0"))
        assertEquals("Zero", NumberToWordUtil.toWords("-0"))
        assertEquals("One", NumberToWordUtil.toWords("1"))
        assertEquals("Two", NumberToWordUtil.toWords("2"))
        assertEquals("Three", NumberToWordUtil.toWords("3"))
        assertEquals("Four", NumberToWordUtil.toWords("4"))
        assertEquals("Five", NumberToWordUtil.toWords("5"))
        assertEquals("Six", NumberToWordUtil.toWords("6"))
        assertEquals("Seven", NumberToWordUtil.toWords("7"))
        assertEquals("Eight", NumberToWordUtil.toWords("8"))
        assertEquals("Nine", NumberToWordUtil.toWords("9"))
        assertEquals("Ten", NumberToWordUtil.toWords("10"))
        assertEquals("Eleven", NumberToWordUtil.toWords("11"))
        assertEquals("Twelve", NumberToWordUtil.toWords("12"))
        assertEquals("Thirteen", NumberToWordUtil.toWords("13"))
        assertEquals("Fourteen", NumberToWordUtil.toWords("14"))
        assertEquals("Fifteen", NumberToWordUtil.toWords("15"))
        assertEquals("Sixteen", NumberToWordUtil.toWords("16"))
        assertEquals("Seventeen", NumberToWordUtil.toWords("17"))
        assertEquals("Eighteen", NumberToWordUtil.toWords("18"))
        assertEquals("Nineteen", NumberToWordUtil.toWords("19"))
        assertEquals("Twenty", NumberToWordUtil.toWords("20"))
        assertEquals("Twenty One", NumberToWordUtil.toWords("21"))

        assertEquals("Thirty", NumberToWordUtil.toWords("30"))
        assertEquals("Forty", NumberToWordUtil.toWords("40"))
        assertEquals("Fifty", NumberToWordUtil.toWords("50"))
        assertEquals("Sixty", NumberToWordUtil.toWords("60"))
        assertEquals("Seventy", NumberToWordUtil.toWords("70"))
        assertEquals("Eighty", NumberToWordUtil.toWords("80"))
        assertEquals("Ninety", NumberToWordUtil.toWords("90"))

        assertEquals("One Hundred", NumberToWordUtil.toWords("100"))
        assertEquals("Two Hundred", NumberToWordUtil.toWords("200"))
        assertEquals("Three Hundred", NumberToWordUtil.toWords("300"))
        assertEquals("Four Hundred", NumberToWordUtil.toWords("400"))
        assertEquals("Five Hundred", NumberToWordUtil.toWords("500"))
        assertEquals("Six Hundred", NumberToWordUtil.toWords("600"))
        assertEquals("Seven Hundred", NumberToWordUtil.toWords("700"))
        assertEquals("Eight Hundred", NumberToWordUtil.toWords("800"))
        assertEquals("Nine Hundred", NumberToWordUtil.toWords("900"))

        assertEquals("One-thousand", NumberToWordUtil.toWords("1000"))
        assertEquals("Five-thousand", NumberToWordUtil.toWords("5000"))
        assertEquals("Ten-thousand", NumberToWordUtil.toWords("10000"))
        assertEquals("Fifty-thousand", NumberToWordUtil.toWords("50000"))
        assertEquals("One Hundred Fifty-thousand", NumberToWordUtil.toWords("150000"))

        // Here's 44 more.
        assertEquals("Forty Four", NumberToWordUtil.toWords("44"))
        assertEquals("One Hundred Forty Three", NumberToWordUtil.toWords("143"))
        // What all do you want from me?
        assertEquals("Two Hundred Twenty Three", NumberToWordUtil.toWords("223"))
        // All fast money.
        assertEquals("Six Hundred Seventy Nine", NumberToWordUtil.toWords("679"))
        // Approaching Nirvana.
        assertEquals("Three Hundred Five", NumberToWordUtil.toWords("305"))
        // Thirty's in the city moving slow.
        assertEquals("Three-thousand Five Hundred", NumberToWordUtil.toWords("3500"))
        // Reverb on guitar plucks.
        assertEquals("Ninety-thousand Two Hundred Ten", NumberToWordUtil.toWords("90210"))
        // For the price of a dime.
        assertEquals("Eight-million Six Hundred Seventy Five-thousand And Three Hundred Nine",
                NumberToWordUtil.toWords("8675309"))
        // You swipe like a credit card scammer.
        assertEquals("One-million Four Hundred-thousand Nine Hundred Ninety Nine",
                NumberToWordUtil.toWords("1400999"))
        // I know I'm hurting deep down but can't show it.
        assertEquals("Eighteen-billion Two-million Seven Hundred Thirty Eight-thousand Two Hundred Fifty Five",
                NumberToWordUtil.toWords("18002738255"))
    }
}