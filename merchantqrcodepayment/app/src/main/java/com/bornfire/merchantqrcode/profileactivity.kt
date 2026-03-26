package com.bornfire.merchantqrcode

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.bornfire.merchantqrcode.AppMonitorService
import com.bornfire.merchantqrcode.DataModel.*
import com.bornfire.merchantqrcode.Utils.EditNonEdit
import com.bornfire.merchantqrcode.Utils.EditNonEdit.setTextWhiteColor
import com.bornfire.merchantqrcode.Utils.HelpInfo
import com.bornfire.merchantqrcode.Utils.NullCheck
import com.bornfire.merchantqrcode.Utils.PdfDateSelect
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.bornfire.merchantqrcode.Utils.PdfDateSelect.tranFrom
import com.bornfire.merchantqrcode.Utils.PdfDateSelect.tranTo
import com.bornfire.merchantqrcode.Utils.SuccesssDialog.showImageAlertDialog
class profileactivity : BaseActivity() {
    val usercategory= SharedusercatDataObj.UserCategory
    private lateinit var updateBtn:Button
    val userData = SharedUserDataObj.userData
    val merchantdata = SharedMerchantDataObj.merchantData
    private lateinit var userID: EditText
    lateinit var userName: EditText
    private lateinit var userMobNo: EditText
    private lateinit var userEmail: EditText
    private lateinit var swpTxt1:TextView
    private lateinit var swpTxt2:TextView
    private lateinit var swpEt1: EditText
    private lateinit var swpEt2 :EditText
    private lateinit var countryCodeWithPlus:String
    lateinit var merchantId: EditText
    lateinit var merchantName: EditText
    lateinit var unitId:EditText
    lateinit var unitName:EditText
    private lateinit var unitType:EditText
    private lateinit var disFromDate:EditText
    private lateinit var disToDate:EditText
    private lateinit var formattedDate1: String
    private lateinit var formattedDate2: String
    private lateinit var imgBack: ImageView
    private lateinit var editBtn: ImageView
    private lateinit var countryCodePicker: CountryCodePicker
    private lateinit var disablefromtext:TextView
    private lateinit var disabletodatetext:TextView
    private lateinit var helpImg:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profileactivity)
        val isTablet = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        requestedOrientation = if (isTablet) { ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        imgBack = findViewById(R.id.back_img)
        editBtn = findViewById(R.id.editableBtn)
        helpImg = findViewById(R.id.pro_help_image)
        helpImg.setOnClickListener{ HelpInfo.getInfo(this,"8") }
        userID = findViewById(R.id.etUserId)
        userName = findViewById(R.id.etUserName)
        merchantId = findViewById(R.id.etMerId)
        merchantName = findViewById(R.id.etMerName)
        userEmail = findViewById(R.id.etEmail)
        disFromDate = findViewById(R.id.etUserDisFrDate)
        disToDate = findViewById(R.id.etUserDisToDate)
        userMobNo = findViewById(R.id.etMob)
        countryCodePicker = findViewById(R.id.country_code)
        unitId = findViewById(R.id.etunitId)
        unitName = findViewById(R.id.etuniName)
        unitType = findViewById(R.id.etUnitType)
            swpTxt1 = findViewById(R.id.swap_text1)
            swpTxt2 = findViewById(R.id.swap_text2)
            swpEt1 = findViewById(R.id.swpEt1)
            swpEt2 = findViewById(R.id.swpEt2)
            disablefromtext=findViewById(R.id.fromdisabletext)
            disabletodatetext=findViewById(R.id.todisabletext)
           updateBtn = findViewById(R.id.updateBtn)
            if(usercategory?.user_category=="Representative") {
                swpTxt1.text = "Number of Concurrent User"
                swpTxt2.text = "Number of Active Device"
                editBtn.visibility=View.VISIBLE
                disablefromtext.visibility=View.VISIBLE
                disabletodatetext.visibility=View.VISIBLE
                disFromDate.visibility=View.VISIBLE
                disToDate.visibility=View.VISIBLE
                setMerchantProfile()
            }
            else{
                swpTxt1.text = "User Designation"
                swpTxt2.text = "User Role"
                userProfile()
            }
        imgBack.setOnClickListener{ finish() }
        editBtn.setOnClickListener{
            updateBtn.visibility=View.VISIBLE
            setTextWhiteColor(applicationContext,userMobNo,userEmail,userName,swpEt1,swpEt2,disFromDate,disToDate)
                    EditNonEdit.setEditTextsEditable(userMobNo, userEmail, userName, swpEt1, swpEt2)
                    disFromDate.isEnabled = true
                    disFromDate.inputType = InputType.TYPE_NULL
                    disToDate.isEnabled = true
                    disToDate.inputType = InputType.TYPE_NULL
                    countryCodePicker.setOnCountryChangeListener{ userMobNo.text.clear() }
                    userMobNo.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            PhNumValidation.updateExpectedLength(countryCodePicker.selectedCountryNameCode, userMobNo)
                        }
                    })
        }
        supportActionBar?.hide()
        val serviceIntent = Intent(this, AppMonitorService::class.java)
        startService(serviceIntent)
        updateBtn.setOnClickListener{
                    updateMerchantProfile() }

    }
    private fun updateMerchantProfile() {
        if(disFromDate.text.isNotEmpty()){
            val inputDate1 = disFromDate.text.toString()
            val inputFormat1 = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val outputFormat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date1: LocalDate = LocalDate.parse(inputDate1, inputFormat1) // Parse input date string
            formattedDate1 = date1.format(outputFormat1)
            disFromDate.setText(formattedDate1)
        }
        if(disToDate.text.isNotEmpty()){
            val inputDate1 = disToDate.text.toString()
            val inputFormat1 = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val outputFormat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date1: LocalDate = LocalDate.parse(inputDate1, inputFormat1) // Parse input date string
            formattedDate2 = date1.format(outputFormat1)
            disToDate.setText(formattedDate2) }
        val updprofile = UpdateProfile(userID.text.toString(),userName.text.toString(),merchantId.text.toString(),merchantName.text.toString(),
       "", tranFrom, tranTo,swpEt1.text.toString().toInt(),swpEt2.text.toString().toInt(),userMobNo.text.toString(),userEmail.text.toString(),
            unitType.text.toString(),unitName.text.toString(),countryCodePicker.selectedCountryCode.toString())
        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(updprofile)
        val psuDeviceID = Encryption.generatePSUDeviceId()
        val encryptedText = Encryption.encrypt(jsonString, psuDeviceID)
        val encryptedRequest = EncryptedRequest(encryptedstring = encryptedText)
        val call = ApiClient.apiService.updateUserProfile(encryptedRequest,psuDeviceID)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val encryptedResponse = response.body()?.string()
                    encryptedResponse?.let { it ->
                        val decryptedResponse = Encryption.decrypt(it, psuDeviceID)
                     //   println("Decrypted response: $decryptedResponse")
                        val keyValuePairs = decryptedResponse.substringAfter("[").substringBeforeLast("]")
                        val keyValueMap = keyValuePairs.split(",").associate {
                            it.split("=").let { (key, value) -> key.trim() to value.trim() }
                        }
                        // Extract values from the map with null safety
                        fun getValue(key: String) = keyValueMap[key] ?: ""
                        val shareMerchantData = SharedMerchantData(
                            merchant_rep_id = getValue("merchant_rep_id"),
                            mer_representative_name = getValue("mer_representative_name"),
                            merchant_user_id = getValue("merchant_user_id"),
                            merchant_name = getValue("merchant_name"),
                            merchant_legal_user_id = getValue("merchant_legal_user_id"),
                            merchant_corporate_name = getValue("merchant_corporate_name"),
                            password = getValue("password"),
                            password_expiry_date = getValue("password_expiry_date"),
                            password_life = getValue("password_life"),
                            account_expiry_date = getValue("account_expiry_date"),
                            user_disable_flag = getValue("user_disable_flag"),
                            user_disable_from_date = getValue("user_disable_from_date"),
                            user_disable_to_date = getValue("user_disable_to_date"),
                            del_flag = getValue("del_flag"),
                            user_status = getValue("user_status"),
                            login_status = getValue("login_status"),
                            login_channel = getValue("login_channel"),
                            mobile_no = getValue("mobile_no"),
                            alternate_mobile_no = getValue("alternate_mobile_no"),
                            email_address = getValue("email_address"),
                            alternate_email_id = getValue("alternate_email_id"),
                            no_of_concurrent_users = getValue("no_of_concurrent_users").toIntOrNull() ?: 0,
                            no_of_active_devices = getValue("no_of_active_devices").toIntOrNull() ?: 0,
                            entry_user = getValue("entry_user"),
                            modify_user = getValue("modify_user"),
                            verify_user = getValue("verify_user"),
                            entry_time = getValue("entry_time"),
                            modify_time = getValue("modify_time"),
                            verify_time = getValue("verify_time"),
                            unit_id = NullCheck.getValidText(getValue("unit_id")),
                            unit_type = getValue("unit_type"),
                            unit_name = getValue("unit_name"),
                            maker_or_checker = getValue("maker_or_checker"),
                            entry_flag = getValue("entry_flag"),
                            modify_flag = getValue("modify_flag"),
                             "",
                           "Y",
                            countrycode = NullCheck.getValidText(getValue("countrycode")))
                        SharedMerchantDataObj.merchantData = shareMerchantData
                        showImageAlertDialog(this@profileactivity,"Profile Updated Successfully",MainActivity::class.java)
                    }
                }
                else{
                    Toast.makeText(this@profileactivity, "Error: "+response.message(), Toast.LENGTH_LONG).show()
               //     println("Error: ${response.code()}")
                }}
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
              //  println("Network error: ${t.message}")
                Toast.makeText(this@profileactivity,"Something Went Wrong at Server End",Toast.LENGTH_LONG).show()
            }
        })
    }
    override fun onSupportNavigateUp(): Boolean {
      finish()
         return true }
    private fun setMerchantProfile(){
        countryCodeWithPlus = countryCodePicker.selectedCountryCode
        countryCodePicker.setOnCountryChangeListener { countryCodeWithPlus = countryCodePicker.selectedCountryCode }
        userID.setText(if (merchantdata?.merchant_rep_id == null || merchantdata.merchant_rep_id == "null") "" else merchantdata.merchant_rep_id)
        userName.setText(if (merchantdata?.mer_representative_name == null || merchantdata.mer_representative_name == "null") "" else merchantdata.mer_representative_name)
        merchantId.setText(if (merchantdata?.merchant_user_id == null || merchantdata.merchant_user_id == "null") "" else merchantdata.merchant_user_id)
        merchantName.setText(if (merchantdata?.merchant_name == null || merchantdata.merchant_name == "null") "" else merchantdata.merchant_name)
        userMobNo.setText(if (merchantdata?.mobile_no == null || merchantdata.mobile_no.toString() == "null") "" else merchantdata.mobile_no.toString())
        userEmail.setText(if (merchantdata?.email_address == null || merchantdata.email_address == "null") "" else merchantdata.email_address)
        swpEt1.setText(if (merchantdata?.no_of_concurrent_users == null || merchantdata.no_of_concurrent_users.toString() == "") "" else merchantdata.no_of_concurrent_users.toString())
        swpEt2.setText(if (merchantdata?.no_of_active_devices == null || merchantdata.no_of_active_devices.toString() == "null") "" else merchantdata.no_of_active_devices.toString())
        unitId.setText(if (merchantdata?.unit_id == null || merchantdata.unit_id == "null") "" else merchantdata.unit_id)
        unitName.setText(if (merchantdata?.unit_name == null || merchantdata.unit_name == "null") "" else merchantdata.unit_name)
        unitType.setText(if (merchantdata?.unit_type == null || merchantdata.unit_type == "null") "" else merchantdata.unit_type)
        val countrycode = merchantdata?.countrycode
        if(countrycode!=null){
            countryCodePicker.setCountryForPhoneCode(countrycode.toInt())
        }
        formatAndSetDate(merchantdata?.user_disable_from_date, disFromDate)
        formatAndSetDate(merchantdata?.user_disable_to_date, disToDate)
            disFromDate.setOnClickListener{
                disFromDate.inputType = InputType.TYPE_NULL
                disFromDate.isFocusable = false
                disFromDate.isFocusableInTouchMode = false
                PdfDateSelect.showDatePickerDialog(true, disFromDate, disToDate, this,true)
            }
            disToDate.setOnClickListener{
                PdfDateSelect.showDatePickerDialog(false, disFromDate, disToDate, this,true)
            }
    }
    private fun userProfile(){
            userID.setText(if (userData?.user_id == null || userData.user_id == "null") "" else userData.user_id)
            userName.setText(if (userData?.user_name == null || userData.user_name == "null") "" else userData.user_name)
            userMobNo.setText(if (userData?.mobile_no1 == null || userData.mobile_no1.toString() == "null") "" else userData.mobile_no1.toString())
            userEmail.setText(if (userData?.email_address1 == null || userData.email_address1 == "null") "" else userData.email_address1)
            merchantId.setText(if (userData?.merchant_user_id == null || userData.merchant_user_id == "null") "" else userData.merchant_user_id)
            merchantName.setText(if (userData?.merchant_name == null || userData.merchant_name == "null") "" else userData.merchant_name)
            unitId.setText(if (userData?.unit_id == null || userData.unit_id == "null") "" else userData.unit_id)
            unitName.setText(if (userData?.unit_name == null || userData.unit_name == "null") "" else userData.unit_name)
            unitType.setText(if (userData?.unit_type == null || userData.unit_type == "null") "" else userData.unit_type)
            swpEt1.setText(if(userData?.user_designation == null || userData.user_designation == "null") "" else userData.user_designation)
            swpEt2.setText(if(userData?.user_role == null || userData.user_role == "null") "" else userData.user_role)
            val countrycode = userData?.countrycode?.toInt()
            if(countrycode!=null){
                countryCodePicker.setCountryForPhoneCode(countrycode) }
    }
    private fun formatAndSetDate(dateStr: String?, editText: EditText) {
        if (dateStr == "null" || dateStr.isNullOrBlank()) {
            editText.setText("")
        } else {
            val inputDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault())
            val outputDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val inputDate = inputDateFormat.parse(dateStr!!)
            val outputDateStr = outputDateFormat.format(inputDate!!)
            editText.setText(outputDateStr) }
    }
}