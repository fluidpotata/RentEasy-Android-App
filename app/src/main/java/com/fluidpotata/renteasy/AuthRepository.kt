package com.fluidpotata.renteasy

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.fluidpotata.renteasy.data.AppDatabase
import com.fluidpotata.renteasy.data.AuthToken
import com.fluidpotata.renteasy.data.AuthTokenDao
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.edit

class AuthRepository(context: Context) {
    private val api: ApiService
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val db: AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "renteasy-db")
        .fallbackToDestructiveMigration()
        .build()
    private val tokenDao: AuthTokenDao = db.authTokenDao()

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://private.fluidpotata.com/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ApiService::class.java)
    }

    private fun saveTokenToPrefs(token: String) {
        prefs.edit { putString("jwt", token) }
    }

    private fun saveTokenToDb(token: String, role: String? = null, username: String? = null, userId: Int? = null) {
        val now = System.currentTimeMillis()
        val authToken = AuthToken(token = token, role = role, username = username, userId = userId, timestamp = now)
        CoroutineScope(Dispatchers.IO).launch {
            tokenDao.upsert(authToken)
        }
    }

    // synchronous fast token from prefs (no DB access)
    fun getToken(): String? = prefs.getString("jwt", null)

    // suspend-safe token fetch that checks DB expiry and falls back to prefs
    suspend fun getValidToken(): String? {
        val tokenObj = withContext(Dispatchers.IO) { tokenDao.getToken() }
        if (tokenObj != null) {
            val age = System.currentTimeMillis() - tokenObj.timestamp
            if (age <= 60 * 60 * 1000L) return tokenObj.token
            // expired - clear
            CoroutineScope(Dispatchers.IO).launch { tokenDao.clear() }
            return null
        }
        return prefs.getString("jwt", null)
    }

    // suspend access to saved token record
    suspend fun getSavedAuth(): com.fluidpotata.renteasy.data.AuthToken? {
        return withContext(Dispatchers.IO) { tokenDao.getToken() }
    }

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
        // persist token to DB with timestamp and user id
        saveTokenToPrefs(response.access_token)
        saveTokenToDb(response.access_token, role = response.role, username = username, userId = response.id)
        return response
    }

    suspend fun getAdminDashboard(): AdminDashboardResponse {
    val token = getValidToken() ?: throw Exception("No token found")
    return api.getAdminDashboard("Bearer $token")
    }

    suspend fun getCustomerDashboard(): CustomerDashboardResponse {
    val token = getValidToken() ?: throw Exception("No token found")
    return api.getCustomerDashboard("Bearer $token")
    }

    suspend fun getSeeApps(): SeeAppsResponse {
    val token = getValidToken() ?: throw Exception("No token found")
    return api.getSeeApps("Bearer $token")
    }

    suspend fun getUpdateRoom(): UpdateRoomGetResponse {
    val token = getValidToken() ?: throw Exception("No token found")
    return api.getUpdateRoom("Bearer $token")
    }

    suspend fun postUpdateRoom(tenantId: Int, roomId: Int): UpdateRoomPostResponse {
    val token = getValidToken() ?: throw Exception("No token found")
    return api.postUpdateRoom("Bearer $token", UpdateRoomPostRequest(tenantId, roomId))
    }

    suspend fun getTenants(): TenantsGetResponse {
    val token = getValidToken() ?: throw Exception("No token found")
    return api.getTenants("Bearer $token")
    }

    suspend fun postTenantUpdate(tenantId: Int, option: String, value: String): PostTenantUpdateResponse {
    val token = getValidToken() ?: throw Exception("No token found")
    return api.postTenantUpdate("Bearer $token", PostTenantUpdateRequest(tenantId, option, value))
    }

    suspend fun getTicketAdmin(): TicketAdminResponse {
    val token = getValidToken() ?: throw Exception("No token found")
    return api.getTicketAdmin("Bearer $token")
    }

    suspend fun closeTicket(ticketId: Int): CloseTicketResponse {
    val token = getValidToken() ?: throw Exception("No token found")
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
    val token = getValidToken() ?: throw Exception("No token found")
    return api.allocateRoom("Bearer $token", AllocateRoomRequest(roomId, reqId, name, roomChoice, username, password, phone))
    }

    suspend fun addRoom(roomType: String, roomName: String): AddRoomResponse {
    val token = getValidToken() ?: throw Exception("No token found")
    return api.addRoom("Bearer $token", AddRoomRequest(roomType, roomName))
    }

    // Generate bills
    suspend fun generateBills(): GenerateBillResponse {
        val token = getValidToken() ?: throw Exception("No token found")
        return api.generateBills("Bearer $token")
    }

    // Bills endpoints
    suspend fun getInternetBills(): BillsGetResponse {
        val token = getValidToken() ?: throw Exception("No token found")
        return api.getInternetBills("Bearer $token")
    }

    suspend fun getUtilityBills(): BillsGetResponse {
        val token = getValidToken() ?: throw Exception("No token found")
        return api.getUtilityBills("Bearer $token")
    }

    suspend fun getRentBills(): BillsGetResponse {
        val token = getValidToken() ?: throw Exception("No token found")
        return api.getRentBills("Bearer $token")
    }

}
