package com.fluidpotata.renteasy

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import com.fluidpotata.renteasy.ui.theme.RentEasyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomUpdateScreen(
    authViewModel: AuthViewModel,
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    val resolvedDark = darkTheme ?: isSystemInDarkTheme()
    RentEasyTheme(darkTheme = resolvedDark) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Rooms Management", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))

                var tenants by remember { mutableStateOf<List<TenantShort>>(emptyList()) }
                var rooms by remember { mutableStateOf<List<AvailableRoom>>(emptyList()) }
                var selectedTenant by remember { mutableStateOf<TenantShort?>(null) }
                var selectedRoom by remember { mutableStateOf<AvailableRoom?>(null) }
                var loadingUpdateData by remember { mutableStateOf(false) }
                var updating by remember { mutableStateOf(false) }
                var message by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    loadingUpdateData = true
                    authViewModel.loadUpdateRoomData { result ->
                        result.onSuccess {
                            tenants = it.tenantList
                            rooms = it.roomList
                        }.onFailure {
                            message = it.message
                        }
                        loadingUpdateData = false
                    }
                }

                if (loadingUpdateData) {
                    CircularProgressIndicator()
                } else {
                    // Tenant dropdown
                    var tenantExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = tenantExpanded,
                        onExpandedChange = { tenantExpanded = !tenantExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedTenant?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Tenant") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tenantExpanded) },
                            modifier = Modifier
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = tenantExpanded,
                            onDismissRequest = { tenantExpanded = false }
                        ) {
                            tenants.forEach { t ->
                                DropdownMenuItem(text = { Text(t.name.ifBlank { "#${t.id}" }) }, onClick = {
                                    selectedTenant = t
                                    tenantExpanded = false
                                })
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Room dropdown
                    var roomExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = roomExpanded,
                        onExpandedChange = { roomExpanded = !roomExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedRoom?.roomName ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Room") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomExpanded) },
                            modifier = Modifier
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = roomExpanded,
                            onDismissRequest = { roomExpanded = false }
                        ) {
                            rooms.forEach { r ->
                                DropdownMenuItem(text = { Text("${r.roomName} (${r.type})") }, onClick = {
                                    selectedRoom = r
                                    roomExpanded = false
                                })
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val tenant = selectedTenant
                            val room = selectedRoom
                            if (tenant != null && room != null) {
                                updating = true
                                message = null
                                authViewModel.updateRoom(tenant.id, room.id) { result ->
                                    result.onSuccess {
                                        message = it.message
                                    }.onFailure {
                                        message = it.message
                                    }
                                    updating = false
                                }
                            } else {
                                message = "Select tenant and room"
                            }
                        },
                        enabled = !updating,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (updating) "Updating..." else "Update Room")
                    }

                    message?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("Use the + button below to add a new room.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
