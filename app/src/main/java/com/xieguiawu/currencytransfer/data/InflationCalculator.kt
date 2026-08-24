package com.xieguiawu.currencytransfer.data

import kotlinx.serialization.Serializable

/**
 * A single annual CPI observation from the World Bank.
 * [value] is the consumer price index (2010 = 100).
 */
@Serializable
data class CpiPoint(
    val year: Int,
    val value: Double,
)

/**
 * Pure inflation math. All functions return null when the input
 * cannot produce a meaningful result (empty data, invalid years).
 */
object InflationCalculator {

    /**
     * Finds the data point that best represents [year]:
     * - the latest point with year <= [year], or
     * - the earliest point overall when [year] predates all data.
     * CPI for the current year is often unpublished, so the most
     * recent available value is the correct proxy.
     */
    fun nearestCpi(cpi: List<CpiPoint>, year: Int): CpiPoint? {
        if (cpi.isEmpty()) return null
        val valid = cpi.filter { it.value > 0 }
        if (valid.isEmpty()) return null
        return valid
            .filter { it.year <= year }
            .maxByOrNull { it.year }
            ?: valid.minByOrNull { it.year }
    }

    /**
     * Cumulative inflation from [fromYear] to [toYear], in percent.
     * Example: 10.0 means prices rose 10% over the period.
     */
    fun cumulativeInflation(cpi: List<CpiPoint>, fromYear: Int, toYear: Int): Double? {
        if (fromYear >= toYear) return null
        val from = nearestCpi(cpi, fromYear) ?: return null
        val to = nearestCpi(cpi, toYear) ?: return null
        if (from.year == to.year) return null
        return (to.value / from.value - 1.0) * 100.0
    }

    /**
     * Average annual inflation rate over the period, in percent.
     * This is the CAGR of the price index.
     */
    fun annualRate(cpi: List<CpiPoint>, fromYear: Int, toYear: Int): Double? {
        if (fromYear >= toYear) return null
        val from = nearestCpi(cpi, fromYear) ?: return null
        val to = nearestCpi(cpi, toYear) ?: return null
        if (from.year == to.year) return null
        val years = (to.year - from.year).toDouble()
        return (Math.pow(to.value / from.value, 1.0 / years) - 1.0) * 100.0
    }

    /**
     * Amount of money in [toYear] needed to match [amount] in [fromYear],
     * given the same purchasing power.
     */
    fun purchasingPower(
        cpi: List<CpiPoint>,
        fromYear: Int,
        toYear: Int,
        amount: Double,
    ): Double? {
        if (amount <= 0.0) return null
        if (fromYear >= toYear) return null
        val from = nearestCpi(cpi, fromYear) ?: return null
        val to = nearestCpi(cpi, toYear) ?: return null
        if (from.year == to.year) return null
        return amount * to.value / from.value
    }
}
