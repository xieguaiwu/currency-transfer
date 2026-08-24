package com.xieguiawu.currencytransfer

import com.xieguiawu.currencytransfer.data.CpiCache
import com.xieguiawu.currencytransfer.data.CpiPoint
import com.xieguiawu.currencytransfer.data.DiskCpiCache
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DiskCpiCacheTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun cache(
        ttlMillis: Long = DiskCpiCache.DEFAULT_TTL_MILLIS,
        clock: () -> Long = { 0L },
    ) = DiskCpiCache(tmp.root, ttlMillis, clock)

    @Test
    fun roundTrip_persistsPoints() {
        val c = cache()
        c.put("USA", listOf(CpiPoint(2024, 143.8), CpiPoint(2023, 139.7)))
        assertEquals(listOf(CpiPoint(2024, 143.8), CpiPoint(2023, 139.7)), c.getFresh("USA"))
    }

    @Test
    fun getFresh_expiredEntry_returnsNull() {
        var now = 0L
        val c = cache(ttlMillis = 1000) { now }
        c.put("USA", listOf(CpiPoint(2024, 143.8)))
        now = 1001
        assertNull(c.getFresh("USA"))
    }

    @Test
    fun getAny_returnsExpiredEntry() {
        var now = 0L
        val c = cache(ttlMillis = 1000) { now }
        c.put("USA", listOf(CpiPoint(2024, 143.8)))
        now = 999_999
        assertNotNull(c.getAny("USA"))
    }

    @Test
    fun missingIso3_returnsNull() {
        assertNull(cache().getFresh("CHN"))
        assertNull(cache().getAny("CHN"))
    }

    @Test
    fun iso3AreIsolated() {
        val c = cache()
        c.put("USA", listOf(CpiPoint(2024, 1.0)))
        assertNull(c.getFresh("CHN"))
    }

    @Test
    fun emptySeries_isCached() {
        val c = cache()
        c.put("TWN", emptyList())
        assertEquals(emptyList<CpiPoint>(), c.getFresh("TWN"))
    }

    @Test
    fun corruptFile_treatedAsMiss() {
        val c = cache()
        c.put("USA", listOf(CpiPoint(2024, 1.0)))
        File(tmp.root, "USA.json").writeText("{not json")
        assertNull(c.getFresh("USA"))
        assertNull(c.getAny("USA"))
    }

    @Test
    fun survivesAcrossInstances() {
        cache().put("USA", listOf(CpiPoint(2024, 143.8)))
        val reopened = cache()
        assertEquals(listOf(CpiPoint(2024, 143.8)), reopened.getFresh("USA"))
    }
}
