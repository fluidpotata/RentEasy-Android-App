package com.fluidpotata.renteasy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository() // default instance
) : ViewModel() {

    var role: String? = null
        private set

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

    fun login(
        username: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
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
}
