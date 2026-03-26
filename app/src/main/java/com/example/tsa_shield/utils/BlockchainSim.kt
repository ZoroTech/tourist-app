package com.example.tsa_shield.utils

import java.security.MessageDigest

object BlockchainSim {
    fun generateHash(data: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(data.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
