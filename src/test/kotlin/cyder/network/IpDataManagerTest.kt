package cyder.network

import cyder.props.PropLoader
import cyder.props.Props
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
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
        assertNotNull(key)

        assertTrue(IpDataManager.INSTANCE.pullIpData(null).isEmpty)
        assertTrue(IpDataManager.INSTANCE.pullIpData("").isEmpty)
        assertTrue(IpDataManager.INSTANCE.pullIpData("random_key_that_does_not_exist").isEmpty)

        val ipData = IpDataManager.INSTANCE.pullIpData(key)
        assertTrue(ipData.isPresent)
        println(ipData.get())

        assertDoesNotThrow { IpDataManager.INSTANCE.ipData }
        println(IpDataManager.INSTANCE.ipData)
    }

    companion object {
        private var key: String? = null

        @BeforeAll
        @JvmStatic
        fun setup() {
            PropLoader.reloadProps()
            key = Props.ipKey.value
        }
    }
}