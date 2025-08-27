package com.fluidpotata.renteasy

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.TileMode

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit,
    onNavigateSignup: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

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
                Text("Welcome Back", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("Sign in to continue", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                )
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
                Spacer(Modifier.height(20.dp))

                FilledTonalButton(
                    onClick = {
                        loading = true
                        status = ""
                        viewModel.login(username, password) { success, msg ->
                            loading = false
                            status = msg
                            if (success) onLoginSuccess(viewModel.role ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    enabled = !loading && username.isNotBlank() && password.isNotBlank()
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (loading) "Signing In..." else "Login")
                }

                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onNavigateSignup, modifier = Modifier.align(Alignment.CenterHorizontally), enabled = !loading) {
                    Text("Don't have an account? Sign up")
                }

                AnimatedStatusText(status)
            }
        }
    }
}

@Composable
fun AnimatedStatusText(message: String) {
    if (message.isEmpty()) return
    val isError = !message.contains("success", ignoreCase = true)
    val color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Spacer(Modifier.height(8.dp))
    Text(message, style = MaterialTheme.typography.bodyMedium, color = color)
}
