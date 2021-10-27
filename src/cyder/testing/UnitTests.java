package cyder.testing;

import cyder.utilities.BoundsUtil;
import cyder.widgets.Weather;
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

    @Test
    public void windBearingDirection() {
        //cardinal directions and +/1 1, +/1 0.1

        //east
        assertEquals(Weather.getWindDirection(- 1.0), "SE");
        assertEquals(Weather.getWindDirection(- 0.1), "SE");
        assertEquals(Weather.getWindDirection(0.0), "E");
        assertEquals(Weather.getWindDirection(0.1), "NE");
        assertEquals(Weather.getWindDirection(1.0), "NE");

        //north
        assertEquals(Weather.getWindDirection(89.0), "NE");
        assertEquals(Weather.getWindDirection(89.9), "NE");
        assertEquals(Weather.getWindDirection(90.0), "N");
        assertEquals(Weather.getWindDirection(90.1), "NW");
        assertEquals(Weather.getWindDirection(91.0), "NW");

        //west
        assertEquals(Weather.getWindDirection(179.0), "NW");
        assertEquals(Weather.getWindDirection(179.9), "NW");
        assertEquals(Weather.getWindDirection(180.0), "W");
        assertEquals(Weather.getWindDirection(180.1), "SW");
        assertEquals(Weather.getWindDirection(181.0), "SW");

        //south
        assertEquals(Weather.getWindDirection(269.0), "SW");
        assertEquals(Weather.getWindDirection(269.9), "SW");
        assertEquals(Weather.getWindDirection(270.0), "S");
        assertEquals(Weather.getWindDirection(270.1), "SE");
        assertEquals(Weather.getWindDirection(271.0), "SE");

        //half angles
        assertEquals(Weather.getWindDirection(45.0), "NE");
        assertEquals(Weather.getWindDirection(45.0 + 360.0), "NE");
        assertEquals(Weather.getWindDirection(45.0 - 360.0), "NE");

        assertEquals(Weather.getWindDirection(135.0), "NW");
        assertEquals(Weather.getWindDirection(135.0 + 360.0), "NW");
        assertEquals(Weather.getWindDirection(135.0 - 360.0), "NW");

        assertEquals(Weather.getWindDirection(225.0), "SW");
        assertEquals(Weather.getWindDirection(225.0 + 360.0), "SW");
        assertEquals(Weather.getWindDirection(225.0 - 360.0), "SW");

        assertEquals(Weather.getWindDirection(315.0), "SE");
        assertEquals(Weather.getWindDirection(315.0 + 360.0), "SE");
        assertEquals(Weather.getWindDirection(315.0 - 360.0), "SE");
    }
}
