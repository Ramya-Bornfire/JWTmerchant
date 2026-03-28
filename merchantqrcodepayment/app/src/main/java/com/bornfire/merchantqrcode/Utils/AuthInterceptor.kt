
package com.bornfire.merchantqrcode.interceptor

import android.content.Context
import com.bornfire.merchantqrcode.Utils.AuthToken
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.Request
import android.util.Log
class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", null)

        // Add token if available and not a login/otp request
        val newRequest = if (token != null && !isAuthEndpoint(request.url.toString())) {
            Log.d("AuthInterceptor", "✅ Adding token to ${request.url}")
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        return chain.proceed(newRequest)
    }

    private fun isAuthEndpoint(url: String): Boolean {
        return url.contains("/api/LoginAndroid") ||
                url.contains("/api/OtpForAndroid") ||
                url.contains("/api/OtpForMerchant") ||
                url.contains("/api/OtpForUser") ||
                url.contains("/api/CheckDeviceId")
    }
}