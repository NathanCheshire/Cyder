package cyder.props

import com.google.common.collect.ImmutableList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for [PropValueList]s.
 */
class PropValueListTest {
    /**
     * Tests for creation of prop value lists.
     */
    @Test
    fun testCreation() {
        assertThrows(NullPointerException::class.java) { PropValueList(null) }

        assertDoesNotThrow { PropValueList(ImmutableList.of()) }
        assertDoesNotThrow { PropValueList(ArrayList<String>()) }
    }

    /**
     * Tests for accessors of prop value lists.
     */
    @Test
    fun testAccessors() {
        val emptyList: ImmutableList<String> = ImmutableList.of()

        val empty = PropValueList(emptyList)
        val filled = PropValueList(ImmutableList.of("Alpha", "Beta", "Gamma"))

        assertEquals(emptyList, empty.getList())
        assertEquals(ImmutableList.of("Alpha", "Beta", "Gamma"), filled.getList())
    }

    /**
     * Tests for the to string method.
     */
    @Test
    fun testToString() {
        val emptyList: ImmutableList<String> = ImmutableList.of()

        val empty = PropValueList(emptyList)
        val filled = PropValueList(ImmutableList.of("Alpha", "Beta", "Gamma"))

        assertEquals("PropValueList{list=[]}", empty.toString())
        assertEquals("PropValueList{list=[Alpha, Beta, Gamma]}", filled.toString())
    }

    /**
     * Tests for the hash code method.
     */
    @Test
    fun testHashCode() {
        val emptyList: ImmutableList<String> = ImmutableList.of()

        val empty = PropValueList(emptyList)
        val filled = PropValueList(ImmutableList.of("Alpha", "Beta", "Gamma"))

        assertEquals(1, empty.hashCode())
        assertEquals(889444756, filled.hashCode())
    }

    /**
     * Tests for the equals method.
     */
    @Test
    fun testEquals() {
        val emptyList: ImmutableList<String> = ImmutableList.of()

        val empty = PropValueList(emptyList)
        val empty2 = PropValueList(emptyList)
        val filled = PropValueList(ImmutableList.of("Alpha", "Beta", "Gamma"))
        val filled2 = PropValueList(ImmutableList.of("Alpha", "Beta", "Gamma"))

        assertEquals(empty, empty)
        assertEquals(empty, empty2)
        assertEquals(filled, filled)
        assertEquals(filled, filled2)

        assertNotEquals(filled, empty)
    }
}