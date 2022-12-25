package cyder.bounds

import cyder.constants.CyderFonts
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Tests for the [BoundsUtil] methods.
 */
class BoundsUtilTest {
    /**
     * The string to insert breaks into.
     */
    private val breakString = ("Still it cried 'Sleep no more!' to all the house: Glamis hath murdered sleep, and "
            + "therefore Cawdor shall sleep no more; Macbeth shall sleep no more.")

    /**
     * Tests for inserting breaks.
     */
    @Suppress("SpellCheckingInspection")
    @Test
    fun testInsertBreaks() {
        Assertions.assertThrows(NullPointerException::class.java) { BoundsUtil.insertBreaks(null, 0) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { BoundsUtil.insertBreaks("", 0) }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            BoundsUtil.insertBreaks("<html>", 1)
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            BoundsUtil.insertBreaks("<I'm nothing with out your love alive>", 1)
        }

        Assertions.assertEquals("Something just like this",
                BoundsUtil.insertBreaks("Something just like this", 1))

        Assertions.assertEquals("Something<br/>just like this",
                BoundsUtil.insertBreaks("Something just like this", 2))

        Assertions.assertEquals("Somethingj<br/>ustlikethis",
                BoundsUtil.insertBreaks("Somethingjustlikethis", 2))

        Assertions.assertEquals("Alpha<br/>Beta<br/>Gamma",
                BoundsUtil.insertBreaks("Alpha Beta Gamma", 3))

        Assertions.assertEquals("Alpha<br/>Beta<br/>Gamma<br/>Delta",
                BoundsUtil.insertBreaks("Alpha Beta Gamma Delta", 4))

        Assertions.assertEquals("Alpha<br/>Beta<br/>Gamma<br/>Delta<br/>Epsilon",
                BoundsUtil.insertBreaks("Alpha Beta Gamma Delta Epsilon", 5))

        Assertions.assertEquals("Alpha<br/>Beta<br/>Gamma<br/>Delta<br/>Epsilon<br/>Zeta",
                BoundsUtil.insertBreaks("Alpha Beta Gamma Delta Epsilon Zeta", 6))

        Assertions.assertEquals("Still it cried 'Sleep no more!' to all the house:"
                + " Glamis hath murdered<br/>sleep, and therefore Cawdor shall sleep no more;"
                + " Macbeth shall sleep no more.", BoundsUtil.insertBreaks(breakString, 2))

        Assertions.assertEquals("Still it cried 'Sleep no more!' to all the house:<br/>Glamis"
                + " hath murdered sleep, and therefore Cawdor<br/>shall sleep no more;"
                + " Macbeth shall sleep no more.", BoundsUtil.insertBreaks(breakString, 3))

        Assertions.assertEquals("Still it cried 'Sleep no more!' to<br/>all the house: Glamis"
                + " hath murdered<br/>sleep, and therefore Cawdor shall sleep no<br/>more; Macbeth"
                + " shall sleep no more.", BoundsUtil.insertBreaks(breakString, 4))

        Assertions.assertEquals("Still it cried 'Sleep no<br/>more!' to all the house:"
                + " Glamis<br/>hath murdered sleep, and therefore<br/>Cawdor shall sleep no more;<br/>"
                + "Macbeth shall sleep no more.", BoundsUtil.insertBreaks(breakString, 5))
    }

    /**
     * Tests for inserting breaks.
     */
    @Test
    fun testWidthHeightCalculation() {
        var result = BoundsString("<html>Still it cried 'Sleep no more!' to all the house:"
                + " Glamis hath murdered<br/>sleep, and therefore Cawdor shall sleep no more;"
                + " Macbeth shall sleep no more.</html>", 593, 72)
        Assertions.assertEquals(result, BoundsUtil.widthHeightCalculation(breakString,
                CyderFonts.AGENCY_FB_22, 600))
        Assertions.assertEquals(result, BoundsUtil.widthHeightCalculation(breakString,
                CyderFonts.AGENCY_FB_22, 800))

        result = BoundsString("<html>Still it cried 'Sleep no more!' to all the"
                + " house: Glamis hath murdered sleep, and therefore Cawdor shall sleep"
                + " no more; Macbeth shall sleep no more.</html>", 1104, 36)
        Assertions.assertEquals(result, BoundsUtil.widthHeightCalculation(breakString, CyderFonts.AGENCY_FB_22))

        result = BoundsString("<html>Still it cried 'Sleep no more!' to all the house:<br/>Glamis"
                + " hath murdered sleep, and therefore Cawdor<br/>shall sleep no more;"
                + " Macbeth shall sleep no more.</html>", 389, 108)
        Assertions.assertEquals(result, BoundsUtil.widthHeightCalculation(breakString,
                CyderFonts.AGENCY_FB_22, 400))

        result = BoundsString("<html>Still it cried 'Sleep no more!' to<br/>all the house:"
                + " Glamis hath murdered<br/>sleep, and therefore Cawdor shall sleep no<br/>more;"
                + " Macbeth shall sleep no more.</html>", 324, 144)
        Assertions.assertEquals(result, BoundsUtil.widthHeightCalculation(breakString,
                CyderFonts.AGENCY_FB_22, 300))
    }
}