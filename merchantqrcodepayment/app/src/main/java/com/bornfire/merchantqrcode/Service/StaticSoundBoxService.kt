package com.bornfire.merchantqrcode.Service
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.widget.Toast
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DataModel.SharedusercatDataObj
import kotlin.concurrent.thread
import com.bornfire.merchantqrcode.DataModel.StaticPaymentData
import com.bornfire.merchantqrcode.SharedUserDataObj
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption.getAndroidId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
class StaticSoundBoxService : Service(),TextToSpeech.OnInitListener  {
    private lateinit var tts: TextToSpeech
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var apiRunnable: Runnable
    private val interval: Long = 5000
    private val delayMillis: Long = 300
    private lateinit var merchantid:String
    private lateinit var userid:String
    private lateinit var activityStateReceiver: BroadcastReceiver
    override fun onCreate() {
        super.onCreate()

        val usercategory = SharedusercatDataObj.UserCategory
        val merchantData = SharedMerchantDataObj.merchantData
        if (usercategory?.user_category == "Representative") {
            merchantid=merchantData?.merchant_user_id.toString()
            userid=merchantData?.merchant_rep_id.toString()
        } else {
            val userData = SharedUserDataObj.userData
            merchantid=userData?.merchant_user_id.toString()
            userid=userData?.user_id.toString()
        }
        apiRunnable = object : Runnable {
            override fun run() {
                callApi()
                handler.postDelayed(this, interval)
            }
        }
        handler.post(apiRunnable)

        // Initialize BroadcastReceiver for activity state changes
        activityStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == "com.bornfire.ACTION_RESUME") {
                    onResume()
                }
            }
        }
        // Register the receiver
        // Register the receiver with the appropriate flag
        val filter = IntentFilter("com.bornfire.ACTION_RESUME")
        registerReceiver(activityStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        tts = TextToSpeech(this, this)
        return START_STICKY
    }

    override fun onDestroy() {
        if (this::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
        try {
            unregisterReceiver(activityStateReceiver)
        } catch (e: IllegalArgumentException) {
           // e.printStackTrace()
        }
        handler.removeCallbacks(apiRunnable)
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    private fun callApi() {
        val call = ApiClient.apiService.getStaticPayment(merchantid, getAndroidId(this), userid)
        call.enqueue(object : Callback<List<StaticPaymentData>> {
            override fun onResponse(
                call: Call<List<StaticPaymentData>>,
                response: Response<List<StaticPaymentData>>
            ) {
                if (response.isSuccessful) {
                    val responseList = response.body()
                    if (responseList != null && responseList.isNotEmpty()) {
                        thread {
                            var index = 0
                            while (index < responseList.size) {
                                val item = responseList[index]
                                handler.post {
                                    Toast.makeText(
                                        applicationContext,
                                        "Transaction Amount: ${item.tran_amount}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                val text = "Pula ${item.tran_amount} is deposited into Merchant Account"
                                speakOut(text)

                                // Wait for TTS to complete before processing the next item
                                waitForTTSToComplete()

                                index++
                                if (index < responseList.size) {
                                    // Delay before processing the next item
                                    Thread.sleep(delayMillis)
                                }
                            }
                        }
                    } else {
                   //     Toast.makeText(this@StaticSoundBoxService, "No data received", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                 //   println("Error: ${response.code()}")
                  //  Toast.makeText(this@StaticSoundBoxService, "Response error: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<List<StaticPaymentData>>, t: Throwable) {
              //  println("Network : ${t.message}")
               // Toast.makeText(this@StaticSoundBoxService, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun waitForTTSToComplete() {
        while (tts.isSpeaking) {
            Thread.sleep(100) // Check every 100 ms
        }
    }
    private fun speakOut(text: String) {
     //   println("Speaking out: $text")
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        } else {
            Toast.makeText(this, "TextToSpeech initialization failed", Toast.LENGTH_LONG).show()
        }
    }
    private fun onResume() {
        val usercategory = SharedusercatDataObj.UserCategory
        val merchantData = SharedMerchantDataObj.merchantData
        if (usercategory?.user_category == "Representative") {
            merchantid=merchantData?.merchant_user_id.toString()
            userid=merchantData?.merchant_rep_id.toString()
        } else {
            val userData = SharedUserDataObj.userData
            merchantid=userData?.merchant_user_id.toString()
            userid=userData?.user_id.toString()
        }
        apiRunnable = object : Runnable {
            override fun run() {
                callApi()
                handler.postDelayed(this, interval)
            }
        }
        handler.post(apiRunnable)
       // Toast.makeText(this, "Service Resumed", Toast.LENGTH_LONG).show()
        // Add any logic you need to handle here
    }
}

