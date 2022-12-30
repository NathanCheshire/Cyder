package cyder.network

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for the [IpDataManager].
 */
class IpDataManagerTest {
    /**
     * Tests for the get ip data method.
     */
    @Test
    fun testGetIpData() {
        assertTrue(IpDataManager.INSTANCE.pullIpData(null).isEmpty)
        assertTrue(IpDataManager.INSTANCE.pullIpData("").isEmpty)
        assertTrue(IpDataManager.INSTANCE.pullIpData("random_key_that_does_not_exist").isEmpty)

        // todo
        val ipData = IpDataManager.INSTANCE.pullIpData("actual key here")
        assertTrue(ipData.isPresent)
        println(ipData.get())

        assertDoesNotThrow { IpDataManager.INSTANCE.ipData }
        println(IpDataManager.INSTANCE.ipData)
    }
}