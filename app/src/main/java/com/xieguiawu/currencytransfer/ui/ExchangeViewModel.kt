package com.xieguiawu.currencytransfer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xieguiawu.currencytransfer.data.ExchangeRateApi
import com.xieguiawu.currencytransfer.data.ExchangeRateSource
import com.xieguiawu.currencytransfer.data.ExchangeRates
import java.io.IOException
import kotlinx.coroutines.launch

data class ExchangeUiState(
    val loading: Boolean = false,
    val rates: ExchangeRates? = null,
    val error: String? = null,
)

class ExchangeViewModel(
    private val source: ExchangeRateSource = ExchangeRateApi(),
) : ViewModel() {

    var state by mutableStateOf(ExchangeUiState())
        private set

    init {
        load()
    }

    fun load() {
        if (state.loading) return
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val rates = source.fetchRates()
                state = ExchangeUiState(loading = false, rates = rates)
            } catch (e: IOException) {
                state = ExchangeUiState(loading = false, error = friendlyMessage(e))
            } catch (e: Exception) {
                state = ExchangeUiState(loading = false, error = "Failed to load rates. Check your connection.")
            }
        }
    }

    private fun friendlyMessage(e: IOException): String = when {
        e.message?.contains("HTTP") == true -> "Rate service unavailable (${e.message}). Try again later."
        else -> "Network error. Check your connection and retry."
    }
}
