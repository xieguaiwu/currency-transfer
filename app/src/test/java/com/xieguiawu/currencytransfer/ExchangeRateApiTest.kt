package com.xieguiawu.currencytransfer

import com.xieguiawu.currencytransfer.data.ExchangeRateApi
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Parses a real open.er-api.com response captured on 2026-08-24.
 * Guards against API contract drift.
 */
class ExchangeRateApiTest {

    private fun sample(): String =
        File("src/test/resources/exchange_rates_usd.json").readText()

    @Test
    fun parseRates_realResponse() {
        val rates = ExchangeRateApi().parseRates(sample())
        assertEquals("USD", rates.baseCode)
        assertTrue(rates.rates.size >= 160)
        assertTrue("missing CNY", rates.rates.containsKey("CNY"))
        assertTrue("missing EUR", rates.rates.containsKey("EUR"))
        assertTrue("missing JPY", rates.rates.containsKey("JPY"))
    }

    @Test
    fun parseRates_valuesArePositive() {
        val rates = ExchangeRateApi().parseRates(sample())
        rates.rates.forEach { (code, value) ->
            assertTrue("non-positive rate for $code: $value", value > 0.0)
        }
    }

    @Test
    fun parseRates_usdIsOne() {
        val rates = ExchangeRateApi().parseRates(sample())
        assertEquals(1.0, rates.rateOf("USD")!!, 0.0)
    }

    @Test
    fun parseRates_timestampPresent() {
        val rates = ExchangeRateApi().parseRates(sample())
        assertNotNull(rates.updatedUtc)
        assertTrue(rates.updatedUtc.isNotBlank())
    }

    @Test
    fun parseRates_knownCrossRate() {
        // CNY per USD should be in a plausible band (e.g. 6-8 in 2026)
        val rates = ExchangeRateApi().parseRates(sample())
        val cny = rates.rateOf("CNY")!!
        assertTrue("implausible CNY rate: $cny", cny in 5.0..10.0)
    }
}
