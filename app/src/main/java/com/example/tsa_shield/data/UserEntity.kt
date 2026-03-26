package com.example.tsa_shield.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val emergencyContact: String,
    val profileHash: String // The "Blockchain" ID
)
