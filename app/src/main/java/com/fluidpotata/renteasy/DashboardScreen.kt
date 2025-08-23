package com.fluidpotata.renteasy


import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun DashboardScreen(role: String) {
    if (role == "admin") {
        Text("Welcome Admin (Landlord) Dashboard")
    } else {
        Text("Welcome Tenant Dashboard")
    }
}
