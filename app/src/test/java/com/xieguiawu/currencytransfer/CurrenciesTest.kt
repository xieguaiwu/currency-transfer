package com.xieguiawu.currencytransfer

import com.xieguiawu.currencytransfer.data.Currencies
import com.xieguiawu.currencytransfer.data.CurrencyInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrenciesTest {

    @Test
    fun all_containsAtLeast60Currencies() {
        assertTrue("expected >= 60 currencies, got ${Currencies.all.size}", Currencies.all.size >= 60)
    }

    @Test
    fun all_codesAreUniqueAndUpperCase() {
        val codes = Currencies.all.map { it.code }
        assertEquals("duplicate codes", codes.size, codes.toSet().size)
        codes.forEach { code ->
            assertEquals("code not uppercase: $code", code, code.uppercase())
        }
    }

    @Test
    fun all_sortedByCode() {
        val codes = Currencies.all.map { it.code }
        assertEquals(codes.sorted(), codes)
    }

    @Test
    fun iso3For_majorCurrencies() {
        assertEquals("USA", Currencies.iso3For("USD"))
        assertEquals("EMU", Currencies.iso3For("EUR"))
        assertEquals("CHN", Currencies.iso3For("CNY"))
        assertEquals("JPN", Currencies.iso3For("JPY"))
        assertEquals("GBR", Currencies.iso3For("GBP"))
        assertEquals("AUS", Currencies.iso3For("AUD"))
        assertEquals("CAN", Currencies.iso3For("CAD"))
        assertEquals("IND", Currencies.iso3For("INR"))
        assertEquals("BRA", Currencies.iso3For("BRL"))
    }

    @Test
    fun iso3For_unknownCurrency_returnsNull() {
        assertNull(Currencies.iso3For("XXX"))
        assertNull(Currencies.iso3For(""))
    }

    @Test
    fun iso3For_multiCountryCurrencies_areNull() {
        // XOF (West African CFA) is used by several countries - no single CPI series
        assertNull(Currencies.iso3For("XOF"))
        assertNull(Currencies.iso3For("XCD"))
        assertNull(Currencies.iso3For("BTC"))
    }

    @Test
    fun iso3For_countriesWithoutWorldBankCpi_areNull() {
        // Verified live 2026-08-25: World Bank publishes neither FP.CPI.TOTL nor
        // FP.CPI.TOTL.ZG for these ISO3 codes. Null keeps them out of the
        // inflation picker instead of showing a permanent "no data" dead state.
        assertNull(Currencies.iso3For("TWD")) // Taiwan
        assertNull(Currencies.iso3For("CUP")) // Cuba
        assertNull(Currencies.iso3For("SOS")) // Somalia
        assertNull(Currencies.iso3For("TMT")) // Turkmenistan
        assertNull(Currencies.iso3For("ERN")) // Eritrea
    }

    @Test
    fun displayName_knownCurrency() {
        val name = Currencies.displayName("USD")
        assertTrue("unexpected name: $name", name.contains("US") || name.contains("Dollar"))
    }

    @Test
    fun displayName_unknownReturnsCode() {
        assertEquals("ZZZ", Currencies.displayName("ZZZ"))
    }

    @Test
    fun everyInfoHasNonBlankFields() {
        Currencies.all.forEach { info: CurrencyInfo ->
            assertTrue("blank code", info.code.isNotBlank())
            assertTrue("blank name for ${info.code}", info.name.isNotBlank())
        }
    }

    @Test
    fun euroAreaMapsToEmu() {
        // World Bank uses EMU for Euro area CPI
        assertNotNull(Currencies.iso3For("EUR"))
    }
}
