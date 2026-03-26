package com.bornfire.merchantqrcode.Utils

import android.app.Activity
import android.widget.Toast
import androidx.core.app.ActivityCompat.finishAffinity
import com.bornfire.merchantqrcode.DataModel.EncryptedRequest
import com.bornfire.merchantqrcode.DataModel.LogoutData
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object LogoutApi {
    fun logoutTab(merid: String,activity: Activity) {
        val logoutdata = LogoutData(merid)
        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(logoutdata)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)
        val call = ApiClient.apiService.logoutTab(encryptedRequest,psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    // Handle the successful response here (update UI, show toast, etc.)
                    val loginResponse = response.body()
                    print(loginResponse)

                } else {
                    Toast.makeText(activity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                    println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println("Network error: ${t.message}")
                Toast.makeText(activity,"Something Went Wrong at Server End", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun logoutMobile(userid: String,activity: Activity) {
        val logoutdata = LogoutData(userid)
        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(logoutdata)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)
        val call = ApiClient.apiService.logoutMobile(encryptedRequest,psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    // Handle the successful response here (update UI, show toast, etc.)
                    val loginResponse = response.body()
                    print(loginResponse)

                } else {
                    // Handle error (non-successful response)
                    println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println("Network error: ${t.message}")
                Toast.makeText(activity,"Something Went Wrong at Server End", Toast.LENGTH_LONG).show()

            }
        })
    }
}