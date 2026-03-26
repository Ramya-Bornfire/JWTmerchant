package com.bornfire.merchantqrcode.Utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import java.net.NetworkInterface
import java.util.*

object LoginSessionHandling {
    //Function to get the os version of the mobile device
    fun getOSVersion(): String {
        return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    // Function to get IP address based on network type
    fun getIPAddress(context: Context): String? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        return when {
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> {
                getWifiIPAddress(context)
            }
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                getLocalIPAddress()
            }
            else -> {
                null
            }
        }
    }
    // Function to get IP address from Wi-Fi connection
    fun getWifiIPAddress(context: Context): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo: WifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress
        return String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xFF,
            ipAddress shr 8 and 0xFF,
            ipAddress shr 16 and 0xFF,
            ipAddress shr 24 and 0xFF
        )
    }
    // Function to get the local IP address from all network interfaces
    fun getLocalIPAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address.address.size == 4) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}