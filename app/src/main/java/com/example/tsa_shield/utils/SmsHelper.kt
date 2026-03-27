package com.example.tsa_shield.utils

import android.telephony.SmsManager
import android.util.Log

object SmsHelper {
    /**
     * Sends an SOS message to a list of phone numbers.
     * message includes the alert text and a Google Maps live location link.
     */
    fun sendSOSMessages(phoneNumbers: List<String>, message: String) {
        val smsManager: SmsManager = SmsManager.getDefault()
        for (number in phoneNumbers) {
            try {
                if (number.isNotBlank()) {
                    // divideMessage handles long messages that exceed 160 characters
                    val parts = smsManager.divideMessage(message)
                    smsManager.sendMultipartTextMessage(number, null, parts, null, null)
                    Log.d("SmsHelper", "SOS SMS sent to $number")
                }
            } catch (e: Exception) {
                Log.e("SmsHelper", "Failed to send SOS SMS to $number", e)
            }
        }
    }
}
