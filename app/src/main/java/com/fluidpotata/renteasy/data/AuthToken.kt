package com.fluidpotata.renteasy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_token")
data class AuthToken(
    @PrimaryKey val id: Int = 1,
    val token: String,
    val role: String? = null,
    val username: String? = null,
    val userId: Int? = null,
    val timestamp: Long
)
