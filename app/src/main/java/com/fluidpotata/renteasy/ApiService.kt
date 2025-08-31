package com.fluidpotata.renteasy

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName


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
data class LoginResponse(val access_token: String, val message: String, val role: String, val id: Int?)


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
    val bill: Any?,
    val internetbill: Any?,
    val utilitybill: Any?,
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

// Customer tickets (tenant) response
data class TicketsGetResponse(val tickets: List<List<Any>>) {
    val ticketList: List<Ticket> get() = tickets.map { list ->
        Ticket(
            id = (list.getOrNull(0) as? Number)?.toInt() ?: 0,
            userId = (list.getOrNull(1) as? Number)?.toInt() ?: 0,
            category = list.getOrNull(2) as? String ?: "",
            subject = list.getOrNull(3) as? String ?: "",
            status = list.getOrNull(4) as? String ?: "",
            username = ""
        )
    }
}

data class CreateTicketRequest(val category: String, val description: String)
data class CreateTicketResponse(val message: String)

// Add these data classes
data class AddRoomRequest(val roomType: String, val roomName: String)
data class AddRoomResponse(val message: String)

// Update room API data classes
data class TenantShort(
    val id: Int,
    val name: String
)

data class UpdateRoomGetResponse(
    val tenants: List<List<Any>>,
    val rooms: List<List<Any>>
) {
    val tenantList: List<TenantShort> get() = tenants.map { list ->
        TenantShort(
            id = (list[0] as Number).toInt(),
            name = (list.getOrNull(2) as? String) ?: ""
        )
    }

    val roomList: List<AvailableRoom> get() = rooms.map { list ->
        AvailableRoom(
            id = (list[0] as Number).toInt(),
            roomName = list.getOrNull(1) as? String ?: "",
            type = list.getOrNull(2) as? String ?: "",
            status = list.getOrNull(3) as? String ?: ""
        )
    }
}

data class UpdateRoomPostRequest(val tenantid: Int, val roomid: Int)
data class UpdateRoomPostResponse(val message: String)

// Tenants API
data class TenantItem(
    val id: Int,
    val userId: Int,
    val name: String,
    val packageId: Int,
    val phone: String
)

data class TenantsGetResponse(val tenants: List<List<Any>>) {
    val tenantList: List<TenantItem> get() = tenants.map { list ->
        TenantItem(
            id = (list.getOrNull(0) as? Number)?.toInt() ?: 0,
            userId = (list.getOrNull(1) as? Number)?.toInt() ?: 0,
            name = list.getOrNull(2) as? String ?: "",
            packageId = (list.getOrNull(3) as? Number)?.toInt() ?: 0,
            phone = list.getOrNull(4) as? String ?: ""
        )
    }
}

data class PostTenantUpdateRequest(
    val tenantid: Int,
    val option: String,
    @SerializedName("val") val value: String
)

data class PostTenantUpdateResponse(val message: String)

// Generate bills response
data class GenerateBillResponse(
    val message: String? = null,
    val error: String? = null
)

