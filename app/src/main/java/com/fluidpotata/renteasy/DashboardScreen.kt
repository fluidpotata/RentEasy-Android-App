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
// ...existing imports...

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
        var openBillsKind by remember { mutableStateOf<String?>(null) }

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
            adminMessage?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
            }

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
                            adminError = adminError,
                            onNavigateToTickets = onNavigateToTickets,
                            onNavigateToApplications = onNavigateToApplications,
                            onNavigateToAddRoom = onNavigateToAddRoom,
                                onOpenBills = { kind -> openBillsKind = kind; adminMessage = "Opening ${kind} bills" }
                        )

                            // Show bills in a dialog so it is visible independent of parent scrolling
                            openBillsKind?.let { kind ->
                                AlertDialog(
                                    onDismissRequest = { openBillsKind = null },
                                    confirmButton = {
                                        OutlinedButton(onClick = { openBillsKind = null }) { Text("Close") }
                                    },
                                    title = { Text("${kind.replaceFirstChar { it.uppercase() }} Bills") },
                                    text = {
                                        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth().height(480.dp)) {
                                            BillsScreen(kind = kind, authViewModel = authViewModel, onVerifyBills = {
                                                // TODO: navigate to verify bills screen
                                            })
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


