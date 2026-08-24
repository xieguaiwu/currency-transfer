package com.xieguiawu.currencytransfer.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xieguiawu.currencytransfer.data.Currencies

/**
 * Tab 1: live currency conversion.
 * Converts an amount from one currency into another using
 * rates fetched from open.er-api.com.
 */
@Composable
fun ExchangeScreen(
    modifier: Modifier = Modifier,
    viewModel: ExchangeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val state = viewModel.state
    var from by rememberSaveable { mutableStateOf("USD") }
    var to by rememberSaveable { mutableStateOf("CNY") }
    var amountText by rememberSaveable { mutableStateOf("100") }
    val amount = amountText.toDoubleOrNull() ?: 0.0

    val rateFrom = state.rates?.rateOf(from) ?: 1.0
    val rateTo = state.rates?.rateOf(to) ?: 1.0
    val converted = if (state.rates == null) null else amount * rateTo / rateFrom

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Currency Exchange", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Live rates for 160+ currencies.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))

        // From row
        OutlinedTextField(
            value = amountText,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() || it == '.' }
                if (filtered.count { it == '.' } <= 1) amountText = filtered
            },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        CurrencyPicker(selected = from, onSelect = { from = it })

        // Swap button
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            IconButton(onClick = {
                val tmp = from; from = to; to = tmp
            }) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = "Swap currencies")
            }
        }

        // To row
        CurrencyPicker(selected = to, onSelect = { to = it })

        Spacer(Modifier.height(20.dp))

        // Result card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Result",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(8.dp))
                when {
                    state.loading -> Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(20.dp))
                        Spacer(Modifier.size(12.dp))
                        Text("Loading rates...")
                    }
                    state.error != null -> Text(
                        state.error,
                        color = MaterialTheme.colorScheme.error,
                    )
                    converted != null -> {
                        Text(
                            "${formatAmount(amount)} $from",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text("=", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${formatAmount(converted)} $to",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "1 $from = ${formatRate(rateTo / rateFrom)} $to",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            "1 $to = ${formatRate(rateFrom / rateTo)} $from",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Footer: refresh + timestamp
        Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Button(onClick = { viewModel.load() }) {
                Icon(Icons.Filled.Refresh, contentDescription = null, Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text("Refresh")
            }
            Spacer(Modifier.weight(1f))
            state.rates?.let { rates ->
                Text(
                    "Updated: ${rates.updatedUtc}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatAmount(value: Double): String =
    if (value >= 1000) "%,.2f".format(value) else "%.2f".format(value)

private fun formatRate(value: Double): String =
    if (value >= 1000) "%,.2f".format(value)
    else if (value < 0.01) "%.6f".format(value)
    else "%.4f".format(value)
