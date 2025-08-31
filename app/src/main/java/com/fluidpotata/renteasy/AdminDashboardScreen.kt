package com.fluidpotata.renteasy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    onNavigateToAddRoom: () -> Unit = {},
    onGenerateBills: () -> Unit = {},
    onOpenBills: (String) -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    LazyColumn(modifier = Modifier.padding(8.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onRefresh) { Text("Refresh") }
            }
            Spacer(Modifier.height(8.dp))
        }
        item {
            if (adminLoading) {
                Text("Loading...", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
            }
        }

        item {
            adminError?.let {
                Text("Error loading admin data: $it", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }
        }

        items(listOf(
            Triple("Create New Bills", "Generate bills for everyone", { onGenerateBills() }),
            Triple("Check Tickets", "Review user submitted tickets", { onNavigateToTickets() }),
            Triple("Open Rent Bills", "Rent left to pay: ${adminData?.rent ?: "-"}", { onOpenBills("rent") }),
            Triple("Open Internet Bills", "Bill left to pay: ${adminData?.internet ?: "-"}", { onOpenBills("internet") }),
            Triple("Open Utility Bills", "Utility bill left: ${adminData?.utility ?: "-"}", { onOpenBills("utility") }),
            Triple("Join Requests", "Current join requests: ${adminData?.joinreqs ?: "-"}", { onNavigateToApplications() })
        )) { itemTriple ->
            val (title, desc, action) = itemTriple
            AdminActionCard(
                title = title,
                description = desc,
                modifier = Modifier.fillMaxWidth()
            ) {
                action()
            }
            Spacer(Modifier.height(12.dp))
        }
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
        modifier = modifier.clickable(onClick = onClick),
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
