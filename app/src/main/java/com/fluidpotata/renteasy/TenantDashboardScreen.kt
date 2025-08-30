package com.fluidpotata.renteasy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TenantDashboardScreen(
    customerData: CustomerDashboardResponse,
    onPayRent: () -> Unit = {},
    onPayInternet: () -> Unit = {},
    onPayUtility: () -> Unit = {}
) {
    Column {
        TenantInfoCard(title = "Package", value = customerData.`package`)
        Spacer(Modifier.height(12.dp))
        TenantBillCard(
            title = "Rent Bill",
            unpaid = customerData.rentUnpaid,
            onPay = onPayRent
        )
        Spacer(Modifier.height(12.dp))
        TenantBillCard(
            title = "Internet Bill",
            unpaid = customerData.internetUnpaid,
            onPay = onPayInternet
        )
        Spacer(Modifier.height(12.dp))
        TenantBillCard(
            title = "Utility Bill",
            unpaid = customerData.utilityUnpaid,
            onPay = onPayUtility
        )
        Spacer(Modifier.height(12.dp))
        TenantInfoCard(title = "Tickets", value = customerData.ticketCount.toString())
    }
}

@Composable
fun TenantInfoCard(title: String, value: String) {
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
fun TenantBillCard(title: String, unpaid: Boolean, onPay: () -> Unit) {
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
