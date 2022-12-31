package cyder.network

import cyder.files.FileUtil
import cyder.threads.ThreadUtil
import cyder.utils.OsUtil
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

/**
 * Tests for [NetworkUtil]s.
 */
class NetworkUtilTest {
    /**
     * Tests for the high latency setter/getter.
     */
    @Test
    fun highLatency() {
        assertDoesNotThrow { NetworkUtil.isHighLatency() }
        assertFalse { NetworkUtil.isHighLatency() }
    }

    /**
     * Tests for the high ping checker starter/terminator.
     */
    @Test
    fun testHighPingChecker() {
        assertFalse { NetworkUtil.highPingCheckerRunning() }
        assertDoesNotThrow { NetworkUtil.startHighPingChecker() }
        assertTrue { NetworkUtil.highPingCheckerRunning() }
        assertDoesNotThrow { NetworkUtil.terminateHighPingChecker() }
        assertFalse { NetworkUtil.highPingCheckerRunning() }
    }

    /**
     * Tests for the open url method.
     */
    @Test
    fun testOpenUrl() {
        assertThrows(NullPointerException::class.java) { NetworkUtil.openUrl(null) }
        assertThrows(IllegalArgumentException::class.java) { NetworkUtil.openUrl("") }

        var optionalProcess: Optional<Process>? = null
        assertDoesNotThrow { optionalProcess = NetworkUtil.openUrl("https://www.google.com") }
        assertTrue(optionalProcess!!.isPresent)

        val process = optionalProcess!!.get()
        assertNotNull(process)
        ThreadUtil.sleepSeconds(5)
        assertDoesNotThrow { process.destroy() }
    }

    /**
     * Tests for the url reachable method.
     */
    @Test
    fun testUrlReachable() {
        assertThrows(NullPointerException::class.java) { NetworkUtil.urlReachable(null) }
        assertThrows(IllegalArgumentException::class.java) { NetworkUtil.urlReachable("") }

        assertDoesNotThrow { NetworkUtil.urlReachable("https://www.youtube.com") }
        assertDoesNotThrow { NetworkUtil.urlReachable("https://www.google.com") }
        assertDoesNotThrow { NetworkUtil.urlReachable("https://www.github.com") }
    }

    /**
     * Test for the read url method.
     */
    @Test
    fun testReadUrl() {
        assertThrows(NullPointerException::class.java) { NetworkUtil.readUrl(null) }
        assertThrows(IllegalArgumentException::class.java) { NetworkUtil.readUrl("") }

        assertDoesNotThrow { Jsoup.parse(NetworkUtil.readUrl("https://www.google.com/")) }
        assertDoesNotThrow { Jsoup.parse(NetworkUtil.readUrl("https://www.youtube.com/")) }
        assertDoesNotThrow { Jsoup.parse(NetworkUtil.readUrl("https://www.github.com/")) }
    }

    /**
     * Tests for the get url title method.
     */
    @Test
    fun testGetUrlTitle() {
        assertThrows(NullPointerException::class.java) { NetworkUtil.getUrlTitle(null) }
        assertThrows(IllegalArgumentException::class.java) { NetworkUtil.getUrlTitle("") }

        var optionalTitle: Optional<String>? = null
        assertDoesNotThrow { optionalTitle = NetworkUtil.getUrlTitle("https://www.google.com") }
        assertNotNull(optionalTitle)
        assertTrue(optionalTitle!!.isPresent)
        assertEquals("Google", optionalTitle!!.get())

        assertDoesNotThrow { optionalTitle = NetworkUtil.getUrlTitle("https://www.youtube.com") }
        assertNotNull(optionalTitle)
        assertTrue(optionalTitle!!.isPresent)
        assertEquals("YouTube", optionalTitle!!.get())

        assertDoesNotThrow { optionalTitle = NetworkUtil.getUrlTitle("https://www.github.com") }
        assertNotNull(optionalTitle)
        assertTrue(optionalTitle!!.isPresent)
        assertEquals("GitHub: Let’s build from here · GitHub", optionalTitle!!.get())
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
        assertThrows(NullPointerException::class.java) {
            NetworkUtil.downloadResource(null, null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            NetworkUtil.downloadResource("", null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            NetworkUtil.downloadResource("asdf", null)
        }
        assertThrows(NullPointerException::class.java) {
            NetworkUtil.downloadResource("https://www.google.com", null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            NetworkUtil.downloadResource("https://www.google.com", File(".gitignore"))
        }

        val tmpDir = File("tmp")
        tmpDir.mkdir()
        assertTrue(tmpDir.exists())

        val downloadFile = File("tmp/readme.md")
        assertDoesNotThrow {
            NetworkUtil.downloadResource(
                    "https://raw.githubusercontent.com/NathanCheshire/Cyder/main/README.md", downloadFile)
        }

        assertTrue(downloadFile.exists())
        assertEquals(FileUtil.getFileLines(downloadFile), FileUtil.getFileLines(File("readme.md")))

        OsUtil.deleteFile(tmpDir, false)
        assertFalse(tmpDir.exists())
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