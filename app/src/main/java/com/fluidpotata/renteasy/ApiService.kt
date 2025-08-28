package com.fluidpotata.renteasy

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST


data class SignupRequest(
    val name: String,
    val username: String,
    val phone: String,
    val password: String,
    val confirm_password: String,
    val room_type: String
)

data class SignupResponse(val message: String)

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val access_token: String, val message: String, val role: String)


data class AdminDashboardResponse(
    val tickets: List<List<Any>>,
    val count: List<List<Int>>,
    val joinreqs: Int,
    val rent: Int,
    val internet: Int,
    val utility: Int
) {
    val totalTickets: Int get() = count.firstOrNull()?.firstOrNull() ?: tickets.size
}

data class CustomerDashboardResponse(
    val username: String,
    val `package`: String,
    val bill: Boolean,
    val internetbill: Boolean,
    val utilitybill: Boolean,
    val ticketCount: Int
) {
    val rentUnpaid get() = bill != false
    val internetUnpaid get() = internetbill != false
    val utilityUnpaid get() = utilitybill != false
}

data class SeeAppsResponse(
    val requests: List<List<Any>>,
    val available_rooms: List<List<Any>>
) {
    val requestList: List<ApplicationRequest> get() = requests.map { list ->
        ApplicationRequest(
            id = (list[0] as Number).toInt(),
            name = list[1] as String,
            roomChoice = list[2] as String,
            username = list[3] as String,
            password = list[4] as String,
            phone = list[5] as String
        )
    }

    val availableRoomList: List<AvailableRoom> get() = available_rooms.map { list ->
        AvailableRoom(
            id = (list[0] as Number).toInt(),
            roomName = list[1] as String,
            type = list[2] as String,
            status = list[3] as String
        )
    }
}

data class ApplicationRequest(
    val id: Int,
    val name: String,
    val roomChoice: String,
    val username: String,
    val password: String,
    val phone: String
)

data class AvailableRoom(
    val id: Int,
    val roomName: String,
    val type: String,
    val status: String
)

data class Ticket(
    val id: Int,
    val userId: Int,
    val category: String,
    val subject: String,
    val status: String,
    val username: String
)

data class TicketAdminResponse(
    val tickets: List<List<Any>>
) {
    val ticketList: List<Ticket> get() = tickets.map { list ->
        Ticket(
            id = (list[0] as Number).toInt(),
            userId = (list[1] as Number).toInt(),
            category = list[2] as String,
            subject = list[3] as String,
            status = list[4] as String,
            username = list[5] as String
        )
    }
}

data class AllocateRoomRequest(
    val room_id: Int,
    val req_id: Int,
    val name: String,
    val roomChoice: String,
    val username: String,
    val password: String,
    val phone: String
)
data class AllocateRoomResponse(val message: String)


data class CloseTicketRequest(val ticket_id: Int)
data class CloseTicketResponse(val message: String)

// Add these data classes
data class AddRoomRequest(val roomType: String, val roomName: String)
data class AddRoomResponse(val message: String)




interface ApiService {
    @POST("signup")
    @Headers("Content-Type: application/json")
    suspend fun signup(@Body request: SignupRequest): SignupResponse

    @POST("login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("admin")
    suspend fun getAdminDashboard(
        @Header("Authorization") token: String
    ): AdminDashboardResponse

    @GET("customer")
    suspend fun getCustomerDashboard(
        @Header("Authorization") token: String
    ): CustomerDashboardResponse

    @GET("seeapps")
    suspend fun getSeeApps(@Header("Authorization") token: String): SeeAppsResponse

    @GET("ticketadmin")
    suspend fun getTicketAdmin(@Header("Authorization") token: String): TicketAdminResponse

    @POST("ticketadmin")
    @Headers("Content-Type: application/json")
    suspend fun closeTicket(@Header("Authorization") token: String, @Body request: CloseTicketRequest): CloseTicketResponse

    @POST("seeapps")
    @Headers("Content-Type: application/json")
    suspend fun allocateRoom(@Header("Authorization") token: String, @Body request: AllocateRoomRequest): AllocateRoomResponse

    @POST("rooms")
    @Headers("Content-Type: application/json")
    suspend fun addRoom(
        @Header("Authorization") token: String,
        @Body request: AddRoomRequest
    ): AddRoomResponse
}
