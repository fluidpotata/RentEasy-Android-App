package com.fluidpotata.renteasy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BillsScreen(
    kind: String, // "internet" or "utility"
    authViewModel: AuthViewModel,
    onVerifyBills: () -> Unit = {}
) {
    val loading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }
    val bills = remember { mutableStateOf<List<BillItem>>(emptyList()) }

    LaunchedEffect(kind) {
        loading.value = true
        error.value = null
        if (kind == "internet") {
            authViewModel.loadInternetBills { result ->
                result.onSuccess {
                    bills.value = it.billList
                }.onFailure {
                    error.value = it.message
                }
                loading.value = false
            }
        } else if (kind == "utility") {
            authViewModel.loadUtilityBills { result ->
                result.onSuccess {
                    bills.value = it.billList
                }.onFailure {
                    error.value = it.message
                }
                loading.value = false
            }
        } else if (kind == "rent") {
            authViewModel.loadRentBills { result ->
                result.onSuccess {
                    bills.value = it.billList
                }.onFailure {
                    error.value = it.message
                }
                loading.value = false
            }
        } else {
            loading.value = false
            error.value = "Unknown bills kind: $kind"
        }
    }

    Column(modifier = Modifier.padding(12.dp)) {
        Text("${kind.replaceFirstChar { it.uppercase() }} Bills", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        when {
            loading.value -> Text("Loading...", style = MaterialTheme.typography.bodyMedium)
            error.value != null -> Text("Error: ${error.value}", color = MaterialTheme.colorScheme.error)
            else -> {
                if (bills.value.isEmpty()) {
                    Text("No bills found")
                } else {
                    // Bound the LazyColumn height to avoid Compose measure crash
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = true)
                    ) {
                        items(bills.value) { bill ->
                            ElevatedCard(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.elevatedCardElevation(2.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("#${bill.serial} - ${bill.name}", style = MaterialTheme.typography.titleMedium)
                                        Text("${bill.month}")
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Amount: ${bill.amount}")
                                        Text("Status: ${bill.status}")
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    if (bill.status == "unverified") {
                                        // show a button to navigate to verify bills page (not implemented)
                                        Button(onClick = onVerifyBills) {
                                            Text("Verify")
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}
