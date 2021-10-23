package cyder.testing;

import cyder.utilities.BoundsUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnitTests {
    @Test
    public void testInsertBreaks() {
        assertEquals("It's the strangest feeling,<br/>feeling this way for you.", BoundsUtil.insertBreaks(
                "It's the strangest feeling, feeling this way for you.",2));
        assertEquals("Waka waka<br/>waka<br/>waka<br/>waka waka waka.", BoundsUtil.insertBreaks(
                "Waka waka waka waka waka waka waka.",4));
    }
}
