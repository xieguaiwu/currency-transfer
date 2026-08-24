package com.xieguiawu.currencytransfer.data

import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
private data class WbDatum(
    val date: String? = null,
    val value: Double? = null,
)

/** Data source for annual consumer price index data. */
interface CpiSource {
    suspend fun fetchCpi(iso3: String): List<CpiPoint>
}

/**
 * Client for the World Bank Indicators API.
 * URL: https://api.worldbank.org/v2/country/{iso3}/indicator/{code}?format=json&per_page=200&date=1990:2026
 *
 * The response is a JSON array: [metadata, [observations]].
 * Observations arrive newest-first and may have null values
 * (current-year data is often unpublished).
 *
 * Primary series: FP.CPI.TOTL (consumer price index, 2010 = 100).
 * Some aggregates (Euro area) publish no index; we fall back to
 * FP.CPI.TOTL.ZG (annual inflation rate, %) and rebuild an index
 * from the rate chain. Both series are free and keyless.
 */
class WorldBankApi(
    private val json: Json = ApiClient.json,
    private val client: OkHttpClient = ApiClient.client,
    private val cache: CpiCache? = null,
    private val baseUrl: String = WORLD_BANK_BASE_URL,
) : CpiSource {

    override suspend fun fetchCpi(iso3: String): List<CpiPoint> = withContext(Dispatchers.IO) {
        cache?.getFresh(iso3)?.let { return@withContext it }
        try {
            fetchRemote(iso3).also { cache?.put(iso3, it) }
        } catch (e: IOException) {
            // Offline fallback: annual CPI does not meaningfully go stale.
            cache?.getAny(iso3)?.let { return@withContext it }
            throw e
        }
    }

    private suspend fun fetchRemote(iso3: String): List<CpiPoint> {
        val index = fetchIndicator(iso3, "FP.CPI.TOTL")
        if (index.isNotEmpty()) return index
        val rates = fetchIndicator(iso3, "FP.CPI.TOTL.ZG")
        if (rates.isEmpty()) return emptyList()
        return rebuildIndex(rates)
    }

    private suspend fun fetchIndicator(iso3: String, indicator: String): List<CpiPoint> {
        val request = Request.Builder()
            .url("$baseUrl/v2/country/$iso3/indicator/$indicator?format=json&per_page=200&date=1990:2026")
            .build()
        val body = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("World Bank API returned HTTP ${response.code}")
            }
            response.body?.string() ?: throw IOException("Empty response from World Bank API")
        }
        return parseCpi(body)
    }

    /** Visible for testing: parses a raw World Bank response body. */
    fun parseCpi(body: String): List<CpiPoint> {
        val root = json.parseToJsonElement(body).jsonArray
        if (root.size < 2) {
            throw IOException("Unexpected World Bank response shape")
        }
        return root[1].jsonArray.mapNotNull { datum ->
            val element = datum.jsonObject
            val date = element["date"]?.jsonPrimitive?.content?.toIntOrNull() ?: return@mapNotNull null
            val value = element["value"]?.let {
                if (it is JsonNull) null else (it as? JsonPrimitive)?.contentOrNull?.toDoubleOrNull()
            }
            if (value == null) null else CpiPoint(date, value)
        }
    }

    /**
     * Rebuilds a price index (anchored at 100 before the first year)
     * from a chain of annual inflation rates (percent).
     * The anchor cancels out in every ratio, so cumulative inflation,
     * annual rates, and purchasing power remain exact.
     */
    fun rebuildIndex(annualRates: List<CpiPoint>): List<CpiPoint> {
        val sorted = annualRates.sortedBy { it.year }
        var index = 100.0
        return sorted.mapNotNull { p ->
            val factor = 1.0 + p.value / 100.0
            if (factor <= 0.0) return@mapNotNull null // pathological deflation
            index *= factor
            CpiPoint(p.year, index)
        }
    }

    companion object {
        const val WORLD_BANK_BASE_URL = "https://api.worldbank.org"
    }
}
