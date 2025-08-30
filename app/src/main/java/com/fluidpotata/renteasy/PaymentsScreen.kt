package com.fluidpotata.renteasy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun PaymentsScreen(
	authViewModel: AuthViewModel = viewModel(),
	initialKind: String? = null,
	onCloseInitial: () -> Unit = {}
) {
	var kind by remember { mutableStateOf(initialKind ?: "rent") }
	var loading by remember { mutableStateOf(true) }
	var error by remember { mutableStateOf<String?>(null) }
	var bills by remember { mutableStateOf<List<BillItem>>(emptyList()) }
	var snackbar by remember { mutableStateOf<String?>(null) }

	val scope = rememberCoroutineScope()

	fun load() {
		loading = true
		error = null
		// load bills for the logged-in tenant
		authViewModel.loadMyBills { result ->
			result.onSuccess { bills = it.billList; loading = false }
				.onFailure { error = it.message; loading = false }
		}
	}

	LaunchedEffect(Unit) { load(); onCloseInitial() }

	Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {

		if (loading) {
			CircularProgressIndicator()
		} else if (error != null) {
			Text("Error: $error", color = MaterialTheme.colorScheme.error)
		} else {
			var showDialog by remember { mutableStateOf<Pair<Boolean, BillItem?>>(false to null) }

			LazyColumn(modifier = Modifier.fillMaxSize()) {
				items(bills) { bill ->
					Card(modifier = Modifier
						.fillMaxWidth()
						.padding(vertical = 6.dp)
						.clickable { /* maybe show details */ }) {
						Column(modifier = Modifier.padding(12.dp)) {
							Text(bill.name, style = MaterialTheme.typography.titleMedium)
							Spacer(Modifier.height(4.dp))
							Text("Amount: ${bill.amount}")
							Text("Month: ${bill.month}")
							Spacer(Modifier.height(6.dp))
							val unpaid = bill.status.equals("unpaid", ignoreCase = true)
							FilledTonalButton(onClick = {
								showDialog = true to bill
							}, enabled = unpaid) { Text("Pay") }
						}
					}
				}
			}

			val (dialogVisible, dialogBill) = showDialog
			if (dialogVisible && dialogBill != null) {
				var tIdText by remember { mutableStateOf("") }
				var inputError by remember { mutableStateOf<String?>(null) }
				val saved = remember { mutableStateOf<com.fluidpotata.renteasy.data.AuthToken?>(null) }
				LaunchedEffect(Unit) {
					saved.value = authViewModel.getSavedAuth()
				}
				val tenantId = saved.value?.userId ?: 0
				val isValid = remember(tIdText, tenantId) { tIdText.trim().isNotEmpty() && tenantId > 0 }
				AlertDialog(
					onDismissRequest = { showDialog = false to null },
					title = { Text("Pay Bill") },
					text = {
						Column {
							Text("Tenant ID: ${saved.value?.userId ?: ""}")
							Spacer(Modifier.height(8.dp))
							Text("Bill: ${dialogBill.name} - ${dialogBill.month} : ${dialogBill.amount}")
							Spacer(Modifier.height(8.dp))
							OutlinedTextField(
								value = tIdText,
								onValueChange = {
									tIdText = it
									if (it.trim().isEmpty()) inputError = "Transaction ID required" else inputError = null
								},
								label = { Text("Transaction ID") }
							)
							inputError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
						}
					},
					confirmButton = {
						FilledTonalButton(
							onClick = {
								scope.launch {
									if (tenantId <= 0) {
										snackbar = "Not logged in (no tenant id)"
										return@launch
									}
									if (tIdText.trim().isEmpty()) {
										inputError = "Transaction ID required"
										return@launch
									}
									authViewModel.payBill(dialogBill.serial, tIdText) { res ->
										res.onSuccess {
											snackbar = it.message
											load()
										}.onFailure {
											snackbar = it.message
										}
									}
									showDialog = false to null
								}
							}, enabled = isValid) { Text("Pay Now") }
					},
					dismissButton = {
						OutlinedButton(onClick = { showDialog = false to null }) { Text("Cancel") }
					}
				)
			}
		}

		snackbar?.let { msg ->
			Spacer(Modifier.height(8.dp))
			Snackbar { Text(msg) }
		}
	}
}
