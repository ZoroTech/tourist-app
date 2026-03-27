package com.example.tsa_shield.viewmodel

import android.app.Application
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tsa_shield.data.AppDatabase
import com.example.tsa_shield.data.ContactEntity
import com.example.tsa_shield.data.IncidentEntity
import com.example.tsa_shield.data.SafetyAlert
import com.example.tsa_shield.data.UserProfile
import com.example.tsa_shield.model.SafetyZone
import com.example.tsa_shield.utils.BlockchainSim
import com.example.tsa_shield.utils.RiskDetector
import com.example.tsa_shield.utils.RiskLevel
import com.example.tsa_shield.utils.SmsHelper
import com.example.tsa_shield.utils.UserSafetyState
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SafetyViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()

    // State Management
    var userLocation by mutableStateOf<LatLng?>(null)
    var riskLevel by mutableStateOf(RiskLevel.LOW)
    var safetyState by mutableStateOf(UserSafetyState.SAFE)
    var isEmergencyTriggered by mutableStateOf(false)
    var isMonitoringOn by mutableStateOf(true) // Task 1: Monitoring Toggle
    
    var userProfile by mutableStateOf<UserProfile?>(null)
    var isDataTampered by mutableStateOf(false)
    
    var incidentHistory by mutableStateOf<List<IncidentEntity>>(emptyList())
    var emergencyContacts by mutableStateOf<List<ContactEntity>>(emptyList())
    
    // Abnormal situation states
    var isStayingTooLong by mutableStateOf(false)
    private var lastLocationTime: Long = 0
    private var lastSignificantLocation: LatLng? = null
    
    // Tracking Jobs
    private var emergencyTrackingJob: Job? = null
    private var monitoringJob: Job? = null

    private val dangerZones = listOf(
        SafetyZone("Danger Area Alpha", 28.6139, 77.2090, 500.0, false),
        SafetyZone("Safe Zone Beta", 28.6200, 77.2100, 300.0, true)
    )

    init {
        loadProfile()
        loadIncidentHistory()
        loadContacts()
        startInactivityChecker()
        startContinuousMonitoring() // Task 1
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

    private fun loadIncidentHistory() {
        viewModelScope.launch {
            incidentHistory = userDao.getAllIncidents()
        }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            emergencyContacts = userDao.getAllContacts()
        }
    }

    // Task 1: Continuous Monitoring
    private fun startContinuousMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = viewModelScope.launch {
            while (isActive) {
                if (isMonitoringOn) {
                    evaluateRisk()
                }
                delay(10000) // 10 seconds interval
            }
        }
    }

    fun toggleMonitoring(isOn: Boolean) {
        isMonitoringOn = isOn
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
            safetyState = RiskDetector.getSafetyState(riskLevel, isEmergencyTriggered)
            handleRiskLevelChange(riskLevel)
        }
    }

    private fun handleRiskLevelChange(newRisk: RiskLevel) {
        when (newRisk) {
            RiskLevel.HIGH -> {
                logIncident(RiskLevel.HIGH.name, "ZONE_ENTRY")
                autoTriggerSOS()
            }
            RiskLevel.MEDIUM -> {
                logIncident(RiskLevel.MEDIUM.name, "INACTIVITY")
            }
            else -> {}
        }
    }

    private fun autoTriggerSOS() {
        if (!isEmergencyTriggered) {
            triggerSOS(isAuto = true)
        }
    }

    private fun startInactivityChecker() {
        viewModelScope.launch {
            while (isActive) {
                delay(10000)
                val currentTime = System.currentTimeMillis()
                if (lastLocationTime != 0L && (currentTime - lastLocationTime) > 30000 && !isStayingTooLong) {
                    isStayingTooLong = true
                    evaluateRisk()
                }
            }
        }
    }

    // Task 2: Incident Logging
    private fun logIncident(riskLevel: String, type: String) {
        viewModelScope.launch {
            val incident = IncidentEntity(
                timestamp = System.currentTimeMillis(),
                latitude = userLocation?.latitude ?: 0.0,
                longitude = userLocation?.longitude ?: 0.0,
                riskLevel = riskLevel,
                type = type
            )
            userDao.insertIncident(incident)
            loadIncidentHistory()
        }
    }

    private fun getDistance(loc1: LatLng, loc2: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(loc1.latitude, loc1.longitude, loc2.latitude, loc2.longitude, results)
        return results[0]
    }

    /**
     * Task 4: Emergency SOS System with Continuous Tracking
     */
    fun triggerSOS(isAuto: Boolean = false) {
        if (isEmergencyTriggered) return
        
        isEmergencyTriggered = true
        safetyState = UserSafetyState.EMERGENCY
        val triggerType = if (isAuto) "AUTOMATIC SOS" else "MANUAL SOS"
        logIncident(RiskLevel.HIGH.name, "SOS")
        
        startEmergencyTracking()
    }

    private fun startEmergencyTracking() {
        emergencyTrackingJob?.cancel()
        emergencyTrackingJob = viewModelScope.launch {
            while (isEmergencyTriggered) {
                sendEmergencySMS()
                delay(15000) // Repeated alert every 15 seconds
            }
        }
    }

    fun cancelSOS() {
        isEmergencyTriggered = false
        safetyState = RiskDetector.getSafetyState(riskLevel, false)
        emergencyTrackingJob?.cancel()
    }

    private fun sendEmergencySMS() {
        val profile = userProfile ?: return
        val latLng = userLocation ?: return
        
        val googleMapsLink = "https://maps.google.com/?q=${latLng.latitude},${latLng.longitude}"
        val message = "EMERGENCY ALERT: Tourist ${profile.name} is in danger. Current Location: $googleMapsLink"
        
        // Use database contacts + legacy contact string
        val dbContacts = emergencyContacts.map { it.phoneNumber }
        val legacyContacts = profile.emergencyContact.split(",").map { it.trim() }
        val allContacts = (dbContacts + legacyContacts).distinct().filter { it.isNotBlank() }
        
        SmsHelper.sendSOSMessages(allContacts, message)
    }

    // Task 6: Emergency Contacts Management
    fun addContact(name: String, phone: String) {
        viewModelScope.launch {
            userDao.insertContact(ContactEntity(name = name, phoneNumber = phone))
            loadContacts()
        }
    }

    fun removeContact(contact: ContactEntity) {
        viewModelScope.launch {
            userDao.deleteContact(contact)
            loadContacts()
        }
    }

    // Task 8: Demo Mode
    fun simulateEmergency() {
        triggerSOS(isAuto = false)
        logIncident("HIGH", "MANUAL_SIMULATION")
    }

    fun resetSafetyStatus() {
        isStayingTooLong = false
        lastLocationTime = System.currentTimeMillis()
        evaluateRisk()
    }

    fun resetProfileData() {
        viewModelScope.launch {
            userDao.deleteProfile()
            userProfile = null
            riskLevel = RiskLevel.LOW
            safetyState = UserSafetyState.SAFE
            isEmergencyTriggered = false
            emergencyTrackingJob?.cancel()
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
