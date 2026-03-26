package com.bornfire.merchantqrcode.retrofit


import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import android.content.Context
import android.util.Log
import com.bornfire.merchantqrcode.interceptor.AuthInterceptor

object ApiClient {

    var gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private const val BASE_URL = "http://10.0.2.2:8080/BMPAYInternet/"
    private lateinit var appContext: Context

    // ✅ Add this init function
    fun init(context: Context) {
        appContext = context.applicationContext
        Log.d("ApiClient", "✅ Initialized with context: $appContext")
    }
    // ✅ Certificate Pinning Setup
    private val certificatePinner = CertificatePinner.Builder()
        .add("bwqrpay.bankofbaroda.co.in", "sha256/weUbsUO7XWui9nn2cjO6YfhxzvaxfTVP9AUN8P2k8I8=") // Replace with actual SHA-256
        .build()

    // ✅ Secure OkHttpClient with SSL Pinning
    private fun getCustomOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(appContext))
            //.certificatePinner(certificatePinner) // Apply SSL Pinning
            .build()
    }

    val apiService: ApplicationApi by lazy {
        if (!::appContext.isInitialized) {
            throw IllegalStateException("❌ ApiClient.init(context) must be called first!")
        }
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getCustomOkHttpClient()) // Use the secured OkHttpClient
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApplicationApi::class.java)
    }
}
