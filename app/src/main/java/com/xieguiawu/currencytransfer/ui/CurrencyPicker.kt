package com.xieguiawu.currencytransfer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xieguiawu.currencytransfer.data.Currencies

/**
 * A text-field-style currency selector. Tapping opens a searchable
 * dialog listing all currencies (or the subset in [allowedCodes]).
 */
@Composable
fun CurrencyPicker(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowedCodes: List<String>? = null,
) {
    var dialogOpen by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    OutlinedTextField(
        value = Currencies.displayName(selected),
        onValueChange = {},
        readOnly = true,
        label = { Text("Currency") },
        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select currency") },
        modifier = modifier.clickable { dialogOpen = true },
    )

    if (dialogOpen) {
        val candidates = Currencies.all.filter { allowedCodes == null || it.code in allowedCodes }
        val filtered = if (query.isBlank()) {
            candidates
        } else {
            val q = query.trim().lowercase()
            candidates.filter {
                it.code.lowercase().contains(q) || it.name.lowercase().contains(q)
            }
        }
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            title = { Text("Select currency") },
            text = {
                Column {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search code or name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (filtered.isEmpty()) {
                        Text(
                            "No currency matches.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    } else {
                        LazyColumn(Modifier.fillMaxWidth()) {
                            items(filtered) { info ->
                                Column(Modifier.fillMaxWidth()) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSelect(info.code)
                                                dialogOpen = false
                                                query = ""
                                            }
                                            .padding(vertical = 10.dp, horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(info.code, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            info.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogOpen = false }) { Text("Close") }
            },
        )
    }
}
