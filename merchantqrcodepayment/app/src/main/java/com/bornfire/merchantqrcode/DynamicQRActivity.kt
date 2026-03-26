package com.bornfire.merchantqrcode

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.*
import android.media.MediaPlayer
import android.os.*
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBar
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.DataModel.*
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.bornfire.merchantqrcode.retrofit.Encryption.getAndroidId
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.TimeUnit

class DynamicQRActivity : BaseActivity(), TextToSpeech.OnInitListener {
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var amountEditText: EditText
    private lateinit var billNumEditText: EditText
    private lateinit var generateQRCodeButton: Button
    private lateinit var imageView: ImageView
    private lateinit var linearLayout: LinearLayout
    var dbAmountLimit: Double = 0.0
    private var mediaPlayer: MediaPlayer? = null
    private val merchantSharedData = SharedMerchantDataObj.merchantData
    val userData = SharedUserDataObj.userData
    private val userSharedCategory = SharedusercatDataObj.UserCategory
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    lateinit var merchantID: String
    private lateinit var psuChannel: String
    lateinit var userID: String
    lateinit var unitId: String
    private var shouldContinuePolling = true
    private var screenTimeOut = true
    private lateinit var transactionAmountText: TextView
    private lateinit var billNumberText: TextView
    private val tranAmount = "Amount"
    private val billNum = "Bill Number"
    lateinit var timeText: TextView
    private lateinit var emailId: String
    private lateinit var contactId: String
    private var countDownTimer: CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamicqractivity)
        timeText = findViewById(R.id.timer_txt)
        textToSpeech = TextToSpeech(this, this)
        handler = Handler(Looper.getMainLooper())
        val isTablet =
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        if (isTablet) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            psuChannel = "TAB"
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            psuChannel = "MOBILE"
        }
        if (userSharedCategory?.user_category == "Representative") {
            merchantID = merchantSharedData?.merchant_user_id.toString()
            userID = merchantSharedData?.merchant_rep_id.toString()
            unitId = merchantSharedData?.unit_id.toString()
            emailId = merchantSharedData?.email_address.toString()
            contactId = merchantSharedData?.mobile_no.toString()
        } else {
            merchantID = userData?.merchant_user_id.toString()
            userID = userData?.user_id.toString()
            unitId = userData?.unit_id.toString()
            emailId = userData?.email_address1.toString()
            contactId = userData?.mobile_no1.toString()
        }
        transactionAmountText = findViewById(R.id.texttranamount)
        billNumberText = findViewById(R.id.textbillnumber)
        setEditMode()
        val actionBar = supportActionBar
        if (actionBar != null) {
            val inflater = LayoutInflater.from(this)
            val customView = inflater.inflate(R.layout.custom_action_bar, null)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            actionBar.customView = customView
            val titleTextView: TextView = customView.findViewById(R.id.action_bar_title)
            titleTextView.text = getString(R.string.dym_qr)
            val helpImg = findViewById<ImageView>(R.id.help_image)
            helpImg.setOnClickListener {
                HelpInfo.getInfo(this, "2")
            }
        }
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        checkAmountLimit()
        amountEditText = findViewById(R.id.amountEditText)
        billNumEditText = findViewById(R.id.billnum)
        generateQRCodeButton = findViewById(R.id.generateQRCodeButton)
        imageView = findViewById(R.id.imageView)
        linearLayout = findViewById(R.id.linearlay)
        billNumEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Do nothing
            }
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val input = it.toString()
                    val capitalizedInput = input.uppercase()
                    if (input != capitalizedInput) {
                        billNumEditText.setText(capitalizedInput)
                        billNumEditText.setSelection(capitalizedInput.length)
                    }
                }
            }
        })
        generateQRCodeButton.setOnClickListener {
            hideKeyboard()
            val billNumber = billNumEditText.text.toString()
            val amountText = amountEditText.text.toString()
            val amount = amountText.toDoubleOrNull()
            if (billNumber.isEmpty()) {
                AlertDialogBox().showDialog(this, "Bill number is empty")
            } else if (billNumber.length < 3) {
                AlertDialogBox().showDialog(this, "Bill number should be minimum three character")
            } else if (!billNumber.matches("[a-zA-Z0-9]+".toRegex())) {
                AlertDialogBox().showDialog(this, "Only alphabets and numbers are allowed")
            } else if (!amountText.matches(Regex("^\\d+(\\.\\d{2})?$"))) {
                AlertDialogBox().showDialog(this, "Invalid amount")
            } else if (amountText.isEmpty()) {
                AlertDialogBox().showDialog(this, "Amount is empty")
            } else if (amount == null) {
                AlertDialogBox().showDialog(this, "Invalid amount")
            } else if (amount > dbAmountLimit) {
                AlertDialogBox().showDialog(this, "Amount should not exceed $dbAmountLimit")
            } else if (amount == 0.0) {
                AlertDialogBox().showDialog(this, "Amount should not be 0")
            } else {
                generateQRMer()
                linearLayout.visibility = View.VISIBLE
                Handler(Looper.getMainLooper()).postDelayed({
                    if (screenTimeOut) {
                        if (!isFinishing && !isDestroyed) {
                            generateQRCodeButton.isEnabled = true
                            val builder = AlertDialog.Builder(this)
                            builder.setTitle("Timed Out")
                            builder.setCancelable(false)
                            builder.setMessage("Your session has timed out.")
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                shouldContinuePolling = false
                                linearLayout.visibility = View.GONE
                                finish()
                            }
                            builder.setCancelable(false)
                            if (!isFinishing && !isDestroyed) {
                                builder.show()
                            }
                        }
                    }

                }, 120000)
            }
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
             finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun setEditMode() {
        transactionAmountText.text = getSpannableStringWithRedStar(tranAmount)
        billNumberText.text = getSpannableStringWithRedStar(billNum)
    }

    private fun getSpannableStringWithRedStar(text: String): SpannableString {
        val spannable = SpannableString("$text *")
        spannable.setSpan(
            ForegroundColorSpan(Color.RED),
            spannable.length - 1,
            spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    private fun checkAmountLimit() {
        val call = ApiClient.apiService.getTranAmountLimit(merchantID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>, response: Response<ResponseBody>
            ) {
                dbAmountLimit = if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    responseBody?.toDoubleOrNull() ?: 50000.0
                } else {
                //    println("Error: Received HTTP ${response.code()} - ${response.message()}")
                    Toast.makeText(
                        this@DynamicQRActivity,
                        "Error: Unable to fetch amount limit. Please try again.${
                            response.message()
                        }",
                        Toast.LENGTH_LONG
                    ).show()
                    50000.0
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            //    println("Network error: ${t.message}")
                Toast.makeText(
                    this@DynamicQRActivity, "Something Went Wrong at Server End", Toast.LENGTH_LONG
                ).show()

            }
        })
    }
    private fun generateQRMer() {
        val pID = Encryption.generatePID()
        val psuDeviceId = Encryption.generatePSUDeviceId()
        val psuIpAddress = Encryption.getPSUIPADDRESS()
        val psuId = Encryption.generatePSUID(this)

        val tranAmount = amountEditText.text.toString()
        val mobileNumber = ""
        val referenceNumber = pID + Encryption.getRandom4Digit()
        val billNo = billNumEditText.text.toString()

        val dynamicReqData =
            DynamicQRRequest(merchantID, tranAmount, mobileNumber, billNo, referenceNumber)
        val call = ApiClient.apiService.generateDynamicQRCode(
            pID,
            psuDeviceId,
            getAndroidId(this),
            psuIpAddress,
            psuId,
            psuChannel,
            merchantID,
            userID,
            unitId,
            dynamicReqData
        )
        call.enqueue(object : Callback<DynamicQRResponse> {
            override fun onResponse(
                call: Call<DynamicQRResponse>, response: Response<DynamicQRResponse>
            ) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val loginEntityJsonString = loginResponse?.base64QR
                    val base64QR = loginEntityJsonString.toString()
                    if (base64QR.contains("Unable to generate QR code")) {
                        AlertDialog.Builder(this@DynamicQRActivity).setTitle("QR Code Error")
                            .setMessage(base64QR).setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }.show()
                    } else {
                        timeText.visibility = View.VISIBLE
                        startTimerText()
                        generateQRCodeButton.isEnabled = false
                        billNumEditText.isEnabled = false
                        amountEditText.isEnabled = false
                        val decodedBytes = Base64.decode(base64QR, Base64.DEFAULT)
                        val bitmap =
                            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        imageView.setImageBitmap(bitmap)
                        playSound()
                        startPollingCustomerPayDetails(
                            merchantID,
                            getAndroidId(this@DynamicQRActivity),
                            referenceNumber,
                            psuDeviceId
                        )
                    }
                } else {
                    Toast.makeText(
                        this@DynamicQRActivity, "Error: " + response.message(), Toast.LENGTH_LONG
                    ).show()
                 //   println("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<DynamicQRResponse>, t: Throwable) {
              //  println("Network error: ${t.message}")
                Toast.makeText(
                    this@DynamicQRActivity, "Something Went Wrong at Server End", Toast.LENGTH_LONG
                ).show()

            }
        })
    }

    private fun startPollingCustomerPayDetails(
        merchantId: String, deviceId: String, referenceNumber: String, psuDeviceID: String
    ) {
        runnable = object : Runnable {
            override fun run() {
                if (shouldContinuePolling) {
                    callCustomerPayDetailsApi(merchantId, deviceId, referenceNumber, psuDeviceID)
                    handler.postDelayed(this, 5000) // Call API every 5 seconds
                } else {
                    shouldContinuePolling = false
                }
            }
        }
        handler.post(runnable)
    }

    private fun callCustomerPayDetailsApi(
        merchantId: String, deviceId: String, referenceNumber: String, psuDeviceID: String
    ) {
        val call = ApiClient.apiService.getCustomerDetails(
            merchantId, deviceId, referenceNumber, psuDeviceID
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>, response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val encryptedResponse = response.body()?.string()
                    if (encryptedResponse != null) {
                        val decryptedResponse = Encryption.decrypt(encryptedResponse, psuDeviceID)
                        if (decryptedResponse != "null") {
                            val objectMapper = jacksonObjectMapper()
                            val loginResponse = objectMapper.readValue(
                                decryptedResponse, CustomerPayResponse::class.java
                            )
                            if (loginResponse.tran_status != "null") {
                                when (loginResponse.tran_status) {
                                    "SUCCESS" -> {
                                        shouldContinuePolling = false
                                        screenTimeOut = false
                                        stopTimerText()
                                        val text =
                                            "Pula  " + loginResponse.amount + "is Deposited into Merchant Account"
                                        speakOut(text)
                                        showImageAlertDialog(
                                            loginResponse.merchant_id,
                                            loginResponse.device_id,
                                            loginResponse.tran_status,
                                            loginResponse.amount.toString(),
                                            loginResponse.referencelabel,
                                            loginResponse.tran_id,
                                            loginResponse.tran_date,
                                            loginResponse.merchant_name,
                                            loginResponse.merchant_addr,
                                            loginResponse.merchant_city,
                                            loginResponse.merchant_terminal
                                        )
                                    }
                                    "FAILURE" -> {
                                        shouldContinuePolling = false
                                        screenTimeOut = false
                                        stopTimerText()
                                        val text = "TRANSACTION FAILED"
                                        speakOut(text)
                                        callFailedDialog()
                                    }
                                    else -> {
                                        shouldContinuePolling = true
                                        screenTimeOut = true
                                    }
                                }

                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        this@DynamicQRActivity, "Error: " + response.message(), Toast.LENGTH_LONG
                    ).show()
                 //   println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
              //  println("Network error: ${t.message}")
                Toast.makeText(
                    this@DynamicQRActivity, "Something Went Wrong at Server End", Toast.LENGTH_LONG
                ).show()

            }
        })
    }

    private fun showImageAlertDialog(
        merchantId: String,
        deviceId: String,
        tranStatus: String,
        amount: String,
        referenceLabel: String,
        tranId: String,
        tranDate: Date,
        merchantName: String,
        merchantAddress: String,
        merchantCity: String,
        merchantTerminal: String
    ) {
        if (!isFinishing && !isDestroyed) {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val view = inflater.inflate(R.layout.transactionsuccess, null)
            builder.setView(view)
            builder.setCancelable(false)
            builder.setPositiveButton("OK") { dialog, _ ->
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                dialog.dismiss()
                val dialogFragment = CustomDialogFragment.newInstance(
                    merchantId,
                    deviceId,
                    tranStatus,
                    amount,
                    referenceLabel,
                    tranId,
                    tranDate.toString(),
                    merchantName,
                    merchantAddress,
                    merchantCity,
                    merchantTerminal,
                    billNumEditText.text.toString(),
                    emailId,
                    contactId
                )
                dialogFragment.show(supportFragmentManager, "CustomDialogFragment")
                dialogFragment.dialog?.setCancelable(false)
            }
            val alertDialog = builder.create()
            alertDialog.setOnKeyListener { _, keyCode, event ->
                keyCode == android.view.KeyEvent.KEYCODE_BACK && event.action == android.view.KeyEvent.ACTION_UP
            }
            alertDialog.show()
        }
    }

    private fun speakOut(text: String) {
        val speechRate = 0.8f
        textToSpeech.setSpeechRate(speechRate)
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun callFailedDialog() {
        if (!isFinishing && !isDestroyed) {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val view = inflater.inflate(R.layout.transactionfailed, null)
            builder.setCancelable(false)
            builder.setView(view)
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(amountEditText.windowToken, 0)
    }

    private fun playSound() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, R.raw.my_sound)
        mediaPlayer?.start()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTimerText() {
        countDownTimer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                val timeFormatted = String.format("%02d:%02d", minutes, seconds)
                timeText.text = getString(R.string.qr_code_expiry, timeFormatted)
            }
            override fun onFinish() {
            }
        }.start()
    }
    private fun stopTimerText(){
        countDownTimer?.cancel()
    }
}