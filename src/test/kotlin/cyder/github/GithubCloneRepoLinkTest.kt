package cyder.github

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for [GithubCloneRepoLink]s.
 */
class GithubCloneRepoLinkTest {
    /**
     * Tests for creation of github clone repo links.
     */
    @Test
    fun testCreation() {
        assertThrows(NullPointerException::class.java) { GithubCloneRepoLink(null) }
        assertThrows(IllegalArgumentException::class.java) { GithubCloneRepoLink("") }
        assertThrows(IllegalArgumentException::class.java) { GithubCloneRepoLink("youtube.com") }
        assertThrows(IllegalArgumentException::class.java) { GithubCloneRepoLink("google.com") }
        assertThrows(IllegalArgumentException::class.java) { GithubCloneRepoLink("github.com") }
        assertThrows(IllegalArgumentException::class.java) { GithubCloneRepoLink("github.com/nathancheshire") }
        assertThrows(IllegalArgumentException::class.java) {
            GithubCloneRepoLink("github.com/nathancheshire/goopy")
        }

        assertThrows(IllegalArgumentException::class.java) {
            GithubCloneRepoLink("ftp:github.com/nathancheshire/goopy.git")
        }
        assertThrows(IllegalArgumentException::class.java) {
            GithubCloneRepoLink("ssh:github.com/nathancheshire/goopy.git")
        }
        assertThrows(IllegalArgumentException::class.java) {
            GithubCloneRepoLink("protocol:github.com/nathancheshire/goopy.git")
        }

        assertThrows(IllegalArgumentException::class.java) {
            GithubCloneRepoLink(".github.com/nathancheshire/goopy.git")
        }
        assertThrows(IllegalArgumentException::class.java) {
            GithubCloneRepoLink("ww.github.com/nathancheshire/goopy.git")
        }

        assertDoesNotThrow { GithubCloneRepoLink("https://github.com/nathancheshire/goopy.git") }
        assertDoesNotThrow { GithubCloneRepoLink("http://www.github.com/nathancheshire/goopy.git") }
        assertDoesNotThrow { GithubCloneRepoLink("http://github.com/nathancheshire/goopy.git") }
        assertDoesNotThrow { GithubCloneRepoLink("www.github.com/nathancheshire/goopy.git") }
        assertDoesNotThrow { GithubCloneRepoLink("github.com/nathancheshire/goopy.git") }
    }

    /**
     * Tests for accessors, mutators, and other methods.
     */
    @Test
    fun testMethods() {
        val goopy = GithubCloneRepoLink("github.com/nathancheshire/goopy.git")
        assertEquals("https://www.github.com/nathancheshire/goopy.git", goopy.link)
        assertEquals("nathancheshire", goopy.user)
        assertEquals("goopy", goopy.repository)
        assertTrue(goopy.urlExists())

        val punch = GithubCloneRepoLink("github.com/nathancheshire/punch.git")
        assertEquals("https://www.github.com/nathancheshire/punch.git", punch.link)
        assertEquals("nathancheshire", punch.user)
        assertEquals("punch", punch.repository)
        assertTrue(punch.urlExists())
    }

    /**
     * Tests the url correction method of Github clone repo links.
     */
    @Test
    fun testUrlCorrection() {
        assertEquals("https://www.github.com/nathancheshire/goopy.git",
                GithubCloneRepoLink("github.com/nathancheshire/goopy.git").link)
        assertEquals("https://www.github.com/nathancheshire/goopy.git",
                GithubCloneRepoLink("www.github.com/nathancheshire/goopy.git").link)
        assertEquals("https://www.github.com/nathancheshire/goopy.git",
                GithubCloneRepoLink("http://www.github.com/nathancheshire/goopy.git").link)
        assertEquals("https://www.github.com/nathancheshire/goopy.git",
                GithubCloneRepoLink("https://github.com/nathancheshire/goopy.git").link)
    }

    /**
     * Tests for the equals method.
     */
    @Test
    fun testEquals() {
        val goopy = GithubCloneRepoLink("github.com/nathancheshire/goopy.git")
        val goopy2 = GithubCloneRepoLink("github.com/nathancheshire/goopy.git")
        val cyder = GithubCloneRepoLink("github.com/nathancheshire/cyder.git")

        assertEquals(goopy, goopy2)
        assertNotEquals(goopy, cyder)
        assertNotEquals(goopy2, cyder)
    }

    /**
     * Tests for the hash code method.
     */
    @Test
    fun testHashCode() {
        val goopy = GithubCloneRepoLink("github.com/nathancheshire/goopy.git")
        val goopy2 = GithubCloneRepoLink("github.com/nathancheshire/goopy.git")
        val cyder = GithubCloneRepoLink("github.com/nathancheshire/cyder.git")

        assertEquals(goopy.hashCode(), goopy2.hashCode())
        assertNotEquals(goopy.hashCode(), cyder.hashCode())
        assertNotEquals(goopy2.hashCode(), cyder.hashCode())
    }

    /**
     * Tests for the to string method.
     */
    @Test
    fun testToString() {
        val goopy = GithubCloneRepoLink("github.com/nathancheshire/goopy.git")
        val goopy2 = GithubCloneRepoLink("github.com/nathancheshire/goopy.git")
        val cyder = GithubCloneRepoLink("github.com/nathancheshire/cyder.git")

        assertEquals("GithubCloneRepoLink{link=\"https://www.github.com/nathancheshire/goopy.git\","
                + " user=\"nathancheshire\", repository=\"goopy\"}", goopy.toString())
        assertEquals("GithubCloneRepoLink{link=\"https://www.github.com/nathancheshire/goopy.git\","
                + " user=\"nathancheshire\", repository=\"goopy\"}", goopy2.toString())
        assertEquals("GithubCloneRepoLink{link=\"https://www.github.com/nathancheshire/cyder.git\","
                + " user=\"nathancheshire\", repository=\"cyder\"}", cyder.toString())
    }
}