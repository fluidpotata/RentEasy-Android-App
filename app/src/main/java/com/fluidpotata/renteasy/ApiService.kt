package com.fluidpotata.renteasy

import retrofit2.http.Body
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

interface ApiService {
    @POST("signup")
    @Headers("Content-Type: application/json")
    suspend fun signup(@Body request: SignupRequest): SignupResponse

    @POST("login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}


