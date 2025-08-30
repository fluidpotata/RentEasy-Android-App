package com.fluidpotata.renteasy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

enum class UserRole { TENANT, LANDLORD }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userRole: UserRole,
    token: String,
    authViewModel: AuthViewModel = viewModel(),
    onSignOut: () -> Unit = {},
    onNavigateToTickets: () -> Unit = {},
    onNavigateToApplications: () -> Unit = {},
    onNavigateToAddRoom: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = when (userRole) {
        UserRole.TENANT -> listOf("Dashboard", "Payments", "Ticket")
        UserRole.LANDLORD -> listOf("Dashboard", "Tenants", "Rooms")
    }

    var adminData by remember { mutableStateOf<AdminDashboardResponse?>(null) }
    var customerData by remember { mutableStateOf<CustomerDashboardResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var adminLoading by remember { mutableStateOf(false) }
    var adminError by remember { mutableStateOf<String?>(null) }
    var adminMessage by remember { mutableStateOf<String?>(null) }

    // Load dashboard data on role change
    LaunchedEffect(userRole) {
        loading = true
        error = null
        if (userRole == UserRole.TENANT) {
            authViewModel.loadCustomerDashboard { customerData = it; loading = false }
        } else {
            adminLoading = true
            adminError = null
            authViewModel.loadAdminDashboardResult { result ->
                result.onSuccess { adminData = it }
                    .onFailure { adminError = it.message }
                adminLoading = false
                loading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (userRole == UserRole.TENANT) "Tenant View" else "Landlord View") },
                actions = {
                    TextButton(onClick = onSignOut) {
                        Text("Sign Out", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(label) },
                        icon = {} // optional
                    )
                }
            }
        },
        floatingActionButton = {
            if (userRole == UserRole.LANDLORD && tabs[selectedTab] == "Rooms") {
                FloatingActionButton(onClick = onNavigateToAddRoom) {
                    Icon(Icons.Default.Add, contentDescription = "Add Room")
                }
            }
        }
    ) { padding ->
    Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
        ) {
            Text("Selected Tab: ${tabs[selectedTab]}", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            when {
                loading -> CircularProgressIndicator()
                error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error)
                userRole == UserRole.TENANT && customerData != null -> {
                    TenantDashboardScreen(
                        customerData = customerData!!,
                        onPayRent = { /* TODO: initiate rent payment */ },
                        onPayInternet = { /* TODO: initiate internet payment */ },
                        onPayUtility = { /* TODO: initiate utility payment */ }
                    )
                }
                userRole == UserRole.LANDLORD -> {
                    if (tabs[selectedTab] == "Rooms") {
                        RoomUpdateScreen(authViewModel = authViewModel)
                    } else if (tabs[selectedTab] == "Tenants") {
                        TenantsScreen(authViewModel = authViewModel)
                    } else {
                        AdminDashboardScreen(
                            adminData = adminData,
                            adminLoading = adminLoading,
                            adminError = adminError ?: adminMessage,
                            onNavigateToTickets = onNavigateToTickets,
                            onNavigateToApplications = onNavigateToApplications,
                            onNavigateToAddRoom = onNavigateToAddRoom,
                            onGenerateBills = {
                                adminLoading = true
                                adminError = null
                                adminMessage = null
                                authViewModel.generateBills { result ->
                                    result.onSuccess {
                                        adminMessage = it.message ?: "Bills generated"
                                    }.onFailure {
                                        adminError = it.message
                                    }
                                    adminLoading = false
                                }
                            }
                        )
                    }
                }
            }


        }
    }
}


