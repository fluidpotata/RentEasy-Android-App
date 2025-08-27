package com.fluidpotata.renteasy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    var role: String? = null
        private set

    val token: String?
        get() = repository.getToken()

    fun signup(
        name: String,
        username: String,
        phone: String,
        password: String,
        confirmPassword: String,
        roomType: String,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.signup(name, username, phone, password, confirmPassword, roomType)
                onResult(response.message)
            } catch (e: Exception) {
                onResult("Signup failed: ${e.message}")
            }
        }
    }

    fun login(username: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.login(username, password)
                role = response.role
                onResult(true, response.message)
            } catch (e: Exception) {
                onResult(false, "Login failed: ${e.message}")
            }
        }
    }


    fun loadAdminDashboardResult(onResult: (Result<AdminDashboardResponse>) -> Unit) {
        viewModelScope.launch {
            try {
                val data = repository.getAdminDashboard()
                onResult(Result.success(data))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun loadCustomerDashboard(onResult: (CustomerDashboardResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                val data = repository.getCustomerDashboard()
                onResult(data)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    fun loadSeeApps(onResult: (Result<SeeAppsResponse>) -> Unit) {
        viewModelScope.launch {
            try {
                val data = repository.getSeeApps()
                onResult(Result.success(data))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }


}
