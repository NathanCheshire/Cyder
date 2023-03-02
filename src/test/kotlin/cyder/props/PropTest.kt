package cyder.props

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

/**
 * Tests for [Prop]s.
 */
class PropTest {
    /**
     * Tests for creation of props.
     */
    @Test
    fun testCreation() {
        assertThrows(NullPointerException::class.java) { Prop(null, null, null) }
        assertThrows(IllegalArgumentException::class.java) { Prop("", null, null) }
        assertThrows(NullPointerException::class.java) { Prop("key", null, null) }
        assertThrows(NullPointerException::class.java) { Prop("key", "value", null) }
        assertThrows(IllegalArgumentException::class.java) { Prop("key", "value", Int.javaClass) }

        assertDoesNotThrow { Prop("key", "value", String::class.java) }
    }

    /**
     * Tests for the accessor methods.
     */
    @Test
    fun testAccessors() {
        val prop = Prop("key", "value", String::class.java)

        assertEquals("key", prop.key)
        assertEquals("value", prop.defaultValue)
        assertEquals("value", prop.value)
        assertEquals(String::class.java, prop.type)
    }

    /**
     * Tests for the to string method.
     */
    @Test
    fun testToString() {
        val prop = Prop("key", "value", String::class.java)

        assertEquals("Prop{key=key, value=value, type=class java.lang.String, defaultValue=value,"
                + " cachedCustomSpecifiedValue=null}", prop.toString())
    }

    /**
     * Tests for the hash code method.
     */
    @Test
    fun testHashCode() {
        val prop = Prop("key", "value", String::class.java)

        assertEquals(1034582418, prop.hashCode())
    }

    /**
     * Tests for the equals method.
     */
    @Test
    fun testEquals() {
        val prop1 = Prop("key", "value", String::class.java)
        val prop2 = Prop("key", "value", String::class.java)
        val prop3 = Prop("key", "third", String::class.java)

        assertEquals(prop1, prop2)
        assertNotEquals(prop1, prop3)
    }
}