package com.fluidpotata.renteasy.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AuthToken::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authTokenDao(): AuthTokenDao
}
