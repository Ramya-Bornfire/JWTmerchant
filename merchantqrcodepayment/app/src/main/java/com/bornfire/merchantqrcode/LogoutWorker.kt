package com.bornfire.merchantqrcode

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bornfire.merchantqrcode.DataModel.*
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogoutWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Perform logout and cleanup operations here
        logoutAndCloseApp()
        return Result.success()
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
                //    Log.d("LogoutWorker", "Logout successful for tablet")
                } else {
                 //   Log.d("LogoutWorker", "Error: ${response.code()}")
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
                 //   Log.d("LogoutWorker", "Logout successful for mobile: $decryptedResponse")
                } else {
                   // Log.d("LogoutWorker", "Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //Log.d("LogoutWorker", "Network error: ${t.message}")
            }
        })
    }
}
