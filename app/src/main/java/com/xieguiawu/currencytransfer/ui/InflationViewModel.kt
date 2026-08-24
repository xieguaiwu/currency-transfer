package com.xieguiawu.currencytransfer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xieguiawu.currencytransfer.data.CpiPoint
import com.xieguiawu.currencytransfer.data.CpiSource
import com.xieguiawu.currencytransfer.data.WorldBankApi
import java.io.IOException
import kotlinx.coroutines.launch

data class InflationUiState(
    val loading: Boolean = false,
    val cpi: List<CpiPoint>? = null,
    val error: String? = null,
    val loadedIso3: String? = null,
)

class InflationViewModel(
    private val source: CpiSource = WorldBankApi(),
) : ViewModel() {

    var state by mutableStateOf(InflationUiState())
        private set

    /** Fetches CPI for [iso3]; no-op when data for that country is already loaded. */
    fun load(iso3: String) {
        if (state.loading || state.loadedIso3 == iso3) return
        state = InflationUiState(loading = true)
        viewModelScope.launch {
            try {
                val cpi = source.fetchCpi(iso3)
                state = InflationUiState(loading = false, cpi = cpi, loadedIso3 = iso3)
            } catch (e: IOException) {
                state = InflationUiState(loading = false, error = friendlyMessage(e))
            } catch (e: Exception) {
                state = InflationUiState(
                    loading = false,
                    error = "Failed to load inflation data. Check your connection.",
                )
            }
        }
    }

    private fun friendlyMessage(e: IOException): String = when {
        e.message?.contains("HTTP") == true -> "Data service unavailable (${e.message}). Try again later."
        else -> "Network error. Check your connection and retry."
    }
}
