package com.codewithkael.localvideocall.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteOrder


fun getWifiIPAddress(context: Context): String? {
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