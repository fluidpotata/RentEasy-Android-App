package com.fluidpotata.renteasy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AuthTokenDao {
    @Query("SELECT * FROM auth_token WHERE id = 1 LIMIT 1")
    fun getToken(): AuthToken?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(token: AuthToken)

    @Query("DELETE FROM auth_token WHERE id = 1")
    fun clear()
}
