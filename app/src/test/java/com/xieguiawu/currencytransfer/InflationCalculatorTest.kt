package com.xieguiawu.currencytransfer

import com.xieguiawu.currencytransfer.data.CpiPoint
import com.xieguiawu.currencytransfer.data.InflationCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InflationCalculatorTest {

    private val ascending = listOf(
        CpiPoint(2019, 105.0),
        CpiPoint(2020, 110.0),
        CpiPoint(2021, 114.0),
        CpiPoint(2022, 118.0),
        CpiPoint(2023, 121.0),
        CpiPoint(2024, 124.0),
    )

    // World Bank API returns descending order
    private val descending = ascending.reversed()

    @Test
    fun cumulativeInflation_simpleYears() {
        val result = InflationCalculator.cumulativeInflation(ascending, 2020, 2024)
        assertEquals(12.7272, result!!, 0.0001) // 124/110 - 1 = 12.727%
    }

    @Test
    fun cumulativeInflation_orderIndependent() {
        assertEquals(
            InflationCalculator.cumulativeInflation(ascending, 2020, 2024),
            InflationCalculator.cumulativeInflation(descending, 2020, 2024),
        )
    }

    @Test
    fun cumulativeInflation_missingYearUsesNearestAvailable() {
        // Data starts at 2019; from=2018 falls back to 2019 (105.0)
        val result = InflationCalculator.cumulativeInflation(ascending, 2018, 2024)
        assertEquals(18.0952, result!!, 0.0001) // 124/105 - 1
    }

    @Test
    fun cumulativeInflation_fromYearAfterLastPoint() {
        // 2025 not in data; nearest <= 2025 is 2024 (124.0)
        val result = InflationCalculator.cumulativeInflation(ascending, 2020, 2025)
        assertEquals(12.7272, result!!, 0.0001)
    }

    @Test
    fun cumulativeInflation_fromGreaterThanTo_returnsNull() {
        assertNull(InflationCalculator.cumulativeInflation(ascending, 2024, 2020))
    }

    @Test
    fun cumulativeInflation_equalYears_returnsNull() {
        assertNull(InflationCalculator.cumulativeInflation(ascending, 2022, 2022))
    }

    @Test
    fun cumulativeInflation_emptyList_returnsNull() {
        assertNull(InflationCalculator.cumulativeInflation(emptyList(), 2020, 2024))
    }

    @Test
    fun cumulativeInflation_fromYearBeforeDataStarts_usesEarliestData() {
        // from=2000 predates all data; fall back to the earliest point (2019, 105.0)
        val result = InflationCalculator.cumulativeInflation(ascending, 2000, 2024)
        assertEquals(18.0952, result!!, 0.0001) // 124/105 - 1
    }

    @Test
    fun annualRate_knownValues() {
        // 110 -> 124 over 4 years: ((124/110)^(1/4) - 1) * 100
        val result = InflationCalculator.annualRate(ascending, 2020, 2024)
        assertEquals(3.0403, result!!, 0.0001)
    }

    @Test
    fun annualRate_singleYear_givesDirectGrowth() {
        // 110 -> 114 over 1 year: 3.636%
        val result = InflationCalculator.annualRate(ascending, 2020, 2021)
        assertEquals(3.6363, result!!, 0.0001)
    }

    @Test
    fun annualRate_invalidYears_returnsNull() {
        assertNull(InflationCalculator.annualRate(ascending, 2024, 2020))
        assertNull(InflationCalculator.annualRate(ascending, 2020, 2020))
        assertNull(InflationCalculator.annualRate(emptyList(), 2020, 2024))
    }

    @Test
    fun purchasingPower_simple() {
        // 100 in 2020 equals 124/110*100 in 2024
        val result = InflationCalculator.purchasingPower(ascending, 2020, 2024, 100.0)
        assertEquals(112.7272, result!!, 0.0001)
    }

    @Test
    fun purchasingPower_zeroAmount_returnsNull() {
        assertNull(InflationCalculator.purchasingPower(ascending, 2020, 2024, 0.0))
    }

    @Test
    fun purchasingPower_negativeAmount_returnsNull() {
        assertNull(InflationCalculator.purchasingPower(ascending, 2020, 2024, -5.0))
    }

    @Test
    fun purchasingPower_deflation_lessThanOne() {
        // 100 in 2024 equals 110/124*100 in 2020 (deflation case)
        val result = InflationCalculator.purchasingPower(ascending, 2024, 2020, 100.0)
        assertNull(result) // fromYear > toYear is invalid
    }

    @Test
    fun nearestCpi_exactYear() {
        val p = InflationCalculator.nearestCpi(ascending, 2022)
        assertEquals(2022, p!!.year)
        assertEquals(118.0, p.value, 0.0)
    }

    @Test
    fun nearestCpi_fallsBackToEarlierYear() {
        val p = InflationCalculator.nearestCpi(ascending, 2017)
        assertEquals(2019, p!!.year) // oldest available
    }

    @Test
    fun nearestCpi_futureYearUsesLatest() {
        val p = InflationCalculator.nearestCpi(ascending, 2030)
        assertEquals(2024, p!!.year)
    }

    @Test
    fun nearestCpi_emptyList_returnsNull() {
        assertNull(InflationCalculator.nearestCpi(emptyList(), 2020))
    }
}