// Bill item and bills response used by seebills endpoints
data class BillItem(
    val serial: Int,
    val name: String,
    val amount: String,
    val month: String,
    val status: String,
    val tenantId: Int? = null,
    val trxId: String? = null,
    val type: String? = null
) {
    companion object {
        fun fromArray(arr: List<Any?>): BillItem {
            // The backend currently returns two variants:
            // Short form: [serial, name, amount, month, status]
            // Long form:  [paymentId, tenantId, billtypeid, status, trxid, month, amount, type]
            // Detect by checking size and types and map accordingly.
            // Helper to parse int-like values safely
            fun parseInt(v: Any?): Int {
                return when (v) {
                    is Number -> v.toInt()
                    is String -> v.toIntOrNull() ?: 0
                    else -> 0
                }
            }

            // Short form detection: second element is a String (name) and size >=5
            if (arr.size >= 5 && arr.getOrNull(1) is String) {
                val serial = parseInt(arr.getOrNull(0))
                val name = arr.getOrNull(1)?.toString() ?: ""
                val amount = arr.getOrNull(2)?.toString() ?: "-"
                val month = arr.getOrNull(3)?.toString() ?: ""
                val status = arr.getOrNull(4)?.toString() ?: ""
                return BillItem(serial = serial, name = name, amount = amount, month = month, status = status)
            }

            // Fallback to long form mapping
            val serial = parseInt(arr.getOrNull(0))
            val tenantId = when (val v = arr.getOrNull(1)) {
                is Number -> v.toInt()
                is String -> v.toIntOrNull()
                else -> null
            }
            val status = arr.getOrNull(3)?.toString() ?: ""
            val trx = arr.getOrNull(4)?.toString()
            val month = arr.getOrNull(5)?.toString() ?: ""
            val amount = arr.getOrNull(6)?.toString() ?: "-"
            val type = arr.getOrNull(7)?.toString()
            // name is not provided by the long-form endpoint; use tenantId as placeholder when available
            val name = arr.getOrNull(2)?.toString()?.takeIf { it.isNotBlank() }
                ?: tenantId?.let { "Tenant #$it" } ?: ""
            return BillItem(serial = serial, name = name, amount = amount, month = month, status = status, tenantId = tenantId, trxId = trx, type = type)
        }
    }
}

data class BillsGetResponse(val bills: List<List<Any>>) {
    val billList: List<BillItem> get() = bills.map { arr ->
        // server returns arrays like: [serial, name, amount, month, status]
        // map each inner array to a BillItem using the helper
        BillItem.fromArray(arr)
    }
}

// include both tenantid and id to be compatible with backend variations
data class PayBillRequest(
    val id: Int,     // userId
    val bill: Int,
    val tID: String 
)

data class PayBillResponse(val message: String)

// Public rent info endpoint response (no auth required)
data class RentInfo(
    val internet: Int? = null,
    val rent: Int? = null,
    val utility: Int? = null
)




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

    @GET("ticket")
    suspend fun getTickets(@Header("Authorization") token: String): TicketsGetResponse

    @POST("ticket")
    @Headers("Content-Type: application/json")
    suspend fun createTicket(@Header("Authorization") token: String, @Body request: CreateTicketRequest): CreateTicketResponse

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

    @GET("updateroom")
    suspend fun getUpdateRoom(@Header("Authorization") token: String): UpdateRoomGetResponse

    @POST("updateroom")
    @Headers("Content-Type: application/json")
    suspend fun postUpdateRoom(@Header("Authorization") token: String, @Body request: UpdateRoomPostRequest): UpdateRoomPostResponse

    @GET("tenants")
    suspend fun getTenants(@Header("Authorization") token: String): TenantsGetResponse

    @POST("tenants")
    @Headers("Content-Type: application/json")
    suspend fun postTenantUpdate(@Header("Authorization") token: String, @Body request: PostTenantUpdateRequest): PostTenantUpdateResponse

    @GET("generatebill")
    suspend fun generateBills(@Header("Authorization") token: String): GenerateBillResponse

    @GET("seebills/internet")
    suspend fun getInternetBills(@Header("Authorization") token: String): BillsGetResponse

    @GET("seebills/utility")
    suspend fun getUtilityBills(@Header("Authorization") token: String): BillsGetResponse

    @GET("seebills/rent")
    suspend fun getRentBills(@Header("Authorization") token: String): BillsGetResponse

    // tenant payment endpoints
    @GET("paybill")
    suspend fun getMyBills(@Header("Authorization") token: String): BillsGetResponse

    @POST("paybill")
    suspend fun payBill(
        @Header("Authorization") token: String,
        @Body req: PayBillRequest
    ): PayBillResponse

    @GET("unverifiedbills")
    suspend fun getUnverifiedBills(@Header("Authorization") token: String): BillsGetResponse

    @POST("unverifiedbills")
    @Headers("Content-Type: application/json")
    suspend fun verifyBill(@Header("Authorization") token: String, @Body request: Map<String, Int>): GenerateBillResponse

    // Public endpoint: fetch current rent/internet/utility amounts for a tenant by user id
    @GET("getrent/{userid}")
    suspend fun getRent(@Path("userid") userId: Int): RentInfo


}
