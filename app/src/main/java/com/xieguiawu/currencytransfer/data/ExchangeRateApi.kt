package com.xieguiawu.currencytransfer.data

import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Request

/** Latest exchange rates relative to a base currency. */
data class ExchangeRates(
    val baseCode: String,
    val updatedUtc: String,
    val rates: Map<String, Double>,
) {
    /** Rate for converting 1 [code] into [baseCode]. */
    fun rateOf(code: String): Double? = rates[code]
}

@Serializable
private data class ExchangeRatesResponse(
    val result: String,
    @SerialName("base_code") val baseCode: String = "",
    @SerialName("time_last_update_utc") val timeLastUpdateUtc: String = "",
    val rates: Map<String, Double> = emptyMap(),
)

/**
 * Client for open.er-api.com — free, keyless, ~160 currencies.
 * Documented at https://www.exchangerate-api.com/docs/free
 */
class ExchangeRateApi(private val json: Json = ApiClient.json) {

    suspend fun fetchRates(base: String = "USD"): ExchangeRates = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://open.er-api.com/v6/latest/$base")
            .build()
        val body = ApiClient.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Exchange rate API returned HTTP ${response.code}")
            }
            response.body?.string() ?: throw IOException("Empty response from exchange rate API")
        }
        parseRates(body)
    }

    /** Visible for testing: parses a raw exchange-rate API response body. */
    fun parseRates(body: String): ExchangeRates {
        val parsed = json.decodeFromString<ExchangeRatesResponse>(body)
        if (parsed.result != "success") {
            throw IOException("Exchange rate API error: ${parsed.result}")
        }
        return ExchangeRates(parsed.baseCode, parsed.timeLastUpdateUtc, parsed.rates)
    }
}
