package com.m391.primavera

import android.os.Build
import java.net.NetworkInterface
import java.security.MessageDigest

object DeviceIdGenerator {

    fun generateDeviceId(): String {
        // Get the MAC address of the device
        val macAddress = getMacAddress()

        // Get the Android ID of the device
        val androidId = getAndroidId()

        // Concatenate the MAC address and Android ID
        val combined = "$macAddress$androidId"

        // Generate an MD5 hash of the concatenated string
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(combined.toByteArray())

        // Convert the MD5 hash to a hex string
        val hexString = StringBuilder()
        for (byte in digest) {
            hexString.append(String.format("%02x", byte))
        }

        return hexString.toString()
    }

    private fun getMacAddress(): String {
        // Get the MAC address of the device
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()
            val macBytes = networkInterface.hardwareAddress
            if (macBytes != null && macBytes.isNotEmpty()) {
                val mac = StringBuilder()
                for (byte in macBytes) {
                    mac.append(String.format("%02x", byte))
                }
                return mac.toString()
            }
        }
        return ""
    }

    private fun getAndroidId(): String {
        // Get the Android ID of the device
        return Build.ID
    }
}