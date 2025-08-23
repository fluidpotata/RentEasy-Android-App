package com.fluidpotata.renteasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val vm: AuthViewModel = viewModel()

            val currentScreen = remember { mutableStateOf("login") }
            val role = remember { mutableStateOf("") }

            when (currentScreen.value) {
                "login" -> LoginScreen(
                    vm,
                    onLoginSuccess = {
                        role.value = it
                        currentScreen.value = "dashboard"
                    },
                    onNavigateSignup = {
                        currentScreen.value = "signup"
                    }
                )
                "signup" -> SignupScreen(
                    vm,
                    onSignupSuccess = {
                        currentScreen.value = "login"
                    }
                )
                "dashboard" -> DashboardScreen(role.value)
            }
        }
    }
}
