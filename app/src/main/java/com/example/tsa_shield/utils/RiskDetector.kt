package com.example.tsa_shield.utils

import android.location.Location
import com.example.tsa_shield.model.SafetyZone

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
        var risk = RiskLevel.LOW
        
        for (zone in zones) {
            val results = FloatArray(1)
            Location.distanceBetween(
                currentLocation.latitude, currentLocation.longitude,
                zone.lat, zone.lng, results
            )
            
            val distance = results[0]
            if (distance < zone.radiusInMeters) {
                // If inside a zone, determine risk by its type
                if (!zone.isSafe) {
                    return RiskLevel.HIGH // Danger zone takes precedence
                } else {
                    risk = RiskLevel.LOW // Inside a safe zone
                }
            }
        }
        return risk
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
}
