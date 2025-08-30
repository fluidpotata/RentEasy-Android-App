package com.fluidpotata.renteasy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdminDashboardScreen(
    adminData: AdminDashboardResponse?,
    adminLoading: Boolean,
    adminError: String?,
    onNavigateToTickets: () -> Unit = {},
    onNavigateToApplications: () -> Unit = {},
    onNavigateToAddRoom: () -> Unit = {}
) {
    Column {
        if (adminLoading) {
            // simple loading indicator inline; keep minimal to avoid bringing extra imports
            Text("Loading...", style = MaterialTheme.typography.bodyMedium)
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
            onNavigateToTickets()
        }
        Spacer(Modifier.height(12.dp))
        AdminActionCard(
            title = "Join Requests",
            description = "Current join requests: ${adminData?.joinreqs ?: "-"}",
            modifier = Modifier.fillMaxWidth()
        ) {
            onNavigateToApplications()
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
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun AdminActionCard(
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
