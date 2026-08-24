package com.xieguiawu.currencytransfer

import com.xieguiawu.currencytransfer.data.CpiCache
import com.xieguiawu.currencytransfer.data.CpiPoint
import com.xieguiawu.currencytransfer.data.WorldBankApi
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Parses a real World Bank FP.CPI.TOTL response captured on 2026-08-24.
 * Guards against API contract drift.
 */
class WorldBankApiTest {

    private fun sample(): String =
        File("src/test/resources/wb_cpi_usa.json").readText()

    @Test
    fun parseCpi_realResponse_returnsPoints() {
        val points = WorldBankApi().parseCpi(sample())
        assertTrue("expected ~35 points, got ${points.size}", points.size in 30..40)
    }

    @Test
    fun parseCpi_skipsNullValues() {
        val points = WorldBankApi().parseCpi(sample())
        // 2025 is null in the captured response; it must be skipped
        assertTrue(points.none { it.year == 2025 })
        // But 2024 (latest published) must be present
        assertTrue(points.any { it.year == 2024 })
    }

    @Test
    fun parseCpi_knownUsaValue() {
        val points = WorldBankApi().parseCpi(sample())
        val y2024 = points.first { it.year == 2024 }
        assertEquals(143.857336014608, y2024.value, 0.0001)
    }

    @Test
    fun parseCpi_yearsAreSortedDescending() {
        val points = WorldBankApi().parseCpi(sample())
        points.zipWithNext().forEach { (a, b) ->
            assertTrue("not descending: ${a.year} < ${b.year}", a.year > b.year)
        }
    }

    @Test
    fun parseCpi_spansFrom1990() {
        val points = WorldBankApi().parseCpi(sample())
        assertTrue(points.any { it.year == 1990 })
    }

    @Test
    fun parseCpi_negativeRatesAreKept() {
        // Annual inflation series (ZG) may contain deflation years
        val body = File("src/test/resources/wb_cpi_emu_rates.json").readText()
        val points = WorldBankApi().parseCpi(body)
        assertTrue("expected some negative rates in EMU series", points.any { it.value < 0.0 })
        assertEquals(36, points.size)
    }

    @Test
    fun rebuildIndex_chainIsMultiplicative() {
        val api = WorldBankApi()
        val rates = listOf(
            com.xieguiawu.currencytransfer.data.CpiPoint(1991, 5.0),
            com.xieguiawu.currencytransfer.data.CpiPoint(1990, 10.0),
            com.xieguiawu.currencytransfer.data.CpiPoint(1992, -2.0),
        )
        val index = api.rebuildIndex(rates)
        assertEquals(listOf(1990, 1991, 1992), index.map { it.year })
        assertEquals(110.0, index[0].value, 1e-9)   // 100 * 1.10
        assertEquals(115.5, index[1].value, 1e-9)   // 110 * 1.05
        assertEquals(113.19, index[2].value, 1e-9)  // 115.5 * 0.98
    }

    @Test
    fun rebuildIndex_ratiosPreserved() {
        // cumulative inflation 1990->1992 = 113.19/110 - 1 = 2.9%
        // (the 100 anchor predates the series and cancels out)
        val api = WorldBankApi()
        val rates = listOf(
            com.xieguiawu.currencytransfer.data.CpiPoint(1990, 10.0),
            com.xieguiawu.currencytransfer.data.CpiPoint(1991, 5.0),
            com.xieguiawu.currencytransfer.data.CpiPoint(1992, -2.0),
        )
        val index = api.rebuildIndex(rates)
        val cum = com.xieguiawu.currencytransfer.data.InflationCalculator
            .cumulativeInflation(index, 1990, 1992)
        assertEquals(2.9, cum!!, 1e-9) // 113.19/110 - 1
    }

    // --- cache + network behaviour (MockWebServer) ---

    private lateinit var server: MockWebServer

    @Before
    fun setUpServer() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDownServer() {
        server.shutdown()
    }

    private fun api(cache: CpiCache?): WorldBankApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
        val baseUrl = server.url("/").toString().removeSuffix("/")
        return WorldBankApi(client = client, cache = cache, baseUrl = baseUrl)
    }

    private fun enqueueUsaIndex() {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(File("src/test/resources/wb_cpi_usa.json").readText()),
        )
    }

    /** In-memory cache whose fresh/any state can be controlled per test. */
    private class MemoryCpiCache : CpiCache {
        private var stored: List<CpiPoint>? = null
        override fun getFresh(iso3: String): List<CpiPoint>? = stored
        override fun getAny(iso3: String): List<CpiPoint>? = stored
        override fun put(iso3: String, points: List<CpiPoint>) { stored = points }
    }

    /** Cache that only serves stale data (fresh = miss, any = hit). */
    private class StaleOnlyCpiCache(private val stored: List<CpiPoint>) : CpiCache {
        override fun getFresh(iso3: String): List<CpiPoint>? = null
        override fun getAny(iso3: String): List<CpiPoint>? = stored
        override fun put(iso3: String, points: List<CpiPoint>) {}
    }

    @Test
    fun fetchCpi_cacheHit_skipsNetwork() {
        val cached = listOf(CpiPoint(2024, 143.8))
        val cache = MemoryCpiCache().apply { put("USA", cached) }
        val result = runBlocking { api(cache).fetchCpi("USA") }
        assertEquals(cached, result)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun fetchCpi_cacheMiss_fetchesAndStores() {
        enqueueUsaIndex()
        val cache = MemoryCpiCache()
        val api = api(cache)
        val result = runBlocking { api.fetchCpi("USA") }
        assertTrue(result.isNotEmpty())
        assertNotNull(cache.getFresh("USA"))
        assertEquals(1, server.requestCount)
        // Second call is served from cache - no second network request.
        runBlocking { api.fetchCpi("USA") }
        assertEquals(1, server.requestCount)
    }

    @Test
    fun fetchCpi_networkError_servesStaleCache() {
        server.enqueue(MockResponse().setResponseCode(500).setBody("boom"))
        val stale = listOf(CpiPoint(2024, 143.8))
        val result = runBlocking { api(StaleOnlyCpiCache(stale)).fetchCpi("USA") }
        assertEquals(stale, result)
    }

    @Test
    fun fetchCpi_networkError_withoutCache_throws() {
        server.enqueue(MockResponse().setResponseCode(500).setBody("boom"))
        val ex = assertThrows(IOException::class.java) {
            runBlocking { api(null).fetchCpi("USA") }
        }
        assertTrue(ex.message?.contains("HTTP 500") == true)
    }

    @Test
    fun fetchCpi_emuFallback_rebuiltAndCached() {
        // First request: index series is all-null for EMU -> fallback to rates.
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"page":1,"pages":1,"per_page":200,"total":36,"sourceid":"2","lastupdated":"2026-07-13"},[{"indicator":{"id":"FP.CPI.TOTL","value":"Consumer price index (2010 = 100)"},"country":{"id":"XC","value":"Euro area"},"countryiso3code":"EMU","date":"2025","value":null,"unit":"","obs_status":"","decimal":1}]]""",
            ),
        )
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(File("src/test/resources/wb_cpi_emu_rates.json").readText()),
        )
        val cache = MemoryCpiCache()
        val result = runBlocking { api(cache).fetchCpi("EMU") }
        assertEquals(36, result.size)
        assertEquals(2, server.requestCount)
        assertEquals(36, cache.getFresh("EMU")?.size)
    }
}
