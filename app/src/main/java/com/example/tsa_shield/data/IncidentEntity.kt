package com.example.tsa_shield.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val riskLevel: String,
    val type: String // ZONE_ENTRY, INACTIVITY, SOS, MANUAL_SIMULATION
)
