package com.bornfire.merchantqrcode.Utils

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import android.provider.Settings
import com.bornfire.merchantqrcode.retrofit.Encryption

object AuthToken {

    private const val PREF_FILE = "secure_prefs"
    private const val TOKEN_KEY = "JWT_TOKEN"
    private const val DEVICE_ID_KEY = "STABLE_DEVICE_ID"

    private var tokenInMemory: String? = null
    private var deviceIdInMemory: String? = null

    //  FIXED: Use ANDROID_ID as stable device identifier (NEVER CHANGES)
    fun getStableDeviceId(context: Context): String {
        return Encryption.getAndroidId(context)
    }
    fun getToken(context: Context): String? {
        try {
            // Check memory cache first
            if (!tokenInMemory.isNullOrEmpty()) {
                Log.d("AUTH", "⚡ Token from memory (${tokenInMemory?.length} chars)")
                return tokenInMemory
            }

            // Load from secure storage
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val sharedPrefs = EncryptedSharedPreferences.create(
                PREF_FILE, masterKeyAlias, context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            tokenInMemory = sharedPrefs.getString(TOKEN_KEY, null)

            if (!tokenInMemory.isNullOrEmpty()) {
                Log.d("AUTH", "📦 Token loaded from storage: ${tokenInMemory?.length} chars")
                Log.d("AUTH", "📦 Token preview: ${tokenInMemory?.take(50)}...")
            } else {
                Log.w("AUTH", "⚠️ No token found in storage")
            }

            return tokenInMemory
        } catch (e: Exception) {
            Log.e("AUTH", "❌ Error getting token", e)
            return null
        }
    }

    //  getDeviceId - ALWAYS returns STABLE Android ID for interceptor
    fun getDeviceId(context: Context): String? {
        return try {
            // ✅ Fast memory cache
            deviceIdInMemory?.let {
                Log.d("AUTH", "⚡ DeviceID from memory: $it")
                return it
            }

            // ✅ Generate/load stable Android ID
            val stableDeviceId = getStableDeviceId(context)
            deviceIdInMemory = stableDeviceId
            Log.d("AUTH", "📦 STABLE DeviceID loaded: $stableDeviceId")
            stableDeviceId
        } catch (e: Exception) {
            Log.e("AUTH", "❌ Error getting deviceId", e)
            null
        }
    }

    //  Clear everything
    fun clearToken(context: Context) {
        try {
            Log.d("AUTH", "🗑️ Clearing Token + DeviceID")
            tokenInMemory = null
            deviceIdInMemory = null

            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val sharedPrefs = EncryptedSharedPreferences.create(
                PREF_FILE, masterKeyAlias, context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            sharedPrefs.edit()
                .remove(TOKEN_KEY)
                .remove(DEVICE_ID_KEY)
                .apply()

            Log.d("AUTH", "✅ Cleared successfully")
        } catch (e: Exception) {
            Log.e("AUTH", "❌ Error clearing token", e)
        }
    }

    // ✅ Check login status
    fun isLoggedIn(context: Context): Boolean {
        val loggedIn = getToken(context) != null
        Log.d("AUTH", "👤 Is Logged In: $loggedIn")
        return loggedIn
    }

}