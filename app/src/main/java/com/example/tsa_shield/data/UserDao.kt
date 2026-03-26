package com.example.tsa_shield.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Query("DELETE FROM user_profile")
    suspend fun deleteProfile()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: SafetyAlert)

    @Query("SELECT * FROM safety_alerts ORDER BY timestamp DESC")
    suspend fun getAllAlerts(): List<SafetyAlert>

    @Query("DELETE FROM safety_alerts")
    suspend fun deleteAllAlerts()
}
