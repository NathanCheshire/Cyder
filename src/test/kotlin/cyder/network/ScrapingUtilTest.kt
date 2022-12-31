package cyder.network

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Tests for [ScrapingUtil]s.
 */
class ScrapingUtilTest {
    /**
     * Tests for the get isp and network details method.
     */
    @Test
    fun testGetIspAndNetworkDetails() {
        var result: ScrapingUtil.IspQueryResult? = null
        assertDoesNotThrow { result = ScrapingUtil.getIspAndNetworkDetails() }
        println(result)
        assertNotNull(result!!.isp)
        assertNotNull(result!!.city)
        assertNotNull(result!!.country)
        assertNotNull(result!!.hostname)
        assertNotNull(result!!.state)
    }
}