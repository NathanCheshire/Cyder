package cyder.animation

import main.java.cyder.animation.HarmonicRectangle
import main.java.cyder.constants.CyderColors
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Tests for [HarmonicRectangle].
 */
class HarmonicRectangleTest {
    /**
     * tests for creating and setting properties of the Harmonic Rectangle.
     */
    @Test
    fun testCreation() {
        val setAnimationDelay = 20
        val setAnimationInc = 2
        val setBackgroundColor = CyderColors.regularPink
        val setHarmonicDirection = HarmonicRectangle.HarmonicDirection.HORIZONTAL

        val rectangle = HarmonicRectangle(20, 20, 30, 30)
        rectangle.animationDelay = setAnimationDelay
        rectangle.animationInc = setAnimationInc
        rectangle.backgroundColor = setBackgroundColor
        rectangle.harmonicDirection = setHarmonicDirection

        Assertions.assertEquals(setAnimationDelay, rectangle.animationDelay)
        Assertions.assertEquals(setAnimationInc, rectangle.animationInc)
        Assertions.assertEquals(setBackgroundColor, rectangle.backgroundColor)
        Assertions.assertEquals(setHarmonicDirection, rectangle.harmonicDirection)

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            HarmonicRectangle(-1, -1, -1, -1)
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            HarmonicRectangle(0, 0, 0, 0)
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            HarmonicRectangle(20, 20, 20, 20)
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            HarmonicRectangle(20, 20, -1, -1)
        }
    }

    /**
     * Tests for the animation step method.
     */
    @Test
    fun testAnimationStep() {
        val staticMin = 20
        val staticMax = 30

        val animationDelay = 20
        val animationInc = 2

        val rectangle = HarmonicRectangle(staticMin, staticMin, staticMax, staticMax)
        rectangle.animationDelay = animationDelay
        rectangle.animationInc = animationInc
        rectangle.harmonicDirection = HarmonicRectangle.HarmonicDirection.HORIZONTAL

        var start = staticMin + animationInc
        for (i in start..staticMax step animationInc) {
            rectangle.animationStep()
            Assertions.assertEquals(i, rectangle.width)
        }

        start = staticMax - animationInc
        for (i in start downTo staticMin step animationInc) {
            rectangle.animationStep()
            Assertions.assertEquals(i, rectangle.width)
        }
    }
}