package com.bornfire.merchantqrcode

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Intent
import android.util.Log
import com.bornfire.merchantqrcode.DataModel.EncryptedRequest
import com.bornfire.merchantqrcode.DataModel.LogoutData
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DataModel.SharedusercatDataObj
import com.bornfire.merchantqrcode.Utils.AuthToken
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyApp : Application() {
    private val PREFS_NAME = "AppPrefs"
    private val LAST_VISIBILITY_KEY = "lastVisibilityTime"
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
        if (AuthToken.isLoggedIn(this)) {
            Log.d("MyApp", " Valid JWT found: ${AuthToken.getToken(this)?.length ?: 0} chars")
            Log.d("MyApp", " DeviceID: ${AuthToken.getDeviceId(this)}")
        } else {
            Log.d("MyApp", " No JWT - user needs to login")
            AuthToken.clearToken(this)  // Clean old invalid tokens
        }
        // Initialize any global resources here
    }
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            logoutAndCloseApp()
        }
    }
    private fun logoutAndCloseApp() {
        val usercategory = SharedusercatDataObj.UserCategory
        val merchantData = SharedMerchantDataObj.merchantData
        if (usercategory?.user_category == "Representative") {
            LogoutforTab(merchantData?.merchant_rep_id.toString())
        } else {
            val userData = SharedUserDataObj.userData
            logoutforMobile(userData?.user_id.toString())
        }
    }

    private fun LogoutforTab(merid: String) {
        val logoutdata = LogoutData(merid)
        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(logoutdata)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)

        val call = ApiClient.apiService.logoutTab(encryptedRequest, psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                 //   Log.d("LogoutWorker", "Logout successful for tablet")
                } else {
                  //  Log.d("LogoutWorker", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
               // Log.d("LogoutWorker", "Network error: ${t.message}")
            }
        })
    }

    private fun logoutforMobile(userid: String) {
        val logoutdata = LogoutData(userid)
        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(logoutdata)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)

        val call = ApiClient.apiService.logoutMobile(encryptedRequest, psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()?.string()
                    val decryptedResponse = Encryption.decrypt(loginResponse!!, psuDeviceID)
                    //Log.d("LogoutWorker", "Logout successful for mobile: $decryptedResponse")
                } else {
                    //Log.d("LogoutWorker", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
               // Log.d("LogoutWorker", "Network error: ${t.message}")
            }
        })
    }
}