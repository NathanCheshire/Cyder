package test.java;

import cyder.constants.CyderRegexPatterns;
import cyder.utilities.*;
import cyder.widgets.WeatherWidget;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class UnitTests {
    @Test
    public void testInsertBreaks() {
        assertEquals("It's the strangest feeling,<br/>feeling this way for you.", BoundsUtil.insertBreaks(
                "It's the strangest feeling, feeling this way for you.",2));
        assertEquals("Waka waka<br/>waka<br/>waka<br/>waka waka waka.", BoundsUtil.insertBreaks(
                "Waka waka waka waka waka waka waka.",4));
    }

    @Test
    public void testWindBearingDirection() {
        //cardinal directions and +/1 1, +/1 0.1

        //east
        assertEquals(WeatherWidget.getWindDirection(- 1.0), "SE");
        assertEquals(WeatherWidget.getWindDirection(- 0.1), "SE");
        assertEquals(WeatherWidget.getWindDirection(0.0), "E");
        assertEquals(WeatherWidget.getWindDirection(0.1), "NE");
        assertEquals(WeatherWidget.getWindDirection(1.0), "NE");

        //north
        assertEquals(WeatherWidget.getWindDirection(89.0), "NE");
        assertEquals(WeatherWidget.getWindDirection(89.9), "NE");
        assertEquals(WeatherWidget.getWindDirection(90.0), "N");
        assertEquals(WeatherWidget.getWindDirection(90.1), "NW");
        assertEquals(WeatherWidget.getWindDirection(91.0), "NW");

        //west
        assertEquals(WeatherWidget.getWindDirection(179.0), "NW");
        assertEquals(WeatherWidget.getWindDirection(179.9), "NW");
        assertEquals(WeatherWidget.getWindDirection(180.0), "W");
        assertEquals(WeatherWidget.getWindDirection(180.1), "SW");
        assertEquals(WeatherWidget.getWindDirection(181.0), "SW");

        //south
        assertEquals(WeatherWidget.getWindDirection(269.0), "SW");
        assertEquals(WeatherWidget.getWindDirection(269.9), "SW");
        assertEquals(WeatherWidget.getWindDirection(270.0), "S");
        assertEquals(WeatherWidget.getWindDirection(270.1), "SE");
        assertEquals(WeatherWidget.getWindDirection(271.0), "SE");

        //half angles
        assertEquals(WeatherWidget.getWindDirection(45.0), "NE");
        assertEquals(WeatherWidget.getWindDirection(45.0 + 360.0), "NE");
        assertEquals(WeatherWidget.getWindDirection(45.0 - 360.0), "NE");

        assertEquals(WeatherWidget.getWindDirection(135.0), "NW");
        assertEquals(WeatherWidget.getWindDirection(135.0 + 360.0), "NW");
        assertEquals(WeatherWidget.getWindDirection(135.0 - 360.0), "NW");

        assertEquals(WeatherWidget.getWindDirection(225.0), "SW");
        assertEquals(WeatherWidget.getWindDirection(225.0 + 360.0), "SW");
        assertEquals(WeatherWidget.getWindDirection(225.0 - 360.0), "SW");

        assertEquals(WeatherWidget.getWindDirection(315.0), "SE");
        assertEquals(WeatherWidget.getWindDirection(315.0 + 360.0), "SE");
        assertEquals(WeatherWidget.getWindDirection(315.0 - 360.0), "SE");
    }

    @Test
    public void testIPv4RegexMatcher() {
        ArrayList<String> ipv4Tests = new ArrayList<>();
        ipv4Tests.add("  127.045.04.1  ");
        ipv4Tests.add("  127.045.04.1");
        ipv4Tests.add("123");
        ipv4Tests.add("123.123");
        ipv4Tests.add("123.123.123");
        ipv4Tests.add("123.123.123.123");
        ipv4Tests.add("127.045.04.1   ");
        ipv4Tests.add("0.0.0.0");
        ipv4Tests.add("045.450.330.340");
        ipv4Tests.add("045.450.330");
        ipv4Tests.add("045.450");
        ipv4Tests.add("045");

        for (String ipv4Address : ipv4Tests) {
            assert ipv4Address.matches(CyderRegexPatterns.ipv4Pattern);
        }
    }

    @Test
    public void testPluralConversion() {
        assertEquals(StringUtil.getPlural(-1, "dog"),"dogs");
        assertEquals(StringUtil.getPlural(0, "dog"),"dogs");
        assertEquals(StringUtil.getPlural(1, "dog"),"dog");
        assertEquals(StringUtil.getPlural(2, "dog"),"dogs");

        assertEquals(StringUtil.getPlural(-1, "bus"),"buses");
        assertEquals(StringUtil.getPlural(0, "bus"),"buses");
        assertEquals(StringUtil.getPlural(1, "bus"),"bus");
        assertEquals(StringUtil.getPlural(2, "bus"),"buses");
    }

    @Test
    public void testPhoneNumberPattern() {
        ArrayList<String> phoneNumbers = new ArrayList<>();
        phoneNumbers.add("456 0112");
        phoneNumbers.add("456-0112");
        phoneNumbers.add("456 - 0112");
        phoneNumbers.add("(888) 456 0112");
        phoneNumbers.add("888 456 0112");
        phoneNumbers.add("888 - 456 0112");
        phoneNumbers.add("888 - 456 - 0112");
        phoneNumbers.add("4 (888) - 456 - 0112");
        phoneNumbers.add("4 - (888) - 456 - 0112");
        phoneNumbers.add("4 - 888 - 456 - 0112");
        phoneNumbers.add("4-888-456-0112");
        phoneNumbers.add("48884560112");

        for (String phoneNumber : phoneNumbers) {
            assert phoneNumber.matches(CyderRegexPatterns.phoneNumberPattern);
        }
    }

    @Test
    public void testIsComment() {
         assert StatUtil.isComment("*");
         assert StatUtil.isComment("//*");
         assert StatUtil.isComment("*/");
         assert StatUtil.isComment("**");
         assert StatUtil.isComment("//todos");
         assert StatUtil.isComment("/* is this one */");
         assert StatUtil.isComment("//**//**//");
         assert StatUtil.isComment("/*/");

        assert !StatUtil.isComment("raw text");
        assert !StatUtil.isComment("tee: //haha");

        //technically this line contains a comment but it iself is not a comment
        assert !StatUtil.isComment("tee: /*haha*/ alpha");

        assert !StatUtil.isComment("tee: //haha   /*");
        assert !StatUtil.isComment("tee: //haha/*");
        assert !StatUtil.isComment("tee: /*haha");
        assert !StatUtil.isComment("\"//\"");
    }

    @Test
    public void testFileSignature() {
        assertTrue(FileUtil.matchesSignature(new File(OSUtil.buildPath("static","pictures","C.png")),
               FileUtil.PNG_SIGNATURE));
        assertFalse(FileUtil.matchesSignature(new File(""), FileUtil.PNG_SIGNATURE));
        assertFalse(FileUtil.matchesSignature(null, FileUtil.PNG_SIGNATURE));
    }
}
