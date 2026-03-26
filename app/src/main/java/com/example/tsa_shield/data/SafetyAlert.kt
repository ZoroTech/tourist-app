package com.example.tsa_shield.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "safety_alerts")
data class SafetyAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val alertType: String,
    val latitude: Double,
    val longitude: Double,
    val description: String
)
