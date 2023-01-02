package cyder.github

import com.google.common.collect.ImmutableList
import cyder.files.FileUtil
import cyder.github.parsers.Issue
import cyder.utils.OsUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.util.concurrent.Future

/**
 * Tests for [GitHubUtil] methods.
 */
class GitHubUtilTest {
    /**
     * Tests for the get issues method.
     */
    @Test
    fun testGetIssues() {
        assertThrows(NullPointerException::class.java) { GitHubUtil.getIssues(null, null) }
        assertThrows(NullPointerException::class.java) { GitHubUtil.getIssues("user", null) }
        assertThrows(IllegalArgumentException::class.java) { GitHubUtil.getIssues("", "") }
        assertThrows(IllegalArgumentException::class.java) { GitHubUtil.getIssues("user", "") }

        var punchIssues: ImmutableList<Issue>? = null
        assertDoesNotThrow { punchIssues = GitHubUtil.getIssues("nathancheshire", "punch") }
        assertTrue(punchIssues!!.isEmpty())
    }

    /**
     * Tests for the get Cyder issues method.
     */
    @Test
    fun testGetCyderIssues() {
        assertDoesNotThrow { GitHubUtil.getCyderIssues() }

        val issues = GitHubUtil.getCyderIssues()
        assertFalse(issues.isEmpty())

        val testIssue: Issue? = issues.stream().filter { it.number == 244 }.findFirst().orElse(null)
        assertNotNull(testIssue)
    }

    /**
     * Tests for the get languages method.
     */
    @Test
    fun testGetLanguages() {
        assertDoesNotThrow { GitHubUtil.getLanguages() }

        val languages = GitHubUtil.getLanguages()
        val pythonBytes = languages["Python"]?.toDouble()
        val javaBytes = languages["Java"]?.toDouble()
        val kotlinBytes = languages["Kotlin"]?.toDouble()

        println("Python bytes: ${OsUtil.formatBytes(pythonBytes!!.toFloat())}")
        println("Java bytes: ${OsUtil.formatBytes(javaBytes!!.toFloat())}")
        println("Kotlin bytes: ${OsUtil.formatBytes(kotlinBytes!!.toFloat())}")

        assertTrue(pythonBytes > 0.0)
        assertTrue(javaBytes > 0.0)
        assertTrue(kotlinBytes > 0.0)
    }

    /**
     * Tests for the clone repo to directory links.
     */
    @Test
    fun testCloneRepoToDirectory() {
        assertThrows(NullPointerException::class.java) { GitHubUtil.cloneRepoToDirectory(null, null) }
        assertThrows(IllegalArgumentException::class.java) { GitHubUtil.cloneRepoToDirectory("", null) }
        assertThrows(NullPointerException::class.java) { GitHubUtil.cloneRepoToDirectory("link", null) }
        assertThrows(IllegalArgumentException::class.java) {
            GitHubUtil.cloneRepoToDirectory("link", File("directory_that_does_not_exist"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            GitHubUtil.cloneRepoToDirectory("link", File(".gitignore"))
        }

        val tmpDirectory = File("tmp")
        tmpDirectory.mkdir()
        assertTrue(tmpDirectory.exists())

        var downloaded: Future<Boolean>? = null
        assertDoesNotThrow {
            downloaded = GitHubUtil.cloneRepoToDirectory("github.com/nathancheshire/punch.git", tmpDirectory)
        }
        assertNotNull(downloaded)
        while (downloaded == null || !downloaded!!.isDone) {
            Thread.onSpinWait()
        }

        assertTrue(downloaded!!.get())
        assertTrue(File("tmp/punch").exists())
        assertTrue(FileUtil.size(File("tmp/punch")) > 0)
        assertTrue(File("tmp/punch/src").exists())
        assertTrue(OsUtil.deleteFile(tmpDirectory, false))
    }
}