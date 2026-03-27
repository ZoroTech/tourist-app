package com.example.tsa_shield.data

import androidx.room.Dao
import androidx.room.Delete
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

    // Incident Management
    @Insert
    suspend fun insertIncident(incident: IncidentEntity)

    @Query("SELECT * FROM incidents ORDER BY timestamp DESC")
    suspend fun getAllIncidents(): List<IncidentEntity>

    // Emergency Contacts Management
    @Query("SELECT * FROM emergency_contacts")
    suspend fun getAllContacts(): List<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    // Alert History (keeping for backward compatibility or general logging)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: SafetyAlert)

    @Query("SELECT * FROM safety_alerts ORDER BY timestamp DESC")
    suspend fun getAllAlerts(): List<SafetyAlert>

    @Query("DELETE FROM safety_alerts")
    suspend fun deleteAllAlerts()
}
