
package com.bornfire.merchantqrcode.interceptor

import android.content.Context
import com.bornfire.merchantqrcode.Utils.AuthToken
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.Request
import android.util.Log

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val token = AuthToken.getToken(context)
        val deviceId = AuthToken.getDeviceId(context)

        Log.d("AUTH", "🔗 Request: ${original.url} | Token: ${token?.length ?: 0} | Device: ${deviceId?.take(8)}...")

        val requestBuilder = original.newBuilder()


        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        } else {
            Log.w("AUTH", "⚠️ No token - Public endpoint?")
        }
        if (!deviceId.isNullOrEmpty()) {
            requestBuilder.header("Device-Id", deviceId)
        }


        val request = requestBuilder.build()
        val response = chain.proceed(request)

        // Log failures
        if (!response.isSuccessful) {
            Log.e("AUTH", "❌ API FAILED ${response.code}: ${response.message}")
        }

        return response
    }
}