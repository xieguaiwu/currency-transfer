package com.xieguiawu.currencytransfer.data

import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

/** Shared HTTP client: 15s timeouts, no cleartext traffic. */
object ApiClient {
    val json: Json = Json { ignoreUnknownKeys = true }

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
}
