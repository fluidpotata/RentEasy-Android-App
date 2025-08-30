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

    fun loadTicketAdmin(onResult: (Result<TicketAdminResponse>) -> Unit) {
        viewModelScope.launch {
            try {
                val data = repository.getTicketAdmin()
                onResult(Result.success(data))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun closeTicket(ticketId: Int, onResult: (Result<CloseTicketResponse>) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.closeTicket(ticketId)
                onResult(Result.success(response))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun allocateRoom(
        roomId: Int,
        reqId: Int,
        name: String,
        roomChoice: String,
        username: String,
        password: String,
        phone: String,
        onResult: (Result<AllocateRoomResponse>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.allocateRoom(roomId, reqId, name, roomChoice, username, password, phone)
                onResult(Result.success(response))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun addRoom(roomType: String, roomName: String, onResult: (Result<AddRoomResponse>) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.addRoom(roomType, roomName)
                onResult(Result.success(response))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun loadUpdateRoomData(onResult: (Result<UpdateRoomGetResponse>) -> Unit) {
        viewModelScope.launch {
            try {
                val data = repository.getUpdateRoom()
                onResult(Result.success(data))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun updateRoom(tenantId: Int, roomId: Int, onResult: (Result<UpdateRoomPostResponse>) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = repository.postUpdateRoom(tenantId, roomId)
                onResult(Result.success(resp))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun loadTenants(onResult: (Result<TenantsGetResponse>) -> Unit) {
        viewModelScope.launch {
            try {
                val data = repository.getTenants()
                onResult(Result.success(data))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun updateTenant(tenantId: Int, option: String, value: String, onResult: (Result<PostTenantUpdateResponse>) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = repository.postTenantUpdate(tenantId, option, value)
                onResult(Result.success(resp))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    // suspend helper to read saved auth info (token + role + username + timestamp)
    suspend fun getSavedAuth(): com.fluidpotata.renteasy.data.AuthToken? {
        return repository.getSavedAuth()
    }

}
