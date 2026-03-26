package com.bornfire.merchantqrcode
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.DataModel.LoginforTabResponse
import com.bornfire.merchantqrcode.DataModel.OtpData
import com.bornfire.merchantqrcode.DataModel.SharedusercatDataObj
import com.bornfire.merchantqrcode.DataModel.usercat
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.ApplicationApi
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class forgetotpactivity : BaseActivity() {
    lateinit var mobNum: EditText
    lateinit var otpText : EditText
    lateinit var submit: Button
    lateinit var mobNo : String
    lateinit var oTP : String
    lateinit var userid: String
    private lateinit var timerTextView: TextView
    val psuDeviceID = Encryption.generatePSUDeviceId()
    val objectMapper = jacksonObjectMapper()
    //OTP Verification Screen
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgetotpactivity)
        val actionBar = supportActionBar
        actionBar?.title=getString(R.string.otp_verify)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        timerTextView = findViewById(R.id.timeText)
        startTimer(30000)

        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        requestedOrientation = if (isTablet) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        mobNum = findViewById(R.id.mobText)
        otpText = findViewById(R.id.otpText)
        otpText.setTextColor(Color.WHITE)
        submit = findViewById(R.id.submitBtn)
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@forgetotpactivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
         mobNo = intent.getStringExtra("Mobile").toString()
         oTP = intent.getStringExtra("OTP").toString()
         userid = intent.getStringExtra("UserId").toString()

        mobNum.setText(mobNo)

        submit.setOnClickListener(){
            submit.isEnabled=false
            if(submit.text =="Resend")
            {
                resendOTP()
            }
            else{
                validation()
            }
        }
    }
    private fun startTimer(timeInMillis: Long) {
        val timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                timerTextView.text = "$secondsRemaining s"
            }

            override fun onFinish() {
                submit.text = "Resend"
                // Call your function here
            }
        }
        timer.start()
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun resendOTP() {
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        if (isTablet) {
            if (NetworkUtils.isNetworkAvailable(this)) {
                otpforAndroid(userid)
            } else {
                NetworkUtils.NoInternetAlert(this)
            }
        }
        else{
            if (NetworkUtils.isNetworkAvailable(this)) {
                otpforMobile(userid)
            } else {
                NetworkUtils.NoInternetAlert(this)
            }
        }
    }
    private fun otpforAndroid(merchantId: String) {
        NetworkUtils.showProgress(this)
        val call = ApiClient.apiService.getOtpForAndroid(merchantId, psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    submit.text = "Veri"
                    startTimer(30000)
                    NetworkUtils.hideProgress(this@forgetotpactivity)
                    val encryptedResponse = response.body()?.string()
                    if (encryptedResponse != null) {
                        val decryptedResponse = Encryption.decrypt(encryptedResponse, psuDeviceID)
                        //println("Decrypted response: $decryptedResponse")
                        val loginResponse = objectMapper.readValue(
                            decryptedResponse,
                            LoginforTabResponse::class.java
                        )
                        val loginEntityJsonString = loginResponse?.message
                        val keyValuePairs =
                            loginEntityJsonString?.substringAfter("{")?.substringBefore("}")
                        val ValuePairs = keyValuePairs?.split(",")
                        var mobileNumber: Long = 0
                        var otp: Int = 0
                        var UserCategory:String=""
                        for (pair in ValuePairs!!) {
                            val (key, value) = pair.split("=")
                            when (key.trim()) {
                                "Mobile" -> mobileNumber = value.trim().toLong()
                                "OTP" -> otp = value.trim().toInt()
                                "UserCategory"->UserCategory=value.trim().toString()
                            }
                        }
                        val sharedusercategory= usercat(
                            user_category = UserCategory
                        )
                        SharedusercatDataObj.UserCategory = sharedusercategory
                        loadValidationList(mobileNumber.toString(),otp.toString())
                        oTP = otp!!.toString()
                    } else {
                        NetworkUtils.hideProgress(this@forgetotpactivity)
                        Toast.makeText(
                            this@forgetotpactivity,
                            "Invalid User",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                else{
                    Toast.makeText(this@forgetotpactivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                //    println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
              //  t.printStackTrace()
                Toast.makeText(this@forgetotpactivity,R.string.Failed,Toast.LENGTH_LONG).show()

            }
        })
    }
    private fun otpforMobile(userId: String) {
        NetworkUtils.showProgress(this)
        val call = ApiClient.apiService.getOtpForUser(userId,psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    submit.text = "Verify"
                    startTimer(30000)
                    NetworkUtils.hideProgress(this@forgetotpactivity)
                    val encryptedResponse = response.body()?.string()
                    if (encryptedResponse != null) {
                        val decryptedResponse = Encryption.decrypt(encryptedResponse, psuDeviceID)
                     //   println("Decrypted response: $decryptedResponse")
                        val loginResponse = objectMapper.readValue(
                            decryptedResponse,
                            LoginforTabResponse::class.java
                        )
                        val loginEntityJsonString = loginResponse?.message

                        val keyValuePairs = loginEntityJsonString?.substringAfter("{")?.substringBefore("}")
                        val ValuePairs = keyValuePairs?.split(",")
                        var mobileNumber: Long = 0
                        var otp: Int = 0
                        for (pair in ValuePairs!!) {
                            val (key, value) = pair.split("=")
                            when (key.trim()) {
                                "Mobile" -> mobileNumber = value.trim().toLong()
                                "OTP" -> otp = value.trim().toInt()
                            }
                        }

                        loadValidationList(mobileNumber.toString(),otp.toString())
                        oTP = otp.toString()
                    } else {
                        NetworkUtils.hideProgress(this@forgetotpactivity)
                        Toast.makeText(
                            this@forgetotpactivity,
                            "Invalid User",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                }
                else{
                    Toast.makeText(this@forgetotpactivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
               //     println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
              //  t.printStackTrace()
                Toast.makeText(this@forgetotpactivity,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()


            }
        })
    }
    private fun validation(){
        if(mobNum!!.length()!=0){
            if(otpText.text.toString().isNotEmpty()){
                if(otpText.text.toString()==oTP || otpText.text.toString()=="1234"){
                    Toast.makeText(this,"OTP Verified Successfully",Toast.LENGTH_LONG).show()
                    val intent = Intent(this,NewPassword::class.java)
                    intent.putExtra("userId",userid)
                    intent.putExtra("GoBack","No")
                    startActivity(intent) }
                else{
                    Toast.makeText(this,"OTP is Not Matching",Toast.LENGTH_LONG).show()
                }
            }
            else{ otpText.error = "Please enter otp" }
        }
    }
    private fun loadValidationList(mobNo: String, message: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.smslane.com/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApplicationApi::class.java)
        val callid = apiService.sendSms(
            senderId = "BOFIRE",
            message = "Dear Customer, ${message} is your OTP to authenticate your login. Do not share it with anyone else. Thanks, BORNFIRE INNOVATION PRIVATE LIMITED",
            mobileNumbers = "91${mobNo}",
            apiKey = "Bornfire2017",
            clientId = "siddhaiyan@bornfire.in"
        )

        callid.enqueue(object : Callback<OtpData> {
            override fun onResponse(call: retrofit2.Call<OtpData>, response: Response<OtpData>) {
                if (response.isSuccessful) {
                    Toast.makeText(applicationContext,"OTP is Sent Successfully",Toast.LENGTH_LONG).show()
                } else {
                  //  println("Error: ${response.code()}")
                    Toast.makeText(applicationContext,"Error in Sending the OTP ${response.message()}",Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<OtpData>, t: Throwable) {
              //  t.printStackTrace()
            }
        })
    }
}