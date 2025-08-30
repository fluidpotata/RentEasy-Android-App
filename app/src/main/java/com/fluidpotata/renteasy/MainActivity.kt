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

            // check saved auth token and auto-login if present and not expired
            LaunchedEffect(Unit) {
                val saved = vm.getSavedAuth()
                if (saved != null) {
                    roleString.value = saved.role ?: ""
                    currentScreen.value = "dashboard"
                }
            }

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
                    },
                    onNavigateToTickets = {
                        currentScreen.value = "tickets"
                    },
                    onNavigateToApplications = {
                        currentScreen.value = "applications"
                    },
                    onNavigateToAddRoom = {
                        currentScreen.value = "add_room"
                    }
                )

                "tickets" -> TicketsScreen(
                    authViewModel = vm,
                    onBack = {
                        currentScreen.value = "dashboard"
                    }
                )

                "applications" -> ApplicationsScreen(
                    authViewModel = vm,
                    onBack = {
                        currentScreen.value = "dashboard"
                    }
                )

                "add_room" -> AddRoomScreen(
                    authViewModel = vm,
                    onRoomAdded = {
                        currentScreen.value = "dashboard"
                    },
                    onBack = {
                        currentScreen.value = "dashboard"
                    }
                )
            }
        }
    }
    }
}