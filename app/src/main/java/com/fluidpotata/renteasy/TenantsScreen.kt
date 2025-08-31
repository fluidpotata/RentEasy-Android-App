package com.fluidpotata.renteasy

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import com.fluidpotata.renteasy.ui.theme.RentEasyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantsScreen(
    authViewModel: AuthViewModel,
    darkTheme: Boolean? = null
) {
    val resolvedDark = darkTheme ?: isSystemInDarkTheme()
    RentEasyTheme(darkTheme = resolvedDark) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Tenants", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                var tenants by remember { mutableStateOf<List<TenantItem>>(emptyList()) }
                var loading by remember { mutableStateOf(false) }
                var message by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    loading = true
                    authViewModel.loadTenants { result ->
                        result.onSuccess {
                            tenants = it.tenantList
                        }.onFailure {
                            message = it.message
                        }
                        loading = false
                    }
                }

                // Refresh control in case token expired or fetch failed
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text("Total: ${tenants.size}", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        loading = true
                        message = null
                        authViewModel.loadTenants { result ->
                            result.onSuccess { tenants = it.tenantList }
                                .onFailure { message = it.message }
                            loading = false
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh tenants")
                    }
                }

                if (loading) {
                    CircularProgressIndicator()
                } else {
                    message?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                    }

                    // Tenant dropdown
                    var tenantExpanded by remember { mutableStateOf(false) }
                    var selectedTenant by remember { mutableStateOf<TenantItem?>(null) }
                    var rentInfo by remember { mutableStateOf<RentInfo?>(null) }
                    var rentInfoLoading by remember { mutableStateOf(false) }
                    var rentInfoError by remember { mutableStateOf<String?>(null) }

                    ExposedDropdownMenuBox(expanded = tenantExpanded, onExpandedChange = { tenantExpanded = !tenantExpanded }) {
                        OutlinedTextField(
                            value = selectedTenant?.name ?: "Select tenant",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tenant") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tenantExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                        )

                        ExposedDropdownMenu(expanded = tenantExpanded, onDismissRequest = { tenantExpanded = false }) {
                            tenants.forEach { t ->
                                DropdownMenuItem(text = { Text(t.name.ifBlank { "#${t.id}" }) }, onClick = {
                                    selectedTenant = t
                                    tenantExpanded = false
                                    // fetch rent info when tenant selected
                                    rentInfoLoading = true
                                    rentInfoError = null
                                    rentInfo = null
                                    authViewModel.loadRentInfo(t.userId) { result ->
                                        result.onSuccess { info -> rentInfo = info }
                                            .onFailure { rentInfoError = it.message }
                                        rentInfoLoading = false
                                    }
                                })
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Option selector + input + update
                    var optionExpanded by remember { mutableStateOf(false) }
                    var selectedOption by remember { mutableStateOf("") }
                    var inputVal by remember { mutableStateOf("") }
                    var updating by remember { mutableStateOf(false) }
                    var localMsg by remember { mutableStateOf<String?>(null) }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp) // spacing between rows
                    ) {
                        // Row 1: Option dropdown
                        ExposedDropdownMenuBox(
                            expanded = optionExpanded,
                            onExpandedChange = { optionExpanded = !optionExpanded }
                        ) {
                            val labelText = when (selectedOption) {
                                "rent" -> {
                                    val cur = rentInfo?.rent?.takeIf { it > 0 }
                                    if (cur != null) "Change Rent (Current: $cur)" else "Change Rent"
                                }
                                "internet" -> {
                                    val cur = rentInfo?.internet?.takeIf { it > 0 }
                                    if (cur != null) "Change Internet Bill (Current: $cur)" else "Change Internet Bill"
                                }
                                "utility" -> {
                                    val cur = rentInfo?.utility?.takeIf { it > 0 }
                                    if (cur != null) "Change Utility Bill (Current: $cur)" else "Change Utility Bill"
                                }
                                else -> "Select an action"
                            }
                            OutlinedTextField(
                                value = labelText,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = optionExpanded)
                                },
                                supportingText = {
                                    if (rentInfoLoading) Text("Loading current amounts...")
                                    else rentInfoError?.let { Text(it) }
                                },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = optionExpanded,
                                onDismissRequest = { optionExpanded = false }
                            ) {
                                listOf("rent", "internet", "utility").forEach { opt ->
                                    val currentVal = when (opt) {
                                        "rent" -> rentInfo?.rent
                                        "internet" -> rentInfo?.internet
                                        "utility" -> rentInfo?.utility
                                        else -> null
                                    }
                                    val text = buildString {
                                        append(
                                            when (opt) {
                                                "rent" -> "Change Rent"
                                                "internet" -> "Change Internet Bill"
                                                "utility" -> "Change Utility Bill"
                                                else -> opt
                                            }
                                        )
                                        if (currentVal != null && currentVal > 0) append(" (Current: $currentVal)")
                                    }
                                    DropdownMenuItem(
                                        text = { Text(text) },
                                        onClick = {
                                            selectedOption = opt
                                            optionExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Row 2: Input field
                        OutlinedTextField(
                            value = inputVal,
                            onValueChange = { inputVal = it },
                            label = { Text("Enter new amount") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Row 3: Update button
                        Button(
                            onClick = {
                                if (selectedTenant == null) {
                                    localMsg = "Select a tenant"
                                    return@Button
                                }
                                if (selectedOption.isBlank() || inputVal.isBlank()) {
                                    localMsg = "Select option and enter value"
                                    return@Button
                                }
                                updating = true
                                localMsg = null
                                authViewModel.updateTenant(
                                    selectedTenant!!.id,
                                    selectedOption,
                                    inputVal
                                ) { result ->
                                    result.onSuccess { localMsg = it.message }
                                        .onFailure { localMsg = it.message }
                                    updating = false
                                }
                            },
                            enabled = !updating,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (updating) "Updating..." else "Update")
                        }

                        // Optional status message
                        localMsg?.let {
                            Text(it, color = MaterialTheme.colorScheme.primary)
                        }
                    }


                    localMsg?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}