package com.example.tsa_shield.model

data class SafetyZone(
    val name: String,
    val lat: Double,
    val lng: Double,
    val radiusInMeters: Double,
    val isSafe: Boolean
)
