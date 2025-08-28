package com.fluidpotata.renteasy

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fluidpotata.renteasy.ui.theme.RentEasyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoomScreen(
    authViewModel: AuthViewModel,
    onRoomAdded: () -> Unit,
    onBack: () -> Unit,
    darkTheme: Boolean = isSystemInDarkTheme() //or true can do force dark
) {
    RentEasyTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Add New Room",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(24.dp))

                var roomName by remember { mutableStateOf("") }
                var roomType by remember { mutableStateOf("") }
                var isLoading by remember { mutableStateOf(false) }
                var message by remember { mutableStateOf<String?>(null) }

                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Room Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                RoomTypeDropdown(
                    value = roomType,
                    onValueChange = { roomType = it },
                    label = "Room Type",
                    options = listOf("Luxury", "Regular")
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        isLoading = true
                        authViewModel.addRoom(roomType, roomName) { result ->
                            isLoading = false
                            result.onSuccess {
                                message = it.message
                                onRoomAdded()
                            }.onFailure {
                                message = it.message
                            }
                        }
                    },
                    enabled = roomName.isNotBlank() && roomType.isNotBlank() && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Room")
                }

                message?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Back")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomTypeDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
