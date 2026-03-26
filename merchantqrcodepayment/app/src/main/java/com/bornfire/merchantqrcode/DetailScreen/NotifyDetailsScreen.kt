package com.bornfire.merchantqrcode.DetailScreen

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import com.bornfire.merchantqrcode.AdminScreens.NotificationScreen
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.BaseActivity
import com.bornfire.merchantqrcode.DataModel.EncryptedRequest
import com.bornfire.merchantqrcode.DataModel.NotifyData
import com.bornfire.merchantqrcode.DataModel.SharedMerchantDataObj
import com.bornfire.merchantqrcode.Dialog.AlertDialogBox
import com.bornfire.merchantqrcode.Dialog.FrequencyDialog.showfrequencey
import com.bornfire.merchantqrcode.Dialog.SuccessDialog.callDialog
import com.bornfire.merchantqrcode.Dialog.YesNoDialog.showYesOrNO
import com.bornfire.merchantqrcode.NetworkUtils
import com.bornfire.merchantqrcode.R
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.PhNumValidation.setupPhoneNumberValidation
import com.bornfire.merchantqrcode.Utils.ReferenceCode
import com.bornfire.merchantqrcode.Utils.ReferenceCodeCallback
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

class NotifyDetailsScreen : BaseActivity() {
    lateinit var editsrlNo:EditText
    lateinit var editEveNo:EditText
    lateinit var editNotLimit:EditText
    lateinit var editNotiUser1:EditText
    lateinit var editNotiUser2:EditText
    lateinit var editNotiUser3:EditText
    lateinit var editEmail1:EditText
    lateinit var editEmail2:EditText
    lateinit var editEmail3:EditText
    lateinit var editRecDate:EditText
    lateinit var editTranCate:EditText
    lateinit var countryCodePicker1: CountryCodePicker
    lateinit var countryCodePicker2:CountryCodePicker
    lateinit var countryCodePicker3:CountryCodePicker
    lateinit var editEveDesc:EditText
    lateinit var editMob1:EditText
    lateinit var editMob2:EditText
    private var mediaPlayer: MediaPlayer? = null
    lateinit var editMob3:EditText
    lateinit var editSMS:AutoCompleteTextView
    lateinit var editEmail:AutoCompleteTextView
    lateinit var editAlert:AutoCompleteTextView
    lateinit var editFrq:EditText
    lateinit var notifyBtn:Button
    val merchantdata = SharedMerchantDataObj.merchantData
    lateinit var editEndDate:EditText
    lateinit var editStDate:EditText
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) // Date format
    private lateinit var texteventnumber: TextView
    private lateinit var textnotificationuser: TextView
    private lateinit var textdate: TextView
    private lateinit var texttran: TextView
    private lateinit var texteventdesc: TextView
    private lateinit var textmobilenumber: TextView
    private lateinit var textfrequency: TextView
    val eventnumber="EVENT NUMBER"
    val notificationuser="NOTIFICATION USER 1"
    val notificationdate="DATE"
    val tran="TRAN CATEGORY"
    val eventdesc="EVENT DESCRIPTION"
    val mobilenum="MOBILE 1"
    val frequency="FREQUENCY"
    var screenid = "31"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notify_details_screen)
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            val inflater = LayoutInflater.from(this)
            val customView = inflater.inflate(R.layout.custom_action_bar, null)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            actionBar.customView = customView
            // Set the title
            val titleTextView: TextView = customView.findViewById(R.id.action_bar_title)
            titleTextView.text = getString(R.string.notify_det)
            val helpImg = findViewById<ImageView>(R.id.help_image)
            helpImg.setOnClickListener(){
                HelpInfo.getInfo(this,screenid)
            }
        }
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)

        texteventnumber=findViewById(R.id.texteventnumber)
        textnotificationuser=findViewById(R.id.textnotificationuser)
        textdate=findViewById(R.id.textdate)
        texttran=findViewById(R.id.texttran)
        texteventdesc=findViewById(R.id.texteventdesc)
        textmobilenumber=findViewById(R.id.textmobilenumber)
        textfrequency=findViewById(R.id.textfrequency)
        mediaPlayer = MediaPlayer.create(this, R.raw.iphone)
        editsrlNo = findViewById(R.id.editsrlNo)
        editEveNo = findViewById(R.id.editEveNo)
        editNotLimit = findViewById(R.id.editNotLimit)
        editNotiUser1 = findViewById(R.id.editNotiUser1)
        editNotiUser2 = findViewById(R.id.editNotiUser2)
        editNotiUser3 = findViewById(R.id.editNotiUser3)
        editEmail1 = findViewById(R.id.editEmail1)
        editEmail2 = findViewById(R.id.editEmail2)
        editEmail3 = findViewById(R.id.editEmail3)
        editRecDate = findViewById(R.id.editRecDate)
        editTranCate = findViewById(R.id.editTranCate)
        editEveDesc = findViewById(R.id.editEveDesc)
        editMob1 = findViewById(R.id.mobnum1)
        editMob2 = findViewById(R.id.mobnum2)
        editMob3 = findViewById(R.id.mobnum3)
        countryCodePicker1 = findViewById(R.id.country_code1)
        countryCodePicker2 = findViewById(R.id.country_code2)
        countryCodePicker3=findViewById(R.id.country_code3)
        editSMS = findViewById(R.id.editSMS)
        editEmail = findViewById(R.id.editEmail)
        editAlert = findViewById(R.id.editAlert)
        notifyBtn = findViewById(R.id.notifyBtn)
        editEndDate = findViewById(R.id.editEndDate)
        editStDate = findViewById(R.id.editStDate)
        editFrq = findViewById(R.id.editFrq)

        editTranCate.setOnClickListener(){
            val refId = "NP"
            if (refId.isNotEmpty()) {
                ReferenceCode.getInfo(this, refId, object : ReferenceCodeCallback {
                    override fun onSuccess(data: List<String>) {
                        // Show the popup window with the received data
                        ReferenceCode.showReferenceCodePopup(this@NotifyDetailsScreen,editTranCate, data) }
                    override fun onError(errorMessage: String) {
                    }
                })
            }
        }
        setupClickListener(editSMS)
        setupClickListener(editEmail)
        setupClickListener(editAlert)
        editFrq.setOnClickListener(){
            editFrq.error = null
            editFrq.clearFocus()
            showfrequencey(this,editFrq)
        }
        val buttonName = intent.getStringExtra("Button")
        //Add Screen
        if(buttonName==null) {
            getNotiUniqueId()
            setupPhoneNumberValidation(editMob1, countryCodePicker1)
            setupPhoneNumberValidation(editMob2, countryCodePicker2)
            setupPhoneNumberValidation(editMob3, countryCodePicker3)
            setEditMode()
            screenid = "37"
            notifyBtn.setText("Add Notification")
            setCurrentDate(editRecDate)
            editStDate.setOnClickListener {
                openStartDatePickerDialog()
            }
            // Listener for End Date
            editEndDate.setOnClickListener {
                if (editStDate.text.isNotEmpty()) {
                    openEndDatePickerDialog()
                } else {
                    Toast.makeText(this, "Please select a start date first", Toast.LENGTH_SHORT).show()
                }
            }
            notifyBtn.setOnClickListener() {
                if (validateFields()) {
                    if (NetworkUtils.isNetworkAvailable(this)) {
                        PostNotificationdata()
                    } else {
                        NetworkUtils.NoInternetAlert(this)
                    }
                } else {
                    AlertDialogBox().showDialog(this,"Please fill the required field")
                }
            }
        }
        else {
            Updatemethod(notifyBtn)
        }
    }
    private fun setupClickListener(editText: EditText) {
        editText.setOnClickListener {
            showYesOrNO(this, editText)
        }
    }

    fun setTextViewMode() {
        texteventnumber.text = eventnumber
        textnotificationuser.text = notificationuser
        textdate.text = notificationdate
        texttran.text = tran
        texteventdesc.text = eventdesc
        textmobilenumber.text = mobilenum
        textfrequency.text = frequency
    }
    fun setEditMode() {
        texteventnumber.text = getSpannableStringWithRedStar(eventnumber)
        textnotificationuser.text = getSpannableStringWithRedStar(notificationuser)
        textdate.text = getSpannableStringWithRedStar(notificationdate)
        texttran.text = getSpannableStringWithRedStar(tran)
        texteventdesc.text = getSpannableStringWithRedStar(eventdesc)
        textmobilenumber.text = getSpannableStringWithRedStar(mobilenum)
        textfrequency.text = getSpannableStringWithRedStar(frequency)
    }
    fun getSpannableStringWithRedStar(text: String): SpannableString {
        val spannable = SpannableString("$text *") // Append a space and a star
        val starIndex = text.length + 1 // Index where the star is appended
        spannable.setSpan(ForegroundColorSpan(Color.RED), starIndex, starIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannable
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun PostNotificationdata() {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDate = Date()
        val formattedDateString = dateFormat.format(currentDate)
        val mob1 = getTextOrDefault(editMob1)
        val mob2 = getTextOrDefault(editMob2)
        val mob3 = getTextOrDefault(editMob3)

        val notfydata = NotifyData(editsrlNo.text.toString(),formattedDateString,editTranCate.text.toString(),editEveNo.text.toString(),editEveDesc.text.toString(),editNotLimit.text.toString(),editNotiUser1.text.toString(),editNotiUser2.text.toString(),editNotiUser3.text.toString(),editSMS.text.toString(),
            mob1,mob2,mob3,countryCodePicker1.selectedCountryCode,countryCodePicker2.selectedCountryCode,countryCodePicker3.selectedCountryCode,editEmail.text.toString(),editEmail1.text.toString(),editEmail2.text.toString(),editEmail3.text.toString(),
            editAlert.text.toString(),editStDate.text.toString(),editEndDate.text.toString(),"Y","N",formattedDateString,null,null,
            merchantdata?.merchant_rep_id!!,"","","MERCHANT","Tab",
            merchantdata?.merchant_user_id!!,merchantdata?.unit_id!!,editFrq.text.toString())
        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(notfydata)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)
        val call = ApiClient.apiService.addNotification(encryptedRequest,psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val encryptedResponseBody = response.body()?.string()
                    if (encryptedResponseBody != null) {
                        try {
                              val decryptedResponse = Encryption.decrypt(encryptedResponseBody.toString(), psuDeviceID)
                              //println("Decrypted Response: $decryptedResponse")
                            val intent = Intent(this@NotifyDetailsScreen, NotificationScreen::class.java)
                            callDialog(this@NotifyDetailsScreen, intent)
                        } catch (e: Exception) {
                           // e.printStackTrace()
                            Toast.makeText(this@NotifyDetailsScreen, "Decryption failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                    //    println("Error: Empty response body")
                    }
                } else {
                    Toast.makeText(this@NotifyDetailsScreen, "Error: "+response.message(), Toast.LENGTH_LONG).show()
                   // println("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
              //  println("Network error: ${t.message}")
                Toast.makeText(this@NotifyDetailsScreen,"Something Went Wrong at Server End",
                    Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun Updatemethod(notifyBtn: Button?) {
        editsrlNo.setText(intent.getStringExtra("recSrl"))
        editRecDate.setText(intent.getStringExtra("recDate"))
        editEveNo.setText(intent.getStringExtra("eveNo"))
        editTranCate.setText(intent.getStringExtra("tranCate"))
        editNotLimit.setText(intent.getStringExtra("notiLimit"))
        editEveDesc.setText(intent.getStringExtra("eveDesc"))
        editNotiUser1.setText(intent.getStringExtra("notiUser1"))
        editNotiUser2.setText(intent.getStringExtra("notiUser2"))
        editNotiUser3.setText(intent.getStringExtra("notiUser3"))
        editEmail1.setText(intent.getStringExtra("email1"))
        editEmail2.setText(intent.getStringExtra("email2"))
        editEmail3.setText(intent.getStringExtra("email3"))
        editMob1.setText(intent.getStringExtra("mob1"))
        editMob2.setText(intent.getStringExtra("mob2"))
        editMob3.setText(intent.getStringExtra("mob3"))
        editSMS.setText(intent.getStringExtra("sms"))
        editEmail.setText(intent.getStringExtra("email"))
        editAlert.setText(intent.getStringExtra("alertt"))
        editStDate.setText(intent.getStringExtra("StartDate"))
        editEndDate.setText(intent.getStringExtra("EndDate"))
        editFrq.setText(intent.getStringExtra("Frequency"))
        val countryCodes = listOf(
            intent.getStringExtra("countrycode1"),
            intent.getStringExtra("countrycode2"),
            intent.getStringExtra("countrycode3")
        )
        val countryPickers = listOf(countryCodePicker1, countryCodePicker2, countryCodePicker3)
        countryCodes.forEachIndexed { index, code ->
            val phoneCode = code?.toIntOrNull() // Safely convert to an integer, returns null if conversion fails
            phoneCode?.let {
                countryPickers[index].setCountryForPhoneCode(it)
            } ?: run {
                // Handle the case when code is null or not a valid number
              //  println("Invalid or null phone code at index $index")
            }
        }
        editsrlNo.isEnabled = false
        editEveNo.isEnabled = false
        editNotLimit.isEnabled = false
        editNotiUser1.isEnabled = false
        editNotiUser2.isEnabled = false
        editNotiUser3.isEnabled = false
        editEmail1.isEnabled = false
        editEmail2.isEnabled = false
        editEmail3.isEnabled = false
        editRecDate.isEnabled = false
        editTranCate.isEnabled = false
        editEveDesc.isEnabled = false
        editMob1.isEnabled = false
        editMob2.isEnabled = false
        editMob3.isEnabled = false
        editSMS.isEnabled = false
        editEmail.isEnabled = false
        editAlert.isEnabled = false
        editStDate.isEnabled = false
        editEndDate.isEnabled = false
        editFrq.isEnabled = false
        hideHint(editsrlNo, editEveNo, editNotLimit, editNotiUser1, editNotiUser2, editNotiUser3,
            editEmail1, editEmail2, editEmail3, editRecDate, editTranCate, editEveDesc,
            editMob1, editMob2, editMob3, editSMS, editEmail, editAlert,editStDate,editEndDate,editFrq )
        setEditTextsColor(
            editsrlNo, editEveNo, editNotLimit, editNotiUser1, editNotiUser2, editNotiUser3,
            editEmail1, editEmail2, editEmail3, editRecDate, editTranCate, editEveDesc,
            editMob1, editMob2, editMob3, editSMS, editEmail, editAlert,editStDate,editEndDate,editFrq )
        notifyBtn?.visibility = View.GONE
    }
    private fun validateFields(): Boolean {
        val emailRegex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+$")
        if(editsrlNo.text.toString().isEmpty() || editEveNo.text.toString().isEmpty() || editTranCate.text.toString().isEmpty() || editEveDesc.text.toString().isEmpty() ||
            editStDate.text.toString().isEmpty() || editEndDate.text.toString().isEmpty() || editMob1.text.toString().isEmpty() ||
            editNotiUser1.text.toString().isEmpty() || editEmail1.error!=null || editEmail2.error!=null || editEmail3.error!=null || editMob1.error!=null ||editMob2.error!=null || editMob3.error!=null ||editMob1.error!=null
            ) {
            if (editsrlNo.text.toString().isEmpty()) {
                editsrlNo.error = "Please Enter SRl Number"
            }
            if (editEveNo.text.toString().isEmpty()) {
                editEveNo.error = "Please Enter Event Number"
            }
            if (editTranCate.text.toString().isEmpty()) {
                editTranCate.error = "Please Enter Tran Category"
            }
            if (editEveDesc.text.toString().isEmpty()) {
                editEveDesc.error = "Please Enter Description"
            }
            if(editStDate.text.toString().isEmpty() ){
                editStDate.error = "Please Select Start Date"
            }
            else{
                editStDate.error = null
                editStDate.clearFocus()
            }
            if(editNotiUser1.text.toString().isEmpty())
            {
                editNotiUser1.error = "Please Enter User"
            }
            if(editEndDate.text.toString().isEmpty()){
                editEndDate.error = "Please Select End date"
            }
            else{
                editEndDate.error = null
                editEndDate.clearFocus()
            }
            if(editFrq.text.toString().isEmpty()){
                editFrq.error = "Please Choose Frequency"
            }
            else{
                editFrq.error = null
                editFrq.clearFocus()
            }
            if(editMob1.text.toString().isEmpty()){
                editMob1.error = "Please Enter Mobile Number"
            }
            else if(editMob1.error!=null){
            }
            return false
        }
        else{
            if(editEmail1.text.toString().isNotEmpty())
            {
                if(!editEmail1.text.toString().matches(emailRegex)){
                    editEmail1.error = "Inavalid Email"
                 return false
                }
            }
            if(editMob2.text.toString().isNotEmpty()){
                if(editMob2.error!=null){
                    return false
                }
            }
            if(editMob3.text.toString().isNotEmpty()){
                if(editMob3.error!=null){
                    return false
                }
            }
            if(editEmail2.text.toString().isNotEmpty()){
                if(!editEmail2.text.toString().matches(emailRegex)){
                    editEmail2.error = "Inavalid Email"
                    return false
                }
            }
            if(editEmail3.text.toString().isNotEmpty()){
                if(!editEmail3.text.toString().matches(emailRegex)){
                    editEmail3.error = "Inavalid Email"
                    return false
                }
            }
            return true
        }
        return true
    }

    private fun setCurrentDate(editText: EditText) {
        val calendar: Calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.getTime())
        editText.setText(currentDate)
    }
    private fun openStartDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                editStDate.setText(dateFormat.format(selectedDate.time))
                editEndDate.text.clear() // Clear end date when start date changes
            },
            year,
            month,
            day
        )
        // Restrict start date to current and future dates
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
        editStDate.error=null
        editStDate.clearFocus()
    }

    private fun openEndDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val startDate = dateFormat.parse(editStDate.text.toString())
        calendar.time = startDate ?: Calendar.getInstance().time

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                editEndDate.setText(dateFormat.format(selectedDate.time))
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
        editEndDate.error=null
        editEndDate.clearFocus()
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    fun setEditTextsColor(vararg editTexts: EditText) {
        for (editText in editTexts) {
            editText.setTextColor(getResources().getColor(R.color.lightgrey))
        }
    }

    private fun hideHint(vararg editTexts: EditText){
        for (editText in editTexts) {
            editText.hint = null
        }
    }
    fun getTextOrDefault(editText: EditText): String {
        return editText.text.toString().takeIf { it.isNotEmpty() } ?: " "
    }
    private fun getNotiUniqueId() {
        val call = ApiClient.apiService.getNotificationId()
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val response = response.body()
                    editsrlNo.setText(response.toString())
                }
                else {
                    Toast.makeText(this@NotifyDetailsScreen, "Error: "+response.message(), Toast.LENGTH_LONG).show()
               //     println("Failed to get notification id: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
              //  Log.d("Get Notification Id", "Network error: ${t.message}")
            }
        })
    }
}