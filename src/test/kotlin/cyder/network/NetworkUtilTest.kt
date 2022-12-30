package cyder.network

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for [NetworkUtil]s.
 */
class NetworkUtilTest {
    /**
     * Tests for the high latency setter/getter.
     */
    @Test
    fun highLatency() {

    }

    /**
     * Tests for the high ping checker starter/terminator.
     */
    @Test
    fun testHighPingChecker() {

    }

    /**
     * Tests for the open url method.
     */
    @Test
    fun testOpenUrl() {

    }

    /**
     * Tests for the url reachable method.
     */
    @Test
    fun testUrlReachable() {

    }

    /**
     * Test for the read url method.
     */
    @Test
    fun testReadUrl() {
        assertThrows(NullPointerException::class.java) { NetworkUtil.readUrl(null) }
        assertThrows(IllegalArgumentException::class.java) { NetworkUtil.readUrl("") }

        assertEquals("", NetworkUtil.readUrl("https://www.google.com/"))
        assertEquals("", NetworkUtil.readUrl("https://www.google.com/"))
    }

    /**
     * Tests for the get url title method.
     */
    @Test
    fun testGetUrlTitle() {

    }

    /**
     * Tests for the is valid url method.
     */
    @Test
    fun testIsValidUrl() {
        assertThrows(NullPointerException::class.java) { NetworkUtil.isValidUrl(null) }
        assertThrows(IllegalArgumentException::class.java) { NetworkUtil.isValidUrl("") }

        assertTrue(NetworkUtil.isValidUrl("www.google.com"))
        assertTrue(NetworkUtil.isValidUrl("http://www.google.com"))
        assertTrue(NetworkUtil.isValidUrl("https://www.google.com"))
        assertTrue(NetworkUtil.isValidUrl("http://google.com"))
        assertTrue(NetworkUtil.isValidUrl("https://google.com"))

        assertTrue(NetworkUtil.isValidUrl("ftp://github.com/nathancheshire/cyder.git"))
        assertTrue(NetworkUtil.isValidUrl("ssh://github.com/nathancheshire/cyder.git"))

        assertFalse(NetworkUtil.isValidUrl("asdf"))
        assertFalse(NetworkUtil.isValidUrl("asdf/asdf/asdf"))
    }

    /**
     * Tests for the download resource method.
     */
    @Test
    fun testDownloadResource() {

    }

    /**
     * Tests for the local port available.
     */
    @Test
    fun testLocalPortAvailable() {
        assertThrows(IllegalArgumentException::class.java) { NetworkUtil.localPortAvailable(-1) }
        assertThrows(IllegalArgumentException::class.java) { NetworkUtil.localPortAvailable(0) }
        assertThrows(IllegalArgumentException::class.java) { NetworkUtil.localPortAvailable(1) }
        assertThrows(IllegalArgumentException::class.java) { NetworkUtil.localPortAvailable(1023) }

        assertDoesNotThrow { NetworkUtil.localPortAvailable(1024) }
        assertDoesNotThrow { NetworkUtil.localPortAvailable(5000) }
        assertDoesNotThrow { NetworkUtil.localPortAvailable(10000) }
        assertDoesNotThrow { NetworkUtil.localPortAvailable(40000) }

        assertDoesNotThrow { NetworkUtil.localPortAvailable(65534) }
        assertDoesNotThrow { NetworkUtil.localPortAvailable(65535) }

        assertThrows(IllegalArgumentException::class.java) { NetworkUtil.localPortAvailable(65536) }
        assertThrows(IllegalArgumentException::class.java) { NetworkUtil.localPortAvailable(65537) }
        assertThrows(IllegalArgumentException::class.java) { NetworkUtil.localPortAvailable(65538) }
    }
}