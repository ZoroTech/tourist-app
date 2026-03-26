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
import com.example.tsa_shield.utils.SafetyState
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SafetyViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()

    var userLocation by mutableStateOf<LatLng?>(null)
    var safetyStatus by mutableStateOf(SafetyState.SAFE)
    var isEmergencyTriggered by mutableStateOf(false)
    var userProfile by mutableStateOf<UserProfile?>(null)
    var isDataTampered by mutableStateOf(false)
    
    var alertHistory by mutableStateOf<List<SafetyAlert>>(emptyList())
    
    // Abnormal situation states
    var isStayingTooLong by mutableStateOf(false)
    private var lastLocationTime: Long = 0
    private var lastSignificantLocation: LatLng? = null

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

        val loc = Location("").apply {
            latitude = latLng.latitude
            longitude = latLng.longitude
        }
        
        val newState = RiskDetector.checkSafetyStatus(loc, dangerZones)
        if (!isStayingTooLong && newState != safetyStatus) {
            if (newState == SafetyState.UNSAFE_ZONE) {
                logAlert("Unsafe Zone Entry", "Entered restricted area at ${latLng.latitude}, ${latLng.longitude}")
            }
            safetyStatus = newState
        }
    }

    private fun startInactivityChecker() {
        viewModelScope.launch {
            while (true) {
                delay(10000)
                val currentTime = System.currentTimeMillis()
                if (lastLocationTime != 0L && (currentTime - lastLocationTime) > 30000 && !isStayingTooLong) {
                    isStayingTooLong = true
                    safetyStatus = SafetyState.STATIONARY_TOO_LONG
                    logAlert("Inactivity Detected", "User stationary for more than 30 seconds")
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

    fun triggerSOS() {
        isEmergencyTriggered = true
        logAlert("SOS TRIGGERED", "Manual SOS button pressed")
    }

    fun resetSafetyStatus() {
        isStayingTooLong = false
        lastLocationTime = System.currentTimeMillis()
        userLocation?.let {
            val loc = Location("").apply {
                latitude = it.latitude
                longitude = it.longitude
            }
            safetyStatus = RiskDetector.checkSafetyStatus(loc, dangerZones)
        } ?: run {
            safetyStatus = SafetyState.SAFE
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
