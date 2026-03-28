package com.bornfire.merchantqrcode.retrofit
import android.content.Context
import android.provider.Settings
import android.util.Log

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.spec.KeySpec
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESedeKeySpec

object Encryption {
    fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
    fun getPSUIPADDRESS(): String {
        val random = Random()
        val ip = "${random.nextInt(256)}.${random.nextInt(256)}.${random.nextInt(256)}.${random.nextInt(256)}"
        return ip.toString()
    }
    private val counter = AtomicInteger(0)

    fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }
    fun getRandom4Digit(): String {
        val random = Random()
        val number = random.nextInt(1000) // Generates a random number between 1000 and 9999
        return number.toString()
    }
    fun generatereceipt(): String {
        val dateFormat = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault() // Set time zone to default
        val currentDate = Date()
        val datePart = dateFormat.format(currentDate)

        // Generate a random 5-digit number
        val randomPart = (10000..99999).random().toString()

        return "RN$datePart-$randomPart"
    }

    fun generatePID(): String {
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault() // Set time zone to default
        val currentDate = Date()
        val pid = dateFormat.format(currentDate)
        return pid
    }

    fun generatePSUID(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return md5(androidId)
    }

    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun generatePSUDeviceId(): String {
       val uuid = UUID.randomUUID()
       return uuid.toString()
    }
    fun encrypt(unencryptedString: String, psuDeviceID: String): String {
        return try {
            val arrayBytes = fixKeyLength(psuDeviceID.toByteArray(StandardCharsets.UTF_8))
            val ks: KeySpec = DESedeKeySpec(arrayBytes)
            val skf: SecretKeyFactory = SecretKeyFactory.getInstance("DESede")
            val cipher: Cipher = Cipher.getInstance("DESede")
            val key: SecretKey = skf.generateSecret(ks)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val plainText: ByteArray = unencryptedString.toByteArray(StandardCharsets.UTF_8)
            val encryptedText: ByteArray = cipher.doFinal(plainText)
            Base64.getEncoder().encodeToString(encryptedText)
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Encryption failed", e)
        }
    }

    fun decrypt(encryptedString: String, psuDeviceID: String): String {
        return try {
            val arrayBytes = fixKeyLength(psuDeviceID.toByteArray(StandardCharsets.UTF_8))
            val ks: KeySpec = DESedeKeySpec(arrayBytes)
            val skf: SecretKeyFactory = SecretKeyFactory.getInstance("DESede")
            val cipher: Cipher = Cipher.getInstance("DESede")
            val key: SecretKey = skf.generateSecret(ks)
            cipher.init(Cipher.DECRYPT_MODE, key)
            val encryptedText: ByteArray = Base64.getDecoder().decode(encryptedString.replace("\n", "").replace("\r", ""))
            val plainText: ByteArray = cipher.doFinal(encryptedText)
            String(plainText, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Decryption failed", e)
        }
    }

    private fun fixKeyLength(keyBytes: ByteArray): ByteArray {
        val fixedKey = ByteArray(24)
        System.arraycopy(keyBytes, 0, fixedKey, 0, Math.min(keyBytes.size, fixedKey.size))
        return fixedKey
    }

}