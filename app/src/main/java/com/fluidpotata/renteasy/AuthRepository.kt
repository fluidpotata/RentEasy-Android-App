package com.fluidpotata.renteasy

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepository {
    private val api: ApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://private.fluidpotata.com/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ApiService::class.java)
    }

    suspend fun signup(
        name: String,
        username: String,
        phone: String,
        password: String,
        confirmPassword: String,
        roomType: String
    ) = api.signup(SignupRequest(name, username, phone, password, confirmPassword, roomType))

    suspend fun login(username: String, password: String) =
        api.login(LoginRequest(username, password))
}
