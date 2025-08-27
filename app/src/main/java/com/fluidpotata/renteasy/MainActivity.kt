package com.fluidpotata.renteasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fluidpotata.renteasy.ui.theme.RentEasyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            RentEasyTheme {
            val factory = AuthViewModelFactory(this)
            val vm: AuthViewModel = viewModel(factory = factory)

            val currentScreen = remember { mutableStateOf("login") }
            val roleString = remember { mutableStateOf("") }

            val userRole = when (roleString.value.lowercase()) {
                "tenant" -> UserRole.TENANT
                "landlord", "admin" -> UserRole.LANDLORD
                else -> UserRole.TENANT
            }

            when (currentScreen.value) {
                "login" -> LoginScreen(
                    vm,
                    onLoginSuccess = { roleFromApi ->
                        roleString.value = roleFromApi
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

                "dashboard" -> DashboardScreen(
                    userRole = userRole,
                    token = vm.token ?: "",
                    onSignOut = {
                        currentScreen.value = "login"
                        roleString.value = ""
                    }
                )

                "tickets" -> TicketsScreen(
                    authViewModel = vm,
                    onBack = {
                        currentScreen.value = "dashboard"
                    }
                )
            }
            }
        }
    }
}
