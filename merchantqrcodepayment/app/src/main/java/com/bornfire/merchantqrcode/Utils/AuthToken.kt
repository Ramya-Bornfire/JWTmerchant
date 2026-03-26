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
    private const val DEVICE_ID_KEY = "STABLE_DEVICE_ID"  // ✅ Stable Android ID

    private var tokenInMemory: String? = null
    private var deviceIdInMemory: String? = null

    // ✅ FIXED: Use ANDROID_ID as stable device identifier (NEVER CHANGES)
    fun getStableDeviceId(context: Context): String {
        return Encryption.getAndroidId(context)
    }

    // ✅ COMPLETE saveToken - handles both dynamic PSU_ID and stable Device ID
    fun saveToken(context: Context, token: String, psuDeviceID: String) {
        try {
            tokenInMemory = token

            // ✅ CRITICAL: Use STABLE Android ID for JWT validation (not changing UUID)
            val stableDeviceId = getStableDeviceId(context)
            deviceIdInMemory = stableDeviceId

            Log.d("AUTH", "💾 Saving Token + STABLE DeviceID: $stableDeviceId (PSU: ${psuDeviceID.take(8)}...)")

            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val sharedPrefs = EncryptedSharedPreferences.create(
                PREF_FILE,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            sharedPrefs.edit()
                .putString(TOKEN_KEY, token)
                .putString(DEVICE_ID_KEY, stableDeviceId)  // ✅ SAVE STABLE ID for interceptor
                .apply()

            Log.d("AUTH", "✅ Token + Stable DeviceID saved successfully")
        } catch (e: Exception) {
            Log.e("AUTH", "❌ Error saving token", e)
        }
    }

    // ✅ getToken - memory cache + secure storage
    fun getToken(context: Context): String? {
        return try {
            // ✅ Fast memory cache
            tokenInMemory?.let {
                Log.d("AUTH", "⚡ Token from memory (${it.length} chars)")
                return it
            }

            // ✅ Secure storage fallback
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val sharedPrefs = EncryptedSharedPreferences.create(
                PREF_FILE, masterKeyAlias, context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            tokenInMemory = sharedPrefs.getString(TOKEN_KEY, null)
            Log.d("AUTH", "📦 Token loaded: ${tokenInMemory?.length ?: 0} chars")
            tokenInMemory
        } catch (e: Exception) {
            Log.e("AUTH", "❌ Error getting token", e)
            null
        }
    }

    // ✅ getDeviceId - ALWAYS returns STABLE Android ID for interceptor
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
    private const val PSU_DEVICE_ID_KEY = "PSU_DEVICE_ID"

    fun savePSUDeviceId(context: Context, psuDeviceID: String) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPrefs = EncryptedSharedPreferences.create(
            PREF_FILE,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        sharedPrefs.edit().putString(PSU_DEVICE_ID_KEY, psuDeviceID).apply()
    }

    fun getPSUDeviceId(context: Context): String? {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPrefs = EncryptedSharedPreferences.create(
            PREF_FILE,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPrefs.getString(PSU_DEVICE_ID_KEY, null)
    }

    // ✅ Clear everything
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

    // ✅ Real Android ID (for debugging)
    fun getRealDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}