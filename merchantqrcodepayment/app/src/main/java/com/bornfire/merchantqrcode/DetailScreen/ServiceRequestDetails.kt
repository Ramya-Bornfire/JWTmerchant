package com.bornfire.merchantqrcode.DetailScreen

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import com.bornfire.merchantqrcode.AdminScreens.ServiceRequest
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.BaseActivity
import com.bornfire.merchantqrcode.DataModel.EncryptedRequest
import com.bornfire.merchantqrcode.DataModel.SerReqData
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.NetworkUtils
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.PhNumValidation
import com.bornfire.merchantqrcode.retrofit.ApiClient
import com.bornfire.merchantqrcode.retrofit.Encryption
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.hbb20.CountryCodePicker
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ServiceRequestDetails : BaseActivity() {
    lateinit var editReqId: EditText
    lateinit var editMerId: EditText
    lateinit var editReqType: EditText
    lateinit var editReqDesc: EditText
    lateinit var editReproduce: EditText
    lateinit var editErrMsg: EditText
    lateinit var editPriority: EditText
    lateinit var editEmail: EditText
    lateinit var editPhone: EditText
    lateinit var editAddNotes: EditText
    lateinit var requestBtn:Button
    lateinit var countryCodePicker1: CountryCodePicker
    private lateinit var textrequesttype: TextView
    private lateinit var textrequestdesc: TextView
    private lateinit var textsteps: TextView
    private lateinit var texterrormsg: TextView
    private lateinit var textpriority: TextView
    private lateinit var textemailid: TextView
    private lateinit var textphonenum: TextView
    val requesttype = "REQUEST TYPE"
    val requestdesc = "REQUEST DESCRIPTION"
    val stepstorep = "STEPS TO REPRODUCE"
    val errormsg = "ERROR MESSAGE"
    val prioritywise = "PRIORITY"
    val mailid = "CONTACT EMAIL"
    val mobnum = "CONTACT PHONE"
    val merchantdata = SharedMerchantDataObj.merchantData
    var screenid:String = "33"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_request_details)
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            val inflater = LayoutInflater.from(this)
            val customView = inflater.inflate(R.layout.custom_action_bar, null)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            actionBar.customView = customView
            // Set the title
            val titleTextView: TextView = customView.findViewById(R.id.action_bar_title)
            titleTextView.text = getString(R.string.ser_req_det)
            val helpImg = findViewById<ImageView>(R.id.help_image)
            helpImg.setOnClickListener(){
                HelpInfo.getInfo(this,screenid)
            } }
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        textrequesttype = findViewById(R.id.textrequesttype)
        textrequestdesc = findViewById(R.id.textrequestdesc)
        textsteps = findViewById(R.id.textsteps)
        texterrormsg = findViewById(R.id.texterror)
        textpriority = findViewById(R.id.textpriority)
        textemailid = findViewById(R.id.textemail)
        textphonenum = findViewById(R.id.textmobilenumber)
        editReqId = findViewById(R.id.editReqId)
        editMerId = findViewById(R.id.editMerId)
        editReqType = findViewById(R.id.editReqType)
        editReqDesc = findViewById(R.id.editReqDesc)
        editReproduce = findViewById(R.id.editReproduce)
        editPriority = findViewById(R.id.editPriority)
        editErrMsg = findViewById(R.id.editErrMsg)
        editEmail = findViewById(R.id.editEmail)
        editPhone = findViewById(R.id.editPhone)
        editAddNotes = findViewById(R.id.editAddNotes)
        requestBtn = findViewById(R.id.requestBtn)
        countryCodePicker1 = findViewById(R.id.country_code1)

        val buttonName = intent.getStringExtra("Button")
        //Add Screen
        if(buttonName==null) {
            getSereqUniqueId()
            setEditMode()
            screenid = "36"
            requestBtn.setText("Request")
            editMerId.setText(merchantdata?.merchant_user_id!!)
            countryCodePicker1.setOnCountryChangeListener {PhNumValidation.updateExpectedLength(countryCodePicker1.selectedCountryNameCode, editPhone) }
            editPhone.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) { PhNumValidation.updateExpectedLength(countryCodePicker1.selectedCountryNameCode, editPhone) } })
            editPriority.setOnClickListener(){
                showPriority(editPriority)
            }
            requestBtn.setOnClickListener() {
                if (validateFields()) {
                    if (NetworkUtils.isNetworkAvailable(this)) {
                        PostServiceReq()
                    } else {
                        NetworkUtils.NoInternetAlert(this)
                    }
                } else {
                    AlertDialogBox().showDialog(this,"Please fill the required field")
                }
            }
        }
        else {
            Updatemethod(requestBtn)
        }
    }

    fun setTextViewMode() {
        textrequesttype.text = requesttype
        textrequestdesc.text = requestdesc
        textsteps.text = stepstorep
        texterrormsg.text = errormsg
        textpriority.text = prioritywise
        textemailid.text = mailid
        textphonenum.text = mobnum
    }
    fun setEditMode() {
        textrequesttype.text = getSpannableStringWithRedStar(requesttype)
        textrequestdesc.text = getSpannableStringWithRedStar(requestdesc)
        textsteps.text = getSpannableStringWithRedStar(stepstorep)
        texterrormsg.text = getSpannableStringWithRedStar(errormsg)
        textpriority.text = getSpannableStringWithRedStar(prioritywise)
        textemailid.text = getSpannableStringWithRedStar(mailid)
        textphonenum.text = getSpannableStringWithRedStar(mobnum)
    }
    fun getSpannableStringWithRedStar(text: String): SpannableString {
        val spannable = SpannableString("$text *") // Append a space and a star
        val starIndex = text.length + 1 // Index where the star is appended

        // Apply color to the star
        spannable.setSpan(ForegroundColorSpan(Color.RED), starIndex, starIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannable
    }

    private fun Updatemethod(notifyBtn: Button?) {

        val requestId = intent.getStringExtra("request_id").orEmpty()
        val merchantId = intent.getStringExtra("merchant_id").orEmpty()
        val requestType = intent.getStringExtra("request_type").orEmpty()
        val requestDescription = intent.getStringExtra("request_description").orEmpty()
        val stepsToReproduce = intent.getStringExtra("steps_to_reproduce").orEmpty()
        val priority = intent.getStringExtra("priority").orEmpty()
        val errorMessage = intent.getStringExtra("error_message").orEmpty()
        val contactEmail = intent.getStringExtra("contact_email").orEmpty()
        val contactPhone = intent.getStringExtra("contact_phone").orEmpty()
        val additionalNotes = intent.getStringExtra("additional_notes").orEmpty()
        val countrycode1 = intent.getStringExtra("countrycode1")
        if (countrycode1 != null) {
            countryCodePicker1.setCountryForPhoneCode(countrycode1.toInt())
        }
        editReqId.setText(requestId)
        editMerId.setText(merchantId)
        editReqType.setText(requestType)
        editReqDesc.setText(requestDescription)
        editReproduce.setText(stepsToReproduce)
        editPriority.setText(priority)
        editErrMsg.setText(errorMessage)
        editEmail.setText(contactEmail)
        editPhone.setText(contactPhone)
        editAddNotes.setText(additionalNotes)
        hideHint(editReqId, editMerId,editReqType,editReqDesc,editReproduce,editPriority,editErrMsg,editEmail,editPhone,editAddNotes)
        setEditTextsColor(
            editReqId, editMerId,editReqType,editReqDesc,editReproduce,editPriority,editErrMsg,editEmail,editPhone,editAddNotes
        )
        setEditTextsNonEditable(editReqId, editMerId,editReqType,editReqDesc,editReproduce,editPriority,editErrMsg,editEmail,editPhone,editAddNotes)
        notifyBtn?.visibility = View.GONE
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun PostServiceReq() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val currentDate: Date = calendar.time
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateString: String = dateFormat.format(currentDate)
        val reqDate: Date = dateFormat.parse(dateString)!!
        val servicedata = SerReqData(editReqId.text.toString(),editMerId.text.toString(),editReqType.text.toString(),editReqDesc.text.toString(),
            editReproduce.text.toString(),editErrMsg.text.toString(),editPriority.text.toString(),editEmail.text.toString(),editPhone.text.toString(),countryCodePicker1.selectedCountryCode,editAddNotes.text.toString(),"Pending","","",merchantdata?.merchant_rep_id,dateString,null,null,merchantdata?.unit_id.toString(),merchantdata?.merchant_user_id.toString())
        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(servicedata)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)
        //println(encryptedText)
        //println(psuDeviceID)
        val call = ApiClient.apiService.addServiceRequest(encryptedRequest,psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    // Handle the successful response here (update UI, show toast, etc.)
                    val encryptedResponseBody = response.body()?.string()
                    if (encryptedResponseBody != null) {
                       // println("Encrypted Response---------> " +  encryptedResponseBody )
                        try {
                            val decryptedResponse = Encryption.decrypt(encryptedResponseBody.toString(), psuDeviceID)
                            callDialog()
                         //   println("Decrypted Response: $decryptedResponse")
                        } catch (e: Exception) {
                           // e.printStackTrace()
                            Toast.makeText(this@ServiceRequestDetails, "Decryption failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        //println("Error: Empty response body")
                    }
                }
                else {
                    Toast.makeText(this@ServiceRequestDetails, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                    //println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //println("Network error: ${t.message}")
                Toast.makeText(this@ServiceRequestDetails,"Something Went Wrong at Server End",
                    Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun validateFields(): Boolean {
        val emailRegex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+$")

        if(editReqId.text.toString().isEmpty() || editReqDesc.text.toString().isEmpty() || editReproduce.text.toString().isEmpty() || editErrMsg.text.toString().isEmpty() ||
            editPriority.text.toString().isEmpty() ||  editEmail.text.toString().isEmpty() || !editEmail.text.matches(emailRegex) || editPhone.text.toString().isEmpty() || editPhone.error!=null) {

            if (editReqType.text.toString().isEmpty()) {
                editReqType.error = "Please Enter Request Type"
            }
            if (editReqDesc.text.toString().isEmpty()) {
                editReqDesc.error = "Please Enter Request Description"
            }
            if (editReproduce.text.toString().isEmpty()) {
                editReproduce.error = "Please Enter Steps to Reproduce"
            }
            if (editErrMsg.text.toString().isEmpty()) {
                editErrMsg.error = "Please Enter Error Message"
            }
            if (editPriority.text.toString().isEmpty()) {
                editPriority.error = "Please Choose Priority"
            }
            else{
                editPriority.error = null
                editPriority.clearFocus()
            }
            if (editEmail.text.toString().isEmpty()) {
                editEmail.error = "Please Enter Email Id"
            } else if (!editEmail.text.matches(emailRegex)) {
                editEmail.error = "Invalid Email Format"
            }
            if (editPhone.text.toString().isEmpty()) {
                editPhone.error = "Please Enter Mobile Number"
            }
            else if(editPhone.error!=null){
                return false
            }
            return false
        }
    return true
    }
    private fun showPriority(anchor: EditText) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(R.menu.priority_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.high -> {
                    // Handle selection
                    anchor.setText("HIGH")
                    true
                }
                R.id.med -> {
                    // Handle selection
                    anchor.setText("MEDIUM")
                    true
                }
                R.id.low -> {
                    // Handle selection
                    anchor.setText("LOW")
                    true
                }
                // Add more items as needed
                else -> false
            }
        }
        popupMenu.show()
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun callDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.sucessdialog, null)
        builder.setCancelable(false)
        // Set the custom view to the builder
        builder.setView(view)
        // Set other properties of the AlertDialog (e.g., title, buttons, etc.)
        builder.setPositiveButton("OK") { dialog, which ->
            // Handle positive button click
            dialog.dismiss()
            val intent = Intent(this, ServiceRequest::class.java)
            startActivity(intent)
            finish()
        }
        val alertDialog = builder.create()
        alertDialog.show()

    }
    fun setEditTextsColor(vararg editTexts: EditText) {
        for (editText in editTexts) {
            editText.setTextColor(getResources().getColor(R.color.lightgrey))
        }
    }
    fun setEditTextsNonEditable(vararg editTexts: EditText) {
        for (editText in editTexts) {
            editText.isEnabled = false
        }
    }
    private fun hideHint(vararg editTexts: EditText){
        for (editText in editTexts) {
            editText.hint = null
        }
    }
    private fun getSereqUniqueId() {
        val call = ApiClient.apiService.getServiceId()
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val response = response.body()
                    editReqId.setText(response.toString()) }
                else {
                    Toast.makeText(this@ServiceRequestDetails, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                  //  println("Failed to get service request id: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
              //  Log.d("Get Notification Id", "Network error: ${t.message}")
            }
        })
    }
}
