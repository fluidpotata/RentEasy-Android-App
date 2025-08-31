package com.fluidpotata.renteasy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun UnverifiedBillsScreen(
    authViewModel: AuthViewModel = viewModel(),
    onVerified: () -> Unit = {}
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var bills by remember { mutableStateOf<List<BillItem>>(emptyList()) }
    var snackbar by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        authViewModel.loadUnverifiedBills { res ->
            res.onSuccess { bills = it.billList; loading = false }
                .onFailure { error = it.message; loading = false }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text("Unverified Bills", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        when {
            loading -> CircularProgressIndicator()
            error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error)
            bills.isEmpty() -> Text("No unverified bills found")
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(bills) { bill ->
                        Card(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Column {
                                    Text("#${bill.serial} - ${bill.name}")
                                    Text("Tenant ID: ${bill.tenantId ?: "-"}")
                                    Text("Amount: ${bill.amount}")
                                    Text("TRXID: ${bill.trxId ?: "-"}")
                                    Text("Type: ${bill.type ?: "-"}")
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Button(onClick = {
                                        // verify the bill (payment id is bill.serial)
                                        authViewModel.verifyBill(bill.serial) { res ->
                                            res.onSuccess {
                                                snackbar = it.message
                                                onVerified()
                                            }.onFailure { snackbar = it.message }
                                        }
                                    }) {
                                        Text("Confirm")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        snackbar?.let { s ->
            Spacer(Modifier.height(8.dp))
            Snackbar { Text(s) }
        }
    }
}
