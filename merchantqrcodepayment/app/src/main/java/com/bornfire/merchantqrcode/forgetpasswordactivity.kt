package com.bornfire.merchantqrcode
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.DataModel.LoginforTabResponse
import com.bornfire.merchantqrcode.DataModel.OtpData
import com.bornfire.merchantqrcode.DataModel.SharedusercatDataObj
import com.bornfire.merchantqrcode.DataModel.usercat
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
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


class forgetpasswordactivity : BaseActivity() {
    lateinit var userid: EditText
    lateinit var verifybtn: Button
    lateinit var clickhereBtn: TextView
    lateinit var answerText: EditText
    val psuDeviceID = Encryption.generatePSUDeviceId()
    val objectMapper = jacksonObjectMapper()
    private var selectedQuestionIndex: Int = -1
    val securityQuestions = listOf(
        "What is your mother's name?",
        "What was the name of your first pet?",
        "What is your favorite book?",
        "What is your favorite food?",
        "What city were you born in?",
        "What is your favorite color?",
        "What was your first car?",
        "What was the name of your elementary school?",
        "What is your favorite movie?",
        "What is your father's name?"
    )
    //sending the otp to the user registered phone number
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgetpasswordactivity)
        val actionBar = supportActionBar
        actionBar?.title=getString(R.string.forget_password_header)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        val securityQuestionSpinner: Spinner = findViewById(R.id.securityQuestionSpinner)
        answerText= findViewById(R.id.answerEditText)

        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, securityQuestions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        securityQuestionSpinner.adapter = adapter
        securityQuestionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (parent.getChildAt(0) as? TextView)?.setTextColor(resources.getColor(R.color.white))
                selectedQuestionIndex = position + 1
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle the case where no item is selected if needed
            }
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@forgetpasswordactivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
        if (isTablet) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        userid = findViewById(R.id.mobText)
        addTextWatcher(userid)
        addTextWatcher(answerText)
        verifybtn = findViewById(R.id.verifyBtn)
        clickhereBtn = findViewById(R.id.clickhereText)
        clickhereBtn.setOnClickListener() {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        verifybtn.setOnClickListener() {
            verifybtn.isEnabled=false
            validation()
        }
    }

    private fun addTextWatcher(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text is changed
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed during text change
            }
            override fun afterTextChanged(s: Editable?) {
                // Remove the listener to avoid infinite loop
                editText.removeTextChangedListener(this)
                val filteredText = s.toString().filter { it.isLetter() || it.isDigit()|| it.isWhitespace() }.uppercase()
                // Set the filtered text back to the EditText
                editText.setText(filteredText)
                // Move cursor to the end of the text
                editText.setSelection(filteredText.length)
                // Add the listener back
                editText.addTextChangedListener(this)
            }
        })
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

    private fun validation() {
        if (userid!!.length() == 0) {
           // println("Please enter the user id")
            AlertDialogBox().showDialog(this@forgetpasswordactivity, "Please enter the user id.")

        } else {
            callauthenticationapi(userid.text.toString(),selectedQuestionIndex,answerText.text.toString())

        }
    }
    private fun callauthenticationapi(userId: String, selectedQuestionIndex: Int, answer: String) {
        val psuDeviceID = Encryption.generatePSUDeviceId()
        NetworkUtils.showProgress(this)
        val call = ApiClient.apiService.checkTwoFactorAnswer(userId,selectedQuestionIndex,answer,psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    NetworkUtils.hideProgress(this@forgetpasswordactivity)
                    val encryptedResponse = response.body()?.string()
                    if (encryptedResponse != null) {
                        val decryptedResponse = Encryption.decrypt(encryptedResponse, psuDeviceID)
                        val loginResponse = objectMapper.readValue(
                            decryptedResponse,
                            LoginforTabResponse::class.java
                        )
                        val loginEntityJsonString = loginResponse?.message
                        if(loginEntityJsonString=="true") {
                            val isTablet =
                                resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE

                            if (isTablet) {
                                if (NetworkUtils.isNetworkAvailable(this@forgetpasswordactivity)) {
                                    otpforAndroid(userid.text.toString())
                                } else {
                                    NetworkUtils.NoInternetAlert(this@forgetpasswordactivity)
                                }
                            } else {
                                if (NetworkUtils.isNetworkAvailable(this@forgetpasswordactivity)) {
                                    otpforMobile(userid.text.toString())
                                } else {
                                    NetworkUtils.NoInternetAlert(this@forgetpasswordactivity)
                                }
                            }
                        }
                        else{
                            AlertDialogBox().showDialog(this@forgetpasswordactivity, "Incorrect Answer.")

                        }
                    } else {
                        NetworkUtils.hideProgress(this@forgetpasswordactivity)
                        Toast.makeText(
                            this@forgetpasswordactivity,
                            "Invalid User",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                }
                else{
                    Toast.makeText(this@forgetpasswordactivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                  //  println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
               // t.printStackTrace()
                Toast.makeText(this@forgetpasswordactivity,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()


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
                    NetworkUtils.hideProgress(this@forgetpasswordactivity)
                    val encryptedResponse = response.body()?.string()
                    if (encryptedResponse != null) {
                        val decryptedResponse = Encryption.decrypt(encryptedResponse, psuDeviceID)
                    //    println("Decrypted response: $decryptedResponse")
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
                    NextScreen(mobileNumber.toString(),otp.toString())
                } else {
                        NetworkUtils.hideProgress(this@forgetpasswordactivity)
                        Toast.makeText(
                            this@forgetpasswordactivity,
                            "Invalid User",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                }
                else{
                    Toast.makeText(this@forgetpasswordactivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                //    println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
              //  t.printStackTrace()
                Toast.makeText(this@forgetpasswordactivity,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()


            }
        })
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
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
                    NetworkUtils.hideProgress(this@forgetpasswordactivity)
                    val encryptedResponse = response.body()?.string()
                    if (encryptedResponse != null) {
                        val decryptedResponse = Encryption.decrypt(encryptedResponse, psuDeviceID)
                      //  println("Decrypted response: $decryptedResponse")
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
                        NextScreen(mobileNumber!!.toString(), otp!!.toString())
                    } else {
                        NetworkUtils.hideProgress(this@forgetpasswordactivity)
                        Toast.makeText(
                            this@forgetpasswordactivity,
                            "Invalid User",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                else{
                    Toast.makeText(this@forgetpasswordactivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                 //   println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
              //  t.printStackTrace()
                Toast.makeText(this@forgetpasswordactivity,R.string.Failed,Toast.LENGTH_LONG).show()

            }
        })
    }
    private fun otpforTab(merchantId: String) {
        NetworkUtils.showProgress(this)
        val call = ApiClient.apiService.getOtpForMerchant(merchantId, psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    NetworkUtils.hideProgress(this@forgetpasswordactivity)
                    val encryptedResponse = response.body()?.string()
                    if (encryptedResponse != null) {
                        val decryptedResponse = Encryption.decrypt(encryptedResponse, psuDeviceID)
                       // println("Decrypted response: $decryptedResponse")
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
                        for (pair in ValuePairs!!) {
                            val (key, value) = pair.split("=")
                            when (key.trim()) {
                                "Mobile" -> mobileNumber = value.trim().toLong()
                                "OTP" -> otp = value.trim().toInt()
                            }
                        }
                        NextScreen(mobileNumber!!.toString(), otp!!.toString())
                    } else {
                        NetworkUtils.hideProgress(this@forgetpasswordactivity)
                        Toast.makeText(
                            this@forgetpasswordactivity,
                            "Invalid User",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                else{
                    Toast.makeText(this@forgetpasswordactivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                 //   println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            //    t.printStackTrace()
                Toast.makeText(this@forgetpasswordactivity,R.string.Failed,Toast.LENGTH_LONG).show()

            }
        })
    }
    private fun NextScreen(mob : String, otp : String) {
     //   loadValidationList(mob,otp)
        val intent = Intent(this,forgetotpactivity::class.java)
        intent.putExtra("Mobile",mob)
        intent.putExtra("OTP", otp)
        intent.putExtra("UserId",userid.text.toString())
        startActivity(intent)
    }
}