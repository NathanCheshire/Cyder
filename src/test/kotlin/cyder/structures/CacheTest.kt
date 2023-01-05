package cyder.structures

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

/**
 * Tests for [Cache]s.
 */
class CacheTest {
    /**
     * Tests for creation of Caches.
     */
    @Test
    fun testCreation() {
        assertThrows(NullPointerException::class.java) { Cache<Int>(null) }

        assertDoesNotThrow { Cache<Int>() }
    }

    /**
     * Tests for the cache setter method.
     */
    @Test
    fun testSetter() {
        val cache = Cache<Int>()

        assertThrows(NullPointerException::class.java) { cache.cache = null }
        assertDoesNotThrow { cache.cache = 1 }
    }

    /**
     * Tests for the cache accessor method.
     */
    @Test
    fun testGetter() {
        val cache = Cache(5)

        assertEquals(5, cache.cache)
    }

    /**
     * Tests for the clear method.
     */
    @Test
    fun testClear() {
        val cache = Cache(5)
        assertEquals(5, cache.cache)
        assertTrue(cache.isCachePresent)
        cache.clear()
        assertFalse(cache.isCachePresent)
    }

    /**
     * Tests for the is cache present method.
     */
    @Test
    fun testIsCachePresent() {
        val cache = Cache(5)
        assertEquals(5, cache.cache)
        assertTrue(cache.isCachePresent)
        cache.clear()
        assertFalse(cache.isCachePresent)
    }

    /**
     * Tests for the cache if not present method.
     */
    @Test
    fun testCacheIfNotPresent() {
        val cache = Cache(5)
        cache.clear()
        cache.cacheIfNotPresent(15)
        assertTrue(cache.isCachePresent)
        assertEquals(15, cache.cache)
    }

    /**
     * Tests for the set cached value updater.
     */
    @Test
    fun testSetCachedValueUpdater() {
        val cache = Cache(5)
        assertEquals(5, cache.cache)
        cache.clear()
        assertDoesNotThrow { cache.setCachedValueUpdater { 1 } }
    }

    /**
     * Tests for the refresh cache value.
     */
    @Test
    fun testRefreshCachedValue() {
        val cache = Cache(5)
        assertEquals(5, cache.cache)
        cache.clear()
        cache.setCachedValueUpdater { 1 }
        assertFalse(cache.isCachePresent)
        cache.refreshCachedValue()
        assertEquals(1, cache.cache)
    }
}