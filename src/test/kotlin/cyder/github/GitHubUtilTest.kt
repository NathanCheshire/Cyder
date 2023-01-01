package cyder.github

import com.google.common.collect.ImmutableList
import cyder.files.FileUtil
import cyder.parsers.github.Issue
import cyder.strings.LevenshteinUtil
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

        val testIssue = ("Issue{url=\"https://api.github.com/repos/NathanCheshire/Cyder/issues/244\","
                + " repository_url=\"https://api.github.com/repos/NathanCheshire/Cyder\","
                + " labels_url=\"https://api.github.com/repos/NathanCheshire/Cyder/issues/244/labels{/name}\","
                + " comments_url=\"https://api.github.com/repos/NathanCheshire/Cyder/issues/244/comments\","
                + " events_url=\"https://api.github.com/repos/NathanCheshire/Cyder/issues/244/events\","
                + " html_url=\"https://github.com/NathanCheshire/Cyder/issues/244\", id=1511095984,"
                + " node_id=\"I_kwDOENRUpc5aEX6w\", number=244, title=\"Test Issue (DO NOT CLOSE)\","
                + " user=cyder.parsers.remote.github.User@59e505b2, labels=[], state=\"open\", locked=false,"
                + " assignee=cyder.parsers.remote.github.User@3af0a9da,"
                + " assignees=[cyder.parsers.remote.github.User@43b9fd5], milestone=false, comments=0,"
                + " created_at=\"2022-12-26T17:16:52Z\", updated_at=\"2022-12-26T17:16:52Z\", closed_at=\"null\","
                + " author_association=\"OWNER\", active_lock_reason=false, body=\"This is a test issue for Cyder's"
                + " unit tests. DO NOT CLOSE.\", reactions=cyder.parsers.remote.github.Reaction@79dc5318,"
                + " timeline_url=\"https://api.github.com/repos/NathanCheshire/Cyder/issues/244/timeline\","
                + " performed_via_github_app=false, state_reason=\"null\"}")
        assertTrue(GitHubUtil.getCyderIssues().stream().filter {
            LevenshteinUtil.computeLevenshteinDistance(it.toString(), testIssue) <= 32 // 4 * 8
        }.count() > 0)
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