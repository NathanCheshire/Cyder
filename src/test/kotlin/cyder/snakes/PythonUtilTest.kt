package cyder.snakes

import cyder.process.ProcessResult
import cyder.process.PythonPackage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.Future

/**
 * Tests for [PythonUtil]s.
 */
class PythonUtilTest {
    /**
     * Tests for the get missing required packages method.
     */
    @Test
    fun testGetMissingRequiredPackages() {
        val futureMissingPackages = PythonUtil.getMissingRequiredPythonPackages()
        while (!futureMissingPackages.isDone) Thread.onSpinWait()
        val missingPackages = futureMissingPackages.get()
        println("Missing packages: $missingPackages")
        assertTrue(missingPackages.isEmpty())
    }

    /**
     * Tests for the get python version method.
     */
    @Test
    fun testGetPythonVersion() {
        val futureOptionalVersion = PythonUtil.getPythonVersion()
        while (!futureOptionalVersion.isDone) Thread.onSpinWait()

        val optionalResult = futureOptionalVersion.get()
        assertTrue(optionalResult.isPresent)

        val version = optionalResult.get()
        println("Python version: $version")
        assertTrue(version.matches(Regex("3\\.\\d+\\.\\d+")))
    }

    /**
     * Tests for the install pip dependency function.
     */
    @Test
    fun testInstallPipDependency() {
        var futureResult: Future<ProcessResult>? = null
        assertDoesNotThrow { futureResult = PythonUtil.installPipDependency(PythonPackage.PILLOW) }
        assertNotNull(futureResult)
        while (!futureResult!!.isDone) Thread.onSpinWait()
        val result = futureResult!!.get()

        println("Install Pillow pip dependency results:")
        println("Standard output: ${result.standardOutput}")
        println("Error output: ${result.errorOutput}")
        println("---------------------------------------")
    }

    /**
     * Tests for the is pip dependency present method.
     */
    @Test
    fun testIsPipDependencyPresent() {
        var futureResult: Future<Boolean>? = null
        assertDoesNotThrow { futureResult = PythonUtil.isPipDependencyPresent(PythonPackage.PILLOW) }
        while (!futureResult!!.isDone) Thread.onSpinWait()
        assertNotNull(futureResult)
        val result = futureResult!!.get()
        println("Pillow present: $result")

    }

    /**
     * Tests for the get pip dependency version method.
     */
    @Test
    fun testGetPipDependencyVersion() {
        var futureOptionalVersion: Future<Optional<String>>? = null
        assertDoesNotThrow { futureOptionalVersion = PythonUtil.getPipDependencyVersion(PythonPackage.PILLOW) }
        while (!futureOptionalVersion!!.isDone) Thread.onSpinWait()
        val optionalVersion = futureOptionalVersion!!.get()
        assertTrue(optionalVersion.isPresent)
        println("Pillow version: ${optionalVersion.get()}")
    }
}