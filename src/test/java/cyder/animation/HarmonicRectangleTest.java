package cyder.animation;

import main.java.cyder.animation.HarmonicRectangle;
import main.java.cyder.constants.CyderColors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link main.java.cyder.animation.HarmonicRectangle}s.
 */
public class HarmonicRectangleTest {
    /**
     * Default constructor for JUnit.
     */
    public HarmonicRectangleTest() {}

    @Test
    void testCreation() {
        HarmonicRectangle rectangle = new HarmonicRectangle(20, 20,
                30, 30);

        rectangle.setAnimationDelay(20);
        rectangle.setAnimationInc(2);
        rectangle.setBackgroundColor(CyderColors.regularPink);
        rectangle.setHarmonicDirection(HarmonicRectangle.HarmonicDirection.HORIZONTAL);

        assertEquals(20, rectangle.getAnimationDelay());
        assertEquals(2, rectangle.getAnimationInc());
        assertEquals(CyderColors.regularPink, rectangle.getBackgroundColor());
        assertEquals(HarmonicRectangle.HarmonicDirection.HORIZONTAL, rectangle.getHarmonicDirection());
    }

    /**
     * Tests for the animation step method.
     */
    @Test
    void testAnimationStep() {
        HarmonicRectangle rectangle = new HarmonicRectangle(20, 20,
                30, 30);

        rectangle.setAnimationDelay(20);
        rectangle.setAnimationInc(2);
        rectangle.setHarmonicDirection(HarmonicRectangle.HarmonicDirection.HORIZONTAL);

        rectangle.animationStep();
        assertEquals(22, rectangle.getWidth());
        rectangle.animationStep();
        assertEquals(24, rectangle.getWidth());
        rectangle.animationStep();
        assertEquals(26, rectangle.getWidth());
        rectangle.animationStep();
        assertEquals(28, rectangle.getWidth());
        rectangle.animationStep();
        assertEquals(30, rectangle.getWidth());
        rectangle.animationStep();
        assertEquals(28, rectangle.getWidth());
    }
}
