package com.example.tsa_shield.utils

import android.location.Location
import com.example.tsa_shield.model.SafetyZone

/**
 * Defines the user's overall safety state.
 */
enum class UserSafetyState {
    SAFE,
    AT_RISK,
    EMERGENCY
}

/**
 * Defines the calculated risk levels for the tourist.
 */
enum class RiskLevel {
    LOW,    // Safe or neutral environment
    MEDIUM, // Potential issue (e.g., stationary too long)
    HIGH    // Immediate danger (e.g., in an unsafe zone)
}

/**
 * Utility to detect risk based on location geofencing and behavioral patterns.
 */
object RiskDetector {

    /**
     * Determines the risk level based on the current location relative to safety zones.
     */
    fun detectZoneRisk(currentLocation: Location, zones: List<SafetyZone>): RiskLevel {
        for (zone in zones) {
            val results = FloatArray(1)
            Location.distanceBetween(
                currentLocation.latitude, currentLocation.longitude,
                zone.lat, zone.lng, results
            )
            
            val distance = results[0]
            if (distance < zone.radiusInMeters) {
                if (!zone.isSafe) return RiskLevel.HIGH
            }
        }
        return RiskLevel.LOW
    }

    /**
     * Combines multiple factors (Location Risk + Inactivity) into a final Risk Level.
     */
    fun calculateCombinedRisk(zoneRisk: RiskLevel, isStationaryTooLong: Boolean): RiskLevel {
        return when {
            zoneRisk == RiskLevel.HIGH -> RiskLevel.HIGH
            isStationaryTooLong -> RiskLevel.MEDIUM
            else -> zoneRisk
        }
    }

    /**
     * Maps RiskLevel to a UserSafetyState.
     */
    fun getSafetyState(risk: RiskLevel, isEmergencyTriggered: Boolean): UserSafetyState {
        return when {
            isEmergencyTriggered -> UserSafetyState.EMERGENCY
            risk == RiskLevel.HIGH -> UserSafetyState.AT_RISK
            else -> UserSafetyState.SAFE
        }
    }
}
