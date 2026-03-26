package com.bornfire.merchantqrcode

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import com.scottyab.rootbeer.RootBeer
import java.io.*
import java.net.*
import java.util.*

class SplashActivity : AppCompatActivity() {
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashactivity)

        supportActionBar?.hide()

        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        requestedOrientation = if (isTablet) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        if (isDeviceRooted() || isDeviceCompromised(this) || isUsingVPN() || isUsingProxy(this)||checkFridaFiles()) {
            Toast.makeText(this, "Security issue detected! Exiting...", Toast.LENGTH_LONG).show()
            finishAffinity() // Close the app
            return
        }


        val animationZoomOut = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
        textView = findViewById(R.id.splashText)
        textView.startAnimation(animationZoomOut)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
    fun checkFridaFiles(): Boolean {
        val paths = arrayOf(
            "/data/local/tmp/frida-server",
            "/data/local/tmp/frida-agent.so",
            "/data/local/tmp/libgadget.so",
            "/system/bin/frida-server",
            "/system/xbin/frida-server"
        )

        paths.forEach { path ->
            val file = File(path)
            if (file.exists()) {
                return true
            }
        }
        return false
    }

    private fun isDeviceRooted(): Boolean {
        val rootBeer = RootBeer(this)
        return rootBeer.isRooted
    }
    fun isFridaRunning(): Boolean {
        val fridaProcesses = listOf("frida", "gum-js", "gadget")
        val mapsFile = File("/proc/self/maps")

        if (mapsFile.exists()) {
            val content = mapsFile.readText()
            fridaProcesses.forEach { process ->
                if (content.contains(process)) {
                    return true
                }
            }
        }
        return false
    }


    fun isRootBinaryPresent(): Boolean {
        val rootBinaries = listOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/xbin/su"
        )
        return rootBinaries.any { File(it).exists() }
    }

    fun isRootManagementAppInstalled(context: Context): Boolean {
        val rootApps = listOf(
            "com.noshufou.android.su", "eu.chainfire.supersu",
            "com.koushikdutta.superuser", "com.zachspong.temprootremovejb"
        )
        return rootApps.any {
            try {
                context.packageManager.getPackageInfo(it, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    fun isDangerousPropsSet(): Boolean {
        val props = listOf(
            "ro.debuggable" to "1",
            "ro.secure" to "0"
        )
        return props.any { (key, expected) ->
            try {
                val process = Runtime.getRuntime().exec("getprop $key")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val value = reader.readLine()
                value == expected
            } catch (e: Exception) {
                false
            }
        }
    }

    fun hasTestKeys(): Boolean {
        return Build.TAGS?.contains("test-keys") == true
    }

    fun canExecuteSuCommand(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            process.inputStream.bufferedReader().readLine() != null
        } catch (e: Exception) {
            false
        }
    }

    fun isFridaPortOpen(): Boolean {
        val ports = listOf(27042, 27043)
        return ports.any {
            try {
                Socket("127.0.0.1", it).use { _ -> true }
            } catch (e: Exception) {
                false
            }
        }
    }

    fun isFridaInjected(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("cat /proc/net/tcp")
            process.inputStream.bufferedReader().useLines { lines ->
                lines.any { it.contains("27042") || it.contains("27043") }
            }
        } catch (e: Exception) {
            false
        }
    }

    fun isDeviceCompromised(context: Context): Boolean {
        return isRootBinaryPresent() ||
                isRootManagementAppInstalled(context) ||
                isDangerousPropsSet() ||
                hasTestKeys() ||
                canExecuteSuCommand() ||
                isFridaRunning() ||
                isFridaPortOpen() ||
                isFridaInjected()
    }

    /**
     * Check if the device is connected to a VPN.
     */
    private fun isUsingVPN(): Boolean {
        try {
            val networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in networkInterfaces) {
                if (networkInterface.isUp && (networkInterface.name.contains("tun") ||
                            networkInterface.name.contains("ppp") ||
                            networkInterface.name.contains("pptp"))
                ) {
                    return true
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * Check if the device is using a proxy.
     */
    private fun isUsingProxy(context: Context): Boolean {
        return try {
            val proxyAddress = System.getProperty("http.proxyHost")
            val proxyPort = System.getProperty("http.proxyPort")?.toIntOrNull()
            if (proxyAddress != null && proxyPort != null && proxyPort != -1) {
                return true
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = connectivityManager.activeNetwork
                val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                if (networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    val proxyInfo = connectivityManager.getDefaultProxy()
                    return proxyInfo != null
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
}
