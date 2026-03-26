package com.bornfire.merchantqrcode

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBar
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.DataModel.InitPayReq
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.DataModel.SharedusercatDataObj
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.NullCheck.getValidText
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.bornfire.merchantqrcode.retrofit.Encryption.getAndroidId
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class InitiatePayment  : BaseActivity() {
    private val userSharedCategory= SharedusercatDataObj.UserCategory
    private var mediaPlayer: MediaPlayer? = null
    val userData = SharedUserDataObj.userData
    companion object {
        const val EXTRA_MESSAGE = "com.bornfire.merchantqrcode.MESSAGE"
    }
    var message = ""
    var s = ""
    private lateinit var initiateButton : Button
    private lateinit var merchantID:String
    private val merchantSharedData = SharedMerchantDataObj.merchantData
    val userdata=SharedUserDataObj.userData
    var dbAmountLimit :Double = 0.00
    private lateinit var payloadFormatIndicatorET:EditText
    private lateinit var poiMethodET:EditText
    private lateinit var globalUniqueIdET:EditText
    private lateinit var payeeParticipantCodeET:EditText
    private lateinit var customerIdET:EditText
    private lateinit var transactionAmountET:EditText
    private lateinit var customerNameET:EditText
    private lateinit var cityET:EditText
    private lateinit var billNumberET:EditText
    private lateinit var mobileET:EditText
    private lateinit var customerCurrencyET:EditText
    private lateinit var customerCountryCode:EditText
    private lateinit var cancelButton:Button
    private lateinit var customerId :String
    private lateinit var customerName :String
    private lateinit var customerDeviceID :String
    private lateinit var customerReferenceLabel :String
    private lateinit var purposeTransaction:String
    private lateinit var customerCity:String
    private lateinit var purposeOfTran:EditText
    private lateinit var transactionAmountText: TextView
    private lateinit var billNumberText: TextView
    private lateinit var purposeOfTranText:TextView
    private val tranAmount = "Transaction Amount"
    private val billNum = "Bill Number"
    private val remark="Remark"
    private lateinit var emailId:String
    private lateinit var contactId:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initiate_payment)
        val actionBar = supportActionBar
        if (actionBar != null) {
            val inflater = LayoutInflater.from(this)
            val customView = inflater.inflate(R.layout.custom_action_bar, null)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            actionBar.customView = customView
            val titleTextView: TextView = customView.findViewById(R.id.action_bar_title)
            titleTextView.text = getString(R.string.init_pay)
            val helpImg = findViewById<ImageView>(R.id.help_image)
            helpImg.setOnClickListener{
                HelpInfo.getInfo(this,"6")
            }
        }
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
         customerId = intent.getStringExtra("customerId").toString()
         customerName = intent.getStringExtra("customerName").toString()
        customerDeviceID = intent.getStringExtra("customerDeviceId").toString()
        customerReferenceLabel = intent.getStringExtra("customerReferenceNumber").toString()
        purposeTransaction = intent.getStringExtra("Purpose").toString()
        customerCity = intent.getStringExtra("City").toString()

        payloadFormatIndicatorET = findViewById(R.id.payloadFormatIndicator)
        poiMethodET = findViewById(R.id.poiMethod)
        globalUniqueIdET = findViewById(R.id.globalUniqueId)
        payeeParticipantCodeET = findViewById(R.id.payeeParticipantCode)
        customerIdET = findViewById(R.id.customerIdEt)
        customerNameET = findViewById(R.id.customerNameEt)
        cityET = findViewById(R.id.city)
        customerCurrencyET=findViewById(R.id.transactionCurrency)
        customerCountryCode=findViewById(R.id.countryCode)
        billNumberET = findViewById(R.id.billNumber)
        mobileET = findViewById(R.id.mobile)
        transactionAmountET = findViewById(R.id.transactionAmount)
        transactionAmountText=findViewById(R.id.texttranamount)
        billNumberText=findViewById(R.id.textbillnumber)
        purposeOfTranText=findViewById(R.id.purpose_trans_txt)
        purposeOfTran = findViewById(R.id.edit_purpose_trans)
        setEditMode()
        payloadFormatIndicatorET.setText(getString(R.string.payload))
        poiMethodET.setText(R.string.poi)
        globalUniqueIdET.setText(R.string.global)
        payeeParticipantCodeET.setText(R.string.payee)
        customerIdET.setText(maskMiddleDigits(customerId))
        customerNameET.setText(customerName)
     //   purposeOfTran.setText(getValidText(purposeTransaction))
        payloadFormatIndicatorET.isFocusable = false
        payloadFormatIndicatorET.isFocusableInTouchMode = false
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        poiMethodET.isFocusable = false
        poiMethodET.isFocusableInTouchMode = false
        globalUniqueIdET.isFocusable = false
        globalUniqueIdET.isFocusableInTouchMode = false
        payeeParticipantCodeET.isFocusable = false
        payeeParticipantCodeET.isFocusableInTouchMode = false
        customerIdET.isFocusable = false
        customerIdET.isFocusableInTouchMode = false
        transactionAmountET.isFocusable = false
        transactionAmountET.isFocusableInTouchMode = false
        customerNameET.isFocusable = false
        customerNameET.isFocusableInTouchMode = false
        cityET.isFocusable = false
        cityET.isFocusableInTouchMode = false
        billNumberET.isFocusable = false
        billNumberET.isFocusableInTouchMode = false
        mobileET.isFocusable = false
        mobileET.isFocusableInTouchMode = false
        transactionAmountET.isFocusable = false
        transactionAmountET.isFocusableInTouchMode = false
        customerCurrencyET.isFocusable=false
        customerCurrencyET.isFocusableInTouchMode=false
        customerCountryCode.isFocusable=false
        customerCountryCode.isFocusableInTouchMode=false
        customerCountryCode.setText(getString(R.string.BW))
        customerCurrencyET.setText(getString(R.string.BWP))
        cityET.setText(getValidText(customerCity))
        if(billNumberET.text.toString()=="null"){
            billNumberET.setText("")
        }
        if(mobileET.text.toString()=="null"){
            mobileET.setText("")
        }
        if(transactionAmountET.text.toString()=="null"){
            transactionAmountET.setText("")
        }
        if(poiMethodET.text.toString()=="11"){
            billNumberET.isFocusable = true
            billNumberET.isFocusableInTouchMode = true
            mobileET.isFocusable = true
            mobileET.isFocusableInTouchMode = true
            transactionAmountET.isFocusable = true
            transactionAmountET.isFocusableInTouchMode = true
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.my_sound)
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        requestedOrientation = if (isTablet) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        if (userSharedCategory?.user_category == "Representative") {
            merchantID = merchantSharedData?.merchant_user_id.toString()
            emailId = merchantSharedData?.email_address.toString()
            contactId = merchantSharedData?.mobile_no.toString()
        }else{
            merchantID =userData?.merchant_user_id.toString()
            emailId = userData?.email_address1.toString()
            contactId = userData?.mobile_no1.toString()
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@InitiatePayment, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
        initiateButton = findViewById(R.id.initbtn)
        cancelButton=findViewById(R.id.cancelbtn)
        cancelButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        checkAmountLimit()
        message = intent.getStringExtra(EXTRA_MESSAGE).toString()
      //  println(message)
        initiateButton.setOnClickListener{
            initiateButton.isEnabled=false
            if(validate()){
                initTransaction()
            }
        }
    }
    private fun setEditMode() {
        transactionAmountText.text=getSpannableStringWithRedStar(tranAmount)
        billNumberText.text=getSpannableStringWithRedStar(billNum)
        purposeOfTranText.text=getSpannableStringWithRedStar(remark)
    }
    private fun getSpannableStringWithRedStar(text: String): SpannableString {
        val spannable = SpannableString("$text *")
        spannable.setSpan(ForegroundColorSpan(Color.RED), spannable.length - 1, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }
    private fun checkAmountLimit() {
        val call  = ApiClient.apiService.getTranAmountLimit(merchantID)
        call.enqueue(object : Callback<ResponseBody> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                dbAmountLimit = if (response.isSuccessful) {
                    val response1 = response.body()?.string()
                    response1?.toDoubleOrNull() ?: 50000.00
                } else {
                 //   println("Error: Received HTTP ${response.code()} - ${response.message()}")
                    Toast.makeText(this@InitiatePayment, "Error: Unable to fetch amount limit. Please try again.", Toast.LENGTH_LONG).show()
                    50000.00
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
               // println("Network error: ${t.message}")
                Toast.makeText(this@InitiatePayment,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()

            }
        })
    }
    private fun validate(): Boolean {

        if (transactionAmountET.text.isEmpty()) {
            AlertDialogBox().showDialog(this, "Transaction amount is empty")
            return false
        }
        val transactionAmount = transactionAmountET.text.toString().toDoubleOrNull()
        if (transactionAmount == null) {
           AlertDialogBox().showDialog(this, "Invalid transaction amount")
            return false
        }
        if (transactionAmount == 0.0) {
            AlertDialogBox().showDialog(this, "Transaction amount cannot be zero")
            return false
        }
        if (!transactionAmountET.text.matches(Regex("^\\d+(\\.\\d{1,2})?$"))) {
            AlertDialogBox().showDialog(this, "Invalid transaction amount format")
            return false
        }
        if (transactionAmount > dbAmountLimit) {
            AlertDialogBox().showDialog(this, "Amount should not exceed $dbAmountLimit")
            return false
        }
        if (billNumberET.text.isEmpty()) {
            AlertDialogBox().showDialog(this, "Bill number is empty")
            return false
        }
        if (billNumberET.text.length < 3) {
            AlertDialogBox().showDialog(this, "Bill number should be a minimum of three characters")
            return false
        }
        if (!billNumberET.text.matches("[a-zA-Z0-9]+".toRegex())) {
            AlertDialogBox().showDialog(this, "Only alphabets and numbers are allowed")
            return false
        }
        if(purposeOfTran.text.isEmpty()){
            AlertDialogBox().showDialog(this, "Remark is empty")
            return false
        }
        return true
    }

    private fun initTransaction() {
        val pID = Encryption.generatePID()
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val psuIpAddress = Encryption.getPSUIPADDRESS()
        val psuId=Encryption.generatePSUID(this)
        val psuChannel="Mobile"
        var payDetails= InitPayReq("","","","","","",
        "","","","","","","","","","",
        "","","","","","","","","","","")
        val merchantReferenceId=pID+ Encryption.getRandom4Digit()
            if (userSharedCategory?.user_category == "Representative") {
                payDetails = InitPayReq(
                    payloadFormatIndicatorET.text.toString(),
                    poiMethodET.text.toString(),
                    globalUniqueIdET.text.toString(),
                    payeeParticipantCodeET.text.toString(),
                    "",
                    customerId,
                    customerNameET.text.toString(),
                    cityET.text.toString(),
                    "",
                    "BW",
                    merchantSharedData?.mobile_no.toString(),
                    transactionAmountET.text.toString(),
                    customerReferenceLabel,
                    customerDeviceID,
                    billNumberET.text.toString(),
                    "BW",
                    "BWP",
                    merchantSharedData?.user_status.toString(),
                    merchantReferenceId,
                    getAndroidId(this),
                    "",
                    "",
                    merchantSharedData?.merchant_user_id.toString(),
                    merchantSharedData?.merchant_name.toString(),
                    merchantSharedData?.merchant_rep_id.toString(),
                    merchantSharedData?.unit_id.toString(),
                    purposeOfTran.text.toString()
                )

            }
            else {
                payDetails = InitPayReq(
                    payloadFormatIndicatorET.text.toString(),
                    poiMethodET.text.toString(),
                    globalUniqueIdET.text.toString(),
                    payeeParticipantCodeET.text.toString(),
                    "",
                    customerId,
                    customerNameET.text.toString(),
                    cityET.text.toString(),
                    "",
                    "BW",
                    userdata!!.mobile_no1.toString(),
                    transactionAmountET.text.toString(),
                    customerReferenceLabel,
                    customerDeviceID,
                    billNumberET.text.toString(),
                    "BW",
                    "BWP",
                    userdata.user_status1!!,
                    merchantReferenceId,
                    getAndroidId(this),
                    "",
                    "",
                    userdata.merchant_user_id!!,
                    userdata.merchant_name!!,
                    userdata.user_id,
                    userdata.unit_id,
                    purposeOfTran.text.toString() )
            }
        val call = ApiClient.apiService.initiateTransaction(pID,psuDeviceID,psuIpAddress,psuId,psuChannel,"6","7",payDetails)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val response1 = response.body()
                  //  println("Response: $response1")
                    showTransactionInitiatedAlert(this@InitiatePayment)
                } else {
                    Toast.makeText(this@InitiatePayment,response.toString(),Toast.LENGTH_LONG).show()
                   // println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
               // println("Network error: ${t.message}")
                Toast.makeText(this@InitiatePayment,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()

            }
        })
    }
    fun showTransactionInitiatedAlert(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Transaction Status")
        builder.setCancelable(false)
        builder.setMessage("Transaction Initiated Successfully")
        playSound()
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()
    }
    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        return true
    }
    private fun playSound() {
        mediaPlayer?.start()
    }
    private fun maskMiddleDigits(input: String): String {
        return if (input.length < 8) {
            input // If the string is shorter than 8 characters, return it as is.
        } else {
            // Masking characters from index 3 to index 6 (4th to 7th)
            val start = input.substring(0, 3) // First 3 characters
            val masked = "XXXX" // Masked portion
            val end = input.substring(7) // Last 2 characters
            start + masked + end
        }
    }
}