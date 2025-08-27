package com.fluidpotata.renteasy


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode

@Composable
fun SignupScreen(viewModel: AuthViewModel, onSignupSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var roomType by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val nameTrim = name.trim()
    val usernameTrim = username.trim()
    val phoneTrim = phone.trim()
    val roomTypeTrim = roomType.trim()
    val formValid = nameTrim.isNotEmpty() && usernameTrim.isNotEmpty() && phoneTrim.isNotEmpty() &&
            password.length >= 6 && password == confirmPassword && roomTypeTrim.isNotEmpty()

    val colorScheme = MaterialTheme.colorScheme
    val gradient = remember(isSystemInDarkTheme()) {
        val c1 = colorScheme.surfaceVariant
        val c2 = colorScheme.surface
        val c3 = colorScheme.background
        Brush.verticalGradient(listOf(c1, c2, c3), tileMode = TileMode.Clamp)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.align(Alignment.Center),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Create Account", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("Join RentEasy to manage your stay", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), enabled = !loading, isError = nameTrim.isEmpty() && name.isNotEmpty())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), enabled = !loading, isError = usernameTrim.isEmpty() && username.isNotEmpty())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), enabled = !loading, isError = phoneTrim.isEmpty() && phone.isNotEmpty())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = null)
                        }
                    }
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = roomType, onValueChange = { roomType = it }, label = { Text("Room Type") }, modifier = Modifier.fillMaxWidth(), enabled = !loading, isError = roomTypeTrim.isEmpty() && roomType.isNotEmpty())
                Spacer(Modifier.height(20.dp))

                FilledTonalButton(
                    onClick = {
                        loading = true
                        status = ""
                        viewModel.signup(name, username, phone, password, confirmPassword, roomType) { msg ->
                            loading = false
                            status = msg
                            if (msg.contains("success", ignoreCase = true)) onSignupSuccess()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    enabled = !loading && formValid
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (loading) "Creating..." else "Create Account")
                }
                if (!formValid && !loading) {
                    Spacer(Modifier.height(8.dp))
                    val reasons = buildList {
                        if (nameTrim.isEmpty()) add("Name")
                        if (usernameTrim.isEmpty()) add("Username")
                        if (phoneTrim.isEmpty()) add("Phone")
                        if (password.length < 6) add("Password >= 6 chars")
                        if (password != confirmPassword) add("Passwords match")
                        if (roomTypeTrim.isEmpty()) add("Room Type")
                    }
                    if (reasons.isNotEmpty()) {
                        Text(
                            "Complete: ${reasons.joinToString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                AnimatedStatusText(status)
            }
        }
    }
}
