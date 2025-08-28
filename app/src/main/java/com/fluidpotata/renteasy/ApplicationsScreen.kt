package com.fluidpotata.renteasy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsScreen(
    authViewModel: AuthViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    var applicationsData by remember { mutableStateOf<SeeAppsResponse?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var allocating by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        authViewModel.loadSeeApps { result ->
            loading = false
            result.onSuccess { data ->
                applicationsData = data
            }.onFailure { e ->
                error = e.message ?: "Failed to load applications"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Applications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("← Back")
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
        ) {
            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error: $error", color = MaterialTheme.colorScheme.error)
                    }
                }
                applicationsData != null -> {
                    val data = applicationsData!!
                    if (data.requestList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No applications found")
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(data.requestList) { request ->
                                ApplicationCard(
                                    request = request,
                                    availableRooms = data.availableRoomList,
                                    isAllocating = allocating == request.id,
                                    onAllocate = { roomId ->
                                        allocating = request.id
                                        authViewModel.allocateRoom(
                                            roomId = roomId,
                                            reqId = request.id,
                                            name = request.name,
                                            roomChoice = request.roomChoice,
                                            username = request.username,
                                            password = request.password,
                                            phone = request.phone
                                        ) { result ->
                                            allocating = null
                                            result.onSuccess {
                                                // Refresh the list after successful allocation
                                                loading = true
                                                error = null
                                                authViewModel.loadSeeApps { refreshResult ->
                                                    loading = false
                                                    refreshResult.onSuccess { newData ->
                                                        applicationsData = newData
                                                    }.onFailure { e ->
                                                        error = e.message ?: "Failed to refresh applications"
                                                    }
                                                }
                                            }.onFailure { e ->
                                                error = e.message ?: "Failed to allocate room"
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationCard(
    request: ApplicationRequest,
    availableRooms: List<AvailableRoom>,
    isAllocating: Boolean,
    onAllocate: (Int) -> Unit
) {
    var selectedRoomId by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#${request.id}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Name: ${request.name}")
                    Text("Room Choice: ${request.roomChoice}")
                    Text("Phone: ${request.phone}")
                }

                Column {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedRoomId?.let { id ->
                                availableRooms.find { it.id == id }?.let { "${it.roomName} [${it.type}]" } ?: ""
                            } ?: "Select Room",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .width(200.dp)
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            availableRooms.forEach { room ->
                                DropdownMenuItem(
                                    text = { Text("${room.roomName} [${room.type}]") },
                                    onClick = {
                                        selectedRoomId = room.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            selectedRoomId?.let { onAllocate(it) }
                        },
                        enabled = selectedRoomId != null && !isAllocating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (isAllocating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Allocate")
                        }
                    }
                }
            }
        }
    }
}
