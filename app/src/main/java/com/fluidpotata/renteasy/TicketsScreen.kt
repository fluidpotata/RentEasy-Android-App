package com.fluidpotata.renteasy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreen(
    authViewModel: AuthViewModel = viewModel(),
    onBack: () -> Unit
) {
    var ticketsResponse by remember { mutableStateOf<TicketAdminResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var closingTickets by remember { mutableStateOf<Set<Int>>(emptySet()) }

    LaunchedEffect(Unit) {
        authViewModel.loadTicketAdmin { result ->
            result.onSuccess { ticketsResponse = it }
                .onFailure { error = it.message }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Tickets") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("< Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when {
                loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                }
                ticketsResponse != null -> {
                    val tickets = ticketsResponse!!.ticketList
                    if (tickets.isEmpty()) {
                        Text("No tickets found.", style = MaterialTheme.typography.bodyLarge)
                    } else {
                        tickets.forEach { ticket ->
                            TicketCard(
                                ticket = ticket,
                                isClosing = ticket.id in closingTickets,
                                onClose = { ticketId ->
                                    closingTickets = closingTickets + ticketId
                                    authViewModel.closeTicket(ticketId) { result ->
                                        closingTickets = closingTickets - ticketId
                                        result.onSuccess {
                                            // Refresh tickets after closing
                                            authViewModel.loadTicketAdmin { refreshResult ->
                                                refreshResult.onSuccess { ticketsResponse = it }
                                            }
                                        }.onFailure { closeError ->
                                            error = "Failed to close ticket: ${closeError.message}"
                                        }
                                    }
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketCard(
    ticket: Ticket,
    isClosing: Boolean,
    onClose: (Int) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ID: ${ticket.id}", style = MaterialTheme.typography.titleSmall)
                    Text("User: ${ticket.username}", style = MaterialTheme.typography.bodyMedium)
                    Text("Category: ${ticket.category}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(ticket.subject, style = MaterialTheme.typography.bodyLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(ticket.status) },
                        colors = AssistChipDefaults.assistChipColors(
                            disabledLabelColor = if (ticket.status == "Open") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    if (ticket.status == "Open") {
                        OutlinedButton(
                            onClick = { onClose(ticket.id) },
                            enabled = !isClosing
                        ) {
                            if (isClosing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}
