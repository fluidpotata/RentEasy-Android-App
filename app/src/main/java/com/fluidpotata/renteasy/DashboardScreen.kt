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
    var openBillsKind by remember { mutableStateOf<String?>(null) }
    var showUnverifiedDialog by remember { mutableStateOf(false) }

    // Load dashboard data when role changes
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
                        icon = {}
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
                    when (selectedTab) {
                        0 -> TenantDashboardScreen(
                            customerData = customerData!!,
                            onPayRent = { selectedTab = 1 },
                            onPayInternet = { selectedTab = 1 },
                            onPayUtility = { selectedTab = 1 }
                        )
                        1 -> PaymentsScreen(authViewModel = authViewModel, initialKind = null, onCloseInitial = {})
                        2 -> CustomerTicketsScreen(authViewModel = authViewModel)
                        else -> TenantDashboardScreen(
                            customerData = customerData!!,
                            onPayRent = { selectedTab = 1 },
                            onPayInternet = { selectedTab = 1 },
                            onPayUtility = { selectedTab = 1 }
                        )
                    }
                }
                userRole == UserRole.LANDLORD -> {
                    when (tabs[selectedTab]) {
                        "Rooms" -> RoomUpdateScreen(authViewModel = authViewModel)
                        "Tenants" -> TenantsScreen(authViewModel = authViewModel)
                        else -> AdminDashboardScreen(
                            adminData = adminData,
                            adminLoading = adminLoading,
                            adminError = adminError,
                            onNavigateToTickets = onNavigateToTickets,
                            onNavigateToApplications = onNavigateToApplications,
                            onNavigateToAddRoom = onNavigateToAddRoom,
                            onOpenBills = { kind ->
                                openBillsKind = kind
                                adminMessage = "Opening ${kind} bills"
                            }
                            , onRefresh = {
                                adminLoading = true
                                adminError = null
                                authViewModel.loadAdminDashboardResult { result ->
                                    result.onSuccess { adminData = it }
                                        .onFailure { adminError = it.message }
                                    adminLoading = false
                                }
                            }
                        )
                    }
                }
            }
        }

        // Bills dialog
        openBillsKind?.let { kind ->
            AlertDialog(
                onDismissRequest = { openBillsKind = null },
                confirmButton = { OutlinedButton(onClick = { openBillsKind = null }) { Text("Close") } },
                title = { Text("${kind.replaceFirstChar { it.uppercase() }} Bills") },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(480.dp) // fixed height
                    ) {
                        BillsScreen(kind = kind, authViewModel = authViewModel, onVerifyBills = {
                            showUnverifiedDialog = true
                        })
                    }
                }
            )
        }

        // Unverified bills dialog
        if (showUnverifiedDialog) {
            AlertDialog(
                onDismissRequest = { showUnverifiedDialog = false },
                confirmButton = { OutlinedButton(onClick = { showUnverifiedDialog = false }) { Text("Close") } },
                title = { Text("Unverified Bills") },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(480.dp) // fixed height
                    ) {
                        UnverifiedBillsScreen(authViewModel = authViewModel, onVerified = {
                            authViewModel.loadAdminDashboardResult { result ->
                                result.onSuccess { adminData = it }
                                    .onFailure { adminError = it.message }
                            }
                            showUnverifiedDialog = false
                            openBillsKind = null
                        })
                    }
                }
            )
        }
    }
}
