package com.fluidpotata.renteasy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerTicketsScreen(
    authViewModel: AuthViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var tickets by remember { mutableStateOf<List<Ticket>>(emptyList()) }
    var showNew by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.loadTickets { result ->
            result.onSuccess { tickets = it.ticketList; loading = false }
               .onFailure { error = it.message; loading = false }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(title = { Text("Tickets") })
        Spacer(Modifier.height(8.dp))

        Button(onClick = { showNew = true }) { Text("Create New Ticket") }
        Spacer(Modifier.height(8.dp))

        if (loading) {
            CircularProgressIndicator()
        } else if (error != null) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tickets) { ticket ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(ticket.category, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(Modifier.height(4.dp))
                            Text(ticket.subject)
                            Spacer(Modifier.height(4.dp))
                            Text("Status: ${ticket.status}")
                        }
                    }
                }
            }
        }

        if (showNew) {
            var selected by remember { mutableStateOf("Upgrade Room") }
            var description by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showNew = false },
                title = { Text("Create Ticket") },
                text = {
                    Column {
                        DropdownMenuDemo(options = listOf("Upgrade Room", "Issue with Billing", "Report a problem"), selected = selected, onSelect = { selected = it })
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    FilledTonalButton(onClick = {
                        authViewModel.createTicket(selected, description) { res ->
                            res.onSuccess {
                                showNew = false
                                // reload
                                authViewModel.loadTickets { r -> r.onSuccess { tickets = it.ticketList } }
                            }.onFailure {
                                error = it.message
                            }
                        }
                    }) { Text("Create") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showNew = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuDemo(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth(),
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = {
                    onSelect(opt)
                    expanded = false
                })
            }
        }
    }
}
