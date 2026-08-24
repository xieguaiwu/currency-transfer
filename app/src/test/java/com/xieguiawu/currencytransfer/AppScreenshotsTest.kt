package com.xieguiawu.currencytransfer

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xieguiawu.currencytransfer.data.CpiPoint
import com.xieguiawu.currencytransfer.data.CpiSource
import com.xieguiawu.currencytransfer.data.ExchangeRateSource
import com.xieguiawu.currencytransfer.data.ExchangeRates
import com.xieguiawu.currencytransfer.ui.ExchangeScreen
import com.xieguiawu.currencytransfer.ui.ExchangeViewModel
import com.xieguiawu.currencytransfer.ui.InflationScreen
import com.xieguiawu.currencytransfer.ui.InflationViewModel
import com.xieguiawu.currencytransfer.ui.theme.CurrencyTransferTheme
import org.junit.Rule
import org.junit.Test

/** Screenshots of the live UI (JVM-rendered via Paparazzi). */
class AppScreenshotsTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.Light.NoActionBar",
    )

    private class FakeExchangeSource : ExchangeRateSource {
        override suspend fun fetchRates(base: String): ExchangeRates = ExchangeRates(
            baseCode = "USD",
            updatedUtc = "Mon, 24 Aug 2026 00:02:31 +0000",
            rates = mapOf(
                "USD" to 1.0, "EUR" to 0.82, "CNY" to 7.12, "JPY" to 145.6,
                "GBP" to 0.71, "CHF" to 0.84, "AUD" to 1.39, "CAD" to 1.26,
                "SEK" to 10.2, "KRW" to 1330.0, "INR" to 83.5, "BRL" to 4.9,
                "THB" to 31.8, "SGD" to 1.28, "NOK" to 10.5, "MXN" to 18.9,
            ),
        )
    }

    private class FakeCpiSource : CpiSource {
        override suspend fun fetchCpi(iso3: String): List<CpiPoint> = listOf(
            CpiPoint(2024, 143.86), CpiPoint(2023, 139.74), CpiPoint(2022, 133.61),
            CpiPoint(2021, 125.62), CpiPoint(2020, 120.33), CpiPoint(2019, 117.64),
            CpiPoint(2018, 114.94), CpiPoint(2017, 112.32), CpiPoint(2016, 110.36),
            CpiPoint(2015, 108.44), CpiPoint(2014, 106.41), CpiPoint(2013, 104.14),
            CpiPoint(2012, 101.93), CpiPoint(2011, 99.76), CpiPoint(2010, 95.71),
        )
    }

    @Test
    fun exchangeScreen() {
        val vm = ExchangeViewModel(FakeExchangeSource())
        paparazzi.snapshot {
            CurrencyTransferTheme {
                AppRoot { ExchangeScreen(Modifier.fillMaxSize(), vm) }
            }
        }
    }

    @Test
    fun inflationScreen() {
        val vm = InflationViewModel(FakeCpiSource())
        paparazzi.snapshot {
            CurrencyTransferTheme {
                AppRoot { InflationScreen(Modifier.fillMaxSize(), vm) }
            }
        }
    }

    /** Matches the real window background (Scaffold container color). */
    @Composable
    private fun AppRoot(content: @Composable () -> Unit) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            content = { content() },
        )
    }
}
