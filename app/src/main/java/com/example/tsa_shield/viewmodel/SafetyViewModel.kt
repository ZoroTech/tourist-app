package com.example.tsa_shield.viewmodel

import android.app.Application
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tsa_shield.data.AppDatabase
import com.example.tsa_shield.data.SafetyAlert
import com.example.tsa_shield.data.UserProfile
import com.example.tsa_shield.model.SafetyZone
import com.example.tsa_shield.utils.BlockchainSim
import com.example.tsa_shield.utils.RiskDetector
import com.example.tsa_shield.utils.RiskLevel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SafetyViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()

    var userLocation by mutableStateOf<LatLng?>(null)
    var riskLevel by mutableStateOf(RiskLevel.LOW)
    var isEmergencyTriggered by mutableStateOf(false)
    var userProfile by mutableStateOf<UserProfile?>(null)
    var isDataTampered by mutableStateOf(false)
    
    var alertHistory by mutableStateOf<List<SafetyAlert>>(emptyList())
    
    // Abnormal situation states
    var isStayingTooLong by mutableStateOf(false)
    private var lastLocationTime: Long = 0
    private var lastSignificantLocation: LatLng? = null
    
    // SOS Cooldown to avoid repeated triggers (in milliseconds)
    private var lastSOSAutoTriggerTime: Long = 0
    private val SOS_COOLDOWN = 60000L // 1 minute cooldown

    private val dangerZones = listOf(
        SafetyZone("Danger Area Alpha", 28.6139, 77.2090, 500.0, false),
        SafetyZone("Safe Zone Beta", 28.6200, 77.2100, 300.0, true)
    )

    init {
        loadProfile()
        loadAlertHistory()
        startInactivityChecker()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = userDao.getUserProfile()
            if (profile != null) {
                val currentHash = BlockchainSim.generateHash("${profile.name}${profile.email}${profile.emergencyContact}")
                isDataTampered = currentHash != profile.profileHash
                userProfile = profile
            }
        }
    }

    private fun loadAlertHistory() {
        viewModelScope.launch {
            alertHistory = userDao.getAllAlerts()
        }
    }

    fun updateLocation(latLng: LatLng) {
        userLocation = latLng
        
        if (lastSignificantLocation == null || getDistance(lastSignificantLocation!!, latLng) > 10.0) {
            lastSignificantLocation = latLng
            lastLocationTime = System.currentTimeMillis()
            isStayingTooLong = false
        }

        evaluateRisk()
    }

    private fun evaluateRisk() {
        val latLng = userLocation ?: return
        val loc = Location("").apply {
            latitude = latLng.latitude
            longitude = latLng.longitude
        }
        
        val zoneRisk = RiskDetector.detectZoneRisk(loc, dangerZones)
        val combinedRisk = RiskDetector.calculateCombinedRisk(zoneRisk, isStayingTooLong)
        
        if (combinedRisk != riskLevel) {
            riskLevel = combinedRisk
            handleRiskLevelChange(riskLevel)
        }
    }

    private fun handleRiskLevelChange(newRisk: RiskLevel) {
        if (newRisk == RiskLevel.HIGH) {
            logAlert("HIGH RISK DETECTED", "User entered an unsafe zone at ${userLocation?.latitude}, ${userLocation?.longitude}")
            autoTriggerSOS()
        } else if (newRisk == RiskLevel.MEDIUM) {
            logAlert("MEDIUM RISK DETECTED", "User has been stationary for too long.")
        }
    }

    private fun autoTriggerSOS() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSOSAutoTriggerTime > SOS_COOLDOWN) {
            lastSOSAutoTriggerTime = currentTime
            triggerSOS(isAuto = true)
        }
    }

    private fun startInactivityChecker() {
        viewModelScope.launch {
            while (true) {
                delay(10000)
                val currentTime = System.currentTimeMillis()
                if (lastLocationTime != 0L && (currentTime - lastLocationTime) > 30000 && !isStayingTooLong) {
                    isStayingTooLong = true
                    evaluateRisk()
                }
            }
        }
    }

    private fun logAlert(type: String, desc: String) {
        viewModelScope.launch {
            val alert = SafetyAlert(
                timestamp = System.currentTimeMillis(),
                alertType = type,
                latitude = userLocation?.latitude ?: 0.0,
                longitude = userLocation?.longitude ?: 0.0,
                description = desc
            )
            userDao.insertAlert(alert)
            loadAlertHistory()
        }
    }

    private fun getDistance(loc1: LatLng, loc2: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(loc1.latitude, loc1.longitude, loc2.latitude, loc2.longitude, results)
        return results[0]
    }

    fun triggerSOS(isAuto: Boolean = false) {
        isEmergencyTriggered = true
        val triggerType = if (isAuto) "AUTOMATIC SOS" else "MANUAL SOS"
        logAlert(triggerType, "SOS system activated. Notifying emergency contacts.")
    }

    fun resetSafetyStatus() {
        isStayingTooLong = false
        lastLocationTime = System.currentTimeMillis()
        evaluateRisk()
    }

    fun resetProfileData() {
        viewModelScope.launch {
            userDao.deleteProfile()
            userDao.deleteAllAlerts()
            userProfile = null
            alertHistory = emptyList()
            riskLevel = RiskLevel.LOW
            isStayingTooLong = false
        }
    }

    fun saveProfile(name: String, email: String, contact: String) {
        val hash = BlockchainSim.generateHash("$name$email$contact")
        val profile = UserProfile(name = name, email = email, emergencyContact = contact, profileHash = hash)
        viewModelScope.launch {
            userDao.insertProfile(profile)
            userProfile = profile
            isDataTampered = false
        }
    }
}
