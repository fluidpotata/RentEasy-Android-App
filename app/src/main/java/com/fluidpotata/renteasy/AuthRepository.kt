package com.fluidpotata.renteasy

import android.content.Context
import android.content.SharedPreferences
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.core.content.edit

class AuthRepository(context: Context) {
    private val api: ApiService
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://private.fluidpotata.com/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ApiService::class.java)
    }

    private fun saveToken(token: String) {
        prefs.edit { putString("jwt", token) }
    }

    fun getToken(): String? = prefs.getString("jwt", null)

    suspend fun signup(
        name: String,
        username: String,
        phone: String,
        password: String,
        confirmPassword: String,
        roomType: String
    ) = api.signup(SignupRequest(name, username, phone, password, confirmPassword, roomType))

    suspend fun login(username: String, password: String): LoginResponse {
        val response = api.login(LoginRequest(username, password))
        saveToken(response.access_token)
        return response
    }

    suspend fun getAdminDashboard(): AdminDashboardResponse {
        val token = getToken() ?: throw Exception("No token found")
        return api.getAdminDashboard("Bearer $token")
    }

    suspend fun getCustomerDashboard(): CustomerDashboardResponse {
        val token = getToken() ?: throw Exception("No token found")
        return api.getCustomerDashboard("Bearer $token")
    }

    suspend fun getSeeApps(): SeeAppsResponse {
        val token = getToken() ?: throw Exception("No token found")
        return api.getSeeApps("Bearer $token")
    }

    suspend fun getUpdateRoom(): UpdateRoomGetResponse {
        val token = getToken() ?: throw Exception("No token found")
        return api.getUpdateRoom("Bearer $token")
    }

    suspend fun postUpdateRoom(tenantId: Int, roomId: Int): UpdateRoomPostResponse {
        val token = getToken() ?: throw Exception("No token found")
        return api.postUpdateRoom("Bearer $token", UpdateRoomPostRequest(tenantId, roomId))
    }

    suspend fun getTenants(): TenantsGetResponse {
        val token = getToken() ?: throw Exception("No token found")
        return api.getTenants("Bearer $token")
    }

    suspend fun postTenantUpdate(tenantId: Int, option: String, value: String): PostTenantUpdateResponse {
        val token = getToken() ?: throw Exception("No token found")
        return api.postTenantUpdate("Bearer $token", PostTenantUpdateRequest(tenantId, option, value))
    }

    suspend fun getTicketAdmin(): TicketAdminResponse {
        val token = getToken() ?: throw Exception("No token found")
        return api.getTicketAdmin("Bearer $token")
    }

    suspend fun closeTicket(ticketId: Int): CloseTicketResponse {
        val token = getToken() ?: throw Exception("No token found")
        return api.closeTicket("Bearer $token", CloseTicketRequest(ticketId))
    }

    suspend fun allocateRoom(
        roomId: Int,
        reqId: Int,
        name: String,
        roomChoice: String,
        username: String,
        password: String,
        phone: String
    ): AllocateRoomResponse {
        val token = getToken() ?: throw Exception("No token found")
        return api.allocateRoom("Bearer $token", AllocateRoomRequest(roomId, reqId, name, roomChoice, username, password, phone))
    }

    suspend fun addRoom(roomType: String, roomName: String): AddRoomResponse {
        val token = getToken() ?: throw Exception("No token found")
        return api.addRoom("Bearer $token", AddRoomRequest(roomType, roomName))
    }
}
