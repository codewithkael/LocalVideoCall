@file:Suppress("DEPRECATION")

package com.codewithkael.localvideocall.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.math.BigInteger
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.ByteOrder

fun getWifiIPAddress(context: Context): String? {
    // First, try to get the host IP address
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            val addresses = networkInterface.inetAddresses

            while (addresses.hasMoreElements()) {
                val inetAddress = addresses.nextElement()

                // Check if the IP address is not a loopback address and is an IPv4 address
                if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                    return inetAddress.hostAddress
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }

    // If the host IP address could not be determined, try to get the client IP address
    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    var ipAddress = wifiManager.connectionInfo.ipAddress

    // Convert little-endian to big-endian if needed
    if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
        ipAddress = Integer.reverseBytes(ipAddress)
    }

    val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()

    return try {
        InetAddress.getByAddress(ipByteArray).hostAddress
    } catch (ex: Exception) {
        ex.printStackTrace()
        null
    }
}

