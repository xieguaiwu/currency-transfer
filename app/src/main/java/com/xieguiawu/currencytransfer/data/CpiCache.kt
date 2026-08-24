package com.xieguiawu.currencytransfer.data

import java.io.File
import java.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Cache for CPI series. Annual data changes slowly, so a fresh entry avoids
 * repeated slow World Bank requests; a stale entry still beats an error.
 */
interface CpiCache {

    /** Returns the cached series when fresh (within TTL), else null. */
    fun getFresh(iso3: String): List<CpiPoint>?

    /** Returns any cached series regardless of age (offline fallback), else null. */
    fun getAny(iso3: String): List<CpiPoint>?

    /** Stores [points] for [iso3] with the current time as its fetch time. */
    fun put(iso3: String, points: List<CpiPoint>)
}

@Serializable
private data class CacheEntry(
    val fetchedAt: Long,
    val points: List<CpiPoint>,
)

/**
 * File-backed CPI cache, one JSON file per ISO3 code under [dir].
 *
 * - Writes are atomic: temp file + rename.
 * - Corrupt files are treated as a miss and deleted.
 * - Cache I/O failures never propagate - the app keeps working without caching.
 */
class DiskCpiCache(
    private val dir: File,
    private val ttlMillis: Long = DEFAULT_TTL_MILLIS,
    private val clock: () -> Long = System::currentTimeMillis,
    private val json: Json = ApiClient.json,
) : CpiCache {

    @Synchronized
    override fun getFresh(iso3: String): List<CpiPoint>? {
        val entry = read(iso3) ?: return null
        return if (clock() - entry.fetchedAt <= ttlMillis) entry.points else null
    }

    @Synchronized
    override fun getAny(iso3: String): List<CpiPoint>? = read(iso3)?.points

    @Synchronized
    override fun put(iso3: String, points: List<CpiPoint>) {
        try {
            val file = fileFor(iso3)
            file.parentFile?.mkdirs()
            val encoded = json.encodeToString(CacheEntry.serializer(), CacheEntry(clock(), points))
            val tmp = File(file.parentFile, "${file.name}.tmp")
            tmp.writeText(encoded)
            if (!tmp.renameTo(file)) {
                // rename failed (e.g. target exists on some filesystems): overwrite directly
                file.writeText(encoded)
                tmp.delete()
            }
        } catch (e: IOException) {
            // Cache failures must never break the app.
        }
    }

    private fun read(iso3: String): CacheEntry? {
        val file = fileFor(iso3)
        if (!file.isFile) return null
        return try {
            json.decodeFromString(CacheEntry.serializer(), file.readText())
        } catch (e: Exception) {
            file.delete() // corrupt entry - treat as a miss
            null
        }
    }

    private fun fileFor(iso3: String): File = File(dir, "$iso3.json")

    companion object {
        /** CPI is published annually; 7 days is plenty of freshness. */
        const val DEFAULT_TTL_MILLIS = 7L * 24 * 60 * 60 * 1000
    }
}
