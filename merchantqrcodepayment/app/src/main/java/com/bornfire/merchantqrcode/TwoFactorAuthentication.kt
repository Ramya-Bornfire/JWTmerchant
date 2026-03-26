package com.bornfire.merchantqrcode

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.DataModel.*
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.Utils.LogoutApi
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TwoFactorAuthentication : BaseActivity() {
    private lateinit var userIdEditText: EditText
    private lateinit var userNameEditText: EditText
    private lateinit var question1EditText: EditText
    private lateinit var question2EditText: EditText
    private lateinit var question3EditText: EditText
    private lateinit var question4EditText: EditText
    private lateinit var question5EditText: EditText
    private lateinit var question6EditText: EditText
    private lateinit var question7EditText: EditText
    private lateinit var question8EditText: EditText
    private lateinit var question9EditText: EditText
    private lateinit var question10EditText: EditText
    private lateinit var submitBtn: Button
    lateinit var userName : String
    lateinit var password : String
    lateinit var userid: String
    lateinit var email: String
    private lateinit var mobileNo: String
    private val userSharedCategory= SharedusercatDataObj.UserCategory
    private val merchantSharedData = SharedMerchantDataObj.merchantData
    val userData = SharedUserDataObj.userData
    private lateinit var commonUserId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_two_factor_authentication)
        userIdEditText = findViewById(R.id.userIdEditText)
        userNameEditText = findViewById(R.id.userNameEditText)
        question1EditText = findViewById(R.id.question1EditText)
        question2EditText = findViewById(R.id.question2EditText)
        question3EditText = findViewById(R.id.question3EditText)
        question4EditText = findViewById(R.id.question4EditText)
        question5EditText = findViewById(R.id.question5EditText)
        question6EditText = findViewById(R.id.question6EditText)
        question7EditText = findViewById(R.id.question7EditText)
        question8EditText = findViewById(R.id.question8EditText)
        question9EditText = findViewById(R.id.question9EditText)
        question10EditText = findViewById(R.id.question10EditText)
        addTextWatcher(question1EditText)
        addTextWatcher(question2EditText)
        addTextWatcher(question3EditText)
        addTextWatcher(question4EditText)
        addTextWatcher(question5EditText)
        addTextWatcher(question6EditText)
        addTextWatcher(question7EditText)
        addTextWatcher(question8EditText)
        addTextWatcher(question9EditText)
        addTextWatcher(question10EditText)

        supportActionBar?.title="Two-Factor Authentication"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        submitBtn = findViewById(R.id.submitBtn)
        userid = intent.getStringExtra("userId").toString()
        userName = intent.getStringExtra("userName").toString()
        password = intent.getStringExtra("password").toString()
        mobileNo = intent.getStringExtra("mobNo").toString()
        email = intent.getStringExtra("email").toString()
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                logoutId()
                backToLogin()
            }
        }
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        onBackPressedDispatcher.addCallback(this, callback)
        userIdEditText.setText(userid)
        userNameEditText.setText(userName)
        submitBtn.setOnClickListener {
            if (validateFields()) {
                saveAnswer()
            }
        }
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        if (isTablet) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            commonUserId = if(userSharedCategory?.user_category=="Representative") {
                merchantSharedData?.merchant_rep_id!!
            } else{
                userData?.user_id!!
            }
        }
        else{
            commonUserId=userData?.user_id!!
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
                editText.removeTextChangedListener(this)
                val filteredText = s.toString().filter { it.isLetter() || it.isWhitespace() }.uppercase()
                editText.setText(filteredText)
                editText.setSelection(filteredText.length)
                editText.addTextChangedListener(this)
            }
        })
    }

    private fun validateFields(): Boolean {
        var filledCount = 0
        val questionEditTexts = listOf(
            question1EditText,
            question2EditText,
            question3EditText,
            question4EditText,
            question5EditText,
            question6EditText,
            question7EditText,
            question8EditText,
            question9EditText,
            question10EditText
        )
        questionEditTexts.forEach { editText ->
            if (editText.text.isNotBlank()) {
                filledCount++
            }
        }
        val requiredFilledQuestions = 3
        if (filledCount < requiredFilledQuestions) {
            val message = when ( val remainingQuestions = requiredFilledQuestions - filledCount) {
                1 -> "Please fill at least 1 more question."
                else -> "Please fill at least $remainingQuestions more questions."
            }
            AlertDialogBox().showDialog(this, message)
            return false
        }
        return true
    }

    private fun saveAnswer() {
        val request = SaveTwoFactor(userid,userName,password,"Y","Null",mobileNo,email,"1312",question1EditText.text.toString(),
            question2EditText.text.toString(),question3EditText.text.toString(),question4EditText.text.toString(),question5EditText.text.toString(),question6EditText.text.toString(),question7EditText.text.toString(),question8EditText.text.toString(),question9EditText.text.toString(),question10EditText.text.toString())

        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(request)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)
        val call = ApiClient.apiService.saveTwoFactorAuthentication(encryptedRequest,psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val encryptedResponse = response.body()?.string()
                    if (encryptedResponse != null) {
                        val decryptedResponse = Encryption.decrypt(encryptedResponse, psuDeviceID)
                      //  println("Decrypted response: $decryptedResponse")
                        val intent = Intent(this@TwoFactorAuthentication,NewPassword::class.java)
                        intent.putExtra("userId",userid)
                        intent.putExtra("GoBack","No")
                        startActivity(intent)
                    }
                }
                else{
                    Toast.makeText(this@TwoFactorAuthentication, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                 //   println("Error: ${response.code()}")
                }}
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //println("Network error: ${t.message}")
                Toast.makeText(this@TwoFactorAuthentication,"Something Went Wrong at Server End", Toast.LENGTH_LONG).show()
            }
        })
    }
    override fun onSupportNavigateUp(): Boolean {
        logoutId()
        backToLogin()
        return true
    }
    private fun logoutId() {
        if (userSharedCategory?.user_category == "Representative") {
            LogoutApi.logoutTab(commonUserId,this)
        }
        else{
            LogoutApi.logoutMobile(commonUserId,this)
        }
    }
    private fun backToLogin(){
        val intent = Intent(this@TwoFactorAuthentication,LoginActivity::class.java)
        startActivity(intent)
    }
}