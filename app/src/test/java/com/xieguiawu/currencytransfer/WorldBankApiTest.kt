package com.xieguiawu.currencytransfer

import com.xieguiawu.currencytransfer.data.WorldBankApi
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
}
