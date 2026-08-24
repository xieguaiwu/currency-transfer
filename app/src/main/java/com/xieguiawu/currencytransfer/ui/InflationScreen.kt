package com.xieguiawu.currencytransfer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.xieguiawu.currencytransfer.data.CpiPoint
import com.xieguiawu.currencytransfer.data.Currencies
import com.xieguiawu.currencytransfer.data.DiskCpiCache
import com.xieguiawu.currencytransfer.data.InflationCalculator
import com.xieguiawu.currencytransfer.data.WorldBankApi
import java.io.File

private const val MIN_YEAR = 1990
private const val MAX_YEAR = 2026

/**
 * Default view model: World Bank CPI with a 7-day on-disk cache.
 * The cache survives restarts and serves the last known series offline.
 */
@Composable
private fun defaultInflationViewModel(): InflationViewModel {
    val appContext = LocalContext.current.applicationContext
    return viewModel(factory = viewModelFactory {
        initializer {
            InflationViewModel(
                source = WorldBankApi(
                    cache = DiskCpiCache(File(appContext.cacheDir, "cpi-cache")),
                ),
            )
        }
    })
}

/**
 * Tab 2: inflation between two years for a chosen currency.
 * Uses World Bank CPI (consumer price index, 2010 = 100).
 */
@Composable
fun InflationScreen(
    modifier: Modifier = Modifier,
    viewModel: InflationViewModel = defaultInflationViewModel(),
) {
    val state = viewModel.state
    var currency by rememberSaveable { mutableStateOf("USD") }
    var fromYearText by rememberSaveable { mutableStateOf("2015") }
    var toYearText by rememberSaveable { mutableStateOf("2025") }
    var amountText by rememberSaveable { mutableStateOf("100") }

    val iso3 = Currencies.iso3For(currency)
    val fromYear = fromYearText.toIntOrNull()
    val toYear = toYearText.toIntOrNull()

    // Fetch CPI when the currency (or its data) changes.
    LaunchedEffect(currency, iso3) {
        if (iso3 != null) viewModel.load(iso3)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Inflation Calculator", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Compare purchasing power between two years.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))

        val cpiCurrencies = Currencies.all.filter { it.countryIso3 != null }.map { it.code }
        CurrencyPicker(
            selected = currency,
            onSelect = { currency = it },
            allowedCodes = cpiCurrencies,
        )
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = fromYearText,
                onValueChange = { input -> fromYearText = input.filter { it.isDigit() }.take(4) },
                label = { Text("From year") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = fromYear != null && (fromYear < MIN_YEAR || fromYear > MAX_YEAR),
                supportingText = {
                    if (fromYear != null && (fromYear < MIN_YEAR || fromYear > MAX_YEAR)) {
                        Text("Year must be $MIN_YEAR-$MAX_YEAR")
                    }
                },
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.size(12.dp))
            OutlinedTextField(
                value = toYearText,
                onValueChange = { input -> toYearText = input.filter { it.isDigit() }.take(4) },
                label = { Text("To year") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = toYear != null && (toYear < MIN_YEAR || toYear > MAX_YEAR),
                supportingText = {
                    if (toYear != null && (toYear < MIN_YEAR || toYear > MAX_YEAR)) {
                        Text("Year must be $MIN_YEAR-$MAX_YEAR")
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() || it == '.' }
                if (filtered.count { it == '.' } <= 1) amountText = filtered
            },
            label = { Text("Amount in $currency") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))

        // Result area
        when {
            iso3 == null -> Text(
                "Inflation data is not available for this currency.",
                color = MaterialTheme.colorScheme.error,
            )
            state.loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(20.dp))
                Spacer(Modifier.size(12.dp))
                Text("Loading CPI data...")
            }
            state.error != null -> Column {
                Text(state.error, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { iso3.let { viewModel.load(it) } },
                    shape = MaterialTheme.shapes.small,
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Retry")
                }
            }
            state.cpi != null -> {
                val cpi = state.cpi
                if (cpi.isEmpty()) {
                    Text(
                        "No CPI data returned for ${Currencies.displayName(currency)}.",
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    InflationResultCard(
                        cpi = cpi,
                        currency = currency,
                        fromYear = fromYear,
                        toYear = toYear,
                        amount = amountText.toDoubleOrNull(),
                    )
                }
            }
        }
    }
}

@Composable
private fun InflationResultCard(
    cpi: List<CpiPoint>,
    currency: String,
    fromYear: Int?,
    toYear: Int?,
    amount: Double?,
) {
    val validYears = fromYear != null && toYear != null && fromYear < toYear
    if (!validYears) {
        Text("Enter a valid year range (from year < to year).")
        return
    }

    val cumulative = InflationCalculator.cumulativeInflation(cpi, fromYear, toYear)
    val annual = InflationCalculator.annualRate(cpi, fromYear, toYear)
    val power = InflationCalculator.purchasingPower(cpi, fromYear, toYear, amount ?: 0.0)
    val actualFrom = InflationCalculator.nearestCpi(cpi, fromYear)
    val actualTo = InflationCalculator.nearestCpi(cpi, toYear)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Inflation $fromYear \u2192 $toYear ($currency)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(8.dp))
            if (cumulative == null || annual == null || power == null) {
                Text(
                    "Not enough data for this year range.",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                Text(
                    "Cumulative inflation: ${"%.1f".format(cumulative)}%",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Average per year: ${"%.2f".format(annual)}%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                amount?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${formatAmount(it)} $currency in $fromYear \u2248 ${formatAmount(power)} $currency in $toYear",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            actualFrom?.let { from -> actualTo?.let { to ->
                if (from.year != fromYear || to.year != toYear) {
                    Text(
                        "Note: based on available data for ${from.year} and ${to.year}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            } }
        }
    }
}

private fun formatAmount(value: Double): String =
    if (value >= 1000) "%,.2f".format(value) else "%.2f".format(value)
