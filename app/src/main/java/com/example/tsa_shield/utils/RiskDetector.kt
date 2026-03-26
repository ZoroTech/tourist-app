package com.example.tsa_shield.utils

import android.location.Location
import com.example.tsa_shield.model.SafetyZone

enum class SafetyState {
    SAFE,
    UNSAFE_ZONE,
    STATIONARY_TOO_LONG
}

object RiskDetector {
    fun checkSafetyStatus(currentLocation: Location, zones: List<SafetyZone>): SafetyState {
        for (zone in zones) {
            val results = FloatArray(1)
            Location.distanceBetween(
                currentLocation.latitude, currentLocation.longitude,
                zone.lat, zone.lng, results
            )
            if (results[0] < zone.radiusInMeters) {
                return if (zone.isSafe) SafetyState.SAFE else SafetyState.UNSAFE_ZONE
            }
        }
        return SafetyState.SAFE
    }
}
