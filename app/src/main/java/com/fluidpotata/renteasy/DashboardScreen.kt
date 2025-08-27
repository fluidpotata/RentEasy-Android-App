package com.fluidpotata.renteasy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

enum class UserRole { TENANT, LANDLORD }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userRole: UserRole,
    token: String,
    authViewModel: AuthViewModel = viewModel(),
    onSignOut: () -> Unit = {},
    onNavigateToTickets: () -> Unit = {}
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

    var seeAppsDialogVisible by remember { mutableStateOf(false) }
    var seeAppsData by remember { mutableStateOf<SeeAppsResponse?>(null) }
    var seeAppsLoading by remember { mutableStateOf(false) }
    var seeAppsError by remember { mutableStateOf<String?>(null) }
    var seeAppsFetchRequested by remember { mutableStateOf(false) }

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
                    val data = customerData!!
                    TenantInfoCard(title = "Package", value = data.`package`)
                    Spacer(Modifier.height(12.dp))
                    TenantBillCard(
                        title = "Rent Bill",
                        unpaid = data.rentUnpaid,
                        onPay = { /* TODO initiate rent payment */ }
                    )
                    Spacer(Modifier.height(12.dp))
                    TenantBillCard(
                        title = "Internet Bill",
                        unpaid = data.internetUnpaid,
                        onPay = { /* TODO initiate internet payment */ }
                    )
                    Spacer(Modifier.height(12.dp))
                    TenantBillCard(
                        title = "Utility Bill",
                        unpaid = data.utilityUnpaid,
                        onPay = { /* TODO initiate utility payment */ }
                    )
                    Spacer(Modifier.height(12.dp))
                    TenantInfoCard(title = "Tickets", value = data.ticketCount.toString())
                }
                userRole == UserRole.LANDLORD -> {
                    Column {
                        if (adminLoading) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                        }
                        adminError?.let {
                            Text("Error loading admin data: $it", color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                        }


                        AdminActionCard(
                            title = "Create New Bills",
                            description = "Generate bills for everyone",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // TODO: trigger bill generation API
                        }
                        Spacer(Modifier.height(12.dp))
                        AdminActionCard(
                            title = "Check Tickets",
                            description = "Review user submitted tickets",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // TODO: navigate to see tickets
                        }
                        Spacer(Modifier.height(12.dp))
                        AdminActionCard(
                            title = "Join Requests",
                            description = "Current join requests: ${adminData?.joinreqs ?: "-"}",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            seeAppsDialogVisible = true
                            seeAppsLoading = true
                            seeAppsError = null
                            seeAppsData = null
                            seeAppsFetchRequested = true
                        }
                        Spacer(Modifier.height(12.dp))
                        AdminActionCard(
                            title = "Rent Status",
                            description = "Rent left to pay: ${adminData?.rent ?: "-"}",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // TODO: navigate to rent bills
                        }
                        Spacer(Modifier.height(12.dp))
                        AdminActionCard(
                            title = "Internet Bill Status",
                            description = "Bill left to pay: ${adminData?.internet ?: "-"}",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // TODO: navigate to internet bills
                        }
                        Spacer(Modifier.height(12.dp))
                        AdminActionCard(
                            title = "Utility Bill Status",
                            description = "Utility bill left: ${adminData?.utility ?: "-"}",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // TODO: navigate to utility bills
                        }
                    }
                }
            }


            if (seeAppsDialogVisible) {
                AlertDialog(
                    onDismissRequest = { seeAppsDialogVisible = false },
                    confirmButton = {
                        TextButton(onClick = { seeAppsDialogVisible = false }) {
                            Text("Close")
                        }
                    },
                    title = { Text("Applications") },
                    text = {
                        when {
                            seeAppsLoading -> CircularProgressIndicator()
                            seeAppsError != null -> Text("Error: $seeAppsError")
                            seeAppsData != null -> {
                                Column {
                                    Text("Requests:")
                                    seeAppsData!!.requests.forEach { req ->
                                        Text("- ${req["username"] ?: "Unknown"}")
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text("Available Rooms:")
                                    seeAppsData!!.available_rooms.forEach { room ->
                                        Text("- ${room["room_id"] ?: "Unknown"}")
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AdminActionCard(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (description != null) {
                Spacer(Modifier.height(6.dp))
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onClick) { Text("Open") }
        }
    }
}

@Composable
private fun TenantInfoCard(title: String, value: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun TenantBillCard(title: String, unpaid: Boolean, onPay: () -> Unit) {
    val statusText = if (unpaid) "Bill Unpaid" else "Bill Paid"
    val statusColor = if (unpaid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(statusText) },
                colors = AssistChipDefaults.assistChipColors(
                    disabledLabelColor = statusColor
                )
            )
            Spacer(Modifier.height(8.dp))
            FilledTonalButton(onClick = onPay, enabled = unpaid) { Text("Pay") }
        }
    }
}
